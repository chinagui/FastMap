package com.navinfo.dataservice.day2mon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.sql.DBUtils;

public class Classifier {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private Map<String, Map<Long, Set<String>>> checkResult;
	private Connection conn;
	private List<String> namelist1 = new ArrayList<String>();
	private List<String> namelist2 = new ArrayList<String>();
	private List<String> namelist3 = new ArrayList<String>();
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
		
		namelist3.add("FM-M01-01");
		
		othernamelist.add("FM-A04-18");
	}
	public void execute() throws Exception {
		if (checkResult==null) {
			log.info("检查结果为空，不执行重分类");
			return ;	
		}
		// 重分类
		log.info("开始执行重分类");
		Map<Long, Set<String>> poiMap = checkResult.get("IX_POI");

		for (Long pid:poiMap.keySet()) {
			log.info("当前执行poi:"+pid);
			Set<String> ruleList = poiMap.get(pid);
			log.info("当前poi检查结果:"+ruleList.toString());
			boolean isEng = false;
			boolean isName = false;
			// poi_englishname
			Set<String> workItemIdEngName = new HashSet<String>();
			int engNameHandler = 1;
			//删除网络英文作业项
//			if (ruleList.contains("FM-M01-02")) {
//				workItemIdEngName.add("FM-M01-02");
//				ruleList.remove("FM-M01-02");
//				if (ruleList.contains("FM-YW-20-013")) {
//					ruleList.remove("FM-YW-20-013");
//				}
//				if (ruleList.contains("FM-YW-20-012")) {
//					ruleList.remove("FM-YW-20-012");
//				}
//				if (ruleList.contains("FM-YW-20-014")) {
//					ruleList.remove("FM-YW-20-014");
//				}
//				if (ruleList.contains("FM-YW-20-017")) {
//					ruleList.remove("FM-YW-20-017");
//				}
// 				isEng = true;
//			}else 
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
			log.info("英文名称：workItemIdEngName:"+workItemIdEngName.toString());
			
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
			
			for (String name1:namelist3) {
				if(ruleList.contains(name1)) {
					workItemIdName.add(name1);
					isName = true;
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
			
			log.info("中文名：workItemIdName:"+workItemIdName.toString());
			
			// 其他作业
			Set<String> otherWorkItemId = new HashSet<String>();
			otherWorkItemId = ruleList;
			int otherHandler = 1;
			if (otherWorkItemId.contains("FM-YW-20-018")) {
				otherHandler = 201250;
			}
			
			log.info("其他作业：otherWorkItemId:"+otherWorkItemId.toString());
			
			if (isEng&&!isName) {
				// 英文名作业只有FM-YW-20-017且不需中文作业的，执行FM-BAT-20-115,FM-BAT-20-135,FM-BAT-20-163批处理
				if (workItemIdEngName.size() == 1 && workItemIdEngName.contains("FM-YW-20-017")) {
					engNameHandler = 200170;
				} else if (workItemIdEngName.size() == 1 && workItemIdEngName.contains("FM-YW-20-014")) {
					// 不需中文名称作业，需要FM-YW-20-014作业的执行FM-BAT-20-115
					engNameHandler = 200140;
				} else {
					// 不需中文名称作业，需要英文名称作业的执行FM-BAT-20-115
					engNameHandler = 201150;
				}
			}
			log.info("特殊标记engNameHandler:"+engNameHandler);
			log.info("特殊标记nameHandler:"+nameHandler);
			log.info("特殊标记otherHandler:"+otherHandler);
			
			updateColumnStatus(pid,workItemIdEngName,engNameHandler);
			updateColumnStatus(pid,workItemIdName,nameHandler);
			updateColumnStatus(pid,otherWorkItemId,otherHandler);
			
		}
		
	}
	
	// 执行重分类，更新POI_COLUMN_STATUS 表
	private void updateColumnStatus(Long pid,Set<String> workItemId,int handler) throws Exception {
		// TODO 清理上次灌库时所打的数据标记
		
		Statement stmt = conn.createStatement();
		try {
			for (String workItem:workItemId) {
				StringBuilder sb = new StringBuilder(" MERGE INTO poi_column_status T1 ");
				sb.append(" USING (SELECT "+pid+" as b,'" + workItem + "' as c," + handler
						+ " as d  FROM dual) T2 ");
				sb.append(" ON ( T1.pid=T2.b and T1.work_item_id=T2.c) ");
				sb.append(" WHEN MATCHED THEN ");
				sb.append(" UPDATE SET T1.first_work_status = 1,T1.second_work_status = 1,T1.handler = T2.d,T1.QC_FLAG=0,T1.common_handler=0 ");
				sb.append(" WHEN NOT MATCHED THEN ");
				sb.append(" INSERT (T1.pid,T1.work_item_id,T1.first_work_status,T1.second_work_status,T1.handler) VALUES(T2.b,T2.c,1,1,T2.d)");
				stmt.addBatch(sb.toString());
			}
			stmt.executeBatch();
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(stmt);
		}
	}
	
}
