package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;

public class CheckTest1 {

	public CheckTest1() {
		// TODO Auto-generated constructor stub
	}
	

	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	

	
	public static void main(String[] args) throws Exception{
		System.out.println("start check test");
		CheckTest1 test=new CheckTest1();
		test.init();
		Connection conn = DBConnector.getInstance().getConnectionById(13);
		OperationResult operationResult=new OperationResult();
		
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,false, 502000132, false);
		IxPoi row=(IxPoi) obj.getMainrow();
//		ChangeLog logg=new ChangeLog();
//		logg.setOpType(OperationType.INSERT);
//		List<ChangeLog> logList=new ArrayList<ChangeLog>();
//		logList.add(logg);
//		row.setHisChangeLogs(logList);
		row.setOpType(OperationType.UPDATE);
		operationResult.putObj(obj);
		
		CheckCommand checkCommand=new CheckCommand();		
		List<String> ruleIdList=new ArrayList<String>();
		ruleIdList.add("GLM60293");
		
		checkCommand.setRuleIdList(ruleIdList);
		
		Check check=new Check(conn,operationResult);
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

}
