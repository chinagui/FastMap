package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload;

import java.io.FileInputStream;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UploadOperation {
	
	/**
	 * 读取txt，解析，入库
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public JSONObject importPoi(String fileName)throws Exception{
		JSONObject retObj = new JSONObject();
		Scanner scanner = new Scanner(new FileInputStream(fileName));
		int total = 0;
		while (scanner.hasNextLine()) {

			total += 1;

			try {
				String line = scanner.nextLine();
				retObj = changeData(line);
			} catch (Exception e) {
				throw e;
			}
		}
		return retObj;
	}
	
	/**
	 * 数据解析，入库
	 * @param line
	 * @return
	 */
	private JSONObject changeData(String line) throws Exception{
		JSONObject json = JSONObject.fromObject(line);
		
		return null;
	}
	
}
