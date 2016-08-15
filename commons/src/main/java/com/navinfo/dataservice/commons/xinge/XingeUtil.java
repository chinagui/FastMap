package com.navinfo.dataservice.commons.xinge;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.tencent.xinge.ClickAction;
import com.tencent.xinge.Message;
import com.tencent.xinge.MessageIOS;
import com.tencent.xinge.Style;
import com.tencent.xinge.XingeApp;

public class XingeUtil {
	public static String CUSTOM_DICT_KEY_MSG_TYPE="msgType";
	public static String CUSTOM_DICT_KEY_OTHER_INFO="otherInfo";
	public static int PUSH_MSG_TYPE_PROJECT=1;//#项目
	public static int PUSH_MSG_TYPE_GRID=2;//#地盘
	public static int PUSH_MSG_TYPE_INNERMSG=3;//#站内信息
	public static int PUSH_MSG_TYPE_FEEDBACK=4;//#站内反馈
	public static int PUSH_MSG_TYPE_CLOSETASK=5;//#关闭地盘
	
	public static String DEVICE_PLATFORM_ANDROID="Android";
	public static String DEVICE_PLATFORM_IOS="IOS";
	
	String devicePlatform="";
	String deviceToken="";
	String title="";
	String content="";
	String msgType="";
	String otherInfo=""; 
	
	public static long iosAccessId = 2100042998;
	public static String iosSecrectKey = "a547532e1c19cdb1e013651ff68798a8";
	
	public static long androidAccessId = 2100042998;
	public static String androidSecrectKey = "a547532e1c19cdb1e013651ff68798a8";

	public XingeUtil(String devicePlatform,String deviceToken,String title,String content,String msgType,String otherInfo) {
		this.devicePlatform=devicePlatform;
		this.deviceToken=deviceToken;
		this.title=title;
		this.content=content;
		this.msgType=msgType;
		this.otherInfo=otherInfo;
	}
	
	public JSONObject pushSingleDevice(){
		JSONObject msgReturn=new JSONObject();
		if(this.devicePlatform.equals(XingeUtil.DEVICE_PLATFORM_ANDROID)){
			XingeApp xingeApp = new XingeApp(XingeUtil.androidAccessId, XingeUtil.androidSecrectKey);
			Message msg = this.buildAndroidNotification();
			msgReturn=xingeApp.pushSingleDevice(this.deviceToken, msg);
		}
		if(this.devicePlatform.equals(XingeUtil.DEVICE_PLATFORM_IOS)){
			XingeApp xingeApp = new XingeApp(XingeUtil.iosAccessId, XingeUtil.iosSecrectKey);
			MessageIOS msg = this.buildIOSNotification();
			msgReturn=xingeApp.pushSingleDevice(this.deviceToken, msg, 1);
		}
        return  msgReturn;
	}
	
    private MessageIOS buildIOSNotification() {    	
    	MessageIOS localMessageIOS = new MessageIOS();
		localMessageIOS.setAlert(this.title+":"+this.content);
		localMessageIOS.setBadge(1);
		localMessageIOS.setSound("beep.wav");
		Map<String, Object> customMsg=new HashMap<String, Object>();
		customMsg.put(XingeUtil.CUSTOM_DICT_KEY_MSG_TYPE, this.msgType);
		customMsg.put(XingeUtil.CUSTOM_DICT_KEY_OTHER_INFO, this.otherInfo);
		localMessageIOS.setCustom(customMsg);
		return localMessageIOS;
	}

	private Message buildAndroidNotification() {
		Message localMessage = new Message();
		localMessage.setType(1);
		localMessage.setTitle(this.title);
		localMessage.setContent(this.content);
		Map<String, Object> customMsg=new HashMap<String, Object>();
		customMsg.put(XingeUtil.CUSTOM_DICT_KEY_MSG_TYPE, this.msgType);
		customMsg.put(XingeUtil.CUSTOM_DICT_KEY_OTHER_INFO, this.otherInfo);
		localMessage.setCustom(customMsg);
		
		/* 通知展示样式，仅对通知有效
        # 样式编号为2，响铃，震动，不可从通知栏清除，不影响先前通知*/
		localMessage.setStyle(new Style(2));
		/*# 点击动作设置，仅对通知有效
        # 以下例子为点击打开url*/
		ClickAction action=new ClickAction();
		action.setActionType(1);
		localMessage.setAction(action);
		return localMessage;
	}   

}
