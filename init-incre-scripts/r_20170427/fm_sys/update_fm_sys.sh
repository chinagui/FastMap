export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf


sqlplus $fmsys_url @./glm_table_init.sql

sh ./init_check_rules.sh