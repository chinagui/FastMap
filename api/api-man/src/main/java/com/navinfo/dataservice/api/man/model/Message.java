package com.navinfo.dataservice.api.man.model;

import java.sql.Timestamp;

/** 
 * @ClassName: Message
 * @author songdongyan
 * @date 2016年9月13日
 * @Description: Message.java
 */
public class Message {

	/**
	 * 
	 */
	public Message() {
		// TODO Auto-generated constructor stub
	}

	private Integer msgId;
	private String msgTitle;
	private String msgContent;
	private Integer pushUserId;
	private Integer receiverId;
	private Timestamp pushTime;
	private Integer msgStatus;
	
	
	public Message(Integer msgId
			,String msgTitle
			,String msgContext
			,Integer pushUserId
			,Integer receiverId
			,Timestamp pushTime
			,Integer msgStatus){
		this.msgId = msgId;
		this.msgTitle = msgTitle;
		this.msgContent=msgContext;
		this.pushUserId=pushUserId;
		this.receiverId=receiverId;
		this.pushTime=pushTime;
		this.msgStatus=msgStatus;
	}
	/**
	 * @return the msgId
	 */
	public Integer getMsgId() {
		return msgId;
	}
	/**
	 * @param msgId the msgId to set
	 */
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}
	/**
	 * @return the msgTitle
	 */
	public String getMsgTitle() {
		return msgTitle;
	}
	/**
	 * @param msgTitle the msgTitle to set
	 */
	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}
	/**
	 * @return the msgContext
	 */
	public String getMsgContent() {
		return msgContent;
	}
	/**
	 * @param msgContext the msgContext to set
	 */
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	/**
	 * @return the pushUserId
	 */
	public Integer getPushUserId() {
		return pushUserId;
	}
	/**
	 * @param pushUserId the pushUserId to set
	 */
	public void setPushUserId(Integer pushUserId) {
		this.pushUserId = pushUserId;
	}
	/**
	 * @return the receiverId
	 */
	public Integer getReceiverId() {
		return receiverId;
	}
	/**
	 * @param receiverId the receiverId to set
	 */
	public void setReceiverId(Integer receiverId) {
		this.receiverId = receiverId;
	}
	/**
	 * @return the pushTime
	 */
	public Timestamp getPushTime() {
		return pushTime;
	}
	/**
	 * @param pushTime the pushTime to set
	 */
	public void setPushTime(Timestamp pushTime) {
		this.pushTime = pushTime;
	}
	/**
	 * @return the msgStatus
	 */
	public Integer getMsgStatus() {
		return msgStatus;
	}
	/**
	 * @param msgStatus the msgStatus to set
	 */
	public void setMsgStatus(Integer msgStatus) {
		this.msgStatus = msgStatus;
	}
	
	
}
