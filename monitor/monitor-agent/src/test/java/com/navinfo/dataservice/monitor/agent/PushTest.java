package com.navinfo.dataservice.monitor.agent;

import java.util.Date;

import org.junit.Test;

import com.navinfo.dataservice.monitor.agent.utils.AgentUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PushTest {

	@Test
	public void test01() throws Exception{
		JSONArray jsa = new JSONArray();
		JSONObject jso = new JSONObject();
		Date date = new Date();
		int time = (int) date.getTime();
		
		jso.put("endpoint", "192.168.4.110");
		jso.put("metric", "fos.man.render");
		jso.put("timestamp", time);
		jso.put("step", 5);
		jso.put("value", 3);
		jso.put("counterType", "GAUGE");
		jso.put("tags", "service=man,project=render,module=tips");
		jsa.add(jso);
		String data = AgentUtils.pushData(jsa);
		System.out.println(data);
		
	}
}
