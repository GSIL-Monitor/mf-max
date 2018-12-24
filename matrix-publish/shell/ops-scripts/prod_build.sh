#!/bin/bash
source /etc/profile
#线上环境jar发布脚本

STRING=$1
echo  "prod_build.sh .................................."
echo "qwer1234567890================="
echo "STRING:"$STRING
echo "qwer1234567890================="
WORKSPACE=$2
echo "WORKSPACE:"$WORKSPACE
echo "qwer1234567890================="

getValue(){
        echo $1 | awk -F "``$2\":\""  '{print $2}' |awk -F "\"" '{print $1}'
}
getInteger(){
        echo $1 | awk -F "``$2\":"  '{print $2}' |awk -F "," '{print $1}'
}

echo "qwer1234567890================="
echo "STRING:"$STRING
echo "qwer1234567890================="
GIT_ADDRESS=`getValue $STRING "gitAddress"`
echo "GIT_ADDRESS:"$GIT_ADDRESS
build_branch=`getValue $STRING "buildBranch"`
echo "GIT_BRANCH:"$build_branch
BUILD_USER_ID=`getValue $STRING "user"`
echo "USER_NAME:"$BUILD_USER_ID
DEPLOY_SCRIPT=`getValue $STRING "script"`
echo "DEPLOY_SCRIPT:"$DEPLOY_SCRIPT
APP_CODE=`getValue $STRING "appCode"`
echo "APP_CODE:"$APP_CODE
jar_name=`getValue $STRING "jarName"`
echo "JAR_NAME:"$jar_name
build_type=`getValue $STRING "buildType"`
echo "BUILD_TYPE:"$build_type
SERVER_LIST=`getValue $STRING "serverList"`
echo "SERVER_LIST:"$SERVER_LIST
mvn_ops=`getValue $STRING "mvnOps"`
echo "MVN_OPS:"$mvn_ops
GROUP=`getValue $STRING "appGroup"`
echo "GROUP:"$GROUP
deploy_path=`getValue $STRING "deployPath"`
echo "DEPLOY_PATH:"$deploy_path
health_check=`getValue $STRING "healthCheck"`
echo "HEALTHCHECK:"$health_check
PROJECT_POM=`getValue $STRING "projectPom"`
echo "PROJECT_POM:"$PROJECT_POM
PUBLISH_RECORD=`getInteger $STRING "record"`
echo "PUBLISH_RECORD:"$PUBLISH_RECORD
APP_TYPE=`getValue $STRING "appType"`
echo "APP_TYPE:"$APP_TYPE
BATCH_RECORD=`getInteger $STRING "batch"`
echo "BATCH_RECORD:"$BATCH_RECORD
WAIT_TIME=`getInteger $STRING "waitTime"`
echo "WAIT_TIME:"$WAIT_TIME
DEPLOY_STATUS=
TAG=


msg() {
        printf '%s %b\n' "`date +%H:%M:%S`" "$1" >&2
}
success() {
        msg "<span style=\"color:green\"><b> ${1}${2}</b> </span> "
}
warn(){
        msg "<span style=\"color:yellow\"><b> ${1}${2}</b> </span> "
}
error() {
        msg "<span style=\"color:red\"><b> ${1}${2}</b> </span>"
}
error_exit() {
        msg "<span style=\"color:red\"><b> ${1}${2}</b> </span>"
        if [ "$build_type" != "release" ];then
                DEPLOY_STATUS=4
                betaCallBack &>/dev/null
         else
                DEPLOY_STATUS=9
                releaseCallBack
        fi
        exit 1
}
betaCallBack(){
        curl -X POST http://max.missfresh.net/api/publish/publish/betaCallBack -H 'Content-Type: application/json'  -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECOR'","batchId":"'$BATCH_RECORD'"}'
}
releaseCallBack(){
        curl -X POST http://max.missfresh.net/api/publish/publish/releaseCallBack -H 'Content-Type: application/json'  -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECORD'","batchId":"'$BATCH_RECORD'"}'
}
batchCallBack(){
        curl -X POST http://max.missfresh.net/api/publish/publish/batchPublish -H 'Content-Type: application/json'  -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECORD'","batchId":"'$BATCH_RECORD'"}'
}
exec_cmd(){
        printf '%s %b\n' "`date +%H:%M:%S`" "[执行命令]$1" >&2
        $1
        if [ "$?" != "0" ]; then
                error_exit "命令执行失败: 错误码为 $?"
        fi
}
function git_master_merge(){
        exec_cmd "git checkout master"
        exec_cmd "git merge "$build_branch
}
function prod_branch_check(){
        tag_type=${build_branch:0:2}
        if [ $tag_type != "b-" -a $tag_type != "r-" -a "$build_branch" != "master" ]; then
                error_exit "无效的分支【$build_branch】，生产部署需要是btag或者rtag"
        fi
}
function beta_prod_master_merged_check(){
        echo "检查主干上的提交是否已经合并至分支【$build_branch】"
        if [ "$build_branch" == "master" ]; then
                echo "【$build_branch】无需merge检查"
                return 0
        fi
	    tag_type=${build_branch:0:2}
	    if [ $tag_type != "b-" -a $tag_type != "r-" ]; then
		        diff_target="^origin/$build_branch"
	    else
		        diff_target="^tags/$build_branch"
	    fi
 	    diff_log=`git log origin/master $diff_target --oneline`
	    if [ -n "$diff_log" ]; then
                msg "----------------------------------------"
		        msg "$diff_log"
		        msg "----------------------------------------"
                DEPLOY_STATUS=12
                error_exit "请将主干上的更改合并至分支【$build_branch】再执行发布"
	    else
		        success "已经合并"
	    fi
}
function beta_master_merge_to_branch(){
        tag_type=${build_branch:0:2}
        if [ $tag_type == "b-" ]; then
                msg "合并 主干 代码到 分支 上.."
	            exec_cmd "git checkout ${build_branch}"
	            exec_cmd "git pull"
	            #exec_cmd "git merge --no-ff tags/master"
	            #exec_cmd "git push origin HEAD:${build_branch}"
	            success "合并主干代码到分支上成功！！"
        fi
}
#TOTO
function prod_branch_merge_to_master(){
        tag_type=${build_branch:0:2}
        if [ "$build_branch" == "master" ]; then
                echo "【$build_branch】无需代码合并！！"
                return 0
        fi
        if [ $tag_type == "b-" ]; then
                msg "合并分支代码到主干上.."
	            exec_cmd "git checkout master"
	            exec_cmd "git pull"
	            #exec_cmd "git merge --no-ff tags/${build_branch}"
	            #exec_cmd "git push origin HEAD:master"
	            success "合并分支代码到主干上成功！！"
        fi
}
function prod_rollback_check(){
        tag_type=${build_branch:0:2}
        if [ "$tag_type" == "b-" ]; then
                return 0
        fi
        if [ "$tag_type" == "master" ]; then
                return 0
        fi
        diff_log=`git log origin/master ^tags/$build_branch --oneline`
        if [ "$diff_log" != "" ]; then
  	            return 1
        fi
        return 0
}
#TODO
function beta_git_push_btag(){
        if [ "$build_branch" == "master" ]; then
                echo "【$build_branch】无需生成btag"
                return 0
        fi
        btag_name=$(date +"b-%Y%m%d-%H%M%S-$BUILD_USER_ID")
        tag_type=${build_branch:0:2}
        if [ $tag_type != "b-" -a $tag_type != "r-" -a $tag_type != "master" ]; then
                exec_cmd "git tag $btag_name"
                exec_cmd "git push origin $btag_name"
                msg "----------------------------------------"
                success "生成btag: $btag_name"
                msg "----------------------------------------"
                DEPLOY_STATUS=11
                TAG=$btag_name
                betaCallBack
        fi
}
# rtag
rtag_name=$(date +"r-%Y%m%d-%H%M%S-$BUILD_USER_ID")
#
#TODO
function prod_git_push_rtag(){
        tag_type=${build_branch:0:2}
        if [ "$build_branch" == "master" ]; then
                echo "【$build_branch】无需处理"
                return 0
        fi
        if [ $tag_type == "b-" -a $tag_type != "master" ]; then
                exec_cmd "git tag $rtag_name"
                #exec_cmd "git push origin $rtag_name"
                msg "----------------------------------------"
                success "生成rtag: $rtag_name"
                msg "----------------------------------------"
                DEPLOY_STATUS=11
                TAG=$rtag_name
                releaseCallBack
        else
	            msg "无需生成新的rtag"
        fi
}
function count_code(){
        count=$(git log --oneline | wc -l)
        msg "----------------------------------------"
        success "后端代码行数为: $count"
        msg "----------------------------------------"
}
#TODO deploy
function mvn_build(){
        if test -z  ${group} ;then
            group=""
        fi
        if [ "$build_type" == "release" ];then
                if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean install  -Dmaven.test.skip=true -P ${GROUP} -f $PROJECT_POM "
                else
                        exec_cmd "mvn clean install  -Dmaven.test.skip=true -P ${GROUP} -f $PROJECT_POM ${mvn_ops}"
                fi
        else
                if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean install  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM "
                else
                        exec_cmd "mvn clean install  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM ${mvn_ops}"
                fi
        fi
}
# jar包路径
#source_path="$WORKSPACE$deploy_path$jar_name"
#

if [ "$build_type" != "release" ];then
    source_path="$WORKSPACE$deploy_path$jar_name"
else
    echo $WORKSPACE
	echo "-----------------------------"
    source_path="$WORKSPACE$deploy_path$jar_name"
	echo $source_path
fi

function backup()
{
    backup_file_path=/data/backup/${jar_name}
    if [ ! -d "$backup_file_path" ]; then
       exec_cmd "mkdir $backup_file_path"
    fi
    exec_cmd "cp ${source_path} ${backup_file_path}/${rtag_name}.jar"
}

function rollback(){
        backup_file_path=/data/backup/${jar_name}
        if [ ! -f "${backup_file_path}/${build_branch}.jar" ];then
                mvn_build
                return 0
        fi
        echo "source_path='${backup_file_path}/${build_branch}.jar'"
        source_path="${backup_file_path}/${build_branch}.jar"
}
# master分支代码回滚
function rollback_master_branch(){
        backup_branch_name=$(date +"k_%Y%m%d_${build_branch}")
        exec_cmd "git fetch"
        exec_cmd "git checkout -b ${backup_branch_name} ${build_branch}"
        echo "开始回滚master代码为Tag：${build_branch}"
        exec_cmd "git push origin ${backup_branch_name}:master -f"
        #exec_cmd "git reset --hard ${build_branch}"
}
function curl_live(){
        count=1
        for i in $(seq 1 20):
        do
                msg "探活的地址为:$1"
                #curl 请求
                http_status_code=$(curl -sL -w "%{http_code}" $1 -o /dev/null)
                #对响应码进行判断
                if [ "$http_status_code" == "200" ];then
                        echo "请求成功，响应码是$http_status_code"
                        count=0
                        break
                else
                        sleep 5s
                        echo "服务没有在规定的时间内启动!"
                fi
        done
        if [ "$count" -eq 1 ];then
                error "$1服务没有在规定的时间内启动!"
                return 1
        else
		return 0
	fi
}

cd ${WORKSPACE}"/"
if [ "$build_type" == "" ];then
        error_exit "你需要先设置 build_type!!"
fi
if [ "$build_type" == 'dev' ]; then
        success "dev发布，跳过git检查！"
        beta_prod_master_merged_check
        mvn_build
elif [ "$build_type" == 'test' ]; then
        beta_prod_master_merged_check
        beta_git_push_btag
        mvn_build
elif [ "$build_type" == 'beta' ]; then
        beta_prod_master_merged_check
        beta_git_push_btag
        mvn_build
elif [ "$build_type" == 'release' ]; then
	echo ${WORKSPACE}${PUBLISH_RECORD}${GROUP}
	echo "000000000000111111111111---------------"
        if [ ! -e ${WORKSPACE}${deploy_path} ];then
		prod_branch_check
        	prod_rollback_check
        	if [ "$?" == "1" ]; then
                	warn "由于发布不是最新的rtag，系统判断为一次回滚操作，跳过主干合并检测和生成新的rag"
                	# 如果有问题，则换成
                	rollback
                	success "git发布完成!"
        	else
                        success "执行新分支发布.."
                        beta_prod_master_merged_check
                        prod_branch_merge_to_master
                        prod_git_push_rtag
                        success "git发布完成!"
			mvn_build
                        	if [ "$build_branch" != "" ]; then
                                	echo "【$build_branch】开始备份！！"
                                	backup
                        	fi
                fi
        fi
fi
##############################
##发布开始
##############################
IFS=","
arr=($SERVER_LIST)
#TODO
function push_jar_server(){
        for ip in ${arr[@]};
        do
		echo "test==================="
		    curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app/","action":"stop"}' &>/dev/null
   		echo "test!!!!!!!!!!!!!!!!!!!"
		 curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app/","action":"clear"}' &>/dev/null
                /usr/bin/rsync -avz -e ssh ${source_path} "www@${ip}:/data/app/${jar_name}"
                msg "/usr/bin/rsync -avz -e ssh ${source_path} 'www@${ip}:/data/app/${jar_name}'"
                #/usr/bin/ssh  www@${ip} "sh /data/start.sh"
		echo "test~~~~~~~~~~~~~~~~~~~~~"
                curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app/","action":"start"}' &>/dev/null
                echo $ip
echo $build_type $APP_TYPE $jar_name
		msg "来鞭挞我吧!准备撸一发!"
        done
}
##############################
##发布探活
##############################
function check_app_status(){
        result=0
        for ip in ${arr[@]};
        do
                url="http://${ip}:${health_check}"
                response=`curl_live $url`
                if [ "$response" == "1" ]; then
                        msg "${ip}启动失败"
                        result=1
                else
                        msg "${ip}启动成功"
                fi
        done
        if [ "$result" == "1" ];then
                error_exit "发布失败"
        fi
}
push_jar_server
wait
#check_app_status
if [ "$build_type" != "release" ];then
        DEPLOY_STATUS=3
        betaCallBack
        rm -rf $WORKSPACE
        exit 1
else
        DEPLOY_STATUS=8
        releaseCallBack &>/dev/null
	wait
        sleep ${WAIT_TIME}s
        batchCallBack &>/dev/null
	exit 1
fi
exit 0