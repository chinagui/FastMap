package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import com.navinfo.dataservice.engine.edit.operation.Transaction;

import net.sf.json.JSONObject;

public class TestUtil {
	public static void run(String requester) {
		Transaction t = new Transaction(requester);
		try {
			String msg = t.run();
			String log = t.getLogs();
			JSONObject json = new JSONObject();
			json.put("result", msg);
			json.put("log", log);
			json.put("check", t.getCheckLog());
			json.put("pid", t.getPid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
