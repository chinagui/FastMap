WHENEVER SQLERROR CONTINUE;
INSERT INTO DB_HUB
  (DB_ID,
   DB_NAME,
   DB_USER_NAME,
   DB_USER_PASSWD,
   DB_ROLE,
   TABLESPACE_NAME,
   BIZ_TYPE,
   SERVER_ID,GDB_VERSION,DB_STATUS,CREATE_TIME,DESCP)
   VALUES(
   DB_SEQ.NEXTVAL,NULL,'&1','&2',0,'users','fmMan',
   (SELECT server_id from db_server where biz_type like '%fmMan%' and rownum = 1),
   (SELECT CONF_VALUE FROM SYS_CONFIG WHERE CONF_KEY='gdb.version' and rownum=1),
   2,sysdate,'fm man db');
COMMIT;
EXIT;
