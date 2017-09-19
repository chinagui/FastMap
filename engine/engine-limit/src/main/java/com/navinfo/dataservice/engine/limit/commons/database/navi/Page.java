package com.navinfo.dataservice.engine.limit.commons.database.navi;

import com.navinfo.dataservice.engine.limit.commons.config.SystemConfigFactory;


/**
 * 封装分页和查询的结果.
 * 
 * @param Page中的记录类型.
 */
public class Page implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3167350028645121914L;
	private int start =0;
	private int pageSize = SystemConfigFactory.getSystemConfig().getIntValue("UI.pageSize",20);
	private int totalCount = -1;
	private Object result = null;
	private int pageNum;
	public Page() {
	}

	public Page(int pageNum) {
		this.pageNum=pageNum;
		start=(pageNum-1)*pageSize;
	}

	public Page(int start, int limit)
    {
        this.start = start;
        this.pageSize = limit;
    }

	/**
	 * 页内的数据列表.
	 */
	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
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

	public int getStart() {
		return start;
	}

	
	public int getEnd() {
		return start+pageSize;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public int thePageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize=pageSize;
		this.start=(pageNum-1)*pageSize;
	}

	

	

}
