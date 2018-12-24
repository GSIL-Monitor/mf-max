package service

import (
	"matrix-agent/utils"
	"matrix-agent/config"
)

type AgentService struct {
}

func NewAgentService() *AgentService {
	return &AgentService{}
}

func (agentService *AgentService) Start() (string, error) {
	result, err := utils.ExecCommand(config.Script.Start)
	return result, err
}

func (agentService *AgentService) Shutdown() (string, error) {
	result, err := utils.ExecCommand(config.Script.Shutdown)
	return result, err
}

func (agentService *AgentService) Restart() (string, error) {
	result, err := utils.ExecCommand(config.Script.Restart)
	return result, err
}
