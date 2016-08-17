package com.navinfo.dataservice.web.man.page;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: Page
 * @author songdongyan
 * @date 2016年8月2日
 * @Description: Page.java
 */
public class Page {

	/**
	 * 
	 */

	private static final long serialVersionUID = -3167350028645121914L;
	
	@ApiModelProperty(position = 1, required = true, value = "起始数")
	private int start =0;
	@ApiModelProperty(position = 1, required = true, value = "页容量")
	private int pageSize = SystemConfigFactory.getSystemConfig().getIntValue("UI.pageSize",20);
	@ApiModelProperty(position = 1, required = true, value = "总数")
	private int totalCount = -1;
	@ApiModelProperty(position = 1, required = true, value = "页码")
	private int pageNum;
	
	public Page() {
	}

	public Page(int pageNum) {
		this.pageNum=pageNum;
		start=(pageNum-1)*pageSize;
	}

	public Page(int start,int limit)
    {
        this.start = start;
        this.pageSize = limit;
    }

	/**
	 * 总记录数.
	 */
	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * 计算总页数.
	 */
	public int getTotalPages() {
		if (totalCount == -1)
			return -1;

		int count = totalCount / pageSize;
		if (totalCount % pageSize > 0) {
			count++;
		}
		return count;
	}
	public void setStart(int start){
		this.start = start;
	}
	public int getStart() {
		return start;
	}
	public void setPageSize(int pageSize) {
		this.pageSize=pageSize;
		this.start=(pageNum-1)*pageSize;
	}
	public int getPageSize() {
		return pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	
	public int getEnd() {
		return start+pageSize;
	}

}
