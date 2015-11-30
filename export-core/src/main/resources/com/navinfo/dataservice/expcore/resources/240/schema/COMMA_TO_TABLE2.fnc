CREATE OR REPLACE FUNCTION COMMA_TO_TABLE2(p_list IN clob) RETURN varchar_arr
  PIPELINED AS
  l_string      LONG ;--:= p_list || ',';

  l_comma_index PLS_INTEGER;
  l_index       PLS_INTEGER := 1;
  v_mesh_list   clob:=p_list;
BEGIN
  dbms_lob.writeappend(v_mesh_list,1,',');
  LOOP
    l_comma_index := dbms_lob.instr(v_mesh_list,',',l_index);--INSTR(l_string, ',', l_index);
    EXIT WHEN l_comma_index = 0;
    --PIPE ROW(SUBSTR(l_string, l_index, l_comma_index - l_index));
    PIPE ROW(dbms_lob.SUBSTR(v_mesh_list, l_comma_index - l_index, l_index));
    l_index := l_comma_index + 1;
  END LOOP;
  RETURN;
END COMMA_TO_TABLE2;
/
