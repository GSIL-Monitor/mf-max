package rabbitmq

import (
	"matrix-agent/model"
	"matrix-agent/service"
	"matrix-agent/utils"
	"matrix-agent/enum"
)

func (mq *RabbitMq) StartApp(rcvData []byte) {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("startApp接口异常:%v", err)
		}
	}()
	var param model.StartApp
	err := utils.Jsoniteror.Unmarshal(rcvData, &param)
	if err != nil {
		utils.Logger().ParamWarn("startApp 参数错误 err =%+v", err.Error())
		mq.SendRabitMqMsgError(err.Error(), param.RecordId, param.BuildType)
		return
	}
	utils.Logger().Info("startApp param = %+v", param)
	err = param.CheckParam()
	if err != nil {
		mq.SendRabitMqMsgError(err.Error(), param.RecordId, param.BuildType)
		return
	}
	err = service.NewAppService().StartApp(&param)
	if param.PackageType == "agent" { //不发送返回信息
		return
	}
	if err != nil {
		mq.SendRabitMqMsgError(err.Error(), param.RecordId, param.BuildType)

		return
	}
	if !mq.judgeIsCheck(&param) {
		mq.SendRabitMqMsgOk(param.RecordId, param.BuildType)
		return
	}
	err = param.CheckIsSuccess()
	if err != nil {
		mq.SendRabitMqMsgError(err.Error(), param.RecordId, param.BuildType)
		return
	}
	mq.SendRabitMqMsgOk(param.RecordId, param.BuildType)

}

//判断是否检验探活地址
func (mq *RabbitMq) judgeIsCheck(param *model.StartApp) bool {
	switch param.PackageType {
	case enum.Docker.String():
		return false
	case enum.Static.String():
		return false
	case enum.Wmsconf.String():
		return false
	case enum.WmsWwwRoot.String():
		return false
	case enum.JarMain.String():
		return false
	default:
		return true
	}
}
