package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
/**
 * 索引:POI 深度信息(建筑物和租户的楼层信息) 查询接口
 * @author zhaokk
 *
 */
public class IxPoiBuildingSelector implements ISelector {
	private Connection conn;

	public IxPoiBuildingSelector(Connection conn) {
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
				"select * from ix_poi_building  WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiBuilding ixPoiBuilding = new IxPoiBuilding();
				this.setAttr(ixPoiBuilding, resultSet);
				rows.add(ixPoiBuilding);
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

	private void setAttr(IxPoiBuilding ixPoiBuilding,ResultSet resultSet) throws SQLException{
		ixPoiBuilding.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiBuilding.setFloorUsed(resultSet.getString("floor_used"));
		ixPoiBuilding.setFloorEmpty(resultSet.getString("floor_empty"));
		ixPoiBuilding.setMemo(resultSet.getString("memo"));
		ixPoiBuilding.setRowId(resultSet.getString("row_id"));

	}
	
}
