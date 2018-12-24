# 继承的基础镜像，注意jdk和tomcat版本需要配套
FROM harbor.missfresh.net/library/java:8u191-jdk

MAINTAINER weibing@missfresh.cn

# 修改这里的值，使用实际的jar来进行替换
ENV PROJETC_HOME matrix-process
ENV TARGET_DIR matrix-process-web/target
ENV TARGET_JAR matrix-process.jar
#ENV HEALTHCHECK_URL :8080/api/matrix/healthcheck
ENV LOG_PATH /data/logs/matrix-process
# 修改部署环境信息，这个操作在创建镜像之前，通过修改Dockerfile完成
#ENV DEPLOY_ENV ENV_VARIABLE

RUN mkdir -p /data/logs/$PROJETC_HOME

# 复制必要数据存入镜像
COPY $TARGET_DIR/$TARGET_JAR /data/app/$TARGET_JAR
#COPY ./start.sh /data/scripts/start.sh

# 加入healthcheck检查，检查失败则认为容器启动失败
# 注意，healthcheck的端口应该使用8080
#HEALTHCHECK --interval=5s --timeout=3s \
 # CMD curl -fs http://localhost$HEALTHCHECK_URL || exit 1

# 容器启动的命令，注意使用数组格式，否则会被解析成sh -c cmd，导致启动后容器立即退出
#CMD ["/bin/bash", "/data/scripts/start.sh"]
CMD ["java", "-jar", "/data/app/matrix-process.jar"]
