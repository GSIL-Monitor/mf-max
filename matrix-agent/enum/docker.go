package enum

import (
	"matrix-agent/model"
	"matrix-agent/utils"
	"fmt"
	"errors"
	"strings"
	"os"
	"time"
)

func (docker DockerType) PullPackageInfo(param *model.StartApp) error {
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
	//packagePathTemp := fmt.Sprintf("%v/temp", param.PackagePath) //包的临时路径
	packagePathTemp := fmt.Sprintf("/data/backup/%v/docker/%v", param.BuildType, param.RecordId)
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
	//param.PackageName = strings.Replace(param.PackageName, ".zip", ".yaml", -1) //执行文件
	return nil
}
func (docker DockerType) MvFile(param *model.StartApp) error {
	cmd := fmt.Sprintf("cp -r %v/* %v", param.PackagePathTemp, param.PackagePath)
	outPut, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("exec mvFile cmd fail:%v", err)
		return err
	}
	utils.Logger().Info("exec mvFile cmd res:%v", outPut)
	return nil

}
func (docker DockerType) Start(param *model.StartApp) error {
	err := docker.MvFile(param)
	if err != nil {
		return err
	}
	out, err := utils.ExecCommand(param.DockerStartCmd)
	utils.Logger().Info("docker启动：%v out:%v", param.DockerStartCmd, out)
	if err != nil {
		utils.Logger().Error("docker启动：%v fail:%v", param.DockerStartCmd, err)
		errInfo := fmt.Sprintf("docker启动：%v fail:%v", param.DockerStartCmd, err)
		return errors.New(errInfo)
	}
	return nil

}
func (docker DockerType) ReStart(param *model.StartApp) error {
	return nil

}
func (docker DockerType) Stop(param *model.StartApp) error {
	return nil

}
func (docker DockerType) HandleAction(param *model.StartApp) error {
	switch param.Action {
	case "restart":
		return docker.ReStart(param)
	case "start":
		return docker.Start(param)
	case "stop":
		return docker.Stop(param)
	default:
		errInfo := fmt.Sprintf("docker应用无此种处理方式:%v", param.Action)
		utils.Logger().Warn(errInfo)
		return errors.New(errInfo)
	}

}
