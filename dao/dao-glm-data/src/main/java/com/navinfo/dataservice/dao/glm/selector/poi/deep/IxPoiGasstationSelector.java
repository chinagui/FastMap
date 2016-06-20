package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
/**
 * 索引:POI 深度信息(加油站类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiGasstationSelector implements ISelector {
	private Connection conn;

	public IxPoiGasstationSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiGasstation ixPoiGasstation = new IxPoiGasstation();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixPoiGasstation.tableName() + " WHERE  gasstation_id= :1 and  u_record !=2");

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
				this.setAttr(ixPoiGasstation, resultSet);
				return ixPoiGasstation;
			} else {
				throw new Exception("对应"+ixPoiGasstation.tableName()+"数据不存在不存在!");
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
				"select * from ix_poi_gasstation WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiGasstation ixPoiGasstation = new IxPoiGasstation();
				this.setAttr(ixPoiGasstation, resultSet);
				rows.add(ixPoiGasstation);
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
 
	private void setAttr(IxPoiGasstation ixPoiGasstation,ResultSet resultSet) throws SQLException{
		ixPoiGasstation.setPid(resultSet.getInt("gasstation_id"));
		ixPoiGasstation.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiGasstation.setServiceProv(resultSet.getString("service_prov"));
		ixPoiGasstation.setFuelType(resultSet.getString("fuel_type"));
		ixPoiGasstation.setOilType(resultSet.getString("oil_type"));
		ixPoiGasstation.setEgType(resultSet.getString("eg_type"));
		ixPoiGasstation.setMgType(resultSet.getString("mg_type"));
		ixPoiGasstation.setPayment(resultSet.getString("payment"));
		ixPoiGasstation.setService(resultSet.getString("service"));
		ixPoiGasstation.setMemo(resultSet.getString("memo"));
		ixPoiGasstation.setOpenHour(resultSet.getString("open_hour"));
		ixPoiGasstation.setPhotoName(resultSet.getString("photo_name"));
		ixPoiGasstation.setRowId(resultSet.getString("row_id"));
		ixPoiGasstation.setuDate(resultSet.getString("u_date"));
	}
	
	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select fuel_type,oil_type,eg_type,mg_type,payment,service,service_prov,open_hour,row_id from ix_poi_gasstation WHERE poi_pid  = :1 and  u_record !=2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiGasstation ixPoiGasstation = new IxPoiGasstation();
				ixPoiGasstation.setFuelType(resultSet.getString("fuel_type"));
				ixPoiGasstation.setOilType(resultSet.getString("oil_type"));
				ixPoiGasstation.setEgType(resultSet.getString("eg_type"));
				ixPoiGasstation.setMgType(resultSet.getString("mg_type"));
				ixPoiGasstation.setPayment(resultSet.getString("payment"));
				ixPoiGasstation.setService(resultSet.getString("service"));
				ixPoiGasstation.setServiceProv(resultSet.getString("service_prov"));
				ixPoiGasstation.setOpenHour(resultSet.getString("open_hour"));
				ixPoiGasstation.setRowId(resultSet.getString("row_id"));
				rows.add(ixPoiGasstation);
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
	
}
