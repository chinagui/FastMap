package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiForAndroid;

public class IxSamepoiHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> samepoiMap = new HashMap<Long, List<IRow>>();
		
		try {
			while (rs.next()){
				List<IRow> samepoiList = new ArrayList<IRow>();
				IxSamepoiForAndroid ixsamepoi = new IxSamepoiForAndroid();
				ixsamepoi.setPoiNum(rs.getString("poi_num"));
				samepoiList.add(ixsamepoi);
				samepoiMap.put(rs.getLong("pid"), samepoiList);
			}
			return samepoiMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
