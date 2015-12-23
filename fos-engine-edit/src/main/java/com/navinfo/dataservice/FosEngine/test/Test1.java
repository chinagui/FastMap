package com.navinfo.dataservice.FosEngine.test;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.FosEngine.tips.TipsBuilder;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.util.UuidUtils;

//import com.navinfo.comm.util.SerializeUtils;





public class Test1 {
	

	public static void main(String[] args) throws Exception {

		String uuid = UuidUtils.genUuid();
		
		String username1 = "gdb240_15win_ml_6p_1216";
		
		String password1 ="gdb240_15win_ml_6p_1216";
		
		int port1 =1521;
		
		String ip1 = "192.168.4.131";
		
		String serviceName1 = "orcl";
		
		OracleAddress oa1 = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		String username2 = "fmgdb5";
		
		String password2 ="fmgdb5";
		
		int port2 =1521;
		
		String ip2 = "192.168.4.131";
		
		String serviceName2 = "orcl";
		
		OracleAddress oa2 = new OracleAddress(username2,password2,port2,ip2,serviceName2);
		
		TipsBuilder b = new TipsBuilder();
		
		HBaseAddress.initHBaseAddress("192.168.3.156");
		
		b.run(oa1, oa2, uuid,"http://192.168.4.130:8081/solr/tips");
		
	}


}
