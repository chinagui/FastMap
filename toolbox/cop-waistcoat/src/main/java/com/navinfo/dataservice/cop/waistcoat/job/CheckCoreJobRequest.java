package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import java.util.List;

/**
 * @ClassName: BatchCoreJobRequest
 * @author Xiao Xiaowen
 * @date 2016年6月30日 下午13:57:17
 * @Description: TODO：检查核心Job参数封装
 *
 */
public class CheckCoreJobRequest extends AbstractJobRequest {

    private int executeDBId;
    private int kdbDBId;
    private List<String> ruleIds;
    private int timeOut;

    @Override
    public void defineSubJobRequests() throws JobCreateException {

    }

    @Override
    protected int myStepCount() throws JobException {
        return 2;
    }
    @Override
    public String getJobType() {
		return "checkCore";
	}
	
	@Override
	public String getJobTypeName(){
		return "检查（核心）";
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

    public int getKdbDBId() {
		return kdbDBId;
	}

    public void setKdbDBId(int kdbDBId) {
		this.kdbDBId = kdbDBId;
	}

    public List<String> getRuleIds() {
		return ruleIds;
	}

    public void setRuleIds(List<String> ruleIds) {
		this.ruleIds = ruleIds;
	}

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

}
