package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

import net.sf.json.JSONObject;

public class ImportTipsIntoHadoop {

	public static void main(String[] args) throws Exception {

		int total = 0;

		try {

			String path = args[0];

			JobScriptsInterface.initContext();

			total = importTipsInfo(path);

		} catch (Exception e) {
			System.out.println("Oops, something wrong...");

			e.printStackTrace();
		} finally {
			System.out.println("import total count :" + total);

			System.out.println("Success, import is over...");
		}
	}

	/**
	 * 遍历txt生成put对象，然后写入
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private static int importTipsInfo(String path) throws Exception {

		String line;

		File file = new File(path);

		Connection hbaseConn = null;

		Table htab = null;

		int total = 0;

		InputStreamReader read = new InputStreamReader(new FileInputStream(file));

		BufferedReader reader = new BufferedReader(read);

		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			int cache = 0;

			List<Put> puts = new ArrayList<>();

			while ((line = reader.readLine()) != null) {

				Put put = getPutInfo(line);

				if (put == null) {
					continue;
				}

				puts.add(put);

				cache++;

				total++;

				if (cache > 10000) {

					htab.put(puts);

					cache = 0;

					puts.clear();
				}
			}
			if (cache > 0) {

				htab.put(puts);
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			reader.close();

			htab.close();

			hbaseConn.close();
		}

		return total;
	}

	/**
	 * 每一行记录生成一个put对象
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private static Put getPutInfo(String line) throws Exception {
		Put put = null;

		try {
			JSONObject jsonInfo = JSONObject.fromObject(line);

			String rowkey = jsonInfo.getString("rowkey");

			put = new Put(rowkey.getBytes());

			Set<String> keys = (Set<String>) jsonInfo.keySet();

			for (String key : keys) {

				if (key.equals("rowkey")) {
					continue;
				}

				put.addColumn("data".getBytes(), key.getBytes(), jsonInfo.getString(key).getBytes());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return put;
	}
}
