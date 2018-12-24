package utils

import (
	"crypto/md5"
	"encoding/hex"
)

func Md5Encode(value string) string {
	md5Ctx := md5.New()
	md5Ctx.Write([]byte(value))
	pushToken := md5Ctx.Sum(nil)
	return hex.EncodeToString(pushToken)
}
