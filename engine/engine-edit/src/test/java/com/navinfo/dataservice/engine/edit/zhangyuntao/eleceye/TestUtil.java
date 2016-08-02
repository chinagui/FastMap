package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class TestUtil {
	public static void run(String requester) {
		Transaction t = new Transaction(requester);
		try {
			t.run();
			System.out.println(t.getPid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
