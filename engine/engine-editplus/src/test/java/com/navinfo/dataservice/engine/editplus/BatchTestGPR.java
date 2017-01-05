package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;

public class BatchTestGPR {

	public BatchTestGPR() {
		// TODO Auto-generated constructor stub
	}
	

	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("start batch test");
		BatchTest test=new BatchTest();
		test.init();
		Connection conn = DBConnector.getInstance().getConnectionById(19);
		OperationResult operationResult=new OperationResult();
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null, false,320000162, false);
//		operationResult.putObj(obj);
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		//IxPoi row=(IxPoi) obj.getMainrow();
		//row.setKindCode("190100");
		ChangeLog logg=new ChangeLog();
		Map<String, Object> oldValues=new HashMap<String, Object>();
		oldValues.put("PREFIX", "123");
		logg.setOldValues(oldValues);
		List<ChangeLog> logList=new ArrayList<ChangeLog>();
		logList.add(logg);
		chiAddress.setHisChangeLogs(logList);
		chiAddress.setOpType(OperationType.UPDATE);
		operationResult.putObj(obj);
		
		BatchCommand batchCommand=new BatchCommand();	
		batchCommand.setRuleId("FM-BAT-20-142");
		//batchCommand.setOperationName("day2month");
		Batch batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
		System.out.println(batch.getName());
		batch.persistChangeLog(1, 2);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end batch test FM-BAT-20-164");
	}

}
