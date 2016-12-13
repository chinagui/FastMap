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
	//******************************
/**
 * @Title: listCheckResults
 * @Description: 查询道路名检查结果
 * @param params
 * @param tips
 * @param ruleCodes 
 * @return  Page
 * @throws 
 * @author zl zhangli5174@navinfo.com
 * @date 2016年11月22日 下午8:55:38 
 */
public Page listCheckResults(JSONObject params, JSONArray tips, JSONArray ruleCodes) throws SQLException{
		final int pageSize = params.getInt("pageSize");
		final int pageNum = params.getInt("pageNum");
		int subtaskId = params.getInt("subtaskId");//获取subtaskid 
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;
		StringBuilder sql = new StringBuilder();
		//获取ids
		String ids = "";
		String tmep = "";
		for (int i=0;i<tips.size();i++) {
			JSONObject tipsObj = tips.getJSONObject(i);
			ids += tmep;
			tmep = ",";
			ids +=tipsObj.getString("id");
		}
		String rules = "";
		String tmep2 = "";
		for (int i=0;i<ruleCodes.size();i++) {
			JSONObject ruleObj = ruleCodes.getJSONObject(i);
			rules += tmep2;
			tmep2 = ",";
			rules +=ruleObj.getString("ruleCode");
		}
		
	//	sql.append(" select * from ( ");
		sql.append("with ");
		//**********************
		//获取子任务范围内的所有 rdName 的nameId 
		sql.append("q1 as ( ");
		sql.append("select rd.name_id from ( SELECT null tipid,r.* from rd_name r  where r.src_resume = '\"task\":"+subtaskId+"' ");
		sql.append(" union all ");
		sql.append(" SELECT tt.*  FROM ( select substr(replace(t.src_resume,'\"',''),instr(replace(t.src_resume,'\"',''), ':') + 1,length(replace(src_resume,'\"',''))) as tipid,t.*  from rd_name t  where t.src_resume like '%tips%' ) tt ");
		sql.append(" where 1=1 ");
		//********zl 2016.12.12 新增判断tips 是否有值****************
		if(ids != null && StringUtils.isNotEmpty(ids)){
			sql.append(" and tt.tipid in (select column_value from table(clob_to_table('"+ids+"'))) ");
		}
		sql.append(" ) rd ),");
		//**********************
		//所有道路名的检查结果包含的 nameId 及其 val_exception_id
		sql.append("q2 as ( ");
		sql.append(" SELECT distinct  to_char(replace(REGEXP_SUBSTR(t.addition_info,'NAME_ID,[0-9]+', 1, LEVEL, 'i'),'NAME_ID,',''))  nameid ,t.val_exception_id eid ");
		sql.append(" FROM ( ");
		sql.append(" select a.* from ni_val_exception a  ");
		sql.append(" where a.ruleid in (select column_value from table(clob_to_table('"+rules+"'))) ");
		sql.append(" ) t ");
		sql.append(" CONNECT BY LEVEL <= LENGTH(t.addition_info) - LENGTH(REGEXP_REPLACE(t.addition_info, 'NAME_ID,', 'NAME_ID'))  ");
		sql.append(" ),");
		//*********************
		sql.append("q3 as ( ");
		sql.append("select distinct b.eid from q2 b ,q1 c where b.nameid = c.name_id  ");
		sql.append(" ), ");
		//**********************
		sql.append("q4 as ( ");
		sql.append(" select NVL(d.md5_code,0) md5_code,NVL(d.ruleid,0) ruleid,NVL(d.situation,'') situation,\"LEVEL\" level_,"
				+ "NVL(to_char(d.addition_info),'') targets,"
				+ "NVL(d.information,'') information, "
				+ "NVL(d.location.sdo_point.x,0) x, "
				+ "NVL(d.location.sdo_point.y,0) y,"
				+ "d.created,NVL(d.worker,'') worker  "
				+ "from ni_val_exception d ,q3 e where d.val_exception_id = e.eid ");
		sql.append(") ");
		//************************
		sql.append(" SELECT A.*,(SELECT COUNT(1) FROM q4) AS TOTAL_RECORD_NUM_  "
				+ "FROM "
				+ "(SELECT T.*, ROWNUM AS ROWNO FROM q4 T ");
		sql.append(" WHERE ROWNUM <= "+pageEndNum+") A "
				+ "WHERE A.ROWNO >= "+pageStartNum+" ");
		sql.append(" order by created desc,md5_code desc ");
		//System.out.println("listCheckResults sql:  "+sql.toString());
		
		QueryRunner run=new QueryRunner();
		
		//****************************************
		ResultSetHandler<Page> rsHandler3=new ResultSetHandler<Page>() {
			public Page handle(ResultSet rs) throws SQLException{
				Page page = new Page();
				int total=0;
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
		};
		
		Page p = run.query(conn, sql.toString(),rsHandler3);
			return p;
	}
	//******************************
	
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
}
