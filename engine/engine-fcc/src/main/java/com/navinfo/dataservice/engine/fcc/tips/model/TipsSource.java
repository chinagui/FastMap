package com.navinfo.dataservice.engine.fcc.tips.model;

/**
 * Created by zhangjunfang on 2017/5/19.
 */
public class TipsSource {
    private int s_featureKind = 2;
    private String s_project = "";
    private int s_sourceCode;
    private String s_sourceId = "";
    private String s_sourceType = "";
    private int s_sourceProvider = 0;
    private int s_reliability = 100;
    private int s_qTaskId = 0; //快线采集任务ID
    private int s_mTaskId = 0; //中线采集任务ID
    private int s_qSubTaskId = 0;//快线采集子任务ID
    private int s_mSubTaskId = 0;//中线采集子任务ID

    public int getS_featureKind() {
        return s_featureKind;
    }

    public void setS_featureKind(int s_featureKind) {
        this.s_featureKind = s_featureKind;
    }

    public String getS_project() {
        return s_project;
    }

    public void setS_project(String s_project) {
        this.s_project = s_project;
    }

    public int getS_sourceCode() {
        return s_sourceCode;
    }

    public void setS_sourceCode(int s_sourceCode) {
        this.s_sourceCode = s_sourceCode;
    }

    public String getS_sourceId() {
        return s_sourceId;
    }

    public void setS_sourceId(String s_sourceId) {
        this.s_sourceId = s_sourceId;
    }

    public String getS_sourceType() {
        return s_sourceType;
    }

    public void setS_sourceType(String s_sourceType) {
        this.s_sourceType = s_sourceType;
    }

    public int getS_sourceProvider() {
        return s_sourceProvider;
    }

    public void setS_sourceProvider(int s_sourceProvider) {
        this.s_sourceProvider = s_sourceProvider;
    }

    public int getS_reliability() {
        return s_reliability;
    }

    public void setS_reliability(int s_reliability) {
        this.s_reliability = s_reliability;
    }

    public int getS_qTaskId() {
        return s_qTaskId;
    }

    public void setS_qTaskId(int s_qTaskId) {
        this.s_qTaskId = s_qTaskId;
    }

    public int getS_mTaskId() {
        return s_mTaskId;
    }

    public void setS_mTaskId(int s_mTaskId) {
        this.s_mTaskId = s_mTaskId;
    }

    public int getS_qSubTaskId() {
        return s_qSubTaskId;
    }

    public void setS_qSubTaskId(int s_qSubTaskId) {
        this.s_qSubTaskId = s_qSubTaskId;
    }

    public int getS_mSubTaskId() {
        return s_mSubTaskId;
    }

    public void setS_mSubTaskId(int s_mSubTaskId) {
        this.s_mSubTaskId = s_mSubTaskId;
    }
}
