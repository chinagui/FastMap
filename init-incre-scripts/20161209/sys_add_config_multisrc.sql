-- sys_config
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.root','/data/resources/download/','下载父目录','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.root','/resources/download/','下载url的父目录','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'multisrc.day.sync.url','http://192.168.0.40:8090/iqu/data/InfoDataAction.do?operate=downloadFastMapPoi','日增量推送多源url','default');

commit;

exit;