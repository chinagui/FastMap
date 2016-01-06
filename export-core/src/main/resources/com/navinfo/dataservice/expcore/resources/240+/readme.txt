1. features下面不同要素文件夹下xml文件不能重名;
2. 100步的select 语句中，FROM TABLE_NAME P这部分不能变，生成insert 语句时根据正则"FROM\\s+(\\S+)\\s+P"匹配得到表名的；