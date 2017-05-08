1. features下面不同要素文件夹下xml文件不能重名;
2. 100步的select 语句中，FROM TABLE_NAME P这部分不能变，生成insert 语句时根据正则"FROM\\s+(\\S+)\\s+P"匹配得到表名的；




---
下一步可优化：
1. 智能收集统计信息，没执行完一步，如果有insert temp表的，结束后都收集一下统计信息
2. 缓存main文件中的file对应的<step,List<SQL>>,根据不同main配置的文件组织一个完整的<step,List<SQL>>


------
从二代迁移过来说明：
1. scripts
  1) schema\temp_table_create.sql-->scripts\temp_table_create.sql
  2) schema\temp_table_create.sql-->scripts\temp_table_truncate.sql
                把create脚本复制一份，生成truncate语句脚本。
  3) logger.pck,PK_TABLE_STATS.pck,view.sql一般不变动

2. features
  1) all\exp-all-main*.xml-->features\main\exp-all-main*.xml
               文件内部去除：clean-index.xml, clean-data.xml, exp-shared.xml。
               文件内部增加：exp-ck_excep.xml, exp-m_para.xml。
  2) all\*.xml-->features\gdb\*.xml
               排除:1)中exp-all-main*.xml, clean-data.xml, clean-index.xml
     exp-simple-rd-link.xml文件内去除clean-data.xml, clean-index.xml
     *.xml中替换by-mesh,by-area为mesh,area,
  3) ck,log,m_para一般不变动

3. tools
  1) exp-shared.xml-->tools\remove-dup.xml
                从二代脚本内部把<step value="109">整个节点拷贝过来，替换相同节点