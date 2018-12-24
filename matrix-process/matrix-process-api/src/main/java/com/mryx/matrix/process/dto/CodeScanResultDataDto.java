package com.mryx.matrix.process.dto;

import com.mryx.matrix.process.domain.BasePage;
import lombok.Data;

import java.io.Serializable;

@Data
public class CodeScanResultDataDto{
    private Integer blocker;
    private Integer critical;
    private Integer major;
    private Integer minor;
    private Integer info;

    @Override
    public String toString() {
        return "CodeScanResultDataDto{" +
                ", blocker=" + blocker +
                ", critical=" + critical +
                ", major=" + major +
                ", minor=" + minor +
                ", info=" + info +
                '}';
    }
}
