package com.navinfo.dataservice.engine.edit.luyao.rdbranch;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdBranchTest extends InitApplication{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void init() {
		initContext();
	}
	
	@Test
	public void createRdBranchTest_1() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":1,\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_1() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671,\"pid\":100000728,\"details\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"nameKind\":3,\"pid\":100000250}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_2() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":2,\"inLinkPid\":100006717,\"nodePid\": 100023848,\"outLinkPid\": 100006720}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_6() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":6,\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_6() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671,\"pid\":100000728,\"signasreals\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"svgfileCode\":\"3\",\"pid\":100005932}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_8() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":8,\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_8() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671,\"pid\":100000728,\"schematics\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"schematicCode\":\"321\",\"pid\":100005921}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	@Test
	public void createRdBranchTest_9() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":9,\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_9() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671,\"pid\":100000728,\"signboards\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"backimageCode\":\"3\",\"pid\":100005904}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	
	@Test
	public void createRdBranchTest_5() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":5,\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_5() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671,\"pid\":100000728,\"realimages\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"realCode\":\"3\",\"rowId\":\"2A81085C8F504EAC969CF0968FC42EF3\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	
	@Test
	public void createRdBranchTest_7() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"branchType\":7,\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void updateRdBranchTest_7() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDBRANCH\",\"dbId\":42,\"data\":{\"pid\":100000857,\"objStatus\":\"UPDATE\",\"seriesbranches\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"123\",\"patternCode\":\"3\",\"rowId\":\"043E31E7F09345FE87A94E52F518D5FB\"}]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	

	@Test
	public void updateRdBranchTest_OutLink() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RDBRANCH\",\"objId\":\"100000806\",\"data\":{\"nodePid\":\"51728089\",\"inLinkPid\":\"58253132\",\"outLinkPid\":\"58253135\",\"pid\":100000806,\"objStatus\": \"UPDATE\"}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	
	
	
	
	
	@Test
	public void createRdBranchTest_0719() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDBRANCH\",\"data\":{\"branchType\":3,\"inLinkPid\":722876,\"nodePid\":469534,\"outLinkPid\":577197}}";
		
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}
	

	@Test
	public void update_0827() throws Exception {
		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":42,\"data\":{\"details\":[{\"estabType\":3,\"pid\":100000438,\"objStatus\":\"UPDATE\"}],\"pid\":23037}}";
		
		
		log.info(parameter);
		Transaction t = new Transaction(parameter);		;
		String msg = t.run();
	}

}
