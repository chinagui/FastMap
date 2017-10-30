alter table ADMIN_GROUP_MAPPING add (refine_group_name varchar2(100));
comment on column ADMIN_GROUP_MAPPING.refine_group_name is '精细化作业组';

comment on column user_group.group_subtype is '值域说明：1外业采集2众包3情报矢量4多源5精细化';

comment on column SUBTASK.work_kind is '值域说明：1外业采集2众包3情报矢量4多源5精细化';

create table subtask_refer_detail(
     refer_id number(10),
     detail_info clob
);

commit; 
exit;