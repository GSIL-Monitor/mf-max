package model

import (
	"errors"
	"time"
	"net/http"
	"matrix-agent/utils"
	"github.com/hpcloud/tail"
	"fmt"
	"os"
	"strings"
)

const (
	Jar        = "jar"
	Jetty      = "jetty"
	Tomcat     = "tomcat"
	Golang     = "go"
	Py         = "python"
	Static     = "static"
	Wms        = "wms"
	WmsWwwRoot = "wms-wwwroot"
	Wmsconf    = "wms-conf"
	Agent      = "agent"
	Docker     = "docker"
	SpringGz   = "spring_gz"
	JarMain    = "jar-main"
)

type StartApp struct {
	BuildType       string `json:"buildType"`
	PackageType     string `json:"packageType"`        // 包类型
	PackageName     string `json:"packageName"`        // 包名
	PackagePath     string `json:"packagePath"`        //包路径
	PackagePathTemp string `json:"packagePathTemp"`    //包备份路径
	PackageUrl      string `json:"packageUrl"`         //包url
	Md5Name         string `json:"md5Name"`            //md5文件名
	Action          string `json:"action"`             //动作
	StartOps        string `json:"startOps"`           // 启动参数
	RecordId        string `json:"recordId"`           //记录id
	ContainerPath   string `json:"containerPath"`      //容器（tomcat/jetty）路径
	HealthCheckUrl  string `json:"healthCheckUrl"`     //app启动之后探活url
	WmsJsService    string `json:"wmsJsService"`       //js服务应用名称
	StartShellPath  string `json:"startShellPath"`     //脚本启动路径
	StopShellPath   string `json:"stopShellPath"`      //脚本停止路径
	DockerStartCmd  string `json:"dockerStartCmd"`     //docker 启动命令
	AppLogPath      string `json:"appLogPath"`         //应用日志路径
	AppLogKeyWord   string `json:"appLogKeyWord"`      //应用日志检索成功关键词
	SpringCloudFlag int    `json:"springCloudFlag"`    //springcloud摘流量标志 1：摘 0：不摘
	AppName         string `json:"springCloudAppID"`   //应用名称
	AppPort         string `json:"springCloudAppPort"` //应用端口号
}

//应用行为
const (
	StartAction     = "start"
	RestartAction   = "restart"
	StopAction      = "stop"
	ClearAction     = "clear"
	LogKeywordCheck = 1 //日志探活检验
	HealthUrlCheck  = 2 //探活地址检验
	ErrCheck        = 0 //无探活方式
	findJavaPidCmd  = "ps aux --width=1000 | grep java | grep -v 'ps aux' | grep -v grep | grep %v | awk '{print $2}'"
	findGoPidCmd    = "ps aux --width=1000 | grep -v 'ps aux' |grep -v grep | grep '%v -v' |awk '{print $2}'"
)

//判断是以日志校验还是探活地址
func (app *StartApp) judgeCheckLogUrl() (int, error) {
	if app.HealthCheckUrl == "" && app.AppLogPath == "" {
		return ErrCheck, errors.New("app启动完成,应用pid存在，但应用无探活地址及日志探活路径")
	} else if app.AppLogPath != "" {
		if app.AppLogKeyWord == "" {
			return ErrCheck, errors.New("日志探活无探活关键字")
		}
		return LogKeywordCheck, nil
	} else {
		return HealthUrlCheck, nil
	}

}
func (app *StartApp) healthUrlCheckApp() error {
	statusCode := 200
	var detailError string
	for i := 0; i < 80; i++ {
		time.Sleep(3 * time.Second)
		if i == 19 { //再次查找pid，若不存在，则app没起来
			err := app.FindPid()
			if err != nil {
				return err
			}
		}
		resp, err := http.Get(app.HealthCheckUrl)
		if (err != nil) {
			utils.Logger().Warn("url:%v healthcheck err:%v", app.HealthCheckUrl, err)
			detailError = err.Error()
			continue
		}
		if resp.StatusCode == statusCode {
			utils.Logger().Info("url:%v healthcheck success!", app.HealthCheckUrl)
			resp.Body.Close()
			return nil
		}
		detailError = fmt.Sprintf("statusCode:%v", resp.StatusCode)
		utils.Logger().Warn("url:%v healthcheck fail statusCode:%v", app.HealthCheckUrl, resp.StatusCode)
		resp.Body.Close()

	}
	errInfo := fmt.Sprintf("url:%v healthcheck fail:%v", app.HealthCheckUrl, detailError)
	detailErr := fmt.Sprintf("app启动完成,应用pid存在，但探活失败：%v", errInfo)
	return errors.New(detailErr)

}
func (app *StartApp) FindPid() error {
	var cmd string
	switch app.PackageType {
	case Golang:
		cmd = fmt.Sprintf(findGoPidCmd, app.PackageName)
	case Jetty, Tomcat:
		cmd = fmt.Sprintf(findJavaPidCmd, app.ContainerPath)
	case Jar:
		cmd = fmt.Sprintf(findJavaPidCmd, app.PackageName)
	case SpringGz:
		packageName := fmt.Sprintf("%v/%v", app.PackagePath, app.PackageName)
		cmd = fmt.Sprintf(findJavaPidCmd, packageName)
	default:
		errInfo := fmt.Sprintf("此packageType:%v无查找pid命令", app.PackageType)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}
	pid, err := utils.ExecCommand(cmd)
	if err != nil {
		return err
	}
	if pid == "" {
		errInfo := fmt.Sprintf("app:%v程序可能有问题，应用没起来，请尝试手动启动！", app.PackageName)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}
	pid = strings.Replace(strings.Trim(pid, "\n"), "\n", " ", -1)
	utils.Logger().Info("app:%v最终pid:%v", app.PackageName, pid)
	return nil

}

func (app *StartApp) healthLogCheckApp() error {
	utils.Logger().Info("start探活日志路径:%v 关键字：%v", app.AppLogPath, app.AppLogKeyWord)
	tails, err := tail.TailFile(app.AppLogPath, tail.Config{
		Follow:    true, //tail -f
		MustExist: true, //若文件不存在，则报错
		Poll:      true,
		Location:  &tail.SeekInfo{0, os.SEEK_END}, //只打印最新的log，从启动这一刻起
	})
	if err != nil {
		utils.Logger().Warn("tail file err:", err)
		return err
	}

	timer := time.NewTicker(4 * 60 * time.Second)
	timerPid := time.NewTicker(60 * time.Second)
	defer func() {
		go tails.Stop()
		go timer.Stop()
		go timerPid.Stop()
	}()
	for {
		select {
		case msg, ok := <-tails.Lines:
			if !ok {
				utils.Logger().Warn("logFile:%v close", tails.Filename)
				time.Sleep(100 * time.Nanosecond)
				continue
			}
			utils.Logger().Info("app log:%v", msg.Text)
			if strings.Contains(msg.Text, app.AppLogKeyWord) {
				utils.Logger().Info("探活关键字：%v", app.AppLogKeyWord)
				utils.Logger().Info("日志:%v 探活成功 msg:%v", app.AppLogPath, msg.Text)
				return nil
			}
		case <-timer.C:
			utils.Logger().Info("stop timer...........")
			errInfo := fmt.Sprintf("日志:%v4分钟内探活关键字:%v失败", app.AppLogPath, app.AppLogKeyWord)
			detailErr := fmt.Sprintf("app启动完成,应用pid存在，但探活失败：%v", errInfo)
			utils.Logger().Warn(detailErr)
			return errors.New(detailErr)
		case <-timerPid.C:
			err = app.FindPid()
			if err != nil {
				return err
			}
		}
	}
}
func (app *StartApp) CheckIsSuccess() error {
	checkFlag, err := app.judgeCheckLogUrl()
	if err != nil {
		utils.Logger().Warn("app.judgeCheckLogUrl() fail:%v", err)
		return err
	}
	switch checkFlag {
	case HealthUrlCheck:
		return app.healthUrlCheckApp()
	case LogKeywordCheck:
		return app.healthLogCheckApp()
	default:
		errInfo := fmt.Sprintf("无此探活类型:%v", checkFlag)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)

	}

}
func (app *StartApp) CheckParam() error {
	var errInfo string
	if app.PackageName == "" {
		errInfo = fmt.Sprintf("packageName:%v is 空", app.PackageName)
	}
	if app.PackagePath == "" {
		errInfo = fmt.Sprintf("packagePath:%v is 空", app.PackagePath)
	}
	if app.Md5Name == "" {
		errInfo = fmt.Sprintf("md5Name:%v is 空", app.Md5Name)
	}
	if app.PackageType == "" {
		errInfo = fmt.Sprintf("packageType:%v is 空", app.PackageType)
	}
	if app.BuildType == "" {
		errInfo = fmt.Sprintf("buildType:%v is 空", app.BuildType)
	}
	if app.PackageUrl == "" {
		errInfo = fmt.Sprintf("packageUrl:%v is 空", app.PackageUrl)
	}
	if app.PackageType == Jetty || app.PackageType == Tomcat {
		if app.ContainerPath == "" {
			errInfo = fmt.Sprintf("containerPath:%v is 空", app.ContainerPath)
		}
	}
	if app.PackageType == SpringGz {
		if app.StartShellPath == "" {
			errInfo = "gz启动脚本is 空"
		}
		if app.StopShellPath == "" {
			errInfo = "gz停服脚本is 空"
		}
	}
	if errInfo != "" {
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}
	return nil

}
