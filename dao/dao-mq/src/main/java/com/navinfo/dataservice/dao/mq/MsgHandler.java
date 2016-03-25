package com.navinfo.dataservice.dao.mq;

/** 
* @ClassName: MsgHandler 
* @author Xiao Xiaowen 
* @date 2016年3月24日 上午10:43:19 
* @Description: TODO
*/
public interface MsgHandler {
	/**
	 * 不要抛出异常，内部处理掉
	 * @param message
	 */
	void handle(String message);
}
