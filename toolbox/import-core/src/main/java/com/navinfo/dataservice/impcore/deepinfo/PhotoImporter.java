package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PhotoImporter {
	public static int run(Connection conn, Statement stmt, JSONObject poi) throws Exception{
		JSONArray array = poi.getJSONArray("attachments");
		
		for(int i = 0; i<array.size();i++){
			JSONObject obj = array.getJSONObject(i);
			
			int tag = obj.getInt("tag");
			int type = obj.getInt("type");
			
			if(tag == 7||type ==1){
				return 0;
			}
			
			IxPoiPhoto ixPhoto = new IxPoiPhoto();
			
			
					}
		
		return 1;
	}
}
