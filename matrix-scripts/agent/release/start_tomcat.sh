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

#WAI包临时路径
TOMCAT_PATH=$4

#Jetty的路径
WAR_PATH=$5

echo "${APP_NAME} 的操作是:$2"
echo "${APP_NAME} 的JVM_OPS是:$3"
echo "${APP_NAME} 的TOMCAT_PATH是:$4"
echo "${APP_NAME} 的WAR_PATH是:$5"
source /etc/profile

usage() {
    echo "Usage: sh $SCRIPT [app_name] [start|stop|restart|status |clear] JVM_OPS JETTY_PATH WAR_PATH"
    exit 1
}

#判断是否输入了五个参数
#注意①
if [ $# != 5 ]; then
    usage
fi

if [  "${APP_NAME}" == "" ];then
   echo "APP_NAME是空字符串！"
   exit 1
fi

if [  "${OPERATOR}" == "" ];then
   echo "OPERATOR是空字符串！"
   exit 1
fi

if [  "${JVM_OPS}" == "" ];then
   echo "JVM_OPS是空字符串！"
   exit 1
fi

if [  "${TOMCAT_PATH}" == "" ];then
   echo "TOMCAT_PATH是空字符串！"
   exit 1
fi

if [  "${WAR_PATH}" == "" ];then
   echo "WAR_PATH是空字符串！"
   exit 1
fi

findPID(){
   WAR=`echo ${APP_NAME:0-4:4}`
   PACKAGE_NAME=${APP_NAME}
   if [ "${WAR}" == ".war" ] ;then
      PACKAGE_NAME=${PACKAGE_NAME%.war}
   fi
	 echo `ps ax --width=1000 | grep java | grep ${PACKAGE_NAME} | awk '{print $1}'`
}

clear(){

   if [ -d "${WAR_PATH}/${APP_NAME}" ]; then
      echo "rm -rf ${WAR_PATH}/${APP_NAME}"
      rm -rf "${WAR_PATH}/${APP_NAME}"
      echo "${WAR_PATH}/${APP_NAME} 已经清理！"
   fi
    WAR=`echo ${APP_NAME:0-4:4}`
    if [ "${WAR}" == ".war" ] ;then
       if [ -f "${WAR_PATH}/${APP_NAME}" ]; then
          echo "rm -f ${WAR_PATH}/${APP_NAME}"
          rm -f "${WAR_PATH}/${APP_NAME}"
          APP_DIR_NAME=`echo ${APP_NAME%.war}`
          echo "rm -rf ${WAR_PATH}/${APP_DIR_NAME}"
          rm -rf "${WAR_PATH}/${APP_DIR_NAME}"
          echo "${WAR_PATH}/${APP_NAME} 已经清理！"
       fi
    else
        echo "${APP_NAME} 不是一个war文件"
    fi
}

start(){
    echo "cd ${TOMCAT_PATH}"
    cd ${TOMCAT_PATH}
    echo "./bin/startup.sh &"
    nohup ./bin/startup.sh &
    echo "这是一个Tomcat项目：${APP_NAME} 已经完成启动"
}

stop(){
 echo -n "Shutting down $APP_NAME.."
 PID=$(findPID)
 if [ -n "$PID" ]; then
     echo "cd ${TOMCAT_PATH}"
     cd ${TOMCAT_PATH}
     echo "开始stop了!${PID}"
     nohup ./bin/shutdown.sh
     sleep 10
     PID=$(findPID)
     if [ "${PID}" != "" ];then
       kill -9 ${PID}
       echo 'kill -9 ${PID}'
     fi
    echo "stop 完了!"
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
   if [ -f "/data/app/temp/${APP_NAME}" ]; then
	    echo "开始mv了!"
	    echo "mv /data/app/temp/${APP_NAME} ${WAR_PATH}"
	    mv /data/app/temp/${APP_NAME} ${WAR_PATH}
		echo "mv 完了!"
	else
		echo "/data/app/temp/${APP_NAME} is not exist"
	fi
}
restart(){
  stop
  clear
  mvFile
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
