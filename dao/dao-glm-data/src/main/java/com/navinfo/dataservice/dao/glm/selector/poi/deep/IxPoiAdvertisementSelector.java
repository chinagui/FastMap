package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
/**
 * 索引:POI 深度信息(广告类) 查询接口
 * @author zhaokk
 *
 */
public class IxPoiAdvertisementSelector implements ISelector {
	private Connection conn;

	public IxPoiAdvertisementSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiAdvertisement advertisement = new IxPoiAdvertisement();

		StringBuilder sb = new StringBuilder(
				"select * from " + advertisement.tableName() + " WHERE advertise_id  = :1 and  u_record !=2");

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
				this.setAttr(advertisement, resultSet);
				return advertisement;
			} else {
				throw new Exception("对应"+advertisement.tableName()+"数据不存在不存在!");
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
				"select * from ix_poi_advertisement  WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiAdvertisement advertisement = new IxPoiAdvertisement();
				this.setAttr(advertisement, resultSet);
				rows.add(advertisement);
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
 
	private void setAttr(IxPoiAdvertisement advertisement,ResultSet resultSet) throws SQLException{
		advertisement.setPid(resultSet.getInt("advertise_id"));
		advertisement.setPoiPid(resultSet.getInt("poi_pid"));
		advertisement.setLableText(resultSet.getString("label_text"));
		advertisement.setType(resultSet.getString("type"));
		advertisement.setPriority(resultSet.getInt("priority"));
		advertisement.setStartTime(resultSet.getString("start_time"));
		advertisement.setEndTime(resultSet.getString("end_time"));
		advertisement.setuDate(resultSet.getString("u_date"));
		advertisement.setRowId(resultSet.getString("row_id"));
	}
	
}
