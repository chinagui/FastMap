package com.navinfo.dataservice.engine.photo;

import java.io.File;
import java.io.FileInputStream;
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
	@Test
	public void importCrowdPhoto() throws Exception{
		try{
			File file = new File("W:\\test\\1QAZ2WSX3EDC4RFV5TGB6YHN7UJM1QAZ.jpg");
			int angle = 0;
			String fileName = "1QAZ2WSX3EDC4RFV5TGB6YHN7UJM1QAZ.jpg";
			System.out.println(fileName.length());
			CollectorImport.importCrowdPhoto(new FileInputStream(file), angle, fileName, 0, 0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
