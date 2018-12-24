#!/bin/bash

CLONE_DIR="/data/matrix/project"
BETA_DIR="/data/matrix/project/beta"
RELEASE_DIR="/data/matrix/project/release"
STRING=$1

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
GIT_BRANCH=`getValue $STRING "buildBranch"`
USER_NAME=`getValue $STRING "user"`
DEPLOY_SCRIPT=`getValue $STRING "script"`
APP_CODE=`getValue $STRING "appCode"`
WORKSPACE=
JAR_NAME=`getValue $STRING "jarName"`
BUILD_TYPE=`getValue $STRING "buildType"`
echo $BUILD_TYPE
SERVER_LIST=`getValue $STRING "serverList"`
MVN_OPS=`getValue $STRING "mvnOps"`
#GROUP=`getValue $STRING "group"`
DEPLOY_PATH=`getValue $STRING "deployPath"`
HEALTHCHECK=`getValue $STRING "healthCheck"`
PROJECT_POM=`getValue $STRING "projectPom"`
PUBLISH_RECORD=`getInteger $STRING "record"`
APP_TYPE=`getValue $STRING "appType"`
BATCH_RECORD=
DEPLOY_STATUS=
TAG=
callBack(){
    curl -X POST http://localhost:18090/api/matrix/publish/betaCallBack -H 'Content-Type: application/json' -d '{"tag":"'$TAG'","deployStatus":"'$DEPLOY_STATUS'","recordId":"'$PUBLISH_RECORD'","batchId":"'$BATCH_RECORD'"}'
}

if [ ! -e $CLONE_DIR ];then
        mkdir $CLONE_DIR
fi
if [ ! -e $CLONE_DIR$APP_CODE ];then
        cd $CLONE_DIR
        git clone $GIT_ADDRESS $APP_CODE
fi
if [ "$BUILD_TYPE" == "beta" -o "$BUILD_TYPE" == "dev" ];then
        if [ ! -e $BETA_DIR ];then
                mkdir $BETA_DIR
        fi
        #if [ ! -e $BETA_DIR"/"$PUBLISH_RECORD ];then
        #        mkdir $BETA_DIR"/"$PUBLISH_RECORD
        #fi
        WORKSPACE=$BETA_DIR"/"$PUBLISH_RECORD
        #cp -r $CLONE_DIR$APP_CODE $WORKSPACE
else
echo "hahaah"
        if [ ! -e $RELEASE_DIR ];then
                mkdir $RELEASE_DIR
        fi
        #if [ ! -e $RELEASE_DIR"/"$APP_CODE ];then
        #        mkdir $RELEASE_DIR"/"$APP_CODE
        #fi
        WORKSPACE=$RELEASE_DIR"/"$APP_CODE
fi
echo "build 1111"
echo $WORKSPACE
if [ ! -e $WORKSPACE ];then
        mkdir $WORKSPACE
        cp -R $CLONE_DIR"/"$APP_CODE"/". $WORKSPACE"/"
        msg "Started by user $USER_NAME"
        msg "Building in workspace $WORKSPACE"
        cd $WORKSPACE"/"
        pwd
        git rev-parse --is-inside-work-tree &>/dev/null
        msg "Fetching changes from the remote Git repository"
        git config remote.origin.url $GIT_ADDRESS
        msg "Fetching upstream changes from $GIT_ADDRESS"
        git --version
        git fetch --tags --progress $GIT_ADDRESS +refs/heads/*:refs/remotes/origin/*
        git rev-parse origin/$GIT_BRANCH^{commit}
        if [ $? -ne 0 ];then
                error_exit "没有对应的分支"
        fi
        GIT_REVISION=`git rev-parse origin/$GIT_BRANCH^{commit}`
        msg "Checking out Revision $GIT_REVISION (origin/$GIT_BRANCH)"
        git config core.sparsecheckout
        git checkout -f $GIT_REVISION
        COMMIT=`git show $GIT_REVISION`
        GIT_MESSAGE=`echo $COMMIT |awk -F "0800" '{print $2}' | awk -F "diff" '{print $1}'`
        msg "Commit message: $GIT_MESSAGE"
fi
if [ "$APP_TYPE" == "go" ];then
        /bin/sh $DEPLOY_SCRIPT $SERVER_LIST $JAR_NAME $WORKSPACE $USER_NAME $GIT_BRANCH $BUILD_TYPE $PUBLISH_RECORD $BATCH_RECORD
elif [ "$APP_TYPE" == "static" ];then
        /bin/sh $DEPLOY_SCRIPT $SERVER_LIST $DEPLOY_PATH $WORKSPACE $USER_NAME $GIT_BRANCH $BUILD_TYPE $PUBLISH_RECORD $BATCH_RECORD
else
        /bin/sh $DEPLOY_SCRIPT $SERVER_LIST $JAR_NAME $DEPLOY_PATH $WORKSPACE $USER_NAME $GIT_BRANCH $BUILD_TYPE $PROJECT_POM $PUBLISH_RECORD $PUBLISH_RECORD $BATCH_RECORD
fi