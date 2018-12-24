package enum

import (
	"matrix-agent/model"
	"fmt"
	"matrix-agent/utils"
	"os"
	"errors"
	"strings"
	"syscall"
	"time"
)

/*
1,http请求拉取包信息
2，将压缩文件解压（zip文件）
一：
packageUrl:http://www.nginx.com/data/app/beta/matrix-process/123/matrix-process.jar
packagePath:/data/app/matrix-process
packageName:matrix-process.jar
二：
packageUrl:http://www.nginx.com/data/app/beta/grampus-wos-go/125/grampus-wos-go.zip
packagePath:/data/app/grampus-wos-go
packageName:grampus-wos-go.zip
 */

func (golang GolangType) PullPackageInfo(param *model.StartApp) error {
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
	utils.Logger().Info("sleep 2s............")
	time.Sleep(2 * time.Second)
	if !utils.PathExists(param.PackagePath) {
		err = os.MkdirAll(param.PackagePath, os.ModePerm)
		if err != nil {
			utils.Logger().Error("os.MkdirAll path:%v faill:%v", param.PackagePath, err)
			return err
		}
	}
	//packagePathTemp := fmt.Sprintf("%v/temp", param.PackagePath) //包的临时路径
	packagePathTemp := fmt.Sprintf("/data/backup/%v/go/%v", param.BuildType, param.RecordId)

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
		errInfo := fmt.Sprintf("packagePath：%v下文件md5sum fail", packagePathTemp)
		utils.Logger().Warn("packagePath：%v下文件md5sum fail", packagePathTemp)
		return errors.New(errInfo)
	}
	return nil
}
func (golang GolangType) FindInfo(param string) (string, error) {
	pid, err := utils.ExecCommand(param)
	if err != nil {
		return "", err
	}
	if pid == "" {
		return "", nil
	}
	//res:"\n13320\n"
	pid = strings.Replace(strings.Trim(pid, "\n"), "\n", " ", -1)
	return pid, nil
}

//echo `ps aux | grep "${APP_NAME} -v"|grep -v grep|awk '{ print $2}'
func (golang GolangType) FindPid(packageName string) (string, error) {
	cmd := fmt.Sprintf("ps aux --width=1000 | grep -v 'ps aux' |grep -v grep | grep '%v -v' |awk '{print $2}'", packageName)
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

/*
	kill应用进程：
	1，查找pid
	2,kill  pid
	3,检测pid是否存在，若存在 kill -9 pid
 */
func (golang GolangType) Stop(param *model.StartApp) error {
	pid, err := golang.FindPid(param.PackageName)
	if err != nil {
		return err
	}
	if pid == "" {
		return nil
	}

	utils.Logger().Info("start kill app:%v对应的pids:%v了", param.PackageName, pid)
	cmd := fmt.Sprintf("kill %v", pid)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("kill pid:%v fail:%v", pid, err)
		return err

	}
	utils.Logger().Info("sleep 5s............")
	time.Sleep(5 * time.Second)
	pidTemp, err := golang.FindPid(param.PackageName)
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
	pidTemp, err = golang.FindPid(param.PackageName)
	if err != nil {
		return err
	}
	if pidTemp != "" {
		errInfo := fmt.Sprintf("stop fail:kill again pid:%v fail", pidTemp)
		utils.Logger().Warn("stop fail:kill again pid:%v fail", pidTemp)
		return errors.New(errInfo)
	}
	return nil
}

/*
	nohup /data/app/${APP_NAME}/${APP_NAME} -v ${build_type} -c ./data/app/${APP_NAME}/config.yaml > /dev/null 2>&1 &
	启动应用：
	1，启动
	2，检测应用是否启动成功，若不成功再次启动
 */
func (golang GolangType) Start(param *model.StartApp) error {
	cmd := "source /etc/profile"
	_, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Error("source /etc/profile fail:%v", err)
		return err
	}
	cmd = fmt.Sprintf("nohup %v/%v -v %v -c .%v/config.yaml > /dev/null 2>&1 &", param.PackagePath, param.PackageName, param.BuildType, param.PackagePath)
	utils.Logger().Info("开始启动app:%v", param.PackageName)
	err = golang.StartCmd(param, cmd)
	if err != nil {
		return err
	}

	utils.Logger().Info("slepp 5s...............")
	time.Sleep(5 * time.Second)
	pid, err := golang.FindPid(param.PackageName)
	if err != nil {
		utils.Logger().Warn("exec cmd:%v fail:%v", cmd, err)
		return err
	}
	if pid != "" {
		utils.Logger().Info("app:%v启动成功pid:%v", param.PackageName, pid)
		return nil
	}
	for i := 0; i < 30; i++ {
		err = golang.StartCmd(param, cmd)
		if err != nil {
			return err
		}
		utils.Logger().Info("slepp 5s...............")
		time.Sleep(5 * time.Second)
		pid, err = golang.FindPid(param.PackageName)
		if err != nil {
			return err
		}
		if pid != "" {
			utils.Logger().Info("app:%v启动成功pid:%v", param.PackageName, pid)
			return nil
		}
	}
	errInfo := fmt.Sprintf("start app:%v 已经尽力了，试试手动起go文件，看程序是否有问题!!", param.PackageName)
	utils.Logger().Warn("start app:%v 已经尽力了，试试手动起go文件，看程序是否有问题!!", param.PackageName)
	return errors.New(errInfo)
}

//
func (golang GolangType) MvFile(param *model.StartApp) error {
	cmd := fmt.Sprintf("cp -r %v/* %v", param.PackagePathTemp, param.PackagePath)
	outPut, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("exec mvFile cmd fail:%v", err)
		return err
	}
	utils.Logger().Info("exec mvFile cmd res:%v", outPut)
	return nil

}
func (golang GolangType) StartCmd(param *model.StartApp, startCmd string) error {
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
	_, err = utils.ExecCommand(startCmdSh)
	return err
}
func (golang GolangType) ReStart(param *model.StartApp) error {
	err := golang.Stop(param)
	if err != nil {
		return err
	}
	err = golang.MvFile(param)
	if err != nil {
		return err
	}
	err = golang.Start(param)
	return err
}

func (golang GolangType) RestartAgent() error {
	pid := syscall.Getpid()
	utils.Logger().Info("now pid=============:%v", pid)
	err := syscall.Kill(pid, syscall.SIGHUP)
	if err != nil {
		utils.Logger().Error("给pid:%v发送SIGHUP信号fail:%v", pid, err)
		return err
	}

	return nil
}
func (golang GolangType) HandleAction(param *model.StartApp) error {
	switch param.Action {
	case "restart":
		return golang.ReStart(param)
	case "stop":
		return golang.Stop(param)
	default:
		errInfo := fmt.Sprintf("go应用无此种处理方式:%v", param.Action)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}

}
