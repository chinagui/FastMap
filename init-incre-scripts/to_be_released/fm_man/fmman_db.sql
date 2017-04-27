drop table program;
drop table infor;
drop table INFOR_GRID_MAPPING;
-- Create table
create table INFOR
(
  infor_name     VARCHAR2(200),
  geometry       CLOB not null,
  infor_level    NUMBER(1),
  plan_status    NUMBER(1),
  infor_content  VARCHAR2(400),
  insert_time    TIMESTAMP(6) default sysdate,
  feedback_type  NUMBER(1) default 0 not null,
  feature_kind   NUMBER(1),
  admin_name     VARCHAR2(100),
  infor_code     VARCHAR2(100),
  publish_date   TIMESTAMP(6),
  expect_date    TIMESTAMP(6),
  topic_name     VARCHAR2(100),
  method         VARCHAR2(100),
  news_date      TIMESTAMP(6),
  road_length    NUMBER(10),
  source_code    NUMBER(1),
  info_type_name VARCHAR2(100),
  infor_id       NUMBER(10) not null
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 128K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the columns 
comment on column INFOR.infor_name
  is '情报名称';
comment on column INFOR.geometry
  is '几何';
comment on column INFOR.infor_level
  is '情报级别';
comment on column INFOR.plan_status
  is '情报规划状态:0未规划，1已规划，2已关闭';
comment on column INFOR.infor_content
  is '情报内容描述';
comment on column INFOR.insert_time
  is '情报插入时间';
comment on column INFOR.feedback_type
  is '0未反馈 1已反馈';
comment on column INFOR.feature_kind
  is '种别代码	1 POI；2 道路（默认）';
comment on column INFOR.admin_name
  is '情报省份城市';
comment on column INFOR.infor_code
  is '情报编码';
comment on column INFOR.publish_date
  is '发布时间';
comment on column INFOR.expect_date
  is '投入时间';
comment on column INFOR.topic_name
  is '专题名称';
comment on column INFOR.method
  is '情报对应方式';
comment on column INFOR.news_date
  is '新闻发布时间';
comment on column INFOR.road_length
  is '长度';
comment on column INFOR.source_code
  is '情报来源';
comment on column INFOR.info_type_name
  is '情报类别名称';
comment on column INFOR.infor_id
  is '情报ID';
  
  
CREATE SEQUENCE INFOR_SEQ START WITH 1 MAXVALUE 9999999999;


-- Create table
create table INFOR_GRID_MAPPING
(
  infor_id NUMBER(10) not null,
  grid_id  NUMBER(10) not null
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
comment on column INFOR_GRID_MAPPING.infor_id
  is '情报id';
comment on column INFOR_GRID_MAPPING.grid_id
  is 'grid id';
-- Create/Recreate indexes 
create index IDX_INFOR_GRID_MAP_1 on INFOR_GRID_MAPPING (INFOR_ID)
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
create index IDX_INFOR_GRID_MAP_2 on INFOR_GRID_MAPPING (GRID_ID)
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
-- Create table
create table PROGRAM
(
  program_id                 NUMBER(10) not null,
  city_id                    NUMBER(10) default 0 not null,
  latest                     NUMBER(1) default 1 not null,
  create_user_id             NUMBER(10) default 0,
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
  lot                        NUMBER(1) default 0,
  infor_id                   NUMBER(10)
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
comment on column PROGRAM.program_id
  is '项目id';
comment on column PROGRAM.city_id
  is '城市id';
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
  add constraint PROGRAMKEY primary key (PROGRAM_ID)
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
  
-- Create table
create table ADMIN_GROUP_MAPPING
(
  province_name      VARCHAR2(100) not null,
  collect_group_name VARCHAR2(100),
  edit_group_name    VARCHAR2(100)
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
comment on column ADMIN_GROUP_MAPPING.province_name
  is '省份城市';
comment on column ADMIN_GROUP_MAPPING.collect_group_name
  is '采集组名称';
comment on column ADMIN_GROUP_MAPPING.edit_group_name
  is '编辑组名称';

