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
        if [ "${build_type}" == "release" ];then
                if test -z ${MAVEN_PATH} ;then
                    if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} " 14 14
                    else
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} ${mvn_ops}" 14 14
                    fi
                else
                    if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} -s ${MAVEN_PATH}" 14 14
                    else
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} -s ${MAVEN_PATH} ${mvn_ops}" 14 14
                    fi
                fi
                DEPLOY_STATUS=13
                releaseCallBack &>/dev/null
        else
                if test -z ${MAVEN_PATH} ;then
                    if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} " 14 14
                    else
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} ${mvn_ops}" 14 14
                    fi
                else
                    if test -z  ${mvn_ops} ;then
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} -s ${MAVEN_PATH}" 14 14
                    else
                        exec_cmd "mvn clean deploy  -Dmaven.test.skip=true -P ${GROUP} -f ${PROJECT_POM} -s ${MAVEN_PATH} ${mvn_ops}" 14 14
                    fi
                fi
                DEPLOY_STATUS=13
                betaCallBack &>/dev/null
        fi

}
# jar包路径
#source_path="${WORKSPACE}${deploy_path}${jar_name}"
#

function backup(){
        backup_file_path=/data/backup/${jar_name}
        if [ ! -d "${backup_file_path}" ]; then
                exec_cmd "mkdir ${backup_file_path}" 4 9
        fi
        exec_cmd "cp ${source_path} ${backup_file_path}/${rtag_name}-${GROUP}.jar" 4 9
}
function rollback(){
        backup_file_path=/data/backup/${jar_name}
        if [ ! -f "${backup_file_path}/${build_branch}-${GROUP}.jar" ];then
                mvn_build
                return 0
        fi
        echo "source_path='${backup_file_path}/${build_branch}-${GROUP}.jar'"
        source_path="${backup_file_path}/${build_branch}-${GROUP}.jar"
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
echo "PUBLISH_RECORD:"${PUBLISH_RECOR}D
APP_TYPE=`getValue "${STRING}" "appType"`
echo "APP_TYPE:"${APP_TYPE}
BATCH_RECORD=`getInteger "${STRING}" "batch"`
echo "BATCH_RECORD:"${BATCH_RECORD}
WAIT_TIME=`getInteger "${STRING}" "waitTime"`
echo "WAIT_TIME:"$WAIT_TIME
PLAN_RECORD=`getInteger "${STRING}" "plan"`
echo "PLAN_RECORD:"${PLAN_RECORD}
DEPLOY_STATUS=
TAG=
MAVEN_PATH=`getValue "${STRING}" "mavenPath"`
echo "MAVEN_PATH:"${MAVEN_PATH}

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
            mvn_build
            prod_branch_merge_to_master
            prod_git_push_rtag
            success "git发布完成!"
        fi
    fi
fi

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