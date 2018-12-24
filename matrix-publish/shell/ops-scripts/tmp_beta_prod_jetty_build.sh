#!/bin/bash
source /etc/profile
#测试环境jetty发布脚本

STRING=$1
echo  "tmp_beta_prod_jetty_build.sh .................................."
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


function mvn_build()
{

   if ( 0"${mvn_ops}" = "0" );then
      exec_cmd "mvn clean install -U  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM "
   else
      exec_cmd "mvn clean install -U  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM ${mvn_ops}"
   fi
}

####################################
# build proc
####################################
cd $WORKSPACE"/"
if [ "$build_type" != "release"  -o ! -e $WORKSPACE"/"$record ];then
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
     mvn_build
                 cp -r $WORKSPACE"/"$deploy_path$jar_name $WORKSPACE"/"$record"/"$deploy_path$jar_name

  fi
fi

#
#
if [ "$build_type" != "release" ];then
    source_path="$WORKSPACE$deploy_path$jar_name"
else
    source_path="$WORKSPACE$record"/"$deploy_path$jar_name"
fi
#
##############################
##发布开始
##############################
#
#
#
IFS=","
arr=($SERVER_LIST)
function push_jar_server()
{
for ip in ${arr[@]};
do
  msg "开始停服!"
  #/usr/bin/ssh -t www@${ip} "cd /data/app &&/data/app/ops-client-script/ops_boot.sh ${jar_name} stop ''"
  curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app/","action":"stop"}' &>/dev/null
  #/usr/bin/ssh -t www@${ip} "cd /data/app &&/data/app/ops-client-script/ops_boot.sh ${jar_name} clear ''"
  curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app/","action":"clear"}' &>/dev/null
  msg "克塞号即将出发!"
  #exec_cmd  "scp -r ${source_path} www@${SERVER_LIST}:/data/app/jettys/${jar_name}/webapps"
  /usr/bin/rsync -avz -e ssh ${source_path} "www@${ip}:/data/app/jettys/${jar_name}/webapps"
  msg "/usr/bin/rsync -avz -e ssh ${source_path} 'www@${ip}:/data/app/jettys/${jar_name}/webapps'"
  /usr/bin/ssh -t www@${ip} "sudo chown www:www /data/app/jettys/${jar_name}/webapps"
  /usr/bin/ssh -t www@${ip} "sudo chmod +x /data/app/jettys/${jar_name}/webapps"
  sleep 5s
  msg "克塞号已经到达!准备撸一发!"
  #/usr/bin/ssh -t www@${ip} "cd /data/app && /data/app/ops-client-script/ops_boot.sh ${jar_name} start ''"
  curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app/","action":"start"}' &>/dev/null
  msg "侏罗纪我来了!"
 done
}
push_jar_server
DEPLOY_STATUS=3
TAG=$btag_name
wait
callBack
if [ "$build_type" != "release" ];then
    rm -rf $WORKSPACE
fi

success "发布成功！"