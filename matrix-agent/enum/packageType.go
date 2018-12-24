package enum

import (
	"matrix-agent/model"
)

//type PackageType
type PackageTypeHandle interface {
	PullPackageInfo(param *model.StartApp) (string, error)
	String() string
	FindPid(string) (string, error)
	Stop(param *model.StartApp) error
	MvFile(string) error
	Start(param *model.StartApp) error
	ReStart(param *model.StartApp) error
	Clear(param *model.StartApp) error
	RestartAgent() error
	HandleAction(param *model.StartApp) error
	FindInfo(param string) (string, error)
	Update(param *model.StartApp) error
	CheckEnvInfo() error
	PickTraffic() error                           //摘dubbo流量
	HangTraffic() error                           //挂dubbo流量
	PickCloudTraffic(param *model.StartApp) error                      //摘springCloud流量
	CheckTrafficIsSuccess(param *model.StartApp) error                 //检测流量是否挂上
	StartPickTraffic(param *model.StartApp) error //开始摘流量
	StartHangTraffic(param *model.StartApp) error //开始挂流量
	StartCmd(param *model.StartApp) error
}

type (
	JarType string
	GolangType string
	JettyType string
	TomcatType string
	PyType string
	StaticType string
	WmsType string
	WmsWwwRootType string
	WmsConfType string
	AgentType string
	DockerType string
	SpringGzType string
	JarMainType string
)

const (
	Jar            JarType        = "jar"
	Jetty          JettyType      = "jetty"
	Tomcat         TomcatType     = "tomcat"
	Golang         GolangType     = "go"
	Py             PyType         = "python"
	Static         StaticType     = "static"
	Wms            WmsType        = "wms"
	WmsWwwRoot     WmsWwwRootType = "wmswwwroot"
	Wmsconf        WmsConfType    = "wmsconf"
	Agent          AgentType      = "agent"
	Docker         DockerType     = "docker"
	SpringGz       SpringGzType   = "targz"
	JarMain        JarMainType    = "jar-main"
	startCmdHeader                = "#!/bin/bash\nsource /etc/profile\n"
)

func (jar JarType) String() string {
	return "jar"
}
func (jetty JettyType) String() string {
	return "jetty"
}
func (tomcat TomcatType) String() string {
	return "tomcat"
}
func (golang GolangType) String() string {
	return "go"
}
func (py PyType) String() string {
	return "python"
}
func (static StaticType) String() string {
	return "static"
}
func (wms WmsType) String() string {
	return "wms"
}
func (wmswwwroot WmsWwwRootType) String() string {
	return "wms-wwwroot"
}
func (wmsconf WmsConfType) String() string {
	return "wms-conf"
}
func (agent AgentType) String() string {
	return "agent"
}
func (docker DockerType) String() string {
	return "docker"
}
func (sg SpringGzType) String() string {
	return "spring_gz"
}
func (jarmain JarMainType) String() string {
	return "jar-main"
}
