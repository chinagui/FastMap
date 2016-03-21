create or replace package PK_TABLE_STATS is

  -- Author  : MAYUNFEI
  -- Created : 2013/3/26 11:50:24
  -- Purpose : 收集指定表的统计信息
  
  procedure gather(v_table_name varchar2);

end PK_TABLE_STATS;
/
create or replace package body PK_TABLE_STATS is
 procedure gather(v_table_name varchar2) as
   v_tab varchar2(100);
   begin
      dbms_stats.gather_table_stats(USER,V_TABLE_NAME);
      exception
        when others then 
          null;--do nothing      
     end;
end PK_TABLE_STATS;
/
