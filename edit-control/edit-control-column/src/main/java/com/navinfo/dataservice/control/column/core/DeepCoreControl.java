package com.navinfo.dataservice.control.column.core;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiDeepStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DeepCoreControl {
	private static final Logger logger = Logger.getLogger(DeepCoreControl.class);

	/**
	 * 深度信息库存统计
	 * 
	 * @param subtask
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getLogCount(Subtask subtask,int dbId) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(1) AS num,p.type");
		sb.append(" FROM ix_poi i,poi_deep_status p");
		sb.append(" WHERE sdo_within_distance(i.geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE'");
		sb.append(" AND i.u_record!=2");
		sb.append(" AND i.row_id=p.row_id ");
		sb.append(" AND p.status=1");
		sb.append(" AND p.handler is null");
		sb.append(" GROUP BY p.type");
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		logger.debug("sql:"+sb);
		
		logger.debug("wkt:"+subtask.getGeometry());
		
		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setString(1, subtask.getGeometry());
			
			resultSet = pstmt.executeQuery();
			
			JSONObject resutlObj = new JSONObject();
			
			while (resultSet.next()) {
				if (resultSet.getInt("type")==1) {
					resutlObj.put("detail", resultSet.getInt("num"));
				} else if (resultSet.getInt("type")==2) {
					resutlObj.put("parking", resultSet.getInt("num"));
				} else if (resultSet.getInt("type")==3) {
					resutlObj.put("carrental", resultSet.getInt("num"));
				}
			}
			
			logger.debug("result:"+resutlObj);
			
			return resutlObj;
		}catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(conn);
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 清理检查结果
	 * 
	 * @param pids
	 * @param conn
	 * @throws Exception
	 */
	public void cleanCheckResult(List<Integer> pids,Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		Clob pidClod = null;
		try {
			logger.debug("开始清理检查结果");
			String pois = StringUtils.join(pids, ",");
			pidClod = ConnectionUtil.createClob(conn);
			pidClod.setString(1, pois);
			String sql = "SELECT md5_code FROM ck_result_object WHERE table_name='IX_POI' AND pid in (select column_value from table(clob_to_table(?)))";
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidClod);
			resultSet = pstmt.executeQuery();
			List<String> md5List = new ArrayList<String>();
			while (resultSet.next()) {
				md5List.add(resultSet.getString("md5_code"));
			}
			cleanCheckException(md5List,conn);
			cleanCheckObj(md5List,conn);
			logger.debug("清理完成");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 删除ni_val_exception表 
	 * 
	 * @param md5List
	 * @param conn
	 * @throws Exception
	 */
	private void cleanCheckException (List<String> md5List,Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		
		Clob md5Clod = null;
		
		String sql = "DELETE FROM ni_val_exception WHERE md5_code in (select column_value from table(clob_to_table(?)))";
		try {
			logger.debug("清理ni_val_exception");
			logger.debug(md5List);
			logger.debug("sql:"+sql);
			String md5s = "";
			String tmep = "";
			for (int i=0;i<md5List.size();i++) {
				String md5Code = md5List.get(i);
				md5s += tmep;
				tmep = ",";
				md5s += md5Code;
			}
			md5Clod = ConnectionUtil.createClob(conn);
			md5Clod.setString(1, md5s);
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, md5Clod);
			pstmt.execute();
			logger.debug("ni_val_exception表清理完成");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 删除ck_result_object表
	 * 
	 * @param md5List
	 * @param conn
	 * @throws Exception
	 */
	private void cleanCheckObj (List<String> md5List,Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		
		Clob md5Clod = null;
		
		String sql = "DELETE FROM ck_result_object WHERE md5_code in (select column_value from table(clob_to_table(?)))";
		try {
			logger.debug("清理ck_result_object");
			logger.debug(md5List);
			logger.debug("sql:"+sql);
			String md5s = "";
			String tmep = "";
			for (int i=0;i<md5List.size();i++) {
				String md5Code = md5List.get(i);
				md5s += tmep;
				tmep = ",";
				md5s += md5Code;
			}
			md5Clod = ConnectionUtil.createClob(conn);
			md5Clod.setString(1, md5s);
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, md5Clod);
			pstmt.execute();
			logger.debug("ck_result_object表清理完成");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
	
	/**
	 * 清ni_val_exception_grid表
	 * @param md5List
	 * @param conn
	 * @throws Exception
	 */
	public void cleanExceptionGrid(List<String> md5List, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		
		Clob md5Clod = null;
		
		String sql = "DELETE FROM ni_val_exception_grid WHERE md5_code in (select column_value from table(clob_to_table(?)))";
		try {
			logger.debug("清理ni_val_exception_grid");
			logger.debug(md5List);
			logger.debug("sql:"+sql);
			String md5s = "";
			String temp = "";
			for (int i=0;i<md5List.size();i++) {
				String md5Code = md5List.get(i);
				md5s += temp;
				temp = ",";
				md5s += md5Code;
			}
			md5Clod = ConnectionUtil.createClob(conn);
			md5Clod.setString(1, md5s);
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, md5Clod);
			pstmt.execute();
			logger.debug("ni_val_exception_grid表清理完成");
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
	
    /**
     * @param parameter
     * @param userId
     * @return
     * @throws Exception
     * @Gaopr POI深度信息作业保存
     */
	public JSONObject save(String parameter, long userId) throws Exception {

        Connection conn = null;
        JSONObject result = null;
        
        List<Integer> pids = new ArrayList<Integer>();
        
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            int dbId = json.getInt("dbId");
            int objId = json.getInt("objId");
            String secondWorkItem = json.getString("secondWorkItem");

            conn = DBConnector.getInstance().getConnectionById(dbId);

            JSONObject poiData = json.getJSONObject("data");
            
            pids.add(objId);
            //rowIdList = getRowIdsByPids(conn,pids);
            
            if (poiData.size() == 0) {
            	updateDeepStatus(pids, conn, 0,secondWorkItem);
                return result;
            }
            
            json.put("command", "UPDATE");
            
            DefaultObjImportor importor = new DefaultObjImportor(conn,null);
			EditJson editJson = new EditJson();
			editJson.addJsonPoi(json);
			DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
			importor.operate(command);
			importor.persistChangeLog(OperationSegment.SG_COLUMN, userId);

//            EditApiImpl editApiImpl = new EditApiImpl(conn);
//            editApiImpl.setToken(userId);
//            result = editApiImpl.runPoi(json);
            
			OperationResult operationResult=importor.getResult();

            //更新数据状态
            updateDeepStatus(pids, conn, 0,secondWorkItem);
            //调用清理检查结果方法
            cleanCheckResult(pids,conn);
            
    		//获取后检查需要执行规则列表
            List<String> checkList=getCheckRuleList(conn,secondWorkItem);
            
    		//执行检查
			CheckCommand checkCommand=new CheckCommand();		
			checkCommand.setRuleIdList(checkList);
			Check check=new Check(conn,operationResult);
			check.operate(checkCommand);
            
            return result;
        } catch (DataNotChangeException e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndClose(conn);
        }
    }
    
    /**
     * @param parameter
     * @param userId
     * @return
     * @throws Exception
     * @Gaopr POI深度信息作业提交
     */
	public JSONObject release(String parameter, long userId) throws Exception {
		
		List<Integer> pidList = new ArrayList<Integer>();
        Connection conn = null;
        JSONObject result = new JSONObject();
        int sucReleaseTotal = 0;
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            int dbId = json.getInt("dbId");
            int subtaskId = json.getInt("subtaskId");
            String secondWorkItem = json.getString("secondWorkItem");

            //Subtask subtask = apiService.queryBySubtaskId(subtaskId);
            conn = DBConnector.getInstance().getConnectionById(dbId);  
			
			// 查询可提交数据
            IxPoiColumnStatusSelector ixPoiColumnStatusSelector = new IxPoiColumnStatusSelector(conn);
			pidList = ixPoiColumnStatusSelector.getpidsForRelease(subtaskId,2,userId, secondWorkItem);
			sucReleaseTotal = pidList.size();
			
			// 修改poi_deep_status表作业项状态
			updateDeepStatus(pidList, conn, 1,secondWorkItem);
			result.put("sucReleaseTotal", sucReleaseTotal);
            return result;
        } catch (DataNotChangeException e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndClose(conn);
        }
    }
    
    /**
     * 深度信息更新配置表状态 
     * @param pids conn flag(0:保存 1:提交)
     * @throws Exception
     */
	public void updateDeepStatus(List<Integer> pidList,Connection conn,int flag,String secondWorkItem) throws Exception {
		StringBuilder sb = new StringBuilder();
		
        if (pidList.isEmpty()){
        	return;
        }
        if (flag==0) {
        	sb.append(" UPDATE poi_column_status T1 SET T1.SECOND_WORK_STATUS= 2  WHERE  T1.work_item_id IN (SELECT cf.work_item_id FROM POI_COLUMN_WORKITEM_CONF cf WHERE cf.second_work_item='"+secondWorkItem+"') AND T1.pid in (");
        } else {
        	sb.append(" UPDATE poi_column_status T1 SET T1.SECOND_WORK_STATUS= 3,T1.HANDLER=0  WHERE  T1.work_item_id IN (SELECT cf.work_item_id FROM POI_COLUMN_WORKITEM_CONF cf WHERE cf.second_work_item='"+secondWorkItem+"') AND T1.pid in (");
        }
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			String temp="";
			for (int pid:pidList) {
				sb.append(temp);
				sb.append("'"+pid+"'");
				temp = ",";
			}
			sb.append(")");
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
    
    
	/**
	 * 深度信息申请数据
	 * @param taskId
	 * @param userId
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public int applyData(int taskId, long userId, String firstWorkItem, String secondWorkItem) throws Exception {
		int applyCount = 0;
		int hasApply = 0;
		
		Connection conn = null;
		
		// 默认为大陆数据
		int type = 1;
		try {
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			//查询当前作业员已占有数据量
			IxPoiColumnStatusSelector poiColumnSelector = new IxPoiColumnStatusSelector(conn);
			hasApply = poiColumnSelector.queryHandlerCount(firstWorkItem, secondWorkItem, userId, type);
			
			// 可申请数据的条数
			int canApply = 100 - hasApply;
			if (canApply == 0) {
				throw new Exception("该作业员名下已存在100条数据，不可继续申请");
			}
			
			//获取从状态表查询到能够申请数据的pids
			List<Integer> pids = poiColumnSelector.getApplyPids(subtask, firstWorkItem, secondWorkItem, type);
			if (pids.size() == 0){
				//未查询到可以申请的数据
				return 0;
			}
			
			//实际申请到的数据pids
			List<Integer> applyDataPids = new ArrayList<Integer>();
			if (pids.size() >= canApply){
				applyDataPids = pids.subList(0, canApply);
			}else{
				//库里面查询出的数据量小于当前用户可申请的量，即锁定库中查询出的数据
				applyDataPids = pids;
			}
			
			Timestamp timeStamp = new Timestamp(new Date().getTime());
			
			//数据加锁， 赋值handler，维护update_date,task_id
			applyCount += applyDataPids.size();
			List<String> workItemIds = poiColumnSelector.getWorkItemIds(firstWorkItem, secondWorkItem);
			poiColumnSelector.dataSetLock(applyDataPids, workItemIds, userId, taskId, timeStamp);
			
			OperationResult operationResult=new OperationResult();
			List<BasicObj> objList = new ArrayList<BasicObj>();
			for (int pid:applyDataPids) {
				BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,true, pid, false);
				objList.add(obj);
			}
			operationResult.putAll(objList);
			
			// 深度信息批处理 -- 作业前批
			List<String> batchRuleList = getDeepBatchRules(secondWorkItem);
			BatchCommand batchCommand=new BatchCommand();
			for (String rule:batchRuleList) {
				batchCommand.setRuleId(rule);
			}
			Batch batch=new Batch(conn,operationResult);
			batch.operate(batchCommand);
			batch.persistChangeLog(OperationSegment.SG_COLUMN, userId);
			
			return applyCount;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.commitAndClose(conn);
		}

	}
	
	
	/**
	 * 获取深度信息批处理的规则
	 * @param secondWorkItem
	 * @return
	 * @throws Exception 
	 */
	public List<String> getDeepBatchRules(String secondWorkItem) throws Exception {
		List<String> rules = new ArrayList<String>();
		if ("deepDetail".equals(secondWorkItem)){
			// 通用
			rules.add("FM-BAT-20-195");
			rules.add("FM-BAT-20-196");
		}else if ("deepParking".equals(secondWorkItem)){
			// 停车场
			rules.add("FM-BAT-20-198");
		}else if ("deepCarrental".equals(secondWorkItem)){
			// 汽车租赁
			rules.add("FM-BAT-20-197");
		}
			return rules;
	}
	
	/**
	 * 根据rowIds 获取 pids
	 * @param conn
	 * @param rowIds
	 * @return
	 * @throws Exception 
	 */
	public List<Integer> getPidsByRowIds(Connection conn, List<String> rowIds) throws Exception{
		List<Integer> pids = new ArrayList<Integer>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pid FROM ix_poi WHERE row_id in (");
		String temp = "";
		for (String rowId:rowIds) {
			sb.append(temp);
			sb.append("'"+rowId+"'");
			temp = ",";
		}
		sb.append(")");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				pids.add(resultSet.getInt("pid"));
			}
			
			return pids;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	/**
	 * 根据pids 获取 RowIds
	 * @param conn
	 * @param rowIds
	 * @return
	 * @throws Exception 
	 */
	public List<String> getRowIdsByPids(Connection conn, List<Integer> pids) throws Exception{
		List<String> rowIds = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT row_id FROM ix_poi WHERE pid in (");
		String temp = "";
		for (int pid:pids) {
			sb.append(temp);
			sb.append(pid);
			temp = ",";
		}
		sb.append(")");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				rowIds.add(resultSet.getString("row_id"));
			}
			
			return rowIds;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 深度信息查询poi
	 * @param json
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject queryPoi(JSONObject jsonReq, long userId) throws Exception{

		JSONObject result = new JSONObject();
		int subtaskId = jsonReq.getInt("subtaskId");
		int dbId = jsonReq.getInt("dbId");

		Connection conn = null;
		try {
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiDeepStatusSelector deepSelector = new IxPoiDeepStatusSelector(conn);
			result = deepSelector.loadDeepPoiByCondition(jsonReq, subtaskId, userId);
			
			return result;
		} catch(Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	
	/**
	 * 清检查结果，用于POI行编和月编
	 * 月编 ：
	 * jsonReq JSONObject
	 * pids：[123,123],ckRules:["rule1","rule2"],checkType:1 //0行编 1精编 
	 * @param jsonReq JSONObject
	 * @param jsonReq
	 * @param userId
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void cleanCheck(JSONObject jsonReq, long userId) throws Exception {
		int taskId = jsonReq.getInt("subtaskId");
		
		Connection conn = null;
		
		try {
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			int checkType = jsonReq.getInt("checkType");
			
			List<Integer> pids = new ArrayList<Integer>();
			if (jsonReq.containsKey("pids")) {
				pids = jsonReq.getJSONArray("pids");
			}
			List<String> ckRules = new JSONArray();
			if (jsonReq.containsKey("ckRules")) {
				ckRules = jsonReq.getJSONArray("ckRules");
			}
			
			// POI行编
			if (checkType == 0){

				if (ckRules.size() == 0) {
					throw new Exception("检查规则checkType=0为行编时，ckRules不能为空");
				}
				if (pids.size() == 0) {
					IxPoiSelector poiSelector = new IxPoiSelector(conn);
					pids = poiSelector.getPidsBySubTask(subtask);
				}
			} 
			
			// POI精编
			if (checkType == 1) {
				
				IxPoiColumnStatusSelector columnSelector = new IxPoiColumnStatusSelector(conn);
				
				if (ckRules.size() == 0) {
					// 如果没有ckRules,则根据firstWorkItem和secondWorkItem从精编配置表POI_COLUMN_WORKITEM_CONF获取
					String firstWorkItem = new String();
					if (jsonReq.containsKey("firstWorkItem")){
						firstWorkItem = jsonReq.getString("firstWorkItem");
					}
					String secondWorkItem = new String();
					if (jsonReq.containsKey("secondWorkItem")) {
						secondWorkItem = jsonReq.getString("secondWorkItem");
					}
					if (StringUtils.isEmpty(firstWorkItem) || StringUtils.isEmpty(secondWorkItem)) {
						throw new Exception("检查规则checkType=1为精编，ckRules为空时，firstWorkItem和secondWorkItem不能为空");
					} else {
						// 根据一级项和二级项获取ckRules
						ckRules = columnSelector.getWorkItemIds(firstWorkItem, secondWorkItem);
					}
				}
				if (pids.size() == 0) {
					pids = columnSelector.getPids(taskId, userId);
				}
			}
			
			String objType = new String();
			if (checkType == 0 || checkType == 1) {
				objType = "IX_POI";
			}
			// 根据pids和ckRules获取md5List
			List<String> md5List = new ArrayList<String>();
			md5List = getMd5List(conn, pids, ckRules, objType);
			
			// 清ni_val_exception表
			cleanCheckException(md5List,conn);
			// 清ck_result_object表
			cleanCheckObj(md5List,conn);
			// 清ni_val_exception_grid表
			cleanExceptionGrid(md5List,conn);
			 
		} catch (DataNotChangeException e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndClose(conn);
        }
	}
	
	
	/**
	 * 根据pids和ckRules获取md5List
	 * @param conn
	 * @param pids
	 * @param ckRules
	 * @param objType
	 * @return
	 * @throws Exception
	 */
	public List<String> getMd5List(Connection conn, List<Integer> pids, List<String> ckRules, String objType) throws Exception {
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		try {
			Clob pidClob = null;
			String pois = StringUtils.join(pids, ",");
			pidClob = ConnectionUtil.createClob(conn);
			pidClob.setString(1, pois);
			
			Clob ckRuleClob = null;
			String rules = "";
			String temp = "";
			for (int i=0;i<ckRules.size();i++) {
				String rule = ckRules.get(i);
				rules += temp;
				temp = ",";
				rules += rule;
			}
			ckRuleClob = ConnectionUtil.createClob(conn);
			ckRuleClob.setString(1, rules);
			
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT o.md5_code FROM ck_result_object o,ni_val_exception e ");
			sb.append(" WHERE o.md5_code=e.md5_code ");
			sb.append(" AND o.table_name=? ");
			sb.append(" AND o.pid in (select column_value from table(clob_to_table(?)))");
			sb.append(" AND e.ruleid in (select column_value from table(clob_to_table(?)))");
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setString(1, objType);
			pstmt.setClob(2, pidClob);
			pstmt.setClob(3, ckRuleClob);
			
			resultSet = pstmt.executeQuery();
			
			List<String> md5List = new ArrayList<String>();
			
			while (resultSet.next()) {
				md5List.add(resultSet.getString("md5_code"));
			}
			return md5List;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 根据规则号清理检查结果
	 * @param conn
	 * @param pids
	 * @param ckRules
	 * @param objType
	 * @throws Exception
	 */
	public void cleanExByCkRule(Connection conn, List<Integer> pids, List<String> ckRules, String objType) throws Exception {
		List<String> md5List = getMd5List(conn,pids,ckRules,objType);
		cleanCheckException(md5List,conn);
		cleanCheckObj(md5List,conn);
	}
	
	/**
	 * 查询深度信息检查项
	 * @param conn
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	private List<String> getCheckRuleList(Connection conn,String secondWorkItem) throws Exception{
		try{
			List<String> rules = new ArrayList<String>();
			
			String sql="SELECT DISTINCT WORK_ITEM_ID"
					+ "  FROM POI_COLUMN_WORKITEM_CONF C"
					+ " WHERE C.FIRST_WORK_ITEM = 'poi_deep'"
					+ "   AND CHECK_FLAG IN (2, 3)"
					+ "   AND C.SECOND_WORK_ITEM='" + secondWorkItem + "'";

			QueryRunner run=new QueryRunner();
			rules=run.query(conn, sql, new ResultSetHandler<List<String>>(){

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> rules=new ArrayList<String>();
					while(rs.next()){
						rules.add(rs.getString("WORK_ITEM_ID"));
					}
					return rules;
				}});
			return rules;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}
	
}
