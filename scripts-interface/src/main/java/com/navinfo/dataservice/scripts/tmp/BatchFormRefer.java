package com.navinfo.dataservice.scripts.tmp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.NEW;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.runner.Result;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class BatchFormRefer {
	private static Logger log = LogManager.getLogger(BatchFormRefer.class);
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		log.info("start");
		JobScriptsInterface.initContext();
		final Connection connection=DBConnector.getInstance().getManConnection();
		//获取subtask_refer
		QueryRunner runner=new QueryRunner();
		String sql="create table subtask_refer_tmp as select * from subtask_refer";
		runner.update(connection, sql);
		sql="select id,geometry from subtask_refer";
		
		Map<Integer, STRUCT> map=runner.query(connection, sql, new ResultSetHandler<Map<Integer, STRUCT>>(){

			@Override
			public Map<Integer, STRUCT> handle(ResultSet rs) throws SQLException {
				Map<Integer, STRUCT> map=new HashMap<>();
				while(rs.next()){
					STRUCT struct = (STRUCT) rs.getObject("geometry");
					try {
						Geometry geometry=GeoTranslator.struct2Jts(struct,1,6);
						String wkt=GeoTranslator.jts2Wkt(geometry);
						map.put(rs.getInt("id"), GeoTranslator.wkt2Struct(connection, wkt));
					} catch (Exception e) {
						log.error("geometry转JSON失败，原因为:" + e.getMessage());
					}					
				}
				return map;
			}
			
		});
		sql="update subtask_refer set Geometry=? where id=?";
		Object[][] params=new Object[map.size()][2];
		Set<Integer> keys = map.keySet();
		List<Integer> keyList=new ArrayList<>();
		keyList.addAll(keys);
		for(int i=0;i<map.size();i++){
			params[i][1]=keyList.get(i);
			params[i][0]=map.get(keyList.get(i));
		}
		runner.batch(connection, sql, params);
		connection.commit();
		log.info("end");
		System.exit(0);
	}
}
