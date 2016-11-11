export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf

sqlplus $fmsys_url @./update_fm_sys_msg_glmtables.sql

sqlplus $fmsys_url @./update_cop_ck_rules.sql