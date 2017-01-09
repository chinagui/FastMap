/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

/** 
* @ClassName: RdLaneTest 
* @author Zhang Xiaolong
* @date 2017年1月5日 上午10:46:19 
* @Description: TODO
*/
public class RdLaneTest extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testUpdatRdLaneByLinkKind()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":309003117,\"data\":{\"kind\":9,\"pid\":309003117,\"objStatus\":\"UPDATE\",\"routeAdopt\":0,\"laneNum\":3,\"laneClass\":2,\"limits\":[{\"linkPid\":309003117,\"rowId\":\"\",\"type\":3,\"limitDir\":0,\"timeDomain\":\"\",\"vehicle\":0,\"tollType\":9,\"weather\":9,\"inputTime\":\"\",\"processFlag\":2,\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateRdLaneByLinkDirect()
	{	
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":206000006,\"data\":{\"direct\":3,\"pid\":206000006,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdatRdLaneByRdTollgate()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTOLLGATE\",\"dbId\":17,\"data\":{\"passageNum\":1,\"rowId\":\"AC3EC5679C5244FE8FD957FF994A9FEE\",\"pid\":208000021,\"objStatus\":\"UPDATE\",\"etcFigureCode\":\"T0110000\",\"passages\":[{\"pid\":0,\"seqNum\":1,\"tollForm\":1,\"cardType\":0,\"vehicle\":0,\"rowId\":\"\",\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdatRdLaneByTrafficsignal()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"inLinkPid\":201002710,\"nodePid\":210002133,\"outLinkPids\":[302002723],\"laneInfo\":\"a\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdatRdLaneByCrossLink()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":305000525,\"data\":{\"forms\":[{\"rowId\":\"C0EC16F073E342A3BBE9F705D363358F\",\"objStatus\":\"UPDATE\",\"formOfWay\":10}],\"pid\":305000525}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateByRdBranchPattern()
	{
		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"details\":[{\"patternCode\":\"80211009\",\"rowId\":\"B7DB167556E047828F3614313E497F83\",\"pid\":306000039,\"objStatus\":\"UPDATE\",\"arrowCode\":\"10211009\"}],\"pid\":220000035},\"objId\":220000035}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
