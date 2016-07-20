package com.navinfo.dataservice.bizcommons;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datarow.CkResultTool;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;

/** 
 * @ClassName: CkResultToolTest
 * @author xiaoxiaowen4127
 * @date 2016年7月7日
 * @Description: CkResultToolTest.java
 */
public class CkResultToolTest extends ClassPathXmlAppContextInit {
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
	}
	@Test
	public void generateCkResultGrid_01(){
		try{
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo db = datahub.getDbById(192);
			OracleSchema schema = new OracleSchema(DbConnectConfig.createConnectConfig(db.getConnectParam()));
			CkResultTool.generateCkResultGrid(schema);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void moveNiVal_01(){
		try{
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo dbSrc = datahub.getDbById(100);
			OracleSchema srcSchema = new OracleSchema(DbConnectConfig.createConnectConfig(dbSrc.getConnectParam()));
			DbInfo tarDb = datahub.getDbById(25);
			OracleSchema tarSchema = new OracleSchema(DbConnectConfig.createConnectConfig(tarDb.getConnectParam()));
			Set<Integer> grids = new HashSet<Integer>();
			grids.add(59567101);
			grids.add(59567102);
			grids.add(59567103);
			CkResultTool.moveNiVal(srcSchema, tarSchema, grids);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
