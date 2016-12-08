package com.navinfo.dataservice.engine.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

/**
 * @ClassName: AudioImport.java
 * @author y
 * @date 2016-12-3 下午3:36:50
 * @Description: TODO
 * 
 */
public class AudioImport {

	private static String AUDIO_TABLE_NAME = HBaseConstant.audioTab;

	public static void importAudio(Map<String, Audio> map, String dir)
			throws Exception {

		if (map.size() == 0) {
			return;
		}

		File file = new File(dir);

		if (!file.exists()) {
			return;
		}

		// readPhotos 读取同照片 这里不用修改
		Map<String, byte[]> mapAdio = FileUtils.readPhotos(dir);

		Table audioTab = HBaseConnector.getInstance().getConnection()
				.getTable(TableName.valueOf(AUDIO_TABLE_NAME));

		List<Put> puts = new ArrayList<Put>();

		Set<Entry<String, Audio>> set = map.entrySet();

		Iterator<Entry<String, Audio>> it = set.iterator();

		int num = 0;

		while (it.hasNext()) {
			Entry<String, Audio> entry = it.next();

			Put put = enclosedPut(entry, mapAdio);

			if (put == null) {
				continue;
			}

			puts.add(put);

			num++;

			if (num >= 1000) {
				audioTab.put(puts);

				puts.clear();

				num = 0;
			}
		}

		audioTab.put(puts);

		audioTab.close();
	}

	private static Put enclosedPut(Entry<String, Audio> entry,
			Map<String, byte[]> mapAudio) throws Exception {

		Map<String, byte[]> mapNew = mapChangeKey(mapAudio); // map中key放的是文件名，要取掉后缀

		Audio audio = entry.getValue();

		String id = entry.getKey();

		byte[] audioByte = mapNew.get(id);

		if (audio == null) {
			return null;
		}

		JSONObject o_audio = new JSONObject();

		o_audio.put("o_audio", audioByte);

		Put put = new Put(audio.getRowkey().getBytes());
		
		JSONObject  attribute=JSONObject.fromObject( JSONObject.fromObject(audio).discard("rowkey")); //取掉对象中的rowkey字段
		
		put.addColumn("data".getBytes(), "attribute".getBytes(), attribute.toString().getBytes());

		put.addColumn("data".getBytes(), "origin".getBytes(), o_audio
				.toString().getBytes());

		return put;
	}

	/**
	 * @Description:map换key,取掉文件后缀
	 * @param mapAudio
	 * @return
	 * @author: y
	 * @time:2016-12-3 下午4:08:54
	 */
	private static Map<String, byte[]> mapChangeKey(Map<String, byte[]> mapAudio) {

		Map<String, byte[]> newMap = new HashMap<String, byte[]>();

		Set<String> keys = mapAudio.keySet();

		for (String fileName : keys) {

			String prefix = fileName.substring(fileName.lastIndexOf("."));// 如果想获得不带点的后缀，变为fileName.lastIndexOf(".")+1
			
			int num = prefix.length();// 得到后缀名长度

			String newkey = fileName.substring(0, fileName.length() - num);// 得到文件名。去掉了后缀

			newMap.put(newkey, mapAudio.get(fileName));
		}

		return newMap;
	}

	public static void main(String[] args) {
		String fileName = "test.test2.map";

		System.out.println(File.separator);

		String prefix = fileName.substring(fileName.lastIndexOf("."));// 如果想获得不带点的后缀，变为fileName.lastIndexOf(".")+1
		int num = prefix.length();// 得到后缀名长度

		String fileOtherName = fileName.substring(0, fileName.length() - num);// 得到文件名。去掉了后缀

		System.out.println(fileOtherName);
	}

}
