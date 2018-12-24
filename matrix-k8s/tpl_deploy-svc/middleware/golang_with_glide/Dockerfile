FROM golang:latest
# harbor.missfresh.net/library/golang_with_glide:1.11.2

MAINTAINER missfresh.net

RUN curl https://glide.sh/get | sh \
  && mkdir -p /data \
  && ln -s $GOPATH /data/app