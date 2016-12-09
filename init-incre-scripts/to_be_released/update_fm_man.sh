export NLS_LANG=.AL32UTF8
source ./update_fm_man.conf


sqlplus $fmman_url @./man_add_day2month_sync.sql

sqlplus $fmman_url @./man_add_day2month_config.sql

