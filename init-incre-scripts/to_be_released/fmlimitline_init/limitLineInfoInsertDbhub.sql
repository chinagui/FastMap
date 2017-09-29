WHENEVER SQLERROR CONTINUE;
UPDATE DB_SERVER SET BIZ_TYPE = 'desDayAll,desMon,desDayPoi,fmMan,dealership,fmCheck,fmRender,fmLimit' WHERE SERVER_IP='192.168.4.131';
COMMIT;

insert into db_hub (DB_ID, DB_NAME, DB_USER_NAME, DB_USER_PASSWD, DB_ROLE, TABLESPACE_NAME, BIZ_TYPE, SERVER_ID, GDB_VERSION, DB_STATUS, CREATE_TIME, DESCP) 
VALUES(
   DB_SEQ.NEXTVAL,NULL,'&1','&1',0,'GDB_DATA','&2',
   (SELECT server_id from db_server where biz_type like '%&2%' and rownum = 1),
   (SELECT CONF_VALUE FROM SYS_CONFIG WHERE CONF_KEY='gdb.version' and rownum=1),
   2,sysdate,'&2 db');
COMMIT;
EXIT;