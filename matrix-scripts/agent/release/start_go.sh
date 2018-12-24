#!/bin/bash
source /etc/profile

#本脚本基本无需改动，注意要点均已用中文说明
#建议使用 . xx.sh 命令执行脚本。如果使用sh xx.sh执行，注意is_exist方法里的注释
#获取脚本名称
SCRIPT=$0

#获取进程名称，必须为完整程序名，否则可能会误操作其他进程

build_type=$1
#服务名
APP_NAME=$2

#获取操作符
OPERATOR=$3
#服务临时路径
APP_PATH_TEMP=$4
echo "${APP_NAME} 的操作是:$3"



usage() {
    echo "Usage: sh $SCRIPT [app_name] [start|stop|restart|status|clear] "
    exit 1
}

#判断是否输入了三个参数
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

  pid= `ps aux | grep "${APP_NAME}"|grep -v "grep"|awk '{ print $2}'`
  if [ "${pid}" == "" ]; then
      return 1
  else
    return 0
  fi
}

findPID(){
#	 echo `ps ax --width=1000 | grep /data/app/${APP_NAME}/${APP_NAME} | awk '{print $1}'`
     #TODO 兼容性
	 echo `ps aux | grep "${APP_NAME} -v"|grep -v grep|awk '{ print $2}'`
}


start(){
    echo "${APP_NAME} 启动！"
    echo "nohup /data/app/${APP_NAME}/${APP_NAME} -v ${build_type} -c /data/app/${APP_NAME}/config.yaml > /dev/null 2>&1 &"
    cd /data/app/${APP_NAME}
    nohup /data/app/${APP_NAME}/${APP_NAME} -v ${build_type} -c /data/app/${APP_NAME}/config.yaml > /dev/null 2>&1 &

    echo "${APP_NAME} 已经完成启动"
}


stop(){
 echo -n "Shutting down $APP_NAME.."
 PID=$(findPID)
 echo "PID:${PID}"
 if [ -n "$PID" ]; then
     echo "开始kill了!${PID}"
     kill ${PID}
#     echo "sleep 20s"
#     sleep 20
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
