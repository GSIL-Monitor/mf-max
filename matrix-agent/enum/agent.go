package enum

import (
	"matrix-agent/model"
	"matrix-agent/utils"
	"fmt"
	"syscall"
	"errors"
)

func (agent AgentType) Update(param *model.StartApp) error {
	cmd := "wget http://git.missfresh.net/matrix/matrix-init/raw/master/install.sh -O - | sh"
	//cmd := "/etc/init.d/start_matrixagent restart"
	utils.ExecCommandAsync(cmd)
	return nil

}
func (agent AgentType) RestartAgent() error {
	pid := syscall.Getpid()
	utils.Logger().Info("now pid=============:%v", pid)
	err := syscall.Kill(pid, syscall.SIGHUP)
	if err != nil {
		utils.Logger().Error("给pid:%v发送SIGHUP信号fail:%v", pid, err)
		return err
	}

	return nil
}
func (agent AgentType) HandleAction(param *model.StartApp) error {
	switch param.Action {
	case "restart":
		return agent.RestartAgent()
	case "upgrade":
		return agent.Update(param)
	default:
		errInfo := fmt.Sprintf("agent应用无此种处理方式:%v", param.Action)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}

}
