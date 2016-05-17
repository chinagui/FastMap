package com.navinfo.dataservice.dao.log;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.datahub.glm.GlmGridCalculator;
import com.navinfo.dataservice.datahub.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.engine.man.project.ProjectSelector;
import com.vividsolutions.jts.geom.Geometry;

class Status {
	public static int INSERT = 1;
	public static int DELETE = 2;
	public static int UPDATE = 3;
}

public class LogWriter {

	private Connection conn;

	private LogOperation geoLogOperation;// 修改拓扑相关的履历记一个operation

	private List<LogOperation> operations = new ArrayList<LogOperation>();

	private GlmGridCalculator gridCalculator;

	public LogWriter(Connection conn, int projectId) throws Exception {
		this.conn = conn;

		ProjectSelector selector = new ProjectSelector();

		String gdbVersion = selector.getGdbVersion(projectId);

		this.gridCalculator = GlmGridCalculatorFactory.getInstance().create(
				gdbVersion);
	}

	private void insertRow() throws Exception {

		PreparedStatement pstmt = null;

		String sql = "insert into log_operation (op_id, us_id, op_cmd, op_dt, op_sg, com_sta, lock_sta) values (?,?,?,to_date(?,'yyyymmddhh24miss'),?,?,?)";

		try {

			for (LogOperation logOperation : operations) {
				pstmt = this.conn.prepareStatement(sql);

				pstmt.setString(1, logOperation.getOpId());

				pstmt.setString(2, logOperation.getUsId());

				pstmt.setString(3, logOperation.getOpCmd());

				pstmt.setString(4, logOperation.getOpDt());

				pstmt.setInt(5, logOperation.getOpSg());
				
				pstmt.setInt(6, logOperation.getComSta());

				pstmt.setInt(7, logOperation.getLockSta());

				pstmt.execute();

				pstmt.close();

				for (LogDetail r : logOperation.getDetails()) {
					this.insertLogDetail(r);
				}
			}
		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

//	private void insertRow2Sql(Statement stmt, LogOperation logOperation)
//			throws Exception {
//
//		StringBuilder sb = new StringBuilder("insert into ");
//
//		sb.append(logOperation.tableName());
//
//		sb.append("(op_id, us_id, op_cmd, op_dt, op_sg) values (");
//
//		sb.append("'" + logOperation.getOpId() + "'");
//
//		if (logOperation.getUsId() == null) {
//			sb.append(",null");
//		} else {
//			sb.append(",'" + logOperation.getUsId() + "'");
//		}
//
//		if (logOperation.getOpCmd() == null) {
//			sb.append(",null");
//		} else {
//			sb.append(",'" + logOperation.getOpCmd() + "'");
//		}
//
//		if (logOperation.getOpDt() == null) {
//			sb.append(",null");
//		} else {
//			sb.append(",to_date('" + logOperation.getOpDt()
//					+ "','yyyymmddhh24miss')");
//		}
//
//		sb.append("," + logOperation.getOpSg());
//
//		sb.append(")");
//
//		stmt.addBatch(sb.toString());
//
//		for (LogDetail r : logOperation.getDetails()) {
//			this.insertLogDetail2Sql(r, stmt);
//		}
//	}

	private void insertLogDetail(LogDetail detail) throws Exception {

		PreparedStatement pstmt = null;

		String sql = "insert into log_detail (op_id, ob_nm, ob_pk, ob_pid, opb_tp, ob_tp, tb_nm, old, new, fd_lst, op_tp, row_id, is_ck,tb_row_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, detail.getOpId());

			pstmt.setString(2, detail.getObNm());

			pstmt.setString(3, detail.getObPk());

			pstmt.setInt(4, detail.getObPid());

			pstmt.setInt(5, detail.getOpbTp());

			pstmt.setInt(6, detail.getObTp());

			pstmt.setString(7, detail.getTbNm());

			Clob oldclob = conn.createClob();

			oldclob.setString(1, detail.getOldValue());

			if (oldclob instanceof com.alibaba.druid.proxy.jdbc.ClobProxyImpl) {
				com.alibaba.druid.proxy.jdbc.ClobProxyImpl impl = (com.alibaba.druid.proxy.jdbc.ClobProxyImpl) oldclob;
				oldclob = impl.getRawClob(); // 获取原生的这个 Clob
			}

			pstmt.setClob(8, oldclob);

			Clob newclob = conn.createClob();

			newclob.setString(1, detail.getNewValue());

			if (newclob instanceof com.alibaba.druid.proxy.jdbc.ClobProxyImpl) {
				com.alibaba.druid.proxy.jdbc.ClobProxyImpl impl = (com.alibaba.druid.proxy.jdbc.ClobProxyImpl) newclob;
				newclob = impl.getRawClob(); // 获取原生的这个 Clob
			}

			pstmt.setClob(9, newclob);

			pstmt.setString(10, detail.getFdLst());

			pstmt.setInt(11, detail.getOpTp());

			pstmt.setString(12, detail.getRowId());

			pstmt.setInt(13, detail.getIsCk());

			pstmt.setString(14, detail.getTbRowId());

			pstmt.execute();

			pstmt.close();

			for (LogDetailGrid r : detail.getGrids()) {
				this.insertLogDetailGrid(r);
			}

		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	private void insertLogDetailGrid(LogDetailGrid grid) throws Exception {

		PreparedStatement pstmt = null;

		String sql = "insert into log_detail_grid (log_row_id,grid_id,grid_type) values (?,?,?)";

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, grid.getLogRowId());

			pstmt.setInt(2, grid.getGridId());

			pstmt.setInt(3, grid.getGridType());

			pstmt.execute();

			pstmt.close();

		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

	}

	private void insertLogDetail2Sql(LogDetail detail, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(detail.tableName());

		sb.append("(op_id, ob_nm, ob_pk, ob_pid, opb_tp, ob_tp, tb_nm, old, new, fd_lst, op_tp, row_id, is_ck,tb_row_id) values (");

		sb.append("'" + detail.getOpId() + "'");

		if (detail.getObNm() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getObNm() + "'");
		}

		if (detail.getObPk() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getObPk() + "'");
		}

		sb.append("," + detail.getObPid());

		sb.append("," + detail.getOpbTp());

		sb.append("," + detail.getObTp());

		if (detail.getTbNm() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getTbNm() + "'");
		}

		if (detail.getOldValue() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getOldValue() + "'");
		}

		if (detail.getNewValue() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getNewValue() + "'");
		}

		if (detail.getFdLst() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getFdLst() + "'");
		}

		sb.append("," + detail.getOpTp());

		if (detail.getRowId() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getRowId() + "'");
		}

		sb.append("," + detail.getIsCk());

		sb.append(",hextoraw('" + detail.getTbRowId() + "')");

		sb.append(")");

		stmt.addBatch(sb.toString());
	}

	private void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	private void deleteRow2Sql(Statement stmt) throws Exception {

	}

	private boolean addToOperation(ICommand command, IRow r, LogDetail ld) {
		boolean flag = false;
		if (r.objType() == ObjType.RDNODE || r.objType() == ObjType.RDLINK
				|| r.objType() == ObjType.ADLINK
				|| r.objType() == ObjType.ADNODE|| r.objType() == ObjType.ADFACE) {

			if (r.status() == ObjStatus.INSERT
					|| r.status() == ObjStatus.DELETE) {
				flag = true;
			} else if (r.changedFields().containsKey("geometry")
					|| r.changedFields().containsKey("sNodePid")
					|| r.changedFields().containsKey("eNodePid")) {
				flag = true;
			}
		}

		if (flag) {
			ld.setOpId(geoLogOperation.getOpId());

			geoLogOperation.getDetails().add(ld);
		} else {
			LogOperation op = new LogOperation(UuidUtils.genUuid(), command
					.getOperType().toString(), 1);

			ld.setOpId(op.getOpId());

			op.getDetails().add(ld);

			operations.add(op);
		}
		
		return flag;
	}

	public void generateLog(ICommand command, Result result) throws Exception {

		geoLogOperation = new LogOperation(UuidUtils.genUuid(), command
				.getOperType().toString(), 1);

		// 处理修改的对象
		List<IRow> list = result.getUpdateObjects();

		for (IRow r : list) {
			LogDetail ld = new LogDetail();

			ld.setOpTp(Status.UPDATE);

			ld.setOpbTp(Status.UPDATE);

			ld.setObNm(r.parentTableName());

			ld.setObPid(r.parentPKValue());

			ld.setObPk(r.parentPKName());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setRowId(UuidUtils.genUuid());

			ld.setTbRowId(r.rowId());

			Set<Entry<String, Object>> set = r.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			JSONArray fieldList = new JSONArray();

			JSONObject oldValue = new JSONObject();

			JSONObject newValue = new JSONObject();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = r.getClass().getDeclaredField(column);

				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(r);

				fieldList.add(column);

				if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
							0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					oldValue.put(column, oldWkt);

					newValue.put(column, newWkt);

				}

				else {
					if (value instanceof String) {
						oldValue.put(column,
								(String.valueOf(value)).replace("'", "''"));
					} else {
						oldValue.put(column, value);
					}

					newValue.put(column, columnValue);
				}
			}

			ld.setFdLst(fieldList.toString());

			ld.setOldValue(oldValue.toString());

			ld.setNewValue(newValue.toString());

			// 查询对象的grid，并生成LogDetailGrid
			String[] gridIds = gridCalculator.calc(ld.getTbNm().toUpperCase(),
					ld.getTbRowId(), conn);

			for (String gridId : gridIds) {
				LogDetailGrid grid = new LogDetailGrid();

				grid.setGridId(Integer.valueOf(gridId));

				grid.setLogRowId(ld.getRowId());

				grid.setGridType(0);

				ld.getGrids().add(grid);
			}

			addToOperation(command, r, ld);

		}

		list = result.getDelObjects();

		for (IRow r : list) {
			LogDetail ld = new LogDetail();

			ld.setOpTp(Status.DELETE);

			if (r.parentTableName().equals(r.tableName())) {
				ld.setOpbTp(Status.DELETE);

				ld.setObTp(1);

			} else {
				ld.setOpbTp(Status.UPDATE);

				ld.setObTp(2);
			}

			ld.setObNm(r.parentTableName());

			ld.setObPid(r.parentPKValue());

			ld.setObPk(r.parentPKName());

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setRowId(UuidUtils.genUuid());

			ld.setTbRowId(r.rowId());

			// 查询对象的grid，并生成LogDetailGrid
			String[] gridIds = gridCalculator.calc(ld.getTbNm().toUpperCase(),
					ld.getTbRowId(), conn);

			for (String gridId : gridIds) {
				LogDetailGrid grid = new LogDetailGrid();

				grid.setGridId(Integer.valueOf(gridId));

				grid.setLogRowId(ld.getRowId());

				grid.setGridType(0);

				ld.getGrids().add(grid);
			}

			boolean flag = addToOperation(command, r, ld);

			List<List<IRow>> children = r.children();

			if (children != null) {
				for (List<IRow> list1 : children) {
					for (IRow row : list1) {
						LogDetail ldC = new LogDetail();

						ldC.setOpTp(Status.DELETE);

						ldC.setOpbTp(Status.DELETE);

						ldC.setObNm(row.parentTableName());

						ldC.setObPid(row.parentPKValue());

						ldC.setObPk(row.parentPKName());

						ldC.setObTp(2);

						ldC.setTbNm(row.tableName());

						ldC.setIsCk(0);

						ldC.setRowId(UuidUtils.genUuid());

						ldC.setTbRowId(row.rowId());

						// 查询对象的grid，并生成LogDetailGrid
						String[] grids = gridCalculator.calc(ldC.getTbNm()
								.toUpperCase(), ldC.getTbRowId(), conn);

						for (String gridId : grids) {
							LogDetailGrid grid = new LogDetailGrid();

							grid.setGridId(Integer.valueOf(gridId));

							grid.setLogRowId(ldC.getRowId());

							grid.setGridType(0);

							ldC.getGrids().add(grid);
						}

						if(flag){
							geoLogOperation.getDetails().add(ldC);
						}
						else{
							LogOperation op = new LogOperation(UuidUtils.genUuid(), command
									.getOperType().toString(), 1);

							ldC.setOpId(op.getOpId());

							op.getDetails().add(ldC);

							operations.add(op);
						}
					}
				}
			}

		}
	}

	public void recordLog(ICommand command, Result result) throws Exception {
		// 处理新增的对象
		List<IRow> list = result.getAddObjects();

		for (IRow r : list) {

			LogDetail ld = new LogDetail();

			ld.setOpTp(Status.INSERT);

			ld.setOpbTp(Status.INSERT);

			ld.setObNm(r.parentTableName());

			ld.setObPid(r.parentPKValue());

			ld.setObPk(r.parentPKName());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setNewValue(convertObj2NewValue(r).toString());

			ld.setRowId(UuidUtils.genUuid());

			ld.setTbRowId(r.rowId());

			// 查询对象的grid，并生成LogDetailGrid
			String[] grids = gridCalculator.calc(ld.getTbNm().toUpperCase(),
					ld.getTbRowId(), conn);

			for (String gridId : grids) {
				LogDetailGrid grid = new LogDetailGrid();

				grid.setGridId(Integer.valueOf(gridId));

				grid.setLogRowId(ld.getRowId());

				grid.setGridType(1);

				ld.getGrids().add(grid);
			}

			boolean flag = addToOperation(command, r, ld);

			List<List<IRow>> children = r.children();

			if (children != null) {
				for (List<IRow> list1 : children) {
					for (IRow row : list1) {
						LogDetail ldC = new LogDetail();

						ldC.setOpTp(Status.INSERT);

						ldC.setOpbTp(Status.INSERT);

						ldC.setObNm(row.parentTableName());

						ldC.setObPid(row.parentPKValue());

						ldC.setObPk(row.parentPKName());

						ldC.setObTp(1);

						ldC.setTbNm(row.tableName());

						ldC.setIsCk(0);

						ldC.setNewValue(convertObj2NewValue(row).toString());

						ldC.setRowId(UuidUtils.genUuid());

						ldC.setTbRowId(row.rowId());

						// 查询对象的grid，并生成LogDetailGrid
						grids = gridCalculator.calc(
								ldC.getTbNm().toUpperCase(), ldC.getTbRowId(),
								conn);

						for (String gridId : grids) {
							LogDetailGrid grid = new LogDetailGrid();

							grid.setGridId(Integer.valueOf(gridId));

							grid.setLogRowId(ldC.getRowId());

							grid.setGridType(1);

							ldC.getGrids().add(grid);
						}

						if(flag){
							geoLogOperation.getDetails().add(ldC);
						}
						else{
							LogOperation op = new LogOperation(UuidUtils.genUuid(), command
									.getOperType().toString(), 1);

							ldC.setOpId(op.getOpId());

							op.getDetails().add(ldC);

							operations.add(op);
						}
					}
				}
			}
		}

		if(geoLogOperation.getDetails().size()>0){
			operations.add(geoLogOperation);
		}

		// 计算修改的对象的改后grid
		for (LogOperation op : operations) {
			for (LogDetail detail : op.getDetails()) {

				if (detail.getOpTp() == Status.UPDATE) {
					// 查询对象的grid，并生成LogDetailGrid
					String[] grids = gridCalculator.calc(detail.getTbNm()
							.toUpperCase(), detail.getTbRowId(), conn);

					for (String gridId : grids) {
						LogDetailGrid grid = new LogDetailGrid();

						grid.setGridId(Integer.valueOf(gridId));

						grid.setLogRowId(detail.getRowId());

						grid.setGridType(1);

						detail.getGrids().add(grid);
					}
				}
			}
		}

		this.insertRow();

		// 删除关联的检查结果
		NiValExceptionOperator operator = new NiValExceptionOperator(conn);

		for (IRow r : result.getDelObjects()) {
			if (r instanceof IObj) {
				operator.deleteNiValException(r.tableName().toUpperCase(),
						((IObj) r).pid());
			}
		}
	}

	private static JSONObject convertObj2NewValue(IRow row) throws Exception {
		JSONObject json = new JSONObject();

		JSONObject rowJson = row.Serialize(ObjLevel.HISTORY);

		Iterator<String> keys = rowJson.keys();

		while (keys.hasNext()) {
			String key = keys.next();

			if (!(rowJson.get(key) instanceof JSONArray)) {
				if ("pid".equals(key)) {
					if (row instanceof IObj) {
						if (row instanceof RdCrossName) {
							json.put(key, rowJson.get(key));
						} else {
							json.put(((IObj) row).primaryKey(),
									rowJson.get(key));
						}
					} else {
						json.put(key, rowJson.get(key));
					}

				} else if ("geometry".equals(key)) {

					if (row.objType() == ObjType.CKEXCEPTION) {
						json.put(StringUtils.toColumnName(key),
								rowJson.get(key));
					} else {
						json.put("geometry", Geojson.geojson2Wkt(rowJson
								.getString("geometry")));
					}
				} else {

					Object value = rowJson.get(key);

					if (value instanceof String) {
						json.put(StringUtils.toColumnName(key),
								((String) value).replace("'", "''"));
					} else {
						json.put(StringUtils.toColumnName(key), value);
					}

				}
			}
		}

		json.put("row_id", row.rowId());

		return json;
	}

}