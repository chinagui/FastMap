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
	配置文件：scripts/standalone/fmman_init/init_fmman.conf
	执行环境：脚本环境服务器
	执行命令：# sh init_fmman.sh
	
4. FM-GDB+母库初始化
    执行前提：FM-META库初始化完成。
    脚本路径：scripts/standalone/transition_fmgdb_init/transition_fmgdb_init.sh
	配置文件：scripts/standalone/transition_fmgdb_init/transition_fmgdb_init.conf
	执行环境：脚本环境服务器
	执行命令：# sh transition_fmgdb_init.sh
	
5. 深度信息hadoop导出
    执行前提：FM-GDB+母库初始化完成。
    脚本路径：scripts/standalone/hadoop_poi_deep_export/init_fmgdb_fmpoihadoop_export.sh
	配置文件：1) scripts/standalone/hadoop_poi_deep_export/conf.properties
	          2) 将所在的Hbase环境的hbase-site.xml覆盖jar包根目录内同名文件
	执行环境：Hadoop主节点服务器上，hadoop用户
	执行命令：sh init_fmgdb_fmpoihadoop_export.sh /XXX/scripts/standalone/hadoop_poi_deep_export/conf.properties
	注意事项：执行完成后，同级目录生成poi_deep.txt文件
	
6. 深度信息导入母库
    执行前提：hadoop导出文件已生成及FM-GDB+初始化完成。
    脚本路径：scripts/container/init_fmgdb_fmpoihadoop_import.sh
	配置文件：依赖脚本容器container统一配置
	执行环境：脚本环境服务器
	执行命令：# sh init_fmgdb_fmpoihadoop_import.sh /XXX/poi_deep.txt

7. FM-PID库初始化
    执行前提：FM-SYS库初始化完成。
    脚本路径：scripts/standalone/pid_init/init_fmpid.sh
	配置文件：scripts/standalone/pid_init/init_fmpid.conf
	执行环境：脚本环境服务器
	执行命令：# sh init_fmpid.sh
	注意事项：执行两次，初始化两个PID库，第一个初始为400000001，第二个初始为500000001
	
8. FM-STATICS统计库初始化
    手动创建一个fm_stat的DB。
	
注：至此，环境初始化完成，请启动所有FOS的Tomcat服务，以下脚本中可能会依赖服务。

/* 数据初始化部分*/
1. 大区库初始化
    执行前提：母库初始化完成。
    脚本路径：scripts/container/init_regiondb.sh
	配置文件：scripts/container/request/init_regiondb.json
	执行环境：脚本环境服务器
	执行命令：# sh init_regiondb.sh

2. City&Block初始化
    执行前提：大区库初始化完成。
    脚本路径：data_flow/import_city_block.sh
	配置文件：
	
3. 日出品POI库&POI+道路库初始化
    执行前提：母库初始化完成
    脚本路径：scripts/container/init_desgdb.sh
	配置文件：scripts/container/request/init_desgdb.json
	执行环境：脚本环境服务器
	执行命令：# sh init_desgdb.sh
	注意事项：过程中有一段大概15分钟左右没有输出导出日志，请耐心等待
	
4. GDB SNAPSHOT提取
    执行前提：大区库初始化完成
    脚本路径：scripts/container/rebuild_regiongdb_snapshot.sh
	配置文件：依赖脚本容器container统一配置
	执行环境：脚本环境服务器
	执行命令：# sh rebuild_regiongdb_snapshot.sh /XXX/ day
	注意事项：第一个参数为提取出来的文件存放目录，第二个参数值域为day|month
	
4. FM-META SNAPSHOT提取
    执行前提：元数据库初始化完成。
    脚本路径：scripts/container/exp_meta_to_sqlite.sh
	配置文件：依赖脚本容器container统一配置
	执行环境：脚本环境服务器
	执行命令：# sh exp_meta_to_sqlite.sh
	注意事项：生成的文件位于sys库中配置的元数据库下载目录
	
5. FM-META 模式图包提取
    执行前提：元数据库初始化完成。
    脚本路径：scripts/container/rebuild_fmmeta_snapshot.sh
	配置文件：依赖脚本容器container统一配置
	执行环境：脚本环境服务器
	执行命令：# sh rebuild_fmmeta_snapshot.sh /XXX/
	注意事项：第一个参数为生成的文件存放目录

6. 大区库日库、月库的Render初始化
    执行前提：大区库初始化完成。
    脚本路径：scripts/standalone/prjrender_rebuild/rebuild_regiongdb_render.sh
	配置文件：scripts/standalone/prjrender_rebuild/conf.properties
	执行环境：Hadoop主节点，hadoop用户登录执行
	执行命令：$ sh rebuild_regiongdb_render.sh /XXX/scripts/standalone/prjrender_rebuild/conf.properties
	
/* 作业过程中运维执行 */
1. POI批采集子任务号
    下个版本提供
	
2. 日落月
    脚本路径：scripts/container/createJob_poiDayRelease.sh
	配置文件：scripts/container/request/createJob_poiDayRelease.json
	执行环境：脚本环境服务器
    执行时机：加入Crontab每日01:00执行。

3. 日出品
    脚本路径：scripts/container/createJob_day2monSync.sh
	配置文件：scripts/container/request/createJob_day2monSync.json
	执行环境：脚本环境服务器
    执行时机：加入Crontab每日03:00执行。

3. 统计脚本
    无。

说明：以上脚本目录下如果有README.txt，请一定要确认文件中的说明，如有任何不确定的项，一定要和研发沟通，谢谢！

UPDATE SYS_CONFIG SET CONF_VALUE='/app/fm315/svr/scripts/container/' WHERE CONF_KEY='scripts.dir';
UPDATE SYS_CONFIG SET CONF_VALUE='DEV_R' WHERE CONF_KEY='render.table.prefix';
UPDATE SYS_CONFIG SET CONF_VALUE='amqp://fos:fos@192.168.4.130:5672' WHERE CONF_KEY='main.mq.uri';


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
