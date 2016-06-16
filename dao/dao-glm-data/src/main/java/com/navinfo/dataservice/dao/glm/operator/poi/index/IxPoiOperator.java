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

	public IxPoiOperator(Connection conn, IxPoi ixPoi) {
		this.conn = conn;
		this.ixPoi = ixPoi;
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
		StringBuilder sb = new StringBuilder("update " + ixPoi.tableName() + " set u_record=3,u_date="+StringUtils.getCurrentTime()+",");

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

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='" + String.valueOf(columnValue) + "',");
						}
						isChanged = true;
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
						sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");

						isChanged = true;
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");

						isChanged = true;
					}

				} else if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value, 0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					if (!StringUtils.isStringSame(oldWkt, newWkt)) {
						sb.append("geometry=sdo_geometry('" + String.valueOf(newWkt) + "',8307),");

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
		ixPoi.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoi.tableName());

		sb.append("(PID, KIND_CODE, GEOMETRY, X_GUIDE, Y_GUIDE, LINK_PID, SIDE, "
				+ "NAME_GROUPID, ROAD_FLAG, PMESH_ID, ADMIN_REAL, IMPORTANCE, CHAIN, "
				+ "AIRPORT_CODE, ACCESS_FLAG, OPEN_24H, MESH_ID_5K, MESH_ID, REGION_ID, "
				+ "POST_CODE, EDIT_FLAG, DIF_GROUPID, RESERVED, STATE, FIELD_STATE, "
				+ "LABEL, TYPE, ADDRESS_FLAG, EX_PRIORITY, EDITION_FLAG, POI_MEMO, "
				+ "OLD_BLOCKCODE, OLD_NAME, OLD_ADDRESS, OLD_KIND, POI_NUM, LOG, TASK_ID, "
				+ "DATA_VERSION, FIELD_TASK_ID, VERIFIED_FLAG, COLLECT_TIME, "
				+ "GEO_ADJUST_FLAG, FULL_ATTR_FLAG, OLD_X_GUIDE, OLD_Y_GUIDE, U_RECORD, " + "ROW_ID) values (");

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

		sb.append(",'" + ixPoi.getChain() + "'");

		sb.append("," + ixPoi.getAirportCode());

		sb.append("," + ixPoi.getAccessFlag());

		sb.append("," + ixPoi.getOpen24h());

		sb.append("," + ixPoi.getMeshId5k());

		sb.append("," + ixPoi.getMeshId());

		sb.append("," + ixPoi.getRegionId());

		sb.append(",'" + ixPoi.getPostCode() + "'");

		sb.append("," + ixPoi.getEditFlag());

		sb.append(",'" + ixPoi.getDifGroupid() + "'");

		sb.append(",'" + ixPoi.getReserved() + "'");

		sb.append("," + ixPoi.getState());

		sb.append(",'" + ixPoi.getFieldState() + "'");

		sb.append(",'" + ixPoi.getLabel() + "'");

		sb.append("," + ixPoi.getType());

		sb.append("," + ixPoi.getAddressFlag());

		sb.append(",'" + ixPoi.getExPriority() + "'");

		sb.append("," + ixPoi.getEditFlag());

		sb.append(",'" + ixPoi.getPoiMemo() + "'");

		sb.append(",'" + ixPoi.getOldBlockcode() + "'");

		sb.append(",'" + ixPoi.getOldName() + "'");

		sb.append(",'" + ixPoi.getOldAddress() + "'");

		sb.append(",'" + ixPoi.getOldKind() + "'");

		sb.append(",'" + ixPoi.getPoiNum() + "'");

		sb.append(",'" + ixPoi.getLog() + "'");

		sb.append("," + ixPoi.getTaskId());

		sb.append(",'" + ixPoi.getDataVersion() + "'");

		sb.append("," + ixPoi.getFieldTaskId());

		sb.append("," + ixPoi.getVerifiedFlag());

		sb.append(",'" + ixPoi.getCollectTime() + "'");

		sb.append("," + ixPoi.getGeoAdjustFlag());

		sb.append("," + ixPoi.getFullAttrFlag());

		sb.append("," + ixPoi.getOldXGuide());

		sb.append("," + ixPoi.getOldYGuide());

		sb.append(",1,'" + ixPoi.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : ixPoi.getFlags()) {
			IxPoiFlagOperator op = new IxPoiFlagOperator(conn, (IxPoiFlag) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getContacts()) {
			IxPoiContactOperator op = new IxPoiContactOperator(conn, (IxPoiContact) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAddresses()) {
			IxPoiAddressOperator op = new IxPoiAddressOperator(conn, (IxPoiAddress) r);

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
			IxPoiEntryImageOperator op = new IxPoiEntryImageOperator(conn, (IxPoiEntryimage) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getNames()) {
			IxPoiNameOperator op = new IxPoiNameOperator(conn, (IxPoiName) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getParkings()) {
			IxPoiParkingOperator op = new IxPoiParkingOperator(conn, (IxPoiParking) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getTourroutes()) {
			IxPoiTourroutOperator op = new IxPoiTourroutOperator(conn, (IxPoiTourroute) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getEvents()) {
			IxPoiEventOperator op = new IxPoiEventOperator(conn, (IxPoiEvent) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getDetails()) {
			IxPoiDetailOperator op = new IxPoiDetailOperator(conn, (IxPoiDetail) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn, (IxPoiBusinessTime) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn, (IxPoiBusinessTime) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingstations()) {
			IxPoiChargingStationOperator op = new IxPoiChargingStationOperator(conn, (IxPoiChargingStation) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotOperator op = new IxPoiChargingPlotOperator(conn, (IxPoiChargingPlot) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(conn, (IxPoiChargingPlotPh) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplotPhs()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(conn, (IxPoiChargingPlotPh) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBuildings()) {
			IxPoiBuildingOperator op = new IxPoiBuildingOperator(conn, (IxPoiBuilding) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAdvertisements()) {
			IxPoiAdvertisementOperator op = new IxPoiAdvertisementOperator(conn, (IxPoiAdvertisement) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getGasstations()) {
			IxPoiGasstationOperator op = new IxPoiGasstationOperator(conn, (IxPoiGasstation) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getIntroductions()) {
			IxPoiIntroductionOperator op = new IxPoiIntroductionOperator(conn, (IxPoiIntroduction) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn, (IxPoiAttraction) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn, (IxPoiAttraction) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getHotels()) {
			IxPoiHotelOperator op = new IxPoiHotelOperator(conn, (IxPoiHotel) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getRestaurants()) {
			IxPoiRestaurantOperator op = new IxPoiRestaurantOperator(conn, (IxPoiRestaurant) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getCarrentals()) {
			IxPoiCarrentalOperator op = new IxPoiCarrentalOperator(conn, (IxPoiCarrental) r);

			op.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoi.tableName() + " set u_record=2 where pid=" + ixPoi.getPid();

		stmt.addBatch(sql);

		for (IRow r : ixPoi.getFlags()) {
			IxPoiFlagOperator op = new IxPoiFlagOperator(conn, (IxPoiFlag) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getContacts()) {
			IxPoiContactOperator op = new IxPoiContactOperator(conn, (IxPoiContact) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAddresses()) {
			IxPoiAddressOperator op = new IxPoiAddressOperator(conn, (IxPoiAddress) r);

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
			IxPoiEntryImageOperator op = new IxPoiEntryImageOperator(conn, (IxPoiEntryimage) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getNames()) {
			IxPoiNameOperator op = new IxPoiNameOperator(conn, (IxPoiName) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getParkings()) {
			IxPoiParkingOperator op = new IxPoiParkingOperator(conn, (IxPoiParking) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getTourroutes()) {
			IxPoiTourroutOperator op = new IxPoiTourroutOperator(conn, (IxPoiTourroute) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getEvents()) {
			IxPoiEventOperator op = new IxPoiEventOperator(conn, (IxPoiEvent) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getDetails()) {
			IxPoiDetailOperator op = new IxPoiDetailOperator(conn, (IxPoiDetail) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn, (IxPoiBusinessTime) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBusinesstimes()) {
			IxPoiBusinessTimeOperator op = new IxPoiBusinessTimeOperator(conn, (IxPoiBusinessTime) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingstations()) {
			IxPoiChargingStationOperator op = new IxPoiChargingStationOperator(conn, (IxPoiChargingStation) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotOperator op = new IxPoiChargingPlotOperator(conn, (IxPoiChargingPlot) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplots()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(conn, (IxPoiChargingPlotPh) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getChargingplotPhs()) {
			IxPoiChargingPlotPhOperator op = new IxPoiChargingPlotPhOperator(conn, (IxPoiChargingPlotPh) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getBuildings()) {
			IxPoiBuildingOperator op = new IxPoiBuildingOperator(conn, (IxPoiBuilding) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAdvertisements()) {
			IxPoiAdvertisementOperator op = new IxPoiAdvertisementOperator(conn, (IxPoiAdvertisement) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getGasstations()) {
			IxPoiGasstationOperator op = new IxPoiGasstationOperator(conn, (IxPoiGasstation) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getIntroductions()) {
			IxPoiIntroductionOperator op = new IxPoiIntroductionOperator(conn, (IxPoiIntroduction) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn, (IxPoiAttraction) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getAttractions()) {
			IxPoiAttractionOperator op = new IxPoiAttractionOperator(conn, (IxPoiAttraction) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getHotels()) {
			IxPoiHotelOperator op = new IxPoiHotelOperator(conn, (IxPoiHotel) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getRestaurants()) {
			IxPoiRestaurantOperator op = new IxPoiRestaurantOperator(conn, (IxPoiRestaurant) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoi.getCarrentals()) {
			IxPoiCarrentalOperator op = new IxPoiCarrentalOperator(conn, (IxPoiCarrental) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
