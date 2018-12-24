package utils

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

var rm *responder

type responder struct {
}

func init() {
	newResponseManager()
}

func newResponseManager() {
	rm = &responder{}
}

func Responder() *responder {
	return rm
}

func (r *responder) BadRequest(context *gin.Context, msg string) {
	if msg == "" || context == nil {
		fmt.Println("参数不能为空！")
		return
	}
	gh := gin.H{
		"code":    1,
		"ret":     "fail",
		"message": msg,
		"data":    nil,
	}
	Logger().Info("返回数据 %v %+v", http.StatusBadRequest, gh)
	context.JSON(http.StatusBadRequest, gh)
	return
}

func (r *responder) InternalError(context *gin.Context, msg string) {
	if context == nil || msg == "" {
		fmt.Println("参数不能为空！")
		return
	}
	gh := gin.H{
		"code":    1,
		"ret":     "fail",
		"message": msg,
		"data":    nil,
	}
	Logger().Info("返回数据 %v %+v", http.StatusOK, gh)
	context.JSON(http.StatusOK, gh)
	return
}

func (r *responder) OK(context *gin.Context, data interface{}) {
	if context == nil {
		fmt.Println("context不能为空！")
		return
	}
	gh := gin.H{
		"code":    0,
		"ret":     "success",
		"message": nil,
		"data":    data,
	}
	Logger().Info("返回数据 %v %+v", http.StatusOK, gh)
	context.JSON(http.StatusOK, gh)
	return
}
