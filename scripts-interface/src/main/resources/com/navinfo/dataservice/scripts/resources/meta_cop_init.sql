-- Create table
create table RD_NAME
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
-- Create/Recreate primary, unique and foreign key constraints 
alter table RD_NAME
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
-- Create/Recreate check constraints 
alter table RD_NAME
  add constraint CKC_CODE_TYPE_RD_NAME
  check (CODE_TYPE in (0,1,2,3,4,5,6,7));
alter table RD_NAME
  add constraint CKC_ROAD_TYPE_RD_NAME
  check (ROAD_TYPE in (0,1,2,3,4));
alter table RD_NAME
  add constraint CKC_SPLIT_FLAG_RD_NAME
  check (SPLIT_FLAG is null or (SPLIT_FLAG in (0,1,2)));
alter table RD_NAME
  add constraint CKC_SRC_FLAG_RD_NAME
  check (SRC_FLAG is null or (SRC_FLAG in (0,1,2,3)));
  
  
--SC_ROADNAME_SUFFIX
-- Create table
create table SC_ROADNAME_SUFFIX
(
  id          NUMBER(10) not null,
  name        VARCHAR2(10) not null,
  py          VARCHAR2(10) not null,
  englishname VARCHAR2(10) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_SUFFIX
  add constraint PK_SC_ROADNAME_SUFFIX primary key (NAME, LANG_CODE)
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
 
--SC_ROADNAME_SUFFIX
-- Create table
create table SC_ROADNAME_SUFFIX
(
  id          NUMBER(10) not null,
  name        VARCHAR2(10) not null,
  py          VARCHAR2(10) not null,
  englishname VARCHAR2(10) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_SUFFIX
  add constraint PK_SC_ROADNAME_SUFFIX primary key (NAME, LANG_CODE)
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

--
-- Create table
create table SC_ROADNAME_INFIX
(
  id          NUMBER(10) not null,
  name        VARCHAR2(50) not null,
  py          VARCHAR2(50) not null,
  englishname VARCHAR2(50) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_INFIX
  add constraint PK_SC_ROADNAME_INFIX primary key (NAME, LANG_CODE)
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

-- Create table
create table SC_ROADNAME_INFIX
(
  id          NUMBER(10) not null,
  name        VARCHAR2(50) not null,
  py          VARCHAR2(50) not null,
  englishname VARCHAR2(50) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_INFIX
  add constraint PK_SC_ROADNAME_INFIX primary key (NAME, LANG_CODE)
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
  
 -- Create table
create table SC_ROADNAME_TYPENAME
(
  id          NUMBER(10) not null,
  name        VARCHAR2(50) not null,
  py          VARCHAR2(50) not null,
  englishname VARCHAR2(50) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);

-- Create table
create table TY_CHARACTER_EGALCHAR_EXT
(
  extention_type VARCHAR2(50) not null,
  character      VARCHAR2(50) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TY_CHARACTER_EGALCHAR_EXT
  add constraint PK_TY_CHARACTER_EGALCHAR_EXT primary key (EXTENTION_TYPE, CHARACTER)
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

 -- Create table
create table SC_ROADNAME_POSITION
(
  id          NUMBER(10) not null,
  name        VARCHAR2(100) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_POSITION
  add constraint PK_SC_ROADNAME_POSITION primary key (NAME, LANG_CODE)
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
 
-- Create table
create table SC_ROADNAME_SPLIT_PREFIX
(
  id                NUMBER(10) not null,
  word_can_split    VARCHAR2(255) not null,
  word_cannot_split VARCHAR2(255),
  region_flag       NUMBER(1) not null,
  lang_code         VARCHAR2(3) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_SPLIT_PREFIX
  add constraint PK_SC_ROADNAME_SPLIT_PREFIX primary key (WORD_CAN_SPLIT, LANG_CODE)
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

 -- Create table
create table SC_ROADNAME_ENGNM_QJ
(
  id        NUMBER(10) not null,
  name_q    VARCHAR2(50) not null,
  name_j    VARCHAR2(50) not null,
  lang_code VARCHAR2(10)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_ROADNAME_ENGNM_QJ
  add constraint METADATA_SC_RDNAME_06 primary key (ID)
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

 -- Create table
create table TY_CHARACTER_FJT_HZ
(
  ft      VARCHAR2(255) not null,
  jt      VARCHAR2(255) not null,
  convert NUMBER(2) not null,
  ftorder NUMBER(2) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TY_CHARACTER_FJT_HZ
  add constraint FJT_HZ primary key (FT)
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
-- Create/Recreate check constraints 
alter table TY_CHARACTER_FJT_HZ
  add constraint CKC_CONVERT_TY_FJT_HZ
  check (CONVERT in (0,1,2));
 
 -- Create table
create table TMC_VERSION
(
  loctable_id         VARCHAR2(2) not null,
  version             VARCHAR2(64) not null,
  version_des         VARCHAR2(200) not null,
  orgdata_version     VARCHAR2(200) not null,
  orgdata_releasedate VARCHAR2(200) not null,
  orgdata_nextupdate  VARCHAR2(200) not null,
  author              VARCHAR2(200) not null,
  charset             VARCHAR2(64) not null,
  exchange_version    VARCHAR2(200),
  country_id          VARCHAR2(3) not null,
  country_name        VARCHAR2(64) not null,
  country_short       VARCHAR2(64) not null,
  bound_name          VARCHAR2(200),
  bound_id            VARCHAR2(64),
  bound_short         VARCHAR2(64) not null,
  note                VARCHAR2(200),
  u_record            NUMBER(2) not null,
  u_fields            VARCHAR2(1000)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TMC_VERSION
  add constraint PK_TMC_VERSION primary key (COUNTRY_ID)
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
-- Create/Recreate check constraints 
alter table TMC_VERSION
  add constraint CKC_U_RECORD_TMC_VERS
  check (U_RECORD in (0,1,2,3));

 -- Create table
create table TMC_POINT_TRANSLATENAME
(
  tmc_id         NUMBER(10) not null,
  name_flag      NUMBER(1) not null,
  trans_lang     VARCHAR2(3),
  translate_name VARCHAR2(100),
  phonetic       VARCHAR2(1000),
  u_record       NUMBER(2) not null,
  u_fields       VARCHAR2(1000)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TMC_POINT_TRANSLATENAME
  add constraint TMC_POINT_01 primary key (TMC_ID)
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
-- Create/Recreate check constraints 
alter table TMC_POINT_TRANSLATENAME
  add constraint CKC_NAME_FLAG_TMC_POIN
  check (NAME_FLAG in (1,2,3));
alter table TMC_POINT_TRANSLATENAME
  add constraint CKC_U_RECORD_POINTRANSLATE
  check (U_RECORD in (0,1,2,3));
 
 -- Create table
create table TMC_LINE_TRANSLATENAME
(
  tmc_id         NUMBER(10) not null,
  name_flag      NUMBER(1) not null,
  trans_lang     VARCHAR2(3) not null,
  translate_name VARCHAR2(100),
  phonetic       VARCHAR2(1000),
  u_record       NUMBER(2) not null,
  u_fields       VARCHAR2(1000)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TMC_LINE_TRANSLATENAME
  add constraint PK_TMC_LINE_TRANSLATENAME primary key (TMC_ID, NAME_FLAG, TRANS_LANG)
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
-- Create/Recreate check constraints 
alter table TMC_LINE_TRANSLATENAME
  add constraint CKC_NAME_FLAG_TMC_LINE
  check (NAME_FLAG in (0,1,2));
alter table TMC_LINE_TRANSLATENAME
  add constraint CKC_U_RECORD_LINE_TRANSLATE
  check (U_RECORD in (0,1,2,3));
  
 -- Create table
create table TMC_AREA_TRANSLATENAME
(
  tmc_id         NUMBER(10) not null,
  trans_lang     VARCHAR2(3),
  translate_name VARCHAR2(100),
  phonetic       VARCHAR2(1000),
  u_record       NUMBER(2) not null,
  u_fields       VARCHAR2(1000)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TMC_AREA_TRANSLATENAME
  add constraint TRANSLATE_01 primary key (TMC_ID)
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
-- Create/Recreate check constraints 
alter table TMC_AREA_TRANSLATENAME
  add constraint CKC_U_RECORD_TRANSLATENAME
  check (U_RECORD in (0,1,2,3));

 -- Create table
create table TMC_LOCATION_CODE
(
  loctable_id VARCHAR2(2) not null,
  name        VARCHAR2(64) not null,
  u_record    NUMBER(2),
  u_fields    VARCHAR2(1000),
  region_code VARCHAR2(20),
  cid         VARCHAR2(4) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TMC_LOCATION_CODE
  add constraint TMC_LOCATION_CODE_CID primary key (CID)
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

  -- Create table
create table TY_CHARACTER_FJT_HM_CHECK
(
  id      NUMBER(5) not null,
  hz      VARCHAR2(8) not null,
  correct VARCHAR2(20),
  kg_flag VARCHAR2(2) not null,
  hm_flag VARCHAR2(3) not null,
  memo    VARCHAR2(256),
  type    NUMBER(2)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table TY_CHARACTER_FJT_HM_CHECK
  add constraint CODHNG primary key (ID)
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
-- Create/Recreate check constraints 
alter table TY_CHARACTER_FJT_HM_CHECK
  add constraint CKC_HM_FLAG_TY_CHARA
  check (HM_FLAG in ('D','H','M','HM','DHM'));
alter table TY_CHARACTER_FJT_HM_CHECK
  add constraint CKC_KG_FLAG_TY_CHARA
  check (KG_FLAG in ('K','G','KG'));
alter table TY_CHARACTER_FJT_HM_CHECK
  add constraint CKC_TYPE_TY_CHARA
  check (TYPE in (1,2));

 commit;
