export NLS_LANG=.AL32UTF8

source ./*.conf
sqlplus $fmgdb_manager_username/$fmgdb_manager_password @./create_meta_user.sql $fmmeta_username $fmmeta_password
sqlplus $fmmeta_username/$fmmeta_password @./create_dblink.sql $fmmeta_dblink_name $remote_metadb_username $remote_metadb_password $remote_metadb_ip $remote_metadb_port $remote_metadb_servicename
# dump exp start
sqlplus $fmmeta_username/$fmmeta_password @./prepare_dump.sql $fmmeta_directory_name $fmmeta_directory
mkdir -p $fmmeta_directory
expdp $fmmeta_username/$fmmeta_password directory=$fmmeta_directory_name dumpfile=$remote_metadb_username.dmp schemas=$remote_metadb_username network_link=$fmmeta_dblink_name
# dump exp end
impdp $fmmeta_username/$fmmeta_password directory=$fmmeta_directory_name dumpfile=$remote_metadb_username.dmp remap_schema=$remote_metadb_username:$fmmeta_username  remap_tablespace=GDB_DATA:USERS

sqlplus $fmmeta_username/$fmmeta_password @./table_create_meta.sql $fmmeta_dblink_name

sqlplus $fmmeta_username/$fmmeta_password @./MetaTrigger.sql
sqlplus $fmmeta_username/$fmmeta_password @./pyutils.sql
sqlplus $fmsys_url @./insert_datahub.sql $fmmeta_username $fmmeta_password

java -Djava.awt.headless=true -cp .:ojdbc6.jar:import_pattern_image.jar com.navinfo.dataservice.FosEngine.patternimg.ImportPatternImage $fmmeta_username $fmmeta_password $fmmeta_ip $fmmeta_port $fmmeta_servicename $image_path

sqlplus $fmmeta_username/$fmmeta_password @./set_pattern_updatetime.sql
