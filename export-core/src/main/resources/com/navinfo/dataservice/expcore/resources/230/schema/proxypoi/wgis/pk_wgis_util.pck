create or replace package pk_wgis_util is

  -- Author  : ARNOLD
  -- Created : 2010-10-27 13:50:43
  -- Purpose :
C_TOLERANCE CONSTANT NUMBER := 0.5;
C_SRID constant int := 8307;
G_ITYPE_NAME VARCHAR2(20) := 'SPATIAL_INDEX';  

procedure create_index
(
 p_tables varchar2,
 p_cols varchar2
);

procedure create_meta
(
 p_tables varchar2,
 p_cols varchar2
);

procedure drop_table_quiet
(
 p_table_name varchar2
);

procedure add_pk
(
 p_table_name varchar2,
 p_col_name varchar2
);


end pk_wgis_util;
/
create or replace package body pk_wgis_util is

procedure drop_spatial_index
(
  p_table varchar2
)
is
v_index_name varchar2(100);
begin
     select p.index_name into v_index_name 
     from user_indexes p 
     where p.table_name = upper(p_table)
     and p.ityp_name = G_ITYPE_NAME;
     
     execute immediate 'drop index '|| v_index_name;
     
 exception
    when others then
    dbms_output.put_line(Sqlerrm); 
end;

procedure create_table_meta
(
 p_table varchar2,
 p_cloumn varchar2
)
is
  diminfo mdsys.sdo_dim_array;
begin
  diminfo := mdsys.sdo_dim_array(
  mdsys.sdo_dim_element('XLONG', -180, 180, C_TOLERANCE),
  mdsys.sdo_dim_element('YLAT', -90, 90, C_TOLERANCE));

  delete from user_sdo_geom_metadata 
  where table_name = upper(p_table);

  insert into user_sdo_geom_metadata
  values (p_table, p_cloumn, diminfo, C_SRID);

  commit;
 exception
    when others then
    rollback;
    dbms_output.put_line(Sqlerrm);                         
end;

procedure create_table_index
(
 p_table varchar2,
 p_cloumn varchar2
)
is

begin
     create_table_meta(p_table,p_cloumn);
     drop_spatial_index(p_table);
     execute immediate 'create index '||p_table||'_'||p_cloumn||' on '||p_table||
     '('||p_cloumn||') indextype is mdsys.spatial_index';
 exception
    when others then
    dbms_output.put_line(Sqlerrm);     
end;






procedure create_index
(
 p_tables varchar2,
 p_cols varchar2
)

is
v_table_array  t_varchar_array;
v_col_array  t_varchar_array;
v_sql varchar2(400);
begin
     v_table_array := f_common_split(p_tables,',');
     v_col_array := f_common_split(p_cols,',');
     if v_table_array.count > 0 and v_table_array.count = v_col_array.count
     then
         for i in 1..v_table_array.count
         loop
/*             v_sql := 'update   '||v_table_array(i)||' t
              set t.geometry.sdo_srid = 8307';
              --where t.geometry.sdo_srid <> 8307';
             execute immediate v_sql;
             commit;
*/             
               create_table_index(v_table_array(i),v_col_array(i));
         end loop;
     end if;
 exception
    when others then
    dbms_output.put_line(Sqlerrm);
end;

procedure create_meta
(
 p_tables varchar2,
 p_cols varchar2
)

is
v_table_array  t_varchar_array;
v_col_array  t_varchar_array;
v_sql varchar2(400);
begin
     v_table_array := f_common_split(p_tables,',');
     v_col_array := f_common_split(p_cols,',');
     if v_table_array.count > 0 and v_table_array.count = v_col_array.count
     then
         for i in 1..v_table_array.count
         loop
/*             v_sql := 'update   '||v_table_array(i)||' t
              set t.geometry.sdo_srid = 8307';
              --where t.geometry.sdo_srid <> 8307';
             execute immediate v_sql;
             commit;
*/             
             create_table_meta(v_table_array(i),v_col_array(i));
         end loop;
     end if;
 exception
    when others then
    dbms_output.put_line(Sqlerrm);
end;

procedure drop_table_quiet
(
 p_table_name varchar2
)
is
begin
     execute immediate 'drop table '||p_table_name || ' cascade constraints';
 exception
    when others then
    dbms_output.put_line(Sqlerrm);
end;

procedure add_pk
(
 p_table_name varchar2,
 p_col_name varchar2
)
is
begin
 execute immediate 'alter table '||p_table_name||'
  add constraint pk_'||p_table_name||' primary key ('||p_col_name||')';
   exception
    when others then
    dbms_output.put_line(Sqlerrm);
end;

end pk_wgis_util;
/
