#!/bin/bash
#
# node_exporter		Start up the Node Exporter daemon
#
# chkconfig: 345 70 30
# description: node_exporter is a Prometheus system metrics exporter
# processname: node_exporter

# source function library
. /etc/rc.d/init.d/functions


RETVAL=0
prog="node_exporter"
lockfile=/var/lock/subsys/$prog

# Some functions to make the below more readable
USER=www
PID_FILE=/var/run/exporter/$prog.pid
NODE_EXPORTER=/usr/local/services/exporter/bin/$prog

runlevel=$(set -- $(runlevel); eval "echo \$$#" )

clean(){
if [ -d "/data/app" ]; then
    cd /data/app
    if [ -d "/data/app/matrix-scripts" ]; then
        echo "rm -rf matrix-scripts"
        rm -rf matrix-scripts
        echo "matrix-scripts 已经清理！"
    fi
    if [ -f "/data/app/matrix-scripts.zip" ]; then
        echo "rm -f matrix-scripts.zip"
        rm -f matrix-scripts.zip
        echo "matrix-scripts.zip 已经清理！"
    fi
    if [ -f "/data/app/matrix-scripts.tar.gz" ]; then
        echo "rm -f matrix-scripts.tar.gz"
        rm -f matrix-scripts.tar.gz
        echo "matrix-scripts.tar.gz 已经清理！"
    fi
    if [ -d "/data/app/matrix-agent" ]; then
        # echo  "cd /data/app/matrix-agent && rm -rf *"
        # cd /data/app/matrix-agent && rm -rf *
        echo "rm -rf matrix-agent"
        rm -rf matrix-agent
        echo "matrix-agent 已经清理！"
    fi
    if [ -f "/data/app/matrix-agent.zip" ]; then
        echo "rm -f matrix-agent.zip"
        rm -f matrix-agent.zip
        echo "matrix-agent.zip 已经清理！"
    fi
    if [ -f "/data/app/matrix-agent.tar.gz" ]; then
        echo "rm -f matrix-agent.tar.gz"
        rm -f matrix-agent.tar.gz
        echo "matrix-agent.tar.gz 已经清理！"
    fi
fi
}

is_user(){
check_user=`whoami`
if [ "$check_user" == "www"  ];then
    echo "当前用户是www"
else
    echo "当前用户不是www....退出安装"
    exit
fi
}
findPID(){
	 echo `ps ax --width=1000 | grep "/data/app/matrix-agent/matrix-agent" |grep -v "grep" | awk '{print $1}'`
}

do_start_prepare()
{
  if [ ! -f /usr/local/services/exporter ];then
      mkdir -pv /usr/local/services/exporter
  fi
  if [ ! -f /usr/local/services/exporter/bin ];then
      mkdir -pv /usr/local/services/exporter/bin
  fi
  if [ ! -f /var/run/exporter ];then
      mkdir -pv /var/run/exporter
  fi
  chown -R www.www /usr/local/services/exporter
  chown -R www.www /var/run/exporter
}


is_user(){
check_user=`whoami`
if [ "$check_user" == "www"  ];then
    echo "当前用户是www"
else
    echo "当前用户不是www....退出安装"
    exit
fi

}
findPID(){
	 echo `ps ax --width=1000 | grep "/data/app/matrix-agent/matrix-agent" |grep -v "grep" | awk '{print $1}'`
}

start()
{
    is_user
    /data/app/matrix-agent/matrix-agent -v release -c /data/app/matrix-agent/config.yaml > /dev/null 2>&1 &
    PID=$(findPID)
    echo "启动完成PID:$PID"
    if [ -n "$PID" ]; then
        echo "matrix-agent 启动成功！"
    else
	    echo "matrix-agent 启动失败！"
    fi
}

stop()
{
    is_user
	echo -n "Shutting down matrix-agent.."
	PID=$(findPID)
	if [ -n "$PID" ]; then
	    echo "开始kill了!"
		kill ${PID}
        sleep 3
        PID=$(findPID)
        if [ "${PID}" != "" ];then
            kill -9 ${PID}
            echo 'kill -9 ${PID}'
        fi
		echo "kill 完了!"
	else
		echo "matrix-agent is not running"
	fi
}

restart() {
	stop
	clean
	cd /data/app
	ehco "进入/data/app目录"
    wget -c http://matrix-storage.missfresh.net/matrix-scripts.tar.gz
    tar -xzvf matrix-scripts.tar.gz
    echo "script 已经安装完成！"
    wget http://matrix-storage.missfresh.net/matrix-agent.tar.gz
    tar -xzvf matrix-agent.tar.gz
    echo "agent 已经安装完成！"
	start
}

#reload()
#{
#	echo -n $"Reloading $prog: "
#	killproc -p $PID_FILE $NODE_EXPORTER -HUP
#	RETVAL=$?
#	echo
#}

force_reload() {
         restart
}

rh_status() {
	status -p $PID_FILE openssh-daemon
}

rh_status_q() {
	rh_status >/dev/null 2>&1
}

case "$1" in
	start)
		#rh_status_q && exit 0
		start
		;;
	stop)
		if ! rh_status_q; then
			rm -f $lockfile
			exit 0
		fi
		stop
		;;
	restart)
		restart
		;;
	reload)
		rh_status_q || exit 7
		reload
		;;
	force-reload)
		force_reload
		;;
	condrestart|try-restart)
		rh_status_q || exit 0
		if [ -f $lockfile ] ; then
			do_restart_sanity_check
			if [ $RETVAL -eq 0 ] ; then
				stop
				# avoid race
				sleep 3
				start
			else
				RETVAL=6
			fi
		fi
		;;
	status)
                sum=`ps uax|grep -w matrix-agent | grep -v grep | wc -l`
                if [ $sum -eq 1 ]
                   then echo "matrix-agent is start"
                else
                     echo "matrix-agent is stop"
                fi

		;;
	*)
		echo $"Usage: $0 {start|stop|restart|reload|force-reload|condrestart|try-restart|status}"
		RETVAL=2
esac
exit $RETVAL
