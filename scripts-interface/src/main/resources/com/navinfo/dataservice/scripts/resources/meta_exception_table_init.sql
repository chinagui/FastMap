/*==============================================================*/
/* Table: CK_EXCEPTION                                          */
/*==============================================================*/
create table CK_EXCEPTION  (
  exception_id  NUMBER(10) not null,
  rule_id       VARCHAR2(100),
  task_name     VARCHAR2(50),
  status        NUMBER(2) default 0 not null,
  group_id      NUMBER(10) default 0 not null,
  rank          NUMBER(10) default 0 not null,
  situation     VARCHAR2(4000),
  information   VARCHAR2(4000),
  suggestion    VARCHAR2(4000),
  geometry      VARCHAR2(4000),
  targets       CLOB,
  addition_info CLOB,
  memo          VARCHAR2(500),
  create_date   DATE,
  update_date   DATE,
  mesh_id       NUMBER(8) default 0 not null,
  scope_flag    NUMBER(2) default 1 not null,
  province_name VARCHAR2(60),
  map_scale     NUMBER(2) default 0 not null,
  reserved      VARCHAR2(1000),
  extended      VARCHAR2(1000),
  task_id       VARCHAR2(500),
  qa_task_id    VARCHAR2(500),
  qa_status     NUMBER(2) default 2 not null,
  worker        VARCHAR2(500),
  qa_worker     VARCHAR2(500),
  memo_1        VARCHAR2(500),
  memo_2        VARCHAR2(500),
  memo_3        VARCHAR2(500),
  md5_code      VARCHAR2(32),
  u_record      NUMBER(2) default 0 not null,
  u_fields      VARCHAR2(1000),
  u_date        VARCHAR2(14),
  row_id        RAW(16),
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
-- Create table
create table NI_VAL_EXCEPTION
(
  val_exception_id NUMBER(10) not null,
  ruleid           VARCHAR2(100) default '0',
  task_name        VARCHAR2(50),
  groupid          NUMBER(10) default 0 not null,
  "LEVEL"            NUMBER(10) default 0,
  situation        VARCHAR2(4000),
  information      VARCHAR2(4000),
  suggestion       VARCHAR2(4000),
  location         SDO_GEOMETRY,
  targets          CLOB,
  addition_info    CLOB,
  del_flag         NUMBER(1) default 0 not null,
  created          DATE,
  updated          DATE,
  mesh_id          NUMBER(6),
  scope_flag       NUMBER(2) default 1 not null,
  province_name    VARCHAR2(60),
  map_scale        NUMBER(2) default 0 not null,
  reserved         VARCHAR2(1000),
  extended         VARCHAR2(1000),
  task_id          VARCHAR2(500),
  qa_task_id       VARCHAR2(500),
  qa_status        NUMBER(2) default 2 not null,
  worker           VARCHAR2(500),
  qa_worker        VARCHAR2(500),
  log_type         NUMBER(5) default 0 not null,
  md5_code         VARCHAR2(32),
  u_record         NUMBER(2) default 0,
  u_fields         VARCHAR2(1000),
  u_date           VARCHAR2(14),
  row_id           RAW(16)
);

alter table NI_VAL_EXCEPTION
  add constraint CKC_RECORD_EXCEPTION
  check (U_RECORD in ('1','2','3','0'));
alter table NI_VAL_EXCEPTION
  add check (DEL_FLAG in (0,1));
alter table NI_VAL_EXCEPTION
  add check (SCOPE_FLAG in (1,2,3));
alter table NI_VAL_EXCEPTION
  add check (MAP_SCALE in (0,1,2,3));
alter table NI_VAL_EXCEPTION
  add check (QA_STATUS in (1,2));

comment on column NI_VAL_EXCEPTION.RULEID is
'参考"CK_RULE"';

comment on column NI_VAL_EXCEPTION.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column NI_VAL_EXCEPTION.UPDATED is
'格式"YYYY/MM/DD HH:mm:ss"';


CREATE UNIQUE INDEX IX_NI_VAL_MD5 ON NI_VAL_EXCEPTION(MD5_CODE);


commit;
