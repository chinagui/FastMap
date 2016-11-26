package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.engine.editplus.log.LogGenerator;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjFactory;
import com.navinfo.dataservice.engine.editplus.model.selector.MultiSrcPoiSelectorConfig;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;
import com.navinfo.navicommons.database.sql.RunnableSQL;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: LogTest
 * @author songdongyan
 * @date 2016年11月25日
 * @Description: LogTest.java
 */
public class LogTest {

	/**
	 * 
	 */
	public LogTest() {
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
			long pid = 308;
			boolean isLock = false;

			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");
			
			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames, pid, isLock);
			
			obj.getMainrow().setAttrByCol("KIND_CODE", "");
			List<BasicRow> ixPoiNameList = obj.getRowsByName("IX_POI_NAME");
			ixPoiNameList.get(0).setAttrByCol("NAME_TYPE", 2);
			IxPoiName ixPoiName = (IxPoiName) ObjFactory.getInstance().createRow("IX_POI_NAME", obj.objPid());
			ixPoiName.setPoiPid(obj.objPid());
			ixPoiName.setLangCode("POR");
			ixPoiName.setNameGroupid(2);
			ixPoiName.setNameType(2);
			ixPoiName.setNameClass(3);
			ixPoiNameList.add(ixPoiName);
			obj.setSubrows("IX_POI_NAME", ixPoiNameList);
			
			List<BasicObj> basicObjs = new ArrayList<BasicObj>();
			basicObjs.add(obj);
			String opCmd = "UPDATE";
			int opSg = 1; 
			long userId = 1;
			
			for(BasicObj basicObj:basicObjs){
				List<RunnableSQL> runnableSqlList = basicObj.generateSql();
				for(RunnableSQL runnableSql:runnableSqlList){
					runnableSql.run(conn);
				}
			}
			
			LogGenerator.writeLog(conn, basicObjs, opCmd, opSg, userId);
//			conn.commit();
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}

	@Test
	public void test1(){
		try{
			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);;
			String objType = "IX_POI";
			long pid = 308;
			boolean isLock = false;
			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_FLAG");
			tabNames.add("IX_POI_NAME_TONE");
			tabNames.add("IX_POI_ADDRESS");
			tabNames.add("IX_POI_CONTACT");
			tabNames.add("IX_POI_FLAG");

			BasicObj obj = ObjSelector.selectByPid(conn, objType, tabNames, pid, isLock);
			
			Geometry geo = JtsGeometryFactory.read("LINESTRING(129.789321823 34.18782666,129.34455656 34.898776)");
			obj.getMainrow().setAttrByCol("GEOMETRY", JtsGeometryFactory.read("LINESTRING(129.789321823 34.18782666,129.34455656 34.898776)"));
			List<BasicRow> ixPoiNameList = obj.getRowsByName("IX_POI_NAME");
			ixPoiNameList.get(0).setAttrByCol("NAME_TYPE", 2);
			IxPoiName ixPoiName = (IxPoiName) ObjFactory.getInstance().createRow("IX_POI_NAME", obj.objPid());
			ixPoiName.setPoiPid(obj.objPid());
			ixPoiName.setLangCode("POR");
			ixPoiName.setNameGroupid(2);
			ixPoiName.setNameType(2);
			ixPoiName.setNameClass(3);
			ixPoiNameList.add(ixPoiName);
			obj.setSubrows("IX_POI_NAME", ixPoiNameList);
			
			List<BasicObj> basicObjs = new ArrayList<BasicObj>();
			basicObjs.add(obj);

			for(BasicObj basicObj:basicObjs){
				List<RunnableSQL> runnableSqlList = basicObj.generateSql();
				for(RunnableSQL runnableSql:runnableSqlList){
					runnableSql.run(conn);
				}
			}
			
			String opCmd = "UPDATE";
			int opSg = 1; 
			long userId = 1;
			LogGenerator.writeLog(conn, basicObjs, opCmd, opSg, userId);

//			conn.commit();
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
			conn = DBConnector.getInstance().getConnectionById(17);;
			
			BasicObj objNew = ObjFactory.getInstance().create("IX_POI");
			
			List<BasicObj> basicObjs = new ArrayList<BasicObj>();
			basicObjs.add(objNew);
			Geometry geo = JtsGeometryFactory.read("LINESTRING(129.789321823 34.18782666,129.34455656 34.898776)");
			objNew.getMainrow().setAttrByCol("GEOMETRY", geo);

			for(BasicObj basicObj:basicObjs){
				List<RunnableSQL> runnableSqlList = basicObj.generateSql();
				for(RunnableSQL runnableSql:runnableSqlList){
					runnableSql.run(conn);
				}
			}
			
			String opCmd = "CREATE";
			int opSg = 1; 
			long userId = 1;
			LogGenerator.writeLog(conn, basicObjs, opCmd, opSg, userId);

//			conn.commit();
			System.out.println("Over.");
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
