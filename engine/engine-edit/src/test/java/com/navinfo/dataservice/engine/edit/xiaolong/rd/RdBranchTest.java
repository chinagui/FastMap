package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.xiaolong.InitApplication;

public class RdBranchTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAdd3dBranch()
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
}
