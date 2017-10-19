package com.navinfo.dataservice.edit.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/**
 * @Title: TranslateJobRequest
 * @Package: com.navinfo.dataservice.edit.job
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/16/2017
 * @Version: V1.0
 */
public class TranslateJobRequest extends AbstractJobRequest{
    private String filePath;

    @Override
    public void defineSubJobRequests() throws JobCreateException {

    }

    @Override
    public String getJobType() {
        return "translateJob";
    }

    @Override
    public String getJobTypeName() {
        return "英文翻译工具";
    }

    @Override
    protected int myStepCount() throws JobException {
        return 1;
    }

    @Override
    public void validate() throws JobException {

    }

    /**
     * Getter method for property <tt>filePath</tt>.
     *
     * @return property value of filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Setter method for property <tt>filePath</tt>.
     *
     * @param filePath value to be assigned to property filePath
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
