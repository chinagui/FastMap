package com.navinfo.dataservice.engine.release;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class Release {
	private static final Logger logger = Logger.getLogger(Release.class);

	/**
	 * road提交
	 * @param parameter
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public void roadRelease(int subtaskId) throws Exception {
		//任务关闭
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		manApi.close(subtaskId);
	}

}
