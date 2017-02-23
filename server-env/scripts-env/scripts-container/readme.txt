1. 大区库初始化
  script: init_regiondb.sh
  request: init_regiondb.json
  response: init_regiondb.json
2.子任务不规则圈导入
  loadTab2Oracle.sh
  执行说明：
  一个参数:tab文件
  sh loadTab2Oracle.sh /data/tmp/tt/10725.tab
  

  

  