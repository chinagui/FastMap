/*==============================================================*/
/* Table: IX_POI                                                */
/*==============================================================*/
create table IX_POI 
(
   PID                  NUMBER(10)           not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_IX_POI check (SIDE in (0,1,2,3)) disable ,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_FLAG_IX_POI check (ROAD_FLAG in (0,1,2,3)) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   ADMIN_REAL           NUMBER(6)            default 0 not null,
   IMPORTANCE           NUMBER(1)            default 0 not null
      constraint CKC_IMPORTANCE_IX_POI check (IMPORTANCE in (0,1)) disable ,
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)            default 0 not null
      constraint CKC_ACCESS_FLAG_IX_POI check (ACCESS_FLAG in (0,1,2)) disable ,
   OPEN_24H             NUMBER(1)            default 0 not null
      constraint CKC_OPEN_24H_IX_POI check (OPEN_24H in (0,1,2)) disable ,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   POST_CODE            VARCHAR2(6),
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_POI check (EDIT_FLAG in (0,1)) disable ,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_IX_POI check (STATE in (0,1,2,3)) disable ,
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(32),
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_IX_POI check (TYPE in (0,1)) disable ,
   ADDRESS_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_ADDRESS_FLAG_IX_POI check (ADDRESS_FLAG in (0,1,9)) disable ,
   EX_PRIORITY          VARCHAR2(10),
   EDITION_FLAG         VARCHAR2(12),
   POI_MEMO             VARCHAR2(200),
   OLD_BLOCKCODE        VARCHAR2(200),
   OLD_NAME             VARCHAR2(200),
   OLD_ADDRESS          VARCHAR2(200),
   OLD_KIND             VARCHAR2(8),
   POI_NUM              VARCHAR2(20),
   LOG                  VARCHAR2(200),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI primary key (PID)
);

comment on column IX_POI.PID is
'主键';

comment on column IX_POI.KIND_CODE is
'参考"IX_POI_CODE"';

comment on column IX_POI.GEOMETRY is
'存储以"度"为单位的经纬度坐标点,用于POI显示和计算Link左右关系
';

comment on column IX_POI.LINK_PID is
'参考"RD_LINK"';

comment on column IX_POI.SIDE is
'记录POI位于引导道路Link上,左侧或右侧';

comment on column IX_POI.NAME_GROUPID is
'[173sp2]参考"RD_NAME"';

comment on column IX_POI.ROAD_FLAG is
'[170]';

comment on column IX_POI.PMESH_ID is
'[171A]每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';

comment on column IX_POI.IMPORTANCE is
'记录以下分类的POI为重要,即IMPORTANCE为1,否则为0
(1)拥有国际进出港口的机场
(2)国家旅游局评定的等级为3A,4A,5A的风景区
(3)世界文化遗产';

comment on column IX_POI.CHAIN is
'[171U]主要制作对象是宾馆和加油站';

comment on column IX_POI.ACCESS_FLAG is
'[170]';

comment on column IX_POI.OPEN_24H is
'[171U]';

comment on column IX_POI.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_POI.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_POI.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_POI.STATE is
'[170]';

comment on column IX_POI.FIELD_STATE is
'[170]改名称,改地址,改分类';

comment on column IX_POI.LABEL is
'记录路,水,绿地,单项收费,双向收费,显示位置,24小时便利店';

comment on column IX_POI.TYPE is
'[170]';

comment on column IX_POI.ADDRESS_FLAG is
'标志POI 地址(IX_POI_ADDRESS)完整性';

comment on column IX_POI.EX_PRIORITY is
'[171A]提取的优先级别(城区为A1~A11;县乡为B2~B5)';

comment on column IX_POI.EDITION_FLAG is
'记录数据是由内业还是外业修改,新增,删除等标志';

comment on column IX_POI.OLD_BLOCKCODE is
'原结构中的"OLD大字"';

comment on column IX_POI.OLD_KIND is
'[170]';

comment on column IX_POI.POI_NUM is
'记录来自NIDB的POI编号';

comment on column IX_POI.TASK_ID is
'[170]记录内业的任务编号';

comment on column IX_POI.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column IX_POI.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column IX_POI.U_RECORD is
'增量更新标识';

comment on column IX_POI.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';


/*==============================================================*/
/* Table: IX_POI_ADDRESS                                        */
/*==============================================================*/
create table IX_POI_ADDRESS 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_IX_POI_A check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_IX_POI_A check (SRC_FLAG in (0,1)) disable ,
   FULLNAME             VARCHAR2(500),
   FULLNAME_PHONETIC    VARCHAR2(1000),
   ROADNAME             VARCHAR2(500),
   ROADNAME_PHONETIC    VARCHAR2(1000),
   ADDRNAME             VARCHAR2(500),
   ADDRNAME_PHONETIC    VARCHAR2(1000),
   PROVINCE             VARCHAR2(64),
   CITY                 VARCHAR2(64),
   COUNTY               VARCHAR2(64),
   TOWN                 VARCHAR2(200),
   PLACE                VARCHAR2(100),
   STREET               VARCHAR2(100),
   LANDMARK             VARCHAR2(100),
   PREFIX               VARCHAR2(64),
   HOUSENUM             VARCHAR2(64),
   TYPE                 VARCHAR2(32),
   SUBNUM               VARCHAR2(64),
   SURFIX               VARCHAR2(64),
   ESTAB                VARCHAR2(64),
   BUILDING             VARCHAR2(100),
   FLOOR                VARCHAR2(64),
   UNIT                 VARCHAR2(64),
   ROOM                 VARCHAR2(64),
   ADDONS               VARCHAR2(200),
   PROV_PHONETIC        VARCHAR2(1000),
   CITY_PHONETIC        VARCHAR2(1000),
   COUNTY_PHONETIC      VARCHAR2(1000),
   TOWN_PHONETIC        VARCHAR2(1000),
   STREET_PHONETIC      VARCHAR2(1000),
   PLACE_PHONETIC       VARCHAR2(1000),
   LANDMARK_PHONETIC    VARCHAR2(1000),
   PREFIX_PHONETIC      VARCHAR2(1000),
   HOUSENUM_PHONETIC    VARCHAR2(1000),
   TYPE_PHONETIC        VARCHAR2(1000),
   SUBNUM_PHONETIC      VARCHAR2(1000),
   SURFIX_PHONETIC      VARCHAR2(1000),
   ESTAB_PHONETIC       VARCHAR2(1000),
   BUILDING_PHONETIC    VARCHAR2(1000),
   FLOOR_PHONETIC       VARCHAR2(1000),
   UNIT_PHONETIC        VARCHAR2(1000),
   ROOM_PHONETIC        VARCHAR2(1000),
   ADDONS_PHONETIC      VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_ADDRESS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ADDRESS primary key (NAME_ID),
   constraint IXPOI_ADDRESS foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on column IX_POI_ADDRESS.NAME_ID is
'[170]主键';

comment on column IX_POI_ADDRESS.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column IX_POI_ADDRESS.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_ADDRESS.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column IX_POI_ADDRESS.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column IX_POI_ADDRESS.FULLNAME is
'[170]';

comment on column IX_POI_ADDRESS.FULLNAME_PHONETIC is
'[171U][170]';

comment on column IX_POI_ADDRESS.ROADNAME is
'[173sp1]';

comment on column IX_POI_ADDRESS.ROADNAME_PHONETIC is
'[173sp1]';

comment on column IX_POI_ADDRESS.ADDRNAME is
'[173sp1]';

comment on column IX_POI_ADDRESS.ADDRNAME_PHONETIC is
'[173sp1]';

comment on column IX_POI_ADDRESS.PROVINCE is
'POI标牌中的"省名/直辖市/自治区/特别行政区名"';

comment on column IX_POI_ADDRESS.CITY is
'POI标牌中的"地级市名/自治洲名"';

comment on column IX_POI_ADDRESS.COUNTY is
'POI标牌中的"县级市名/县名/区名(含直辖市的区)"';

comment on column IX_POI_ADDRESS.TOWN is
'[170]乡镇街道办名称';

comment on column IX_POI_ADDRESS.STREET is
'[170]街道,道路名, 胡同,巷,条,弄';

comment on column IX_POI_ADDRESS.PLACE is
'[170]自然村落,居民小区,区域地名,开发区名';

comment on column IX_POI_ADDRESS.LANDMARK is
'指有地理表示作用的店铺,公共设施,单位,建筑或交通运输设施,包括桥梁,公路环岛,交通站场等';

comment on column IX_POI_ADDRESS.PREFIX is
'用于修饰门牌号号码的成分';

comment on column IX_POI_ADDRESS.HOUSENUM is
'主门牌号号码,以序号方式命名的弄或条';

comment on column IX_POI_ADDRESS.TYPE is
'门牌号号码类型';

comment on column IX_POI_ADDRESS.SUBNUM is
'主门牌号所属的子门牌号及修饰该子门牌的前缀信息';

comment on column IX_POI_ADDRESS.SURFIX is
'用于修饰门牌地址的词语,其本身没有实际意义,不影响门牌地址的含义,如:自编,临时';

comment on column IX_POI_ADDRESS.ESTAB is
'如"**大厦","**小区"';

comment on column IX_POI_ADDRESS.BUILDING is
'如"A栋,12栋,31楼,B座"等';

comment on column IX_POI_ADDRESS.FLOOR is
'如"12层"';

comment on column IX_POI_ADDRESS.UNIT is
'如"2门"';

comment on column IX_POI_ADDRESS.ROOM is
'如"503室"';

comment on column IX_POI_ADDRESS.ADDONS is
'[171U]如"对面,旁边,附近"';

comment on column IX_POI_ADDRESS.PROV_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.CITY_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.COUNTY_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.TOWN_PHONETIC is
'[171U][170]';

comment on column IX_POI_ADDRESS.STREET_PHONETIC is
'[171U][170]';

comment on column IX_POI_ADDRESS.PLACE_PHONETIC is
'[171U][170]';

comment on column IX_POI_ADDRESS.LANDMARK_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.PREFIX_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.HOUSENUM_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.TYPE_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.SUBNUM_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.SURFIX_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.ESTAB_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.BUILDING_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.FLOOR_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.UNIT_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.ROOM_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.ADDONS_PHONETIC is
'[171U]';

comment on column IX_POI_ADDRESS.U_RECORD is
'增量更新标识';

comment on column IX_POI_ADDRESS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';
/*==============================================================*/
/* Table: IX_POI_ADVERTISEMENT                                  */
/*==============================================================*/
create table IX_POI_ADVERTISEMENT 
(
   ADVERTISE_ID         NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   LABEL_TEXT           VARCHAR2(500),
   TYPE                 VARCHAR2(100),
   PRIORITY             NUMBER(2)            default 1 not null
      constraint CKC_PRIORITY_IX_POI_A check (PRIORITY in (1,2,3,4,5,6,7,8,9)) disable ,
   START_TIME           VARCHAR2(100),
   END_TIME             VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_ADVERTISE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ADVERTISEMENT primary key (ADVERTISE_ID)
);

comment on table IX_POI_ADVERTISEMENT is
'[170]';

comment on column IX_POI_ADVERTISEMENT.ADVERTISE_ID is
'主键';

comment on column IX_POI_ADVERTISEMENT.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_ADVERTISEMENT.TYPE is
'0:入库
1:特殊显示
2:广告语
3:语音提示
多个功能时采用英文半角”|”分隔
如果为空表示未调查
';

comment on column IX_POI_ADVERTISEMENT.START_TIME is
'格式:YYYY:MM:DD
多个时间段时采用英文半角”|”分隔';

comment on column IX_POI_ADVERTISEMENT.END_TIME is
'格式:YYYY:MM:DD
多个时间段时采用英文半角”|”分隔';

comment on column IX_POI_ADVERTISEMENT.U_RECORD is
'增量更新标识';

comment on column IX_POI_ADVERTISEMENT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_ATTRACTION                                     */
/*==============================================================*/
create table IX_POI_ATTRACTION 
(
   ATTRACTION_ID        NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   SIGHT_LEVEL          NUMBER(2)            default 0 not null
      constraint CKC_SIGHT_LEVEL_IX_POI_A check (SIGHT_LEVEL in (0,1,2,3,4,5)) disable ,
   CREDIT_CARD          VARCHAR2(100),
   BUSY_SEASON          VARCHAR2(100),
   OFF_SEASON           VARCHAR2(100),
   TICKET_TYPE          VARCHAR2(100),
   BUSY_PRICE           VARCHAR2(100),
   OFF_PRICE            VARCHAR2(100),
   PARKING              NUMBER(2)            default 0 not null
      constraint CKC_PARKING_IX_POI_A check (PARKING in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_ATTRACTION check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ATTRACTION primary key (ATTRACTION_ID)
);

comment on table IX_POI_ATTRACTION is
'[170]';

comment on column IX_POI_ATTRACTION.ATTRACTION_ID is
'主键';

comment on column IX_POI_ATTRACTION.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_ATTRACTION.CREDIT_CARD is
'值域包括:
1 维士(visa)
2 万事达(mastercard)
3 大来(dinas)
4 日本国际信用卡(jcb)
5 美国运通(America
Express)
6 银联(unionpay)
多个类型时采用英文半角”|”分隔
如果为0 表示不支持信用卡
如果为空表示未调查';

comment on column IX_POI_ATTRACTION.BUSY_SEASON is
'格式:MM:DD-MM:DD
起始和终止时间之间采用英文半角”-”连接
多个时间段时采用英文半角”|”分隔';

comment on column IX_POI_ATTRACTION.OFF_SEASON is
'格式:MM:DD-MM:DD
起始和终止时间之间采用英文半角”-”连接
多个时间段时采用英文半角”|”分隔';

comment on column IX_POI_ATTRACTION.TICKET_TYPE is
'值域包括:
1 成人票
2 儿童票/老人票
3 学生票
多个类型时采用英文半角”|”分隔
如果为空表示未调查';

comment on column IX_POI_ATTRACTION.BUSY_PRICE is
'多个价格时采用英文半角”|”分隔,且顺序与门票类型一致, 如果为空表示未调查';

comment on column IX_POI_ATTRACTION.OFF_PRICE is
'多个价格时采用英文半角”|”分隔,且顺序与门票类型一致, 如果为空表示未调查';

comment on column IX_POI_ATTRACTION.U_RECORD is
'增量更新标识';

comment on column IX_POI_ATTRACTION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_AUDIO                                          */
/*==============================================================*/
create table IX_POI_AUDIO 
(
   POI_PID              NUMBER(10)           not null,
   AUDIO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_AUDIO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_AUDIO foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_AUDIO is
'[170]';

comment on column IX_POI_AUDIO.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_AUDIO.AUDIO_ID is
'参考"AU_AUDIO"';

comment on column IX_POI_AUDIO.STATUS is
'记录是否确认';

comment on column IX_POI_AUDIO.U_RECORD is
'增量更新标识';

comment on column IX_POI_AUDIO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_BUSINESSTIME                                   */
/*==============================================================*/
create table IX_POI_BUSINESSTIME 
(
   BUSINESSTIME_ID      NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   TIME_DOMAIN          VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_BUSINESSTI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_BUSINESSTIME primary key (BUSINESSTIME_ID)
);

comment on table IX_POI_BUSINESSTIME is
'[171A]';

comment on column IX_POI_BUSINESSTIME.BUSINESSTIME_ID is
'主键';

comment on column IX_POI_BUSINESSTIME.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_BUSINESSTIME.TIME_DOMAIN is
'格式参考"时间域"';

comment on column IX_POI_BUSINESSTIME.U_RECORD is
'增量更新标识';

comment on column IX_POI_BUSINESSTIME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_CHARGINGSTATION                                */
/*==============================================================*/
create table IX_POI_CHARGINGSTATION 
(
   CHARGING_ID          NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   CHARGING_TYPE        NUMBER(2)            default 2 not null
      constraint CKC_CHARGING_TYPE_IX_POI_C check (CHARGING_TYPE in (1,2,3,4)) disable ,
   CHARGING_NUM         VARCHAR2(5),
   EXCHANGE_NUM         VARCHAR2(5),
   PAYMENT              VARCHAR2(100),
   SERVICE_PROV         VARCHAR2(8),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_CHARGINGST check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_CHARGINGSTATION primary key (CHARGING_ID)
);

comment on table IX_POI_CHARGINGSTATION is
'[170]';

comment on column IX_POI_CHARGINGSTATION.CHARGING_ID is
'主键';

comment on column IX_POI_CHARGINGSTATION.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_CHARGINGSTATION.CHARGING_TYPE is
'[180U]';

comment on column IX_POI_CHARGINGSTATION.CHARGING_NUM is
'大于等于0,空表示未调查';

comment on column IX_POI_CHARGINGSTATION.EXCHANGE_NUM is
'空表示未调查';

comment on column IX_POI_CHARGINGSTATION.PAYMENT is
'[180U]值域包括:
代码	名称
0	其他 
1	现金
2	信用卡
3	IC卡
4	特制充值卡
多种付费方式时采用英文半角”|”分隔
如果为空表示未调查
';

comment on column IX_POI_CHARGINGSTATION.SERVICE_PROV is
'[180A]值域包括:
0	其它
1	国家电网
2	南方电网
3	中石油
4	中石化
5	中海油
如果为空表示未调查';

comment on column IX_POI_CHARGINGSTATION.MEMO is
'[180A]';

comment on column IX_POI_CHARGINGSTATION.U_RECORD is
'增量更新标识';

comment on column IX_POI_CHARGINGSTATION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_CHARGINGPLOT                                   */
/*==============================================================*/
create table IX_POI_CHARGINGPLOT 
(
   CHARGING_ID          NUMBER(10)           not null,
   GROUP_ID             NUMBER(5)            default 1 not null,
   COUNT                NUMBER(5)            default 1 not null,
   ACDC                 NUMBER(5)            default 0 not null
      constraint CKC_ACDC_IX_POI_C check (ACDC in (0,1)) disable ,
   PLUG_TYPE            VARCHAR2(100),
   POWER                VARCHAR2(100),
   VOLTAGE              VARCHAR2(100),
   "CURRENT"            VARCHAR2(100),
   "MODE"               NUMBER(2)            default 0
      constraint CKC_MODE_IX_POI_C check ("MODE" is null or ("MODE" in (0,1))) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_CHARGINGP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOICHARGINGSTATION_PLOT foreign key (CHARGING_ID)
         references IX_POI_CHARGINGSTATION (CHARGING_ID) disable
);

comment on table IX_POI_CHARGINGPLOT is
'[170]';

comment on column IX_POI_CHARGINGPLOT.CHARGING_ID is
'外键,引用"IX_POI_CHARGINESTATION"';

comment on column IX_POI_CHARGINGPLOT.GROUP_ID is
'交/直流电,插头类型,充电功率和电压都相同的充电桩为一组';

comment on column IX_POI_CHARGINGPLOT.COUNT is
'同一组内的充电桩个数';

comment on column IX_POI_CHARGINGPLOT.ACDC is
'[180U]';

comment on column IX_POI_CHARGINGPLOT.PLUG_TYPE is
'[180U]值域包括:
代码	名称
0	其他
1	交流电3孔插槽
2	交流电7孔插槽
3	直流电9孔插槽
多个类型时采用英文半角”|”分隔
如果为空表示未调查';

comment on column IX_POI_CHARGINGPLOT.POWER is
'单位为KW,不同功率之间采用英文半角”|”分隔';

comment on column IX_POI_CHARGINGPLOT.VOLTAGE is
'单位为V,不同电压之间采用英文半角”|”分隔,与交流电充电功率一一对应';

comment on column IX_POI_CHARGINGPLOT."CURRENT" is
'[180A]单位为A,不同电流之间采用英文半角”|”分隔';

comment on column IX_POI_CHARGINGPLOT."MODE" is
'[180A]';

comment on column IX_POI_CHARGINGPLOT.MEMO is
'[180A]';

comment on column IX_POI_CHARGINGPLOT.U_RECORD is
'增量更新标识';

comment on column IX_POI_CHARGINGPLOT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_PARENT                                         */
/*==============================================================*/
create table IX_POI_PARENT 
(
   GROUP_ID             NUMBER(10)           not null,
   PARENT_POI_PID       NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_PARENT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_PARENT primary key (GROUP_ID),
   constraint IXPOI_PARENT foreign key (PARENT_POI_PID)
         references IX_POI (PID) disable
);

comment on column IX_POI_PARENT.GROUP_ID is
'主键';

comment on column IX_POI_PARENT.PARENT_POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_PARENT.U_RECORD is
'增量更新标识';

comment on column IX_POI_PARENT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_CHILDREN                                       */
/*==============================================================*/
create table IX_POI_CHILDREN 
(
   GROUP_ID             NUMBER(10)           not null,
   CHILD_POI_PID        NUMBER(10)           not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
      constraint CKC_RELATION_TYPE_IX_POI_C check (RELATION_TYPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_CHILDREN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_CHILD foreign key (CHILD_POI_PID)
         references IX_POI (PID) disable ,
   constraint IXPOI_PARENT_CHILD foreign key (GROUP_ID)
         references IX_POI_PARENT (GROUP_ID) disable
);

comment on column IX_POI_CHILDREN.GROUP_ID is
'外键,引用"IX_POI_PARENT"';

comment on column IX_POI_CHILDREN.CHILD_POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_CHILDREN.U_RECORD is
'增量更新标识';

comment on column IX_POI_CHILDREN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_CODE                                           */
/*==============================================================*/
create table IX_POI_CODE 
(
   KIND_CODE            VARCHAR2(8)          not null,
   KIND_NAME            VARCHAR2(200),
   CLASS_CODE           VARCHAR2(32),
   CLASS_NAME           VARCHAR2(200),
   SUB_CLASS_CODE       VARCHAR2(32),
   SUB_CLASS_NAME       VARCHAR2(200),
   "LEVEL"              VARCHAR2(12),
   MHM_DES              VARCHAR2(1000),
   KG_DES               VARCHAR2(1000),
   COL_DES              VARCHAR2(1000),
   FLAG                 VARCHAR2(32),
   DESCRIPT             VARCHAR2(200),
   constraint PK_IX_POI_CODE primary key (KIND_CODE)
);

comment on table IX_POI_CODE is
'POI的分类代码,参见附录"POI分类表"';

comment on column IX_POI_CODE.KIND_CODE is
'主键';

comment on column IX_POI_CODE.CLASS_CODE is
'大分类代码,如餐饮,零售业,汽车,住宿,休闲与娱乐等';

comment on column IX_POI_CODE.SUB_CLASS_CODE is
'中分类,如餐饮大分类中的快餐,异国风味,中餐馆等';

comment on column IX_POI_CODE."LEVEL" is
'[180A]参考"M_FLAG_CODE"';

comment on column IX_POI_CODE.MHM_DES is
'[173sp2]';

comment on column IX_POI_CODE.KG_DES is
'[173sp2]';

comment on column IX_POI_CODE.COL_DES is
'[173sp2]';

comment on column IX_POI_CODE.FLAG is
'[173sp2]';

/*==============================================================*/
/* Table: IX_POI_CONTACT                                        */
/*==============================================================*/
create table IX_POI_CONTACT 
(
   POI_PID              NUMBER(10)           not null,
   CONTACT_TYPE         NUMBER(2)            default 1 not null
      constraint CKC_CONTACT_TYPE_IX_POI_C check (CONTACT_TYPE in (1,2,3,4,11,21,22)) disable ,
   CONTACT              VARCHAR2(128),
   CONTACT_DEPART       NUMBER(3)            default 0 not null,
   PRIORITY             NUMBER(1)            default 0 not null
      constraint CKC_PRIORITY_IX_POI_C check (PRIORITY in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_CONTACT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_TELEPHONE foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_CONTACT is
'当存在多个联系方式时,存储为多条记录';

comment on column IX_POI_CONTACT.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_CONTACT.CONTACT is
'记录包括区号在内的电话号码,存储为英文半角数字字符,二者之间以半角"-"分隔,如010-82306399';

comment on column IX_POI_CONTACT.CONTACT_DEPART is
'采用8bit 表示,从右到左依次为0~7bit,每bit 表示一个服务部门(如下),赋值为0/1 分别表示否/是,如:00000011 表示总机和客服;00000101 表示总机和预订
第0bit:总机
第1bit:客服
第2bit:预订
第3bit:销售
第4bit:维修
第5bit:其他
如果所有bit 位均为0,表示未调查';

comment on column IX_POI_CONTACT.PRIORITY is
'是否优先选择的联系方式';

comment on column IX_POI_CONTACT.U_RECORD is
'增量更新标识';

comment on column IX_POI_CONTACT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_ENTRYIMAGE                                     */
/*==============================================================*/
create table IX_POI_ENTRYIMAGE 
(
   POI_PID              NUMBER(10)           not null,
   IMAGE_CODE           VARCHAR2(8),
   X_PIXEL              NUMBER(5)            default 0 not null,
   Y_PIXEL              NUMBER(5)            default 0 not null,
   MEMO                 VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_E check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_ENTRYIMAGE foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_ENTRYIMAGE is
'[170]';

comment on column IX_POI_ENTRYIMAGE.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_ENTRYIMAGE.IMAGE_CODE is
'图形文件名称';

comment on column IX_POI_ENTRYIMAGE.X_PIXEL is
'POI 在概略图内X 轴像素坐标';

comment on column IX_POI_ENTRYIMAGE.Y_PIXEL is
'POI 在概略图内Y 轴像素坐标';

comment on column IX_POI_ENTRYIMAGE.U_RECORD is
'增量更新标识';

comment on column IX_POI_ENTRYIMAGE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_FLAG                                           */
/*==============================================================*/
create table IX_POI_FLAG 
(
   POI_PID              NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_F check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_FLAG foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_FLAG is
'[170]';

comment on column IX_POI_FLAG.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column IX_POI_FLAG.U_RECORD is
'增量更新标识';

comment on column IX_POI_FLAG.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_GASSTATION                                     */
/*==============================================================*/
create table IX_POI_GASSTATION 
(
   GASSTATION_ID        NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   FUEL_TYPE            VARCHAR2(100),
   OIL_TYPE             VARCHAR2(100),
   EG_TYPE              VARCHAR2(100),
   OIL_CARD             VARCHAR2(100),
   CREDIT_CARD          VARCHAR2(100),
   SERVICE              VARCHAR2(20),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_G check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_GASSTATION primary key (GASSTATION_ID)
);

comment on table IX_POI_GASSTATION is
'[170]';

comment on column IX_POI_GASSTATION.GASSTATION_ID is
'主键';

comment on column IX_POI_GASSTATION.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_GASSTATION.FUEL_TYPE is
'[180A]值域包括:
代码	名称
0	其它
1	汽油(Gasoline)
2	柴油(Diesel)
4	液化石油气(LPG)
5	天然气(CNG)
6	乙醇汽油(E10)
7	氢燃料(Hydrogen)
8	生物柴油(Biodiesel)
9	液化天然气(LNG)
多种类型时采用英文半角”|”分隔
如果为空表示未调查
';

comment on column IX_POI_GASSTATION.OIL_TYPE is
'[180U]值域包括:
代码	名称
0	其它
90	90#汽油
93	93#汽油
97	97#汽油
98	98#汽油 
多种类型时采用英文半角”|”分隔
如果为空表示未调查
注:当FUEL_TYPE=1(汽油)时有值,其他为空
';

comment on column IX_POI_GASSTATION.EG_TYPE is
'[180A]值域包括:
代码	名称
0	其它
E90	E90#汽油
E93	E93#汽油
E97	E97#汽油
多种类型时采用英文半角”|”分隔
如果为空表示未调查
注:当FUEL_TYPE=6(乙醇汽油)时有值,其他为空';

comment on column IX_POI_GASSTATION.OIL_CARD is
'0 不支持
1 支持
空表示未调查';

comment on column IX_POI_GASSTATION.CREDIT_CARD is
'值域包括:
1 维士(visa)
2 万事达(mastercard)
3 大来(dinas)
4 日本国际信用卡(jcb)
5 美国运通(America
Express)
6 银联(unionpay)
多个类型时采用英文半角”|”分隔
如果为0 表示不支持信用卡
如果为空表示未调查';

comment on column IX_POI_GASSTATION.SERVICE is
'[180U]值域包括:
代码 名称
1 便利店
2 洗车
3汽车维修 
多个服务时采用英文半角”|”分隔
如果为空表示未调查';

comment on column IX_POI_GASSTATION.MEMO is
'[180A]';

comment on column IX_POI_GASSTATION.U_RECORD is
'增量更新标识';

comment on column IX_POI_GASSTATION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_HOTEL                                          */
/*==============================================================*/
create table IX_POI_HOTEL 
(
   HOTEL_ID             NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   CREDIT_CARD          VARCHAR2(100),
   RATING               NUMBER(2)            default 0 not null
      constraint CKC_RATING_IX_POI_H check (RATING in (0,1,2,3,4,5,6,12,13,14,15)) disable ,
   CHECKIN_TIME         VARCHAR2(100)        default '14:00' not null,
   CHECKOUT_TIME        VARCHAR2(100)        default '12:00' not null,
   ROOM_COUNT           NUMBER(5)            default 0 not null,
   ROOM_TYPE            VARCHAR2(100),
   ROOM_PRICE           VARCHAR2(100),
   BREAKFAST            NUMBER(2)            default 0 not null
      constraint CKC_BREAKFAST_IX_POI_H check (BREAKFAST in (0,1)) disable ,
   SERVICE              VARCHAR2(1000),
   PARKING              NUMBER(2)            default 0 not null
      constraint CKC_PARKING_IX_POI_H check (PARKING in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_H check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_HOTEL primary key (HOTEL_ID)
);

comment on table IX_POI_HOTEL is
'[170]';

comment on column IX_POI_HOTEL.HOTEL_ID is
'主键';

comment on column IX_POI_HOTEL.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_HOTEL.CREDIT_CARD is
'值域包括:
1 维士(visa)
2 万事达(mastercard)
3 大来(dinas)
4 日本国际信用卡(jcb)
5 美国运通(America
Express)
6 银联(unionpay)
多个类型时采用英文半角”|”分隔
如果为0 表示不支持信用卡
如果为空表示未调查';

comment on column IX_POI_HOTEL.CHECKIN_TIME is
'格式:HH:mm';

comment on column IX_POI_HOTEL.CHECKOUT_TIME is
'格式:HH:mm';

comment on column IX_POI_HOTEL.ROOM_COUNT is
'大于等于0 的整数,0 表示未调查';

comment on column IX_POI_HOTEL.ROOM_TYPE is
'值域包括:
1 单人间(single)
2 标准间(double)
3 套房(suite)
多个类型时采用英文半角"|"分隔
如果为空表示未调查';

comment on column IX_POI_HOTEL.ROOM_PRICE is
'多个价格时采用英文半角"|"分隔,顺序必须与客房类型一致
如果为空表示未调查';

comment on column IX_POI_HOTEL.SERVICE is
'值域包括:
1 会议厅
2 商务中心
31 酒吧
32 卡拉OK
33 健身中心
34 室内游泳池
35 SPA
36 桑拿
51 中餐厅
52 西餐厅
53 咖啡厅
54 茶室
多个服务时采用英文半角"|"分隔
如果为空表示未调查';

comment on column IX_POI_HOTEL.U_RECORD is
'增量更新标识';

comment on column IX_POI_HOTEL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_ICON                                           */
/*==============================================================*/
create table IX_POI_ICON 
(
   REL_ID               NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   ICON_NAME            VARCHAR2(100),
   GEOMETRY             SDO_GEOMETRY,
   MANAGE_CODE          VARCHAR2(100),
   CLIENT_FLAG          VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_ICON check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ICON primary key (REL_ID),
   constraint IXPOI_ICON foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_ICON is
'记录POI的3DIcon信息';

comment on column IX_POI_ICON.REL_ID is
'主键';

comment on column IX_POI_ICON.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_ICON.ICON_NAME is
'[170]参考"AU_MULTIMEDIA"中"NAME"';

comment on column IX_POI_ICON.GEOMETRY is
'[173sp1]';

comment on column IX_POI_ICON.MANAGE_CODE is
'[170]';

comment on column IX_POI_ICON.CLIENT_FLAG is
'[170]根据不同客户需求,输出不同文字,值域包括:
MB 三菱
HD 广本
TY 丰田
PI 先锋
PA 松下
NE NavEx
注:
(1)以上每一代码表示只输出给某一客户,如只给三菱,表示为"MB"
(2)如果表示输出给除某一客户外的其他客户,则在以上代码前加英文半角"-",如输出给除三菱外的客户,则表示为"-MB"
(3)多个之间以英文半角"|"分隔,如表示输出给三菱而不给丰田,则表示为"MB|-TY"
(4)默认为空,表示所有客户都输出';

comment on column IX_POI_ICON.MEMO is
'[170]';

comment on column IX_POI_ICON.U_RECORD is
'增量更新标识';

comment on column IX_POI_ICON.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_INTRODUCTION                                   */
/*==============================================================*/
create table IX_POI_INTRODUCTION 
(
   INTRODUCTION_ID      NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   INTRODUCTION         VARCHAR2(1000),
   INTRODUCTION_ENG     VARCHAR2(1000),
   WEBSITE              VARCHAR2(500),
   NEIGHBOR             VARCHAR2(500),
   NEIGHBOR_ENG         VARCHAR2(500),
   TRAFFIC              VARCHAR2(500),
   TRAFFIC_ENG          VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_INSTRODUCT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_INTRODUCTION primary key (INTRODUCTION_ID)
);

comment on table IX_POI_INTRODUCTION is
'[170]记录当前POI 附近的行政机构,大型商场,公园等地标建筑,或飞机场,火车站,客运站,地铁站等周边交通枢纽,以及POI 距这些地标或枢纽的距离,单位:KM';

comment on column IX_POI_INTRODUCTION.INTRODUCTION_ID is
'主键';

comment on column IX_POI_INTRODUCTION.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_INTRODUCTION.INTRODUCTION is
'中文全角字符, 标点符号符合中文使用习惯';

comment on column IX_POI_INTRODUCTION.NEIGHBOR is
'(1)POI 附近的地标性建筑物,或行政机构,大型商场,公园及其他著名游览景点
(2)中文全角字符,多个地标时以”｜”分隔,如:北京天伦王朝酒店 紫禁城｜天安门广场｜王府井大街｜圣约瑟堂';

comment on column IX_POI_INTRODUCTION.TRAFFIC is
'(1)POI 附近的大型交通枢纽(主要有机场,火车站,汽车客运站,港口码头)以及距这些枢纽的大致距离
(2)中文全角字符,枢纽后括号内为距此枢纽的大致距离,单位为KM,多个枢纽时以”｜”分隔,如:北京天伦王朝酒店 北京首都国际机场（３０）｜北京火车站（３.５）';

comment on column IX_POI_INTRODUCTION.U_RECORD is
'增量更新标识';

comment on column IX_POI_INTRODUCTION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_NAME                                           */
/*==============================================================*/
create table IX_POI_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_IX_POI_N check (NAME_CLASS in (1,3,4,5,6,7,9)) disable ,
   NAME_TYPE            NUMBER(2)            default 1 not null
      constraint CKC_NAME_TYPE_IX_POI_N check (NAME_TYPE in (1,2)) disable ,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_IX_POI_N check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_NAME primary key (NAME_ID),
   constraint IXPOI_NAME foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on column IX_POI_NAME.NAME_ID is
'主键';

comment on column IX_POI_NAME.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_NAME.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column IX_POI_NAME.NAME_CLASS is
'[180U]';

comment on column IX_POI_NAME.NAME_TYPE is
'[180A]';

comment on column IX_POI_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column IX_POI_NAME.NAME_PHONETIC is
'[171U]';

comment on column IX_POI_NAME.KEYWORDS is
'记录POI 名称完整的拼音关键字划分内容,关键字之间用英文半角"/"分割,如"北京市政府"关键字划分为:"bei jing shi/zheng fu';

comment on column IX_POI_NAME.NIDB_PID is
'记录现有POI中已经出品的永久ID,不同语言类型PID不同';

comment on column IX_POI_NAME.U_RECORD is
'增量更新标识';

comment on column IX_POI_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_NAME_FLAG                                      */
/*==============================================================*/
create table IX_POI_NAME_FLAG 
(
   NAME_ID              NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_N check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_NAMEFLAG foreign key (NAME_ID)
         references IX_POI_NAME (NAME_ID) disable
);

comment on table IX_POI_NAME_FLAG is
'[180]';

comment on column IX_POI_NAME_FLAG.NAME_ID is
'外键,引用"IX_POI_NAME"';

comment on column IX_POI_NAME_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column IX_POI_NAME_FLAG.U_RECORD is
'增量更新标识';

comment on column IX_POI_NAME_FLAG.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_NAME_TONE                                      */
/*==============================================================*/
create table IX_POI_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_NAME_TONE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINAME_TONE foreign key (NAME_ID)
         references IX_POI_NAME (NAME_ID) disable
);

comment on table IX_POI_NAME_TONE is
'[170]';

comment on column IX_POI_NAME_TONE.NAME_ID is
'外键,引用"IX_POI_NAME"';

comment on column IX_POI_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column IX_POI_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column IX_POI_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column IX_POI_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column IX_POI_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column IX_POI_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column IX_POI_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_PHOTO                                          */
/*==============================================================*/
create table IX_POI_PHOTO 
(
   POI_PID              NUMBER(10)           not null,
   PHOTO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_PHOTO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_PHOTO foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_PHOTO is
'[170]';

comment on column IX_POI_PHOTO.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_PHOTO.PHOTO_ID is
'参考"AU_PHOTO"';

comment on column IX_POI_PHOTO.STATUS is
'记录是否确认';

comment on column IX_POI_PHOTO.U_RECORD is
'增量更新标识';

comment on column IX_POI_PHOTO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_RESTAURANT                                     */
/*==============================================================*/
create table IX_POI_RESTAURANT 
(
   RESTAURANT_ID        NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   FOOD_TYPE            VARCHAR2(32),
   CREDIT_CARD          VARCHAR2(100),
   AVG_COST             NUMBER(5)            default 0 not null,
   PARKING              NUMBER(2)            default 0 not null
      constraint CKC_PARKING_IX_POI_R check (PARKING in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_R check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_RESTAURANT primary key (RESTAURANT_ID)
);

comment on table IX_POI_RESTAURANT is
'[170]';

comment on column IX_POI_RESTAURANT.RESTAURANT_ID is
'主键';

comment on column IX_POI_RESTAURANT.POI_PID is
'参考"IX_POI"';

comment on column IX_POI_RESTAURANT.FOOD_TYPE is
'记录各种菜系类型代码,如鲁菜,川菜,日本料理,法国菜等,多个菜系之间以"|"分隔;空为未调查';

comment on column IX_POI_RESTAURANT.CREDIT_CARD is
'值域包括:
1 维士(visa)
2 万事达(mastercard)
3 大来(dinas)
4 日本国际信用卡(jcb)
5 美国运通(America
Express)
6 银联(unionpay)
多个类型时采用英文半角”|”分隔
如果为0 表示不支持信用卡
如果为空表示未调查';

comment on column IX_POI_RESTAURANT.AVG_COST is
'如果为0 表示未调查';

comment on column IX_POI_RESTAURANT.U_RECORD is
'增量更新标识';

comment on column IX_POI_RESTAURANT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_VIDEO                                          */
/*==============================================================*/
create table IX_POI_VIDEO 
(
   POI_PID              NUMBER(10)           not null,
   VIDEO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POI_V check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_VIDEO foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on table IX_POI_VIDEO is
'[170]';

comment on column IX_POI_VIDEO.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_POI_VIDEO.VIDEO_ID is
'参考"AU_VIDEO"';

comment on column IX_POI_VIDEO.STATUS is
'记录是否确认';

comment on column IX_POI_VIDEO.U_RECORD is
'增量更新标识';

comment on column IX_POI_VIDEO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';
