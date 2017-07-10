package com.navinfo.dataservice.engine.man.job.bean;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;

import java.util.Date;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public class JobProgress {
    private long phaseId;
    private long jobId;
    private int phase;
    private JobProgressStatus status = JobProgressStatus.CREATED;
    private int lastest = 1;
    private Date createDate;
    private Date startDate;
    private Date endDate;
    private String message;
    private JSONObject parameter;

    public long getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(long phaseId) {
        this.phaseId = phaseId;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public JobProgressStatus getStatus() {
        return status;
    }

    public void setStatus(JobProgressStatus status) {
        this.status = status;
    }

    public int getLastest() {
        return lastest;
    }

    public void setLastest(int lastest) {
        this.lastest = lastest;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONObject getParameter() {
        return parameter;
    }

    public void setParameter(JSONObject parameter) {
        this.parameter = parameter;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }
}
