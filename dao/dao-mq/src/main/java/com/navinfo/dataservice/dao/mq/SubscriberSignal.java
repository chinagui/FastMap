package com.navinfo.dataservice.dao.mq;

/** 
* @ClassName: SubscriberSignal 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午1:31:56 
* @Description: TODO
*/
public interface SubscriberSignal {
	boolean needWait();
	boolean isWaiting();
}
