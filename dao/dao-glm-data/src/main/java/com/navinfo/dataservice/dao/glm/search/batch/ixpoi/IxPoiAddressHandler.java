package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;

public class IxPoiAddressHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> addressMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> addressList = new ArrayList<IRow>();
				IxPoiAddress ixPoiAddress = new IxPoiAddress();
				ixPoiAddress.setFullname(rs.getString("fullname"));
				ixPoiAddress.setFloor(rs.getString("floor"));
				addressList.add(ixPoiAddress);
				addressMap.put(rs.getLong("poi_pid"), addressList);
			}
			return addressMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
