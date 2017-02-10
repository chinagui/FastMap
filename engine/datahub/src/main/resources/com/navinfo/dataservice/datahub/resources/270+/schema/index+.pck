declare
  v_cnt number := to_number(to_char(sysdate, 'yyyymmddhh24miss'));
begin
  for a in (select table_name
              from user_tab_cols a
             where column_name = 'ROW_ID' and a.TABLE_NAME in (SELECT TABLE_NAME FROM USER_TABLES) AND A.TABLE_NAME NOT IN('LOG_DETAIL')
            ) loop
  	
    execute immediate 'create unique index idx_' || v_cnt ||
                      '_r on ' || a.table_name || '(row_id)';
  
    v_cnt := v_cnt + 1;
  end loop;
end;
/

begin
  for a in (select b.*
              from user_constraints a, user_cons_columns b
             where a.constraint_type = 'R'
               and a.CONSTRAINT_NAME = b.CONSTRAINT_NAME
               and not exists
             (select null
                      from user_ind_columns c
                     where c.TABLE_NAME = b.TABLE_NAME
                       and c.COLUMN_NAME = b.COLUMN_NAME)) loop
    if length(a.constraint_name) > 26 then
      execute immediate 'create index idx_' || substr(a.constraint_name, length(a.constraint_name) - 25) ||
                        ' on ' || a.table_name || '(' || a.column_name || ')';
    else
      execute immediate 'create index idx_' || a.constraint_name || ' on ' ||
                        a.table_name || '(' || a.column_name || ')';
    end if;
  end loop;
  
  
end;
/

BEGIN
  FOR A IN (SELECT A.TABLE_NAME,A.CONSTRAINT_NAME FROM USER_CONSTRAINTS A WHERE A.CONSTRAINT_TYPE= 'R') LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE '|| A.TABLE_NAME ||' DISABLE CONSTRAINT ' || A.CONSTRAINT_NAME ;
  END LOOP;
END;
/