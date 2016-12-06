package com.navinfo.dataservice.diff.merge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DbLinkUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogDayRelease;
import com.navinfo.dataservice.dao.log.LogDetail;
import com.navinfo.dataservice.dao.log.LogDetailGrid;
import com.navinfo.dataservice.dao.log.LogOperation;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Geometry;

public class DayRegionPoiMerge {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Connection conn = null;
	
	private int dbId;

	private String dblinkName = "mk_dblink";
	
	private String tempTableName = "fuse_day_poi_tmp";

	private int poiCount;

	private int logOpCount;

	private int logDetailCount;

	private int logDetailGridCount;

	private int logDayReleaseCount;
	
	public DayRegionPoiMerge(int dbId) throws Exception{
		conn = DBConnector.getInstance().getConnectionById(dbId);
		
		this.dbId = dbId;
	}

	public int getPoiCount() {
		return poiCount;
	}

	public void setPoiCount(int poiCount) {
		this.poiCount = poiCount;
	}

	public int getLogOpCount() {
		return logOpCount;
	}

	public void setLogOpCount(int logOpCount) {
		this.logOpCount = logOpCount;
	}

	public int getLogDetailCount() {
		return logDetailCount;
	}

	public void setLogDetailCount(int logDetailCount) {
		this.logDetailCount = logDetailCount;
	}

	public int getLogDetailGridCount() {
		return logDetailGridCount;
	}

	public void setLogDetailGridCount(int logDetailGridCount) {
		this.logDetailGridCount = logDetailGridCount;
	}

	public int getLogDayReleaseCount() {
		return logDayReleaseCount;
	}

	public void setLogDayReleaseCount(int logDayReleaseCount) {
		this.logDayReleaseCount = logDayReleaseCount;
	}

	private void createDBLink() throws Exception {

		log.info("create dblink ...");

		DatahubApi datahub = (DatahubApi) ApplicationContextUtil
				.getBean("datahubApi");

		DbInfo db = datahub.getOnlyDbByType("nationRoad");

		DbConnectConfig config = DbConnectConfig.createConnectConfig(db
				.getConnectParam());

		// 创建DBLINK
		QueryRunner run = new QueryRunner();

		try{
			String sql = DbLinkUtils.getCreateSql(dblinkName, config);
	
			run.update(conn, sql);
		}
		catch (Exception ex){
			log.error(ex.getMessage());
		}
		
		log.info("create dblink success");

	}

	// 删除DBLINK
	private void deleteDBLink() throws SQLException {

		log.info("drop dblink...");

		QueryRunner run = new QueryRunner();

		String sql = "DROP DATABASE LINK " + dblinkName;

		run.update(conn, sql);

		log.info("drop dblink success");
	}

	private void createTempTable() throws SQLException{
		log.info("create temp table...");
		
		String sql = "create table "+tempTableName+" as select p.pid, q.geometry from ix_poi p, ix_poi@"
				+ dblinkName
				+ " q  where p.pid = q.pid    and p.u_record != 2    and q.u_record != 2    and (p.geometry.sdo_point.x != q.geometry.sdo_point.x or        p.geometry.sdo_point.y != q.geometry.sdo_point.y)    and not exists(        select 1 from log_operation t,log_detail d where t.op_id=d.op_id and t.com_sta=0 and d.op_tp=3 and d.fd_lst like '%geometry%' and d.tb_row_id=p.row_id)";

		QueryRunner run = new QueryRunner();
		
		run.update(conn, sql);
		
		log.info("create temp table success");
	}
	
	private void dropTempTable() throws SQLException{
		
		log.info("drop temp table...");
		
		String sql = "drop table "+tempTableName;
		
		QueryRunner run = new QueryRunner();
		
		run.update(conn, sql);
		
		log.info("drop temp table success");
	}
	
	
	private void writeLog() throws Exception {

		log.info("generate logs...");

		String sql = "select p.pid,p.row_id,p.geometry old_geometry, q.geometry new_geometry   from ix_poi p,"+tempTableName+" q where p.pid=q.pid";

		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery(sql);

		Statement pstmt = conn.createStatement();

		LogWriter logWriter = new LogWriter();

		int count = 0;
		while (rs.next()) {

			IxPoi poi = new IxPoi();

			poi.setPid(rs.getInt("pid"));

			poi.setRowId(rs.getString("row_id"));

			STRUCT struct = (STRUCT) rs.getObject("old_geometry");

			Geometry oldGeometry = GeoTranslator.struct2Jts(struct);

			struct = (STRUCT) rs.getObject("new_geometry");

			Geometry newGeometry = GeoTranslator.struct2Jts(struct);

			LogOperation op = new LogOperation(UuidUtils.genUuid(), 5);

			op.setComSta(1);

			op.setComDt(op.getOpDt());

			addLogDetails(op, poi, oldGeometry, newGeometry);

			addLogDayRelease(op);

			logWriter.insertLogOperation2Sql(op, pstmt);

			count++;
			logOpCount++;

			if (count % 2000 == 0) {
				pstmt.executeBatch();
				pstmt.clearBatch();
				count = 0;

				log.info(logOpCount);
			}
		}

		if (count > 0) {
			pstmt.executeBatch();
			log.info(logOpCount);
		}

		pstmt.close();

		rs.close();

		stmt.close();

		log.info("generate logs success");

	}

	private void addLogDayRelease(LogOperation op) {

		LogDayRelease release = new LogDayRelease(op.getOpId());

		op.setRelease(release);

		logDayReleaseCount++;
	}

	private void addLogDetails(LogOperation op, IxPoi poi,
			Geometry oldGeometry, Geometry newGeometry) {
		LogDetail detail = new LogDetail();

		detail.setRowId(UuidUtils.genUuid());

		detail.setOpId(op.getOpId());

		detail.setTbNm(poi.tableName());

		JSONObject oldValue = new JSONObject();

		JSONObject newValue = new JSONObject();

		oldValue.put("geometry", GeoTranslator.jts2Wkt(oldGeometry));

		detail.setOldValue(oldValue.toString());

		newValue.put("geometry", GeoTranslator.jts2Wkt(newGeometry));

		detail.setNewValue(newValue.toString());

		JSONArray fieldList = new JSONArray();

		fieldList.add("geometry");

		detail.setFdLst(fieldList.toString());

		detail.setOpTp(3);

		detail.setTbRowId(poi.getRowId());

		addLogDetailGrids(detail, oldGeometry, 0);

		addLogDetailGrids(detail, newGeometry, 1);

		op.getDetails().add(detail);

		logDetailCount++;
	}

	private void addLogDetailGrids(LogDetail detail, Geometry geometry, int type) {

		String[] grids = CompGridUtil.point2Grids(geometry.getCoordinate().x,
				geometry.getCoordinate().y);

		for (String grid : grids) {
			LogDetailGrid detailGrid = new LogDetailGrid();

			detailGrid.setLogRowId(detail.getRowId());

			detailGrid.setGridId(Integer.valueOf(grid));

			detailGrid.setGridType(type);

			detail.getGrids().add(detailGrid);

			logDetailGridCount++;
		}

	}

	private void updatePoi() throws SQLException {

		log.info("update poi geometry...");

		String sql = "update ix_poi p set p.geometry = (select q.geometry from "+tempTableName+" q where q.pid = p.pid) where exists(select 1 from "+tempTableName+" q where q.pid=p.pid)";
		
		QueryRunner run = new QueryRunner();

		poiCount = run.update(conn, sql);

		log.info("update poi geometry success. total " + poiCount);

	}

	public void run() throws Exception {

		log.info("start update region dbId " + dbId);

		try {

			conn.setAutoCommit(false);

			// 创建dblink
			createDBLink();
			
			//创建临时表
			createTempTable();

			// 写履历
			writeLog();

			// 更新POI几何
			updatePoi();
			
			//删除临时表
			dropTempTable();

			// 删除dblink
			deleteDBLink();

			conn.commit();
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

		log.info("finish update region dbId " + dbId);
	}
}
