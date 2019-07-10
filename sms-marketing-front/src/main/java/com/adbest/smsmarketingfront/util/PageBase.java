package com.adbest.smsmarketingfront.util;

import com.querydsl.core.QueryResults;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * 分页基础类型
 * 限制了最大页容量
 */
public class PageBase {
    
    private final int MAX_PAGE_SIZE = 1000;
    
    protected int page = 0;
    protected int size = 10;
    
    public PageBase() {
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
            this.size = MAX_PAGE_SIZE;
        } else if (size < 1) {
            this.size = 1;
        } else {
            this.size = size;
        }
    }
    
    /**
     * @param queryResults
     * @param p
     * @param <T>
     * @param <P>
     * @return
     * @deprecated
     */
    public static <T, P extends PageBase> Page<T> toPageEntity(QueryResults<T> queryResults, P p) {
        Page<T> pageEntity = new PageImpl<>(
                queryResults.getResults(),
                PageRequest.of(p.page, p.size),
                queryResults.getTotal()
        );
        return pageEntity;
    }
    
    /**
     * 将查询结果转化为分页实体
     * 推荐使用本方法
     *
     * @param queryResults  jpa 查询结果集
     * @param <T>
     * @return
     */
    public <T> Page<T> toPageEntity(QueryResults<T> queryResults) {
        return new PageImpl<>(queryResults.getResults(), PageRequest.of(this.page, this.size), queryResults.getTotal());
    }
    
    /**
     * 将指定的数据列表和查询的数据总数 转化为 分页实体
     * @param list  数据列表
     * @param total  数据总数
     * @param <T>
     * @return
     */
    public  <T> Page<T> toPageEntity(List<T> list, long total) {
        return new PageImpl<>(list, PageRequest.of(this.page, this.size), total);
    }
}
