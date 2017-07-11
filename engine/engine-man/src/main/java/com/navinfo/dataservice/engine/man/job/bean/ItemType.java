package com.navinfo.dataservice.engine.man.job.bean;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public enum ItemType {
    PROJECT(1),
    TASK(2),
    SUBTASK(3),
    LOT(4);  //批次

    private int value = 0;

    private ItemType(int value) {
        this.value = value;
    }

    public static ItemType valueOf(int value) {
        return ItemType.values()[value-1];
    }

    public int value() {
        return this.value;
    }
}
