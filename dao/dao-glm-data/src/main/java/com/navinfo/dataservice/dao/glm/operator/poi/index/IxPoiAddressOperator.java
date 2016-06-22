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
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getFullname()))
		{
			sb.append(",'" + ixPoiAddress.getFullname()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getFloorPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getFloorPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getRodename()))
		{
			sb.append(",'" + ixPoiAddress.getRodename()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getRoadnamePhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getRoadnamePhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getAddrname()))
		{
			sb.append(",'" + ixPoiAddress.getAddrname()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getAddrnamePhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getAddrnamePhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getProvince()))
		{
			sb.append(",'" + ixPoiAddress.getProvince()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getCity()))
		{
			sb.append(",'" + ixPoiAddress.getCity()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getCounty()))
		{
			sb.append(",'" + ixPoiAddress.getCounty()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getTown()))
		{
			sb.append(",'" + ixPoiAddress.getTown()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getPlace()))
		{
			sb.append(",'" + ixPoiAddress.getPlace()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getStreet()))
		{
			sb.append(",'" + ixPoiAddress.getStreet()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getLandmark()))
		{
			sb.append(",'" + ixPoiAddress.getLandmark()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getPrefix()))
		{
			sb.append(",'" + ixPoiAddress.getPrefix()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getHousesum()))
		{
			sb.append(",'" + ixPoiAddress.getHousesum()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getType()))
		{
			sb.append(",'" + ixPoiAddress.getType()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getSubnum()))
		{
			sb.append(",'" + ixPoiAddress.getSubnum()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getSurfix()))
		{
			sb.append(",'" + ixPoiAddress.getSurfix()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getEstab()))
		{
			sb.append(",'" + ixPoiAddress.getEstab()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getBuilding()))
		{
			sb.append(",'" + ixPoiAddress.getBuilding()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getFloor()))
		{
			sb.append(",'" + ixPoiAddress.getFloor()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getUnit()))
		{
			sb.append(",'" + ixPoiAddress.getUnit()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getRoom()))
		{
			sb.append(",'" + ixPoiAddress.getRoom()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getAddons()))
		{
			sb.append(",'" + ixPoiAddress.getAddons()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getProvPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getProvPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getCityPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getCityPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getCountyPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getCountyPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getTownPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getTownPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getStreetPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getStreetPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getPlacePhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getPlacePhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getLandmarkPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getLandmarkPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getPrefixPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getPrefixPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getHousenumPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getHousenumPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getTypePhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getTypePhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getSubsumPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getSubsumPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getSurfixPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getSurfixPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getEstabPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getEstabPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}

		if(StringUtils.isNotEmpty(ixPoiAddress.getBuildingPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getBuildingPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getFloorPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getFloorPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getUnitPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getUnitPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getRoomPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getRoomPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		if(StringUtils.isNotEmpty(ixPoiAddress.getAddonsPhonetic()))
		{
			sb.append(",'" + ixPoiAddress.getAddonsPhonetic()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
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
