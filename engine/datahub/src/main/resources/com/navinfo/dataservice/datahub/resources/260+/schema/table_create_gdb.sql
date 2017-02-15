/*==============================================================*/
/* DBMS name:      ORACLE Version 10g                           */
/* Created on:     2016-7-7 17:17:30                            */
/*==============================================================*/


--drop index IDX_DATA_LOG_OBJ;

--drop index IDX_DATA_LOG_VERSION;

--drop index IDX_RAW_COMMIT;

--drop index IDX_RAW_TASK;

/*==============================================================*/
/* Table: ACH_GDB_INFO                                          */
/*==============================================================*/
create table ACH_GDB_INFO  (
   ACH_GDB_ID           VARCHAR2(1000)                  not null,
   CREATE_TIME          DATE,
   SYS_INFO             VARCHAR2(1000),
   CREATE_PERSON        NUMBER(10)                     default 0,
   VER_NAME             VARCHAR2(1000),
   VER_NUM              VARCHAR2(1000)                 
       check (VER_NUM is null or (VER_NUM in ('0','1','2','3'))),
   SUB_VER_NUM          VARCHAR2(1000),
   PARENT_VER_NUM       VARCHAR2(1000),
   STATUS               VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   SUBMIT_PERSON        VARCHAR2(1000),
   TASK_RANGE           VARCHAR2(1000),
   TASK_DESCRIPT        VARCHAR2(1000),
   RECEIVE_PERSON       NUMBER(10)                     default 0,
   RECEIVE_TIME         DATE,
   MEMO                 VARCHAR2(1000),
   constraint PK_ACH_GDB_INFO primary key (ACH_GDB_ID)
);

comment on table ACH_GDB_INFO is
'[173A]';

comment on column ACH_GDB_INFO.ACH_GDB_ID is
'����';

comment on column ACH_GDB_INFO.SYS_INFO is
'��ݿ���,�û���,��ݿ�IP';

comment on column ACH_GDB_INFO.CREATE_PERSON is
'�ο�"BI_PERSON"';

comment on column ACH_GDB_INFO.VER_NAME is
'��:12Q1';

comment on column ACH_GDB_INFO.VER_NUM is
'��ݳɹ��汾���,��1.0��ʼ,���μ�1.��:1.0,2.0 ';

comment on column ACH_GDB_INFO.SUB_VER_NUM is
'�˹�����';

comment on column ACH_GDB_INFO.PARENT_VER_NUM is
'����ҵ�����ʱ,����Ϊ��';

comment on column ACH_GDB_INFO.STATUS is
'���,���';

comment on column ACH_GDB_INFO.ITEM is
'�û�����:��13CY,NIMIF-G,NAVEX,NIGDF-G,����';

comment on column ACH_GDB_INFO.SUBMIT_PERSON is
'�ο�"BI_PERSON"';

comment on column ACH_GDB_INFO.TASK_RANGE is
'�ο�"BI_TASK"';

comment on column ACH_GDB_INFO.TASK_DESCRIPT is
'�ο�"BI_TASK"';

comment on column ACH_GDB_INFO.RECEIVE_PERSON is
'�ο�"BI_PERSON"';

/*==============================================================*/
/* Table: RD_NODE                                               */
/*==============================================================*/
create table RD_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2,3)),
   GEOMETRY             SDO_GEOMETRY,
   ADAS_FLAG            NUMBER(1)                      default 2 not null
       check (ADAS_FLAG in (0,1,2)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 6 not null
       check (SRC_FLAG in (1,2,3,4,5,6)),
   DIGITAL_LEVEL        NUMBER(2)                      default 0 not null
       check (DIGITAL_LEVEL in (0,1,2,3,4)),
   RESERVED             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_NODE primary key (NODE_PID)
);

comment on column RD_NODE.NODE_PID is
'����';

comment on column RD_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column RD_NODE.ADAS_FLAG is
'��־�Ƿ����ADAS���';

comment on column RD_NODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column RD_NODE.DIF_GROUPID is
'[172A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column RD_NODE.SRC_FLAG is
'[180A]13CY';

comment on column RD_NODE.DIGITAL_LEVEL is
'[1802A]13CY';

comment on column RD_NODE.RESERVED is
'[1802A]';

comment on column RD_NODE.U_RECORD is
'�������±�ʶ';

comment on column RD_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK                                               */
/*==============================================================*/
create table RD_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 7 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,9,10,11,13,15)),
   DIRECT               NUMBER(1)                      default 1 not null
       check (DIRECT in (0,1,2,3)),
   APP_INFO             NUMBER(1)                      default 1 not null
       check (APP_INFO in (0,1,2,3,5)),
   TOLL_INFO            NUMBER(1)                      default 2 not null
       check (TOLL_INFO in (0,1,2,3)),
   ROUTE_ADOPT          NUMBER(1)                      default 2 not null
       check (ROUTE_ADOPT in (0,1,2,3,4,5,9)),
   MULTI_DIGITIZED      NUMBER(1)                      default 0 not null
       check (MULTI_DIGITIZED between 0 and 1 and MULTI_DIGITIZED in (0,1)),
   DEVELOP_STATE        NUMBER(1)                      default 0 not null
       check (DEVELOP_STATE between 0 and 2 and DEVELOP_STATE in (0,1,2)),
   IMI_CODE             NUMBER(1)                      default 0 not null
       check (IMI_CODE between 0 and 3 and IMI_CODE in (0,1,2,3)),
   SPECIAL_TRAFFIC      NUMBER(1)                      default 0 not null
       check (SPECIAL_TRAFFIC between 0 and 1 and SPECIAL_TRAFFIC in (0,1)),
   FUNCTION_CLASS       NUMBER(1)                      default 5 not null
       check (FUNCTION_CLASS between 0 and 5 and FUNCTION_CLASS in (0,1,2,3,4,5)),
   URBAN                NUMBER(1)                      default 0 not null
       check (URBAN between 0 and 1 and URBAN in (0,1)),
   PAVE_STATUS          NUMBER(1)                      default 0 not null
       check (PAVE_STATUS between 0 and 1 and PAVE_STATUS in (0,1)),
   LANE_NUM             NUMBER(2)                      default 2 not null,
   LANE_LEFT            NUMBER(2)                      default 0 not null,
   LANE_RIGHT           NUMBER(2)                      default 0 not null,
   LANE_WIDTH_LEFT      NUMBER(1)                      default 1 not null
       check (LANE_WIDTH_LEFT in (1,2,3)),
   LANE_WIDTH_RIGHT     NUMBER(1)                      default 1 not null
       check (LANE_WIDTH_RIGHT in (1,2,3)),
   LANE_CLASS           NUMBER(1)                      default 2 not null
       check (LANE_CLASS between 0 and 3 and LANE_CLASS in (0,1,2,3)),
   WIDTH                NUMBER(8)                      default 0 not null,
   IS_VIADUCT           NUMBER(1)                      default 0 not null
       check (IS_VIADUCT between 0 and 2 and IS_VIADUCT in (0,1,2)),
   LEFT_REGION_ID       NUMBER(10)                     default 0 not null,
   RIGHT_REGION_ID      NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   ONEWAY_MARK          NUMBER(2)                      default 0 not null
       check (ONEWAY_MARK in (0,1)),
   MESH_ID              NUMBER(6)                      default 0 not null,
   STREET_LIGHT         NUMBER(1)                      default 0 not null
       check (STREET_LIGHT between 0 and 2 and STREET_LIGHT in (0,1,2)),
   PARKING_LOT          NUMBER(1)                      default 0 not null
       check (PARKING_LOT in (0,1,2)),
   ADAS_FLAG            NUMBER(1)                      default 0 not null
       check (ADAS_FLAG in (0,1,2)),
   SIDEWALK_FLAG        NUMBER(1)                      default 0 not null
       check (SIDEWALK_FLAG in (0,1,2)),
   WALKSTAIR_FLAG       NUMBER(1)                      default 0 not null
       check (WALKSTAIR_FLAG in (0,1,2)),
   DICI_TYPE            NUMBER(1)                      default 0 not null
       check (DICI_TYPE in (0,1,2)),
   WALK_FLAG            NUMBER(1)                      default 0 not null
       check (WALK_FLAG in (0,1,2)),
   DIF_GROUPID          VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 6 not null
       check (SRC_FLAG in (1,2,3,4,5,6)),
   DIGITAL_LEVEL        NUMBER(2)                      default 0 not null
       check (DIGITAL_LEVEL in (0,1,2,3,4)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   TRUCK_FLAG           NUMBER(1)                      default 0 not null
       check (TRUCK_FLAG in (0,1)),
   FEE_STD              NUMBER(5,2)                    default 0 not null,
   FEE_FLAG             NUMBER(1)                      default 0 not null
       check (FEE_FLAG in (0,1,2)),
   SYSTEM_ID            NUMBER(6)                      default 0 not null,
   ORIGIN_LINK_PID      NUMBER(10)                     default 0 not null,
   CENTER_DIVIDER       NUMBER(2)                      default 0 not null
       check (CENTER_DIVIDER in (0,10,11,12,13,20,21,30,31,40,50,51,60,61,62,63,99)),
   PARKING_FLAG         NUMBER(1)                      default 0 not null
       check (PARKING_FLAG in (0,1)),
   ADAS_MEMO            NUMBER(5)                      default 0 not null,
   MEMO                 VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_LINK primary key (LINK_PID),
   constraint RDLINK_SNODE foreign key (S_NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDLINK_ENODE foreign key (E_NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_LINK is
'��·Link�ǹ��ɵ�·�Ļ�Ԫ��,������·�ļ�����״,���˹�ϵ,�Լ���ĵ�·������Ϣ,���·���,��·�ֱ�,·������,��·����.';

comment on column RD_LINK.LINK_PID is
'����';

comment on column RD_LINK.S_NODE_PID is
'���,����"RD_NODE"';

comment on column RD_LINK.E_NODE_PID is
'���,����"RD_NODE"';

comment on column RD_LINK.KIND is
'[180U]�洢Ϊ10��������,NaviMap��ʾΪ16������ʽ
';

comment on column RD_LINK.DIRECT is
'���������ڵ�·�ϵ�ͨ�з���,�������Link�����˳��������ʾ';

comment on column RD_LINK.APP_INFO is
'[180U]������·ͨ����Ϣ';

comment on column RD_LINK.FUNCTION_CLASS is
'[180U]';

comment on column RD_LINK.URBAN is
'�Ƿ�Ϊ���е�·';

comment on column RD_LINK.LANE_NUM is
'[180U](1)�������·:ֻ��¼"�ܳ�����"
(2)˫�����·:
������ҳ�����һ��, ֻ��¼"�ܳ�����"
������ҳ�����һ��,��ֱ��¼"��/�ҳ�����"';

comment on column RD_LINK.LANE_WIDTH_LEFT is
'[200]��άclm[210]�޸��ֶκ���';

comment on column RD_LINK.LANE_WIDTH_RIGHT is
'[200]��άclm[210]�޸��ֶκ���';

comment on column RD_LINK.LANE_CLASS is
'[180U]';

comment on column RD_LINK.WIDTH is
'[180U]';

comment on column RD_LINK.LEFT_REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ����������Ҷ�Ӧ����������������������';

comment on column RD_LINK.RIGHT_REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ����������Ҷ�Ӧ����������������������';

comment on column RD_LINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column RD_LINK.LENGTH is
'��λ:��';

comment on column RD_LINK.ONEWAY_MARK is
'[181A]';

comment on column RD_LINK.STREET_LIGHT is
'��·�Ƿ����·��֮���������ʩ';

comment on column RD_LINK.PARKING_LOT is
'��·�Ƿ����ͣ�����ͣ��λ';

comment on column RD_LINK.ADAS_FLAG is
'��־�Ƿ����ADAS���
[190]����2:��';

comment on column RD_LINK.SIDEWALK_FLAG is
'ע:�����ֵΪ2 ʱ,��RD_LINK_SIDEWALK �м�¼��ϸ�����Ϣ';

comment on column RD_LINK.WALKSTAIR_FLAG is
'ע: ���ֵΪ2 ʱ,��RD_LINK_WALKSTAIR �м�¼��ϸ������Ϣ';

comment on column RD_LINK.DICI_TYPE is
'[180U]ȫҪ�ػ�򻯰�';

comment on column RD_LINK.WALK_FLAG is
'[180U]������ֹ����ͨ��';

comment on column RD_LINK.DIF_GROUPID is
'[172A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column RD_LINK.SRC_FLAG is
'[180A]13CY';

comment on column RD_LINK.DIGITAL_LEVEL is
'[1802A]13CY';

comment on column RD_LINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column RD_LINK.TRUCK_FLAG is
'[200]������ͼ�����Ƿ�����֤';

comment on column RD_LINK.MEMO is
'[171A]��¼�����Դ(�ο�Ӱ����»������ݵ�)�Լ�����GDB ��ʱ���汾';

comment on column RD_LINK.RESERVED is
'[1802A]';

comment on column RD_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ADAS_ITPLINK_GEOMETRY                                 */
/*==============================================================*/
create table ADAS_ITPLINK_GEOMETRY  (
   LINK_PID             NUMBER(10)                      not null,
   SHP_SEQ_NUM          NUMBER(5)                      default 0 not null,
   IS_RDLINK_SHPT       NUMBER(1)                      default 0 not null
       check (IS_RDLINK_SHPT in (0,1,2)),
   OFFSET               NUMBER(10,3)                   default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   BANKING              NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint FK_ADASITPLINKGEO_RDLINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table ADAS_ITPLINK_GEOMETRY is
'[210]';

comment on column ADAS_ITPLINK_GEOMETRY.OFFSET is
'��λ����';

comment on column ADAS_ITPLINK_GEOMETRY.GEOMETRY is
'�洢��"��"Ϊ��λ��5λ��γ�����
���(RD_LINK ��S_NODE_PID)���յ�(RD_LINK ��E_NODE_PID)�����Ϊ��״�����洢�����¶Ⱥ����ʺ�����Ϣ�ڱ�����Ч������ADAS_NODE��Ĺ�����ϵ�ο�ADAS_SLOPE���ADAS_NODE_INFO���ȡ
';

comment on column ADAS_ITPLINK_GEOMETRY.Z_VALUE is
'��λ:��';

comment on column ADAS_ITPLINK_GEOMETRY.HEADING is
'��λ:��,ֵ��:[0,360]';

comment on column ADAS_ITPLINK_GEOMETRY.CURVATURE is
'��λ:1/��,ֵ��:[-1,1]';

comment on column ADAS_ITPLINK_GEOMETRY.SLOPE is
'��λ:��,ֵ��:[-90,90]
���յ���ЧֵΪ-999999';

comment on column ADAS_ITPLINK_GEOMETRY.BANKING is
'��λ:��,ֵ��:[-90,90]';

/*==============================================================*/
/* Table: ADAS_NODE                                             */
/*==============================================================*/
create table ADAS_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   RDNODE_PID           NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   FORM_OF_WAY          NUMBER(2)                      default 1 not null
       check (FORM_OF_WAY in (1,2)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_ADAS_NODE primary key (NODE_PID)
);

comment on table ADAS_NODE is
'[170]';

comment on column ADAS_NODE.NODE_PID is
'[173U]����';

comment on column ADAS_NODE.RDNODE_PID is
'[173U]�ο�"RD_NODE"
[260]���ڶ��NODE_PID��Ӧͬһ��RDNODE_PID�����';

comment on column ADAS_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ��7 λ��γ�����
Ϊ��x��y��z������ά���';

comment on column ADAS_NODE.Z_VALUE is
'��λ:��';

comment on column ADAS_NODE.U_RECORD is
'�������±�ʶ';

comment on column ADAS_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (EDIT_FLAG in (0,1)),
   SRC_FLAG             NUMBER(1)                      default 1 not null
       check (SRC_FLAG in (1,2)),
   SOURCE               NUMBER(1)                      default 0 not null
       check (SOURCE in (0,1,2,3,4,5)),
   DEVICE               NUMBER(1)                      default 0 not null
       check (DEVICE in (0,1,2,3,4,5)),
   LENGTH               NUMBER(15,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_ADAS_LINK primary key (LINK_PID),
   constraint ADASLINK_SNODE foreign key (S_NODE_PID)
         references ADAS_NODE (NODE_PID),
   constraint ADASLINK_ENODE foreign key (E_NODE_PID)
         references ADAS_NODE (NODE_PID)
);

comment on table ADAS_LINK is
'[170]';

comment on column ADAS_LINK.LINK_PID is
'[173U]����';

comment on column ADAS_LINK.S_NODE_PID is
'[173U]���,����"ADAS_NODE"';

comment on column ADAS_LINK.E_NODE_PID is
'[173U]���,����"ADAS_NODE"';

comment on column ADAS_LINK.GEOMETRY is
'[171A](1)�洢��"��"Ϊ��λ��7 λ��γ���������
(2)Ϊ��x��y��z������ά���
(3)���(S_ADAS_NODEID) ���յ�(E_ADAS_NODEID)�����Ϊ��״�����洢';

comment on column ADAS_LINK.RDLINK_PID is
'[173U]�ο�"RD_LINK"';

comment on column ADAS_LINK.MESH_ID is
'[171A]';

comment on column ADAS_LINK.U_RECORD is
'�������±�ʶ';

comment on column ADAS_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ADAS_LINK_GEOMETRY                                    */
/*==============================================================*/
create table ADAS_LINK_GEOMETRY  (
   LINK_PID             NUMBER(10)                      not null,
   SHP_SEQ_NUM          NUMBER(5)                      default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   BANKING              NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADASLINKGEOMETRY_ADASLINK foreign key (LINK_PID)
         references ADAS_LINK (LINK_PID)
);

comment on table ADAS_LINK_GEOMETRY is
'[170]';

comment on column ADAS_LINK_GEOMETRY.LINK_PID is
'���,����"ADAS_LINK"';

comment on column ADAS_LINK_GEOMETRY.GEOMETRY is
'(1)�洢��"��"Ϊ��λ��7λ��γ�����
(2)���(S_ADAS_NODEID)���յ�(E_ADAS_NODEID)�����Ϊ��״�����洢�����¶Ⱥ����ʺ�����Ϣ�ڱ�����Ч����ο�ADAS_SLOPE���ADAS_NODE_INFO��[210]';

comment on column ADAS_LINK_GEOMETRY.Z_VALUE is
'[171U]��λ:��';

comment on column ADAS_LINK_GEOMETRY.HEADING is
'��λ:��,ֵ��:[0,360]';

comment on column ADAS_LINK_GEOMETRY.CURVATURE is
'[210]�޸ĵ�λ.
��λ:1/��,ֵ��:[-1,1]';

comment on column ADAS_LINK_GEOMETRY.SLOPE is
'��λ:��,ֵ��:[-90,90]';

comment on column ADAS_LINK_GEOMETRY.BANKING is
'��λ:��,ֵ��:[-90,90]';

comment on column ADAS_LINK_GEOMETRY.U_RECORD is
'�������±�ʶ';

comment on column ADAS_LINK_GEOMETRY.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ADAS_NODE_INFO                                        */
/*==============================================================*/
create table ADAS_NODE_INFO  (
   NODE_PID             NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADASNODEINFO_INLINK foreign key (IN_LINK_PID)
         references ADAS_LINK (LINK_PID),
   constraint ADASNODEINFO_OUTLINK foreign key (OUT_LINK_PID)
         references ADAS_LINK (LINK_PID),
   constraint ADASNODEINFO_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID)
);

comment on table ADAS_NODE_INFO is
'[170]';

comment on column ADAS_NODE_INFO.NODE_PID is
'[171U][173U]���,����"ADAS_NODE"';

comment on column ADAS_NODE_INFO.IN_LINK_PID is
'[171U][173U]���,����"ADAS_LINK"';

comment on column ADAS_NODE_INFO.OUT_LINK_PID is
'[171U][173U]���,����"ADAS_LINK"';

comment on column ADAS_NODE_INFO.HEADING is
'��λ:��,ֵ��:[0,360]';

comment on column ADAS_NODE_INFO.CURVATURE is
'[210]�޸ĵ�λ.
��λ:1/��,ֵ��:[-1,1]';

comment on column ADAS_NODE_INFO.U_RECORD is
'�������±�ʶ';

comment on column ADAS_NODE_INFO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ADAS_NODE_MESH                                        */
/*==============================================================*/
create table ADAS_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADASNODEMESH_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID)
);

/*==============================================================*/
/* Table: ADAS_RDLINK_GEOMETRY_DTM                              */
/*==============================================================*/
create table ADAS_RDLINK_GEOMETRY_DTM  (
   LINK_PID             NUMBER(10)                      not null,
   SHP_SEQ_NUM          NUMBER(5)                      default 0 not null,
   IS_RDLINK_SHPT       NUMBER(1)                      default 0 not null
       check (IS_RDLINK_SHPT in (0,1,2)),
   OFFSET               NUMBER(10,3)                   default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   Z_VALUE              NUMBER(10,3)                   default -9999 not null,
   HEADING              NUMBER(10,3)                   default 0 not null,
   CURVATURE            NUMBER(10,6)                   default 0 not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   BANKING              NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint FK_ADASRDLINKGEODTM_RDLINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table ADAS_RDLINK_GEOMETRY_DTM is
'[210]';

comment on column ADAS_RDLINK_GEOMETRY_DTM.OFFSET is
'��λ����';

comment on column ADAS_RDLINK_GEOMETRY_DTM.GEOMETRY is
'(1)�洢��"��"Ϊ��λ��7λ��γ�����
(2)���(S_ADAS_NODEID)���յ�(E_ADAS_NODEID)�����Ϊ��״�����洢�����¶Ⱥ����ʺ�����Ϣ�ڱ�����Ч����ο�ADAS_SLOPE���ADAS_NODE_INFO��';

comment on column ADAS_RDLINK_GEOMETRY_DTM.Z_VALUE is
'[171U]��λ:��';

comment on column ADAS_RDLINK_GEOMETRY_DTM.HEADING is
'��λ:��,ֵ��:[0,360]
���յ���ЧֵΪ-9999999';

comment on column ADAS_RDLINK_GEOMETRY_DTM.CURVATURE is
'��λ:1/��,ֵ��:[-1,1]
���յ���ЧֵΪ-9999';

comment on column ADAS_RDLINK_GEOMETRY_DTM.SLOPE is
'��λ:��,ֵ��:[-90,90]';

comment on column ADAS_RDLINK_GEOMETRY_DTM.BANKING is
'��λ:��,ֵ��:[-90,90]';

comment on column ADAS_RDLINK_GEOMETRY_DTM.U_RECORD is
'�������±�ʶ';

comment on column ADAS_RDLINK_GEOMETRY_DTM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint FK_ADASRDNODEINFODTMIN_RDLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint FK_ADASRDNODEINFODTMOUT_RDLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint FK_ADASRDNODEINFODTM_RDNODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table ADAS_RDNODE_INFO_DTM is
'[210]';

comment on column ADAS_RDNODE_INFO_DTM.HEADING is
'��λ:��,ֵ��:[0,360]';

comment on column ADAS_RDNODE_INFO_DTM.CURVATURE is
'[210]�޸ĵ�λ.
��λ:1/��,ֵ��:[-1,1]';

comment on column ADAS_RDNODE_INFO_DTM.U_RECORD is
'�������±�ʶ';

comment on column ADAS_RDNODE_INFO_DTM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ADAS_RDNODE_SLOPE_DTM                                 */
/*==============================================================*/
create table ADAS_RDNODE_SLOPE_DTM  (
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint FK_ADASRDNODESLOPEDTM_RDLINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint FK_ADASRDNODESLOPEDTM_RDNODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column ADAS_RDNODE_SLOPE_DTM.SLOPE is
'��λ:��, ֵ��:[-90,90]';

comment on column ADAS_RDNODE_SLOPE_DTM.U_RECORD is
'�������±�ʶ';

comment on column ADAS_RDNODE_SLOPE_DTM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ADAS_SLOPE                                            */
/*==============================================================*/
create table ADAS_SLOPE  (
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SLOPE                NUMBER(10,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADASSLOPE_NODE foreign key (NODE_PID)
         references ADAS_NODE (NODE_PID),
   constraint ADASSLOPE_LINK foreign key (LINK_PID)
         references ADAS_LINK (LINK_PID)
);

comment on table ADAS_SLOPE is
'[170]';

comment on column ADAS_SLOPE.NODE_PID is
'[173U]���,����"ADAS_NODE"';

comment on column ADAS_SLOPE.LINK_PID is
'[173U]���,����"ADAS_LINK"';

comment on column ADAS_SLOPE.SLOPE is
'��λ:��, ֵ��:[-90,90]';

comment on column ADAS_SLOPE.U_RECORD is
'�������±�ʶ';

comment on column ADAS_SLOPE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_ADMIN                                              */
/*==============================================================*/
create table AD_ADMIN  (
   REGION_ID            NUMBER(10)                      not null,
   ADMIN_ID             NUMBER(6)                      default 0 not null,
   EXTEND_ID            NUMBER(4)                      default 0 not null,
   ADMIN_TYPE           NUMBER(3,1)                    default 0 not null
       check (ADMIN_TYPE in (0,1,2,2.5,3,3.5,4,4.5,4.8,5,6,7,8,9)),
   CAPITAL              NUMBER(1)                      default 0 not null
       check (CAPITAL in (0,1,2,3)),
   POPULATION           VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   LINK_PID             NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)),
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   JIS_CODE             NUMBER(5)                      default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_ADMIN primary key (REGION_ID)
);

comment on column AD_ADMIN.REGION_ID is
'����';

comment on column AD_ADMIN.ADMIN_TYPE is
'[181U]��Ϊ��ҵ���,ʡ��,�ؼ��м�,��/�ؼ�������';

comment on column AD_ADMIN.CAPITAL is
'ע:TYPE=2 ��2.5 ��¼Ϊʡ��/ֱϽ��,TYPE=0��¼Ϊ�׶�,����Ϊδ����';

comment on column AD_ADMIN.POPULATION is
'��λ:����';

comment on column AD_ADMIN.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AD_ADMIN.LINK_PID is
'�ο�"RD_LINK"';

comment on column AD_ADMIN.NAME_GROUPID is
'�ο�"RD_NAME"';

comment on column AD_ADMIN.ROAD_FLAG is
'[170]';

comment on column AD_ADMIN.PMESH_ID is
'[171A]ÿ����ҵ��POI �ڳɹ���е�һ����LINK ������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column AD_ADMIN.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AD_ADMIN.MEMO is
'[173A]';

comment on column AD_ADMIN.U_RECORD is
'�������±�ʶ';

comment on column AD_ADMIN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_ADMIN_DETAIL primary key (ADMIN_ID)
);

comment on column AD_ADMIN_DETAIL.ADMIN_ID is
'����,ͬAD_ADMIN �С��������롱';

comment on column AD_ADMIN_DETAIL.COUNTRY is
'������������ǹ�Ҽ���,ֵΪ��;�����
ֵΪ��Ҵ�������';

comment on column AD_ADMIN_DETAIL.PHOTO_NAME is
'�����Ƭʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column AD_ADMIN_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column AD_ADMIN_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_ADMIN_GROUP                                        */
/*==============================================================*/
create table AD_ADMIN_GROUP  (
   GROUP_ID             NUMBER(10)                      not null,
   REGION_ID_UP         NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_ADMIN_GROUP primary key (GROUP_ID),
   constraint ADADMIN_UPLEVEL foreign key (REGION_ID_UP)
         references AD_ADMIN (REGION_ID)
);

comment on column AD_ADMIN_GROUP.GROUP_ID is
'����';

comment on column AD_ADMIN_GROUP.REGION_ID_UP is
'���,����"AD_ADMIN"';

comment on column AD_ADMIN_GROUP.U_RECORD is
'�������±�ʶ';

comment on column AD_ADMIN_GROUP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_ADMIN_NAME                                         */
/*==============================================================*/
create table AD_ADMIN_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2,3,4)),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4,5,6)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_ADMIN_NAME primary key (NAME_ID),
   constraint ADADMIN_NAMES foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID)
);

comment on column AD_ADMIN_NAME.NAME_ID is
'[170]����';

comment on column AD_ADMIN_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column AD_ADMIN_NAME.REGION_ID is
'���,����"AD_ADMIN"';

comment on column AD_ADMIN_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AD_ADMIN_NAME.NAME_CLASS is
'[170][172U]';

comment on column AD_ADMIN_NAME.NAME is
'[172A]';

comment on column AD_ADMIN_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column AD_ADMIN_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ
ע:
(1)AOIZone ȡֵ0~6
(2)GCZone ȡֵ:0
(3)����ȡֵ:0~1';

comment on column AD_ADMIN_NAME.U_RECORD is
'�������±�ʶ';

comment on column AD_ADMIN_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_ADMIN_NAME_TONE                                    */
/*==============================================================*/
create table AD_ADMIN_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADADMINNAME_TONE foreign key (NAME_ID)
         references AD_ADMIN_NAME (NAME_ID)
);

comment on table AD_ADMIN_NAME_TONE is
'[170]';

comment on column AD_ADMIN_NAME_TONE.NAME_ID is
'���,����"AD_ADMIN_NAME"';

comment on column AD_ADMIN_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column AD_ADMIN_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column AD_ADMIN_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column AD_ADMIN_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column AD_ADMIN_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column AD_ADMIN_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column AD_ADMIN_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_ADMIN_PART                                         */
/*==============================================================*/
create table AD_ADMIN_PART  (
   GROUP_ID             NUMBER(10)                      not null,
   REGION_ID_DOWN       NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADADMIN_DOWNLEVEL foreign key (REGION_ID_DOWN)
         references AD_ADMIN (REGION_ID),
   constraint ADADMIN_UPDOWN foreign key (GROUP_ID)
         references AD_ADMIN_GROUP (GROUP_ID)
);

comment on column AD_ADMIN_PART.GROUP_ID is
'���,����"AD_ADMIN_GROUP"';

comment on column AD_ADMIN_PART.REGION_ID_DOWN is
'���,����"AD_ADMIN"';

comment on column AD_ADMIN_PART.U_RECORD is
'�������±�ʶ';

comment on column AD_ADMIN_PART.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_FACE                                               */
/*==============================================================*/
create table AD_FACE  (
   FACE_PID             NUMBER(10)                      not null,
   REGION_ID            NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_FACE primary key (FACE_PID),
   constraint ADADMIN_ADFACE foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID)
);

comment on column AD_FACE.FACE_PID is
'����';

comment on column AD_FACE.REGION_ID is
'[170]���,����"AD_ADMIN"';

comment on column AD_FACE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column AD_FACE.AREA is
'��λ:ƽ����';

comment on column AD_FACE.PERIMETER is
'��λ:��';

comment on column AD_FACE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AD_FACE.U_RECORD is
'�������±�ʶ';

comment on column AD_FACE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_NODE                                               */
/*==============================================================*/
create table AD_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_NODE primary key (NODE_PID)
);

comment on column AD_NODE.NODE_PID is
'����';

comment on column AD_NODE.KIND is
'ƽ�潻���,������߽��';

comment on column AD_NODE.FORM is
'ͼ����,�ǵ�';

comment on column AD_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AD_NODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AD_NODE.U_RECORD is
'�������±�ʶ';

comment on column AD_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_LINK                                               */
/*==============================================================*/
create table AD_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 1 not null
       check (KIND in (0,1,2,3,4,5,6,7)),
   FORM                 NUMBER(1)                      default 1 not null
       check (FORM in (0,1,2,6,7,8,9)),
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_LINK primary key (LINK_PID),
   constraint ADLINK_SNODE foreign key (S_NODE_PID)
         references AD_NODE (NODE_PID),
   constraint ADLINK_ENODE foreign key (E_NODE_PID)
         references AD_NODE (NODE_PID)
);

comment on table AD_LINK is
'��������߽�(����ʡ/ֱϽ�б߽�,���������,���ر߽�,����߽�,��߽�,���)֮�乲��ʱ,Link�ֱ�ȡ�ߵȼ��߽�,��ʡ�����н繲Linkʱ,Link�ֱ�Ϊ"ʡ��"';

comment on column AD_LINK.LINK_PID is
'����';

comment on column AD_LINK.S_NODE_PID is
'���,����"AD_NODE"';

comment on column AD_LINK.E_NODE_PID is
'���,����"AD_NODE"';

comment on column AD_LINK.KIND is
'ע:
(1)2.5 �����:0~5
(2)�������:1,6,7';

comment on column AD_LINK.FORM is
'ע:
(1)2.5 �����:0~1
(2)�������:0~2,6~9';

comment on column AD_LINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column AD_LINK.LENGTH is
'��λ:��';

comment on column AD_LINK.SCALE is
'ע:���ֶν�����2.5 �����,������ݲ���Ҫ';

comment on column AD_LINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AD_LINK.U_RECORD is
'�������±�ʶ';

comment on column AD_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_FACE_TOPO                                          */
/*==============================================================*/
create table AD_FACE_TOPO  (
   FACE_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADFACE_LINK foreign key (LINK_PID)
         references AD_LINK (LINK_PID),
   constraint ADFACE_LINKS foreign key (FACE_PID)
         references AD_FACE (FACE_PID)
);

comment on table AD_FACE_TOPO is
'��¼����Face��Link���б�,������ʱ�뷽��洢';

comment on column AD_FACE_TOPO.FACE_PID is
'���,����"AD_FACE"';

comment on column AD_FACE_TOPO.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column AD_FACE_TOPO.LINK_PID is
'���,����"AD_LINK"';

comment on column AD_FACE_TOPO.U_RECORD is
'�������±�ʶ';

comment on column AD_FACE_TOPO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_NODE_100W                                          */
/*==============================================================*/
create table AD_NODE_100W  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_NODE_100W primary key (NODE_PID)
);

comment on column AD_NODE_100W.NODE_PID is
'����';

comment on column AD_NODE_100W.KIND is
'ƽ�潻���,������߽��';

comment on column AD_NODE_100W.FORM is
'ͼ����,�ǵ�';

comment on column AD_NODE_100W.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AD_NODE_100W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AD_NODE_100W.U_RECORD is
'�������±�ʶ';

comment on column AD_NODE_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_LINK_100W                                          */
/*==============================================================*/
create table AD_LINK_100W  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 1 not null
       check (KIND in (0,1,2,3,4,5,6,7)),
   FORM                 NUMBER(1)                      default 1 not null
       check (FORM in (0,1,2,6,7,8,9)),
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_LINK_100W primary key (LINK_PID),
   constraint ADLINK_SNODE_100W foreign key (S_NODE_PID)
         references AD_NODE_100W (NODE_PID),
   constraint ADLINK_ENODE_100W foreign key (E_NODE_PID)
         references AD_NODE_100W (NODE_PID)
);

comment on table AD_LINK_100W is
'��������߽�(����ʡ/ֱϽ�б߽�,���������,���ر߽�,����߽�,��߽�,���)֮�乲��ʱ,Link�ֱ�ȡ�ߵȼ��߽�,��ʡ�����н繲Linkʱ,Link�ֱ�Ϊ"ʡ��"';

comment on column AD_LINK_100W.LINK_PID is
'����';

comment on column AD_LINK_100W.S_NODE_PID is
'���,����"AD_NODE_100W"';

comment on column AD_LINK_100W.E_NODE_PID is
'���,����"AD_NODE_100W"';

comment on column AD_LINK_100W.KIND is
'ע:
(1)2.5 �����:0~5
(2)�������:1,6,7';

comment on column AD_LINK_100W.FORM is
'ע:
(1)2.5 �����:0~1
(2)�������:0~2,6~9';

comment on column AD_LINK_100W.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column AD_LINK_100W.LENGTH is
'��λ:��';

comment on column AD_LINK_100W.SCALE is
'ע:���ֶν�����2.5 �����,������ݲ���Ҫ';

comment on column AD_LINK_100W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AD_LINK_100W.U_RECORD is
'�������±�ʶ';

comment on column AD_LINK_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_LINK_MESH                                          */
/*==============================================================*/
create table AD_LINK_MESH  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADLINK_MESH foreign key (LINK_PID)
         references AD_LINK (LINK_PID)
);

comment on column AD_LINK_MESH.LINK_PID is
'���,����"AD_LINK"';

comment on column AD_LINK_MESH.U_RECORD is
'�������±�ʶ';

comment on column AD_LINK_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_LINK_MESH_100W                                     */
/*==============================================================*/
create table AD_LINK_MESH_100W  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADLINK_MESH_100W foreign key (LINK_PID)
         references AD_LINK_100W (LINK_PID)
);

comment on column AD_LINK_MESH_100W.LINK_PID is
'���,����"AD_LINK_100W"';

comment on column AD_LINK_MESH_100W.U_RECORD is
'�������±�ʶ';

comment on column AD_LINK_MESH_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_NODE_MESH                                          */
/*==============================================================*/
create table AD_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADNODE_MESH foreign key (NODE_PID)
         references AD_NODE (NODE_PID)
);

comment on column AD_NODE_MESH.NODE_PID is
'���,����"AD_NODE"';

comment on column AD_NODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column AD_NODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: AD_NODE_MESH_100W                                     */
/*==============================================================*/
create table AD_NODE_MESH_100W  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ADNODE_MESH_100W foreign key (NODE_PID)
         references AD_NODE_100W (NODE_PID)
);

comment on column AD_NODE_MESH_100W.NODE_PID is
'���,����"AD_NODE_100W"';

comment on column AD_NODE_MESH_100W.U_RECORD is
'�������±�ʶ';

comment on column AD_NODE_MESH_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (TYPE in (0,1)),
   LINK_PID             NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   MARK_ITEM            NUMBER(5)                      default 0 not null
       check (MARK_ITEM in (0,1,2)),
   PARAM_L              NUMBER(10)                     default 0 not null,
   PARAM_R              NUMBER(10)                     default 0 not null,
   PARAM_S              VARCHAR2(1000),
   PARAM_EX             VARCHAR2(1000),
   STATUS               NUMBER(1)                      default 0 not null
       check (STATUS in (0,1,2,3,4,5,6,7,8)),
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
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           CLOB,
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   ACCURACY             NUMBER(1)                      default 0 not null
       check (ACCURACY in (0,1)),
   constraint PK_AU_ADAS_MARK primary key (MARK_ID)
);

comment on table AU_ADAS_MARK is
'[171A]';

comment on column AU_ADAS_MARK.MARK_ID is
'����';

comment on column AU_ADAS_MARK.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_ADAS_MARK.ANGLE is
'���������ļн�,0~360��';

comment on column AU_ADAS_MARK.TYPE is
'����(Ĭ��ֵ)������,��ͼ,��Ƭ,��Ƶ,���ߵ�';

comment on column AU_ADAS_MARK.LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_ADAS_MARK.NODE_PID is
'�ο�"RD_NODE"';

comment on column AU_ADAS_MARK.DAY_TIME is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_ADAS_MARK.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_ADAS_MARK.PARAM_EX is
'���������';

comment on column AU_ADAS_MARK.TASK_ID is
'��¼��ҵ��������';

comment on column AU_ADAS_MARK.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_ADAS_MARK.FIELD_TASK_ID is
'��¼��ҵ��������';

/*==============================================================*/
/* Table: AU_ADAS_GPSRECORD                                     */
/*==============================================================*/
create table AU_ADAS_GPSRECORD  (
   GPSRECORD_ID         NUMBER(10)                      not null,
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(1)                      default 0 not null
       check (SOURCE in (0,1,2,3,4,5)),
   MESH_ID              NUMBER(6)                      default 0 not null,
   MEMO                 VARCHAR2(255),
   ACCURACY             NUMBER(1)                      default 0 not null
       check (ACCURACY in (0,1)),
   constraint PK_AU_ADAS_GPSRECORD primary key (GPSRECORD_ID),
   constraint AUADAS_MARK_GPSRECORD foreign key (MARK_ID)
         references AU_ADAS_MARK (MARK_ID)
);

comment on table AU_ADAS_GPSRECORD is
'[171A]';

comment on column AU_ADAS_GPSRECORD.GPSRECORD_ID is
'����';

comment on column AU_ADAS_GPSRECORD.MARK_ID is
'���,����"AU_MARK"';

comment on column AU_ADAS_GPSRECORD.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������';

/*==============================================================*/
/* Table: AU_ADAS_GPSTRACK                                      */
/*==============================================================*/
create table AU_ADAS_GPSTRACK  (
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
       check (FIELD_SOURCE in (0,1)),
   MESH_ID_2K           VARCHAR2(12),
   ACCURACY             NUMBER(1)                      default 0 not null
       check (ACCURACY in (0,1)),
   constraint PK_AU_ADAS_GPSTRACK primary key (GPSTRACK_ID)
);

comment on table AU_ADAS_GPSTRACK is
'[171A]';

comment on column AU_ADAS_GPSTRACK.GPSTRACK_ID is
'����';

comment on column AU_ADAS_GPSTRACK.GPS_TEXT is
'GPS�����ı���';

comment on column AU_ADAS_GPSTRACK.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_ADAS_GPSTRACK.TASK_ID is
'��¼��ҵ��������';

comment on column AU_ADAS_GPSTRACK.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_ADAS_GPSTRACK.FIELD_TASK_ID is
'��¼��ҵ��������';

/*==============================================================*/
/* Table: AU_AUDIO                                              */
/*==============================================================*/
create table AU_AUDIO  (
   AUDIO_ID             NUMBER(10)                      not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   "SIZE"               VARCHAR2(256),
   FORMAT               VARCHAR2(256),
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   IMP_WORKER           NUMBER(4)                      default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   MESH_ID              NUMBER(6)                      default 0 not null,
   constraint PK_AU_AUDIO primary key (AUDIO_ID)
);

comment on column AU_AUDIO.AUDIO_ID is
'����';

comment on column AU_AUDIO.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column AU_AUDIO.URL_FILE is
'��Ƶ�ļ��洢�ı������·����,��\Data\Audio\';

comment on column AU_AUDIO.FILE_NAME is
'[170]�ļ���(����չ��)';

comment on column AU_AUDIO."SIZE" is
'[170]';

comment on column AU_AUDIO.FORMAT is
'[170]WAV,ADP';

comment on column AU_AUDIO.DAY_TIME is
'[170]��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_AUDIO.WORKER is
'[170]�ο�"BI_PERSON"';

comment on column AU_AUDIO.IMP_WORKER is
'[170]�ο�"BI_PERSON"';

comment on column AU_AUDIO.IMP_VERSION is
'[170]';

comment on column AU_AUDIO.IMP_DATE is
'[170]��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_AUDIO.MESH_ID is
'[170]';

/*==============================================================*/
/* Table: AU_COMMUNICATION                                      */
/*==============================================================*/
create table AU_COMMUNICATION  (
   ID                   NUMBER(10)                      not null,
   TITLE                VARCHAR2(50),
   CONTENT              VARCHAR2(1000),
   GEOMETRY             SDO_GEOMETRY,
   RECEIVE_PERSON       NUMBER(10)                     default 0 not null,
   CREAT_TIME           TIMESTAMP,
   CREAT_PERSON         NUMBER(10)                     default 0 not null,
   constraint PK_AU_COMMUNICATION primary key (ID)
);

comment on column AU_COMMUNICATION.ID is
'����';

comment on column AU_COMMUNICATION.TITLE is
'������';

comment on column AU_COMMUNICATION.GEOMETRY is
'����WEBGIS����';

/*==============================================================*/
/* Table: AU_DATA_STATISTICS                                    */
/*==============================================================*/
create table AU_DATA_STATISTICS  (
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_CATEGORY        VARCHAR2(200 char),
   STATIS_TYPE          NUMBER(1)                      default 0 not null
       check (STATIS_TYPE in (0,1)),
   MARK_TYPE            NUMBER(6)                      default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_COUNT           NUMBER(10)                     default 0 not null
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
       check (TYPE in (0,1,2,3,4,5,6,7,8,9)),
   LINK_PID             NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   IN_WORKER            NUMBER(4)                      default 0 not null,
   MARK_ITEM            NUMBER(5)                      default 0 not null
       check (MARK_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,30,31,32,33,34,35,36,37,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,61,62,63,70,71,72,73,74,75,76,77,78,80,81,82,83,101,102,103,104,105,106,107,108,109,110,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232)),
   PARAM_L              NUMBER(10)                     default 0 not null,
   PARAM_R              NUMBER(10)                     default 0 not null,
   PARAM_S              VARCHAR2(2000),
   PARAM_EX             VARCHAR2(2000),
   STATUS               NUMBER(2)                      default 0 not null
       check (STATUS in (0,1,2,3,4,5,6,11)),
   CK_STATUS            NUMBER(2)                      default 0 not null
       check (CK_STATUS in (0,1,2,3,4,5)),
   ADJA_FLAG            NUMBER(2)                      default 0
       check (ADJA_FLAG is null or (ADJA_FLAG in (0,1,2))),
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
       check (FIELD_SOURCE in (0,1,2)),
   PARAM_EX_2           VARCHAR2(2000),
   PARAM_EX_3           VARCHAR2(2000),
   MESH_ID_2K           VARCHAR2(12),
   STATE                NUMBER(1)                      default 3 not null
       check (STATE in (1,2,3)),
   MERGE_FLAG           NUMBER(1)                      default 0 not null
       check (MERGE_FLAG in (0,1,2)),
   constraint PK_AU_MARK primary key (MARK_ID)
);

comment on column AU_MARK.MARK_ID is
'����';

comment on column AU_MARK.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_MARK.ANGLE is
'���������ļн�,0~360��';

comment on column AU_MARK.TYPE is
'����(Ĭ��ֵ)������,��ͼ,��Ƭ,��Ƶ,���ߵ�';

comment on column AU_MARK.LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_MARK.NODE_PID is
'�ο�"RD_NODE"';

comment on column AU_MARK.DAY_TIME is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_MARK.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_MARK.IN_WORKER is
'[173sp1]�ο�"BI_PERSON"';

comment on column AU_MARK.PARAM_EX is
'[173sp1]';

comment on column AU_MARK.CK_STATUS is
'[1900A]';

comment on column AU_MARK.ADJA_FLAG is
'[180A]';

comment on column AU_MARK.TASK_ID is
'��¼��ҵ��������';

comment on column AU_MARK.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_MARK.FIELD_TASK_ID is
'��¼��ҵ��������';

/*==============================================================*/
/* Table: AU_DRAFT                                              */
/*==============================================================*/
create table AU_DRAFT  (
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   STYLE                NUMBER(1)                      default 0 not null
       check (STYLE in (0,1)),
   COLOR                VARCHAR2(10),
   WIDTH                NUMBER(2)                      default 1 not null,
   GEO_SEG              VARCHAR2(500),
   TYPE                 NUMBER(2)                      default 0 not null
       check (TYPE in (0,1,2,3,4)),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   constraint AUDRAFT_MARK foreign key (MARK_ID)
         references AU_MARK (MARK_ID)
);

comment on column AU_DRAFT.MARK_ID is
'���,����"AU_MARK"';

comment on column AU_DRAFT.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������';

comment on column AU_DRAFT.STYLE is
'ʵ��,����';

comment on column AU_DRAFT.COLOR is
'[173sp1]';

/*==============================================================*/
/* Table: AU_GPSRECORD                                          */
/*==============================================================*/
create table AU_GPSRECORD  (
   GPSRECORD_ID         NUMBER(10)                      not null,
   MARK_ID              NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   SOURCE               NUMBER(2)                      default 0 not null
       check (SOURCE in (0,1,2,3,4,5,6,9,10,11)),
   NAME                 VARCHAR2(255),
   TABLE_NAME           VARCHAR2(64),
   LANE_NUM             NUMBER(2)                      default 0 not null,
   KIND                 NUMBER(5)                      default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   MEMO                 VARCHAR2(255),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   constraint PK_AU_GPSRECORD primary key (GPSRECORD_ID),
   constraint AUGPSRECORD_MARK foreign key (MARK_ID)
         references AU_MARK (MARK_ID)
);

comment on column AU_GPSRECORD.GPSRECORD_ID is
'����';

comment on column AU_GPSRECORD.MARK_ID is
'���,����"AU_MARK"';

comment on column AU_GPSRECORD.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������';

comment on column AU_GPSRECORD.NAME is
'�������ID,������ݡ���ʽ�洢,��������Ӣ�İ��"|"�ָ�,�硱100,ѧԺ·|202,�ɸ�·��';

comment on column AU_GPSRECORD.KIND is
'��TABLE_NAME���Ӧ���ֱ������ͬ,��TABLE_NAMEΪ"LU_LINK_KIND",���ֶ�ȡֵ��LU_LINK_KIND���е�"KIND"�ֱ����ֵ��ͬ';

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
       check (FIELD_SOURCE in (0,1)),
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
         references AU_GPSTRACK_GROUP (GROUP_ID)
);

comment on column AU_GPSTRACK.GPSTRACK_ID is
'����';

/*==============================================================*/
/* Table: AU_GPSTRACK_GROUP_VIDEO                               */
/*==============================================================*/
create table AU_GPSTRACK_GROUP_VIDEO  (
   GROUP_ID             NUMBER(10)                      not null,
   VIDEO_ID             NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   constraint FK_AUGPSTRACKGROUP_VIDEO foreign key (GROUP_ID)
         references AU_GPSTRACK_GROUP (GROUP_ID)
);

comment on column AU_GPSTRACK_GROUP_VIDEO.VIDEO_ID is
'�ο�"AU_VIDEO"';

/*==============================================================*/
/* Table: AU_GPSTRACK_PHOTO                                     */
/*==============================================================*/
create table AU_GPSTRACK_PHOTO  (
   GPSTRACK_ID          NUMBER(10)                      not null,
   PHOTO_GUID           NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUGPSTRACK_PHOTO foreign key (GPSTRACK_ID)
         references AU_GPSTRACK (GPSTRACK_ID)
);

comment on table AU_GPSTRACK_PHOTO is
'[210]';

comment on column AU_GPSTRACK_PHOTO.GPSTRACK_ID is
'���,����"AU_GPSTRACK"';

comment on column AU_GPSTRACK_PHOTO.PHOTO_GUID is
'�ο�������Ƭ�洢��';

comment on column AU_GPSTRACK_PHOTO.STATUS is
'��¼�Ƿ�ȷ��';

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
       check (SRC_FLAG in (0,1,2,3,4,5)),
   SRC_PID              NUMBER(10)                     default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   IMP_DATE             DATE,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_IX_ANNOTATION primary key (AUDATA_ID)
);

comment on table AU_IX_ANNOTATION is
'[171A]�ڵ����豸����ʾ��Ȼ������,����,��·��,��������Ƶȵ����';

comment on column AU_IX_ANNOTATION.AUDATA_ID is
'����';

comment on column AU_IX_ANNOTATION.PID is
'�ο�"IX_ANNOTATION"';

comment on column AU_IX_ANNOTATION.KIND_CODE is
'�ο�"IX_ANNOTATION_CODE"';

comment on column AU_IX_ANNOTATION.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_IX_ANNOTATION.RANK is
'����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾ
һ����ʾ�ȼ�(����),��ֵΪ0/1 �ֱ��ʾ��Ч/��Ч,
��:00000111 ��ʾ������1,2,4 ��ȼ��Ͼ����ʾ
��0bit:1 ��
��1bit:2 ��
��2bit:4 ��
��3bit:8 ��
��4bit:16 ��
��5bit:32 ��
��6bit:64 ��
��7bit:128 ��
��8bit:256 ��
��9bit:512 ��
��10bit:1024 ��
��11bit:2048 ��
��12bit:4096 ��
��13bit:8192 ��
ע:
(1)2.5 �����:1~8 ��
(2)20 �����:16~32 ��
(3)�������:64~512
(4)TOP �����:1024~8192 ��';

comment on column AU_IX_ANNOTATION.SRC_FLAG is
'ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.SRC_PID is
'������Դ�����ID,������POI��ΪPO��PID;���Ե�·����Ϊ��·��ID
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.CLIENT_FLAG is
'��ݲ�ͬ�ͻ�����,�����ͬ����,ֵ�����:
MB ����
HD �㱾
TY ����
PI �ȷ�
PA ����
NE NavEx
13CY 13CY
NBT ����
ע:
(1)����ÿһ�����ʾֻ�����ĳһ�ͻ�,��ֻ������,��ʾΪ"MB"
(2)����ʾ������ĳһ�ͻ��������ͻ�,�������ϴ���ǰ��Ӣ�İ��"-",��������������Ŀͻ�,���ʾΪ"-MB"
(3)���֮����Ӣ�İ��"|"�ָ�,���ʾ��������������,���ʾΪ"MB|-TY"
(4)Ĭ��Ϊ��,��ʾ���пͻ������
(5)���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.SPECTIAL_FLAG is
'����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾһ������(����),��ֵΪ0/1 �ֱ��ʾ��/��
��0bit:3DICON
��1bit:��ˮ��
����bit Ϊ��Ϊ0,��ʾ�������ʶ
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.REGION_ID is
'�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column AU_IX_ANNOTATION.EDIT_FLAG is
'�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AU_IX_ANNOTATION.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column AU_IX_ANNOTATION.RESERVED is
'[181A]';

comment on column AU_IX_ANNOTATION.MODIFY_FLAG is
'��¼�޸ķ�ʽ������,�����,�ĵȼ�,��λ��,ɾ���';

comment on column AU_IX_ANNOTATION.FIELD_MODIFY_FLAG is
'��¼�޸ķ�ʽ������,�����,�ĵȼ�,��λ��,ɾ���';

comment on column AU_IX_ANNOTATION.EXTRACT_INFO is
'(1)���"�汾+����������ȡ"
(2)����Address �ֶ�
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.EXTRACT_PRIORITY is
'��ȡ�����ȼ���(����ΪA1~A11;����ΪB2~B5)
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.REMARK is
'ת���ʱ,����page�ֶ�,������:"��ʾ���"��"�����"';

comment on column AU_IX_ANNOTATION.DETAIL_FLAG is
'ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column AU_IX_ANNOTATION.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_IX_ANNOTATION.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_ANNOTATION.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_ANNOTATION.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

comment on column AU_IX_ANNOTATION.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_IX_ANNOTATION_NAME                                 */
/*==============================================================*/
create table AU_IX_ANNOTATION_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2)),
   OLD_NAME             VARCHAR2(200),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_ANNOTATION_NAME foreign key (AUDATA_ID)
         references AU_IX_ANNOTATION (AUDATA_ID)
);

comment on table AU_IX_ANNOTATION_NAME is
'[171A]';

comment on column AU_IX_ANNOTATION_NAME.AUDATA_ID is
'���,����"AU_IX_ANNOTATION"';

comment on column AU_IX_ANNOTATION_NAME.NAME_ID is
'�ο�"IX_ANNOTATION_NAME"';

comment on column AU_IX_ANNOTATION_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_IX_ANNOTATION_NAME.PID is
'�ο�"IX_ANNOTATION"';

comment on column AU_IX_ANNOTATION_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_IX_ANNOTATION_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column AU_IX_ANNOTATION_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_ANNOTATION_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (SIDE in (0,1,2,3)),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   ADMIN_REAL           NUMBER(6)                      default 0 not null,
   IMPORTANCE           NUMBER(1)                      default 0 not null
       check (IMPORTANCE in (0,1)),
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)                      default 0 not null
       check (ACCESS_FLAG in (0,1,2)),
   OPEN_24H             NUMBER(1)                      default 0 not null
       check (OPEN_24H in (0,1,2)),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POST_CODE            VARCHAR2(6),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(100),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,9)),
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
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   IMP_DATE             DATE,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   VERIFIED_FLAG        NUMBER(1)                      default 9 not null
       check (VERIFIED_FLAG in (0,1,2,3,9)),
   OLD_X_GUIDE          NUMBER(10,5)                   default 0 not null,
   OLD_Y_GUIDE          NUMBER(10,5)                   default 0 not null,
   TRUCK_FLAG           NUMBER(1)                      default 0 not null
      check (TRUCK_FLAG in (0,1,2)),
   "LEVEL"              VARCHAR2(2)                    
      check ("LEVEL" is null or ("LEVEL" in ('A','B1','B2','B3','B4','C'))),
   SPORTS_VENUE         VARCHAR2(3),
   INDOOR               NUMBER(1)                      default 0 not null
      check (INDOOR in (0,1)),
   VIP_FLAG             VARCHAR2(10),
   constraint PK_AU_IX_POI primary key (AUDATA_ID)
);

comment on table AU_IX_POI is
'[171A]';

comment on column AU_IX_POI.AUDATA_ID is
'����';

comment on column AU_IX_POI.PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI.KIND_CODE is
'�ο�"IX_POI_CODE"';

comment on column AU_IX_POI.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������,����POI��ʾ�ͼ���Link���ҹ�ϵ
';

comment on column AU_IX_POI.LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_IX_POI.SIDE is
'��¼POIλ�����·Link��,�����Ҳ�';

comment on column AU_IX_POI.NAME_GROUPID is
'[173sp2]�ο�"RD_NAME"';

comment on column AU_IX_POI.PMESH_ID is
'ÿ����ҵ��POI �ڳɹ���е�һ����LINK ������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column AU_IX_POI.IMPORTANCE is
'��¼���·����POIΪ��Ҫ,��IMPORTANCEΪ1,����Ϊ0
(1)ӵ�й�ʽ���ۿڵĻ�
(2)������ξ������ĵȼ�Ϊ3A,4A,5A�ķ羰��
(3)�����Ļ��Ų�';

comment on column AU_IX_POI.CHAIN is
'��Ҫ���������Ǳ��ݺͼ���վ';

comment on column AU_IX_POI.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column AU_IX_POI.REGION_ID is
'�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column AU_IX_POI.EDIT_FLAG is
'�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AU_IX_POI.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column AU_IX_POI.RESERVED is
'[181A]';

comment on column AU_IX_POI.FIELD_STATE is
'�����,�ĵ�ַ,�ķ���';

comment on column AU_IX_POI.LABEL is
'[181U]��¼·,ˮ,�̵�,�����շ�,˫���շ�,��ʾλ��,24Сʱ�����';

comment on column AU_IX_POI.ADDRESS_FLAG is
'��־POI ��ַ(IX_POI_ADDRESS)������';

comment on column AU_IX_POI.EX_PRIORITY is
'��ȡ�����ȼ���(����ΪA1~A11;����ΪB2~B5)';

comment on column AU_IX_POI.EDITION_FLAG is
'��¼���������ҵ������ҵ�޸�,����,ɾ��ȱ�־';

comment on column AU_IX_POI.OLD_BLOCKCODE is
'ԭ�ṹ�е�"OLD����"';

comment on column AU_IX_POI.POI_NUM is
'��¼����NIDB��POI���';

comment on column AU_IX_POI.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_IX_POI.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_IX_POI.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (GUIDE_LINK_SIDE in (0,1,2,3)),
   LOCATE_LINK_SIDE     NUMBER(1)                      default 0 not null
       check (LOCATE_LINK_SIDE in (0,1,2,3)),
   SRC_PID              NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   IDCODE               VARCHAR2(36),
   DPR_NAME             VARCHAR2(100),
   DP_NAME              VARCHAR2(35),
   OPERATOR             VARCHAR2(32),
   MEMOIRE              VARCHAR2(200),
   DPF_NAME             VARCHAR2(500),
   POSTER_ID            VARCHAR2(100),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,2)),
   VERIFED              VARCHAR2(1)                    default 'F' not null
       check (VERIFED in ('T','F')),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   LOG                  VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   RESERVED             VARCHAR2(1000),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   IMP_DATE             DATE,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_IX_POINTADDRESS primary key (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS is
'[171A]���ƺ������ɵ������ܲ��Ű���һ���������,������λ����������λ�õı���,��������(������),¥(��)��,��Ԫ��,�����Ƶ�.';

comment on column AU_IX_POINTADDRESS.AUDATA_ID is
'����';

comment on column AU_IX_POINTADDRESS.PID is
'�ο�"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_IX_POINTADDRESS.GUIDE_LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_IX_POINTADDRESS.LOCATE_LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_IX_POINTADDRESS.LOCATE_NAME_GROUPID is
'�ο�"RD_NAME"';

comment on column AU_IX_POINTADDRESS.REGION_ID is
'�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column AU_IX_POINTADDRESS.EDIT_FLAG is
'�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AU_IX_POINTADDRESS.DPR_NAME is
'��ҵ�ɼ���·��';

comment on column AU_IX_POINTADDRESS.DP_NAME is
'��ҵ�ɼ����ƺ�';

comment on column AU_IX_POINTADDRESS.OPERATOR is
'��ҵ��OPERATOR�ֶ��е�����ԭ��ת��';

comment on column AU_IX_POINTADDRESS.MEMOIRE is
'��ע��Ϣ(������ҵLABEL)';

comment on column AU_IX_POINTADDRESS.POSTER_ID is
'�ʵ�Ա���';

comment on column AU_IX_POINTADDRESS.ADDRESS_FLAG is
'�����Ƶĵ�ַȷ�ϱ�ʶ';

comment on column AU_IX_POINTADDRESS.STATE is
'[173sp2]';

comment on column AU_IX_POINTADDRESS.LOG is
'���в�ֳ���������ֶ�';

comment on column AU_IX_POINTADDRESS.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_IX_POINTADDRESS.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POINTADDRESS.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POINTADDRESS.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

comment on column AU_IX_POINTADDRESS.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   LABEL                NUMBER(1)                      default 0 not null
       check (LABEL in (0,1,2)),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   constraint AU_IX_PADDRCHILD foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS_CHILDREN is
'[171A][1901U]';

comment on column AU_IX_POINTADDRESS_CHILDREN.AUDATA_ID is
'���,����"AU_IX_POINTADDRESS"';

comment on column AU_IX_POINTADDRESS_CHILDREN.GROUP_ID is
'�ο�"IX_POINTADDRESS_PARENT" ';

comment on column AU_IX_POINTADDRESS_CHILDREN.CHILD_PA_PID is
'�ο�"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS_CHILDREN.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POINTADDRESS_CHILDREN.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AU_IX_PADDRESSFLAG foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS_FLAG is
'[1901A]';

comment on column AU_IX_POINTADDRESS_FLAG.AUDATA_ID is
'���,����"AU_IX_POINTADDRESS"';

comment on column AU_IX_POINTADDRESS_FLAG.POINTADDRESS_PID is
'�ο�"IX_POINTADDRESS"';

comment on column AU_IX_POINTADDRESS_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column AU_IX_POINTADDRESS_FLAG.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POINTADDRESS_FLAG.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POINTADDRESS_NAME                               */
/*==============================================================*/
create table AU_IX_POINTADDRESS_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')),
   SUM_CHAR             NUMBER(1)                      default 0 not null
       check (SUM_CHAR in (0,1,2,3)),
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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POINTADDRESS_NAME foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS_NAME is
'[171A]';

comment on column AU_IX_POINTADDRESS_NAME.AUDATA_ID is
'���,����"AU_IX_POINTADDRESS"';

comment on column AU_IX_POINTADDRESS_NAME.NAME_ID is
'�ο�"IX_POINTADDRESS_NAME" ';

comment on column AU_IX_POINTADDRESS_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_IX_POINTADDRESS_NAME.PID is
'�ο�"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_IX_POINTADDRESS_NAME.SUM_CHAR is
'�����Ƶĺ�������,������,��ż,���';

comment on column AU_IX_POINTADDRESS_NAME.SPLIT_FLAG is
'[173sp2]';

comment on column AU_IX_POINTADDRESS_NAME.FULLNAME is
'��¼���ǰ��ȫ��ַ���';

comment on column AU_IX_POINTADDRESS_NAME.ROADNAME is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.ROADNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.ADDRNAME is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.ADDRNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POINTADDRESS_NAME.PROVINCE is
'������"ʡ��/ֱϽ��/������/�ر���������"';

comment on column AU_IX_POINTADDRESS_NAME.CITY is
'������"�ؼ�����/��������"';

comment on column AU_IX_POINTADDRESS_NAME.COUNTY is
'������"�ؼ�����/����/����(��ֱϽ�е���)"';

comment on column AU_IX_POINTADDRESS_NAME.TOWN is
'����ֵ������';

comment on column AU_IX_POINTADDRESS_NAME.PLACE is
'��Ȼ����,����С��,�������,��������';

comment on column AU_IX_POINTADDRESS_NAME.STREET is
'�ֵ�,��·��, ��ͬ,��,��,Ū';

comment on column AU_IX_POINTADDRESS_NAME.LANDMARK is
'ָ�е����ʾ���õĵ���,������ʩ,��λ,������ͨ������ʩ,��������,��·����,��ͨվ����';

comment on column AU_IX_POINTADDRESS_NAME.PREFIX is
'�����������ƺź���ĳɷ�';

comment on column AU_IX_POINTADDRESS_NAME.HOUSENUM is
'�����ƺź���,����ŷ�ʽ�����Ū����';

comment on column AU_IX_POINTADDRESS_NAME.TYPE is
'���ƺź�������';

comment on column AU_IX_POINTADDRESS_NAME.SUBNUM is
'�����ƺ������������ƺż����θ������Ƶ�ǰ׺��Ϣ';

comment on column AU_IX_POINTADDRESS_NAME.SURFIX is
'�����������Ƶ�ַ�Ĵ���,�䱾��û��ʵ������,��Ӱ�����Ƶ�ַ�ĺ���,��:�Ա�,��ʱ';

comment on column AU_IX_POINTADDRESS_NAME.ESTAB is
'��"**����","**С��"';

comment on column AU_IX_POINTADDRESS_NAME.BUILDING is
'��"A��,12��,31¥,B��"��';

comment on column AU_IX_POINTADDRESS_NAME.UNIT is
'��"2��"';

comment on column AU_IX_POINTADDRESS_NAME.FLOOR is
'��"12��"';

comment on column AU_IX_POINTADDRESS_NAME.ROOM is
'��"503��"';

comment on column AU_IX_POINTADDRESS_NAME.ADDONS is
'��"����,�Ա�,����"';

comment on column AU_IX_POINTADDRESS_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POINTADDRESS_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MODIFY_FLAG          NUMBER(1)                      default 0 not null
       check (MODIFY_FLAG in (0,1,2)),
   constraint AU_IX_PADDRPARENT foreign key (AUDATA_ID)
         references AU_IX_POINTADDRESS (AUDATA_ID)
);

comment on table AU_IX_POINTADDRESS_PARENT is
'[171A][1901U]';

comment on column AU_IX_POINTADDRESS_PARENT.AUDATA_ID is
'���,����"AU_IX_POINTADDRESS"';

comment on column AU_IX_POINTADDRESS_PARENT.GROUP_ID is
'�ο�"IX_POINTADDRESS_PARENT" ';

comment on column AU_IX_POINTADDRESS_PARENT.PARENT_PA_PID is
'�ο�"IX_POINTADDRESS" ';

comment on column AU_IX_POINTADDRESS_PARENT.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POINTADDRESS_PARENT.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_ADDRESS                                     */
/*==============================================================*/
create table AU_IX_POI_ADDRESS  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
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
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POI_ADDRESS foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_ADDRESS is
'[171A]';

comment on column AU_IX_POI_ADDRESS.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_ADDRESS.NAME_ID is
'�ο�"IX_POI_ADDRESS"';

comment on column AU_IX_POI_ADDRESS.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_IX_POI_ADDRESS.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_ADDRESS.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_IX_POI_ADDRESS.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column AU_IX_POI_ADDRESS.ROADNAME is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.ROADNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.ADDRNAME is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.ADDRNAME_PHONETIC is
'[173sp1]';

comment on column AU_IX_POI_ADDRESS.PROVINCE is
'POI�����е�"ʡ��/ֱϽ��/������/�ر���������"';

comment on column AU_IX_POI_ADDRESS.CITY is
'POI�����е�"�ؼ�����/��������"';

comment on column AU_IX_POI_ADDRESS.COUNTY is
'POI�����е�"�ؼ�����/����/����(��ֱϽ�е���)"';

comment on column AU_IX_POI_ADDRESS.TOWN is
'����ֵ������';

comment on column AU_IX_POI_ADDRESS.PLACE is
'��Ȼ����,����С��,�������,��������';

comment on column AU_IX_POI_ADDRESS.STREET is
'�ֵ�,��·��, ��ͬ,��,��,Ū';

comment on column AU_IX_POI_ADDRESS.LANDMARK is
'ָ�е����ʾ���õĵ���,������ʩ,��λ,������ͨ������ʩ,��������,��·����,��ͨվ����';

comment on column AU_IX_POI_ADDRESS.PREFIX is
'�����������ƺź���ĳɷ�';

comment on column AU_IX_POI_ADDRESS.HOUSENUM is
'�����ƺź���,����ŷ�ʽ�����Ū����';

comment on column AU_IX_POI_ADDRESS.TYPE is
'���ƺź�������';

comment on column AU_IX_POI_ADDRESS.SUBNUM is
'�����ƺ������������ƺż����θ������Ƶ�ǰ׺��Ϣ';

comment on column AU_IX_POI_ADDRESS.SURFIX is
'�����������Ƶ�ַ�Ĵ���,�䱾��û��ʵ������,��Ӱ�����Ƶ�ַ�ĺ���,��:�Ա�,��ʱ';

comment on column AU_IX_POI_ADDRESS.ESTAB is
'��"**����","**С��"';

comment on column AU_IX_POI_ADDRESS.BUILDING is
'��"A��,12��,31¥,B��"��';

comment on column AU_IX_POI_ADDRESS.FLOOR is
'��"12��"';

comment on column AU_IX_POI_ADDRESS.UNIT is
'��"2��"';

comment on column AU_IX_POI_ADDRESS.ROOM is
'��"503��"';

comment on column AU_IX_POI_ADDRESS.ADDONS is
'��"����,�Ա�,����"';

comment on column AU_IX_POI_ADDRESS.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_ADDRESS.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_BUILDING                                    */
/*==============================================================*/
create table AU_IX_POI_BUILDING  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0,
   FLOOR_USED           VARCHAR2(1000),
   FLOOR_EMPTY          VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POI_BUILDING foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_BUILDING is
'[181A]';

comment on column AU_IX_POI_BUILDING.AUDATA_ID is
'���';

comment on column AU_IX_POI_BUILDING.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_BUILDING.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_BUILDING.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_CHARGINGSTATION                             */
/*==============================================================*/
create table AU_IX_POI_CHARGINGSTATION  (
   AUCHARG_ID           NUMBER(10)                      not null,
   AUDATA_ID            NUMBER(10)                      not null,
   CHARGING_ID          NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CHARGING_TYPE        NUMBER(2)                      default 2 not null
       check (CHARGING_TYPE in (0,1,2,3,4)),
   CHARGING_NUM         VARCHAR2(5),
   EXCHANGE_NUM         VARCHAR2(5),
   PAYMENT              VARCHAR2(20),
   SERVICE_PROV         VARCHAR2(2),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   PARKING_NUM          VARCHAR2(30),
   "MODE"               VARCHAR2(10),
   PLUG_TYPE            VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(100),
   constraint PK_AU_IX_POI_CHARGINGSTATION primary key (AUCHARG_ID),
   constraint FK_AUIX_POI_CHARGINGSTATION foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_CHARGINGSTATION is
'[190A]';

comment on column AU_IX_POI_CHARGINGSTATION.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_CHARGINGSTATION.CHARGING_ID is
'�ο�"IX_POI_CHARGINGSTATION"';

comment on column AU_IX_POI_CHARGINGSTATION.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_CHARGINGSTATION.CHARGING_NUM is
'���ڵ���0,�ձ�ʾδ����';

comment on column AU_IX_POI_CHARGINGSTATION.EXCHANGE_NUM is
'�ձ�ʾδ����';

comment on column AU_IX_POI_CHARGINGSTATION.PAYMENT is
'ֵ�����:
����	���
0	���� 
1	�ֽ�
2	���ÿ�
3	IC��
4	���Ƴ�ֵ��
���ָ��ѷ�ʽʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_CHARGINGSTATION.SERVICE_PROV is
'ֵ�����:
1	��ҵ���
2	�Ϸ�����
3	��ʯ��
4	��ʯ��
5	       �к���
6	����
�۰�ֵ��
11	   ���
12	   �۟�
13	   ���
14       ����
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_CHARGINGSTATION.ATT_TASK_ID is
'DMS ������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_CHARGINGSTATION.FIELD_TASK_ID is
'��ҵPOI ����DMS ʱ��ֵ';

comment on column AU_IX_POI_CHARGINGSTATION.PARKING_NUM is
'�۰Ķ����ֶΣ���½ʱΪ��';

comment on column AU_IX_POI_CHARGINGSTATION."MODE" is
'ֵ�����:
����	���
1	�˜ʳ��
2	���ٳ��
3	���ٳ��
����ģʽʱ����Ӣ�İ�ǡ�|���ָ�
�۰Ķ����ֶΣ���½ʱΪ��';

comment on column AU_IX_POI_CHARGINGSTATION.PLUG_TYPE is
'ֵ�����: ��һλ��ʾ���ģʽ
����	���
11	3�׼���
12	7�ךWʽ�����
21	7�ךWʽ�����
22	5����ʽ
23	3�׼���
31	�ձ�CHAdeMO�˜ʲ��^
�������ʱ����Ӣ�İ�ǡ�|���ָ�
�۰Ķ����ֶΣ���½ʱΪ��';

/*==============================================================*/
/* Table: AU_IX_POI_CHARGINGPLOT                                */
/*==============================================================*/
create table AU_IX_POI_CHARGINGPLOT  (
   AUCHARG_ID           NUMBER(10)                      not null,
   CHARGING_ID          NUMBER(10)                     default 0 not null,
   GROUP_ID             NUMBER(5)                      default 1 not null,
   COUNT                NUMBER(5)                      default 1 not null,
   ACDC                 NUMBER(5)                      default 0 not null
       check (ACDC in (0,1)),
   PLUG_TYPE            VARCHAR2(10),
   POWER                VARCHAR2(10),
   VOLTAGE              VARCHAR2(10),
   "CURRENT"            VARCHAR2(10),
   "MODE"               NUMBER(2)                      default 0 not null
       check ("MODE" in (0,1)),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   CHARGINGSTATION_FIELD_GUID VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   constraint FK_AUIX_POI_CHARGINGPLOT foreign key (AUCHARG_ID)
         references AU_IX_POI_CHARGINGSTATION (AUCHARG_ID)
);

comment on table AU_IX_POI_CHARGINGPLOT is
'[190A]';

comment on column AU_IX_POI_CHARGINGPLOT.AUCHARG_ID is
'���,����"AU_IX_POI_CHARGINGSTATION"';

comment on column AU_IX_POI_CHARGINGPLOT.CHARGING_ID is
'���,����"IX_POI_CHARGINESTATION"';

comment on column AU_IX_POI_CHARGINGPLOT.GROUP_ID is
'��/ֱ����,��ͷ����,��繦�ʺ͵�ѹ����ͬ�ĳ��׮Ϊһ��';

comment on column AU_IX_POI_CHARGINGPLOT.COUNT is
'ͬһ���ڵĳ��׮����';

comment on column AU_IX_POI_CHARGINGPLOT.PLUG_TYPE is
'ֵ�����:
����	���
0 ������3�ײ��
1 ������7�ײ��
2 ֱ����9�ײ��
9 ����
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_CHARGINGPLOT.POWER is
'��λΪKW';

comment on column AU_IX_POI_CHARGINGPLOT.VOLTAGE is
'��λΪV,';

comment on column AU_IX_POI_CHARGINGPLOT."CURRENT" is
'[180A]��λΪA';

comment on column AU_IX_POI_CHARGINGPLOT.MEMO is
'[180A]';

comment on column AU_IX_POI_CHARGINGPLOT.ATT_TASK_ID is
'DMS ������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_CHARGINGPLOT.FIELD_TASK_ID is
'��ҵPOI ����DMS ʱ��ֵ';

/*==============================================================*/
/* Table: AU_IX_POI_CHILDREN                                    */
/*==============================================================*/
create table AU_IX_POI_CHILDREN  (
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   CHILD_POI_PID        NUMBER(10)                     default 0 not null,
   RELATION_TYPE        NUMBER(1)                      default 0 not null
       check (RELATION_TYPE in (0,1,2)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   LABEL                NUMBER(1)                      default 0 not null
       check (LABEL in (0,1,2)),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   constraint AU_IX_POI_CHILD foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_CHILDREN is
'[171A][1901U]';

comment on column AU_IX_POI_CHILDREN.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_CHILDREN.GROUP_ID is
'�ο�"IX_POI_PARENT"';

comment on column AU_IX_POI_CHILDREN.CHILD_POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_CHILDREN.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_CHILDREN.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_CONTACT                                     */
/*==============================================================*/
create table AU_IX_POI_CONTACT  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   OLD_CONTACT          VARCHAR2(128),
   CONTACT_TYPE         NUMBER(2)                      default 1 not null
       check (CONTACT_TYPE in (1,2,3,4,11,21,22)),
   CONTACT              VARCHAR2(128),
   CONTACT_DEPART       NUMBER(3)                      default 0 not null,
   PRIORITY             NUMBER(5)                      default 1 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POI_CONTACT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_CONTACT is
'[171A]�����ڶ����ϵ��ʽʱ,�洢Ϊ������¼';

comment on column AU_IX_POI_CONTACT.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_CONTACT.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_CONTACT.CONTACT is
'��¼����������ڵĵ绰����,�洢ΪӢ�İ�������ַ�,����֮���԰��"-"�ָ�,��010-82306399';

comment on column AU_IX_POI_CONTACT.CONTACT_DEPART is
'����8bit ��ʾ,���ҵ�������Ϊ0~7bit,ÿbit ��ʾһ��������(����),��ֵΪ0/1 �ֱ��ʾ��/��,��:00000011 ��ʾ�ܻ�Ϳͷ�;00000101 ��ʾ�ܻ��Ԥ��
��0bit:�ܻ�
��1bit:�ͷ�
��2bit:Ԥ��
��3bit:����
��4bit:ά��
��5bit:����
�������bit λ��Ϊ0,��ʾδ����';

comment on column AU_IX_POI_CONTACT.PRIORITY is
'[1901U]��ϵ��ʽ�����ȼ�����';

comment on column AU_IX_POI_CONTACT.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_CONTACT.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_FLAG                                        */
/*==============================================================*/
create table AU_IX_POI_FLAG  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   FLAG_CODE            VARCHAR2(12),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POIFLAG foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_FLAG is
'[180]';

comment on column AU_IX_POI_FLAG.AUDATA_ID is
'���';

comment on column AU_IX_POI_FLAG.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column AU_IX_POI_FLAG.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_FLAG.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   SERVICE_PROV         VARCHAR2(2),
   PAYMENT              VARCHAR2(50),
   OPEN_HOUR            VARCHAR2(254),
   PHOTO_NAME           VARCHAR2(100),
   constraint FK_AUIX_POI_PARKING foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_GASSTATION is
'[190A]';

comment on column AU_IX_POI_GASSTATION.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_GASSTATION.GASSTATION_ID is
'�ο�"IX_POI_GASSTATION"';

comment on column AU_IX_POI_GASSTATION.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_GASSTATION.FUEL_TYPE is
'ֵ�����:
����	���
0	����(Diesel)
1	����(Gasoline)
2	�״�����(MG85)
3	����
4	Һ��ʯ����(LPG)
5	��Ȼ��(CNG)
6	�Ҵ�����(E10)
7	��ȼ��(Hydrogen)
8	�������(Biodiesel)
9	Һ����Ȼ��(LNG)
10	ѹ����Ȼ��
�۰�ֵ�� ��һλ����Ӧ�ķ����ṩ��
����	���
11	SINO X Power
12	SINO Power
13	SINO Disel
14	LPG
21	���Ų���
22	��������
23	��������
24	LPG
31	Gold�ƽ�
32	Platinum�׽�
33	Diesel�������
34	�����ֽ���ieselCashCard
35	ʯ�͚�AutoGas
41	Disel���������
42	8000���
43	F-1�ؼ����
44	AutoGasʯ�͚�
51	Disesel����
52	FuelSave�a���䷽����
53	Shell V-Power
54	AutoGasʯ�͚�
61	���őa���䷽����
62	�坍�䷽�������
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
';

comment on column AU_IX_POI_GASSTATION.OIL_TYPE is
'[180U]ֵ�����:
����	���
0	����
89            89#����
90	90#����
92            92#����
93	93#����
95            95#����
97	97#����
98	98#���� 
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ע:��FUEL_TYPE=1(����)ʱ��ֵ,����Ϊ��
';

comment on column AU_IX_POI_GASSTATION.EG_TYPE is
'ֵ�����:
����	���
0	����
E90	E90#����
E93	E93#����
E97	E97#����
E98	E98#����
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ע:��FUEL_TYPE=6(�Ҵ�����)ʱ��ֵ,����Ϊ��';

comment on column AU_IX_POI_GASSTATION.MG_TYPE is
'ֵ�����:
���� ���
0 ����
M5 M5#����
M10 M10#����
M15 M15#����
M30 M30#����
M50 M50#����
M85 M85#����
M100 M100#����
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ע:��FUEL_TYPE=3(�״�����)ʱ��ֵ,����Ϊ��';

comment on column AU_IX_POI_GASSTATION.SERVICE is
'ֵ�����:
����	���
1	�����
2	ϴ��
3	��ά��
4	�����
5	����
6	ס��
7	����
8	�������
�۰�ֵ��
����	���
11	�Q�ͷ���Lube Service
12	ϴ܇����Car Wash
13	�����Convenience Store
14	����Toilet
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
';

comment on column AU_IX_POI_GASSTATION.MEMO is
'[180A]';

comment on column AU_IX_POI_GASSTATION.ATT_TASK_ID is
'DMS ������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_GASSTATION.FIELD_TASK_ID is
'��ҵPOI ����DMS ʱ��ֵ';

comment on column AU_IX_POI_GASSTATION.SERVICE_PROV is
'1	��ʯ��(Sinopec)
2	�Ї�ʯ��(Chinaoil)
3	�ӵ�ʿ(Caltex)
4	����ɭ���ں͖|��(Esso Feoso)
5	͘��(Shell)
6	�Ϲ�ʯ��(Nkoil)
7	�׸�(Towngas)
8	����
�۰Ķ����ֶΣ���½ʱΪ��
';

comment on column AU_IX_POI_GASSTATION.PAYMENT is
'�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ֵ�����:
��½ֵ��
����	���
0	�ֽ�
1	��ǿ�
2	���ÿ�
�۰�ֵ��
10	���_ͨ
11	VISA
12	MasterCard
13	�F��
14	����
';

/*==============================================================*/
/* Table: AU_IX_POI_HOTEL                                       */
/*==============================================================*/
create table AU_IX_POI_HOTEL  (
   AUDATA_ID            NUMBER(10)                      not null,
   HOTEL_ID             NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CREDIT_CARD          VARCHAR2(10),
   RATING               NUMBER(2)                      default 0 not null
       check (RATING in (0,1,3,4,5,6,7,8,13,14,15,16,17)),
   CHECKIN_TIME         VARCHAR2(20)                   default '14:00' not null,
   CHECKOUT_TIME        VARCHAR2(20)                   default '12:00' not null,
   ROOM_COUNT           NUMBER(5)                      default 0 not null,
   ROOM_TYPE            VARCHAR2(20),
   ROOM_PRICE           VARCHAR2(100),
   BREAKFAST            NUMBER(2)                      default 0 not null
       check (BREAKFAST in (0,1)),
   SERVICE              VARCHAR2(254),
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)),
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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   constraint AUIX_POI_HOTEL foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_HOTEL is
'[173sp1]';

comment on column AU_IX_POI_HOTEL.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_HOTEL.HOTEL_ID is
'���,�ο�"IX_POI_HOTEL"';

comment on column AU_IX_POI_HOTEL.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_HOTEL.CREDIT_CARD is
'ֵ�����:
1 άʿ(visa)
2 ���´�(mastercard)
3 ����(dinas)
4 �ձ�������ÿ�(jcb)
5 ������ͨ(America
Express)
6 ����(unionpay)
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ0 ��ʾ��֧�����ÿ�
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_HOTEL.CHECKIN_TIME is
'��ʽ:HH:mm';

comment on column AU_IX_POI_HOTEL.CHECKOUT_TIME is
'��ʽ:HH:mm';

comment on column AU_IX_POI_HOTEL.ROOM_COUNT is
'���ڵ���0 ������,0 ��ʾδ����';

comment on column AU_IX_POI_HOTEL.ROOM_TYPE is
'ֵ�����:
1 ���˼�(single)
2 ��׼��(double)
3 �׷�(suite)
�������ʱ����Ӣ�İ��"|"�ָ�
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_HOTEL.ROOM_PRICE is
'����۸�ʱ����Ӣ�İ��"|"�ָ�,˳�������ͷ�����һ��
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_HOTEL.SERVICE is
'ֵ�����:
1 ������
2 ��������
31 �ư�
32 ����OK
33 ��������
34 ������Ӿ��
35 SPA
36 ɣ��
51 �в���
52 ������
53 ������
54 ����
�������ʱ����Ӣ�İ��"|"�ָ�
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_HOTEL.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_HOTEL.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (NAME_CLASS in (1,3,4,5,6,7,8,9)),
   NAME_TYPE            NUMBER(2)                      default 1 not null
       check (NAME_TYPE in (1,2)),
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_IX_POI_NAME primary key (AUNAME_ID),
   constraint AUIX_POI_NAME foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_NAME is
'[171A]';

comment on column AU_IX_POI_NAME.AUNAME_ID is
'[180A]����';

comment on column AU_IX_POI_NAME.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_NAME.NAME_ID is
'�ο�"IX_POI_NAME"';

comment on column AU_IX_POI_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_IX_POI_NAME.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_NAME.NAME_CLASS is
'[180U]';

comment on column AU_IX_POI_NAME.NAME_TYPE is
'[180A]';

comment on column AU_IX_POI_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_IX_POI_NAME.KEYWORDS is
'��¼POI ��������ƴ���ؼ��ֻ�������,�ؼ���֮����Ӣ�İ��"/"�ָ�,��"����������"�ؼ��ֻ���Ϊ:"bei jing shi/zheng fu';

comment on column AU_IX_POI_NAME.NIDB_PID is
'��¼����POI���Ѿ���Ʒ������ID,��ͬ��������PID��ͬ';

comment on column AU_IX_POI_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_NAME_FLAG                                   */
/*==============================================================*/
create table AU_IX_POI_NAME_FLAG  (
   AUNAME_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   FLAG_CODE            VARCHAR2(12),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POINAMEFLAG foreign key (AUNAME_ID)
         references AU_IX_POI_NAME (AUNAME_ID)
);

comment on table AU_IX_POI_NAME_FLAG is
'[180]';

comment on column AU_IX_POI_NAME_FLAG.AUNAME_ID is
'���,����"AU_IX_POI_NAME"';

comment on column AU_IX_POI_NAME_FLAG.NAME_ID is
'�ο�"IX_POI_NAME"';

comment on column AU_IX_POI_NAME_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column AU_IX_POI_NAME_FLAG.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_NAME_FLAG.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (SIDE in (0,1,2,3)),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   ADMIN_REAL           NUMBER(6)                      default 0 not null,
   IMPORTANCE           NUMBER(1)                      default 0 not null
       check (IMPORTANCE in (0,1)),
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)                      default 0 not null
       check (ACCESS_FLAG in (0,1,2)),
   OPEN_24H             NUMBER(1)                      default 0 not null
       check (OPEN_24H in (0,1,2)),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POST_CODE            VARCHAR2(6),
   DIF_GROUPID          VARCHAR2(200),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   RESERVED             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(500),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,9)),
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
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   IMP_DATE             DATE,
   constraint PK_AU_IX_POI_RP00 primary key (AUDATA_ID)
);

comment on table AU_IX_POI_RP00 is
'[181A]';

comment on column AU_IX_POI_RP00.AUDATA_ID is
'����';

comment on column AU_IX_POI_RP00.PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_RP00.KIND_CODE is
'�ο�"IX_POI_CODE"';

comment on column AU_IX_POI_RP00.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������,����POI��ʾ�ͼ���Link���ҹ�ϵ
';

comment on column AU_IX_POI_RP00.LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_IX_POI_RP00.SIDE is
'��¼POIλ�����·Link��,�����Ҳ�';

comment on column AU_IX_POI_RP00.NAME_GROUPID is
'�ο�"RD_NAME"';

comment on column AU_IX_POI_RP00.PMESH_ID is
'ÿ����ҵ��POI �ڳɹ���е�һ����LINK ������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column AU_IX_POI_RP00.IMPORTANCE is
'��¼���·����POIΪ��Ҫ,��IMPORTANCEΪ1,����Ϊ0
(1)ӵ�й�ʽ���ۿڵĻ�
(2)������ξ������ĵȼ�Ϊ3A,4A,5A�ķ羰��
(3)�����Ļ��Ų�';

comment on column AU_IX_POI_RP00.CHAIN is
'��Ҫ���������Ǳ��ݺͼ���վ';

comment on column AU_IX_POI_RP00.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column AU_IX_POI_RP00.REGION_ID is
'�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column AU_IX_POI_RP00.DIF_GROUPID is
'���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column AU_IX_POI_RP00.EDIT_FLAG is
'�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AU_IX_POI_RP00.FIELD_STATE is
'�����,�ĵ�ַ,�ķ���';

comment on column AU_IX_POI_RP00.LABEL is
'��¼·,ˮ,�̵�,�����շ�,˫���շ�,��ʾλ��,24Сʱ�����';

comment on column AU_IX_POI_RP00.ADDRESS_FLAG is
'��־POI ��ַ(IX_POI_ADDRESS)������';

comment on column AU_IX_POI_RP00.EX_PRIORITY is
'��ȡ�����ȼ���(����ΪA1~A11;����ΪB2~B5)';

comment on column AU_IX_POI_RP00.EDITION_FLAG is
'��¼���������ҵ������ҵ�޸�,����,ɾ��ȱ�־';

comment on column AU_IX_POI_RP00.OLD_BLOCKCODE is
'ԭ�ṹ�е�"OLD����"';

comment on column AU_IX_POI_RP00.POI_NUM is
'��¼����NIDB��POI���';

comment on column AU_IX_POI_RP00.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_IX_POI_RP00.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_RP00.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_RP00.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

comment on column AU_IX_POI_RP00.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

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
       check (NAME_CLASS in (1,3,4,5,6,7,9)),
   NAME_TYPE            NUMBER(2)                      default 1 not null
       check (NAME_TYPE in (1,2)),
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_IX_POI_NAME_RP00 primary key (AUNAME_ID),
   constraint AUIX_POI_NAMERP00 foreign key (AUDATA_ID)
         references AU_IX_POI_RP00 (AUDATA_ID)
);

comment on table AU_IX_POI_NAME_RP00 is
'[181A]';

comment on column AU_IX_POI_NAME_RP00.AUNAME_ID is
'����';

comment on column AU_IX_POI_NAME_RP00.AUDATA_ID is
'���,����"AU_IX_POI_RP00"';

comment on column AU_IX_POI_NAME_RP00.NAME_ID is
'�ο�"IX_POI_NAME"';

comment on column AU_IX_POI_NAME_RP00.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_IX_POI_NAME_RP00.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_NAME_RP00.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_IX_POI_NAME_RP00.KEYWORDS is
'��¼POI ��������ƴ���ؼ��ֻ�������,�ؼ���֮����Ӣ�İ��"/"�ָ�,��"����������"�ؼ��ֻ���Ϊ:"bei jing shi/zheng fu';

comment on column AU_IX_POI_NAME_RP00.NIDB_PID is
'��¼����POI���Ѿ���Ʒ������ID,��ͬ��������PID��ͬ';

comment on column AU_IX_POI_NAME_RP00.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_NAME_RP00.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_POI_NOKIA                                       */
/*==============================================================*/
create table AU_IX_POI_NOKIA  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_NUM              VARCHAR2(100),
   KIND_CODE            VARCHAR2(8),
   NAME                 VARCHAR2(1000),
   GEOMETRY             SDO_GEOMETRY,
   TELEPHONE            VARCHAR2(1000),
   ADDRESS              VARCHAR2(1000),
   STATE                NUMBER(2)                      default 0 not null
       check (STATE in (0,1,2)),
   constraint PK_AU_IX_POI_NOKIA primary key (AUDATA_ID)
);

comment on table AU_IX_POI_NOKIA is
'[173sp2]';

comment on column AU_IX_POI_NOKIA.AUDATA_ID is
'����';

/*==============================================================*/
/* Table: AU_IX_POI_PARENT                                      */
/*==============================================================*/
create table AU_IX_POI_PARENT  (
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   PARENT_POI_PID       NUMBER(10)                     default 0 not null,
   TENANT_FLAG          NUMBER(2)                      default 0
       check (TENANT_FLAG is null or (TENANT_FLAG in (0,1))),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   MODIFY_FLAG          NUMBER(1)                      default 0 not null
       check (MODIFY_FLAG in (0,1,2)),
   constraint AU_IX_POI_PARENT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_PARENT is
'[171A][1901U]';

comment on column AU_IX_POI_PARENT.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_PARENT.GROUP_ID is
'�ο�"IX_POI_PARENT"';

comment on column AU_IX_POI_PARENT.PARENT_POI_PID is
'�ο�"IX_POI" ';

comment on column AU_IX_POI_PARENT.TENANT_FLAG is
'[181A]';

comment on column AU_IX_POI_PARENT.MEMO is
'[181A]';

comment on column AU_IX_POI_PARENT.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_PARENT.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   POI_FIELD_GUID       VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   PARAM_EX_1           VARCHAR2(1000),
   PARAM_EX_2           VARCHAR2(1000),
   PARAM_EX_3           VARCHAR2(1000),
   PARAM_EX_4           VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   PAYMENT              VARCHAR2(20),
   REMARK               VARCHAR2(30),
   SOURCE               VARCHAR2(10),
   ACCESS_TYPE          NUMBER(1)                      default 2 not null
       check (ACCESS_TYPE in (0,1,2,3)),
   RES_HIGH             NUMBER(5,2)                    default 0 not null,
   RES_WIDTH            NUMBER(5,2)                    default 0 not null,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   CERTIFICATE          NUMBER(1)                      default 0 not null
       check (CERTIFICATE in (0,1,2,3)),
   MECHANICAL_GARAGE    NUMBER(1)                      default 0 not null
       check (MECHANICAL_GARAGE in (0,1,2,3)),
   VEHICLE              NUMBER(1)                      default 0 not null
       check (VEHICLE in (0,1,2,3)),
   PHOTO_NAME           VARCHAR2(100),
   constraint FK_AU_IX_PO_REFERENCE_AU_IX_PO foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_PARKING is
'[190A]';

comment on column AU_IX_POI_PARKING.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_PARKING.PARKING_ID is
'�ο�"IX_POI_PARKING"';

comment on column AU_IX_POI_PARKING.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_PARKING.PARKING_TYPE is
'ֵ�����:
���� ���
0	���ڣ�����ֵ��ϵ��£�
1	����
2	ռ��
3	���ڣ����ϣ�
4	���ڣ����£�
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_PARKING.TOLL_STD is
'ֵ�����:
���� ���
0 ����
1 ����
2 �ƴ�
3 ��ʱ
4 �ֶμƼ�
5 ���
�����׼ʱ����Ӣ�İ��"|"�ָ�
���Ϊ�ձ�ʾδ����,��5(���)�����������͹���';

comment on column AU_IX_POI_PARKING.TOLL_WAY is
'ֵ�����:
���� ���
0 �ֹ��շ�
1 �����շ�
2 ����ɷ�
�����׼ʱ����Ӣ�İ��"|"�ָ�';

comment on column AU_IX_POI_PARKING.WORK_TIME is
'�������������,��:2012-08-10';

comment on column AU_IX_POI_PARKING.ATT_TASK_ID is
'DMS ������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_PARKING.FIELD_TASK_ID is
'��ҵPOI ����DMS ʱ��ֵ';

comment on column AU_IX_POI_PARKING.PAYMENT is
'ֵ�����:
����	���
10	���_ͨ
11	VISA
12	MasterCard
13	�F��
14	����
�����׼ʱ����Ӣ�İ��"|"�ָ�
�۰Ķ����ֶΣ���½ʱΪ��
';

comment on column AU_IX_POI_PARKING.REMARK is
'ֵ�����: 
��½��ֻ�����һ��ֵ
����	���
1	ס�����
2	�Ͳ����
3	�������
4	������������ⲿ�ַ���
5	��ͣ�������ڵ�����POI������ѡ����¡����ʡ��Һš���ҽ�ȹ�ϵʱ���
6	ֻ���ڻ������ѵ�Ⱥ�忪��
7	������
�۰ģ���ֵʱ����Ӣ�İ��"|"�ָ�
����	���
11	����C
12	�����L��
13	��ͣ؛܇
14	30������M
15	늄�܇���
16	����
17	ϴ܇����Ϟ
';

comment on column AU_IX_POI_PARKING.SOURCE is
'1         �����ֳ����ƻ���������˵��
2         ����ѯ��
3         ���Ա��ƺ�ѯ��';

/*==============================================================*/
/* Table: AU_IX_POI_PHOTO                                       */
/*==============================================================*/
create table AU_IX_POI_PHOTO  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   PHOTO_ID             NUMBER(10)                     default 0 not null,
   PHOTO_GUID           VARCHAR2(64),
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   POI_FIELD_GUID       VARCHAR2(64),
   TYPE                 NUMBER(1)                      default 2 not null
       check (TYPE in (1,2,3,4)),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   constraint AU_IX_POI_PHOTO foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_PHOTO is
'[173sp2]';

comment on column AU_IX_POI_PHOTO.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_PHOTO.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_PHOTO.PHOTO_ID is
'�ο�"AU_PHOTO"';

comment on column AU_IX_POI_PHOTO.STATUS is
'��¼�Ƿ�ȷ��';

comment on column AU_IX_POI_PHOTO.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_PHOTO.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

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
       check (PARKING in (0,1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUIX_POI_RESTAURANT foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

comment on table AU_IX_POI_RESTAURANT is
'[171A]';

comment on column AU_IX_POI_RESTAURANT.AUDATA_ID is
'���,����"AU_IX_POI"';

comment on column AU_IX_POI_RESTAURANT.RESTAURANT_ID is
'�ο�"IX_POI_RESTAURANT"';

comment on column AU_IX_POI_RESTAURANT.POI_PID is
'�ο�"IX_POI"';

comment on column AU_IX_POI_RESTAURANT.FOOD_TYPE is
'NM�༭,��¼���ֲ�ϵ���ʹ���,��³��,����,�ձ�����,����˵�,�����ϵ֮����"|"�ָ�;��Ϊδ����';

comment on column AU_IX_POI_RESTAURANT.CREDIT_CARD is
'ֵ�����:
1 άʿ(visa)
2 ���´�(mastercard)
3 ����(dinas)
4 �ձ�������ÿ�(jcb)
5 ������ͨ(America
Express)
6 ����(unionpay)
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ0 ��ʾ��֧�����ÿ�
���Ϊ�ձ�ʾδ����';

comment on column AU_IX_POI_RESTAURANT.AVG_COST is
'���Ϊ0 ��ʾδ����';

comment on column AU_IX_POI_RESTAURANT.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_IX_POI_RESTAURANT.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_IX_SAMEPOI                                         */
/*==============================================================*/
create table AU_IX_SAMEPOI  (
   SAMEPOI_AUDATA_ID    NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   RELATION_TYPE        NUMBER(1)                      default 1 not null
       check (RELATION_TYPE in (1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_DAY_TIME       DATE,
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
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
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   FIELD_TASK_SUB_ID    VARCHAR2(200),
   FIELD_GUID           VARCHAR2(64),
   FIELD_SOURCE         NUMBER(1)                      default 0 not null
       check (FIELD_SOURCE in (0,1)),
   constraint FK_AU_IX_SAMEPOIPART_SAMEPOI foreign key (SAMEPOI_AUDATA_ID)
         references AU_IX_SAMEPOI (SAMEPOI_AUDATA_ID),
   constraint FK_AU_IX_SAMEPOIPART_POI foreign key (AUDATA_ID)
         references AU_IX_POI (AUDATA_ID)
);

/*==============================================================*/
/* Table: AU_LOG_STATISTICS                                     */
/*==============================================================*/
create table AU_LOG_STATISTICS  (
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_CATEGORY        VARCHAR2(200),
   LOG_CATEGORY         NUMBER(2)                      default 0 not null
       check (LOG_CATEGORY in (0,1,2,3,4,5,6)),
   DATA_COUNT           NUMBER(10)                     default 0 not null
);

/*==============================================================*/
/* Table: AU_MARK_AUDIO                                         */
/*==============================================================*/
create table AU_MARK_AUDIO  (
   MARK_ID              NUMBER(10)                      not null,
   AUDIO_ID             NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUMARK_AUDIO foreign key (MARK_ID)
         references AU_MARK (MARK_ID)
);

comment on table AU_MARK_AUDIO is
'[170]';

comment on column AU_MARK_AUDIO.MARK_ID is
'���,����"AU_MARK"';

comment on column AU_MARK_AUDIO.AUDIO_ID is
'�ο�"AU_AUDIO"';

comment on column AU_MARK_AUDIO.STATUS is
'��¼�Ƿ�ȷ��';

/*==============================================================*/
/* Table: AU_MARK_PHOTO                                         */
/*==============================================================*/
create table AU_MARK_PHOTO  (
   MARK_ID              NUMBER(10)                      not null,
   PHOTO_ID             NUMBER(10)                     default 0 not null,
   PHOTO_GUID           VARCHAR2(64),
   STATUS               VARCHAR2(100),
   constraint AUMARK_PHOTO foreign key (MARK_ID)
         references AU_MARK (MARK_ID)
);

comment on column AU_MARK_PHOTO.MARK_ID is
'���,����"AU_MARK"';

comment on column AU_MARK_PHOTO.PHOTO_ID is
'[170]�ο�"AU_PHOTO"';

comment on column AU_MARK_PHOTO.STATUS is
'[170]��¼�Ƿ�ȷ��';

/*==============================================================*/
/* Table: AU_MARK_VIDEO                                         */
/*==============================================================*/
create table AU_MARK_VIDEO  (
   MARK_ID              NUMBER(10)                      not null,
   VIDEO_ID             NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   constraint AUMARK_VIDEO foreign key (MARK_ID)
         references AU_MARK (MARK_ID)
);

comment on table AU_MARK_VIDEO is
'[170]';

comment on column AU_MARK_VIDEO.MARK_ID is
'���,����"AU_MARK"';

comment on column AU_MARK_VIDEO.VIDEO_ID is
'�ο�"AU_VIDEO"';

comment on column AU_MARK_VIDEO.STATUS is
'��¼�Ƿ�ȷ��';

/*==============================================================*/
/* Table: AU_PHOTO                                              */
/*==============================================================*/
create table AU_PHOTO  (
   PHOTO_ID             NUMBER(10)                      not null,
   CLASS                NUMBER(2)                      default 1 not null
       check (CLASS in (1,2,3,4,5,6,7)),
   NAME                 VARCHAR2(254),
   MESH_ID              NUMBER(6)                      default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   CAMERA_ID            NUMBER(2)                      default 1 not null,
   GEOMETRY             SDO_GEOMETRY,
   ANGLE                NUMBER(8,5)                    default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   IMP_WORKER           NUMBER(4)                      default 0 not null,
   IMP_FILENAME         VARCHAR2(256),
   FORMAT               VARCHAR2(64),
   STORE_SPACE          VARCHAR2(64),
   "SIZE"               VARCHAR2(64),
   DEPTH                VARCHAR2(64),
   DPI                  VARCHAR2(64),
   MEMO                 VARCHAR2(200),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_AU_PHOTO primary key (PHOTO_ID)
);

comment on table AU_PHOTO is
'��¼������ҵ��POI��Ƭ,�����Ƭ,��·����Ƭ�ȳɹ�';

comment on column AU_PHOTO.PHOTO_ID is
'����';

comment on column AU_PHOTO.CLASS is
'[170]';

comment on column AU_PHOTO.NAME is
'[170]�ļ���(����չ��)';

comment on column AU_PHOTO.MESH_ID_5K is
'��¼��Ƭ���ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column AU_PHOTO.CAMERA_ID is
'Ĭ��Ϊ0,˳ʱ����';

comment on column AU_PHOTO.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_PHOTO.ANGLE is
'����ļн�,-180~180��';

comment on column AU_PHOTO.DAY_TIME is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_PHOTO.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_PHOTO.IMP_DATE is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_PHOTO.IMP_WORKER is
'[170]�ο�"BI_PERSON"';

comment on column AU_PHOTO.FORMAT is
'��JPG,BMP,PNG��';

comment on column AU_PHOTO.STORE_SPACE is
'��Ƭ�Ĵ洢����';

comment on column AU_PHOTO."SIZE" is
'��Ƭ��������ظ���,��1024*768';

comment on column AU_PHOTO.DEPTH is
'��Ƭ���е���ɫ����,��8λ,24λ��';

comment on column AU_PHOTO.DPI is
'ÿӢ������ظ���';

comment on column AU_PHOTO.TASK_ID is
'��¼��ҵ��������';

comment on column AU_PHOTO.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_PHOTO.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column AU_PHOTO.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column AU_PHOTO.URL_FILE is
'��Ƭ�ļ��洢�ı������j·����,��\Data\Photo\';

/*==============================================================*/
/* Table: AU_PT_COMPANY                                         */
/*==============================================================*/
create table AU_PT_COMPANY  (
   AUDATA_ID            NUMBER(10)                      not null,
   COMPANY_ID           NUMBER(10)                     default 0 not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   CITY_CODE            NUMBER(6)                      default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_COMPANYID       VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_PT_COMPANY primary key (AUDATA_ID)
);

comment on table AU_PT_COMPANY is
'[171A]������˾��ָ���𹫽���·��ϵͳ��Ӫ�Ĺ�˾,�����?��ϵͳ���ϼ���λ';

comment on column AU_PT_COMPANY.AUDATA_ID is
'����';

comment on column AU_PT_COMPANY.COMPANY_ID is
'�ο�"PT_COMPANY"';

comment on column AU_PT_COMPANY.CITY_CODE is
'�洢����Ϊ4λ';

comment on column AU_PT_COMPANY.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_COMPANY.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_POI                                             */
/*==============================================================*/
create table AU_PT_POI  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   POI_KIND             VARCHAR2(4),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   ACCESS_CODE          VARCHAR2(32),
   ACCESS_TYPE          VARCHAR2(10)                   default '0' not null
       check (ACCESS_TYPE in ('0','1','2','3')),
   ACCESS_METH          NUMBER(3)                      default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   POI_MEMO             VARCHAR2(200),
   OPERATOR             VARCHAR2(30),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   POI_NUM              VARCHAR2(100),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   IMP_DATE             DATE,
   MESH_ID_2K           VARCHAR2(12),
   constraint PK_AU_PT_POI primary key (AUDATA_ID)
);

comment on table AU_PT_POI is
'[171A]����POI������(Stop POI)�ͳ����(Access POI)���������,ÿ�������Ӧһ����������,��������������POI.';

comment on column AU_PT_POI.AUDATA_ID is
'����';

comment on column AU_PT_POI.PID is
'�ο�"PT_POI"';

comment on column AU_PT_POI.POI_KIND is
'�ο�"IX_POI_CODE"';

comment on column AU_PT_POI.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_PT_POI.LINK_PID is
'�ο�"RD_LINK"';

comment on column AU_PT_POI.SIDE is
'���POI����ʾ������POIλ�����·Link�ϻ������Ҳ�(�����Link����)';

comment on column AU_PT_POI.NAME_GROUPID is
'[173sp2]�ο�"RD_NAME"';

comment on column AU_PT_POI.PMESH_ID is
'ÿ����ҵ��POI�ڳɹ���е�һ����LINK������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column AU_PT_POI.ACCESS_CODE is
'���������е�˳��Ż���,��:��������A �ڡ�,����ǡ� A��;�����깬վ�����ڡ�,����ǡ� ������;������֮��վ���ſڡ�,����ǡ� ����.����������û�б�ŵ�ֵΪ��';

comment on column AU_PT_POI.ACCESS_TYPE is
'����,���,�����';

comment on column AU_PT_POI.ACCESS_METH is
'����8bit ��ʾ,���ҵ�������Ϊ0~7bit,ÿbit ��ʾһ�ַ�ʽ����(����),��ֵΪ0/1 �ֱ��ʾ��/��,��:00000011 ��ʾб�ºͽ���;00000101 ��ʾб�ºͷ���
��0bit:б��
��1bit:����
��2bit:����
��3bit:ֱ��
��4bit:����
�������bit λ��Ϊ0,��ʾ��Ӧ��';

comment on column AU_PT_POI.MESH_ID_5K is
'��¼����POI���ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column AU_PT_POI.REGION_ID is
'�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column AU_PT_POI.EDIT_FLAG is
'�������������ȡʱ,����Ƿ�ɱ༭';

comment on column AU_PT_POI.POI_NUM is
'��¼����NIDB��POI���';

comment on column AU_PT_POI.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_PT_POI.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_POI.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_POI.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

comment on column AU_PT_POI.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_PT_ETA_ACCESS                                      */
/*==============================================================*/
create table AU_PT_ETA_ACCESS  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   OPEN_PERIOD          VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)                    default '0' not null
       check (MANUAL_TICKET in ('0','1','2')),
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   AUTO_TICKET          VARCHAR2(1)                    default '0' not null
       check (AUTO_TICKET in ('0','1','2')),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_POI_ETAACCESS foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID)
);

comment on table AU_PT_ETA_ACCESS is
'[171A]������ʵ���繫���������߱��Ĺ��ܼ����ܱߵĸ���������ʩ��';

comment on column AU_PT_ETA_ACCESS.AUDATA_ID is
'���,����"AU_PT_POI"';

comment on column AU_PT_ETA_ACCESS.POI_PID is
'�ο�"PT_POI"';

comment on column AU_PT_ETA_ACCESS.OPEN_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column AU_PT_ETA_ACCESS.MANUAL_TICKET_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-�������˿���,����¼����';

comment on column AU_PT_ETA_ACCESS.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_ETA_ACCESS.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_LINE                                            */
/*==============================================================*/
create table AU_PT_LINE  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   SYSTEM_ID            NUMBER(10)                     default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   TYPE                 NUMBER(2)                      default 11 not null
       check (TYPE in (11,12,13,14,15,21,31,32,33,41,42,16,51,52,53,54,61)),
   COLOR                VARCHAR2(10),
   NIDB_LINEID          VARCHAR2(32),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_PT_LINE primary key (AUDATA_ID)
);

comment on table AU_PT_LINE is
'[171A]��¼ĳ����Ĺ�����·��Ϣ,���� 656·,��4·��';

comment on column AU_PT_LINE.AUDATA_ID is
'����';

comment on column AU_PT_LINE.PID is
'�ο�"PT_LINE"';

comment on column AU_PT_LINE.SYSTEM_ID is
'�ο�"PT_SYSTEM"';

comment on column AU_PT_LINE.CITY_CODE is
'�����������û��ֱ�ӹ�ϵ,�������ά��';

comment on column AU_PT_LINE.COLOR is
'�洢16���Ƶ�RGBֵ';

comment on column AU_PT_LINE.LOG is
'[173sp1]';

comment on column AU_PT_LINE.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_LINE.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_ETA_LINE                                        */
/*==============================================================*/
create table AU_PT_ETA_LINE  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   BIKE                 VARCHAR2(1)                    default '0' not null
       check (BIKE in ('0','1','2')),
   BIKE_PERIOD          VARCHAR2(200),
   IMAGE                VARCHAR2(20),
   RACK                 VARCHAR2(1)                    default '0' not null
       check (RACK in ('0','1','2')),
   DINNER               VARCHAR2(1)                    default '0' not null
       check (DINNER in ('0','1','2')),
   TOILET               VARCHAR2(1)                    default '0' not null
       check (TOILET in ('0','1','2')),
   SLEEPER              VARCHAR2(1)                    default '0' not null
       check (SLEEPER in ('0','1','2')),
   WHEEL_CHAIR          VARCHAR2(1)                    default '0' not null
       check (WHEEL_CHAIR in ('0','1','2')),
   SMOKE                VARCHAR2(1)                    default '0' not null
       check (SMOKE in ('0','1','2')),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_LINE_ETALINE foreign key (AUDATA_ID)
         references AU_PT_LINE (AUDATA_ID)
);

comment on table AU_PT_ETA_LINE is
'[171A]������ʵ���繫����ͨ��·�о߱��Ĺ���,���������г�,�����,�òͷ����';

comment on column AU_PT_ETA_LINE.AUDATA_ID is
'���,����"AU_PT_LINE"';

comment on column AU_PT_ETA_LINE.PID is
'�ο�"PT_LINE"';

comment on column AU_PT_ETA_LINE.BIKE_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column AU_PT_ETA_LINE.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_ETA_LINE.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_ETA_STOP                                        */
/*==============================================================*/
create table AU_PT_ETA_STOP  (
   AUDATA_ID            NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   PRIVATE_PARK         VARCHAR2(1)                    default '0' not null
       check (PRIVATE_PARK in ('0','1','2','3')),
   PRIVATE_PARK_PERIOD  VARCHAR2(200),
   CARPORT_EXACT        VARCHAR2(32),
   CARPORT_ESTIMATE     VARCHAR2(1)                    default '0' not null
       check (CARPORT_ESTIMATE in ('0','1','2','3','4','5')),
   BIKE_PARK            VARCHAR2(1)                    default '0' not null
       check (BIKE_PARK in ('0','1','2','3')),
   BIKE_PARK_PERIOD     VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)                    default '0' not null
       check (MANUAL_TICKET in ('0','1','2')),
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   MOBILE               VARCHAR2(1)                    default '0' not null
       check (MOBILE in ('0','1','2')),
   BAGGAGE_SECURITY     VARCHAR2(1)                    default '0' not null
       check (BAGGAGE_SECURITY in ('0','1','2')),
   LEFT_BAGGAGE         VARCHAR2(1)                    default '0' not null
       check (LEFT_BAGGAGE in ('0','1','2')),
   CONSIGNATION_EXACT   VARCHAR2(32),
   CONSIGNATION_ESTIMATE VARCHAR2(1)                    default '0' not null
       check (CONSIGNATION_ESTIMATE in ('0','1','2','3','4','5')),
   CONVENIENT           VARCHAR2(1)                    default '0' not null
       check (CONVENIENT in ('0','1','2')),
   SMOKE                VARCHAR2(1)                    default '0' not null
       check (SMOKE in ('0','1','2')),
   BUILD_TYPE           VARCHAR2(1)                    default '0' not null
       check (BUILD_TYPE in ('0','1','2','3')),
   AUTO_TICKET          VARCHAR2(1)                    default '0' not null
       check (AUTO_TICKET in ('0','1','2')),
   TOILET               VARCHAR2(1)                    default '0' not null
       check (TOILET in ('0','1','2')),
   WIFI                 VARCHAR2(1)                    default '0' not null
       check (WIFI in ('0','1','2')),
   OPEN_PERIOD          VARCHAR2(200),
   FARE_AREA            VARCHAR2(1),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_POI_ETASTOP foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID)
);

comment on table AU_PT_ETA_STOP is
'[171A]������ʵ���繫����ͨվ��߱��Ĺ��ܼ����ܱߵĸ���������ʩ��';

comment on column AU_PT_ETA_STOP.AUDATA_ID is
'���,����"AU_PT_POI"';

comment on column AU_PT_ETA_STOP.POI_PID is
'�ο�"PT_POI"';

comment on column AU_PT_ETA_STOP.PRIVATE_PARK is
'�շѻ����';

comment on column AU_PT_ETA_STOP.PRIVATE_PARK_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column AU_PT_ETA_STOP.BIKE_PARK is
'�Ƿ����˿���';

comment on column AU_PT_ETA_STOP.BIKE_PARK_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-�������˿���,����¼����';

comment on column AU_PT_ETA_STOP.MANUAL_TICKET_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-�������˿���,����¼����';

comment on column AU_PT_ETA_STOP.OPEN_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column AU_PT_ETA_STOP.FARE_AREA is
'�ٷ���·ͼ��ֵ';

comment on column AU_PT_ETA_STOP.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_ETA_STOP.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_LINE_NAME                                       */
/*==============================================================*/
create table AU_PT_LINE_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_LINE_NAME foreign key (AUDATA_ID)
         references AU_PT_LINE (AUDATA_ID)
);

comment on table AU_PT_LINE_NAME is
'[171A]';

comment on column AU_PT_LINE_NAME.AUDATA_ID is
'���,����"AU_PT_LINE"';

comment on column AU_PT_LINE_NAME.NAME_ID is
'�ο�"PT_LINE_NAME"';

comment on column AU_PT_LINE_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_PT_LINE_NAME.PID is
'�ο�"PT_LINE"';

comment on column AU_PT_LINE_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_PT_LINE_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column AU_PT_LINE_NAME.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column AU_PT_LINE_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_LINE_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_PLATFORM                                        */
/*==============================================================*/
create table AU_PT_PLATFORM  (
   AUDATA_ID            NUMBER(10)                      not null,
   PID                  NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   COLLECT              NUMBER(2)                      default 0 not null
       check (COLLECT in (0,1)),
   P_LEVEL              NUMBER(2)                      default 0 not null
       check (P_LEVEL in (4,3,2,1,0,-1,-2,-3,-4,-5,-6)),
   TRANSIT_FLAG         NUMBER(1)                      default 0 not null
       check (TRANSIT_FLAG in (0,1)),
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_PLATFORMID      VARCHAR2(32),
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   IMP_DATE             DATE,
   constraint PK_AU_PT_PLATFORM primary key (AUDATA_ID)
);

comment on table AU_PT_PLATFORM is
'[171A]վ̨,Ҳ����̨,��������ͨ����ͣ��ʱ,���˿ͺ򳵺����³�����ʩ';

comment on column AU_PT_PLATFORM.AUDATA_ID is
'����';

comment on column AU_PT_PLATFORM.PID is
'�ο�"PT_PLATFORM"';

comment on column AU_PT_PLATFORM.POI_PID is
'�ο�"PT_POI"';

comment on column AU_PT_PLATFORM.P_LEVEL is
'ֵ��:-6~4;0��ʾ����';

comment on column AU_PT_PLATFORM.TRANSIT_FLAG is
'��¼��վ̨�Ƿ��ܺ�����վ̨��ͨ,���ܺ�����վ̨��ͨ,��ʶΪ"�ɻ���";������ͨ���κ�����վ̨,��ʶΪ"���ɻ���"';

comment on column AU_PT_PLATFORM.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_PT_PLATFORM.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_PLATFORM.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_PLATFORM.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

comment on column AU_PT_PLATFORM.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_PT_PLATFORM_ACCESS                                 */
/*==============================================================*/
create table AU_PT_PLATFORM_ACCESS  (
   AUDATA_ID            NUMBER(10)                      not null,
   RELATE_ID            NUMBER(10)                     default 0 not null,
   PLATFORM_ID          NUMBER(10)                     default 0 not null,
   ACCESS_ID            NUMBER(10)                     default 0 not null,
   AVAILABLE            NUMBER(1)                      default 1 not null
       check (AVAILABLE in (0,1)),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_PLATFORMACCESS foreign key (AUDATA_ID)
         references AU_PT_PLATFORM (AUDATA_ID)
);

comment on table AU_PT_PLATFORM_ACCESS is
'[171A]��¼վ̨���ܵ����վ̨����ڵ�֮��Ķ�Ӧ��ϵ,һ��վ̨�����ж����ڵ�';

comment on column AU_PT_PLATFORM_ACCESS.AUDATA_ID is
'[173sp1]���,����"AU_PT_PLATFORM"';

comment on column AU_PT_PLATFORM_ACCESS.RELATE_ID is
'�ο�"PT_PLATFORM_ACCESS"';

comment on column AU_PT_PLATFORM_ACCESS.PLATFORM_ID is
'[173sp1]�ο�"PT_PLATFORM"';

comment on column AU_PT_PLATFORM_ACCESS.ACCESS_ID is
'�ο�"PT_POI"';

comment on column AU_PT_PLATFORM_ACCESS.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_PLATFORM_ACCESS.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_PLATFORM_NAME                                   */
/*==============================================================*/
create table AU_PT_PLATFORM_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_PLATFORM_NAME foreign key (AUDATA_ID)
         references AU_PT_PLATFORM (AUDATA_ID)
);

comment on table AU_PT_PLATFORM_NAME is
'[171A]';

comment on column AU_PT_PLATFORM_NAME.AUDATA_ID is
'���,����"PT_PLATFORM"';

comment on column AU_PT_PLATFORM_NAME.NAME_ID is
'�ο�"PT_PLATFORM_NAME"';

comment on column AU_PT_PLATFORM_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_PT_PLATFORM_NAME.PID is
'�ο�"PT_PLATFORM"';

comment on column AU_PT_PLATFORM_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_PT_PLATFORM_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column AU_PT_PLATFORM_NAME.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column AU_PT_PLATFORM_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_PLATFORM_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_POI_PARENT                                      */
/*==============================================================*/
create table AU_PT_POI_PARENT  (
   AUDATA_ID            NUMBER(10)                      not null,
   GROUP_ID             NUMBER(10)                     default 0 not null,
   PARENT_POI_PID       NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_PT_POI_PARENT primary key (AUDATA_ID)
);

comment on table AU_PT_POI_PARENT is
'[171A]��¼վ��������֮��Ĺ�ϵ��Ϣ';

comment on column AU_PT_POI_PARENT.AUDATA_ID is
'[173sp1]����';

comment on column AU_PT_POI_PARENT.GROUP_ID is
'�ο�"PT_POI_PARENT"';

comment on column AU_PT_POI_PARENT.PARENT_POI_PID is
'�ο�"PT_POI"';

comment on column AU_PT_POI_PARENT.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_POI_PARENT.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_POI_CHILDREN                                    */
/*==============================================================*/
create table AU_PT_POI_CHILDREN  (
   AUDATA_ID            NUMBER(10),
   GROUP_ID             NUMBER(10)                     default 0 not null,
   CHILD_POI_PID        NUMBER(10)                     default 0 not null,
   RELATION_TYPE        NUMBER(1)                      default 0 not null
       check (RELATION_TYPE in (0,1,2)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_POI_CHILDREN foreign key (AUDATA_ID)
         references AU_PT_POI_PARENT (AUDATA_ID)
);

comment on table AU_PT_POI_CHILDREN is
'[171A]����POI�ĸ��ӹ�ϵ,��վ��������֮��Ĺ�ϵ��Ϣ';

comment on column AU_PT_POI_CHILDREN.AUDATA_ID is
'[173sp1]���,����"AU_PT_POI_PARENT"';

comment on column AU_PT_POI_CHILDREN.GROUP_ID is
'�ο�"PT_POI_PARENT"';

comment on column AU_PT_POI_CHILDREN.CHILD_POI_PID is
'�ο�"PT_POI"';

comment on column AU_PT_POI_CHILDREN.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_POI_CHILDREN.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_POI_NAME                                        */
/*==============================================================*/
create table AU_PT_POI_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2)),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_POI_NAME foreign key (AUDATA_ID)
         references AU_PT_POI (AUDATA_ID)
);

comment on table AU_PT_POI_NAME is
'[171A]��IX_POI_NAMEԭ����ͬ';

comment on column AU_PT_POI_NAME.AUDATA_ID is
'���,����"AU_PT_POI"';

comment on column AU_PT_POI_NAME.NAME_ID is
'�ο�"PT_POI_NAME"';

comment on column AU_PT_POI_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_PT_POI_NAME.POI_PID is
'�ο�"PT_POI"';

comment on column AU_PT_POI_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_PT_POI_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column AU_PT_POI_NAME.NIDB_PID is
'��¼����POI���Ѿ���Ʒ������ID,��ͬ��������PID��ͬ';

comment on column AU_PT_POI_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_POI_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_STRAND                                          */
/*==============================================================*/
create table AU_PT_STRAND  (
   AUDATA_ID            NUMBER(10)                      not null,
   LINE_AUDATA_ID       NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   PAIR_STRAND_PID      NUMBER(10)                     default 0 not null,
   LINE_ID              NUMBER(10)                     default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   UP_DOWN              VARCHAR2(16)                   
       check (UP_DOWN is null or (UP_DOWN in ('��','��','��','�Σ�','�ף�','�ã�','�ãΣ�','�ãף�'))),
   DISTANCE             VARCHAR2(10),
   TICKET_SYS           NUMBER(2)                      default 0 not null
       check (TICKET_SYS in (0,1,2,9)),
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
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   IMP_DATE             DATE,
   DATA_VERSION         VARCHAR2(128),
   GEO_TASK_ID          NUMBER(10)                     default 0 not null,
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   GEO_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (GEO_OPRSTATUS in (0,1,2)),
   GEO_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (GEO_CHECKSTATUS in (0,1,2)),
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_PT_STRAND primary key (AUDATA_ID)
);

comment on table AU_PT_STRAND is
'[171A]Strand,����α�,������¼ÿ����·�����г������ڲ�ͬ��ʱ��㷢���İ��,���ð�εĸ�����ϸ��Ϣ,�羭����·,��ĩ��ʱ��,���������';

comment on column AU_PT_STRAND.AUDATA_ID is
'����';

comment on column AU_PT_STRAND.LINE_AUDATA_ID is
'[173sp1]�ο�"AU_PT_LINE"';

comment on column AU_PT_STRAND.PID is
'�ο�"PT_STRAND"';

comment on column AU_PT_STRAND.LINE_ID is
'�ο�"PT_LINE"';

comment on column AU_PT_STRAND.UP_DOWN is
'��ʾ����,����,���е�,�洢Ϊȫ���ַ�';

comment on column AU_PT_STRAND.GEOMETRY is
'(1)Strand �г��켣,�������������,��ͼ���߲������,������п����ཻ
(2)�洢��"��"Ϊ��λ�ľ�γ���������';

comment on column AU_PT_STRAND.LOG is
'[173sp1]';

comment on column AU_PT_STRAND.IMP_DATE is
'��ҵPOI����ʱ,��DMS��ֵ,��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_PT_STRAND.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_PT_STRAND.GEO_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_STRAND.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_STRAND.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_STRAND_NAME                                     */
/*==============================================================*/
create table AU_PT_STRAND_NAME  (
   AUDATA_ID            NUMBER(10)                      not null,
   NAME_ID              NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                     default 0 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2,3,4)),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_STRAND_NAME foreign key (AUDATA_ID)
         references AU_PT_STRAND (AUDATA_ID)
);

comment on table AU_PT_STRAND_NAME is
'[171A]';

comment on column AU_PT_STRAND_NAME.AUDATA_ID is
'���,����"AU_PT_STRAND" ';

comment on column AU_PT_STRAND_NAME.NAME_ID is
'�ο�"PT_STRAND_NAME"';

comment on column AU_PT_STRAND_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column AU_PT_STRAND_NAME.PID is
'�ο�"PT_STRAND"';

comment on column AU_PT_STRAND_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column AU_PT_STRAND_NAME.NAME_CLASS is
'[170][172U]';

comment on column AU_PT_STRAND_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column AU_PT_STRAND_NAME.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column AU_PT_STRAND_NAME.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_STRAND_NAME.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_STRAND_PLATFORM                                 */
/*==============================================================*/
create table AU_PT_STRAND_PLATFORM  (
   AUDATA_ID            NUMBER(10)                      not null,
   STRAND_PID           NUMBER(10)                     default 0 not null,
   PLATFORM_PID         NUMBER(10)                     default 0 not null,
   SEQ_NUM              NUMBER(10)                     default 0 not null,
   INTERVAL             NUMBER(3)                      default 0 not null,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITIONFLAG          VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_STRAND_PLATFORM foreign key (AUDATA_ID)
         references AU_PT_STRAND (AUDATA_ID)
);

comment on table AU_PT_STRAND_PLATFORM is
'[171A]';

comment on column AU_PT_STRAND_PLATFORM.AUDATA_ID is
'���,����"AU_PT_STRAND" ';

comment on column AU_PT_STRAND_PLATFORM.STRAND_PID is
'�ο�"PT_STRAND"';

comment on column AU_PT_STRAND_PLATFORM.PLATFORM_PID is
'�ο�"PT_PLATFORM"';

comment on column AU_PT_STRAND_PLATFORM.SEQ_NUM is
'(1)��¼������·ĳ��Strand���ߵ�վ̨��Ϣ
(2)Ŀǰ������·��վ̨ͳһ��10000��ʼÿ�ε���10000���,��10000,20000,30000��';

comment on column AU_PT_STRAND_PLATFORM.INTERVAL is
'��λ:����';

comment on column AU_PT_STRAND_PLATFORM.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_STRAND_PLATFORM.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_SYSTEM                                          */
/*==============================================================*/
create table AU_PT_SYSTEM  (
   AUDATA_ID            NUMBER(10)                      not null,
   SYSTEM_ID            NUMBER(10)                     default 0 not null,
   COMPANY_ID           NUMBER(10)                     default 0 not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   CITY_CODE            NUMBER(6)                      default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_SYSTEMID        VARCHAR2(32),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint AUPT_COMPANY_SYSTEM foreign key (AUDATA_ID)
         references AU_PT_COMPANY (AUDATA_ID)
);

comment on table AU_PT_SYSTEM is
'[171A]����ϵͳ��һ��������˾�����������,��Ӫ���幫����·�ķ�֧��λ.ͨ����ָ������ͬһ��ϵͳ�Ĺ�����·�Ĺ��?λ,��ֱ�ӹ���,��Ӫ��ʿ/�������·�ĵ�λ';

comment on column AU_PT_SYSTEM.AUDATA_ID is
'[173sp1]���,����"AU_PT_COMPANY"';

comment on column AU_PT_SYSTEM.SYSTEM_ID is
'�ο�"PT_SYSTEM"';

comment on column AU_PT_SYSTEM.COMPANY_ID is
'�ο�"PT_COMPANY"';

comment on column AU_PT_SYSTEM.CITY_CODE is
'�洢����Ϊ4λ';

comment on column AU_PT_SYSTEM.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_SYSTEM.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_PT_TRANSFER                                        */
/*==============================================================*/
create table AU_PT_TRANSFER  (
   AUDATA_ID            NUMBER(10)                      not null,
   TRANSFER_ID          NUMBER(10)                     default 0 not null,
   TRANSFER_TYPE        NUMBER(1)                      default 1 not null
       check (TRANSFER_TYPE in (0,1)),
   POI_FIR              NUMBER(10)                     default 0 not null,
   POI_SEC              NUMBER(10)                     default 0 not null,
   PLATFORM_FIR         NUMBER(10)                     default 0 not null,
   PLATFORM_SEC         NUMBER(10)                     default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   TRANSFER_TIME        NUMBER(2)                      default 0 not null,
   EXTERNAL_FLAG        NUMBER(1)                      default 0 not null
       check (EXTERNAL_FLAG in (0,1,2)),
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(200),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   ATT_TASK_ID          NUMBER(10)                     default 0 not null,
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   ATT_OPRSTATUS        NUMBER(2)                      default 0 not null
       check (ATT_OPRSTATUS in (0,1,2)),
   ATT_CHECKSTATUS      NUMBER(2)                      default 0 not null
       check (ATT_CHECKSTATUS in (0,1,2)),
   constraint PK_AU_PT_TRANSFER primary key (AUDATA_ID)
);

comment on table AU_PT_TRANSFER is
'[171A]NaviMap����ʱ,�����·ͼ�еĻ���վ��������������������(��վ��),��Ҫ����Ӧ������֮������������������Ϊ"��վ����"�Ĺ�ϵ.
�����·ͼ�еĻ���վ��������һ������,��Ҫ�ڴ������������ܹ���ͨ��վ̨֮������������������Ϊ"վ�ڻ���"�Ĺ�ϵ.
��:�����վ�ڻ���,���˵�Ӧ����վ̨���;�����վ�任��,���˵�Ӧ��������(��վ��)';

comment on column AU_PT_TRANSFER.AUDATA_ID is
'����';

comment on column AU_PT_TRANSFER.TRANSFER_ID is
'�ο�"PT_TRANSFER"';

comment on column AU_PT_TRANSFER.TRANSFER_TYPE is
'��ֿ�վ���˺�վ�ڻ�����������:
(1)��վ����,��ﲻͬ������·����������վ��֮��Ļ���;��ʱ,���˵�һ�ͻ��˵���ֱ��ʾվ��
(2)վ�ڻ���,��ﲻͬ������·��ͬһ��վ���ڲ��Ļ���,��ʱ,���˵�һ�ͻ��˵���ֱ��ʾվ̨';

comment on column AU_PT_TRANSFER.POI_FIR is
'[173sp1]�ο�"PT_POI"';

comment on column AU_PT_TRANSFER.POI_SEC is
'[173sp1]�ο�"PT_POI"';

comment on column AU_PT_TRANSFER.PLATFORM_FIR is
'[173sp1]�ο�"PT_PLATFORM"';

comment on column AU_PT_TRANSFER.PLATFORM_SEC is
'[173sp1]�ο�"PT_PLATFORM"';

comment on column AU_PT_TRANSFER.TRANSFER_TIME is
'�Է���Ϊ��λ,��¼�˿ͻ���ʱ������Ҫ��ʱ��';

comment on column AU_PT_TRANSFER.EXTERNAL_FLAG is
'ÿһ���վ���˹�ϵ,����Ҫ����"�ⲿ��ʶ"����,���������˿ͻ���ʱ�Ƿ���Ҫ�ߵ�վ���ⲿ.����������֮����ר�û���ͨ��ʱ,"�ⲿ��ʶ"����Ϊ"��";��û��ר��ͨ��,�˿���Ҫ�ߵ�վ�����滻��,"�ⲿ��ʶ"����Ϊ"��".';

comment on column AU_PT_TRANSFER.ATT_TASK_ID is
'DMS������ҵ��ҵ���ʱ��ֵ(�����ҵLOG)';

comment on column AU_PT_TRANSFER.FIELD_TASK_ID is
'��ҵPOI����DMSʱ��ֵ ';

/*==============================================================*/
/* Table: AU_RECEIVE                                            */
/*==============================================================*/
create table AU_RECEIVE  (
   ID                   NUMBER(10)                      not null,
   TITLE                VARCHAR2(50),
   CONTENT              VARCHAR2(1000),
   RECEIVE_PERSON       NUMBER(10)                     default 0 not null,
   RECEIVE_TIME         TIMESTAMP,
   RECEIVE_TYPE         NUMBER(1)                      default 0 not null
       check (RECEIVE_TYPE in (0,1,2,3)),
   DATA_ID              VARCHAR2(32),
   constraint PK_AU_RECEIVE primary key (ID)
);

comment on column AU_RECEIVE.ID is
'����';

comment on column AU_RECEIVE.TITLE is
'���ظ�����';

comment on column AU_RECEIVE.DATA_ID is
'����"AU_SPECIALCASE"��"TB_ABSTRACT_INFO"��"COMMUNICATION"';

/*==============================================================*/
/* Table: AU_SERIESPHOTO                                        */
/*==============================================================*/
create table AU_SERIESPHOTO  (
   PHOTO_ID             NUMBER(10)                      not null,
   PHOTO_GROUPID        NUMBER(10)                     default 0 not null,
   CAMERA_ID            NUMBER(2)                      default 1 not null,
   GEOMETRY             SDO_GEOMETRY,
   ANGLE                NUMBER(8,5)                    default 0 not null,
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   FILE_TYPE            VARCHAR2(32),
   "SIZE"               VARCHAR2(256),
   FORMAT               VARCHAR2(256),
   IMP_WORKER           NUMBER(4)                      default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   constraint PK_AU_SERIESPHOTO primary key (PHOTO_ID)
);

comment on column AU_SERIESPHOTO.PHOTO_ID is
'����';

comment on column AU_SERIESPHOTO.PHOTO_GROUPID is
'[170]����"PHOTO_ID"��Ϊ���';

comment on column AU_SERIESPHOTO.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_SERIESPHOTO.ANGLE is
'����ļн�,-180~180��';

comment on column AU_SERIESPHOTO.DAY_TIME is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_SERIESPHOTO.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_SERIESPHOTO.TASK_ID is
'��¼��ҵ��������';

comment on column AU_SERIESPHOTO.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column AU_SERIESPHOTO.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column AU_SERIESPHOTO.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column AU_SERIESPHOTO.URL_FILE is
'�洢���·����,��\Data\SeriesPhoto\';

comment on column AU_SERIESPHOTO.FILE_NAME is
'[170]�ļ���(����չ��)';

comment on column AU_SERIESPHOTO.FILE_TYPE is
'[170]';

comment on column AU_SERIESPHOTO."SIZE" is
'[170]64*64,32*32,16*16';

comment on column AU_SERIESPHOTO.FORMAT is
'[170]WAV,ADP';

comment on column AU_SERIESPHOTO.IMP_WORKER is
'[170]�ο�"BI_PERSON"';

comment on column AU_SERIESPHOTO.IMP_VERSION is
'[170]';

comment on column AU_SERIESPHOTO.IMP_DATE is
'[170]��ʽ"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: AU_SPECIALCASE                                        */
/*==============================================================*/
create table AU_SPECIALCASE  (
   SPECIALCASE_ID       NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   STATUS               NUMBER(1)                      default 1 not null
       check (STATUS in (0,1)),
   TYPE                 NUMBER(5)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5)),
   RANK                 NUMBER(1)                      default 0 not null
       check (RANK in (0,1,2,3)),
   DAY_TIME             DATE,
   WORKER               NUMBER(4)                      default 0 not null,
   DESCRIPT             VARCHAR2(2000),
   constraint PK_AU_SPECIALCASE primary key (SPECIALCASE_ID)
);

comment on column AU_SPECIALCASE.SPECIALCASE_ID is
'����';

comment on column AU_SPECIALCASE.GEOMETRY is
'�������õĵ��?Χ���λ';

comment on column AU_SPECIALCASE.STATUS is
'�Ƿ���Ч';

comment on column AU_SPECIALCASE.TYPE is
'�����������������⴦�?';

comment on column AU_SPECIALCASE.RANK is
'����ҵ���һ������';

comment on column AU_SPECIALCASE.DAY_TIME is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_SPECIALCASE.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_SPECIALCASE.DESCRIPT is
'�����˵������(��ϵͳ��дʱ��,��Ա���������)';

/*==============================================================*/
/* Table: AU_SPECIALCASE_IMAGE                                  */
/*==============================================================*/
create table AU_SPECIALCASE_IMAGE  (
   SPECIALCASE_ID       NUMBER(10)                      not null,
   FILENAME             VARCHAR2(254),
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint AUSPECIALCASE_IMAGE foreign key (SPECIALCASE_ID)
         references AU_SPECIALCASE (SPECIALCASE_ID)
);

comment on column AU_SPECIALCASE_IMAGE.SPECIALCASE_ID is
'���,����"AU_SPECIALCASE"';

comment on column AU_SPECIALCASE_IMAGE.FILENAME is
'����ͼƬ���ļ����(����׺��),��"256.bmp"';

comment on column AU_SPECIALCASE_IMAGE.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column AU_SPECIALCASE_IMAGE.URL_FILE is
'��Ƭ�ļ��洢�ı������·����,��\Data\Photo\';

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
       check (HAVE_ROADNAME in (0,1,9)),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,2)),
   MATCH_ID             NUMBER(10)                     default 0 not null,
   MANAGER_ID           varchar2(200),
   EXT_FLAG             VARCHAR2(200),
   EXT_OBJ              VARCHAR2(200),
   EXT_TIME             VARCHAR2(200),
   IMP_TASK_NAME        VARCHAR2(400)
);

/*==============================================================*/
/* Table: AU_TOPOIMAGE                                          */
/*==============================================================*/
create table AU_TOPOIMAGE  (
   IMAGE_ID             NUMBER(10)                      not null,
   IMAGE_NAME           VARCHAR2(100),
   DESCRIPT             VARCHAR2(500),
   constraint PK_AU_TOPOIMAGE primary key (IMAGE_ID)
);

comment on column AU_TOPOIMAGE.IMAGE_ID is
'����';

/*==============================================================*/
/* Table: AU_VIDEO                                              */
/*==============================================================*/
create table AU_VIDEO  (
   VIDEO_ID             NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   ANGLE                NUMBER(8,5)                    default 0 not null,
   WORKER               NUMBER(4)                      default 0 not null,
   START_TIME           DATE,
   END_TIME             DATE,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   FILE_NAME            VARCHAR2(254),
   FILE_TYPE            VARCHAR2(32),
   "SIZE"               VARCHAR2(256),
   FORMAT               VARCHAR2(256),
   IMP_WORKER           NUMBER(4)                      default 0 not null,
   IMP_VERSION          VARCHAR2(64),
   IMP_DATE             DATE,
   MESH_ID              NUMBER(6)                      default 0 not null,
   constraint PK_AU_VIDEO primary key (VIDEO_ID)
);

comment on column AU_VIDEO.VIDEO_ID is
'����';

comment on column AU_VIDEO.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column AU_VIDEO.ANGLE is
'����ļн�,-180~180��';

comment on column AU_VIDEO.WORKER is
'�ο�"BI_PERSON"';

comment on column AU_VIDEO.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column AU_VIDEO.URL_FILE is
'�洢���·����,��\Data\Video\';

comment on column AU_VIDEO.FILE_NAME is
'[170]�ļ���(����չ��)';

comment on column AU_VIDEO.FILE_TYPE is
'[170]';

comment on column AU_VIDEO."SIZE" is
'[170]';

comment on column AU_VIDEO.FORMAT is
'[170]WAV,ADP';

comment on column AU_VIDEO.IMP_WORKER is
'[170]�ο�"BI_PERSON"';

comment on column AU_VIDEO.IMP_VERSION is
'[170]';

comment on column AU_VIDEO.IMP_DATE is
'[170]��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column AU_VIDEO.MESH_ID is
'[170]';

/*==============================================================*/
/* Table: AU_WHITEBOARD                                         */
/*==============================================================*/
create table AU_WHITEBOARD  (
   WHITEBOARD_ID        NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   STYLE                NUMBER(1)                      default 0 not null
       check (STYLE in (0,1)),
   COLOR                VARCHAR2(10)                    not null,
   WIDTH                NUMBER(2)                      default 1 not null,
   constraint PK_AU_WHITEBOARD primary key (WHITEBOARD_ID)
);

comment on table AU_WHITEBOARD is
'��¼�װ���Ϣ';

comment on column AU_WHITEBOARD.WHITEBOARD_ID is
'����';

comment on column AU_WHITEBOARD.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������';

comment on column AU_WHITEBOARD.STYLE is
'ʵ��,����';

comment on column AU_WHITEBOARD.COLOR is
'[173sp1]';

/*==============================================================*/
/* Table: AU_WHITEBOARD_PARAMETER                               */
/*==============================================================*/
create table AU_WHITEBOARD_PARAMETER  (
   WHITEBOARD_ID        NUMBER(10)                      not null,
   NAME                 VARCHAR2(32),
   PARAMETER            VARCHAR2(32),
   DESCRIPTION          VARCHAR2(200),
   constraint AUWHITEBOARD_PARAMETER foreign key (WHITEBOARD_ID)
         references AU_WHITEBOARD (WHITEBOARD_ID)
);

comment on table AU_WHITEBOARD_PARAMETER is
'��¼�װ������Ϣ';

comment on column AU_WHITEBOARD_PARAMETER.WHITEBOARD_ID is
'���,����"AU_WHITEBOARD"';

/*==============================================================*/
/* Table: BI_PERSON                                             */
/*==============================================================*/
create table BI_PERSON  (
   PERSON_ID            NUMBER(4)                       not null,
   NAME                 VARCHAR2(16),
   GENDER               NUMBER(1)                      default 0 not null
       check (GENDER in (0,1)),
   DEPARTMENT           VARCHAR2(100),
   WORK_GROUP           VARCHAR2(64),
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_PERSON primary key (PERSON_ID)
);

comment on column BI_PERSON.PERSON_ID is
'����';

comment on column BI_PERSON.GENDER is
'��,Ů';

comment on column BI_PERSON.DEPARTMENT is
'���з���';

comment on column BI_PERSON.WORK_GROUP is
'�������';

/*==============================================================*/
/* Table: BI_ROLE                                               */
/*==============================================================*/
create table BI_ROLE  (
   ROLE_ID              NUMBER(2)                       not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3)),
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_ROLE primary key (ROLE_ID)
);

comment on column BI_ROLE.ROLE_ID is
'����';

comment on column BI_ROLE.TYPE is
'��ҵ,��ҵ,Ʒ�ʼ��,Ʒ�ʼ���';

/*==============================================================*/
/* Table: BI_PERSON_ROLE                                        */
/*==============================================================*/
create table BI_PERSON_ROLE  (
   PERSON_ID            NUMBER(4)                       not null,
   ROLE_ID              NUMBER(2)                       not null,
   constraint BIPERSON_ROLE foreign key (PERSON_ID)
         references BI_PERSON (PERSON_ID),
   constraint BIROLE_PERSON foreign key (ROLE_ID)
         references BI_ROLE (ROLE_ID)
);

comment on column BI_PERSON_ROLE.PERSON_ID is
'���,����"BI_PERSON"';

comment on column BI_PERSON_ROLE.ROLE_ID is
'���,����"BI_ROLE"';

/*==============================================================*/
/* Table: BI_POWER                                              */
/*==============================================================*/
create table BI_POWER  (
   POWER_ID             NUMBER(2)                       not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3,4)),
   DESCRIPT             VARCHAR2(32),
   constraint PK_BI_POWER primary key (POWER_ID)
);

comment on column BI_POWER.POWER_ID is
'����';

comment on column BI_POWER.TYPE is
'Ҫ������,�޸�,ɾ��,��ѯ,�����';

/*==============================================================*/
/* Table: BI_ROLE_POWER                                         */
/*==============================================================*/
create table BI_ROLE_POWER  (
   ROLE_ID              NUMBER(2)                       not null,
   POWER_ID             NUMBER(2)                       not null,
   constraint BIROLE_POWER foreign key (ROLE_ID)
         references BI_ROLE (ROLE_ID),
   constraint BIPOWER_ROLE foreign key (POWER_ID)
         references BI_POWER (POWER_ID)
);

comment on column BI_ROLE_POWER.ROLE_ID is
'���,����"BI_ROLE"';

comment on column BI_ROLE_POWER.POWER_ID is
'���,����"BI_POWER"';

/*==============================================================*/
/* Table: BI_TASK                                               */
/*==============================================================*/
create table BI_TASK  (
   TASK_ID              NUMBER(10)                      not null,
   MAN_ID               NUMBER(4)                      default 0 not null,
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
'����,�����Ψһ˳����';

comment on column BI_TASK.MAN_ID is
'���������ҵԱ';

comment on column BI_TASK.FOCUS_ITEM is
'ȫҪ��,ĳ��ҵ��Ŀ��';

comment on column BI_TASK."RESOURCE" is
'��������,�豸,�����Ա,��ҵ����������;�Լ�Ӱ��,�����ļ�,ʾ��ͼ,Ԥ�����,��ҳ��';

comment on column BI_TASK.TASK_TYPE is
'�ֳ���������,�ֳ��滮·������,���ڳɹ�¼������,һ�廯����,�鱨����,�ʼ�����,�������,��������,��������,��ͻ��������,�ӱ�����,��ҵ����.';

comment on column BI_TASK.WORK_MODE is
'������ҵ,������ҵ,ȫ��Ƭ������ҵ,��ϸ���ֳ��ɼ���ҵ';

comment on column BI_TASK.TASK_POWER is
'������������,��ҵԱ��ӵ�е�Ȩ��;����ֻ��������ҵ,������������޸ĵ�.�Լ���������޸ĵ���ݲ�(��·,������)POI,��·,�����Ķ�д,ֻ��';

comment on column BI_TASK.GEOMETRY is
'��������ĵ���Χ';

comment on column BI_TASK.ASSIGN_TIME is
'������´�ʱ��
��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column BI_TASK.START_TIME is
'�����Ԥ�ƿ�ʼʱ��
��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column BI_TASK.END_TIME is
'����Ľ�ֹʱ��
��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column BI_TASK.DESCRIPT is
'�������񱳾�,ע������ȵ�����';

comment on column BI_TASK.SCHEDULE is
'�����չԤ��,������̽��й���';

comment on column BI_TASK.STATUS is
'��ͨ��ҵ�����״̬����:��ҵ��,�ʼ���,�ʼ췵��,���(��ҵ��Ա)�ʼ���ҵ�����״̬����:�ʼ���,�ʼ췵���ȴ�,���(�ʼ���Ա)';

comment on column BI_TASK.RESUME is
'����:���,�ѷ���,�����,��ȡ��(���������Ա)';

comment on column BI_TASK.TASK_URL is
'������ݵĲ���·��';

comment on column BI_TASK.DATABASE_VER is
'����ݿ�汾:09��,10��';

comment on column BI_TASK.URL_INTERFACE is
'��ݹ���ӿ�:URL����';

/*==============================================================*/
/* Table: CK_EXCEPTION                                          */
/*==============================================================*/
create table CK_EXCEPTION  (
   EXCEPTION_ID         NUMBER(10)                      not null,
   RULE_ID              VARCHAR2(100),
   TASK_NAME            VARCHAR2(50),
   STATUS               NUMBER(2)                      default 0 not null
       check (STATUS in (0,1,2,3)),
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
       check (SCOPE_FLAG in (1,2,3)),
   PROVINCE_NAME        VARCHAR2(60),
   MAP_SCALE            NUMBER(2)                      default 0 not null
       check (MAP_SCALE in (0,1,2,3)),
   RESERVED             VARCHAR2(1000),
   EXTENDED             VARCHAR2(1000),
   TASK_ID              VARCHAR2(500),
   QA_TASK_ID           VARCHAR2(500),
   QA_STATUS            NUMBER(2)                      default 2 not null
       check (QA_STATUS in (1,2)),
   WORKER               VARCHAR2(500),
   QA_WORKER            VARCHAR2(500),
   MEMO_1               VARCHAR2(500),
   MEMO_2               VARCHAR2(500),
   MEMO_3               VARCHAR2(500),
   MD5_CODE             VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CK_EXCEPTION primary key (EXCEPTION_ID)
);

comment on column CK_EXCEPTION.EXCEPTION_ID is
'����';

comment on column CK_EXCEPTION.RULE_ID is
'[173sp1]�ο�"CK_RULE"';

comment on column CK_EXCEPTION.STATUS is
'[1802A]';

comment on column CK_EXCEPTION.GEOMETRY is
'����WKT ��ʽ';

comment on column CK_EXCEPTION.MEMO is
'[1802A]';

comment on column CK_EXCEPTION.CREATE_DATE is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column CK_EXCEPTION.UPDATE_DATE is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column CK_EXCEPTION.QA_TASK_ID is
'[200A]';

comment on column CK_EXCEPTION.U_RECORD is
'�������±�ʶ';

comment on column CK_EXCEPTION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CK_RULE                                               */
/*==============================================================*/
create table CK_RULE  (
   RULE_ID              VARCHAR2(100)                   not null,
   NAME                 VARCHAR2(254),
   CATEGORY             VARCHAR2(50),
   DESCRIPTION          CLOB                            not null,
   RANK                 NUMBER(2)                      default 0 not null,
   NQL                  CLOB                            not null,
   ERROR_DESC           VARCHAR2(254),
   RESOLUTION           VARCHAR2(254),
   PARA_LIST            CLOB                            not null,
   URL                  VARCHAR2(254),
   CREATE_DATE          DATE,
   constraint PK_CK_RULE primary key (RULE_ID)
);

comment on column CK_RULE.RULE_ID is
'[173sp1]����';

comment on column CK_RULE.CREATE_DATE is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: CMG_BUILDING                                          */
/*==============================================================*/
create table CMG_BUILDING  (
   PID                  NUMBER(10)                      not null,
   KIND                 VARCHAR2(8)                    
       check (KIND is null or (KIND in ('1001','1002','2001','3001','3002','4001','4002','4003','5001','5002','5003','5004','5005','6001','6002','6003','7001','8001','8002','8003','9001','9002','9003','9004','1101','1102','1103','1201','1202','1203','1204','1301','1302','1401','1402','1403','1501','1502','1503','1504','1505','1601'))),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CMG_BUILDING primary key (PID)
);

comment on column CMG_BUILDING.PID is
'����';

comment on column CMG_BUILDING.KIND is
'����,����¥,����¥,�����';

comment on column CMG_BUILDING.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDING.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDFACE                                         */
/*==============================================================*/
create table CMG_BUILDFACE  (
   FACE_PID             NUMBER(10)                      not null,
   BUILDING_PID         NUMBER(10)                      not null,
   MASSING              NUMBER(1)                      default 0 not null
       check (MASSING in (0,1)),
   HEIGHT               NUMBER(5,1)                    default 0 not null,
   HEIGHT_ACURACY       NUMBER(3,1)                    default 0.5 not null
       check (HEIGHT_ACURACY in (0,0.5,1,5,10)),
   HEIGHT_SOURCE        NUMBER(2)                      default 1 not null
       check (HEIGHT_SOURCE in (1,2,3,4,5)),
   DATA_SOURCE          NUMBER(2)                      default 3 not null
       check (DATA_SOURCE in (1,2,3,4)),
   WALL_MATERIAL        NUMBER(2)                      default 1 not null
       check (WALL_MATERIAL in (1,2,3,4,5,6,7,8,9)),
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   CREATE_TIME          DATE,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CMG_BUILDFACE primary key (FACE_PID),
   constraint CMGBUILDFACE_BUILDING foreign key (BUILDING_PID)
         references CMG_BUILDING (PID)
);

comment on column CMG_BUILDFACE.FACE_PID is
'����';

comment on column CMG_BUILDFACE.BUILDING_PID is
'���,����"CMG_BUILDING"';

comment on column CMG_BUILDFACE.HEIGHT is
'[172U]';

comment on column CMG_BUILDFACE.HEIGHT_ACURACY is
'��λ:��';

comment on column CMG_BUILDFACE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column CMG_BUILDFACE.AREA is
'��λ:ƽ����';

comment on column CMG_BUILDFACE.PERIMETER is
'��λ:��';

comment on column CMG_BUILDFACE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column CMG_BUILDFACE.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDFACE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDFACE_TENANT                                  */
/*==============================================================*/
create table CMG_BUILDFACE_TENANT  (
   FACE_PID             NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   FLAG                 NUMBER(1)                      default 0 not null
       check (FLAG in (0,1)),
   TEL                  VARCHAR2(64),
   X                    NUMBER(10,5)                   default 0 not null,
   Y                    NUMBER(10,5)                   default 0 not null,
   NAME                 VARCHAR2(200),
   FLOOR                VARCHAR2(200),
   SRC_FLAG             NUMBER(1)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMGBUILDFACE_TENANT foreign key (FACE_PID)
         references CMG_BUILDFACE (FACE_PID)
);

comment on table CMG_BUILDFACE_TENANT is
'[170]';

comment on column CMG_BUILDFACE_TENANT.FACE_PID is
'���,����"CMG_BUILDFACE"';

comment on column CMG_BUILDFACE_TENANT.POI_PID is
'��¼�⻧����ⷽ��POI ����,��POI ʱ��¼Ϊ0';

comment on column CMG_BUILDFACE_TENANT.TEL is
'��:86-010-82306399 ';

comment on column CMG_BUILDFACE_TENANT.X is
'��Ӧ���õ���ʾ���';

comment on column CMG_BUILDFACE_TENANT.Y is
'��Ӧ���õ���ʾ���';

comment on column CMG_BUILDFACE_TENANT.NAME is
'���û��ڲ��⻧�����';

comment on column CMG_BUILDFACE_TENANT.FLOOR is
'���û��⻧��¥����Ϣ';

comment on column CMG_BUILDFACE_TENANT.SRC_FLAG is
'ע:���CMG_BUILDING_3DMODEL �м�¼��ֵ0,����ֵ1';

comment on column CMG_BUILDFACE_TENANT.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDFACE_TENANT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDNODE                                         */
/*==============================================================*/
create table CMG_BUILDNODE  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CMG_BUILDNODE primary key (NODE_PID)
);

comment on column CMG_BUILDNODE.NODE_PID is
'����';

comment on column CMG_BUILDNODE.FORM is
'��,ͼ����,�ǵ�';

comment on column CMG_BUILDNODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column CMG_BUILDNODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column CMG_BUILDNODE.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDNODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDLINK                                         */
/*==============================================================*/
create table CMG_BUILDLINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (0,1)),
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CMG_BUILDLINK primary key (LINK_PID),
   constraint CMGBUILDLINK_SNODE foreign key (S_NODE_PID)
         references CMG_BUILDNODE (NODE_PID),
   constraint CMGBUILDLINK_ENODE foreign key (E_NODE_PID)
         references CMG_BUILDNODE (NODE_PID)
);

comment on column CMG_BUILDLINK.LINK_PID is
'����';

comment on column CMG_BUILDLINK.S_NODE_PID is
'���,����"CM_BUILDNODE"';

comment on column CMG_BUILDLINK.E_NODE_PID is
'���,����"CM_BUILDNODE"';

comment on column CMG_BUILDLINK.KIND is
'�����߻�����߽���';

comment on column CMG_BUILDLINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column CMG_BUILDLINK.LENGTH is
'��λ:��';

comment on column CMG_BUILDLINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column CMG_BUILDLINK.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDLINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDFACE_TOPO                                    */
/*==============================================================*/
create table CMG_BUILDFACE_TOPO  (
   FACE_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMGBUILDFACE_LINKS foreign key (FACE_PID)
         references CMG_BUILDFACE (FACE_PID),
   constraint CMGBUILDFACE_LINK foreign key (LINK_PID)
         references CMG_BUILDLINK (LINK_PID)
);

comment on table CMG_BUILDFACE_TOPO is
'��¼����������Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column CMG_BUILDFACE_TOPO.FACE_PID is
'���,����"CMG_BUILDFACE"';

comment on column CMG_BUILDFACE_TOPO.SEQ_NUM is
'����ʱ�뷽��,��1 ��ʼ�������';

comment on column CMG_BUILDFACE_TOPO.LINK_PID is
'���,����"CMG_BUILDLINK"';

comment on column CMG_BUILDFACE_TOPO.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDFACE_TOPO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDING_3DICON                                   */
/*==============================================================*/
create table CMG_BUILDING_3DICON  (
   BUILDING_PID         NUMBER(10)                      not null,
   WIDTH                NUMBER(5)                      default 64 not null,
   HEIGHT               NUMBER(5)                      default 64 not null,
   ICON_NAME            VARCHAR2(100),
   ALPHA_NAME           VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMGBUILDING_3DICON foreign key (BUILDING_PID)
         references CMG_BUILDING (PID)
);

comment on table CMG_BUILDING_3DICON is
'�������3DLandMarkͼ���';

comment on column CMG_BUILDING_3DICON.BUILDING_PID is
'���,����"CMG_BUILDING"';

comment on column CMG_BUILDING_3DICON.WIDTH is
'��λ:����,Ĭ��Ϊ64';

comment on column CMG_BUILDING_3DICON.HEIGHT is
'��λ:����,Ĭ��Ϊ64';

comment on column CMG_BUILDING_3DICON.ICON_NAME is
'�ο�"AU_MULTIMEDIA"��"NAME"';

comment on column CMG_BUILDING_3DICON.ALPHA_NAME is
'�ο�"AU_MULTIMEDIA"��"NAME",TGA ��ʽ';

comment on column CMG_BUILDING_3DICON.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDING_3DICON.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDING_3DMODEL                                  */
/*==============================================================*/
create table CMG_BUILDING_3DMODEL  (
   MODEL_ID             NUMBER(10)                     default 0 not null,
   BUILDING_PID         NUMBER(10)                      not null,
   RESOLUTION           NUMBER(1)                      default 0 not null
       check (RESOLUTION in (0,1,2)),
   MODEL_NAME           VARCHAR2(100),
   MATERIAL_NAME        VARCHAR2(100),
   TEXTURE_NAME         VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CMG_BUILDING_3DMODEL primary key (MODEL_ID),
   constraint CMGBUILDING_3DMODEL foreign key (BUILDING_PID)
         references CMG_BUILDING (PID)
);

comment on table CMG_BUILDING_3DMODEL is
'�������3DLandMarkģ�ͱ�';

comment on column CMG_BUILDING_3DMODEL.MODEL_ID is
'[180U]����';

comment on column CMG_BUILDING_3DMODEL.BUILDING_PID is
'���,����"CMG_BUILDING"';

comment on column CMG_BUILDING_3DMODEL.RESOLUTION is
'��,��,��';

comment on column CMG_BUILDING_3DMODEL.MODEL_NAME is
'[170]';

comment on column CMG_BUILDING_3DMODEL.MATERIAL_NAME is
'[170]';

comment on column CMG_BUILDING_3DMODEL.TEXTURE_NAME is
'[170]MTL ��ʽ';

comment on column CMG_BUILDING_3DMODEL.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDING_3DMODEL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDING_NAME                                     */
/*==============================================================*/
create table CMG_BUILDING_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   BUILDING_PID         NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')),
   FULL_NAME            VARCHAR2(256),
   BASE_NAME            VARCHAR2(256),
   BUILD_NUMBER         VARCHAR2(64),
   FULL_NAME_PHONETIC   VARCHAR2(1000),
   BASE_NAME_PHONETIC   VARCHAR2(1000),
   BUILD_NUM_PHONETIC   VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CMG_BUILDING_NAME primary key (NAME_ID),
   constraint CMGBUILDING_NAME foreign key (BUILDING_PID)
         references CMG_BUILDING (PID)
);

comment on column CMG_BUILDING_NAME.NAME_ID is
'[170]����';

comment on column CMG_BUILDING_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column CMG_BUILDING_NAME.BUILDING_PID is
'���,����"CMG_BUILDING"';

comment on column CMG_BUILDING_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column CMG_BUILDING_NAME.FULL_NAME_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.BASE_NAME_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.BUILD_NUM_PHONETIC is
'[171U]';

comment on column CMG_BUILDING_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ
ע:
(1)BUA ȡֵ0~1
(2)����ȡֵ0';

comment on column CMG_BUILDING_NAME.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDING_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (SIDE in (0,1,2,3)),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   ADMIN_REAL           NUMBER(6)                      default 0 not null,
   IMPORTANCE           NUMBER(1)                      default 0 not null
       check (IMPORTANCE in (0,1)),
   CHAIN                VARCHAR2(12),
   AIRPORT_CODE         VARCHAR2(3),
   ACCESS_FLAG          NUMBER(2)                      default 0 not null
       check (ACCESS_FLAG in (0,1,2)),
   OPEN_24H             NUMBER(1)                      default 0 not null
       check (OPEN_24H in (0,1,2)),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POST_CODE            VARCHAR2(6),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   FIELD_STATE          VARCHAR2(500),
   LABEL                VARCHAR2(500),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,9)),
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
       check (VERIFIED_FLAG in (0,1,2,3,9)),
   COLLECT_TIME         VARCHAR2(15),
   GEO_ADJUST_FLAG      NUMBER(1)                      default 9 not null
       check (GEO_ADJUST_FLAG in (0,1,9)),
   FULL_ATTR_FLAG       NUMBER(1)                      default 9 not null
       check (FULL_ATTR_FLAG in (0,1,9)),
   OLD_X_GUIDE          NUMBER(10,5)                   default 0 not null,
   OLD_Y_GUIDE          NUMBER(10,5)                   default 0 not null,
   TRUCK_FLAG           NUMBER(1)                      default 0 not null
       check (TRUCK_FLAG in (0,1,2)),
   "LEVEL"              VARCHAR2(2)                    
       check ("LEVEL" is null or ("LEVEL" in ('A','B1','B2','B3','B4','C'))),
   SPORTS_VENUE         VARCHAR2(3),
   INDOOR               NUMBER(1)                      default 0 not null
       check (INDOOR in (0,1)),
   VIP_FLAG             VARCHAR2(10),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI primary key (PID)
);

comment on column IX_POI.PID is
'����';

comment on column IX_POI.KIND_CODE is
'�ο�"IX_POI_CODE"';

comment on column IX_POI.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������,����POI��ʾ�ͼ���Link���ҹ�ϵ
';

comment on column IX_POI.LINK_PID is
'�ο�"RD_LINK"';

comment on column IX_POI.SIDE is
'��¼POIλ�����·Link��,�����Ҳ�';

comment on column IX_POI.NAME_GROUPID is
'[173sp2]�ο�"RD_NAME"';

comment on column IX_POI.ROAD_FLAG is
'[170]';

comment on column IX_POI.PMESH_ID is
'[171A]ÿ����ҵ��POI �ڳɹ���е�һ����LINK ������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column IX_POI.IMPORTANCE is
'��¼���·����POIΪ��Ҫ,��IMPORTANCEΪ1,����Ϊ0
(1)ӵ�й�ʽ���ۿڵĻ�
(2)������ξ������ĵȼ�Ϊ3A,4A,5A�ķ羰��
(3)�����Ļ��Ų�';

comment on column IX_POI.CHAIN is
'[171U]��Ҫ���������Ǳ��ݺͼ���վ';

comment on column IX_POI.ACCESS_FLAG is
'[170]';

comment on column IX_POI.OPEN_24H is
'[171U]';

comment on column IX_POI.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_POI.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_POI.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_POI.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column IX_POI.RESERVED is
'[181A]';

comment on column IX_POI.STATE is
'[170]';

comment on column IX_POI.FIELD_STATE is
'[170]�����,�ĵ�ַ,�ķ���';

comment on column IX_POI.LABEL is
'[181U]��¼·,ˮ,�̵�,�����շ�,˫���շ�,��ʾλ��,24Сʱ�����';

comment on column IX_POI.TYPE is
'[170]';

comment on column IX_POI.ADDRESS_FLAG is
'��־POI ��ַ(IX_POI_ADDRESS)������';

comment on column IX_POI.EX_PRIORITY is
'[171A]��ȡ�����ȼ���(����ΪA1~A11;����ΪB2~B5)';

comment on column IX_POI.EDITION_FLAG is
'��¼���������ҵ������ҵ�޸�,����,ɾ��ȱ�־';

comment on column IX_POI.OLD_BLOCKCODE is
'ԭ�ṹ�е�"OLD����"';

comment on column IX_POI.OLD_KIND is
'[170]';

comment on column IX_POI.POI_NUM is
'��¼����NIDB��POI���';

comment on column IX_POI.TASK_ID is
'[170]��¼��ҵ��������';

comment on column IX_POI.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column IX_POI.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column IX_POI.U_RECORD is
'�������±�ʶ';

comment on column IX_POI.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDING_POI                                      */
/*==============================================================*/
create table CMG_BUILDING_POI  (
   BUILDING_PID         NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMGBUILDING_POI foreign key (BUILDING_PID)
         references CMG_BUILDING (PID),
   constraint IXPOI_CMGBUILDINGPOI foreign key (POI_PID)
         references IX_POI (PID)
);

comment on column CMG_BUILDING_POI.BUILDING_PID is
'���,����"CMG_BUILDING"';

comment on column CMG_BUILDING_POI.POI_PID is
'���,����"IX_POI"';

comment on column CMG_BUILDING_POI.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDING_POI.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDLINK_MESH                                    */
/*==============================================================*/
create table CMG_BUILDLINK_MESH  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMGBUILDLINK_MESH foreign key (LINK_PID)
         references CMG_BUILDLINK (LINK_PID)
);

comment on column CMG_BUILDLINK_MESH.LINK_PID is
'���,����"CMG_BUILDLINK"';

comment on column CMG_BUILDLINK_MESH.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDLINK_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CMG_BUILDNODE_MESH                                    */
/*==============================================================*/
create table CMG_BUILDNODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMGBUILDNODE_MESH foreign key (NODE_PID)
         references CMG_BUILDNODE (NODE_PID)
);

comment on column CMG_BUILDNODE_MESH.NODE_PID is
'���,����"CMG_BUILDNODE"';

comment on column CMG_BUILDNODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column CMG_BUILDNODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDING                                           */
/*==============================================================*/
create table CM_BUILDING  (
   PID                  NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CM_BUILDING primary key (PID)
);

comment on column CM_BUILDING.PID is
'����';

comment on column CM_BUILDING.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDING.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDFACE                                          */
/*==============================================================*/
create table CM_BUILDFACE  (
   FACE_PID             NUMBER(10)                      not null,
   BUILDING_PID         NUMBER(10)                      not null,
   KIND                 VARCHAR2(8)                    
       check (KIND is null or (KIND in ('7110169','7110194','7110193','7110010','7110022','7110021','7110090','7110080','7110071','7110167','7110070','7110072','7110060','7110040','7110041','7110050','7110195','7110255'))),
   MASSING              NUMBER(1)                      default 0 not null
       check (MASSING in (0,1)),
   HEIGHT               NUMBER(3)                      default 0 not null,
   LANDMARK_CODE        VARCHAR2(16),
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CM_BUILDFACE primary key (FACE_PID),
   constraint CMBUILDFACE_BUILDING foreign key (BUILDING_PID)
         references CM_BUILDING (PID)
);

comment on column CM_BUILDFACE.FACE_PID is
'����';

comment on column CM_BUILDFACE.BUILDING_PID is
'���,����"CM_BUILDING"';

comment on column CM_BUILDFACE.KIND is
'����,����¥,����¥,�����';

comment on column CM_BUILDFACE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column CM_BUILDFACE.AREA is
'��λ:ƽ����';

comment on column CM_BUILDFACE.PERIMETER is
'��λ:��';

comment on column CM_BUILDFACE.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDFACE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDNODE                                          */
/*==============================================================*/
create table CM_BUILDNODE  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CM_BUILDNODE primary key (NODE_PID)
);

comment on column CM_BUILDNODE.NODE_PID is
'����';

comment on column CM_BUILDNODE.FORM is
'��,ͼ����,�ǵ�';

comment on column CM_BUILDNODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column CM_BUILDNODE.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDNODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDLINK                                          */
/*==============================================================*/
create table CM_BUILDLINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (0,1)),
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_CM_BUILDLINK primary key (LINK_PID),
   constraint CMBUILDLINK_SNODE foreign key (S_NODE_PID)
         references CM_BUILDNODE (NODE_PID),
   constraint CMBUILDLINK_ENODE foreign key (E_NODE_PID)
         references CM_BUILDNODE (NODE_PID)
);

comment on column CM_BUILDLINK.LINK_PID is
'����';

comment on column CM_BUILDLINK.S_NODE_PID is
'���,����"CM_BUILDNODE"';

comment on column CM_BUILDLINK.E_NODE_PID is
'���,����"CM_BUILDNODE"';

comment on column CM_BUILDLINK.KIND is
'�����߻�����߽���';

comment on column CM_BUILDLINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column CM_BUILDLINK.LENGTH is
'��λ:��';

comment on column CM_BUILDLINK.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDLINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDFACE_TOPO                                     */
/*==============================================================*/
create table CM_BUILDFACE_TOPO  (
   FACE_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMBUILDFACE_LINKS foreign key (FACE_PID)
         references CM_BUILDFACE (FACE_PID),
   constraint CMBUILDFACE_LINK foreign key (LINK_PID)
         references CM_BUILDLINK (LINK_PID)
);

comment on table CM_BUILDFACE_TOPO is
'��¼����������Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column CM_BUILDFACE_TOPO.FACE_PID is
'���,����"CM_BUILDFACE"';

comment on column CM_BUILDFACE_TOPO.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column CM_BUILDFACE_TOPO.LINK_PID is
'���,����"CM_BUILDLINK"';

comment on column CM_BUILDFACE_TOPO.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDFACE_TOPO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDLINK_MESH                                     */
/*==============================================================*/
create table CM_BUILDLINK_MESH  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMBUILDLINK_MESH foreign key (LINK_PID)
         references CM_BUILDLINK (LINK_PID)
);

comment on column CM_BUILDLINK_MESH.LINK_PID is
'���,����"CM_BUILDLINK"';

comment on column CM_BUILDLINK_MESH.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDLINK_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: CM_BUILDNODE_MESH                                     */
/*==============================================================*/
create table CM_BUILDNODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint CMBUILDNODE_MESH foreign key (NODE_PID)
         references CM_BUILDNODE (NODE_PID)
);

comment on column CM_BUILDNODE_MESH.NODE_PID is
'���,����"CM_BUILDNODE"';

comment on column CM_BUILDNODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column CM_BUILDNODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: DEM_GRID                                              */
/*==============================================================*/
create table DEM_GRID  (
   DEM_ID               NUMBER(10)                      not null,
   SW_X                 NUMBER(8)                      default 0 not null,
   SW_Y                 NUMBER(8)                      default 0 not null,
   NE_X                 NUMBER(8)                      default 0 not null,
   NE_Y                 NUMBER(8)                      default 0 not null,
   WIDTH                NUMBER(5)                      default 0 not null,
   HEIGHT               NUMBER(6)                      default 0 not null,
   constraint PK_DEM_GRID primary key (DEM_ID)
);

comment on column DEM_GRID.DEM_ID is
'����';

/*==============================================================*/
/* Table: DEM_ELEVATION                                         */
/*==============================================================*/
create table DEM_ELEVATION  (
   ELEVATION_ID         NUMBER(10)                      not null,
   DEM_ID               NUMBER(10)                      not null,
   X_VAL                NUMBER(8)                      default 0 not null,
   Y_VAL                NUMBER(8)                      default 0 not null,
   Z_VAL                NUMBER(8)                      default 0 not null,
   constraint PK_DEM_ELEVATION primary key (ELEVATION_ID),
   constraint DEMELEVATION_GRID foreign key (DEM_ID)
         references DEM_GRID (DEM_ID)
);

comment on column DEM_ELEVATION.ELEVATION_ID is
'����';

comment on column DEM_ELEVATION.DEM_ID is
'���,����"DEM_GRID"';

/*==============================================================*/
/* Table: DTM_INFO                                              */
/*==============================================================*/
create table DTM_INFO  (
   DTM_ID               NUMBER(10)                      not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_DTM_INFO primary key (DTM_ID)
);

comment on column DTM_INFO.DTM_ID is
'����';

comment on column DTM_INFO.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column DTM_INFO.URL_FILE is
'�洢���·����,��\Data\Video\';

/*==============================================================*/
/* Table: EF_3DMAP                                              */
/*==============================================================*/
create table EF_3DMAP  (
   THREED_ID            NUMBER(10)                      not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_3DMAP primary key (THREED_ID)
);

comment on column EF_3DMAP.THREED_ID is
'����';

comment on column EF_3DMAP.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column EF_3DMAP.URL_FILE is
'�洢���·����,��\Data\3dmap\';

/*==============================================================*/
/* Table: EF_IMAGE                                              */
/*==============================================================*/
create table EF_IMAGE  (
   IMAGE_ID             NUMBER(10)                      not null,
   WIDTH                NUMBER(5)                      default 0 not null,
   HEIGHT               NUMBER(5)                      default 0 not null,
   ALPHA                NUMBER(1)                      default 1 not null
       check (ALPHA in (1,2)),
   TYPE                 NUMBER(1)                      default 1 not null
       check (TYPE in (1,2)),
   FORMAT               NUMBER(1)                      default 2 not null
       check (FORMAT in (1,2)),
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_IMAGE primary key (IMAGE_ID)
);

comment on column EF_IMAGE.IMAGE_ID is
'����';

comment on column EF_IMAGE.WIDTH is
'��λ:����';

comment on column EF_IMAGE.HEIGHT is
'��λ:����';

comment on column EF_IMAGE.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column EF_IMAGE.URL_FILE is
'�洢���·����,��\Data\Video\';

/*==============================================================*/
/* Table: EF_LINEMAP                                            */
/*==============================================================*/
create table EF_LINEMAP  (
   LINEMAP_ID           NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 0 not null,
   ELEVATION            NUMBER(3)                      default 0 not null,
   URL_DB               VARCHAR2(254),
   URL_FILE             VARCHAR2(254),
   constraint PK_EF_LINEMAP primary key (LINEMAP_ID)
);

comment on column EF_LINEMAP.LINEMAP_ID is
'����';

comment on column EF_LINEMAP.URL_DB is
'������ĵ��ļ��洢·�����';

comment on column EF_LINEMAP.URL_FILE is
'�洢���·����,��\Data\Video\';

/*==============================================================*/
/* Table: HWY_JUNCTION                                          */
/*==============================================================*/
create table HWY_JUNCTION  (
   JUNC_PID             NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                     default 0 not null,
   ACCESS_TYPE          NUMBER(1)                      default 0 not null
       check (ACCESS_TYPE in (0,1,2)),
   ATTR                 NUMBER(2)                      default 0 not null
       check (ATTR in (0,1,2,4,8)),
   DIS_BETW             NUMBER(15,3)                   default 0 not null,
   SEQ_NUM              NUMBER(3)                      default 0 not null,
   HW_PID               NUMBER(10)                     default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HWY_JUNCTION primary key (JUNC_PID),
   constraint HWYJUNCTION_LINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint HWYJUNCTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint FK_HWYJUNC_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column HWY_JUNCTION.JUNC_PID is
'����';

comment on column HWY_JUNCTION.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column HWY_JUNCTION.NODE_PID is
'���,����"RD_NODE"';

comment on column HWY_JUNCTION.U_RECORD is
'�������±�ʶ';

comment on column HWY_JUNCTION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HWY_JCT                                               */
/*==============================================================*/
create table HWY_JCT  (
   JCT_PID              NUMBER(10)                      not null,
   S_JUNC_PID           NUMBER(10)                      not null,
   E_JUNC_PID           NUMBER(10)                      not null,
   DIS_BETW             NUMBER(15,3)                   default 0 not null,
   ORIETATION           NUMBER(2)                      default 0 not null
       check (ORIETATION in (0,1,2,3,4)),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HWY_JCT primary key (JCT_PID),
   constraint HWYJCT_JUN_S foreign key (S_JUNC_PID)
         references HWY_JUNCTION (JUNC_PID),
   constraint HWYJCT_JUN_E foreign key (E_JUNC_PID)
         references HWY_JUNCTION (JUNC_PID)
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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint HWYJCTLINK_JCT foreign key (JCT_PID)
         references HWY_JCT (JCT_PID),
   constraint HWYJCTLINK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

/*==============================================================*/
/* Table: HWY_JUNCTION_NAME                                     */
/*==============================================================*/
create table HWY_JUNCTION_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   JUNC_PID             NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 1 not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(1000),
   PHONETIC             VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HWY_JUNCTION_NAME primary key (NAME_ID),
   constraint HWYJUNCTIONNAME_JUN foreign key (JUNC_PID)
         references HWY_JUNCTION (JUNC_PID)
);

comment on column HWY_JUNCTION_NAME.NAME_ID is
'����';

comment on column HWY_JUNCTION_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column HWY_JUNCTION_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column HWY_JUNCTION_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column HWY_JUNCTION_NAME.U_RECORD is
'�������±�ʶ';

comment on column HWY_JUNCTION_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HWY_SAPA                                              */
/*==============================================================*/
create table HWY_SAPA  (
   JUNC_PID             NUMBER(10)                      not null,
   ESTAB_ITEM           VARCHAR2(200),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint HWYSAPA_JUN foreign key (JUNC_PID)
         references HWY_JUNCTION (JUNC_PID)
);

/*==============================================================*/
/* Table: HW_ESTAB                                              */
/*==============================================================*/
create table HW_ESTAB  (
   PID                  NUMBER(10)                      not null,
   FLAG                 NUMBER(2)                      default 1 not null
       check (FLAG in (1,2)),
   ATTR                 NUMBER(2)                      default 0 not null
       check (ATTR in (0,1,2,4,8,16,32)),
   SUFFIX               NUMBER(2)                      default 0 not null
       check (SUFFIX in (0,1,2,3,4,5,8,16,17,18,19,24,25,26)),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HW_ESTAB primary key (PID)
);

comment on column HW_ESTAB.PID is
'����';

comment on column HW_ESTAB.REGION_ID is
'�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column HW_ESTAB.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ESTAB_MAIN                                         */
/*==============================================================*/
create table HW_ESTAB_MAIN  (
   GROUP_ID             NUMBER(10)                      not null,
   ESTAB_PID            NUMBER(10)                      not null,
   REL_TYPE             NUMBER(2)                      default 0
       check (REL_TYPE is null or (REL_TYPE in (0,1))),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HW_ESTAB_MAIN primary key (GROUP_ID),
   constraint HWESTAB_MAIN foreign key (ESTAB_PID)
         references HW_ESTAB (PID)
);

comment on table HW_ESTAB_MAIN is
'��¼HW_ESTAB���б�ʶΪ����(�в���),��FLAG=2����Ϣ';

comment on column HW_ESTAB_MAIN.GROUP_ID is
'����';

comment on column HW_ESTAB_MAIN.ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_MAIN.REL_TYPE is
'[181A]';

comment on column HW_ESTAB_MAIN.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB_MAIN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ESTAB_CONTAIN                                      */
/*==============================================================*/
create table HW_ESTAB_CONTAIN  (
   GROUP_ID             NUMBER(10)                      not null,
   ESTAB_PID            NUMBER(10)                      not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint HWESTAB_MAIN_CONTAIN foreign key (GROUP_ID)
         references HW_ESTAB_MAIN (GROUP_ID),
   constraint HWESTAB_CONTAIN foreign key (ESTAB_PID)
         references HW_ESTAB (PID)
);

comment on column HW_ESTAB_CONTAIN.GROUP_ID is
'���,����"HW_ESTAB_MAIN"';

comment on column HW_ESTAB_CONTAIN.ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_CONTAIN.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB_CONTAIN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ESTAB_JCT                                          */
/*==============================================================*/
create table HW_ESTAB_JCT  (
   JCT_PID              NUMBER(10)                      not null,
   S_ESTAB_PID          NUMBER(10)                      not null,
   E_ESTAB_PID          NUMBER(10)                      not null,
   JCTLINK_PID          NUMBER(10)                     default 0 not null,
   DIS_BETW             NUMBER(15,3)                   default 0 not null,
   ORIETATION           NUMBER(8,5)                    default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HW_ESTAB_JCT primary key (JCT_PID),
   constraint HWESTAB_JCT_S foreign key (S_ESTAB_PID)
         references HW_ESTAB (PID),
   constraint HWESTAB_JCT_E foreign key (E_ESTAB_PID)
         references HW_ESTAB (PID)
);

comment on column HW_ESTAB_JCT.JCT_PID is
'����';

comment on column HW_ESTAB_JCT.S_ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_JCT.E_ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_JCT.JCTLINK_PID is
'�ο�"RD_LINK"';

comment on column HW_ESTAB_JCT.DIS_BETW is
'��λ:��,������յ�ľ���';

comment on column HW_ESTAB_JCT.ORIETATION is
'��λ:��,���յ����������ǰһ�����ߵļн�';

comment on column HW_ESTAB_JCT.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB_JCT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ESTAB_NAME                                         */
/*==============================================================*/
create table HW_ESTAB_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 1 not null,
   ESTAB_PID            NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(1000),
   PHONETIC             VARCHAR2(1000),
   VOICE_FLAG           NUMBER(2)                      default 0 not null
       check (VOICE_FLAG in (0,1)),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HW_ESTAB_NAME primary key (NAME_ID),
   constraint HWESTAB_NAME foreign key (ESTAB_PID)
         references HW_ESTAB (PID)
);

comment on column HW_ESTAB_NAME.NAME_ID is
'����';

comment on column HW_ESTAB_NAME.NAME_GROUPID is
'��1��ʼ�������';

comment on column HW_ESTAB_NAME.ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column HW_ESTAB_NAME.PHONETIC is
'����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column HW_ESTAB_NAME.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ESTAB_ROUTE_POS                                    */
/*==============================================================*/
create table HW_ESTAB_ROUTE_POS  (
   ESTAB_PID            NUMBER(10)                      not null,
   ROUTE_PID            NUMBER(10)                     default 0 not null,
   POSITION_PID         NUMBER(10)                     default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint HWESTAB_ROUTE_POS foreign key (ESTAB_PID)
         references HW_ESTAB (PID)
);

comment on column HW_ESTAB_ROUTE_POS.ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_ROUTE_POS.ROUTE_PID is
'�ο�"HW_ROUTE"';

comment on column HW_ESTAB_ROUTE_POS.POSITION_PID is
'�ο�"HW_POSITION"';

comment on column HW_ESTAB_ROUTE_POS.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB_ROUTE_POS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ESTAB_SA                                           */
/*==============================================================*/
create table HW_ESTAB_SA  (
   ESTAB_PID            NUMBER(10)                      not null,
   INNER_ITEM           NUMBER(3)                      default 0 not null
       check (INNER_ITEM in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,99)),
   KIND                 VARCHAR2(32),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint HWESTAB_SA foreign key (ESTAB_PID)
         references HW_ESTAB (PID)
);

comment on column HW_ESTAB_SA.ESTAB_PID is
'���,����"HW_ESTAB"';

comment on column HW_ESTAB_SA.KIND is
'��"1300"���͹�,"2200"����̵�
';

comment on column HW_ESTAB_SA.U_RECORD is
'�������±�ʶ';

comment on column HW_ESTAB_SA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_POSITION                                           */
/*==============================================================*/
create table HW_POSITION  (
   POSITION_PID         NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   ACCESS_TYPE          NUMBER(1)                      default 1 not null
       check (ACCESS_TYPE in (1,2)),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HW_POSITION primary key (POSITION_PID),
   constraint HWPOSITION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint HWPOSITION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column HW_POSITION.POSITION_PID is
'����';

comment on column HW_POSITION.LINK_PID is
'���,����"RD_LINK"';

comment on column HW_POSITION.NODE_PID is
'���,����"RD_NODE"';

comment on column HW_POSITION.ACCESS_TYPE is
'�����,���,����';

comment on column HW_POSITION.U_RECORD is
'�������±�ʶ';

comment on column HW_POSITION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: HW_ROUTE                                              */
/*==============================================================*/
create table HW_ROUTE  (
   ROUTE_PID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   ACCESS_TYPE          NUMBER(1)                      default 1 not null
       check (ACCESS_TYPE in (1,2,4)),
   PRE_NODEPID          NUMBER(10)                     default 0 not null,
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_HW_ROUTE primary key (ROUTE_PID),
   constraint HWROUTE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint HWROUTE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column HW_ROUTE.ROUTE_PID is
'����';

comment on column HW_ROUTE.LINK_PID is
'���,����"RD_LINK"';

comment on column HW_ROUTE.NODE_PID is
'���,����"RD_NODE"';

comment on column HW_ROUTE.U_RECORD is
'�������±�ʶ';

comment on column HW_ROUTE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IDB_COUNTRY_INFO                                      */
/*==============================================================*/
create table IDB_COUNTRY_INFO  (
   IDB_COUNTRY_ID       VARCHAR2(1000),
   VER_NAME             VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   ITEM_VER             VARCHAR2(1000),
   IDB_REGION_ID        VARCHAR2(1000),
   PERSON               NUMBER(10)                     default 0,
   MEMO                 VARCHAR2(1000)
);

comment on table IDB_COUNTRY_INFO is
'[173A]';

comment on column IDB_COUNTRY_INFO.VER_NAME is
'��12Q1';

comment on column IDB_COUNTRY_INFO.ITEM is
'�û�����:��13CY,NIMIF-G,NAVEX,NIGDF-G,����';

comment on column IDB_COUNTRY_INFO.ITEM_VER is
'ϵͳ��1.0��ʼ,���μ�1';

comment on column IDB_COUNTRY_INFO.IDB_REGION_ID is
'�ο�"IDB_REGION_INFO"';

comment on column IDB_COUNTRY_INFO.PERSON is
'�ο�"BI_PERSON"';

comment on column IDB_COUNTRY_INFO.MEMO is
'�û�����';

/*==============================================================*/
/* Table: IDB_DIFF_INFO                                         */
/*==============================================================*/
create table IDB_DIFF_INFO  (
   IDB_DIFF_ID          VARCHAR2(1000),
   VER_NAME             VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   CUR_IDB_REGION_ID    VARCHAR2(1000),
   PRE_IDB_REGION_ID    VARCHAR2(1000),
   DIFF_PERSON          NUMBER(10)                     default 0,
   DIFF_TIME            DATE,
   DIFF_SOFT_NAME       VARCHAR2(1000),
   DIFF_SOFT_VER        VARCHAR2(1000),
   MEMO                 VARCHAR2(1000)
);

comment on table IDB_DIFF_INFO is
'[173A]';

comment on column IDB_DIFF_INFO.VER_NAME is
'��12Q1';

comment on column IDB_DIFF_INFO.ITEM is
'�û�����:��13CY,NIMIF-G,NAVEX,NIGDF-G,����';

comment on column IDB_DIFF_INFO.CUR_IDB_REGION_ID is
'�ο�"IDB_REGION_INFO",���֮����ð��"|"�ָ�';

comment on column IDB_DIFF_INFO.PRE_IDB_REGION_ID is
'�ο�"IDB_REGION_INFO",���֮����ð��"|"�ָ�';

comment on column IDB_DIFF_INFO.DIFF_PERSON is
'�ο�"BI_PERSON"';

comment on column IDB_DIFF_INFO.DIFF_TIME is
'ϵͳ����';

comment on column IDB_DIFF_INFO.DIFF_SOFT_NAME is
'ϵͳ����';

comment on column IDB_DIFF_INFO.DIFF_SOFT_VER is
'ϵͳ����';

comment on column IDB_DIFF_INFO.MEMO is
'�û�����';

/*==============================================================*/
/* Table: IDB_REGION_INFO                                       */
/*==============================================================*/
create table IDB_REGION_INFO  (
   IDB_REGION_ID        VARCHAR2(1000)                  not null,
   VER_NAME             VARCHAR2(1000),
   IDB_REGION_NAME      VARCHAR2(1000),
   IDB_REGION_NUM       VARCHAR2(1000),
   ACH_GDB_ID           VARCHAR2(1000),
   ITEM                 VARCHAR2(1000),
   PERSON               NUMBER(10)                     default 0,
   MEMO                 VARCHAR2(1000),
   constraint PK_IDB_REGION_INFO primary key (IDB_REGION_ID)
);

comment on table IDB_REGION_INFO is
'[173A]';

comment on column IDB_REGION_INFO.IDB_REGION_ID is
'����';

comment on column IDB_REGION_INFO.VER_NAME is
'��12Q1';

comment on column IDB_REGION_INFO.IDB_REGION_NUM is
'ϵͳ��1.0��ʼ,���μ�1';

comment on column IDB_REGION_INFO.ACH_GDB_ID is
'�ο�"ACH_GDB_INFO",���֮����ð��"|"�ָ�';

comment on column IDB_REGION_INFO.ITEM is
'�û�����:��13CY,NIMIF-G,NAVEX,NIGDF-G,����';

comment on column IDB_REGION_INFO.PERSON is
'�ο�"BI_PERSON"';

comment on column IDB_REGION_INFO.MEMO is
'�û�����';

/*==============================================================*/
/* Table: IX_ANNOTATION                                         */
/*==============================================================*/
create table IX_ANNOTATION  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)                     default 1 not null,
   SRC_FLAG             NUMBER(1)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4,5)),
   SRC_PID              NUMBER(10)                     default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_ANNOTATION primary key (PID)
);

comment on table IX_ANNOTATION is
'�ڵ����豸����ʾ��Ȼ������,����,��·��,��������Ƶȵ����';

comment on column IX_ANNOTATION.PID is
'����';

comment on column IX_ANNOTATION.KIND_CODE is
'�ο�"IX_ANNOTATION_CODE"';

comment on column IX_ANNOTATION.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_ANNOTATION.RANK is
'����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾ
һ����ʾ�ȼ�(����),��ֵΪ0/1 �ֱ��ʾ��Ч/��Ч,
��:00000111 ��ʾ������1,2,4 ��ȼ��Ͼ����ʾ
��0bit:1 ��
��1bit:2 ��
��2bit:4 ��
��3bit:8 ��
��4bit:16 ��
��5bit:32 ��
��6bit:64 ��
��7bit:128 ��
��8bit:256 ��
��9bit:512 ��
��10bit:1024 ��
��11bit:2048 ��
��12bit:4096 ��
��13bit:8192 ��
ע:
(1)2.5 �����:1~8 ��
(2)20 �����:16~32 ��
(3)�������:64~512
(4)TOP �����:1024~8192 ��';

comment on column IX_ANNOTATION.SRC_FLAG is
'ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.SRC_PID is
'������Դ�����ID,������POI��ΪPO��PID;���Ե�·����Ϊ��·��ID
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.CLIENT_FLAG is
'��ݲ�ͬ�ͻ�����,�����ͬ����,ֵ�����:
MB ����
HD �㱾
TY ����
PI �ȷ�
PA ����
NE NavEx
G MIFG
13CY 13CY
NBT ����
ע:
(1)����ÿһ�����ʾֻ�����ĳһ�ͻ�,��ֻ������,��ʾΪ"MB"
(2)����ʾ������ĳһ�ͻ��������ͻ�,�������ϴ���ǰ��Ӣ�İ��"-",��������������Ŀͻ�,���ʾΪ"-MB"
(3)���֮����Ӣ�İ��"|"�ָ�,���ʾ��������������,���ʾΪ"MB|-TY"
(4)Ĭ��Ϊ��,��ʾ���пͻ������
(5)���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.SPECTIAL_FLAG is
'[170]����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾһ������(����),��ֵΪ0/1 �ֱ��ʾ��/��
��0bit:3DICON
��1bit:��ˮ��
����bit Ϊ��Ϊ0,��ʾ�������ʶ
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_ANNOTATION.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_ANNOTATION.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column IX_ANNOTATION.RESERVED is
'[181A]';

comment on column IX_ANNOTATION.MODIFY_FLAG is
'��¼�޸ķ�ʽ������,�����,�ĵȼ�,��λ��,ɾ���';

comment on column IX_ANNOTATION.FIELD_MODIFY_FLAG is
'[170]��¼�޸ķ�ʽ������,�����,�ĵȼ�,��λ��,ɾ���';

comment on column IX_ANNOTATION.EXTRACT_INFO is
'(1)���"�汾+����������ȡ"
(2)����Address �ֶ�
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.EXTRACT_PRIORITY is
'��ȡ�����ȼ���(����ΪA1~A11;����ΪB2~B5)
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.REMARK is
'ת���ʱ,����page�ֶ�,������:"��ʾ���"��"�����"';

comment on column IX_ANNOTATION.DETAIL_FLAG is
'ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION.TASK_ID is
'[170]��¼��ҵ��������';

comment on column IX_ANNOTATION.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column IX_ANNOTATION.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column IX_ANNOTATION.U_RECORD is
'�������±�ʶ';

comment on column IX_ANNOTATION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_ANNOTATION_100W                                    */
/*==============================================================*/
create table IX_ANNOTATION_100W  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   RANK                 NUMBER(10)                     default 1 not null,
   SRC_FLAG             NUMBER(1)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4,5)),
   SRC_PID              NUMBER(10)                     default 0 not null,
   CLIENT_FLAG          VARCHAR2(100),
   SPECTIAL_FLAG        NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   MODIFY_FLAG          VARCHAR2(200),
   FIELD_MODIFY_FLAG    VARCHAR2(200),
   EXTRACT_INFO         VARCHAR2(64),
   EXTRACT_PRIORITY     VARCHAR2(10),
   REMARK               VARCHAR2(64),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_ANNOTATION_100W primary key (PID)
);

comment on table IX_ANNOTATION_100W is
'�ڵ����豸����ʾ��Ȼ������,����,��·��,��������Ƶȵ����';

comment on column IX_ANNOTATION_100W.PID is
'����';

comment on column IX_ANNOTATION_100W.KIND_CODE is
'�ο�"IX_ANNOTATION_CODE"';

comment on column IX_ANNOTATION_100W.GEOMETRY is
'Ĭ�������,�������ΪPOI����ʾ���;������ֵ�ѹ�����,������Ҫ�������ֵĵ�λ';

comment on column IX_ANNOTATION_100W.RANK is
'����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾ
һ����ʾ�ȼ�(����),��ֵΪ0/1 �ֱ��ʾ��Ч/��Ч,
��:00000111 ��ʾ������1,2,4 ��ȼ��Ͼ����ʾ
��0bit:1 ��
��1bit:2 ��
��2bit:4 ��
��3bit:8 ��
��4bit:16 ��
��5bit:32 ��
��6bit:64 ��
��7bit:128 ��
��8bit:256 ��
��9bit:512 ��
��10bit:1024 ��
��11bit:2048 ��
��12bit:4096 ��
��13bit:8192 ��
ע:
(1)2.5 �����:1~8 ��
(2)20 �����:16~32 ��
(3)�������:64~512
(4)TOP �����:1024~8192 ��';

comment on column IX_ANNOTATION_100W.SRC_FLAG is
'ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.SRC_PID is
'������Դ�����ID,������POI��ΪPO��PID;���Ե�·����Ϊ��·��ID
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.CLIENT_FLAG is
'��ݲ�ͬ�ͻ�����,�����ͬ����,ֵ�����:
MB ����
HD �㱾
TY ����
PI �ȷ�
PA ����
NE NavEx
13CY 13CY
NBT ����
ע:
(1)����ÿһ�����ʾֻ�����ĳһ�ͻ�,��ֻ������,��ʾΪ"MB"
(2)����ʾ������ĳһ�ͻ��������ͻ�,�������ϴ���ǰ��Ӣ�İ��"-",��������������Ŀͻ�,���ʾΪ"-MB"
(3)���֮����Ӣ�İ��"|"�ָ�,���ʾ��������������,���ʾΪ"MB|-TY"
(4)Ĭ��Ϊ��,��ʾ���пͻ������
(5)���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.SPECTIAL_FLAG is
'[170]����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾһ������(����),��ֵΪ0/1 �ֱ��ʾ��/��
��0bit:3DICON
��1bit:��ˮ��
����bit Ϊ��Ϊ0,��ʾ�������ʶ
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_ANNOTATION_100W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_ANNOTATION_100W.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column IX_ANNOTATION_100W.RESERVED is
'[181A]';

comment on column IX_ANNOTATION_100W.MODIFY_FLAG is
'��¼�޸ķ�ʽ������,�����,�ĵȼ�,��λ��,ɾ���';

comment on column IX_ANNOTATION_100W.FIELD_MODIFY_FLAG is
'[170]��¼�޸ķ�ʽ������,�����,�ĵȼ�,��λ��,ɾ���';

comment on column IX_ANNOTATION_100W.EXTRACT_INFO is
'(1)���"�汾+����������ȡ"
(2)����Address �ֶ�
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.EXTRACT_PRIORITY is
'��ȡ�����ȼ���(����ΪA1~A11;����ΪB2~B5)
ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.REMARK is
'ת���ʱ,����page�ֶ�,������:"��ʾ���"��"�����"';

comment on column IX_ANNOTATION_100W.DETAIL_FLAG is
'ע:���ֶν�����2.5 ��20 �����,�����TOP ����ݲ���Ҫ';

comment on column IX_ANNOTATION_100W.TASK_ID is
'[170]��¼��ҵ��������';

comment on column IX_ANNOTATION_100W.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column IX_ANNOTATION_100W.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column IX_ANNOTATION_100W.U_RECORD is
'�������±�ʶ';

comment on column IX_ANNOTATION_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_ANNOTATION_FLAG                                    */
/*==============================================================*/
create table IX_ANNOTATION_FLAG  (
   PID                  NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXANNOTATION_FLAG foreign key (PID)
         references IX_ANNOTATION (PID)
);

comment on table IX_ANNOTATION_FLAG is
'[170]';

comment on column IX_ANNOTATION_FLAG.PID is
'���,����"IX_ANNOTATION"';

comment on column IX_ANNOTATION_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column IX_ANNOTATION_FLAG.U_RECORD is
'�������±�ʶ';

comment on column IX_ANNOTATION_FLAG.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_ANNOTATION_FLAG_100W                               */
/*==============================================================*/
create table IX_ANNOTATION_FLAG_100W  (
   PID                  NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXANNOTATION_FLAG_100W foreign key (PID)
         references IX_ANNOTATION_100W (PID)
);

comment on table IX_ANNOTATION_FLAG_100W is
'[171A]��¼Ӣ�����ֵ���Դ��Ϣ';

comment on column IX_ANNOTATION_FLAG_100W.PID is
'���,����"IX_ANNOTATION_100W"';

comment on column IX_ANNOTATION_FLAG_100W.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column IX_ANNOTATION_FLAG_100W.U_RECORD is
'�������±�ʶ';

comment on column IX_ANNOTATION_FLAG_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_ANNOTATION_NAME                                    */
/*==============================================================*/
create table IX_ANNOTATION_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2)),
   OLD_NAME             VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_ANNOTATION_NAME primary key (NAME_ID),
   constraint IXANNOTATION_NAME foreign key (PID)
         references IX_ANNOTATION (PID)
);

comment on column IX_ANNOTATION_NAME.NAME_ID is
'[170]����';

comment on column IX_ANNOTATION_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column IX_ANNOTATION_NAME.PID is
'���,����"IX_ANNOTATION"';

comment on column IX_ANNOTATION_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_ANNOTATION_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column IX_ANNOTATION_NAME.U_RECORD is
'�������±�ʶ';

comment on column IX_ANNOTATION_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_ANNOTATION_NAME_100W                               */
/*==============================================================*/
create table IX_ANNOTATION_NAME_100W  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2)),
   OLD_NAME             VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_ANNOTATION_NAME_100W primary key (NAME_ID),
   constraint IXANNOTATION_NAME_100W foreign key (PID)
         references IX_ANNOTATION_100W (PID)
);

comment on column IX_ANNOTATION_NAME_100W.NAME_ID is
'[170]����';

comment on column IX_ANNOTATION_NAME_100W.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column IX_ANNOTATION_NAME_100W.PID is
'���,����"IX_ANNOTATION_100W"';

comment on column IX_ANNOTATION_NAME_100W.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_ANNOTATION_NAME_100W.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column IX_ANNOTATION_NAME_100W.NAME_CLASS is
'[171U]';

comment on column IX_ANNOTATION_NAME_100W.U_RECORD is
'�������±�ʶ';

comment on column IX_ANNOTATION_NAME_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_CROSSPOINT                                         */
/*==============================================================*/
create table IX_CROSSPOINT  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   NAME_FIR             VARCHAR2(60),
   PINYIN_FIR           VARCHAR2(1000),
   NAME_ENG_FIR         VARCHAR2(200),
   NAME_SEC             VARCHAR2(60),
   PINYIN_SEC           VARCHAR2(1000),
   NAME_ENG_SEC         VARCHAR2(200),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_CROSSPOINT primary key (PID)
);

comment on table IX_CROSSPOINT is
'����������¼�����໥����ĵ�·���佻���λ';

comment on column IX_CROSSPOINT.PID is
'����';

comment on column IX_CROSSPOINT.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_CROSSPOINT.TYPE is
'ƽ��������';

comment on column IX_CROSSPOINT.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_CROSSPOINT.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_CROSSPOINT.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_CROSSPOINT.U_RECORD is
'�������±�ʶ';

comment on column IX_CROSSPOINT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_HAMLET                                             */
/*==============================================================*/
create table IX_HAMLET  (
   PID                  NUMBER(10)                      not null,
   KIND_CODE            VARCHAR2(8)                    
       check (KIND_CODE is null or (KIND_CODE in ('260100','260200','260000'))),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 VARCHAR2(1)                    default '0' not null
       check (SIDE in ('0','1','2','3')),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   POI_NUM              VARCHAR2(36),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_HAMLET primary key (PID)
);

comment on table IX_HAMLET is
'���������,��¼��������Ĵ�,�������';

comment on column IX_HAMLET.PID is
'����';

comment on column IX_HAMLET.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column IX_HAMLET.LINK_PID is
'�ο�"RD_LINK"';

comment on column IX_HAMLET.SIDE is
'��¼Hamletλ�����·Link��,�����Ҳ�';

comment on column IX_HAMLET.NAME_GROUPID is
'[173sp2]�ο�"RD_NAME"';

comment on column IX_HAMLET.ROAD_FLAG is
'[170]';

comment on column IX_HAMLET.PMESH_ID is
'[171A]ÿ����ҵ��POI �ڳɹ���е�һ����LINK ������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column IX_HAMLET.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_HAMLET.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_HAMLET.POI_PID is
'[171A]�ο�"IX_POI"';

comment on column IX_HAMLET.POI_NUM is
'[173A]��¼����NIDB��POI���';

comment on column IX_HAMLET.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_HAMLET.U_RECORD is
'�������±�ʶ';

comment on column IX_HAMLET.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_HAMLET_FLAG                                        */
/*==============================================================*/
create table IX_HAMLET_FLAG  (
   PID                  NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXHAMLET_FLAG foreign key (PID)
         references IX_HAMLET (PID)
);

comment on table IX_HAMLET_FLAG is
'[170]';

comment on column IX_HAMLET_FLAG.PID is
'���,����"IX_HAMLET"';

comment on column IX_HAMLET_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column IX_HAMLET_FLAG.U_RECORD is
'�������±�ʶ';

comment on column IX_HAMLET_FLAG.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_HAMLET_NAME                                        */
/*==============================================================*/
create table IX_HAMLET_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2)),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_HAMLET_NAME primary key (NAME_ID),
   constraint IXHAMLE_NAME foreign key (PID)
         references IX_HAMLET (PID)
);

comment on table IX_HAMLET_NAME is
'[170]';

comment on column IX_HAMLET_NAME.NAME_ID is
'[170]����';

comment on column IX_HAMLET_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column IX_HAMLET_NAME.PID is
'���,����"IX_HAMLET"';

comment on column IX_HAMLET_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_HAMLET_NAME.NAME_CLASS is
'[170]';

comment on column IX_HAMLET_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column IX_HAMLET_NAME.NIDB_PID is
'[173A]��¼����POI���Ѿ���Ʒ������ID,��ͬ��������PID��ͬ';

comment on column IX_HAMLET_NAME.U_RECORD is
'�������±�ʶ';

comment on column IX_HAMLET_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_HAMLET_NAME_TONE                                   */
/*==============================================================*/
create table IX_HAMLET_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXHAMLETNAME_TONE foreign key (NAME_ID)
         references IX_HAMLET_NAME (NAME_ID)
);

comment on table IX_HAMLET_NAME_TONE is
'[170]';

comment on column IX_HAMLET_NAME_TONE.NAME_ID is
'���,����"IX_HAMLET_NAME"';

comment on column IX_HAMLET_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column IX_HAMLET_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column IX_HAMLET_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column IX_HAMLET_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column IX_HAMLET_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column IX_HAMLET_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column IX_HAMLET_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_IC                                                 */
/*==============================================================*/
create table IX_IC  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   KIND_CODE            VARCHAR2(8)                    
       check (KIND_CODE is null or (KIND_CODE in ('230205','230203','230204'))),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   LINK_PID             NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_IC primary key (PID)
);

comment on column IX_IC.PID is
'����';

comment on column IX_IC.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_IC.NAME_GROUPID is
'[170]�ο�"RD_NAME"';

comment on column IX_IC.ROAD_FLAG is
'[170]';

comment on column IX_IC.KIND_CODE is
'[170]';

comment on column IX_IC.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_IC.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_IC.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_IC.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column IX_IC.RESERVED is
'[181A]';

comment on column IX_IC.U_RECORD is
'�������±�ʶ';

comment on column IX_IC.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_NATGUD primary key (PID)
);

comment on column IX_NATGUD.PID is
'����';

comment on column IX_NATGUD.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column IX_NATGUD.U_RECORD is
'�������±�ʶ';

comment on column IX_NATGUD.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_NATGUD_NAME                                        */
/*==============================================================*/
create table IX_NATGUD_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 1 not null,
   NG_ASSO_PID          NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   DESCRIPTION          VARCHAR2(200),
   DESC_PHONETIC        VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_NATGUD_NAME primary key (NAME_ID),
   constraint IXNATGUD_NAME foreign key (NG_ASSO_PID)
         references IX_NATGUD (PID)
);

comment on column IX_NATGUD_NAME.NG_ASSO_PID is
'����';

comment on column IX_NATGUD_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_NATGUD_NAME.U_RECORD is
'�������±�ʶ';

comment on column IX_NATGUD_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (GUIDE_LINK_SIDE in (0,1,2,3)),
   LOCATE_LINK_SIDE     NUMBER(1)                      default 0 not null
       check (LOCATE_LINK_SIDE in (0,1,2,3)),
   SRC_PID              NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   IDCODE               VARCHAR2(36),
   DPR_NAME             VARCHAR2(100),
   DP_NAME              VARCHAR2(35),
   OPERATOR             VARCHAR2(32),
   MEMOIRE              VARCHAR2(200),
   DPF_NAME             VARCHAR2(500),
   POSTER_ID            VARCHAR2(100),
   ADDRESS_FLAG         NUMBER(1)                      default 0 not null
       check (ADDRESS_FLAG in (0,1,2)),
   VERIFED              VARCHAR2(1)                    default 'F' not null
       check (VERIFED in ('T','F')),
   LOG                  VARCHAR2(1000),
   MEMO                 VARCHAR2(500),
   RESERVED             VARCHAR2(1000),
   TASK_ID              NUMBER(10)                     default 0 not null,
   SRC_TYPE             VARCHAR2(100),
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POINTADDRESS primary key (PID)
);

comment on table IX_POINTADDRESS is
'���ƺ������ɵ������ܲ��Ű���һ���������,������λ����������λ�õı���,��������(������),¥(��)��,��Ԫ��,�����Ƶ�.';

comment on column IX_POINTADDRESS.PID is
'����';

comment on column IX_POINTADDRESS.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_POINTADDRESS.GUIDE_LINK_PID is
'�ο�"RD_LINK"';

comment on column IX_POINTADDRESS.LOCATE_LINK_PID is
'�ο�"RD_LINK"';

comment on column IX_POINTADDRESS.LOCATE_NAME_GROUPID is
'�ο�"RD_NAME"';

comment on column IX_POINTADDRESS.GUIDE_LINK_SIDE is
'[171A]';

comment on column IX_POINTADDRESS.LOCATE_LINK_SIDE is
'[171A]';

comment on column IX_POINTADDRESS.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_POINTADDRESS.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_POINTADDRESS.DPR_NAME is
'��ҵ�ɼ���·��';

comment on column IX_POINTADDRESS.DP_NAME is
'��ҵ�ɼ����ƺ�';

comment on column IX_POINTADDRESS.OPERATOR is
'��ҵ��OPERATOR�ֶ��е�����ԭ��ת��';

comment on column IX_POINTADDRESS.MEMOIRE is
'��ע��Ϣ(������ҵLABEL)';

comment on column IX_POINTADDRESS.DPF_NAME is
'[170]';

comment on column IX_POINTADDRESS.POSTER_ID is
'[170]�ʵ�Ա���';

comment on column IX_POINTADDRESS.ADDRESS_FLAG is
'[171U]�����Ƶĵ�ַȷ�ϱ�ʶ';

comment on column IX_POINTADDRESS.LOG is
'���в�ֳ���������ֶ�';

comment on column IX_POINTADDRESS.TASK_ID is
'[170]��¼��ҵ��������';

comment on column IX_POINTADDRESS.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column IX_POINTADDRESS.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column IX_POINTADDRESS.U_RECORD is
'�������±�ʶ';

comment on column IX_POINTADDRESS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

comment on column IX_POINTADDRESS.STATE is
'[173sp2]';

/*==============================================================*/
/* Table: IX_POINTADDRESS_PARENT                                */
/*==============================================================*/
create table IX_POINTADDRESS_PARENT  (
   GROUP_ID             NUMBER(10)                      not null,
   PARENT_PA_PID        NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POINTADDRESS_PARENT primary key (GROUP_ID),
   constraint IXPOINTADDRESS_PARENT foreign key (PARENT_PA_PID)
         references IX_POINTADDRESS (PID)
);

comment on table IX_POINTADDRESS_PARENT is
'[170]';

comment on column IX_POINTADDRESS_PARENT.GROUP_ID is
'����';

comment on column IX_POINTADDRESS_PARENT.PARENT_PA_PID is
'���,����"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_PARENT.U_RECORD is
'�������±�ʶ';

comment on column IX_POINTADDRESS_PARENT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POINTADDRESS_CHILDREN                              */
/*==============================================================*/
create table IX_POINTADDRESS_CHILDREN  (
   GROUP_ID             NUMBER(10)                      not null,
   CHILD_PA_PID         NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOINTADDRESS_PARENT_CHILD foreign key (GROUP_ID)
         references IX_POINTADDRESS_PARENT (GROUP_ID),
   constraint IXPOINTADDRESS_CHILDREN foreign key (CHILD_PA_PID)
         references IX_POINTADDRESS (PID)
);

comment on table IX_POINTADDRESS_CHILDREN is
'[170]';

comment on column IX_POINTADDRESS_CHILDREN.GROUP_ID is
'���,����"IX_POINTADDRESS_PARENT"';

comment on column IX_POINTADDRESS_CHILDREN.CHILD_PA_PID is
'���,����"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_CHILDREN.U_RECORD is
'�������±�ʶ';

comment on column IX_POINTADDRESS_CHILDREN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POINTADDRESS_FLAG                                  */
/*==============================================================*/
create table IX_POINTADDRESS_FLAG  (
   PID                  NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOINTADDRESS_FLAG foreign key (PID)
         references IX_POINTADDRESS (PID)
);

comment on table IX_POINTADDRESS_FLAG is
'[170]';

comment on column IX_POINTADDRESS_FLAG.PID is
'���,����"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column IX_POINTADDRESS_FLAG.U_RECORD is
'�������±�ʶ';

comment on column IX_POINTADDRESS_FLAG.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POINTADDRESS_NAME                                  */
/*==============================================================*/
create table IX_POINTADDRESS_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR','MPY')),
   SUM_CHAR             NUMBER(1)                      default 0 not null
       check (SUM_CHAR in (0,1,2,3)),
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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POINTADDRESS_NAME primary key (NAME_ID),
   constraint IXPOINTADDRESS_NAME foreign key (PID)
         references IX_POINTADDRESS (PID)
);

comment on column IX_POINTADDRESS_NAME.NAME_ID is
'[170]����';

comment on column IX_POINTADDRESS_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column IX_POINTADDRESS_NAME.PID is
'���,����"IX_POINTADDRESS"';

comment on column IX_POINTADDRESS_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_POINTADDRESS_NAME.SUM_CHAR is
'�����Ƶĺ�������,������,��ż,���';

comment on column IX_POINTADDRESS_NAME.SPLIT_FLAG is
'[173sp2]';

comment on column IX_POINTADDRESS_NAME.FULLNAME is
'[170]��¼���ǰ��ȫ��ַ���';

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
'[170]������"ʡ��/ֱϽ��/������/�ر���������"';

comment on column IX_POINTADDRESS_NAME.CITY is
'[170]������"�ؼ�����/��������"';

comment on column IX_POINTADDRESS_NAME.COUNTY is
'[170]������"�ؼ�����/����/����(��ֱϽ�е���)"';

comment on column IX_POINTADDRESS_NAME.TOWN is
'[170]����ֵ������';

comment on column IX_POINTADDRESS_NAME.PLACE is
'[170]��Ȼ����,����С��,�������,��������';

comment on column IX_POINTADDRESS_NAME.STREET is
'[170]�ֵ�,��·��, ��ͬ,��,��,Ū';

comment on column IX_POINTADDRESS_NAME.LANDMARK is
'[170]ָ�е����ʾ���õĵ���,������ʩ,��λ,������ͨ������ʩ,��������,��·����,��ͨվ����';

comment on column IX_POINTADDRESS_NAME.PREFIX is
'[170]�����������ƺź���ĳɷ�';

comment on column IX_POINTADDRESS_NAME.HOUSENUM is
'[170]�����ƺź���,����ŷ�ʽ�����Ū����';

comment on column IX_POINTADDRESS_NAME.TYPE is
'[170]���ƺź�������';

comment on column IX_POINTADDRESS_NAME.SUBNUM is
'[170]�����ƺ������������ƺż����θ������Ƶ�ǰ׺��Ϣ';

comment on column IX_POINTADDRESS_NAME.SURFIX is
'[170]�����������Ƶ�ַ�Ĵ���,�䱾��û��ʵ������,��Ӱ�����Ƶ�ַ�ĺ���,��:�Ա�,��ʱ';

comment on column IX_POINTADDRESS_NAME.ESTAB is
'[170]��"**����","**С��"';

comment on column IX_POINTADDRESS_NAME.BUILDING is
'[170]��"A��,12��,31¥,B��"��';

comment on column IX_POINTADDRESS_NAME.UNIT is
'[170]��"2��"';

comment on column IX_POINTADDRESS_NAME.FLOOR is
'[170]��"12��"';

comment on column IX_POINTADDRESS_NAME.ROOM is
'[170]��"503��"';

comment on column IX_POINTADDRESS_NAME.ADDONS is
'[171U][170]��"����,�Ա�,����"';

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
'�������±�ʶ';

comment on column IX_POINTADDRESS_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POINTADDRESS_NAME_TONE                             */
/*==============================================================*/
create table IX_POINTADDRESS_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
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
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOINTADDRESSNAME_TONE foreign key (NAME_ID)
         references IX_POINTADDRESS_NAME (NAME_ID)
);

comment on table IX_POINTADDRESS_NAME_TONE is
'[170]';

comment on column IX_POINTADDRESS_NAME_TONE.NAME_ID is
'���,����"IX_POINTADDRESS_NAME"';

comment on column IX_POINTADDRESS_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column IX_POINTADDRESS_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column IX_POINTADDRESS_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column IX_POINTADDRESS_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column IX_POINTADDRESS_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column IX_POINTADDRESS_NAME_TONE.PA_JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column IX_POINTADDRESS_NAME_TONE.PA_TONE_A is
'��Ӧ�ڶ�Ӧ�����ƺ�ƴ��,���ֲ�תƴ��';

comment on column IX_POINTADDRESS_NAME_TONE.PA_TONE_B is
'��Ӧ�����ƺŵ�ƴ��,����תƴ��';

comment on column IX_POINTADDRESS_NAME_TONE.PA_LH_A is
'��Ӧ���ƺ����ƴ��1,ת��LH+';

comment on column IX_POINTADDRESS_NAME_TONE.PA_LH_B is
'��Ӧ���ƺ����ƴ��2,ת��LH+';

comment on column IX_POINTADDRESS_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column IX_POINTADDRESS_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_ADDRESS                                        */
/*==============================================================*/
create table IX_POI_ADDRESS  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
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
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_ADDRESS primary key (NAME_ID),
   constraint IXPOI_ADDRESS foreign key (POI_PID)
         references IX_POI (PID)
);

comment on column IX_POI_ADDRESS.NAME_ID is
'[170]����';

comment on column IX_POI_ADDRESS.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column IX_POI_ADDRESS.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_ADDRESS.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_POI_ADDRESS.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

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
'POI�����е�"ʡ��/ֱϽ��/������/�ر���������"';

comment on column IX_POI_ADDRESS.CITY is
'POI�����е�"�ؼ�����/��������"';

comment on column IX_POI_ADDRESS.COUNTY is
'POI�����е�"�ؼ�����/����/����(��ֱϽ�е���)"';

comment on column IX_POI_ADDRESS.TOWN is
'[170]����ֵ������';

comment on column IX_POI_ADDRESS.PLACE is
'[170]��Ȼ����,����С��,�������,��������';

comment on column IX_POI_ADDRESS.STREET is
'[170]�ֵ�,��·��, ��ͬ,��,��,Ū';

comment on column IX_POI_ADDRESS.LANDMARK is
'ָ�е����ʾ���õĵ���,������ʩ,��λ,������ͨ������ʩ,��������,��·����,��ͨվ����';

comment on column IX_POI_ADDRESS.PREFIX is
'�����������ƺź���ĳɷ�';

comment on column IX_POI_ADDRESS.HOUSENUM is
'�����ƺź���,����ŷ�ʽ�����Ū����';

comment on column IX_POI_ADDRESS.TYPE is
'���ƺź�������';

comment on column IX_POI_ADDRESS.SUBNUM is
'�����ƺ������������ƺż����θ������Ƶ�ǰ׺��Ϣ';

comment on column IX_POI_ADDRESS.SURFIX is
'�����������Ƶ�ַ�Ĵ���,�䱾��û��ʵ������,��Ӱ�����Ƶ�ַ�ĺ���,��:�Ա�,��ʱ';

comment on column IX_POI_ADDRESS.ESTAB is
'��"**����","**С��"';

comment on column IX_POI_ADDRESS.BUILDING is
'��"A��,12��,31¥,B��"��';

comment on column IX_POI_ADDRESS.FLOOR is
'��"12��"';

comment on column IX_POI_ADDRESS.UNIT is
'��"2��"';

comment on column IX_POI_ADDRESS.ROOM is
'��"503��"';

comment on column IX_POI_ADDRESS.ADDONS is
'[171U]��"����,�Ա�,����"';

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
'�������±�ʶ';

comment on column IX_POI_ADDRESS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_ADVERTISEMENT                                  */
/*==============================================================*/
create table IX_POI_ADVERTISEMENT  (
   ADVERTISE_ID         NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   LABEL_TEXT           VARCHAR2(500),
   TYPE                 VARCHAR2(100),
   PRIORITY             NUMBER(2)                      default 1 not null
       check (PRIORITY in (1,2,3,4,5,6,7,8,9)),
   START_TIME           VARCHAR2(100),
   END_TIME             VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_ADVERTISEMENT primary key (ADVERTISE_ID)
);

comment on table IX_POI_ADVERTISEMENT is
'[170]';

comment on column IX_POI_ADVERTISEMENT.ADVERTISE_ID is
'����';

comment on column IX_POI_ADVERTISEMENT.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_ADVERTISEMENT.TYPE is
'0:���
1:������ʾ
2:�����
3:������ʾ
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
';

comment on column IX_POI_ADVERTISEMENT.START_TIME is
'��ʽ:YYYY:MM:DD
���ʱ���ʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_ADVERTISEMENT.END_TIME is
'��ʽ:YYYY:MM:DD
���ʱ���ʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_ADVERTISEMENT.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_ADVERTISEMENT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_ATTRACTION                                     */
/*==============================================================*/
create table IX_POI_ATTRACTION  (
   ATTRACTION_ID        NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   SIGHT_LEVEL          NUMBER(2)                      default 0 not null
       check (SIGHT_LEVEL in (0,1,2,3,4,5)),
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
       check (PARKING in (0,1,2,3)),
   TRAVELGUIDE_FLAG     NUMBER(1)                      default 0 not null
       check (TRAVELGUIDE_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_ATTRACTION primary key (ATTRACTION_ID)
);

comment on table IX_POI_ATTRACTION is
'[170][190U][210]';

comment on column IX_POI_ATTRACTION.ATTRACTION_ID is
'����';

comment on column IX_POI_ATTRACTION.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_ATTRACTION.CITY is
'�洢��ʽ��';

comment on column IX_POI_ATTRACTION.PHOTO_NAME is
'�����Ƭʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_ATTRACTION.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_ATTRACTION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_AUDIO                                          */
/*==============================================================*/
create table IX_POI_AUDIO  (
   POI_PID              NUMBER(10)                      not null,
   AUDIO_ID             NUMBER(10)                     default 0 not null,
   PID                  VARCHAR2(32),
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   TAG                  NUMBER(3)                      default 0 not null
       check (TAG in (0)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_AUDIO foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_AUDIO is
'[170]';

comment on column IX_POI_AUDIO.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_AUDIO.AUDIO_ID is
'�ο�"AU_AUDIO"';

comment on column IX_POI_AUDIO.STATUS is
'��¼�Ƿ�ȷ��';

comment on column IX_POI_AUDIO.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_AUDIO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_BUILDING                                       */
/*==============================================================*/
create table IX_POI_BUILDING  (
   POI_PID              NUMBER(10)                     default 0 not null,
   FLOOR_USED           VARCHAR2(1000),
   FLOOR_EMPTY          VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16)
);

comment on table IX_POI_BUILDING is
'[1802A]';

comment on column IX_POI_BUILDING.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_BUILDING.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16)
);

comment on table IX_POI_BUSINESSTIME is
'[171A][190U]';

comment on column IX_POI_BUSINESSTIME.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_BUSINESSTIME.MON_SRT is
'Ӫҵ��ʼ�·�,1~12';

comment on column IX_POI_BUSINESSTIME.MON_END is
'Ӫҵ�����·�,1~12';

comment on column IX_POI_BUSINESSTIME.WEEK_IN_YEAR_SRT is
'ָһ���е���ʼ��,��ʾ�ӵ�N �ܿ�ʼ,N ȡֵ:
1~53 ��-1~-53';

comment on column IX_POI_BUSINESSTIME.WEEK_IN_YEAR_END is
'ָһ���еĽ�����,��ʾ����N �ܽ���,N ȡֵ:
1~53 ��-1~-53';

comment on column IX_POI_BUSINESSTIME.WEEK_IN_MONTH_SRT is
'ָ���·ݵ�Ӫҵ��ʼ��, ��ʾ�ӵ�N �ܿ�ʼ,N
ȡֵ:1~5 ��-1~-5';

comment on column IX_POI_BUSINESSTIME.WEEK_IN_MONTH_END is
'ָ���·ݵ�Ӫҵ������, ��ʾ����N �ܽ���,N
ȡֵ:1~5 ��-1~-5';

comment on column IX_POI_BUSINESSTIME.VALID_WEEK is
'0/1:��Ч/ ��Ч,��7 λ,��1000000 Ϊ����Ӫҵ';

comment on column IX_POI_BUSINESSTIME.DAY_SRT is
'ȡֵ:1~31 �� -31~-1,����:
3 :���������3 ��(3 ��);-4:���µ��������';

comment on column IX_POI_BUSINESSTIME.DAY_END is
'ȡֵ:1~31 �� -31~-1';

comment on column IX_POI_BUSINESSTIME.TIME_SRT is
'08:00 Ӫҵ��ʼʱ��Ϊ����8 ����
ע:ð��Ϊ��Ǹ�ʽ';

comment on column IX_POI_BUSINESSTIME.TIME_DUR is
'12:00 Ӫҵʱ��Ϊ12 Сʱ0 ����
ע: time_srt + time_dur �ɴ��ڵ���24:00,��ʾ
Ӫҵ������ĳʱ';

comment on column IX_POI_BUSINESSTIME.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_BUSINESSTIME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16)
);

comment on column IX_POI_CARRENTAL.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_CARRENTAL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_CHARGINGPLOT                                   */
/*==============================================================*/
create table IX_POI_CHARGINGPLOT  (
   POI_PID              NUMBER(10)                     default 0 not null,
   GROUP_ID             NUMBER(5)                      default 1 not null,
   COUNT                NUMBER(5)                      default 1 not null,
   ACDC                 NUMBER(1)                      default 0 not null
       check (ACDC in (0,1)),
   PLUG_TYPE            VARCHAR2(20)                   default '9' not null,
   POWER                VARCHAR2(50),
   VOLTAGE              VARCHAR2(50),
   "CURRENT"            VARCHAR2(50),
   "MODE"               NUMBER(1)                      default 0 not null
       check ("MODE" in (0,1)),
   MEMO                 VARCHAR2(500),
   PLUG_NUM             NUMBER(5)                      default 1 not null,
   PRICES               VARCHAR2(254),
   OPEN_TYPE            VARCHAR2(50)                   default '1' not null,
   AVAILABLE_STATE      NUMBER(1)                      default 0 not null
       check (AVAILABLE_STATE in (0,1,2,3,4)),
   MANUFACTURER         VARCHAR2(254),
   FACTORY_NUM          VARCHAR2(254),
   PLOT_NUM             VARCHAR2(50),
   PRODUCT_NUM          VARCHAR2(254),
   PARKING_NUM          VARCHAR2(50),
   FLOOR                NUMBER(2)                      default 1 not null,
   LOCATION_TYPE        NUMBER(1)                      default 0 not null
       check (LOCATION_TYPE in (0,1,2)),
   PAYMENT              VARCHAR2(20)                   default '4' not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16)
);

comment on table IX_POI_CHARGINGPLOT is
'[170][190U]';

comment on column IX_POI_CHARGINGPLOT.GROUP_ID is
'��/ֱ����,��ͷ����,��繦�ʺ͵�ѹ����ͬ�ĳ��׮Ϊһ��';

comment on column IX_POI_CHARGINGPLOT.COUNT is
'ͬһ���ڵĳ��׮����';

comment on column IX_POI_CHARGINGPLOT.PLUG_TYPE is
'ֵ�����:
����	���
0	������3�׼���
1	��꽻����7�ײ��
2	���ֱ����9�ײ��
3	��ʽ����5�ײ��
4	��ʽֱ��Combo���
5	ŷʽ����7�ײ��
6	ŷʽֱ��Combo���
7	��ʽֱ��CHAdeMO���
8	��˹��ר�ò��
9	����
10	�޷��ɼ�
�ɲ��棬"|"�ָ�
';

comment on column IX_POI_CHARGINGPLOT.POWER is
'[210]��λΪKW';

comment on column IX_POI_CHARGINGPLOT.VOLTAGE is
'[210]��λΪV';

comment on column IX_POI_CHARGINGPLOT."CURRENT" is
'[180A][210]��λΪA';

comment on column IX_POI_CHARGINGPLOT.MEMO is
'[180A]';

comment on column IX_POI_CHARGINGPLOT.OPEN_TYPE is
'1	�����г�������
2	�Ի���������
3	�Թ���������
4	�Գ��⳵����
5	���������ֳ�������
6	�����г�������
7	���˳��׮
ChainID����ĳƷ�����ţ�
���У�
1��������ֵ���棻
��1֮�������ֵ���Թ��棬���ַ�ʽ���ͼ��԰�ǡ�|���ָ�����ʾֻ����Щ�������ţ�';

comment on column IX_POI_CHARGINGPLOT.PAYMENT is
'ֵ�����:
����	���
0	���� 
1	�ֽ�
2	���ÿ�
3	��ǿ�
4	���Ƴ�ֵ��
5	 APP
���ָ��ѷ�ʽʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
';

comment on column IX_POI_CHARGINGPLOT.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_CHARGINGPLOT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_CHARGINGPLOT_PH                                */
/*==============================================================*/
create table IX_POI_CHARGINGPLOT_PH  (
   POI_PID              NUMBER(10)                     default 0 not null,
   PHOTO_NAME           VARCHAR2(254),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16)
);

comment on column IX_POI_CHARGINGPLOT_PH.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_CHARGINGPLOT_PH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_CHARGINGSTATION                                */
/*==============================================================*/
create table IX_POI_CHARGINGSTATION  (
   CHARGING_ID          NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CHARGING_TYPE        NUMBER(2)                      default 3 not null
       check (CHARGING_TYPE in (1,2,3,4)),
   CHANGE_BRANDS        VARCHAR2(100),
   CHANGE_OPEN_TYPE     VARCHAR2(10)                   default '1' not null,
   CHARGING_NUM         NUMBER(5)                      default 0 not null,
   SERVICE_PROV         VARCHAR2(5)                    default '0' not null,
   MEMO                 VARCHAR2(500),
   PHOTO_NAME           VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   PARKING_FEES         NUMBER(1)                      default 0 not null
       check (PARKING_FEES in (0,1)),
   PARKING_INFO         VARCHAR2(254),
   AVAILABLE_STATE      NUMBER(1)                      default 0 not null
       check (AVAILABLE_STATE in (0,1,2,3,4)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_CHARGINGSTATION primary key (CHARGING_ID)
);

comment on table IX_POI_CHARGINGSTATION is
'[170][210]';

comment on column IX_POI_CHARGINGSTATION.CHARGING_ID is
'����';

comment on column IX_POI_CHARGINGSTATION.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_CHARGINGSTATION.CHARGING_TYPE is
'[180U][210]';

comment on column IX_POI_CHARGINGSTATION.CHANGE_OPEN_TYPE is
'1	������
2	�Ի���������
3	�Թ���������
4	�Գ��⳵����
5	���������ֳ�������
6	�����г�������
1��������ֵ���棻
����ֵ���Թ��棬��ֵʱ�԰�ǡ�|���ָ���';

comment on column IX_POI_CHARGINGSTATION.CHARGING_NUM is
'���ڵ���0,�ձ�ʾδ����';

comment on column IX_POI_CHARGINGSTATION.SERVICE_PROV is
'ֻ����һ��ֵ��ֵ�����:
0	����
1	��ҵ���
2	�Ϸ�����
3	��ʯ��
4	��ʯ��
5	�к���
6	�й�����
7	������
8	ѭ������Դ
9	����Ƽ�
10	��������
11	���
12	�۟�
13	���
14	�̹�
15	EVCARD
16	���ǳ��
17	��׮
18	������Դ
ChainID';

comment on column IX_POI_CHARGINGSTATION.MEMO is
'[180A]';

comment on column IX_POI_CHARGINGSTATION.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_CHARGINGSTATION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_PARENT                                         */
/*==============================================================*/
create table IX_POI_PARENT  (
   GROUP_ID             NUMBER(10)                      not null,
   PARENT_POI_PID       NUMBER(10)                      not null,
   TENANT_FLAG          NUMBER(2)                      default 0
       check (TENANT_FLAG is null or (TENANT_FLAG in (0,1))),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_PARENT primary key (GROUP_ID),
   constraint IXPOI_PARENT foreign key (PARENT_POI_PID)
         references IX_POI (PID)
);

comment on column IX_POI_PARENT.GROUP_ID is
'����';

comment on column IX_POI_PARENT.PARENT_POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_PARENT.TENANT_FLAG is
'[181U]';

comment on column IX_POI_PARENT.MEMO is
'[181A]';

comment on column IX_POI_PARENT.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_PARENT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_CHILDREN                                       */
/*==============================================================*/
create table IX_POI_CHILDREN  (
   GROUP_ID             NUMBER(10)                      not null,
   CHILD_POI_PID        NUMBER(10)                      not null,
   RELATION_TYPE        NUMBER(1)                      default 0 not null
       check (RELATION_TYPE in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_CHILD foreign key (CHILD_POI_PID)
         references IX_POI (PID),
   constraint IXPOI_PARENT_CHILD foreign key (GROUP_ID)
         references IX_POI_PARENT (GROUP_ID)
);

comment on column IX_POI_CHILDREN.GROUP_ID is
'���,����"IX_POI_PARENT"';

comment on column IX_POI_CHILDREN.CHILD_POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_CHILDREN.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_CHILDREN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_CONTACT                                        */
/*==============================================================*/
create table IX_POI_CONTACT  (
   POI_PID              NUMBER(10)                      not null,
   CONTACT_TYPE         NUMBER(2)                      default 1 not null
       check (CONTACT_TYPE in (1,2,3,4,11,21,22)),
   CONTACT              VARCHAR2(128),
   CONTACT_DEPART       NUMBER(3)                      default 0 not null,
   PRIORITY             NUMBER(5)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_TELEPHONE foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_CONTACT is
'�����ڶ����ϵ��ʽʱ,�洢Ϊ������¼';

comment on column IX_POI_CONTACT.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_CONTACT.CONTACT is
'��¼����������ڵĵ绰����,�洢ΪӢ�İ�������ַ�,����֮���԰��"-"�ָ�,��010-82306399';

comment on column IX_POI_CONTACT.CONTACT_DEPART is
'����8bit ��ʾ,���ҵ�������Ϊ0~7bit,ÿbit ��ʾһ��������(����),��ֵΪ0/1 �ֱ��ʾ��/��,��:00000011 ��ʾ�ܻ�Ϳͷ�;00000101 ��ʾ�ܻ��Ԥ��
��0bit:�ܻ�
��1bit:�ͷ�
��2bit:Ԥ��
��3bit:����
��4bit:ά��
��5bit:����
�������bit λ��Ϊ0,��ʾδ����';

comment on column IX_POI_CONTACT.PRIORITY is
'[1901U]��ϵ��ʽ�����ȼ�����';

comment on column IX_POI_CONTACT.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_CONTACT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (ADVER_FLAG in (0,1)),
   PHOTO_NAME           VARCHAR2(254),
   RESERVED             VARCHAR2(1000),
   MEMO                 VARCHAR2(1000),
   HW_ENTRYEXIT         NUMBER(1)                      default 0 not null
       check (HW_ENTRYEXIT in (0,1)),
   PAYCARD              NUMBER(1)                      default 0 not null
       check (PAYCARD in (0,1)),
   CARDTYPE             VARCHAR2(10),
   HOSPITAL_CLASS       NUMBER(2)                      default 0 not null
       check (HOSPITAL_CLASS in (0,1,2,3,4,5,6,7,8,9)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16)
);

comment on table IX_POI_DETAIL is
'[190A][210]';

comment on column IX_POI_DETAIL.WEB_SITE is
'��¼��ַ��Ϣ';

comment on column IX_POI_DETAIL.FAX is
'���֮����ð��"|"�ָ�';

comment on column IX_POI_DETAIL.STAR_HOTEL is
'��POI KIND=5081,5082,5083,5084,5085 ʱ
��ֵ,����Ϊ��';

comment on column IX_POI_DETAIL.ADVER_FLAG is
'ֵ�����:
0 ��
1 ��';

comment on column IX_POI_DETAIL.CARDTYPE is
'1	άʿ(visa)
2	���´�(mastercard)
�������ʱ����Ӣ�İ�ǡ�|���ָ�
��֧�����ÿ�ʱΪ��';

comment on column IX_POI_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_ENTRYIMAGE                                     */
/*==============================================================*/
create table IX_POI_ENTRYIMAGE  (
   POI_PID              NUMBER(10)                      not null,
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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_ENTRYIMAGE foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_ENTRYIMAGE is
'[170]';

comment on column IX_POI_ENTRYIMAGE.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_ENTRYIMAGE.IMAGE_CODE is
'ͼ���ļ����';

comment on column IX_POI_ENTRYIMAGE.X_PIXEL_R4 is
'POI �ڸ���ͼ��X ���������(reuko id4)';

comment on column IX_POI_ENTRYIMAGE.Y_PIXEL_R4 is
'POI �ڸ���ͼ��Y ���������(reuko id4)';

comment on column IX_POI_ENTRYIMAGE.X_PIXEL_R5 is
'POI �ڸ���ͼ��X ���������(reuko id5)';

comment on column IX_POI_ENTRYIMAGE.Y_PIXEL_R5 is
'POI �ڸ���ͼ��Y ���������(reuko id5)';

comment on column IX_POI_ENTRYIMAGE.X_PIXEL_35 is
'POI �ڸ���ͼ��X ���������(35up id5)';

comment on column IX_POI_ENTRYIMAGE.Y_PIXEL_35 is
'POI �ڸ���ͼ��Y ���������(35up id5)';

comment on column IX_POI_ENTRYIMAGE.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_ENTRYIMAGE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_EVENT primary key (EVENT_ID)
);

comment on table IX_POI_EVENT is
'[190A][210]';

comment on column IX_POI_EVENT.POI_PID is
'���POI ʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_EVENT.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_EVENT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_FLAG                                           */
/*==============================================================*/
create table IX_POI_FLAG  (
   POI_PID              NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_FLAG foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_FLAG is
'[170]';

comment on column IX_POI_FLAG.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column IX_POI_FLAG.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_FLAG.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_GASSTATION primary key (GASSTATION_ID)
);

comment on table IX_POI_GASSTATION is
'[170][190U][210]';

comment on column IX_POI_GASSTATION.GASSTATION_ID is
'����';

comment on column IX_POI_GASSTATION.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_GASSTATION.SERVICE_PROV is
'1	��ʯ��(Sinopec)
2	�Ї�ʯ��(Chinaoil)
3	�ӵ�ʿ(Caltex)
4	����ɭ���ں͖|��(Esso Feoso)
5	͘��(Shell)
6	�Ϲ�ʯ��(Nkoil)
7	�׸�(Towngas)
8	����
�۰Ķ����ֶΣ���½ʱΪ��
';

comment on column IX_POI_GASSTATION.FUEL_TYPE is
'[180A][210]ֵ�����:
����	���
0	����(Diesel)
1	����(Gasoline)
2	�״�����(MG85)
3	����
4	Һ��ʯ����(LPG)
5	��Ȼ��(CNG)
6	�Ҵ�����(E10)
7	��ȼ��(Hydrogen)
8	�������(Biodiesel)
9	Һ����Ȼ��(LNG)
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
�۰�ֵ�� ��һλ����Ӧ�ķ����ṩ��
����	���
11	SINO X Power
12	SINO Power
13	SINO Disel
14	LPG
21	���Ų���
22	��������
23	��������
24	LPG
31	Gold�ƽ�
32	Platinum�׽�
33	Diesel�������
34	�����ֽ���ieselCasCard
35	ʯ�͚�AutoGas
41	Disel���������
42	8000���
43	F-1�ؼ����
44	AutoGasʯ�͚�
51	Disesel����
52	FuelSave�a���䷽����
53	Shell V-Power
54	AutoGasʯ�͚�
61	���őa���䷽����
62	�坍�䷽�������
';

comment on column IX_POI_GASSTATION.OIL_TYPE is
'[180U]ֵ�����:
����	���
0	����
89            89#����
90	90#����
92            92#����
93	93#����
95            95#����
97	97#����
98	98#���� 
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ע:��FUEL_TYPE=1(����)ʱ��ֵ,����Ϊ��
';

comment on column IX_POI_GASSTATION.EG_TYPE is
'[180A]ֵ�����:
����	���
0	����
E90	E90#����
E92	E92#����
E93	E93#����
E95	E95#����
E97	E97#����
E98	E98#����
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ע:��FUEL_TYPE=6(�Ҵ�����)ʱ��ֵ,����Ϊ��';

comment on column IX_POI_GASSTATION.MG_TYPE is
'ֵ�����:
���� ���
0 ����
M5 M5#����
M10 M10#����
M15 M15#����
M30 M30#����
M50 M50#����
M85 M85#����
M100 M100#����
��������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ע:��FUEL_TYPE=2(�״�����)ʱ��ֵ,����Ϊ��';

comment on column IX_POI_GASSTATION.PAYMENT is
'�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����
ֵ�����:
��½ֵ��
����	���
0	�ֽ�
1	��ǿ�
2	���ÿ�
�۰�ֵ��
10	���_ͨ
11	VISA
12	MasterCard
13	�F��
14	����
';

comment on column IX_POI_GASSTATION.SERVICE is
'[180U]ֵ�����:
���� ���
1 �����
2 ϴ��
3��ά�� 
4	�����
5	����
6	ס��
7	����
8	�������
�������ʱ����Ӣ�İ�ǡ�|���ָ�
�۰�ֵ��
����	���
11	�Q�ͷ���Lube Service
12	ϴ܇����Car Wash
13	�����Convenience Store
14	����Toilet
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_GASSTATION.MEMO is
'[180A]';

comment on column IX_POI_GASSTATION.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_GASSTATION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_HOTEL                                          */
/*==============================================================*/
create table IX_POI_HOTEL  (
   HOTEL_ID             NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   CREDIT_CARD          VARCHAR2(10),
   RATING               NUMBER(2)                      default 0 not null
       check (RATING in (0,1,3,4,5,6,7,8,13,14,15,16,17)),
   CHECKIN_TIME         VARCHAR2(20)                   default '14:00' not null,
   CHECKOUT_TIME        VARCHAR2(20)                   default '12:00' not null,
   ROOM_COUNT           NUMBER(5)                      default 0 not null,
   ROOM_TYPE            VARCHAR2(20),
   ROOM_PRICE           VARCHAR2(100),
   BREAKFAST            NUMBER(2)                      default 0 not null
       check (BREAKFAST in (0,1)),
   SERVICE              VARCHAR2(254),
   PARKING              NUMBER(2)                      default 0 not null
       check (PARKING in (0,1,2,3)),
   LONG_DESCRIPTION     VARCHAR2(254),
   LONG_DESCRIP_ENG     VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   OPEN_HOUR_ENG        VARCHAR2(254),
   TELEPHONE            VARCHAR2(100),
   ADDRESS              VARCHAR2(200),
   CITY                 VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   TRAVELGUIDE_FLAG     NUMBER(1)                      default 0 not null
       check (TRAVELGUIDE_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_HOTEL primary key (HOTEL_ID)
);

comment on table IX_POI_HOTEL is
'[170][190U][210]';

comment on column IX_POI_HOTEL.HOTEL_ID is
'����';

comment on column IX_POI_HOTEL.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_HOTEL.CREDIT_CARD is
'ֵ�����:
1 άʿ(visa)
2 ���´�(mastercard)
3 ����(dinas)
4 �ձ�������ÿ�(jcb)
5 ������ͨ(America
Express)
6 ����(unionpay)
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ0 ��ʾ��֧�����ÿ�
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_HOTEL.RATING is
'[210]';

comment on column IX_POI_HOTEL.CHECKIN_TIME is
'��ʽ:HH:mm';

comment on column IX_POI_HOTEL.CHECKOUT_TIME is
'��ʽ:HH:mm';

comment on column IX_POI_HOTEL.ROOM_COUNT is
'���ڵ���0 ������,0 ��ʾδ����';

comment on column IX_POI_HOTEL.ROOM_TYPE is
'ֵ�����:
1 ���˼�(single)
2 ��׼��(double)
3 �׷�(suite)
�������ʱ����Ӣ�İ��"|"�ָ�
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_HOTEL.ROOM_PRICE is
'����۸�ʱ����Ӣ�İ��"|"�ָ�,˳�������ͷ�����һ��
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_HOTEL.SERVICE is
'ֵ�����:
1 ������
2 ��������
31 �ư�
32 ����OK
33 ��������
34 ������Ӿ��
35 SPA
36 ɣ��
51 �в���
52 ������
53 ������
54 ����
�������ʱ����Ӣ�İ��"|"�ָ�
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_HOTEL.CITY is
'�洢��ʽ��';

comment on column IX_POI_HOTEL.PHOTO_NAME is
'�����Ƭʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_HOTEL.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_HOTEL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_ICON                                           */
/*==============================================================*/
create table IX_POI_ICON  (
   REL_ID               NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                      not null,
   ICON_NAME            VARCHAR2(100),
   GEOMETRY             SDO_GEOMETRY,
   MANAGE_CODE          VARCHAR2(100),
   CLIENT_FLAG          VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_ICON primary key (REL_ID),
   constraint IXPOI_ICON foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_ICON is
'��¼POI��3DIcon��Ϣ';

comment on column IX_POI_ICON.REL_ID is
'����';

comment on column IX_POI_ICON.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_ICON.ICON_NAME is
'[170]�ο�"AU_MULTIMEDIA"��"NAME"';

comment on column IX_POI_ICON.GEOMETRY is
'[173sp1]';

comment on column IX_POI_ICON.MANAGE_CODE is
'[170]';

comment on column IX_POI_ICON.CLIENT_FLAG is
'[170]��ݲ�ͬ�ͻ�����,�����ͬ����,ֵ�����:
MB ����
HD �㱾
TY ����
PI �ȷ�
PA ����
NE NavEx
G MIFG
13CY 13CY
NBT ����
ע:
(1)����ÿһ�����ʾֻ�����ĳһ�ͻ�,��ֻ������,��ʾΪ"MB"
(2)����ʾ������ĳһ�ͻ��������ͻ�,�������ϴ���ǰ��Ӣ�İ��"-",��������������Ŀͻ�,���ʾΪ"-MB"
(3)���֮����Ӣ�İ��"|"�ָ�,���ʾ��������������,���ʾΪ"MB|-TY"
(4)Ĭ��Ϊ��,��ʾ���пͻ������';

comment on column IX_POI_ICON.MEMO is
'[170]';

comment on column IX_POI_ICON.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_ICON.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_INTRODUCTION                                   */
/*==============================================================*/
create table IX_POI_INTRODUCTION  (
   INTRODUCTION_ID      NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                     default 0 not null,
   INTRODUCTION         VARCHAR2(1000),
   INTRODUCTION_ENG     VARCHAR2(1000),
   WEBSITE              VARCHAR2(500),
   NEIGHBOR             VARCHAR2(500),
   NEIGHBOR_ENG         VARCHAR2(500),
   TRAFFIC              VARCHAR2(500),
   TRAFFIC_ENG          VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_INTRODUCTION primary key (INTRODUCTION_ID)
);

comment on table IX_POI_INTRODUCTION is
'[170]��¼��ǰPOI �����������,�����̳�,��԰�ȵر꽨��,��ɻ�,��վ,����վ,����վ���ܱ߽�ͨ��Ŧ,�Լ�POI ����Щ�ر����Ŧ�ľ���,��λ:KM';

comment on column IX_POI_INTRODUCTION.INTRODUCTION_ID is
'����';

comment on column IX_POI_INTRODUCTION.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_INTRODUCTION.INTRODUCTION is
'����ȫ���ַ�, ����ŷ������ʹ��ϰ��';

comment on column IX_POI_INTRODUCTION.NEIGHBOR is
'(1)POI ����ĵر��Խ�����,��������,�����̳�,��԰������������������
(2)����ȫ���ַ�,����ر�ʱ�ԡ���ָ�,��:�������������Ƶ� �Ͻ�ǣ��찲�Ź㳡����������֣�ʥԼɪ��';

comment on column IX_POI_INTRODUCTION.TRAFFIC is
'(1)POI ����Ĵ��ͽ�ͨ��Ŧ(��Ҫ�л�,��վ,�����վ,�ۿ���ͷ)�Լ�����Щ��Ŧ�Ĵ��¾���
(2)����ȫ���ַ�,��Ŧ��������Ϊ�����Ŧ�Ĵ��¾���,��λΪKM,�����Ŧʱ�ԡ���ָ�,��:�������������Ƶ� �����׶���ʻ����������վ����.����';

comment on column IX_POI_INTRODUCTION.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_INTRODUCTION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_NAME                                           */
/*==============================================================*/
create table IX_POI_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,3,4,5,6,7,8,9)),
   NAME_TYPE            NUMBER(2)                      default 1 not null
       check (NAME_TYPE in (1,2)),
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   NAME_PHONETIC        VARCHAR2(1000),
   KEYWORDS             VARCHAR2(254),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_NAME primary key (NAME_ID),
   constraint IXPOI_NAME foreign key (POI_PID)
         references IX_POI (PID)
);

comment on column IX_POI_NAME.NAME_ID is
'����';

comment on column IX_POI_NAME.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_NAME.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column IX_POI_NAME.NAME_CLASS is
'[180U]';

comment on column IX_POI_NAME.NAME_TYPE is
'[180A]';

comment on column IX_POI_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column IX_POI_NAME.NAME_PHONETIC is
'[171U]';

comment on column IX_POI_NAME.KEYWORDS is
'��¼POI ��������ƴ���ؼ��ֻ�������,�ؼ���֮����Ӣ�İ��"/"�ָ�,��"����������"�ؼ��ֻ���Ϊ:"bei jing shi/zheng fu';

comment on column IX_POI_NAME.NIDB_PID is
'��¼����POI���Ѿ���Ʒ������ID,��ͬ��������PID��ͬ';

comment on column IX_POI_NAME.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_NAME_FLAG                                      */
/*==============================================================*/
create table IX_POI_NAME_FLAG  (
   NAME_ID              NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_NAMEFLAG foreign key (NAME_ID)
         references IX_POI_NAME (NAME_ID)
);

comment on table IX_POI_NAME_FLAG is
'[180]';

comment on column IX_POI_NAME_FLAG.NAME_ID is
'���,����"IX_POI_NAME"';

comment on column IX_POI_NAME_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column IX_POI_NAME_FLAG.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_NAME_FLAG.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_NAME_TONE                                      */
/*==============================================================*/
create table IX_POI_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOINAME_TONE foreign key (NAME_ID)
         references IX_POI_NAME (NAME_ID)
);

comment on table IX_POI_NAME_TONE is
'[170]';

comment on column IX_POI_NAME_TONE.NAME_ID is
'���,����"IX_POI_NAME"';

comment on column IX_POI_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column IX_POI_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column IX_POI_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column IX_POI_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column IX_POI_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column IX_POI_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
   RES_HIGH             NUMBER(5,2)                    default 0 not null,
   RES_WIDTH            NUMBER(5,2)                    default 0 not null,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   CERTIFICATE          NUMBER(1)                      default 0 not null
       check (CERTIFICATE in (0,1,2,3)),
   VEHICLE              NUMBER(1)                      default 0 not null
       check (VEHICLE in (0,1,2,3)),
   PHOTO_NAME           VARCHAR2(100),
   HAVE_SPECIALPLACE    VARCHAR2(4),
   WOMEN_NUM            NUMBER(4)                      default 0 not null,
   HANDICAP_NUM         NUMBER(4)                      default 0 not null,
   MINI_NUM             NUMBER(4)                      default 0 not null,
   VIP_NUM              NUMBER(4)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_PARKING primary key (PARKING_ID)
);

comment on table IX_POI_PARKING is
'[190A]';

comment on column IX_POI_PARKING.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_PARKING.PARKING_TYPE is
'ֵ�����:
���� ���
0	���ڣ�����ֵ��ϵ��£�
1	����
2	ռ��
3	���ڣ����ϣ�
4	���ڣ����£�
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_PARKING.TOLL_STD is
'ֵ�����:
���� ���
0 ����
1 ����
2 �ƴ�
3 ��ʱ
4 �ֶμƼ�
5 ���
�����׼ʱ����Ӣ�İ��"|"�ָ�
���Ϊ�ձ�ʾδ����,��5(���)�����������͹���';

comment on column IX_POI_PARKING.TOLL_WAY is
'ֵ�����:
���� ���
0 �˹��շ�
1 �����շ�
2 ����ɷ�
�����׼ʱ����Ӣ�İ��"|"�ָ�';

comment on column IX_POI_PARKING.PAYMENT is
'ֵ�����:
����	���
10	���_ͨ
11	VISA
12	MasterCard
13	�F��
14	����
15	��ֵ��
�����׼ʱ����Ӣ�İ��"|"�ָ�
�۰Ķ����ֶΣ���½ʱΪ��
';

comment on column IX_POI_PARKING.REMARK is
'ֵ�����: 
��½��ֻ�����һ��ֵ
����	���
0	���������
1	ס�����
2	�Ͳ����
3	�������
4	������������ⲿ�ַ���
5	��ͣ�������ڵ�����POI������ѡ����¡����ʡ��Һš���ҽ�ȹ�ϵʱ���
6	ֻ���ڻ������ѵ�Ⱥ�忪��
7	������
�۰ģ���ֵʱ����Ӣ�İ��"|"�ָ�
����	���
11	����C
12	�����L��
13	��ͣ؛܇
14	��ʱ�����M
15	늄�܇���
16	����
17	ϴ܇����Ϟ
';

comment on column IX_POI_PARKING.SOURCE is
'1         �����ֳ����ƻ���������˵��
2         ����ѯ��
3         ���Ա��ƺ�ѯ��
';

comment on column IX_POI_PARKING.WORK_TIME is
'�������������,��:2012-08-10';

comment on column IX_POI_PARKING.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_PARKING.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_PHOTO                                          */
/*==============================================================*/
create table IX_POI_PHOTO  (
   POI_PID              NUMBER(10)                      not null,
   PHOTO_ID             NUMBER(10)                     default 0 not null,
   PID                  VARCHAR2(32),
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   TAG                  NUMBER(3)                      default 1 not null
       check (TAG in (1,2,3,4,5,7,100)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_PHOTO foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_PHOTO is
'[170]';

comment on column IX_POI_PHOTO.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_PHOTO.PHOTO_ID is
'�ο�"AU_PHOTO"';

comment on column IX_POI_PHOTO.STATUS is
'��¼�Ƿ�ȷ��';

comment on column IX_POI_PHOTO.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_PHOTO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (PARKING in (0,1,2,3)),
   LONG_DESCRIPTION     VARCHAR2(254),
   LONG_DESCRIP_ENG     VARCHAR2(254),
   OPEN_HOUR            VARCHAR2(254),
   OPEN_HOUR_ENG        VARCHAR2(254),
   TELEPHONE            VARCHAR2(100),
   ADDRESS              VARCHAR2(200),
   CITY                 VARCHAR2(50),
   PHOTO_NAME           VARCHAR2(254),
   TRAVELGUIDE_FLAG     NUMBER(1)                      default 0 not null
       check (TRAVELGUIDE_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_RESTAURANT primary key (RESTAURANT_ID)
);

comment on table IX_POI_RESTAURANT is
'[170][190U][210]';

comment on column IX_POI_RESTAURANT.RESTAURANT_ID is
'����';

comment on column IX_POI_RESTAURANT.POI_PID is
'�ο�"IX_POI"';

comment on column IX_POI_RESTAURANT.FOOD_TYPE is
'��¼���ֲ�ϵ���ʹ���,��³��,����,�ձ�����,����˵�,�����ϵ֮����"|"�ָ�;��Ϊδ����';

comment on column IX_POI_RESTAURANT.CREDIT_CARD is
'ֵ�����:
1 άʿ(visa)
2 ���´�(mastercard)
3 ����(dinas)
4 �ձ�������ÿ�(jcb)
5 ������ͨ(America
Express)
6 ����(unionpay)
�������ʱ����Ӣ�İ�ǡ�|���ָ�
���Ϊ0 ��ʾ��֧�����ÿ�
���Ϊ�ձ�ʾδ����';

comment on column IX_POI_RESTAURANT.AVG_COST is
'���Ϊ0 ��ʾδ����';

comment on column IX_POI_RESTAURANT.CITY is
'�洢��ʽ��';

comment on column IX_POI_RESTAURANT.PHOTO_NAME is
'�����Ƭʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_RESTAURANT.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_RESTAURANT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POI_TOURROUTE primary key (TOUR_ID)
);

comment on table IX_POI_TOURROUTE is
'[190A][210]';

comment on column IX_POI_TOURROUTE.POI_PID is
'���POI ʱ����Ӣ�İ�ǡ�|���ָ�';

comment on column IX_POI_TOURROUTE.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_TOURROUTE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POI_VIDEO                                          */
/*==============================================================*/
create table IX_POI_VIDEO  (
   POI_PID              NUMBER(10)                      not null,
   VIDEO_ID             NUMBER(10)                     default 0 not null,
   PID                  VARCHAR2(32),
   STATUS               VARCHAR2(100),
   MEMO                 VARCHAR2(500),
   TAG                  NUMBER(3)                      default 0 not null
       check (TAG in (0)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_VIDEO foreign key (POI_PID)
         references IX_POI (PID)
);

comment on table IX_POI_VIDEO is
'[170]';

comment on column IX_POI_VIDEO.POI_PID is
'���,����"IX_POI"';

comment on column IX_POI_VIDEO.VIDEO_ID is
'�ο�"AU_VIDEO"';

comment on column IX_POI_VIDEO.STATUS is
'��¼�Ƿ�ȷ��';

comment on column IX_POI_VIDEO.U_RECORD is
'�������±�ʶ';

comment on column IX_POI_VIDEO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_POSTCODE                                           */
/*==============================================================*/
create table IX_POSTCODE  (
   POST_ID              NUMBER(10)                      not null,
   POST_CODE            VARCHAR2(6),
   GEOMETRY             SDO_GEOMETRY,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_POSTCODE primary key (POST_ID)
);

comment on table IX_POSTCODE is
'�ʱ���ʵ���ʼ������ּ������ͨ��ר�ô��,Ŀ��������ż��ڴ��ݹ���е��ٶȺ�׼ȷ��';

comment on column IX_POSTCODE.POST_ID is
'����';

comment on column IX_POSTCODE.POST_CODE is
'�洢Ϊ6λӢ�İ������';

comment on column IX_POSTCODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_POSTCODE.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_POSTCODE.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_POSTCODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_POSTCODE.U_RECORD is
'�������±�ʶ';

comment on column IX_POSTCODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_ROADNAME                                           */
/*==============================================================*/
create table IX_ROADNAME  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_ROADNAME primary key (PID)
);

comment on column IX_ROADNAME.PID is
'����';

comment on column IX_ROADNAME.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_ROADNAME.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_ROADNAME.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_ROADNAME.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_ROADNAME.U_RECORD is
'�������±�ʶ';

comment on column IX_ROADNAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_SAMEPOI                                            */
/*==============================================================*/
create table IX_SAMEPOI  (
   GROUP_ID             NUMBER(10)                      not null,
   RELATION_TYPE        NUMBER(1)                      default 1 not null
       check (RELATION_TYPE in (1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_SAMEPOI primary key (GROUP_ID)
);

comment on column IX_SAMEPOI.GROUP_ID is
'����';

comment on column IX_SAMEPOI.U_RECORD is
'�������±�ʶ';

comment on column IX_SAMEPOI.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_SAMEPOI_PART                                       */
/*==============================================================*/
create table IX_SAMEPOI_PART  (
   GROUP_ID             NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint IXPOI_GROUP foreign key (GROUP_ID)
         references IX_SAMEPOI (GROUP_ID),
   constraint IXPOI_SAMEPOIPART foreign key (POI_PID)
         references IX_POI (PID)
);

comment on column IX_SAMEPOI_PART.GROUP_ID is
'���,����"IX_SAMEPOI"';

comment on column IX_SAMEPOI_PART.POI_PID is
'���,����"IX_POI"';

comment on column IX_SAMEPOI_PART.U_RECORD is
'�������±�ʶ';

comment on column IX_SAMEPOI_PART.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: IX_TOLLGATE                                           */
/*==============================================================*/
create table IX_TOLLGATE  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   NAME                 VARCHAR2(60),
   PINYIN               VARCHAR2(1000),
   NAME_ENG             VARCHAR2(200),
   NAME_POR             VARCHAR2(200),
   KIND_CODE            VARCHAR2(8)                    
       check (KIND_CODE is null or (KIND_CODE in ('230209'))),
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DIF_GROUPID          VARCHAR2(200),
   RESERVED             VARCHAR2(1000),
   LINK_PID             NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_IX_TOLLGATE primary key (PID)
);

comment on column IX_TOLLGATE.PID is
'����';

comment on column IX_TOLLGATE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column IX_TOLLGATE.NAME_GROUPID is
'[170]�ο�"RD_NAME"';

comment on column IX_TOLLGATE.ROAD_FLAG is
'[170]';

comment on column IX_TOLLGATE.NAME_POR is
'[170]';

comment on column IX_TOLLGATE.KIND_CODE is
'[180U]';

comment on column IX_TOLLGATE.MESH_ID_5K is
'��¼�������ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column IX_TOLLGATE.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column IX_TOLLGATE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column IX_TOLLGATE.DIF_GROUPID is
'[181A]���ڲ�ָ�����ݰ�Ĳ�Ʒ�汾����,�����ڶ��,���ð��"|"�ָ�';

comment on column IX_TOLLGATE.RESERVED is
'[181A]';

comment on column IX_TOLLGATE.U_RECORD is
'�������±�ʶ';

comment on column IX_TOLLGATE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE                                               */
/*==============================================================*/
create table LC_FACE  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   DISPLAY_CLASS        NUMBER(1)                      default 0 not null
       check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)),
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE primary key (FACE_PID)
);

comment on column LC_FACE.FACE_PID is
'����';

comment on column LC_FACE.FEATURE_PID is
'�ο�"LC_FEATURE"';

comment on column LC_FACE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column LC_FACE.KIND is
'ע:
(1)2.5 ���20 �����:0~6,11~17
(2)�����TOP �����:0~6,17';

comment on column LC_FACE.FORM is
'ע:
(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_FACE.DISPLAY_CLASS is
'����ˮϵ�����Ӵ�С��Ϊ1~8 ��
ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE.AREA is
'��λ:ƽ����';

comment on column LC_FACE.PERIMETER is
'��λ:��';

comment on column LC_FACE.SCALE is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE.DETAIL_FLAG is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_FACE.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_100W                                          */
/*==============================================================*/
create table LC_FACE_100W  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   DISPLAY_CLASS        NUMBER(1)                      default 0 not null
       check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)),
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_100W primary key (FACE_PID)
);

comment on column LC_FACE_100W.FACE_PID is
'����';

comment on column LC_FACE_100W.FEATURE_PID is
'�ο�"LC_FEATURE_100W"';

comment on column LC_FACE_100W.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column LC_FACE_100W.KIND is
'ע:
(1)2.5 ���20 �����:0~6,11~17
(2)�����TOP �����:0~6,17';

comment on column LC_FACE_100W.FORM is
'ע:
(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_FACE_100W.DISPLAY_CLASS is
'����ˮϵ�����Ӵ�С��Ϊ1~8 ��
ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_100W.AREA is
'��λ:ƽ����';

comment on column LC_FACE_100W.PERIMETER is
'��λ:��';

comment on column LC_FACE_100W.SCALE is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_100W.DETAIL_FLAG is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_100W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_FACE_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_20W                                           */
/*==============================================================*/
create table LC_FACE_20W  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   DISPLAY_CLASS        NUMBER(1)                      default 0 not null
       check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)),
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_20W primary key (FACE_PID)
);

comment on column LC_FACE_20W.FACE_PID is
'����';

comment on column LC_FACE_20W.FEATURE_PID is
'�ο�"LC_FEATURE_20W"';

comment on column LC_FACE_20W.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column LC_FACE_20W.KIND is
'ע:
(1)2.5 ���20 �����:0~6,11~17
(2)�����TOP �����:0~6,17';

comment on column LC_FACE_20W.FORM is
'ע:
(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_FACE_20W.DISPLAY_CLASS is
'����ˮϵ�����Ӵ�С��Ϊ1~8 ��
ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_20W.AREA is
'��λ:ƽ����';

comment on column LC_FACE_20W.PERIMETER is
'��λ:��';

comment on column LC_FACE_20W.SCALE is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_20W.DETAIL_FLAG is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_20W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_FACE_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_NAME                                          */
/*==============================================================*/
create table LC_FACE_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   FACE_PID             NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_NAME primary key (NAME_ID),
   constraint LCFACE_NAME foreign key (FACE_PID)
         references LC_FACE (FACE_PID)
);

comment on column LC_FACE_NAME.NAME_ID is
'[170]����';

comment on column LC_FACE_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column LC_FACE_NAME.FACE_PID is
'���,����"LC_FACE"';

comment on column LC_FACE_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column LC_FACE_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column LC_FACE_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

comment on column LC_FACE_NAME.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_NAME_100W                                     */
/*==============================================================*/
create table LC_FACE_NAME_100W  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   FACE_PID             NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_NAME_100W primary key (NAME_ID),
   constraint LCFACE_NAME_100W foreign key (FACE_PID)
         references LC_FACE_100W (FACE_PID)
);

comment on table LC_FACE_NAME_100W is
'[170]';

comment on column LC_FACE_NAME_100W.NAME_ID is
'[170]����';

comment on column LC_FACE_NAME_100W.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column LC_FACE_NAME_100W.FACE_PID is
'���,����"LC_FACE_100W"';

comment on column LC_FACE_NAME_100W.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column LC_FACE_NAME_100W.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column LC_FACE_NAME_100W.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column LC_FACE_NAME_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_NAME_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_NAME_20W                                      */
/*==============================================================*/
create table LC_FACE_NAME_20W  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   FACE_PID             NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_NAME_20W primary key (NAME_ID),
   constraint LCFACE_NAME_20W foreign key (FACE_PID)
         references LC_FACE_20W (FACE_PID)
);

comment on table LC_FACE_NAME_20W is
'[170]';

comment on column LC_FACE_NAME_20W.NAME_ID is
'����';

comment on column LC_FACE_NAME_20W.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column LC_FACE_NAME_20W.FACE_PID is
'���,����"LC_FACE_20W"';

comment on column LC_FACE_NAME_20W.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column LC_FACE_NAME_20W.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column LC_FACE_NAME_20W.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column LC_FACE_NAME_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_NAME_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_TOP                                           */
/*==============================================================*/
create table LC_FACE_TOP  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,11,12,13,14,15,16,17)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   DISPLAY_CLASS        NUMBER(1)                      default 0 not null
       check (DISPLAY_CLASS in (0,1,2,3,4,5,6,7,8)),
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_TOP primary key (FACE_PID)
);

comment on table LC_FACE_TOP is
'[170]';

comment on column LC_FACE_TOP.FACE_PID is
'����';

comment on column LC_FACE_TOP.FEATURE_PID is
'�ο�"LC_FEATURE_TOP"';

comment on column LC_FACE_TOP.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column LC_FACE_TOP.KIND is
'ע:
(1)2.5 ���20 �����:0~6,11~17
(2)�����TOP �����:0~6,17';

comment on column LC_FACE_TOP.FORM is
'ע:
(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_FACE_TOP.DISPLAY_CLASS is
'����ˮϵ�����Ӵ�С��Ϊ1~8 ��
ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_TOP.AREA is
'��λ:ƽ����';

comment on column LC_FACE_TOP.PERIMETER is
'��λ:��';

comment on column LC_FACE_TOP.SCALE is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_TOP.DETAIL_FLAG is
'ע:���ֶν�����2.5 �����,20 ��,�����TOP ����ݲ���Ҫ';

comment on column LC_FACE_TOP.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_FACE_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_NAME_TOP                                      */
/*==============================================================*/
create table LC_FACE_NAME_TOP  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   FACE_PID             NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FACE_NAME_TOP primary key (NAME_ID),
   constraint LCFACE_NAME_TOP foreign key (FACE_PID)
         references LC_FACE_TOP (FACE_PID)
);

comment on table LC_FACE_NAME_TOP is
'[170]';

comment on column LC_FACE_NAME_TOP.NAME_ID is
'����';

comment on column LC_FACE_NAME_TOP.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column LC_FACE_NAME_TOP.FACE_PID is
'���,����"LC_FACE_TOP"';

comment on column LC_FACE_NAME_TOP.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column LC_FACE_NAME_TOP.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column LC_FACE_NAME_TOP.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column LC_FACE_NAME_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_NAME_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE                                               */
/*==============================================================*/
create table LC_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_NODE primary key (NODE_PID)
);

comment on column LC_NODE.NODE_PID is
'����';

comment on column LC_NODE.FORM is
'ͼ����,�ǵ�';

comment on column LC_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column LC_NODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_NODE.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK                                               */
/*==============================================================*/
create table LC_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_LINK primary key (LINK_PID),
   constraint LCLINK_SNODE foreign key (S_NODE_PID)
         references LC_NODE (NODE_PID),
   constraint LCLINK_ENODE foreign key (E_NODE_PID)
         references LC_NODE (NODE_PID)
);

comment on column LC_LINK.LINK_PID is
'����';

comment on column LC_LINK.S_NODE_PID is
'���,����"LC_NODE"';

comment on column LC_LINK.E_NODE_PID is
'���,����"LC_NODE"';

comment on column LC_LINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column LC_LINK.LENGTH is
'��λ:��';

comment on column LC_LINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_LINK.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_TOPO                                          */
/*==============================================================*/
create table LC_FACE_TOPO  (
   FACE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCFACE_LINK foreign key (LINK_PID)
         references LC_LINK (LINK_PID),
   constraint LCFACE_LINKS foreign key (FACE_PID)
         references LC_FACE (FACE_PID)
);

comment on table LC_FACE_TOPO is
'��¼����Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column LC_FACE_TOPO.FACE_PID is
'���,����"LC_FACE"';

comment on column LC_FACE_TOPO.LINK_PID is
'���,����"LC_LINK"';

comment on column LC_FACE_TOPO.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column LC_FACE_TOPO.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_TOPO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_100W                                          */
/*==============================================================*/
create table LC_NODE_100W  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_NODE_100W primary key (NODE_PID)
);

comment on column LC_NODE_100W.NODE_PID is
'����';

comment on column LC_NODE_100W.FORM is
'ͼ����,�ǵ�';

comment on column LC_NODE_100W.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column LC_NODE_100W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_NODE_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_100W                                          */
/*==============================================================*/
create table LC_LINK_100W  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_LINK_100W primary key (LINK_PID),
   constraint LCLINK_SNODE_100W foreign key (S_NODE_PID)
         references LC_NODE_100W (NODE_PID),
   constraint LCLINK_ENODE_100W foreign key (E_NODE_PID)
         references LC_NODE_100W (NODE_PID)
);

comment on column LC_LINK_100W.LINK_PID is
'����';

comment on column LC_LINK_100W.S_NODE_PID is
'���,����"LC_NODE_100W"';

comment on column LC_LINK_100W.E_NODE_PID is
'���,����"LC_NODE_100W"';

comment on column LC_LINK_100W.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column LC_LINK_100W.LENGTH is
'��λ:��';

comment on column LC_LINK_100W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_LINK_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_TOPO_100W                                     */
/*==============================================================*/
create table LC_FACE_TOPO_100W  (
   FACE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCFACE_LINK_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID),
   constraint LCFACE_LINKS_100W foreign key (FACE_PID)
         references LC_FACE_100W (FACE_PID)
);

comment on table LC_FACE_TOPO_100W is
'��¼����Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column LC_FACE_TOPO_100W.FACE_PID is
'���,����"LC_FACE_100W"';

comment on column LC_FACE_TOPO_100W.LINK_PID is
'���,����"LC_LINK_100W"';

comment on column LC_FACE_TOPO_100W.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column LC_FACE_TOPO_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_TOPO_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_20W                                           */
/*==============================================================*/
create table LC_NODE_20W  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_NODE_20W primary key (NODE_PID)
);

comment on column LC_NODE_20W.NODE_PID is
'����';

comment on column LC_NODE_20W.FORM is
'ͼ����,�ǵ�';

comment on column LC_NODE_20W.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column LC_NODE_20W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_NODE_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_20W                                           */
/*==============================================================*/
create table LC_LINK_20W  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_LINK_20W primary key (LINK_PID),
   constraint LCLINK_SNODE_20W foreign key (S_NODE_PID)
         references LC_NODE_20W (NODE_PID),
   constraint LCLINK_ENODE_20W foreign key (E_NODE_PID)
         references LC_NODE_20W (NODE_PID)
);

comment on column LC_LINK_20W.LINK_PID is
'����';

comment on column LC_LINK_20W.S_NODE_PID is
'���,����"LC_NODE_20W"';

comment on column LC_LINK_20W.E_NODE_PID is
'���,����"LC_NODE_20W"';

comment on column LC_LINK_20W.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column LC_LINK_20W.LENGTH is
'��λ:��';

comment on column LC_LINK_20W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_LINK_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_TOPO_20W                                      */
/*==============================================================*/
create table LC_FACE_TOPO_20W  (
   LINK_PID             NUMBER(10)                      not null,
   FACE_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCFACE_LINK_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID),
   constraint LCFACE_LINKS_20W foreign key (FACE_PID)
         references LC_FACE_20W (FACE_PID)
);

comment on table LC_FACE_TOPO_20W is
'��¼����Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column LC_FACE_TOPO_20W.LINK_PID is
'���,����"LC_LINK_20W"';

comment on column LC_FACE_TOPO_20W.FACE_PID is
'���,����"LC_FACE_20W"';

comment on column LC_FACE_TOPO_20W.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column LC_FACE_TOPO_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_TOPO_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_TOP                                           */
/*==============================================================*/
create table LC_NODE_TOP  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_NODE_TOP primary key (NODE_PID)
);

comment on table LC_NODE_TOP is
'[170]';

comment on column LC_NODE_TOP.NODE_PID is
'����';

comment on column LC_NODE_TOP.FORM is
'ͼ����,�ǵ�';

comment on column LC_NODE_TOP.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������
';

comment on column LC_NODE_TOP.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_NODE_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_TOP                                           */
/*==============================================================*/
create table LC_LINK_TOP  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_LINK_TOP primary key (LINK_PID),
   constraint LCLINK_SNODE_TOP foreign key (S_NODE_PID)
         references LC_NODE_TOP (NODE_PID),
   constraint LCLINK_ENODE_TOP foreign key (E_NODE_PID)
         references LC_NODE_TOP (NODE_PID)
);

comment on table LC_LINK_TOP is
'[170]';

comment on column LC_LINK_TOP.LINK_PID is
'����';

comment on column LC_LINK_TOP.S_NODE_PID is
'���,����"LC_NODE_TOP"';

comment on column LC_LINK_TOP.E_NODE_PID is
'���,����"LC_NODE_TOP"';

comment on column LC_LINK_TOP.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column LC_LINK_TOP.LENGTH is
'��λ:��';

comment on column LC_LINK_TOP.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LC_LINK_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FACE_TOPO_TOP                                      */
/*==============================================================*/
create table LC_FACE_TOPO_TOP  (
   FACE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCFACE_LINK_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID),
   constraint LCFACE_LINKS_TOP foreign key (FACE_PID)
         references LC_FACE_TOP (FACE_PID)
);

comment on table LC_FACE_TOPO_TOP is
'[170]��¼����Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column LC_FACE_TOPO_TOP.FACE_PID is
'���,����"LC_FACE_TOP"';

comment on column LC_FACE_TOPO_TOP.LINK_PID is
'���,����"LC_LINK_TOP"';

comment on column LC_FACE_TOPO_TOP.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column LC_FACE_TOPO_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_FACE_TOPO_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FEATURE                                            */
/*==============================================================*/
create table LC_FEATURE  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FEATURE primary key (FEATURE_PID)
);

comment on table LC_FEATURE is
'���ظ���Ҫ��,����ˮϵ(����,�Ӵ���,���ӳ�,ˮ��,����,�˺�),�̵�(�����ֵ�,�ݵ�,�̻���,��԰,�����),��NavEx�е�Carto��Ӧ';

comment on column LC_FEATURE.FEATURE_PID is
'����';

comment on column LC_FEATURE.U_RECORD is
'�������±�ʶ';

comment on column LC_FEATURE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FEATURE_100W                                       */
/*==============================================================*/
create table LC_FEATURE_100W  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FEATURE_100W primary key (FEATURE_PID)
);

comment on table LC_FEATURE_100W is
'���ظ���Ҫ��,����ˮϵ(����,�Ӵ���,���ӳ�,ˮ��,����,�˺�),�̵�(�����ֵ�,�ݵ�,�̻���,��԰,�����),��NavEx�е�Carto��Ӧ';

comment on column LC_FEATURE_100W.FEATURE_PID is
'����';

comment on column LC_FEATURE_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_FEATURE_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FEATURE_20W                                        */
/*==============================================================*/
create table LC_FEATURE_20W  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FEATURE_20W primary key (FEATURE_PID)
);

comment on table LC_FEATURE_20W is
'���ظ���Ҫ��,����ˮϵ(����,�Ӵ���,���ӳ�,ˮ��,����,�˺�),�̵�(�����ֵ�,�ݵ�,�̻���,��԰,�����),��NavEx�е�Carto��Ӧ';

comment on column LC_FEATURE_20W.FEATURE_PID is
'����';

comment on column LC_FEATURE_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_FEATURE_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_FEATURE_TOP                                        */
/*==============================================================*/
create table LC_FEATURE_TOP  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LC_FEATURE_TOP primary key (FEATURE_PID)
);

comment on table LC_FEATURE_TOP is
'[170]���ظ���Ҫ��,����ˮϵ(����,�Ӵ���,���ӳ�,ˮ��,����,�˺�),�̵�(�����ֵ�,�ݵ�,�̻���,��԰,�����),��NavEx�е�Carto��Ӧ';

comment on column LC_FEATURE_TOP.FEATURE_PID is
'����';

comment on column LC_FEATURE_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_FEATURE_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_KIND                                          */
/*==============================================================*/
create table LC_LINK_KIND  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_KIND foreign key (LINK_PID)
         references LC_LINK (LINK_PID)
);

comment on table LC_LINK_KIND is
'��¼Link��ˮϵ,�̵��ֱ�';

comment on column LC_LINK_KIND.LINK_PID is
'���,����"LC_LINK"';

comment on column LC_LINK_KIND.KIND is
'ע:
(1)2.5 ���20 �����:0~8,11~18
(2)�����TOP �����:0~6,8,17';

comment on column LC_LINK_KIND.FORM is
'(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_LINK_KIND.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_KIND.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_KIND_100W                                     */
/*==============================================================*/
create table LC_LINK_KIND_100W  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_KIND_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID)
);

comment on table LC_LINK_KIND_100W is
'��¼Link��ˮϵ,�̵��ֱ�';

comment on column LC_LINK_KIND_100W.LINK_PID is
'���,����"LC_LINK_100W"';

comment on column LC_LINK_KIND_100W.KIND is
'ע:
(1)2.5 ���20 �����:0~8,11~18
(2)�����TOP �����:0~6,8,17';

comment on column LC_LINK_KIND_100W.FORM is
'(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_LINK_KIND_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_KIND_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_KIND_20W                                      */
/*==============================================================*/
create table LC_LINK_KIND_20W  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_KIND_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID)
);

comment on table LC_LINK_KIND_20W is
'��¼Link��ˮϵ,�̵��ֱ�';

comment on column LC_LINK_KIND_20W.LINK_PID is
'���,����"LC_LINK_20W"';

comment on column LC_LINK_KIND_20W.KIND is
'ע:
(1)2.5 ���20 �����:0~8,11~18
(2)�����TOP �����:0~6,8,17';

comment on column LC_LINK_KIND_20W.FORM is
'(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_LINK_KIND_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_KIND_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_KIND_TOP                                      */
/*==============================================================*/
create table LC_LINK_KIND_TOP  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,13,14,15,16,17,18)),
   FORM                 NUMBER(2)                      default 0 not null
       check (FORM in (0,1,2,3,4,8,9,10)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_KIND_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID)
);

comment on table LC_LINK_KIND_TOP is
'[170]��¼Link��ˮϵ,�̵��ֱ�';

comment on column LC_LINK_KIND_TOP.LINK_PID is
'���,����"LC_LINK_TOP"';

comment on column LC_LINK_KIND_TOP.KIND is
'ע:
(1)2.5 ���20 �����:0~8,11~18
(2)�����TOP �����:0~6,8,17';

comment on column LC_LINK_KIND_TOP.FORM is
'(1)2.5 ���20 �����:0~4
(2)�����TOP �����:0~4,8,9
(3)��KIND=17 ʱ,ȡֵ1~4,��KIND=3 ʱ,ȡֵ8,9,����ȡֵ0';

comment on column LC_LINK_KIND_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_KIND_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_MESH                                          */
/*==============================================================*/
create table LC_LINK_MESH  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_MESH foreign key (LINK_PID)
         references LC_LINK (LINK_PID)
);

comment on column LC_LINK_MESH.LINK_PID is
'���,����"LC_LINK"';

comment on column LC_LINK_MESH.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_MESH_100W                                     */
/*==============================================================*/
create table LC_LINK_MESH_100W  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_MESH_100W foreign key (LINK_PID)
         references LC_LINK_100W (LINK_PID)
);

comment on column LC_LINK_MESH_100W.LINK_PID is
'���,����"LC_LINK_100W"';

comment on column LC_LINK_MESH_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_MESH_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_MESH_20W                                      */
/*==============================================================*/
create table LC_LINK_MESH_20W  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_MESH_20W foreign key (LINK_PID)
         references LC_LINK_20W (LINK_PID)
);

comment on column LC_LINK_MESH_20W.LINK_PID is
'���,����"LC_LINK_20W"';

comment on column LC_LINK_MESH_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_MESH_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_LINK_MESH_TOP                                      */
/*==============================================================*/
create table LC_LINK_MESH_TOP  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCLINK_MESH_TOP foreign key (LINK_PID)
         references LC_LINK_TOP (LINK_PID)
);

comment on column LC_LINK_MESH_TOP.LINK_PID is
'���,����"LC_LINK_TOP"';

comment on column LC_LINK_MESH_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_LINK_MESH_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_MESH                                          */
/*==============================================================*/
create table LC_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCNODE_MESH foreign key (NODE_PID)
         references LC_NODE (NODE_PID)
);

comment on column LC_NODE_MESH.NODE_PID is
'���,����"LC_NODE"';

comment on column LC_NODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_MESH_100W                                     */
/*==============================================================*/
create table LC_NODE_MESH_100W  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCNODE_MESH_100W foreign key (NODE_PID)
         references LC_NODE_100W (NODE_PID)
);

comment on column LC_NODE_MESH_100W.NODE_PID is
'���,����"LC_NODE_100W"';

comment on column LC_NODE_MESH_100W.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_MESH_100W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_MESH_20W                                      */
/*==============================================================*/
create table LC_NODE_MESH_20W  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCNODE_MESH_20W foreign key (NODE_PID)
         references LC_NODE_20W (NODE_PID)
);

comment on column LC_NODE_MESH_20W.NODE_PID is
'���,����"LC_NODE_20W"';

comment on column LC_NODE_MESH_20W.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_MESH_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LC_NODE_MESH_TOP                                      */
/*==============================================================*/
create table LC_NODE_MESH_TOP  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LCNODE_MESH_TOP foreign key (NODE_PID)
         references LC_NODE_TOP (NODE_PID)
);

comment on table LC_NODE_MESH_TOP is
'[170]';

comment on column LC_NODE_MESH_TOP.NODE_PID is
'���,����"LC_NODE_TOP"';

comment on column LC_NODE_MESH_TOP.U_RECORD is
'�������±�ʶ';

comment on column LC_NODE_MESH_TOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_FACE                                               */
/*==============================================================*/
create table LU_FACE  (
   FACE_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,11,12,21,22,23,24,30,31,32,33,34,35,36,37,38,39,40,41)),
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LU_FACE primary key (FACE_PID)
);

comment on column LU_FACE.FACE_PID is
'����';

comment on column LU_FACE.FEATURE_PID is
'�ο�"LU_FEATURE"';

comment on column LU_FACE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column LU_FACE.KIND is
'��ѧ,ҽԺ,�������ĵ�';

comment on column LU_FACE.AREA is
'��λ:ƽ����';

comment on column LU_FACE.PERIMETER is
'��λ:��';

comment on column LU_FACE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LU_FACE.U_RECORD is
'�������±�ʶ';

comment on column LU_FACE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_FACE_NAME                                          */
/*==============================================================*/
create table LU_FACE_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   FACE_PID             NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LU_FACE_NAME primary key (NAME_ID),
   constraint LUFACE_NAME foreign key (FACE_PID)
         references LU_FACE (FACE_PID)
);

comment on column LU_FACE_NAME.NAME_ID is
'[170]����';

comment on column LU_FACE_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column LU_FACE_NAME.FACE_PID is
'���,����"LU_FACE"';

comment on column LU_FACE_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column LU_FACE_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column LU_FACE_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ
ע:
(1)BUA ȡֵ0~1
(2)����ȡֵ0';

comment on column LU_FACE_NAME.U_RECORD is
'�������±�ʶ';

comment on column LU_FACE_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_NODE                                               */
/*==============================================================*/
create table LU_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LU_NODE primary key (NODE_PID)
);

comment on column LU_NODE.NODE_PID is
'����';

comment on column LU_NODE.FORM is
'ͼ����,�ǵ�';

comment on column LU_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column LU_NODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LU_NODE.U_RECORD is
'�������±�ʶ';

comment on column LU_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_LINK                                               */
/*==============================================================*/
create table LU_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LU_LINK primary key (LINK_PID),
   constraint LULINK_NODE foreign key (S_NODE_PID)
         references LU_NODE (NODE_PID),
   constraint LULINK_ENODE foreign key (E_NODE_PID)
         references LU_NODE (NODE_PID)
);

comment on column LU_LINK.LINK_PID is
'����';

comment on column LU_LINK.S_NODE_PID is
'���,����"LU_NODE"';

comment on column LU_LINK.E_NODE_PID is
'���,����"LU_NODE"';

comment on column LU_LINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column LU_LINK.LENGTH is
'��λ:��';

comment on column LU_LINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column LU_LINK.U_RECORD is
'�������±�ʶ';

comment on column LU_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_FACE_TOPO                                          */
/*==============================================================*/
create table LU_FACE_TOPO  (
   FACE_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LUFACE_LINK foreign key (LINK_PID)
         references LU_LINK (LINK_PID),
   constraint LUFACE_LINKS foreign key (FACE_PID)
         references LU_FACE (FACE_PID)
);

comment on table LU_FACE_TOPO is
'��¼����Link֮������˹�ϵ,������ʱ�뷽��洢';

comment on column LU_FACE_TOPO.FACE_PID is
'���,����"LU_FACE"';

comment on column LU_FACE_TOPO.SEQ_NUM is
'����ʱ�뷽��,��1��ʼ�������';

comment on column LU_FACE_TOPO.LINK_PID is
'���,����"LU_LINK"';

comment on column LU_FACE_TOPO.U_RECORD is
'�������±�ʶ';

comment on column LU_FACE_TOPO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_FEATURE                                            */
/*==============================================================*/
create table LU_FEATURE  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_LU_FEATURE primary key (FEATURE_PID)
);

comment on table LU_FEATURE is
'��������Ҫ��,����BUA,������(��ѧ,ҽԺ,��������,����,��Ĺ,��,���ܵ�),��NavEx�е�Carto��Ӧ';

comment on column LU_FEATURE.FEATURE_PID is
'����';

comment on column LU_FEATURE.U_RECORD is
'�������±�ʶ';

comment on column LU_FEATURE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_LINK_KIND                                          */
/*==============================================================*/
create table LU_LINK_KIND  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,4,5,6,7,8,11,12,21,22,23,24,30,31,32,33,34,35,36,37,38,39,40,41)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LULINK_KIND foreign key (LINK_PID)
         references LU_LINK (LINK_PID)
);

comment on table LU_LINK_KIND is
'��¼Link��BUA,������(��ѧ,��������,ҽԺ��)���ֱ�';

comment on column LU_LINK_KIND.LINK_PID is
'���,����"LU_LINK"';

comment on column LU_LINK_KIND.KIND is
'��ѧ,ͣ����,�������ĵ���߽��ߵ�';

comment on column LU_LINK_KIND.U_RECORD is
'�������±�ʶ';

comment on column LU_LINK_KIND.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_LINK_MESH                                          */
/*==============================================================*/
create table LU_LINK_MESH  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LULINK_MESH foreign key (LINK_PID)
         references LU_LINK (LINK_PID)
);

comment on column LU_LINK_MESH.LINK_PID is
'���,����"LU_LINK"';

comment on column LU_LINK_MESH.U_RECORD is
'�������±�ʶ';

comment on column LU_LINK_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: LU_NODE_MESH                                          */
/*==============================================================*/
create table LU_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint LUNODE_MESH foreign key (NODE_PID)
         references LU_NODE (NODE_PID)
);

comment on column LU_NODE_MESH.NODE_PID is
'���,����"LU_NODE"';

comment on column LU_NODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column LU_NODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: M_MESH_TYPE                                           */
/*==============================================================*/
create table M_MESH_TYPE  (
   MESH_ID              NUMBER(6)                       not null,
   TYPE                 NUMBER(2)                      default 0 not null
       check (TYPE in (0,1,2)),
   MEMO                 VARCHAR2(500),
   constraint PK_M_MESH_TYPE primary key (MESH_ID)
);

comment on table M_MESH_TYPE is
'[171A]';

comment on column M_MESH_TYPE.MESH_ID is
'����';

/*==============================================================*/
/* Table: M_PARAMETER                                           */
/*==============================================================*/
create table M_PARAMETER  (
   NAME                 VARCHAR2(32),
   PARAMETER            VARCHAR2(32),
   DESCRIPTION          VARCHAR2(200)
);

comment on table M_PARAMETER is
'��¼ϵͳ����,����:ģ�Ͱ汾(GLM_VERSION),��ݲ���(DATA_VERSION),�����(MAP_SCALE),���ϵ(COORDINATE_SYS),��γ����굥λ(COORDINATE_UNIT),��γ����꾫ȷ��(XY_PRECISION),�߳���꾫��(Z_PRECISION),������Ϣ(REGION_INFO)';

comment on column M_PARAMETER.NAME is
'��:GLM_VERSION,DATA_VERSION,MAP_SCALE��';

comment on column M_PARAMETER.PARAMETER is
'��GLM_VERSION=1.6.1,DATA_VERSION=10winter';

/*==============================================================*/
/* Table: M_UPDATE_PARAMETER                                    */
/*==============================================================*/
create table M_UPDATE_PARAMETER  (
   DB_TYPE              VARCHAR2(100),
   VERSION_TYPE         VARCHAR2(100),
   VERSION_CODE         VARCHAR2(100),
   CREATE_TIME          VARCHAR2(100),
   DB_SOURCE_A          VARCHAR2(100),
   DB_SOURCE_B          VARCHAR2(100),
   CONTENT              VARCHAR2(100),
   DESCRIPT             VARCHAR2(1000),
   AVAILABLE_TYPE       VARCHAR2(100)
);

comment on table M_UPDATE_PARAMETER is
'[170]';

comment on column M_UPDATE_PARAMETER.DB_TYPE is
'�����ɹ�⡢��Ʒ�⡢��ֿ�';

comment on column M_UPDATE_PARAMETER.VERSION_TYPE is
'������ְ滹�ǻ��';

comment on column M_UPDATE_PARAMETER.VERSION_CODE is
'��汾��,��1.0.0;1.1.0 ��';

comment on column M_UPDATE_PARAMETER.CREATE_TIME is
'��ɿ������ʱ��';

comment on column M_UPDATE_PARAMETER.DB_SOURCE_A is
'����ҵ��,�ɹ��汾,ԭ��Ʒ��汾';

comment on column M_UPDATE_PARAMETER.DB_SOURCE_B is
'���ְ汾��Ч,�²�Ʒ��汾';

comment on column M_UPDATE_PARAMETER.CONTENT is
'��ȫҪ��,POI,��·,������';

comment on column M_UPDATE_PARAMETER.DESCRIPT is
'�������������,����ͳ�����ֵ�,�籱���������,��·200km,POI ��12000 ��';

comment on column M_UPDATE_PARAMETER.AVAILABLE_TYPE is
'����,����,GIS ��';

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
       check (DEL_FLAG in (0,1)),
   CREATED              DATE,
   UPDATED              DATE,
   MESH_ID              NUMBER(6),
   SCOPE_FLAG           NUMBER(2)                      default 1 not null
       check (SCOPE_FLAG in (1,2,3)),
   PROVINCE_NAME        VARCHAR2(60),
   MAP_SCALE            NUMBER(2)                      default 0 not null
       check (MAP_SCALE in (0,1,2,3)),
   RESERVED             VARCHAR2(1000),
   EXTENDED             VARCHAR2(1000),
   TASK_ID              VARCHAR2(500),
   QA_TASK_ID           VARCHAR2(500),
   QA_STATUS            NUMBER(2)                      default 2 not null
       check (QA_STATUS in (1,2)),
   WORKER               VARCHAR2(500),
   QA_WORKER            VARCHAR2(500),
   LOG_TYPE             NUMBER(5)                      default 0 not null,
   MD5_CODE             VARCHAR2(32)
);

comment on column NI_VAL_EXCEPTION.RULEID is
'�ο�"CK_RULE"';

comment on column NI_VAL_EXCEPTION.CREATED is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

comment on column NI_VAL_EXCEPTION.UPDATED is
'��ʽ"YYYY/MM/DD HH:mm:ss"';

/*==============================================================*/
/* Table: PT_COMPANY                                            */
/*==============================================================*/
create table PT_COMPANY  (
   COMPANY_ID           NUMBER(10)                      not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   CITY_CODE            NUMBER(6)                      default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_COMPANYID       VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_COMPANY primary key (COMPANY_ID)
);

comment on table PT_COMPANY is
'������˾��ָ���𹫽���·��ϵͳ��Ӫ�Ĺ�˾,�����?��ϵͳ���ϼ���λ';

comment on column PT_COMPANY.COMPANY_ID is
'����';

comment on column PT_COMPANY.PHONETIC is
'[171U]';

comment on column PT_COMPANY.SRC_FLAG is
'[170]';

comment on column PT_COMPANY.CITY_CODE is
'�洢����Ϊ4λ';

comment on column PT_COMPANY.DATA_SOURCE is
'[170]';

comment on column PT_COMPANY.UPDATE_BATCH is
'[170]';

comment on column PT_COMPANY.U_RECORD is
'�������±�ʶ';

comment on column PT_COMPANY.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_POI                                                */
/*==============================================================*/
create table PT_POI  (
   PID                  NUMBER(10)                      not null,
   POI_KIND             VARCHAR2(4),
   GEOMETRY             SDO_GEOMETRY,
   X_GUIDE              NUMBER(10,5)                   default 0 not null,
   Y_GUIDE              NUMBER(10,5)                   default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)),
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(6)                      default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   ACCESS_CODE          VARCHAR2(32),
   ACCESS_TYPE          VARCHAR2(10)                   default '0' not null
       check (ACCESS_TYPE in ('0','1','2','3')),
   ACCESS_METH          NUMBER(3)                      default 0 not null,
   MESH_ID_5K           VARCHAR2(10),
   MESH_ID              NUMBER(6)                      default 0 not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   POI_MEMO             VARCHAR2(200),
   OPERATOR             VARCHAR2(30),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   POI_NUM              VARCHAR2(100),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_POI primary key (PID)
);

comment on table PT_POI is
'����POI������(Stop POI)�ͳ����(Access POI)���������,ÿ�������Ӧһ����������,��������������POI.';

comment on column PT_POI.PID is
'����';

comment on column PT_POI.POI_KIND is
'�ο�"IX_POI_CODE"';

comment on column PT_POI.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column PT_POI.LINK_PID is
'�ο�"RD_LINK"';

comment on column PT_POI.SIDE is
'���POI����ʾ������POIλ�����·Link�ϻ������Ҳ�(�����Link����)';

comment on column PT_POI.NAME_GROUPID is
'[173sp2]�ο�"RD_NAME"';

comment on column PT_POI.ROAD_FLAG is
'[170]';

comment on column PT_POI.PMESH_ID is
'[171A]ÿ����ҵ��POI �ڳɹ���е�һ����LINK ������ʱ���,�Ҹ���ҵ�������½�����ʱ��ͼ��Ų���,�Ա�֤����ҵ��ÿ����ݷ�ʡת����һ����';

comment on column PT_POI.ACCESS_CODE is
'[170]���������е�˳��Ż���,��:��������A �ڡ�,����ǡ� A��;�����깬վ�����ڡ�,����ǡ� ������;������֮��վ���ſڡ�,����ǡ� ����.����������û�б�ŵ�ֵΪ��';

comment on column PT_POI.ACCESS_TYPE is
'����,���,�����';

comment on column PT_POI.ACCESS_METH is
'����8bit ��ʾ,���ҵ�������Ϊ0~7bit,ÿbit ��ʾһ�ַ�ʽ����(����),��ֵΪ0/1 �ֱ��ʾ��/��,��:00000011 ��ʾб�ºͽ���;00000101 ��ʾб�ºͷ���
��0bit:б��
��1bit:����
��2bit:����
��3bit:ֱ��
��4bit:����
�������bit λ��Ϊ0,��ʾ��Ӧ��';

comment on column PT_POI.MESH_ID_5K is
'��¼����POI���ڵ�5000ͼ���,��ʽΪ:605603_1_3';

comment on column PT_POI.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ��������Ҷ�Ӧ������������������';

comment on column PT_POI.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column PT_POI.POI_NUM is
'[173sp1]��¼����NIDB��POI���';

comment on column PT_POI.TASK_ID is
'[170]��¼��ҵ��������';

comment on column PT_POI.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column PT_POI.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column PT_POI.U_RECORD is
'�������±�ʶ';

comment on column PT_POI.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_ETA_ACCESS                                         */
/*==============================================================*/
create table PT_ETA_ACCESS  (
   POI_PID              NUMBER(10)                      not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   OPEN_PERIOD          VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)                    default '0' not null
       check (MANUAL_TICKET in ('0','1','2')),
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   AUTO_TICKET          VARCHAR2(1)                    default '0' not null
       check (AUTO_TICKET in ('0','1','2')),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTETA_ACCESS foreign key (POI_PID)
         references PT_POI (PID)
);

comment on table PT_ETA_ACCESS is
'������ʵ���繫���������߱��Ĺ��ܼ����ܱߵĸ���������ʩ��';

comment on column PT_ETA_ACCESS.POI_PID is
'���,����"PT_POI"';

comment on column PT_ETA_ACCESS.OPEN_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column PT_ETA_ACCESS.MANUAL_TICKET_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-�������˿���,����¼����';

comment on column PT_ETA_ACCESS.U_RECORD is
'�������±�ʶ';

comment on column PT_ETA_ACCESS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_ETA_COMPANY                                        */
/*==============================================================*/
create table PT_ETA_COMPANY  (
   COMPANY_ID           NUMBER(10)                      not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   TEL_TYPE             VARCHAR2(32),
   TELEPHONE            VARCHAR2(500),
   URL_TYPE             VARCHAR2(32),
   URL                  VARCHAR2(500),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTETA_COMPANY foreign key (COMPANY_ID)
         references PT_COMPANY (COMPANY_ID)
);

comment on table PT_ETA_COMPANY is
'������˾�����Ϣ:�绰,��ַ,ͼ���
�绰����ַ���������Է��ʸù�����˾��������Ϣ;ͼ��������﹫����˾��LOGOͼ���·��ͼ.';

comment on column PT_ETA_COMPANY.COMPANY_ID is
'���,����"PT_COMPANY"';

comment on column PT_ETA_COMPANY.TEL_TYPE is
'[172U]ֵ�����:
0 δ����
1 �ܻ�
2 �ճ̰����Ϣ
3 ������Ϣ';

comment on column PT_ETA_COMPANY.TELEPHONE is
'��ʽΪ:"���-����",��010-82306399.
�������ʱ����Ӣ�İ�ǡ�|���ָ�,����绰����һһ��Ӧ';

comment on column PT_ETA_COMPANY.URL_TYPE is
'[172U]ֵ�����:
0 δ����
1 ·���滮
2 �ճ̰����Ϣ
3 ��ͼ����
4 ��ҳ';

comment on column PT_ETA_COMPANY.URL is
'��ʽ:http://xxxxxxxxx,��http://www.navinfo.com/.
�����ַʱ����Ӣ�İ�ǡ�|���ָ�,������ַ����һһ��Ӧ';

comment on column PT_ETA_COMPANY.U_RECORD is
'�������±�ʶ';

comment on column PT_ETA_COMPANY.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_LINE                                               */
/*==============================================================*/
create table PT_LINE  (
   PID                  NUMBER(10)                      not null,
   SYSTEM_ID            NUMBER(10)                     default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   TYPE                 NUMBER(2)                      default 11 not null
       check (TYPE in (11,12,13,14,15,16,17,21,31,32,33,41,42,51,52,53,54,61)),
   COLOR                VARCHAR2(10),
   NIDB_LINEID          VARCHAR2(32),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(16),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_LINE primary key (PID)
);

comment on table PT_LINE is
'��¼ĳ����Ĺ�����·��Ϣ,���� 656·,��4·��';

comment on column PT_LINE.PID is
'����';

comment on column PT_LINE.SYSTEM_ID is
'�ο�"PT_SYSTEM"';

comment on column PT_LINE.CITY_CODE is
'�����������û��ֱ�ӹ�ϵ,�������ά��';

comment on column PT_LINE.COLOR is
'�洢16���Ƶ�RGBֵ';

comment on column PT_LINE.LOG is
'[173sp1]';

comment on column PT_LINE.DATA_SOURCE is
'[170]';

comment on column PT_LINE.UPDATE_BATCH is
'[170]';

comment on column PT_LINE.U_RECORD is
'�������±�ʶ';

comment on column PT_LINE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_ETA_LINE                                           */
/*==============================================================*/
create table PT_ETA_LINE  (
   PID                  NUMBER(10)                      not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   BIKE                 VARCHAR2(1)                    default '0' not null
       check (BIKE in ('0','1','2')),
   BIKE_PERIOD          VARCHAR2(200),
   IMAGE                VARCHAR2(20),
   RACK                 VARCHAR2(1)                    default '0' not null
       check (RACK in ('0','1','2')),
   DINNER               VARCHAR2(1)                    default '0' not null
       check (DINNER in ('0','1','2')),
   TOILET               VARCHAR2(1)                    default '0' not null
       check (TOILET in ('0','1','2')),
   SLEEPER              VARCHAR2(1)                    default '0' not null
       check (SLEEPER in ('0','1','2')),
   WHEEL_CHAIR          VARCHAR2(1)                    default '0' not null
       check (WHEEL_CHAIR in ('0','1','2')),
   SMOKE                VARCHAR2(1)                    default '0' not null
       check (SMOKE in ('0','1','2')),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTETA_LINE foreign key (PID)
         references PT_LINE (PID)
);

comment on table PT_ETA_LINE is
'������ʵ���繫����ͨ��·�о߱��Ĺ���,���������г�,�����,�òͷ����';

comment on column PT_ETA_LINE.PID is
'���,����"PT_LINE"';

comment on column PT_ETA_LINE.BIKE_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column PT_ETA_LINE.U_RECORD is
'�������±�ʶ';

comment on column PT_ETA_LINE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_ETA_STOP                                           */
/*==============================================================*/
create table PT_ETA_STOP  (
   POI_PID              NUMBER(10)                      not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   PRIVATE_PARK         VARCHAR2(1)                    default '0' not null
       check (PRIVATE_PARK in ('0','1','2','3')),
   PRIVATE_PARK_PERIOD  VARCHAR2(200),
   CARPORT_EXACT        VARCHAR2(32),
   CARPORT_ESTIMATE     VARCHAR2(1)                    default '0' not null
       check (CARPORT_ESTIMATE in ('0','1','2','3','4','5')),
   BIKE_PARK            VARCHAR2(1)                    default '0' not null
       check (BIKE_PARK in ('0','1','2','3')),
   BIKE_PARK_PERIOD     VARCHAR2(200),
   MANUAL_TICKET        VARCHAR2(1)                    default '0' not null
       check (MANUAL_TICKET in ('0','1','2')),
   MANUAL_TICKET_PERIOD VARCHAR2(200),
   MOBILE               VARCHAR2(1)                    default '0' not null
       check (MOBILE in ('0','1','2')),
   BAGGAGE_SECURITY     VARCHAR2(1)                    default '0' not null
       check (BAGGAGE_SECURITY in ('0','1','2')),
   LEFT_BAGGAGE         VARCHAR2(1)                    default '0' not null
       check (LEFT_BAGGAGE in ('0','1','2')),
   CONSIGNATION_EXACT   VARCHAR2(32),
   CONSIGNATION_ESTIMATE VARCHAR2(1)                    default '0' not null
       check (CONSIGNATION_ESTIMATE in ('0','1','2','3','4','5')),
   CONVENIENT           VARCHAR2(1)                    default '0' not null
       check (CONVENIENT in ('0','1','2')),
   SMOKE                VARCHAR2(1)                    default '0' not null
       check (SMOKE in ('0','1','2')),
   BUILD_TYPE           VARCHAR2(1)                    default '0' not null
       check (BUILD_TYPE in ('0','1','2','3')),
   AUTO_TICKET          VARCHAR2(1)                    default '0' not null
       check (AUTO_TICKET in ('0','1','2')),
   TOILET               VARCHAR2(1)                    default '0' not null
       check (TOILET in ('0','1','2')),
   WIFI                 VARCHAR2(1)                    default '0' not null
       check (WIFI in ('0','1','2')),
   OPEN_PERIOD          VARCHAR2(200),
   FARE_AREA            VARCHAR2(1),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTETA_POI foreign key (POI_PID)
         references PT_POI (PID)
);

comment on table PT_ETA_STOP is
'[170]������ʵ���繫����ͨվ��߱��Ĺ��ܼ����ܱߵĸ���������ʩ��';

comment on column PT_ETA_STOP.POI_PID is
'���,����"PT_POI"';

comment on column PT_ETA_STOP.PRIVATE_PARK is
'�շѻ����';

comment on column PT_ETA_STOP.PRIVATE_PARK_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column PT_ETA_STOP.BIKE_PARK is
'�Ƿ����˿���';

comment on column PT_ETA_STOP.BIKE_PARK_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-�������˿���,����¼����';

comment on column PT_ETA_STOP.MANUAL_TICKET_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-�������˿���,����¼����';

comment on column PT_ETA_STOP.OPEN_PERIOD is
'��ʽΪ��hh:mm��,ֻ��¼ÿ��ļ���-���㿪��,����¼����';

comment on column PT_ETA_STOP.FARE_AREA is
'�ٷ���·ͼ��ֵ';

comment on column PT_ETA_STOP.U_RECORD is
'�������±�ʶ';

comment on column PT_ETA_STOP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_SYSTEM                                             */
/*==============================================================*/
create table PT_SYSTEM  (
   SYSTEM_ID            NUMBER(10)                      not null,
   COMPANY_ID           NUMBER(10)                      not null,
   NAME                 VARCHAR2(35),
   PHONETIC             VARCHAR2(1000),
   NAME_ENG_SHORT       VARCHAR2(35),
   NAME_ENG_FULL        VARCHAR2(200),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   CITY_CODE            NUMBER(6)                      default 0 not null,
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_SYSTEMID        VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_SYSTEM primary key (SYSTEM_ID),
   constraint PTSYSTEM_COMPANY foreign key (COMPANY_ID)
         references PT_COMPANY (COMPANY_ID)
);

comment on table PT_SYSTEM is
'����ϵͳ��һ��������˾�����������,��Ӫ���幫����·�ķ�֧��λ.ͨ����ָ������ͬһ��ϵͳ�Ĺ�����·�Ĺ��?λ,��ֱ�ӹ���,��Ӫ��ʿ/�������·�ĵ�λ';

comment on column PT_SYSTEM.SYSTEM_ID is
'����,ϵͳ����Ψһ˳���';

comment on column PT_SYSTEM.COMPANY_ID is
'���,����"PT_COMPANY"';

comment on column PT_SYSTEM.PHONETIC is
'[171U]';

comment on column PT_SYSTEM.SRC_FLAG is
'[170]';

comment on column PT_SYSTEM.CITY_CODE is
'�洢����Ϊ4λ';

comment on column PT_SYSTEM.DATA_SOURCE is
'[170]';

comment on column PT_SYSTEM.UPDATE_BATCH is
'[170]';

comment on column PT_SYSTEM.U_RECORD is
'�������±�ʶ';

comment on column PT_SYSTEM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_ETA_SYSTEM                                         */
/*==============================================================*/
create table PT_ETA_SYSTEM  (
   SYSTEM_ID            NUMBER(10)                      not null,
   ALIAS_NAME           VARCHAR2(35),
   ALIAS_PINYIN         VARCHAR2(1000),
   TEL_TYPE             VARCHAR2(32),
   TELEPHONE            VARCHAR2(500),
   URL_TYPE             VARCHAR2(32),
   URL                  VARCHAR2(500),
   BILL_METHOD          VARCHAR2(1)                    default '1' not null
       check (BILL_METHOD in ('0','1','2','3')),
   COIN                 VARCHAR2(200)                  default 'CNY' not null,
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTETA_SYSTEM foreign key (SYSTEM_ID)
         references PT_SYSTEM (SYSTEM_ID)
);

comment on table PT_ETA_SYSTEM is
'��Ҫ�е绰,��ַ,ͼ��,�Ʒѷ�ʽ,���û��ҵ���Ϣ';

comment on column PT_ETA_SYSTEM.SYSTEM_ID is
'���,����"PT_SYSTEM"';

comment on column PT_ETA_SYSTEM.TEL_TYPE is
'[172U]ֵ�����:
0 δ����
1 �ܻ�
2 �ճ̰����Ϣ
3 ������Ϣ';

comment on column PT_ETA_SYSTEM.TELEPHONE is
'��ʽΪ:"���-����",��010-82306399.
�������ʱ����Ӣ�İ�ǡ�|���ָ�,����绰����һһ��Ӧ';

comment on column PT_ETA_SYSTEM.URL_TYPE is
'[172U]ֵ�����:
0 δ����
1 ·���滮
2 �ճ̰����Ϣ
3 ��ͼ����
4 ��ҳ';

comment on column PT_ETA_SYSTEM.URL is
'��ʽ:http://xxxxxxxxx,��http://www.navinfo.com/.
�����ַʱ����Ӣ�İ�ǡ�|���ָ�,������ַ����һһ��Ӧ';

comment on column PT_ETA_SYSTEM.COIN is
'ֵ��Ϊ:
CNY �����(��½)
HKD ��Ԫ(���)
MOP ���ű�
���ֻ��ҷ�ʽ��Ӣ�İ�ǡ�|���ָ�';

comment on column PT_ETA_SYSTEM.U_RECORD is
'�������±�ʶ';

comment on column PT_ETA_SYSTEM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_LINE_NAME                                          */
/*==============================================================*/
create table PT_LINE_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_LINE_NAME primary key (NAME_ID),
   constraint PTLINE_NAME foreign key (PID)
         references PT_LINE (PID)
);

comment on table PT_LINE_NAME is
'[170]';

comment on column PT_LINE_NAME.NAME_ID is
'����';

comment on column PT_LINE_NAME.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column PT_LINE_NAME.PID is
'���,����"PT_LINE"';

comment on column PT_LINE_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column PT_LINE_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column PT_LINE_NAME.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column PT_LINE_NAME.U_RECORD is
'�������±�ʶ';

comment on column PT_LINE_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_PLATFORM                                           */
/*==============================================================*/
create table PT_PLATFORM  (
   PID                  NUMBER(10)                      not null,
   POI_PID              NUMBER(10)                      not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   COLLECT              NUMBER(2)                      default 0 not null
       check (COLLECT in (0,1)),
   P_LEVEL              NUMBER(2)                      default 0 not null
       check (P_LEVEL in (4,3,2,1,0,-1,-2,-3,-4,-5,-6)),
   TRANSIT_FLAG         NUMBER(1)                      default 0 not null
       check (TRANSIT_FLAG in (0,1)),
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   NIDB_PLATFORMID      VARCHAR2(32),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_PLATFORM primary key (PID),
   constraint PTPLATFORM_POI foreign key (POI_PID)
         references PT_POI (PID)
);

comment on table PT_PLATFORM is
'վ̨,Ҳ����̨,��������ͨ����ͣ��ʱ,���˿ͺ򳵺����³�����ʩ';

comment on column PT_PLATFORM.PID is
'����';

comment on column PT_PLATFORM.POI_PID is
'���,����"PT_POI"';

comment on column PT_PLATFORM.COLLECT is
'[170]';

comment on column PT_PLATFORM.P_LEVEL is
'ֵ��:-6~4;0��ʾ����';

comment on column PT_PLATFORM.TRANSIT_FLAG is
'��¼��վ̨�Ƿ��ܺ�����վ̨��ͨ,���ܺ�����վ̨��ͨ,��ʶΪ"�ɻ���";������ͨ���κ�����վ̨,��ʶΪ"���ɻ���"';

comment on column PT_PLATFORM.DATA_SOURCE is
'[170]';

comment on column PT_PLATFORM.UPDATE_BATCH is
'[170]';

comment on column PT_PLATFORM.TASK_ID is
'[170]��¼��ҵ��������';

comment on column PT_PLATFORM.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column PT_PLATFORM.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column PT_PLATFORM.U_RECORD is
'�������±�ʶ';

comment on column PT_PLATFORM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_PLATFORM_ACCESS                                    */
/*==============================================================*/
create table PT_PLATFORM_ACCESS  (
   RELATE_ID            NUMBER(10)                      not null,
   PLATFORM_ID          NUMBER(10)                      not null,
   ACCESS_ID            NUMBER(10)                      not null,
   AVAILABLE            NUMBER(1)                      default 1 not null
       check (AVAILABLE in (0,1)),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_PLATFORM_ACCESS primary key (RELATE_ID),
   constraint PTPLATFORM_ACCESSES foreign key (PLATFORM_ID)
         references PT_PLATFORM (PID),
   constraint PTPLATFORM_ACCESS foreign key (ACCESS_ID)
         references PT_POI (PID)
);

comment on table PT_PLATFORM_ACCESS is
'��¼վ̨���ܵ����վ̨����ڵ�֮��Ķ�Ӧ��ϵ,һ��վ̨�����ж����ڵ�';

comment on column PT_PLATFORM_ACCESS.RELATE_ID is
'����';

comment on column PT_PLATFORM_ACCESS.PLATFORM_ID is
'���,����"PT_PLATFORM"';

comment on column PT_PLATFORM_ACCESS.ACCESS_ID is
'���,����"PT_POI"';

comment on column PT_PLATFORM_ACCESS.U_RECORD is
'�������±�ʶ';

comment on column PT_PLATFORM_ACCESS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_PLATFORM_NAME                                      */
/*==============================================================*/
create table PT_PLATFORM_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_PLATFORM_NAME primary key (NAME_ID),
   constraint PTPLATFORM_NAME foreign key (PID)
         references PT_PLATFORM (PID)
);

comment on table PT_PLATFORM_NAME is
'[170]';

comment on column PT_PLATFORM_NAME.NAME_ID is
'����';

comment on column PT_PLATFORM_NAME.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column PT_PLATFORM_NAME.PID is
'���,����"PT_PLATFORM"';

comment on column PT_PLATFORM_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column PT_PLATFORM_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column PT_PLATFORM_NAME.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column PT_PLATFORM_NAME.U_RECORD is
'�������±�ʶ';

comment on column PT_PLATFORM_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_POI_PARENT                                         */
/*==============================================================*/
create table PT_POI_PARENT  (
   GROUP_ID             NUMBER(10)                      not null,
   PARENT_POI_PID       NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_POI_PARENT primary key (GROUP_ID),
   constraint PTPOI_PARENT foreign key (PARENT_POI_PID)
         references PT_POI (PID)
);

comment on table PT_POI_PARENT is
'��¼վ��������֮��Ĺ�ϵ��Ϣ';

comment on column PT_POI_PARENT.GROUP_ID is
'����';

comment on column PT_POI_PARENT.PARENT_POI_PID is
'���,����"PT_POI"';

comment on column PT_POI_PARENT.U_RECORD is
'�������±�ʶ';

comment on column PT_POI_PARENT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_POI_CHILDREN                                       */
/*==============================================================*/
create table PT_POI_CHILDREN  (
   GROUP_ID             NUMBER(10)                      not null,
   CHILD_POI_PID        NUMBER(10)                      not null,
   RELATION_TYPE        NUMBER(1)                      default 0 not null
       check (RELATION_TYPE in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTPOI_CHILDREN foreign key (CHILD_POI_PID)
         references PT_POI (PID),
   constraint PTPOI_CHILDPARENT foreign key (GROUP_ID)
         references PT_POI_PARENT (GROUP_ID)
);

comment on table PT_POI_CHILDREN is
'����POI�ĸ��ӹ�ϵ,��վ��������֮��Ĺ�ϵ��Ϣ';

comment on column PT_POI_CHILDREN.GROUP_ID is
'���,����"PT_POI_PARENT"';

comment on column PT_POI_CHILDREN.CHILD_POI_PID is
'���,����"PT_POI"';

comment on column PT_POI_CHILDREN.U_RECORD is
'�������±�ʶ';

comment on column PT_POI_CHILDREN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_POI_FLAG                                           */
/*==============================================================*/
create table PT_POI_FLAG  (
   POI_PID              NUMBER(10)                      not null,
   FLAG_CODE            VARCHAR2(12),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTPOI_FLAG foreign key (POI_PID)
         references PT_POI (PID)
);

comment on table PT_POI_FLAG is
'[170]';

comment on column PT_POI_FLAG.POI_PID is
'���,����"PT_POI"';

comment on column PT_POI_FLAG.FLAG_CODE is
'�ο�"M_FLAG_CODE"';

comment on column PT_POI_FLAG.U_RECORD is
'�������±�ʶ';

comment on column PT_POI_FLAG.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_POI_NAME                                           */
/*==============================================================*/
create table PT_POI_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   POI_PID              NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2)),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   NIDB_PID             VARCHAR2(32),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_POI_NAME primary key (NAME_ID),
   constraint PTPOI_NAME foreign key (POI_PID)
         references PT_POI (PID)
);

comment on table PT_POI_NAME is
'��IX_POI_NAMEԭ����ͬ';

comment on column PT_POI_NAME.NAME_ID is
'[170]����';

comment on column PT_POI_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column PT_POI_NAME.POI_PID is
'���,����"PT_POI"';

comment on column PT_POI_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column PT_POI_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column PT_POI_NAME.NIDB_PID is
'��¼����POI���Ѿ���Ʒ������ID,��ͬ��������PID��ͬ';

comment on column PT_POI_NAME.U_RECORD is
'�������±�ʶ';

comment on column PT_POI_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_POI_NAME_TONE                                      */
/*==============================================================*/
create table PT_POI_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTPOINAME_TONE foreign key (NAME_ID)
         references PT_POI_NAME (NAME_ID)
);

comment on table PT_POI_NAME_TONE is
'[170]';

comment on column PT_POI_NAME_TONE.NAME_ID is
'���,����"PT_POI_NAME"';

comment on column PT_POI_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column PT_POI_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column PT_POI_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column PT_POI_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column PT_POI_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column PT_POI_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column PT_POI_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_STRAND                                             */
/*==============================================================*/
create table PT_STRAND  (
   PID                  NUMBER(10)                      not null,
   PAIR_STRAND_PID      NUMBER(10)                     default 0 not null,
   LINE_ID              NUMBER(10)                      not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   UP_DOWN              VARCHAR2(16)                   
       check (UP_DOWN is null or (UP_DOWN in ('��','��','��','�Σ�','�ף�','�ã�','�ãΣ�','�ãף�'))),
   DISTANCE             VARCHAR2(10),
   TICKET_SYS           NUMBER(2)                      default 0 not null
       check (TICKET_SYS in (0,1,2,9)),
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
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   TASK_ID              NUMBER(10)                     default 0 not null,
   DATA_VERSION         VARCHAR2(128),
   FIELD_TASK_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_STRAND primary key (PID),
   constraint PTSTRAND_LINE foreign key (LINE_ID)
         references PT_LINE (PID)
);

comment on table PT_STRAND is
'Strand,����α�,������¼ÿ����·�����г������ڲ�ͬ��ʱ��㷢���İ��,���ð�εĸ�����ϸ��Ϣ,�羭����·,��ĩ��ʱ��,���������';

comment on column PT_STRAND.PID is
'����';

comment on column PT_STRAND.PAIR_STRAND_PID is
'[170]';

comment on column PT_STRAND.LINE_ID is
'���,����"PT_LINE"';

comment on column PT_STRAND.UP_DOWN is
'��ʾ����,����,���е�,�洢Ϊȫ���ַ�';

comment on column PT_STRAND.TICKET_START is
'[170]';

comment on column PT_STRAND.TOTAL_PRICE is
'[170]';

comment on column PT_STRAND.INCREASED_PRICE is
'[170]';

comment on column PT_STRAND.INCREASED_STEP is
'[170]';

comment on column PT_STRAND.GEOMETRY is
'(1)Strand �г��켣,�������������,��ͼ���߲������,������п����ཻ
(2)�洢��"��"Ϊ��λ�ľ�γ���������';

comment on column PT_STRAND.DATA_SOURCE is
'[170]';

comment on column PT_STRAND.UPDATE_BATCH is
'[170]';

comment on column PT_STRAND.LOG is
'[173sp1]';

comment on column PT_STRAND.TASK_ID is
'[170]��¼��ҵ��������';

comment on column PT_STRAND.DATA_VERSION is
'��¼��ݲɼ�����ҵ��,��10��,11��';

comment on column PT_STRAND.FIELD_TASK_ID is
'��¼��ҵ��������';

comment on column PT_STRAND.U_RECORD is
'�������±�ʶ';

comment on column PT_STRAND.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_RUNTIME                                            */
/*==============================================================*/
create table PT_RUNTIME  (
   STRAND_PID           NUMBER(10)                      not null,
   PLATFORM_PID         NUMBER(10)                      not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   ARRIVAL_TIME         VARCHAR2(32),
   DEPART_TIME          VARCHAR2(32),
   APPROX_TIME          NUMBER(1)                      default 0 not null
       check (APPROX_TIME in (0,1)),
   VALID_WEEK           VARCHAR2(7),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTRUNTIME_STRAND foreign key (STRAND_PID)
         references PT_STRAND (PID),
   constraint PTRUNTIME_PLATFORM foreign key (PLATFORM_PID)
         references PT_PLATFORM (PID)
);

comment on table PT_RUNTIME is
'(1)��¼��ͬ��εĳ����͵���ʱ��,��ĳ����·Strand������뿪ָ��վ̨��ʱ��
(2)NaviMap������,������NavEx��Ʒ֮ǰͨ��"Strand����ʱ�̱�"��"Stand��վ̨��ϵ��"�������';

comment on column PT_RUNTIME.STRAND_PID is
'���,����"PT_STRAND"';

comment on column PT_RUNTIME.PLATFORM_PID is
'���,����"PT_PLATFORM"';

comment on column PT_RUNTIME.ARRIVAL_TIME is
'��ʽΪ24Сʱ��,��ʽΪ"ʱ:��",����06:05.
�����ε�ʱ�����ʱ,ֵ������24,�����賿1:30Ҫ���Ϊ25:30';

comment on column PT_RUNTIME.DEPART_TIME is
'��ʽͬ"����ʱ��",��ֵĬ��Ϊ"����ʱ����ʱһ����"';

comment on column PT_RUNTIME.VALID_WEEK is
'����Ϊ��λ,�����ҹ�7λ��ʾ���յ�����,1��ʾ��Ч,0��ʾ��Ч.������������ڶ���Ч:1010000,һֱ��ЧΪ1111111';

comment on column PT_RUNTIME.U_RECORD is
'�������±�ʶ';

comment on column PT_RUNTIME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_STRAND_NAME                                        */
/*==============================================================*/
create table PT_STRAND_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2,3,4)),
   NAME                 VARCHAR2(200),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_STRAND_NAME primary key (NAME_ID),
   constraint PTSTRAND_NAME foreign key (PID)
         references PT_STRAND (PID)
);

comment on table PT_STRAND_NAME is
'[170]';

comment on column PT_STRAND_NAME.NAME_ID is
'����';

comment on column PT_STRAND_NAME.NAME_GROUPID is
'[171U]��1��ʼ�������';

comment on column PT_STRAND_NAME.PID is
'���,����"PT_STRAND"';

comment on column PT_STRAND_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column PT_STRAND_NAME.NAME_CLASS is
'[170][172U]';

comment on column PT_STRAND_NAME.NAME is
'[170]';

comment on column PT_STRAND_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column PT_STRAND_NAME.SRC_FLAG is
'�ֽ�ָӢ������Դ';

comment on column PT_STRAND_NAME.U_RECORD is
'�������±�ʶ';

comment on column PT_STRAND_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_STRAND_PLATFORM                                    */
/*==============================================================*/
create table PT_STRAND_PLATFORM  (
   STRAND_PID           NUMBER(10)                      not null,
   PLATFORM_PID         NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(10)                     default 0 not null,
   INTERVAL             NUMBER(3)                      default 0 not null,
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(255),
   EDITIONFLAG          VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   DATA_SOURCE          VARCHAR2(100),
   UPDATE_BATCH         VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTSTRAND_PLATFORM foreign key (PLATFORM_PID)
         references PT_PLATFORM (PID),
   constraint PTSTRAND_FLATFORM foreign key (STRAND_PID)
         references PT_STRAND (PID)
);

comment on column PT_STRAND_PLATFORM.STRAND_PID is
'���,����"PT_STRAND"';

comment on column PT_STRAND_PLATFORM.PLATFORM_PID is
'���,����"PT_PLATFORM"';

comment on column PT_STRAND_PLATFORM.SEQ_NUM is
'(1)��¼������·ĳ��Strand���ߵ�վ̨��Ϣ
(2)Ŀǰ������·��վ̨ͳһ��10000��ʼÿ�ε���10000���,��10000,20000,30000��';

comment on column PT_STRAND_PLATFORM.INTERVAL is
'��λ:����';

comment on column PT_STRAND_PLATFORM.DATA_SOURCE is
'[170]';

comment on column PT_STRAND_PLATFORM.UPDATE_BATCH is
'[170]';

comment on column PT_STRAND_PLATFORM.U_RECORD is
'�������±�ʶ';

comment on column PT_STRAND_PLATFORM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_STRAND_SCHEDULE                                    */
/*==============================================================*/
create table PT_STRAND_SCHEDULE  (
   STRAND_PID           NUMBER(10)                      not null,
   VALID_DAY            NUMBER(5)                      default 0 not null,
   START_TIME           VARCHAR2(32),
   END_TIME             VARCHAR2(32),
   INTERVAL             VARCHAR2(50),
   CITY_CODE            NUMBER(6)                      default 0 not null,
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PTSTRAND_SCHEDULE foreign key (STRAND_PID)
         references PT_STRAND (PID)
);

comment on table PT_STRAND_SCHEDULE is
'��¼ĳ��·strand��������Ϣ: ��������ֹʱ��ͼ��ʱ���,�����۵���,���ڷ�ʱ��εķ������,����һ������,ĳ����·9:00��12:00,ÿ��15���ӷ�һ��,12:00��18:00ÿ��20���ӷ�һ��';

comment on column PT_STRAND_SCHEDULE.STRAND_PID is
'���,����"PT_STRAND"';

comment on column PT_STRAND_SCHEDULE.VALID_DAY is
'����16bit ��ʾ,���ҵ�������Ϊ0~15bit,ÿbit ��ʾһ��ʱ������(����),��ֵΪ0/1 �ֱ��ʾ��Ч/��Ч,��:0000001111000010,��ʾ�ļ�����һ��������Ч
��0bit:����
��1bit:�ļ�
��2bit:�ڼ���
��3bit:����
��4bit:����
��5bit:����
��6bit:����
��7bit:����
��8bit:�ܶ�
��9bit:��һ
��10~15bit ��Ϊ0
����0~9bit λ��Ϊ1,��ʾȫ����Ч';

comment on column PT_STRAND_SCHEDULE.START_TIME is
'��¼ÿ����·�����г�����ķ�����ʼʱ��;��ʽΪ24Сʱ��,��ð�ŷָ�,"xx:xx"������ֵ�ֱ��¼"Сʱ:����",ÿ��strand���ܴ��ڶ��鷢����ʼ�ͽ���ʱ��,����ֽڼ���,��ĩ';

comment on column PT_STRAND_SCHEDULE.END_TIME is
'��¼ÿ����·�����г�����ķ�������ʱ��,��ʽͬ"������ʼʱ��"';

comment on column PT_STRAND_SCHEDULE.INTERVAL is
'��¼ÿ��strand����η����ļ��ʱ��,�Է���Ϊ��λ��¼,��С��λ��ȷ��0.5��,ÿ��Strandֻ��¼һ���������,����ֽڼ���,��ĩ,Ҳ����ָ߷�ʱ���';

comment on column PT_STRAND_SCHEDULE.U_RECORD is
'�������±�ʶ';

comment on column PT_STRAND_SCHEDULE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: PT_TRANSFER                                           */
/*==============================================================*/
create table PT_TRANSFER  (
   TRANSFER_ID          NUMBER(10)                      not null,
   TRANSFER_TYPE        NUMBER(1)                      default 1 not null
       check (TRANSFER_TYPE in (0,1)),
   POI_FIR              NUMBER(10)                     default 0 not null,
   POI_SEC              NUMBER(10)                     default 0 not null,
   PLATFORM_FIR         NUMBER(10)                     default 0 not null,
   PLATFORM_SEC         NUMBER(10)                     default 0 not null,
   CITY_CODE            NUMBER(6)                      default 0 not null,
   TRANSFER_TIME        NUMBER(2)                      default 0 not null,
   EXTERNAL_FLAG        NUMBER(1)                      default 0 not null
       check (EXTERNAL_FLAG in (0,1,2)),
   OPERATOR             VARCHAR2(32),
   UPDATE_TIME          VARCHAR2(200),
   LOG                  VARCHAR2(200),
   EDITION_FLAG         VARCHAR2(12),
   STATE                NUMBER(1)                      default 0 not null
       check (STATE in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_PT_TRANSFER primary key (TRANSFER_ID)
);

comment on table PT_TRANSFER is
'NaviMap����ʱ,�����·ͼ�еĻ���վ��������������������(��վ��),��Ҫ����Ӧ������֮������������������Ϊ"��վ����"�Ĺ�ϵ.
�����·ͼ�еĻ���վ��������һ������,��Ҫ�ڴ������������ܹ���ͨ��վ̨֮������������������Ϊ"վ�ڻ���"�Ĺ�ϵ.
��:�����վ�ڻ���,���˵�Ӧ����վ̨���;�����վ�任��,���˵�Ӧ��������(��վ��)';

comment on column PT_TRANSFER.TRANSFER_ID is
'����';

comment on column PT_TRANSFER.TRANSFER_TYPE is
'��ֿ�վ���˺�վ�ڻ�����������:
(1)��վ����,��ﲻͬ������·����������վ��֮��Ļ���;��ʱ,���˵�һ�ͻ��˵���ֱ��ʾվ��
(2)վ�ڻ���,��ﲻͬ������·��ͬһ��վ���ڲ��Ļ���,��ʱ,���˵�һ�ͻ��˵���ֱ��ʾվ̨';

comment on column PT_TRANSFER.POI_FIR is
'[173sp1]�ο�"PT_POI"';

comment on column PT_TRANSFER.POI_SEC is
'[173sp1]�ο�"PT_POI"';

comment on column PT_TRANSFER.PLATFORM_FIR is
'[173sp1]�ο�"PT_PLATFORM"';

comment on column PT_TRANSFER.PLATFORM_SEC is
'[173sp1]�ο�"PT_PLATFORM"';

comment on column PT_TRANSFER.TRANSFER_TIME is
'�Է���Ϊ��λ,��¼�˿ͻ���ʱ������Ҫ��ʱ��';

comment on column PT_TRANSFER.EXTERNAL_FLAG is
'[180U]ÿһ���վ���˹�ϵ,����Ҫ����"�ⲿ��ʶ"����,���������˿ͻ���ʱ�Ƿ���Ҫ�ߵ�վ���ⲿ.����������֮����ר�û���ͨ��ʱ,"�ⲿ��ʶ"����Ϊ"��";��û��ר��ͨ��,�˿���Ҫ�ߵ�վ�����滻��,"�ⲿ��ʶ"����Ϊ"��".';

comment on column PT_TRANSFER.U_RECORD is
'�������±�ʶ';

comment on column PT_TRANSFER.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: QC_QUESTION                                           */
/*==============================================================*/
create table QC_QUESTION  (
   QU_ID                NUMBER(10)                      not null,
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
   MESH_ID              NUMBER(6)                      default 0,
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

comment on column QC_QUESTION.QU_ID is
'����';

/*==============================================================*/
/* Table: RD_BRANCH                                             */
/*==============================================================*/
create table RD_BRANCH  (
   BRANCH_PID           NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   RELATIONSHIP_TYPE    NUMBER(1)                      default 1 not null
       check (RELATIONSHIP_TYPE in (1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_BRANCH primary key (BRANCH_PID),
   constraint RDBRANCH_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDBRANCH_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDBRANCH_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_BRANCH.BRANCH_PID is
'����';

comment on column RD_BRANCH.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_BRANCH.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_BRANCH.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_BRANCH.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_BRANCH_DETAIL                                      */
/*==============================================================*/
create table RD_BRANCH_DETAIL  (
   DETAIL_ID            NUMBER(10)                      not null,
   BRANCH_PID           NUMBER(10)                      not null,
   BRANCH_TYPE          NUMBER(1)                      default 0 not null
       check (BRANCH_TYPE in (0,1,2,3,4)),
   VOICE_DIR            NUMBER(1)                      default 0 not null
       check (VOICE_DIR in (0,2,5,9)),
   ESTAB_TYPE           NUMBER(1)                      default 0 not null
       check (ESTAB_TYPE in (0,1,2,3,4,5,9)),
   NAME_KIND            NUMBER(1)                      default 0 not null
       check (NAME_KIND in (0,1,2,3,4,5,6,7,8,9)),
   EXIT_NUM             VARCHAR2(32),
   ARROW_CODE           VARCHAR2(10),
   PATTERN_CODE         VARCHAR2(10),
   ARROW_FLAG           NUMBER(2)                      default 0 not null
       check (ARROW_FLAG in (0,1)),
   GUIDE_CODE           NUMBER(1)                      default 0 not null
       check (GUIDE_CODE in (0,1,2,3,9)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_BRANCH_DETAIL primary key (DETAIL_ID),
   constraint RDBRANCH_DETAIL foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID)
);

comment on column RD_BRANCH_DETAIL.DETAIL_ID is
'����';

comment on column RD_BRANCH_DETAIL.BRANCH_PID is
'���,����"RD_BRANCH"';

comment on column RD_BRANCH_DETAIL.BRANCH_TYPE is
'[180U]';

comment on column RD_BRANCH_DETAIL.VOICE_DIR is
'��,��,��';

comment on column RD_BRANCH_DETAIL.ESTAB_TYPE is
'����,���,SA,PA,JCT��';

comment on column RD_BRANCH_DETAIL.NAME_KIND is
'IC,SA,PA,JCT,����,��ڵ�';

comment on column RD_BRANCH_DETAIL.ARROW_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME",��:0a24030a';

comment on column RD_BRANCH_DETAIL.PATTERN_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME",��:8a430211';

comment on column RD_BRANCH_DETAIL.ARROW_FLAG is
'[171A]';

comment on column RD_BRANCH_DETAIL.GUIDE_CODE is
'�߼���,Underpath�򵼵�';

comment on column RD_BRANCH_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_BRANCH_NAME                                        */
/*==============================================================*/
create table RD_BRANCH_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   DETAIL_ID            NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   NAME_CLASS           NUMBER(1)                      default 0 not null
       check (NAME_CLASS in (0,1)),
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   CODE_TYPE            NUMBER(2)                      default 0 not null
       check (CODE_TYPE in (0,1,2,3,4,5,6,7,8,9,10)),
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4,5)),
   VOICE_FILE           VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_BRANCH_NAME primary key (NAME_ID),
   constraint RDBRANCH_NAME foreign key (DETAIL_ID)
         references RD_BRANCH_DETAIL (DETAIL_ID)
);

comment on column RD_BRANCH_NAME.NAME_ID is
'[170]����';

comment on column RD_BRANCH_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_BRANCH_NAME.DETAIL_ID is
'���,����"RD_BRANCH_DETAIL"';

comment on column RD_BRANCH_NAME.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_BRANCH_NAME.NAME_CLASS is
'����,����';

comment on column RD_BRANCH_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_BRANCH_NAME.CODE_TYPE is
'��ͨ��·��,��ʩ��,���ٵ�·���';

comment on column RD_BRANCH_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_BRANCH_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

comment on column RD_BRANCH_NAME.VOICE_FILE is
'[170]�ο�"AU_MULTIMEDIA"��"NAME"';

comment on column RD_BRANCH_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_BRANCH_NAME_TONE                                   */
/*==============================================================*/
create table RD_BRANCH_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDBRANCHNAME_TONE foreign key (NAME_ID)
         references RD_BRANCH_NAME (NAME_ID)
);

comment on table RD_BRANCH_NAME_TONE is
'[170]';

comment on column RD_BRANCH_NAME_TONE.NAME_ID is
'[170]���,����"RD_BRANCH_NAME"';

comment on column RD_BRANCH_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column RD_BRANCH_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column RD_BRANCH_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column RD_BRANCH_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column RD_BRANCH_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column RD_BRANCH_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_BRANCH_REALIMAGE                                   */
/*==============================================================*/
create table RD_BRANCH_REALIMAGE  (
   BRANCH_PID           NUMBER(10)                      not null,
   IMAGE_TYPE           NUMBER(1)                      default 0 not null
       check (IMAGE_TYPE in (0,1)),
   REAL_CODE            VARCHAR2(10),
   ARROW_CODE           VARCHAR2(10),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDBRANCH_REALIMAGE foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID)
);

comment on column RD_BRANCH_REALIMAGE.BRANCH_PID is
'���,"RD_BRANCH"';

comment on column RD_BRANCH_REALIMAGE.REAL_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME"
(1)���ٳ����ʵ��ͼ��HEG ʵ��ͼ������ͬ,����8 λ����,��6102500a
(2) ��ͨ��··��ʵ��ͼ����9 λ����, ��140230281;';

comment on column RD_BRANCH_REALIMAGE.ARROW_CODE is
'���һλ���벻ͬ��,������ʵ��ͼ������ͬ';

comment on column RD_BRANCH_REALIMAGE.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH_REALIMAGE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_BRANCH_SCHEMATIC                                   */
/*==============================================================*/
create table RD_BRANCH_SCHEMATIC  (
   SCHEMATIC_ID         NUMBER(10)                      not null,
   BRANCH_PID           NUMBER(10)                      not null,
   SCHEMATIC_CODE       VARCHAR2(16),
   ARROW_CODE           VARCHAR2(16),
   MEMO                 VARCHAR2(500),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_BRANCH_SCHEMATIC primary key (SCHEMATIC_ID),
   constraint RDBRANCH_SCHEMATIC foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID)
);

comment on column RD_BRANCH_SCHEMATIC.SCHEMATIC_ID is
'����';

comment on column RD_BRANCH_SCHEMATIC.BRANCH_PID is
'���,����"RD_BRANCH"';

comment on column RD_BRANCH_SCHEMATIC.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH_SCHEMATIC.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_BRANCH_VIA                                         */
/*==============================================================*/
create table RD_BRANCH_VIA  (
   BRANCH_PID           NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDBRANCH_VIALINKS foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID),
   constraint RDBRANCH_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_BRANCH_VIA is
'(1)���ͬһ����Link���˳�Link֮��Ķ��龭��Link,���Ҿ���Link����������Link���˳�Link
(2)NaviMap��ҵ��,��������˳�Linkֱ����ͬһ·�ڹҽ�ʱ,����������Link;����(�����߹�ϵ),��Ҫ��������Link';

comment on column RD_BRANCH_VIA.BRANCH_PID is
'���,����"RD_BRANCH"';

comment on column RD_BRANCH_VIA.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_BRANCH_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_BRANCH_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_BRANCH_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_BRANCH_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CHAIN                                              */
/*==============================================================*/
create table RD_CHAIN  (
   PID                  NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5,6,7,8)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_CHAIN primary key (PID)
);

comment on column RD_CHAIN.PID is
'����';

comment on column RD_CHAIN.TYPE is
'ͬ��·��,����,JCT��
';

comment on column RD_CHAIN.U_RECORD is
'�������±�ʶ';

comment on column RD_CHAIN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CHAIN_LINK                                         */
/*==============================================================*/
create table RD_CHAIN_LINK  (
   PID                  NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDCHAIN_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDCHAIN_LINKS foreign key (PID)
         references RD_CHAIN (PID)
);

comment on column RD_CHAIN_LINK.PID is
'���,����"RD_CHAIN"';

comment on column RD_CHAIN_LINK.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_CHAIN_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_CHAIN_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_CHAIN_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CHAIN_NAME                                         */
/*==============================================================*/
create table RD_CHAIN_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_CHAIN_NAME primary key (NAME_ID),
   constraint RDCHAIN_NAME foreign key (PID)
         references RD_CHAIN (PID)
);

comment on column RD_CHAIN_NAME.NAME_ID is
'[170]����';

comment on column RD_CHAIN_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_CHAIN_NAME.PID is
'���,����"RD_CHAIN"';

comment on column RD_CHAIN_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_CHAIN_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_CHAIN_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_CHAIN_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSS                                              */
/*==============================================================*/
create table RD_CROSS  (
   PID                  NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   SIGNAL               NUMBER(1)                      default 0 not null
       check (SIGNAL in (0,1,2)),
   ELECTROEYE           NUMBER(1)                      default 0 not null
       check (ELECTROEYE in (0,1,2)),
   KG_FLAG              NUMBER(1)                      default 0 not null
       check (KG_FLAG in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_CROSS primary key (PID)
);

comment on column RD_CROSS.PID is
'����';

comment on column RD_CROSS.TYPE is
'��·��,����·��';

comment on column RD_CROSS.SIGNAL is
'���޺��̵�,·�ں��̵ƻ����˺��̵�';

comment on column RD_CROSS.ELECTROEYE is
'�Ƿ���е�����';

comment on column RD_CROSS.KG_FLAG is
'���·����Kר��,Gר��,KG���õı�־';

comment on column RD_CROSS.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSSWALK                                          */
/*==============================================================*/
create table RD_CROSSWALK  (
   PID                  NUMBER(10)                      not null,
   CURB_RAMP            NUMBER(1)                      default 0 not null
       check (CURB_RAMP in (0,1,2,3)),
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_CROSSWALK primary key (PID)
);

comment on column RD_CROSSWALK.PID is
'����';

comment on column RD_CROSSWALK.CURB_RAMP is
'��,�޻�Ӧ��';

comment on column RD_CROSSWALK.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_CROSSWALK.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSSWALK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSSWALK_INFO                                     */
/*==============================================================*/
create table RD_CROSSWALK_INFO  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5,6)),
   ATTR                 NUMBER(5)                      default 0 not null,
   SIGNAGE              NUMBER(1)                      default 0 not null
       check (SIGNAGE in (0,1,2,3,4)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDNODE_RDCROSSWALKINFO foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDCROSSWALK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDCROSSWALK foreign key (PID)
         references RD_CROSSWALK (PID)
);

comment on column RD_CROSSWALK_INFO.PID is
'���,����"RD_CROSSWALK"';

comment on column RD_CROSSWALK_INFO.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_CROSSWALK_INFO.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_CROSSWALK_INFO.TYPE is
'���к��(�����߻�ƽ����),����ͨ��,������ŵ�';

comment on column RD_CROSSWALK_INFO.ATTR is
'����8bit ��ʾ,���ҵ�������Ϊ0~7bit,ÿbit ��ʾһ�ַ�ʽ����(����),��ֵΪ0/1 �ֱ��ʾ��/��,��:00000011 ��ʾб�ºͽ���;00000101 ��ʾб�ºͷ���
��0bit:б��
��1bit:����
��2bit:����
��3bit:ֱ��
��4bit:����
��5bit:����
�������bit λ��Ϊ0,��ʾδ����';

comment on column RD_CROSSWALK_INFO.SIGNAGE is
'��¼��Node��Link�������Ҳ����޹��������Ϣ';

comment on column RD_CROSSWALK_INFO.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSSWALK_INFO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSSWALK_NODE                                     */
/*==============================================================*/
create table RD_CROSSWALK_NODE  (
   PID                  NUMBER(10)                      not null,
   FIR_NODE_PID         NUMBER(10)                      not null,
   SEN_NODE_PID         NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDCROSSWALK_NODE foreign key (PID)
         references RD_CROSSWALK (PID),
   constraint RDCROSSWALK_FIRNODE foreign key (FIR_NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDCROSSWALK_SENNODE foreign key (SEN_NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_CROSSWALK_NODE.PID is
'���,����"RD_CROSSWALK"';

comment on column RD_CROSSWALK_NODE.FIR_NODE_PID is
'���,����"RD_NODE"';

comment on column RD_CROSSWALK_NODE.SEN_NODE_PID is
'���,����"RD_NODE"';

comment on column RD_CROSSWALK_NODE.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSSWALK_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSS_LINK                                         */
/*==============================================================*/
create table RD_CROSS_LINK  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDCROSS_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDCROSS_LINKS foreign key (PID)
         references RD_CROSS (PID)
);

comment on table RD_CROSS_LINK is
'���������Link';

comment on column RD_CROSS_LINK.PID is
'���,����"RD_CROSS"';

comment on column RD_CROSS_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_CROSS_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSS_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSS_NAME                                         */
/*==============================================================*/
create table RD_CROSS_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_CROSS_NAME primary key (NAME_ID),
   constraint RDCROSS_NAME foreign key (PID)
         references RD_CROSS (PID)
);

comment on column RD_CROSS_NAME.NAME_ID is
'[170]����';

comment on column RD_CROSS_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_CROSS_NAME.PID is
'���,����"RD_CROSS"';

comment on column RD_CROSS_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_CROSS_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_CROSS_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

comment on column RD_CROSS_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSS_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_CROSS_NODE                                         */
/*==============================================================*/
create table RD_CROSS_NODE  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   IS_MAIN              NUMBER(1)                      default 0 not null
       check (IS_MAIN in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDCROSS_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDCROSS_NODES foreign key (PID)
         references RD_CROSS (PID)
);

comment on column RD_CROSS_NODE.PID is
'���,����"RD_CROSS"';

comment on column RD_CROSS_NODE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_CROSS_NODE.IS_MAIN is
'���Node��·�����㻹���ӵ�';

comment on column RD_CROSS_NODE.U_RECORD is
'�������±�ʶ';

comment on column RD_CROSS_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_DIRECTROUTE                                        */
/*==============================================================*/
create table RD_DIRECTROUTE  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   FLAG                 NUMBER(1)                      default 2 not null
       check (FLAG in (0,1,2)),
   PROCESS_FLAG         NUMBER(1)                      default 1 not null
       check (PROCESS_FLAG in (0,1,2)),
   RELATIONSHIP_TYPE    NUMBER(1)                      default 1 not null
       check (RELATIONSHIP_TYPE in (1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_DIRECTROUTE primary key (PID),
   constraint RDDIRECT_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDDIRECT_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDDIRECTROUTE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_DIRECTROUTE.PID is
'����';

comment on column RD_DIRECTROUTE.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_DIRECTROUTE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_DIRECTROUTE.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_DIRECTROUTE.FLAG is
'δ��֤,ʵ��˳��,����˳��';

comment on column RD_DIRECTROUTE.PROCESS_FLAG is
'�˹���ӻ�����';

comment on column RD_DIRECTROUTE.U_RECORD is
'�������±�ʶ';

comment on column RD_DIRECTROUTE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_DIRECTROUTE_VIA                                    */
/*==============================================================*/
create table RD_DIRECTROUTE_VIA  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDDIRECT_VIALINK foreign key (PID)
         references RD_DIRECTROUTE (PID),
   constraint RDDIRECTROUTE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_DIRECTROUTE_VIA is
'(1)���ͬһ����Link���˳�Link֮��Ķ��龭��Link,���Ҿ���Link����������Link���˳�Link
(2)NaviMap��ҵ��,��������˳�Linkֱ����ͬһ·�ڹҽ�ʱ,����������Link;����(�����߹�ϵ),��Ҫ��������Link';

comment on column RD_DIRECTROUTE_VIA.PID is
'���,����"RD_DIRECTROUTE"';

comment on column RD_DIRECTROUTE_VIA.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_DIRECTROUTE_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_DIRECTROUTE_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_DIRECTROUTE_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_DIRECTROUTE_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_ELECEYE_PAIR                                       */
/*==============================================================*/
create table RD_ELECEYE_PAIR  (
   GROUP_ID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_ELECEYE_PAIR primary key (GROUP_ID)
);

comment on column RD_ELECEYE_PAIR.U_RECORD is
'�������±�ʶ';

comment on column RD_ELECEYE_PAIR.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_ELECTRONICEYE                                      */
/*==============================================================*/
create table RD_ELECTRONICEYE  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)),
   KIND                 NUMBER(2)                      default 0 not null
       check (KIND in (0,1,2,3,10,11,12,13,14,15,16,17,18,19,20,21,22,23,98)),
   ANGLE                NUMBER(8,5)                    default 0 not null,
   LOCATION             NUMBER(2)                      default 0 not null,
   SPEED_LIMIT          NUMBER(4)                      default 0 not null,
   VERIFIED_FLAG        NUMBER(2)                      default 0 not null
       check (VERIFIED_FLAG in (0,1,2)),
   MESH_ID              NUMBER(6)                      default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   SRC_FLAG             VARCHAR2(2)                    default '1' not null
       check (SRC_FLAG in ('0','1','2','3')),
   CREATION_DATE        DATE,
   HIGH_VIOLATION       NUMBER(1)                      default 0 not null
       check (HIGH_VIOLATION in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_ELECTRONICEYE primary key (PID)
);

comment on column RD_ELECTRONICEYE.PID is
'����';

comment on column RD_ELECTRONICEYE.KIND is
'��������ͷ,�״��������ͷ,�ƶ�ʽ����,��ͨ�źŵ�����ͷ��';

comment on column RD_ELECTRONICEYE.ANGLE is
'[171A]������������ļн�,0~360��';

comment on column RD_ELECTRONICEYE.LOCATION is
'[172U]����3bit��ʾ,���ҵ�������Ϊ0~2bit,ÿbit��ʾһ��λ��(����),��ֵΪ0/1�ֱ��ʾ��/��,��: 101��ʾ�����
��0bit:��(Left)
��1bit:��(Right)
��2bit:��(Overhead)
�������bitλ��Ϊ0,��ʾδ����';

comment on column RD_ELECTRONICEYE.SPEED_LIMIT is
'��Kind=1~3��20��21 ʱ��Ч, ��λ:����/ʱ,ֵ��: 1~9999';

comment on column RD_ELECTRONICEYE.VERIFIED_FLAG is
'[173sp2]';

comment on column RD_ELECTRONICEYE.U_RECORD is
'�������±�ʶ';

comment on column RD_ELECTRONICEYE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_ELECEYE_PART                                       */
/*==============================================================*/
create table RD_ELECEYE_PART  (
   GROUP_ID             NUMBER(10)                      not null,
   ELECEYE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDELECPAIR foreign key (GROUP_ID)
         references RD_ELECEYE_PAIR (GROUP_ID),
   constraint RDELECPARTS foreign key (ELECEYE_PID)
         references RD_ELECTRONICEYE (PID)
);

comment on column RD_ELECEYE_PART.U_RECORD is
'�������±�ʶ';

comment on column RD_ELECEYE_PART.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_GATE                                               */
/*==============================================================*/
create table RD_GATE  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 2 not null
       check (TYPE in (0,1,2)),
   DIR                  NUMBER(1)                      default 2 not null
       check (DIR in (0,1,2)),
   FEE                  NUMBER(1)                      default 0 not null
       check (FEE in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_GATE primary key (PID),
   constraint RDGATE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDGATE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDGATE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_GATE is
'NaviMap����ʱ����Link���˳�Link�������';

comment on column RD_GATE.PID is
'����';

comment on column RD_GATE.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_GATE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_GATE.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_GATE.FEE is
'���,�շ�';

comment on column RD_GATE.U_RECORD is
'�������±�ʶ';

comment on column RD_GATE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_GATE_CONDITION                                     */
/*==============================================================*/
create table RD_GATE_CONDITION  (
   PID                  NUMBER(10)                      not null,
   VALID_OBJ            NUMBER(1)                      default 0 not null
       check (VALID_OBJ in (0,1)),
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDGATE_CONDITION foreign key (PID)
         references RD_GATE (PID)
);

comment on table RD_GATE_CONDITION is
'(1)��¼��Բ�ͬ"���Ŷ���"�Ŀ���ʱ��
(2)����ʾ"�������˾�ŵ�ʱ��",��ֱ�洢Ϊ������¼';

comment on column RD_GATE_CONDITION.PID is
'���,����"RD_GATE"';

comment on column RD_GATE_CONDITION.VALID_OBJ is
'����,����';

comment on column RD_GATE_CONDITION.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_GATE_CONDITION.U_RECORD is
'�������±�ʶ';

comment on column RD_GATE_CONDITION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_GSC                                                */
/*==============================================================*/
create table RD_GSC  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   PROCESS_FLAG         NUMBER(1)                      default 1 not null
       check (PROCESS_FLAG in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_GSC primary key (PID)
);

comment on table RD_GSC is
'��¼Link֮���������ϵ';

comment on column RD_GSC.PID is
'����';

comment on column RD_GSC.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ�����������';

comment on column RD_GSC.PROCESS_FLAG is
'����˹���ֵ,����ֵ,���⴦��ȷ�ʽ';

comment on column RD_GSC.U_RECORD is
'�������±�ʶ';

comment on column RD_GSC.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_GSC_LINK                                           */
/*==============================================================*/
create table RD_GSC_LINK  (
   PID                  NUMBER(10)                      not null,
   ZLEVEL               NUMBER(2)                      default 0 not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   SHP_SEQ_NUM          NUMBER(5)                      default 1 not null,
   START_END            NUMBER(1)                      default 0 not null
       check (START_END in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDGSC_LINK foreign key (PID)
         references RD_GSC (PID)
);

comment on table RD_GSC_LINK is
'��¼����������ϵ��LINK��Ϣ';

comment on column RD_GSC_LINK.PID is
'���,����"RD_GSC"';

comment on column RD_GSC_LINK.ZLEVEL is
'��¼����Link ֮������²�ι�ϵ,ֵ�����:
(1)����:0
(2)����: ����0
(3)����: С��0';

comment on column RD_GSC_LINK.LINK_PID is
'�ο�"RD_LINK","AD_LINK"��,��¼����������ϵ�Ĳ�ͬҪ�������Link����,���·Link,��·Link��';

comment on column RD_GSC_LINK.TABLE_NAME is
'��¼Ҫ�����ڵ���ݱ�,��LINK����Ϊ��·LINK=20��ˮϵLINK=40����ʱ,��ݱ���ֱ�Ϊ"RD_LINK"��"LC_LINK"';

comment on column RD_GSC_LINK.SHP_SEQ_NUM is
'(1)��¼��ǰ��������Link�ϵ�λ�����,�������㲻��Link��������״��ʱ�����
(2)��Ŵ�0��ʼ�������,��SHP_SEQ_NUM=0~N-1(NΪ���������յ����ڵ�Link�ܵ���).����,���(SHP_SEQ_NUM=0),�յ�(SHP_SEQ_NUM=N-1)';

comment on column RD_GSC_LINK.START_END is
'��¼��������LINK�����(SHP_SEQ_NUM=0),�յ�(SHP_SEQ_NUM=N-1)����״��';

comment on column RD_GSC_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_GSC_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_HGWG_LIMIT                                         */
/*==============================================================*/
create table RD_HGWG_LIMIT  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)),
   RES_HIGH             NUMBER(5,2)                    default 0 not null,
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   RES_AXLE_LOAD        NUMBER(5,2)                    default 0 not null,
   RES_WIDTH            NUMBER(5,2)                    default 0 not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_HGWG_LIMIT primary key (PID),
   constraint RDHGWG_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_HGWG_LIMIT.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_HGWG_LIMIT.U_RECORD is
'�������±�ʶ';

comment on column RD_HGWG_LIMIT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_INTER                                              */
/*==============================================================*/
create table RD_INTER  (
   PID                  NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_INTER primary key (PID)
);

comment on column RD_INTER.PID is
'����';

comment on column RD_INTER.U_RECORD is
'�������±�ʶ';

comment on column RD_INTER.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_INTER_LINK                                         */
/*==============================================================*/
create table RD_INTER_LINK  (
   PID                  NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDINTERSECTION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDINTERSECTION_LINKS foreign key (PID)
         references RD_INTER (PID)
);

comment on column RD_INTER_LINK.PID is
'���,����"RD_INTER"';

comment on column RD_INTER_LINK.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_INTER_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_INTER_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_INTER_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_INTER_NODE                                         */
/*==============================================================*/
create table RD_INTER_NODE  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDINTERSECTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDINTERSECTION_NODES foreign key (PID)
         references RD_INTER (PID)
);

comment on column RD_INTER_NODE.PID is
'���,����"RD_INTER"';

comment on column RD_INTER_NODE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_INTER_NODE.U_RECORD is
'�������±�ʶ';

comment on column RD_INTER_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE                                               */
/*==============================================================*/
create table RD_LANE  (
   LANE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   LANE_NUM             NUMBER(2)                      default 1 not null,
   TRAVEL_FLAG          NUMBER(1)                      default 0 not null
       check (TRAVEL_FLAG in (0,1)),
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   LANE_FORMING         NUMBER(2)                      default 0 not null
       check (LANE_FORMING in (0,1,2,3)),
   LANE_DIR             NUMBER(1)                      default 2 not null
       check (LANE_DIR in (1,2,3)),
   LANE_TYPE            NUMBER(10)                     default 1 not null,
   ARROW_DIR            VARCHAR2(1)                    default '9' not null
       check (ARROW_DIR in ('9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5')),
   LANE_MARK            NUMBER(2)                      default 0 not null
       check (LANE_MARK in (0,1,2,3,4,5,6,7,98,99)),
   WIDTH                NUMBER(5,2)                    default 0 not null,
   RESTRICT_HEIGHT      NUMBER(5,2)                    default 0 not null,
   TRANSITION_AREA      NUMBER(2)                      default 0 not null
       check (TRANSITION_AREA in (0,1,2)),
   FROM_MAX_SPEED       NUMBER(4)                      default 0 not null,
   TO_MAX_SPEED         NUMBER(4)                      default 0 not null,
   FROM_MIN_SPEED       NUMBER(4)                      default 0 not null,
   TO_MIN_SPEED         NUMBER(4)                      default 0 not null,
   ELEC_EYE             NUMBER(1)                      default 0 not null
       check (ELEC_EYE in (0,1,2)),
   LANE_DIVIDER         NUMBER(2)                      default 0 not null
       check (LANE_DIVIDER in (0,10,11,12,13,20,21,30,31,40,50,51,60,61,62,63,99)),
   CENTER_DIVIDER       NUMBER(2)                      default 0 not null,
   SPEED_FLAG           NUMBER(1)                      default 0 not null
       check (SPEED_FLAG in (0,1,2)),
   SRC_FLAG             NUMBER(1)                      default 1 not null
       check (SRC_FLAG in (1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_LANE primary key (LANE_PID),
   constraint RDLANE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LANE is
'��¼��������,��ͷ����,���ߵ�,�Լ����·Link�Ĺ�ϵ��';

comment on column RD_LANE.LANE_PID is
'����';

comment on column RD_LANE.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LANE.LANE_NUM is
'ע:��LINK�������һ��';

comment on column RD_LANE.TRAVEL_FLAG is
'[171A]';

comment on column RD_LANE.SEQ_NUM is
'��1��ʼ�������
ע:��½:������;�۰�:���ҵ���
';

comment on column RD_LANE.LANE_FORMING is
'[170]';

comment on column RD_LANE.LANE_DIR is
'ע: 
(1)����¼˫����LINK�ĳ�������,�����LINK���߷�����ͬΪ˳����,��֮�淽��
(2)����ֵ��';

comment on column RD_LANE.LANE_TYPE is
'����32bit ��ʾ,���ҵ�������Ϊ0~31bit,ÿbit ��ʾһ������(����),��ֵΪ0/1 �ֱ��ʾ��/��, ��:0000 0000 0011 0000 ��ʾ���س����Ϳ쳵��
��0bit:���泵��(Regular Lane)
��1bit:���ϳ���(Auxiliary Lane)
��2bit:���ٳ���(Accelerate Lane)
��3bit:���ٳ���(Decelerate Lane)
��4bit:���س���(HOV Lane)
��5bit:�쳵��(Express Lane)
��6bit:���(Slow Lane)
��7bit:������(Passing/Overtaking Lane)
��8bit:����ʻ·���(Drivable shoulder Lane)
��9bit:����ͣ����(Truck Parking Lane)
��10bit:���Ƴ���(Regulated Lane Access)
��11bit:���泵��(Reversible Lane)
��12bit:����ת�򳵵�(Center Turn Lane)
��13bit:ת�򳵵�(Turn Lane)
��14bit:�ճ���
��15bit:ת��ɱ䳵��
ע:
(1)���泵��/ ·�����ͣ������RD_LANE_CONDITION �������ʱ��δ���
(2)������ֻ����˫���·
(3)���Ƴ������ڹ��ƿ���';

comment on column RD_LANE.ARROW_DIR is
'��¼ÿһ�������ĵ����ͷ��Ϣ,����ת,ֱ��,��ͷ��';

comment on column RD_LANE.LANE_MARK is
'��¼ʵ�ʵ�·��ÿ�������ϻ��Ƶı���,��ͣ��������,����������,���������,�շ�վ���ٱ��ߵ�';

comment on column RD_LANE.WIDTH is
'[170]��λ:��';

comment on column RD_LANE.RESTRICT_HEIGHT is
'[170]��λ:��';

comment on column RD_LANE.TRANSITION_AREA is
'[170]';

comment on column RD_LANE.FROM_MAX_SPEED is
'[170]��λ:����/ʱ,ֵ��: 1~9999
ͬһLINK��˳��ͨ�еĸ������ٶ����Ʋ���ȫ��ͬʱ��ֵ������ΪĬ��ֵ0��';

comment on column RD_LANE.TO_MAX_SPEED is
'[173sp1][170]��λ:����/ʱ,ֵ��: 1~9999
ͬһLINK������ͨ�еĸ������ٶ����Ʋ���ȫ��ͬʱ��ֵ������ΪĬ��ֵ0��';

comment on column RD_LANE.FROM_MIN_SPEED is
'[170]��λ:����/ʱ,ֵ��: 1~9999';

comment on column RD_LANE.TO_MIN_SPEED is
'[170]��λ:����/ʱ,ֵ��: 1~9999';

comment on column RD_LANE.ELEC_EYE is
'�Ƿ��г���������';

comment on column RD_LANE.LANE_DIVIDER is
'[170][190A]��¼λ�ڳ��������Ҳ�(��½)�����(�۰�)�ķָ���';

comment on column RD_LANE.CENTER_DIVIDER is
'[170A][1901U]ע:��ݵ�·������������������:
��˫�����·:��¼��������
�ڵ�����������߷����·:��½��¼����߳����ָ��;��ۼ�¼���ұ߳����ָ�� 
';

comment on column RD_LANE.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE_CONDITION                                     */
/*==============================================================*/
create table RD_LANE_CONDITION  (
   LANE_PID             NUMBER(10)                      not null,
   DIRECTION            NUMBER(1)                      default 1 not null
       check (DIRECTION in (1,2,3)),
   DIRECTION_TIME       VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   VEHICLE_TIME         VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLANE_CONDITION foreign key (LANE_PID)
         references RD_LANE (LANE_PID)
);

comment on column RD_LANE_CONDITION.LANE_PID is
'���,����"RD_LANE"';

comment on column RD_LANE_CONDITION.DIRECTION is
'[210]������Ϊ��ϫ����ʱ����¼ĳʱ����ڳ�����ͨ�з��������LINK���߷�����ͬΪ˳���򣬷�֮�淽��;����ֵ��
';

comment on column RD_LANE_CONDITION.DIRECTION_TIME is
'��ʽ�ο�"ʱ����"';

comment on column RD_LANE_CONDITION.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_LANE_CONDITION.VEHICLE_TIME is
'��ʽ�ο�"ʱ����"';

comment on column RD_LANE_CONDITION.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE_CONDITION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE_CONNEXITY                                     */
/*==============================================================*/
create table RD_LANE_CONNEXITY  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   LANE_INFO            VARCHAR2(64),
   CONFLICT_FLAG        NUMBER(1)                      default 0 not null
       check (CONFLICT_FLAG in (0,1)),
   KG_FLAG              NUMBER(1)                      default 0 not null
       check (KG_FLAG in (0,1,2)),
   LANE_NUM             NUMBER(3)                      default 0 not null,
   LEFT_EXTEND          NUMBER(3)                      default 0 not null,
   RIGHT_EXTEND         NUMBER(3)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_LANE_CONNEXITY primary key (PID),
   constraint RDLANE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDLANE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_LANE_CONNEXITY is
'��¼���ŵĽ����ߵ��ϵ,�Լ�·�ڴ���·�ϵ�ͨ�г���,���Ҹ��ӳ�������ֱ�ҵȳ�����Ϣ
(1)·�ڳ���:����NODE���˳�LINK(RD_LANE_TOPOLOGY)ֵ��Ϊ0
(2)·�ϳ���:����NODE���˳�LINK(RD_LANE_TOPOLOGY)ֵ��Ϊ0';

comment on column RD_LANE_CONNEXITY.PID is
'����';

comment on column RD_LANE_CONNEXITY.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LANE_CONNEXITY.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_LANE_CONNEXITY.LANE_INFO is
'��¼·�ڴ����·�ϵĽ���LINK�������˳�LINK�ĳ�����Ϣ
(1)��һ������Ӣ�İ�ǳ��Ŵ��빹��,������RD_LANEһ��,��:"A"��ʾֱ��;"B"��ʾ"��ת
(2)�����ڸ��ӳ���,�򽫴�����Ӣ�İ������"[]"������,��"[A]"��ʾ����ֱ��,"[B]"��ʾ������ת,ͨ���ַ��˳��������󸽼ӻ����Ҹ���
(3)��ϳ���֮����Ӣ�İ��","�ָ�,��:"[A],A,C,[C]"��ʾ"�󸽼�ֱ��,ֱ��,��ת,�Ҹ�����ת"
(4)����ר�õ���ת����Ϣ��Ӣ�İ�Ǽ�����"<>"��ʾ,�繫��ר����ת����ʾΪ"<c>"
(5)������ĳһ������ᳵ���빫������ʱ,�ȱ����ᳵ����ת����Ϣ,���﹫��ת����Ϣ,�м��޶��ŷָ�,��:ĳһ������ᳵ��ֱ��,ͬʱ������תʱ,��ʾΪ:"b<c>"
(6)������ĳһ�������Ǹ��ӳ������ǹ���ר��ʱ,��ԭ��2 �Ļ���,��[]��︽�ӳ���,��:ĳһ�����Ƕ���ᳵ�����Ҹ���ֱ��,�����ǹ���ר����ת��ʾΪ:"[b<c>]"';

comment on column RD_LANE_CONNEXITY.LANE_NUM is
'����LINK��·�ڴ��ĳ�������(�����Ҹ��ӳ���)';

comment on column RD_LANE_CONNEXITY.LEFT_EXTEND is
'����LINK��·�ڴ����󸽼ӳ�����';

comment on column RD_LANE_CONNEXITY.RIGHT_EXTEND is
'����LINK��·�ڴ����Ҹ��ӳ�����';

comment on column RD_LANE_CONNEXITY.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE_CONNEXITY.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE_TOPOLOGY                                      */
/*==============================================================*/
create table RD_LANE_TOPOLOGY  (
   TOPOLOGY_ID          NUMBER(10)                      not null,
   CONNEXITY_PID        NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   IN_LANE_INFO         NUMBER(10)                     default 0 not null,
   BUS_LANE_INFO        NUMBER(10)                     default 0 not null,
   REACH_DIR            NUMBER(1)                      default 0 not null
       check (REACH_DIR in (0,1,2,3,4,5,6)),
   RELATIONSHIP_TYPE    NUMBER(1)                      default 1 not null
       check (RELATIONSHIP_TYPE in (1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_LANE_TOPOLOGY primary key (TOPOLOGY_ID),
   constraint RDLANE_TOPOLOGY foreign key (CONNEXITY_PID)
         references RD_LANE_CONNEXITY (PID),
   constraint RDLANEVTOPOLOGY_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LANE_TOPOLOGY.TOPOLOGY_ID is
'����';

comment on column RD_LANE_TOPOLOGY.CONNEXITY_PID is
'���,����"RD_LANE_CONNEXITY"';

comment on column RD_LANE_TOPOLOGY.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LANE_TOPOLOGY.IN_LANE_INFO is
'����16bit��ʾ,1Ϊ���ó���,0Ϊ������,Ĭ��Ϊ0';

comment on column RD_LANE_TOPOLOGY.BUS_LANE_INFO is
'ͬ"���복����Ϣ"';

comment on column RD_LANE_TOPOLOGY.REACH_DIR is
'NaviMap������,ֱ(1),��(a),��(4),��(7),��бǰ(b,c),��бǰ(2,3)������ΪGDB��Ӧ��NIDB��ֵ';

comment on column RD_LANE_TOPOLOGY.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE_TOPOLOGY.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE_TOPO_DETAIL                                   */
/*==============================================================*/
create table RD_LANE_TOPO_DETAIL  (
   TOPO_ID              NUMBER(10)                      not null,
   IN_LANE_PID          NUMBER(10)                      not null,
   OUT_LANE_PID         NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   REACH_DIR            NUMBER(1)                      default 0 not null
       check (REACH_DIR in (0,1,2,3,4,5,6)),
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   PROCESS_FLAG         NUMBER(1)                      default 2 not null
       check (PROCESS_FLAG in (1,2,3)),
   THROUGH_TURN         NUMBER(1)                      default 0
       check (THROUGH_TURN is null or (THROUGH_TURN in (0,1))),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_LANE_TOPO_DETAIL primary key (TOPO_ID),
   constraint RDLANE_DETAIL_IN foreign key (IN_LANE_PID)
         references RD_LANE (LANE_PID),
   constraint RDLANE_DETAIL_OUT foreign key (OUT_LANE_PID)
         references RD_LANE (LANE_PID),
   constraint RDLANE_DETAIL_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDLANE_DETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDLANETOPODTLNODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_LANE_TOPO_DETAIL is
'��¼��ϸ����֮�����ͨ��ϵ,����·�ڳ�����·�ϳ���������¼�ͳ���С����ͨ�еĳ�����ͨ��ϵ��';

comment on column RD_LANE_TOPO_DETAIL.TOPO_ID is
'[170]����';

comment on column RD_LANE_TOPO_DETAIL.IN_LANE_PID is
'���,����"RD_LANE"';

comment on column RD_LANE_TOPO_DETAIL.OUT_LANE_PID is
'���,����"RD_LANE"';

comment on column RD_LANE_TOPO_DETAIL.IN_LINK_PID is
'[171A]���,����"RD_LINK"';

comment on column RD_LANE_TOPO_DETAIL.OUT_LINK_PID is
'[171A]���,����"RD_LINK"';

comment on column RD_LANE_TOPO_DETAIL.REACH_DIR is
'[170]��¼���복�����˳�������ͨ�﷽��
[260]���ֶ���Ч';

comment on column RD_LANE_TOPO_DETAIL.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_LANE_TOPO_DETAIL.VEHICLE is
'��ʽ�ο�"��������"�����ֶ���Ч����Ϊ�������¼�ͳ���С����ͨ�еĳ�����ͨ��ϵ';

comment on column RD_LANE_TOPO_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE_TOPO_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE_TOPO_VIA                                      */
/*==============================================================*/
create table RD_LANE_TOPO_VIA  (
   TOPO_ID              NUMBER(10)                      not null,
   LANE_PID             NUMBER(10)                     default 0 not null,
   VIA_LINK_PID         NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLANETOPODETAIL_VIA foreign key (TOPO_ID)
         references RD_LANE_TOPO_DETAIL (TOPO_ID),
   constraint RDLANE_TOPO_VIA_LINK foreign key (VIA_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LANE_TOPO_VIA is
'[170]';

comment on column RD_LANE_TOPO_VIA.TOPO_ID is
'���,����"RD_LANE_TOPO_DETAIL"';

comment on column RD_LANE_TOPO_VIA.LANE_PID is
'[171U]�ο�"RD_LANE",������˳�LANE ����';

comment on column RD_LANE_TOPO_VIA.VIA_LINK_PID is
'[171A]���,����"RD_LINK"';

comment on column RD_LANE_TOPO_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_LANE_TOPO_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_LANE_TOPO_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE_TOPO_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LANE_VIA                                           */
/*==============================================================*/
create table RD_LANE_VIA  (
   TOPOLOGY_ID          NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLANE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDLANETOPOLOGY_VIALINK foreign key (TOPOLOGY_ID)
         references RD_LANE_TOPOLOGY (TOPOLOGY_ID)
);

comment on table RD_LANE_VIA is
'(1)���ͬһ����Link���˳�Link֮��Ķ��龭��Link,���Ҿ���Link����������Link���˳�Link
(2)NaviMap��ҵ��,��������˳�Linkֱ����ͬһ·�ڹҽ�ʱ,����Link��·�ڵĽ������Link��ȡ;����,�ɸ���ʵ��ļ���ԭ��(�羭��Link����,ͨ�з���һ�µ�)ȡ�Ƽ�·��';

comment on column RD_LANE_VIA.TOPOLOGY_ID is
'���,����"RD_LANE_TOPOLOGY"';

comment on column RD_LANE_VIA.LINK_PID is
'���,����"RD_LINK",������˳�LINK����';

comment on column RD_LANE_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_LANE_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_LANE_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_LANE_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_ADDRESS                                       */
/*==============================================================*/
create table RD_LINK_ADDRESS  (
   LINK_PID             NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   LEFT_START           VARCHAR2(20),
   LEFT_END             VARCHAR2(20),
   LEFT_TYPE            NUMBER(1)                      default 0 not null
       check (LEFT_TYPE in (0,1,2,3,4)),
   RIGHT_START          VARCHAR2(20),
   RIGHT_END            VARCHAR2(20),
   RIGHT_TYPE           NUMBER(1)                      default 0 not null
       check (RIGHT_TYPE in (0,1,2,3,4)),
   ADDRESS_TYPE         NUMBER(1)                      default 0 not null
       check (ADDRESS_TYPE in (0,1,2,3)),
   WORK_DIR             NUMBER(1)                      default 0 not null
       check (WORK_DIR in (0,1,2)),
   SRC_FLAG             NUMBER(1)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_ADDRESS foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LINK_ADDRESS is
'��ѧԺ·(NAME_ID)10�ŵ�100��,ѧԺ·��ΪLink1,Link2,���ʾLink1�ϵ�ѧԺ·10�ŵ�25��,Link2�ϵ�ѧԺ·26�ŵ�100��';

comment on column RD_LINK_ADDRESS.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_ADDRESS.NAME_GROUPID is
'[170]�ο�"RD_NAME"';

comment on column RD_LINK_ADDRESS.LEFT_TYPE is
'����,˫��,��Ϻŵ�';

comment on column RD_LINK_ADDRESS.RIGHT_TYPE is
'����,˫��,��Ϻŵ�';

comment on column RD_LINK_ADDRESS.ADDRESS_TYPE is
'���ַ,ԭ��ַ,���õ�ַ';

comment on column RD_LINK_ADDRESS.WORK_DIR is
'���Ƹ�ֵ�Ĳο�����,��˳����,�淽��';

comment on column RD_LINK_ADDRESS.SRC_FLAG is
'��¼�����Ƶ���Դ,�������,��ҵ�ɼ���';

comment on column RD_LINK_ADDRESS.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_ADDRESS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_FORM                                          */
/*==============================================================*/
create table RD_LINK_FORM  (
   LINK_PID             NUMBER(10)                      not null,
   FORM_OF_WAY          NUMBER(2)                      default 1 not null
       check (FORM_OF_WAY in (0,1,2,10,11,12,13,14,15,16,17,18,20,21,22,23,24,30,31,32,33,34,35,36,37,38,39,43,48,49,50,51,52,53,54,57,60,80,81,82)),
   EXTENDED_FORM        NUMBER(2)                      default 0 not null
       check (EXTENDED_FORM in (0,40,41,42)),
   AUXI_FLAG            NUMBER(2)                      default 0 not null
       check (AUXI_FLAG in (0,55,56,58,70,71,72,73,76,77)),
   KG_FLAG              NUMBER(1)                      default 0 not null
       check (KG_FLAG in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_FORM foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_FORM.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_FORM.EXTENDED_FORM is
'[171A]';

comment on column RD_LINK_FORM.AUXI_FLAG is
'[171A]';

comment on column RD_LINK_FORM.KG_FLAG is
'��ֵ�·��̬��Kר��,Gר��,KG���õı�־';

comment on column RD_LINK_FORM.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_FORM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_INTRTICS foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

/*==============================================================*/
/* Table: RD_LINK_LIMIT                                         */
/*==============================================================*/
create table RD_LINK_LIMIT  (
   LINK_PID             NUMBER(10)                      not null,
   TYPE                 NUMBER(2)                      default 3 not null
       check (TYPE in (0,1,2,3,4,5,6,7,8,9,10)),
   LIMIT_DIR            NUMBER(1)                      default 0 not null
       check (LIMIT_DIR in (0,1,2,3,9)),
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   TOLL_TYPE            NUMBER(1)                      default 9 not null
       check (TOLL_TYPE in (0,1,2,3,4,5,6,9)),
   WEATHER              NUMBER(1)                      default 9 not null
       check (WEATHER in (0,1,2,3,9)),
   INPUT_TIME           VARCHAR2(32),
   PROCESS_FLAG         NUMBER(1)                      default 0 not null
       check (PROCESS_FLAG in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_LIMIT foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_LIMIT.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_LIMIT.LIMIT_DIR is
'˫����,˳����,�淽��';

comment on column RD_LINK_LIMIT.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_LINK_LIMIT.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_LINK_LIMIT.TOLL_TYPE is
'��TYPE=6(Usage Fee Required)ʱ��Ч,����Ϊ9';

comment on column RD_LINK_LIMIT.WEATHER is
'[170]��TYPE=7(��������)�ҳ�������Ϊ����ʱ��Ч';

comment on column RD_LINK_LIMIT.INPUT_TIME is
'��TYPE=0��=4(ʩ���в����Ż��·ά����)ʱ��Ч,����Ϊ��';

comment on column RD_LINK_LIMIT.PROCESS_FLAG is
'��ҵ�ɼ�(�˹���ֵ),δ��֤(����ֵ)';

comment on column RD_LINK_LIMIT.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_LIMIT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_LIMIT_TRUCK                                   */
/*==============================================================*/
create table RD_LINK_LIMIT_TRUCK  (
   LINK_PID             NUMBER(10)                      not null,
   LIMIT_DIR            NUMBER(1)                      default 0 not null
       check (LIMIT_DIR in (0,1,2,3,9)),
   TIME_DOMAIN          VARCHAR2(1000),
   RES_TRAILER          NUMBER(1)                      default 0 not null
       check (RES_TRAILER in (0,1)),
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   RES_AXLE_LOAD        NUMBER(5,2)                    default 0 not null,
   RES_AXLE_COUNT       NUMBER(2)                      default 0 not null,
   RES_OUT              NUMBER(1)                      default 0 not null
       check (RES_OUT in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_LIMIT_TRUCK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_LIMIT_TRUCK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_LIMIT_TRUCK.LIMIT_DIR is
'˫����,˳����,�淽��';

comment on column RD_LINK_LIMIT_TRUCK.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_LINK_LIMIT_TRUCK.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_LIMIT_TRUCK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_NAME                                          */
/*==============================================================*/
create table RD_LINK_NAME  (
   LINK_PID             NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2,3)),
   INPUT_TIME           VARCHAR2(32),
   NAME_TYPE            NUMBER(2)                      default 0 not null
       check (NAME_TYPE in (0,1,2,3,4,5,6,7,8,9,14,15)),
   SRC_FLAG             NUMBER(1)                      default 9 not null
       check (SRC_FLAG in (0,1,2,3,4,5,6,9)),
   ROUTE_ATT            NUMBER(1)                      default 0 not null
       check (ROUTE_ATT in (0,1,2,3,4,5,9)),
   CODE                 NUMBER(1)                      default 0 not null
       check (CODE in (0,1,2,9)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_NAMES foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_NAME.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_NAME.NAME_GROUPID is
'[170]�ο�"RD_NAME"';

comment on column RD_LINK_NAME.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_LINK_NAME.NAME_CLASS is
'(1)��Ϊ�ٷ����,����,������������������
(2)NaviMap��,��Ϊ������ʱ,���¼������ʱ��(OLD_NAME_TIME)';

comment on column RD_LINK_NAME.INPUT_TIME is
'[170](1)��NAME_CLASS=3 ʱ��Ч,����Ϊ��
(2)��¼��ʽΪ��ݰ汾,��"10 ��"';

comment on column RD_LINK_NAME.NAME_TYPE is
'���Junction Name,��������(��·)';

comment on column RD_LINK_NAME.SRC_FLAG is
'ע:������������ʱ,���ж�Link ���Ƿ��йٷ���,���򸳹ٷ���;���򸳱���';

comment on column RD_LINK_NAME.ROUTE_ATT is
'(1)��¼·�ߵ�������,���⻷
(2)NaviMap���ת����ֵΪ15��תΪ9(δ����)';

comment on column RD_LINK_NAME.CODE is
'ע:��Link �ֱ�Ϊ����,���и���,���ʱ,����CODE=1,����Ϊ0';

comment on column RD_LINK_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint FK_RDLINK_PARAM_ADAS foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LINK_PARAM_ADAS is
'[190A]';

comment on column RD_LINK_PARAM_ADAS.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_PARAM_ADAS.OFFSET is
'��λ:��';

comment on column RD_LINK_PARAM_ADAS.RADIUS is
'��λ:��,����������ĳ������ʰ뾶';

comment on column RD_LINK_PARAM_ADAS.LEN is
'��λ:��,����������ָ���㵽���������߳�';

comment on column RD_LINK_PARAM_ADAS.PARAM is
'�������ߵĲ���,A2=C Ϊ����';

comment on column RD_LINK_PARAM_ADAS.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_PARAM_ADAS.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_RTIC                                          */
/*==============================================================*/
create table RD_LINK_RTIC  (
   LINK_PID             NUMBER(10)                      not null,
   CODE                 NUMBER(5)                      default 0 not null,
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
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_RTICS foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LINK_RTIC is
'��¼��·LINK��RTICʵʱ��ͨ��Ϣ';

comment on column RD_LINK_RTIC.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_RTIC.CODE is
'ͬһͼ����,ͬһRTIC�ȼ���RTIC�������Ψһ,ֵ��Χ:1~4095';

comment on column RD_LINK_RTIC.RANK is
'����,���и���,���ߵ�·';

comment on column RD_LINK_RTIC.RTIC_DIR is
'��¼RTIC�ĸ�ֵ����,��˳����,�淽��';

comment on column RD_LINK_RTIC.UPDOWN_FLAG is
'(1)˫�����·����ֱ������к�����
(2)����������߷����·��Ϊ����';

comment on column RD_LINK_RTIC.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_RTIC.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_SIDEWALK                                      */
/*==============================================================*/
create table RD_LINK_SIDEWALK  (
   LINK_PID             NUMBER(10)                      not null,
   SIDEWALK_LOC         NUMBER(1)                      default 0 not null
       check (SIDEWALK_LOC in (0,1,2,3,4,5,6,7,8)),
   DIVIDER_TYPE         NUMBER(1)                      default 0 not null
       check (DIVIDER_TYPE in (0,1,2,3,4)),
   WORK_DIR             NUMBER(1)                      default 0 not null
       check (WORK_DIR in (0,1,2)),
   PROCESS_FLAG         NUMBER(1)                      default 0 not null
       check (PROCESS_FLAG in (0,1)),
   CAPTURE_FLAG         NUMBER(1)                      default 0 not null
       check (CAPTURE_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_SIDEWALK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LINK_SIDEWALK is
'���б����λ�ڵ�·�����һ��,·������õ�ר������ͨ�еĵ�·';

comment on column RD_LINK_SIDEWALK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_SIDEWALK.SIDEWALK_LOC is
'��¼Link����λ��,���Ҳ�,�м�,���';

comment on column RD_LINK_SIDEWALK.DIVIDER_TYPE is
'�߶Ȳ����(��·��),����դ������,���߸����';

comment on column RD_LINK_SIDEWALK.WORK_DIR is
'���б����ֵ�Ĳο�����,��˳����,�淽��';

comment on column RD_LINK_SIDEWALK.PROCESS_FLAG is
'����˹���ֵ,����ֵ�ȷ�ʽ';

comment on column RD_LINK_SIDEWALK.CAPTURE_FLAG is
'��¼�ɼ�״̬,��δ�ɼ�,��ҵȷ��';

comment on column RD_LINK_SIDEWALK.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_SIDEWALK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_SPEEDLIMIT                                    */
/*==============================================================*/
create table RD_LINK_SPEEDLIMIT  (
   LINK_PID             NUMBER(10)                      not null,
   FROM_SPEED_LIMIT     NUMBER(4)                      default 0 not null,
   TO_SPEED_LIMIT       NUMBER(4)                      default 0 not null,
   SPEED_CLASS          NUMBER(1)                      default 0 not null
       check (SPEED_CLASS between 0 and 8 and SPEED_CLASS in (0,1,2,3,4,5,6,7,8)),
   FROM_LIMIT_SRC       NUMBER(2)                      default 0 not null
       check (FROM_LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)),
   TO_LIMIT_SRC         NUMBER(2)                      default 0 not null
       check (TO_LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)),
   SPEED_TYPE           NUMBER(1)                      default 0 not null
       check (SPEED_TYPE in (0,1 ,3 )),
   SPEED_DEPENDENT      NUMBER(2)                      default 0 not null
       check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  ,13,14,15,16,17,18)),
   TIME_DOMAIN          VARCHAR2(1000),
   SPEED_CLASS_WORK     NUMBER(1)                      default 1 not null
       check (SPEED_CLASS_WORK in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_SPEEDLIMIT foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_SPEEDLIMIT.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_SPEEDLIMIT.FROM_SPEED_LIMIT is
'(1)�洢��Linkͬ����(��˳��)���������
(2)����ֵΪ����,Ӧ��ʱ����10,��λ��0.1km/h,Ĭ��Ϊ0';

comment on column RD_LINK_SPEEDLIMIT.TO_SPEED_LIMIT is
'(1)�洢��Link������(������)���������
(2)����ֵΪ����,Ӧ��ʱ����10,��λ��0.1km/h,Ĭ��Ϊ0';

comment on column RD_LINK_SPEEDLIMIT.FROM_LIMIT_SRC is
'��¼˳��������Ϣ����Դ,������ʶ,���ٱ�ʶ,��������,�������ٵ�';

comment on column RD_LINK_SPEEDLIMIT.TO_LIMIT_SRC is
'��¼����������Ϣ����Դ,������ʶ,���ٱ�ʶ,��������,�������ٵ�';

comment on column RD_LINK_SPEEDLIMIT.SPEED_TYPE is
'[170][172U][190A]';

comment on column RD_LINK_SPEEDLIMIT.SPEED_DEPENDENT is
'[170][172U]';

comment on column RD_LINK_SPEEDLIMIT.TIME_DOMAIN is
'[170]��ʽ�ο�"ʱ����"';

comment on column RD_LINK_SPEEDLIMIT.SPEED_CLASS_WORK is
'[181U][190U]��¼���ٵȼ��ĸ�ֵ���';

comment on column RD_LINK_SPEEDLIMIT.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_SPEEDLIMIT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_SPEED_TRUCK                                   */
/*==============================================================*/
create table RD_LINK_SPEED_TRUCK  (
   LINK_PID             NUMBER(10)                      not null,
   FROM_SPEED_LIMIT     NUMBER(4)                      default 0 not null,
   TO_SPEED_LIMIT       NUMBER(4)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint FK_RD_LINK__RDLINKSPDTRUCK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_SPEED_TRUCK.FROM_SPEED_LIMIT is
'(1)�洢��Linkͬ����(��˳��)���������
(2)����ֵΪ����,Ӧ��ʱ����10,��λ��0.1km/h,Ĭ��Ϊ0';

comment on column RD_LINK_SPEED_TRUCK.TO_SPEED_LIMIT is
'(1)�洢��Link������(������)���������
(2)����ֵΪ����,Ӧ��ʱ����10,��λ��0.1km/h,Ĭ��Ϊ0';

comment on column RD_LINK_SPEED_TRUCK.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_SPEED_TRUCK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_WALKSTAIR                                     */
/*==============================================================*/
create table RD_LINK_WALKSTAIR  (
   LINK_PID             NUMBER(10)                      not null,
   STAIR_LOC            NUMBER(1)                      default 0 not null
       check (STAIR_LOC in (0,1,2,3,4,5,6,7)),
   STAIR_FLAG           NUMBER(1)                      default 0 not null
       check (STAIR_FLAG in (0,1,2)),
   WORK_DIR             NUMBER(1)                      default 0 not null
       check (WORK_DIR in (0,1,2)),
   CAPTURE_FLAG         NUMBER(1)                      default 0 not null
       check (CAPTURE_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_STAIR foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_LINK_WALKSTAIR is
'�����кܶ��ִ�����ʽ:��·������,���й���ĵ��﷽ʽ,�������ӵ���ͨ�ϰ�,������ͨ����ڼ�վ��ĵ��﷽ʽ��';

comment on column RD_LINK_WALKSTAIR.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_WALKSTAIR.STAIR_LOC is
'���н�������λ��,���Ҳ�,�м�,���';

comment on column RD_LINK_WALKSTAIR.STAIR_FLAG is
'����,����';

comment on column RD_LINK_WALKSTAIR.WORK_DIR is
'���ݸ�ֵ�Ĳο�����,��˳����,�淽��';

comment on column RD_LINK_WALKSTAIR.CAPTURE_FLAG is
'��¼�ɼ�״̬,��δ�ɼ�,��ҵȷ��';

comment on column RD_LINK_WALKSTAIR.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_WALKSTAIR.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_LINK_WARNING                                       */
/*==============================================================*/
create table RD_LINK_WARNING  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)),
   GEOMETRY             SDO_GEOMETRY,
   TYPE_CODE            VARCHAR2(5),
   VALID_DIS            NUMBER(5)                      default 0 not null,
   WARN_DIS             NUMBER(5)                      default 0 not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   DESCRIPT             VARCHAR2(100),
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_LINK_WARNING primary key (PID)
);

/*==============================================================*/
/* Table: RD_LINK_ZONE                                          */
/*==============================================================*/
create table RD_LINK_ZONE  (
   LINK_PID             NUMBER(10)                      not null,
   REGION_ID            NUMBER(10)                     default 0 not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3)),
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDLINK_ZONES foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_LINK_ZONE.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_LINK_ZONE.REGION_ID is
'[170]�ο�"AD_ADMIN",ͨ����������Ҷ�Ӧ�����������ZONE ����';

comment on column RD_LINK_ZONE.TYPE is
'AOIZone,KDZone,GCZone';

comment on column RD_LINK_ZONE.SIDE is
'��¼Zone����Link��������Ҳ�';

comment on column RD_LINK_ZONE.U_RECORD is
'�������±�ʶ';

comment on column RD_LINK_ZONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_MAINSIDE                                           */
/*==============================================================*/
create table RD_MAINSIDE  (
   GROUP_ID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_MAINSIDE primary key (GROUP_ID)
);

comment on column RD_MAINSIDE.GROUP_ID is
'����';

comment on column RD_MAINSIDE.U_RECORD is
'�������±�ʶ';

comment on column RD_MAINSIDE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_MAINSIDE_LINK                                      */
/*==============================================================*/
create table RD_MAINSIDE_LINK  (
   GROUP_ID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(2)                      default 0 not null,
   LINK_PID             NUMBER(10)                      not null,
   LINK_TYPE            NUMBER(1)                      default 0 not null
       check (LINK_TYPE in (0,1)),
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDMAINSIDE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDMAINSIDE foreign key (GROUP_ID)
         references RD_MAINSIDE (GROUP_ID)
);

comment on table RD_MAINSIDE_LINK is
'ͨ��ָ���������·֮��Ĺ�ϵ';

comment on column RD_MAINSIDE_LINK.GROUP_ID is
'���,����"RD_MAINSIDE"';

comment on column RD_MAINSIDE_LINK.SEQ_NUM is
'���Ҳ�ֱ��1��ʼ�������';

comment on column RD_MAINSIDE_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_MAINSIDE_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_MAINSIDE_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_MILEAGEPILE                                        */
/*==============================================================*/
create table RD_MILEAGEPILE  (
   PID                  NUMBER(10)                      not null,
   MILEAGE_NUM          NUMBER(10,3)                    not null,
   LINK_PID             NUMBER(10)                      not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,1,2)),
   ROAD_NAME            VARCHAR2(50),
   ROAD_NUM             VARCHAR2(50),
   ROAD_TYPE            NUMBER(1)                      default 1 not null
       check (ROAD_TYPE in (1,2,3)),
   SOURCE               NUMBER(1)                      default 1 not null
       check (SOURCE in (1,2,3)),
   DLLX                 VARCHAR2(50),
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null,
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_MILEAGEPILE primary key (PID),
   constraint FK_RDLINK_MILEAGEPILE foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_MILEAGEPILE is
'���׮��';

/*==============================================================*/
/* Table: RD_MULTIDIGITIZED                                     */
/*==============================================================*/
create table RD_MULTIDIGITIZED  (
   GROUP_ID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_MULTIDIGITIZED primary key (GROUP_ID)
);

comment on column RD_MULTIDIGITIZED.GROUP_ID is
'����';

comment on column RD_MULTIDIGITIZED.U_RECORD is
'�������±�ʶ';

comment on column RD_MULTIDIGITIZED.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_MULTIDIGITIZED_LINK                                */
/*==============================================================*/
create table RD_MULTIDIGITIZED_LINK  (
   GROUP_ID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   LINK_PID             NUMBER(10)                      not null,
   SIDE                 NUMBER(1)                      default 1 not null
       check (SIDE in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDMULTIDIGITIZED_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDMULTIDIGITIZED foreign key (GROUP_ID)
         references RD_MULTIDIGITIZED (GROUP_ID)
);

comment on table RD_MULTIDIGITIZED_LINK is
'(1)ͨ������ָ����������߷����·֮��Ĺ�ϵ
(2)���δ��ȷ,NaviMap�༭ƽ̨�ݲ�ʵ��';

comment on column RD_MULTIDIGITIZED_LINK.GROUP_ID is
'���,����"RD_MULTIDIGITIZED"';

comment on column RD_MULTIDIGITIZED_LINK.SEQ_NUM is
'���Ҳ�ֱ��1 ��ʼ�������';

comment on column RD_MULTIDIGITIZED_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_MULTIDIGITIZED_LINK.SIDE is
'�����Ҳ�';

comment on column RD_MULTIDIGITIZED_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_MULTIDIGITIZED_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_NATGUD_JUN                                         */
/*==============================================================*/
create table RD_NATGUD_JUN  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_NATGUD_JUN primary key (PID),
   constraint RDNATGUD_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDNATGUD_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_NATGUD_JUN.PID is
'����';

comment on column RD_NATGUD_JUN.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_NATGUD_JUN.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_NATGUD_JUN.U_RECORD is
'�������±�ʶ';

comment on column RD_NATGUD_JUN.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_NATGUD_JUN_DETAIL                                  */
/*==============================================================*/
create table RD_NATGUD_JUN_DETAIL  (
   DETAIL_ID            NUMBER(10)                      not null,
   NG_COND_PID          NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   NG_ASSO_PID          NUMBER(10)                      not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)),
   EXP_LINK_PID         NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_NATGUD_JUN_DETAIL primary key (DETAIL_ID),
   constraint RDNATGUD_DETAIL foreign key (NG_COND_PID)
         references RD_NATGUD_JUN (PID),
   constraint RDNATGUD_IXNATGUD foreign key (NG_ASSO_PID)
         references IX_NATGUD (PID),
   constraint RDNATGUD_DETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDNATGUD_DETAIL_EXPLINK foreign key (EXP_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_NATGUD_JUN_DETAIL.DETAIL_ID is
'����';

comment on column RD_NATGUD_JUN_DETAIL.NG_COND_PID is
'����';

comment on column RD_NATGUD_JUN_DETAIL.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_NATGUD_JUN_DETAIL.NG_ASSO_PID is
'����';

comment on column RD_NATGUD_JUN_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column RD_NATGUD_JUN_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_NATGUD_JUN_VIA                                     */
/*==============================================================*/
create table RD_NATGUD_JUN_VIA  (
   DETAIL_ID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDNATGUDVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_NATGUD_JUN_DETAIL (DETAIL_ID),
   constraint RDNATGUD_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_NATGUD_JUN_VIA.DETAIL_ID is
'����';

comment on column RD_NATGUD_JUN_VIA.LINK_PID is
'���,����"RD_LINK",������˳�Link ����';

comment on column RD_NATGUD_JUN_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_NATGUD_JUN_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_NATGUD_JUN_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_NATGUD_JUN_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_NODE_FORM                                          */
/*==============================================================*/
create table RD_NODE_FORM  (
   NODE_PID             NUMBER(10)                      not null,
   FORM_OF_WAY          NUMBER(2)                      default 1 not null
       check (FORM_OF_WAY in (0,1,2,3,4,5,6,10,11,12,13,14,15,16,20,21,22,23,30,31,32,41)),
   AUXI_FLAG            NUMBER(2)                      default 0 not null
       check (AUXI_FLAG in (0,42,43)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDNODE_FORM foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_NODE_FORM.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_NODE_FORM.AUXI_FLAG is
'[171A]';

comment on column RD_NODE_FORM.U_RECORD is
'�������±�ʶ';

comment on column RD_NODE_FORM.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_NODE_MESH                                          */
/*==============================================================*/
create table RD_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDNODE_MESH foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_NODE_MESH.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_NODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column RD_NODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_NODE_NAME                                          */
/*==============================================================*/
create table RD_NODE_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   NODE_PID             NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_NODE_NAME primary key (NAME_ID),
   constraint RDNODE_NAME foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_NODE_NAME.NAME_ID is
'[170]����';

comment on column RD_NODE_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_NODE_NAME.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_NODE_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_NODE_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_NODE_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

comment on column RD_NODE_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_NODE_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_OBJECT                                             */
/*==============================================================*/
create table RD_OBJECT  (
   PID                  NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_OBJECT primary key (PID)
);

comment on column RD_OBJECT.PID is
'����';

comment on column RD_OBJECT.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column RD_OBJECT.U_RECORD is
'�������±�ʶ';

comment on column RD_OBJECT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_OBJECT_INTER                                       */
/*==============================================================*/
create table RD_OBJECT_INTER  (
   PID                  NUMBER(10)                      not null,
   INTER_PID            NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDOBJECT_INTERSECTION foreign key (INTER_PID)
         references RD_INTER (PID),
   constraint RDOBJECT_INTERSECTIONS foreign key (PID)
         references RD_OBJECT (PID)
);

comment on column RD_OBJECT_INTER.PID is
'���,����"RD_OBJECT"';

comment on column RD_OBJECT_INTER.INTER_PID is
'���,����"RD_INTER"';

comment on column RD_OBJECT_INTER.U_RECORD is
'�������±�ʶ';

comment on column RD_OBJECT_INTER.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_OBJECT_LINK                                        */
/*==============================================================*/
create table RD_OBJECT_LINK  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDOBJECT_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDOBJECT_LINKS foreign key (PID)
         references RD_OBJECT (PID)
);

comment on column RD_OBJECT_LINK.PID is
'���,����"RD_OBJECT"';

comment on column RD_OBJECT_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_OBJECT_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_OBJECT_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_OBJECT_NAME                                        */
/*==============================================================*/
create table RD_OBJECT_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_OBJECT_NAME primary key (NAME_ID),
   constraint RDOBJECT_NAME foreign key (PID)
         references RD_OBJECT (PID)
);

comment on column RD_OBJECT_NAME.NAME_ID is
'[170]����';

comment on column RD_OBJECT_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_OBJECT_NAME.PID is
'���,����"RD_OBJECT"';

comment on column RD_OBJECT_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_OBJECT_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_OBJECT_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

comment on column RD_OBJECT_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_OBJECT_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_OBJECT_NODE                                        */
/*==============================================================*/
create table RD_OBJECT_NODE  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDOBJECT_NODES foreign key (PID)
         references RD_OBJECT (PID),
   constraint RDOBJECT_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_OBJECT_NODE.PID is
'���,����"RD_OBJECT"';

comment on column RD_OBJECT_NODE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_OBJECT_NODE.U_RECORD is
'�������±�ʶ';

comment on column RD_OBJECT_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_ROAD                                               */
/*==============================================================*/
create table RD_ROAD  (
   PID                  NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_ROAD primary key (PID)
);

comment on column RD_ROAD.PID is
'����';

comment on column RD_ROAD.U_RECORD is
'�������±�ʶ';

comment on column RD_ROAD.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_OBJECT_ROAD                                        */
/*==============================================================*/
create table RD_OBJECT_ROAD  (
   PID                  NUMBER(10)                      not null,
   ROAD_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDOBJECT_ROAD foreign key (ROAD_PID)
         references RD_ROAD (PID),
   constraint RDOBJECT_ROADS foreign key (PID)
         references RD_OBJECT (PID)
);

comment on column RD_OBJECT_ROAD.PID is
'���,����"RD_OBJECT"';

comment on column RD_OBJECT_ROAD.ROAD_PID is
'���,����"RD_ROAD"';

comment on column RD_OBJECT_ROAD.U_RECORD is
'�������±�ʶ';

comment on column RD_OBJECT_ROAD.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_RESTRICTION                                        */
/*==============================================================*/
create table RD_RESTRICTION  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   RESTRIC_INFO         VARCHAR2(64),
   KG_FLAG              NUMBER(1)                      default 0 not null
       check (KG_FLAG in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_RESTRICTION primary key (PID),
   constraint RDRESTRICT_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDRESTRICTION_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_RESTRICTION is
'��¼���޵Ľ����ߵ��ϵ,�Լ��Ӹ��ߵ�������˳�Link�ϵ�����������Ϣ';

comment on column RD_RESTRICTION.PID is
'����';

comment on column RD_RESTRICTION.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_RESTRICTION.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_RESTRICTION.RESTRIC_INFO is
'��¼�ӽ���LINK�������˳�LINK�Ľ�����Ϣ
(1)��һ������Ӣ�İ�����ƴ��빹��,������RD_RESTRICTION_DETAILһ��,��:"1"��ʾ����
(2)��Ͻ�����Ӣ�İ��","�ָ�,��"1,2"��ʾ"��ֱ�ͽ���"
(3)Ĭ�ϱ�ʾʵ�ʽ���,��������۽��޻�δ��֤,�򽫾������ƴ�����Ӣ�İ��"[]"������,��"[2]",��ʾ���۽���';

comment on column RD_RESTRICTION.KG_FLAG is
'��ֽ�����Ϣ��Kר��,Gר��,KG���õı�־';

comment on column RD_RESTRICTION.U_RECORD is
'�������±�ʶ';

comment on column RD_RESTRICTION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_RESTRICTION_DETAIL                                 */
/*==============================================================*/
create table RD_RESTRICTION_DETAIL  (
   DETAIL_ID            NUMBER(10)                      not null,
   RESTRIC_PID          NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   FLAG                 NUMBER(1)                      default 2 not null
       check (FLAG in (0,1,2)),
   RESTRIC_INFO         NUMBER(1)                      default 0 not null
       check (RESTRIC_INFO in (0,1,2,3,4)),
   TYPE                 NUMBER(1)                      default 1 not null
       check (TYPE in (0,1,2)),
   RELATIONSHIP_TYPE    NUMBER(1)                      default 1 not null
       check (RELATIONSHIP_TYPE in (1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_RESTRICTION_DETAIL primary key (DETAIL_ID),
   constraint RDRESTRIC_DETAIL foreign key (RESTRIC_PID)
         references RD_RESTRICTION (PID),
   constraint RDRESTRICTDETAIL_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_RESTRICTION_DETAIL is
'��¼��ǰ�˳�Link�Ͼ������������,������Ϣ(��ֱ,�����)';

comment on column RD_RESTRICTION_DETAIL.DETAIL_ID is
'����';

comment on column RD_RESTRICTION_DETAIL.RESTRIC_PID is
'���,����"RD_RESTRICTION"';

comment on column RD_RESTRICTION_DETAIL.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_RESTRICTION_DETAIL.FLAG is
'δ��֤,ʵ�ؽ���,���۽���';

comment on column RD_RESTRICTION_DETAIL.RESTRIC_INFO is
'[180U]��¼��ǰ�˳�Link�ϵĽ�ֱ,����,����,�������Ϣ';

comment on column RD_RESTRICTION_DETAIL.TYPE is
'��ֹ����,ʱ��ν�ֹ(����Link���˳�Link��ʱ����ڽ�ֹ,����ʱ���ͨ��)';

comment on column RD_RESTRICTION_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column RD_RESTRICTION_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_RESTRICTION_CONDITION                              */
/*==============================================================*/
create table RD_RESTRICTION_CONDITION  (
   DETAIL_ID            NUMBER(10)                      not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   RES_TRAILER          NUMBER(1)                      default 0 not null
       check (RES_TRAILER in (0,1)),
   RES_WEIGH            NUMBER(5,2)                    default 0 not null,
   RES_AXLE_LOAD        NUMBER(5,2)                    default 0 not null,
   RES_AXLE_COUNT       NUMBER(2)                      default 0 not null,
   RES_OUT              NUMBER(1)                      default 0 not null
       check (RES_OUT in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDRESTRICCONDITION_DETAIL foreign key (DETAIL_ID)
         references RD_RESTRICTION_DETAIL (DETAIL_ID)
);

comment on table RD_RESTRICTION_CONDITION is
'��¼��ǰ�˳�Link�ϵ�ʱ��κͳ���������Ϣ';

comment on column RD_RESTRICTION_CONDITION.DETAIL_ID is
'���,����"RD_RESTRICTION_DETAIL"';

comment on column RD_RESTRICTION_CONDITION.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_RESTRICTION_CONDITION.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_RESTRICTION_CONDITION.RES_WEIGH is
'[190A]��λ:��,0��ʾ��';

comment on column RD_RESTRICTION_CONDITION.U_RECORD is
'�������±�ʶ';

comment on column RD_RESTRICTION_CONDITION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_RESTRICTION_VIA                                    */
/*==============================================================*/
create table RD_RESTRICTION_VIA  (
   DETAIL_ID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDRESTRICTION_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDRESTRICVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_RESTRICTION_DETAIL (DETAIL_ID)
);

comment on table RD_RESTRICTION_VIA is
'(1)���ͬһ����Link���˳�Link֮��Ķ��龭��Link,���Ҿ���Link����������Link���˳�Link
(1)NaviMap��ҵ��,��������˳�Linkֱ����ͬһ·�ڹҽ�ʱ,����������Link;����(�����߹�ϵ),��Ҫ��������Link';

comment on column RD_RESTRICTION_VIA.DETAIL_ID is
'���,����"RD_RESTRICTION_DETAIL"';

comment on column RD_RESTRICTION_VIA.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_RESTRICTION_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_RESTRICTION_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_RESTRICTION_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_RESTRICTION_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_ROAD_LINK                                          */
/*==============================================================*/
create table RD_ROAD_LINK  (
   PID                  NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDROAD_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDROAD_LINKS foreign key (PID)
         references RD_ROAD (PID)
);

comment on column RD_ROAD_LINK.PID is
'���,����"RD_ROAD"';

comment on column RD_ROAD_LINK.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_ROAD_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_ROAD_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_ROAD_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SAMELINK                                           */
/*==============================================================*/
create table RD_SAMELINK  (
   GROUP_ID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SAMELINK primary key (GROUP_ID)
);

comment on column RD_SAMELINK.GROUP_ID is
'����';

comment on column RD_SAMELINK.U_RECORD is
'�������±�ʶ';

comment on column RD_SAMELINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SAMELINK_PART                                      */
/*==============================================================*/
create table RD_SAMELINK_PART  (
   GROUP_ID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSAMELINK_PARTS foreign key (GROUP_ID)
         references RD_SAMELINK (GROUP_ID)
);

comment on table RD_SAMELINK_PART is
'��¼ͬһLink��ϵ�����Link��Ϣ';

comment on column RD_SAMELINK_PART.GROUP_ID is
'���,����"RD_SAMELINK"';

comment on column RD_SAMELINK_PART.LINK_PID is
'�ο�"RD_LINK","AD_LINK"��';

comment on column RD_SAMELINK_PART.TABLE_NAME is
'��¼LINK���ڵ���ݱ�,���·LINK=20��������LINK=40ΪͬһLINKʱ,��ݱ���ֱ�Ϊ"RD_LINK"��"AD_LINK"';

comment on column RD_SAMELINK_PART.U_RECORD is
'�������±�ʶ';

comment on column RD_SAMELINK_PART.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SAMENODE                                           */
/*==============================================================*/
create table RD_SAMENODE  (
   GROUP_ID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SAMENODE primary key (GROUP_ID)
);

comment on column RD_SAMENODE.GROUP_ID is
'����';

comment on column RD_SAMENODE.U_RECORD is
'�������±�ʶ';

comment on column RD_SAMENODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SAMENODE_PART                                      */
/*==============================================================*/
create table RD_SAMENODE_PART  (
   GROUP_ID             NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                     default 0 not null,
   TABLE_NAME           VARCHAR2(64),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSAMENODE_PARTS foreign key (GROUP_ID)
         references RD_SAMENODE (GROUP_ID)
);

comment on table RD_SAMENODE_PART is
'��¼ͬһNode��ϵ�����Node��Ϣ';

comment on column RD_SAMENODE_PART.GROUP_ID is
'���,����"RD_SAMENODE"';

comment on column RD_SAMENODE_PART.NODE_PID is
'�ο�"RD_NODE","AD_NODE"��';

comment on column RD_SAMENODE_PART.TABLE_NAME is
'��¼NODE���ڵ���ݱ�,���·NODE=20��������NODE=40ΪͬһNODEʱ,��ݱ���ֱ�Ϊ"RD_NODE"��"AD_NODE"';

comment on column RD_SAMENODE_PART.U_RECORD is
'�������±�ʶ';

comment on column RD_SAMENODE_PART.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SE                                                 */
/*==============================================================*/
create table RD_SE  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SE primary key (PID),
   constraint RDSE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDSE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDSE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_SE is
'NaviMap����ʱ����Link���˳�Link�������';

comment on column RD_SE.PID is
'����';

comment on column RD_SE.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_SE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_SE.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_SE.U_RECORD is
'�������±�ʶ';

comment on column RD_SE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SERIESBRANCH                                       */
/*==============================================================*/
create table RD_SERIESBRANCH  (
   BRANCH_PID           NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   VOICE_DIR            NUMBER(1)                      default 0 not null
       check (VOICE_DIR in (0,2,5)),
   PATTERN_CODE         VARCHAR2(10),
   ARROW_CODE           VARCHAR2(10),
   ARROW_FLAG           NUMBER(2)                      default 0 not null
       check (ARROW_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSERIESBRANCH foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID)
);

comment on table RD_SERIESBRANCH is
'������������ߵ��ϵ(�����߹�ϵ)�ͷ���ģʽͼ��Ϣ
';

comment on column RD_SERIESBRANCH.BRANCH_PID is
'���,����"RD_BRANCH"';

comment on column RD_SERIESBRANCH.VOICE_DIR is
'��,��,��';

comment on column RD_SERIESBRANCH.PATTERN_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME",��:8a430211';

comment on column RD_SERIESBRANCH.ARROW_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME",��:0a24030a';

comment on column RD_SERIESBRANCH.ARROW_FLAG is
'[180A]';

comment on column RD_SERIESBRANCH.U_RECORD is
'�������±�ʶ';

comment on column RD_SERIESBRANCH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SIGNASREAL primary key (SIGNBOARD_ID),
   constraint RDBRANCH_SIGNASREAL foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID)
);

comment on table RD_SIGNASREAL is
'[170]';

comment on column RD_SIGNASREAL.SIGNBOARD_ID is
'����';

comment on column RD_SIGNASREAL.BRANCH_PID is
'���,����"RD_BRANCH"';

comment on column RD_SIGNASREAL.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNASREAL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SIGNBOARD                                          */
/*==============================================================*/
create table RD_SIGNBOARD  (
   SIGNBOARD_ID         NUMBER(10)                      not null,
   BRANCH_PID           NUMBER(10)                      not null,
   ARROW_CODE           VARCHAR2(16),
   BACKIMAGE_CODE       VARCHAR2(16),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SIGNBOARD primary key (SIGNBOARD_ID),
   constraint RDBRANCH_SIGNBOARD foreign key (BRANCH_PID)
         references RD_BRANCH (BRANCH_PID)
);

comment on column RD_SIGNBOARD.SIGNBOARD_ID is
'����';

comment on column RD_SIGNBOARD.BRANCH_PID is
'���,����"RD_BRANCH"';

comment on column RD_SIGNBOARD.ARROW_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME"';

comment on column RD_SIGNBOARD.BACKIMAGE_CODE is
'ͬ��ͷͼ����,��Ϊ11 λ����';

comment on column RD_SIGNBOARD.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNBOARD.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SIGNBOARD_NAME                                     */
/*==============================================================*/
create table RD_SIGNBOARD_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   SIGNBOARD_ID         NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   NAME_CLASS           NUMBER(1)                      default 0 not null
       check (NAME_CLASS in (0,1)),
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   CODE_TYPE            NUMBER(2)                      default 0 not null
       check (CODE_TYPE in (0,1,2,3,4,5,6,7,8,9,10)),
   NAME                 VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   VOICE_FILE           VARCHAR2(100),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SIGNBOARD_NAME primary key (NAME_ID),
   constraint RDSIGNBOARD_NAME foreign key (SIGNBOARD_ID)
         references RD_SIGNBOARD (SIGNBOARD_ID)
);

comment on column RD_SIGNBOARD_NAME.NAME_ID is
'[170]����';

comment on column RD_SIGNBOARD_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_SIGNBOARD_NAME.SIGNBOARD_ID is
'���,����"RD_SIGNBOARD"';

comment on column RD_SIGNBOARD_NAME.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_SIGNBOARD_NAME.NAME_CLASS is
'����,����';

comment on column RD_SIGNBOARD_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_SIGNBOARD_NAME.CODE_TYPE is
'��ͨ��·��,��ʩ��,���ٵ�·���';

comment on column RD_SIGNBOARD_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_SIGNBOARD_NAME.VOICE_FILE is
'[170]�ο�"AU_MULTIMEDIA"��"NAME"';

comment on column RD_SIGNBOARD_NAME.SRC_FLAG is
'[170]�ֽ�ָӢ������Դ';

comment on column RD_SIGNBOARD_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNBOARD_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SIGNBOARD_NAME_TONE                                */
/*==============================================================*/
create table RD_SIGNBOARD_NAME_TONE  (
   NAME_ID              NUMBER(10)                      not null,
   TONE_A               VARCHAR2(400),
   TONE_B               VARCHAR2(400),
   LH_A                 VARCHAR2(400),
   LH_B                 VARCHAR2(400),
   JYUTP                VARCHAR2(400),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSIGNBOARDNAME_TONE foreign key (NAME_ID)
         references RD_SIGNBOARD_NAME (NAME_ID)
);

comment on table RD_SIGNBOARD_NAME_TONE is
'[170]';

comment on column RD_SIGNBOARD_NAME_TONE.NAME_ID is
'���,����"RD_SIGNBOARD_NAME"';

comment on column RD_SIGNBOARD_NAME_TONE.TONE_A is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column RD_SIGNBOARD_NAME_TONE.TONE_B is
'��������е����ֽ�ת��ƴ��';

comment on column RD_SIGNBOARD_NAME_TONE.LH_A is
'��Ӧ�����ƴ��1,ת��LH+';

comment on column RD_SIGNBOARD_NAME_TONE.LH_B is
'��Ӧ�����ƴ��2,ת��LH+';

comment on column RD_SIGNBOARD_NAME_TONE.JYUTP is
'������ͨ��ʱ���ֶ�Ϊ��ֵ';

comment on column RD_SIGNBOARD_NAME_TONE.MEMO is
'������ƶ�Ӧ�Ĵ����ƴ��(ĿǰΪ����ƴ��������ƴ��),���ֺ���ĸ��ת,��������Ϊ׼';

comment on column RD_SIGNBOARD_NAME_TONE.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNBOARD_NAME_TONE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SIGNPOST                                           */
/*==============================================================*/
create table RD_SIGNPOST  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   TYPE_CODE            VARCHAR2(5),
   ANGLE                NUMBER(5,2)                    default 0 not null,
   POSITION             NUMBER(2)                      default 0 not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   DESCRIPT             VARCHAR2(100),
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SIGNPOST primary key (PID)
);

comment on table RD_SIGNPOST is
'��¼���Ʊ���,��λ,�����.���������ƹ���ʱ,��¼�����Ƶ�PID�������Ϣ,ͬʱ��¼�����Ƶ���Ϣ';

comment on column RD_SIGNPOST.PID is
'����';

comment on column RD_SIGNPOST.LINK_PID is
'�ο�"RD_LINK"';

comment on column RD_SIGNPOST.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column RD_SIGNPOST.TYPE_CODE is
'�ο�"RD_SIGNPOST_CODE"';

comment on column RD_SIGNPOST.ANGLE is
'���ƿ���������ļн�,��λ:��,ֵ��Χ:0.00~360.00';

comment on column RD_SIGNPOST.POSITION is
'[172U]����3bit��ʾ,���ҵ�������Ϊ0~2bit,ÿbit��ʾһ��λ��(����),��ֵΪ0/1�ֱ��ʾ��/��,��: 101��ʾ�����
��0bit:��(Left)
��1bit:��(Right)
��2bit:��(Overhead)
�������bitλ��Ϊ0,��ʾδ����';

comment on column RD_SIGNPOST.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_SIGNPOST.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_SIGNPOST.DESCRIPT is
'��¼�ֳ������н���˵��������';

comment on column RD_SIGNPOST.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNPOST.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SIGNPOST_LINK                                      */
/*==============================================================*/
create table RD_SIGNPOST_LINK  (
   SIGNPOST_PID         NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSIGNPOST_CONTROLLINK foreign key (SIGNPOST_PID)
         references RD_SIGNPOST (PID)
);

comment on table RD_SIGNPOST_LINK is
'
';

comment on column RD_SIGNPOST_LINK.SIGNPOST_PID is
'���,����"RD_SIGNPOST"';

comment on column RD_SIGNPOST_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNPOST_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SIGNPOST_PHOTO                                     */
/*==============================================================*/
create table RD_SIGNPOST_PHOTO  (
   SIGNPOST_PID         NUMBER(10)                      not null,
   PHOTO_ID             NUMBER(10)                     default 0 not null,
   STATUS               VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSIGNPOST_PHOTOES foreign key (SIGNPOST_PID)
         references RD_SIGNPOST (PID)
);

comment on table RD_SIGNPOST_PHOTO is
'GDB��������ҵ��Ƭ�ɹ�֮��Ĺ�ϵ';

comment on column RD_SIGNPOST_PHOTO.SIGNPOST_PID is
'���,����"RD_SIGNPOST"';

comment on column RD_SIGNPOST_PHOTO.PHOTO_ID is
'�ο�"AU_PHOTO"';

comment on column RD_SIGNPOST_PHOTO.STATUS is
'��¼�Ƿ�ȷ��';

comment on column RD_SIGNPOST_PHOTO.U_RECORD is
'�������±�ʶ';

comment on column RD_SIGNPOST_PHOTO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SLOPE                                              */
/*==============================================================*/
create table RD_SLOPE  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   TYPE                 NUMBER(1)                      default 1 not null
       check (TYPE in (0,1,2,3)),
   ANGLE                NUMBER(2)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SLOPE primary key (PID),
   constraint RDSLOPE_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDSLOPE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_SLOPE.PID is
'����';

comment on column RD_SLOPE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_SLOPE.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_SLOPE.TYPE is
'ˮƽ,����,����';

comment on column RD_SLOPE.ANGLE is
'��λ:��,ֵ��Χ:0~90,Ĭ��Ϊ0';

comment on column RD_SLOPE.U_RECORD is
'�������±�ʶ';

comment on column RD_SLOPE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SLOPE_VIA                                          */
/*==============================================================*/
create table RD_SLOPE_VIA  (
   SLOPE_PID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDSLOPE_VIA_LINK foreign key (SLOPE_PID)
         references RD_SLOPE (PID),
   constraint RDSLOPE_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_SLOPE_VIA is
'[1901]';

comment on column RD_SLOPE_VIA.SLOPE_PID is
'���,����"RD_SLOPE"';

comment on column RD_SLOPE_VIA.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_SLOPE_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_SLOPE_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_SLOPE_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

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
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SPEEDBUMP primary key (BUMP_PID),
   constraint FK_RDSPEEDBUMP foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint FK_RDSPEEDBUMP_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_SPEEDBUMP.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_SPEEDBUMP.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_SPEEDBUMP.RESERVED is
'�ṹԤ��';

comment on column RD_SPEEDBUMP.U_RECORD is
'�������±�ʶ';

comment on column RD_SPEEDBUMP.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SPEEDLIMIT                                         */
/*==============================================================*/
create table RD_SPEEDLIMIT  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)),
   SPEED_VALUE          NUMBER(4)                      default 0 not null,
   SPEED_TYPE           NUMBER(1)                      default 0 not null
       check (SPEED_TYPE in (0,1 ,3 ,4)),
   TOLLGATE_FLAG        NUMBER(1)                      default 0 not null
       check (TOLLGATE_FLAG in (0,1)),
   SPEED_DEPENDENT      NUMBER(2)                      default 0 not null
       check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  ,13,14,15,16,17,18)),
   SPEED_FLAG           NUMBER(1)                      default 0 not null
       check (SPEED_FLAG in (0,1)),
   LIMIT_SRC            NUMBER(2)                      default 1 not null
       check (LIMIT_SRC in (0,1,2,3,4,5,6,7,8,9)),
   TIME_DOMAIN          VARCHAR2(1000),
   CAPTURE_FLAG         NUMBER(1)                      default 0 not null
       check (CAPTURE_FLAG in (0,1,2)),
   DESCRIPT             VARCHAR2(100),
   MESH_ID              NUMBER(6)                      default 0 not null,
   STATUS               NUMBER(1)                      default 7 not null
       check (STATUS in (0,1,2,3,4,5,6,7)),
   CK_STATUS            NUMBER(2)                      default 6 not null
       check (CK_STATUS in (0,1,2,3,4,5,6)),
   ADJA_FLAG            NUMBER(2)                      default 0 not null
       check (ADJA_FLAG in (0,1,2)),
   REC_STATUS_IN        NUMBER(1)                      default 0 not null
       check (REC_STATUS_IN in (0,2,3)),
   REC_STATUS_OUT       NUMBER(1)                      default 0 not null
       check (REC_STATUS_OUT in (0,1,2,3)),
   TIME_DESCRIPT        VARCHAR2(1000),
   GEOMETRY             SDO_GEOMETRY,
   LANE_SPEED_VALUE     VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SPEEDLIMIT primary key (PID)
);

comment on table RD_SPEEDLIMIT is
'(1)ͼ���ڵ�"�������link"��ϵ
(2)���ٹ�ϵ����ͼ��,����ֵ�ڹ�ϵ�Ϻ�Link���������зֱ��¼
(2)����¼"����"��"������"֮��ĳɶԹ�ϵ';

comment on column RD_SPEEDLIMIT.PID is
'����';

comment on column RD_SPEEDLIMIT.LINK_PID is
'�ο�"RD_LINK"';

comment on column RD_SPEEDLIMIT.SPEED_VALUE is
'��¼�������ֵ,ֵ��Χ:1~9999,��λ:����/ʱ,Ӧ��ʱ�����10';

comment on column RD_SPEEDLIMIT.SPEED_TYPE is
'[170][172U][190A]';

comment on column RD_SPEEDLIMIT.SPEED_DEPENDENT is
'[170][172U]';

comment on column RD_SPEEDLIMIT.SPEED_FLAG is
'���ٿ�ʼ�����ٽ��';

comment on column RD_SPEEDLIMIT.LIMIT_SRC is
'��¼������Ϣ����Դ,������ʶ,���ٱ�ʶ,��������,�������ٵ�';

comment on column RD_SPEEDLIMIT.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_SPEEDLIMIT.CAPTURE_FLAG is
'�����жϻ��ֳ��ɼ�';

comment on column RD_SPEEDLIMIT.U_RECORD is
'�������±�ʶ';

comment on column RD_SPEEDLIMIT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_SPEEDLIMIT_TRUCK                                   */
/*==============================================================*/
create table RD_SPEEDLIMIT_TRUCK  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                     default 0 not null,
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,2,3)),
   SPEED_VALUE          NUMBER(4)                      default 0 not null,
   SPEED_FLAG           NUMBER(1)                      default 0 not null
       check (SPEED_FLAG in (0,1)),
   CAPTURE_FLAG         NUMBER(1)                      default 0 not null
       check (CAPTURE_FLAG in (0,1,2)),
   DESCRIPT             VARCHAR2(100),
   MESH_ID              NUMBER(6)                      default 0 not null,
   STATUS               NUMBER(1)                      default 7 not null
       check (STATUS in (0,1,2,3,4,5,6,7)),
   CK_STATUS            NUMBER(2)                      default 6 not null
       check (CK_STATUS in (0,1,2,3,4,5,6)),
   ADJA_FLAG            NUMBER(2)                      default 0 not null
       check (ADJA_FLAG in (0,1,2)),
   REC_STATUS_IN        NUMBER(1)                      default 0 not null
       check (REC_STATUS_IN in (0,2,3)),
   REC_STATUS_OUT       NUMBER(1)                      default 0 not null
       check (REC_STATUS_OUT in (0,1,2,3)),
   GEOMETRY             SDO_GEOMETRY,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_SPEEDLIMIT_TRUCK primary key (PID)
);

comment on column RD_SPEEDLIMIT_TRUCK.PID is
'����';

comment on column RD_SPEEDLIMIT_TRUCK.LINK_PID is
'�ο�"RD_LINK"';

comment on column RD_SPEEDLIMIT_TRUCK.SPEED_VALUE is
'��¼�������ֵ,ֵ��Χ:1~9999,��λ:����/ʱ,Ӧ��ʱ�����10';

comment on column RD_SPEEDLIMIT_TRUCK.SPEED_FLAG is
'���ٿ�ʼ�����ٽ��';

comment on column RD_SPEEDLIMIT_TRUCK.CAPTURE_FLAG is
'�����жϻ��ֳ��ɼ�';

comment on column RD_SPEEDLIMIT_TRUCK.U_RECORD is
'�������±�ʶ';

comment on column RD_SPEEDLIMIT_TRUCK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TMCLOCATION                                        */
/*==============================================================*/
create table RD_TMCLOCATION  (
   GROUP_ID             NUMBER(10)                      not null,
   TMC_ID               NUMBER(10)                     default 0 not null,
   LOCTABLE_ID          NUMBER(2)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_TMCLOCATION primary key (GROUP_ID)
);

comment on column RD_TMCLOCATION.GROUP_ID is
'����';

comment on column RD_TMCLOCATION.TMC_ID is
'[170]�ο�"TMC_POINT","TMC_LINE","TMC_AREA"';

comment on column RD_TMCLOCATION.U_RECORD is
'�������±�ʶ';

comment on column RD_TMCLOCATION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TMCLOCATION_LINK                                   */
/*==============================================================*/
create table RD_TMCLOCATION_LINK  (
   GROUP_ID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   LOC_DIRECT           NUMBER(1)                      default 0 not null
       check (LOC_DIRECT in (0,1,2,3,4)),
   DIRECT               NUMBER(1)                      default 0 not null
       check (DIRECT in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDTMCLOCATION_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDTMCLOCATION_LINKS foreign key (GROUP_ID)
         references RD_TMCLOCATION (GROUP_ID)
);

comment on column RD_TMCLOCATION_LINK.GROUP_ID is
'���,����"RD_TMCLOCATION"';

comment on column RD_TMCLOCATION_LINK.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_TMCLOCATION_LINK.DIRECT is
'TMCλ�÷������·LINK����Ĺ�ϵ';

comment on column RD_TMCLOCATION_LINK.U_RECORD is
'�������±�ʶ';

comment on column RD_TMCLOCATION_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TOLLGATE                                           */
/*==============================================================*/
create table RD_TOLLGATE  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   TYPE                 NUMBER(2)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5,6,7,8,9,10)),
   PASSAGE_NUM          NUMBER(2)                      default 0 not null,
   ETC_FIGURE_CODE      VARCHAR2(8),
   HW_NAME              VARCHAR2(1000),
   FEE_TYPE             NUMBER(1)                      default 0 not null
       check (FEE_TYPE in (0,1,2)),
   FEE_STD              NUMBER(5,2)                    default 0 not null,
   SYSTEM_ID            NUMBER(6)                      default 0 not null,
   LOCATION_FLAG        NUMBER(1)                      default 0 not null
       check (LOCATION_FLAG in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_TOLLGATE primary key (PID),
   constraint RDTOLLGATE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDTOLLGATE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDTOLLGATE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on table RD_TOLLGATE is
'(1)����շ�վ���շ����ͺ�ETC������
(2)ע��:����LINK���˳�LINK�������';

comment on column RD_TOLLGATE.PID is
'����';

comment on column RD_TOLLGATE.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_TOLLGATE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_TOLLGATE.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_TOLLGATE.TYPE is
'�շ�,�쿨������';

comment on column RD_TOLLGATE.PASSAGE_NUM is
'����ETCͨ�������ڵ�ͨ������';

comment on column RD_TOLLGATE.ETC_FIGURE_CODE is
'�ο�"AU_MULTIMEDIA"��"NAME"';

comment on column RD_TOLLGATE.HW_NAME is
'��¼�շ�վ���ڵĸ������';

comment on column RD_TOLLGATE.FEE_TYPE is
'0 ������շ�
1 �̶��շ�
2 δ����';

comment on column RD_TOLLGATE.FEE_STD is
'��λ:Ԫ/�λ�Ԫ/����';

comment on column RD_TOLLGATE.U_RECORD is
'�������±�ʶ';

comment on column RD_TOLLGATE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TOLLGATE_COST                                      */
/*==============================================================*/
create table RD_TOLLGATE_COST  (
   TOLLCOST_ID          NUMBER(10)                      not null,
   IN_TOLLGATE          NUMBER(10)                     default 0 not null,
   OUT_TOLLGATE         NUMBER(10)                     default 0 not null,
   FEE                  NUMBER(10,2)                   default 0 not null,
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1)),
   CASH                 VARCHAR2(3)                    default 'CNY' not null
       check (CASH in ('CNY','HKD','MOP')),
   VEHICLE_CLASS        NUMBER(1)                      default 1 not null
       check (VEHICLE_CLASS in (1,2,3,4,5)),
   DISTANCE             NUMBER(10,4)                   default 0 not null,
   SYSTEM_ID            NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_TOLLGATE_COST primary key (TOLLCOST_ID)
);

comment on table RD_TOLLGATE_COST is
'[180]��IDBͨ��RD_TOLLGATE_MAPPING��RD_TOLLGATE_FEE���';

comment on column RD_TOLLGATE_COST.TOLLCOST_ID is
'[180U]����,ȡ��"RD_TOLLGATE_FEE"';

comment on column RD_TOLLGATE_COST.IN_TOLLGATE is
'�ο�"RD_TOLLGATE"';

comment on column RD_TOLLGATE_COST.OUT_TOLLGATE is
'�ο�"RD_TOLLGATE"';

comment on column RD_TOLLGATE_COST.VEHICLE_CLASS is
'���ս�ͨ���շѹ�·����ͨ�зѳ��ͷ����׼,��Ϊ1~5 ��,';

comment on column RD_TOLLGATE_COST.U_RECORD is
'�������±�ʶ';

comment on column RD_TOLLGATE_COST.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TOLLGATE_FEE                                       */
/*==============================================================*/
create table RD_TOLLGATE_FEE  (
   TOLLCOST_ID          NUMBER(10)                      not null,
   S_TOLL_OLD           VARCHAR2(1000),
   E_TOLL_OLD           VARCHAR2(1000),
   S_TOLL_NEW           VARCHAR2(1000),
   E_TOLL_NEW           VARCHAR2(1000),
   S_MAPPINGID          NUMBER(10)                     default 0 not null,
   E_MAPPINGID          NUMBER(10)                     default 0 not null,
   CASH                 VARCHAR2(3)                    default 'CNY' not null
       check (CASH in ('CNY','HKD','MOP')),
   FEE                  NUMBER(10,2)                   default 0 not null,
   VEHICLE_CLASS        NUMBER(1)                      default 1 not null
       check (VEHICLE_CLASS in (1,2,3,4,5)),
   DISTANCE             NUMBER(10,4)                   default 0 not null,
   FLAG                 VARCHAR2(100),
   SYSTEM_ID            NUMBER(6),
   VER_INFO             VARCHAR2(100),
   MEMO                 VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_FEE primary key (TOLLCOST_ID)
);

comment on table RD_TOLLGATE_FEE is
'[180]���������߱༭ƽ̨';

comment on column RD_TOLLGATE_FEE.TOLLCOST_ID is
'[180A]����';

comment on column RD_TOLLGATE_FEE.S_TOLL_OLD is
'ȡ��"����ݱ�"';

comment on column RD_TOLLGATE_FEE.E_TOLL_OLD is
'ȡ��"����ݱ�"';

comment on column RD_TOLLGATE_FEE.S_MAPPINGID is
'�ο�"RD_TOLLGATE_MAPPING"';

comment on column RD_TOLLGATE_FEE.E_MAPPINGID is
'�ο�"RD_TOLLGATE_MAPPING"';

comment on column RD_TOLLGATE_FEE.VEHICLE_CLASS is
'���ս�ͨ���շѹ�·����ͨ�зѳ��ͷ����׼,��Ϊ1~5 ��,';

/*==============================================================*/
/* Table: RD_TOLLGATE_MAPPING                                   */
/*==============================================================*/
create table RD_TOLLGATE_MAPPING  (
   MAPPING_ID           NUMBER(10)                      not null,
   SE_TOLL_OLD          VARCHAR2(1000),
   SE_TOLL_NEW          VARCHAR2(1000),
   GDB_TOLL_PID         NUMBER(10)                     default 0 not null,
   GDB_TOLL_NAME        VARCHAR2(1000),
   GDB_TOLL_NODEID      NUMBER(10)                     default 0 not null,
   FLAG                 VARCHAR2(100),
   SYSTEM_ID            NUMBER(6),
   VER_INFO             VARCHAR2(100),
   MEMO                 VARCHAR2(1000),
   constraint PK_RD_TOLLGATE_MAPPING primary key (MAPPING_ID)
);

comment on table RD_TOLLGATE_MAPPING is
'[180]���������߱༭ƽ̨';

comment on column RD_TOLLGATE_MAPPING.MAPPING_ID is
'����';

comment on column RD_TOLLGATE_MAPPING.SE_TOLL_OLD is
'ȡ��"����ݱ�"';

comment on column RD_TOLLGATE_MAPPING.SE_TOLL_NEW is
'ԭʼ�����յ�+���(ϵͳ��1��ʼ���ε�����ţ�';

comment on column RD_TOLLGATE_MAPPING.GDB_TOLL_PID is
'�ο�"RD_TOLLGATE"';

comment on column RD_TOLLGATE_MAPPING.GDB_TOLL_NAME is
'�ο�"RD_TOLLGATE_NAME"';

comment on column RD_TOLLGATE_MAPPING.GDB_TOLL_NODEID is
'�ο�"RD_TOLLGATE"�е�"NODE_PID"';

/*==============================================================*/
/* Table: RD_TOLLGATE_NAME                                      */
/*==============================================================*/
create table RD_TOLLGATE_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_TOLLGATE_NAME primary key (NAME_ID),
   constraint RDTOLLGTE_NAME foreign key (PID)
         references RD_TOLLGATE (PID)
);

comment on column RD_TOLLGATE_NAME.NAME_ID is
'[170]����';

comment on column RD_TOLLGATE_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_TOLLGATE_NAME.PID is
'���,����"RD_TOLLGATE"';

comment on column RD_TOLLGATE_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_TOLLGATE_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_TOLLGATE_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_TOLLGATE_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TOLLGATE_PASSAGE                                   */
/*==============================================================*/
create table RD_TOLLGATE_PASSAGE  (
   PID                  NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   TOLL_FORM            NUMBER(10)                     default 0 not null,
   CARD_TYPE            NUMBER(1)                      default 0 not null
       check (CARD_TYPE in (0,1,2,3)),
   VEHICLE              NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDTOLLGATE_PASSAGE foreign key (PID)
         references RD_TOLLGATE (PID)
);

comment on table RD_TOLLGATE_PASSAGE is
'��¼ÿһ��ͨ�����շѷ�ʽ,�쿨��ʽ�ͳ�������';

comment on column RD_TOLLGATE_PASSAGE.PID is
'���,����"RD_TOLLGATE"';

comment on column RD_TOLLGATE_PASSAGE.SEQ_NUM is
'�շ�վ����Link�����ҵ�ͨ�����,���δ�1��ʼ�������';

comment on column RD_TOLLGATE_PASSAGE.TOLL_FORM is
'����8bit ��ʾ,���ҵ�������Ϊ0~7bit,ÿbit ��ʾһ�ַ�ʽ����(����),��ֵΪ0/1 �ֱ��ʾ��/��,��:00000110 ��ʾ�ֽ�����п�
��0bit:ETC
��1bit:�ֽ�
��2bit: ���п�(��ǿ�)
��3bit:���ÿ�
��4bit:IC ��
��5bit:Ԥ����
�������bit λ��Ϊ0,��ʾδ����
ע:���շѷ�ʽΪ"ETC"ʱ,��������������ʽ';

comment on column RD_TOLLGATE_PASSAGE.CARD_TYPE is
'ÿ��ͨ�����쿨��ʽ,��ETC,�˹�,�����';

comment on column RD_TOLLGATE_PASSAGE.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_TOLLGATE_PASSAGE.U_RECORD is
'�������±�ʶ';

comment on column RD_TOLLGATE_PASSAGE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_TRAFFICSIGNAL                                      */
/*==============================================================*/
create table RD_TRAFFICSIGNAL  (
   PID                  NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   LOCATION             NUMBER(2)                      default 0 not null,
   FLAG                 NUMBER(1)                      default 0 not null
       check (FLAG in (0,1,2)),
   TYPE                 NUMBER(1)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5)),
   KG_FLAG              NUMBER(1)                      default 0 not null
       check (KG_FLAG in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_TRAFFICSIGNAL primary key (PID),
   constraint RDSIGNAL_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDSIGNAL_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_TRAFFICSIGNAL.PID is
'����';

comment on column RD_TRAFFICSIGNAL.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_TRAFFICSIGNAL.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_TRAFFICSIGNAL.LOCATION is
'[172U]����3bit��ʾ,���ҵ�������Ϊ0~2bit,ÿbit��ʾһ��λ��(����),��ֵΪ0/1�ֱ��ʾ��/��,��: 101��ʾ�����
��0bit:��(Left)
��1bit:��(Right)
��2bit:��(Overhead)
�������bitλ��Ϊ0,��ʾδ����';

comment on column RD_TRAFFICSIGNAL.FLAG is
'�Ƿ����źŵƿ���';

comment on column RD_TRAFFICSIGNAL.TYPE is
'���źŵ�,�����źŵ�,����ָʾ�Ƶ�';

comment on column RD_TRAFFICSIGNAL.U_RECORD is
'�������±�ʶ';

comment on column RD_TRAFFICSIGNAL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VARIABLE_SPEED                                     */
/*==============================================================*/
create table RD_VARIABLE_SPEED  (
   VSPEED_PID           NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   LOCATION             NUMBER(2)                      default 0 not null,
   SPEED_VALUE          NUMBER(4)                      default 0 not null,
   SPEED_TYPE           NUMBER(1)                      default 0 not null
       check (SPEED_TYPE in (0,1 ,2 ,3 )),
   SPEED_DEPENDENT      NUMBER(2)                      default 0 not null
       check (SPEED_DEPENDENT in (0,1,2 ,3 ,6 ,10  ,11 ,12  )),
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_VARIABLE_SPEED primary key (VSPEED_PID),
   constraint RDVARIABLESPEED_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDVARIABLESPEED_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDVARIABLESPEED_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on table RD_VARIABLE_SPEED is
'[170]';

comment on column RD_VARIABLE_SPEED.VSPEED_PID is
'����';

comment on column RD_VARIABLE_SPEED.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_VARIABLE_SPEED.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_VARIABLE_SPEED.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_VARIABLE_SPEED.LOCATION is
'[172U]����3bit��ʾ,���ҵ�������Ϊ0~2bit,ÿbit��ʾһ��λ��(����),��ֵΪ0/1�ֱ��ʾ��/��,��: 101��ʾ�����
��0bit:��(Left)
��1bit:��(Right)
��2bit:��(Overhead)
�������bitλ��Ϊ0,��ʾδ����';

comment on column RD_VARIABLE_SPEED.SPEED_VALUE is
'��λ:����/ʱ,ֵ��: 1~9999';

comment on column RD_VARIABLE_SPEED.SPEED_TYPE is
'[170][172U]';

comment on column RD_VARIABLE_SPEED.SPEED_DEPENDENT is
'[170][172U]';

comment on column RD_VARIABLE_SPEED.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_VARIABLE_SPEED.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_VARIABLE_SPEED.U_RECORD is
'�������±�ʶ';

comment on column RD_VARIABLE_SPEED.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VARIABLE_SPEED_VIA                                 */
/*==============================================================*/
create table RD_VARIABLE_SPEED_VIA  (
   VSPEED_PID           NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDVARIABLESPEED_VIA foreign key (VSPEED_PID)
         references RD_VARIABLE_SPEED (VSPEED_PID),
   constraint RDVARIABLESPEED_VIALINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID)
);

comment on column RD_VARIABLE_SPEED_VIA.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_VARIABLE_SPEED_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_VARIABLE_SPEED_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_VARIABLE_SPEED_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VIRCONNECT                                         */
/*==============================================================*/
create table RD_VIRCONNECT  (
   PID                  NUMBER(10)                      not null,
   TYPE                 NUMBER(2)                      default 0 not null
       check (TYPE in (0,1,2,3,4,5,11,12,13,14,15,99)),
   OBSTACLE_FREE        NUMBER(1)                      default 0 not null
       check (OBSTACLE_FREE in (0,1,2)),
   FEE                  NUMBER(1)                      default 0 not null
       check (FEE in (0,1)),
   STREET_LIGHT         NUMBER(1)                      default 0 not null
       check (STREET_LIGHT in (0,1,2,3)),
   TIME_DOMAIN          VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_VIRCONNECT primary key (PID)
);

comment on column RD_VIRCONNECT.PID is
'����';

comment on column RD_VIRCONNECT.TYPE is
'ֱ��,����,������,��԰,�㳡,�ֵ���';

comment on column RD_VIRCONNECT.OBSTACLE_FREE is
'[180U]�Ƿ�������ϰ�ͨ��';

comment on column RD_VIRCONNECT.FEE is
'���,�շ�';

comment on column RD_VIRCONNECT.STREET_LIGHT is
'δ����,��,��,��Ӧ��';

comment on column RD_VIRCONNECT.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_VIRCONNECT.U_RECORD is
'�������±�ʶ';

comment on column RD_VIRCONNECT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VIRCONNECT_NAME                                    */
/*==============================================================*/
create table RD_VIRCONNECT_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   PID                  NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_VIRCONNECT_NAME primary key (NAME_ID),
   constraint RDVIRCONNECT_NAME foreign key (PID)
         references RD_VIRCONNECT (PID)
);

comment on column RD_VIRCONNECT_NAME.NAME_ID is
'[170]����';

comment on column RD_VIRCONNECT_NAME.NAME_GROUPID is
'[171U][170]��1��ʼ�������';

comment on column RD_VIRCONNECT_NAME.PID is
'���,����"RD_VIRCONNECT"';

comment on column RD_VIRCONNECT_NAME.LANG_CODE is
'��������,��������,Ӣ��,���ĵȶ�������';

comment on column RD_VIRCONNECT_NAME.PHONETIC is
'[171U]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column RD_VIRCONNECT_NAME.U_RECORD is
'�������±�ʶ';

comment on column RD_VIRCONNECT_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VIRCONNECT_TRANSIT                                 */
/*==============================================================*/
create table RD_VIRCONNECT_TRANSIT  (
   PID                  NUMBER(10)                      not null,
   FIR_NODE_PID         NUMBER(10)                      not null,
   SEN_NODE_PID         NUMBER(10)                      not null,
   TRANSIT              NUMBER(1)                      default 0 not null
       check (TRANSIT in (0,1,2,3)),
   SLOPE                NUMBER(1)                      default 0 not null
       check (SLOPE in (0,1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDVIRCONNCT_FIRNODE foreign key (FIR_NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDVIRCONNCT_SECNODE foreign key (SEN_NODE_PID)
         references RD_NODE (NODE_PID),
   constraint RDVIRCONNECT_NODES foreign key (PID)
         references RD_VIRCONNECT (PID)
);

comment on table RD_VIRCONNECT_TRANSIT is
'��¼������������������֮�����ͨ��ϵ';

comment on column RD_VIRCONNECT_TRANSIT.PID is
'���,����"RD_VIRCONNECT"';

comment on column RD_VIRCONNECT_TRANSIT.FIR_NODE_PID is
'���,����"RD_NODE"';

comment on column RD_VIRCONNECT_TRANSIT.SEN_NODE_PID is
'���,����"RD_NODE"';

comment on column RD_VIRCONNECT_TRANSIT.TRANSIT is
'�ڵ�һ���ڵ����˳�淽��򲻿�ͨ����Ϣ';

comment on column RD_VIRCONNECT_TRANSIT.SLOPE is
'����,����';

comment on column RD_VIRCONNECT_TRANSIT.U_RECORD is
'�������±�ʶ';

comment on column RD_VIRCONNECT_TRANSIT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VOICEGUIDE                                         */
/*==============================================================*/
create table RD_VOICEGUIDE  (
   PID                  NUMBER(10)                      not null,
   IN_LINK_PID          NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_VOICEGUIDE primary key (PID),
   constraint RDVOICE_INLINK foreign key (IN_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDVOICE_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_VOICEGUIDE.PID is
'����';

comment on column RD_VOICEGUIDE.IN_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_VOICEGUIDE.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_VOICEGUIDE.U_RECORD is
'�������±�ʶ';

comment on column RD_VOICEGUIDE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VOICEGUIDE_DETAIL                                  */
/*==============================================================*/
create table RD_VOICEGUIDE_DETAIL  (
   DETAIL_ID            NUMBER(10)                      not null,
   VOICEGUIDE_PID       NUMBER(10)                      not null,
   OUT_LINK_PID         NUMBER(10)                      not null,
   GUIDE_CODE           NUMBER(2)                      default 0 not null
       check (GUIDE_CODE in (0,1,2,4,6,7,8,10,12,16,19)),
   GUIDE_TYPE           NUMBER(1)                      default 0 not null
       check (GUIDE_TYPE in (0,1,2,3)),
   PROCESS_FLAG         NUMBER(1)                      default 1 not null
       check (PROCESS_FLAG in (0,1,2)),
   RELATIONSHIP_TYPE    NUMBER(1)                      default 1 not null
       check (RELATIONSHIP_TYPE in (1,2)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_VOICEGUIDE_DETAIL primary key (DETAIL_ID),
   constraint RDVOICE_OUTLINK foreign key (OUT_LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDVOICE_DETAIL foreign key (VOICEGUIDE_PID)
         references RD_VOICEGUIDE (PID)
);

comment on column RD_VOICEGUIDE_DETAIL.DETAIL_ID is
'����';

comment on column RD_VOICEGUIDE_DETAIL.VOICEGUIDE_PID is
'���,����"RD_VOICEGUIDE"';

comment on column RD_VOICEGUIDE_DETAIL.OUT_LINK_PID is
'���,����"RD_LINK"';

comment on column RD_VOICEGUIDE_DETAIL.GUIDE_CODE is
'��бǰ,��ת,�Һ�ת,��ͷ,���ת��������ʾ����';

comment on column RD_VOICEGUIDE_DETAIL.GUIDE_TYPE is
'ƽ��,�߼�,���µ���������';

comment on column RD_VOICEGUIDE_DETAIL.U_RECORD is
'�������±�ʶ';

comment on column RD_VOICEGUIDE_DETAIL.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_VOICEGUIDE_VIA                                     */
/*==============================================================*/
create table RD_VOICEGUIDE_VIA  (
   DETAIL_ID            NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   GROUP_ID             NUMBER(2)                      default 1 not null,
   SEQ_NUM              NUMBER(2)                      default 1 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RDVOICEGUIDEVIALINK_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDVOICEGUIDEVIALINK_DETAIL foreign key (DETAIL_ID)
         references RD_VOICEGUIDE_DETAIL (DETAIL_ID)
);

comment on table RD_VOICEGUIDE_VIA is
'(1)���ͬһ����Link���˳�Link֮��Ķ��龭��Link,���Ҿ���Link����������Link���˳�Link
(2)NaviMap��ҵ��,��������˳�Linkֱ����ͬһ·�ڹҽ�ʱ,����������Link;����(�����߹�ϵ),��Ҫ��������Link';

comment on column RD_VOICEGUIDE_VIA.DETAIL_ID is
'���,����"RD_VOICEGUIDE_DETAIL"';

comment on column RD_VOICEGUIDE_VIA.LINK_PID is
'���,����"RD_LINK",������˳�Link ����';

comment on column RD_VOICEGUIDE_VIA.GROUP_ID is
'��1��ʼ�������';

comment on column RD_VOICEGUIDE_VIA.SEQ_NUM is
'��1��ʼ�������';

comment on column RD_VOICEGUIDE_VIA.U_RECORD is
'�������±�ʶ';

comment on column RD_VOICEGUIDE_VIA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RD_WARNINGINFO                                        */
/*==============================================================*/
create table RD_WARNINGINFO  (
   PID                  NUMBER(10)                      not null,
   LINK_PID             NUMBER(10)                      not null,
   NODE_PID             NUMBER(10)                      not null,
   TYPE_CODE            VARCHAR2(5),
   VALID_DIS            NUMBER(5)                      default 0 not null,
   WARN_DIS             NUMBER(5)                      default 0 not null,
   TIME_DOMAIN          VARCHAR2(1000),
   VEHICLE              NUMBER(10)                     default 0 not null,
   DESCRIPT             VARCHAR2(100),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RD_WARNINGINFO primary key (PID),
   constraint RDWARNING_LINK foreign key (LINK_PID)
         references RD_LINK (LINK_PID),
   constraint RDWARNING_NODE foreign key (NODE_PID)
         references RD_NODE (NODE_PID)
);

comment on column RD_WARNINGINFO.PID is
'����';

comment on column RD_WARNINGINFO.LINK_PID is
'���,����"RD_LINK"';

comment on column RD_WARNINGINFO.NODE_PID is
'���,����"RD_NODE"';

comment on column RD_WARNINGINFO.TYPE_CODE is
'�ο�"RD_SIGNPOST_CODE"';

comment on column RD_WARNINGINFO.VALID_DIS is
'ʵ��Ԥ������,��λ:��';

comment on column RD_WARNINGINFO.WARN_DIS is
'��ǰԤ�����,Ŀǰֻ������·����,��λ:��';

comment on column RD_WARNINGINFO.TIME_DOMAIN is
'��ʽ�ο�"ʱ����"';

comment on column RD_WARNINGINFO.VEHICLE is
'��ʽ�ο�"��������"';

comment on column RD_WARNINGINFO.DESCRIPT is
'��¼�ֳ������е�˵������';

comment on column RD_WARNINGINFO.U_RECORD is
'�������±�ʶ';

comment on column RD_WARNINGINFO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RI_FEATURE                                            */
/*==============================================================*/
create table RI_FEATURE  (
   DATA_LOG_ID          VARCHAR2(32)                    not null,
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
'�������ID';

comment on column RI_FEATURE.VERSION_ID is
'�汾���';

comment on column RI_FEATURE.COMMITED_STATE_ID is
'�����ύ�󣬵�ǰ״ֵ̬';

comment on column RI_FEATURE.ID is
'������';

comment on column RI_FEATURE.TASK_ID is
'�������ƽ̨�����';

comment on column RI_FEATURE.OPERATE_ID is
'������';

comment on column RI_FEATURE.OPERATE_NAME is
'����LINK����������';

comment on column RI_FEATURE.INNER_ID is
'���ڱ��';

comment on column RI_FEATURE.TABLE_NAME is
'��ǰ����';

comment on column RI_FEATURE.OBJECT_NAME is
'��ǰ����Ķ�����';

comment on column RI_FEATURE.OBJECT_ID is
'��ǰ�����PID������Ϊ��';

comment on column RI_FEATURE.DML_TYPE is
'0-����1-�ġ�2-ɾ';

comment on column RI_FEATURE.PREVIOUS_CONTENT1 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT1 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.GEOMETRY is
'������ȡ��ҵ��Χ�ڵ�������ٲ��ڱ�ͼ���ڵ����';

comment on column RI_FEATURE.UUID is
'��¼ÿ����¼��Ψһ��ʾ';

comment on column RI_FEATURE.HIS_TYPE is
'1-������
2-ĸ�⳷����
3-���س�������
4-ĸ����������
5-��Ч����
6-���������';

comment on column RI_FEATURE.REF_ID is
'����������';

comment on column RI_FEATURE.CHANG_COL is
'�ļ����ֶα仯��';

comment on column RI_FEATURE.PREVIOUS_CONTENT2 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT3 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT4 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT5 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT6 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT7 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT8 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT9 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_CONTENT10 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT2 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT3 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT4 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT5 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT6 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT7 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT8 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT9 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.CURRENT_CONTENT10 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_FEATURE.PREVIOUS_GEOMETRY is
'�仯ǰ����';

comment on column RI_FEATURE.CURRENT_GEOMETRY is
'�仯�󼸺�';

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
create table RI_OPERATION  (
   OPERATE_LOG_ID       VARCHAR2(32)                    not null,
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
'��ҵ����ID';

comment on column RI_OPERATION.OPERATE_STATE_ID is
'�ύ״̬��';

comment on column RI_OPERATION.VERSION_ID is
'�汾��';

comment on column RI_OPERATION.ID is
'������';

comment on column RI_OPERATION.TASK_ID is
'�������ƽ̨�����';

comment on column RI_OPERATION.OPERATE_ID is
'������';

comment on column RI_OPERATION.OPERATE_NAME is
'����LINK����������';

comment on column RI_OPERATION.INNER_ID is
'���ڱ��';

comment on column RI_OPERATION.TABLE_NAME is
'��ǰ����';

comment on column RI_OPERATION.OBJECT_NAME is
'��ǰ����Ķ�����';

comment on column RI_OPERATION.OBJECT_ID is
'��ǰ�����PID������Ϊ��';

comment on column RI_OPERATION.DML_TYPE is
'0-����1-�ġ�2-ɾ';

comment on column RI_OPERATION.PREVIOUS_CONTENT1 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT1 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.GEOMETRY is
'������ȡ��ҵ��Χ�ڵ�������ٲ��ڱ�ͼ���ڵ����';

comment on column RI_OPERATION.UUID is
'��¼ÿ����¼��Ψһ��ʾ';

comment on column RI_OPERATION.HIS_TYPE is
'1-������
2-ĸ�⳷����
3-���س�������
4-ĸ����������
5-��Ч����
6-���������';

comment on column RI_OPERATION.REF_ID is
'����������';

comment on column RI_OPERATION.CHANG_COL is
'�ļ����ֶα仯��';

comment on column RI_OPERATION.PREVIOUS_CONTENT2 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT3 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT4 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT5 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT6 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT7 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT8 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT9 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.PREVIOUS_CONTENT10 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT2 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT3 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT4 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT5 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT6 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT7 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT8 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT9 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.CURRENT_CONTENT10 is
'XML��Ϊ����Ϊ�ա�Ϊ�����¼Ҫ���ֶ�δ��ǰ��ֵ��Ϊɾʱ��¼��ݵ�ȫ�ֶ�����';

comment on column RI_OPERATION.OPERATE_DESC is
'��������';

comment on column RI_OPERATION.OPERATION_RUNTIME is
'��������ʱ��';

comment on column RI_OPERATION.FEATURE_RID is
'�����ID';

comment on column RI_OPERATION.FEATURE_BLOB is
'��������Ʊ�ʾ';

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
create table RW_FEATURE  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RW_FEATURE primary key (FEATURE_PID)
);

comment on column RW_FEATURE.FEATURE_PID is
'����';

comment on column RW_FEATURE.U_RECORD is
'�������±�ʶ';

comment on column RW_FEATURE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_FEATURE_20W                                        */
/*==============================================================*/
create table RW_FEATURE_20W  (
   FEATURE_PID          NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RW_FEATURE_20W primary key (FEATURE_PID)
);

comment on column RW_FEATURE_20W.FEATURE_PID is
'����';

comment on column RW_FEATURE_20W.U_RECORD is
'�������±�ʶ';

comment on column RW_FEATURE_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_NODE                                               */
/*==============================================================*/
create table RW_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,2,3,4,5,6)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RW_NODE primary key (NODE_PID)
);

comment on column RW_NODE.NODE_PID is
'����';

comment on column RW_NODE.KIND is
'ƽ�潻����Link���Ա仯��';

comment on column RW_NODE.FORM is
'��·����,��,���,ͼ����,��·���ڵ�';

comment on column RW_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column RW_NODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column RW_NODE.U_RECORD is
'�������±�ʶ';

comment on column RW_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_LINK                                               */
/*==============================================================*/
create table RW_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2,3)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,2)),
   LENGTH               NUMBER(15,3)                   default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   COLOR                VARCHAR2(10),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RW_LINK primary key (LINK_PID),
   constraint RWLINK_ENODE foreign key (E_NODE_PID)
         references RW_NODE (NODE_PID),
   constraint RWLINK_SNODE foreign key (S_NODE_PID)
         references RW_NODE (NODE_PID)
);

comment on column RW_LINK.LINK_PID is
'����';

comment on column RW_LINK.FEATURE_PID is
'�ο�"RW_FEATURE"';

comment on column RW_LINK.S_NODE_PID is
'���,����"RW_NODE"';

comment on column RW_LINK.E_NODE_PID is
'���,����"RW_NODE"';

comment on column RW_LINK.KIND is
'��·,����,���/����';

comment on column RW_LINK.FORM is
'�Ż����';

comment on column RW_LINK.LENGTH is
'��λ:��';

comment on column RW_LINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column RW_LINK.SCALE is
'ע:���ֶν�����2.5 �����,20 ����ݲ���Ҫ';

comment on column RW_LINK.DETAIL_FLAG is
'ע:���ֶν�����2.5 �����,20 ����ݲ���Ҫ';

comment on column RW_LINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column RW_LINK.U_RECORD is
'�������±�ʶ';

comment on column RW_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_NODE_20W                                           */
/*==============================================================*/
create table RW_NODE_20W  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,2,3,4,5,6)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RW_NODE_20W primary key (NODE_PID)
);

comment on column RW_NODE_20W.NODE_PID is
'����';

comment on column RW_NODE_20W.KIND is
'ƽ�潻����Link���Ա仯��';

comment on column RW_NODE_20W.FORM is
'��·����,��,���,ͼ����,��·���ڵ�';

comment on column RW_NODE_20W.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column RW_NODE_20W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column RW_NODE_20W.U_RECORD is
'�������±�ʶ';

comment on column RW_NODE_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_LINK_20W                                           */
/*==============================================================*/
create table RW_LINK_20W  (
   LINK_PID             NUMBER(10)                      not null,
   FEATURE_PID          NUMBER(10)                     default 0 not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2,3)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,2)),
   LENGTH               NUMBER(15,3)                   default 0 not null,
   GEOMETRY             SDO_GEOMETRY,
   MESH_ID              NUMBER(6)                      default 0 not null,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   DETAIL_FLAG          NUMBER(1)                      default 0 not null
       check (DETAIL_FLAG in (0,1,2,3)),
   COLOR                VARCHAR2(10),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_RW_LINK_20W primary key (LINK_PID),
   constraint RWLINK_ENODE_20W foreign key (E_NODE_PID)
         references RW_NODE_20W (NODE_PID),
   constraint RWLINK_SNODE_20W foreign key (S_NODE_PID)
         references RW_NODE_20W (NODE_PID)
);

comment on column RW_LINK_20W.LINK_PID is
'����';

comment on column RW_LINK_20W.FEATURE_PID is
'�ο�"RW_FEATURE_20W"';

comment on column RW_LINK_20W.S_NODE_PID is
'���,����"RW_NODE_20W"';

comment on column RW_LINK_20W.E_NODE_PID is
'���,����"RW_NODE_20W"';

comment on column RW_LINK_20W.KIND is
'��·,����,���/����';

comment on column RW_LINK_20W.FORM is
'�Ż����';

comment on column RW_LINK_20W.LENGTH is
'��λ:��';

comment on column RW_LINK_20W.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column RW_LINK_20W.SCALE is
'ע:���ֶν�����2.5 �����,20 ����ݲ���Ҫ';

comment on column RW_LINK_20W.DETAIL_FLAG is
'ע:���ֶν�����2.5 �����,20 ����ݲ���Ҫ';

comment on column RW_LINK_20W.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column RW_LINK_20W.U_RECORD is
'�������±�ʶ';

comment on column RW_LINK_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_LINK_NAME                                          */
/*==============================================================*/
create table RW_LINK_NAME  (
   LINK_PID             NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RWLINK_NAME foreign key (LINK_PID)
         references RW_LINK (LINK_PID)
);

comment on column RW_LINK_NAME.LINK_PID is
'���,����"RW_LINK"';

comment on column RW_LINK_NAME.NAME_GROUPID is
'[171U][170]�ο���·���"RD_NAME"';

comment on column RW_LINK_NAME.U_RECORD is
'�������±�ʶ';

comment on column RW_LINK_NAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_LINK_NAME_20W                                      */
/*==============================================================*/
create table RW_LINK_NAME_20W  (
   LINK_PID             NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RWLINK_NAME_20W foreign key (LINK_PID)
         references RW_LINK_20W (LINK_PID)
);

comment on table RW_LINK_NAME_20W is
'[170]';

comment on column RW_LINK_NAME_20W.LINK_PID is
'���,����"RW_LINK_20W"';

comment on column RW_LINK_NAME_20W.NAME_GROUPID is
'[171U]�ο���·���"RD_NAME"';

comment on column RW_LINK_NAME_20W.U_RECORD is
'�������±�ʶ';

comment on column RW_LINK_NAME_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_NODE_MESH                                          */
/*==============================================================*/
create table RW_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RWNODE_MESH foreign key (NODE_PID)
         references RW_NODE (NODE_PID)
);

comment on column RW_NODE_MESH.NODE_PID is
'���,����"RW_NODE"';

comment on column RW_NODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column RW_NODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: RW_NODE_MESH_20W                                      */
/*==============================================================*/
create table RW_NODE_MESH_20W  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint RWNODE_MESH_20W foreign key (NODE_PID)
         references RW_NODE_20W (NODE_PID)
);

comment on column RW_NODE_MESH_20W.NODE_PID is
'���,����"RW_NODE_20W"';

comment on column RW_NODE_MESH_20W.U_RECORD is
'�������±�ʶ';

comment on column RW_NODE_MESH_20W.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TB_ABSTRACT_INFO                                      */
/*==============================================================*/
create table TB_ABSTRACT_INFO  (
   INFO_UUID            VARCHAR2(32)                    not null,
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
   LOCK_FLAG            NUMBER(1,0)                    default 0 not null,
   DELETE_FLAG          NUMBER(1,0)                    default 0 not null,
   DELETE_PERSON        VARCHAR2(50),
   DELETE_TIME          DATE,
   REMARK               VARCHAR2(500),
   QI_STATUS            VARCHAR2(32),
   GET_METHOD_UUID      VARCHAR2(32),
   IS_INFO              NUMBER(1)                      default 0 not null,
   URL                  VARCHAR2(500),
   TITLE                VARCHAR2(2000),
   JUDGE_BATCH_UUID     VARCHAR2(32),
   PUSH_BATCH_UUID      VARCHAR2(32),
   HISTORY_STEP         NUMBER(10)                     default 0 not null,
   QI_REMARK            VARCHAR2(1000),
   QI_ERROR_TYPE        VARCHAR2(1000),
   QI_ERROR_STAT_FLAG   NUMBER(1,0)                    default 0 not null,
   DUPLICATE_FLAG       NUMBER(1,0)                    default 0 not null,
   DUP_DATA_UUID        VARCHAR2(32),
   DUP_REFERENCE_FLAG   NUMBER(1,0)                    default 0 not null,
   SIM_NUM              NUMBER(22)                     default 0 not null,
   DUP_CHECK_TIME       TIMESTAMP(6),
   INFO_RELATION        VARCHAR2(32),
   INFO_RELATION_UUID   VARCHAR2(32),
   AVAILABILITY_JUDGE_PERSON VARCHAR2(50),
   AVAILABILITY_JUDGE_TIME TIMESTAMP(6),
   AVAILABILITY_JUDGE_FALG NUMBER(1)                      default 0 not null,
   PROPERTY_JUDGE_FLAG  NUMBER(1)                      default 0 not null,
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
   SIM_DUP_JUDGE_FLAG   NUMBER(1)                      default 0 not null,
   CONVERT_FLAG         NUMBER(22)                     default 0 not null,
   QI_AVAILABLE_BATCH_UUID VARCHAR2(32),
   QI_DUP_BATCH_UUID    VARCHAR2(32),
   QI_BATCH_UUID        VARCHAR2(32),
   KEYWORDS_GEN_STATUS  NUMBER(1)                      default 0 not null,
   CONTENT_MD5          VARCHAR2(255),
   USE_LEVEL            VARCHAR2(50),
   UPDATE_TYPE          VARCHAR2(32),
   QI_AVAILABLE_NUM     NUMBER(22)                     default 0 not null,
   QI_DUP_NUM           NUMBER(22)                     default 0 not null,
   QI_NUM               NUMBER(22)                     default 0 not null,
   ADMIN_CODE_SIX_BIT   VARCHAR2(6),
   constraint PK_TB_ABSTRACT_INFO primary key (INFO_UUID)
);

comment on column TB_ABSTRACT_INFO.INFO_UUID is
'����';

comment on column TB_ABSTRACT_INFO.HISTORY_STEP is
'Ĭ��Ϊ0,��ʾ����Ϣ,��0Ϊ������Ϣ';

comment on column TB_ABSTRACT_INFO.CONTENT_MD5 is
'���ڱȽ�';

comment on column TB_ABSTRACT_INFO.ADMIN_CODE_SIX_BIT is
'�ڷ���ʱ���øñ���';

/*==============================================================*/
/* Table: TB_INTELLIGENCE                                       */
/*==============================================================*/
create table TB_INTELLIGENCE  (
   SPECIALCASE_ID       VARCHAR2(32)                    not null,
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
   REMIND_CYCLE_VALUE   NUMBER(5,0)                    default 0 not null,
   VERSION_UUID         VARCHAR2(32),
   UPDATE_NUM           NUMBER(5,0)                    default 0 not null,
   INFO_SOURCE_NAME     VARCHAR2(1000),
   OPERATE_TYPE         VARCHAR2(32),
   PUBLISH_NUM          NUMBER(10,0)                   default 0 not null,
   INFO_ADD_FLAG        NUMBER(1)                      default 0 not null,
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
         references TB_ABSTRACT_INFO (INFO_UUID)
);

comment on column TB_INTELLIGENCE.SPECIALCASE_ID is
'���,����"TB_ABSTRACT_INFO"';

/*==============================================================*/
/* Table: TMC_AREA                                              */
/*==============================================================*/
create table TMC_AREA  (
   TMC_ID               NUMBER(10)                      not null,
   LOCTABLE_ID          VARCHAR2(2),
   CID                  VARCHAR2(4)                     not null,
   LOC_CODE             NUMBER(5)                      default 0 not null,
   TYPE_CODE            VARCHAR2(32)                   
       check (TYPE_CODE is null or (TYPE_CODE in ('A1.0','A2.0','A3.0','A5.0','A5.1','A5.2','A5.3','A6.0','A6.1','A6.2','A6.3','A6.4','A6.5','A6.6','A6.7','A6.8','A7.0','A8.0','A9.0','A9.1','A9.2','A10.0','A11.0','A12.0'))),
   UPAREA_TMC_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_AREA primary key (TMC_ID)
);

comment on column TMC_AREA.TMC_ID is
'����';

comment on column TMC_AREA.LOCTABLE_ID is
'�ο�"TMC_LOCATION_CODE"';

comment on column TMC_AREA.LOC_CODE is
'��"LOCTABLE_ID"����ʹ��';

comment on column TMC_AREA.TYPE_CODE is
'TMCλ�ô������(��дӢ�İ����ĸ,��P,A,L),�����Լ�������(Ӣ�İ������,��1,2��)���ɵ��ַ�,ͨ��Ӣ�İ�Ǿ��"."����,��A1.0��ʾ����,A5.1��ʾ�����';

comment on column TMC_AREA.UPAREA_TMC_ID is
'[173sp1][170]�ο���TMC_AREA���ġ�TMC_ID��';

comment on column TMC_AREA.U_RECORD is
'�������±�ʶ';

comment on column TMC_AREA.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TMC_AREA_TRANSLATENAME                                */
/*==============================================================*/
create table TMC_AREA_TRANSLATENAME  (
   TMC_ID               NUMBER(10)                      not null,
   TRANS_LANG           VARCHAR2(3)                    default 'CHI' not null
       check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint TMCAREA_NAME foreign key (TMC_ID)
         references TMC_AREA (TMC_ID)
);

comment on column TMC_AREA_TRANSLATENAME.TMC_ID is
'���,����"TMC_AREA"';

comment on column TMC_AREA_TRANSLATENAME.TRANS_LANG is
'��������(��½),��������(�۰�),Ӣ��,���ĵȶ�������';

comment on column TMC_AREA_TRANSLATENAME.PHONETIC is
'[171U][170]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column TMC_AREA_TRANSLATENAME.U_RECORD is
'�������±�ʶ';

comment on column TMC_AREA_TRANSLATENAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TMC_LINE                                              */
/*==============================================================*/
create table TMC_LINE  (
   TMC_ID               NUMBER(10)                      not null,
   LOCTABLE_ID          VARCHAR2(2),
   CID                  VARCHAR2(4)                     not null,
   LOC_CODE             NUMBER(5)                      default 0 not null,
   TYPE_CODE            VARCHAR2(32)                   
       check (TYPE_CODE is null or (TYPE_CODE in ('L1.0','L1.1','L1.2','L1.3','L1.4','L2.0','L2.1','L2.2','L3.0','L4.0','L5.0','L6.0','L6.1','L6.2','L7.0'))),
   SEQ_NUM              NUMBER(5)                      default 0 not null,
   AREA_TMC_ID          NUMBER(10)                     default 0 not null,
   LOCOFF_POS           NUMBER(10)                     default 0 not null,
   LOCOFF_NEG           NUMBER(10)                     default 0 not null,
   UPLINE_TMC_ID        NUMBER(10)                     default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_LINE primary key (TMC_ID)
);

comment on table TMC_LINE is
'��¼TMC��·��·�ε���Ϣ';

comment on column TMC_LINE.TMC_ID is
'����';

comment on column TMC_LINE.LOCTABLE_ID is
'�ο�"TMC_LOCATION_CODE"';

comment on column TMC_LINE.LOC_CODE is
'��"LOCTABLE_ID"����ʹ��';

comment on column TMC_LINE.TYPE_CODE is
'TMCλ�ô������(��дӢ�İ����ĸ,��P,A,L),�����Լ�������(Ӣ�İ������,��1,2��)���ɵ��ַ�,ͨ��Ӣ�İ�Ǿ��"."����,��L1.0��ʾ��·,L2.0��ʾ��·��';

comment on column TMC_LINE.AREA_TMC_ID is
'[173sp1][170]�ο�"TMC_AREA"��"TMC_ID"';

comment on column TMC_LINE.LOCOFF_POS is
'[173sp1]�ο�"TMC_LINE"��"TMC_ID"';

comment on column TMC_LINE.LOCOFF_NEG is
'[173sp1]�ο�"TMC_LINE"��"TMC_ID"';

comment on column TMC_LINE.UPLINE_TMC_ID is
'[173sp1][170]�ο�"TMC_LINE"��"TMC_ID"';

comment on column TMC_LINE.U_RECORD is
'�������±�ʶ';

comment on column TMC_LINE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TMC_LINE_TRANSLATENAME                                */
/*==============================================================*/
create table TMC_LINE_TRANSLATENAME  (
   TMC_ID               NUMBER(10)                      not null,
   NAME_FLAG            NUMBER(1)                      default 0 not null
       check (NAME_FLAG in (0,1,2)),
   TRANS_LANG           VARCHAR2(3)                    default 'CHI' not null
       check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint TMCLINE_TRANSLATENAME foreign key (TMC_ID)
         references TMC_LINE (TMC_ID)
);

comment on table TMC_LINE_TRANSLATENAME is
'TMC��·��·�ε������';

comment on column TMC_LINE_TRANSLATENAME.TMC_ID is
'���,����"TMC_LINE"';

comment on column TMC_LINE_TRANSLATENAME.TRANS_LANG is
'��������(��½),��������(�۰�),Ӣ��,���ĵȶ�������';

comment on column TMC_LINE_TRANSLATENAME.PHONETIC is
'[171U][170]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column TMC_LINE_TRANSLATENAME.U_RECORD is
'�������±�ʶ';

comment on column TMC_LINE_TRANSLATENAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TMC_POINT                                             */
/*==============================================================*/
create table TMC_POINT  (
   TMC_ID               NUMBER(10)                      not null,
   LOCTABLE_ID          VARCHAR2(2),
   CID                  VARCHAR2(4)                     not null,
   LOC_CODE             NUMBER(5)                      default 0 not null,
   TYPE_CODE            VARCHAR2(32)                   
       check (TYPE_CODE is null or (TYPE_CODE in ('P1.0','P1.1','P1.2','P1.3','P1.4','P1.5','P1.6','P1.7','P1.8','P1.9','P1.10','P1.11','P1.12','P1.13','P1.14','P1.15','P2.0','P2.1','P2.2','P3.0','P3.1','P3.2','P3.3','P3.4','P3.5','P3.6','P3.7','P3.8','P3.9','P3.10','P3.11','P3.12','P3.13','P3.14','P3.15','P3.16','P3.17','P3.18','P3.19','P3.20','P3.21','P3.22','P3.23','P3.24','P3.25','P3.26','P3.27','P3.28','P3.29','P3.30','P3.31','P3.34','P3.35','P3.36','P3.37','P3.38','P3.39','P3.40','P3.41','P3.42','P3.43','P3.44','P3.45','P3.46','P3.47','P4.0'))),
   IN_POS               NUMBER(1)                      default 0 not null
       check (IN_POS in (0,1)),
   IN_NEG               NUMBER(1)                      default 0 not null
       check (IN_NEG in (0,1)),
   OUT_POS              NUMBER(1)                      default 0 not null
       check (OUT_POS in (0,1)),
   OUT_NEG              NUMBER(1)                      default 0 not null
       check (OUT_NEG in (0,1)),
   PRESENT_POS          NUMBER(1)                      default 0 not null
       check (PRESENT_POS in (0,1)),
   PRESENT_NEG          NUMBER(1)                      default 0 not null
       check (PRESENT_NEG in (0,1)),
   LOCOFF_POS           NUMBER(10)                     default 0 not null,
   LOCOFF_NEG           NUMBER(10)                     default 0 not null,
   LINE_TMC_ID          NUMBER(10)                     default 0 not null,
   AREA_TMC_ID          NUMBER(10)                     default 0 not null,
   JUNC_LOCCODE         NUMBER(10)                     default 0 not null,
   NEIGHBOUR_BOUND      VARCHAR2(32),
   NEIGHBOUR_TABLE      NUMBER(2)                      default 0 not null,
   URBAN                NUMBER(1)                      default 0 not null
       check (URBAN in (0,1)),
   INTERUPT_ROAD        NUMBER(1)                      default 0 not null
       check (INTERUPT_ROAD in (0,1)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint PK_TMC_POINT primary key (TMC_ID)
);

comment on column TMC_POINT.TMC_ID is
'����';

comment on column TMC_POINT.LOCTABLE_ID is
'�ο�"TMC_LOCATION_CODE"';

comment on column TMC_POINT.LOC_CODE is
'��"LOCTABLE_ID"����ʹ��';

comment on column TMC_POINT.TYPE_CODE is
'TMCλ�ô������(��дӢ�İ����ĸ,��P,A,L),�����Լ�������(Ӣ�İ������,��1,2��)���ɵ��ַ�,ͨ��Ӣ�İ�Ǿ��"."����,��P1.0��ʾ���ӵ�,L1.15��ʾ���ڵ�';

comment on column TMC_POINT.IN_POS is
'����������·';

comment on column TMC_POINT.IN_NEG is
'�ظ���������·';

comment on column TMC_POINT.OUT_POS is
'�������뿪��·';

comment on column TMC_POINT.OUT_NEG is
'�ظ������뿪��·';

comment on column TMC_POINT.PRESENT_POS is
'�Ƿ�����ڵ�·��������';

comment on column TMC_POINT.PRESENT_NEG is
'�Ƿ�����ڵ�·�ĸ�������';

comment on column TMC_POINT.LOCOFF_POS is
'[173sp1]�ο�"TMC_POINT"��"TMC_ID"';

comment on column TMC_POINT.LOCOFF_NEG is
'[173sp1]�ο�"TMC_POINT"��"TMC_ID"';

comment on column TMC_POINT.LINE_TMC_ID is
'[173sp1][170]�ο�"TMC_LINE"��"TMC_ID"';

comment on column TMC_POINT.AREA_TMC_ID is
'[173sp1][170]�ο�"TMC_AREA"��"TMC_ID"';

comment on column TMC_POINT.JUNC_LOCCODE is
'[173sp1]';

comment on column TMC_POINT.URBAN is
'�Ƿ������';

comment on column TMC_POINT.INTERUPT_ROAD is
'�Ƿ��ϵ�·';

comment on column TMC_POINT.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column TMC_POINT.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column TMC_POINT.U_RECORD is
'�������±�ʶ';

comment on column TMC_POINT.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TMC_POINT_TRANSLATENAME                               */
/*==============================================================*/
create table TMC_POINT_TRANSLATENAME  (
   TMC_ID               NUMBER(10)                      not null,
   NAME_FLAG            NUMBER(1)                      default 1 not null
       check (NAME_FLAG in (1,2,3)),
   TRANS_LANG           VARCHAR2(3)                    default 'CHI' not null
       check (TRANS_LANG in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   TRANSLATE_NAME       VARCHAR2(100),
   PHONETIC             VARCHAR2(1000),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   constraint TMCPOINT_TRANSLATENAME foreign key (TMC_ID)
         references TMC_POINT (TMC_ID)
);

comment on column TMC_POINT_TRANSLATENAME.TMC_ID is
'���,����"TMC_POINT"';

comment on column TMC_POINT_TRANSLATENAME.TRANS_LANG is
'��������(��½),��������(�۰�),Ӣ��,���ĵȶ�������';

comment on column TMC_POINT_TRANSLATENAME.PHONETIC is
'[171U][170]����Ϊƴ��,Ӣ��(���ĵ�)Ϊ����';

comment on column TMC_POINT_TRANSLATENAME.U_RECORD is
'�������±�ʶ';

comment on column TMC_POINT_TRANSLATENAME.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: TMC_VERSION                                           */
/*==============================================================*/
create table TMC_VERSION  (
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
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000)
);

comment on column TMC_VERSION.LOCTABLE_ID is
'�ο�"TMC_LOCATION_CODE"';

comment on column TMC_VERSION.COUNTRY_ID is
'�й�������:059';

comment on column TMC_VERSION.U_RECORD is
'�������±�ʶ';

comment on column TMC_VERSION.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: WL_CHECKCOUNT                                         */
/*==============================================================*/
create table WL_CHECKCOUNT  (
   ITEM_ID              VARCHAR2(100)                   not null,
   TASK_ID              VARCHAR2(500),
   CHECK_TIME           VARCHAR2(100),
   CHECK_COUNT          NUMBER(10)                     default 0 not null
       check (CHECK_COUNT in (0,1)),
   VER_COUNT            NUMBER(10)                     default 0 not null
       check (VER_COUNT in (0,1)),
   NONVER_COUNT         NUMBER(10)                     default 0 not null,
   MEMO                 VARCHAR2(500)
);

/*==============================================================*/
/* Table: WL_CHECKLOG                                           */
/*==============================================================*/
create table WL_CHECKLOG  (
   ITEM_ID              VARCHAR2(100)                   not null,
   PID                  NUMBER(10)                      not null,
   TASK_ID              VARCHAR2(500),
   CHECK_TIME           VARCHAR2(100),
   IS_VIEWED            NUMBER(2)                      default 0 not null
       check (IS_VIEWED in (0,1)),
   IS_MODIFIED          NUMBER(2)                      default 0 not null
       check (IS_MODIFIED in (0,1)),
   MEMO                 VARCHAR2(500)
);

/*==============================================================*/
/* Table: ZONE_FACE                                             */
/*==============================================================*/
create table ZONE_FACE  (
   FACE_PID             NUMBER(10)                      not null,
   REGION_ID            NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   AREA                 NUMBER(30,6)                   default 0,
   PERIMETER            NUMBER(15,3)                   default 0,
   MESH_ID              NUMBER(6)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_ZONE_FACE primary key (FACE_PID),
   constraint ADADMIN_ZONEFACE foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID)
);

comment on column ZONE_FACE.FACE_PID is
'����';

comment on column ZONE_FACE.REGION_ID is
'[170]���,����"AD_ADMIN"';

comment on column ZONE_FACE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ���������,��ĩ�ڵ�����غ�';

comment on column ZONE_FACE.AREA is
'��λ:ƽ����';

comment on column ZONE_FACE.PERIMETER is
'��λ:��';

comment on column ZONE_FACE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column ZONE_FACE.U_RECORD is
'�������±�ʶ';

comment on column ZONE_FACE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ZONE_NODE                                             */
/*==============================================================*/
create table ZONE_NODE  (
   NODE_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (1,2,3)),
   FORM                 NUMBER(1)                      default 0 not null
       check (FORM in (0,1,7)),
   GEOMETRY             SDO_GEOMETRY,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_ZONE_NODE primary key (NODE_PID)
);

comment on column ZONE_NODE.NODE_PID is
'����';

comment on column ZONE_NODE.KIND is
'ƽ�潻���,Zone�߽��';

comment on column ZONE_NODE.FORM is
'ͼ����,�ǵ�';

comment on column ZONE_NODE.GEOMETRY is
'�洢��"��"Ϊ��λ�ľ�γ������';

comment on column ZONE_NODE.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column ZONE_NODE.U_RECORD is
'�������±�ʶ';

comment on column ZONE_NODE.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ZONE_LINK                                             */
/*==============================================================*/
create table ZONE_LINK  (
   LINK_PID             NUMBER(10)                      not null,
   S_NODE_PID           NUMBER(10)                      not null,
   E_NODE_PID           NUMBER(10)                      not null,
   GEOMETRY             SDO_GEOMETRY,
   LENGTH               NUMBER(15,3)                   default 0 not null,
   SCALE                NUMBER(1)                      default 0 not null
       check (SCALE in (0,1,2)),
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_ZONE_LINK primary key (LINK_PID),
   constraint ZONELINK_SNODE foreign key (S_NODE_PID)
         references ZONE_NODE (NODE_PID),
   constraint ZONELINK_ENODE foreign key (E_NODE_PID)
         references ZONE_NODE (NODE_PID)
);

comment on column ZONE_LINK.LINK_PID is
'����';

comment on column ZONE_LINK.S_NODE_PID is
'���,����"ZONE_NODE"';

comment on column ZONE_LINK.E_NODE_PID is
'���,����"ZONE_NODE"';

comment on column ZONE_LINK.GEOMETRY is
'(1)�洢��"��"Ϊ��λ�ľ�γ���������
(2)���(S_NODE_PID)���յ�(E_NODE_PID)�����Ϊ��״�����洢';

comment on column ZONE_LINK.LENGTH is
'��λ:��';

comment on column ZONE_LINK.EDIT_FLAG is
'[171A]�������������ȡʱ,����Ƿ�ɱ༭';

comment on column ZONE_LINK.U_RECORD is
'�������±�ʶ';

comment on column ZONE_LINK.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ZONE_FACE_TOPO                                        */
/*==============================================================*/
create table ZONE_FACE_TOPO  (
   FACE_PID             NUMBER(10)                      not null,
   SEQ_NUM              NUMBER(3)                      default 1,
   LINK_PID             NUMBER(10)                      not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ZONEFACE_LINK foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID),
   constraint ZONEFACE_LINKS foreign key (FACE_PID)
         references ZONE_FACE (FACE_PID)
);

comment on column ZONE_FACE_TOPO.FACE_PID is
'���,����"ZONE_FACE"';

comment on column ZONE_FACE_TOPO.SEQ_NUM is
'��1��ʼ�������';

comment on column ZONE_FACE_TOPO.LINK_PID is
'���,����"ZONE_LINK"';

comment on column ZONE_FACE_TOPO.U_RECORD is
'�������±�ʶ';

comment on column ZONE_FACE_TOPO.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ZONE_LINK_KIND                                        */
/*==============================================================*/
create table ZONE_LINK_KIND  (
   LINK_PID             NUMBER(10)                      not null,
   KIND                 NUMBER(1)                      default 1 not null
       check (KIND in (0,1,2)),
   FORM                 NUMBER(1)                      default 1 not null
       check (FORM in (0,1)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ZONELINK_LINKKIND foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID)
);

comment on table ZONE_LINK_KIND is
'[173sp1]';

comment on column ZONE_LINK_KIND.LINK_PID is
'���,����"ZONE_LINK"';

comment on column ZONE_LINK_KIND.U_RECORD is
'�������±�ʶ';

comment on column ZONE_LINK_KIND.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ZONE_LINK_MESH                                        */
/*==============================================================*/
create table ZONE_LINK_MESH  (
   LINK_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ZONELINK_MESH foreign key (LINK_PID)
         references ZONE_LINK (LINK_PID)
);

comment on column ZONE_LINK_MESH.LINK_PID is
'���,����"ZONE_LINK"';

comment on column ZONE_LINK_MESH.U_RECORD is
'�������±�ʶ';

comment on column ZONE_LINK_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

/*==============================================================*/
/* Table: ZONE_NODE_MESH                                        */
/*==============================================================*/
create table ZONE_NODE_MESH  (
   NODE_PID             NUMBER(10)                      not null,
   MESH_ID              NUMBER(6)                      default 0 not null,
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint ZONENODE_MESH foreign key (NODE_PID)
         references ZONE_NODE (NODE_PID)
);

comment on column ZONE_NODE_MESH.NODE_PID is
'���,����"ZONE_NODE"';

comment on column ZONE_NODE_MESH.U_RECORD is
'�������±�ʶ';

comment on column ZONE_NODE_MESH.U_FIELDS is
'��¼���µ�Ӣ���ֶ���,���֮����ð��''|''�ָ�';

