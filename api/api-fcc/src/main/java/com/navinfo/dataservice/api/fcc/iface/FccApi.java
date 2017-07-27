package com.navinfo.dataservice.api.fcc.iface;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface FccApi {
	
	public JSONArray searchDataBySpatial(String wkt, int editTaskId, int type, JSONArray stages) throws Exception;
	
//	/**
//	 * @Description:根据grid，查询子tips的数据总量和已完成量，
//	 * @param grids
//	 * @return
//	 * @throws Exception
//	 * @author: y
//	 * @time:2016-10-25 上午10:57:37
//	 */
//	public JSONObject getSubTaskStats(JSONArray grids) throws Exception;
	
	/**
	 * @Description:根据wkt+collectTaskIds，查询子tips的数据总量和待作业量，web编辑卡片统计
	 * @param wkt: 任务wkt
	 * @param collectTaskIds:采集子任务号（目前默认都是快线）
	 * @param taskType：任务类型：作业任务，1.是质检任务
	 * @param handler：质检员id(作业任务不用，可以传递0)
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-7-12 下午5:46:13
	 */
	public JSONObject getSubTaskStatsByWkt(int subtaskId, String wkt, int subTaskType,int handler, int isQuality) throws Exception;
	
	
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

    /**
     * 根据rowkey列表批快线的任务，子任务号
     * @param taskId
     * @param subtaskId
     * @param tips
     * @throws Exception
     */
    public void batchQuickTask(int taskId, int subtaskId, List<String> tips) throws Exception;

    /**
     * tips无任务批中线任务号api
     * @param wkt
     * @param midTaskId
     * @throws Exception
     */
    public void batchNoTaskDataByMidTask(String wkt,int midTaskId) throws Exception;
    
    
    /**
     * 根据质检任务号，获取质检作业量
     * @param checkSubTaskId
     * 返回：
     * key1:	"checkCount" --之间数据量
	 * key2:	"tipsTypeCount" --tips类型量
	 * desc:未抽取返回value都为0
     * @throws Exception
     */
    public Map<String,Integer> getCheckTaskCount(int checkSubTaskId) throws Exception;

    /**
     * 获取该任务下tips前后图幅
     * @param collectTaskSet
     * @return
     */
    public Set<Integer> getTipsMeshIdSet(Set<Integer> collectTaskSet,int taskType) throws Exception;

}
