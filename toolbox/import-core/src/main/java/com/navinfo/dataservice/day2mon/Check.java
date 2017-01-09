package com.navinfo.dataservice.day2mon;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;

public class Check {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult;
	private Connection conn;

	public Check(OperationResult opResult,Connection conn) {
		super();
		this.opResult = opResult;
		this.conn = conn;
	}
	public Map<String, Map<Long, Set<String>>> execute() throws Exception{
		// 检查
		log.info("开始执行检查");
		CheckCommand checkCommand=new CheckCommand();		
//		List<String> checkList=new ArrayList<String>();
//		checkList.add("FM-A04-04");
//		checkList.add("FM-A04-05");
//		checkList.add("FM-A04-08");
//		checkList.add("FM-A04-10");
//		checkList.add("FM-A04-21");
//		checkList.add("FM-A04-09");
//		checkList.add("FM-A07-01");
//		checkList.add("FM-A07-03");
//		checkList.add("FM-A07-02");
//		checkList.add("FM-A07-11");
//		checkList.add("FM-A07-12");
//		checkList.add("FM-A04-18");
//		
//		checkList.add("FM-A09-01");
//		checkList.add("FM-YW-20-026");
//		
//		checkList.add("FM-YW-20-012");
//		checkList.add("FM-YW-20-013");
//		checkList.add("FM-YW-20-014");
//		checkList.add("FM-YW-20-017");
//		
//		checkList.add("FM-YW-20-018");
//		checkList.add("FM-GLM60189");
//		checkCommand.setRuleIdList(checkList);
		checkCommand.setOperationName("CHECK_DAY2MONTH");
		log.info("当前执行检查项:"+checkCommand.getRuleIdList());
		checkCommand.setSaveResult(false);
//		log.info("要执行的检查项:"+checkList.toString());
		log.info("当前数据:"+opResult.getAllObjs());
		com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check check=new com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check(conn,opResult);
		check.operate(checkCommand);
		
		log.info("检查完成");
		return check.getErrorPidMap();
	}
}
