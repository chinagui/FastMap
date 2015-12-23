package com.navinfo.dataservice.FosEngine.test;

import com.navinfo.dataservice.FosEngine.tips.TipsBuilder;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class TipsBuildTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		TipsBuilder t = new TipsBuilder();
		
		String username2 = "beijing11";
		
		String password2 ="beijing11";
		
		int port2 =1521;
		
		String ip2 = "192.168.4.131";
		
		String serviceName2 = "orcl";
		
		OracleAddress fmgdbOA = new OracleAddress(username2,password2,port2,ip2,serviceName2);
		
		String username1 = "fmgdb5";
		
		String password1 ="fmgdb5";
		
		int port1 =1521;
		
		String ip1 = "192.168.4.131";
		
		String serviceName1 = "orcl";
		
		OracleAddress pmOA = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		HBaseAddress.initHBaseAddress("192.168.3.156");
		
		t.run(fmgdbOA, pmOA, UuidUtils.genUuid(),"http://192.168.4.130:8081/solr/tips");
	}

}
