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
	
	private long msgId;
	private int msgType;
	private String msgContent;
	private Date createTime;
	private long targetUserId;
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
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
	
}
