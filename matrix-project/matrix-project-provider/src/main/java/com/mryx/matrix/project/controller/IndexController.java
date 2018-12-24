package com.mryx.matrix.project.controller;

import com.mryx.common.rmq.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IndexController
 *
 * @author wangwenbo
 * @create 2018-05-31 22:17
 **/
@RestController
@RequestMapping("/api/project")
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired(required = false)
    RocketMQTemplate rocketMQTemplate;

    @RequestMapping("/healthcheck")
    public String healthcheck() {
        logger.info("healthcheck");
        return "success";
    }

//    @ResponseBody
//    @RequestMapping("/testmq")
//    public String testmq() {
//        logger.info("testmq");
//
//        String topic = "test1-192-168-97-118";
//        Object messageBody = "messageBody";
//        GrampusMessage message = new GrampusMessage(topic, messageBody);
////        message.setDefaultTopicQueueNums(4);
//        SendResult result1 = rocketMQTemplate.syncSend(message);
//        logger.info("result1={}", result1);
//        return "success";
//    }
}
