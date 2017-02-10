package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;
//import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

public class CheckTest {

	public CheckTest() {
		// TODO Auto-generated constructor stub
	}
	

	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
//	
//	@Test
//	public void test() {
//		UploadOperationByGather operation = new UploadOperationByGather((long) 0);
//		try {
//			Date startTime = new Date();
//			JSONObject ret = operation.importPoi("F://testpoi.txt");
//			System.out.println(ret);
//			Date endTime = new Date();
//			System.out.println("total time:"+ (endTime.getTime() - startTime.getTime()));
////			System.out.println(UuidUtils.genUuid());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("start check test");
		CheckTest test=new CheckTest();
		test.init();
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		OperationResult operationResult=new OperationResult();
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,false, 767, false);
		IxPoi row=(IxPoi) obj.getMainrow();
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoiAddress chiAddress = poiObj.getCHAddress();
//		row.setKindCode("230126");
		ChangeLog logg=new ChangeLog();
//		Map<String, Object> oldValues=new HashMap<String, Object>();
//		oldValues.put("FULLNAME", "四川省凉山彝族自治州会理县Ｇ１０８大运摩托附近北京银行培训中心");
//		logg.setOldValues(oldValues);
		logg.setOpType(OperationType.UPDATE);
		List<ChangeLog> logList=new ArrayList<ChangeLog>();
		logList.add(logg);
		row.setHisChangeLogs(logList);
		
//		ChangeLog logg1=new ChangeLog();
//		Map<String, Object> oldValues1=new HashMap<String, Object>();
//		oldValues1.put("FULLNAME", "四川省凉山彝族自治州会理县Ｇ１０８大运摩托附近北京银行培训中心號");
//		logg1.setOldValues(oldValues1);
//		logg1.setOpType(OperationType.UPDATE);
//		List<ChangeLog> logList1=new ArrayList<ChangeLog>();
//		logList1.add(logg1);
//		chiAddress.setHisChangeLogs(logList1);
		
		
		operationResult.putObj(obj);
		
		CheckCommand checkCommand=new CheckCommand();		
		List<String> ruleIdList=new ArrayList<String>();
		ruleIdList.add("FM-11Win-05-02");
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
