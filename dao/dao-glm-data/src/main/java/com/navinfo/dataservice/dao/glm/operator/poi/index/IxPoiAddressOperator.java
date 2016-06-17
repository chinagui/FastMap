package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;

/**
 * POI地址表操作类
 * @author zhangxiaolong
 *
 */
public class IxPoiAddressOperator implements IOperator {
	
	private Connection conn;

	private IxPoiAddress ixPoiAddress;
	
	public IxPoiAddressOperator(Connection conn, IxPoiAddress ixPoiAddress) {
		this.conn = conn;
		this.ixPoiAddress = ixPoiAddress;
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
		StringBuilder sb = new StringBuilder("update " + ixPoiAddress.tableName() + " set u_record=3,u_date="+StringUtils.getCurrentTime()+",");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiAddress.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiAddress.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiAddress);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='" + String.valueOf(columnValue) + "',");
						}

					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
						sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");
					}

				}
			}
			sb.append(" where name_id= " + ixPoiAddress.getPid());

			sb.append("')");

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			pstmt = conn.prepareStatement(sql);

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
		ixPoiAddress.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiAddress.tableName());

		sb.append(
				"(NAME_ID, NAME_GROUPID, POI_PID, LANG_CODE, SRC_FLAG, FULLNAME, FULLNAME_PHONETIC, ROADNAME, ROADNAME_PHONETIC, ADDRNAME, "
				+ "ADDRNAME_PHONETIC, PROVINCE, CITY, COUNTY, TOWN, PLACE, STREET, LANDMARK,"
				+ " PREFIX, HOUSENUM, TYPE, SUBNUM, SURFIX, ESTAB, BUILDING, FLOOR, UNIT, ROOM, ADDONS, PROV_PHONETIC, CITY_PHONETIC, COUNTY_PHONETIC, TOWN_PHONETIC, STREET_PHONETIC, PLACE_PHONETIC, LANDMARK_PHONETIC, PREFIX_PHONETIC, HOUSENUM_PHONETIC, TYPE_PHONETIC, SUBNUM_PHONETIC, SURFIX_PHONETIC, ESTAB_PHONETIC, BUILDING_PHONETIC, FLOOR_PHONETIC, UNIT_PHONETIC, ROOM_PHONETIC, ADDONS_PHONETIC,U_DATE,U_RECORD,ROW_ID) values (");

		sb.append(ixPoiAddress.getPid());

		sb.append("," + ixPoiAddress.getNameGroupid());
		
		sb.append("," + ixPoiAddress.getPoiPid());
		
		sb.append("," + ixPoiAddress.getLangCode());
		
		sb.append("," + ixPoiAddress.getSrcFlag());
		
		sb.append(",'" + ixPoiAddress.getFullname()+"'");
		
		sb.append(",'" + ixPoiAddress.getFloorPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getRodename()+"'");
		
		sb.append(",'" + ixPoiAddress.getRoadnamePhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getAddrname()+"'");
		
		sb.append(",'" + ixPoiAddress.getAddrnamePhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getProvince()+"'");
		
		sb.append(",'" + ixPoiAddress.getCity()+"'");
		
		sb.append(",'" + ixPoiAddress.getCounty()+"'");
		
		sb.append(",'" + ixPoiAddress.getTown()+"'");
		
		sb.append(",'" + ixPoiAddress.getPlace()+"'");
		
		sb.append(",'" + ixPoiAddress.getStreet()+"'");
		
		sb.append(",'" + ixPoiAddress.getLandmark()+"'");
		
		sb.append(",'" + ixPoiAddress.getPrefix()+"'");
		
		sb.append(",'" + ixPoiAddress.getHousesum()+"'");
		
		sb.append(",'" + ixPoiAddress.getType()+"'");
		
		sb.append(",'" + ixPoiAddress.getSubnum()+"'");
		
		sb.append(",'" + ixPoiAddress.getSurfix()+"'");

		sb.append(",'" + ixPoiAddress.getEstab()+"'");
		
		sb.append(",'" + ixPoiAddress.getBuilding()+"'");
		
		sb.append(",'" + ixPoiAddress.getFloor()+"'");
		
		sb.append(",'" + ixPoiAddress.getUnit()+"'");
		
		sb.append(",'" + ixPoiAddress.getRoom()+"'");
		
		sb.append(",'" + ixPoiAddress.getAddons()+"'");
		
		sb.append(",'" + ixPoiAddress.getProvPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getCityPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getCountyPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getTownPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getStreetPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getPlacePhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getLandmarkPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getPrefixPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getHousenumPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getTypePhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getSubsumPhonetic()+"'");

		sb.append(",'" + ixPoiAddress.getSurfixPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getEstabPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getBuildingPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getFloorPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getUnitPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getRoomPhonetic()+"'");
		
		sb.append(",'" + ixPoiAddress.getAddonsPhonetic()+"'");
		
		sb.append(",'" + StringUtils.getCurrentTime()+"'");
		
		sb.append(",1,'" + ixPoiAddress.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiAddress.tableName() + " set u_record=2,u_date="+StringUtils.getCurrentTime()+" where name_id="
				
				+ ixPoiAddress.getPid();

		stmt.addBatch(sql);

	}

}
