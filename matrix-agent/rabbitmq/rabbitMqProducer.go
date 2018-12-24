package rabbitmq

import (
	"matrix-agent/utils"
	"github.com/streadway/amqp"
	"matrix-agent/model"
)

var (
//chPro *amqp.Channel
)

type response struct {
	Code      string `json:"code"`
	Message   string `json:"message"`
	Ip        string `json:"ip"`
	RecordId  string `json:"recordId"`
	BuildType string `json:"buildType"`
}

func (mq *RabbitMq) SendRabitMqMsgOk(recordId, buildType string) error {
	body := response{
		Code:      "0",
		Message:   "success",
		Ip:        localIp,
		RecordId:  recordId,
		BuildType: buildType,
	}
	sendBytes, err := utils.Jsoniteror.Marshal(body)
	if err != nil {
		return err
	}
	utils.Logger().Info("ip:%v往exchange:%v routingKey:%v发送数据:%+v", localIp, mq.ExChangeProducerName,
		mq.RoutingProducerKey, body)
	err = mq.ChPro.Publish(
		mq.ExChangeProducerName, // exchange
		mq.RoutingProducerKey,   // routing key
		false,
		false,
		amqp.Publishing{
			DeliveryMode: amqp.Persistent, //队列持久模式，必须tian加
			ContentType:  "application/json",
			Body:         sendBytes,
		})
	return err
}
func (mq *RabbitMq) SendRabitMqMsgError(msg string, recordId, buildType string) error {

	body := response{
		Code:      "1",
		Message:   msg,
		Ip:        localIp,
		RecordId:  recordId,
		BuildType: buildType,
	}
	sendBytes, err := utils.Jsoniteror.Marshal(body)
	if err != nil {
		return err
	}
	utils.Logger().Info("ip:%v往exchange:%v routingKey:%v发送数据:%+v", localIp, mq.ExChangeProducerName,
		mq.RoutingProducerKey, body)
	err = mq.ChPro.Publish(
		mq.ExChangeProducerName, // exchange
		mq.RoutingProducerKey,   // routing key
		false,
		false,
		amqp.Publishing{
			DeliveryMode: amqp.Persistent, //队列持久模式，必须tian加
			ContentType:  "application/json",
			Body:         sendBytes,
		})
	return err
}
func (mq *RabbitMq) SendRabitMqMsgData(data interface{}) error {
	sendBytes, err := utils.Jsoniteror.Marshal(data)
	if err != nil {
		return err
	}
	if mq.ChPro == nil {
		utils.Logger().Warn("mq.ChPro is nil")
		return model.ChannelNilErr
	}
	utils.Logger().Info("ip:%v往exchange:%v routingKey:%v发送数据:%+v", localIp, mq.ExChangeProducerName,
		mq.RoutingProducerKey, data)
	err = mq.ChPro.Publish(
		mq.ExChangeProducerName, // exchange
		mq.RoutingProducerKey,   // routing key
		false,
		false,
		amqp.Publishing{
			DeliveryMode: amqp.Persistent, //队列持久模式，必须tian加
			ContentType:  "application/json",
			Body:         sendBytes,
		})
	return err
}
