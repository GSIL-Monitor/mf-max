package schedule

import "github.com/robfig/cron"

//每个5分钟上报一次数据
const reportServerInfo = "0 0/5 * * * ?"

//const reportServerInfo = "0/20 * * * * ?" //假数据

func init() {
	setSchedule() //暂停
}

func setSchedule() {
	c := cron.New()
	c.AddFunc(reportServerInfo, report)
	c.Start()
}
