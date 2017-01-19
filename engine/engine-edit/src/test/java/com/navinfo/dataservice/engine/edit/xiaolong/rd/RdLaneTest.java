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
		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":19,\"distance\":\"9.8\",\"data\":{\"linkPids\":[202003226,300003318]}}";
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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLANECONNEXITY\",\"objId\":208000048,\"data\":{\"laneInfo\":\"a<a>,c\",\"rowId\":\"84FBB659321B48A7BEE994899E394989\",\"pid\":208000048,\"objStatus\":\"UPDATE\"}}";
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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"RDLINK\",\"objId\":306003112,\"data\":{\"forms\":[{\"linkPid\":306003112,\"rowId\":\"\",\"formOfWay\":20,\"extendedForm\":0,\"auxiFlag\":0,\"kgFlag\":0,\"objStatus\":\"INSERT\"},{\"rowId\":\"480116B8BAF04601B1EE8547EE24B47E\",\"objStatus\":\"UPDATE\",\"formOfWay\":50}],\"pid\":306003112}}";
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
