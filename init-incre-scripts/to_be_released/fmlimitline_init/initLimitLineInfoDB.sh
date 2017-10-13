export NLS_LANG=.AL32UTF8
source ./initLimitLineInfoDB.conf

sqlplus $manager_username/$manager_password@$limitlineinfo_ip @./createLimitLineInfoUser.sql $limitlineinfo_user
sqlplus $fmsys_url @./limitLineInfoInsertDbhub.sql $limitlineinfo_user $limitlineinfo_server_biz
sqlplus $limitlineinfo_user/$limitlineinfo_user@$limitlineinfo_ip @./create_info_table.sql








