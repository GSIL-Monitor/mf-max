package enum

import (
	"matrix-agent/model"
	"matrix-agent/utils"
	"fmt"
	"errors"
	"os"
	"strings"
	"time"
	"matrix-agent/config"
)

func (sg SpringGzType) PullPackageInfo(param *model.StartApp) error {
	err := sg.CheckEnvInfo()
	if err != nil {
		return err
	}
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
	packagePathTemp := fmt.Sprintf("/data/backup/%v/dubbo/%v", param.BuildType, param.RecordId)
	if !utils.PathExists(packagePathTemp) {
		err = os.MkdirAll(packagePathTemp, os.ModePerm)
		if err != nil {
			utils.Logger().Error("os.MkdirAll path:%v faill:%v", packagePathTemp, err)
			return err
		}
	}
	param.PackagePathTemp = packagePathTemp
	md5FileName := fmt.Sprintf("%v/%v", packagePathTemp, param.PackageName)

	//strings
	utils.Logger().Info("md5FileName:%v", md5FileName)
	f, err := utils.CreateFile(md5FileName)
	if err != nil {
		utils.Logger().Error("os.OpenFile file:%v faill:%v", md5FileName, err)
		return err
	}
	defer f.Close()
	_, err = f.Write(resBytes)
	if err != nil {
		utils.Logger().Error("f.Write faill:%v", err)
		return err
	}
	if !utils.IsZip(md5FileName) {
		errInfo := fmt.Sprintf("fileName:%v is not zip file", md5FileName)
		utils.Logger().Warn("fileName:%v is not zip file", md5FileName)
		return errors.New(errInfo)
	}
	utils.Logger().Info("md5FileNameZip:%v start unzip.............", md5FileName)

	//解压压缩文件
	err = utils.Unzip(md5FileName, packagePathTemp)
	if err != nil {
		utils.Logger().Warn("解压%v文件fail:%v", md5FileName, err)
		return err
	}
	utils.Logger().Info("md5FileNameZip:%v unzip end...............", md5FileName)
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
	//解压tar.gz文件
	targzName := strings.Replace(param.PackageName, ".zip", "", -1)

	targzFileName := fmt.Sprintf("%v/%v", packagePathTemp, targzName)
	if !utils.IsTargz(targzFileName) {
		errInfo := fmt.Sprintf("fileName:%v is not targz file", targzFileName)
		utils.Logger().Warn("fileName:%v is not targz file", targzFileName)
		return errors.New(errInfo)
	}
	utils.Logger().Info("targzFileName:%v start untargz.............", targzFileName)
	cmd = fmt.Sprintf("tar -zxvf %v -C %v", targzFileName, param.PackagePath)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("解压%v文件fail:%v", targzFileName, err)
		return err
	}
	utils.Logger().Info("targzFileName:%v untargz end...............", targzFileName)
	param.PackageName = strings.Replace(param.PackageName, ".tar.gz.zip", "", -1)
	utils.Logger().Info("sleep 1s............")
	time.Sleep(1 * time.Second)
	return nil
}
func (sg SpringGzType) Start(param *model.StartApp) error {
	utils.Logger().Info("start dubbo:%v", param.StartShellPath)
	err := utils.ExecCommandAsync(param.StartShellPath)
	if err != nil {
		utils.Logger().Warn("start dubbo：%v fail:%v", param.StartShellPath, err)
		errInfo := fmt.Sprintf("start dubbo：%v fail:%v", param.StartShellPath, err)
		return errors.New(errInfo)
	}
	utils.Logger().Info("sleep 4s.......")
	time.Sleep(4 * time.Second)
	//packageName := fmt.Sprintf("%v/%v/conf", param.PackagePath, param.PackageName)
	packageName := fmt.Sprintf("%v/%v", param.PackagePath, param.PackageName)
	pid, err := sg.FindPid(packageName)
	if err != nil {
		utils.Logger().Warn("start dubbo：%v fail:%v", param.StopShellPath, err)
		return err
	}
	if pid != "" {
		utils.Logger().Info("已成功start dubbo:%v pid:%v", param.PackageName, pid)
		return nil
	}
	utils.Logger().Warn("未知原因导致启动失败")
	return errors.New("未知原因导致启动失败")

}
func (sg SpringGzType) StartPickTraffic(param *model.StartApp) error {
	if param.BuildType == Release.String() {
		err := sg.PickTraffic()
		if err != nil {
			return err
		}
	}
	return nil
}
func (sg SpringGzType) StartHangTraffic(param *model.StartApp) error {
	if param.BuildType == Release.String() {
		err := sg.HangTraffic()
		if err != nil {
			return err
		}
	}
	return nil
}
func (sg SpringGzType) ReStart(param *model.StartApp) error {
	err := sg.StartPickTraffic(param)
	if err != nil {
		return err
	}
	err = sg.Stop(param)
	if err != nil {
		return err
	}
	err = sg.Start(param)
	if err != nil {
		return err
	}
	err = sg.StartHangTraffic(param)
	return err

}
func (sg SpringGzType) Stop(param *model.StartApp) error {
	utils.Logger().Info("stop dubbo:%v ", param.StopShellPath)
	_, err := utils.ExecCommandNoErr(param.StopShellPath)
	if err != nil {
		utils.Logger().Warn("stop dubbo：%v fail:%v", param.StopShellPath, err)
		errInfo := fmt.Sprintf("stop dubbo：%v fail:%v", param.StopShellPath, err)
		return errors.New(errInfo)
	}
	utils.Logger().Info("sleep 10s........")
	time.Sleep(10 * time.Second)
	packageName := fmt.Sprintf("%v/%v", param.PackagePath, param.PackageName)
	pid, err := sg.FindPid(packageName)
	if err != nil {
		utils.Logger().Warn("stop dubbo：%v fail:%v", param.StopShellPath, err)
		return err
	}
	if pid == "" {
		utils.Logger().Info("已成功stop dubbo:%v", param.PackageName)
		return nil
	}
	utils.Logger().Warn("未知原因导致stop失败")
	return errors.New("未知原因导致stop失败")
}
func (sg SpringGzType) FindPid(packageName string) (string, error) {
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
func (sg SpringGzType) CheckEnvInfo() error {
	//判断nc命令是否存在
	_, err := utils.ExecCommand("man nc") //若存在输出详细信息
	if err != nil {
		utils.Logger().Warn("%v", err)
		return err
	}
	return nil
}

//摘流量
func (sg SpringGzType) PickTraffic() error {
	ip := utils.GetLocalIp()
	if ip == "" {
		utils.Logger().Warn("获得本机eth0网卡对应的ip is nil")
		return errors.New("获得本机eth0网卡对应的ip is nil")
	}
	url := fmt.Sprintf("%v%v", config.Traffic.PickTrafficUrl, ip)
	resBytes, err := utils.Get(url)
	if err != nil {
		for i := 0; i < 50; i++ {
			resBytes, err = utils.Get(url)
			if err == nil {
				break
			}
		}
		if err != nil {
			return err
		}
	}
	if string(resBytes) != "ok" {
		utils.Logger().Warn("摘dubbo流量:%v fail:%v", url, string(resBytes))
		errInfo := fmt.Sprintf("摘dubbo流量:%v fail:%v", url, string(resBytes))
		//摘失败，删除文件
		cmd := "rm -rf /data/logs/online_check"
		_, err = utils.ExecCommand(cmd)
		if err != nil {
			utils.Logger().Warn("rm -rf /data/logs/online_check fail:%v", err)
			return err
		}
		return errors.New(errInfo)
	}
	utils.Logger().Info("摘dubbo流量:%v success", url, string(resBytes))
	return nil

}

//挂流量
func (sg SpringGzType) HangTraffic() error {
	ip := utils.GetLocalIp()
	if ip == "" {
		utils.Logger().Warn("获得本机eth0网卡对应的ip is nil")
		return errors.New("获得本机eth0网卡对应的ip is nil")
	}
	url := fmt.Sprintf("%v%v", config.Traffic.HangTrafficUrl, ip)
	resBytes, err := utils.Get(url)
	if err != nil {
		for i := 0; i < 50; i++ {
			resBytes, err = utils.Get(url)
			if err == nil {
				break
			}
		}
		if err != nil {
			return err
		}
	}
	if string(resBytes) != "ok" {
		utils.Logger().Warn("挂dubbo流量:%v fail:%v", url, string(resBytes))
		errInfo := fmt.Sprintf("挂dubbo流量:%v fail:%v", url, string(resBytes))
		//摘失败，删除文件
		cmd := "rm -rf /data/logs/online_check"
		_, err = utils.ExecCommand(cmd)
		if err != nil {
			utils.Logger().Warn("rm -rf /data/logs/online_check fail:%v", err)
			return err
		}
		return errors.New(errInfo)
	}
	utils.Logger().Info("挂dubbo流量:%v success", url, string(resBytes))
	return nil

}
func (sg SpringGzType) HandleAction(param *model.StartApp) error {
	switch param.Action {
	case "restart":
		return sg.ReStart(param)
	case "start":
		return sg.Start(param)
	case "stop":
		return sg.Stop(param)
	default:
		errInfo := fmt.Sprintf("targz应用无此种处理方式:%v", param.Action)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}

}
