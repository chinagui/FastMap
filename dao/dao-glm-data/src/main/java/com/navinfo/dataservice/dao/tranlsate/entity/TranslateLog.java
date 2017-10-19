package com.navinfo.dataservice.dao.tranlsate.entity;

import java.util.Date;

/**
 * @Title: TranslateLog
 * @Package: com.navinfo.dataservice.dao.tranlsate.entity
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/12/2017
 * @Version: V1.0
 */
public class TranslateLog {

    public static final String TABLE_NAME = "TRANSLATE_LOG";

    private String id;

    private String fileName;

    private Long fileSize;

    private Long userId;

    private String downloadPath;

    private String downloadFileName;

    private Integer jobId;

    protected String downloadUrl;

    protected Date startDate;

    protected Date endDate;

    protected Integer state;

    /**
     * Getter method for property <tt>id</tt>.
     *
     * @return property value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter method for property <tt>id</tt>.
     *
     * @param id value to be assigned to property id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter method for property <tt>fileName</tt>.
     *
     * @return property value of fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter method for property <tt>fileName</tt>.
     *
     * @param fileName value to be assigned to property fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter method for property <tt>startDate</tt>.
     *
     * @return property value of startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Setter method for property <tt>startDate</tt>.
     *
     * @param startDate value to be assigned to property startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter method for property <tt>endDate</tt>.
     *
     * @return property value of endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Setter method for property <tt>endDate</tt>.
     *
     * @param endDate value to be assigned to property endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter method for property <tt>userId</tt>.
     *
     * @return property value of userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Setter method for property <tt>userId</tt>.
     *
     * @param userId value to be assigned to property userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Getter method for property <tt>downloadPath</tt>.
     *
     * @return property value of downloadPath
     */
    public String getDownloadPath() {
        return downloadPath;
    }

    /**
     * Setter method for property <tt>downloadPath</tt>.
     *
     * @param downloadPath value to be assigned to property downloadPath
     */
    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    /**
     * Getter method for property <tt>downloadFileName</tt>.
     *
     * @return property value of downloadFileName
     */
    public String getDownloadFileName() {
        return downloadFileName;
    }

    /**
     * Setter method for property <tt>downloadFileName</tt>.
     *
     * @param downloadFileName value to be assigned to property downloadFileName
     */
    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    /**
     * Getter method for property <tt>jobId</tt>.
     *
     * @return property value of jobId
     */
    public Integer getJobId() {
        return jobId;
    }

    /**
     * Setter method for property <tt>jobId</tt>.
     *
     * @param jobId value to be assigned to property jobId
     */
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    /**
     * Getter method for property <tt>state</tt>.
     *
     * @return property value of state
     */
    public Integer getState() {
        return state;
    }

    /**
     * Setter method for property <tt>state</tt>.
     *
     * @param state value to be assigned to property state
     */
    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * Getter method for property <tt>downloadUrl</tt>.
     *
     * @return property value of downloadUrl
     */
    public String getDownloadUrl() {
        return downloadPath + downloadFileName;
    }

}
