package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildrenForAndroid;

public class IxPoiChildrenHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> childrenMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> childrenList = new ArrayList<IRow>();
				IxPoiChildrenForAndroid poiCheildren = new IxPoiChildrenForAndroid();
				poiCheildren.setRelationType(rs.getInt("relation_type"));
				poiCheildren.setChildPoiPid(rs.getInt("child_poi_pid"));
				poiCheildren.setPoiNum(rs.getString("poi_num"));
				poiCheildren.setRowId(rs.getString("row_id"));
				
				if (childrenMap.containsKey(rs.getLong("parent_poi_pid"))) {
					childrenList = childrenMap.get(rs.getLong("parent_poi_pid"));
					childrenList.add(poiCheildren);
					childrenMap.put(rs.getLong("parent_poi_pid"), childrenList);
				} else {
					childrenList.add(poiCheildren);
					childrenMap.put(rs.getLong("parent_poi_pid"), childrenList);
				}
			}
			return childrenMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
