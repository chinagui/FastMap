--创建表
--drop table rtic_code;
create table rtic_code(
       mesh_id varchar2(10) not null,
       rtic_class number(2) not null,
       rtic_id number(5) not null,
       rtic_state varchar2(10) not null check (rtic_state in ('未使用','预使用','新增','使用','封号','INT封号')),
       season varchar2(10),
       primary key (mesh_id,rtic_class,rtic_id)
);
create index idx_rtic_code_1 on rtic_code(mesh_id,rtic_class);
create index idx_rtic_code_2 on rtic_code(mesh_id,rtic_class,rtic_state);
--RTIC_SEGMENT_USAGE
--drop table rtic_segment_usage;
create table rtic_segment_usage(
       useage_id varchar2(32) primary key,
       mesh_id varchar2(10) not null,
       rtic_class number(2) not null,
       rtic_ids t_varchar_array,
       version_id number(10) default 0 not null,
       task_id varchar2(32),
       client_id varchar2(32),
       client_ip varchar2(32),
       use_for varchar2(1024),
       usage_type number(2) not null,
       usage_time date     
);
create index idx_rtic_usage on rtic_segment_usage(mesh_id,rtic_class);
create index idx_rtic_usage_task on rtic_segment_usage(task_id);
create index idx_rtic_usage_task_1 on rtic_segment_usage(task_id,version_id);
create index idx_rtic_usage_task_2 on rtic_segment_usage(task_id,usage_type);

comment on table rtic_segment_usage
  is '记录每次申请和归还成功记录表';
-- Add comments to the columns 
comment on column rtic_segment_usage.usage_type
  is '使用类型，1-申请，2-归还，0-用于锁控制，无业务意义';

--插入初始记录，用于锁控制
insert into rtic_segment_usage values ('0','0',0,null,0,null,null,null,null,0,sysdate);
commit;

/*
--导入SC_RTIC_CODE
-- 1. 建立到企划给定的元数据库的database link 【请根据元数据库实际地址修改以下参数】
create database link RTIC_IMP_DBLINK
  connect to 元数据库名 identified by 元数据库密码
  using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = IP地址 )(PORT = 1521 )))(CONNECT_DATA = (SERVICE_NAME = orcl )))';
--参考
--2. 导入到dms的rtic_code表
delete from rtic_code;
insert into rtic_code (mesh_id,rtic_class,rtic_id,rtic_state,season) select "MESH","CLASS","ID","STATE",SEASON FROM SC_RTIC_CODE@RTIC_IMP_DBLINK;
commit;
--3. 删除dblink
drop database link RTIC_IMP_DBLINK;
*/



