/*==============================================================*/
/* Table: ACH_GD                                                */
/*==============================================================*/
create table ACH_GDB_INFO 
(
   ACH_GDB_ID           VARCHAR2(1000)       not null,
   CREATE_TIME          DATE,
   SYS_INFO             VARCHAR2(1000),
   CREATE_PERSON        NUMBER(10)           default 0,
   VER_NAME             VARCHAR2(1000),
   VER_NUM              VARCHAR2(1000)      
check (VER_NUM is null or (VER_NUM in ('0','1','2','3'))) disable ,
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

/*==============================================================*/
/* Table: ADAS_NODE                                             */
/*==============================================================*/
create table ADAS_NODE  
(
   NODE_PID             NUMBER(10)                     not null,
   RDNODE_PID           NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   FORM_OF_WAY          NUMBER(2)                      default 1 not null
       check (FORM_OF_WAY in (1,2)) disable,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ADAS_NODE primary key (NODE_PID)
);



/*==============================================================*/
/* Table: ADAS_LINK                                             */
/*==============================================================*/
create table ADAS_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   RDLINK_PID           NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   DESCRIPTION          VARCHAR2(1000),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   SRC_FLAG             NUMBER(1)                      default 1 not null
       check (SRC_FLAG in (1,2)) disable,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ADAS_LINK primary key (LINK_PID),
   constraint ADASLINK_SNODE foreign key (S_NODE_PID)
         references ADAS_NODE (NODE_PID) disable,
   constraint ADASLINK_ENODE foreign key (E_NODE_PID)
         references ADAS_NODE (NODE_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASLINKGEOMETRY_ADASLINK foreign key (LINK_PID)
         references ADAS_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_NODE                                               */
/*==============================================================*/
create table RD_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2,3)) disable,
   GEOMETRY             SDO_GEOMETRY,
   ADAS_FLAG            NUMBER(1)                      default 2 not null
       check (ADAS_FLAG in (0,1,2)) disable,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   DIF_GROUPID          VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 6 not null
       check (SRC_FLAG in (1,2,3,4,5,6)) disable,
   DIGITAL_LEVEL        NUMBER(2)                      default 0 not null
       check (DIGITAL_LEVEL in (0,1,2,3,4)) disable,
   RESERVED             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NODE primary key (NODE_PID)
);


/*==============================================================*/
/* Table: RD_LINK                                               */
/*==============================================================*/
create table RD_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 7 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,9,10,11,13,15)) disable,
   DIRECT               NUMBER(1)                      default 1 not null
       check (DIRECT in (0,1,2,3)) disable,
   APP_INFO             NUMBER(1)                      default 1 not null
       check (APP_INFO in (0,1,2,3,5)) disable,
   TOLL_INFO            NUMBER(1)                      default 2 not null
       check (TOLL_INFO in (0,1,2,3)) disable,
   ROUTE_ADOPT          NUMBER(1)                      default 2 not null
       check (ROUTE_ADOPT in (0,1,2,3,4,5,9)) disable,
   MULTI_DIGITIZED      NUMBER(1)                      default 0 not null
       check (MULTI_DIGITIZED between 0 and 1 and MULTI_DIGITIZED in (0,1)) disable,
   DEVELOP_STATE        NUMBER(1)                      default 0 not null
       check (DEVELOP_STATE between 0 and 2 and DEVELOP_STATE in (0,1,2)) disable,
   IMI_CODE             NUMBER(1)                      default 0 not null
       check (IMI_CODE between 0 and 3 and IMI_CODE in (0,1,2,3)) disable,
   SPECIAL_TRAFFIC      NUMBER(1)                      default 0 not null
       check (SPECIAL_TRAFFIC between 0 and 1 and SPECIAL_TRAFFIC in (0,1)) disable,
   FUNCTION_CLASS       NUMBER(1)                      default 5 not null
       check (FUNCTION_CLASS between 0 and 5 and FUNCTION_CLASS in (0,1,2,3,4,5))disable,
   URBAN                NUMBER(1)                      default 0 not null
       check (URBAN between 0 and 1 and URBAN in (0,1)) disable,
   PAVE_STATUS          NUMBER(1)                      default 0 not null
       check (PAVE_STATUS between 0 and 1 and PAVE_STATUS in (0,1)) disable,
   LANE_NUM             NUMBER(2)                      default 2 not null,
   LANE_LEFT            NUMBER(2)                      default 0 not null,
   LANE_RIGHT           NUMBER(2)                      default 0 not null,
   LANE_WIDTH_LEFT      NUMBER(1)                      default 1 not null
       check (LANE_WIDTH_LEFT in (1,2,3)) disable ,
   LANE_WIDTH_RIGHT     NUMBER(1)                      default 1 not null
       check (LANE_WIDTH_RIGHT in (1,2,3)) disable,
   LANE_CLASS           NUMBER(1)                      default 2 not null
       check (LANE_CLASS between 0 and 3 and LANE_CLASS in (0,1,2,3)) disable,
   WIDTH                NUMBER(8)                      default 0 not null,
   IS_VIADUCT           NUMBER(1)                      default 0 not null
       check (IS_VIADUCT between 0 and 2 and IS_VIADUCT in (0,1,2)) disable,
   LEFT_REGION_ID       NUMBER(10)                     default 0 not null,
   RIGHT_REGION_ID      NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   ONEWAY_MARK          NUMBER(2)                      default 0 not null
       check (ONEWAY_MARK in (0,1)) disable,
   MESH_ID              NUMBER(6)                      default 0 not null,
   STREET_LIGHT         NUMBER(1)                      default 0 not null
       check (STREET_LIGHT between 0 and 2 and STREET_LIGHT in (0,1,2)) disable,
   PARKING_LOT          NUMBER(1)                      default 0 not null
       check (PARKING_LOT in (0,1,2)) disable,
   ADAS_FLAG            NUMBER(1)                      default 0 not null
       check (ADAS_FLAG in (0,1,2)) disable,
   SIDEWALK_FLAG        NUMBER(1)                      default 0 not null
       check (SIDEWALK_FLAG in (0,1,2)) disable,
   WALKSTAIR_FLAG       NUMBER(1)                      default 0 not null
       check (WALKSTAIR_FLAG in (0,1,2)) disable,
   DICI_TYPE            NUMBER(1)                      default 0 not null
       check (DICI_TYPE in (0,1,2)) ,
   WALK_FLAG            NUMBER(1)                      default 0 not null
       check (WALK_FLAG in (0,1,2)) disable,
   DIF_GROUPID          VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 6 not null
       check (SRC_FLAG in (1,2,3,4,5,6)) disable,
   DIGITAL_LEVEL        NUMBER(2)                      default 0 not null
       check (DIGITAL_LEVEL in (0,1,2,3,4)) disable,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   TRUCK_FLAG           NUMBER(1)                      default 0 not null
       check (TRUCK_FLAG in (0,1)) disable,
   FEE_STD              NUMBER(5,2)                    default 0 not null,
   FEE_FLAG             NUMBER(1)                      default 0 not null
       check (FEE_FLAG in (0,1,2)) disable,
   SYSTEM_ID            NUMBER(6)                      default 0 not null,
   ORIGIN_LINK_PID      NUMBER(10)                     default 0 not null,
   CENTER_DIVIDER       NUMBER(2)                      default 0 not null
       check (CENTER_DIVIDER in (0,10,11,12,13,20,21,30,31,40,50,51,60,61,62,63,99)) disable,
   PARKING_FLAG         NUMBER(1)                      default 0 not null
       check (PARKING_FLAG in (0,1)) disable,
   MEMO                 VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LINK primary key (LINK_PID),
   constraint RDLINK_SNODE foreign key (S_NODE_PID)
         references RD_NODE (NODE_PID) disable,
   constraint RDLINK_ENODE foreign key (E_NODE_PID)
         references RD_NODE (NODE_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASNODEINFO_INLINK foreign key (IN_LINK_PID)
         references ADAS_LINK (LINK_PID) disable ,
   constraint ADASNODEINFO_OUTLINK foreign key (OUT_LINK_PID)
         references ADAS_LINK (LINK_PID) disable ,
   constraint ADASNODEINFO_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: ADAS_SLOPE                                            */
/*==============================================================*/
create table ADAS_SLOPE 
(
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SLOPE                NUMBER(10,3)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASSLOPE_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID) disable ,
   constraint ADASSLOPE_LINK foreign key (LINK_PID)
         references ADAS_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: AD_ADMIN                                              */
/*==============================================================*/
create table AD_ADMIN 
(
   REGION_ID            NUMBER(10)           not null,
   ADMIN_ID             NUMBER(6)            default 0 not null,
   EXTEND_ID            NUMBER(4)            default 0 not null,
   ADMIN_TYPE           NUMBER(3,1)          default 0 not null
check (ADMIN_TYPE in (0,1,2,2.5,3,3.5,4,4.5,4.8,5,6,7,8,9)) disable ,
   CAPITAL              NUMBER(1)            default 0 not null
check (CAPITAL in (0,1,2,3)) disable ,
   POPULATION           VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   LINK_PID             NUMBER(10)           default 0 not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   SIDE                 NUMBER(1)            default 0 not null
check (SIDE in (0,1,2,3)) disable ,
   ROAD_FLAG            NUMBER(1)            default 0 not null
check (ROAD_FLAG in (0,1,2,3)) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   JIS_CODE             NUMBER(5)            default 0 not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN primary key (REGION_ID)
);

/*==============================================================*/
/* Table: AD_ADMIN_GROUP                                        */
/*==============================================================*/
create table AD_ADMIN_GROUP 
(
   GROUP_ID             NUMBER(10)           not null,
   REGION_ID_UP         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN_GROUP primary key (GROUP_ID),
   constraint ADADMIN_UPLEVEL foreign key (REGION_ID_UP)
         references AD_ADMIN (REGION_ID) disable
);

/*==============================================================*/
/* Table: AD_ADMIN_NAME                                         */
/*==============================================================*/
create table AD_ADMIN_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   REGION_ID            NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2,3,4)) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1,2,3,4,5,6)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN_NAME primary key (NAME_ID),
   constraint ADADMIN_NAMES foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADADMINNAME_TONE foreign key (NAME_ID)
         references AD_ADMIN_NAME (NAME_ID) disable
);

/*==============================================================*/
/* Table: AD_ADMIN_PART                                         */
/*==============================================================*/
create table AD_ADMIN_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   REGION_ID_DOWN       NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADADMIN_DOWNLEVEL foreign key (REGION_ID_DOWN)
         references AD_ADMIN (REGION_ID) disable ,
   constraint ADADMIN_UPDOWN foreign key (GROUP_ID)
         references AD_ADMIN_GROUP (GROUP_ID) disable
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_FACE primary key (FACE_PID),
   constraint ADADMIN_ADFACE foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID) disable
);

/*==============================================================*/
/* Table: AD_NODE                                               */
/*==============================================================*/
create table AD_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (1)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_NODE primary key (NODE_PID)
);

/*==============================================================*/
/* Table: AD_LINK                                               */
/*==============================================================*/
create table AD_LINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 1 not null
check (KIND in (0,1,2,3,4,5,6,7)) disable ,
   FORM                 NUMBER(1)            default 1 not null
check (FORM in (0,1,2,6,7,8,9)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
check (SCALE in (0,1,2)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_LINK primary key (LINK_PID),
   constraint ADLINK_SNODE foreign key (S_NODE_PID)
         references AD_NODE (NODE_PID) disable ,
   constraint ADLINK_ENODE foreign key (E_NODE_PID)
         references AD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: AD_FACE_TOPO                                          */
/*==============================================================*/
create table AD_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADFACE_LINK foreign key (LINK_PID)
         references AD_LINK (LINK_PID) disable ,
   constraint ADFACE_LINKS foreign key (FACE_PID)
         references AD_FACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: AD_NODE_100W                                          */
/*==============================================================*/
create table AD_NODE_100W 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (1,2)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_NODE_100W primary key (NODE_PID)
);

/*==============================================================*/
/* Table: AD_LINK_100W                                          */
/*==============================================================*/
create table AD_LINK_100W 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 1 not null
check (KIND in (0,1,2,3,4,5,6,7)) disable ,
   FORM                 NUMBER(1)            default 1 not null
check (FORM in (0,1,2,6,7,8,9)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
check (SCALE in (0,1,2)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_LINK_100W primary key (LINK_PID),
   constraint ADLINK_SNODE_100W foreign key (S_NODE_PID)
         references AD_NODE_100W (NODE_PID) disable ,
   constraint ADLINK_ENODE_100W foreign key (E_NODE_PID)
         references AD_NODE_100W (NODE_PID) disable
);

/*==============================================================*/
/* Table: AD_LINK_MESH                                          */
/*==============================================================*/
create table AD_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADLINK_MESH foreign key (LINK_PID)
         references AD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: AD_LINK_MESH_100W                                     */
/*==============================================================*/
create table AD_LINK_MESH_100W 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADLINK_MESH_100W foreign key (LINK_PID)
         references AD_LINK_100W (LINK_PID) disable
);

/*==============================================================*/
/* Table: AD_NODE_MESH                                          */
/*==============================================================*/
create table AD_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADNODE_MESH foreign key (NODE_PID)
         references AD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: AD_NODE_MESH_100W                                     */
/*==============================================================*/
create table AD_NODE_MESH_100W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ADNODE_MESH_100W foreign key (NODE_PID)
         references AD_NODE_100W (NODE_PID) disable
);

/*==============================================================*/
/* Table: AU_ADAS_MARK                                          */
/*==============================================================*/
create table AU_ADAS_MARK  (
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   X_LEAD               NUMBER(8,5)                    default 0 not null,
   Y_LEAD               NUMBER(8,5)                    default 0 not null,
   ANGLE                NUMBER(8,5)                    default 0 not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)) disable,
   LINK_PID             NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   MARK_ITEM            NUMBER(5)                      default 0 not null
       check (MARK_ITEM in (0,1,2)) disable,
   PARAM_L              NUMBER(10)                     default 0 not null,
   PARAM_R              NUMBER(10)                     default 0 not null,
   PARAM_S              VARCHAR2(1000),
   PARAM_EX             VARCHAR2(1000),
   STATUS               NUMBER(1)                      default 0 not null
       check (STATUS in (0,1,2,3,4,5,6,7,8)) disable,
   MESH_ID              NUMBER(6)                      default 0 not null,
   DESCRIPT             VARCHAR2(200),
   REMARK               VARCHAR2(200),
   MEMO                 VARCHAR2(200),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   IMP_DATE             DATE,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           CLOB,
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_ADAS_MARK primary key (MARK_ID)
);



/*==============================================================*/
/* Table: AU_ADAS_GPSRECORD                                     */
/*==============================================================*/
create table AU_ADAS_GPSRECORD 
(
   GPSRECORD_ID         NUMBER(10)           not null,
   MARK_ID              NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(1)            default 0 not null
check (SOURCE in (0,1)) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   MEMO                 VARCHAR2(255),
   constraint PK_AU_ADAS_GPSRECORD primary key (GPSRECORD_ID),
   constraint AUADAS_MARK_GPSRECORD foreign key (MARK_ID)
         references AU_ADAS_MARK (MARK_ID) disable
);

/*==============================================================*/
/* Table: AU_ADAS_GPSTRACK                                      */
/*==============================================================*/
create table AU_ADAS_GPSTRACK 
(
   GPSTRACK_ID          NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   RECORD_TIME          DATE,
   GPS_TEXT             VARCHAR2(255),
   MESH_ID              NUMBER(6)                      default 0 not null,
   WORKER               NUMBER(4)                      default 0 not null,
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_ADAS_GPSTRACK primary key (GPSTRACK_ID)
);


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

/*==============================================================*/
/* Table: AU_MARK                                               */
/*==============================================================*/
create table AU_MARK  (
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   X_LEAD               NUMBER(8,5)                    default 0 not null,
   Y_LEAD               NUMBER(8,5)                    default 0 not null,
   ANGLE                NUMBER(8,5)                    default 0 not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5,6,7,8,9)) disable,
   LINK_PID             NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   IN_WORKER            NUMBER(4)                      default 0 not null,
   MARK_ITEM            NUMBER(5)                      default 0 not null
       check (MARK_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,30,31,32,33,34,35,36,37,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,61,62,63,70,71,72,73,74,75,76,77,78,80,81,82,83,101,102,103,104,105,106,107,108,109,110,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224)) disable,
   PARAM_L              NUMBER(10)                     default 0 not null,
   PARAM_R              NUMBER(10)                     default 0 not null,
   PARAM_S              VARCHAR2(2000),
   PARAM_EX             VARCHAR2(2000),
   STATUS               NUMBER(2)                      default 0 not null
       check (STATUS in (0,1,2,3,4,5,6,11)) disable,
   CK_STATUS            NUMBER(2)                      default 0 not null
       check (CK_STATUS in (0,1,2,3,4,5)) disable,
   ADJA_FLAG            NUMBER(2)                      default 0
       check (ADJA_FLAG is null or (ADJA_FLAG in (0,1,2))) disable,
   MESH_ID              NUMBER(6)                      default 0 not null,
   DESCRIPT             VARCHAR2(200),
   REMARK               VARCHAR2(200),
   MEMO                 VARCHAR2(200),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   IMP_DATE             DATE,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   GDB_FEA_PID          NUMBER(10)                     default 0 not null,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_2           VARCHAR2(2000),
   PARAM_EX_3           VARCHAR2(2000),
   MESH_ID_2K           VARCHAR2(12),
   STATE                NUMBER(1)                      default 3 not null
       check (STATE in (1,2,3)) disable,
   MERGE_FLAG           NUMBER(1)                      default 0 not null
       check (MERGE_FLAG in (0,1,2)) disable,
   constraint PK_AU_MARK primary key (MARK_ID)
);


/*==============================================================*/
/* Table: AU_DRAFT                                              */
/*==============================================================*/
create table AU_DRAFT  
(
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   STYLE                NUMBER(1)                      default 0 not null
       check (STYLE in (0,1)) disable,
   COLOR                VARCHAR2(10) ,
   WIDTH                NUMBER(2)                      default 1 not null,
   GEO_SEG              VARCHAR2(500),
   TYPE                 NUMBER(2)                      default 0 not null
       check (TYPE in (0,1,2,3,4)) disable,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint AUDRAFT_MARK foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);



/*==============================================================*/
/* Table: AU_GPSRECORD                                          */
/*==============================================================*/
create table AU_GPSRECORD  
(
   GPSRECORD_ID         NUMBER(10)                      not null,
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(2)                      default 0 not null
       check (SOURCE in (0,1,2,3,4,5,6,9,10,11)) disable,
   NAME                 VARCHAR2(255),
   TABLE_NAME           VARCHAR2(64),
   LANE_NUM             NUMBER(2)                      default 0 not null,
   KIND                 NUMBER(5)                      default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   MEMO                 VARCHAR2(255),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint PK_AU_GPSRECORD primary key (GPSRECORD_ID),
   constraint AUGPSRECORD_MARK foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);

/*==============================================================*/
/* Table: AU_GPSTRACK_GROUP                                     */
/*==============================================================*/
create table AU_GPSTRACK_GROUP  (
   GROUP_ID             NUMBER(10)                      not null,
   START_TIME           VARCHAR2(20),
   END_TIME             VARCHAR2(20),
   WORKER               NUMBER(4)                      default 0 not null,
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   IMP_WORKER           NUMBER(4)                      default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint PK_AU_GPSTRACK_GROUP primary key (GROUP_ID)
);

/*==============================================================*/
/* Table: AU_GPSTRACK                                           */
/*==============================================================*/
create table AU_GPSTRACK  (
   GPSTRACK_ID          NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   SEQ_NUM              NUMBER(10)                      not null,
   DIRECTION            NUMBER(8,5)                    default 0 not null,
   SPEED                NUMBER(8,5)                    default 0 not null,
   STRING               VARCHAR2(1000),
   RECORD_TIME          DATE,
   MESH_ID              NUMBER(6)                      default 0 not null,
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_GPSTRACK primary key (GPSTRACK_ID),
   constraint FK_AUGPSTRACK_GROUP foreign key (GROUP_ID)
         references AU_GPSTRACK_GROUP (GROUP_ID) disable
);

/*==============================================================*/
/* Table: AU_GPSTRACK_GROUP_VIDEO                               */
/*==============================================================*/
create table AU_GPSTRACK_GROUP_VIDEO  (
   GROUP_ID             NUMBER(10)                      not null,
   VIDEO_ID             NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   constraint FK_AUGPSTRACKGROUP_VIDEO foreign key (GROUP_ID)
         references AU_GPSTRACK_GROUP (GROUP_ID) disable
);

/*==============================================================*/
/* Table: AU_GPSTRACK_PHOTO                                     */
/*==============================================================*/
create table AU_GPSTRACK_PHOTO  (
   GPSTRACK_ID          NUMBER(10)                      not null,
   PHOTO_GUID           NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUGPSTRACK_PHOTO foreign key (GPSTRACK_ID)
         references AU_GPSTRACK (GPSTRACK_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_ANNOTATION                                      */
/*==============================================================*/
create table AU_IX_ANNOTATION  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)                     default 1 not null,
   SRC_FLAG             NUMBER(1)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4,5)) disable,
   SRC_PID              NUMBER(10)                     default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)) disable,
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)) disable,
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)) disable,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   IMP_DATE             DATE,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_IX_ANNOTATION primary key (AUDATA_ID)
);

/*==============================================================*/
/* Table: AU_IX_ANNOTATION_NAME                                 */
/*==============================================================*/
create table AU_IX_ANNOTATION_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)                      default 1 not null
check (NAME_CLASS in (1,2)) disable,
   OLD_NAME             VARCHAR2(200),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint AUIX_ANNOTATION_NAME foreign key (AUDATA_ID)
         references AU_IX_ANNOTATION (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI                                             */
/*==============================================================*/
create table AU_IX_POI  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)) disable,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)) disable,
   PMESH_ID             NUMBER(6)                      default 0 not null,
   ADMIN_REAL           NUMBER(6)                      default 0 not null,
   IMPORTANCE           NUMBER(1)                      default 0 not null
       check (IMPORTANCE in (0,1)) disable,
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)                      default 0 not null
       check (ACCESS_FLAG in (0,1,2)) disable,
   OPEN_24H             NUMBER(1)                      default 0 not null
       check (OPEN_24H in (0,1,2)) disable,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POST_CODE            VARCHAR2(6),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(100),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)) disable,
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,9)) disable,
   EX_PRIORITY          VARCHAR2(10),
   EDITION_FLAG         VARCHAR2(12),
   POI_MEMO             VARCHAR2(200),
   OLD_BLOCKCODE        VARCHAR2(200),
   OLD_NAME             VARCHAR2(200),
   OLD_ADDRESS          VARCHAR2(200),
   OLD_KIND             VARCHAR2(8),
   POI_NUM              VARCHAR2(36),
   LOG                  VARCHAR2(200),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)) disable,
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)) disable,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   IMP_DATE             DATE,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   VERIFIED_FLAG        NUMBER(1)                      default 9 not null
       check (VERIFIED_FLAG in (0,1,2,3,9)) disable,
   constraint PK_AU_IX_POI primary key (AUDATA_ID)
);


/*==============================================================*/
/* Table: AU_IX_POINTADDRESS                                    */
/*==============================================================*/
create table AU_IX_POINTADDRESS  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   GUIDE_LINK_PID       NUMBER(10)                     default 0 not null,
   LOCATE_LINK_PID      NUMBER(10)                     default 0 not null,
   LOCATE_NAME_GROUPID  NUMBER(10)                     default 0 not null,
   GUIDE_LINK_SIDE      NUMBER(1)                      default 0 not null
       check (GUIDE_LINK_SIDE in (0,1,2,3)) disable,
   LOCATE_LINK_SIDE     NUMBER(1)                      default 0 not null
       check (LOCATE_LINK_SIDE in (0,1,2,3)) disable,
   SRC_PID              NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   IDCODE               VARCHAR2(36),
   DPR_NAME             VARCHAR2(100),
   DP_NAME              VARCHAR2(35),
   OPERATOR             VARCHAR2(32),
   MEMOIRE              VARCHAR2(200),
   DPF_NAME             VARCHAR2(500),
   POSTER_ID            VARCHAR2(100),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,2)) disable,
   VERIFED              VARCHAR2(1)                    default 'F' not null
       check (VERIFED in ('T','F')) disable,
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   LOG                  VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   RESERVED             VARCHAR2(1000),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)) disable,
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)) disable,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   IMP_DATE             DATE,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_IX_POINTADDRESS primary key (AUDATA_ID)
);



/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_PARENT                             */
/*==============================================================*/
create table AU_IX_POINTADDRESS_PARENT  (   
   AUDATA_ID            NUMBER(10)                      not null, 
   GROUP_ID             NUMBER(10)                     default 0 not null,
   PARENT_PA_PID        NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MODIFY_FLAG          NUMBER(1)                      default 0 not null
       check (MODIFY_FLAG in (0,1,2)) disable,
   constraint AU_IX_PADDRPARENT foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID) disable
);


/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_CHILDREN                           */
/*==============================================================*/
create table AU_IX_POINTADDRESS_CHILDREN  (
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   CHILD_PA_PID         NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   LABEL                NUMBER(1)                      default 0 not null
       check (LABEL in (0,1,2)) disable,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint AU_IX_PADDRCHILD foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID) disable
);


/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_NAME                               */
/*==============================================================*/
create table AU_IX_POINTADDRESS_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')) disable,
   SUM_CHAR             NUMBER(1)                      default 0 not null
check (SUM_CHAR in (0,1,2,3)) disable,
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
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint AUIX_POINTADDRESS_NAME foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID) disable
);

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
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
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
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_ADDRESS foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_BUILDING                                    */
/*==============================================================*/
create table AU_IX_POI_BUILDING 
(
   AUDATA_ID            NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0,
   FLOOR_USED           VARCHAR2(1000),
   FLOOR_EMPTY          VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POI_BUILDING foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_PARENT                                      */
/*==============================================================*/
create table AU_IX_POI_PARENT  (
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   PARENT_POI_PID       NUMBER(10)                     default 0 not null,
   TENANT_FLAG          NUMBER(2)                      default 0
       check (TENANT_FLAG is null or (TENANT_FLAG in (0,1))) disable,
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MODIFY_FLAG          NUMBER(1)                      default 0 not null
       check (MODIFY_FLAG in (0,1,2)) disable,
   constraint AU_IX_POI_PARENT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);


/*==============================================================*/
/* Table: AU_IX_POI_CHILDREN                                    */
/*==============================================================*/
create table AU_IX_POI_CHILDREN  (
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   CHILD_POI_PID        NUMBER(10)                     default 0 not null,
   RELATION_TYPE        NUMBER(1)                      default 0 not null
       check (RELATION_TYPE in (0,1,2)) disable,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   LABEL                NUMBER(1)                      default 0 not null
       check (LABEL in (0,1,2)) disable,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint AU_IX_POI_CHILD foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);


/*==============================================================*/
/* Table: AU_IX_POI_CONTACT                                     */
/*==============================================================*/
create table AU_IX_POI_CONTACT  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   OLD_CONTACT          VARCHAR2(128),
   CONTACT_TYPE         NUMBER(2)                      default 1 not null
       check (CONTACT_TYPE in (1,2,3,4,11,21,22)) disable,
   CONTACT              VARCHAR2(128),
   CONTACT_DEPART       NUMBER(3)                      default 0 not null,
   PRIORITY             NUMBER(5)                      default 1 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint AUIX_POI_CONTACT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);


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
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POIFLAG foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_HOTEL                                       */
/*==============================================================*/
create table AU_IX_POI_HOTEL  (
   AUDATA_ID            NUMBER(10)                      not null,
   HOTEL_ID             NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CREDIT_CARD          VARCHAR2(10),
   RATING               NUMBER(2)                      default 0 not null
       check (RATING in (0,1,3,4,5,6,7,8,13,14,15,16,17)) disable,
   CHECKIN_TIME         VARCHAR2(20)                   default '14:00' not null,
   CHECKOUT_TIME        VARCHAR2(20)                   default '12:00' not null,
   ROOM_COUNT           NUMBER(5)                      default 0 not null,
   ROOM_TYPE            VARCHAR2(20),
   ROOM_PRICE           VARCHAR2(100),
   BREAKFAST            NUMBER(2)                      default 0 not null
       check (BREAKFAST in (0,1)) disable,
   SERVICE              VARCHAR2(254),
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)) disable,
   LONG_DESCRIPTION     VARCHAR2(254),
   LONG_DESCRIP_ENG     VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   OPEN_HOUR_ENG        VARCHAR2(254),
   TELEPHONE            VARCHAR2(100),
   ADDRESS              VARCHAR2(200),
   CITY                 VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   constraint AUIX_POI_HOTEL foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_NAME                                        */
/*==============================================================*/
create table AU_IX_POI_NAME  (
   AUNAME_ID            NUMBER(10)                      not null,
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   NAME_CLASS           NUMBER(1)                      default 1 not null
check (NAME_CLASS in (1,3,4,5,6,7,8,9)) disable,
   NAME_TYPE            NUMBER(2)                      default 1 not null
check (NAME_TYPE in (1,2)) disable,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint PK_AU_IX_POI_NAME primary key (AUNAME_ID),
   constraint AUIX_POI_NAME foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

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
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUIX_POINAMEFLAG foreign key (AUNAME_ID)
         references AU_IX_POI_NAME (AUNAME_ID) disable
);

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
   ADDRESS              VARCHAR2(1000) ,
   STATE                NUMBER(2)  ,
   constraint PK_AU_IX_POI_NOKIA primary key (AUDATA_ID)
);

/*==============================================================*/
/* Table: AU_IX_POI_PHOTO                                       */
/*==============================================================*/
create table AU_IX_POI_PHOTO  
(
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   PHOTO_ID             NUMBER(10)                     default 0 not null,
   PHOTO_GUID           VARCHAR2(64),
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   POI_FIELD_GUID       VARCHAR2(64),
   TYPE                 NUMBER(1)                      default 2 not null
       check (TYPE in (1,2,3,4)) disable,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint AU_IX_POI_PHOTO foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);


/*==============================================================*/
/* Table: AU_IX_POI_RESTAURANT                                  */
/*==============================================================*/
create table AU_IX_POI_RESTAURANT  (
   AUDATA_ID            NUMBER(10)                      not null,
   RESTAURANT_ID        NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   FOOD_TYPE            VARCHAR2(32),
   CREDIT_CARD          VARCHAR2(20),
   AVG_COST             NUMBER(5)                      default 0 not null,
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)) disable,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint AUIX_POI_RESTAURANT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_SAMEPOI                                         */
/*==============================================================*/
create table AU_IX_SAMEPOI  (
   SAMEPOI_AUDATA_ID    NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   RELATION_TYPE        NUMBER(1)                      default 1 not null
       check (RELATION_TYPE in (1,2,3)) disable,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   constraint PK_AU_IX_SAMEPOI primary key (SAMEPOI_AUDATA_ID)
);

/*==============================================================*/
/* Table: AU_IX_SAMEPOI_PART                                    */
/*==============================================================*/
create table AU_IX_SAMEPOI_PART  (
   SAMEPOI_AUDATA_ID    NUMBER(10)                      not null,
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   constraint FK_AU_IX_SAMEPOIPART_SAMEPOI foreign key (SAMEPOI_AUDATA_ID)
         references AU_IX_SAMEPOI (SAMEPOI_AUDATA_ID) disable,
   constraint FK_AU_IX_SAMEPOIPART_POI foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

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

/*==============================================================*/
/* Table: AU_MARK_PHOTO                                         */
/*==============================================================*/
create table AU_MARK_PHOTO  
(
   MARK_ID              NUMBER(10)                      not null,
   PHOTO_ID             NUMBER(10)                     default 0 not null,
   PHOTO_GUID           VARCHAR2(64),
   STATUS               VARCHAR2(100),
   constraint AUMARK_PHOTO foreign key (MARK_ID)
         references AU_MARK (MARK_ID) disable
);


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



/*==============================================================*/
/* Table: AU_PHOTO                                              */
/*==============================================================*/
create table AU_PHOTO 
(
   PHOTO_ID             NUMBER(10)           not null,
   CLASS                NUMBER(2)            default 1 not null
check (CLASS in (1,2,3,4,5,6,7)) disable ,
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
check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_COMPANYID       VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_COMPANY primary key (AUDATA_ID)
);

/*==============================================================*/
/* Table: AU_PT_POI                                             */
/*==============================================================*/
create table AU_PT_POI  
(
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   POI_KIND             VARCHAR2(4),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)) disable,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)) disable,
   PMESH_ID             NUMBER(6)                      default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   ACCESS_CODE          VARCHAR2(32),
   ACCESS_TYPE          VARCHAR2(10)                   default '0' not null
       check (ACCESS_TYPE in ('0','1','2','3')) disable,
   ACCESS_METH          NUMBER(3)                      default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   POI_MEMO             VARCHAR2(200),
   OPERATOR             VARCHAR2(30),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   POI_NUM              VARCHAR2(100),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)) disable,
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)) disable,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   IMP_DATE             DATE,
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_PT_POI primary key (AUDATA_ID)
);


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
check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
check (AUTO_TICKET in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_ETAACCESS foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID) disable
);

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
check (TYPE in (11,12,13,14,15,21,31,32,33,41,42,16,51,52,53,54,61)) disable ,
   COLOR                VARCHAR2(10),
   NIDB_LINEID          VARCHAR2(32),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_LINE primary key (AUDATA_ID)
);

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
check (BIKE in ('0','1','2')) disable ,
   BIKE_PERIOD          VARCHAR2(200),
   IMAGE                VARCHAR2(20),
   RACK                 VARCHAR2(1)          default '0' not null
check (RACK in ('0','1','2')) disable ,
   DINNER               VARCHAR2(1)          default '0' not null
check (DINNER in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
check (TOILET in ('0','1','2')) disable ,
   SLEEPER              VARCHAR2(1)          default '0' not null
check (SLEEPER in ('0','1','2')) disable ,
   WHEEL_CHAIR          VARCHAR2(1)          default '0' not null
check (WHEEL_CHAIR in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
check (SMOKE in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_LINE_ETALINE foreign key (AUDATA_ID)
         references AU_PT_LINE (AUDATA_ID) disable
);

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
check (PRIVATE_PARK in ('0','1','2','3')) disable ,
   PRIVATE_PARK_PERIOD  VARCHAR2(200),
   CARPORT_EXACT        VARCHAR2(32)         default '0' ,
   CARPORT_ESTIMATE     VARCHAR2(1)          default '0' not null
check (CARPORT_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   BIKE_PARK            VARCHAR2(1)          default '0' not null
check (BIKE_PARK in ('0','1','2','3')) disable ,
   BIKE_PARK_PERIOD     VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)          default '0' not null
check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   MOBILE               VARCHAR2(1)          default '0' not null
check (MOBILE in ('0','1','2')) disable ,
   BAGGAGE_SECURITY     VARCHAR2(1)          default '0' not null
check (BAGGAGE_SECURITY in ('0','1','2')) disable ,
   LEFT_BAGGAGE         VARCHAR2(1)          default '0' not null
check (LEFT_BAGGAGE in ('0','1','2')) disable ,
   CONSIGNATION_EXACT   VARCHAR2(32)         default '0',
   CONSIGNATION_ESTIMATE VARCHAR2(1)          default '0' not null
check (CONSIGNATION_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   CONVENIENT           VARCHAR2(1)          default '0' not null
check (CONVENIENT in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
check (SMOKE in ('0','1','2')) disable ,
   BUILD_TYPE           VARCHAR2(1)          default '0' not null
check (BUILD_TYPE in ('0','1','2','3')) disable ,
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
check (AUTO_TICKET in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
check (TOILET in ('0','1','2')) disable ,
   WIFI                 VARCHAR2(1)          default '0' not null
check (WIFI in ('0','1','2')) disable ,
   OPEN_PERIOD          VARCHAR2(200),
   FARE_AREA            VARCHAR2(1),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_ETASTOP foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID) disable
);

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
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_LINE_NAME foreign key (AUDATA_ID)
         references AU_PT_LINE (AUDATA_ID) disable
);

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
check (COLLECT in (0,1)) disable ,
   P_LEVEL              NUMBER(2)            default 0 not null
check (P_LEVEL in (4,3,2,1,0,-1,-2,-3,-4,-5,-6)) disable ,
   TRANSIT_FLAG         NUMBER(1)            default 0 not null
check (TRANSIT_FLAG in (0,1)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_PLATFORMID      VARCHAR2(32),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   IMP_DATE             DATE,
   constraint PK_AU_PT_PLATFORM primary key (AUDATA_ID)
);

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
check (AVAILABLE in (0,1)) disable ,
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_PLATFORMACCESS foreign key (AUDATA_ID)
         references AU_PT_PLATFORM (AUDATA_ID) disable
);

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
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_PLATFORM_NAME foreign key (AUDATA_ID)
         references AU_PT_PLATFORM (AUDATA_ID) disable
);

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
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_POI_PARENT primary key (AUDATA_ID)
);

/*==============================================================*/
/* Table: AU_PT_POI_CHILDREN                                    */
/*==============================================================*/
create table AU_PT_POI_CHILDREN 
(
   AUDATA_ID            NUMBER(10),
   GROUP_ID             NUMBER(10)           default 0 not null,
   CHILD_POI_PID        NUMBER(10)           default 0 not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
check (RELATION_TYPE in (0,1,2)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_CHILDREN foreign key (AUDATA_ID)
         references AU_PT_POI_PARENT (AUDATA_ID) disable
);

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
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_POI_NAME foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID) disable
);

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
check (UP_DOWN is null or (UP_DOWN in ('Ｓ','Ｘ','Ｈ','ＮＨ','ＷＨ','ＣＨ','ＣＮＨ','ＣＷＨ'))) disable ,
   DISTANCE             VARCHAR2(10),
   TICKET_SYS           NUMBER(2)            default 0 not null
check (TICKET_SYS in (0,1,2,9)) disable ,
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
check (STATE in (0,1,2,3)) disable ,
   IMP_DATE             DATE,
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)           default 0 not null,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)            default 0 not null
check (GEO_OPRSTATUS in (0,1,2)) disable ,
   GEO_CHECKSTATUS      NUMBER(2)            default 0 not null
check (GEO_CHECKSTATUS in (0,1,2)) disable ,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_STRAND primary key (AUDATA_ID)
);

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
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2,3,4)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_STRAND_NAME foreign key (AUDATA_ID)
         references AU_PT_STRAND (AUDATA_ID) disable
);

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
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_STRAND_PLATFORM foreign key (AUDATA_ID)
         references AU_PT_STRAND (AUDATA_ID) disable
);

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
check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_SYSTEMID        VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint AUPT_COMPANY_SYSTEM foreign key (AUDATA_ID)
         references AU_PT_COMPANY (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_PT_TRANSFER                                        */
/*==============================================================*/
create table AU_PT_TRANSFER 
(
   AUDATA_ID            NUMBER(10)           not null,
   TRANSFER_ID          NUMBER(10)           default 0 not null,
   TRANSFER_TYPE        NUMBER(1)            default 1 not null
check (TRANSFER_TYPE in (0,1)) disable ,
   POI_FIR              NUMBER(10)           default 0 not null,
   POI_SEC              NUMBER(10)           default 0 not null,
   PLATFORM_FIR         NUMBER(10)           default 0 not null,
   PLATFORM_SEC         NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TRANSFER_TIME        NUMBER(2)            default 0 not null,
   EXTERNAL_FLAG        NUMBER(1)            default 0 not null
check (EXTERNAL_FLAG in (0,1,2)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(200),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   ATT_TASK_ID          NUMBER(10)           default 0 not null,
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)            default 0 not null
check (ATT_OPRSTATUS in (0,1,2)) disable ,
   ATT_CHECKSTATUS      NUMBER(2)            default 0 not null
check (ATT_CHECKSTATUS in (0,1,2)) disable ,
   constraint PK_AU_PT_TRANSFER primary key (AUDATA_ID)
);

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
check (RECEIVE_TYPE in (0,1,2,3)) disable ,
   DATA_ID              VARCHAR2(32),
   constraint PK_AU_RECEIVE primary key (ID)
);

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

/*==============================================================*/
/* Table: AU_SPECIALCASE                                        */
/*==============================================================*/
create table AU_SPECIALCASE 
(
   SPECIALCASE_ID       NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   STATUS               NUMBER(1)            default 1 not null
check (STATUS in (0,1)) disable ,
   TYPE                 NUMBER(5)            default 0 not null
check (TYPE in (0,1,2,3,4,5)) disable ,
   RANK                 NUMBER(1)            default 0 not null
check (RANK in (0,1,2,3)) disable ,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)            default 0 not null,
   DESCRIPT             VARCHAR2(2000),
   constraint PK_AU_SPECIALCASE primary key (SPECIALCASE_ID)
);

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

/*==============================================================*/
/* Table: AU_WHITEBOARD                                         */
/*==============================================================*/
create table AU_WHITEBOARD 
(
   WHITEBOARD_ID        NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   STYLE                NUMBER(1)            default 0 not null
check (STYLE in (0,1)) disable ,
   COLOR                VARCHAR2(10)         not null,
   WIDTH                NUMBER(2)            default 1 not null,
   constraint PK_AU_WHITEBOARD primary key (WHITEBOARD_ID)
);

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

/*==============================================================*/
/* Table: BI_PERSON                                             */
/*==============================================================*/
create table BI_PERSON 
(
   PERSON_ID            NUMBER(4)            not null,
   NAME                 VARCHAR2(16),
   GENDER               NUMBER(1)            default 0 not null
check (GENDER in (0,1)) disable ,
   DEPARTMENT           VARCHAR2(100),
   WORK_GROUP           VARCHAR2(64),
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_PERSON primary key (PERSON_ID)
);

/*==============================================================*/
/* Table: BI_ROLE                                               */
/*==============================================================*/
create table BI_ROLE 
(
   ROLE_ID              NUMBER(2)            not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1,2,3)) disable ,
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_ROLE primary key (ROLE_ID)
);

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

/*==============================================================*/
/* Table: BI_POWER                                              */
/*==============================================================*/
create table BI_POWER 
(
   POWER_ID             NUMBER(2)            not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1,2,3,4)) disable ,
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_POWER primary key (POWER_ID)
);

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

/*==============================================================*/
/* Table: CK_EXCEPTION                                          */
/*==============================================================*/
create table CK_EXCEPTION  (
   EXCEPTION_ID         NUMBER(10)                      not null,
   RULE_ID              VARCHAR2(100),
   TASK_NAME            VARCHAR2(50),
   STATUS               NUMBER(2)                      default 0 not null
       check (STATUS in (0,1,2,3)) disable,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   RANK                 NUMBER(10)                     default 0 not null,
   SITUATION            VARCHAR2(4000),
   INFORMATION          VARCHAR2(4000),
   SUGGESTION           VARCHAR2(4000),
   GEOMETRY             VARCHAR2(4000),
   TARGETS              CLOB,
   ADDITION_INFO        CLOB,
   MEMO                 VARCHAR2(500),
   CREATE_DATE          DATE,
   UPDATE_DATE          DATE,
   MESH_ID              NUMBER(6)                       not null,
   SCOPE_FLAG           NUMBER(2)                      default 1 not null
       check (SCOPE_FLAG in (1,2,3)) disable,
   PROVINCE_NAME        VARCHAR2(60),
   MAP_SCALE            NUMBER(2)                      default 0 not null
       check (MAP_SCALE in (0,1,2,3)) disable,
   RESERVED             VARCHAR2(1000),
   EXTENDED             VARCHAR2(1000),
   TASK_ID              VARCHAR2(500),
   QA_TASK_ID           VARCHAR2(500),
   QA_STATUS            NUMBER(2)                      default 2 not null
       check (QA_STATUS in (1,2)) disable,
   WORKER               VARCHAR2(500),
   QA_WORKER            VARCHAR2(500),
   MEMO_1               VARCHAR2(500),
   MEMO_2               VARCHAR2(500),
   MEMO_3               VARCHAR2(500),
   constraint PK_CK_EXCEPTION primary key (EXCEPTION_ID)
);


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

/*==============================================================*/
/* Table: CMG_BUILDING                                          */
/*==============================================================*/
create table CMG_BUILDING 
(
   PID                  NUMBER(10)           not null,
   KIND                 VARCHAR2(8)         
check (KIND is null or (KIND in ('1001','1002','2001','3001','3002','4001','4002','4003','5001','5002','5003','5004','5005','6001','6002','6003','7001','8001','8002','8003','9001','9002','9003','9004','1101','1102','1103','1201','1202','1203','1204','1301','1302','1401','1402','1403','1501','1502','1503','1504','1505','1601'))) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDING primary key (PID)
);

/*==============================================================*/
/* Table: CMG_BUILDFACE                                         */
/*==============================================================*/
create table CMG_BUILDFACE 
(
   FACE_PID             NUMBER(10)           not null,
   BUILDING_PID         NUMBER(10)           not null,
   MASSING              NUMBER(1)            default 0 not null
check (MASSING in (0,1)) disable ,
   HEIGHT               NUMBER(5,1)          default 0 not null,
   HEIGHT_ACURACY       NUMBER(3,1)          default 0.5 not null
check (HEIGHT_ACURACY in (0,0.5,1,5,10)) disable ,
   HEIGHT_SOURCE        NUMBER(2)            default 1 not null
check (HEIGHT_SOURCE in (1,2,3,4,5)) disable ,
   DATA_SOURCE          NUMBER(2)            default 3 not null
check (DATA_SOURCE in (1,2,3,4)) disable ,
   WALL_MATERIAL        NUMBER(2)            default 1 not null
check (WALL_MATERIAL in (1,2,3,4,5,6,7,8,9)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   CREATE_TIME          DATE,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDFACE primary key (FACE_PID),
   constraint CMGBUILDFACE_BUILDING foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDFACE_TENANT                                  */
/*==============================================================*/
create table CMG_BUILDFACE_TENANT 
(
   FACE_PID             NUMBER(10)           not null,
   POI_PID              NUMBER(10)           default 0 not null,
   FLAG                 NUMBER(1)            default 0 not null
check (FLAG in (0,1)) disable ,
   TEL                  VARCHAR2(64),
   X                    NUMBER(10,5)         default 0 not null,
   Y                    NUMBER(10,5)         default 0 not null,
   NAME                 VARCHAR2(200),
   FLOOR                VARCHAR2(200),
   SRC_FLAG             NUMBER(1)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDFACE_TENANT foreign key (FACE_PID)
         references CMG_BUILDFACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDNODE                                         */
/*==============================================================*/
create table CMG_BUILDNODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDNODE primary key (NODE_PID)
);

/*==============================================================*/
/* Table: CMG_BUILDLINK                                         */
/*==============================================================*/
create table CMG_BUILDLINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (0,1)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDLINK primary key (LINK_PID),
   constraint CMGBUILDLINK_SNODE foreign key (S_NODE_PID)
         references CMG_BUILDNODE (NODE_PID) disable ,
   constraint CMGBUILDLINK_ENODE foreign key (E_NODE_PID)
         references CMG_BUILDNODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDFACE_TOPO                                    */
/*==============================================================*/
create table CMG_BUILDFACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDFACE_LINKS foreign key (FACE_PID)
         references CMG_BUILDFACE (FACE_PID) disable ,
   constraint CMGBUILDFACE_LINK foreign key (LINK_PID)
         references CMG_BUILDLINK (LINK_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDING_3DICON foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDING_3DMODEL                                  */
/*==============================================================*/
create table CMG_BUILDING_3DMODEL 
(
   MODEL_ID             NUMBER(10)           default 0 not null,
   BUILDING_PID         NUMBER(10)           not null,
   RESOLUTION           NUMBER(1)            default 0 not null
check (RESOLUTION in (0,1,2)) disable ,
   MODEL_NAME           VARCHAR2(100),
   MATERIAL_NAME        VARCHAR2(100),
   TEXTURE_NAME         VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDING_3DMODEL primary key (MODEL_ID),
   constraint CMGBUILDING_3DMODEL foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDING_NAME                                     */
/*==============================================================*/
create table CMG_BUILDING_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   BUILDING_PID         NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')) disable ,
   FULL_NAME            VARCHAR2(256),
   BASE_NAME            VARCHAR2(256),
   BUILD_NUMBER         VARCHAR2(64),
   FULL_NAME_PHONETIC   VARCHAR2(1000),
   BASE_NAME_PHONETIC   VARCHAR2(1000),
   BUILD_NUM_PHONETIC   VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CMG_BUILDING_NAME primary key (NAME_ID),
   constraint CMGBUILDING_NAME foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable
);

/*==============================================================*/
/* Table: IX_POI                                                */
/*==============================================================*/
create table IX_POI  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)) disable,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)) disable,
   PMESH_ID             NUMBER(6)                      default 0 not null,
   ADMIN_REAL           NUMBER(6)                      default 0 not null,
   IMPORTANCE           NUMBER(1)                      default 0 not null
       check (IMPORTANCE in (0,1)) disable,
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)                      default 0 not null
       check (ACCESS_FLAG in (0,1,2)) disable,
   OPEN_24H             NUMBER(1)                      default 0 not null
       check (OPEN_24H in (0,1,2)) disable,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POST_CODE            VARCHAR2(6),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(500),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)) disable,
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,9)) disable,
   EX_PRIORITY          VARCHAR2(10),
   EDITION_FLAG         VARCHAR2(12),
   POI_MEMO             VARCHAR2(200),
   OLD_BLOCKCODE        VARCHAR2(200),
   OLD_NAME             VARCHAR2(200),
   OLD_ADDRESS          VARCHAR2(200),
   OLD_KIND             VARCHAR2(8),
   POI_NUM              VARCHAR2(36),
   LOG                  VARCHAR2(200),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   VERIFIED_FLAG        NUMBER(1)                      default 9 not null
       check (VERIFIED_FLAG in (0,1,2,3,9)) disable,
   COLLECT_TIME         VARCHAR2(15),
   GEO_ADJUST_FLAG      NUMBER(1)                      default 9 not null
       check (GEO_ADJUST_FLAG in (0,1,9)) disable,
   FULL_ATTR_FLAG       NUMBER(1)                      default 9 not null
       check (FULL_ATTR_FLAG in (0,1,9)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI primary key (PID)
);


/*==============================================================*/
/* Table: CMG_BUILDING_POI                                      */
/*==============================================================*/
create table CMG_BUILDING_POI 
(
   BUILDING_PID         NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDING_POI foreign key (BUILDING_PID)
         references CMG_BUILDING (PID) disable ,
   constraint IXPOI_CMGBUILDINGPOI foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDLINK_MESH                                    */
/*==============================================================*/
create table CMG_BUILDLINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDLINK_MESH foreign key (LINK_PID)
         references CMG_BUILDLINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: CMG_BUILDNODE_MESH                                    */
/*==============================================================*/
create table CMG_BUILDNODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMGBUILDNODE_MESH foreign key (NODE_PID)
         references CMG_BUILDNODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: CM_BUILDING                                           */
/*==============================================================*/
create table CM_BUILDING 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDING primary key (PID)
);

/*==============================================================*/
/* Table: CM_BUILDFACE                                          */
/*==============================================================*/
create table CM_BUILDFACE 
(
   FACE_PID             NUMBER(10)           not null,
   BUILDING_PID         NUMBER(10)           not null,
   KIND                 VARCHAR2(8)         
check (KIND is null or (KIND in ('7110169','7110194','7110193','7110010','7110022','7110021','7110090','7110080','7110071','7110167','7110070','7110072','7110060','7110040','7110041','7110050','7110195','7110255'))) disable ,
   MASSING              NUMBER(1)            default 0 not null
check (MASSING in (0,1)) disable ,
   HEIGHT               NUMBER(3)            default 0 not null,
   LANDMARK_CODE        VARCHAR2(16),
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDFACE primary key (FACE_PID),
   constraint CMBUILDFACE_BUILDING foreign key (BUILDING_PID)
         references CM_BUILDING (PID) disable
);

/*==============================================================*/
/* Table: CM_BUILDNODE                                          */
/*==============================================================*/
create table CM_BUILDNODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDNODE primary key (NODE_PID)
);

/*==============================================================*/
/* Table: CM_BUILDLINK                                          */
/*==============================================================*/
create table CM_BUILDLINK 
(
   LINK_PID             NUMBER(10)           not null,
   S_NODE_PID           NUMBER(10)           not null,
   E_NODE_PID           NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (0,1)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)         default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_CM_BUILDLINK primary key (LINK_PID),
   constraint CMBUILDLINK_SNODE foreign key (S_NODE_PID)
         references CM_BUILDNODE (NODE_PID) disable ,
   constraint CMBUILDLINK_ENODE foreign key (E_NODE_PID)
         references CM_BUILDNODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: CM_BUILDFACE_TOPO                                     */
/*==============================================================*/
create table CM_BUILDFACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMBUILDFACE_LINKS foreign key (FACE_PID)
         references CM_BUILDFACE (FACE_PID) disable ,
   constraint CMBUILDFACE_LINK foreign key (LINK_PID)
         references CM_BUILDLINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: CM_BUILDLINK_MESH                                     */
/*==============================================================*/
create table CM_BUILDLINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMBUILDLINK_MESH foreign key (LINK_PID)
         references CM_BUILDLINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: CM_BUILDNODE_MESH                                     */
/*==============================================================*/
create table CM_BUILDNODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint CMBUILDNODE_MESH foreign key (NODE_PID)
         references CM_BUILDNODE (NODE_PID) disable
);

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

/*==============================================================*/
/* Table: EF_IMAGE                                              */
/*==============================================================*/
create table EF_IMAGE 
(
   IMAGE_ID             NUMBER(10)           not null,
   WIDTH                NUMBER(5)            default 0 not null,
   HEIGHT               NUMBER(5)            default 0 not null,
   ALPHA                NUMBER(1)            default 1 not null
check (ALPHA in (1,2)) disable ,
   TYPE                 NUMBER(1)            default 1 not null
check (TYPE in (1,2)) disable ,
   FORMAT               NUMBER(1)            default 2 not null
check (FORMAT in (1,2)) disable ,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_IMAGE primary key (IMAGE_ID)
);

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

/*==============================================================*/
/* Table: HW_ESTAB                                              */
/*==============================================================*/
create table HW_ESTAB 
(
   PID                  NUMBER(10)           not null,
   FLAG                 NUMBER(2)            default 1 not null
check (FLAG in (1,2)) disable ,
   ATTR                 NUMBER(2)            default 0 not null
check (ATTR in (0,1,2,4,8,16,32)) disable ,
   SUFFIX               NUMBER(2)            default 0 not null
check (SUFFIX in (0,1,2,3,4,5,8,16,17,18,19,24,25,26)) disable ,
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB primary key (PID)
);

/*==============================================================*/
/* Table: HW_ESTAB_MAIN                                         */
/*==============================================================*/
create table HW_ESTAB_MAIN 
(
   GROUP_ID             NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10)           not null,
   REL_TYPE             NUMBER(2)            default 0
check (REL_TYPE is null or (REL_TYPE in (0,1))) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB_MAIN primary key (GROUP_ID),
   constraint HWESTAB_MAIN foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

/*==============================================================*/
/* Table: HW_ESTAB_CONTAIN                                      */
/*==============================================================*/
create table HW_ESTAB_CONTAIN 
(
   GROUP_ID             NUMBER(10)           not null,
   ESTAB_PID            NUMBER(10)           not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint HWESTAB_MAIN_CONTAIN foreign key (GROUP_ID)
         references HW_ESTAB_MAIN (GROUP_ID) disable ,
   constraint HWESTAB_CONTAIN foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

/*==============================================================*/
/* Table: HW_ESTAB_JCT                                          */
/*==============================================================*/
create table HW_ESTAB_JCT 
(
   JCT_PID              NUMBER(10)           not null,
   S_ESTAB_PID          NUMBER(10)           not null,
   E_ESTAB_PID          NUMBER(10)           not null,
   JCTLINK_PID          NUMBER(10)           default 0 not null,
   DIS_BETW             NUMBER(15,3)         default 0 not null,
   ORIETATION           NUMBER(8,5)          default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB_JCT primary key (JCT_PID),
   constraint HWESTAB_JCT_S foreign key (S_ESTAB_PID)
         references HW_ESTAB (PID) disable ,
   constraint HWESTAB_JCT_E foreign key (E_ESTAB_PID)
         references HW_ESTAB (PID) disable
);

/*==============================================================*/
/* Table: HW_ESTAB_NAME                                         */
/*==============================================================*/
create table HW_ESTAB_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 1 not null,
   ESTAB_PID            NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(1000),
   PHONETIC             VARCHAR2(1000),
   VOICE_FLAG           NUMBER(2)            default 0 not null
check (VOICE_FLAG in (0,1)) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ESTAB_NAME primary key (NAME_ID),
   constraint HWESTAB_NAME foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

/*==============================================================*/
/* Table: HW_ESTAB_ROUTE_POS                                    */
/*==============================================================*/
create table HW_ESTAB_ROUTE_POS 
(
   ESTAB_PID            NUMBER(10)           not null,
   ROUTE_PID            NUMBER(10)           not null,
   POSITION_PID         NUMBER(10)           not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint HWESTAB_ROUTE_POS foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);

/*==============================================================*/
/* Table: HW_ESTAB_SA                                           */
/*==============================================================*/
create table HW_ESTAB_SA 
(
   ESTAB_PID            NUMBER(10),
   INNER_ITEM           NUMBER(3)            default 0
check (INNER_ITEM is null or (INNER_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,99))) disable ,
   KIND                 VARCHAR2(32),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint HWESTAB_SA foreign key (ESTAB_PID)
         references HW_ESTAB (PID) disable
);


/*==============================================================*/
/* Table: HW_POSITION                                           */
/*==============================================================*/
create table HW_POSITION 
(
   POSITION_PID         NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   ACCESS_TYPE          NUMBER(1)            default 1 not null
check (ACCESS_TYPE in (1,2)) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_POSITION primary key (POSITION_PID),
   constraint HWPOSITION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint HWPOSITION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: HW_ROUTE                                              */
/*==============================================================*/
create table HW_ROUTE 
(
   ROUTE_PID            NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   ACCESS_TYPE          NUMBER(1)            default 1 not null
check (ACCESS_TYPE in (1,2,4)) disable ,
   PRE_NODEPID          NUMBER(10)           default 0,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HW_ROUTE primary key (ROUTE_PID),
   constraint HWROUTE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint HWROUTE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

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

/*==============================================================*/
/* Table: IX_ANNOTATION                                         */
/*==============================================================*/
create table IX_ANNOTATION  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)                     default 1 not null,
   SRC_FLAG             NUMBER(1)                      default 0 not null
check (SRC_FLAG in (0,1,2,3,4,5)) disable,
   SRC_PID              NUMBER(10)                     default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
check (EDIT_FLAG in (0,1)) disable,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
check (DETAIL_FLAG in (0,1,2,3)) disable,
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION primary key (PID)
);

/*==============================================================*/
/* Table: IX_ANNOTATION_100W                                    */
/*==============================================================*/
create table IX_ANNOTATION_100W  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)                     default 1 not null,
   SRC_FLAG             NUMBER(1)                      default 0 not null
check (SRC_FLAG in (0,1,2,3,4,5)) disable,
   SRC_PID              NUMBER(10)                     default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
check (EDIT_FLAG in (0,1)) disable,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
check (DETAIL_FLAG in (0,1,2,3)) disable,
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION_100W primary key (PID)
);




/*==============================================================*/
/* Table: IX_ANNOTATION_FLAG                                    */
/*==============================================================*/
create table IX_ANNOTATION_FLAG 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXANNOTATION_FLAG foreign key (PID)
         references IX_ANNOTATION (PID) disable
);

/*==============================================================*/
/* Table: IX_ANNOTATION_FLAG_100W                               */
/*==============================================================*/
create table IX_ANNOTATION_FLAG_100W 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXANNOTATION_FLAG_100W foreign key (PID)
         references IX_ANNOTATION_100W (PID) disable
);

/*==============================================================*/
/* Table: IX_ANNOTATION_NAME                                    */
/*==============================================================*/
create table IX_ANNOTATION_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)                      default 1 not null
check (NAME_CLASS in (1,2)) disable,
   OLD_NAME             VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION_NAME primary key (NAME_ID),
   constraint IXANNOTATION_NAME foreign key (PID)
         references IX_ANNOTATION (PID) disable
);

/*==============================================================*/
/* Table: IX_ANNOTATION_NAME_100W                               */
/*==============================================================*/
create table IX_ANNOTATION_NAME_100W  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)                      default 1 not null
check (NAME_CLASS in (1,2)) disable,
   OLD_NAME             VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ANNOTATION_NAME_100W primary key (NAME_ID),
   constraint IXANNOTATION_NAME_100W foreign key (PID)
         references IX_ANNOTATION_100W (PID) disable
);

/*==============================================================*/
/* Table: IX_CROSSPOINT                                         */
/*==============================================================*/
create table IX_CROSSPOINT 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1)) disable ,
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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_CROSSPOINT primary key (PID)
);

/*==============================================================*/
/* Table: IX_HAMLET                                             */
/*==============================================================*/
create table IX_HAMLET  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8)                    
       check (KIND_CODE is null or (KIND_CODE in ('260100','260200','260000'))) disable,
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 VARCHAR2(1)                    default '0' not null
       check (SIDE in ('0','1','2','3')) disable,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)) disable,
   PMESH_ID             NUMBER(6)                      default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   POI_NUM              VARCHAR2(36),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_HAMLET primary key (PID)
);

/*==============================================================*/
/* Table: IX_HAMLET_FLAG                                        */
/*==============================================================*/
create table IX_HAMLET_FLAG 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXHAMLET_FLAG foreign key (PID)
         references IX_HAMLET (PID) disable
);

/*==============================================================*/
/* Table: IX_HAMLET_NAME                                        */
/*==============================================================*/
create table IX_HAMLET_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_HAMLET_NAME primary key (NAME_ID),
   constraint IXHAMLE_NAME foreign key (PID)
         references IX_HAMLET (PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXHAMLETNAME_TONE foreign key (NAME_ID)
         references IX_HAMLET_NAME (NAME_ID) disable
);

/*==============================================================*/
/* Table: IX_IC                                                 */
/*==============================================================*/
create table IX_IC 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
check (ROAD_FLAG in (0,1,2,3)) disable ,
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   KIND_CODE            VARCHAR2(8)         
check (KIND_CODE is null or (KIND_CODE in ('230205','230203','230204'))) disable ,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_IC primary key (PID)
);

/*==============================================================*/
/* Table: IX_KEYWORD                                            */
/*==============================================================*/
/*闇�眰鏉ヨ嚜浜庤�鐜�
create table IX_KEYWORD 
(
   PID                  NUMBER(10)           not null,
   KEYWORD              VARCHAR2(32),
   PINYIN               VARCHAR2(254),
   PRIORITY             NUMBER(1)            default 0 not null,
   ADMIN_AREA           VARCHAR2(6),
   constraint PK_IX_KEYWORD primary key (PID)
);*/

/*==============================================================*/
/* Table: IX_KEYWORD_KIND                                       */
/*==============================================================*/
/*闇�眰鏉ヨ嚜浜庤�鐜�
create table IX_KEYWORD_KIND 
(
   PID                  NUMBER(10)           not null,
   KIND                 VARCHAR2(32),
   constraint IXKEYWORD_KIND foreign key (PID)
         references IX_KEYWORD (PID) disable
);*/

/*==============================================================*/
/* Table: IX_POINTADDRESS                                       */
/*==============================================================*/
create table IX_POINTADDRESS  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   GUIDE_LINK_PID       NUMBER(10)                     default 0 not null,
   LOCATE_LINK_PID      NUMBER(10)                     default 0 not null,
   LOCATE_NAME_GROUPID  NUMBER(10)                     default 0 not null,
   GUIDE_LINK_SIDE      NUMBER(1)                      default 0 not null
       check (GUIDE_LINK_SIDE in (0,1,2,3)) disable,
   LOCATE_LINK_SIDE     NUMBER(1)                      default 0 not null
       check (LOCATE_LINK_SIDE in (0,1,2,3)) disable,
   SRC_PID              NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   IDCODE               VARCHAR2(36),
   DPR_NAME             VARCHAR2(100),
   DP_NAME              VARCHAR2(35),
   OPERATOR             VARCHAR2(32),
   MEMOIRE              VARCHAR2(200),
   DPF_NAME             VARCHAR2(500),
   POSTER_ID            VARCHAR2(100),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,2)) disable,
   VERIFED              VARCHAR2(1)                    default 'F' not null
       check (VERIFED in ('T','F')) disable,
   LOG                  VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   RESERVED             VARCHAR2(1000),
   TASK_ID              NUMBER(10)                     default 0 not null,
   SRC_TYPE             VARCHAR2(100),
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   constraint PK_IX_POINTADDRESS primary key (PID)
);

/*==============================================================*/
/* Table: IX_POINTADDRESS_PARENT                                */
/*==============================================================*/
create table IX_POINTADDRESS_PARENT 
(
   GROUP_ID             NUMBER(10)           not null,
   PARENT_PA_PID        NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POINTADDRESS_PARENT primary key (GROUP_ID),
   constraint IXPOINTADDRESS_PARENT foreign key (PARENT_PA_PID)
         references IX_POINTADDRESS (PID) disable
);

/*==============================================================*/
/* Table: IX_POINTADDRESS_CHILDREN                              */
/*==============================================================*/
create table IX_POINTADDRESS_CHILDREN 
(
   GROUP_ID             NUMBER(10)           not null,
   CHILD_PA_PID         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINTADDRESS_PARENT_CHILD foreign key (GROUP_ID)
         references IX_POINTADDRESS_PARENT (GROUP_ID) disable ,
   constraint IXPOINTADDRESS_CHILDREN foreign key (CHILD_PA_PID)
         references IX_POINTADDRESS (PID) disable
);

/*==============================================================*/
/* Table: IX_POINTADDRESS_FLAG                                  */
/*==============================================================*/
create table IX_POINTADDRESS_FLAG 
(
   PID                  NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINTADDRESS_FLAG foreign key (PID)
         references IX_POINTADDRESS (PID) disable
);

/*==============================================================*/
/* Table: IX_POINTADDRESS_NAME                                  */
/*==============================================================*/
create table IX_POINTADDRESS_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')) disable,
   SUM_CHAR             NUMBER(1)                      default 0 not null
check (SUM_CHAR in (0,1,2,3)) disable,
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
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POINTADDRESS_NAME primary key (NAME_ID),
   constraint IXPOINTADDRESS_NAME foreign key (PID)
         references IX_POINTADDRESS (PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINTADDRESSNAME_TONE foreign key (NAME_ID)
         references IX_POINTADDRESS_NAME (NAME_ID) disable
);

/*==============================================================*/
/* Table: IX_POI_ADDRESS                                        */
/*==============================================================*/
create table IX_POI_ADDRESS 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ADDRESS primary key (NAME_ID),
   constraint IXPOI_ADDRESS foreign key (POI_PID)
         references IX_POI (PID) disable
);

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
check (PRIORITY in (1,2,3,4,5,6,7,8,9)) disable ,
   START_TIME           VARCHAR2(100),
   END_TIME             VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ADVERTISEMENT primary key (ADVERTISE_ID)
);

/*==============================================================*/
/* Table: IX_POI_ATTRACTION                                     */
/*==============================================================*/
create table IX_POI_ATTRACTION  (
   ATTRACTION_ID        NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   SIGHT_LEVEL          NUMBER(2)                      default 0 not null
       check (SIGHT_LEVEL in (0,1,2,3,4,5)) disable,
   LONG_DESCRIPTION     VARCHAR2(254),
   LONG_DESCRIP_ENG     VARCHAR2(254),
   TICKET_PRICE         VARCHAR2(254),
   TICKET_PRICE_ENG     VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   OPEN_HOUR_ENG        VARCHAR2(254),
   TELEPHONE            VARCHAR2(100),
   ADDRESS              VARCHAR2(200),
   CITY                 VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)) disable,
   TRAVELGUIDE_FLAG     NUMBER(1)                      default 0 not null
       check (TRAVELGUIDE_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ATTRACTION primary key (ATTRACTION_ID)
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_AUDIO foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_BUILDING                                       */
/*==============================================================*/
create table IX_POI_BUILDING 
(
   POI_PID              NUMBER(10)           default 0 not null,
   FLOOR_USED           VARCHAR2(1000),
   FLOOR_EMPTY          VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_BUILDING foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_BUSINESSTIME                                   */
/*==============================================================*/
create table IX_POI_BUSINESSTIME  (
   POI_PID              NUMBER(10)                     default 0 not null,
   MON_SRT              VARCHAR2(10),
   MON_END              VARCHAR2(10),
   WEEK_IN_YEAR_SRT     VARCHAR2(10),
   WEEK_IN_YEAR_END     VARCHAR2(10),
   WEEK_IN_MONTH_SRT    VARCHAR2(10),
   WEEK_IN_MONTH_END    VARCHAR2(10),
   VALID_WEEK           VARCHAR2(10),
   DAY_SRT              VARCHAR2(10),
   DAY_END              VARCHAR2(10),
   TIME_SRT             VARCHAR2(10),
   TIME_DUR             VARCHAR2(10),
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000)
);

/*==============================================================*/
/* Table: IX_POI_CARRENTAL                                      */
/*==============================================================*/
create table IX_POI_CARRENTAL  (
   POI_PID              NUMBER(10)                     default 0 not null,
   OPEN_HOUR            VARCHAR2(254),
   ADDRESS              VARCHAR2(254),
   HOW_TO_GO            VARCHAR2(254),
   PHONE_400            VARCHAR2(50),
   WEB_SITE             VARCHAR2(254),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000)
);

/*==============================================================*/
/* Table: IX_POI_CHARGINGSTATION                                */
/*==============================================================*/
create table IX_POI_CHARGINGSTATION  (
   CHARGING_ID          NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CHARGING_TYPE        NUMBER(2)                      default 3 not null
       check (CHARGING_TYPE in (1,2,3,4)) disable,
   CHARGING_NUM         NUMBER(5)                      default 0 not null,
   PAYMENT              VARCHAR2(20)                   default '0' not null,
   SERVICE_PROV         VARCHAR2(5)                    default '0' not null,
   MEMO                 VARCHAR2(500),
   PHOTO_NAME           VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   PARKING_FEES         NUMBER(1)                      default 0 not null
       check (PARKING_FEES in (0,1)) disable,
   PARKING_INFO         VARCHAR2(254),
   AVAILABLE_STATE      NUMBER(1)                      default 0 not null
       check (AVAILABLE_STATE in (0,1,2,3,4)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_CHARGINGSTATION primary key (CHARGING_ID)
);

/*==============================================================*/
/* Table: IX_POI_CHARGINGPLOT                                   */
/*==============================================================*/
create table IX_POI_CHARGINGPLOT  (
   POI_PID              NUMBER(10)                     default 0 not null,
   GROUP_ID             NUMBER(5)                      default 1 not null,
   COUNT                NUMBER(5)                      default 1 not null,
   ACDC                 NUMBER(1)                      default 0 not null
       check (ACDC in (0,1)) disable,
   PLUG_TYPE            VARCHAR2(20)                   default '9' not null,
   POWER                VARCHAR2(50),
   VOLTAGE              VARCHAR2(50),
   "CURRENT"            VARCHAR2(50),
   "MODE"               NUMBER(1)                      default 0 not null
       check ("MODE" in (0,1)) disable,
   MEMO                 VARCHAR2(500),
   PLUG_NUM             NUMBER(5)                      default 1 not null,
   PRICES               VARCHAR2(254),
   OPEN_TYPE            VARCHAR2(50)                   default '1' not null,
   AVAILABLE_STATE      NUMBER(1)                      default 0 not null
       check (AVAILABLE_STATE in (0,1,2,3,4)) disable,
   PLOT_NUM             VARCHAR2(50),
   PRODUCT_NUM          VARCHAR2(254),
   PARKING_NUM          VARCHAR2(50),
   FLOOR                NUMBER(2)                      default 1 not null,
   LOCATION_TYPE        NUMBER(1)                      default 0 not null
       check (LOCATION_TYPE in (0,1,2)) disable,
   MANUFACTURER         VARCHAR2(254),
   FACTORY_NUM          VARCHAR2(254),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000)
);

/*==============================================================*/
/* Table: IX_POI_CHARGINGPLOT_PH                                */
/*==============================================================*/
create table IX_POI_CHARGINGPLOT_PH  (
   POI_PID              NUMBER(10)                     default 0 not null,
   PHOTO_NAME           VARCHAR2(254),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000)
);

/*==============================================================*/
/* Table: IX_POI_PARENT                                         */
/*==============================================================*/
create table IX_POI_PARENT 
(
   GROUP_ID             NUMBER(10)           not null,
   PARENT_POI_PID       NUMBER(10)           not null,
   TENANT_FLAG          NUMBER(2)            default 0
check (TENANT_FLAG is null or (TENANT_FLAG in (0,1))) disable ,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_PARENT primary key (GROUP_ID),
   constraint IXPOI_PARENT foreign key (PARENT_POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_CHILDREN                                       */
/*==============================================================*/
create table IX_POI_CHILDREN 
(
   GROUP_ID             NUMBER(10)           not null,
   CHILD_POI_PID        NUMBER(10)           not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
check (RELATION_TYPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_CHILD foreign key (CHILD_POI_PID)
         references IX_POI (PID) disable ,
   constraint IXPOI_PARENT_CHILD foreign key (GROUP_ID)
         references IX_POI_PARENT (GROUP_ID) disable
);


/*==============================================================*/
/* Table: IX_POI_CONTACT                                        */
/*==============================================================*/
create table IX_POI_CONTACT  (
   POI_PID              NUMBER(10)                      not null,
   CONTACT_TYPE         NUMBER(2)                      default 1 not null
       check (CONTACT_TYPE in (1,2,3,4,11,21,22)) disable,
   CONTACT              VARCHAR2(128),
   CONTACT_DEPART       NUMBER(3)                      default 0 not null,
   PRIORITY             NUMBER(5)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_TELEPHONE foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_ENTRYIMAGE                                     */
/*==============================================================*/
create table IX_POI_ENTRYIMAGE 
(
   POI_PID              NUMBER(10)           not null,
   IMAGE_CODE           VARCHAR2(8),
   X_PIXEL_R4           NUMBER(5)                      default 0 not null,
   Y_PIXEL_R4           NUMBER(5)                      default 0 not null,
   X_PIXEL_R5           NUMBER(5)                      default 0 not null,
   Y_PIXEL_R5           NUMBER(5)                      default 0 not null,
   X_PIXEL_35           NUMBER(5)                      default 0 not null,
   Y_PIXEL_35           NUMBER(5)                      default 0 not null,
   MEMO                 VARCHAR2(100),
   MAIN_POI_PID         NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_ENTRYIMAGE foreign key (POI_PID)
         references IX_POI (PID) disable,
   constraint IXPOI_MAINENTRYIMAGE foreign key (MAIN_POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_FLAG                                           */
/*==============================================================*/
create table IX_POI_FLAG 
(
   POI_PID              NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_FLAG foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_GASSTATION                                     */
/*==============================================================*/
create table IX_POI_GASSTATION  (
   GASSTATION_ID        NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   SERVICE_PROV         VARCHAR2(2),
   FUEL_TYPE            VARCHAR2(50),
   OIL_TYPE             VARCHAR2(50),
   EG_TYPE              VARCHAR2(50),
   MG_TYPE              VARCHAR2(50),
   PAYMENT              VARCHAR2(50),
   SERVICE              VARCHAR2(20),
   MEMO                 VARCHAR2(500),
   OPEN_HOUR            VARCHAR2(254),
   PHOTO_NAME           VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_GASSTATION primary key (GASSTATION_ID)
);


/*==============================================================*/
/* Table: IX_POI_HOTEL                                          */
/*==============================================================*/
create table IX_POI_HOTEL  (
   HOTEL_ID             NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CREDIT_CARD          VARCHAR2(10),
   RATING               NUMBER(2)                      default 0 not null
       check (RATING in (0,1,3,4,5,6,7,8,13,14,15,16,17)) disable,
   CHECKIN_TIME         VARCHAR2(20)                   default '14:00' not null,
   CHECKOUT_TIME        VARCHAR2(20)                   default '12:00' not null,
   ROOM_COUNT           NUMBER(5)                      default 0 not null,
   ROOM_TYPE            VARCHAR2(20),
   ROOM_PRICE           VARCHAR2(100),
   BREAKFAST            NUMBER(2)                      default 0 not null
       check (BREAKFAST in (0,1)) disable,
   SERVICE              VARCHAR2(254),
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)) disable,
   LONG_DESCRIPTION     VARCHAR2(254),
   LONG_DESCRIP_ENG     VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   OPEN_HOUR_ENG        VARCHAR2(254),
   TELEPHONE            VARCHAR2(100),
   ADDRESS              VARCHAR2(200),
   CITY                 VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   TRAVELGUIDE_FLAG     NUMBER(1)                      default 0 not null
       check (TRAVELGUIDE_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_HOTEL primary key (HOTEL_ID)
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_ICON primary key (REL_ID),
   constraint IXPOI_ICON foreign key (POI_PID)
         references IX_POI (PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_INTRODUCTION primary key (INTRODUCTION_ID)
);

/*==============================================================*/
/* Table: IX_POI_NAME                                           */
/*==============================================================*/
create table IX_POI_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   NAME_CLASS           NUMBER(1)                      default 1 not null
check (NAME_CLASS in (1,3,4,5,6,7,8,9)) disable,
   NAME_TYPE            NUMBER(2)                      default 1 not null
check (NAME_TYPE in (1,2)) disable,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_NAME primary key (NAME_ID),
   constraint IXPOI_NAME foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_NAME_FLAG                                      */
/*==============================================================*/
create table IX_POI_NAME_FLAG 
(
   NAME_ID              NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_NAMEFLAG foreign key (NAME_ID)
         references IX_POI_NAME (NAME_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOINAME_TONE foreign key (NAME_ID)
         references IX_POI_NAME (NAME_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_PHOTO foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POI_RESTAURANT                                     */
/*==============================================================*/
create table IX_POI_RESTAURANT  (
   RESTAURANT_ID        NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   FOOD_TYPE            VARCHAR2(32),
   CREDIT_CARD          VARCHAR2(20),
   AVG_COST             NUMBER(5)                      default 0 not null,
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)) disable,
   LONG_DESCRIPTION     VARCHAR2(254),
   LONG_DESCRIP_ENG     VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   OPEN_HOUR_ENG        VARCHAR2(254),
   TELEPHONE            VARCHAR2(100),
   ADDRESS              VARCHAR2(200),
   CITY                 VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   TRAVELGUIDE_FLAG     NUMBER(1)                      default 0 not null
       check (TRAVELGUIDE_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_RESTAURANT primary key (RESTAURANT_ID)
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_VIDEO foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_POSTCODE                                           */
/*==============================================================*/
create table IX_POSTCODE 
(
   POST_ID              NUMBER(10)           not null,
   POST_CODE            VARCHAR2(6),
   GEOMETRY             SDO_GEOMETRY,
   LINK_PID              NUMBER(10)           default 0 not null,
   SIDE          NUMBER(1)            default 0 not null,
   NAME_GROUPID      NUMBER(10)           default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POSTCODE primary key (POST_ID)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_ROADNAME primary key (PID)
);

/*==============================================================*/
/* Table: IX_SAMEPOI                                            */
/*==============================================================*/
create table IX_SAMEPOI 
(
   GROUP_ID             NUMBER(10)           not null,
   RELATION_TYPE        NUMBER(1)            default 1 not null
check (RELATION_TYPE in (1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_SAMEPOI primary key (GROUP_ID)
);

/*==============================================================*/
/* Table: IX_SAMEPOI_PART                                       */
/*==============================================================*/
create table IX_SAMEPOI_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint IXPOI_GROUP foreign key (GROUP_ID)
         references IX_SAMEPOI (GROUP_ID) disable ,
   constraint IXPOI_SAMEPOIPART foreign key (POI_PID)
         references IX_POI (PID) disable
);

/*==============================================================*/
/* Table: IX_TOLLGATE                                           */
/*==============================================================*/
create table IX_TOLLGATE 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
check (ROAD_FLAG in (0,1,2,3)) disable ,
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   NAME_POR             VARCHAR2(200),
   KIND_CODE            VARCHAR2(8)         
check (KIND_CODE is null or (KIND_CODE in ('230209'))) disable ,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   LINK_PID             NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_TOLLGATE primary key (PID)
);

/*==============================================================*/
/* Table: LC_FACE                                               */
/*==============================================================*/
create table LC_FACE  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable,
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)) disable,
   DISPLAY_CLASS        NUMBER(1)                      default 0 not null
       check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)) disable,
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)) disable,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE primary key (FACE_PID)
);


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
check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable ,
   FORM                 NUMBER(2)            default 0 not null
check (FORM in (0,1,2,3,4,8,9,10)) disable ,
   DISPLAY_CLASS        NUMBER(1)            default 0 not null
check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   SCALE                NUMBER(1)            default 0 not null
check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_100W primary key (FACE_PID)
);

/*==============================================================*/
/* Table: LC_FACE_20W                                           */
/*==============================================================*/
create table LC_FACE_20W  
(
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable,
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)) disable,
   DISPLAY_CLASS        NUMBER(1)                      default 0 not null
       check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)) disable,
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)) disable,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_20W primary key (FACE_PID)
);


/*==============================================================*/
/* Table: LC_FACE_NAME                                          */
/*==============================================================*/
create table LC_FACE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME primary key (NAME_ID),
   constraint LCFACE_NAME foreign key (FACE_PID)
         references LC_FACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_FACE_NAME_100W                                     */
/*==============================================================*/
create table LC_FACE_NAME_100W 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME_100W primary key (NAME_ID),
   constraint LCFACE_NAME_100W foreign key (FACE_PID)
         references LC_FACE_100W (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_FACE_NAME_20W                                      */
/*==============================================================*/
create table LC_FACE_NAME_20W 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME_20W primary key (NAME_ID),
   constraint LCFACE_NAME_20W foreign key (FACE_PID)
         references LC_FACE_20W (FACE_PID) disable
);

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
check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)) disable ,
   FORM                 NUMBER(2)            default 0 not null
check (FORM in (0,1,2,3,4,8,9,10)) disable ,
   DISPLAY_CLASS        NUMBER(1)            default 0 not null
check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)) disable ,
   AREA                 NUMBER(30,6)         default 0,
   PERIMETER            NUMBER(15,3)         default 0,
   SCALE                NUMBER(1)            default 0 not null
check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_TOP primary key (FACE_PID)
);

/*==============================================================*/
/* Table: LC_FACE_NAME_TOP                                      */
/*==============================================================*/
create table LC_FACE_NAME_TOP 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FACE_NAME_TOP primary key (NAME_ID),
   constraint LCFACE_NAME_TOP foreign key (FACE_PID)
         references LC_FACE_TOP (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE                                               */
/*==============================================================*/
create table LC_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE primary key (NODE_PID)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK primary key (LINK_PID),
   constraint LCLINK_SNODE foreign key (S_NODE_PID)
         references LC_NODE (NODE_PID) disable ,
   constraint LCLINK_ENODE foreign key (E_NODE_PID)
         references LC_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_FACE_TOPO                                          */
/*==============================================================*/
create table LC_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK foreign key (LINK_PID)
         references LC_LINK (LINK_PID) disable ,
   constraint LCFACE_LINKS foreign key (FACE_PID)
         references LC_FACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_100W                                          */
/*==============================================================*/
create table LC_NODE_100W 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE_100W primary key (NODE_PID)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK_100W primary key (LINK_PID),
   constraint LCLINK_SNODE_100W foreign key (S_NODE_PID)
         references LC_NODE_100W (NODE_PID) disable ,
   constraint LCLINK_ENODE_100W foreign key (E_NODE_PID)
         references LC_NODE_100W (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_FACE_TOPO_100W                                     */
/*==============================================================*/
create table LC_FACE_TOPO_100W 
(
   FACE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID) disable ,
   constraint LCFACE_LINKS_100W foreign key (FACE_PID)
         references LC_FACE_100W (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_20W                                           */
/*==============================================================*/
create table LC_NODE_20W 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE_20W primary key (NODE_PID)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK_20W primary key (LINK_PID),
   constraint LCLINK_SNODE_20W foreign key (S_NODE_PID)
         references LC_NODE_20W (NODE_PID) disable ,
   constraint LCLINK_ENODE_20W foreign key (E_NODE_PID)
         references LC_NODE_20W (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_FACE_TOPO_20W                                      */
/*==============================================================*/
create table LC_FACE_TOPO_20W 
(
   LINK_PID             NUMBER(10)           not null,
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID) disable ,
   constraint LCFACE_LINKS_20W foreign key (FACE_PID)
         references LC_FACE_20W (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_TOP                                           */
/*==============================================================*/
create table LC_NODE_TOP 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_NODE_TOP primary key (NODE_PID)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_LINK_TOP primary key (LINK_PID),
   constraint LCLINK_SNODE_TOP foreign key (S_NODE_PID)
         references LC_NODE_TOP (NODE_PID) disable ,
   constraint LCLINK_ENODE_TOP foreign key (E_NODE_PID)
         references LC_NODE_TOP (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_FACE_TOPO_TOP                                      */
/*==============================================================*/
create table LC_FACE_TOPO_TOP 
(
   FACE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCFACE_LINK_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID) disable ,
   constraint LCFACE_LINKS_TOP foreign key (FACE_PID)
         references LC_FACE_TOP (FACE_PID) disable
);

/*==============================================================*/
/* Table: LC_FEATURE                                            */
/*==============================================================*/
create table LC_FEATURE 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: LC_FEATURE_100W                                       */
/*==============================================================*/
create table LC_FEATURE_100W 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE_100W primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: LC_FEATURE_20W                                        */
/*==============================================================*/
create table LC_FEATURE_20W 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE_20W primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: LC_FEATURE_TOP                                        */
/*==============================================================*/
create table LC_FEATURE_TOP 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LC_FEATURE_TOP primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: LC_LINK_KIND                                          */
/*==============================================================*/
create table LC_LINK_KIND  
(
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable,
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND foreign key (LINK_PID)
         references LC_LINK (LINK_PID) disable
);


/*==============================================================*/
/* Table: LC_LINK_KIND_100W                                     */
/*==============================================================*/
create table LC_LINK_KIND_100W 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
check (FORM in (0,1,2,3,4,8,9,10)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_LINK_KIND_20W                                      */
/*==============================================================*/
create table LC_LINK_KIND_20W 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
check (FORM in (0,1,2,3,4,8,9,10)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_LINK_KIND_TOP                                      */
/*==============================================================*/
create table LC_LINK_KIND_TOP 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(2)            default 0 not null
check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)) disable ,
   FORM                 NUMBER(2)            default 0 not null
check (FORM in (0,1,2,3,4,8,9,10)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_KIND_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_LINK_MESH                                          */
/*==============================================================*/
create table LC_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH foreign key (LINK_PID)
         references LC_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_LINK_MESH_100W                                     */
/*==============================================================*/
create table LC_LINK_MESH_100W 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_LINK_MESH_20W                                      */
/*==============================================================*/
create table LC_LINK_MESH_20W 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_LINK_MESH_TOP                                      */
/*==============================================================*/
create table LC_LINK_MESH_TOP 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCLINK_MESH_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_MESH                                          */
/*==============================================================*/
create table LC_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH foreign key (NODE_PID)
         references LC_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_MESH_100W                                     */
/*==============================================================*/
create table LC_NODE_MESH_100W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH_100W foreign key (NODE_PID)
         references LC_NODE_100W (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_MESH_20W                                      */
/*==============================================================*/
create table LC_NODE_MESH_20W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH_20W foreign key (NODE_PID)
         references LC_NODE_20W (NODE_PID) disable
);

/*==============================================================*/
/* Table: LC_NODE_MESH_TOP                                      */
/*==============================================================*/
create table LC_NODE_MESH_TOP 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LCNODE_MESH_TOP foreign key (NODE_PID)
         references LC_NODE_TOP (NODE_PID) disable
);

/*==============================================================*/
/* Table: LU_FACE                                               */
/*==============================================================*/
create table LU_FACE  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,11,12,21,22,23,24,30,31,32,33,34,35,36,37,38,39,40,41)) disable,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_FACE primary key (FACE_PID)
);

/*==============================================================*/
/* Table: LU_FACE_NAME                                          */
/*==============================================================*/
create table LU_FACE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   FACE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_FACE_NAME primary key (NAME_ID),
   constraint LUFACE_NAME foreign key (FACE_PID)
         references LU_FACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: LU_NODE                                               */
/*==============================================================*/
create table LU_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_NODE primary key (NODE_PID)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_LINK primary key (LINK_PID),
   constraint LULINK_NODE foreign key (S_NODE_PID)
         references LU_NODE (NODE_PID) disable ,
   constraint LULINK_ENODE foreign key (E_NODE_PID)
         references LU_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: LU_FACE_TOPO                                          */
/*==============================================================*/
create table LU_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LUFACE_LINK foreign key (LINK_PID)
         references LU_LINK (LINK_PID) disable ,
   constraint LUFACE_LINKS foreign key (FACE_PID)
         references LU_FACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: LU_FEATURE                                            */
/*==============================================================*/
create table LU_FEATURE 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_LU_FEATURE primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: LU_LINK_KIND                                          */
/*==============================================================*/
create table LU_LINK_KIND  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,21,22,23,24,30,31,32,33,34,35,36,37,38,39,40,41)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint LULINK_KIND foreign key (LINK_PID)
         references LU_LINK (LINK_PID) disable
);


/*==============================================================*/
/* Table: LU_LINK_MESH                                          */
/*==============================================================*/
create table LU_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LULINK_MESH foreign key (LINK_PID)
         references LU_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: LU_NODE_MESH                                          */
/*==============================================================*/
create table LU_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint LUNODE_MESH foreign key (NODE_PID)
         references LU_NODE (NODE_PID) disable
);


/*==============================================================*/
/* Table: M_MESH_TYPE                                           */
/*==============================================================*/
create table M_MESH_TYPE 
(
   MESH_ID              NUMBER(6)            not null,
   TYPE                 NUMBER(2)            default 0 not null
check (TYPE in (0,1,2)) disable ,
   MEMO                 VARCHAR2(500),
   constraint PK_M_MESH_TYPE primary key (MESH_ID)
);

/*==============================================================*/
/* Table: M_PARAMETER                                           */
/*==============================================================*/
create table M_PARAMETER 
(
   NAME                 VARCHAR2(32),
   PARAMETER            VARCHAR2(32),
   DESCRIPTION          VARCHAR2(200)
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000)
);

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
check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_COMPANYID       VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_COMPANY primary key (COMPANY_ID)
);

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
check (SIDE in (0,1,2,3)) disable ,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   ROAD_FLAG            NUMBER(1)            default 0 not null
check (ROAD_FLAG in ('0','1','2','3')) disable ,
   PMESH_ID             NUMBER(6)            default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   ACCESS_CODE          VARCHAR2(32),
   ACCESS_TYPE          VARCHAR2(10)         default '0' not null
check (ACCESS_TYPE in ('0','1','2','3')) disable ,
   ACCESS_METH          NUMBER(3)            default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)            default 0 not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   POI_MEMO             VARCHAR2(200),
   OPERATOR             VARCHAR2(30),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   POI_NUM              VARCHAR2(100),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_POI primary key (PID)
);

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
check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
check (AUTO_TICKET in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_ACCESS foreign key (POI_PID)
         references PT_POI (PID) disable
);

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
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_COMPANY foreign key (COMPANY_ID)
         references PT_COMPANY (COMPANY_ID) disable
);

/*==============================================================*/
/* Table: PT_LINE                                               */
/*==============================================================*/
create table PT_LINE 
(
   PID                  NUMBER(10)           not null,
   SYSTEM_ID            NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TYPE                 NUMBER(2)            default 11 not null
check (TYPE in (11,12,13,14,15,16,17,21,31,32,33,41,42,51,52,53,54,61)) disable ,
   COLOR                VARCHAR2(10),
   NIDB_LINEID          VARCHAR2(32),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_LINE primary key (PID)
);

/*==============================================================*/
/* Table: PT_ETA_LINE                                           */
/*==============================================================*/
create table PT_ETA_LINE 
(
   PID                  NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   BIKE                 VARCHAR2(1)          default '0' not null
check (BIKE in ('0','1','2')) disable ,
   BIKE_PERIOD          VARCHAR2(200),
   IMAGE                VARCHAR2(20),
   RACK                 VARCHAR2(1)          default '0' not null
check (RACK in ('0','1','2')) disable ,
   DINNER               VARCHAR2(1)          default '0' not null
check (DINNER in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
check (TOILET in ('0','1','2')) disable ,
   SLEEPER              VARCHAR2(1)          default '0' not null
check (SLEEPER in ('0','1','2')) disable ,
   WHEEL_CHAIR          VARCHAR2(1)          default '0' not null
check (WHEEL_CHAIR in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
check (SMOKE in ('0','1','2')) disable ,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_LINE foreign key (PID)
         references PT_LINE (PID) disable
);

/*==============================================================*/
/* Table: PT_ETA_STOP                                           */
/*==============================================================*/
create table PT_ETA_STOP 
(
   POI_PID              NUMBER(10)           not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   PRIVATE_PARK         VARCHAR2(1)          default '0' not null
check (PRIVATE_PARK in ('0','1','2','3')) disable ,
   PRIVATE_PARK_PERIOD  VARCHAR2(200),
   CARPORT_EXACT        VARCHAR2(32),
   CARPORT_ESTIMATE     VARCHAR2(1)          default '0' not null
check (CARPORT_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   BIKE_PARK            VARCHAR2(1)          default '0' not null
check (BIKE_PARK in ('0','1','2','3')) disable ,
   BIKE_PARK_PERIOD     VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)          default '0' not null
check (MANUAL_TICKET in ('0','1','2')) disable ,
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   MOBILE               VARCHAR2(1)          default '0' not null
check (MOBILE in ('0','1','2')) disable ,
   BAGGAGE_SECURITY     VARCHAR2(1)          default '0' not null
check (BAGGAGE_SECURITY in ('0','1','2')) disable ,
   LEFT_BAGGAGE         VARCHAR2(1)          default '0' not null
check (LEFT_BAGGAGE in ('0','1','2')) disable ,
   CONSIGNATION_EXACT   VARCHAR2(32),
   CONSIGNATION_ESTIMATE VARCHAR2(1)          default '0' not null
check (CONSIGNATION_ESTIMATE in ('0','1','2','3','4','5')) disable ,
   CONVENIENT           VARCHAR2(1)          default '0' not null
check (CONVENIENT in ('0','1','2')) disable ,
   SMOKE                VARCHAR2(1)          default '0' not null
check (SMOKE in ('0','1','2')) disable ,
   BUILD_TYPE           VARCHAR2(1)          default '0' not null
check (BUILD_TYPE in ('0','1','2','3')) disable ,
   AUTO_TICKET          VARCHAR2(1)          default '0' not null
check (AUTO_TICKET in ('0','1','2')) disable ,
   TOILET               VARCHAR2(1)          default '0' not null
check (TOILET in ('0','1','2')) disable ,
   WIFI                 VARCHAR2(1)          default '0' not null
check (WIFI in ('0','1','2')) disable ,
   OPEN_PERIOD          VARCHAR2(200),
   FARE_AREA            VARCHAR2(1),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_POI foreign key (POI_PID)
         references PT_POI (PID) disable
);

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
check (SRC_FLAG in (0,1)) disable ,
   CITY_CODE            NUMBER(6)            default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_SYSTEMID        VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_SYSTEM primary key (SYSTEM_ID),
   constraint PTSYSTEM_COMPANY foreign key (COMPANY_ID)
         references PT_COMPANY (COMPANY_ID) disable
);

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
check (BILL_METHOD in ('0','1','2','3')) disable ,
   COIN                 VARCHAR2(200)        default 'CNY' not null,
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTETA_SYSTEM foreign key (SYSTEM_ID)
         references PT_SYSTEM (SYSTEM_ID) disable
);

/*==============================================================*/
/* Table: PT_LINE_NAME                                          */
/*==============================================================*/
create table PT_LINE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_LINE_NAME primary key (NAME_ID),
   constraint PTLINE_NAME foreign key (PID)
         references PT_LINE (PID) disable
);

/*==============================================================*/
/* Table: PT_PLATFORM                                           */
/*==============================================================*/
create table PT_PLATFORM 
(
   PID                  NUMBER(10)           not null,
   POI_PID              NUMBER(10)           not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   COLLECT              NUMBER(2)            default 0 not null
check (COLLECT in (0,1)) disable ,
   P_LEVEL              NUMBER(2)            default 0 not null
check (P_LEVEL in (4,3,2,1,0,-1,-2,-3,-4,-5,-6)) disable ,
   TRANSIT_FLAG         NUMBER(1)            default 0 not null
check (TRANSIT_FLAG in (0,1)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_PLATFORMID      VARCHAR2(32),
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_PLATFORM primary key (PID),
   constraint PTPLATFORM_POI foreign key (POI_PID)
         references PT_POI (PID) disable
);

/*==============================================================*/
/* Table: PT_PLATFORM_ACCESS                                    */
/*==============================================================*/
create table PT_PLATFORM_ACCESS 
(
   RELATE_ID            NUMBER(10)           not null,
   PLATFORM_ID          NUMBER(10)           not null,
   ACCESS_ID            NUMBER(10)           not null,
   AVAILABLE            NUMBER(1)            default 1 not null
check (AVAILABLE in (0,1)) disable ,
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_PLATFORM_ACCESS primary key (RELATE_ID),
   constraint PTPLATFORM_ACCESSES foreign key (PLATFORM_ID)
         references PT_PLATFORM (PID) disable ,
   constraint PTPLATFORM_ACCESS foreign key (ACCESS_ID)
         references PT_POI (PID) disable
);

/*==============================================================*/
/* Table: PT_PLATFORM_NAME                                      */
/*==============================================================*/
create table PT_PLATFORM_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_PLATFORM_NAME primary key (NAME_ID),
   constraint PTPLATFORM_NAME foreign key (PID)
         references PT_PLATFORM (PID) disable
);

/*==============================================================*/
/* Table: PT_POI_PARENT                                         */
/*==============================================================*/
create table PT_POI_PARENT 
(
   GROUP_ID             NUMBER(10)           not null,
   PARENT_POI_PID       NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_POI_PARENT primary key (GROUP_ID),
   constraint PTPOI_PARENT foreign key (PARENT_POI_PID)
         references PT_POI (PID) disable
);

/*==============================================================*/
/* Table: PT_POI_CHILDREN                                       */
/*==============================================================*/
create table PT_POI_CHILDREN 
(
   GROUP_ID             NUMBER(10)           not null,
   CHILD_POI_PID        NUMBER(10)           not null,
   RELATION_TYPE        NUMBER(1)            default 0 not null
check (RELATION_TYPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTPOI_CHILDREN foreign key (CHILD_POI_PID)
         references PT_POI (PID) disable ,
   constraint PTPOI_CHILDPARENT foreign key (GROUP_ID)
         references PT_POI_PARENT (GROUP_ID) disable
);

/*==============================================================*/
/* Table: PT_POI_FLAG                                           */
/*==============================================================*/
create table PT_POI_FLAG 
(
   POI_PID              NUMBER(10)           not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTPOI_FLAG foreign key (POI_PID)
         references PT_POI (PID) disable
);

/*==============================================================*/
/* Table: PT_POI_NAME                                           */
/*==============================================================*/
create table PT_POI_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   POI_PID              NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_POI_NAME primary key (NAME_ID),
   constraint PTPOI_NAME foreign key (POI_PID)
         references PT_POI (PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTPOINAME_TONE foreign key (NAME_ID)
         references PT_POI_NAME (NAME_ID) disable
);

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
check (UP_DOWN is null or (UP_DOWN in ('Ｓ','Ｘ','Ｈ','ＮＨ','ＷＨ','ＣＨ','ＣＮＨ','ＣＷＨ'))) disable ,
   DISTANCE             VARCHAR2(10),
   TICKET_SYS           NUMBER(2)            default 0 not null
check (TICKET_SYS in (0,1,2,9)) disable ,
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
check (STATE in (0,1,2,3)) disable ,
   TASK_ID              NUMBER(10)           default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_STRAND primary key (PID),
   constraint PTSTRAND_LINE foreign key (LINE_ID)
         references PT_LINE (PID) disable
);

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
check (APPROX_TIME in (0,1)) disable ,
   VALID_WEEK           VARCHAR2(7),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTRUNTIME_STRAND foreign key (STRAND_PID)
         references PT_STRAND (PID) disable ,
   constraint PTRUNTIME_PLATFORM foreign key (PLATFORM_PID)
         references PT_PLATFORM (PID) disable
);

/*==============================================================*/
/* Table: PT_STRAND_NAME                                        */
/*==============================================================*/
create table PT_STRAND_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2,3,4)) disable ,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_STRAND_NAME primary key (NAME_ID),
   constraint PTSTRAND_NAME foreign key (PID)
         references PT_STRAND (PID) disable
);

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
check (STATE in (0,1,2,3)) disable ,
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTSTRAND_PLATFORM foreign key (PLATFORM_PID)
         references PT_PLATFORM (PID) disable ,
   constraint PTSTRAND_FLATFORM foreign key (STRAND_PID)
         references PT_STRAND (PID) disable
);

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
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PTSTRAND_SCHEDULE foreign key (STRAND_PID)
         references PT_STRAND (PID) disable
);

/*==============================================================*/
/* Table: PT_TRANSFER                                           */
/*==============================================================*/
create table PT_TRANSFER 
(
   TRANSFER_ID          NUMBER(10)           not null,
   TRANSFER_TYPE        NUMBER(1)            default 1 not null
check (TRANSFER_TYPE in (0,1)) disable ,
   POI_FIR              NUMBER(10)           default 0 not null,
   POI_SEC              NUMBER(10)           default 0 not null,
   PLATFORM_FIR         NUMBER(10)           default 0 not null,
   PLATFORM_SEC         NUMBER(10)           default 0 not null,
   CITY_CODE            NUMBER(6)            default 0 not null,
   TRANSFER_TIME        NUMBER(2)            default 0 not null,
   EXTERNAL_FLAG        NUMBER(1)            default 0 not null
check (EXTERNAL_FLAG in (0,1,2)) disable ,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(200),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)            default 0 not null
check (STATE in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_PT_TRANSFER primary key (TRANSFER_ID)
);

/*==============================================================*/
/* Table: QC_QUESTION                                           */
/*==============================================================*/
create table QC_QUESTION 
(
   QU_ID                NUMBER(10)           not null,
   FEATURE_ID           NUMBER(10),
   TABLE_NAME           VARCHAR2(100),
   GEOMETRY             SDO_GEOMETRY,
   INFORMATION          VARCHAR2(4000),
   EDIT_ITEM            VARCHAR2(1000),
   QU_TYPE              VARCHAR2(100),
   QU_RANK              VARCHAR2(100),
   QU_INFO              VARCHAR2(4000),
   QU_STATUS            VARCHAR2(100),
   ER_TYPE              VARCHAR2(100),
   ER_CONTENT           VARCHAR2(4000),
   ER_DETAILBMP         BLOB,
   ER_GENBMP            BLOB,
   OK_CONTENT           VARCHAR2(4000),
   OK_SNAPSHOT          BLOB,
   ASK_NUM              VARCHAR2(100),
   MESH_ID              NUMBER(6)            default 0,
   TASK_NAME            VARCHAR2(50),
   ADDITION_INFO        VARCHAR2(4000),
   MODIFY_INFO          VARCHAR2(4000),
   SOLVE                VARCHAR2(4000),
   SOLVE_PERSON         VARCHAR2(100),
   SOLVE_TIME           DATE,
   MOD_PERSON           VARCHAR2(100),
   VER_PERSON           VARCHAR2(100),
   constraint PK_QC_QUESTION primary key (QU_ID)
);

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
check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH primary key (BRANCH_PID),
   constraint RDBRANCH_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDBRANCH_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDBRANCH_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_BRANCH_DETAIL                                      */
/*==============================================================*/
create table RD_BRANCH_DETAIL 
(
   DETAIL_ID            NUMBER(10)           not null,
   BRANCH_PID           NUMBER(10)           not null,
   BRANCH_TYPE          NUMBER(1)            default 0 not null
check (BRANCH_TYPE in (0,1,2,3,4)) disable ,
   VOICE_DIR            NUMBER(1)            default 0 not null
check (VOICE_DIR in (0,2,5,9)) disable ,
   ESTAB_TYPE           NUMBER(1)            default 0 not null
check (ESTAB_TYPE in (0,1,2,3,4,5,9)) disable ,
   NAME_KIND            NUMBER(1)            default 0 not null
check (NAME_KIND in (0,1,2,3,4,5,6,7,8,9)) disable ,
   EXIT_NUM             VARCHAR2(32),
   ARROW_CODE           VARCHAR2(10),
   PATTERN_CODE         VARCHAR2(10),
   ARROW_FLAG           NUMBER(2)            default 0 not null
check (ARROW_FLAG in (0,1)) disable ,
   GUIDE_CODE           NUMBER(1)            default 0 not null
check (GUIDE_CODE in (0,1,2,3,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH_DETAIL primary key (DETAIL_ID),
   constraint RDBRANCH_DETAIL foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

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
check (NAME_CLASS in (0,1)) disable ,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   CODE_TYPE            NUMBER(2)            default 0 not null
check (CODE_TYPE in (0,1,2,3,4,5,6,7,8,9,10)) disable ,
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1,2,3,4,5)) disable ,
   VOICE_FILE           VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH_NAME primary key (NAME_ID),
   constraint RDBRANCH_NAME foreign key (DETAIL_ID)
         references RD_BRANCH_DETAIL (DETAIL_ID) disable
);


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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDBRANCHNAME_TONE foreign key (NAME_ID)
         references RD_BRANCH_NAME (NAME_ID) disable
);

/*==============================================================*/
/* Table: RD_BRANCH_REALIMAGE                                   */
/*==============================================================*/
create table RD_BRANCH_REALIMAGE 
(
   BRANCH_PID           NUMBER(10)           not null,
   IMAGE_TYPE           NUMBER(1)            default 0 not null
check (IMAGE_TYPE in (0,1)) disable ,
   REAL_CODE            VARCHAR2(10),
   ARROW_CODE           VARCHAR2(10),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDBRANCH_REALIMAGE foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

/*==============================================================*/
/* Table: RD_BRANCH_SCHEMATIC                                   */
/*==============================================================*/
create table RD_BRANCH_SCHEMATIC 
(
   SCHEMATIC_ID         NUMBER(10)           not null,
   BRANCH_PID           NUMBER(10)           not null,
   SCHEMATIC_CODE       VARCHAR2(16),
   ARROW_CODE           VARCHAR2(16),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_BRANCH_SCHEMATIC primary key (SCHEMATIC_ID),
   constraint RDBRANCH_SCHEMATIC foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDBRANCH_VIALINKS foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable ,
   constraint RDBRANCH_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_CHAIN                                              */
/*==============================================================*/
create table RD_CHAIN 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1,2,3,4,5,6,7,8)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CHAIN primary key (PID)
);

/*==============================================================*/
/* Table: RD_CHAIN_LINK                                         */
/*==============================================================*/
create table RD_CHAIN_LINK 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCHAIN_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDCHAIN_LINKS foreign key (PID)
         references RD_CHAIN (PID) disable
);

/*==============================================================*/
/* Table: RD_CHAIN_NAME                                         */
/*==============================================================*/
create table RD_CHAIN_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CHAIN_NAME primary key (NAME_ID),
   constraint RDCHAIN_NAME foreign key (PID)
         references RD_CHAIN (PID) disable
);

/*==============================================================*/
/* Table: RD_CROSS                                              */
/*==============================================================*/
create table RD_CROSS 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1)) disable ,
   SIGNAL               NUMBER(1)            default 0 not null
check (SIGNAL in (0,1,2)) disable ,
   ELECTROEYE           NUMBER(1)            default 0 not null
check (ELECTROEYE in (0,1,2)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CROSS primary key (PID)
);

/*==============================================================*/
/* Table: RD_CROSSWALK                                          */
/*==============================================================*/
create table RD_CROSSWALK 
(
   PID                  NUMBER(10)           not null,
   CURB_RAMP            NUMBER(1)            default 0 not null
check (CURB_RAMP in (0,1,2,3)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CROSSWALK primary key (PID)
);

/*==============================================================*/
/* Table: RD_CROSSWALK_INFO                                     */
/*==============================================================*/
create table RD_CROSSWALK_INFO 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1,2,3,4,5,6)) disable ,
   ATTR                 NUMBER(5)            default 0 not null,
   SIGNAGE              NUMBER(1)            default 0 not null
check (SIGNAGE in (0,1,2,3,4)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNODE_RDCROSSWALKINFO foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDCROSSWALK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDCROSSWALK foreign key (PID)
         references RD_CROSSWALK (PID) disable
);

/*==============================================================*/
/* Table: RD_CROSSWALK_NODE                                     */
/*==============================================================*/
create table RD_CROSSWALK_NODE 
(
   PID                  NUMBER(10)           not null,
   FIR_NODE_PID         NUMBER(10)           not null,
   SEN_NODE_PID         NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCROSSWALK_NODE foreign key (PID)
         references RD_CROSSWALK (PID) disable ,
   constraint RDCROSSWALK_FIRNODE foreign key (FIR_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDCROSSWALK_SENNODE foreign key (SEN_NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_CROSS_LINK                                         */
/*==============================================================*/
create table RD_CROSS_LINK 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCROSS_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDCROSS_LINKS foreign key (PID)
         references RD_CROSS (PID) disable
);

/*==============================================================*/
/* Table: RD_CROSS_NAME                                         */
/*==============================================================*/
create table RD_CROSS_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_CROSS_NAME primary key (NAME_ID),
   constraint RDCROSS_NAME foreign key (PID)
         references RD_CROSS (PID) disable
);

/*==============================================================*/
/* Table: RD_CROSS_NODE                                         */
/*==============================================================*/
create table RD_CROSS_NODE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   IS_MAIN              NUMBER(1)            default 0 not null
check (IS_MAIN in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDCROSS_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDCROSS_NODES foreign key (PID)
         references RD_CROSS (PID) disable
);

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
check (FLAG in (0,1,2)) disable ,
   PROCESS_FLAG         NUMBER(1)            default 1 not null
check (PROCESS_FLAG in (0,1,2)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_DIRECTROUTE primary key (PID),
   constraint RDDIRECT_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDDIRECT_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDDIRECTROUTE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDDIRECT_VIALINK foreign key (PID)
         references RD_DIRECTROUTE (PID) disable ,
   constraint RDDIRECTROUTE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_ELECTRONICEYE                                      */
/*==============================================================*/
create table RD_ELECTRONICEYE  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)) disable,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,10,11,12,13,14,15,16,17,18,19,20,21,98)) disable,
   ANGLE                NUMBER(8,5)                    default 0 not null,
   LOCATION             NUMBER(2)                      default 0 not null,
   SPEED_LIMIT          NUMBER(4)                      default 0 not null,
   VERIFIED_FLAG        NUMBER(2)                      default 0 not null
       check (VERIFIED_FLAG in (0,1,2)) disable,
   MESH_ID              NUMBER(6)                      default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   SRC_FLAG             VARCHAR2(2)                    default '1' not null
       check (SRC_FLAG in ('0','1','2','3')) disable,
   CREATION_DATE        DATE,
   HIGH_VIOLATION       NUMBER(1)                      default 0 not null
       check (HIGH_VIOLATION in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_ELECTRONICEYE primary key (PID)
);

/*==============================================================*/
/* Table: RD_ELECEYE_PAIR                                       */
/*==============================================================*/
create table RD_ELECEYE_PAIR  (
   GROUP_ID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_ELECEYE_PAIR primary key (GROUP_ID)
);

comment on column RD_ELECEYE_PAIR.U_RECORD is
'增量更新标识';

comment on column RD_ELECEYE_PAIR.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';


/*==============================================================*/
/* Table: RD_ELECEYE_PART                                       */
/*==============================================================*/
create table RD_ELECEYE_PART  (
   GROUP_ID             NUMBER(10)                      not null,
   ELECEYE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDELECPAIR foreign key (GROUP_ID)
         references RD_ELECEYE_PAIR (GROUP_ID) disable,
   constraint RDELECPARTS foreign key (ELECEYE_PID)
         references RD_ELECTRONICEYE (PID) disable
);

comment on column RD_ELECEYE_PART.U_RECORD is
'增量更新标识';

comment on column RD_ELECEYE_PART.U_FIELDS is
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
check (TYPE in (0,1,2)) disable ,
   DIR                  NUMBER(1)            default 2 not null
check (DIR in (0,1,2)) disable ,
   FEE                  NUMBER(1)            default 0 not null
check (FEE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_GATE primary key (PID),
   constraint RDGATE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDGATE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDGATE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_GATE_CONDITION                                     */
/*==============================================================*/
create table RD_GATE_CONDITION 
(
   PID                  NUMBER(10)           not null,
   VALID_OBJ            NUMBER(1)            default 0 not null
check (VALID_OBJ in (0,1)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDGATE_CONDITION foreign key (PID)
         references RD_GATE (PID) disable
);

/*==============================================================*/
/* Table: RD_GSC                                                */
/*==============================================================*/
create table RD_GSC 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   PROCESS_FLAG         NUMBER(1)            default 1 not null
check (PROCESS_FLAG in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_GSC primary key (PID)
);

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
check (START_END in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDGSC_LINK foreign key (PID)
         references RD_GSC (PID) disable
);

/*==============================================================*/
/* Table: RD_INTER                                              */
/*==============================================================*/
create table RD_INTER 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_INTER primary key (PID)
);

/*==============================================================*/
/* Table: RD_INTER_LINK                                         */
/*==============================================================*/
create table RD_INTER_LINK 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDINTERSECTION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDINTERSECTION_LINKS foreign key (PID)
         references RD_INTER (PID) disable
);

/*==============================================================*/
/* Table: RD_INTER_NODE                                         */
/*==============================================================*/
create table RD_INTER_NODE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDINTERSECTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDINTERSECTION_NODES foreign key (PID)
         references RD_INTER (PID) disable
);

/*==============================================================*/
/* Table: RD_LANE                                               */
/*==============================================================*/
create table RD_LANE  (
   LANE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   LANE_NUM             NUMBER(2)                      default 1 not null,
   TRAVEL_FLAG          NUMBER(1)                      default 0 not null
       check (TRAVEL_FLAG in (0,1)) disable,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   LANE_FORMING         NUMBER(2)                      default 0 not null
       check (LANE_FORMING in (0,1,2,3)) disable,
   LANE_DIR             NUMBER(1)                      default 2 not null
       check (LANE_DIR in (1,2,3)) disable,
   LANE_TYPE            NUMBER(10)                     default 1 not null,
   ARROW_DIR            VARCHAR2(1)                    default '9' not null
       check (ARROW_DIR in ('9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5')) disable,
   LANE_MARK            NUMBER(2)                      default 0 not null
       check (LANE_MARK in (0,1,2,3,4,5,6,7,98,99)) disable,
   WIDTH                NUMBER(5,2)                    default 0 not null,
   RESTRICT_HEIGHT      NUMBER(5,2)                    default 0 not null,
   TRANSITION_AREA      NUMBER(2)                      default 0 not null
       check (TRANSITION_AREA in (0,1,2)) disable,
   FROM_MAX_SPEED       NUMBER(4)                      default 0 not null,
   TO_MAX_SPEED         NUMBER(4)                      default 0 not null,
   FROM_MIN_SPEED       NUMBER(4)                      default 0 not null,
   TO_MIN_SPEED         NUMBER(4)                      default 0 not null,
   ELEC_EYE             NUMBER(1)                      default 0 not null
       check (ELEC_EYE in (0,1,2)) disable,
   LANE_DIVIDER         NUMBER(2)                      default 0 not null
       check (LANE_DIVIDER in (0,10,11,12,13,20,21,30,31,40,50,51,60,61,62,63,99)) disable,
   CENTER_DIVIDER       NUMBER(2)                      default 0 not null,
   SPEED_FLAG           NUMBER(1)                      default 0 not null
       check (SPEED_FLAG in (0,1,2)) disable,
   SRC_FLAG             NUMBER(1)                      default 1 not null
       check (SRC_FLAG in (1,2,3,4)) disable,
   OUTPUT_FLAG          NUMBER(1)                      default 0 not null
       check (OUTPUT_FLAG in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE primary key (LANE_PID),
   constraint RDLANE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LANE_CONDITION                                     */
/*==============================================================*/
create table RD_LANE_CONDITION 
(
   LANE_PID             NUMBER(10)           not null,
   DIRECTION            NUMBER(1)            default 1 not null
check (DIRECTION in (1,2,3)) disable ,
   DIRECTION_TIME       VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   VEHICLE_TIME         VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLANE_CONDITION foreign key (LANE_PID)
         references RD_LANE (LANE_PID) disable
);

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
check (CONFLICT_FLAG in (0,1)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
check (KG_FLAG in (0,1,2)) disable ,
   LANE_NUM             NUMBER(3)            default 0 not null,
   LEFT_EXTEND          NUMBER(3)            default 0 not null,
   RIGHT_EXTEND         NUMBER(3)            default 0 not null,
   SRC_FLAG             NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE_CONNEXITY primary key (PID),
   constraint RDLANE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDLANE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_LANE_TOPOLOGY                                      */
/*==============================================================*/
create table RD_LANE_TOPOLOGY 
(
   TOPOLOGY_ID          NUMBER(10)           not null,
   CONNEXITY_PID        NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   IN_LANE_INFO         NUMBER(10)           default 0 not null,
   BUS_LANE_INFO        NUMBER(10)           default 0 not null,
   REACH_DIR            NUMBER(1)            default 0 not null
check (REACH_DIR in (0,1,2,3,4,5,6)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_LANE_TOPOLOGY primary key (TOPOLOGY_ID),
   constraint RDLANE_TOPOLOGY foreign key (CONNEXITY_PID)
         references RD_LANE_CONNEXITY (PID) disable ,
   constraint RDLANEVTOPOLOGY_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LANE_TOPO_DETAIL                                   */
/*==============================================================*/
create table RD_LANE_TOPO_DETAIL 
(
   TOPO_ID              NUMBER(10)           not null,
   IN_LANE_PID          NUMBER(10)           not null,
   OUT_LANE_PID         NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   REACH_DIR            NUMBER(1)            default 0 not null
check (REACH_DIR in (0,1,2,3,4,5,6)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   TOLL_FORM            NUMBER(10)                     default 0 not null,
   CARD_TYPE            NUMBER(1)                      default 0 not null
       check (CARD_TYPE in (0,1,2,3)) disable,
   TOPOLOGY_ID          NUMBER(10)                     default 0 not null,
   SRC_FLAG             NUMBER(1)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4)) disable,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLANETOPODETAIL_VIA foreign key (TOPO_ID)
         references RD_LANE_TOPO_DETAIL (TOPO_ID) disable ,
   constraint RDLANE_TOPO_VIA_LINK foreign key (VIA_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLANE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDLANETOPOLOGY_VIALINK foreign key (TOPOLOGY_ID)
         references RD_LANE_TOPOLOGY (TOPOLOGY_ID) disable
);

/*==============================================================*/
/* Table: RD_LINK_ADDRESS                                       */
/*==============================================================*/
create table RD_LINK_ADDRESS 
(
   LINK_PID             NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   LEFT_START           VARCHAR2(20),
   LEFT_END             VARCHAR2(20),
   LEFT_TYPE            NUMBER(1)            default 0 not null
check (LEFT_TYPE in (0,1,2,3,4)) disable ,
   RIGHT_START          VARCHAR2(20),
   RIGHT_END            VARCHAR2(20),
   RIGHT_TYPE           NUMBER(1)            default 0 not null
check (RIGHT_TYPE in (0,1,2,3,4)) disable ,
   ADDRESS_TYPE         NUMBER(1)            default 0 not null
check (ADDRESS_TYPE in (0,1,2,3)) disable ,
   WORK_DIR             NUMBER(1)            default 0 not null
check (WORK_DIR in (0,1,2)) disable ,
   SRC_FLAG             NUMBER(1)            default 0 not null
check (SRC_FLAG in (0,1,2,3)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_ADDRESS foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_FORM                                          */
/*==============================================================*/
create table RD_LINK_FORM  (
   LINK_PID             NUMBER(10)                      not null,
   FORM_OF_WAY          NUMBER(2)                      default 1 not null
       check (FORM_OF_WAY in (0,1,2,10,11,12,13,14,15,16,17,18,20,21,22,23,24,30,31,32,33,34,35,36,37,38,39,43,48,49,50,51,52,53,54,57,60,80,81,82)) disable,
   EXTENDED_FORM        NUMBER(2)                      default 0 not null
       check (EXTENDED_FORM in (0,40,41,42)) disable,
   AUXI_FLAG            NUMBER(2)                      default 0 not null
       check (AUXI_FLAG in (0,55,56,58,70,71,72,73,76,77)) disable,
   KG_FLAG              NUMBER(1)                      default 0 not null
       check (KG_FLAG in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_FORM foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_INT_RTIC                                      */
/*==============================================================*/
create table RD_LINK_INT_RTIC  (
   LINK_PID             NUMBER(10)                      not null,
   CODE                 NUMBER(8)                      default 0 not null,
   RANK                 NUMBER(1)                      default 0 not null
       check (RANK in (0,1,2,3,4)),
   RTIC_DIR             NUMBER(1)                      default 0 not null
       check (RTIC_DIR in (0,1,2)),
   UPDOWN_FLAG          NUMBER(1)                      default 0 not null
       check (UPDOWN_FLAG in (0,1)),
   RANGE_TYPE           NUMBER(1)                      default 1 not null
       check (RANGE_TYPE in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_INTRTICS foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_LIMIT                                         */
/*==============================================================*/
create table RD_LINK_LIMIT  (
   LINK_PID             NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 3 not null
       check (TYPE in (0,1,2,3,4,5,6,7,8,9)) disable,
   LIMIT_DIR            NUMBER(1)                      default 0 not null
       check (LIMIT_DIR in (0,1,2,3,9)) disable,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   TOLL_TYPE            NUMBER(1)                      default 9 not null
       check (TOLL_TYPE in (0,1,2,3,4,5,6,9)) disable,
   WEATHER              NUMBER(1)                      default 9 not null
       check (WEATHER in (0,1,2,3,9)) disable,
   INPUT_TIME           VARCHAR2(32),
   PROCESS_FLAG         NUMBER(1)                      default 0 not null
       check (PROCESS_FLAG in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_LIMIT foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);


/*==============================================================*/
/* Table: RD_LINK_NAME                                          */
/*==============================================================*/
create table RD_LINK_NAME 
(
   LINK_PID             NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   NAME_CLASS           NUMBER(1)            default 1 not null
check (NAME_CLASS in (1,2,3)) disable ,
   INPUT_TIME           VARCHAR2(32),
   NAME_TYPE            NUMBER(2)            default 0 not null
check (NAME_TYPE in (0,1,2,3,4,5,6,7,8,9,14,15)) disable ,
   SRC_FLAG             NUMBER(1)            default 9 not null
check (SRC_FLAG in (0,1,2,3,4,5,6,9)) disable ,
   ROUTE_ATT            NUMBER(1)            default 0 not null
check (ROUTE_ATT in (0,1,2,3,4,5,9)) disable ,
   CODE                 NUMBER(1)            default 0 not null
check (CODE in (0,1,2,9)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_NAMES foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_RTIC                                          */
/*==============================================================*/
create table RD_LINK_RTIC 
(
   LINK_PID             NUMBER(10)           not null,
   CODE                 NUMBER(5)            default 0 not null,
   RANK                 NUMBER(1)            default 0 not null
check (RANK in (0,1,2,3,4)) disable ,
   RTIC_DIR             NUMBER(1)            default 0 not null
check (RTIC_DIR in (0,1,2)) disable ,
   UPDOWN_FLAG          NUMBER(1)            default 0 not null
check (UPDOWN_FLAG in (0,1)) disable ,
   RANGE_TYPE           NUMBER(1)            default 1 not null
check (RANGE_TYPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_RTICS foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_SIDEWALK                                      */
/*==============================================================*/
create table RD_LINK_SIDEWALK 
(
   LINK_PID             NUMBER(10)           not null,
   SIDEWALK_LOC         NUMBER(1)            default 0 not null
check (SIDEWALK_LOC in (0,1,2,3,4,5,6,7,8)) disable ,
   DIVIDER_TYPE         NUMBER(1)            default 0 not null
check (DIVIDER_TYPE in (0,1,2,3,4)) disable ,
   WORK_DIR             NUMBER(1)            default 0 not null
check (WORK_DIR in (0,1,2)) disable ,
   PROCESS_FLAG         NUMBER(1)            default 0 not null
check (PROCESS_FLAG in (0,1)) disable ,
   CAPTURE_FLAG         NUMBER(1)            default 0 not null
check (CAPTURE_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_SIDEWALK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_SPEEDLIMIT                                    */
/*==============================================================*/
create table RD_LINK_SPEEDLIMIT  (
   LINK_PID             NUMBER(10)                      not null,
   FROM_SPEED_LIMIT     NUMBER(4)                      default 0 not null,
   TO_SPEED_LIMIT       NUMBER(4)                      default 0 not null,
   SPEED_CLASS          NUMBER(1)                      default 0 not null
check (SPEED_CLASS between 0 and 8 and SPEED_CLASS in (0,1,2,3,4,5,6,7,8)) disable,
   FROM_LIMIT_SRC       NUMBER(2)                      default 0 not null
check (FROM_LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)) disable,
   TO_LIMIT_SRC         NUMBER(2)                      default 0 not null
check (TO_LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)) disable,
   SPEED_TYPE           NUMBER(1)                      default 0 not null
check (SPEED_TYPE in (0,1 ,3 )) disable,
   SPEED_DEPENDENT      NUMBER(2)                      default 0 not null
check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  ,13,14,15,16,17,18)) disable,
   TIME_DOMAIN          VARCHAR2(1000),
   SPEED_CLASS_WORK     NUMBER(1)                      default 1 not null
check (SPEED_CLASS_WORK in (0,1)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_SPEEDLIMIT foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_WALKSTAIR                                     */
/*==============================================================*/
create table RD_LINK_WALKSTAIR 
(
   LINK_PID             NUMBER(10)           not null,
   STAIR_LOC            NUMBER(1)            default 0 not null
check (STAIR_LOC in (0,1,2,3,4,5,6,7)) disable ,
   STAIR_FLAG           NUMBER(1)            default 0 not null
check (STAIR_FLAG in (0,1,2)) disable ,
   WORK_DIR             NUMBER(1)            default 0 not null
check (WORK_DIR in (0,1,2)) disable ,
   CAPTURE_FLAG         NUMBER(1)            default 0 not null
check (CAPTURE_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_STAIR foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_LINK_ZONE                                          */
/*==============================================================*/
create table RD_LINK_ZONE 
(
   LINK_PID             NUMBER(10)           not null,
   REGION_ID            NUMBER(10)           default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1,2,3)) disable ,
   SIDE                 NUMBER(1)            default 0 not null
check (SIDE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_ZONES foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_MAINSIDE                                           */
/*==============================================================*/
create table RD_MAINSIDE 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_MAINSIDE primary key (GROUP_ID)
);

/*==============================================================*/
/* Table: RD_MAINSIDE_LINK                                      */
/*==============================================================*/
create table RD_MAINSIDE_LINK 
(
   GROUP_ID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   LINK_TYPE            NUMBER(1)            default 0 not null
check (LINK_TYPE in (0,1)) disable ,
   SIDE                 NUMBER(1)            default 0 not null
check (SIDE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDMAINSIDE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDMAINSIDE foreign key (GROUP_ID)
         references RD_MAINSIDE (GROUP_ID) disable
);

/*==============================================================*/
/* Table: RD_MULTIDIGITIZED                                     */
/*==============================================================*/
create table RD_MULTIDIGITIZED 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_MULTIDIGITIZED primary key (GROUP_ID)
);

/*==============================================================*/
/* Table: RD_MULTIDIGITIZED_LINK                                */
/*==============================================================*/
create table RD_MULTIDIGITIZED_LINK 
(
   GROUP_ID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   SIDE                 NUMBER(1)            default 1 not null
check (SIDE in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDMULTIDIGITIZED_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDMULTIDIGITIZED foreign key (GROUP_ID)
         references RD_MULTIDIGITIZED (GROUP_ID) disable
);

/*==============================================================*/
/* Table: RD_NAME                                               */
/*==============================================================*/
create table RD_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          not null,
   NAME                 VARCHAR2(500)        not null,
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
   SRC_FLAG             NUMBER(2)           
check (SRC_FLAG in (0,1,2,3)) disable ,
   ROAD_TYPE            NUMBER(1)            not null
check (ROAD_TYPE in (0,1,2,3,4)) disable ,
   ADMIN_ID             NUMBER(6)            not null,
   CODE_TYPE            NUMBER(1)            not null
check (CODE_TYPE in (0,1,2,3,4,5,6,7)) disable ,
   VOICE_FILE           VARCHAR2(100),
   SRC_RESUME           VARCHAR2(1000),
   PA_REGION_ID         NUMBER(10),
   SPLIT_FLAG           NUMBER(2)      default 0
check (SPLIT_FLAG in (0,1,2)) disable ,
   MEMO                 VARCHAR2(200),
   ROUTE_ID             NUMBER(10),
   U_RECORD             NUMBER(2),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NAME primary key (NAME_ID)
);

/*==============================================================*/
/* Table: RD_NODE_FORM                                          */
/*==============================================================*/
create table RD_NODE_FORM 
(
   NODE_PID             NUMBER(10)           not null,
   FORM_OF_WAY          NUMBER(2)            default 1 not null
check (FORM_OF_WAY in (0,1,2,3,4,5,6,10,11,12,13,14,15,16,20,21,22,23,30,31,32,41)) disable ,
   AUXI_FLAG            NUMBER(2)            default 0 not null
check (AUXI_FLAG in (0,42,43)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNODE_FORM foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_NODE_MESH                                          */
/*==============================================================*/
create table RD_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNODE_MESH foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_NODE_NAME                                          */
/*==============================================================*/
create table RD_NODE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   NODE_PID             NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NODE_NAME primary key (NAME_ID),
   constraint RDNODE_NAME foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_OBJECT                                             */
/*==============================================================*/
create table RD_OBJECT 
(
   PID                  NUMBER(10)           not null,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_OBJECT primary key (PID)
);

/*==============================================================*/
/* Table: RD_OBJECT_INTER                                       */
/*==============================================================*/
create table RD_OBJECT_INTER 
(
   PID                  NUMBER(10)           not null,
   INTER_PID            NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_INTERSECTION foreign key (INTER_PID)
         references RD_INTER (PID) disable ,
   constraint RDOBJECT_INTERSECTIONS foreign key (PID)
         references RD_OBJECT (PID) disable
);

/*==============================================================*/
/* Table: RD_OBJECT_LINK                                        */
/*==============================================================*/
create table RD_OBJECT_LINK 
(
   PID                  NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDOBJECT_LINKS foreign key (PID)
         references RD_OBJECT (PID) disable
);

/*==============================================================*/
/* Table: RD_OBJECT_NAME                                        */
/*==============================================================*/
create table RD_OBJECT_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_OBJECT_NAME primary key (NAME_ID),
   constraint RDOBJECT_NAME foreign key (PID)
         references RD_OBJECT (PID) disable
);

/*==============================================================*/
/* Table: RD_OBJECT_NODE                                        */
/*==============================================================*/
create table RD_OBJECT_NODE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_NODES foreign key (PID)
         references RD_OBJECT (PID) disable ,
   constraint RDOBJECT_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_ROAD                                               */
/*==============================================================*/
create table RD_ROAD 
(
   PID                  NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_ROAD primary key (PID)
);

/*==============================================================*/
/* Table: RD_OBJECT_ROAD                                        */
/*==============================================================*/
create table RD_OBJECT_ROAD 
(
   PID                  NUMBER(10)           not null,
   ROAD_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDOBJECT_ROAD foreign key (ROAD_PID)
         references RD_ROAD (PID) disable ,
   constraint RDOBJECT_ROADS foreign key (PID)
         references RD_OBJECT (PID) disable
);

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
check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_RESTRICTION primary key (PID),
   constraint RDRESTRICT_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDRESTRICTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_RESTRICTION_DETAIL                                 */
/*==============================================================*/
create table RD_RESTRICTION_DETAIL 
(
   DETAIL_ID            NUMBER(10)           not null,
   RESTRIC_PID          NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   FLAG                 NUMBER(1)            default 2 not null
check (FLAG in (0,1,2)) disable ,
   RESTRIC_INFO         NUMBER(1)            default 0 not null
check (RESTRIC_INFO in (0,1,2,3,4)) disable ,
   TYPE                 NUMBER(1)            default 1 not null
check (TYPE in (0,1,2)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_RESTRICTION_DETAIL primary key (DETAIL_ID),
   constraint RDRESTRIC_DETAIL foreign key (RESTRIC_PID)
         references RD_RESTRICTION (PID) disable ,
   constraint RDRESTRICTDETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_RESTRICTION_CONDITION                              */
/*==============================================================*/
create table RD_RESTRICTION_CONDITION  (
   DETAIL_ID            NUMBER(10)                      not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   RES_TRAILER          NUMBER(1)                      default 0 not null
       check (RES_TRAILER in (0,1)) disable,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   RES_AXLE_LOAD        NUMBER(5,2)                    default 0 not null,
   RES_AXLE_COUNT       NUMBER(2)                      default 0 not null,
   RES_OUT              NUMBER(1)                      default 0 not null
       check (RES_OUT in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint RDRESTRICCONDITION_DETAIL foreign key (DETAIL_ID)
         references RD_RESTRICTION_DETAIL (DETAIL_ID) disable
);


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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDRESTRICTION_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDRESTRICVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_RESTRICTION_DETAIL (DETAIL_ID) disable
);

/*==============================================================*/
/* Table: RD_ROAD_LINK                                          */
/*==============================================================*/
create table RD_ROAD_LINK 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1 not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDROAD_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDROAD_LINKS foreign key (PID)
         references RD_ROAD (PID) disable
);

/*==============================================================*/
/* Table: RD_SAMELINK                                           */
/*==============================================================*/
create table RD_SAMELINK 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SAMELINK primary key (GROUP_ID)
);

/*==============================================================*/
/* Table: RD_SAMELINK_PART                                      */
/*==============================================================*/
create table RD_SAMELINK_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSAMELINK_PARTS foreign key (GROUP_ID)
         references RD_SAMELINK (GROUP_ID) disable
);

/*==============================================================*/
/* Table: RD_SAMENODE                                           */
/*==============================================================*/
create table RD_SAMENODE 
(
   GROUP_ID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SAMENODE primary key (GROUP_ID)
);

/*==============================================================*/
/* Table: RD_SAMENODE_PART                                      */
/*==============================================================*/
create table RD_SAMENODE_PART 
(
   GROUP_ID             NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSAMENODE_PARTS foreign key (GROUP_ID)
         references RD_SAMENODE (GROUP_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SE primary key (PID),
   constraint RDSE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_SERIESBRANCH                                       */
/*==============================================================*/
create table RD_SERIESBRANCH 
(
   BRANCH_PID           NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1)) disable ,
   VOICE_DIR            NUMBER(1)            default 0 not null
check (VOICE_DIR in (0,2,5)) disable ,
   PATTERN_CODE         VARCHAR2(10),
   ARROW_CODE           VARCHAR2(10),
   ARROW_FLAG           NUMBER(2)            default 0 not null
check (ARROW_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSERIESBRANCH foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

/*==============================================================*/
/* Table: RD_SIGNASREAL                                         */
/*==============================================================*/
create table RD_SIGNASREAL  (
   SIGNBOARD_ID         NUMBER(10)                      not null,
   BRANCH_PID           NUMBER(10)                      not null,
   SVGFILE_CODE         VARCHAR2(100),
   ARROW_CODE           VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNASREAL primary key (SIGNBOARD_ID),
   constraint RDBRANCH_SIGNASREAL foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNBOARD primary key (SIGNBOARD_ID),
   constraint RDBRANCH_SIGNBOARD foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID) disable
);

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
check (NAME_CLASS in (0,1)) disable ,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   CODE_TYPE            NUMBER(2)            default 0 not null
check (CODE_TYPE in (0,1,2,3,4,5,6,7,8,9,10)) disable ,
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   VOICE_FILE           VARCHAR2(100),
   SRC_FLAG             NUMBER(2)            default 0 not null
check (SRC_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNBOARD_NAME primary key (NAME_ID),
   constraint RDSIGNBOARD_NAME foreign key (SIGNBOARD_ID)
         references RD_SIGNBOARD (SIGNBOARD_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSIGNBOARDNAME_TONE foreign key (NAME_ID)
         references RD_SIGNBOARD_NAME (NAME_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SIGNPOST primary key (PID)
);


/*==============================================================*/
/* Table: RD_SIGNPOST_LINK                                      */
/*==============================================================*/
create table RD_SIGNPOST_LINK 
(
   SIGNPOST_PID         NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSIGNPOST_CONTROLLINK foreign key (SIGNPOST_PID)
         references RD_SIGNPOST (PID) disable
);

/*==============================================================*/
/* Table: RD_SIGNPOST_PHOTO                                     */
/*==============================================================*/
create table RD_SIGNPOST_PHOTO 
(
   SIGNPOST_PID         NUMBER(10)           not null,
   PHOTO_ID             NUMBER(10)           default 0 not null,
   STATUS               VARCHAR2(100),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSIGNPOST_PHOTOES foreign key (SIGNPOST_PID)
         references RD_SIGNPOST (PID) disable
);

/*==============================================================*/
/* Table: RD_SLOPE                                              */
/*==============================================================*/
create table RD_SLOPE 
(
   PID                  NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   TYPE                 NUMBER(1)            default 1 not null
check (TYPE in (0,1,2,3)) disable ,
   ANGLE                NUMBER(2)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SLOPE primary key (PID),
   constraint RDSLOPE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSLOPE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_SPEEDLIMIT                                         */
/*==============================================================*/
create table RD_SPEEDLIMIT  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)) disable,
   SPEED_VALUE          NUMBER(4)                      default 0 not null,
   SPEED_TYPE           NUMBER(1)                      default 0 not null
       check (SPEED_TYPE in (0,1 ,3 ,4)) disable,
   TOLLGATE_FLAG        NUMBER(1)                      default 0 not null
       check (TOLLGATE_FLAG in (0,1)) disable,
   SPEED_DEPENDENT      NUMBER(2)                      default 0 not null
       check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  ,13,14,15,16,17,18)) disable,
   SPEED_FLAG           NUMBER(1)                      default 0 not null
       check (SPEED_FLAG in (0,1)) disable,
   LIMIT_SRC            NUMBER(2)                      default 1 not null
       check (LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)) disable,
   TIME_DOMAIN          VARCHAR2(1000),
   CAPTURE_FLAG         NUMBER(1)                      default 0 not null
       check (CAPTURE_FLAG in (0,1)) disable,
   DESCRIPT             VARCHAR2(100),
   MESH_ID              NUMBER(6)                      default 0 not null,
   STATUS               NUMBER(1)                      default 7 not null
       check (STATUS in (0,1,2,3,4,5,6,7)) disable,
   CK_STATUS            NUMBER(2)                      default 6 not null
       check (CK_STATUS in (0,1,2,3,4,5,6)) disable,
   ADJA_FLAG            NUMBER(2)                      default 0 not null
       check (ADJA_FLAG in (0,1,2)) disable,
   REC_STATUS_IN        NUMBER(1)                      default 0 not null
       check (REC_STATUS_IN in (0,2,3)) disable,
   REC_STATUS_OUT       NUMBER(1)                      default 0 not null
       check (REC_STATUS_OUT in (0,1,2,3)) disable,
   TIME_DESCRIPT        VARCHAR2(1000),
   GEOMETRY             SDO_GEOMETRY,
   LANE_SPEED_VALUE     VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SPEEDLIMIT primary key (PID)
);

/*==============================================================*/
/* Table: RD_TMCLOCATION                                        */
/*==============================================================*/
create table RD_TMCLOCATION  (
   GROUP_ID             NUMBER(10)                      not null,
   TMC_ID               NUMBER(10)                     default 0 not null,
   LOCTABLE_ID          NUMBER(2)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TMCLOCATION primary key (GROUP_ID)
);


/*==============================================================*/
/* Table: RD_TMCLOCATION_LINK                                   */
/*==============================================================*/
create table RD_TMCLOCATION_LINK 
(
   GROUP_ID             NUMBER(10)           not null,
   LINK_PID             NUMBER(10)           not null,
   LOC_DIRECT           NUMBER(1)            default 0 not null
check (LOC_DIRECT in (0,1,2,3,4)) disable ,
   DIRECT               NUMBER(1)            default 0 not null
check (DIRECT in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDTMCLOCATION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDTMCLOCATION_LINKS foreign key (GROUP_ID)
         references RD_TMCLOCATION (GROUP_ID) disable
);

/*==============================================================*/
/* Table: RD_TOLLGATE                                           */
/*==============================================================*/
create table RD_TOLLGATE  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   TYPE                 NUMBER(2)                      default 0 not null
check (TYPE in (0,1,2,3,4,5,6,7,8,9,10)) disable,
   PASSAGE_NUM          NUMBER(2)                      default 0 not null,
   ETC_FIGURE_CODE      VARCHAR2(8),
   HW_NAME              VARCHAR2(1000),
   FEE_TYPE             NUMBER(1)                      default 0 not null
check (FEE_TYPE in (0,1,2)) disable,
   FEE_STD              NUMBER(5,2)                    default 0 not null,
   SYSTEM_ID            NUMBER(6)                      default 0 not null,
   LOCATION_FLAG        NUMBER(1)                      default 0 not null
       check (LOCATION_FLAG in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TOLLGATE primary key (PID),
   constraint RDTOLLGATE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint RDTOLLGATE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint RDTOLLGATE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_TOLLGATE_COST                                      */
/*==============================================================*/
create table RD_TOLLGATE_COST 
(
   TOLLCOST_ID          NUMBER(10)           not null,
   IN_TOLLGATE          NUMBER(10)           default 0 not null,
   OUT_TOLLGATE         NUMBER(10)           default 0 not null,
   FEE                  NUMBER(10,2)         default 0 not null,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1)) disable ,
   CASH                 VARCHAR2(3)          default 'CNY' not null
check (CASH in ('CNY','HKD','MOP')) disable ,
   VEHICLE_CLASS        NUMBER(1)            default 1 not null
check (VEHICLE_CLASS in (1,2,3,4,5)) disable ,
   DISTANCE             NUMBER(10,4)         default 0 not null,
   SYSTEM_ID            NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_COST primary key (TOLLCOST_ID)
);

/*==============================================================*/
/* Table: RD_TOLLGATE_FEE                                       */
/*==============================================================*/
create table RD_TOLLGATE_FEE 
(
   TOLLCOST_ID          NUMBER(10)           not null,
   S_TOLL_OLD           VARCHAR2(1000)       ,
   E_TOLL_OLD           VARCHAR2(1000)       ,
   S_TOLL_NEW           VARCHAR2(1000)       ,
   E_TOLL_NEW           VARCHAR2(1000)       ,
   S_MAPPINGID          NUMBER(10)           default 0 not null,
   E_MAPPINGID          NUMBER(10)           default 0 not null,
   CASH                 VARCHAR2(3)          default 'CNY' not null
check (CASH in ('CNY','HKD','MOP')) disable ,
   FEE                  NUMBER(10,2)         default 0 not null,
   VEHICLE_CLASS        NUMBER(1)            default 1 not null
check (VEHICLE_CLASS in (1,2,3,4,5)) disable ,
   DISTANCE             NUMBER(10,4)         default 0 not null,
   FLAG                 VARCHAR2(100),
   SYSTEM_ID            NUMBER(6),
   VER_INFO             VARCHAR2(100),
   MEMO                 VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_FEE primary key (TOLLCOST_ID)
);

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

/*==============================================================*/
/* Table: RD_TOLLGATE_NAME                                      */
/*==============================================================*/
create table RD_TOLLGATE_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_NAME primary key (NAME_ID),
   constraint RDTOLLGTE_NAME foreign key (PID)
         references RD_TOLLGATE (PID) disable
);

/*==============================================================*/
/* Table: RD_TOLLGATE_PASSAGE                                   */
/*==============================================================*/
create table RD_TOLLGATE_PASSAGE 
(
   PID                  NUMBER(10)           not null,
   SEQ_NUM              NUMBER(2)            default 1 not null,
   TOLL_FORM            NUMBER(10)           default 0 not null,
   CARD_TYPE            NUMBER(1)            default 0 not null
check (CARD_TYPE in (0,1,2,3)) disable ,
   VEHICLE              NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDTOLLGATE_PASSAGE foreign key (PID)
         references RD_TOLLGATE (PID) disable
);

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
check (FLAG in (0,1,2)) disable ,
   TYPE                 NUMBER(1)            default 0 not null
check (TYPE in (0,1,2,3,4,5)) disable ,
   KG_FLAG              NUMBER(1)            default 0 not null
check (KG_FLAG in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_TRAFFICSIGNAL primary key (PID),
   constraint RDSIGNAL_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDSIGNAL_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

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
check (SPEED_TYPE in (0,1 ,2 ,3 )) disable ,
   SPEED_DEPENDENT      NUMBER(2)            default 0 not null
check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  )) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VARIABLE_SPEED primary key (VSPEED_PID),
   constraint RDVARIABLESPEED_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVARIABLESPEED_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDVARIABLESPEED_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_VIRCONNECT                                         */
/*==============================================================*/
create table RD_VIRCONNECT 
(
   PID                  NUMBER(10)           not null,
   TYPE                 NUMBER(2)            default 0 not null
check (TYPE in (0,1,2,3,4,5,11,12,13,14,15,99)) disable ,
   OBSTACLE_FREE        NUMBER(1)            default 0 not null
check (OBSTACLE_FREE in (0,1,2)) disable ,
   FEE                  NUMBER(1)            default 0 not null
check (FEE in (0,1)) disable ,
   STREET_LIGHT         NUMBER(1)            default 0 not null
check (STREET_LIGHT in (0,1,2,3)) disable ,
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VIRCONNECT primary key (PID)
);

/*==============================================================*/
/* Table: RD_VIRCONNECT_NAME                                    */
/*==============================================================*/
create table RD_VIRCONNECT_NAME 
(
   NAME_ID              NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   PID                  NUMBER(10)           not null,
   LANG_CODE            VARCHAR2(3)          default 'CHI' not null
check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VIRCONNECT_NAME primary key (NAME_ID),
   constraint RDVIRCONNECT_NAME foreign key (PID)
         references RD_VIRCONNECT (PID) disable
);

/*==============================================================*/
/* Table: RD_VIRCONNECT_TRANSIT                                 */
/*==============================================================*/
create table RD_VIRCONNECT_TRANSIT 
(
   PID                  NUMBER(10)           not null,
   FIR_NODE_PID         NUMBER(10)           not null,
   SEN_NODE_PID         NUMBER(10)           not null,
   TRANSIT              NUMBER(1)            default 0 not null
check (TRANSIT in (0,1,2,3)) disable ,
   SLOPE                NUMBER(1)            default 0 not null
check (SLOPE in (0,1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDVIRCONNCT_FIRNODE foreign key (FIR_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDVIRCONNCT_SECNODE foreign key (SEN_NODE_PID)
         references RD_NODE (NODE_PID) disable ,
   constraint RDVIRCONNECT_NODES foreign key (PID)
         references RD_VIRCONNECT (PID) disable
);

/*==============================================================*/
/* Table: RD_VOICEGUIDE                                         */
/*==============================================================*/
create table RD_VOICEGUIDE 
(
   PID                  NUMBER(10)           not null,
   IN_LINK_PID          NUMBER(10)           not null,
   NODE_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VOICEGUIDE primary key (PID),
   constraint RDVOICE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVOICE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RD_VOICEGUIDE_DETAIL                                  */
/*==============================================================*/
create table RD_VOICEGUIDE_DETAIL 
(
   DETAIL_ID            NUMBER(10)           not null,
   VOICEGUIDE_PID       NUMBER(10)           not null,
   OUT_LINK_PID         NUMBER(10)           not null,
   GUIDE_CODE           NUMBER(2)            default 0 not null
check (GUIDE_CODE in (0,1,2,4,6,7,8,10,12,16,19)) disable ,
   GUIDE_TYPE           NUMBER(1)            default 0 not null
check (GUIDE_TYPE in (0,1,2,3)) disable ,
   PROCESS_FLAG         NUMBER(1)            default 1 not null
check (PROCESS_FLAG in (0,1,2)) disable ,
   RELATIONSHIP_TYPE    NUMBER(1)            default 1 not null
check (RELATIONSHIP_TYPE in (1,2)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_VOICEGUIDE_DETAIL primary key (DETAIL_ID),
   constraint RDVOICE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVOICE_DETAIL foreign key (VOICEGUIDE_PID)
         references RD_VOICEGUIDE (PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RDVOICEGUIDEVIALINK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDVOICEGUIDEVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_VOICEGUIDE_DETAIL (DETAIL_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_WARNINGINFO primary key (PID),
   constraint RDWARNING_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable ,
   constraint RDWARNING_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_FEATURE primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: RW_FEATURE_20W                                        */
/*==============================================================*/
create table RW_FEATURE_20W 
(
   FEATURE_PID          NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_FEATURE_20W primary key (FEATURE_PID)
);

/*==============================================================*/
/* Table: RW_NODE                                               */
/*==============================================================*/
create table RW_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (1,2)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,2,3,4,5,6)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_NODE primary key (NODE_PID)
);

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
check (KIND in (1,2,3)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,2)) disable ,
   LENGTH               NUMBER(15,3)         default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null,
   COLOR                VARCHAR2(10),
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_LINK primary key (LINK_PID),
   constraint RWLINK_ENODE foreign key (E_NODE_PID)
         references RW_NODE (NODE_PID) disable ,
   constraint RWLINK_SNODE foreign key (S_NODE_PID)
         references RW_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RW_NODE_20W                                           */
/*==============================================================*/
create table RW_NODE_20W 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (1,2)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,2,3,4,5,6)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_NODE_20W primary key (NODE_PID)
);

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
check (KIND in (1,2,3)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,2)) disable ,
   LENGTH               NUMBER(15,3)         default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)            default 0 not null,
   SCALE                NUMBER(1)            default 0 not null
check (SCALE in (0,1,2)) disable ,
   DETAIL_FLAG          NUMBER(1)            default 0 not null
check (DETAIL_FLAG in (0,1,2,3)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   COLOR                VARCHAR2(10),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RW_LINK_20W primary key (LINK_PID),
   constraint RWLINK_ENODE_20W foreign key (E_NODE_PID)
         references RW_NODE_20W (NODE_PID) disable ,
   constraint RWLINK_SNODE_20W foreign key (S_NODE_PID)
         references RW_NODE_20W (NODE_PID) disable
);

/*==============================================================*/
/* Table: RW_LINK_NAME                                          */
/*==============================================================*/
create table RW_LINK_NAME 
(
   LINK_PID             NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWLINK_NAME foreign key (LINK_PID)
         references RW_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RW_LINK_NAME_20W                                      */
/*==============================================================*/
create table RW_LINK_NAME_20W 
(
   LINK_PID             NUMBER(10)           not null,
   NAME_GROUPID         NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWLINK_NAME_20W foreign key (LINK_PID)
         references RW_LINK_20W (LINK_PID) disable
);

/*==============================================================*/
/* Table: RW_NODE_MESH                                          */
/*==============================================================*/
create table RW_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWNODE_MESH foreign key (NODE_PID)
         references RW_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: RW_NODE_MESH_20W                                      */
/*==============================================================*/
create table RW_NODE_MESH_20W 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint RWNODE_MESH_20W foreign key (NODE_PID)
         references RW_NODE_20W (NODE_PID) disable
);

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

/*==============================================================*/
/* Table: TMC_AREA                                              */
/*==============================================================*/
create table TMC_AREA 
(
   TMC_ID               NUMBER(10)           not null,
   LOCTABLE_ID          VARCHAR2(2),
    CID          VARCHAR2(4)       not null,
   LOC_CODE             NUMBER(5)            default 0 not null,
   TYPE_CODE            VARCHAR2(32)        
check (TYPE_CODE is null or (TYPE_CODE in ('A1.0','A2.0','A3.0','A5.0','A5.1','A5.2','A5.3','A6.0','A6.1','A6.2','A6.3','A6.4','A6.5','A6.6','A6.7','A6.8','A7.0','A8.0','A9.0','A9.1','A9.2','A10.0','A11.0','A12.0'))) disable ,
   UPAREA_TMC_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_AREA primary key (TMC_ID)
);

/*==============================================================*/
/* Table: TMC_AREA_TRANSLATENAME                                */
/*==============================================================*/
create table TMC_AREA_TRANSLATENAME 
(
   TMC_ID               NUMBER(10)           not null,
   TRANS_LANG           VARCHAR2(3)          default 'CHI' not null
check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint TMCAREA_NAME foreign key (TMC_ID)
         references TMC_AREA (TMC_ID) disable
);

/*==============================================================*/
/* Table: TMC_LINE                                              */
/*==============================================================*/
create table TMC_LINE 
(
   TMC_ID               NUMBER(10)           not null,
   LOCTABLE_ID          VARCHAR2(2),
   CID          VARCHAR2(4)         not null,
   LOC_CODE             NUMBER(5)            default 0 not null,
   TYPE_CODE            VARCHAR2(32)        
check (TYPE_CODE is null or (TYPE_CODE in ('L1.0','L1.1','L1.2','L1.3','L1.4','L2.0','L2.1','L2.2','L3.0','L4.0','L5.0','L6.0','L6.1','L6.2','L7.0'))) disable ,
   SEQ_NUM              NUMBER(5)            default 0 not null,
   AREA_TMC_ID          NUMBER(10)           default 0 not null,
   LOCOFF_POS           NUMBER(10)           default 0 not null,
   LOCOFF_NEG           NUMBER(10)           default 0 not null,
   UPLINE_TMC_ID        NUMBER(10)           default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_LINE primary key (TMC_ID)
);

/*==============================================================*/
/* Table: TMC_LINE_TRANSLATENAME                                */
/*==============================================================*/
create table TMC_LINE_TRANSLATENAME 
(
   TMC_ID               NUMBER(10)           not null,
   NAME_FLAG            NUMBER(1)            default 0 not null
check (NAME_FLAG in (0,1,2)) disable ,
   TRANS_LANG           VARCHAR2(3)          default 'CHI' not null
check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint TMCLINE_TRANSLATENAME foreign key (TMC_ID)
         references TMC_LINE (TMC_ID) disable
);



/*==============================================================*/
/* Table: TMC_POINT                                             */
/*==============================================================*/
create table TMC_POINT 
(
   TMC_ID               NUMBER(10)           not null,
   LOCTABLE_ID          VARCHAR2(2),
    CID          VARCHAR2(4)       not null,
   LOC_CODE             NUMBER(5)            default 0 not null,
   TYPE_CODE            VARCHAR2(32)        
check (TYPE_CODE is null or (TYPE_CODE in ('P1.0','P1.1','P1.2','P1.3','P1.4','P1.5','P1.6','P1.7','P1.8','P1.9','P1.10','P1.11','P1.12','P1.13','P1.14','P1.15','P2.0','P2.1','P2.2','P3.0','P3.1','P3.2','P3.3','P3.4','P3.5','P3.6','P3.7','P3.8','P3.9','P3.10','P3.11','P3.12','P3.13','P3.14','P3.15','P3.16','P3.17','P3.18','P3.19','P3.20','P3.21','P3.22','P3.23','P3.24','P3.25','P3.26','P3.27','P3.28','P3.29','P3.30','P3.31','P3.34','P3.35','P3.36','P3.37','P3.38','P3.39','P3.40','P3.41','P3.42','P3.43','P3.44','P3.45','P3.46','P3.47','P4.0'))) disable ,
   IN_POS               NUMBER(1)            default 0 not null
check (IN_POS in (0,1)) disable ,
   IN_NEG               NUMBER(1)            default 0 not null
check (IN_NEG in (0,1)) disable ,
   OUT_POS              NUMBER(1)            default 0 not null
check (OUT_POS in (0,1)) disable ,
   OUT_NEG              NUMBER(1)            default 0 not null
check (OUT_NEG in (0,1)) disable ,
   PRESENT_POS          NUMBER(1)            default 0 not null
check (PRESENT_POS in (0,1)) disable ,
   PRESENT_NEG          NUMBER(1)            default 0 not null
check (PRESENT_NEG in (0,1)) disable ,
   LOCOFF_POS           NUMBER(10)           default 0 not null,
   LOCOFF_NEG           NUMBER(10)           default 0 not null,
   LINE_TMC_ID          NUMBER(10)           default 0 not null,
   AREA_TMC_ID          NUMBER(10)           default 0 not null,
   JUNC_LOCCODE         NUMBER(10)           default 0 not null,
   NEIGHBOUR_BOUND      VARCHAR2(32),
   NEIGHBOUR_TABLE      NUMBER(2)            default 0 not null,
   URBAN                NUMBER(1)            default 0 not null
check (URBAN in (0,1)) disable ,
   INTERUPT_ROAD        NUMBER(1)            default 0 not null
check (INTERUPT_ROAD in (0,1)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_POINT primary key (TMC_ID)
);

/*==============================================================*/
/* Table: TMC_POINT_TRANSLATENAME                               */
/*==============================================================*/
create table TMC_POINT_TRANSLATENAME 
(
   TMC_ID               NUMBER(10)           not null,
   NAME_FLAG            NUMBER(1)            default 1 not null
check (NAME_FLAG in (1,2,3)) disable ,
   TRANS_LANG           VARCHAR2(3)          default 'CHI' not null
check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable ,
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint TMCPOINT_TRANSLATENAME foreign key (TMC_ID)
         references TMC_POINT (TMC_ID) disable
);

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
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000)
);

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
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ZONE_FACE primary key (FACE_PID),
   constraint ADADMIN_ZONEFACE foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID) disable
);

/*==============================================================*/
/* Table: ZONE_NODE                                             */
/*==============================================================*/
create table ZONE_NODE 
(
   NODE_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (1,2,3)) disable ,
   FORM                 NUMBER(1)            default 0 not null
check (FORM in (0,1,7)) disable ,
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ZONE_NODE primary key (NODE_PID)
);

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
check (SCALE in (0,1,2)) disable ,
   EDIT_FLAG            NUMBER(1)            default 1 not null
check (EDIT_FLAG in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_ZONE_LINK primary key (LINK_PID),
   constraint ZONELINK_SNODE foreign key (S_NODE_PID)
         references ZONE_NODE (NODE_PID) disable ,
   constraint ZONELINK_ENODE foreign key (E_NODE_PID)
         references ZONE_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: ZONE_FACE_TOPO                                        */
/*==============================================================*/
create table ZONE_FACE_TOPO 
(
   FACE_PID             NUMBER(10)           not null,
   SEQ_NUM              NUMBER(3)            default 1,
   LINK_PID             NUMBER(10)           not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONEFACE_LINK foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID) disable ,
   constraint ZONEFACE_LINKS foreign key (FACE_PID)
         references ZONE_FACE (FACE_PID) disable
);

/*==============================================================*/
/* Table: ZONE_LINK_KIND                                        */
/*==============================================================*/
create table ZONE_LINK_KIND 
(
   LINK_PID             NUMBER(10)           not null,
   KIND                 NUMBER(1)            default 1 not null
check (KIND in (0,1,2)) disable ,
   FORM                 NUMBER(1)            default 1 not null
check (FORM in (0,1)) disable ,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONELINK_LINKKIND foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: ZONE_LINK_MESH                                        */
/*==============================================================*/
create table ZONE_LINK_MESH 
(
   LINK_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONELINK_MESH foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: ZONE_NODE_MESH                                        */
/*==============================================================*/
create table ZONE_NODE_MESH 
(
   NODE_PID             NUMBER(10)           not null,
   MESH_ID              NUMBER(6)            default 0 not null,
   U_RECORD             NUMBER(2)            default 0 not null
check (U_RECORD in (0,1,2,3)) disable ,
   U_FIELDS             VARCHAR2(1000),
   constraint ZONENODE_MESH foreign key (NODE_PID)
         references ZONE_NODE (NODE_PID) disable
);





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
create index IDX_OPERATE_TABLE_NAME on OPERATE_LOG (TABLE_NAME, NVL(OPERATE_STATE_ID,0));


/*==============================================================*/
/* Table: RD_LINK_PARAM_ADAS                                    */
/*==============================================================*/
create table RD_LINK_PARAM_ADAS  (
   LINK_PID             NUMBER(10)                      not null,
   OFFSET               NUMBER(10,3)                   default 0 not null,
   RADIUS               NUMBER(10,3)                   default 0 not null,
   LEN                  NUMBER(10,3)                   default 0 not null,
   PARAM                NUMBER(10,3)                   default 0 not null,
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint FK_RDLINK_PARAM_ADAS foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_LINK_PARAM_ADAS is
'[190A]';

comment on column RD_LINK_PARAM_ADAS.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_LINK_PARAM_ADAS.OFFSET is
'单位:米';

comment on column RD_LINK_PARAM_ADAS.RADIUS is
'单位:米,回旋曲线上某点的曲率半径';

comment on column RD_LINK_PARAM_ADAS.LEN is
'单位:米,回旋曲线上指定点到起算点的曲线长';

comment on column RD_LINK_PARAM_ADAS.PARAM is
'回旋曲线的参数,A2=C 为常数';

comment on column RD_LINK_PARAM_ADAS.U_RECORD is
'增量更新标识';

comment on column RD_LINK_PARAM_ADAS.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: RD_SPEEDBUMP                                          */
/*==============================================================*/
create table RD_SPEEDBUMP  (
   BUMP_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   MEMO                 VARCHAR2(1000),
   RESERVED             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_SPEEDBUMP primary key (BUMP_PID),
   constraint FK_RDSPEEDBUMP foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable,
   constraint FK_RDSPEEDBUMP_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on column RD_SPEEDBUMP.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_SPEEDBUMP.NODE_PID is
'外键,引用"RD_NODE"';

comment on column RD_SPEEDBUMP.RESERVED is
'结构预留';

comment on column RD_SPEEDBUMP.U_RECORD is
'增量更新标识';

comment on column RD_SPEEDBUMP.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: NI_VAL_EXCEPTION                                      */
/*==============================================================*/
create table NI_VAL_EXCEPTION  (
   VAL_EXCEPTION_ID     NUMBER(10)                     default 0 not null,
   RULEID               VARCHAR2(100),
   TASK_NAME            VARCHAR2(50),
   GROUPID              NUMBER(10)                     default 0 not null,
   "LEVEL"              NUMBER(10)                     default 0 not null,
   SITUATION            VARCHAR2(4000),
   INFORMATION          VARCHAR2(4000),
   SUGGESTION           VARCHAR2(4000),
   LOCATION             SDO_GEOMETRY,
   TARGETS              CLOB,
   ADDITION_INFO        CLOB,
   DEL_FLAG             NUMBER(1)                      default 0 not null
       check (DEL_FLAG in (0,1)) disable,
   CREATED              DATE,
   UPDATED              DATE,
   MESH_ID              NUMBER(6),
   SCOPE_FLAG           NUMBER(2)                      default 1 not null
       check (SCOPE_FLAG in (1,2,3)) disable,
   PROVINCE_NAME        VARCHAR2(60),
   MAP_SCALE            NUMBER(2)                      default 0 not null
       check (MAP_SCALE in (0,1,2,3)) disable,
   RESERVED             VARCHAR2(1000),
   EXTENDED             VARCHAR2(1000),
   TASK_ID              VARCHAR2(500),
   QA_TASK_ID           VARCHAR2(500),
   QA_STATUS            NUMBER(2)                      default 2 not null
       check (QA_STATUS in (1,2))disable,
   WORKER               VARCHAR2(500),
   QA_WORKER            VARCHAR2(500),
   LOG_TYPE             NUMBER(5)                      default 0 not null
);

comment on column NI_VAL_EXCEPTION.RULEID is
'参考"CK_RULE"';

comment on column NI_VAL_EXCEPTION.CREATED is
'格式"YYYY/MM/DD HH:mm:ss"';

comment on column NI_VAL_EXCEPTION.UPDATED is
'格式"YYYY/MM/DD HH:mm:ss"';


/*==============================================================*/
/* Table: RD_SLOPE_VIA                                          */
/*==============================================================*/
create table RD_SLOPE_VIA  (
   SLOPE_PID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDSLOPE_VIA_LINK foreign key (SLOPE_PID)
         references RD_SLOPE (PID) disable,
   constraint RDSLOPE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

comment on table RD_SLOPE_VIA is
'[1901]';

comment on column RD_SLOPE_VIA.SLOPE_PID is
'外键,引用"RD_SLOPE"';

comment on column RD_SLOPE_VIA.LINK_PID is
'外键,引用"RD_LINK"';

comment on column RD_SLOPE_VIA.SEQ_NUM is
'从1开始递增编号';

comment on column RD_SLOPE_VIA.U_RECORD is
'增量更新标识';

comment on column RD_SLOPE_VIA.U_FIELDS is
'记录更新的英文字段名,多个之间采用半角''|''分隔';

/*==============================================================*/
/* Table: IX_POI_DETAIL                                         */
/*==============================================================*/
create table IX_POI_DETAIL  (
   POI_PID              NUMBER(10)                     default 0 not null,
   WEB_SITE             VARCHAR2(200),
   FAX                  VARCHAR2(50),
   STAR_HOTEL           VARCHAR2(10),
   BRIEF_DESC           VARCHAR2(254),
   ADVER_FLAG           NUMBER(1)                      default 0 not null
       check (ADVER_FLAG in (0,1)) disable,
   PHOTO_NAME           VARCHAR2(254),
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   HW_ENTRYEXIT         NUMBER(1)                      default 0 not null
       check (HW_ENTRYEXIT in (0,1)) disable,
   PAYCARD              NUMBER(1)                      default 0 not null
       check (PAYCARD in (0,1)) disable,
   CARDTYPE             VARCHAR2(10),
   HOSPITAL_CLASS       NUMBER(2)                      default 0 not null
       check (HOSPITAL_CLASS in (0,1,2,3,4,5,6,7,8,9)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000)
);

/*==============================================================*/
/* Table: IX_POI_PARKING                                        */
/*==============================================================*/
create table IX_POI_PARKING  (
   PARKING_ID           NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   PARKING_TYPE         VARCHAR2(10),
   TOLL_STD             VARCHAR2(20),
   TOLL_DES             VARCHAR2(254),
   TOLL_WAY             VARCHAR2(20),
   PAYMENT              VARCHAR2(20),
   REMARK               VARCHAR2(30),
   SOURCE               VARCHAR2(10),
   OPEN_TIIME           VARCHAR2(254),
   TOTAL_NUM            NUMBER(10)                     default 0 not null,
   WORK_TIME            VARCHAR2(20),
   ACCESS_TYPE          NUMBER(1)                      default 2 not null
       check (ACCESS_TYPE in (0,1,2,3)) disable,
   RES_HIGH             NUMBER(5,2)                    default 0 not null,
   RES_WIDTH            NUMBER(5,2)                    default 0 not null,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   CERTIFICATE          NUMBER(1)                      default 0 not null
       check (CERTIFICATE in (0,1,2,3)) disable,
   MECHANICAL_GARAGE    NUMBER(1)                      default 0 not null
       check (MECHANICAL_GARAGE in (0,1,2,3)) disable,
   VEHICLE              NUMBER(1)                      default 0 not null
       check (VEHICLE in (0,1,2,3)) disable,
   PHOTO_NAME           VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_PARKING primary key (PARKING_ID)
);

/*==============================================================*/
/* Table: IX_POI_TOURROUTE                                      */
/*==============================================================*/
create table IX_POI_TOURROUTE  (
   TOUR_ID              NUMBER(10)                      not null,
   TOUR_NAME            VARCHAR2(100),
   TOUR_NAME_ENG        VARCHAR2(100),
   TOUR_INTR            VARCHAR2(254),
   TOUR_INTR_ENG        VARCHAR2(254),
   TOUR_TYPE            VARCHAR2(254),
   TOUR_TYPE_ENG        VARCHAR2(254),
   TOUR_X               NUMBER(8,5)                    default 0 not null,
   TOUR_Y               NUMBER(8,5)                    default 0 not null,
   TOUR_LEN             NUMBER(15,3)                   default 0 not null,
   TRAIL_TIME           VARCHAR2(20),
   VISIT_TIME           VARCHAR2(20),
   POI_PID              VARCHAR2(254),
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_TOURROUTE primary key (TOUR_ID)
);
/*==============================================================*/
/* Table: IX_POI_EVENT                                          */
/*==============================================================*/
create table IX_POI_EVENT  (
   EVENT_ID             NUMBER(10)                      not null,
   EVENT_NAME           VARCHAR2(100),
   EVENT_NAME_ENG       VARCHAR2(100),
   EVENT_KIND           VARCHAR2(32),
   EVENT_KIND_ENG       VARCHAR2(100),
   EVENT_DESC           VARCHAR2(100),
   EVENT_DESC_ENG       VARCHAR2(254),
   START_DATE           VARCHAR2(50),
   END_DATE             VARCHAR2(50),
   DETAIL_TIME          VARCHAR2(200),
   DETAIL_TIME_ENG      VARCHAR2(200),
   CITY                 VARCHAR2(50),
   POI_PID              VARCHAR2(25),
   PHOTO_NAME           VARCHAR2(254),
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_POI_EVENT primary key (EVENT_ID)
);
/*==============================================================*/
/* Table: AD_ADMIN_DETAIL                                       */
/*==============================================================*/
create table AD_ADMIN_DETAIL  (
   ADMIN_ID             NUMBER(6)                       not null,
   CITY_NAME            VARCHAR2(100),
   CITY_NAME_ENG        VARCHAR2(100),
   CITY_INTR            VARCHAR2(254),
   CITY_INTR_ENG        VARCHAR2(254),
   COUNTRY              VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   AUDIO_FILE           VARCHAR2(254),
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_AD_ADMIN_DETAIL primary key (ADMIN_ID)
);
/*==============================================================*/
/* Table: AU_IX_POI_PARKING                                     */
/*==============================================================*/
create table AU_IX_POI_PARKING  (
   AUDATA_ID            NUMBER(10)                      not null,
   PARKING_ID           NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   PARKING_TYPE         VARCHAR2(10),
   TOLL_STD             VARCHAR2(20),
   TOLL_DES             VARCHAR2(254),
   TOLL_WAY             VARCHAR2(20),
   OPEN_TIIME           VARCHAR2(254),
   TOTAL_NUM            NUMBER(10)                     default 0 not null,
   WORK_TIME            VARCHAR2(20),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   PAYMENT              VARCHAR2(20),
   REMARK               VARCHAR2(30),
   SOURCE               VARCHAR2(10),
   ACCESS_TYPE          NUMBER(1)                      default 2 not null
       check (ACCESS_TYPE in (0,1,2,3)) disable,
   RES_HIGH             NUMBER(5,2)                    default 0 not null,
   RES_WIDTH            NUMBER(5,2)                    default 0 not null,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   CERTIFICATE          NUMBER(1)                      default 0 not null
       check (CERTIFICATE in (0,1,2,3)) disable,
   MECHANICAL_GARAGE    NUMBER(1)                      default 0 not null
       check (MECHANICAL_GARAGE in (0,1,2,3)) disable,
   VEHICLE              NUMBER(1)                      default 0 not null
       check (VEHICLE in (0,1,2,3)) disable,
   PHOTO_NAME           VARCHAR2(100),
   constraint FK_AU_IX_PO_REFERENCE_AU_IX_PO foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);


/*==============================================================*/
/* Table: AU_IX_POI_GASSTATION                                  */
/*==============================================================*/
create table AU_IX_POI_GASSTATION  (
   AUDATA_ID            NUMBER(10)                      not null,
   GASSTATION_ID        NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   FUEL_TYPE            VARCHAR2(50),
   OIL_TYPE             VARCHAR2(50),
   EG_TYPE              VARCHAR2(50),
   MG_TYPE              VARCHAR2(50),
   SERVICE              VARCHAR2(20),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   SERVICE_PROV         VARCHAR2(2),
   PAYMENT              VARCHAR2(50),
   OPEN_HOUR            VARCHAR2(254),
   PHOTO_NAME           VARCHAR2(100),
   constraint FK_AUIX_POI_PARKING foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_CHARGINGSTATION                              */
/*==============================================================*/
create table AU_IX_POI_CHARGINGSTATION  (
   AUCHARG_ID           NUMBER(10)                      not null,
   AUDATA_ID            NUMBER(10)                      not null,
   CHARGING_ID          NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CHARGING_TYPE        NUMBER(2)                      default 2 not null
       check (CHARGING_TYPE in (0,1,2,3,4)) disable,
   CHARGING_NUM         VARCHAR2(5),
   EXCHANGE_NUM         VARCHAR2(5),
   PAYMENT              VARCHAR2(20),
   SERVICE_PROV         VARCHAR2(2),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   PARKING_NUM          VARCHAR2(30),
   "MODE"               VARCHAR2(10),
   PLUG_TYPE            VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(100),
   constraint PK_AU_IX_POI_CHARGINGSTATION primary key (AUCHARG_ID),
   constraint FK_AUIX_POI_CHARGINGSTATION foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_CHARGINGPLOT                                 */
/*==============================================================*/
create table AU_IX_POI_CHARGINGPLOT  (
   AUCHARG_ID           NUMBER(10)                      not null,
   CHARGING_ID          NUMBER(10)                     default 0 not null,
   GROUP_ID             NUMBER(5)                      default 1 not null,
   COUNT                NUMBER(5)                      default 1 not null,
   ACDC                 NUMBER(5)                      default 0 not null
       check (ACDC in (0,1)) disable,
   PLUG_TYPE            VARCHAR2(10),
   POWER                VARCHAR2(10),
   VOLTAGE              VARCHAR2(10),
   "CURRENT"            VARCHAR2(10),
   "MODE"               NUMBER(2)                      default 0 not null
       check ("MODE" in (0,1)) disable,
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   CHARGINGSTATION_FIELD_GUID VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)) disable,
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   constraint FK_AUIX_POI_CHARGINGPLOT foreign key (AUCHARG_ID)
         references AU_IX_POI_CHARGINGSTATION (AUCHARG_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POI_RP00                                        */
/*==============================================================*/
create table AU_IX_POI_RP00  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)) disable,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)) disable,
   PMESH_ID             NUMBER(6)                      default 0 not null,
   ADMIN_REAL           NUMBER(6)                      default 0 not null,
   IMPORTANCE           NUMBER(1)                      default 0 not null
       check (IMPORTANCE in (0,1)) disable,
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)                      default 0 not null
       check (ACCESS_FLAG in (0,1,2)) disable,
   OPEN_24H             NUMBER(1)                      default 0 not null
       check (OPEN_24H in (0,1,2)) disable,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POST_CODE            VARCHAR2(6),
   DIF_GROUPID          VARCHAR2(200),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)) disable,
   RESERVED             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(500),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)) disable,
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,9)) disable,
   EX_PRIORITY          VARCHAR2(10),
   EDITION_FLAG         VARCHAR2(12),
   POI_MEMO             VARCHAR2(200),
   OLD_BLOCKCODE        VARCHAR2(200),
   OLD_NAME             VARCHAR2(200),
   OLD_ADDRESS          VARCHAR2(200),
   OLD_KIND             VARCHAR2(8),
   POI_NUM              VARCHAR2(36),
   LOG                  VARCHAR2(200),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)) disable,
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)) disable,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   IMP_DATE             DATE,
   constraint PK_AU_IX_POI_RP00 primary key (AUDATA_ID)
);
/*==============================================================*/
/* Table: AU_IX_POI_NAME_RP00                                   */
/*==============================================================*/
create table AU_IX_POI_NAME_RP00  (
   AUNAME_ID            NUMBER(10)                      not null,
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,3,4,5,6,7,9)) disable,
   NAME_TYPE            NUMBER(2)                      default 1 not null
       check (NAME_TYPE in (1,2)),
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint PK_AU_IX_POI_NAME_RP00 primary key (AUNAME_ID),
   constraint AUIX_POI_NAMERP00 foreign key (AUDATA_ID)
         references AU_IX_POI_RP00 (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_FLAG                               */
/*==============================================================*/
create table AU_IX_POINTADDRESS_FLAG  (
   AUDATA_ID            NUMBER(10)                      not null,
   POINTADDRESS_PID     NUMBER(10)                     default 0 not null,
   FLAG_CODE            VARCHAR2(12),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)) disable,
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)) disable,
   constraint AU_IX_PADDRESSFLAG foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID) disable
);

/*==============================================================*/
/* Table: WL_CHECKLOG                               */
/*==============================================================*/
create table WL_CHECKLOG  (
   ITEM_ID            VARCHAR2(100)                      not null,
   PID     NUMBER(10)                     not null,
   TASK_ID          VARCHAR2(500),
   CHECK_TIME        VARCHAR2(100),
   IS_VIEWED        NUMBER(2)                      default 0 not null
       check (IS_VIEWED in (0,1)) disable,
   IS_MODIFIED      NUMBER(2)                      default 0 not null
       check (IS_MODIFIED in (0,1)) disable,
   MEMO  VARCHAR2(500)
);

/*==============================================================*/
/* Table: WL_CHECKCOUNT                               */
/*==============================================================*/
create table WL_CHECKCOUNT  (
   ITEM_ID            VARCHAR2(100)                      not null,
   TASK_ID          VARCHAR2(500),
   CHECK_TIME        VARCHAR2(100),
   CHECK_COUNT        NUMBER(10) default 0 ,
   VER_COUNT        NUMBER(10) default 0 ,
   NONVER_COUNT        NUMBER(10) default 0 ,
   MEMO  VARCHAR2(500)
);

/*==============================================================*/
/* Table: RD_HGWG_LIMIT                                         */
/*==============================================================*/
create table RD_HGWG_LIMIT  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   RES_HIGH             NUMBER(5,2)                    default 0 not null,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,       
   RES_AXLE_LOAD        NUMBER(5,2)                    default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_HGWG_LIMIT primary key (PID),
   constraint RDHGWG_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint RDHGWG_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)  disable
);

/*==============================================================*/
/* Table: RD_LINK_LIMIT_TRUCK                                   */
/*==============================================================*/
create table RD_LINK_LIMIT_TRUCK  (
   LINK_PID             NUMBER(10)                      not null,
   LIMIT_DIR            NUMBER(1)                      default 0 not null
       check (LIMIT_DIR in (0,1,2,3,9)) disable,
   TIME_DOMAIN          VARCHAR2(1000),
   RES_TRAILER          NUMBER(1)                      default 0 not null
       check (RES_TRAILER in (0,1)) disable,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   RES_AXLE_LOAD        NUMBER(5,2)                    default 0 not null,
   RES_AXLE_COUNT       NUMBER(2)                      default 0 not null,
   RES_OUT              NUMBER(1)                      default 0 not null
       check (RES_OUT in (0,1,2)) disable,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDLINK_LIMIT_TRUCK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_VARIABLE_SPEED_VIA                                 */
/*==============================================================*/
create table RD_VARIABLE_SPEED_VIA  (
   VSPEED_PID           NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDVARIABLESPEED_VIA foreign key (VSPEED_PID)
         references RD_VARIABLE_SPEED (VSPEED_PID) disable,
   constraint RDVARIABLESPEED_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: IX_NATGUD                                             */
/*==============================================================*/
create table IX_NATGUD  (
   PID                  NUMBER(10)                      not null,
   SOURCE               NUMBER(1)                      default 0 not null
       check (SOURCE in (0,1,2)),
   SOURCE_TYPE          NUMBER(1)                      default 0 not null
       check (SOURCE_TYPE in (0,1,2,3,4,5,6)),
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   POI_PID              NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_NATGUD primary key (PID)
);

/*==============================================================*/
/* Table: IX_NATGUD_NAME                                        */
/*==============================================================*/
create table IX_NATGUD_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 1 not null,
   NG_ASSO_PID          NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   DESCRIPTION          VARCHAR2(200),
   DESC_PHONETIC        VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_IX_NATGUD_NAME primary key (NAME_ID),
   constraint IXNATGUD_NAME foreign key (NG_ASSO_PID)
         references IX_NATGUD (PID) disable
);

/*==============================================================*/
/* Table: RD_NATGUD_JUN                                         */
/*==============================================================*/
create table RD_NATGUD_JUN  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NATGUD_JUN primary key (PID),
   constraint RDNATGUD_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint RDNATGUD_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);



/*==============================================================*/
/* Table: RD_NATGUD_JUN_DETAIL                                  */
/*==============================================================*/
create table RD_NATGUD_JUN_DETAIL  (
   DETAIL_ID            NUMBER(10)                      not null,
   NG_COND_PID          NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   NG_ASSO_PID          NUMBER(10)                      not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)) disable,
   EXP_LINK_PID         NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_RD_NATGUD_JUN_DETAIL primary key (DETAIL_ID),
   constraint RDNATGUD_DETAIL foreign key (NG_COND_PID)
         references RD_NATGUD_JUN (PID) disable,
   constraint RDNATGUD_IXNATGUD foreign key (NG_ASSO_PID)
         references IX_NATGUD (PID) disable,
   constraint RDNATGUD_DETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint RDNATGUD_DETAIL_EXPLINK foreign key (EXP_LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: RD_NATGUD_JUN_VIA                                     */
/*==============================================================*/
create table RD_NATGUD_JUN_VIA  (
   DETAIL_ID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint RDNATGUDVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_NATGUD_JUN_DETAIL (DETAIL_ID) disable,
   constraint RDNATGUD_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);
/*==============================================================*/
/* Table: HWY_JUNCTION                                          */
/*==============================================================*/
/*==============================================================*/
create table HWY_JUNCTION  (
   JUNC_PID             NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                     default 0 not null,
   ACCESS_TYPE          NUMBER(1)                      default 0 not null
       check (ACCESS_TYPE in (0,1,2)) disable,
   ATTR                 NUMBER(2)                      default 0 not null
       check (ATTR in (0,1,2,4,8)) disable,
   DIS_BETW             NUMBER(15,3)                   default 0 not null,
   SEQ_NUM              NUMBER(3)                      default 0 not null,
   HW_PID               NUMBER(10)                     default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HWY_JUNCTION primary key (JUNC_PID),
   constraint HWYJUNCTION_LINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint HWYJUNCTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);


/*==============================================================*/
/* Table: HWY_JUNCTION_NAME                                     */
/*==============================================================*/
create table HWY_JUNCTION_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   JUNC_PID             NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 1 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')) disable,
   NAME                 VARCHAR2(1000),
   PHONETIC             VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HWY_JUNCTION_NAME primary key (NAME_ID),
   constraint HWYJUNCTIONNAME_JUN foreign key (JUNC_PID)
         references HWY_JUNCTION (JUNC_PID) disable
);

/*==============================================================*/
/* Table: HWY_SAPA                                              */
/*==============================================================*/
create table HWY_SAPA  (
   JUNC_PID             NUMBER(10)                      not null,
   ESTAB_ITEM           VARCHAR2(200),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint HWYSAPA_JUN foreign key (JUNC_PID)
         references HWY_JUNCTION (JUNC_PID) disable
);

/*==============================================================*/
/* Table: HWY_JCT                                               */
/*==============================================================*/
create table HWY_JCT  (
   JCT_PID              NUMBER(10)                      not null,
   S_JUNC_PID           NUMBER(10)                      not null,
   E_JUNC_PID           NUMBER(10)                      not null,
   DIS_BETW             NUMBER(15,3)                   default 0 not null,
   ORIETATION           NUMBER(2)                      default 0 not null
       check (ORIETATION in (0,1,2,3,4)) disable,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint PK_HWY_JCT primary key (JCT_PID),
   constraint HWYJCT_JUN_S foreign key (S_JUNC_PID)
         references HWY_JUNCTION (JUNC_PID) disable,
   constraint HWYJCT_JUN_E foreign key (E_JUNC_PID)
         references HWY_JUNCTION (JUNC_PID) disable
);

/*==============================================================*/
/* Table: HWY_JCT_LINK                                          */
/*==============================================================*/
create table HWY_JCT_LINK  (
   JCT_PID              NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint HWYJCTLINK_JCT foreign key (JCT_PID)
         references HWY_JCT (JCT_PID) disable,
   constraint HWYJCTLINK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: ADAS_NODE_MESH                                        */
/*==============================================================*/
create table ADAS_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint ADASNODEMESH_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: ADAS_RDLINK_GEOMETRY_DTM                              */
/*==============================================================*/
create table ADAS_RDLINK_GEOMETRY_DTM  (
   LINK_PID             NUMBER(10)                      not null,
   SHP_SEQ_NUM          NUMBER(5)                      default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   BANKING              NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint FK_ADASRDLINKGEODTM_RDLINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: ADAS_RDNODE_INFO_DTM                                  */
/*==============================================================*/
create table ADAS_RDNODE_INFO_DTM  (
   NODE_PID             NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint FK_ADASRDNODEINFODTMIN_RDLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint FK_ADASRDNODEINFODTMOUT_RDLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint FK_ADASRDNODEINFODTM_RDNODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);


/*==============================================================*/
/* Table: ADAS_RDNODE_SLOPE_DTM                                 */
/*==============================================================*/
create table ADAS_RDNODE_SLOPE_DTM  (
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint FK_ADASRDNODESLOPEDTM_RDLINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable,
   constraint FK_ADASRDNODESLOPEDTM_RDNODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID) disable
);

/*==============================================================*/
/* Table: ADAS_ITPLINK_GEOMETRY                                 */
/*==============================================================*/
create table ADAS_ITPLINK_GEOMETRY  (
   LINK_PID             NUMBER(10)                      not null,
   SHP_SEQ_NUM          NUMBER(5)                      default 0 not null,
   IS_RDLINK_SHPT       NUMBER(1)                      default 0 not null
       check (IS_RDLINK_SHPT in (0,1,2)) disable,
   OFFSET               NUMBER(10,3)                   default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   BANKING              NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)) disable,
   U_FIELDS             VARCHAR2(1000),
   constraint FK_ADASITPLINKGEO_RDLINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID) disable
);

/*==============================================================*/
/* Table: AU_TASK_INFO                                          */
/*==============================================================*/
create table AU_TASK_INFO  (
   IMP_DATE             DATE,
   IMP_TASK_ID          NUMBER(10)                     default 0 not null,
   DATA_PATH            VARCHAR2(200),
   IMP_FILE             VARCHAR2(200),
   PROVINCE             VARCHAR2(200),
   CITY                 VARCHAR2(200),
   DISTRICT             VARCHAR2(200),
   REGION               VARCHAR2(200),
   DETAIL_REGION        VARCHAR2(200),
   JOB_NATURE           VARCHAR2(200),
   JOB_TYPE             VARCHAR2(200),
   JOB_DTAIL            VARCHAR2(200),
   DEPART               VARCHAR2(200),
   CATEGORY             VARCHAR2(200),
   MEMO                 VARCHAR2(4000),
   HAVE_ROADNAME        NUMBER(1)                      default 9 not null
       check (HAVE_ROADNAME in (0,1,9)) disable,
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,2)) disable,
   MATCH_ID             NUMBER(10)                     default 0 not null,
   MANAGER_ID           varchar2(200),
   EXT_FLAG             VARCHAR2(200),
   EXT_OBJ              VARCHAR2(200),
   EXT_TIME             VARCHAR2(200),
   IMP_TASK_NAME        VARCHAR2(400)
);

/*==============================================================*/
/* Table: AU_DATA_STATISTICS                                    */
/*==============================================================*/
create table AU_DATA_STATISTICS  (
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_CATEGORY        VARCHAR2(200 char),
   STATIS_TYPE          NUMBER(1)                      default 0 not null
       check (STATIS_TYPE in (0,1)) disable,
   MARK_TYPE            NUMBER(6)                      default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)) disable,
   DATA_COUNT           NUMBER(10)                     default 0 not null
);

/*==============================================================*/
/* Table: AU_LOG_STATISTICS                                     */
/*==============================================================*/
create table AU_LOG_STATISTICS  (
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_CATEGORY        VARCHAR2(200),
   LOG_CATEGORY         NUMBER(2)                      default 0 not null
       check (LOG_CATEGORY in (0,1,2,3,4,5,6)) disable,
   DATA_COUNT           NUMBER(10)                     default 0 not null
);
