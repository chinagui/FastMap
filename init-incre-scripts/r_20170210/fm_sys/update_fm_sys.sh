export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf


sqlplus $fmsys_url @./update_rules_sys.sql

sqlplus $fmsys_url @./update_sys_config.sql

sh ./init_check_rules.sh
