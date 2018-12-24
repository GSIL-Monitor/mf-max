package com.mryx.matrix.publish.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.publish.core.service.AppStartResultService;
import com.mryx.matrix.publish.domain.AppServer;
import com.mryx.matrix.publish.domain.AppStartResult;
import com.mryx.matrix.publish.dto.AgentDTO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * rabbltmq consumer 消费应用启动结果消息
 *
 * @author supeng
 * @date 2018/11/19
 */
@Slf4j
@Component
public class StartAppResultConsumer {

    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Value("${agentIpUpdateStatus}")
    private String agentIpUpdateStatus;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.username}")
    private String userName;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.port}")
    private Integer port;

    @Autowired
    private AppStartResultService appStartResultService;

    private static final String EXCHANGE_NAME = "topic_matrix_publish_start_app_result";

    private static final String ROUTING_KEY = "key_matrix_publish_start_app_result";

    private static final String QUEUE_NAME = "queue_matrix_publish_start_app_result";
//    private static final String EXCHANGE_NAME = "topic_matrix_publish_tmp_result";
//
//    private static final String ROUTING_KEY = "key_matrix_publish_tmp_result";
//
//    private static final String QUEUE_NAME = "queue_matrix_publish_tmp_result";

    private static final String REPORT_EXCHANGE_NAME = "topic_matrix_publish_agent_report";

    private static final String REPORT_ROUTING_KEY = "key_matrix_publish_agent_report";

    private static final String REPORT_QUEUE_NAME = "queue_matrix_publish_agent_report";

    private static ConnectionFactory factory;

    private static Connection connection;

    @PostConstruct
    public void start() throws Exception {
        log.info("startConsumer...");
        createConnection();
        log.info("connection.getChannelMax() = {},factory.getConnectionTimeout() = {}", connection.getChannelMax(), factory.getConnectionTimeout());

        startConsumer();
        reportConsumer();
    }

    public void startConsumer() throws Exception {
        log.info("startConsumer...");
        createConnection();
        log.info("connection.getChannelMax() = {},factory.getConnectionTimeout() = {}", connection.getChannelMax(), factory.getConnectionTimeout());
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 60000);
        channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                log.info("message = {}", message);
                try {
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    log.info("jsonObject = {}", jsonObject);
                    if (jsonObject != null && jsonObject.getString("recordId") != null && !"".equals(jsonObject.getString("recordId"))
                            && jsonObject.getString("ip") != null && !"".equals(jsonObject.getString("ip"))
                            && jsonObject.getString("code") != null && !"".equals(jsonObject.getString("code"))
                            && jsonObject.getString("buildType") != null && !"".equals(jsonObject.getString("buildType"))) {
                        try {
                            AppStartResult appStartResult = new AppStartResult();
                            appStartResult.setBuildType(jsonObject.getString("buildType"));
                            appStartResult.setResultCode(jsonObject.getString("code"));
                            appStartResult.setMessage(jsonObject.getString("message"));
                            appStartResult.setRecordId(jsonObject.getString("recordId"));
                            appStartResult.setIp(jsonObject.getString("ip"));
                            Integer flag = appStartResultService.insert(appStartResult);
                            log.info("result insert flag = {}", flag);
                        } catch (Exception e) {
                            log.error("insert error ", e);
                        }
                    }
                } catch (Exception e) {
                    log.error("consumer error ", e);
                }
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

    public void reportConsumer() throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(REPORT_EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
        Map<String, Object> arguments = new HashMap<>();
        channel.queueDeclare(REPORT_QUEUE_NAME, true, false, false, arguments);
        channel.queueBind(REPORT_QUEUE_NAME, REPORT_EXCHANGE_NAME, REPORT_ROUTING_KEY);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                try {
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    if (jsonObject != null) {
                        try {
                            AgentDTO agentDTO = JSONObject.parseObject(message, AgentDTO.class);
                            agentDTO.setAgentUpdateTime(new Date());

                            String ip = agentDTO.getIp();
                            redisTemplate.opsForValue().set("matrix:report:" + ip, JSON.toJSONString(agentDTO));

                            AppServer server = new AppServer();
                            server.setHostIp(ip);
                            server.setMsReport(System.currentTimeMillis());
                            HTTP_POOL_CLIENT.postJson(agentIpUpdateStatus, JSON.toJSONString(server));
                        } catch (Exception e) {
                            log.error("insert error ", e);
                        }
                    }
                } catch (Exception e) {
                    log.error("consumer error ", e);
                }
            }
        };
        channel.basicConsume(REPORT_QUEUE_NAME, true, consumer);
    }

    /**
     * 创建连接
     *
     * @throws IOException
     * @throws TimeoutException
     */
    private void createConnection() throws IOException, TimeoutException {
        createConnectionFactory();
        if (connection == null) {
            log.info("create new connection...");
            connection = factory.newConnection();
        }
    }

    /**
     * 创建连接工厂
     */
    private void createConnectionFactory() {
        if (factory == null) {
            log.info("create new ConnectionFactory...");
            factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(userName);
            factory.setPassword(password);
            factory.setPort(port);
            factory.setConnectionTimeout(5000);
            factory.setRequestedChannelMax(32);
        }
    }
}
