export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf


sh ./init_check_rules.sh

sqlplus $fmsys_url @./update_job_info.sql

sqlplus $fmsys_url @./CHECK_PLUS_AND_CHECK_OPERATION_PLUS.sql
