export NLS_LANG=.AL32UTF8
source ./*.conf
sqlplus $fmsys_url @./drop_check_rules.sql
imp $fmsys_url file=sysCheck.dmp tables=ck_rule,ck_suite,ck_suite_rule_mapping,ck_object_node