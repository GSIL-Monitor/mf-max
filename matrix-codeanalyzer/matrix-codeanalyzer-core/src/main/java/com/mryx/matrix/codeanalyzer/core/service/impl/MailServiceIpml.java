package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.google.common.collect.Lists;
import com.mryx.matrix.codeanalyzer.core.dao.P3cCodeScanJobDao;
import com.mryx.matrix.codeanalyzer.core.service.MailService;
import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.Email;
import com.mryx.matrix.codeanalyzer.dto.MailJobDto;
import com.mryx.matrix.codeanalyzer.dto.MailRecordDto;
import com.mryx.matrix.common.domain.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service("mailService")
public class MailServiceIpml implements MailService {
    @Resource
    private JavaMailSender javaMailSender;//执行者

    @Resource
    private TemplateEngine templateEngine;//thymeleaf模版引擎

    @Resource
    private P3cCodeScanJobDao p3cCodeScanJobDao;

    @Value("${spring.mail.username}")
    public String userName;//发送者

    @Override
    public ResultVo<String> send(Email mail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(userName);
        message.setTo(mail.getEmail());
        message.setSubject(mail.getSubject());
        message.setText(mail.getContent());
        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "邮件发送失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "邮件发送成功");
    }

    @Override
    public ResultVo<String> sendHtml(Email mail) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(userName);
            helper.setTo(mail.getEmail());
            helper.setSubject(mail.getSubject());
            helper.setText(
                    "<html><body><h1>你好！每日优鲜</h1></body></html>",
                    true);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "邮件发送失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "邮件发送成功");
    }

    @Override
    public ResultVo<String> sendThymeleaf(Email mail, CodeScanJob codeScanJob) {
        CodeScanJob codeScanJobResult = p3cCodeScanJobDao.getCodeScanJobByJobId(codeScanJob);
        if (codeScanJobResult == null) {
            log.error("CodeScanJob结果为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "CodeScanJob结果为空");
        }
        CodeScanJobRecord codeScanJobRecord = new CodeScanJobRecord();
        codeScanJobRecord.setJobId(codeScanJob.getId());
        List<CodeScanJobRecord> codeScanJobRecords = p3cCodeScanJobDao.getP3cDataWeek(codeScanJobRecord);
        if (codeScanJobRecords.isEmpty()) {
            log.error("CodeScanJobRecord结果为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "CodeScanJobRecord结果为空");
        }
        log.info("{}", codeScanJobResult);
        log.info("{}", codeScanJobRecords);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateTime = codeScanJobResult.getCodeScanTime();
            String scanTime = sdf.format(dateTime);
            MailJobDto job = new MailJobDto();
            BeanUtils.copyProperties(codeScanJobResult, job);
            job.setScanTime(scanTime);
            List<MailRecordDto> records = Lists.newArrayList();
            for (CodeScanJobRecord cr : codeScanJobRecords) {
                MailRecordDto dto = new MailRecordDto();
                BeanUtils.copyProperties(cr, dto);
                String time = sdf.format(cr.getCodeScanTime());
                dto.setScanTime(time);
                records.add(dto);
            }
            MimeMessage message = javaMailSender.createMimeMessage();
            Context context = new Context();
            context.setVariable("id", codeScanJobResult.getId());
            context.setVariable("job", job);
            context.setVariable("records", records);
            String emailContent = templateEngine.process("emailTemplate", context);
            //true表示需要创建一个multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(userName);
            helper.setTo(mail.getEmail());
            helper.setSubject(mail.getSubject());
            helper.setText(emailContent, true);
            javaMailSender.send(message);
            log.info("邮件发送成功");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "邮件发送成功");
        } catch (MessagingException e) {
            log.error("发送邮件时发生异常！{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "发送邮件时发生异常");
        }
    }
}
