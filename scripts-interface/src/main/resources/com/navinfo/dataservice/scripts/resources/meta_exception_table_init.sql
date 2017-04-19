/*==============================================================*/
/* Table: CK_EXCEPTION                                          */
/*==============================================================*/
create table CK_EXCEPTION  (
   EXCEPTION_ID         NUMBER(10)                      not null,
   RULE_ID              VARCHAR2(100),
   TASK_NAME            VARCHAR2(50),
   STATUS               NUMBER(2)                      default 0 not null
       check (STATUS in (0,1,2,3)),
   GROUP_ID             NUMBER(10)                     default 0 not null,
   RANK                 NUMBER(10)                     default 0 not null,
   SITUATION            VARCHAR2(4000),
   INFORMATION          VARCHAR2(4000),
   SUGGESTION           VARCHAR2(4000),
   GEOMETRY             VARCHAR2(4000),
   TARGETS              CLOB,
   ADDITION_INFO        CLOB,
   MEMO                 VARCHAR2(500),
   CREATE_DATE          DATE,
   UPDATE_DATE          DATE,
   MESH_ID              NUMBER(8)                       not null,
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
   MEMO_1               VARCHAR2(500),
   MEMO_2               VARCHAR2(500),
   MEMO_3               VARCHAR2(500),
   MD5_CODE             VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CK_EXCEPTION primary key (EXCEPTION_ID)
);

comment on column CK_EXCEPTION.EXCEPTION_ID is
'主键';

comment on column CK_EXCEPTION.RULE_ID is
'[173sp1]参考"CK_RULE"';

comment on column CK_EXCEPTION.STATUS is
'[1802A]';

comment on column CK_EXCEPTION.GEOMETRY is
'采用WKT 格式';

comment on column CK_EXCEPTION.MEMO is
'[1802A]';

comment on column CK_EXCEPTION.CREATE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CK_EXCEPTION.UPDATE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CK_EXCEPTION.QA_TASK_ID is
'[200A]';

comment on column CK_EXCEPTION.U_RECORD is
'增量更新标识';

comment on column CK_EXCEPTION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';


-----------------
/*==============================================================*/
/* Table: NI_VAL_EXCEPTION                                      */
/*==============================================================*/
create table NI_VAL_EXCEPTION  (
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

comment on column NI_VAL_EXCEPTION.RULEID is
'参考"CK_RULE"';

comment on column NI_VAL_EXCEPTION.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column NI_VAL_EXCEPTION.UPDATED is
'格式"YYYY/MM/DD HH:mm:ss"';

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

commit;
