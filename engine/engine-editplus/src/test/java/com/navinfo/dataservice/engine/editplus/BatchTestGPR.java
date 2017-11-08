package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
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
		Connection conn = DBConnector.getInstance().getConnectionById(12);
		OperationResult operationResult = new OperationResult();

		List<Long> pids = new ArrayList<>();
		pids.add(4015088L);
		
		Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(conn, pids);
		Set<String> tabNames = new HashSet<>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_NAME_FLAG");
		
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
		
		BatchCommand batchCommand=new BatchCommand();	
		batchCommand.setRuleId("FM-BAT-20-135");
		//batchCommand.setOperationName("day2month");
		Batch batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
		System.out.println(batch.getName());
		batch.persistChangeLog(1, 2);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end batch test FM-BAT-20-164");
	}

}
