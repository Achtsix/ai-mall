package com.aimall.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private int pageNum;
    private int pageSize;
    private List<T> list;

    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(pageInfo.getTotal());
        r.setPageNum(pageInfo.getPageNum());
        r.setPageSize(pageInfo.getPageSize());
        r.setList(pageInfo.getList());
        return r;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setTotal(total);
        r.setPageNum(pageNum);
        r.setPageSize(pageSize);
        return r;
    }
}
