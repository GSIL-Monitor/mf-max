package config

type beta struct {
	T
}

func (beta *beta) newConfig() T {
	//script初始化
	beta.T.Script.Start = "/data/scripts/matrix-scripts/agent/start.sh"
	beta.T.Script.Shutdown = "/data/scripts/matrix-scripts/agent/shutdown.sh"
	beta.T.Script.Restart = "/data/scripts/matrix-scripts/agent/restart.sh"
	beta.T.Script.Env = "/data/scripts/matrix-scripts/agent/env.sh"
	beta.T.Script.JarBeta = "/data/app/matrix-scripts/agent/start_jar.sh"
	beta.T.Script.JettyBeta = "/data/app/matrix-scripts/agent/start_jetty.sh"
	beta.T.Script.TomcatBeta = "/data/app/matrix-scripts/agent/start_tomcat.sh"
	beta.T.Script.GoBeta = "/data/app/matrix-scripts/agent/start_go.sh"
	beta.T.Script.PythonBeta = "/data/app/matrix-scripts/agent/start_python.sh"
	//beta.T.Script.StaticBeta = "/data/scripts/matrix-scripts/agent/start_static.sh"
	beta.T.Script.WmsBeta = "/data/app/ops-client-script/start_wms.sh"
	beta.T.Script.JarRelease = "/data/app/matrix-scripts/agent/start_jar.sh"
	beta.T.Script.JettyRelease = "/data/app/matrix-scripts/agent/start_jetty.sh"
	beta.T.Script.TomcatRelease = "/data/app/matrix-scripts/agent/start_tomcat.sh"
	beta.T.Script.GoRelease = "/data/app/matrix-scripts/agent/start_go.sh"
	beta.T.Script.PythonRelease = "/data/app/matrix-scripts/agent/start_python.sh"
	//beta.T.Script.StaticRelease = "/data/scripts/matrix-scripts/agent/start_static.sh"
	beta.T.Script.WmsRelease = "/data/app/ops-client-script/start_wms.sh"
	//url初始化
	beta.T.Url.ReportUrl = "http://127.0.0.1:18090/api/publish/agent/receive"
	//rabbitMq初始化
	beta.T.RabbitMq.Url = "amqp://admin:d2PD7f7N@10.2.3.36:5672"
	beta.T.RabbitMq.ConsumerExchanage = "topic_matrix_publish_start_app"
	beta.T.RabbitMq.ProducerExchange = "topic_matrix_publish_start_app_result"
	beta.T.RabbitMq.ProducerRoutingKey = "key_matrix_publish_start_app_result"
	beta.T.RabbitMq.ProducerReportExchange = "topic_matrix_publish_agent_report"
	beta.T.RabbitMq.ProducerReportRoutingKey = "key_matrix_publish_agent_report"
	//dubbo 摘挂流量
	beta.T.Traffic.PickTrafficUrl = "http://10.7.5.2:9876/assist/dubbo/provider/disable?urls="
	beta.T.Traffic.HangTrafficUrl = "http://10.7.5.2:9876/assist/dubbo/provider/enable?urls="
	beta.T.Traffic.SpringCloudDomain00 = "http://10.2.4.201:8010"
	beta.T.Traffic.SpringCloudDomain01 = "http://10.2.4.201:8010"
	beta.T.Traffic.SpringCloudDomain02 = "http://10.2.4.201:8010"
	return beta.T
}
