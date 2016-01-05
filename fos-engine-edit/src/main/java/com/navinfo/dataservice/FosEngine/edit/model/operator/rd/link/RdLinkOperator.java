package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimitTruck;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkRtic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkWalkstair;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkZone;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdLinkOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdLinkOperator.class);

	private Connection conn;

	private RdLink rdLink;

	public RdLinkOperator(Connection conn, RdLink rdLink) {
		this.conn = conn;

		this.rdLink = rdLink;
	}

	@Override
	public void insertRow() throws Exception {

		rdLink.setRowId(UuidUtils.genUuid());

		// 新增rd_link
		String sql = "insert into rd_link"
				+ "(link_pid, s_node_pid, e_node_pid, kind, direct, app_info, toll_info, route_adopt, multi_digitized, develop_state, imi_code, special_traffic, function_class, urban, pave_status, lane_num, lane_left, lane_right, lane_width_left, lane_width_right, lane_class, width, is_viaduct, left_region_id, right_region_id, geometry, length, oneway_mark, mesh_id, street_light, parking_lot, adas_flag, sidewalk_flag, walkstair_flag, dici_type, walk_flag, dif_groupid, src_flag, digital_level, edit_flag, truck_flag, origin_link_pid, center_divider, parking_flag, memo, u_record,row_id)"
				+ " values (:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12,:13,:14,:15,:16,:17,:18,:19,:20,:21,:22,:23,:24,:25,sdo_geometry(:26,8307),:27,:28,:29,:30,:31,:32,:33,:34,:35,:36,:37,:38,:39,:40,:41,:42,:43,:44,:45,:46,:47)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, rdLink.getPid());

			pstmt.setInt(2, rdLink.getsNodePid());

			pstmt.setInt(3, rdLink.geteNodePid());

			pstmt.setInt(4, rdLink.getKind());

			pstmt.setInt(5, rdLink.getDirect());

			pstmt.setInt(6, rdLink.getAppInfo());

			pstmt.setInt(7, rdLink.getTollInfo());

			pstmt.setInt(8, rdLink.getRouteAdopt());

			pstmt.setInt(9, rdLink.getMultiDigitized());

			pstmt.setInt(10, rdLink.getDevelopState());

			pstmt.setInt(11, rdLink.getImiCode());

			pstmt.setInt(12, rdLink.getSpecialTraffic());

			pstmt.setInt(13, rdLink.getFunctionClass());

			pstmt.setInt(14, rdLink.getUrban());

			pstmt.setInt(15, rdLink.getPaveStatus());

			pstmt.setInt(16, rdLink.getLaneNum());

			pstmt.setInt(17, rdLink.getLaneLeft());

			pstmt.setInt(18, rdLink.getLaneRight());

			pstmt.setInt(19, rdLink.getLaneWidthLeft());

			pstmt.setInt(20, rdLink.getLaneWidthRight());

			pstmt.setInt(21, rdLink.getLaneClass());

			pstmt.setInt(22, rdLink.getWidth());

			pstmt.setInt(23, rdLink.getIsViaduct());

			pstmt.setInt(24, rdLink.getLeftRegionId());

			pstmt.setInt(25, rdLink.getRightRegionId());

			pstmt.setString(26,
					GeoTranslator.jts2Wkt(rdLink.getGeometry(), 0.00001, 5));

			pstmt.setDouble(27, rdLink.getLength());

			pstmt.setInt(28, rdLink.getOnewayMark());

			pstmt.setInt(29, rdLink.getMeshId());

			pstmt.setInt(30, rdLink.getStreetLight());

			pstmt.setInt(31, rdLink.getParkingLot());

			pstmt.setInt(32, rdLink.getAdasFlag());

			pstmt.setInt(33, rdLink.getSidewalkFlag());

			pstmt.setInt(34, rdLink.getWalkstairFlag());

			pstmt.setInt(35, rdLink.getDiciType());

			pstmt.setInt(36, rdLink.getWalkFlag());

			pstmt.setString(37, rdLink.getDifGroupid());

			pstmt.setInt(38, rdLink.getSrcFlag());

			pstmt.setInt(39, rdLink.getDigitalLevel());

			pstmt.setInt(40, rdLink.getEditFlag());

			pstmt.setInt(41, rdLink.getTruckFlag());

			pstmt.setInt(42, rdLink.getOriginLinkPid());

			pstmt.setInt(43, rdLink.getCenterDivider());

			pstmt.setInt(44, rdLink.getParkingFlag());

			pstmt.setString(45, rdLink.getMemo());

			pstmt.setInt(46, 1);

			pstmt.setString(47, rdLink.rowId());

			pstmt.execute();

			pstmt.close();

			Statement stmt = conn.createStatement();
			
			sql = "update rd_link set length=sdo_geom.sdo_length(geometry,0.1) where link_pid = "+rdLink.getPid();
			
			stmt.executeUpdate(sql);

			// 新增rd_link_form
			for (IRow r : rdLink.getForms()) {
				RdLinkFormOperator op = new RdLinkFormOperator(conn,
						(RdLinkForm) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_int_rtic
			for (IRow r : rdLink.getIntRtics()) {
				RdLinkIntRticOperator op = new RdLinkIntRticOperator(conn,
						(RdLinkIntRtic) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_limit
			for (IRow r : rdLink.getLimits()) {
				RdLinkLimitOperator op = new RdLinkLimitOperator(conn,
						(RdLinkLimit) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_limit_truck
			for (IRow r : rdLink.getLimitTrucks()) {
				RdLinkLimitTruckOperator op = new RdLinkLimitTruckOperator(
						conn, (RdLinkLimitTruck) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_name
			for (IRow r : rdLink.getNames()) {
				RdLinkNameOperator op = new RdLinkNameOperator(conn,
						(RdLinkName) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_rtic
			for (IRow r : rdLink.getRtics()) {
				RdLinkRticOperator op = new RdLinkRticOperator(conn,
						(RdLinkRtic) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_sidewalk
			for (IRow r : rdLink.getSidewalks()) {
				RdLinkSidewalkOperator op = new RdLinkSidewalkOperator(conn,
						(RdLinkSidewalk) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_speedlimit
			for (IRow r : rdLink.getSpeedlimits()) {
				RdLinkSpeedlimitOperator op = new RdLinkSpeedlimitOperator(
						conn, (RdLinkSpeedlimit) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_walkstair
			for (IRow r : rdLink.getWalkstairs()) {
				RdLinkWalkstairOperator op = new RdLinkWalkstairOperator(conn,
						(RdLinkWalkstair) r);

				op.insertRow2Sql(stmt);
			}

			// 新增rd_link_zone
			for (IRow r : rdLink.getZones()) {
				RdLinkZoneOperator op = new RdLinkZoneOperator(conn,
						(RdLinkZone) r);

				op.insertRow2Sql(stmt);
			}

			stmt.executeBatch();
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

	@Override
	public void updateRow() throws Exception {

		StringBuilder sb = new StringBuilder("update " + rdLink.tableName()
				+ " set ");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = rdLink.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = rdLink.getClass().getDeclaredField(column);

				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(rdLink);

				if (value instanceof String) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");

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

				} else if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
							0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					if (!StringUtils.isStringSame(oldWkt, newWkt)) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(newWkt) + "',8307),");

						isChanged = true;
					}
				}
			}
			sb.append(" where link_pid=" + rdLink.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			if (isChanged) {
				pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
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

	@Override
	public void deleteRow() throws Exception {

		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			// 先删除LINK关联的子表,再删除LINK
			for (IRow r : rdLink.getForms()) {
				RdLinkFormOperator op = new RdLinkFormOperator(conn,
						(RdLinkForm) r);

				op.deleteRow2Sql(stmt);
			}

			for (IRow r : rdLink.getLimits()) {
				RdLinkLimitOperator op = new RdLinkLimitOperator(conn,
						(RdLinkLimit) r);

				op.deleteRow2Sql(stmt);
			}

			for (IRow r : rdLink.getNames()) {
				RdLinkNameOperator op = new RdLinkNameOperator(conn,
						(RdLinkName) r);

				op.deleteRow2Sql(stmt);
			}

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

	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update rd_link set u_record=2 where link_pid = "
				+ rdLink.getPid();

		stmt.addBatch(sql);
	}

}
