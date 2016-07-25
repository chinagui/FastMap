package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;

/**
 * POI地址表selector
 * @author zhangxiaolong
 *
 */
public class IxPoiAddressSelector implements ISelector {
	
	private Connection conn;
	

	public IxPoiAddressSelector(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiAddress ixPoiAddress = new IxPoiAddress();

		String sql = "select * from "+ixPoiAddress.tableName()+" where name_id=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				setAttr(ixPoiAddress, resultSet);
			} else {
				
				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}

		return ixPoiAddress;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_address where poi_pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoiAddress ixPoiAddress = new IxPoiAddress();

				setAttr(ixPoiAddress, resultSet);

				rows.add(ixPoiAddress);
			}
		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}

		return rows;
	}

	private void setAttr(IxPoiAddress ixPoiAddress,ResultSet resultSet) throws SQLException
	{
		ixPoiAddress.setPid(resultSet.getInt("name_id"));
		
		ixPoiAddress.setNameGroupid(resultSet.getInt("name_groupid"));
		
		ixPoiAddress.setPoiPid(resultSet.getInt("poi_pid"));
		
		ixPoiAddress.setLangCode(resultSet.getString("lang_code"));
		
		ixPoiAddress.setSrcFlag(resultSet.getInt("src_flag"));
		
		ixPoiAddress.setFullname(resultSet.getString("fullname"));
		
		ixPoiAddress.setFullnamePhonetic(resultSet.getString("fullname_phonetic"));
		
		ixPoiAddress.setRoadname(resultSet.getString("roadname"));
		
		ixPoiAddress.setRoadnamePhonetic(resultSet.getString("roadname_phonetic"));
		
		ixPoiAddress.setAddrname(resultSet.getString("addrname"));
		
		ixPoiAddress.setAddrnamePhonetic(resultSet.getString("addrname_phonetic"));
		
		ixPoiAddress.setProvince(resultSet.getString("province"));
		
		ixPoiAddress.setCity(resultSet.getString("city"));
		
		ixPoiAddress.setCounty(resultSet.getString("county"));
		
		ixPoiAddress.setTown(resultSet.getString("town"));
		
		ixPoiAddress.setPlace(resultSet.getString("place"));
		
		ixPoiAddress.setStreet(resultSet.getString("street"));
		
		ixPoiAddress.setLandmark(resultSet.getString("landmark"));
		
		ixPoiAddress.setPrefix(resultSet.getString("prefix"));
		
		ixPoiAddress.setHousenum(resultSet.getString("housenum"));
		
		ixPoiAddress.setType(resultSet.getString("type"));
		
		ixPoiAddress.setSubnum(resultSet.getString("subnum"));
		
		ixPoiAddress.setSurfix(resultSet.getString("surfix"));
		
		ixPoiAddress.setEstab(resultSet.getString("estab"));
		
		ixPoiAddress.setBuilding(resultSet.getString("building"));
		
		ixPoiAddress.setFloor(resultSet.getString("floor"));
		
		ixPoiAddress.setUnit(resultSet.getString("unit"));
		
		ixPoiAddress.setRoom(resultSet.getString("room"));
		
		ixPoiAddress.setAddons(resultSet.getString("addons"));
		
		ixPoiAddress.setProvPhonetic(resultSet.getString("prov_phonetic"));
		
		ixPoiAddress.setCityPhonetic(resultSet.getString("city_phonetic"));
		
		ixPoiAddress.setCountyPhonetic(resultSet.getString("county_phonetic"));
		
		ixPoiAddress.setTownPhonetic(resultSet.getString("town_phonetic"));
		
		ixPoiAddress.setStreetPhonetic(resultSet.getString("street_phonetic"));
		
		ixPoiAddress.setPlacePhonetic(resultSet.getString("place_phonetic"));
		
		ixPoiAddress.setLandmarkPhonetic(resultSet.getString("landmark_phonetic"));
		
		ixPoiAddress.setPrefixPhonetic(resultSet.getString("prefix_phonetic"));
		
		ixPoiAddress.setHousenumPhonetic(resultSet.getString("housenum_phonetic"));
		
		ixPoiAddress.setTypePhonetic(resultSet.getString("type_phonetic"));
		
		ixPoiAddress.setSubnumPhonetic(resultSet.getString("subnum_phonetic"));
		
		ixPoiAddress.setSurfixPhonetic(resultSet.getString("surfix_phonetic"));
		
		ixPoiAddress.setEstabPhonetic(resultSet.getString("estab_phonetic"));
		
		ixPoiAddress.setBuildingPhonetic(resultSet.getString("building_phonetic"));
		
		ixPoiAddress.setFloorPhonetic(resultSet.getString("floor_phonetic"));
		
		ixPoiAddress.setUnitPhonetic(resultSet.getString("unit_phonetic"));
		
		ixPoiAddress.setRoomPhonetic(resultSet.getString("room_phonetic"));
		
		ixPoiAddress.setAddonsPhonetic(resultSet.getString("addons_phonetic"));
		
		ixPoiAddress.setRowId(resultSet.getString("row_id"));
		
		ixPoiAddress.setuDate(resultSet.getString("u_date"));
	}
	
	/**
	 * add by wangdongbin
	 * for android download
	 * @param id
	 * @return IxPoiAddress
	 * @throws Exception
	 */
	public List<IRow> loadByIdForAndroid(int id)throws Exception{
		List<IRow> rows = new ArrayList<IRow>();
		IxPoiAddress ixPoiAddress = new IxPoiAddress();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			String sql = "SELECT fullname FROM " + ixPoiAddress.tableName() + " where poi_pid=:1 AND name_groupid=1 AND lang_code='CHI' AND u_record!=2";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()){
				ixPoiAddress.setFullname(resultSet.getString("fullname"));
			}
			rows.add(ixPoiAddress);
			return rows;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}

	}
}
