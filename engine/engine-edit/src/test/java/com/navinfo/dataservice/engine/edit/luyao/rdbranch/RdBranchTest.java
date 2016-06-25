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
	public void createRdBranchTest_1() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":1,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_1() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"details\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"nameKind\":3,\"pid\":100000250}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_6() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":6,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_6() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"signasreals\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"svgfileCode\":\"3\",\"pid\":100005932}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_8() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":8,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_8() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"schematics\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"schematicCode\":\"321\",\"pid\":100005921}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_9() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":9,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_9() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"signboards\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"backimageCode\":\"3\",\"pid\":100005904}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	
	@Test
	public void createRdBranchTest_5() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":5,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_5() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"realimages\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"realCode\":\"3\",\"rowId\":\"2A81085C8F504EAC969CF0968FC42EF3\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	
	@Test
	public void createRdBranchTest_7() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":7,\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_7() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":596721,\"nodePid\":471913,\"outLinkPid\":596721,\"pid\":100000728,\"seriesbranches\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"patternCode\":\"3\",\"rowId\":\"33EE2D15EC5A47F2AC92D144DDD69F7E\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	

	
	
	
	

}
