export NLS_LANG=.AL32UTF8
source ./*.conf
sqlplus $sys_username/$sys_password @./drop_check_rules.sql
imp $sys_username/$sys_password file=sysCheck.dmp tables=ck_rule,ck_suite,ck_suite_rule_mapping,ck_object_node,CHECK_OPERATION_PLUS,CHECK_PLUS,BATCH_OPERATION_PLUS,BATCH_PLUS