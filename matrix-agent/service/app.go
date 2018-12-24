package service

import (
	"matrix-agent/enum"
	"matrix-agent/model"
	"matrix-agent/utils"
	"fmt"
	"errors"
	"strings"
)

type AppService struct {
}

func NewAppService() *AppService {
	return &AppService{}
}

func (appService *AppService) StartApp(param *model.StartApp) error {
	err := appService.CheckEnvInfo()
	if err != nil {
		return err
	}
	err = appService.HandlePackageInfo(param)
	if err != nil {
		return err
	}
	utils.Logger().Info("packagePath:%v", param.PackagePath)
	if param.PackageType == enum.Static.String() || param.PackageType == enum.WmsWwwRoot.String() {
		return nil
	}
	switch param.BuildType {
	case enum.Beta.String(), enum.Test.String(), enum.Dev.String(), enum.AutoTest.String(), enum.Stress.String(), enum.Presstest.String():
		return enum.Beta.HandleBetaType(param)
	case enum.Release.String(), enum.Prod.String():
		return enum.Release.HandleReleaseType(param)
	default:
		errInfo := fmt.Sprintf("startApp 参数错误 buildType =%v", param.BuildType)
		utils.Logger().Warn("startApp 参数错误 buildType =%v", param.BuildType)
		return errors.New(errInfo)
	}
	return nil
}

func (appService *AppService) HandlePackageInfo(param *model.StartApp) (error) {
	switch param.PackageType {
	case enum.Jar.String():
		return enum.Jar.PullPackageInfo(param)
	case enum.Jetty.String():
		return enum.Jetty.PullPackageInfo(param)
	case enum.Tomcat.String():
		return enum.Tomcat.PullPackageInfo(param)
	case enum.Golang.String():
		return enum.Golang.PullPackageInfo(param)
	case enum.Static.String():
		return enum.Static.PullPackageInfo(param)
	case enum.WmsWwwRoot.String():
		return enum.WmsWwwRoot.PullPackageInfo(param)
	case enum.Wmsconf.String():
		return enum.Wmsconf.PullPackageInfo(param)
	case enum.Agent.String():
		return nil
	case enum.Docker.String():
		return enum.Docker.PullPackageInfo(param)
	case enum.SpringGz.String():
		return enum.SpringGz.PullPackageInfo(param)
	case enum.JarMain.String():
		return enum.JarMain.PullPackageInfo(param)

	default:
		return nil

	}
}
func (appService *AppService) CheckEnvInfo() error {
	//判断该系统登陆用户是否是www用户
	loginUser, err := appService.FindInfo("cd /data/app && whoami")
	if err != nil {
		utils.Logger().Error("jar.FindInfo(whoami) fail:%v", err)
		return err
	}
	utils.Logger().Info("当前系统登陆用户是:%v", loginUser)
	if loginUser != "www" {
		errInfo := fmt.Sprintf("当前系统登陆用户不是www用户，而是:%v用户", loginUser)
		utils.Logger().Warn("当前系统登陆用户不是www用户，而是:%v用户", loginUser)
		return errors.New(errInfo)
	}
	//判断/data目录是否存在及所属用户是否是www
	//cmd := "ls -l / | grep -w data | awk '{print $3}'" //因为data,  -w无法精确匹配
	cmd := "ls  -l /data  |grep '^d' |grep -v , | grep '\\<app$' | awk '{print $3}'"
	user, err := appService.FindInfo(cmd)
	if err != nil {
		return err
	}
	utils.Logger().Info("/data/app目录所属用户是：%v", user)
	if user != "www" {
		errInfo := fmt.Sprintf("/data/app目录所属用户不是www用户，而是:%v用户", user)
		utils.Logger().Warn("/data/app目录所属用户不是www用户，而是:%v用户", user)
		return errors.New(errInfo)
	}
	cmd = "ls  -l /data  |grep '^d' |grep -v , | grep '\\<logs$' | awk '{print $3}'"
	user, err = appService.FindInfo(cmd)
	if err != nil {
		return err
	}
	utils.Logger().Info("/data/logs目录所属用户是：%v", user)
	if user != "www" {
		errInfo := fmt.Sprintf("/data/logs目录所属用户不是www用户，而是:%v用户", user)
		utils.Logger().Warn("/data/logs目录所属用户不是www用户，而是:%v用户", user)
		return errors.New(errInfo)
	}
	cmd = "ls  -l /data  |grep '^d' |grep -v , | grep '\\<backup$' | awk '{print $3}'"
	user, err = appService.FindInfo(cmd)
	if err != nil {
		return err
	}
	utils.Logger().Info("/data/backup目录所属用户是：%v", user)
	if user != "www" {
		errInfo := fmt.Sprintf("/data/backup目录所属用户不是www用户，而是:%v用户", user)
		utils.Logger().Warn("/data/backup目录所属用户不是www用户，而是:%v用户", user)
		return errors.New(errInfo)
	}
	return nil

}
func (appService *AppService) FindInfo(param string) (string, error) {
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
func (appService *AppService) HealthCheck(healthCheck string) (string, error) {

	return utils.ExecCommand("curl " + healthCheck)
}
