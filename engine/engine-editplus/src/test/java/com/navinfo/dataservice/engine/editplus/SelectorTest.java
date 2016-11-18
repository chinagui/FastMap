package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.selector.MultiSrcPoiSelectorConfig;
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

	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-editplus.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public static void main(String[] args){
		try{
			
			//初始化context
			initContext();

			Connection conn = null;
			conn = DBConnector.getInstance().getConnectionById(17);
//			String objType = "FEATURE";
			String objType = "IX_POI";
			long pid = 308;
			boolean isOnlyMain = false;
			boolean isLock = false;
			
			MultiSrcPoiSelectorConfig multiSrcPoiSelectorConfig = MultiSrcPoiSelectorConfig.getInstance();
			BasicObj obj = ObjSelector.selectByPid(conn, objType, multiSrcPoiSelectorConfig, pid, isOnlyMain, isLock);
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
