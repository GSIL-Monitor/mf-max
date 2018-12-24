FROM alpine:3.8
MAINTAINER missfresh.cn
# harbor.missfresh.net/library/redis:ha-sentinel

RUN apk add --no-cache redis sed bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

COPY redis-master.conf /redis-master/redis.conf
COPY redis-slave.conf /redis-slave/redis.conf
COPY run.sh /run.sh

CMD [ "bash", "-c", "/run.sh" ]
