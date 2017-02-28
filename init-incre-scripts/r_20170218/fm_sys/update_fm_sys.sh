export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf


sqlplus $fmsys_url @./update_sys_ckRuleCop.sql

sh ./init_check_rules.sh
