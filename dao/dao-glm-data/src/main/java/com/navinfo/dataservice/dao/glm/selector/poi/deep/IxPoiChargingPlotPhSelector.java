package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlotPh;
/**
 * 索引:POI 深度信息(充电桩类-照片) 
 * @author zhaokk
 *
 */
public class IxPoiChargingPlotPhSelector implements ISelector {
	private Connection conn;

	public IxPoiChargingPlotPhSelector(Connection conn) {
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
				"select * from ix_poi_chargingplot_ph  WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiChargingPlotPh ixPoiChargingPlotPh= new IxPoiChargingPlotPh();
				this.setAttr(ixPoiChargingPlotPh, resultSet);
				rows.add(ixPoiChargingPlotPh);
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

	private void setAttr(IxPoiChargingPlotPh chargingPlotPh,ResultSet resultSet) throws SQLException{
		
		chargingPlotPh.setPoiPid(resultSet.getInt("poi_pid"));
		chargingPlotPh.setPhotoName(resultSet.getString("photo_name"));
		chargingPlotPh.setRowId(resultSet.getString("rowId"));

	}
	
}
