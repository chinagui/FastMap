/* GDB+ log part */
create table LOG_OPERATION (
    OP_ID VARCHAR2(32) NOT NULL,
    US_ID NUMBER(36) DEFAULT 0,
    OP_CMD VARCHAR2(200),
    OP_DT TIMESTAMP,
    OP_SG NUMBER(1) DEFAULT 0 not null
        check (OP_SG in (0,1,2,3,4,5)) disable,
    constraint PK_LOG_OP primary key (OP_ID)
);

create table LOG_DETAIL (
	ROW_ID RAW(16) NOT NULL,
    OP_ID VARCHAR2(32) NOT NULL,
    OB_NM VARCHAR2(30),
    OB_PK VARCHAR2(30),
    OB_PID NUMBER(10) DEFAULT 0,
    OPB_TP NUMBER(1) DEFAULT 0 NOT NULL
        CHECK (OPB_TP IN (0,1,2,3)) DISABLE,
    OB_TP NUMBER(1) DEFAULT 1 NOT NULL
        CHECK (OB_TP IN (1,2)) DISABLE,
    OP_DT TIMESTAMP,
    TB_NM VARCHAR2(30),
    OLD CLOB,
    NEW CLOB,
    FD_LST VARCHAR2(1000),
    OP_TP NUMBER(1) DEFAULT 0 NOT NULL
        CHECK (OP_TP IN (0,1,2,3)) DISABLE,
    TB_ROW_ID RAW(16),
    IS_CK NUMBER(1) DEFAULT 0,
    MESH_ID NUMBER(6),
    GRID_ID NUMBER(10),
    COM_DT TIMESTAMP,
    COM_STA NUMBER(1) default 0 not null
        check (COM_STA IN (0,1)) DISABLE,
    constraint FK_LOG_DETAIL_OP foreign key (OP_ID)
         references LOG_OPERATION (OP_ID) disable,
    constraint PK_LOG_DETAIL primary key (ROW_ID)
);

create table LOG_DETAIL_GRID (
	ROW_ID RAW(16) NOT NULL,
	GRID_ID NUMBER(10),
	GRID_TYPE NUMBER(1),
	constraint FK_LOG_DETAIL_GRID_ROWID foreign key (ROW_ID)
        references LOG_DETAIL (ROW_ID) disable
);

/*==============================================================*/
/* Table: NI_VAL_EXCEPTION_HISTORY                              */
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
       check (DEL_FLAG in (0,1)) disable,
   CREATED              DATE,
   UPDATED              DATE,
   MESH_ID              NUMBER(6),
   SCOPE_FLAG           NUMBER(2)                      default 1 not null
       check (SCOPE_FLAG in (1,2,3)) disable,
   PROVINCE_NAME        VARCHAR2(60),
   MAP_SCALE            NUMBER(2)                      default 0 not null
       check (MAP_SCALE in (0,1,2,3)) disable,
   RESERVED             VARCHAR2(32),
   EXTENDED             VARCHAR2(1000),
   TASK_ID              VARCHAR2(500),
   QA_TASK_ID           VARCHAR2(500),
   QA_STATUS            NUMBER(2)                      default 2 not null
       check (QA_STATUS in (1,2))disable,
   WORKER               VARCHAR2(500),
   QA_WORKER            VARCHAR2(500),
   LOG_TYPE             NUMBER(5)                      default 0 not null,
   U_DATE           	VARCHAR2(14),
   ROW_ID           	RAW(16)
);

create table CK_RESULT_OBJECT (
	CK_RESULT_ID VARCHAR2(32) not null,
	TABLE_NAME VARCHAR2(32) not null,
	PID NUMBER(10) default 0 not null
);
create index IX_CK_RESULT_OBJECT_01 on CK_RESULT_OBJECT (PID);
