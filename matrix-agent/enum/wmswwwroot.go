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

func (wmswwwroot WmsWwwRootType) PullPackageInfo(param *model.StartApp) error {
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
	packagePathTemp := fmt.Sprintf("/data/backup/%v/wmswwwroot/%v", param.BuildType, param.RecordId)
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
	utils.Logger().Info("jsNameZip:%v unzip end...............", jsFileName)
	//cmd = "rsync -avz --delete --exclude-from  "/data/app/exclude.txt" /data/app/test1/  /data/app/test/"
	if param.PackagePath != "" {
		if string(param.PackagePath[len(param.PackagePath)-1]) != "/" {
			param.PackagePath = fmt.Sprintf("%v/", param.PackagePath)
		}

	}
	cmd = fmt.Sprintf("nohup rsync -avz --delete --exclude-from %v/exclude %v/ %v &", packagePathTemp, packagePathTemp, param.PackagePath)
	_, err = utils.ExecCommand(cmd)
	if err != nil {
		utils.Logger().Warn("js wmswwwroot:%v rsync fail:%v", param.PackageName, err)
		return err
	}
	utils.Logger().Info("start sleep 10s........")
	time.Sleep(10 * time.Second)
	return nil
}
