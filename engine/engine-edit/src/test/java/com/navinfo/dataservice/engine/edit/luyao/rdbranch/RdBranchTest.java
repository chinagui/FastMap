package com.navinfo.dataservice.engine.edit.luyao.rdbranch;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdBranchTest extends InitApplication{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void init() {
		initContext();
	}
	
	

	@Test
	public void createRdBranch8Test() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":8,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranch8Test() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"schematics\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"branchPid\":100005904,\"imageType\":1,\"realCode\":\"123\",\"rowId\":\"C95ED1D783924C2B8F51FE6914A50C68\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"realimages\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"\",\"branchPid\":100005904,\"imageType\":1,\"realCode\":\"123\",\"rowId\":\"C95ED1D783924C2B8F51FE6914A50C68\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	

}
