package com.navinfo.dataservice.engine.edit.wangdongbin.poi;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload.UploadOperation;

public class androidtest extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void test() {
		UploadOperation operation = new UploadOperation();
		try {
			operation.importPoi("F://poi.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
