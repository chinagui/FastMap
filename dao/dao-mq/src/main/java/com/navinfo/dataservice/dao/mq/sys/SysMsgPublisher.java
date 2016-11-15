package com.navinfo.dataservice.dao.mq.sys;

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
	public static void publishMsg(String msgTitle,String msgContent,long pushUserId,long[] targetUserIds,long msgType,String msgParam,String pushUserName) throws Exception{
		//判断数据是否为空
		if(msgType == 0	|| targetUserIds==null || targetUserIds.length==0){
			throw new Exception("收件人id和消息类型msgType不能为空");
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
				sysMsg.put("msgType", msgType);
				sysMsg.put("msgParam", msgParam);
				sysMsg.put("pushUserName", pushUserName);
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
				sysMsg.put("msgType", msgType);
				sysMsg.put("msgParam", msgParam);
				sysMsg.put("pushUserName", pushUserName);
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
				sysMsg.put("msgType", msgType);
				sysMsg.put("msgParam", msgParam);
				sysMsg.put("pushUserName", pushUserName);
				MsgPublisher.publish2WorkQueue("group_msg", sysMsg.toString());
			}
		}
	}
	
	/**
	 * 发送申请消息到消息队列
	 * @author Han Shaoming
	 * @param message
	 * @throws Exception
	 */
	public static void publishApplyMsg(String applyMessage,long userId) throws Exception{
		JSONObject applyMsg = new JSONObject();
		applyMsg.put("userId", userId);
		applyMsg.put("applyMessage", applyMessage);
		//发送申请消息
		MsgPublisher.publish2WorkQueue("apply_personal_msg", applyMsg.toString());
	}
	
	

}
