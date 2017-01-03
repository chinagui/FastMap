insert into CHECK_PLUS (RULE_ID, ACCESSOR, ACCESSOR_TYPE, OBJ_NAME_SET, REFER_SUBTABLE_MAP, STATUS, LOG, DESC_SHORT, DESC_DETAIL, MEMO, RULE_LEVEL)
values ('FM-YW-20-053', 'com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule.FMYW20053', 'JAVA', 'IX_POI', '{"IX_POI":["IX_POI_NAME"]}', 'E', '官方标准化英文名作业', '官方标准化英文名作业', '检查条件：
非删除POI
检查原则：
官方原始英文名长度大于35，报LOG：官方标准化英文名作业', '', 1);
insert into CHECK_OPERATION_PLUS (OPERATION_CODE, CHECK_ID, OPERATION_DESC)
values ('CHECK_DAY2MONTH', 'FM-M01-01', '日落月检查');

insert into CHECK_OPERATION_PLUS (OPERATION_CODE, CHECK_ID, OPERATION_DESC)
values ('CHECK_DAY2MONTH', 'FM-M01-02', '日落月检查');
COMMIT;

exit;