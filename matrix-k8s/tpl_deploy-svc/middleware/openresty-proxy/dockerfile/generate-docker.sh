#!/bin/bash

docker build -t harbor.missfresh.net/library/openresty-proxy:centos .
docker push harbor.missfresh.net/library/openresty-proxy:centos
