#!/bin/bash
source /etc/profile

SERVER_LIST=$1
jar_name=$2
deploy_path=$3
WORKSPACE=$4
BUILD_USER_ID=$5
build_branch=$6
build_type=$7
mvn_ops=$8
PROJECT_POM=$9
record=$10
batch=$11
APP_TYPE=$12

msg() {
    printf '%b\n' "$1" >&2
}
}
success() {
    msg "\33[32m[✔] ${1}${2} \33[0m "
}
warn(){
    msg "\33[33m[✔] ${1}${2} \33[0m "
}
error() {
    msg "\33[31m[✘] ${1}${2} \33[0m"
}
error_exit() {
    msg "\33[31m[✘] ${1}${2} \33[0m"
    callBack "4" &>/dev/null
    exit 1
}
callBack(){
    curl -X POST http://max.missfresh.net/api/matrix/publish/betaCallBack -H 'Content-Type: application/json' -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECORD'","batchId":"'$BATCH_RECORD'"}'
}
exec_cmd()
{
    echo "[执行命令] $1"
    $1
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?"
    fi
}
function git_master_merge()
{
    exec_cmd "git checkout master"
    exec_cmd "git merge "$build_branch
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
        callBack "2" &>/dev/null
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
        exec_cmd "mvn clean install -U  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM "
    else
        exec_cmd "mvn clean install -U  -Dmaven.test.skip=true -P ${build_type} -f $PROJECT_POM ${mvn_ops}"
    fi
}

if [["$build_type" != "release*" ]] || [ ! -e $WORKSPACE$record ];then
    BUILD_NUMBER=${BUILD_USER_ID}_${build_branch}
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
            cp -r $WORKSPACE$deploy_path$jar_name $WORKSPACE$record$deploy_path$jar_name
        fi
    fi
fi

#
#
if [[ "$build_type" != "release*" ]];then
    source_path="$WORKSPACE$deploy_path$jar_name"
else
    source_path="$WORKSPACE$record$deploy_path$jar_name"
fi
#
##############################
##发布开始
##############################
#
#
#

function push_jar_server()
{
for ip in $SERVER_LIST;
do
    msg "开始停服!"
    #/usr/bin/ssh -t -t  www@${ip} "cd /data/app &&/data/app/ops-client-script/ops_boot.sh ${jar_name} stop ''"
    #/usr/bin/ssh -t www@${ip} "cd /data/app &&/data/app/ops-client-script/ops_boot.sh ${jar_name} clear ''"
    curl -X POST http://10.3.39.930000/api/agent/startapp -H 'Content-Type: application/json' -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app","action":"stop"}'
    curl -X POST http://10.3.39.930000/api/agent/startapp -H 'Content-Type: application/json' -d '{"buildType":"'$build_type'","packageType":"'$APP_TYPE'","packageName":"'$jar_name'","packagePath":"/data/app","action":"stop"}'
    msg "来鞭挞我吧！"
    /usr/bin/rsync -avz -e ssh ${source_path} "www@${ip}:/data/app/${jar_name}"
    msg "/usr/bin/rsync -avz -e ssh ${source_path} 'www@${ip}:/data/app/${jar_name}'"
    sleep 5s
    msg "来鞭挞我吧!准备撸一发!"
    #/usr/bin/ssh -t www@${ip} "cd /data/app && /data/app/ops-client-script/ops_boot.sh ${jar_name} start ''"
    done
}

push_jar_server

if [[ "$build_type" != "release*" ]];then
    rm -rf $WORKSPACE
fi