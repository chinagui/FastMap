package com.navinfo.dataservice.engine.man.timeline;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 用于记录关闭数据时间点
 * @author songhe
 * @version 1.0
 * 
 * */
public class TimelineService {
	private static Logger log = LoggerRepos.getLogger(TimelineService.class);
	/**
	 * @param 操作对象的ID
	 * @param 操作对象的name
	 * @param 操作类型，0关闭
	 * @param Connection
	 * 
	 * */
	public static void recordTimeline(int objectID, String name, int type, Connection conn) throws Exception{
		
		String sql = "insert into MAN_TIMELINE t(t.obj_id,t.obj_type,t.operate_type,t.operate_desc)"
				+ "values("+objectID+",'"+name+"',"+type+",'')";
		try{
			QueryRunner run = new QueryRunner();
			run.execute(conn, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 查询MAN_TIMELINE
	 * objName:program,task,subtask,infor
	 * @return Map<Long,Map<String, Object>> key:objId
	 * @throws ServiceException 
	 */
	public static Map<Integer,Map<String, Object>> queryManTimelineByObjName(String objName) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			//查询数据
			String sql = "SELECT * FROM MAN_TIMELINE WHERE OBJ_TYPE = ? ORDER BY OBJ_ID,OPERATE_DATE";
			Object[] params = {objName};
			//处理结果集
			ResultSetHandler<Map<Integer,Map<String, Object>>> rsh = new ResultSetHandler<Map<Integer,Map<String, Object>>>() {
				
				@Override
				public Map<Integer,Map<String, Object>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Map<String, Object>> data = new HashMap<Integer,Map<String, Object>>();
					//处理数据
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						int objId = rs.getInt("OBJ_ID");
						map.put("objId", objId);
						map.put("objType", rs.getString("OBJ_TYPE"));
						map.put("operateDate", DateUtils.dateToString(rs.getTimestamp("OPERATE_DATE"),DateUtils.DATE_COMPACTED_FORMAT));
						data.put(objId, map);
					}
					return data;
				}
			};
			Map<Integer,Map<String, Object>> result = queryRunner.query(conn, sql, rsh, params);
			//返回数据
			return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询MAN_TIMELINE明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
