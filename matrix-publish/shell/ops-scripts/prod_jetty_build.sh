#!/bin/bash
source /etc/profile
msg() {
    printf '%b\n' "$1" >&2
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
  #exec_cmd "git merge --abort"
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

    if [ "$build_branch" == "master" ]; then
        echo "【$build_branch】无需merge检查"
        return 0
    fi
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
#		beta_master_merge_to_branch
        error_exit "请将主干上的更改合并至分支【$build_branch】再执行发布"
	else
		success "已经合并"
	fi
}


function beta_master_merge_to_branch(){
    tag_type=${build_branch:0:2}
    if [ $tag_type == "b-" ]; then
    msg "合并 主干 代码到 分支 上.."
	exec_cmd "git checkout ${build_branch}"
	exec_cmd "git pull"
	exec_cmd "git merge --no-ff tags/master"
	exec_cmd "git push origin HEAD:${build_branch}"
	success "合并主干代码到分支上成功！！"
    fi
}



function prod_branch_merge_to_master(){
    tag_type=${build_branch:0:2}
    if [ "$build_branch" == "master" ]; then
        echo "【$build_branch】无需代码合并！！"
        return 0
    fi
    if [ $tag_type == "b-" ]; then
        msg "合并分支代码到主干上.."
	exec_cmd "git checkout master"
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

    if [ "$tag_type" == "master" ]; then
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
     if [ "$build_branch" == "master" ]; then
        echo "【$build_branch】无需生成btag"
        return 0
    fi
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

rtag_name=$(date +"r-%Y%m%d-%H%M%S-$BUILD_USER_ID")

function prod_git_push_rtag()
{
#    rtag_name=$(date +"r-%Y%m%d-%H%M%S-$BUILD_USER_ID")
    tag_type=${build_branch:0:2}
     if [ "$build_branch" == "master" ]; then
        echo "【$build_branch】无需处理"
        return 0
    fi
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
   if test -z  ${group} ;then
      group=""
   fi
   if test -z  ${mvn_ops} ;then
      exec_cmd "mvn clean deploy -Dmaven.test.skip=true -P ${build_type}${group} -f $PROJECT_POM "
   else
      exec_cmd "mvn clean deploy -Dmaven.test.skip=true -f $PROJECT_POM ${mvn_ops}"
   fi
}

#
source_path="$WORKSPACE$deploy_path$jar_name"
#

function backup()
{
    backup_file_path=/data/backup/${jar_name}
    if [ ! -d "$backup_file_path" ]; then
       exec_cmd "mkdir $backup_file_path"
    fi
    exec_cmd "cp ${source_path} ${backup_file_path}/$rtag_name.war"
}

function rollback()
{
   backup_file_path=/data/backup/${jar_name}
   if [ ! -f "${backup_file_path}/${build_branch}.war" ];then
       mvn_build
   fi
#   exec_cmd "source_path='${backup_file_path}/${build_branch}.jar'"
   echo "source_path='${backup_file_path}/${build_branch}.war'"
   source_path="${backup_file_path}/${build_branch}.war"
}

# master分支代码回滚
function rollback_master_branch()
{
    backup_branch_name=$(date +"k_%Y%m%d_${build_branch}")
    exec_cmd "git fetch"
    exec_cmd "git checkout -b ${backup_branch_name} ${build_branch}"
    echo "开始回滚master代码为Tag：${build_branch}"
    exec_cmd "git push origin ${backup_branch_name}:master -f"
#    exec_cmd "git reset --hard ${build_branch}"
}

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
     #rollback
     mvn_build
  else
     success "执行新分支发布.."

     beta_prod_master_merged_check
     prod_branch_merge_to_master
     prod_git_push_rtag
     success "git发布完成!!"
     mvn_build
     if [ "$build_branch" != "master" ]; then
        echo "【$build_branch】开始备份！！"
        backup
     fi

  fi
fi


function curl_live()
{
   count=1
   for i in $(seq 1 20):
   do
    msg "探活的地址为:$1"
    #curl 请求
    http_status_code=$(curl -sL -w "%{http_code}" $1 -o /dev/null)
    #对响应码进行判断
    if [ "$http_status_code" == "200" ];then
        echo "请求成功，响应码是$http_status_code"
        count=0
        break
    else
        sleep 5s
        echo "服务没有在规定的时间内启动!"
    fi
   done
   if [ "$count" -eq 1 ];then
       error_exit "服务没有在规定的时间内启动!"
   fi

}

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
  /usr/bin/rsync -avz -e ssh ${source_path} "www@${ip}:/data/${jar_name}"
   msg "/usr/bin/rsync -avz -e ssh ${source_path} 'www@${ip}:/data/${jar_name}'"
  /usr/bin/ssh  www@${ip} "sh /data/start.sh"
  msg "来鞭挞我吧!准备撸一发!"
  url="http://${ip}:${health_check}"
  curl_live $url
  msg "启动成功!sleep 5s"
  sleep 5s
  done
}
push_jar_server
