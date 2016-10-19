package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @ClassName: BatchCoreJobRequest
 * @author Xiao Xiaowen
 * @date 2016年6月21日 上午11:57:17
 * @Description: TODO：批处理核心Job参数封装
 *
 */
public class BatchCoreJobRequest extends AbstractJobRequest {


	private int executeDBId;
	private int backupDBId;
	private int kdbDBId;
	private String pidDbInfo;
	private List<String> ruleIds;

	@Override
	public void defineSubJobRequests() throws JobCreateException {

	}

	@Override
	protected int myStepCount() throws JobException {
		return 2;
	}

	@Override
	public String getJobType() {
		return "batchCore";
	}
	
	@Override
	public String getJobTypeName(){
		return "批处理（核心）";
	}

	@Override
	public void validate() throws JobException {
	}

	public int getExecuteDBId() {
		return executeDBId;
	}

	public void setExecuteDBId(int executeDBId) {
		this.executeDBId = executeDBId;
	}

	public int getBackupDBId() {
		return backupDBId;
	}

	public void setBackupDBId(int backupDBId) {
		this.backupDBId = backupDBId;
	}

	public int getKdbDBId() {
		return kdbDBId;
	}

	public void setKdbDBId(int kdbDBId) {
		this.kdbDBId = kdbDBId;
	}

	public String getPidDbInfo() {
		return pidDbInfo;
	}

	public void setPidDbInfo(String pidDbInfo) {
		this.pidDbInfo = pidDbInfo;
	}

	public List<String> getRuleIds() {
		return ruleIds;
	}

	public void setRuleIds(List<String> ruleIds) {
		this.ruleIds = ruleIds;
	}



}
