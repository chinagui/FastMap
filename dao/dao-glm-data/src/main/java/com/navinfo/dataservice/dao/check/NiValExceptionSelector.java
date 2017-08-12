package com.navinfo.dataservice.dao.check;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class NiValExceptionSelector {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;

	public NiValExceptionSelector(Connection conn) {
		this.conn = conn;
	}

	public NiValException loadByExId(String id, boolean isLock)
			throws Exception {

		NiValException exception = new NiValException();

		String sql = "select * from ni_val_exception where val_exception_id=:1";

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

				exception.setValExceptionId(resultSet
						.getInt("val_exception_id"));

				exception.setRuleid(resultSet.getString("ruleid"));

				exception.setTaskName(resultSet.getString("task_name"));

				exception.setGroupid(resultSet.getInt("groupid"));

				exception.setLevel(resultSet.getInt("level"));

				exception.setSituation(resultSet.getString("situation"));

				exception.setInformation(resultSet.getString("information"));

				exception.setSuggestion(resultSet.getString("suggestion"));
				if (resultSet.getObject("location") != null) {
					STRUCT struct = (STRUCT) resultSet.getObject("location");

					exception.setLocation(GeoTranslator.struct2Jts(struct));
				}

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

				exception.setValExceptionId(resultSet
						.getInt("val_exception_id"));

				exception.setRuleid(resultSet.getString("ruleid"));

				exception.setTaskName(resultSet.getString("task_name"));

				exception.setGroupid(resultSet.getInt("groupid"));

				exception.setLevel(resultSet.getInt("level"));

				exception.setSituation(resultSet.getString("situation"));

				exception.setInformation(resultSet.getString("information"));

				exception.setSuggestion(resultSet.getString("suggestion"));
				if (resultSet.getObject("location") != null) {
					STRUCT struct = (STRUCT) resultSet.getObject("location");

					exception.setLocation(GeoTranslator.struct2Jts(struct));
				}

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

				json.put("id", rs.getString("md5_code"));

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

	public int loadCountByMesh(JSONArray meshes) throws Exception {

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

	/***
	 * 
	 * @param subtaskType
	 * @param grids
	 * @param pageSize
	 * @param pageNum
	 * @param flag
	 *            0 全部 1 待处理 2确认修改 3 确认不修改 4 例外 5 未质检 6 已质检
	 * @parm level 0 全部 1 错误 2 警告 3 提示
	 * @param ruleId
	 *            规则号
	 * @return
	 * @throws Exception
	 */
	public Page list(int subtaskType, Collection<String> grids,
			final int pageSize, final int pageNum, int flag, String ruleId,
			int level) throws Exception {

		Clob pidsClob = ConnectionUtil.createClob(conn);
		pidsClob.setString(1, StringUtils.join(grids, ","));

		StringBuilder sql1 = new StringBuilder(
				"select a.md5_code,ruleid,situation,\"LEVEL\" level_,0 state,"
						+ "targets,information,a.location.sdo_point.x x,a.location.sdo_point.y y,created,updated,"
						+ "worker,qa_worker,qa_status from ni_val_exception a where a.md5_code in (select b.md5_code from ni_val_exception_grid b,"
						+ "(select to_number(COLUMN_VALUE) COLUMN_VALUE from table(clob_to_table(?))) grid_table "
						+ "where b.grid_id =grid_table.COLUMN_VALUE) ");
		StringBuilder sql4 = new StringBuilder(
				"select a.md5_code,rule_id as ruleid,situation,rank level_,1 state,"
						+ "targets,information,decode(a.geometry,null,0.0,(sdo_util.from_wktgeometry(a.geometry)).sdo_point.x) x,decode(a.geometry,null,0.0,(sdo_util.from_wktgeometry(a.geometry)).sdo_point.y )y,create_date as created,update_date as updated,"
						+ "worker,qa_worker,qa_status from ck_exception a where a.status = 1 and a.row_id in (select b.ck_row_id from ck_exception_grid b,"
						+ "(select to_number(COLUMN_VALUE) COLUMN_VALUE from table(clob_to_table(?))) grid_table "
						+ "where b.grid_id =grid_table.COLUMN_VALUE) ");
		StringBuilder sql3 = new StringBuilder(
				"select a.md5_code,rule_id as ruleid,situation,rank level_,2 state,"
						+ "targets,information,decode(a.geometry,null,0.0,(sdo_util.from_wktgeometry(a.geometry)).sdo_point.x )x,decode(a.geometry,null,0.0,(sdo_util.from_wktgeometry(a.geometry)).sdo_point.y )y,create_date as created,update_date as updated,"
						+ "worker,qa_worker,qa_status from ck_exception a where a.status = 2 and a.row_id in (select b.ck_row_id from ck_exception_grid b,"
						+ "(select to_number(COLUMN_VALUE) COLUMN_VALUE from table(clob_to_table(?))) grid_table "
						+ "where b.grid_id =grid_table.COLUMN_VALUE) ");
		StringBuilder sql2 = new StringBuilder(
				"   select a.md5_code,ruleid,situation,\"LEVEL\" level_,3 state,"
						+ "targets,information,a.location.sdo_point.x x,a.location.sdo_point.y y,created,updated,"
						+ "worker ,qa_worker,qa_status from ni_val_exception_history a where a.md5_code in (select b.md5_code from ni_val_exception_history_grid b,"
						+ "(select to_number(COLUMN_VALUE) COLUMN_VALUE from table(clob_to_table(?))) grid_table "
						+ "where b.grid_id =grid_table.COLUMN_VALUE) ");

		StringBuilder sql = null;
		if (StringUtils.isNotEmpty(ruleId)) {
			sql1 = sql1.append(" AND  ruleid  = '" + ruleId + "' ");
			sql2 = sql2.append(" AND ruleid  = '" + ruleId + "' ");
			sql3 = sql3.append(" AND rule_id  = '" + ruleId + "' ");
			sql4 = sql4.append(" AND rule_id  = '" + ruleId + "' ");
		}
		if (level != 0) {
			sql1 = sql1.append(" AND \"LEVEL\"  = " + level + " ");
			sql2 = sql2.append(" AND  \"LEVEL\"  = " + level + " ");
			sql3 = sql3.append(" AND rank  = " + level + " ");
			sql4 = sql4.append(" AND rank  = " + level + " ");
		}

		if (flag == 0) {
			sql = sql1.append(" union all ").append(sql2).append(" union all ")
					.append(sql3).append(" union all ").append(sql4);
		}
		if (flag == 1) {
			sql = sql1;
		}
		if (flag == 2) {
			sql = sql2;
		}
		if (flag == 3) {
			sql = sql3;
		}
		if (flag == 4) {
			sql = sql4;
		}
		if (flag == 5) {
			sql1 = sql1.append(" AND qa_status  = " + 2 + " ");
			sql2 = sql2.append(" AND qa_status  = " + 2 + " ");
			sql3 = sql3.append(" AND qa_status  = " + 2 + " ");
			sql4 = sql4.append(" AND qa_status  = " + 2 + " ");
			sql = sql1.append(" union all ").append(sql2).append(" union all ")
					.append(sql3).append(" union all ").append(sql4);
		}
		if (flag == 6) {
			sql1 = sql1.append(" AND qa_status  = " + 1 + " ");
			sql2 = sql2.append(" AND qa_status  = " + 1 + " ");
			sql3 = sql3.append(" AND qa_status  = " + 1 + " ");
			sql4 = sql4.append(" AND qa_status  = " + 1 + " ");
			sql = sql1.append(" union all ").append(sql2).append(" union all ")
					.append(sql3).append(" union all ").append(sql4);
		}
		StringBuilder resultSql = new StringBuilder();
		resultSql.append(" select * from (" + sql + ") a");
		// 道路检查排除POI
		resultSql
				.append(" where  NOT EXISTS ("
						+ " SELECT 1 FROM CK_RESULT_OBJECT O "
						+ " WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')"
						+ "   AND O.MD5_CODE=a.MD5_CODE)");

		resultSql.append(" order by created desc,md5_code desc");
		log.info("resultSql ====" + resultSql.toString());
		Page page = null;
		if (flag == 0 || flag == 5 || flag == 6) {
			page = new QueryRunner().query(pageNum, pageSize, conn,
					resultSql.toString(), new ResultSetHandler<Page>() {

						@Override
						public Page handle(ResultSet rs) throws SQLException {
							return handResult(pageNum, pageSize, rs);
						}
					}, pidsClob, pidsClob, pidsClob, pidsClob);
		} else {
			page = new QueryRunner().query(pageNum, pageSize, conn,
					resultSql.toString(), new ResultSetHandler<Page>() {

						@Override
						public Page handle(ResultSet rs) throws SQLException {
							return handResult(pageNum, pageSize, rs);
						}
					}, pidsClob);
		}
		return page;
	}

	/***
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private Page handResult(int pageNum, int pageSize, ResultSet rs)
			throws SQLException {

		Page page = new Page(pageNum);
		page.setPageSize(pageSize);
		int total = 0;
		JSONArray results = new JSONArray();
		while (rs.next()) {

			if (total == 0) {
				total = rs.getInt("TOTAL_RECORD_NUM_");
			}

			JSONObject json = new JSONObject();

			json.put("id", rs.getString("md5_code"));

			json.put("ruleid", rs.getString("ruleid"));

			json.put("situation", rs.getString("situation"));

			json.put("rank", rs.getInt("level_"));
			json.put("status", rs.getInt("state"));

			json.put("targets", rs.getString("targets"));

			json.put("information", rs.getString("information"));

			json.put("geometry",
					"(" + rs.getDouble("x") + "," + rs.getDouble("y") + ")");

			json.put("create_date", rs.getString("created"));
			json.put("update_date", rs.getString("updated"));

			json.put("worker", rs.getString("worker"));
			json.put(
					"qa_worker",
					rs.getString("qa_worker") == null ? "" : rs
							.getString("qa_worker"));
			json.put("qa_status", rs.getString("qa_status"));

			results.add(json);
		}
		page.setTotalCount(total);
		page.setResult(results);
		return page;

	}

	// ******************************
	/**
	 * @Title: listCheckResults
	 * @Description: 查询道路名检查结果
	 * @param params
	 * @param tips
	 * @param ruleCodes
	 * @return Page
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月22日 下午8:55:38
	 */
	public Page listCheckResults(JSONObject params, JSONArray tips,
			JSONArray ruleCodes) throws SQLException {
		final int pageSize = params.getInt("pageSize");
		final int pageNum = params.getInt("pageNum");
		int subtaskId = params.getInt("subtaskId");// 获取subtaskid
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;
		StringBuilder sql = new StringBuilder();
		// 获取ids
		String ids = "";
		String tmep = "";
		for (int i = 0; i < tips.size(); i++) {
			JSONObject tipsObj = tips.getJSONObject(i);
			ids += tmep;
			tmep = ",";
			ids += tipsObj.getString("id");
		}
		String rules = "";
		String tmep2 = "";
		for (int i = 0; i < ruleCodes.size(); i++) {
			JSONObject ruleObj = ruleCodes.getJSONObject(i);
			rules += tmep2;
			tmep2 = ",";
			rules += ruleObj.getString("ruleCode");
		}

		// sql.append(" select * from ( ");
		sql.append("with ");
		// **********************
		// 获取子任务范围内的所有 rdName 的nameId
		sql.append("q1 as ( ");
		sql.append("select rd.name_id from ( SELECT null tipid,r.* from rd_name r  where r.src_resume = '\"task\":"
				+ subtaskId + "' ");
		sql.append(" union all ");
		sql.append(" SELECT tt.*  FROM ( select substr(replace(t.src_resume,'\"',''),instr(replace(t.src_resume,'\"',''), ':') + 1,length(replace(src_resume,'\"',''))) as tipid,t.*  from rd_name t  where t.src_resume like '%tips%' ) tt ");
		sql.append(" where 1=1 ");
		// ********zl 2016.12.12 新增判断tips 是否有值****************
		if (ids != null && StringUtils.isNotEmpty(ids)) {
			sql.append(" and tt.tipid in (select column_value from table(clob_to_table('"
					+ ids + "'))) ");
		}
		sql.append(" ) rd ),");
		// **********************
		// 所有道路名的检查结果包含的 nameId 及其 val_exception_id
		/*
		 * select t.val_exception_id eid,REGEXP_SUBSTR(t.addition_info,
		 * '(\[NAME_ID,[0-9]+)', 1, LEVEL, 'i') nameid from (select
		 * a.val_exception_id,to_char(a.addition_info) addition_info from
		 * ni_val_exception a where a.ruleid in (select column_value from
		 * table(clob_to_table(
		 * 'COM01001,COM01003,COM20552,COM60104,GLM02115,GLM02129,GLM02130,GLM02131,GLM02132,GLM02137,GLM02138,GLM02139,GLM02142,GLM02145,GLM02150,GLM02154,GLM02156,GLM02157,GLM02166,GLM02167,GLM02170,GLM02173,GLM02183,GLM02187,GLM02191,GLM02197,GLM02198,GLM02209,GLM02213,GLM02214,GLM02215,GLM02216,GLM02223,GLM02224,GLM02227,GLM02228,GLM02230,GLM02233,GLM02234,GLM02235,GLM02236,GLM02248,GLM02254,GLM02260,GLM02261,GLM02262,GLM02269,GLM02270,GLM90216')))
		 * and a.addition_info like '%NAME_ID,%' ) t CONNECT BY LEVEL <=
		 * LENGTH(t.addition_info) - LENGTH(replace(t.addition_info,
		 * '[NAME_ID,', '[NAME_ID'))
		 */
		sql.append("q2 as ( ");
		sql.append(" SELECT  t.val_exception_id eid, replace(REGEXP_SUBSTR(t.addition_info,'(\\[NAME_ID,[0-9]+)', 1, LEVEL, 'i'),'[NAME_ID,','')  nameid  ");
		sql.append(" FROM ( ");
		sql.append(" select a.val_exception_id,to_char(a.addition_info) addition_info from ni_val_exception a  ");
		sql.append(" where a.ruleid in (select column_value from table(clob_to_table('"
				+ rules + "'))) ");
		sql.append(" ) t ");
		sql.append(" CONNECT BY LEVEL <= LENGTH(t.addition_info) - LENGTH(replace(t.addition_info, '[NAME_ID,', '[NAME_ID'))  ");
		sql.append(" ),");

		/*
		 * sql.append("q2 as ( "); sql.append(
		 * " SELECT distinct  to_char(replace(REGEXP_SUBSTR(t.addition_info,'NAME_ID,[0-9]+', 1, LEVEL, 'i'),'NAME_ID,',''))  nameid ,t.val_exception_id eid "
		 * ); sql.append(" FROM ( ");
		 * sql.append(" select a.* from ni_val_exception a  "); sql.append(
		 * " where a.ruleid in (select column_value from table(clob_to_table('"
		 * +rules+"'))) "); sql.append(" ) t "); sql.append(
		 * " CONNECT BY LEVEL <= LENGTH(t.addition_info) - LENGTH(REGEXP_REPLACE(t.addition_info, 'NAME_ID,', 'NAME_ID'))  "
		 * ); sql.append(" ),");
		 */
		// *********************
		sql.append("q3 as ( ");
		sql.append("select distinct b.eid from q2 b ,q1 c where b.nameid = c.name_id  ");
		sql.append(" ), ");
		// **********************
		sql.append("q4 as ( ");
		sql.append(" select NVL(d.md5_code,0) md5_code,NVL(d.ruleid,0) ruleid,NVL(d.situation,'') situation,\"LEVEL\" level_,"
				+ "NVL(to_char(d.addition_info),'') targets,"
				+ "NVL(d.information,'') information, "
				+ "NVL(d.location.sdo_point.x,0) x, "
				+ "NVL(d.location.sdo_point.y,0) y,"
				+ "d.created,NVL(d.worker,'') worker  "
				+ "from ni_val_exception d ,q3 e where d.val_exception_id = e.eid ");
		sql.append(") ");
		// ************************
		sql.append(" SELECT A.*,(SELECT COUNT(1) FROM q4) AS TOTAL_RECORD_NUM_  "
				+ "FROM " + "(SELECT T.*, ROWNUM AS ROWNO FROM q4 T ");
		sql.append(" WHERE ROWNUM <= " + pageEndNum + ") A "
				+ "WHERE A.ROWNO >= " + pageStartNum + " ");
		sql.append(" order by created desc,md5_code desc ");
		log.info("listCheckResults sql:  " + sql.toString());

		QueryRunner run = new QueryRunner();

		// ****************************************
		ResultSetHandler<Page> rsHandler3 = new ResultSetHandler<Page>() {
			public Page handle(ResultSet rs) throws SQLException {
				Page page = new Page();
				int total = 0;
				JSONArray results = new JSONArray();
				while (rs.next()) {
					if (total == 0) {
						total = rs.getInt("TOTAL_RECORD_NUM_");
					}

					JSONObject json = new JSONObject();

					json.put("id", rs.getString("md5_code"));

					json.put("ruleid", rs.getString("ruleid"));

					json.put("situation", rs.getString("situation"));

					json.put("rank", rs.getInt("level_"));

					json.put("targets", rs.getString("targets"));

					json.put("information", rs.getString("information"));

					json.put("geometry",
							"(" + rs.getDouble("x") + "," + rs.getDouble("y")
									+ ")");

					json.put("create_date", rs.getString("created"));

					json.put("worker", rs.getString("worker"));
					results.add(json);
				}
				page.setTotalCount(total);
				page.setResult(results);
				return page;
			}
		};

		Page p = run.query(conn, sql.toString(), rsHandler3);
		return p;
	}

	// ******************************

	public JSONArray loadByGrid(JSONArray grids, int pageSize, int page)
			throws Exception {

		JSONArray results = new JSONArray();

		Statement stmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder(
				"select * from (select tmp.*,rownum rn from (select a.md5_code,ruleid,situation,\"LEVEL\" level_,targets,information,a.location.sdo_point.x x,a.location.sdo_point.y y,created,worker from ni_val_exception a where exists(select 1 from ni_val_exception_grid b where a.md5_code=b.md5_code and b.grid_id in(");

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

				json.put("id", rs.getString("md5_code"));

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

	public int loadCountByGrid(JSONArray grids) throws Exception {

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

	public List loadByPid(int pid, List<String> ckRule) throws Exception {

		List ruleList = new ArrayList();

		Statement stmt = null;

		ResultSet rs = null;

		String ckRules = "(";

		for (String rule : ckRule) {
			ckRules += "'" + rule + "',";
		}
		ckRules = ckRules.substring(0, ckRules.length() - 1);

		ckRules += ")";

		StringBuilder sql = new StringBuilder(
				"SELECT n.ruleid FROM ck_result_object c,ni_val_exception n WHERE c.pid="
						+ pid + " AND c.md5_code=n.md5_code AND n.ruleid IN "
						+ ckRules);

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

	/**
	 * @Title: listPoiCheckResultList
	 * @Description: 子任务范围内poi检查结果列表查询接口
	 * @param params
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 *             Page
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月23日 下午4:07:22
	 */
	public Page listPoiCheckResultList(JSONObject params, int subtaskId)
			throws Exception {
		log.info(" begin time"
				+ DateUtils.dateToString(new Date(),
						DateUtils.DATE_DEFAULT_FORMAT));
		Page p = null;
		QueryRunner run = null;
		final int pageSize = params.getInt("pageSize");
		final int pageNum = params.getInt("pageNum");

		String sortby = "";
		if (params.containsKey("sortby")) {
			sortby = params.getString("sortby");
		}
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;
		List<Integer> pids = getCheckPidList(conn, subtaskId);

		log.info("pids :" + pids.size());
		try {
			if (pids != null && pids.size() > 0) {
				String orderSql = "";
				com.navinfo.dataservice.commons.util.StringUtils sUtils = new com.navinfo.dataservice.commons.util.StringUtils();
				// 添加排序条件
				if (sortby.length() > 0) {
					int index = sortby.indexOf("-");
					if (index != -1) {
						orderSql += " ORDER BY ";
						String sortbyName = sUtils.toColumnName(sortby
								.substring(1));
						orderSql += "  ";
						orderSql += sortbyName;
						orderSql += " DESC";
					} else {
						orderSql += " ORDER BY ";
						String sortbyName = sUtils.toColumnName(sortby
								.substring(1));
						orderSql += "  ";
						orderSql += sortbyName;
					}
				}

				StringBuilder sql = new StringBuilder(
						"SELECT q.* FROM ( SELECT T.*, ROWNUM AS ROWNO FROM ("
								+ "select b.*,count(1) over () total from ( "
								+ "select a.md5_code,a.ruleid,a.\"LEVEL\" level_,a.targets,a.information,a.worker ,a.created,a.location.sdo_point.x x,a.location.sdo_point.y y,a.updated,a.qa_worker,a.qa_status,O.PID "
								+ "from "
								+ "ni_val_exception a  , CK_RESULT_OBJECT O  "
								+ "WHERE  (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')  AND O.MD5_CODE=a.MD5_CODE "
								+ " and O.pid in (select column_value from table(clob_to_table(?)) "
								+ ") "
								+ " union all "
								+ "select c.md5_code,c.rule_id ruleid,c.status level_,c.targets,c.information,c.worker ,c.create_date created,(sdo_util.from_wktgeometry(c.geometry)).sdo_point.x x,(sdo_util.from_wktgeometry(c.geometry)).sdo_point.y y,c.update_date as updated,c.qa_worker,c.qa_status,O.PID "
								+ "from "
								+ "ck_exception c , CK_RESULT_OBJECT O "
								+ "  WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')  AND O.MD5_CODE=c.MD5_CODE "
								+ " and O.pid in (select column_value from table(clob_to_table(?)) "
								+ " )  "
								+ " )  b  "
								+ orderSql
								+ " ) T  WHERE ROWNUM <= ? ) q  WHERE q.ROWNO >= ? ");

				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));

				log.info("poiCheckResultList:  " + sql);
				run = new QueryRunner();

				ResultSetHandler<Page> rsHandler3 = new ResultSetHandler<Page>() {
					public Page handle(ResultSet rs) throws SQLException {
						Page page = new Page();
						int total = 0;
						JSONArray results = new JSONArray();
						while (rs.next()) {
							if (total == 0) {
								total = rs.getInt("total");
							}

							JSONObject json = new JSONObject();
							json.put("id", rs.getString("md5_code"));

							json.put("ruleid", rs.getString("ruleid"));

							// json.put("situation", rs.getString("situation"));

							json.put("rank", rs.getInt("level_"));

							String targets = "";
							if (rs.getString("targets") != null
									&& StringUtils.isNotEmpty(rs
											.getString("targets"))) {
								targets = rs.getString("targets");
							}
							json.put("targets", targets);

							json.put("information", rs.getString("information"));

							json.put("geometry", "(" + rs.getDouble("x") + ","
									+ rs.getDouble("y") + ")");

							json.put("create_date", rs.getString("created"));
							json.put("update_date", rs.getString("updated"));

							json.put("worker", rs.getString("worker"));
							json.put(
									"qa_worker",
									rs.getString("qa_worker") == null ? "" : rs
											.getString("qa_worker"));
							json.put("qa_status", rs.getString("qa_status"));

							JSONArray refFeaturesArr = new JSONArray();

							if (targets != null
									&& StringUtils.isNotEmpty(targets)) {

								String pids = targets
										.replaceAll("[\\[\\]]", "")
										.replaceAll("IX_POI,", "")
										.replaceAll(";", ",");
								System.out.println(pids + " "
										+ rs.getInt("pid"));
								refFeaturesArr = queryRefFeatures(pids,
										rs.getInt("pid"));
							}
							// 查询关联poi根据pid
							json.put("refFeatures", refFeaturesArr);
							json.put("refCount", refFeaturesArr.size());
							results.add(json);
							System.out.println("json: " + json);
						}
						page.setTotalCount(total);
						page.setResult(results);

						return page;
					}
				};
				p = run.query(conn, sql.toString(), new Object[] { clob, clob,
						pageEndNum, pageStartNum }, rsHandler3);

			}
			log.info(" end time"
					+ DateUtils.dateToString(new Date(),
							DateUtils.DATE_DEFAULT_FORMAT));
			return p;
		} catch (Exception e) {
			throw new Exception(e);
		} finally {

		}
	}

	

	/**
	 * @Title: getCheckPidList
	 * @Description: 查询子任务范围内的poi.pid
	 * @param conn
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 *             List<Long>
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月23日 下午4:14:11
	 */
	private List<Integer> getCheckPidList(Connection conn, int subtaskId)
			throws Exception {
		List<Integer> pids = null;
		try {
			ManApi apiService = (ManApi) ApplicationContextUtil
					.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			// 行编有针对删除数据进行的检查，此处要把删除数据也加载出来
			String sql = "SELECT ip.pid"
					+ "  FROM ix_poi ip, poi_edit_status ps"
					+ " WHERE ip.pid = ps.pid"
//					+ "   AND ps.work_type = 1"
					+ " AND ps.status in (1,2) "
					+ " and (ps.quick_subtask_id="+subtask.getSubtaskId()+" or ps.medium_subtask_id="+subtask.getSubtaskId()+") "
					// + "   and ip.u_record!=2"
					/*+ "   AND sdo_within_distance(ip.geometry,"
					+ "                           sdo_geometry('"
					+ subtask.getGeometry() + "', 8307),"
					+ "                           'mask=anyinteract') = 'TRUE'"*/
					
					;

			log.info("getCheckPidList sql: " + sql);
			QueryRunner run = new QueryRunner();
			pids = run.query(conn, sql, new ResultSetHandler<List<Integer>>() {

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> pids = new ArrayList<Integer>();
					while (rs.next()) {
						pids.add(rs.getInt("PID"));
					}
					return pids;
				}
			});

		} catch (Exception e) {
			log.error("行编获取检查数据报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception(e);
		}

		return pids;
	}

	/**
	 * @Title: queryRefFeatures
	 * @Description: 根据pid 获取关联poi的数据
	 * @param pid
	 * @return JSONArray
	 * @throws SQLException
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月19日
	 */
	public JSONArray queryRefFeatures(String pids, int thisPid)
			throws SQLException {
		StringBuilder sql = new StringBuilder(
				" select t.pid,t.kind_code,t.geometry,t.\"LEVEL\" level_,t.u_record,t.link_pid,t.poi_num fid,(select n.name from ix_poi_name n where n.poi_pid = t.pid  and n.name_type = 1 AND n.lang_code =  'CHI' and n.name_class = 1) name "
						+ "from ix_poi t  where t.pid in ("
						+ pids
						+ ")  and t.pid != " + thisPid + " ");
		log.info("queryRefFeatures : " + sql);

		try {
			return new QueryRunner().query(conn, sql.toString(),
					new ResultSetHandler<JSONArray>() {

						@Override
						public JSONArray handle(ResultSet rs)
								throws SQLException {

							JSONArray results = new JSONArray();
							while (rs.next()) {
								JSONObject json = new JSONObject();

								json.put("name", rs.getString("name"));

								json.put("kindCode", rs.getString("kind_code"));

								json.put("fid", rs.getString("fid"));

								json.put("level", rs.getString("level_"));

								json.put("pid", rs.getInt("pid"));

								// json.put("refType", rs.getInt("ref_type"));

								int lifecycle = 0;
								switch (rs.getInt("u_record")) {
								case 0:
									lifecycle = 0;
									break;
								case 1:
									lifecycle = 3;
									break;
								case 2:
									lifecycle = 1;
									break;
								case 3:
									lifecycle = 2;
									break;
								}

								json.put("state", lifecycle);

								json.put("linkPid", rs.getInt("link_pid"));

								STRUCT struct = (STRUCT) rs
										.getObject("geometry");
								Geometry geometry = null;
								GeoTranslator trans = null;
								String geometryStr = null;
								try {
									geometry = GeoTranslator.struct2Jts(struct);
									trans = new GeoTranslator();
									geometryStr = trans.jts2Wkt(geometry, 1, 5);
								} catch (Exception e) {
									log.info("查询结果获取Geometry失败");
									e.printStackTrace();
								}
								json.put("geometry", geometryStr);
								results.add(json);
							}

							return results;
						}
					});
		} catch (SQLException e) {
			throw e;
		}
	}

	public Page listCheckResultsByJobId(JSONObject params, Integer jobId,
			String jobUuid) throws SQLException {
		final int pageSize = params.getInt("pageSize");
		final int pageNum = params.getInt("pageNum");

		String sortby = "";
		if (params.containsKey("sortby")) {
			sortby = params.getString("sortby");
		}
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;

		String orderSpl = "";
		com.navinfo.dataservice.commons.util.StringUtils sUtils = new com.navinfo.dataservice.commons.util.StringUtils();
		// 添加排序条件
		if (sortby.length() > 0) {
			int index = sortby.indexOf("-");
			if (index != -1) {
				orderSpl += " ORDER BY ";
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				orderSpl += "  ";
				orderSpl += sortbyName;
				orderSpl += " DESC";
			} else {
				orderSpl += " ORDER BY ";
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				orderSpl += "  ";
				orderSpl += sortbyName;
			}
		}

		StringBuilder sql = new StringBuilder();

		sql.append("with ");

		sql.append("q3 as ( ");
		sql.append(" select val_exception_id id,NVL(d.md5_code,0) md5_code,NVL(d.ruleid,0) ruleid,NVL(d.situation,'') situation,\"LEVEL\" rank,"
				// + "NVL(to_char(d.addition_info),'') targets,"
				+ "substr(to_char(d.ADDITION_INFO),1,instr(to_char(d.ADDITION_INFO),']')) targets,"
				+ "NVL(d.information,'') information, "
				+ "NVL(d.location.sdo_point.x,0) x, "
				+ "NVL(d.location.sdo_point.y,0) y,"
				+ "d.created,NVL(d.worker,'') worker  "
				+ "from ni_val_exception d  where 1=1 "
				+ " and d.addition_info like '[NAME_ID,%' "
				+ " and d.task_name = '" + jobUuid + "' " + orderSpl);
		sql.append(") ");

		// ************************
		sql.append(" SELECT " + jobId
				+ " jobId,A.*,(SELECT COUNT(1) FROM q3) AS TOTAL_RECORD_NUM_  "
				+ "FROM " + "(SELECT T.*, ROWNUM AS ROWNO FROM q3 T ");
		sql.append(" WHERE ROWNUM <= " + pageEndNum + ") A "
				+ "WHERE A.ROWNO >= " + pageStartNum + " ");

		// sql.append(" order by level_ desc ");
		log.info("listCheckResultsByJobId sql:  " + sql.toString());

		QueryRunner run = new QueryRunner();

		// ****************************************
		ResultSetHandler<Page> rsHandler3 = new ResultSetHandler<Page>() {
			public Page handle(ResultSet rs) throws SQLException {
				Page page = new Page();
				int total = 0;
				JSONArray results = new JSONArray();
				while (rs.next()) {
					if (total == 0) {
						total = rs.getInt("TOTAL_RECORD_NUM_");
					}

					JSONObject json = new JSONObject();

					json.put("jobId", rs.getInt("jobId"));

					json.put("id", rs.getString("id"));

					json.put("md5_code", rs.getString("md5_code"));

					json.put("ruleid", rs.getString("ruleid"));

					json.put("situation", rs.getString("situation"));

					json.put("rank", rs.getInt("rank"));

					json.put("targets", rs.getString("targets"));

					json.put("information", rs.getString("information"));

					json.put("geometry",
							"(" + rs.getDouble("x") + "," + rs.getDouble("y")
									+ ")");

					json.put("create_date", rs.getString("created"));

					json.put("worker", rs.getString("worker"));
					results.add(json);
				}
				page.setTotalCount(total);
				page.setResult(results);
				return page;
			}
		};

		Page p = run.query(conn, sql.toString(), rsHandler3);
		return p;
	}

	/**
	 * @Title: listCheckResultsByTaskName
	 * @Description: 元数据库编辑平台 根据taskname 查询检查结果
	 * @param params
	 * @param adminMap
	 * @return
	 * @throws SQLException
	 *             Page
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月4日 下午6:09:44
	 */
	public Page listCheckResultsByTaskName(JSONObject params,
			final Map<String, String> adminMap) throws SQLException {
		final int pageSize = params.getInt("pageSize");
		final int pageNum = params.getInt("pageNum");

		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;

		String taskName = params.getString("taskName");
		String sql_where_r = "";
		String sql_where_e = "";
		// 根据查询条件查询检查结果
		JSONObject paramsObj = params.getJSONObject("params");
		if (paramsObj != null) {
			if (paramsObj.containsKey("name")
					&& paramsObj.getString("name") != null
					&& StringUtils.isNotEmpty(paramsObj.getString("name"))) {
				sql_where_r += " and  n.name like '%"
						+ paramsObj.getString("name") + "%' ";
			}
			if (paramsObj.containsKey("nameId")
					&& paramsObj.getString("nameId") != null
					&& StringUtils.isNotEmpty(paramsObj.getString("nameId"))) {
				sql_where_r += " and  n.name_id = "
						+ paramsObj.getString("nameId") + " ";
			}
			if (paramsObj.containsKey("adminId")
					&& paramsObj.getString("adminId") != null
					&& StringUtils.isNotEmpty(paramsObj.getString("adminId"))) {
				sql_where_r += " and n.admin_id =  "
						+ paramsObj.getString("adminId") + " ";
			}
			if (paramsObj.containsKey("namePhonetic")
					&& paramsObj.getString("namePhonetic") != null
					&& StringUtils.isNotEmpty(paramsObj
							.getString("namePhonetic"))) {
				sql_where_r += " and  n.name_phonetic like '%"
						+ paramsObj.getString("namePhonetic") + "%' ";
			}
			if (paramsObj.containsKey("ruleCode")
					&& paramsObj.getString("ruleCode") != null
					&& StringUtils.isNotEmpty(paramsObj.getString("ruleCode"))) {
				sql_where_e += " and  d.ruleid like '%"
						+ paramsObj.getString("ruleCode") + "%' ";
			}
			if (paramsObj.containsKey("information")
					&& paramsObj.getString("information") != null
					&& StringUtils.isNotEmpty(paramsObj
							.getString("information"))) {
				sql_where_e += " and  d.information like '%"
						+ paramsObj.getString("information") + "%' ";
			}

		}

		StringBuilder sql = new StringBuilder();

		sql.append("with ");
		// **********************
		// 获取子任务范围内的所有 rdName 的nameId
		sql.append("q1 as ( ");
		// sql.append("select '[NAME_ID,'||r.name_id||']' targets,r.* from rd_name r where 1=1 ");
		sql.append("select  n.name_id, n.name, n.name_phonetic, n.road_type, n.admin_id from rd_name n where 1=1 ");

		sql.append(sql_where_r);

		sql.append(" ),");
		// **********************
		sql.append("q2 as ( ");
		sql.append(" select val_exception_id id,NVL(d.MD5_CODE,0) md5_code,NVL(d.RULEID,0) ruleid,NVL(d.SITUATION,'') situation,\"LEVEL\" level_,"
				+ "NVL(to_char(d.ADDITION_INFO),'') targets,"
				+ "substr(to_char(d.ADDITION_INFO),1,instr(to_char(d.ADDITION_INFO),']')) target,"
				+ "NVL(d.INFORMATION,'') information, "
				+ "NVL(d.LOCATION.SDO_POINT.X,0) x, "
				+ "NVL(d.LOCATION.SDO_POINT.Y,0) y,"
				+ "d.created,NVL(d.WORKER,'') worker  "
				+ "from ni_val_exception d  where d.TASK_NAME = '"
				+ taskName
				+ "' and to_char(d.ADDITION_INFO) like '[NAME_ID,%'");

		sql.append(sql_where_e);

		sql.append(" ), ");
		sql.append("q3 as ( ");
		sql.append(" select e.id,e.md5_code,e.ruleid,e.situation,e.level_,e.targets,e.information,e.x,e.y,e.created,e.worker,n.name_id,n.name,n.name_phonetic,n.road_type,n.admin_id from q1 n ,q2 e "
				+ " where   e.target =  '[NAME_ID,' || n.name_id || ']'  ");
		sql.append(") ");

		// ************************
		sql.append(" SELECT A.*,(SELECT COUNT(1) FROM q3) AS TOTAL_RECORD_NUM_  "
				+ "FROM " + "(SELECT T.*, ROWNUM AS ROWNO FROM q3 T ");
		sql.append(" WHERE ROWNUM <= " + pageEndNum + ") A "
				+ "WHERE A.ROWNO >= " + pageStartNum + " ");

		log.info("listCheckResultsBytaskName sql:  " + sql.toString());

		QueryRunner run = new QueryRunner();

		// ****************************************
		ResultSetHandler<Page> rsHandler3 = new ResultSetHandler<Page>() {
			public Page handle(ResultSet rs) throws SQLException {
				Page page = new Page();
				int total = 0;
				JSONArray results = new JSONArray();
				while (rs.next()) {
					if (total == 0) {
						total = rs.getInt("TOTAL_RECORD_NUM_");
					}

					JSONObject json = new JSONObject();

					json.put("id", rs.getString("id"));

					json.put("md5_code", rs.getString("md5_code"));

					json.put("ruleid", rs.getString("ruleid"));

					json.put("situation", rs.getString("situation"));

					json.put("rank", rs.getInt("level_"));

					json.put("information", rs.getString("information"));

					json.put("create_date", rs.getString("created"));

					json.put("targets", rs.getString("targets"));

					json.put("nameId", rs.getInt("name_id"));
					json.put("name", rs.getString("name"));
					json.put("namePhonetic", rs.getString("name_phonetic"));
					json.put("roadType", rs.getInt("road_type"));
					int adminId = rs.getInt("admin_id");
					if (adminId == 214) {
						json.put("adminName", "全国");
					} else {
						if (!adminMap.isEmpty()) {
							if (adminMap.containsKey(String.valueOf(adminId))) {
								json.put("adminName",
										adminMap.get(String.valueOf(adminId)));
							} else {
								json.put("adminName", "");
							}
						}
					}
					results.add(json);
				}
				page.setTotalCount(total);
				page.setResult(results);
				return page;
			}
		};
		Page p = run.query(conn, sql.toString(), rsHandler3);
		return p;
	}

	public JSONArray listCheckResultsRuleIds(JSONObject params) {
		JSONArray jobRuleObjs = null;
		try {

			String taskName = params.getString("taskName");

			QueryRunner run = new QueryRunner();

			String jobInfoSql = "select e.ruleid,count(1) numb  from ni_val_exception e  where e.task_name = '"
					+ taskName + "' group by(ruleid) ";

			jobRuleObjs = run.query(conn, jobInfoSql,
					new ResultSetHandler<JSONArray>() {

						@Override
						public JSONArray handle(ResultSet rs)
								throws SQLException {
							JSONArray jobRuleArr = new JSONArray();
							while (rs.next()) {
								JSONObject jobRuleObj = new JSONObject();
								jobRuleObj.put("ruleId", rs.getString("ruleid"));
								jobRuleArr.add(jobRuleObj);
							}
							return jobRuleArr;
						}

					});
			return jobRuleObjs;

		} catch (SQLException e) {
			e.printStackTrace();
			return jobRuleObjs;
		}
	}

	public JSONArray checkResultsStatis(String taskName, List<String> groupList) {
		JSONArray jobRuleObjs = null;
		try {
			QueryRunner run = new QueryRunner();
			String columSql = "";
			String groupBy = "";
			if (groupList.contains("rule")) {
				if (StringUtils.isNotEmpty(groupBy)) {
					groupBy += " , ";
				}
				columSql += ",ruleid";
				groupBy += "ruleid ";
			}
			if (groupList.contains("information")) {
				if (StringUtils.isNotEmpty(groupBy)) {
					groupBy += " , ";
				}
				columSql += ",information";
				groupBy += "information ";
			}
			if (groupList.contains("adminName")) {
				if (StringUtils.isNotEmpty(groupBy)) {
					groupBy += " , ";
				}
				columSql += ",admin_id";
				groupBy += "admin_id ";
			}
			if (groupList.contains("level")) {
				if (StringUtils.isNotEmpty(groupBy)) {
					groupBy += " , ";
				}
				columSql += ",\"LEVEL\" level_";
				groupBy += "\"LEVEL\" ";

			}

			String sql = "select count(1) numb " + columSql
					+ "   from ni_val_exception   where task_name = '"
					+ taskName + "' ";
			String sql_adminId = " with  ";
			// sql_adminId +=
			// " select '[NAME_ID,'||r.name_id||']' targets,r.admin_id  from rd_name r where 1=1 ), ";
			sql_adminId += " q2 as ( "
					+ " select NVL(d.ruleid,0) ruleid,\"LEVEL\" , "
					// + "NVL(to_char(d.addition_info),'') targets,"
					+ "substr(to_char(d.ADDITION_INFO),1,instr(to_char(d.ADDITION_INFO),']')) target,"
					+ "NVL(d.information,'') information "
					+ " from ni_val_exception d  " + " where d.task_name = '"
					+ taskName
					+ "' and to_char(d.ADDITION_INFO) like '[NAME_ID,%' "
					+ "), ";
			sql_adminId += " q3 as ( select e.*,n.admin_id from rd_name n, q2 e "
					+ " where   e.target =  '[NAME_ID,' || n.name_id || ']' ) ";

			sql_adminId += "  select count(1) numb" + columSql + " from q3 q  ";
			if (StringUtils.isNotEmpty(groupBy)) {

				sql += "group by(  ";
				sql += groupBy;
				sql += " ) ";

				sql_adminId += "group by(  ";
				sql_adminId += groupBy;
				sql_adminId += " ) ";
			}
			log.info("sql : " + sql);
			log.info("sql_adminId : " + sql_adminId);
			if (groupList.contains("adminName")) {
				jobRuleObjs = run.query(conn, sql_adminId,
						new ResultSetHandler<JSONArray>() {
							@Override
							public JSONArray handle(ResultSet rs)
									throws SQLException {
								JSONArray jobRuleArr = new JSONArray();
								ResultSetMetaData rsmd = rs.getMetaData();
								int columnCount = rsmd.getColumnCount();
								log.info(columnCount);
								List<String> columns = new ArrayList<String>();
								for (int i = 1; i <= columnCount; i++) {
									columns.add(rsmd.getColumnName(i));
								}
								while (rs.next()) {
									JSONObject jobRuleObj = new JSONObject();
									jobRuleObj.put("count", rs.getInt("numb"));

									if (columns.contains("RULEID")) {
										jobRuleObj.put("ruleid",
												rs.getString("RULEID"));
									}
									if (columns.contains("INFORMATION")) {
										jobRuleObj.put("information",
												rs.getString("information"));
									}
									if (columns.contains("ADMIN_ID")) {
										jobRuleObj.put("admin_id",
												rs.getInt("admin_id"));

									}
									if (columns.contains("LEVEL_")) {
										jobRuleObj.put("level",
												rs.getInt("level_"));
									}

									jobRuleArr.add(jobRuleObj);
								}
								return jobRuleArr;
							}

						});
			} else {
				jobRuleObjs = run.query(conn, sql,
						new ResultSetHandler<JSONArray>() {
							@Override
							public JSONArray handle(ResultSet rs)
									throws SQLException {
								JSONArray jobRuleArr = new JSONArray();
								ResultSetMetaData rsmd = rs.getMetaData();
								int columnCount = rsmd.getColumnCount();
								List<String> columns = new ArrayList<String>();
								for (int i = 1; i <= columnCount; i++) {
									columns.add(rsmd.getColumnName(i));
								}
								while (rs.next()) {
									JSONObject jobRuleObj = new JSONObject();
									jobRuleObj.put("count", rs.getInt("numb"));

									if (columns.contains("RULEID")) {
										jobRuleObj.put("ruleid",
												rs.getString("RULEID"));
									}
									if (columns.contains("INFORMATION")) {
										jobRuleObj.put("information",
												rs.getString("information"));
									}

									if (columns.contains("LEVEL_")) {
										jobRuleObj.put("level",
												rs.getInt("level_"));
									}

									jobRuleArr.add(jobRuleObj);
								}
								return jobRuleArr;
							}

						});
			}
			return jobRuleObjs;

		} catch (SQLException e) {
			e.printStackTrace();
			return jobRuleObjs;
		}
	}
	
	/**
	 * 根据operationName获取规则列表
	 * @param operationName
	 * @return
	 * @throws Exception
	 */
	public List<String> loadByOperationName(String operationName) throws Exception {

					try {
						String sql="SELECT OPERATION_CODE, CHECK_ID FROM CHECK_OPERATION_PLUS WHERE OPERATION_CODE=?";
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,operationName);
							rs = pstmt.executeQuery();
							List<String> ruleList=new ArrayList<String>();
							while (rs.next()) {
								ruleList.add(rs.getString("CHECK_ID"));		
							} 
							return ruleList;
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.closeQuietly(rs);
							DbUtils.closeQuietly(pstmt);
							DbUtils.commitAndCloseQuietly(conn);
						}
					} catch (Exception e) {
						throw new SQLException("获取检查规则"+operationName+"失败："
								+ e.getMessage(), e);
					}
				}

	/**
	 * 获取月編规则号
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public List<String> getColumnCheckRules(String type) throws Exception{
				
		List<String> deepCheckRules = new ArrayList<String>();
		String sql = "select work_item_id from POI_COLUMN_WORKITEM_CONF where first_work_item='"+type+"'";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {			
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				deepCheckRules.add(resultSet.getString("work_item_id"));
			}
			
			return deepCheckRules;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	/**
	 * @Title: poiCheckResultList，根据检查规则过滤
	 * @Description: 根据 pid 查询 exception
	 * @return
	 * @throws Exception
	 * @throws
	 * @author add by update by jch
	 * @date 2017年7月18日
	 */
	public JSONArray poiCheckResultList(int pid,List<String> ckRuleList) throws Exception {
		String ckRules = "('";
		ckRules += StringUtils.join(ckRuleList.toArray(), "','") + "')";
		StringBuilder sql = new StringBuilder(
				"select q.*,"
						+ pid
						+ " pid from ( "
						+ "select a.md5_code,a.ruleid,a.\"LEVEL\" level_,a.targets,a.information,a.worker ,a.created,a.location.sdo_point.x x,a.location.sdo_point.y y,a.updated,a.qa_worker,a.qa_status from ni_val_exception a where  a.ruleid in "+ckRules+" and "
						+ " EXISTS ( SELECT 1 FROM CK_RESULT_OBJECT O WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')  AND O.MD5_CODE=a.MD5_CODE "
						+ " and o.pid="
						+ pid
						+ "  "
						+ ") "
						+ " union all "
						+ "select c.md5_code,c.rule_id ruleid,c.status level_,c.targets,c.information,c.worker ,c.create_date created,(sdo_util.from_wktgeometry(c.geometry)).sdo_point.x x,(sdo_util.from_wktgeometry(c.geometry)).sdo_point.y y,c.update_date as updated,c.qa_worker,c.qa_status from ck_exception c where c.rule_id in "+ckRules+" and "
						+ " EXISTS ( SELECT 1 FROM CK_RESULT_OBJECT O WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')  AND O.MD5_CODE=c.MD5_CODE "
						+ " and o.pid=" + pid + " " + " )  " + " )  q ");

		sql.append("  order by q.created desc,q.md5_code desc ");

		log.info("poiCheckResultList:  " + sql);
		return new QueryRunner().query(conn, sql.toString(),
				new ResultSetHandler<JSONArray>() {

					@Override
					public JSONArray handle(ResultSet rs) throws SQLException {

						JSONArray results = new JSONArray();
						while (rs.next()) {

							JSONObject json = new JSONObject();

							json.put("id", rs.getString("md5_code"));

							json.put("ruleid", rs.getString("ruleid"));

							// json.put("situation", rs.getString("situation"));

							json.put("rank", rs.getInt("level_"));

							String targets = "";
							if (rs.getString("targets") != null
									&& StringUtils.isNotEmpty(rs
											.getString("targets"))) {
								targets = rs.getString("targets");
							}
							json.put("targets", targets);

							json.put("information", rs.getString("information"));

							json.put("geometry", "(" + rs.getDouble("x") + ","
									+ rs.getDouble("y") + ")");

							json.put("create_date", rs.getString("created"));
							json.put("update_date", rs.getString("updated"));

							json.put("worker", rs.getString("worker"));
							json.put(
									"qa_worker",
									rs.getString("qa_worker") == null ? "" : rs
											.getString("qa_worker"));
							json.put("qa_status", rs.getString("qa_status"));

							JSONArray refFeaturesArr = new JSONArray();
							// int refPoiCount = 0;

							if (targets != null
									&& StringUtils.isNotEmpty(targets)) {

								String pids = targets
										.replaceAll("[\\[\\]]", "")
										.replaceAll("IX_POI,", "")
										.replaceAll(";", ",");
								refFeaturesArr = queryRefFeatures(pids,
										rs.getInt("pid"));
							}
							// 查询关联poi根据pid
							json.put("refFeatures", refFeaturesArr);
							json.put("refCount", refFeaturesArr.size());

							results.add(json);
						}
						return results;
					}
				});
	}
	
}
