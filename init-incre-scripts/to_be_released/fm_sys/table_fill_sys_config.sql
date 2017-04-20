WHENEVER SQLERROR CONTINUE;
insert into sys_config (conf_id,conf_key,conf_value,conf_desc,app_type)
values(sys_config_seq.nextval,'VALUE_SMTP','smtp.163.com','公共邮箱，用于管理任务等发邮件','default');
insert into sys_config (conf_id,conf_key,conf_value,conf_desc,app_type)
values(sys_config_seq.nextval,'SEND_EMAil','swtxtest@163.com','公共邮箱，用于管理任务等发邮件','default');
insert into sys_config (conf_id,conf_key,conf_value,conf_desc,app_type)
values(sys_config_seq.nextval,'SEND_PWD','swtx123456','公共邮箱，用于管理任务等发邮件','default');
COMMIT;

EXIT;
