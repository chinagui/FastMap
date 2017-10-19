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
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
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
		CheckTest test=new CheckTest();
		test.init();
//		Connection conn = DBConnector.getInstance().getConnectionById(17);
		Connection conn = DBConnector.getInstance().getConnectionById(13);
		OperationResult operationResult=new OperationResult();
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_SAMEPOI");
		tabNames.add("IX_SAMEPOI_PART");
//		tabNames.add("IX_POI_CHARGINGSTATION");
//		tabNames.add("IX_POI_GASSTATION");
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", tabNames,false, 767, false);
		IxPoi row=(IxPoi) obj.getMainrow();
		IxPoiObj poiObj=(IxPoiObj) obj;
//		row.setKindCode("230126");
		ChangeLog logg=new ChangeLog();
//		Map<String, Object> oldValues=new HashMap<String, Object>();
//		oldValues.put("KIND_CODE", "230126");
//		oldValues.put("GEOMETRY", "");
//		logg.setOldValues(oldValues);
//		logg.setOpType(OperationType.UPDATE);
		logg.setOpType(OperationType.INSERT);
		List<ChangeLog> logList=new ArrayList<ChangeLog>();
		logList.add(logg);
//		row.setOpType(OperationType.PRE_DELETED);
//		row.setOpType(OperationType.INSERT);
		row.setHisChangeLogs(logList);
		
//		IxPoiAddress chiAddress = poiObj.getCHAddress();
//		ChangeLog logg1=new ChangeLog();
//		Map<String, Object> oldValues1=new HashMap<String, Object>();
//		oldValues1.put("FULLNAME", "四川省凉山彝族自治州会理县Ｇ１０８大运摩托附近北京银行培训中心號");
//		logg1.setOldValues(oldValues1);
//		logg1.setOpType(OperationType.UPDATE);
//		List<ChangeLog> logList1=new ArrayList<ChangeLog>();
//		logList1.add(logg1);
//		chiAddress.setHisChangeLogs(logList1);
		
//		IxPoiName name = poiObj.getOfficeOriginCHName();
//		ChangeLog namelogg=new ChangeLog();
//		Map<String, Object> nameOldValues=new HashMap<String, Object>();
//		nameOldValues.put("NAME", "四川省凉山彝族自治州会理县Ｇ１０８大运摩托附近北京银行培训中心號");
//		namelogg.setOldValues(nameOldValues);
//		namelogg.setOpType(OperationType.UPDATE);
//		List<ChangeLog> nameLog=new ArrayList<ChangeLog>();
//		nameLog.add(namelogg);
//		name.setHisChangeLogs(nameLog);
		
//		Set<Long> pids = new HashSet<Long>();
//		pids.add(64L);
//		pids.add(8165144L);
//		pids.add(8165145L);
//		pids.add(4696166L);
//		pids.add(74850060L);
//		pids.add(64133244L);
//		Map<Long, BasicObj> rows=ObjBatchSelector.selectByPids(conn, "IX_POI", tabNames, false, pids, false, true);
//		for (Long key : rows.keySet()) {
//			BasicObj obj1 = rows.get(key);
//			IxPoi row1=(IxPoi) obj1.getMainrow();
//			row1.setHisChangeLogs(logList);
//			operationResult.putObj(obj1);
//		}
		
		operationResult.putObj(obj);
		
		CheckCommand checkCommand=new CheckCommand();		
		List<String> ruleIdList=new ArrayList<String>();
		ruleIdList.add("GLM60238");
		checkCommand.setRuleIdList(ruleIdList);
		
		Check check=new Check(conn,operationResult);
		check.operate(checkCommand);
		Map<String, Map<Long, Set<String>>> errorPid = check.getErrorPidMap();
		if(errorPid!=null){
			System.out.println(check.getReturnExceptions().get(0).getInformation());}
		else{System.out.println("null");}
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end check test");
	}

}
