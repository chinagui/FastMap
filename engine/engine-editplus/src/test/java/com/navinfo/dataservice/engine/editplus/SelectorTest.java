package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.selector.MultiSrcPoiSelectorConfig;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;

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
	
	/*
	 * MultiSrcPoiSelectorConfig-null
	 */
	@Test
	public void test0(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);;
			String objType = "IX_POI";
			long pid = 308;
			boolean isOnlyMain = false;
			boolean isLock = false;

			BasicObj obj = ObjSelector.selectByPid(conn, objType, null, pid, isOnlyMain, isLock);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	/*
	 * MultiSrcPoiSelectorConfig-specTables
	 */
	@Test
	public void test1(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			long pid = 308;
			boolean isOnlyMain = false;
			boolean isLock = false;
			
			MultiSrcPoiSelectorConfig multiSrcPoiSelectorConfig = MultiSrcPoiSelectorConfig.getInstance();
			BasicObj obj = ObjSelector.selectByPid(conn, objType, multiSrcPoiSelectorConfig, pid, isOnlyMain, isLock);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	

	/**
	 * MultiSrcPoiSelectorConfig-specTables
	 */
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
			boolean isOnlyMain = false;
			boolean isLock = false;
			boolean isNowait = false;
			
			MultiSrcPoiSelectorConfig multiSrcPoiSelectorConfig = MultiSrcPoiSelectorConfig.getInstance();
			//MultiSrcPoiSelectorConfig-specTables
			List<BasicObj> objList = ObjBatchSelector.selectByPids(conn, objType, multiSrcPoiSelectorConfig, pids, isOnlyMain, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	/**
	 * MultiSrcPoiSelectorConfig=NULL
	 */
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
			boolean isOnlyMain = false;
			boolean isLock = false;
			boolean isNowait = false;

			List<BasicObj> objList = ObjBatchSelector.selectByPids(conn, objType, null, pids, isOnlyMain, isLock,isNowait);
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
}
