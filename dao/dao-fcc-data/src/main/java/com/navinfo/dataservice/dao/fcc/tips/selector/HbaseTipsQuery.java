package com.navinfo.dataservice.dao.fcc.tips.selector;

import java.util.*;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.log4j.Logger;

/** 
 * @ClassName: HbaseTipsQuery.java
 * @author y
 * @date 2017-5-31 上午11:06:58
 * @Description: TODO
 *  
 */
public class HbaseTipsQuery {
	
	
	private static final Logger logger = Logger
			.getLogger(HbaseTipsQuery.class);
	
	   public static JSONObject getHbaseTipsByRowkey(Table htab, String rowkey,
   			String[] queryColNames) throws Exception{
       	
       	JSONObject resultJson=new JSONObject();
       	Result result = null;
   		try {

   			Get get = new Get(rowkey.getBytes());

   			// 没有给定字段，则全字段查
   			if (queryColNames != null && queryColNames.length != 0) {

   				for (String colName : queryColNames) {

   					get.addColumn("data".getBytes(), colName.getBytes());
   				}
   			}
   			
   			result = htab.get(get);

   			if (result.isEmpty()) {
   				throw new Exception("根据rowkey,没有找到需要对应的tips信息，rowkey："+rowkey);
   			}

   			if(result!=null){
   				
   				// 没有给定字段，则全字段查
       			if (queryColNames != null && queryColNames.length != 0) {

       				for (String colName : queryColNames) {

       					// 1.update track
           				JSONObject value = JSONObject.fromObject(new String(result
           						.getValue("data".getBytes(), colName.getBytes())));
           				
           				resultJson.put(colName,value);
       				}
       			}
   				
   			}

   		} catch (Exception e) {

   			logger.error("根据rowkey查询tips信息出错:" + rowkey + "原因："
   					+ e.getMessage());

   			throw new Exception("根据rowkey查询tips信息出错:" + rowkey + "原因："
   					+ e.getMessage(), e);
   		}

   		return resultJson;
   	}

	public static Map<String, JSONObject> getHbaseTipsByRowkeys(Set<String> rowkeys,
																String[] queryColNames) throws Exception{
		long start = System.currentTimeMillis();
		Map<String, JSONObject> map = new HashMap<>();
		Connection hbaseConn = null;
		Table htab = null;
		try {
			if(rowkeys.size()==0){
				return map;
			}

			hbaseConn = HBaseConnector
					.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			List<Get> gets = new ArrayList<>();
			for(String rowkey:rowkeys) {
				Get get = new Get(rowkey.getBytes());
				// 没有给定字段，则全字段查
				if (queryColNames != null && queryColNames.length != 0) {
					for (String colName : queryColNames) {
						get.addColumn("data".getBytes(), colName.getBytes());
					}
				}
				gets.add(get);
			}

			Result[] results = htab.get(gets);

			if(results!=null){
				for(Result result : results) {
					if(result.isEmpty()){
						continue;
					}
					JSONObject resultJson = new JSONObject();
					// 没有给定字段，则全字段查
					if (queryColNames != null && queryColNames.length != 0) {
						for (String colName : queryColNames) {
							// 1.update track
							byte[] bytes = result.getValue("data".getBytes(), colName.getBytes());
							if(bytes != null){
								JSONObject value = JSONObject.fromObject(new String(bytes));
								resultJson.put(colName, value);
							}else{
								resultJson.put(colName, "{}");
							}
						}
						map.put(new String(result.getRow()), resultJson);
					}
				}
			}

		} catch (Exception e) {

			logger.error("根据rowkeys查询tips信息出错,原因："
					+ e.getMessage());

			throw new Exception("根据rowkeys查询tips信息出错,原因："
					+ e.getMessage(), e);
		}finally {
			if(htab!=null) {
				htab.close();
			}
		}

		long end = System.currentTimeMillis();
		System.out.println("hbase query end. time:"+ (end-start) + ",count:"+rowkeys.size());

		return map;
	}


}
