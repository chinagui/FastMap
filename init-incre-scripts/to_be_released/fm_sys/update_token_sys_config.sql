-- token expired time
UPDATE SYS_CONFIG SET CONF_VALUE='172800' WHERE CONF_KEY='token.expire.second';

commit;
exit;