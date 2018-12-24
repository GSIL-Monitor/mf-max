package schedule

import (
	"matrix-agent/service"
	"matrix-agent/utils"
	"matrix-agent/rabbitmq"
	"matrix-agent/config"
	"github.com/streadway/amqp"
	"matrix-agent/model"
)

var mq *rabbitmq.RabbitMq

func init() {
	newMqProducerReport()
}
func newMqProducerReport() *rabbitmq.RabbitMq {
	mq = &rabbitmq.RabbitMq{ExChangeProducerName: config.RabbitMq.ProducerReportExchange, RoutingProducerKey: config.RabbitMq.ProducerReportRoutingKey}
	err := mq.InitRabbitMqProducer()
	utils.Logger().Info("initRabbitMqReportProducer starting...........")
	err = mq.InitRabbitMqProducer()
	if err != nil {
		utils.Logger().Warn("initRabbitMqReportProducer fail:%v", err)
		return nil
	}
	utils.Logger().Info("initRabbitMqReportProducer start success........")
	return mq
}

func report() {
	utils.Logger().Info("report server info")
	info := service.NewEnvService().Env()
	err := mq.SendRabitMqMsgData(info)
	if err != nil {
		utils.Logger().Info("report resp fail:%v", err)
		if err == amqp.ErrClosed || err == model.ChannelNilErr {
			utils.Logger().Warn("mq连接断了.....将马上重新连接.......")
			newMqProducerReport()
		}

	}
}
