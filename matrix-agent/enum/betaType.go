package enum

import (
	"matrix-agent/model"
	"strings"
	"matrix-agent/utils"
	"matrix-agent/config"
	"fmt"
	"errors"
)

const (
	BetaDefaultJvmOps    = "-Xmx2g -Xms2g -Xmn1g -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=256M"
	ReleaseDefaultJvmOps = "-Xmx6g -Xms6g -Xmn3g -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=256M"
)

func (beta BeatType) HandleBetaType(param *model.StartApp) error {
	switch param.PackageType {
	case Jar.String():
		//处理stop时 startOps为空导致stop失败的问题
		if param.StartOps == "" {
			param.StartOps = BetaDefaultJvmOps
		} else {
			param.StartOps = strings.Replace(param.StartOps, "@", " ", -1)
		}
		return Jar.HandleAction(param)
	case Jetty.String():
		if param.StartOps == "" {
			param.StartOps = BetaDefaultJvmOps
		}
		//_, err := utils.ExecCommand(config.Script.JettyBeta + " " + param.PackageName + " " + param.Action + " '" + param.StartOps + "' '" + param.ContainerPath + "' '" + param.PackagePath + "'")
		return Jetty.HandleAction(param)
	case Tomcat.String():
		if param.StartOps == "" {
			param.StartOps = BetaDefaultJvmOps
		}
		//_, err := utils.ExecCommand(config.Script.TomcatBeta + " " + param.PackageName + " " + param.Action + " '" + param.StartOps + "' '" + param.ContainerPath + "' '" + param.PackagePath + "'")
		return Tomcat.HandleAction(param)
	case Golang.String():
		appName, err := utils.GetAppNameFromZipFile(param.PackageName)
		if err != nil {
			utils.Logger().Warn("packageName:%v GetAppNameFromZipFile Fail:%v", param.PackageName, err)
			return err
		}
		param.PackageName = appName
		return Golang.HandleAction(param)
	case Py.String():
		_, err := utils.ExecCommand(config.Script.PythonBeta + " " + param.PackageName + " " + param.PackagePath + " " + param.Action)
		return err

	case Wms.String():
		_, err := utils.ExecCommand(config.Script.WmsBeta + " " + param.PackageName + " " + param.BuildType)
		return err
	case Wmsconf.String():
		return Wmsconf.HandleAction(param)

	case Agent.String():
		return Agent.HandleAction(param)
	case Docker.String():
		return Docker.HandleAction(param)
	case SpringGz.String():
		return SpringGz.HandleAction(param)
	case JarMain.String():
		//处理stop时 startOps为空导致stop失败的问题
		if param.StartOps == "" {
			param.StartOps = BetaDefaultJvmOps
		} else {
			param.StartOps = strings.Replace(param.StartOps, "@", " ", -1)
		}
		return JarMain.HandleAction(param)

	default:
		errInfo := fmt.Sprintf("startApp 参数错误 packageType =%v", param.PackageType)
		utils.Logger().Warn("startApp 参数错误 packageType =%v", param.PackageType)
		return errors.New(errInfo)
	}
}
