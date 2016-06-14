package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;

/*
 * @author mayunfei
 * 2016年6月8日
 * 描述：RegionInfo的数据
 */
public  interface IRegion extends Serializable {
	
	public Integer getRegionId() ;	
	public Integer getDailyDbId() ;
	
	public Integer getMonthlyDbId() ;
	
	
}


