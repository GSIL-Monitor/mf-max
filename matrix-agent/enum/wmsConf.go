package enum

import (
	"strings"
	"fmt"
	"matrix-agent/model"
	"matrix-agent/utils"
	"os"
	"errors"
	"time"
)

func (wmconf WmsConfType) PullPackageInfo(param *model.StartApp) error {
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
	packagePathTemp := fmt.Sprintf("/data/backup/%v/wmsconf/%v", param.BuildType, param.RecordId)
	if !utils.PathExists(packagePathTemp) {
		err = os.MkdirAll(packagePathTemp, os.ModePerm)
		if err != nil {
			utils.Logger().Error("os.MkdirAll path:%v faill:%v", packagePathTemp, err)
			return err
		}
	}
	param.PackagePathTemp = packagePathTemp
	cmd := fmt.Sprintf("cd %v && rm -rf *", packagePathTemp)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("exec cmd:%v fail:%v", cmd, err)
		return err
	}
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
	cmd = fmt.Sprintf("cd %v && md5sum -c %v", packagePathTemp, param.Md5Name)
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
	//解压
	jsName := strings.Replace(param.PackageName, "-md5", "", -1)
	jsFileName := fmt.Sprintf("%v/%v", packagePathTemp, jsName)
	if !utils.IsZip(jsFileName) {
		errInfo := fmt.Sprintf("fileName:%v is not zip file", jsFileName)
		utils.Logger().Warn("fileName:%v is not zip file", jsFileName)
		return errors.New(errInfo)
	}
	utils.Logger().Info("jsFileNameZip:%v start unzip.............", jsFileName)
	err = utils.Unzip(jsFileName, packagePathTemp)
	if err != nil {
		utils.Logger().Warn("解压%v文件fail:%v", jsFileName, err)
		return err
	}
	utils.Logger().Info("sleep 1s............")
	time.Sleep(1 * time.Second)
	return nil
}

/*
//--------------- 目标机器
startallserver.sh
echo "start t1 server"
sudo service t1 start
echo "start t3 server"
sudo service t3 start
echo "sleep 20s"
sleep 20s
echo "start nginx server"
sudo service nginx start
echo "start all server finish"

//----- 目标机器
stopallserver.sh
echo "stop nginx server"
sudo service nginx stop
echo "stop t1 server"
sudo service t1 stop
echo "stop t3 server"
sudo service t3 stop

 */
func (wmconf WmsConfType) Start(param *model.StartApp) error {
	var jsT1service string
	var jsT2service string

	var cmd string
	if param.WmsJsService != "" {
		cmd = fmt.Sprintf("sudo service %v start", param.WmsJsService)
		jsT1service = param.WmsJsService
	} else {
		cmd = "sudo service t1 start && sudo service t3 start"
		jsT1service = "t1"
		jsT2service = "t3"

	}

	_, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("start %v %v fail:%v", jsT1service, jsT2service, err)
		errInfo := fmt.Sprintf("start %v %v fail:%v", jsT1service, jsT2service, err)
		return errors.New(errInfo)
	}
	if jsT1service != "" {
		pid, err := wmconf.FindPid(jsT1service + "/tomcat")
		if err != nil {
			utils.Logger().Warn("wmsJsService:%v start fail:%v", jsT1service, err)
			return err
		}
		if pid == "" {
			errInfo := fmt.Sprintf("wmsJsService:%v start fail pid is nil", jsT1service)
			utils.Logger().Warn(errInfo)
			return errors.New(errInfo)
		}
	}
	if jsT2service != "" {
		pid, err := wmconf.FindPid(jsT2service + "/tomcat")
		if err != nil {
			utils.Logger().Warn("wmsJsService:%v start fail:%v", jsT2service, err)
			return err
		}
		if pid == "" {
			errInfo := fmt.Sprintf("wmsJsService:%v start fail pid is nil", jsT2service)
			utils.Logger().Warn(errInfo)
			return errors.New(errInfo)
		}
	}
	if param.BuildType == Release.String() {
		utils.Logger().Info("start sleep 10s........")
		time.Sleep(10 * time.Second)
		cmd = "sudo service nginx start"
		_, err = utils.ExecCommand(cmd)
		if err != nil {
			utils.Logger().Error("start nginx server fail:%v", err)
			return err
		}
		nginxPid, err := wmconf.FindPid("nginx -")
		if err != nil {
			utils.Logger().Error("start nginx server fail:%v", err)
			return err
		}
		if nginxPid == "" {
			utils.Logger().Error("start nginx server fail")
			return errors.New("nginx start fail")
		}
	}
	utils.Logger().Info("start app:%v %v success", jsT1service, jsT2service)
	return nil
}
func (wmconf WmsConfType) Stop(param *model.StartApp) error {
	var jsT1service string
	var jsT2service string
	var cmd string
	if param.BuildType == Release.String() {

		cmd = "sudo service nginx stop"
		_, err := utils.ExecCommand(cmd)
		if err != nil {
			utils.Logger().Error("stop nginx server fail:%v", err)
			return err
		}
		nginxPid, err := wmconf.FindPid("nginx -")
		if err != nil {
			utils.Logger().Error("stop nginx server fail:%v", err)
			return err
		}
		if nginxPid != "" {
			utils.Logger().Error("stop nginx server fail")
			return errors.New("nginx stop fail")
		}
	}
	if param.WmsJsService != "" {
		cmd = fmt.Sprintf("sudo service %v stop", param.WmsJsService)
		jsT1service = param.WmsJsService
	} else {
		cmd = "sudo service t1 stop && sudo service t3 stop"
		jsT1service = "t1"
		jsT2service = "t3"

	}

	_, err := utils.ExecCommand(cmd)
	if err != nil {
		return err
	}
	if jsT1service != "" {
		pid, err := wmconf.FindPid(jsT1service + "/tomcat")
		if err != nil {
			utils.Logger().Warn("wmsJsService:%v stop fail:%v", jsT1service, err)
			return err
		}
		if pid != "" {
			errInfo := fmt.Sprintf("wmsJsService:%v stop pid:%v fail", jsT1service, pid)
			utils.Logger().Warn("wmsJsService:%v stop pid:%v fail", jsT1service, pid)
			return errors.New(errInfo)
		}
	}
	if jsT2service != "" {
		pid, err := wmconf.FindPid(jsT2service + "/tomcat")
		if err != nil {
			utils.Logger().Warn("wmsJsService:%v stop fail:%v", jsT2service, err)
			return err
		}
		if pid != "" {
			errInfo := fmt.Sprintf("wmsJsService:%v stop pid:%v fail", jsT2service, pid)
			utils.Logger().Warn("wmsJsService:%v stop pid:%v fail", jsT2service, pid)
			return errors.New(errInfo)
		}
	}

	utils.Logger().Info("stop app:%v %v success", jsT1service, jsT2service)
	return nil
}

//echo `ps aux | grep "${APP_NAME} -v"|grep -v grep|awk '{ print $2}'
func (wmconf WmsConfType) FindPid(packageName string) (string, error) {
	cmd := fmt.Sprintf("ps aux | grep '%v' | grep -v 'ps aux' |grep -v grep|awk '{ print $2}'", packageName)
	pid, err := utils.ExecCommand(cmd)
	if err != nil {
		return "", err
	}
	if pid == "" {
		return "", nil
	}
	//res:"\n13320\n"
	pid = strings.Trim(pid, "\n")
	pid = strings.Replace(pid, "\n", " ", -1)
	return pid, nil
}

//rsync -avz --delete --exclude-from  "/data/app/include.txt" /data/app/test1/  /data/app/test/
///data/app/temp
func (wmconf WmsConfType) RsyncDeleteExclude(param *model.StartApp) error {
	if param.PackagePath != "" {
		if string(param.PackagePath[len(param.PackagePath)-1]) != "/" {
			param.PackagePath = fmt.Sprintf("%v/", param.PackagePath)
		}

	}
	//cmd := fmt.Sprintf("nohup rsync -avz --delete --exclude-from  /data/app/temp/exclude /data/app/temp/  %v &", param.PackagePath)
	cmd := fmt.Sprintf("nohup rsync -avz --delete --exclude-from %v/exclude %v/ %v &", param.PackagePathTemp, param.PackagePathTemp, param.PackagePath)

	_, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("rsync data:%v fail：%v", cmd, err)
		return err
	}
	utils.Logger().Info("start sleep 10s........")
	time.Sleep(10 * time.Second)
	return nil
}
func (wmconf WmsConfType) Restart(param *model.StartApp) error {
	err := wmconf.Stop(param)
	if err != nil {
		return err
	}
	err = wmconf.RsyncDeleteExclude(param)
	if err != nil {
		return err
	}
	err = wmconf.Start(param)
	return err
}
func (wmconf WmsConfType) HandleAction(param *model.StartApp) error {

	switch param.Action {
	case "restart":
		return wmconf.Restart(param)
	case "stop":
		return wmconf.Stop(param)
	default:
		errInfo := fmt.Sprintf("go应用无此种处理方式:%v", param.Action)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}

}
