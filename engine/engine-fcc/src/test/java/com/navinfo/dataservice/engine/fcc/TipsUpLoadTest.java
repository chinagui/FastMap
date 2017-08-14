package com.navinfo.dataservice.engine.fcc;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;

public class TipsUpLoadTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	//private Connection conn;

	public TipsUpLoadTest() throws Exception {
		// this.conn = DBConnector.getInstance().getConnectionById(11);
	}

	@Test
	public void tesUpload() {
	//	String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLINK\",\"projectId\":11,\"objId\":100002773}";
		try {
			TipsUpload a = new TipsUpload(806);
			a.runQuality("F:/FCC/rd_qcRecord.txt");
            JSONObject result = new JSONObject();
            result.put("qcTotal", a.getQcTotal());
            result.put("qcReasons", a.getQcReasons());
			result.put("qcErrMsg", a.getQcErrMsg());
			//a.run("E:/87/tips.txt");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testbatchTaskId() {
	//	String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLINK\",\"projectId\":11,\"objId\":100002773}";
		try {
			JSONObject  json=new JSONObject();
			json.put("g_guide", "{\"type\":\"Point\",\"coordinates\":[116.48137,40.01349]}");
			TipsUpload  l=new TipsUpload(0);
			l.updateTaskIds(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


}