#!/bin/bash

CLONE_DIR="/data/matrix/project/"
BETA_DIR="/data/matrix/project/beta/"
RELEASE_DIR="/data/matrix/project/release/"
STRING=$1
echo "========================="
echo "param : "$STRING
echo "========================="
betaCallBack(){
    curl -X POST http://max.missfresh.net/api/publish/publish/betaCallBack -H 'Content-Type: application/json' -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECORD'","batchId":"'$BATCH_RECORD'"}'
}
releaseCallBack(){
        curl -X POST http://max.missfresh.net/api/publish/publish/releaseCallBack -H 'Content-Type: application/json' -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECORD'","batchId":"'$BATCH_RECORD'"}'
}
getValue(){
        echo $1 | awk -F "``$2\":\""  '{print $2}' |awk -F "\"" '{print $1}'
}
getInteger(){
        echo $1 | awk -F "``$2\":"  '{print $2}' |awk -F "," '{print $1}'
}
msg() {
        printf '%s %b\n' "`date +%H:%M:%S`" "$1" >&2
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
        if [ "$BUILD_TYPE" == "release" ];then
		DEPLOY_STATUS=9
		releaseCallBack &>/dev/null
	else
		DEPLOY_STATUS=4
       		betaCallBack &>/dev/null
	fi
	exit 1
}
exec_cmd()
{
        echo "[执行命令] `$1`"
        $1
        if [ "$?" != "0" ]; then
                error_exit "命令执行失败: 错误码为 $?"
        fi
}

GIT_ADDRESS=`getValue $STRING "gitAddress"`
echo "GIT_ADDRESS:"$GIT_ADDRESS
GIT_BRANCH=`getValue $STRING "buildBranch"`
echo "GIT_BRANCH:"$GIT_BRANCH
USER_NAME=`getValue $STRING "user"`
echo "USER_NAME:"$USER_NAME
DEPLOY_SCRIPT=`getValue $STRING "script"`
echo "DEPLOY_SCRIPT:"$DEPLOY_SCRIPT
APP_CODE=`getValue $STRING "appCode"`
echo "APP_CODE:"$APP_CODE
BUILD_TYPE=`getValue $STRING "buildType"`
echo "BUILD_TYPE:"$BUILD_TYPE
GROUP=`getValue $STRING "appGroup"`
echo "GROUP:"$GROUP
PUBLISH_RECORD=`getInteger $STRING "record"`
echo "PUBLISH_RECORD:"$PUBLISH_RECORD
APP_TYPE=`getValue $STRING "appType"`
echo "APP_TYPE:"$APP_TYPE
BATCH_RECORD=`getInteger $STRING "batch"`
echo "BATCH_RECORD:"$BATCH_RECORD
DEPLOY_STATUS=0
TAG=
WORKSPACE=
if [ ! -e $CLONE_DIR ];then
        mkdir -p  $CLONE_DIR
fi
if [ ! -e $CLONE_DIR$APP_CODE ];then
        cd $CLONE_DIR
        git clone $GIT_ADDRESS $APP_CODE
fi
if [ "$BUILD_TYPE" != "release" ];then
        if [ ! -e $BETA_DIR ];then
                mkdir -p  $BETA_DIR
        fi
        WORKSPACE=$BETA_DIR$PUBLISH_RECORD
	echo "beta WORKSPACE : "$WORKSPACE
	else
        if [ ! -e $RELEASE_DIR ];then
                mkdir $RELEASE_DIR
        fi
        WORKSPACE=$RELEASE_DIR$APP_CODE"/"$PUBLISH_RECORD"/"${GROUP}
	echo "release WORKSPACE : "$WORKSPACE
fi
echo "~~~~~~~~~~~~~~~~000000000000000000~~~~~~~~~~~~~~~~~~~~"
echo "WORKSPACE:"$WORKSPACE
if [ ! -e $WORKSPACE ];then
        mkdir -p $WORKSPACE
	cp -R $CLONE_DIR$APP_CODE"/". $WORKSPACE"/"
	msg "Started by user $USER_NAME"
        msg "Building in workspace $WORKSPACE"
        cd $WORKSPACE"/"
        exec_cmd "git rev-parse --is-inside-work-tree"
        msg "Fetching changes from the remote Git repository"
        exec_cmd "git config remote.origin.url $GIT_ADDRESS"
        msg "Fetching upstream changes from $GIT_ADDRESS"
        exec_cmd "git --version"
        exec_cmd "git fetch --tags --progress $GIT_ADDRESS +refs/heads/*:refs/remotes/origin/*"
	GIT_REVISION=
	if [ "$BUILD_TYPE" != "release" ];then
		exec_cmd "git rev-parse origin/$GIT_BRANCH^{commit}"
        	GIT_REVISION=`git rev-parse origin/$GIT_BRANCH^{commit}`
	fi
	if [ "$BUILD_TYPE" == "release" ];then
		exec_cmd "git rev-parse $GIT_BRANCH^{commit}"
		GIT_REVISION=`git rev-parse $GIT_BRANCH^{commit}`
	fi
	if [ "$?" != "0" ];then
                error_exit "没有对应的分支"
        fi
        echo "GIT_REVISION:"$GIT_REVISION
	msg "Checking out Revision $GIT_REVISION (origin/$GIT_BRANCH)"
        git config core.sparsecheckout true
	exec_cmd "git checkout -f $GIT_REVISION"
        COMMIT=`git show $GIT_REVISION`
        GIT_MESSAGE=`echo $COMMIT |awk -F "0800" '{print $2}' | awk -F "diff" '{print $1}'`
        msg "Commit message: $GIT_MESSAGE"
fi
/bin/sh $DEPLOY_SCRIPT $STRING $WORKSPACE &
wait