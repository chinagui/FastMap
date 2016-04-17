package com.navinfo.dataservice.engine.photo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.FileUtils;

public class CollectorImport {

	public static void importPhoto(Map<String,Photo> map,String dir) throws Exception{
		
		if (map.size()==0){
			return;
		}
		
		Map<String,byte[]> mapPhoto = FileUtils.readPhotos(dir);
		
		Map<String,byte[]> mapSltPhoto = FileUtils.genSmallImageMap(dir);
		
		Table photoTab = HBaseAddress.getHBaseConnection().getTable(
				TableName.valueOf(HBaseConstant.photoTab));
		
		List<Put> puts = new ArrayList<Put>();
		
		Set<Entry<String,Photo>> set = map.entrySet();
		
		Iterator<Entry<String,Photo>> it = set.iterator();
		
		int num = 0;
		
		while(it.hasNext()){
			Entry<String,Photo> entry = it.next();
			
			Put put = enclosedPut(entry,mapPhoto,mapSltPhoto);
			
			if(put == null){
				continue;
			}
			
			puts.add(put);
			
			num++;
			
			if (num >=1000){
				photoTab.put(puts);
				
				puts.clear();
				
				num = 0;
			}
		}
		
		photoTab.put(puts);
		
		photoTab.close();
	}
	
	private static Put enclosedPut(Entry<String,Photo> entry,Map<String,byte[]> mapPhoto,Map<String,byte[]> mapSltPhoto) throws Exception
	{
		Photo pht = entry.getValue();
		
		String name = entry.getKey();
		
		byte[] photo = mapPhoto.get(name);
		
		if(photo == null){
			return null;
		}
		
		byte[] sltPhoto = mapSltPhoto.get(name);
		
		Put put = new Put(pht.getRowkey().getBytes());
		
		put.addColumn("data".getBytes(), "attribute".getBytes(), JSONObject
				.fromObject(pht).toString().getBytes());
		
		put.addColumn("data".getBytes(), "origin".getBytes(), photo);
		
		put.addColumn("data".getBytes(), "thumbnail".getBytes(), sltPhoto);
		
		return put;
	}
}
