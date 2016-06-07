package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
/**
 * 索引:POI 深度信息停车场类查询接口
 * @author zhaokk
 *
 */
public class IxPoiParkingSelector implements ISelector {
	private Connection conn;

	public IxPoiParkingSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiParking ixPoiParking = new IxPoiParking();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixPoiParking.tableName() + " WHERE  parking_id= :1 and  u_record !=2");

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
				this.setAttr(ixPoiParking, resultSet);
				return ixPoiParking;
			} else {
				throw new Exception("对应"+ixPoiParking.tableName()+"数据不存在不存在!");
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
				IxPoiParking ixPoiParking = new IxPoiParking();
				this.setAttr(ixPoiParking, resultSet);
				rows.add(ixPoiParking);
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
	private void setAttr(IxPoiParking ixPoiParking,ResultSet resultSet) throws SQLException{
		ixPoiParking.setPid(resultSet.getInt("parking_id"));
		ixPoiParking.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiParking.setParkingType(resultSet.getString("parking_type"));
		ixPoiParking.setTollStd(resultSet.getString("toll_std"));
		ixPoiParking.setTollWay(resultSet.getString("toll_way"));
		ixPoiParking.setTollDes(resultSet.getString("toll_des"));
		ixPoiParking.setPayment(resultSet.getString("payment"));
		ixPoiParking.setRemark(resultSet.getString("remark"));
		ixPoiParking.setSource(resultSet.getString("source"));
		ixPoiParking.setOpenTime(resultSet.getString("open_time"));
		ixPoiParking.setTotalNum(resultSet.getInt("total_num"));
		ixPoiParking.setWorkTime(resultSet.getString("work_time"));
		ixPoiParking.setResHigh(resultSet.getDouble("res_high"));
		ixPoiParking.setResWeigh(resultSet.getDouble("res_weigh"));
		ixPoiParking.setResWidth(resultSet.getDouble("res_width"));
		ixPoiParking.setVehicle(resultSet.getInt("vehicle"));
		ixPoiParking.setPhotoName(resultSet.getString("photo_name"));
		ixPoiParking.setWomenNum(resultSet.getInt("women_num"));
		ixPoiParking.setHandicapNum(resultSet.getInt("handicap_num"));
		ixPoiParking.setMiniNum(resultSet.getInt("mini_num"));
		ixPoiParking.setVipNum(resultSet.getInt("vip_num"));
		ixPoiParking.setHaveSpecialplace("have_specialplace");
		ixPoiParking.setCertificate(resultSet.getInt("certificate"));
		ixPoiParking.setRowId(resultSet.getString("row_id"));

	}
	
}
