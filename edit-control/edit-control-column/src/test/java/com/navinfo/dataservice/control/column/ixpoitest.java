package com.navinfo.dataservice.control.column;


import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;

public class ixpoitest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test() {
		Connection conn = null;
		try {
//			conn = DBConnector.getInstance().getConnectionById(17);
//			IxPoiSearch search = new IxPoiSearch(conn);
//			List<String> rowIds = new ArrayList<String>();
//			rowIds.add("3AE1FB4B0B4392F7E050A8C08304EE4C");
//			search.searchColumnPoiByRowId("poi_address", "addrPinyin", rowIds, "1", "CHI");
			ColumnCoreControl control = new ColumnCoreControl();

			control.applyData(0, "poi_name", 2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}
	

}
