package com.navinfo.dataservice.FosEngine.test;

import java.util.Map;

import com.navinfo.dataservice.FosEngine.photos.CollectorImport;
import com.navinfo.dataservice.FosEngine.photos.Photo;
import com.navinfo.dataservice.FosEngine.tips.TipsUpload;
import com.navinfo.dataservice.commons.db.HBaseAddress;

public class Test5 {
	
	public static class Inner{
		private double a;

		public double getA() {
			return a;
		}

		public void setA(double a) {
			this.a = a;
		}
		
		
	}
	
	
	public static void main(String[] args) throws Exception {
		HBaseAddress.initHBaseAddress("192.168.3.156");
		
		String filePath = "C:/1";
		
		TipsUpload upload = new TipsUpload();
		
		Map<String, Photo> map = upload.run(filePath+"/"+"tips.txt");
		
		CollectorImport.importPhoto(map, filePath+"/photo");
		
		System.out.println("done");
		
	}

}
