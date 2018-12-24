# Kubenetes部署高可用Redis
参考：https://blog.frognew.com/2017/08/kubernetes-redis-stateful-set.html

-----

# 环境准备
## 启动
替换DEPLOY_ENV为对应环境(b1,b2,..)后，执行

```sudo kubectl create -f redis-HA/```

## 清理
```sudo kubectl delete -f redis-HA/```

## pv清理
腾讯云会根据使用情况，自动扩容(挂在硬盘），不需要关心

## pvc如何清理？
```e.g. sudo kubectl delete pvc redis-volume-redis-0 -n b1```

## 扩容
修改yml文件后

```sudo kubectl apply -f redis-HA/```

# 使用

## password
redis_password：6V76CR20EOGse*ZL5lmwMYdyqrotHQ

## 容器内连接
redisCli -h redis-master.env -a password

