package com.navinfo.dataservice.control.row.crowds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.operation.imp.CorwdsSrcPoiDayImportor;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

import net.sf.json.JSONObject;

/** 
 * 行编众包业务处理类
* @ClassName: RowCrowdsControl 
* @author: zhangpengpeng 
* @date: 2017年5月22日
* @Desc: RowCrowdsControl.java
*/
public class RowCrowdsControl {
	
	private static final Logger logger = Logger.getLogger(RowCrowdsControl.class);
	
	/**
	 * 根据POI点位和名称在所属大区库中查询是否存在
	 * @param reqJson
	 * @return
	 */
	public int checkDuplicate(JSONObject reqJson) throws Exception {
		int result = 0;
		String name = reqJson.getString("name");
		// 转全角
		String fullName = ExcelReader.h2f(name);
		double x = reqJson.getDouble("x");
		double y = reqJson.getDouble("y");
		Connection dailyConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			String dailyDbId = getDailyDbId(x, y);
			if(StringUtils.isNotEmpty(dailyDbId)){
				dailyConn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dailyDbId));
				String sql = "select count(*) num from ix_poi_name n where n.name_class=1 and n.name_type=2 and n.lang_code = 'CHI' and n.name=:1";
				pstmt = dailyConn.prepareStatement(sql);
				pstmt.setString(1, fullName);
				
				rs = pstmt.executeQuery();
				if (rs.next()){
					int number = rs.getInt("num");
					if (number>0){
						result = 1;
					}
				}
			}
		}catch (Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.closeQuietly(dailyConn);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(rs);
		}
		return result;
	}
	
	/**
	 * 众包审核数据入大区库
	 * @param reqJson
	 * @return
	 * @throws Exception 
	 */
	public String release(JSONObject reqJson) throws Exception{
		String msg = null;
		Connection dayConn = null;
		try{
			JSONObject tPoi = reqJson.getJSONObject("data");
			if (tPoi == null || tPoi.isNullObject() || tPoi.isEmpty()){
				return "参数data数据错误！！";
			}
			// 验证缺少那些字段
			List<String> fields = Arrays.asList("FID", "RECLASSCODE", "PHOTO", "REAUDITNAME", "REAUDITADDRESS", "GEOX", "GEOY", "DESCP",
	                  "REAUDITPHONE", "BATCHTASK_ID", "EDITHISTORY", "GATHERUSERID", "STATE");
			String keyNotExists = null;
			for(String key: fields){
				if (!tPoi.containsKey(key)){
					keyNotExists = key;
					break;
				}
			}
			if (StringUtils.isNotEmpty(keyNotExists)){
				return "当前字段在提交数据中不存在! key:"+ keyNotExists;
			}
			String fid = tPoi.getString("FID");
			
			// 验证FID是否非法
			if (StringUtils.isEmpty(fid)){
				return "当前数据FID为空！tPoi:"+ tPoi.toString();
			}
			logger.info("crowds user:" + tPoi.getInt("GATHERUSERID"));
			
			int state = tPoi.getInt("STATE");
			double x = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOX"));
			double y = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOY"));
			String dbId = getDailyDbId(x, y);
			
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			if(StringUtils.isNotEmpty(dbId)){
				dayConn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				
				if (state != 3){
					//判断有无常规子任务(且状态为待作业或待提交)
					if (HasComSubtask(dayConn, fid, apiService)){
						return "常规子任务FID:" + fid + " 正在作业！";
					}
				}
				// 默认为无众包子任务，任务号赋0，状态为待作业
				int subTaskId = 0;
				int taskId = 0;
				int status = 1;
				int subtaskType = 0;
				// 根据grid获取众包子任务
				Subtask subTask = apiService.queryCrowdSubtaskByGrid(getGrid(x, y));
				// 有众包子任务，任务号赋众包子任务号，状态为待作业
				if(subTask != null){
					subTaskId = subTask.getSubtaskId();
					taskId = subTask.getTaskId();
					subtaskType = subTask.getSubType();
				}
				// 根据fid获取pid
				List<String> fids = Arrays.asList(fid);
				long pid = 0;
				Map<String,Long> fidPid = IxPoiSelector.getPidByFids(dayConn, fids);
				if (fidPid != null && fidPid.containsKey(fid)){
					pid = fidPid.get(fid);
				}
				tPoi.put("dbId", dbId);
				CorwdsSrcPoiDayImportor imp = new CorwdsSrcPoiDayImportor(dayConn, null);
				imp.setSubtaskId(subTaskId);
				imp.setName("Crowd2Day"); //众包入日库				
				// 判断在日库中存在否
				if(pid == 0){
					if(state != 3){
						return "非新增FID为"+ fid +"的POI数据在日库中不存在！";
					}else{
						// 生成新增数据
						pid = imp.importAddPoi(tPoi);
					}
				}else{
					if(state == 3){
						return "新增数据FID为"+ fid +"的POI数据在日库中存在！";
					}else{
						if (state == 2){
							// 生成修改数据
							imp.importUpdatePoi(tPoi);
						}else{
							// 生成删除数据
							imp.importDelPoi(tPoi);
						}
					}
				}
				imp.persistChangeLog(OperationSegment.SG_ROW, 0); //众包用户ID
				// 维护状态表poi_edit_status
				logger.info("维护状态表:pid" + pid);
				List<Long> pids = Arrays.asList(pid);
				PoiEditStatus.forCollector(dayConn, pids, null, subTaskId, taskId, subtaskType);
			}else{
				return "FID:" + fid + "数据未获取到大区库信息，不入库！";
			}
		}catch (Exception e){
			DbUtils.rollback(dayConn);
			logger.error(e.getMessage(), e);
			msg = e.getMessage();
		}finally{
			DbUtils.commitAndCloseQuietly(dayConn);
		}
		return msg;
		
	}
	
	/**
	 * 判断fid有无常规子任务（且状态为待作业、待提交）
	 * @param dayConn
	 * @param fid
	 * @param apiService
	 * @return
	 */
	private boolean HasComSubtask(Connection dayConn, String fid, ManApi apiService)throws Exception {
		boolean flag = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			
			String sql = "select s.quick_subtask_id, s.medium_subtask_id from poi_edit_status s,ix_poi p where p.pid=s.pid and s.status in (1,2) and p.poi_num=:1";
			pstmt = dayConn.prepareStatement(sql);
			pstmt.setString(1, fid);
			rs = pstmt.executeQuery();
			
			while (rs.next()){
				int qSubtaskId = rs.getInt("quick_subtask_id");
				int mSubtaskId = rs.getInt("medium_subtask_id");
				if (qSubtaskId !=0 ){
					Subtask qSubtask = apiService.queryBySubtaskId(qSubtaskId);
					if (qSubtask != null && qSubtask.getWorkKind() == 1){
						flag = true;
						break;
					}
				}
				if (mSubtaskId !=0 ){
					Subtask mSubtask = apiService.queryBySubtaskId(mSubtaskId);
					if (mSubtask != null && mSubtask.getWorkKind() == 1){
						flag = true;
						break;
					}
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return flag;
	}

	/**
	 * 根据显示坐标算日库dbId
	 * @param x
	 * @param y
	 * @return
	 * @throws Exception
	 */
	private static String getDailyDbId(double x,double y) throws Exception {
		String dbId = "";
		Connection manConn = null;
		try{

			String grid = getGrid(x, y);
			manConn = DBConnector.getInstance().getManConnection();
			String manQuery = "SELECT daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=:1";
			QueryRunner qRunner = new QueryRunner();
			dbId = qRunner.queryForString(manConn, manQuery, grid);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new Exception("数据未获取到大区库信息，不入库");
		}finally{
			DbUtils.commitAndClose(manConn);
		}
		return dbId;
	}
	
	/**
	 * 根据显示坐标算grid
	 * @param x
	 * @param y
	 * @return
	 */
	private static String getGrid(double x,double y){
		String grid = "";
		CompGridUtil gridUtil = new CompGridUtil();
		grid = gridUtil.point2Grids(x, y)[0];
		return grid;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(getGrid(116.36348, 39.97247));
	}

}
