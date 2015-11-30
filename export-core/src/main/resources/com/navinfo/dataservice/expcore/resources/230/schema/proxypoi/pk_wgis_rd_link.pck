create or replace package pk_wgis_rd_link is

  -- Author  : ARNOLD
  -- Created : 2010-10-27 16:18:16
  -- Purpose :

  procedure assemble_rd_link;


  procedure assemble_rd_link_name;


end pk_wgis_rd_link;
/
create or replace package body pk_wgis_rd_link is

  -- Private type declarations
  procedure assemble_rd_link
  is
  begin
       pk_wgis_util.create_index('RD_LINK','GEOMETRY');
  end;

  procedure assemble_rd_link_name
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'create or replace view wgis_road_name
      (link_pid,geometry,name,constraint gis_road_name_pk primary key (link_pid) rely disable novalidate)
      as
      select r1.link_pid,r1.geometry,r3.name
      from
      rd_link r1,rd_link_name r2,rd_name r3
      where r1.link_pid = r2.link_pid
      and r2.name_groupid = r3.name_groupid
      and r2.seq_num = 1
      and r3.lang_code=''CHI''';
      execute immediate v_sql;
      pk_wgis_util.create_meta('WGIS_ROAD_NAME','GEOMETRY');
  end;
end pk_wgis_rd_link;
/
