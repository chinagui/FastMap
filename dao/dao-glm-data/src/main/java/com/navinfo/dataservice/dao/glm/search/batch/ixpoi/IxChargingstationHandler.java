package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;

public class IxChargingstationHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> chargingstationMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> chargingstationList = new ArrayList<IRow>();
				IxPoiChargingStation ixPoiChargingstation = new IxPoiChargingStation();
				ixPoiChargingstation.setChargingType(rs.getInt("charging_type"));
				ixPoiChargingstation.setChangeBrands(rs.getString("change_brands"));
				ixPoiChargingstation.setChangeOpenType(rs.getString("change_open_type"));
				ixPoiChargingstation.setServiceProv(rs.getString("service_prov"));
				ixPoiChargingstation.setChargingNum(rs.getInt("charging_num"));
				ixPoiChargingstation.setOpenHour(rs.getString("open_hour"));
				ixPoiChargingstation.setParkingFees(rs.getInt("parking_fees"));
				ixPoiChargingstation.setParkingInfo(rs.getString("parking_info"));
				ixPoiChargingstation.setAvailableState(rs.getInt("available_state"));
				ixPoiChargingstation.setRowId(rs.getString("row_id"));
				
				if (chargingstationMap.containsKey(rs.getLong("poi_pid"))) {
					chargingstationList = chargingstationMap.get(rs.getLong("poi_pid"));
					chargingstationList.add(ixPoiChargingstation);
					chargingstationMap.put(rs.getLong("poi_pid"), chargingstationList);
				} else {
					chargingstationList.add(ixPoiChargingstation);
					chargingstationMap.put(rs.getLong("poi_pid"), chargingstationList);
				}
			}
			return chargingstationMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
