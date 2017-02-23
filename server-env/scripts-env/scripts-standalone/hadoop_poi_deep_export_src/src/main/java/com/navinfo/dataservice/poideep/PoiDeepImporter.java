package com.navinfo.dataservice.poideep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

public class PoiDeepImporter {
	
	private static Set<Integer> kcs = null;

	public static void main(String[] args) throws IOException,Exception {
		
		Properties props = new Properties();
		System.out.println("load conf file:"+args[0]);
		props.load(new FileInputStream(args[0]));
		
		Map<String,Collection<String>> adminMeshes = parseMeshes(props);
		
		if(adminMeshes==null){
			System.out.println("admincodes is null,nothing to do.");
			return;
		}
		
		initKcs();

		Configuration conf = HBaseConfiguration.create();

		HTable htable = new HTable(conf, "poi");
		
		for(String admimcode:adminMeshes.keySet()){
			System.out.println("starting exp "+admimcode);
			String fileName = admimcode+"_poi_deep.txt";
			PrintWriter pw = new PrintWriter(fileName);

			int total = 0;

			int count = 0;
			
			for(String mesh:adminMeshes.get(admimcode)){
				
				System.out.println("..starting scan mesh:"+mesh);
				Scan scan = new Scan();

				scan.addFamily("data".getBytes());

				scan.setCaching(5000);
				
				scan.setStartRow(mesh.getBytes());
				
				scan.setStopRow((mesh+"a").getBytes());

				ResultScanner rs = htable.getScanner(scan);

				Result[] results = null;

				while ((results = rs.next(5000)).length > 0) {

					for (Result result : results) {

						total++;

						if (total % 10000 == 0) {
							System.out.println("total:"+total+",output:"+count);
						}

						String s1 = new String(result.getValue("data".getBytes(),
								"histories".getBytes()));

						JSONObject histories = JSONObject.fromObject(s1);

						if (histories.getInt("lifecycle") == 1) {
							continue;
						}

						String s2 = new String(result.getValue("data".getBytes(),
								"attributes".getBytes()));

						JSONObject attritutes = JSONObject.fromObject(s2);
						
						Object bt = attritutes.get("businessTime");
						if(bt!=null&&!(bt instanceof JSONNull)&&((JSONArray)bt).size()==0){
							pw.println(s2);
							count++;
							continue;
						}

						Object ft = attritutes.get("foodtypes");
						if(ft!=null&&!(ft instanceof JSONNull)){
							pw.println(s2);
							count++;
							continue;
						}

						Object hot = attritutes.get("hotel");
						if(hot!=null&&!(hot instanceof JSONNull)){
							pw.println(s2);
							count++;
							continue;
						}
						
						Object rental = attritutes.get("rental");
						if(rental!=null&&!(rental instanceof JSONNull)){
							pw.println(s2);
							count++;
							continue;
						}
						
						
						int kindCode = attritutes.getInt("kindCode");

						if (kcs.contains(kindCode)) {
							pw.println(s2);
							count++;
							continue;
						}
					}
				}
			}
			pw.flush();
			pw.close();

			System.out.println("Total count of "+admimcode+": " + total);
			System.out.println("Output count of "+admimcode+": " + count);
		}

		htable.close();
	}
	
	private static Map<String,Collection<String>> parseMeshes(Properties props)throws Exception{
		
		String admincodes = props.getProperty("admincodes");
		if(StringUtils.isEmpty(admincodes)){
			return null;
		}
		
		Map<String,Collection<String>> adminMeshes = new HashMap<String,Collection<String>>();
		
		String username = props.getProperty("fmmeta.username");

		String password = props.getProperty("fmmeta.password");

		String url = props.getProperty("fmmeta.url");

		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			String sql = "SELECT ADMINCODE,MESH FROM CP_MESHLIST WHERE ADMINCODE IN ("+admincodes+")";
			conn = DriverManager.getConnection(url, username, password);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()){
				String ac = rs.getString("ADMINCODE");
				String mesh = rs.getString("MESH");
				if(adminMeshes.containsKey(ac)){
					adminMeshes.get(ac).add(StringUtils.leftPad(mesh,8, '0'));
				}else{
					Collection<String> meshes = new HashSet<String>();
					meshes.add(StringUtils.leftPad(mesh, 8, '0'));
					adminMeshes.put(ac, meshes);
				}
			}
		}catch(Exception e){
			if(rs!=null)rs.close();
			if(pstmt!=null)pstmt.close();
			if(conn!=null){
				try{
					conn.close();
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			e.printStackTrace();
			throw e;
		}
		return adminMeshes;
	}
	
	private static void initKcs(){
		kcs = new HashSet<Integer>();
		kcs.add(230215);
		kcs.add(230216);
		kcs.add(230217);
		kcs.add(230210);
		kcs.add(230213);
		kcs.add(230214);
		//tongyong
		kcs.add(180308);
		kcs.add(180309);
		kcs.add(180304);
		kcs.add(180400);
		kcs.add(160206);
		kcs.add(160205);
		kcs.add(170100);
		kcs.add(170101);
		kcs.add(170102);
		kcs.add(150101);
		kcs.add(110103);
		kcs.add(110102);
		kcs.add(130501);
		kcs.add(130105);
		kcs.add(110200);
		kcs.add(120101);
		kcs.add(120101);
		kcs.add(120101);
		kcs.add(120102);
		kcs.add(230215);
		kcs.add(230216);
		kcs.add(230217);
		kcs.add(200201);
	}
}
