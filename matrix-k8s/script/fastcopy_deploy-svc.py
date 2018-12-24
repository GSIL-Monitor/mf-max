#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: bing.wei
# Email: weibing@missfresh.cn
from __future__ import print_function, division
import sys
sys.path.append(".")
sys.path.append("../")
import config.Config as Cfg
import os
import shutil
import platform
reload(sys)
sys.setdefaultencoding('utf-8')

"""
用于基于基础模板，生成多套环境的deployment和service的启动yaml文件
"""
NODE_IP = "10.2.4.15"

TMP_DIR = "tmp_deploy-svc"
DEPLOYMENT_DIR = "tpl_deploy-svc"

CONFIRM_ZK_DICT = Cfg.get_zk()
CONFIRM_ENV = CONFIRM_ZK_DICT.keys()


def exec_system_cmd(cmd):
    print("执行系统命令: %s" % cmd)
    result = os.system(cmd)
    result_code = result >> 8
    if result_code != 0:
        print("执行命令失败: %s" % cmd)
        sys.exit(1)


def parse_args():
    if len(sys.argv) < 4:
        print("Usage: python {0} biz beta_env_index tag. E.g. {0} blg b1 master".format(__file__))
        sys.exit(1)
    else:
        biz = sys.argv[1]
        beta_env_index = sys.argv[2]
        tag = sys.argv[3]
        if biz not in os.listdir(DEPLOYMENT_DIR) \
                or beta_env_index not in CONFIRM_ENV:
            print("Wrong args provided!")
            sys.exit(1)
    return biz, beta_env_index, tag


def init_dir(biz):
    if os.path.exists(TMP_DIR):
        shutil.rmtree(TMP_DIR)
    os.makedirs(TMP_DIR)
    shutil.copytree(os.path.join(DEPLOYMENT_DIR, biz), os.path.join(TMP_DIR, biz))
    return os.path.join(TMP_DIR, biz)


def is_mac_system():
    return platform.system() == 'Darwin'


def replace_var(target_folder, beta_env_index):
    """
    方便起见，临时统一修改为latest的tag用于发布最新环境
    :param target_folder:
    :param beta_env_index:
    :return:
    """
    if is_mac_system():
        exec_system_cmd('''for item in `ls {target_folder}`; \
                    do echo {target_folder}/$item \
                    && sed -i '' "s/DEPLOY_ENV/{env}/g" {target_folder}/$item \
                    && sed -i '' "s/ZK_ADDR_ENV/{zk}/g" {target_folder}/$item \
                    ;done'''.format(target_folder=target_folder, zk=CONFIRM_ZK_DICT.get(beta_env_index),
                                    env=beta_env_index))
    else:
        exec_system_cmd('''for item in `ls {target_folder}`; \
                    do echo {target_folder}/$item \
                    && sed -i "s/DEPLOY_ENV/{env}/g" {target_folder}/$item \
                    && sed -i "s/ZK_ADDR_ENV/{zk}/g" {target_folder}/$item \
                    ;done'''.format(target_folder=target_folder, zk=CONFIRM_ZK_DICT.get(beta_env_index),
                                    env=beta_env_index))
    return beta_env_index


def remote_start_new_env(target_folder, biz_line, test_env, tag):
    """
    根据指定的业务线和环境信息，一键生成新的环境
    如果deployment不存在则创建，存在则跳过

    复制deploy-svc目录-单个业务线为最小维度
    推送文件到服务器
    远程执行脚本
    :return:
    """
    shell_script_name_with_path = "script/remote_script.py"
    shell_script_name = "remote_script.py"
    remote_folder = "/tmp/%s" % biz_line

    # 推送到远端的/tmp目录下
    # 重新创建目录
    exec_system_cmd('''ssh www@{node_ip} > /dev/null 2>&1 << EOF
test -d {remote_folder} && rm -rf {remote_folder};
mkdir -p {remote_folder};
EOF'''.format(node_ip=NODE_IP, remote_folder=remote_folder))

    # 推送文件
    exec_system_cmd("scp %s/*.yml www@%s:%s/" % (target_folder, NODE_IP, remote_folder))
    exec_system_cmd("scp %s www@%s:/tmp/" % (shell_script_name_with_path, NODE_IP))
    # 启动
    exec_system_cmd('''ssh www@{node_ip} > /dev/null 2>&1 << EOF
/usr/bin/python /tmp/{script} {biz} {env} {tag}
EOF'''.format(node_ip=NODE_IP, script=shell_script_name, biz=biz_line, env=test_env, tag=tag))


def main():
    biz, test_env, tag = parse_args()
    target_folder = init_dir(biz)
    replace_var(target_folder, test_env)
    remote_start_new_env(target_folder, biz, test_env, tag)
    print("done")


if __name__ == '__main__':
    main()
