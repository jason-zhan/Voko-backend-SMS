package com.adbest.smsmarketingfront.util;

import com.querydsl.core.QueryResults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * 分页基础类型
 * 限制了最大页容量
 */
public class PageBase {

    private final int MAX_PAGE_SIZE = 1000;

    protected int page = 0;
    protected int size = 10;

    protected PageBase() {
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page < 0 ? 0 : page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        if (size < 1) {
            size = 1;
        }
        this.size = size;
    }

    public static <T, P extends PageBase> Page<T> toPageEntity(QueryResults<T> queryResults, P p) {
        Page<T> pageEntity = new PageImpl<>(
                queryResults.getResults(),
                PageRequest.of(p.page, p.size),
                queryResults.getTotal()
        );
        return pageEntity;
    }
}
