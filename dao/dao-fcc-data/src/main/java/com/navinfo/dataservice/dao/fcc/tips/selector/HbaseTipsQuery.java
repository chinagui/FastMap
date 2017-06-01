package com.navinfo.dataservice.dao.fcc.tips.selector;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
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
       

}
