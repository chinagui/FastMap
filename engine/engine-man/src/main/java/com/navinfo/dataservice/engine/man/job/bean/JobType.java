package com.navinfo.dataservice.engine.man.job.bean;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public enum JobType {
    TiPS2MARK(1),
    DAY2MONTH(2),
    NOTASK2MID(3); //无任务转中

    private int value = 0;

    private JobType(int value) {
        this.value = value;
    }

    public static JobType valueOf(int value) {
        return JobType.values()[value - 1];
    }

    public int value() {
        return this.value;
    }
}
