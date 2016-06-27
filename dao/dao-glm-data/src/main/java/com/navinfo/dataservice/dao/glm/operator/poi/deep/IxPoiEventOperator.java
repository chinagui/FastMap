package com.navinfo.dataservice.dao.glm.operator.poi.deep;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiEvent;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiTourroute;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(重大事件类)操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiEventOperator implements IOperator {

	private static Logger logger = Logger.getLogger(IxPoiEventOperator.class);

	private Connection conn;
	private IxPoiEvent ixPoiEvent;

	public IxPoiEventOperator(Connection conn, IxPoiEvent ixPoiEvent) {
		this.conn = conn;
		this.ixPoiEvent = ixPoiEvent;
	}

	@Override
	public void insertRow() throws Exception {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

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

	@Override
	public void updateRow() throws Exception {
		StringBuilder sb = new StringBuilder("update " + ixPoiEvent.tableName()
				+ " set u_record=3,u_date='" + StringUtils.getCurrentTime()
				+ "',");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiEvent.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiEvent.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiEvent);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='"
									+ String.valueOf(columnValue) + "',");
						}
						isChanged = true;
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");

						isChanged = true;
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer
							.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "="
								+ Integer.parseInt(String.valueOf(columnValue))
								+ ",");

						isChanged = true;
					}

				}
			}
			sb.append(" where event_id    =" + ixPoiEvent.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			if (isChanged) {

				pstmt = conn.prepareStatement(sql);

				pstmt.executeUpdate();

			}

		} catch (Exception e) {
			logger.debug("");
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

	@Override
	public void deleteRow() throws Exception {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.deleteRow2Sql(stmt);

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

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiEvent.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiEvent.tableName());
		sb.append("(event_id, event_name, event_name_eng, event_kind, event_kind_eng, event_desc, event_desc_eng, start_date, end_date, detail_time, detail_time_eng, city, poi_pid, photo_name, reserved,memo, u_date,u_record,row_id) values (");
		sb.append(ixPoiEvent.getPid());
		if(StringUtils.isNotEmpty(ixPoiEvent.getEventName())){
			sb.append(",'" + ixPoiEvent.getEventName() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiEvent.getEventNameEng())){
			sb.append(",'" + ixPoiEvent.getEventNameEng() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiEvent.getEventKind())){
			sb.append(",'" + ixPoiEvent.getEventKind() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiEvent.getEventKindEng())){
			sb.append(",'" + ixPoiEvent.getEventKindEng() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getEventDesc())){
			sb.append(",'" + ixPoiEvent.getEventDesc() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiEvent.getEventDescEng())){
			sb.append(",'" + ixPoiEvent.getEventDescEng() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getStartDate())){
			sb.append(",'" + ixPoiEvent.getStartDate() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getEndDate())){
			sb.append(",'" + ixPoiEvent.getEndDate() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiEvent.getDetailTime())){
			sb.append(",'" + ixPoiEvent.getDetailTime() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiEvent.getDetailTimeEng())){
			sb.append(",'" + ixPoiEvent.getDetailTimeEng() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getCity())){
			sb.append(",'" + ixPoiEvent.getCity() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getPoiPid())){
			sb.append(",'" + ixPoiEvent.getPoiPid() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getPhotoName())){
			sb.append(",'" + ixPoiEvent.getPhotoName() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getReserved())){
			sb.append(",'" + ixPoiEvent.getReserved() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiEvent.getMemo())){
			sb.append(",'" + ixPoiEvent.getMemo() + "'");
		}else{
			sb.append(", null ");
		}
		sb.append(",'" + StringUtils.getCurrentTime()+"'");
		sb.append(",1,'" + ixPoiEvent.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiEvent.tableName()
				+ " set u_record=2 ,u_date='"+StringUtils.getCurrentTime()+"' where   event_id      ="
				+ ixPoiEvent.getPid();
		stmt.addBatch(sql);
	}

}
