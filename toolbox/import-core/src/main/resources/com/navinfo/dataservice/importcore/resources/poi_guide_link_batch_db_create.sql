create table NI_RD_NAME
(
  name_id         NUMBER(10) not null,
  name_groupid    NUMBER(10) not null,
  lang_code       VARCHAR2(3) not null,
  name            VARCHAR2(500) not null,
  type            VARCHAR2(100),
  base            VARCHAR2(100),
  prefix          VARCHAR2(100),
  infix           VARCHAR2(100),
  suffix          VARCHAR2(100),
  name_phonetic   VARCHAR2(1000),
  type_phonetic   VARCHAR2(1000),
  base_phonetic   VARCHAR2(1000),
  prefix_phonetic VARCHAR2(1000),
  infix_phonetic  VARCHAR2(1000),
  suffix_phonetic VARCHAR2(1000),
  src_flag        NUMBER(2),
  road_type       NUMBER(1) not null,
  admin_id        NUMBER(6) not null,
  code_type       NUMBER(1) not null,
  voice_file      VARCHAR2(100),
  src_resume      VARCHAR2(1000),
  pa_region_id    NUMBER(10),
  memo            VARCHAR2(200),
  route_id        NUMBER(10),
  u_record        NUMBER(2),
  u_fields        VARCHAR2(1000),
  split_flag      NUMBER(2) default 0,
  city            VARCHAR2(400),
  process_flag    NUMBER(1) default 0
);

alter table NI_RD_NAME
  add constraint NAME_01 primary key (NAME_ID)
  using index 
  tablespace GDB_DATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 80K
    next 1M
    minextents 1
    maxextents unlimited
  );

alter table NI_RD_NAME
  add constraint CKC_CODE_TYPE_RD_NAME
  check (CODE_TYPE in (0,1,2,3,4,5,6,7));
alter table NI_RD_NAME
  add constraint CKC_ROAD_TYPE_RD_NAME
  check (ROAD_TYPE in (0,1,2,3,4));
alter table NI_RD_NAME
  add constraint CKC_SPLIT_FLAG_RD_NAME
  check (SPLIT_FLAG is null or (SPLIT_FLAG in (0,1,2)));
alter table NI_RD_NAME
  add constraint CKC_SRC_FLAG_RD_NAME
  check (SRC_FLAG is null or (SRC_FLAG in (0,1,2,3)));
  

create table IX_POI
(
  PID             NUMBER(10) not null,
  KIND_CODE       VARCHAR2(8),
  GEOMETRY        SDO_GEOMETRY,
  X_GUIDE         NUMBER(10,5) default 0 not null,
  Y_GUIDE         NUMBER(10,5) default 0 not null,
  LINK_PID        NUMBER(10) default 0 not null,
  SIDE            NUMBER(1) default 0 not null,
  NAME_GROUPID    NUMBER(10) default 0 not null,
  ROAD_FLAG       NUMBER(1) default 0 not null,
  PMESH_ID        NUMBER(8) default 0 not null,
  ADMIN_REAL      NUMBER(6) default 0 not null,
  IMPORTANCE      NUMBER(1) default 0 not null,
  CHAIN           VARCHAR2(12),
  AIRPORT_CODE    VARCHAR2(3),
  ACCESS_FLAG     NUMBER(2) default 0 not null,
  OPEN_24H        NUMBER(1) default 0 not null,
  MESH_ID_5K      VARCHAR2(10),
  MESH_ID         NUMBER(8) default 0 not null,
  REGION_ID       NUMBER(10) default 0 not null,
  POST_CODE       VARCHAR2(6),
  EDIT_FLAG       NUMBER(1) default 1 not null,
  DIF_GROUPID     VARCHAR2(200),
  RESERVED        VARCHAR2(1000),
  STATE           NUMBER(1) default 0 not null,
  FIELD_STATE     VARCHAR2(500),
  LABEL           VARCHAR2(500),
  TYPE            NUMBER(1) default 0 not null,
  ADDRESS_FLAG    NUMBER(1) default 0 not null,
  EX_PRIORITY     VARCHAR2(10),
  EDITION_FLAG    VARCHAR2(12),
  POI_MEMO        VARCHAR2(200),
  OLD_BLOCKCODE   VARCHAR2(200),
  OLD_NAME        VARCHAR2(200),
  OLD_ADDRESS     VARCHAR2(200),
  OLD_KIND        VARCHAR2(8),
  POI_NUM         VARCHAR2(36),
  LOG             VARCHAR2(200),
  TASK_ID         NUMBER(10) default 0 not null,
  DATA_VERSION    VARCHAR2(128),
  FIELD_TASK_ID   NUMBER(10) default 0 not null,
  VERIFIED_FLAG   NUMBER(1) default 9 not null,
  COLLECT_TIME    VARCHAR2(15),
  GEO_ADJUST_FLAG NUMBER(1) default 9 not null,
  FULL_ATTR_FLAG  NUMBER(1) default 9 not null,
  OLD_X_GUIDE     NUMBER(10,5) default 0 not null,
  OLD_Y_GUIDE     NUMBER(10,5) default 0 not null,
  TRUCK_FLAG      NUMBER(1) default 0 not null,
  "LEVEL"         VARCHAR2(2),
  SPORTS_VENUE    VARCHAR2(3),
  INDOOR          NUMBER(1) default 0 not null,
  VIP_FLAG        VARCHAR2(10),
  U_RECORD        NUMBER(2) default 0 not null,
  U_FIELDS        VARCHAR2(1000),
  U_DATE          VARCHAR2(14),
  ROW_ID          RAW(16)
);

alter table IX_POI
  add constraint PK_IX_POI primary key (PID);
alter table IX_POI
  add check (SIDE in (0,1,2,3));
alter table IX_POI
  add check (ROAD_FLAG in (0,1,2,3));
alter table IX_POI
  add check (IMPORTANCE in (0,1));
alter table IX_POI
  add check (ACCESS_FLAG in (0,1,2));
alter table IX_POI
  add check (OPEN_24H in (0,1,2));
alter table IX_POI
  add check (EDIT_FLAG in (0,1));
alter table IX_POI
  add check (STATE in (0,1,2,3));
alter table IX_POI
  add check (TYPE in (0,1));
alter table IX_POI
  add check (ADDRESS_FLAG in (0,1,9));
alter table IX_POI
  add check (VERIFIED_FLAG in (0,1,2,3,9));
alter table IX_POI
  add check (GEO_ADJUST_FLAG in (0,1,9));
alter table IX_POI
  add check (FULL_ATTR_FLAG in (0,1,9));
alter table IX_POI
  add check (TRUCK_FLAG in (0,1,2));
alter table IX_POI
  add check ("LEVEL" is null or ("LEVEL" in ('A','B1','B2','B3','B4','C')));
alter table IX_POI
  add check (INDOOR in (0,1));
alter table IX_POI
  add check (U_RECORD in (0,1,2,3));

create index EXP_IX_01 on IX_POI (MESH_ID, LINK_PID);
create index EXP_IX_1001 on IX_POI (POI_NUM);
create index EXP_IX_49 on IX_POI (KIND_CODE);
create index EXP_IX_D01 on IX_POI (CHAIN, TYPE);
create unique index IDX_20170414205940_R on IX_POI (ROW_ID);

create table IX_POI_ADDRESS
(
  NAME_ID           NUMBER(10) not null,
  NAME_GROUPID      NUMBER(10) default 0 not null,
  POI_PID           NUMBER(10) not null,
  LANG_CODE         VARCHAR2(3) default 'CHI' not null,
  SRC_FLAG          NUMBER(2) default 0 not null,
  FULLNAME          VARCHAR2(500),
  FULLNAME_PHONETIC VARCHAR2(1000),
  ROADNAME          VARCHAR2(500),
  ROADNAME_PHONETIC VARCHAR2(1000),
  ADDRNAME          VARCHAR2(500),
  ADDRNAME_PHONETIC VARCHAR2(1000),
  PROVINCE          VARCHAR2(64),
  CITY              VARCHAR2(64),
  COUNTY            VARCHAR2(64),
  TOWN              VARCHAR2(200),
  PLACE             VARCHAR2(100),
  STREET            VARCHAR2(100),
  LANDMARK          VARCHAR2(100),
  PREFIX            VARCHAR2(64),
  HOUSENUM          VARCHAR2(64),
  TYPE              VARCHAR2(32),
  SUBNUM            VARCHAR2(64),
  SURFIX            VARCHAR2(64),
  ESTAB             VARCHAR2(64),
  BUILDING          VARCHAR2(100),
  FLOOR             VARCHAR2(64),
  UNIT              VARCHAR2(64),
  ROOM              VARCHAR2(64),
  ADDONS            VARCHAR2(200),
  PROV_PHONETIC     VARCHAR2(1000),
  CITY_PHONETIC     VARCHAR2(1000),
  COUNTY_PHONETIC   VARCHAR2(1000),
  TOWN_PHONETIC     VARCHAR2(1000),
  STREET_PHONETIC   VARCHAR2(1000),
  PLACE_PHONETIC    VARCHAR2(1000),
  LANDMARK_PHONETIC VARCHAR2(1000),
  PREFIX_PHONETIC   VARCHAR2(1000),
  HOUSENUM_PHONETIC VARCHAR2(1000),
  TYPE_PHONETIC     VARCHAR2(1000),
  SUBNUM_PHONETIC   VARCHAR2(1000),
  SURFIX_PHONETIC   VARCHAR2(1000),
  ESTAB_PHONETIC    VARCHAR2(1000),
  BUILDING_PHONETIC VARCHAR2(1000),
  FLOOR_PHONETIC    VARCHAR2(1000),
  UNIT_PHONETIC     VARCHAR2(1000),
  ROOM_PHONETIC     VARCHAR2(1000),
  ADDONS_PHONETIC   VARCHAR2(1000),
  U_RECORD          NUMBER(2) default 0 not null,
  U_FIELDS          VARCHAR2(1000),
  U_DATE            VARCHAR2(14),
  ROW_ID            RAW(16)
);
 
alter table IX_POI_ADDRESS
  add constraint PK_IX_POI_ADDRESS primary key (NAME_ID);
alter table IX_POI_ADDRESS
  add constraint IXPOI_ADDRESS foreign key (POI_PID)
  references IX_POI (PID)
  disable;
alter table IX_POI_ADDRESS
  add check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR'));
alter table IX_POI_ADDRESS
  add check (SRC_FLAG in (0,1));
alter table IX_POI_ADDRESS
  add check (U_RECORD in (0,1,2,3));
create index EXP_IX_26 on IX_POI_ADDRESS (POI_PID);
create unique index IDX_20170414206192_R on IX_POI_ADDRESS (ROW_ID);


create table RD_LINK
(
  LINK_PID         NUMBER(10) not null,
  S_NODE_PID       NUMBER(10) not null,
  E_NODE_PID       NUMBER(10) not null,
  KIND             NUMBER(2) default 7 not null,
  DIRECT           NUMBER(1) default 1 not null,
  APP_INFO         NUMBER(1) default 1 not null,
  TOLL_INFO        NUMBER(1) default 2 not null,
  ROUTE_ADOPT      NUMBER(1) default 2 not null,
  MULTI_DIGITIZED  NUMBER(1) default 0 not null,
  DEVELOP_STATE    NUMBER(1) default 0 not null,
  IMI_CODE         NUMBER(1) default 0 not null,
  SPECIAL_TRAFFIC  NUMBER(1) default 0 not null,
  FUNCTION_CLASS   NUMBER(1) default 5 not null,
  URBAN            NUMBER(1) default 0 not null,
  PAVE_STATUS      NUMBER(1) default 0 not null,
  LANE_NUM         NUMBER(2) default 2 not null,
  LANE_LEFT        NUMBER(2) default 0 not null,
  LANE_RIGHT       NUMBER(2) default 0 not null,
  LANE_WIDTH_LEFT  NUMBER(1) default 1 not null,
  LANE_WIDTH_RIGHT NUMBER(1) default 1 not null,
  LANE_CLASS       NUMBER(1) default 2 not null,
  WIDTH            NUMBER(8) default 0 not null,
  IS_VIADUCT       NUMBER(1) default 0 not null,
  LEFT_REGION_ID   NUMBER(10) default 0 not null,
  RIGHT_REGION_ID  NUMBER(10) default 0 not null,
  GEOMETRY         SDO_GEOMETRY,
  LENGTH           NUMBER(15,3) default 0 not null,
  ONEWAY_MARK      NUMBER(2) default 0 not null,
  MESH_ID          NUMBER(8) default 0 not null,
  STREET_LIGHT     NUMBER(1) default 0 not null,
  PARKING_LOT      NUMBER(1) default 0 not null,
  ADAS_FLAG        NUMBER(1) default 0 not null,
  SIDEWALK_FLAG    NUMBER(1) default 0 not null,
  WALKSTAIR_FLAG   NUMBER(1) default 0 not null,
  DICI_TYPE        NUMBER(1) default 0 not null,
  WALK_FLAG        NUMBER(1) default 0 not null,
  DIF_GROUPID      VARCHAR2(200),
  SRC_FLAG         NUMBER(2) default 6 not null,
  DIGITAL_LEVEL    NUMBER(2) default 0 not null,
  EDIT_FLAG        NUMBER(1) default 1 not null,
  TRUCK_FLAG       NUMBER(1) default 0 not null,
  FEE_STD          NUMBER(5,2) default 0 not null,
  FEE_FLAG         NUMBER(1) default 0 not null,
  SYSTEM_ID        NUMBER(6) default 0 not null,
  ORIGIN_LINK_PID  NUMBER(10) default 0 not null,
  CENTER_DIVIDER   NUMBER(2) default 0 not null,
  PARKING_FLAG     NUMBER(1) default 0 not null,
  ADAS_MEMO        NUMBER(5) default 0 not null,
  MEMO             VARCHAR2(200),
  RESERVED         VARCHAR2(1000),
  U_RECORD         NUMBER(2) default 0 not null,
  U_FIELDS         VARCHAR2(1000),
  U_DATE           VARCHAR2(14),
  ROW_ID           RAW(16)
)
tablespace GDB_DATA
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


alter table RD_LINK
  add constraint PK_RD_LINK primary key (LINK_PID);
alter table RD_LINK
  add check (KIND in (0,1,2,3,4,5,6,7,8,9,10,11,13,15));
alter table RD_LINK
  add check (DIRECT in (0,1,2,3));
alter table RD_LINK
  add check (APP_INFO in (0,1,2,3,5));
alter table RD_LINK
  add check (TOLL_INFO in (0,1,2,3));
alter table RD_LINK
  add check (ROUTE_ADOPT in (0,1,2,3,4,5,9));
alter table RD_LINK
  add check (MULTI_DIGITIZED between 0 and 1 and MULTI_DIGITIZED in (0,1));
alter table RD_LINK
  add check (DEVELOP_STATE between 0 and 2 and DEVELOP_STATE in (0,1,2));
alter table RD_LINK
  add check (IMI_CODE between 0 and 3 and IMI_CODE in (0,1,2,3));
alter table RD_LINK
  add check (SPECIAL_TRAFFIC between 0 and 1 and SPECIAL_TRAFFIC in (0,1));
alter table RD_LINK
  add check (FUNCTION_CLASS between 0 and 5 and FUNCTION_CLASS in (0,1,2,3,4,5));
alter table RD_LINK
  add check (URBAN between 0 and 1 and URBAN in (0,1));
alter table RD_LINK
  add check (PAVE_STATUS between 0 and 1 and PAVE_STATUS in (0,1));
alter table RD_LINK
  add check (LANE_WIDTH_LEFT in (1,2,3));
alter table RD_LINK
  add check (LANE_WIDTH_RIGHT in (1,2,3));
alter table RD_LINK
  add check (LANE_CLASS between 0 and 3 and LANE_CLASS in (0,1,2,3));
alter table RD_LINK
  add check (IS_VIADUCT between 0 and 2 and IS_VIADUCT in (0,1,2));
alter table RD_LINK
  add check (ONEWAY_MARK in (0,1));
alter table RD_LINK
  add check (STREET_LIGHT between 0 and 2 and STREET_LIGHT in (0,1,2));
alter table RD_LINK
  add check (PARKING_LOT in (0,1,2));
alter table RD_LINK
  add check (ADAS_FLAG in (0,1,2));
alter table RD_LINK
  add check (SIDEWALK_FLAG in (0,1,2));
alter table RD_LINK
  add check (WALKSTAIR_FLAG in (0,1,2));
alter table RD_LINK
  add check (DICI_TYPE in (0,1,2));
alter table RD_LINK
  add check (WALK_FLAG in (0,1,2));
alter table RD_LINK
  add check (SRC_FLAG in (1,2,3,4,5,6));
alter table RD_LINK
  add check (DIGITAL_LEVEL in (0,1,2,3,4));
alter table RD_LINK
  add check (EDIT_FLAG in (0,1));
alter table RD_LINK
  add check (TRUCK_FLAG in (0,1));
alter table RD_LINK
  add check (FEE_FLAG in (0,1,2));
alter table RD_LINK
  add check (CENTER_DIVIDER in (0,10,11,12,13,20,21,30,31,40,50,51,60,61,62,63,99));
alter table RD_LINK
  add check (PARKING_FLAG in (0,1));
alter table RD_LINK
  add check (U_RECORD in (0,1,2,3));

create index EXP_RD_01 on RD_LINK (MESH_ID);
create index EXP_RD_1001 on RD_LINK (S_NODE_PID, LINK_PID);
create index EXP_RD_1002 on RD_LINK (E_NODE_PID, LINK_PID);
create index EXP_RD_D01 on RD_LINK (KIND, S_NODE_PID, E_NODE_PID);
create index EXP_RD_D02 on RD_LINK (KIND, LINK_PID);
create index EXP_RD_D03 on RD_LINK (LINK_PID, LENGTH);
create bitmap index EXP_RD_D05 on RD_LINK (DICI_TYPE);
create bitmap index EXP_RD_D06 on RD_LINK (PAVE_STATUS);
create bitmap index EXP_RD_D09 on RD_LINK (LANE_CLASS);
create bitmap index EXP_RD_D10 on RD_LINK (URBAN);
create bitmap index EXP_RD_D11 on RD_LINK (DIGITAL_LEVEL);
create bitmap index EXP_RD_D12 on RD_LINK (DEVELOP_STATE);
create bitmap index EXP_RD_D13 on RD_LINK (TOLL_INFO);
create bitmap index EXP_RD_D14 on RD_LINK (DIRECT);
create bitmap index EXP_RD_D15 on RD_LINK (MULTI_DIGITIZED);
create bitmap index EXP_RD_D16 on RD_LINK (IMI_CODE);
create bitmap index EXP_RD_D17 on RD_LINK (ROUTE_ADOPT);
create bitmap index EXP_RD_D18 on RD_LINK (APP_INFO);
create bitmap index EXP_RD_D19 on RD_LINK (FUNCTION_CLASS);
create bitmap index EXP_RD_D23 on RD_LINK (SIDEWALK_FLAG);
create bitmap index EXP_RD_D24 on RD_LINK (IS_VIADUCT);
create bitmap index EXP_RD_D25 on RD_LINK (WALK_FLAG);
create unique index IDX_20170414205991_R on RD_LINK (ROW_ID);
 
create table RD_LINK_FORM
(
  LINK_PID      NUMBER(10) not null,
  FORM_OF_WAY   NUMBER(2) default 1 not null,
  EXTENDED_FORM NUMBER(2) default 0 not null,
  AUXI_FLAG     NUMBER(2) default 0 not null,
  KG_FLAG       NUMBER(1) default 0 not null,
  U_RECORD      NUMBER(2) default 0 not null,
  U_FIELDS      VARCHAR2(1000),
  U_DATE        VARCHAR2(14),
  ROW_ID        RAW(16)
);

alter table RD_LINK_FORM
  add constraint RDLINK_FORM foreign key (LINK_PID)
  references RD_LINK (LINK_PID)
  disable;

alter table RD_LINK_FORM
  add check (FORM_OF_WAY in (0,1,2,10,11,12,13,14,15,16,17,18,20,21,22,23,24,30,31,32,33,34,35,36,37,38,39,43,48,49,50,51,52,53,54,57,60,80,81,82));
alter table RD_LINK_FORM
  add check (EXTENDED_FORM in (0,40,41,42));
alter table RD_LINK_FORM
  add check (AUXI_FLAG in (0,55,56,58,70,71,72,73,76,77));
alter table RD_LINK_FORM
  add check (KG_FLAG in (0,1,2));
alter table RD_LINK_FORM
  add check (U_RECORD in (0,1,2,3));

create index EXP_RD_60 on RD_LINK_FORM (LINK_PID);
create index EXP_RD_D04 on RD_LINK_FORM (LINK_PID, FORM_OF_WAY);
create unique index IDX_20170414206221_R on RD_LINK_FORM (ROW_ID);

create table IX_POI_FLAG
(
  POI_PID   NUMBER(10) not null,
  FLAG_CODE VARCHAR2(12),
  U_RECORD  NUMBER(2) default 0 not null,
  U_FIELDS  VARCHAR2(1000),
  U_DATE    VARCHAR2(14),
  ROW_ID    RAW(16)
)
tablespace GDB_DATA
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 80
    next 1
    minextents 1
    maxextents unlimited
  );

alter table IX_POI_FLAG
  add constraint IXPOI_FLAG foreign key (POI_PID)
  references IX_POI (PID)
  disable;

alter table IX_POI_FLAG
  add check (U_RECORD in (0,1,2,3));

create index EXP_IX_35 on IX_POI_FLAG (POI_PID)
  tablespace GDB_DATA
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
create unique index IDX_20170411032958_R on IX_POI_FLAG (ROW_ID)
  tablespace GDB_DATA
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
  

create table LOG_ACTION
(
  ACT_ID RAW(16) not null,
  US_ID  NUMBER(36) default 0,
  OP_CMD VARCHAR2(1000),
  SRC_DB NUMBER(1) default 2,
  STK_ID NUMBER(10) default 0
)
tablespace GDB_DATA
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

alter table LOG_ACTION
  add constraint PK_LOG_ACT primary key (ACT_ID)
  using index 
  tablespace GDB_DATA
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

create index IX_LOG_ACT_STKID on LOG_ACTION (STK_ID)
  tablespace GDB_DATA
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


create table LOG_OPERATION
(
  OP_ID    RAW(16) not null,
  ACT_ID   RAW(16) not null,
  OP_DT    TIMESTAMP(6),
  OP_SEQ   NUMBER(12) default 0 not null,
  COM_STA  NUMBER(1) default 0 not null,
  COM_DT   TIMESTAMP(6),
  LOCK_STA NUMBER(1) default 0 not null
)
tablespace GDB_DATA
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

alter table LOG_OPERATION
  add constraint PK_LOG_OP primary key (OP_ID)
  using index 
  tablespace GDB_DATA
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
alter table LOG_OPERATION
  add constraint FK_LOG_OP_ACT foreign key (ACT_ID)
  references LOG_ACTION (ACT_ID)
  disable;

alter table LOG_OPERATION
  add check (COM_STA IN (0,1))
  disable;
alter table LOG_OPERATION
  add check (LOCK_STA IN (0,1))
  disable;

create index IX_LOG_OP_DT on LOG_OPERATION (OP_DT)
  tablespace GDB_DATA
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

  
create table LOG_DETAIL
(
  ROW_ID    RAW(16) not null,
  OP_ID     RAW(16) not null,
  OB_NM     VARCHAR2(30),
  OB_PID    NUMBER(10) default 0,
  GEO_NM    VARCHAR2(30),
  GEO_PID   NUMBER(10) default 0,
  TB_NM     VARCHAR2(30),
  OLD       CLOB,
  NEW       CLOB,
  FD_LST    VARCHAR2(1000),
  OP_TP     NUMBER(1) default 0 not null,
  TB_ROW_ID RAW(16),
  IS_CK     NUMBER(1) default 0,
  DES_STA   NUMBER(1) default 0 not null,
  DES_DT    TIMESTAMP(6)
)
tablespace GDB_DATA
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

alter table LOG_DETAIL
  add constraint PK_LOG_DETAIL primary key (ROW_ID)
  using index 
  tablespace GDB_DATA
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
alter table LOG_DETAIL
  add constraint FK_LOG_DETAIL_OP foreign key (OP_ID)
  references LOG_OPERATION (OP_ID)
  disable;

alter table LOG_DETAIL
  add check (OP_TP IN (0,1,2,3))
  disable;
alter table LOG_DETAIL
  add check (DES_STA IN (0,1))
  disable;

create index IX_LOG_DETAIL_OPID on LOG_DETAIL (OP_ID)
  tablespace GDB_DATA
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




create table LOG_DAY_RELEASE
(
  OP_ID        RAW(16) not null,
  REL_POI_STA  NUMBER(1) default 0 not null,
  REL_POI_DT   TIMESTAMP(6),
  REL_ALL_STA  NUMBER(1) default 0 not null,
  REL_ALL_DT   TIMESTAMP(6),
  REL_POI_LOCK NUMBER(1) default 0 not null,
  REL_ALL_LOCK NUMBER(1) default 0 not null
)
tablespace GDB_DATA
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


alter table LOG_DAY_RELEASE
  add constraint PK_LOG_RELEASE primary key (OP_ID)
  using index 
  tablespace GDB_DATA
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

create index IDX_LOG_DAY_REL_1 on LOG_DAY_RELEASE (REL_POI_STA)
  tablespace GDB_DATA
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
create index IDX_LOG_DAY_REL_2 on LOG_DAY_RELEASE (REL_ALL_STA)
  tablespace GDB_DATA
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
create index IDX_LOG_DAY_REL_3 on LOG_DAY_RELEASE (REL_POI_LOCK)
  tablespace GDB_DATA
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
create index IDX_LOG_DAY_REL_4 on LOG_DAY_RELEASE (REL_ALL_LOCK)
  tablespace GDB_DATA
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


create table LOG_DETAIL_GRID
(
  LOG_ROW_ID RAW(16) not null,
  GRID_ID    NUMBER(10) not null,
  GRID_TYPE  NUMBER(1) not null
)
tablespace GDB_DATA
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

alter table LOG_DETAIL_GRID
  add constraint FK_LOG_DETAIL_GRID_ROWID foreign key (LOG_ROW_ID)
  references LOG_DETAIL (ROW_ID)
  disable;

alter table LOG_DETAIL_GRID
  add check (GRID_TYPE IN (0,1))
  disable;

create index IX_LOG_DET_GRID_ROW on LOG_DETAIL_GRID (LOG_ROW_ID)
  tablespace GDB_DATA
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


create table IX_POI_BACK
(
  PID             NUMBER(10) not null,
  X_GUIDE         NUMBER(10,5) default 0 not null,
  Y_GUIDE         NUMBER(10,5) default 0 not null,
  LINK_PID        NUMBER(10) default 0 not null,
  SIDE            NUMBER(1) default 0 not null,
  NAME_GROUPID    NUMBER(10) default 0 not null,
  PMESH_ID        NUMBER(8) default 0 not null
)
tablespace GDB_DATA
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 80
    next 1
    minextents 1
    maxextents unlimited
  );

alter table IX_POI_BACK
  add constraint PK_IX_POI_BACK primary key (PID)
  using index 
  tablespace GDB_DATA
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


create index EXP_IX_POI_BACK_01 on IX_POI_BACK (PMESH_ID, LINK_PID)
  tablespace GDB_DATA
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


create table IX_POI_FLAG_BACK
(
  POI_PID   NUMBER(10) not null,
  FLAG_CODE VARCHAR2(12),
  U_RECORD  NUMBER(2) default 0 not null,
  U_FIELDS  VARCHAR2(1000),
  U_DATE    VARCHAR2(14),
  ROW_ID    RAW(16)
)
tablespace GDB_DATA
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 80
    next 1
    minextents 1
    maxextents unlimited
  );

alter table IX_POI_FLAG_BACK
  add constraint IXPOI_FLAG_BACK foreign key (POI_PID)
  references IX_POI_BACK (PID)
  disable;

alter table IX_POI_FLAG_BACK
  add check (U_RECORD in (0,1,2,3));

create index EXP_IX_POI_FLAG_BACK_35 on IX_POI_FLAG_BACK (POI_PID)
  tablespace GDB_DATA
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
create unique index IDX_POI_FLAG_BACK_0508_R on IX_POI_FLAG_BACK (ROW_ID)
  tablespace GDB_DATA
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

create table RD_LINK_NAME
(
  LINK_PID     NUMBER(10) not null,
  NAME_GROUPID NUMBER(10) default 0 not null,
  SEQ_NUM      NUMBER(2) default 1 not null,
  NAME_CLASS   NUMBER(1) default 1 not null,
  INPUT_TIME   VARCHAR2(32),
  NAME_TYPE    NUMBER(2) default 0 not null,
  SRC_FLAG     NUMBER(1) default 9 not null,
  ROUTE_ATT    NUMBER(1) default 0 not null,
  CODE         NUMBER(1) default 0 not null,
  U_RECORD     NUMBER(2) default 0 not null,
  U_FIELDS     VARCHAR2(1000),
  U_DATE       VARCHAR2(14),
  ROW_ID       RAW(16)
)
tablespace GDB_DATA
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 80
    next 1
    minextents 1
    maxextents unlimited
  );


alter table RD_LINK_NAME
  add constraint RDLINK_NAMES foreign key (LINK_PID)
  references RD_LINK (LINK_PID)
  disable;

alter table RD_LINK_NAME
  add check (NAME_CLASS in (1,2,3));
alter table RD_LINK_NAME
  add check (NAME_TYPE in (0,1,2,3,4,5,6,7,8,9,14,15));
alter table RD_LINK_NAME
  add check (SRC_FLAG in (0,1,2,3,4,5,6,9));
alter table RD_LINK_NAME
  add check (ROUTE_ATT in (0,1,2,3,4,5,9));
alter table RD_LINK_NAME
  add check (CODE in (0,1,2,9));
alter table RD_LINK_NAME
  add check (U_RECORD in (0,1,2,3));

create index EXP_RD_52 on RD_LINK_NAME (LINK_PID, NAME_GROUPID)
  tablespace GDB_DATA
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
create unique index IDX_20170411032911_R on RD_LINK_NAME (ROW_ID)
  tablespace GDB_DATA
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

  
