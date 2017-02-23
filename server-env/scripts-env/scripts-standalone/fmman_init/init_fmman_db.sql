CREATE DATABASE LINK metadb_link
  CONNECT TO &1 IDENTIFIED BY &2
  USING '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = &3 )(PORT = &4 )))(CONNECT_DATA = (SERVICE_NAME = &5 )))';

CREATE TABLE CP_REGION_PROVINCE
(
  REGION_ID      NUMBER(10) NOT NULL,
  NDS_REGIONCODE VARCHAR2(10),
  ADMINCODE      NUMBER(6),
  PROVINCE       VARCHAR2(60)
);

create table USER_INFO
(
  user_id        NUMBER(10) not null,
  user_real_name VARCHAR2(50),
  user_nick_name VARCHAR2(50),
  user_password  VARCHAR2(20),
  user_email     VARCHAR2(75),
  user_phone     VARCHAR2(50),
  user_level     NUMBER(2),
  user_score     NUMBER(10),
  user_icon      CLOB,
  user_gpsid     VARCHAR2(50)
);
comment on table USER_INFO
  is '用户信息表';
-- Add comments to the columns 
comment on column USER_INFO.user_id
  is '用户ID';
comment on column USER_INFO.user_real_name
  is '用户真实名';
comment on column USER_INFO.user_nick_name
  is '用户昵称';
comment on column USER_INFO.user_password
  is '用户密码';
comment on column USER_INFO.user_email
  is '用户邮箱';
comment on column USER_INFO.user_phone
  is '用户电话';
comment on column USER_INFO.user_level
  is '用户等级';
comment on column USER_INFO.user_score
  is '用户积分';
comment on column USER_INFO.user_icon
  is '用户头像';
comment on column USER_INFO.user_gpsid
  is '用户GPSID';
alter table USER_INFO
  add constraint PK_USERINFO primary key (USER_ID);
CREATE TABLE REGION
(
  REGION_ID     NUMBER(10) not null,
  REGION_NAME   VARCHAR2(200),
  DAILY_DB_ID   NUMBER(10),
  MONTHLY_DB_ID NUMBER(10),
  constraint PK_REGION_ID primary key (REGION_ID)
);
COMMENT ON COLUMN REGION.REGION_ID IS '大区ID';
COMMENT ON COLUMN REGION.REGION_NAME IS '日库ID';
COMMENT ON COLUMN REGION.DAILY_DB_ID IS '日库ID';
COMMENT ON COLUMN REGION.MONTHLY_DB_ID IS '月库ID';

create table CITY
(
  city_id       NUMBER(10) not null,
  city_name     VARCHAR2(200),
  admin_id      NUMBER(6),
  province_name VARCHAR2(50),
  geometry      SDO_GEOMETRY,
  region_id     NUMBER(10) not null,
  plan_status   NUMBER(1) default 0 not null,
  constraint PK_CITY_ID primary key (CITY_ID)
);
comment on column CITY.city_id is '城市ID';
comment on column CITY.city_name  is '城市名称';
comment on column CITY.admin_id  is '行政代码';
comment on column CITY.province_name  is '省名';
comment on column CITY.geometry  is '几何';
comment on column CITY.region_id  is '大区ID';
comment on column CITY.plan_status  is '规划状态。0未规划，1已规划，2已关闭';


create table BLOCK
(
  block_id    NUMBER(10) not null,
  city_id     NUMBER(10) not null,
  block_name  VARCHAR2(200),
  geometry    SDO_GEOMETRY,
  plan_status NUMBER(1) default 0 not null,
  constraint PK_BLOCK_ID primary key (block_id),
  constraint FK_CITY_ID foreign key (CITY_ID) references CITY (CITY_ID) disable
);
comment on column BLOCK.block_id  is 'BLOCK ID';
comment on column BLOCK.city_id  is '城市ID';
comment on column BLOCK.block_name  is 'BLOCK名称';
comment on column BLOCK.geometry  is '几何';
comment on column BLOCK.plan_status  is '规划状态。0未规划，1已规划，2已关闭';



CREATE TABLE BLOCK_MAN  (
	BLOCK_MAN_ID NUMBER(10) not null,
	BLOCK_ID NUMBER(10) not null,	
	STATUS NUMBER(1) default 1 not null,
	LATEST NUMBER(1) default 1 not null,
	DESCP VARCHAR2(200), 	
	CREATE_USER_ID NUMBER(10),	
	CREATE_DATE TIMESTAMP,	
	COLLECT_PLAN_START_DATE TIMESTAMP,	
	COLLECT_PLAN_END_DATE TIMESTAMP,	
	COLLECT_GROUP_ID NUMBER(10),
	DAY_EDIT_PLAN_START_DATE TIMESTAMP,
	DAY_EDIT_PLAN_END_DATE TIMESTAMP,	
	DAY_EDIT_GROUP_ID NUMBER(10),
	MONTH_EDIT_PLAN_START_DATE TIMESTAMP,
	MONTH_EDIT_PLAN_END_DATE TIMESTAMP,	
	MONTH_EDIT_GROUP_ID NUMBER(10),	
	DAY_PRODUCE_PLAN_START_DATE TIMESTAMP,	
	DAY_PRODUCE_PLAN_END_DATE TIMESTAMP,	
	MONTH_PRODUCE_PLAN_START_DATE TIMESTAMP,	
	MONTH_PRODUCE_PLAN_END_DATE TIMESTAMP,	
	constraint PK_BLOCK_MAN_ID primary key (BLOCK_MAN_ID),
	constraint FK_BLOCK_ID foreign key (BLOCK_ID) references BLOCK (BLOCK_ID) disable,
	constraint FK_USER_ID foreign key (CREATE_USER_ID) references USER_INFO (USER_ID) disable
);
COMMENT ON TABLE BLOCK_MAN IS 'BLOCK管理表';
COMMENT ON COLUMN BLOCK_MAN.BLOCK_MAN_ID IS 'BLOCK_MAN_ID';
COMMENT ON COLUMN BLOCK_MAN.BLOCK_ID IS 'BLOCK ID';
COMMENT ON COLUMN BLOCK_MAN.STATUS IS 'BLOCK状态。0关闭，1开启';
COMMENT ON COLUMN BLOCK_MAN.LATEST IS '是否与block为最新关联。0否，1是';
COMMENT ON COLUMN BLOCK_MAN.DESCP IS 'BLOCK描述';
COMMENT ON COLUMN BLOCK_MAN.CREATE_USER_ID IS '创建人';
COMMENT ON COLUMN BLOCK_MAN.CREATE_DATE IS '创建时间';
COMMENT ON COLUMN BLOCK_MAN.COLLECT_PLAN_START_DATE IS '采集计划开始时间';
COMMENT ON COLUMN BLOCK_MAN.COLLECT_PLAN_END_DATE IS '采集计划结束时间';
COMMENT ON COLUMN BLOCK_MAN.COLLECT_GROUP_ID IS '采集作业组ID';
COMMENT ON COLUMN BLOCK_MAN.DAY_EDIT_PLAN_START_DATE IS '日编计划开始时间';
COMMENT ON COLUMN BLOCK_MAN.DAY_EDIT_PLAN_END_DATE IS '日编计划结束时间';
COMMENT ON COLUMN BLOCK_MAN.DAY_EDIT_GROUP_ID IS '日编作业组ID';
COMMENT ON COLUMN BLOCK_MAN.MONTH_EDIT_PLAN_START_DATE IS '月编计划开始时间';
COMMENT ON COLUMN BLOCK_MAN.MONTH_EDIT_PLAN_END_DATE IS '月编计划结束时间';
COMMENT ON COLUMN BLOCK_MAN.MONTH_EDIT_GROUP_ID IS '月编作业组ID';
COMMENT ON COLUMN BLOCK_MAN.DAY_PRODUCE_PLAN_START_DATE IS '日出品计划开始时间';
COMMENT ON COLUMN BLOCK_MAN.DAY_PRODUCE_PLAN_END_DATE IS '日出品计划结束时间';
COMMENT ON COLUMN BLOCK_MAN.MONTH_PRODUCE_PLAN_START_DATE IS '月出品计划开始时间';
COMMENT ON COLUMN BLOCK_MAN.MONTH_PRODUCE_PLAN_END_DATE IS '月出品计划结束时间';


CREATE TABLE INFOR_MAN  (
  INFOR_ID VARCHAR2(32) not null, 
  INFOR_STATUS NUMBER(1) default 1 not null,  
  DESCP VARCHAR2(200),
  CREATE_USER_ID NUMBER(10),
  CREATE_DATE TIMESTAMP,
  COLLECT_PLAN_START_DATE TIMESTAMP,
  COLLECT_PLAN_END_DATE TIMESTAMP,
  DAY_EDIT_PLAN_START_DATE TIMESTAMP, 
  DAY_EDIT_PLAN_END_DATE TIMESTAMP,
  MONTH_EDIT_PLAN_START_DATE TIMESTAMP, 
  MONTH_EDIT_PLAN_END_DATE TIMESTAMP, 
  DAY_PRODUCE_PLAN_START_DATE TIMESTAMP,  
  DAY_PRODUCE_PLAN_END_DATE TIMESTAMP,  
  MONTH_PRODUCE_PLAN_START_DATE TIMESTAMP,  
  MONTH_PRODUCE_PLAN_END_DATE TIMESTAMP,
  constraint FK_INFOR_MAN_USER_ID foreign key (CREATE_USER_ID) references USER_INFO (USER_ID) disable
);
COMMENT ON TABLE INFOR_MAN IS 'INFO管理表';
COMMENT ON COLUMN INFOR_MAN.INFOR_ID IS '情报ID';
COMMENT ON COLUMN INFOR_MAN.INFOR_STATUS IS '情报规划状态。0关闭，1开启';
COMMENT ON COLUMN INFOR_MAN.DESCP IS '情报规划描述';
COMMENT ON COLUMN INFOR_MAN.CREATE_USER_ID IS '创建人';
COMMENT ON COLUMN INFOR_MAN.CREATE_DATE IS '创建时间';
COMMENT ON COLUMN INFOR_MAN.COLLECT_PLAN_START_DATE IS '采集计划开始时间';
COMMENT ON COLUMN INFOR_MAN.COLLECT_PLAN_END_DATE IS '采集计划结束时间';
COMMENT ON COLUMN INFOR_MAN.DAY_EDIT_PLAN_START_DATE IS '日编计划开始时间';
COMMENT ON COLUMN INFOR_MAN.DAY_EDIT_PLAN_END_DATE IS '日编计划结束时间';
COMMENT ON COLUMN INFOR_MAN.MONTH_EDIT_PLAN_START_DATE IS '月编计划开始时间';
COMMENT ON COLUMN INFOR_MAN.MONTH_EDIT_PLAN_END_DATE IS '月编计划结束时间';
COMMENT ON COLUMN INFOR_MAN.DAY_PRODUCE_PLAN_START_DATE IS '日出品计划开始时间';
COMMENT ON COLUMN INFOR_MAN.DAY_PRODUCE_PLAN_END_DATE IS '日出品计划结束时间';
COMMENT ON COLUMN INFOR_MAN.MONTH_PRODUCE_PLAN_START_DATE IS '月出品计划开始时间';
COMMENT ON COLUMN INFOR_MAN.MONTH_PRODUCE_PLAN_END_DATE IS '月出品计划结束时间';



CREATE TABLE CUSTOMISED_LAYER  (
  LAYER_ID NUMBER(10) not null, 
  LAYER_NAME VARCHAR2(200),  
  GEOMETRY SDO_GEOMETRY,
  CREATE_USER_ID NUMBER(10),
  CREATE_DATE TIMESTAMP,
  STATUS NUMBER(1),
  constraint PK_LAYER_ID primary key (LAYER_ID),
  constraint FK_CUSTOMISED_LAYER_USER_ID foreign key (CREATE_USER_ID) references USER_INFO (USER_ID) disable
);
COMMENT ON COLUMN CUSTOMISED_LAYER.LAYER_ID IS '重点区块ID';
COMMENT ON COLUMN CUSTOMISED_LAYER.LAYER_NAME IS '重点区块名称';
COMMENT ON COLUMN CUSTOMISED_LAYER.GEOMETRY IS '几何';
COMMENT ON COLUMN CUSTOMISED_LAYER.CREATE_USER_ID IS '创建人';
COMMENT ON COLUMN CUSTOMISED_LAYER.CREATE_DATE IS '创建时间';
COMMENT ON COLUMN CUSTOMISED_LAYER.STATUS IS '重点区块状态，0删除，1开启';


CREATE TABLE TASK  (
	TASK_ID NUMBER(10) not null,
	CITY_ID NUMBER(10) not null,	
	CREATE_USER_ID NUMBER(10),
	CREATE_DATE TIMESTAMP,
	STATUS NUMBER(1) default 1 not null,
  NAME VARCHAR2(200),
	DESCP VARCHAR2(200),
	PLAN_START_DATE TIMESTAMP,
	PLAN_END_DATE TIMESTAMP,
  MONTH_EDIT_PLAN_START_DATE TIMESTAMP,
  MONTH_EDIT_PLAN_END_DATE TIMESTAMP,
  MONTH_EDIT_GROUP_ID NUMBER(10),
	LATEST NUMBER(1) default 1 not null,
	constraint PK_TASK_ID primary key (TASK_ID),
	constraint FK_TASK_CITY_ID foreign key (CITY_ID) references CITY (CITY_ID) disable, 
	constraint FK_TASK_USER_ID foreign key (CREATE_USER_ID) references USER_INFO (USER_ID) disable
);
COMMENT ON TABLE TASK IS 'TASK表';
COMMENT ON COLUMN TASK.TASK_ID IS '任务';
COMMENT ON COLUMN TASK.CITY_ID IS '城市ID';
COMMENT ON COLUMN TASK.CREATE_USER_ID IS '创建人';
COMMENT ON COLUMN TASK.CREATE_DATE IS '创建时间';
COMMENT ON COLUMN TASK.STATUS IS 'TASK状态:0关闭，1开启，2草稿';
COMMENT ON COLUMN TASK.NAME IS '任务名称';
COMMENT ON COLUMN TASK.DESCP IS '描述';
COMMENT ON COLUMN TASK.PLAN_START_DATE IS '计划开始时间';
COMMENT ON COLUMN TASK.PLAN_END_DATE IS '计划结束时间';
COMMENT ON COLUMN TASK.MONTH_EDIT_PLAN_START_DATE IS '月编区域作业计划结束时间';
COMMENT ON COLUMN TASK.MONTH_EDIT_PLAN_END_DATE IS '月编区域作业计划结束时间';
COMMENT ON COLUMN TASK.LATEST IS '是否当前city关联最新任务';

CREATE TABLE SUBTASK  (
  SUBTASK_ID NUMBER(10) not null, 
  BLOCK_ID NUMBER(10),  
  TASK_ID NUMBER(10),
  NAME VARCHAR2(200),
  GEOMETRY SDO_GEOMETRY,
  STAGE NUMBER(1) default 0 not null, 
  TYPE NUMBER(1) default 0 not null,  
  CREATE_USER_ID NUMBER(10),
  CREATE_DATE TIMESTAMP,  
  EXE_USER_ID NUMBER(10),
  STATUS NUMBER(1) default 1 not null,    
  PLAN_START_DATE TIMESTAMP,
  PLAN_END_DATE TIMESTAMP,
  DESCP VARCHAR2(200),
  constraint PK_SUBTASK_ID primary key (SUBTASK_ID),
  constraint FK_USER_ID_1 foreign key (CREATE_USER_ID) references USER_INFO (USER_ID) disable,
  constraint FK_USER_ID_2 foreign key (EXE_USER_ID) references USER_INFO (USER_ID) disable,
  constraint FK_SUBTASK_BLOCK_ID foreign key (BLOCK_ID) references BLOCK (BLOCK_ID) disable,
  constraint FK_SUBTASK_TASK_ID foreign key (TASK_ID) references TASK (TASK_ID) disable
);
COMMENT ON COLUMN SUBTASK.SUBTASK_ID IS '子任务 ID';
COMMENT ON COLUMN SUBTASK.BLOCK_ID IS 'BLOCK ID';
COMMENT ON COLUMN SUBTASK.TASK_ID IS '任务ID';
COMMENT ON COLUMN SUBTASK.NAME IS '任务名称';
COMMENT ON COLUMN SUBTASK.GEOMETRY IS '几何';
COMMENT ON COLUMN SUBTASK.STAGE IS '作业阶段.0采集，1日编，2月编';
COMMENT ON COLUMN SUBTASK.TYPE IS '作业类型.0POI，1道路，2一体化，3专项作业';
COMMENT ON COLUMN SUBTASK.CREATE_USER_ID IS '创建人';
COMMENT ON COLUMN SUBTASK.CREATE_DATE IS '创建时间';
COMMENT ON COLUMN SUBTASK.EXE_USER_ID IS '执行人';
COMMENT ON COLUMN SUBTASK.STATUS IS '任务状态.0关闭、1开启';
COMMENT ON COLUMN SUBTASK.PLAN_START_DATE IS '计划开始时间';
COMMENT ON COLUMN SUBTASK.PLAN_END_DATE IS '计划结束时间';
COMMENT ON COLUMN SUBTASK.DESCP IS '描述';

CREATE SEQUENCE CITY_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE BLOCK_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE BLOCK_MAN_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE CUSTOMISED_LAYER_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE TASK_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE SUBTASK_SEQ START WITH 1 MAXVALUE 9999999999;

create table GRID
(
  grid_id          NUMBER(10) not null,
  region_id        NUMBER(10),
  city_id          NUMBER(10),
  block_id         NUMBER(10)
);
comment on column GRID.grid_id
  is '网格ID';
comment on column GRID.region_id
  is '大区ID';
comment on column GRID.city_id
  is '城市ID';
comment on column GRID.block_id
  is 'BLOCK ID';
alter table GRID
  add constraint PK_GRID primary key (GRID_ID);
  
create table GRID_LOCK
(
  grid_id     NUMBER(10) not null,
  region_id   NUMBER(10),
  handle_region_id   NUMBER(10),
  lock_object NUMBER(1) not null,
  lock_status NUMBER(1) default 0 not null,
  lock_type   NUMBER(2),
  lock_seq    NUMBER(10),
  job_id      NUMBER(10),
  lock_time   TIMESTAMP(6)
);
comment on column GRID_LOCK.grid_id
  is '网格ID';
comment on column GRID_LOCK.lock_object
  is '锁定要素,1POI；2道路';
comment on column GRID_LOCK.lock_status
  is '锁定状态,0未锁定，1已锁定';
comment on column GRID_LOCK.lock_type
  is '锁定类型,1检查，2借出，3归还，4批处理';
comment on column GRID_LOCK.lock_seq
  is '锁定批次号,一次锁申请一个批次号';
comment on column GRID_LOCK.job_id
  is '当前锁来自的JOB号';
comment on column GRID_LOCK.lock_time
  is '锁定时间';
alter table GRID_LOCK
  add constraint PK_GRID_LOCK primary key (GRID_ID, LOCK_OBJECT);
  
create table GRID_LOCK_DAY
(
  grid_id     NUMBER(10) not null,
  region_id   NUMBER(10),
  handle_region_id   NUMBER(10),
  lock_object NUMBER(1) not null,
  lock_status NUMBER(1) default 0 not null,
  lock_type   NUMBER(2),
  lock_seq    NUMBER(10),
  job_id      NUMBER(10),
  lock_time   TIMESTAMP(6)
);
comment on column GRID_LOCK_DAY.grid_id
  is '网格ID';
comment on column GRID_LOCK_DAY.lock_object
  is '锁定要素,1POI；2道路';
comment on column GRID_LOCK_DAY.lock_status
  is '锁定状态,0未锁定，1已锁定';
comment on column GRID_LOCK_DAY.lock_type
  is '锁定类型,1检查，2借出，3归还，4批处理';
comment on column GRID_LOCK_DAY.lock_seq
  is '锁定批次号,一次锁申请一个批次号';
comment on column GRID_LOCK_DAY.job_id
  is '当前锁来自的JOB号';
comment on column GRID_LOCK_DAY.lock_time
  is '锁定时间';
alter table GRID_LOCK_DAY
  add constraint PK_GRID_LOCK_DAY primary key (GRID_ID, LOCK_OBJECT);

create table GRID_LOCK_MONTH
(
  grid_id     NUMBER(10) not null,
  region_id   NUMBER(10),
  handle_region_id   NUMBER(10),
  lock_object NUMBER(1) not null,
  lock_status NUMBER(1) default 0 not null,
  lock_type   NUMBER(2),
  lock_seq    NUMBER(10),
  job_id      NUMBER(10),
  lock_time   TIMESTAMP(6)
);
comment on column GRID_LOCK_MONTH.grid_id
  is '网格ID';
comment on column GRID_LOCK_MONTH.lock_object
  is '锁定要素,1POI；2道路';
comment on column GRID_LOCK_MONTH.lock_status
  is '锁定状态,0未锁定，1已锁定';
comment on column GRID_LOCK_MONTH.lock_type
  is '锁定类型,1检查，2借出，3归还，4批处理';
comment on column GRID_LOCK_MONTH.lock_seq
  is '锁定批次号,一次锁申请一个批次号';
comment on column GRID_LOCK_MONTH.job_id
  is '当前锁来自的JOB号';
comment on column GRID_LOCK_MONTH.lock_time
  is '锁定时间';
alter table GRID_LOCK_MONTH
  add constraint PK_GRID_LOCK_MONTH primary key (GRID_ID, LOCK_OBJECT);  
  
create table INFOR_BLOCK_MAPPING
(
  infor_id VARCHAR2(32) not null,
  block_id NUMBER(10) not null
);
comment on column INFOR_BLOCK_MAPPING.infor_id
  is '情报ID';
comment on column INFOR_BLOCK_MAPPING.block_id
  is 'BLOCK ID';
alter table INFOR_BLOCK_MAPPING
  add constraint BLOCK_ID foreign key (BLOCK_ID)
  references BLOCK (BLOCK_ID);
  
create table SUBTASK_GRID_MAPPING
(
  subtask_id NUMBER(10) not null,
  grid_id    NUMBER(10) not null
);
comment on column SUBTASK_GRID_MAPPING.subtask_id
  is '子任务 ID';
comment on column SUBTASK_GRID_MAPPING.grid_id
  is '网格ID';
alter table SUBTASK_GRID_MAPPING
  add constraint SUBTASKGRID_GRIDID_FOREIGN foreign key (GRID_ID)
  references GRID (GRID_ID);
  
 
CREATE SEQUENCE GRID_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE USER_INFO_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE GRID_LOCK_SEQ START WITH 1 MAXVALUE 9999999999;

-- Create table
create table USER_GROUP
(
  GROUP_ID   NUMBER(10) not null,
  GROUP_NAME VARCHAR2(50),
  GROUP_TYPE NUMBER(2),
  LEADER_ID  NUMBER(10)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255;
-- Add comments to the table 
comment on table USER_GROUP
  is '用户组';
-- Add comments to the columns 
comment on column USER_GROUP.GROUP_ID
  is '用户组ID';
comment on column USER_GROUP.GROUP_NAME
  is '用户组名称';
comment on column USER_GROUP.GROUP_TYPE
  is '用户组类型';
comment on column USER_GROUP.LEADER_ID
  is '队长ID';
-- Create/Recreate primary, unique and foreign key constraints 
alter table USER_GROUP
  add constraint P_GROUP_ID primary key (GROUP_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255;

create table GROUP_USER_MAPPING
(
  GROUP_ID NUMBER(10) not null,
  USER_ID  NUMBER(10) not null
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255;
-- Add comments to the table 
comment on table GROUP_USER_MAPPING
  is '用户组和用户关系表';
-- Add comments to the columns 
comment on column GROUP_USER_MAPPING.GROUP_ID
  is '用户组ID';
comment on column GROUP_USER_MAPPING.USER_ID
  is '用户ID';
 
 
create table USER_UPLOAD
(
  USER_ID          number(10) not null,
  DEVICE_ID        number(10),
  UPLOAD_POI_TIME  timestamp,
  UPLOAD_TIPS_TIME timestamp
)
;
-- Add comments to the table 
comment on table USER_UPLOAD
  is '用户app上传记录表';
-- Add comments to the columns 
comment on column USER_UPLOAD.USER_ID
  is '用户组ID';
comment on column USER_UPLOAD.DEVICE_ID
  is '设备ID';
comment on column USER_UPLOAD.UPLOAD_POI_TIME
  is '上传POI时间';
comment on column USER_UPLOAD.UPLOAD_TIPS_TIME
  is '上传TIPS时间';
  
-- Create table
create table USER_DEVICE
(
  device_id                 NUMBER(10) not null,
  user_id                   NUMBER(10),
  device_token              VARCHAR2(32),
  device_platform           VARCHAR2(20),
  device_version            VARCHAR2(20),
  device_model              VARCHAR2(20),
  device_system_version     VARCHAR2(20),
  device_descendant_version VARCHAR2(20),
  status                    NUMBER(1) default 1
);
-- Add comments to the table 
comment on table USER_DEVICE
  is '用户app设备记录表';
-- Add comments to the columns 
comment on column USER_DEVICE.device_id
  is '设备ID';
comment on column USER_DEVICE.user_id
  is '用户ID';
comment on column USER_DEVICE.device_token
  is '设备token';
comment on column USER_DEVICE.device_platform
  is '设备平台';
comment on column USER_DEVICE.device_version
  is '设备型号';
comment on column USER_DEVICE.device_model
  is '机型';
comment on column USER_DEVICE.device_system_version
  is '系统版本';
comment on column USER_DEVICE.device_descendant_version
  is '衍生版本';
comment on column USER_DEVICE.status
  is '设备是否登录。1：是，0：否';
-- Create/Recreate primary, unique and foreign key constraints 
alter table USER_DEVICE
  add constraint P_USER_DEVICE primary key (DEVICE_ID)
 
create table ROLE
(
  ROLE_ID   number(10) not null,
  ROLE_NAME varchar2(20)
)
;
-- Add comments to the table 
comment on table ROLE
  is '角色表';
-- Add comments to the columns 
comment on column ROLE.ROLE_ID
  is '角色ID';
comment on column ROLE.ROLE_NAME
  is '角色名';
-- Create/Recreate primary, unique and foreign key constraints 
alter table ROLE
  add constraint P_ROLE primary key (ROLE_ID);

  
create table ROLE_USER_MAPPING
(
  ROLE_ID number(10) not null,
  USER_ID number(10) not null
)
;
-- Add comments to the table 
comment on table ROLE_USER_MAPPING
  is '角色与用户关联表';
-- Add comments to the columns 
comment on column ROLE_USER_MAPPING.ROLE_ID
  is '角色ID';
comment on column ROLE_USER_MAPPING.USER_ID
  is '用户ID';
  

create table ROLE_MODULE_MAPPING
(
  ROLE_ID   NUMBER(10) not null,
  MODULE_ID NUMBER(10) not null
);
-- Add comments to the table 
comment on table ROLE_MODULE_MAPPING
  is '角色与模块关联表';
-- Add comments to the columns 
comment on column ROLE_MODULE_MAPPING.ROLE_ID
  is '角色ID';
comment on column ROLE_MODULE_MAPPING.MODULE_ID
  is '模块ID';
  
  
create table MODULE
(
  MODULE_ID   number(10) not null,
  MODULE_NAME varchar2(20)
)
;
-- Add comments to the table 
comment on table MODULE
  is '平台模块表';
-- Add comments to the columns 
comment on column MODULE.MODULE_ID
  is '模块ID';
comment on column MODULE.MODULE_NAME
  is '模块名';
  
CREATE SEQUENCE USER_GROUP_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE USER_DEVICE_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE MODULE_SEQ START WITH 1 MAXVALUE 9999999999;
CREATE SEQUENCE ROLE_SEQ START WITH 1 MAXVALUE 9999999999;


INSERT INTO GRID(GRID_ID)
  SELECT   TO_NUMBER(MESH || '0' || LEVEL)
    FROM (SELECT DISTINCT TO_NUMBER( MESH) MESH FROM CP_MESHLIST@metadb_link WHERE SCALE='2.5')
  CONNECT BY PRIOR DBMS_RANDOM.VALUE IS NOT NULL
         AND PRIOR MESH = MESH
         AND LEVEL <= 3;

INSERT INTO GRID(GRID_ID)
  SELECT   TO_NUMBER(MESH || '1' || LEVEL)
    FROM (SELECT DISTINCT TO_NUMBER( MESH) MESH FROM CP_MESHLIST@metadb_link WHERE SCALE='2.5')
  CONNECT BY PRIOR DBMS_RANDOM.VALUE IS NOT NULL
         AND PRIOR MESH = MESH
         AND LEVEL <= 3;

INSERT INTO GRID(GRID_ID)
  SELECT   TO_NUMBER(MESH || '2' || LEVEL)
    FROM (SELECT DISTINCT TO_NUMBER( MESH) MESH FROM CP_MESHLIST@metadb_link WHERE SCALE='2.5')
  CONNECT BY PRIOR DBMS_RANDOM.VALUE IS NOT NULL
         AND PRIOR MESH = MESH
         AND LEVEL <= 3;

INSERT INTO GRID(GRID_ID)
  SELECT   TO_NUMBER(MESH || '3' || LEVEL)
    FROM (SELECT DISTINCT TO_NUMBER( MESH) MESH FROM CP_MESHLIST@metadb_link WHERE SCALE='2.5')
  CONNECT BY PRIOR DBMS_RANDOM.VALUE IS NOT NULL
         AND PRIOR MESH = MESH
         AND LEVEL <= 3;

INSERT INTO GRID(GRID_ID)
  SELECT   TO_NUMBER(MESH || LEVEL || '0')
    FROM (SELECT DISTINCT TO_NUMBER( MESH) MESH FROM CP_MESHLIST@metadb_link WHERE SCALE='2.5')
  CONNECT BY PRIOR DBMS_RANDOM.VALUE IS NOT NULL
         AND PRIOR MESH = MESH
         AND LEVEL <= 3;

INSERT INTO GRID(GRID_ID)
  SELECT   TO_NUMBER(MESH || '00')
    FROM (SELECT DISTINCT TO_NUMBER( MESH) MESH FROM CP_MESHLIST@metadb_link WHERE SCALE='2.5');


INSERT INTO GRID_LOCK_DAY (GRID_ID,LOCK_OBJECT,LOCK_STATUS) SELECT GRID_ID,1,0 FROM GRID;
INSERT INTO GRID_LOCK_DAY (GRID_ID,LOCK_OBJECT,LOCK_STATUS) SELECT GRID_ID,2,0 FROM GRID;
INSERT INTO GRID_LOCK_MONTH (GRID_ID,LOCK_OBJECT,LOCK_STATUS) SELECT GRID_ID,1,0 FROM GRID;
INSERT INTO GRID_LOCK_MONTH (GRID_ID,LOCK_OBJECT,LOCK_STATUS) SELECT GRID_ID,2,0 FROM GRID;

INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('BLOCK',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_BLOCK ON BLOCK(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE BLOCK COMPUTE STATISTICS;


INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('CITY',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_CITY ON CITY(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE CITY COMPUTE STATISTICS;

INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('CUSTOMISED_LAYER',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_CUSTOMISED_LAYER ON CUSTOMISED_LAYER(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE CUSTOMISED_LAYER COMPUTE STATISTICS;

INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('SUBTASK',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_SUBTASK ON SUBTASK(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE SUBTASK COMPUTE STATISTICS;

-- Alter table 
alter table TASK
  storage
  (
    next 1
  )
;
-- Add/modify columns 
alter table TASK add MONTH_PRODUCE_PLAN_START_DATE TIMESTAMP(6);
alter table TASK add MONTH_PRODUCE_PLAN_END_DATE TIMESTAMP(6);
alter table TASK add TASK_TYPE NUMBER(1);
-- Add comments to the columns 
comment on column TASK.MONTH_PRODUCE_PLAN_START_DATE
  is '月出计划开始时间';
comment on column TASK.MONTH_PRODUCE_PLAN_END_DATE
  is '月出计划结束时间';
comment on column TASK.TASK_TYPE
  is '任务类型:1常规，2多源，3代理店，4情报';
  
-- Alter table 
alter table SUBTASK
  storage
  (
    next 1
  )
;
-- Add comments to the columns 
comment on column SUBTASK.TYPE
  is '作业类型：0POI，1道路，2一体化，3一体化_grid粗编，4一体化_区域粗编，5多源POI，6
代理店， 7POI专项,8道路_grid精编，9道路_grid粗编，10道路区域专项';

DROP TABLE INFOR_MAN;

-- Create table
create table INFOR
(
  INFOR_ID     varchar2(50) not null,
  INFOR_NAME   VARCHAR2 (200),
  GEOMETRY     clob not null,
  INFOR_LEVEL  NUMBER(1),
  INFOR_STATUS NUMBER(1),
  INFOR_CONTENT VARCHAR2(200),
  TASK_ID      NUMBER(10),
  INSERT_TIME  timestamp
)
;
-- Add comments to the columns 
comment on column INFOR.INFOR_ID
  is '情报ID';
comment on column INFOR.INFOR_NAME
  is '情报名称';
comment on column INFOR.GEOMETRY
  is '几何';
comment on column INFOR.INFOR_LEVEL
  is '情报级别';
comment on column INFOR.INFOR_STATUS
  is '情报规划状态:0未规划，1已规划，2已关闭';
comment on column INFOR.INFOR_CONTENT
  is '情报内容描述';
comment on column INFOR.TASK_ID
  is '任务id';
comment on column INFOR.INSERT_TIME
  is '情报插入时间';
  
-- Create table
create table MESSAGE
(
  MSG_ID       NUMBER(10) not null,
  MSG_TITLE    VARCHAR2(50),
  MSG_CONTENT  VARCHAR2(200),
  PUSH_USER    NUMBER(10),
  MSG_RECERVER NUMBER(10),
  PUSH_TIME    timestamp,
  MSG_STATUS   NUMBER(1) default 0
)
;
-- Add comments to the columns 
comment on column MESSAGE.MSG_ID
  is '消息id';
comment on column MESSAGE.MSG_TITLE
  is '消息主题';
comment on column MESSAGE.MSG_CONTENT
  is '消息内容';
comment on column MESSAGE.PUSH_USER
  is '推送人';
comment on column MESSAGE.MSG_RECERVER
  is '接收人';
comment on column MESSAGE.PUSH_TIME
  is '推送时间';
comment on column MESSAGE.MSG_STATUS
  is '消息状态';
  
alter table INFOR modify INSERT_TIME default sysdate;
-- Add/modify columns 
alter table INFOR rename column INFOR_STATUS to PLAN_STATUS;
  
-- Create sequence 
create sequence MESSAGE_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

DROP TABLE infor_block_mapping;

-- Create table
create TABLE subtask_finish
(
  subtask_id     number(10) not null,
  finish_percent number(2)
);

-- Add comments to the columns 
comment on column SUBTASK_FINISH.SUBTASK_ID
  is '子任务id';
comment on column SUBTASK_FINISH.FINISH_PERCENT
  is '完成度（刷grid脚本，将子任务完成度一并刷）';
  
alter table BLOCK_MAN add block_man_name varchar2(200);

-- Create table
create table BLOCK_GRID_MAPPING
(
  grid_id  NUMBER(10),
  block_id NUMBER(10)
);
-- Add comments to the columns 
comment on column BLOCK_GRID_MAPPING.grid_id
  is 'grid id';
comment on column BLOCK_GRID_MAPPING.block_id
  is 'block id';

-- Create table
create table INFOR_GRID_MAPPING
(
  infor_id VARCHAR2(50) not null,
  grid_id  NUMBER(10) not null
);
-- Add comments to the columns 
comment on column INFOR_GRID_MAPPING.infor_id
  is '情报id';
comment on column INFOR_GRID_MAPPING.grid_id
  is 'grid id';
-- Create/Recreate indexes 
create index IDX_INFOR_GRID_MAP_1 on INFOR_GRID_MAPPING (INFOR_ID);
create index IDX_INFOR_GRID_MAP_2 on INFOR_GRID_MAPPING (GRID_ID);

  
alter table BLOCK add(REGION_ID NUMBER(10));
comment on column BLOCK.REGION_ID is '大区id';

alter table BLOCK_MAN add(TASK_ID NUMBER(10));
comment on column BLOCK_MAN.TASK_ID is '任务id';
comment on column BLOCK_MAN.STATUS
  is 'BLOCK状态:0关闭，1开启，2 草稿';

alter table SUBTASK add(EXE_GROUP_ID NUMBER(10));
comment on column SUBTASK.EXE_GROUP_ID is '作业组ID';
comment on column SUBTASK.TYPE is '0POI，1道路，2一体化，3一体化_grid粗编，4一体化_区域粗编，5多源POI，6代理店， 7POI专项,8道路_grid精编，9道路_grid粗编，10道路区域专项';

alter table USER_GROUP add(PARENT_GROUP_ID NUMBER(10));
comment on column USER_GROUP.PARENT_GROUP_ID is '父用户组ID';


alter table SUBTASK add(BLOCK_MAN_ID NUMBER(10));
comment on column SUBTASK.BLOCK_MAN_ID is '子任务所在block_manID';

-- Create table
create table FM_STAT_OVERVIEW_BLOCKMAN
(
  block_man_id      NUMBER(10),
  percent           NUMBER(10),
  diff_date         NUMBER(10),
  progress          NUMBER(1),
  collect_progress  NUMBER(1),
  collect_percent   NUMBER(10),
  collect_diff_date NUMBER(10),
  daily_progress    NUMBER(1),
  daily_percent     NUMBER(10),
  daily_diff_date   NUMBER(10),
  stat_date         VARCHAR2(200)
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
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_BLOCKMAN.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.stat_date
  is '统计时间';

-- Create table
create table FM_STAT_OVERVIEW_SUBTASK
(
  subtask_id NUMBER(10),
  percent    NUMBER(10),
  diff_date  NUMBER(10),
  progress   NUMBER(1),
  stat_date  VARCHAR2(200)
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
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_SUBTASK.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_SUBTASK.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_SUBTASK.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_SUBTASK.stat_date
  is '统计时间';

-- Create table
create table FM_STAT_OVERVIEW_TASK
(
  task_id          NUMBER(10),
  percent          NUMBER(10),
  diff_date        NUMBER(10),
  progress         NUMBER(1),
  collect_progress NUMBER(1),
  collect_percent  NUMBER(10),
  daily_progress   NUMBER(1),
  daily_percent    NUMBER(10),
  stat_date        VARCHAR2(200),
  monthly_progress NUMBER(1),
  monthly_percent  NUMBER(10)
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
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_TASK.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_TASK.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_TASK.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_TASK.stat_date
  is '统计时间';


alter table BLOCK add work_property VARCHAR2(200);
-- Add comments to the columns 
comment on column BLOCK.work_property
  is '作业性质';
  
alter table BLOCK_MAN add road_plan_total NUMBER(10);
alter table BLOCK_MAN add poi_plan_total NUMBER(10);
-- Add comments to the columns 
comment on column BLOCK_MAN.road_plan_total
  is '计划道路作业总量';
comment on column BLOCK_MAN.poi_plan_total
  is '计划POI作业总量';
 
 
 
--drop table 
drop table FM_STAT_OVERVIEW_BLOCKMAN;
-- Create table
create table FM_STAT_OVERVIEW_BLOCKMAN
(
  block_man_id              NUMBER(10),
  task_id                   NUMBER(10),
  progress                  NUMBER(1),
  percent                   NUMBER(10),
  status                    NUMBER(1),
  plan_start_date           TIMESTAMP(6),
  plan_end_date             TIMESTAMP(6),
  diff_date                 NUMBER(10),
  poi_plan_total            NUMBER(10),
  road_plan_total           NUMBER(10),
  collect_progress          NUMBER(1),
  collect_percent           NUMBER(10),
  collect_plan_start_date   TIMESTAMP(6),
  collect_plan_end_date     TIMESTAMP(6),
  collect_plan_date         NUMBER(4),
  collect_actual_start_date TIMESTAMP(6),
  collect_actual_end_date   TIMESTAMP(6),
  collect_diff_date         NUMBER(10),
  daily_progress            NUMBER(1),
  daily_percent             NUMBER(10),
  daily_plan_start_date     TIMESTAMP(6),
  daily_plan_end_date       TIMESTAMP(6),
  daily_plan_date           NUMBER,
  daily_actual_start_date   TIMESTAMP(6),
  daily_actual_end_date     TIMESTAMP(6),
  daily_diff_date           NUMBER(10),
  stat_date                 VARCHAR2(200),
  stat_time                 TIMESTAMP(6)
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
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_BLOCKMAN.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.status
  is '0关闭，1开启,2草稿';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.poi_plan_total
  is '预估作业量_POI';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.road_plan_total
  is '预估作业量_道路';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.collect_plan_date
  is '采集计划天数';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.collect_actual_start_date
  is '采集实际开始时间';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.collect_actual_end_date
  is '采集实际结束时间';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.daily_plan_start_date
  is '日编计划开始时间';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.daily_plan_end_date
  is '日编计划结束时间';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.daily_plan_date
  is '日编计划天数';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.daily_actual_start_date
  is '日编实际开始时间';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.stat_date
  is '统计时间';
comment on column FM_STAT_OVERVIEW_BLOCKMAN.stat_time
  is '统计详细时间';

  
-- Create table
create table FM_STAT_OVERVIEW_GROUP
(
  group_id          NUMBER(10),
  percent           NUMBER(5),
  plan_start_date   TIMESTAMP(6),
  plan_end_date     TIMESTAMP(6),
  plan_date         NUMBER(5),
  actual_start_date TIMESTAMP(6),
  actual_end_date   TIMESTAMP(6),
  diff_date         NUMBER(5),
  stage             NUMBER(1),
  poi_plan_total    NUMBER(10),
  road_plan_total   NUMBER(10),
  stat_date         TIMESTAMP(6),
  stat_time         TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_GROUP.percent
  is '进度百分比';
comment on column FM_STAT_OVERVIEW_GROUP.plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW_GROUP.diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW_GROUP.stage
  is '0采集  1日编';
comment on column FM_STAT_OVERVIEW_GROUP.poi_plan_total
  is 'block_man记录的poi计划量汇总';
comment on column FM_STAT_OVERVIEW_GROUP.road_plan_total
  is 'block_man记录的road计划量汇总';
comment on column FM_STAT_OVERVIEW_GROUP.stat_date
  is '统计日期';
comment on column FM_STAT_OVERVIEW_GROUP.stat_time
  is '统计详细时间';
 
--drop table 
drop table FM_STAT_OVERVIEW_SUBTASK;
-- Create table
create table FM_STAT_OVERVIEW_SUBTASK
(
  subtask_id           NUMBER(10),
  percent              NUMBER(10),
  diff_date            NUMBER(10),
  progress             NUMBER(1),
  stat_date            VARCHAR2(8),
  status               NUMBER(1),
  total_poi            NUMBER(10),
  finished_poi         NUMBER(10),
  total_road           NUMBER(10),
  finished_road        NUMBER(10),
  percent_poi          NUMBER(10),
  percent_road         NUMBER(10),
  plan_start_date      TIMESTAMP(6) not null,
  plan_end_date        TIMESTAMP(6),
  actual_start_date    TIMESTAMP(6) not null,
  actual_end_date      TIMESTAMP(6),
  stat_time            VARCHAR2(14),
  grid_percent_details CLOB,
  block_man_id         NUMBER(10)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_SUBTASK.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_SUBTASK.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_SUBTASK.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_SUBTASK.stat_date
  is '统计日期';
comment on column FM_STAT_OVERVIEW_SUBTASK.status
  is '0关闭，1开启,2草稿';
comment on column FM_STAT_OVERVIEW_SUBTASK.total_poi
  is 'POI总量';
comment on column FM_STAT_OVERVIEW_SUBTASK.finished_poi
  is 'POI完成量';
comment on column FM_STAT_OVERVIEW_SUBTASK.total_road
  is 'ROAD总量';
comment on column FM_STAT_OVERVIEW_SUBTASK.finished_road
  is 'ROAD完成量';
comment on column FM_STAT_OVERVIEW_SUBTASK.percent_poi
  is 'POI完成百分比';
comment on column FM_STAT_OVERVIEW_SUBTASK.percent_road
  is 'ROAD完成百分比';
comment on column FM_STAT_OVERVIEW_SUBTASK.plan_start_date
  is '几乎开始时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.plan_end_date
  is '计划结束时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.actual_start_date
  is '实际开始时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.actual_end_date
  is '实际结束时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.stat_time
  is '统计时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.grid_percent_details
  is 'grid进度详情';
comment on column FM_STAT_OVERVIEW_SUBTASK.block_man_id
  is 'BLCOK_MAN_ID';
  
--drop table 
drop table FM_STAT_OVERVIEW_TASK;
-- Create table
create table FM_STAT_OVERVIEW_TASK
(
  task_id                   NUMBER(10),
  percent                   NUMBER(10),
  diff_date                 NUMBER(10),
  progress                  NUMBER(1),
  collect_progress          NUMBER(1),
  collect_percent           NUMBER(10),
  daily_progress            NUMBER(1),
  daily_percent             NUMBER(10),
  stat_date                 TIMESTAMP(6),
  monthly_progress          NUMBER(1),
  monthly_percent           NUMBER(10),
  plan_start_date           TIMESTAMP(6),
  plan_end_date             TIMESTAMP(6),
  plan_date                 NUMBER(10),
  actual_start_date         TIMESTAMP(6),
  actual_end_date           TIMESTAMP(6),
  collect_plan_start_date   TIMESTAMP(6),
  collect_plan_end_date     TIMESTAMP(6),
  collect_plan_date         NUMBER(10),
  collect_actual_start_date TIMESTAMP(6),
  collect_actual_end_date   TIMESTAMP(6),
  collect_diff_date         NUMBER(10),
  daily_plan_start_date     TIMESTAMP(6),
  daily_plan_end_date       TIMESTAMP(6),
  daily_plan_date           NUMBER(10),
  daily_actual_start_date   TIMESTAMP(6),
  daily_actual_end_date     TIMESTAMP(6),
  daily_diff_date           NUMBER(10),
  stat_time                 TIMESTAMP(6),
  poi_plan_total            NUMBER(10),
  road_plan_total           NUMBER(10)
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
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_TASK.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_TASK.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_TASK.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_TASK.daily_progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期)';
comment on column FM_STAT_OVERVIEW_TASK.stat_date
  is '统计时间';
comment on column FM_STAT_OVERVIEW_TASK.collect_plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW_TASK.collect_diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW_TASK.daily_plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW_TASK.daily_diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW_TASK.stat_time
  is '统计详细时间';
comment on column FM_STAT_OVERVIEW_TASK.poi_plan_total
  is '预估作业量_POI';
comment on column FM_STAT_OVERVIEW_TASK.road_plan_total
  is '预估作业量_道路';
  
-- Create table
create table FM_STAT_OVERVIEW
(
  collect_percent           NUMBER(10),
  collect_plan_start_date   TIMESTAMP(6),
  collect_plan_end_date     TIMESTAMP(6),
  collect_plan_date         NUMBER(10),
  collect_actual_start_date TIMESTAMP(6),
  collect_actual_end_date   TIMESTAMP(6),
  collect_diff_date         NUMBER(10),
  daily_percent             NUMBER(10),
  daily_plan_start_date     TIMESTAMP(6),
  daily_plan_end_date       TIMESTAMP(6),
  daily_plan_date           NUMBER(10),
  daily_actual_start_date   TIMESTAMP(6),
  daily_actual_end_date     TIMESTAMP(6),
  daily_diff_date           NUMBER(10),
  poi_plan_total            NUMBER(10),
  road_plan_total           NUMBER(10),
  stat_date                 TIMESTAMP(6),
  stat_time                 TIMESTAMP(6)
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
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW.collect_percent
  is '进度百分比 , 所有block采集完成度取平均值';
comment on column FM_STAT_OVERVIEW.collect_plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW.collect_diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW.daily_percent
  is '进度百分比,所有block日编完成度取平均值';
comment on column FM_STAT_OVERVIEW.daily_plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW.daily_diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW.poi_plan_total
  is 'block_man记录的poi计划量汇总';
comment on column FM_STAT_OVERVIEW.road_plan_total
  is 'block_man记录的road计划量汇总';
comment on column FM_STAT_OVERVIEW.stat_date
  is '统计日期';
comment on column FM_STAT_OVERVIEW.stat_time
  is '统计详细时间';

alter table FM_STAT_OVERVIEW_BLOCKMAN add collect_group_id NUMBER(10);
alter table FM_STAT_OVERVIEW_BLOCKMAN add daily_group_id NUMBER(10);

create table PRODUCE
(
  produce_id     NUMBER(10),
  produce_name   VARCHAR2(100),
  produce_type   VARCHAR2(50),
  create_user_id NUMBER(10),
  create_date    TIMESTAMP(6),
  produce_status NUMBER(2),
  parameter      CLOB
);
-- Add comments to the columns 
comment on column PRODUCE.produce_type
  is 'POI,ALL(一体化)';
comment on column PRODUCE.produce_status
  is '0创建 1执行中 2完成';
comment on column PRODUCE.parameter
  is '参数 {gridIds:[123,345]}';
  
CREATE SEQUENCE PRODUCE_SEQ START WITH 1 MAXVALUE 9999999999;

alter table PRODUCE add subtask_id NUMBER(10);
drop table subtask_finish;
alter table SUBTASK add quality_subtask_id NUMBER(10);
alter table SUBTASK add is_quality NUMBER(1);
-- Add comments to the columns 
comment on column SUBTASK.quality_subtask_id
  is '质检子任务id';
comment on column SUBTASK.is_quality
  is '0非质检子任务 1质检子任务';

--删除无用字段，block_man_id替代
alter table SUBTASK drop column block_id;
alter table SUBTASK add refer_geometry SDO_GEOMETRY;
comment on column SUBTASK. refer_geometry
  is '参考不规则任务圈';


create table APPLICATION
(
  apply_id       NUMBER(10) not null,
  apply_title    VARCHAR2(50),
  apply_type     NUMBER(1),
  apply_status   NUMBER(1),
  severity       NUMBER(10),
  operate_time   TIMESTAMP(6),
  operator       NUMBER(10),
  audit_role_id  NUMBER(10),
  auditor        NUMBER(10),
  create_time    TIMESTAMP(6),
  apply_group_id NUMBER(10),
  apply_user_id  NUMBER(10),
  delete_flag    NUMBER(1)
);
-- Add comments to the columns 
comment on column APPLICATION.apply_id
  is '申请id';
comment on column APPLICATION.apply_title
  is '申请内容';
comment on column APPLICATION.apply_type
  is '申请类型 1作业申请2计划变更';
comment on column APPLICATION.apply_status
  is '申请状态 1保存，2提交，3通过，4不通过';
comment on column APPLICATION.severity
  is '优先级 1高2中3低';
comment on column APPLICATION.operate_time
  is '处理时间';
comment on column APPLICATION.operator
  is '处理人';
comment on column APPLICATION.audit_role_id
  is '审核人角色';
comment on column APPLICATION.auditor
  is '审核人';
comment on column APPLICATION.create_time
  is '创建时间';
comment on column APPLICATION.apply_group_id
  is '申请人组';
comment on column APPLICATION.apply_user_id
  is '申请人';
comment on column APPLICATION.delete_flag
  is '删除状态 0新增，1删除';
-- Create/Recreate primary, unique and foreign key constraints 
alter table APPLICATION
  add constraint PK_APPLY_ID primary key (APPLY_ID);

create table APPLICATION_DETAIL
(
  apply_id         NUMBER(10),
  relate_object    VARCHAR2(10),
  relate_object_id NUMBER(10),
  apply_content    VARCHAR2(200),
  audit_reason     VARCHAR2(20)
);
-- Add comments to the columns 
comment on column APPLICATION_DETAIL.apply_id
  is '申请id';
comment on column APPLICATION_DETAIL.relate_object
  is '关联对象 BLOCK_MAN,TASK';
comment on column APPLICATION_DETAIL.relate_object_id
  is '关联对象id';
comment on column APPLICATION_DETAIL.apply_content
  is '申请内容 例:计划变更：
{“changeType”:”TASK” //TASK,任务计划;BLOCK_MAN,block计划
,”name”:”task1”,”reason”:”修改” }
作业申请：
{“wordContent”:”block1”,”poiPlanTotal”:123,”roadPlanTotal”:123,”memo”:”备注” }
';
comment on column APPLICATION_DETAIL.audit_reason
  is '审批原因';

create table APPLICATION_TIMELINE
(
  apply_id     NUMBER(10),
  operate_time TIMESTAMP(6),
  operator     NUMBER(10),
  content      VARCHAR2(100)
);
-- Add comments to the columns 
comment on column APPLICATION_TIMELINE.apply_id
  is '申请id';
comment on column APPLICATION_TIMELINE.operate_time
  is '处理时间';
comment on column APPLICATION_TIMELINE.operator
  is '处理人';
comment on column APPLICATION_TIMELINE.content
  is '该操作执行的事件，
例如：创建(取生成记录时间)
提交审核(取提交记录时间)
审核通过(取审核时间)
';

CREATE SEQUENCE APPLICATION_SEQ START WITH 1 MAXVALUE 9999999999;

create table MAN_CONFIG
(
  conf_key   VARCHAR2(64) not null,
  conf_value VARCHAR2(512) not null,
  conf_desc  VARCHAR2(512)，
  exe_user_id number(10),
exe_date timestamp
);
-- Add comments to the table 
comment on table MAN_CONFIG
  is '管理统一配置表';
comment on column MAN_CONFIG.conf_key
  is '参数名称';
comment on column MAN_CONFIG.conf_value
  is '参数值';
comment on column MAN_CONFIG.conf_desc
  is '参数用途描述';

alter table customised_layer add city_ID NUMBER(10);

create table SUBTASK_REFER
(
  id       NUMBER(10),
  geometry SDO_GEOMETRY
);
-- Add comments to the table 
comment on table SUBTASK_REFER
  is '不规则子任务表';

INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('SUBTASK_REFER',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_SUBTASK_REFER ON SUBTASK_REFER(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE SUBTASK_REFER COMPUTE STATISTICS;

alter table SUBTASK drop column refer_geometry;
alter table SUBTASK add refer_id NUMBER(10);
comment on column SUBTASK. Refer_id
  is '参考不规则任务圈';

-- Create table
create table DAY2MONTH_CONFIG
(
  conf_id     NUMBER(10) not null,
  city_id     NUMBER(10),
  type        VARCHAR2(10),
  status      NUMBER(2),
  exe_user_id NUMBER(10),
  exe_date    TIMESTAMP(6)
);
-- Add comments to the table 
comment on table DAY2MONTH_CONFIG
  is '日落月开关管理表';
-- Add comments to the columns 
comment on column DAY2MONTH_CONFIG.type
  is 'POI,ALL';
comment on column DAY2MONTH_CONFIG.status
  is '0开1关，不可进行操作';
comment on column DAY2MONTH_CONFIG.exe_user_id
  is '执行人';
comment on column DAY2MONTH_CONFIG.exe_date
  is '执行时间';
-- Create/Recreate primary, unique and foreign key constraints 
alter table DAY2MONTH_CONFIG
  add constraint PK_DAY2MONTH_CONFIG primary key (CONF_ID);
  
create table FM_STAT_DAY2MONTH
(
  city_id            NUMBER(10),
  type               VARCHAR2(10),
  cur_total          NUMBER(10),
  accumulative_total NUMBER(10),
  stat_date          TIMESTAMP(6),
  stat_time          TIMESTAMP(6)
);
-- Add comments to the table 
comment on table FM_STAT_DAY2MONTH
  is '日落月统计表';
-- Add comments to the columns 
comment on column FM_STAT_DAY2MONTH.type
  is 'POI,ALL';
comment on column FM_STAT_DAY2MONTH.cur_total
  is '当前数量';
comment on column FM_STAT_DAY2MONTH.accumulative_total
  is '累计数量';

CREATE SEQUENCE day2month_config_seq START WITH 1 MAXVALUE 9999999999;

alter table infor add feedback_type NUMBER(1) default 0 not null;
comment on column INFOR.feedback_type
  is '0未反馈 1已反馈';

--20170215过渡期管理平台增量脚本
alter table REGION add geometry SDO_GEOMETRY;

INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('REGION',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_REGION ON REGION(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE REGION COMPUTE STATISTICS;

comment on column CITY.plan_status
  is '规划状态。0未规划，1已规划，2已关闭3进行中';
create table PROGRAM
(
  program_id                 NUMBER(10) not null,
  city_id                    NUMBER(10) default 0 not null,
  infor_id                   VARCHAR2(50),
  latest                     NUMBER(1) default 1 not null,
  create_user_id             NUMBER(10) default 0 not null,
  name                       VARCHAR2(200),
  type                       NUMBER(1) default 1 not null,
  status                     NUMBER(1) default 2,
  descp                      VARCHAR2(200),
  create_date                TIMESTAMP(6),
  plan_start_date            TIMESTAMP(6),
  plan_end_date              TIMESTAMP(6),
  collect_plan_start_date    TIMESTAMP(6),
  collect_plan_end_date      TIMESTAMP(6),
  day_edit_plan_start_date   TIMESTAMP(6),
  day_edit_plan_end_date     TIMESTAMP(6),
  month_edit_plan_start_date TIMESTAMP(6),
  month_edit_plan_end_date   TIMESTAMP(6),
  produce_plan_start_date    TIMESTAMP(6),
  produce_plan_end_date      TIMESTAMP(6),
  lot                        NUMBER(1) default 0
);
-- Add comments to the columns 
comment on column PROGRAM.program_id
  is '项目id';
comment on column PROGRAM.city_id
  is '城市id';
comment on column PROGRAM.infor_id
  is '情报id';
comment on column PROGRAM.latest
  is '是否为当前city关联最新项目，0 否 1是';
comment on column PROGRAM.create_user_id
  is '创建人';
comment on column PROGRAM.name
  is '项目名称';
comment on column PROGRAM.type
  is '项目类型。1常规2快速更新9 虚拟项目';
comment on column PROGRAM.status
  is '项目状态。0关闭1开启2草稿';
comment on column PROGRAM.descp
  is '项目描述';
comment on column PROGRAM.create_date
  is '创建时间';
comment on column PROGRAM.plan_start_date
  is '项目计划开始时间';
comment on column PROGRAM.plan_end_date
  is '项目计划结束时间';
comment on column PROGRAM.collect_plan_start_date
  is '采集计划开始时间';
comment on column PROGRAM.collect_plan_end_date
  is '采集计划结束时间';
comment on column PROGRAM.day_edit_plan_start_date
  is '日编计划开始时间';
comment on column PROGRAM.day_edit_plan_end_date
  is '日编计划结束时间';
comment on column PROGRAM.month_edit_plan_start_date
  is '月编计划开始时间';
comment on column PROGRAM.month_edit_plan_end_date
  is '月编计划结束时间';
comment on column PROGRAM.produce_plan_start_date
  is '出品计划开始时间';
comment on column PROGRAM.produce_plan_end_date
  is '出品计划结束时间';
comment on column PROGRAM.lot
  is '中线出品批次，仅快速更新项目用，常规项目均为0。0无批次1中线第一批次2中线第二批次3中线第三批次';
-- Create/Recreate primary, unique and foreign key constraints 
alter table PROGRAM
  add constraint PROGRAMKEY primary key (PROGRAM_ID);
  
CREATE SEQUENCE PROGRAM_SEQ START WITH 1 MAXVALUE 9999999999;
comment on column BLOCK.plan_status
  is '规划状态。0未规划，1已规划，2已关闭3进行中';
drop table task cascade constraints;

alter table task
  add program_id              NUMBER(10);
alter table task
  add block_id number(10) default 0;
alter table task
  add region_id number(10);

alter table task drop column city_id;
alter table task drop column month_edit_plan_start_date;
alter table task drop column month_edit_plan_end_date;
alter table task drop column month_edit_group_id;
alter table task drop column month_produce_plan_start_date;
alter table task drop column month_produce_plan_end_date;
alter table task drop column task_type;

alter table task
  add produce_plan_start_date TIMESTAMP(6);
alter table task
  add produce_plan_end_date TIMESTAMP(6);
alter table task
  add lot NUMBER(1);
alter table task
  add group_id                NUMBER(10) default 0;
alter table task
  add  road_plan_total         NUMBER(10) default 0;
  alter table task
  add poi_plan_total          NUMBER(10) default 0;
  
-- Add comments to the columns 
comment on column TASK.task_id
  is '任务id';
comment on column TASK.block_id
  is '所属区县id';
comment on column TASK.program_id
  is '所属项目id';
comment on column TASK.create_user_id
  is '创建人';
comment on column TASK.status
  is '任务状态。0关闭1开启2草稿';
comment on column TASK.type
  is '任务类型。0采集1日编2 POI月编3二代编辑9虚拟任务';
comment on column TASK.name
  is '任务名称';
comment on column TASK.descp
  is '任务描述';
comment on column TASK.create_date
  is '创建时间';
comment on column TASK.plan_start_date
  is '作业计划开始时间';
comment on column TASK.plan_end_date
  is '作业计划结束时间';
comment on column TASK.produce_plan_start_date
  is '出品计划开始时间。仅常规采集任务修改，其他常规任务同步更新';
comment on column TASK.produce_plan_end_date
  is '出品计划结束时间。仅常规采集任务修改，其他常规任务同步更新';
comment on column TASK.lot
  is '中线出品批次，仅常规采集任务修改，其他常规任务同步更新。0无批次1中线第一批次2中线第二批次3中线第三批次';
comment on column TASK.latest
  is '是否为与当前区县关联最新的任务。0否，1是';
comment on column TASK.group_id
  is '作业组';
comment on column TASK.road_plan_total
  is '计划道路作业总量。常规/快速更新采集任务修改，其他任务同步更新';
comment on column TASK.poi_plan_total
  is '计划poi作业总量。常规/快速更新采集任务修改，其他任务同步更新';

create table TASK_CMS_PROGRESS
(
phase_id     NUMBER(10),
  task_id     NUMBER(10),
  phase       NUMBER(1) default 1,
  status      NUMBER(1) default 0,
  create_date TIMESTAMP(6),
  start_date  TIMESTAMP(6),
  end_date    TIMESTAMP(6)
);
-- Add comments to the columns 
comment on column TASK_CMS_PROGRESS.task_id
  is '任务id';
comment on column TASK_CMS_PROGRESS.phase
  is '阶段。1日落月2aumark自动转换3日落月开关修改4cms任务创建';
comment on column TASK_CMS_PROGRESS.status
  is '执行状态。0创建，1进行中,2执行成功3执行失败';
comment on column TASK_CMS_PROGRESS.create_date
  is '创建时间';
comment on column TASK_CMS_PROGRESS.start_date
  is '开始时间';
comment on column TASK_CMS_PROGRESS.end_date
  is '结束时间';
CREATE SEQUENCE phase_SEQ START WITH 1 MAXVALUE 9999999999;
alter table SUBTASK drop column block_man_id;
-- Add comments to the columns 
comment on column SUBTASK.type
  is '0 POI_采集 1道路_采集 2一体化_采集 3一体化_grid粗编_日编 4一体化_区域粗编_日编 5 POI粗编_日编 6代理店 7 POI专项_月编 8道路_grid精编 9道路_grid粗编 10道路区域专项';
drop table block_grid_mapping;
alter table SUBTASK_GRID_MAPPING add type number(1) default 1;
-- Add comments to the columns 
comment on column SUBTASK_GRID_MAPPING.type
  is '类型。1规划 2规划外，调整后的';
alter table INFOR drop column task_id;
create table TASK_GRID_MAPPING
(
  task_id VARCHAR2(50) not null,
  grid_id NUMBER(10) not null,
  type    NUMBER(1) default 1 not null
);
-- Add comments to the columns 
comment on column TASK_GRID_MAPPING.task_id
  is '任务id';
comment on column TASK_GRID_MAPPING.grid_id
  is 'grid id.调整增加的grid';
comment on column TASK_GRID_MAPPING.type
  is '1:规划；2：规划外，调整后的。';
drop table message;
alter table PRODUCE rename column subtask_id to program_ID;
-- Add comments to the columns 
comment on column PRODUCE.program_ID
  is '出品项目id';

alter table FM_STAT_OVERVIEW add monthly_percent number(10);
alter table FM_STAT_OVERVIEW add monthly_plan_start_date timestamp;
alter table FM_STAT_OVERVIEW add monthly_plan_end_date timestamp;
alter table FM_STAT_OVERVIEW add monthly_plan_date number(10);
alter table FM_STAT_OVERVIEW add monthly_actual_start_date timestamp;
alter table FM_STAT_OVERVIEW add monthly_actual_end_date timestamp;
alter table FM_STAT_OVERVIEW add monthly_diff_date number(10);

create table FM_STAT_OVERVIEW_PROGRAM
(
  PROGRAM_ID                   NUMBER(10),
  percent                   NUMBER(10),
  diff_date                 NUMBER(10),
  progress                  NUMBER(1),
  status               NUMBER(1),
  collect_progress          NUMBER(1),
  collect_percent           NUMBER(10),
  daily_progress            NUMBER(1),
  daily_percent             NUMBER(10),
  stat_date                 TIMESTAMP(6),
  monthly_progress          NUMBER(1),
  monthly_percent           NUMBER(10),
  plan_start_date           TIMESTAMP(6),
  plan_end_date             TIMESTAMP(6),
  plan_date                 NUMBER(10),
  actual_start_date         TIMESTAMP(6),
  actual_end_date           TIMESTAMP(6),
  collect_plan_start_date   TIMESTAMP(6),
  collect_plan_end_date     TIMESTAMP(6),
  collect_plan_date         NUMBER(10),
  collect_actual_start_date TIMESTAMP(6),
  collect_actual_end_date   TIMESTAMP(6),
  collect_diff_date         NUMBER(10),
  daily_plan_start_date     TIMESTAMP(6),
  daily_plan_end_date       TIMESTAMP(6),
  daily_plan_date           NUMBER(10),
  daily_actual_start_date   TIMESTAMP(6),
  daily_actual_end_date     TIMESTAMP(6),
  daily_diff_date           NUMBER(10),
  stat_time                 TIMESTAMP(6),
  poi_plan_total            NUMBER(10),
  road_plan_total           NUMBER(10),
monthly_plan_start_date     TIMESTAMP(6),
  monthly_plan_end_date       TIMESTAMP(6),
  monthly_plan_date           NUMBER(10),
  monthly_actual_start_date   TIMESTAMP(6),
  monthly_actual_end_date     TIMESTAMP(6),
  monthly_diff_date           NUMBER(10)
);
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_PROGRAM.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_PROGRAM.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_PROGRAM.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_PROGRAM.daily_progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期)';
comment on column FM_STAT_OVERVIEW_PROGRAM.status
  is '0关闭，1开启,2草稿';
comment on column FM_STAT_OVERVIEW_PROGRAM.stat_date
  is '统计时间';
comment on column FM_STAT_OVERVIEW_PROGRAM.collect_plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW_PROGRAM.collect_diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW_PROGRAM.daily_plan_date
  is '计划天数';
comment on column FM_STAT_OVERVIEW_PROGRAM.daily_diff_date
  is '距离计划结束时间天数';
comment on column FM_STAT_OVERVIEW_PROGRAM.stat_time
  is '统计详细时间';
comment on column FM_STAT_OVERVIEW_PROGRAM.poi_plan_total
  is '预估作业量_POI';
comment on column FM_STAT_OVERVIEW_PROGRAM.road_plan_total
  is '预估作业量_道路';
drop table fm_stat_overview_task;
create table FM_STAT_OVERVIEW_task
(
 Program_id NUMBER(10),
  task_id                   NUMBER(10),
  progress                  NUMBER(1),
  percent                   NUMBER(10),
status               NUMBER(1),
  plan_start_date           TIMESTAMP(6),
  plan_end_date             TIMESTAMP(6),
  diff_date                 NUMBER(10),
  poi_plan_total            NUMBER(10),
  road_plan_total           NUMBER(10),
  stat_date                 VARCHAR2(200),
  stat_time                 TIMESTAMP(6),
  group_id          NUMBER(10)
);
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_task.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_task.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_task.status
  is '0关闭，1开启,2草稿';
comment on column FM_STAT_OVERVIEW_task.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_task.poi_plan_total
  is '预估作业量_POI';
comment on column FM_STAT_OVERVIEW_task.road_plan_total
  is '预估作业量_道路';
comment on column FM_STAT_OVERVIEW_task.stat_date
  is '统计时间';
comment on column FM_STAT_OVERVIEW_task.stat_time
  is '统计详细时间';
drop table fm_stat_overview_blockman;
drop table fm_stat_overview_subtask;
create table FM_STAT_OVERVIEW_SUBTASK
(
  subtask_id           NUMBER(10),
  percent              NUMBER(10),
  diff_date            NUMBER(10),
  progress             NUMBER(1),
  status               NUMBER(1),
  total_poi            NUMBER(10),
  finished_poi         NUMBER(10),
  total_road           NUMBER(10),
  finished_road        NUMBER(10),
  percent_poi          NUMBER(10),
  percent_road         NUMBER(10),
  plan_start_date      TIMESTAMP(6) not null,
  plan_end_date        TIMESTAMP(6),
  actual_start_date    TIMESTAMP(6) not null,
  actual_end_date      TIMESTAMP(6),
  stat_time            VARCHAR2(14),
  stat_date            VARCHAR2(8),
  grid_percent_details CLOB,
  task_id         NUMBER(10)
);
-- Add comments to the columns 
comment on column FM_STAT_OVERVIEW_SUBTASK.percent
  is '进度百分比 0-100';
comment on column FM_STAT_OVERVIEW_SUBTASK.diff_date
  is '距离计划结束时间天数,负数表示预期';
comment on column FM_STAT_OVERVIEW_SUBTASK.progress
  is '进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）';
comment on column FM_STAT_OVERVIEW_SUBTASK.stat_date
  is '统计日期';
comment on column FM_STAT_OVERVIEW_SUBTASK.status
  is '0关闭，1开启,2草稿';
comment on column FM_STAT_OVERVIEW_SUBTASK.total_poi
  is 'POI总量';
comment on column FM_STAT_OVERVIEW_SUBTASK.finished_poi
  is 'POI完成量';
comment on column FM_STAT_OVERVIEW_SUBTASK.total_road
  is 'ROAD总量';
comment on column FM_STAT_OVERVIEW_SUBTASK.finished_road
  is 'ROAD完成量';
comment on column FM_STAT_OVERVIEW_SUBTASK.percent_poi
  is 'POI完成百分比';
comment on column FM_STAT_OVERVIEW_SUBTASK.percent_road
  is 'ROAD完成百分比';
comment on column FM_STAT_OVERVIEW_SUBTASK.plan_start_date
  is '几乎开始时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.plan_end_date
  is '计划结束时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.actual_start_date
  is '实际开始时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.actual_end_date
  is '实际结束时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.stat_time
  is '统计时间';
comment on column FM_STAT_OVERVIEW_SUBTASK.grid_percent_details
  is 'grid进度详情'; 

create table PROGRAM_GRID_MAPPING
(
  program_id VARCHAR2(50) not null,
  grid_id    NUMBER(10) not null,
  type       NUMBER(1) default 1 not null
);
-- Add comments to the columns 
comment on column PROGRAM_GRID_MAPPING.program_id
  is '项目ID';
comment on column PROGRAM_GRID_MAPPING.grid_id
  is 'GRID ID.调整增加的GRID';
comment on column PROGRAM_GRID_MAPPING.type
  is '1:规划；2：规划外，调整后的。';
alter table block add WORK_TYPE varchar(10);
exit;




