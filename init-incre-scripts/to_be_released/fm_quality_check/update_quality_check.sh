export NLS_LANG=.AL32UTF8
source ./update_quality_check.conf

sqlplus $db_user/$db_user$db_ip @./create_table.sql






