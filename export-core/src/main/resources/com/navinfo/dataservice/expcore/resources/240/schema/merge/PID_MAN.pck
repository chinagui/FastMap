CREATE OR REPLACE PACKAGE PID_MAN IS

  -- AUTHOR  : LIUQING
  -- CREATED : 2011/1/4 19:12:11
  -- PURPOSE : pid申请

  TYPE split_type IS TABLE OF VARCHAR2(4000);

  type t_pid_seg is record
  (
   start_num number,
   end_num number
  );
  type t_pid_segs is table of t_pid_seg;
  
  type t_pid_info is record(
    table_name      varchar2(50),
    pid_current_val number,
    pid_max_val     number,
    pid_segs t_pid_segs,
    current_seg number);    
    
  type t_pid_map is table of t_pid_info index by varchar2(50);
  v_pid_map t_pid_map;

  FUNCTION split(p_str IN VARCHAR2, p_delimiter IN VARCHAR2 default (','))
    RETURN split_type;

  PROCEDURE APPLY_PID(P_NAME   IN VARCHAR2,
                      P_COUNT  IN NUMBER,
                      P_CLIENT IN VARCHAR2 := 'PID_MAN');

  --取当前PID
  FUNCTION PID_NEXTVAL RETURN NUMBER;

  function pid_nextval(p_table_name varchar2) return number;
  function pid_currentval(p_table_name varchar2) return number;
  procedure reset;

  PROCEDURE CLOSE_DBLINK;
  v_pid_seg_seqence varchar2(32767);
  function get_pid_segs return varchar2;
  function get_vm_task_id return varchar2;
END;

 
/
CREATE OR REPLACE PACKAGE BODY PID_MAN IS
 function get_vm_task_id return varchar2 is
   v_vm_task_id varchar2(100);
   begin
     execute immediate '
     select /*+result_cache*/ parameter   from  m_parameter t where t.name=''merge_vm_task_id'' and rownum=1' 
     into v_vm_task_id;
     return v_vm_task_id;
     exception when others then
     return null;
   end;
  -- AUTHOR  : LIUQING
  -- CREATED : 2011/12/20 9:12:11
  -- PURPOSE : 代理店数据导出和融合脚本

  --创建DBLINK ,然后申请pid
  --create public database link  dblink_dms connect to dms identified by dms using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = 192.168.3.107  )(PORT = 1521 )))(CONNECT_DATA = (SID =orcl  )))'
  --需要注意这个方法会不断到表中
  PROCEDURE APPLY_PID(P_NAME   IN VARCHAR2,
                      P_COUNT  IN NUMBER,
                      P_CLIENT IN VARCHAR2 := 'PID_MAN') IS
    V_PID_SEGMENT    VARCHAR2(32767);
    V_PID_SPLIT_TYPE SPLIT_TYPE;
    V_PID_START      NUMBER := 0;
    V_PID_END        NUMBER := 0;
    v_pid_info       t_pid_info;
    v_pid_seg        t_pid_seg;
    v_pid_segs       t_pid_segs := t_pid_segs();
    
    PRAGMA AUTONOMOUS_TRANSACTION;
  BEGIN
    /*v_table_name IN VARCHAR2, v_limit IN INTEGER, v_client IN VARCHAR2 DEFAULT '',
    p_continuous in varchar2 default 'no',
                      v_client_ip  in varchar2 default '',
                      v_client_task_id in varchar2 default ''*/
    
    V_PID_SEGMENT := DMS_PID_MAN.APPLY_PID@DBLINK_DMS(P_NAME,
                                                      P_COUNT,
                                                      P_CLIENT,'no','',get_vm_task_id);
    --V_PID_SEGMENT := '12,14,18,20,25,30';                                                                                                           
    DBMS_OUTPUT.PUT_LINE(V_PID_SEGMENT);
    v_pid_seg_seqence:=V_PID_SEGMENT;
    V_PID_SPLIT_TYPE := SPLIT(V_PID_SEGMENT);
    v_pid_info.table_name := P_NAME;
    
    v_pid_segs.extend(V_PID_SPLIT_TYPE.count/2);
    for i in 1.. V_PID_SPLIT_TYPE.count/2
    loop
        v_pid_seg.start_num := V_PID_SPLIT_TYPE(2*i - 1);
        v_pid_seg.end_num := V_PID_SPLIT_TYPE(2*i);
        v_pid_segs(i) := v_pid_seg;
    end loop;
    v_pid_info.pid_segs := v_pid_segs;
    v_pid_info.current_seg := 1;                        
    v_pid_info.pid_current_val := TO_NUMBER(V_PID_SPLIT_TYPE(1)) - 1;
    v_pid_info.pid_max_val := TO_NUMBER(V_PID_SPLIT_TYPE(2));
    
    v_pid_map(P_NAME) := v_pid_info;
  
    COMMIT;
  
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      RAISE;
  END;

  function pid_nextval(p_pid_info t_pid_info) return number is
    v_pid_info t_pid_info;
  begin
    v_pid_info := p_pid_info;
    v_pid_info.pid_current_val := v_pid_info.pid_current_val + 1;
    v_pid_map(v_pid_info.table_name) := v_pid_info;
    IF v_pid_info.pid_current_val > v_pid_info.pid_max_val 
    THEN
        if v_pid_info.current_seg < v_pid_info.pid_segs.count
        then
            v_pid_info.current_seg := v_pid_info.current_seg + 1;
            v_pid_info.pid_current_val := v_pid_info.pid_segs(v_pid_info.current_seg).start_num - 1;
            v_pid_info.pid_max_val := v_pid_info.pid_segs(v_pid_info.current_seg).end_num;
            v_pid_map(v_pid_info.table_name) := v_pid_info;
            return pid_nextval(v_pid_info);
        else
            v_pid_info.pid_current_val := v_pid_info.pid_current_val - 1;
            v_pid_map(v_pid_info.table_name) := v_pid_info;
            RAISE_APPLICATION_ERROR(-20999,
                                    v_pid_info.table_name || ' PID ' ||
                                    (v_pid_info.pid_current_val + 1) ||
                                    ' is out of range ' || v_pid_info.pid_max_val);
        end if;
    END IF;
    return v_pid_info.pid_current_val;
  end;

  function pid_currentval(p_pid_info t_pid_info) return number is
  begin
    return p_pid_info.pid_current_val;
  end;

  function pid_currentval(p_table_name varchar2) return number is
  begin
    if not v_pid_map.exists(p_table_name) then
      RAISE_APPLICATION_ERROR(-20999, '没有申请过PID，请先申请PID');
    end if;
    return pid_currentval(v_pid_map(p_table_name));
  end;

  function pid_nextval(p_table_name varchar2) return number is
  begin
    if not v_pid_map.exists(p_table_name) then
      RAISE_APPLICATION_ERROR(-20999, '没有申请过PID，请先申请PID');
    end if;
    return pid_nextval(v_pid_map(p_table_name));
  end;

  --取当前PID
  FUNCTION PID_NEXTVAL RETURN NUMBER IS
    v_pid_info t_pid_info;
  BEGIN
    if v_pid_map.count = 0 then
      RAISE_APPLICATION_ERROR(-20998, '没有申请过PID，请先申请PID');
    end if;
    v_pid_info := v_pid_map(v_pid_map.first);
    return pid_nextval(v_pid_info);
  END;

  FUNCTION SPLIT(p_str IN VARCHAR2, p_delimiter IN VARCHAR2 default (','))
    RETURN split_type IS
    j        INT := 0;
    i        INT := 1;
    len      INT := 0;
    len1     INT := 0;
    str      VARCHAR2(4000);
    my_split split_type := split_type();
  BEGIN
    len  := LENGTH(p_str);
    len1 := LENGTH(p_delimiter);
    WHILE j < len LOOP
      j := INSTR(p_str, p_delimiter, i);
      IF j = 0 THEN
        j   := len;
        str := SUBSTR(p_str, i);
        my_split.EXTEND;
        my_split(my_split.COUNT) := str;
        IF i >= len THEN
          EXIT;
        END IF;
      ELSE
        str := SUBSTR(p_str, i, j - i);
        i   := j + len1;
        my_split.EXTEND;
        my_split(my_split.COUNT) := str;
      END IF;
    END LOOP;
    RETURN my_split;
  END split;

  procedure reset is
  begin
    v_pid_map.delete;
  end;

  PROCEDURE CLOSE_DBLINK IS
  BEGIN
    DBMS_SESSION.close_database_link('DBLINK_DMS');
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.put_line('CLOSE DBLINK ERROR');
  END;
  FUNCTION get_pid_segs RETURN VARCHAR2 IS
  BEGIN
    RETURN v_pid_seg_seqence;
  END;

END;
/
