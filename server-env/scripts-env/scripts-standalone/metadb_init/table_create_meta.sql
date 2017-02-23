WHENEVER SQLERROR CONTINUE;


CREATE TABLE B_PYLET AS SELECT * FROM B_PYLET;
CREATE TABLE CP_PROVINCELIST AS SELECT DISTINCT(ADMINCODE),PROVINCE FROM CP_MESHLIST;

CREATE INDEX IDX_TY_NAVICOVPY_KEYWORD_1 ON TY_NAVICOVPY_KEYWORD(KEYWORD); 

ALTER TABLE RD_NAME ADD(PROCESS_FLAG NUMBER(1) DEFAULT 0);
UPDATE RD_NAME SET PROCESS_FLAG=1;

ALTER TABLE SC_MODEL_MATCH_G ADD (FILE_CONTENT BLOB,FILE_TYPE NUMBER(1) DEFAULT 0,UPDATE_TIME DATE DEFAULT NULL);
CREATE INDEX IDX_SC_MODEL_MATCH_G_1 ON SC_MODEL_MATCH_G(FILE_NAME); 
ALTER TABLE SC_VECTOR_MATCH ADD (FILE_CONTENT BLOB);

--元数据库与大区库的同步记录表
create table META_DML_LOGS
(
  dml_object_id NUMBER(10) not null,
  dml_type      NUMBER(1) not null,
  dml_date      TIMESTAMP(6) default sysdate not null,
  dml_table     VARCHAR2(100)
);
-- Add comments to the columns 
comment on column META_DML_LOGS.dml_object_id
  is '被执行对象的ID';
comment on column META_DML_LOGS.dml_type
  is '具体操作 0新增;1删除;2修改';

  comment on column META_DML_LOGS.dml_table
  is '操作日期';
  
comment on column META_DML_LOGS.dml_table
  is '操作针对的元数据库的表';

-- Create table 同步失败的记录
create table META_DML_SF_LOGS
(
  dml_success      NUMBER(1) not null,
  dml_date      TIMESTAMP(6) default sysdate not null,
  dml_table     VARCHAR2(100),
  exception_info VARCHAR2(200)
);
-- Add comments to the columns 

comment on column META_DML_SF_LOGS.dml_success
  is '操作是否成功 0 失败 ;1成功';
  
  comment on column META_DML_SF_LOGS.dml_date
  is '操作 时间';
  
   comment on column META_DML_SF_LOGS.dml_table
  is '操作 的表';
  comment on column META_DML_SF_LOGS.exception_info
  is '报错信息';


COMMIT;
EXIT;
