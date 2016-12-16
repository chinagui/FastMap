package com.navinfo.dataservice.engine.audio;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.ByteUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

/**
 * @ClassName: AudioController.java
 * @author y
 * @date 2016-12-2 下午7:26:23
 * @Description: 音频处理类
 *
 */
public class AudioController {
	
	public static String tableName=HBaseConstant.audioTab;

	private ArrayList<KeyValue> getByRowkey(String tableName, String rowkey,
			String family, String... qualifiers) throws Exception {

		final GetRequest get = new GetRequest(tableName, rowkey);

		if (family != null) {
			get.family(family);
		}

		if (qualifiers.length > 0) {
			get.qualifiers(ByteUtils.toBytes(qualifiers));
		}

		ArrayList<KeyValue> list = HBaseConnector.getInstance().getClient()
				.get(get).joinUninterruptibly();

		return list;
	}

	/**
	 * @Description:根据rowkey返回音频
	 * @param rowkey
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-2 下午8:04:52
	 */
	public byte[] getAudioByRowkey(String rowkey) throws Exception {
		
		List<KeyValue> list = getByRowkey(tableName, rowkey, "data", "origin.o_audio");

		for (KeyValue kv : list) {
			return kv.value();
		}

		return new byte[0];
	}

	/**
	 * @Description:获取音频的属性信息
	 * @param rowkey
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-2 下午7:53:16
	 */
	public byte[] getAudioDetailByRowkey(String rowkey) throws Exception {

		List<KeyValue> list = getByRowkey(tableName, rowkey, "data", "attribute");

		for (KeyValue kv : list) {
			return kv.value();
		}

		return new byte[0];
	}




	
	public void putAudio(String rowkey, InputStream in) throws Exception{
		
		Audio audio = new Audio();
		
		audio.setRowkey(rowkey);
		
		int count = in.available();
		
		byte[] bytes = new byte[(int) count];

		in.read(bytes);
		
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
		
		Put put = new Put(rowkey.getBytes());
		
		put.addColumn("data".getBytes(), "attribute".getBytes(), JSONObject
				.fromObject(audio).toString().getBytes());
		
		put.addColumn("data".getBytes(), "origin".getBytes(), bytes);
		
		htab.put(put);
		
	}

	/**
	 * @Description:新增音频
	 * @param in
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-2 下午8:03:11
	 */
	public String putAudio(InputStream in) throws Exception{
		String rowkey = UuidUtils.genUuid();
		putAudio(rowkey, in);
		return rowkey;
	}
}
