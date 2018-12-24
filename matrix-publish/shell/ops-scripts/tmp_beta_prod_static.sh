#!/bin/bash
source /etc/profile
#测试环境前端发布脚本

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
build_path=`getValue $STRING "deployPath"`
JOB_NAME=


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
		success "$diff_log"
		msg "----------------------------------------"
		error_exit "请将主干上的更改合并至分支【$build_branch】再执行发布"
	else
		success "已经合并"
	fi
}


function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" ]; then
        msg "合并分支代码到主干上.."
	exec_cmd "git checkout origin/master"
	exec_cmd "git pull"
	exec_cmd "git merge --no-ff tags/${build_branch}"
	exec_cmd "git push origin HEAD:master"
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
        success "----------------------------------------"
        success "生成btag: $btag_name"
        success "----------------------------------------"
    fi
}


function prod_git_push_rtag()
{
    rtag_name=$(date +"r-%Y%m%d-%H%M%S-$BUILD_USER_ID")
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" -a $tag_type != "master" ]; then
       exec_cmd "git tag $rtag_name"
       exec_cmd "git push origin $rtag_name"
       success "----------------------------------------"
       success "生成rtag: $rtag_name"
       success "----------------------------------------"
    else
	    success "无需生成新的rtag"
    fi
}

#build_path=app_code
source_path="$WORKSPACE/dist/"
IFS=","
arr=($SERVER_LIST)
function push_static_server()
{
for ip in ${arr[@]};
do
   msg "----------------------------------------"
   /usr/bin/ssh www@${ip} "cd /data/app/static-html"
   /usr/bin/ssh www@${ip} "rm -rf /data/app/static-html/${build_path}/"
   /usr/bin/ssh www@${ip} "mkdir /data/app/static-html/${build_path}/"
   /usr/bin/rsync -avz -e ssh ${source_path} "www@${ip}:/data/app/static-html/$build_path"
   msg "/usr/bin/rsync -avz -e ssh ${source_path} 'www@${ip}:/data/app/static-html/$build_path'"
   /usr/bin/ssh www@{ip} "sudo chown -R www:www /data/app/static-html/${build_path}/"
   /usr/bin/ssh www@{ip} "sudo chmod -R +x /data/app/static-html/${build_path}/"
   sleep 60s
done
}
#job_path=/home/jenkins/.jenkins/workspace/${JOB_NAME}
#修改  JOBNAME  jobpath=workspace
#job_path=/data/jenkins/workspace/${JOB_NAME}
job_path="$WORKSPACE"
function set_toket(){
    path=$job_path/src/router/index.js
    if [ -f $path ]; then
      sed -i "" "s/^store.commit('user\/setAccessToken'.*/store.commit('user\/setAccessToken', Cookies.get('token'))/" $path
    fi

}
function npm_build(){
   #set_toket
   exec_cmd "rm -rf $job_path/dist/*"
   exec_cmd "npm install"
   exec_cmd "npm run build"
   exec_cmd "chown -R www:www $job_path/dist"
}
if [ "$build_type" == "" ];then
	error_exit "你需要先设置 build_type!!"
fi

if [ "$build_type" == 'dev' ]; then
     success "dev发布，跳过git检查！"
     npm_build
     beta_prod_master_merged_check

elif [ "$build_type" == 'test' ]; then
    beta_prod_master_merged_check
    npm_build
    beta_git_push_btag

elif [ "$build_type" == 'beta' ]; then
    beta_prod_master_merged_check
    npm_build
    beta_git_push_btag

elif [ "$build_type" == 'prod' ]; then
    prod_branch_check
    prod_rollback_check
    npm_build
if [ "$?" == "1" ]; then
     warn "由于发布不是最新的rtag，系统判断为一次回滚操作，跳过主干合并检测和生成新的rag"
  else
     success "执行新分支发布.."
     beta_prod_master_merged_check
     prod_branch_merge_to_master
     npm_build
     prod_git_push_rtag
     success "git发布完成!"
  fi
fi

push_static_server
DEPLOY_STATUS=3
TAG=$btag_name
wait
betaCallBack
if [ "$build_type" != "release" ];then
    rm -rf $WORKSPACE
fi

success "发布成功！"