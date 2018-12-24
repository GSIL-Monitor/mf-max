package rabbitmq

import (
	"github.com/streadway/amqp"
	"matrix-agent/config"
	"matrix-agent/utils"
)

type RabbitMq struct {
	QueueName            string
	ExChangeConsumerName string
	ExChangeProducerName string

	RoutingConsumerKey string
	RoutingProducerKey string
	MsgChans           <-chan amqp.Delivery
	ChPro              *amqp.Channel
}

func (mq *RabbitMq) InitRabbitMqProducer() error {

	var err error
	if conn == nil {
		conn, err = amqp.Dial(config.RabbitMq.Url)
		if err != nil {
			utils.Logger().Error("amqp.Dial url:%v fail:%v", config.RabbitMq.Url, err)
			return err
		}
	}
	defer func() {
		if err != nil {
			conn.Close()
		}
	}()
	mq.ChPro, err = conn.Channel()
	if err != nil {
		utils.Logger().Error("amqp conn.Channel() fail:%v", err)
		return err
	}
	defer func() {
		if err != nil {
			mq.ChPro.Close()
		}
	}()
	err = mq.ChPro.ExchangeDeclare(
		mq.ExChangeProducerName,
		"topic",
		true,
		false,
		false,
		false,
		nil,
	)

	if err != nil {
		utils.Logger().Error("amqp exchangeDeclare:%v fail:%v", mq.ExChangeProducerName, err)
		return err
	}
	return nil
}
func (mq *RabbitMq) InitRabbitMqConsumer() error {
	var err error
	if conn == nil {
		conn, err = amqp.Dial(config.RabbitMq.Url)
		if err != nil {
			utils.Logger().Error("amqp.Dial url:%v fail:%v", config.RabbitMq.Url, err)
			return err
		}
	}
	defer func() {
		if err != nil {
			conn.Close()
		}
	}()
	chCon, err := conn.Channel()
	if err != nil {
		utils.Logger().Error("amqp conn.Channel() fail:%v", err)
		return err
	}
	defer func() {
		if err != nil {
			chCon.Close()
		}
	}()
	//交换器声明
	err = chCon.ExchangeDeclare(
		mq.ExChangeConsumerName, // name  交换器name
		"topic",                 // type
		true,                    // durable true:持续化队列
		false,                   // auto-deleted
		false,                   // internal
		false,                   // no-wait
		nil,                     // arguments
	)
	if err != nil {
		utils.Logger().Error("ch.ExchangeDeclare:%v fail:%v", mq.ExChangeConsumerName, err)
		return err
	}
	//队列声明
	args := make(amqp.Table)
	args["x-message-ttl"] = int32(60 * 1000) //只支持int32类型，不支持int类型（坑货）
	q, err := chCon.QueueDeclare(
		mq.QueueName, // name
		true,         // durable
		false,        // delete when unused
		false,        // exclusive
		false,        // no-wait
		args,
	)
	if err != nil {
		utils.Logger().Error("ch.QueueDeclare:%v fail:%v", mq.QueueName, err)
		return err
	}
	//队列绑定bindingkey
	err = chCon.QueueBind(
		q.Name,                  // queue name
		mq.RoutingConsumerKey,   // routing key  相当于bindkey
		mq.ExChangeConsumerName, // exchange
		false,
		nil)
	if err != nil {
		utils.Logger().Error("queue:%v bindkey:%v fail:%v", q.Name, localIp, err)
		return err
	}
	mq.MsgChans, err = chCon.Consume(
		q.Name, // queue
		"",     // consumer
		false,  // auto ack
		false,  // exclusive
		false,  // no local
		false,  // no wait
		nil,    // args
	)
	if err != nil {
		utils.Logger().Error("ch.Consume:%v fail:%v", q.Name, err)
		return err
	}
	return nil

}
