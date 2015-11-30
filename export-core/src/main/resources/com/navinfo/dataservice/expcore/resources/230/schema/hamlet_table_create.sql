/*==============================================================*/
/* Table: IX_HAMLET                                             */
/*==============================================================*/
create table IX_HAMLET 
(
   PID                  NUMBER(10)           not null,
   KIND_CODE            VARCHAR2(8)         
      constraint CKC_KIND_CODE_IX_HAMLE check (KIND_CODE is null or (KIND_CODE in ('260100','260200','260000'))) disable ,
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   SIDE                 VARCHAR2(1)          default '0' not null
      constraint CKC_SIDE_IX_HAMLE check (SIDE in ('0','1','2','3')) disable ,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_FLAG_IX_HAMLE check (ROAD_FLAG in (0,1,2,3)) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           default 0 not null,
   POI_NUM              VARCHAR2(20),
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_HAMLE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_HAMLET check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_HAMLET primary key (PID)
);

comment on table IX_HAMLET is
'地名索引表,记录可用于引导的村,乡镇代表点';

comment on column IX_HAMLET.PID is
'主键';

comment on column IX_HAMLET.GEOMETRY is
'存储以"度"为单位的经纬度坐标点
';

comment on column IX_HAMLET.LINK_PID is
'参考"RD_LINK"';

comment on column IX_HAMLET.SIDE is
'记录Hamlet位于引导道路Link上,左侧或右侧';

comment on column IX_HAMLET.NAME_GROUPID is
'[173sp2]参考"RD_NAME"';

comment on column IX_HAMLET.ROAD_FLAG is
'[170]';

comment on column IX_HAMLET.PMESH_ID is
'[171A]每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';

comment on column IX_HAMLET.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_HAMLET.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_HAMLET.POI_PID is
'[171A]参考"IX_POI"';

comment on column IX_HAMLET.POI_NUM is
'[173A]记录来自NIDB的POI编号';

comment on column IX_HAMLET.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_HAMLET.U_RECORD is
'增量更新标识';

comment on column IX_HAMLET.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_HAMLET_FLAG                                        */
/*==============================================================*/
create table IX_HAMLET_FLAG 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_HAMLE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXHAMLET_FLAG foreign key (PID)
         references IX_HAMLET (PID) disable
);

comment on table IX_HAMLET_FLAG is
'[170]';

comment on column IX_HAMLET_FLAG.PID is
'外键,引用"IX_HAMLET"';

comment on column IX_HAMLET_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column IX_HAMLET_FLAG.U_RECORD is
'增量更新标识';

comment on column IX_HAMLET_FLAG.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_HAMLET_NAME                                        */
/*==============================================================*/
create table IX_HAMLET_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_IX_HAMLE check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_IX_HAMLE check (NAME_CLASS in (1,2)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_HAMLET_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_HAMLET_NAME primary key (NAME_ID),
   constraint IXHAMLE_NAME foreign key (PID)
         references IX_HAMLET (PID) disable
);

comment on table IX_HAMLET_NAME is
'[170]';

comment on column IX_HAMLET_NAME.NAME_ID is
'[170]主键';

comment on column IX_HAMLET_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column IX_HAMLET_NAME.PID is
'外键,引用"IX_HAMLET"';

comment on column IX_HAMLET_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column IX_HAMLET_NAME.NAME_CLASS is
'[170]';

comment on column IX_HAMLET_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column IX_HAMLET_NAME.NIDB_PID is
'[173A]记录现有POI中已经出品的永久ID,不同语言类型PID不同';

comment on column IX_HAMLET_NAME.U_RECORD is
'增量更新标识';

comment on column IX_HAMLET_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_HAMLET_NAME_TONE                                   */
/*==============================================================*/
create table IX_HAMLET_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_HAMLET_NAME_T check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXHAMLETNAME_TONE foreign key (NAME_ID)
         references IX_HAMLET_NAME (NAME_ID) disable
);

comment on table IX_HAMLET_NAME_TONE is
'[170]';

comment on column IX_HAMLET_NAME_TONE.NAME_ID is
'外键,引用"IX_HAMLET_NAME"';

comment on column IX_HAMLET_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column IX_HAMLET_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column IX_HAMLET_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column IX_HAMLET_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column IX_HAMLET_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column IX_HAMLET_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column IX_HAMLET_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';