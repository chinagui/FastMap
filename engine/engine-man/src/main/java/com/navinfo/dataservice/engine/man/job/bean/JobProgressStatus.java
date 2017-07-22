package com.navinfo.dataservice.engine.man.job.bean;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public enum JobProgressStatus {
    CREATED(0), RUNNING(1), SUCCESS(2), FAILURE(3), NODATA(4);

    private int value = 0;

    private JobProgressStatus(int value) {
        this.value = value;
    }

    public static JobProgressStatus valueOf(int value) {
        return JobProgressStatus.values()[value];
    }

    public int value() {
        return this.value;
    }
}
