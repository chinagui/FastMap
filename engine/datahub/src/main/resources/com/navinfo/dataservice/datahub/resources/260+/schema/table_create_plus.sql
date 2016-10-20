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
   SPLIT_FLAG           NUMBER(2)      default 0
check (SPLIT_FLAG in (0,1,2)) disable ,
   MEMO                 VARCHAR2(200),
   ROUTE_ID             NUMBER(10),
   PROCESS_FLAG         NUMBER(1)      default 0,
   U_RECORD             NUMBER(2),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NAME primary key (NAME_ID)
);

/* GDB+ POI EDIT PART */
CREATE TABLE POI_EDIT_STATUS(
  ROW_ID RAW(16) NOT NULL,
  STATUS NUMBER(1) DEFAULT 0
      CHECK(STATUS IN (0,1,2,3)) DISABLE,
  IS_UPLOAD NUMBER(1) DEFAULT 0
      CHECK(IS_UPLOAD IN (0,1)) DISABLE,
  UPLOAD_DATE TIMESTAMP,
  FRESH_VERIFIED NUMBER(1) DEFAULT 0
    CHECK(FRESH_VERIFIED IN (0,1)) DISABLE,
  RAW_FIELDS VARCHAR2(30)
);
CREATE UNIQUE INDEX IDX_POI_EDIT_STATUS_1 ON POI_EDIT_STATUS(ROW_ID);

CREATE TABLE POI_DEEP_STATUS(
  ROW_ID RAW(16) NOT NULL,
  TYPE NUMBER(1) DEFAULT 1 NOT NULL,
  STATUS NUMBER(1) DEFAULT 1,
  CONSTRAINT PK_POI_DEEP_STATUS PRIMARY KEY(ROW_ID,TYPE)
);
create table POI_DEEP_WORKITEM_CONF
(
  ID               VARCHAR2(100) not null,
  FIRST_WORK_ITEM  VARCHAR2(50),
  SECOND_WORK_ITEM VARCHAR2(50),
  WORK_ITEM_ID     VARCHAR2(50),
  TYPE             NUMBER(1)
);
-- Add comments to the columns 
comment on column POI_DEEP_WORKITEM_CONF.ID
  is '主键';
comment on column POI_DEEP_WORKITEM_CONF.FIRST_WORK_ITEM
  is '一级作业项:poi_name-中文名称,poi_address-中文地址,poi_englishname-英文名称,poi_englishaddress-英文地址';
comment on column POI_DEEP_WORKITEM_CONF.SECOND_WORK_ITEM
  is '二级作业项:nameUnify-名称统一,shortName-简称作业,namePinyin-名称拼音作业,addrSplit-地址拆分作业,addrPinyin-地址拼音作业,photoEngName-照片录入英文名作业,chiEngName-中文既是英文作业,confirmEngName-人工确认英文名作业,officalStandardEngName-官方标准化英文作业,nonImportantLongEngName-非重要分类英文名超长作业,engMapAddress-英文版地图作业,nonImportantLongEngAddress-非重要分类英文地址超长作业,engNameInvalidChar-英文名非法字符检查,portuNameInvalidChar-葡文名非法字符检查,macaoEngName-澳门英文名作业,officalStandardPortuName-官方标准化葡文作业,engAddrInvalidChar-英文地址非法字符检查,portuAddrInvalidChar-葡文地址非法字符检查,longEngAddress-英文地址超长作业,longPortuAddress-葡文地址超长作业,';
comment on column POI_DEEP_WORKITEM_CONF.WORK_ITEM_ID
  is '作业项规则号';
comment on column POI_DEEP_WORKITEM_CONF.TYPE
  is '1常规大陆,2常规港澳';

create table POI_DEEP_OP_CONF
(
  ID                   VARCHAR2(100) not null,
  FIRST_WORK_ITEM      VARCHAR2(50),
  SECOND_WORK_ITEM     VARCHAR2(50),
  SAVE_EXEBATCH        NUMBER(1) default 0,
  SAVE_BATCHRULES      VARCHAR2(100),
  SAVE_EXECHECK        NUMBER(1) default 0,
  SAVE_CKRULES         VARCHAR2(100),
  SAVE_EXECLASSIFY     NUMBER(1) default 0,
  SAVE_CLASSIFYRULES   VARCHAR2(100),
  SUBMIT_EXEBATCH      NUMBER(1) default 0,
  SUBMIT_BATCHRULES    VARCHAR2(100),
  SUBMIT_EXECHECK      NUMBER(1) default 0,
  SUBMIT_CKRULES       VARCHAR2(100),
  SUBMIT_EXECLASSIFY   NUMBER(1) default 0,
  SUBMIT_CLASSIFYRULES VARCHAR2(100),
  TYPE                 NUMBER(1)
);
-- Add comments to the columns 
comment on column POI_DEEP_OP_CONF.ID
  is '主键';
comment on column POI_DEEP_OP_CONF.FIRST_WORK_ITEM
  is '一级作业项:poi_name-中文名称,poi_address-中文地址,poi_englishname-英文名称,poi_englishaddress-英文地址';
comment on column POI_DEEP_OP_CONF.SECOND_WORK_ITEM
  is 'nameUnify-名称统一,shortName-简称作业,namePinyin-名称拼音作业,addrSplit-地址拆分作业,addrPinyin-地址拼音作业,photoEngName-照片录入英文名作业,chiEngName-中文既是英文作业,confirmEngName-人工确认英文名作业,officalStandardEngName-官方标准化英文作业,nonImportantLongEngName-非重要分类英文名超长作业,engMapAddress-英文版地图作业,nonImportantLongEngAddress-非重要分类英文地址超长作业,engNameInvalidChar-英文名非法字符检查,portuNameInvalidChar-葡文名非法字符检查,macaoEngName-澳门英文名作业,officalStandardPortuName-官方标准化葡文作业,engAddrInvalidChar-英文地址非法字符检查,portuAddrInvalidChar-葡文地址非法字符检查,longEngAddress-英文地址超长作业,longPortuAddress-葡文地址超长作业';
comment on column POI_DEEP_OP_CONF.SAVE_EXEBATCH
  is '保存时是否执行批处理,0否  1是';
comment on column POI_DEEP_OP_CONF.SAVE_BATCHRULES
  is '保存时要执行的批处理规则数组,[]';
comment on column POI_DEEP_OP_CONF.SAVE_EXECHECK
  is '保存时是否执行检查,0否 1是';
comment on column POI_DEEP_OP_CONF.SAVE_CKRULES
  is '保存时要执行的检查规则';
comment on column POI_DEEP_OP_CONF.SAVE_EXECLASSIFY
  is '保存时是否执行重分类,0否   1是';
comment on column POI_DEEP_OP_CONF.SAVE_CLASSIFYRULES
  is '保存时要执行的重分类规则 []';
comment on column POI_DEEP_OP_CONF.SUBMIT_EXEBATCH
  is '提交时是否执行批处理 0否   1是';
comment on column POI_DEEP_OP_CONF.SUBMIT_BATCHRULES
  is '  提交时要执行的批处理规则数组,[]';
comment on column POI_DEEP_OP_CONF.SUBMIT_EXECHECK
  is '提交时是否执行检查,0否   1是';
comment on column POI_DEEP_OP_CONF.SUBMIT_CKRULES
  is '提交时要执行的检查规则[]';
comment on column POI_DEEP_OP_CONF.SUBMIT_EXECLASSIFY
  is '提交时是否执行重分类 0否   1是';
comment on column POI_DEEP_OP_CONF.SUBMIT_CLASSIFYRULES
  is '提交时要执行的重分类规则[]';
comment on column POI_DEEP_OP_CONF.TYPE
  is '1常规大陆,2常规港澳';

/* GDB+ log part */
create table LOG_OPERATION (
    OP_ID RAW(16) NOT NULL,
    US_ID NUMBER(36) DEFAULT 0,
    OP_CMD VARCHAR2(200),
    OP_DT TIMESTAMP,
    OP_SG NUMBER(1) DEFAULT 0 not null
        check (OP_SG in (0,1,2,3,4,5)) disable,
    COM_STA NUMBER(1) DEFAULT 0 NOT NULL
        CHECK(COM_STA IN (0,1)) DISABLE,
    COM_DT TIMESTAMP,
    LOCK_STA NUMBER(1) DEFAULT 0 NOT NULL
        CHECK(LOCK_STA IN (0,1)) DISABLE,
    constraint PK_LOG_OP primary key (OP_ID)
);

create table LOG_DETAIL (
  ROW_ID RAW(16) NOT NULL,
    OP_ID RAW(16) NOT NULL,
	OBJ_NM VARCHAR(30),
	OBJ_PID NUMBER(10),
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

create table LOG_DETAIL_GRID (
  LOG_ROW_ID RAW(16) NOT NULL,
  GRID_ID NUMBER(10) NOT NULL,
  GRID_TYPE NUMBER(1) NOT NULL
      CHECK (GRID_TYPE IN (0,1)) DISABLE,
  constraint FK_LOG_DETAIL_GRID_ROWID foreign key (LOG_ROW_ID)
        references LOG_DETAIL (ROW_ID) disable
);
CREATE INDEX IX_LOG_DET_GRID_ROW ON LOG_DETAIL_GRID(LOG_ROW_ID);
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
COMMENT ON TABLE LOG_DAY_RELEASE IS '履历日出品状态表';
COMMENT ON COLUMN LOG_DAY_RELEASE.OP_ID
  IS '主键 对应 log_operation.op_id';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_STA
  IS 'POI 出品状态，0 ：否 1：是';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_DT
  IS 'POI出品时间';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_STA
  IS 'POI+ROAD 出品状态，0 ：否 1：是';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_DT
  IS 'POI+ROAD 出品时间';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_LOCK
  IS 'POI 出品锁 0 ：否 1：是';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_LOCK
  IS 'POI+ROAD 出品锁0 ：否 1：是';
--ADD INDEXES
create bitmap index IDX_LOG_DAY_REL_1 on LOG_DAY_RELEASE (rel_poi_sta);
create bitmap index IDX_LOG_DAY_REL_2 on LOG_DAY_RELEASE (rel_all_sta);
create bitmap index IDX_LOG_DAY_REL_3 on LOG_DAY_RELEASE (rel_poi_lock);
create bitmap index IDX_LOG_DAY_REL_4 on LOG_DAY_RELEASE (rel_all_lock);

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
CREATE TABLE NI_VAL_EXCEPTION_HISTORY AS SELECT * FROM NI_VAL_EXCEPTION WHERE 1=2;
CREATE UNIQUE INDEX IX_NIVAL_HIS_MD5 ON NI_VAL_EXCEPTION_HISTORY(MD5_CODE);

