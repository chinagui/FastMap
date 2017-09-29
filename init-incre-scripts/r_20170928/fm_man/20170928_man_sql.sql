alter table  TASK add (upload_Method varchar2(50));
comment on column TASK.upload_Method is '更新方式';


alter table task add (geometry SDO_GEOMETRY);
INSERT INTO USER_SDO_GEOM_METADATA
  (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES
  ('task',
   'GEOMETRY',
   MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG', -180, 180, 0.005),
                       MDSYS.SDO_DIM_ELEMENT('YLAT', -90, 90, 0.005)),
   8307);

CREATE INDEX IDX_SDO_task ON task(GEOMETRY) 
INDEXTYPE IS MDSYS.SPATIAL_INDEX;
 
 commit; 
 exit;