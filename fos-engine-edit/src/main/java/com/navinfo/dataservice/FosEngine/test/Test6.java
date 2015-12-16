package com.navinfo.dataservice.FosEngine.test;

import java.io.IOException;
import java.net.MalformedURLException;

import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class Test6 {

	public static void main(String[] args) throws MalformedURLException, IOException {
		String t = "UPDATE1";
		
		OperType tt = Enum.valueOf(OperType.class, t);
		
		System.out.println(tt);
	}

}
