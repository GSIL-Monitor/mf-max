package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.Email;
import com.mryx.matrix.common.domain.ResultVo;

public interface MailService {

    /**
     *
     * @param mail
     * @return
     */
    public ResultVo<String> send(Email mail);

    /**
     *
     * @param mail
     * @return
     */
    public ResultVo<String> sendHtml(Email mail);

    /**
     *
     * @param mail
     * @return
     */
    public ResultVo<String> sendThymeleaf(Email mail, CodeScanJob codeScanJob);
}
