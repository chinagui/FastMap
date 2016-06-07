package com.navinfo.dataservice.engine.edit.ad;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.edit.search.rd.utils.RdLinkSearchUtils;

/**
 * @author zhaokk
 *
 */
public class AdLinkTest {
	
	//初始化系统参数
	private Connection conn;
    public AdLinkTest() throws Exception{
    	 this.conn = GlmDbPoolManager.getInstance().getConnection(11);
    }
	protected Logger log = Logger.getLogger(this.getClass());
	//创建一条link
	public  void createAdLinkTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADLINK\",\"projectId\":11," +
		"\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[117.37423092126846,38.91671492709221],[117.37430199980734,38.91657301643589]]},\"catchLinks\":[]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//删除一条LINK
	public  void deleteAdLinkTest() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADLINK\",\"projectId\":11,\"objId\":100031492}";
		log.info(parameter);
		System.out.println(parameter+"-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//打断一条LINK
	public  void breakAdLinkTest() {
		//{"command":"BREAK","projectId":11,"objId":100031682,"data":{"longitude":116.4677675266779,"latitude":40.01207106100581},"type":"ADLINK"}
		//"{"command":"BREAK","projectId":11,"objId":100031679,"data":{"longitude":116.46851064297599,"latitude":40.01208957670038},"type":"ADLINK"}"
		//{"command":"BREAK","projectId":11,"objId":100031676,"data":{"longitude":116.47621786669173,"latitude":40.01248730218289},"type":"ADLINK"}
		String parameter = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100031676,\"data\":{\"longitude\":116.47621786669173,\"latitude\":40.01248730218289},\"type\":\"ADLINK\"}";
		String parameter1 = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100031682,\"data\":{\"longitude\":116.4677675266779,\"latitude\":40.01207106100581},\"type\":\"ADLINK\"}";
		
		log.info(parameter);
		System.out.println(parameter+"-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
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
	
	public void testSearchAdLink()
	{
		String parameter = "{\"projectId\":11,\"type\":\"ADLINK\",\"pid\":100031492}";
		
		Connection conn;
			try {
				conn = GlmDbPoolManager.getInstance().getConnection(11);
				
				JSONObject jsonReq = JSONObject.fromObject(parameter);

				SearchProcess p = new SearchProcess(conn);

				System.out.println(p.searchDataByPid(ObjType.ADLINK, 100031492).Serialize(ObjLevel.FULL));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	
	
	public void tesRepairtAdLink()
	{
		String parameter = "{\"command\":\"REPAIR\",\"projectId\":11,\"objId\":100032727,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.62528,39.25022],[116.62528,39.25006],[116.62535838820631,39.25011395094421],[116.62544,39.25017],[116.62528,39.25022]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"ADLINK\"}";
		
		log.info(parameter);
		System.out.println(parameter+"-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void testSearchAdNode()
	{
		Connection conn;
			try {
				conn = GlmDbPoolManager.getInstance().getConnection(11);
				
				SearchProcess p = new SearchProcess(conn);

				System.out.println(p.searchDataByPid(ObjType.ADNODE, 100021766).Serialize(ObjLevel.FULL));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public static void main(String[] args) throws Exception{
		//new AdLinkTest().deleteAdLinkTest();
		//new AdLinkTest().TrackRdLink();
		new AdLinkTest().tesRepairtAdLink();
		
	}
}
