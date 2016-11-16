package com.navinfo.dataservice.control.column.core;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.check.CheckEngine;
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
	
}
