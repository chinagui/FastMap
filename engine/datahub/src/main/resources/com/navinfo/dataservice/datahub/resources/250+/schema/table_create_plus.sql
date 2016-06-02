
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
   U_RECORD             NUMBER(2),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NAME primary key (NAME_ID)
);

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
    OB_NM VARCHAR2(30),
    OB_PK VARCHAR2(30),
    OB_PID NUMBER(10) DEFAULT 0,
    OPB_TP NUMBER(1) DEFAULT 0 NOT NULL
        CHECK (OPB_TP IN (0,1,2,3)) DISABLE,
    OB_TP NUMBER(1) DEFAULT 1 NOT NULL
        CHECK (OB_TP IN (1,2)) DISABLE,
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

create table LOG_DETAIL_GRID (
	LOG_ROW_ID RAW(16) NOT NULL,
	GRID_ID NUMBER(10) NOT NULL,
	GRID_TYPE NUMBER(1) NOT NULL
	    CHECK (GRID_TYPE IN (0,1)) DISABLE,
	constraint FK_LOG_DETAIL_GRID_ROWID foreign key (LOG_ROW_ID)
        references LOG_DETAIL (ROW_ID) disable
);

create table CK_RESULT_OBJECT (
	CK_RESULT_ID VARCHAR2(32) not null,
	TABLE_NAME VARCHAR2(32) not null,
	PID NUMBER(10) default 0 not null
);
create index IX_CK_RESULT_OBJECT_01 on CK_RESULT_OBJECT (PID);

CREATE TABLE CK_EXCEPTION_GRID(
	EXCEP_ROW_ID RAW(16) NOT NULL,
	GRID_ID NUMBER(10) NOT NULL
);
CREATE INDEX IX_CK_EXCEP_GRID_01 ON CK_EXCEPTION_GRID(EXCEP_ROW_ID);

CREATE TABLE NI_VAL_EXCEPTION_GRID(
	CK_RESULT_ID VARCHAR2(32) NOT NULL,
	GRID_ID NUMBER(10) NOT NULL
);
CREATE INDEX IX_NI_VAL_GRID_01 ON NI_VAL_EXCEPTION_GRID(CK_RESULT_ID);

-- RD_TOLLGATE_MAPPING,RD_TOLLGATE_FEE,CK_EXCEPTION,NI_VAL_EXCEPTION
ALTER TABLE RD_TOLLGATE_MAPPING ADD (U_RECORD NUMBER(2) default 0 not null check (U_RECORD in (0,1,2,3)),U_DATE VARCHAR2(14),ROW_ID RAW(16));
ALTER TABLE RD_TOLLGATE_FEE ADD (U_RECORD NUMBER(2) default 0 not null check (U_RECORD in (0,1,2,3)),U_DATE VARCHAR2(14),ROW_ID RAW(16));
ALTER TABLE CK_EXCEPTION ADD (U_RECORD NUMBER(2) default 0 not null check (U_RECORD in (0,1,2,3)),U_DATE VARCHAR2(14),ROW_ID RAW(16));
CREATE TABLE NI_VAL_EXCEPTION_HISTORY AS SELECT * FROM NI_VAL_EXCEPTION WHERE 1=2;
