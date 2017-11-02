create table USER_INFO
(
  user_id        NUMBER(10) not null,
  user_real_name VARCHAR2(50),
  user_nick_name VARCHAR2(50),
  user_password  VARCHAR2(20),
  user_email     VARCHAR2(75),
  user_phone     VARCHAR2(50)
);
comment on table USER_INFO
  is '用户信息表';
-- Add comments to the columns 
comment on column USER_INFO.user_id
  is '用户ID';
comment on column USER_INFO.user_real_name
  is '用户真实名';
comment on column USER_INFO.user_nick_name
  is '用户昵称';
comment on column USER_INFO.user_password
  is '用户密码';
comment on column USER_INFO.user_email
  is '用户邮箱';
alter table USER_INFO
  add constraint PK_USERINFO primary key (USER_ID);
  
CREATE SEQUENCE USER_INFO_SEQ START WITH 1 MAXVALUE 9999999999;

alter table SC_PLATERES_LINK add (LINK_PID number(10));
alter table SC_PLATERES_FACE add (LINK_PID number(10));
alter table SC_PLATERES_FACE add (LINK_TYPE VARCHAR2(20));
  
exit;