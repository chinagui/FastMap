package com.navinfo.dataservice.jobframework;

import com.navinfo.dataservice.dao.mq.MsgHandler;

/** 
* @ClassName: RunJobHandler 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午6:16:57 
* @Description: TODO
*/
public class RunJobHandler implements MsgHandler {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		//解析message生成jobInfo
		//...
		JobInfo jobInfo = null;
		JobThreadPoolExecutor.getInstance().execute(jobInfo);
	}

}
