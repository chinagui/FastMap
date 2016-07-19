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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(加油站类)操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiGasstationOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiGasstationOperator.class);

	private IxPoiGasstation ixPoiGasstation;

	public IxPoiGasstationOperator(Connection conn,
			IxPoiGasstation ixPoiGasstation) {
		super(conn);
		this.ixPoiGasstation = ixPoiGasstation;
	}

	
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiGasstation.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiGasstation.tableName());
		sb.append("(gasstation_id, poi_pid, service_prov, fuel_type, oil_type, eg_type, mg_type, payment, service, memo,open_hour, photo_name, u_date,u_record,row_id) values (");
		sb.append(ixPoiGasstation.getPid());
		sb.append("," + ixPoiGasstation.getPoiPid());
		if (ixPoiGasstation.getServiceProv() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getServiceProv() + "'");
		}
		if (ixPoiGasstation.getServiceProv() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getFuelType() + "'");
		}
		if (ixPoiGasstation.getOilType() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getOilType() + "'");
		}
		if (ixPoiGasstation.getEgType() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getEgType() + "'");
		}
		if (ixPoiGasstation.getMgType() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getMgType() + "'");
		}
		if (ixPoiGasstation.getPayment() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getPayment() + "'");
		}
		if (ixPoiGasstation.getService() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getService() + "'");
		}
		if (ixPoiGasstation.getMemo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getMemo() + "'");
		}
		if (ixPoiGasstation.getOpenHour() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getOpenHour() + "'");
		}
		if (ixPoiGasstation.getPhotoName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiGasstation.getPhotoName() + "'");
		}
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiGasstation.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiGasstation.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiGasstation.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiGasstation.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiGasstation);

			column = StringUtils.toColumnName(column);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value),
						String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}
					this.setChanged(true);
				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double
						.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Double.parseDouble(String.valueOf(columnValue))
							+ ",");

					this.setChanged(true);
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer
						.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Integer.parseInt(String.valueOf(columnValue))
							+ ",");

					this.setChanged(true);
				}

			}
		}
		sb.append(" where gasstation_id=" + ixPoiGasstation.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiGasstation.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where gasstation_id   =" + ixPoiGasstation.getPid();
		stmt.addBatch(sql);
	}

}
