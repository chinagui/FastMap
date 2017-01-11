package com.navinfo.dataservice.engine.photo;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

import com.navinfo.dataservice.engine.photo.PhotoGetter;

public class photoTest {
	@Test
	public void getPhotoTest() throws Exception{
		JSONArray rowkeys = new JSONArray();
		PhotoGetter getter = new PhotoGetter();
		//List<Map<String, Object>> data = getter.getPhotosByRowkey(rowkeys);
		JSONObject tt = getter.getPhotoDetailByRowkey("87bda841aec4486fadfe9882cc25a3e0");
		System.out.println("over");
	} 
}
