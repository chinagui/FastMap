export NLS_LANG=.AL32UTF8
source ./initLimitLineInfoDB.conf

sqlplus $limitlineinfo_user/$limitlineinfo_user@$limitlineinfo_ip @./modify_length.sql








