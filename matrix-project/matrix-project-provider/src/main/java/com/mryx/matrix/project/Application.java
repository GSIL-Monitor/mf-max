package com.mryx.matrix.project;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ImportResource({"classpath:spring-dubbo-*.xml"})
@ComponentScan({"com.mryx.matrix.project","com.missfresh"})
@ServletComponentScan("com.mryx.matrix.project")
@MapperScan("com.mryx.matrix.project.dao")
public class Application {

    private static Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        LOG.info("matrix-project SpringBoot Start Success");
    }
}
