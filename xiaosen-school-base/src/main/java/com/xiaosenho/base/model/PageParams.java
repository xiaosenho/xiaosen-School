package com.xiaosenho.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author: 作者
 * @create: 2025-02-13 19:04
 * @Description: 分页查询参数
 */
@Data
@ToString
public class PageParams {
    //页码
    private Long pageNo=1L;
    //每页显示记录数
    private Long pageSize=10L;

    private PageParams(){

    }

    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
