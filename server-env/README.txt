所有FOS服务端环境初始化全量脚本放在这里，包括：
/* 环境初始化部分 */
1. FM-SYS系统库初始化
	执行前提：无
    脚本路径：1) scripts/standalone/init-fmsys/init_fmsys.sh
	          2) scripts/standalone/init-fmsys/init_check_rules.sh
	配置文件: 1) 修改init_fmsys.conf
	          2) 修改table_fill_datahub.sql
			  3) 修改table_fill_sys_config.sql
	执行环境：脚本环境服务器
	执行命令：# sh init_fmsys.sh
2. FM-META元数据库初始化
    执行前提：FM-SYS库初始化完成。
    脚本路径：scripts/standalone/transition_metadb_init/transition_metadb_init.sh
	配置文件：修改scripts/standalone/transition_metadb_init/transition_metadb_init.conf
	执行环境：脚本环境服务器
	执行命令：# sh transition_metadb_init.sh
3. FM-MAN管理库初始化
    执行前提：FM-META库初始化完成。
    脚本路径：scripts/standalone/fmman_init/init_fmman.sh
	执行前提条件：FM-META库初始化完成。
4. FM-GDB+母库初始化
    脚本路径：transition_fmgdb_init/transition_fmgdb_init.sh
	前提条件：FM-META库初始化完成。
5. 深度信息hadoop导出
    脚本路径：hadoop_poi_deep_export/init_fmgdb_fmpoihadoop_export.sh
	前提前提条件：无。
6. 深度信息导入母库
    脚本路径：data_flow/init_fmgdb_fmpoihadoop_import.sh
	执行前提条件：hadoop导出文件已生成及FM-GDB+初始化完成。
7. FM-PID库初始化
    脚本路径：pid_init/
	执行前提条件：FM-SYS库初始化完成。
8. FM-STATICS统计库初始化
    脚本路径：无。手动创建一个fm_stat的DB。
	执行前提条件：无。
注：至此，环境初始化完成，请启动所有FOS的Tomcat服务，以下脚本中可能会依赖服务。
/* 数据初始化部分*/
0. City&Block初始化
    脚本路径：data_flow/import_city_block.sh
	前提依赖：FM-MAN库初始化完成。
1. 日出品POI库&POI+道路库初始化
    脚本路径：data_flow/init_desgdb.sh
	前提依赖：母库初始化完成
2. 大区库初始化
    脚本路径：data_flow/init_regiondb.sh
	执行前提条件：母库初始化完成
3. GDB SNAPSHOT提取(日？月？)
    脚本路径：data_flow/rebuild_regiongdb_snapshot.sh
	执行前提条件：大区库初始化完成。
4. FM-META SNAPSHOT提取
    脚本路径：data_flow/exp_meta_to_sqlite.sh
	执行前提条件：FM-META库初始化完成。
5. FM-META 模式图包提取
    脚本路径：data_flow/rebuild_fmmeta_snapshot.sh
	执行前提条件：FM-META库初始化完成。
6. 大区库日库、月库的Render初始化
    脚本路径：prjrender_rebuild/rebuild_regiongdb_render.sh
	执行环境：Hadoop主节点，hadoop用户登录执行
	执行前提条件：FM-SYS库初始化完成。
/* 作业过程中运维执行 */
1. POI和TIPS批采集子任务号
    脚本路径：data_flow/
    执行时机：生管输入后执行。
2. 日落月
    脚本路径：data_flow/
    执行时机：加入Crontab每日01:00执行。
3. 日出品
    脚本路径：data_flow/
    执行时机：加入Crontab每日03:00执行。
3. 统计脚本
    执行时机：data_flow/

说明：以上脚本目录下如果有README.txt，请一定要确认文件中的说明，如有任何不确定的项，一定要和研发沟通，谢谢！

UPDATE SYS_CONFIG SET CONF_VALUE='/app/fm315/svr/scripts/container/' WHERE CONF_KEY='scripts.dir';
UPDATE SYS_CONFIG SET CONF_VALUE='DEV_R' WHERE CONF_KEY='render.table.prefix';
UPDATE SYS_CONFIG SET CONF_VALUE='amqp://fos:fos@192.168.4.130:5672' WHERE CONF_KEY='main.mq.uri';
INSERT INTO DB_HUB VALUES (DB_SEQ.NEXTVAL,NULL,'gdb270_dcs_17sum_bj','gdb270_dcs_17sum_bj',0,'GDB_DATA','gen2Au',2,null,2,null,'二代外业成果库');


sys:fm_sys_315@4.131
meta:metadata_pd_17sum@3.227
母库&月库：gdb270_17sum_bj@3.227
man:fm_man_315@4.131
日库：fm_regiondb_315_d_1@4.61

服务：root@4.130:/app/fm315/svr/


问题：
1. 元数据库：sc_partition_menshlist中增加OPEN_FLAG字段
2. dropbox配置下载服务，sys_config中修改dropbox配置
3. fcc有增加的sys_config配置
4. 母库注意空间索引
