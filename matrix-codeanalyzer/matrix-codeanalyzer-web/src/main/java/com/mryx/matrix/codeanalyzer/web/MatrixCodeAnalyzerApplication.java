package com.mryx.matrix.codeanalyzer.web;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.mryx.matrix.codeanalyzer")
@MapperScan("com.mryx.matrix.codeanalyzer.core.dao")
@ServletComponentScan("com.mryx.matrix.codeanalyzer.web.filter")
public class MatrixCodeAnalyzerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixCodeAnalyzerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MatrixCodeAnalyzerApplication.class, args);
        LOGGER.info("MatrixCodeAnalyzerApplication start success!");
    }
}
