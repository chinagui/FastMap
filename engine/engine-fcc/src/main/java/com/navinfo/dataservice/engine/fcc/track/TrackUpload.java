package com.navinfo.dataservice.engine.fcc.track;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import net.sf.json.JSON;
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
    private JSONObject result = new JSONObject();
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
                JSONObject json = JSONObject.parseObject(line);
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
			}catch (Exception e) {
				failed ++;
				throw new Exception(e.getMessage());
			}
		}
		htab.put(puts);
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


	public JSONObject getResult() {
		return result;
	}

	public void setResult(JSONObject result) {
		this.result = result;
	}

	public static void main(String[] args) throws Exception {
//		AdasTrackPointUpload trackUploader = new AdasTrackPointUpload();
//		trackUploader.run("F:\\FCC\\adas_track_collect.json","trackpoints_trunk");
//        System.out.println(trackUploader.getFailed());
        net.sf.json.JSONObject nameTipsJson = new net.sf.json.JSONObject();
        nameTipsJson.put("g_location", "{\"coordinates\":[116.79561,39.93595],\"type\":\"Point\"}");
        net.sf.json.JSONObject gLocation = net.sf.json.JSONObject.fromObject(nameTipsJson.get("g_location"));
        net.sf.json.JSONArray jsonArray = gLocation.getJSONArray("coordinates");
        System.out.println(jsonArray.get(0));
	}
}
