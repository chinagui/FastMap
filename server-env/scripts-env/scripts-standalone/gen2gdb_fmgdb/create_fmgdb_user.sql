create user &1 identified by &2;
grant dba to &1;

grant connect,resource,dba to &1;
grant execute on dbms_lock to &1 with grant option;
grant CREATE ANY CONTEXT to &1 with admin option;
exit;