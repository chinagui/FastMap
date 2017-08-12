package com.navinfo.dataservice.check.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import java.util.List;

/**
 * Created by ly on 2017/8/1.
 */
public class DayLogCheckImportJobRequest extends AbstractJobRequest {


    protected int logDbId;
    protected int targetDbId;
    protected boolean ignoreError;
    protected String logMoveType="default";//如果是copBatch


    @Override
    public String getJobType() {
        return "dayLogCheckImport";
    }

    @Override
    public String getJobTypeName(){
        return "履历回库";
    }

    @Override
    public void validate() throws JobException {
        // TODO Auto-generated method stub

    }


    public int getLogDbId() {
        return logDbId;
    }

    public void setLogDbId(int logDbId) {
        this.logDbId = logDbId;
    }

    public int getTargetDbId() {
        return targetDbId;
    }

    public void setTargetDbId(int targetDbId) {
        this.targetDbId = targetDbId;
    }

    public boolean isIgnoreError() {
        return ignoreError;
    }

    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    public String getLogMoveType() {
        return logMoveType;
    }

    public void setLogMoveType(String logMoveType) {
        this.logMoveType = logMoveType;
    }

    @Override
    public void defineSubJobRequests() throws JobCreateException {

    }

    @Override
    protected int myStepCount() throws JobException {
        return 3;
    }

}
