package enum

import (
	"matrix-agent/model"
	"fmt"
	"matrix-agent/utils"
	"os"
	"strings"
	"errors"
	"time"
)

/*
1,http请求拉取包信息
2，将压缩文件解压（zip文件）
一：
packageUrl:http://www.nginx.com/data/app/beta/matrix-process/123/matrix-process.zip
packagePath:/data/app/matrix-process
packageName:matrix-process.zip
二：
packageUrl:http://www.nginx.com/data/app/beta/grampus-wos-go/125/grampus-wos-go.zip
packagePath:/data/app/grampus-wos-go
packageName:grampus-wos-go.zip
 */

func (jetty JettyType) PullPackageInfo(param *model.StartApp) error {
	resBytes, err := utils.Get(param.PackageUrl)
	if err != nil {
		for i := 0; i < 50; i++ {
			resBytes, err = utils.Get(param.PackageUrl)
			if err == nil {
				break
			}
		}
		if err != nil {
			return err
		}
	}
	if param.PackageName == "" {
		utils.Logger().Warn("packageName is 空")
		return errors.New("packageName is 空")
	}
	if param.PackagePath == "" {
		utils.Logger().Warn("packagePath is 空")
		return errors.New("packagePath is 空")
	}
	utils.Logger().Info("sleep 2s............")
	time.Sleep(2 * time.Second)
	if !utils.PathExists(param.PackagePath) {
		err = os.MkdirAll(param.PackagePath, os.ModePerm)
		if err != nil {
			utils.Logger().Error("os.MkdirAll path:%v faill:%v", param.PackagePath, err)
			return err
		}
	}
	packagePathTemp := fmt.Sprintf("/data/backup/%v/jetty/%v", param.BuildType, param.RecordId)

	if !utils.PathExists(packagePathTemp) {
		err = os.MkdirAll(packagePathTemp, os.ModePerm)
		if err != nil {
			utils.Logger().Error("os.MkdirAll path:%v faill:%v", packagePathTemp, err)
			return err
		}
	}
	param.PackagePathTemp = packagePathTemp
	fileName := fmt.Sprintf("%v/%v", packagePathTemp, param.PackageName)

	//strings
	utils.Logger().Info("fileName:%v", fileName)
	f, err := utils.CreateFile(fileName)
	if err != nil {
		utils.Logger().Error("os.OpenFile file:%v faill:%v", fileName, err)
		return err
	}
	defer f.Close()
	_, err = f.Write(resBytes)
	if err != nil {
		utils.Logger().Error("f.Write faill:%v", err)
		return err
	}
	if !utils.IsZip(fileName) {
		errInfo := fmt.Sprintf("fileName:%v is not zip file", fileName)
		utils.Logger().Warn("fileName:%v is not zip file", fileName)
		return errors.New(errInfo)
	}
	utils.Logger().Info("fileNameZip:%v start unzip.............", fileName)

	//解压压缩文件
	err = utils.Unzip(fileName, packagePathTemp)
	if err != nil {
		utils.Logger().Warn("解压%v文件fail:%v", fileName, err)
		return err
	}
	utils.Logger().Info("fileNameZip:%v unzip end...............", fileName)
	//md5sum 校验md5文件
	utils.Logger().Info("sleep 5s............")
	time.Sleep(5 * time.Second)
	cmd := fmt.Sprintf("cd %v && md5sum -c %v", packagePathTemp, param.Md5Name)
	output, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("md5sum文件：%v校验fail:%v", param.Md5Name, err)
		return err
	}
	if strings.Contains(output, "FAILED") {
		errInfo := fmt.Sprintf("packagePathTemp：%v下文件md5sum fail", packagePathTemp)
		utils.Logger().Warn("packagePathTemp：%v下文件md5sum fail", packagePathTemp)
		return errors.New(errInfo)
	}
	param.PackageName = strings.Replace(param.PackageName, ".zip", "", -1)
	if param.ContainerPath == "" {
		utils.Logger().Warn("containerPath is 空")
		return errors.New("containerPath is 空")
	}
	if utils.JudgeLastIsContain(param.ContainerPath, "/") {
		param.ContainerPath = param.ContainerPath[:len(param.ContainerPath)-1]
	}
	packageLen := len(param.PackageName)
	war := param.PackageName[packageLen-4 : packageLen]
	if war != ".war" {
		errInfo := fmt.Sprintf("app:%v不是一个war包", param.PackageName)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}
	param.PackageName = param.PackageName[:packageLen-4]
	if utils.JudgeLastIsContain(param.PackagePath, "/") {
		param.PackagePath = param.PackagePath[:len(param.PackagePath)-1]
	}
	return nil
}
func (jetty JettyType) ReStart(param *model.StartApp) error {
	err := jetty.Stop(param)
	if err != nil {
		return err
	}
	err = jetty.Clear(param)
	if err != nil {
		return err
	}
	err = jetty.MvFile(param)
	if err != nil {
		return err
	}
	err = jetty.Start(param)
	return err
}

/*
    cd ${JETTY_PATH}
    nohup ./bin/jetty.sh start &

 */
func (jetty JettyType) Start(param *model.StartApp) error {
	cmd := "source /etc/profile"
	_, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Error("source /etc/profile fail:%v", err)
		return err
	}
	cmd = fmt.Sprintf("nohup %v/bin/jetty.sh start &", param.ContainerPath)
	utils.Logger().Info("开始启动app:%v", param.PackageName)
	err = jetty.StartCmd(param, cmd)
	if err != nil {
		return err
	}
	utils.Logger().Info("slepp 4s...............")
	time.Sleep(4 * time.Second)
	pid, err := jetty.FindPid(param.ContainerPath)
	if err != nil {
		utils.Logger().Warn("exec cmd:%v fail:%v", cmd, err)
		return err
	}
	if pid != "" {
		utils.Logger().Info("app:%v启动成功pid:%v", param.PackageName, pid)
		return nil
	}
	for i := 0; i < 30; i++ {
		err = jetty.StartCmd(param, cmd)
		if err != nil {
			return err
		}
		utils.Logger().Info("slepp 4s...............")
		time.Sleep(4 * time.Second)
		pid, err = jetty.FindPid(param.ContainerPath)
		if err != nil {
			return err
		}
		if pid != "" {
			utils.Logger().Info("app:%v启动成功pid:%v", param.PackageName, pid)
			return nil
		}
	}
	errInfo := fmt.Sprintf("start app:%v 已经尽力了，试试手动起jetty包，看程序是否有问题!", param.PackageName)
	utils.Logger().Warn("start app:%v 已经尽力了，试试手动起jetty包，看程序是否有问题!", param.PackageName)
	return errors.New(errInfo)
}

/*
1,find pid
2,stop jetty.sh
3,find pid
4,kill -9 pid
 */
func (jetty JettyType) Stop(param *model.StartApp) error {
	pid, err := jetty.FindPid(param.ContainerPath)
	if err != nil {
		return err
	}

	if pid == "" {
		return nil
	}

	utils.Logger().Info("start stop app:%v对应的pids:%v了", param.PackageName, pid)
	cmd := fmt.Sprintf("source /etc/profile && %v/bin/jetty.sh stop", param.ContainerPath)
	_, err = utils.ExecCommandNoErr(cmd)
	if err != nil {
		utils.Logger().Warn("kill pid:%v fail:%v", pid, err)
		return err

	}
	utils.Logger().Info("sleep 10s............")
	time.Sleep(10 * time.Second)
	pidTemp, err := jetty.FindPid(param.ContainerPath)
	if err != nil {
		utils.Logger().Warn("exec cmd:%v fail:%v", cmd, err)
		return err
	}
	if pidTemp == "" {
		utils.Logger().Info("已成功kill app:%v的pid:%v", param.PackageName, pid)
		return nil
	}
	cmd = fmt.Sprintf("kill -9 %v", pidTemp)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("exec cmd:%v fail:%v", cmd, err)
		return err
	}
	utils.Logger().Info("sleep 5s............")
	time.Sleep(5 * time.Second)
	pid, err = jetty.FindPid(param.ContainerPath)
	if err != nil {
		return err
	}
	if pid != "" {
		errInfo := fmt.Sprintf("stop fail:kill again pid:%v fail", pid)
		utils.Logger().Warn("stop fail:kill again pid:%v fail", pid)
		return errors.New(errInfo)
	}
	utils.Logger().Info("已成功kill -9 app:%v的pid:%v", param.PackageName, pidTemp)
	return nil
}
func (jetty JettyType) StartCmd(param *model.StartApp, startCmd string) error {
	startCmdSh := fmt.Sprintf("%v/%v.sh", param.PackagePath, param.PackageName)
	utils.Logger().Info("startCmdSh:%v", startCmdSh)
	f, err := utils.CreateFile(startCmdSh)
	if err != nil {
		utils.Logger().Warn("create file:%v fail", startCmdSh, err)
		return err
	}
	cnt := startCmdHeader + startCmd + "\n"
	utils.Logger().Info("startCmdSh cnt:%v", cnt)
	_, err = f.WriteString(cnt)
	if err != nil {
		utils.Logger().Warn("f.WriteString:%v fail", startCmdSh, err)
		return err
	}
	f.Close()
	err = utils.ExecCommandAsync(startCmdSh)
	return err
}
func (jetty JettyType) FindPid(packageName string) (string, error) {
	cmd := fmt.Sprintf("ps aux --width=1000 | grep java | grep -v 'ps aux' | grep -v grep | grep %v | awk '{print $2}'", packageName)
	pid, err := utils.ExecCommand(cmd)
	if err != nil {
		return "", err
	}
	if pid == "" {
		return "", nil
	}
	pid = strings.Replace(strings.Trim(pid, "\n"), "\n", " ", -1)
	return pid, nil
}

func (jetty JettyType) Clear(param *model.StartApp) error {

	appDirPath := fmt.Sprintf("%v/%v", param.PackagePath, param.PackageName)
	cmd := fmt.Sprintf("rm -rf %v*", appDirPath)
	_, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("rm -rf %v* fail:%v", appDirPath, err)
		return err
	}
	return nil
}

func (jetty JettyType) MvFile(param *model.StartApp) error {
	cmd := fmt.Sprintf("cp -r %v/* %v", param.PackagePathTemp, param.PackagePath)
	outPut, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("exec mvFile cmd fail:%v", err)
		return err
	}
	utils.Logger().Info("exec mvFile cmd res:%v", outPut)
	return nil

}
func (jetty JettyType) HandleAction(param *model.StartApp) error {
	switch param.Action {
	case "restart":
		return jetty.ReStart(param)
	case "stop":
		return jetty.Stop(param)
	default:
		errInfo := fmt.Sprintf("jar应用无此种处理方式:%v", param.Action)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}

}
