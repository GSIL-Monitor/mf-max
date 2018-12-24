## 参考
- https://cloud.tencent.com/developer/article/1046647
- https://andrewpqc.github.io/2018/04/24/setup-k8s-dashboard-on-cluster/

## 坑
- 初始化提示/certs目录不存在
  - 需要手工创建
  
```
# 创建cert文件
$ openssl req -newkey rsa:4096 -nodes -sha256 -keyout dashboard.key -x509 -days 365 -out dashboard.crt

# 移动到/certs目录下
$ mkdir /certs && mv dashboard* /certs

# 手工创建secret
$ kubectl create secret generic kubernetes-dashboard-certs --from-file=/certs -n kube-system

# 然后再执行创建
$ kubectl create -f kubernetes-dashboard.yml
```

- 安全组添加了端口访问的权限控制，需要手工指定service的nodePort
  - 参考可用端口：http://wiki.missfresh.net/pages/viewpage.action?pageId=36241497
  
## 访问
https://集群节点ip:nodePort
使用https://10.2.4.15:30920访问
