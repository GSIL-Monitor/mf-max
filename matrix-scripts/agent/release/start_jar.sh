#!/bin/bash
#本脚本基本无需改动，注意要点均已用中文说明
#建议使用 . xx.sh 命令执行脚本。如果使用sh xx.sh执行，注意is_exist方法里的注释
#获取脚本名称
SCRIPT=$0

#获取进程名称，必须为完整程序名，否则可能会误操作其他进程
APP_NAME=$1

#获取操作符
OPERATOR=$2

#获取其他参数
JVM_OPS=$3
#服务临时路径
APP_PATH_TEMP=$4
echo "${APP_NAME} 的操作是:$2"
echo "${APP_NAME} 的JVM_OPS是:$3"
source /etc/profile

usage() {
    echo "Usage: sh $SCRIPT [app_name] [start|stop|restart|status |clear] JVM_OPS"
    exit 1
}

#判断是否输入了四个个参数
#注意①
if [ $# != 4 ]; then
    usage
fi

if [  "${APP_PATH_TEMP}" == "" ];then
   echo "APP_PATH_TEMP是空字符串！"
   exit 1
fi

is_exist(){
  #过滤grep命令本身

  pid= `ps aux |grep "java"|grep " /data/app/${APP_NAME}"|grep -v "grep"|awk '{ print $2}'`
  if [ "${pid}" == "" ]; then
      return 1
  else
    return 0
  fi
}

findPID(){
	 echo `ps ax --width=1000 | grep java | grep ${APP_NAME} | awk '{print $1}'`

}


clear(){
   if [ -f "/data/app/${APP_NAME}" ]; then
      rm -rf "/data/app/${APP_NAME}"
      echo "/data/app/${APP_NAME} 已经清理！"
   fi

   if [ -d "/data/app/jettys/${APP_NAME}" ]; then
      rm -rf "/data/app/jettys/${APP_NAME}/webapps/*"
      echo "/data/app/jettys/${APP_NAME}/webapps/* 已经清理！"
   fi
}

isJarOrWar(){
    if ["${APP_NAME##*.}" == "jar"];then
        echo "jar"
    elif ["${APP_NAME##*.}" == "war"];then
        echo "war"
    fi
}

start(){
    # TODO 这里简单粗暴了!
    jar="jar"
    war="war"
    result_jar=$(echo $APP_NAME | grep "${jar}")
    if [[ "$result_jar" != "" ]]
    then

        nohup java ${JVM_OPS} -jar -server /data/app/${APP_NAME} > /dev/null 2>&1 &
        echo "这是一个jar包项目：$result_jar 已经完成启动"
    fi
    result_war=$(echo $APP_NAME | grep "${war}")
    if [[ "$result_war" != "" ]]
    then
        nohup /data/app/jettys/${APP_NAME}/bin/jetty.sh start
        echo "这是一个war包项目：$result_war 已经完成启动"
    fi
}

stop(){
 echo -n "Shutting down $APP_NAME.."
 PID=$(findPID)
 if [ -n "$PID" ]; then
     echo "开始kill了!${PID}"
  kill ${PID}
               echo "sleep 20s"
                sleep 20
                PID=$(findPID)
                if [ "${PID}" != "" ];then
       kill -9 ${PID}
       echo 'kill -9 ${PID}'
  fi
  echo "kill 完了!"
 else
  echo "${APP_NAME} is not running"
 fi
}

status(){
	PID=$(findPID)
	if [ -n "$PID" ] ; then
		echo "${APP_NAME} is running"
	else
		echo "${APP_NAME} is not running"
	fi
}
mvFile(){
    cd "$1"
    echo "mvFile-param:$1"
    if [ $? -eq 0 ]; then
	    echo "开始mv了!"
	    mv * ../
		echo "mv 完了!"
	else
		echo "$1 is not exist"
	fi
}
restart(){
  stop
#  clear
  mvFile ${APP_PATH_TEMP}
  start
}

case "$OPERATOR" in
  "start")
    start ;;
  "stop")
    stop ;;
  "status")
    status ;;
  "restart")
    restart ;;
   "clear")
    clear ;;
  *)
    usage ;;
esac
