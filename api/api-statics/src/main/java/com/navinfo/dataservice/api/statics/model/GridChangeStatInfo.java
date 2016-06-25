package com.navinfo.dataservice.api.statics.model;

import java.io.Serializable;

/**
 * grid变迁统计信息
 * 
 * @author wangshishuai3966
 *
 */
public class GridChangeStatInfo implements Serializable {

	private String gridId;
	
	private int percent;

	public String getGridId() {
		return gridId;
	}

	public void setGridId(String gridId) {
		this.gridId = gridId;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}
	
}
