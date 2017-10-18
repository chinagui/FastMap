/*==============================================================*/
/* Table: RD_NAME                                               */
/*==============================================================*/
create table RD_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          not null,
   NAME                 VARCHAR2(500)        not null,
   TYPE                 VARCHAR2(100),
   BASE                 VARCHAR2(100),
   PREFIX               VARCHAR2(100),
   INFIX                VARCHAR2(100),
   SUFFIX               VARCHAR2(100),
   NAME_PHONETIC        VARCHAR2(1000),
   TYPE_PHONETIC        VARCHAR2(1000),
   BASE_PHONETIC        VARCHAR2(1000),
   PREFIX_PHONETIC      VARCHAR2(1000),
   INFIX_PHONETIC       VARCHAR2(1000),
   SUFFIX_PHONETIC      VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)           
check (SRC_FLAG in (0,1,2,3)) disable ,
   ROAD_TYPE            NUMBER(1)            not null
check (ROAD_TYPE in (0,1,2,3,4)) disable ,
   ADMIN_ID             NUMBER(6)            not null,
   CODE_TYPE            NUMBER(1)            not null
check (CODE_TYPE in (0,1,2,3,4,5,6,7)) disable ,
   VOICE_FILE           VARCHAR2(100),
   SRC_RESUME           VARCHAR2(1000),
   PA_REGION_ID         NUMBER(10),
   MEMO                 VARCHAR2(200),
   ROUTE_ID             NUMBER(10),
   U_RECORD             NUMBER(2),
   U_FIELDS             VARCHAR2(1000),
   SPLIT_FLAG           NUMBER(2)      default 0
check (SPLIT_FLAG in (0,1,2)) disable ,
   CITY                 VARCHAR2(400),
   PROCESS_FLAG          NUMBER(1) default 0,
  constraint PK_RD_NAME primary key (NAME_ID)
);
CREATE INDEX IX_RD_NM_1  ON  RD_NAME(NAME_GROUPID);

/* GDB+ POI EDIT PART */
CREATE TABLE POI_EDIT_STATUS(
  PID NUMBER(10) ,
  STATUS NUMBER(1) DEFAULT 0
      CHECK(STATUS IN (0,1,2,3)) DISABLE,
  IS_UPLOAD NUMBER(1) DEFAULT 0
      CHECK(IS_UPLOAD IN (0,1)) DISABLE,
  UPLOAD_DATE TIMESTAMP,
  FRESH_VERIFIED NUMBER(1) DEFAULT 0
    CHECK(FRESH_VERIFIED IN (0,1)) DISABLE,
  RAW_FIELDS VARCHAR2(30),
  WORK_TYPE NUMBER(2) DEFAULT 1,
  COMMIT_HIS_STATUS NUMBER(2) DEFAULT 0,
  SUBMIT_DATE TIMESTAMP,
  QUICK_SUBTASK_ID NUMBER(10) DEFAULT 0,
  QUICK_TASK_ID NUMBER(10) DEFAULT 0,
  MEDIUM_SUBTASK_ID NUMBER(10) DEFAULT 0,
  MEDIUM_TASK_ID NUMBER(10) DEFAULT 0
);
CREATE UNIQUE INDEX IDX_POI_EDIT_STATUS_2 ON POI_EDIT_STATUS(PID);

create table POI_EDIT_MULTISRC
(
  pid         number(10) not null,
  source_type varchar2(12) not null,
  main_type   number(2) not null
);
CREATE INDEX IDX_POI_EDIT_MS_ID ON POI_EDIT_MULTISRC(PID);
-- Create table
create table POI_COLUMN_STATUS
(
  PID                NUMBER(10) not null,
  WORK_ITEM_ID       VARCHAR2(50),
  FIRST_WORK_STATUS  NUMBER(1) default 1,
  SECOND_WORK_STATUS NUMBER(1) default 1,
  HANDLER            NUMBER(10) default 0,
  TASK_ID            NUMBER(10) default 0,
  APPLY_DATE		 TIMESTAMP,
  QC_FLAG            NUMBER(1) default 0,
  COMMON_HANDLER     NUMBER(10) default 0
);
-- Add comments to the table 
comment on table POI_COLUMN_STATUS
  is 'POI精编作业状态';
-- Add comments to the columns 
comment on column POI_COLUMN_STATUS.PID
  is 'POI ID';
comment on column POI_COLUMN_STATUS.WORK_ITEM_ID
  is '作业项ID';
comment on column POI_COLUMN_STATUS.FIRST_WORK_STATUS
  is '一级作业项状态';
comment on column POI_COLUMN_STATUS.SECOND_WORK_STATUS
  is '二级作业项状态';
comment on column POI_COLUMN_STATUS.HANDLER
  is '作业人员ID';
comment on column POI_COLUMN_STATUS.TASK_ID
  is '任务号';
comment on column POI_COLUMN_STATUS.APPLY_DATE
  is '申请时间';
comment on column POI_COLUMN_STATUS.QC_FLAG
  is '质检标识';
comment on column POI_COLUMN_STATUS.COMMON_HANDLER
  is '常规作业员hander';

create table POI_COLUMN_WORKITEM_CONF
(
  ID               VARCHAR2(100) not null,
  FIRST_WORK_ITEM  VARCHAR2(50),
  SECOND_WORK_ITEM VARCHAR2(50),
  WORK_ITEM_ID     VARCHAR2(50),
  CHECK_FLAG       NUMBER(1),
  TYPE             NUMBER(1)
);

create table POI_COLUMN_OP_CONF
(
  ID                         VARCHAR2(100) not null,
  FIRST_WORK_ITEM            VARCHAR2(50),
  SECOND_WORK_ITEM           VARCHAR2(50),
  SAVE_EXEBATCH              NUMBER(1) default 0,
  SAVE_BATCHRULES            VARCHAR2(100),
  SAVE_EXECHECK              NUMBER(1) default 0,
  SAVE_CKRULES               VARCHAR2(500),
  SAVE_EXECLASSIFY           NUMBER(1) default 0,
  SAVE_CLASSIFYRULES         VARCHAR2(100),
  SUBMIT_EXEBATCH            NUMBER(1) default 0,
  SUBMIT_BATCHRULES          VARCHAR2(100),
  SUBMIT_EXECHECK            NUMBER(1) default 0,
  SUBMIT_CKRULES             VARCHAR2(500),
  SUBMIT_EXECLASSIFY         NUMBER(1) default 0,
  SUBMIT_CLASSIFYRULES       VARCHAR2(100),
  TYPE                       NUMBER(1),
  FIRST_SUBMIT_EXEBATCH      NUMBER(1) default 0,
  FIRST_SUBMIT_BATCHRULES    VARCHAR2(100),
  FIRST_SUBMIT_EXECHECK      NUMBER(1) default 0,
  FIRST_SUBMIT_CKRULES       VARCHAR2(500),
  FIRST_SUBMIT_EXECLASSIFY   NUMBER(1) default 0,
  FIRST_SUBMIT_CLASSIFYRULES VARCHAR2(100)
);
create table COLUMN_QC_PROBLEM
(
ID                    NUMBER(10),
SUBTASK_ID            NUMBER(10),
PID                   NUMBER(10),
FIRST_WORK_ITEM       VARCHAR2(50),
SECOND_WORK_ITEM      VARCHAR2(50),
WORK_ITEM_ID          VARCHAR2(50),
OLD_VALUE             VARCHAR2(500),
NEW_VALUE             VARCHAR2(500),
ERROR_TYPE            VARCHAR2(100),
ERROR_LEVEL           VARCHAR2(200),
PROBLEM_DESC          VARCHAR2(500),
TECH_GUIDANCE         VARCHAR2(500),
TECH_SCHEME           VARCHAR2(500),
WORK_TIME             TIMESTAMP,
QC_TIME               TIMESTAMP,
IS_PROBLEM            VARCHAR2(100),
IS_VALID              NUMBER(1),
WORKER                NUMBER(10),
QC_WORKER             NUMBER(10),
ORIGINAL_INFO         VARCHAR2(200),
    constraint PK_COLUMN_QC_PROBLEM primary key (ID)
);

create sequence COLUMN_QC_PROBLEM_seq
increment by 1
start with 1
maxvalue 999999999;
  
/* GDB+ log part */
CREATE TABLE LOG_ACTION(
    ACT_ID RAW(16) NOT NULL,
    US_ID NUMBER(36) DEFAULT 0,
    OP_CMD VARCHAR2(1000),
    SRC_DB NUMBER(1) DEFAULT 0,
    STK_ID NUMBER(10) DEFAULT 0,
    CONSTRAINT PK_LOG_ACT PRIMARY KEY(ACT_ID)
);
CREATE INDEX IX_LOG_ACT_STKID ON LOG_ACTION(STK_ID);

CREATE SEQUENCE LOG_OP_SEQ MINVALUE 1 MAXVALUE 99999999999 START WITH 1 INCREMENT BY 1 CACHE 20;
create table LOG_OPERATION (
    OP_ID RAW(16) NOT NULL,
    ACT_ID RAW(16) NOT NULL,
    OP_DT TIMESTAMP,
    OP_SEQ NUMBER(12) DEFAULT 0 not null,
    COM_STA NUMBER(1) DEFAULT 0 NOT NULL
        CHECK(COM_STA IN (0,1)) DISABLE,
    COM_DT TIMESTAMP,
    LOCK_STA NUMBER(1) DEFAULT 0 NOT NULL
        CHECK(LOCK_STA IN (0,1)) DISABLE,
    constraint FK_LOG_OP_ACT foreign key (ACT_ID)
         references LOG_ACTION (ACT_ID) disable,
    constraint PK_LOG_OP primary key (OP_ID)
);
CREATE INDEX IX_LOG_OP_DT ON LOG_OPERATION(OP_DT);

create table LOG_DETAIL (
  ROW_ID RAW(16) NOT NULL,
    OP_ID RAW(16) NOT NULL,
  OB_NM VARCHAR(30),
  OB_PID NUMBER(10) DEFAULT 0,
  GEO_NM VARCHAR(30),
  GEO_PID NUMBER(10) DEFAULT 0,
    TB_NM VARCHAR2(30),
    OLD CLOB,
    NEW CLOB,
    FD_LST VARCHAR2(1000),
    OP_TP NUMBER(1) DEFAULT 0 NOT NULL
        CHECK (OP_TP IN (0,1,2,3)) DISABLE,
    TB_ROW_ID RAW(16),
    IS_CK NUMBER(1) DEFAULT 0,
  DES_STA NUMBER(1) DEFAULT 0 NOT NULL
        CHECK(DES_STA IN (0,1)) DISABLE,
    DES_DT TIMESTAMP,
    constraint FK_LOG_DETAIL_OP foreign key (OP_ID)
         references LOG_OPERATION (OP_ID) disable,
    constraint PK_LOG_DETAIL primary key (ROW_ID)
);
CREATE INDEX IX_LOG_DETAIL_OPID ON LOG_DETAIL(OP_ID);
CREATE INDEX IX_LOG_DETAIL_OBNM ON LOG_DETAIL(OB_NM);
CREATE INDEX IX_LOG_DETAIL_OBPID ON LOG_DETAIL(OB_PID);
CREATE INDEX IX_LOG_DETAIL_TBNM ON LOG_DETAIL(TB_NM);
CREATE INDEX IX_LOG_DETAIL_TBRID ON LOG_DETAIL(TB_ROW_ID);

create table LOG_DETAIL_GRID (
  LOG_ROW_ID RAW(16) NOT NULL,
  GRID_ID NUMBER(10) NOT NULL,
  GRID_TYPE NUMBER(1) NOT NULL
      CHECK (GRID_TYPE IN (0,1)) DISABLE,
  constraint FK_LOG_DETAIL_GRID_ROWID foreign key (LOG_ROW_ID)
        references LOG_DETAIL (ROW_ID) disable
);
CREATE INDEX IX_LOG_DET_GRID_ROW ON LOG_DETAIL_GRID(LOG_ROW_ID);
CREATE INDEX IX_LOG_DET_GRID_1 ON LOG_DETAIL_GRID(GRID_ID);
CREATE INDEX IX_LOG_DET_GRID_2 ON LOG_DETAIL_GRID(GRID_TYPE);
-- Create table
CREATE TABLE LOG_DAY_RELEASE
(
  OP_ID        RAW(16) NOT NULL,
  REL_POI_STA  NUMBER(1) DEFAULT 0 NOT NULL,
  REL_POI_DT   TIMESTAMP(6),
  REL_ALL_STA  NUMBER(1) DEFAULT 0 NOT NULL,
  REL_ALL_DT   TIMESTAMP(6),
  REL_POI_LOCK NUMBER(1) DEFAULT 0 NOT NULL,
  REL_ALL_LOCK NUMBER(1) DEFAULT 0 NOT NULL,
  CONSTRAINT PK_LOG_RELEASE PRIMARY KEY(OP_ID)
);
-- Add comments to the columns 
COMMENT ON TABLE LOG_DAY_RELEASE IS '日库出品管理表';
COMMENT ON COLUMN LOG_DAY_RELEASE.OP_ID
  IS '参考log_operation.op_id';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_STA
  IS 'POI出品状态';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_DT
  IS 'POI出品时间';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_STA
  IS 'POI+ROAD出品状态';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_DT
  IS 'POI+ROAD出品时间';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_LOCK
  IS 'POI 出品锁状态';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_LOCK
  IS 'POI+ROAD出品锁状态';
--ADD INDEXES
create index IDX_LOG_DAY_REL_1 on LOG_DAY_RELEASE (rel_poi_sta);
create index IDX_LOG_DAY_REL_2 on LOG_DAY_RELEASE (rel_all_sta);
create index IDX_LOG_DAY_REL_3 on LOG_DAY_RELEASE (rel_poi_lock);
create index IDX_LOG_DAY_REL_4 on LOG_DAY_RELEASE (rel_all_lock);


/* ck */

CREATE TABLE CK_EXCEPTION_GRID(
  CK_ROW_ID RAW(16) NOT NULL,
  GRID_ID NUMBER(10) NOT NULL
);
CREATE INDEX IX_CK_EXCEP_GRID_01 ON CK_EXCEPTION_GRID(CK_ROW_ID);

CREATE UNIQUE INDEX IX_NI_VAL_MD5 ON NI_VAL_EXCEPTION(MD5_CODE);
create table CK_RESULT_OBJECT (
  MD5_CODE VARCHAR2(32) not null,
  TABLE_NAME VARCHAR2(32) not null,
  PID NUMBER(10) default 0 not null
);
CREATE INDEX IX_CK_RESULT_OBJ_MD5 ON CK_RESULT_OBJECT(MD5_CODE);
create index IX_CK_RESULT_OBJECT_01 on CK_RESULT_OBJECT (PID);

CREATE TABLE NI_VAL_EXCEPTION_GRID(
  MD5_CODE VARCHAR2(32) NOT NULL,
  GRID_ID NUMBER(10) NOT NULL
);
CREATE INDEX IX_NI_VAL_GRID_01 ON NI_VAL_EXCEPTION_GRID(MD5_CODE);

-- RD_TOLLGATE_MAPPING,RD_TOLLGATE_FEE,CK_EXCEPTION,NI_VAL_EXCEPTION
ALTER TABLE RD_TOLLGATE_MAPPING ADD (U_RECORD NUMBER(2) default 0 not null check (U_RECORD in (0,1,2,3)),U_DATE VARCHAR2(14),ROW_ID RAW(16));
ALTER TABLE RD_TOLLGATE_FEE ADD (U_RECORD NUMBER(2) default 0 not null check (U_RECORD in (0,1,2,3)),U_DATE VARCHAR2(14),ROW_ID RAW(16));





/*==============================================================*/
/* Table: NI_VAL_EXCEPTION_HISTORY                                      */
/*==============================================================*/
create table NI_VAL_EXCEPTION_HISTORY  (
   VAL_EXCEPTION_ID     NUMBER(10)                     default 0 not null,
   RULEID               VARCHAR2(100),
   TASK_NAME            VARCHAR2(50),
   GROUPID              NUMBER(10)                     default 0 not null,
   "LEVEL"              NUMBER(10)                     default 0 not null,
   SITUATION            VARCHAR2(4000),
   INFORMATION          VARCHAR2(4000),
   SUGGESTION           VARCHAR2(4000),
   LOCATION             SDO_GEOMETRY,
   TARGETS              CLOB,
   ADDITION_INFO        CLOB,
   DEL_FLAG             NUMBER(1)                      default 0 not null
       check (DEL_FLAG in (0,1)),
   CREATED              DATE,
   UPDATED              DATE,
   MESH_ID              NUMBER(8),
   SCOPE_FLAG           NUMBER(2)                      default 1 not null
       check (SCOPE_FLAG in (1,2,3)),
   PROVINCE_NAME        VARCHAR2(60),
   MAP_SCALE            NUMBER(2)                      default 0 not null
       check (MAP_SCALE in (0,1,2,3)),
   RESERVED             VARCHAR2(1000),
   EXTENDED             VARCHAR2(1000),
   TASK_ID              VARCHAR2(500),
   QA_TASK_ID           VARCHAR2(500),
   QA_STATUS            NUMBER(2)                      default 2 not null
       check (QA_STATUS in (1,2)),
   WORKER               VARCHAR2(500),
   QA_WORKER            VARCHAR2(500),
   LOG_TYPE             NUMBER(5)                      default 0 not null,
   MD5_CODE             VARCHAR2(32)
);

comment on column NI_VAL_EXCEPTION_HISTORY.RULEID is
'参考"CK_RULE"';

comment on column NI_VAL_EXCEPTION_HISTORY.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column NI_VAL_EXCEPTION_HISTORY.UPDATED is
'格式"YYYY/MM/DD HH:mm:ss"';
CREATE UNIQUE INDEX IX_NIVAL_HIS_MD5 ON NI_VAL_EXCEPTION_HISTORY(MD5_CODE);
CREATE TABLE NI_VAL_EXCEPTION_HISTORY_GRID(
  MD5_CODE VARCHAR2(32) NOT NULL,
  GRID_ID NUMBER(10) NOT NULL
);
CREATE INDEX IX_NI_VAL_HIS_GRID_01 ON NI_VAL_EXCEPTION_HISTORY_GRID(MD5_CODE);
-- SVR INNER TABLE
CREATE TABLE SVR_MULTISRC_DAY_IMP(
FID VARCHAR2(36),
START_DATE DATE,
END_DATE DATE
);

create table DATA_PLAN
(
  pid              NUMBER(10) not null,
  data_type        NUMBER(1),
  is_plan_selected NUMBER(1) default 1,
  task_id          NUMBER(10),
  is_important     NUMBER(1) default 0,
  operate_date TIMESTAMP(6)
);
comment on table DATA_PLAN
  is '日库外业规划数据表';
-- Add comments to the columns 
comment on column DATA_PLAN.data_type
  is '数据类型，1 poi 2road ';
comment on column DATA_PLAN.is_plan_selected
  is '是否规划选中状态 0非选中状态，1选中状态';
comment on column DATA_PLAN.is_important
  is '是否重要poi 0非重要poi，1重要poi';
comment on column DATA_PLAN.operate_date
  is '规划时间';


create table  IX_POI_FLAG_METHOD(
   POI_PID              NUMBER(10)                      not null,
   VER_RECORD         NUMBER(1)                      default 0 not null,
   SRC_RECORD         NUMBER(1)                      default 0 not null,
   SRC_NAME_CH         NUMBER(1)                      default 0 not null,
   SRC_ADDRESS         NUMBER(1)                      default 0 not null,
   SRC_TELEPHONE         NUMBER(1)                      default 0 not null,
   SRC_COORDINATE         NUMBER(1)                      default 0 not null,
   SRC_NAME_ENG         NUMBER(1)                      default 0 not null,
   SRC_NAME_POR        NUMBER(1)                      default 0 not null,
   FIELD_VERIFIED         NUMBER(1)                      default 0 not null,
   REFRESH_CYCLE        NUMBER(3)                      default 0 not null,
   REFRESH_DATE           VARCHAR2(14)      ,
   U_RECORD         NUMBER(2)     DEFAULT 0 NOT NULL,
   U_FIELDS          VARCHAR2(1000)  ,
   U_DATE            VARCHAR2(14)  ,
   ROW_ID            RAW(16)  
);

comment on column IX_POI_FLAG_METHOD.VER_RECORD is '0 无;1 外业;2  内业;3  多源;4  众包;5  代理店 0';
comment on column IX_POI_FLAG_METHOD.SRC_RECORD is '0 无;1 外业;2  内业;3  多源;4  众包;5  代理店 0';
comment on column IX_POI_FLAG_METHOD.SRC_NAME_CH is '0  无;1 外业;2  内业;3  多源;4  众包;5  代理店 0';
comment on column IX_POI_FLAG_METHOD.SRC_ADDRESS is '0  无;1 外业;2  内业;3  多源;4  众包;5  代理店 0';
comment on column IX_POI_FLAG_METHOD.SRC_TELEPHONE is '0  无;1 外业;2  内业;3  多源;4  众包;5  代理店 0';
comment on column IX_POI_FLAG_METHOD.SRC_COORDINATE is '0 无;1 外业;2  内业;3  多源;4  众包;5  代理店 0';
comment on column IX_POI_FLAG_METHOD.SRC_NAME_ENG is '0 无;1 采集（大陆与港澳）;2 官方网站搜集;3  非官方网站搜集，网站搜集后+分店名;4 基于网站搜集（专项改善））;5 品牌名/分类名+分店名翻译;6 关键词翻译程序+人工确认;7  各项目代理店;8  已训练关键词翻译程序;9  未训练关键词翻译程序  ';
comment on column IX_POI_FLAG_METHOD.SRC_NAME_POR is '0 无;1 采集（大陆与港澳）;2 官方网站搜集;3  非官方网站搜集，网站搜集后+分店名;4 基于网站搜集（专项改善））;5 品牌名/分类名+分店名翻译;6 关键词翻译程序+人工确认;7  各项目代理店;8  已训练关键词翻译程序;9  未训练关键词翻译程序  ';
comment on column IX_POI_FLAG_METHOD.FIELD_VERIFIED is '0 否;1 是';
comment on column IX_POI_FLAG_METHOD.REFRESH_CYCLE is '0  否;1 是';
comment on column IX_POI_FLAG_METHOD.REFRESH_DATE is '格式： YYYYMMDDHHMMSS 如 20140812152100；24小时制。  空';
comment on column IX_POI_FLAG_METHOD.U_RECORD is '增量更新标识,值域包括:0	无;1	新增;2	删除 ;3	修改';
comment on column IX_POI_FLAG_METHOD.U_FIELDS is '记录更新的英文字段名,多个之间采用半角"|"分隔 ';
comment on column IX_POI_FLAG_METHOD.REFRESH_DATE is '格式： YYYYMMDDHHMMSS 如 20140812152100；24小时制。';
comment on column IX_POI_FLAG_METHOD.REFRESH_DATE is '唯一标识一条记录的ID ';
create table LINK_EDIT_PRE
(
  pid          NUMBER(10) not null,
  scenario     NUMBER(1),
  operate_date TIMESTAMP(6)
);
-- Add comments to the table 
comment on table LINK_EDIT_PRE
  is '采集场景专题图表';
-- Add comments to the columns 
comment on column LINK_EDIT_PRE.scenario
  is '0 不参与计算道路1 步采一体化2 车采一体化3 图像工艺（预留暂不开放）4 1+0工艺（预留暂不开放）';
  
create index data_plan_index1 on DATA_PLAN (data_type, task_id, is_plan_selected);
create index DD on LINK_EDIT_PRE (PID);

create table POINTADDRESS_EDIT_STATUS
(
  PID               NUMBER(10) not null,
  STATUS            NUMBER(1) default 0 not null,
  IS_UPLOAD         NUMBER(1) default 0 not null,
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
comment on column POINTADDRESS_EDIT_STATUS.IS_UPLOAD
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
