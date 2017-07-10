package com.navinfo.dataservice.engine.man.job.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public class Job {
    private long jobId;
    private JobType type;
    private long operator;
    private JobStatus status;
    private Date createDate;
    private Date endDate;
    private int lastest = 1;
    private JSONObject parameter;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public long getOperator() {
        return operator;
    }

    public void setOperator(long operator) {
        this.operator = operator;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getLastest() {
        return lastest;
    }

    public void setLastest(int lastest) {
        this.lastest = lastest;
    }

    public JSONObject getParameter() {
        return parameter;
    }

    public void setParameter(JSONObject parameter) {
        this.parameter = parameter;
    }
}
