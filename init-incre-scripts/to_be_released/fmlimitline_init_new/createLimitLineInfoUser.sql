-- Create the user 
create user &1 identified by &1
default tablespace GDB_DATA;  
   

grant connect,resource to &1;
grant create any sequence to &1;
grant create any table to &1;
grant delete any table to &1;
grant insert any table to &1;
grant select any table to &1;
grant unlimited tablespace to &1;
grant execute any procedure to &1;
grant update any table to &1;
grant create any view to &1;

commit;
exit;
