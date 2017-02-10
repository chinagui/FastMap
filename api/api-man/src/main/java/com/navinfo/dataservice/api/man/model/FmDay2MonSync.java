package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;
import java.util.Date;

/** 
 * @ClassName: FmDay2MonSync
 * @author MaYunFei
 * @date 上午10:14:12
 * @Description: 
 * -- Create table
create table FM_DAY2MONTH_SYNC
(
  SYNC_ID     NUMBER(10) not null,
  CITY_ID     NUMBER(10),
  SYNC_TIME   TIMESTAMP(6),
  SYNC_STATUS NUMBER(1),
  JOB_ID      NUMBER(10)
);
-- Add comments to the columns 
comment on column FM_DAY2MONTH_SYNC.SYNC_ID
  is 'pk';
comment on column FM_DAY2MONTH_SYNC.CITY_ID
  is '城市id，参考city.city_id';
comment on column FM_DAY2MONTH_SYNC.SYNC_TIME
  is '同步时间戳';
comment on column FM_DAY2MONTH_SYNC.SYNC_STATUS
  is '同步状态 1.创建；2.开始刷库.3，开始搬移履历；4.执行精编批处理检查；5.执行深度信息分类;6.同步完成；7.同步失败.';
comment on column FM_DAY2MONTH_SYNC.JOB_ID
  is '后台jobid，参考sys库中的JOB_INFO.job_id';
-- Create/Recreate primary, unique and foreign key constraints 
alter table FM_DAY2MONTH_SYNC
  add constraint PK_FM_DAY2MON_SYNC_PK primary key (SYNC_ID);
-- Create/Recreate indexes 
create index IDX_FM_DAY2MON_SYNC_1 on FM_DAY2MONTH_SYNC (CITY_ID, SYNC_STATUS);
-- Create sequence 
create sequence FM_DAY2MONTH_SYNC_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

 */
public class FmDay2MonSync implements Serializable {
	private long sid;
	private long cityId;//315已不用
	private Date syncTime;
	private int syncStatus;
	private long jobId;
	private long regionId;//315增加，以大区为单位日落月
	public long getSid() {
		return sid;
	}
	public void setSid(long sid) {
		this.sid = sid;
	}
	public long getCityId() {
		return cityId;
	}
	public void setCityId(long cityId) {
		this.cityId = cityId;
	}
	public Date getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}
	public int getSyncStatus() {
		return syncStatus;
	}
	public void setSyncStatus(int syncStatus) {
		this.syncStatus = syncStatus;
	}
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public long getRegionId() {
		return regionId;
	}
	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}
	@Override
	public String toString() {
		return "FmDay2MonSync [sid=" + sid + ", cityId=" + cityId + ", syncTime=" + syncTime + ", syncStatus="
				+ syncStatus + ", jobId=" + jobId+ ", regionId=" + regionId + "]";
	}
	/**
	 * 同步状态， 1.创建；2.开始刷库.3，开始搬移履历；4.执行精编批处理检查；5.执行深度信息分类;6.同步完成；7.同步失败.
	 */
	public enum SyncStatusEnum { 
	    CREATE(1), HIS_FLUSH(2), HIS_MOVE(3), BATCH_CHECK(4), RE_CLASSIFY(5), SUCCESS(6), FAIL(7); 
	    private int val; 
	    SyncStatusEnum(int val) { 
	        this.val = val; 
	    } 
	    public int getValue() { 
	        return val; 
	    } 
	 } 
	
	
	
}
