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
alter table FM_DAY2MONTH_SYNC
  add constraint PK_FM_DAY2MON_SYNC_PK primary key (SYNC_ID);
create index IDX_FM_DAY2MON_SYNC_1 on FM_DAY2MONTH_SYNC (CITY_ID, SYNC_STATUS);
-- Create sequence 
create sequence FM_DAY2MONTH_SYNC_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;  