package main

import (
	"matrix-agent/config"
	"github.com/gin-gonic/gin"
	"matrix-agent/api"
	"matrix-agent/utils"
	_ "matrix-agent/schedule"
	"net/http"
	"net"
	"os"
	"syscall"
	"flag"
	"sync"
	"matrix-agent/service"
	"matrix-agent/rabbitmq"
)

var (
	server           http.Server
	listener         net.Listener
	runningServerReg sync.RWMutex
	isChild          bool
)

func init() {
	runningServerReg = sync.RWMutex{}
	flag.BoolVar(&isChild, "continue", false, "listen on open fd (after forking)")
	flag.String("v", "dev", "matrix-agent version")
	flag.String("c", "config.yaml", "matrix-agent config")

	flag.Parse()
}
func main() {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("matrix-agent服务异常:%v", err)
		}
	}()
	utils.Logger().Info("agent starting...")
	ginEngine := initGin()
	server = http.Server{
		Addr:    ":30000",
		Handler: ginEngine,
	}
	var err error
	listener, err = getListener(server.Addr)
	if err != nil {
		utils.Logger().Error("net.Listen error: %v", err)
	}
	newSig := service.NewSig()
	newSig.Server = server
	newSig.Listener = listener
	go newSig.HandleSignals()
	go rabbitmq.NewRabbitMq().HandlerRabbitMqMsg()
	server.Serve(listener)
	utils.Logger().Info("agent started")
}

func initGin() *gin.Engine {
	if "release" == config.Mode {
		gin.SetMode(gin.ReleaseMode)
	} else {
		gin.SetMode(gin.DebugMode)
	}

	ginEngine := gin.Default()
	api.InitApiGroup(ginEngine)
	return ginEngine
}
func getListener(laddr string) (l net.Listener, err error) {
	if isChild {
		runningServerReg.RLock()
		defer runningServerReg.RUnlock()
		f := os.NewFile(3, "")
		l, err = net.FileListener(f)
		if err != nil {
			utils.Logger().Error("net.FileListener error:", err)
			return
		}
		syscall.Kill(syscall.Getppid(), syscall.SIGTSTP) //干掉父进程

	} else {
		l, err = net.Listen("tcp", laddr)
		if err != nil {
			utils.Logger().Error("net.FileListener error:", err)
			return
		}
	}
	return
}
