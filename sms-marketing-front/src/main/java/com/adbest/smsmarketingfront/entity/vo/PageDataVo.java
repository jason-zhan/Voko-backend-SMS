package com.adbest.smsmarketingfront.entity.vo;

import java.util.List;
import java.util.Map;

import com.adbest.smsmarketingfront.handler.ServiceException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@JsonIgnoreProperties({"queryMap"})
public class PageDataVo {
	
	private List<?>	list;
	
	/**
	 * 总数
	 */
    private long count;
    
    /**
     * 每页条数
     */
    private int	size = 20;
   
    /**
     *当前页 
     */
    private int	start = 0;
    
    /**
     * 所有页数
     */
    private int pages;
    
    /**
     * 查询条件参数
     */
    private Map<String, Object> queryMap = Maps.newHashMap();
    
	public PageDataVo() {
		super();
	}

	public Map<String, Object> getQueryMap() {
		return queryMap;
	}

	public void setQueryMap(Map<String, Object> queryMap) {
		this.queryMap = queryMap;
	}
	
	public void putSearchParam(String key, Object value) {
    	queryMap.put(key, value);
    }

    public PageDataVo(Page page){
		this.list = page.getContent();
		this.count = page.getTotalElements();
		this.size = page.getSize();
		this.start = page.getNumber();
		this.pages = (int) (count%size==0?count/size:count/size+1);
	}
}
