-- Create table
create table POINTADDRESS_EDIT_STATUS
(
  PID               NUMBER(10) not null,
  STATUS            NUMBER(1) default 0 not null,
  IS_UPLOAG         NUMBER(1) default 0 not null,
  UPLOAD_DATE       TIMESTAMP(6),
  FRESH_VERIFIED    NUMBER(1) default 0 not null,
  RAW_FIELDS        VARCHAR2(30),
  WORK_TYPE         NUMBER(2) default 1 not null,
  COMMIT_HIS_STATUS NUMBER(2) default 0 not null,
  SUBMIT_DATE       TIMESTAMP(6),
  QUICK_SUBTASK_ID  NUMBER(10) default 0 not null,
  QUICK_TASK_ID     NUMBER(10) default 0 not null,
  MEDIUM_SUBTASK_ID NUMBER(10) default 0 not null,
  MEDIUM_TASK_ID    NUMBER(10) default 0 not null
);
COMMIT;
-- Add comments to the columns 
comment on column POINTADDRESS_EDIT_STATUS.PID
  is '外键，点门牌的Pid';
comment on column POINTADDRESS_EDIT_STATUS.STATUS
  is '0:未作业;1:待作业;2:已作业;3:已提交';
comment on column POINTADDRESS_EDIT_STATUS.IS_UPLOAG
  is '0:未上传;1:已上传';
comment on column POINTADDRESS_EDIT_STATUS.UPLOAD_DATE
  is '上传时间';
comment on column POINTADDRESS_EDIT_STATUS.FRESH_VERIFIED
  is '0:否;1:是';
comment on column POINTADDRESS_EDIT_STATUS.RAW_FIELDS
  is '后期待修改字段';
comment on column POINTADDRESS_EDIT_STATUS.WORK_TYPE
  is '作业类型:1:常规;2:多源';
comment on column POINTADDRESS_EDIT_STATUS.COMMIT_HIS_STATUS
  is '0:未提交过;1:提交过';
comment on column POINTADDRESS_EDIT_STATUS.SUBMIT_DATE
  is '提交时间';
comment on column POINTADDRESS_EDIT_STATUS.QUICK_SUBTASK_ID
  is '快线子任务号';
comment on column POINTADDRESS_EDIT_STATUS.QUICK_TASK_ID
  is '快线任务号';
comment on column POINTADDRESS_EDIT_STATUS.MEDIUM_SUBTASK_ID
  is '中线子任务号';
comment on column POINTADDRESS_EDIT_STATUS.MEDIUM_TASK_ID
  is '中线任务号';


COMMIT;
EXIT;
