 
 alter table ADMIN_GROUP_MAPPING add (month_group_name varchar2(100)); comment on column ADMIN_GROUP_MAPPING.month_group_name1 is '月编作业组';  
 
 commit; 
 exit;