package com.navinfo.dataservice.scripts;

import java.io.FileInputStream;
import java.util.Properties;

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.fcc.tips.TipsBuilder;


public class FmGdb2Tips {
	

	public static void main(String[] args) throws Exception {
		
		Properties props = new Properties();
		
		props.load(new FileInputStream(args[0]));

		String uuid = UuidUtils.genUuid();
		
		String username1 = props.getProperty("gdb_username");
		
		String password1 =props.getProperty("gdb_password");
		
		int port1 =Integer.parseInt(props.getProperty("gdb_port"));
		
		String ip1 = props.getProperty("gdb_ip");
		
		String serviceName1 = props.getProperty("gdb_service_name");
		
		OracleAddress oa1 = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		TipsBuilder b = new TipsBuilder();
		
		HBaseAddress.initHBaseAddress(props.getProperty("hbase_address"));
		
		b.run(oa1, uuid);
		
	}


}