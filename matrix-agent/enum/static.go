package enum

import (
	"matrix-agent/model"
	"fmt"
	"matrix-agent/utils"
	"os"
	"errors"
	"strings"
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
packageName:static-html-md5.zip
 */
func (static StaticType) PullPackageInfo(param *model.StartApp) error {
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
	packagePathTemp := fmt.Sprintf("/data/backup/%v/static-html/%v", param.BuildType, param.RecordId)
	if !utils.PathExists(packagePathTemp) {
		err = os.MkdirAll(packagePathTemp, os.ModePerm)
		if err != nil {
			utils.Logger().Error("os.MkdirAll path:%v faill:%v", packagePathTemp, err)
			return err
		}
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
	//解压static-html.zip
	staticName := strings.Replace(param.PackageName, "-md5", "", -1)
	staticFileName := fmt.Sprintf("%v/%v", packagePathTemp, staticName)
	if !utils.IsZip(staticFileName) {
		errInfo := fmt.Sprintf("fileName:%v is not zip file", staticFileName)
		utils.Logger().Warn("fileName:%v is not zip file", staticFileName)
		return errors.New(errInfo)
	}
	utils.Logger().Info("staticFileNameZip:%v start unzip.............", staticFileName)
	err = utils.Unzip(staticFileName, packagePathTemp)
	if err != nil {
		utils.Logger().Warn("解压%v文件fail:%v", staticFileName, err)
		return err
	}
	utils.Logger().Info("staticFileNameZip:%v unzip end...............", staticFileName)

	//赋予权限
	//cmd := "chown -R www:www /data/app/static-html/${build_path}"
	cmd = fmt.Sprintf("chown -R www:www %v", packagePathTemp)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		errInfo := fmt.Sprintf("cmd:%v utils.ExecCommand faill:%v", cmd, err)
		utils.Logger().Warn("cmd:%v utils.ExecCommand faill:%v", cmd, err)
		return errors.New(errInfo)
	}
	cmd = fmt.Sprintf("chmod -R +x %v", packagePathTemp)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		errInfo := fmt.Sprintf("cmd:%v utils.ExecCommand faill:%v", cmd, err)
		utils.Logger().Warn("cmd:%v utils.ExecCommand faill:%v", cmd, err)
		return errors.New(errInfo)
	}
	param.PackagePathTemp = packagePathTemp
	utils.Logger().Info("sleep 1s............")
	time.Sleep(1 * time.Second)
	//cp -r
	return static.MvFile(param)

}
func (static StaticType) MvFile(param *model.StartApp) error {
	cmd := fmt.Sprintf("cp -r %v/* %v", param.PackagePathTemp, param.PackagePath)
	outPut, err := utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("exec mvFile cmd fail:%v", err)
		return err
	}
	utils.Logger().Info("exec mvFile cmd res:%v", outPut)
	return nil

}
