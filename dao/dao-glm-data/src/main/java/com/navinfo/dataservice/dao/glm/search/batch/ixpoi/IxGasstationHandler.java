package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;

public class IxGasstationHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> gasstationMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> gasstationList = new ArrayList<IRow>();
				IxPoiGasstation ixPoiGasstation = new IxPoiGasstation();
				ixPoiGasstation.setFuelType(rs.getString("fuel_type"));
				ixPoiGasstation.setOilType(rs.getString("oil_type"));
				ixPoiGasstation.setEgType(rs.getString("eg_type"));
				ixPoiGasstation.setMgType(rs.getString("mg_type"));
				ixPoiGasstation.setPayment(rs.getString("payment"));
				ixPoiGasstation.setService(rs.getString("service"));
				ixPoiGasstation.setServiceProv(rs.getString("service_prov"));
				ixPoiGasstation.setOpenHour(rs.getString("open_hour"));
				ixPoiGasstation.setRowId(rs.getString("row_id"));
				
				if (gasstationMap.containsKey(rs.getLong("poi_pid"))) {
					gasstationList = gasstationMap.get(rs.getLong("poi_pid"));
					gasstationList.add(ixPoiGasstation);
					gasstationMap.put(rs.getLong("poi_pid"), gasstationList);
				} else {
					gasstationList.add(ixPoiGasstation);
					gasstationMap.put(rs.getLong("poi_pid"), gasstationList);
				}
			}
			return gasstationMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
