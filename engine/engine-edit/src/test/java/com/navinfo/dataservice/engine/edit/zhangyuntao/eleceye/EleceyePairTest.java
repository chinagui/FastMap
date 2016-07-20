package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;

public class EleceyePairTest extends InitApplication {

	@Override
	public void init() {
		super.initContext();
	}
	
	@Test
	public void createEleceyePair(){
		String requester = "{'dbId':43,'command':'CREATE','type':'RDELECEYEPAIR','data':{'pid1':100281917,'pid2':100281916}}";
		TestUtil.run(requester);
	}
	
	@Test
	public void deleteEleceyePair(){
		String requester = "{'dbId':43,'command':'DELETE','type':'RDELECEYEPAIR','data':{'pid':100281916}}";
		TestUtil.run(requester);
	}
	
	@Test 
	public void getEleceyePair(){
		
	}
	
}
