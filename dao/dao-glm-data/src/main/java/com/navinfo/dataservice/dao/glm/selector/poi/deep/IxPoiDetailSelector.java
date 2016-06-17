package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
/**
 * 索引:POI 深度信息(充电桩类)
 * @author zhaokk
 *
 */
public class IxPoiDetailSelector implements ISelector {
	private Connection conn;

	public IxPoiDetailSelector(Connection conn) {
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
				"select * from ix_poi_detail  WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiDetail ixPoiDetail= new IxPoiDetail();
				this.setAttr(ixPoiDetail, resultSet);
				rows.add(ixPoiDetail);
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

	private void setAttr(IxPoiDetail ixPoiDetail,ResultSet resultSet) throws SQLException{
		
		ixPoiDetail.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiDetail.setWebsite(resultSet.getString("web_site"));
		ixPoiDetail.setFax(resultSet.getString("fax"));
		ixPoiDetail.setStarHotel(resultSet.getString("star_hotel"));
		ixPoiDetail.setBriefDesc(resultSet.getString("brief_desc"));
		ixPoiDetail.setAdverFlag(resultSet.getInt("adver_flag"));
	    ixPoiDetail.setPhotoName(resultSet.getString("photo_name"));
	    ixPoiDetail.setReserved(resultSet.getString("reserved"));
	    ixPoiDetail.setMemo(resultSet.getString("memo"));
	    ixPoiDetail.setHwEntryExit(resultSet.getInt("hw_entryexit"));
	    ixPoiDetail.setPayCard(resultSet.getInt("paycard"));
	    ixPoiDetail.setCardType(resultSet.getString("cardtype"));
	    ixPoiDetail.setHospitalClass(resultSet.getInt("hospital_class"));
	    ixPoiDetail.setRowId(resultSet.getString("rowId"));
	    ixPoiDetail.setuDate(resultSet.getString("u_date"));

	}
	
}
