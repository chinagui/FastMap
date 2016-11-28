package com.navinfo.dataservice.engine.editplus;

import java.sql.Connection;
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
import com.navinfo.dataservice.dao.plus.selector.IxPoiSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.convert.MultiSrcPoiConvertor;

import net.sf.json.JSONObject;

/** 
 * @ClassName: IxPoiSelectorTest
 * @author songdongyan
 * @date 2016年11月28日
 * @Description: IxPoiSelectorTest.java
 */
public class IxPoiSelectorTest {

	/**
	 * 
	 */
	public IxPoiSelectorTest() {
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
			Set<Long> pidList = new HashSet<Long>();
			pidList.add((long) 308);
			pidList.add((long) 37993);
			IxPoiSelector.getIxPoiParentMapByChildrenPidList(conn, pidList);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
