package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
/**
 * 索引:POI 深度信息停车场类查询接口
 * @author zhaokk
 *
 */
public class IxPoiParkingSelector extends AbstractSelector {
	private Connection conn;

	public IxPoiParkingSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiParking.class);
	}

	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		IxPoiParking ixPoiParking = new IxPoiParking();

		StringBuilder sb = new StringBuilder(
				"select toll_std,toll_des,toll_way,open_tiime,total_num,payment,remark,parking_type,res_high,res_width,res_weigh,certificate,vehicle,have_specialplace,women_num,handicap_num,");
		sb.append("mini_num,vip_num,row_id ");
		sb.append(" from " + ixPoiParking.tableName() + " WHERE poi_pid  = :1 and  u_record !=2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				ixPoiParking.setParkingType(resultSet.getString("parking_type"));
				ixPoiParking.setTollStd(resultSet.getString("toll_std"));
				ixPoiParking.setTollWay(resultSet.getString("toll_way"));
				ixPoiParking.setTollDes(resultSet.getString("toll_des"));
				ixPoiParking.setPayment(resultSet.getString("payment"));
				ixPoiParking.setRemark(resultSet.getString("remark"));
				ixPoiParking.setOpenTiime(resultSet.getString("open_tiime"));
				ixPoiParking.setTotalNum(resultSet.getInt("total_num"));
				ixPoiParking.setResHigh(resultSet.getDouble("res_high"));
				ixPoiParking.setResWeigh(resultSet.getDouble("res_weigh"));
				ixPoiParking.setResWidth(resultSet.getDouble("res_width"));
				ixPoiParking.setVehicle(resultSet.getInt("vehicle"));
				ixPoiParking.setWomenNum(resultSet.getInt("women_num"));
				ixPoiParking.setHandicapNum(resultSet.getInt("handicap_num"));
				ixPoiParking.setMiniNum(resultSet.getInt("mini_num"));
				ixPoiParking.setVipNum(resultSet.getInt("vip_num"));
				ixPoiParking.setHaveSpecialplace("have_specialplace");
				ixPoiParking.setCertificate(resultSet.getInt("certificate"));
				ixPoiParking.setRowId(resultSet.getString("row_id"));
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
}
