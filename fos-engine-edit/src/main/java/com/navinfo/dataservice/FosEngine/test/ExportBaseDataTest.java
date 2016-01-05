package com.navinfo.dataservice.FosEngine.test;

import com.navinfo.dataservice.FosEngine.export.ExportBaseData;
import com.navinfo.dataservice.commons.db.OracleAddress;

public class ExportBaseDataTest {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String username1 = "fmgdb14";
		
		String password1 ="fmgdb14";
		
		int port1 =1521;
		
		String ip1 = "192.168.4.131";
		
		String serviceName1 = "orcl";
		
		OracleAddress oa1 = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		ExportBaseData.exportBaseData(oa1,"c:/1");
	}

}
