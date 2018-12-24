#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: bing.wei
# Email: weibing@missfresh.cn
from __future__ import print_function, division
import sys

# import here

reload(sys)
sys.setdefaultencoding('utf-8')

ENV_LIMIT = 10


def get_zk():
    zk_cluster = dict()
    for i in range(1, ENV_LIMIT+1):
        zk_cluster["b%d" % i] = "zk-0.b{env}:2181,zk-1.b{env}:2181,zk-2.b{env}:2181".format(env=i)
    return zk_cluster


def get_redis():
    redis_cluster = dict()
    for i in range(1, ENV_LIMIT+1):
        redis_cluster["b%d" % i] = "redis-master.b{env}:6379".format(env=i)
    return redis_cluster


def get_env():
    return get_zk().keys()


def get_rocketmq_topic():
    return [
      "SendUserMergeTaskForShareMoney",
      "SendRedPacketForUnionIdList",
      "BenchmarkTest",
      "OFFSET_MOVED_EVENT",
      "SaveAntispamCheckLogs",
      "TBW102",
      "as_risk_AntispamDatalistUpdate",
      "SELF_TEST_TOPIC",
      "SendAccountMergerForCanteenTask",
      "as_risk_AntispamTianyuCheckLog",
      "SendAccountMergerTask",
      "SendChannelInfo",
      "as_risk_AntispamDatalistSync",
      "SupplierSendMail",
      "%RETRY%TOOLS_CONSUMER",
      "UserLogin",
      "SendAccountMergerForMarketTask",
      "UserFirstLogin"
    ]


if __name__ == '__main__':
    print(get_redis())
