package com.navinfo.dataservice.web.man.page;

import java.util.List;
import com.navinfo.dataservice.api.man.model.subtask.SubtaskListByUser;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskListByUserPage
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskListByUserPage.java
 */
public class SubtaskListByUserPage extends Page {

	/**
	 * 
	 */
	public SubtaskListByUserPage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param pageNum
	 */
	public SubtaskListByUserPage(int pageNum) {
		super(pageNum);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param start
	 * @param limit
	 */
	public SubtaskListByUserPage(int start, int limit) {
		super(start, limit);
		// TODO Auto-generated constructor stub
	}
	
	@ApiModelProperty(position = 1, required = true, value = "子任务信息")
	private List<SubtaskListByUser> result = null;
	/**
	 * 页内的数据列表.
	 */
	public List<SubtaskListByUser> getResult() {
		return result;
	}

	public void setResult(List<SubtaskListByUser> result) {
		this.result = result;
	}
	
	public SubtaskListByUserPage(int pageSize,int pageNum,int start,int totalCount,List<SubtaskListByUser> result){
		super.setPageSize(pageSize);
		super.setPageNum(pageNum);
		super.setStart(start);
		super.setTotalCount(totalCount);
		this.result = result;
	}

}
