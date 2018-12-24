package config

type dev struct {
	T
}

func (dev *dev) newConfig() T {
	//script初始化
	dev.T.Script.Start = "/data/scripts/matrix-scripts/agent/start.sh"
	dev.T.Script.Shutdown = "/data/scripts/matrix-scripts/agent/shutdown.sh"

	dev.T.Script.Restart = "/data/scripts/matrix-scripts/agent/restart.sh"
	dev.T.Script.Env = "/data/scripts/matrix-scripts/agent/env.sh"
	dev.T.Script.JarBeta = "/data/app/matrix-scripts/agent/start_jar.sh"
	dev.T.Script.JettyBeta = "/data/app/matrix-scripts/agent/start_jetty.sh"
	dev.T.Script.TomcatBeta = "/data/app/matrix-scripts/agent/start_tomcat.sh"
	dev.T.Script.GoBeta = "/data/app/matrix-scripts/agent/start_go.sh"
	dev.T.Script.PythonBeta = "/data/app/matrix-scripts/agent/start_python.sh"
	//beta.T.Script.StaticBeta = "/data/scripts/matrix-scripts/agent/start_static.sh"
	dev.T.Script.WmsBeta = "/data/app/ops-client-script/start_wms.sh"
	dev.T.Script.JarRelease = "/data/app/matrix-scripts/agent/start_jar.sh"
	dev.T.Script.JettyRelease = "/data/app/matrix-scripts/agent/start_jetty.sh"
	dev.T.Script.TomcatRelease = "/data/app/matrix-scripts/agent/start_tomcat.sh"
	dev.T.Script.GoRelease = "/data/app/matrix-scripts/agent/start_go.sh"
	dev.T.Script.PythonRelease = "/data/app/matrix-scripts/agent/start_python.sh"
	//beta.T.Script.StaticRelease = "/data/scripts/matrix-scripts/agent/start_static.sh"
	dev.T.Script.WmsRelease = "/data/app/ops-client-script/start_wms.sh"
	//url初始化
	dev.T.Url.ReportUrl = "http://127.0.0.1:18090/api/publish/agent/receive"
	//rabbitMq初始化
	dev.T.RabbitMq.Url = "amqp://guest:guest@localhost:5672"
	dev.T.RabbitMq.ConsumerExchanage = "topic_matrix_publish_start_app"
	dev.T.RabbitMq.ProducerExchange = "topic_matrix_publish_start_app_result"
	dev.T.RabbitMq.ProducerRoutingKey = "key_matrix_publish_start_app_result"
	dev.T.RabbitMq.ProducerReportExchange = "topic_matrix_publish_agent_report"
	dev.T.RabbitMq.ProducerReportRoutingKey = "key_matrix_publish_agent_report"
	//dubbo 摘挂流量
	dev.T.Traffic.PickTrafficUrl = "http://10.7.5.2:9876/assist/dubbo/provider/disable?urls="
	dev.T.Traffic.HangTrafficUrl = "http://10.7.5.2:9876/assist/dubbo/provider/enable?urls="
	dev.T.Traffic.SpringCloudDomain00 = "http://10.2.4.201:8010"
	dev.T.Traffic.SpringCloudDomain01 = "http://10.2.4.201:8010"
	dev.T.Traffic.SpringCloudDomain02 = "http://10.2.4.201:8010"
	return dev.T
}

/*
func (dev *dev) newConfig() T {
	//script初始化
	dev.T.Script.Start = "/Users/Heyu/data/start.sh"
	dev.T.Script.Shutdown = "/Users/Heyu/data/shutdown.sh"
	dev.T.Script.Restart = "/Users/Heyu/data/restart.sh"
	dev.T.Script.Env = "/Users/Heyu/data/env.sh"
	dev.T.Script.JarBeta = "/Users/Heyu/data/start_jar.sh"
	dev.T.Script.TomcatBeta = "/Users/Heyu/data/start_tomcat.sh"
	dev.T.Script.GoBeta = "/Users/Heyu/data/start_go.sh"
	dev.T.Script.PythonBeta = "/Users/Heyu/data/start_python.sh"
	dev.T.Script.StaticBeta = "/Users/Heyu/data/start_static.sh"
	dev.T.Script.WmsBeta = "/data/app/ops-client-script/start_wms.sh"
	dev.T.Script.JarRelease = "/Users/Heyu/data/start_jar.sh"
	dev.T.Script.JettyRelease = "/Users/Heyu/data/start_jetty.sh"
	dev.T.Script.TomcatRelease = "/Users/Heyu/data/start_tomcat.sh"
	dev.T.Script.GoRelease = "/Users/Heyu/data/start_go.sh"
	dev.T.Script.PythonRelease = "/Users/Heyu/data/start_python.sh"
	dev.T.Script.StaticRelease = "/Users/Heyu/data/start_static.sh"
	dev.T.Script.WmsRelease = "/data/app/ops-client-script/start_wms.sh"
	//url初始化
	dev.T.Url.ReportUrl = "http://127.0.0.1:18090/api/publish/agent/receive"
	return dev.T
}
*/
