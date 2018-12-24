FROM alpine:3.8
MAINTAINER missfresh.com
# harbor.missfresh.net/library/filebeat:5.4.0

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk update && apk add --no-cache bash tzdata curl\
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && cd /usr/local && \
    curl -L -O "https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-5.4.0-linux-x86_64.tar.gz" && \
    tar xvf filebeat-5.4.0-linux-x86_64.tar.gz && \
    rm -f filebeat-5.4.0-linux-x86_64.tar.gz && \
    ln -s /usr/local/filebeat-5.4.0-linux-x86_64 /usr/local/filebeat && \
    chmod +x /usr/local/filebeat/filebeat && \
    mkdir -p /etc/filebeat


CMD ["/usr/local/filebeat/filebeat","-e","-c","/etc/filebeat/filebeat.yml"]