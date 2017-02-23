export NLS_LANG=.AL32UTF8
source ./*.conf
sqlplus $sys_manager_username/\"$sys_manager_password\"@$sys_url @./create_sys_user.sql $sys_username $sys_password

sqlplus $sys_username/$sys_password@$sys_url @./create_type_function.sql

sqlplus $sys_username/$sys_password@$sys_url @./table_create_sys.sql

sqlplus $sys_username/$sys_password@$sys_url @./glm_table_init.sql

sqlplus $sys_username/$sys_password@$sys_url @./table_fill_sys_config.sql

sqlplus $sys_username/$sys_password@$sys_url @./table_fill_datahub.sql