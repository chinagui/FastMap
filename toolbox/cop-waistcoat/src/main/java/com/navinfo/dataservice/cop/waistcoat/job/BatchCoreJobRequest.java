package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: BatchCoreJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月21日 上午11:57:17 
* @Description: TODO
*  
*/
public class BatchCoreJobRequest extends AbstractJobRequest {

    private int executeDBId = 0;
	private String executeGdbConnInfo = "";
	private String backupGdbConnInfo = "";
    private String kdbConnInfo = "";
	private String pidConnInfo = "";
    private String ruleIds = "";

	@Override
	public String getJobType() {
		return "batchCore";
	}

	@Override
	public int getStepCount() throws JobException {
		return 3;
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

    public String getExecuteGdbConnInfo() {
        return executeGdbConnInfo;
    }

    public void setExecuteGdbConnInfo(String executeGdbConnInfo) {
        this.executeGdbConnInfo = executeGdbConnInfo;
    }

    public String getKdbConnInfo() {
        return kdbConnInfo;
    }

    public void setKdbConnInfo(String kdbConnInfo) {
        this.kdbConnInfo = kdbConnInfo;
    }

    public String getBackupGdbConnInfo() {
        return backupGdbConnInfo;
    }

    public void setBackupGdbConnInfo(String backupGdbConnInfo) {
        this.backupGdbConnInfo = backupGdbConnInfo;
    }

    public String getPidConnInfo() {
        return pidConnInfo;
    }

    public void setPidConnInfo(String pidConnInfo) {
        this.pidConnInfo = pidConnInfo;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

}
