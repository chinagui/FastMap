export NLS_LANG=.AL32UTF8
source ./update_fm_limit.conf

sqlplus $fm_limit_url @./modify_length.sql








