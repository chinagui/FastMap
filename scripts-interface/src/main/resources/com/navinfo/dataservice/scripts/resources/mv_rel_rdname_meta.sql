create table SC_ROADNAME_SUFFIX
(
  id          NUMBER(10) not null,
  name        VARCHAR2(10) not null,
  py          VARCHAR2(10) not null,
  englishname VARCHAR2(10) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
alter table SC_ROADNAME_SUFFIX add constraint PK_SC_ROADNAME_SUFFIX primary key (NAME, LANG_CODE);

create table SC_ROADNAME_TYPENAME
(
  id          NUMBER(10) not null,
  name        VARCHAR2(50) not null,
  py          VARCHAR2(50) not null,
  englishname VARCHAR2(50) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);

create table SC_ROADNAME_POSITION
(
  id          NUMBER(10) not null,
  name        VARCHAR2(100) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
alter table SC_ROADNAME_POSITION add constraint PK_SC_ROADNAME_POSITION primary key (NAME, LANG_CODE);

create table SC_ROADNAME_SPLIT_PREFIX
(
  id                NUMBER(10) not null,
  word_can_split    VARCHAR2(255) not null,
  word_cannot_split VARCHAR2(255),
  region_flag       NUMBER(1) not null,
  lang_code         VARCHAR2(3) not null
);
alter table SC_ROADNAME_SPLIT_PREFIX add constraint PK_SC_ROADNAME_SPLIT_PREFIX primary key (WORD_CAN_SPLIT, LANG_CODE);

create table SC_ROADNAME_INFIX
(
  id          NUMBER(10) not null,
  name        VARCHAR2(50) not null,
  py          VARCHAR2(50) not null,
  englishname VARCHAR2(50) not null,
  region_flag NUMBER(1),
  lang_code   VARCHAR2(3) not null
);
alter table SC_ROADNAME_INFIX add constraint PK_SC_ROADNAME_INFIX primary key (NAME, LANG_CODE);


-- ROWS
INSERT INTO SC_ROADNAME_SUFFIX SELECT * FROM SC_ROADNAME_SUFFIX@DBLINK_RMS;
INSERT INTO SC_ROADNAME_TYPENAME SELECT * FROM SC_ROADNAME_TYPENAME@DBLINK_RMS;
INSERT INTO SC_ROADNAME_POSITION SELECT * FROM SC_ROADNAME_POSITION@DBLINK_RMS;
INSERT INTO SC_ROADNAME_SPLIT_PREFIX SELECT * FROM SC_ROADNAME_SPLIT_PREFIX@DBLINK_RMS;
INSERT INTO SC_ROADNAME_INFIX SELECT * FROM SC_ROADNAME_INFIX@DBLINK_RMS;
COMMIT;

-- py util
create table TY_CHARACTER_FULL2HALF
(
  half_width VARCHAR2(10),
  h_unicode  NUMBER(10),
  h_ansi     NUMBER(10),
  full_width VARCHAR2(10) not null,
  f_unicode  NUMBER(10),
  f_ansi     NUMBER(10),
  serial_id  NUMBER not null
);
alter table TY_CHARACTER_FULL2HALF add constraint PK_TY_CHARACTER_FULL2HALF primary key (SERIAL_ID);

create table TY_NAVICOVPY_KEYWORD
(
  keyword   VARCHAR2(255) not null,
  keywordpy VARCHAR2(255) not null,
  adminarea VARCHAR2(255),
  priority  VARCHAR2(255) not null,
  id        NUMBER not null,
  frequency NUMBER,
  kind      VARCHAR2(255),
  type      VARCHAR2(2),
  chain     VARCHAR2(4)
);
alter table TY_NAVICOVPY_KEYWORD add constraint METADATA_TY_KEYWORD_02 primary key (ID);
create index IDX_TY_NAVICOVPY_KEYWORD_1 on TY_NAVICOVPY_KEYWORD (KEYWORD);
alter table TY_NAVICOVPY_KEYWORD add constraint CKC_TYPE_TY_NAVIC check (TYPE is null or (TYPE in ('1')));

create table TY_NAVICOVPY_KEYWORD_KIND
(
  kind       VARCHAR2(255),
  keyword_id NUMBER not null,
  chain      VARCHAR2(4)
);

create table TY_NAVICOVPY_PY
(
  serial_id       NUMBER not null,
  jt              VARCHAR2(10) not null,
  py              VARCHAR2(10) not null,
  pyorder         NUMBER(2) not null,
  py2             VARCHAR2(10),
  ft              VARCHAR2(10),
  tone            VARCHAR2(10),
  toneorder       NUMBER(2),
  tone1_frequency NUMBER(1)
);
alter table TY_NAVICOVPY_PY add constraint METADATA_TY_PY_01 primary key (SERIAL_ID);
alter table TY_NAVICOVPY_PY add constraint CK_NAVICOVPY_TONE1 check (TONE1_FREQUENCY IN(1,2,3));

create table TY_NAVICOVPY_WORD
(
  serial_id NUMBER not null,
  word      VARCHAR2(50) not null,
  py        VARCHAR2(200) not null,
  py2       VARCHAR2(200),
  adminarea VARCHAR2(6),
  py3       VARCHAR2(200),
  version   VARCHAR2(10),
  mark      NUMBER(1),
  memo      VARCHAR2(100)
);
alter table TY_NAVICOVPY_WORD add constraint METADATA_TY_WORD_01 primary key (SERIAL_ID);
alter table TY_NAVICOVPY_WORD add constraint CKC_MARK_TY_NAVIC check (MARK in (0,1));

CREATE TABLE B_PYLET AS SELECT * FROM B_PYLET@DBLINK_RMS;
-- rows
INSERT INTO TY_CHARACTER_FULL2HALF SELECT * FROM TY_CHARACTER_FULL2HALF@DBLINK_RMS;
INSERT INTO TY_NAVICOVPY_KEYWORD SELECT * FROM TY_NAVICOVPY_KEYWORD@DBLINK_RMS;
INSERT INTO TY_NAVICOVPY_KEYWORD_KIND SELECT * FROM TY_NAVICOVPY_KEYWORD_KIND@DBLINK_RMS;
INSERT INTO TY_NAVICOVPY_PY SELECT * FROM TY_NAVICOVPY_PY@DBLINK_RMS;
INSERT INTO TY_NAVICOVPY_WORD SELECT * FROM TY_NAVICOVPY_WORD@DBLINK_RMS;
COMMIT;