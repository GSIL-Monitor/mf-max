package com.mryx.matrix.publish.core.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.enums.ResultType;
import com.mryx.matrix.publish.dto.AppStartDTO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author supeng
 * @date 2018/11/19
 */
@Slf4j
@Component
public class StartAppProducer {

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.username}")
    private String userName;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.port}")
    private Integer port;

    private static final String EXCHANGE_NAME = "topic_matrix_publish_start_app";
//    private static final String EXCHANGE_NAME = "topic_matrix_publish_tmp";

    private static ConnectionFactory factory;

    private static Connection connection;

    public ResultVo send(AppStartDTO appStartDTO) {
        log.info("start send......appStartDTO = {}", appStartDTO);
        if (appStartDTO == null || appStartDTO.getIp() == null || "".equals(appStartDTO.getIp())) {
            log.warn("ip is null...");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg(ResultType.PARAMETER_ERROR.getCode(), ResultType.PARAMETER_ERROR.getMessage());
        }
        Channel channel = null;
        try {
            createConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
            ByteArrayOutputStream message = getMessage(appStartDTO);
            log.info("basicPublish EXCHANGE_NAME = {},routingKey= {},props = {},message = {}", EXCHANGE_NAME, appStartDTO.getIp(), null, appStartDTO);
            channel.basicPublish(EXCHANGE_NAME, appStartDTO.getIp(), null, message.toByteArray());
            //closeConnection();
        } catch (Exception e) {
            log.error("send error ", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg(ResultType.SYSTEM_EXCEPTION.getCode(), ResultType.SYSTEM_EXCEPTION.getMessage());
        } finally {
            try {
                closeChannel(channel);
            } catch (Exception e) {
                log.error("closeChannel error ", e);
            }
        }
        return ResultVo.Builder.SUCC();
    }

    /**
     * 关闭channel
     *
     * @param channel
     * @throws IOException
     * @throws TimeoutException
     */
    private void closeChannel(Channel channel) throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    /**
     * 关闭connection
     *
     * @param connection
     * @throws IOException
     */
    private void closeConnection(Connection connection) throws IOException {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
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
            //factory.setVirtualHost("/matrix-publish");
            factory.setConnectionTimeout(5000);
//            factory.setRequestedChannelMax(16);
        }
    }

    /**
     * 获取BasicProperties
     *
     * @return
     */
    private AMQP.BasicProperties getBasicProperties() {
        Map<String, Object> headers = new HashMap<>();
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
        builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
        builder.headers(headers);
        return builder.build();
    }

    /**
     * 获取 Message
     *
     * @param appStartDTO
     * @return
     */
    private ByteArrayOutputStream getMessage(AppStartDTO appStartDTO) {
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mapper.writeValue(out, appStartDTO);
        } catch (IOException e) {
            log.error("getMessage error ", e);
        }
        return out;
    }

    public static void main(String[] argv) throws Exception {
        StartAppProducer startAppProducer = new StartAppProducer();
        AppStartDTO appStartDTO = new AppStartDTO();
        appStartDTO.setIp("192.168.96.98");
        startAppProducer.send(appStartDTO);
    }
}
