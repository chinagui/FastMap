package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;

public class IxChargingplotHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> chargingplotMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> chargingplotList = new ArrayList<IRow>();
				IxPoiChargingPlot ixPoiChargingplot = new IxPoiChargingPlot();
				ixPoiChargingplot.setGroupId(rs.getInt("group_id"));
				ixPoiChargingplot.setAcdc(rs.getInt("acdc"));
				ixPoiChargingplot.setPlugType(rs.getString("plug_type"));
				ixPoiChargingplot.setPower(rs.getString("power"));
				ixPoiChargingplot.setVoltage(rs.getString("voltage"));
				ixPoiChargingplot.setCurrent(rs.getString("current"));
				ixPoiChargingplot.setMode(rs.getInt("mode"));
				ixPoiChargingplot.setCount(rs.getInt("count"));
				ixPoiChargingplot.setPlugNum(rs.getInt("plug_num"));
				ixPoiChargingplot.setPrices(rs.getString("prices"));
				ixPoiChargingplot.setOpenType(rs.getString("open_type"));
				ixPoiChargingplot.setAvailableState(rs.getInt("available_state"));
				ixPoiChargingplot.setManufacturer(rs.getString("manufacturer"));
				ixPoiChargingplot.setFactoryNum(rs.getString("factory_num"));
				ixPoiChargingplot.setPlotNum(rs.getString("plot_num"));
				ixPoiChargingplot.setProductNum(rs.getString("product_num"));
				ixPoiChargingplot.setParkingNum(rs.getString("parking_num"));
				ixPoiChargingplot.setFloor(rs.getInt("floor"));
				ixPoiChargingplot.setLocationType(rs.getInt("location_type"));
				ixPoiChargingplot.setPayment(rs.getString("payment"));
				ixPoiChargingplot.setRowId(rs.getString("row_id"));
				
				if (chargingplotMap.containsKey(rs.getLong("poi_pid"))) {
					chargingplotList = chargingplotMap.get(rs.getLong("poi_pid"));
					chargingplotList.add(ixPoiChargingplot);
					chargingplotMap.put(rs.getLong("poi_pid"), chargingplotList);
				} else {
					chargingplotList.add(ixPoiChargingplot);
					chargingplotMap.put(rs.getLong("poi_pid"), chargingplotList);
				}
			}
			return chargingplotMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
