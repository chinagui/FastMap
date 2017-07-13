package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.PoiFlag;

public class PoiFlagHandler implements ResultSetHandler<Map<Long,List<IRow>>>{
	
	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> poiFlagMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> poiFlagList = new ArrayList<IRow>();
				PoiFlag poiFlag = new PoiFlag();
				poiFlag.setPid(rs.getInt("pid"));
				poiFlag.setSrcRecord(rs.getInt("src_record"));
				poiFlag.setFieldVerified(rs.getInt("field_verified"));
				poiFlagList.add(poiFlag);
				poiFlagMap.put(rs.getLong("pid"), poiFlagList);
			}
			return poiFlagMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
