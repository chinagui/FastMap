package com.navinfo.dataservice.engine.fcc;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Test;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;

/**
 * @ClassName: TipsOperateTest.java
 * @author y
 * @date 2016-7-2下午1:56:04
 * @Description: TODO
 * 
 */
public class TipsOperateTest {

	@Test
	public void testEdit() {

		TipsOperator operate = new TipsOperator();

		try {
			operate.update("0215015ffa9224d4034f38995b6e8a173d9a55", 0, null,
					"m");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 批量修改tips状态
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-2-9 上午9:45:51
	 */
	@Test
	public void testBatchEdit() {

		TipsOperator operate = new TipsOperator();
		
		//JSONArray data, int handler, String mdFlag
		
		JSONArray data=new JSONArray();
		
		JSONObject b=new JSONObject();
		
		b.put("rowkey", "1115023838453");
		
		b.put("status", "2");
		
		data.add(b);
		
		JSONObject a=new JSONObject();
		
		a.put("rowkey", "1115024070073");
		
		a.put("status", "2");
		
		data.add(a);
		
		int handler=2922;
		
		String mdFlag="m";

		try {
			operate.batchUpdateStatus(data, handler, mdFlag);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testUpdateAll() {

		TipsOperator operate = new TipsOperator();

		try {
			String rowkey = "220215153123fa4d7d62479fa7d35ab9def60fa8";
			// operate.update("220215153123fa4d7d62479fa7d35ab9def60fa8", 123,
			// 0, "m");

			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			Get get = new Get(rowkey.getBytes());

			get.addColumn("data".getBytes(), "track".getBytes());

			Result result = htab.get(get);

			Put put = new Put(rowkey.getBytes());

			JSONObject track = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "track".getBytes())));

			JSONArray trackInfo = track.getJSONArray("t_trackInfo");

			track.put("t_mStatus", 0);

			track.put("t_dStatus", 0);

			String date = StringUtils.getCurrentTime();

			track.put("t_trackInfo", trackInfo);

			track.put("t_date", date);

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			htab.put(put);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//造数据
	public static void main(String[] args) {

		int  count=1200;
		Scanner scanner = null;
		PrintWriter pw=null;
		try {
			scanner = new Scanner(
					new FileInputStream(
							"E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tips.txt"));
			
			JSONObject json =null;
			if (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				json = JSONObject.fromObject(line);
				
			}
			
			 pw = new PrintWriter("E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tip_1000.txt");
			for (int i = 0; i < count; i++) {
				String rowkey=TipsUtils.getNewRowkey("2001");
				json.put("rowkey", rowkey);
				json.put("t_handler", "2922");
				json.put("t_operateDate", DateUtils.dateToString(new Date(),
						DateUtils.DATE_COMPACTED_FORMAT));
				pw.println(json.toString());
				
			}
			
			System.out.println("造数据完成");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			scanner.close();
			pw.close();
		}

	}

}
