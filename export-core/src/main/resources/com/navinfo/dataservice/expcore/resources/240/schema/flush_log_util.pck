create or replace package flush_log_util is

  function md5(i_str varchar2) return varchar2;

end flush_log_util;
/
create or replace package body flush_log_util is

  function MD5(i_str in varchar2) return varchar2 is
    retval varchar2(32);
  begin
    retval := utl_raw.cast_to_raw(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING => i_str));
    return retval;
  end;
end flush_log_util;
/
