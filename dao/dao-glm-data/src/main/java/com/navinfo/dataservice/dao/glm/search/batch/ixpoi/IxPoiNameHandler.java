package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;

public class IxPoiNameHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> nameMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> nameList = new ArrayList<IRow>();
				IxPoiName ixPoiName = new IxPoiName();
				ixPoiName.setName(rs.getString("name"));
				nameList.add(ixPoiName);
				nameMap.put(rs.getLong("poi_pid"), nameList);
			}
			return nameMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
