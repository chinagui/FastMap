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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(简介) 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiIntroductionOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiIntroductionOperator.class);

	
	private IxPoiIntroduction ixPoiIntroduction;

	public IxPoiIntroductionOperator(Connection conn,
			IxPoiIntroduction ixPoiIntroduction) {
		super(conn);
		this.ixPoiIntroduction = ixPoiIntroduction;
	}

	

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiIntroduction.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiIntroduction.tableName());
		sb.append("(introduction_id, poi_pid, introduction, introduction_eng, website,neighbor, neighbor_eng, traffic, traffic_eng, u_date,u_record, row_id) values (");
		sb.append(ixPoiIntroduction.getPid());
		sb.append("," + ixPoiIntroduction.getPoiPid());

		if (StringUtils.isNotEmpty(ixPoiIntroduction.getIntroduction())) {
			sb.append(",'" + ixPoiIntroduction.getIntroduction() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiIntroduction.getIntroductionEng())) {
			sb.append(",'" + ixPoiIntroduction.getIntroductionEng() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiIntroduction.getWebsite())) {
			sb.append(",'" + ixPoiIntroduction.getWebsite() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiIntroduction.getNeighbor())) {
			sb.append(",'" + ixPoiIntroduction.getNeighbor() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiIntroduction.getNeighborEng())) {
			sb.append(",'" + ixPoiIntroduction.getNeighborEng() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiIntroduction.getTraffic())) {
			sb.append(",'" + ixPoiIntroduction.getTraffic() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiIntroduction.getTrafficEng())) {
			sb.append(",'" + ixPoiIntroduction.getTrafficEng() + "'");
		} else {
			sb.append(", null ");
		}

		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiIntroduction.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiIntroduction.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

			Set<Entry<String, Object>> set = ixPoiIntroduction.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiIntroduction.getClass().getDeclaredField(
						column);

				field.setAccessible(true);

				Object value = field.get(ixPoiIntroduction);

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
						this.setChanged(true);
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");

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
			sb.append(" where introduction_id=" + ixPoiIntroduction.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");
			stmt.addBatch(sql);
	}
	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiIntroduction.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where introduction_id =" + ixPoiIntroduction.getPid();
		stmt.addBatch(sql);
	}

}
