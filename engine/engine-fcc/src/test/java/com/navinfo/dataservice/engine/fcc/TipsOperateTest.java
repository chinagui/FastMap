package com.navinfo.dataservice.engine.fcc;

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
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;

/** 
 * @ClassName: TipsOperateTest.java
 * @author y
 * @date 2016-7-2下午1:56:04
 * @Description: TODO
 *  
 */
public class TipsOperateTest {
	
	@Test
	public void testEdit(){
		
		TipsOperator operate=new TipsOperator();
		
		try {
			operate.update("0212055b268d5faff94b59b94ad7aec3348d4f", 123, null, "m");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	//@Test
	public void testUpdateAll(){
		
		TipsOperator operate=new TipsOperator();
		
		try {
			String rowkey="220215153123fa4d7d62479fa7d35ab9def60fa8";
		//	operate.update("220215153123fa4d7d62479fa7d35ab9def60fa8", 123, 0, "m");
			
			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			Get get = new Get(rowkey.getBytes());

			get.addColumn("data".getBytes(), "track".getBytes());


			Result result = htab.get(get);


			Put put = new Put(rowkey.getBytes());

			JSONObject track = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "track".getBytes())));


			JSONArray trackInfo = track.getJSONArray("t_trackInfo");
			

			
			track.put("t_mStatus", 0);
			
			track.put("t_dStatus", 0);
			
			String date = StringUtils.getCurrentTime();

			track.put("t_trackInfo", trackInfo);

			track.put("t_date", date);

			put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
					.getBytes());

			htab.put(put);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
