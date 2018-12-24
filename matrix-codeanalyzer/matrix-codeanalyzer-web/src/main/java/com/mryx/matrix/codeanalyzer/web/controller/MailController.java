package com.mryx.matrix.codeanalyzer.web.controller;

import com.mryx.matrix.codeanalyzer.core.service.MailService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.Email;
import com.mryx.matrix.common.domain.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/mail/")
public class MailController {

    @Resource
    MailService mailService;

    @RequestMapping(value = "/send", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> send() {
        ResultVo mail = mailService.send(new Email("lina02@missfresh.cn", "静态代码扫描结果详情", "hello missfresh"));
        if("0".equals(mail.getCode())){
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0","邮件发送成功");
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1","邮件发送失败");
    }

    @RequestMapping(value = "/sendHtml", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> sendHtml() {
        ResultVo mail = mailService.sendHtml(new Email("lina02@missfresh.cn", "静态代码扫描结果详情", "hello missfresh"));
        if("0".equals(mail.getCode())){
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0","邮件发送成功");
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1","邮件发送失败");
    }

    @RequestMapping(value = "/sendThymeleaf", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> sendThymeleaf(@RequestBody CodeScanJob codeScanJob) {
        if(codeScanJob==null){
            log.error("入参CodeScanJob为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2","入参CodeScanJob为空");
        }
        log.info("入参CodeScanJob为{}",codeScanJob);
        ResultVo mail = mailService.sendThymeleaf(new Email("lina02@missfresh.cn", "静态代码扫描结果详情", "hello missfresh"),codeScanJob);
        if("0".equals(mail.getCode())){
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0","邮件发送成功");
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1","邮件发送失败");
    }
}
