package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParentForAndroid;

public class IxPoiParentHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> parentMap = new HashMap<Long, List<IRow>>();
		
		try {
			while (rs.next()){
				List<IRow> parentList = new ArrayList<IRow>();
				IxPoiParentForAndroid ixPoiParent = new IxPoiParentForAndroid();
				ixPoiParent.setPoiNum(rs.getString("poi_num"));
				parentList.add(ixPoiParent);
				parentMap.put(rs.getLong("child_poi_pid"), parentList);
			}
			return parentMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
