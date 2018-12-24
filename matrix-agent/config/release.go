package config

type release struct {
	T
}

func (release *release) newConfig() T {
	//script初始化
	release.T.Script.Start = "/data/scripts/matrix-scripts/agent/start.sh"
	release.T.Script.Shutdown = "/data/scripts/matrix-scripts/agent/shutdown.sh"
	release.T.Script.Restart = "/data/scripts/matrix-scripts/agent/restart.sh"
	release.T.Script.Env = "/data/scripts/matrix-scripts/agent/env.sh"
	release.T.Script.JarBeta = "/data/app/matrix-scripts/agent/start_jar.sh"
	release.T.Script.JettyBeta = "/data/app/matrix-scripts/agent/start_jetty.sh"
	release.T.Script.TomcatBeta = "/data/app/matrix-scripts/agent/start_tomcat.sh"
	release.T.Script.GoBeta = "/data/app/matrix-scripts/agent/start_go.sh"
	release.T.Script.PythonBeta = "/data/app/matrix-scripts/agent/start_python.sh"
	//release.T.Script.StaticBeta = "/data/scripts/matrix-scripts/agent/beta/start_static.sh"
	release.T.Script.WmsBeta = "/data/app/ops-client-script/start_wms.sh"
	release.T.Script.JarRelease = "/data/app/matrix-scripts/agent/start_jar.sh"
	release.T.Script.JettyRelease = "/data/app/matrix-scripts/agent/start_jetty.sh"
	release.T.Script.TomcatRelease = "/data/app/matrix-scripts/agent/start_tomcat.sh"
	release.T.Script.GoRelease = "/data/app/matrix-scripts/agent/start_go.sh"
	release.T.Script.PythonRelease = "/data/app/matrix-scripts/agent/start_python.sh"
	//release.T.Script.StaticRelease = "/data/app/matrix-scripts/agent/start_static.sh"
	release.T.Script.WmsRelease = "/data/app/ops-client-script/start_wms.sh"
	//url初始化
	release.T.Url.ReportUrl = "http://127.0.0.1:18090/api/publish/agent/receive"
	//rabbitMq初始化
	release.T.RabbitMq.Url = "amqp://admin:tAPnDE2f3N4o@10.3.39.39:30000"
	release.T.RabbitMq.ConsumerExchanage = "topic_matrix_publish_start_app"
	release.T.RabbitMq.ProducerExchange = "topic_matrix_publish_start_app_result"
	release.T.RabbitMq.ProducerRoutingKey = "key_matrix_publish_start_app_result"
	release.T.RabbitMq.ProducerReportExchange = "topic_matrix_publish_agent_report"
	release.T.RabbitMq.ProducerReportRoutingKey = "key_matrix_publish_agent_report"
	//dubbo 摘挂流量
	release.T.Traffic.PickTrafficUrl = "http://10.10.100.68:9876/assist/dubbo/provider/disable?urls="
	release.T.Traffic.HangTrafficUrl = "http://10.10.100.68:9876/assist/dubbo/provider/enable?urls="
	release.T.Traffic.SpringCloudDomain00 = "http://idc01-tms-eureka-00.dns.missfresh.cn:8080/eureka"
	release.T.Traffic.SpringCloudDomain01 = "http://idc01-tms-eureka-01.dns.missfresh.cn:8080/eureka"
	release.T.Traffic.SpringCloudDomain02 = "http://idc01-tms-eureka-02.dns.missfresh.cn:8080/eureka"
	return release.T

}

/*
 ,http://idc01-tms-eureka-02.dns.missfresh.cn:8080/eureka/
func (release *release) newConfig() T {
	//script初始化
	release.T.Script.Start = "/data/scripts/matrix-scripts/agent/start.sh"
	release.T.Script.Shutdown = "/data/scripts/matrix-scripts/agent/shutdown.sh"
	release.T.Script.Restart = "/data/scripts/matrix-scripts/agent/restart.sh"
	release.T.Script.Env = "/data/scripts/matrix-scripts/agent/env.sh"
	release.T.Script.JarBeta = "/data/app/ops-client-script/ops_boot.sh"
	release.T.Script.JettyBeta = "/data/app/ops-client-script/ops_boot.sh"
	release.T.Script.TomcatBeta = "/data/scripts/matrix-scripts/agent/beta/start_tomcat.sh"
	release.T.Script.GoBeta = "/data/app/ops-client-script/temp_start_ess_go.sh"
	release.T.Script.PythonBeta = "/data/scripts/matrix-scripts/agent/beta/start_python.sh"
	release.T.Script.StaticBeta = "/data/scripts/matrix-scripts/agent/beta/start_static.sh"
	release.T.Script.WmsBeta = "/data/app/ops-client-script/start_wms.sh"
	release.T.Script.JarRelease = "/data/app/ops-client-script/ops_boot.sh"
	release.T.Script.JettyRelease = "/data/scripts/matrix-scripts/agent/release/start_jetty.sh"
	release.T.Script.TomcatRelease = "/data/scripts/matrix-scripts/agent/release/start_tomcat.sh"
	release.T.Script.GoRelease = "/data/app/ops-client-script/temp_start_ess_go.sh"
	release.T.Script.PythonRelease = "/data/scripts/matrix-scripts/agent/release/start_python.sh"
	release.T.Script.StaticRelease = "/data/scripts/matrix-scripts/agent/release/start_static.sh"
	release.T.Script.WmsRelease = "/data/app/ops-client-script/start_wms.sh"
	//url初始化
	release.T.Url.ReportUrl = "http://127.0.0.1:18090/api/publish/agent/receive"
	return release.T

}
 */
