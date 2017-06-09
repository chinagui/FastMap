
DROP TABLE IX_DEALERSHIP_CHAIN;
DROP TABLE IX_DEALERSHIP_HISTORY;
DROP TABLE IX_DEALERSHIP_RESULT;
DROP TABLE IX_DEALERSHIP_SOURCE;


-- Create table
create table IX_DEALERSHIP_CHAIN
(
  CHAIN_CODE   VARCHAR2(4) not null,
  CHAIN_NAME   VARCHAR2(100) not null,
  CHAIN_WEIGHT NUMBER(1) default 1 not null,
  CHAIN_STATUS NUMBER(1) default 0 not null,
  WORK_TYPE    NUMBER(1) default 0 not null,
  WORK_STATUS  NUMBER(1) default 0 not null
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
comment on column IX_DEALERSHIP_CHAIN.CHAIN_CODE
  is '品牌代码';
comment on column IX_DEALERSHIP_CHAIN.CHAIN_NAME
  is '品牌名称';
comment on column IX_DEALERSHIP_CHAIN.CHAIN_WEIGHT
  is '品牌权重。0  重要
1  普通
';
comment on column IX_DEALERSHIP_CHAIN.CHAIN_STATUS
  is '品牌状态。0  未开启
1  作业中
2  已完成
';
comment on column IX_DEALERSHIP_CHAIN.WORK_TYPE
  is '品牌作业类型。0  无
1  品牌更新
2  一览表
';
comment on column IX_DEALERSHIP_CHAIN.WORK_STATUS
  is '品牌作业状态。0无
1完成表差分
2  完成库差分
3  完成启动录入作业
';


-- Create table
create table IX_DEALERSHIP_SOURCE
(
  SOURCE_ID       NUMBER(10) not null,
  PROVINCE        VARCHAR2(64),
  CITY            VARCHAR2(64),
  PROJECT         VARCHAR2(50),
  KIND_CODE       VARCHAR2(8),
  CHAIN           VARCHAR2(12),
  NAME            VARCHAR2(200),
  NAME_SHORT      VARCHAR2(200),
  ADDRESS         VARCHAR2(500),
  TEL_SALE        VARCHAR2(128),
  TEL_SERVICE     VARCHAR2(128),
  TEL_OTHER       VARCHAR2(128),
  POST_CODE       VARCHAR2(6),
  NAME_ENG        VARCHAR2(200),
  ADDRESS_ENG     VARCHAR2(500),
  PROVIDE_DATE    VARCHAR2(14),
  IS_DELETED      NUMBER(1) default 0 not null,
  FB_SOURCE       NUMBER(1) default 0 not null,
  FB_CONTENT      VARCHAR2(200),
  FB_AUDIT_REMARK VARCHAR2(100),
  FB_DATE         VARCHAR2(14),
  CFM_POI_NUM     VARCHAR2(36),
  CFM_MEMO        VARCHAR2(200),
  DEAL_CFM_DATE   VARCHAR2(14),
  POI_KIND_CODE   VARCHAR2(8),
  POI_CHAIN       VARCHAR2(12),
  POI_NAME        VARCHAR2(200),
  POI_NAME_SHORT  VARCHAR2(200),
  POI_ADDRESS     VARCHAR2(500),
  POI_POST_CODE   VARCHAR2(6),
  POI_X_DISPLAY   NUMBER(10,5) default 0 not null,
  POI_Y_DISPLAY   NUMBER(10,5) default 0 not null,
  POI_X_GUIDE     NUMBER(10,5) default 0 not null,
  POI_Y_GUIDE     NUMBER(10,5) default 0 not null,
  GEOMETRY        SDO_GEOMETRY not null,
  POI_TEL         VARCHAR2(128)
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
comment on column IX_DEALERSHIP_SOURCE.SOURCE_ID
  is '总表代理店号码,主键，本表唯一';
comment on column IX_DEALERSHIP_SOURCE.PROVINCE
  is '省份名称';
comment on column IX_DEALERSHIP_SOURCE.CITY
  is '城市名称';
comment on column IX_DEALERSHIP_SOURCE.PROJECT
  is '项目名称';
comment on column IX_DEALERSHIP_SOURCE.KIND_CODE
  is '代理店分类';
comment on column IX_DEALERSHIP_SOURCE.CHAIN
  is '代理店品牌';
comment on column IX_DEALERSHIP_SOURCE.NAME
  is '厂商提供名称';
comment on column IX_DEALERSHIP_SOURCE.NAME_SHORT
  is '厂商提供简称';
comment on column IX_DEALERSHIP_SOURCE.ADDRESS
  is '厂商提供地址';
comment on column IX_DEALERSHIP_SOURCE.TEL_SALE
  is '厂商提供电话（销售）,多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_SOURCE.TEL_SERVICE
  is '厂商提供电话（维修）,多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_SOURCE.TEL_OTHER
  is '厂商提供电话（其他）,多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_SOURCE.POST_CODE
  is '厂商提供邮编';
comment on column IX_DEALERSHIP_SOURCE.NAME_ENG
  is '厂商提供英文名称';
comment on column IX_DEALERSHIP_SOURCE.ADDRESS_ENG
  is '厂商提供英文地址';
comment on column IX_DEALERSHIP_SOURCE.PROVIDE_DATE
  is '客户提供一览表时间,格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_SOURCE.IS_DELETED
  is '是否是总表中要删除的记录。0	否
1	是（一览表中不存在但总表中存在的记录）
';
comment on column IX_DEALERSHIP_SOURCE.FB_SOURCE
  is '反馈来源。0	无
1	外业 
2	客户
';
comment on column IX_DEALERSHIP_SOURCE.FB_CONTENT
  is '反馈内容';
comment on column IX_DEALERSHIP_SOURCE.FB_AUDIT_REMARK
  is '审核意见。根据反馈内容形成的审核意见';
comment on column IX_DEALERSHIP_SOURCE.FB_DATE
  is '反馈时间。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_SOURCE.CFM_POI_NUM
  is '已采纳POI外业采集号码';
comment on column IX_DEALERSHIP_SOURCE.CFM_MEMO
  is '已采纳POI确认备注';
comment on column IX_DEALERSHIP_SOURCE.DEAL_CFM_DATE
  is '代理店确认时间。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_SOURCE.POI_KIND_CODE
  is '已采纳POI分类新值';
comment on column IX_DEALERSHIP_SOURCE.POI_CHAIN
  is '已采纳POI品牌新值';
comment on column IX_DEALERSHIP_SOURCE.POI_NAME
  is '已采纳POI名称新值';
comment on column IX_DEALERSHIP_SOURCE.POI_NAME_SHORT
  is '已采纳POI简称新值';
comment on column IX_DEALERSHIP_SOURCE.POI_ADDRESS
  is '已采纳POI地址新值';
comment on column IX_DEALERSHIP_SOURCE.POI_POST_CODE
  is '已采纳POI邮编新值';
comment on column IX_DEALERSHIP_SOURCE.POI_X_DISPLAY
  is '已采纳POI显示坐标X新值';
comment on column IX_DEALERSHIP_SOURCE.POI_Y_DISPLAY
  is '已采纳POI显示坐标Y新值';
comment on column IX_DEALERSHIP_SOURCE.POI_X_GUIDE
  is '已采纳POI引导坐标X新值';
comment on column IX_DEALERSHIP_SOURCE.POI_Y_GUIDE
  is '已采纳POI引导坐标Y新值';
comment on column IX_DEALERSHIP_SOURCE.GEOMETRY
  is '代理店显示坐标';
comment on column IX_DEALERSHIP_SOURCE.POI_TEL
  is '已采纳POI电话新值。多个之间半角“|”分隔';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_DEALERSHIP_SOURCE
  add constraint SOURCE_ID_INDEX primary key (SOURCE_ID)
  using index 
  tablespace USERS
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
create table IX_DEALERSHIP_RESULT
(
  RESULT_ID       NUMBER(10) not null,
  WORKFLOW_STATUS NUMBER(1) default 0 not null,
  DEAL_STATUS     NUMBER(1) default 0 not null,
  USER_ID         NUMBER(10) default 0 not null,
  TO_INFO_DATE    VARCHAR2(14),
  TO_CLIENT_DATE  VARCHAR2(14),
  PROVINCE        VARCHAR2(64),
  CITY            VARCHAR2(64),
  PROJECT         VARCHAR2(50),
  KIND_CODE       VARCHAR2(8),
  CHAIN           VARCHAR2(12),
  NAME            VARCHAR2(200),
  NAME_SHORT      VARCHAR2(200),
  ADDRESS         VARCHAR2(500),
  TEL_SALE        VARCHAR2(128),
  TEL_SERVICE     VARCHAR2(128),
  TEL_OTHER       VARCHAR2(128),
  POST_CODE       VARCHAR2(6),
  NAME_ENG        VARCHAR2(200),
  ADDRESS_ENG     VARCHAR2(500),
  PROVIDE_DATE    VARCHAR2(14),
  IS_DELETED      NUMBER(1) default 0 not null,
  MATCH_METHOD    NUMBER(1) default 0 not null,
  POI_NUM_1       VARCHAR2(36),
  POI_NUM_2       VARCHAR2(36),
  POI_NUM_3       VARCHAR2(36),
  POI_NUM_4       VARCHAR2(36),
  POI_NUM_5       VARCHAR2(36),
  SIMILARITY      VARCHAR2(20),
  FB_SOURCE       NUMBER(1) default 0 not null,
  FB_CONTENT      VARCHAR2(200),
  FB_AUDIT_REMARK VARCHAR2(100),
  FB_DATE         VARCHAR2(14),
  CFM_STATUS      NUMBER(1) default 0 not null,
  CFM_POI_NUM     VARCHAR2(36),
  CFM_MEMO        VARCHAR2(200),
  SOURCE_ID       NUMBER(10) default 0,
  DEAL_SRC_DIFF   NUMBER(1) default 1 not null,
  DEAL_CFM_DATE   VARCHAR2(14),
  POI_KIND_CODE   VARCHAR2(8),
  POI_CHAIN       VARCHAR2(12),
  POI_NAME        VARCHAR2(200),
  POI_NAME_SHORT  VARCHAR2(200),
  POI_ADDRESS     VARCHAR2(500),
  POI_TEL         VARCHAR2(128),
  POI_POST_CODE   VARCHAR2(6),
  POI_X_DISPLAY   NUMBER(10,5) default 0 not null,
  POI_Y_DISPLAY   NUMBER(10,5) default 0 not null,
  POI_X_GUIDE     NUMBER(10,5) default 0 not null,
  POI_Y_GUIDE     NUMBER(10,5) default 0 not null,
  GEOMETRY        SDO_GEOMETRY not null,
  REGION_ID       NUMBER(10) default 0 not null,
  CFM_IS_ADOPTED  NUMBER(1) default 0
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the columns 
comment on column IX_DEALERSHIP_RESULT.RESULT_ID
  is '一览表代理店号码。主键，本表唯一';
comment on column IX_DEALERSHIP_RESULT.WORKFLOW_STATUS
  is '工艺状态。0 无
1 差分一致，无需处理；
2 需删除；
3 内业录入作业；
4 转外业确认；
5 转客户确认；
6 不代理
9 外业处理完成，出品；
';
comment on column IX_DEALERSHIP_RESULT.DEAL_STATUS
  is '代理店状态。0	无
1	待作业
2	待提交
3	已提交
';
comment on column IX_DEALERSHIP_RESULT.USER_ID
  is '作业员ID。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_RESULT.TO_INFO_DATE
  is '下发情报时间。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_RESULT.TO_CLIENT_DATE
  is '发布客户时间';
comment on column IX_DEALERSHIP_RESULT.PROVINCE
  is '省份名称';
comment on column IX_DEALERSHIP_RESULT.CITY
  is '城市名称';
comment on column IX_DEALERSHIP_RESULT.PROJECT
  is '项目名称';
comment on column IX_DEALERSHIP_RESULT.KIND_CODE
  is '代理店分类';
comment on column IX_DEALERSHIP_RESULT.CHAIN
  is '代理店品牌';
comment on column IX_DEALERSHIP_RESULT.NAME
  is '厂商提供名称';
comment on column IX_DEALERSHIP_RESULT.NAME_SHORT
  is '厂商提供简称';
comment on column IX_DEALERSHIP_RESULT.ADDRESS
  is '厂商提供地址';
comment on column IX_DEALERSHIP_RESULT.TEL_SALE
  is '厂商提供电话（销售）,多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_RESULT.TEL_SERVICE
  is '厂商提供电话（维修）,多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_RESULT.TEL_OTHER
  is '厂商提供电话（其他）,多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_RESULT.POST_CODE
  is '厂商提供邮编';
comment on column IX_DEALERSHIP_RESULT.NAME_ENG
  is '厂商提供英文名称';
comment on column IX_DEALERSHIP_RESULT.ADDRESS_ENG
  is '厂商提供英文地址';
comment on column IX_DEALERSHIP_RESULT.PROVIDE_DATE
  is '客户提供一览表时间,格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_RESULT.IS_DELETED
  is '是否是总表中要删除的记录。0  否
1  是（一览表中不存在但总表中存在的记录）
';
comment on column IX_DEALERSHIP_RESULT.MATCH_METHOD
  is '与POI的匹配方式。0	不应用
1	ID匹配
2	推荐匹配
';
comment on column IX_DEALERSHIP_RESULT.POI_NUM_1
  is '匹配度最高的POI外业采集号码';
comment on column IX_DEALERSHIP_RESULT.POI_NUM_2
  is '匹配度最高的POI外业采集号码';
comment on column IX_DEALERSHIP_RESULT.POI_NUM_3
  is '匹配度最高的POI外业采集号码';
comment on column IX_DEALERSHIP_RESULT.POI_NUM_4
  is '匹配度最高的POI外业采集号码';
comment on column IX_DEALERSHIP_RESULT.POI_NUM_5
  is '匹配度最高的POI外业采集号码';
comment on column IX_DEALERSHIP_RESULT.SIMILARITY
  is '匹配度。推荐匹配时有效；
按匹配度从高到底顺序排列，五个值之间用半角“|”分隔；
';
comment on column IX_DEALERSHIP_RESULT.FB_SOURCE
  is '反馈来源。0  无
1  外业
2  客户
';
comment on column IX_DEALERSHIP_RESULT.FB_CONTENT
  is '反馈内容';
comment on column IX_DEALERSHIP_RESULT.FB_AUDIT_REMARK
  is '审核意见。根据反馈内容形成的审核意见';
comment on column IX_DEALERSHIP_RESULT.FB_DATE
  is '反馈时间。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_RESULT.CFM_STATUS
  is '数据确认状态。0	无
1	待发布
2	待确认
3	已反馈
';
comment on column IX_DEALERSHIP_RESULT.CFM_POI_NUM
  is '已采纳POI外业采集号码';
comment on column IX_DEALERSHIP_RESULT.CFM_MEMO
  is '已采纳POI确认备注';
comment on column IX_DEALERSHIP_RESULT.SOURCE_ID
  is '总表代理店号码,主键，本表唯一';
comment on column IX_DEALERSHIP_RESULT.DEAL_SRC_DIFF
  is '一览表与总表差分结果类型。1	新旧一致
2	旧版有新版没有，需删除
3	新版有旧版没有，需新增
4	新版较旧版有变更，需变更
5	其他情况
';
comment on column IX_DEALERSHIP_RESULT.DEAL_CFM_DATE
  is '代理店确认时间。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_RESULT.POI_KIND_CODE
  is '已采纳POI分类新值';
comment on column IX_DEALERSHIP_RESULT.POI_CHAIN
  is '已采纳POI品牌新值';
comment on column IX_DEALERSHIP_RESULT.POI_NAME
  is '已采纳POI名称新值';
comment on column IX_DEALERSHIP_RESULT.POI_NAME_SHORT
  is '已采纳POI简称新值';
comment on column IX_DEALERSHIP_RESULT.POI_ADDRESS
  is '已采纳POI地址新值';
comment on column IX_DEALERSHIP_RESULT.POI_TEL
  is '已采纳POI电话新值。多个之间半角“|”分隔';
comment on column IX_DEALERSHIP_RESULT.POI_POST_CODE
  is '已采纳POI邮编新值';
comment on column IX_DEALERSHIP_RESULT.POI_X_DISPLAY
  is '已采纳POI显示坐标X新值';
comment on column IX_DEALERSHIP_RESULT.POI_Y_DISPLAY
  is '已采纳POI显示坐标Y新值';
comment on column IX_DEALERSHIP_RESULT.POI_X_GUIDE
  is '已采纳POI引导坐标X新值';
comment on column IX_DEALERSHIP_RESULT.POI_Y_GUIDE
  is '已采纳POI引导坐标Y新值';
comment on column IX_DEALERSHIP_RESULT.GEOMETRY
  is '代理店显示坐标';
comment on column IX_DEALERSHIP_RESULT.REGION_ID
  is '大区ID';
comment on column IX_DEALERSHIP_RESULT.CFM_IS_ADOPTED
  is '0未处理，1未采纳，2已采纳';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_DEALERSHIP_RESULT
  add constraint RESULT_ID_INDEX primary key (RESULT_ID)
  using index 
  tablespace USERS
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
create table IX_DEALERSHIP_HISTORY
(
  HISTORY_ID NUMBER(10) not null,
  RESULT_ID  NUMBER(10) not null,
  FIELD_NAME VARCHAR2(25),
  U_RECORD   NUMBER(1) default 3 not null,
  OLD_VALUE  VARCHAR2(1000),
  NEW_VALUE  VARCHAR2(1000),
  U_DATE     VARCHAR2(14),
  USER_ID    NUMBER(10) default 0 not null
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255;
-- Add comments to the columns 
comment on column IX_DEALERSHIP_HISTORY.HISTORY_ID
  is '变更履历号码，主键，本表唯一';
comment on column IX_DEALERSHIP_HISTORY.RESULT_ID
  is '一览表代理店号码外键，关联IX_DEALERSHIP_RESULT. RESULT_ID';
comment on column IX_DEALERSHIP_HISTORY.FIELD_NAME
  is '变更字段名称';
comment on column IX_DEALERSHIP_HISTORY.U_RECORD
  is '变更字段变更类型1  新增
2  删除
3  修改
';
comment on column IX_DEALERSHIP_HISTORY.OLD_VALUE
  is '变更字段变更前内容';
comment on column IX_DEALERSHIP_HISTORY.NEW_VALUE
  is '变更字段变更后内容';
comment on column IX_DEALERSHIP_HISTORY.U_DATE
  is '变更日期。格式： YYYYMMDDHHMMSS
如 20140812152100；24小时制。
';
comment on column IX_DEALERSHIP_HISTORY.USER_ID
  is '作业员ID';
-- Create/Recreate primary, unique and foreign key constraints 
alter table IX_DEALERSHIP_HISTORY
  add constraint HISTORY_ID_INDEX primary key (HISTORY_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255;





commit;
exit;