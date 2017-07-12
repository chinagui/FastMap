package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;

public class PoiEvaluPlanHandler implements ResultSetHandler<Map<Long,Integer>>{

	@Override
	public Map<Long,Integer> handle(ResultSet rs) throws SQLException {
		Map<Long,Integer> evaluPlanMap = new HashMap<Long,Integer>();
		
		try {
			while (rs.next()){
				int evaluPlan = 2;
				int isPlanSelected = rs.getInt("is_plan_selected");
				int isImportant = rs.getInt("is_important");
				if(isPlanSelected == 0){
					evaluPlan = 3;
				}else if(isPlanSelected == 1 ){
					if(isImportant == 1){
						evaluPlan = 1;
					}
				}
				evaluPlanMap.put(rs.getLong("pid"), evaluPlan);
			}
			return evaluPlanMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
