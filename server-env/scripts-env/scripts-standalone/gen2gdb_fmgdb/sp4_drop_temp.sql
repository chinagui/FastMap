WHENEVER SQLERROR CONTINUE;
begin
  for a in (select a.table_name
              from user_tables a where a.table_name like 'TEMP_%'
            ) loop
    execute immediate 'drop table ' || a.table_name || ' cascade constraints';
  end loop;
end;
/

EXIT;
