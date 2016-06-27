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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(汽车租赁) 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiCarrentalOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiCarrentalOperator.class);

	private Connection conn;
	private IxPoiCarrental ixPoiCarrental;

	public IxPoiCarrentalOperator(Connection conn, IxPoiCarrental ixPoiCarrental) {
		this.conn = conn;
		this.ixPoiCarrental = ixPoiCarrental;
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
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiCarrental.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiCarrental.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiCarrental.getClass()
						.getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiCarrental);

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
			sb.append(" where row_id=hextoraw('" + ixPoiCarrental.getRowId()
					+ "')");

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
		ixPoiCarrental.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiCarrental.tableName());
		sb.append("(poi_pid, open_hour, address, how_to_go, phone_400,web_site, u_date,u_record,row_id) values (");

		sb.append(ixPoiCarrental.getPoiPid());
		if(StringUtils.isNotEmpty(ixPoiCarrental.getOpenHour())){
			sb.append(",'" + ixPoiCarrental.getOpenHour() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiCarrental.getAdress())){
			sb.append(",'" + ixPoiCarrental.getAdress() + "'");
		}else{
			sb.append(", null ");
		}
		
		if(StringUtils.isNotEmpty(ixPoiCarrental.getHowToGo())){
			sb.append(",'" + ixPoiCarrental.getHowToGo() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiCarrental.getPhone400())){
			sb.append(",'" + ixPoiCarrental.getPhone400() + "'");
		}else{
			sb.append(", null ");
		}
		if(StringUtils.isNotEmpty(ixPoiCarrental.getWebsite())){
			sb.append(",'" + ixPoiCarrental.getWebsite() + "'");
		}else{
			sb.append(", null ");
		}
		
		
		
		
		sb.append(",'" + StringUtils.getCurrentTime()+"'");
		sb.append(",1,'" + ixPoiCarrental.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + ixPoiCarrental.tableName()
				+ " set u_record=2 ,u_date='"+StringUtils.getCurrentTime()+"' where row_id=hextoraw('"
				+ ixPoiCarrental.rowId() + "')";
		stmt.addBatch(sql);
	}

}
