package api

import (
	"github.com/gin-gonic/gin"
	"matrix-agent/model"
	"matrix-agent/service"
	"matrix-agent/utils"
	"net/http"
)

func addAgentGroup(g *gin.RouterGroup) {
	agent := g.Group("/agent")

	//agent
	agent.POST("/start", start)
	agent.POST("/shutdown", shutdown)
	agent.POST("/restart", restart)

	//env
	agent.POST("/env", env)

	//app
	agent.POST("/startapp", startApp)
	agent.POST("/healthcheck", healthCheckApp)

}

//agent启动
func start(c *gin.Context) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("start接口异常:%v", err)
		}
	}()
	utils.Logger().Info("start")
	result, err := service.NewAgentService().Start()
	if err != nil {
		utils.Responder().InternalError(c, "start failure :"+err.Error())
		return
	}
	utils.Responder().OK(c, string(result))
}

func shutdown(c *gin.Context) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("shutdown接口异常:%v", err)
		}
	}()
	utils.Logger().Info("shutdown")
	result, err := service.NewAgentService().Shutdown()
	if err != nil {
		utils.Responder().InternalError(c, "shutdown failure :"+err.Error())
		return
	}
	utils.Responder().OK(c, string(result))
}

func restart(c *gin.Context) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("restart接口异常:%v", err)
		}
	}()
	utils.Logger().Info("restart")
	result, err := service.NewAgentService().Restart()
	if err != nil {
		utils.Responder().InternalError(c, "restart failure :"+err.Error())
		return
	}
	utils.Responder().OK(c, string(result))
}

func env(c *gin.Context) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("env接口异常:%v", err)
		}
	}()
	utils.Logger().Info("env")
	result := service.NewEnvService().Env()
	utils.Responder().OK(c, result)
}

func startApp(c *gin.Context) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("startApp接口异常:%v", err)
		}
	}()
	var param model.StartApp
	if err := c.Bind(&param); err != nil {
		utils.Logger().ParamWarn("startApp 参数错误 err =%+v", err.Error())
		utils.Responder().BadRequest(c, "参数错误")
		return
	}
	utils.Logger().Info("startApp param = %+v", param)
	err := service.NewAppService().StartApp(&param)
	if err != nil {
		utils.Responder().InternalError(c, "startApp failure :"+err.Error())
		return
	}
	utils.Responder().OK(c, nil)
}

func healthCheckApp(c *gin.Context) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("healthCheckApp接口异常:%v", err)
		}
	}()
	var param model.HealthCheck
	if err := c.Bind(&param); err != nil {
		utils.Logger().ParamWarn("healthcheck 参数错误 err =%+v", err.Error())
		utils.Responder().BadRequest(c, "参数错误")
		return
	}
	utils.Logger().Info("healthcheck param = %+v", param)
	statusCode := 200

	resp, err := http.Get(param.Url)

	if (err != nil) {

		utils.Logger().Info("healthcheck err:%v", err)

	}
	defer resp.Body.Close()
	utils.Logger().Info("healthcheck StatusCode =  %v", resp.StatusCode)
	if resp.StatusCode == statusCode {
		utils.Responder().OK(c, "success")
		return
	} else {
		utils.Responder().BadRequest(c, "healthcheck failure")
		return
	}
}
