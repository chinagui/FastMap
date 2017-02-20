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




