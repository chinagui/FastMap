package com.navinfo.dataservice.engine.editplus;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;
import net.sf.json.JSONObject;

/** 
 * @ClassName: PoiUploadTest
 * @author zl
 * @date 2017年1月3日
 * @Description: PoiUploadTest.java
 */
public class PoiUploadTest {

	/**
	 * 
	 */
	public PoiUploadTest() {
		// TODO Auto-generated constructor stub
	}

	@Before
	public void init(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-editplus.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	
	@Test
	//上传
	public void uploadTest(){
		try{
//			Connection conn = null;
//			conn = DBConnector.getInstance().getConnectionById(17);;
			
			UploadOperationByGather uploadOperation = new UploadOperationByGather((long) 0);
			JSONObject retObj = uploadOperation.importPoi("F://poi003.txt");//F://testpoi.txt
			
			System.out.println("retObj: "+ retObj);
			
			System.out.println("Over.");

		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
}
