package com.navinfo.dataservice.api.dropbox.model;

import java.io.Serializable;

/** 
 * @ClassName: UploadInfo
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: UploadInfo.java
 */
public class UploadInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	int uploadId=0;
	int progress=0;
	String fileName = null;
	String filePath = null;
	String fileMd5 = null;
	double fileSize = 0.0;
	public int getUploadId() {
		return uploadId;
	}
	public void setUploadId(int uploadId) {
		this.uploadId = uploadId;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileMd5() {
		return fileMd5;
	}
	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}
	public double getFileSize() {
		return fileSize;
	}
	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

}
