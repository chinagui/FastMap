package com.navinfo.dataservice.control.column.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
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
	
	
}
