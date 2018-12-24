#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: bing.wei
# Email: weibing@missfresh.cn
from __future__ import print_function, division
import sys
import os
import shutil
import platform
reload(sys)
sys.setdefaultencoding('utf-8')

"""
废弃！！！！
用于基于基础模板，生成多套环境的ingress.yml文件
"""
NODE_IP = "10.2.4.15"

CONFIRM_INDEX = range(1, 6)
CONFIRM_ENV = ["b%d" % x for x in range(10)]
TMP_DIR = "tmp_ingress"
INGRESS_DIR = "tpl_ingress"
INGRESS_FILE = "ingress.yml"
INGRESS_LIMIT = 2 # 用于控制ingress文件数量。使用递增的方法增加ingress的svc, 每次增加不得超过100
INGRESS_FILES = ["ingress%02d.yml" % x for x in range(INGRESS_LIMIT)]


def parse_args():
    if len(sys.argv) < 2:
        print("Usage: python {0} beta_env_index. E.g. {0} 1".format(__file__))
        sys.exit(1)
    else:
        beta_env_index = sys.argv[1]
        if str(beta_env_index).lower() == "all" or int(beta_env_index) in CONFIRM_INDEX:
            return sys.argv[1]
        else:
            print("Wrong args provided!")
            sys.exit(1)


def init_dir():
    if os.path.exists(TMP_DIR):
        shutil.rmtree(TMP_DIR)
    shutil.copytree(INGRESS_DIR, TMP_DIR)


def replace_var(beta_env_index):
    if platform.system() == 'Darwin':
        os.system('''for item in `ls {tmp_dir}`; \
                    do echo {tmp_dir}/$item \
                    && sed -i '' "s/DEPLOY_ENV/{env}/g" {tmp_dir}/$item \
                    ;done'''.format(tmp_dir=TMP_DIR, env=CONFIRM_ENV[beta_env_index]))
    else:
        os.system('''for item in `ls {tmp_dir}`; \
                    do echo {tmp_dir}/$item \
                    && sed -i "s/DEPLOY_ENV/{env}/g" {tmp_dir}/$item \
                    ;done'''.format(tmp_dir=TMP_DIR, env=CONFIRM_ENV[beta_env_index]))


def remote_start_or_update_ingress(test_env):
    """
    更新对应环境的Ingress服务配置
    :return:
    """
    # cmd = '''test `sudo kubectl get ingress -n {test_env}|grep "{test_env}"|wc -l` -eq 0 && sudo kubectl create -f /tmp/{ingress} || sudo kubectl apply -f /tmp/{ingress}''' \
    #     .format(test_env=test_env, ingress=INGRESS_FILE)
    # print(cmd)
    # 推送到远端的/tmp目录下
    os.system("scp %s www@%s:/tmp/" % (os.path.join(TMP_DIR, "*.yml"), NODE_IP))
    # 根据顺序逐个执行
    for ingress in INGRESS_FILES:
        os.system('''ssh www@{node_ip} > /dev/null 2>&1 << EOF
sudo kubectl apply -f /tmp/{ingress} && sleep 300
EOF'''.format(node_ip=NODE_IP, ingress=ingress))


def main():
    env_index = parse_args()
    if env_index.lower() == "all":
        for each_idx in CONFIRM_INDEX:
            init_dir()
            replace_var(each_idx)
            remote_start_or_update_ingress(CONFIRM_ENV[each_idx])
    else:
        env_index = int(env_index)
        init_dir()
        replace_var(env_index)
        remote_start_or_update_ingress(CONFIRM_ENV[env_index])
    print("done")


if __name__ == '__main__':
    main()