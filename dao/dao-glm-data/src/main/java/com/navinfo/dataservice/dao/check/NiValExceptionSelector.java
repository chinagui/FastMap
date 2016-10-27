package com.navinfo.dataservice.dao.check;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

public class NiValExceptionSelector {

	private Connection conn;

	public NiValExceptionSelector() {
	}

	public NiValExceptionSelector(Connection conn) {
		this.conn = conn;
	}

	public NiValException loadById(String id, boolean isLock) throws Exception {

		NiValException exception = new NiValException();

		String sql = "select * from ni_val_exception where md5_code=:1";
		
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				exception.setValExceptionId(resultSet.getInt("val_exception_id"));
				
				exception.setRuleid(resultSet.getString("ruleid"));
				
				exception.setTaskName(resultSet.getString("task_name"));
				
				exception.setGroupid(resultSet.getInt("groupid"));
				
				exception.setLevel(resultSet.getInt("level"));
				
				exception.setSituation(resultSet.getString("situation"));
				
				exception.setInformation(resultSet.getString("information"));
				
				exception.setSuggestion(resultSet.getString("suggestion"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("location");

				exception.setLocation(GeoTranslator.struct2Jts(struct));

				exception.setTargets(resultSet.getString("targets"));
				
				exception.setAdditionInfo(resultSet.getString("addition_info"));
				
				exception.setDelFlag(resultSet.getInt("del_flag"));
				
				exception.setCreated(resultSet.getString("created"));
				
				exception.setUpdated(resultSet.getString("updated"));
				
				exception.setMeshId(resultSet.getInt("mesh_id"));
				
				exception.setScopeFlag(resultSet.getInt("scope_flag"));
				
				exception.setProvinceName(resultSet.getString("province_name"));
				
				exception.setMapScale(resultSet.getInt("map_scale"));
				
				exception.setReserved(resultSet.getString("reserved"));
				
				exception.setExtended(resultSet.getString("extended"));
				
				exception.setTaskId(resultSet.getString("task_id"));
				
				exception.setQaTaskId(resultSet.getString("qa_task_id"));
				
				exception.setQaStatus(resultSet.getInt("qa_status"));
				
				exception.setWorker(resultSet.getString("worker"));
				
				exception.setQaWorker(resultSet.getString("qa_worker"));
				
				exception.setLogType(resultSet.getInt("log_type"));
				
				exception.setMd5Code(resultSet.getString("md5_code"));

			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return exception;
	
	}

	public JSONArray loadByMesh(JSONArray meshes, int pageSize, int page)
			throws Exception {

		JSONArray results = new JSONArray();

		Statement stmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder(
				"select * from (select b.*,rownum rn from (select md5_code,ruleid,situation,\"LEVEL\" level_,targets,information,a.location.sdo_point.x x,"
						+ "a.location.sdo_point.y y,created,worker from ni_val_exception a where mesh_id in (");

		for (int i = 0; i < meshes.size(); i++) {
			if (i > 0) {
				sql.append(",");

				sql.append(meshes.getInt(i));
			} else {
				sql.append(meshes.getInt(i));
			}
		}

		sql.append(") order by created desc ) b where rownum<=");

		sql.append(pageSize * page);

		sql.append(") where rn>");

		sql.append((page - 1) * pageSize);

		try {

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				JSONObject json = new JSONObject();
				
				json.put("id",  rs.getString("md5_code"));

				json.put("ruleid", rs.getString("ruleid"));

				json.put("situation", rs.getString("situation"));

				json.put("rank", rs.getInt("level_"));

				json.put("targets", rs.getString("targets"));

				json.put("information", rs.getString("information"));

				json.put("geometry",
						"(" + rs.getDouble("x") + "," + rs.getDouble("y") + ")");

				json.put("create_date", rs.getString("created"));

				json.put("worker", rs.getString("worker"));

				results.add(json);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		return results;
	}

	public int loadCountByMesh(JSONArray meshes)
			throws Exception {

		Statement stmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder(
				"select count(1) count from ni_val_exception a where mesh_id in (");

		for (int i = 0; i < meshes.size(); i++) {
			if (i > 0) {
				sql.append(",");

				sql.append(meshes.getInt(i));
			} else {
				sql.append(meshes.getInt(i));
			}
		}

		sql.append(")");

		try {

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql.toString());

			if (rs.next()) {
				return rs.getInt("count");
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		return 0;
	}

	public Page list(int subtaskType,Collection<String> grids, final int pageSize, final int pageNum)
			throws Exception {
		
		Clob pidsClob = ConnectionUtil.createClob(conn);
		pidsClob.setString(1, StringUtils.join(grids, ","));		
		StringBuilder sql = new StringBuilder("select a.md5_code,ruleid,situation,\"LEVEL\" level_,"
				+ "targets,information,a.location.sdo_point.x x,a.location.sdo_point.y y,created,"
				+ "worker from ni_val_exception a where exists(select 1 from ni_val_exception_grid b,"
				+ "(select to_number(COLUMN_VALUE) COLUMN_VALUE from table(clob_to_table(?))) grid_table "
				+ "where a.md5_code=b.md5_code and b.grid_id =grid_table.COLUMN_VALUE)");
		
		if(subtaskType==0||subtaskType==5||subtaskType==6||subtaskType==7){
			sql.append(" and EXISTS ("
					+ " SELECT 1 FROM CK_RESULT_OBJECT O "
					+ " WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')"
					+ "   AND O.MD5_CODE=a.MD5_CODE)");
		}

		sql.append(" order by created desc,md5_code desc");
		
		return new QueryRunner().query(pageNum,pageSize,conn, sql.toString(), new ResultSetHandler<Page>(){

			@Override
			public Page handle(ResultSet rs) throws SQLException {
				Page page =new Page(pageNum);
				page.setPageSize(pageSize);
				int total = 0;
				JSONArray results = new JSONArray();
				while(rs.next()){
					
					if(total ==0){total=rs.getInt("TOTAL_RECORD_NUM_");}
					
					JSONObject json = new JSONObject();
					
					json.put("id",  rs.getString("md5_code"));

					json.put("ruleid", rs.getString("ruleid"));

					json.put("situation", rs.getString("situation"));

					json.put("rank", rs.getInt("level_"));

					json.put("targets", rs.getString("targets"));

					json.put("information", rs.getString("information"));

					json.put("geometry",
							"(" + rs.getDouble("x") + "," + rs.getDouble("y") + ")");

					json.put("create_date", rs.getString("created"));

					json.put("worker", rs.getString("worker"));

					results.add(json);
				}
				page.setTotalCount(total);
				page.setResult(results);
				return page;
			}
			
		},pidsClob);
	}
	

	public JSONArray loadByGrid(JSONArray grids, int pageSize, int page)
			throws Exception {

		JSONArray results = new JSONArray();

		Statement stmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("select * from (select tmp.*,rownum rn from (select a.md5_code,ruleid,situation,\"LEVEL\" level_,targets,information,a.location.sdo_point.x x,a.location.sdo_point.y y,created,worker from ni_val_exception a where exists(select 1 from ni_val_exception_grid b where a.md5_code=b.md5_code and b.grid_id in(");

		for (int i = 0; i < grids.size(); i++) {
			if (i > 0) {
				sql.append(",");

				sql.append(grids.getLong(i));
			} else {
				sql.append(grids.getLong(i));
			}
		}

		sql.append(")) order by created desc,md5_code desc ) tmp where rownum<=");

		sql.append(pageSize * page);

		sql.append(") where rn>");

		sql.append((page - 1) * pageSize);

		try {

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				JSONObject json = new JSONObject();
				
				json.put("id",  rs.getString("md5_code"));

				json.put("ruleid", rs.getString("ruleid"));

				json.put("situation", rs.getString("situation"));

				json.put("rank", rs.getInt("level_"));

				json.put("targets", rs.getString("targets"));

				json.put("information", rs.getString("information"));

				json.put("geometry",
						"(" + rs.getDouble("x") + "," + rs.getDouble("y") + ")");

				json.put("create_date", rs.getString("created"));

				json.put("worker", rs.getString("worker"));

				results.add(json);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		return results;
	}

	public int loadCountByGrid(JSONArray grids)
			throws Exception {

		Statement stmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder(
				"select count(distinct(v.md5_code)) count from ni_val_exception v, ni_val_exception_grid g where v.md5_code=g.md5_code and g.grid_id in (");

		for (int i = 0; i < grids.size(); i++) {
			if (i > 0) {
				sql.append(",");

				sql.append(grids.getLong(i));
			} else {
				sql.append(grids.getLong(i));
			}
		}

		sql.append(")");

		try {

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql.toString());

			if (rs.next()) {
				return rs.getInt("count");
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		return 0;
	}
	
	
	public List loadByPid(int pid, List ckRule)
			throws Exception {

		List ruleList =new ArrayList() ;

		Statement stmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("select ruleid from ni_val_exception where dbms_lob.instr(targets,'"+pid+"',1,1)<>0 and ruleid in "+ckRule);                          

		try {

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				ruleList.add(rs.getString("ruleid"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		return ruleList;
	}
	
	public static void main(String[] args) throws Exception {


		NiValExceptionSelector selector = new NiValExceptionSelector(
				DBConnector.getInstance().getConnectionById(11));

		JSONArray grids = new JSONArray();

		grids.add(60560303);
//
//		System.out.println(selector.loadCountByGrid(grids));
//		
//		System.out.println(selector.loadByGrid(grids, 10, 1));
		
		Page page = selector.list(0, grids, 20, 1);
		System.out.println(page.getResult());
	}
}
