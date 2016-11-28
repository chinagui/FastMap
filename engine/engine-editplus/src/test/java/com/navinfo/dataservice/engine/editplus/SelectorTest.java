package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.MultiSrcPoiSelectorConfig;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.convert.MultiSrcPoiConvertor;
import com.navinfo.navicommons.database.sql.RunnableSQL;

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
			long pid = 300000013;
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
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames, pid, isLock);
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

			BasicObj obj = ObjSelector.selectByPid(conn, objType, null, pid, isLock);
			List<RunnableSQL> sqlList = obj.generateSql();
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

			BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, tabNames, colName,colValue, isLock);
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

			BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, null, colName,colValue, isLock);
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

			BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, tabNames, colName,colValue, isLock);
			List<RunnableSQL> sqlList = obj.generateSql();
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
			
			List<BasicObj> objList = ObjBatchSelector.selectByPids(conn, objType, tabNames, pids, isLock,isNowait);
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

			List<BasicObj> objList = ObjBatchSelector.selectByPids(conn, objType, null, pids, isLock,isNowait);
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

			List<BasicObj> objList = ObjBatchSelector.selectBySpecColumn(conn, objType, tabNames, colName,pids, isLock,isNowait);
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
			
			List<BasicObj> objList = ObjBatchSelector.selectBySpecColumn(conn, objType, tabNames, colName,pids, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
}
