package com.navinfo.dataservice.FosEngine.test;

import java.util.Date;

import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;

//import com.navinfo.comm.util.SerializeUtils;

public class CopyOfTest1 {

	public static void main(String[] args) throws Exception {

//		String username1 = "fmgdb14";
//
//		String password1 = "fmgdb14";
//
//		int port1 = 1521;
//
//		String ip1 = "192.168.4.131";
//
//		String serviceName1 = "orcl";
//
//		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
//				serviceName1);
//		
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/git/FosEngine/FosEngine/src/config.properties");
		
		
		System.out.println(new Date());

		RdLinkSelector op = new RdLinkSelector(DBOraclePoolManager.getConnection(1));

		int c=0;
		for (int i = 211700; i <= 211709; i++) {
			try {
				op.loadById(i, false);
				c++;
				
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		System.out.println(c);
		
		System.out.println(new Date());
	}

}
