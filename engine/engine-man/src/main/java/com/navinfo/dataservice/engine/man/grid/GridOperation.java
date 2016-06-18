package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class GridOperation {
	private static Logger log = LoggerRepos.getLogger(GridOperation.class);

	public GridOperation() {
		// TODO Auto-generated constructor stub
	}

	public static List<HashMap> queryGirdBySql(Connection conn,String selectSql) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap<String, Integer>();
						map.put("gridId", rs.getInt("grid_id"));
						map.put("status", rs.getInt("status"));
						try {
							map.put("type", GridOperation.getGridType(rs.getInt("grid_id"),rs.getInt("status")));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						list.add(map);
					}
					return list;
				}
	    		
	    	};
	    	if (null==grids || grids.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,grids.toArray()
					);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:"+e.getMessage(),e);
		}
	}

	// public static List<HashMap> queryProduceBlock(Connection conn,String
	// selectSql,List<Object> values) throws Exception{
	// try{
	// QueryRunner run = new QueryRunner();
	// ResultSetHandler<List<HashMap>> rsHandler = new
	// ResultSetHandler<List<HashMap>>(){
	// public List<HashMap> handle(ResultSet rs) throws SQLException {
	// List<HashMap> list = new ArrayList<HashMap>();
	// while(rs.next()){
	// HashMap map = new HashMap<String, Integer>();
	// //block下grid日完成度为100%，block才可出品
	// try {
	// if (GridOperation.checkGridFinished(rs.getInt("BLOCK_ID"))){
	// map.put("blockId", rs.getInt("BLOCK_ID"));
	// map.put("blockName", rs.getInt("BLOCK_NAME"));
	// CLOB clob = (CLOB)rs.getObject("geometry");
	// String clobStr = DataBaseUtils.clob2String(clob);
	// try {
	// map.put("geometry",Geojson.wkt2Geojson(clobStr));
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// list.add(map);
	// }
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// return list;
	// }
	//
	// } ;
	// if (null==values || values.size()==0){
	// return run.query(conn, selectSql, rsHandler
	// );
	// }
	// return run.query(conn, selectSql, rsHandler,values.toArray()
	// );
	// }catch(Exception e){
	// DbUtils.rollbackAndCloseQuietly(conn);
	// log.error(e.getMessage(), e);
	// throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:"+e.getMessage(),e);
	// }
	// }

	public static <E> String getGridType(int gridId, int stage) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select distinct s.type from subtask_grid_mapping t,subtask s where t.subtask_id=s.subtask_id "
					+ "and s.stage=" + stage + "and t.grid_id=" + gridId;

			PreparedStatement stmt = conn.prepareStatement(selectSql);
			ResultSet rs = stmt.executeQuery();
			List<Integer> listType = new ArrayList();
			int TypeTotal = 0;
			String flagType = null;
			while (rs.next()) {
				listType.add(rs.getInt(1));
			}
			for (int i = 0; i < listType.size(); i++) {
				TypeTotal += listType.get(i);
			}
			if (2 == TypeTotal) {
				flagType = "P+R";
			}
			if (1 == TypeTotal) {
				if (listType.contains(0)) {
					flagType = "P/R";
				} else {
					flagType = "R";
				}
			}
			if (0 == TypeTotal) {
				if (listType.contains(0)) {
					flagType = "P";
				}
			}

			return flagType;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
