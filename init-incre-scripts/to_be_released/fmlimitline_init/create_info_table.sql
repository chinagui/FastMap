create table SC_PLATERES_INFO
(
  INFO_INTEL_ID   VARCHAR2(100) not null,
  INFO_CODE   VARCHAR2(18) not null,
  ADMIN_CODE VARCHAR2(6) not null,
  URL VARCHAR2(100) not null,
  NEWS_TIME VARCHAR2(100) not null,
  INFO_CONTENT VARCHAR2(3000) not null,
  COMPLETE NUMBER(1) default 1 not null,
  CONDITION VARCHAR2(1) not null,
  MEMO VARCHAR2(1000)
)
tablespace USERS
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
-- Add comments to the columns 
comment on column SC_PLATERES_INFO.INFO_INTEL_ID
  is '情报主键';
comment on column SC_PLATERES_INFO.INFO_CODE
  is '情报编码';
comment on column SC_PLATERES_INFO.ADMIN_CODE
  is '行政区划代码';
comment on column SC_PLATERES_INFO.URL
  is '来源网址';
comment on column SC_PLATERES_INFO.NEWS_TIME
  is '新闻发布日期';
comment on column SC_PLATERES_INFO.INFO_CONTENT
  is '新闻内容';
comment on column SC_PLATERES_INFO.COMPLETE
  is '完成状态
1未处理
2已处理
3 无法处理（情报错误，无法制作）
';
comment on column SC_PLATERES_INFO.CONDITION
  is '限行长短期说明';
comment on column SC_PLATERES_INFO.MEMO
  is '备注';

create table SC_PLATERES_MANOEUVRE
(
  manoeuvre_id   NUMBER(3) not null,
  group_id       VARCHAR2(11) not null,
  vehicle        VARCHAR2(40) not null,
  attribution    VARCHAR2(10) not null,
  restrict       VARCHAR2(10),
  temp_plate     NUMBER(1) not null,
  temp_plate_num VARCHAR2(1),
  char_switch    NUMBER(1) not null,
  char_to_num    VARCHAR2(1),
  tail_number    VARCHAR2(50) not null,
  platecolor     VARCHAR2(10) not null,
  energy_type    VARCHAR2(10) not null,
  gas_emisstand  VARCHAR2(10) not null,
  seatnum        NUMBER(3) not null,
  vehicle_length NUMBER(3,1) not null,
  res_weigh      NUMBER(5,2) not null,
  res_axle_load  NUMBER(5,2) not null,
  res_axle_count NUMBER(2) not null,
  start_date     VARCHAR2(1000),
  end_date       VARCHAR2(1000),
  res_datetype   VARCHAR2(50) not null,
  time           VARCHAR2(1000),
  spec_flag      VARCHAR2(15)
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
  
create table SC_PLATERES_GROUP
(
  group_id      VARCHAR2(11) not null,
  info_intel_id VARCHAR2(100) not null,
  ad_admin      NUMBER(6) not null,
  principle     VARCHAR2(2400) not null,
  group_type    NUMBER(1) default 1 not null,
  u_date        VARCHAR2(2400) not null
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

CREATE TABLE SC_PLATERES_LINK(
  GEOMETRY_ID VARCHAR2(18) not null,
  GROUP_ID VARCHAR2(11) not null,
  GEOMETRY SDO_GEOMETRY,
  BOUNDARY_LINK VARCHAR2(1) not null)
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

INSERT INTO user_sdo_geom_metadata  
    (TABLE_NAME,  
     COLUMN_NAME,  
     DIMINFO,  
     SRID)  
  VALUES (  
  'SC_PLATERES_LINK',  
  'GEOMETRY',  
  SDO_DIM_ARRAY(   -- 20X20 grid  
    SDO_DIM_ELEMENT('X', -180, 180, 0.005),  
    SDO_DIM_ELEMENT('Y', -90, 90, 0.005)  
     ),  
  8307   -- SRID  
);  

CREATE INDEX LINK_GEOMETRY_INDEX  
   ON SC_PLATERES_LINK(GEOMETRY)  
   INDEXTYPE IS MDSYS.SPATIAL_INDEX;
  
CREATE TABLE SC_PLATERES_FACE(
  GEOMETRY_ID VARCHAR2(18) not null,
  GROUP_ID VARCHAR2(11) not null,
  GEOMETRY SDO_GEOMETRY,
  BOUNDARY_LINK VARCHAR2(1) not null)
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

INSERT INTO user_sdo_geom_metadata  
    (TABLE_NAME,  
     COLUMN_NAME,  
     DIMINFO,  
     SRID)  
  VALUES (  
  'SC_PLATERES_FACE',  
  'GEOMETRY',  
  SDO_DIM_ARRAY(   -- 20X20 grid  
    SDO_DIM_ELEMENT('X', -180, 180, 0.005),  
    SDO_DIM_ELEMENT('Y', -90, 90, 0.005)  
     ),  
  8307   -- SRID  
);  

CREATE INDEX FACE_GEOMETRY_INDEX  
   ON SC_PLATERES_FACE(GEOMETRY)  
   INDEXTYPE IS MDSYS.SPATIAL_INDEX;
  
create table SC_PLATERES_RDLINK
(
  link_pid        NUMBER(10) not null,
  limit_dir       NUMBER(1) default 0 not null,
  geometry_id     VARCHAR2(18) not null,
  geometry_rdlink SDO_GEOMETRY
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
  
INSERT INTO user_sdo_geom_metadata  
    (TABLE_NAME,  
     COLUMN_NAME,  
     DIMINFO,  
     SRID)  
  VALUES (  
  'SC_PLATERES_RDLINK',  
  'geometry_rdlink',  
  SDO_DIM_ARRAY(   -- 20X20 grid  
    SDO_DIM_ELEMENT('X', -180, 180, 0.005),  
    SDO_DIM_ELEMENT('Y', -90, 90, 0.005)  
     ),  
  8307   -- SRID  
);  

CREATE INDEX RDLINK_GEOMETRY_INDEX  
   ON SC_PLATERES_RDLINK(geometry_rdlink)  
   INDEXTYPE IS MDSYS.SPATIAL_INDEX;
  
  
create table SC_PLATERES_GEOMETRY
(
  GEOMETRY_ID  VARCHAR2(18) not null,
  GROUP_ID   VARCHAR2(11) not null,
  GEOMETRY SDO_GEOMETRY,
  BOUNDARY_LINK VARCHAR2(1) not null
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


INSERT INTO user_sdo_geom_metadata  
    (TABLE_NAME,  
     COLUMN_NAME,  
     DIMINFO,  
     SRID)  
  VALUES (  
  'SC_PLATERES_GEOMETRY',  
  'GEOMETRY',  
  SDO_DIM_ARRAY(   -- 20X20 grid  
    SDO_DIM_ELEMENT('X', -180, 180, 0.005),  
    SDO_DIM_ELEMENT('Y', -90, 90, 0.005)  
     ),  
  8307   -- SRID  
);  

CREATE INDEX GEOMETRY_INDEX  
   ON SC_PLATERES_GEOMETRY(GEOMETRY)  
   INDEXTYPE IS MDSYS.SPATIAL_INDEX;
  

