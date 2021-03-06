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
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;

public class BatchTest {

	public BatchTest() {
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
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		OperationResult operationResult=new OperationResult();
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,true, 2179861, false);
//		operationResult.putObj(obj);
		IxPoi row=(IxPoi) obj.getMainrow();
		row.setKindCode("190100");
		ChangeLog logg=new ChangeLog();
		Map<String, Object> oldValues=new HashMap<String, Object>();
		oldValues.put("KIND_CODE", "123");
		logg.setOldValues(oldValues);
		List<ChangeLog> logList=new ArrayList<ChangeLog>();
		logList.add(logg);
		row.setHisChangeLogs(logList);
		operationResult.putObj(obj);
		
		BatchCommand batchCommand=new BatchCommand();	
		//batchCommand.setRuleId("FM-BAT-20-137");
		batchCommand.setOperationName("day2month");
		Batch batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
		System.out.println(batch.getName());
		//batch.persistChangeLog(1, 2);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end batch test");
	}

}
