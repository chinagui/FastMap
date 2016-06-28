package com.navinfo.dataservice.engine.edit.luyao.rwnode;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RwNodeTest extends InitApplication{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void init() {
		initContext();
	}
	@Test
	public void createAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100031585,\"data\":{\"longitude\":116.47493102897037,\"latitude\":40.01395406127133},\"type\":\"ADLINK\"}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void deleteRwNodeTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RWNODE\",\"dbId\":42,\"objId\":5419}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	// "{"command":"UPDATE","type":"ADNODE","projectId":11,"data":{"kind":"12","pid":100021403,"objStatus":"UPDATE"}}"
	public void updateRwNodeTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADNODE\",\"projectId\":11,\"data\":{\"inLinkPid\":100005725,\"nodePid\":469291,\"outLinkPid\":719802,\"pid\":100000670,\"realimages\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"\",\"branchPid\":100000670,\"imageType\":1,\"realCode\":\"123\",\"rowId\":\"C95ED1D783924C2B8F51FE6914A50C68\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void moveRwNodeTest() throws Exception {
		String parameter = "{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100006009,\"data\":{\"longitude\":116.90934,\"latitude\":39.91718},\"type\":\"RWNODE\"}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}
	

}
