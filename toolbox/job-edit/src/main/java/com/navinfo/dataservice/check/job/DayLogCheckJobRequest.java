package com.navinfo.dataservice.check.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;


public class DayLogCheckJobRequest extends AbstractJobRequest
{
    private int bakDbId;//备份库
    private int regionDbId;//大区库

    @Override
    public void defineSubJobRequests() throws JobCreateException {

    }

    @Override
    protected int myStepCount() throws JobException {
        return 0;
    }

    @Override
    public String getJobType() {
        return "dayLogCheck";
    }
    @Override
    public String getJobTypeName(){
        return "日编履历正确性验证";
    }

    @Override
    public void validate() throws JobException {

    }

    public int getBakDbId() {
        return bakDbId;
    }

    public void setBakDbId(int bakDbId) {
        this.bakDbId = bakDbId;
    }

    public int getRegionDbId() {
        return regionDbId;
    }

    public void setRegionDbId(int regionDbId) {
        this.regionDbId = regionDbId;
    }
}
