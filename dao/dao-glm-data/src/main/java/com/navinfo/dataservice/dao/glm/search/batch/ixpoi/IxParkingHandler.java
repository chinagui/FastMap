package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;

public class IxParkingHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> parkingMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> parkingList = new ArrayList<IRow>();
				IxPoiParking ixPoiParking = new IxPoiParking();
				ixPoiParking.setParkingType(rs.getString("parking_type"));
				ixPoiParking.setTollStd(rs.getString("toll_std"));
				ixPoiParking.setTollWay(rs.getString("toll_way"));
				ixPoiParking.setTollDes(rs.getString("toll_des"));
				ixPoiParking.setPayment(rs.getString("payment"));
				ixPoiParking.setRemark(rs.getString("remark"));
				ixPoiParking.setOpenTiime(rs.getString("open_tiime"));
				ixPoiParking.setTotalNum(rs.getInt("total_num"));
				ixPoiParking.setResHigh(rs.getDouble("res_high"));
				ixPoiParking.setResWeigh(rs.getDouble("res_weigh"));
				ixPoiParking.setResWidth(rs.getDouble("res_width"));
				ixPoiParking.setVehicle(rs.getLong("vehicle"));
				ixPoiParking.setWomenNum(rs.getInt("women_num"));
				ixPoiParking.setHandicapNum(rs.getInt("handicap_num"));
				ixPoiParking.setMiniNum(rs.getInt("mini_num"));
				ixPoiParking.setVipNum(rs.getInt("vip_num"));
				ixPoiParking.setHaveSpecialplace(rs.getString("have_specialplace"));
				ixPoiParking.setCertificate(rs.getInt("certificate"));
				ixPoiParking.setRowId(rs.getString("row_id"));
				
				if (parkingMap.containsKey(rs.getLong("poi_pid"))) {
					parkingList = parkingMap.get(rs.getLong("poi_pid"));
					parkingList.add(ixPoiParking);
					parkingMap.put(rs.getLong("poi_pid"), parkingList);
				} else {
					parkingList.add(ixPoiParking);
					parkingMap.put(rs.getLong("poi_pid"), parkingList);
				}
			}
			return parkingMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
