package com.mryx.matrix.codeanalyzer.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class Email implements Serializable {
    private static final long serialVersionUID = 1324901L;

    private Integer id;
    //必填参数
    private String email;//接收方邮件
    private String subject;//主题
    private String content;//邮件内容
    private String path;
    //选填
    private String template;//模板
    private HashMap<String, String> kvMap;// 自定义参数

    public Email() {
    }

    public Email(String email, String subject, String content) {
        this.email = email;
        this.subject = subject;
        this.content = content;
    }
}
