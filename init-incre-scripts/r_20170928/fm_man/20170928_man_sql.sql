insert into DB_HUB t (t.db_id,t.db_user_name,t.db_user_passwd,t.db_role,t.tablespace_name,t.biz_type,t.server_id,t.db_status,t.descp) 
values(DB_SEQ.NEXTVAL,'gdb270_dcs_17sum_bj','gdb270_dcs_17sum_bj',0,'GDB_DATA','gen2AuWeek',2,2,'二代外业成果周库')

update DB_SERVER t set t.biz_type=(select t.biz_type from DB_SERVER t where t.server_id = 2)||',gen2AuWeek' where t.server_id = 2






alter table  TASK add (upload_Method varchar2(50));
comment on column TASK.upload_Method is '更新方式';


alter table task add (geometry SDO_GEOMETRY);
INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('task',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_task ON task(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;
 
 commit; 
 exit;