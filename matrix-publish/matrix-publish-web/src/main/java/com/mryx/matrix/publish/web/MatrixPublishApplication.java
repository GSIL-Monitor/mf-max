package com.mryx.matrix.publish.web;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ComponentScan("com.mryx.matrix.publish")
@MapperScan("com.mryx.matrix.publish.core.dao")
@ServletComponentScan("com.mryx.matrix.publish.web.filter")
//@ImportResource({"classpath:spring-dubbo-provider.xml"})
public class MatrixPublishApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixPublishApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MatrixPublishApplication.class, args);
        LOGGER.info("MatrixPublishApplication start success!");
    }
}