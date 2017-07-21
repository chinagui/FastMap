package com.navinfo.dataservice.engine.man;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.mqmsg.InfoChangeMsgHandler;
import com.navinfo.dataservice.engine.man.mqmsg.SendEmailMsgHandler;

public class InfoTest extends InitApplication {
	
	@Test
	public void msgTest() throws Exception{
		InfoChangeMsgHandler info = new InfoChangeMsgHandler();
		String message = "{\"adminCode\":110210,\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"e58b02ca56de4f9d92fdd22bdc45d995\",\"inforLevel\":1,\"feedbackType\":1,\"featureKind\":2,\"sourceCode\":\"1\",\"roadLength\":19,\"adminName\":\"云南省|西双版纳傣族自治州\",\"publishDate\":\"2017-05-06 20:20:55\",\"expectDate\":\"2017-05-06 20:20:55\",\"newsDate\":\"2017-05-06 20:20:55\",\"infoCode\":\"20160306QB00000341\",\"topicName\":\"\",\"inforName\":\"1月\",\"infoTypeName\":\"道路|普通道路\"}";			
		info.save(message);
	}
	
	@Test
	public void mailTest() throws Exception{
		SendEmailMsgHandler info = new SendEmailMsgHandler();
		/**
		 * String toMail = jo.getString("toMail");
			String mailTitle = jo.getString("mailTitle");
			String mailContent = jo.getString("mailContent");
		 */
		String message = "{\"toMail\":\"zhangxiaoyi@navinfo.com\",\"mailTitle\":\"test20170619\",\"mailContent\":\"道路|普通道路\"}";			
		info.handle(message);
	}

	@Override
	@Before
	public void init() {
		// TODO Auto-generated method stub
		initContext();
	}

}
