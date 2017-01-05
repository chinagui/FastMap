package com.navinfo.dataservice.engine.fcc.patternImage;

/** 
 * @ClassName: PatternImage.java
 * @author y
 * @date 2016-12-29 下午2:35:17
 * @Description: 模式图model
 *  
 */
public class PatternImage {
	
	
	String name	;//文件名称	
	String format;		//文件后缀
	byte[] content;		//文件内容
	String bType;		//大文件类型
	String mType;		//中文件类型
	int userId;		//创建人
	String operateDate;		//创建时间
	String uploadDate	;	//上传时间
	String downloadDate;		//下载时间
	int status		;//当前阶段作业状态
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}
	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}
	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}
	/**
	 * @return the bType
	 */
	public String getbType() {
		return bType;
	}
	/**
	 * @param bType the bType to set
	 */
	public void setbType(String bType) {
		this.bType = bType;
	}
	/**
	 * @return the mType
	 */
	public String getmType() {
		return mType;
	}
	/**
	 * @param mType the mType to set
	 */
	public void setmType(String mType) {
		this.mType = mType;
	}
	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the operateDate
	 */
	public String getOperateDate() {
		return operateDate;
	}
	/**
	 * @param operateDate the operateDate to set
	 */
	public void setOperateDate(String operateDate) {
		this.operateDate = operateDate;
	}
	/**
	 * @return the uploadDate
	 */
	public String getUploadDate() {
		return uploadDate;
	}
	/**
	 * @param uploadDate the uploadDate to set
	 */
	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}
	/**
	 * @return the downloadDate
	 */
	public String getDownloadDate() {
		return downloadDate;
	}
	/**
	 * @param downloadDate the downloadDate to set
	 */
	public void setDownloadDate(String downloadDate) {
		this.downloadDate = downloadDate;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	

	
	
	

}
