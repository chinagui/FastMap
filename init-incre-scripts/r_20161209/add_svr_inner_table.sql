-- used by multisrc2fm
create table POI_EDIT_MULTISRC
(
  pid         number(10) not null,
  source_type varchar2(12) not null,
  main_type   number(2) not null
);
CREATE INDEX IDX_POI_EDIT_MS_ID ON POI_EDIT_MULTISRC(PID);

CREATE TABLE SVR_MULTISRC_DAY_IMP(
FID VARCHAR2(36),
START_DATE DATE,
END_DATE DATE
);

commit;
exit;