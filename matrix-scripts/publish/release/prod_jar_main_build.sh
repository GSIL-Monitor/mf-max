 #!/bin/bash
source /etc/profile
#线上jar发布
STRING=$1
WORKSPACE=$2
echo "WORKSPACE = ${WORKSPACE}"
function getValue(){
        echo $1 | awk -F "$2\":\""  '{print $2}' |awk -F "\"" '{print $1}'
}
function getInteger(){
        echo $1 | awk -F "$2\":"  '{print $2}' |awk -F "," '{print $1}'
}
function getStartOps(){
        echo $1 | awk -F "$2\":\"{" '{print $2}' | awk -F "}\"" '{print $1}'
}
function getIpStartOps(){
        echo $1 | awk -F"\"$2" '{print $2}' |awk -F"\"" '{print $3}'| awk -F "\\" '{print $1}'
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
        if [ "${build_type}" != "release" ];then
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
function exec_cmd(){
        printf '%s %b\n' "`date "+%Y-%m-%d %H:%M:%S"`" "[执行命令]$1" >&2
        #$1 | sed "s/^/`date +%H:%M:%S` &/g"
        $1
        if [ "$?" != "0" ]; then
                error_exit "命令执行失败: 错误码为 $?" '' $2 $3
        fi
}
function http_get_response_code(){
        http_status_code=$(curl -sL -w "%{http_code}" $1 -o /dev/null)
        return ${http_status_code}
}
function http_post_response_code(){
        http_status_code=$(curl -sL -w "%{http_code}" $1 -H 'Content-Type: application/json'  -d $2 -o /dev/null)
        return ${http_status_code}
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
        if [ "${build_branch}" == "master" ]; then
                echo "【${build_branch}】无需merge检查"
                return 0
        fi
	tag_type=${build_branch:0:2}
	if [ ${tag_type} != "b-" -a ${tag_type} != "r-" ]; then
		diff_target="^origin/${build_branch}"
	else
	        diff_target="^tags/${build_branch}"
	fi
 	diff_log=`git log origin/master ${diff_target} --oneline`
	if [ -n "${diff_log}" ]; then
        msg  "----------------------------------------"
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
	        exec_cmd "git checkout ${build_branch}" 12 12
	        exec_cmd "git pull" 12 12
	        exec_cmd "git merge --no-ff tags/master" 12 12
	        exec_cmd "git push origin HEAD:${build_branch}" 12 12
	        success "合并主干代码到分支上成功！！"
        fi
}
function prod_branch_merge_to_master(){
        tag_type=${build_branch:0:2}
        if [ "${build_branch}" == "master" ]; then
                echo "【${build_branch}】无需代码合并！！"
                return 0
        fi
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
        if [ "${tag_type}" == "master" ]; then
                return 0
        fi
        diff_log=`git log origin/master ^tags/${build_branch} --oneline`
        if [ "${diff_log}" != "" ]; then
  	            return 1
        fi
        return 0
}
#TODO
function beta_git_push_btag(){
        if [ "${build_branch}" == "master" ]; then
                echo "【${build_branch}】无需生成btag"
                return 0
        fi
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
# rtag
#
#TODO
function prod_git_push_rtag(){
        tag_type=${build_branch:0:2}
        if [ "${build_branch}" == "master" ]; then
                echo "【${build_branch}】无需处理"
                return 0
        fi
        rtag_name=$(date +"r-%Y%m%d-%H%M%S-${BUILD_USER_ID}")
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
        if test -z  ${group} ;then
            group=""
        fi
        if test -z ${MAVEN_PATH} ;then
                if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean install deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} " 14 14
                else
                        exec_cmd "mvn clean install deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} ${mvn_ops}" 14 14
                fi
        else
                if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean install deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} -s ${MAVEN_PATH} " 14 14
                else
                        exec_cmd "mvn clean install deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} -s ${MAVEN_PATH} ${mvn_ops}" 14 14
                fi
        fi
        if [ "${build_type}" == "release" ];then
                DEPLOY_STATUS=13
                releaseCallBack &>/dev/null
        else
                DEPLOY_STATUS=13
                betaCallBack &>/dev/null
        fi

}
# jar包路径
#source_path="${WORKSPACE}${deploy_path}${jar_name}"
#
function zip_file(){
    msg "zip -rq ${1} ${2} ${3}"
    zip -rq ${1} ${2} ${3}
    if [ "$?" != "0" ]; then
    error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi
}
function backup(){
        backup_file_path=/data/backup/${jar_name}
        if [ ! -d "${backup_file_path}" ]; then
                exec_cmd "mkdir -p ${backup_file_path}" 4 9
        fi
        exec_cmd "cp ${source_path} ${backup_file_path}/${rtag_name}-${GROUP}.jar" 4 9
}
function rollback(){
        backup_file_path=/data/backup/${jar_name}
        if [ ! -f "${backup_file_path}/${build_branch}-${GROUP}.jar" ];then
                mvn_build
                return 0
        fi
        echo "source_path='${backup_file_path}/${build_branch}-{GROUP}.jar'"
        source_path="${backup_file_path}/${build_branch}-{GROUP}.jar"
}
# master分支代码回滚
function rollback_master_branch(){
        backup_branch_name=$(date +"k_%Y%m%d_${build_branch}")
        exec_cmd "git fetch" 12 12
        exec_cmd "git checkout -b ${backup_branch_name} ${build_branch}" 12 12
        echo "开始回滚master代码为Tag：${build_branch}" 12 12
        exec_cmd "git push origin ${backup_branch_name}:master -f" 12 12
        #exec_cmd "git reset --hard ${build_branch}"
}

build_branch=`getValue "${STRING}" "buildBranch"`
echo "GIT_BRANCH:"${build_branch}
BUILD_USER_ID=`getValue "${STRING}" "user"`
echo "BUILD_USER_ID:"${BUILD_USER_ID}
jar_name=`getValue "${STRING}" "jarName"`
echo "jar_name:"${jar_name}
build_type=`getValue "${STRING}" "buildType"`
echo "build_type:"${build_type}
SERVER_LIST=`getValue "${STRING}" "serverList"`
echo "SERVER_LIST:"${SERVER_LIST}
SERVER_LIST=${SERVER_LIST//,/ }
mvn_ops=`getValue "${STRING}" "mvnOps"`
echo "mvn_ops:"${mvn_ops}
GROUP=`getValue "${STRING}" "appGroup"`
echo "GROUP:"${GROUP}
deploy_path=`getValue "${STRING}" "deployPath"`
echo "DEPLOY_PATH:"${deploy_path}
health_check=`getValue "${STRING}" "healthCheck"`
echo "health_check:"$health_check
PROJECT_POM=`getValue "${STRING}" "projectPom"`
echo "PROJECT_POM:"${PROJECT_POM}
PUBLISH_RECORD=`getInteger "${STRING}" "record"`
echo "PUBLISH_RECORD:"${PUBLISH_RECORD}
APP_TYPE=`getValue "${STRING}" "appType"`
echo "APP_TYPE:"${APP_TYPE}
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
ALL_START_OPS=`getStartOps "${STRING}" "startOps"`
#START_OPS=`echo ${START_OPS} | sed 's/@/ /g'`
echo "ALL_START_OPS:"${ALL_START_OPS}
MAVEN_PATH=`getValue "${STRING}" "mavenPath"`
echo "MAVEN_PATH:"${MAVEN_PATH}
SERVER_PORT=`echo ${health_check} | awk -F"/" '{print $1}'`
echo "SERVER_PORT:"${SERVER_PORT}
DEPLOY_STATUS=
TAG=
app_name_zip="${jar_name}-md5.zip"
md5_file="${jar_name}.md5"
switchNode_url="http://127.0.0.1:18080/api/publish/lb/switchNode"

if [ "${build_type}" != "release" ];then
    source_path="${WORKSPACE}${deploy_path}${jar_name}"
else
    source_path="${WORKSPACE}${deploy_path}${jar_name}"
fi
matrix_storage_ip="10.3.39.33"
matrix_storage_domain="matrix-storage.missfresh.net"
FILE_DIR=/data/app/${build_type}/${APP_CODE}/${GROUP}/
FILE_PATH=/data/app/${build_type}/${APP_CODE}/${GROUP}/${build_branch}.zip
FILE_EXIST=$(curl -o /dev/null -s -w %{http_code} ${matrix_storage_domain}/${build_type}/${APP_CODE}/${GROUP}/${build_branch}.zip )
need=0
if test -z "${FILE_EXIST}" ;then
    need=1
elif [ "${FILE_EXIST}" != "200" ]; then
    need=1
fi
if [ "${build_branch}" == "master" ];then
    need=1
fi
function zip_jar_main_file() {
    jar=`echo ${jar_name:0-4:4}`
    jarmain_name=${jar_name}
    if [ "${jar}" == ".jar" ] ;then
      jarmain_name=${jar_name%.jar}
    fi
    mkdir ${jarmain_name}
    mv -f classes/*.xml ${jarmain_name}/
    mv -f classes/*.properties ${jarmain_name}/
    mv ${jar_name} ${jarmain_name}
    mv -f lib ${jarmain_name}
    jar_name_zip="${jar_name}.zip"
    cd ${jarmain_name}
    zip_file ${jar_name_zip} "./*"
}
if [ "${need}" -ne "0" ];then
    cd ${WORKSPACE}"/"
    BUILD_NUMBER=${BUILD_USER_ID}_${build_branch}
    if [ "${build_type}" == "" ];then
        error_exit "你需要先设置 build_type!!" '' 12 12
    fi
    if [ "${build_type}" == 'dev' ]; then
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
        if [ ! -e ${WORKSPACE}${deploy_path} ];then
            prod_branch_check
            prod_rollback_check
            if [ "$?" == "1" ]; then
                warn "由于发布不是最新的rtag，系统判断为一次回滚操作，跳过主干合并检测和生成新的rag"
                # 如果有问题，则换成
                rollback
                success "git发布完成!"
            else
                success "执行新分支发布.."
                beta_prod_master_merged_check
                #prod_branch_merge_to_master
                #prod_git_push_rtag
                success "git发布完成!"
                mvn_build
                if [ "${build_branch}" != "master" ]; then
                    echo "【${build_branch}】开始备份！！"
                    backup
                fi
            fi
        fi
    fi
    #压缩文件
    #推送nginx服务器
    #创建存储服务器目录
    zip_jar_main_file
    cd "${WORKSPACE}${deploy_path}"
    md5sum ${jar_name_zip} >${md5_file}
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi
    zip_file ${app_name_zip} "${md5_file}" "${jar_name_zip}"
    /usr/bin/ssh  www@${matrix_storage_ip} "mkdir -p  ${FILE_DIR}"
    if [ "$?" != "0" ]; then
        error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi
    msg "/usr/bin/rsync -avz -e ssh ${app_name_zip} 'www@${matrix_storage_ip}:${FILE_PATH}'"
    /usr/bin/rsync -avz -e ssh ${app_name_zip} "www@${matrix_storage_ip}:${FILE_PATH}"
    if [ "$?" != "0" ]; then
       error_exit "命令执行失败: 错误码为 $?" '' 4 9
    fi
    if test -n "${rtag_name}";then
        /usr/bin/rsync -avz -e ssh ${app_name_zip} "www@${matrix_storage_ip}:${FILE_DIR}${rtag_name}.zip"
        if [ "$?" != "0" ]; then
            error_exit "命令执行失败: 错误码为 $?" '' 4 9
        fi
    fi
fi

##############################
#发布
#每台机器
#1、启动（发启动消息；获取启动结果）
##############################
function push_jar_server(){
    msg "开始启动..."
    package_path="/data/app/${APP_CODE}"
    package_name="${app_name_zip}"
    package_url="http://${matrix_storage_domain}/${build_type}/${APP_CODE}/${GROUP}/${build_branch}.zip"

    for ip in ${SERVER_LIST}
    do

        #1、启动（发启动消息；获取启动结果）
        start_ops=`getIpStartOps ${ALL_START_OPS} "${ip}"`
        echo "start_ops:"${start_ops}
        msg "${ip} ${jar_name} 请求启动中···"
#        local agent_url=http://${ip}:30000/api/agent/startapp
#        local healthcheck_url=http://${ip}:${health_check}
        if test -n "${health_check}" ;then
            healthcheck_url=http://${ip}:${health_check}
        fi
        #改为调用发布系统发消息
        local agent_url=http://127.0.0.1:18080/api/publish/agent/startapp
        local post_param=\{\"buildType\":\"${build_type}\",\"packageType\":\"${APP_TYPE}\",\"healthCheckUrl\":\"${healthcheck_url}\",\"packageName\":\"${package_name}\",\"packagePath\":\"${package_path}\",\"packageUrl\":\"${package_url}\",\"action\":\"restart\",\"startOps\":\"${start_ops}\",\"recordId\":\"${PUBLISH_RECORD}\",\"ip\":\"${ip}\",\"md5Name\":\"${md5_file}\"\}
        #http_status_code=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" -o /dev/null)
        echo "post_param:"${post_param}
        local http_response=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" )
        msg "${ip} ${jar_name} 启动消息发送中···"
#        local http_status_code=`echo ${http_response} | awk -F"}" '{print $2}'`
        local http_status_code=`echo ${http_response:0-3:3}`
        if test -z "${http_status_code}" ;then
            error_exit "${ip} ${jar_name} 启动消息发送失败, ${http_response}" '' 4 9
        fi
        if [ "${http_status_code}" -ne "200" ];then
            error_exit "${ip} ${jar_name} 启动消息发送失败, ${http_status_code}" '' 4 9
        else
            local response_body=`echo ${http_response} | awk -F"}" '{print $1}'`
            local response_code=`getValue "${response_body}" "code"`
            if test -z "${response_code}" ;then
                error_exit "${ip} ${jar_name} 启动消息发送失败, ${response_body}" '' 4 9
            fi
            if [ "${response_code}" -ne "0" ];then
                local response_message=`getValue "${response_body}" "message"`
                error_exit "${ip} ${jar_name} 启动消息发送失败, ${response_message}" '' 4 9
            fi
            success "${ip} ${jar_name} 启动消息发送成功，启动中..."
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
                    warn "${ip} ${jar_name} 获取启动结果失败, ${http_response}"
                fi
                if [ "${http_status_code}" -ne "200" ];then
                    warn "${ip} ${jar_name} 获取启动结果失败, ${http_status_code}" '' 4 9
                else
                    local response_body=`echo ${http_response} | awk -F"}" '{print $1}'`
                    local response_code=`getValue "${response_body}" "code"`
                    if [ "${response_code}" -eq "0" ];then
                        local response_message=`getValue "${response_body}" "message"`
                        start_result_code="0"
                        success "${ip} ${jar_name} 启动成功, ${response_message}"
                        break
                    elif [ "${response_code}" -eq "1" ];then
                        local response_message=`getValue "${response_body}" "message"`
                        error_exit "${ip} ${jar_name} 启动失败, ${response_message}" '' 4 9
                    else
                        local response_message=`getValue "${response_body}" "message"`
                        msg "${ip} ${jar_name} 应用启动中, ${response_message}"
                    fi
#                    msg "${ip} ${jar_name} 启动完成"
                fi
            done
            if [ "${start_result_code}" -ne "0" ];then
                error_exit "${ip} ${jar_name} 在180s内未启动完成！请确认服务状态！" '' 4 9
            fi
        fi

    done
    msg "启动结束！"

}
##############################
##发布探活
##############################
http_status_code=
#废弃
function check_app_status(){
    result=0
    for ip in ${SERVER_LIST};
    do
        healthcheck_url=http://${ip}:${health_check}
        agent_url=http://${ip}:30000/api/agent/healthcheck
        post_param=\{\"url\":\"${healthcheck_url}\"\}
        for i in $(seq 1 20);
		do
			msg "探活的地址为:${healthcheck_url}"
			#http_status_code=$(curl -sL -w "%{http_code}" $url -o /dev/null)
            #http_status_code=`http_post_response_code ${agent_url} "${post_param}"`
            http_status_code=$(curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d "${post_param}" -o /dev/null)
			if [ "${http_status_code}" -eq "200" ];then
				msg "请求成功，响应码是$http_status_code"
                DEPLOY_STATUS=8
                ipCallBack &>/dev/null
                msg "挂流量请求中"
				break
			else
				sleep 5s
                error "服务没有在规定的时间内启动!"
			fi
		done
        if [ "$http_status_code" != "200" ];then
		    result=1
			error "${ip}部署失败"
            DEPLOY_STATUS=9
            ipCallBack &>/dev/null
		elif [ "$http_status_code" == "200" ];then
            local switchNode_on_param=\{\"ip\":\"${ip}\",\"backendPort\":${SERVER_PORT},\"type\":\"ULB\",\"action\":\"ON\"\}
            local on_http_response=$(curl -sL -w "%{http_code}" ${switchNode_url} -H 'Content-Type: application/json'  -d ${switchNode_on_param} )
            local on_http_status_code=`echo ${on_http_response} | awk -F"}" '{print $2}'`
            echo "on_http_response:"${on_http_response}
            echo "on_http_status_code:"${on_http_status_code}
            if test -z "${on_http_status_code}" ;then
                error_exit "挂流量失败, ${on_http_response}" '' 4 9
            fi
            if [ "${on_http_status_code}" -ne "200" ];then
                error_exit "挂流量失败, ${on_http_status_code}" '' 4 9
            else
                local on_response_body=`echo ${on_http_response} | awk -F"}" '{print $1}'`
                local on_response_code=`getValue "${on_response_body}" "code"`
                if test -z "on_response_code" ;then
                    error_exit "挂流量失败, ${on_response_body}" '' 4 9
                fi
                on_response_message=`getValue "${on_response_body}" "message"`
                if [ "${on_response_code}" != "0" ];then
                    error_exit "挂流量失败, ${on_response_message}" '' 4 9
                fi
            fi
            msg "${on_response_message}"
        fi
	done
    if [ "$result" == "1" ];then
        error_exit "发布失败" '' 4 9
    else
		success "发布成功"
	fi
}
#push_jar_server && check_app_status
push_jar_server
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
