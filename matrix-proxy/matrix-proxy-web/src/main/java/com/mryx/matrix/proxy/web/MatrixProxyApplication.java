package com.mryx.matrix.proxy.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ImportResource({"classpath:spring-dubbo-consumer.xml","classpath:spring-dubbo-provider.xml"})
@ComponentScan("com.mryx.matrix.proxy")
public class MatrixProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(MatrixProxyApplication.class, args);
	}
}
