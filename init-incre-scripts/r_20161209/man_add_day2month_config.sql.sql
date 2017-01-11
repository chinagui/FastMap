-- Create table
create table DAY2MONTH_CONFIG
(
  CONF_ID     NUMBER(10) not null,
  CITY_ID     NUMBER(10),
  TYPE        VARCHAR2(10),
  STATUS      NUMBER(2),
  EXE_USER_ID NUMBER(10),
  EXE_DATE    TIMESTAMP(6)
);
-- Add comments to the table 
comment on table DAY2MONTH_CONFIG
  is '日落月开关管理表';
-- Add comments to the columns 
comment on column DAY2MONTH_CONFIG.TYPE
  is 'POI,ALL';
comment on column DAY2MONTH_CONFIG.STATUS
  is '0开1关，不可进行操作';
comment on column DAY2MONTH_CONFIG.EXE_USER_ID
  is '执行人';
comment on column DAY2MONTH_CONFIG.EXE_DATE
  is '执行时间';
-- Create/Recreate primary, unique and foreign key constraints 
alter table DAY2MONTH_CONFIG
  add constraint PK_DAY2MONTH_CONFIG primary key (CONF_ID);

-- Create sequence 
create sequence DAY2MONTH_CONFIG_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;