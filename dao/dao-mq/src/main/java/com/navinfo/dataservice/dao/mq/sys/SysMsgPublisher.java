package com.navinfo.dataservice.dao.mq.sys;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.mq.MsgPublisher;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SysMsgPublisher
 * @author Han Shaoming
 * @date 2016年9月21日 下午1:58:15
 * @Description 发送消息
 */
public class SysMsgPublisher {
	
	/**
	 * 发送消息到消息队列
	 * @param msgTitle
	 * @param msgContent
	 * @param pushUserId
	 * @param targetUserIds 0:系统消息发给所有人
	 * @throws Exception 
	 */
	public static void publishMsg(String msgTitle,String msgContent,long pushUserId,long[] targetUserIds) throws Exception{
		//判断数据是否为空
		if(StringUtils.isEmpty(msgTitle)||StringUtils.isEmpty(msgContent)
				||targetUserIds==null||targetUserIds.length==0){
			throw new Exception("消息标题或消息内容或收件人id不能为空");
		}
		//发给所有人
		if(targetUserIds.length==1 && targetUserIds[0]==0){
			//发送消息到队列
			for (long targetUserId : targetUserIds) {
				JSONObject sysMsg = new JSONObject();
				sysMsg.put("msgTitle", msgTitle);
				sysMsg.put("msgContent", msgContent);
				sysMsg.put("pushUserId", pushUserId);
				sysMsg.put("targetUserId", targetUserId);
				MsgPublisher.publish2WorkQueue("all_msg", sysMsg.toString());
			}
		}
		//发给个人
		if(targetUserIds.length ==1 && targetUserIds[0] !=0){
			//发送消息到队列
			for (long targetUserId : targetUserIds) {
				JSONObject sysMsg = new JSONObject();
				sysMsg.put("msgTitle", msgTitle);
				sysMsg.put("msgContent", msgContent);
				sysMsg.put("pushUserId", pushUserId);
				sysMsg.put("targetUserId", targetUserId);
				MsgPublisher.publish2WorkQueue("personal_msg", sysMsg.toString());
			}
		}
		//发给一组人
		if(targetUserIds.length > 1){
			//发送消息到队列
			for (long targetUserId : targetUserIds) {
				JSONObject sysMsg = new JSONObject();
				sysMsg.put("msgTitle", msgTitle);
				sysMsg.put("msgContent", msgContent);
				sysMsg.put("pushUserId", pushUserId);
				sysMsg.put("targetUserId", targetUserId);
				MsgPublisher.publish2WorkQueue("group_msg", sysMsg.toString());
			}
		}
	}

}
