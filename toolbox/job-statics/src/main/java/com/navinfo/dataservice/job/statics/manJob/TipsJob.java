package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 *  
 *  Tips统计job
 *  
 *  Collection:
	Subtask_tips
	只查中线子任务
	字段
	subtaskId, firstCollectDate,tipsTotal
	子任务-实际开始日期-tips/day：（需求待确认）(采集子任务)第一条数据采集的日期
	任务-众包tips作业量【MT-CR-12】-tips：（中线-采集任务）如果采集任务的采集方式包含众包，则查找任务下所有“采集方式=众包”的子任务，根据子任务Id统计tips(同样，任务-情报矢量tips作业量【MT-CR-13】-tips：（中线-采集任务）如果采集任务的采集方式包含情报矢量，则查找任务下“采集方式=情报矢量”的子任务，根据子任务Id统计tips)
	
	Collection:
	Task_grid_tips
	中线、快线任务均查询
	字段
	taskId,tipsAddLen,tipsUploadNum
	任务-道路实际作业量【QT-CR-2】-tips：（快线-采集任务）采集任务中所有新增测线tips，统计link里程(同样，任务-道路实际作业里程【MT-CR-7】-day：（中线-采集任务）采集上传现场轨迹匹配的link里程+根据采集任务ID，查找所有新增测线tips，统计里程；任务-新增里程【MT-CR-10】-day：（中线-采集任务）根据采集任务ID，查找所有新增测线tips，统计里程)
	任务-采集上传tips个数【QT-CR-3】-tips：（快线-采集任务）根据采集任务ID直接获取（同样，任务-采集上传tips个数【MT-CR-11】-tips：（中线-采集任务）根据采集任务ID直接获取）
	
	
	Collection:
	Grid_tips
	字段
	gridId, subtaskEditAllNum, subtaskEditFinishNum, taskEditAllNum,taskNoeditAllNum,taskCreateByEditNum, taskEditFinishNum,noTaskTotal
	子任务-日编tips总量【QS-D-1】-tips：（快线日编grid粗编子任务），快线任务id！=0，t_tipStatus=2(已完成)且t_dEditStatus=2(已完成)
	子任务-日编tips完成个数【QS-D-2】-tips：（快线日编grid粗编子任务），快线任务id！=0，t_tipStatus=2(已完成)
	任务-日编tips总量【QT-D-1】-tips：（快线-日编任务），快线任务id！=0，t_tipStatus=2(已完成)且stage=1，2，6，7注：需关联配置表，排除不需要日编grid粗编作业的tips
	任务-日编不作业tips总量【QT-D-2】-tips：（快线-日编任务），快线任务id！=0，t_tipStatus=2(已完成)且stage=1，2，6，7注：需关联配置表，统计不需要日编作业的tips
	任务-内业生成tips个数【QT-D-3】-tips：（快线-日编任务），快线任务id！=0，t_tipStatus=2(已完成)且80开头类型的tips
	任务-日编tips完成个数【QT-D-4】-tips：（快线-日编任务），快线任务id！=0，t_tipStatus=2(已完成)且t_dEditStatus=2(已完成)且stage=1，2，6，7
	城市-无任务Tips数量-tips：中线快线任务id均为0的，track.t_tipStatus=2(已完成)且tips类型(s_sourceType)不为80开头的数据（同样，区县-无任务Tips数量-tips：track.t_tipStatus=2(已完成)且tips类型(s_sourceType)不为80开头的数据）

 * @author sjw
 *
 */
public class TipsJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	/**
	 * @param jobInfo
	 */
	public TipsJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.job.statics.AbstractStatJob#stat()
	 */
	@Override
	public String stat() throws JobException {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getTipsIdxConnection();
			long t = System.currentTimeMillis();
			log.debug("所有Tips数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			Map<String,List<Map<String,Object>>> result = new HashMap<String,List<Map<String,Object>>>();
			result.put("subtask_tips", getSubtaskTips(conn));
			result.put("task_grid_tips", getTaskGridTips(conn));
			result.put("grid_tips", getGridTips(conn));

			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	
	public List<Map<String, Object>> getGridTips(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String, Object>> gridTipsTaskList = new ArrayList<>();
		Map<String, Object> gridTipsTaskMap = new HashMap<>();
		List<Map<String, Object>> gridTipsNoTaskList = new ArrayList<>();
		Map<String, Object> gridTipsNoTaskMap = new HashMap<>();
		List<Integer> gridTaskList = new ArrayList<>();
		List<Integer> gridNoTaskList = new ArrayList<>();
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		try{
			
			int gridId = 0;
			int subtaskEditAllNum = 0; 
			int subtaskEditFinishNum = 0; 
			int taskEditAllNum = 0; 
			int taskNoEditAllNum = 0; 
			int taskCreateByEditNum = 0; 
			int taskEditFinishNum = 0; 
			int noTaskTotal = 0;
			
			String sql = "SELECT wkt,t_dEditStatus,stage,s_sourceType FROM tips_index WHERE s_qtaskid <> 0 AND t_tipStatus = 2 ORDER BY s_qtaskid" ;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				
				int grid = getGrid(rs);
				
				if(gridId!=grid&&(!gridTaskList.contains(grid))) {
					gridTipsTaskMap = new HashMap<>(); 
					gridId = grid;
					gridTipsTaskMap.put("gridId",gridId);
					gridTipsTaskList.add(gridTipsTaskMap);
					gridTaskList.add(gridId);
					subtaskEditAllNum = 0; 
					subtaskEditFinishNum = 0; 
					taskEditAllNum = 0; 
					taskNoEditAllNum = 0; 
					taskCreateByEditNum = 0; 
					taskEditFinishNum = 0;
				}
				int stage = rs.getInt("stage");
				if(rs.getInt("t_dEditStatus")==2){
					subtaskEditAllNum++;
					if(stage==1||stage==2||stage==6||stage==7){
						taskEditFinishNum++;
					}
				}
				
				subtaskEditFinishNum++;
				
				String sourceType = rs.getString("s_sourceType");
				
				if(sourceType.startsWith("80")){
					taskCreateByEditNum++;
				}
				
				Integer editMeth = metaApi.queryEditMethTipsCode(sourceType);
				if(editMeth!=null){
					if(stage==1||stage==2||stage==6||stage==7){
						if(editMeth!=1){
							taskEditAllNum++;
							if(editMeth==0){
								taskNoEditAllNum++;
							} 
						}
					}
					
				}
				
				
				gridTipsTaskMap.put("subtaskEditAllNum", subtaskEditAllNum);
				gridTipsTaskMap.put("subtaskEditFinishNum", subtaskEditFinishNum);
				gridTipsTaskMap.put("taskEditAllNum", taskEditAllNum);
				gridTipsTaskMap.put("taskNoEditAllNum", taskNoEditAllNum);
				gridTipsTaskMap.put("taskCreateByEditNum", taskCreateByEditNum);
				gridTipsTaskMap.put("taskEditFinishNum", taskEditFinishNum);
				gridTipsTaskMap.put("noTaskTotal", 0);
				
			}
			
			
			
			String sql1 = "SELECT wkt FROM tips_index WHERE S_QTASKID = 0 AND S_MTASKID = 0 AND t_tipStatus = 2 AND s_sourceType not like '80%'" ;
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				
				int grid = getGrid(rs);
				
				if(gridId!=grid&&(!gridNoTaskList.contains(grid))) {
					gridTipsNoTaskMap = new HashMap<>(); 
					gridId = grid;
					gridTipsNoTaskMap.put("gridId",gridId);
					gridTipsNoTaskList.add(gridTipsNoTaskMap);
					gridNoTaskList.add(gridId);
					noTaskTotal = 0;
				}
				
				noTaskTotal++;
				
				gridTipsNoTaskMap.put("subtaskEditAllNum", 0);
				gridTipsNoTaskMap.put("subtaskEditFinishNum", 0);
				gridTipsNoTaskMap.put("taskEditAllNum", 0);
				gridTipsNoTaskMap.put("taskNoEditAllNum", 0);
				gridTipsNoTaskMap.put("taskCreateByEditNum", 0);
				gridTipsNoTaskMap.put("taskEditFinishNum", 0);
				gridTipsNoTaskMap.put("noTaskTotal",noTaskTotal);
				
			}
	
			
			
			for (Map<String, Object> tipsNoTaskMap : gridTipsNoTaskList) {
				for (Map<String, Object> tipsTaskMap: gridTipsTaskList) {
					if((int)tipsTaskMap.get("gridId")==(int)tipsNoTaskMap.get("gridId")){
						tipsTaskMap.put("noTaskTotal", tipsNoTaskMap.get("noTaskTotal"));
						break;
					}
				}
			}
			
			
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new ThreadExecuteException("Tips数据统计失败");
		}
		return gridTipsTaskList;
	}

	public Integer getGrid(ResultSet rs) throws SQLException{
		STRUCT geoStruct=(STRUCT) rs.getObject("wkt");
		Geometry geometry = null;
		try {
			geometry = GeoTranslator.struct2Jts(geoStruct);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Coordinate coordinate = geometry.getCoordinate();
		double x = coordinate.x;
		double y = coordinate.y;
		String[] grids = CompGridUtil.point2Grids(x, y);
		Integer grid = Integer.parseInt(grids[0]);
		return grid;
	}
	
	public List<Map<String, Object>> getTaskGridTips(Connection conn) {
		List<Map<String, Object>> tipsAddLenList = new ArrayList<>();
		Map<String, Object> tipsAddLenMap = null;
		List<Map<String, Object>> tipsUploadNumList = new ArrayList<>();
		List<Map<String, Object>> taskGridTipsList = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			String baseAddLenSql = " WKTLOCATION from tips_index where s_sourceType in (2001,2002) AND t_lifecycle=3 " ;
			String sql = "SELECT s_mtaskid,"+baseAddLenSql+" AND s_mtaskid <> 0 ORDER BY s_mtaskid";//中线新增测线
			int taskId = 0;
			double tipsAddLen = 0.0;
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			compomentTipsAddLenMap(pstmt, rs, taskId, tipsAddLen, tipsAddLenList, tipsAddLenMap);
			
			String sql1 = "SELECT s_qtaskid,"+baseAddLenSql+" AND s_qtaskid <> 0 ORDER BY s_qtaskid";//快线新增测线
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			
			compomentTipsAddLenMap(pstmt, rs, taskId, tipsAddLen, tipsAddLenList, tipsAddLenMap);
			
			String baseTipsUploadNumSql = " COUNT(1) FROM tips_index WHERE t_tipstatus =2 AND " ;
			String sql2 = "SELECT s_mtaskid,"+baseTipsUploadNumSql+" s_mtaskid <> 0 GROUP BY s_mtaskid";//中线采集上传
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			
			compomentTipsUploadNumMap(rs, tipsUploadNumList);
			
			String sql3 = "SELECT s_qtaskid,"+baseTipsUploadNumSql+" s_qtaskid <> 0 GROUP BY s_qtaskid";//快线采集上传
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			
			compomentTipsUploadNumMap(rs, tipsUploadNumList);
			
			for (int i = tipsAddLenList.size()-1; i >=0 ; i--) {
				Map<String, Object> map = tipsAddLenList.get(i);
				for (int j = tipsUploadNumList.size()-1; j >=0 ; j--) {
					Map<String, Object> tipsUploadNumMap = tipsUploadNumList.get(j);
					if((int)map.get("taskId")==(int)tipsUploadNumMap.get("taskId")){
						map.putAll(tipsUploadNumMap);
						taskGridTipsList.add(map);
						tipsUploadNumList.remove(j);
						tipsAddLenList.remove(i);
						break;
					}
				}
			}
			
			
			for (Map<String, Object> map : tipsAddLenList) {
				map.put("tipsUploadNum", 0);
			}
			
			for (Map<String, Object> map : tipsUploadNumList) {
				map.put("tipsAddLen", "0");
			}
			
			taskGridTipsList.addAll(tipsAddLenList);
			taskGridTipsList.addAll(tipsUploadNumList);
			
			log.debug("taskGridTipsList-------"+ JSONArray.fromObject(taskGridTipsList));
			
			return taskGridTipsList;

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ThreadExecuteException("Tips数据统计失败");
		}
	}
	
	public void compomentTipsUploadNumMap(ResultSet rs,List<Map<String, Object>> tipsUploadNumList) throws SQLException{
		while(rs.next()){
			Map<String, Object> tipsUploadNumMap = new HashMap<>(); 
			tipsUploadNumMap.put("taskId",rs.getInt(1));
			tipsUploadNumMap.put("tipsUploadNum",rs.getInt(2));
			tipsUploadNumList.add(tipsUploadNumMap);
		}
	}
	
	
	public void compomentTipsAddLenMap(PreparedStatement pstmt,ResultSet rs,int taskId,double tipsAddLen,
			List<Map<String, Object>> taskGridTipsList,Map<String, Object> taskGridTipsMap) throws SQLException{
		while(rs.next()){
			if(taskId!=rs.getInt(1)) {
				taskGridTipsMap = new HashMap<>(); 
				taskId = rs.getInt(1);
				taskGridTipsMap.put("taskId",taskId);
				taskGridTipsList.add(taskGridTipsMap);
			}
			 
			
			STRUCT geoStruct=(STRUCT) rs.getObject("wktlocation");
			Geometry geometry = null;
			try {
				geometry = GeoTranslator.struct2Jts(geoStruct);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			
			tipsAddLen+=GeometryUtils.getLinkLength(geometry);//根据geometry求里程
			DecimalFormat df=new DecimalFormat("0.00");
			taskGridTipsMap.put("tipsAddLen",df.format(tipsAddLen).toString());
		}
	}


	/**
	 * 获取子任务统计tips
	 * @return
	 */
	public List<Map<String,Object>> getSubtaskTips(Connection conn) {
		final List<Map<String, Object>> subtaskTipsList = new ArrayList<>();
			try{
				QueryRunner run = new QueryRunner();
				
				String sql = "SELECT s_msubtaskid,COUNT(1) FROM tips_index WHERE s_msubtaskid <> 0 GROUP BY s_msubtaskid";
				
				return run.query(conn, sql,new ResultSetHandler<List<Map<String, Object>>>() {
					@Override
					public List<Map<String, Object>> handle(ResultSet rs)
							throws SQLException {
						while(rs.next()){
							Map<String, Object> subtaskTipsMap = new HashMap<>(); 
							subtaskTipsMap.put("subtaskId",rs.getInt(1));
							subtaskTipsMap.put("tipsTotal",rs.getInt(2));
							subtaskTipsList.add(subtaskTipsMap);
						}
						return subtaskTipsList;
					}
				});
				

			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("Tips数据统计失败");
			}
		
	 }
	
	

}
