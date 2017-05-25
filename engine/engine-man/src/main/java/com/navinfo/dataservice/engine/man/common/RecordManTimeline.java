package com.navinfo.dataservice.engine.man.common;

import java.sql.Connection;
import java.sql.Timestamp;

import com.navinfo.navicommons.database.QueryRunner;

/**
 * 用于记录关闭数据时间点
 * @author songhe
 * @version 1.0
 * 
 * */
public class RecordManTimeline {
	
	public static void recordTimeline(int objectID, String name, Connection conn) throws Exception{
		
		String sql = "insert into MAN_TIMELINE t(t.obj_id,t.obj_type,t.operate_type,t.operate_desc)"
				+ "values("+objectID+",'"+name+"',0,'')";
		
		try{
			QueryRunner run = new QueryRunner();
			run.execute(conn, sql);
		}catch(Exception e){
			throw e;
		}
	}

}
