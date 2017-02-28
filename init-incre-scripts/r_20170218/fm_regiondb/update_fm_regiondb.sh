export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb.conf

sqlplus $fmregiondb_url @./NiValException.sql
