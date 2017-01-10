export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb_month.conf

sqlplus $fmregiondb_url @./drop_index.sql
