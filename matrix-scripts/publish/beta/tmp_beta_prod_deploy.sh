#!/bin/bash
source /etc/profile
#测试jar发布
STRING=$1
WORKSPACE=$2
echo "WORKSPACE = ${WORKSPACE}"
function getValue(){
    echo $1 | awk -F "$2\":\""  '{print $2}' | awk -F "\"" '{print $1}'
}
function getInteger(){
    echo $1 | awk -F "$2\":"  '{print $2}' | awk -F "," '{print $1}'
}
function msg(){
    printf '%s %b\n' "`date "+%Y-%m-%d %H:%M:%S"`" "$1" >&2
}
function success(){
    msg "<span style=\"color:green\"><b> ${1}${2}</b> </span> "
}
function warn(){
    msg "<span style=\"color:yellow\"><b> ${1}${2}</b> </span> "
}
function error(){
    msg "<span style=\"color:red\"><b> ${1}${2}</b> </span>"
}
function betaCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/betaCallBack -H 'Content-Type: application/json'  -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECORD}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'"}'
}
function releaseCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/releaseCallBack -H 'Content-Type: application/json'  -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECORD}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'"}'
}
function batchCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/batchPublish -H 'Content-Type: application/json'  -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECORD}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'"}'
}
function ipCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/ipCallBack -H 'Content-Type: application/json'  -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECOR}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'","ip":"'${ip}'"}'
}
function error_exit(){
    msg "<span style=\"color:red\"><b> ${1}${2}</b> </span>"
    if [ "${build_type}" != "release" ];then
        DEPLOY_STATUS=$3
        betaCallBack &>/dev/null
    else
        DEPLOY_STATUS=$4
        releaseCallBack &>/dev/null
    fi
    exit 1
}
function exec_cmd(){
    printf '%s %b\n' "`date "+%Y-%m-%d %H:%M:%S"`" "[执行命令]$1" >&2
    #$1 | sed "s/^/`date +%H:%M:%S` &/g"
    $1 
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' $2 $3
    fi
}
function git_master_merge(){
    exec_cmd "git checkout master" 12 12
    exec_cmd "git merge "${build_branch} 12 12
}
function prod_branch_check(){
    tag_type=${build_branch:0:2}
    if [ ${tag_type} != "b-" -a ${tag_type} != "r-" -a "${build_branch}" != "master" ]; then
        error_exit "无效的分支【${build_branch}】，生产部署需要是btag或者rtag" '' 12 12
    fi
}
function beta_prod_master_merged_check(){
	echo "检查主干上的提交是否已经合并至分支【${build_branch}】"
	tag_type=${build_branch:0:2}
	if [ ${tag_type} != "b-" -a ${tag_type} != "r-" ]; then
		diff_target="^origin/${build_branch}"
	else
		diff_target="^tags/${build_branch}"
	fi
 	diff_log=`git log origin/master ${diff_target} --oneline`
	if [ -n "${diff_log}" ]; then
        msg "----------------------------------------"
		msg "${diff_log}"
		msg "----------------------------------------"
        diff_stat=`git diff origin/master ${diff_target}`
        if [ -n "${diff_stat}" ];then
          error_exit "请将主干上的更改合并至分支【${GIT_BRANCH}】再执行发布" '' 12 12
        else
            success "无代码差异"
        fi
	else
		success "已经合并"
	fi
}
function beta_master_merge_to_branch(){
    tag_type=${build_branch:0:2}
    if [ ${tag_type} == "b-" ]; then
        msg "合并 主干 代码到 分支 上.."
	    exec_cmd "git checkout origin/${build_branch}" 12 12
	    exec_cmd "git pull" 12 12
	    exec_cmd "git merge --no-ff tags/master" 12 12
	    exec_cmd "git push origin HEAD:${build_branch}" 12 12
	    success "合并主干代码到分支上成功！！"
    fi
}
function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ ${tag_type} == "b-" ]; then
        msg "合并分支代码到主干上.."
	    exec_cmd "git checkout origin/master" 12 12
	    exec_cmd "git pull" 12 12
	    exec_cmd "git merge --no-ff tags/${build_branch}" 12 12
	    exec_cmd "git push origin HEAD:master" 12 12
	    success "合并分支代码到主干上成功！！" 12 12
    fi
}
function prod_rollback_check(){
    tag_type=${build_branch:0:2}
    if [ "${tag_type}" == "b-" ]; then
        return 0
    fi
    diff_log=`git log origin/master ^tags/${build_branch} --oneline`
    if [ "${diff_log}" != "" ]; then
  	    return 1
    fi
    return 0
}
function beta_git_push_btag(){
    btag_name=$(date +"b-%Y%m%d-%H%M%S-${BUILD_USER_ID}")
    tag_type=${build_branch:0:2}
    if [ ${tag_type} != "b-" -a ${tag_type} != "r-" -a ${tag_type} != "master" ]; then
        exec_cmd "git tag ${btag_name}" 12 12
        exec_cmd "git push origin ${btag_name}" 12 12
        msg "----------------------------------------"
        success "生成btag: ${btag_name}"
        TAG=${btag_name}
        msg "----------------------------------------"
    fi
    DEPLOY_STATUS=11
    betaCallBack  &>/dev/null
}
function prod_git_push_rtag(){
    rtag_name=$(date +"r-%Y%m%d-%H%M%S-${BUILD_USER_ID}")
    tag_type=${build_branch:0:2}
    if [ ${tag_type} == "b-" -a ${tag_type} != "master" ]; then
        exec_cmd "git tag ${rtag_name}" 12 12
        exec_cmd "git push origin ${rtag_name}" 12 12
        msg "----------------------------------------"
        success "生成rtag: ${rtag_name}"
        TAG=${rtag_name}
        msg "----------------------------------------"
    else
	    msg "无需生成新的rtag"
    fi
    DEPLOY_STATUS=11
    betaCallBack  &>/dev/null
}
function count_code(){
    count=$(git log --oneline | wc -l)
    msg "----------------------------------------"
    success "后端代码行数为: ${count}"
    msg "----------------------------------------"
}
function mvn_build(){
    if test -z ${MAVEN_PATH} ;then
        if test -z  ${mvn_ops} ;then
            exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${build_type} -f ${PROJECT_POM} " 14 14
        else
            exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${build_type} -f ${PROJECT_POM} ${mvn_ops}" 14 14
        fi
    else
        if test -z  ${mvn_ops};then
            exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${build_type} -f ${PROJECT_POM} -s ${MAVEN_PATH}" 14 14
        else
            exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${build_type} -f ${PROJECT_POM} -s ${MAVEN_PATH} ${mvn_ops} " 14 14
        fi
    fi
    DDEPLOY_STATUS=13
    betaCallBack  &>/dev/null
}
build_branch=`getValue "${STRING}" "buildBranch"`
echo "build_branch:"${build_branch}
BUILD_USER_ID=`getValue "${STRING}" "user"`
echo "BUILD_USER_ID:"${BUILD_USER_ID}
jar_name=`getValue "${STRING}" "jarName"`
echo "jar_name:"${jar_name}
build_type=`getValue "${STRING}" "buildType"`
echo "build_type:"${build_type}
mvn_ops=`getValue "${STRING}" "mvnOps"`
echo "mvn_ops:"${mvn_ops}
deploy_path=`getValue "${STRING}" "deployPath"`
echo "deploy_path:"${deploy_path}
health_check=`getValue "${STRING}" "healthCheck"`
echo "health_check:"${health_check}
PROJECT_POM=`getValue "${STRING}" "projectPom"`
echo "PROJECT_POM:"${PROJECT_POM}
PUBLISH_RECORD=`getInteger "${STRING}" "record"`
echo "PUBLISH_RECORD:"${PUBLISH_RECORD}
APP_TYPE=`getValue "${STRING}" "appType"`
echo "APP_TYPE:"${APP_TYPE}
BATCH_RECORD=`getInteger "${STRING}" "batch"`
echo "BATCH_RECORD:"${BATCH_RECORD}
#新添加
PROJECT_TASk_ID=`getInteger "${STRING}" "projectTaskId"`
echo "PROJECT_TASk_ID:"${PROJECT_TASk_ID}
APP_CODE=`getValue "${STRING}" "appCode"`
echo "APP_CODE:"${APP_CODE}
DEPLOY_STATUS=
TAG=
MAVEN_PATH=`getValue "${STRING}" "mavenPath"`
echo "MAVEN_PATH:"${MAVEN_PATH}

cd ${WORKSPACE}"/"
if [ "${build_type}" != "release"  -o ! -e ${WORKSPACE}"/"${PUBLISH_RECORD} ];then
    BUILD_NUMBER=${BUILD_USER_ID}_${build_branch}
    if [ "${build_type}" == "" ];then
	    error_exit "你需要先设置 build_type!!" '' 12 12
    fi
    if [ "${build_type}" == 'presstest' ]; then
        beta_prod_master_merged_check
        beta_git_push_btag
        mvn_build
    elif [ "${build_type}" == 'dev' ]; then
        success "dev发布，跳过git检查！"
        beta_prod_master_merged_check
        mvn_build
    elif [ "${build_type}" == 'test' ]; then
        beta_prod_master_merged_check
        beta_git_push_btag
        mvn_build
    elif [ "${build_type}" == 'beta' ]; then
        beta_prod_master_merged_check
	    beta_git_push_btag
        mvn_build
    elif [ "${build_type}" == 'release' ]; then
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
            cp -r ${WORKSPACE}"/"${deploy_path}${jar_name} ${WORKSPACE}"/"${PUBLISH_RECORD}"/"${deploy_path}${jar_name}
        fi
    fi
fi
success "发布成功"

if [ "${build_type}" != "release" ];then
    DEPLOY_STATUS=3
    betaCallBack &>/dev/null
    rm -rf ${WORKSPACE}
    exit 1
else
    DEPLOY_STATUS=8
    releaseCallBack &>/dev/null
    sleep ${WAIT_TIME}s
    batchCallBack &>/dev/null
    exit 1
fi