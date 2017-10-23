create table SC_PLATERES_INFO
(
  INFO_INTEL_ID   VARCHAR2(100) not null,
  INFO_CODE   VARCHAR2(50) not null,
  ADMIN_CODE VARCHAR2(30) not null,
  URL VARCHAR2(1000) not null,
  NEWS_TIME VARCHAR2(200) not null,
  INFO_CONTENT VARCHAR2(4000) not null,
  COMPLETE NUMBER(1) default 1 not null,
  CONDITION VARCHAR2(1) not null,
  MEMO VARCHAR2(4000)
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

CREATE INDEX SC_PLATERES_LINK_GEOMETRY  
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

CREATE INDEX SC_PLATERES_FACE_GEOMETRY  
   ON SC_PLATERES_FACE(GEOMETRY)  
   INDEXTYPE IS MDSYS.SPATIAL_INDEX;
  
exit;