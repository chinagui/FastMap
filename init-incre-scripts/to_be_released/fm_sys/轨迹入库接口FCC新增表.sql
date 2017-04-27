
---FCC新增ADAS轨迹点表
INSERT INTO SYS_CONFIG
  (CONF_ID, CONF_KEY, CONF_VALUE, CONF_DESC, APP_TYPE)
VALUES
  (SYS_CONFIG_SEQ.NEXTVAL,
   'hbase.tablename.adasTrackPoints',
   'AdasTrackPoints',
   'ADAS轨迹点',
   'default')