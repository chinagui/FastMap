package com.navinfo.dataservice.engine.fcc.track;

import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 轨迹入库
 */
public abstract class TrackUpload {

	private static final int FAIL = 0;
    private static final int SUCESS = 1;
    private JSONArray resultJsonArr = new JSONArray();
    private int failed = 0;
    private int total = 0;

    /**
     * 获取json文件rowkey
     * @param json
     * @return
     */
    public abstract String getSourceRowkey(JSONObject json);

    /**
     * 按照赋值原则，数据保存到hbase
     * @param json
     * @param rowkey
     * @return
     * @throws Exception
     */
    public abstract Put generatePut(JSONObject json, String rowkey) throws Exception;

    /**
     * 轨迹类型  "traceType":0 普通轨迹线； 1 adas轨迹点
     * @return
     */
    public abstract int getTrackType();

	/**
	 * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void run(String fileName, String tableName) throws Exception {
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
        this.createTabIfNotExists(hbaseConn, tableName);
		Table htab = hbaseConn.getTable(TableName
				.valueOf(tableName));
		loadFileContent(fileName, htab);
		htab.close();
	}

	/**
	 * 读取track文件，组装Get列表
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
			String rowkey = null;
			try{
				String line = scanner.nextLine();
				JSONObject json = JSONObject.fromObject(line);
                //获取rowkey
                rowkey = this.getSourceRowkey(json);
				//通过id判断数据在hbase库中是否已经存在，存在则使用库中的rowkey
				Put put = this.generatePut(json, rowkey);
				puts.add(put);
				count++;
				if (count > 5000) {
					htab.put(puts);
					puts.clear();
					count = 0;
				}
				resultJsonArr.add(newResultObject(rowkey, SUCESS, 1));
			}catch (Exception e) {
				failed ++;
				resultJsonArr.add(newResultObject(rowkey, FAIL, 0));
			}
		}
		htab.put(puts);
	}
	

	/**
	 * @Description:入库信息（用于接口返回）
	 * @param id
	 * @param result
	 * @param errorReason
	 * @return
	 * @author: y
	 * @time:2016-6-30 下午4:50:41
	 */
	private JSONObject newResultObject(String id, int result,int errorReason) {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("status", result);
        json.put("trackType", this.getTrackType());
        json.put("remark", errorReason);
		return json;
	}

    private static void createTabIfNotExists(Connection connection,
                                             String tabName) throws IOException {
        Admin admin = connection.getAdmin();
        TableName tableName = TableName.valueOf(tabName);
        if (!admin.tableExists(tableName)) {
            HTableDescriptor htd = new HTableDescriptor(tableName);
            HColumnDescriptor hcd = new HColumnDescriptor("attribute");
            htd.addFamily(hcd);
            admin.createTable(htd);
        }
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

	
	public static void main(String[] args) throws Exception {
		
		long t1=System.currentTimeMillis();

        TrackLinesUpload trackUploader = new TrackLinesUpload();
		trackUploader.run("F:\\FCC\\track\\Datum_Track.json","tracklines_sprint5");
        System.out.println(trackUploader.getResultJsonArr().get(0).toString());

//		TrackUpload a = new TrackUpload();
//
//		a.run("D:\\line.txt");
		
		long t2=System.currentTimeMillis();
		
		System.out.println("耗时："+(t2-t1));
		
//		System.out.println(a.getResultJsonArr());
	}
}
