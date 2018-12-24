package api

import (
	"matrix-agent/utils"
	"github.com/gin-gonic/gin"
)
func init() {

}

func InitApiGroup(r *gin.Engine) {
	api :=r.Group("/api")
	api.GET("/healthcheck",healthCheck)

	addAgentGroup(api)
}

func healthCheck(c *gin.Context)  {
	utils.Responder().OK(c,"success")
}
