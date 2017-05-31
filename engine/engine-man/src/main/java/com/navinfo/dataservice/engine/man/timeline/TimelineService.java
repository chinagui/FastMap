package com.navinfo.dataservice.engine.man.timeline;

import java.sql.Connection;
import java.sql.Timestamp;

import com.navinfo.navicommons.database.QueryRunner;

/**
 * 用于记录关闭数据时间点
 * @author songhe
 * @version 1.0
 * 
 * */
public class TimelineService {
	
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

}
