package model

type ServerInfo struct {
	Ip              string `json:"ip"`
	Java            string `json:"java"`
	Cpu             string `json:"cpu"`
	Load            string `json:"load"`
	Mem             string `json:"mem"`
	Disk            string `json:"disk"`
	Os              string `json:"os"`
	AgentCreateTime string `json:"agentCreateTime"`
	AgentStartTime  string `json:"agentStartTime"`
	DataAppUser     string `json:"dataAppUser"`
	DataLogsUser    string `json:"dataLogsUser"`
	DataBackupUser  string `json:"dataBackupUser"`
	AgentLogSize    string `json:"agentLogSize"`
	AgentPercentMem string `json:"agentPercentMem"`
	AgentPercentCpu string `json:"agentPercentCpu"`
	AgentMem        string `json:"agentMem"`
}
