package com.navinfo.dataservice.engine.man.job.bean;

import java.sql.ResultSet;
import java.sql.SQLException;

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

    public void load(ResultSet rs) throws SQLException {
        this.setJobId(rs.getLong("job_id"));
        this.setItemId(rs.getLong("item_id"));
        this.setItemType(ItemType.valueOf(rs.getInt("item_type")));
    }
}
