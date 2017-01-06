WHENEVER SQLERROR CONTINUE;
drop table ck_rule;
drop table ck_suite;
drop table ck_suite_rule_mapping;
drop table ck_object_node;

drop table BATCH_PLUS;
drop table BATCH_OPERATION_PLUS;
drop table CHECK_PLUS;
drop table CHECK_OPERATION_PLUS;

COMMIT;
EXIT;