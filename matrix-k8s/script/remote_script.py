#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: bing.wei
# Email: weibing@missfresh.cn
from __future__ import print_function, division
import sys

# import here
import commands
import json
import sys
import os

reload(sys)
sys.setdefaultencoding('utf-8')

BIZ_LINES = ["6", "9", "11", "13", "16", "20", "33", "34", "35", "78"]
ENV_LIST = ["b1", "b2", "b3", "b4", "b5"]


def exec_system_cmd(cmd):
    print("执行系统命令: %s" % cmd)
    result = os.system(cmd)
    result_code = result >> 8
    if result_code != 0:
        print("执行命令失败: %s" % cmd)
        sys.exit(1)


def exec_system_cmd_with_feedback(cmd, err_list):
    print("执行系统命令: %s" % cmd)
    result = commands.getstatusoutput(cmd)
    if result[0] != 0:
        print("MEET ERROR %s" % result[1])
        err_list.append(result[1])


def parse_args():
    if len(sys.argv) < 4:
        print("Usage: python {0} biz env tag. E.g. {0} 6 b1 master".format(__file__))
        sys.exit(1)
    else:
        biz = sys.argv[1]
        env = sys.argv[2]
        tag = sys.argv[3]
        if biz not in BIZ_LINES or env not in ENV_LIST:
            print("Wrong args provided!")
            sys.exit(1)
    return biz, env, tag


def get_harbor_token(app_code):
    """
    获取harbor token
    :return:
    """
    user = "weibing"
    password = "ph@nt)M>11"
    harbor_url = "https://harbor.missfresh.net"

    cmd = '''curl -iksL -X GET -u %s:'%s' %s/service/token?account=%s\&service=harbor-registry\&scope=repository:testing/%s:pull|grep "token" |awk -F   '"' '{print $4}' ''' % (
        user, password, harbor_url, user, app_code
    )
    ret = commands.getstatusoutput(cmd)
    if ret[0] == 0:
        return ret[1], harbor_url
    else:
        print("Failed to retrieve token with error code: %d" % ret[0])
        sys.exit(1)


def get_harbor_image_tags(app_code):
    """
    获取指定image的tag列表
    :return:
    """
    token, harbor_url = get_harbor_token(app_code)
    image = "testing/%s" % app_code
    cmd = '''curl -ksL -X GET -H "Content-Type: application/json" -H "Authorization: Bearer %s" %s/v2/%s/tags/list''' % (
        token, harbor_url, image
    )
    ret = commands.getstatusoutput(cmd)
    if ret[0] == 0 and json.loads(ret[1]).get("errors", None) is None:
        data = json.loads(ret[1])
        return data.get("tags", [])
    else:
        print("Failed to retrieve tags of %s with error code: %d and error msg: %s" % (app_code, ret[0], ret[1]))
        return []


def get_current_image(app_code, env):
    """
    获取当前指定app_code在特定测试环境下使用的image
    :param app_code:
    :param env:
    :return:
    """
    shell_cmd = r'''sudo kubectl get pod -n %s -l app='%s' -o jsonpath="{..image}" |tr -s '[[:space:]]' '\n' |sort |uniq |grep %s ''' % (
        env, app_code, app_code
    )
    print(shell_cmd)
    image_name = None
    ret = commands.getstatusoutput(shell_cmd)
    if ret[0] == 0:
        # TODO: 当前使用非master分支，后面需要调整
        images = ret[1].strip().split()
        if len(images) > 1:
            for image_name in images:
                if "master" not in image_name:
                    return image_name
    return image_name


def replace_deploy_yml(new_image_tag, file_path):
    cmd = '''sed -i 's/master/%s/g' %s ''' % (new_image_tag, file_path)
    commands.getstatusoutput(cmd)


def replace_yaml_content(biz_dir, env, tag="CURRENT_TAG"):
    """
    获取app_code当前部署的image
    将image替换到yml文件中，不存在时，替换优先顺序为：current_image > master > latest > last_of_tags
    重新部署 kubectl apply -f
    :param biz_dir:
    :param env:
    :return:
    """
    real_dir = "/tmp/%s" % biz_dir
    apps = {}
    final_apply_apps = []

    # 遍历文件，获取app_code和文件路径
    for root, dirs, files in os.walk(real_dir, topdown=False):
        for name in files:
            base_name, ext = os.path.splitext(name)
            if ext == ".yml":
                apps[base_name] = os.path.join(root, name)

    for app_code, file_path in apps.items():

        # # 更新yml文件将要使用的tag
        # current_image = get_current_image(app_code, env)
        # print("current image: %s" % current_image)
        # if current_image is not None:
        #     current_image_tag = current_image.split(":")[1]
        #     print("存在当前部署的镜像的tag: %s并部署" % current_image_tag)
        #     replace_deploy_yml(current_image_tag, file_path)
        #     final_apply_apps.append(app_code)
        # else:
        #     tags = get_harbor_image_tags(app_code)
        #     if not tags:
        #         print("当前%s没有创建过镜像，跳过替换" % app_code)
        #         continue
        #     # # 优先使用master
        #     if "master" in tags:
        #         print("存在master的tag并部署")
        #         replace_deploy_yml("master", file_path)
        #     # 然后使用latest
        #     elif "latest" in tags:
        #         print("存在latest的tag并部署")
        #         replace_deploy_yml("latest", file_path)
        #     # 最后使用当前最新的一个tag
        #     else:
        #         print("使用last tag部署")
        #         replace_deploy_yml(tags[-1], file_path)

        tags = get_harbor_image_tags(app_code)
        if not tags:
            print("当前%s没有创建过镜像，跳过替换" % app_code)
            continue
        if tag not in tags:
            print("当前%s没有目标TAG %s，跳过替换" % (app_code, tag))
            continue

        print("使用TAG %s部署" % tag)
        replace_deploy_yml(tag, file_path)
        final_apply_apps.append(app_code)

    # 替换完毕后，执行部署
    err_list = []
    for app in final_apply_apps:
        apply_cmd = "sudo kubectl apply -f %s" % os.path.join(real_dir, "%s.yml" % app)
        exec_system_cmd_with_feedback(apply_cmd, err_list)

    if len(err_list):
        for err in err_list:
            exec_system_cmd("echo '%s' >> %s" % (err, os.path.join(real_dir, "error.log")))
        sys.exit(1)


def main():
    biz_line, env, tag = parse_args()  # biz_line也是目录的名称
    replace_yaml_content(biz_line, env, tag)
    sys.exit(0)


if __name__ == '__main__':
    main()
