package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelation;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationType;

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
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames,false, pid, isLock);
			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(308);
			poiRelation.setFatherPid(317);
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			result.isObjExist(obj);
			result.putObj(obj);
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
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
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames,false, pid, isLock);
			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherPid(317);
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			result.putObj(obj);
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
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
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, null,true, pid, isLock);
			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherPid(0);
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			result.putObj(obj);
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
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

			Map<Long,BasicObj> objMap = ObjBatchSelector.selectByPids(conn, objType, null,true, pids, isLock,isNowait);

			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(308);
			poiRelation.setFatherPid(316);
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			for(Map.Entry<Long, BasicObj> entry:objMap.entrySet()){
				result.putObj(entry.getValue());
			}
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
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

			Map<Long,BasicObj> objMap = ObjBatchSelector.selectByPids(conn, objType, tabNames,false, pids, isLock,isNowait);

			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherPid(308);
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			for(Map.Entry<Long, BasicObj> entry:objMap.entrySet()){
				result.putObj(entry.getValue());
			}
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	//改
	public void test20(){
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

			Map<Long,BasicObj> objMap = ObjBatchSelector.selectByPids(conn, objType, null,false, pids, isLock,isNowait);

			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherFid("0335100531LS100266");
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			for(Map.Entry<Long, BasicObj> entry:objMap.entrySet()){
				result.putObj(entry.getValue());
			}
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
	@Test
	//改
	public void test21(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
			String objType = "IX_POI";
			Collection<Long> pids = new ArrayList<Long>();
			pids.add((long) 158982);//原子
			pids.add((long) 308);//新父
			boolean isLock = false;
			boolean isNowait = false;

			Map<Long,BasicObj> objMap = ObjBatchSelector.selectByPids(conn, objType, null,false, pids, isLock,isNowait);

			//构造PoiRelationImporterCommand
			Set<PoiRelation> poiCollectionSet = new HashSet<PoiRelation>();
			PoiRelation poiRelation = new PoiRelation();
			poiRelation.setPid(158982);
			poiRelation.setFatherFid("0335100531LS100266");
			poiRelation.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			
			PoiRelation poiRelation2 = new PoiRelation();
			poiRelation2.setPid(158982);
			poiRelation2.setFatherFid("0010060909HYX00856");
			poiRelation2.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
			poiCollectionSet.add(poiRelation2);
			
			PoiRelationImportorCommand poiRelationImporterCommand = new PoiRelationImportorCommand();
			poiRelationImporterCommand.setPoiRels(poiCollectionSet);
			
			OperationResult result = new OperationResult();
			for(Map.Entry<Long, BasicObj> entry:objMap.entrySet()){
				result.putObj(entry.getValue());
			}
			PoiRelationImportor imp = new PoiRelationImportor(conn,result);
			imp.operate(poiRelationImporterCommand);
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
