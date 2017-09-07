package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;

public class CheckTestGPR {

	public CheckTestGPR() {
		// TODO Auto-generated constructor stub
	}

	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("start check test");
		CheckTest test = new CheckTest();
		test.init();
		Connection conn = DBConnector.getInstance().getConnectionById(12);
		OperationResult operationResult = new OperationResult();

		List<Long> pids = new ArrayList<>();
		pids.add(45L);
		pids.add(46L);
		pids.add(49L);
		pids.add(209L);
		pids.add(92419067L);
		pids.add(95385951L);
		
		Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(conn, pids);
		Set<String> tabNames = getChangeTableSet(logs);
		// 获取poi对象
		Map<Long, BasicObj> objs = null;
		if (tabNames == null || tabNames.size() == 0) {
			// log.info(1);
			objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, true, pids, false, false);
			// log.info(2);
		} else {
			objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false, pids, false, false);
		}
		// 将poi对象与履历合并起来
		ObjHisLogParser.parse(objs, logs);
		operationResult.putAll(objs.values());

		CheckCommand checkCommand = new CheckCommand();
		List<String> ruleIdList = new ArrayList<String>();
		ruleIdList.add("FM-A09-13");
		ruleIdList.add("FM-14Sum-06-03");
		ruleIdList.add("FM-YW-20-038");

		checkCommand.setRuleIdList(ruleIdList);

		Check check = new Check(conn, operationResult);
		check.operate(checkCommand);
		Map<String, Map<Long, Set<String>>> errorPid = check.getErrorPidMap();
		if(errorPid!=null){
			System.out.println(check.getReturnExceptions().get(0).getInformation());
		}else{
			System.out.println("null");
		}
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end check test");
	}

	/**
	 * 分析履历，将履历中涉及的变更过的子表集合返回
	 * 
	 * @param logs
	 * @return [IX_POI_NAME,IX_POI_ADDRESS]
	 */
	private static Set<String> getChangeTableSet(Map<Long, List<LogDetail>> logs) {
		Set<String> subtables = new HashSet<String>();
		if (logs == null || logs.size() == 0) {
			return subtables;
		}
		String mainTable = "IX_POI";
		for (Long objId : logs.keySet()) {
			List<LogDetail> logList = logs.get(objId);
			for (LogDetail logTmp : logList) {
				String tableName = logTmp.getTbNm();
				if (!mainTable.equals(tableName)) {
					subtables.add(tableName);
				}
			}
		}
		return subtables;
	}
}
