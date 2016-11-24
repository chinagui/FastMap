package com.navinfo.dataservice.control.column.core;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiDeepStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.BatchProcess;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

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
	 * 深度信息检查执行方法
	 * 
	 * @param pids
	 * @param checkResultList
	 * @param objType
	 * @param operType
	 * @param conn
	 * @throws Exception
	 */
	public void deepCheckRun(List<Integer> pids,JSONArray checkResultList,String objType,String operType,Connection conn) throws Exception {
		try {
			logger.debug("开始执行检查项"+checkResultList);
			logger.debug("检查数据:"+pids);
			IxPoiSelector selector = new IxPoiSelector(conn);
			List<IRow> datas = selector.loadByIds(pids, false, true);
			CheckCommand checkCommand = new CheckCommand();			
			checkCommand.setObjType(Enum.valueOf(ObjType.class,objType));
			checkCommand.setOperType(Enum.valueOf(OperType.class,operType));
			checkCommand.setGlmList(datas);
			CheckEngine cEngine=new CheckEngine(checkCommand,conn);
			cEngine.checkByRules(checkResultList, "POST");	
			logger.debug("检查完毕");
		} catch (Exception e) {
			throw e;
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
     * @param parameter
     * @param userId
     * @return
     * @throws Exception
     * @Gaopr POI深度信息作业保存
     */
	public JSONObject save(String parameter, long userId) throws Exception {

        Connection conn = null;
        JSONObject result = null;
        
        List<String> rowIdList = new ArrayList<String>();
        List<Integer> pids = new ArrayList<Integer>();
        
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            ObjType objType = Enum.valueOf(ObjType.class,"IXPOI");
            int dbId = json.getInt("dbId");
            int objId = json.getInt("objId");

            conn = DBConnector.getInstance().getConnectionById(dbId);

            JSONObject poiData = json.getJSONObject("data");
            
            pids.add(objId);
            rowIdList = getRowIdsByPids(conn,pids);
            
            if (poiData.size() == 0) {
            	updateDeepStatus(rowIdList, conn, 0);
                return result;
            }
            
            json.put("command", "UPDATE");

            EditApiImpl editApiImpl = new EditApiImpl(conn);
            editApiImpl.setToken(userId);
            result = editApiImpl.runPoi(json);
            
            StringBuffer sb = new StringBuffer();
            sb.append(String.valueOf(objId));

            //更新数据状态
            updateDeepStatus(rowIdList, conn, 0);
            //调用清理检查结果方法
            cleanCheckResult(pids,conn);
            
    		//获取后检查需要执行规则列表
    		List<String> fields = Arrays.asList("categorys", "subcategory");
    		List<String> values = Arrays.asList("poi深度信息", "IX_POI_PARKING");
			IxPoiDeepStatusSelector ixPoiDeepStatusSelector = new IxPoiDeepStatusSelector(conn);
			JSONArray checkResultList = ixPoiDeepStatusSelector.getCheckRulesByCondition(fields,values);
    		//执行检查
            deepCheckRun(pids,checkResultList,objType.toString(),"UPDATE",conn);
            
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
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<String> rowIdList = new ArrayList<String>();
        Connection conn = null;
        JSONObject result = new JSONObject();
        int sucReleaseTotal = 0;
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            int dbId = json.getInt("dbId");
            int subtaskId = json.getInt("subtaskId");
            int type = json.getInt("type");

            Subtask subtask = apiService.queryBySubtaskId(subtaskId);
            conn = DBConnector.getInstance().getConnectionById(dbId);  
			
			// 查询可提交数据
			IxPoiDeepStatusSelector ixPoiDeepStatusSelector = new IxPoiDeepStatusSelector(conn);
			rowIdList = ixPoiDeepStatusSelector.getRowIdsForRelease(subtask,2,userId, type);
			sucReleaseTotal = rowIdList.size();
			
			// 修改poi_deep_status表作业项状态
			updateDeepStatus(rowIdList, conn, 1);
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
	public void updateDeepStatus(List<String> rowIdList,Connection conn,int flag) throws Exception {
		StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        if (rowIdList.isEmpty()){
        	return;
        }
        if (flag==0) {
        	sb.append(" UPDATE poi_deep_status T1 SET T1.status = 2 , T1.update_date = to_date('"+df.format(new Date())+"','yyyy-mm-dd hh24:mi:ss') WHERE row_id in (");
        } else {
        	sb.append(" UPDATE poi_deep_status T1 SET T1.status = 3 , T1.update_date = to_date('"+df.format(new Date())+"','yyyy-mm-dd hh24:mi:ss') WHERE row_id in (");
        }
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			String temp="";
			for (String rowId:rowIdList) {
				sb.append(temp);
				sb.append("'"+rowId+"'");
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
	 * @param subtask
	 * @param dbId
	 * @param userId
	 * @param type
	 * @return 申请的数据量
	 * @throws Exception
	 */
	public int applyData(Subtask subtask, int dbId, long userId,int type) throws Exception {
		int applyCount = 0;
		int hasApply = 0;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiDeepStatusSelector poiDeepStatusSelector = new IxPoiDeepStatusSelector(conn);
			//查询当前作业员已占有数据量
			hasApply = poiDeepStatusSelector.queryHandlerCount(userId, type);
			
			// 可申请数据的条数
			int canApply = 100 - hasApply;
			if (canApply == 0) {
				throw new Exception("该作业员名下已存在100条数据，不可继续申请");
			}
			
			//获取从状态表查询到能够申请数据的rowIds
			List<String> rowIds = poiDeepStatusSelector.getRowIds(subtask, type);
			if (rowIds.size() == 0){
				//未查询到可以申请的数据
				return 0;
			}
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			//实际申请到的数据rowIds
			List<String> applyDataRowIds = new ArrayList<String>();
			if (rowIds.size() >= canApply){
				applyDataRowIds = rowIds.subList(0, canApply-1);
				//数据加锁， 赋值handler，维护update_date
				dataSetLock(conn, applyDataRowIds, userId, df);
				applyCount += applyDataRowIds.size();
			}else{
				//库里面查询出的数据量小于当前用户可申请的量，即锁定库中查询出的数据
				applyDataRowIds = rowIds;
				dataSetLock(conn, applyDataRowIds, userId, df);
				applyCount += applyDataRowIds.size();
			}
			
			// 深度信息批处理 -- 作业前批
			List<Integer> pids = getPidsByRowIds(conn, applyDataRowIds);
			List<String> batchRuleList = getDeepBatchRules(type);
			exeBatch(conn, pids, batchRuleList, dbId);
			
			return applyCount;
		} catch (Exception e) {
			throw e;
		}

	}
	
	
	/**
	 * 获取深度信息批处理的规则
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public List<String> getDeepBatchRules(int type) throws Exception {
		List<String> rules = new ArrayList<String>();
		if (type == 1){
			// 通用
			rules.add("FM_BAT_20_195");
			rules.add("FM_BAT_20_196");
		}else if (type == 2){
			// 停车场
			rules.add("FM_BAT_20_198");
		}else if (type == 3){
			// 汽车租赁
			rules.add("FM_BAT_20_197");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT process_path FROM batch_rule WHERE rule_code in (");
		String temp = "";
		for (String rule:rules) {
			sb.append(temp);
			sb.append("'"+rule+"'");
			temp = ",";
		}
		sb.append(")");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		Connection conn = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			List<String> batchList = new ArrayList<String>();
			
			while (resultSet.next()) {
				batchList.add(resultSet.getString("process_path"));
			}
			return batchList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
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
	 * 深度信息申请数据-数据加锁
	 * @param conn
	 * @param rowIds
	 * @param userId
	 * @param df
	 * @throws Exception
	 */
	public void dataSetLock(Connection conn, List<String> rowIds, long userId,SimpleDateFormat df) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_deep_status SET handler=:1,update_date= to_date('"+ df.format(new Date()) + "','yyyy-mm-dd hh24:mi:ss') WHERE row_id in (");
		String temp = "";
		for (String rowId:rowIds) {
			sb.append(temp);
			sb.append("'"+rowId+"'");
			temp = ",";
		}
		sb.append(")");
		
		PreparedStatement pstmt = null;

		try {
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setLong(1, userId);
			
			pstmt.execute();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	/**
	 * 深度信息申请数据后-作业前批处理
	 * @param conn
	 * @param rowIds
	 * @param type
	 * @throws Exception 
	 */
	public void exeBatch(Connection conn, List<Integer> pids, List<String> batchRules, int dbId) throws Exception{

		try {
			
			//执行批处理
			for (int pid: pids) {
				
				JSONObject poiObj = new JSONObject();
				poiObj.put("objId", pid);
				poiObj.put("dbId", dbId);
				
				//调用批处理
				BatchProcess batchProcess = new BatchProcess();
				batchProcess.execute(poiObj, conn, new EditApiImpl(conn), batchRules);
				
			}
		} catch (Exception e) {
			throw e;
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

		try {
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			Connection conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiDeepStatusSelector deepSelector = new IxPoiDeepStatusSelector(conn);
			result = deepSelector.loadDeepPoiByCondition(jsonReq, subtask, userId);
			
			return result;
		} catch(Exception e) {
			throw e;
		} 
	}
	
	public static void main(String[] args) throws Exception{
		long timeCur = new Date().getTime();
		Timestamp time = new Timestamp(timeCur);
		System.out.println(time.toString());
	}
	
}
