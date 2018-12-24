# 日志收集套件

- filebeat参考
  - https://jimmysong.io/posts/kubernetes-filebeat/
  - https://github.com/rootsongjc/docker-images
- es, kibana配置参考 版本6.3.2
  - https://github.com/pires/kubernetes-elasticsearch-cluster/tree/master/stateful （使用Statefulset模式搭建es）
  - https://akomljen.com/kubernetes-persistent-volumes-with-deployment-and-statefulset/


## 核心概念
- 将filebeat和server放入同一个pod中，进行日志的监听处理
- es-data用于保存日志，将数据保存在pvc中


## 套件
- filebeat
- elasticsearch
- kibana

## 数据维护
- 查看索引：curl http://elasticsearch:9200/_cat/indices?v
- 删除索引：curl -XDELETE 'http://elasticsearch:9200/logstash-*'

这套es 配置是master, node, client分开的，node上存储数据，所以master挂一个，node挂一个， client都不会影响数据和功能。
最好保证3个master 2个node是分布在不同的节点上的, 避免一台节点挂掉导致日志服务不可用


历史数据的维护：通过删除旧索引来删除不需要的文档信息
- 参考：https://www.elastic.co/guide/cn/elasticsearch/guide/cn/retiring-data.html


## 单个yml模板
```yaml
piVersion: v1
kind: Pod
metadata:
  name: filebeat-test
  namespace: default
  labels:
    name: tomcat
spec:
  containers:
  - image: harbor.missfresh.net/library/filebeat:5.4.0
    imagePullPolicy: Always
    name: filebeat
    volumeMounts:
    - name: app-logs
      mountPath: /log
    - name: filebeat-config
      mountPath: /etc/filebeat/
  - image: tomcat
    imagePullPolicy: Always
    name: tomcat-test
    volumeMounts:
    - name: app-logs
      mountPath: /usr/local/tomcat/logs
    env: ## 变量，用于替换配置文件中的系统变量
    - name: PATHS
      value: "/usr/local/tomcat/logs"
  volumes:
    - name: app-logs
      emptyDir: {}
    - name: filebeat-config
      configMap:
        name: filebeat-config
---
apiVersion: v1
kind: Service
metadata:
  name: filebeat-test
  labels:
    app: filebeat-test
spec:
  ports:
  - port: 80
    protocol: TCP
    name: http
  selector:
    run: filebeat-test
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
data:
  filebeat.yml: |
    filebeat.prospectors:
    - input_type: log
      paths:
        - "/log/*"
        # 此处可以增加多条日志路径
        - “/data/logs/*”
    output.elasticsearch:
      hosts: ["elasticsearch:9200"]
      username: "elastic"
      password: "changeme"
      index: "filebeat-docker-test-%{+yyyy.MM.dd}"
```

