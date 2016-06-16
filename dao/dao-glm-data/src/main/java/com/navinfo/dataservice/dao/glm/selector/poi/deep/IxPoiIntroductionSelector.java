package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
/**
 * 索引:POI 深度信息(简介查询接口
 * @author zhaokk
 *
 */
public class IxPoiIntroductionSelector implements ISelector {
	private Connection conn;

	public IxPoiIntroductionSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiIntroduction ixIntroduction = new IxPoiIntroduction();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixIntroduction.tableName() + " WHERE  introduction_id= :1 and  u_record !=2");

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
				this.setAttr(ixIntroduction, resultSet);
				return ixIntroduction;
			} else {
				throw new Exception("对应"+ixIntroduction.tableName()+"数据不存在不存在!");
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
				"select * from ix_poi_introduction WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiIntroduction ixPoiIntroduction= new IxPoiIntroduction();
				this.setAttr(ixPoiIntroduction, resultSet);
				rows.add(ixPoiIntroduction);
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
	private void setAttr(IxPoiIntroduction ixPoiIntroduction,ResultSet resultSet) throws SQLException{
		ixPoiIntroduction.setPid(resultSet.getInt("introduction_id"));
		ixPoiIntroduction.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiIntroduction.setIntroduction(resultSet.getString("introduction"));
		ixPoiIntroduction.setIntroductionEng(resultSet.getString("introduction_eng"));
		ixPoiIntroduction.setWebsite(resultSet.getString("web_site"));
		ixPoiIntroduction.setNeighbor(resultSet.getString("neighbor"));
		ixPoiIntroduction.setNeighborEng(resultSet.getString("neighbor_eng"));
		ixPoiIntroduction.setTraffic(resultSet.getString("traffic"));
		ixPoiIntroduction.setTrafficEng(resultSet.getString("traffic_eng"));
		ixPoiIntroduction.setRowId(resultSet.getString("row_id"));
		ixPoiIntroduction.setuDate(resultSet.getString("u_date"));
	}
	
}
