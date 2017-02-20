--ck_suite_cop ck_rule_cop
update ck_suite_cop t set t.feature = 5 where t.feature = 3 ;
update ck_suite_cop t set t.feature = 1 where t.feature = 0 ;
update ck_suite_cop t set t.feature = 3 where t.feature = 2 ;


comment on column CK_SUITE_COP.feature
  is '1 poi粗编 ;2 poi精编 ; 3 道路粗编 ; 4道路精编 ; 5道路名 ; 6 其他';
  
commit;

delete ck_suite_cop c where c.SUITE_ID = 'suite9';

insert into ck_suite_cop (SUITE_ID, SUITE_NAME, SUITE_RANGE, FEATURE)
values ('suite9', 'POI日编阶段自定义检查8项', '子任务范围', 1);


delete  ck_rule_cop c  where c.rule_status=1 and c.suite_id  = 'suite9';

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR70003', '中文格式检查', 'IX_POI_ADDRESS(FULLNAME)', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR70116', '中文格式检查', 'IX_POI_NAME(NAME) 检查原则：name_class = 1.name_type = 2的原始官方中文名称中,存在回车换行符,名称中包含连续两个或者多个空格,名称前后存在空格', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20491', '未知', 'IX_POI中，字段KIND_CODE的值必须存在于SC_POINT_POICODE_NEW(KIND_CODE)中', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20531', '未知', 'IX_POI中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM300033', '几何验证', 'IX_POI ( GEOMETRY )', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60038', '未知', 'IX_POI_CONTACT(POI_PID,CONTACT,CONTACT_TYPE,CONTACT_DEPART)应唯一', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60994', 'POI不应落在图廓上', 'POI的坐标不应位于图廓上，否则报log', null, null, 1, 'suite9');

insert into ck_rule_cop (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR63027', '值域检查', 'IX_POI(KIND_CODE)', null, null, 1, 'suite9');

commit;

exit;