# Openresty-proxy

## 用途
目前我们每个namespace,对应用的请求使用的域名都是规范的: app_code.bx.missfresh.net

通过openresty解析请求的host,动态的将请求转发到相应的svc上。


## 好处
- 不再需要频繁改动ingress.yml配置。每套测试环境仅需要绑定一个代理服务即可
- 避免了大量的应用使用NodePort暴露集群端口
- 
