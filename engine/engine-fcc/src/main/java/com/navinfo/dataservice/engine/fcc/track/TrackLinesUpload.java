package com.navinfo.dataservice.engine.fcc.track;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

/**
 * @ClassName: TrackLinesUpload.java
 * @author y
 * @date 2016-6-29下午2:03:48
 * @Description: 轨迹上传，入hbase库
 * 
 */
public class TrackLinesUpload {
	
	
	static final int   FAIL = 0;
	
	static final int   SUCESS = 1;
	
	JSONArray resultJsonArr=new JSONArray();
	
	int  failed=0;
	
	int  total=0;
    
	/**
	 * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void run(String fileName) throws Exception {


		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName
				.valueOf(HBaseConstant.trackLineTab));

		loadFileContent(fileName, htab);

		htab.close();

	}

	/**
	 * 读取trackLine文件，组装Get列表
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private void loadFileContent(String fileName, Table htab) throws Exception {


		Scanner scanner = new Scanner(new FileInputStream(fileName));

		List<Put> puts = new ArrayList<Put>();

		int count = 0;

		while (scanner.hasNextLine()) {
			
			total++;
			
			String id=null;
			
			try{
			
				Put put = null;
	
				//JSONObject trackLine = new JSONObject();
	
				String line = scanner.nextLine();
	
				JSONObject json = JSONObject.fromObject(line);
	
				String segmentId = json.getString("segmentId");
				
				id=json.getString("id");
	
			/*	trackLine.put("a_uuid", json.getString("id"));
	
				trackLine.put("a_startTime", json.getString("startTime"));
	
				trackLine.put("a_endTime", json.getString("endTime"));
	
				trackLine.put("a_user", json.getInt("userId"));
	
				trackLine.put("a_geometry", json.getJSONObject("geometry"));
	
				trackLine.put("a_segmentId", segmentId);*/
	
				//通过id判断数据在hbase库中是否已经存在，存在则使用库中的rowkey
				String rowkey =extisRowKey(json.getString("id"),htab);
	
				if(rowkey==null){
					rowkey = segmentId;
				}
	
				put = new Put(rowkey.getBytes());
	
	
				put.addColumn("attribute".getBytes(), "a_uuid".getBytes(),
						json.getString("id").getBytes());
				
				put.addColumn("attribute".getBytes(), "a_startTime".getBytes(),
						json.getString("startTime").getBytes());
				
				put.addColumn("attribute".getBytes(), "a_endTime".getBytes(),
						Bytes.toBytes(json.getInt("endTime")));
				
				put.addColumn("attribute".getBytes(), "a_user".getBytes(),
						json.getString("userId").getBytes());
				
				put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
						json.getString("geometry").getBytes());
				
				put.addColumn("attribute".getBytes(), "a_segmentId".getBytes(),
						segmentId.getBytes());
		
				puts.add(put);
	
				count++;
	
				if (count > 5000) {
					htab.put(puts);
	
					puts.clear();
	
					count = 0;
				}
				resultJsonArr.add(newResultObject(id,SUCESS,""));
			
			}catch (Exception e) {
				failed++;
				resultJsonArr.add(newResultObject(id,FAIL,e.getMessage()));
			}
		}

		htab.put(puts);


	}
	
	
//	id	Text	轨迹线号码
//	status	Text	记录入库或安装状态：成功，失败，不入库
//	remark	Integer	记录失败的原因，记录不入库的原因：
//	…		
	
	/**
	 * @Description:入库信息（用于接口返回）
	 * @param id
	 * @param result
	 * @param errorReason
	 * @return
	 * @author: y
	 * @time:2016-6-30 下午4:50:41
	 */

	private JSONObject newResultObject(String id, int result,String errorReason) {

		JSONObject json = new JSONObject();

		json.put("id", id);

		json.put("status", result);
		
		json.put("remark", errorReason);

		return json;
	}
	
	

	/**
	 * @return the errCount
	 */
	public int getFailed() {
		return failed;
	}
	
	

	/**
	 * @return the total
	 */
	public int getTotal() {
		return total;
	}
	
	

	/**
	 * @return the resultJsonArr
	 */
	public JSONArray getResultJsonArr() {
		return resultJsonArr;
	}

	/**
	 * @param resultJsonArr the resultJsonArr to set
	 */
	public void setResultJsonArr(JSONArray resultJsonArr) {
		this.resultJsonArr = resultJsonArr;
	}

	/**
	 * @Description:TODO
	 * @param string
	 * @return
	 * @author: y
	 * @param htab
	 * @throws Exception
	 * @time:2016-6-30 下午4:00:37
	 */
	private String extisRowKey(String uuid, Table htab) throws Exception {

		try {
			Scan scan = new Scan();
			scan.addColumn(Bytes.toBytes("attribute"), Bytes.toBytes("a_uuid"));
			SingleColumnValueFilter filter = new SingleColumnValueFilter(
					Bytes.toBytes("attribute"), Bytes.toBytes("a_uuid"),
					CompareOp.EQUAL, Bytes.toBytes(uuid));
			filter.setFilterIfMissing(true);
			scan.setFilter(filter);
			ResultScanner rs = htab.getScanner(scan);
			for (Result r : rs) {
				if (r != null && r.getRow() != null) {
						//System.out.println("获得到rowkey:" + new String(r.getRow()));
						/*for (KeyValue keyValue : r.raw()) {
							System.out.println("列：" + new String(keyValue.getFamily())
									+ "====值:" + new String(keyValue.getValue()));
						}*/
					return new String(r.getRow());
				}

			}
		} catch (Exception e) {
			throw e;
		} finally {

		}

		return null;
	}

	
	public static void main(String[] args) throws Exception {
		
		long t1=System.currentTimeMillis();

		TrackLinesUpload a = new TrackLinesUpload();

		a.run("D:\\line.txt");
		
		long t2=System.currentTimeMillis();
		
		System.out.println("耗时："+(t2-t1));
		
		System.out.println(a.getResultJsonArr());
	}
}
