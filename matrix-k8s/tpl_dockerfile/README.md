# 乱码解决办法
https://blog.saintic.com/blog/122.html


# apk使用
```
sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
apk update
apk add busybox-extras
apk add mysql-client
```
