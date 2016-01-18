package com.navinfo.dataservice.scripts;

import java.io.FileInputStream;
import java.util.Properties;

import com.navinfo.dataservice.FosEngine.tips.TipsBuilder;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.util.UuidUtils;


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
		
		String username2 = props.getProperty("fm_username");
		
		String password2 =props.getProperty("fm_username");
		
		int port2 = Integer.parseInt(props.getProperty("fm_port"));
		
		String ip2 = props.getProperty("fm_ip");
		
		String serviceName2 = props.getProperty("fm_service_name");
		
		OracleAddress oa2 = new OracleAddress(username2,password2,port2,ip2,serviceName2);
		
		TipsBuilder b = new TipsBuilder();
		
		HBaseAddress.initHBaseAddress(props.getProperty("hbase_address"));
		
		b.run(oa1, oa2, uuid,props.getProperty("solr_address"));
		
	}


}
