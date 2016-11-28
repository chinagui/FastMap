package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
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
		BatchTest test=new BatchTest();
		test.init();
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		OperationResult operationResult=new OperationResult();
		BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null, 308, false);
//		operationResult.putObj(obj);
		IxPoi row=(IxPoi) obj.getMainrow();
		row.setKindCode("newkind");
		operationResult.putObj(obj);
		
		BatchCommand batchCommand=new BatchCommand();		
		List<String> ruleIdList=new ArrayList<String>();
		ruleIdList.add("GLM001TEST");
		batchCommand.setRuleIdList(ruleIdList);
		
		Batch batch=new Batch(conn,operationResult);
		batch.setCmd(batchCommand);
		batch.operate();
		System.out.println("123");
	}

}
