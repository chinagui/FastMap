export NLS_LANG=.AL32UTF8
source ./update_fm_mk.conf

sqlplus $fmmeta_url @./drop_index.sql
