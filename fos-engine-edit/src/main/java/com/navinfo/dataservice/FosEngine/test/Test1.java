package com.navinfo.dataservice.FosEngine.test;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.db.OracleAddress;
import com.navinfo.dataservice.FosEngine.comm.util.UuidUtils;
import com.navinfo.dataservice.FosEngine.tips.TipsBuilder;

//import com.navinfo.comm.util.SerializeUtils;





public class Test1 {
	

	public static void main(String[] args) throws Exception {

		String uuid = UuidUtils.genUuid();
		
		String username1 = "beijing11";
		
		String password1 ="beijing11";
		
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
		
		b.run(oa1, oa2, uuid);
		
		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(39.899996, 116.899996, 16));
		
		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(39.899999, 116.899999, 16));
		
		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(39.899998, 116.899994, 16));
	}


}
