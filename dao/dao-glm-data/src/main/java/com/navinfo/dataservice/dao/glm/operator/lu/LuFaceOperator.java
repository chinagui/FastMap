package com.navinfo.dataservice.dao.glm.operator.lu;

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
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

public class LuFaceOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(LuFaceOperator.class);

	private LuFace luFace;

	public LuFaceOperator(Connection conn, LuFace luFace) {
		super(conn);

		this.luFace = luFace;
	}

	

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luFace.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luFace.tableName());

		sb.append("(face_pid, feature_pid, geometry, kind, perimeter, mesh_id, edit_flag, detail_flag, u_record, row_id) values (");

		sb.append(luFace.getPid());

		sb.append("," + luFace.getFeaturePid());

		String wkt = GeoTranslator.jts2Wkt(luFace.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + luFace.getKind());

		sb.append("," + luFace.getPerimeter());

		sb.append("," + luFace.getMeshId());

		sb.append("," + luFace.getEditFlag());

		sb.append("," + luFace.getDetailFlag());

		sb.append(",1,'" + luFace.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : luFace.getFaceTopos()) {
			LuFaceTopoOperator ap = new LuFaceTopoOperator(conn, (LuFaceTopo) r);

			ap.insertRow2Sql(stmt);
		}

		for (IRow r : luFace.getFaceNames()) {
			LuFaceNameOperator ap = new LuFaceNameOperator(conn, (LuFaceName) r);

			ap.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + luFace.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = luFace.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = luFace.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(luFace);

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

			} else if (value instanceof Geometry) {
				// 先降级转WKT

				String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
						0.00001, 5);

				String newWkt = Geojson.geojson2Wkt(columnValue.toString());

				if (!StringUtils.isStringSame(oldWkt, newWkt)) {
					sb.append("geometry=sdo_geometry('"
							+ String.valueOf(newWkt) + "',8307),");

					this.setChanged(true);
				}
			}
		}
		sb.append(" where face_pid=" + luFace.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luFace.tableName()
				+ " set u_record=2 where face_pid =" + luFace.getPid();

		stmt.addBatch(sql);

		for (IRow r : luFace.getFaceTopos()) {
			LuFaceTopoOperator ap = new LuFaceTopoOperator(conn, (LuFaceTopo) r);

			ap.insertRow2Sql(stmt);
		}

		for (IRow r : luFace.getFaceNames()) {
			LuFaceNameOperator ap = new LuFaceNameOperator(conn, (LuFaceName) r);

			ap.insertRow2Sql(stmt);
		}
	}

}
