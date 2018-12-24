package com.mryx.matrix.process.web.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分页工具类
 *
 * @author jq
 * @Date 2018-09-04
 */
public class Pagination<T> implements Serializable {

    private static final long serialVersionUID = 9114641116237712552L;

    /**
     * 页码(第几页)
     */
    private Integer pageNo = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 20;

    /**
     * 开始查询序号
     */
    private Integer startNo = 0;

    /**
     * 总记录数
     */
    private Integer totalSize = 0;

    /**
     * 总页数
     */
    private Integer totalPage = 0;

    /**
     * 查询条件
     */
    private Map<String, Object> query;

    /**
     * 分页数据
     */
    private List<T> dataList;

    public Pagination() {
    }

    /**
     * @param pageNo   页码
     * @param pageSize 每页记录数
     */
    public Pagination(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

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

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * 通过总记录数计算总页数
     *
     * @param totalSize 总记录数
     */
    public void setTotalPageForTotalSize(int totalSize) {
        this.totalSize = totalSize;
        if (totalSize != 0) {
            this.totalPage = (totalSize % pageSize == 0) ? (totalSize / pageSize) : (totalSize / pageSize + 1);
        }
    }

    public Map<String, Object> getQuery() {
        return query;
    }

    public void setQuery(Map<String, Object> query) {
        this.query = query;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    public Integer getStartNo() {
        return ((pageNo - 1) * pageSize);
    }

    public void setStartNo(Integer startNo) {
        this.startNo = startNo;
    }
}
