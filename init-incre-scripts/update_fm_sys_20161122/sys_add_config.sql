-- sys_config
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.root','/data/resources/download/','下载父目录','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.root','/resources/download/','下载url的父目录','default');
commit;

exit;