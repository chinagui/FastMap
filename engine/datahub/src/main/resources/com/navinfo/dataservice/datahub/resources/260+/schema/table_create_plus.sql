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
  is '����';
comment on column POI_DEEP_WORKITEM_CONF.FIRST_WORK_ITEM
  is 'һ����ҵ��:poi_name-�������,poi_address-���ĵ�ַ,poi_englishname-Ӣ�����,poi_englishaddress-Ӣ�ĵ�ַ';
comment on column POI_DEEP_WORKITEM_CONF.SECOND_WORK_ITEM
  is '������ҵ��:nameUnify-���ͳһ,shortName-�����ҵ,namePinyin-���ƴ����ҵ,addrSplit-��ַ�����ҵ,addrPinyin-��ַƴ����ҵ,photoEngName-��Ƭ¼��Ӣ������ҵ,chiEngName-���ļ���Ӣ����ҵ,confirmEngName-�˹�ȷ��Ӣ������ҵ,officalStandardEngName-�ٷ���׼��Ӣ����ҵ,nonImportantLongEngName-����Ҫ����Ӣ������ҵ,engMapAddress-Ӣ�İ��ͼ��ҵ,nonImportantLongEngAddress-����Ҫ����Ӣ�ĵ�ַ������ҵ,engNameInvalidChar-Ӣ����Ƿ��ַ���,portuNameInvalidChar-������Ƿ��ַ���,macaoEngName-����Ӣ������ҵ,officalStandardPortuName-�ٷ���׼��������ҵ,engAddrInvalidChar-Ӣ�ĵ�ַ�Ƿ��ַ���,portuAddrInvalidChar-���ĵ�ַ�Ƿ��ַ���,longEngAddress-Ӣ�ĵ�ַ������ҵ,longPortuAddress-���ĵ�ַ������ҵ,';
comment on column POI_DEEP_WORKITEM_CONF.WORK_ITEM_ID
  is '��ҵ������';
comment on column POI_DEEP_WORKITEM_CONF.TYPE
  is '1�����½,2����۰�';

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
  is '����';
comment on column POI_DEEP_OP_CONF.FIRST_WORK_ITEM
  is 'һ����ҵ��:poi_name-�������,poi_address-���ĵ�ַ,poi_englishname-Ӣ�����,poi_englishaddress-Ӣ�ĵ�ַ';
comment on column POI_DEEP_OP_CONF.SECOND_WORK_ITEM
  is 'nameUnify-���ͳһ,shortName-�����ҵ,namePinyin-���ƴ����ҵ,addrSplit-��ַ�����ҵ,addrPinyin-��ַƴ����ҵ,photoEngName-��Ƭ¼��Ӣ������ҵ,chiEngName-���ļ���Ӣ����ҵ,confirmEngName-�˹�ȷ��Ӣ������ҵ,officalStandardEngName-�ٷ���׼��Ӣ����ҵ,nonImportantLongEngName-����Ҫ����Ӣ������ҵ,engMapAddress-Ӣ�İ��ͼ��ҵ,nonImportantLongEngAddress-����Ҫ����Ӣ�ĵ�ַ������ҵ,engNameInvalidChar-Ӣ����Ƿ��ַ���,portuNameInvalidChar-������Ƿ��ַ���,macaoEngName-����Ӣ������ҵ,officalStandardPortuName-�ٷ���׼��������ҵ,engAddrInvalidChar-Ӣ�ĵ�ַ�Ƿ��ַ���,portuAddrInvalidChar-���ĵ�ַ�Ƿ��ַ���,longEngAddress-Ӣ�ĵ�ַ������ҵ,longPortuAddress-���ĵ�ַ������ҵ';
comment on column POI_DEEP_OP_CONF.SAVE_EXEBATCH
  is '����ʱ�Ƿ�ִ������,0��  1��';
comment on column POI_DEEP_OP_CONF.SAVE_BATCHRULES
  is '����ʱҪִ�е������������,[]';
comment on column POI_DEEP_OP_CONF.SAVE_EXECHECK
  is '����ʱ�Ƿ�ִ�м��,0�� 1��';
comment on column POI_DEEP_OP_CONF.SAVE_CKRULES
  is '����ʱҪִ�еļ�����';
comment on column POI_DEEP_OP_CONF.SAVE_EXECLASSIFY
  is '����ʱ�Ƿ�ִ���ط���,0��   1��';
comment on column POI_DEEP_OP_CONF.SAVE_CLASSIFYRULES
  is '����ʱҪִ�е��ط������ []';
comment on column POI_DEEP_OP_CONF.SUBMIT_EXEBATCH
  is '�ύʱ�Ƿ�ִ������ 0��   1��';
comment on column POI_DEEP_OP_CONF.SUBMIT_BATCHRULES
  is '  �ύʱҪִ�е������������,[]';
comment on column POI_DEEP_OP_CONF.SUBMIT_EXECHECK
  is '�ύʱ�Ƿ�ִ�м��,0��   1��';
comment on column POI_DEEP_OP_CONF.SUBMIT_CKRULES
  is '�ύʱҪִ�еļ�����[]';
comment on column POI_DEEP_OP_CONF.SUBMIT_EXECLASSIFY
  is '�ύʱ�Ƿ�ִ���ط��� 0��   1��';
comment on column POI_DEEP_OP_CONF.SUBMIT_CLASSIFYRULES
  is '�ύʱҪִ�е��ط������[]';
comment on column POI_DEEP_OP_CONF.TYPE
  is '1�����½,2����۰�';

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
	OB_NM VARCHAR(30),
	OB_PID NUMBER(10),
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
COMMENT ON TABLE LOG_DAY_RELEASE IS '�����ճ�Ʒ״̬��';
COMMENT ON COLUMN LOG_DAY_RELEASE.OP_ID
  IS '���� ��Ӧ log_operation.op_id';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_STA
  IS 'POI ��Ʒ״̬��0 ���� 1����';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_DT
  IS 'POI��Ʒʱ��';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_STA
  IS 'POI+ROAD ��Ʒ״̬��0 ���� 1����';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_DT
  IS 'POI+ROAD ��Ʒʱ��';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_POI_LOCK
  IS 'POI ��Ʒ�� 0 ���� 1����';
COMMENT ON COLUMN LOG_DAY_RELEASE.REL_ALL_LOCK
  IS 'POI+ROAD ��Ʒ��0 ���� 1����';
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

