package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.rd.utils.RdLinkSearchUtils;

public class RdLinkTest {
	private Connection conn;
	public RdLinkTest() throws Exception {
		this.conn = DBConnector.getInstance().getConnectionById(11);
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
	
	public void TrackRdLink() throws Exception{
		//创建起始link LINESTRING (116.20091 39.84598, 116.20095 39.84568, 116.20111 39.84551)
		// PID 100002627 s 100018779 e 100018780
		//1.LINESTRING (116.20111 39.84551, 116.20122 39.84585) pid 100002628 s 100018780    e  100018781
		//2.LINESTRING (116.20111 39.84551, 116.20133 39.84551, 116.20156 39.84551) pid 100002629 s  100018780 e  100018782
		//3.LINESTRING (116.20111 39.84551, 116.20133 39.84536, 116.20166 39.84544) pid 100002637 s  100018780 e 100018787
		//4. LINESTRING (116.20111 39.84551, 116.20081 39.84565, 116.20083 39.84554) pid 100002641 s  100018780    e 100018791
		int cuurentLinkPid = 100003385 ;
		int cruuentNodePidDir = 100019726;
		List<RdLink> links  =new RdLinkSearchUtils(conn).getNextTrackLinks(cuurentLinkPid, cruuentNodePidDir);
		for(RdLink rdLink:links){
			System.out.println(rdLink.getPid());
		}
	}
	public void departRdLink()
	{
		String line  = "[100003385,100003386,100003387,100003389,100003397]";
		String parameter =  "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"distance\":20,\"projectId\":11,\"data\":{\"linkPids\":"+line+"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
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
	private void testLoadTractLink() throws Exception{
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> nextLinks = linkSelector.loadTrackLink(100003389,100019730,true);
		for(RdLink link:nextLinks){
			System.out.println(link.getPid());
		}
	}
	
	private static void testAddLinkIn2Mesh()
	{
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.70726 ,40.09797],[116.70878,40.06482]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			testAddLinkIn2Mesh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
