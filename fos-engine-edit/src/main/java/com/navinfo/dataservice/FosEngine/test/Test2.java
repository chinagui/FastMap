package com.navinfo.dataservice.FosEngine.test;

import com.navinfo.dataservice.FosEngine.edit.operation.Transaction;
import com.navinfo.dataservice.commons.db.ConfigLoader;



public class Test2{

	public static void main(String[] args) throws Exception{
		
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/git/FosEngine/FosEngine/src/config.properties");
		
		String a = "{\"command\":\"createrestriction\",\"projectId\":1,\"data\":{\"nodePid\":245336,\"inLinkPid\":351954,\"outLinkPids\":[351952]}}";
		Transaction t = new Transaction(a);
		
		System.out.print(t.run());
		System.out.print(t.getLogs());
		
	}

}
