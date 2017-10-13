package com.navinfo.dataservice.job.statics.manJob;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
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
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

/**
 *  
 * Tips统计job
 * @author sjw
 *
 */
public class TipsJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	public TipsJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getTipsIdxConnection();
			long t = System.currentTimeMillis();
			
			Map<String,List<Map<String,Object>>> result = new HashMap<String,List<Map<String,Object>>>();
			result.put("subtask_tips", getSubtaskTips(conn));
			result.put("task_grid_tips", getTaskGridTips(conn));
			result.put("grid_task_tips", getGridTaskTips(conn));
			result.put("grid_notask_tips", getGridNoTaskTips(conn));
			log.debug("所有Tips数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");

			return JSONObject.fromObject(result).toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	public  List<Map<String, Object>> getGridTaskTips(Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String, Object>> gridTipsTaskList = new ArrayList<>();
		Map<String, Object> tipsTaskMap = new HashMap<>();
		Map<String,Map<String, Object>> gridTipsTaskMap = new HashMap<>();
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String,Integer> codeEditMethMap  = metaApi.queryEditMethTipsCode();
		try{
			String sql = "SELECT s_qtaskid,wkt,t_dEditStatus,stage,s_sourceType FROM tips_index WHERE s_qtaskid <> 0 AND t_tipStatus = 2 ORDER BY s_qtaskid" ;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				int gridId = getGrid(rs);
				int taskId = rs.getInt("s_qtaskid");
				String key = gridId + "_" + taskId;
				
				if(!gridTipsTaskMap.containsKey(key)) {
					tipsTaskMap = new HashMap<>(); 
					tipsTaskMap.put("gridId",gridId);
					tipsTaskMap.put("taskId",taskId);
					gridTipsTaskList.add(tipsTaskMap);
					gridTipsTaskMap.put(key, tipsTaskMap);
				}
				
				tipsTaskMap = gridTipsTaskMap.get(key);
				
//				int subtaskEditAllNum = tipsTaskMap.containsKey("subtaskEditAllNum")?(int) tipsTaskMap.get("subtaskEditAllNum"):0;
//				int subtaskEditFinishNum = tipsTaskMap.containsKey("subtaskEditFinishNum")?(int)tipsTaskMap.get("subtaskEditFinishNum"):0;
				int taskEditAllNum = tipsTaskMap.containsKey("taskEditAllNum")?(int)tipsTaskMap.get("taskEditAllNum"):0;
				int taskNoEditAllNum = tipsTaskMap.containsKey("taskNoEditAllNum")?(int)tipsTaskMap.get("taskNoEditAllNum"):0;
				int taskCreateByEditNum = tipsTaskMap.containsKey("taskCreateByEditNum")?(int) tipsTaskMap.get("taskCreateByEditNum"):0;
				int taskEditFinishNum = tipsTaskMap.containsKey("taskEditFinishNum")?(int)tipsTaskMap.get("taskEditFinishNum"):0;
				
				int stage = rs.getInt("stage");
				if(rs.getInt("t_dEditStatus")==2){
//					subtaskEditAllNum++;
					if(stage==1||stage==2||stage==6||stage==7){
						taskEditFinishNum++;
					}
				}
				
//				subtaskEditFinishNum++;
				
				String sourceType = rs.getString("s_sourceType");
				
				if(sourceType.startsWith("80")){
					taskCreateByEditNum++;
				}
				
				Integer editMeth = null;
				if(codeEditMethMap.containsKey(sourceType)){
					editMeth = codeEditMethMap.get(sourceType);
				}
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
//				tipsTaskMap.put("subtaskEditAllNum", subtaskEditAllNum);
//				tipsTaskMap.put("subtaskEditFinishNum", subtaskEditFinishNum);
				tipsTaskMap.put("taskEditAllNum", taskEditAllNum);
				tipsTaskMap.put("taskNoEditAllNum", taskNoEditAllNum);
				tipsTaskMap.put("taskCreateByEditNum", taskCreateByEditNum);
				tipsTaskMap.put("taskEditFinishNum", taskEditFinishNum);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new ThreadExecuteException("Tips数据统计失败");
		}finally{
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
		}
		return gridTipsTaskList;
	}
	
	public List<Map<String, Object>> getGridNoTaskTips(Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String, Object>> gridTipsTaskList = new ArrayList<>();
		Map<String, Object> tipsTaskMap = new HashMap<>();
		Map<Integer,Map<String, Object>> gridTipsTaskMap = new HashMap<>();
		try{
			String sql1 = "SELECT wkt FROM tips_index WHERE S_QTASKID = 0 AND S_MTASKID = 0 AND t_tipStatus = 2 AND s_sourceType not like '80%'" ;
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				int gridId = getGrid(rs);
				
				if(!gridTipsTaskMap.containsKey(gridId)) {
					tipsTaskMap = new HashMap<>(); 
					tipsTaskMap.put("gridId",gridId);
					gridTipsTaskList.add(tipsTaskMap);
					gridTipsTaskMap.put(gridId, tipsTaskMap);
				}				
				
				tipsTaskMap = gridTipsTaskMap.get(gridId);
				
				int noTaskTotal = tipsTaskMap.containsKey("noTaskTotal")?(int)tipsTaskMap.get("noTaskTotal"):0;
				noTaskTotal++;
				
				tipsTaskMap.put("noTaskTotal",noTaskTotal);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new ThreadExecuteException("Tips数据统计失败");
		}finally{
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
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
		List<Map<String, Object>> tipsUploadNumList = new ArrayList<>();
		List<Map<String, Object>> taskGridTipsList = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        Table htab = null;

		try{
            htab = HBaseConnector.getInstance().getConnection()
                    .getTable(TableName.valueOf(HBaseConstant.tipTab));

			String baseAddLenSql = " WKTLOCATION,ID,S_SOURCETYPE from tips_index where s_sourceType = '2001' AND t_lifecycle=3 " ;
			String sql = "SELECT s_mtaskid,"+baseAddLenSql+" AND s_mtaskid <> 0 ORDER BY s_mtaskid";//中线新增测线

			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			compomentTipsAddLenMap(rs,tipsAddLenList,htab);
			
			String sql1 = "SELECT s_qtaskid,"+baseAddLenSql+" AND s_qtaskid <> 0 ORDER BY s_qtaskid";//快线新增测线
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			
			compomentTipsAddLenMap(rs,tipsAddLenList,htab);
			
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
		}finally{
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
            if(htab != null) {
                try {
                    htab.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
	
	
	public void compomentTipsAddLenMap(ResultSet rs,List<Map<String, Object>> taskGridTipsList, Table htab) throws SQLException, IOException {
		int taskId = 0;
		Map<String, Object> taskGridTipsMap = null;
		DecimalFormat df=new DecimalFormat("0.00");
		while(rs.next()){
			if(taskId!=rs.getInt(1)) {
				taskGridTipsMap = new HashMap<>(); 
				taskId = rs.getInt(1);
				taskGridTipsMap.put("taskId",taskId);
				taskGridTipsMap.put("tipsAddLen",df.format(0.0).toString());
				taskGridTipsList.add(taskGridTipsMap);
			}
			double tipsAddLen = Double.valueOf((String) taskGridTipsMap.get("tipsAddLen"));
            //20170927 新增里程区分测线来源统计
            //测线来源src=0（GPS测线手持端）和2（自绘测线）
            String sourceType = rs.getString("S_SOURCETYPE");
            if(sourceType.equals("2001")) {
                String rowkey = rs.getString("ID");
                Get get = new Get(rowkey.getBytes());
                get.addColumn("data".getBytes(), "deep".getBytes());
                Result result = htab.get(get);
                if(result == null || result.isEmpty()) {
                    continue;
                }
                JSONObject deepJSON = JSONObject.fromObject(new String(result.getValue(
                        "data".getBytes(), "deep".getBytes())));
                int src = deepJSON.getInt("src");
                if(src != 0 && src != 2) {
                    continue;
                }
            }

			
			STRUCT geoStruct=(STRUCT) rs.getObject("wktlocation");
			Geometry geometry = null;
			try {
				geometry = GeoTranslator.struct2Jts(geoStruct);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
			
			if(geometry==null){
				continue;
			}
			tipsAddLen+=GeometryUtils.getLinkLength(geometry);//根据geometry求里程
			taskGridTipsMap.put("tipsAddLen",df.format(tipsAddLen).toString());
		}
	}


	/**
	 * 获取子任务统计tips
	 * @return
	 */
	public List<Map<String,Object>> getSubtaskTips(Connection conn) {
			try{
				QueryRunner run = new QueryRunner();
				
				String sql = "SELECT s_msubtaskid,COUNT(1) FROM tips_index WHERE s_msubtaskid <> 0 GROUP BY s_msubtaskid";
				
				return run.query(conn, sql,new ResultSetHandler<List<Map<String, Object>>>() {
					@Override
					public List<Map<String, Object>> handle(ResultSet rs)
							throws SQLException {
						List<Map<String, Object>> subtaskTipsList = new ArrayList<>();
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
