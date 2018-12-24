FROM alpine:3.8
MAINTAINER missfresh.com
# harbor.missfresh.net/library/alpine-nginx:1.12.2

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk update && apk add --no-cache bash tzdata nginx\
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && mkdir -p /run/nginx

COPY env/default.conf /etc/nginx/conf.d/

CMD ["nginx", "-g", "daemon off;"]