package com.navinfo.dataservice.engine.edit.zhaokk.zone;


import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.edit.search.rd.utils.RdLinkSearchUtils;

import net.sf.json.JSONObject;

/**
 * @author zhaokk
 *
 */
public class ZoneLinkTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	//初始化系统参数
	private Connection conn;
	protected Logger log = Logger.getLogger(this.getClass());
	//创建一条link
	@Test
	public void tesRepairtZoneLink()
	{
		//{"command":"REPAIR","dbId":42,"objId":100034125,"data":{"geometry":{"type":"LineString","coordinates":[[116.49603,38.00015],[116.49610787630081,37.999949311603245],[116.49624,38.00013],[116.49603,38.00015]]},"interLinks":[],"interNodes":[]},"type":"ZONELINK"}
		//{"command":"REPAIR","dbId":42,"objId":100034144,"data":{"geometry":{"type":"LineString","coordinates":[[116.49564,38.00018],[116.49548,38.00011],[116.49573370814322,37.999949311603245],[116.49564,38.00018]]},"interLinks":[],"interNodes":[]},"type":"ZONELINK"}
		
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100034144,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49564,38.00018],[116.49548,38.00011],[116.49573370814322,37.999949311603245],[116.49564,38.00018]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"ZONELINK\"}";
		
		log.info(parameter);
		System.out.println(parameter+"-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
}
