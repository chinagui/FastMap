package com.navinfo.dataservice.engine.edit.zhaokk.zone;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ZoneNodeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Test
	public void createAdNodeTest() throws Exception {
		//{"command":"BREAK","dbId":42,"objId":100033806,"data":{"longitude":117.99929871228692,"latitude":39.83308947693323},"type":"ZONENODE"}
		//{"command":"BREAK","dbId":42,"objId":100033806,"data":{"longitude":117.9994136153139,"latitude":39.83314081078462},"type":"ZONENODE"}
		
		String parameter = "{\"command\":\"BREAK\",\"type\":\"ZONENODE\",\"dbId\":42,\"objId\":100033835,\"data\":{\"longitude\":117.99954088700899,\"latitude\":83296472974102}}";
		Transaction t = new Transaction(parameter);;
		String msg = t.run();
	}
}
