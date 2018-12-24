#!/bin/bash
rtag_name=$1
jar_name=$2

if [ "$rtag_name" == "" ]; then
    echo "发布失败，rtag_name为空..."
    exit
fi

if [ "$jar_name" == "" ]; then
    echo "发布失败，jar_name为空..."
    exit
fi

tag_type=${rtag_name:0:2}
if [ "$tag_type" != "r-" ]; then
    echo "回滚失败，rtag格式不正确..."
    exit
fi

if [ ! -f "/data/backup/${rtag_name}.jar" ];then
    echo "回滚失败，备份jar包 ${rtag_name}.jar 不存在..."
    exit
fi


source /etc/profile
IP_NAME=`hostname -I`
pid=`pgrep -f ${jar_name}`
kill $pid
echo "kill $pid"
sleep 20s
echo "sleep 20s"
pid=`pgrep -f ${jar_name}`
if [ "$pid" != "" ];then
    kill -9 $pid
    echo 'kill -9 $pid'
fi
cp /data/backup/${rtag_name}.jar /data/app/${jar_name}
echo '已部署'
project_name=`echo $jar_name | cut -d \. -f 1`
nohup java -javaagent:/data/app/pinpoint-agent-1.7.3/pinpoint-bootstrap-1.7.3.jar -Dpinpoint.agentId=$IP_NAME -Dpinpoint.applicationName="${project_name}" -jar -Xmx6g -Xms6g -Xmn3g -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=256M  -Xloggc:/data/logs/gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/logs/java.hprof /data/app/${jar_name} >/dev/null 2>&1 &
echo '正在启动...'