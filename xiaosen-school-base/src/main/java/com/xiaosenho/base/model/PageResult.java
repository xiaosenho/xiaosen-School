package com.xiaosenho.base.model;

import io.swagger.annotations.ApiResponse;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-13 19:22
 * @Description: 分页查询结果
 */
@Data
@ToString
public class PageResult<T> implements Serializable {
    private List<T> items;
    private long counts;
    private long page;
    private long pageSize;

    public PageResult(List<T> items, long counts, long page, long pageSize) {
        this.items = items;
        this.counts = counts;
        this.page = page;
        this.pageSize = pageSize;
    }
}
