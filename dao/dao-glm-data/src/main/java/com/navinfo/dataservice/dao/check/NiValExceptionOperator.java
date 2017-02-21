package com.navinfo.dataservice.dao.check;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import oracle.spatial.geometry.JGeometry;

import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

public class NiValExceptionOperator {

	private Connection conn;
	private String gdbVersion;
	private Map<String, Geometry> geometryMap = new HashMap<String, Geometry>();
	private Map<String, Integer> meshMap = new HashMap<String, Integer>();
	private CkObjectNodeLoader objectNodeLoader = CkObjectNodeLoader
			.getInstance();
	private static Logger logg = Logger.getLogger(NiValExceptionOperator.class);

	public NiValExceptionOperator() {

	}

	public NiValExceptionOperator(Connection conn) throws Exception {
		this.conn = conn;
		this.gdbVersion = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.gdbVersion);

		// ProjectSelector selector = new ProjectSelector();

		// this.gdbVersion = selector.getGdbVersion(projectId);
	}

	public void insertCheckLogGrid(String md5, String loc) throws Exception {

		Glm glm = GlmCache.getInstance().getGlm(gdbVersion);

		GlmGridCalculator calculator = GlmGridCalculatorFactory.getInstance()
				.create(gdbVersion);

		String insertSql = "INSERT INTO NI_VAL_EXCEPTION_GRID (md5_code,GRID_ID) VALUES (?,?)";

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insertSql);
			JGeometry geo = GeoTranslator.wkt2JGrometry(loc);
			String[] grids = CompGridUtil.point2Grids(geo.getPoint()[0],
					geo.getPoint()[1]);
			if (grids != null) {
				for (String grid : grids) {
					stmt.setString(1, md5);
					stmt.setLong(2, Long.valueOf(grid));
					stmt.addBatch();
				}
			}
			stmt.executeBatch();
			stmt.clearBatch();

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {

			}
		}
	}

	private List<Object> calGeometryAndMesh(String tableName, String pid)
			throws Exception {
		List<Object> list = new ArrayList<Object>();
		String key = tableName + ":" + pid;
		if (geometryMap.containsKey(key)) {
			list.add(geometryMap.get(key));
			list.add(meshMap.get(key));
		}
		if (list.size() == 0) {
			CkObjectNode objectNode = objectNodeLoader.getObjectNode(tableName);
			if (objectNode.getMeshTable().equals("RD_LINK")) {
				RdLinkSelector linkSelector = new RdLinkSelector(conn);
				String sql = objectNode.getMeshSql();
				sql = sql.replace("!OBJECT_PID!", pid);
				RdLink link = linkSelector.loadBySql(sql, false).get(0);
				geometryMap.put(key, GeometryUtils
						.getPointFromGeo(GeoTranslator.transform(
								link.getGeometry(), 0.00001, 5)));
				meshMap.put(key, link.mesh());
			}
			if (objectNode.getMeshTable().equals("RD_NODE")) {
				RdNodeSelector nodeSelector = new RdNodeSelector(conn);
				String sql = objectNode.getMeshSql();
				sql = sql.replace("!OBJECT_PID!", pid);
				RdNode node = nodeSelector.loadBySql(sql, false).get(0);
				Geometry geo = node.getGeometry();
				geometryMap.put(key, node.getGeometry());
				meshMap.put(
						key,
						Integer.valueOf(MeshUtils.point2Meshes(
								geo.getCoordinate().x * 0.00001,
								geo.getCoordinate().y * 0.00001)[0]));
			}
			if (objectNode.getMeshTable().equals("IX_POI")) {
				IxPoiSelector poiSelector = new IxPoiSelector(conn);
				String sql = objectNode.getMeshSql();
				sql = sql.replace("!OBJECT_PID!", pid);
				IxPoi node = (IxPoi) poiSelector.loadBySql(sql, false, false)
						.get(0);
				Geometry geo = node.getGeometry();
				geometryMap.put(key, node.getGeometry());
				meshMap.put(
						key,
						Integer.valueOf(MeshUtils.point2Meshes(
								geo.getCoordinate().x * 0.00001,
								geo.getCoordinate().y * 0.00001)[0]));
			}
			list.add(geometryMap.get(key));
			list.add(meshMap.get(key));
		}
		return list;
	}

	private List<Object> calGeoAndMeshWithTarget(String targets)
			throws Exception {
		List<Object> list = null;
		String value = StringUtils.removeBlankChar(targets);
		if (value != null && value.length() > 2) {
			String subValue = value.substring(1, value.length() - 1);
			for (String table : subValue.split("\\];\\[")) {
				String[] arr = table.split(",");
				list = calGeometryAndMesh(arr[0], arr[1]);
				if (list != null && list.size() != 0) {
					return list;
				}
			}
		}
		return list;
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String worker) throws Exception {
		logg.debug("start insert ni_val:1");
		if (loc == null || loc.isEmpty()) {
			List<Object> list = calGeoAndMeshWithTarget(targets);
			loc = GeoTranslator.jts2Wkt((Geometry) list.get(0), 0.00001, 5);
			meshId = (int) list.get(1);
		}
		logg.debug("start insert ni_val:2");
		String sql = "merge into ni_val_exception a using ( select * from ( select :1 as MD5_CODE from dual) where MD5_CODE not in ( select MD5_CODE          from ni_val_exception          where MD5_CODE is not null        union all        select RESERVED as MDS_CODE          from ck_exception          where RESERVED is not null          )) b on (a.MD5_CODE = b.MD5_CODE) when not matched then   insert     (MD5_CODE, ruleid, information, location, targets, mesh_id, worker, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, sysdate, sysdate)";
		PreparedStatement pstmt = conn.prepareStatement(sql);

		try {
			String md5 = this.generateMd5(ruleId,
					CheckItems.getInforByRuleId(ruleId), targets, null);

			pstmt.setString(1, md5);

			pstmt.setString(2, md5);

			pstmt.setString(3, ruleId);

			pstmt.setString(4, CheckItems.getInforByRuleId(ruleId));

			pstmt.setString(5, loc);

			pstmt.setString(6, targets);

			pstmt.setInt(7, meshId);

			pstmt.setString(8, worker);

			pstmt.setInt(9, 1);

			int res = pstmt.executeUpdate();

			logg.debug("start insert ni_val:3");

			if (res > 0) {

				CkResultObjectOperator op = new CkResultObjectOperator(conn);

				op.insertCkResultObject(md5, targets);
				logg.debug("start insert ni_val:3-1");

				this.insertCheckLogGrid(md5, loc);
				logg.debug("start insert ni_val:3-2");
			}

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String log, String worker) throws Exception {
		insertCheckLog(ruleId, loc, targets, meshId, log, 1, worker);
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String log, int logLevel, String worker)
			throws Exception {
		logg.debug("start insert ni_val:1");
		if (loc == null || loc.isEmpty()) {
			List<Object> list = calGeoAndMeshWithTarget(targets);
			loc = GeoTranslator.jts2Wkt((Geometry) list.get(0), 0.00001, 5);
			meshId = (int) list.get(1);
		}
		logg.debug("start insert ni_val:2");
		String md5Sql = "with t as(SELECT LOWER(UTL_RAW.CAST_TO_RAW(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING =>?||?||?||?))) "
				+ "AS MD5_CODE FROM DUAL) "
				+ "select md5_code from t minus "
				+ "(SELECT N.MD5_CODE FROM NI_VAL_EXCEPTION N,t WHERE t.MD5_CODE=N.MD5_CODE "
				+ "union all SELECT C.MD5_CODE FROM CK_EXCEPTION C,t WHERE t.MD5_CODE=C.MD5_CODE )";
		// String md5 = this.generateMd5(ruleId, log, targets, null);
		// String sql =
		// "merge into ni_val_exception a using (select :1 as MD5_CODE from dual) b on (a.MD5_CODE = b.MD5_CODE) when not matched then   insert (MD5_CODE, ruleid, information, location, targets, mesh_id, worker, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, sysdate, sysdate)";
		logg.debug("start insert ni_val:2-1");
		// String cSql =
		// "SELECT 1 FROM NI_VAL_EXCEPTION WHERE MD5_CODE=? UNION SELECT 1 FROM CK_EXCEPTION WHERE MD5_CODE=?";
		String md5 = new QueryRunner().queryForString(conn, md5Sql, ruleId,
				log, targets, "null");

		if (StringUtils.isEmpty(md5))
			return;
		logg.debug("start insert ni_val:2-2");
		String sql = "insert into ni_val_exception(MD5_CODE, ruleid, information, location, targets, mesh_id, worker, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, sysdate, sysdate)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		logg.debug("start insert ni_val:2-3");
		try {
			logg.debug("start insert ni_val:2-4");
			pstmt.setString(1, md5);
			logg.debug("start insert ni_val:2-5");
			pstmt.setString(2, ruleId);
			logg.debug("start insert ni_val:2-6");
			pstmt.setString(3, log);
			logg.debug("start insert ni_val:2-7");

			pstmt.setString(4, loc);
			logg.debug("start insert ni_val:2-8");
			pstmt.setString(5, targets);
			logg.debug("start insert ni_val:2-9");
			pstmt.setInt(6, meshId);
			logg.debug("start insert ni_val:2-10");
			pstmt.setString(7, worker);
			logg.debug("start insert ni_val:2-11");
			pstmt.setInt(8, logLevel);
			logg.debug("start insert ni_val:2-12");
			int res = pstmt.executeUpdate();
			logg.debug("start insert ni_val:3");

			if (res > 0) {

				CkResultObjectOperator op = new CkResultObjectOperator(conn);

				op.insertCkResultObject(md5, targets);
				logg.debug("start insert ni_val:3-1");

				this.insertCheckLogGrid(md5, loc);
				logg.debug("start insert ni_val:3-2");
			}

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

	public void deleteNiValException(String tableName, int pid)
			throws Exception {

		String sql = "delete from ni_val_exception a where exists (select null from ck_result_object b where a.MD5_CODE=b.md5_code and b.table_name=? and b.pid=?)";

		PreparedStatement pstmt = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();

			pstmt.close();

			sql = "delete from NI_VAL_EXCEPTION_GRID a where exists (select null from ck_result_object b where a.md5_code=b.md5_code and b.table_name=? and b.pid=?)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();

			pstmt.close();

			sql = "delete from ck_result_object a where a.md5_code in (select b.md5_code from ck_result_object b where b.table_name=? and b.pid=?)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

	/**
	 * 修改检查结果状态为例外、确认已修改、确认不修改 确认已修改进ni_val_exception_history表
	 * 例外、确认不修改就ck_exception表
	 * 
	 * @param md5
	 * @param projectId
	 * @param type
	 *            1例外，2确认不修改，3确认已修改
	 * @throws Exception
	 */
	public void updateCheckLogStatusForRd(String md5, int type) throws Exception {

		conn.setAutoCommit(false);

		PreparedStatement pstmt = null;

		try {

			Result result = null;

			String sql = "";

			if (type == 3) {
				sql = "delete from ni_val_exception a where a.MD5_CODE=:1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, md5);
				pstmt.executeUpdate();
				pstmt.close();

				sql = "delete from ni_val_exception_grid a where a.MD5_CODE=:1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, md5);
				pstmt.executeUpdate();
				pstmt.close();

				sql = "delete from ck_result_object a where a.MD5_CODE=:1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, md5);
				pstmt.executeUpdate();
				pstmt.close();
			} else {

				NiValExceptionSelector selector = new NiValExceptionSelector(
						conn);

				NiValException exception = selector.loadById(md5, false);

				CkException ckexception = new CkException();

				ckexception.copy(exception);

				int pid = PidUtil.getInstance().applyCkExceptionId();

				ckexception.setExceptionId(pid);

				ckexception.setStatus(type);

				ckexception.setRowId(UuidUtils.genUuid());

				sql = "insert into ck_exception(exception_id, rule_id, task_name, status, group_id, rank, situation, information, suggestion, geometry, targets, addition_info, memo, create_date, update_date, mesh_id, scope_flag, province_name, map_scale, MD5_CODE, extended, task_id, qa_task_id, qa_status, worker, qa_worker, row_id, u_record) select :1,ruleid, task_name,:2,groupid, \"LEVEL\" level_, situation, information, suggestion,sdo_util.to_wktgeometry(location), targets, addition_info, '',created, updated, mesh_id, scope_flag, province_name, map_scale, MD5_CODE, extended, task_id, qa_task_id, qa_status, worker, qa_worker,:3,1 from ni_val_exception a where a.MD5_CODE=:4";

				pstmt = conn.prepareStatement(sql);

				pstmt.setInt(1, pid);

				pstmt.setInt(2, type);

				pstmt.setString(3, ckexception.rowId());

				pstmt.setString(4, md5);

				pstmt.executeUpdate();

				pstmt.close();

				sql = "insert into ck_exception_grid select :1,grid_id from NI_VAL_EXCEPTION_GRID where md5_code=:2";

				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, ckexception.rowId());

				pstmt.setString(2, md5);

				pstmt.executeUpdate();

				pstmt.close();

				result = new Result();

				result.insertObject(ckexception, ObjStatus.INSERT,
						ckexception.getExceptionId());

			}

			sql = "delete from ni_val_exception where MD5_CODE=:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, md5);

			pstmt.executeUpdate();

			pstmt.close();

			sql = "delete from ck_result_object where md5_code=:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, md5);

			pstmt.executeUpdate();

			pstmt.close();

			sql = "delete from NI_VAL_EXCEPTION_GRID where md5_code=:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, md5);

			pstmt.executeUpdate();

			if (result != null) {
				LogWriter writer = new LogWriter(conn);

				Command command = new Command();

				writer.generateLog(command, result);

				writer.recordLog(command, result);
			}

			conn.commit();

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * 修改检查结果状态为例外、确认已修改、确认不修改 确认已修改进ni_val_exception_history表
	 * 例外、确认不修改就ck_exception表
	 * 
	 * @param md5
	 * @param projectId
	 * @param type
	 *            0 未修改 1例外，2确认不修改，3确认已修改
	 * @throws Exception
	 */
	public void updateCheckLogStatus(String md5, int oldType, int type)
			throws Exception {

		conn.setAutoCommit(false);
		try {

			if (oldType == 0) {
				if (type == 3) {
					// 新增his历史信息 确认已修改
					this.insertForException(md5, 1);
				} else {
					// 转例外
					this.insertForCkException(md5, type, 1);

				}
				// 删除结果信息
				this.delValException(md5);
				this.delCkResultObj(md5);
				this.delValExceptionGrid(md5);

			} else if (oldType == 3) {
				if (type == 0) {
					this.insertForException(md5, 0);
				} else {
					this.insertForCkException(md5, type, 0);

				}
				this.delValExceptionHis(md5);
				this.delValExceptionGridHis(md5);

			} else {
				if (type == 0) {
					// 删除例外信息 0 处理exception信息
					this.delForCkException(md5, 2);

				}
				if (type == 1||type ==2) {
					// 更新例外信息
					this.updateForCkException(md5, type);
				}
				if (type == 3) {
					// 删除例外信息 1处理his 信息
					this.delForCkException(md5, 3);
				}
			}
			
			conn.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {

			}
		}
	}

	/***
	 * 新增检查结果相关信息
	 * 
	 * @param tableFlag
	 *            0是 ni_val_exception 1是ni_val_exception_history
	 * @param md5
	 * @throws Exception
	 */
	private void insertForException(String md5, int tableFlag) throws Exception {
		if (tableFlag == 0) {
			String sqlExpHis = "insert into ni_val_exception select * from ni_val_exception_history a where a.MD5_CODE = ?";
			this.insertNiValException(md5, sqlExpHis);
			String sqlExpGridHis = "insert into ni_val_exception_grid select * from ni_val_exception_history_grid a where a.MD5_CODE = ?";
			this.insertNiValExceptionGrid(md5, sqlExpGridHis);
		}
		if (tableFlag == 1) {

			String sqlExp = "insert into ni_val_exception_history select * from ni_val_exception a where a.MD5_CODE = ?";
			this.insertNiValExceptionHistory(md5, sqlExp);
			String sqlExpGrid = "insert into ni_val_exception_history_grid select * from ni_val_exception_grid a where a.MD5_CODE = ?";
			this.insertNiValExceptionHistoryGrid(md5, sqlExpGrid);
		}
		if(tableFlag == 2){
			String sqlExpHis = " INSERT INTO ni_val_exception (ruleid,task_name,groupid,\"LEVEL\",situation,information,suggestion,location,targets,addition_info,created,updated,mesh_id,scope_flag,province_name,map_scale,extended,task_id,qa_task_id,qa_status,worker,qa_worker,md5_code) SELECT rule_id,task_name,group_id,status, situation, information, suggestion,sdo_util.from_wktgeometry(geometry), targets, addition_info,create_date, update_date, mesh_id, scope_flag, province_name, map_scale, extended, task_id, qa_task_id, qa_status, worker, qa_worker,md5_code from ck_exception a where a.MD5_CODE=?";
			this.insertNiValException(md5, sqlExpHis);
			String sqlExpGridHis = "insert into ni_val_exception_grid select ce.md5_code,cg.grid_id from ck_exception_grid cg,ck_exception ce where cg.ck_row_id = ce.row_id and  md5_code=?";
			this.insertNiValExceptionGrid(md5, sqlExpGridHis);
		}
		if(tableFlag == 3){
			String sqlExpHis = " INSERT INTO ni_val_exception_history (ruleid,task_name,groupid,\"LEVEL\",situation,information,suggestion,location,targets,addition_info,created,updated,mesh_id,scope_flag,province_name,map_scale,extended,task_id,qa_task_id,qa_status,worker,qa_worker,md5_code) SELECT rule_id,task_name,group_id,status, situation, information, suggestion,sdo_util.from_wktgeometry(geometry), targets, addition_info,create_date, update_date, mesh_id, scope_flag, province_name, map_scale, extended, task_id, qa_task_id, qa_status, worker, qa_worker,md5_code from ck_exception a where a.MD5_CODE=:4";
			this.insertNiValException(md5, sqlExpHis);
			String sqlExpGridHis = "insert into ni_val_exception_history_grid select ce.md5_code,cg.grid_id from ck_exception_grid cg,ck_exception ce where cg.ck_row_id = ce.row_id and  md5_code=?";
						this.insertNiValExceptionGrid(md5, sqlExpGridHis);
		}
	}

	/**
	 * 例外表删除处理逻辑
	 * 
	 * @param md5
	 * @param tableFlag
	 * @param result
	 * @throws Exception
	 */
	private void delForCkException(String md5, int tableFlag)
			throws Exception {
		CkExceptionSelector selector = new CkExceptionSelector(conn);
		CkException ckexception = selector.loadById(md5, false);
		this.insertForException(md5, tableFlag);
		// 删除ck_exception 插入到ni_val_exception
		this.delCkExceptionGrid(md5);
		this.delCkException(md5);
		/*Result result = new Result();
		result.insertObject(ckexception, ObjStatus.DELETE,
				ckexception.getExceptionId());
		this.recordLogForCkException(result,OperType.DELETE);*/
	}

	/***
	 * 新增例外表相关信息
	 * 
	 * @param tableFlag
	 *            0是 ni_val_exception 1是ni_val_exception_history
	 * @param md5
	 * @param type
	 * @param result
	 * @throws Exception
	 */
	private void insertForCkException(String md5, int type, int tableFlag) throws Exception {
		NiValExceptionHistory exceptionHis = null;
		NiValException exception = null;
		CkException ckexception = new CkException();
		if (tableFlag == 0) {
			NiValExceptionHistorySelector selector = new NiValExceptionHistorySelector(
					conn);

			exceptionHis = selector.loadById(md5, false);
			ckexception.copy(exceptionHis);
		}
		if (tableFlag == 1) {
			NiValExceptionSelector selector = new NiValExceptionSelector(conn);
			exception = selector.loadById(md5, false);
			ckexception.copy(exception);
		}

		int pid = PidUtil.getInstance().applyCkExceptionId();
		ckexception.setExceptionId(pid);
		ckexception.setStatus(type);
		ckexception.setRowId(UuidUtils.genUuid());
		this.insertCkException(pid, type, ckexception.rowId(), md5,tableFlag);
		this.insertCkExceptionGrid(ckexception.rowId(), md5,tableFlag);
	/*	Result result = new Result();
		result.insertObject(ckexception, ObjStatus.INSERT,
				ckexception.getExceptionId());
		this.recordLogForCkException(result,OperType.CREATE);*/
	}

	/**
	 * zhaokk 增加检查历史信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertNiValException(String md5, String sql) throws Exception {

		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("增加检查信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 增加检查历史GRID信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertNiValExceptionGrid(String md5, String sql)
			throws Exception {

		// String sql =
		// "insert into ni_val_exception_grid select * from ni_val_exception_history_grid a where a.MD5_CODE = ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("增加检查GRID信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 增加检查历史信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertNiValExceptionHistory(String md5, String sql)
			throws Exception {

		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("增加检查历史信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 增加检查历史GRID信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertNiValExceptionHistoryGrid(String md5, String sql)
			throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("增加检查历史GRID信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 增加例外检查信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertCkException(int pid, int type, String rowId, String md5,int tableFlag)
			throws Exception {
		String tableName= "";
        if (tableFlag == 1){
        	tableName = "ni_val_exception";
        }
        if(tableFlag == 0){
        	tableName = "ni_val_exception_history";
        }
		String sql = "insert into ck_exception(exception_id, rule_id, task_name, status, group_id, rank, situation, information, suggestion, geometry, targets, addition_info, memo, create_date, update_date, mesh_id, scope_flag, province_name, map_scale, MD5_CODE, extended, task_id, qa_task_id, qa_status, worker, qa_worker, row_id, u_record) select ?,ruleid, task_name,?,groupid, \"LEVEL\" level_, situation, information, suggestion,sdo_util.to_wktgeometry(location), targets, addition_info, '',created, updated, mesh_id, scope_flag, province_name, map_scale, MD5_CODE, extended, task_id, qa_task_id, qa_status, worker, qa_worker,?,1 from " +tableName+ " a where a.MD5_CODE= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, pid, type, rowId, md5);
		} catch (Exception e) {
			throw new Exception("增加检查历史信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 增加例外检查信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertCkExceptionGrid(String rowId, String md5,int tableFlag)
			throws Exception {
		String tableName= "";
        if (tableFlag == 1){
        	tableName = "ni_val_exception_grid";
        }
        if(tableFlag == 0){
        	tableName = "ni_val_exception_history_grid";
        }
		String sql = "insert into ck_exception_grid select ?,grid_id from " +tableName+ " where md5_code=?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, rowId, md5);
		} catch (Exception e) {
			throw new Exception("增加检查历史信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除检查结果信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delValException(String md5) throws Exception {

		String sql = "delete from ni_val_exception where MD5_CODE= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除检查结果信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除检查结果GRID信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delValExceptionGrid(String md5) throws Exception {
		String sql = "delete from ni_val_exception_grid where MD5_CODE= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除检查结果GRID信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除检查结果OBJ信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delCkResultObj(String md5) throws Exception {
		String sql = "delete from ck_result_object where MD5_CODE= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除检查结果OBJ信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除检查结果历史信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delValExceptionHis(String md5) throws Exception {

		String sql = "delete from ni_val_exception_history where MD5_CODE= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除检查结果历史信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除检查结果GRID历史信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delValExceptionGridHis(String md5) throws Exception {
		String sql = "delete from ni_val_exception_history_grid where MD5_CODE= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除检查结果GRID历史信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除例外信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delCkException(String md5) throws Exception {

		String sql = " delete from ck_exception where md5_code= ?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除例外信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除例外信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void delCkExceptionGrid(String md5) throws Exception {

		String sql = " delete from ck_exception_grid where ck_row_id in (select row_id from ck_exception where md5_code=?) ";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, md5);
		} catch (Exception e) {
			throw new Exception("删除例外GRID信息出错，" + e.getMessage(), e);
		}
	}

	/**
	 * zhaokk 删除例外信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void updateCkException(int type, String md5) throws Exception {

		String sql = " update ck_exception set status =? where  md5_code=?";
		try {
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, type, md5);
		} catch (Exception e) {
			throw new Exception("更新例外信息出错，" + e.getMessage(), e);
		}
	}

	private void updateForCkException(String md5, int type)
			throws Exception {
		CkExceptionSelector selector = new CkExceptionSelector(conn);
		CkException ckexception = selector.loadById(md5, false);
		this.updateCkException(type, md5);
		ckexception.changedFields().put("status", type);
		/*Result result = new Result();
		result.insertObject(ckexception, ObjStatus.UPDATE,
				ckexception.getExceptionId());
		this.recordLogForCkException(result,OperType.UPDATE);*/

	}

	/**
	 * Ck EXCEPTION 履历写入
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void recordLogForCkException(Result result,OperType operType) throws Exception {
		if (result != null) {
			LogWriter writer = new LogWriter(conn);

			Command command = new Command();
			command.setOperType(operType);
			writer.generateLog(command, result);

			writer.recordLog(command, result);
		}
	}

	private String generateMd5(String ruleId, String infor, String targets,
			String addInfo) {

		StringBuilder sb = new StringBuilder(ruleId);

		sb.append(infor);

		sb.append(targets);

		if (addInfo != null) {
			sb.append(addInfo);
		}

		return getMd5(sb.toString());

	}

	private static MessageDigest md5 = null;
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 用于获取一个String的md5值
	 * 
	 * @param string
	 * @return
	 */
	private static String getMd5(String str) {
		byte[] bs = md5.digest(str.getBytes());
		StringBuilder sb = new StringBuilder(40);
		for (byte x : bs) {
			if ((x & 0xff) >> 4 == 0) {
				sb.append("0").append(Integer.toHexString(x & 0xff));
			} else {
				sb.append(Integer.toHexString(x & 0xff));
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {

	}
}
