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
import requests
import json
import time


reload(sys)
sys.setdefaultencoding('utf-8')


"""
基于应用中心的数据，根据应用，获取相关的编译参数，完成编译工作
"""
NODE_IP = "10.2.4.15"
CONFIRM_ZK = Cfg.get_zk()
STATIC_PURE_PROJ = [ # 跳过npm编译的工程
    "base-missconf-fe",
]
APP_CODE = "app_code"
GIT_URL = "git"
BUILD_TYPE = "pkgType"
TARGET_NAME = "pkgName"  # 包名
MVN_PATH = "mvnPath"
TARGET_DIR = "dubboWorkDir"  # gz解压后的包名
DEPLOY_DATA = "deployPath_pkgName"  # 最终发布的的data
DOCKER_FILE_TEMPLATE = "../tpl_dockerfile/%s" # %s需要替换为对应的builg_type, 例如jar, tomcat等
TPL_DEPLOY_SVC_DIR = "tpl_deploy-svc"
TMP_DIR = "tmp_project"
BUILD_TYPE_METHOD = {"jar": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
                     # "jetty": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
                     "tomcat": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
                     "spring_gz": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
                     "go": "",
                     # "python": "",
                     "static": "npm install && npm run build",
                     }


def parse_args():
    if len(sys.argv) < 6:
        print("Usage: python {0} test_env app_code biz_line branch is_init. E.g. {0} b1 thor 11 master false".format(__file__))
        sys.exit(1)
    # test_env = sys.argv[1] # b1
    # app_code = sys.argv[2] # thor
    # biz_line = sys.argv[3] # blg
    # branch = sys.argv[4]   # master
    # is_init = sys.arv[5] # true/false Optional

    return sys.argv[1:]


def retrieve_app_info(app_code):
    """
    该方法已经过滤了数据，仅返回必须的数据信息
    appcenter.missfresh.net/openapi/app/getAppInfoByAppCode
    {
      "appCode": "app_code"，
      "envCode":"prod"
    }
    :param app_code:
    :return:
    """
    ### helloworld 测试方法 命中时跳过应用中心 ###
    if app_code == "helloworld":
        app_info = {APP_CODE: "helloworld", GIT_URL: "git@git.missfresh.cn:weibing/docker-project-demo.git",
                    BUILD_TYPE:"jar", TARGET_NAME: "spb-test.jar", DEPLOY_DATA: "target/spb-test.jar"}
        return app_info
    ##########################################

    app_info = {}
    payload = {"appCode": app_code, "envCode": "prod"} # 考虑使用prod
    # url = r'http://appcenter.missfresh.net/openapi/app/getAppInfoByAppCode'
    url = r'http://10.3.39.15:8888/openapi/app/getAppInfoByAppCode'
    result = requests.post(url, json=payload)
    if result.status_code == 200:
        j_data = json.loads(result.content).get("data")
        app_info = parse_app_info(j_data)
    else:
        print(result.text)
        print("从应用中心获取数据出错，请先在应用中心注册应用后使用")
        sys.exit(1)

    return app_info


def valid_app_info(app_info):
    """
    检查必要的参数，缺一不可
    :param app_info:
    :return:
    """
    for k, v in app_info.items():
        if not len(v.strip()):
            print("应用中心的prod配置缺少必要的参数【%s】,请更新后重试" % k)
            sys.exit(1)


def parse_app_info(app_raw_data):
    """
    pkgType
    :param app_raw_data:
    :return:
    """
    app_info = dict()
    app_info[APP_CODE] = app_raw_data.get("appCode")
    # 卑劣的操作，将-替换为_，为了兼容K8s, 日乐购
    app_info[APP_CODE] = app_raw_data.get("appCode").replace("_", "-")
    app_info[GIT_URL] = app_raw_data.get("git")
    app_build_type = app_raw_data.get("pkgType")
    # app_build_type[MVN_PATH] = app_raw_data.get("mavenPath", "").strip()
    app_info[BUILD_TYPE] = app_build_type
    if app_build_type in ["jar", "tomcat", "spring_gz"]: #TODO: 增加类型需要修改这里
        pure_path = app_raw_data.get("deployPath").strip().strip("/")
        app_info[TARGET_NAME] = app_raw_data.get("pkgName").strip()
        tmp_split_names = app_info[TARGET_NAME].split(".")
        if len(tmp_split_names) > 0:
            app_info[TARGET_DIR] = tmp_split_names[0]
        app_info[DEPLOY_DATA] = '%s/%s' % (pure_path, app_raw_data.get("pkgName"))
    elif "jetty" == app_build_type:
        pass
    ## TODO: more types

    valid_app_info(app_info)
    return app_info


def load_dockerfile_template(build_type):
    with open(DOCKER_FILE_TEMPLATE % build_type, "r") as f:
        return f.read()


def create_docker_file(app_info):
    """
    这里后面可能需要针对不同的build_type进行区分，目前统一处理
    :param app_info:
    :param dockerfile_tpl:
    :return:
    """
    if os.path.exists("Dockerfile"):
        return

    if app_info.get(BUILD_TYPE) == "static":
        # 需要拉取Dockerfile, start.sh文件
        docker_file_path = DOCKER_FILE_TEMPLATE % r"static/Dockerfile"
        start_script_path = DOCKER_FILE_TEMPLATE % r"static/start.sh"
        os.system("cp %s ./" % docker_file_path)
        os.system("cp %s ./" % start_script_path)
    else:
        docker_file_tpl = load_dockerfile_template(app_info.get(BUILD_TYPE))
        content = docker_file_tpl.replace("PROJECT_HOME", app_info.get(APP_CODE))\
            .replace("TARGET_DATA", app_info.get(DEPLOY_DATA))\
            .replace("TARGET_NAME", app_info.get(TARGET_NAME))\
            .replace("TARGET_DIR", app_info.get(TARGET_DIR, ''))

        # TODO: 更新dockerfile

        with open("Dockerfile", "w") as f:
            f.write(content)


def create_docker_image(app_code, tag):
    """
    全部保存到testing项目下，app_code, tag一般为timestamp_branch
    :param app_code:
    :param tag:
    :return:
    """
    image = "harbor.missfresh.net/testing/{app_code}:{tag}".format(app_code=app_code, tag=tag)
    os.system("sudo /usr/bin/docker build -t %s ." % image)
    return image


def push_docker_image(image):
    '''
    push 之后，需要在本地清除image，避免磁盘被填满
    harbor.missfresh.net/testing/$app_name:v$BUILD_NUMBER_$app_branch
    :return:
    '''
    old_tag = image.split(":")[1]
    image_with_latest = image.replace(old_tag, "latest")
    os.system("sudo /usr/bin/docker tag %s %s" % (image, image_with_latest))
    print(image_with_latest)

    os.system("sudo /usr/bin/docker push %s" % image)
    os.system("sudo /usr/bin/docker push %s" % image_with_latest)
    os.system("sudo /usr/bin/docker rmi -f %s" % image)
    os.system("sudo /usr/bin/docker rmi -f %s" % image_with_latest)


def prepare_image(app_info, branch, is_init):
    """
    完成编译，镜像打包上传的步骤
    :param app_info:
    :param branch:
    :param dockerfile_tpl:
    :param is_init:
    :return:
    """
    # 记录当前位置
    old_path = os.getcwd()

    git_url = app_info.get(GIT_URL)
    build_type = app_info.get(BUILD_TYPE)

    # clone代码以及切换分支
    os.system("git clone --depth=1 -b {branch} {git_url}".format(branch=branch, git_url=git_url))

    # 执行编译
    project_dir = os.path.split(git_url)[-1].split(".")[0]
    os.chdir(project_dir)
    build_method = BUILD_TYPE_METHOD.get(build_type)

    # 静态工程不需要额外的参数
    if app_info.get(BUILD_TYPE) not in ['static', 'go']:
        mvn_path = '/home/www/.m2/mryx/settings.xml'
        if app_info.get(APP_CODE) in ["wuliu-ocean-wms-report-impl",]:
            build_method = "%s -s %s" % (build_method, mvn_path)

    # 纯静态代码，不需要编译的则跳过
    if app_info.get(APP_CODE) not in STATIC_PURE_PROJ:
        os.system(build_method)

    # 打镜像包
    create_docker_file(app_info)
    if is_init:
        image_tag = "master"
    else:
        image_tag = "%s_%d" % (branch, time.time())
    image_name = create_docker_image(app_info.get(APP_CODE), image_tag)
    # 上传镜像
    push_docker_image(image_name)
    # 返回之前的目录
    os.chdir(old_path)

    if is_init: # 如果首次，仅做上传操作，push之后返回
        print("初始化完成，已上传初始化基础镜像")
        sys.exit(0)
    return image_name


def remote_start_or_update_deployment(image_name, app_code, biz_line, test_env):
    """
    判断是否存在deployment文件，存在则使用其启动并更新
    :return:
    """
    # 判断是否存在对应的deployment文件
    file_path = os.path.join(TPL_DEPLOY_SVC_DIR, biz_line, "%s.yml" % app_code)
    if not os.path.exists(file_path):
        print("未创建相关的deployment.yml文件，请联系weibing, juqing配合创建")
        sys.exit(1)

    # 更新文件的环境信息，以及image信息
    deploy_file = "%s_deployment.yml" % app_code
    with open(file_path, "r") as f:
        raw_content = f.read()
        content = raw_content.replace("DEPLOY_ENV", test_env)\
            .replace("ZK_ADDR_ENV", CONFIRM_ZK.get(test_env))\
            .replace("harbor.missfresh.net/testing/%s:master"%app_code, image_name)
        with open(deploy_file, "w") as fw:
            fw.write(content)
    # 推送到远端的/tmp目录下
    os.system("scp %s www@%s:/tmp/" % (deploy_file, NODE_IP))
    # 判断是否有deployment存活，没有的话，create；有的话, apply
    os.system('''ssh www@{node_ip} > /dev/null 2>&1 << EOF
test `sudo kubectl get pod -n {test_env}|grep "{app_name}"|wc -l` -eq 0 && sudo kubectl create -f /tmp/{deployment} || sudo kubectl apply -f /tmp/{deployment}
EOF
    '''.format(node_ip=NODE_IP, test_env=test_env, app_name=app_code, deployment=deploy_file))

    # 检查是否启动成功


def main():
    """
    主流程
    :param is_init: 当为True的时候，表示首次上传镜像，不管用户使用什么分支，都需要上传一份master tag的镜像到harbor
    :return:
    """
    test_env, app_code, biz_line, branch, is_init = parse_args()
    is_init = False if is_init == "false" else True

    app_info = retrieve_app_info(app_code)
    image_name = prepare_image(app_info, branch, is_init)
    # 远程服务启动 - 不存在则启动，存在则更新镜像
    remote_start_or_update_deployment(image_name, app_info.get(APP_CODE), biz_line, test_env)
    print("done")


if __name__ == '__main__':
    main()
