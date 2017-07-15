-- Create table
drop table POI_COUNT_TABLE;
drop table POI_PROBLEM_SUMMARY;
create table POI_COUNT_TABLE
(
  FID                        VARCHAR2(20) not null,
  "LEVEL"                      VARCHAR2(10),
  POI_NAME                   VARCHAR2(20),
  MESH_ID                    VARCHAR2(20),
  AREA                       VARCHAR2(20),
  CATEGORY                   VARCHAR2(20),
  FULL_MATCH                 VARCHAR2(10) default 0,
  PARTIAL_MATCH              VARCHAR2(10) default 0,
  EXTRA                      VARCHAR2(10),
  MISSING                    VARCHAR2(10),
  NAME_DB_COUNT              VARCHAR2(10),
  NAME_SITE_COUNT            VARCHAR2(10),
  NAME_MODIFY                VARCHAR2(10),
  NAME_DATA_UNMODIFIED       VARCHAR2(200),
  NAME_DATA_MODIFIED         VARCHAR2(200),
  POSITION_DB_COUNT          VARCHAR2(10),
  POSITION_SITE_COUNT        VARCHAR2(10),
  POSITION_MODIFY            VARCHAR2(10),
  POSITION_DATA_UNMODIFIED   VARCHAR2(50),
  POSITION_DATA_MODIFIED     VARCHAR2(50),
  CATEGORY_DB_COUNT          VARCHAR2(10),
  CATEGORY_SITE_COUNT        VARCHAR2(10),
  CATEGORY_MODIFY            VARCHAR2(10),
  CATEGORY_DATA_UNMODIFIED   VARCHAR2(8),
  CATEGORY_DATA_MODIFIED     VARCHAR2(8),
  ADDRESS_DB_COUNT           VARCHAR2(10),
  ADDRESS_SITE_COUNT         VARCHAR2(10),
  ADDRESS_MODIFY             VARCHAR2(10),
  ADDRESS_DATA_UNMODIFIED    VARCHAR2(500),
  ADDRESS_DATA_MODIFIED      VARCHAR2(500),
  PHOTE_DB_COUNT             VARCHAR2(10),
  PHOTE_SITE_COUNT           VARCHAR2(10),
  PHOTE_MODIFY               VARCHAR2(10),
  PHOTE_DATA_UNMODIFIED      VARCHAR2(500),
  PHOTE_DATA_MODIFIED        VARCHAR2(500),
  SITE_DB_COUNT              VARCHAR2(10) default 'null',
  SITE_SITE_COUNT            VARCHAR2(10) default 'null',
  SITE_MODIFY                VARCHAR2(10) default 'null',
  SITE_DATA_UNMODIFIED       VARCHAR2(200) default 'null',
  SITE_DATA_MODIFIED         VARCHAR2(200) default 'null',
  FATHER_SON_DB_COUNT        VARCHAR2(10),
  FATHER_SON_SITE_COUNT      VARCHAR2(10),
  FATHER_SON_MODIFY          VARCHAR2(10),
  FATHER_SON_DATA_UNMODIFIED CLOB,
  FATHER_SON_DATA_MODIFIED   CLOB,
  DEEP_DB_COUNT              VARCHAR2(10),
  DEEP_SITE_COUNT            VARCHAR2(10),
  DEEP_MODIFY                VARCHAR2(10),
  DEEP_DATA_UNMODIFIED       CLOB,
  DEEP_DATA_MODIFIED         CLOB,
  LABEL_DB_COUNT             VARCHAR2(10),
  LABEL_SITE_COUNT           VARCHAR2(10),
  LABEL_MODIFY               VARCHAR2(10),
  LABEL_DATA_UNMODIFIED      VARCHAR2(500),
  LABEL_DATA_MODIFIED        VARCHAR2(500),
  RESTURANT_DB_COUNT         VARCHAR2(10),
  RESTURANT_SITE_COUNT       VARCHAR2(10),
  RESTURANT_MODIFY           VARCHAR2(10),
  RESTURANT_DATA_UNMODIFIED  CLOB,
  RESTURANT_DATA_MODIFIED    CLOB,
  LINK_DB_COUNT              VARCHAR2(10),
  LINK_SITE_COUNT            VARCHAR2(10),
  LINK_MODIFY                VARCHAR2(10),
  LINK_DATA_UNMODIFIED       VARCHAR2(10),
  LINK_DATA_MODIFIED         VARCHAR2(10),
  POSTCODE_DB_COUNT          VARCHAR2(10) default 'null',
  POSTCODE_SITE_COUNT        VARCHAR2(10) default 'null',
  POSTCODE_MODIFY            VARCHAR2(10) default 'null',
  POSTCODE_DATA_UNMODIFIED   VARCHAR2(200) default 'null',
  POSTCODE_DATA_MODIFIED     VARCHAR2(200) default 'null',
  LEVEL_DB_COUNT             VARCHAR2(10),
  LEVEL_SITE_COUNT           VARCHAR2(10),
  LEVEL_MODIFY               VARCHAR2(10),
  LEVEL_DATA_UNMODIFIED      VARCHAR2(200),
  LEVEL_DATA_MODIFIED        VARCHAR2(200),
  COLLECTOR_USERID           VARCHAR2(10),
  COLLECTOR_TIME             VARCHAR2(50),
  INPUT_USERID               VARCHAR2(10),
  INPUT_TIME                 VARCHAR2(50),
  QC_USERID                  VARCHAR2(10),
  QC_TIME                    VARCHAR2(50),
  QC_SUB_TASKID              VARCHAR2(10),
  VISION                     VARCHAR2(10),
  MEMO                       VARCHAR2(200),
  "TYPE"                       VARCHAR2(10) default 0,
  MEMO_USERID                VARCHAR2(10),
  HAS_EXPORT                 VARCHAR2(10)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 16
    next 8
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table POI_COUNT_TABLE
  is 'POI质检样本表';

  
-- Create table
create  table POI_PROBLEM_SUMMARY
(
  "GROUP"               VARCHAR2(100),
  PROVINCE            VARCHAR2(20),
  CITY                VARCHAR2(20),
  SUBTASK_ID          NUMBER(10),
  ROUTE_NUM           NUMBER(10),
  "LEVEL"               VARCHAR2(2),
  PROBLEM_NUM         VARCHAR2(32),
  PHOTO_NUM           VARCHAR2(200),
  MESH_ID             NUMBER(8),
  GROUP_NAME          VARCHAR2(20),
  POI_NUM             VARCHAR2(20),
  KIND_CODE           VARCHAR2(8),
  CLASS_TOP           VARCHAR2(20),
  CLASS_MEDIUM        VARCHAR2(20),
  CLASS_BOTTOM        VARCHAR2(50),
  PROBLEM_TYPE        VARCHAR2(10),
  PROBLEM_PHENOMENON  VARCHAR2(100),
  PROBLEM_DESCRIPTION VARCHAR2(200),
  INITIAL_CAUSE       VARCHAR2(20),
  ROOT_CAUSE          VARCHAR2(100),
  CHECK_USER          NUMBER(10),
  CHECK_TIME          TIMESTAMP(6),
  COLLECTOR_USER      VARCHAR2(20),
  COLLECTOR_TIME      TIMESTAMP(6),
  INPUT_USER          VARCHAR2(20),
  INPUT_TIME          TIMESTAMP(6),
  CHECK_DEPARTMENT    VARCHAR2(20) default '外业采集部' not null,
  CHECK_MODE          VARCHAR2(20),
  MODIFY_DATE         TIMESTAMP(6),
  MODIFY_USER         VARCHAR2(20),
  CONFIRM_USER        VARCHAR2(20),
  VERSION             VARCHAR2(10),
  PROBLEM_LEVEL       VARCHAR2(10),
  PHOTO_EXIST         NUMBER(1),
  MEMO                VARCHAR2(200),
  MEMO_USER           VARCHAR2(10),
  CLASS_WEIGHT        VARCHAR2(5),
  PROBLEM_WEIGHT      VARCHAR2(5),
  TOTAL_WEIGHT        VARCHAR2(5),
  WORD_YEAR           VARCHAR2(5)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64
    next 1
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table POI_PROBLEM_SUMMARY
  is 'POI质检问题记录表';
-- Add comments to the columns 
comment on column POI_PROBLEM_SUMMARY."GROUP"
  is '队别,子任务所属基地名';
comment on column POI_PROBLEM_SUMMARY.PROVINCE
  is '省,子任务所属省';
comment on column POI_PROBLEM_SUMMARY.CITY
  is '市,子任务所属市';
comment on column POI_PROBLEM_SUMMARY.SUBTASK_ID
  is '项目，导出时导出子任务名称';
comment on column POI_PROBLEM_SUMMARY.ROUTE_NUM
  is '线路号，不维护';
comment on column POI_PROBLEM_SUMMARY."LEVEL"
  is '设施类别，POI.level';
comment on column POI_PROBLEM_SUMMARY.PROBLEM_NUM
  is '问题编号，uuid（不带下划线_的uuid）';
comment on column POI_PROBLEM_SUMMARY.PHOTO_NUM
  is '照片编号，不维护';
comment on column POI_PROBLEM_SUMMARY.MESH_ID
  is '图幅号，Poi.meshid';
comment on column POI_PROBLEM_SUMMARY.GROUP_NAME
  is '组名，不维护';
comment on column POI_PROBLEM_SUMMARY.POI_NUM
  is '设施ID，Poi.POI_NUM，对应导出报表fid';
comment on column POI_PROBLEM_SUMMARY.KIND_CODE
  is '分类代码，Poi.kindcode';
comment on column POI_PROBLEM_SUMMARY.CLASS_TOP
  is '大分类，前台输入';
comment on column POI_PROBLEM_SUMMARY.CLASS_MEDIUM
  is '中分类，前台输入';
comment on column POI_PROBLEM_SUMMARY.CLASS_BOTTOM
  is '小分类，前台输入';
comment on column POI_PROBLEM_SUMMARY.PROBLEM_TYPE
  is '问题类型，前台输入';
comment on column POI_PROBLEM_SUMMARY.PROBLEM_PHENOMENON
  is '问题现象，前台输入';
comment on column POI_PROBLEM_SUMMARY.PROBLEM_DESCRIPTION
  is '问题描述，前台输入';
comment on column POI_PROBLEM_SUMMARY.INITIAL_CAUSE
  is '初步原因，前台输入';
comment on column POI_PROBLEM_SUMMARY.ROOT_CAUSE
  is '根本原因（RCA），前台输入';
comment on column POI_PROBLEM_SUMMARY.CHECK_USER
  is '质检员，质检员ID';
comment on column POI_PROBLEM_SUMMARY.CHECK_TIME
  is '质检日期，当前记录问题时间';
comment on column POI_PROBLEM_SUMMARY.COLLECTOR_USER
  is '采集员，前台输入（如果查
询不到采集员赋“AAA”）
';
comment on column POI_PROBLEM_SUMMARY.COLLECTOR_TIME
  is '采集日期，前台输入';
comment on column POI_PROBLEM_SUMMARY.INPUT_USER
  is '录入员，不维护';
comment on column POI_PROBLEM_SUMMARY.INPUT_TIME
  is '录入日期，不维护';
comment on column POI_PROBLEM_SUMMARY.CHECK_DEPARTMENT
  is '质检部门，服务赋值默认外业采集部';
comment on column POI_PROBLEM_SUMMARY.CHECK_MODE
  is '质检方式，前台输入';
comment on column POI_PROBLEM_SUMMARY.MODIFY_DATE
  is '更改日期，同质检日期';
comment on column POI_PROBLEM_SUMMARY.MODIFY_USER
  is '更改人，同质检人';
comment on column POI_PROBLEM_SUMMARY.CONFIRM_USER
  is '确认人，前台输入';
comment on column POI_PROBLEM_SUMMARY.VERSION
  is '版本号，前台输入（前端提供前后
四年的版本供选择）
';
comment on column POI_PROBLEM_SUMMARY.PROBLEM_LEVEL
  is '问题等级，前台输入';
comment on column POI_PROBLEM_SUMMARY.PHOTO_EXIST
  is '是否有照片，不维护';
comment on column POI_PROBLEM_SUMMARY.MEMO
  is '备注，前台输入';
comment on column POI_PROBLEM_SUMMARY.MEMO_USER
  is '备注作业员，当采集员是”AAA“时，
该字段读取采集子任务作业员userid
';
comment on column POI_PROBLEM_SUMMARY.CLASS_WEIGHT
  is '类别权重，不维护';
comment on column POI_PROBLEM_SUMMARY.PROBLEM_WEIGHT
  is '问题重要度权重，不维护';
comment on column POI_PROBLEM_SUMMARY.TOTAL_WEIGHT
  is '总权重，不维护';
comment on column POI_PROBLEM_SUMMARY.WORD_YEAR
  is '工作年限，不维护';
  
commit;
exit;
