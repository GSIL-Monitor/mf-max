# Martix Agent

## 包含内容

1. Agent管理
   - 状态、环境变量、启动、停止、重启
   - 执行shell命令
2. App管理
   - 启动、停止、重启



## 需要设置镜像

- glide mirror set golang.org/x/crypto github.com/golang/crypto
- glide mirror set golang.org/x/sys github.com/golang/sys
- glide mirror set golang.org/x/net github.com/golang/net
- glide mirror set golang.org/x/text github.com/golang/text

## 启动带参数




3、matrix-agent 自启动
release版：
/data/app/matrix-agent/matrix-agent -v release -c /data/app/matrix-agent/config.yaml > /dev/null 2>&1 &
beta版：
/data/app/matrix-agent/matrix-agent -v beta -c /data/app/matrix-agent/config.yaml > /dev/null 2>&1 &

