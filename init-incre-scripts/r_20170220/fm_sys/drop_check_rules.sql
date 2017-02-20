WHENEVER SQLERROR CONTINUE;
drop table ck_rule;
drop table ck_suite;
drop table ck_suite_rule_mapping;
drop table ck_object_node;

drop table BATCH_PLUS;
drop table BATCH_OPERATION_PLUS;
drop table CHECK_PLUS;
drop table CHECK_OPERATION_PLUS;

drop table batch_rule;
drop table batch_rule_cop;
drop table batch_suite_cop;
drop table ck_rule_cop;
drop table ck_suite_cop;

COMMIT;
EXIT;