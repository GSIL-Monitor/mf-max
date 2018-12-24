#!/bin/bash
source /etc/profile
#测试环境前端发布脚本

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
    if [ "${build_type}" != "release" -a "${build_type}" != "prod" ];then
        DEPLOY_STATUS=$3
        betaCallBack &>/dev/null
    else
        DEPLOY_STATUS=$4
        releaseCallBack &>/dev/null
    fi
    exit 1
}
function http_get_response_code(){
    http_status_code=$(curl -sL -w "%{http_code}" $1 -o /dev/null)
    return ${http_status_code}
}
function http_post_response_code(){
    http_status_code=$(curl -sL -w "%{http_code}" $1 -H 'Content-Type: application/json'  -d $2 -o /dev/null)
    return ${http_status_code}
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
function exec_cmd(){
    printf '%s %b\n' "`date "+%Y-%m-%d %H:%M:%S"`" "[执行命令]$1" >&2
    $1
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' $2 $3
    fi
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
		success "${diff_log}"
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
function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ ${tag_type} == "b-" ]; then
        msg "合并分支代码到主干上.."
	    exec_cmd "git checkout master" 12 12
	    exec_cmd "git pull" 12 12
	    exec_cmd "git merge --no-ff tags/${build_branch}" 12 12
	    exec_cmd "git push origin HEAD:master" 12 12
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
        success "----------------------------------------"
        success "生成btag: ${btag_name}"
        success "----------------------------------------"
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
       success "----------------------------------------"
       success "生成rtag: ${rtag_name}"
       success "----------------------------------------"
    else
	    success "无需生成新的rtag"
    fi
    DEPLOY_STATUS=11
    TAG=${rtag_name}
    releaseCallBack &>/dev/null
}
function zip_file(){
    msg "zip -rq ${1} ${2} ${3}"
    zip -rq ${1} ${2} ${3}
}
function push_static_server(){
    msg "开始启动..."
    if test -z ${pkgPath};then
        package_path="/data/app/static-html/${build_path}"
    else
        package_path="${pkgPath}"    
    fi 
    package_name="${app_name_md5_zip}" #改成我生成的
    package_url="http://${matrix_storage_domain}/${build_type}/${APP_CODE}/${build_branch}.zip" #改成我的/data/app/后缀

    for ip in ${SERVER_LIST};
    do
        post_param=\{\"buildType\":\"${build_type}\",\"packageType\":\"${APP_TYPE}\",\"healthCheckUrl\":\"${healthcheck_url}\",\"packageName\":\"${package_name}\",\"packagePath\":\"${package_path}\",\"packageUrl\":\"${package_url}\",\"action\":\"restart\",\"recordId\":\"${PUBLISH_RECORD}\",\"ip\":\"${ip}\",\"md5Name\":\"${md5_name}\"\}
#        local agent_url="http://${ip}:30000/api/agent/startapp"
        #改为调用发布系统发消息
        local agent_url=http://127.0.0.1:18080/api/publish/agent/startapp
        #前端不用healthcheck
        if test -n "${health_check}" ;then
            healthcheck_url=http://${ip}:${health_check}
        fi
        #local http_status_code=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" -o /dev/null)
        msg "${ip} ${package_name} 启动消息发送成功，启动中..."
        http_response=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" )
#        local http_status_code=`echo ${http_response} | awk -F"}" '{print $2}'`
        local http_status_code=`echo ${http_response:0-3:3}`
        if test -z "${http_status_code}" ;then
            error_exit "${ip} ${package_name} 启动消息发送失败, ${http_response}" '' 4 9
        fi
        if [ "${http_status_code}" -ne "200" ];then
            error_exit "${ip} ${package_name} 启动消息发送失败, ${http_status_code}" '' 4 9
        else
            local response_body=`echo ${http_response} | awk -F"}" '{print $1}'`
            local response_code=`getValue "${response_body}" "code"`
            if test -z "${response_code}" ;then
                error_exit "${ip} ${package_name} 启动消息发送失败, ${response_body}" '' 4 9
            fi
            if [ "${response_code}" -ne "0" ];then
                local response_message=`getValue "${response_body}" "message"`
                error_exit "${ip} ${package_name} 启动消息发送失败, ${response_message}" '' 4 9
            fi
            msg "${ip} ${package_name} 启动消息发送成功，启动中..."
            #获取启动结果
            local start_result_code="1"
            for i in {1..60}
            do
                local result_url=http://127.0.0.1:18080/api/publish/agent/getresult
                local post_param=\{\"buildType\":\"${build_type}\",\"recordId\":\"${PUBLISH_RECORD}\",\"ip\":\"${ip}\"\}
                sleep 3
                local http_response=$(curl -sL -w "%{http_code}" ${result_url} -H 'Content-Type: application/json'  -d "${post_param}" )
#                local http_status_code=`echo ${http_response} | awk -F"}" '{print $2}'`
                local http_status_code=`echo ${http_response:0-3:3}`
                if test -z "${http_status_code}" ;then
                    warn "${ip} ${package_name} 获取启动结果失败, ${http_response}" '' 4 9
                fi
                if [ "${http_status_code}" -ne "200" ];then
                    warn "${ip} ${package_name} 获取启动结果失败, ${http_status_code}" '' 4 9
                else
                    local response_body=`echo ${http_response} | awk -F"}" '{print $1}'`
                    local response_code=`getValue "${response_body}" "code"`
                    if [ "${response_code}" -eq "0" ];then
                        local response_message=`getValue "${response_body}" "message"`
                        start_result_code="0"
                        success "${ip} ${package_name} 启动成功, ${response_message}"
                        break
                    elif [ "${response_code}" -eq "1" ];then
                        local response_message=`getValue "${response_body}" "message"`
                        error_exit "${ip} ${package_name} 启动失败, ${response_message}" '' 4 9
                    else
                        local response_message=`getValue "${response_body}" "message"`
                        msg "${ip} ${package_name} 应用启动中, ${response_message}" '' 4 9
                    fi
#                    msg "${ip} ${jar_name} 启动完成"
                fi
            done
            if [ "${start_result_code}" -ne "0" ];then
                error_exit "${ip} ${package_name} 在300s内未启动完成！请确认服务状态！" '' 4 9
            fi
        fi
#        curl -X POST http://${ip}:30000/api/agent/startapp -H 'Content-Type: application/json'  -d '{"buildType":"'${build_type}'","packageType":"'${APP_TYPE}'","packageName":"'${package_name}'","packagePath":"'${package_path}'","packageUrl":"'${package_url}'","action":"restart"}'
        sleep 10
        DEPLOY_STATUS=8
        ipCallBack &>/dev/null
    done
    msg "启动结束"
}
function set_toket(){
    path=${job_path}/src/router/index.js
    if [ -f $path ]; then
        sed -i "" "s/^store.commit('user\/setAccessToken'.*/store.commit('user\/setAccessToken', Cookies.get('token'))/" $path
    fi
}
function npm_build(){
    #set_toket
    if [ -e ${job_path}/dist/ ];then
        exec_cmd "rm -rf ${job_path}/dist/*" 14 14
    fi
    if test -z "${MAVEN_PATH}";then
        exec_cmd "npm install --registry=https://registry.npm.taobao.org" 14 14
    elif [ "${MAVEN_PATH:0:4}" != "http" ];then
        exec_cmd "npm install --registry=https://registry.npm.taobao.org" 14 14
    else
        exec_cmd "npm install --registry=${MAVEN_PATH}" 14 14
    fi
    #参数可配
    if test -z  ${mvn_ops} ;then
        exec_cmd "npm run build" 14 14
    else
        exec_cmd "npm run ${mvn_ops}" 14 14
    fi
#    exec_cmd "npm run build" 14 14
    exec_cmd "chown -R www:www ${job_path}/dist" 14 14
    if [ "${build_type}" == "prod" -o "${build_type}" == "release" ];then
        DEPLOY_STATUS=13
        releaseCallBack &>/dev/null
    else
        DEPLOY_STATUS=13
        betaCallBack &>/dev/null
    fi
}

STRING=$1
WORKSPACE=$2

echo "WORKSPACE = ${WORKSPACE}"

matrix_storage_domain="matrix-storage.missfresh.net"
job_path=${WORKSPACE}
source_path=${WORKSPACE}/dist/
build_branch=`getValue "${STRING}" "buildBranch"`
echo "build_branch:"${build_branch}
BUILD_USER_ID=`getValue "${STRING}" "user"`
echo "BUILD_USER_ID:"${BUILD_USER_ID}
build_type=`getValue "${STRING}" "buildType"`
echo "build_type:"${build_type}
SERVER_LIST=`getValue "${STRING}" "serverList"`
echo "SERVER_LIST:"${SERVER_LIST}
SERVER_LIST=${SERVER_LIST//,/ }
PUBLISH_RECORD=`getInteger "${STRING}" "record"`
echo "PUBLISH_RECORD:"${PUBLISH_RECORD}
BATCH_RECORD=`getInteger "${STRING}" "batch"`
echo "BATCH_RECORD:"${BATCH_RECORD}
PLAN_RECORD=`getInteger "${STRING}" "plan"`
echo "PLAN_RECORD:"${PLAN_RECORD}
mvn_ops=`getValue "${STRING}" "mvnOps"`
echo "mvn_ops:"${mvn_ops}
DEPLOY_STATUS=
TAG=
build_path=`getValue "${STRING}" "buildPath"`
echo "build_path:"${build_path}
WAIT_TIME=`getInteger "${STRING}" "waitTime"`
echo "WAIT_TIME:"$WAIT_TIME
PROJECT_TASk_ID=`getInteger "${STRING}" "projectTaskId"`
echo "PROJECT_TASk_ID:"${PROJECT_TASk_ID}
APP_CODE=`getValue "${STRING}" "appCode"`
echo "APP_CODE:"${APP_CODE}
APP_TYPE=`getValue "${STRING}" "appType"`
echo "APP_TYPE:"${APP_TYPE}
MAVEN_PATH=`getValue "${STRING}" "mavenPath"`
echo "MAVEN_PATH:"${MAVEN_PATH}
pkgPath=`getValue "${STRING}" "pkgPath"`
echo "pkgPath:"${pkgPath}
matrix_storage_ip="10.3.39.33"
app_name_zip="static-html.zip"
md5_name="static-html.zip.md5"
app_name_md5_zip="static-html-md5.zip"
FILE_DIR=/data/app/${build_type}/${APP_CODE}/
FILE_PATH=/data/app/${build_type}/${APP_CODE}/${build_branch}.zip
FILE_EXIST=$(curl -o /dev/null -s -w %{http_code} ${matrix_storage_domain}/${build_type}/${APP_CODE}/${build_branch}.zip )
need=0
if test -z "${FILE_EXIST}" ;then
    need=1
elif [ "${FILE_EXIST}" != "200" ]; then
    need=1
fi
if [ "${build_branch}" == "master" ];then
    need=1
fi
if [ "${need}" -ne "0" ];then
    cd ${WORKSPACE}/
    if [ "${build_type}" == "" ];then
       error_exit "你需要先设置 build_type!!" '' 12 12
    fi
    if [ "${build_type}" == 'dev' ]; then
        success "dev发布，跳过git检查！"
        npm_build
        beta_prod_master_merged_check
    elif [ "${build_type}" == 'test' ]; then
        beta_prod_master_merged_check
        npm_build
        beta_git_push_btag
    elif [ "${build_type}" == 'beta' ]; then
        beta_prod_master_merged_check
        npm_build
        beta_git_push_btag
    elif [ "$build_type" == 'release' ]; then
        prod_branch_check
        prod_rollback_check
        if [ "$?" == "1" ]; then
            warn "由于发布不是最新的rtag，系统判断为一次回滚操作，跳过主干合并检测和生成新的rag"
            npm_build
        else
            success "执行新分支发布.."
            #beta_prod_master_merged_check
            #prod_branch_merge_to_master
            npm_build
            #prod_git_push_rtag
            success "git发布完成!"
        fi
    fi
    cd ${source_path}
    zip_file ${app_name_zip} "*"
    md5sum ${app_name_zip} >${md5_name}
    zip_file ${app_name_md5_zip} ${app_name_zip} ${md5_name}
    msg "/usr/bin/ssh  www@${matrix_storage_ip} 'mkdir -p  ${FILE_DIR}'" #目录创建成我的
    /usr/bin/ssh  www@${matrix_storage_ip} "mkdir -p  ${FILE_DIR}" #目录修改
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi
    msg "/usr/bin/rsync -avz -e ssh  ${app_name_md5_zip} 'www@${matrix_storage_ip}:${FILE_PATH}'" #地址修改
    /usr/bin/rsync -avz -e ssh  ${app_name_md5_zip} "www@${matrix_storage_ip}:${FILE_PATH}" #地址修改
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi    
    if test -n "${rtag_name}" ;then
        /usr/bin/rsync -avz -e ssh  ${app_name_md5_zip} "www@${matrix_storage_ip}:/data/app/${build_type}/${APP_CODE}/${rtag_name}.zip" #地址修改
        if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi
    fi
fi
push_static_server
#if [ "${need}" -ne "0" ];then
#    cd ${WORKSPACE}"/"
#    prod_branch_merge_to_master
#    prod_git_push_rtag
#fi
msg "部署成功，开始合并代码"
cd /data/matrix/project/${APP_CODE}
if [ "$?" != "0" ]; then
error_exit "代码合入失败"
fi
exec_cmd "git checkout master"
exec_cmd "git pull"
git_commit=`git log | grep "Merge tag 'tags/${build_branch}'" `
echo "git_commit:${git_commit}"
if [ "${git_commit}" == "" ];then
    msg "${build_branch}未合并过master，执行代码合并"
    beta_prod_master_merged_check
    prod_branch_merge_to_master
    prod_git_push_rtag
else
    msg "${build_branch}已经合并master"
fi
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