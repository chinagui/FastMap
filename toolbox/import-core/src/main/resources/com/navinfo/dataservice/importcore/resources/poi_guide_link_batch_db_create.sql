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
  
-- Create table
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
  LEVEL           VARCHAR2(2),
  SPORTS_VENUE    VARCHAR2(3),
  INDOOR          NUMBER(1) default 0 not null,
  VIP_FLAG        VARCHAR2(10),
  U_RECORD        NUMBER(2) default 0 not null,
  U_FIELDS        VARCHAR2(1000),
  U_DATE          VARCHAR2(14),
  ROW_ID          RAW(16)
);
-- Add comments to the columns 
comment on column IX_POI.PID
  is '主键';
comment on column IX_POI.KIND_CODE
  is '参考"IX_POI_CODE"';
comment on column IX_POI.GEOMETRY
  is '存储以"度"为单位的经纬度坐标点,用于POI显示和计算Link左右关系
';
comment on column IX_POI.LINK_PID
  is '参考"RD_LINK"';
comment on column IX_POI.SIDE
  is '记录POI位于引导道路Link上,左侧或右侧';
comment on column IX_POI.NAME_GROUPID
  is '[173sp2]参考"RD_NAME"';
comment on column IX_POI.ROAD_FLAG
  is '[170]';
comment on column IX_POI.PMESH_ID
  is '[171A]每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';
comment on column IX_POI.IMPORTANCE
  is '记录以下分类的POI为重要,即IMPORTANCE为1,否则为0
(1)拥有国际进出港口的机场
(2)国家旅游局评定的等级为3A,4A,5A的风景区
(3)世界文化遗产';
comment on column IX_POI.CHAIN
  is '[171U]主要制作对象是宾馆和加油站';
comment on column IX_POI.ACCESS_FLAG
  is '[170]';
comment on column IX_POI.OPEN_24H
  is '[171U]';
comment on column IX_POI.MESH_ID_5K
  is '记录索引所在的5000图幅号,格式为:605603_1_3';
comment on column IX_POI.REGION_ID
  is '[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';
comment on column IX_POI.EDIT_FLAG
  is '[171A]用于数据完整提取时,区分是否可编辑';
comment on column IX_POI.DIF_GROUPID
  is '[181A]用于差分更新数据包的产品版本管理,如果存在多个,采用半角"|"分隔';
comment on column IX_POI.RESERVED
  is '[181A]';
comment on column IX_POI.STATE
  is '[170]';
comment on column IX_POI.FIELD_STATE
  is '[170]改名称,改地址,改分类';
comment on column IX_POI.LABEL
  is '[181U]记录路,水,绿地,单项收费,双向收费,显示位置,24小时便利店';
comment on column IX_POI.TYPE
  is '[170]';
comment on column IX_POI.ADDRESS_FLAG
  is '标志POI 地址(IX_POI_ADDRESS)完整性';
comment on column IX_POI.EX_PRIORITY
  is '[171A]提取的优先级别(城区为A1~A11;县乡为B2~B5)';
comment on column IX_POI.EDITION_FLAG
  is '记录数据是由内业还是外业修改,新增,删除等标志';
comment on column IX_POI.OLD_BLOCKCODE
  is '原结构中的"OLD大字"';
comment on column IX_POI.OLD_KIND
  is '[170]';
comment on column IX_POI.POI_NUM
  is '记录来自NIDB的POI编号';
comment on column IX_POI.TASK_ID
  is '[170]记录内业的任务编号';
comment on column IX_POI.DATA_VERSION
  is '记录数据采集的作业季,如10冬,11夏';
comment on column IX_POI.FIELD_TASK_ID
  is '记录外业的任务编号';
comment on column IX_POI.U_RECORD
  is '增量更新标识';
comment on column IX_POI.U_FIELDS
  is '记录更新的英文字段名,多个之间采用半角''|''分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_POI
  add constraint PK_IX_POI primary key (PID);
-- Create/Recreate check constraints 
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
-- Create/Recreate indexes 
create index EXP_IX_01 on IX_POI (MESH_ID, LINK_PID);
create index EXP_IX_1001 on IX_POI (POI_NUM);
create index EXP_IX_49 on IX_POI (KIND_CODE);
create index EXP_IX_D01 on IX_POI (CHAIN, TYPE);
create unique index IDX_20170414205940_R on IX_POI (ROW_ID);
-- Create table
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
-- Add comments to the columns 
comment on column IX_POI_ADDRESS.NAME_ID
  is '[170]主键';
comment on column IX_POI_ADDRESS.NAME_GROUPID
  is '[171U][170]从1开始递增编号';
comment on column IX_POI_ADDRESS.POI_PID
  is '外键,引用"IX_POI"';
comment on column IX_POI_ADDRESS.LANG_CODE
  is '简体中文,繁体中文,英文,葡文等多种语言';
comment on column IX_POI_ADDRESS.SRC_FLAG
  is '[170]现仅指英文名来源';
comment on column IX_POI_ADDRESS.FULLNAME
  is '[170]';
comment on column IX_POI_ADDRESS.FULLNAME_PHONETIC
  is '[171U][170]';
comment on column IX_POI_ADDRESS.ROADNAME
  is '[173sp1]';
comment on column IX_POI_ADDRESS.ROADNAME_PHONETIC
  is '[173sp1]';
comment on column IX_POI_ADDRESS.ADDRNAME
  is '[173sp1]';
comment on column IX_POI_ADDRESS.ADDRNAME_PHONETIC
  is '[173sp1]';
comment on column IX_POI_ADDRESS.PROVINCE
  is 'POI标牌中的"省名/直辖市/自治区/特别行政区名"';
comment on column IX_POI_ADDRESS.CITY
  is 'POI标牌中的"地级市名/自治洲名"';
comment on column IX_POI_ADDRESS.COUNTY
  is 'POI标牌中的"县级市名/县名/区名(含直辖市的区)"';
comment on column IX_POI_ADDRESS.TOWN
  is '[170]乡镇街道办名称';
comment on column IX_POI_ADDRESS.PLACE
  is '[170]自然村落,居民小区,区域地名,开发区名';
comment on column IX_POI_ADDRESS.STREET
  is '[170]街道,道路名, 胡同,巷,条,弄';
comment on column IX_POI_ADDRESS.LANDMARK
  is '指有地理表示作用的店铺,公共设施,单位,建筑或交通运输设施,包括桥梁,公路环岛,交通站场等';
comment on column IX_POI_ADDRESS.PREFIX
  is '用于修饰门牌号号码的成分';
comment on column IX_POI_ADDRESS.HOUSENUM
  is '主门牌号号码,以序号方式命名的弄或条';
comment on column IX_POI_ADDRESS.TYPE
  is '门牌号号码类型';
comment on column IX_POI_ADDRESS.SUBNUM
  is '主门牌号所属的子门牌号及修饰该子门牌的前缀信息';
comment on column IX_POI_ADDRESS.SURFIX
  is '用于修饰门牌地址的词语,其本身没有实际意义,不影响门牌地址的含义,如:自编,临时';
comment on column IX_POI_ADDRESS.ESTAB
  is '如"**大厦","**小区"';
comment on column IX_POI_ADDRESS.BUILDING
  is '如"A栋,12栋,31楼,B座"等';
comment on column IX_POI_ADDRESS.FLOOR
  is '如"12层"';
comment on column IX_POI_ADDRESS.UNIT
  is '如"2门"';
comment on column IX_POI_ADDRESS.ROOM
  is '如"503室"';
comment on column IX_POI_ADDRESS.ADDONS
  is '[171U]如"对面,旁边,附近"';
comment on column IX_POI_ADDRESS.PROV_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.CITY_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.COUNTY_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.TOWN_PHONETIC
  is '[171U][170]';
comment on column IX_POI_ADDRESS.STREET_PHONETIC
  is '[171U][170]';
comment on column IX_POI_ADDRESS.PLACE_PHONETIC
  is '[171U][170]';
comment on column IX_POI_ADDRESS.LANDMARK_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.PREFIX_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.HOUSENUM_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.TYPE_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.SUBNUM_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.SURFIX_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.ESTAB_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.BUILDING_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.FLOOR_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.UNIT_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.ROOM_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.ADDONS_PHONETIC
  is '[171U]';
comment on column IX_POI_ADDRESS.U_RECORD
  is '增量更新标识';
comment on column IX_POI_ADDRESS.U_FIELDS
  is '记录更新的英文字段名,多个之间采用半角''|''分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_POI_ADDRESS
  add constraint PK_IX_POI_ADDRESS primary key (NAME_ID);
alter table IX_POI_ADDRESS
  add constraint IXPOI_ADDRESS foreign key (POI_PID)
  references IX_POI (PID)
  disable;
-- Create/Recreate check constraints 
alter table IX_POI_ADDRESS
  add check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR'));
alter table IX_POI_ADDRESS
  add check (SRC_FLAG in (0,1));
alter table IX_POI_ADDRESS
  add check (U_RECORD in (0,1,2,3));
-- Create/Recreate indexes 
create index EXP_IX_26 on IX_POI_ADDRESS (POI_PID);
create unique index IDX_20170414206192_R on IX_POI_ADDRESS (ROW_ID);

-- Create table
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
-- Add comments to the table 
comment on table RD_LINK
  is '道路Link是构成道路的基本元素,包括道路的几何形状,拓扑关系,以及基本的道路属性信息,如道路名称,道路种别,路径采纳,道路幅宽等.';
-- Add comments to the columns 
comment on column RD_LINK.LINK_PID
  is '主键';
comment on column RD_LINK.S_NODE_PID
  is '外键,引用"RD_NODE"';
comment on column RD_LINK.E_NODE_PID
  is '外键,引用"RD_NODE"';
comment on column RD_LINK.KIND
  is '[180U]存储为10进制数字,NaviMap显示为16进制形式
';
comment on column RD_LINK.DIRECT
  is '描述车辆在道路上的通行方向,用相对于Link方向的顺和逆来表示';
comment on column RD_LINK.APP_INFO
  is '[180U]描述道路通行信息';
comment on column RD_LINK.FUNCTION_CLASS
  is '[180U]';
comment on column RD_LINK.URBAN
  is '是否为城市道路';
comment on column RD_LINK.LANE_NUM
  is '[180U](1)单方向道路:只记录"总车道数"
(2)双方向道路:
如果左右车道数一致, 只记录"总车道数"
如果左右车道数不一致,则分别记录"左/右车道数"';
comment on column RD_LINK.LANE_WIDTH_LEFT
  is '[200]三维clm[210]修改字段含义';
comment on column RD_LINK.LANE_WIDTH_RIGHT
  is '[200]三维clm[210]修改字段含义';
comment on column RD_LINK.LANE_CLASS
  is '[180U]';
comment on column RD_LINK.WIDTH
  is '[180U]';
comment on column RD_LINK.LEFT_REGION_ID
  is '[170]参考"AD_ADMIN",通过区域号码找对应的左行政代码和左乡镇代码';
comment on column RD_LINK.RIGHT_REGION_ID
  is '[170]参考"AD_ADMIN",通过区域号码找对应的右行政代码和右乡镇代码';
comment on column RD_LINK.GEOMETRY
  is '(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';
comment on column RD_LINK.LENGTH
  is '单位:米';
comment on column RD_LINK.ONEWAY_MARK
  is '[181A]';
comment on column RD_LINK.STREET_LIGHT
  is '道路是否具有路灯之类的照明设施';
comment on column RD_LINK.PARKING_LOT
  is '道路是否具有停车带或停车位';
comment on column RD_LINK.ADAS_FLAG
  is '标志是否存在ADAS数据
[190]增加2:假';
comment on column RD_LINK.SIDEWALK_FLAG
  is '注:当标记值为2 时,在RD_LINK_SIDEWALK 中记录详细便道信息';
comment on column RD_LINK.WALKSTAIR_FLAG
  is '注: 标记值为2 时,在RD_LINK_WALKSTAIR 中记录详细阶梯信息';
comment on column RD_LINK.DICI_TYPE
  is '[180U]全要素或简化版';
comment on column RD_LINK.WALK_FLAG
  is '[180U]允许或禁止行人通行';
comment on column RD_LINK.DIF_GROUPID
  is '[172A]用于差分更新数据包的产品版本管理,如果存在多个,采用半角"|"分隔';
comment on column RD_LINK.SRC_FLAG
  is '[180A]13CY';
comment on column RD_LINK.DIGITAL_LEVEL
  is '[1802A]13CY';
comment on column RD_LINK.EDIT_FLAG
  is '[171A]用于数据完整提取时,区分是否可编辑';
comment on column RD_LINK.TRUCK_FLAG
  is '[200]卡车地图属性是否已验证';
comment on column RD_LINK.MEMO
  is '[171A]记录数据来源(参考影像更新或外包数据等)以及导入GDB 的时间或版本';
comment on column RD_LINK.RESERVED
  is '[1802A]';
comment on column RD_LINK.U_RECORD
  is '增量更新标识';
comment on column RD_LINK.U_FIELDS
  is '记录更新的英文字段名,多个之间采用半角''|''分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table RD_LINK
  add constraint PK_RD_LINK primary key (LINK_PID);
alter table RD_LINK
  add constraint RDLINK_ENODE foreign key (E_NODE_PID)
  references RD_NODE (NODE_PID)
  disable;
alter table RD_LINK
  add constraint RDLINK_SNODE foreign key (S_NODE_PID)
  references RD_NODE (NODE_PID)
  disable;
-- Create/Recreate check constraints 
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
-- Create/Recreate indexes 
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
 
-- Create table
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
-- Add comments to the columns 
comment on column RD_LINK_FORM.LINK_PID
  is '外键,引用"RD_LINK"';
comment on column RD_LINK_FORM.EXTENDED_FORM
  is '[171A]';
comment on column RD_LINK_FORM.AUXI_FLAG
  is '[171A]';
comment on column RD_LINK_FORM.KG_FLAG
  is '区分道路形态是K专用,G专用,KG共用的标志';
comment on column RD_LINK_FORM.U_RECORD
  is '增量更新标识';
comment on column RD_LINK_FORM.U_FIELDS
  is '记录更新的英文字段名,多个之间采用半角''|''分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table RD_LINK_FORM
  add constraint RDLINK_FORM foreign key (LINK_PID)
  references RD_LINK (LINK_PID)
  disable;
-- Create/Recreate check constraints 
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
-- Create/Recreate indexes 
create index EXP_RD_60 on RD_LINK_FORM (LINK_PID);
create index EXP_RD_D04 on RD_LINK_FORM (LINK_PID, FORM_OF_WAY);
create unique index IDX_20170414206221_R on RD_LINK_FORM (ROW_ID);


-- Create table
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
-- Add comments to the table 
comment on table IX_POI_FLAG
  is '[170]';
-- Add comments to the columns 
comment on column IX_POI_FLAG.POI_PID
  is '外键,引用"IX_POI"';
comment on column IX_POI_FLAG.FLAG_CODE
  is '参考"M_FLAG_CODE"';
comment on column IX_POI_FLAG.U_RECORD
  is '增量更新标识';
comment on column IX_POI_FLAG.U_FIELDS
  is '记录更新的英文字段名,多个之间采用半角''|''分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_POI_FLAG
  add constraint IXPOI_FLAG foreign key (POI_PID)
  references IX_POI (PID)
  disable;
-- Create/Recreate check constraints 
alter table IX_POI_FLAG
  add check (U_RECORD in (0,1,2,3));
-- Create/Recreate indexes 
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
  
  
-- Create table
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
-- Create/Recreate primary, unique and foreign key constraints 
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
-- Create/Recreate indexes 
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

  
  
-- Create table
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
-- Create/Recreate primary, unique and foreign key constraints 
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
-- Create/Recreate check constraints 
alter table LOG_DETAIL
  add check (OP_TP IN (0,1,2,3))
  disable;
alter table LOG_DETAIL
  add check (DES_STA IN (0,1))
  disable;
-- Create/Recreate indexes 
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

  
  -- Create table
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
-- Create/Recreate primary, unique and foreign key constraints 
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
-- Create/Recreate check constraints 
alter table LOG_OPERATION
  add check (COM_STA IN (0,1))
  disable;
alter table LOG_OPERATION
  add check (LOCK_STA IN (0,1))
  disable;
-- Create/Recreate indexes 
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

-- Create table
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
-- Add comments to the table 
comment on table LOG_DAY_RELEASE
  is '日库出品管理表';
-- Add comments to the columns 
comment on column LOG_DAY_RELEASE.OP_ID
  is '参考log_operation.op_id';
comment on column LOG_DAY_RELEASE.REL_POI_STA
  is 'POI出品状态';
comment on column LOG_DAY_RELEASE.REL_POI_DT
  is 'POI出品时间';
comment on column LOG_DAY_RELEASE.REL_ALL_STA
  is 'POI+ROAD出品状态';
comment on column LOG_DAY_RELEASE.REL_ALL_DT
  is 'POI+ROAD出品时间';
comment on column LOG_DAY_RELEASE.REL_POI_LOCK
  is 'POI 出品锁状态';
comment on column LOG_DAY_RELEASE.REL_ALL_LOCK
  is 'POI+ROAD出品锁状态';
-- Create/Recreate primary, unique and foreign key constraints 
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
-- Create/Recreate indexes 
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

  
-- Create table
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
-- Create/Recreate primary, unique and foreign key constraints 
alter table LOG_DETAIL_GRID
  add constraint FK_LOG_DETAIL_GRID_ROWID foreign key (LOG_ROW_ID)
  references LOG_DETAIL (ROW_ID)
  disable;
-- Create/Recreate check constraints 
alter table LOG_DETAIL_GRID
  add check (GRID_TYPE IN (0,1))
  disable;
-- Create/Recreate indexes 
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
  -- Create/Recreate primary, unique and foreign key constraints 
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
-- Add comments to the columns 
comment on column IX_POI_BACK.PID
  is '主键';
comment on column IX_POI_BACK.LINK_PID
  is '参考"RD_LINK"';
comment on column IX_POI_BACK.SIDE
  is '记录POI位于引导道路Link上,左侧或右侧';
comment on column IX_POI_BACK.NAME_GROUPID
  is '[173sp2]参考"RD_NAME"';
comment on column IX_POI_BACK.PMESH_ID
  is '[171A]每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';
-- Create/Recreate check constraints 
alter table IX_POI_BACK
  add check (SIDE in (0,1,2,3));
-- Create/Recreate indexes 
create index EXP_IX_POI_BACK_01 on IX_POI_BACK (MESH_ID, LINK_PID)
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
create index EXP_IX_POI_BACK_1001 on IX_POI_BACK (POI_NUM)
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
create index EXP_IX_POI_BACK_49 on IX_POI_BACK (KIND_CODE)
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
create index EXP_IX_POI_BACK_D01 on IX_POI_BACK (CHAIN, TYPE)
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
create unique index IDX_POI_BACK_20170411032882_R on IX_POI_BACK (ROW_ID)
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
  
  -- Create table
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
-- Add comments to the table 
comment on table IX_POI_FLAG_BACK
  is '[170]';
-- Add comments to the columns 
comment on column IX_POI_FLAG_BACK.POI_PID
  is '外键,引用"IX_POI"';
comment on column IX_POI_FLAG_BACK.FLAG_CODE
  is '参考"M_FLAG_CODE"';
comment on column IX_POI_FLAG_BACK.U_RECORD
  is '增量更新标识';
comment on column IX_POI_FLAG_BACK.U_FIELDS
  is '记录更新的英文字段名,多个之间采用半角''|''分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_POI_FLAG_BACK
  add constraint IXPOI_FLAG_BACK foreign key (POI_PID)
  references IX_POI_BACK (PID)
  disable;
-- Create/Recreate check constraints 
alter table IX_POI_FLAG_BACK
  add check (U_RECORD in (0,1,2,3));
  -- Create/Recreate indexes 
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
create unique index IDX_POI_FLAG_BACK_20170411032958_R on IX_POI_FLAG_BACK (ROW_ID)
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