ALTER TABLE POI_EDIT_STATUS ADD (commit_his_status NUMBER(2) default 1 not null );
	comment on column POI_EDIT_STATUS.commit_his_status is '0:未提交过 ;  1:已提交';
	