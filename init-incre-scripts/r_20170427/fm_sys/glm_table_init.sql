INSERT INTO GLM_GRID_MAP VALUES('CMG_BUILDING','','',0,'270+');
INSERT INTO GLM_GRID_MAP VALUES('CMG_BUILDING_POI','','',0,'270+');
INSERT INTO GLM_GRID_MAP VALUES('CMG_BUILDING_NAME','','',0,'270+');
INSERT INTO GLM_GRID_MAP VALUES('CMG_BUILDING_3DICON','','',0,'270+');
INSERT INTO GLM_GRID_MAP VALUES('CMG_BUILDING_3DMODEL','','',0,'270+');

delete from glm_grid_map where table_name = 'CMG_BUILDFACE_TOPO';

insert into glm_grid_map(table_name, ref_col_name, ref_info, single_mesh, gdb_version) values('CMG_BUILDFACE_TOPO', 'FACE_PID', 'CMG_BUILDFACE:FACE_PID:NULL', '1', '270+');