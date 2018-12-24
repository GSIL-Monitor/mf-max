#!/bin/bash

CLONE_DIR=/data/matrix/project/
BETA_DIR=/data/matrix/project/beta/
RELEASE_DIR=/data/matrix/project/release/
STRING=$1

function betaCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/betaCallBack -H 'Content-Type: application/json' -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECORD}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'"}'
}
function releaseCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/releaseCallBack -H 'Content-Type: application/json' -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECORD}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'"}'
}
function getValue(){
    echo $1 | awk -F "$2\":\""  '{print $2}' |awk -F "\"" '{print $1}'
}
function getInteger(){
    echo $1 | awk -F "$2\":"  '{print $2}' |awk -F "," '{print $1}'
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
function error_exit(){
    msg "<span style=\"color:red\"><b> ${1}${2}</b> </span>"
    if [ "${BUILD_TYPE}" == "release" ];then
	    DEPLOY_STATUS=12
	    releaseCallBack &>/dev/null
	else
		DEPLOY_STATUS=12
       	betaCallBack &>/dev/null
	fi
	exit 1
}
function exec_cmd(){
    printf '%s %b\n' "`date "+%Y-%m-%d %H:%M:%S"`" "[执行命令]$1" >&2
    $1 &>/dev/null
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?"
    fi
}

function beta_prod_master_merged_check(){
	echo "检查主干上的提交是否已经合并至分支【${GIT_BRANCH}】"
	tag_type=${GIT_BRANCH:0:2}
	if [ ${tag_type} != "b-" -a ${tag_type} != "r-" ]; then
		diff_target="^origin/${GIT_BRANCH}"
	else
		diff_target="^tags/${GIT_BRANCH}"
	fi
 	diff_log=`git log origin/master ${diff_target} --oneline`
	if [ -n "${diff_log}" ]; then
        msg "----------------------------------------"
		msg "${diff_log}"
		msg "----------------------------------------"
        diff_stat=`git diff origin/master ${diff_target}`
        if [ -n "${diff_stat}" ];then
		  error_exit "请将主干上的更改合并至分支【${GIT_BRANCH}】再执行发布" 
        else
            success "无代码差异"
        fi
	else
		success "已经合并"
	fi
}
matrix_storage_domain="matrix-storage.missfresh.net"
GIT_ADDRESS=`getValue "${STRING}" "gitAddress"`
echo "GIT_ADDRESS:"${GIT_ADDRESS}
GIT_BRANCH=`getValue "${STRING}" "buildBranch"`
echo "GIT_BRANCH:"${GIT_BRANCH}
USER_NAME=`getValue "${STRING}" "user"`
echo "USER_NAME:"${USER_NAME}
DEPLOY_SCRIPT=`getValue "${STRING}" "script"`
echo "DEPLOY_SCRIPT:"${DEPLOY_SCRIPT}
APP_CODE=`getValue "${STRING}" "appCode"`
echo "APP_CODE:"${APP_CODE}
BUILD_TYPE=`getValue "${STRING}" "buildType"`
echo "BUILD_TYPE:"${BUILD_TYPE}
GROUP=`getValue "${STRING}" "appGroup"`
echo "GROUP:"${GROUP}
PUBLISH_RECORD=`getInteger "${STRING}" "record"`
echo "PUBLISH_RECORD:"${PUBLISH_RECORD}
BATCH_RECORD=`getInteger "${STRING}" "batch"`
echo "BATCH_RECORD:"${BATCH_RECORD}
PLAN_RECORD=`getInteger "${STRING}" "plan"`
echo "PLAN_RECORD:"${PLAN_RECORD}
DEPLOY_STATUS=
TAG=
WORKSPACE=
#部署的包路径
PACKAGE_PATH=`getValue "${STRING}" "pkgPath"`
echo "PACKAGE_PATH:"${PACKAGE_PATH}
#容器路径tomcat/jetty等
CONTAINER_PATH=`getValue "${STRING}" "containerPath"`
echo "CONTAINER_PATH:"${CONTAINER_PATH}

appType=`getValue "${STRING}" "appType"`
if [ "${appType}" == "jar" -o "${appType}" == "jar-api" ];then
    FILE_DIR=${BUILD_TYPE}/${APP_CODE}/${GROUP}/
    FILE_PATH=${BUILD_TYPE}/${APP_CODE}/${GROUP}/${GIT_BRANCH}.zip
else
    FILE_DIR=${BUILD_TYPE}/${APP_CODE}/
    FILE_PATH=${BUILD_TYPE}/${APP_CODE}/${GIT_BRANCH}.zip
fi
matrix_storage_domain="matrix-storage.missfresh.net"
FILE_EXIST=$(curl -o /dev/null -s -w %{http_code} ${matrix_storage_domain}/${FILE_PATH} )
need=0
if [ "${BUILD_TYPE}" == "release" -o "${BUILD_TYPE}" == "prod" ];then
    FILE_EXIST=$(curl -o /dev/null -s -w %{http_code} ${matrix_storage_domain}/${FILE_PATH} )
    if test -z "${FILE_EXIST}" ;then
        need=1
    elif [ "${FILE_EXIST}" != "200" ]; then
        need=1
    fi
    if [ "${GIT_BRANCH}" == "master" ];then
        need=1
    fi
else
    need=1
fi
if [ "${need}" -ne "0" ];then
if [ ! -e ${CLONE_DIR} ];then
    mkdir -p  ${CLONE_DIR}
fi
if [ ! -e ${CLONE_DIR}${APP_CODE} ];then
    cd ${CLONE_DIR}
    git clone ${GIT_ADDRESS} ${APP_CODE}
fi
if [ "${BUILD_TYPE}" != "release" ];then
    if [ ! -e ${BETA_DIR} ];then
        mkdir -p  ${BETA_DIR}
    fi
    WORKSPACE=${BETA_DIR}${PUBLISH_RECORD}
else
    if [ ! -e ${RELEASE_DIR} ];then
        mkdir -p ${RELEASE_DIR}
    fi
    WORKSPACE=${RELEASE_DIR}${APP_CODE}"/"${PUBLISH_RECORD}"/"${GROUP}
fi
echo "build WORKSPACE = ${WORKSPACE}"
if [ ! -e ${WORKSPACE} ];then
    mkdir -p ${WORKSPACE}
	cp -R ${CLONE_DIR}${APP_CODE}"/". ${WORKSPACE}"/"
	msg "Started by user ${USER_NAME}"
    msg "Building in workspace ${WORKSPACE}"
    cd ${WORKSPACE}"/"
    exec_cmd "git rev-parse --is-inside-work-tree"
    msg "Fetching changes from the remote Git repository"
    exec_cmd "git config remote.origin.url ${GIT_ADDRESS}"
    msg "Fetching upstream changes from ${GIT_ADDRESS}"
    exec_cmd "git --version"
    exec_cmd "git pull"
    exec_cmd "git fetch --tags --progress ${GIT_ADDRESS} +refs/heads/*:refs/remotes/origin/*"
	GIT_REVISION=
	if [ "${BUILD_TYPE}" != "release" -a "${BUILD_TYPE}" != "prod" ];then
		exec_cmd "git rev-parse origin/${GIT_BRANCH}^{commit}"
        GIT_REVISION=`git rev-parse origin/${GIT_BRANCH}^{commit}`
	fi
	if [ "${BUILD_TYPE}" == "release" -o "${BUILD_TYPE}" == "prod" ];then
		exec_cmd "git rev-parse ${GIT_BRANCH}^{commit}"
		GIT_REVISION=`git rev-parse ${GIT_BRANCH}^{commit}`
	fi
	if [ "$?" != "0" ];then
        error_exit "没有对应的分支"
    fi
	msg "Checking out Revision ${GIT_REVISION} (origin/${GIT_BRANCH})"
    git config core.sparsecheckout true
	exec_cmd "git checkout -f ${GIT_REVISION}"
    COMMIT=`git show ${GIT_REVISION}`
    GIT_MESSAGE=`echo ${COMMIT} |awk -F "0800" '{print $2}' | awk -F "diff" '{print $1}'`
    msg "Commit message: ${GIT_MESSAGE}"
    # 检查当前分支是否已经merge origin/master的代码
    if [ "${GIT_ADDRESS}" != "git@git.missfresh.cn:wms-group/wms-t1.git" -a "${GIT_ADDRESS}" != "git@git.missfresh.cn:wms-group/wms-wwwroot.git" -a "${GIT_ADDRESS}" != "git@git.missfresh.cn:wms-group/wms-t3.git" ];then
        beta_prod_master_merged_check
    fi
fi
fi
SHELL="/bin/sh"
if [ 1 -eq `echo ${DEPLOY_SCRIPT}|grep ".py$"|wc -l` ];then
    SHELL=`which python`
fi
msg "${SHELL} ${DEPLOY_SCRIPT} "${STRING}" ${WORKSPACE} &"
${SHELL} ${DEPLOY_SCRIPT} "${STRING}" ${WORKSPACE} &
if [ "$?" != "0" ]; then
    if [ "${BUILD_TYPE}" == "release" ];then
        DEPLOY_STATUS=4
        releaseCallBack &>/dev/null
    else
        DEPLOY_STATUS=9
        betaCallBack &>/dev/null
    fi
    exit 1
fi
wait