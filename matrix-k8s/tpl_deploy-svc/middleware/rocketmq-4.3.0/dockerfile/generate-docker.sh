#!/bin/bash

# Build base image
docker build -t apache/rocketmq-base:4.3.0 --build-arg version=4.3.0 ./rocketmq-base

# Build namesrv and broker
docker build -t harbor.missfresh.net/library/rocketmq-namesrv:4.3.0-k8s ./kubernetes/rocketmq-namesrv
docker build -t harbor.missfresh.net/library/rocketmq-broker:4.3.0-k8s ./kubernetes/rocketmq-broker

docker push harbor.missfresh.net/library/rocketmq-namesrv:4.3.0-k8s
docker push harbor.missfresh.net/library/rocketmq-broker:4.3.0-k8s

# 仅用于创建Docker并推送，并不启动服务
# Run namesrv and broker on your Kubernetes cluster.
#kubectl create -f kubernetes/deployment.yaml
