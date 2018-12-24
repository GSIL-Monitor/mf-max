package config

import (
	"matrix-agent/utils"
	"os"
	"strings"
)

// 对外提供的变量
var Mode string
var Script script
var Url url
var RabbitMq rabbitMq
var Traffic traffic

type T struct {
	Script   script
	Url      url
	RabbitMq rabbitMq
	Traffic  traffic
}
type configInterface interface {
	newConfig() script
}
type script struct {
	Start         string
	Shutdown      string
	Restart       string
	Env           string
	JarBeta       string
	JettyBeta     string
	TomcatBeta    string
	GoBeta        string
	PythonBeta    string
	StaticBeta    string
	WmsBeta       string
	JarRelease    string
	JettyRelease  string
	TomcatRelease string
	GoRelease     string
	PythonRelease string
	StaticRelease string
	WmsRelease    string
}

type url struct {
	ReportUrl string
}
type rabbitMq struct {
	Url                      string
	ConsumerExchanage        string
	ProducerExchange         string
	ProducerRoutingKey       string
	ProducerReportExchange   string
	ProducerReportRoutingKey string
}
type traffic struct {
	PickTrafficUrl      string //针对dubbo
	HangTrafficUrl      string //针对dubbo
	SpringCloudDomain00 string //针对springcloud
	SpringCloudDomain01 string //针对springcloud
	SpringCloudDomain02 string //针对springcloud
}
type config struct {
	Dev     dev
	Test    beta
	Beta    beta
	Release release
	Prod    release
}

//初始化config
func init() {
	Mode = "dev"
	cfg := "./config.yaml"
	var args = os.Args
	//-v beta -cfg ./config.yaml
	var allVersion = [5]string{"dev", "beta", "release", "test", "prod"}
	for i := len(args) - 1; i > 0; i-- {
		//-c配置文件路径参数
		//if strings.ToLower(args[i]) == "-c" {
		//	if i+1 < len(args) && strings.Index(args[i+1], "-") < 0 {
		//		cfg = args[i+1]
		//	}
		//}
		//处理-v发版参数
		if strings.ToLower(args[i]) == "-v" {
			if i+1 < len(args) && strings.Index(args[i+1], "-") < 0 {
				for _, v := range allVersion {
					if v == args[i+1] {
						Mode = args[i+1]
						break
					}
				}
			}
		}
	}
	utils.Logger().Info("config args=%v mode=%v path=%v", args, Mode, cfg)
	var config config
	newConfig := config.getNewConfig(Mode)
	Script = newConfig.Script
	Url = newConfig.Url
	RabbitMq = newConfig.RabbitMq
	Traffic = newConfig.Traffic

}

func (config *config) getNewConfig(mode string) T {
	switch mode {
	case "dev":
		return config.Dev.newConfig()
	case "beta":
		return config.Beta.newConfig()
	case "test":
		return config.Beta.newConfig()
	case "release":
		return config.Release.newConfig()
	case "prod":
		return config.Release.newConfig()
	default:
		return T{}

	}
}
