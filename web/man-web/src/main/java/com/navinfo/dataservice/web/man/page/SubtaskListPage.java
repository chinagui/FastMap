package com.navinfo.dataservice.web.man.page;

import java.util.List;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.subtask.SubtaskList;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskListPage
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskListPage.java
 */
public class SubtaskListPage extends Page {

	/**
	 * 
	 */
	public SubtaskListPage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param pageNum
	 */
	public SubtaskListPage(int pageNum) {
		super(pageNum);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param start
	 * @param limit
	 */
	public SubtaskListPage(int start, int limit) {
		super(start, limit);
		// TODO Auto-generated constructor stub
	}
	@ApiModelProperty(position = 1, required = true, value = "子任务信息")
	private List<SubtaskList> result = null;
	/**
	 * 页内的数据列表.
	 */
	public List<SubtaskList> getResult() {
		return result;
	}

	public void setResult(List<SubtaskList> result) {
		this.result = result;
	}
	
	public SubtaskListPage(int pageSize,int pageNum,int start,int totalCount,List<SubtaskList> result){
		super.setPageSize(pageSize);
		super.setPageNum(pageNum);
		super.setStart(start);
		super.setTotalCount(totalCount);
		this.result = result;
	}

}
