package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;

public class RdSlopeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	private Connection conn;
	public RdSlopeTest() throws Exception {
		//this.conn = DBConnector.getInstance().getConnectionById(11);
		//parameter={"command":"CREATE","dbId":42,"type":"RDSLOPE","data":{"nodePid":100022836,"linkPid":100007426,"linkPids":[100007427]}}
	}
	@Test
	public void TestAdd() {
		
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDSLOPE\",\"dbId\":42,\"data\":{\"nodePid\": 100022836,\"linkPid\":100007426,\"linkPids\":[100007427]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddRdLink()
	{
		//
		//parameter={"command":"CREATE","dbId":42,"data":{"eNodePid":0,"sNodePid":0,"geometry":{"type":"LineString","coordinates":},"catchLinks":[]},"type":"RDLINK"}
		String line= "[[116.74960479140282,39.00004566522346],[116.749634295702,38.99995186416615]]";
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"dbId\":42," +
				"\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\"," +
				"\"coordinates\":"+line+"},\"catchLinks\":[]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void TrackRdLink() throws Exception{
		//创建起始link LINESTRING (116.20091 39.84598, 116.20095 39.84568, 116.20111 39.84551)
		// PID 100002627 s 100018779 e 100018780
		//1.LINESTRING (116.20111 39.84551, 116.20122 39.84585) pid 100002628 s 100018780    e  100018781
		//2.LINESTRING (116.20111 39.84551, 116.20133 39.84551, 116.20156 39.84551) pid 100002629 s  100018780 e  100018782
		//3.LINESTRING (116.20111 39.84551, 116.20133 39.84536, 116.20166 39.84544) pid 100002637 s  100018780 e 100018787
		//4. LINESTRING (116.20111 39.84551, 116.20081 39.84565, 116.20083 39.84554) pid 100002641 s  100018780    e 100018791
		int cuurentLinkPid = 100003385 ;
		int cruuentNodePidDir = 100019726;
		List<RdLink> links  =new RdLinkSearchUtils(conn).getNextTrackLinks(cuurentLinkPid, cruuentNodePidDir,11);
		for(RdLink rdLink:links){
			System.out.println(rdLink.getPid());
		}
	}
	
	@Test
	public void departRdLink()
	{
		String line  = "[100005822,100005823]";
		String parameter =  "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"distance\":25.3,\"dbId\":42,\"data\":{\"linkPids\":"+line+"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSet(){
		List<Boolean> booleans = new ArrayList<Boolean>();
		booleans.add(false);
		booleans.add(false);
		booleans.add(false);
		booleans.add(true);
		if(!booleans.contains(true)){
			System.out.println("kkv5");
		}
		
	}
	@Test
	
	public void breankRdLink(){
		//05d420113061454b8b45083f2ff15dd8
		
		//AFABA38D96AC41E09E0A385F653C8FA0
		//72ade565c57c4747986b6402382908d6
		//parameter:{"command":"BREAK","dbId":42,"objId":100007804,"data":{"longitude":116.64105676404851,"latitude":39.827078818848676},"type":"RDLINK"}
		//access_token:00000002IRBME52A8A7DE96FAC8D03B1AB9ECA00A2977965
		//parameter:{"command":"CREATE","dbId":42,"objId":100007804,"data":{"longitude":116.64105541766425,"latitude":39.827078822935626},"type":"RDNODE"}
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100007804,\"data\":{\"longitude\":116.64105676404851,\"latitude\":39.827078818848676},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testLoadTractLink() throws Exception{
		List<Integer> pids = new ArrayList<Integer>();
		pids.add(20465744);
		pids.add(20465745);
		pids.add(14226884);
		System.out.println(pids);
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> links =linkSelector.loadByPids(pids, true);
		for(RdLink r:links){
			System.out.println(r.getPid());
		}
	}
}
