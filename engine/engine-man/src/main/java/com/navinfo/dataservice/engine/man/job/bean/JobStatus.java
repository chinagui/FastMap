package com.navinfo.dataservice.engine.man.job.bean;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public enum JobStatus {
    RUNNING(1), SUCCESS(2), FAILURE(3),NODATA(4);

    private int value = 0;

    private JobStatus(int value) {
        this.value = value;
    }

    public static JobStatus valueOf(int value) {
        return JobStatus.values()[value - 1];
    }

    public int value() {
        return this.value;
    }
}
