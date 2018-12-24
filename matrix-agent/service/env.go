package service

import (
	"fmt"
	"github.com/shirou/gopsutil/load"
	"matrix-agent/model"

	"matrix-agent/utils"
	"strconv"
	"strings"

	//"matrix-agent/utils"
	//"matrix-agent/config"
	"time"

	"github.com/shirou/gopsutil/cpu"
	"github.com/shirou/gopsutil/disk"
	"github.com/shirou/gopsutil/host"
	"github.com/shirou/gopsutil/mem"
	"os/exec"
	"syscall"
	"github.com/shirou/gopsutil/process"
	"os"
)

type EnvService struct {
}

func NewEnvService() *EnvService {
	return &EnvService{}
}

func (envService *EnvService) Env() model.ServerInfo {
	v, _ := mem.VirtualMemory()
	c, _ := cpu.Info()
	cc, _ := cpu.Percent(time.Second, false)
	d, _ := disk.Usage("/")
	n, _ := host.Info()
	l, _ := load.Avg()
	//nv, _ := net.IOCounters(true)

	serverInfo := model.ServerInfo{}
	serverInfo.Ip = utils.GetLocalIp()
	serverInfo.Mem = fmt.Sprintf("Mem: %v MB Free: %v MB Used:%v MB Usage:%f%%\n", v.Total/1024/1024, v.Available/1024/1024, v.Used/1024/1024, v.UsedPercent)
	//fmt.Printf("Mem: %v MB Free: %v MB Used:%v MB Usage:%f%%\n", v.Total/1024/1024, v.Available/1024/1024, v.Used/1024/1024, v.UsedPercent)
	var builder strings.Builder
	if len(c) > 1 {
		for _, sub_cpu := range c {
			modelname := sub_cpu.ModelName
			cores := sub_cpu.Cores
			//fmt.Printf("CPU:%v %v cores \n", modelname, cores)
			builder.WriteString("CPU:")
			builder.WriteString(modelname)
			builder.WriteString(" ")
			builder.WriteString(string(cores))
			builder.WriteString(" cores. ")
		}
	} else {
		sub_cpu := c[0]
		modelname := sub_cpu.ModelName
		cores := sub_cpu.Cores
		//fmt.Printf("CPU:%v %v cores \n", modelname, cores)
		builder.WriteString("CPU:")
		builder.WriteString(modelname)
		builder.WriteString(" ")
		builder.WriteString(strconv.Itoa(int(cores)))
		builder.WriteString(" cores. ")
	}
	//fmt.Printf("Network: %v bytes / %v bytes\n", nv[0].BytesRecv/1024/1024, nv[0].BytesSent/1024/1024)
	//fmt.Printf("CPU Used:used %f%% \n", cc[0])
	builder.WriteString("CPU Used:used ")
	//float64转string
	builder.WriteString(strconv.FormatFloat(cc[0], 'E', -1, 64))
	serverInfo.Cpu = builder.String()
	//fmt.Printf("HD: %v GB Used: %v GB Free: %v GB Usage:%f%%\n", d.Total/1024/1024/1024, d.Used/1024/1024/1024, d.Free/1024/1024/1024, d.UsedPercent)
	//fmt.Printf("OS: %v(%v) %v \n", n.Platform, n.PlatformFamily, n.PlatformVersion)
	//fmt.Printf("Hostname  : %v \n", n.Hostname)
	serverInfo.Disk = fmt.Sprintf("HD: %v GB Used: %v GB Free: %v GB Usage:%f%%\n", d.Total/1024/1024/1024, d.Used/1024/1024/1024, d.Free/1024/1024/1024, d.UsedPercent)
	serverInfo.Java = getJava()
	serverInfo.Os = fmt.Sprintf("OS: %v(%v) %v \n", n.Platform, n.PlatformFamily, n.PlatformVersion)
	serverInfo.Load = l.String()
	serverInfo.AgentCreateTime = getAgentCreateTime()
	serverInfo.AgentStartTime = getAgentStartTime()
	serverInfo.DataAppUser = getDirUser("app")
	serverInfo.DataLogsUser = getDirUser("logs")
	serverInfo.DataBackupUser = getDirUser("backup")
	serverInfo.AgentLogSize = getAgentLogSize()
	cpuPercent, memPercent, pidMem := getAgentMemCpu()
	serverInfo.AgentPercentCpu = cpuPercent
	serverInfo.AgentPercentMem = memPercent
	serverInfo.AgentMem = pidMem
	return serverInfo
}

func getJava() string {
	_, err := utils.ExecCommand("source /etc/profile")
	if err != nil {
		utils.Logger().Error("source /etc/profile fail:%v", err)
		return ""
	}
	cmd := exec.Command("java", "-version")
	out, err := cmd.CombinedOutput()
	if err != nil {
		utils.Logger().Warn("exec java -version cmd fail:%v", err)
		return ""
	}
	return string(out)

}
func getAgentCreateTime() string {
	cmd := "ls -l /data/app/matrix-agent | grep -w matrix-agent| awk '{print $6$7} {print $8}'"
	startTime, err := utils.ExecCommand(cmd)
	if err != nil {
		return ""
	}
	if startTime == "" {
		return ""
	}
	allInfo := strings.Trim(startTime, "\n")

	startTime = strings.Replace(allInfo, "\n", " ", -1)
	return startTime

}
func timespecToStrTime(ts syscall.Timespec) string {
	return time.Unix(int64(ts.Sec), int64(ts.Nsec)).String()[:19]
}
func getAgentStartTime() string {
	cmd := "ps aux --width=1000 | grep -v 'ps aux' |grep -v grep | grep 'matrix-agent -v' |awk '{print $9} {print $10}'"
	startTime, err := utils.ExecCommand(cmd)
	if err != nil {
		return ""
	}
	if startTime == "" {
		return ""
	}
	allInfo := strings.Trim(startTime, "\n")

	startTime = strings.Replace(allInfo, "\n", " ", -1)
	return startTime
}
func getDirUser(dir string) string {
	cmd := fmt.Sprintf("ls -l /data | grep -w %v | awk '{print $3}'", dir)
	cmd = fmt.Sprintf(	"ls  -l /data  |grep '^d' |grep -v , | grep '\\<%v$' | awk '{print $3}'",dir)
	user, err := NewAppService().FindInfo(cmd)
	if err != nil {
		utils.Logger().Warn("findInfo:/data/%v fail:%v", dir, err)
		return ""
	}
	utils.Logger().Info("/data/%v目录所属用户是：%v", dir, user)
	return user
}
func getAgentLogSize() string {
	cmd := "ls -l -h /data/logs/matrix-agent/matrix-agent.log | awk '{print $5}'"
	size, err := utils.ExecCommand(cmd)
	if err != nil {
		return ""
	}
	allInfo := strings.Trim(size, "\n")

	size = strings.Replace(allInfo, "\n", " ", -1)
	return size
}
func getAgentMemCpu() (string, string, string) {
	pid := os.Getpid()
	utils.Logger().Info("agentPid==============:%v", pid)
	pro, err := process.NewProcess(int32(pid))
	if err != nil {
		utils.Logger().Warn("agentPid:%v process.NewProcess fail", pid, err)
		return "", "", ""
	}
	cpup, err := pro.CPUPercent() //返回改进程所占cpu百分比
	if err != nil {
		utils.Logger().Warn("agentPid:%v pro.CPUPercent fail", pid, err)
		return "", "", ""
	}
	cpuPercent := fmt.Sprintf("%0.4v%v", cpup*100, "%")
	memp, err := pro.MemoryPercent()
	if err != nil {
		utils.Logger().Warn("agentPid:%v pro.MemoryPercent fail", pid, err)
		return cpuPercent, "", ""
	}
	memPercent := fmt.Sprintf("%0.4v%v", memp*100, "%")

	cmd := fmt.Sprintf("cat /proc/%v/status | grep VmRSS | awk '{print $2$3}'", pid)

	size, err := utils.ExecCommand(cmd)
	if err != nil {
		return cpuPercent, memPercent, ""
	}
	size = strings.Trim(size, "\n")
	pidMem := strings.Replace(size, "\n", " ", -1)
	return cpuPercent, memPercent, pidMem
}
