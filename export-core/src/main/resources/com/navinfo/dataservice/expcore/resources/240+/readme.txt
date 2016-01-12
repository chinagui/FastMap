1. features下面不同要素文件夹下xml文件不能重名;
2. 100步的select 语句中，FROM TABLE_NAME P这部分不能变，生成insert 语句时根据正则"FROM\\s+(\\S+)\\s+P"匹配得到表名的；




---
下一步可优化：
1. 智能收集统计信息，没执行完一步，如果有insert temp表的，结束后都收集一下统计信息
2. 缓存main文件中的file对应的<step,List<SQL>>,根据不同main配置的文件组织一个完整的<step,List<SQL>>