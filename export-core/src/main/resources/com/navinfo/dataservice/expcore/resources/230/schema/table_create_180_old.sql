

/*==============================================================*/
/* Table: ACH_GDB_INFO                                          */
/*==============================================================*/
create table ACH_GDB_INFO 
(
   ACH_GDB_ID           VARCHAR2(1000)       not null,
   CREATE_TIME          DATE,
   SYS_INFO             VARCHAR2(1000),
   CREATE_PERSON        NUMBER(10)           default 0,
   VER_NAME             VARCHAR2(1000),
   VER_NUM              VARCHAR2(1000)      
      constraint CKC_VER_NUM_ACH_GDB_ check (VER_NUM is null or (VER_NUM in ('0','1','2','3'))) disable ,
   SUB_VER_NUM          VARCHAR2(1000),
   PARENT_VER_NUM       VARCHAR2(1000),
   STATUS               VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   SUBMIT_PERSON        VARCHAR2(1000),
   TASK_RANGE           VARCHAR2(1000),
   TASK_DESCRIPT        VARCHAR2(1000),
   RECEIVE_PERSON       NUMBER(10)           default 0,
   RECEIVE_TIME         DATE,
   MEMO                 VARCHAR2(1000),
   constraint PK_ACH_GDB_INFO primary key (ACH_GDB_ID)
);

comment on table ACH_GDB_INFO is
'[173A]';

comment on column ACH_GDB_INFO.ACH_GDB_ID is
'主键';

comment on column ACH_GDB_INFO.SYS_INFO is
'数据库名,用户名,数据库IP';

comment on column ACH_GDB_INFO.CREATE_PERSON is
'参考"BI_PERSON"';

comment on column ACH_GDB_INFO.VER_NAME is
'如:12Q1';

comment on column ACH_GDB_INFO.VER_NUM is
'根据成果库版本名称,从1.0开始,依次加1.如:1.0,2.0 ';

comment on column ACH_GDB_INFO.SUB_VER_NUM is
'人工命名';

comment on column ACH_GDB_INFO.PARENT_VER_NUM is
'从作业库出来时,此项为空';

comment on column ACH_GDB_INFO.STATUS is
'封库,解库';

comment on column ACH_GDB_INFO.ITEM is
'用户设置:如13CY,NIMIF-G,NAVEX,NIGDF-G,其他';

comment on column ACH_GDB_INFO.SUBMIT_PERSON is
'参考"BI_PERSON"';

comment on column ACH_GDB_INFO.TASK_RANGE is
'参考"BI_TASK"';

comment on column ACH_GDB_INFO.TASK_DESCRIPT is
'参考"BI_TASK"';

comment on column ACH_GDB_INFO.RECEIVE_PERSON is
'参考"BI_PERSON"';

/*==============================================================*/
/* Table: ADAS_NODE                                             */
/*==============================================================*/
create table ADAS_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   RDNODE_PID           NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)         default -9999 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ADAS_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ADAS_NODE primary key (NODE_PID)
);

comment on table ADAS_NODE is
'[170]';

comment on column ADAS_NODE.NODE_PID is
'[173U]主键';

comment on column ADAS_NODE.RDNODE_PID is
'[173U]参考"RD_NODE"';

comment on column ADAS_NODE.GEOMETRY is
'存储以"度"为单位的7 位经纬度坐标';

comment on column ADAS_NODE.Z_VALUE is
'单位:米';

comment on column ADAS_NODE.MESH_ID is
'[171A]';

comment on column ADAS_NODE.U_RECORD is
'增量更新标识';

comment on column ADAS_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ADAS_LINK                                             */
/*==============================================================*/
create table ADAS_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   RDLINK_PID           NUMBER(10)           default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   DESCRIPTION          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ADAS_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ADAS_LINK primary key (LINK_PID),
   constraint ADASLINK_SNODE foreign key (S_NODE_PID)
         references ADAS_NODE (NODE_PID) disable ,
   constraint ADASLINK_ENODE foreign key (E_NODE_PID)
         references ADAS_NODE (NODE_PID) disable
);

comment on table ADAS_LINK is
'[170]';

comment on column ADAS_LINK.LINK_PID is
'[173U]主键';

comment on column ADAS_LINK.S_NODE_PID is
'[173U]外键,引用"ADAS_NODE"';

comment on column ADAS_LINK.E_NODE_PID is
'[173U]外键,引用"ADAS_NODE"';

comment on column ADAS_LINK.GEOMETRY is
'[171A](1)存储以"度"为单位的7 位经纬度坐标序列
(2)起点(S_ADAS_NODEID) 和终点(E_ADAS_NODEID)坐标作为形状点来存储';

comment on column ADAS_LINK.RDLINK_PID is
'[173U]参考"RD_LINK"';

comment on column ADAS_LINK.MESH_ID is
'[171A]';

comment on column ADAS_LINK.U_RECORD is
'增量更新标识';

comment on column ADAS_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ADAS_LINK_GEOMETRY                                    */
/*==============================================================*/
create table ADAS_LINK_GEOMETRY 
(
   LINK_PID             NUMBER(10)           not null,
   SHP_SEQ_NUM          NUMBER(5)            default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)         default -9999 not null,
   HEADING              NUMBER(10,3)         default 0 not null,
   CURVATURE            NUMBER(10,6)         default 0 not null,
   SLOPE                NUMBER(10,3)         default 0 not null,
   BANKING              NUMBER(10,3)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ADAS_LINK_GEO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASLINKGEOMETRY_ADASLINK foreign key (LINK_PID)
         references ADAS_LINK (LINK_PID) disable
);

comment on table ADAS_LINK_GEOMETRY is
'[170]';

comment on column ADAS_LINK_GEOMETRY.LINK_PID is
'外键,引用"ADAS_LINK"';

comment on column ADAS_LINK_GEOMETRY.GEOMETRY is
'(1)存储以"度"为单位的6位经纬度坐标和"厘米"为单位的高程值
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column ADAS_LINK_GEOMETRY.Z_VALUE is
'[171U]单位:米';

comment on column ADAS_LINK_GEOMETRY.HEADING is
'单位:度,值域:[0,360]';

comment on column ADAS_LINK_GEOMETRY.CURVATURE is
'单位:米,值域:[-1,1]';

comment on column ADAS_LINK_GEOMETRY.SLOPE is
'单位:度,值域:[-90,90]';

comment on column ADAS_LINK_GEOMETRY.BANKING is
'单位:度,值域:[-90,90]';

comment on column ADAS_LINK_GEOMETRY.U_RECORD is
'增量更新标识';

comment on column ADAS_LINK_GEOMETRY.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NODE                                               */
/*==============================================================*/
create table RD_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_RD_NODE check (KIND in (1,2,3)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   ADAS_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ADAS_FLAG_RD_NODE check (ADAS_FLAG in (0,1)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_RD_NODE check (EDIT_FLAG in (0,1)) disable ,
   DIF_GROUPID          VARCHAR2(200),
   SRC_FLAG             NUMBER(2)            default 6 not null
      constraint CKC_SRC_FLAG_RD_NODE check (SRC_FLAG in (1,2,3,4,5,6)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NODE primary key (NODE_PID)
);

comment on column RD_NODE.NODE_PID is
'主键';

comment on column RD_NODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点
';

comment on column RD_NODE.ADAS_FLAG is
'标志是否存在ADAS数据';

comment on column RD_NODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column RD_NODE.DIF_GROUPID is
'[172A]用于差分更新数据包的产品版本管理,如果存在多个,采用半角"|"分隔';

comment on column RD_NODE.SRC_FLAG is
'[180A]13CY';

comment on column RD_NODE.U_RECORD is
'增量更新标识';

comment on column RD_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK                                               */
/*==============================================================*/
create table RD_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 7 not null
      constraint CKC_KIND_RD_LINK check (KIND in (0,1,2,3,4,5,6,7,8,9,10,11,13,15)) disable ,
   DIRECT               NUMBER(1)            default 1 not null
      constraint CKC_DIRECT_RD_LINK check (DIRECT in (0,1,2,3)) disable ,
   APP_INFO             NUMBER(1)            default 1 not null
      constraint CKC_APP_INFO_RD_LINK check (APP_INFO in (0,1,2,3,5)) disable ,
   TOLL_INFO            NUMBER(1)            default 2 not null
      constraint CKC_TOLL_INFO_RD_LINK check (TOLL_INFO in (0,1,2,3)) disable ,
   ROUTE_ADOPT          NUMBER(1)            default 2 not null
      constraint CKC_ROUTE_ADOPT_RD_LINK check (ROUTE_ADOPT in (0,1,2,3,4,5,9)) disable ,
   MULTI_DIGITIZED      NUMBER(1)            default 0 not null
      constraint CKC_MULTI_DIGITIZED_RD_LINK check (MULTI_DIGITIZED between 0 and 1 and MULTI_DIGITIZED in (0,1)) disable ,
   DEVELOP_STATE        NUMBER(1)            default 0 not null
      constraint CKC_DEVELOP_STATE_RD_LINK check (DEVELOP_STATE between 0 and 2 and DEVELOP_STATE in (0,1,2)) disable ,
   IMI_CODE             NUMBER(1)            default 0 not null
      constraint CKC_IMI_CODE_RD_LINK check (IMI_CODE between 0 and 3 and IMI_CODE in (0,1,2,3)) disable ,
   SPECIAL_TRAFFIC      NUMBER(1)            default 0 not null
      constraint CKC_SPECIAL_TRAFFIC_RD_LINK check (SPECIAL_TRAFFIC between 0 and 1 and SPECIAL_TRAFFIC in (0,1)) disable ,
   FUNCTION_CLASS       NUMBER(1)            default 5 not null
      constraint CKC_FUNCTION_CLASS_RD_LINK check (FUNCTION_CLASS between 0 and 5 and FUNCTION_CLASS in (0,1,2,3,4,5)) disable ,
   URBAN                NUMBER(1)            default 0 not null
      constraint CKC_URBAN_RD_LINK check (URBAN between 0 and 1 and URBAN in (0,1)) disable ,
   PAVE_STATUS          NUMBER(1)            default 0 not null
      constraint CKC_PAVE_STATUS_RD_LINK check (PAVE_STATUS between 0 and 1 and PAVE_STATUS in (0,1)) disable ,
   LANE_NUM             NUMBER(2)            default 2 not null,
   LANE_LEFT            NUMBER(2)            default 0 not null,
   LANE_RIGHT           NUMBER(2)            default 0 not null,
   LANE_CLASS           NUMBER(1)            default 2 not null
      constraint CKC_LANE_CLASS_RD_LINK check (LANE_CLASS between 0 and 3 and LANE_CLASS in (0,1,2,3)) disable ,
   WIDTH                NUMBER(8)          default 0 not null,
   IS_VIADUCT           NUMBER(1)            default 0 not null
      constraint CKC_IS_VIADUCT_RD_LINK check (IS_VIADUCT between 0 and 2 and IS_VIADUCT in (0,1,2)) disable ,
   LEFT_REGION_ID       NUMBER(10)           default 0 not null,
   RIGHT_REGION_ID      NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   STREET_LIGHT         NUMBER(1)            default 0 not null
      constraint CKC_STREET_LIGHT_RD_LINK check (STREET_LIGHT between 0 and 2 and STREET_LIGHT in (0,1,2)) disable ,
   PARKING_LOT          NUMBER(1)            default 0 not null
      constraint CKC_PARKING_LOT_RD_LINK check (PARKING_LOT in (0,1,2)) disable ,
   ADAS_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ADAS_FLAG_RD_LINK check (ADAS_FLAG in (0,1)) disable ,
   SIDEWALK_FLAG        NUMBER(1)            default 0 not null
      constraint CKC_SIDEWALK_FLAG_RD_LINK check (SIDEWALK_FLAG in (0,1,2)) disable ,
   WALKSTAIR_FLAG       NUMBER(1)            default 0 not null
      constraint CKC_WALKSTAIR_FLAG_RD_LINK check (WALKSTAIR_FLAG in (0,1,2)) disable ,
   DICI_TYPE            NUMBER(1)            default 0 not null
      constraint CKC_DICI_TYPE_RD_LINKWALKING check (DICI_TYPE in (0,1,2)) disable ,
   WALK_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_WALK_FLAG_RD_LINK check (WALK_FLAG in (0,1,2)) disable ,
   DIF_GROUPID          VARCHAR2(200),
   SRC_FLAG             NUMBER(2)            default 6 not null
      constraint CKC_SRC_FLAG_RD_LINK check (SRC_FLAG in (1,2,3,4,5,6)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_RD_LINK check (EDIT_FLAG in (0,1)) disable ,
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LINK primary key (LINK_PID),
   constraint RDLINK_SNODE foreign key (S_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDLINK_ENODE foreign key (E_NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_LINK is
'道路Link是构成道路的基本元素,包括道路的几何形状,拓扑关系,以及基本的道路属性信息,如道路名称,道路种别,路径采纳,道路幅宽等.';

comment on column RD_LINK.LINK_PID is
'主键';

comment on column RD_LINK.S_NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_LINK.E_NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_LINK.KIND is
'[180U]存储为10进制数字,NaviMap显示为16进制形式
';

comment on column RD_LINK.DIRECT is
'描述车辆在道路上的通行方向,用相对于Link方向的顺和逆来表示';

comment on column RD_LINK.APP_INFO is
'[180U]描述道路通行信息';

comment on column RD_LINK.FUNCTION_CLASS is
'[180U]';

comment on column RD_LINK.URBAN is
'是否为城市道路';

comment on column RD_LINK.LANE_NUM is
'[180U](1)单方向道路:只记录"总车道数"
(2)双方向道路:
如果左右车道数一致, 只记录"总车道数"
如果左右车道数不一致,则分别记录"左/右车道数"';

comment on column RD_LINK.LANE_CLASS is
'[180U]';

comment on column RD_LINK.WIDTH is
'[171U]';

comment on column RD_LINK.LEFT_REGION_ID is
'[170]参考"AD_ADMIN",通过区域号码找对应的左行政代码和左乡镇代码';

comment on column RD_LINK.RIGHT_REGION_ID is
'[170]参考"AD_ADMIN",通过区域号码找对应的右行政代码和右乡镇代码';

comment on column RD_LINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column RD_LINK.LENGTH is
'单位:米';

comment on column RD_LINK.STREET_LIGHT is
'道路是否具有路灯之类的照明设施';

comment on column RD_LINK.PARKING_LOT is
'道路是否具有停车带或停车位';

comment on column RD_LINK.ADAS_FLAG is
'标志是否存在ADAS数据';

comment on column RD_LINK.SIDEWALK_FLAG is
'注:当标记值为2 时,在RD_LINK_SIDEWALK 中记录详细便道信息';

comment on column RD_LINK.WALKSTAIR_FLAG is
'注: 标记值为2 时,在RD_LINK_WALKSTAIR 中记录详细阶梯信息';

comment on column RD_LINK.DICI_TYPE is
'[180U]全要素或简化版';

comment on column RD_LINK.WALK_FLAG is
'[180U]允许或禁止行人通行';

comment on column RD_LINK.DIF_GROUPID is
'[172A]用于差分更新数据包的产品版本管理,如果存在多个,采用半角"|"分隔';

comment on column RD_LINK.SRC_FLAG is
'[180A]13CY';

comment on column RD_LINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column RD_LINK.MEMO is
'[171A]记录数据来源(参考影像更新或外包数据等)以及导入GDB 的时间或版本';

comment on column RD_LINK.U_RECORD is
'增量更新标识';

comment on column RD_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ADAS_LINK_LD                                          */
/*==============================================================*/
create table ADAS_LINK_LD 
(
   LINK_PID             NUMBER(10)           not null,
   SHP_SEQ_NUM          NUMBER(5)            default 0 not null,
   Z_VALUE              NUMBER(10,3)         default -9999 not null,
   HEADING              NUMBER(10,3)         default 0 not null,
   CURVATURE            NUMBER(10,6)         default 0 not null,
   SLOPE                NUMBER(10,3)         default 0 not null,
   BANKING              NUMBER(10,3)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ADAS_LINK_LD check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_ADASLINKLD foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table ADAS_LINK_LD is
'[170]';

comment on column ADAS_LINK_LD.LINK_PID is
'外键,引用"RD_LINK"';

comment on column ADAS_LINK_LD.Z_VALUE is
'[171U]单位:米';

comment on column ADAS_LINK_LD.HEADING is
'单位:度,值域:[0,360]';

comment on column ADAS_LINK_LD.CURVATURE is
'单位:米,值域:[-1,1]';

comment on column ADAS_LINK_LD.SLOPE is
'单位:度,值域:[-90,90]';

comment on column ADAS_LINK_LD.BANKING is
'单位:度,值域:[-90,90]';

comment on column ADAS_LINK_LD.U_RECORD is
'增量更新标识';

comment on column ADAS_LINK_LD.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ADAS_NODE_INFO                                        */
/*==============================================================*/
create table ADAS_NODE_INFO 
(
   NODE_PID             NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   HEADING              NUMBER(10,3)         default 0 not null,
   CURVATURE            NUMBER(10,6)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ADAS_NODE_INFO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASNODEINFO_INLINK foreign key (IN_LINK_PID)
         references ADAS_LINK (LINK_PID) disable ,
   constraint ADASNODEINFO_OUTLINK foreign key (OUT_LINK_PID)
         references ADAS_LINK (LINK_PID) disable ,
   constraint ADASNODEINFO_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID) disable
);

comment on table ADAS_NODE_INFO is
'[170]';

comment on column ADAS_NODE_INFO.NODE_PID is
'[171U][173U]外键,引用"ADAS_NODE"';

comment on column ADAS_NODE_INFO.IN_LINK_PID is
'[171U][173U]外键,引用"ADAS_LINK"';

comment on column ADAS_NODE_INFO.OUT_LINK_PID is
'[171U][173U]外键,引用"ADAS_LINK"';

comment on column ADAS_NODE_INFO.HEADING is
'单位:度,值域:[0,360]';

comment on column ADAS_NODE_INFO.CURVATURE is
'单位:米,值域:[-1,1]';

comment on column ADAS_NODE_INFO.U_RECORD is
'增量更新标识';

comment on column ADAS_NODE_INFO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ADAS_SLOPE                                            */
/*==============================================================*/
create table ADAS_SLOPE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SLOPE                NUMBER(10,3)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ADAS_SLO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASSLOPE_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID) disable ,
   constraint ADASSLOPE_LINK foreign key (LINK_PID)
         references ADAS_LINK (LINK_PID) disable
);

comment on table ADAS_SLOPE is
'[170]';

comment on column ADAS_SLOPE.PID is
'[173A]主键';

comment on column ADAS_SLOPE.NODE_PID is
'[173U]外键,引用"ADAS_NODE"';

comment on column ADAS_SLOPE.LINK_PID is
'[173U]外键,引用"ADAS_LINK"';

comment on column ADAS_SLOPE.SLOPE is
'单位:度, 值域:[-90,90]';

comment on column ADAS_SLOPE.U_RECORD is
'增量更新标识';

comment on column ADAS_SLOPE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_ADMIN                                              */
/*==============================================================*/
create table AD_ADMIN 
(
   REGION_ID            NUMBER(10)           not null,
   ADMIN_ID             NUMBER(6)            default 0 not null,
   EXTEND_ID            NUMBER(4)            default 0 not null,
   ADMIN_TYPE           VARCHAR2(4)          default '0' not null
      constraint CKC_ADMIN_TYPE_AD_ADMIN check (ADMIN_TYPE in ('0','1','2','2.5','3','3.5','4','4.5','4.8','5','6','7','8','9')) disable ,
   CAPITAL              NUMBER(1)            default 0 not null
      constraint CKC_CAPITAL_AD_ADMIN check (CAPITAL in (0,1,2,3)) disable ,
   POPULATION           VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   LINK_PID             NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_AD_ADMIN check (SIDE in (0,1,2,3)) disable ,
   ROAD_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_FLAG_AD_ADMIN check (ROAD_FLAG in (0,1,2,3)) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   JIS_CODE             NUMBER(5)            default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AD_ADMIN check (EDIT_FLAG in (0,1)) disable ,
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_ADMIN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN primary key (REGION_ID)
);

comment on column AD_ADMIN.REGION_ID is
'主键';

comment on column AD_ADMIN.ADMIN_TYPE is
'分为国家地区级,省级,地级市级,区/县级等类型';

comment on column AD_ADMIN.CAPITAL is
'注:TYPE=2 和2.5 记录为省会/直辖市,TYPE=0记录为首都,其他为未定义';

comment on column AD_ADMIN.POPULATION is
'单位:万人';

comment on column AD_ADMIN.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AD_ADMIN.LINK_PID is
'参考"RD_LINK"';

comment on column AD_ADMIN.NAME_GROUPID is
'参考"RD_NAME"';

comment on column AD_ADMIN.ROAD_FLAG is
'[170]';

comment on column AD_ADMIN.PMESH_ID is
'[171A]每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';

comment on column AD_ADMIN.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column AD_ADMIN.MEMO is
'[173A]';

comment on column AD_ADMIN.U_RECORD is
'增量更新标识';

comment on column AD_ADMIN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_ADMIN_GROUP                                        */
/*==============================================================*/
create table AD_ADMIN_GROUP 
(
   GROUP_ID             NUMBER(10)           not null,
   REGION_ID_UP         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_ADMIN_GROUP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN_GROUP primary key (GROUP_ID),
   constraint ADADMIN_UPLEVEL foreign key (REGION_ID_UP)
         references AD_ADMIN (REGION_ID) disable
);

comment on column AD_ADMIN_GROUP.GROUP_ID is
'主键';

comment on column AD_ADMIN_GROUP.REGION_ID_UP is
'外键,引用"AD_ADMIN"';

comment on column AD_ADMIN_GROUP.U_RECORD is
'增量更新标识';

comment on column AD_ADMIN_GROUP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_ADMIN_NAME                                         */
/*==============================================================*/
create table AD_ADMIN_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   REGION_ID            NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AD_ADMIN check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_AD_ADMIN check (NAME_CLASS in (1,2,3,4)) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AD_ADMIN check (SRC_FLAG in (0,1,2,3,4,5,6)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_ADMIN_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN_NAME primary key (NAME_ID),
   constraint ADADMIN_NAMES foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID) disable
);

comment on column AD_ADMIN_NAME.NAME_ID is
'[170]主键';

comment on column AD_ADMIN_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column AD_ADMIN_NAME.REGION_ID is
'外键,引用"AD_ADMIN"';

comment on column AD_ADMIN_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AD_ADMIN_NAME.NAME_CLASS is
'[170][172U]';

comment on column AD_ADMIN_NAME.NAME is
'[172A]';

comment on column AD_ADMIN_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column AD_ADMIN_NAME.SRC_FLAG is
'[170]现仅指英文名来源
注:
(1)AOIZone 取值0~6
(2)GCZone 取值:0
(3)其他取值:0~1';

comment on column AD_ADMIN_NAME.U_RECORD is
'增量更新标识';

comment on column AD_ADMIN_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_ADMIN_NAME_TONE                                    */
/*==============================================================*/
create table AD_ADMIN_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_ADMIN_NAME_T check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADADMINNAME_TONE foreign key (NAME_ID)
         references AD_ADMIN_NAME (NAME_ID) disable
);

comment on table AD_ADMIN_NAME_TONE is
'[170]';

comment on column AD_ADMIN_NAME_TONE.NAME_ID is
'外键,引用"AD_ADMIN_NAME"';

comment on column AD_ADMIN_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column AD_ADMIN_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column AD_ADMIN_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column AD_ADMIN_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column AD_ADMIN_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column AD_ADMIN_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column AD_ADMIN_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_ADMIN_PART                                         */
/*==============================================================*/
create table AD_ADMIN_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   REGION_ID_DOWN       NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_ADMIN_PART check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADADMIN_DOWNLEVEL foreign key (REGION_ID_DOWN)
         references AD_ADMIN (REGION_ID) disable ,
   constraint ADADMIN_UPDOWN foreign key (GROUP_ID)
         references AD_ADMIN_GROUP (GROUP_ID) disable
);

comment on column AD_ADMIN_PART.GROUP_ID is
'外键,引用"AD_ADMIN_GROUP"';

comment on column AD_ADMIN_PART.REGION_ID_DOWN is
'外键,引用"AD_ADMIN"';

comment on column AD_ADMIN_PART.U_RECORD is
'增量更新标识';

comment on column AD_ADMIN_PART.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_FACE                                               */
/*==============================================================*/
create table AD_FACE 
(
   FACE_PID             NUMBER(10)           not null,
   REGION_ID            NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AD_FACE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_FACE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_FACE primary key (FACE_PID),
   constraint ADADMIN_ADFACE foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID) disable
);

comment on column AD_FACE.FACE_PID is
'主键';

comment on column AD_FACE.REGION_ID is
'[170]外键,引用"AD_ADMIN"';

comment on column AD_FACE.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column AD_FACE.AREA is
'单位:平方米';

comment on column AD_FACE.PERIMETER is
'单位:米';

comment on column AD_FACE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column AD_FACE.U_RECORD is
'增量更新标识';

comment on column AD_FACE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_NODE                                               */
/*==============================================================*/
create table AD_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_AD_NODE check (KIND in (1)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_AD_NODE check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AD_NODE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_NODE primary key (NODE_PID)
);

comment on column AD_NODE.NODE_PID is
'主键';

comment on column AD_NODE.KIND is
'平面交叉点,行政区划边界点';

comment on column AD_NODE.FORM is
'图廓点,角点';

comment on column AD_NODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AD_NODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column AD_NODE.U_RECORD is
'增量更新标识';

comment on column AD_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_LINK                                               */
/*==============================================================*/
create table AD_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 1 not null
      constraint CKC_KIND_AD_LINK check (KIND in (0,1,2,3,4,5,6,7)) disable ,
   FORM                 NUMBER(1)            default 1 not null
      constraint CKC_FORM_AD_LINK check (FORM in (0,1,2,6,7,8,9)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_AD_LINK check (SCALE in (0,1,2)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AD_LINK check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_LINK primary key (LINK_PID),
   constraint ADLINK_SNODE foreign key (S_NODE_PID)
         references AD_NODE (NODE_PID) disable ,
   constraint ADLINK_ENODE foreign key (E_NODE_PID)
         references AD_NODE (NODE_PID) disable
);

comment on table AD_LINK is
'当行政区划边界(包括省/直辖市边界,市行政区界,区县边界,乡镇边界,村边界,国界)之间共用时,Link种别取高等级边界,如省界与市界共Link时,Link种别为"省界"';

comment on column AD_LINK.LINK_PID is
'主键';

comment on column AD_LINK.S_NODE_PID is
'外键,引用"AD_NODE"';

comment on column AD_LINK.E_NODE_PID is
'外键,引用"AD_NODE"';

comment on column AD_LINK.KIND is
'注:
(1)2.5 万数据:0~5
(2)百万数据:1,6,7';

comment on column AD_LINK.FORM is
'注:
(1)2.5 万数据:0~1
(2)百万数据:0~2,6~9';

comment on column AD_LINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column AD_LINK.LENGTH is
'单位:米';

comment on column AD_LINK.SCALE is
'注:该字段仅用于2.5 万数据,百万数据不需要';

comment on column AD_LINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column AD_LINK.U_RECORD is
'增量更新标识';

comment on column AD_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_FACE_TOPO                                          */
/*==============================================================*/
create table AD_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_FACE_TOPO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADFACE_LINK foreign key (LINK_PID)
         references AD_LINK (LINK_PID) disable ,
   constraint ADFACE_LINKS foreign key (FACE_PID)
         references AD_FACE (FACE_PID) disable
);

comment on table AD_FACE_TOPO is
'记录构成Face的Link序列表,按照逆时针方向存储';

comment on column AD_FACE_TOPO.FACE_PID is
'外键,引用"AD_FACE"';

comment on column AD_FACE_TOPO.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column AD_FACE_TOPO.LINK_PID is
'外键,引用"AD_LINK"';

comment on column AD_FACE_TOPO.U_RECORD is
'增量更新标识';

comment on column AD_FACE_TOPO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_NODE_100W                                          */
/*==============================================================*/
create table AD_NODE_100W 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_AD_NODE_ check (KIND in (1,2)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_AD_NODE_ check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AD_NODE_ check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_NODE_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_NODE_100W primary key (NODE_PID)
);

comment on column AD_NODE_100W.NODE_PID is
'主键';

comment on column AD_NODE_100W.KIND is
'平面交叉点,行政区划边界点';

comment on column AD_NODE_100W.FORM is
'图廓点,角点';

comment on column AD_NODE_100W.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AD_NODE_100W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column AD_NODE_100W.U_RECORD is
'增量更新标识';

comment on column AD_NODE_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_LINK_100W                                          */
/*==============================================================*/
create table AD_LINK_100W 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 1 not null
      constraint CKC_KIND_AD_LINK_ check (KIND in (0,1,2,3,4,5,6,7)) disable ,
   FORM                 NUMBER(1)            default 1 not null
      constraint CKC_FORM_AD_LINK_ check (FORM in (0,1,2,6,7,8,9)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_AD_LINK_ check (SCALE in (0,1,2)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AD_LINK_ check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_LINK_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_LINK_100W primary key (LINK_PID),
   constraint ADLINK_SNODE_100W foreign key (S_NODE_PID)
         references AD_NODE_100W (NODE_PID) disable ,
   constraint ADLINK_ENODE_100W foreign key (E_NODE_PID)
         references AD_NODE_100W (NODE_PID) disable
);

comment on table AD_LINK_100W is
'当行政区划边界(包括省/直辖市边界,市行政区界,区县边界,乡镇边界,村边界,国界)之间共用时,Link种别取高等级边界,如省界与市界共Link时,Link种别为"省界"';

comment on column AD_LINK_100W.LINK_PID is
'主键';

comment on column AD_LINK_100W.S_NODE_PID is
'外键,引用"AD_NODE_100W"';

comment on column AD_LINK_100W.E_NODE_PID is
'外键,引用"AD_NODE_100W"';

comment on column AD_LINK_100W.KIND is
'注:
(1)2.5 万数据:0~5
(2)百万数据:1,6,7';

comment on column AD_LINK_100W.FORM is
'注:
(1)2.5 万数据:0~1
(2)百万数据:0~2,6~9';

comment on column AD_LINK_100W.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column AD_LINK_100W.LENGTH is
'单位:米';

comment on column AD_LINK_100W.SCALE is
'注:该字段仅用于2.5 万数据,百万数据不需要';

comment on column AD_LINK_100W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column AD_LINK_100W.U_RECORD is
'增量更新标识';

comment on column AD_LINK_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_LINK_MESH                                          */
/*==============================================================*/
create table AD_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_LINK_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADLINK_MESH foreign key (LINK_PID)
         references AD_LINK (LINK_PID) disable
);

comment on column AD_LINK_MESH.LINK_PID is
'外键,引用"AD_LINK"';

comment on column AD_LINK_MESH.U_RECORD is
'增量更新标识';

comment on column AD_LINK_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_LINK_MESH_100W                                     */
/*==============================================================*/
create table AD_LINK_MESH_100W 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_LINK_MESH_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADLINK_MESH_100W foreign key (LINK_PID)
         references AD_LINK_100W (LINK_PID) disable
);

comment on column AD_LINK_MESH_100W.LINK_PID is
'外键,引用"AD_LINK_100W"';

comment on column AD_LINK_MESH_100W.U_RECORD is
'增量更新标识';

comment on column AD_LINK_MESH_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_NODE_MESH                                          */
/*==============================================================*/
create table AD_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_NODE_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADNODE_MESH foreign key (NODE_PID)
         references AD_NODE (NODE_PID) disable
);

comment on column AD_NODE_MESH.NODE_PID is
'外键,引用"AD_NODE"';

comment on column AD_NODE_MESH.U_RECORD is
'增量更新标识';

comment on column AD_NODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AD_NODE_MESH_100W                                     */
/*==============================================================*/
create table AD_NODE_MESH_100W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_AD_NODE_MESH_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADNODE_MESH_100W foreign key (NODE_PID)
         references AD_NODE_100W (NODE_PID) disable
);

comment on column AD_NODE_MESH_100W.NODE_PID is
'外键,引用"AD_NODE_100W"';

comment on column AD_NODE_MESH_100W.U_RECORD is
'增量更新标识';

comment on column AD_NODE_MESH_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: AU_ADAS_MARK                                          */
/*==============================================================*/
create table AU_ADAS_MARK 
(
   MARK_ID              NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   X_LEAD               NUMBER(8,5)          default 0 not null,
   Y_LEAD               NUMBER(8,5)          default 0 not null,
   ANGLE                NUMBER(8,5)          default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_AU_ADAS_ check (TYPE in (0)) disable ,
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   MARK_ITEM            NUMBER(5)            default 0 not null
      constraint CKC_MARK_ITEM_AU_ADAS_ check (MARK_ITEM in (0)) disable ,
   PARAM_L              NUMBER(10)           default 0 not null,
   PARAM_R              NUMBER(10)           default 0 not null,
   PARAM_S              VARCHAR2(1000),
   PARAM_EX             VARCHAR2(1000),
   STATUS               NUMBER(1)            default 0 not null
      constraint CKC_STATUS_AU_ADAS_ check (STATUS in (0,1,2,3,4,5,6,7,8)) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   DESCRIPT             VARCHAR2(200),
   REMARK               VARCHAR2(200),
   MEMO                 VARCHAR2(200),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_AU_ADAS_MARK primary key (MARK_ID)
);

comment on table AU_ADAS_MARK is
'[171A]';

comment on column AU_ADAS_MARK.MARK_ID is
'主键';

comment on column AU_ADAS_MARK.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_ADAS_MARK.ANGLE is
'标记与正北方向的夹角,0~360度';

comment on column AU_ADAS_MARK.TYPE is
'文字(默认值)参数化标记,草图,照片,音频,测线等';

comment on column AU_ADAS_MARK.LINK_PID is
'参考"RD_LINK"';

comment on column AU_ADAS_MARK.NODE_PID is
'参考"RD_NODE"';

comment on column AU_ADAS_MARK.DAY_TIME is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_ADAS_MARK.WORKER is
'参考"BI_PERSON"';

comment on column AU_ADAS_MARK.TASK_ID is
'记录内业的任务编号';

comment on column AU_ADAS_MARK.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_ADAS_MARK.FIELD_TASK_ID is
'记录外业的任务编号';

/*==============================================================*/
/* Table: AU_ADAS_GPSRECORD                                     */
/*==============================================================*/
create table AU_ADAS_GPSRECORD 
(
   GPSRECORD_ID         NUMBER(10)           not null,
   MARK_ID              NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(1)            default 0 not null
      constraint CKC_SOURCE_AU_ADAS_ check (SOURCE in (0,1)) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   MEMO                 VARCHAR2(255),
   constraint PK_AU_ADAS_GPSRECORD primary key (GPSRECORD_ID),
   constraint AUADAS_MARK_GPSRECORD foreign key (MARK_ID)
         references AU_ADAS_MARK (MARK_ID) disable
);

comment on table AU_ADAS_GPSRECORD is
'[171A]';

comment on column AU_ADAS_GPSRECORD.GPSRECORD_ID is
'主键';

comment on column AU_ADAS_GPSRECORD.MARK_ID is
'外键,引用"AU_MARK"';

comment on column AU_ADAS_GPSRECORD.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列';

/*==============================================================*/
/* Table: AU_ADAS_GPSTRACK                                      */
/*==============================================================*/
create table AU_ADAS_GPSTRACK 
(
   GPSTRACK_ID          NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   RECORD_TIME          DATE,
   GPS_TEXT             VARCHAR2(255),
   MESH_ID              NUMBER(6)            default 0 not null,
   WORKER               NUMBER(4)            default 0 not null,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_AU_ADAS_GPSTRACK primary key (GPSTRACK_ID)
);

comment on table AU_ADAS_GPSTRACK is
'[171A]';

comment on column AU_ADAS_GPSTRACK.GPSTRACK_ID is
'主键';

comment on column AU_ADAS_GPSTRACK.GPS_TEXT is
'GPS接收文本串';

comment on column AU_ADAS_GPSTRACK.WORKER is
'参考"BI_PERSON"';

comment on column AU_ADAS_GPSTRACK.TASK_ID is
'记录内业的任务编号';

comment on column AU_ADAS_GPSTRACK.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_ADAS_GPSTRACK.FIELD_TASK_ID is
'记录外业的任务编号';

/*==============================================================*/
/* Table: AU_AUDIO                                              */
/*==============================================================*/
create table AU_AUDIO 
(
   AUDIO_ID             NUMBER(10)           not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   "SIZE"               VARCHAR2(256),
   FORMAT               VARCHAR2(256),
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   IMP_WORKER           NUMBER(4)            default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   MESH_ID              NUMBER(6)            default 0 not null,
   constraint PK_AU_AUDIO primary key (AUDIO_ID)
);

comment on column AU_AUDIO.AUDIO_ID is
'主键';

comment on column AU_AUDIO.URL_DB is
'数据中心的文件存储路径名称';

comment on column AU_AUDIO.URL_FILE is
'音频文件存储的本地相对路径名,如\Data\Audio\';

comment on column AU_AUDIO.FILE_NAME is
'[170]文件名(含扩展名)';

comment on column AU_AUDIO."SIZE" is
'[170]';

comment on column AU_AUDIO.FORMAT is
'[170]WAV,ADP';

comment on column AU_AUDIO.DAY_TIME is
'[170]格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_AUDIO.WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_AUDIO.IMP_WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_AUDIO.IMP_VERSION is
'[170]';

comment on column AU_AUDIO.IMP_DATE is
'[170]格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_AUDIO.MESH_ID is
'[170]';

/*==============================================================*/
/* Table: AU_COMMUNICATION                                      */
/*==============================================================*/
create table AU_COMMUNICATION 
(
   ID                   NUMBER(10)           not null,
   TITLE                VARCHAR2(50),
   CONTENT              VARCHAR2(1000),
   GEOMETRY             SDO_GEOMETRY,
   RECEIVE_PERSON       NUMBER(10)           default 0 not null,
   CREAT_TIME           TIMESTAMP,
   CREAT_PERSON         NUMBER(10)           default 0 not null,
   constraint PK_AU_COMMUNICATION primary key (ID)
);

comment on column AU_COMMUNICATION.ID is
'主键';

comment on column AU_COMMUNICATION.TITLE is
'即主题';

comment on column AU_COMMUNICATION.GEOMETRY is
'用于WEBGIS交互';

/*==============================================================*/
/* Table: AU_MARK                                               */
/*==============================================================*/
create table AU_MARK 
(
   MARK_ID              NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   X_LEAD               NUMBER(8,5)          default 0 not null,
   Y_LEAD               NUMBER(8,5)          default 0 not null,
   ANGLE                NUMBER(8,5)          default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_AU_MARK check (TYPE in (0,1,2,3,4,5)) disable ,
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   IN_WORKER            NUMBER(4)            default 0 not null,
   MARK_ITEM            NUMBER(5)            default 0 not null
      constraint CKC_MARK_ITEM_AU_MARK check (MARK_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,30,31,32,33,34,35,36,37,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,61,62,63,70,71,72,73,74,101,102,103,104,105,106,107,108,109,199)) disable ,
   PARAM_L              NUMBER(10)           default 0 not null,
   PARAM_R              NUMBER(10)           default 0 not null,
   PARAM_S              VARCHAR2(1000),
   PARAM_EX             VARCHAR2(1000),
   STATUS               NUMBER(1)            default 0 not null
      constraint CKC_STATUS_AU_MARK check (STATUS in (0,1,2,3,4,5,6,7,8)) disable ,
   ADJA_FLAG            NUMBER(2)            default 0
      constraint CKC_ADJA_FLAG_AU_MARK check (ADJA_FLAG is null or (ADJA_FLAG in (0,1,2))) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   DESCRIPT             VARCHAR2(200),
   REMARK               VARCHAR2(200),
   MEMO                 VARCHAR2(200),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_AU_MARK primary key (MARK_ID)
);

comment on column AU_MARK.MARK_ID is
'主键';

comment on column AU_MARK.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_MARK.ANGLE is
'标记与正北方向的夹角,0~360度';

comment on column AU_MARK.TYPE is
'文字(默认值)参数化标记,草图,照片,音频,测线等';

comment on column AU_MARK.LINK_PID is
'参考"RD_LINK"';

comment on column AU_MARK.NODE_PID is
'参考"RD_NODE"';

comment on column AU_MARK.DAY_TIME is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_MARK.WORKER is
'参考"BI_PERSON"';

comment on column AU_MARK.IN_WORKER is
'[173sp1]参考"BI_PERSON"';

comment on column AU_MARK.PARAM_EX is
'[173sp1]';

comment on column AU_MARK.ADJA_FLAG is
'[180A]';

comment on column AU_MARK.TASK_ID is
'记录内业的任务编号';

comment on column AU_MARK.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_MARK.FIELD_TASK_ID is
'记录外业的任务编号';

/*==============================================================*/
/* Table: AU_DRAFT                                              */
/*==============================================================*/
create table AU_DRAFT 
(
   MARK_ID              NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   STYLE                NUMBER(1)            default 0 not null
      constraint CKC_STYLE_AU_DRAFT check (STYLE in (0,1)) disable ,
   COLOR                VARCHAR2(10)         not null,
   WIDTH                NUMBER(2)            default 1 not null,
   constraint AUDRAFT_MARK foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);

comment on column AU_DRAFT.MARK_ID is
'外键,引用"AU_MARK"';

comment on column AU_DRAFT.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列';

comment on column AU_DRAFT.STYLE is
'实线,虚线';

comment on column AU_DRAFT.COLOR is
'[173sp1]';

/*==============================================================*/
/* Table: AU_GPSRECORD                                          */
/*==============================================================*/
create table AU_GPSRECORD 
(
   GPSRECORD_ID         NUMBER(10)           not null,
   MARK_ID              NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(1)            default 0 not null
      constraint CKC_SOURCE_AU_GPSRE check (SOURCE in (0,1,2,3,4,5,6,9)) disable ,
   NAME                 VARCHAR2(255),
   TABLE_NAME           VARCHAR2(64),
   KIND                 NUMBER(5)            default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   MEMO                 VARCHAR2(255),
   constraint PK_AU_GPSRECORD primary key (GPSRECORD_ID),
   constraint AUGPSRECORD_MARK foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);

comment on column AU_GPSRECORD.GPSRECORD_ID is
'主键';

comment on column AU_GPSRECORD.MARK_ID is
'外键,引用"AU_MARK"';

comment on column AU_GPSRECORD.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列';

comment on column AU_GPSRECORD.NAME is
'按”名称ID,名称内容”方式存储,多个名称以英文半角"|"分割,如”100,学院路|202,成府路”';

comment on column AU_GPSRECORD.KIND is
'与TABLE_NAME表对应的种别代码相同,如TABLE_NAME为"LU_LINK_KIND",该字段取值与LU_LINK_KIND表中的"KIND"种别代码值相同';

/*==============================================================*/
/* Table: AU_GPSTRACK                                           */
/*==============================================================*/
create table AU_GPSTRACK 
(
   GPSTRACK_ID          NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   RECORD_TIME          DATE,
   MESH_ID              NUMBER(6)            default 0 not null,
   WORKER               NUMBER(4)            default 0 not null,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   "SIZE"               VARCHAR2(256),
   IMP_WORKER           NUMBER(4)            default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   constraint PK_AU_GPSTRACK primary key (GPSTRACK_ID)
);

comment on column AU_GPSTRACK.GPSTRACK_ID is
'主键';

comment on column AU_GPSTRACK.WORKER is
'参考"BI_PERSON"';

comment on column AU_GPSTRACK.TASK_ID is
'记录内业的任务编号';

comment on column AU_GPSTRACK.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_GPSTRACK.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column AU_GPSTRACK.URL_DB is
'[173sp1]数据中心的文件存储路径名称';

comment on column AU_GPSTRACK.URL_FILE is
'[173sp1]存储相对路径名,如\Data\SeriesPhoto\';

comment on column AU_GPSTRACK.FILE_NAME is
'[173sp1]文件名(含扩展名)';

comment on column AU_GPSTRACK."SIZE" is
'[173sp1]64*64,32*32,16*16';

comment on column AU_GPSTRACK.IMP_WORKER is
'[173sp1]参考"BI_PERSON"';

comment on column AU_GPSTRACK.IMP_VERSION is
'[173sp1]';

comment on column AU_GPSTRACK.IMP_DATE is
'[173sp1]格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_GPSTRACK_VIDEO                                     */
/*==============================================================*/
create table AU_GPSTRACK_VIDEO 
(
   GPSTRACK_ID          NUMBER(10)           not null,
   VIDEO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUGPSTRACK_VIDEO foreign key (GPSTRACK_ID)
         references AU_GPSTRACK (GPSTRACK_ID) disable
);

comment on table AU_GPSTRACK_VIDEO is
'[170]';

comment on column AU_GPSTRACK_VIDEO.GPSTRACK_ID is
'外键,引用"AU_GPSTRACK"';

comment on column AU_GPSTRACK_VIDEO.VIDEO_ID is
'参考"AU_VIDEO"';

comment on column AU_GPSTRACK_VIDEO.STATUS is
'记录是否确认';

/*==============================================================*/
/* Table: AU_IX_ANNOTATION                                      */
/*==============================================================*/
create table AU_IX_ANNOTATION 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   KIND_CODE            VARCHAR2(8)          not null,
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)           default 1 not null,
   SRC_FLAG             NUMBER(1)            default 0 not null
      constraint CKC_SRC_FLAG_AU_IX_ANNOTATION check (SRC_FLAG in (0,1,2,3,4,5)) disable ,
   SRC_PID              NUMBER(10)           default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)           default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AU_IX_ANNOTATION check (EDIT_FLAG in (0,1)) disable ,
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_AU_IX_ANNOTION check (DETAIL_FLAG in (0,1,2,3)) disable ,
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_GEO_OPRSTATUS_AU_IX_AN check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_GEO_CHECKSTATUS_AU_IX_AN check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_ANNOT check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_IX_ANNOT check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   IMP_DATE             DATE,
   constraint PK_AU_IX_ANNOTATION primary key (AUDATA_ID)
);

comment on table AU_IX_ANNOTATION is
'[171A]在导航设备上显示自然地形名,地名,道路名,建筑物名称等的数据';

comment on column AU_IX_ANNOTATION.AUDATA_ID is
'主键';

comment on column AU_IX_ANNOTATION.PID is
'参考"IX_ANNOTATION"';

comment on column AU_IX_ANNOTATION.KIND_CODE is
'参考"IX_ANNOTATION_CODE"';

comment on column AU_IX_ANNOTATION.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_IX_ANNOTATION.RANK is
'采用32bit 表示,从右到左依次为0~31bit,每bit 表示
一个显示等级(如下),赋值为0/1 分别表示无效/有效,
如:00000111 表示文字在1,2,4 万等级上均可显示
第0bit:1 万
第1bit:2 万
第2bit:4 万
第3bit:8 万
第4bit:16 万
第5bit:32 万
第6bit:64 万
第7bit:128 万
第8bit:256 万
第9bit:512 万
第10bit:1024 万
第11bit:2048 万
第12bit:4096 万
第13bit:8192 万
注:
(1)2.5 万数据:1~8 万
(2)20 万数据:16~32 万
(3)百万数据:64~512
(4)TOP 级数据:1024~8192 万';

comment on column AU_IX_ANNOTATION.SRC_FLAG is
'注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.SRC_PID is
'文字来源的数据ID,如来自POI则为PO的PID;来自道路名则为道路名ID
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.CLIENT_FLAG is
'根据不同客户需求,输出不同文字,值域包括:
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
(4)默认为空,表示所有客户都输出
(5)该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.SPECTIAL_FLAG is
'采用32bit 表示,从右到左依次为0~31bit,每bit 表示一个类型(如下),赋值为0/1 分别表示否/是
第0bit:3DICON
第1bit:在水中
所有bit 为均为0,表示无特殊标识
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.REGION_ID is
'参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column AU_IX_ANNOTATION.EDIT_FLAG is
'用于数据完整提取时,区分是否可编辑';

comment on column AU_IX_ANNOTATION.MODIFY_FLAG is
'记录修改方式如新增,改名称,改等级,改位移,删除等';

comment on column AU_IX_ANNOTATION.FIELD_MODIFY_FLAG is
'记录修改方式如新增,改名称,改等级,改位移,删除等';

comment on column AU_IX_ANNOTATION.EXTRACT_INFO is
'(1)存放"版本+从索引中提取"
(2)来自Address 字段
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.EXTRACT_PRIORITY is
'提取的优先级别(城区为A1~A11;县乡为B2~B5)
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.REMARK is
'转数据时,来自page字段,内容如:"显示坐标"和"引导坐标"';

comment on column AU_IX_ANNOTATION.DETAIL_FLAG is
'注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column AU_IX_ANNOTATION.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_IX_ANNOTATION.GEO_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_ANNOTATION.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_ANNOTATION.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

comment on column AU_IX_ANNOTATION.IMP_DATE is
'外业POI导入时,由DMS赋值,格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_IX_ANNOTATION_NAME                                 */
/*==============================================================*/
create table AU_IX_ANNOTATION_NAME 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_IX_ANNOTATION check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_AU_IX_ANN check (NAME_CLASS in (1,2)) disable ,
   OLD_NAME             VARCHAR2(200),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_AN check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_IX_AN check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_ANNOTATION_NAME foreign key (AUDATA_ID)
         references AU_IX_ANNOTATION (AUDATA_ID) disable
);

comment on table AU_IX_ANNOTATION_NAME is
'[171A]';

comment on column AU_IX_ANNOTATION_NAME.AUDATA_ID is
'外键,引用"AU_IX_ANNOTATION"';

comment on column AU_IX_ANNOTATION_NAME.NAME_ID is
'参考"IX_ANNOTATION_NAME"';

comment on column AU_IX_ANNOTATION_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_IX_ANNOTATION_NAME.PID is
'参考"IX_ANNOTATION"';

comment on column AU_IX_ANNOTATION_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_IX_ANNOTATION_NAME.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column AU_IX_ANNOTATION_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_ANNOTATION_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI                                             */
/*==============================================================*/
create table AU_IX_POI 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_AU_IX_PO check (SIDE in (0,1,2,3)) disable ,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_FLAG_AU_IX_PO check (ROAD_FLAG in (0,1,2,3)) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   ADMIN_REAL           NUMBER(6)            default 0 not null,
   IMPORTANCE           NUMBER(1)            default 0 not null
      constraint CKC_IMPORTANCE_AU_IX_PO check (IMPORTANCE in (0,1)) disable ,
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)            default 0 not null
      constraint CKC_ACCESS_FLAG_AU_IX_PO check (ACCESS_FLAG in (0,1,2)) disable ,
   OPEN_24H             NUMBER(1)            default 0 not null
      constraint CKC_OPEN_24H_AU_IX_PO check (OPEN_24H in (0,1,2)) disable ,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   POST_CODE            VARCHAR2(6),
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AU_IX_PO check (EDIT_FLAG in (0,1)) disable ,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AUIXPOI check (STATE in (0,1,2,3)) disable ,
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(32),
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_AU_IX_POI check (TYPE in (0,1)) disable ,
   ADDRESS_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_ADDRESS_FLAG_AU_IX_POI check (ADDRESS_FLAG in (0,1,9)) disable ,
   EX_PRIORITY          VARCHAR2(10),
   EDITION_FLAG         VARCHAR2(12),
   POI_MEMO             VARCHAR2(200),
   OLD_BLOCKCODE        VARCHAR2(200),
   OLD_NAME             VARCHAR2(200),
   OLD_ADDRESS          VARCHAR2(200),
   OLD_KIND             VARCHAR2(8),
   POI_NUM              VARCHAR2(20),
   LOG                  VARCHAR2(200),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_GEO_OPRSTATUS_AU_IX_POI check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_GEO_CHECKSTATUS_AU_IX_POI check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POI check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_IX_POI check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   IMP_DATE             DATE,
   constraint PK_AU_IX_POI primary key (AUDATA_ID)
);

comment on table AU_IX_POI is
'[171A]';

comment on column AU_IX_POI.AUDATA_ID is
'主键';

comment on column AU_IX_POI.PID is
'参考"IX_POI"';

comment on column AU_IX_POI.KIND_CODE is
'参考"IX_POI_CODE"';

comment on column AU_IX_POI.GEOMETRY is
'存储以"度"为单位的经纬度坐标点,用于POI显示和计算Link左右关系
';

comment on column AU_IX_POI.LINK_PID is
'参考"RD_LINK"';

comment on column AU_IX_POI.SIDE is
'记录POI位于引导道路Link上,左侧或右侧';

comment on column AU_IX_POI.NAME_GROUPID is
'[173sp2]参考"RD_NAME"';

comment on column AU_IX_POI.PMESH_ID is
'每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';

comment on column AU_IX_POI.IMPORTANCE is
'记录以下分类的POI为重要,即IMPORTANCE为1,否则为0
(1)拥有国际进出港口的机场
(2)国家旅游局评定的等级为3A,4A,5A的风景区
(3)世界文化遗产';

comment on column AU_IX_POI.CHAIN is
'主要制作对象是宾馆和加油站';

comment on column AU_IX_POI.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column AU_IX_POI.REGION_ID is
'参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column AU_IX_POI.EDIT_FLAG is
'用于数据完整提取时,区分是否可编辑';

comment on column AU_IX_POI.FIELD_STATE is
'改名称,改地址,改分类';

comment on column AU_IX_POI.LABEL is
'记录路,水,绿地,单项收费,双向收费,显示位置,24小时便利店';

comment on column AU_IX_POI.ADDRESS_FLAG is
'标志POI 地址(IX_POI_ADDRESS)完整性';

comment on column AU_IX_POI.EX_PRIORITY is
'提取的优先级别(城区为A1~A11;县乡为B2~B5)';

comment on column AU_IX_POI.EDITION_FLAG is
'记录数据是由内业还是外业修改,新增,删除等标志';

comment on column AU_IX_POI.OLD_BLOCKCODE is
'原结构中的"OLD大字"';

comment on column AU_IX_POI.POI_NUM is
'记录来自NIDB的POI编号';

comment on column AU_IX_POI.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_IX_POI.GEO_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

comment on column AU_IX_POI.IMP_DATE is
'外业POI导入时,由DMS赋值,格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_IX_POINTADDRESS                                    */
/*==============================================================*/
create table AU_IX_POINTADDRESS 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   GUIDE_LINK_PID       NUMBER(10)           default 0 not null,
   GUIDE_NAME_GROUPID   NUMBER(10)           default 0 not null,
   LOCATE_LINK_PID      NUMBER(10)           default 0 not null,
   LOCATE_NAME_GROUPID  NUMBER(10)           default 0 not null,
   GUIDE_LINK_SIDE      NUMBER(1)            default 0 not null
      constraint CKC_GUIDE_LINK_SIDE_AU_IX_PO check (GUIDE_LINK_SIDE in (0,1,2,3)) disable ,
   LOCATE_LINK_SIDE     NUMBER(1)            default 0 not null
      constraint CKC_LOCATE_LINK_SIDE_AU_IX_PO check (LOCATE_LINK_SIDE in (0,1,2,3)) disable ,
   REGION_ID            NUMBER(10)           default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AU_IX_POINT check (EDIT_FLAG in (0,1)) disable ,
   IDCODE               VARCHAR2(32),
   DPR_NAME             VARCHAR2(100),
   DP_NAME              VARCHAR2(35),
   OPERATOR             VARCHAR2(32),
   MEMOIRE              VARCHAR2(200),
   DPF_NAME             VARCHAR2(500),
   POSTER_ID            VARCHAR2(100),
   ADDRESS_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_ADDRESS_FLAG_AU_IX_PO check (ADDRESS_FLAG in (0,1,2)) disable ,
   VERIFED              VARCHAR2(1)          default 'F' not null
      constraint CKC_VERIFED_AU_IX_PO check (VERIFED in ('T','F')) disable ,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AUIXPADDR check (STATE in (0,1,2,3)) disable ,
   LOG                  VARCHAR2(32),
   MEMO                 VARCHAR2(32),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_GEO_OPRSTATUS_AU_IX_PO check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_GEO_CHECKSTATUS_AU_IX_PO check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POINT4 check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_IX_POINT check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   IMP_DATE             DATE,
   constraint PK_AU_IX_POINTADDRESS primary key (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS is
'[171A]门牌号码是由地名主管部门按照一定规则编制,用来定位建筑物所在位置的标牌,包括门牌(附号牌),楼(栋)牌,单元牌,户号牌等.';

comment on column AU_IX_POINTADDRESS.AUDATA_ID is
'主键';

comment on column AU_IX_POINTADDRESS.PID is
'参考"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_IX_POINTADDRESS.GUIDE_LINK_PID is
'参考"RD_LINK"';

comment on column AU_IX_POINTADDRESS.GUIDE_NAME_GROUPID is
'参考"RD_NAME"';

comment on column AU_IX_POINTADDRESS.LOCATE_LINK_PID is
'参考"RD_LINK"';

comment on column AU_IX_POINTADDRESS.LOCATE_NAME_GROUPID is
'参考"RD_NAME"';

comment on column AU_IX_POINTADDRESS.REGION_ID is
'参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column AU_IX_POINTADDRESS.EDIT_FLAG is
'用于数据完整提取时,区分是否可编辑';

comment on column AU_IX_POINTADDRESS.DPR_NAME is
'外业采集道路名';

comment on column AU_IX_POINTADDRESS.DP_NAME is
'外业采集门牌号';

comment on column AU_IX_POINTADDRESS.OPERATOR is
'外业的OPERATOR字段中的内容原样转入';

comment on column AU_IX_POINTADDRESS.MEMOIRE is
'标注信息(导入外业LABEL)';

comment on column AU_IX_POINTADDRESS.POSTER_ID is
'邮递员编号';

comment on column AU_IX_POINTADDRESS.ADDRESS_FLAG is
'点门牌的地址确认标识';

comment on column AU_IX_POINTADDRESS.STATE is
'[173sp2]';

comment on column AU_IX_POINTADDRESS.LOG is
'运行拆分程序后产生的字段';

comment on column AU_IX_POINTADDRESS.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_IX_POINTADDRESS.GEO_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POINTADDRESS.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POINTADDRESS.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

comment on column AU_IX_POINTADDRESS.IMP_DATE is
'外业POI导入时,由DMS赋值,格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_PARENT                             */
/*==============================================================*/
create table AU_IX_POINTADDRESS_PARENT 
(
   AUDATA_ID            NUMBER(10)           not null,
   GROUP_ID             NUMBER(10)           default 0 not null,
   PARENT_PA_PID        NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POINTP check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_IX_POINTP check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_IX_POINTADDRESS_PARENT primary key (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS_PARENT is
'[171A]';

comment on column AU_IX_POINTADDRESS_PARENT.AUDATA_ID is
'[173sp1]主键';

comment on column AU_IX_POINTADDRESS_PARENT.GROUP_ID is
'参考"IX_POINTADDRESS_PARENT" ';

comment on column AU_IX_POINTADDRESS_PARENT.PARENT_PA_PID is
'参考"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS_PARENT.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POINTADDRESS_PARENT.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_CHILDREN                           */
/*==============================================================*/
create table AU_IX_POINTADDRESS_CHILDREN 
(
   AUDATA_ID            NUMBER(10),
   GROUP_ID             NUMBER(10)           default 0 not null,
   CHILD_PA_PID         NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POINTC check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_IX_POINTC check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POINTADDRESS_CHILDREN foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS_PARENT (AUDATA_ID) disable
);

comment on table AU_IX_POINTADDRESS_CHILDREN is
'[171A]';

comment on column AU_IX_POINTADDRESS_CHILDREN.AUDATA_ID is
'[173sp1]外键,引用"AU_IX_POINTADDRESS_PARENT"';

comment on column AU_IX_POINTADDRESS_CHILDREN.GROUP_ID is
'参考"IX_POINTADDRESS_PARENT" ';

comment on column AU_IX_POINTADDRESS_CHILDREN.CHILD_PA_PID is
'参考"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS_CHILDREN.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POINTADDRESS_CHILDREN.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_NAME                               */
/*==============================================================*/
create table AU_IX_POINTADDRESS_NAME 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_IX_PO check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')) disable ,
   SUM_CHAR             NUMBER(1)            default 0 not null
      constraint CKC_SUM_CHAR_AU_IX_PO check (SUM_CHAR in (0,1,2,3)) disable ,
   SPLIT_FLAG           VARCHAR2(1000),
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
   STREET               VARCHAR2(100),
   PLACE                VARCHAR2(100),
   LANDMARK             VARCHAR2(100),
   PREFIX               VARCHAR2(64),
   HOUSENUM             VARCHAR2(64),
   TYPE                 VARCHAR2(32),
   SUBNUM               VARCHAR2(64),
   SURFIX               VARCHAR2(64),
   ESTAB                VARCHAR2(64),
   BUILDING             VARCHAR2(100),
   UNIT                 VARCHAR2(64),
   FLOOR                VARCHAR2(64),
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
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POINTN check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_IX_POINTN check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POINTADDRESS_NAME foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID) disable
);

comment on table AU_IX_POINTADDRESS_NAME is
'[171A]';

comment on column AU_IX_POINTADDRESS_NAME.AUDATA_ID is
'外键,引用"AU_IX_POINTADDRESS"';

comment on column AU_IX_POINTADDRESS_NAME.NAME_ID is
'参考"IX_POINTADDRESS_NAME" ';

comment on column AU_IX_POINTADDRESS_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_IX_POINTADDRESS_NAME.PID is
'参考"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_IX_POINTADDRESS_NAME.SUM_CHAR is
'点门牌的号码特征,如连续,奇偶,混合';

comment on column AU_IX_POINTADDRESS_NAME.SPLIT_FLAG is
'[173sp2]';

comment on column AU_IX_POINTADDRESS_NAME.FULLNAME is
'记录拆分前的全地址名称';

comment on column AU_IX_POINTADDRESS_NAME.ROADNAME is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.ROADNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.ADDRNAME is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.ADDRNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.PROVINCE is
'标牌中"省名/直辖市/自治区/特别行政区名"';

comment on column AU_IX_POINTADDRESS_NAME.CITY is
'标牌中"地级市名/自治洲名"';

comment on column AU_IX_POINTADDRESS_NAME.COUNTY is
'标牌中"县级市名/县名/区名(含直辖市的区)"';

comment on column AU_IX_POINTADDRESS_NAME.TOWN is
'乡镇街道办名称';

comment on column AU_IX_POINTADDRESS_NAME.STREET is
'街道,道路名, 胡同,巷,条,弄';

comment on column AU_IX_POINTADDRESS_NAME.PLACE is
'自然村落,居民小区,区域地名,开发区名';

comment on column AU_IX_POINTADDRESS_NAME.LANDMARK is
'指有地理表示作用的店铺,公共设施,单位,建筑或交通运输设施,包括桥梁,公路环岛,交通站场等';

comment on column AU_IX_POINTADDRESS_NAME.PREFIX is
'用于修饰门牌号号码的成分';

comment on column AU_IX_POINTADDRESS_NAME.HOUSENUM is
'主门牌号号码,以序号方式命名的弄或条';

comment on column AU_IX_POINTADDRESS_NAME.TYPE is
'门牌号号码类型';

comment on column AU_IX_POINTADDRESS_NAME.SUBNUM is
'主门牌号所属的子门牌号及修饰该子门牌的前缀信息';

comment on column AU_IX_POINTADDRESS_NAME.SURFIX is
'用于修饰门牌地址的词语,其本身没有实际意义,不影响门牌地址的含义,如:自编,临时';

comment on column AU_IX_POINTADDRESS_NAME.ESTAB is
'如"**大厦","**小区"';

comment on column AU_IX_POINTADDRESS_NAME.BUILDING is
'如"A栋,12栋,31楼,B座"等';

comment on column AU_IX_POINTADDRESS_NAME.UNIT is
'如"2门"';

comment on column AU_IX_POINTADDRESS_NAME.FLOOR is
'如"12层"';

comment on column AU_IX_POINTADDRESS_NAME.ROOM is
'如"503室"';

comment on column AU_IX_POINTADDRESS_NAME.ADDONS is
'如"对面,旁边,附近"';

comment on column AU_IX_POINTADDRESS_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POINTADDRESS_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_ADDRESS                                     */
/*==============================================================*/
create table AU_IX_POI_ADDRESS 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_IX_POI check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AU_IX_PO check (SRC_FLAG in (0,1)) disable ,
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
   STREET               VARCHAR2(100),
   PLACE                VARCHAR2(100),
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
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POIA check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_IX_POIA check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_ADDRESS foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_ADDRESS is
'[171A]';

comment on column AU_IX_POI_ADDRESS.AUDATA_ID is
'外键,引用"AU_IX_POI"';

comment on column AU_IX_POI_ADDRESS.NAME_ID is
'参考"IX_POI_ADDRESS"';

comment on column AU_IX_POI_ADDRESS.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_IX_POI_ADDRESS.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_ADDRESS.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_IX_POI_ADDRESS.SRC_FLAG is
'现仅指英文名来源';

comment on column AU_IX_POI_ADDRESS.ROADNAME is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.ROADNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.ADDRNAME is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.ADDRNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.PROVINCE is
'POI标牌中的"省名/直辖市/自治区/特别行政区名"';

comment on column AU_IX_POI_ADDRESS.CITY is
'POI标牌中的"地级市名/自治洲名"';

comment on column AU_IX_POI_ADDRESS.COUNTY is
'POI标牌中的"县级市名/县名/区名(含直辖市的区)"';

comment on column AU_IX_POI_ADDRESS.TOWN is
'乡镇街道办名称';

comment on column AU_IX_POI_ADDRESS.STREET is
'街道,道路名, 胡同,巷,条,弄';

comment on column AU_IX_POI_ADDRESS.PLACE is
'自然村落,居民小区,区域地名,开发区名';

comment on column AU_IX_POI_ADDRESS.LANDMARK is
'指有地理表示作用的店铺,公共设施,单位,建筑或交通运输设施,包括桥梁,公路环岛,交通站场等';

comment on column AU_IX_POI_ADDRESS.PREFIX is
'用于修饰门牌号号码的成分';

comment on column AU_IX_POI_ADDRESS.HOUSENUM is
'主门牌号号码,以序号方式命名的弄或条';

comment on column AU_IX_POI_ADDRESS.TYPE is
'门牌号号码类型';

comment on column AU_IX_POI_ADDRESS.SUBNUM is
'主门牌号所属的子门牌号及修饰该子门牌的前缀信息';

comment on column AU_IX_POI_ADDRESS.SURFIX is
'用于修饰门牌地址的词语,其本身没有实际意义,不影响门牌地址的含义,如:自编,临时';

comment on column AU_IX_POI_ADDRESS.ESTAB is
'如"**大厦","**小区"';

comment on column AU_IX_POI_ADDRESS.BUILDING is
'如"A栋,12栋,31楼,B座"等';

comment on column AU_IX_POI_ADDRESS.FLOOR is
'如"12层"';

comment on column AU_IX_POI_ADDRESS.UNIT is
'如"2门"';

comment on column AU_IX_POI_ADDRESS.ROOM is
'如"503室"';

comment on column AU_IX_POI_ADDRESS.ADDONS is
'如"对面,旁边,附近"';

comment on column AU_IX_POI_ADDRESS.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_ADDRESS.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_PARENT                                      */
/*==============================================================*/
create table AU_IX_POI_PARENT 
(
   AUDATA_ID            NUMBER(10)           not null,
   GROUP_ID             NUMBER(10)           default 0 not null,
   PARENT_POI_PID       NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POIP check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_IX_POIP check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_IX_POI_PARENT primary key (AUDATA_ID)
);

comment on table AU_IX_POI_PARENT is
'[171A]';

comment on column AU_IX_POI_PARENT.AUDATA_ID is
'[173sp1]主键';

comment on column AU_IX_POI_PARENT.GROUP_ID is
'参考"IX_POI_PARENT"';

comment on column AU_IX_POI_PARENT.PARENT_POI_PID is
'参考"IX_POI" ';

comment on column AU_IX_POI_PARENT.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_PARENT.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_CHILDREN                                    */
/*==============================================================*/
create table AU_IX_POI_CHILDREN 
(
   AUDATA_ID            NUMBER(10)           not null,
   GROUP_ID             NUMBER(10)           default 0 not null,
   CHILD_POI_PID        NUMBER(10)           default 0 not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
      constraint CKC_RELATION_TYPE_AU_IX_PO check (RELATION_TYPE in (0,1,2)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_PO check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_IX_PO check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_CHILDREN foreign key (AUDATA_ID)
         references AU_IX_POI_PARENT (AUDATA_ID) disable
);

comment on table AU_IX_POI_CHILDREN is
'[171A]';

comment on column AU_IX_POI_CHILDREN.AUDATA_ID is
'[173sp1]外键,引用"AU_IX_POI_PARENT"';

comment on column AU_IX_POI_CHILDREN.GROUP_ID is
'参考"IX_POI_PARENT"';

comment on column AU_IX_POI_CHILDREN.CHILD_POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_CHILDREN.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_CHILDREN.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_CONTACT                                     */
/*==============================================================*/
create table AU_IX_POI_CONTACT 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   CONTACT_TYPE         NUMBER(2)            default 1 not null
      constraint CKC_CONTACT_TYPE_AU_IX_PO check (CONTACT_TYPE in (1,2,3,4,11,21,22)) disable ,
   CONTACT              VARCHAR2(128),
   CONTACT_DEPART       NUMBER(3)            default 0 not null,
   PRIORITY             NUMBER(1)            default 0 not null
      constraint CKC_PRIORITY_AU_IX_PO check (PRIORITY in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POIC check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_IX_POIC check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_CONTACT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_CONTACT is
'[171A]当存在多个联系方式时,存储为多条记录';

comment on column AU_IX_POI_CONTACT.AUDATA_ID is
'外键,引用"AU_IX_POI"';

comment on column AU_IX_POI_CONTACT.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_CONTACT.CONTACT is
'记录包括区号在内的电话号码,存储为英文半角数字字符,二者之间以半角"-"分隔,如010-82306399';

comment on column AU_IX_POI_CONTACT.CONTACT_DEPART is
'采用8bit 表示,从右到左依次为0~7bit,每bit 表示一个服务部门(如下),赋值为0/1 分别表示否/是,如:00000011 表示总机和客服;00000101 表示总机和预订
第0bit:总机
第1bit:客服
第2bit:预订
第3bit:销售
第4bit:维修
第5bit:其他
如果所有bit 位均为0,表示未调查';

comment on column AU_IX_POI_CONTACT.PRIORITY is
'是否优先选择的联系方式';

comment on column AU_IX_POI_CONTACT.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_CONTACT.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_FLAG                                        */
/*==============================================================*/
create table AU_IX_POI_FLAG 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   FLAG_CODE            VARCHAR2(12),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_IX_POIN1 check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHETUS_AU_IX_POIN5 check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POIFLAG foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_FLAG is
'[180]';

comment on column AU_IX_POI_FLAG.AUDATA_ID is
'外键';

comment on column AU_IX_POI_FLAG.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column AU_IX_POI_FLAG.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_FLAG.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_HOTEL                                       */
/*==============================================================*/
create table AU_IX_POI_HOTEL 
(
   AUDATA_ID            NUMBER(10)           not null,
   HOTEL_ID             NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   CREDIT_CARD          VARCHAR2(100),
   RATING               NUMBER(2)            default 0 not null
      constraint CKC_RATING_AU_IX_PO check (RATING in (0,1,2,3,4,5,6,12,13,14,15)) disable ,
   CHECKIN_TIME         VARCHAR2(100)        default '14:00' not null,
   CHECKOUT_TIME        VARCHAR2(100)        default '12:00' not null,
   ROOM_COUNT           NUMBER(5)            default 0 not null,
   ROOM_TYPE            VARCHAR2(100),
   ROOM_PRICE           VARCHAR2(100),
   BREAKFAST            NUMBER(2)            default 0 not null
      constraint CKC_BREAKFAST_AU_IX_PO check (BREAKFAST in (0,1)) disable ,
   SERVICE              VARCHAR2(1000),
   PARKING              NUMBER(2)            default 0 not null
      constraint CKC_PARKING_AU_IX_PO check (PARKING in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_IX_POIHOTEL check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKST check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_HOTEL foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_HOTEL is
'[173sp1]';

comment on column AU_IX_POI_HOTEL.AUDATA_ID is
'外键,引用"AU_IX_POI"';

comment on column AU_IX_POI_HOTEL.HOTEL_ID is
'外键,参考"IX_POI_HOTEL"';

comment on column AU_IX_POI_HOTEL.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_HOTEL.CREDIT_CARD is
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

comment on column AU_IX_POI_HOTEL.CHECKIN_TIME is
'格式:HH:mm';

comment on column AU_IX_POI_HOTEL.CHECKOUT_TIME is
'格式:HH:mm';

comment on column AU_IX_POI_HOTEL.ROOM_COUNT is
'大于等于0 的整数,0 表示未调查';

comment on column AU_IX_POI_HOTEL.ROOM_TYPE is
'值域包括:
1 单人间(single)
2 标准间(double)
3 套房(suite)
多个类型时采用英文半角"|"分隔
如果为空表示未调查';

comment on column AU_IX_POI_HOTEL.ROOM_PRICE is
'多个价格时采用英文半角"|"分隔,顺序必须与客房类型一致
如果为空表示未调查';

comment on column AU_IX_POI_HOTEL.SERVICE is
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

comment on column AU_IX_POI_HOTEL.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_HOTEL.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_NAME                                        */
/*==============================================================*/
create table AU_IX_POI_NAME 
(
   AUNAME_ID            NUMBER(10)           not null,
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           default 0 not null,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_AU_IX_PO check (NAME_CLASS in (1,3,4,5,6,7,9)) disable ,
   NAME_TYPE            NUMBER(2)            default 1 not null
      constraint CKC_NAME_TYPE_AU_IX_PO check (NAME_TYPE in (1,2)) disable ,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_IX_POINAME check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OS_AU_IX_POIN2 check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CS_AU_IX_POIN6 check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_IX_POI_NAME primary key (AUNAME_ID),
   constraint AUIX_POI_NAME foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_NAME is
'[171A]';

comment on column AU_IX_POI_NAME.AUNAME_ID is
'[180A]主键';

comment on column AU_IX_POI_NAME.AUDATA_ID is
'外键,引用"AU_IX_POI"';

comment on column AU_IX_POI_NAME.NAME_ID is
'参考"IX_POI_NAME"';

comment on column AU_IX_POI_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_IX_POI_NAME.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_NAME.NAME_CLASS is
'[180U]';

comment on column AU_IX_POI_NAME.NAME_TYPE is
'[180A]';

comment on column AU_IX_POI_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_IX_POI_NAME.KEYWORDS is
'记录POI 名称完整的拼音关键字划分内容,关键字之间用英文半角"/"分割,如"北京市政府"关键字划分为:"bei jing shi/zheng fu';

comment on column AU_IX_POI_NAME.NIDB_PID is
'记录现有POI中已经出品的永久ID,不同语言类型PID不同';

comment on column AU_IX_POI_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_NAME_FLAG                                   */
/*==============================================================*/
create table AU_IX_POI_NAME_FLAG 
(
   AUNAME_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   FLAG_CODE            VARCHAR2(12),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPUS_AU_IX_POIN3 check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHEATUS_AU_IX_POIN7 check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POINAMEFLAG foreign key (AUNAME_ID)
         references AU_IX_POI_NAME (AUNAME_ID) disable
);

comment on table AU_IX_POI_NAME_FLAG is
'[180]';

comment on column AU_IX_POI_NAME_FLAG.AUNAME_ID is
'外键,引用"AU_IX_POI_NAME"';

comment on column AU_IX_POI_NAME_FLAG.NAME_ID is
'参考"IX_POI_NAME"';

comment on column AU_IX_POI_NAME_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column AU_IX_POI_NAME_FLAG.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_NAME_FLAG.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_NOKIA                                       */
/*==============================================================*/
create table AU_IX_POI_NOKIA 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_NUM              VARCHAR2(100),
   KIND_CODE            VARCHAR2(8),
   NAME                 VARCHAR2(1000),
   GEOMETRY             SDO_GEOMETRY,
   TELEPHONE            VARCHAR2(1000),
   ADDRESS              VARCHAR2(1000)      
      constraint CKC_PARKING_AU_IX_PO2 check (ADDRESS is null or (ADDRESS in ('0','1','2','3'))) disable ,
   STATE                NUMBER(2)            default 0 not null
      constraint CKC_STATE_AU_IX_PO check (STATE in (0,1,2)) disable ,
   constraint PK_AU_IX_POI_NOKIA primary key (AUDATA_ID)
);

comment on table AU_IX_POI_NOKIA is
'[173sp2]';

comment on column AU_IX_POI_NOKIA.AUDATA_ID is
'主键';

/*==============================================================*/
/* Table: AU_IX_POI_PHOTO                                       */
/*==============================================================*/
create table AU_IX_POI_PHOTO 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   PHOTO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_AUIX_ATT_OP check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_AUPOIATT_CK check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AU_IX_POI_PHOTO foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_PHOTO is
'[173sp2]';

comment on column AU_IX_POI_PHOTO.AUDATA_ID is
'外键,引用"AU_IX_POI"';

comment on column AU_IX_POI_PHOTO.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_PHOTO.PHOTO_ID is
'参考"AU_PHOTO"';

comment on column AU_IX_POI_PHOTO.STATUS is
'记录是否确认';

comment on column AU_IX_POI_PHOTO.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_PHOTO.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_IX_POI_RESTAURANT                                  */
/*==============================================================*/
create table AU_IX_POI_RESTAURANT 
(
   AUDATA_ID            NUMBER(10)           not null,
   RESTAURANT_ID        NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           default 0 not null,
   FOOD_TYPE            VARCHAR2(32),
   CREDIT_CARD          VARCHAR2(100),
   AVG_COST             NUMBER(5)            default 0 not null,
   PARKING              NUMBER(2)            default 0 not null
      constraint CKC_PARK_AUIXPOI check (PARKING in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_OPER_ATTOP check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_AUPOI_ATTCH check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_RESTAURANT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

comment on table AU_IX_POI_RESTAURANT is
'[171A]';

comment on column AU_IX_POI_RESTAURANT.AUDATA_ID is
'外键,引用"AU_IX_POI"';

comment on column AU_IX_POI_RESTAURANT.RESTAURANT_ID is
'参考"IX_POI_RESTAURANT"';

comment on column AU_IX_POI_RESTAURANT.POI_PID is
'参考"IX_POI"';

comment on column AU_IX_POI_RESTAURANT.FOOD_TYPE is
'NM编辑,记录各种菜系类型代码,如鲁菜,川菜,日本料理,法国菜等,多个菜系之间以"|"分隔;空为未调查';

comment on column AU_IX_POI_RESTAURANT.CREDIT_CARD is
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

comment on column AU_IX_POI_RESTAURANT.AVG_COST is
'如果为0 表示未调查';

comment on column AU_IX_POI_RESTAURANT.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_IX_POI_RESTAURANT.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_MARK_AUDIO                                         */
/*==============================================================*/
create table AU_MARK_AUDIO 
(
   MARK_ID              NUMBER(10)           not null,
   AUDIO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUMARK_AUDIO foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);

comment on table AU_MARK_AUDIO is
'[170]';

comment on column AU_MARK_AUDIO.MARK_ID is
'外键,引用"AU_MARK"';

comment on column AU_MARK_AUDIO.AUDIO_ID is
'参考"AU_AUDIO"';

comment on column AU_MARK_AUDIO.STATUS is
'记录是否确认';

/*==============================================================*/
/* Table: AU_MARK_PHOTO                                         */
/*==============================================================*/
create table AU_MARK_PHOTO 
(
   MARK_ID              NUMBER(10)           not null,
   PHOTO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUMARK_PHOTO foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);

comment on column AU_MARK_PHOTO.MARK_ID is
'外键,引用"AU_MARK"';

comment on column AU_MARK_PHOTO.PHOTO_ID is
'[170]参考"AU_PHOTO"';

comment on column AU_MARK_PHOTO.STATUS is
'[170]记录是否确认';

/*==============================================================*/
/* Table: AU_MARK_VIDEO                                         */
/*==============================================================*/
create table AU_MARK_VIDEO 
(
   MARK_ID              NUMBER(10)           not null,
   VIDEO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUMARK_VIDEO foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);

comment on table AU_MARK_VIDEO is
'[170]';

comment on column AU_MARK_VIDEO.MARK_ID is
'外键,引用"AU_MARK"';

comment on column AU_MARK_VIDEO.VIDEO_ID is
'参考"AU_VIDEO"';

comment on column AU_MARK_VIDEO.STATUS is
'记录是否确认';

/*==============================================================*/
/* Table: AU_MULTIMEDIA                                         */
/*==============================================================*/
create table AU_MULTIMEDIA 
(
   FILE_ID              NUMBER(10)           not null,
   NAME                 VARCHAR2(100),
   TYPE                 VARCHAR2(100),
   VERSION              VARCHAR2(100),
   FORMAT               VARCHAR2(100),
   FILE_SIZE            VARCHAR2(100),
   EXTENDED             VARCHAR2(100),
   GEOMETRY             SDO_GEOMETRY,
   WORKER               NUMBER(4)            default 0 not null,
   PRO_DATE             VARCHAR2(64),
   IMP_WORKER           NUMBER(4)            default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   MESH_ID              NUMBER(6)            default 0 not null,
   MEMO                 VARCHAR2(200),
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_AU_MULTIMEDIA primary key (FILE_ID)
);

comment on column AU_MULTIMEDIA.FILE_ID is
'主键';

comment on column AU_MULTIMEDIA.NAME is
'不带后缀的文件名,用于数据关联';

comment on column AU_MULTIMEDIA.TYPE is
'[170]记录存储的多媒体类型,如模式图,实景图,箭头图,POI Icon,Landmark,文字Icon,和建筑物纹理,材质,3D 模型等,以及道路名语音,分歧名称语音等';

comment on column AU_MULTIMEDIA.VERSION is
'[170]如06CY,08CY 等';

comment on column AU_MULTIMEDIA.FORMAT is
'[170]如JPG,PNG,BMP 等';

comment on column AU_MULTIMEDIA.FILE_SIZE is
'[171U][170]如64,32,16 等';

comment on column AU_MULTIMEDIA.EXTENDED is
'[170]如示意图,MASK,POINT_LIST 等';

comment on column AU_MULTIMEDIA.GEOMETRY is
'[170]存储以"度"为单位的经纬度坐标点';

comment on column AU_MULTIMEDIA.WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_MULTIMEDIA.PRO_DATE is
'[170]';

comment on column AU_MULTIMEDIA.IMP_WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_MULTIMEDIA.IMP_VERSION is
'[170]';

comment on column AU_MULTIMEDIA.IMP_DATE is
'[170]格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_MULTIMEDIA.MESH_ID is
'[170]';

comment on column AU_MULTIMEDIA.URL_DB is
'数据中心的文件存储路径名称';

comment on column AU_MULTIMEDIA.URL_FILE is
'音频文件存储的本地相对路径名,如\Data\Audio\';

/*==============================================================*/
/* Table: AU_PHOTO                                              */
/*==============================================================*/
create table AU_PHOTO 
(
   PHOTO_ID             NUMBER(10)           not null,
   CLASS                NUMBER(2)            default 1 not null
      constraint CKC_CLASS_AU_PHOTO check (CLASS in (1,2,3,4,5,6,7)) disable ,
   NAME                 VARCHAR2(254),
   MESH_ID              NUMBER(6)            default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   CAMERA_ID            NUMBER(2)            default 1 not null,
   GEOMETRY             SDO_GEOMETRY,
   ANGLE                NUMBER(8,5)          default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   IMP_WORKER           NUMBER(4)            default 0 not null,
   IMP_FILENAME         VARCHAR2(256),
   FORMAT               VARCHAR2(64),
   STORE_SPACE          VARCHAR2(64),
   "SIZE"               VARCHAR2(64),
   DEPTH                VARCHAR2(64),
   DPI                  VARCHAR2(64),
   MEMO                 VARCHAR2(200),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_AU_PHOTO primary key (PHOTO_ID)
);

comment on table AU_PHOTO is
'记录来自外业的POI照片,标记照片,道路名照片等成果';

comment on column AU_PHOTO.PHOTO_ID is
'主键';

comment on column AU_PHOTO.CLASS is
'[170]';

comment on column AU_PHOTO.NAME is
'[170]文件名(含扩展名)';

comment on column AU_PHOTO.MESH_ID_5K is
'记录照片所在的5000图幅号,格式为:605603_1_3';

comment on column AU_PHOTO.CAMERA_ID is
'默认为0,顺时针编号';

comment on column AU_PHOTO.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_PHOTO.ANGLE is
'与正北的夹角,-180~180度';

comment on column AU_PHOTO.DAY_TIME is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_PHOTO.WORKER is
'参考"BI_PERSON"';

comment on column AU_PHOTO.IMP_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_PHOTO.IMP_WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_PHOTO.FORMAT is
'如JPG,BMP,PNG等';

comment on column AU_PHOTO.STORE_SPACE is
'照片的存储容量';

comment on column AU_PHOTO."SIZE" is
'照片长宽的像素个数,如1024*768';

comment on column AU_PHOTO.DEPTH is
'照片具有的颜色个数,如8位,24位等';

comment on column AU_PHOTO.DPI is
'每英寸的像素个数';

comment on column AU_PHOTO.TASK_ID is
'记录内业的任务编号';

comment on column AU_PHOTO.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_PHOTO.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column AU_PHOTO.URL_DB is
'数据中心的文件存储路径名称';

comment on column AU_PHOTO.URL_FILE is
'照片文件存储的本地相对j路径名,如\Data\Photo\';

/*==============================================================*/
/* Table: AU_PT_COMPANY                                         */
/*==============================================================*/
create table AU_PT_COMPANY 
(
   AUDATA_ID            NUMBER(10)           not null,
   COMPANY_ID           NUMBER(10)           default 0 not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AU_PT_CO check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_CO check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_COMPANYID       VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_CO check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_CO check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_COMPANY primary key (AUDATA_ID)
);

comment on table AU_PT_COMPANY is
'[171A]公交公司是指负责公交线路和系统运营的公司,即管理公交系统的上级单位';

comment on column AU_PT_COMPANY.AUDATA_ID is
'主键';

comment on column AU_PT_COMPANY.COMPANY_ID is
'参考"PT_COMPANY"';

comment on column AU_PT_COMPANY.CITY_CODE is
'存储长度为4位';

comment on column AU_PT_COMPANY.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_COMPANY.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_POI                                             */
/*==============================================================*/
create table AU_PT_POI 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   POI_KIND             VARCHAR2(4),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_AU_PT_PO check (SIDE in (0,1,2,3)) disable ,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            VARCHAR2(1)          default '0' not null
      constraint CKC_ROAD_FLAG_AU_PT_PO check (ROAD_FLAG in ('0','1','2','3')) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   ACCESS_CODE          VARCHAR2(32),
   ACCESS_TYPE          VARCHAR2(10)         default '0' not null
      constraint CKC_ACCESS_TYPE_AU_PT_PO check (ACCESS_TYPE in ('0','1','2','3')) disable ,
   ACCESS_METH          NUMBER(3)            default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_AU_PT_PO check (EDIT_FLAG in (0,1)) disable ,
   POI_MEMO             VARCHAR2(200),
   OPERATOR             VARCHAR2(30),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_PO check (STATE in (0,1,2,3)) disable ,
   POI_NUM              VARCHAR2(100),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_GEO_OPRSTATUS_AU_PT_PO check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_GEO_CHECKSTATUS_AU_PT_PO check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_POI check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_POI check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   IMP_DATE             DATE,
   constraint PK_AU_PT_POI primary key (AUDATA_ID)
);

comment on table AU_PT_POI is
'[171A]公交POI由主点(Stop POI)和出入口(Access POI)两部分组成,每个主点对应一到多个出入口,出入口是主点的子POI.';

comment on column AU_PT_POI.AUDATA_ID is
'主键';

comment on column AU_PT_POI.PID is
'参考"PT_POI"';

comment on column AU_PT_POI.POI_KIND is
'参考"IX_POI_CODE"';

comment on column AU_PT_POI.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_PT_POI.LINK_PID is
'参考"RD_LINK"';

comment on column AU_PT_POI.SIDE is
'根据POI的显示坐标计算POI位于引导道路Link上或左侧或右侧(相对于Link方向)';

comment on column AU_PT_POI.NAME_GROUPID is
'[173sp2]参考"RD_NAME"';

comment on column AU_PT_POI.PMESH_ID is
'每个作业季POI在成果库中第一次与LINK建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';

comment on column AU_PT_POI.ACCESS_CODE is
'出入口名称中的顺序号或编号,如:”安定门A 口”,编号是” A”;”少年宫站Ａ２口”,编号是” Ａ２”;”世界之窗站１号口”,编号是” １”.出入口名称中没有编号的值为空';

comment on column AU_PT_POI.ACCESS_TYPE is
'出入,入口,出入口';

comment on column AU_PT_POI.ACCESS_METH is
'采用8bit 表示,从右到左依次为0~7bit,每bit 表示一种方式类型(如下),赋值为0/1 分别表示无/有,如:00000011 表示斜坡和阶梯;00000101 表示斜坡和扶梯
第0bit:斜坡
第1bit:阶梯
第2bit:扶梯
第3bit:直梯
第4bit:其他
如果所有bit 位均为0,表示不应用';

comment on column AU_PT_POI.MESH_ID_5K is
'记录公交POI所在的5000图幅号,格式为:605603_1_3';

comment on column AU_PT_POI.REGION_ID is
'参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column AU_PT_POI.EDIT_FLAG is
'用于数据完整提取时,区分是否可编辑';

comment on column AU_PT_POI.POI_NUM is
'记录来自NIDB的POI编号';

comment on column AU_PT_POI.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_PT_POI.GEO_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_POI.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_POI.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

comment on column AU_PT_POI.IMP_DATE is
'外业POI导入时,由DMS赋值,格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_PT_ETA_ACCESS                                      */
/*==============================================================*/
create table AU_PT_ETA_ACCESS 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   OPEN_PERIOD          VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)          default '0' not null
      constraint CKC_MANUAL_TICKET_AU_PT_ETA check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
      constraint CKC_AUTO_TICKET_AU_PT_ETAA check (AUTO_TICKET in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_ETA_ACCESS check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_ET check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_ETAA check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_ETAACCESS foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID) disable
);

comment on table AU_PT_ETA_ACCESS is
'[171A]描述现实世界公共出入口所具备的功能及其周边的附属服务设施等';

comment on column AU_PT_ETA_ACCESS.AUDATA_ID is
'外键,引用"AU_PT_POI"';

comment on column AU_PT_ETA_ACCESS.POI_PID is
'参考"PT_POI"';

comment on column AU_PT_ETA_ACCESS.OPEN_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column AU_PT_ETA_ACCESS.MANUAL_TICKET_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点有人看守,不记录日期';

comment on column AU_PT_ETA_ACCESS.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_ETA_ACCESS.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_LINE                                            */
/*==============================================================*/
create table AU_PT_LINE 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   SYSTEM_ID            NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TYPE                 NUMBER(2)            default 11 not null
      constraint CKC_TYPE_AU_PT_LI check (TYPE in (11,12,13,14,15,21,31,32,33,41,42,16,51,52,53,54,61)) disable ,
   COLOR                VARCHAR2(10),
   NIDB_LINEID          VARCHAR2(32),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_LI check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_LINE check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_LINE check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_LINE primary key (AUDATA_ID)
);

comment on table AU_PT_LINE is
'[171A]记录某地区的公交线路信息,比如 656路,特4路等';

comment on column AU_PT_LINE.AUDATA_ID is
'主键';

comment on column AU_PT_LINE.PID is
'参考"PT_LINE"';

comment on column AU_PT_LINE.SYSTEM_ID is
'参考"PT_SYSTEM"';

comment on column AU_PT_LINE.CITY_CODE is
'与行政区划代码没有直接关系,由生产部门维护';

comment on column AU_PT_LINE.COLOR is
'存储16进制的RGB值';

comment on column AU_PT_LINE.LOG is
'[173sp1]';

comment on column AU_PT_LINE.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_LINE.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_ETA_LINE                                        */
/*==============================================================*/
create table AU_PT_ETA_LINE 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   BIKE                 VARCHAR2(1)          default '0' not null
      constraint CKC_BIKE_AU_PT_ET check (BIKE in ('0','1','2')) disable ,
   BIKE_PERIOD          VARCHAR2(200),
   IMAGE                VARCHAR2(20),
   RACK                 VARCHAR2(1)          default '0' not null
      constraint CKC_RACK_AU_PT_ET check (RACK in ('0','1','2')) disable ,
   DINNER               VARCHAR2(1)          default '0' not null
      constraint CKC_DINNER_AU_PT_ET check (DINNER in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
      constraint CKC_TOILET_AU_PT_ETALINE check (TOILET in ('0','1','2')) disable ,
   SLEEPER              VARCHAR2(1)          default '0' not null
      constraint CKC_SLEEPER_AU_PT_ET check (SLEEPER in ('0','1','2')) disable ,
   WHEEL_CHAIR          VARCHAR2(1)          default '0' not null
      constraint CKC_WHEEL_CHAIR_AU_PT_ET check (WHEEL_CHAIR in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
      constraint CKC_SMOKE_AU_PT_ETALINE check (SMOKE in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_ETALINE check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_ETAL check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_ETAL check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_LINE_ETALINE foreign key (AUDATA_ID)
         references AU_PT_LINE (AUDATA_ID) disable
);

comment on table AU_PT_ETA_LINE is
'[171A]描述现实世界公共交通线路中具备的功能,如允许自行车,行李架,用餐服务等';

comment on column AU_PT_ETA_LINE.AUDATA_ID is
'外键,引用"AU_PT_LINE"';

comment on column AU_PT_ETA_LINE.PID is
'参考"PT_LINE"';

comment on column AU_PT_ETA_LINE.BIKE_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column AU_PT_ETA_LINE.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_ETA_LINE.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_ETA_STOP                                        */
/*==============================================================*/
create table AU_PT_ETA_STOP 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   PRIVATE_PARK         VARCHAR2(1)          default '0' not null
      constraint CKC_PRIVATE_PARK_AU_PT_ET check (PRIVATE_PARK in ('0','1','2','3')) disable ,
   PRIVATE_PARK_PERIOD  VARCHAR2(200),
   CARPORT_EXACT        VARCHAR2(32)         default '0' not null,
   CARPORT_ESTIMATE     VARCHAR2(1)          default '0' not null
      constraint CKC_CARPORT_ESTIMATE_AU_PT_ET check (CARPORT_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   BIKE_PARK            VARCHAR2(1)          default '0' not null
      constraint CKC_BIKE_PARK_AU_PT_ET check (BIKE_PARK in ('0','1','2','3')) disable ,
   BIKE_PARK_PERIOD     VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)          default '0' not null
      constraint CKC_MANUAL_TICKET_AU_PT_ET check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   MOBILE               VARCHAR2(1)          default '0' not null
      constraint CKC_MOBILE_AU_PT_ET check (MOBILE in ('0','1','2')) disable ,
   BAGGAGE_SECURITY     VARCHAR2(1)          default '0' not null
      constraint CKC_BAGGAGE_SECURITY_AU_PT_ET check (BAGGAGE_SECURITY in ('0','1','2')) disable ,
   LEFT_BAGGAGE         VARCHAR2(1)          default '0' not null
      constraint CKC_LEFT_BAGGAGE_AU_PT_ET check (LEFT_BAGGAGE in ('0','1','2')) disable ,
   CONSIGNATION_EXACT   VARCHAR2(32)         default '0' not null,
   CONSIGNATION_ESTIMATE VARCHAR2(1)          default '0' not null
      constraint CKC_CONSIGNATION_ESTI_AU_PT_ET check (CONSIGNATION_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   CONVENIENT           VARCHAR2(1)          default '0' not null
      constraint CKC_CONVENIENT_AU_PT_ET check (CONVENIENT in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
      constraint CKC_SMOKE_AU_PT_ETASTOP check (SMOKE in ('0','1','2')) disable ,
   BUILD_TYPE           VARCHAR2(1)          default '0' not null
      constraint CKC_BUILD_TYPE_AU_PT_ET check (BUILD_TYPE in ('0','1','2','3')) disable ,
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
      constraint CKC_AUTO_TICKET_AU_PT_ETAS check (AUTO_TICKET in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
      constraint CKC_TOILET_AU_PT_ETASTOP check (TOILET in ('0','1','2')) disable ,
   WIFI                 VARCHAR2(1)          default '0' not null
      constraint CKC_WIFI_AU_PT_ET check (WIFI in ('0','1','2')) disable ,
   OPEN_PERIOD          VARCHAR2(200),
   FARE_AREA            VARCHAR2(1),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_ETA_STOP check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_ETAS check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_ETAS check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_ETASTOP foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID) disable
);

comment on table AU_PT_ETA_STOP is
'[171A]描述现实世界公共交通站点具备的功能及其周边的附属服务设施等';

comment on column AU_PT_ETA_STOP.AUDATA_ID is
'外键,引用"AU_PT_POI"';

comment on column AU_PT_ETA_STOP.POI_PID is
'参考"PT_POI"';

comment on column AU_PT_ETA_STOP.PRIVATE_PARK is
'收费或免费';

comment on column AU_PT_ETA_STOP.PRIVATE_PARK_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column AU_PT_ETA_STOP.BIKE_PARK is
'是否有人看守';

comment on column AU_PT_ETA_STOP.BIKE_PARK_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点有人看守,不记录日期';

comment on column AU_PT_ETA_STOP.MANUAL_TICKET_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点有人看守,不记录日期';

comment on column AU_PT_ETA_STOP.OPEN_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column AU_PT_ETA_STOP.FARE_AREA is
'官方线路图的值';

comment on column AU_PT_ETA_STOP.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_ETA_STOP.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_LINE_NAME                                       */
/*==============================================================*/
create table AU_PT_LINE_NAME 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_PT_LI check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AU_PT_LI check (SRC_FLAG in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_LI check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_LI check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_LINE_NAME foreign key (AUDATA_ID)
         references AU_PT_LINE (AUDATA_ID) disable
);

comment on table AU_PT_LINE_NAME is
'[171A]';

comment on column AU_PT_LINE_NAME.AUDATA_ID is
'外键,引用"AU_PT_LINE"';

comment on column AU_PT_LINE_NAME.NAME_ID is
'参考"PT_LINE_NAME"';

comment on column AU_PT_LINE_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_PT_LINE_NAME.PID is
'参考"PT_LINE"';

comment on column AU_PT_LINE_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_PT_LINE_NAME.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column AU_PT_LINE_NAME.SRC_FLAG is
'现仅指英文名来源';

comment on column AU_PT_LINE_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_LINE_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_PLATFORM                                        */
/*==============================================================*/
create table AU_PT_PLATFORM 
(
   AUDATA_ID            NUMBER(10)           not null,
   PID                  NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   COLLECT              NUMBER(2)            default 0 not null
      constraint CKC_COLLECT_AU_PT_PL check (COLLECT in (0,1)) disable ,
   P_LEVEL              NUMBER(2)            default 0 not null
      constraint CKC_P_LEVEL_AU_PT_PL check (P_LEVEL in (4,3,2,1,0,-1,-2,-3,-4,-5,-6)) disable ,
   TRANSIT_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_TRANSIT_FLAG_AU_PT_PL check (TRANSIT_FLAG in (0,1)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_PLATFORM check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_PLATFORMID      VARCHAR2(32),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_GEO_OPRSTATUS_AU_PT_PL check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_GEO_CHECKSTATUS_AU_PT_PL check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_PLAT check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_PL check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   IMP_DATE             DATE,
   constraint PK_AU_PT_PLATFORM primary key (AUDATA_ID)
);

comment on table AU_PT_PLATFORM is
'[171A]站台,也叫月台,即公共交通车辆停靠时,供乘客候车和上下车的设施';

comment on column AU_PT_PLATFORM.AUDATA_ID is
'主键';

comment on column AU_PT_PLATFORM.PID is
'参考"PT_PLATFORM"';

comment on column AU_PT_PLATFORM.POI_PID is
'参考"PT_POI"';

comment on column AU_PT_PLATFORM.P_LEVEL is
'值域:-6~4;0表示地面';

comment on column AU_PT_PLATFORM.TRANSIT_FLAG is
'记录该站台是否能和其他站台互通,若能和其他站台互通,标识为"可换乘";若不能通往任何其他站台,标识为"不可换乘"';

comment on column AU_PT_PLATFORM.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_PT_PLATFORM.GEO_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_PLATFORM.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_PLATFORM.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

comment on column AU_PT_PLATFORM.IMP_DATE is
'外业POI导入时,由DMS赋值,格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_PT_PLATFORM_ACCESS                                 */
/*==============================================================*/
create table AU_PT_PLATFORM_ACCESS 
(
   AUDATA_ID            NUMBER(10)           not null,
   RELATE_ID            NUMBER(10)           default 0 not null,
   PLATFORM_ID          NUMBER(10)           default 0 not null,
   ACCESS_ID            NUMBER(10)           default 0 not null,
   AVAILABLE            NUMBER(1)            default 1 not null
      constraint CKC_AVAILABLE_AU_PT_PL check (AVAILABLE in (0,1)) disable ,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_PLATFORM_ACCE check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_PLATA check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_PT_PLATA check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_PLATFORMACCESS foreign key (AUDATA_ID)
         references AU_PT_PLATFORM (AUDATA_ID) disable
);

comment on table AU_PT_PLATFORM_ACCESS is
'[171A]记录站台与能到达该站台的入口点之间的对应关系,一个站台可以有多个入口点';

comment on column AU_PT_PLATFORM_ACCESS.AUDATA_ID is
'[173sp1]外键,引用"AU_PT_PLATFORM"';

comment on column AU_PT_PLATFORM_ACCESS.RELATE_ID is
'参考"PT_PLATFORM_ACCESS"';

comment on column AU_PT_PLATFORM_ACCESS.PLATFORM_ID is
'[173sp1]参考"PT_PLATFORM"';

comment on column AU_PT_PLATFORM_ACCESS.ACCESS_ID is
'参考"PT_POI"';

comment on column AU_PT_PLATFORM_ACCESS.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_PLATFORM_ACCESS.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_PLATFORM_NAME                                   */
/*==============================================================*/
create table AU_PT_PLATFORM_NAME 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_PT_PL check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AU_PT_PL check (SRC_FLAG in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_PLATN check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_PT_PLATN check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_PLATFORM_NAME foreign key (AUDATA_ID)
         references AU_PT_PLATFORM (AUDATA_ID) disable
);

comment on table AU_PT_PLATFORM_NAME is
'[171A]';

comment on column AU_PT_PLATFORM_NAME.AUDATA_ID is
'外键,引用"PT_PLATFORM"';

comment on column AU_PT_PLATFORM_NAME.NAME_ID is
'参考"PT_PLATFORM_NAME"';

comment on column AU_PT_PLATFORM_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_PT_PLATFORM_NAME.PID is
'参考"PT_PLATFORM"';

comment on column AU_PT_PLATFORM_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_PT_PLATFORM_NAME.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column AU_PT_PLATFORM_NAME.SRC_FLAG is
'现仅指英文名来源';

comment on column AU_PT_PLATFORM_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_PLATFORM_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_POI_PARENT                                      */
/*==============================================================*/
create table AU_PT_POI_PARENT 
(
   AUDATA_ID            NUMBER(10)           not null,
   GROUP_ID             NUMBER(10)           default 0 not null,
   PARENT_POI_PID       NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_POIP check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_POIP check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_POI_PARENT primary key (AUDATA_ID)
);

comment on table AU_PT_POI_PARENT is
'[171A]记录站点与出入口之间的关系信息';

comment on column AU_PT_POI_PARENT.AUDATA_ID is
'[173sp1]主键';

comment on column AU_PT_POI_PARENT.GROUP_ID is
'参考"PT_POI_PARENT"';

comment on column AU_PT_POI_PARENT.PARENT_POI_PID is
'参考"PT_POI"';

comment on column AU_PT_POI_PARENT.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_POI_PARENT.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_POI_CHILDREN                                    */
/*==============================================================*/
create table AU_PT_POI_CHILDREN 
(
   AUDATA_ID            NUMBER(10),
   GROUP_ID             NUMBER(10)           default 0 not null,
   CHILD_POI_PID        NUMBER(10)           default 0 not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
      constraint CKC_RELATION_TYPE_AU_PT_PO check (RELATION_TYPE in (0,1,2)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_POIC check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_POIC check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_CHILDREN foreign key (AUDATA_ID)
         references AU_PT_POI_PARENT (AUDATA_ID) disable
);

comment on table AU_PT_POI_CHILDREN is
'[171A]公交POI的父子关系,即站点与出入口之间的关系信息';

comment on column AU_PT_POI_CHILDREN.AUDATA_ID is
'[173sp1]外键,引用"AU_PT_POI_PARENT"';

comment on column AU_PT_POI_CHILDREN.GROUP_ID is
'参考"PT_POI_PARENT"';

comment on column AU_PT_POI_CHILDREN.CHILD_POI_PID is
'参考"PT_POI"';

comment on column AU_PT_POI_CHILDREN.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_POI_CHILDREN.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_POI_NAME                                        */
/*==============================================================*/
create table AU_PT_POI_NAME 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_PT_PO check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_AU_PT_PO check (NAME_CLASS in (1,2)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_POIN check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_POIN check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_NAME foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID) disable
);

comment on table AU_PT_POI_NAME is
'[171A]与IX_POI_NAME原则相同';

comment on column AU_PT_POI_NAME.AUDATA_ID is
'外键,引用"AU_PT_POI"';

comment on column AU_PT_POI_NAME.NAME_ID is
'参考"PT_POI_NAME"';

comment on column AU_PT_POI_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_PT_POI_NAME.POI_PID is
'参考"PT_POI"';

comment on column AU_PT_POI_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_PT_POI_NAME.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column AU_PT_POI_NAME.NIDB_PID is
'记录现有POI中已经出品的永久ID,不同语言类型PID不同';

comment on column AU_PT_POI_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_POI_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_STRAND                                          */
/*==============================================================*/
create table AU_PT_STRAND 
(
   AUDATA_ID            NUMBER(10)           not null,
   LINE_AUDATA_ID       NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           default 0 not null,
   PAIR_STRAND_PID      NUMBER(10)           default 0 not null,
   LINE_ID              NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   UP_DOWN              VARCHAR2(16)        
      constraint CKC_UP_DOWN_AU_PT_ST check (UP_DOWN is null or (UP_DOWN in ('Ｓ','Ｘ','Ｈ','ＮＨ','ＷＨ','ＣＨ','ＣＮＨ','ＣＷＨ'))) disable ,
   DISTANCE             VARCHAR2(10),
   TICKET_SYS           NUMBER(2)            default 0 not null
      constraint CKC_TICKET_SYS_AU_PT_ST check (TICKET_SYS in (0,1,2,9)) disable ,
   TICKET_START         VARCHAR2(255),
   TOTAL_PRICE          VARCHAR2(255),
   INCREASED_PRICE      VARCHAR2(255),
   INCREASED_STEP       VARCHAR2(255),
   GEOMETRY             SDO_GEOMETRY,
   NIDB_STRANDID        VARCHAR2(32),
   MEMO                 VARCHAR2(200),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_STRAND check (STATE in (0,1,2,3)) disable ,
   IMP_DATE             DATE,
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_GEO_OPRSTATUS_AU_PT_ST check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_GEO_CHECKSTATUS_AU_PT_ST check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_ST check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_ST check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_STRAND primary key (AUDATA_ID)
);

comment on table AU_PT_STRAND is
'[171A]Strand,即班次表,用来记录每条线路各个行车方向在不同的时间点发出的班次,及该班次的各类详细信息,如经过线路,首末车时间,发车间隔等';

comment on column AU_PT_STRAND.AUDATA_ID is
'主键';

comment on column AU_PT_STRAND.LINE_AUDATA_ID is
'[173sp1]参考"AU_PT_LINE"';

comment on column AU_PT_STRAND.PID is
'参考"PT_STRAND"';

comment on column AU_PT_STRAND.LINE_ID is
'参考"PT_LINE"';

comment on column AU_PT_STRAND.UP_DOWN is
'表示上行,下行,环行等,存储为全角字符';

comment on column AU_PT_STRAND.GEOMETRY is
'(1)Strand 行车轨迹,即几何坐标序列,与图廓线不做打断,坐标序列可自相交
(2)存储以"度"为单位的经纬度坐标序列,WKT 格式';

comment on column AU_PT_STRAND.LOG is
'[173sp1]';

comment on column AU_PT_STRAND.IMP_DATE is
'外业POI导入时,由DMS赋值,格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_PT_STRAND.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_PT_STRAND.GEO_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_STRAND.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_STRAND.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_STRAND_NAME                                     */
/*==============================================================*/
create table AU_PT_STRAND_NAME 
(
   AUDATA_ID            NUMBER(10)           not null,
   NAME_ID              NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_AU_PT_ST check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_AU_PT_ST check (NAME_CLASS in (1,2,3,4)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AU_PT_ST check (SRC_FLAG in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_STRN check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_PT_STRN check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_STRAND_NAME foreign key (AUDATA_ID)
         references AU_PT_STRAND (AUDATA_ID) disable
);

comment on table AU_PT_STRAND_NAME is
'[171A]';

comment on column AU_PT_STRAND_NAME.AUDATA_ID is
'外键,引用"AU_PT_STRAND" ';

comment on column AU_PT_STRAND_NAME.NAME_ID is
'参考"PT_STRAND_NAME"';

comment on column AU_PT_STRAND_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column AU_PT_STRAND_NAME.PID is
'参考"PT_STRAND"';

comment on column AU_PT_STRAND_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column AU_PT_STRAND_NAME.NAME_CLASS is
'[170][172U]';

comment on column AU_PT_STRAND_NAME.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column AU_PT_STRAND_NAME.SRC_FLAG is
'现仅指英文名来源';

comment on column AU_PT_STRAND_NAME.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_STRAND_NAME.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_STRAND_PLATFORM                                 */
/*==============================================================*/
create table AU_PT_STRAND_PLATFORM 
(
   AUDATA_ID            NUMBER(10)           not null,
   STRAND_PID           NUMBER(10)           default 0 not null,
   PLATFORM_PID         NUMBER(10)           default 0 not null,
   SEQ_NUM              NUMBER(10)           default 0 not null,
   INTERVAL             NUMBER(3)            default 0 not null,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITIONFLAG          VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_STRAND_PLAT check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_STRP check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTAT_AU_PT_STRP check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_STRAND_PLATFORM foreign key (AUDATA_ID)
         references AU_PT_STRAND (AUDATA_ID) disable
);

comment on table AU_PT_STRAND_PLATFORM is
'[171A]';

comment on column AU_PT_STRAND_PLATFORM.AUDATA_ID is
'外键,引用"AU_PT_STRAND" ';

comment on column AU_PT_STRAND_PLATFORM.STRAND_PID is
'参考"PT_STRAND"';

comment on column AU_PT_STRAND_PLATFORM.PLATFORM_PID is
'参考"PT_PLATFORM"';

comment on column AU_PT_STRAND_PLATFORM.SEQ_NUM is
'(1)记录公交线路某条Strand沿线的站台信息
(2)目前所有线路的站台统一从10000开始每次递增10000编号,即10000,20000,30000等';

comment on column AU_PT_STRAND_PLATFORM.INTERVAL is
'单位:分钟';

comment on column AU_PT_STRAND_PLATFORM.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_STRAND_PLATFORM.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_SYSTEM                                          */
/*==============================================================*/
create table AU_PT_SYSTEM 
(
   AUDATA_ID            NUMBER(10)           not null,
   SYSTEM_ID            NUMBER(10)           default 0 not null,
   COMPANY_ID           NUMBER(10)           default 0 not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_AU_PT_SY check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_SY check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_SYSTEMID        VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_SY check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_SY check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_COMPANY_SYSTEM foreign key (AUDATA_ID)
         references AU_PT_COMPANY (AUDATA_ID) disable
);

comment on table AU_PT_SYSTEM is
'[171A]公交系统是一个公交公司下属负责管理,运营具体公交线路的分支单位.通常是指隶属于同一个系统的公交线路的管理单位,即直接管理,运营巴士/地铁等线路的单位';

comment on column AU_PT_SYSTEM.AUDATA_ID is
'[173sp1]外键,引用"AU_PT_COMPANY"';

comment on column AU_PT_SYSTEM.SYSTEM_ID is
'参考"PT_SYSTEM"';

comment on column AU_PT_SYSTEM.COMPANY_ID is
'参考"PT_COMPANY"';

comment on column AU_PT_SYSTEM.CITY_CODE is
'存储长度为4位';

comment on column AU_PT_SYSTEM.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_SYSTEM.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_PT_TRANSFER                                        */
/*==============================================================*/
create table AU_PT_TRANSFER 
(
   AUDATA_ID            NUMBER(10)           not null,
   TRANSFER_ID          NUMBER(10)           default 0 not null,
   TRANSFER_TYPE        NUMBER(1)            default 1 not null
      constraint CKC_TRANSFER_TYPE_AU_PT_TR check (TRANSFER_TYPE in (0,1)) disable ,
   POI_FIR              NUMBER(10)           default 0 not null,
   POI_SEC              NUMBER(10)           default 0 not null,
   PLATFORM_FIR         NUMBER(10)           default 0 not null,
   PLATFORM_SEC         NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TRANSFER_TIME        NUMBER(2)            default 0 not null,
   EXTERNAL_FLAG        NUMBER(1)            default 0 not null
      constraint CKC_EXTERNAL_FLAG_AU_PT_TR check (EXTERNAL_FLAG in (0,1,2)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(200),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_TR check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
      constraint CKC_ATT_OPRSTATUS_AU_PT_TR check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
      constraint CKC_ATT_CHECKSTATUS_AU_PT_TR check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_TRANSFER primary key (AUDATA_ID)
);

comment on table AU_PT_TRANSFER is
'[171A]NaviMap制作时,如果线路图中的换乘站制作成了两个或多个主点(即站点),需要将对应的主点之间两两制作换乘类型为"跨站换乘"的关系.
如果线路图中的换乘站制作成了一个主点,需要在此主点中所有能够互通的站台之间两两制作换乘类型为"站内换乘"的关系.
即:如果是站内换乘,换乘点应该是站台编号;如果是站间换乘,换乘点应该是主点(即站点)';

comment on column AU_PT_TRANSFER.AUDATA_ID is
'主键';

comment on column AU_PT_TRANSFER.TRANSFER_ID is
'参考"PT_TRANSFER"';

comment on column AU_PT_TRANSFER.TRANSFER_TYPE is
'区分跨站换乘和站内换乘两种类型:
(1)跨站换乘,表达不同公交线路在两个相邻站点之间的换乘;此时,换乘点一和换乘点二分别表示站点
(2)站内换乘,表达不同公交线路在同一个站点内部的换乘,此时,换乘点一和换乘点二分别表示站台';

comment on column AU_PT_TRANSFER.POI_FIR is
'[173sp1]参考"PT_POI"';

comment on column AU_PT_TRANSFER.POI_SEC is
'[173sp1]参考"PT_POI"';

comment on column AU_PT_TRANSFER.PLATFORM_FIR is
'[173sp1]参考"PT_PLATFORM"';

comment on column AU_PT_TRANSFER.PLATFORM_SEC is
'[173sp1]参考"PT_PLATFORM"';

comment on column AU_PT_TRANSFER.TRANSFER_TIME is
'以分钟为单位,记录乘客换乘时步行需要的时间';

comment on column AU_PT_TRANSFER.EXTERNAL_FLAG is
'每一组跨站换乘关系,都需要制作"外部标识"属性,用来描述乘客换乘时是否需要走到站点外部.当两个主点之间有专用换乘通道时,"外部标识"制作为"否";若没有专用通道,乘客需要走到站点外面换乘,"外部标识"制作为"是".';

comment on column AU_PT_TRANSFER.ATT_TASK_ID is
'DMS导出内业作业数据时赋值(根据外业LOG)';

comment on column AU_PT_TRANSFER.FIELD_TASK_ID is
'外业POI导入DMS时赋值 ';

/*==============================================================*/
/* Table: AU_RECEIVE                                            */
/*==============================================================*/
create table AU_RECEIVE 
(
   ID                   NUMBER(10)           not null,
   TITLE                VARCHAR2(50),
   CONTENT              VARCHAR2(1000),
   RECEIVE_PERSON       NUMBER(10)           default 0 not null,
   RECEIVE_TIME         TIMESTAMP,
   RECEIVE_TYPE         NUMBER(1)            default 0 not null
      constraint CKC_RECEIVE_TYPE_AU_RECEI check (RECEIVE_TYPE in (0,1,2,3)) disable ,
   DATA_ID              VARCHAR2(32),
   constraint PK_AU_RECEIVE primary key (ID)
);

comment on column AU_RECEIVE.ID is
'主键';

comment on column AU_RECEIVE.TITLE is
'即回复主题';

comment on column AU_RECEIVE.DATA_ID is
'引用"AU_SPECIALCASE"或"TB_ABSTRACT_INFO"或"COMMUNICATION"';

/*==============================================================*/
/* Table: AU_SERIESPHOTO                                        */
/*==============================================================*/
create table AU_SERIESPHOTO 
(
   PHOTO_ID             NUMBER(10)           not null,
   PHOTO_GROUPID        NUMBER(10)           default 0 not null,
   CAMERA_ID            NUMBER(2)            default 1 not null,
   GEOMETRY             SDO_GEOMETRY,
   ANGLE                NUMBER(8,5)          default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   FILE_TYPE            VARCHAR2(32),
   "SIZE"               VARCHAR2(256),
   FORMAT               VARCHAR2(256),
   IMP_WORKER           NUMBER(4)            default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   constraint PK_AU_SERIESPHOTO primary key (PHOTO_ID)
);

comment on column AU_SERIESPHOTO.PHOTO_ID is
'主键';

comment on column AU_SERIESPHOTO.PHOTO_GROUPID is
'[170]采用"PHOTO_ID"作为组号';

comment on column AU_SERIESPHOTO.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_SERIESPHOTO.ANGLE is
'与正北的夹角,-180~180度';

comment on column AU_SERIESPHOTO.DAY_TIME is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_SERIESPHOTO.WORKER is
'参考"BI_PERSON"';

comment on column AU_SERIESPHOTO.TASK_ID is
'记录内业的任务编号';

comment on column AU_SERIESPHOTO.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column AU_SERIESPHOTO.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column AU_SERIESPHOTO.URL_DB is
'数据中心的文件存储路径名称';

comment on column AU_SERIESPHOTO.URL_FILE is
'存储相对路径名,如\Data\SeriesPhoto\';

comment on column AU_SERIESPHOTO.FILE_NAME is
'[170]文件名(含扩展名)';

comment on column AU_SERIESPHOTO.FILE_TYPE is
'[170]';

comment on column AU_SERIESPHOTO."SIZE" is
'[170]64*64,32*32,16*16';

comment on column AU_SERIESPHOTO.FORMAT is
'[170]WAV,ADP';

comment on column AU_SERIESPHOTO.IMP_WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_SERIESPHOTO.IMP_VERSION is
'[170]';

comment on column AU_SERIESPHOTO.IMP_DATE is
'[170]格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_SPECIALCASE                                        */
/*==============================================================*/
create table AU_SPECIALCASE 
(
   SPECIALCASE_ID       NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   STATUS               NUMBER(1)            default 1 not null
      constraint CKC_STATUS_AU_SPECI check (STATUS in (0,1)) disable ,
   TYPE                 NUMBER(5)            default 0 not null
      constraint CKC_TYPE_AU_SPECI check (TYPE in (0,1,2,3,4,5)) disable ,
   RANK                 NUMBER(1)            default 0 not null
      constraint CKC_RANK_AU_SPECI check (RANK in (0,1,2,3)) disable ,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   DESCRIPT             VARCHAR2(2000),
   constraint PK_AU_SPECIALCASE primary key (SPECIALCASE_ID)
);

comment on column AU_SPECIALCASE.SPECIALCASE_ID is
'主键';

comment on column AU_SPECIALCASE.GEOMETRY is
'特例作用的地理范围或点位';

comment on column AU_SPECIALCASE.STATUS is
'是否有效';

comment on column AU_SPECIALCASE.TYPE is
'来自现有生产案例集和特殊处理集';

comment on column AU_SPECIALCASE.RANK is
'需根据业务进一步定义';

comment on column AU_SPECIALCASE.DAY_TIME is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_SPECIALCASE.WORKER is
'参考"BI_PERSON"';

comment on column AU_SPECIALCASE.DESCRIPT is
'特例的说明文字(由系统填写时间,人员和任务号码)';

/*==============================================================*/
/* Table: AU_SPECIALCASE_IMAGE                                  */
/*==============================================================*/
create table AU_SPECIALCASE_IMAGE 
(
   SPECIALCASE_ID       NUMBER(10)           not null,
   FILENAME             VARCHAR2(254),
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint AUSPECIALCASE_IMAGE foreign key (SPECIALCASE_ID)
         references AU_SPECIALCASE (SPECIALCASE_ID) disable
);

comment on column AU_SPECIALCASE_IMAGE.SPECIALCASE_ID is
'外键,引用"AU_SPECIALCASE"';

comment on column AU_SPECIALCASE_IMAGE.FILENAME is
'快照图片的文件名称(含后缀名),如"256.bmp"';

comment on column AU_SPECIALCASE_IMAGE.URL_DB is
'数据中心的文件存储路径名称';

comment on column AU_SPECIALCASE_IMAGE.URL_FILE is
'照片文件存储的本地相对路径名,如\Data\Photo\';

/*==============================================================*/
/* Table: AU_TOPOIMAGE                                          */
/*==============================================================*/
create table AU_TOPOIMAGE 
(
   IMAGE_ID             NUMBER(10)           not null,
   IMAGE_NAME           VARCHAR2(100),
   DESCRIPT             VARCHAR2(500),
   constraint PK_AU_TOPOIMAGE primary key (IMAGE_ID)
);

comment on column AU_TOPOIMAGE.IMAGE_ID is
'主键';

/*==============================================================*/
/* Table: AU_VIDEO                                              */
/*==============================================================*/
create table AU_VIDEO 
(
   VIDEO_ID             NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   ANGLE                NUMBER(8,5)          default 0 not null,
   WORKER               NUMBER(4)            default 0 not null,
   START_TIME           DATE,
   END_TIME             DATE,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   FILE_TYPE            VARCHAR2(32),
   "SIZE"               VARCHAR2(256),
   FORMAT               VARCHAR2(256),
   IMP_WORKER           NUMBER(4)            default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   MESH_ID              NUMBER(6)            default 0 not null,
   constraint PK_AU_VIDEO primary key (VIDEO_ID)
);

comment on column AU_VIDEO.VIDEO_ID is
'主键';

comment on column AU_VIDEO.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column AU_VIDEO.ANGLE is
'与正北的夹角,-180~180度';

comment on column AU_VIDEO.WORKER is
'参考"BI_PERSON"';

comment on column AU_VIDEO.URL_DB is
'数据中心的文件存储路径名称';

comment on column AU_VIDEO.URL_FILE is
'存储相对路径名,如\Data\Video\';

comment on column AU_VIDEO.FILE_NAME is
'[170]文件名(含扩展名)';

comment on column AU_VIDEO.FILE_TYPE is
'[170]';

comment on column AU_VIDEO."SIZE" is
'[170]';

comment on column AU_VIDEO.FORMAT is
'[170]WAV,ADP';

comment on column AU_VIDEO.IMP_WORKER is
'[170]参考"BI_PERSON"';

comment on column AU_VIDEO.IMP_VERSION is
'[170]';

comment on column AU_VIDEO.IMP_DATE is
'[170]格式"YYYY/MM/DD HH:mm:ss"';

comment on column AU_VIDEO.MESH_ID is
'[170]';

/*==============================================================*/
/* Table: AU_WHITEBOARD                                         */
/*==============================================================*/
create table AU_WHITEBOARD 
(
   WHITEBOARD_ID        NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   STYLE                NUMBER(1)            default 0 not null
      constraint CKC_STYLE_AU_WHITE check (STYLE in (0,1)) disable ,
   COLOR                VARCHAR2(10)         not null,
   WIDTH                NUMBER(2)            default 1 not null,
   constraint PK_AU_WHITEBOARD primary key (WHITEBOARD_ID)
);

comment on table AU_WHITEBOARD is
'记录白板信息';

comment on column AU_WHITEBOARD.WHITEBOARD_ID is
'主键';

comment on column AU_WHITEBOARD.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列';

comment on column AU_WHITEBOARD.STYLE is
'实线,虚线';

comment on column AU_WHITEBOARD.COLOR is
'[173sp1]';

/*==============================================================*/
/* Table: AU_WHITEBOARD_PARAMETER                               */
/*==============================================================*/
create table AU_WHITEBOARD_PARAMETER 
(
   WHITEBOARD_ID        NUMBER(10)           not null,
   NAME                 VARCHAR2(32),
   PARAMETER            VARCHAR2(32),
   DESCRIPTION          VARCHAR2(200),
   constraint AUWHITEBOARD_PARAMETER foreign key (WHITEBOARD_ID)
         references AU_WHITEBOARD (WHITEBOARD_ID) disable
);

comment on table AU_WHITEBOARD_PARAMETER is
'记录白板参数信息';

comment on column AU_WHITEBOARD_PARAMETER.WHITEBOARD_ID is
'外键,引用"AU_WHITEBOARD"';

/*==============================================================*/
/* Table: BI_PERSON                                             */
/*==============================================================*/
create table BI_PERSON 
(
   PERSON_ID            NUMBER(4)            not null,
   NAME                 VARCHAR2(16),
   GENDER               NUMBER(1)            default 0 not null
      constraint CKC_GENDER_BI_PERSO check (GENDER in (0,1)) disable ,
   DEPARTMENT           VARCHAR2(100),
   WORK_GROUP           VARCHAR2(64),
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_PERSON primary key (PERSON_ID)
);

comment on column BI_PERSON.PERSON_ID is
'主键';

comment on column BI_PERSON.GENDER is
'男,女';

comment on column BI_PERSON.DEPARTMENT is
'如研发部';

comment on column BI_PERSON.WORK_GROUP is
'如设计组';

/*==============================================================*/
/* Table: BI_ROLE                                               */
/*==============================================================*/
create table BI_ROLE 
(
   ROLE_ID              NUMBER(2)            not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_BI_ROLE check (TYPE in (0,1,2,3)) disable ,
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_ROLE primary key (ROLE_ID)
);

comment on column BI_ROLE.ROLE_ID is
'主键';

comment on column BI_ROLE.TYPE is
'外业,内业,品质检查,品质监察等';

/*==============================================================*/
/* Table: BI_PERSON_ROLE                                        */
/*==============================================================*/
create table BI_PERSON_ROLE 
(
   PERSON_ID            NUMBER(4)            not null,
   ROLE_ID              NUMBER(2)            not null,
   constraint BIPERSON_ROLE foreign key (PERSON_ID)
         references BI_PERSON (PERSON_ID) disable ,
   constraint BIROLE_PERSON foreign key (ROLE_ID)
         references BI_ROLE (ROLE_ID) disable
);

comment on column BI_PERSON_ROLE.PERSON_ID is
'外键,引用"BI_PERSON"';

comment on column BI_PERSON_ROLE.ROLE_ID is
'外键,引用"BI_ROLE"';

/*==============================================================*/
/* Table: BI_POWER                                              */
/*==============================================================*/
create table BI_POWER 
(
   POWER_ID             NUMBER(2)            not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_BI_POWER check (TYPE in (0,1,2,3,4)) disable ,
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_POWER primary key (POWER_ID)
);

comment on column BI_POWER.POWER_ID is
'主键';

comment on column BI_POWER.TYPE is
'要素新增,修改,删除,查询,浏览等';

/*==============================================================*/
/* Table: BI_ROLE_POWER                                         */
/*==============================================================*/
create table BI_ROLE_POWER 
(
   ROLE_ID              NUMBER(2)            not null,
   POWER_ID             NUMBER(2)            not null,
   constraint BIROLE_POWER foreign key (ROLE_ID)
         references BI_ROLE (ROLE_ID) disable ,
   constraint BIPOWER_ROLE foreign key (POWER_ID)
         references BI_POWER (POWER_ID) disable
);

comment on column BI_ROLE_POWER.ROLE_ID is
'外键,引用"BI_ROLE"';

comment on column BI_ROLE_POWER.POWER_ID is
'外键,引用"BI_POWER"';

/*==============================================================*/
/* Table: BI_TASK                                               */
/*==============================================================*/
create table BI_TASK 
(
   TASK_ID              NUMBER(10)           not null,
   MAN_ID               NUMBER(4)            default 0 not null,
   FOCUS_ITEM           VARCHAR2(255),
   "RESOURCE"           VARCHAR2(200),
   TASK_TYPE            VARCHAR2(200),
   WORK_MODE            VARCHAR2(200),
   TASK_POWER           VARCHAR2(200),
   GEOMETRY             SDO_GEOMETRY,
   ASSIGN_TIME          DATE,
   START_TIME           DATE,
   END_TIME             DATE,
   DESCRIPT             VARCHAR2(200),
   SCHEDULE             VARCHAR2(32),
   STATUS               VARCHAR2(16),
   RESUME               VARCHAR2(8),
   TASK_URL             VARCHAR2(200),
   DATABASE_VER         VARCHAR2(8),
   URL_INTERFACE        VARCHAR2(200),
   constraint PK_BI_TASK primary key (TASK_ID)
);

comment on column BI_TASK.TASK_ID is
'主键,任务的唯一顺序编号';

comment on column BI_TASK.MAN_ID is
'该任务的作业员';

comment on column BI_TASK.FOCUS_ITEM is
'全要素,某作业项目等';

comment on column BI_TASK."RESOURCE" is
'包括车辆,设备,配合人员,作业规格变更的描述;以及影像,语音文件,示意图,预处理稿,网页等';

comment on column BI_TASK.TASK_TYPE is
'现场区域任务,现场规划路线任务,室内成果录入任务,一体化任务,情报任务,质检任务,监察任务,问联任务,评价任务,冲突处理任务,接边任务,内业任务.';

comment on column BI_TASK.WORK_MODE is
'车上作业,步行作业,全照片车上作业,精细化现场采集作业';

comment on column BI_TASK.TASK_POWER is
'描述该任务中,作业员所拥有的权限;比如只允许标记作业,或者允许数据修改等.以及任务可以修改的数据层(道路,背景等)POI,道路,背景的读写,只读';

comment on column BI_TASK.GEOMETRY is
'描述任务的地域范围';

comment on column BI_TASK.ASSIGN_TIME is
'任务的下达时间
格式"YYYY/MM/DD HH:mm:ss"';

comment on column BI_TASK.START_TIME is
'任务的预计开始时间
格式"YYYY/MM/DD HH:mm:ss"';

comment on column BI_TASK.END_TIME is
'任务的截止时间
格式"YYYY/MM/DD HH:mm:ss"';

comment on column BI_TASK.DESCRIPT is
'关于任务背景,注意事项等的描述';

comment on column BI_TASK.SCHEDULE is
'任务进展预估,如根据里程进行估算';

comment on column BI_TASK.STATUS is
'普通作业任务的状态包括:作业中,质检中,质检返工,完成(作业人员)质检作业任务的状态包括:质检中,质检返工等待,完成(质检人员)';

comment on column BI_TASK.RESUME is
'包括:生成,已分派,已完成,已取消(任务分配人员)';

comment on column BI_TASK.TASK_URL is
'任务数据的部署路径';

comment on column BI_TASK.DATABASE_VER is
'基础数据库版本:09冬,10夏';

comment on column BI_TASK.URL_INTERFACE is
'数据管理接口:URL序列';

/*==============================================================*/
/* Table: CI_GPSRECEIVED                                        */
/*==============================================================*/
create table CI_GPSRECEIVED 
(
   GUID                 VARCHAR2(64)         not null,
   GEOMETRY             SDO_GEOMETRY,
   LONGITUDE            NUMBER(8,5)          default 0 not null,
   LATITUDE             NUMBER(8,5)          default 0 not null,
   DIRECTION            NUMBER(8,5)          default 0 not null,
   SPEED                NUMBER(8,5)          default 0 not null,
   UTCTIME              VARCHAR2(20),
   UTCDATE              VARCHAR2(20),
   STATUS               VARCHAR2(20),
   STRING               VARCHAR2(1000),
   CREATED              DATE,
   WORKER               NUMBER(4)            default 0 not null,
   IN_WORKER            NUMBER(4)            default 0 not null,
   MEMO                 VARCHAR2(500),
   TASK_ID              NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_CI_GPSRECEIVED primary key (GUID)
);

comment on table CI_GPSRECEIVED is
'解析GPS原始信息中“$GPRMC,$GPGGA,$GPGSA,$GPGSV”等文本内容为一条记录';

comment on column CI_GPSRECEIVED.GUID is
'主键,由DCS生成全局唯一的ID';

comment on column CI_GPSRECEIVED.GEOMETRY is
'DCS采集时不维护,DCS成果导入时,将DCS采集点经度,纬度转为SDO_GEOMETRY对象';

comment on column CI_GPSRECEIVED.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CI_GPSRECEIVED.WORKER is
'参考"BI_PERSON"';

comment on column CI_GPSRECEIVED.IN_WORKER is
'参考"BI_PERSON"';

comment on column CI_GPSRECEIVED.TASK_ID is
'参考"BI_TASK"';

comment on column CI_GPSRECEIVED.FIELD_TASK_ID is
'参考"BI_TASK"';

/*==============================================================*/
/* Table: CI_GPSRECORD                                          */
/*==============================================================*/
create table CI_GPSRECORD 
(
   GUID                 VARCHAR2(64)         not null,
   GPS_STARTED_GUID     VARCHAR2(64),
   GPS_STOPPED_GUID     VARCHAR2(64),
   RESHAPED_GEOMETRY    VARCHAR2(4000),
   STATUS               NUMBER(1)            default 0 not null
      constraint CKC_STATUS_CI_GPSRE check (STATUS in (0,1,2,3,4,5,6,7,8)) disable ,
   GPSRECORD_ID         NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(1)            default 0 not null
      constraint CKC_SOURCE_CI_GPSRE check (SOURCE in (0,1,2,3,4,5,6,9)) disable ,
   NAME                 VARCHAR2(255),
   TABLE_NAME           VARCHAR2(64),
   KIND                 NUMBER(5)            default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   WORKER               NUMBER(4)            default 0 not null,
   IN_WORKER            NUMBER(4)            default 0 not null,
   DESCRIPT             VARCHAR2(200),
   REMARK               VARCHAR2(200),
   CREATED              DATE,
   MEMO                 VARCHAR2(500),
   DATA_VERSION         VARCHAR2(128),
   TASK_ID              NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_CI_GPSRECORD primary key (GUID)
);

comment on column CI_GPSRECORD.GUID is
'主键,由DCS生成全局唯一的ID';

comment on column CI_GPSRECORD.GPS_STARTED_GUID is
'参考"CI_GPSRECIEVED"';

comment on column CI_GPSRECORD.GPS_STOPPED_GUID is
'参考"CI_GPSRECIEVED"';

comment on column CI_GPSRECORD.RESHAPED_GEOMETRY is
'GPS轨迹修形几何,WKT格式';

comment on column CI_GPSRECORD.GPSRECORD_ID is
'DCS成果导入时,由DMS按PID原则赋值,供Navimap使用,DCS采集时赋值0';

comment on column CI_GPSRECORD.GEOMETRY is
'DCS成果导入时,将DCS测线几何转为SDO_GEOMETRY对象,DCS采集时不维护';

comment on column CI_GPSRECORD.NAME is
'按"名称ID,名称内容"方式存储,多个名称以英文半角"|"分割,如”100,学院路|202,成府路”';

comment on column CI_GPSRECORD.KIND is
'与TABLE_NAME表对应的种别代码相同,如TABLE_NAME为"LU_LINK_KIND",该字段取值与LU_LINK_KIND表中的"KIND"种别代码值相同';

comment on column CI_GPSRECORD.WORKER is
'参考"BI_PERSON"';

comment on column CI_GPSRECORD.IN_WORKER is
'参考"BI_PERSON"';

comment on column CI_GPSRECORD.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CI_GPSRECORD.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column CI_GPSRECORD.TASK_ID is
'参考"BI_TASK"';

comment on column CI_GPSRECORD.FIELD_TASK_ID is
'参考"BI_TASK"';

/*==============================================================*/
/* Table: CI_ITEM                                               */
/*==============================================================*/
create table CI_ITEM 
(
   GUID                 VARCHAR2(64)         not null,
   ITEM_LONGITUDE       NUMBER(8,5)          default 0,
   ITEM_LATITUDE        NUMBER(8,5)          default 0,
   ITEM_ALTITUDE        NUMBER(8,5)          default 0,
   ITEM_METHOD          NUMBER(2)            default 1
      constraint CKC_ITEM_METHOD_CI_ITEM check (ITEM_METHOD is null or (ITEM_METHOD in (1,2))) disable ,
   FACE_SOURCE_MBR      VARCHAR2(1000),
   MARK_ID              NUMBER(10)           default 0,
   GEOMETRY             SDO_GEOMETRY         not null,
   X_LEAD               NUMBER(8,5)          default 0 not null,
   Y_LEAD               NUMBER(8,5)          default 0 not null,
   ANGLE                NUMBER(8,5)          default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_CI_ITEM check (TYPE in (0,1,2,3,5)) disable ,
   MARK_ITEM            NUMBER(5)            default 0 not null
      constraint CKC_MARK_ITEM_CI_ITEM check (MARK_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,30,31,32,33,34,35,36,37,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,61,62,63,70,71,72,73,74,101,102,103,104,105,106,107,108,109,199)) disable ,
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   IN_WORKER            NUMBER(4)            default 0 not null,
   PARAM_L              VARCHAR2(1000),
   PARAM_R              VARCHAR2(1000),
   PARAM_S              VARCHAR2(1000),
   PARAM_EX             VARCHAR2(1000),
   STATUS               NUMBER(1)            default 0 not null
      constraint CKC_STATUS_CI_ITEM check (STATUS in (0,1,2,3,4,5,6,7,8)) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   DESCRIPT             VARCHAR2(200),
   REMARK               VARCHAR2(200),
   CREATED              DATE,
   MEMO                 VARCHAR2(500),
   DATA_VERSION         VARCHAR2(128),
   TASK_ID              NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_CI_ITEM primary key (GUID)
);

comment on column CI_ITEM.GUID is
'主键,由DCS生成全局唯一的ID';

comment on column CI_ITEM.ITEM_LONGITUDE is
'DCS采集点经度';

comment on column CI_ITEM.ITEM_LATITUDE is
'DCS采集点纬度';

comment on column CI_ITEM.ITEM_ALTITUDE is
'DCS采集点高程';

comment on column CI_ITEM.FACE_SOURCE_MBR is
'正面图片在关联照片中的MBR';

comment on column CI_ITEM.MARK_ID is
'DCS成果导入时,由DMS按PID原则赋值,供Navimap使用,DCS采集时赋值0';

comment on column CI_ITEM.GEOMETRY is
'DCS成果导入时,将DCS采集点经度,纬度转为SDO_GEOMETRY对象,DCS采集时不维护';

comment on column CI_ITEM.ANGLE is
'标记与正北方向的夹角,0~360度';

comment on column CI_ITEM.TYPE is
'文字(默认值)参数化标记,草图,照片,音频,测线等';

comment on column CI_ITEM.LINK_PID is
'参考"RD_LINK"';

comment on column CI_ITEM.NODE_PID is
'参考"RD_NODE"';

comment on column CI_ITEM.DAY_TIME is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CI_ITEM.WORKER is
'参考"BI_PERSON"';

comment on column CI_ITEM.IN_WORKER is
'参考"BI_PERSON"';

comment on column CI_ITEM.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CI_ITEM.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column CI_ITEM.TASK_ID is
'参考"BI_TASK"';

comment on column CI_ITEM.FIELD_TASK_ID is
'参考"BI_TASK"';

/*==============================================================*/
/* Table: CI_MEDIARECEIVED                                      */
/*==============================================================*/
create table CI_MEDIARECEIVED 
(
   GUID                 VARCHAR2(64)         not null,
   TYPE                 NUMBER(2)            default 1 not null
      constraint CKC_TYPE_CI_MEDIA check (TYPE in (1,2,3)) disable ,
   FILE_URL             VARCHAR2(500),
   GPS_GUID             VARCHAR2(64),
   GPS_GEOMETRY         SDO_GEOMETRY,
   GPS_LONGITUDE        NUMBER(8,5)          default 0 not null,
   GPS_LATITUDE         NUMBER(8,5)          default 0 not null,
   GPS_DIRECTION        NUMBER(8,5)          default 0 not null,
   DEVICE_NUM           VARCHAR2(100),
   CREATED              DATE,
   WORKER               NUMBER(4)            default 0 not null,
   IN_WORKER            NUMBER(4)            default 0 not null,
   MEMO                 VARCHAR2(500),
   TASK_ID              NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   constraint PK_CI_MEDIARECEIVED primary key (GUID)
);

comment on column CI_MEDIARECEIVED.GUID is
'主键,由DCS生成全局唯一的ID';

comment on column CI_MEDIARECEIVED.DEVICE_NUM is
'如多个相机时,指定的相机编号';

comment on column CI_MEDIARECEIVED.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CI_MEDIARECEIVED.WORKER is
'参考"BI_PERSON"';

comment on column CI_MEDIARECEIVED.IN_WORKER is
'参考"BI_PERSON"';

comment on column CI_MEDIARECEIVED.TASK_ID is
'参考"BI_TASK"';

comment on column CI_MEDIARECEIVED.FIELD_TASK_ID is
'参考"BI_TASK"';

/*==============================================================*/
/* Table: CI_ITEM_MEDIA                                         */
/*==============================================================*/
create table CI_ITEM_MEDIA 
(
   ITEM_GUID            VARCHAR2(64),
   MEDIA_GUID           VARCHAR2(64),
   MEDIA_TYPE           NUMBER(2)            default 1
      constraint CKC_MEDIA_TYPE_CI_ITEM_ check (MEDIA_TYPE is null or (MEDIA_TYPE in (1,2,3))) disable ,
   MEDIA_FILE           VARCHAR2(200),
   MARK_ID              NUMBER(10)           default 0,
   STATUS               VARCHAR2(100),
   constraint FK_CI_ITEM__REFERENCE_CI_ITEM foreign key (ITEM_GUID)
         references CI_ITEM (GUID) disable ,
   constraint FK_CI_ITEM__REFERENCE_CI_MEDIA foreign key (MEDIA_GUID)
         references CI_MEDIARECEIVED (GUID) disable
);

comment on column CI_ITEM_MEDIA.ITEM_GUID is
'外键,引用"CI_ITEM"';

comment on column CI_ITEM_MEDIA.MEDIA_GUID is
'外键,引用"CI_MEDIA"';

comment on column CI_ITEM_MEDIA.MEDIA_FILE is
'含文件后缀名';

comment on column CI_ITEM_MEDIA.MARK_ID is
'参考"CI_ITEM",DCS采集时赋值0,DCS成果导入时,DMS将采集项目ID号对应为MARK_ID赋值';

comment on column CI_ITEM_MEDIA.STATUS is
'记录是否确认';

/*==============================================================*/
/* Table: CK_EXCEPTION                                          */
/*==============================================================*/
create table CK_EXCEPTION 
(
   EXCEPTION_ID         NUMBER(10)           not null,
   RULE_ID              VARCHAR2(100),
   GROUP_ID             NUMBER(10)           default 0 not null,
   RANK                 NUMBER(10)           default 0 not null,
   SITUATION            VARCHAR2(4000),
   INFORMATION          VARCHAR2(4000),
   SUGGESTION           VARCHAR2(4000),
   GEOMETRY             VARCHAR2(4000),
   TARGETS              CLOB,
   ADDITION_INFO        CLOB,
   CREATE_DATE          DATE,
   UPDATE_DATE          DATE,
   constraint PK_CK_EXCEPTION primary key (EXCEPTION_ID)
);

comment on column CK_EXCEPTION.EXCEPTION_ID is
'主键';

comment on column CK_EXCEPTION.RULE_ID is
'[173sp1]参考"CK_RULE"';

comment on column CK_EXCEPTION.GEOMETRY is
'采用WKT 格式';

comment on column CK_EXCEPTION.CREATE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CK_EXCEPTION.UPDATE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: CK_RESULT                                             */
/*==============================================================*/
create table CK_RESULT 
(
   ID                   NUMBER(10)           not null,
   RULE_ID              VARCHAR2(100),
   PARA_LIST            CLOB                 not null,
   MODIFY_TOKEN         NUMBER(1)            default 0 not null
      constraint CKC_MODIFY_TOKEN_CK_RESUL check (MODIFY_TOKEN in (0,1)) disable ,
   CREATE_DATE          DATE,
   RESOLVE_DATE         DATE,
   constraint PK_CK_RESULT primary key (ID)
);

comment on column CK_RESULT.ID is
'主键';

comment on column CK_RESULT.RULE_ID is
'[173sp1]参考"CK_RULE"';

comment on column CK_RESULT.MODIFY_TOKEN is
'未修改或已修改';

comment on column CK_RESULT.CREATE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column CK_RESULT.RESOLVE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: CK_RULE                                               */
/*==============================================================*/
create table CK_RULE 
(
   RULE_ID              VARCHAR2(100)        not null,
   NAME                 VARCHAR2(254),
   CATEGORY             VARCHAR2(50),
   DESCRIPTION          CLOB                 not null,
   RANK                 NUMBER(2)            default 0 not null,
   NQL                  CLOB                 not null,
   ERROR_DESC           VARCHAR2(254),
   RESOLUTION           VARCHAR2(254),
   PARA_LIST            CLOB                 not null,
   URL                  VARCHAR2(254),
   CREATE_DATE          DATE,
   constraint PK_CK_RULE primary key (RULE_ID)
);

comment on column CK_RULE.RULE_ID is
'[173sp1]主键';

comment on column CK_RULE.CREATE_DATE is
'格式"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: CMG_BUILDING                                          */
/*==============================================================*/
create table CMG_BUILDING 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDING check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDING primary key (PID)
);

comment on column CMG_BUILDING.PID is
'主键';

comment on column CMG_BUILDING.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDING.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDFACE                                         */
/*==============================================================*/
create table CMG_BUILDFACE 
(
   FACE_PID             NUMBER(10)           not null,
   BUILDING_PID         NUMBER(10)           not null,
   KIND                 VARCHAR2(8)         
      constraint CKC_KIND_CMG_BUILDING check (KIND is null or (KIND in ('1001','1002','2001','3001','3002','4001','4002','4003','5001','5002','5003','5004','5005','6001','6002','6003','7001','8001','8002','8003','9001','9002','9003','9004','1101','1102','1103','1201','1202','1203','1204','1301','1302','1401','1402','1403','1501','1502','1503','1504','1505','1601'))) disable ,
   MASSING              NUMBER(1)            default 0 not null
      constraint CKC_MASSING_CMG_BUIL check (MASSING in (0,1)) disable ,
   HEIGHT               NUMBER(5,1)          default 0 not null,
   HEIGHT_ACURACY       NUMBER(3,1)          default 0.5 not null
      constraint CKC_HEIGHT_ACURACY_CMG_BUIL check (HEIGHT_ACURACY in (0,0.5,1,5,10)) disable ,
   HEIGHT_SOURCE        NUMBER(2)            default 1 not null
      constraint CKC_HEIGHT_SOURCE_CMG_BUIL check (HEIGHT_SOURCE in (1,2,3,4,5)) disable ,
   DATA_SOURCE          NUMBER(2)            default 3 not null
      constraint CKC_DATA_SOURCE_CMG_BUIL check (DATA_SOURCE in (1,2,3,4)) disable ,
   WALL_MATERIAL        NUMBER(2)            default 1 not null
      constraint CKC_WALL_MATERIAL_CMG_BUIL check (WALL_MATERIAL in (1,2,3,4,5,6,7,8,9)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_CMG_BUILDFACE check (EDIT_FLAG in (0,1)) disable ,
   CREATE_TIME          DATE,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDFACE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDFACE primary key (FACE_PID),
   constraint CMGBUILDFACE_BUILDING foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

comment on column CMG_BUILDFACE.FACE_PID is
'主键';

comment on column CMG_BUILDFACE.BUILDING_PID is
'外键,引用"CMG_BUILDING"';

comment on column CMG_BUILDFACE.KIND is
'工厂,商务楼,居民楼,餐饮等';

comment on column CMG_BUILDFACE.HEIGHT is
'[172U]';

comment on column CMG_BUILDFACE.HEIGHT_ACURACY is
'单位:米';

comment on column CMG_BUILDFACE.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column CMG_BUILDFACE.AREA is
'单位:平方米';

comment on column CMG_BUILDFACE.PERIMETER is
'单位:米';

comment on column CMG_BUILDFACE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column CMG_BUILDFACE.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDFACE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDFACE_TENANT                                  */
/*==============================================================*/
create table CMG_BUILDFACE_TENANT 
(
   FACE_PID             NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   FLAG                 NUMBER(1)            default 0 not null
      constraint CKC_FLAG_CMG_BUIL check (FLAG in (0,1)) disable ,
   TEL                  VARCHAR2(64),
   X                    NUMBER(10,5)         default 0 not null,
   Y                    NUMBER(10,5)         default 0 not null,
   NAME                 VARCHAR2(200),
   FLOOR                VARCHAR2(200),
   SRC_FLAG             NUMBER(1)            default 0 not null
      constraint CKC_SRC_FLAG_CMG_BUILDFACE_TEN check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDFACE_TEN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDFACE_TENANT foreign key (FACE_PID)
         references CMG_BUILDFACE (FACE_PID) disable
);

comment on table CMG_BUILDFACE_TENANT is
'[170]';

comment on column CMG_BUILDFACE_TENANT.FACE_PID is
'外键,引用"CMG_BUILDFACE"';

comment on column CMG_BUILDFACE_TENANT.POI_PID is
'记录租户或出租方的POI 号码,无POI 时记录为0';

comment on column CMG_BUILDFACE_TENANT.TEL is
'如:86-010-82306399 ';

comment on column CMG_BUILDFACE_TENANT.X is
'对应大厦的显示坐标';

comment on column CMG_BUILDFACE_TENANT.Y is
'对应大厦的显示坐标';

comment on column CMG_BUILDFACE_TENANT.NAME is
'大厦或内部租户的名称';

comment on column CMG_BUILDFACE_TENANT.FLOOR is
'大厦或租户的楼层信息';

comment on column CMG_BUILDFACE_TENANT.SRC_FLAG is
'注:如果CMG_BUILDING_3DMODEL 有记录赋值0,否则赋值1';

comment on column CMG_BUILDFACE_TENANT.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDFACE_TENANT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDNODE                                         */
/*==============================================================*/
create table CMG_BUILDNODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_CMG_BUIL check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_CMG_BUILDNODE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDNODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDNODE primary key (NODE_PID)
);

comment on column CMG_BUILDNODE.NODE_PID is
'主键';

comment on column CMG_BUILDNODE.FORM is
'无,图廓点,角点';

comment on column CMG_BUILDNODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column CMG_BUILDNODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column CMG_BUILDNODE.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDNODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDLINK                                         */
/*==============================================================*/
create table CMG_BUILDLINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_CMG_BUILDLINK check (KIND in (0,1)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_CMG_BUILDLINK check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDLINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDLINK primary key (LINK_PID),
   constraint CMGBUILDLINK_SNODE foreign key (S_NODE_PID)
         references CMG_BUILDNODE (NODE_PID) disable ,
   constraint CMGBUILDLINK_ENODE foreign key (E_NODE_PID)
         references CMG_BUILDNODE (NODE_PID) disable
);

comment on column CMG_BUILDLINK.LINK_PID is
'主键';

comment on column CMG_BUILDLINK.S_NODE_PID is
'外键,引用"CM_BUILDNODE"';

comment on column CMG_BUILDLINK.E_NODE_PID is
'外键,引用"CM_BUILDNODE"';

comment on column CMG_BUILDLINK.KIND is
'假想线或建筑物边界线';

comment on column CMG_BUILDLINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column CMG_BUILDLINK.LENGTH is
'单位:米';

comment on column CMG_BUILDLINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column CMG_BUILDLINK.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDLINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDFACE_TOPO                                    */
/*==============================================================*/
create table CMG_BUILDFACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDFACETOPO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDFACE_LINKS foreign key (FACE_PID)
         references CMG_BUILDFACE (FACE_PID) disable ,
   constraint CMGBUILDFACE_LINK foreign key (LINK_PID)
         references CMG_BUILDLINK (LINK_PID) disable
);

comment on table CMG_BUILDFACE_TOPO is
'记录建筑物面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column CMG_BUILDFACE_TOPO.FACE_PID is
'外键,引用"CMG_BUILDFACE"';

comment on column CMG_BUILDFACE_TOPO.SEQ_NUM is
'按逆时针方向,从1 开始递增编号';

comment on column CMG_BUILDFACE_TOPO.LINK_PID is
'外键,引用"CMG_BUILDLINK"';

comment on column CMG_BUILDFACE_TOPO.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDFACE_TOPO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDING_3DICON                                   */
/*==============================================================*/
create table CMG_BUILDING_3DICON 
(
   BUILDING_PID         NUMBER(10)           not null,
   WIDTH                NUMBER(5)            default 64 not null,
   HEIGHT               NUMBER(5)            default 64 not null,
   ICON_NAME            VARCHAR2(100),
   ALPHA_NAME           VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDING_3DI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDING_3DICON foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

comment on table CMG_BUILDING_3DICON is
'建筑物的3DLandMark图标表';

comment on column CMG_BUILDING_3DICON.BUILDING_PID is
'外键,引用"CMG_BUILDING"';

comment on column CMG_BUILDING_3DICON.WIDTH is
'单位:像素,默认为64';

comment on column CMG_BUILDING_3DICON.HEIGHT is
'单位:像素,默认为64';

comment on column CMG_BUILDING_3DICON.ICON_NAME is
'参考"AU_MULTIMEDIA"中"NAME"';

comment on column CMG_BUILDING_3DICON.ALPHA_NAME is
'参考"AU_MULTIMEDIA"中"NAME",TGA 格式';

comment on column CMG_BUILDING_3DICON.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDING_3DICON.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDING_3DMODEL                                  */
/*==============================================================*/
create table CMG_BUILDING_3DMODEL 
(
   MODEL_ID             NUMBER(10)           default 0 not null,
   BUILDING_PID         NUMBER(10)           not null,
   RESOLUTION           NUMBER(1)            default 0 not null
      constraint CKC_RESOLUTION_CMG_BUIL check (RESOLUTION in (0,1,2)) disable ,
   MODEL_NAME           VARCHAR2(100),
   MATERIAL_NAME        VARCHAR2(100),
   TEXTURE_NAME         VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDING_3DM check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDING_3DMODEL primary key (MODEL_ID),
   constraint CMGBUILDING_3DMODEL foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

comment on table CMG_BUILDING_3DMODEL is
'建筑物的3DLandMark模型表';

comment on column CMG_BUILDING_3DMODEL.MODEL_ID is
'[180U]主键';

comment on column CMG_BUILDING_3DMODEL.BUILDING_PID is
'外键,引用"CMG_BUILDING"';

comment on column CMG_BUILDING_3DMODEL.RESOLUTION is
'低,中,高';

comment on column CMG_BUILDING_3DMODEL.MODEL_NAME is
'[170]';

comment on column CMG_BUILDING_3DMODEL.MATERIAL_NAME is
'[170]';

comment on column CMG_BUILDING_3DMODEL.TEXTURE_NAME is
'[170]MTL 格式';

comment on column CMG_BUILDING_3DMODEL.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDING_3DMODEL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDING_NAME                                     */
/*==============================================================*/
create table CMG_BUILDING_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   BUILDING_PID         NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_CMG_BUIL check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')) disable ,
   FULL_NAME            VARCHAR2(256),
   BASE_NAME            VARCHAR2(256),
   BUILD_NUMBER         VARCHAR2(64),
   POINTADDRESS         VARCHAR2(200),
   FULL_NAME_PHONETIC   VARCHAR2(1000),
   BASE_NAME_PHONETIC   VARCHAR2(1000),
   BUILD_NUM_PHONETIC   VARCHAR2(1000),
   POINTADD_PHONETIC    VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_CMG_BUILDING_MANE check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDING_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDING_NAME primary key (NAME_ID),
   constraint CMGBUILDING_NAME foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

comment on column CMG_BUILDING_NAME.NAME_ID is
'[170]主键';

comment on column CMG_BUILDING_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column CMG_BUILDING_NAME.BUILDING_PID is
'外键,引用"CMG_BUILDING"';

comment on column CMG_BUILDING_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column CMG_BUILDING_NAME.FULL_NAME_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.BASE_NAME_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.BUILD_NUM_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.POINTADD_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.SRC_FLAG is
'[170]现仅指英文名来源
注:
(1)BUA 取值0~1
(2)其他取值0';

comment on column CMG_BUILDING_NAME.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDING_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

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
/* Table: CMG_BUILDING_POI                                      */
/*==============================================================*/
create table CMG_BUILDING_POI 
(
   BUILDING_PID         NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDING_POI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDING_POI foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable ,
   constraint IXPOI_CMGBUILDINGPOI foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on column CMG_BUILDING_POI.BUILDING_PID is
'外键,引用"CMG_BUILDING"';

comment on column CMG_BUILDING_POI.POI_PID is
'外键,引用"IX_POI"';

comment on column CMG_BUILDING_POI.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDING_POI.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDLINK_MESH                                    */
/*==============================================================*/
create table CMG_BUILDLINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDLINK_M check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDLINK_MESH foreign key (LINK_PID)
         references CMG_BUILDLINK (LINK_PID) disable
);

comment on column CMG_BUILDLINK_MESH.LINK_PID is
'外键,引用"CMG_BUILDLINK"';

comment on column CMG_BUILDLINK_MESH.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDLINK_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CMG_BUILDNODE_MESH                                    */
/*==============================================================*/
create table CMG_BUILDNODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CMG_BUILDNODE_M check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDNODE_MESH foreign key (NODE_PID)
         references CMG_BUILDNODE (NODE_PID) disable
);

comment on column CMG_BUILDNODE_MESH.NODE_PID is
'外键,引用"CMG_BUILDNODE"';

comment on column CMG_BUILDNODE_MESH.U_RECORD is
'增量更新标识';

comment on column CMG_BUILDNODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDING                                           */
/*==============================================================*/
create table CM_BUILDING 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILDING check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDING primary key (PID)
);

comment on column CM_BUILDING.PID is
'主键';

comment on column CM_BUILDING.U_RECORD is
'增量更新标识';

comment on column CM_BUILDING.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDFACE                                          */
/*==============================================================*/
create table CM_BUILDFACE 
(
   FACE_PID             NUMBER(10)           not null,
   BUILDING_PID         NUMBER(10)           not null,
   KIND                 VARCHAR2(8)         
      constraint CKC_KIND_CM_BUILDING check (KIND is null or (KIND in ('7110169','7110194','7110193','7110010','7110022','7110021','7110090','7110080','7110071','7110167','7110070','7110072','7110060','7110040','7110041','7110050','7110195','7110255'))) disable ,
   MASSING              NUMBER(1)            default 0 not null
      constraint CKC_MASSING_CM_BUILD check (MASSING in (0,1)) disable ,
   HEIGHT               NUMBER(3)            default 0 not null,
   LANDMARK_CODE        VARCHAR2(16),
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILDFACE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDFACE primary key (FACE_PID),
   constraint CMBUILDFACE_BUILDING foreign key (BUILDING_PID)
         references CM_BUILDING (PID) disable
);

comment on column CM_BUILDFACE.FACE_PID is
'主键';

comment on column CM_BUILDFACE.BUILDING_PID is
'外键,引用"CM_BUILDING"';

comment on column CM_BUILDFACE.KIND is
'工厂,商务楼,居民楼,餐饮等';

comment on column CM_BUILDFACE.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column CM_BUILDFACE.AREA is
'单位:平方米';

comment on column CM_BUILDFACE.PERIMETER is
'单位:米';

comment on column CM_BUILDFACE.U_RECORD is
'增量更新标识';

comment on column CM_BUILDFACE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDNODE                                          */
/*==============================================================*/
create table CM_BUILDNODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_CM_BUILD check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILDNODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDNODE primary key (NODE_PID)
);

comment on column CM_BUILDNODE.NODE_PID is
'主键';

comment on column CM_BUILDNODE.FORM is
'无,图廓点,角点';

comment on column CM_BUILDNODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column CM_BUILDNODE.U_RECORD is
'增量更新标识';

comment on column CM_BUILDNODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDLINK                                          */
/*==============================================================*/
create table CM_BUILDLINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_CM_BUILDLINK check (KIND in (0,1)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILDLINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDLINK primary key (LINK_PID),
   constraint CMBUILDLINK_SNODE foreign key (S_NODE_PID)
         references CM_BUILDNODE (NODE_PID) disable ,
   constraint CMBUILDLINK_ENODE foreign key (E_NODE_PID)
         references CM_BUILDNODE (NODE_PID) disable
);

comment on column CM_BUILDLINK.LINK_PID is
'主键';

comment on column CM_BUILDLINK.S_NODE_PID is
'外键,引用"CM_BUILDNODE"';

comment on column CM_BUILDLINK.E_NODE_PID is
'外键,引用"CM_BUILDNODE"';

comment on column CM_BUILDLINK.KIND is
'假想线或建筑物边界线';

comment on column CM_BUILDLINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column CM_BUILDLINK.LENGTH is
'单位:米';

comment on column CM_BUILDLINK.U_RECORD is
'增量更新标识';

comment on column CM_BUILDLINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDFACE_TOPO                                     */
/*==============================================================*/
create table CM_BUILDFACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILDFACE_TOPO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMBUILDFACE_LINKS foreign key (FACE_PID)
         references CM_BUILDFACE (FACE_PID) disable ,
   constraint CMBUILDFACE_LINK foreign key (LINK_PID)
         references CM_BUILDLINK (LINK_PID) disable
);

comment on table CM_BUILDFACE_TOPO is
'记录建筑物面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column CM_BUILDFACE_TOPO.FACE_PID is
'外键,引用"CM_BUILDFACE"';

comment on column CM_BUILDFACE_TOPO.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column CM_BUILDFACE_TOPO.LINK_PID is
'外键,引用"CM_BUILDLINK"';

comment on column CM_BUILDFACE_TOPO.U_RECORD is
'增量更新标识';

comment on column CM_BUILDFACE_TOPO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDLINK_MESH                                     */
/*==============================================================*/
create table CM_BUILDLINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILD_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMBUILDLINK_MESH foreign key (LINK_PID)
         references CM_BUILDLINK (LINK_PID) disable
);

comment on column CM_BUILDLINK_MESH.LINK_PID is
'外键,引用"CM_BUILDLINK"';

comment on column CM_BUILDLINK_MESH.U_RECORD is
'增量更新标识';

comment on column CM_BUILDLINK_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: CM_BUILDNODE_MESH                                     */
/*==============================================================*/
create table CM_BUILDNODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_CM_BUILDNODE_M check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMBUILDNODE_MESH foreign key (NODE_PID)
         references CM_BUILDNODE (NODE_PID) disable
);

comment on column CM_BUILDNODE_MESH.NODE_PID is
'外键,引用"CM_BUILDNODE"';

comment on column CM_BUILDNODE_MESH.U_RECORD is
'增量更新标识';

comment on column CM_BUILDNODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: DEM_GRID                                              */
/*==============================================================*/
create table DEM_GRID 
(
   DEM_ID               NUMBER(10)           not null,
   SW_X                 NUMBER(8)            default 0 not null,
   SW_Y                 NUMBER(8)            default 0 not null,
   NE_X                 NUMBER(8)            default 0 not null,
   NE_Y                 NUMBER(8)            default 0 not null,
   WIDTH                NUMBER(5)            default 0 not null,
   HEIGHT               NUMBER(6)            default 0 not null,
   constraint PK_DEM_GRID primary key (DEM_ID)
);

comment on column DEM_GRID.DEM_ID is
'主键';

/*==============================================================*/
/* Table: DEM_ELEVATION                                         */
/*==============================================================*/
create table DEM_ELEVATION 
(
   ELEVATION_ID         NUMBER(10)           not null,
   DEM_ID               NUMBER(10)           not null,
   X_VAL                NUMBER(8)            default 0 not null,
   Y_VAL                NUMBER(8)            default 0 not null,
   Z_VAL                NUMBER(8)            default 0 not null,
   constraint PK_DEM_ELEVATION primary key (ELEVATION_ID),
   constraint DEMELEVATION_GRID foreign key (DEM_ID)
         references DEM_GRID (DEM_ID) disable
);

comment on column DEM_ELEVATION.ELEVATION_ID is
'主键';

comment on column DEM_ELEVATION.DEM_ID is
'外键,引用"DEM_GRID"';

/*==============================================================*/
/* Table: DTM_INFO                                              */
/*==============================================================*/
create table DTM_INFO 
(
   DTM_ID               NUMBER(10)           not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_DTM_INFO primary key (DTM_ID)
);

comment on column DTM_INFO.DTM_ID is
'主键';

comment on column DTM_INFO.URL_DB is
'数据中心的文件存储路径名称';

comment on column DTM_INFO.URL_FILE is
'存储相对路径名,如\Data\Video\';

/*==============================================================*/
/* Table: EF_3DMAP                                              */
/*==============================================================*/
create table EF_3DMAP 
(
   THREED_ID            NUMBER(10)           not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_3DMAP primary key (THREED_ID)
);

comment on column EF_3DMAP.THREED_ID is
'主键';

comment on column EF_3DMAP.URL_DB is
'数据中心的文件存储路径名称';

comment on column EF_3DMAP.URL_FILE is
'存储相对路径名,如\Data\3dmap\';

/*==============================================================*/
/* Table: EF_IMAGE                                              */
/*==============================================================*/
create table EF_IMAGE 
(
   IMAGE_ID             NUMBER(10)           not null,
   WIDTH                NUMBER(5)            default 0 not null,
   HEIGHT               NUMBER(5)            default 0 not null,
   ALPHA                NUMBER(1)            default 1 not null
      constraint CKC_ALPHA_EF_IMAGE check (ALPHA in (1,2)) disable ,
   TYPE                 NUMBER(1)            default 1 not null
      constraint CKC_TYPE_EF_IMAGE check (TYPE in (1,2)) disable ,
   FORMAT               NUMBER(1)            default 2 not null
      constraint CKC_FORMAT_EF_IMAGE check (FORMAT in (1,2)) disable ,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_IMAGE primary key (IMAGE_ID)
);

comment on column EF_IMAGE.IMAGE_ID is
'主键';

comment on column EF_IMAGE.WIDTH is
'单位:像素';

comment on column EF_IMAGE.HEIGHT is
'单位:像素';

comment on column EF_IMAGE.URL_DB is
'数据中心的文件存储路径名称';

comment on column EF_IMAGE.URL_FILE is
'存储相对路径名,如\Data\Video\';

/*==============================================================*/
/* Table: EF_LINEMAP                                            */
/*==============================================================*/
create table EF_LINEMAP 
(
   LINEMAP_ID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 0 not null,
   ELEVATION            NUMBER(3)            default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_LINEMAP primary key (LINEMAP_ID)
);

comment on column EF_LINEMAP.LINEMAP_ID is
'主键';

comment on column EF_LINEMAP.URL_DB is
'数据中心的文件存储路径名称';

comment on column EF_LINEMAP.URL_FILE is
'存储相对路径名,如\Data\Video\';

/*==============================================================*/
/* Table: HW_ESTAB                                              */
/*==============================================================*/
create table HW_ESTAB 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(2)            default 0 not null
      constraint CKC_TYPE_HW_ESTAB check (TYPE in (0,1,2,4)) disable ,
   ATTR                 NUMBER(2)            default 0 not null
      constraint CKC_ATTR_HW_ESTAB check (ATTR in (0,1,2,4,8,16,32)) disable ,
   FLAG                 NUMBER(1)            default 0 not null
      constraint CKC_FLAG_HW_ESTAB2 check (FLAG in (0,1,2)) disable ,
   SUFFIX               NUMBER(2)            default 0 not null
      constraint CKC_SUFFIX_HW_ESTAB check (SUFFIX in (0,1,2,3,4,5,8,16,17,18,19,24,25,26)) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   ESTAB_PRE            NUMBER(10)           default 0 not null,
   ESTAB_NEX            NUMBER(10)           default 0 not null,
   DIST_BETW            NUMBER(15,3)         default 0 not null,
   DIST_INNER           NUMBER(15,3)         default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_ESTAB2 check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB primary key (PID)
);

comment on column HW_ESTAB.PID is
'主键';

comment on column HW_ESTAB.FLAG is
'注: 当FLAG=1 时, 在HW_ESTAB_MAIN 和HW_ESTAB_CONTAIN 中记录主并设关系';

comment on column HW_ESTAB.REGION_ID is
'参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column HW_ESTAB.ESTAB_PRE is
'本线上根据道路方向判断相对于该设施的上一个设施的号码
参考"HW_ESTAB"';

comment on column HW_ESTAB.ESTAB_NEX is
'本线上根据道路方向判断相对于该设施的下一个设施的号码
参考"HW_ESTAB"';

comment on column HW_ESTAB.DIST_BETW is
'单位:米,记录相对于下一个设施的距离';

comment on column HW_ESTAB.DIST_INNER is
'单位:米';

comment on column HW_ESTAB.U_RECORD is
'增量更新标识';

comment on column HW_ESTAB.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_ESTAB_MAIN                                         */
/*==============================================================*/
create table HW_ESTAB_MAIN 
(
   GROUP_ID             NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_ESTAB_MAIN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB_MAIN primary key (GROUP_ID)
);

comment on table HW_ESTAB_MAIN is
'记录HW_ESTAB表中标识为主设(有并设),即FLAG=2的信息';

comment on column HW_ESTAB_MAIN.GROUP_ID is
'主键';

comment on column HW_ESTAB_MAIN.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_ESTAB_MAIN.U_RECORD is
'增量更新标识';

comment on column HW_ESTAB_MAIN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_ESTAB_CONTAIN                                      */
/*==============================================================*/
create table HW_ESTAB_CONTAIN 
(
   GROUP_ID             NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 1 not null
      constraint CKC_TYPE_HW_ESTAB1 check (TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_ESTAB_CONTAIN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint HWESTAB_MAIN_CONTAIN foreign key (GROUP_ID)
         references HW_ESTAB_MAIN (GROUP_ID) disable
);

comment on column HW_ESTAB_CONTAIN.GROUP_ID is
'外键,引用"HW_ESTAB_MAIN"';

comment on column HW_ESTAB_CONTAIN.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_ESTAB_CONTAIN.U_RECORD is
'增量更新标识';

comment on column HW_ESTAB_CONTAIN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_ESTAB_JCT                                          */
/*==============================================================*/
create table HW_ESTAB_JCT 
(
   JCT_PID              NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10)           not null,
   S_HWLINK_PID         NUMBER(10)           not null,
   E_HWLINK_PID         NUMBER(10)           not null,
   S_JCTLINK_PID        NUMBER(10)           not null,
   E_JCTLINK_PID        NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   DIS_BETW             NUMBER(15,3)         default 0 not null,
   ORIETATION           NUMBER(8,5)          default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_ESTAB3 check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB_JCT primary key (JCT_PID),
   constraint HWESTAB_JCT foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

comment on column HW_ESTAB_JCT.JCT_PID is
'主键';

comment on column HW_ESTAB_JCT.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_ESTAB_JCT.S_HWLINK_PID is
'外键,引用"RD_LINK"';

comment on column HW_ESTAB_JCT.E_HWLINK_PID is
'外键,引用"RD_LINK"';

comment on column HW_ESTAB_JCT.S_JCTLINK_PID is
'外键,引用"RD_LINK"';

comment on column HW_ESTAB_JCT.E_JCTLINK_PID is
'外键,引用"RD_LINK"';

comment on column HW_ESTAB_JCT.S_NODE_PID is
'外键,引用"RD_NODE"';

comment on column HW_ESTAB_JCT.E_NODE_PID is
'外键,引用"RD_NODE"';

comment on column HW_ESTAB_JCT.DIS_BETW is
'单位:米,起点至终点的距离';

comment on column HW_ESTAB_JCT.ORIETATION is
'单位:度,起终点连线与起点前一点连线的夹角';

comment on column HW_ESTAB_JCT.U_RECORD is
'增量更新标识';

comment on column HW_ESTAB_JCT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_ESTAB_NAME                                         */
/*==============================================================*/
create table HW_ESTAB_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ESTAB_PID            NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_HW_ESTAB check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(1000),
   PHONETIC             VARCHAR2(1000),
   FLAG                 NUMBER(1)            default 0 not null
      constraint CKC_FLAG_HW_ESTAB1 check (FLAG in (0,1,2)) disable ,
   VOICE_FLAG           NUMBER(2)            default 0 not null
      constraint CKC_INPUT_TIME_RD_LINK_NAME check (VOICE_FLAG in (0,1)) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_NAME1 check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB_NAME primary key (NAME_ID),
   constraint HWESTAB_NAME foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

comment on column HW_ESTAB_NAME.NAME_ID is
'主键';

comment on column HW_ESTAB_NAME.NAME_GROUPID is
'从1开始递增编号';

comment on column HW_ESTAB_NAME.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_ESTAB_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column HW_ESTAB_NAME.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column HW_ESTAB_NAME.U_RECORD is
'增量更新标识';

comment on column HW_ESTAB_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_ESTAB_SA                                           */
/*==============================================================*/
create table HW_ESTAB_SA 
(
   ESTAB_PID            NUMBER(10),
   INNER_ITEM           NUMBER(3)            default 0
      constraint CKC_INNER_ITEM_HW_ESTAB check (INNER_ITEM is null or (INNER_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,99))) disable ,
   KIND                 VARCHAR2(32),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_ESTAB1 check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint HWESTAB_SA foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

comment on column HW_ESTAB_SA.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_ESTAB_SA.KIND is
'如"1300"代表餐馆,"2200"代表商店
';

comment on column HW_ESTAB_SA.U_RECORD is
'增量更新标识';

comment on column HW_ESTAB_SA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_INFO                                               */
/*==============================================================*/
create table HW_INFO 
(
   HW_PID_UP            NUMBER(10)           not null,
   HW_PID_DW            NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_INFO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_INFO primary key (HW_PID_UP)
);

comment on column HW_INFO.HW_PID_UP is
'主键,同PID分配原则';

comment on column HW_INFO.HW_PID_DW is
'同PID分配原则';

comment on column HW_INFO.NAME_GROUPID is
'外键,引用道路名库"RD_NAME"';

comment on column HW_INFO.U_RECORD is
'增量更新标识';

comment on column HW_INFO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_POSITION                                           */
/*==============================================================*/
create table HW_POSITION 
(
   POSITION_PID         NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10),
   LINK_PID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   ACCESS_TYPE          NUMBER(1)            default 0 not null
      constraint CKC_ACCESS_TYPE_HW_POSIT check (ACCESS_TYPE in (0,1,2)) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_POSIT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_POSITION primary key (POSITION_PID),
   constraint HWESTAB_IC foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

comment on column HW_POSITION.POSITION_PID is
'主键';

comment on column HW_POSITION.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_POSITION.LINK_PID is
'外键,引用"RD_LINK"';

comment on column HW_POSITION.NODE_PID is
'外键,引用"RD_NODE"';

comment on column HW_POSITION.ACCESS_TYPE is
'出入口,入口,出口';

comment on column HW_POSITION.U_RECORD is
'增量更新标识';

comment on column HW_POSITION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: HW_ROUTE                                              */
/*==============================================================*/
create table HW_ROUTE 
(
   ROUTE_PID            NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10),
   LINK_PID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   FLAG                 NUMBER(2)            default 0 not null
      constraint CKC_FLAG_HW_ROUTE check (FLAG in (0,1,2)) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_HW_ROUTE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ROUTE primary key (ROUTE_PID),
   constraint HWESTAB_ROUTE foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

comment on column HW_ROUTE.ROUTE_PID is
'主键';

comment on column HW_ROUTE.ESTAB_PID is
'外键,引用"HW_ESTAB"';

comment on column HW_ROUTE.LINK_PID is
'外键,引用"RD_LINK"';

comment on column HW_ROUTE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column HW_ROUTE.U_RECORD is
'增量更新标识';

comment on column HW_ROUTE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IDB_COUNTRY_INFO                                      */
/*==============================================================*/
create table IDB_COUNTRY_INFO 
(
   IDB_COUNTRY_ID       VARCHAR2(1000),
   VER_NAME             VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   ITEM_VER             VARCHAR2(1000),
   IDB_REGION_ID        VARCHAR2(1000),
   PERSON               NUMBER(10)           default 0,
   MEMO                 VARCHAR2(1000)
);

comment on table IDB_COUNTRY_INFO is
'[173A]';

comment on column IDB_COUNTRY_INFO.VER_NAME is
'如12Q1';

comment on column IDB_COUNTRY_INFO.ITEM is
'用户设置:如13CY,NIMIF-G,NAVEX,NIGDF-G,其他';

comment on column IDB_COUNTRY_INFO.ITEM_VER is
'系统从1.0开始,依次加1';

comment on column IDB_COUNTRY_INFO.IDB_REGION_ID is
'参考"IDB_REGION_INFO"';

comment on column IDB_COUNTRY_INFO.PERSON is
'参考"BI_PERSON"';

comment on column IDB_COUNTRY_INFO.MEMO is
'用户设置';

/*==============================================================*/
/* Table: IDB_DIFF_INFO                                         */
/*==============================================================*/
create table IDB_DIFF_INFO 
(
   IDB_DIFF_ID          VARCHAR2(1000),
   VER_NAME             VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   CUR_IDB_REGION_ID    VARCHAR2(1000),
   PRE_IDB_REGION_ID    VARCHAR2(1000),
   DIFF_PERSON          NUMBER(10)           default 0,
   DIFF_TIME            DATE,
   DIFF_SOFT_NAME       VARCHAR2(1000),
   DIFF_SOFT_VER        VARCHAR2(1000),
   MEMO                 VARCHAR2(1000)
);

comment on table IDB_DIFF_INFO is
'[173A]';

comment on column IDB_DIFF_INFO.VER_NAME is
'如12Q1';

comment on column IDB_DIFF_INFO.ITEM is
'用户设置:如13CY,NIMIF-G,NAVEX,NIGDF-G,其他';

comment on column IDB_DIFF_INFO.CUR_IDB_REGION_ID is
'参考"IDB_REGION_INFO",多个之间采用半角"|"分隔';

comment on column IDB_DIFF_INFO.PRE_IDB_REGION_ID is
'参考"IDB_REGION_INFO",多个之间采用半角"|"分隔';

comment on column IDB_DIFF_INFO.DIFF_PERSON is
'参考"BI_PERSON"';

comment on column IDB_DIFF_INFO.DIFF_TIME is
'系统设置';

comment on column IDB_DIFF_INFO.DIFF_SOFT_NAME is
'系统设置';

comment on column IDB_DIFF_INFO.DIFF_SOFT_VER is
'系统设置';

comment on column IDB_DIFF_INFO.MEMO is
'用户设置';

/*==============================================================*/
/* Table: IDB_REGION_INFO                                       */
/*==============================================================*/
create table IDB_REGION_INFO 
(
   IDB_REGION_ID        VARCHAR2(1000)       not null,
   VER_NAME             VARCHAR2(1000),
   IDB_REGION_NAME      VARCHAR2(1000),
   IDB_REGION_NUM       VARCHAR2(1000),
   ACH_GDB_ID           VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   PERSON               NUMBER(10)           default 0,
   MEMO                 VARCHAR2(1000),
   constraint PK_IDB_REGION_INFO primary key (IDB_REGION_ID)
);

comment on table IDB_REGION_INFO is
'[173A]';

comment on column IDB_REGION_INFO.IDB_REGION_ID is
'主键';

comment on column IDB_REGION_INFO.VER_NAME is
'如12Q1';

comment on column IDB_REGION_INFO.IDB_REGION_NUM is
'系统从1.0开始,依次加1';

comment on column IDB_REGION_INFO.ACH_GDB_ID is
'参考"ACH_GDB_INFO",多个之间采用半角"|"分隔';

comment on column IDB_REGION_INFO.ITEM is
'用户设置:如13CY,NIMIF-G,NAVEX,NIGDF-G,其他';

comment on column IDB_REGION_INFO.PERSON is
'参考"BI_PERSON"';

comment on column IDB_REGION_INFO.MEMO is
'用户设置';

/*==============================================================*/
/* Table: IX_ANNOTATION                                         */
/*==============================================================*/
create table IX_ANNOTATION 
(
   PID                  NUMBER(10)           not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)           default 1 not null,
   SRC_FLAG             NUMBER(1)            default 0 not null
      constraint CKC_SRC_FLAG_IX_ANNOTATION check (SRC_FLAG in (0,1,2,3,4,5)) disable ,
   SRC_PID              NUMBER(10)           default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)           default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_ANNOTATION check (EDIT_FLAG in (0,1)) disable ,
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_IX_ANNOTION check (DETAIL_FLAG in (0,1,2,3)) disable ,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_ANNOTATION check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION primary key (PID)
);

comment on table IX_ANNOTATION is
'在导航设备上显示自然地形名,地名,道路名,建筑物名称等的数据';

comment on column IX_ANNOTATION.PID is
'主键';

comment on column IX_ANNOTATION.KIND_CODE is
'参考"IX_ANNOTATION_CODE"';

comment on column IX_ANNOTATION.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_ANNOTATION.RANK is
'采用32bit 表示,从右到左依次为0~31bit,每bit 表示
一个显示等级(如下),赋值为0/1 分别表示无效/有效,
如:00000111 表示文字在1,2,4 万等级上均可显示
第0bit:1 万
第1bit:2 万
第2bit:4 万
第3bit:8 万
第4bit:16 万
第5bit:32 万
第6bit:64 万
第7bit:128 万
第8bit:256 万
第9bit:512 万
第10bit:1024 万
第11bit:2048 万
第12bit:4096 万
第13bit:8192 万
注:
(1)2.5 万数据:1~8 万
(2)20 万数据:16~32 万
(3)百万数据:64~512
(4)TOP 级数据:1024~8192 万';

comment on column IX_ANNOTATION.SRC_FLAG is
'注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.SRC_PID is
'文字来源的数据ID,如来自POI则为PO的PID;来自道路名则为道路名ID
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.CLIENT_FLAG is
'根据不同客户需求,输出不同文字,值域包括:
MB 三菱
HD 广本
TY 丰田
PI 先锋
PA 松下
NE NavEx
G MIFG
注:
(1)以上每一代码表示只输出给某一客户,如只给三菱,表示为"MB"
(2)如果表示输出给除某一客户外的其他客户,则在以上代码前加英文半角"-",如输出给除三菱外的客户,则表示为"-MB"
(3)多个之间以英文半角"|"分隔,如表示输出给三菱而不给丰田,则表示为"MB|-TY"
(4)默认为空,表示所有客户都输出
(5)该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.SPECTIAL_FLAG is
'[170]采用32bit 表示,从右到左依次为0~31bit,每bit 表示一个类型(如下),赋值为0/1 分别表示否/是
第0bit:3DICON
第1bit:在水中
所有bit 为均为0,表示无特殊标识
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_ANNOTATION.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_ANNOTATION.MODIFY_FLAG is
'记录修改方式如新增,改名称,改等级,改位移,删除等';

comment on column IX_ANNOTATION.FIELD_MODIFY_FLAG is
'[170]记录修改方式如新增,改名称,改等级,改位移,删除等';

comment on column IX_ANNOTATION.EXTRACT_INFO is
'(1)存放"版本+从索引中提取"
(2)来自Address 字段
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.EXTRACT_PRIORITY is
'提取的优先级别(城区为A1~A11;县乡为B2~B5)
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.REMARK is
'转数据时,来自page字段,内容如:"显示坐标"和"引导坐标"';

comment on column IX_ANNOTATION.DETAIL_FLAG is
'注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION.TASK_ID is
'[170]记录内业的任务编号';

comment on column IX_ANNOTATION.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column IX_ANNOTATION.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column IX_ANNOTATION.U_RECORD is
'增量更新标识';

comment on column IX_ANNOTATION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_ANNOTATION_100W                                    */
/*==============================================================*/
create table IX_ANNOTATION_100W 
(
   PID                  NUMBER(10)           not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)           default 1 not null,
   SRC_FLAG             NUMBER(1)            default 0 not null
      constraint CKC_SRC_FLAG_IX_ANNOTATION100W check (SRC_FLAG in (0,1,2,3,4,5)) disable ,
   SRC_PID              NUMBER(10)           default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)           default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_ANNOTATION100 check (EDIT_FLAG in (0,1)) disable ,
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_IX_AN_100W check (DETAIL_FLAG in (0,1,2,3)) disable ,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_ANNOTATION100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION_100W primary key (PID)
);

comment on table IX_ANNOTATION_100W is
'在导航设备上显示自然地形名,地名,道路名,建筑物名称等的数据';

comment on column IX_ANNOTATION_100W.PID is
'主键';

comment on column IX_ANNOTATION_100W.KIND_CODE is
'参考"IX_ANNOTATION_CODE"';

comment on column IX_ANNOTATION_100W.GEOMETRY is
'默认情况下,文字坐标为POI的显示坐标;根据文字的压盖情况,可能需要调整文字的点位';

comment on column IX_ANNOTATION_100W.RANK is
'采用32bit 表示,从右到左依次为0~31bit,每bit 表示
一个显示等级(如下),赋值为0/1 分别表示无效/有效,
如:00000111 表示文字在1,2,4 万等级上均可显示
第0bit:1 万
第1bit:2 万
第2bit:4 万
第3bit:8 万
第4bit:16 万
第5bit:32 万
第6bit:64 万
第7bit:128 万
第8bit:256 万
第9bit:512 万
第10bit:1024 万
第11bit:2048 万
第12bit:4096 万
第13bit:8192 万
注:
(1)2.5 万数据:1~8 万
(2)20 万数据:16~32 万
(3)百万数据:64~512
(4)TOP 级数据:1024~8192 万';

comment on column IX_ANNOTATION_100W.SRC_FLAG is
'注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.SRC_PID is
'文字来源的数据ID,如来自POI则为PO的PID;来自道路名则为道路名ID
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.CLIENT_FLAG is
'根据不同客户需求,输出不同文字,值域包括:
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
(4)默认为空,表示所有客户都输出
(5)该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.SPECTIAL_FLAG is
'[170]采用32bit 表示,从右到左依次为0~31bit,每bit 表示一个类型(如下),赋值为0/1 分别表示否/是
第0bit:3DICON
第1bit:在水中
所有bit 为均为0,表示无特殊标识
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_ANNOTATION_100W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_ANNOTATION_100W.MODIFY_FLAG is
'记录修改方式如新增,改名称,改等级,改位移,删除等';

comment on column IX_ANNOTATION_100W.FIELD_MODIFY_FLAG is
'[170]记录修改方式如新增,改名称,改等级,改位移,删除等';

comment on column IX_ANNOTATION_100W.EXTRACT_INFO is
'(1)存放"版本+从索引中提取"
(2)来自Address 字段
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.EXTRACT_PRIORITY is
'提取的优先级别(城区为A1~A11;县乡为B2~B5)
注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.REMARK is
'转数据时,来自page字段,内容如:"显示坐标"和"引导坐标"';

comment on column IX_ANNOTATION_100W.DETAIL_FLAG is
'注:该字段仅用于2.5 和20 万数据,百万和TOP 级数据不需要';

comment on column IX_ANNOTATION_100W.TASK_ID is
'[170]记录内业的任务编号';

comment on column IX_ANNOTATION_100W.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column IX_ANNOTATION_100W.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column IX_ANNOTATION_100W.U_RECORD is
'增量更新标识';

comment on column IX_ANNOTATION_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_ANNOTATION_CODE                                    */
/*==============================================================*/
create table IX_ANNOTATION_CODE 
(
   KIND_CODE            VARCHAR2(8),
   NAME_CHI             VARCHAR2(200),
   CLASS_NAME           VARCHAR2(200),
   SUB_CLASS_NAME       VARCHAR2(200),
   "LEVEL"              VARCHAR2(200),
   MHM_DES              VARCHAR2(8),
   ICON_NAME            VARCHAR2(100),
   DESCRIPT             VARCHAR2(200)
);

comment on table IX_ANNOTATION_CODE is
'文字的分类代码,参见附录"文字分类表"';

comment on column IX_ANNOTATION_CODE."LEVEL" is
'[180A]值域包括:
代码	名称
0	2.5万
1	20万
2	百万
T	TOP级
多比例尺时采用英文半角”|”分隔';

comment on column IX_ANNOTATION_CODE.MHM_DES is
'[180A]值域包括:
D	大陆
H	香港
M	澳门
HM	港澳
默认为空,表示全国,含大陆和港澳';

comment on column IX_ANNOTATION_CODE.ICON_NAME is
'[170]';

/*==============================================================*/
/* Table: IX_ANNOTATION_FLAG                                    */
/*==============================================================*/
create table IX_ANNOTATION_FLAG 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_ANNOT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXANNOTATION_FLAG foreign key (PID)
         references IX_ANNOTATION (PID) disable
);

comment on table IX_ANNOTATION_FLAG is
'[170]';

comment on column IX_ANNOTATION_FLAG.PID is
'外键,引用"IX_ANNOTATION"';

comment on column IX_ANNOTATION_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column IX_ANNOTATION_FLAG.U_RECORD is
'增量更新标识';

comment on column IX_ANNOTATION_FLAG.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_ANNOTATION_FLAG_100W                               */
/*==============================================================*/
create table IX_ANNOTATION_FLAG_100W 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_AN_FLAG_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXANNOTATION_FLAG_100W foreign key (PID)
         references IX_ANNOTATION_100W (PID) disable
);

comment on table IX_ANNOTATION_FLAG_100W is
'[171A]记录英文文字的来源信息';

comment on column IX_ANNOTATION_FLAG_100W.PID is
'外键,引用"IX_ANNOTATION_100W"';

comment on column IX_ANNOTATION_FLAG_100W.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column IX_ANNOTATION_FLAG_100W.U_RECORD is
'增量更新标识';

comment on column IX_ANNOTATION_FLAG_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_ANNOTATION_NAME                                    */
/*==============================================================*/
create table IX_ANNOTATION_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_IX_ANNOTATION_NA check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_IX_ANNOTATION check (NAME_CLASS in (1,2)) disable ,
   OLD_NAME             VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_ANNOTATION_N check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION_NAME primary key (NAME_ID),
   constraint IXANNOTATION_NAME foreign key (PID)
         references IX_ANNOTATION (PID) disable
);

comment on column IX_ANNOTATION_NAME.NAME_ID is
'[170]主键';

comment on column IX_ANNOTATION_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column IX_ANNOTATION_NAME.PID is
'外键,引用"IX_ANNOTATION"';

comment on column IX_ANNOTATION_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column IX_ANNOTATION_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column IX_ANNOTATION_NAME.U_RECORD is
'增量更新标识';

comment on column IX_ANNOTATION_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_ANNOTATION_NAME_100W                               */
/*==============================================================*/
create table IX_ANNOTATION_NAME_100W 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_IX_AN_NAME_100W check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_IX_ANNOT check (NAME_CLASS in (1,2)) disable ,
   OLD_NAME             VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_AN_NAME_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION_NAME_100W primary key (NAME_ID),
   constraint IXANNOTATION_NAME_100W foreign key (PID)
         references IX_ANNOTATION_100W (PID) disable
);

comment on column IX_ANNOTATION_NAME_100W.NAME_ID is
'[170]主键';

comment on column IX_ANNOTATION_NAME_100W.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column IX_ANNOTATION_NAME_100W.PID is
'外键,引用"IX_ANNOTATION_100W"';

comment on column IX_ANNOTATION_NAME_100W.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column IX_ANNOTATION_NAME_100W.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column IX_ANNOTATION_NAME_100W.NAME_CLASS is
'[171U]';

comment on column IX_ANNOTATION_NAME_100W.U_RECORD is
'增量更新标识';

comment on column IX_ANNOTATION_NAME_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_CHAIN_CODE                                         */
/*==============================================================*/
create table IX_CHAIN_CODE 
(
   CHAIN_CODE           VARCHAR2(8)          not null,
   CHAIN_NAME           VARCHAR2(200),
   DESCRIPT             VARCHAR2(200),
   constraint PK_IX_CHAIN_CODE primary key (CHAIN_CODE)
);

comment on table IX_CHAIN_CODE is
'连锁店的分类代码,参见附录"连锁店分类表"';

comment on column IX_CHAIN_CODE.CHAIN_CODE is
'主键';

comment on column IX_CHAIN_CODE.U_RECORD is
'增量更新标识';

comment on column IX_CHAIN_CODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_CROSSPOINT                                         */
/*==============================================================*/
create table IX_CROSSPOINT 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_IX_CROSS check (TYPE in (0,1)) disable ,
   NAME_FIR             VARCHAR2(60),
   PINYIN_FIR           VARCHAR2(1000),
   NAME_ENG_FIR         VARCHAR2(200),
   NAME_SEC             VARCHAR2(60),
   PINYIN_SEC           VARCHAR2(1000),
   NAME_ENG_SEC         VARCHAR2(200),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_CROSS check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_CROSS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_CROSSPOINT primary key (PID)
);

comment on table IX_CROSSPOINT is
'交叉点索引记录两条相互交叉的道路及其交叉点位';

comment on column IX_CROSSPOINT.PID is
'主键';

comment on column IX_CROSSPOINT.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_CROSSPOINT.TYPE is
'平交或立交';

comment on column IX_CROSSPOINT.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_CROSSPOINT.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_CROSSPOINT.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_CROSSPOINT.U_RECORD is
'增量更新标识';

comment on column IX_CROSSPOINT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_HAMLET                                             */
/*==============================================================*/
create table IX_HAMLET 
(
   PID                  NUMBER(10)           not null,
   KIND_CODE            VARCHAR2(8)         
      constraint CKC_KIND_CODE_IX_HAMLE check (KIND_CODE is null or (KIND_CODE in ('BB80','BB83','BB84'))) disable ,
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

/*==============================================================*/
/* Table: IX_IC                                                 */
/*==============================================================*/
create table IX_IC 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_FLAG_IX_IC check (ROAD_FLAG in (0,1,2,3)) disable ,
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   KIND_CODE            VARCHAR2(8)         
      constraint CKC_KIND_CODE_IX_IC check (KIND_CODE is null or (KIND_CODE in ('8300','8301','8302'))) disable ,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_IC check (EDIT_FLAG in (0,1)) disable ,
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_IC check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_IC primary key (PID)
);

comment on column IX_IC.PID is
'主键';

comment on column IX_IC.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_IC.NAME_GROUPID is
'[170]参考"RD_NAME"';

comment on column IX_IC.ROAD_FLAG is
'[170]';

comment on column IX_IC.KIND_CODE is
'[170]';

comment on column IX_IC.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_IC.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_IC.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_IC.U_RECORD is
'增量更新标识';

comment on column IX_IC.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_KEYWORD                                            */
/*==============================================================*/
create table IX_KEYWORD 
(
   PID                  NUMBER(10)           not null,
   KEYWORD              VARCHAR2(32),
   PINYIN               VARCHAR2(254),
   PRIORITY             NUMBER(1)            default 0 not null,
   ADMIN_AREA           VARCHAR2(6),
   constraint PK_IX_KEYWORD primary key (PID)
);

comment on table IX_KEYWORD is
'关键字功能是通过输入POI完整名称拼音的一部分进行模糊检索';

comment on column IX_KEYWORD.PID is
'主键';

comment on column IX_KEYWORD.ADMIN_AREA is
'存储为空,两位或四位数字,NULL表示该关键词总适用,两位表示该关键词在省内适用,四位表示该关键词在市内适用';

comment on column IX_KEYWORD.U_RECORD is
'增量更新标识';

comment on column IX_KEYWORD.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_KEYWORD_KIND                                       */
/*==============================================================*/
create table IX_KEYWORD_KIND 
(
   PID                  NUMBER(10)           not null,
   KIND                 VARCHAR2(32),
   constraint IXKEYWORD_KIND foreign key (PID)
         references IX_KEYWORD (PID) disable
);

comment on column IX_KEYWORD_KIND.PID is
'外键,引用"IX_KEYWORD"';

comment on column IX_KEYWORD_KIND.U_RECORD is
'增量更新标识';

comment on column IX_KEYWORD_KIND.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POINTADDRESS                                       */
/*==============================================================*/
create table IX_POINTADDRESS 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   GUIDE_LINK_PID       NUMBER(10)           default 0 not null,
   GUIDE_NAME_GROUPID   NUMBER(10)           default 0 not null,
   LOCATE_LINK_PID      NUMBER(10)           default 0 not null,
   LOCATE_NAME_GROUPID  NUMBER(10)           default 0 not null,
   GUIDE_LINK_SIDE      NUMBER(1)            default 0 not null
      constraint CKC_GUIDE_LINK_SIDE_IX_POINT check (GUIDE_LINK_SIDE in (0,1,2,3)) disable ,
   LOCATE_LINK_SIDE     NUMBER(1)            default 0 not null
      constraint CKC_LOCATE_LINK_SIDE_IX_POINT check (LOCATE_LINK_SIDE in (0,1,2,3)) disable ,
   REGION_ID            NUMBER(10)           default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_POINT check (EDIT_FLAG in (0,1)) disable ,
   IDCODE               VARCHAR2(32),
   DPR_NAME             VARCHAR2(100),
   DP_NAME              VARCHAR2(35),
   OPERATOR             VARCHAR2(32),
   MEMOIRE              VARCHAR2(200),
   DPF_NAME             VARCHAR2(500),
   POSTER_ID            VARCHAR2(100),
   ADDRESS_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_ADDRESS_FLAG_IX_POINT check (ADDRESS_FLAG in (0,1,2)) disable ,
   VERIFED              VARCHAR2(1)          default 'F' not null
      constraint CKC_VERIFED_IX_POINT check (VERIFED in ('T','F')) disable ,
   LOG                  VARCHAR2(32),
   MEMO                 VARCHAR2(32),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POINTADDRESS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STAT_IXPADDR check (STATE in (0,1,2,3)) disable ,
   constraint PK_IX_POINTADDRESS primary key (PID)
);

comment on table IX_POINTADDRESS is
'门牌号码是由地名主管部门按照一定规则编制,用来定位建筑物所在位置的标牌,包括门牌(附号牌),楼(栋)牌,单元牌,户号牌等.';

comment on column IX_POINTADDRESS.PID is
'主键';

comment on column IX_POINTADDRESS.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_POINTADDRESS.GUIDE_LINK_PID is
'参考"RD_LINK"';

comment on column IX_POINTADDRESS.GUIDE_NAME_GROUPID is
'参考"RD_NAME"';

comment on column IX_POINTADDRESS.LOCATE_LINK_PID is
'参考"RD_LINK"';

comment on column IX_POINTADDRESS.LOCATE_NAME_GROUPID is
'参考"RD_NAME"';

comment on column IX_POINTADDRESS.GUIDE_LINK_SIDE is
'[171A]';

comment on column IX_POINTADDRESS.LOCATE_LINK_SIDE is
'[171A]';

comment on column IX_POINTADDRESS.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_POINTADDRESS.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_POINTADDRESS.DPR_NAME is
'外业采集道路名';

comment on column IX_POINTADDRESS.DP_NAME is
'外业采集门牌号';

comment on column IX_POINTADDRESS.OPERATOR is
'外业的OPERATOR字段中的内容原样转入';

comment on column IX_POINTADDRESS.MEMOIRE is
'标注信息(导入外业LABEL)';

comment on column IX_POINTADDRESS.DPF_NAME is
'[170]';

comment on column IX_POINTADDRESS.POSTER_ID is
'[170]邮递员编号';

comment on column IX_POINTADDRESS.ADDRESS_FLAG is
'[171U]点门牌的地址确认标识';

comment on column IX_POINTADDRESS.LOG is
'运行拆分程序后产生的字段';

comment on column IX_POINTADDRESS.TASK_ID is
'[170]记录内业的任务编号';

comment on column IX_POINTADDRESS.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column IX_POINTADDRESS.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column IX_POINTADDRESS.U_RECORD is
'增量更新标识';

comment on column IX_POINTADDRESS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

comment on column IX_POINTADDRESS.STATE is
'[173sp2]';

/*==============================================================*/
/* Table: IX_POINTADDRESS_PARENT                                */
/*==============================================================*/
create table IX_POINTADDRESS_PARENT 
(
   GROUP_ID             NUMBER(10)           not null,
   PARENT_PA_PID        NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POINTADDRESS_P check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POINTADDRESS_PARENT primary key (GROUP_ID),
   constraint IXPOINTADDRESS_PARENT foreign key (PARENT_PA_PID)
         references IX_POINTADDRESS (PID) disable
);

comment on table IX_POINTADDRESS_PARENT is
'[170]';

comment on column IX_POINTADDRESS_PARENT.GROUP_ID is
'主键';

comment on column IX_POINTADDRESS_PARENT.PARENT_PA_PID is
'外键,引用"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_PARENT.U_RECORD is
'增量更新标识';

comment on column IX_POINTADDRESS_PARENT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POINTADDRESS_CHILDREN                              */
/*==============================================================*/
create table IX_POINTADDRESS_CHILDREN 
(
   GROUP_ID             NUMBER(10)           not null,
   CHILD_PA_PID         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POINTADDRESS_C check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINTADDRESS_PARENT_CHILD foreign key (GROUP_ID)
         references IX_POINTADDRESS_PARENT (GROUP_ID) disable ,
   constraint IXPOINTADDRESS_CHILDREN foreign key (CHILD_PA_PID)
         references IX_POINTADDRESS (PID) disable
);

comment on table IX_POINTADDRESS_CHILDREN is
'[170]';

comment on column IX_POINTADDRESS_CHILDREN.GROUP_ID is
'外键,引用"IX_POINTADDRESS_PARENT"';

comment on column IX_POINTADDRESS_CHILDREN.CHILD_PA_PID is
'外键,引用"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_CHILDREN.U_RECORD is
'增量更新标识';

comment on column IX_POINTADDRESS_CHILDREN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POINTADDRESS_FLAG                                  */
/*==============================================================*/
create table IX_POINTADDRESS_FLAG 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POINT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINTADDRESS_FLAG foreign key (PID)
         references IX_POINTADDRESS (PID) disable
);

comment on table IX_POINTADDRESS_FLAG is
'[170]';

comment on column IX_POINTADDRESS_FLAG.PID is
'外键,引用"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column IX_POINTADDRESS_FLAG.U_RECORD is
'增量更新标识';

comment on column IX_POINTADDRESS_FLAG.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POINTADDRESS_NAME                                  */
/*==============================================================*/
create table IX_POINTADDRESS_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_IX_POINT check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')) disable ,
   SUM_CHAR             NUMBER(1)            default 0 not null
      constraint CKC_SUM_CHAR_IX_POINT check (SUM_CHAR in (0,1,2,3)) disable ,
   SPLIT_FLAG           VARCHAR2(1000),
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
   STREET               VARCHAR2(100),
   PLACE                VARCHAR2(100),
   LANDMARK             VARCHAR2(100),
   PREFIX               VARCHAR2(64),
   HOUSENUM             VARCHAR2(64),
   TYPE                 VARCHAR2(32),
   SUBNUM               VARCHAR2(64),
   SURFIX               VARCHAR2(64),
   ESTAB                VARCHAR2(64),
   BUILDING             VARCHAR2(100),
   UNIT                 VARCHAR2(64),
   FLOOR                VARCHAR2(64),
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
      constraint CKC_U_RECORD_IX_POINTADDRESS_N check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POINTADDRESS_NAME primary key (NAME_ID),
   constraint IXPOINTADDRESS_NAME foreign key (PID)
         references IX_POINTADDRESS (PID) disable
);

comment on column IX_POINTADDRESS_NAME.NAME_ID is
'[170]主键';

comment on column IX_POINTADDRESS_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column IX_POINTADDRESS_NAME.PID is
'外键,引用"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column IX_POINTADDRESS_NAME.SUM_CHAR is
'点门牌的号码特征,如连续,奇偶,混合';

comment on column IX_POINTADDRESS_NAME.SPLIT_FLAG is
'[173sp2]';

comment on column IX_POINTADDRESS_NAME.FULLNAME is
'[170]记录拆分前的全地址名称';

comment on column IX_POINTADDRESS_NAME.FULLNAME_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.ROADNAME is
'[173sp1]';

comment on column IX_POINTADDRESS_NAME.ROADNAME_PHONETIC is
'[173sp1]';

comment on column IX_POINTADDRESS_NAME.ADDRNAME is
'[173sp1]';

comment on column IX_POINTADDRESS_NAME.ADDRNAME_PHONETIC is
'[173sp1]';

comment on column IX_POINTADDRESS_NAME.PROVINCE is
'[170]标牌中"省名/直辖市/自治区/特别行政区名"';

comment on column IX_POINTADDRESS_NAME.CITY is
'[170]标牌中"地级市名/自治洲名"';

comment on column IX_POINTADDRESS_NAME.COUNTY is
'[170]标牌中"县级市名/县名/区名(含直辖市的区)"';

comment on column IX_POINTADDRESS_NAME.TOWN is
'[170]乡镇街道办名称';

comment on column IX_POINTADDRESS_NAME.STREET is
'[170]街道,道路名, 胡同,巷,条,弄';

comment on column IX_POINTADDRESS_NAME.PLACE is
'[170]自然村落,居民小区,区域地名,开发区名';

comment on column IX_POINTADDRESS_NAME.LANDMARK is
'[170]指有地理表示作用的店铺,公共设施,单位,建筑或交通运输设施,包括桥梁,公路环岛,交通站场等';

comment on column IX_POINTADDRESS_NAME.PREFIX is
'[170]用于修饰门牌号号码的成分';

comment on column IX_POINTADDRESS_NAME.HOUSENUM is
'[170]主门牌号号码,以序号方式命名的弄或条';

comment on column IX_POINTADDRESS_NAME.TYPE is
'[170]门牌号号码类型';

comment on column IX_POINTADDRESS_NAME.SUBNUM is
'[170]主门牌号所属的子门牌号及修饰该子门牌的前缀信息';

comment on column IX_POINTADDRESS_NAME.SURFIX is
'[170]用于修饰门牌地址的词语,其本身没有实际意义,不影响门牌地址的含义,如:自编,临时';

comment on column IX_POINTADDRESS_NAME.ESTAB is
'[170]如"**大厦","**小区"';

comment on column IX_POINTADDRESS_NAME.BUILDING is
'[170]如"A栋,12栋,31楼,B座"等';

comment on column IX_POINTADDRESS_NAME.UNIT is
'[170]如"2门"';

comment on column IX_POINTADDRESS_NAME.FLOOR is
'[170]如"12层"';

comment on column IX_POINTADDRESS_NAME.ROOM is
'[170]如"503室"';

comment on column IX_POINTADDRESS_NAME.ADDONS is
'[171U][170]如"对面,旁边,附近"';

comment on column IX_POINTADDRESS_NAME.PROV_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.CITY_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.COUNTY_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.TOWN_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.STREET_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.PLACE_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.LANDMARK_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.PREFIX_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.HOUSENUM_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.TYPE_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.SUBNUM_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.SURFIX_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.ESTAB_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.BUILDING_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.FLOOR_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.UNIT_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.ROOM_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.ADDONS_PHONETIC is
'[171U][170]';

comment on column IX_POINTADDRESS_NAME.U_RECORD is
'增量更新标识';

comment on column IX_POINTADDRESS_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POINTADDRESS_NAME_TONE                             */
/*==============================================================*/
create table IX_POINTADDRESS_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   JYUTP                VARCHAR2(400),
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   PA_JYUTP             VARCHAR2(400),
   PA_TONE_A            VARCHAR2(400),
   PA_TONE_B            VARCHAR2(400),
   PA_LH_A              VARCHAR2(400),
   PA_LH_B              VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POINTADDRESS_T check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINTADDRESSNAME_TONE foreign key (NAME_ID)
         references IX_POINTADDRESS_NAME (NAME_ID) disable
);

comment on table IX_POINTADDRESS_NAME_TONE is
'[170]';

comment on column IX_POINTADDRESS_NAME_TONE.NAME_ID is
'外键,引用"IX_POINTADDRESS_NAME"';

comment on column IX_POINTADDRESS_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column IX_POINTADDRESS_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column IX_POINTADDRESS_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column IX_POINTADDRESS_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column IX_POINTADDRESS_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column IX_POINTADDRESS_NAME_TONE.PA_JYUTP is
'制作普通话时本字段为空值';

comment on column IX_POINTADDRESS_NAME_TONE.PA_TONE_A is
'对应于对应于门牌号拼音,数字不转拼音';

comment on column IX_POINTADDRESS_NAME_TONE.PA_TONE_B is
'对应于门牌号的拼音,数字转拼音';

comment on column IX_POINTADDRESS_NAME_TONE.PA_LH_A is
'对应门牌号声调拼音1,转出LH+';

comment on column IX_POINTADDRESS_NAME_TONE.PA_LH_B is
'对应门牌号声调拼音2,转出LH+';

comment on column IX_POINTADDRESS_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column IX_POINTADDRESS_NAME_TONE.U_FIELDS is
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
   STREET               VARCHAR2(100),
   PLACE                VARCHAR2(100),
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

/*==============================================================*/
/* Table: IX_POSTCODE                                           */
/*==============================================================*/
create table IX_POSTCODE 
(
   POST_ID              NUMBER(10)           not null,
   POST_CODE            VARCHAR2(6),
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_POSTC check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_POSTC check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POSTCODE primary key (POST_ID)
);

comment on table IX_POSTCODE is
'邮编是实现邮件机器分拣的邮政通信专用代号,目的是提高信件在传递过程中的速度和准确性';

comment on column IX_POSTCODE.POST_ID is
'主键';

comment on column IX_POSTCODE.POST_CODE is
'存储为6位英文半角数字';

comment on column IX_POSTCODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_POSTCODE.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_POSTCODE.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_POSTCODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_POSTCODE.U_RECORD is
'增量更新标识';

comment on column IX_POSTCODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_ROADNAME                                           */
/*==============================================================*/
create table IX_ROADNAME 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_ROADN check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_ROADN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ROADNAME primary key (PID)
);

comment on column IX_ROADNAME.PID is
'主键';

comment on column IX_ROADNAME.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_ROADNAME.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_ROADNAME.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_ROADNAME.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_ROADNAME.U_RECORD is
'增量更新标识';

comment on column IX_ROADNAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_SAMEPOI                                            */
/*==============================================================*/
create table IX_SAMEPOI  (
   GROUP_ID             NUMBER(10)                      not null,
   RELATION_TYPE        NUMBER(1)                      default 1 not null
      constraint CKC_RELATION_TYPE_IX_SAMEP check (RELATION_TYPE in (1,2,3)) disable ,
   U_RECORD             NUMBER(2)                      default 0 not null
      constraint CKC_U_RECORD_IX_SAMEPOI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_SAMEPOI primary key (GROUP_ID)
);

comment on column IX_SAMEPOI.GROUP_ID is
'主键';

comment on column IX_SAMEPOI.U_RECORD is
'增量更新标识';

comment on column IX_SAMEPOI.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';


/*==============================================================*/
/* Table: IX_SAMEPOI_PART                                       */
/*==============================================================*/
create table IX_SAMEPOI_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_SAMEPOI_PART check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_GROUP foreign key (GROUP_ID)
         references IX_SAMEPOI (GROUP_ID) disable ,
   constraint IXPOI_SAMEPOIPART foreign key (POI_PID)
         references IX_POI (PID) disable
);

comment on column IX_SAMEPOI_PART.GROUP_ID is
'外键,引用"IX_SAMEPOI"';

comment on column IX_SAMEPOI_PART.POI_PID is
'外键,引用"IX_POI"';

comment on column IX_SAMEPOI_PART.U_RECORD is
'增量更新标识';

comment on column IX_SAMEPOI_PART.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_TOLLGATE                                           */
/*==============================================================*/
create table IX_TOLLGATE 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_FLAG_IX_TOLLG check (ROAD_FLAG in (0,1,2,3)) disable ,
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   NAME_POR             VARCHAR2(200),
   KIND_CODE            VARCHAR2(8)         
      constraint CKC_KIND_CODE_IX_TOLLG check (KIND_CODE is null or (KIND_CODE in ('8401'))) disable ,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_IX_TOLLG check (EDIT_FLAG in (0,1)) disable ,
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_IX_TOLLG check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_TOLLGATE primary key (PID)
);

comment on column IX_TOLLGATE.PID is
'主键';

comment on column IX_TOLLGATE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column IX_TOLLGATE.NAME_GROUPID is
'[170]参考"RD_NAME"';

comment on column IX_TOLLGATE.ROAD_FLAG is
'[170]';

comment on column IX_TOLLGATE.NAME_POR is
'[170]';

comment on column IX_TOLLGATE.KIND_CODE is
'[180U]';

comment on column IX_TOLLGATE.MESH_ID_5K is
'记录索引所在的5000图幅号,格式为:605603_1_3';

comment on column IX_TOLLGATE.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column IX_TOLLGATE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column IX_TOLLGATE.U_RECORD is
'增量更新标识';

comment on column IX_TOLLGATE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE                                               */
/*==============================================================*/
create table LC_FACE 
(
   FACE_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_FACE check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_FACE check (FORM in (0,1,2,3,4,8,9)) disable ,
   DISPLAY_CLASS        NUMBER(1)            default 0 not null
      constraint CKC_DISPLAY_CLASS_LC_FACE check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_LC_FACE check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_LC_FACE check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_FACE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE primary key (FACE_PID)
);

comment on column LC_FACE.FACE_PID is
'主键';

comment on column LC_FACE.FEATURE_PID is
'参考"LC_FEATURE"';

comment on column LC_FACE.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column LC_FACE.KIND is
'注:
(1)2.5 万和20 万数据:0~6,11~17
(2)百万和TOP 级数据:0~6,17';

comment on column LC_FACE.FORM is
'注:
(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_FACE.DISPLAY_CLASS is
'按照水系的面积从大到小分为1~8 级
注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE.AREA is
'单位:平方米';

comment on column LC_FACE.PERIMETER is
'单位:米';

comment on column LC_FACE.SCALE is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE.DETAIL_FLAG is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_FACE.U_RECORD is
'增量更新标识';

comment on column LC_FACE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_100W                                          */
/*==============================================================*/
create table LC_FACE_100W 
(
   FACE_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_FACE_100W check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_FACE_100W check (FORM in (0,1,2,3,4,8,9)) disable ,
   DISPLAY_CLASS        NUMBER(1)            default 0 not null
      constraint CKC_DISPLAY_CLASS_LC_FACE_100W check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_LC_FACE_100W check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_LC_FACE_100W check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_FACE_100W check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_100W primary key (FACE_PID)
);

comment on column LC_FACE_100W.FACE_PID is
'主键';

comment on column LC_FACE_100W.FEATURE_PID is
'参考"LC_FEATURE_100W"';

comment on column LC_FACE_100W.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column LC_FACE_100W.KIND is
'注:
(1)2.5 万和20 万数据:0~6,11~17
(2)百万和TOP 级数据:0~6,17';

comment on column LC_FACE_100W.FORM is
'注:
(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_FACE_100W.DISPLAY_CLASS is
'按照水系的面积从大到小分为1~8 级
注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_100W.AREA is
'单位:平方米';

comment on column LC_FACE_100W.PERIMETER is
'单位:米';

comment on column LC_FACE_100W.SCALE is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_100W.DETAIL_FLAG is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_100W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_FACE_100W.U_RECORD is
'增量更新标识';

comment on column LC_FACE_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_20W                                           */
/*==============================================================*/
create table LC_FACE_20W 
(
   FACE_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_FACE_20W check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_FACE_20W check (FORM in (0,1,2,3,4,8,9)) disable ,
   DISPLAY_CLASS        NUMBER(1)            default 0 not null
      constraint CKC_DISPLAY_CLASS_LC_FACE_20W check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_LC_FACE_20W check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_LC_FACE_20W check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_FACE_20W check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_20W primary key (FACE_PID)
);

comment on column LC_FACE_20W.FACE_PID is
'主键';

comment on column LC_FACE_20W.FEATURE_PID is
'参考"LC_FEATURE_20W"';

comment on column LC_FACE_20W.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column LC_FACE_20W.KIND is
'注:
(1)2.5 万和20 万数据:0~6,11~17
(2)百万和TOP 级数据:0~6,17';

comment on column LC_FACE_20W.FORM is
'注:
(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_FACE_20W.DISPLAY_CLASS is
'按照水系的面积从大到小分为1~8 级
注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_20W.AREA is
'单位:平方米';

comment on column LC_FACE_20W.PERIMETER is
'单位:米';

comment on column LC_FACE_20W.SCALE is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_20W.DETAIL_FLAG is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_20W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_FACE_20W.U_RECORD is
'增量更新标识';

comment on column LC_FACE_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_NAME                                          */
/*==============================================================*/
create table LC_FACE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_LC_FACE_NAME check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_LC_FACE_NAME check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME primary key (NAME_ID),
   constraint LCFACE_NAME foreign key (FACE_PID)
         references LC_FACE (FACE_PID) disable
);

comment on column LC_FACE_NAME.NAME_ID is
'[170]主键';

comment on column LC_FACE_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column LC_FACE_NAME.FACE_PID is
'外键,引用"LC_FACE"';

comment on column LC_FACE_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column LC_FACE_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column LC_FACE_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column LC_FACE_NAME.U_RECORD is
'增量更新标识';

comment on column LC_FACE_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_NAME_100W                                     */
/*==============================================================*/
create table LC_FACE_NAME_100W 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_LC_FACE_NAME100W check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_LC_FACE_NAME_100W check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_NAME_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME_100W primary key (NAME_ID),
   constraint LCFACE_NAME_100W foreign key (FACE_PID)
         references LC_FACE_100W (FACE_PID) disable
);

comment on table LC_FACE_NAME_100W is
'[170]';

comment on column LC_FACE_NAME_100W.NAME_ID is
'[170]主键';

comment on column LC_FACE_NAME_100W.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column LC_FACE_NAME_100W.FACE_PID is
'外键,引用"LC_FACE_100W"';

comment on column LC_FACE_NAME_100W.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column LC_FACE_NAME_100W.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column LC_FACE_NAME_100W.SRC_FLAG is
'现仅指英文名来源';

comment on column LC_FACE_NAME_100W.U_RECORD is
'增量更新标识';

comment on column LC_FACE_NAME_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_NAME_20W                                      */
/*==============================================================*/
create table LC_FACE_NAME_20W 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_LC_FACE_NAME_20W check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_LC_FACE_NAME_20W check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_NAME_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME_20W primary key (NAME_ID),
   constraint LCFACE_NAME_20W foreign key (FACE_PID)
         references LC_FACE_20W (FACE_PID) disable
);

comment on table LC_FACE_NAME_20W is
'[170]';

comment on column LC_FACE_NAME_20W.NAME_ID is
'主键';

comment on column LC_FACE_NAME_20W.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column LC_FACE_NAME_20W.FACE_PID is
'外键,引用"LC_FACE_20W"';

comment on column LC_FACE_NAME_20W.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column LC_FACE_NAME_20W.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column LC_FACE_NAME_20W.SRC_FLAG is
'现仅指英文名来源';

comment on column LC_FACE_NAME_20W.U_RECORD is
'增量更新标识';

comment on column LC_FACE_NAME_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_TOP                                           */
/*==============================================================*/
create table LC_FACE_TOP 
(
   FACE_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_FACE_TOP check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_FACE_TOP check (FORM in (0,1,2,3,4,8,9)) disable ,
   DISPLAY_CLASS        NUMBER(1)            default 0 not null
      constraint CKC_DISPLAY_CLASS_LC_FACE_TOP check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_LC_FACE_TOP check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_LC_FACE_TOP check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_FACE_TOP check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_TOP primary key (FACE_PID)
);

comment on table LC_FACE_TOP is
'[170]';

comment on column LC_FACE_TOP.FACE_PID is
'主键';

comment on column LC_FACE_TOP.FEATURE_PID is
'参考"LC_FEATURE_TOP"';

comment on column LC_FACE_TOP.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column LC_FACE_TOP.KIND is
'注:
(1)2.5 万和20 万数据:0~6,11~17
(2)百万和TOP 级数据:0~6,17';

comment on column LC_FACE_TOP.FORM is
'注:
(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_FACE_TOP.DISPLAY_CLASS is
'按照水系的面积从大到小分为1~8 级
注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_TOP.AREA is
'单位:平方米';

comment on column LC_FACE_TOP.PERIMETER is
'单位:米';

comment on column LC_FACE_TOP.SCALE is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_TOP.DETAIL_FLAG is
'注:该字段仅用于2.5 万数据,20 万,百万和TOP 级数据不需要';

comment on column LC_FACE_TOP.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_FACE_TOP.U_RECORD is
'增量更新标识';

comment on column LC_FACE_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_NAME_TOP                                      */
/*==============================================================*/
create table LC_FACE_NAME_TOP 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_LC_FACE_TOP check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_LC_FACE_NAME_TOP check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_NAME_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME_TOP primary key (NAME_ID),
   constraint LCFACE_NAME_TOP foreign key (FACE_PID)
         references LC_FACE_TOP (FACE_PID) disable
);

comment on table LC_FACE_NAME_TOP is
'[170]';

comment on column LC_FACE_NAME_TOP.NAME_ID is
'主键';

comment on column LC_FACE_NAME_TOP.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column LC_FACE_NAME_TOP.FACE_PID is
'外键,引用"LC_FACE_TOP"';

comment on column LC_FACE_NAME_TOP.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column LC_FACE_NAME_TOP.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column LC_FACE_NAME_TOP.SRC_FLAG is
'现仅指英文名来源';

comment on column LC_FACE_NAME_TOP.U_RECORD is
'增量更新标识';

comment on column LC_FACE_NAME_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE                                               */
/*==============================================================*/
create table LC_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_LC_NODE check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_NODE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE primary key (NODE_PID)
);

comment on column LC_NODE.NODE_PID is
'主键';

comment on column LC_NODE.FORM is
'图廓点,角点';

comment on column LC_NODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点
';

comment on column LC_NODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_NODE.U_RECORD is
'增量更新标识';

comment on column LC_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK                                               */
/*==============================================================*/
create table LC_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_LINK check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK primary key (LINK_PID),
   constraint LCLINK_SNODE foreign key (S_NODE_PID)
         references LC_NODE (NODE_PID) disable ,
   constraint LCLINK_ENODE foreign key (E_NODE_PID)
         references LC_NODE (NODE_PID) disable
);

comment on column LC_LINK.LINK_PID is
'主键';

comment on column LC_LINK.S_NODE_PID is
'外键,引用"LC_NODE"';

comment on column LC_LINK.E_NODE_PID is
'外键,引用"LC_NODE"';

comment on column LC_LINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column LC_LINK.LENGTH is
'单位:米';

comment on column LC_LINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_LINK.U_RECORD is
'增量更新标识';

comment on column LC_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_TOPO                                          */
/*==============================================================*/
create table LC_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_TOPO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK foreign key (LINK_PID)
         references LC_LINK (LINK_PID) disable ,
   constraint LCFACE_LINKS foreign key (FACE_PID)
         references LC_FACE (FACE_PID) disable
);

comment on table LC_FACE_TOPO is
'记录面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column LC_FACE_TOPO.FACE_PID is
'外键,引用"LC_FACE"';

comment on column LC_FACE_TOPO.LINK_PID is
'外键,引用"LC_LINK"';

comment on column LC_FACE_TOPO.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column LC_FACE_TOPO.U_RECORD is
'增量更新标识';

comment on column LC_FACE_TOPO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_100W                                          */
/*==============================================================*/
create table LC_NODE_100W 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_LC_NODE_100W check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_NODE_100W check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE_100W primary key (NODE_PID)
);

comment on column LC_NODE_100W.NODE_PID is
'主键';

comment on column LC_NODE_100W.FORM is
'图廓点,角点';

comment on column LC_NODE_100W.GEOMETRY is
'存储以"度"为单位的经纬度坐标点
';

comment on column LC_NODE_100W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_NODE_100W.U_RECORD is
'增量更新标识';

comment on column LC_NODE_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_100W                                          */
/*==============================================================*/
create table LC_LINK_100W 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_LINK_100W check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK_100W primary key (LINK_PID),
   constraint LCLINK_SNODE_100W foreign key (S_NODE_PID)
         references LC_NODE_100W (NODE_PID) disable ,
   constraint LCLINK_ENODE_100W foreign key (E_NODE_PID)
         references LC_NODE_100W (NODE_PID) disable
);

comment on column LC_LINK_100W.LINK_PID is
'主键';

comment on column LC_LINK_100W.S_NODE_PID is
'外键,引用"LC_NODE_100W"';

comment on column LC_LINK_100W.E_NODE_PID is
'外键,引用"LC_NODE_100W"';

comment on column LC_LINK_100W.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column LC_LINK_100W.LENGTH is
'单位:米';

comment on column LC_LINK_100W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_LINK_100W.U_RECORD is
'增量更新标识';

comment on column LC_LINK_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_TOPO_100W                                     */
/*==============================================================*/
create table LC_FACE_TOPO_100W 
(
   FACE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_TOPO_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID) disable ,
   constraint LCFACE_LINKS_100W foreign key (FACE_PID)
         references LC_FACE_100W (FACE_PID) disable
);

comment on table LC_FACE_TOPO_100W is
'记录面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column LC_FACE_TOPO_100W.FACE_PID is
'外键,引用"LC_FACE_100W"';

comment on column LC_FACE_TOPO_100W.LINK_PID is
'外键,引用"LC_LINK_100W"';

comment on column LC_FACE_TOPO_100W.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column LC_FACE_TOPO_100W.U_RECORD is
'增量更新标识';

comment on column LC_FACE_TOPO_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_20W                                           */
/*==============================================================*/
create table LC_NODE_20W 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_LC_NODE_20W check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_NODE_20W check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE_20W primary key (NODE_PID)
);

comment on column LC_NODE_20W.NODE_PID is
'主键';

comment on column LC_NODE_20W.FORM is
'图廓点,角点';

comment on column LC_NODE_20W.GEOMETRY is
'存储以"度"为单位的经纬度坐标点
';

comment on column LC_NODE_20W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_NODE_20W.U_RECORD is
'增量更新标识';

comment on column LC_NODE_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_20W                                           */
/*==============================================================*/
create table LC_LINK_20W 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_LINK_20W check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK_20W primary key (LINK_PID),
   constraint LCLINK_SNODE_20W foreign key (S_NODE_PID)
         references LC_NODE_20W (NODE_PID) disable ,
   constraint LCLINK_ENODE_20W foreign key (E_NODE_PID)
         references LC_NODE_20W (NODE_PID) disable
);

comment on column LC_LINK_20W.LINK_PID is
'主键';

comment on column LC_LINK_20W.S_NODE_PID is
'外键,引用"LC_NODE_20W"';

comment on column LC_LINK_20W.E_NODE_PID is
'外键,引用"LC_NODE_20W"';

comment on column LC_LINK_20W.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column LC_LINK_20W.LENGTH is
'单位:米';

comment on column LC_LINK_20W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_LINK_20W.U_RECORD is
'增量更新标识';

comment on column LC_LINK_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_TOPO_20W                                      */
/*==============================================================*/
create table LC_FACE_TOPO_20W 
(
   LINK_PID             NUMBER(10)           not null,
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_TOPO_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID) disable ,
   constraint LCFACE_LINKS_20W foreign key (FACE_PID)
         references LC_FACE_20W (FACE_PID) disable
);

comment on table LC_FACE_TOPO_20W is
'记录面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column LC_FACE_TOPO_20W.LINK_PID is
'外键,引用"LC_LINK_20W"';

comment on column LC_FACE_TOPO_20W.FACE_PID is
'外键,引用"LC_FACE_20W"';

comment on column LC_FACE_TOPO_20W.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column LC_FACE_TOPO_20W.U_RECORD is
'增量更新标识';

comment on column LC_FACE_TOPO_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_TOP                                           */
/*==============================================================*/
create table LC_NODE_TOP 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_LC_NODE_TOP check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_NODE_TOP check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE_TOP primary key (NODE_PID)
);

comment on table LC_NODE_TOP is
'[170]';

comment on column LC_NODE_TOP.NODE_PID is
'主键';

comment on column LC_NODE_TOP.FORM is
'图廓点,角点';

comment on column LC_NODE_TOP.GEOMETRY is
'存储以"度"为单位的经纬度坐标点
';

comment on column LC_NODE_TOP.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_NODE_TOP.U_RECORD is
'增量更新标识';

comment on column LC_NODE_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_TOP                                           */
/*==============================================================*/
create table LC_LINK_TOP 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LC_LINK_TOP check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK_TOP primary key (LINK_PID),
   constraint LCLINK_SNODE_TOP foreign key (S_NODE_PID)
         references LC_NODE_TOP (NODE_PID) disable ,
   constraint LCLINK_ENODE_TOP foreign key (E_NODE_PID)
         references LC_NODE_TOP (NODE_PID) disable
);

comment on table LC_LINK_TOP is
'[170]';

comment on column LC_LINK_TOP.LINK_PID is
'主键';

comment on column LC_LINK_TOP.S_NODE_PID is
'外键,引用"LC_NODE_TOP"';

comment on column LC_LINK_TOP.E_NODE_PID is
'外键,引用"LC_NODE_TOP"';

comment on column LC_LINK_TOP.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column LC_LINK_TOP.LENGTH is
'单位:米';

comment on column LC_LINK_TOP.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LC_LINK_TOP.U_RECORD is
'增量更新标识';

comment on column LC_LINK_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FACE_TOPO_TOP                                      */
/*==============================================================*/
create table LC_FACE_TOPO_TOP 
(
   FACE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FACE_TOPO_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID) disable ,
   constraint LCFACE_LINKS_TOP foreign key (FACE_PID)
         references LC_FACE_TOP (FACE_PID) disable
);

comment on table LC_FACE_TOPO_TOP is
'[170]记录面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column LC_FACE_TOPO_TOP.FACE_PID is
'外键,引用"LC_FACE_TOP"';

comment on column LC_FACE_TOPO_TOP.LINK_PID is
'外键,引用"LC_LINK_TOP"';

comment on column LC_FACE_TOPO_TOP.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column LC_FACE_TOPO_TOP.U_RECORD is
'增量更新标识';

comment on column LC_FACE_TOPO_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FEATURE                                            */
/*==============================================================*/
create table LC_FEATURE 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FEATURE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE primary key (FEATURE_PID)
);

comment on table LC_FEATURE is
'土地覆盖要素,包括水系(海域,河川域,湖沼池,水库,港湾,运河),绿地(树林林地,草地,绿化带,公园,岛屿等),与NavEx中的Carto对应';

comment on column LC_FEATURE.FEATURE_PID is
'主键';

comment on column LC_FEATURE.U_RECORD is
'增量更新标识';

comment on column LC_FEATURE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FEATURE_100W                                       */
/*==============================================================*/
create table LC_FEATURE_100W 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FEATURE_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE_100W primary key (FEATURE_PID)
);

comment on table LC_FEATURE_100W is
'土地覆盖要素,包括水系(海域,河川域,湖沼池,水库,港湾,运河),绿地(树林林地,草地,绿化带,公园,岛屿等),与NavEx中的Carto对应';

comment on column LC_FEATURE_100W.FEATURE_PID is
'主键';

comment on column LC_FEATURE_100W.U_RECORD is
'增量更新标识';

comment on column LC_FEATURE_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FEATURE_20W                                        */
/*==============================================================*/
create table LC_FEATURE_20W 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FEATURE_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE_20W primary key (FEATURE_PID)
);

comment on table LC_FEATURE_20W is
'土地覆盖要素,包括水系(海域,河川域,湖沼池,水库,港湾,运河),绿地(树林林地,草地,绿化带,公园,岛屿等),与NavEx中的Carto对应';

comment on column LC_FEATURE_20W.FEATURE_PID is
'主键';

comment on column LC_FEATURE_20W.U_RECORD is
'增量更新标识';

comment on column LC_FEATURE_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_FEATURE_TOP                                        */
/*==============================================================*/
create table LC_FEATURE_TOP 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_FEATURE_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE_TOP primary key (FEATURE_PID)
);

comment on table LC_FEATURE_TOP is
'[170]土地覆盖要素,包括水系(海域,河川域,湖沼池,水库,港湾,运河),绿地(树林林地,草地,绿化带,公园,岛屿等),与NavEx中的Carto对应';

comment on column LC_FEATURE_TOP.FEATURE_PID is
'主键';

comment on column LC_FEATURE_TOP.U_RECORD is
'增量更新标识';

comment on column LC_FEATURE_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_KIND                                          */
/*==============================================================*/
create table LC_LINK_KIND 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_LINK_KIND check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_LINK_KIND check (FORM in (0,1,2,3,4,8,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_KIND check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND foreign key (LINK_PID)
         references LC_LINK (LINK_PID) disable
);

comment on table LC_LINK_KIND is
'记录Link的水系,绿地种别';

comment on column LC_LINK_KIND.LINK_PID is
'外键,引用"LC_LINK"';

comment on column LC_LINK_KIND.KIND is
'注:
(1)2.5 万和20 万数据:0~8,11~18
(2)百万和TOP 级数据:0~6,8,17';

comment on column LC_LINK_KIND.FORM is
'(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_LINK_KIND.U_RECORD is
'增量更新标识';

comment on column LC_LINK_KIND.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_KIND_100W                                     */
/*==============================================================*/
create table LC_LINK_KIND_100W 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_LINK_KIND_100W check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_LINK_KIND_100W check (FORM in (0,1,2,3,4,8,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_KIND_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID) disable
);

comment on table LC_LINK_KIND_100W is
'记录Link的水系,绿地种别';

comment on column LC_LINK_KIND_100W.LINK_PID is
'外键,引用"LC_LINK_100W"';

comment on column LC_LINK_KIND_100W.KIND is
'注:
(1)2.5 万和20 万数据:0~8,11~18
(2)百万和TOP 级数据:0~6,8,17';

comment on column LC_LINK_KIND_100W.FORM is
'(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_LINK_KIND_100W.U_RECORD is
'增量更新标识';

comment on column LC_LINK_KIND_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_KIND_20W                                      */
/*==============================================================*/
create table LC_LINK_KIND_20W 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_LINK_KIND_20W check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_LINK_KIND_20W check (FORM in (0,1,2,3,4,8,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_KIND_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID) disable
);

comment on table LC_LINK_KIND_20W is
'记录Link的水系,绿地种别';

comment on column LC_LINK_KIND_20W.LINK_PID is
'外键,引用"LC_LINK_20W"';

comment on column LC_LINK_KIND_20W.KIND is
'注:
(1)2.5 万和20 万数据:0~8,11~18
(2)百万和TOP 级数据:0~6,8,17';

comment on column LC_LINK_KIND_20W.FORM is
'(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_LINK_KIND_20W.U_RECORD is
'增量更新标识';

comment on column LC_LINK_KIND_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_KIND_TOP                                      */
/*==============================================================*/
create table LC_LINK_KIND_TOP 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LC_LINK_KIND_TOP check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
      constraint CKC_FORM_LC_LINK_TOP check (FORM in (0,1,2,3,4,8,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_KIND_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID) disable
);

comment on table LC_LINK_KIND_TOP is
'[170]记录Link的水系,绿地种别';

comment on column LC_LINK_KIND_TOP.LINK_PID is
'外键,引用"LC_LINK_TOP"';

comment on column LC_LINK_KIND_TOP.KIND is
'注:
(1)2.5 万和20 万数据:0~8,11~18
(2)百万和TOP 级数据:0~6,8,17';

comment on column LC_LINK_KIND_TOP.FORM is
'(1)2.5 万和20 万数据:0~4
(2)百万和TOP 级数据:0~4,8,9
(3)当KIND=17 时,取值1~4,当KIND=3 时,取值8,9,其他取值0';

comment on column LC_LINK_KIND_TOP.U_RECORD is
'增量更新标识';

comment on column LC_LINK_KIND_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_MESH                                          */
/*==============================================================*/
create table LC_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH foreign key (LINK_PID)
         references LC_LINK (LINK_PID) disable
);

comment on column LC_LINK_MESH.LINK_PID is
'外键,引用"LC_LINK"';

comment on column LC_LINK_MESH.U_RECORD is
'增量更新标识';

comment on column LC_LINK_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_MESH_100W                                     */
/*==============================================================*/
create table LC_LINK_MESH_100W 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_MESH_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID) disable
);

comment on column LC_LINK_MESH_100W.LINK_PID is
'外键,引用"LC_LINK_100W"';

comment on column LC_LINK_MESH_100W.U_RECORD is
'增量更新标识';

comment on column LC_LINK_MESH_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_MESH_20W                                      */
/*==============================================================*/
create table LC_LINK_MESH_20W 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_MESH_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID) disable
);

comment on column LC_LINK_MESH_20W.LINK_PID is
'外键,引用"LC_LINK_20W"';

comment on column LC_LINK_MESH_20W.U_RECORD is
'增量更新标识';

comment on column LC_LINK_MESH_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_LINK_MESH_TOP                                      */
/*==============================================================*/
create table LC_LINK_MESH_TOP 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_LINK_MESH_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID) disable
);

comment on column LC_LINK_MESH_TOP.LINK_PID is
'外键,引用"LC_LINK_TOP"';

comment on column LC_LINK_MESH_TOP.U_RECORD is
'增量更新标识';

comment on column LC_LINK_MESH_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_MESH                                          */
/*==============================================================*/
create table LC_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH foreign key (NODE_PID)
         references LC_NODE (NODE_PID) disable
);

comment on column LC_NODE_MESH.NODE_PID is
'外键,引用"LC_NODE"';

comment on column LC_NODE_MESH.U_RECORD is
'增量更新标识';

comment on column LC_NODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_MESH_100W                                     */
/*==============================================================*/
create table LC_NODE_MESH_100W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_MESH_100W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH_100W foreign key (NODE_PID)
         references LC_NODE_100W (NODE_PID) disable
);

comment on column LC_NODE_MESH_100W.NODE_PID is
'外键,引用"LC_NODE_100W"';

comment on column LC_NODE_MESH_100W.U_RECORD is
'增量更新标识';

comment on column LC_NODE_MESH_100W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_MESH_20W                                      */
/*==============================================================*/
create table LC_NODE_MESH_20W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_MESH_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH_20W foreign key (NODE_PID)
         references LC_NODE_20W (NODE_PID) disable
);

comment on column LC_NODE_MESH_20W.NODE_PID is
'外键,引用"LC_NODE_20W"';

comment on column LC_NODE_MESH_20W.U_RECORD is
'增量更新标识';

comment on column LC_NODE_MESH_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LC_NODE_MESH_TOP                                      */
/*==============================================================*/
create table LC_NODE_MESH_TOP 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LC_NODE_MESH_TOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH_TOP foreign key (NODE_PID)
         references LC_NODE_TOP (NODE_PID) disable
);

comment on table LC_NODE_MESH_TOP is
'[170]';

comment on column LC_NODE_MESH_TOP.NODE_PID is
'外键,引用"LC_NODE_TOP"';

comment on column LC_NODE_MESH_TOP.U_RECORD is
'增量更新标识';

comment on column LC_NODE_MESH_TOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_FACE                                               */
/*==============================================================*/
create table LU_FACE 
(
   FACE_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LU_FACE check (KIND in (0,1,2,3,4,5,6,7,11,12,21,22,23,24,30,31,32,33,34,35,36,37,38,39)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LU_FACE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_FACE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_FACE primary key (FACE_PID)
);

comment on column LU_FACE.FACE_PID is
'主键';

comment on column LU_FACE.FEATURE_PID is
'参考"LU_FEATURE"';

comment on column LU_FACE.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column LU_FACE.KIND is
'大学,医院,购物中心等';

comment on column LU_FACE.AREA is
'单位:平方米';

comment on column LU_FACE.PERIMETER is
'单位:米';

comment on column LU_FACE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LU_FACE.U_RECORD is
'增量更新标识';

comment on column LU_FACE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_FACE_NAME                                          */
/*==============================================================*/
create table LU_FACE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_LU_FACE_ check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_LU_FACE_NAME check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_FACE_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_FACE_NAME primary key (NAME_ID),
   constraint LUFACE_NAME foreign key (FACE_PID)
         references LU_FACE (FACE_PID) disable
);

comment on column LU_FACE_NAME.NAME_ID is
'[170]主键';

comment on column LU_FACE_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column LU_FACE_NAME.FACE_PID is
'外键,引用"LU_FACE"';

comment on column LU_FACE_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column LU_FACE_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column LU_FACE_NAME.SRC_FLAG is
'[170]现仅指英文名来源
注:
(1)BUA 取值0~1
(2)其他取值0';

comment on column LU_FACE_NAME.U_RECORD is
'增量更新标识';

comment on column LU_FACE_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_NODE                                               */
/*==============================================================*/
create table LU_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_LU_NODE check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LU_NODE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_NODE primary key (NODE_PID)
);

comment on column LU_NODE.NODE_PID is
'主键';

comment on column LU_NODE.FORM is
'图廓点,角点';

comment on column LU_NODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column LU_NODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LU_NODE.U_RECORD is
'增量更新标识';

comment on column LU_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_LINK                                               */
/*==============================================================*/
create table LU_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_LU_LINK check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_LINK primary key (LINK_PID),
   constraint LULINK_NODE foreign key (S_NODE_PID)
         references LU_NODE (NODE_PID) disable ,
   constraint LULINK_ENODE foreign key (E_NODE_PID)
         references LU_NODE (NODE_PID) disable
);

comment on column LU_LINK.LINK_PID is
'主键';

comment on column LU_LINK.S_NODE_PID is
'外键,引用"LU_NODE"';

comment on column LU_LINK.E_NODE_PID is
'外键,引用"LU_NODE"';

comment on column LU_LINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column LU_LINK.LENGTH is
'单位:米';

comment on column LU_LINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column LU_LINK.U_RECORD is
'增量更新标识';

comment on column LU_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_FACE_TOPO                                          */
/*==============================================================*/
create table LU_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_FACE_TOPO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LUFACE_LINK foreign key (LINK_PID)
         references LU_LINK (LINK_PID) disable ,
   constraint LUFACE_LINKS foreign key (FACE_PID)
         references LU_FACE (FACE_PID) disable
);

comment on table LU_FACE_TOPO is
'记录面与Link之间的拓扑关系,按照逆时针方向存储';

comment on column LU_FACE_TOPO.FACE_PID is
'外键,引用"LU_FACE"';

comment on column LU_FACE_TOPO.SEQ_NUM is
'按逆时针方向,从1开始递增编号';

comment on column LU_FACE_TOPO.LINK_PID is
'外键,引用"LU_LINK"';

comment on column LU_FACE_TOPO.U_RECORD is
'增量更新标识';

comment on column LU_FACE_TOPO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_FEATURE                                            */
/*==============================================================*/
create table LU_FEATURE 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_FEATU check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_FEATURE primary key (FEATURE_PID)
);

comment on table LU_FEATURE is
'土地利用要素,包括BUA,功能面(大学,医院,购物中心,体育场,公墓,机场,机场跑道),与NavEx中的Carto对应';

comment on column LU_FEATURE.FEATURE_PID is
'主键';

comment on column LU_FEATURE.U_RECORD is
'增量更新标识';

comment on column LU_FEATURE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_LINK_KIND                                          */
/*==============================================================*/
create table LU_LINK_KIND 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_LU_LINK_ check (KIND in (0,1,2,3,4,5,6,7,8,11,12,21,22,23,24,30,31,32,33,34,35,36,37,38,39)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_LINK_KIND check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LULINK_KIND foreign key (LINK_PID)
         references LU_LINK (LINK_PID) disable
);

comment on table LU_LINK_KIND is
'记录Link的BUA,功能面(大学,购物中心,医院等)等种别';

comment on column LU_LINK_KIND.LINK_PID is
'外键,引用"LU_LINK"';

comment on column LU_LINK_KIND.KIND is
'大学,停车场,购物中心的面边界线等';

comment on column LU_LINK_KIND.U_RECORD is
'增量更新标识';

comment on column LU_LINK_KIND.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_LINK_MESH                                          */
/*==============================================================*/
create table LU_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_LINK_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LULINK_MESH foreign key (LINK_PID)
         references LU_LINK (LINK_PID) disable
);

comment on column LU_LINK_MESH.LINK_PID is
'外键,引用"LU_LINK"';

comment on column LU_LINK_MESH.U_RECORD is
'增量更新标识';

comment on column LU_LINK_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: LU_NODE_MESH                                          */
/*==============================================================*/
create table LU_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_LU_NODE_ check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LUNODE_MESH foreign key (NODE_PID)
         references LU_NODE (NODE_PID) disable
);

comment on column LU_NODE_MESH.NODE_PID is
'外键,引用"LU_NODE"';

comment on column LU_NODE_MESH.U_RECORD is
'增量更新标识';

comment on column LU_NODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';



/*==============================================================*/
/* Table: M_MESH_TYPE                                           */
/*==============================================================*/
create table M_MESH_TYPE 
(
   MESH_ID              NUMBER(6)            not null,
   TYPE                 NUMBER(2)            default 0 not null
      constraint CKC_TYPE_M_MESH_T check (TYPE in (0,1,2)) disable ,
   MEMO                 VARCHAR2(500),
   constraint PK_M_MESH_TYPE primary key (MESH_ID)
);

comment on table M_MESH_TYPE is
'[171A]';

comment on column M_MESH_TYPE.MESH_ID is
'主键';

/*==============================================================*/
/* Table: M_PARAMETER                                           */
/*==============================================================*/
create table M_PARAMETER 
(
   NAME                 VARCHAR2(32),
   PARAMETER            VARCHAR2(32),
   DESCRIPTION          VARCHAR2(200)
);

comment on table M_PARAMETER is
'记录系统参数,包括:模型版本(GLM_VERSION),数据参数(DATA_VERSION),比例尺(MAP_SCALE),坐标系(COORDINATE_SYS),经纬度坐标单位(COORDINATE_UNIT),经纬度坐标精确度(XY_PRECISION),高程坐标精度(Z_PRECISION),区域信息(REGION_INFO)';

comment on column M_PARAMETER.NAME is
'如:GLM_VERSION,DATA_VERSION,MAP_SCALE等';

comment on column M_PARAMETER.PARAMETER is
'如GLM_VERSION=1.6.1,DATA_VERSION=10winter';

/*==============================================================*/
/* Table: M_UPDATE_PARAMETER                                    */
/*==============================================================*/
create table M_UPDATE_PARAMETER 
(
   DB_TYPE              VARCHAR2(100),
   VERSION_TYPE         VARCHAR2(100),
   VERSION_CODE         VARCHAR2(100),
   CREATE_TIME          VARCHAR2(100),
   DB_SOURCE_A          VARCHAR2(100),
   DB_SOURCE_B          VARCHAR2(100),
   CONTENT              VARCHAR2(100),
   DESCRIPT             VARCHAR2(1000),
   AVAILABLE_TYPE       VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_M_UPDATE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000)
);

comment on table M_UPDATE_PARAMETER is
'[170]';

comment on column M_UPDATE_PARAMETER.DB_TYPE is
'描述成果库、产品库、差分库';

comment on column M_UPDATE_PARAMETER.VERSION_TYPE is
'描述差分版还是基础版';

comment on column M_UPDATE_PARAMETER.VERSION_CODE is
'库版本号,如1.0.0;1.1.0 等';

comment on column M_UPDATE_PARAMETER.CREATE_TIME is
'生成库的日期时间';

comment on column M_UPDATE_PARAMETER.DB_SOURCE_A is
'如作业库,成果库版本,原产品库版本';

comment on column M_UPDATE_PARAMETER.DB_SOURCE_B is
'如差分版本有效,新产品库版本';

comment on column M_UPDATE_PARAMETER.CONTENT is
'如全要素,POI,道路,公交等';

comment on column M_UPDATE_PARAMETER.DESCRIPT is
'数据升级内容描述,包括统计数字等,如北京城区更新,道路200km,POI 共12000 个';

comment on column M_UPDATE_PARAMETER.AVAILABLE_TYPE is
'网络,导航,GIS 等';

comment on column M_UPDATE_PARAMETER.U_RECORD is
'增量更新标识';

comment on column M_UPDATE_PARAMETER.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_COMPANY                                            */
/*==============================================================*/
create table PT_COMPANY 
(
   COMPANY_ID           NUMBER(10)           not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_PT_COMPA check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_COMPA check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_COMPANYID       VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_COMPA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_COMPANY primary key (COMPANY_ID)
);

comment on table PT_COMPANY is
'公交公司是指负责公交线路和系统运营的公司,即管理公交系统的上级单位';

comment on column PT_COMPANY.COMPANY_ID is
'主键';

comment on column PT_COMPANY.PHONETIC is
'[171U]';

comment on column PT_COMPANY.SRC_FLAG is
'[170]';

comment on column PT_COMPANY.CITY_CODE is
'存储长度为4位';

comment on column PT_COMPANY.DATA_SOURCE is
'[170]';

comment on column PT_COMPANY.UPDATE_BATCH is
'[170]';

comment on column PT_COMPANY.U_RECORD is
'增量更新标识';

comment on column PT_COMPANY.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_POI                                                */
/*==============================================================*/
create table PT_POI 
(
   PID                  NUMBER(10)           not null,
   POI_KIND             VARCHAR2(4),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)         default 0 not null,
   Y_GUIDE              NUMBER(10,5)         default 0 not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_PT_POI check (SIDE in (0,1,2,3)) disable ,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            VARCHAR2(1)          default '0' not null
      constraint CKC_ROAD_FLAG_PT_POI check (ROAD_FLAG in ('0','1','2','3')) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   ACCESS_CODE          VARCHAR2(32),
   ACCESS_TYPE          VARCHAR2(10)         default '0' not null
      constraint CKC_ACCESS_TYPE_PT_POI check (ACCESS_TYPE in ('0','1','2','3')) disable ,
   ACCESS_METH          NUMBER(3)            default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_PT_POI check (EDIT_FLAG in (0,1)) disable ,
   POI_MEMO             VARCHAR2(200),
   OPERATOR             VARCHAR2(30),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_POI check (STATE in (0,1,2,3)) disable ,
   POI_NUM              VARCHAR2(100),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_POI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_POI primary key (PID)
);

comment on table PT_POI is
'公交POI由主点(Stop POI)和出入口(Access POI)两部分组成,每个主点对应一到多个出入口,出入口是主点的子POI.';

comment on column PT_POI.PID is
'主键';

comment on column PT_POI.POI_KIND is
'参考"IX_POI_CODE"';

comment on column PT_POI.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column PT_POI.LINK_PID is
'参考"RD_LINK"';

comment on column PT_POI.SIDE is
'根据POI的显示坐标计算POI位于引导道路Link上或左侧或右侧(相对于Link方向)';

comment on column PT_POI.NAME_GROUPID is
'[173sp2]参考"RD_NAME"';

comment on column PT_POI.ROAD_FLAG is
'[170]';

comment on column PT_POI.PMESH_ID is
'[171A]每个作业季POI 在成果库中第一次与LINK 建关联时生成,且该作业季内重新建关联时该图幅号不变,以保证该作业季每次数据分省转出的一致性';

comment on column PT_POI.ACCESS_CODE is
'[170]出入口名称中的顺序号或编号,如:”安定门A 口”,编号是” A”;”少年宫站Ａ２口”,编号是” Ａ２”;”世界之窗站１号口”,编号是” １”.出入口名称中没有编号的值为空';

comment on column PT_POI.ACCESS_TYPE is
'出入,入口,出入口';

comment on column PT_POI.ACCESS_METH is
'采用8bit 表示,从右到左依次为0~7bit,每bit 表示一种方式类型(如下),赋值为0/1 分别表示无/有,如:00000011 表示斜坡和阶梯;00000101 表示斜坡和扶梯
第0bit:斜坡
第1bit:阶梯
第2bit:扶梯
第3bit:直梯
第4bit:其他
如果所有bit 位均为0,表示不应用';

comment on column PT_POI.MESH_ID_5K is
'记录公交POI所在的5000图幅号,格式为:605603_1_3';

comment on column PT_POI.REGION_ID is
'[170]参考"AD_ADMIN",通过区划号码找对应的行政代码和乡镇号码';

comment on column PT_POI.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column PT_POI.POI_NUM is
'[173sp1]记录来自NIDB的POI编号';

comment on column PT_POI.TASK_ID is
'[170]记录内业的任务编号';

comment on column PT_POI.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column PT_POI.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column PT_POI.U_RECORD is
'增量更新标识';

comment on column PT_POI.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_ETA_ACCESS                                         */
/*==============================================================*/
create table PT_ETA_ACCESS 
(
   POI_PID              NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   OPEN_PERIOD          VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)          default '0' not null
      constraint CKC_MANUAL_TICKET_PT_ETA_A check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
      constraint CKC_AUTO_TICKET_PT_ETA_A check (AUTO_TICKET in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_ETA_A check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_ETA_A check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_ACCESS foreign key (POI_PID)
         references PT_POI (PID) disable
);

comment on table PT_ETA_ACCESS is
'描述现实世界公共出入口所具备的功能及其周边的附属服务设施等';

comment on column PT_ETA_ACCESS.POI_PID is
'外键,引用"PT_POI"';

comment on column PT_ETA_ACCESS.OPEN_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column PT_ETA_ACCESS.MANUAL_TICKET_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点有人看守,不记录日期';

comment on column PT_ETA_ACCESS.U_RECORD is
'增量更新标识';

comment on column PT_ETA_ACCESS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_ETA_COMPANY                                        */
/*==============================================================*/
create table PT_ETA_COMPANY 
(
   COMPANY_ID           NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   TEL_TYPE             VARCHAR2(32),
   TELEPHONE            VARCHAR2(500),
   URL_TYPE             VARCHAR2(32),
   URL                  VARCHAR2(500),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_ETA_C check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_ETA_C check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_COMPANY foreign key (COMPANY_ID)
         references PT_COMPANY (COMPANY_ID) disable
);

comment on table PT_ETA_COMPANY is
'公交公司深度信息:电话,网址,图像等
电话和网址用来表达可以访问该公交公司的联络信息;图像用来表达公交公司的LOGO图标或路线图.';

comment on column PT_ETA_COMPANY.COMPANY_ID is
'外键,引用"PT_COMPANY"';

comment on column PT_ETA_COMPANY.TEL_TYPE is
'[172U]值域包括:
0 未调查
1 总机
2 日程班次信息
3 服务信息';

comment on column PT_ETA_COMPANY.TELEPHONE is
'格式为:"区号-号码",如010-82306399.
多个号码时采用英文半角”|”分隔,并与电话类型一一对应';

comment on column PT_ETA_COMPANY.URL_TYPE is
'[172U]值域包括:
0 未调查
1 路径规划
2 日程班次信息
3 地图服务
4 主页';

comment on column PT_ETA_COMPANY.URL is
'格式:http://xxxxxxxxx,如http://www.navinfo.com/.
多个网址时采用英文半角”|”分隔,并与网址类型一一对应';

comment on column PT_ETA_COMPANY.U_RECORD is
'增量更新标识';

comment on column PT_ETA_COMPANY.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_LINE                                               */
/*==============================================================*/
create table PT_LINE 
(
   PID                  NUMBER(10)           not null,
   SYSTEM_ID            NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TYPE                 NUMBER(2)            default 11 not null
      constraint CKC_TYPE_PT_LINE check (TYPE in (11,12,13,14,15,16,17,21,31,32,33,41,42,51,52,53,54,61)) disable ,
   COLOR                VARCHAR2(10),
   NIDB_LINEID          VARCHAR2(32),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_LINE check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_LINE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_LINE primary key (PID)
);

comment on table PT_LINE is
'记录某地区的公交线路信息,比如 656路,特4路等';

comment on column PT_LINE.PID is
'主键';

comment on column PT_LINE.SYSTEM_ID is
'参考"PT_SYSTEM"';

comment on column PT_LINE.CITY_CODE is
'与行政区划代码没有直接关系,由生产部门维护';

comment on column PT_LINE.COLOR is
'存储16进制的RGB值';

comment on column PT_LINE.LOG is
'[173sp1]';

comment on column PT_LINE.DATA_SOURCE is
'[170]';

comment on column PT_LINE.UPDATE_BATCH is
'[170]';

comment on column PT_LINE.U_RECORD is
'增量更新标识';

comment on column PT_LINE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_ETA_LINE                                           */
/*==============================================================*/
create table PT_ETA_LINE 
(
   PID                  NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   BIKE                 VARCHAR2(1)          default '0' not null
      constraint CKC_BIKE_PT_ETA_L check (BIKE in ('0','1','2')) disable ,
   BIKE_PERIOD          VARCHAR2(200),
   IMAGE                VARCHAR2(20),
   RACK                 VARCHAR2(1)          default '0' not null
      constraint CKC_RACK_PT_ETA_L check (RACK in ('0','1','2')) disable ,
   DINNER               VARCHAR2(1)          default '0' not null
      constraint CKC_DINNER_PT_ETA_L check (DINNER in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
      constraint CKC_TOILET_PT_ETA_L check (TOILET in ('0','1','2')) disable ,
   SLEEPER              VARCHAR2(1)          default '0' not null
      constraint CKC_SLEEPER_PT_ETA_L check (SLEEPER in ('0','1','2')) disable ,
   WHEEL_CHAIR          VARCHAR2(1)          default '0' not null
      constraint CKC_WHEEL_CHAIR_PT_ETA_L check (WHEEL_CHAIR in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
      constraint CKC_SMOKE_PT_ETA_L check (SMOKE in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_ETA_L check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_ETA_L check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_LINE foreign key (PID)
         references PT_LINE (PID) disable
);

comment on table PT_ETA_LINE is
'描述现实世界公共交通线路中具备的功能,如允许自行车,行李架,用餐服务等';

comment on column PT_ETA_LINE.PID is
'外键,引用"PT_LINE"';

comment on column PT_ETA_LINE.BIKE_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column PT_ETA_LINE.U_RECORD is
'增量更新标识';

comment on column PT_ETA_LINE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_ETA_STOP                                           */
/*==============================================================*/
create table PT_ETA_STOP 
(
   POI_PID              NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   PRIVATE_PARK         VARCHAR2(1)          default '0' not null
      constraint CKC_PRIVATE_PARK_PT_ETA_S check (PRIVATE_PARK in ('0','1','2','3')) disable ,
   PRIVATE_PARK_PERIOD  VARCHAR2(200),
   CARPORT_EXACT        VARCHAR2(32),
   CARPORT_ESTIMATE     VARCHAR2(1)          default '0' not null
      constraint CKC_CARPORT_ESTIMATE_PT_ETA_S check (CARPORT_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   BIKE_PARK            VARCHAR2(1)          default '0' not null
      constraint CKC_BIKE_PARK_PT_ETA_S check (BIKE_PARK in ('0','1','2','3')) disable ,
   BIKE_PARK_PERIOD     VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)          default '0' not null
      constraint CKC_MANUAL_TICKET_PT_ETA_S check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   MOBILE               VARCHAR2(1)          default '0' not null
      constraint CKC_MOBILE_PT_ETA_S check (MOBILE in ('0','1','2')) disable ,
   BAGGAGE_SECURITY     VARCHAR2(1)          default '0' not null
      constraint CKC_BAGGAGE_SECURITY_PT_ETA_S check (BAGGAGE_SECURITY in ('0','1','2')) disable ,
   LEFT_BAGGAGE         VARCHAR2(1)          default '0' not null
      constraint CKC_LEFT_BAGGAGE_PT_ETA_S check (LEFT_BAGGAGE in ('0','1','2')) disable ,
   CONSIGNATION_EXACT   VARCHAR2(32),
   CONSIGNATION_ESTIMATE VARCHAR2(1)          default '0' not null
      constraint CKC_CONSIGNATION_ESTI_PT_ETA_S check (CONSIGNATION_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   CONVENIENT           VARCHAR2(1)          default '0' not null
      constraint CKC_CONVENIENT_PT_ETA_S check (CONVENIENT in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
      constraint CKC_SMOKE_PT_ETA_S check (SMOKE in ('0','1','2')) disable ,
   BUILD_TYPE           VARCHAR2(1)          default '0' not null
      constraint CKC_BUILD_TYPE_PT_ETA_S check (BUILD_TYPE in ('0','1','2','3')) disable ,
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
      constraint CKC_AUTO_TICKET_PT_ETA_S check (AUTO_TICKET in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
      constraint CKC_TOILET_PT_ETA_S check (TOILET in ('0','1','2')) disable ,
   WIFI                 VARCHAR2(1)          default '0' not null
      constraint CKC_WIFI_PT_ETA_S check (WIFI in ('0','1','2')) disable ,
   OPEN_PERIOD          VARCHAR2(200),
   FARE_AREA            VARCHAR2(1),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_ETA_STOP check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_ETA_STOP check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_POI foreign key (POI_PID)
         references PT_POI (PID) disable
);

comment on table PT_ETA_STOP is
'[170]描述现实世界公共交通站点具备的功能及其周边的附属服务设施等';

comment on column PT_ETA_STOP.POI_PID is
'外键,引用"PT_POI"';

comment on column PT_ETA_STOP.PRIVATE_PARK is
'收费或免费';

comment on column PT_ETA_STOP.PRIVATE_PARK_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column PT_ETA_STOP.BIKE_PARK is
'是否有人看守';

comment on column PT_ETA_STOP.BIKE_PARK_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点有人看守,不记录日期';

comment on column PT_ETA_STOP.MANUAL_TICKET_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点有人看守,不记录日期';

comment on column PT_ETA_STOP.OPEN_PERIOD is
'格式为”hh:mm”,只记录每天的几点-几点开放,不记录日期';

comment on column PT_ETA_STOP.FARE_AREA is
'官方线路图的值';

comment on column PT_ETA_STOP.U_RECORD is
'增量更新标识';

comment on column PT_ETA_STOP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_SYSTEM                                             */
/*==============================================================*/
create table PT_SYSTEM 
(
   SYSTEM_ID            NUMBER(10)           not null,
   COMPANY_ID           NUMBER(10)           not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_PT_SYSTE check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_SYSTE check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_SYSTEMID        VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_SYSTE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_SYSTEM primary key (SYSTEM_ID),
   constraint PTSYSTEM_COMPANY foreign key (COMPANY_ID)
         references PT_COMPANY (COMPANY_ID) disable
);

comment on table PT_SYSTEM is
'公交系统是一个公交公司下属负责管理,运营具体公交线路的分支单位.通常是指隶属于同一个系统的公交线路的管理单位,即直接管理,运营巴士/地铁等线路的单位';

comment on column PT_SYSTEM.SYSTEM_ID is
'主键,系统分配唯一顺序号';

comment on column PT_SYSTEM.COMPANY_ID is
'外键,引用"PT_COMPANY"';

comment on column PT_SYSTEM.PHONETIC is
'[171U]';

comment on column PT_SYSTEM.SRC_FLAG is
'[170]';

comment on column PT_SYSTEM.CITY_CODE is
'存储长度为4位';

comment on column PT_SYSTEM.DATA_SOURCE is
'[170]';

comment on column PT_SYSTEM.UPDATE_BATCH is
'[170]';

comment on column PT_SYSTEM.U_RECORD is
'增量更新标识';

comment on column PT_SYSTEM.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_ETA_SYSTEM                                         */
/*==============================================================*/
create table PT_ETA_SYSTEM 
(
   SYSTEM_ID            NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   TEL_TYPE             VARCHAR2(32),
   TELEPHONE            VARCHAR2(500),
   URL_TYPE             VARCHAR2(32),
   URL                  VARCHAR2(500),
   BILL_METHOD          VARCHAR2(1)          default '1' not null
      constraint CKC_BILL_METHOD_PT_ETA_S check (BILL_METHOD in ('0','1','2','3')) disable ,
   COIN                 VARCHAR2(200)        default 'CNY' not null,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_ETA_SYSTEM check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_ETA_SYSTEM check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_SYSTEM foreign key (SYSTEM_ID)
         references PT_SYSTEM (SYSTEM_ID) disable
);

comment on table PT_ETA_SYSTEM is
'主要有电话,网址,图像,计费方式,可用货币等信息';

comment on column PT_ETA_SYSTEM.SYSTEM_ID is
'外键,引用"PT_SYSTEM"';

comment on column PT_ETA_SYSTEM.TEL_TYPE is
'[172U]值域包括:
0 未调查
1 总机
2 日程班次信息
3 服务信息';

comment on column PT_ETA_SYSTEM.TELEPHONE is
'格式为:"区号-号码",如010-82306399.
多个号码时采用英文半角”|”分隔,并与电话类型一一对应';

comment on column PT_ETA_SYSTEM.URL_TYPE is
'[172U]值域包括:
0 未调查
1 路径规划
2 日程班次信息
3 地图服务
4 主页';

comment on column PT_ETA_SYSTEM.URL is
'格式:http://xxxxxxxxx,如http://www.navinfo.com/.
多个网址时采用英文半角”|”分隔,并与网址类型一一对应';

comment on column PT_ETA_SYSTEM.COIN is
'值域为:
CNY 人民币(大陆)
HKD 港元(香港)
MOP 澳门币
多种货币方式用英文半角”|”分隔';

comment on column PT_ETA_SYSTEM.U_RECORD is
'增量更新标识';

comment on column PT_ETA_SYSTEM.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_LINE_NAME                                          */
/*==============================================================*/
create table PT_LINE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_PT_LINE_ check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_PT_LINE_ check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_LINE_ check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_LINE_NAME primary key (NAME_ID),
   constraint PTLINE_NAME foreign key (PID)
         references PT_LINE (PID) disable
);

comment on table PT_LINE_NAME is
'[170]';

comment on column PT_LINE_NAME.NAME_ID is
'主键';

comment on column PT_LINE_NAME.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column PT_LINE_NAME.PID is
'外键,引用"PT_LINE"';

comment on column PT_LINE_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column PT_LINE_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column PT_LINE_NAME.SRC_FLAG is
'现仅指英文名来源';

comment on column PT_LINE_NAME.U_RECORD is
'增量更新标识';

comment on column PT_LINE_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_PLATFORM                                           */
/*==============================================================*/
create table PT_PLATFORM 
(
   PID                  NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   COLLECT              NUMBER(2)            default 0 not null
      constraint CKC_COLLECT_PT_PLATF check (COLLECT in (0,1)) disable ,
   P_LEVEL              NUMBER(2)            default 0 not null
      constraint CKC_P_LEVEL_PT_PLATF check (P_LEVEL in (4,3,2,1,0,-1,-2,-3,-4,-5,-6)) disable ,
   TRANSIT_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_TRANSIT_FLAG_PT_PLATF check (TRANSIT_FLAG in (0,1)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_PLATFORM check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_PLATFORMID      VARCHAR2(32),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_PLATFORM check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_PLATFORM primary key (PID),
   constraint PTPLATFORM_POI foreign key (POI_PID)
         references PT_POI (PID) disable
);

comment on table PT_PLATFORM is
'站台,也叫月台,即公共交通车辆停靠时,供乘客候车和上下车的设施';

comment on column PT_PLATFORM.PID is
'主键';

comment on column PT_PLATFORM.POI_PID is
'外键,引用"PT_POI"';

comment on column PT_PLATFORM.COLLECT is
'[170]';

comment on column PT_PLATFORM.P_LEVEL is
'值域:-6~4;0表示地面';

comment on column PT_PLATFORM.TRANSIT_FLAG is
'记录该站台是否能和其他站台互通,若能和其他站台互通,标识为"可换乘";若不能通往任何其他站台,标识为"不可换乘"';

comment on column PT_PLATFORM.DATA_SOURCE is
'[170]';

comment on column PT_PLATFORM.UPDATE_BATCH is
'[170]';

comment on column PT_PLATFORM.TASK_ID is
'[170]记录内业的任务编号';

comment on column PT_PLATFORM.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column PT_PLATFORM.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column PT_PLATFORM.U_RECORD is
'增量更新标识';

comment on column PT_PLATFORM.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_PLATFORM_ACCESS                                    */
/*==============================================================*/
create table PT_PLATFORM_ACCESS 
(
   RELATE_ID            NUMBER(10)           not null,
   PLATFORM_ID          NUMBER(10)           not null,
   ACCESS_ID            NUMBER(10)           not null,
   AVAILABLE            NUMBER(1)            default 1 not null
      constraint CKC_AVAILABLE_PT_PLATF check (AVAILABLE in (0,1)) disable ,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_PLATFORM_ACCESS check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_PLATFORM_ACCES check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_PLATFORM_ACCESS primary key (RELATE_ID),
   constraint PTPLATFORM_ACCESSES foreign key (PLATFORM_ID)
         references PT_PLATFORM (PID) disable ,
   constraint PTPLATFORM_ACCESS foreign key (ACCESS_ID)
         references PT_POI (PID) disable
);

comment on table PT_PLATFORM_ACCESS is
'记录站台与能到达该站台的入口点之间的对应关系,一个站台可以有多个入口点';

comment on column PT_PLATFORM_ACCESS.RELATE_ID is
'主键';

comment on column PT_PLATFORM_ACCESS.PLATFORM_ID is
'外键,引用"PT_PLATFORM"';

comment on column PT_PLATFORM_ACCESS.ACCESS_ID is
'外键,引用"PT_POI"';

comment on column PT_PLATFORM_ACCESS.U_RECORD is
'增量更新标识';

comment on column PT_PLATFORM_ACCESS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_PLATFORM_NAME                                      */
/*==============================================================*/
create table PT_PLATFORM_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_PT_PLATF check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_PT_PLATF check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_PLATFORM_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_PLATFORM_NAME primary key (NAME_ID),
   constraint PTPLATFORM_NAME foreign key (PID)
         references PT_PLATFORM (PID) disable
);

comment on table PT_PLATFORM_NAME is
'[170]';

comment on column PT_PLATFORM_NAME.NAME_ID is
'主键';

comment on column PT_PLATFORM_NAME.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column PT_PLATFORM_NAME.PID is
'外键,引用"PT_PLATFORM"';

comment on column PT_PLATFORM_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column PT_PLATFORM_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column PT_PLATFORM_NAME.SRC_FLAG is
'现仅指英文名来源';

comment on column PT_PLATFORM_NAME.U_RECORD is
'增量更新标识';

comment on column PT_PLATFORM_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_POI_PARENT                                         */
/*==============================================================*/
create table PT_POI_PARENT 
(
   GROUP_ID             NUMBER(10)           not null,
   PARENT_POI_PID       NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_POI_P check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_POI_PARENT primary key (GROUP_ID),
   constraint PTPOI_PARENT foreign key (PARENT_POI_PID)
         references PT_POI (PID) disable
);

comment on table PT_POI_PARENT is
'记录站点与出入口之间的关系信息';

comment on column PT_POI_PARENT.GROUP_ID is
'主键';

comment on column PT_POI_PARENT.PARENT_POI_PID is
'外键,引用"PT_POI"';

comment on column PT_POI_PARENT.U_RECORD is
'增量更新标识';

comment on column PT_POI_PARENT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_POI_CHILDREN                                       */
/*==============================================================*/
create table PT_POI_CHILDREN 
(
   GROUP_ID             NUMBER(10)           not null,
   CHILD_POI_PID        NUMBER(10)           not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
      constraint CKC_RELATION_TYPE_PT_POI_C check (RELATION_TYPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_POI_C check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTPOI_CHILDREN foreign key (CHILD_POI_PID)
         references PT_POI (PID) disable ,
   constraint PTPOI_CHILDPARENT foreign key (GROUP_ID)
         references PT_POI_PARENT (GROUP_ID) disable
);

comment on table PT_POI_CHILDREN is
'公交POI的父子关系,即站点与出入口之间的关系信息';

comment on column PT_POI_CHILDREN.GROUP_ID is
'外键,引用"PT_POI_PARENT"';

comment on column PT_POI_CHILDREN.CHILD_POI_PID is
'外键,引用"PT_POI"';

comment on column PT_POI_CHILDREN.U_RECORD is
'增量更新标识';

comment on column PT_POI_CHILDREN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_POI_FLAG                                           */
/*==============================================================*/
create table PT_POI_FLAG 
(
   POI_PID              NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_POI_F check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTPOI_FLAG foreign key (POI_PID)
         references PT_POI (PID) disable
);

comment on table PT_POI_FLAG is
'[170]';

comment on column PT_POI_FLAG.POI_PID is
'外键,引用"PT_POI"';

comment on column PT_POI_FLAG.FLAG_CODE is
'参考"M_FLAG_CODE"';

comment on column PT_POI_FLAG.U_RECORD is
'增量更新标识';

comment on column PT_POI_FLAG.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_POI_NAME                                           */
/*==============================================================*/
create table PT_POI_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_PT_POI_N check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_PT_POI_N check (NAME_CLASS in (1,2)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_POI_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_POI_NAME primary key (NAME_ID),
   constraint PTPOI_NAME foreign key (POI_PID)
         references PT_POI (PID) disable
);

comment on table PT_POI_NAME is
'与IX_POI_NAME原则相同';

comment on column PT_POI_NAME.NAME_ID is
'[170]主键';

comment on column PT_POI_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column PT_POI_NAME.POI_PID is
'外键,引用"PT_POI"';

comment on column PT_POI_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column PT_POI_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column PT_POI_NAME.NIDB_PID is
'记录现有POI中已经出品的永久ID,不同语言类型PID不同';

comment on column PT_POI_NAME.U_RECORD is
'增量更新标识';

comment on column PT_POI_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_POI_NAME_TONE                                      */
/*==============================================================*/
create table PT_POI_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_POI_NAME_TONE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTPOINAME_TONE foreign key (NAME_ID)
         references PT_POI_NAME (NAME_ID) disable
);

comment on table PT_POI_NAME_TONE is
'[170]';

comment on column PT_POI_NAME_TONE.NAME_ID is
'外键,引用"PT_POI_NAME"';

comment on column PT_POI_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column PT_POI_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column PT_POI_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column PT_POI_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column PT_POI_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column PT_POI_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column PT_POI_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_STRAND                                             */
/*==============================================================*/
create table PT_STRAND 
(
   PID                  NUMBER(10)           not null,
   PAIR_STRAND_PID      NUMBER(10)           default 0 not null,
   LINE_ID              NUMBER(10)           not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   UP_DOWN              VARCHAR2(16)        
      constraint CKC_UP_DOWN_PT_STRAN check (UP_DOWN is null or (UP_DOWN in ('Ｓ','Ｘ','Ｈ','ＮＨ','ＷＨ','ＣＨ','ＣＮＨ','ＣＷＨ'))) disable ,
   DISTANCE             VARCHAR2(10),
   TICKET_SYS           NUMBER(2)            default 0 not null
      constraint CKC_TICKET_SYS_PT_STRAN check (TICKET_SYS in (0,1,2,9)) disable ,
   TICKET_START         VARCHAR2(255),
   TOTAL_PRICE          VARCHAR2(255),
   INCREASED_PRICE      VARCHAR2(255),
   INCREASED_STEP       VARCHAR2(255),
   GEOMETRY             SDO_GEOMETRY,
   NIDB_STRANDID        VARCHAR2(32),
   MEMO                 VARCHAR2(200),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_AU_PT_STRAND check (STATE in (0,1,2,3)) disable ,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_STRAND check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_STRAND primary key (PID),
   constraint PTSTRAND_LINE foreign key (LINE_ID)
         references PT_LINE (PID) disable
);

comment on table PT_STRAND is
'Strand,即班次表,用来记录每条线路各个行车方向在不同的时间点发出的班次,及该班次的各类详细信息,如经过线路,首末车时间,发车间隔等';

comment on column PT_STRAND.PID is
'主键';

comment on column PT_STRAND.PAIR_STRAND_PID is
'[170]';

comment on column PT_STRAND.LINE_ID is
'外键,引用"PT_LINE"';

comment on column PT_STRAND.UP_DOWN is
'表示上行,下行,环行等,存储为全角字符';

comment on column PT_STRAND.TICKET_START is
'[170]';

comment on column PT_STRAND.TOTAL_PRICE is
'[170]';

comment on column PT_STRAND.INCREASED_PRICE is
'[170]';

comment on column PT_STRAND.INCREASED_STEP is
'[170]';

comment on column PT_STRAND.GEOMETRY is
'(1)Strand 行车轨迹,即几何坐标序列,与图廓线不做打断,坐标序列可自相交
(2)存储以"度"为单位的经纬度坐标序列,WKT 格式';

comment on column PT_STRAND.DATA_SOURCE is
'[170]';

comment on column PT_STRAND.UPDATE_BATCH is
'[170]';

comment on column PT_STRAND.LOG is
'[173sp1]';

comment on column PT_STRAND.TASK_ID is
'[170]记录内业的任务编号';

comment on column PT_STRAND.DATA_VERSION is
'记录数据采集的作业季,如10冬,11夏';

comment on column PT_STRAND.FIELD_TASK_ID is
'记录外业的任务编号';

comment on column PT_STRAND.U_RECORD is
'增量更新标识';

comment on column PT_STRAND.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_RUNTIME                                            */
/*==============================================================*/
create table PT_RUNTIME 
(
   STRAND_PID           NUMBER(10)           not null,
   PLATFORM_PID         NUMBER(10)           not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   ARRIVAL_TIME         VARCHAR2(32),
   DEPART_TIME          VARCHAR2(32),
   APPROX_TIME          NUMBER(1)            default 0 not null
      constraint CKC_APPROX_TIME_PT_RUNTI check (APPROX_TIME in (0,1)) disable ,
   VALID_WEEK           VARCHAR2(7),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_RUNTI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTRUNTIME_STRAND foreign key (STRAND_PID)
         references PT_STRAND (PID) disable ,
   constraint PTRUNTIME_PLATFORM foreign key (PLATFORM_PID)
         references PT_PLATFORM (PID) disable
);

comment on table PT_RUNTIME is
'(1)记录不同班次的出发和到达时间,即某条线路Strand到达或离开指定站台的时间
(2)NaviMap不制作,而是在NavEx出品之前通过"Strand运行时刻表"和"Stand与站台关系表"批处理完成';

comment on column PT_RUNTIME.STRAND_PID is
'外键,引用"PT_STRAND"';

comment on column PT_RUNTIME.PLATFORM_PID is
'外键,引用"PT_PLATFORM"';

comment on column PT_RUNTIME.ARRIVAL_TIME is
'格式为24小时制,格式为"时:分",例如06:05.
整个班次的时间跨天时,值将大于24,比如凌晨1:30要表达为25:30';

comment on column PT_RUNTIME.DEPART_TIME is
'格式同"到达时间",数值默认为"到达时间延时一分钟"';

comment on column PT_RUNTIME.VALID_WEEK is
'以周为单位,从左到右共7位表示周日到周六,1表示有效,0表示无效.如星期天和星期二有效:1010000,一直有效为1111111';

comment on column PT_RUNTIME.U_RECORD is
'增量更新标识';

comment on column PT_RUNTIME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_STRAND_NAME                                        */
/*==============================================================*/
create table PT_STRAND_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_PT_STRAN check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_PT_STRAN check (NAME_CLASS in (1,2,3,4)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_PT_STRAN check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_STRAND_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_STRAND_NAME primary key (NAME_ID),
   constraint PTSTRAND_NAME foreign key (PID)
         references PT_STRAND (PID) disable
);

comment on table PT_STRAND_NAME is
'[170]';

comment on column PT_STRAND_NAME.NAME_ID is
'主键';

comment on column PT_STRAND_NAME.NAME_GROUPID is
'[171U]从1开始递增编号';

comment on column PT_STRAND_NAME.PID is
'外键,引用"PT_STRAND"';

comment on column PT_STRAND_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column PT_STRAND_NAME.NAME_CLASS is
'[170][172U]';

comment on column PT_STRAND_NAME.NAME is
'[170]';

comment on column PT_STRAND_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column PT_STRAND_NAME.SRC_FLAG is
'现仅指英文名来源';

comment on column PT_STRAND_NAME.U_RECORD is
'增量更新标识';

comment on column PT_STRAND_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_STRAND_PLATFORM                                    */
/*==============================================================*/
create table PT_STRAND_PLATFORM 
(
   STRAND_PID           NUMBER(10)           not null,
   PLATFORM_PID         NUMBER(10)           not null,
   SEQ_NUM              NUMBER(10)           default 0 not null,
   INTERVAL             NUMBER(3)            default 0 not null,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITIONFLAG          VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_STRAND_PLAT check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_STRAND_PLAT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTSTRAND_PLATFORM foreign key (PLATFORM_PID)
         references PT_PLATFORM (PID) disable ,
   constraint PTSTRAND_FLATFORM foreign key (STRAND_PID)
         references PT_STRAND (PID) disable
);

comment on column PT_STRAND_PLATFORM.STRAND_PID is
'外键,引用"PT_STRAND"';

comment on column PT_STRAND_PLATFORM.PLATFORM_PID is
'外键,引用"PT_PLATFORM"';

comment on column PT_STRAND_PLATFORM.SEQ_NUM is
'(1)记录公交线路某条Strand沿线的站台信息
(2)目前所有线路的站台统一从10000开始每次递增10000编号,即10000,20000,30000等';

comment on column PT_STRAND_PLATFORM.INTERVAL is
'单位:分钟';

comment on column PT_STRAND_PLATFORM.DATA_SOURCE is
'[170]';

comment on column PT_STRAND_PLATFORM.UPDATE_BATCH is
'[170]';

comment on column PT_STRAND_PLATFORM.U_RECORD is
'增量更新标识';

comment on column PT_STRAND_PLATFORM.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_STRAND_SCHEDULE                                    */
/*==============================================================*/
create table PT_STRAND_SCHEDULE 
(
   STRAND_PID           NUMBER(10)           not null,
   VALID_DAY            NUMBER(5)            default 0 not null,
   START_TIME           VARCHAR2(32),
   END_TIME             VARCHAR2(32),
   INTERVAL             VARCHAR2(50),
   CITY_CODE            NUMBER(6)            default 0 not null,
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_STRAND_SCHEDULE check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_STRAND_SCHEDUL check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTSTRAND_SCHEDULE foreign key (STRAND_PID)
         references PT_STRAND (PID) disable
);

comment on table PT_STRAND_SCHEDULE is
'记录某线路strand的运行信息: 发车的起止时间和间隔时间等,针对香港地区,存在分时间段的发车情况,如周一到周四,某条线路9:00到12:00,每隔15分钟发一次,12:00到18:00每隔20分钟发一次';

comment on column PT_STRAND_SCHEDULE.STRAND_PID is
'外键,引用"PT_STRAND"';

comment on column PT_STRAND_SCHEDULE.VALID_DAY is
'采用16bit 表示,从右到左依次为0~15bit,每bit 表示一种时间类型(如下),赋值为0/1 分别表示无效/有效,如:0000001111000010,表示夏季的周一到周四有效
第0bit:冬季
第1bit:夏季
第2bit:节假日
第3bit:周日
第4bit:周六
第5bit:周五
第6bit:周四
第7bit:周三
第8bit:周二
第9bit:周一
第10~15bit 均为0
如果第0~9bit 位均为1,表示全部有效';

comment on column PT_STRAND_SCHEDULE.START_TIME is
'记录每条线路各个行车方向的发车开始时间;格式为24小时制,用冒号分隔,"xx:xx"两段数值分别记录"小时:分钟",每条strand可能存在多组发车开始和结束时间,不区分节假日,周末';

comment on column PT_STRAND_SCHEDULE.END_TIME is
'记录每条线路各个行车方向的发车结束时间,格式同"发车开始时间"';

comment on column PT_STRAND_SCHEDULE.INTERVAL is
'记录每条strand单班次发车的间隔时间,以分钟为单位记录,最小单位精确到0.5分,每条Strand只记录一个发车间隔,不区分节假日,周末,也不区分高峰时间等';

comment on column PT_STRAND_SCHEDULE.U_RECORD is
'增量更新标识';

comment on column PT_STRAND_SCHEDULE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: PT_TRANSFER                                           */
/*==============================================================*/
create table PT_TRANSFER 
(
   TRANSFER_ID          NUMBER(10)           not null,
   TRANSFER_TYPE        NUMBER(1)            default 1 not null
      constraint CKC_TRANSFER_TYPE_PT_TRANS check (TRANSFER_TYPE in (0,1)) disable ,
   POI_FIR              NUMBER(10)           default 0 not null,
   POI_SEC              NUMBER(10)           default 0 not null,
   PLATFORM_FIR         NUMBER(10)           default 0 not null,
   PLATFORM_SEC         NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TRANSFER_TIME        NUMBER(2)            default 0 not null,
   EXTERNAL_FLAG        NUMBER(1)            default 0 not null
      constraint CKC_EXTERNAL_FLAG_PT_TRANS check (EXTERNAL_FLAG in (0,1,2)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(200),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
      constraint CKC_STATE_PT_TRANS check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_PT_TRANS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_TRANSFER primary key (TRANSFER_ID)
);

comment on table PT_TRANSFER is
'NaviMap制作时,如果线路图中的换乘站制作成了两个或多个主点(即站点),需要将对应的主点之间两两制作换乘类型为"跨站换乘"的关系.
如果线路图中的换乘站制作成了一个主点,需要在此主点中所有能够互通的站台之间两两制作换乘类型为"站内换乘"的关系.
即:如果是站内换乘,换乘点应该是站台编号;如果是站间换乘,换乘点应该是主点(即站点)';

comment on column PT_TRANSFER.TRANSFER_ID is
'主键';

comment on column PT_TRANSFER.TRANSFER_TYPE is
'区分跨站换乘和站内换乘两种类型:
(1)跨站换乘,表达不同公交线路在两个相邻站点之间的换乘;此时,换乘点一和换乘点二分别表示站点
(2)站内换乘,表达不同公交线路在同一个站点内部的换乘,此时,换乘点一和换乘点二分别表示站台';

comment on column PT_TRANSFER.POI_FIR is
'[173sp1]参考"PT_POI"';

comment on column PT_TRANSFER.POI_SEC is
'[173sp1]参考"PT_POI"';

comment on column PT_TRANSFER.PLATFORM_FIR is
'[173sp1]参考"PT_PLATFORM"';

comment on column PT_TRANSFER.PLATFORM_SEC is
'[173sp1]参考"PT_PLATFORM"';

comment on column PT_TRANSFER.TRANSFER_TIME is
'以分钟为单位,记录乘客换乘时步行需要的时间';

comment on column PT_TRANSFER.EXTERNAL_FLAG is
'[180U]每一组跨站换乘关系,都需要制作"外部标识"属性,用来描述乘客换乘时是否需要走到站点外部.当两个主点之间有专用换乘通道时,"外部标识"制作为"否";若没有专用通道,乘客需要走到站点外面换乘,"外部标识"制作为"是".';

comment on column PT_TRANSFER.U_RECORD is
'增量更新标识';

comment on column PT_TRANSFER.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_BRANCH                                             */
/*==============================================================*/
create table RD_BRANCH 
(
   BRANCH_PID           NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
      constraint CKC_RELATIONSHIP_TYPE_RD_BRANC check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_BRANCH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH primary key (BRANCH_PID),
   constraint RDBRANCH_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDBRANCH_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDBRANCH_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_BRANCH.BRANCH_PID is
'主键';

comment on column RD_BRANCH.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_BRANCH.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_BRANCH.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_BRANCH.U_RECORD is
'增量更新标识';

comment on column RD_BRANCH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_BRANCH_DETAIL                                      */
/*==============================================================*/
create table RD_BRANCH_DETAIL 
(
   DETAIL_ID            NUMBER(10)           not null,
   BRANCH_PID           NUMBER(10)           not null,
   BRANCH_TYPE          NUMBER(1)            default 0 not null
      constraint CKC_BRANCH_TYPE_RD_BRANC check (BRANCH_TYPE in (0,1,2,3,4)) disable ,
   VOICE_DIR            NUMBER(1)            default 0 not null
      constraint CKC_VOICE_DIR_RD_BRANC check (VOICE_DIR in (0,2,5,9)) disable ,
   ESTAB_TYPE           NUMBER(1)            default 0 not null
      constraint CKC_ESTAB_TYPE_RD_BRANC check (ESTAB_TYPE in (0,1,2,3,4,5,9)) disable ,
   NAME_KIND            NUMBER(1)            default 0 not null
      constraint CKC_NAME_KIND_RD_BRANC check (NAME_KIND in (0,1,2,3,4,5,6,7,8,9)) disable ,
   EXIT_NUM             VARCHAR2(32),
   ARROW_CODE           VARCHAR2(10),
   PATTERN_CODE         VARCHAR2(10),
   ARROW_FLAG           NUMBER(2)            default 0 not null
      constraint CKC_ARROW_FLAG_RD_BRANC check (ARROW_FLAG in (0,1)) disable ,
   GUIDE_CODE           NUMBER(1)            default 0 not null
      constraint CKC_GUIDE_CODE_RD_BRANC check (GUIDE_CODE in (0,1,2,3,9)) disable ,
   PROCESS_FLAG         NUMBER(1)                      default 0 not null
      constraint CKC_PROCESS_FLAG_RD_BRANC check (PROCESS_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_BRANCH_DETAIL check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH_DETAIL primary key (DETAIL_ID),
   constraint RDBRANCH_DETAIL foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

comment on column RD_BRANCH_DETAIL.DETAIL_ID is
'主键';

comment on column RD_BRANCH_DETAIL.BRANCH_PID is
'外键,引用"RD_BRANCH"';

comment on column RD_BRANCH_DETAIL.BRANCH_TYPE is
'[180U]';

comment on column RD_BRANCH_DETAIL.VOICE_DIR is
'无,右,左';

comment on column RD_BRANCH_DETAIL.ESTAB_TYPE is
'出口,入口,SA,PA,JCT等';

comment on column RD_BRANCH_DETAIL.NAME_KIND is
'IC,SA,PA,JCT,出口,入口等';

comment on column RD_BRANCH_DETAIL.ARROW_CODE is
'参考"AU_MULTIMEDIA"中"NAME",如:0a24030a';

comment on column RD_BRANCH_DETAIL.PATTERN_CODE is
'参考"AU_MULTIMEDIA"中"NAME",如:8a430211';

comment on column RD_BRANCH_DETAIL.ARROW_FLAG is
'[171A]';

comment on column RD_BRANCH_DETAIL.GUIDE_CODE is
'高架向导,Underpath向导等';

comment on column RD_BRANCH_DETAIL.U_RECORD is
'增量更新标识';

comment on column RD_BRANCH_DETAIL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_BRANCH_NAME                                        */
/*==============================================================*/
create table RD_BRANCH_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   DETAIL_ID            NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   NAME_CLASS           NUMBER(1)            default 0 not null
      constraint CKC_NAME_CLASS_RD_BRANC check (NAME_CLASS in (0,1)) disable ,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_BRANC check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   CODE_TYPE            NUMBER(2)            default 0 not null
      constraint CKC_CODE_TYPE_RD_BRANC check (CODE_TYPE in (0,1,2,3,4,5,6,7,8,9,10)) disable ,
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RD_BRANC check (SRC_FLAG in (0,1,2,3,4,5)) disable ,
   VOICE_FILE           VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_BRANCH_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH_NAME primary key (NAME_ID),
   constraint RDBRANCH_NAME foreign key (DETAIL_ID)
         references RD_BRANCH_DETAIL (DETAIL_ID) disable
);

comment on column RD_BRANCH_NAME.NAME_ID is
'[170]主键';

comment on column RD_BRANCH_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_BRANCH_NAME.DETAIL_ID is
'外键,引用"RD_BRANCH_DETAIL"';

comment on column RD_BRANCH_NAME.SEQ_NUM is
'从1开始递增编号';

comment on column RD_BRANCH_NAME.NAME_CLASS is
'方向,出口';

comment on column RD_BRANCH_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_BRANCH_NAME.CODE_TYPE is
'普通道路名,设施名,高速道路名等';

comment on column RD_BRANCH_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_BRANCH_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column RD_BRANCH_NAME.VOICE_FILE is
'[170]参考"AU_MULTIMEDIA"中"NAME"';

comment on column RD_BRANCH_NAME.U_RECORD is
'增量更新标识';

comment on column RD_BRANCH_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_BRANCH_NAME_META                                   */
/*==============================================================*/
create table RD_BRANCH_NAME_META 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_BRA_META check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_BRA_META check (SRC_FLAG in (0,1,2,3,4,5)) disable ,
   VOICE_FILE           VARCHAR2(100),
   constraint PK_RD_BRANCH_NAME_META primary key (NAME_ID)
);

comment on table RD_BRANCH_NAME_META is
'[173sp2]用于存储分歧英文名称';

comment on column RD_BRANCH_NAME_META.NAME_ID is
'主键';

comment on column RD_BRANCH_NAME_META.NAME_GROUPID is
'从1开始递增编号';

comment on column RD_BRANCH_NAME_META.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_BRANCH_NAME_META.PHONETIC is
'中文为拼音,英文(葡文等)为音标';

comment on column RD_BRANCH_NAME_META.SRC_FLAG is
'现仅指英文名来源';

comment on column RD_BRANCH_NAME_META.VOICE_FILE is
'参考"AU_MULTIMEDIA"中"NAME"';

/*==============================================================*/
/* Table: RD_BRANCH_NAME_TONE                                   */
/*==============================================================*/
create table RD_BRANCH_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_BRANCH_NAME_T check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDBRANCHNAME_TONE foreign key (NAME_ID)
         references RD_BRANCH_NAME (NAME_ID) disable
);

comment on table RD_BRANCH_NAME_TONE is
'[170]';

comment on column RD_BRANCH_NAME_TONE.NAME_ID is
'[170]外键,引用"RD_BRANCH_NAME"';

comment on column RD_BRANCH_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column RD_BRANCH_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column RD_BRANCH_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column RD_BRANCH_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column RD_BRANCH_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column RD_BRANCH_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column RD_BRANCH_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_BRANCH_REALIMAGE                                   */
/*==============================================================*/
create table RD_BRANCH_REALIMAGE 
(
   BRANCH_PID           NUMBER(10)           not null,
   IMAGE_TYPE           NUMBER(1)            default 0 not null
      constraint CKC_IMAGE_TYPE_RD_BRANC check (IMAGE_TYPE in (0,1)) disable ,
   REAL_CODE            VARCHAR2(10),
   ARROW_CODE           VARCHAR2(10),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_BRANCH_REALIMA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDBRANCH_REALIMAGE foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

comment on column RD_BRANCH_REALIMAGE.BRANCH_PID is
'外键,"RD_BRANCH"';

comment on column RD_BRANCH_REALIMAGE.REAL_CODE is
'参考"AU_MULTIMEDIA"中"NAME"
(1)高速出入口实景图与HEG 实景图代码相同,采用8 位编码,如6102500a
(2) 普通道路路口实景图代码9 位编码, 如140230281;';

comment on column RD_BRANCH_REALIMAGE.ARROW_CODE is
'除第一位编码不同外,其他与实景图代码相同';

comment on column RD_BRANCH_REALIMAGE.U_RECORD is
'增量更新标识';

comment on column RD_BRANCH_REALIMAGE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_BRANCH_VIA                                         */
/*==============================================================*/
create table RD_BRANCH_VIA 
(
   BRANCH_PID           NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   GROUP_ID             NUMBER(2)            default 1 not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_BRANCH_VIA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDBRANCH_VIALINKS foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable ,
   constraint RDBRANCH_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_BRANCH_VIA is
'(1)表达同一进入Link和退出Link之间的多组经过Link,而且经过Link不包括进入Link和退出Link
(2)NaviMap作业中,当进入和退出Link直接在同一路口挂接时,不制作经过Link;否则(如线线关系),需要制作经过Link';

comment on column RD_BRANCH_VIA.BRANCH_PID is
'外键,引用"RD_BRANCH"';

comment on column RD_BRANCH_VIA.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_BRANCH_VIA.GROUP_ID is
'从1开始递增编号';

comment on column RD_BRANCH_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_BRANCH_VIA.U_RECORD is
'增量更新标识';

comment on column RD_BRANCH_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CHAIN                                              */
/*==============================================================*/
create table RD_CHAIN 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_CHAIN check (TYPE in (0,1,2,3,4,5,6,7,8)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CHAIN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CHAIN primary key (PID)
);

comment on column RD_CHAIN.PID is
'主键';

comment on column RD_CHAIN.TYPE is
'同名路链,环岛,JCT等
';

comment on column RD_CHAIN.U_RECORD is
'增量更新标识';

comment on column RD_CHAIN.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CHAIN_LINK                                         */
/*==============================================================*/
create table RD_CHAIN_LINK 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CHAIN_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCHAIN_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDCHAIN_LINKS foreign key (PID)
         references RD_CHAIN (PID) disable
);

comment on column RD_CHAIN_LINK.PID is
'外键,引用"RD_CHAIN"';

comment on column RD_CHAIN_LINK.SEQ_NUM is
'从1开始递增编号';

comment on column RD_CHAIN_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_CHAIN_LINK.U_RECORD is
'增量更新标识';

comment on column RD_CHAIN_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CHAIN_NAME                                         */
/*==============================================================*/
create table RD_CHAIN_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_CHAIN check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CHAIN_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CHAIN_NAME primary key (NAME_ID),
   constraint RDCHAIN_NAME foreign key (PID)
         references RD_CHAIN (PID) disable
);

comment on column RD_CHAIN_NAME.NAME_ID is
'[170]主键';

comment on column RD_CHAIN_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_CHAIN_NAME.PID is
'外键,引用"RD_CHAIN"';

comment on column RD_CHAIN_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_CHAIN_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_CHAIN_NAME.U_RECORD is
'增量更新标识';

comment on column RD_CHAIN_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSS                                              */
/*==============================================================*/
create table RD_CROSS 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_CROSS check (TYPE in (0,1)) disable ,
   SIGNAL               NUMBER(1)            default 0 not null
      constraint CKC_SIGNAL_RD_CROSS check (SIGNAL in (0,1,2)) disable ,
   ELECTROEYE           NUMBER(1)            default 0 not null
      constraint CKC_ELECTROEYE_RD_CROSS check (ELECTROEYE in (0,1,2)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
      constraint CKC_KG_FLAG_RD_CROSS check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CROSS primary key (PID)
);

comment on column RD_CROSS.PID is
'主键';

comment on column RD_CROSS.TYPE is
'简单路口,复合路口';

comment on column RD_CROSS.SIGNAL is
'有无红绿灯,路口红绿灯或行人红绿灯';

comment on column RD_CROSS.ELECTROEYE is
'是否具有电子眼';

comment on column RD_CROSS.KG_FLAG is
'区分路口是K专用,G专用,KG共用的标志';

comment on column RD_CROSS.U_RECORD is
'增量更新标识';

comment on column RD_CROSS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSSWALK                                          */
/*==============================================================*/
create table RD_CROSSWALK 
(
   PID                  NUMBER(10)           not null,
   CURB_RAMP            NUMBER(1)            default 0 not null
      constraint CKC_CURB_RAMP_RD_CROSS check (CURB_RAMP in (0,1,2,3)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSSWALK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CROSSWALK primary key (PID)
);

comment on column RD_CROSSWALK.PID is
'主键';

comment on column RD_CROSSWALK.CURB_RAMP is
'有,无或不应用';

comment on column RD_CROSSWALK.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_CROSSWALK.U_RECORD is
'增量更新标识';

comment on column RD_CROSSWALK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSSWALK_INFO                                     */
/*==============================================================*/
create table RD_CROSSWALK_INFO 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_CROSSWALK_INFO check (TYPE in (0,1,2,3,4,5,6)) disable ,
   ATTR                 NUMBER(5)            default 0 not null,
   SIGNAGE              NUMBER(1)            default 0 not null
      constraint CKC_SIGNAGE_RD_CROSS check (SIGNAGE in (0,1,2,3,4)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSSWALK_INFO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNODE_RDCROSSWALKINFO foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDCROSSWALK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDCROSSWALK foreign key (PID)
         references RD_CROSSWALK (PID) disable
);

comment on column RD_CROSSWALK_INFO.PID is
'外键,引用"RD_CROSSWALK"';

comment on column RD_CROSSWALK_INFO.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_CROSSWALK_INFO.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_CROSSWALK_INFO.TYPE is
'人行横道(斑马线或平行线),地下通道,过街天桥等';

comment on column RD_CROSSWALK_INFO.ATTR is
'采用8bit 表示,从右到左依次为0~7bit,每bit 表示一种方式类型(如下),赋值为0/1 分别表示无/有,如:00000011 表示斜坡和阶梯;00000101 表示斜坡和扶梯
第0bit:斜坡
第1bit:阶梯
第2bit:扶梯
第3bit:直梯
第4bit:索道
第5bit:其他
如果所有bit 位均为0,表示未调查';

comment on column RD_CROSSWALK_INFO.SIGNAGE is
'记录从Node到Link的左侧或右侧有无过道标牌信息';

comment on column RD_CROSSWALK_INFO.U_RECORD is
'增量更新标识';

comment on column RD_CROSSWALK_INFO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSSWALK_NODE                                     */
/*==============================================================*/
create table RD_CROSSWALK_NODE 
(
   PID                  NUMBER(10)           not null,
   FIR_NODE_PID         NUMBER(10)           not null,
   SEN_NODE_PID         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSSWALK_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCROSSWALK_NODE foreign key (PID)
         references RD_CROSSWALK (PID) disable ,
   constraint RDCROSSWALK_FIRNODE foreign key (FIR_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDCROSSWALK_SENNODE foreign key (SEN_NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_CROSSWALK_NODE.PID is
'外键,引用"RD_CROSSWALK"';

comment on column RD_CROSSWALK_NODE.FIR_NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_CROSSWALK_NODE.SEN_NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_CROSSWALK_NODE.U_RECORD is
'增量更新标识';

comment on column RD_CROSSWALK_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSS_LINK                                         */
/*==============================================================*/
create table RD_CROSS_LINK 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSS_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCROSS_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDCROSS_LINKS foreign key (PID)
         references RD_CROSS (PID) disable
);

comment on table RD_CROSS_LINK is
'即交叉口内Link';

comment on column RD_CROSS_LINK.PID is
'外键,引用"RD_CROSS"';

comment on column RD_CROSS_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_CROSS_LINK.U_RECORD is
'增量更新标识';

comment on column RD_CROSS_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSS_NAME                                         */
/*==============================================================*/
create table RD_CROSS_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_CROSS check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RD_CROSS check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSS_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CROSS_NAME primary key (NAME_ID),
   constraint RDCROSS_NAME foreign key (PID)
         references RD_CROSS (PID) disable
);

comment on column RD_CROSS_NAME.NAME_ID is
'[170]主键';

comment on column RD_CROSS_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_CROSS_NAME.PID is
'外键,引用"RD_CROSS"';

comment on column RD_CROSS_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_CROSS_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_CROSS_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column RD_CROSS_NAME.U_RECORD is
'增量更新标识';

comment on column RD_CROSS_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_CROSS_NODE                                         */
/*==============================================================*/
create table RD_CROSS_NODE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   IS_MAIN              NUMBER(1)            default 0 not null
      constraint CKC_IS_MAIN_RD_CROSS check (IS_MAIN in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_CROSS_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCROSS_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDCROSS_NODES foreign key (PID)
         references RD_CROSS (PID) disable
);

comment on column RD_CROSS_NODE.PID is
'外键,引用"RD_CROSS"';

comment on column RD_CROSS_NODE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_CROSS_NODE.IS_MAIN is
'区分Node是路口主点还是子点';

comment on column RD_CROSS_NODE.U_RECORD is
'增量更新标识';

comment on column RD_CROSS_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_DIRECTROUTE                                        */
/*==============================================================*/
create table RD_DIRECTROUTE 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   FLAG                 NUMBER(1)            default 2 not null
      constraint CKC_FLAG_RD_DIREC check (FLAG in (0,1,2)) disable ,
   PROCESS_FLAG         NUMBER(1)            default 1 not null
      constraint CKC_PROCESS_FLAG_RD_DIREC check (PROCESS_FLAG in (0,1,2)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
      constraint CKC_RELATIONSHIP_TYPE_RD_DIREC check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_DIRECTROUTE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_DIRECTROUTE primary key (PID),
   constraint RDDIRECT_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDDIRECT_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDDIRECTROUTE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_DIRECTROUTE.PID is
'主键';

comment on column RD_DIRECTROUTE.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_DIRECTROUTE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_DIRECTROUTE.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_DIRECTROUTE.FLAG is
'未验证,实地顺行,理论顺行';

comment on column RD_DIRECTROUTE.PROCESS_FLAG is
'人工添加或批处理';

comment on column RD_DIRECTROUTE.U_RECORD is
'增量更新标识';

comment on column RD_DIRECTROUTE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_DIRECTROUTE_VIA                                    */
/*==============================================================*/
create table RD_DIRECTROUTE_VIA 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   GROUP_ID             NUMBER(2)            default 1 not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_DIRECTROUTE_V check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDDIRECT_VIALINK foreign key (PID)
         references RD_DIRECTROUTE (PID) disable ,
   constraint RDDIRECTROUTE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_DIRECTROUTE_VIA is
'(1)表达同一进入Link和退出Link之间的多组经过Link,而且经过Link不包括进入Link和退出Link
(2)NaviMap作业中,当进入和退出Link直接在同一路口挂接时,不制作经过Link;否则(如线线关系),需要制作经过Link';

comment on column RD_DIRECTROUTE_VIA.PID is
'外键,引用"RD_DIRECTROUTE"';

comment on column RD_DIRECTROUTE_VIA.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_DIRECTROUTE_VIA.GROUP_ID is
'从1开始递增编号';

comment on column RD_DIRECTROUTE_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_DIRECTROUTE_VIA.U_RECORD is
'增量更新标识';

comment on column RD_DIRECTROUTE_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_ELECTRONICEYE                                      */
/*==============================================================*/
create table RD_ELECTRONICEYE 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
      constraint CKC_KIND_RD_ELECT check (KIND in (0,1,2,3,10,11,12,13,14,15,16,98)) disable ,
   ANGLE                NUMBER(8,5)          default 0 not null,
   LOCATION             NUMBER(2)            default 0 not null,
   SPEED_LIMIT          NUMBER(4)            default 0 not null,
   VERIFIED_FLAG        NUMBER(2)            default 0 not null
      constraint CKC_VERIFIED_FLAG_RD_ELECT check (VERIFIED_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_ELECT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_ELECTRONICEYE primary key (PID),
   constraint RDELECTRONIC_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDELECTRONIC_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_ELECTRONICEYE.PID is
'主键';

comment on column RD_ELECTRONICEYE.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_ELECTRONICEYE.NODE_PID is
'[170]外键,引用"RD_NODE"';

comment on column RD_ELECTRONICEYE.KIND is
'限速摄像头,雷达测速摄像头,移动式测速,交通信号灯摄像头等';

comment on column RD_ELECTRONICEYE.ANGLE is
'[171A]电子眼与正北方向的夹角,0~360度';

comment on column RD_ELECTRONICEYE.LOCATION is
'[172U]采用3bit表示,从右到左依次为0~2bit,每bit表示一个位置(如下),赋值为0/1分别表示否/是,如: 101表示左和上
第0bit:左(Left)
第1bit:右(Right)
第2bit:上(Overhead)
如果所有bit位均为0,表示未调查';

comment on column RD_ELECTRONICEYE.SPEED_LIMIT is
'当Kind=1~3 时有效, 单位:百米/时,值域: 1~9999';

comment on column RD_ELECTRONICEYE.VERIFIED_FLAG is
'[173sp2]';

comment on column RD_ELECTRONICEYE.U_RECORD is
'增量更新标识';

comment on column RD_ELECTRONICEYE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_GATE                                               */
/*==============================================================*/
create table RD_GATE 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 2 not null
      constraint CKC_TYPE_RD_GATE check (TYPE in (0,1,2)) disable ,
   DIR                  NUMBER(1)            default 2 not null
      constraint CKC_DIR_RD_GATE check (DIR in (0,1,2)) disable ,
   FEE                  NUMBER(1)            default 0 not null
      constraint CKC_FEE_RD_GATE check (FEE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_GATE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_GATE primary key (PID),
   constraint RDGATE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDGATE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDGATE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_GATE is
'NaviMap制作时进入Link和退出Link必须接续';

comment on column RD_GATE.PID is
'主键';

comment on column RD_GATE.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_GATE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_GATE.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_GATE.FEE is
'免费,收费';

comment on column RD_GATE.U_RECORD is
'增量更新标识';

comment on column RD_GATE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_GATE_CONDITION                                     */
/*==============================================================*/
create table RD_GATE_CONDITION 
(
   PID                  NUMBER(10)           not null,
   VALID_OBJ            NUMBER(1)            default 0 not null
      constraint CKC_VALID_OBJ_RD_GATE_ check (VALID_OBJ in (0,1)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_GATE_ check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDGATE_CONDITION foreign key (PID)
         references RD_GATE (PID) disable
);

comment on table RD_GATE_CONDITION is
'(1)记录针对不同"开放对象"的开放时间
(2)如果表示"机动车和行人均开放的时间",则分别存储为两条记录';

comment on column RD_GATE_CONDITION.PID is
'外键,引用"RD_GATE"';

comment on column RD_GATE_CONDITION.VALID_OBJ is
'机动车辆,行人';

comment on column RD_GATE_CONDITION.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_GATE_CONDITION.U_RECORD is
'增量更新标识';

comment on column RD_GATE_CONDITION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_GSC                                                */
/*==============================================================*/
create table RD_GSC 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   PROCESS_FLAG         NUMBER(1)            default 1 not null
      constraint CKC_PROCESS_FLAG_RD_GSC check (PROCESS_FLAG in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_GSC check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_GSC primary key (PID)
);

comment on table RD_GSC is
'记录Link之间的立交关系';

comment on column RD_GSC.PID is
'主键';

comment on column RD_GSC.GEOMETRY is
'存储以"度"为单位的经纬度立交点坐标';

comment on column RD_GSC.PROCESS_FLAG is
'区分人工赋值,程序赋值,特殊处理等方式';

comment on column RD_GSC.U_RECORD is
'增量更新标识';

comment on column RD_GSC.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_GSC_LINK                                           */
/*==============================================================*/
create table RD_GSC_LINK 
(
   PID                  NUMBER(10)           not null,
   ZLEVEL               NUMBER(2)            default 0 not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   SHP_SEQ_NUM          NUMBER(5)            default 1 not null,
   START_END            NUMBER(1)            default 0 not null
      constraint CKC_START_END_RD_GSC_L check (START_END in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_GSC_L check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDGSC_LINK foreign key (PID)
         references RD_GSC (PID) disable
);

comment on table RD_GSC_LINK is
'记录具有立交关系的LINK信息';

comment on column RD_GSC_LINK.PID is
'外键,引用"RD_GSC"';

comment on column RD_GSC_LINK.ZLEVEL is
'记录立交Link 之间的上下层次关系,值域包括:
(1)地面:0
(2)地上: 大于0
(3)地下: 小于0';

comment on column RD_GSC_LINK.LINK_PID is
'参考"RD_LINK","AD_LINK"等,记录构成立交关系的不同要素主题的Link号码,如道路Link,铁路Link等';

comment on column RD_GSC_LINK.TABLE_NAME is
'记录要素所在的数据表,如LINK号码为道路LINK=20与水系LINK=40立交时,数据表名分别为"RD_LINK"和"LC_LINK"';

comment on column RD_GSC_LINK.SHP_SEQ_NUM is
'(1)记录当前立交点在Link上的位置序号,当立交点不是Link上已有形状点时则插入
(2)序号从0开始递增编号,即SHP_SEQ_NUM=0~N-1(N为包括起点和终点在内的Link总点数).其中,起点(SHP_SEQ_NUM=0),终点(SHP_SEQ_NUM=N-1)';

comment on column RD_GSC_LINK.START_END is
'记录立交点是LINK的起点(SHP_SEQ_NUM=0),终点(SHP_SEQ_NUM=N-1)或形状点';

comment on column RD_GSC_LINK.U_RECORD is
'增量更新标识';

comment on column RD_GSC_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_INTER                                              */
/*==============================================================*/
create table RD_INTER 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_INTER check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_INTER primary key (PID)
);

comment on column RD_INTER.PID is
'主键';

comment on column RD_INTER.U_RECORD is
'增量更新标识';

comment on column RD_INTER.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_INTER_LINK                                         */
/*==============================================================*/
create table RD_INTER_LINK 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_INTER_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDINTERSECTION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDINTERSECTION_LINKS foreign key (PID)
         references RD_INTER (PID) disable
);

comment on column RD_INTER_LINK.PID is
'外键,引用"RD_INTER"';

comment on column RD_INTER_LINK.SEQ_NUM is
'从1开始递增编号';

comment on column RD_INTER_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_INTER_LINK.U_RECORD is
'增量更新标识';

comment on column RD_INTER_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_INTER_NODE                                         */
/*==============================================================*/
create table RD_INTER_NODE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_INTER_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDINTERSECTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDINTERSECTION_NODES foreign key (PID)
         references RD_INTER (PID) disable
);

comment on column RD_INTER_NODE.PID is
'外键,引用"RD_INTER"';

comment on column RD_INTER_NODE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_INTER_NODE.U_RECORD is
'增量更新标识';

comment on column RD_INTER_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE                                               */
/*==============================================================*/
create table RD_LANE 
(
   LANE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   LANE_NUM             NUMBER(2)            default 1 not null,
   TRAVEL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_TRAVEL_FLAG_RD_LANE check (TRAVEL_FLAG in (0,1)) disable ,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   LANE_FORMING         NUMBER(2)            default 0 not null
      constraint CKC_LANE_FORMING_RD_LANE check (LANE_FORMING in (0,1,2,3)) disable ,
   LANE_DIR             NUMBER(1)            default 2 not null
      constraint CKC_LANE_DIR_RD_LANE check (LANE_DIR in (1,2,3)) disable ,
   LANE_TYPE            NUMBER(10)           default 1 not null,
   ARROW_DIR            VARCHAR2(1)          default '9' not null
      constraint CKC_ARROW_DIR_RD_LANE check (ARROW_DIR in ('9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5')) disable ,
   LANE_MARK            NUMBER(2)            default 0 not null
      constraint CKC_LANE_MARK_RD_LANE check (LANE_MARK in (0,1,2,3,4,5,6,7,98,99)) disable ,
   WIDTH                NUMBER(5,2)          default 0 not null,
   RESTRICT_HEIGHT      NUMBER(5,2)          default 0 not null,
   TRANSITION_AREA      NUMBER(2)            default 0 not null
      constraint CKC_TRANSITION_AREA_RD_LANE check (TRANSITION_AREA in (0,1,2)) disable ,
   FROM_MAX_SPEED       NUMBER(4)            default 0 not null,
   TO_MAX_SPEED         NUMBER(4)            default 0 not null,
   FROM_MIN_SPEED       NUMBER(4)            default 0 not null,
   TO_MIN_SPEED         NUMBER(4)            default 0 not null,
   ELEC_EYE             NUMBER(1)            default 0 not null
      constraint CKC_ELEC_EYE_RD_LANE check (ELEC_EYE in (0,1,2)) disable ,
   LANE_DIVIDER         NUMBER(2)            default 0 not null
      constraint CKC_LANE_DIVIDER_RD_LANE check (LANE_DIVIDER in (0,10,11,12,13,20,21,30,31,40,50,51,60,99)) disable ,
   CENTER_DIVIDER       NUMBER(2)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE primary key (LANE_PID),
   constraint RDLANE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LANE is
'记录车道方向,箭头方向,标线等,以及与道路Link的关系等';

comment on column RD_LANE.LANE_PID is
'主键';

comment on column RD_LANE.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LANE.LANE_NUM is
'注:与LINK车道数保持一致';

comment on column RD_LANE.TRAVEL_FLAG is
'[171A]';

comment on column RD_LANE.SEQ_NUM is
'从1开始递增编号
注:大陆:从左到右;港澳:从右到左
';

comment on column RD_LANE.LANE_FORMING is
'[170]';

comment on column RD_LANE.LANE_DIR is
'注: 
(1)仅记录双方向LINK的车道方向,如果与LINK画线方向相同为顺方向,反之逆方向
(2)其他赋值无';

comment on column RD_LANE.LANE_TYPE is
'采用32bit 表示,从右到左依次为0~31bit,每bit 表示一个类型(如下),赋值为0/1 分别表示否/是, 如:0000 0000 0011 0000 表示满载车道和快车道
第0bit:常规车道(Regular Lane)
第1bit:复合车道(Auxiliary Lane)
第2bit:加速车道(Accelerate Lane)
第3bit:减速车道(Decelerate Lane)
第4bit:满载车道(HOV Lane)
第5bit:快车道(Express Lane)
第6bit:慢车道(Slow Lane)
第7bit:超车道(Passing/Overtaking Lane)
第8bit:路肩紧急停车带(Drivable shoulder Lane)
第9bit:货车停车带(Truck Parking Lane)
第10bit:管制车道(Regulated Lane Access)
第11bit:可逆车道(Reversible Lane)
第12bit:中心转向车道(Center Turn Lane)
第13bit:转向车道(Turn Lane)
第14bit:空车道
第15bit:转向可变车道
注:
(1)可逆车道/ 路肩紧急停车带在RD_LANE_CONDITION 表必须有时间段存在
(2)超车道只用于双向道路
(3)管制车道用于管制卡车
(4)转向可变车道时转向箭头为所有可变箭头的组合,在RD_LANE_TOPO_DETAIL 表必须有时间段存在';

comment on column RD_LANE.ARROW_DIR is
'记录每一个车道的导向箭头信息,如左转,直左,调头等';

comment on column RD_LANE.LANE_MARK is
'记录实际道路中每个车道上绘制的标线,如停车让行线,减速让行线,减速丘标线,收费站减速标线等';

comment on column RD_LANE.WIDTH is
'[170]单位:米';

comment on column RD_LANE.RESTRICT_HEIGHT is
'[170]单位:米';

comment on column RD_LANE.TRANSITION_AREA is
'[170]';

comment on column RD_LANE.FROM_MAX_SPEED is
'[170]单位:百米/时,值域: 1~9999';

comment on column RD_LANE.TO_MAX_SPEED is
'[173sp1][170]单位:百米/时,值域: 1~9999';

comment on column RD_LANE.FROM_MIN_SPEED is
'[170]单位:百米/时,值域: 1~9999';

comment on column RD_LANE.TO_MIN_SPEED is
'[170]单位:百米/时,值域: 1~9999';

comment on column RD_LANE.ELEC_EYE is
'是否有车道电子眼';

comment on column RD_LANE.LANE_DIVIDER is
'[170]记录位于车道方向右侧(大陆)或左侧(港澳)的分隔带';

comment on column RD_LANE.CENTER_DIVIDER is
'[170]记录位于车道左侧(大陆)或右侧(港澳)的中央分隔带,类型同车道分隔带
注:只对双方向道路有效';

comment on column RD_LANE.U_RECORD is
'增量更新标识';

comment on column RD_LANE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE_CONDITION                                     */
/*==============================================================*/
create table RD_LANE_CONDITION 
(
   LANE_PID             NUMBER(10)           not null,
   DIRECTION            NUMBER(1)            default 2 not null
      constraint CKC_DIRECTION_RD_LANE_ check (DIRECTION in (1,2,3)) disable ,
   DIRECTION_TIME       VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   VEHICLE_TIME         VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE_CONDITION check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLANE_CONDITION foreign key (LANE_PID)
         references RD_LANE (LANE_PID) disable
);

comment on column RD_LANE_CONDITION.LANE_PID is
'外键,引用"RD_LANE"';

comment on column RD_LANE_CONDITION.DIRECTION is
'相对于LINK 通行方向(即车流方向),如果与车流方向相同为顺方向,反之为逆方向';

comment on column RD_LANE_CONDITION.DIRECTION_TIME is
'格式参考"时间域"';

comment on column RD_LANE_CONDITION.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_LANE_CONDITION.VEHICLE_TIME is
'格式参考"时间域"';

comment on column RD_LANE_CONDITION.U_RECORD is
'增量更新标识';

comment on column RD_LANE_CONDITION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE_CONNEXITY                                     */
/*==============================================================*/
create table RD_LANE_CONNEXITY 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LANE_INFO            VARCHAR2(64),
   CONFLICT_FLAG        NUMBER(1)            default 0 not null
      constraint CKC_CONFLICT_FLAG_RD_LANE_ check (CONFLICT_FLAG in (0,1)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
      constraint CKC_KG_FLAG_RD_LANE_TOPOLOGY check (KG_FLAG in (0,1,2)) disable ,
   LANE_NUM             NUMBER(3)            default 0 not null,
   LEFT_EXTEND          NUMBER(3)            default 0 not null,
   RIGHT_EXTEND         NUMBER(3)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE_CONNEXITY check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE_CONNEXITY primary key (PID),
   constraint RDLANE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDLANE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_LANE_CONNEXITY is
'记录车信的进入线点关系,以及路口处或路上的通行车道,左右附加车道和左直右等车道信息
(1)路口车信:进入NODE和退出LINK(RD_LANE_TOPOLOGY)值均不为0
(2)路上车信:进入NODE和退出LINK(RD_LANE_TOPOLOGY)值均为0';

comment on column RD_LANE_CONNEXITY.PID is
'主键';

comment on column RD_LANE_CONNEXITY.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LANE_CONNEXITY.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_LANE_CONNEXITY.LANE_INFO is
'记录路口处或道路上的进入LINK到所有退出LINK的车道信息
(1)由一个或多个英文半角车信代码构成,内容与RD_LANE一致,如:"A"表示直行;"B"表示"左转
(2)如果存在附加车信,则将代码用英文半角括号"[]"括起来,如"[A]"表示附加直行,"[B]"表示附加左转,通过字符串的顺序来区分左附加还是右附加
(3)组合车信之间用英文半角","分割,如:"[A],A,C,[C]"表示"左附加直行,直行,右转,右附加右转"
(4)公交专用道的转向信息用英文半角尖括号"<>"表示,如公交专用右转，表示为"<c>"
(5)当存在某一车道社会车辆与公交共用时,先表达社会车辆的转向信息,后表达公交转向信息,中间无逗号分隔,如:某一车道社会车辆直行,同时公交右转时,表示为:"b<c>"
(6)当存在某一车道既是附加车道又是公交专用时,在原则2 的基础上,用[]表达附加车道,如:某一车道是对社会车辆是右附加直行,而且是公交专用右转表示为:"[b<c>]"';

comment on column RD_LANE_CONNEXITY.LANE_NUM is
'进入LINK在路口处的车道总数(含左右附加车道)';

comment on column RD_LANE_CONNEXITY.LEFT_EXTEND is
'进入LINK在路口处的左附加车道数';

comment on column RD_LANE_CONNEXITY.RIGHT_EXTEND is
'进入LINK在路口处的右附加车道数';

comment on column RD_LANE_CONNEXITY.U_RECORD is
'增量更新标识';

comment on column RD_LANE_CONNEXITY.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE_TOPOLOGY                                      */
/*==============================================================*/
create table RD_LANE_TOPOLOGY 
(
   TOPOLOGY_ID          NUMBER(10)           not null,
   CONNEXITY_PID        NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   IN_LANE_INFO         NUMBER(10)           default 0 not null,
   OUT_LANE_INFO        NUMBER(10)           default 0 not null,
   BUS_LANE_INFO        NUMBER(10)           default 0 not null,
   REACH_DIR            NUMBER(1)            default 0 not null
      constraint CKC_REACH_DIR_RD_LANE_TOPOLOGY check (REACH_DIR in (0,1,2,3,4,5,6)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
      constraint CKC_RELATIONSHIP_TYPE_RD_LANE_ check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE_TOPOLOGY check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE_TOPOLOGY primary key (TOPOLOGY_ID),
   constraint RDLANE_TOPOLOGY foreign key (CONNEXITY_PID)
         references RD_LANE_CONNEXITY (PID) disable ,
   constraint RDLANEVTOPOLOGY_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_LANE_TOPOLOGY.TOPOLOGY_ID is
'主键';

comment on column RD_LANE_TOPOLOGY.CONNEXITY_PID is
'外键,引用"RD_LANE_CONNEXITY"';

comment on column RD_LANE_TOPOLOGY.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LANE_TOPOLOGY.IN_LANE_INFO is
'采用16bit表示,1为可用车道,0为不可用,默认为0';

comment on column RD_LANE_TOPOLOGY.OUT_LANE_INFO is
'同"进入车道信息"';

comment on column RD_LANE_TOPOLOGY.BUS_LANE_INFO is
'同"进入车道信息"';

comment on column RD_LANE_TOPOLOGY.REACH_DIR is
'NaviMap制作中,直(1),左(a),右(4),调(7),左斜前(b,c),右斜前(2,3)括号内为GDB对应的NIDB数值';

comment on column RD_LANE_TOPOLOGY.U_RECORD is
'增量更新标识';

comment on column RD_LANE_TOPOLOGY.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE_TOPO_DETAIL                                   */
/*==============================================================*/
create table RD_LANE_TOPO_DETAIL 
(
   TOPO_ID              NUMBER(10)           not null,
   IN_LANE_PID          NUMBER(10)           not null,
   OUT_LANE_PID         NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   REACH_DIR            NUMBER(1)            default 0 not null
      constraint CKC_REACH_DIR_RD_LANE_TOPO_D check (REACH_DIR in (0,1,2,3,4,5,6)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE_TOPO_D check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE_TOPO_DETAIL primary key (TOPO_ID),
   constraint RDLANE_DETAIL_IN foreign key (IN_LANE_PID)
         references RD_LANE (LANE_PID) disable ,
   constraint RDLANE_DETAIL_OUT foreign key (OUT_LANE_PID)
         references RD_LANE (LANE_PID) disable ,
   constraint RDLANE_DETAIL_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDLANE_DETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LANE_TOPO_DETAIL is
'记录详细车道之间的连通关系,包括路口车道和路上车道';

comment on column RD_LANE_TOPO_DETAIL.TOPO_ID is
'[170]主键';

comment on column RD_LANE_TOPO_DETAIL.IN_LANE_PID is
'外键,引用"RD_LANE"';

comment on column RD_LANE_TOPO_DETAIL.OUT_LANE_PID is
'外键,引用"RD_LANE"';

comment on column RD_LANE_TOPO_DETAIL.IN_LINK_PID is
'[171A]外键,引用"RD_LINK"';

comment on column RD_LANE_TOPO_DETAIL.OUT_LINK_PID is
'[171A]外键,引用"RD_LINK"';

comment on column RD_LANE_TOPO_DETAIL.REACH_DIR is
'[170]记录进入车道到退出车道的通达方向';

comment on column RD_LANE_TOPO_DETAIL.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_LANE_TOPO_DETAIL.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_LANE_TOPO_DETAIL.U_RECORD is
'增量更新标识';

comment on column RD_LANE_TOPO_DETAIL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE_TOPO_VIA                                      */
/*==============================================================*/
create table RD_LANE_TOPO_VIA 
(
   TOPO_ID              NUMBER(10)           not null,
   LANE_PID             NUMBER(10)           default 0 not null,
   VIA_LINK_PID         NUMBER(10)           not null,
   GROUP_ID             NUMBER(2)            default 1 not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE_TOPO_VIA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLANETOPODETAIL_VIA foreign key (TOPO_ID)
         references RD_LANE_TOPO_DETAIL (TOPO_ID) disable ,
   constraint RDLANE_TOPO_VIA_LINK foreign key (VIA_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LANE_TOPO_VIA is
'[170]';

comment on column RD_LANE_TOPO_VIA.TOPO_ID is
'外键,引用"RD_LANE_TOPO_DETAIL"';

comment on column RD_LANE_TOPO_VIA.LANE_PID is
'[171U]参考"RD_LANE",进入和退出LANE 除外';

comment on column RD_LANE_TOPO_VIA.VIA_LINK_PID is
'[171A]外键,引用"RD_LINK"';

comment on column RD_LANE_TOPO_VIA.GROUP_ID is
'从1开始递增编号';

comment on column RD_LANE_TOPO_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_LANE_TOPO_VIA.U_RECORD is
'增量更新标识';

comment on column RD_LANE_TOPO_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LANE_VIA                                           */
/*==============================================================*/
create table RD_LANE_VIA 
(
   TOPOLOGY_ID          NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   GROUP_ID             NUMBER(2)            default 1 not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LANE_VIA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLANE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDLANETOPOLOGY_VIALINK foreign key (TOPOLOGY_ID)
         references RD_LANE_TOPOLOGY (TOPOLOGY_ID) disable
);

comment on table RD_LANE_VIA is
'(1)表达同一进入Link和退出Link之间的多组经过Link,而且经过Link不包括进入Link和退出Link
(2)NaviMap作业中,当进入和退出Link直接在同一路口挂接时,经过Link从路口的交叉口内Link中取;否则,可根据适当的计算原则(如经过Link最少,通行方向一致等)取推荐路径';

comment on column RD_LANE_VIA.TOPOLOGY_ID is
'外键,引用"RD_LANE_TOPOLOGY"';

comment on column RD_LANE_VIA.LINK_PID is
'外键,引用"RD_LINK",进入和退出LINK除外';

comment on column RD_LANE_VIA.GROUP_ID is
'从1开始递增编号';

comment on column RD_LANE_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_LANE_VIA.U_RECORD is
'增量更新标识';

comment on column RD_LANE_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_NAME                                          */
/*==============================================================*/
create table RD_LINK_NAME 
(
   RELATE_ID            NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   NAME_CLASS           NUMBER(1)            default 1 not null
      constraint CKC_NAME_CLASS_RD_LINK_ check (NAME_CLASS in (1,2,3)) disable ,
   INPUT_TIME           VARCHAR2(32),
   NAME_TYPE            NUMBER(1)            default 0 not null
      constraint CKC_NAME_TYPE_RD_LINK_ check (NAME_TYPE in (0,1,2,3,4,5,6,7,8,9)) disable ,
   SRC_FLAG             NUMBER(1)            default 0 not null
      constraint CKC_SRC_FLAG_RD_LINKNAME check (SRC_FLAG in (0,1,2,3,4,5,6,9)) disable ,
   ROUTE_ATT            NUMBER(1)            default 0 not null
      constraint CKC_ROUTE_ATT_RD_LINK_ check (ROUTE_ATT in (0,1,2,3,4,5,9)) disable ,
   CODE                 NUMBER(1)            default 0 not null
      constraint CKC_CODE_RD_LINK_NAME check (CODE in (0,1,2,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_NAME2 check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LINK_NAME primary key (RELATE_ID),
   constraint RDLINK_NAMES foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_LINK_NAME.RELATE_ID is
'主键';

comment on column RD_LINK_NAME.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_NAME.SEQ_NUM is
'从1开始递增编号';

comment on column RD_LINK_NAME.NAME_GROUPID is
'[170]参考"RD_NAME"';

comment on column RD_LINK_NAME.NAME_CLASS is
'(1)分为官方名称,别名,外来名或曾用名等类型
(2)NaviMap中,当为曾用名时,需记录曾用名时间(OLD_NAME_TIME)';

comment on column RD_LINK_NAME.INPUT_TIME is
'[170](1)当NAME_CLASS=3 时有效,其他为空
(2)记录方式为数据版本,如"10 夏"';

comment on column RD_LINK_NAME.NAME_TYPE is
'区分Junction Name,立交桥名(主路)';

comment on column RD_LINK_NAME.SRC_FLAG is
'注:当来自线门牌时,需判断Link 上是否有官方名,无则赋官方名;有则赋别名';

comment on column RD_LINK_NAME.ROUTE_ATT is
'(1)记录路线的上下行,内外环
(2)NaviMap数据转换中值为15的转为9(未定义)';

comment on column RD_LINK_NAME.CODE is
'注:当Link 种别为高速,城市高速,国道时,主从CODE=1,其他为0';

comment on column RD_LINK_NAME.U_RECORD is
'增量更新标识';

comment on column RD_LINK_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_ADDRESS                                       */
/*==============================================================*/
create table RD_LINK_ADDRESS  (
   RELATE_ID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   U_FIELDS             VARCHAR2(1000),
   LEFT_START           VARCHAR2(20),
   LEFT_END             VARCHAR2(20),
   LEFT_TYPE            NUMBER(1)                      default 0 not null
      constraint CKC_LEFT_TYPE_RD_LINK_ check (LEFT_TYPE in (0,1,2,3,4)) disable ,
   RIGHT_START          VARCHAR2(20),
   RIGHT_END            VARCHAR2(20),
   RIGHT_TYPE           NUMBER(1)                      default 0 not null
      constraint CKC_RIGHT_TYPE_RD_LINK_ check (RIGHT_TYPE in (0,1,2,3,4)) disable ,
   ADDRESS_TYPE         NUMBER(1)                      default 0 not null
      constraint CKC_ADDRESS_TYPE_RD_LINK_ check (ADDRESS_TYPE in (0,1,2,3)) disable ,
   WORK_DIR             NUMBER(1)                      default 0 not null
      constraint CKC_WORK_DIR_RD_LINK_ADDRESS check (WORK_DIR in (0,1,2)) disable ,
   SRC_FLAG             NUMBER(1)                      default 0 not null
      constraint CKC_SRC_FLAG_RD_LINKADDRESS check (SRC_FLAG in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)                      default 0 not null
      constraint CKC_U_RECORD_RD_LINK_ADDRESS check (U_RECORD in (0,1,2,3)) disable ,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   constraint RDLINKNAME_ADDRESS foreign key (RELATE_ID)
         references RD_LINK_NAME (RELATE_ID) disable 
);

comment on table RD_LINK_ADDRESS is
'如学院路(NAME_ID)10号到100号,学院路分为Link1,Link2,则表示Link1上的学院路10号到25号,Link2上的学院路26号到100号';

comment on column RD_LINK_ADDRESS.RELATE_ID is
'外键,引用"RD_LINK_NAME"';

comment on column RD_LINK_ADDRESS.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_ADDRESS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

comment on column RD_LINK_ADDRESS.LEFT_TYPE is
'单号,双号,混合号等';

comment on column RD_LINK_ADDRESS.RIGHT_TYPE is
'单号,双号,混合号等';

comment on column RD_LINK_ADDRESS.ADDRESS_TYPE is
'基本地址,原地址,商用地址';

comment on column RD_LINK_ADDRESS.WORK_DIR is
'门牌赋值的参考方向,如顺方向,逆方向';

comment on column RD_LINK_ADDRESS.SRC_FLAG is
'记录线门牌的来源,如点门牌,外业采集等';

comment on column RD_LINK_ADDRESS.U_RECORD is
'增量更新标识';

comment on column RD_LINK_ADDRESS.NAME_GROUPID is
'[170]参考"RD_NAME"';


/*==============================================================*/
/* Table: RD_LINK_FORM                                          */
/*==============================================================*/
create table RD_LINK_FORM 
(
   LINK_PID             NUMBER(10)           not null,
   FORM_OF_WAY          NUMBER(2)            default 1 not null
      constraint CKC_FORM_OF_WAY_RD_LINK_ check (FORM_OF_WAY in (0,1,2,10,11,12,13,14,15,16,17,18,20,21,22,23,24,30,31,32,33,34,35,36,37,38,39,48,49,50,51,52,53,54,57,60,80)) disable ,
   EXTENDED_FORM        NUMBER(2)            default 0 not null
      constraint CKC_EXTENDED_FORM_RD_LINK_ check (EXTENDED_FORM in (0,40,41,42)) disable ,
   AUXI_FLAG            NUMBER(2)            default 0 not null
      constraint CKC_AUXI_FLAG_RD_LINK_ check (AUXI_FLAG in (0,55,56,58,70,71,72,73,76,77)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
      constraint CKC_KG_FLAG_RD_LINK_FORM check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_FORM check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_FORM foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_LINK_FORM.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_FORM.EXTENDED_FORM is
'[171A]';

comment on column RD_LINK_FORM.AUXI_FLAG is
'[171A]';

comment on column RD_LINK_FORM.KG_FLAG is
'区分道路形态是K专用,G专用,KG共用的标志';

comment on column RD_LINK_FORM.U_RECORD is
'增量更新标识';

comment on column RD_LINK_FORM.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_LIMIT                                         */
/*==============================================================*/
create table RD_LINK_LIMIT 
(
   LINK_PID             NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 3 not null
      constraint CKC_TYPE_RD_LINK_LIMIT check (TYPE in (0,1,2,3,4,5,6,7)) disable ,
   LIMIT_DIR            NUMBER(1)            default 0 not null
      constraint CKC_LIMIT_DIR_RD_LINK_ check (LIMIT_DIR in (0,1,2,3,9)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   TOLL_TYPE            NUMBER(1)            default 9 not null
      constraint CKC_TOLL_TYPE_RD_LINK_ check (TOLL_TYPE in (0,1,2,3,4,5,6,9)) disable ,
   WEATHER              NUMBER(1)            default 9 not null
      constraint CKC_WEATHER_RD_LINK_ check (WEATHER in (0,1,2,3,9)) disable ,
   INPUT_TIME           VARCHAR2(32),
   PROCESS_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_PROCESS_FLAG_RD_LINK_LIMIT check (PROCESS_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_LIMIT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_LIMIT foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_LINK_LIMIT.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_LIMIT.LIMIT_DIR is
'双方向,顺方向,逆方向';

comment on column RD_LINK_LIMIT.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_LINK_LIMIT.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_LINK_LIMIT.TOLL_TYPE is
'当TYPE=6(Usage Fee Required)时有效,其他为9';

comment on column RD_LINK_LIMIT.WEATHER is
'[170]当TYPE=7(超车限制)且车辆类型为卡车时有效';

comment on column RD_LINK_LIMIT.INPUT_TIME is
'当TYPE=0或=4(施工中不开放或道路维修中)时有效,其他为空';

comment on column RD_LINK_LIMIT.PROCESS_FLAG is
'外业采集(人工赋值),未验证(程序赋值)';

comment on column RD_LINK_LIMIT.U_RECORD is
'增量更新标识';

comment on column RD_LINK_LIMIT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_RTIC                                          */
/*==============================================================*/
create table RD_LINK_RTIC 
(
   LINK_PID             NUMBER(10)           not null,
   CODE                 NUMBER(5)            default 0 not null,
   RANK                 NUMBER(1)            default 0 not null
      constraint CKC_RANK_RD_LINK_ check (RANK in (0,1,2,3,4)) disable ,
   RTIC_DIR             NUMBER(1)            default 0 not null
      constraint CKC_WORK_DIR_RD_LINK_RTIC check (RTIC_DIR in (0,1,2)) disable ,
   UPDOWN_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_UPDOWN_FLAG_RD_LINK_ check (UPDOWN_FLAG in (0,1)) disable ,
   RANGE_TYPE           NUMBER(1)            default 1 not null
      constraint CKC_RANGE_TYPE_RD_LINK_ check (RANGE_TYPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_RTIC check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_RTICS foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LINK_RTIC is
'记录道路LINK的RTIC实时交通信息';

comment on column RD_LINK_RTIC.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_RTIC.CODE is
'同一图幅内,同一RTIC等级的RTIC代码必须唯一,值域范围:1~4095';

comment on column RD_LINK_RTIC.RANK is
'高速,城市高速,干线道路';

comment on column RD_LINK_RTIC.RTIC_DIR is
'记录RTIC的赋值方向,如顺方向,逆方向';

comment on column RD_LINK_RTIC.UPDOWN_FLAG is
'(1)双方向道路两侧分别是上行和下行
(2)单向或上下线分离道路均为上行';

comment on column RD_LINK_RTIC.U_RECORD is
'增量更新标识';

comment on column RD_LINK_RTIC.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_SIDEWALK                                      */
/*==============================================================*/
create table RD_LINK_SIDEWALK 
(
   LINK_PID             NUMBER(10)           not null,
   SIDEWALK_LOC         NUMBER(1)            default 0 not null
      constraint CKC_SIDEWALK_LOC_RD_LINK_ check (SIDEWALK_LOC in (0,1,2,3,4,5,6,7,8)) disable ,
   DIVIDER_TYPE         NUMBER(1)            default 0 not null
      constraint CKC_DIVIDER_TYPE_RD_LINK_ check (DIVIDER_TYPE in (0,1,2,3,4)) disable ,
   WORK_DIR             NUMBER(1)            default 0 not null
      constraint CKC_WORK_DIR_RD_LINK_SIDEWALK check (WORK_DIR in (0,1,2)) disable ,
   PROCESS_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_PROCESS_FLAG_RD_LINKSIDE check (PROCESS_FLAG in (0,1)) disable ,
   CAPTURE_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_CAPTURE_FLAG_RD_LINK_ check (CAPTURE_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_SIDEWALK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_SIDEWALK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LINK_SIDEWALK is
'人行便道是位于道路两侧或一侧,路面铺设好的专供行人通行的道路';

comment on column RD_LINK_SIDEWALK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_SIDEWALK.SIDEWALK_LOC is
'记录Link所在位置,如右侧,中间,左侧';

comment on column RD_LINK_SIDEWALK.DIVIDER_TYPE is
'高度差隔离(马路涯),物理栅栏隔离,划线隔离等';

comment on column RD_LINK_SIDEWALK.WORK_DIR is
'人行便道赋值的参考方向,如顺方向,逆方向';

comment on column RD_LINK_SIDEWALK.PROCESS_FLAG is
'区分人工赋值,程序赋值等方式';

comment on column RD_LINK_SIDEWALK.CAPTURE_FLAG is
'记录采集状态,如未采集,外业确认';

comment on column RD_LINK_SIDEWALK.U_RECORD is
'增量更新标识';

comment on column RD_LINK_SIDEWALK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_SPEEDLIMIT                                    */
/*==============================================================*/
create table RD_LINK_SPEEDLIMIT 
(
   LINK_PID             NUMBER(10)           not null,
   FROM_SPEED_LIMIT     NUMBER(4)            default 0 not null,
   TO_SPEED_LIMIT       NUMBER(4)            default 0 not null,
   SPEED_CLASS          NUMBER(1)            default 0 not null
      constraint CKC_SPEED_CLASS_RD_LINK_ check (SPEED_CLASS between 0 and 8 and SPEED_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   FROM_LIMIT_SRC       NUMBER(2)            default 0 not null
      constraint CKC_FROM_LIMIT_SRC_RD_LINK_ check (FROM_LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)) disable ,
   TO_LIMIT_SRC         NUMBER(2)            default 0 not null
      constraint CKC_TO_LIMIT_SRC_RD_LINK_ check (TO_LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)) disable ,
   SPEED_TYPE           NUMBER(1)            default 0 not null
      constraint CKC_SPEED_TYPE_RD_LINK_ check (SPEED_TYPE in (0,1 ,2 ,3 )) disable ,
   SPEED_DEPENDENT      NUMBER(2)            default 0 not null
      constraint CKC_SPEED_DEPENDENT_RD_LINK_ check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  )) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   SPEED_CLASS_WORK     NUMBER(1)            default 0 not null
      constraint CKC_SPEED_CLASS_WORK_RD_LINK_ check (SPEED_CLASS_WORK in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINKSPEEDLIMIT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_SPEEDLIMIT foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_LINK_SPEEDLIMIT.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_SPEEDLIMIT.FROM_SPEED_LIMIT is
'(1)存储与Link同方向(即顺向)的最大限速
(2)限速值为整数,应用时除以10,单位是0.1km/h,默认为0';

comment on column RD_LINK_SPEEDLIMIT.TO_SPEED_LIMIT is
'(1)存储与Link反方向(即逆向)的最大限速
(2)限速值为整数,应用时除以10,单位是0.1km/h,默认为0';

comment on column RD_LINK_SPEEDLIMIT.FROM_LIMIT_SRC is
'记录顺向限速信息的来源,如城区标识,高速标识,车道限速,方向限速等';

comment on column RD_LINK_SPEEDLIMIT.TO_LIMIT_SRC is
'记录逆向限速信息的来源,如城区标识,高速标识,车道限速,方向限速等';

comment on column RD_LINK_SPEEDLIMIT.SPEED_TYPE is
'[170][172U]';

comment on column RD_LINK_SPEEDLIMIT.SPEED_DEPENDENT is
'[170][172U]';

comment on column RD_LINK_SPEEDLIMIT.TIME_DOMAIN is
'[170]格式参考"时间域"';

comment on column RD_LINK_SPEEDLIMIT.SPEED_CLASS_WORK is
'记录限速等级的赋值标记';

comment on column RD_LINK_SPEEDLIMIT.U_RECORD is
'增量更新标识';

comment on column RD_LINK_SPEEDLIMIT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_WALKSTAIR                                     */
/*==============================================================*/
create table RD_LINK_WALKSTAIR 
(
   LINK_PID             NUMBER(10)           not null,
   STAIR_LOC            NUMBER(1)            default 0 not null
      constraint CKC_STAIR_LOC_RD_LINK_ check (STAIR_LOC in (0,1,2,3,4,5,6,7)) disable ,
   STAIR_FLAG           NUMBER(1)            default 0 not null
      constraint CKC_STAIR_FLAG_RD_LINK_ check (STAIR_FLAG in (0,1,2)) disable ,
   WORK_DIR             NUMBER(1)            default 0 not null
      constraint CKC_WORK_DIR_RD_LINK_WALKSTAIR check (WORK_DIR in (0,1,2)) disable ,
   CAPTURE_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_CAPTURE_FLAG_RD_LINK_STAIR check (CAPTURE_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_WALKSTAIR check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_STAIR foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LINK_WALKSTAIR is
'阶梯有很多种存在形式:道路的属性,人行过道的到达方式,虚拟连接的连通障碍,公共交通出入口及站点的到达方式等';

comment on column RD_LINK_WALKSTAIR.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_WALKSTAIR.STAIR_LOC is
'人行阶梯所在位置,如右侧,中间,左侧';

comment on column RD_LINK_WALKSTAIR.STAIR_FLAG is
'上坡,下坡';

comment on column RD_LINK_WALKSTAIR.WORK_DIR is
'阶梯赋值的参考方向,如顺方向,逆方向';

comment on column RD_LINK_WALKSTAIR.CAPTURE_FLAG is
'记录采集状态,如未采集,外业确认';

comment on column RD_LINK_WALKSTAIR.U_RECORD is
'增量更新标识';

comment on column RD_LINK_WALKSTAIR.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_LINK_ZONE                                          */
/*==============================================================*/
create table RD_LINK_ZONE 
(
   LINK_PID             NUMBER(10)           not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_LINK_ZONE check (TYPE in (0,1,2,3)) disable ,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_RD_LINK_ check (SIDE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_LINK_ZONE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_ZONES foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_LINK_ZONE.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_ZONE.REGION_ID is
'[170]参考"AD_ADMIN",通过区域号码找对应的行政代码和ZONE 号码';

comment on column RD_LINK_ZONE.TYPE is
'AOIZone,KDZone,GCZone';

comment on column RD_LINK_ZONE.SIDE is
'记录Zone所在Link方向的左右侧';

comment on column RD_LINK_ZONE.U_RECORD is
'增量更新标识';

comment on column RD_LINK_ZONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_MAINSIDE                                           */
/*==============================================================*/
create table RD_MAINSIDE 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_MAINSIDE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_MAINSIDE primary key (GROUP_ID)
);

comment on column RD_MAINSIDE.GROUP_ID is
'主键';

comment on column RD_MAINSIDE.U_RECORD is
'增量更新标识';

comment on column RD_MAINSIDE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_MAINSIDE_LINK                                      */
/*==============================================================*/
create table RD_MAINSIDE_LINK 
(
   GROUP_ID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   LINK_TYPE            NUMBER(1)            default 0 not null
      constraint CKC_LINK_TYPE_RD_MAINS check (LINK_TYPE in (0,1)) disable ,
   SIDE                 NUMBER(1)            default 0 not null
      constraint CKC_SIDE_RD_MAINS check (SIDE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_MAINSIDE_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDMAINSIDE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDMAINSIDE foreign key (GROUP_ID)
         references RD_MAINSIDE (GROUP_ID) disable
);

comment on table RD_MAINSIDE_LINK is
'通过分隔带表达主辅路之间的关系';

comment on column RD_MAINSIDE_LINK.GROUP_ID is
'外键,引用"RD_MAINSIDE"';

comment on column RD_MAINSIDE_LINK.SEQ_NUM is
'左右侧分别从1开始递增编号';

comment on column RD_MAINSIDE_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_MAINSIDE_LINK.U_RECORD is
'增量更新标识';

comment on column RD_MAINSIDE_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_MULTIDIGITIZED                                     */
/*==============================================================*/
create table RD_MULTIDIGITIZED 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_MULTIDIGITIZED check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_MULTIDIGITIZED primary key (GROUP_ID)
);

comment on column RD_MULTIDIGITIZED.GROUP_ID is
'主键';

comment on column RD_MULTIDIGITIZED.U_RECORD is
'增量更新标识';

comment on column RD_MULTIDIGITIZED.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_MULTIDIGITIZED_LINK                                */
/*==============================================================*/
create table RD_MULTIDIGITIZED_LINK 
(
   GROUP_ID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   SIDE                 NUMBER(1)            default 1 not null
      constraint CKC_SIDE_RD_MULTI check (SIDE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RDMULTIDIGITIZEDL check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDMULTIDIGITIZED_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDMULTIDIGITIZED foreign key (GROUP_ID)
         references RD_MULTIDIGITIZED (GROUP_ID) disable
);

comment on table RD_MULTIDIGITIZED_LINK is
'(1)通过中央分隔带表达上下线分离道路之间的关系
(2)规格未明确,NaviMap编辑平台暂不实现';

comment on column RD_MULTIDIGITIZED_LINK.GROUP_ID is
'外键,引用"RD_MULTIDIGITIZED"';

comment on column RD_MULTIDIGITIZED_LINK.SEQ_NUM is
'左右侧分别从1 开始递增编号';

comment on column RD_MULTIDIGITIZED_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_MULTIDIGITIZED_LINK.SIDE is
'左侧或右侧';

comment on column RD_MULTIDIGITIZED_LINK.U_RECORD is
'增量更新标识';

comment on column RD_MULTIDIGITIZED_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NAME                                               */
/*==============================================================*/
create table RD_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_NAME check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(500),
   TYPE                 VARCHAR2(100),
   BASE                 VARCHAR2(100),
   PREFIX               VARCHAR2(100),
   INFIX                VARCHAR2(100),
   SUFFIX               VARCHAR2(100),
   NAME_PHONETIC        VARCHAR2(1000),
   TYPE_PHONETIC        VARCHAR2(1000),
   BASE_PHONETIC        VARCHAR2(1000),
   PREFIX_PHONETIC      VARCHAR2(1000),
   INFIX_PHONETIC       VARCHAR2(1000),
   SUFFIX_PHONETIC      VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RD_NAME check (SRC_FLAG in (0,1,2,3)) disable ,
   ROAD_TYPE            NUMBER(1)            default 0 not null
      constraint CKC_ROAD_TYPE_RD_NAME check (ROAD_TYPE in (0,1,2,3,4)) disable ,
   ADMIN_ID             NUMBER(6)            default 0 not null,
   CODE_TYPE            NUMBER(1)            default 0 not null
      constraint CKC_CODE_TYPE_RD_NAME check (CODE_TYPE in (0,1,2,3,4,5,6,7)) disable ,
   VOICE_FILE           VARCHAR2(100),
   SRC_RESUME           VARCHAR2(1000),
   PA_REGION_ID         NUMBER(10)           default 0 not null,
   MEMO                 VARCHAR2(200),
   ROUTE_ID             NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NAME primary key (NAME_ID)
);

comment on column RD_NAME.NAME_ID is
'主键';

comment on column RD_NAME.NAME_GROUPID is
'[171U][170]同PID分配原则';

comment on column RD_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_NAME.TYPE is
'大街,大道,胡同,环路,隧道等';

comment on column RD_NAME.PREFIX is
'东,西,南,北,中,前,后,左,右等';

comment on column RD_NAME.INFIX is
'[171U]';

comment on column RD_NAME.SUFFIX is
'东,西,南,北,中等';

comment on column RD_NAME.NAME_PHONETIC is
'[171U][170]';

comment on column RD_NAME.TYPE_PHONETIC is
'[171U][170]';

comment on column RD_NAME.BASE_PHONETIC is
'[171U][170]';

comment on column RD_NAME.PREFIX_PHONETIC is
'[171U][170]';

comment on column RD_NAME.INFIX_PHONETIC is
'[171U][170]';

comment on column RD_NAME.SUFFIX_PHONETIC is
'[171U][170]';

comment on column RD_NAME.SRC_FLAG is
'[180U]现仅指英文名来源';

comment on column RD_NAME.ROAD_TYPE is
'[170]记录来自不同MDB 文件的道路名';

comment on column RD_NAME.ADMIN_ID is
'[170]参考"AD_ADMIN"中"ADMIN_ID"';

comment on column RD_NAME.CODE_TYPE is
'[170]';

comment on column RD_NAME.VOICE_FILE is
'[171U][170]记录语音文件名( 无扩展名), 或参考"AU_MULTIMEDIA"中"NAME"';

comment on column RD_NAME.SRC_RESUME is
'[171A]记录道路名来源,类似一代道路名库中的“变更履历”字段.(1)来自点门牌,如“CG_MEN_南京”(2)来自外业更新,如“11 冬南京城区”
维护原则:一代UpdataroadName 和批点门牌时程序赋值,无人工维护,每版继承,且不出品';

comment on column RD_NAME.PA_REGION_ID is
'[171A]根据点门牌的道路名补充道路名库时,记录点门牌所属的行政区划号
维护原则: 批点门牌程序将IX_POINTADDRESS的区划号码(REGION_ID)赋值;无人工维护,每版继承,且不出品';

comment on column RD_NAME.MEMO is
'[170]区分主线/支线';

comment on column RD_NAME.ROUTE_ID is
'[170]保留原有RouteID';

comment on column RD_NAME.U_RECORD is
'增量更新标识';

comment on column RD_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NAME_PHOTO                                         */
/*==============================================================*/
create table RD_NAME_PHOTO 
(
   NAME_ID              NUMBER(10)           not null,
   PHOTO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NAME_PHOTO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNAME_PHOTO foreign key (NAME_ID)
         references RD_NAME (NAME_ID) disable
);

comment on table RD_NAME_PHOTO is
'道路名与外业照片成果之间的关系';

comment on column RD_NAME_PHOTO.NAME_ID is
'外键,引用"RD_NAME"';

comment on column RD_NAME_PHOTO.PHOTO_ID is
'[170]参考"AU_PHOTO"';

comment on column RD_NAME_PHOTO.STATUS is
'[170]记录是否确认';

comment on column RD_NAME_PHOTO.U_RECORD is
'增量更新标识';

comment on column RD_NAME_PHOTO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NAME_TONE                                          */
/*==============================================================*/
create table RD_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NAME_TONE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNAME_TONE foreign key (NAME_ID)
         references RD_NAME (NAME_ID) disable
);

comment on table RD_NAME_TONE is
'[170]';

comment on column RD_NAME_TONE.NAME_ID is
'外键,引用"RD_NAME"';

comment on column RD_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column RD_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column RD_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column RD_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column RD_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column RD_NAME_TONE.MEMO is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column RD_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column RD_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NODE_FORM                                          */
/*==============================================================*/
create table RD_NODE_FORM 
(
   NODE_PID             NUMBER(10)           not null,
   FORM_OF_WAY          NUMBER(2)            default 1 not null
      constraint CKC_FORM_OF_WAY_RD_NODE_ check (FORM_OF_WAY in (0,1,2,3,4,5,6,10,11,12,13,14,15,16,20,21,22,23,30,31,32,41)) disable ,
   AUXI_FLAG            NUMBER(2)            default 0 not null
      constraint CKC_AUXI_FLAG_RD_NODE_ check (AUXI_FLAG in (0,42,43)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NODE_FORM check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNODE_FORM foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_NODE_FORM.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_NODE_FORM.AUXI_FLAG is
'[171A]';

comment on column RD_NODE_FORM.U_RECORD is
'增量更新标识';

comment on column RD_NODE_FORM.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NODE_MESH                                          */
/*==============================================================*/
create table RD_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NODE_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNODE_MESH foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_NODE_MESH.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_NODE_MESH.U_RECORD is
'增量更新标识';

comment on column RD_NODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_NODE_NAME                                          */
/*==============================================================*/
create table RD_NODE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_NODE_ check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RD_NODE_ check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_NODE_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NODE_NAME primary key (NAME_ID),
   constraint RDNODE_NAME foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_NODE_NAME.NAME_ID is
'[170]主键';

comment on column RD_NODE_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_NODE_NAME.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_NODE_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_NODE_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_NODE_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column RD_NODE_NAME.U_RECORD is
'增量更新标识';

comment on column RD_NODE_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_OBJECT                                             */
/*==============================================================*/
create table RD_OBJECT 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_OBJECT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_OBJECT primary key (PID)
);

comment on column RD_OBJECT.PID is
'主键';

comment on column RD_OBJECT.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column RD_OBJECT.U_RECORD is
'增量更新标识';

comment on column RD_OBJECT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_OBJECT_INTER                                       */
/*==============================================================*/
create table RD_OBJECT_INTER 
(
   PID                  NUMBER(10)           not null,
   INTER_PID            NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_OBJECT_INTER check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_INTERSECTION foreign key (INTER_PID)
         references RD_INTER (PID) disable ,
   constraint RDOBJECT_INTERSECTIONS foreign key (PID)
         references RD_OBJECT (PID) disable
);

comment on column RD_OBJECT_INTER.PID is
'外键,引用"RD_OBJECT"';

comment on column RD_OBJECT_INTER.INTER_PID is
'外键,引用"RD_INTER"';

comment on column RD_OBJECT_INTER.U_RECORD is
'增量更新标识';

comment on column RD_OBJECT_INTER.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_OBJECT_LINK                                        */
/*==============================================================*/
create table RD_OBJECT_LINK 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_OBJECT_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDOBJECT_LINKS foreign key (PID)
         references RD_OBJECT (PID) disable
);

comment on column RD_OBJECT_LINK.PID is
'外键,引用"RD_OBJECT"';

comment on column RD_OBJECT_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_OBJECT_LINK.U_RECORD is
'增量更新标识';

comment on column RD_OBJECT_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_OBJECT_NAME                                        */
/*==============================================================*/
create table RD_OBJECT_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_OBJEC check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RD_OBJEC check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_OBJECT_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_OBJECT_NAME primary key (NAME_ID),
   constraint RDOBJECT_NAME foreign key (PID)
         references RD_OBJECT (PID) disable
);

comment on column RD_OBJECT_NAME.NAME_ID is
'[170]主键';

comment on column RD_OBJECT_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_OBJECT_NAME.PID is
'外键,引用"RD_OBJECT"';

comment on column RD_OBJECT_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_OBJECT_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_OBJECT_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column RD_OBJECT_NAME.U_RECORD is
'增量更新标识';

comment on column RD_OBJECT_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_OBJECT_NODE                                        */
/*==============================================================*/
create table RD_OBJECT_NODE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_OBJECT_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_NODES foreign key (PID)
         references RD_OBJECT (PID) disable ,
   constraint RDOBJECT_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_OBJECT_NODE.PID is
'外键,引用"RD_OBJECT"';

comment on column RD_OBJECT_NODE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_OBJECT_NODE.U_RECORD is
'增量更新标识';

comment on column RD_OBJECT_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_ROAD                                               */
/*==============================================================*/
create table RD_ROAD 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_ROAD check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_ROAD primary key (PID)
);

comment on column RD_ROAD.PID is
'主键';

comment on column RD_ROAD.U_RECORD is
'增量更新标识';

comment on column RD_ROAD.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_OBJECT_ROAD                                        */
/*==============================================================*/
create table RD_OBJECT_ROAD 
(
   PID                  NUMBER(10)           not null,
   ROAD_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_OBJECT_ROAD check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_ROAD foreign key (ROAD_PID)
         references RD_ROAD (PID) disable ,
   constraint RDOBJECT_ROADS foreign key (PID)
         references RD_OBJECT (PID) disable
);

comment on column RD_OBJECT_ROAD.PID is
'外键,引用"RD_OBJECT"';

comment on column RD_OBJECT_ROAD.ROAD_PID is
'外键,引用"RD_ROAD"';

comment on column RD_OBJECT_ROAD.U_RECORD is
'增量更新标识';

comment on column RD_OBJECT_ROAD.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_RESTRICTION                                        */
/*==============================================================*/
create table RD_RESTRICTION 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   RESTRIC_INFO         VARCHAR2(64),
   KG_FLAG              NUMBER(1)            default 0 not null
      constraint CKC_KG_FLAG_RD_RESTRICDETAIL check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_RESTRICTION check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_RESTRICTION primary key (PID),
   constraint RDRESTRICT_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDRESTRICTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_RESTRICTION is
'记录交限的进入线点关系,以及从该线点出发到退出Link上的所有限制信息';

comment on column RD_RESTRICTION.PID is
'主键';

comment on column RD_RESTRICTION.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_RESTRICTION.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_RESTRICTION.RESTRIC_INFO is
'记录从进入LINK到所有退出LINK的交限信息
(1)由一个或多个英文半角限制代码构成,内容与RD_RESTRICTION_DETAIL一致,如:"1"表示禁左
(2)组合交限用英文半角","分割,如"1,2"表示"禁直和禁左"
(3)默认表示实际交限,如果是理论交限或未验证,则将具体限制代码用英文半角"[]"括起来,如"[2]",表示理论禁左';

comment on column RD_RESTRICTION.KG_FLAG is
'区分交限信息是K专用,G专用,KG共用的标志';

comment on column RD_RESTRICTION.U_RECORD is
'增量更新标识';

comment on column RD_RESTRICTION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_RESTRICTION_DETAIL                                 */
/*==============================================================*/
create table RD_RESTRICTION_DETAIL 
(
   DETAIL_ID            NUMBER(10)           not null,
   RESTRIC_PID          NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   FLAG                 NUMBER(1)            default 2 not null
      constraint CKC_FLAG_RD_RESTR check (FLAG in (0,1,2)) disable ,
   RESTRIC_INFO         NUMBER(1)            default 0 not null
      constraint CKC_RESTRIC_INFO_RD_RESTRICT_D check (RESTRIC_INFO in (0,1,2,3,4)) disable ,
   TYPE                 NUMBER(1)            default 1 not null
      constraint CKC_TYPE_RD_RESTR check (TYPE in (0,1,2)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
      constraint CKC_RELATIONSHIP_TYPE_RD_RESTR check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_RESTRICTION_D check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_RESTRICTION_DETAIL primary key (DETAIL_ID),
   constraint RDRESTRIC_DETAIL foreign key (RESTRIC_PID)
         references RD_RESTRICTION (PID) disable ,
   constraint RDRESTRICTDETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_RESTRICTION_DETAIL is
'记录当前退出Link上具体的限制类型,限制信息(禁直,禁左等)';

comment on column RD_RESTRICTION_DETAIL.DETAIL_ID is
'主键';

comment on column RD_RESTRICTION_DETAIL.RESTRIC_PID is
'外键,引用"RD_RESTRICTION"';

comment on column RD_RESTRICTION_DETAIL.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_RESTRICTION_DETAIL.FLAG is
'未验证,实地交限,理论交限';

comment on column RD_RESTRICTION_DETAIL.RESTRIC_INFO is
'[180U]记录当前退出Link上的禁直,禁左,禁右,禁调等信息';

comment on column RD_RESTRICTION_DETAIL.TYPE is
'禁止进入,时间段禁止(进入Link到退出Link在时间段内禁止,其他时间可通行)';

comment on column RD_RESTRICTION_DETAIL.U_RECORD is
'增量更新标识';

comment on column RD_RESTRICTION_DETAIL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_RESTRICTION_CONDITION                              */
/*==============================================================*/
create table RD_RESTRICTION_CONDITION 
(
   DETAIL_ID            NUMBER(10)           not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_RESTRICTION_C check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDRESTRICCONDITION_DETAIL foreign key (DETAIL_ID)
         references RD_RESTRICTION_DETAIL (DETAIL_ID) disable
);

comment on table RD_RESTRICTION_CONDITION is
'记录当前退出Link上的时间段和车辆限制信息';

comment on column RD_RESTRICTION_CONDITION.DETAIL_ID is
'外键,引用"RD_RESTRICTION_DETAIL"';

comment on column RD_RESTRICTION_CONDITION.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_RESTRICTION_CONDITION.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_RESTRICTION_CONDITION.U_RECORD is
'增量更新标识';

comment on column RD_RESTRICTION_CONDITION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_RESTRICTION_VIA                                    */
/*==============================================================*/
create table RD_RESTRICTION_VIA 
(
   DETAIL_ID            NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   GROUP_ID             NUMBER(2)            default 1 not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_RESTRICTION_V check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDRESTRICTION_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDRESTRICVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_RESTRICTION_DETAIL (DETAIL_ID) disable
);

comment on table RD_RESTRICTION_VIA is
'(1)表达同一进入Link和退出Link之间的多组经过Link,而且经过Link不包括进入Link和退出Link
(1)NaviMap作业中,当进入和退出Link直接在同一路口挂接时,不制作经过Link;否则(如线线关系),需要制作经过Link';

comment on column RD_RESTRICTION_VIA.DETAIL_ID is
'外键,引用"RD_RESTRICTION_DETAIL"';

comment on column RD_RESTRICTION_VIA.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_RESTRICTION_VIA.GROUP_ID is
'从1开始递增编号';

comment on column RD_RESTRICTION_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_RESTRICTION_VIA.U_RECORD is
'增量更新标识';

comment on column RD_RESTRICTION_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_ROAD_LINK                                          */
/*==============================================================*/
create table RD_ROAD_LINK 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_ROAD_ check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDROAD_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDROAD_LINKS foreign key (PID)
         references RD_ROAD (PID) disable
);

comment on column RD_ROAD_LINK.PID is
'外键,引用"RD_ROAD"';

comment on column RD_ROAD_LINK.SEQ_NUM is
'从1开始递增编号';

comment on column RD_ROAD_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_ROAD_LINK.U_RECORD is
'增量更新标识';

comment on column RD_ROAD_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SAMELINK                                           */
/*==============================================================*/
create table RD_SAMELINK 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SAMELINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SAMELINK primary key (GROUP_ID)
);

comment on column RD_SAMELINK.GROUP_ID is
'主键';

comment on column RD_SAMELINK.U_RECORD is
'增量更新标识';

comment on column RD_SAMELINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SAMELINK_PART                                      */
/*==============================================================*/
create table RD_SAMELINK_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SAMELINK_PART check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSAMELINK_PARTS foreign key (GROUP_ID)
         references RD_SAMELINK (GROUP_ID) disable
);

comment on table RD_SAMELINK_PART is
'记录同一Link关系的组成Link信息';

comment on column RD_SAMELINK_PART.GROUP_ID is
'外键,引用"RD_SAMELINK"';

comment on column RD_SAMELINK_PART.LINK_PID is
'参考"RD_LINK","AD_LINK"等';

comment on column RD_SAMELINK_PART.TABLE_NAME is
'记录LINK所在的数据表,如道路LINK=20与行政区划LINK=40为同一LINK时,数据表名分别为"RD_LINK"和"AD_LINK"';

comment on column RD_SAMELINK_PART.U_RECORD is
'增量更新标识';

comment on column RD_SAMELINK_PART.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SAMENODE                                           */
/*==============================================================*/
create table RD_SAMENODE 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SAMENODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SAMENODE primary key (GROUP_ID)
);

comment on column RD_SAMENODE.GROUP_ID is
'主键';

comment on column RD_SAMENODE.U_RECORD is
'增量更新标识';

comment on column RD_SAMENODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SAMENODE_PART                                      */
/*==============================================================*/
create table RD_SAMENODE_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SAMENODE_PART check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSAMENODE_PARTS foreign key (GROUP_ID)
         references RD_SAMENODE (GROUP_ID) disable
);

comment on table RD_SAMENODE_PART is
'记录同一Node关系的组成Node信息';

comment on column RD_SAMENODE_PART.GROUP_ID is
'外键,引用"RD_SAMENODE"';

comment on column RD_SAMENODE_PART.NODE_PID is
'参考"RD_NODE","AD_NODE"等';

comment on column RD_SAMENODE_PART.TABLE_NAME is
'记录NODE所在的数据表,如道路NODE=20与行政区划NODE=40为同一NODE时,数据表名分别为"RD_NODE"和"AD_NODE"';

comment on column RD_SAMENODE_PART.U_RECORD is
'增量更新标识';

comment on column RD_SAMENODE_PART.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SE                                                 */
/*==============================================================*/
create table RD_SE 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SE primary key (PID),
   constraint RDSE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_SE is
'NaviMap制作时进入Link和退出Link必须接续';

comment on column RD_SE.PID is
'主键';

comment on column RD_SE.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_SE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_SE.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_SE.U_RECORD is
'增量更新标识';

comment on column RD_SE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SERIESBRANCH                                       */
/*==============================================================*/
create table RD_SERIESBRANCH 
(
   BRANCH_PID           NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_SERIE check (TYPE in (0,1)) disable ,
   VOICE_DIR            NUMBER(1)            default 0 not null
      constraint CKC_VOICE_DIR_RD_SERIE check (VOICE_DIR in (0,2,5)) disable ,
   PATTERN_CODE         VARCHAR2(10),
   ARROW_CODE           VARCHAR2(10),
   ARROW_FLAG           NUMBER(2)            default 0 not null
      constraint CKC_ARROW_FLAG_RD_SERIE check (ARROW_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SERIE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSERIESBRANCH foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

comment on table RD_SERIESBRANCH is
'表达连续分歧的线点关系(如线线关系)和分歧模式图信息
';

comment on column RD_SERIESBRANCH.BRANCH_PID is
'外键,引用"RD_BRANCH"';

comment on column RD_SERIESBRANCH.VOICE_DIR is
'无,右,左';

comment on column RD_SERIESBRANCH.PATTERN_CODE is
'参考"AU_MULTIMEDIA"中"NAME",如:8a430211';

comment on column RD_SERIESBRANCH.ARROW_CODE is
'参考"AU_MULTIMEDIA"中"NAME",如:0a24030a';

comment on column RD_SERIESBRANCH.ARROW_FLAG is
'[180A]';

comment on column RD_SERIESBRANCH.U_RECORD is
'增量更新标识';

comment on column RD_SERIESBRANCH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SIGNASREAL                                         */
/*==============================================================*/
create table RD_SIGNASREAL 
(
   SIGNBOARD_ID         NUMBER(10)           not null,
   BRANCH_PID           NUMBER(10)           not null,
   LANE_SEQ             VARCHAR2(16),
   IMAGE_CODE           VARCHAR2(16),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNASREAL primary key (SIGNBOARD_ID),
   constraint RDBRANCH_SIGNASREAL foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

comment on table RD_SIGNASREAL is
'[170]';

comment on column RD_SIGNASREAL.SIGNBOARD_ID is
'主键';

comment on column RD_SIGNASREAL.BRANCH_PID is
'外键,引用"RD_BRANCH"';

comment on column RD_SIGNASREAL.LANE_SEQ is
'[171A]记录实景看板中不同面板对应的车道序号';

comment on column RD_SIGNASREAL.IMAGE_CODE is
'同箭头图代码,SVG 格式';

comment on column RD_SIGNASREAL.U_RECORD is
'增量更新标识';

comment on column RD_SIGNASREAL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SIGNBOARD                                          */
/*==============================================================*/
create table RD_SIGNBOARD 
(
   SIGNBOARD_ID         NUMBER(10)           not null,
   BRANCH_PID           NUMBER(10)           not null,
   ARROW_CODE           VARCHAR2(16),
   BACKIMAGE_CODE       VARCHAR2(16),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNBOARD check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNBOARD primary key (SIGNBOARD_ID),
   constraint RDBRANCH_SIGNBOARD foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

comment on column RD_SIGNBOARD.SIGNBOARD_ID is
'主键';

comment on column RD_SIGNBOARD.BRANCH_PID is
'外键,引用"RD_BRANCH"';

comment on column RD_SIGNBOARD.ARROW_CODE is
'参考"AU_MULTIMEDIA"中"NAME"';

comment on column RD_SIGNBOARD.BACKIMAGE_CODE is
'同箭头图代码,均为11 位编码';

comment on column RD_SIGNBOARD.U_RECORD is
'增量更新标识';

comment on column RD_SIGNBOARD.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SIGNBOARD_NAME                                     */
/*==============================================================*/
create table RD_SIGNBOARD_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   SIGNBOARD_ID         NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   NAME_CLASS           NUMBER(1)            default 0 not null
      constraint CKC_NAME_CLASS_RD_SIGNB check (NAME_CLASS in (0,1)) disable ,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_SIGNB check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   CODE_TYPE            NUMBER(2)            default 0 not null
      constraint CKC_CODE_TYPE_RD_SIGNB check (CODE_TYPE in (0,1,2,3,4,5,6,7,8,9,10)) disable ,
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   VOICE_FILE           VARCHAR2(100),
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RD_SIGNB check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNBOARD_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNBOARD_NAME primary key (NAME_ID),
   constraint RDSIGNBOARD_NAME foreign key (SIGNBOARD_ID)
         references RD_SIGNBOARD (SIGNBOARD_ID) disable
);

comment on column RD_SIGNBOARD_NAME.NAME_ID is
'[170]主键';

comment on column RD_SIGNBOARD_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_SIGNBOARD_NAME.SIGNBOARD_ID is
'外键,引用"RD_SIGNBOARD"';

comment on column RD_SIGNBOARD_NAME.SEQ_NUM is
'从1开始递增编号';

comment on column RD_SIGNBOARD_NAME.NAME_CLASS is
'方向,出口';

comment on column RD_SIGNBOARD_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_SIGNBOARD_NAME.CODE_TYPE is
'普通道路名,设施名,高速道路名等';

comment on column RD_SIGNBOARD_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_SIGNBOARD_NAME.VOICE_FILE is
'[170]参考"AU_MULTIMEDIA"中"NAME"';

comment on column RD_SIGNBOARD_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column RD_SIGNBOARD_NAME.U_RECORD is
'增量更新标识';

comment on column RD_SIGNBOARD_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SIGNBOARD_NAME_TONE                                */
/*==============================================================*/
create table RD_SIGNBOARD_NAME_TONE 
(
   NAME_ID              NUMBER(10)           not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNBOARDNAMET check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSIGNBOARDNAME_TONE foreign key (NAME_ID)
         references RD_SIGNBOARD_NAME (NAME_ID) disable
);

comment on table RD_SIGNBOARD_NAME_TONE is
'[170]';

comment on column RD_SIGNBOARD_NAME_TONE.NAME_ID is
'外键,引用"RD_SIGNBOARD_NAME"';

comment on column RD_SIGNBOARD_NAME_TONE.TONE_A is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column RD_SIGNBOARD_NAME_TONE.TONE_B is
'汉语名称中的数字将转成拼音';

comment on column RD_SIGNBOARD_NAME_TONE.LH_A is
'对应带声调拼音1,转出LH+';

comment on column RD_SIGNBOARD_NAME_TONE.LH_B is
'对应带声调拼音2,转出LH+';

comment on column RD_SIGNBOARD_NAME_TONE.JYUTP is
'制作普通话时本字段为空值';

comment on column RD_SIGNBOARD_NAME_TONE.MEMO is
'汉语名称对应的带声调拼音(目前为汉语拼音和粤语拼音),数字和字母不转,以书面语为准';

comment on column RD_SIGNBOARD_NAME_TONE.U_RECORD is
'增量更新标识';

comment on column RD_SIGNBOARD_NAME_TONE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SIGNPOST                                           */
/*==============================================================*/
create table RD_SIGNPOST 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   TYPE_CODE            VARCHAR2(5),
   ANGLE                NUMBER(5,2)          default 0 not null,
   POSITION             NUMBER(2)            default 0 not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   DESCRIPT             VARCHAR2(100),
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNPOST check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNPOST primary key (PID)
);

comment on table RD_SIGNPOST is
'记录标牌编码,点位,朝向等.当主辅标牌共存时,记录主标牌的PID及相关信息,同时记录辅标牌的信息';

comment on column RD_SIGNPOST.PID is
'主键';

comment on column RD_SIGNPOST.LINK_PID is
'参考"RD_LINK"';

comment on column RD_SIGNPOST.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column RD_SIGNPOST.TYPE_CODE is
'参考"RD_SIGNPOST_CODE"';

comment on column RD_SIGNPOST.ANGLE is
'标牌看板与正北方向的夹角,单位:度,值域范围:0.00~360.00';

comment on column RD_SIGNPOST.POSITION is
'[172U]采用3bit表示,从右到左依次为0~2bit,每bit表示一个位置(如下),赋值为0/1分别表示否/是,如: 101表示左和上
第0bit:左(Left)
第1bit:右(Right)
第2bit:上(Overhead)
如果所有bit位均为0,表示未调查';

comment on column RD_SIGNPOST.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_SIGNPOST.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_SIGNPOST.DESCRIPT is
'记录现场标牌中解释说明的文字';

comment on column RD_SIGNPOST.U_RECORD is
'增量更新标识';

comment on column RD_SIGNPOST.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';



/*==============================================================*/
/* Table: RD_SIGNPOST_LINK                                      */
/*==============================================================*/
create table RD_SIGNPOST_LINK 
(
   SIGNPOST_PID         NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNPOST_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSIGNPOST_CONTROLLINK foreign key (SIGNPOST_PID)
         references RD_SIGNPOST (PID) disable
);

comment on table RD_SIGNPOST_LINK is
'
';

comment on column RD_SIGNPOST_LINK.SIGNPOST_PID is
'外键,引用"RD_SIGNPOST"';

comment on column RD_SIGNPOST_LINK.U_RECORD is
'增量更新标识';

comment on column RD_SIGNPOST_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SIGNPOST_PHOTO                                     */
/*==============================================================*/
create table RD_SIGNPOST_PHOTO 
(
   SIGNPOST_PID         NUMBER(10)           not null,
   PHOTO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SIGNPOST_PHOTO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSIGNPOST_PHOTOES foreign key (SIGNPOST_PID)
         references RD_SIGNPOST (PID) disable
);

comment on table RD_SIGNPOST_PHOTO is
'GDB标牌与外业照片成果之间的关系';

comment on column RD_SIGNPOST_PHOTO.SIGNPOST_PID is
'外键,引用"RD_SIGNPOST"';

comment on column RD_SIGNPOST_PHOTO.PHOTO_ID is
'参考"AU_PHOTO"';

comment on column RD_SIGNPOST_PHOTO.STATUS is
'记录是否确认';

comment on column RD_SIGNPOST_PHOTO.U_RECORD is
'增量更新标识';

comment on column RD_SIGNPOST_PHOTO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SLOPE                                              */
/*==============================================================*/
create table RD_SLOPE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 1 not null
      constraint CKC_TYPE_RD_SLOPE check (TYPE in (0,1,2,3)) disable ,
   ANGLE                NUMBER(2)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SLOPE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SLOPE primary key (PID),
   constraint RDSLOPE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSLOPE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_SLOPE.PID is
'主键';

comment on column RD_SLOPE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_SLOPE.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_SLOPE.TYPE is
'水平,上坡,下坡';

comment on column RD_SLOPE.ANGLE is
'单位:度,值域范围:0~90,默认为0';

comment on column RD_SLOPE.U_RECORD is
'增量更新标识';

comment on column RD_SLOPE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SPEEDLIMIT                                         */
/*==============================================================*/
create table RD_SPEEDLIMIT 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SPEED_VALUE          NUMBER(4)            default 0 not null,
   SPEED_TYPE           NUMBER(1)            default 0 not null
      constraint CKC_SPEED_TYPE_RD_SPEED check (SPEED_TYPE in (0,1 ,2 ,3 )) disable ,
   SPEED_DEPENDENT      NUMBER(2)            default 0 not null
      constraint CKC_SPEED_DEPENDENT_RD_SPEED check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  )) disable ,
   SPEED_FLAG           NUMBER(1)            default 0 not null
      constraint CKC_SPEED_FLAG_RD_SPEED check (SPEED_FLAG in (0,1)) disable ,
   LIMIT_SRC            NUMBER(2)            default 1 not null
      constraint CKC_LIMIT_SRC_RD_SPEED check (LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)) disable ,
   CAPTURE_FLAG         NUMBER(1)            default 0 not null
      constraint CKC_CAPTURE_FLAG_RD_SPEED check (CAPTURE_FLAG in (0,1)) disable ,
   DESCRIPT             VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_SPEED check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SPEEDLIMIT primary key (PID),
   constraint RDSPEEDLIMIT_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSPEEDLIMIT_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_SPEEDLIMIT is
'(1)图幅内的"点线"关系,而不是"点线线"关系
(2)限速关系不跨图幅,限速值在关系上和Link限速属性中分别记录
(2)不记录"限速"与"解限速"之间的成对关系';

comment on column RD_SPEEDLIMIT.PID is
'主键';

comment on column RD_SPEEDLIMIT.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_SPEEDLIMIT.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_SPEEDLIMIT.SPEED_VALUE is
'记录最高限速值,值域范围:1~9999,单位:百米/时,应用时需除以10';

comment on column RD_SPEEDLIMIT.SPEED_TYPE is
'[170][172U]';

comment on column RD_SPEEDLIMIT.SPEED_DEPENDENT is
'[170][172U]';

comment on column RD_SPEEDLIMIT.SPEED_FLAG is
'限速开始或限速解除';

comment on column RD_SPEEDLIMIT.LIMIT_SRC is
'记录限速信息的来源,如城区标识,高速标识,车道限速,方向限速等';

comment on column RD_SPEEDLIMIT.CAPTURE_FLAG is
'理论判断或现场采集';

comment on column RD_SPEEDLIMIT.U_RECORD is
'增量更新标识';

comment on column RD_SPEEDLIMIT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TMCLOCATION                                        */
/*==============================================================*/
create table RD_TMCLOCATION 
(
   GROUP_ID             NUMBER(10)           not null,
   TMC_ID               NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TMCLOCATION check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TMCLOCATION primary key (GROUP_ID)
);

comment on column RD_TMCLOCATION.GROUP_ID is
'主键';

comment on column RD_TMCLOCATION.TMC_ID is
'[170]参考"TMC_POINT","TMC_LINE","TMC_AREA"';

comment on column RD_TMCLOCATION.U_RECORD is
'增量更新标识';

comment on column RD_TMCLOCATION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TMCLOCATION_LINK                                   */
/*==============================================================*/
create table RD_TMCLOCATION_LINK 
(
   GROUP_ID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   LOC_DIRECT           NUMBER(1)            default 0 not null
      constraint CKC_LOC_DIRECT_RD_TMCLO check (LOC_DIRECT in (0,1,2,3,4)) disable ,
   DIRECT               NUMBER(1)            default 0 not null
      constraint CKC_DIRECT_RD_TMCLO check (DIRECT in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TMCLOCATION_L check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDTMCLOCATION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDTMCLOCATION_LINKS foreign key (GROUP_ID)
         references RD_TMCLOCATION (GROUP_ID) disable
);

comment on column RD_TMCLOCATION_LINK.GROUP_ID is
'外键,引用"RD_TMCLOCATION"';

comment on column RD_TMCLOCATION_LINK.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_TMCLOCATION_LINK.DIRECT is
'TMC位置方向与道路LINK方向的关系';

comment on column RD_TMCLOCATION_LINK.U_RECORD is
'增量更新标识';

comment on column RD_TMCLOCATION_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TOLLGATE                                           */
/*==============================================================*/
create table RD_TOLLGATE 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_TOLLGATE check (TYPE in (0,1,2,3,4,5,6,7,8,9,10)) disable ,
   PASSAGE_NUM          NUMBER(2)            default 0 not null,
   ETC_FIGURE_CODE      VARCHAR2(8),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TOLLGATE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TOLLGATE primary key (PID),
   constraint RDTOLLGATE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDTOLLGATE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDTOLLGATE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on table RD_TOLLGATE is
'(1)表达收费站的收费类型和ETC车道数
(2)注意:进入LINK和退出LINK必须接续';

comment on column RD_TOLLGATE.PID is
'主键';

comment on column RD_TOLLGATE.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_TOLLGATE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_TOLLGATE.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_TOLLGATE.TYPE is
'收费,领卡等类型';

comment on column RD_TOLLGATE.PASSAGE_NUM is
'包括ETC通道数在内的通道总数';

comment on column RD_TOLLGATE.ETC_FIGURE_CODE is
'参考"AU_MULTIMEDIA"中"NAME"';

comment on column RD_TOLLGATE.U_RECORD is
'增量更新标识';

comment on column RD_TOLLGATE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TOLLGATE_COST                                      */
/*==============================================================*/
create table RD_TOLLGATE_COST 
(
   TOLLCOST_ID          NUMBER(10)           not null,
   IN_TOLLGATE          NUMBER(10)           not null,
   OUT_TOLLGATE         NUMBER(10)           not null,
   FEE                  NUMBER(10,2)         default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_TOLLGATE_COST check (TYPE in (0,1)) disable ,
   CASH                 VARCHAR2(3)          default 'CNY' not null
      constraint CKC_CASH_TOLLCOST check (CASH in ('CNY','HKD','MOP')) disable ,
   VEHICLE_CLASS        NUMBER(1)            default 1 not null
      constraint CKC_VEHICLE_CLASS_TOLLCOST check (VEHICLE_CLASS in (1,2,3,4,5)) disable ,
   DISTANCE             NUMBER(10,4)         default 0 not null,
   SYSTEM_ID            NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TOLLGATE_COST check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_COST primary key (TOLLCOST_ID)
);

comment on table RD_TOLLGATE_COST is
'[180]由IDB通过RD_TOLLGATE_MAPPING和RD_TOLLGATE_FEE生成';

comment on column RD_TOLLGATE_COST.TOLLCOST_ID is
'[180U]主键,取自"RD_TOLLGATE_FEE"';

comment on column RD_TOLLGATE_COST.IN_TOLLGATE is
'参考"RD_TOLLGATE"';

comment on column RD_TOLLGATE_COST.OUT_TOLLGATE is
'参考"RD_TOLLGATE"';

comment on column RD_TOLLGATE_COST.VEHICLE_CLASS is
'按照交通部收费公路车辆通行费车型分类标准,分为1~5 类,';

comment on column RD_TOLLGATE_COST.U_RECORD is
'增量更新标识';

comment on column RD_TOLLGATE_COST.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TOLLGATE_FEE                                       */
/*==============================================================*/
create table RD_TOLLGATE_FEE 
(
   TOLLCOST_ID          NUMBER(10)           not null,
   S_TOLL_OLD           VARCHAR2(1000)       not null,
   E_TOLL_OLD           VARCHAR2(1000)       not null,
   S_TOLL_NEW           VARCHAR2(1000)       not null,
   E_TOLL_NEW           VARCHAR2(1000)       not null,
   S_MAPPINGID          NUMBER(10)           default 0 not null,
   E_MAPPINGID          NUMBER(10)           default 0 not null,
   CASH                 VARCHAR2(3)          default 'CNY' not null
      constraint CKC_CASH_TOLLFEE check (CASH in ('CNY','HKD','MOP')) disable ,
   FEE                  NUMBER(10,2)         default 0 not null,
   VEHICLE_CLASS        NUMBER(1)            default 1 not null
      constraint CKC_VEHICLE_CLASS_TOLLFEE check (VEHICLE_CLASS in (1,2,3,4,5)) disable ,
   DISTANCE             NUMBER(10,4)         default 0 not null,
   FLAG                 VARCHAR2(100),
   SYSTEM_ID            NUMBER(6),
   VER_INFO             VARCHAR2(100),
   MEMO                 VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_FEE primary key (TOLLCOST_ID)
);

comment on table RD_TOLLGATE_FEE is
'[180]仅用于在线编辑平台';

comment on column RD_TOLLGATE_FEE.TOLLCOST_ID is
'[180A]主键';

comment on column RD_TOLLGATE_FEE.S_TOLL_OLD is
'取自"基础数据表"';

comment on column RD_TOLLGATE_FEE.E_TOLL_OLD is
'取自"基础数据表"';

comment on column RD_TOLLGATE_FEE.S_MAPPINGID is
'参考"RD_TOLLGATE_MAPPING"';

comment on column RD_TOLLGATE_FEE.E_MAPPINGID is
'参考"RD_TOLLGATE_MAPPING"';

comment on column RD_TOLLGATE_FEE.VEHICLE_CLASS is
'按照交通部收费公路车辆通行费车型分类标准,分为1~5 类,';

/*==============================================================*/
/* Table: RD_TOLLGATE_MAPPING                                   */
/*==============================================================*/
create table RD_TOLLGATE_MAPPING 
(
   MAPPING_ID           NUMBER(10)           not null,
   SE_TOLL_OLD          VARCHAR2(1000),
   SE_TOLL_NEW          VARCHAR2(1000),
   GDB_TOLL_PID         NUMBER(10)           default 0 not null,
   GDB_TOLL_NAME        VARCHAR2(1000),
   GDB_TOLL_NODEID      NUMBER(10)           default 0 not null,
   FLAG                 VARCHAR2(100),
   SYSTEM_ID            NUMBER(6),
   VER_INFO             VARCHAR2(100),
   MEMO                 VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_MAPPING primary key (MAPPING_ID)
);

comment on table RD_TOLLGATE_MAPPING is
'[180]仅用于在线编辑平台';

comment on column RD_TOLLGATE_MAPPING.MAPPING_ID is
'主键';

comment on column RD_TOLLGATE_MAPPING.SE_TOLL_OLD is
'取自"基础数据表"';

comment on column RD_TOLLGATE_MAPPING.SE_TOLL_NEW is
'原始起点和终点+序号(系统从1开始依次递增编号）';

comment on column RD_TOLLGATE_MAPPING.GDB_TOLL_PID is
'参考"RD_TOLLGATE"';

comment on column RD_TOLLGATE_MAPPING.GDB_TOLL_NAME is
'参考"RD_TOLLGATE_NAME"';

comment on column RD_TOLLGATE_MAPPING.GDB_TOLL_NODEID is
'参考"RD_TOLLGATE"中的"NODE_PID"';

/*==============================================================*/
/* Table: RD_TOLLGATE_NAME                                      */
/*==============================================================*/
create table RD_TOLLGATE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_TOLLG check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TOLLGATE_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_NAME primary key (NAME_ID),
   constraint RDTOLLGTE_NAME foreign key (PID)
         references RD_TOLLGATE (PID) disable
);

comment on column RD_TOLLGATE_NAME.NAME_ID is
'[170]主键';

comment on column RD_TOLLGATE_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_TOLLGATE_NAME.PID is
'外键,引用"RD_TOLLGATE"';

comment on column RD_TOLLGATE_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_TOLLGATE_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_TOLLGATE_NAME.U_RECORD is
'增量更新标识';

comment on column RD_TOLLGATE_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TOLLGATE_PASSAGE                                   */
/*==============================================================*/
create table RD_TOLLGATE_PASSAGE 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   TOLL_FORM            NUMBER(10)           default 0 not null,
   CARD_TYPE            NUMBER(1)            default 0 not null
      constraint CKC_CARD_TYPE_RD_TOLLG check (CARD_TYPE in (0,1,2,3)) disable ,
   VEHICLE              NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TOLLGATE_P check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDTOLLGATE_PASSAGE foreign key (PID)
         references RD_TOLLGATE (PID) disable
);

comment on table RD_TOLLGATE_PASSAGE is
'记录每一个通道的收费方式,领卡方式和车辆类型';

comment on column RD_TOLLGATE_PASSAGE.PID is
'外键,引用"RD_TOLLGATE"';

comment on column RD_TOLLGATE_PASSAGE.SEQ_NUM is
'收费站进入Link从左到右的通道序号,依次从1开始递增编号';

comment on column RD_TOLLGATE_PASSAGE.TOLL_FORM is
'采用8bit 表示,从右到左依次为0~7bit,每bit 表示一种方式类型(如下),赋值为0/1 分别表示无/有,如:00000110 表示现金和银行卡
第0bit:ETC
第1bit:现金
第2bit: 银行卡(借记卡)
第3bit:信用卡
第4bit:IC 卡
第5bit:预付卡
如果所有bit 位均为0,表示未调查
注:当收费方式为"ETC"时,不允许设置其他方式';

comment on column RD_TOLLGATE_PASSAGE.CARD_TYPE is
'每个通道的领卡方式,如ETC,人工,自助等';

comment on column RD_TOLLGATE_PASSAGE.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_TOLLGATE_PASSAGE.U_RECORD is
'增量更新标识';

comment on column RD_TOLLGATE_PASSAGE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_TRAFFICSIGNAL                                      */
/*==============================================================*/
create table RD_TRAFFICSIGNAL 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   LOCATION             NUMBER(2)            default 0 not null,
   FLAG                 NUMBER(1)            default 0 not null
      constraint CKC_FLAG_RD_TRAFF check (FLAG in (0,1,2)) disable ,
   TYPE                 NUMBER(1)            default 0 not null
      constraint CKC_TYPE_RD_TRAFF check (TYPE in (0,1,2,3,4,5)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
      constraint CKC_KG_FLAG_RD_TRAFFICSIGNAL check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_TRAFF check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TRAFFICSIGNAL primary key (PID),
   constraint RDSIGNAL_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSIGNAL_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_TRAFFICSIGNAL.PID is
'主键';

comment on column RD_TRAFFICSIGNAL.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_TRAFFICSIGNAL.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_TRAFFICSIGNAL.LOCATION is
'[172U]采用3bit表示,从右到左依次为0~2bit,每bit表示一个位置(如下),赋值为0/1分别表示否/是,如: 101表示左和上
第0bit:左(Left)
第1bit:右(Right)
第2bit:上(Overhead)
如果所有bit位均为0,表示未调查';

comment on column RD_TRAFFICSIGNAL.FLAG is
'是否受信号灯控制';

comment on column RD_TRAFFICSIGNAL.TYPE is
'机动车信号灯,车道信号灯,方向指示灯等';

comment on column RD_TRAFFICSIGNAL.U_RECORD is
'增量更新标识';

comment on column RD_TRAFFICSIGNAL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VARIABLE_SPEED                                     */
/*==============================================================*/
create table RD_VARIABLE_SPEED 
(
   VSPEED_PID           NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   LOCATION             NUMBER(2)            default 0 not null,
   SPEED_VALUE          NUMBER(4)            default 0 not null,
   SPEED_TYPE           NUMBER(1)            default 0 not null
      constraint CKC_SPEED_TYPE_RD_VARIA check (SPEED_TYPE in (0,1 ,2 ,3 )) disable ,
   SPEED_DEPENDENT      NUMBER(2)            default 0 not null
      constraint CKC_SPEED_DEPENDENT_RD_VARIA check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  )) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VARIA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VARIABLE_SPEED primary key (VSPEED_PID),
   constraint RDVARIABLESPEED_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVARIABLESPEED_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDVARIABLESPEED_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_VARIABLE_SPEED is
'[170]';

comment on column RD_VARIABLE_SPEED.VSPEED_PID is
'主键';

comment on column RD_VARIABLE_SPEED.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_VARIABLE_SPEED.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_VARIABLE_SPEED.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_VARIABLE_SPEED.LOCATION is
'[172U]采用3bit表示,从右到左依次为0~2bit,每bit表示一个位置(如下),赋值为0/1分别表示否/是,如: 101表示左和上
第0bit:左(Left)
第1bit:右(Right)
第2bit:上(Overhead)
如果所有bit位均为0,表示未调查';

comment on column RD_VARIABLE_SPEED.SPEED_VALUE is
'单位:百米/时,值域: 1~9999';

comment on column RD_VARIABLE_SPEED.SPEED_TYPE is
'[170][172U]';

comment on column RD_VARIABLE_SPEED.SPEED_DEPENDENT is
'[170][172U]';

comment on column RD_VARIABLE_SPEED.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_VARIABLE_SPEED.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_VARIABLE_SPEED.U_RECORD is
'增量更新标识';

comment on column RD_VARIABLE_SPEED.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VIRCONNECT                                         */
/*==============================================================*/
create table RD_VIRCONNECT 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(2)            default 0 not null
      constraint CKC_TYPE_RD_VIRCO check (TYPE in (0,1,2,3,4,5,11,12,13,14,99)) disable ,
   OBSTACLE_FREE        NUMBER(1)            default 0 not null
      constraint CKC_OBSTACLE_FREE_RD_VIRCO check (OBSTACLE_FREE in (0,1,2)) disable ,
   FEE                  NUMBER(1)            default 0 not null
      constraint CKC_FEE_RD_VIRCO check (FEE in (0,1)) disable ,
   STREET_LIGHT         NUMBER(1)            default 0 not null
      constraint CKC_STREET_LIGHT_RD_VIRCO check (STREET_LIGHT in (0,1,2,3)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VIRCONNECT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VIRCONNECT primary key (PID)
);

comment on column RD_VIRCONNECT.PID is
'主键';

comment on column RD_VIRCONNECT.TYPE is
'直梯,扶梯,建筑物,公园,广场,街道等';

comment on column RD_VIRCONNECT.OBSTACLE_FREE is
'[180U]是否存在无障碍通道';

comment on column RD_VIRCONNECT.FEE is
'免费,收费';

comment on column RD_VIRCONNECT.STREET_LIGHT is
'未调查,有,无,或不应用';

comment on column RD_VIRCONNECT.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_VIRCONNECT.U_RECORD is
'增量更新标识';

comment on column RD_VIRCONNECT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VIRCONNECT_NAME                                    */
/*==============================================================*/
create table RD_VIRCONNECT_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
      constraint CKC_LANG_CODE_RD_VIRCO check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VIRCONNECT_N check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VIRCONNECT_NAME primary key (NAME_ID),
   constraint RDVIRCONNECT_NAME foreign key (PID)
         references RD_VIRCONNECT (PID) disable
);

comment on column RD_VIRCONNECT_NAME.NAME_ID is
'[170]主键';

comment on column RD_VIRCONNECT_NAME.NAME_GROUPID is
'[171U][170]从1开始递增编号';

comment on column RD_VIRCONNECT_NAME.PID is
'外键,引用"RD_VIRCONNECT"';

comment on column RD_VIRCONNECT_NAME.LANG_CODE is
'简体中文,繁体中文,英文,葡文等多种语言';

comment on column RD_VIRCONNECT_NAME.PHONETIC is
'[171U]中文为拼音,英文(葡文等)为音标';

comment on column RD_VIRCONNECT_NAME.U_RECORD is
'增量更新标识';

comment on column RD_VIRCONNECT_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VIRCONNECT_TRANSIT                                 */
/*==============================================================*/
create table RD_VIRCONNECT_TRANSIT 
(
   PID                  NUMBER(10)           not null,
   FIR_NODE_PID         NUMBER(10)           not null,
   SEN_NODE_PID         NUMBER(10)           not null,
   TRANSIT              NUMBER(1)            default 0 not null
      constraint CKC_TRANSIT_RD_VIRCO check (TRANSIT in (0,1,2,3)) disable ,
   SLOPE                NUMBER(1)            default 0 not null
      constraint CKC_SLOPE_RD_VIRCO check (SLOPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VIRCONNECT_TRA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDVIRCONNCT_FIRNODE foreign key (FIR_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDVIRCONNCT_SECNODE foreign key (SEN_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDVIRCONNECT_NODES foreign key (PID)
         references RD_VIRCONNECT (PID) disable
);

comment on table RD_VIRCONNECT_TRANSIT is
'记录虚拟连接中任意两点之间的连通关系';

comment on column RD_VIRCONNECT_TRANSIT.PID is
'外键,引用"RD_VIRCONNECT"';

comment on column RD_VIRCONNECT_TRANSIT.FIR_NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_VIRCONNECT_TRANSIT.SEN_NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_VIRCONNECT_TRANSIT.TRANSIT is
'节点一到节点二的顺逆方向或不可通行信息';

comment on column RD_VIRCONNECT_TRANSIT.SLOPE is
'上坡,下坡';

comment on column RD_VIRCONNECT_TRANSIT.U_RECORD is
'增量更新标识';

comment on column RD_VIRCONNECT_TRANSIT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VOICEGUIDE                                         */
/*==============================================================*/
create table RD_VOICEGUIDE 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VOICEGUIDE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VOICEGUIDE primary key (PID),
   constraint RDVOICE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVOICE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_VOICEGUIDE.PID is
'主键';

comment on column RD_VOICEGUIDE.IN_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_VOICEGUIDE.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_VOICEGUIDE.U_RECORD is
'增量更新标识';

comment on column RD_VOICEGUIDE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VOICEGUIDE_DETAIL                                  */
/*==============================================================*/
create table RD_VOICEGUIDE_DETAIL 
(
   DETAIL_ID            NUMBER(10)           not null,
   VOICEGUIDE_PID       NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   GUIDE_CODE           NUMBER(2)            default 0 not null
      constraint CKC_GUIDE_CODE_RD_VOICE check (GUIDE_CODE in (0,1,2,4,6,7,8,10,12,16,19)) disable ,
   GUIDE_TYPE           NUMBER(1)            default 0 not null
      constraint CKC_GUIDE_TYPE_RD_VOICE check (GUIDE_TYPE in (0,1,2,3)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
      constraint CKC_RELATIONSHIP_TYPE_RD_VOICE check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VOICEGUIDE_D check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VOICEGUIDE_DETAIL primary key (DETAIL_ID),
   constraint RDVOICE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVOICE_DETAIL foreign key (VOICEGUIDE_PID)
         references RD_VOICEGUIDE (PID) disable
);

comment on column RD_VOICEGUIDE_DETAIL.DETAIL_ID is
'主键';

comment on column RD_VOICEGUIDE_DETAIL.VOICEGUIDE_PID is
'外键,引用"RD_VOICEGUIDE"';

comment on column RD_VOICEGUIDE_DETAIL.OUT_LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_VOICEGUIDE_DETAIL.GUIDE_CODE is
'右斜前,右转,右后转,调头,左后转等语音提示内容';

comment on column RD_VOICEGUIDE_DETAIL.GUIDE_TYPE is
'平面,高架,地下等语音类型';

comment on column RD_VOICEGUIDE_DETAIL.U_RECORD is
'增量更新标识';

comment on column RD_VOICEGUIDE_DETAIL.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_VOICEGUIDE_VIA                                     */
/*==============================================================*/
create table RD_VOICEGUIDE_VIA 
(
   DETAIL_ID            NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   GROUP_ID             NUMBER(2)            default 1 not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_VOICEGUIDE_VIA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDVOICEGUIDEVIALINK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVOICEGUIDEVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_VOICEGUIDE_DETAIL (DETAIL_ID) disable
);

comment on table RD_VOICEGUIDE_VIA is
'(1)表达同一进入Link和退出Link之间的多组经过Link,而且经过Link不包括进入Link和退出Link
(2)NaviMap作业中,当进入和退出Link直接在同一路口挂接时,不制作经过Link;否则(如线线关系),需要制作经过Link';

comment on column RD_VOICEGUIDE_VIA.DETAIL_ID is
'外键,引用"RD_VOICEGUIDE_DETAIL"';

comment on column RD_VOICEGUIDE_VIA.LINK_PID is
'外键,引用"RD_LINK",进入和退出Link 除外';

comment on column RD_VOICEGUIDE_VIA.GROUP_ID is
'从1开始递增编号';

comment on column RD_VOICEGUIDE_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_VOICEGUIDE_VIA.U_RECORD is
'增量更新标识';

comment on column RD_VOICEGUIDE_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_WARNINGINFO                                        */
/*==============================================================*/
create table RD_WARNINGINFO 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   TYPE_CODE            VARCHAR2(5),
   VALID_DIS            NUMBER(5)            default 0 not null,
   WARN_DIS             NUMBER(5)            default 0 not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   DESCRIPT             VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RD_WARNI check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_WARNINGINFO primary key (PID),
   constraint RDWARNING_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDWARNING_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

comment on column RD_WARNINGINFO.PID is
'主键';

comment on column RD_WARNINGINFO.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_WARNINGINFO.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_WARNINGINFO.TYPE_CODE is
'参考"RD_SIGNPOST_CODE"';

comment on column RD_WARNINGINFO.VALID_DIS is
'实际预警距离,单位:米';

comment on column RD_WARNINGINFO.WARN_DIS is
'提前预告距离,目前只用于铁路道口,单位:米';

comment on column RD_WARNINGINFO.TIME_DOMAIN is
'格式参考"时间域"';

comment on column RD_WARNINGINFO.VEHICLE is
'格式参考"车辆类型"';

comment on column RD_WARNINGINFO.DESCRIPT is
'记录现场标牌中的说明文字';

comment on column RD_WARNINGINFO.U_RECORD is
'增量更新标识';

comment on column RD_WARNINGINFO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RI_FEATURE                                            */
/*==============================================================*/
create table RI_FEATURE 
(
   DATA_LOG_ID          VARCHAR2(32)         not null,
   VERSION_ID           NUMBER(10),
   COMMITED_STATE_ID    NUMBER(10),
   ID                   NUMBER(10),
   TASK_ID              VARCHAR2(50),
   OPERATE_ID           NUMBER(10),
   OPERATE_NAME         VARCHAR2(500),
   INNER_ID             NUMBER(10),
   TABLE_NAME           VARCHAR2(50),
   OBJECT_NAME          VARCHAR2(50),
   OBJECT_ID            NUMBER(10),
   DML_TYPE             NUMBER(1),
   PREVIOUS_CONTENT1    VARCHAR2(4000),
   CURRENT_CONTENT1     VARCHAR2(4000),
   GEOMETRY             MDSYS.SDO_GEOMETRY,
   UUID                 VARCHAR2(32),
   HIS_TYPE             NUMBER(1),
   REF_ID               VARCHAR2(32),
   CHANG_COL            VARCHAR2(1000),
   PREVIOUS_CONTENT2    VARCHAR2(4000),
   PREVIOUS_CONTENT3    VARCHAR2(4000),
   PREVIOUS_CONTENT4    VARCHAR2(4000),
   PREVIOUS_CONTENT5    VARCHAR2(4000),
   PREVIOUS_CONTENT6    VARCHAR2(4000),
   PREVIOUS_CONTENT7    VARCHAR2(4000),
   PREVIOUS_CONTENT8    VARCHAR2(4000),
   PREVIOUS_CONTENT9    VARCHAR2(4000),
   PREVIOUS_CONTENT10   VARCHAR2(4000),
   CURRENT_CONTENT2     VARCHAR2(4000),
   CURRENT_CONTENT3     VARCHAR2(4000),
   CURRENT_CONTENT4     VARCHAR2(4000),
   CURRENT_CONTENT5     VARCHAR2(4000),
   CURRENT_CONTENT6     VARCHAR2(4000),
   CURRENT_CONTENT7     VARCHAR2(4000),
   CURRENT_CONTENT8     VARCHAR2(4000),
   CURRENT_CONTENT9     VARCHAR2(4000),
   CURRENT_CONTENT10    VARCHAR2(4000),
   PREVIOUS_GEOMETRY    MDSYS.SDO_GEOMETRY,
   CURRENT_GEOMETRY     MDSYS.SDO_GEOMETRY,
   constraint PK_RI_FEATURE primary key (DATA_LOG_ID)
);

comment on table RI_FEATURE is
'[173U]DATA_LOG';

comment on column RI_FEATURE.DATA_LOG_ID is
'数据履历ID';

comment on column RI_FEATURE.VERSION_ID is
'版本编号';

comment on column RI_FEATURE.COMMITED_STATE_ID is
'履历提交后，当前状态值';

comment on column RI_FEATURE.ID is
'履历编号';

comment on column RI_FEATURE.TASK_ID is
'任务管理平台任务号';

comment on column RI_FEATURE.OPERATE_ID is
'操作号';

comment on column RI_FEATURE.OPERATE_NAME is
'如打断LINK，新增交限';

comment on column RI_FEATURE.INNER_ID is
'组内编号';

comment on column RI_FEATURE.TABLE_NAME is
'当前表名';

comment on column RI_FEATURE.OBJECT_NAME is
'当前对象的对象名';

comment on column RI_FEATURE.OBJECT_ID is
'当前对象的PID，不能为空';

comment on column RI_FEATURE.DML_TYPE is
'0-增、1-改、2-删';

comment on column RI_FEATURE.PREVIOUS_CONTENT1 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT1 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.GEOMETRY is
'用于提取作业范围内的履历，减少不在本图幅内的数据';

comment on column RI_FEATURE.UUID is
'记录每条记录的唯一标示';

comment on column RI_FEATURE.HIS_TYPE is
'1-正常履历
2-母库撤履历
3-本地撤销履历
4-母库重做履历
5-无效履历
6-已入库履历';

comment on column RI_FEATURE.REF_ID is
'关联履历编号';

comment on column RI_FEATURE.CHANG_COL is
'哪几个字段变化了';

comment on column RI_FEATURE.PREVIOUS_CONTENT2 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT3 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT4 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT5 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT6 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT7 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT8 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT9 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_CONTENT10 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT2 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT3 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT4 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT5 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT6 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT7 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT8 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT9 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.CURRENT_CONTENT10 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_FEATURE.PREVIOUS_GEOMETRY is
'变化前几何';

comment on column RI_FEATURE.CURRENT_GEOMETRY is
'变化后几何';

/*==============================================================*/
/* Index: IDX_DATA_LOG_VERSION                                  */
/*==============================================================*/
create index IDX_DATA_LOG_VERSION on RI_FEATURE (
   VERSION_ID ASC,
   COMMITED_STATE_ID ASC
);

/*==============================================================*/
/* Index: IDX_DATA_LOG_OBJ                                      */
/*==============================================================*/
create index IDX_DATA_LOG_OBJ on RI_FEATURE (
   OBJECT_NAME ASC,
   OBJECT_ID ASC
);

/*==============================================================*/
/* Table: RI_OPERATION                                          */
/*==============================================================*/
create table RI_OPERATION 
(
   OPERATE_LOG_ID       VARCHAR2(32)         not null,
   OPERATE_STATE_ID     NUMBER(10),
   VERSION_ID           NUMBER(10),
   ID                   NUMBER(10),
   TASK_ID              VARCHAR2(50),
   OPERATE_ID           NUMBER(10),
   OPERATE_NAME         VARCHAR2(500),
   INNER_ID             NUMBER(10),
   TABLE_NAME           VARCHAR2(50),
   OBJECT_NAME          VARCHAR2(50),
   OBJECT_ID            NUMBER(10),
   DML_TYPE             NUMBER(1),
   PREVIOUS_CONTENT1    VARCHAR2(4000),
   CURRENT_CONTENT1     VARCHAR2(4000),
   GEOMETRY             VARCHAR2(4000),
   UUID                 VARCHAR2(32),
   HIS_TYPE             NUMBER(1),
   REF_ID               VARCHAR2(32),
   CHANG_COL            VARCHAR2(1000),
   PREVIOUS_CONTENT2    VARCHAR2(4000),
   PREVIOUS_CONTENT3    VARCHAR2(4000),
   PREVIOUS_CONTENT4    VARCHAR2(4000),
   PREVIOUS_CONTENT5    VARCHAR2(4000),
   PREVIOUS_CONTENT6    VARCHAR2(4000),
   PREVIOUS_CONTENT7    VARCHAR2(4000),
   PREVIOUS_CONTENT8    VARCHAR2(4000),
   PREVIOUS_CONTENT9    VARCHAR2(4000),
   PREVIOUS_CONTENT10   VARCHAR2(4000),
   CURRENT_CONTENT2     VARCHAR2(4000),
   CURRENT_CONTENT3     VARCHAR2(4000),
   CURRENT_CONTENT4     VARCHAR2(4000),
   CURRENT_CONTENT5     VARCHAR2(4000),
   CURRENT_CONTENT6     VARCHAR2(4000),
   CURRENT_CONTENT7     VARCHAR2(4000),
   CURRENT_CONTENT8     VARCHAR2(4000),
   CURRENT_CONTENT9     VARCHAR2(4000),
   CURRENT_CONTENT10    VARCHAR2(4000),
   OPERATE_DESC         VARCHAR2(4000),
   OPERATION_RUNTIME    DATE,
   FEATURE_RID          NUMBER(10),
   FEATURE_BLOB         VARCHAR2(4000),
   constraint PK_RI_OPERATION primary key (OPERATE_LOG_ID)
);

comment on table RI_OPERATION is
'[172U]RAW_OPERATE_LOG';

comment on column RI_OPERATION.OPERATE_LOG_ID is
'作业履历ID';

comment on column RI_OPERATION.OPERATE_STATE_ID is
'提交状态号';

comment on column RI_OPERATION.VERSION_ID is
'版本号';

comment on column RI_OPERATION.ID is
'履历编号';

comment on column RI_OPERATION.TASK_ID is
'任务管理平台任务号';

comment on column RI_OPERATION.OPERATE_ID is
'操作号';

comment on column RI_OPERATION.OPERATE_NAME is
'如打断LINK，新增交限';

comment on column RI_OPERATION.INNER_ID is
'组内编号';

comment on column RI_OPERATION.TABLE_NAME is
'当前表名';

comment on column RI_OPERATION.OBJECT_NAME is
'当前对象的对象名';

comment on column RI_OPERATION.OBJECT_ID is
'当前对象的PID，不能为空';

comment on column RI_OPERATION.DML_TYPE is
'0-增、1-改、2-删';

comment on column RI_OPERATION.PREVIOUS_CONTENT1 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT1 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.GEOMETRY is
'用于提取作业范围内的履历，减少不在本图幅内的数据';

comment on column RI_OPERATION.UUID is
'记录每条记录的唯一标示';

comment on column RI_OPERATION.HIS_TYPE is
'1-正常履历
2-母库撤履历
3-本地撤销履历
4-母库重做履历
5-无效履历
6-已入库履历';

comment on column RI_OPERATION.REF_ID is
'关联履历编号';

comment on column RI_OPERATION.CHANG_COL is
'哪几个字段变化了';

comment on column RI_OPERATION.PREVIOUS_CONTENT2 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT3 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT4 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT5 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT6 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT7 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT8 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT9 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.PREVIOUS_CONTENT10 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT2 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT3 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT4 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT5 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT6 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT7 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT8 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT9 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.CURRENT_CONTENT10 is
'XML如为增则为空、为改则记录要改字段未改前的值、为删时记录数据的全字段内容';

comment on column RI_OPERATION.OPERATE_DESC is
'操作描述';

comment on column RI_OPERATION.OPERATION_RUNTIME is
'操作发生时间';

comment on column RI_OPERATION.FEATURE_RID is
'数据行ID';

comment on column RI_OPERATION.FEATURE_BLOB is
'履历二进制表示';

/*==============================================================*/
/* Index: IDX_RAW_TASK                                          */
/*==============================================================*/
create index IDX_RAW_TASK on RI_OPERATION (
   TASK_ID ASC
);

/*==============================================================*/
/* Index: IDX_RAW_COMMIT                                        */
/*==============================================================*/
create index IDX_RAW_COMMIT on RI_OPERATION (
   OPERATE_STATE_ID ASC,
   VERSION_ID ASC
);

/*==============================================================*/
/* Table: RW_FEATURE                                            */
/*==============================================================*/
create table RW_FEATURE 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_FEATURE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_FEATURE primary key (FEATURE_PID)
);

comment on column RW_FEATURE.FEATURE_PID is
'主键';

comment on column RW_FEATURE.U_RECORD is
'增量更新标识';

comment on column RW_FEATURE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_FEATURE_20W                                        */
/*==============================================================*/
create table RW_FEATURE_20W 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_FEATURE_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_FEATURE_20W primary key (FEATURE_PID)
);

comment on column RW_FEATURE_20W.FEATURE_PID is
'主键';

comment on column RW_FEATURE_20W.U_RECORD is
'增量更新标识';

comment on column RW_FEATURE_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_NODE                                               */
/*==============================================================*/
create table RW_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_RW_NODE check (KIND in (1,2)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_RW_NODE check (FORM in (0,1,2,3,4,5,6)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_RW_NODE check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_NODE primary key (NODE_PID)
);

comment on column RW_NODE.NODE_PID is
'主键';

comment on column RW_NODE.KIND is
'平面交叉点或Link属性变化点';

comment on column RW_NODE.FORM is
'铁路道口,桥,隧道,图廓点,铁路道口等';

comment on column RW_NODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column RW_NODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column RW_NODE.U_RECORD is
'增量更新标识';

comment on column RW_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_LINK                                               */
/*==============================================================*/
create table RW_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_RW_LINK check (KIND in (1,2,3)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_RW_LINK check (FORM in (0,1,2)) disable ,
   LENGTH               NUMBER(15,3)         default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_RW_LINK check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_RW_LINK check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_RW_LINK check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_LINK check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_LINK primary key (LINK_PID),
   constraint RWLINK_ENODE foreign key (E_NODE_PID)
         references RW_NODE (NODE_PID) disable ,
   constraint RWLINK_SNODE foreign key (S_NODE_PID)
         references RW_NODE (NODE_PID) disable
);

comment on column RW_LINK.LINK_PID is
'主键';

comment on column RW_LINK.FEATURE_PID is
'参考"RW_FEATURE"';

comment on column RW_LINK.S_NODE_PID is
'外键,引用"RW_NODE"';

comment on column RW_LINK.E_NODE_PID is
'外键,引用"RW_NODE"';

comment on column RW_LINK.KIND is
'铁路,磁悬浮,轻轨/地铁';

comment on column RW_LINK.FORM is
'桥或隧道';

comment on column RW_LINK.LENGTH is
'单位:米';

comment on column RW_LINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column RW_LINK.SCALE is
'注:该字段仅用于2.5 万数据,20 万数据不需要';

comment on column RW_LINK.DETAIL_FLAG is
'注:该字段仅用于2.5 万数据,20 万数据不需要';

comment on column RW_LINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column RW_LINK.U_RECORD is
'增量更新标识';

comment on column RW_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_NODE_20W                                           */
/*==============================================================*/
create table RW_NODE_20W 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_RW_NODE_ check (KIND in (1,2)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_RW_NODE_ check (FORM in (0,1,2,3,4,5,6)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_RW_NODE_ check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_NODE_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_NODE_20W primary key (NODE_PID)
);

comment on column RW_NODE_20W.NODE_PID is
'主键';

comment on column RW_NODE_20W.KIND is
'平面交叉点或Link属性变化点';

comment on column RW_NODE_20W.FORM is
'铁路道口,桥,隧道,图廓点,铁路道口等';

comment on column RW_NODE_20W.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column RW_NODE_20W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column RW_NODE_20W.U_RECORD is
'增量更新标识';

comment on column RW_NODE_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_LINK_20W                                           */
/*==============================================================*/
create table RW_LINK_20W 
(
   LINK_PID             NUMBER(10)           not null,
   FEATURE_PID          NUMBER(10)           default 0 not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_RW_LINK_ check (KIND in (1,2,3)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_RW_LINK_ check (FORM in (0,1,2)) disable ,
   LENGTH               NUMBER(15,3)         default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_RW_LINK_ check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
      constraint CKC_DETAIL_FLAG_RW_LINK_ check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_RW_LINK_ check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_LINK_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_LINK_20W primary key (LINK_PID),
   constraint RWLINK_ENODE_20W foreign key (E_NODE_PID)
         references RW_NODE_20W (NODE_PID) disable ,
   constraint RWLINK_SNODE_20W foreign key (S_NODE_PID)
         references RW_NODE_20W (NODE_PID) disable
);

comment on column RW_LINK_20W.LINK_PID is
'主键';

comment on column RW_LINK_20W.FEATURE_PID is
'参考"RW_FEATURE_20W"';

comment on column RW_LINK_20W.S_NODE_PID is
'外键,引用"RW_NODE_20W"';

comment on column RW_LINK_20W.E_NODE_PID is
'外键,引用"RW_NODE_20W"';

comment on column RW_LINK_20W.KIND is
'铁路,磁悬浮,轻轨/地铁';

comment on column RW_LINK_20W.FORM is
'桥或隧道';

comment on column RW_LINK_20W.LENGTH is
'单位:米';

comment on column RW_LINK_20W.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column RW_LINK_20W.SCALE is
'注:该字段仅用于2.5 万数据,20 万数据不需要';

comment on column RW_LINK_20W.DETAIL_FLAG is
'注:该字段仅用于2.5 万数据,20 万数据不需要';

comment on column RW_LINK_20W.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column RW_LINK_20W.U_RECORD is
'增量更新标识';

comment on column RW_LINK_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_LINK_NAME                                          */
/*==============================================================*/
create table RW_LINK_NAME 
(
   LINK_PID             NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RW_LINK_NAME check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_LINK_NAME check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWLINK_NAME foreign key (LINK_PID)
         references RW_LINK (LINK_PID) disable
);

comment on column RW_LINK_NAME.LINK_PID is
'外键,引用"RW_LINK"';

comment on column RW_LINK_NAME.NAME_GROUPID is
'[171U][170]参考道路名库"RD_NAME"';

comment on column RW_LINK_NAME.SRC_FLAG is
'[170]现仅指英文名来源';

comment on column RW_LINK_NAME.U_RECORD is
'增量更新标识';

comment on column RW_LINK_NAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_LINK_NAME_20W                                      */
/*==============================================================*/
create table RW_LINK_NAME_20W 
(
   LINK_PID             NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   SRC_FLAG             NUMBER(2)            default 0 not null
      constraint CKC_SRC_FLAG_RW_LINK_NAME_20W check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_LINK_NAME_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWLINK_NAME_20W foreign key (LINK_PID)
         references RW_LINK_20W (LINK_PID) disable
);

comment on table RW_LINK_NAME_20W is
'[170]';

comment on column RW_LINK_NAME_20W.LINK_PID is
'外键,引用"RW_LINK_20W"';

comment on column RW_LINK_NAME_20W.NAME_GROUPID is
'[171U]参考道路名库"RD_NAME"';

comment on column RW_LINK_NAME_20W.SRC_FLAG is
'现仅指英文名来源';

comment on column RW_LINK_NAME_20W.U_RECORD is
'增量更新标识';

comment on column RW_LINK_NAME_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_NODE_MESH                                          */
/*==============================================================*/
create table RW_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_NODE_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWNODE_MESH foreign key (NODE_PID)
         references RW_NODE (NODE_PID) disable
);

comment on column RW_NODE_MESH.NODE_PID is
'外键,引用"RW_NODE"';

comment on column RW_NODE_MESH.U_RECORD is
'增量更新标识';

comment on column RW_NODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RW_NODE_MESH_20W                                      */
/*==============================================================*/
create table RW_NODE_MESH_20W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_RW_NODE_MESH_20W check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWNODE_MESH_20W foreign key (NODE_PID)
         references RW_NODE_20W (NODE_PID) disable
);

comment on column RW_NODE_MESH_20W.NODE_PID is
'外键,引用"RW_NODE_20W"';

comment on column RW_NODE_MESH_20W.U_RECORD is
'增量更新标识';

comment on column RW_NODE_MESH_20W.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TB_ABSTRACT_INFO                                      */
/*==============================================================*/
create table TB_ABSTRACT_INFO 
(
   INFO_UUID            VARCHAR2(32)         not null,
   INFO_CODE            VARCHAR2(18),
   INFO_NAME            VARCHAR2(2000),
   CONTENT              CLOB,
   INFO_TYPE_CODE       VARCHAR2(32),
   INFO_SOURCE_UUID     VARCHAR2(32),
   ADMIN_CODE           VARCHAR2(32),
   ADMIN_FULL_NAME      VARCHAR2(500),
   CREATE_PERSON        VARCHAR2(50),
   CREATE_TIME          DATE,
   UPDATE_PERSON        VARCHAR2(50),
   UPDATE_TIME          DATE,
   STATUS               VARCHAR2(32),
   LOCK_PERSON          VARCHAR2(50),
   LOCK_TIME            DATE,
   LOCK_FLAG            NUMBER(1,0)          default 0 not null,
   DELETE_FLAG          NUMBER(1,0)          default 0 not null,
   DELETE_PERSON        VARCHAR2(50),
   DELETE_TIME          DATE,
   REMARK               VARCHAR2(500),
   QI_STATUS            VARCHAR2(32),
   GET_METHOD_UUID      VARCHAR2(32),
   IS_INFO              NUMBER(1)            default 0 not null,
   URL                  VARCHAR2(500),
   TITLE                VARCHAR2(2000),
   JUDGE_BATCH_UUID     VARCHAR2(32),
   PUSH_BATCH_UUID      VARCHAR2(32),
   HISTORY_STEP         NUMBER(10)           default 0 not null,
   QI_REMARK            VARCHAR2(1000),
   QI_ERROR_TYPE        VARCHAR2(1000),
   QI_ERROR_STAT_FLAG   NUMBER(1,0)          default 0 not null,
   DUPLICATE_FLAG       NUMBER(1,0)          default 0 not null,
   DUP_DATA_UUID        VARCHAR2(32),
   DUP_REFERENCE_FLAG   NUMBER(1,0)          default 0 not null,
   SIM_NUM              NUMBER(22)           default 0 not null,
   DUP_CHECK_TIME       TIMESTAMP(6),
   INFO_RELATION        VARCHAR2(32),
   INFO_RELATION_UUID   VARCHAR2(32),
   AVAILABILITY_JUDGE_PERSON VARCHAR2(50),
   AVAILABILITY_JUDGE_TIME TIMESTAMP(6),
   AVAILABILITY_JUDGE_FALG NUMBER(1)            default 0 not null,
   PROPERTY_JUDGE_FLAG  NUMBER(1)            default 0 not null,
   PROPERTY_JUDGE_TIME  TIMESTAMP(6),
   AVAILABILITY_JUDGE_REMARK VARCHAR2(500),
   SIM_DUP_JUDGE_PERSON VARCHAR2(50),
   SIM_DUP_JUDGE_TIME   TIMESTAMP(6),
   AVAILABILITY_JUDGE_BATCH_UUID VARCHAR2(32),
   DUP_JUDGE_BATCH_UUID VARCHAR2(32),
   PROPERTY_JUDGE_PERSON VARCHAR2(50),
   AVAILABLE_PUSH_BATCH_UUID VARCHAR2(32),
   DUP_PUSH_BATCH_UUID  VARCHAR2(32),
   PROPERTY_PUSH_BATCH_UUID VARCHAR2(32),
   PROPERTY_JUDGE_BATCH_UUID VARCHAR2(32),
   SIM_DUP_JUDGE_REMARK VARCHAR2(500),
   SIM_DUP_JUDGE_FLAG   NUMBER(1)            default 0 not null,
   CONVERT_FLAG         NUMBER(22)           default 0 not null,
   QI_AVAILABLE_BATCH_UUID VARCHAR2(32),
   QI_DUP_BATCH_UUID    VARCHAR2(32),
   QI_BATCH_UUID        VARCHAR2(32),
   KEYWORDS_GEN_STATUS  NUMBER(1)            default 0 not null,
   CONTENT_MD5          VARCHAR2(255),
   USE_LEVEL            VARCHAR2(50),
   UPDATE_TYPE          VARCHAR2(32),
   QI_AVAILABLE_NUM     NUMBER(22)           default 0 not null,
   QI_DUP_NUM           NUMBER(22)           default 0 not null,
   QI_NUM               NUMBER(22)           default 0 not null,
   ADMIN_CODE_SIX_BIT   VARCHAR2(6),
   constraint PK_TB_ABSTRACT_INFO primary key (INFO_UUID)
);

comment on column TB_ABSTRACT_INFO.INFO_UUID is
'主键';

comment on column TB_ABSTRACT_INFO.HISTORY_STEP is
'默认为0,表示正常信息,非0为履历信息';

comment on column TB_ABSTRACT_INFO.CONTENT_MD5 is
'用于比较';

comment on column TB_ABSTRACT_INFO.ADMIN_CODE_SIX_BIT is
'在发布时设置该编码';

/*==============================================================*/
/* Table: TB_INTELLIGENCE                                       */
/*==============================================================*/
create table TB_INTELLIGENCE 
(
   SPECIALCASE_ID       VARCHAR2(32)         not null,
   ACHIEVEMENT_TYPE     VARCHAR2(32),
   TRANSFORM_REMARK     VARCHAR2(32),
   STRUCTURE_TYPE       VARCHAR2(32),
   CONFIRM_STATUS       VARCHAR2(32),
   LAT                  VARCHAR2(50),
   LON                  VARCHAR2(50),
   PRECISION            VARCHAR2(32),
   MAP_CODE             CLOB,
   USED_TIME            DATE,
   SERVICE_STATUS       VARCHAR2(32),
   REMIND_METHOD        VARCHAR2(50),
   REMIND_CYCLE         VARCHAR2(50),
   REMIND_DATE          DATE,
   PHONE                VARCHAR2(64),
   ADDRESS              VARCHAR2(2000),
   SHORT_NAME           VARCHAR2(50),
   ENGLISH_NAME         VARCHAR2(50),
   FAX                  VARCHAR2(50),
   ZIP_CODE             VARCHAR2(50),
   START_POINT          VARCHAR2(300),
   MIDDLE_POINT         VARCHAR2(300),
   END_POINT            VARCHAR2(300),
   ROAD_LENGTH          VARCHAR2(30),
   GEOINFO              VARCHAR2(2000),
   SPECIAL_TOPIC_NAME   VARCHAR2(2000),
   REMIND_CYCLE_VALUE   NUMBER(5,0)          default 0 not null,
   VERSION_UUID         VARCHAR2(32),
   UPDATE_NUM           NUMBER(5,0)          default 0 not null,
   INFO_SOURCE_NAME     VARCHAR2(1000),
   OPERATE_TYPE         VARCHAR2(32),
   PUBLISH_NUM          NUMBER(10,0)         default 0 not null,
   INFO_ADD_FLAG        NUMBER(1)            default 0 not null,
   IDCODE_REL           VARCHAR2(50),
   PERMANENT_ID_REL     VARCHAR2(50),
   DATA_FORMAT_REL      VARCHAR2(50),
   CHAIN                VARCHAR2(255),
   IMPORTANCE           VARCHAR2(50),
   SIGHTLEVEL           VARCHAR2(50),
   ALIASNAME            VARCHAR2(255),
   GDF_ORIGINENGNAME    VARCHAR2(255),
   GDF_ENGCLASS         VARCHAR2(50),
   CLASS                VARCHAR2(20),
   constraint TBABSTRACT_INTELLIGENCE foreign key (SPECIALCASE_ID)
         references TB_ABSTRACT_INFO (INFO_UUID) disable
);

comment on column TB_INTELLIGENCE.SPECIALCASE_ID is
'外键,引用"TB_ABSTRACT_INFO"';

/*==============================================================*/
/* Table: TMC_AREA                                              */
/*==============================================================*/
create table TMC_AREA 
(
   TMC_ID               NUMBER(10)           not null,
   LOCTABLE_ID          VARCHAR2(2),
   LOC_CODE             NUMBER(5)            default 0 not null,
   TYPE_CODE            VARCHAR2(32)        
      constraint CKC_TYPE_CODE_TMC_AREA check (TYPE_CODE is null or (TYPE_CODE in ('A1.0','A2.0','A3.0','A5.0','A5.1','A5.2','A5.3','A6.0','A6.1','A6.2','A6.3','A6.4','A6.5','A6.6','A6.7','A6.8','A7.0','A8.0','A9.0','A9.1','A9.2','A10.0','A11.0','A12.0'))) disable ,
   UPAREA_TMC_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_AREA check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_AREA primary key (TMC_ID)
);

comment on column TMC_AREA.TMC_ID is
'主键';

comment on column TMC_AREA.LOCTABLE_ID is
'参考"TMC_LOCATION_CODE"';

comment on column TMC_AREA.LOC_CODE is
'与"LOCTABLE_ID"联合使用';

comment on column TMC_AREA.TYPE_CODE is
'TMC位置代码类别(大写英文半角字母,如P,A,L),类型以及子类型(英文半角数字,如1,2等)构成的字符串,通过英文半角句号"."连接,如A1.0表示大洲,A5.1表示海洋等';

comment on column TMC_AREA.UPAREA_TMC_ID is
'[173sp1][170]参考”TMC_AREA”的”TMC_ID”';

comment on column TMC_AREA.U_RECORD is
'增量更新标识';

comment on column TMC_AREA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_AREA_TRANSLATENAME                                */
/*==============================================================*/
create table TMC_AREA_TRANSLATENAME 
(
   TMC_ID               NUMBER(10)           not null,
   TRANS_LANG           VARCHAR2(3)          default 'CHI' not null
      constraint CKC_TRANS_LANG_TMC_AREA check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_AREA_TRANSLAT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint TMCAREA_NAME foreign key (TMC_ID)
         references TMC_AREA (TMC_ID) disable
);

comment on column TMC_AREA_TRANSLATENAME.TMC_ID is
'外键,引用"TMC_AREA"';

comment on column TMC_AREA_TRANSLATENAME.TRANS_LANG is
'简体中文(大陆),繁体中文(港澳),英文,葡文等多种语言';

comment on column TMC_AREA_TRANSLATENAME.PHONETIC is
'[171U][170]中文为拼音,英文(葡文等)为音标';

comment on column TMC_AREA_TRANSLATENAME.U_RECORD is
'增量更新标识';

comment on column TMC_AREA_TRANSLATENAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_LINE                                              */
/*==============================================================*/
create table TMC_LINE 
(
   TMC_ID               NUMBER(10)           not null,
   LOCTABLE_ID          VARCHAR2(2),
   LOC_CODE             NUMBER(5)            default 0 not null,
   TYPE_CODE            VARCHAR2(32)        
      constraint CKC_TYPE_CODE_TMC_LINE check (TYPE_CODE is null or (TYPE_CODE in ('L1.0','L1.1','L1.2','L1.3','L1.4','L2.0','L2.1','L2.2','L3.0','L4.0','L5.0','L6.0','L6.1','L6.2','L7.0'))) disable ,
   SEQ_NUM              NUMBER(5)            default 0 not null,
   AREA_TMC_ID          NUMBER(10)           default 0 not null,
   LOCOFF_POS           NUMBER(10)           default 0 not null,
   LOCOFF_NEG           NUMBER(10)           default 0 not null,
   UPLINE_TMC_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_LINE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_LINE primary key (TMC_ID)
);

comment on table TMC_LINE is
'记录TMC道路和路段的信息';

comment on column TMC_LINE.TMC_ID is
'主键';

comment on column TMC_LINE.LOCTABLE_ID is
'参考"TMC_LOCATION_CODE"';

comment on column TMC_LINE.LOC_CODE is
'与"LOCTABLE_ID"联合使用';

comment on column TMC_LINE.TYPE_CODE is
'TMC位置代码类别(大写英文半角字母,如P,A,L),类型以及子类型(英文半角数字,如1,2等)构成的字符串,通过英文半角句号"."连接,如L1.0表示道路,L2.0表示环路等';

comment on column TMC_LINE.AREA_TMC_ID is
'[173sp1][170]参考"TMC_AREA"的"TMC_ID"';

comment on column TMC_LINE.LOCOFF_POS is
'[173sp1]参考"TMC_LINE"的"TMC_ID"';

comment on column TMC_LINE.LOCOFF_NEG is
'[173sp1]参考"TMC_LINE"的"TMC_ID"';

comment on column TMC_LINE.UPLINE_TMC_ID is
'[173sp1][170]参考"TMC_LINE"的"TMC_ID"';

comment on column TMC_LINE.U_RECORD is
'增量更新标识';

comment on column TMC_LINE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_LINE_TRANSLATENAME                                */
/*==============================================================*/
create table TMC_LINE_TRANSLATENAME 
(
   TMC_ID               NUMBER(10)           not null,
   NAME_FLAG            NUMBER(1)            default 0 not null
      constraint CKC_NAME_FLAG_TMC_LINE check (NAME_FLAG in (0,1,2)) disable ,
   TRANS_LANG           VARCHAR2(3)          default 'CHI' not null
      constraint CKC_TRANS_LANG_TMC_LINE check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_LINE_TRANSLAT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint TMCLINE_TRANSLATENAME foreign key (TMC_ID)
         references TMC_LINE (TMC_ID) disable
);

comment on table TMC_LINE_TRANSLATENAME is
'TMC道路和路段的译名表';

comment on column TMC_LINE_TRANSLATENAME.TMC_ID is
'外键,引用"TMC_LINE"';

comment on column TMC_LINE_TRANSLATENAME.TRANS_LANG is
'简体中文(大陆),繁体中文(港澳),英文,葡文等多种语言';

comment on column TMC_LINE_TRANSLATENAME.PHONETIC is
'[171U][170]中文为拼音,英文(葡文等)为音标';

comment on column TMC_LINE_TRANSLATENAME.U_RECORD is
'增量更新标识';

comment on column TMC_LINE_TRANSLATENAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_LOCATION_CODE                                     */
/*==============================================================*/
create table TMC_LOCATION_CODE 
(
   LOCTABLE_ID          VARCHAR2(2)          not null,
   NAME                 VARCHAR2(64),
   constraint PK_TMC_LOCATION_CODE primary key (LOCTABLE_ID)
);

comment on table TMC_LOCATION_CODE is
'TMC地区代码表';

comment on column TMC_LOCATION_CODE.LOCTABLE_ID is
'主键';

comment on column TMC_LOCATION_CODE.U_RECORD is
'增量更新标识';

comment on column TMC_LOCATION_CODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_POINT                                             */
/*==============================================================*/
create table TMC_POINT 
(
   TMC_ID               NUMBER(10)           not null,
   LOCTABLE_ID          VARCHAR2(2),
   LOC_CODE             NUMBER(5)            default 0 not null,
   TYPE_CODE            VARCHAR2(32)        
      constraint CKC_TYPE_CODE_TMC_POIN check (TYPE_CODE is null or (TYPE_CODE in ('P1.0','P1.1','P1.2','P1.3','P1.4','P1.5','P1.6','P1.7','P1.8','P1.9','P1.10','P1.11','P1.12','P1.13','P1.14','P1.15','P2.0','P2.1','P2.2','P3.0','P3.1','P3.2','P3.3','P3.4','P3.5','P3.6','P3.7','P3.8','P3.9','P3.10','P3.11','P3.12','P3.13','P3.14','P3.15','P3.16','P3.17','P3.18','P3.19','P3.20','P3.21','P3.22','P3.23','P3.24','P3.25','P3.26','P3.27','P3.28','P3.29','P3.30','P3.31','P3.34','P3.35','P3.36','P3.37','P3.38','P3.39','P3.40','P3.41','P3.42','P3.43','P3.44','P3.45','P3.46','P3.47','P4.0'))) disable ,
   IN_POS               NUMBER(1)            default 0 not null
      constraint CKC_IN_POS_TMC_POIN check (IN_POS in (0,1)) disable ,
   IN_NEG               NUMBER(1)            default 0 not null
      constraint CKC_IN_NEG_TMC_POIN check (IN_NEG in (0,1)) disable ,
   OUT_POS              NUMBER(1)            default 0 not null
      constraint CKC_OUT_POS_TMC_POIN check (OUT_POS in (0,1)) disable ,
   OUT_NEG              NUMBER(1)            default 0 not null
      constraint CKC_OUT_NEG_TMC_POIN check (OUT_NEG in (0,1)) disable ,
   PRESENT_POS          NUMBER(1)            default 0 not null
      constraint CKC_PRESENT_POS_TMC_POIN check (PRESENT_POS in (0,1)) disable ,
   PRESENT_NEG          NUMBER(1)            default 0 not null
      constraint CKC_PRESENT_NEG_TMC_POIN check (PRESENT_NEG in (0,1)) disable ,
   LOCOFF_POS           NUMBER(10)           default 0 not null,
   LOCOFF_NEG           NUMBER(10)           default 0 not null,
   LINE_TMC_ID          NUMBER(10)           default 0 not null,
   AREA_TMC_ID          NUMBER(10)           default 0 not null,
   JUNC_LOCCODE         NUMBER(10)           default 0 not null,
   NEIGHBOUR_BOUND      VARCHAR2(32),
   NEIGHBOUR_TABLE      NUMBER(2)            default 0 not null,
   URBAN                NUMBER(1)            default 0 not null
      constraint CKC_URBAN_TMC_POIN check (URBAN in (0,1)) disable ,
   INTERUPT_ROAD        NUMBER(1)            default 0 not null
      constraint CKC_INTERUPT_ROAD_TMC_POIN check (INTERUPT_ROAD in (0,1)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_TMC_POIN check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_POINT check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_POINT primary key (TMC_ID)
);

comment on column TMC_POINT.TMC_ID is
'主键';

comment on column TMC_POINT.LOCTABLE_ID is
'参考"TMC_LOCATION_CODE"';

comment on column TMC_POINT.LOC_CODE is
'与"LOCTABLE_ID"联合使用';

comment on column TMC_POINT.TYPE_CODE is
'TMC位置代码类别(大写英文半角字母,如P,A,L),类型以及子类型(英文半角数字,如1,2等)构成的字符串,通过英文半角句号"."连接,如P1.0表示连接点,L1.15表示出口等';

comment on column TMC_POINT.IN_POS is
'沿正方向进入道路';

comment on column TMC_POINT.IN_NEG is
'沿负方向进入道路';

comment on column TMC_POINT.OUT_POS is
'沿正方向离开道路';

comment on column TMC_POINT.OUT_NEG is
'沿负方向离开道路';

comment on column TMC_POINT.PRESENT_POS is
'是否出现在道路的正方向上';

comment on column TMC_POINT.PRESENT_NEG is
'是否出现在道路的负方向上';

comment on column TMC_POINT.LOCOFF_POS is
'[173sp1]参考"TMC_POINT"的"TMC_ID"';

comment on column TMC_POINT.LOCOFF_NEG is
'[173sp1]参考"TMC_POINT"的"TMC_ID"';

comment on column TMC_POINT.LINE_TMC_ID is
'[173sp1][170]参考"TMC_LINE"的"TMC_ID"';

comment on column TMC_POINT.AREA_TMC_ID is
'[173sp1][170]参考"TMC_AREA"的"TMC_ID"';

comment on column TMC_POINT.JUNC_LOCCODE is
'[173sp1]';

comment on column TMC_POINT.URBAN is
'是否城市内';

comment on column TMC_POINT.INTERUPT_ROAD is
'是否打断道路';

comment on column TMC_POINT.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column TMC_POINT.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column TMC_POINT.U_RECORD is
'增量更新标识';

comment on column TMC_POINT.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_POINT_TRANSLATENAME                               */
/*==============================================================*/
create table TMC_POINT_TRANSLATENAME 
(
   TMC_ID               NUMBER(10)           not null,
   NAME_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_NAME_FLAG_TMC_POIN check (NAME_FLAG in (1,2,3)) disable ,
   TRANS_LANG           VARCHAR2(3)          default 'CHI' not null
      constraint CKC_TRANS_LANG_TMC_POIN check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_POINT_TRANS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint TMCPOINT_TRANSLATENAME foreign key (TMC_ID)
         references TMC_POINT (TMC_ID) disable
);

comment on column TMC_POINT_TRANSLATENAME.TMC_ID is
'外键,引用"TMC_POINT"';

comment on column TMC_POINT_TRANSLATENAME.TRANS_LANG is
'简体中文(大陆),繁体中文(港澳),英文,葡文等多种语言';

comment on column TMC_POINT_TRANSLATENAME.PHONETIC is
'[171U][170]中文为拼音,英文(葡文等)为音标';

comment on column TMC_POINT_TRANSLATENAME.U_RECORD is
'增量更新标识';

comment on column TMC_POINT_TRANSLATENAME.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: TMC_VERSION                                           */
/*==============================================================*/
create table TMC_VERSION 
(
   LOCTABLE_ID          VARCHAR2(2),
   VERSION              VARCHAR2(64),
   VERSION_DES          VARCHAR2(200),
   ORGDATA_VERSION      VARCHAR2(200),
   ORGDATA_RELEASEDATE  VARCHAR2(200),
   ORGDATA_NEXTUPDATE   VARCHAR2(200),
   AUTHOR               VARCHAR2(200),
   CHARSET              VARCHAR2(64),
   EXCHANGE_VERSION     VARCHAR2(200),
   COUNTRY_ID           VARCHAR2(3),
   COUNTRY_NAME         VARCHAR2(64),
   COUNTRY_SHORT        VARCHAR2(64),
   BOUND_NAME           VARCHAR2(200),
   BOUND_ID             VARCHAR2(64),
   BOUND_SHORT          VARCHAR2(64),
   NOTE                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_TMC_VERS check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000)
);

comment on column TMC_VERSION.LOCTABLE_ID is
'参考"TMC_LOCATION_CODE"';

comment on column TMC_VERSION.COUNTRY_ID is
'中国地区代码:059';

comment on column TMC_VERSION.U_RECORD is
'增量更新标识';

comment on column TMC_VERSION.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_FACE                                             */
/*==============================================================*/
create table ZONE_FACE 
(
   FACE_PID             NUMBER(10)           not null,
   REGION_ID            NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_ZONE_FAC check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ZONE_FACE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ZONE_FACE primary key (FACE_PID),
   constraint ADADMIN_ZONEFACE foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID) disable
);

comment on column ZONE_FACE.FACE_PID is
'主键';

comment on column ZONE_FACE.REGION_ID is
'[170]外键,引用"AD_ADMIN"';

comment on column ZONE_FACE.GEOMETRY is
'存储以"度"为单位的经纬度坐标序列,首末节点坐标重合';

comment on column ZONE_FACE.AREA is
'单位:平方米';

comment on column ZONE_FACE.PERIMETER is
'单位:米';

comment on column ZONE_FACE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column ZONE_FACE.U_RECORD is
'增量更新标识';

comment on column ZONE_FACE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_NODE                                             */
/*==============================================================*/
create table ZONE_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_ZONE_NOD check (KIND in (1,2,3)) disable ,
   FORM                 NUMBER(1)            default 0 not null
      constraint CKC_FORM_ZONE_NOD check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_ZONE_NOD check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ZONE_NODE check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ZONE_NODE primary key (NODE_PID)
);

comment on column ZONE_NODE.NODE_PID is
'主键';

comment on column ZONE_NODE.KIND is
'平面交叉点,Zone边界点';

comment on column ZONE_NODE.FORM is
'图廓点,角点';

comment on column ZONE_NODE.GEOMETRY is
'存储以"度"为单位的经纬度坐标点';

comment on column ZONE_NODE.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column ZONE_NODE.U_RECORD is
'增量更新标识';

comment on column ZONE_NODE.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_LINK                                             */
/*==============================================================*/
create table ZONE_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
      constraint CKC_SCALE_ZONE_LIN check (SCALE in (0,1,2)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
      constraint CKC_EDIT_FLAG_ZONE_LIN check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ZONE_LIN check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ZONE_LINK primary key (LINK_PID),
   constraint ZONELINK_SNODE foreign key (S_NODE_PID)
         references ZONE_NODE (NODE_PID) disable ,
   constraint ZONELINK_ENODE foreign key (E_NODE_PID)
         references ZONE_NODE (NODE_PID) disable
);

comment on column ZONE_LINK.LINK_PID is
'主键';

comment on column ZONE_LINK.S_NODE_PID is
'外键,引用"ZONE_NODE"';

comment on column ZONE_LINK.E_NODE_PID is
'外键,引用"ZONE_NODE"';

comment on column ZONE_LINK.GEOMETRY is
'(1)存储以"度"为单位的经纬度坐标序列
(2)起点(S_NODE_PID)和终点(E_NODE_PID)坐标作为形状点来存储';

comment on column ZONE_LINK.LENGTH is
'单位:米';

comment on column ZONE_LINK.EDIT_FLAG is
'[171A]用于数据完整提取时,区分是否可编辑';

comment on column ZONE_LINK.U_RECORD is
'增量更新标识';

comment on column ZONE_LINK.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_FACE_TOPO                                        */
/*==============================================================*/
create table ZONE_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ZONE_FACE_TOPO check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONEFACE_LINK foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID) disable ,
   constraint ZONEFACE_LINKS foreign key (FACE_PID)
         references ZONE_FACE (FACE_PID) disable
);

comment on column ZONE_FACE_TOPO.FACE_PID is
'外键,引用"ZONE_FACE"';

comment on column ZONE_FACE_TOPO.SEQ_NUM is
'从1开始递增编号';

comment on column ZONE_FACE_TOPO.LINK_PID is
'外键,引用"ZONE_LINK"';

comment on column ZONE_FACE_TOPO.U_RECORD is
'增量更新标识';

comment on column ZONE_FACE_TOPO.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_LINK_KIND                                        */
/*==============================================================*/
create table ZONE_LINK_KIND 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
      constraint CKC_KIND_ZONE_LIN check (KIND in (0,1,2)) disable ,
   FORM                 NUMBER(1)            default 1 not null
      constraint CKC_FORM_ZONE_LIN check (FORM in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_REC_ZONE_KIND check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONELINK_LINKKIND foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID) disable
);

comment on table ZONE_LINK_KIND is
'[173sp1]';

comment on column ZONE_LINK_KIND.LINK_PID is
'外键,引用"ZONE_LINK"';

comment on column ZONE_LINK_KIND.U_RECORD is
'增量更新标识';

comment on column ZONE_LINK_KIND.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_LINK_MESH                                        */
/*==============================================================*/
create table ZONE_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ZONE_LINK_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONELINK_MESH foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID) disable
);

comment on column ZONE_LINK_MESH.LINK_PID is
'外键,引用"ZONE_LINK"';

comment on column ZONE_LINK_MESH.U_RECORD is
'增量更新标识';

comment on column ZONE_LINK_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: ZONE_NODE_MESH                                        */
/*==============================================================*/
create table ZONE_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
      constraint CKC_U_RECORD_ZONE_NODE_MESH check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONENODE_MESH foreign key (NODE_PID)
         references ZONE_NODE (NODE_PID) disable
);

comment on column ZONE_NODE_MESH.NODE_PID is
'外键,引用"ZONE_NODE"';

comment on column ZONE_NODE_MESH.U_RECORD is
'增量更新标识';

comment on column ZONE_NODE_MESH.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';



--rd_signpost_code需要删除，使用navidata中的表（需求来至于高超）
--M_FLAG_CODE需要删除，使用navidata中的表（需求来至于高超）

create table TASK_EXTENT
(
   WKT             CLOB
);




create table b_metadata(key varchar2(30),value varchar2(50),memo varchar2(200));
insert into b_metadata(key,value)values('subversionstate','0');
commit;




create table OPERATE_LOG
(
  OPERATE_LOG_ID    VARCHAR2(32) not null,
  OPERATE_STATE_ID  NUMBER(10),
  VERSION_ID        NUMBER(10),
  ID                NUMBER(10),
  TASK_ID           VARCHAR2(50),
  OPERATE_ID        NUMBER(10),
  OPERATE_NAME      VARCHAR2(500),
  INNER_ID          NUMBER(10),
  TABLE_NAME        VARCHAR2(50),
  OBJECT_NAME       VARCHAR2(50),
  OBJECT_ID         NUMBER(10),
  DML_TYPE          NUMBER(1),
  GEOMETRY          VARCHAR2(4000),
  UUID              VARCHAR2(32),
  HIS_TYPE          NUMBER(1),
  REF_ID            VARCHAR2(32),
  CHANG_COL         VARCHAR2(1000),
  OPERATE_DESC      CLOB,
  OPERATION_RUNTIME DATE,
  FEATURE_RID       NUMBER(10),
  FEATURE_BLOB      VARCHAR2(4000),
  PREVIOUS_CONTENT  CLOB,
  CURRENT_CONTENT   CLOB
)
;
comment on column OPERATE_LOG.OPERATE_LOG_ID
  is '作业履历ID';
comment on column OPERATE_LOG.OPERATE_STATE_ID
  is '提交状态号';
comment on column OPERATE_LOG.VERSION_ID
  is '版本号';
comment on column OPERATE_LOG.ID
  is '履历编号';
comment on column OPERATE_LOG.TASK_ID
  is '任务管理平台任务号';
comment on column OPERATE_LOG.OPERATE_ID
  is '操作号';
comment on column OPERATE_LOG.OPERATE_NAME
  is '如打断LINK，新增交限';
comment on column OPERATE_LOG.INNER_ID
  is '组内编号';
comment on column OPERATE_LOG.TABLE_NAME
  is '当前表名';
comment on column OPERATE_LOG.OBJECT_NAME
  is '当前对象的对象名';
comment on column OPERATE_LOG.OBJECT_ID
  is '当前对象的PID，不能为空';
comment on column OPERATE_LOG.DML_TYPE
  is '0-增、1-改、2-删';
comment on column OPERATE_LOG.GEOMETRY
  is '用于提取作业范围内的履历，减少不在本图幅内的数据';
comment on column OPERATE_LOG.UUID
  is '记录每条记录的唯一标示';
comment on column OPERATE_LOG.HIS_TYPE
  is '1-正常履历
2-母库撤履历
3-本地撤销履历
4-母库重做履历
5-无效履历
6-已入库履历';
comment on column OPERATE_LOG.REF_ID
  is '关联履历编号';
comment on column OPERATE_LOG.CHANG_COL
  is '哪几个字段变化了';
comment on column OPERATE_LOG.OPERATE_DESC
  is '操作描述';
comment on column OPERATE_LOG.OPERATION_RUNTIME
  is '操作发生时间';
comment on column OPERATE_LOG.FEATURE_RID
  is '数据行ID';
comment on column OPERATE_LOG.FEATURE_BLOB
  is '履历二进制表示';
comment on column OPERATE_LOG.PREVIOUS_CONTENT
  is '修改前的履历';
comment on column OPERATE_LOG.CURRENT_CONTENT
  is '修改后的履历';
alter table OPERATE_LOG
  add constraint PK_OPERATE_LOG primary key (OPERATE_LOG_ID);
create index idx_operate_log_state on operate_log(nvl(operate_state_id,0));
create index idx_operate_log_OPERATEID on operate_log(OPERATE_ID);
