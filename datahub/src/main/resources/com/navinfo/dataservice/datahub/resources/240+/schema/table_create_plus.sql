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
    TB_NM VARCHAR2(20),
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
         references LOG_OPERATION (OP_ID) disable
);
