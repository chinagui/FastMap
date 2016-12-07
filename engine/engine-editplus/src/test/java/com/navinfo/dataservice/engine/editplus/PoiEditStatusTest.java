package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

/** 
 * @ClassName: PoiEditStatusTest
 * @author songdongyan
 * @date 2016年11月24日
 * @Description: PoiEditStatusTest.java
 */
public class PoiEditStatusTest {

	/**
	 * 
	 */
	public PoiEditStatusTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test0() throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
				"fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1");
		Map<Long,String> map = new HashMap<Long,String>();
		map.put((long) 1, "001000020000");//情报
		map.put((long) 2, "001000030000");//腾讯
		map.put((long) 3, "001000030001");//搜狗
		map.put((long) 4, "001000030002");//百度
		map.put((long) 5, "001000030003");//天地图
		map.put((long) 6, "001000030003");//POI挖掘
		map.put((long) 7, "001000030004");//POI挖掘
		
		PoiEditStatus.tagMultiSrcPoi(conn, map);
		System.out.println("ok");
	}
	
	@Test
	public void test2() throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
				"fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1");
		Map<Long,String> map = new HashMap<Long,String>();
		map.put((long) 1, "001000030001");//情报
		map.put((long) 2, "001000030001");//腾讯
		map.put((long) 3, "001000030001");//搜狗
		map.put((long) 4, "001000030002");//百度
		map.put((long) 5, "001000030003");//天地图
		map.put((long) 6, "001000030003");//POI挖掘
		map.put((long) 7, "001000030004");//POI挖掘
		
		PoiEditStatus.tagMultiSrcPoi(conn, map);
		System.out.println("ok");
	}
	
	@Test
	public void test3() throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
				"fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1");
		OperationResult result = new OperationResult();
		IxPoi ixPoi = new IxPoi(8);
		IxPoiObj ixPoiObj = new IxPoiObj(ixPoi);
		ixPoi.setOpType(OperationType.INSERT);
		result.putObj(ixPoiObj);
		PoiEditStatus.insertPoiEditStatus(conn, result);
		System.out.println("ok");
	}
	
	@Test
	public void test10() throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
				"fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1");
		Map<Integer,Collection<Long>> pids = new HashMap<Integer,Collection<Long>>();
		Collection<Long> insertList = new ArrayList<Long>();
		insertList.add((long) 1);
		insertList.add((long) 2);
		insertList.add((long) 3);

		Collection<Long> pids2 = PoiEditStatus.pidFilterByEditStatus(conn,insertList,3);

		System.out.println("ok");
	}
}
