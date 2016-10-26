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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
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
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONNull;

class Status {
	public static int INSERT = 1;
	public static int DELETE = 2;
	public static int UPDATE = 3;
}

public class LogWriter {
	Logger logger = Logger.getLogger(LogWriter.class);
	private static final String[] poiExcludeColumns =new String[]{"EDIT_FLAG",
			"DIF_GROUPID",
			"RESERVED",
			"FIELD_STATE",
			"STATE",
			"EDITION_FLAG",
			"TASK_ID",
			"COLLECT_TIME",
			"DATA_VERSION",
			"LOG",
			"U_RECORD",
			"U_DATE",
			"U_FIELDS",
			"OLD_ADDRESS",
			"OLD_BLOCKCODE",
			"OLD_NAME",
			"OLD_KIND",
			"POI_NUM"
			};
	private Connection conn;

	private LogOperation geoLogOperation;// 修改拓扑相关的履历记一个operation

	private List<LogOperation> operations = new ArrayList<LogOperation>();

	private GlmGridCalculator gridCalculator;
	
	private long userId;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public LogWriter() {

	}

	public LogWriter(Connection conn) throws Exception {
		this.conn = conn;

		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.gdbVersion);

		this.gridCalculator = GlmGridCalculatorFactory.getInstance().create(
				gdbVersion);
	}

	public List<LogOperation> getOperations() {
		return operations;
	}

	private void insertRow() throws Exception {

		PreparedStatement pstmt = null;

		String sql = "insert into log_operation (op_id, us_id, op_cmd, op_dt, op_sg, com_sta, lock_sta) values (?,?,?,to_date(?,'yyyymmddhh24miss'),?,?,?)";

		try {
			for (LogOperation logOperation : operations) {
				pstmt = this.conn.prepareStatement(sql);

				pstmt.setString(1, logOperation.getOpId());

				pstmt.setString(2, String.valueOf(this.getUserId()));

				pstmt.setString(3, logOperation.getOpCmd());

				pstmt.setString(4, logOperation.getOpDt());

				pstmt.setInt(5, logOperation.getOpSg());

				pstmt.setInt(6, logOperation.getComSta());

				pstmt.setInt(7, logOperation.getLockSta());

				pstmt.execute();

				pstmt.close();
				this.insertLogdayRelease(logOperation.getOpId());
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

	/**
	 * zhaokk 增加日出品信息
	 * 
	 * @param opId
	 * @throws Exception
	 */
	private void insertLogdayRelease(String opId) throws Exception {

		String sql = "insert into log_day_release (op_id, rel_poi_sta,rel_poi_dt,rel_all_sta,rel_all_dt,rel_poi_lock,rel_all_lock) values (?,?,?,?,?,?,?)";
		try {
			QueryRunner run = new QueryRunner();
			LogDayRelease release = new LogDayRelease(opId);
			run.update(conn, sql, opId, release.getRelPoiSta(),
					release.getRelPoiDt(), release.getRelAllSta(),
					release.getRelAllDt(), release.getRelPoiLock(),
					release.getRelAllLock());

		} catch (Exception e) {
			throw new Exception("增加日出品信息SQL错误，" + e.getMessage(), e);
		}
	}

	// private void insertRow2Sql(Statement stmt, LogOperation logOperation)
	// throws Exception {
	//
	// StringBuilder sb = new StringBuilder("insert into ");
	//
	// sb.append(logOperation.tableName());
	//
	// sb.append("(op_id, us_id, op_cmd, op_dt, op_sg) values (");
	//
	// sb.append("'" + logOperation.getOpId() + "'");
	//
	// if (logOperation.getUsId() == null) {
	// sb.append(",null");
	// } else {
	// sb.append(",'" + logOperation.getUsId() + "'");
	// }
	//
	// if (logOperation.getOpCmd() == null) {
	// sb.append(",null");
	// } else {
	// sb.append(",'" + logOperation.getOpCmd() + "'");
	// }
	//
	// if (logOperation.getOpDt() == null) {
	// sb.append(",null");
	// } else {
	// sb.append(",to_date('" + logOperation.getOpDt()
	// + "','yyyymmddhh24miss')");
	// }
	//
	// sb.append("," + logOperation.getOpSg());
	//
	// sb.append(")");
	//
	// stmt.addBatch(sb.toString());
	//
	// for (LogDetail r : logOperation.getDetails()) {
	// this.insertLogDetail2Sql(r, stmt);
	// }
	// }

	private void insertLogDetail(LogDetail detail) throws Exception {

		PreparedStatement pstmt = null;

		String sql = "insert into log_detail (op_id, tb_nm, old, new, fd_lst, op_tp, row_id, is_ck,tb_row_id,ob_nm,ob_pid) values (?,?,?,?,?,?,?,?,?,?,?)";

		//String sql = "insert into log_detail (op_id, tb_nm, old, new, fd_lst, op_tp, row_id, is_ck,tb_row_id) values (?,?,?,?,?,?,?,?,?)";
		
		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, detail.getOpId());

			pstmt.setString(2, detail.getTbNm());

			Clob oldclob = conn.createClob();

			oldclob.setString(1, detail.getOldValue());

			if (oldclob instanceof com.alibaba.druid.proxy.jdbc.ClobProxyImpl) {
				com.alibaba.druid.proxy.jdbc.ClobProxyImpl impl = (com.alibaba.druid.proxy.jdbc.ClobProxyImpl) oldclob;
				oldclob = impl.getRawClob(); // 获取原生的这个 Clob
			}

			pstmt.setClob(3, oldclob);

			Clob newclob = conn.createClob();

			newclob.setString(1, detail.getNewValue());

			if (newclob instanceof com.alibaba.druid.proxy.jdbc.ClobProxyImpl) {
				com.alibaba.druid.proxy.jdbc.ClobProxyImpl impl = (com.alibaba.druid.proxy.jdbc.ClobProxyImpl) newclob;
				newclob = impl.getRawClob(); // 获取原生的这个 Clob
			}

			pstmt.setClob(4, newclob);

			pstmt.setString(5, detail.getFdLst());

			pstmt.setInt(6, detail.getOpTp());

			pstmt.setString(7, detail.getRowId());

			pstmt.setInt(8, detail.getIsCk());

			pstmt.setString(9, detail.getTbRowId());
			
			pstmt.setString(10, detail.getObNm());
			
			pstmt.setInt(11, detail.getObPid());

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

	public void insertLogOperation2Sql(LogOperation op, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(op.tableName());

		sb.append("(op_id, us_id, op_cmd, op_dt, op_sg, com_sta, com_dt, lock_sta) values (");

		sb.append("hextoraw('" + op.getOpId() + "')");

		sb.append("," + op.getUsId());

		if (op.getOpCmd() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + op.getOpCmd() + "'");
		}

		if (op.getOpDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",to_date('" + op.getOpDt() + "','yyyymmddhh24miss')");
		}

		sb.append("," + op.getOpSg());

		sb.append("," + op.getComSta());

		if (op.getComDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",to_date('" + op.getComDt() + "','yyyymmddhh24miss')");
		}

		sb.append("," + op.getLockSta());

		sb.append(")");

		stmt.addBatch(sb.toString());

		for (LogDetail r : op.getDetails()) {
			this.insertLogDetail2Sql(r, stmt);
		}

		this.insertLogDayRelease2Sql(op.getRelease(), stmt);

	}

	private void insertLogDetail2Sql(LogDetail detail, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(detail.tableName());

		sb.append("(op_id, tb_nm, old, new, fd_lst, op_tp, row_id, is_ck,tb_row_id) values (");

		sb.append("'" + detail.getOpId() + "'");

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

		for (LogDetailGrid r : detail.getGrids()) {
			this.insertLogDetailGrid2Sql(r, stmt);
		}
	}

	private void insertLogDetailGrid2Sql(LogDetailGrid grid, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(grid.tableName());

		sb.append("(log_row_id, grid_id, grid_type) values (");

		sb.append("hextoraw('" + grid.getLogRowId() + "')");

		sb.append("," + grid.getGridId());

		sb.append("," + grid.getGridType());

		sb.append(")");

		stmt.addBatch(sb.toString());

	}

	private void insertLogDayRelease2Sql(LogDayRelease release, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(release.tableName());

		sb.append("(op_id, rel_poi_sta,rel_poi_dt,rel_all_sta,rel_all_dt,rel_poi_lock,rel_all_lock) values (");

		sb.append("hextoraw('" + release.getOpId() + "')");

		sb.append("," + release.getRelPoiSta());

		if (release.getRelPoiDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",to_date('" + release.getRelPoiDt()
					+ "','yyyymmddhh24miss')");
		}

		sb.append("," + release.getRelAllSta());

		if (release.getRelAllDt() == null) {
			sb.append(",null");
		} else {
			sb.append(",to_date('" + release.getRelAllDt()
					+ "','yyyymmddhh24miss')");
		}

		sb.append("," + release.getRelPoiLock());

		sb.append("," + release.getRelAllLock());

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
				|| r.objType() == ObjType.ADNODE
				|| r.objType() == ObjType.ADFACE) 
		{

			if (r.status() == ObjStatus.INSERT
					|| r.status() == ObjStatus.DELETE)
			{
				flag = true;
			} else if (r.changedFields().containsKey("geometry")
					|| r.changedFields().containsKey("sNodePid")
					|| r.changedFields().containsKey("eNodePid")) 
			{
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
		
		List<Integer> listObPidList = result.getListUpdateIRowObPid();

		for (int i=0;i<list.size();i++) {
			IRow r = list.get(i);
			LogDetail ld = new LogDetail();

			ld.setOpTp(Status.UPDATE);

			String upperCaseTbName = r.tableName().toUpperCase();
			ld.setTbNm(upperCaseTbName);
			
			//设置对象id和tbName
			ld.setObPid(listObPidList.get(i));
			
			ld.setObNm(SelectorUtils.getObjTableName(r).toUpperCase());

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
				logger.info("column:"+column);
				String tableColumn = StringUtils.toColumnName(column).toUpperCase();
				
				//如果是IX_POI，并且不是作业字段，那么不写入fd_list
				if("IX_POI".equals(upperCaseTbName)
						&&ArrayUtils.contains(poiExcludeColumns,tableColumn)){
					continue;
				}
				Object columnValue = en.getValue();

				Field field = r.getClass().getDeclaredField(column);

				field.setAccessible(true);

				
				
				Object value = field.get(r);
				
				fieldList.add(tableColumn);

				if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
							0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					oldValue.put(tableColumn, oldWkt);

					newValue.put(tableColumn, newWkt);

				}

				else {
					logger.info("value:"+value);
					if (value instanceof String) {
						oldValue.put(tableColumn,
								(String.valueOf(value)).replace("'", "''"));
					} else {
						if (value==null){
							oldValue.put(tableColumn,  "");
						}else{
							oldValue.put(tableColumn, value);
						}
						logger.info("oldValue:"+oldValue);
					}

					newValue.put(tableColumn, columnValue);
				}
			}
			if (CollectionUtils.isEmpty(fieldList)) continue;//修改但是没有变更字段，不写履历
			
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
		
		listObPidList = result.getListDelIRowObPid();

		for (int i=0;i<list.size();i++) {
			IRow r = list.get(i);
			
			LogDetail ld = new LogDetail();
			
			//设置对象id和tbName
			ld.setObPid(listObPidList.get(i));
			
			ld.setObNm(SelectorUtils.getObjTableName(r).toUpperCase());
			
			ld.setOpTp(Status.DELETE);

			ld.setTbNm(r.tableName().toUpperCase());

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
			//循环子表处理子表履历
			handleChildren(command, r, ld);
		}
	}

	public void recordLog(ICommand command, Result result) throws Exception {
		// 处理新增的对象
		List<IRow> list = result.getAddObjects();
		
		List<Integer> listAddObjPidList = result.getListAddIRowObPid();

		for (int i=0;i<list.size();i++) {
			IRow r = list.get(i);

			LogDetail ld = new LogDetail();
			
			//设置对象id和tbName
			ld.setObPid(listAddObjPidList.get(i));
			
			ld.setObNm(SelectorUtils.getObjTableName(r).toUpperCase());

			ld.setOpTp(Status.INSERT);

			ld.setTbNm(r.tableName().toUpperCase());

			ld.setIsCk(0);

			ld.setNewValue(convertObj2NewValue(r).toString());

			ld.setRowId(UuidUtils.genUuid());

			ld.setTbRowId(r.rowId());
			
			String sameTableName = null;
			
			if (r.objType() == ObjType.RDSAMENODE) {
				RdSameNode sameNode = (RdSameNode) r;
				RdSameNodePart part = (RdSameNodePart) sameNode.getParts().get(
						0);
				sameTableName = part.getTableName().toUpperCase();
			}

			if (r.objType() == ObjType.RDSAMELINK) {
				RdSameLink sameLink = (RdSameLink) r;
				RdSameLinkPart part = (RdSameLinkPart) sameLink.getParts().get(
						0);
				sameTableName = part.getTableName().toUpperCase();
			}

			// 查询对象的grid，并生成LogDetailGrid
			String[] grids = gridCalculator.calc(ld.getTbNm().toUpperCase(),
					ld.getTbRowId(), conn,sameTableName);

			for (String gridId : grids) {
				LogDetailGrid grid = new LogDetailGrid();

				grid.setGridId(Integer.valueOf(gridId));

				grid.setLogRowId(ld.getRowId());

				grid.setGridType(1);

				ld.getGrids().add(grid);
			}

			//循环子表处理子表履历
			handleChildren(command, r, ld);
		}

		if (geoLogOperation.getDetails().size() > 0) {
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
							json.put(key.toUpperCase(), rowJson.get(key));
						} else {
							json.put(((IObj) row).primaryKey().toUpperCase(),
									rowJson.get(key));
						}
					} else {
						json.put(key.toUpperCase(), rowJson.get(key));
					}

				} else if ("geometry".equals(key)) {

					if (row.objType() == ObjType.CKEXCEPTION) {
						json.put(StringUtils.toColumnName(key).toUpperCase(),
								rowJson.get(key));
					} else {
						json.put("geometry".toUpperCase(), Geojson.geojson2Wkt(rowJson
								.getString("geometry")));
					}
				} else {

					Object value = rowJson.get(key);

					if (value instanceof String) {
						json.put(StringUtils.toColumnName(key).toUpperCase(),
								((String) value).replace("'", "''"));
					} else {
						json.put(StringUtils.toColumnName(key).toUpperCase(), value);
					}

				}
			}
		}

		json.put("row_id".toUpperCase(), row.rowId());

		return json;
	}
	
	private void handleChildren(ICommand command,IRow r,LogDetail ld) throws Exception
	{
		addToOperation(command, r, ld);

		List<List<IRow>> children = r.children();

		if (children != null) {
			for (List<IRow> list1 : children) {
				for (IRow row : list1) {
					LogDetail ldC = new LogDetail();
					
					//设置对象id和tbName
					ldC.setObPid(ld.getObPid());
					
					ldC.setObNm(ld.getObNm());

					ldC.setOpTp(Status.INSERT);

					ldC.setTbNm(row.tableName().toUpperCase());

					ldC.setIsCk(0);

					ldC.setNewValue(convertObj2NewValue(row).toString());

					ldC.setRowId(UuidUtils.genUuid());

					ldC.setTbRowId(row.rowId());

					// 查询对象的grid，并生成LogDetailGrid
					String[] grids = gridCalculator.calc(
							ldC.getTbNm().toUpperCase(), ldC.getTbRowId(),
							conn);

					for (String gridId : grids) {
						LogDetailGrid grid = new LogDetailGrid();

						grid.setGridId(Integer.valueOf(gridId));

						grid.setLogRowId(ldC.getRowId());

						grid.setGridType(1);

						ldC.getGrids().add(grid);
					}
					if(CollectionUtils.isNotEmpty(row.children()))
					{
						handleChildren(command,row,ldC);
					}
					else
					{
						addToOperation(command,row,ldC);
					}
				}
			}
		}
	}
	public  static void main(String[] args){
		boolean contains = ArrayUtils.contains(poiExcludeColumns,"LOG");
		System.out.println(contains);
	}
}