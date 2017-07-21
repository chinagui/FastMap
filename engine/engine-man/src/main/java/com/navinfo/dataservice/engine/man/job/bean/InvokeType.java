package com.navinfo.dataservice.engine.man.job.bean;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public enum InvokeType {
    ASYNC(0), SYNC(1);//ASYNC异步 SYNC同步

    private int value = 0;

    private InvokeType(int value) {
        this.value = value;
    }

    public static InvokeType valueOf(int value) {
        return InvokeType.values()[value];
    }

    public int value() {
        return this.value;
    }
}
