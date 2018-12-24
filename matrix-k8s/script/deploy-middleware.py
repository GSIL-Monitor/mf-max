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
用于部署中间件，中间件位于tpl_deploy-svc/middleware目录下
"""
NODE_IP = "10.2.4.15"

CONFIRM_ZK = Cfg.get_zk()
TMP_DIR = "tmp_deploy-svc"
DEPLOYMENT_DIR = "tpl_deploy-svc"
MIDDLEWARE_DIR = "middleware"
CONFIRM_MIDDLEWARE = ["zk", "redis", "filebeat_configmap", "rabbitmq", "rocketmq-4.1.0-incubating", "rocketmq-4.3.0", "openresty-proxy"]


def parse_args():
    if len(sys.argv) < 4:
        print("Usage: python {0} beta_env middleware is_apply E.g. {0} b1 zookeeper false".format(__file__))
        sys.exit(1)
    else:
        biz = MIDDLEWARE_DIR
        beta_env = sys.argv[1]
        middleware = sys.argv[2]
        is_apply = sys.argv[3]
        if beta_env != 'all' and beta_env not in Cfg.get_env() \
                or middleware not in CONFIRM_MIDDLEWARE \
                or is_apply not in ["true", "false"]:
            print("Wrong args provided!")
            sys.exit(1)
        is_apply = True if is_apply == "true" else False
    return biz, beta_env, middleware, is_apply


def init_dir(biz, middleware):
    if os.path.exists(TMP_DIR):
        shutil.rmtree(TMP_DIR)
    target_dir = os.path.join(TMP_DIR, middleware)
    os.makedirs(TMP_DIR)
    shutil.copytree(os.path.join(DEPLOYMENT_DIR, biz, middleware), target_dir)
    print(target_dir)
    print(os.listdir(target_dir))
    return target_dir


def replace_var(target_folder, beta_env):
    if platform.system() == 'Darwin':
        os.system('''for item in `ls {target_folder} |grep yml`; \
                    do echo {target_folder}/$item \
                    && sed -i '' "s/DEPLOY_ENV/{env}/g" {target_folder}/$item \
                    ;done'''.format(target_folder=target_folder, zk=CONFIRM_ZK.get(beta_env),
                                    env=beta_env))
    else:
        os.system('''for item in `ls {target_folder} |grep yml`; \
                    do echo {target_folder}/$item \
                    && sed -i "s/DEPLOY_ENV/{env}/g" {target_folder}/$item \
                    ;done'''.format(target_folder=target_folder, zk=CONFIRM_ZK.get(beta_env),
                                    env=beta_env))
    return beta_env


def remote_start_new_env(target_folder, biz_line, middleware, test_env, is_apply_master):
    """
    根据指定的业务线和环境信息，一键生成新的环境
    如果deployment不存在则创建，存在则更新
    :return:
    """
    remote_folder = "/tmp/%s" % biz_line

    shell_script = """sudo kubectl apply -f {remote_folder}""".format(remote_folder=remote_folder)


    # 推送到远端的/tmp目录下
    # 创建目录
    os.system('''ssh www@10.2.4.15 > /dev/null 2>&1 << EOF
test -d {remote_folder} && rm -rf {remote_folder};
mkdir -p {remote_folder};
EOF'''.format(remote_folder=remote_folder))

    # 推送文件
    os.system("scp %s/*.yml www@%s:%s/" % (target_folder,NODE_IP, remote_folder))
    # 启动
    os.system('''ssh www@{node_ip} {shell_script}'''.format(node_ip=NODE_IP, shell_script=shell_script))


def one_loop(biz, middleware, beta_env, apply):
    target_folder = init_dir(biz, middleware)
    replace_var(target_folder, beta_env)
    remote_start_new_env(target_folder, biz, middleware, beta_env, apply)
    print("%s done" % beta_env)


def main():
    biz, beta_env, middleware, apply = parse_args()
    if beta_env == 'all':
        for item in Cfg.get_env():
            one_loop(biz, middleware, item, apply)
    else:
        one_loop(biz, middleware, beta_env, apply)


if __name__ == '__main__':
    main()