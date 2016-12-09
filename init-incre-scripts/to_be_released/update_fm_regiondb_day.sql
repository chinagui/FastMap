create table POI_EDIT_MULTISRC
(
  pid         number(10) not null,
  source_type varchar2(12) not null,
  main_type   number(2) not null
)
;
ALTER TABLE POI_EDIT_STATUS ADD (WORK_TYPE NUMBER(2) default 1);
commit;
EXIT;
