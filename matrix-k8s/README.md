# 测试环境docker(k8s)化
本项目用于维护测试环境docker(k8s)化的配置文件和相关运行脚本

### 根目录
- build_with_app_arg.py: 编译和部署的脚本 - 已废弃，迁移到使用Matrix系统进行发布
- requirements.txt: python依赖的包
- tpl_dockerfile: 根据不同build_type创建的Dockerfile模板

### public_template
存放基本的模板文件
- deploy-svc: deployment和service的模板配置
- ingress: ingress的模板配置

### tpl_deploy-svc
存放与环境无关的配置文件，根据主业务线进行拆分，根据app_code命名对应应用的配置。
需要根据应用进行调整。

| id | 二级业务线 | 缩写编码 |
| ------ | ------ | ------ |
| 6 | 物流产研部 | wuliu |
| 9 | C端产研部 | mryx |
| 11 | 算法部 | alg |
| 13 | 数据部 | data |
| 16 | 基础平台部 | base |
| 20 | 业务中台部 | mryx |
| 33 | 便利购产研部 | blg |
| 34 | 门店系统部 | mryx |
| 35 | 运营管理部 | mryx |
| 78 | 每日一淘 | mryt |


### tpl_ingress
存放与环境无关的ingress配置，根据主业务线划分，文件内，包含各业务线下所有ingress配置


### script
用于存放相关的脚本
- fastcopy_deploy-svc.py: 根据模板生成对应环境配置的脚本
- fastcopy_ingress.py: 根据模板生成对应环境ingress的脚本
- deploy-middleware.py: 向不同环境部署中间件的脚本

# TODO
- api接入，查看日志
https://k8smeetup.github.io/docs/tasks/administer-cluster/access-cluster-api/
- 部署新环境的镜像
  - master + latest tag，优先顺序master > latest. master在线上发布时创建，latest在测试发布时创建
- 物流接入js-rhino项目 - 不再接入
```
1. js代码，直接扔到tomcat目录下，启动即可 - jdk8, tomcat8.0.x
启动时替换环境信息	
在启动的shell脚本中，将指定的环境变量进行替换后，在进行启动
8899.js中，使用NGINX_ENV代替环境信息，
环境变量中设置NGINX_ENV
启动脚本中，使用sed -i 将8899.js中的文件进行替换后，完成启动
- pod中不使用sudo，避免了无法获取系统变量的问题 
2. ng + lua， 加载ng, 将lua放入指定目录下
require "os"
os.getenv("系统变量")
https://hub.docker.com/r/openresty/openresty/tags/
需要将interface.conf加入到启动conf中
同时将upstream中的redis信息进行相应的变更，如何替换？？
直接替换nginx.conf文件 /usr/local/openresty/nginx
将Lua脚本复制到指定目录，然后启动 /data/conf
3. redis(v3.0.5) - 根据配置文件启动
http://futeng.iteye.com/blog/2071867
harbor.missfresh.net/library/redis:3.0.5
需要几个？2个端口？
4. 
```
