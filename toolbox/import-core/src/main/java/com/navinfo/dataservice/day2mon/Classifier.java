package com.navinfo.dataservice.day2mon;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mysql.jdbc.Connection;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.sql.DBUtils;

public class Classifier {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private Map<String, Map<Long, Set<String>>> checkResult;
	private Connection conn;
	private List<String> namelist1 = new ArrayList<String>();
	private List<String> namelist2 = new ArrayList<String>();
	private List<String> othernamelist = new ArrayList<String>();
	
	public Classifier(Map<String, Map<Long, Set<String>>> checkResult,Connection conn) {
		super();
		this.checkResult = checkResult;
		this.conn = conn;
		
		namelist1.add("FM-A04-04");
		namelist1.add("FM-A04-05");
		namelist1.add("FM-A04-08");
		namelist1.add("FM-A04-10");
		namelist1.add("FM-A04-21");
		namelist1.add("FM-A04-09");
		
		namelist2.add("FM-A07-01");
		namelist2.add("FM-A07-03");
		namelist2.add("FM-A07-02");
		namelist2.add("FM-A07-11");
		namelist2.add("FM-A07-12");
		
		othernamelist.add("FM-A04-18");
	}
	public void execute() throws Exception {
		// 重分类
		Map<Long, Set<String>> poiMap = checkResult.get("IX_POI");

		for (Long pid:poiMap.keySet()) {
			
			Set<String> ruleList = poiMap.get(pid);
			boolean isEng = false;
			boolean isName = false;
			// poi_englishname
			Set<String> workItemIdEngName = new HashSet<String>();
			int engNameHandler = 1;
			if (ruleList.contains("FM-YW-20-013")) {
				workItemIdEngName.add("FM-YW-20-013");
				ruleList.remove("FM-YW-20-013");
				if (ruleList.contains("FM-YW-20-012")) {
					ruleList.remove("FM-YW-20-012");
				}
				if (ruleList.contains("FM-YW-20-014")) {
					ruleList.remove("FM-YW-20-014");
				}
				if (ruleList.contains("FM-YW-20-017")) {
					ruleList.remove("FM-YW-20-017");
				}
 				isEng = true;
			} else if (ruleList.contains("FM-YW-20-012")) {
				workItemIdEngName.add("FM-YW-20-012");
				isEng = true;
				ruleList.remove("FM-YW-20-012");
				if (ruleList.contains("FM-YW-20-014")) {
					ruleList.remove("FM-YW-20-014");
				}
				if (ruleList.contains("FM-YW-20-017")) {
					ruleList.remove("FM-YW-20-017");
				}
			} else if (ruleList.contains("FM-YW-20-014")) {
				workItemIdEngName.add("FM-YW-20-014");
				isEng = true;
				ruleList.remove("FM-YW-20-014");
				if (ruleList.contains("FM-YW-20-017")) {
					ruleList.remove("FM-YW-20-017");
				}
			} else if (ruleList.contains("FM-YW-20-017")) {
				workItemIdEngName.add("FM-YW-20-017");
				isEng = true;
				ruleList.remove("FM-YW-20-017");
			} 
			
			// poi_name
			Set<String> workItemIdName = new HashSet<String>();
			int nameHandler = 1;
			
			for (String name1:namelist1) {
				if(ruleList.contains(name1)) {
					workItemIdName.add(name1);
					isName = true;
				}
			}
			
			for (String name2:namelist2) {
				if(ruleList.contains(name2)) {
					workItemIdName.add(name2);
					isName = true;
					if (name2 == "FM-A07-02") {
						nameHandler = 107020;
					}
					break;
				}
			}
			
			for (String otherName:othernamelist) {
				if(ruleList.contains(otherName)) {
					workItemIdName.add(otherName);
					isName = true;
				}
			}
			
			ruleList.removeAll(workItemIdName);
			for (String name2:namelist2) {
				if (ruleList.contains(name2)) {
					ruleList.remove(name2);
				}
			}
			
			// 其他作业
			Set<String> otherWorkItemId = new HashSet<String>();
			otherWorkItemId = ruleList;
			int otherHandler = 1;
			if (otherWorkItemId.contains("FM-YW-20-018")) {
				otherHandler = 201250;
			}
			
			if (isEng&&isName) {
				// 英文名作业只有FM-YW-20-017且不需中文作业的，执行FM-BAT-20-135批处理
				if (workItemIdEngName.size() == 1 && workItemIdEngName.contains("FM-YW-20-017")) {
					engNameHandler = 200170;
				} else if (workItemIdEngName.size() == 1 && workItemIdEngName.contains("FM-YW-20-014")) {
					// 不需中文名称作业，需要FM-YW-20-014作业的执行FM-BAT-20-115，FM-BAT-20-140，FM-BAT-20-147
					engNameHandler = 200140;
				} else {
					// 不需中文名称作业，需要英文名称作业的执行FM-BAT-20-115，FM-BAT-20-140
					engNameHandler = 201150;
				}
			}
			updateColumnStatus(pid,workItemIdEngName,engNameHandler);
			updateColumnStatus(pid,workItemIdName,nameHandler);
			updateColumnStatus(pid,otherWorkItemId,otherHandler);
			
		}
		
	}
	
	// 执行重分类，更新POI_COLUMN_STATUS 表
	private void updateColumnStatus(Long pid,Set<String> workItemId,int handler) throws Exception {
		PreparedStatement pstmt = null;
		try {
			for (String workItem:workItemId) {
				StringBuilder sb = new StringBuilder(" MERGE INTO poi_column_status T1 ");
				sb.append(" USING (SELECT "+pid+" as b,'" + workItem + "' as c," + handler
						+ " as d  FROM dual) T2 ");
				sb.append(" ON ( T1.pid=T2.b and T1.work_item_id=T2.c) ");
				sb.append(" WHEN MATCHED THEN ");
				sb.append(" UPDATE SET T1.first_work_status = 1,T1.second_work_status = 1,T1.handler = T2.d ");
				sb.append(" WHEN NOT MATCHED THEN ");
				sb.append(" INSERT (T1.pid,T1.work_item_id,T1.first_work_status,T1.second_work_status,T1.handler) VALUES(T2.b,T2.c,1,1,T2.d)");
				
				pstmt = conn.prepareStatement(sb.toString());
				pstmt.execute();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
}
