package com.navinfo.dataservice.dao.mq.email;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.mq.MsgPublisher;

import net.sf.json.JSONObject;

/**
 * 发送消息
 * @ClassName EmailPublisher
 * @author Han Shaoming
 * @date 2016年11月3日 下午4:47:09
 * @Description TODO
 */
public class EmailPublisher {


	/**
	 * 发送消息到消息队列
	 * @author Han Shaoming
	 * @param toMail
	 * @param msgTitle
	 * @param msgContent
	 * @param pushUserId
	 * @param targetUserIds
	 * @throws Exception
	 */
	public static void publishMsg(String toMail,String mailTitle,String mailContent) throws Exception{
		//判断数据是否为空
		if(StringUtils.isEmpty(toMail)){
			throw new Exception("收件人邮箱不能为空!");
		}
		//发送消息到队列
		JSONObject sysMsg = new JSONObject();
		sysMsg.put("toMail", toMail);
		sysMsg.put("mailTitle", mailTitle);
		sysMsg.put("mailContent", mailContent);
		MsgPublisher.publish2WorkQueue("send_email", sysMsg.toString());
	}
}
