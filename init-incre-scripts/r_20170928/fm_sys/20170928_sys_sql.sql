insert into DB_HUB t (t.db_id,t.db_user_name,t.db_user_passwd,t.db_role,t.tablespace_name,t.biz_type,t.server_id,t.db_status,t.descp) 
values(DB_SEQ.NEXTVAL,'gdb270_dcs_17sum_bj','gdb270_dcs_17sum_bj',0,'GDB_DATA','gen2AuWeek',2,2,'二代外业成果周库');

update DB_SERVER t set t.biz_type=(select t.biz_type from DB_SERVER t where t.server_id = 2)||',gen2AuWeek' where t.server_id = 2

commit; 
exit;



