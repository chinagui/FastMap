create table BATCH_PLUS
(
  rule_id            VARCHAR2(50),
  accessor           CLOB,
  accessor_type      VARCHAR2(10),
  obj_name_set       VARCHAR2(100),
  refer_subtable_map VARCHAR2(100),
  status             VARCHAR2(2),
  desc_detail        VARCHAR2(2000),
  desc_short         VARCHAR2(200)
);
-- Add comments to the columns 
comment on column BATCH_PLUS.accessor
  is '类路径';
comment on column BATCH_PLUS.accessor_type
  is 'JAVA';
comment on column BATCH_PLUS.obj_name_set
  is '检查对象，参考ObjectType类，例如：IX_POI,RD_LINK';
comment on column BATCH_PLUS.refer_subtable_map
  is '检查对象要参考的子表,JSON格式，例如：{"IX_POI":["IX_POI_NAME","IX_POI_ADDRESS"],"RD_LINK":["RD_LINK_NAME"]}';
comment on column BATCH_PLUS.status
  is 'E可用，X删除';
  
create table BATCH_OPERATION_PLUS
(
  operation_code VARCHAR2(10),
  batch_id       VARCHAR2(10)
);
create table CHECK_PLUS
(
  rule_id            VARCHAR2(50),
  accessor           CLOB,
  accessor_type      VARCHAR2(10),
  obj_name_set       VARCHAR2(100),
  refer_subtable_map VARCHAR2(100),
  status             VARCHAR2(2),
  log                VARCHAR2(1000),
  desc_short         VARCHAR2(200),
  desc_detail        VARCHAR2(2000)
);
create table CHECK_OPERATION_PLUS
(
  operation_code VARCHAR2(10),
  check_id       VARCHAR2(10)
);



INSERT INTO SYS_CONFIG
  (CONF_ID, CONF_KEY, CONF_VALUE, CONF_DESC, APP_TYPE)
VALUES
  (SYS_CONFIG_SEQ.NEXTVAL, 'SEASON.VERSION', '16WIN', '作业季', 'default');
  
commit;
exit;