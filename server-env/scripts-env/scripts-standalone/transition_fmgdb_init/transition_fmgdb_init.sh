export NLS_LANG=.AL32UTF8
source ./*.conf
sqlplus $fmgdb_manager_username/\"$fmgdb_manager_password\"@$fmgdb_url @./transition_fmgdb_auth.sql $fmgdb_username $fmgdb_password

sqlplus $fmgdb_username/$fmgdb_password@$fmgdb_url @./transition_fmgdb_resources.sql

sqlplus $fmgdb_username/$fmgdb_password@$fmgdb_url @./link_util.pck

sqlplus $fmgdb_username/$fmgdb_password@$fmgdb_url @./drop_temp.sql

# sqlplus $fmgdb_username/$fmgdb_password @./create_spatial_utils_and_rebuild.sql

sqlplus $fmgdb_username/$fmgdb_password@$fmgdb_url @./create_type_function.sql

sqlplus $fmgdb_username/$fmgdb_password@$fmgdb_url @./prj_utils.pck

sqlplus $fmgdb_username/$fmgdb_password@$fmgdb_url @./poi_addr_split.sql

sqlplus $fmsys_username/$fmsys_password@$fmsys_url @./insert_datahub.sql $fmgdb_username $fmgdb_password
