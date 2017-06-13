package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
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
		Connection conn = DBConnector.getInstance().getConnectionById(13);
		
		String sql="select pid from poi_edit_status where status=3";
		PreparedStatement state = conn.prepareStatement(sql);
		ResultSet rs = state.executeQuery();
		Set<Long> pids=new HashSet<Long>();
		while (rs.next()) {
			pids.add(rs.getLong("PID"));
		}
		
		Map<Long, BasicObj> pois = ObjBatchSelector.selectByPids(conn, "IX_POI", null,true, pids, false, false);
		OperationResult operationResult=new OperationResult();		
		operationResult.putAll(pois.values());
		
		CheckCommand checkCommand=new CheckCommand();		
		List<String> ruleIdList=new ArrayList<String>();
		ruleIdList.add("test2");
		checkCommand.setRuleIdList(ruleIdList);
		checkCommand.setSaveResult(false);
		
		Check check=new Check(conn,operationResult);
		check.operate(checkCommand);
		Map<String, Map<Long, Set<String>>> errorPid = check.getErrorPidMap();
		if(errorPid!=null){
			System.out.println(check.getReturnExceptions().size());
			System.out.println(check.getReturnExceptions().get(0).getInformation());
			for(NiValException tmp:check.getReturnExceptions()){
				System.out.println(tmp.getTargets());
			}
		}
		else{System.out.println("null");}
		//DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end check test");
	}

}
