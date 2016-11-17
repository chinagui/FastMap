package com.navinfo.dataservice.engine.sys.msg;

import java.io.Serializable;
import java.util.Date;

/** 
 * @ClassName: SysMsg
 * @author xiaoxiaowen4127
 * @date 2016年9月8日
 * @Description: SysMsg.java
 */
public class SysMsg implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private long msgId;
	private long msgType;
	private String msgContent;
	private Date createTime;
	private long targetUserId;
	private String msgTitle;
	private long pushUserId;
	private String msgParam;
	private String pushUserName;
	
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public long getMsgType() {
		return msgType;
	}
	public void setMsgType(long msgType) {
		this.msgType = msgType;
	}
	public String getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public long getTargetUserId() {
		return targetUserId;
	}
	public void setTargetUserId(long targetUserId) {
		this.targetUserId = targetUserId;
	}
	public String getMsgTitle() {
		return msgTitle;
	}
	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}
	public Long getPushUserId() {
		return pushUserId;
	}
	public void setPushUserId(Long pushUserId) {
		this.pushUserId = pushUserId;
	}
	public String getMsgParam() {
		return msgParam;
	}
	public void setMsgParam(String msgParam) {
		this.msgParam = msgParam;
	}
	public String getPushUserName() {
		return pushUserName;
	}
	public void setPushUserName(String pushUserName) {
		this.pushUserName = pushUserName;
	}
	
}
