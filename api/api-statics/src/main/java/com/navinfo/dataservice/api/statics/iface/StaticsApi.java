package com.navinfo.dataservice.api.statics.iface;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;

/**
 * @ClassName: DatahubApiService
 * @author Xiao Xiaowen
 * @date 2016年6月6日 下午8:30:06
 * @Description: TODO
 * 
 */
public interface StaticsApi {
	public List<GridStatInfo> getLatestCollectStatByGrids(List<String> grids)
			throws Exception;

	public List<GridStatInfo> getLatestDailyEditStatByGrids(List<String> grids)
			throws Exception;

	public List<GridStatInfo> getLatestMonthlyEditStatByGrids(List<String> grids)
			throws Exception;

	/**
	 * 
	 * 获取变迁统计信息
	 * 
	 * @param grids
	 * @param type
	 * @param stage
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public List<GridChangeStatInfo> getChangeStatByGrids(Set<String> grids, int type,
			int stage, String date) throws Exception;

	public Map<Integer, Integer> getExpectStatusByBlocks(Set<Integer> blocks);
}
