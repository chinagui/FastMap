package com.navinfo.dataservice.api.fcc.iface;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface FccApi {
	
	public JSONArray searchDataBySpatial(String wkt, int editTaskId, int type, JSONArray stages) throws Exception;
	
	/**
	 * @Description:根据grid，查询子tips的数据总量和已完成量，
	 * @param grids
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-10-25 上午10:57:37
	 */
	public JSONObject getSubTaskStats(JSONArray grids) throws Exception;
	/**
	 * @Description:根据wkt，查询子tips的数据总量和已完成量，
	 * @param wkt
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-10-25 上午10:57:37
	 */
	public JSONObject getSubTaskStatsByWkt(String wkt, Set<Integer> collectTaskIds) throws Exception;
	
	
	public void tips2Aumark(JSONObject parameter) throws Exception;
	
	/**
	 * @Description: 快转中1：获取快线采集任务包含的tips的grids
	 * @param collectTaskid:快线采集任务号
	 * @return
	 * @author: y
	 * @time:2017-4-19 下午8:25:41
	 */
	public Set<Integer>  getTipsGridsBySqTaskId(int collectTaskid) throws Exception;
	
	
	/**
	 * @Description:快转中2：根据grid-taskMap批tips中线任务id
	 * @param sQTaskId：快线任务号
	 * @param gridMTaskMap
	 * @throws Exception
	 * @author: y
	 * @time:2017-4-19 下午8:27:17
	 */
	public void batchUpdateSmTaskId(int sQTaskId, Map<Integer,Integer> gridMTaskMap) throws Exception;
	
	
	/**
	 * @Description: 动态调整：获取快线采集任务包含的tips的grids
	 * @param subTaskid:采集子任务号
	 * @param programType：任务类型
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-4-19 下午8:31:39
	 */
	
	public Set<Integer>  getTipsGridsBySubtaskId(int subTaskid,int programType) throws Exception;


    /**
     * 快线tips日编状态实时统计
     * @param collectTaskIds
     * @return
     * @throws Exception
     */
    public List<Map> getCollectTaskTipsStats(Set<Integer> collectTaskIds) throws Exception;

//    /**
//     * 根据rowkey列表批快线的任务，子任务号
//     * @param taskId
//     * @param subtaskId
//     * @param tips
//     * @throws Exception
//     */
//    public void batchQuickTask(int taskId, int subtaskId, List<String> tips) throws Exception;
//
//    /**
//     * 根据rowkey列表批中线任务号
//     * @param taskId
//     * @param tips
//     * @throws Exception
//     */
//    public void batchMidTask(int taskId, List<String> tips) throws Exception;
}
