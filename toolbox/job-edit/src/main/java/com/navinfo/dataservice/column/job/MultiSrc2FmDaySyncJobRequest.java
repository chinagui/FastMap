package com.navinfo.dataservice.column.job;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: MultiSrc2FmDaySyncJobRequest
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: MultiSrc2FmDaySyncJobRequest.java
 */
public class MultiSrc2FmDaySyncJobRequest extends AbstractJobRequest {
	
	protected String remoteZipFile;

	public String getRemoteZipFile() {
		return remoteZipFile;
	}

	public void setRemoteZipFile(String remoteZipFile) {
		this.remoteZipFile = remoteZipFile;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		return "multisrc2FmDay";
	}

	@Override
	public String getJobTypeName() {
		return "多源导入日库";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

	@Override
	public void validate() throws JobException {
		if(StringUtils.isEmpty(remoteZipFile))throw new JobException("导入的目标文件包地址为空");
	}

}
