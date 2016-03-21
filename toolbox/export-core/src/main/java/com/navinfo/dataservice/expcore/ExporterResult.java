package com.navinfo.dataservice.expcore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

/** 
 * @ClassName: ExporterResult 
 * @author Xiao Xiaowen 
 * @date 2015-10-29 下午5:11:45 
 * @Description: TODO
 *  
 */

public class ExporterResult {
	public static Integer STATUS_FAILED=-1;
	public static Integer STATUS_INIT=0;
	public static Integer STATUS_SUCCESS=100;
	private int status;//-1失败，0，导出初始，100导出成功完成
	private long timeConsumingInSec;
	private String msg;//备注，错误时写入的为错误信息
	private List<String> exportSteps = new ArrayList<String>();
	private int newTargetDbId;
	public ExporterResult(){
		this.status=STATUS_INIT;
	}
	
	/**
	 * @return the code
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param code the code to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the timeConsumingInSec
	 */
	public long getTimeConsumingInSec() {
		return timeConsumingInSec;
	}

	/**
	 * @param timeConsumingInSec the timeConsumingInSec to set
	 */
	public void setTimeConsumingInSec(long timeConsumingInSec) {
		this.timeConsumingInSec = timeConsumingInSec;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void addStep(String content){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		exportSteps.add("["+sdf.format(new Date())+"]"+content);
	}

	public int getNewTargetDbId() {
		return newTargetDbId;
	}

	public void setNewTargetDbId(int newTargetDbId) {
		this.newTargetDbId = newTargetDbId;
	}
}
