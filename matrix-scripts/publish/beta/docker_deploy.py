#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: bing.wei
# Email: weibing@missfresh.cn
from __future__ import print_function, division
import sys
import os
import requests
import shutil
import json
import time
import logging
import hashlib
import zipfile
import socket
# from requests import ConnectionError

reload(sys)
sys.setdefaultencoding('utf-8')

"""
基于应用中心的数据，根据应用，获取相关的编译参数，完成编译工作
"""

# TODO: 考虑使用内置的svc提供服务，这样外部通过域名访问
CONFIRM_ZK = {
    "b1": "zk-0.b1:2181,zk-1.b1:2181,zk-2.b1:2181",
    "b2": "zk-0.b2:2181,zk-1.b2:2181,zk-2.b2:2181",
    "b3": "zk-0.b3:2181,zk-1.b3:2181,zk-2.b3:2181",
    "b4": "zk-0.b4:2181,zk-1.b4:2181,zk-2.b4:2181",
    "b5": "zk-0.b5:2181,zk-1.b5:2181,zk-2.b5:2181",
    "b6": "zk-0.b6:2181,zk-1.b6:2181,zk-2.b6:2181",
    "b7": "zk-0.b7:2181,zk-1.b7:2181,zk-2.b7:2181",
    "b8": "zk-0.b8:2181,zk-1.b8:2181,zk-2.b8:2181",
    "b9": "zk-0.b9:2181,zk-1.b9:2181,zk-2.b9:2181",
    "b10": "zk-0.b10:2181,zk-1.b10:2181,zk-2.b10:2181",
    # TODO: 扩展
}

ES_INDEX = {
    "b1": '50167f90-fcfc-11e8-8c9b-211cd11b5e2b',
    "b2": '214568f0-fd53-11e8-8c9b-211cd11b5e2b',
    "b3": '29284ce0-fd53-11e8-8c9b-211cd11b5e2b',
    "b4": '315f6b50-fd53-11e8-8c9b-211cd11b5e2b',
    "b5": 'b908e180-fd53-11e8-8c9b-211cd11b5e2b',
    # TODO: 扩展
}


NODE_IP = "10.2.4.15"

STATIC_PURE_PROJ = [  # 跳过npm编译的工程
    "base-missconf-fe",
]
STATIC_WULIU_SPEC = [ # 物流使用私有源的工程
    'wuliu-eam-fe',
    'wuliu-cloud-fe',
    'wuliu-sms-fe',
    'wuliu-tms-fe',
]
TOMCAT_BUILD_OPT_SPEC = [ # 需要制定使用 -Devn==autotest
    'mryx-fms-due-worker',
]

K8S_TPL_DIR_NAME = "matrix-k8s"
K8S_TPL_GIT = "git@git.missfresh.cn:matrix/matrix-k8s.git"
CHECK_LIMIT = 30  # healthcheck最大检查次数，间隔轮训一秒钟
DEPLOY_SUCCESS = 3
DEPLOY_FAIL = 4

PUBLISH_RECORD = "publish_record"
BATCH_RECORD = "batch_record"
DEPLOY_STATUS = "deploy_status"
PLAN_RECORD = "plan_record"

B_TAG = "btag"
APP_CODE = "app_code"
DOCKER_ENV = "docker_env"
BIZ_LINE = "biz_line"
GIT_URL = "git"
GIT_BRANCH = "branch"
WORKSPACE = "workspace"
APP_TYPE = "pkgType"
MVN_OPS = "mvnOps"
MVN_PATH = "mavenPath"
HEALTH_CHECK = "healthCheck"
BUILD_TYPE = "buildType"  # 测试，灰度或者线上发布类型
TARGET_NAME = "pkgName"  # 包名
COMPILED_PATH = "compiled_path"  #
PROJECT_TASK_ID = "projectTaskId"  # 包名
TARGET_DIR = "spring_gz_dir"  # spring_gz部署解压后的目录名,与压缩包的文件名相同
BUILD_USER = "user"  # 包名
DEPLOY_DATA = "deployPath_pkgName"  # 最终发布的的data
DOCKER_FILE_TEMPLATE = K8S_TPL_DIR_NAME + "/tpl_dockerfile/%s"  # %s需要替换为对应的app_type, 例如jar, tomcat等
TPL_DEPLOY_SVC_DIR = os.path.join(K8S_TPL_DIR_NAME, "tpl_deploy-svc")
TMP_DIR = "tmp_project"
BUILD_TYPE_METHOD = {
    "jar": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
    "jar-main": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
     "tomcat": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
     "spring_gz": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
     # "jetty": "mvn clean package -Dmaven.test.skip=true -B -e -U -P autotest",
     "go": "",
     # "python": "",
     # TODO: static环境略微复杂，目前对接的都是npm编译的，会自动生成dist目录。对于不需要编译，以及其他编译方式的，需要再适配
     "static": "npm install && npm run build",
     "static-wuliu-spec": "npm install --registry=http://10.2.4.224:4873 && npm run build",
                     }
PARAM = dict()


def get_logger():
    logger = logging.getLogger(__file__)
    if not logger.handlers:
        FORMAT = '[%(asctime)-15s] %(levelname)-6s line-no:%(lineno)d %(message)s'
        logging.basicConfig(level=logging.INFO, format=FORMAT)
    return logger


LOG = get_logger()


def is_release():
    """
    检查当前环境是否线上
    :return:
    """
    release_ip = ['10.3.39.3', '10.3.39.2', '10.3.39.9']
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('8.8.8.8', 80))
        current_ip = s.getsockname()[0]
    finally:
        s.close()
    return current_ip in release_ip


def md5sum(filename):
    if not os.path.exists(filename):
        error_exit("%s not existed!" % filename)
    with open(filename, "r") as f:
        fcont = f.read()
    fmd5 = hashlib.md5(fcont).hexdigest()
    return fmd5


def exec_system_cmd(cmd):
    LOG.info("执行系统命令: %s" % cmd)
    result = os.system(cmd)
    result_code = result >> 8
    if result_code != 0:
        error_exit("执行命令失败: %s" % cmd)


def error_exit(msg):
    """
    打印错误信息
    回调，传异常结束的状态
    退出
    :param msg:
    :return:
    """
    LOG.error(msg)
    callback_publish(PARAM, DEPLOY_FAIL)
    sys.exit(1)


def parse_args(params=PARAM):
    """
    argv: json参数列表, workspace
    :return:
    """
    j_data = json.loads(sys.argv[1].strip())
    workspace = sys.argv[2].strip()

    # 不是那么重要的参数
    params[PUBLISH_RECORD] = j_data.get("record")
    params[BATCH_RECORD] = j_data.get("batch")
    params[DEPLOY_STATUS] = DEPLOY_SUCCESS
    params[PLAN_RECORD] = ""

    # 重要参数
    params[DOCKER_ENV] = j_data.get("dockerEnv")
    params[APP_CODE] = j_data.get("appCode")
    params[BIZ_LINE] = j_data.get("bizLine")
    params[GIT_BRANCH] = j_data.get("buildBranch")
    params[APP_TYPE] = j_data.get("appType")
    params[BUILD_TYPE] = j_data.get("buildType")
    params[BUILD_USER] = j_data.get("user")
    params[MVN_OPS] = j_data.get("mvnOps", "").strip()
    params[MVN_PATH] = j_data.get("mavenPath", "").strip()
    params[HEALTH_CHECK] = j_data.get("healthCheck", "").strip()
    params[PROJECT_TASK_ID] = j_data.get("projectTaskId", -1)
    if params[APP_TYPE] in BUILD_TYPE_METHOD.keys():
        pure_path = j_data.get("deployPath", "").strip().strip("/")
        params[COMPILED_PATH] = pure_path
        params[TARGET_NAME] = j_data.get("jarName", "").strip()
        tmp_split_of_target_name = params[TARGET_NAME].split(".")
        if len(tmp_split_of_target_name) > 1:
            params[TARGET_DIR] = tmp_split_of_target_name[0]
        params[DEPLOY_DATA] = '%s/%s' % (pure_path, params[TARGET_NAME])
    else:
        error_exit("NOTICE: 目前仅支持%s编译部署Docker环境，其他类型请稍后" % json.dumps(BUILD_TYPE_METHOD.keys()))
    params[WORKSPACE] = workspace
    valid_app_info(params)
    LOG.info("获取到的参数: %s" % json.dumps(params))
    return params


def valid_app_info(app_info):
    """
    检查必要的参数，缺一不可
    :param app_info:
    :return:
    """
    if app_info.get(APP_TYPE) == 'static':
        return
    for k, v in app_info.items():
        if not len(str(v).strip()) and k not in [MVN_PATH, MVN_OPS, HEALTH_CHECK, PLAN_RECORD]:
            error_exit("应用中心的prod配置缺少必要的参数【%s】,请更新后重试" % k)


def push_tag(app_info):
    """
    如果分支不是tag或者master， 则创建tag并推送
    :return:
    """
    user = app_info.get(BUILD_USER)
    branch = app_info.get(GIT_BRANCH)

    btag = "b-%s-%s" % (time.strftime("%Y%m%d-%H%M%S"), user)
    tag_type = branch[:2]
    if tag_type not in ["b-", "r-"] or branch != "master":
        exec_system_cmd("git tag %s" % btag)
        exec_system_cmd("git push origin %s" % btag)
        LOG.info("生成tag: %s" % ("=" * 20))
        LOG.info("生成tag: %s" % btag)
        LOG.info("生成tag: %s" % ("=" * 20))
    else:
        btag = branch
        LOG.info("不需要重新生成tag")

    app_info[B_TAG] = btag


def prepare_image(params):
    """
    完成编译，镜像打包上传的步骤
    :return:
    """
    # clone模板代码
    exec_system_cmd("git clone --depth=1 -b master {git_url}".format(git_url=K8S_TPL_GIT))

    # 生成btag
    push_tag(params)

    branch = params.get(GIT_BRANCH)
    build_type = params.get(APP_TYPE)
    mvn_ops = params.get(MVN_OPS)
    mvn_path = params.get(MVN_PATH)

    # 执行编译
    build_method = BUILD_TYPE_METHOD.get(build_type)

    # 静态工程不需要额外的参数
    if params.get(APP_TYPE) not in ['static', 'go'] and params.get(APP_CODE) not in TOMCAT_BUILD_OPT_SPEC:
        if mvn_path:
            build_method = "%s -s %s" % (build_method, mvn_path)
        if mvn_ops:
            build_method = "%s %s" % (build_method, mvn_ops)

    # 对于交易中台的几个工程，需要使用指定编译参数
    if params.get(APP_CODE) in TOMCAT_BUILD_OPT_SPEC:
        build_method = "%s %s" % (build_method, '-Devn=autotest')

    # 对于物流的几个使用私有源的工程，特殊处理
    if params.get(APP_CODE) in STATIC_WULIU_SPEC:
        build_method = BUILD_TYPE_METHOD.get("static-wuliu-spec")

    # 纯静态代码，不需要编译的则跳过
    if params.get(APP_CODE) not in STATIC_PURE_PROJ:
        exec_system_cmd(build_method)

    # 打镜像包
    create_docker_file(params)
    image_tag = "%s_%d" % (branch, time.time())
    image_name, image_latest = create_docker_image(params.get(APP_CODE), image_tag)
    # 上传镜像
    push_docker_image(image_name, image_latest)

    return image_name


def create_docker_file(app_info):
    """
    这里后面可能需要针对不同的build_type进行区分，目前统一处理
    :param app_info:
    :param dockerfile_tpl:
    :return:
    """
    if os.path.exists("Dockerfile"):
        return

    if app_info.get(APP_TYPE) == "static":
        # 需要拉取Dockerfile, start.sh文件
        docker_file_path = DOCKER_FILE_TEMPLATE % r"static/Dockerfile"
        start_script_path = DOCKER_FILE_TEMPLATE % r"static/start.sh"
        exec_system_cmd("cp %s ./" % docker_file_path)
        exec_system_cmd("cp %s ./" % start_script_path)
    else:
        docker_file_path = DOCKER_FILE_TEMPLATE % app_info.get(APP_TYPE)
        # 加载Dockerfile模板
        with open(docker_file_path, "r") as f:
            dockerfile_tpl = f.read()

        content = dockerfile_tpl.replace("PROJECT_HOME", app_info.get(APP_CODE)) \
            .replace("TARGET_DATA", app_info.get(DEPLOY_DATA)) \
            .replace("TARGET_NAME", app_info.get(TARGET_NAME)) \
            .replace("TARGET_DIR", app_info.get(TARGET_DIR, '')) \
            .replace("TARGET_PATH", app_info.get(COMPILED_PATH, ''))
        # TODO: 更多Dockerfile中的变量

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
    exec_system_cmd("sudo /usr/bin/docker build -t %s ." % image)
    image_latest = "harbor.missfresh.net/testing/{app_code}:{tag}".format(app_code=app_code, tag="latest")
    exec_system_cmd("sudo /usr/bin/docker tag %s %s" % (image, image_latest))
    return image, image_latest


def push_docker_image(image, image_latest):
    """
    push 之后，需要在本地清除image，避免磁盘被填满
    harbor.missfresh.net/testing/$app_name:v$BUILD_NUMBER_$app_branch

    image: 镜像信息
    :return:
    """
    # 同时上传最新的latest tag
    exec_system_cmd("sudo /usr/bin/docker push %s" % image)
    exec_system_cmd("sudo /usr/bin/docker push %s" % image_latest)

    exec_system_cmd("sudo /usr/bin/docker rmi -f %s" % image)
    exec_system_cmd("sudo /usr/bin/docker rmi -f %s" % image_latest)


def generate_yml(image_name, app_info):
    """

    :param image_name:
    :param app_info:
    :return:
    """
    app_code = app_info.get(APP_CODE)
    biz_line = app_info.get(BIZ_LINE)
    test_env = app_info.get(DOCKER_ENV)

    # 判断是否存在对应的deployment文件
    file_path = os.path.join(TPL_DEPLOY_SVC_DIR, biz_line, "%s.yml" % app_code)
    if not os.path.exists(file_path):
        error_exit("未创建相关的deployment.yml文件，请联系weibing, juqing配合创建")

    # 更新文件的环境信息，以及image信息
    deploy_file = "%s_%s_deploy.yml" % (app_code, test_env)
    with open(file_path, "r") as f:
        raw_content = f.read()
        content = raw_content \
            .replace("DEPLOY_ENV", test_env) \
            .replace("ZK_ADDR_ENV", CONFIRM_ZK.get(test_env)) \
            .replace("harbor.missfresh.net/testing/%s:master" % app_code, image_name)
        # TODO: 更多特异变量替换操作
        with open(deploy_file, "w") as fw:
            fw.write(content)
    return deploy_file


def deploy_by_agent(image_name, app_info, is_test=False):
    """

    :param image_name:
    :param app_info:
    :param is_test:
    :return:
    """
    app_code = app_info.get(APP_CODE)
    test_env = app_info.get(DOCKER_ENV)
    build_type = app_info.get(BUILD_TYPE)
    project_task_id = app_info.get(PROJECT_TASK_ID)
    record_id = app_info.get(PUBLISH_RECORD)

    file_prefix = "%s_%s" % (app_code, test_env)
    # 生成yaml配置文件
    if is_test:
        deploy_file = "%s.yml" % file_prefix
        exec_system_cmd("echo 'hello fucking world!' > %s" %deploy_file)
    else:
        deploy_file = generate_yml(image_name, app_info)

    file_prefix = "%s_%s" % (app_code, test_env)
    # 生成文件的md5,存入新文件
    md5_file = "%s.md5" % file_prefix
    md5_content = md5sum(deploy_file)
    LOG.info("md5 vaule of [%s] is %s" % (deploy_file, md5_content))
    with open(md5_file, "w") as f:
        f.write("%s  %s" % (md5_content, deploy_file))

    # 两个文件压缩成zip
    zip_file = "%s.zip" % file_prefix
    zf = zipfile.ZipFile(zip_file, mode='w')
    try:
        LOG.info('adding %s, %s to %s' % (deploy_file, md5_file, zip_file))
        zf.write(deploy_file)
        zf.write(md5_file)
    finally:
        LOG.info('closing zip')
        zf.close()

    # 传送到服务器，并获取下载地址
    # TODO: 测试环境(1.30)上无法使用10.3.39.33
    if is_release():
        matrix_storage_ip = "10.3.39.33"
        matrix_storage_domain = "matrix-storage.missfresh.net"
        user = "www"
    else:
        matrix_storage_ip = "192.168.101.25" ## from congcong's local server
        matrix_storage_domain = "192.168.101.25:8081"
        user = "mryx"

    remote_path = "/data/app/{build_type}/{app_code}/{project_task_id}".format(build_type=build_type, app_code=app_code,
                                                                               project_task_id=project_task_id)
    push_cmd = '''/usr/bin/rsync -avz -e ssh {zip_file} "{user}@{matrix_storage_ip}:{remote_path}/{zip_file}"'''.format(
        user=user, zip_file=zip_file, matrix_storage_ip=matrix_storage_ip, remote_path=remote_path
    )

    # 创建目标目录
    create_dir_cmd = '''/usr/bin/ssh {user}@{matrix_storage_ip} "mkdir -p {remote_path}"'''.format(
        user =user, matrix_storage_ip=matrix_storage_ip, remote_path=remote_path
    )
    exec_system_cmd(create_dir_cmd)

    # 执行文件上传
    exec_system_cmd(push_cmd)
    package_url = "http://{matrix_storage_domain}/{build_type}/{app_code}/{project_task_id}/{zip_file}".format(
        matrix_storage_domain=matrix_storage_domain, build_type=build_type, app_code=app_code,
        project_task_id=project_task_id, zip_file=zip_file
    )

    # 远程执行的命令
    if is_test:
        remote_cmd = '''sudo cat /tmp/{deployment}'''.format(deployment=deploy_file)
    else:
        remote_cmd = '''sudo kubectl apply -f /tmp/{deployment}'''.format(deployment=deploy_file)

    # 通过调用publish接口，发送mq给agent
    pay_load = dict()
    pay_load["buildType"] = build_type
    pay_load["packageType"] = "docker"
    # 压缩包文件名
    pay_load["packageName"] = zip_file
    # 目标服务器上的存放、解压路径
    pay_load["packagePath"] = "/tmp"
    # 下载链接
    pay_load["packageUrl"] = package_url
    pay_load["md5Name"] = md5_file
    pay_load["action"] = "start"
    pay_load["dockerStartCmd"] = remote_cmd
    pay_load["recordId"] = record_id
    pay_load["ip"] = NODE_IP

    agent_url = r'http://127.0.0.1:18080/api/publish/agent/startapp'

    LOG.info("调用publish接口发消息给Agent: %s" % agent_url)
    LOG.info("调用参数: %s" % json.dumps(pay_load))
    try:
        result = requests.post(agent_url, json=pay_load)
        # TODO: 确认无误后删除 curl -sL -w "%{http_code}" ${agent_url} -H 'Content-Type: application/json'  -d ${post_param}
        if result.status_code != 200:
            LOG.error("调用publish接口发消息失败！")
            LOG.error(result.text)
            sys.exit(1)
    except Exception, e:
        LOG.error("调用publish接口发消息 meets error: " + e)
        sys.exit(1)

    # 轮训监听执行结果
    return listen_mq_from_agent(build_type, record_id)


def listen_mq_from_agent(build_type, record_id):
    """
    轮训监听agent发出的执行消息，30s，每秒轮训一次
    回调执行结果
    :return:
    """
    # 轮训监听执行结果
    result_url = r'http://127.0.0.1:18080/api/publish/agent/getresult'
    post_param = dict()
    post_param["buildType"] = build_type
    post_param["recordId"] = record_id
    post_param["ip"] = NODE_IP
    for i in range(20):  # 20次轮训，间隔3sec
        time.sleep(3)
        try:
            result = requests.post(result_url, json=post_param)
            # TODO: 确认无误后删除 curl -sL -w "%{http_code}" ${result_url} -H 'Content-Type: application/json'  -d ${post_param}
            if result.status_code != 200:
                LOG.error("调用publish接口接收消息失败,非200错误！")
                LOG.error(result.text)
                sys.exit(1)
            j_result = json.loads(result.text)
            if j_result.get("code") == "4":
                # 尚未结果
                LOG.info("尚未获取结果, 继续轮训")
                continue
            elif j_result.get("code") == "0":
                # 获取成功结果
                LOG.info("调用publish接口接收消息成功！")
                return True, "Success"
            else:
                msg = "%s. %s" % (j_result.get("code"), j_result.get("message"))
                LOG.error("调用publish接口接收消息失败，200错误！ %s " % msg)
                return False, msg
        except Exception, e:
            LOG.error("调用publish接口接收消息 meets error: " + e)
            sys.exit(1)
    return False, "Timeout: 未轮训到执行结果"


def remote_start_or_update_deployment(image_name, app_info):
    """
    判断是否存在deployment文件，存在则使用其启动并更新
    :return:
    """
    app_code = app_info.get(APP_CODE)
    test_env = app_info.get(DOCKER_ENV)

    deploy_file = generate_yml(image_name, app_info)
    # 推送到远端的/tmp目录下
    exec_system_cmd("scp %s www@%s:/tmp/" % (deploy_file,NODE_IP))
    # 判断是否有deployment存活，没有的话，create；有的话, apply
    exec_system_cmd('''ssh www@{node_ip} > /dev/null 2>&1 << EOF
test `sudo kubectl get pod -n {test_env}|grep "{app_name}"|wc -l` -eq 0 && sudo kubectl create -f /tmp/{deployment} || sudo kubectl apply -f /tmp/{deployment}
EOF
    '''.format(node_ip=NODE_IP, test_env=test_env, app_name=app_code, deployment=deploy_file))
    LOG.info("部署完成~~~")

    # 检查是否启动成功
    # TODO: 部署机上无法通过域名访问目标服务，暂时注释掉
    # health_check_path = app_info.get(HEALTH_CHECK)
    # if health_check_path:
    #     health_check_path = health_check_path.lstrip('/')
    #
    #     for i in range(1, CHECK_LIMIT + 1):
    #         health_check_url = "http://{app_code}.{docker_env}.missfresh.net/{health_check_path}".format(
    #             app_code=app_code,
    #             docker_env=test_env,
    #             health_check_path=health_check_path)
    #         try:
    #             res = requests.get(health_check_url)
    #             code = res.status_code
    #             print(code)
    #             print(type(code))
    #             if res.status_code != 200:
    #                 print("%02d GET %d code while ping %s" % (i, code, health_check_url))
    #                 print("%2d] GET %d code while ping %s" % (i, code, health_check_url))
    #                 LOG.info("%2d. GET %d code while ping %s" % (i, code, health_check_url))
    #                 time.sleep(1)
    #             else:
    #                 LOG.info("服务已启动！")
    #                 sys.exit(0)
    #         except ConnectionError, e:
    #             LOG.info("%2d] GET ConnectionError while ping %s" % (i, health_check_url))
    #             time.sleep(1)
    #
    # else:
    #     logging.info("没有配置healthcheck检查，跳过！！！")


def callback_publish(app_info, deploy_status=None):
    """
    回调publish, 更新相关参数
    :param app_info:
    :return:
    """
    callback_url = r'http://127.0.0.1:18080/api/publish/publish/betaCallBack'

    deploy_code = app_info.get(DEPLOY_STATUS)
    if deploy_status is not None:
        deploy_code = deploy_status
    payload = {
        "tag": app_info.get(B_TAG),
        "deployStatus": deploy_code,
        "recordId": app_info.get(PUBLISH_RECORD),
        "batchId": app_info.get(BATCH_RECORD),
        "planId": app_info.get(PLAN_RECORD)
    }
    LOG.info("回调url: %s" % callback_url)
    LOG.info("回调参数: %s" % json.dumps(payload))
    result = requests.post(callback_url, json=payload)
    if result.status_code != 200:
        LOG.error("回调Publish失败！")
        LOG.error(result.text)
        sys.exit(1)


def clean_workspace(app_info):
    if app_info.get(WORKSPACE) in ["/", "./"]:
        return
    shutil.rmtree(app_info.get(WORKSPACE))


def get_kibana_url(app_info):
    """
    生成过去3分钟内查询当前部署应用的kibana日志
    :param app_info:
    :return:
    """
    app_code = app_info.get(APP_CODE)
    test_env = ES_INDEX.get(app_info.get(DOCKER_ENV), None)
    kibana_url = '''http://kibana.k8s.missfresh.net/app/kibana#/discover?_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-3m,mode:relative,to:now))&_a=(columns:!(_source),index:'{test_env}',interval:auto,query:(language:lucene,query:%22{app_code}%22),sort:!('@timestamp',desc))'''
    if test_env is None:
        return '当前namespace的尚未支持生成url，请登陆http://kibana.k8s.missfresh.net自行查阅日志'
    return kibana_url.format(test_env=test_env, app_code=app_code)


def how_to_see_log(app_info):
    """
    打印提示信息，如何查看log
    :param app_info:
    :return:
    """
    # if app_info.get(APP_TYPE) == 'static': # 前端静态工程不需要日志
    #     return

    test_env = app_info.get(DOCKER_ENV)
    app_code = app_info.get(APP_CODE)
    LOG.info("=" * 20)
    LOG.info("如何查看发布应用的业务日志")
    LOG.info("Kibana日志访问链接: %s" % get_kibana_url(app_info))
    LOG.info("或者登陆集群节点查看日志")
    LOG.info("1. 通过跳板机登陆%s" % NODE_IP)
    LOG.info("2. 输入以下命令直接登陆目标容器，日志在/log目录下。登陆容器后也可以访问dubbo服务，通过 nc 127.0.0.1 dubbo_port 进行访问")
    LOG.info(
        r"sudo kubectl exec -it -n %s `sudo kubectl get pod -n %s -l app=%s| grep Running| awk '{print $1}'` /bin/bash" % (
            test_env, test_env, app_code))
    LOG.info("3. 如何无法登陆，输入以下命令查看应用是否存在，或者应用的状态是否是Running")
    LOG.info(r"sudo kubectl get pod -n %s -l app=%s" % (test_env, app_code))
    LOG.info("4. 查看标准输出的日志")
    LOG.info(r"sudo kubectl logs -f -n %s `sudo kubectl get pod -n %s -l app=%s| grep Running| awk '{print $1}'` -c %s"
             % (test_env, test_env, app_code, app_code))
    LOG.info("=" * 20)


def main():
    """
    主流程
    :param is_init: 当为True的时候，表示首次上传镜像，不管用户使用什么分支，都需要上传一份master tag的镜像到harbor
    :return:
    """
    # test_env, app_code, biz_line, branch, is_init = parse_args()
    # is_init = bool(is_init)

    app_info = parse_args()
    image_name = prepare_image(app_info)
    # 远程服务启动 - 不存在则启动，存在则更新镜像
    if is_release():
        ret, msg = deploy_by_agent(image_name, app_info)
        LOG.info("通过Agent部署: " + msg)
        if ret:
            callback_publish(app_info)
        else:
            callback_publish(app_info, DEPLOY_FAIL)
    else:
        remote_start_or_update_deployment(image_name, app_info)
        callback_publish(app_info)
    # 清理
    clean_workspace(app_info)
    how_to_see_log(app_info)


def deploy_by_agent_test():
    test_param = dict()
    test_param[BUILD_TYPE] = "beta"
    test_param[APP_CODE] = "mytest"
    test_param[DOCKER_ENV] = "b1"
    test_param[APP_TYPE] = "jar"
    test_param[PROJECT_TASK_ID] = "prj_task_123"
    test_param[PUBLISH_RECORD] = "record123"

    # 轮训监听执行结果
    ret, msg = deploy_by_agent("bbc", test_param, True)
    LOG.info(ret)
    LOG.info(msg)


if __name__ == '__main__':
    main()
    # deploy_by_agent_test()
