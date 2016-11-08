package com.navinfo.dataservice.engine.man;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.mqmsg.InfoChangeMsgHandler;

public class InfoTest extends InitApplication {
	
	@Test
	public void msgTest(){
		InfoChangeMsgHandler info = new InfoChangeMsgHandler();
		String message = "{\"geometry\":\"POINT (186.471866 59.836927)\",\"rowkey\":\"1112122121\""
				+ ",\"INFO_NAME\":\"TEST01\",\"i_level\":1,\"INFO_CONTENT\":\"TEST01\"}";
		info.handle(message);
	}

	@Override
	@Before
	public void init() {
		// TODO Auto-generated method stub
		initContext();
	}

}
