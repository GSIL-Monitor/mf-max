#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: bing.wei
# Email: weibing@missfresh.cn
from __future__ import print_function, division
import sys

sys.path.append(".")
sys.path.append("../")
# import here
import requests
import config.Config as Cfg
import json

reload(sys)
sys.setdefaultencoding('utf-8')

ROCKETMQ_VERSION_MAP = {
    "rocketmq-4.1.0-incubating": "-41",
    "rocketmq-4.2.0": "",
    "rocketmq-4.3.0": "-43",
}

CLUSTER = "cluster"
BROKER_NAME = "brokerName"
TOPIC_NAME = "topicName"


"""
该脚本用于同步rocketmq topic
"""


def valid_info(rocketmq_version, env):
    """
    检查参数有效性，有效时，返回rocketmq version后缀
    :param rocketmq_version:
    :param env:
    :return:
    """
    if rocketmq_version not in ROCKETMQ_VERSION_MAP.keys():
        print("rocketmq vertion版本错误")
        sys.exit(1)
    if env not in Cfg.get_env():
        print("测试环境信息错误")
        sys.exit(1)


def get_rocket_mq_version_postfix(rocketmq_version):
    return ROCKETMQ_VERSION_MAP.get(rocketmq_version)


def get_topic_online(rocketmq_version, env):
    valid_info(rocketmq_version, env)
    rocketmq_version_postfix = get_rocket_mq_version_postfix(rocketmq_version)
    broker_info = get_broker_info(rocketmq_version, env)

    url = r'http://rocketmq-console-ng{version}.{env}.missfresh.net/topic/list.query'.format(
        version=rocketmq_version_postfix, env=env)

    result = requests.get(url)
    if result.status_code == 200:
        j_data = json.loads(result.content).get("data", {})
        topic_list = j_data.get("topicList", [])
        topic_list.remove(broker_info.get(BROKER_NAME))
        topic_list.remove(broker_info.get(CLUSTER))
    else:
        print("获取rocketmq toplic列表出错: %s" % url)
        print(result.text)
        sys.exit(1)

    return topic_list


def save_topic(topic_list, _file):
    with open(_file, "w") as f:
        f.writelines([ x + "\n" for x in topic_list])


def retrieve_topic(rocketmq_version, env=None):
    """
    获取默认或者指定环境的rocketmq topic列表，用于同步到新环境
    :param env:
    :return:
    """
    if env is None:
        return Cfg.get_rocketmq_topic()
    else:
        return get_topic_online(rocketmq_version, env)


def get_broker_info(rocketmq_version, env):
    rocketmq_version_postfix = get_rocket_mq_version_postfix(rocketmq_version)

    url = r'http://rocketmq-console-ng{version}.{env}.missfresh.net/cluster/list.query'.format(
        version=rocketmq_version_postfix, env=env)

    broker_info = dict()
    result = requests.get(url)
    if result.status_code == 200:
        j_data = json.loads(result.content).get("data", {})
        broker_cluster = j_data.get("clusterInfo", {}).get("brokerAddrTable", {})

        for k, v in broker_cluster.items():
            broker_info.update(v)
    else:
        print("获取rocketmq broker出错: %s" % url)
        print(result.text)
        sys.exit(1)

    print(broker_info)
    return broker_info


def sync_target_env(rocketmq_version, target_env, topic_list):
    valid_info(rocketmq_version, target_env)
    rocketmq_version_postfix = get_rocket_mq_version_postfix(rocketmq_version)
    broker_info = get_broker_info(rocketmq_version, target_env)

    payload = {"writeQueueNums": 16,
               "readQueueNums": 16,
               "perm": 6,
               "order": False,
               # "topicName": "TestTopic",
               "brokerNameList": [broker_info.get(BROKER_NAME)],
               "clusterNameList": ["DefaultCluster"]}

    url = r'http://rocketmq-console-ng{version}.{env}.missfresh.net/topic/createOrUpdate.do'.format(
        version=rocketmq_version_postfix, env=target_env)

    for i, topic in enumerate(topic_list):
        payload[TOPIC_NAME] = topic

        result = requests.post(url, json=payload)
        print("{index}. 向 {env} 的{rocketmq_version}添加Topic[{topic}]".format(index=i, env=target_env,
                                                                            rocketmq_version=rocketmq_version,
                                                                            topic=topic))
        if result.status_code != 200 or not json.loads(result.content).get("data"):
            print(result.text)
            print("插入Topic: %s 失败" % topic)
            sys.exit(1)

    print("Done!")


def parse_args():
    if len(sys.argv) < 5:
        print("Usage: python {0} src_rocketmq_version src_env des_rocketmq_version des_env\n \
              E.g. {0} rocketmq-4.1.0-incubating b1 rocketmq-4.3.0 b1".format(__file__))
        sys.exit(1)
    else:
        src_rocketmq_version = sys.argv[1]
        src_env = sys.argv[2]
        des_rocketmq_version = sys.argv[3]
        des_env = sys.argv[4]
        valid_info(src_rocketmq_version, src_env)
        valid_info(des_rocketmq_version, des_env)
    return src_rocketmq_version, src_env, des_rocketmq_version, des_env


def main():
    src_rmq_version, src_env, des_rmq_version, des_env = parse_args()
    topic_list = retrieve_topic(src_rmq_version, src_env)
    save_topic(topic_list, "topic_list.txt")
    sync_target_env(des_rmq_version, des_env, topic_list)


def main_local(src_rmq_version, src_env, des_rmq_version, des_env):
    topic_list = retrieve_topic(src_rmq_version, src_env)
    save_topic(topic_list, "topic_list.txt")
    sync_target_env(des_rmq_version, des_env, topic_list)


if __name__ == '__main__':
    # main_local("rocketmq-4.1.0-incubating", "b1", "rocketmq-4.2.0", "b1")
    print(retrieve_topic("rocketmq-4.1.0-incubating", "b1"))
    print(retrieve_topic("rocketmq-4.2.0", "b1"))
