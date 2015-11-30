-- Create table
create  table TEMP_AU_IX_POI_MUL_TASK
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIP_MT on TEMP_AU_IX_POI_MUL_TASK (PID);
create  table TEMP_AU_IX_POI_UNIQ_TASK
-- Create table
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIP_UT on TEMP_AU_IX_POI_UNIQ_TASK (PID);
-- Create table
create  table TEMP_AU_POI_MODIFY_LOG
(
  PID                NUMBER(10),
  NAME_FLAG          NUMBER(1),
  ADDRESS_FLAG       NUMBER(1),
  TEL_FLAG           NUMBER(1),  
  KIND_FLAG          NUMBER(1),
  POST_CODE_FLAG     NUMBER(1),
  FOOD_TYPE_FLAG     NUMBER(1),
  PARENT_FLAG        NUMBER(1),
  LABLE_FLAG         NUMBER(1),
  DISPLAY_POINT_FLAG NUMBER(1),
  GUIDE_POINT_FLAG   NUMBER(1),
  AUDATA_ID          NUMBER(10) not null,
  GUIDE_X_FLAG 		   NUMBER(1),
  GUIDE_Y_FLAG 		   NUMBER(1),
  CHAIN_FLAG		     NUMBER(1),
  tenant_flag        NUMBER(1),
  FLOOR_USED         NUMBER(1),
  FLOOR_EMPTY        NUMBER(1),
  YUCAIJI_FLAG       NUMBER(1),
  poilevel_flag      NUMBER(1),
  verified_flag      NUMBER(1),
  INNERPOI_FLAG      NUMBER(1),
  OPEN24H_FLAG       NUMBER(1),
  SAMEPOI_FLAG       NUMBER(1),
  verified_mode_flag NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AP_MODLOG_ADID on TEMP_AU_POI_MODIFY_LOG (AUDATA_ID);
--create index IDX_TMP_AP_MODLOG_PID on TEMP_AU_POI_MODIFY_LOG (PID);
-- Create table
create  table TEMP_HIS_IX_POI 
as select * from ix_poi where 1=2
;
-- Create/Re--create indexes
--create index IDX_TMP_HIS_IX_POI on TEMP_HIS_IX_POI (PID);


-- Create/Re--create indexes
--create index idx_au_ipn_pid on AU_IX_POI_NAME (poi_pid,AUDATA_ID);
--create index IX_AIP_PS on AU_IX_POI (PID, STATE, ATT_OPRSTATUS);
-- Create/Re--create indexes
--create index idx_ipa on IX_POI_ADDRESS (poi_pid);
--create index IDX_IPN_PID on IX_POI_NAME (POI_PID);


create  table TEMP_HIS_IX_ANNOTATION 
as select * from IX_ANNOTATION where 1=2;

-- Create/Re--create indexes
--create index IDX_TMP_HIS_IX_ANNOTATION on TEMP_HIS_IX_ANNOTATION (PID);


create  table  TEMP_HIS_IX_POINTADDRESS  
as select * from IX_POINTADDRESS where 1=2;

--create index IDX_TMP_HIS_IX_POINTADDRESS on TEMP_HIS_IX_POINTADDRESS (PID);
-- Create table
create  table TEMP_AU_PT_POI_MODIFY_LOG
(
  PID                NUMBER(10),
  NAME_FLAG          NUMBER(1),
  KIND_FLAG          NUMBER(1),
  GUIDE_POINT_FLAG   NUMBER(1),
  DISPLAY_POINT_FLAG NUMBER(1),
  POI_MEMO_FLAG NUMBER(1),
  PARENT_FLAG        NUMBER(1),
  ACCESS_TYPE_FLAG   NUMBER(1),
  ACCESS_METH_FLAG   NUMBER(1),
  eta_stop_alias_name NUMBER(1),  --改主点别名
  eta_stop_private_park NUMBER(1),  --改专用停车场
  eta_stop_p_park_period NUMBER(1),  --改停车时段
  eta_stop_carport_exact NUMBER(1),  --改车位数量（精确值）
  eta_stop_carport_estimate NUMBER(1),  --改车位数量（估值）
  eta_stop_BIKE_PARK NUMBER(1),  --改自行车停车场|
eta_stop_b_PARK_PERIOD NUMBER(1),  --改自行车有人看守时段|
eta_stop_MANUAL_TICKET NUMBER(1),  --改人工售票|
eta_stop_m_TICKET_PERIOD NUMBER(1),  --改人工售票时段|
eta_stop_MOBILE NUMBER(1),  --改手机信号|
eta_stop_BAGGAGE_SECURITY NUMBER(1),  --改行李安检|
eta_stop_LEFT_BAGGAGE NUMBER(1),  --改自助行李寄存柜|
eta_stop_CONSIGNATION_EXACT NUMBER(1),  --改寄存柜数量（精确值）|
eta_stop_CONSIGNATION_ESTIMATE NUMBER(1),  --改寄存柜数量（估值）|
eta_stop_CONVENIENT NUMBER(1),  --改零售便利店|
eta_stop_SMOKE NUMBER(1),  --改允许吸烟|
eta_stop_BUILD_TYPE NUMBER(1),  --改建筑类型|
eta_stop_AUTO_TICKET NUMBER(1),  --改自动售票机|
eta_stop_TOILET NUMBER(1),  --改洗手间|
eta_stop_WIFI NUMBER(1),  --改无线网|
eta_stop_OPEN_PERIOD NUMBER(1),  --改开放时间|
eta_stop_FARE_AREA NUMBER(1),  --改票价区域|
eta_access_ALIAS_NAME NUMBER(1),  --改出入口别名|
eta_access_OPEN_PERIOD NUMBER(1),  --改开放时间|
eta_access_MANUAL_TICKET NUMBER(1),  --改人工售票|
eta_access_m_TICKET_PERIOD NUMBER(1),  --改人工售票时段|
eta_access_AUTO_TICKET NUMBER(1),  --改自动售票机|
  AUDATA_ID          NUMBER(10) not null
)
;
-- Create/Re--create indexes
--create index IDX_TMP_PT_MODLOG_ADID on TEMP_AU_PT_POI_MODIFY_LOG (AUDATA_ID);
--create index IDX_TMP_PT_MODLOG_PID on TEMP_AU_PT_POI_MODIFY_LOG (PID);


create  table TEMP_HIS_PT_POI 
as select * from pt_poi where 1=2;

--create index IDX_TEMP_HIS_PT_POI on TEMP_HIS_PT_POI (PID);

create  TABLE  TEMP_PT_PLATFORM 
as select * from PT_PLATFORM where 1=2;

--create index IDX_TEMP_PT_PLATFORM on TEMP_PT_PLATFORM (PID);






create   table temp_his_pt_company 
as select * from pt_company where 1=2;


create   table TEMP_AU_PT_COMPANY_MODIFY_LOG
(
COMPANY_ID                NUMBER(10),
NAME_FLAG          NUMBER(1),
NAME_ENG_SHORT_FLAG          NUMBER(1),
NAME_ENG_FULL_FLAG   NUMBER(1),
AUDATA_ID          NUMBER(10) not null
)
;

-- Create table
create  table TEMP_HIS_PT_PLATFORM  
as select * from PT_PLATFORM where 1=2;

-- Create/Re--create indexes
--create index IDX_TEMP_HIS_PT_PLATFORM on TEMP_HIS_PT_PLATFORM (PID);


-- Create table
create  table TEMP_AU_PT_PLATFORM_MODIFY_LOG
(
  PID                NUMBER(10),
  PLATNAME_FLAG      NUMBER(1),
  COLLECT_FLAG       NUMBER(1),
  PLATLEVEL_FLAG     NUMBER(1),
  TRANSFLAG_FLAG     NUMBER(1),
  STOPID_FLAG        NUMBER(1),
  AUDATA_ID          NUMBER(10) not null,
  LOCATION_FIR_FLAG  NUMBER(1),
  LOCATION_SEC_FLAG  NUMBER(1),
  TRANSFER_TYPE_FLAG NUMBER(1),
  TRANSFER_TIME_FLAG NUMBER(1),
  EXTERNAL_FLAG_FLAG NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_PLT_MODLOG_ADID on TEMP_AU_PT_PLATFORM_MODIFY_LOG (AUDATA_ID);
--create index IDX_TMP_PLT_MODLOG_PID on TEMP_AU_PT_PLATFORM_MODIFY_LOG (PID);


-- Create table
create  table TEMP_AU_IX_ANNO_MUL_TASK
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIA_MT on TEMP_AU_IX_ANNO_MUL_TASK (PID);
-- Create table
create  table temp_au_ix_anno_uniq_task
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIA_UT on temp_au_ix_anno_uniq_task (PID);

--temp_au_ix_point_mul_task
-- Create table
create  table temp_au_ix_point_mul_task
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIPA_MT on temp_au_ix_point_mul_task (PID);

create  table temp_au_ptcom_mul_task
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_aptcom_MT on temp_au_ptcom_mul_task (PID);

--temp_au_ptline_mul_task

create  table temp_au_ptline_mul_task
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_ptline_MT on temp_au_ptline_mul_task (PID);

--temp_au_pt_plt_mul_task
create  table temp_au_pt_plt_mul_task
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_ptplt_MT on temp_au_pt_plt_mul_task (PID);
--temp_au_ptpoi_mul_task
create  table temp_au_ptpoi_mul_task
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_ptpoi_MT on temp_au_ptpoi_mul_task (PID);


-- Create table
create  table TEMP_AU_ANN_MODIFY_LOG
(
  PID                NUMBER(10),
  NAME_FLAG          NUMBER(1),
  ADDRESS_FLAG       NUMBER(1),
  TEL_FLAG           NUMBER(1),
  KIND_FLAG          NUMBER(1),
  POST_CODE_FLAG     NUMBER(1),
  FOOD_TYPE_FLAG     NUMBER(1),
  PARENT_FLAG        NUMBER(1),
  LABLE_FLAG         NUMBER(1),
  DISPLAY_POINT_FLAG NUMBER(1),
  GUIDE_POINT_FLAG   NUMBER(1),
  AUDATA_ID          NUMBER(10) not null
)
;
-- Create/Re--create indexes
--create index IDX_TMP_ANN_MODLOG_ADID on TEMP_AU_ANN_MODIFY_LOG (AUDATA_ID);
--create index IDX_TMP_ANN_MODLOG_PID on TEMP_AU_ANN_MODIFY_LOG (PID);



-- Create table
create  table TEMP_HIS_IX_POI_EXT 
as select * from ix_poi where 1=2 ;

-- Create/Re--create indexes
--create index IDX_TMP_HIS_IX_POI_E on TEMP_HIS_IX_POI_EXT (PID);


-- Create table
create  table TEMP_AU_IX_POI_GRP
(
  AUDATA_ID NUMBER(10),
  PID       NUMBER(10),
  STATE     NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIP_GP on TEMP_AU_IX_POI_GRP (AUDATA_ID, PID);

-- Create table
create  table TEMP_AU_MUL_DEL_IX_POI
(
  PID       NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AMD_IP on TEMP_AU_MUL_DEL_IX_POI (PID);

-- Create table
create  table TEMP_HIS_PT_TRANSFER 
as select * from PT_TRANSFER where 1=2;



create  table TEMP_HIS_IX_POI_NAME  
as select * from IX_POI_NAME where 1=2;
--create index IDX_TEMP_HIS_IX_POI_NAME on TEMP_HIS_IX_POI_NAME (POI_PID, LANG_CODE,NAME_CLASS);


CREATE OR REPLACE VIEW VIEW_MG_AU_IX_POI_NAME AS
SELECT NAME_ID,
NAME_GROUPID,
POI_PID,
LANG_CODE,
NAME_CLASS,
NAME_TYPE,
NAME,
NAME_PHONETIC,
KEYWORDS,
NIDB_PID,
0             AS U_RECORD,
NULL          AS U_FIELDS
FROM au_ix_poi_name auipn,TEMP_AU_POI_MODIFY_LOG tmp
WHERE auipn.lang_code IN ('CHI', 'CHT')
AND auipn.name_class = 2
AND  tmp.name_flag = 1
AND auipn.AUDATA_ID = tmp.AUDATA_ID
AND EXISTS(SELECT 1 FROM ix_poi_name ipn WHERE ipn.lang_code IN ('CHI', 'CHT')
AND ipn.name_class = 2
AND ipn.poi_pid = auipn.poi_pid);
---
CREATE VIEW VIEW_MG_AU_IX_POI_NAME_ext AS
SELECT NAME_ID,
       NAME_GROUPID,
       POI_PID,
       LANG_CODE,
       NAME_CLASS,
       NAME_TYPE,
       NAME,
       NAME_PHONETIC,
       KEYWORDS,
       NIDB_PID,
       0             AS U_RECORD,
       NULL          AS U_FIELDS
  FROM au_ix_poi_name auipn
 WHERE auipn.lang_code IN ('CHI', 'CHT')
   AND auipn.name_class = 2
   AND EXISTS
 (SELECT t.poi_pid
          FROM ix_poi_name t
         WHERE t.poi_pid IN (SELECT pid FROM TEMP_HIS_IX_POI_EXT)
           AND t.lang_code IN ('CHI', 'CHT')
           AND t.name_class = 2
           AND t.poi_pid = auipn.poi_pid);
--
CREATE VIEW VIEW_MG_MUL_IX_POI_NAME
AS
SELECT AUDATA_ID,
	   NAME_ID,
       NAME_GROUPID,
       POI_PID,
       LANG_CODE,
       NAME_CLASS,
       NAME_TYPE,
       NAME,
       NAME_PHONETIC,
       KEYWORDS,
       NIDB_PID,
       0             AS U_RECORD,
       NULL          AS U_FIELDS
  FROM au_ix_poi_name auipn
 WHERE auipn.lang_code IN ('CHI', 'CHT')
   AND auipn.name_class = 2
   AND EXISTS (SELECT t.poi_pid
          FROM ix_poi_name t
         WHERE t.poi_pid = auipn.poi_pid
           AND t.lang_code IN ('CHI', 'CHT')
           AND t.name_class = 2);

--
create  table  TEMP_IX_ANNOTATION_EXT 
as select * from IX_ANNOTATION where 1=2;
-- Create table
create  table temp_his_ix_poi_parent 
as select * from ix_poi_parent where 1=2;
--create index IDX_TEMP_HIS_IX_POI_PARENT on TEMP_HIS_IX_POI_PARENT (GROUP_ID);

-- Create table
create  table temp_ix_poi_parent_mg 
as select * from ix_poi_parent where 1=2;

--create index IDX_TEMP_IX_POI_PARENT_MG on temp_ix_poi_parent_mg (GROUP_ID);


create  table TEMP_IX_POI_NAME_MG 
as select * from IX_POI_NAME where 1=2;
alter table TEMP_IX_POI_NAME_MG
  add constraint pk_tmp_mg_poiname primary key (NAME_ID);


create  table temp_ix_poi_name_mg1 
as select * from IX_POI_NAME where 1=2;
alter table temp_ix_poi_name_mg1
  add constraint pk_tmp_mg_poiname1 primary key (NAME_ID);


 CREATE OR REPLACE VIEW view_mg_au_ix_anot_name_ext AS
SELECT * FROM au_ix_annotation_name au
    WHERE au.lang_code IN ('CHI','CHT')
    AND au.name_class=2
    AND EXISTS  (SELECT 1 FROM au_ix_annotation auia WHERE auia.pid=au.pid AND auia.att_oprstatus = 0 )
    AND EXISTS  (SELECT 1 FROM temp_ix_annotation_ext ext WHERE ext.pid = au.pid)
    and exists(select 1 from temp_au_ix_anno_uniq_task tmp where tmp.pid=au.pid)
    AND EXISTS (SELECT 1
              FROM ix_annotation_name ian
             WHERE ian.lang_code = 'CHI'
               AND ian.name_class = 2
               AND ian.pid = au.pid)   ;
CREATE OR REPLACE VIEW view_mg_au_ix_anot_name AS
SELECT * FROM au_ix_annotation_name au
   WHERE au.lang_code IN ('CHI','CHT')
AND au.name_class=2
AND EXISTS  (SELECT 1 FROM au_ix_annotation auia WHERE auia.pid=au.pid AND auia.att_oprstatus = 0 AND auia.field_modify_flag LIKE '%改名称%')
and exists(select 1 from temp_au_ix_anno_uniq_task tmp where tmp.pid=au.pid)
AND EXISTS (SELECT 1
FROM ix_annotation_name ian
WHERE ian.lang_code = 'CHI'
AND ian.name_class = 2
AND ian.pid = au.pid);


create  table TEMP_HIS_IX_ANNOT_NAME 
as select * from ix_annotation_name where 1=2;

alter table TEMP_HIS_IX_ANNOT_NAME
  add constraint pk_tmp_his_anot_name primary key (NAME_ID);



 create  table temp_ix_anot_name_mg
 
as select * from ix_annotation_name where 1=2;

alter table temp_ix_anot_name_mg
  add constraint pk_tmp_mg_annotname primary key (NAME_ID);

 create  table temp_ix_anot_name_mg_1 
as select * from ix_annotation_name where 1=2;
alter table temp_ix_anot_name_mg_1
  add constraint pk_tmp_mg_annotname1 primary key (NAME_ID);



  create  table TEMP_PTPOI_EXT  
  as select * from pt_poi where 1=2;
--create index IDX_TEMP_PTPOI_EXT on TEMP_PTPOI_EXT (PID);

CREATE OR REPLACE VIEW VIEW_MG_PT_POINAME_EXT AS
SELECT AUDATA_ID,NAME_ID,NAME_GROUPID,POI_PID,LANG_CODE,NAME_CLASS,NAME,PHONETIC,NIDB_PID,ATT_TASK_ID,FIELD_TASK_ID,ATT_OPRSTATUS,ATT_CHECKSTATUS
FROM au_pt_poi_name auppn
WHERE auppn.lang_code IN ('CHI', 'CHT')
AND auppn.name_class =1
AND EXISTS (SELECT 1
FROM temp_ptpoi_ext ext,au_pt_poi au
WHERE ext.pid = auppn.poi_pid and ext.pid=au.pid and au.att_oprstatus=0 )
AND EXISTS (SELECT 1
FROM pt_poi_name p
WHERE p.lang_code IN ('CHI', 'CHT')
AND p.name_class = 1
AND p.poi_pid = auppn.poi_pid);


  create  table  TEMP_PT_POI_NAME_MG 
  as select * from pt_poi_name where 1=2;
--create index IDX_TEMP_PTPOI_NAME_MG on TEMP_PT_POI_NAME_MG (NAME_ID);

create  table  temp_pt_poi_name_mg1 
  as select * from pt_poi_name where 1=2;
--create index IDX_TEMP_PTPOI_NAME_MG1 on temp_pt_poi_name_mg1 (NAME_ID);

create  table  TEMP_HIS_PT_POI_NAME 
  as select * from pt_poi_name where 1=2;
--create index IDX_TEMP_HIS_PTPOI_NAME on TEMP_HIS_PT_POI_NAME (NAME_ID);

create  table  temp_pt_poi_parent_mg  
  as select * from pt_poi_parent where 1=2;

--create index IDX_TEMP_HIS_PTPOI_PARENT_MG on temp_pt_poi_parent_mg (GROUP_ID);

create  table  temp_his_pt_poi_parent 
  as select * from pt_poi_parent where 1=2;

--create index IDX_TEMP_HIS_PTPOI_PARENT on temp_his_pt_poi_parent (group_id,parent_poi_pid);


CREATE OR REPLACE VIEW VIEW_MG_AU_PT_POI_NAME AS

SELECT NAME_ID,
NAME_GROUPID,
POI_PID,
LANG_CODE,
NAME_CLASS,
NAME,
PHONETIC
FROM au_pt_poi_name auipn
WHERE auipn.lang_code IN ('CHI', 'CHT')
AND auipn.name_class = 1
AND EXISTS(SELECT 1 FROM temp_au_pt_poi_modify_log tmp
WHERE tmp.name_flag = 1
AND auipn.audata_id = tmp.audata_id)
AND EXISTS(SELECT 1 FROM pt_poi_name ipn WHERE ipn.lang_code IN ('CHI', 'CHT')
AND ipn.name_class = 1
AND ipn.poi_pid = auipn.poi_pid);


-- Create table
create  table temp_mul_ix_poi_mg
(
  AUDATA_ID NUMBER(10),
  PID       NUMBER(10),
  STATE     NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_temp_mul_ix_poi_mg on temp_mul_ix_poi_mg (AUDATA_ID, PID);

create  table TEMP_IX_POI_ADDRESS_MG 
  as select * from ix_poi_address  where 1=2;

--create index IDX_TEMP_IX_POI_ADDRESS_MG on TEMP_IX_POI_ADDRESS_MG (NAME_ID);

create  table TEMP_IX_POI_RESTAURANT_MG 
  as select * from ix_poi_restaurant  where 1=2;

--create index IDX_TEMP_RESTAURANT_MG on TEMP_IX_POI_RESTAURANT_MG (RESTAURANT_ID);



-- Create table
create  table temp_au_ix_anot_grp
(
  AUDATA_ID NUMBER(10),
  PID       NUMBER(10),
  STATE     NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AIA_GP on temp_au_ix_anot_grp (AUDATA_ID, PID);

--create index IDX_AU_IX_ANOT on AU_IX_ANNOTATION (pid, geo_oprstatus, att_oprstatus, modify_flag);


CREATE OR REPLACE VIEW view_mg_mul_ix_anot_name
AS
SELECT audata_id,pid, lang_code, name_class, NAME, phonetic
             FROM au_ix_annotation_name au
            WHERE au.lang_code IN ('CHI', 'CHT')
              AND au.name_class = 2
              AND EXISTS (SELECT 1
                     FROM ix_annotation_name n
                    WHERE n.lang_code IN ('CHI', 'CHT')
                      AND n.name_class = 2
                      AND n.pid = au.pid);

-- Create table
create  table temp_his_pt_line 
  as select * from pt_line  where 1=2;

-- Create/Re--create indexes
--create index IDX_temp_his_pt_line on temp_his_pt_line (PID);

-- Create table
create  table temp_ptline_ext  
  as select * from pt_line  where 1=2;

-- Create/Re--create indexes
--create index IDX_temp_ptline_ext on temp_ptline_ext (PID);

create  table TEMP_PT_LINE_NAME 
  as select * from PT_LINE_NAME  where 1=2;

-- Create/Re--create indexes
--create index IDX_TEMP_PT_LINE_NAME on TEMP_PT_LINE_NAME (NAME_ID);


create  table temp_pt_line_name_mg 
  as select * from PT_LINE_NAME  where 1=2;

-- Create/Re--create indexes
--create index IDX_temp_pt_line_name_mg on temp_pt_line_name_mg (NAME_ID);


CREATE OR REPLACE VIEW view_mg_pt_linename_ext
AS
SELECT *
             FROM au_pt_line_name auppn
            WHERE auppn.lang_code IN ('CHI', 'CHT')
              AND EXISTS (SELECT 1
                     FROM temp_ptline_ext ext
                    WHERE ext.pid = auppn.pid)
              AND EXISTS (SELECT 1
                     FROM pt_line_name p
                    WHERE p.lang_code IN ('CHI', 'CHT')
                      AND p.pid = auppn.pid);
-- Create table
create  table TEMP_AU_PT_LINE_MODIFY_LOG
(
  PID                NUMBER(10),
  SYSTEM_ID 		NUMBER(1),
  TYPE 				NUMBER(1),
  NAME 				NUMBER(1),
  AUDATA_ID          NUMBER(10) not null
)
;
-- Create/Re--create indexes
--create index IDX_TMP_PTLINE_MODLOG_ADID on TEMP_AU_PT_LINE_MODIFY_LOG (AUDATA_ID);
--create index IDX_TMP_PTLINE_MODLOG_PID on TEMP_AU_PT_LINE_MODIFY_LOG (PID);

create or replace view view_mg_pt_linename as
  select auptln.* from au_pt_line_name auptln,temp_au_pt_line_modify_log tmp where auptln.audata_id=tmp.audata_id and tmp.name=1 and auptln.lang_code IN ('CHI', 'CHT')    ;
-- Create table
create  table temp_pt_eta_line 
  as select * from pt_eta_line  where 1=2;
-- Create/Re--create indexes
--create index IDX_temp_pt_eta_line on temp_pt_eta_line (PID);
-- Create table
create  table TEMP_AU_PT_ETA_LINE_MOD_LOG
(
  PID               NUMBER(10) not null,
  AUDATA_ID         NUMBER(10) not null,
  ALIAS_NAME_FLAG   NUMBER(1) not null,
  ALIAS_PINYIN_FLAG NUMBER(1) not null,
  BIKE_FLAG         NUMBER(1) not null,
  BIKE_PERIOD_FLAG  NUMBER(1) not null,
  IMAGE_FLAG        NUMBER(1) not null,
  RACK_FLAG         NUMBER(1) not null,
  DINNER_FLAG       NUMBER(1) not null,
  TOILET_FLAG       NUMBER(1) not null,
  SLEEPER_FLAG      NUMBER(1) not null,
  WHEEL_CHAIR_FLAG  NUMBER(1) not null,
  SMOKE_FLAG        NUMBER(1) not null
)
;
--create index idx_temp_auetaline_mod_log on TEMP_AU_PT_ETA_LINE_MOD_LOG (pid, audata_id);


-- Create table
create  table TEMP_PT_ETA_STOP  
  as select * from PT_ETA_STOP  where 1=2;

-- Create/Re--create indexes
--create index IDX_TMP_PT_ETA_STOP on TEMP_PT_ETA_STOP (POI_PID);

create  table temp_au_pt_eta_stop_mod_log
(
  PID                NUMBER(10),
  AUDATA_ID          NUMBER(10) not null  ,
  ETA_STOP_ALIAS_NAME            NUMBER(1),
  ETA_STOP_PRIVATE_PARK          NUMBER(1),
  ETA_STOP_P_PARK_PERIOD         NUMBER(1),
  ETA_STOP_CARPORT_EXACT         NUMBER(1),
  ETA_STOP_CARPORT_ESTIMATE      NUMBER(1),
  ETA_STOP_BIKE_PARK             NUMBER(1),
  ETA_STOP_B_PARK_PERIOD         NUMBER(1),
  ETA_STOP_MANUAL_TICKET         NUMBER(1),
  ETA_STOP_M_TICKET_PERIOD       NUMBER(1),
  ETA_STOP_MOBILE                NUMBER(1),
  ETA_STOP_BAGGAGE_SECURITY      NUMBER(1),
  ETA_STOP_LEFT_BAGGAGE          NUMBER(1),
  ETA_STOP_CONSIGNATION_EXACT    NUMBER(1),
  ETA_STOP_CONSIGNATION_ESTIMATE NUMBER(1),
  ETA_STOP_CONVENIENT            NUMBER(1),
  ETA_STOP_SMOKE                 NUMBER(1),
  ETA_STOP_BUILD_TYPE            NUMBER(1),
  ETA_STOP_AUTO_TICKET           NUMBER(1),
  ETA_STOP_TOILET                NUMBER(1),
  ETA_STOP_WIFI                  NUMBER(1),
  ETA_STOP_OPEN_PERIOD           NUMBER(1),
  ETA_STOP_FARE_AREA             NUMBER(1)

)
;
-- Create/Re--create indexes
--create index IDX_TMP_PTetastop_MODLOG_ADID on temp_au_pt_eta_stop_mod_log (AUDATA_ID,PID);


create  table TEMP_PT_ETA_ACCESS  
  as select * from PT_ETA_ACCESS   where 1=2;


--create index IDX_TMP_PT_ETA_ACCESS on TEMP_PT_ETA_ACCESS (POI_PID);
create  table temp_au_pt_eta_access_mod_log
(
  PID                NUMBER(10),
  AUDATA_ID          NUMBER(10) not null  ,
 ALIAS_NAME NUMBER(1),
ALIAS_PINYIN NUMBER(1),
OPEN_PERIOD NUMBER(1),
MANUAL_TICKET NUMBER(1),
MANUAL_TICKET_PERIOD NUMBER(1),
AUTO_TICKET NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_ETAACCESS_MODLOG_ADID on temp_au_pt_eta_access_mod_log (AUDATA_ID,PID);


create table temp_his_ix_poi_building as select * from ix_poi_building where 1=2;

-- Create table
create  table TEMP_AU_MARK_SPEEDLMT_MULTASK
(
  PID NUMBER(10)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AMS_MT on TEMP_AU_MARK_SPEEDLMT_MULTASK (PID);

-- Create table
create  table TEMP_AU_MARK_SPEEDLIMIT_GRP
(
  MARK_ID   NUMBER(10),
  PID       NUMBER(10),
  STATE     NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AMS_GP on TEMP_AU_MARK_SPEEDLIMIT_GRP (MARK_ID, PID);

-- Create table
create  table TEMP_AU_MARK_SPEEDLIMIT_LOG
(
  MARK_ID   NUMBER(10),
  PID       NUMBER(10),
  STATE     NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_AMS_LOG_MARKID on TEMP_AU_MARK_SPEEDLIMIT_LOG (MARK_ID);
--create index IDX_TMP_AMS_LOG_PID on TEMP_AU_MARK_SPEEDLIMIT_LOG (PID);

-- Create table
create  table TEMP_HIS_RD_SPEEDLIMIT 
as select * from rd_speedlimit where 1=2
;
-- Create/Re--create indexes
--create index IDX_TMP_HIS_RD_SPEEDLIMIT on TEMP_HIS_RD_SPEEDLIMIT (PID);

create  table TEMP_NEW_RD_SPEEDLIMIT 
as select * from rd_speedlimit where 1=2
;

alter table TEMP_NEW_RD_SPEEDLIMIT
  add constraint pk_tmp_new_rd_speedlimit primary key (PID);
  
alter table TEMP_NEW_RD_SPEEDLIMIT
  add (mark_id number(10) not null);
  
create  table TEMP_MULHIS_RD_SPEEDLIMIT 
as select * from rd_speedlimit where 1=2
;

alter table TEMP_MULHIS_RD_SPEEDLIMIT
  add constraint pk_tmp_mulhis_rd_speedlimit primary key (PID);
  
-- Create table
create  table TEMP_NEW_MARK_DIRECT
(
  MARK_ID    NUMBER(10),
  LINK_PID   NUMBER(10),
  ANGLE      NUMBER(8,5),
  DIRECT     NUMBER(1)
)
;
-- Create/Re--create indexes
--create index IDX_TMP_NEW_MARK_DIRECT on TEMP_NEW_MARK_DIRECT (MARK_ID);

