package com.lxz.base.model;

import io.swagger.annotations.ApiModelProperty;

public class PageParams {
    // 当前页码
    @ApiModelProperty(value = "当前页码", example = "1")
    private  Long pageNo = 1L;
    // 每页记录数
    @ApiModelProperty(value = "每页记录数", example = "10")
    private  Long pageSize = 10L;

    public PageParams() {
    }
    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
