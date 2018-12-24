package rabbitmq

import (
	"matrix-agent/utils"
	"github.com/streadway/amqp"
	"matrix-agent/config"
	"time"
)

var (
	conn    *amqp.Connection
	localIp string
)

func NewRabbitMq() *RabbitMq {
	localIp = utils.GetLocalIp()
	//localIp = "192.168.101.25" //jia
	mq := &RabbitMq{ExChangeConsumerName: config.RabbitMq.ConsumerExchanage, RoutingConsumerKey: localIp, QueueName: localIp}
	utils.Logger().Info("initRabbitMqConsumer starting...........")
	err := mq.InitRabbitMqConsumer()
	if err != nil {
		utils.Logger().Warn("initRabbitMqConsumer fail:%v", err)
		return nil
	}
	utils.Logger().Info("initRabbitMqConsumer start success........")
	mq.ExChangeProducerName = config.RabbitMq.ProducerExchange
	mq.RoutingProducerKey = config.RabbitMq.ProducerRoutingKey
	utils.Logger().Info("initRabbitMProducer starting...........")
	err = mq.InitRabbitMqProducer()
	if err != nil {
		utils.Logger().Warn("initRabbitMqProducer fail:%v", err)
		return nil
	}
	utils.Logger().Info("initRabbitMProducer start success........")
	return mq
}
func (mq *RabbitMq) HandlerRabbitMqMsg() {
	if mq == nil {
		utils.Logger().Warn("initRabbitMqConsumer fail!")
		go mq.TimerConnectMq()
		return
	}
	for {
		select {
		case msg := <-mq.MsgChans:
			utils.Logger().Info("received queue:%v msgId:%v message:%v", mq.QueueName, msg.MessageId, string(msg.Body))
			if string(msg.Body) == "" {
				go mq.TimerConnectMq()
				return

			}
			go mq.StartApp(msg.Body)
			msg.Ack(false) //回应mq，已处理完毕，可将其消息删除
		}
	}
}
func (mq *RabbitMq) TimerConnectMq() {
	utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，10s之后将重启rabbitMq服务............")
	conn = nil
	timer := time.NewTimer(10 * time.Second)
	stopTimer := make(chan bool)
	for i := 0; i < 9; i++ {
		select {
		case <-timer.C:
			mq = reConnectMq()
			if mq != nil {
				go mq.HandlerRabbitMqMsg()
				stopTimer <- true
			} else {
				switch i {
				case 0:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，20s之后将重启rabbitMq服务............")
					timer.Reset(20 * time.Second)
				case 1:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，30s之后将重启rabbitMq服务............")
					timer.Reset(30 * time.Second)
				case 2:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，1分钟之后将重启rabbitMq服务............")
					timer.Reset(1 * time.Minute)
				case 3:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，2分钟之后将重启rabbitMq服务............")
					timer.Reset(2 * time.Minute)
				case 4:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，3分钟之后将重启rabbitMq服务............")
					timer.Reset(3 * time.Minute)
				case 5:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，30分钟之后将重启rabbitMq服务............")
					timer.Reset(30 * time.Minute)
				case 6:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，1小时之后将重启rabbitMq服务............")
					timer.Reset(60 * time.Minute)
				case 7:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，5小时之后将重启rabbitMq服务............")
					timer.Reset(60 * 5 * time.Minute)
				case 8:
					utils.Logger().Error("rabbitMq服务已停止，希望尽快恢复，10小时之后将重启rabbitMq服务............")
					timer.Reset(60 * 10 * time.Minute)
				}

			}
		case <-stopTimer:
			utils.Logger().Info("rabbitMq restart success...............")
			break

		}
	}
}
func reConnectMq() *RabbitMq {
	return NewRabbitMq()
}
