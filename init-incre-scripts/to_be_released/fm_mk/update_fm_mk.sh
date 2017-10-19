export NLS_LANG=.AL32UTF8
source ./update_fm_mk.conf

sqlplus $fm_gdb_url @./update_poi_column.sql
