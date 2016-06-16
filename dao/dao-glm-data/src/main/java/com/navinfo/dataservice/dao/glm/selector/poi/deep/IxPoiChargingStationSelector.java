package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
/**
 * 索引:POI 深度信息(充电站类) 
 * @author zhaokk
 *
 */
public class IxPoiChargingStationSelector implements ISelector {
	private Connection conn;

	public IxPoiChargingStationSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiChargingStation  ixPoiChargingStation = new IxPoiChargingStation();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixPoiChargingStation.tableName() + " WHERE charging_id = :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				this.setAttr(ixPoiChargingStation, resultSet);
				return ixPoiChargingStation;
			} else {
				throw new Exception("对应IxPoiChargingStation数据不存在不存在!");
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
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select * from ix_poi_chargingstation  WHERE poi_pid  = :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiChargingStation chargingStation = new IxPoiChargingStation();
				this.setAttr(chargingStation, resultSet);
				rows.add(chargingStation);
			} return rows;
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
 
	private void setAttr(IxPoiChargingStation chargingStation,ResultSet resultSet) throws SQLException{
		chargingStation.setPid(resultSet.getInt("advertise_id"));
		chargingStation.setPoiPid(resultSet.getInt("poi_pid"));
		chargingStation.setChargingType(resultSet.getInt("charging_type"));
		chargingStation.setChangeBrands(resultSet.getString("change_brands"));
		chargingStation.setChangeOpenType(resultSet.getInt("hange_open_type"));
		chargingStation.setChargingNum(resultSet.getInt("charging_num"));
		chargingStation.setServiceProv(resultSet.getString("service_prov"));
		chargingStation.setMemo(resultSet.getString("memo"));
		chargingStation.setPhotoName(resultSet.getString("photo_name"));
		chargingStation.setOpenHour(resultSet.getString("open_hour"));
		chargingStation.setParkingFees(resultSet.getInt("parking_fees"));
		chargingStation.setParkingInfo(resultSet.getString("parking_info"));
		chargingStation.setAvailableState(resultSet.getInt("available_state"));
		chargingStation.setRowId(resultSet.getString("row_id"));
		chargingStation.setuDate(resultSet.getString("u_date"));

	}
	
}
