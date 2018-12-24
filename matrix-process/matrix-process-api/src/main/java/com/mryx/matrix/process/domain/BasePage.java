package com.mryx.matrix.process.domain;

import java.io.Serializable;

/**
 * 分页
 *
 * @author juqing
 * @create 2018-09-03 15:51
 **/
public abstract class BasePage implements Serializable {
    private static final long serialVersionUID = 4271243427231421634L;
    public final static int DEFAULT_PAGE_NO = 1;
    public final static int DEFAULT_PAGE_SIZE = 20;

    private Integer pageNo = DEFAULT_PAGE_NO;

    private Integer pageSize = DEFAULT_PAGE_SIZE;

    private Integer offset;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffset() {
        return getStartOfPage();
    }

    public int getStartOfPage() {
        if (pageNo == 0) {
            // 页数是从第一页是从1开始计算的
            pageNo = DEFAULT_PAGE_NO;
        }
        return (pageNo - 1) * pageSize;
    }

    @Override
    public String toString() {
        return "BasePage{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                '}';
    }
}
