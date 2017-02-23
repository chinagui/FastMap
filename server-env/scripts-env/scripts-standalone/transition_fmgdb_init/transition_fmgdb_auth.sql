WHENEVER SQLERROR CONTINUE;
-- FM GDB+ part --
-- auth 
grant dba to &1;
grant connect,resource,dba to &1;
grant execute on dbms_lock to &1 with grant option;
grant CREATE ANY CONTEXT to &1 with admin option;

-- FM MonthDB part --
grant execute on dbms_crypto to &1;
GRANT SELECT ANY TABLE TO &1;

EXIT;
