package com.navinfo.dataservice.api.statics.iface;

import java.util.List;

import com.navinfo.dataservice.api.statics.model.GridStatInfo;


/** 
* @ClassName: DatahubApiService 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午8:30:06 
* @Description: TODO
*  
*/
public interface StaticsApi {
	List<GridStatInfo> getCollectStatByGrids(List<String> grids)throws Exception;
	
	List<GridStatInfo> getDailyEditStatByGrids(List<String> grids)throws Exception;
	
	List<GridStatInfo> getMonthlyEditStatByGrids(List<String> grids)throws Exception;
}
