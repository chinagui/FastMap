package com.navinfo.dataservice.engine.man;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.mqmsg.InfoChangeMsgHandler;

public class InfoTest extends InitApplication {
	
	@Test
	public void msgTest(){
		InfoChangeMsgHandler info = new InfoChangeMsgHandler();
		String message = "{\"geometry\":\"POINT (196.471866 39.836927)\",\"rowkey\":\"1112122121\""
				+ ",\"INFO_NAME\":\"TEST00AAA\",\"i_level\":1,\"INFO_CONTENT\":\"TEST00AAA\"}";
		info.handle(message);
	}

	@Override
	@Before
	public void init() {
		// TODO Auto-generated method stub
		initContext();
	}

}
