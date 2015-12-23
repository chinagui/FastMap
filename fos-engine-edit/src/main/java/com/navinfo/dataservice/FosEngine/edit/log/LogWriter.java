package com.navinfo.dataservice.FosEngine.edit.log;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.service.LogPidService;
import com.vividsolutions.jts.geom.Geometry;

public class LogWriter {

	private Connection conn;

	private LogOperation logOperation;

	public LogWriter(Connection conn) {
		this.conn = conn;

	}

	public void insertRow() throws Exception {

		Statement stmt = null;

		try {
			stmt = this.conn.createStatement();

			this.insertRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	public void insertRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(this.logOperation.tableName());

		sb.append("(op_id, us_id, op_cmd, op_dt, op_sg) values (");

		sb.append(this.logOperation.getOpId());

		if (this.logOperation.getUsId() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + this.logOperation.getUsId() + "'");
		}

		if (this.logOperation.getOpCmd() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + this.logOperation.getOpCmd() + "'");
		}

		if (this.logOperation.getOpDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + this.logOperation.getOpDt() + "'");
		}

		sb.append("," + this.logOperation.getOpSg());

		sb.append(")");

		stmt.addBatch(sb.toString());

		for (LogDetail r : this.logOperation.getDetails()) {
			this.insertLogDetail2Sql(r, stmt);
		}
	}

	public void insertLogDetail2Sql(LogDetail detail, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(detail.tableName());

		sb.append("(op_id, ob_nm, ob_pk, ob_pid, opb_tp, ob_tp, op_dt, tb_nm, old_value, new_value, fd_lst, op_tp, row_id, is_ck) values (");

		sb.append(detail.getOpId());

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

		if (detail.getOpDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + detail.getOpDt() + "'");
		}

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

		sb.append(")");

		stmt.addBatch(sb.toString());
	}

	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	public void deleteRow2Sql(Statement stmt) throws Exception {

	}

	public void recordLog(ICommand command, Result result) throws Exception {

		String dt = StringUtils.getCurrentTime();

		logOperation = new LogOperation();

		logOperation.setOpCmd(command.getOperType().toString());

		logOperation.setOpDt(dt);
		
		LogPidService logPidService = new LogPidService(conn);

		int opId = logPidService.generateOpPid();

		logOperation.setOpId(opId);

		logOperation.setOpSg(1);

		List<IRow> list = result.getAddObjects();

		for (IRow r : list) {

			LogDetail ld = new LogDetail();

			ld.setOpId(opId);

			ld.setOpDt(dt);

			ld.setOpTp(3);

			ld.setOpbTp(3);

			ld.setObNm(r.primaryTableName());

			ld.setObPid(r.primaryValue());

			ld.setObPk(r.primaryKey());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setNewValue(r.Serialize(ObjLevel.FULL).toString());

			ld.setRowId(r.rowId());

			logOperation.getDetails().add(ld);

			List<List<IRow>> children = r.children();

			if (children != null) {
				for (List<IRow> list1 : children) {
					for (IRow row : list1) {
						LogDetail ldC = new LogDetail();

						ldC.setOpId(opId);

						ldC.setOpDt(dt);

						ldC.setOpTp(3);

						ldC.setOpbTp(3);

						ldC.setObNm(row.primaryTableName());

						ldC.setObPid(row.primaryValue());

						ldC.setObPk(row.primaryKey());

						ldC.setObTp(1);

						ldC.setTbNm(row.tableName());

						ldC.setIsCk(0);

						ldC.setNewValue(row.Serialize(ObjLevel.FULL).toString());

						ldC.setRowId(row.rowId());

						logOperation.getDetails().add(ldC);
					}
				}
			}
		}

		list = result.getUpdateObjects();

		for (IRow r : list) {
			LogDetail ld = new LogDetail();

			ld.setOpId(opId);

			ld.setOpDt(dt);

			ld.setOpTp(2);

			ld.setOpbTp(2);

			ld.setObNm(r.primaryTableName());

			ld.setObPid(r.primaryValue());

			ld.setObPk(r.primaryKey());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setRowId(r.rowId());

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
					oldValue.put(column, value);

					newValue.put(column, columnValue);
				}
			}

			ld.setFdLst(fieldList.toString());

			ld.setOldValue(oldValue.toString());

			ld.setNewValue(newValue.toString());

			logOperation.getDetails().add(ld);

		}

		list = result.getDelObjects();

		for (IRow r : list) {
			LogDetail ld = new LogDetail();

			ld.setOpId(opId);

			ld.setOpDt(dt);

			ld.setOpTp(1);

			if (r.primaryTableName().equals(r.tableName())) {
				ld.setOpbTp(1);
			} else {
				ld.setOpbTp(2);
			}

			ld.setObNm(r.primaryTableName());

			ld.setObPid(r.primaryValue());

			ld.setObPk(r.primaryKey());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setRowId(r.rowId());

			logOperation.getDetails().add(ld);

			List<List<IRow>> children = r.children();

			if (children != null) {
				for (List<IRow> list1 : children) {
					for (IRow row : list1) {
						LogDetail ldC = new LogDetail();

						ldC.setOpId(opId);

						ldC.setOpDt(dt);

						ldC.setOpTp(1);

						ldC.setOpbTp(1);

						ldC.setObNm(row.primaryTableName());

						ldC.setObPid(row.primaryValue());

						ldC.setObPk(row.primaryKey());

						ldC.setObTp(1);

						ldC.setTbNm(row.tableName());

						ldC.setIsCk(0);

						ldC.setRowId(row.rowId());

						logOperation.getDetails().add(ldC);
					}
				}
			}

		}
		this.insertRow();
	}

}
