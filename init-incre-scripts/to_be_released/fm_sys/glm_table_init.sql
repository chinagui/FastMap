-- Create table
create table MULTISRC_FM_SYNC
(
  sid         NUMBER(10) not null,
  sync_status NUMBER(4) not null,
  sync_time   TIMESTAMP(6),
  job_id      NUMBER(10) not null,
  zip_file    VARCHAR2(128),
  db_type     NUMBER(1)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table MULTISRC_FM_SYNC
  is '多源POI成果导入FM库管理表';
-- Add comments to the columns 
comment on column MULTISRC_FM_SYNC.sid
  is '管理id';
comment on column MULTISRC_FM_SYNC.sync_status
  is '同步状态 已接收1，导入中2 下载成功3，下载失败4，,导入成功5,导入失败6,生成统计成功7，生成统计失败8,反馈多源成功11，反馈多源失败12';
comment on column MULTISRC_FM_SYNC.sync_time
  is '同步时间';
comment on column MULTISRC_FM_SYNC.job_id
  is '生成增量的JOB任务id';
comment on column MULTISRC_FM_SYNC.zip_file
  is '增量包';
comment on column MULTISRC_FM_SYNC.db_type
  is '同步类型 1日编,2月编';
-- Create/Recreate primary, unique and foreign key constraints 
alter table MULTISRC_FM_SYNC
  add constraint PK_MS_FM_SYNC primary key (SID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
  
  
  
  
CREATE SEQUENCE MULTISRC_FM_SYNC_SEQ
MINVALUE 1
MAXVALUE 9999999999  
START WITH 1
INCREMENT BY 1
CACHE 20;
  
  
  
  
  
-- Create table
create table FM_MULTISRC_SYNC
(
  sid            NUMBER(10) not null,
  sync_status    NUMBER(4) not null,
  last_sync_time TIMESTAMP(6),
  sync_time      TIMESTAMP(6),
  job_id         NUMBER(10) not null,
  zip_file       VARCHAR2(128)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table FM_MULTISRC_SYNC
  is 'FM库POI增量更新多源镜像库管理表';
-- Add comments to the columns 
comment on column FM_MULTISRC_SYNC.sid
  is '管理id';
comment on column FM_MULTISRC_SYNC.sync_status
  is '同步状态 开始创建1，创建中2，已创建8，创建失败9，多源同步成功18，多源同步失败19';
comment on column FM_MULTISRC_SYNC.last_sync_time
  is '上次同步时间';
comment on column FM_MULTISRC_SYNC.sync_time
  is '同步时间';
comment on column FM_MULTISRC_SYNC.job_id
  is '生成增量的JOB任务id';
comment on column FM_MULTISRC_SYNC.zip_file
  is '增量包';
-- Create/Recreate primary, unique and foreign key constraints 
alter table FM_MULTISRC_SYNC
  add constraint PK_FM_MS_SYNC primary key (SID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
  
  
 CREATE SEQUENCE FM_MULTISRC_SYNC_SEQ
MINVALUE 1
MAXVALUE 9999999999  
START WITH 1
INCREMENT BY 1
CACHE 20;