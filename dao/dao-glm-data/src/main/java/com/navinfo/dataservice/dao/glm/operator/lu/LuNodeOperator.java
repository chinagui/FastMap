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
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

public class LuNodeOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(LuNodeOperator.class);
	
	
	
	private LuNode luNode;
	
	public LuNodeOperator(Connection conn, LuNode luNode){
		super(conn);
		
		this.luNode = luNode;
	}
	
	
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luNode.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luNode.tableName());

		sb.append("(node_pid, form, geometry, edit_flag, u_record, row_id) values (");

		sb.append(luNode.getPid());

		sb.append("," + luNode.getForm());

		String wkt = GeoTranslator.jts2Wkt(luNode.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + luNode.getEditFlag());

		sb.append(",1,'" + luNode.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : luNode.getMeshes()) {
			LuNodeMeshOperator ap = new LuNodeMeshOperator(conn, (LuNodeMesh) r);

			ap.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + luNode.tableName() + " set u_record=3,");


			Set<Entry<String, Object>> set = luNode.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();


			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = luNode.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(luNode);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='" + String.valueOf(columnValue) + "',");
						}
						this.setChanged(true);
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
						sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");

						this.setChanged(true);
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");

						this.setChanged(true);
					}

				} else if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value, 0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					if (!StringUtils.isStringSame(oldWkt, newWkt)) {
						sb.append("geometry=sdo_geometry('" + String.valueOf(newWkt) + "',8307),");

						this.setChanged(true);
					}
				}
			}
			sb.append(" where node_pid=" + luNode.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luNode.tableName() + " set u_record=2 where " + luNode.primaryKey() + "=" + luNode.getPid();

		stmt.addBatch(sql);

		for (IRow r : luNode.getMeshes()) {
			LuNodeMeshOperator ap = new LuNodeMeshOperator(conn, (LuNodeMesh) r);

			ap.deleteRow2Sql(stmt);
		}
	}

}
