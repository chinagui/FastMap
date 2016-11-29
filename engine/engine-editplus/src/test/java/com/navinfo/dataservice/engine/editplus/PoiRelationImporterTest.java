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
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.convert.MultiSrcPoiConvertor;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelation;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImporterCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportor;

import net.sf.json.JSONObject;

/** 
 * @ClassName: PoiRelationImporterTest
 * @author songdongyan
 * @date 2016年11月29日
 * @Description: PoiRelationImporterTest.java
 */
public class PoiRelationImporterTest {

	/**
	 * 
	 */
	public PoiRelationImporterTest() {
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
	//增
	public void test0(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);;
			String objType = "IX_POI";
			long pid = 308;
			boolean isLock = false;

			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_ADDRESS");
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames, pid, isLock);
			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(308);
			poiRelation.setFatherPid(317);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImporterCommand poiRelationImporterCommand = new PoiRelationImporterCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			result.putObj(obj);
			new PoiRelationImportor(conn,result,poiRelationImporterCommand).operate();
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	//改
	public void test1(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);;
			String objType = "IX_POI";
			long pid = 158982;
			boolean isLock = false;

			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_ADDRESS");
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames, pid, isLock);
			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherPid(317);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImporterCommand poiRelationImporterCommand = new PoiRelationImporterCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			result.putObj(obj);
			new PoiRelationImportor(conn,result,poiRelationImporterCommand).operate();
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	//删
	public void test2(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);;
			String objType = "IX_POI";
			long pid = 158982;
			boolean isLock = false;

			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_ADDRESS");
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames, pid, isLock);
			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherPid(0);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImporterCommand poiRelationImporterCommand = new PoiRelationImporterCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			result.putObj(obj);
			new PoiRelationImportor(conn,result,poiRelationImporterCommand).operate();
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	//改
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
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_ADDRESS");

			List<BasicObj> objList = ObjBatchSelector.selectByPids(conn, objType, tabNames, pids, isLock,isNowait);

			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(308);
			poiRelation.setFatherPid(316);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImporterCommand poiRelationImporterCommand = new PoiRelationImporterCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			for(BasicObj obj:objList){
				result.putObj(obj);
			}
			new PoiRelationImportor(conn,result,poiRelationImporterCommand).operate();
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	//改
	public void test11(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			Collection<Long> pids = new ArrayList<Long>();
			pids.add((long) 159117);//原父
			pids.add((long) 158982);//原子
			pids.add((long) 308);//新父
			boolean isLock = false;
			boolean isNowait = false;
			
			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_ADDRESS");

			List<BasicObj> objList = ObjBatchSelector.selectByPids(conn, objType, tabNames, pids, isLock,isNowait);

			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherPid(308);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImporterCommand poiRelationImporterCommand = new PoiRelationImporterCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			for(BasicObj obj:objList){
				result.putObj(obj);
			}
			new PoiRelationImportor(conn,result,poiRelationImporterCommand).operate();
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
