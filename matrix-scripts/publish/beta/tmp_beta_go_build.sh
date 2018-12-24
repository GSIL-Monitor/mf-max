#!/bin/bash
source /etc/profile
#go发布
STRING=$1
WORKSPACE=$2
echo "WORKSPACE = ${WORKSPACE}"
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
function error_exit() {
    msg "<span style=\"color:red\"><b> ${1}${2}</b> </span>"
    if [ "${build_type}" != "release" -a "${build_type}" != "prod" ];then
        DEPLOY_STATUS=$3
        betaCallBack &>/dev/null
    else
        DEPLOY_STATUS=$4
        releaseCallBack &>/dev/null
    fi
    exit 1
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
    curl -X POST http://max.missfresh.net/api/publish/publish/ipCallBack -H 'Content-Type: application/json'  -d '{"tag":"'${TAG}'","deployStatus":"'${DEPLOY_STATUS}'","recordId":"'${PUBLISH_RECORD}'","batchId":"'${BATCH_RECORD}'","planId":"'${PLAN_RECORD}'","ip":"'${ip}'"}'
}
function http_get_response_code(){
        http_status_code=$(curl -sL -w "%{http_code}" $1 -o /dev/null)
        return ${http_status_code}
}
function http_post_response_code(){
        http_status_code=$(curl -sL -w "%{http_code}" $1 -H 'Content-Type: application/json'  -d $2 -o /dev/null)
        return ${http_status_code}
}
function exec_cmd(){
    printf '%s %b\n' "`date "+%Y-%m-%d %H:%M:%S"`" "[执行命令]$1" >&2
    $1 
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' $2 $3
    fi
}
function git_master_merge(){
    exec_cmd "git checkout master" 12 12
    exec_cmd "git merge ${build_branch}" 12 12
    #exec_cmd "git merge --abort"
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
	    exec_cmd "git checkout master" 12 12
	    exec_cmd "git pull" 12 12
	    exec_cmd "git merge --no-ff tags/${build_branch}" 12 12
	    exec_cmd "git push" 12 12
	    success "合并分支代码到主干上成功！！"
    fi
}
function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ ${tag_type} == "b-" ]; then
        msg "合并分支代码到主干上.."
	    exec_cmd "git checkout master" 12 12
	    exec_cmd "git pull" 12 12
	    exec_cmd "git merge --no-ff tags/${build_branch}" 12 12
	    exec_cmd "git push" 12 12
	    success "合并分支代码到主干上成功！！"
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
        msg "----------------------------------------"
    fi
    DEPLOY_STATUS=11
    TAG=${btag_name}
    betaCallBack &>/dev/null
}
function prod_git_push_rtag(){
    rtag_name=$(date +"r-%Y%m%d-%H%M%S-${BUILD_USER_ID}")
    tag_type=${build_branch:0:2}
    if [ ${tag_type} == "b-" -a ${tag_type} != "master" ]; then
        exec_cmd "git tag ${rtag_name}" 12 12
        exec_cmd "git push origin ${rtag_name}" 12 12
        msg "----------------------------------------"
        success "生成rtag: ${rtag_name}"
        msg "----------------------------------------"
    else
	    msg "无需生成新的rtag"
    fi
    DEPLOY_STATUS=11
    TAG=${rtag_name}
    releaseCallBack &>/dev/null
}
function count_code(){
    count=$(git log --oneline | wc -l)
    msg "----------------------------------------"
    success "后端代码行数为: ${count}"
    msg "----------------------------------------"
}
function mvn_build(){
    if ( 0"${mvn_ops}" = "0" );then
        exec_cmd "mvn clean install package  -Dmaven.test.skip=true -P ${build_type} -f ${PROJECT_POM} " 14 14
    else
        exec_cmd "mvn clean install package  -Dmaven.test.skip=true -P ${build_type} -f ${PROJECT_POM} ${mvn_ops}" 14 14
    fi
}
function zip_file(){
    msg "zip -rq ${1} ${2} ${3} ${4}"
    zip -rq ${1} ${2} ${3} ${4}
}
function go_build(){
    export GOPATH=~/go
    export PATH=$GOPATH/bin:$PATH
    go get -u github.com/Masterminds/glide
    # 临时方案支持一淘
    if [ "${APP_NAME:0:4}" == "mryt" ];then
        rm -rf ~/go/src/missfresh.net/${APP_NAME}/*
        mkdir -p ~/go/src/missfresh.net/${APP_NAME}
        cp -r ${source_path}* ~/go/src/missfresh.net/${APP_NAME}
        cd ~/go/src/missfresh.net/${APP_NAME}
    else
        rm -rf ~/go/src/${APP_NAME}/*
        mkdir -p ~/go/src/${APP_NAME}
        cp -r ${source_path}* ~/go/src/${APP_NAME}
        cd ~/go/src/${APP_NAME}
        glide install
    fi
#    rm -rf ~/go/src/${APP_NAME}/*
#    mkdir -p ~/go/src/${APP_NAME}
#    cp -r ${source_path}* ~/go/src/${APP_NAME}
#    cd ~/go/src/${APP_NAME}
    echo '开始构建'
    go build -v
    echo '完成构建'
    if [ "${build_type}" == "prod" -o "${build_type}" == "release" ];then
        DEPLOY_STATUS=13
        releaseCallBack &>/dev/null
    else
        DEPLOY_STATUS=13
        betaCallBack &>/dev/null
    fi
}
build_branch=`getValue "${STRING}" "buildBranch"`
echo "build_branch:"${build_branch}
BUILD_USER_ID=`getValue "${STRING}" "user"`
echo "USER_NAME:"${BUILD_USER_ID}
APP_NAME=`getValue "${STRING}" "jarName"`
echo "APP_NAME:"$APP_NAME
build_type=`getValue "${STRING}" "buildType"`
echo "BUILD_TYPE:"${build_type}
SERVER_LIST=`getValue "${STRING}" "serverList"`
echo "SERVER_LIST:"$SERVER_LIST
SERVER_LIST=${SERVER_LIST//,/ }
mvn_ops=`getValue "${STRING}" "mvnOps"`
echo "MVN_OPS:"$mvn_ops
GROUP=`getValue "${STRING}" "appGroup"`
echo "GROUP:"$GROUP
deploy_path=`getValue "${STRING}" "deployPath"`
echo "DEPLOY_PATH:"$deploy_path
health_check=`getValue "${STRING}" "healthCheck"`
echo "HEALTHCHECK:"$health_check
PROJECT_POM=`getValue "${STRING}" "projectPom"`
echo "PROJECT_POM:"${PROJECT_POM}
PUBLISH_RECORD=`getInteger "${STRING}" "record"`
echo "PUBLISH_RECORD:"${PUBLISH_RECORD}
APP_TYPE=`getValue "${STRING}" "appType"`
echo "APP_TYPE:"$APP_TYPE
BATCH_RECORD=`getInteger "${STRING}" "batch"`
echo "BATCH_RECORD:"${BATCH_RECORD}
WAIT_TIME=`getInteger "${STRING}" "waitTime"`
echo "WAIT_TIME:"$WAIT_TIME
PLAN_RECORD=`getInteger "${STRING}" "plan"`
echo "PLAN_RECORD:"${PLAN_RECORD}
PROJECT_TASk_ID=`getInteger "${STRING}" "projectTaskId"`
echo "PROJECT_TASk_ID:"${PROJECT_TASk_ID}
APP_CODE=`getValue "${STRING}" "appCode"`
echo "APP_CODE:"${APP_CODE}
DEPLOY_STATUS=
TAG=
SERVER_PORT=`echo ${health_check} | awk -F"/" '{print $1}'`
echo "SERVER_PORT:"${SERVER_PORT}
source_path=${WORKSPACE}/
cd ${WORKSPACE}"/"
if [ "${build_type}" == "" ];then
	error_exit "你需要先设置 build_type!!" '' 12 12
fi
if [ "${build_type}" == 'presstest' ]; then
     beta_prod_master_merged_check
     beta_git_push_btag
elif [ "${build_type}" == 'dev' ]; then
     success "dev发布，跳过git检查！"
     beta_prod_master_merged_check
elif [ "${build_type}" == 'test' ]; then
    beta_prod_master_merged_check
    beta_git_push_btag
elif [ "${build_type}" == 'beta' ]; then
    beta_prod_master_merged_check
    beta_git_push_btag
elif [ "${build_type}" == 'prod' -o "${build_type}" == "release" ]; then
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

function push_go_server(){
    msg "开始启动..."
    local len=${#SERVER_LIST[@]}
    echo "SERVER_LIST_LEN:${len}"
    if [ "${len}" -gt 0 ]; then
        local app_name_zip="${APP_NAME}.zip"
        #压缩文件
        zip_file ${app_name_zip} "${APP_NAME}" "config.yaml" "${md5_file}"
        #推送nginx服务器
        local matrix_storage_ip="10.3.39.33"
        #local matrix_storage_ip="192.168.101.25"
        local remote_path="/data/app/${build_type}/${APP_CODE}/${PROJECT_TASk_ID}"
        #创建存储服务器目录
        /usr/bin/ssh  www@${matrix_storage_ip} "mkdir -p  ${remote_path}"
        #/usr/bin/ssh  mryx@${matrix_storage_ip} "mkdir -p  ${remote_path}"
        if [ "$?" != "0" ]; then
                error_exit "命令执行失败: 错误码为 $?" '' 4 9
        fi

        #临时方案支持每日一淘
        if [ "${APP_NAME:0:4}" == "mryt" ];then
            msg "/usr/bin/rsync -avz -e ssh  ~/go/src/missfresh.net/${APP_NAME}/${app_name_zip} 'www@${matrix_storage_ip}:${remote_path}/${app_name_zip}'"
            /usr/bin/rsync -avz -e ssh  ~/go/src/missfresh.net/${APP_NAME}/${app_name_zip} "www@${matrix_storage_ip}:${remote_path}/${app_name_zip}"
        else
            msg "/usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/${app_name_zip} 'www@${matrix_storage_ip}:${remote_path}/${app_name_zip}'"
            /usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/${app_name_zip} "www@${matrix_storage_ip}:${remote_path}/${app_name_zip}"
        fi
#        msg "/usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/${app_name_zip} 'www@${matrix_storage_ip}:${remote_path}/${app_name_zip}'"
#        /usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/${app_name_zip} "www@${matrix_storage_ip}:${remote_path}/${app_name_zip}"
        #/usr/bin/rsync -avz -e ssh  ~/go/src/${APP_NAME}/${app_name_zip} "mryx@${matrix_storage_ip}:${remote_path}/${app_name_zip}"
        if [ "$?" != "0" ]; then
            error_exit "命令执行失败: 错误码为 $?" '' 4 9
        fi
        package_path="/data/app/${APP_NAME}"
        package_name="${app_name_zip}"
        local matrix_storage_domain="matrix-storage.missfresh.net"
        #local matrix_storage_domain="192.168.101.25:8080"
        package_url="http://${matrix_storage_domain}/${build_type}/${APP_CODE}/${PROJECT_TASk_ID}/${app_name_zip}"
        msg "package_url:${package_url}"
    else
        error_exit "SERVER_LIST为0" '' 4 9
    fi

    for ip in ${SERVER_LIST};
    do
        msg "${ip} ${APP_NAME} 启动消息发送中······"
#        local agent_url="http://${ip}:30000/api/agent/startapp"
#        local healthcheck_url=http://${ip}:${health_check}
        if test -n "${health_check}" ;then
            healthcheck_url=http://${ip}:${health_check}
        fi
        #改为调用发布系统发消息
        local agent_url=http://127.0.0.1:18080/api/publish/agent/startapp
        local post_param=\{\"buildType\":\"${build_type}\",\"packageType\":\"${APP_TYPE}\",\"healthCheckUrl\":\"${healthcheck_url}\",\"packageName\":\"${package_name}\",\"packagePath\":\"${package_path}\",\"packageUrl\":\"${package_url}\",\"action\":\"restart\",\"recordId\":\"${PUBLISH_RECORD}\",\"ip\":\"${ip}\",\"md5Name\":\"${md5_file}\"\}
        #curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'${build_type}'","packageType":"'${APP_TYPE}'","packageName":"'${package_name}'","packagePath":"'${package_path}'","packageUrl":"'${package_url}'","action":"restart"}'
        local http_response=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" )
#        local http_status_code=`echo ${http_response} | awk -F"}" '{print $2}'`
        local http_status_code=`echo ${http_response:0-3:3}`
        if test -z "${http_status_code}" ;then
            error_exit "${ip} ${APP_NAME} 启动消息发送失败, ${http_response}" '' 4 9
        fi
        if [ "${http_status_code}" -ne "200" ];then
            error_exit "${ip} ${APP_NAME} 启动消息发送失败, ${http_status_code}" '' 4 9
        else
            local response_body=`echo ${http_response} | awk -F"}" '{print $1}'`
            local response_code=`getValue "${response_body}" "code"`
            if [ "${response_code}" -ne "0" ];then
                local response_message=`getValue "${response_body}" "message"`
                error_exit "${ip} ${APP_NAME} 启动消息发送失败, ${response_message}" '' 4 9
            fi
            msg "${ip} ${APP_NAME} 启动消息发送成功，启动中..."
            #获取启动结果
            local start_result_code="1"
            for i in {1..100}
            do
                local result_url=http://127.0.0.1:18080/api/publish/agent/getresult
                local post_param=\{\"buildType\":\"${build_type}\",\"recordId\":\"${PUBLISH_RECORD}\",\"ip\":\"${ip}\"\}
                sleep 3
                local http_response=$(curl -sL -w "%{http_code}" ${result_url} -H 'Content-Type: application/json'  -d "${post_param}" )
#                local http_status_code=`echo ${http_response} | awk -F"}" '{print $2}'`
                local http_status_code=`echo ${http_response:0-3:3}`
                if test -z "${http_status_code}" ;then
                    warn "${ip} ${APP_NAME} 启动失败, ${http_response}" '' 4 9
                fi
                if [ "${http_status_code}" -ne "200" ];then
                    warn "${ip} ${APP_NAME} 启动失败, ${http_status_code}" '' 4 9
                else
                    local response_body=`echo ${http_response} | awk -F"}" '{print $1}'`
                    local response_code=`getValue "${response_body}" "code"`
                    if [ "${response_code}" -eq "0" ];then
                        local response_message=`getValue "${response_body}" "message"`
                        start_result_code="0"
                        success "${ip} ${APP_NAME} 启动成功, ${response_message}"
                        break
                    elif [ "${response_code}" -eq "1" ];then
                        local response_message=`getValue "${response_body}" "message"`
                        error_exit "${ip} ${APP_NAME} 启动失败, ${response_message}" '' 4 9
                    else
                        local response_message=`getValue "${response_body}" "message"`
                        msg "${ip} ${APP_NAME} 应用启动中, ${response_message}"
                    fi
                fi
            done
            if [ "${start_result_code}" -ne "0" ];then
                error_exit "${ip} ${APP_NAME} 在300s内未启动完成！请确认服务状态！" '' 4 9
            fi
        fi
    done
    msg "启动结束！"
}
function check_app_status(){
    result=0
    if test -n "${healthcheck_url}" ;then
        for ip in ${SERVER_LIST};
        do
            healthcheck_url=http://${ip}:${health_check}
            agent_url=http://${ip}:30000/api/agent/healthcheck
            post_param=\{\"url\":\"${healthcheck_url}\"\}
            for i in $(seq 1 20);
            do
                msg "探活的地址为:${healthcheck_url}"
                #http_status_code=$(curl -sL -w "%{http_code}" $url -o /dev/null)
                http_status_code=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" -o /dev/null)
                if [ "${http_status_code}" == "200" ];then
                    msg "请求成功，响应码是$http_status_code"
                    break
                else
                    sleep 5
                    msg "服务没有在规定的时间内启动!"
                fi
            done
            if [ "$http_status_code" != "200" ];then
                result=1
                error "${ip}部署失败"
            fi
        done
        if [ "$result" == "1" ];then
            error_exit "发布失败" '' 4 9
        else
            success "发布成功"
        fi
    else
        msg "没有探活地址，请在服务器上确认是否启动成功～"
    fi
}

go_build
md5_file="${APP_NAME}.md5"
#临时方案支持每日一淘
if [ "${APP_NAME:0:4}" == "mryt" ];then
    md5sum ${APP_NAME} >${md5_file}
else
    md5sum ${APP_NAME} config.yaml >${md5_file}
fi
#md5sum ${APP_NAME} config.yaml >${md5_file}
if [ "$?" != "0" ]; then
    error_exit "命令执行失败: 错误码为 $?" '' 4 9
fi
#push_go_server && check_app_status
push_go_server
wait
if [ "${build_type}" != "release" -a "${build_type}" != "prod" ];then
        DEPLOY_STATUS=3
        betaCallBack &>/dev/null
        rm -rf ${WORKSPACE}
else
        DEPLOY_STATUS=8
        releaseCallBack &>/dev/null
        sleep ${WAIT_TIME}
        batchCallBack &>/dev/null
fi
exit 1