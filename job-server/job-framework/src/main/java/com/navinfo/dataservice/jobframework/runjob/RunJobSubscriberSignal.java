package com.navinfo.dataservice.jobframework.runjob;

import com.navinfo.dataservice.dao.mq.SubscriberSignal;

/** 
* @ClassName: RunJobSubscribersignal 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午2:15:56 
* @Description: TODO
*/
public class RunJobSubscriberSignal implements SubscriberSignal {

	@Override
	public boolean needWait() {
		return JobThreadPoolExecutor.getInstance().isTotalFull();
	}
	@Override
	public boolean isWaiting() {
		//...
		return false;
	}

}
