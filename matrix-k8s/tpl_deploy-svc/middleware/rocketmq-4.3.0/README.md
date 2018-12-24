# 搭建k8s rocketMq

## 参考
- http://rocketmq.apache.org/docs/quick-start/
- https://github.com/apache/rocketmq-externals/tree/master/rocketmq-docker
- https://hub.docker.com/r/styletang/rocketmq-console-ng/
- http://laciagin.me/2017/12/07/RocketMQ%E6%90%AD%E5%BB%BA%E5%8F%8A%E5%88%A8%E5%9D%91/
- https://blog.csdn.net/jiangyu1013/article/details/82414932

## 测试
```
sh tools.sh org.apache.rocketmq.example.quickstart.Producer
sh tools.sh org.apache.rocketmq.example.quickstart.Consumer
```


## 坑
- 启动参数调大，避免挂掉
- broker启动需要添加 autoCreateTopicEnable=true 参数，开启自动创建topic, 否则需要手工导入，也会导致测试失败
- broker启动还需要设置环境变量NAMESRV_ADDR:localhost:9876, 否则可能不通
- console-ng启动参数修改为 -Drocketmq.config.namesrvAddr，否则启动失败
