# 继承的基础镜像，注意jdk和tomcat版本需要配套
FROM java:8u111-jdk

MAINTAINER weibing@missfresh.cn

# 修改这里的值，使用实际的jar来进行替换
ENV PROJETC_HOME matrix-codeanalyzer
ENV TARGET_DIR matrix-codeanalyzer-web/target
ENV TARGET_JAR matrix-codeanalyzer.jar
ENV LOG_PATH /data/logs/matrix-codeanalyzer
# 修改部署环境信息，这个操作在创建镜像之前，通过修改Dockerfile完成
#ENV DEPLOY_ENV ENV_VARIABLE

RUN mkdir -p /data/logs/$PROJETC_HOME

# 复制必要数据存入镜像
COPY $TARGET_DIR/$TARGET_JAR /data/app/$TARGET_JAR
#COPY ./start.sh /data/scripts/start.sh


# 容器启动的命令，注意使用数组格式，否则会被解析成sh -c cmd，导致启动后容器立即退出
#CMD ["/bin/bash", "/data/scripts/start.sh"]
CMD ["java", "-jar", "/data/app/matrix-codeanalyzer.jar"]
