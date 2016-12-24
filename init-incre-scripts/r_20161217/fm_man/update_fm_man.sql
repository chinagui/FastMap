create table SUBTASK_REFER
(
  id       NUMBER(10),
  geometry SDO_GEOMETRY
);
-- Add comments to the table 
comment on table SUBTASK_REFER
  is '不规则子任务表';

INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('SUBTASK_REFER',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_SUBTASK_REFER ON SUBTASK_REFER(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ANALYZE TABLE SUBTASK_REFER COMPUTE STATISTICS;

alter table SUBTASK drop column refer_geometry;
alter table SUBTASK add refer_id NUMBER(10);

-- Create table
create table DAY2MONTH_CONFIG
(
  conf_id     NUMBER(10) not null,
  city_id     NUMBER(10),
  type        VARCHAR2(10),
  status      NUMBER(2),
  exe_user_id NUMBER(10),
  exe_date    TIMESTAMP(6)
);
-- Add comments to the table 
comment on table DAY2MONTH_CONFIG
  is '日落月开关管理表';
-- Add comments to the columns 
comment on column DAY2MONTH_CONFIG.type
  is 'POI,ALL';
comment on column DAY2MONTH_CONFIG.status
  is '0开1关，不可进行操作';
comment on column DAY2MONTH_CONFIG.exe_user_id
  is '执行人';
comment on column DAY2MONTH_CONFIG.exe_date
  is '执行时间';
-- Create/Recreate primary, unique and foreign key constraints 
alter table DAY2MONTH_CONFIG
  add constraint PK_DAY2MONTH_CONFIG primary key (CONF_ID);
  
create table FM_STAT_DAY2MONTH
(
  city_id            NUMBER(10),
  type               VARCHAR2(10),
  cur_total          NUMBER(10),
  accumulative_total NUMBER(10),
  stat_date          TIMESTAMP(6),
  stat_time          TIMESTAMP(6)
);
-- Add comments to the table 
comment on table FM_STAT_DAY2MONTH
  is '日落月统计表';
-- Add comments to the columns 
comment on column FM_STAT_DAY2MONTH.type
  is 'POI,ALL';
comment on column FM_STAT_DAY2MONTH.cur_total
  is '当前数量';
comment on column FM_STAT_DAY2MONTH.accumulative_total
  is '累计数量';

CREATE SEQUENCE day2month_config_seq START WITH 1 MAXVALUE 9999999999;

alter table infor add feedback_type NUMBER(1) default 0 not null;
comment on column INFOR.feedback_type
  is '0未反馈 1已反馈';
  
INSERT INTO SUBTASK_REFER
  (ID, GEOMETRY)
  SELECT SUBTASK_SEQ.NEXTVAL, T.GEOMETRY FROM SUBTASK T WHERE T.STAGE = 0;
  
INSERT INTO DAY2MONTH_CONFIG
  (CONF_ID, CITY_ID, TYPE, STATUS)
  SELECT DAY2MONTH_CONFIG_SEQ.NEXTVAL, CITY_ID, 'POI', 0
    FROM CITY
   WHERE CITY_ID NOT IN (100000, 100001, 100002, 100003);
   
   
commit;
exit;