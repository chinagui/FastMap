WHENEVER SQLERROR CONTINUE;
insert into Sys_Config (CONF_ID, CONF_KEY, CONF_VALUE, CONF_DESC, APP_TYPE)
values (Sys_Config_Seq.Nextval, 'mapspotter.info.feedback.url', '&1', '情报下发接口', 'default');

insert into Sys_Config (CONF_ID, CONF_KEY, CONF_VALUE, CONF_DESC, APP_TYPE)
values (Sys_Config_Seq.Nextval, 'mapspotter.info.pass.url', '&2', '情报反馈接口', 'default');

COMMIT;
EXIT;