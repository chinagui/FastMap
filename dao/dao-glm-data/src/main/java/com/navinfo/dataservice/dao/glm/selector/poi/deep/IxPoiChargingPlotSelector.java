package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
/**
 * 索引:POI 深度信息(充电桩类)
 * @author zhaokk
 *
 */
public class IxPoiChargingPlotSelector implements ISelector {
	private Connection conn;

	public IxPoiChargingPlotSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
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
				"select * from ix_poi_chargingplot  WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiChargingPlot ixPoiChargingPlot= new IxPoiChargingPlot();
				this.setAttr(ixPoiChargingPlot, resultSet);
				rows.add(ixPoiChargingPlot);
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

	private void setAttr(IxPoiChargingPlot chargingPlot,ResultSet resultSet) throws SQLException{
		
		chargingPlot.setPoiPid(resultSet.getInt("poi_pid"));
		chargingPlot.setGroupId(resultSet.getInt("group_id"));
		chargingPlot.setCount(resultSet.getInt("count"));
		chargingPlot.setAcdc(resultSet.getInt("acdc"));
		chargingPlot.setPlugType(resultSet.getString("plug_type"));
		chargingPlot.setPower(resultSet.getString("power"));
		chargingPlot.setVoltage(resultSet.getString("voltage"));
		chargingPlot.setCurrent(resultSet.getString("current"));
		chargingPlot.setMode(resultSet.getInt("mode"));
		chargingPlot.setMemo(resultSet.getString("memo"));
		chargingPlot.setPlugNum(resultSet.getInt("plug_num"));
		chargingPlot.setPrices(resultSet.getString("prices"));
		chargingPlot.setOpenType(resultSet.getString("open_type"));
		chargingPlot.setAvailableState(resultSet.getInt("available_state"));
		chargingPlot.setManufacturer(resultSet.getString("manufacturer"));
		chargingPlot.setFactoryNum(resultSet.getString("factory_num"));
		chargingPlot.setPlotNum(resultSet.getString("plot_num"));
		chargingPlot.setProductNum(resultSet.getString("product_num"));
		chargingPlot.setParkingNum(resultSet.getString("parking_num"));
		chargingPlot.setFloor(resultSet.getInt("floor"));
		chargingPlot.setLocationType(resultSet.getInt("location_type"));
		chargingPlot.setPayment(resultSet.getString("payment"));
		chargingPlot.setRowId(resultSet.getString("rowId"));
		chargingPlot.setuDate(resultSet.getString("u_date"));

	}
	
}
