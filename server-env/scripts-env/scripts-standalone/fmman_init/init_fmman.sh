export NLS_LANG=.AL32UTF8
source ./*.conf
sqlplus $fmgdb_manager_username/$fmgdb_manager_password @./create_pm_user.sql $pmdb_username $pmdb_password

sqlplus $pmdb_username/$pmdb_password @./create_type_function.sql

sqlplus $fmsys_url @./insert_datahub.sql $pmdb_username $pmdb_password