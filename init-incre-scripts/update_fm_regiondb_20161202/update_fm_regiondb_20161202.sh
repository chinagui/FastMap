export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb_20161202.conf

sqlplus $fmregiondb_url @./add_svr_inner_table.sql
sqlplus $fmregiondb_url @./update_log.sql