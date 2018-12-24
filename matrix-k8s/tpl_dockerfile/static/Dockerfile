FROM harbor.missfresh.net/library/alpine-nginx:1.12.2
MAINTAINER missfresh.com

RUN apk add curl

# 将dist目录直接复制到/data目录下，重命名为app，注意不要添加多余的斜线
COPY dist /data/app
# 复制配置文件到ng配置下，注意需要添加斜线
COPY env/default.conf /etc/nginx/conf.d/
# start.sh需要执行chmod +x， 配置可执行权限
COPY start.sh /data/app/

CMD ["/bin/bash", "-c", "/data/app/start.sh"]


