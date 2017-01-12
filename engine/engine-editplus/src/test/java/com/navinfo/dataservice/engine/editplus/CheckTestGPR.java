package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;
import com.navinfo.navicommons.database.sql.DBUtils;

public class CheckTestGPR {

	public CheckTestGPR() {
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
		CheckTest test=new CheckTest();
		test.init();
		Connection conn = DBConnector.getInstance().getConnectionById(19);
		OperationResult operationResult=new OperationResult();
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,false,335, false);
		//IxPoi row=(IxPoi) obj.getMainrow();
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoiAddress chiAddress = poiObj.getCHAddress();
		//row.setKindCode("190100");
		ChangeLog logg=new ChangeLog();
		Map<String, Object> oldValues=new HashMap<String, Object>();
		oldValues.put("ROADNAME", "123456");
		logg.setOldValues(oldValues);
		List<ChangeLog> logList=new ArrayList<ChangeLog>();
		logList.add(logg);
		chiAddress.setOpType(OperationType.UPDATE);
		chiAddress.setOpType(OperationType.UPDATE);
		operationResult.putObj(obj);
		
		CheckCommand checkCommand=new CheckCommand();		
		List<String> ruleIdList=new ArrayList<String>();
		ruleIdList.add("FM-YW-20-026");
		checkCommand.setRuleIdList(ruleIdList);
		
		Check check=new Check(conn,operationResult);
		check.operate(checkCommand);
		Map<String, Map<Long, Set<String>>> errorPid = check.getErrorPidMap();
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end check test");
	}

}
