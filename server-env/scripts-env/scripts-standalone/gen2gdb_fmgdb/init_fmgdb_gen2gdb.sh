source ./*.conf
sqlplus $fmgdb_manager_username/$fmgdb_manager_password @./create_fmgdb_user.sql $fmgdb_username $fmgdb_password

impdp $fmgdb_username/$fmgdb_password directory=DATA_PUMP_DIR dumpfile=$gen2gdb_username.dmp remap_schema=$gen2gdb_username:$fmgdb_username  remap_tablespace=GDB_DATA:USERS

sqlplus $fmgdb_username/$fmgdb_password @./add_tables_columns.sql $meta_username $meta_password $meta_ip $meta_service_name $meta_port

sqlplus $fmgdb_username/$fmgdb_password @./link_util.pck

sqlplus $fmgdb_username/$fmgdb_password @./sp4_drop_temp.sql

sqlplus $fmgdb_username/$fmgdb_password @./temp_delete_poi_gd.sql

sqlplus $fmsys_username/$fmsys_password@$fmsys_url @./insert_datahub.sql $fmgdb_username $fmgdb_password
