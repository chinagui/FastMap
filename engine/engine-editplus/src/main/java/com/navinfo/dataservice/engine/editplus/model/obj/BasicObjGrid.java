package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.List;
import java.util.Set;

/** 
 * @ClassName: BasicObjGrid
 * @author songdongyan
 * @date 2016年11月24日
 * @Description: BasicObjGrid.java
 */
public class BasicObjGrid {

	protected Set<String> gridListBefore;
	protected Set<String> gridListAfter;
	
	public Set<String> getGridListBefore() {
		return gridListBefore;
	}
	public void setGridListBefore(Set<String> girdListBefore) {
		this.gridListBefore = girdListBefore;
	}
	public Set<String> getGridListAfter() {
		return gridListAfter;
	}
	public void setGridListAfter(Set<String> gridListAfter) {
		this.gridListAfter = gridListAfter;
	}
	/**
	 * 
	 */
	public BasicObjGrid() {
		// TODO Auto-generated constructor stub
	}

}
