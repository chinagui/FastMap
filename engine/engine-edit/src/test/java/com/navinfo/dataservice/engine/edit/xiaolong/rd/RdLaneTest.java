/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

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
	public void testLoadRdLaneByCondition()
	{
		String parameter = "{\"linkPid\":\"210001313\",\"laneDir\":2}";
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			JSONObject jsonReq2 = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.RDLANE, jsonReq2));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testUpdatRdLaneByLinkKind()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"RDLINK\",\"objId\":306003206,\"data\":{\"kind\":9,\"pid\":306003206,\"objStatus\":\"UPDATE\",\"routeAdopt\":0,\"limits\":[{\"linkPid\":306003206,\"rowId\":\"\",\"type\":3,\"limitDir\":0,\"timeDomain\":\"\",\"vehicle\":0,\"tollType\":9,\"weather\":9,\"inputTime\":\"\",\"processFlag\":2,\"objStatus\":\"INSERT\"}]}}";
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
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDTOLLGATE\",\"dbId\":19,\"objId\":308000011}";
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
		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"details\":[{\"patternCode\":\"80211009\",\"rowId\":\"F309CB33349E440BB968F86230506DC8\",\"pid\":208000044,\"objStatus\":\"UPDATE\",\"arrowCode\":\"10211009\"}],\"pid\":304000052},\"objId\":304000052}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateRdLaneByDeleteBranch()
	{
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"detailId\":208000044,\"rowId\":\"\",\"branchType\":0}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateByLinkForm()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"RDLINK\",\"objId\":200003252,\"data\":{\"forms\":[{\"rowId\":\"CE0543AF440A4974A15B9879429F49A4\",\"objStatus\":\"UPDATE\",\"formOfWay\":0}],\"pid\":200003252}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
