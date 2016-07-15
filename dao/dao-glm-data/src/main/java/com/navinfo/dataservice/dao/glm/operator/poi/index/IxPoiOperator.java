package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlotPh;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiEvent;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiTourroute;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAudio;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEntryimage;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiFlag;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiIcon;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiVideo;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiAdvertisementOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiAttractionOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiBuildingOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiBusinessTimeOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiCarrentalOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiChargingPlotOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiChargingPlotPhOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiChargingStationOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiDetailOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiEventOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiGasstationOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiHotelOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiIntroductionOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiParkingOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiRestaurantOperator;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiTourroutOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * POI主表操作类
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiOperator implements IOperator {

	private Connection conn;

	private IxPoi ixPoi;
	
	private int freshFlag;
	
	private String rawFields;

	public IxPoiOperator(Connection conn, IxPoi ixPoi) throws Exception {
		this.conn = conn;
		this.ixPoi = ixPoi;
		if (org.apache.commons.lang.StringUtils.isBlank(ixPoi.rowId())) {
			ixPoi.setRowId(UuidUtils.genUuid());
		}
		upatePoiStatus();
	}

	public IxPoiOperator(Connection conn, String rowId) throws Exception {
		this.conn = conn;
		ixPoi = new IxPoi();
		ixPoi.setRowId(rowId);
		upatePoiStatus();
	}
	
	public IxPoiOperator(Connection conn, String rowId,int freshFlag, String rawFields) throws Exception {
		this.conn = conn;
		ixPoi = new IxPoi();
		ixPoi.setRowId(rowId);
		this.freshFlag = freshFlag;
		this.rawFields = rawFields;
		upatePoiStatusForAndroid();
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
		StringBuilder sb = new StringBuilder("update " + ixPoi.tableName()
				+ " set u_record=3,u_date= '"+StringUtils.getCurrentTime()+"' ,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoi.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoi.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoi);

				if (column.equals("open24h")) {
					column = "open_24h";
				} else if (column.equals("level")) {
					column = "\"LEVEL\"";
				} else {

					column = StringUtils.toColumnName(column);
				}

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
			sb.append(" where pid=" + ixPoi.getPid());

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

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoi.tableName());

		sb.append("(PID, KIND_CODE, GEOMETRY, X_GUIDE, Y_GUIDE, LINK_PID, SIDE, "
				+ "NAME_GROUPID, ROAD_FLAG, PMESH_ID, ADMIN_REAL, IMPORTANCE, CHAIN, "
				+ "AIRPORT_CODE, ACCESS_FLAG, OPEN_24H, MESH_ID_5K, MESH_ID, REGION_ID, "
				+ "POST_CODE, EDIT_FLAG, DIF_GROUPID, RESERVED, STATE, FIELD_STATE, "
				+ "LABEL, TYPE, ADDRESS_FLAG, EX_PRIORITY, EDITION_FLAG, POI_MEMO, "
				+ "OLD_BLOCKCODE, OLD_NAME, OLD_ADDRESS, OLD_KIND, POI_NUM, LOG, TASK_ID, "
				+ "DATA_VERSION, FIELD_TASK_ID, VERIFIED_FLAG, COLLECT_TIME, "
				+ "GEO_ADJUST_FLAG, FULL_ATTR_FLAG, OLD_X_GUIDE, OLD_Y_GUIDE,U_DATE,"
				+ "\"LEVEL\",SPORTS_VENUE,INDOOR,VIP_FLAG,U_RECORD,"
				+ "ROW_ID) values (");

		sb.append(ixPoi.getPid());

		sb.append("," + ixPoi.getKindCode());

		String wkt = GeoTranslator.jts2Wkt(ixPoi.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + ixPoi.getxGuide());

		sb.append("," + ixPoi.getyGuide());

		sb.append("," + ixPoi.getLinkPid());

		sb.append("," + ixPoi.getSide());

		sb.append("," + ixPoi.getNameGroupid());

		sb.append("," + ixPoi.getRoadFlag());

		sb.append("," + ixPoi.getPmeshId());

		sb.append("," + ixPoi.getAdminReal());

		sb.append("," + ixPoi.getImportance());

		if (StringUtils.isNotEmpty(ixPoi.getChain())) {

			sb.append(",'" + ixPoi.getChain() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getAirportCode())) {

			sb.append(",'" + ixPoi.getAirportCode() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getAccessFlag());

		sb.append("," + ixPoi.getOpen24h());

		if (StringUtils.isNotEmpty(ixPoi.getMeshId5k())) {

			sb.append(",'" + ixPoi.getMeshId5k() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getMeshId());

		sb.append("," + ixPoi.getRegionId());

		if (StringUtils.isNotEmpty(ixPoi.getPostCode())) {

			sb.append(",'" + ixPoi.getPostCode() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getEditFlag());

		if (StringUtils.isNotEmpty(ixPoi.getDifGroupid())) {

			sb.append(",'" + ixPoi.getDifGroupid() + "'");
		} else {
			sb.append(",null");
		}
		if (StringUtils.isNotEmpty(ixPoi.getReserved())) {

			sb.append(",'" + ixPoi.getReserved() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getState());

		if (StringUtils.isNotEmpty(ixPoi.getFieldState())) {

			sb.append(",'" + ixPoi.getFieldState() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getLabel())) {

			sb.append(",'" + ixPoi.getLabel() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getType());

		sb.append("," + ixPoi.getAddressFlag());

		if (StringUtils.isNotEmpty(ixPoi.getExPriority())) {

			sb.append(",'" + ixPoi.getExPriority() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getEditFlag());

		if (StringUtils.isNotEmpty(ixPoi.getPoiMemo())) {

			sb.append(",'" + ixPoi.getPoiMemo() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getOldBlockcode())) {

			sb.append(",'" + ixPoi.getOldBlockcode() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getOldName())) {

			sb.append(",'" + ixPoi.getOldName() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getOldAddress())) {

			sb.append(",'" + ixPoi.getOldAddress() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getOldKind())) {

			sb.append(",'" + ixPoi.getOldKind() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getPoiNum())) {

			sb.append(",'" + ixPoi.getPoiNum() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getLog())) {

			sb.append(",'" + ixPoi.getLog() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getTaskId());

		if (StringUtils.isNotEmpty(ixPoi.getDataVersion())) {

			sb.append(",'" + ixPoi.getDataVersion() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getFieldTaskId());

		sb.append("," + ixPoi.getVerifiedFlag());

		if (StringUtils.isNotEmpty(ixPoi.getCollectTime())) {

			sb.append(",'" + ixPoi.getCollectTime() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getGeoAdjustFlag());

		sb.append("," + ixPoi.getFullAttrFlag());

		sb.append("," + ixPoi.getOldXGuide());

		sb.append("," + ixPoi.getOldYGuide());

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		if (StringUtils.isNotEmpty(ixPoi.getLevel())) {

			sb.append(",'" + ixPoi.getLevel() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoi.getSportsVenue())) {

			sb.append(",'" + ixPoi.getSportsVenue() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoi.getIndoor());

		if (StringUtils.isNotEmpty(ixPoi.getVipFlag())) {

			sb.append(",'" + ixPoi.getVipFlag() + "'");
		} else {
			sb.append(",null");
		}

		sb.append(",1,'" + ixPoi.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : ixPoi.getFlags()) {
			IxPoiFlagOperator op = new IxPoiFlagOperator(conn, (IxPoiFlag) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getContacts()) {
			IxPoiContactOperator op = new IxPoiContactOperator(conn,
					(IxPoiContact) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAddresses()) {
			IxPoiAddressOperator op = new IxPoiAddressOperator(conn,
					(IxPoiAddress) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getPhotos()) {
			IxPoiPhotoOperator op = new IxPoiPhotoOperator(conn, (IxPoiPhoto) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAudioes()) {
			IxPoiAudioOperator op = new IxPoiAudioOperator(conn, (IxPoiAudio) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getVideoes()) {
			IxPoiVideoOperator op = new IxPoiVideoOperator(conn, (IxPoiVideo) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getIcons()) {
			IxPoiIconOperator op = new IxPoiIconOperator(conn, (IxPoiIcon) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getEntryImages()) {
			IxPoiEntryImageOperator op = new IxPoiEntryImageOperator(conn,
					(IxPoiEntryimage) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getNames()) {
			IxPoiNameOperator op = new IxPoiNameOperator(conn, (IxPoiName) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getParkings()) {
			IxPoiParkingOperator op = new IxPoiParkingOperator(conn,
					(IxPoiParking) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getTourroutes()) {
			IxPoiTourroutOperator op = new IxPoiTourroutOperator(conn,
					(IxPoiTourroute) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getEvents()) {
			IxPoiEventOperator op = new IxPoiEventOperator(conn, (IxPoiEvent) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getDetails()) {
			IxPoiDetailOperator op = new IxPoiDetailOperator(conn,
					(IxPoiDetail) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn,
					(IxPoiBusinessTime) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn,
					(IxPoiBusinessTime) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingstations()) {
			IxPoiChargingStationOperator op = new IxPoiChargingStationOperator(
					conn, (IxPoiChargingStation) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotOperator op = new IxPoiChargingPlotOperator(conn,
					(IxPoiChargingPlot) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(
					conn, (IxPoiChargingPlotPh) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplotPhs()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(
					conn, (IxPoiChargingPlotPh) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBuildings()) {
			IxPoiBuildingOperator op = new IxPoiBuildingOperator(conn,
					(IxPoiBuilding) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAdvertisements()) {
			IxPoiAdvertisementOperator op = new IxPoiAdvertisementOperator(
					conn, (IxPoiAdvertisement) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getGasstations()) {
			IxPoiGasstationOperator op = new IxPoiGasstationOperator(conn,
					(IxPoiGasstation) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getIntroductions()) {
			IxPoiIntroductionOperator op = new IxPoiIntroductionOperator(conn,
					(IxPoiIntroduction) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn,
					(IxPoiAttraction) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn,
					(IxPoiAttraction) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getHotels()) {
			IxPoiHotelOperator op = new IxPoiHotelOperator(conn, (IxPoiHotel) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getRestaurants()) {
			IxPoiRestaurantOperator op = new IxPoiRestaurantOperator(conn,
					(IxPoiRestaurant) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getCarrentals()) {
			IxPoiCarrentalOperator op = new IxPoiCarrentalOperator(conn,
					(IxPoiCarrental) r);

			op.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoi.tableName() + " set u_record=2,u_date= '"+StringUtils.getCurrentTime()+"'  where pid=" + ixPoi.getPid();

		stmt.addBatch(sql);

		for (IRow r : ixPoi.getFlags()) {
			IxPoiFlagOperator op = new IxPoiFlagOperator(conn, (IxPoiFlag) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getContacts()) {
			IxPoiContactOperator op = new IxPoiContactOperator(conn,
					(IxPoiContact) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAddresses()) {
			IxPoiAddressOperator op = new IxPoiAddressOperator(conn,
					(IxPoiAddress) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getPhotos()) {
			IxPoiPhotoOperator op = new IxPoiPhotoOperator(conn, (IxPoiPhoto) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAudioes()) {
			IxPoiAudioOperator op = new IxPoiAudioOperator(conn, (IxPoiAudio) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getVideoes()) {
			IxPoiVideoOperator op = new IxPoiVideoOperator(conn, (IxPoiVideo) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getIcons()) {
			IxPoiIconOperator op = new IxPoiIconOperator(conn, (IxPoiIcon) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getEntryImages()) {
			IxPoiEntryImageOperator op = new IxPoiEntryImageOperator(conn,
					(IxPoiEntryimage) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getNames()) {
			IxPoiNameOperator op = new IxPoiNameOperator(conn, (IxPoiName) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getParkings()) {
			IxPoiParkingOperator op = new IxPoiParkingOperator(conn,
					(IxPoiParking) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getTourroutes()) {
			IxPoiTourroutOperator op = new IxPoiTourroutOperator(conn,
					(IxPoiTourroute) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getEvents()) {
			IxPoiEventOperator op = new IxPoiEventOperator(conn, (IxPoiEvent) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getDetails()) {
			IxPoiDetailOperator op = new IxPoiDetailOperator(conn,
					(IxPoiDetail) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn,
					(IxPoiBusinessTime) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn,
					(IxPoiBusinessTime) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingstations()) {
			IxPoiChargingStationOperator op = new IxPoiChargingStationOperator(
					conn, (IxPoiChargingStation) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotOperator op = new IxPoiChargingPlotOperator(conn,
					(IxPoiChargingPlot) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(
					conn, (IxPoiChargingPlotPh) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplotPhs()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(
					conn, (IxPoiChargingPlotPh) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBuildings()) {
			IxPoiBuildingOperator op = new IxPoiBuildingOperator(conn,
					(IxPoiBuilding) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAdvertisements()) {
			IxPoiAdvertisementOperator op = new IxPoiAdvertisementOperator(
					conn, (IxPoiAdvertisement) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getGasstations()) {
			IxPoiGasstationOperator op = new IxPoiGasstationOperator(conn,
					(IxPoiGasstation) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getIntroductions()) {
			IxPoiIntroductionOperator op = new IxPoiIntroductionOperator(conn,
					(IxPoiIntroduction) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn,
					(IxPoiAttraction) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn,
					(IxPoiAttraction) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getHotels()) {
			IxPoiHotelOperator op = new IxPoiHotelOperator(conn, (IxPoiHotel) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getRestaurants()) {
			IxPoiRestaurantOperator op = new IxPoiRestaurantOperator(conn,
					(IxPoiRestaurant) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getCarrentals()) {
			IxPoiCarrentalOperator op = new IxPoiCarrentalOperator(conn,
					(IxPoiCarrental) r);

			op.deleteRow2Sql(stmt);
		}
	}

	/**
	 * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatus() throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT '" + ixPoi.getRowId()
				+ "' as a, 2 as b,0 as c FROM dual) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
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
	 * poi操作修改poi状态为待作业 by wdb
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatusForAndroid() throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT '" + ixPoi.getRowId()
				+ "' as a, 1 as b," 
				+ freshFlag 
				+ " as c,'"
				+ rawFields
				+ "' as d,"
				+ "sysdate as e"
				+"  FROM dual) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.is_upload = T2.b,T1.raw_fields = T2.d,T1.upload_date = T2.e ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified,T1.is_upload,T1.raw_fields,T1.upload_date) VALUES(T2.a,T2.b,T2.c,T2.b,T2.d,T2.e)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
			conn.commit();
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
	
}
