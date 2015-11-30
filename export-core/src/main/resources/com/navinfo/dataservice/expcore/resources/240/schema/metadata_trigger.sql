/*【目标】 为nwmap增量获取元数据库表变更数据，提供行级触发器；
   【触发器】请在各元数据库（通用、生产）中执行如下语句；
   【执行方式】：使用pl/sql developer
    1.file>new>command window;
    2.将下面的脚本黏贴执行
    --METADATA_MAINTAIN.Nextval
  【验证方法：trigger编译没有错误】
  【影响分析】  dms相关变更：
1）(190模型实现)元数据库表导入前，需要将导入表对应的触发器disable；导入完成后向mg_metadata_maintain写导入日志；
最后(或出现异常时)将触发器enable;这样保证表导入时，只写一条表导入的日志，而不是大量的行insert日志；
ALTER TRIGGER trig_sc_point_kind_rule ENABLE;--enable触发器
ALTER TRIGGER trig_sc_point_kind_rule disable;--disable触发器
2)(190模型实现)提取成果库完成后，复制元数据库完成后，需要在新复制的元数据库中创建下面的触发器；
3）(181模型实现)dms在实现元数据表导入时，需要采用新的mg_metadata_maintain表结构来写日志
*/
--SC_POINT_KIND_RULE
CREATE OR REPLACE TRIGGER trig_sc_point_kind_rule
  AFTER INSERT OR UPDATE OR DELETE ON sc_point_kind_rule
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('POI_KIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POI_KIND='||:new.POI_KIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POI_KIND='||:old.POI_KIND||'|';   END IF;
    IF updating('POI_KIND_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POI_KIND_NAME='||:new.POI_KIND_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POI_KIND_NAME='||:old.POI_KIND_NAME||'|';   END IF;
    IF updating('CHECK_RULE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHECK_RULE='||:new.CHECK_RULE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHECK_RULE='||:old.CHECK_RULE||'|';   END IF;
    IF updating('GOVERNMENT_LEVEL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'GOVERNMENT_LEVEL='||:new.GOVERNMENT_LEVEL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'GOVERNMENT_LEVEL='||:old.GOVERNMENT_LEVEL||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
	IF updating('CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN='||:new.CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN='||:old.CHAIN||'|';   END IF;
	IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_KIND_RULE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_KIND_RULE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_KIND_RULE', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_POICODE
CREATE OR REPLACE TRIGGER trig_SC_POINT_POICODE
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_POICODE
  FOR EACH ROW
  DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN
    IF updating('KIND_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KIND_CODE='||:new.KIND_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KIND_CODE='||:old.KIND_CODE||'|';   END IF;
    IF updating('KIND_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KIND_NAME='||:new.KIND_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KIND_NAME='||:old.KIND_NAME||'|';   END IF;
    IF updating('CLASS_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CLASS_CODE='||:new.CLASS_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CLASS_CODE='||:old.CLASS_CODE||'|';   END IF;
    IF updating('CLASS_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CLASS_NAME='||:new.CLASS_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CLASS_NAME='||:old.CLASS_NAME||'|';   END IF;
    IF updating('SUB_CLASS_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUB_CLASS_CODE='||:new.SUB_CLASS_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUB_CLASS_CODE='||:old.SUB_CLASS_CODE||'|';   END IF;
    IF updating('SUB_CLASS_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUB_CLASS_NAME='||:new.SUB_CLASS_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUB_CLASS_NAME='||:old.SUB_CLASS_NAME||'|';   END IF;
    IF updating('DESCRIPT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'DESCRIPT='||:new.DESCRIPT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'DESCRIPT='||:old.DESCRIPT||'|';   END IF;
    IF updating('MHM_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MHM_DES='||:new.MHM_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MHM_DES='||:old.MHM_DES||'|';   END IF;
    IF updating('KG_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_DES='||:new.KG_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_DES='||:old.KG_DES||'|';   END IF;
    IF updating('COL_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'COL_DES='||:new.COL_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'COL_DES='||:old.COL_DES||'|';   END IF;
    IF updating('FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'FLAG='||:new.FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'FLAG='||:old.FLAG||'|';   END IF;
    IF updating('LEVEL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'LEVEL='||:new.LEVEL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'LEVEL='||:old.LEVEL||'|';   END IF;
    IF updating('ICON_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ICON_NAME='||:new.ICON_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ICON_NAME='||:old.ICON_NAME||'|';   END IF;
    IF updating('U_RECODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'U_RECODE='||:new.U_RECODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'U_RECODE='||:old.U_RECODE||'|';   END IF;
    IF updating('U_FIELDS') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'U_FIELDS='||:new.U_FIELDS||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'U_FIELDS='||:old.U_FIELDS||'|';   END IF;
    
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_POICODE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_POICODE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_POICODE', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_KIND
CREATE OR REPLACE TRIGGER trig_SC_POINT_KIND
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_KIND
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';    
BEGIN
  IF updating THEN
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
IF updating('POIKIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND='||:new.POIKIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND='||:old.POIKIND||'|';   END IF;
IF updating('R_KIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND='||:new.R_KIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND='||:old.R_KIND||'|';   END IF;
IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
IF updating('EQUAL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'EQUAL='||:new.EQUAL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'EQUAL='||:old.EQUAL||'|';   END IF;
IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_KIND',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_KIND', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_KIND', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_CHAIN_BRAND_KEY
CREATE OR REPLACE TRIGGER trig_SC_POINT_CHAIN_BRAND_KEY
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_CHAIN_BRAND_KEY
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
  IF updating THEN
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('PRE_KEY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PRE_KEY='||:new.PRE_KEY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PRE_KEY='||:old.PRE_KEY||'|';   END IF;
    IF updating('CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN='||:new.CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN='||:old.CHAIN||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_CHAIN_BRAND_KEY',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_CHAIN_BRAND_KEY', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_CHAIN_BRAND_KEY', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_FOODTYPE
CREATE OR REPLACE TRIGGER trig_SC_POINT_FOODTYPE
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_FOODTYPE
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
 IF updating THEN
    IF updating('POIKIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND='||:new.POIKIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND='||:old.POIKIND||'|';   END IF;
    IF updating('FOODTYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'FOODTYPE='||:new.FOODTYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'FOODTYPE='||:old.FOODTYPE||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
    IF updating('FOODTYPENAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'FOODTYPENAME='||:new.FOODTYPENAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'FOODTYPENAME='||:old.FOODTYPENAME||'|';   END IF;
	IF updating('CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN='||:new.CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN='||:old.CHAIN||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_FOODTYPE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_FOODTYPE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_FOODTYPE', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_BRAND_FOODTYPE
CREATE OR REPLACE TRIGGER trig_SC_POINT_BRAND_FOODTYPE
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_BRAND_FOODTYPE
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
  IF updating THEN
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('CHI_KEY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHI_KEY='||:new.CHI_KEY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHI_KEY='||:old.CHI_KEY||'|';   END IF;
    IF updating('POIKIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND='||:new.POIKIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND='||:old.POIKIND||'|';   END IF;
    IF updating('CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN='||:new.CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN='||:old.CHAIN||'|';   END IF;
    IF updating('FOODTYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'FOODTYPE='||:new.FOODTYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'FOODTYPE='||:old.FOODTYPE||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_BRAND_FOODTYPE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_BRAND_FOODTYPE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_BRAND_FOODTYPE', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_NAMECK
CREATE OR REPLACE TRIGGER trig_SC_POINT_NAMECK
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_NAMECK
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
  IF updating THEN
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('PRE_KEY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PRE_KEY='||:new.PRE_KEY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PRE_KEY='||:old.PRE_KEY||'|';   END IF;
    IF updating('RESULT_KEY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'RESULT_KEY='||:new.RESULT_KEY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'RESULT_KEY='||:old.RESULT_KEY||'|';   END IF;
    IF updating('REF_KEY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'REF_KEY='||:new.REF_KEY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'REF_KEY='||:old.REF_KEY||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
	IF updating('CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN='||:new.CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN='||:old.CHAIN||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_NAMECK',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_NAMECK', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_NAMECK', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_SPEC_KINDCODE
CREATE OR REPLACE TRIGGER trig_SC_POINT_SPEC_KINDCODE
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_SPEC_KINDCODE
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
  IF updating THEN
    IF updating('POI_KIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POI_KIND='||:new.POI_KIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POI_KIND='||:old.POI_KIND||'|';   END IF;
    IF updating('POI_KIND_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POI_KIND_NAME='||:new.POI_KIND_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POI_KIND_NAME='||:old.POI_KIND_NAME||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_SPEC_KINDCODE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_SPEC_KINDCODE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_SPEC_KINDCODE', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_ADMINAREA
CREATE OR REPLACE TRIGGER trig_SC_POINT_ADMINAREA
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_ADMINAREA
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
   IF updating THEN
    IF updating('ADMINAREACODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ADMINAREACODE='||:new.ADMINAREACODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ADMINAREACODE='||:old.ADMINAREACODE||'|';   END IF;
    IF updating('PROVINCE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PROVINCE='||:new.PROVINCE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PROVINCE='||:old.PROVINCE||'|';   END IF;
    IF updating('PROVINCE_SHORT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PROVINCE_SHORT='||:new.PROVINCE_SHORT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PROVINCE_SHORT='||:old.PROVINCE_SHORT||'|';   END IF;
    IF updating('CITY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CITY='||:new.CITY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CITY='||:old.CITY||'|';   END IF;
    IF updating('CITY_SHORT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CITY_SHORT='||:new.CITY_SHORT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CITY_SHORT='||:old.CITY_SHORT||'|';   END IF;
    IF updating('AREACODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'AREACODE='||:new.AREACODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'AREACODE='||:old.AREACODE||'|';   END IF;
    IF updating('DISTRICT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'DISTRICT='||:new.DISTRICT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'DISTRICT='||:old.DISTRICT||'|';   END IF;
    IF updating('DISTRICT_SHORT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'DISTRICT_SHORT='||:new.DISTRICT_SHORT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'DISTRICT_SHORT='||:old.DISTRICT_SHORT||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('WHOLE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'WHOLE='||:new.WHOLE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'WHOLE='||:old.WHOLE||'|';   END IF;
    IF updating('POSTCODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POSTCODE='||:new.POSTCODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POSTCODE='||:old.POSTCODE||'|';   END IF;
    IF updating('PHONENUM_LEN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PHONENUM_LEN='||:new.PHONENUM_LEN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PHONENUM_LEN='||:old.PHONENUM_LEN||'|';   END IF;
    IF updating('WHOLE_PY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'WHOLE_PY='||:new.WHOLE_PY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'WHOLE_PY='||:old.WHOLE_PY||'|';   END IF;
    IF updating('REMARK') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'REMARK='||:new.REMARK||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'REMARK='||:old.REMARK||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_ADMINAREA',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_ADMINAREA', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_ADMINAREA', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_KIND_ENG_RULE
CREATE OR REPLACE TRIGGER trig_SC_POINT_KIND_ENG_RULE
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_KIND_ENG_RULE
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
   IF updating THEN
      IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
      IF updating('R_KIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND='||:new.R_KIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND='||:old.R_KIND||'|';   END IF;
      IF updating('ENG_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ENG_NAME='||:new.ENG_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ENG_NAME='||:old.ENG_NAME||'|';   END IF;
      IF updating('CHI_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHI_NAME='||:new.CHI_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHI_NAME='||:old.CHI_NAME||'|';   END IF;
      IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
      IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
      IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
      IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_KIND_ENG_RULE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_KIND_ENG_RULE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_KIND_ENG_RULE', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_ADDRCK
CREATE OR REPLACE TRIGGER trig_SC_POINT_ADDRCK
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_ADDRCK
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
  IF updating THEN
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('PRE_KEY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PRE_KEY='||:new.PRE_KEY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PRE_KEY='||:old.PRE_KEY||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_ADDRCK',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_ADDRCK', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_ADDRCK', :new.rowid,sysdate;
  END IF;
END;
/


--SC_RTIC_CODE
CREATE OR REPLACE TRIGGER trig_SC_RTIC_CODE 
  AFTER INSERT OR UPDATE OR DELETE ON SC_RTIC_CODE  
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
      IF updating('MESH') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MESH='||:new.MESH||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MESH='||:old.MESH||'|';   END IF;
      IF updating('CLASS') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CLASS='||:new.CLASS||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CLASS='||:old.CLASS||'|';   END IF;
      IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
      IF updating('STATE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'STATE='||:new.STATE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'STATE='||:old.STATE||'|';   END IF;
      IF updating('SEASON') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SEASON='||:new.SEASON||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SEASON='||:old.SEASON||'|';   END IF;

    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_RTIC_CODE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_RTIC_CODE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_RTIC_CODE', :old.rowid,sysdate;
  END IF;
END;
/




--SC_POINT_CHAIN_CODE
CREATE OR REPLACE TRIGGER trig_SC_POINT_CHAIN_CODE 
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_CHAIN_CODE  
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
      IF updating('CHAIN_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN_NAME='||:new.CHAIN_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN_NAME='||:old.CHAIN_NAME||'|';   END IF;
      IF updating('CHAIN_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN_CODE='||:new.CHAIN_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN_CODE='||:old.CHAIN_CODE||'|';   END IF;
      IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
      IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
      IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
	    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
	    IF updating('CHAIN_NAME_CHT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN_NAME_CHT='||:new.CHAIN_NAME_CHT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN_NAME_CHT='||:old.CHAIN_NAME_CHT||'|';   END IF;
	    IF updating('CHAIN_NAME_ENG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN_NAME_ENG='||:new.CHAIN_NAME_ENG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN_NAME_ENG='||:old.CHAIN_NAME_ENG||'|';   END IF;
      IF updating('CATEGORY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CATEGORY='||:new.CATEGORY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CATEGORY='||:old.CATEGORY||'|';   END IF;
      IF updating('WEIGHT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'WEIGHT='||:new.WEIGHT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'WEIGHT='||:old.WEIGHT||'|';   END IF;

    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_CHAIN_CODE',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_CHAIN_CODE', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_CHAIN_CODE', :old.rowid,sysdate;
  END IF;
END;
/


--SC_POINT_LAND_CITY_TOWN
CREATE OR REPLACE TRIGGER trig_SC_POINT_LAND_CITY_TOWN
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_LAND_CITY_TOWN
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
      IF updating('MESH_ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MESH_ID='||:new.MESH_ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MESH_ID='||:old.MESH_ID||'|';   END IF;
      IF updating('CITY_LEVEL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CITY_LEVEL='||:new.CITY_LEVEL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CITY_LEVEL='||:old.CITY_LEVEL||'|';   END IF;

    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_LAND_CITY_TOWN',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_LAND_CITY_TOWN', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_LAND_CITY_TOWN', :old.rowid,sysdate;
  END IF;
END;
/



--SC_POINT_LAND_PICKUP
CREATE OR REPLACE TRIGGER trig_SC_POINT_LAND_PICKUP
  AFTER INSERT OR UPDATE OR DELETE ON SC_POINT_LAND_PICKUP
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
      IF updating('Annkind') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Annkind='||:new.Annkind||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Annkind='||:old.Annkind||'|';   END IF;
      IF updating('AnnkindName') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'AnnkindName='||:new.AnnkindName||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'AnnkindName='||:old.AnnkindName||'|';   END IF;

      IF updating('Poikind') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Poikind='||:new.Poikind||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Poikind='||:old.Poikind||'|';   END IF;
      IF updating('PoikindName') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PoikindName='||:new.PoikindName||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PoikindName='||:old.PoikindName||'|';   END IF;
      IF updating('Equal') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Equal='||:new.Equal||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Equal='||:old.Equal||'|';   END IF;
      IF updating('Extract_rule') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Extract_rule='||:new.Extract_rule||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Extract_rule='||:old.Extract_rule||'|';   END IF;
      IF updating('Ex_erea') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Ex_erea='||:new.Ex_erea||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Ex_erea='||:old.Ex_erea||'|';   END IF;
      IF updating('Ex_seq1') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Ex_seq1='||:new.Ex_seq1||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Ex_seq1='||:old.Ex_seq1||'|';   END IF;
      IF updating('Ex_seq2') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Ex_seq2='||:new.Ex_seq2||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Ex_seq2='||:old.Ex_seq2||'|';   END IF;
      IF updating('Ex_Priority') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Ex_Priority='||:new.Ex_Priority||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Ex_Priority='||:old.Ex_Priority||'|';   END IF;
      IF updating('Memo') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'Memo='||:new.Memo||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'Memo='||:old.Memo||'|';   END IF;

    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_LAND_PICKUP',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_LAND_PICKUP', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_LAND_PICKUP', :old.rowid,sysdate;
  END IF;
END;
/


--RD_NAME
CREATE OR REPLACE TRIGGER trig_RD_NAME
  AFTER INSERT OR UPDATE OR DELETE ON RD_NAME
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';     
BEGIN
   IF updating THEN
    IF updating('ROAD_TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ROAD_TYPE='||:new.ROAD_TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ROAD_TYPE='||:old.ROAD_TYPE||'|';   END IF;
    IF updating('ADMIN_ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ADMIN_ID='||:new.ADMIN_ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ADMIN_ID='||:old.ADMIN_ID||'|';   END IF;
    IF updating('CODE_TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CODE_TYPE='||:new.CODE_TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CODE_TYPE='||:old.CODE_TYPE||'|';   END IF;
    IF updating('VOICE_FILE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'VOICE_FILE='||:new.VOICE_FILE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'VOICE_FILE='||:old.VOICE_FILE||'|';   END IF;
    IF updating('SRC_RESUME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SRC_RESUME='||:new.SRC_RESUME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SRC_RESUME='||:old.SRC_RESUME||'|';   END IF;
    IF updating('PA_REGION_ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PA_REGION_ID='||:new.PA_REGION_ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PA_REGION_ID='||:old.PA_REGION_ID||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
    IF updating('ROUTE_ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ROUTE_ID='||:new.ROUTE_ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ROUTE_ID='||:old.ROUTE_ID||'|';   END IF;
    IF updating('U_RECORD') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'U_RECORD='||:new.U_RECORD||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'U_RECORD='||:old.U_RECORD||'|';   END IF;
    IF updating('U_FIELDS') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'U_FIELDS='||:new.U_FIELDS||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'U_FIELDS='||:old.U_FIELDS||'|';   END IF;
    IF updating('NAME_ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'NAME_ID='||:new.NAME_ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'NAME_ID='||:old.NAME_ID||'|';   END IF;
    IF updating('NAME_GROUPID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'NAME_GROUPID='||:new.NAME_GROUPID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'NAME_GROUPID='||:old.NAME_GROUPID||'|';   END IF;
    IF updating('LANG_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'LANG_CODE='||:new.LANG_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'LANG_CODE='||:old.LANG_CODE||'|';   END IF;
    IF updating('NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'NAME='||:new.NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'NAME='||:old.NAME||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('BASE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'BASE='||:new.BASE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'BASE='||:old.BASE||'|';   END IF;
    IF updating('PREFIX') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PREFIX='||:new.PREFIX||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PREFIX='||:old.PREFIX||'|';   END IF;
    IF updating('INFIX') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'INFIX='||:new.INFIX||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'INFIX='||:old.INFIX||'|';   END IF;
    IF updating('SUFFIX') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUFFIX='||:new.SUFFIX||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUFFIX='||:old.SUFFIX||'|';   END IF;
    IF updating('NAME_PHONETIC') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'NAME_PHONETIC='||:new.NAME_PHONETIC||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'NAME_PHONETIC='||:old.NAME_PHONETIC||'|';   END IF;
    IF updating('TYPE_PHONETIC') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE_PHONETIC='||:new.TYPE_PHONETIC||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE_PHONETIC='||:old.TYPE_PHONETIC||'|';   END IF;
    IF updating('BASE_PHONETIC') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'BASE_PHONETIC='||:new.BASE_PHONETIC||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'BASE_PHONETIC='||:old.BASE_PHONETIC||'|';   END IF;
    IF updating('PREFIX_PHONETIC') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'PREFIX_PHONETIC='||:new.PREFIX_PHONETIC||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'PREFIX_PHONETIC='||:old.PREFIX_PHONETIC||'|';   END IF;
    IF updating('INFIX_PHONETIC') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'INFIX_PHONETIC='||:new.INFIX_PHONETIC||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'INFIX_PHONETIC='||:old.INFIX_PHONETIC||'|';   END IF;
    IF updating('SUFFIX_PHONETIC') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUFFIX_PHONETIC='||:new.SUFFIX_PHONETIC||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUFFIX_PHONETIC='||:old.SUFFIX_PHONETIC||'|';   END IF;
    IF updating('SRC_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SRC_FLAG='||:new.SRC_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SRC_FLAG='||:old.SRC_FLAG||'|';   END IF;
   execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'RD_NAME',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'RD_NAME', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'RD_NAME', :new.rowid,sysdate;
  END IF;
END;
/

--SC_POINT_KIND_NEW
CREATE OR REPLACE TRIGGER trig_sc_point_kind_new
  AFTER INSERT OR UPDATE OR DELETE ON sc_point_kind_new
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('POIKIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND='||:new.POIKIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND='||:old.POIKIND||'|';   END IF;
    IF updating('POIKIND_CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND_CHAIN='||:new.POIKIND_CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND_CHAIN='||:old.POIKIND_CHAIN||'|';   END IF;
    IF updating('POIKIND_RATING') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND_RATING='||:new.POIKIND_RATING||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND_RATING='||:old.POIKIND_RATING||'|';   END IF;
    IF updating('POIKIND_FLAGCODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND_FLAGCODE='||:new.POIKIND_FLAGCODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND_FLAGCODE='||:old.POIKIND_FLAGCODE||'|';   END IF;
    IF updating('POIKIND_CATEGORY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POIKIND_CATEGORY='||:new.POIKIND_CATEGORY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POIKIND_CATEGORY='||:old.POIKIND_CATEGORY||'|';   END IF;
    IF updating('R_KIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND='||:new.R_KIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND='||:old.R_KIND||'|';   END IF;
    IF updating('R_KIND_CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND_CHAIN='||:new.R_KIND_CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND_CHAIN='||:old.R_KIND_CHAIN||'|';   END IF;
    IF updating('R_KIND_RATING') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND_RATING='||:new.R_KIND_RATING||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND_RATING='||:old.R_KIND_RATING||'|';   END IF;
    IF updating('R_KIND_FLAGCODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND_FLAGCODE='||:new.R_KIND_FLAGCODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND_FLAGCODE='||:old.R_KIND_FLAGCODE||'|';   END IF;
    IF updating('R_KIND_CATEGORY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'R_KIND_CATEGORY='||:new.R_KIND_CATEGORY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'R_KIND_CATEGORY='||:old.R_KIND_CATEGORY||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('EQUAL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'EQUAL='||:new.EQUAL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'EQUAL='||:old.EQUAL||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_KIND_NEW',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_KIND_NEW', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_KIND_NEW', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_POICODE_NEW
CREATE OR REPLACE TRIGGER trig_sc_point_poicode_new
  AFTER INSERT OR UPDATE OR DELETE ON sc_point_poicode_new
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
    IF updating('KIND_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KIND_CODE='||:new.KIND_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KIND_CODE='||:old.KIND_CODE||'|';   END IF;
    IF updating('KIND_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KIND_NAME='||:new.KIND_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KIND_NAME='||:old.KIND_NAME||'|';   END IF;
    IF updating('CLASS_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CLASS_CODE='||:new.CLASS_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CLASS_CODE='||:old.CLASS_CODE||'|';   END IF;
    IF updating('CLASS_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CLASS_NAME='||:new.CLASS_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CLASS_NAME='||:old.CLASS_NAME||'|';   END IF;
    IF updating('SUB_CLASS_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUB_CLASS_CODE='||:new.SUB_CLASS_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUB_CLASS_CODE='||:old.SUB_CLASS_CODE||'|';   END IF;
    IF updating('SUB_CLASS_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUB_CLASS_NAME='||:new.SUB_CLASS_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUB_CLASS_NAME='||:old.SUB_CLASS_NAME||'|';   END IF;
    IF updating('ICON_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ICON_NAME='||:new.ICON_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ICON_NAME='||:old.ICON_NAME||'|';   END IF;
    IF updating('DESCRIPT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'DESCRIPT='||:new.DESCRIPT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'DESCRIPT='||:old.DESCRIPT||'|';   END IF;
    IF updating('U_RECODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'U_RECODE='||:new.U_RECODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'U_RECODE='||:old.U_RECODE||'|';   END IF;
    IF updating('U_FIELDS') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'U_FIELDS='||:new.U_FIELDS||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'U_FIELDS='||:old.U_FIELDS||'|';   END IF;
    IF updating('MHM_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MHM_DES='||:new.MHM_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MHM_DES='||:old.MHM_DES||'|';   END IF;
    IF updating('KG_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_DES='||:new.KG_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_DES='||:old.KG_DES||'|';   END IF;
    IF updating('COL_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'COL_DES='||:new.COL_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'COL_DES='||:old.COL_DES||'|';   END IF;
    IF updating('FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'FLAG='||:new.FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'FLAG='||:old.FLAG||'|';   END IF;
    IF updating('LEVEL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'LEVEL='||:new.LEVEL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'LEVEL='||:old.LEVEL||'|';   END IF;
	IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_POICODE_NEW',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_POICODE_NEW', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_POICODE_NEW', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_LAND_CODE_NEW
CREATE OR REPLACE TRIGGER trig_sc_point_land_code_new
  AFTER INSERT OR UPDATE OR DELETE ON sc_point_land_code_new
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
    IF updating('KIND_CODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KIND_CODE='||:new.KIND_CODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KIND_CODE='||:old.KIND_CODE||'|';   END IF;
    IF updating('KIND_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KIND_NAME='||:new.KIND_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KIND_NAME='||:old.KIND_NAME||'|';   END IF;
    IF updating('CLASS_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CLASS_NAME='||:new.CLASS_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CLASS_NAME='||:old.CLASS_NAME||'|';   END IF;
    IF updating('SUB_CLASS_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'SUB_CLASS_NAME='||:new.SUB_CLASS_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'SUB_CLASS_NAME='||:old.SUB_CLASS_NAME||'|';   END IF;
    IF updating('ICON_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ICON_NAME='||:new.ICON_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ICON_NAME='||:old.ICON_NAME||'|';   END IF;
    IF updating('DESCRIPT') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'DESCRIPT='||:new.DESCRIPT||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'DESCRIPT='||:old.DESCRIPT||'|';   END IF;
    IF updating('LEVEL') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'LEVEL='||:new.LEVEL||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'LEVEL='||:old.LEVEL||'|';   END IF;
    IF updating('DEALER_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'DEALER_FLAG='||:new.DEALER_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'DEALER_FLAG='||:old.DEALER_FLAG||'|';   END IF;
    IF updating('MHM_DES') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MHM_DES='||:new.MHM_DES||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MHM_DES='||:old.MHM_DES||'|';   END IF;
    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_LAND_CODE_NEW',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_LAND_CODE_NEW', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_LAND_CODE_NEW', :new.rowid,sysdate;
  END IF;
END;
/
--SC_POINT_SPEC_KINDCODE_NEW
CREATE OR REPLACE TRIGGER trig_sc_point_spec_kc_new
  AFTER INSERT OR UPDATE OR DELETE ON sc_point_spec_kindcode_new
  FOR EACH ROW
DECLARE 
 V_COLS_UPDATED_OLD VARCHAR2(32767):='';
 V_COLS_UPDATED_NEW VARCHAR2(32767):='';
BEGIN
  IF updating THEN   
    IF updating('ID') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'ID='||:new.ID||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'ID='||:old.ID||'|';   END IF;
    IF updating('POI_KIND') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POI_KIND='||:new.POI_KIND||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POI_KIND='||:old.POI_KIND||'|';   END IF;
    IF updating('POI_KIND_NAME') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'POI_KIND_NAME='||:new.POI_KIND_NAME||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'POI_KIND_NAME='||:old.POI_KIND_NAME||'|';   END IF;
    IF updating('CHAIN') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CHAIN='||:new.CHAIN||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CHAIN='||:old.CHAIN||'|';   END IF;
    IF updating('RATING') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'RATING='||:new.RATING||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'RATING='||:old.RATING||'|';   END IF;
    IF updating('FLAGCODE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'FLAGCODE='||:new.FLAGCODE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'FLAGCODE='||:old.FLAGCODE||'|';   END IF;
    IF updating('CATEGORY') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'CATEGORY='||:new.CATEGORY||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'CATEGORY='||:old.CATEGORY||'|';   END IF;
    IF updating('TYPE') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'TYPE='||:new.TYPE||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'TYPE='||:old.TYPE||'|';   END IF;
    IF updating('KG_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'KG_FLAG='||:new.KG_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'KG_FLAG='||:old.KG_FLAG||'|';   END IF;
    IF updating('HM_FLAG') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'HM_FLAG='||:new.HM_FLAG||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'HM_FLAG='||:old.HM_FLAG||'|';   END IF;
    IF updating('MEMO') THEN  V_COLS_UPDATED_new:=V_COLS_UPDATED_new||'MEMO='||:new.MEMO||'|'; V_COLS_UPDATED_old:=V_COLS_UPDATED_old||'MEMO='||:old.MEMO||'|';   END IF;
    execute immediate 'INSERT INTO mg_metadata_maintain
      (serial_id,
       change_type,
       tablename,
       table_rowid,
       before_update,
       after_update,
       main_date
       )
    VALUES(:1,:2,:3,:4,:5,:6,:7)'using metadata_maintain.nextval,
       13,
       'SC_POINT_SPEC_KINDCODE_NEW',
       :old.rowid,
       V_COLS_UPDATED_OLD,
       V_COLS_UPDATED_NEW,
       SYSDATE;   
  END IF;
  IF inserting THEN
    execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,11,'SC_POINT_SPEC_KINDCODE_NEW', :new.rowid,sysdate;  
  END IF;
  IF deleting THEN
     execute immediate 'INSERT INTO MG_METADATA_MAINTAIN
    (SERIAL_ID,CHANGE_TYPE,TABLENAME,TABLE_ROWID,MAIN_DATE)
    VALUES(:1,:2,:3,:4,:5)' using METADATA_MAINTAIN.Nextval,12,'SC_POINT_SPEC_KINDCODE_NEW', :new.rowid,sysdate;
  END IF;
END;
/
/*--truncate触发器，metadata-change的类型为4：清除
CREATE OR REPLACE TRIGGER trig_table_truncate
  BEFORE truncate ON SCHEMA
BEGIN
  IF ora_dict_obj_name IN ('SC_POINT_KIND_RULE',
                           'SC_POINT_POICODE',
                           'SC_POINT_KIND',
                           'SC_POINT_CHAIN_BRAND_KEY',
                           'SC_POINT_FOODTYPE',
                           'SC_POINT_BRAND_FOODTYPE',
                           'SC_POINT_NAMECK',
                           'SC_POINT_SPEC_KINDCODE',
                           'SC_POINT_ADMINAREA',
                           'SC_POINT_KIND_ENG_RULE',
                           'SC_POINT_ADDR_ADMIN',
                           'SC_POINT_ADDRCK',
                           'RD_NAME') THEN
    INSERT INTO MG_METADATA_MAINTAIN
    VALUES
      (ora_dict_obj_name, NULL, 4, SYSDATE);  
  END IF;
END;
/*/
/*


--测试
select * from 　SC_POINT_KIND_RULE where id=999999;
insert into SC_POINT_KIND_RULE values(999999,999,'test',4,'','','','');
select * from MG_METADATA_MAINTAIN;
update SC_POINT_KIND_RULE t set t.poi_kind=9,check_rule=9 where t.id=999999;
delete from SC_POINT_KIND_RULE where id=999999;

truncate table MG_METADATA_MAINTAIN;


create table test_myf1(id number(1));
truncate table test_myf;*/
