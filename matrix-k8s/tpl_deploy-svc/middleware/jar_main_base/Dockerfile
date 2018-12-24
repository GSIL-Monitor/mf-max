# 继承的基础镜像，注意jdk和tomcat版本需要配套
FROM harbor.missfresh.net/library/java:8u192-jdk
# harbor.missfresh.net/library/java:8u192-jdk-jar-main

MAINTAINER missfresh.cn

# 复制必要数据存入镜像
COPY phantomjs-2.1.1-linux-x86_64  /home/www/datasource/phantom/phantomjs-2.1.1-linux-x86_64
COPY *.ttf /usr/share/fonts/win/
COPY *.ttc /usr/share/fonts/win/

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && apk update && apk add --no-cache fontconfig && fc-cache -f

