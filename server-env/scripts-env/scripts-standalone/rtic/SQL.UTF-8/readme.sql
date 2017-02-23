1.plsql中依次执行T_VARCHAR_ARRAY.tps,VARCHAR_ARR.tps,COMMA_TO_TABLE2.fnc
2.执行 建表.sql；
3.执行dms_rticid_man.pck
4.执行号码池的初始化.sql [注意1.注意修改初始化.sql中的dblink的创建信息，指向rtic号码池； 2.每个大区库都要执行；]
验证：执行不报错，没有乱码