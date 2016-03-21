package com.navinfo.dataservice.engine.fcc.tips.forbicross;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;

public class ForbiCrossTipsBuilder {

	private static String sql = "";
	
	private static String type = "1510";
	
	/**
	 * 导入入口程序块
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab) throws Exception{
		
		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;
		
		double[] lonlat = new double[2];
		
		String uniqId = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		while (resultSet.next()) {
			num++;
			
			String rowkey = TipsImportUtils.generateRowkey( uniqId, type);
			
			String source = TipsImportUtils.generateSource(type);
			
			String track = TipsImportUtils.generateTrack(date);
			
			String geometry = generateGeometry();
			
			String deep = generateDeep();
			
			Put put = new Put(rowkey.getBytes());
			
			put.addColumn("data".getBytes(), "source".getBytes(),source.getBytes());
			
			put.addColumn("data".getBytes(), "track".getBytes(),track.getBytes());
			
			put.addColumn("data".getBytes(), "geometry".getBytes(),geometry.getBytes());
			
			put.addColumn("data".getBytes(), "deep".getBytes(),deep.getBytes());
			
			puts.add(put);
			
			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();

			}
		}

		htab.put(puts);
		
	}
	
	private static String generateGeometry(){
		
		
		return null;
	}
	
	private static String generateDeep(){
		
		return null;
	}
	
}
