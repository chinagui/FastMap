create global temporary table TMP_RESTRICT
(
  link_pid   INTEGER,
  s_node_pid INTEGER,
  e_node_pid INTEGER,
  direct     INTEGER
)
on commit delete rows;

create global temporary table TMP_RESTRICT2
(
  link_pid   INTEGER,
  s_node_pid INTEGER,
  e_node_pid INTEGER,
  direct     INTEGER,
  via_path   VARCHAR2(250)
)
on commit delete rows;

-- index
create index idx_rd_speedlimit on rd_speedlimit(link_pid);


insert into user_sdo_geom_metadata
values
  ('RD_LINK',
   'GEOMETRY',
   mdsys.sdo_dim_array(SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       SDO_DIM_ELEMENT('XLAT', -90, 90, 0.005)),
   8307);
   
create index idx_rd_link_geometry
on rd_link(geometry)
indextype is mdsys.spatial_index;   

analyze table rd_link compute statistics;

insert into user_sdo_geom_metadata
values
  ('RD_NODE',
   'GEOMETRY',
   mdsys.sdo_dim_array(SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       SDO_DIM_ELEMENT('XLAT', -90, 90, 0.005)),
   8307);
   
create index idx_rd_node_geometry
on rd_node(geometry)
indextype is mdsys.spatial_index;  

analyze table rd_node compute statistics;

insert into user_sdo_geom_metadata
  (table_name, COLUMN_NAME, DIMINFO, SRID)
values
  ('AD_FACE',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

create index idx_sdo_ad_face on ad_face(geometry) 
indextype is mdsys.spatial_index;
