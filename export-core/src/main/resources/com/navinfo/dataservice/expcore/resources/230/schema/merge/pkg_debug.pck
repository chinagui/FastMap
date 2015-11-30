create or replace package pkg_debug authid current_user

 is
  procedure count_table (v_tablename varchar2);
  procedure print_table (v_sql varchar2);
end pkg_debug;
/
create or replace package body pkg_debug is
  procedure count_table (v_tablename varchar2)is
    v_num number(10);
    begin
      execute immediate 'select count(1) from '||v_tablename into v_num;
        logger.info(v_tablename||' count:'||v_num);
      end;
  procedure print_table (v_sql varchar2)is
        PRAGMA AUTONOMOUS_TRANSACTION;
    begin
        execute immediate v_sql;
    end;
end pkg_debug;
/
