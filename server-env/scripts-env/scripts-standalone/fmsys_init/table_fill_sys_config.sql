WHENEVER SQLERROR CONTINUE;
-- FILL SYS_CONFIG
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'gdb.version','270+','gdb version','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'job.threadpool.size','20','任务并行度','job-server');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'token.expire.second','86400','token有效时间','default');
-- commons
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'glm.ignore.table.prefix','TEMP_,TMP_,NI_,MDRT_,PIPELINE_,SYS_,STAT_,SHD_,B_,NN_,TMAU_,TASK_EXTENT,AU_,TMC_,LOG_,CK_RULE','glm table filters','default');
-- scripts-interface
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'scripts.dir','F:\\Fm_Projects_Doc\\scripts\\','脚本执行路径','default');
-- datahub
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dbserver.strategy.default','random','数据库服务器默认选择策略，只能配置不需要传参的策略类型','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dbserver.cache.enable','false','是否缓存服务器列表','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'datahub.oracle.defaultTablespaces','USERS','datahub创建oracle用户默认的表空间','default');
-- export-core
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'export.multiThread.inputPoolSize','10','导出读取源时内部线程数','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'export.multiThread.outputPoolSize','10','导出写入目标时内部线程数','default');
-- solr
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'solr.address','http://192.168.4.130:8081/solr/tips_sprint6/','','default');
-- hbase
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'hbase.address','192.168.3.156','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'hbase.tablename.tips','tips_sprint6','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'hbase.tablename.tracklines','tracklines_sprint6','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'hbase.tablename.photo','photo','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'hbase.tablename.linktile','link_tile','','default');
-- mq
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'main.mq.uri','amqp://fos:fos@192.168.4.188:5672','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'main.mq.cacheSize.channel','20','','default');
-- Dropbox
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.root','/data/resources/download/','下载父目录','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.root','/resources/download/','下载url的父目录','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.upload.path','/data/resources/upload','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.nds','/data/resources/download/nds','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.tips','/data/resources/download/tips','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.basedata','/data/resources/download/basedata','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.filepath.patternimg','/data/resources/download/patternimg','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.nds','/resources/download/nds','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.tips','/resources/download/tips','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.basedata','/resources/download/basedata','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.download.urlpath.patternimg','/resources/download/patternimg','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'dropbox.url','http://192.168.4.188','','default');
-- poi
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'editsupport.poi.download.filepath.poi','/data/resources/download/poi','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'editsupport.poi.download.urlpath.poi','/resources/download/poi','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'editsupport.poi.url','http://192.168.4.188','','default');
-- mapspotter
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'mapspotter.infor.upload.url','http://192.168.4.188:8000/service/mapspotter/data/info/upload','','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'mapspotter.infor.upload.timeout','120000','','default');
--stat
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'fm_stat','fm_stat','统计库','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'mongo_host','192.168.4.220','mongodbHost','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'mongo_port','30000','mongodbPort','default');
-- render.table
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'render.table.prefix','DEV','render存在hbase中的表名前缀','default');

-- fcc rd_name import
insert into sys_config (CONF_ID, CONF_KEY, CONF_VALUE, CONF_DESC, APP_TYPE)
values (sys_config_seq.nextval, 'region_db_id', '17', '路演的大区库id（路演临时用，道路名导入）', 'default');

INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'rtic.connection.string','ORACLE,192.168.4.131,1521,orcl,fm_pid_1,fm_pid_1','rticid server','default');
INSERT INTO SYS_CONFIG VALUES (SYS_CONFIG_SEQ.NEXTVAL,'multisrc.day.sync.url','http://192.168.0.40:8090/iqu/data/InfoDataAction.do?operate=downloadFastMapPoi','日增量推送多源url','default');

INSERT INTO SYS_CONFIG
  (CONF_ID, CONF_KEY, CONF_VALUE, CONF_DESC, APP_TYPE)
VALUES
  (SYS_CONFIG_SEQ.NEXTVAL, 'SEASON.VERSION', '17SUM', '作业季', 'default');
  
insert into sys_config(conf_id,conf_key,conf_value,conf_desc，app_type)
select sys_config_seq.nextval,'cms.url','http://192.168.4.188:8000','cms任务创建url','default' from dual;
COMMIT;

EXIT;
