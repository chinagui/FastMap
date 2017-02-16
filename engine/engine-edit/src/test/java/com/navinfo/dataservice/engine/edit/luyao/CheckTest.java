package com.navinfo.dataservice.engine.edit.luyao;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class CheckTest extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void RdLink002() throws Exception {
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":303000020,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.25006,40.58348],[116.24999910593033,40.58333165826473],[116.25028,40.58339]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
		
		
		
		
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	
	
	@Test
	public void GLM11024() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"objId\":207000053,\"data\":{\"details\":[{\"branchType\":1,\"estabType\":9,\"nameKind\":9,\"rowId\":\"C893EF14A08A4D52A8BB941597DBC296\",\"pid\":210000040,\"objStatus\":\"UPDATE\"}],\"rowId\":\"DCCA46349E9D4372AD2A804F5A75E8BB\",\"pid\":207000053,\"objStatus\":\"UPDATE\"}}";
		
		
		
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	
}
