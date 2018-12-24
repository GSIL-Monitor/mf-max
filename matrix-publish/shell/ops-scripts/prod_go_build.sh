#!/bin/bash
source /etc/profile

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
APP_NAME=`getValue $STRING "jarName"`
echo "JAR_NAME:"$jar_name
echo "APP_NAME:"$APP_NAME
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

function git_master_merge()
{
  exec_cmd "git checkout master"
  exec_cmd "git merge "$build_branch
  #exec_cmd "git merge --abort"
}


function prod_branch_check()
{
    tag_type=${build_branch:0:2}
    if [ $tag_type != "b-" -a $tag_type != "r-" -a "$build_branch" != "master" ]; then
        error_exit "无效的分支【$build_branch】，生产部署需要是btag或者rtag"
    fi
}


function beta_prod_master_merged_check()
{
	echo "检查主干上的提交是否已经合并至分支【$build_branch】"
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
		beta_master_merge_to_branch

#		error_exit "请将主干上的更改合并至分支【$build_branch】再执行发布"
	else
		success "已经合并"
	fi
}


function beta_master_merge_to_branch(){
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" ]; then
        msg "合并 主干 代码到 分支 上.."
	exec_cmd "git checkout origin/${build_branch}"
	exec_cmd "git pull"
	exec_cmd "git merge --no-ff tags/master"
	exec_cmd "git push origin HEAD:${build_branch}"
	success "合并主干代码到分支上成功！！"
    fi
}

function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" ]; then
        msg "合并分支代码到主干上.."
	exec_cmd "git checkout master"
	exec_cmd "git pull"
	exec_cmd "git merge --no-ff tags/${build_branch}"
	exec_cmd "git push"
	success "合并分支代码到主干上成功！！"
    fi
}


function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" ]; then
        msg "合并分支代码到主干上.."
	exec_cmd "git checkout master"
	exec_cmd "git pull"
	exec_cmd "git merge --no-ff tags/${build_branch}"
	exec_cmd "git push"
	success "合并分支代码到主干上成功！！"
    fi
}



function prod_rollback_check(){
    tag_type=${build_branch:0:2}

    if [ "$tag_type" == "b-" ]; then
        return 0
    fi

    diff_log=`git log origin/master ^tags/$build_branch --oneline`
    if [ "$diff_log" != "" ]; then
  	return 1
    fi
    return 0
}


function beta_git_push_btag()
{
    btag_name=$(date +"b-%Y%m%d-%H%M%S-$BUILD_USER_ID")
    tag_type=${build_branch:0:2}
    if [ $tag_type != "b-" -a $tag_type != "r-" -a $tag_type != "master" ]; then
        exec_cmd "git tag $btag_name"
        exec_cmd "git push origin $btag_name"
        msg "----------------------------------------"
        success "生成btag: $btag_name"
        msg "----------------------------------------"
    fi
}


function prod_git_push_rtag()
{
    rtag_name=$(date +"r-%Y%m%d-%H%M%S-$BUILD_USER_ID")
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" -a $tag_type != "master" ]; then
       exec_cmd "git tag $rtag_name"
       exec_cmd "git push origin $rtag_name"
       msg "----------------------------------------"
       success "生成rtag: $rtag_name"
       msg "----------------------------------------"
    else
	msg "无需生成新的rtag"
    fi
}


function count_code()
{
  count=$(git log --oneline | wc -l)
   msg "----------------------------------------"
  success "后端代码行数为: $count"
  msg "----------------------------------------"


}

function mvn_build()
{
   if ( 0"${mvn_ops}" = "0" );then
      exec_cmd "mvn clean install package  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM "
   else
      exec_cmd "mvn clean install package  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM ${mvn_ops}"
   fi
}
source_path="$WORKSPACE/"

function curl_live()
{
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
       error_exit "服务没有在规定的时间内启动!"
   fi

}

function go_build(){
    export GOPATH=~/go
    export PATH=$GOPATH/bin:$PATH
    go get -u github.com/Masterminds/glide
    rm -rf ~/go/src/${APP_NAME}/*
    cp -r ${source_path}* ~/go/src/${APP_NAME}
    cd ~/go/src/${APP_NAME}
    glide install
    echo '开始构建'
    go build -v
    echo '完成构建'

}
if [ "$build_type" == "" ];then
	error_exit "你需要先设置 build_type!!"
fi

if [ "$build_type" == 'dev' ]; then
     success "dev发布，跳过git检查！"
     beta_prod_master_merged_check


elif [ "$build_type" == 'test' ]; then
    beta_prod_master_merged_check
    beta_git_push_btag


elif [ "$build_type" == 'beta' ]; then
    beta_prod_master_merged_check
    beta_git_push_btag

elif [ "$build_type" == 'prod' ]; then
    prod_branch_check
    prod_rollback_check
    if [ "$?" == "1" ]; then
         warn "由于发布不是最新的rtag，系统判断为一次回滚操作，跳过主干合并检测和生成新的rag"
    else
         success "执行新分支发布.."
         beta_prod_master_merged_check
         prod_branch_merge_to_master
         prod_git_push_rtag
         success "git发布完成!"

    fi
fi

OLD_IFS="SIFS"
IFS=","
arr=($SERVER_LIST)
function push_go_server()
{
for ip in ${arr[@]};
do
    #/usr/bin/ssh  www@${ip} "cd /data/app/${APP_NAME}/ && /data/app/ops-client-script/temp_start_ess_go.sh ${build_type} ${APP_NAME} stop"
    curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$APP_NAME'","packagePath":"/data/app/'${APP_NAME}'","action":"stop"}'
     msg "/usr/bin/ssh -t www@${ip} 'cd /data/app/${APP_NAME}/ && /data/app/ops-client-script/temp_start_ess_go.sh ${build_type} ${APP_NAME} stop'"
    /usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/${APP_NAME} www@${ip}:/data/app/${APP_NAME}/${APP_NAME}
    /usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/config.yaml www@${ip}:/data/app/${APP_NAME}/config.yaml
    curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$APP_NAME'","packagePath":"/data/app/'${APP_NAME}'","action":"start"}'
    /#usr/bin/ssh  www@${ip} "cd /data/app/${APP_NAME}/ && /data/app/ops-client-script/temp_start_ess_go.sh ${build_type} ${APP_NAME} start"
    url="http://${ip}:${health_check}"
    curl_live $url
    msg "启动成功!sleep 5s"
    sleep 5s

done
}
go_build
push_go_server