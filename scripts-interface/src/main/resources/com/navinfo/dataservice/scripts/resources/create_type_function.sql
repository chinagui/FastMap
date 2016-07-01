
-- DROP TYPE VARCHAR_T
CREATE OR REPLACE TYPE "VARCHAR_TAB" AS TABLE OF VARCHAR2(128);
/
-- CREATE FUNCTION
CREATE OR REPLACE FUNCTION VARCHAR_TO_TABLE(p_list IN VARCHAR2) RETURN VARCHAR_TAB
  PIPELINED AS
  l_string      LONG := p_list || ',';
  l_comma_index PLS_INTEGER;
  l_index       PLS_INTEGER := 1;
BEGIN
  LOOP
    l_comma_index := INSTR(l_string, ',', l_index);
    EXIT WHEN l_comma_index = 0;
    PIPE ROW(SUBSTR(l_string, l_index, l_comma_index - l_index));
    l_index := l_comma_index + 1;
  END LOOP;
  RETURN;
END VARCHAR_TO_TABLE;
/

CREATE OR REPLACE FUNCTION CLOB_TO_TABLE(p_list IN clob) RETURN VARCHAR_TAB
  PIPELINED AS
  l_string      LONG ;
  l_comma_index PLS_INTEGER;
  l_index       PLS_INTEGER := 1;
  v_mesh_list   clob:=p_list;
BEGIN
  dbms_lob.writeappend(v_mesh_list,1,',');
  LOOP
    l_comma_index := dbms_lob.instr(v_mesh_list,',',l_index);
    EXIT WHEN l_comma_index = 0;
    PIPE ROW(dbms_lob.SUBSTR(v_mesh_list, l_comma_index - l_index, l_index));
    l_index := l_comma_index + 1;
  END LOOP;
  RETURN;
END CLOB_TO_TABLE;
/
