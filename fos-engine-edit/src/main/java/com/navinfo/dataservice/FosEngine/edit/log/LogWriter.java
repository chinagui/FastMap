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
import com.navinfo.dataservice.FosEngine.edit.check.NiValExceptionOperator;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.vividsolutions.jts.geom.Geometry;


class Status{
	public static int INSERT=1;
	public static int DELETE=2;
	public static int UPDATE=3;
}

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

		sb.append("'" + this.logOperation.getOpId() + "'");

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
			sb.append(",to_date('"+logOperation.getOpDt()+"','yyyymmddhh24miss')");
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

		sb.append("(op_id, ob_nm, ob_pk, ob_pid, opb_tp, ob_tp, op_dt, tb_nm, old, new, fd_lst, op_tp, row_id, is_ck,tb_row_id,mesh_id,com_sta) values (");

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

		if (detail.getOpDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",to_date('"+detail.getOpDt()+"','yyyymmddhh24miss')");
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
		
		sb.append(",hextoraw('"+detail.getTbRowId()+"')");
		
		sb.append(","+detail.getMeshId());
		
		sb.append("," + detail.getComSta());

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
		
		String opId = UuidUtils.genUuid();
		
		logOperation.setOpId(opId);

		logOperation.setOpSg(1);

		List<IRow> list = result.getAddObjects();

		for (IRow r : list) {

			LogDetail ld = new LogDetail();

			ld.setOpId(opId);

			ld.setOpDt(dt);

			ld.setOpTp(Status.INSERT);

			ld.setOpbTp(Status.INSERT);

			ld.setObNm(r.primaryTableName());

			ld.setObPid(r.primaryValue());

			ld.setObPk(r.primaryKey());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

//			ld.setNewValue(r.Serialize(ObjLevel.FULL).toString());

			ld.setNewValue(convertObj2NewValue(r).toString());
			
			ld.setRowId(UuidUtils.genUuid());
			
			ld.setTbRowId(r.rowId());
			
			ld.setMeshId(r.mesh());

			logOperation.getDetails().add(ld);

			List<List<IRow>> children = r.children();

			if (children != null) {
				for (List<IRow> list1 : children) {
					for (IRow row : list1) {
						LogDetail ldC = new LogDetail();

						ldC.setOpId(opId);

						ldC.setOpDt(dt);

						ldC.setOpTp(Status.INSERT);

						ldC.setOpbTp(Status.INSERT);

						ldC.setObNm(row.primaryTableName());

						ldC.setObPid(row.primaryValue());

						ldC.setObPk(row.primaryKey());

						ldC.setObTp(1);

						ldC.setTbNm(row.tableName());

						ldC.setIsCk(0);

//						ldC.setNewValue(row.Serialize(ObjLevel.FULL).toString());
						
						ldC.setNewValue(convertObj2NewValue(row).toString());

						ldC.setRowId(UuidUtils.genUuid());
						
						ldC.setTbRowId(r.rowId());
						
						ldC.setMeshId(r.mesh());

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

			ld.setOpTp(Status.UPDATE);

			ld.setOpbTp(Status.UPDATE);

			ld.setObNm(r.primaryTableName());

			ld.setObPid(r.primaryValue());

			ld.setObPk(r.primaryKey());

			ld.setObTp(1);

			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setRowId(UuidUtils.genUuid());
			
			ld.setTbRowId(r.rowId());
			
			ld.setMeshId(r.mesh());

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
		
		NiValExceptionOperator operator = new NiValExceptionOperator(conn);

		for (IRow r : list) {
			LogDetail ld = new LogDetail();

			ld.setOpId(opId);

			ld.setOpDt(dt);

			ld.setOpTp(Status.DELETE);

			if (r.primaryTableName().equals(r.tableName())) {
				ld.setOpbTp(Status.DELETE);
				
				ld.setObTp(1);
				
				//删除关联的检查结果
				operator.deleteNiValException(r.tableName().toUpperCase(), r.primaryValue());

			} else {
				ld.setOpbTp(Status.UPDATE);
				
				ld.setObTp(2);

			}

			ld.setObNm(r.primaryTableName());

			ld.setObPid(r.primaryValue());

			ld.setObPk(r.primaryKey());
			
			ld.setTbNm(r.tableName());

			ld.setIsCk(0);

			ld.setRowId(UuidUtils.genUuid());
			
			ld.setTbRowId(r.rowId());
			
			ld.setMeshId(r.mesh());

			logOperation.getDetails().add(ld);

			List<List<IRow>> children = r.children();

			if (children != null) {
				for (List<IRow> list1 : children) {
					for (IRow row : list1) {
						LogDetail ldC = new LogDetail();

						ldC.setOpId(opId);

						ldC.setOpDt(dt);

						ldC.setOpTp(Status.DELETE);

						ldC.setOpbTp(Status.DELETE);

						ldC.setObNm(row.primaryTableName());

						ldC.setObPid(row.primaryValue());

						ldC.setObPk(row.primaryKey());

						ldC.setObTp(2);

						ldC.setTbNm(row.tableName());

						ldC.setIsCk(0);

						ldC.setRowId(UuidUtils.genUuid());
						
						ldC.setTbRowId(r.rowId());
						
						ldC.setMeshId(r.mesh());

						logOperation.getDetails().add(ldC);
					}
				}
			}

		}
		this.insertRow();
	}
	
	
	private static JSONObject convertObj2NewValue(IRow row) throws Exception{
		JSONObject json = new JSONObject();
		
		JSONObject rowJson = row.Serialize(ObjLevel.FULL);
		
		Iterator<String> keys = rowJson.keys();
		
		while(keys.hasNext()){
			String key = keys.next();
			
			if (!(rowJson.get(key) instanceof JSONArray)){
				if (!"pid".equals(key) && !"geometry".equals(key)){
					json.put(StringUtils.toColumnName(key), rowJson.get(key));
				}else if ("geometry".equals(key)){
					json.put("geometry", Geojson.geojson2Wkt(rowJson.getString("geometry")));
				}else{
					json.put(row.primaryKey(), rowJson.get(key));
				}
			}
		}
		
		json.put("row_id", row.rowId());
		
		return json;
	}

}
