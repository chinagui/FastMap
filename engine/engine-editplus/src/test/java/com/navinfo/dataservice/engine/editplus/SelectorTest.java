package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.MultiSrcPoiSelectorConfig;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.convert.MultiSrcPoiConvertor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.navicommons.database.sql.RunnableSQL;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/** 
 * @ClassName: SelectorTest
 * @author songdongyan
 * @date 2016年11月18日
 * @Description: SelectorTest.java
 */
public class SelectorTest {

	/**
	 * 
	 */
	public SelectorTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Before
	public void init(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-editplus.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	

	@Test
	public void test0(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);;
			String objType = "IX_POI";
			long pid = 237630;
			boolean isLock = false;

			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_RESTAURANT");
			tabNames.add("IX_POI_CHILDREN");
			tabNames.add("IX_POI_PARENT");
			tabNames.add("IX_POI_PARKING");
			tabNames.add("IX_POI_HOTEL");
			tabNames.add("IX_POI_CHARGINGSTATION");
			tabNames.add("IX_POI_CHARGINGPLOT");
			tabNames.add("IX_POI_GASSTATION");
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames,false, pid, isLock);
			List<BasicRow> list1 = obj.getRowsByName("IX_POI_NAME");
			List<BasicRow> list2 = obj.getRowsByName("IX_POI_ICON");
			System.out.println("Over.");
			MultiSrcPoiConvertor ms = new MultiSrcPoiConvertor();
			JSONObject json = ms.toJson((IxPoiObj) obj);
			System.out.println(json);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	@Test
	public void test1(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			long pid = 308;
			boolean isLock = false;

			BasicObj obj = ObjSelector.selectByPid(conn, objType, null,true, pid, isLock);
			List<RunnableSQL> sqlList = obj.generateSql(true);
			System.out.println("Over.");
			
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	@Test
	public void test2(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			String colName = "POI_NUM";
			String colValue = "0335100531LS100266";
			boolean isLock = false;
			Set<String> tabNames = new HashSet<String>();
			
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");
			tabNames.add("IX_POI_ENTRYIMAGE");
			tabNames.add("IX_POI_ICON");
			tabNames.add("IX_POI_PHOTO");
			tabNames.add("IX_POI_AUDIO");
			tabNames.add("IX_POI_VIDEO");
			tabNames.add("IX_POI_PARENT");
			tabNames.add("IX_POI_CHILDREN");
			tabNames.add("IX_POI_BUILDING");
			tabNames.add("IX_POI_BUSINESSTIME");
			tabNames.add("IX_POI_INTRODUCTION");
			tabNames.add("IX_POI_ADVERTISEMENT");
			tabNames.add("IX_POI_GASSTATION");
			tabNames.add("IX_POI_CHARGINGSTATION");
			tabNames.add("IX_POI_CHARGINGPLOT");
			tabNames.add("IX_POI_CHARGINGPLOT_PH");
			tabNames.add("IX_POI_PARKING");
			tabNames.add("IX_POI_ATTRACTION");
			tabNames.add("IX_POI_HOTEL");
			tabNames.add("IX_POI_RESTAURANT");
			tabNames.add("IX_POI_CARRENTAL");

			BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, tabNames,false, colName,colValue, isLock);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	@Test
	public void test3(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			String colName = "PID";
			long colValue = 308;
			boolean isLock = false;

			BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, null,false, colName,colValue, isLock);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	public void test4(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			String colName = "POI_NUM";
			String colValue = "0010061110WZS00536";
			boolean isLock = false;
			Set<String> tabNames = new HashSet<String>();
			
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");
			tabNames.add("IX_POI_ENTRYIMAGE");
			tabNames.add("IX_POI_ICON");
			tabNames.add("IX_POI_PHOTO");
			tabNames.add("IX_POI_AUDIO");
			tabNames.add("IX_POI_VIDEO");
			tabNames.add("IX_POI_PARENT");
			tabNames.add("IX_POI_CHILDREN");
			tabNames.add("IX_POI_BUILDING");
			tabNames.add("IX_POI_BUSINESSTIME");
			tabNames.add("IX_POI_INTRODUCTION");
			tabNames.add("IX_POI_ADVERTISEMENT");
			tabNames.add("IX_POI_GASSTATION");
			tabNames.add("IX_POI_CHARGINGSTATION");
			tabNames.add("IX_POI_CHARGINGPLOT");
			tabNames.add("IX_POI_CHARGINGPLOT_PH");
			tabNames.add("IX_POI_PARKING");
			tabNames.add("IX_POI_ATTRACTION");
			tabNames.add("IX_POI_HOTEL");
			tabNames.add("IX_POI_RESTAURANT");
			tabNames.add("IX_POI_CARRENTAL");

			BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, tabNames,true, colName,colValue, isLock);
			List<RunnableSQL> sqlList = obj.generateSql(true);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}


	@Test
	public void test10(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			Collection<Long> pids = new ArrayList<Long>();
			pids.add((long) 308);
			pids.add((long) 316);
			pids.add((long) 317);
			boolean isLock = false;
			boolean isNowait = false;
			
			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");
			
			Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, objType, tabNames,false, pids, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	@Test
	public void test11(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			Collection<Long> pids = new ArrayList<Long>();
			pids.add((long) 308);
			pids.add((long) 316);
			pids.add((long) 317);
			boolean isLock = false;
			boolean isNowait = false;

			Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, objType, null, false,pids, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	@Test
	public void test12(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			Collection<Object> pids = new ArrayList<Object>();
			String colName = "PID";
			pids.add((long) 308);
			pids.add((long) 316);
			pids.add((long) 317);
			boolean isLock = false;
			boolean isNowait = false;

			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");

			Map<Long,BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, objType, tabNames, true,colName,pids, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	@Test
	public void test13(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			Collection<String> pids = new ArrayList<String>();
			String colName = "POI_NUM";
			pids.add("0335100531LS100266");
			pids.add("0010060909HYX00855");
			pids.add("0010060909HYX00856");
			boolean isLock = false;
			boolean isNowait = false;


			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");
			
			Map<Long,BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, objType, tabNames, false,colName,pids, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	public void test14(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			List<Long> pids = new ArrayList<Long>();
			pids.add(32355L);
			pids.add(29407L);
			pids.add(44605L);
			pids.add(37935L);
			Map<Long, String> parentFid = IxPoiSelector.getParentFidByPids(conn, pids);
			for(Map.Entry<Long, String> entry :parentFid.entrySet()){
				System.out.println("parentFid"+entry.getValue()+"========"+entry.getValue());
			}
			/*Map<Long, String> childFid = IxPoiSelector.getChildFid(conn, pids);
			for(Map.Entry<Long, String> entry :childFid.entrySet()){
				System.out.println("childFid"+entry.getValue()+"========"+entry.getValue());
			}*/
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	public void test15(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(19);
			/*
			String data = "{\"postCode\": \"\",\"rowId\": \"3AE1FB4B927892F7E050A8C08304EE4C\",\"pid\": 69159,"
					+ "\"objStatus\":\"UPDATE\",\"poiMemo\":\"test\",\"level\":\"B3\","
					+ "\"addresses\":[{\"nameGroupid\":1,\"poiPid\":0,"
					+ "\"langCode\":\"CHI\",\"fullname\":\"北京\",\"objStatus\": \"INSERT\"}]}}";
			*/
			/*String data = "{\"postCode\": \"\",\"rowId\": \"3AE1FB4B927892F7E050A8C08304EE4C\",\"pid\": 69159,"
					+ "\"objStatus\":\"UPDATE\",\"poiMemo\":\"test\",\"level\":\"B3\",\"side\":0,\"regionId\":10010,"
					+ "\"addresses\":[{\"nameGroupid\":1,\"srcFlag\":1,"
					+ "\"poiPid\":69159,\"rowId\":\"D476DB9402074216A28607D8D8C9C71D\","
					+ "\"langCode\":\"CHI\",\"fullname\":\"北京\",\"objStatus\": \"UPDATE\"}]}}";*/
			/*String data = "{\"names\":[{\"name\":\"北京华军中医医院\",\"rowId\":\"\""
					+ ",\"pid\":335,\"objStatus\":\"INSERT\"}],\"pid\":335}";*/
			//String data = "{\"names\":[{\"name\":\"Xihongmen South Brg Toll Gate 2222\",\"rowId\":\"3AE1FCF65D2392F7E050A8C08304EE4C\",\"pid\":8069342,\"objStatus\":\"UPDATE\",\"nameFlags\":[{\"flagCode\":\"002000010000\",\"rowId\":\"3AE1FE47011392F7E050A8C08304EE4C\",\"objStatus\":\"UPDATE\"}]}],\"rowId\":\"3AE1FB4B0B6592F7E050A8C08304EE4C\",\"pid\":336}";
			//String data = "{\"names\":[{\"pid\": 0,\"poiPid\": 335,\"nameGroupid\": 1,\"langCode\": \"ENG\",\"nameClass\": 3,\"nameType\": 2,\"name\": \"111\",\"nameFlags\": [],\"rowId\": \"\",\"objStatus\": \"INSERT\"},{\"pid\": 0,\"poiPid\": 335,\"nameGroupid\": 1,\"langCode\": \"ENG\",\"nameClass\": 3,\"nameType\": 2,\"name\": \"222\",\"nameFlags\": [],\"rowId\": \"\",\"objStatus\": \"INSERT\"}],\"rowId\": \"3AE1FB4B0B6492F7E050A8C08304EE4C\",\"pid\": 335}";
			//String data = "{\"names\":[{\"pid\":0,\"poiPid\":0,\"nameGroupid\":1,\"langCode\":\"ENG\",\"nameClass\":1,\"nameType\":1,\"name\":\"1111\",\"nameFlags\":[{\"nameId\":0,\"flagCode\":\"002000010000\",\"rowId\":\"\",\"objStatus\":\"INSERT\"}],\"namePhonetic\":\"\",\"rowId\":\"\",\"objStatus\":\"INSERT\"}],\"rowId\":\"3AE1FB4B0B6592F7E050A8C08304EE4C\",\"pid\":336,\"objStatus\":\"UPDATE\"}";
			//String data = "{\"photos\":[{\"poiPid\":306000196,\"pid\":\"3a09569156b3463fa4fec23919e6aec0\",\"tag\":7,\"photoId\":0,\"status\":\"\",\"memo\":\"ea378e47ddd54bd0b39ca5a82d929713\",\"objStatus\":\"INSERT\"}],\"rowId\":\"F51CD1C76DB24A7C8D9B074FABCF891E\",\"pid\":306000196,\"objStatus\":\"UPDATE\"}";
			String data = "{\"photos\":[{\"poiPid\":16247512,\"fccPid\":\"d9adeea2e69b42e3abb02ec9a00b3e52\",\"tag\":7,\"photoId\":0,\"status\":\"\",\"memo\":\"6084a4fece3f4485a6193aaf6242ea85\",\"objStatus\":\"INSERT\"}],\"rowId\":\"3F836D0C63344604E050A8C083041544\",\"pid\":16247512,\"objStatus\":\"UPDATE\"}";
			JSONObject jo = JSONObject.fromObject(data);
			System.out.println("导入的json数据"+data);
			//Map<String, JSONObject> addMap = new HashMap<String, JSONObject>();
			//addMap.put("IXPOI", jo);
			Map<String, Map<Long, JSONObject>> updateMap = new HashMap<String, Map<Long, JSONObject>>();
			Map<Long, JSONObject> update = new HashMap<Long, JSONObject>();
			update.put(16247512L, jo);
			updateMap.put("IXPOI", update);
			DefaultObjImportor df = new DefaultObjImportor(conn, null);
			//List<BasicObj> list = df.improtAdd(conn, addMap);
			List<BasicObj> list = df.improtUpdate(conn, updateMap);
			for (BasicObj basicObj : list) {
				System.out.println(basicObj);
			}
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	public void testtest(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(19);
			BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,true, 2179861, false);
			Map<Long,BasicObj> result = new HashMap<Long,BasicObj>();
			result.put((long) 2179861, obj);
			Set<String> selConfig = new HashSet<String>();
			selConfig.add("IX_POI_NAME");
			selConfig.add("IX_POI_ATTRACTION");
			selConfig.add("IX_POI_CHILDREN");
			selConfig.add("IX_POI_HOTEL");

			ObjChildrenIncreSelector.increSelect(conn,result, selConfig);
			System.out.println(obj.getSubrows().keySet());
			BasicObj temp = obj;
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}

	
}
