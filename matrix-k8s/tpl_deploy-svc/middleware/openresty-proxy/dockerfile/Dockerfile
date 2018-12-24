# 继承的基础镜像，注意jdk和tomcat版本需要配套
FROM openresty/openresty:centos
# harbor.missfresh.net/library/openresty-proxy:centos

MAINTAINER missfresh.cn

COPY stringm.lua /usr/local/openresty/lualib/stringm.lua
COPY lua.conf /usr/local/openresty/nginx/conf/lua.conf
COPY nginx.conf /usr/local/openresty/nginx/conf/nginx.conf


# 容器启动的命令，注意使用数组格式，否则会被解析成sh -c cmd，导致启动后容器立即退出
CMD ["nginx", "-g", "daemon off;"]
