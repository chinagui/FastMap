package com.navinfo.dataservice.engine.edit.zhaokk.ad;

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

public class AdNodeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Test
	public void createAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADNODE\",\"projectId\":11,\"objId\":100031211,\"data\":{\"longitude\":116.22590,\"latitude\":39.77897}}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}
	
	@Test
	public void deleteAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADNODE\",\"projectId\":11,\"objId\":100022085}}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}
	
	@Test
	public void updateAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADNODE\",\"projectId\":11,\"data\":{\"kind\":\"3\",\"pid\":100021717,\"objStatus\":\"UPDATE\"}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void moveAdNodeTest() {
		//{"command":"MOVE","dbId":42,"objId":100025259,"data":{"longitude":120.74972122907639,"latitude":38.25230645626886},"type":"ADNODE"}
		try{
			//{"command":"MOVE","dbId":42,"objId":100024881,"data":{"longitude":113.4993615746498,"latitude":40.49997332512635},"type":"ADNODE"}
			//parameter:{"command":"MOVE","dbId":42,"objId":100025047,"data":{"longitude":116.25012651085854,"latitude":38.583638456189604},"type":"ADNODE"}
			//{"command":"MOVE","dbId":42,"objId":100025213,"data":{"longitude":117.62493565678595,"latitude":39.25030419811251},"type":"ADNODE"}
			String parameter = "{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025259,\"data\":{\"longitude\":120.74972122907639,\"latitude\":38.25230645626886},\"type\":\"ADNODE\"}";
			
			
			
			log.info(parameter);
			Transaction t = new Transaction(parameter);
			;
			String msg = t.run();
		}
		catch (Exception e) {
			log.info(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchByGap() {
		String parameter = "{\"projectId\":11,\"gap\":80,\"types\":[\"ADNODE\"],\"z\":17,\"x\":107945,\"y\":49615}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		JSONArray type = jsonReq.getJSONArray("types");

		int projectId = jsonReq.getInt("projectId");

		int x = jsonReq.getInt("x");

		int y = jsonReq.getInt("y");

		int z = jsonReq.getInt("z");

		int gap = jsonReq.getInt("gap");

		List<ObjType> types = new ArrayList<ObjType>();

		for (int i = 0; i < type.size(); i++) {
			types.add(ObjType.valueOf(type.getString(i)));
		}

		try {
			SearchProcess p = new SearchProcess(
					DBConnector.getInstance().getConnectionById(projectId));
			JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

			System.out.println(ResponseUtils.assembleRegularResult(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) {
		RdLinkName name = new RdLinkName();
		if(name.getName() == null){
			System.out.println("ddddd");
			System.out.println("'"+name.getName()+"'");
		}
		if(name.getName() == ""){
			System.out.println("dddfafdsf");
		}
	}
}
