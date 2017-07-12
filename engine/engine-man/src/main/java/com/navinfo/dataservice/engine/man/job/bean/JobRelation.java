package com.navinfo.dataservice.engine.man.job.bean;

/**
 * Created by wangshishuai3966 on 2017/7/8.
 */
public class JobRelation {

    private long jobId;
    private long itemId;
    private ItemType itemType;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
