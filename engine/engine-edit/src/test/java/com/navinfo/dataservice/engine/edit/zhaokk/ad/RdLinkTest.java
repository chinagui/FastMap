package com.navinfo.dataservice.engine.edit.zhaokk.ad;

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

public class RdLinkTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	private Connection conn;
	public RdLinkTest() throws Exception {
		//this.conn = DBConnector.getInstance().getConnectionById(11);
	}
	
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLINK\",\"projectId\":11,\"objId\":100002773}";
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
		String linea= "[[116.02564,39.76918], [116.02601,39.76917]]";
		String lineb= "[[116.02601,39.76917], [116.02634,39.76921]]";
		String linec= "[[116.02634,39.76921], [116.02670,39.76922]]";
		String lined= "[[116.02670,39.76922], [116.02728,39.76921]]";
		String linee= "[[116.02728,39.76921], [116.02766,39.76909]]";
		
		String linef = "[[116.02601,39.76917], [116.02603,39.76889]]";
		String lineg = "[[116.02629,39.76954], [116.02634,39.76921]]";
		
		String lineh = "[[116.02670,39.76922], [116.02674,39.76891]]";
		
		String linek= "[[116.02670,39.76922], [116.02656,39.76950]]";
		String linel ="[[116.02670,39.76922], [116.02683,39.76956]]";
		String linem = "[[116.02728,39.76921], [116.02775,39.76921]]";
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"projectId\":11," +
				"\"data\":{\"eNodePid\":0,\"sNodePid\":100019730,\"geometry\":{\"type\":\"LineString\"," +
				"\"coordinates\":"+linem+"},\"catchLinks\":[]}}";
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
	
	
	//{"command":"UPDOWNDEPART","type":"RDLINK","dbId":43,"distance":"10.0","data":{"linkPids":[100006797,100006790,100006789,719802,719801,596719,582698,574124,588624,724729,724731,724721,724722,572598,574104,574105,584128,574101,581392,730940,86927696,19613244,19613245,727663,584391,730945,730947,727854,15444003,15444004,732739,722007,88026242,88026245,88026246,19613249,19613248,723176,721348,723185,85206511,86035226,86035225,719835,584262,86035239]}}
	public void departRdLink()
	{
		String line  = "[100006797,100006790,100006789,719802,719801,596719,582698,574124,588624,724729,724731,724721,724722,572598,574104,574105,584128,574101,581392,730940,86927696,19613244,19613245,727663,584391,730945,730947,727854,15444003,15444004,732739,722007,88026242,88026245,88026246,19613249,19613248,723176,721348,723185,85206511,86035226,86035225,719835,584262,86035239]";
		String parameter =  "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"distance\":25.3,\"dbId\":43,\"data\":{\"linkPids\":"+line+"}}";
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
