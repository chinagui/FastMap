export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb_month_20161207.conf

sqlplus $fmregiondb_url @./update_log_act_month.sql