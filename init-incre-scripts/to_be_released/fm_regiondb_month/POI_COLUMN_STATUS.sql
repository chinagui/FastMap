-- Create table
create table POI_COLUMN_STATUS
(
  PID                NUMBER(10) not null,
  WORK_ITEM_ID       VARCHAR2(50),
  FIRST_WORK_STATUS  NUMBER(1) default 1,
  SECOND_WORK_STATUS NUMBER(1) default 1,
  HANDLER            NUMBER(10),
  TASK_ID            NUMBER(10),
  APPLY_DATE         TIMESTAMP(6) default sysdate
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table POI_COLUMN_STATUS
  add foreign key (PID)
  references IX_POI (PID);