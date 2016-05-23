package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdBranchTest {
	public static void testAdd3dBranch()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"projectId\":11,\"data\":{\"branchType\":3,\"inLinkPid\":88026344,\"nodePid\":74186157,\"outLinkPid\":88026344}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testAdd3dBranch();
	}
}
