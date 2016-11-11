-- tables
TRUNCATE TABLE CK_SUITE_COP;
TRUNCATE TABLE CK_RULE_COP;
-- suite rows
insert into ck_suite_cop (SUITE_ID, SUITE_NAME, SUITE_RANGE, FEATURE)
values ('suite1', 'POI属性检查40项', '子任务范围', 0);

insert into ck_suite_cop (SUITE_ID, SUITE_NAME, SUITE_RANGE, FEATURE)
values ('suite2', '道路+POI点位检查558项', '子任务范围', 1);

insert into ck_suite_cop (SUITE_ID, SUITE_NAME, SUITE_RANGE, FEATURE)
values ('suite3', '道路+POI点位检查（clean）327项', '子任务范围', 1);

INSERT INTO ck_suite_cop (SUITE_ID, SUITE_NAME, SUITE_RANGE, FEATURE)
values ('suite4', '日线结构检查251项', '子任务范围', 1);

INSERT INTO ck_suite_cop (SUITE_ID, SUITE_NAME, SUITE_RANGE, FEATURE)
values ('suite5', '日线连通性检查4项', '子任务范围', 1);

COMMIT;
-- rule rows
-- suite1
insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR63095', '值域检查', 'IX_POI_NAME ( NAME )', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR73001', '繁体字检查', 'IX_POI_NAME(NAME)', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60038', '未知', 'IX_POI_CONTACT(POI_PID,CONTACT,CONTACT_TYPE,CONTACT_DEPART)应唯一', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60245', 'POI索引(基础信息)', 'IX_POI_CHILDREN(CHILD_POI_PID,RELATION_TYPE)应唯一', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60037', '标注检查', 'KIND_CODE为180104的分类，那么字段LABLE字段值一定包含“室内|”或“室外|”，否则报出', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60041', '引导坐标和显示坐标大于1000米检查', '检查同一ADMIN_ID内显示坐标和引导坐标之间距离在设定范围外(默认容差1000米)的记录距离允许用户分种别设定', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60063', '父子关系检查', 'POI标准化官方中文名称以“机场”或“機場”结尾，且"KIND_CODE"为230126时，一定不能在IX_POI_Children表中存在，否则报log“此设施是父的分类”', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60064', '父子关系检查', '种别代码为出发/到达（230127）的POI记录，一定在IX_POI_Children表中存在，且这些记录的父POI的种别代码一定为机场（230126）,否则报log；', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60065', '父子关系检查', 'Kind为机场（230126）并且标准化官方中文名称以“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾的记录，一定在IX_POI_Children表中存在，且这些记录的父POI的Kind一定为机场（230126）并且标准化官方中文名称以“机场”/“機場”结尾，否则报log；', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60066', '父子关系检查', '服务区/停车区（230206或者230207）不能在IX_POI_Children表中存在，否则报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60069', '父子关系检查', 'kind为机场（230126）的POI记录，其标准化官方中文名称应以“机场”/“機場”、“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾，否则报log：230126分类POI应以“机场”/“機場”、“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60074', '父子关系检查', 'IX_POI_Children表中医院内部POI（170104）、医院紧急出口（170105）的记录，这些记录的父POI的Kind只能是医院(170100、170101、170102)，否则报Log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60078', '父子关系检查', 'Kind为公园（180304）且名称中以“门”或“門”结尾的POI记录
1.一定在IX_POI_Children表中存在，否则报log1：公园门未制作父子关系。
2.这些记录的父POI必须为公园（180304），否则报log2：公园门只能作为公园的子。
3.这些子POI的标准化官方中文名称一定是以父POI的标准化官方中文名称开头，否则log3：公园门名称未以父POI名称开头。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60079', '父子关系检查', '若Kind为120101或120102，且POI标准化官方中文名称中含有“机场”或“機場”的POI记录，则这些记录的父POI的Kind只能是机场（230126）且父POI标准化官方中文名称一定以“机场”或“機場”、“航站楼”、“航站樓”、“候机楼”、“候机樓”结尾，否则报log：机场宾馆未与机场制作父子关系。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60080', '父子关系检查', 'IX_POI_Children表中Kind为停车场（230210）的记录，其标准化官方中文名称不应仅为“停车场”或“停車場”，否则报log：制作了父子关系的停车场名称错误。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60102', 'POI中文名称长度检查(MH)', '检查条件：IX_POI表中“STATE(状态)”非“1（删除）”
检查原则：标准化官方中文名称字段大于35个字符（不区分全半角，只计算字符个数，汉字也属于1个字符），需要报出。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60138', 'chain值与关键字对照检查(M)', '检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的记录；
检查原则：IX_POI中，种别（kind_code）为“150101”且标准化官方中文名中包含“品牌关键字与Chain值对照表”中HM_FLAG”=“D”对应的关键字“pre_key”，但POI的Chain值不等于配置表对应的Chain值，报出', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60143', '餐饮风味类型制作正确性检查', '检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的记录；
检查原则：在IX_POI表中，如果KIND_CODE在元数据表SC_POINT_FOODTYPE中POIKIND列中存在,并且KIND_CODE和CHAIN字段值与元数据表SC_POINT_BRAND_FOODTYPE（品牌分类与FOODTYPE对照表）同一行中的POIKIND和CHAIN值相等，但FOOD_TYPE不等于对照表中同一行的FOODTYPE,报Log。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60154', 'POI中文名称、中文地址检查', '检查条件：
  IX_POI表中“STATE(状态)”非“1（删除）”
检查原则：
 1、检查POI标准化官方中文名称或中文地址（FULLNAME）中“(”与“)”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”应成对出现
 2、括号“（”和“）”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”中间必须有内容。
 3、不允许括号嵌套，
否则报出', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60211', 'NAME非空检查', '检查条件：IX_POI表中“STATE(状态)”非“1（删除）”
检查原则：IX_POI中的NAME字段不能为空，否则报出', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60213', '父子POI距离检查', 'POI_KIND为元数据中表sc_point_poicode_NEW中的所有分类的父POI与其子POI之间的（显示坐标）距离不能大于2000m，否则以子POI为单位报log。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60222', 'POI引导坐标与土地覆盖的关系检查', '检查对象：种别不为230201、230202的POI点位
检查原则：
POI的引导坐标不应落入类型为“1”（海）、“2”（河川域）、“3”（湖沼池）、“4”（水库）、“5”（港湾）、“6”（运河）的土地覆盖面中，否则报出Log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60224', 'POI显示坐标与土地覆盖的关系检查', 'POI的显示坐标不应落在种别为水系（1～6）、高尔夫（12）、滑雪场（13）的土地覆盖面内，否则报log
屏蔽对象：
1.种别为“230201”、“230105”（港口、码头）、“230202”（立交桥）或标记字段含“水”的POI，如果落在种别为水系（1～6），不报log；
2.若种别为“180105/180106”（高尔夫）的POI落入种别为高尔夫的土地覆盖面内，不报log。
3、若种别为“180104”（滑雪场）的POI落入滑雪场的土地覆盖面内，不报log。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60225', '桥显示坐标与道路的关系检查', '检查对象：种别为“230201”或“230202”的POI
检查原则：
  1.如果引导Link的上下线分离属性为“否”，那么显示坐标到引导link的最短距离在1.5米到5米之间，报错
  2.如果引导link的上下线分离属性为“是”，那么显示坐标应位于该对上下线分离道路的中间', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60226', '显示坐标引导坐标跨道路检查', 'POI的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应与道路Link相交，否则报log
特殊说明：
1.若与该POI自身的引导Link相交，不报log；
2.若与连线相交的道路为高架、隧道、公交专用道路、跨线天桥、跨线地道属性，不报log；
3.若与连线相交的道路为高速（1级道路）、城市高速（2级道路）、10级路、或8级道路（非辅路），不报log。
4.若POI的种别230126、230127、230128，230105不报log。
5.若POI的显示和引导坐标跨未验证道路，不报LOG。
6.POI的种别为区域性12类主点（主点：父子关系中的父）：动物园（180308）、植物园（180309）、高尔夫球场（180105）、高尔夫练习场（180106）、游乐园（180307）、公园（180304）、港口（230125）、火车站（230103）、大学（160105）、景区（180400）、滑雪场（180104），不报log。', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60227', '显示坐标引导坐标跨铁路检查', 'POI的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应与铁路相交，否则报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60236', '父子关系重复建立检查', '重复检查：
一组父子关系的全部POI号码（包括父和子）为集合A，另一组父子关系的全部POI号码为集合B。若2个或2个以上的POI同时在集合A和集合B中存在，报log

例如：一组父子关系为A->B->C->D，另一组父子关系为A->M->C，则认为 A->C的关系重复建立
循环检查：
在一组父子关系中，其中一个POI在这组关系中既充当其它POI的父亲，又充当了其它POI的子，则认为这组POI存在循环建立父子关系，报LOG
例如：一组父子关系为A->B->C->D->A，则认为 A->D，与D->A是循环建立了', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60247', '［分类为110102的牛排的餐饮风味类型］错误检查', '检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的数据
检查原则：
分类(KIND_CODE)为110102（西餐），POI标准中文名称含“牛排”，并且风味类型（FOOD_TYPE）非"1001"（国际化）、“3007”（牛排）、“3008”（三明治）、“3010”、“3011”、“3013”（比萨）或为空的记录，报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60248', '［分类为110102的牛排的餐饮风味类型］错误检查', '检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的数据
检查原则：
检查分类(KIND_CODE)为110102（西餐），POI标准中文名称含"比萨"，“比薩”，"牛排""三明治"，“三文治”，并且风味类型（FOOD_TYPE）为"1001"（国际化）的记录，报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60249', '［制作牛排、三明治风味类型记录的分类］错误检查', '检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的数据
检查原则：
数据中已经制作的牛排（3007），三明治（3008）的餐饮风味类型，分类（KIND_CODE）不为“110102”或“110200”且chain为空或chain为3063的记录，报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60256', 'POI制作了多条餐饮风味类型的记录', '检查条件：IX_POI表中“STATE(状态)”非“1（删除）”的数据（非删除POI）
检查原则：同一个POI在IX_POI_RESTAURANT表中有多条记录（多条餐饮风味类型的记录），程序报出', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60257', '父子关系检查', '分类为“体检机构（170109）”的POI，若父分类为医院（170100、170101、170102）时，程序报A类LOG', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60271', '父子关系检查', '检查对象：
IX_POI中状态不为删除（state不为1）的POI
检查原则：
IX_POI_PARENT表中的POI分类（通过PID关联取IX_POI.KIND_CODE）不在"SC_POINT_SPEC_KINDCODE_NEW"中"TYPE"等于12的POI分类时，程序报LOG', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60282', '电话格式检查', '检查条件：
(1)IX_POI表中“STATE(状态)”字段为非1（删除）
检查原则：
1）如果联系方式为普通固话，以下情况报出：
a.电话号码不包含“-”；
b.包含“-”，但“-”后数字第一位为0或1；
c.存在数字（-,0，1，2，3，4，5，6，7，8，9）以外的，报log；
d.电话位数必须为12位或者13位，否则报log
e.电话区号第1位必须为0，否则报log
f.电话区号相同，电话位数不同，报log
2）如果联系方式为免费电话，以下情况报出：
a.不以400/800开头
b.以400/800开头，但长度不为10位
c.存在数字（0，1，2，3，4，5，6，7，8，9）以外的，报log；
3）如果联系方式为移动电话，以下情况报出
a.必须以1开头，必须11位数，否则报出
b.存在数字（0，1，2，3，4，5，6，7，8，9）以外的，报log；
4）如果电话号码为空，报LOG
5)如果联系方式为特殊电话，以下情况报出：
a.电话号码不包含半角的“-”；
b.电话区号第1位必须为0，否则报log；
c.存在以下字符或数字（-,0，1，2，3，4，5，6，7，8，9）以外的，报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60350', '普通POI关联隧道检查', '检查对象：IX_POI表中的全部POI
检查原则：POI的引导LINK属性为隧道时，报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60351', '普通POI关联航线', '检查对象：IX_POI表中的全部POI
检查原则：
POI的引导LINK种别为人渡、轮渡时，报log；', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60353', '普通POI关联跨线地道、跨线天桥', 'IX_POI表中的全部POI当POI种别为桥（230201）、立交桥（230202）、收费站（230208或230209）、出口（230203）、入口（230204）时，即使引导link符合指定的特征，也不需要报log。
检查原则：POI的引导LINK属性为跨线地道、跨线天桥时，报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60358', '普通POI关联全封闭', '检查对象：IX_POI表中的全部POI
当POI种别（POI表中对应字段名即为POI种别）为以下值时，即使引导link符合指定的特征，也不需要报log。
种别：桥（230201）、立交桥（230202）、收费站（230208或230209）、出口（230203）、入口（230204）；
检查原则：
如果POI关联的引导LINK属性为全封闭，报LOG', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60376', '冷饮店分类对应风味类型正确性检查', '检查对象：
IX_POI表中“STATE(状态)”非“1（删除）”的POI
检查原则：
①当KIND_CODE为110302时，FOOD_TYPE的值应在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”中POIKIND为110302对应的FOODTYPE中存在，否则报出；
②当FOOD_TYPE值在SC_POINT_FOODTYPE中“MEMO”字段为“饮品”对应的“FOODTYPE”存在时，则POI的KIND_CODE必须为SC_POINT_FOODTYPE中“MEMO”字段为“饮品”对应的POIKIND，否则报log；', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60378', '同一POI电话区号不同检查', '检查对象：
IX_POI表中“STATE(状态)”非“1（删除）”的POI
检查原则：
同一条POI存在多个电话时，多个电话对应的区号应一致，否则报log', null, null, 1, 'suite1');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60994', 'POI不应落在图廓上', 'POI的坐标不应位于图廓上，否则报log', null, null, 1, 'suite1');

commit;
-- suite2
insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13021', '关系型收费站检查', '若收费站类型不是“领卡”或“未调查”或“持卡打标识不收费”或“验票领卡”或“交卡不收费”，则收费通道的收费方式只能是“ETC”或“现金”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13020', '关系型收费站检查', '收费站类型为“领卡”或“持卡打标识不收费”或“验票领卡”或“交卡不收费”，收费通道的领卡类型只能有“ETC”或“人工”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13003', '关系型收费站检查', '如果一条link的两个端点都做有收费站，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01389', '调头口FC检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11037', '模式图号和形状及音声方向不匹配', '详见检查规则库附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13015', '收费站名称检查', '点收费站与关系收费站名称的拼音必须是与其汉字的拼音匹配，如果汉字是多音字，那么收费站名称的拼音必须是其所有多音字中的一个,否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11072', '分歧模式图号码检查', '分歧的进入线和退出线如果都是高速或者城高，那么一定要有分歧模式图号码', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11040', '分歧名称错误', '检查对象：相同进入线、退出线、相同类型的多个高速分歧名称。
检查原则：分歧名称不能重复。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11064', '分歧名称格式检查', '分歧名称中“中文”不允许有半角字符、全角小写字母；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11034', '名称中选项匹配错误', '分歧类型是方面且分歧模式图ID为空的不做检查。
如果分歧类型是普通，那么设施属性必须是默认，名称种别也必须是默认，并且不能有分歧名称。
如果分歧类型是IC（下面3种情况互斥，顺序检查），
1) 如果分歧退出线属性含SA（PA）和IC，则设施属性为默认，名称种别为IC，并且必须有分歧名称。
2) 如果分歧退出线属性含SA（或PA），则设施属性为SA（或PA），名称种别为SA（或PA），并且必须有分歧名称。
3) 如果分歧退出线属性是其他情况，则设施属性必须是默认，名称种别也必须是IC，并且必须有分歧名称。
如果分歧类型是方面（下面3种情况互斥，顺序检查），
1) 如果分歧退出线属性含JCT，则设施属性为JCT，名称种别为默认，并且必须有分歧名称。
2) 如果分歧退出线属性含IC，则设施属性为出口、入口，名称种别为默认，并且必须有分歧名称。
3) 如果分歧退出线属性含SA（或PA），则设施属性为SA（或PA），名称种别为默认，并且必须有分歧名称。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11031', '分歧/3d/高速入口-A-模式图号码检查', '图层所有的分歧模式图号必须在分歧模式图号列表中存在', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01474', '非新增POI的引导LINK值域检查', '非新增POI（state<>3）的引导LINK不能为空或0，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01475', '新增SA/PA类型POI的引导LINK值域检查', '新增的SA/PA类（种别为230206、230207）的POI的引导LINK不能为空或0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12002', '重复记录', '数据中一个点处的3d信息不允许出现两条记录的进入link号码和退出link号码完全相同的情况', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12011', '3D处顺行检查', '加了3D的进入LINK和退出LINK如果箭头符号分类为副，那么从进入Link到退出LINK不因该加顺行，同时路口上的路口语音也不应该加直行，否则报错。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12042', '分歧名称检查', '当分歧类型为方面分歧和IC分歧时，必须有分歧名称，且名称个数值域为[1,10]，否则报log
当分歧类型为高速分歧、3D分歧和普通分歧时，必须无分歧名称，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11066', '分歧名称格式检查', '分歧名称中拼音必须与名称匹配；（其中有几个特殊情况需要排除）
1.分隔符；例如：济南，拼音应为Ji''nan；
2.括号问题；名称中有括号，拼音中不应该有括号，例如：唐山（西），拼音应为Tangshanxi；
3.国道、省道、县道的名称；例如G103，拼音为Yilingsanguodao；
4.拼音中加了1、2、3、4数字的，例如：铜川，拼音应为Tong2chuan
对于分歧名称中的字母或数字在拼音和道路名拼音中原样转出，除以下特殊情况外
1、对于X+数字或字母的情况，拼音为“拼音（数字或字母）+xiandao”。
2、对于Y+数字或字母的情况，拼音为“拼音（数字或字母）+xiangdao”。
3、对于Z+数字或字母的情况，拼音为“拼音（数字或字母）+ zhuanyongxian”。
4、对于A+数字的情况，拼音为“A+拼音（数字）”。
5、对于G+3位数字的情况，拼音为“拼音（数字）+guodao”。
6、对于S+3位数字的情况，拼音为“拼音（数字）+shengdao”。
7、G+数字（1，2，4位数字）的情况，，拼音为“G+拼音（数字）”
8、G+1或2位数字+“S”/W/N/E,拼音为“G+拼音（数字）+ “s”/w/n/e”。
9、S+数字（1，2，4位数字）的情况，，拼音为“S+拼音（数字）”
10、S+1或2位数字+“S”/W/N/E,拼音为“S+拼音（数字）+ “s”/w/n/e+”。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11101', '线线关系检查', '分歧类别为“普通”、“方面”、“特殊连续分歧”时，其“经过线”不能为空，否则报err。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12001', '3d模式图编号规则检查', '详见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12043', '分歧名称检查', '当有分歧名称时，分歧名称和名称发音字段均不能为空', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12005', '3d进入线或退出线种别属性错误', '3d的进入线不能有交叉点内Link形态', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12010', '3D处顺行检查', '加了3D（提右、提左、调头、提左加调头、高速入口的3D除外）的进入LINK和退出LINK（具有步行街属性的退出LINK除外）如果箭头符号分类为C
1.若3D号码第二位为0，那么从进入路到退出LINK应该加顺行，否则报错。
2.若3D号码第二位不为0，那么从进入路到退出LINK不应该加顺行，否则报错。
特殊说明：
1.如果路口上的路口语音加了直行也算正确，如果既没有加直行也没有加顺行，那么报错；
2.如果进入link到退出link有分叉口提示，不进行检查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12009', '3D处顺行检查', '检查条件：NIDB-G：找到提右退出线outlink的非3D登记点
(A点挂接的非提右LINK为以下情况时，排除：10级路、交叉口内LINK、单方向进入A点的LINK、交限、A点挂了辅路和主路时，排除辅路，若只挂辅路则考虑）
检查原则：
1.如果A挂接link数大于3条，不继续检查；
2.如果A挂接的非提右3D退出线，有任意一条是双方向的，不继续检查；
3.如果A点除oulink外还有两条挂接link，link1和link2，那么两条link的方向相对（同时背离A点或者同时进入A点），不继续检查；
4.找到单方向退出A点的linkB，如果linkB的等级为高速和城高，不继续检查；
5.如果linkB的等级不为高速和城高，判断A点是否是路口，如果不是，报log1，不继续检查；
6.如果是路口，检查提右3D退出线到linkB是否制作了顺行，如果没有，报log2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12044', '逻辑关系错误', '制作分歧信息的点，若该点未制作路口或制作有单路口，则应至少挂接3条link，否则报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11100', '线线关系检查', '分歧类别为普通时。
设施属性和名称种别必须为默认。
分歧模式图ID的第二位数字必须是“3，4”中的一个。如果为“3”，则分歧模式图ID的第一位数字必须是“0，2”中的一个；如果为“4”，则分歧模式图ID的第一位数字必须是“0，2，3”中的一个。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08036', '交限详细信息', 'RD_RESTRICTION_DETAIL表中交限号码相同的多个RESTRIC_INFO字段的合，应与RD_RESTRICTION表中对应交限号码的RESTRIC_INFO字段值相同', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09005', '警示信息错误检查', '信息类型为“通用警示”的危险信息，文字描述为空报Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11006', '进入退出线重复记录', '分歧信息中（包含分歧、连续分歧），不能存在相同进入线，相同退出线，相同经过线的记录', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08038', '交限挂接Link数检查', '检查对象：普通交限信息
检查原则：
1.检查对象进入线的终点必须至少挂接3条Link，否则报log；
2.检查对象退出线的起点必须至少挂接3条Link，否则报log；
特殊说明：
1.若检查对象的进入线的终点与退出线的起点挂接在同一路口，则不报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11016', '出口编号错误', '检查对象：IC分歧，其“设施属性”和“名称种别”选项分别为“SA”、“SA”或“PA”、“PA”。
检查原则：检查对象中不能存在出口编号，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11033', '分歧/3d/高速入口-A-模式图号码检查', '图层所有的高速入口模式图号必须在高速入口模式图号列表中存在', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08014', '交限与交限重叠', '路口交限（普通时间段交限）的所有link完全包含在一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08033', '错误的交限进入线或退出线
', '路口交限的进入线和退出线不能为交叉口link
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08001', '多组时间段交限', '同一link上存在相同车辆类型、相同方向的多组时间段单向限制,程序报log.', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08016', '交限与交限重叠', '路口交限（普通非时间段交限）的所有link完全包含在一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11073', '分歧模式图号码检查', '如果是方面分歧并且进入线和退出线都不是高速或者城高，那么该分歧不应该有分歧模式图号码和出口编号', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11002', '分歧名称格式检查', '一组IC分歧或者方面分歧，其英文名称前后不能有空格，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11024', '进入线或退出线种别错误', '该分歧有分歧模式图号，那么进入线和退出线都必须是高速或城高，否则报Log1；该分歧没有分歧模式图号，那么该分歧应该是方面分歧且进入线是普通道路，否则报log2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11038', '分歧模式图添加错误', '详见检查规则库附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12022', '专用模式图检查', '制作了专用模式图（高速入口模式图除外）的3D信息，其进入和退出link不能同时为高速或城市高速，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12020', '专用模式图检查', '制作了提左加调头的3D，主退出与副退出的号码后七位必须完全相同，且与模式图号的后七位完全相同，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13022', '关系型收费站检查', '收费站类型不为“未调查”时，
ETC模式图号第1位必须为“T”；
收费通道数小于6时，第3位与收费通道数相同；
收费通道数大于等于6时，第3、7、8位一定为0；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13023', '关系型收费站检查', '收费站的收费通道数必须大于0小于等于16：
1、收费站通道数等于0时，报LOG；
2、收费站通道数大于16时，报LOG；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11032', '分歧/3d/高速入口-A-模式图号码检查', '图层所有的3D模式图号必须在3D模式图号列表中存在', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11029', '模式图编号错误', '检查对象：所有分歧（普通分歧、IC分歧、方面分歧）的模式图号码
检查原则：分歧模式图号码第二位如果是“a”，报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11111', '进入退出线重复记录', '相同进入线退出线不能即有线点线分歧（普通、IC、方面）又有线线分歧（普通、IC、方面），否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08030', '连接路属性交限检查', '针对“搜索提右路链本身”的合法性检查：
1.如果提右路链中只由一根提右link组成，若该link为双方向且该link不为交叉口内道路，则报log：“提右Link不能为双方向”。
2. 如果一整条提右路链全部为交叉口内道路，则报log：“提右路链中无道路连接路属性的link”。
3. 如果提右路链起点与多条非提右link挂接：
（1）若起点处未制作分歧，报log“提右没有制作3D”，报出该点。
（2）如果起点挂接的多条非提右link上都没有制作BRANCH_TYPE为“3D属性”的分歧，则报log：“提右没有制作3D”，报出起点。
屏蔽以下情况：
（1） 如果提右路链起点属于复合路口，并且该复合路口制作有提右3D，则不报log；
4.如果提右路链的起点处没有挂接其他link，报log“找不到合法的进入线”。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01472', 'POI与引导link的位置关系检查', 'POI索引中的GUIDE_LINK_SIDE的值与POI在道路中的位置不相符时，报log。
说明：点位于道路的距离判断修改为“1.5”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08009', '交限与交限重叠', '一组线线交限（普通非时间段交限）的所有link完全包含在另一组线线交限（普通非时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08043', '经过线距离检查', '交限经过线（过滤掉经过线中含环岛属性的link）应小于等于10条，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11115', '分歧名称编号类型检查', '分歧信息中名称的编号类型值不能为普通道路名、设施名、高速道路名，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11028', '模式图编号错误', '对象：高速、城高（同时包括进入线和退出线）的ＩＣ、方面、普通分歧的分歧模式图ＩＤ的第一位
原则：箭头图ＩＤ第一位不是“０”、“１”、“２”，报ｌｏｇ', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11039', '分歧退出线属性错误', '一组高速分歧（同一进入线，不同退出线）如果退出线没有匝道或者全封闭属性或者SA或者PA或IC的，程序报出', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11070', '分歧名称格式检查', '一组方面分歧，其英文名称中的数字与数字之间含有空格，报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13002', '关系型收费站检查', '收费站主点的挂接link数必须是2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12014', '提右处理论交限检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11098', '线线关系检查', '分歧类别为特殊连续分歧时，线线分歧经过线不为空，设施属性和名称种别为默认，模式图号码不能为空，箭头图代码的第一位数字必须是“1，2”中的一个，第二位数字必须为“a”。否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08013', '交限与交限重叠', '路口交限（普通非时间段交限）的所有link完全包含在一组线线交限（普通非时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08004', '错误的交限进入线或退出线', '原则：
满足以下条件之一的link不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线（也查线线经过线，但不查线线经过线为交叉口内link的情况）
（１）公交车专用道
（２）步行街
（３）时间段步行街（加了时间段禁止穿行，并且车辆类型限制同时且只制作了步行者、配送卡车、急救车的link）不能作为永久交限的进入、退出线
（４）时间段交限与时间段步行街的限制时间段有重合的link不能作为路口（包括线线）时间段交限的进入、退出线；
（５）出租车专用道（加了永久禁止穿行，并且车辆类型同时且只制作了允许出租车、急救车和行人的link）
（６）卡车专用道（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许配送卡车、运输卡车、急救车和行人的link）
（７）进不去出不来的link（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许步行者、急救车的link）
（８）车辆类型限制的景区内部道路的LINK（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许配送卡车、步行者、急救车的LINK）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08041', '交限类型检查', '交限的限制类型为“时间段禁止”时，交通标志必须为“实地交限”否则报log。
屏蔽：同一路口同一进入线同时存在时间段相同的时间段禁止左转和时间段禁止调头交限时,时间段禁止调头的交限标志为"理论交限",此种情况不报LOG;', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08031', '路口交限有经过线检查', '路口交限里不允许有经过线信息', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12016', '专用模式图检查', '制作了提左加调头的3D箭形分类必须有主（c）、副（e）退出线，且主(c)在左，副(e)在右，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09013', '高速合流危险信息检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12017', '专用模式图检查', '制作了提左、提右、调头、高速入口模式图的3D箭形分类只有副(e)退出线，没有主(c)退出线，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11104', '线线关系检查', '一组类别为普通或特殊连续分歧的线线分歧的进入link，必须也是一个单分歧的进入线，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11025', '模式图编号错误', '相同进入线，相同进入点,存在多个分歧类型相同的分歧，模式图编号必须相同
屏蔽对象：
1.如果一组3D分歧中存在两个分歧的箭头图号都以“e”开头，不报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11027', '模式图编号错误', '如果某分歧的分歧模式图ＩＤ第二位是“３”，同时第三位是“6”时，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11071', '分歧名称重复', '进入线相同的线线方面分歧名称不允许重复（普通道路方面名称的不查）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11015', '出口编号错误', '检查对象：普通道路方面分歧（进入线或退出线任意一根为普通路即为普通道路方面分歧）
检查原则：普通道路方面分歧中不能存在出口编号，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11003', '分歧名称格式检查', '一组IC分歧或者方面分歧，其英文名称中间不能含有连续空格，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11102', '线线关系检查', '多个线线分歧，它们之间具有相同的点号、进入线号、经过线号（包括顺序也相同），不同的退出线号。则它们的“分歧模式图ID”除第一位数字不同外，其它各位数字都应该相同，否则报err。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11059', '分歧出口编号检查', '线线关系的分歧只能为
1.模式图编号为空的方面分歧
2.3d分歧
3.连续分歧
否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11026', '模式图编号错误', '检查对象：BRANCH_TYPE为0,1,2,4的分歧
检查原则：进入线、进入点相同箭头图除第一位外的编号必须相同；相同进入线不同退出线，箭头图编号的第一位不能相同', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11048', '名称长度检查', '分歧名称不能超过35个汉字，拼音不能超过206个字符（去掉空格）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11065', '分歧名称格式检查', '分歧名称中拼音首字母必须是大写；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11030', '模式图编号错误', '检查对象：所有分歧（普通分歧、IC分歧、方面分歧）的模式图号码
检查原则：分歧模式图号码不能为如下值，否则报err。
00ff0004、10ff0004、20ff0004
00ff000d、10ff000d、20ff000d
00ff000f、10ff000f、20ff000f
00ff0010、10ff0010、20ff0010
00ff0012、10ff0012、20ff0012
00ff0013、10ff0013、20ff0013
00ff0017、10ff0017、20ff0017
00ff0019、10ff0019、20ff0019
00ff0008、10ff0008、20ff0008
00ff0015、10ff0015、20ff0015', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11063', '分歧名称格式检查', '分歧名称中“中文”不允许有分号、逗号、空格、tab符；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11068', '分歧名称格式检查', '一组方面分歧，其英文名称前后有空格，报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11060', '分歧出口编号检查', '线线分歧中记录的方面分歧不能有出口编号（因为该处的方面都是普通道路方面）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11004', '分歧名称格式检查', '一组IC分歧或者方面分歧，其英文名称中的数字与数字之间不含有空格，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11007', '逻辑关系错误', '分歧信息中退出线不应为交叉点内link属性', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11112', '分歧名称类型检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11036', '引导错误', '从进入线的方向终点开始，沿着交叉口内link，考虑方向，必须存在着至少一条可通行路径（该路径所有link都是交叉口内link），到达退出线的起点
增加说明：此条原则已包含RD_BRANCH表下各项类型', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11005', '名称一致拼音不一致', '检查指定目录下全部分歧名称中，中文名称相同时，其中文名称对应的拼音也应该相同，否则报err。
也包含线线“方面”分歧中的名称，一同进行检查，但不包含线线其它分歧类型。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12006', '3d进入线或退出线种别属性错误', '3d的退出线不能有交叉点内Link形态', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12021', '专用模式图检查', '制作了专用模式图（提右、高速入口模式图除外）的3D信息，其进入和退出Link都必须是7级以上（含7级）道路，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08025', '连接路属性车信检查', 'form含有“提前右转”的道路，若道路的端点属于一个复合路口，则不能做为直行或调头或左转的线点线车道信息的进入线，否则报log；
form含有“提前右转”的道路连接路，若道路的端点是单路口，则不能做为左转的线点线车道信息的进入线，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08003', '多组时间段交限', '线线交限中，排除货车交限后，同一进入退出线存在多组时间段信息,程序报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09004', '警示信息错误检查', '以下互相矛盾的警示信息不能添加在同一进入link和Node上：
1.“向左急弯路”与“向右急弯路”；
2.“上陡坡”与“下陡坡”；
3.“两侧变窄”与“左侧变窄”；
4.“两侧变窄”与“右侧变窄”；
5.“左侧变窄”与“右侧变窄”；
5.“有人看守铁路道口”与“无人看守铁路道口”；
6.“禁止超车”与“解除禁止超车”；
7.“左右绕行”与“左侧绕行”；
8.“左右绕行”与“右侧绕行”；
9.“左侧绕行”与“右侧绕行”；
10.“注意落石（左）”与“注意落石（右）”；
11.“反向弯路（左）”与“反向弯路（右）”；
12.“傍山险路（左）”与“傍山险路（右）”；
13.“堤坝路（左）”与“堤坝路（右）” ；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09009', '空的警示信息检查', '警示信息标牌类型不能为空，否则报log。log包括警示信息ID。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12019', '专用模式图检查', '制作了80100000、80100001、80000200、80000002、80000004的3D，退出LINK在以进入线延长180度的右侧，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12004', '3d进入线或退出线种别属性错误', '3d的进入线和退出线种别不能同时是高速（包括高速和城高种别）或者是9级路。注：排除专用模式图不查。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09003', '警示信息错误检查', '警示信息进入LINK在驶入警示信息所在Node的方向上可通行，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09012', '警示信息错误检查', '在一组警示信息的线点关系中，如果线具有交叉口link属性，则其警示信息类型只能是“停车让行”或者“减速让行”,否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09006', '警示信息错误检查', '相同点号和线号登记的警示信息不能超过6个，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09007', '警示信息错误检查', '警示信息Node点若挂接的进入线和退出线全部为高速或者城市高速，则该点上仅能挂接2条link，否则报log1；若Node点仅制作有“左侧汇入右侧合流（14401）”、“右侧汇入左侧合流（14402）”、“减速让行（20201）”、“停车让行(20101)”，不报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09010', '警示信息错误检查', '警示信息类型“停车让行”不允许制作在高速本线和直连IC和直连JCT上，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09011', '警示信息错误检查', '警示信息类型为“停车让行”时，其“长度”或“预告距离”应为0，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12012', '普通道路复杂路口模式图检查', '做了3D模式图以7开头的点，并且是路口的，则路口中相同进入线和退出线之间不能存在顺行，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09014', '值域检查', '只能存在以下警示信息，否则报log
1，向左急弯路 10201
2，向右急弯路 10202
3，反向弯路 10301
4，连续弯路 10302
5，上陡坡 10501
6，下陡坡 10502
7，两侧变窄 10701
8，右侧变窄 10702
9，左侧变窄 10703
10，窄桥 10801
11，双向交通 10901
12，注意儿童 11101
13，注意牲畜 11201
14，注意横风 11601
15，易滑 11701
16，村庄 12001
17，驼峰桥 12301
18，路面不平 12401
19，过水路面 12701
20，有人看守铁路道口 12801
21，无人看守铁路道口 12901
22，注意危险 13701
23，事故易发路段 13401
24，鸣喇叭 31501
25，禁止超车 22901
26，解除禁止超车 23001
27，堤坝路（左、右） "11901
"
， 11902
28，傍山险路（左、右） 11801
， 11802
29，注意落石（左、右） 11501
， 11502
30，左右绕行 13601
31，左侧绕行 13602
32，右侧绕行 13603
33，连续下坡 10601
34，通用警示 13702
35，注意左侧合流 14401
36，注意右侧合流 14402
37，会车让行 20301
38，路面低洼 12601
39，减速让行 20201
40，隧道开灯 14001
41，潮汐车道 14101
42，停车让行 20101
43，路面高突 12501
44，交通意外黑点 13703', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12018', '专用模式图检查', '制作了80000800、80000801、80000802、80000803、80000001、80000003的3D，退出LINK在以进入线延长180度的左侧，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03026', '路上点错误', '接边的两条link道路名信息的道路名称应一致（NIDB－K：只检查高速和城市高速）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01028', '10级路/步行街/人渡错误', '1.10级路/步行街/人渡不能是单向道路。
2.10级路/步行街/人渡不能具有IMI属性。
3.10级路/步行街/人渡不能是EG大门的进入、退出线。
4.10级路/步行街/人渡不能是SE的进入、退出线。
5.10级路/步行街/人渡不能是关系型收费站的进入、退出线。
6.10级路/步行街/人渡不能是点收费站的进入、退出线。
7.10级路/步行街/人渡不能是坡度的退出线、延长线。
8.10级路/步行街/人渡不能是分歧的进入线，退出线，经过线。
9.10级路/步行街/人渡不能是交限的进入线，退出线，经过线。
10.10级路/步行街/人渡不能是语音引导的进入线，退出线，经过线。
11.10级路/步行街/人渡不能是顺行的进入线，退出线，经过线。
12.10级路/步行街/人渡不能是车信的进入线，退出线，经过线。
13.10级路/步行街/人渡不能是警示信息的进入线。
14.10级路/步行街/人渡不能是可变限速的进入线，退出线，经过线。
15.10级路/步行街/人渡不能制作单向通行限制。
16.10级路/步行街/人渡不能制作超车限制。
17.10级路/步行街/人渡不能制作条件限速（RD_LINK_SPEEDLIMIT表中的SPEED_TYPE=3）。
18.10级路/步行街/人渡不能制作红绿灯受控信息.', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01364', '种别检查', 'link种别为0作业中道路时，报log
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01012', '无方向', '有上下线分离、环岛的Link，不能是双方向或未调查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03050', '异常点JCT', '种别属性为“属性变化点JCT”的点至少挂接2条第一官方名称的NAME_GROUPID不相同的高速或城高的本线（NDB即上下线分离，NIDB即CA并且非IC，JCT，SAPA，无属性）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03077', '异常点JCT', 'Node的“JCT”形态不能与“隧道”、“桥”形态共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03053', '异常点JCT', '点属性不为“JCT”的点，如果至少挂接2条第一官方名称不相同的高速或城高的本线（NDB即上下线分离，NIDB即CA并且非IC，JCT，SAPA，无属性），那么报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03008', '路上点错误', '挂接的两条link的供用信息应一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03032', '路上点错误', '接边的两条link道路名信息的路线属性应一致（NIDB－K：只检查高速和城市高速）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03078', '异常大门点', '制作有大门信息的Node必须是平面交叉点（无属性），否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03031', '路上点错误', '接边的两条link道路名信息的是否虚拟名称应一致（NIDB－K：只检查高速和城市高速）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03015', '路上点错误', '挂接的两条link的铺设状态信息应一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01264', '速度等级错误', '限速类型为“普通”时，私道形态的速度等级不能为1、2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05049', '分歧模式图位数检查', '1.当分歧类型选择为“0”“2”“3”“4”时，模式图号码一定为8位，否则报错；
2.当分歧类型选择为“1”时，如果进入link、退出link和经过link的道路种别同时为“1：高速公路”或“2：城市高速”时，模式图号码一定为8位，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05041', '提右复合路口个数', '记录了提右3D的信息点，如果是复合路口，那么该复合路口组成点的个数不能大于2个，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03017', '路上点错误', '路上点种别的Node挂接的两条link的左右车道数信息应该一致（路上点收费站Node除外）；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07004', '信号灯受控检查', '如果一个link的受控标志为“受控制”，那么该link必须为一个路口的接续link且方向为进入该路口，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01237', '速度限制变化点错误', '检查对象：限速类型为“普通”、长度小于100米的link，且该link两个端点分别只挂接1条link（步行街、桥、隧道属性、10级路、航线除外），
检查原则：
1、当该link与两端挂接link其中任意一根link速度限制相同，程序不报log；若与两端挂接link速度限制都不同，则报log；
2、如果检查对象为单向通行，则只查可通行方向；
3、如果挂接的link某个方面不能通行，则在不能通行方向上的速度限制值不作为参考与检查检查对象的速限值进行比较；
增加屏蔽条件：
1、当检查对象的种别为10级路（RD_LINK.KIND = 10）、航线（RD_LINK.KIND= 11、13）时，不报log；
2、当检查对象的道路形态包含“区域内道路”（RD_LINK_FORM.FORM_OF_WAY = 52）、“停车场出入口虚拟连接路”（RD_LINK_FORM.FORM_OF_WAY = 54）、“步行街”（RD_LINK_FORM.FORM_OF_WAY = 20）、“桥”（RD_LINK_FORM.FORM_OF_WAY=30）、“隧道”（RD_LINK_FORM.FORM_OF_WAY=31）时，不报log；
3、当检查对象如果是点限速关联link时，不报log；
4、当检查对象为8级路（RD_LINK.KIND=8）限速值为15时，一端挂接7级路一端挂接10级路的，不报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01238', '速度限制变化点错误', '一个点（非收费站、桥、隧道、CRFI点）只连接了两条Link（任意一根link不能存在交叉口link属性），两条Link的限速类型为“普通”时，同一速度限制方向上的值相差不应大于等于60公里，（除非link上有IC、JCT、桥、过街天桥、步行街、普通匝道属性、隧道），否则报err
（10冬link开放两边速限值不一致）
屏蔽：
1.挂接两条link中任意一根link是点限速关联的link，不查
2.如果两根link中，其中一根link的道路等级为8或者10，则不检查；但是两根都是8级或者都是10级则需要检查
3.如果两根link中，一根link是双方向，另一侧是单方向，则不检查；
4.如果两根link的限速来源都是未调查的，则不检查；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01040', '收费有误', '3级及以下道路的收费信息不能为“收费”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01241', '速度限制错误', '限速类型为“普通”时，（1）步行街为双方向时，速度限制必须等于10km/h
（2）当步行街为单方向时，和道路方向相同的速度限制必须等于10km/h，和方向相反的速度限制必须等于0km/h', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05019', '分歧点箭头图标识检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07003', '红绿灯类型检查', '路口红绿灯类型不能为“人行道红绿灯”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07001', '红绿灯控制道路检查', '1.有红绿灯的路口，所有的进入link（双向通行link视为进入link）必须制作有红绿灯控制信息（即受红绿灯控制或者不受红绿灯控制），否则报log1；同时，所有退出link必须未制作有红绿灯控制信息，否则报log2；
2，如果一个路口中的进入link制作有红绿灯控制信息，那么该路口一定有红绿灯，否则报log3；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03071', '异常图廓点', '非图廓点只能有一条图幅号码记录', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03081', '障碍物检查', '若点具有障碍物信息，且该点挂接有FC为1-4的Link报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03061', '异常桥属性点', '有平面交叉点“桥”属性的点的挂接桥link的个数必须大于0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03018', '路上点错误', '挂接的两条link的幅宽应一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03070', '异常图廓点', '同一图幅内图廓点的挂接link数应该等于1', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23005', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中DIRECT(电子眼作用方向)
检查原则：  
电子眼的作用方向应该与道路的方向一致，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM15005', '点限速错误', '当“内业作业状态”≠2时，做如下检查：
检查对象：RD_SPEEDLIMIT表中DIRECT(限速标牌作用方向)
检查原则：  
检查值域不能为0，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01251', '速度限制来源一致性检查', '限速类型为“普通”时,若双向道路一侧的限速来源为“方向限速”，则另一侧的限速来源也必须为“方向限速”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01092', 'FC错误', '私道的FC应该为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08044', '同一位置多组交限检查', '1.相同进入线，相同退出线，不同经过线的多组交限，交限号码不应相同，否则报log；
2.相同进入线相同进入点，不同退出线的多组普通交限，交限号码应相同，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01296', '线线结构构成link检查', '线线结构的进入线和退出线不能挂接在一个点上，也不能挂接在一个复合路口上；
以下情况例外：
1.线线信息为车信，且进入线是交叉口内link
2.线线信息为车信，且退出线是环岛或特殊交通
3.线线信息为车信，且退出线沿车信方向的起点挂接了环岛或特殊交通类型的link。
4.经过线不都是连接此复合路口', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01009', '无方向', '大于等于3个点且含有“双方向”交叉口内link的路口中，存在两条交叉口内link之间方位角之差的绝对值大于25°', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01088', 'FC错误', '“未铺设道路”的FC不能为1或2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01407', '无方向', '有IC、JCT、SA/PA形态的Link，不能是双方向或未调查
屏蔽：具有SA/PA+POI连接路属性的link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01013', 'LINK与图廓线重合', '道路link不能与图框线全部或部分重合', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01249', '速度限制错误', '限速类型为“普通”时，限速来源如果是“匝道未调查”，则道路必须含有匝道形态，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01115', '车道等级检查', '每条LINK都必须有车道等级（大于0）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01266', '速度等级错误', '检查对象：link含“匝道”属性，且种别不为8级路。
检查原则：限速类型为“普通”时，该link速度等级不能为8，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01257', '速度等级错误', '限速类型为“普通”时,步行街形态速度等级必须=8；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01342', '特殊种别属性', '“JCT转IC”形态必须与“JCT”形态共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01406', '道路连接路检查', '检查对象：调头口模式图（80000003）
检查原则：
调头口前方60米内应存在复合路口：
a）检查对象沿进入线的延长线方向（不考虑中间有无道路打断）在60米以内（包括60米）能找到路口，否则报log；
b）检查对象沿退出线的逆方向在120米以内能找到路口，否则报log；
2c）在2a）和2b）中找到的路口，应为同一个路口，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01256', '速度等级错误', '限速类型为“普通”时,非全封闭道路速度等级必须>=2，即必须为2-8；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01026', '形状点在图廓上', '除起终点，Link的形状点不能在图廓上', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01252', '速度限制来源一致性检查', '在道路的可通行方向上，限速类型为“普通”时,线限速来源不能是“无”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01349', '线-A-FC错误', '若点具有障碍物信息，且该点挂接有FC为1-4的Link报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01234', '主辅路的连接link未作复合路口', '具有“主辅路出入口”形态的Link，且道路方向为双方向或未调查，应制作成复合路口，否则报err。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01244', '速度限制错误', '限速类型为“普通”时，单方向道路速度限制方向必须与道路方向相同', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01391', '停车场检查', '检查对象：一个Node挂接有n条Link，其中一条具有“停车场出入口连接路”属性且通行方向为进入该Node，同时其他n-1条Link均为8级路、具有“POI连接路”属性、制作有穿行限制
检查原则：
1.具有“停车场出入口连接路”属性的Link应同时具有“POI连接路”属性，且制作有禁止穿行信息，否则报log1；
2.其他n-1条Link一定不能具有“停车场出入口连接路”属性，否则报log2；
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01258', '速度等级错误', '限速类型为“普通”时，双方向道路速度等级必须>=3，即必须为3-8，否则报log
屏蔽：如果道路含有JCT且有调头口，则不报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01239', '速度限制错误', '限速类型为“普通”时，道路可通行方向上的速度限制均不能为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01245', '速度限制错误', '1) 步行道路种别的Link,限速类型为“普通”时，速度限制必须等于10km/h，否则报log1
2) 非引导道路、步行道路种别的Link,限速类型为“普通”时，限速来源必须是“未调查”，否则报log2。
3）不具有的SA或PA属性且link“供用信息”不为“未供用”（APP_INFO≠3）的高速、城市高速,限速类型为“普通”时，限速来源不能是“未调查”，否则报log3
4）非引导道路种别的Link,限速类型为“普通”时，速度限制必须等于15km/h，否则报log4', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01395', '环岛限速检查', '如果是7级及以上的环岛或特殊交通为“是”，限速值小于等于11时报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01184', '上下线分离之间LINK未登记复合路口', '一条link的两个端点，如果各挂接了1条MD属性link（非高速，非城市高速），所挂接的同一组MD属性link的 NAME_GROUPID有一组相同(或者同时无NAME_GROUPID)时，并且该link本身不是交叉口内link、环岛、上下线分离、特殊交通类型，报log；如果该link的两个端点是两个不同的路口，那么屏蔽log；link长度如果超过50米，则不做该检查；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01097', 'FC错误', '公交专用道形态的道路FC应为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01259', '速度等级错误', '限速类型为“普通”时,FC=1/2/3的道路，速度等级必须<=7，即必须为1-7；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01003', '航线/水逻辑关系错误', '一条航线link串的两个端点，必须都在水系面上，并且这条航线link串都包含在水系内', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01377', '车道数与幅宽检查', '检查对象：车道数（如果总车道数不为空，使用总车道数；如果总车道数为空，使用左车道数+右车道数）、幅宽
检查原则：不满足以下对应关系的报log：
如果车道数为1，幅宽必须为30；
如果车道数为2或者3，幅宽必须为55；
如果车道数为4-15，幅宽必须为130；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01180', '交叉点内道路非路口', '1.交叉口内LINK应属于某一个路口，否则报LOG1
2.LINK的两个NODE点属于同一个路口，则该LINK应属于该路口，否则报LOG2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01255', '速度等级错误', '限速类型为“普通”时，全封闭道路（不含有JCT属性）限速等级必须<6，即必须为1-5；
屏蔽：link的供用信息为“未供用”（APP_INFO=3）的不检查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01104', '车道数与幅宽检查', '轮渡、人渡、9级路、10级路的总车道数必须为1', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01179', '高速与普通相连', '检查对象：高速、城高本线入口的点，与其挂接的进入线种别为非高速或城市高速、非十级路的普通道路，退出线种别为高速或城市高速，属性含IC
检查原则：沿着退出线方向，考虑link通行方向和交限的前提下，查找所有通行路径，对于不能进入高速或城高的路径要报log，即将退出线记录入log图层；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01261', '速度等级错误', '限速类型为“普通”时，人渡/轮渡种别、SA/PA形态（不含有JCT属性）、POI连接路形态Link的速度等级必须为7', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01086', 'FC错误', '8/9级道路FC必须为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01023', '形状有异', '背景link本身折线的角度不应小于用户所设角度（程序默认值为30）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01384', '道路连接路检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01247', '速度限制错误', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60218', '显示坐标与道路距离检查', '检查对象：状态字段为“新增”或“修改”且种别不为230201、230202的POI点位
检查原则：逐一判断POI点显示坐标距离道路最近距离是否小于5米且大于1.5米，如果小于5米且大于1.5米则报出log
屏蔽对象：
1、若POI的标记字段含有“路”，不报log
2、POI的类别为小区（120201）、轮渡（230125）、收费站（230208）、磁悬浮主点（230115）、地铁站主点（230112）、长途客运站出入口（行人导航）（230110）、出租车停靠站（230117）、出租车站出入口（230118）、公交车站、BRT（230101）、公交车站出入口（230102）、缆车站出入口（230122）、缆车站主点（230121）、水上公交车站出入口（230124） 、水上公交站（230123）、小巴出入口（行人导航）（230120）、专线小巴站（230119），不报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01032', '点挂接link检查', '检查对象：Node点，其上仅挂接4条上下线分离属性link，其中有2条为交叉口link属性。
检查原则：Node点上挂接link的方向，必须为2条link进入该Node（一条为交叉口内link，一条为非交叉口内link），两条link退出该Node（一条为交叉口内link，一条为非交叉口内link）。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01031', '长度过短', '道路link长度应大于2米（交叉点内link不查）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01272', '速度等级错误', '检查对象：具有M属性且限速来源是“未调查”，限速类型为“普通”的link
屏蔽对象：具有匝道属性的link不查
检查原则：其限速等级与该link所挂接的link中限速等级值最小的相同，否则报log（检查对象不为8级路时，挂接link排除8,9,10级路、包含步行街属性的link；检查对象为8级路时，挂接link排除9,10级路、包含步行街属性的link）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01017', '航线检查', '轮渡/人渡种别的Link不能作为交限的进入线、经过线或退出线。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01385', '提左linkFC检查', '检查对象：提左3D的退出线（3d号码是80000800、80000802、80000801和80000803的，视为提左3d）
检查原则：
1.若检查对象的IMI代码不为2（转弯道）且不具有提左形态，报log1；
2.若检查对象的IMI代码为2（转弯道）或具有提左形态，那么沿着转弯道或提左形态的Link往下搜索，确定一组转弯道Link或提左Link。这组Link的FC要随与其挂接Link中最低的FC，否则报log2；

检查对象退出方向接续Link的判断标准：
在搜索到提左link组最终结束点的时候，选择接续link中与提左最终link方向一致（可通行），并且没有禁止通行普通交限且不具有辅路形态的link；若除去禁止通行普通交限的link和辅路形态的link不存在其他退出路，提左link的FC随进入道路；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60225', '桥显示坐标与道路的关系检查', '检查对象：种别为“230201”或“230202”的POI
检查原则：
  1.如果引导Link的上下线分离属性为“否”，那么显示坐标到引导link的最短距离在1.5米到5米之间，报错
  2.如果引导link的上下线分离属性为“是”，那么显示坐标应位于该对上下线分离道路的中间', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01039', '收费有误', 'IC属性道路（直连IC除外）的收费信息不能为“收费”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01297', '线线结构构成link检查', '退出线不能是交叉口内link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60234', '标注“路”的POI不位于道路上检查', '检查对象：标注字段中含有“路”的POI(排除标记“跨路”的数据)
检查原则：
1.如果引导Link的上下线分离属性为“否”，那么显示坐标到引导Link的最短距离应小于1米；
2.如果引导Link的上下线分离属性为“是”，那么显示坐标应位于该对上下线分离道路的中间；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01025', '形状有异', 'link的起点应该与该link的第一个形状点坐标一致，否则报err；
link的终点应该与该link的最后一个形状点坐标一致，否则报err；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01090', 'FC错误', '步行街的FC必须为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01008', '无方向', '全封闭道路不能为“双方向”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01108', '总车道数和左右车道数约束检查', 'Link上的总道数和左右车道数不能同时为空或同时为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01106', '车道数与幅宽检查', '道路等级为轮渡、人渡、9级路、10级路，形态为步行街以外的道路，幅宽值都应大于3m', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01105', '车道数与幅宽检查', '步行街属性的总车道数必须为1', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01038', '收费有误', 'JCT属性的道路的收费信息取决与连接的两条高速的收免费信息，两条高速都收费时，JCT属性的link必须为收费，两个高速只要有一边免费，JCT属性的link就必须为免费', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01359', '主辅路连接路速度限制检查', '检查对象：一组连续的包含有“主辅路出入口”道路连接路属性的link，且这组link有且仅有一端挂接有辅路属性的link
检查原则：
检查对象的限速类型为“普通”时，检查对象所挂接的辅路的速度限制值，应等于或低于检查对象另一端所挂接link中最低的速度限制值，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01083', '提右linkFC检查', '检查对象：大陆数据中，当存在3d号码分别是8010 0000和8000 0200和8010 0001时，却没有制作提左或提右属性的一组Link

检查原则：
直接报出LOG，人工确认FC正确性。（量小，北京只有不到30处）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01010', '无方向', '有特殊交通类型形态的Link，方向不能为双方向或未调查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01240', '速度限制错误', '1.限速类型为“普通”时，全封闭道路速度限制必须≥50km/h，全封闭道路有桥或隧道或JCT或IC或匝道形态的应屏蔽不报LOG；
如果全封闭道路速度限制<50km/h且顺向\逆向限速来源为"现场标牌"的屏蔽不报log；
2.非全封闭道路必须<=130km/h', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05056', '名称语音的值域检查', '检查对象：RD_BRANCH_NAME表中LANG_CODE为CHI或CHT的记录
检查原则：
1.VOICE_FILE字段中不能为空值，否则报LOG
2.VOICE_FILE字段中只能存在半角英文字母、半角数字“1”“2”“3”“4”和半角字符“''”，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01248', '速度限制错误', '限速类型为“普通”时，线限速的来源只能是“现场标牌”、“城区标识”、“匝道未调查”、“未调查”、“方向限速”中的一种，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01357', '总车道数和左右车道数约束检查', '单方向的link上不能存在左右车道数，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01221', '特殊种别属性', '道路种别和形态共存性参见元数据库SC_ROAD_KIND_FORM', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01358', '主辅路连接路速度限制检查', '检查对象：一组连续的道路连接路属性包含有“主辅路出入口”的link
检查原则：
1.若检查对象的属性应全部为辅路或全部为匝道，否则报log1；
2.检查对象一端挂接都为非辅路，另一端至少挂接一条辅路，否则报log2；
4.检查对象的限速类型为“普通”时，速度限制值与挂接link中（8,9,10级路除外；除包含步行街属性的link；若检查对象为8级路则9,10级路除外；若检查对象是9级路，则随9级辅路中低的速度限制）有辅路属性且限速类型为“普通”的最低的速度限制值相同，否则报log4；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM15007', '限速值与限速来源关系检查', '当道路为单方向时，顺向或逆向速度限制为0时，对应的速度限制来源应该为0，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05097', '分歧进入link', '分歧类型是IC的分歧，其进入link一定是高速本线（NBT本线），否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01348', '线-A-FC错误', '检查对象：根据普通交限或方向判断出可通行的路径(在道路的未制作普通交限的可通行方向上)

屏蔽条件：满足以下任一条件的道路不进行检查
1.检查对象长度大于200米；
2.检查对象道路等级为8或9或10级路；
3.检查对象具有交叉口内link或私道或SA\PA属性；
4.检查对象有穿行限制或车辆限制或施工中不开放；

检查原则：如果检查对象与其两端挂接link的FC都不相等报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01095', 'FC错误', '含“区域内道路”形态的link的FC应为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01393', '停车场检查', '具有“停车场出入口虚拟连接路”属性的道路，等级必须为8，车道总数必须为1，通行方向必须为双方向，必须制作有穿行限制信息，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01021', '航线检查', '一个Node点只挂接了航线，没有挂接其他道路（图廓点除外）。若该Node挂接航线link数为1或大于等于3，报err（NIDB－K：挂接link数为2，也报err）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01181', '桥/水逻辑关系错误', '一条完整的桥（若干段桥属性link连接在一起）的起终点不能落在水中', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01260', '速度等级错误', '限速类型为“普通”时,FC=4的道路，速度等级必须>1，即必须为2-8；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01043', '收费有误', '除IC、JCT之外的CA道路的收费信息必须连贯（不考虑跨图幅），判断是否连贯的距离限制是500米，如果500米内出现不连贯，则报log，如果超过500，则不报', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01250', '速度限制错误', '匝道形态的道路上，限速类型为“普通”时，限速来源不能是“未调查”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01107', '车道数检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01018', '航线检查', '轮渡/人渡种别的Link必须是双方向（不包括未调查），否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01268', '速度等级错误', '检查对象：含辅路属性的link；
检查原则：限速类型为“普通”时，该link上的速度限制等级不能为1，2，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01005', '1/3处未打断线', '一根有SA/PA属性的Link，起止点不能挂在同一条高速（或城高）线上', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01386', 'FC错误', '检查对象：具有全封闭属性的道路
屏蔽对象：满足以下任一条件，不做检查
1.检查对象制作了车辆限制或穿行限制信息或施工中不开放；
2.检查对象为断头路（道路的一个端点仅挂接一条Link）
检查原则：检查对象的FC不能为5，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01394', '停车场检查', '具有“停车位引导道路”属性的道路，等级必须为8，速度限制等级必须为8，车道等级必须为1，道路功能等级为5，不应具有“POI连接路”属性，且一定制作有穿行限制，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01263', '速度等级错误', '限速类型为“普通”时，未铺设的速度等级不能为1、2、3，', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01041', '收费有误', '直连IC（IC+CA）道路的收费信息取决与连接的高速的收免费信息，高速收费时，直连IC的link必须为收费，高速免费，直连IC的link就必须为免费，如果直连IC出现分叉，直接报log，不继续检查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01267', '速度等级错误', '检查对象：含“环岛”属性的link；
检查原则：限速类型为“普通”时，该link上的速度限制等级不能为1，2，3，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01082', '提右linkFC检查', '检查对象：提右3D的LINK串（3d号码是8010 0000和8000 0200和8010 0001的，视为提右3d）
检查原则：
1.提右LINK串中各条link的FC值应当相同；
2.取提右link串退出线中FC值最小的（假设为A），与提右link串进入线的FC值（假设为B）比较，取两者间值大的（MAX（A,B））,提右link串的FC应与此最大值相同；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01353', '交叉口内linkFC错误', '检查对象：复合路口。

屏蔽对象：满足以下任一条件的复合路口不进行检查。
1.全部交叉口内Link都具有M属性；
2.全部交叉口内Link都具有II属性且其中两个交叉口内link的道路连接路属性为“主辅路出入口”；

检查对象挂接Link的屏蔽条件：
1.具有穿行限制、车辆限制或施工中不开放的Link；
2.9、10级路

路口挂接Link的FC个数计算方法：
所挂接Link的FC中共具有几种FC数值，个数算作几；不同的Link具有相同的FC时，个数不重复累计，仍然算作1；

检查原则：对检查对象所挂接Link的FC个数进行判断（满足检查对象挂接Link屏蔽条件的不计），
1.若FC个数大于或等于3 4，报log；
2.若FC个数等于1，则复合路口内的所有交叉口内Link的FC应与之相等，否则报log；
3.若FC个数等于2或3或4（下文用最高和次高和最低表示），取FC最高的挂接Link（屏蔽具有普通交限、通行方向不一致、同属一个CRFR的Link）查找可通行路径：
3-1.如果可通行路径个数为0，报log并停止当前路口的检查；
3-2.如果最短（Link个数最少）可通行路径个数为1，则该路径中的交叉口内Link的FC都应为最高（路径中8,9级的交叉口内LinkFC可不为最高），同时复合路口内的其他交叉口内Link的FC都应为次高（8,9级的交叉口内LinkFC可不为次高）；
3-3.如果最短（Link个数最少）可通行路径个数大于1，去掉含有辅路属性的路径，其余路径中的任意一个路径所包含的交叉口内Link的FC为最高即可（路径中8,9级的交叉口内LinkFC可不为最高），同时复合路口内的其他交叉口内Link的FC都为次高（8,9级的交叉口内LinkFC可不为次高）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01243', '速度限制错误', 'FC为1，2，3的道路，限速类型为“普通”时，速度限制必须>=15km/h；
FC为4的道路，限速类型为“普通”时，速度限制限制必须<=130km/h', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01363', '收费有误', 'SA、PA属性的道路的收费信息不能为“收费”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01269', '速度等级错误', '检查对象：含“特殊交通”属性的link；
检查原则：限速类型为“普通”时，该link上的速度限制等级不能为1，2，3，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01383', '道路连接路检查', '调头口3D（80000002、80000004）的退出线
检查原则：检查对象的道路形态必须具有“调头口”，且不能存在除“跨线天桥(Overpass)“、”跨线地道(Underpass)“、”跨线立交桥“、“匝道”以外的形态，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01042', '收费有误', '所有道路的收费信息不能为“收费道路的免费区间”或“未调查”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01110', '总车道数和左右车道数约束检查', 'Link上的左右车道数不能一边有值一边无值或为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01270', '速度等级错误', '限速来源是“匝道未调查”的匝道的限速等级随挂接Link中速度等级低的，具体原则如下：
检查对象：对同时满足以下条件的link进行检查
1.具有匝道属性
2.限速来源是“匝道未调查”
3.限速类型为“普通”
检查参考对象：沿检查对象的通行方向进入的端点，该端点上挂接的不在屏蔽对象内的link
屏蔽对象：满足以下任一条件的link不作为检查参考对象
1.检查对象本身
2.沿通行方向无法进入检查对象，和沿检查对象的通行方向无法进入的link
3.做有从检查对象至该link的通行限制信息（普通交限）
4.道路等级为8,9,10的道路（如果检查对象为8级路，则只屏蔽9,10级路）
5.所挂接道路除包含步行街属性的link
检查原则：检查对象的速度等级与检查参考对象中限速等级最低的相同，否则报log1；
特殊情况说明：
1.如果除去满足屏蔽对象前3个条件的link后，检查参考对象只剩下1条link，则检查对象的速度等级与该link相同，否则报log1', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01089', 'FC错误', '制作了穿行限制或车辆限制道路FC必须为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01085', 'FC错误', '功能等级不能为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05075', '3D专用模式图检查', '制作了调头专用模式图（80000001）的点，沿此端点的非交叉口LINK退出线（退出线不包含提右或提左属性）方向查找60米以内（包括60米）的所有点（点集合I），同时沿另一个端点B的进入线方向查找120米之内的所有点（点集合II），检查集合I中是否有点和集合II中的点构成复合路口（单独调头口的复合路口除外）。
1.若是，且该复合路口挂接有大于等于3条非交叉口内Link，不报LOG；
2.除1以外的其他情况，直接报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01235', '非全封闭与全封闭属性相连检查', '检查对象：高速城高种别的道路
检查原则：
若检查对象与具有全封闭属性的link挂接,则检查对象应至少具有以下属性中的一个：
1.匝道
2.全封闭
3.SA
4.PA
否则报log。程序报出非全封闭link的ID。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01254', '速度等级错误', '限速类型为“普通”时,速度等级值不能为“未调查”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05082', '复杂路口模式图检查', '1.分歧类型为“4：复杂路口模式图”时，进入link必须为7级及以上的普通道路，不可为高速、城市高速，否则报log，退出道路不可为9级和10级，否则报log2.制作了3分叉的复杂路口模式图（模式图号为70100040,70100400,70000040,79100400,71000040）应满足以下任意条件，否则报log2-1.只能挂接3条退出线（不考虑交叉口内link），且挂接在同一个点上，否则报log，并且制作了3条相同进入线，不同退出线的3d记录;2-2.箭头图编号第一位只能是“c”,"e","d";且同一进入link的关系中第一位不能出现重复。2-3.箭头图号为“c0100040”的退出link必须在中间，其左侧箭头图号首位为“d”，右侧箭头图首位为：“e”，否则报log2-4.箭头图号为“c0100400”、“c9100400”的退出link必须在最左侧，其右侧相邻的箭头图首位为“e”，最右侧箭头图首位为“d”，否则报log2-5.箭头图号为“c0000040”、“c1000040”的退出link必须在最右侧，其左侧相邻的箭头图首位为“e”，最左侧箭头图首位为“d”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11061', '分歧出口编号检查', '线线分歧中，特殊连续分歧不能有出口编号', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11041', '分歧名称错误', '检查对象：相同进入线、退出线、相同类型的多个高速分歧名称拼音。
检查原则：分歧名称拼音不能重复。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11105', '线线关系检查', '一组类别为普通或特殊连续分歧的线线分歧其进入link的终点不能为图廓点，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11067', '分歧名称格式检查', '检查对象：线线分歧信息中，名称中的“英文”字段值
检查原则：如果全为一个相同的英文字母（不区分大小写），报err。
举例说明：AAAA、bbbb等等，报err。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12045', '箭头图号码检查', '3D分歧箭头图branch_type类型为3，号码以“e”或者“c”开头，否则报log1；3D分歧箭头图branch_type类型为4，号码以“d”、“e”或者“c”开头，否则报log1
同一进入link的3D分歧只能有一条主退路（模式图代码不同则不检查），否则报log2；
同一进入link的3D分歧若有多条辅退路，它们的箭头图编号最后一位应当连续，且不能重复，否则报log3。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11062', '分歧出口编号检查', '线线分歧中，普通不能有出口编号', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11049', '名称长度检查', '出口编号不能超过8个字符', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11103', '线线关系检查', '分歧类别为普通或特殊连续分歧时，一组线线分歧不能完全包含在另一组分歧中（即分歧A的全部link都在分歧B中存在），否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11106', '分歧名称错误', '对于进入线相同，退出线不同的高速方面分歧（进入线或退出线是高速或城高种别的线点线方面分歧），这些高速分歧的第一名称均不相同，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11114', '分歧名称检查', 'RD_BRANCH_NAME中的分歧名称NAME字段不能为空，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11069', '分歧名称格式检查', '一组方面分歧，其英文名称中间含有连续空格，报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05098', '3D专用模式图检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08054', '连接路属性交限检查', '提左连接路在直行/右转方向上应当制作禁止通行交限；
提左连接路在左转方向上不应当制作禁止通行交限', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26014', '高速上制作复合路口', '复合路口所挂接的Link不能全是高速或城高', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26010', '交叉点内link与上下线分离检查', '当路口为2个点时，若交叉点内link进入link个数<=1则交叉点内link不可赋上下线分离', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19021', '车信进入线退出线检查', '检查对象：线线车信的退出link
检查原则：线线车信退出线沿车信方向的起始点只挂接了两条link（包括退出线本身）的报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19007', '车信进入线车道数检查', '车道信息的inLink车道数不能超过16条，会影响到gdf的转出结果；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19032', '车信进入退出一致性检查', '车信进入线记录的车道信息应与退出线上记录的全部通达方向完全匹配（包括附加车道、公交专用道所在的车道位置），否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19034', '经过线距离检查', '车信经过线应小于等于10条，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26032', '路口名称错误', '路口名称的长度不能超过35位，拼音长度不能超过240位，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19014', '逻辑关系错误', '路口车信上的进入线和退出线不能是交叉点内link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19003', '通往退出道路的车道数为0', '车道信息中，必须有到退出Link的车道信息', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20014', '立交形状点错误', 'RD_GSC_LINK表中的SEQ_NUM必须小于该link的形状点数', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20016', '无Z-level错误', '当两条Link（道路与CityModel)立交时，必须制作做立交关系，否则报log1；
当两条Link（道路与CityModel)不能全部或者部分重叠，否则报log2；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20005', '高度层次错误', '高度关系参见附表', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26999', '名称来源检查', '路口名称来源为“翻译”时，语言代码一定为ENG，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26041', '语言代码检查', '语言代码中:
1.大陆 语言代码必须有CHI，且只能为CHI和ENG，否则报错
2.语言代码为"ENG"时,名称发音 字段应为空', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26004', '顺行错误', '高速上做了顺行的路口，并且该路口还做了分歧信息，如果分流处顺行的退出link中有属性为IC/JCT/SA/PA/R（匝道）的话，则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19009', '车信车道数检查', '同一进入线车信的各退出线信息（包含线线车信及路口车信）中“进入车道信息”的车道数必须相同，否则报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19024', '车信结构检查', '车信的进入线和退出线不能相同', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19002', '车道方向有误(非1,4,7,a)', '车道方向应为“a、b、c、d、e、f、g、h、i、j、k、l、m、n、o”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19028', '车信包含检查', '进入线相同的两组车信（包括线点线车信与线线车信，同时考虑跨图幅情况），如果其中一组的进入线+所有经过线+退出线完全包含另一组的进入线+所有经过线+退出线，报log（每有1条被包含的车信，报1条log）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13035', '内容检查', 'SC_TOLLGATE_NAMECK中NAME_CHI与RD_TOLLGATE_NAME的中文收费站名称相同，但关联的收费站英文名与SC_TOLLGATE_NAMECK中NAME_ENG不同时，则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20008', '高度层次错误', 'Node处有Zlevel信息时，则该Node的接续Link的Zlevel值应该一致，否则报Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26017', '关系信息登记点检查', '关系类型为“路口”的车信、交限、顺行、路口语音引导关系信息，应制作到登记了路口的点上。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20004', '高度层次错误', '一组立交关系的所有Link不能挂接在同一端点上，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26008', '交叉点内link与上下线分离检查', '交叉点内link为双方向的不可以赋上下线分离', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26002', '顺行错误', '某路口点的进入Link和对应的退出link之间的夹角小于120度或者大于240度，那么它们之间不允许做顺行信息。角度的判断：查找第一个形状点(起始点)。如果第一个形状点(起始点)与第二个形状点距离过近（20m），则再接着找下一个，详见附件
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26012', '交叉点内link与上下线分离检查', '当路口为4个点时，若交叉内的link所挂接的进入link、退出link为上下线分离的且角度为180度的，那么该交叉口内link应被赋为上下分离', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19025', '车信结构检查', '车道数与可通行车道一致性检查。比如LANE_NUM是2，则IN_LANE_INFO的第15、14位至少有一位为1，其余位必须为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19001', '错误的车信进入线或退出线', '公交车专用道不能作为路口车信的进入线、退出线，否则报err；
公交车专用道不能作为线线车信的进入线、经过线、退出线，否则报err；
非引导道路种别不能作为路口车信的进入Link、退出Link，否则报err；
非引导道路种别不能作为线线车信的进入Link、经过线、退出Link，否则报err；
线线经过线是交叉口内link的情况，不进行检查。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20001', '高度层次错误', '在一组立交关系中，Link间的高度层次从低到高必须相差1。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20002', '高度层次错误', '包含两根或两根以上Link的立交关系中，高度层次值必须从0开始。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19019', '车信进入退出线属性检查', '线线结构中车信的进入线不能为环岛或特殊交通类型，退出线不能是交叉口内link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19026', '车信结构检查', 'BUS_LANE_INFO为“1”的bit位，在IN_LANE_INFO与之相同的bit位一定为“1”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26006', '交叉点内link与上下线分离检查', '对于奇数个点组成的路口，一条单方向的交叉点内link，如果挂接了上下线分离的道路，并且该上下线分离道路与交叉点内link夹角大于120度并且方向贯通，则该交叉口内link应当有上下线分离属性.(程序中的实现为只要两个端点有一个端点满足贯穿条件，那么该交叉口内link就需要赋上下线分离)', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26003', '多余顺行检查', '一个路口上，相同的进入线和进入点，若存在顺行信息的组数大于等于2，报err。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26034', '路口名称错误', '拼音中只允许有半角字符，包括英文字母、隔音符“''”、阿拉伯数字，首字母大写其余小写，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13016', '收费站名称检查', '收费站的名称不能超过35个汉字，拼音不能超过206个字符（去掉空格）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26039', '重复路口', '两个路口不能共用一个node点，否则报log。log包括所有含有该node路口的cross号码。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26031', '路口名称错误', '路口名称的拼音必须是与其汉字的拼音匹配，如果汉字是多音字，那么路口名称的拼音必须是其所有多音字中的一个，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13001', '关系型收费站检查', '收费站的主点必须是路上点（收费站）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13025', '语言代码检查', '语言代码中:
1.大陆 语言代码必须有CHI，且只能为CHI和ENG，否则报错
4.语言代码为"ENG"时,名称发音 字段应为空', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13024', '关系型收费站检查', '双方向通行的道路上制作了单向关系型收费站，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19004', '车道数检查', '车道信息中，inlink车道数不应为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19005', '车道数检查', '一组车道信息的左右附加车道数总和小于车信的进入车道数，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19006', '经过线错误', '详见检查规则库附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19013', '逻辑关系错误', '路口车信上的进入线不能是环岛或者是特殊交通类型', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19023', '单路口车信检查', '只挂接两条Link的路口（单路口）不应该制作车信，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26009', '交叉点内link与上下线分离检查', '当路口为2个点时，若路口的接续link有非上下线分离link，则交叉点内link不可以赋上下线分离', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26013', '交叉点内link与上下线分离检查', '当路口为多于4个点的路口时，交叉点内的link所挂接的进入、退出link为上下线分离且角度为180度的，那么该交叉口内link应被赋为上下分离', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26044', '路口内link属性检查', '路口内Link表中记录的道路必须具有交叉口内link属性，且一定不能具有环岛属性，特殊交通必须为“否”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26011', '交叉点内link与上下线分离检查', '当路口为3个点时，若交叉点内link的进入与退出线为上下线分离则此交叉点内link应被赋上下线分离', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26007', '交叉点内link与上下线分离检查', '对于偶数个点组成的路口，一条单方向的交叉点内link，如果挂接了上下线分离的道路，并且该上下线分离道路与交叉点内link夹角大于165度并且方向贯通，则该交叉口内link应当有上下线分离属性.(程序中的实现为只要两个端点有一个端点满足贯穿条件，那么该交叉口内link就需要赋上下线分离)', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03002', '多余Node点检查', '前提条件：点只挂接2条link，且这两条link上的属性完全一致（11夏新增供用信息维修属性），没有CRF信息，不是关系信息主点，不是路口，不是铁路道口，不是点障碍物，不是危险信息结点，不是闭合环起打断作用的点
1.非闭合环上的平面交叉点（无属性），认为该点多余，报Log1；
2.路上点（CRFInfo），认为该点多余，报Log2；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05031', '分歧名称检查', '分歧类型为“0”、“3”或“4”时必须无分歧名称，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05026', '分歧详细信息检查', '1.分歧类型为“3”或“4”的名称种别只能为“9”，否则报log；
2.分歧类型为“0”、“1”、“2”的名称种别不能为“9”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05036', '分歧名称检查', '编号类型字段值不能为“1”、“2”、“3”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05029', '分歧详细信息检查', '分歧类型为“3”或“4”的箭头图标志只能为“0”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05025', '分歧详细信息检查', '1.分歧类型为“3”或“4”的设施类型只能为“9”，否则报log；
2.分歧类型为“0”、“1”、“2”的设施类型不能为“9”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03037', '路上点错误', '挂接的两条link的TMC匹配信息_匹配方向与道路画线方向关系信息应一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03072', '异常图廓点', '点属性为“图廓点”时，种别必须是“平面交叉点”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03007', '路上点错误', '挂接的两条link的通行方向应一致，“未调查”和“双方向”认为一致,单方向和双方向认为一致；注意：点的属性为“路上点（收费站）”时，当其挂接的Link只为2时，如果一个为单向，一个为双向，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05037', '分歧挂接Link数检查', '检查对象：分歧、方向看板、实景看板、实景图、连续分歧
检查原则：
1.检查对象进入线的终点必须至少挂接3条Link，否则报log；
2.检查对象退出线的起点必须至少挂接3条Link，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03004', '路上点错误', '种别为“路上点”的点挂接Link数应等于2；注意：点的属性为“路上点（收费站）”时，明确说明是收费站的错误；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03055', '障碍物检查', '具有障碍物属性的点所挂接的link上具有步行街属性，报err；（挂接的多条link具有步行街属性，只报一次）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03044', '盲端', '接续link数为1的非图廓点，以该点为中心,半径为10m的范围内有其它点（孤立点除外）或线，则提取该点。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03001', '点挂接多link', '道路Node的接续link数必须小于等于7', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05050', '关系类型检查', '分歧（所有分歧类型）的进入和退出link如果都挂接在同一路口或同一Node上，那么关系类型为“路口关系”，反之应为“线线关系”，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05051', '母库元数据名称号码唯一性检查', '母库RD_BRANCH_NAME的NAME_ID不能与元数据SC_BRANCH_ENGNAME的NAME_ID相同，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03073', '异常图廓点', '点的种别属性“平面交叉点（图廓点）”不能与其他点属性共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03033', '路上点错误', '接边的两条link道路名信息的主从CODE应一致（NIDB－K：只检查高速和城市高速）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05020', '分歧点箭头图标识检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03058', '障碍物检查', '检查对象：障碍物属性Node点
检查原则：该Node上若挂接了10级路，报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03999', '名称来源检查', '点名称来源为“翻译”时，语言代码一定为ENG，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03056', '障碍物检查', '具有障碍物属性的点只能挂接2条link，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03028', '路上点错误', '接边的两条link道路名信息的名称分类应一致（NIDB－K：只检查高速和城市高速）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03054', '障碍物检查', '具有障碍物属性的点在路口上，报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03059', '障碍物检查', '检查对象：障碍物属性Node点上，仅挂接有2根link。
检查原则：该Node上挂接的2根link不能同时为高速或城高，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03013', '路上点错误', '挂接的两条link的功能等级应一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03048', '异常点JCT', '点属性为“JCT”时，种别必须是“属性变化点”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01185', '上下线分离之间距离检查', '高速和城高上下线分离之间的距离不能小于10米', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01029', '10级路错误', '10级路上制作了“永久禁止穿行（有车辆类型限制）”或“条件禁止穿行（有时间段或类型）”信息，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08010', '交限与交限重叠', '一组线线交限（普通时间段交限）的所有link完全包含在另一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01093', 'FC错误', '10级路的FC必须为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03012', '路上点错误', '挂接的两条link的特殊交通类型信息应一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03003', '路上点错误', '种别属性为“路上点（收费站）”时，报err。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03046', '铁路道口点挂接检查', '道路中“铁路道口点”属性的点，与铁路做了同一Node关系的点上挂接link数必须等于2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08040', '交限类型检查', '交限的限制类型为“时间段禁止”时，交通限制信息中的时间段不能为空。否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08007', '交限时间段检查', '时间段重复或者重叠', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08008', '交限时间段检查', '将一个CNavLLRinfo的所有时间段交限按照NaviEx规则转出的字符串长度不能超过254', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03080', 'BUA打断速度限制', '一个点上仅挂接两条道路并且道路等级相同，至少一条Link的速度来源是未调查，若两条道路的城市内道路标识和速度限制值都不相同，报log。
特殊说明：
判断速度限制值（限速类型相同）是否相同时考虑方向，即判断两条Link同侧的速度限制值是否相同', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03068', '异常图廓点', '点如果为图廓点,那么在Node图幅表中应该同时有两条记录', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04002', '大门挂接/方向错误', '大门点的挂接link数必须是2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03096', 'node与省界相交', 'node挂接的所有link（屏蔽：memo字段为“15春矢量融合”和“县乡道矢量入库”且开发状态为“未验证”的link。）的行政区划前两位相同，且该点在省界（KIND：1）上，则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01277', '速度等级变化点错误', '一个点只连接了两条Link，如果两条link（任意一根link不能存在交叉口link属性）中没有IC、JCT、桥、区域内道路、步行街、普通匝道属性的Link，且限速类型为“普通”时，限速等级值之差不允许大于等于3', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08012', '交限与交限重叠', '一组线线交限（普通非时间段交限）的所有link完全包含在另一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03069', '异常图廓点', '图廓点只能挂接两条不同图幅的Link，即只能有两条Link以该图廓点为起终点，且Link属于不同的图幅', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03047', '特殊种别属性', ' NODE点种别和形态的匹配', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03082', '特殊种别属性', 'Node具有“门牌号码点”形态时，种别必须为“路上点”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04009', '大门交限检查', '点障碍物不能与大门共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04008', '大门交限检查', '双向道路上制作了一个方向的大门，但另一个方向没有禁止通行（永久）的普通交限，报log1
双方向大门的进入link或者退出link为单方向，报出log2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01390', '停车场检查', '检查对象：具有“停车场出入口连接路”属性的双方向（道路方向为未调查或双方向）道路
检查原则：检查对象应具有“POI连接路”属性，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01027', 'Link形状点数>=490', 'Link的形状点数应小于490', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01405', '物理断头路检查', '如果一个点只挂接了一条非未供用的link，则该link不能为单方向
屏蔽：高速种别有施工属性的link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08017', '主路有交限', '详见检查规则库附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01307', '速度限制来源一致性检查', '单向道路的限速类型为“普通”时，限速来源不能是“方向限速”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01242', '速度限制错误', '限速类型为“普通”时，双方向道路的速度限制必须<=100km/h,否则报log
屏蔽：如果道路含有JCT且有调头口，则不报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03009', '路上点错误', '挂接的两条link的收费信息应一致，“未调查”与“免费”认为一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01098', 'FC1-4的开发状态检查', '当link的开发状态为“未验证”时，其FC只能是5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01176', '高速与普通相连', 'IC以外的高速或城高不能挂接普通道路（步行道路种别不检查）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01275', '主辅路连接路检查', '
详见附件
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01109', '总车道数和左右车道数约束检查', 'Link上的总道数和左右车道数不能同时有值', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01186', '上下线分离之间距离检查', '除高速和城高等级外，其他等级的上下线分离间的距离不能小于6米，交叉口内Link不查。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03039', 'SA\PA与高速本线的夹角过大', '检查对象：一个Node上，共挂接3根高速/城市高速的link，其中2根为本线，另外一根含SA或PA属性，且为单方向link。
检查原则：含SA或PA属性的link，方向若为退出该Node点，则与方向为退出该Node点的本线的夹角小于等于30°；方向若为进入该Node点，则与方向为进入该Node点的本线的夹角小于等于30°。不满足上诉任意一个条件，报err
说明：以挂接在Node上的第一个形状点定位角度。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03062', '异常桥属性点', '一个点上挂接的link有且只有一条桥属性的link，那么该点应为平面交叉点（桥）（图廓点除外）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05024', '分歧详细信息检查', '1.分歧类型为“3”或“4”的声音方向只能为“9”，否则报log；
2.分歧类型为“0”、“1”、“2”的声音方向不能为“9”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04003', '大门挂接/方向错误', '检查对象：EG类型（紧急车辆能够进入）的大门的进入Link和退出link。
检查原则：该link上的“永久车辆限制”信息中：“禁止”中包含“急救车”，或“允许”中不包含“急救车”的报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04001', '大门前道路检查', '检查对象：大门退出link为8级道路，大门进入link为7级及以上道路。沿大门进入link逆方向同一等级的道路查找，直到找到端点挂接link等级与大门进入link不同，或端点挂接3根及以上link为止，查找到的该组link，即为大门前道路；
检查原则：若检查对象长度小于50米，分两种情况报err，详见“LOGMSG”。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04007', '大门交限检查', '大门类型为紧急车辆进入，不允许制作时间段信息', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04005', '大门交限检查', '大门的进入线到退出线不能有禁止交限（货车交限和时间段交限允许）和顺行', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04004', '大门挂接/方向错误', '一组相同的线点线信息，不允许制作两组及以上大门', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03065', '异常隧道属性点', '挂接隧道link数不能大于2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03067', '异常隧道属性点', '挂接隧道link数等于2时，如果还挂接了非隧道属性的link，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03045', '孤立点', '道路点的接续link数必须大于0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05021', '分歧点箭头图标识检查', '检查原则：
检查对象：线线特殊连续分歧（同一进入link不同退出link的一组线线特殊连续分歧）
1.同一进入link不同退出link的一组线线特殊连续分歧，则箭头图标识都为“无”，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01378', '提右路属性错误', '检查原则：如果一个node上挂接两条具有提右形态link，且该node总共挂接至少3条link（辅路与路九级及九级以下道路除外），报出log
屏蔽对象：若Node为3D分歧的进入Node，不查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01081', '点-B-FC断头点', '判断点的接续Link（检查功能等级FC1-4的道路）中最高功能等级的是否只有一条，如果是那么说明此点是一个FC断头点', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05030', '分歧详细信息检查', '1、分歧的向导代码只能为“0”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03041', '挂接检查', '点挂接了五根link（点挂接link中10级路除外）,两根是上下分离属性的link,一根是交叉口内link,两根是非MD属性的link

屏蔽对象：
点挂接的上下线分离道路如果近似180度（左右偏差20度）不查。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01183', '上下线分离之间LINK未登记复合路口', '一条link的两个端点，如果各挂接了2条及以上的MD属性link（非高速，非城市高速），所挂接的同一组MD属性link的 NAME_GROUPID有一组相同时，并且该link本身不是交叉口内link，报log；如果该link的两个端点是两个不同的路口，那么屏蔽log；另屏蔽所有的提右/提左/高速入口模式图退出线；link长度如果超过50米，则不做该检查；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03038', '死点', '点的所有接续link，排除掉link上限制信息为施工中不开放及未供用后，剩余的接续link数目＞0，则通行方向不应都是出去或者进来的方向。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03030', '路上点错误', '接边的两条link道路名信息的名称来源应一致，名称来源为“点门牌”的道路名应不检查（NIDB－K：只检查高速和城市高速）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03083', '特殊种别属性', '特殊种别属性', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01276', '速度等级变化点错误', '一个只连接两条Link的点，连接的两条Link限速等级不同且限速类型为“普通”时，如果其中一条Link长度在100m以内，那么它的另一端不应该有且仅挂接了一条速度等级且限速类型为“普通”时与其不同的Link（10级路、航线除外）。
增加屏蔽条件：
1.当一个NODE点仅连接两条Link，连接的两条link其中任意一条道路等级为10级路（RD_LINK.KIND = 10）时，不报log；
2.当一个NODE点仅连接两条Link，连接的两条link其中任意一条道路属性为步行街时，不报log；
3.当一个NODE点仅连接两条Link，连接的两条link其中任意一条为航线时，不报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03066', '异常隧道属性点', '如果点是隧道属性点，那么挂接的隧道属性link不能等于0；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03064', '异常隧道属性点', '除图廓点以外,如果点只挂接了一条“隧道”属性的link，那么该点必须“平面交叉点（隧道）”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05048', '分歧类型与模式图匹配检查', '检查对象：模式图代码非空的分歧信息
检查原则：
若模式图代码在SC_MODEL_MATCH_G表的FILE_NAME字段存在，则多媒体文件中对应的TYPE应与分歧类别一致，其中：
1、BRANCH_TYPE为0、1、2，对应SC_MODEL_MATCH_G表的B_TYPE字段值为2D,M_TYPE字段值为pattern
2、BRANCH_TYPE为3，对应SC_MODEL_MATCH_G表的B_TYPE字段值为3D,M_TYPE字段值为pattern
3、BRANCH_TYPE为4，对应SC_MODEL_MATCH_G表的B_TYPE字段值为CRPG,M_TYPE字段值为pattern
否则，报log.', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08002', '多组时间段交限', '路口交限中,排除货车交限后，同一进入退出线存在多组时间段信息,程序报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08035', '交限详细信息', '交限标志不能为未验证，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08034', '交限详细信息', '
交限详细信息表的限制信息中出现值域为“0”的数据报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08039', '交限类型检查', '交限的限制类型不能为“未调查”否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01265', '速度等级错误', '限速类型为“普通”时，有线门牌的LINK上的速度等级不能为1', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01295', '线线结构构成link检查', '一组线线的所有link中不能有步行街属性的link
一组线线的所有link中不能有人渡、轮渡种别的link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01392', '停车场检查', '检查对象：一个Node挂接有n条Link，其中一条具有“停车场出入口连接路”属性且通行方向为退出该Node，同时其他n-1条Link均为8级路、具有“POI连接路”属性、制作有穿行限制
检查原则：
1.具有“停车场出入口连接路”属性的Link应不具有“POI连接路”属性，且制作有穿行限制，否则报log1；
2.其他n-1条Link一定不能具有“停车场出入口连接路”属性，否则报log2；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01091', 'FC错误', '大门的进入和退出link的FC必须为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01094', 'FC错误', 'SA/PA属性的link，若不含IC/JCT,FC应为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01096', 'FC错误', 'POI连接路形态的道路FC必须为5', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05028', '分歧详细信息检查', '1、分歧类型为“3”的模式图代码只能以8或5开头，否则报log；
2、 分歧类型为“4”的模式图代码只能以7开头，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05042', '语言代码检查', '分歧名称的语言代码中:
1.大陆 语言代码必须有CHI，且只能为CHI和ENG，否则报错
3.语言代码为"ENG"时,名称发音 字段应为空', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05023', '2D3D虚拟替换删除后图形匹配检查', '检查对象：分歧的箭头图代码。

检查原则：
1.分歧的箭头图代码应在AU_MULTIMEDIA表的NAME字段中存在（若箭头图代码需要替换，则替换后的代码应在多媒体文件表AU_MULTIMEDIA的NAME字段中存在），否则报log

判断箭头图代码是否需要替换：
若箭头图代码的后7位在配置表SC_MODEL_REPDEL_G的“CONV_BEFORE”字段的后7位中存在，则需要进行替换。
1. 若对应的“CONV_OUT”有值，则将箭头图代码的后7位替换为“CONV_OUT”的后7位再在多媒体文件表AU_MULTIMEDIA的NAME字段进行查找。
2. 若对应的“CONV_OUT”无值，不查。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05027', '分歧详细信息检查', '分歧类型为“3”或“4”的出口编号必须为空，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05054', '拼音相同名称不同检查', '同一图幅内的分歧名称和分歧名称，若存在拼音相同名称不同的情况，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01403', '车道数值域检查', '总车道数值域为[0,15]，左车道数、右车道数值域为[0,14]，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08032', '连接路属性交限检查', '检查对象：
1.道路连接路属性包含有调头口的单Link，如果单Link的端点仅挂接一条包含有调头口的Link（不包含本身）时，作为一个Link组来进行判断（考虑跨图幅）

调头口判断条件：
1.检查对象每一端挂接的Link数（不包含本身）必须等于2；
2.检查对象每一端挂接的两条link，要求必须为单向，且一条进入、一条退出，进入退出是针对于端点而言；

检查原则：
1.若检查对象为单向通行，则在可通行方向上不应存在路口交限(时间段交限除外)；
2.若检查对象为双向通行，则不应在两个方向上都存在路口交限(时间段交限除外)；

特殊说明：
1.若道路连接路属性包含有调头口的单Link，端点挂接有大于1条包含有调头口的Link（不包含本身）直接报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08011', '交限与交限重叠', '一组线线交限（普通时间段交限）的所有link完全包含在另一组线线交限（普通非时间段交限）的所有link中，报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08037', '交限详细信息', '交限详细信息的KG标志不能为“1”、“2”，否则报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04006', '大门交限检查', '大门的进入线或退出线有一根为10级路或者都为10级路，则大门的“通行对象”只能为“行人”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05044', '分歧名称检查', '连续分歧不能有名称，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05035', '分歧名称检查', '名称来源字段值只能为“0”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01030', '10级路错误', '10级路不能挂接全封闭道路', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01340', '停车场出入口检查', '停车场出入口Link不能和高速等级道路直接挂接', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01356', '道路连接路检查', '检查对象：调头口3D（80000001,80000003）的退出线
检查原则：检查对象的道路连接路属性不能为“调头口”，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11118', '名称长度检查', 'RD_BRANCH_NAME表中“分歧名称+名称发音”关联SC_BRANCH_ENGNAME表中“分歧名称+名称发音”，找到与元数据记录GROUP_ID相同的分歧英文名称，其长度不能超过35个字符，否则报log；高速第一分歧英文名称长度不能超过30个字符，否则报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05071', '名称语音一致性检查', '检查对象：RD_BRANCH_NAME表中VOICE_FILE  
检查原则：
分歧名称以G、S、Y、X、C、Z开头，后面+“全部数字”或“字母+数字组合”中任意一种形式的分歧名称，名称语音遵守以下规则
1） 分歧名称中的CODE_TYPE(编号类型)为 4国家高速编号或 10省级高速编号时，字母不变，数字转小写拼音
例： Ｇ１０１ 名称语音为： Gyilingyi
2）分歧名称中的CODE_TYPE(编号类型)=“5（国道编号）、6（省道编号）、7（县道编号）、8（乡道编号）或9（专道编号）”时，去掉首字母，其余的字母不变，数字转拼音，末尾为编号类型的拼音。转化后，名称语音的首字母应为大写
例如：Ｇ１０１    名称语音为：Yilingyiguodao
      Ｓ１１７    名称语音为: Yiyiqishengdao
      Ｙ００１    名称语音为: Linglingyixiangdao
      Ｘ００７    名称语音为: Linglingqixiandao
      Ｃ１１６    名称语音为: Yiyiliucundao
      Ｚ５８６    名称语音为: Wubaliuzhuanyongdao
      ＳＬ６６    名称语音为：Lliuliushengdao
否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05077', '分歧名称检查', 'RD_BRANCH_DETAIL表中BRANCH_TYPE为1：方面分歧，RD_BRANCH_NAME中“NAME”不能重复，否则报og
RD_BRANCH_DETAIL表中BRANCH_TYPE为2：IC分歧，RD_BRANCH_NAME中“NAME”不能重复，否则报og', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26049', '主点唯一性检查', '一个路口中（包含单路口及复合路口）必须有且仅有一个主点，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08045', '交限与顺行矛盾检查', '普通交限与顺行有相同的进入link和退出link，程序报log。说明：时间段交限与顺行可以共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01445', '道路种别检查', '道路link种别不能存在15：10级路(障碍物)，否则报Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05070', '名称语音一致性检查', '同一省份，分歧中文名称、名称发音、编号类型均相同，但名称语音不同。将名称语音不在语音列表(SC_VOICE_file)中的，报出log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11116', '名称长度检查', 'LANG_CODE=ENG时，分歧名称不超过35个字符，否则报LOG；第一分歧名称对应的英文名称长度不能超过30个字符，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20024', '高度层次错误', 'Citymodel与水系/绿地立交时，Citymodel在上方，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01450', '速度限制', '限速类型为“普通”时，高速、城高的可通行方向上的速度限制不能为0，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26051', '路口点线正确性检查', '路口中的点应在该路口的线的NODE中存在，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26050', '路口点线正确性检查', '1.路口中的线必须是交叉口内LINK，否则报LOG1
2.路口中的线的两个NODE应属于该路口；否则报LOG2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05074', '追加IC名称的分歧检查', '
相同进入线、退出线存在分歧类型为1：方面分歧和2：IC分歧的单分歧时，其声音方向，出口编号，箭头图号码，模式图号码，箭头图标志，向导代码需完全一致，否则报Log
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01449', '上下线分离道路方向检查', '一组CRFR的两条LINK为LA和LB,其结点分别为NA和NB,若NA和NB属于同一组CRFI，则LA进入NA,LB退出NB或者LA退出NA,LB进入NB，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01418', '道路施工检查', '同一根LINK限制信息不能同时存在“施工中不开放”和“道路维修中”，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20033', '立交点坐标', 'RD_LINK_GSC表中LINK的形状点号的坐标应和立交点坐标一致', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20028', '高度层次错误', '道路与水系立交时，道路有隧道属性时，道路在下方，无隧道属性时，道路在上方；否则报log

', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20029', '高度层次错误', '道路与绿地立交时，道路有隧道属性时，道路在下方；道路具有高架属性或者道路名类型为立交桥名（主路）时，道路在上方，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01419', '总车道数和左右车道数约束检查', '左车道数与右车道数相等且不等于0，报出LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26047', '路口中电子眼检查', '路口表（RD_CROSS）中的电子眼（ELECTROEYE）值域只能为未调查（0）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23004', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中SPEED_LIMIT(限速值)
检查原则：  
电子眼限速值必须为整数（千米/时），否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23003', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中VERIFIED_FLAG(验证标识)
检查原则：  
电子眼验证标识只能为1（验证正确），否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23002', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中LOCATION(电子眼方位) 
检查原则：  
电子眼方位只能为“左”、“右”、“上”中的其中一个，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26048', '复杂路口检查', '1、路口点数为2-4个的复合路口，路口点比交叉口link多2个及2个以上时，报错；
2、路口点数大于4个的复合路口，路口点比交叉口link多1个及1个以上时，报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23001', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中KIND(电子眼类型)
检查原则：
电子眼类型只能为“1”（限速摄像头）、“13”（非机动车道摄像头、“15”（公交车道摄像头）、“16”（禁止左/右转摄像头）、“20”（区间测速开始）、“21”（区间测速结束）、18（应急车道摄像头）、22（违章停车摄像头）、23（限行限号摄像头）、10（交通信号灯摄像头）、12（单行线摄像头）、98（其他），否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01457', '交叉点内道路浮岛检查', '路口中任意一条交叉口内LINK与该路口挂接的道路（非交叉口内LINK）道路等级不一致时，程序报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01458', '交叉点内道路浮岛检查', '同一路口挂接的所有道路（交叉口内LINK除外）有施工、穿行限制、车辆限制中的一种或多种信息时，假设这个信息的集合为U，那么任意一条交叉口内LINK的限制类型应是这个集合的子集，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01459', '交叉点内道路浮岛检查', '路口中交叉口内LINK(9级路不检查)都有施工、穿行限制、车辆限制中的一种或多种信息时，假设这个信息的集合为A，那么该路口挂接的其它道路都应包含施工、穿行限制、车辆限制中的一种或多种信息，且该信息的集合B应包含A', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01417', '航线/水逻辑关系错误', '航线与水系必须在航线串的起止点处制作立交关系，并且航线的zlevel为1，否则报log
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60297', 'POI引导坐标显示坐标距离检查（5000-10000）', '检查条件：
(1) IX_POI表中“STATE(状态)”为非1（删除）；
检查原则：
POI的引导坐标与显示坐标距离大于等于5000米并且小于等于10000米，报出Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60298', 'POI引导坐标显示坐标距离检查（10001-15000）', '检查条件：
(1) IX_POI表中“STATE(状态)”为非1（删除）；
检查原则：
POI的引导坐标与显示坐标距离大于等于10001米并且小于等于15000米，报出Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM56035', '空的限速关系', '当“内业作业状态”≠2时，做如下检查：
限速值不能为0，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01437', '空的限速关系', '限速类型不为“普通”时，同一根LINK的顺向限速和逆向限速值不能同时为0，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60299', 'POI引导坐标显示坐标距离检查（15001以上）', '检查条件：
(1) IX_POI表中“STATE(状态)”为非1（删除）；
检查原则：
POI的引导坐标与显示坐标距离大于等于15001米，报出Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01004', '航线/水逻辑关系错误', '对象：航线link组（包括轮渡、人渡，多根航线Link组成的Link串），同时满足以下特征：
1）起、终点：同时挂接了有航线种别的link和没有航线种别的link
2）中间点：挂接且仅挂接了两条有航线属性的link
原则：
1）航线link串只能与水系（link上存在水系的种别）有2个交点，否则报log1，提示信息为：“航线与水系边线多次相交！”
说明：如果航线起/终点挂接的航线link被图廓点打断，需要把图廓点挂接的两条航线当做一条
2）航线link串不能与绿地、铁路相交，否则报log2，提示信息为：“航线与非水系相交！”
说明：考虑跨图幅', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08006', '交限车辆类型检查', '线线结构所有的交限都不允许有车辆类型，否则报log1。
车辆类型为“运输卡车”时，屏蔽该log；
当车辆类型为“运输卡车”时，交限标志必须为“实地交限”，否则报log2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08046', '交限车辆类型检查', '交限中的车辆类型为0时，车辆限重必须也为0，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01505', '交叉口内link速度限制值检查', '检查对象：交叉口内link
检查对象的限速值应与检查对象所在的复合路口挂接其挂接的所有link（道路属性为交叉口link除外）的限速值中任意一个限速值相同，如果交叉口内link含掉头口属性且是点限速关联link的除外，否则报log。
（交叉口内link：考虑顺逆限速，两个方向都查；路口挂接link：不考虑顺逆限速来源）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13032', '收费站英文一致性检查', '收费站中，中文一致保证英文一致。否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13033', '关系型收费站', '如果收费通道总数不为0或ETC图标代码不为空，则收费站类型不能为"未调查"', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26033', '路口名称错误', '检查对象：RD_CROSS_NAME
检查原则：1.相同的NAME_GROUPID，CHI和ENG要同时存在，否则报Log；
2.所有名称内容不能为空，否则报Log；
3.当lang_code为“CHI”或“CHT”时,名称拼音内容不能为空，否则报Log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12040', '3D处顺行检查', '检查对象：制作了专用模式图的3D的点
检查原则：
1.若检查对象未登记路口，则报log1； 
2.若检查对象登记了路口，且无SE，则3D进入线到该点挂接的能够退出该点的线上（3D退出线除外、交叉口Link除外）应制作顺行或者路口语音设为直行，否则报log2。
增加屏蔽条件：
1、提左（80000800、80000802）、提左加掉头（80000801、80000803）、调头（80000001、80000003）的，不查原则2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01470', '10级路错误', '10级路的行人步行属性应为“允许”，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08042', '关系类型检查', '交限的进入和退出link如果都挂接在同一路口，那么关系类型为“路口关系”，反之应为“线线关系”，否则报错
说明：交限LINK跨过两个及以上图幅，应制作“线线关系”', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08005', '交限车辆类型检查', '路口所有的交限都不允许有车辆类型，否则报log1。
车辆类型为“运输卡车”时，屏蔽该log；
当车辆类型为“运输卡车”时，交限标志必须为“实地交限”，否则报log2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01522', '速度等级错误', '检查对象：含“未定义交通区域”形态的link；
检查原则：限速类型为“普通”时，该link上的限速等级不能为1、2、3、4、8，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09018', '警示信息错误检查', '同一警示信息中有效距离和预告距离不能同时非0，否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07005', '交通信号灯检查', '如果一个路口的接续link的等级都为高速或城市高速，道路形态不含IC，且制作了红绿灯，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01520', '上下线分离属性丢失', '单方向无匝道属性且限速等级为1，或2的link无上下线分离属性，则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05022', '分歧点箭头图标识检查', '检查原则：
1.箭头图标识为“无”，否则报log
区分普通方面、高速方面的方法是：判断进出线、退出线都是高速的为高速方面，其余均为普通方面', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19033', '关系类型检查', '1.车信的进入和退出link如果都挂接在同一路口或同一个点，车信的进入线不是交叉口内道路，那么关系类型为“路口关系”，否则报错；
屏蔽：车信的经过点中有图廓点的情况
2.满足以下任一条件时，车信的关系类型必须是线线关系，否则报错；
1）车信的进入和退出link如果未挂接在同一路口或同一个点上；
2）车信的进入线是交叉点内道路；
3）车信的LINK跨过两个及以上图幅', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01521', '速度等级错误', '检查对象：含“IMI”属性的link；
检查原则：限速类型为“普通”时，
(1)该link若为II属性，则其限速等级不能为1，否则报err
(2)该link若为M属性，则其限速等级不能为1、2、8，否则报err
(3)该link若为I属性，则其限速等级不能为1、2、3、8，否则报err', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09017', '警示信息错误检查', '警示信息标牌类型“会车让行”不允许制作在单方向的link上，否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60363', '服务区POI或其子关联LINK属性检查', '检查对象：IX_POI表中种别为服务区（230206）的POI
涉及到取父POI编号和子POI编号分别从IX_POI_PARENT和IX_POI_CHILDREN表中获得
检查原则：
若种别为服务区（230206）的POI或其子的引导Link同时具有停车区（0C）和服务区（0B）属性，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03087', '障碍物检查', '障碍物点的挂接link如果满足以下条件之一，报log
（1）公交车专用道
（2）步行街
（3）时间段步行街（加了时间段禁止穿行，并且车辆类型限制同时且只制作了步行者、配送卡车、急救车的link）不能作为永久交限的进入、退出线
（4）出租车专用道（加了永久禁止穿行，并且车辆类型同时且只制作了允许出租车、急救车和行人的link）
（5）卡车专用道（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许配送卡车、运输卡车、急救车和行人的link）
（6）进不去出不来的link（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许步行者、急救车的link）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01512', 'POI与大门逻辑关系检查', '检查对象：
IX_POI表中“STATE(状态)”字段为“3（新增）”或“2（修改）”，且LOG字段包含“改RELATION”
检查原则：
1、以道路大门点做50m半径，搜索后以“门”或“门）”结尾的poi；
1）中POI的父POI（分类为180105和120101的，不报log）；
2）中POI的二级父POI（分类为180105和120101的，不报log）；
2、符合检查规则的poi，如果关联link挂接了比自身等级高的道路，不报LOG。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01516', '线-B-FC断头点', '检查对象：检查的路口为单路口或单点，而不是复合路口
检查原则
1、如果路口的接续link只有1不查该路口
2、如果该路口中所有接续link的FC值为5，则不做检查；
3、确定该路口中所有接续link的FC最大值（FC1为最大）；
4、遍历路口的接续link，分别找出FC为最大的进入线所有交限（卡车交限排除）的退出线和FC为最大的退出线所有交限（卡车交限排除）的进入线，如果进入线大于等于1条并且有退出线，且都为禁止通行或者退出线大于等于1条并且有进入线，且都为禁止通行，报log;
如果最大FC的进入线和最大FC退出线属于同一CRFR，也报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05093', '国家编号类分歧名称的长度检查', '检查对象：
对RD_BRANCH_NAME表中的CODE_TYPE字段的值域为4-10的中文名称和英文名称进行检查。（4 国家高速编号、5 国道编号、6 省道编号、7 县道编号、8 乡道编号、9 专用道编号、10 省级高速编号)
检查原则：
为上述编号类型的分歧名称，中文名称和英文名称长度必须小于或等于20个字符（1个汉字=2个字符进行计算）；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05094', '出口编号退出link属性检查', '分歧中若存在出口编号且其退出link包含有上下线分离属性时，则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09019', '警示信息制作范围检查', '检查原则：警示信息不能够制作在9级辅路上。否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01526', '点挂接link方向检查', '检查对象：Node点，其上挂接4条或以上上下线分离属性link。
检查原则：Node点上挂接link的方向，若进入该node的link大于等于3条，且都为上下线分离属性，则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08049', '交限与交叉口方向矛盾检查', '路口交限的进入线到退出线无通路可行，即报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01483', '道路连接路检查', '1、检查原则：link1上包含有提左连接路形态，且无M形态，无掉头，无提右，挂接的link2上有M、无提左无提右无掉头，则link1报log1，link2报log2；
2、检查原则：link1上包含有提左连接路形态，且有M形态，无掉头，无提右，如果挂接的link2上有提左，无M无提右无掉头，则link2报log1；如果挂接的link2上有M，无提左无提右无掉头，则link2报log2；
3、检查原则：link上包含有提左连接路形态，无掉头无提右，如果端点挂接的link除本身外全是交叉口内link，且都不包含提左连接路形态，则link1报log3；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01525', '速度等级与code_type值不匹配', '1.link的速度等级为7，且该link上道路名的code_type（国家编号）值为1或7时，报log
2.link的速度等级为8，且该link上道路名的code_type（国家编号）值为1,2,3或7时，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60337', 'POIICON距离检查', '检查对象：制作有POI3DICON属性的POI
检查原则：对所有制作有POIICON属性的POI，判断他们相互之间的距离是否小于20米，若小于20米则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11122', '分歧名称错误检查', '分歧的名称中包含“**服务区”“**停车区”的名称，且分歧的退出线为SA/PA属性、且分歧类型为IC分歧，则该名称必须是第一分歧名称（SEQ_NUM最小），否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01482', '道路连接路检查', '1.路口关系中有提左3D号的退出线上无提左连接路形态的报log；线线关系中有提左3D号的所有经过线上无提左连接路形态的报log。
2.有提右3D号的退出线上无提右连接路形态的报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13034', '收费站共线检查', '检查对象:RD_LINK
检查原则：如果一个收费站的退出link与另一个收费站（主点不同）的进入link为同一link，则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60348', '停车区、服务区的POI或其子关联LINK属性检查', '检查对象：IX_POI表中的全部POI
检查原则：
种别为服务区（230206），停车区（230207）的POI或其子的引导LINK没有服务区属性（0B），也没有停车区属性（0C），报Log
注：以上涉及到取父POI编号和子POI编号分别从IX_POI_PARENT和IX_POI_CHILDREN表中获得', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03091', '形态共存检查', '同一个NODE点不能同时存在桥（12）和隧道（13）两种形态', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03092', '形态共存检查', '同一个NODE点不能同时存在桥（12）和图廓点（2）两种形态', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03093', '形态共存检查', '同一个NODE点不能同时存在图廓点（2）和隧道（13）两种形态', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03094', '形态值域检查', 'NODE点形态不能为：未调查（0）、收费站（4）、无人看守铁路道口(32)、有人看守铁路道口(31)、KDZone 与道路交点(41)、幅宽变化点(20)、种别变化点(21)、分隔带变化点(23)、车站(14),否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20034', '立交关系', '检查对象：RD_GSC\RD_GSC_LINK
检查原则：RW_LINK不能与土地覆盖link、市街图link之间制作立交关系', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03090', '形态共存检查', '门牌号码点（RD_NODE_FORM.FORM_OF_WAY=16）不能和其它形态共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03089', '形态共存检查', 'CRFInfo（RD_NODE_FORM.FORM_OF_WAY=3）不能和其它形态共存', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01544', '道路名重复检查', '同一根link上不同的道路名称中不允许有相同的NAME值', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01543', '道路名空值检查', 'link道路名称中的名称组号码(RD_LINK_NAME.NAME_GROUPID)不能为0', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM15008', '限速类型错误', '当内业作业状态≠2时，如果时间段有值，则限速类型一定为特定条件3且限速条件为10（时间限制）或限速条件为6（学校），否则报log ', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19036', '车信包含检查', '同一组车信（车信号码一致），如果通达方向一致，但车通号码不同，且其中一个车信的进入线、经过线、退出线完全包含另一个车信，则报log
', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13037', '收费站名称错误检查', '关系型收费站必须存在收费站名称，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12003', '退出线挂接辅路与非辅路', '3d分歧编号是80100000的,且进入线和退出线挂接在同一点，outlink退出点连接的link中不应既有辅路,又有非辅路(排除outlink本身)；如果进入线和退出线未挂接在同一点，退出线和退出线进入点挂接的link不应既有辅路,又有非辅路', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26052', '路口类型检查', '当路口只有一个节点时，必须为简单路口。当路口有两个及以上节点时，必须为复合路口', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01551', '道路中心隔离带类型', '单方向道路（RD_LINK.DIRECT为2顺方向或3逆方向），道路中心隔离带类型CENTER_DIVIDER只能为0未调查，否则，报log；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01557', '线限速值检查', '检查对象：RD_LINK_SPEEDLIMIT
检查原则：线限速值顺向限速FROM_LINK_SPEEDLIMIT或逆向限速TO_LINK_SPEEDLIMIT不为5的倍数，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19040', '车信可通行车道检查', '检查对象：RD_LANE_TOPOLOGY 
检查原则：
①同一组车信中，车道信息中包含有左，直左，左直右，调左直，左右，调左，调左右，调左直右，左斜左，左斜右，直左斜左，直左斜右的车道，在所有的左转退出线的可通行车道组合后保持一致，否则报log1；
②同一组车信中，车道信息中包含有右，直右，左直右，调直右，左右，调右，调左右，调左直右，右斜左，右斜右，直右斜左，直右斜右的车道，在所有的右转退出线的可通行车道组合后保持一致，否则报log2；
③同一组车信中，车道信息中包含有直，直左，直右，左直右，调直左，调直右，直调，调左直右，直斜左，直斜右，直左斜左，直左斜右，直右斜左，直右斜右的车道，在所有的直行退出线的可通行车道组合后保持一致，否则报log3；
④同一组车信中，车道信息中包含有调，直调，调直左，调直右，调左，调右，调左右，调左直右，调斜左，调斜右的车道，在所有的调头退出线的可通行车道组合后保持一致，否则报log4；
⑤同一组车信中，车道信息中包含有斜左，直斜左，左斜左，右斜左，调斜左，直左斜左，直右斜左的车道，在所有的斜左退出线的可通行车道组合后保持一致，否则报log5；
⑥同一组车信中，车道信息中包含有斜右，直斜右，左斜右，右斜右，调斜右，直左斜右，直右斜右的车道，在所有的斜右退出线的可通行车道组合后保持一致，否则报log6；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19041', '虚拟车信合理性检查', '检查对象：RD_LANE_CONNEXITY 
检查原则：如果车道信息中所有车道来源均为虚拟车信，则车道信息中不能包含有调头方向；
如果车道信息中包含两个及以上调头方向，两个及以上调头方向中任意一个车道信息来源为虚拟车信，则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12049', '专用模式图检查', '3D分歧的关系类型RELATIONSHIP_TYPE必须为1（路口关系），否则报log
屏蔽：1、提左（80000800、80000802）、提左+调头（80000801、80000803）、调头（80000001）的不查
      2、调头（80000001），进入线、经过线、退出线不在同一图幅中的不查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07007', '信号灯位置检查', '“控制标志”为“不受控制”，则“信号灯位置”应该为未调查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01532', '收费有误', '收费道路如果不包含全封闭属性，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19042', '虚拟车信合理性检查', '如果车道信息中车道来源为虚拟车信，且该虚拟车信为左转车道，则其左侧只能存在调头、左转（来源不区分虚拟或现场，下同）；
如果该虚拟车信为直左车道，则其左侧仅能存在调头，左转，直左；
如果该虚拟车信为直行车道，则其左侧仅能存在调头，左转，直行，直左，其右侧不能存在调头，左转，直左；
如果该虚拟车信为直右车道，则其左侧不能存在右转，其右侧不能存在调头，左转，直左；
如果该虚拟车信为右转车道，则其右侧仅能存在右转；
如果该虚拟车信为调头车道，则其左侧仅能存在有调头', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60364', '服务区POI或其子关联LINK属性检查', '检查对象：IX_POI表中种别为停车区（230207）的POI
涉及到取父POI编号和子POI编号分别从IX_POI_PARENT和IX_POI_CHILDREN表中获得
检查原则：
若种别为停车区（230207）的POI或其子的引导Link同时具有停车区（0C）和服务区（0B）属性，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01534', '形态', '等级为7级以上，属性不包含sa和pa的道路上存在poi连接路时,报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19022', '车信进入线退出线检查', '检查对象：线线车信的进入link
检查原则：线线车信的进入线沿车信方向的终止点只挂接了两条link（包括进入线本身）的报log
增加屏蔽条件：车信的第一根经过线有环岛或特殊交通类型时不报。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01169', '道路连接路挂接检查', '检查对象：
link的form中包含“主辅路出入口”或“调头口”或“提右“或”提左“的link
检查原则：
若检查对象的某个节点挂接有大于等于3条（不包含自身）9级以上，且不包含道路“主辅路出入口”或“调头口”或“提右”或“提左”属性道路，则该节点如果挂接有一条9级以上且不包含“上下线分离”，“辅路”，“提右“，”提左“，”调头“，”主辅路出入口“属性link，报错；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60347', '普通POI引导LINK左右行政区划号检查', '检查对象：IX_POI表中的全部POI
检查原则：如果POI在路上（位置关系为“Link上”），且引导LINK的左右行政区划号不相同，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01178', '高速与普通相连', '检查对象：高速、城高本线出口的点，与其挂接的进入线种别为高速或城市高速、属性不含IC，退出线种别为高速或城市高速，属性含有IC
检查原则：沿着退出线方向，考虑link通行方向和交限的前提下，查找所有通行路径，如果所有路径都不能进入普通道路（非高速或城市高速，非十级路）要报log，即将退出线记录入log图层
有以下例外不需报log；路径断茬，不报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05105', '名称语音一致性检查', '查找分歧的中文名称、名称发音、名称语音，与元数据语音列表(SC_VOICE_file)进行对比，如名称相同，发音相同，但名称语音不同，则报出log
说明：如字母+数字组合的名称，名称相同，但发音能找到多个，则只要SC_VOICE_file中有任意一个发音与数据名称发音相同即可。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23007', '电子眼关联link检查', '电子眼关联link不能是交叉口内link、10级路、人渡、轮渡，否则报log1。
应急车道摄像头只允许制作在高速、城市高速道路上，否则报log2.', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19045', '车信与分岔口提示共存', '以SE的进入线为进入线的车信，如果都是直行（LANE_INFO屏蔽空车道和公交车道），则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23009', '电子眼限速值域检查', '电子眼限速值（SPEED_LIMIT）的值域范围必须为：0或100~1200之间且必须为50的倍数，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20035', '立交点坐标', '道路与道路立交或者道路与铁路立交时，立交点不能在图廓线上，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23011', '电子眼区间测速匹配检查', '检查对象：区间测速电子眼组成表（RD_ELECEYE_PART）
检查原则：同一组号（GROUP_ID）只能对应两个电子眼，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09021', '警示信息错误检查', '检查对象：警示信息表（线点结构）RD_WARNINGINFO
检查原则：相同进入线和进入点的警示信息，若警示信息的“标牌类型（TYPE_CODE）"同时存在“下陡坡（10502）”和“连续下坡标志（10601）”，报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12052', '3d模式图编号规则检查', '详见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01575', '折角检查', 'Link的某个折角偏小
（将检查GLM01001中log为“Link的某个折角偏小”的检查规则单独拿出来作为一个检查项）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01175', '高速上下线分离未赋全封闭', '检查对象：具有上下线分离属性的高速或城市高速
检查原则：
1.link具有IC，但无全封闭属性，两个端点挂接任意一条link具有全封闭属性,报log1。屏蔽条件：当该link一端挂接IC匝道，另一端挂接普通路时，不报log；
2.link无IC/全封闭属性，报log2', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01576', '限速条件检查', '检查对象：制作了限速类型为3（特定条件）的道路
检查原则：限速条件只能是1（雨）、2（雪）、3（雾）、10（时间段）、12（季节时段）、6（学校），否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12054', '提右/提左处理论交限检查', '一：提右/提左路链进入点N1，N1必须登记了路口
二：提右/提左路链退出点N2，N2必须登记了路口（增加屏蔽条件：退出点挂接全部为高速和城市高速等级道路时应屏蔽）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23010', '电子眼区间测速匹配检查', '检查对象：区间测速电子眼组成表（RD_ELECEYE_PART），电子眼表（RD_ELECTRONICEYE）
检查原则：当“区间测速电子眼组成表”中存在记录时，同一组号（GROUP_ID）中记录的两个电子眼号码（ELECEYE_PID）在电子眼表中的电子眼类型（KIND）必须一个为区间测速开始（20），另一个为区间测速结束（21），否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01581', '未知', '检查对象：交叉口内link
检查原则：一个路口包含一组或多组CRFI，其中CRFI中的link数量>2,那么该CRFI中的link种别应一致，否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12053', '提左处理论交限检查', '见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03097', '铁路道口形态检查', '道路与铁路制作了同一node，则道路node点必须为铁路道路，否则报log。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05107', '3D专用模式图检查', '检查对象：制作了调头专用模式图（80000001、80000002、80000003、80000004）；
检查原则：检查对象的进入、退出link不能同时为高速、城市高速、8级路、9级路、10级路，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19043', '虚拟车信缺失检查', '1、如果一个复合路口进入线和两根及以上有效退出线（考虑普通交限）满足6级及以上种别或者为7级且任意一侧车道数大于1时，需要有车信，否则报错；
2、如果一个点不为复合路口点，挂接在该点的一根进入线和两根及以上有效退出线（考虑普通交限）都满足6级及以上种别或者为7级且任意一侧车道数大于1时，需要有车信，否则报错；
排除：如果进入线或者退出link的供用信息为“未供用”（APP_INFO=3）时，不进行对应检查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05104', '分歧名称中0和〇检查', '检查对象：分歧名称
检查原则：
1.汉字数字“〇”名称中包含阿拉伯数字“0”，如二0三北路应为二〇三北路
2.阿拉伯数字“0”名称中包含汉字数字“〇”，如G1〇9应为G109', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01555', '点-B-FC断头点', '判断点的接续Link（检查功能等级FC1-4的道路）中最高功能等级的是否只有一条，如果是那么说明此点是一个FC断头点
屏蔽：供用信息为“未供用”的link', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03095', '死点', '点的所有接续link，排除掉link上限制信息为施工中不开放后，剩余的接续link数目＞0，则通行方向不应都是出去或者进来的方向。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01087', 'FC错误', '检查对象：RD_LINK
检查原则：
1、施工中道路的施工时间为封库后3年以上时，FC必须为5，否则报log1；
2、永久施工中道路的FC必须为5，否则报log2；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01020', '航线检查', '一个Node点，挂接了轮渡，如果挂接的其它道路都为9/10级路，报log1；
一个Node点，挂接了人渡，如果挂接的其它道路都为8/9级路，报LOG2。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01413', '超车限制与警示信息匹配检查', '检查对象: 制作了限制信息类型为超车限制的Link（或Link串）
搜索时需要考虑超车限制Link的限制方向，以下原则中称此为路链
检查原则:
1.路链的起点必须含有超车限制警示信息，但此警示信息的进入线，不能为该路链的起始LINK。
2.路链的终点和终止LINK不能为禁止超车警示信息的进入点和进入线。
3.制作了禁止超车警示信息，并且警示信息的线点关系不在路链构建的线点关系中存在，则报LOG。（排除警示信息的点是路链的起点，线不是该路链的起始线）', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60221', '引导坐标不位于道路上', 'POI的引导坐标与其引导Link的最近距离，应小于等于2米，否则报出LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01001', '折角检查', '详见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01014', '闭合环未打断', '起点和终点的NodeId不能相同', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01262', '速度等级错误', '限速类型为“普通”时，步行道路的速度等级必须为8', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01015', '闭合环未打断', '两条或者多条不同的Link具有相同的两个端点', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01246', '速度限制错误', '检查对象：具有匝道形态的link进行检查；
检查原则：限速类型为“普通”时,检查对象的速度限制值随挂接link中速限值最低的；（如果匝道属性的link为8级以上的道路，随8级以上的挂接除包含步行街属性的link中速限值最低的；匝道属性的link本身是8级的，随8级及以上的挂接除包含步行街属性的link中速限值最低的）
特殊说明：满足以下任一条件的检查对象不查
1.种别为高速或城高的匝道；
2.匝道的速度限制来源为“现场标牌”；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01411', '超车限制检查', '单方向的道路只能制作单方向的禁止超车（Link限制类型为超车限制），并且禁止超车的方向应与Link的通行方向一致，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09015', '异常图廓点检查', '警示信息中的点形态不能是图廓点，否则报错。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26045', '异常图廓点检查', '路口中的点形态不能是图廓点，否则报错。', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60041', '引导坐标和显示坐标大于1000米检查', '检查同一ADMIN_ID内显示坐标和引导坐标之间距离在设定范围外(默认容差1000米)的记录距离允许用户分种别设定', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60994', 'POI不应落在图廓上', 'POI的坐标不应位于图廓上，否则报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19035', '同一位置多组车信检查', '1.相同进入线，相同退出线，不同经过线的多组车信，车信号码不应相同，否则报log1；
2.相同进入线相同进入点，不同退出线的多组车信，车信号码应相同，否则报log2；
3.相同进入线，相同退出线，无经过线的多组车信，程序报log3', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01420', 'POI行政区划号码检查', 'POI应与其所在的行政区划面的行政区划号码相同，否则报LOG
说明：如果POI落在行政区划面的公共边界上，则POI的行政区划号与任意一个相邻面的行政区划号码相同即可', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60219', 'POI坐标值域检查', '1.POI的引导坐标X、Y不能为空，且不能为0，否则Log
2.POI的显示坐标X、Y不能为空，且不能为0，否则Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60220', '引导Link值域检查', 'POI的引导Link不能为空，且不能为0，否则Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60039', '引导坐标和显示坐标检查（跨国界）', '检查引导坐标、显示坐标所在的Admin_ID应在Ad_Admin表的ADMIN_ID中存在（即引导坐标或显示坐标都未出国界），否则报出', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26046', '路口跨图幅检查', '一个复合路口包含的所有主点与子点只能存在于同一幅图中，否则报Log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26040', '路口名称错误', '配置文件：路口名称检查表SC_INTERSECTION_NAMECK
检查原则：
1.路口的中文名称若为空，则其拼音和英文也必须都为空，否则报log1；
2.路口的中文名称如果不为空，则应在配置文件第一列中存在，否则报log2；
3.路口的中文名称若在配置文件中存在，则其拼音应与配置文件中相应的内容一致，否则报log3；（如果存在中文名称相同，但多个拼音的情况，则需要对比完所有拼音）；
4.检查对象的中文和拼音与配置表中的相应内容一致后，则英文应与配置文件中相应的内容一致，否则报，否则报log4；', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09016', '警示信息错误检查', '停车让行警示信息与减速让行警示信息不允许制作在同一线点关系上，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01446', '形态', '具有“停车场出入口虚拟连接路”属性的LINK，它所挂接的LINK属性只能是“停车场出入口虚拟连接路”属性或“停车场出入口连接路”属性，否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12047', '3d模式图编号规则检查', '检查原则：同一个点（复合路口视为一个点）的不同的进入线，相同的退出线，3D中的箭头模式图记录的高低逻辑关系应该一致(3D号的第4位判断退出线的高低关系)。否则报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01442', '异常点JCT', '点属性不为“JCT”的点，若挂接2条及以上高速或城高的本线，且连接的高速或城高本线的第一官方名称的NAME_GROUPID不同，那么报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01454', '交叉点内道路浮岛检查', '同一路口挂接的所有道路（交叉口内LINK除外）都有区域内部道路形态，路口中任意一条交叉口内LINK没有区域内部道路形态时，程序报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01455', '交叉点内道路浮岛检查', '同一路口挂接的所有道路（交叉口内LINK除外）都有POI连接路形态，路口中任意一条交叉口内LINK没有POI连接路形态时，程序报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13030', '关系型收费站检查', '当收费站类型为“2”“3”“4”“5”“6”“7”时，领卡类型只能为“0”，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13029', '关系型收费站检查', '当收费站类型为“1”“8”“9”“10”时，收费方式只能为“0”，否则报错', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05072', '名称语音一致性检查', '检查对象：RD_BRANCH_NAME表中VOICE_FILE  
检查原则：
VOICE_FILE 字段的位数必须大于3位，否则报LOG
例如分歧名称为G2，名称语音为Ger，则报出', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26005', '复杂路口提左交限检查', '详见附件', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01471', '9级路挂接8级路或7级路检查', '检查原则：9级辅路的端点除9级路外只挂有一条7级路或8级路，并且没有挂接交叉点内道路，则报log
(检查对象屏蔽9级辅路为交叉口内LINK的情况)', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05055', '拼音相同名称不同检查', '检查对象：同一图幅内的道路名称和分歧名称
检查原则：
先对道路的官方名和别名、曾用名的拼音进行比较：
(1)道路只有官方名，无别名和曾用名；
(2)官方名、别名、曾用名的拼音完全一致；
(3)官方名和别名，或官方名和曾用名的拼音一致；              
只要符合以上任意一个条件，则取道路名称的官方名与分歧名称进行比较，若道路名称与分歧名称的拼音相同，中文名称不相同，则报错；
说明：同一link上，与官方名拼音一致的别名或曾用名不查，只查官方名；剩余与官方名拼音不一致的别名或曾用名再与分歧查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01444', '道路方向分类检查', 'RD_LINK的DIRECT字段值不能为0：未调查', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19044', '车信进入线检查', '检查原则：如果车信的车道信息来源字段中包含有“现场”来源的车信，且该车信的进入线为环岛或特殊交通类型，报log', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01456', '交叉点内道路浮岛检查', '路口中任意一条交叉口内LINK与该路口挂接的道路（非交叉口内LINK）开发状态不一致时，程序报LOG', null, null, 1, 'suite2');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05033', '分歧详细信息检查', '1.相同进入线、退出线处存在两个单分歧时，以下情况可以共存，其他情况报Log1：
a)分歧类型为1：方面分歧,模式图代码不为空, 分歧退出线含有“HW对象JCT属性”,且与2：IC分歧共存；
b)分歧类型为1:方面分歧，模式图代码为空，与3：3D分歧或4：复杂路口模式图（7开头）共存；
c)分歧类型为 3：3D分歧和4：复杂路口模式图（7开头）共存
2.相同进入线、退出线处存在三个单分歧，以下情况可以共存，其他情况报log2
分歧类别为“1：方面分歧”，模式图代码为空与3：3D分歧和4：复杂路口模式图（7开头）共存
3.相同进入线、退出线处存在三个以上单分歧，报log3', null, null, 1, 'suite2');

commit;
-- suite3
insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01244', '速度限制错误', '限速类型为“普通”时，单方向道路速度限制方向必须与道路方向相同', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01245', '速度限制错误', '1) 步行道路种别的Link,限速类型为“普通”时，速度限制必须等于10km/h，否则报log1
2) 非引导道路、步行道路种别的Link,限速类型为“普通”时，限速来源必须是“未调查”，否则报log2。
3）不具有的SA或PA属性且link“供用信息”不为“未供用”（APP_INFO≠3）的高速、城市高速,限速类型为“普通”时，限速来源不能是“未调查”，否则报log3
4）非引导道路种别的Link,限速类型为“普通”时，速度限制必须等于15km/h，否则报log4', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01248', '速度限制错误', '限速类型为“普通”时，线限速的来源只能是“现场标牌”、“城区标识”、“匝道未调查”、“未调查”、“方向限速”中的一种，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01249', '速度限制错误', '限速类型为“普通”时，限速来源如果是“匝道未调查”，则道路必须含有匝道形态，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01250', '速度限制错误', '匝道形态的道路上，限速类型为“普通”时，限速来源不能是“未调查”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01252', '速度限制来源一致性检查', '在道路的可通行方向上，限速类型为“普通”时,线限速来源不能是“无”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01254', '速度等级错误', '限速类型为“普通”时,速度等级值不能为“未调查”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01255', '速度等级错误', '限速类型为“普通”时，全封闭道路（不含有JCT属性）限速等级必须<6，即必须为1-5；
屏蔽：link的供用信息为“未供用”（APP_INFO=3）的不检查', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01257', '速度等级错误', '限速类型为“普通”时,步行街形态速度等级必须=8；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01258', '速度等级错误', '限速类型为“普通”时，双方向道路速度等级必须>=3，即必须为3-8，否则报log
屏蔽：如果道路含有JCT且有调头口，则不报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01260', '速度等级错误', '限速类型为“普通”时,FC=4的道路，速度等级必须>1，即必须为2-8；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01261', '速度等级错误', '限速类型为“普通”时，人渡/轮渡种别、SA/PA形态（不含有JCT属性）、POI连接路形态Link的速度等级必须为7', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01262', '速度等级错误', '限速类型为“普通”时，步行道路的速度等级必须为8', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01277', '速度等级变化点错误', '一个点只连接了两条Link，如果两条link（任意一根link不能存在交叉口link属性）中没有IC、JCT、桥、区域内道路、步行街、普通匝道属性的Link，且限速类型为“普通”时，限速等级值之差不允许大于等于3', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01295', '线线结构构成link检查', '一组线线的所有link中不能有步行街属性的link
一组线线的所有link中不能有人渡、轮渡种别的link', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01296', '线线结构构成link检查', '线线结构的进入线和退出线不能挂接在一个点上，也不能挂接在一个复合路口上；
以下情况例外：
1.线线信息为车信，且进入线是交叉口内link
2.线线信息为车信，且退出线是环岛或特殊交通
3.线线信息为车信，且退出线沿车信方向的起点挂接了环岛或特殊交通类型的link。
4.经过线不都是连接此复合路口', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01297', '线线结构构成link检查', '退出线不能是交叉口内link', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01307', '速度限制来源一致性检查', '单向道路的限速类型为“普通”时，限速来源不能是“方向限速”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01340', '停车场出入口检查', '停车场出入口Link不能和高速等级道路直接挂接', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01349', '线-A-FC错误', '若点具有障碍物信息，且该点挂接有FC为1-4的Link报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01356', '道路连接路检查', '检查对象：调头口3D（80000001,80000003）的退出线
检查原则：检查对象的道路连接路属性不能为“调头口”，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01357', '总车道数和左右车道数约束检查', '单方向的link上不能存在左右车道数，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01363', '收费有误', 'SA、PA属性的道路的收费信息不能为“收费”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01377', '车道数与幅宽检查', '检查对象：车道数（如果总车道数不为空，使用总车道数；如果总车道数为空，使用左车道数+右车道数）、幅宽
检查原则：不满足以下对应关系的报log：
如果车道数为1，幅宽必须为30；
如果车道数为2或者3，幅宽必须为55；
如果车道数为4-15，幅宽必须为130；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01390', '停车场检查', '检查对象：具有“停车场出入口连接路”属性的双方向（道路方向为未调查或双方向）道路
检查原则：检查对象应具有“POI连接路”属性，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01393', '停车场检查', '具有“停车场出入口虚拟连接路”属性的道路，等级必须为8，车道总数必须为1，通行方向必须为双方向，必须制作有穿行限制信息，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01394', '停车场检查', '具有“停车位引导道路”属性的道路，等级必须为8，速度限制等级必须为8，车道等级必须为1，道路功能等级为5，不应具有“POI连接路”属性，且一定制作有穿行限制，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01395', '环岛限速检查', '如果是7级及以上的环岛或特殊交通为“是”，限速值小于等于11时报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01406', '道路连接路检查', '检查对象：调头口模式图（80000003）
检查原则：
调头口前方60米内应存在复合路口：
a）检查对象沿进入线的延长线方向（不考虑中间有无道路打断）在60米以内（包括60米）能找到路口，否则报log；
b）检查对象沿退出线的逆方向在120米以内能找到路口，否则报log；
2c）在2a）和2b）中找到的路口，应为同一个路口，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01411', '超车限制检查', '单方向的道路只能制作单方向的禁止超车（Link限制类型为超车限制），并且禁止超车的方向应与Link的通行方向一致，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01417', '航线/水逻辑关系错误', '航线与水系必须在航线串的起止点处制作立交关系，并且航线的zlevel为1，否则报log
', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01418', '道路施工检查', '同一根LINK限制信息不能同时存在“施工中不开放”和“道路维修中”，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01419', '总车道数和左右车道数约束检查', '左车道数与右车道数相等且不等于0，报出LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01420', 'POI行政区划号码检查', 'POI应与其所在的行政区划面的行政区划号码相同，否则报LOG
说明：如果POI落在行政区划面的公共边界上，则POI的行政区划号与任意一个相邻面的行政区划号码相同即可', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01437', '空的限速关系', '限速类型不为“普通”时，同一根LINK的顺向限速和逆向限速值不能同时为0，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01442', '异常点JCT', '点属性不为“JCT”的点，若挂接2条及以上高速或城高的本线，且连接的高速或城高本线的第一官方名称的NAME_GROUPID不同，那么报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01444', '道路方向分类检查', 'RD_LINK的DIRECT字段值不能为0：未调查', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01445', '道路种别检查', '道路link种别不能存在15：10级路(障碍物)，否则报Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01449', '上下线分离道路方向检查', '一组CRFR的两条LINK为LA和LB,其结点分别为NA和NB,若NA和NB属于同一组CRFI，则LA进入NA,LB退出NB或者LA退出NA,LB进入NB，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01450', '速度限制', '限速类型为“普通”时，高速、城高的可通行方向上的速度限制不能为0，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01454', '交叉点内道路浮岛检查', '同一路口挂接的所有道路（交叉口内LINK除外）都有区域内部道路形态，路口中任意一条交叉口内LINK没有区域内部道路形态时，程序报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01455', '交叉点内道路浮岛检查', '同一路口挂接的所有道路（交叉口内LINK除外）都有POI连接路形态，路口中任意一条交叉口内LINK没有POI连接路形态时，程序报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01004', '航线/水逻辑关系错误', '对象：航线link组（包括轮渡、人渡，多根航线Link组成的Link串），同时满足以下特征：
1）起、终点：同时挂接了有航线种别的link和没有航线种别的link
2）中间点：挂接且仅挂接了两条有航线属性的link
原则：
1）航线link串只能与水系（link上存在水系的种别）有2个交点，否则报log1，提示信息为：“航线与水系边线多次相交！”
说明：如果航线起/终点挂接的航线link被图廓点打断，需要把图廓点挂接的两条航线当做一条
2）航线link串不能与绿地、铁路相交，否则报log2，提示信息为：“航线与非水系相交！”
说明：考虑跨图幅', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01008', '无方向', '全封闭道路不能为“双方向”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01010', '无方向', '有特殊交通类型形态的Link，方向不能为双方向或未调查', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01012', '无方向', '有上下线分离、环岛的Link，不能是双方向或未调查', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01013', 'LINK与图廓线重合', '道路link不能与图框线全部或部分重合', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01015', '闭合环未打断', '两条或者多条不同的Link具有相同的两个端点', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01021', '航线检查', '一个Node点只挂接了航线，没有挂接其他道路（图廓点除外）。若该Node挂接航线link数为1或大于等于3，报err（NIDB－K：挂接link数为2，也报err）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01025', '形状有异', 'link的起点应该与该link的第一个形状点坐标一致，否则报err；
link的终点应该与该link的最后一个形状点坐标一致，否则报err；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01027', 'Link形状点数>=490', 'Link的形状点数应小于490', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01028', '10级路/步行街/人渡错误', '1.10级路/步行街/人渡不能是单向道路。
2.10级路/步行街/人渡不能具有IMI属性。
3.10级路/步行街/人渡不能是EG大门的进入、退出线。
4.10级路/步行街/人渡不能是SE的进入、退出线。
5.10级路/步行街/人渡不能是关系型收费站的进入、退出线。
6.10级路/步行街/人渡不能是点收费站的进入、退出线。
7.10级路/步行街/人渡不能是坡度的退出线、延长线。
8.10级路/步行街/人渡不能是分歧的进入线，退出线，经过线。
9.10级路/步行街/人渡不能是交限的进入线，退出线，经过线。
10.10级路/步行街/人渡不能是语音引导的进入线，退出线，经过线。
11.10级路/步行街/人渡不能是顺行的进入线，退出线，经过线。
12.10级路/步行街/人渡不能是车信的进入线，退出线，经过线。
13.10级路/步行街/人渡不能是警示信息的进入线。
14.10级路/步行街/人渡不能是可变限速的进入线，退出线，经过线。
15.10级路/步行街/人渡不能制作单向通行限制。
16.10级路/步行街/人渡不能制作超车限制。
17.10级路/步行街/人渡不能制作条件限速（RD_LINK_SPEEDLIMIT表中的SPEED_TYPE=3）。
18.10级路/步行街/人渡不能制作红绿灯受控信息.', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01031', '长度过短', '道路link长度应大于2米（交叉点内link不查）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01039', '收费有误', 'IC属性道路（直连IC除外）的收费信息不能为“收费”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01040', '收费有误', '3级及以下道路的收费信息不能为“收费”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01042', '收费有误', '所有道路的收费信息不能为“收费道路的免费区间”或“未调查”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01081', '点-B-FC断头点', '判断点的接续Link（检查功能等级FC1-4的道路）中最高功能等级的是否只有一条，如果是那么说明此点是一个FC断头点', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01086', 'FC错误', '8/9级道路FC必须为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01089', 'FC错误', '制作了穿行限制或车辆限制道路FC必须为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01090', 'FC错误', '步行街的FC必须为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01091', 'FC错误', '大门的进入和退出link的FC必须为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01092', 'FC错误', '私道的FC应该为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01093', 'FC错误', '10级路的FC必须为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01094', 'FC错误', 'SA/PA属性的link，若不含IC/JCT,FC应为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01095', 'FC错误', '含“区域内道路”形态的link的FC应为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01096', 'FC错误', 'POI连接路形态的道路FC必须为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01097', 'FC错误', '公交专用道形态的道路FC应为5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01098', 'FC1-4的开发状态检查', '当link的开发状态为“未验证”时，其FC只能是5', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01104', '车道数与幅宽检查', '轮渡、人渡、9级路、10级路的总车道数必须为1', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01105', '车道数与幅宽检查', '步行街属性的总车道数必须为1', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01108', '总车道数和左右车道数约束检查', 'Link上的总道数和左右车道数不能同时为空或同时为0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01109', '总车道数和左右车道数约束检查', 'Link上的总道数和左右车道数不能同时有值', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01110', '总车道数和左右车道数约束检查', 'Link上的左右车道数不能一边有值一边无值或为0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01176', '高速与普通相连', 'IC以外的高速或城高不能挂接普通道路（步行道路种别不检查）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01180', '交叉点内道路非路口', '1.交叉口内LINK应属于某一个路口，否则报LOG1
2.LINK的两个NODE点属于同一个路口，则该LINK应属于该路口，否则报LOG2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01184', '上下线分离之间LINK未登记复合路口', '一条link的两个端点，如果各挂接了1条MD属性link（非高速，非城市高速），所挂接的同一组MD属性link的 NAME_GROUPID有一组相同(或者同时无NAME_GROUPID)时，并且该link本身不是交叉口内link、环岛、上下线分离、特殊交通类型，报log；如果该link的两个端点是两个不同的路口，那么屏蔽log；link长度如果超过50米，则不做该检查；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01221', '特殊种别属性', '道路种别和形态共存性参见元数据库SC_ROAD_KIND_FORM', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01238', '速度限制变化点错误', '一个点（非收费站、桥、隧道、CRFI点）只连接了两条Link（任意一根link不能存在交叉口link属性），两条Link的限速类型为“普通”时，同一速度限制方向上的值相差不应大于等于60公里，（除非link上有IC、JCT、桥、过街天桥、步行街、普通匝道属性、隧道），否则报err
（10冬link开放两边速限值不一致）
屏蔽：
1.挂接两条link中任意一根link是点限速关联的link，不查
2.如果两根link中，其中一根link的道路等级为8或者10，则不检查；但是两根都是8级或者都是10级则需要检查
3.如果两根link中，一根link是双方向，另一侧是单方向，则不检查；
4.如果两根link的限速来源都是未调查的，则不检查；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01239', '速度限制错误', '限速类型为“普通”时，道路可通行方向上的速度限制均不能为0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01240', '速度限制错误', '1.限速类型为“普通”时，全封闭道路速度限制必须≥50km/h，全封闭道路有桥或隧道或JCT或IC或匝道形态的应屏蔽不报LOG；
如果全封闭道路速度限制<50km/h且顺向\逆向限速来源为"现场标牌"的屏蔽不报log；
2.非全封闭道路必须<=130km/h', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01241', '速度限制错误', '限速类型为“普通”时，（1）步行街为双方向时，速度限制必须等于10km/h
（2）当步行街为单方向时，和道路方向相同的速度限制必须等于10km/h，和方向相反的速度限制必须等于0km/h', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01242', '速度限制错误', '限速类型为“普通”时，双方向道路的速度限制必须<=100km/h,否则报log
屏蔽：如果道路含有JCT且有调头口，则不报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01243', '速度限制错误', 'FC为1，2，3的道路，限速类型为“普通”时，速度限制必须>=15km/h；
FC为4的道路，限速类型为“普通”时，速度限制限制必须<=130km/h', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11029', '模式图编号错误', '检查对象：所有分歧（普通分歧、IC分歧、方面分歧）的模式图号码
检查原则：分歧模式图号码第二位如果是“a”，报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11030', '模式图编号错误', '检查对象：所有分歧（普通分歧、IC分歧、方面分歧）的模式图号码
检查原则：分歧模式图号码不能为如下值，否则报err。
00ff0004、10ff0004、20ff0004
00ff000d、10ff000d、20ff000d
00ff000f、10ff000f、20ff000f
00ff0010、10ff0010、20ff0010
00ff0012、10ff0012、20ff0012
00ff0013、10ff0013、20ff0013
00ff0017、10ff0017、20ff0017
00ff0019、10ff0019、20ff0019
00ff0008、10ff0008、20ff0008
00ff0015、10ff0015、20ff0015', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11034', '名称中选项匹配错误', '分歧类型是方面且分歧模式图ID为空的不做检查。
如果分歧类型是普通，那么设施属性必须是默认，名称种别也必须是默认，并且不能有分歧名称。
如果分歧类型是IC（下面3种情况互斥，顺序检查），
1) 如果分歧退出线属性含SA（PA）和IC，则设施属性为默认，名称种别为IC，并且必须有分歧名称。
2) 如果分歧退出线属性含SA（或PA），则设施属性为SA（或PA），名称种别为SA（或PA），并且必须有分歧名称。
3) 如果分歧退出线属性是其他情况，则设施属性必须是默认，名称种别也必须是IC，并且必须有分歧名称。
如果分歧类型是方面（下面3种情况互斥，顺序检查），
1) 如果分歧退出线属性含JCT，则设施属性为JCT，名称种别为默认，并且必须有分歧名称。
2) 如果分歧退出线属性含IC，则设施属性为出口、入口，名称种别为默认，并且必须有分歧名称。
3) 如果分歧退出线属性含SA（或PA），则设施属性为SA（或PA），名称种别为默认，并且必须有分歧名称。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11036', '引导错误', '从进入线的方向终点开始，沿着交叉口内link，考虑方向，必须存在着至少一条可通行路径（该路径所有link都是交叉口内link），到达退出线的起点
增加说明：此条原则已包含RD_BRANCH表下各项类型', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11038', '分歧模式图添加错误', '详见检查规则库附件', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11039', '分歧退出线属性错误', '一组高速分歧（同一进入线，不同退出线）如果退出线没有匝道或者全封闭属性或者SA或者PA或IC的，程序报出', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11040', '分歧名称错误', '检查对象：相同进入线、退出线、相同类型的多个高速分歧名称。
检查原则：分歧名称不能重复。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11041', '分歧名称错误', '检查对象：相同进入线、退出线、相同类型的多个高速分歧名称拼音。
检查原则：分歧名称拼音不能重复。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11059', '分歧出口编号检查', '线线关系的分歧只能为
1.模式图编号为空的方面分歧
2.3d分歧
3.连续分歧
否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11072', '分歧模式图号码检查', '分歧的进入线和退出线如果都是高速或者城高，那么一定要有分歧模式图号码', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11073', '分歧模式图号码检查', '如果是方面分歧并且进入线和退出线都不是高速或者城高，那么该分歧不应该有分歧模式图号码和出口编号', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11098', '线线关系检查', '分歧类别为特殊连续分歧时，线线分歧经过线不为空，设施属性和名称种别为默认，模式图号码不能为空，箭头图代码的第一位数字必须是“1，2”中的一个，第二位数字必须为“a”。否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11100', '线线关系检查', '分歧类别为普通时。
设施属性和名称种别必须为默认。
分歧模式图ID的第二位数字必须是“3，4”中的一个。如果为“3”，则分歧模式图ID的第一位数字必须是“0，2”中的一个；如果为“4”，则分歧模式图ID的第一位数字必须是“0，2，3”中的一个。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11101', '线线关系检查', '分歧类别为“普通”、“方面”、“特殊连续分歧”时，其“经过线”不能为空，否则报err。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11103', '线线关系检查', '分歧类别为普通或特殊连续分歧时，一组线线分歧不能完全包含在另一组分歧中（即分歧A的全部link都在分歧B中存在），否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11106', '分歧名称错误', '对于进入线相同，退出线不同的高速方面分歧（进入线或退出线是高速或城高种别的线点线方面分歧），这些高速分歧的第一名称均不相同，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11114', '分歧名称检查', 'RD_BRANCH_NAME中的分歧名称NAME字段不能为空，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11116', '名称长度检查', 'LANG_CODE=ENG时，分歧名称不超过35个字符，否则报LOG；第一分歧名称对应的英文名称长度不能超过30个字符，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11118', '名称长度检查', 'RD_BRANCH_NAME表中“分歧名称+名称发音”关联SC_BRANCH_ENGNAME表中“分歧名称+名称发音”，找到与元数据记录GROUP_ID相同的分歧英文名称，其长度不能超过35个字符，否则报log；高速第一分歧英文名称长度不能超过30个字符，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11122', '分歧名称错误检查', '分歧的名称中包含“**服务区”“**停车区”的名称，且分歧的退出线为SA/PA属性、且分歧类型为IC分歧，则该名称必须是第一分歧名称（SEQ_NUM最小），否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12004', '3d进入线或退出线种别属性错误', '3d的进入线和退出线种别不能同时是高速（包括高速和城高种别）或者是9级路。注：排除专用模式图不查。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12005', '3d进入线或退出线种别属性错误', '3d的进入线不能有交叉点内Link形态', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12006', '3d进入线或退出线种别属性错误', '3d的退出线不能有交叉点内Link形态', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12011', '3D处顺行检查', '加了3D的进入LINK和退出LINK如果箭头符号分类为副，那么从进入Link到退出LINK不因该加顺行，同时路口上的路口语音也不应该加直行，否则报错。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12012', '普通道路复杂路口模式图检查', '做了3D模式图以7开头的点，并且是路口的，则路口中相同进入线和退出线之间不能存在顺行，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12016', '专用模式图检查', '制作了提左加调头的3D箭形分类必须有主（c）、副（e）退出线，且主(c)在左，副(e)在右，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12017', '专用模式图检查', '制作了提左、提右、调头、高速入口模式图的3D箭形分类只有副(e)退出线，没有主(c)退出线，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12018', '专用模式图检查', '制作了80000800、80000801、80000802、80000803、80000001、80000003的3D，退出LINK在以进入线延长180度的左侧，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12019', '专用模式图检查', '制作了80100000、80100001、80000200、80000002、80000004的3D，退出LINK在以进入线延长180度的右侧，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12020', '专用模式图检查', '制作了提左加调头的3D，主退出与副退出的号码后七位必须完全相同，且与模式图号的后七位完全相同，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12021', '专用模式图检查', '制作了专用模式图（提右、高速入口模式图除外）的3D信息，其进入和退出Link都必须是7级以上（含7级）道路，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12022', '专用模式图检查', '制作了专用模式图（高速入口模式图除外）的3D信息，其进入和退出link不能同时为高速或城市高速，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12042', '分歧名称检查', '当分歧类型为方面分歧和IC分歧时，必须有分歧名称，且名称个数值域为[1,10]，否则报log
当分歧类型为高速分歧、3D分歧和普通分歧时，必须无分歧名称，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12043', '分歧名称检查', '当有分歧名称时，分歧名称和名称发音字段均不能为空', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12044', '逻辑关系错误', '制作分歧信息的点，若该点未制作路口或制作有单路口，则应至少挂接3条link，否则报错；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12045', '箭头图号码检查', '3D分歧箭头图branch_type类型为3，号码以“e”或者“c”开头，否则报log1；3D分歧箭头图branch_type类型为4，号码以“d”、“e”或者“c”开头，否则报log1
同一进入link的3D分歧只能有一条主退路（模式图代码不同则不检查），否则报log2；
同一进入link的3D分歧若有多条辅退路，它们的箭头图编号最后一位应当连续，且不能重复，否则报log3。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12047', '3d模式图编号规则检查', '检查原则：同一个点（复合路口视为一个点）的不同的进入线，相同的退出线，3D中的箭头模式图记录的高低逻辑关系应该一致(3D号的第4位判断退出线的高低关系)。否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM12049', '专用模式图检查', '3D分歧的关系类型RELATIONSHIP_TYPE必须为1（路口关系），否则报log
屏蔽：1、提左（80000800、80000802）、提左+调头（80000801、80000803）、调头（80000001）的不查
      2、调头（80000001），进入线、经过线、退出线不在同一图幅中的不查', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13002', '关系型收费站检查', '收费站主点的挂接link数必须是2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13003', '关系型收费站检查', '如果一条link的两个端点都做有收费站，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13020', '关系型收费站检查', '收费站类型为“领卡”或“持卡打标识不收费”或“验票领卡”或“交卡不收费”，收费通道的领卡类型只能有“ETC”或“人工”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13022', '关系型收费站检查', '收费站类型不为“未调查”时，
ETC模式图号第1位必须为“T”；
收费通道数小于6时，第3位与收费通道数相同；
收费通道数大于等于6时，第3、7、8位一定为0；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13023', '关系型收费站检查', '收费站的收费通道数必须大于0小于等于16：
1、收费站通道数等于0时，报LOG；
2、收费站通道数大于16时，报LOG；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13029', '关系型收费站检查', '当收费站类型为“1”“8”“9”“10”时，收费方式只能为“0”，否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13030', '关系型收费站检查', '当收费站类型为“2”“3”“4”“5”“6”“7”时，领卡类型只能为“0”，否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13032', '收费站英文一致性检查', '收费站中，中文一致保证英文一致。否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13033', '关系型收费站', '如果收费通道总数不为0或ETC图标代码不为空，则收费站类型不能为"未调查"', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13034', '收费站共线检查', '检查对象:RD_LINK
检查原则：如果一个收费站的退出link与另一个收费站（主点不同）的进入link为同一link，则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13035', '内容检查', 'SC_TOLLGATE_NAMECK中NAME_CHI与RD_TOLLGATE_NAME的中文收费站名称相同，但关联的收费站英文名与SC_TOLLGATE_NAMECK中NAME_ENG不同时，则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM13037', '收费站名称错误检查', '关系型收费站必须存在收费站名称，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM15007', '限速值与限速来源关系检查', '当道路为单方向时，顺向或逆向速度限制为0时，对应的速度限制来源应该为0，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM15008', '限速类型错误', '当内业作业状态≠2时，如果时间段有值，则限速类型一定为特定条件3且限速条件为10（时间限制）或限速条件为6（学校），否则报log ', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19001', '错误的车信进入线或退出线', '公交车专用道不能作为路口车信的进入线、退出线，否则报err；
公交车专用道不能作为线线车信的进入线、经过线、退出线，否则报err；
非引导道路种别不能作为路口车信的进入Link、退出Link，否则报err；
非引导道路种别不能作为线线车信的进入Link、经过线、退出Link，否则报err；
线线经过线是交叉口内link的情况，不进行检查。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19003', '通往退出道路的车道数为0', '车道信息中，必须有到退出Link的车道信息', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19004', '车道数检查', '车道信息中，inlink车道数不应为0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19005', '车道数检查', '一组车道信息的左右附加车道数总和小于车信的进入车道数，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19013', '逻辑关系错误', '路口车信上的进入线不能是环岛或者是特殊交通类型', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19014', '逻辑关系错误', '路口车信上的进入线和退出线不能是交叉点内link', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19019', '车信进入退出线属性检查', '线线结构中车信的进入线不能为环岛或特殊交通类型，退出线不能是交叉口内link', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19021', '车信进入线退出线检查', '检查对象：线线车信的退出link
检查原则：线线车信退出线沿车信方向的起始点只挂接了两条link（包括退出线本身）的报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19022', '车信进入线退出线检查', '检查对象：线线车信的进入link
检查原则：线线车信的进入线沿车信方向的终止点只挂接了两条link（包括进入线本身）的报log
增加屏蔽条件：车信的第一根经过线有环岛或特殊交通类型时不报。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19023', '单路口车信检查', '只挂接两条Link的路口（单路口）不应该制作车信，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19025', '车信结构检查', '车道数与可通行车道一致性检查。比如LANE_NUM是2，则IN_LANE_INFO的第15、14位至少有一位为1，其余位必须为0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19032', '车信进入退出一致性检查', '车信进入线记录的车道信息应与退出线上记录的全部通达方向完全匹配（包括附加车道、公交专用道所在的车道位置），否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19033', '关系类型检查', '1.车信的进入和退出link如果都挂接在同一路口或同一个点，车信的进入线不是交叉口内道路，那么关系类型为“路口关系”，否则报错；
屏蔽：车信的经过点中有图廓点的情况
2.满足以下任一条件时，车信的关系类型必须是线线关系，否则报错；
1）车信的进入和退出link如果未挂接在同一路口或同一个点上；
2）车信的进入线是交叉点内道路；
3）车信的LINK跨过两个及以上图幅', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19034', '经过线距离检查', '车信经过线应小于等于10条，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19035', '同一位置多组车信检查', '1.相同进入线，相同退出线，不同经过线的多组车信，车信号码不应相同，否则报log1；
2.相同进入线相同进入点，不同退出线的多组车信，车信号码应相同，否则报log2；
3.相同进入线，相同退出线，无经过线的多组车信，程序报log3', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19036', '车信包含检查', '同一组车信（车信号码一致），如果通达方向一致，但车通号码不同，且其中一个车信的进入线、经过线、退出线完全包含另一个车信，则报log
', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM19045', '车信与分岔口提示共存', '以SE的进入线为进入线的车信，如果都是直行（LANE_INFO屏蔽空车道和公交车道），则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20001', '高度层次错误', '在一组立交关系中，Link间的高度层次从低到高必须相差1。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20002', '高度层次错误', '包含两根或两根以上Link的立交关系中，高度层次值必须从0开始。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20004', '高度层次错误', '一组立交关系的所有Link不能挂接在同一端点上，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20005', '高度层次错误', '高度关系参见附表', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20008', '高度层次错误', 'Node处有Zlevel信息时，则该Node的接续Link的Zlevel值应该一致，否则报Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20016', '无Z-level错误', '当两条Link（道路与CityModel)立交时，必须制作做立交关系，否则报log1；
当两条Link（道路与CityModel)不能全部或者部分重叠，否则报log2；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20034', '立交关系', '检查对象：RD_GSC\RD_GSC_LINK
检查原则：RW_LINK不能与土地覆盖link、市街图link之间制作立交关系', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM20035', '立交点坐标', '道路与道路立交或者道路与铁路立交时，立交点不能在图廓线上，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23002', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中LOCATION(电子眼方位) 
检查原则：  
电子眼方位只能为“左”、“右”、“上”中的其中一个，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23003', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中VERIFIED_FLAG(验证标识)
检查原则：  
电子眼验证标识只能为1（验证正确），否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23005', '电子眼值域检查', '检查对象：RD_ELECTRONICEYE表中DIRECT(电子眼作用方向)
检查原则：  
电子眼的作用方向应该与道路的方向一致，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23007', '电子眼关联link检查', '电子眼关联link不能是交叉口内link、10级路、人渡、轮渡，否则报log1。
应急车道摄像头只允许制作在高速、城市高速道路上，否则报log2.', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23009', '电子眼限速值域检查', '电子眼限速值（SPEED_LIMIT）的值域范围必须为：0或100~1200之间且必须为50的倍数，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23010', '电子眼区间测速匹配检查', '检查对象：区间测速电子眼组成表（RD_ELECEYE_PART），电子眼表（RD_ELECTRONICEYE）
检查原则：当“区间测速电子眼组成表”中存在记录时，同一组号（GROUP_ID）中记录的两个电子眼号码（ELECEYE_PID）在电子眼表中的电子眼类型（KIND）必须一个为区间测速开始（20），另一个为区间测速结束（21），否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM23011', '电子眼区间测速匹配检查', '检查对象：区间测速电子眼组成表（RD_ELECEYE_PART）
检查原则：同一组号（GROUP_ID）只能对应两个电子眼，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26004', '顺行错误', '高速上做了顺行的路口，并且该路口还做了分歧信息，如果分流处顺行的退出link中有属性为IC/JCT/SA/PA/R（匝道）的话，则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26010', '交叉点内link与上下线分离检查', '当路口为2个点时，若交叉点内link进入link个数<=1则交叉点内link不可赋上下线分离', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26011', '交叉点内link与上下线分离检查', '当路口为3个点时，若交叉点内link的进入与退出线为上下线分离则此交叉点内link应被赋上下线分离', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26012', '交叉点内link与上下线分离检查', '当路口为4个点时，若交叉内的link所挂接的进入link、退出link为上下线分离的且角度为180度的，那么该交叉口内link应被赋为上下分离', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26033', '路口名称错误', '检查对象：RD_CROSS_NAME
检查原则：1.相同的NAME_GROUPID，CHI和ENG要同时存在，否则报Log；
2.所有名称内容不能为空，否则报Log；
3.当lang_code为“CHI”或“CHT”时,名称拼音内容不能为空，否则报Log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26039', '重复路口', '两个路口不能共用一个node点，否则报log。log包括所有含有该node路口的cross号码。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26041', '语言代码检查', '语言代码中:
1.大陆 语言代码必须有CHI，且只能为CHI和ENG，否则报错
2.语言代码为"ENG"时,名称发音 字段应为空', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26044', '路口内link属性检查', '路口内Link表中记录的道路必须具有交叉口内link属性，且一定不能具有环岛属性，特殊交通必须为“否”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26046', '路口跨图幅检查', '一个复合路口包含的所有主点与子点只能存在于同一幅图中，否则报Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26050', '路口点线正确性检查', '1.路口中的线必须是交叉口内LINK，否则报LOG1
2.路口中的线的两个NODE应属于该路口；否则报LOG2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26051', '路口点线正确性检查', '路口中的点应在该路口的线的NODE中存在，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM26052', '路口类型检查', '当路口只有一个节点时，必须为简单路口。当路口有两个及以上节点时，必须为复合路口', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM56035', '空的限速关系', '当“内业作业状态”≠2时，做如下检查：
限速值不能为0，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60039', '引导坐标和显示坐标检查（跨国界）', '检查引导坐标、显示坐标所在的Admin_ID应在Ad_Admin表的ADMIN_ID中存在（即引导坐标或显示坐标都未出国界），否则报出', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60041', '引导坐标和显示坐标大于1000米检查', '检查同一ADMIN_ID内显示坐标和引导坐标之间距离在设定范围外(默认容差1000米)的记录距离允许用户分种别设定', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60218', '显示坐标与道路距离检查', '检查对象：状态字段为“新增”或“修改”且种别不为230201、230202的POI点位
检查原则：逐一判断POI点显示坐标距离道路最近距离是否小于5米且大于1.5米，如果小于5米且大于1.5米则报出log
屏蔽对象：
1、若POI的标记字段含有“路”，不报log
2、POI的类别为小区（120201）、轮渡（230125）、收费站（230208）、磁悬浮主点（230115）、地铁站主点（230112）、长途客运站出入口（行人导航）（230110）、出租车停靠站（230117）、出租车站出入口（230118）、公交车站、BRT（230101）、公交车站出入口（230102）、缆车站出入口（230122）、缆车站主点（230121）、水上公交车站出入口（230124） 、水上公交站（230123）、小巴出入口（行人导航）（230120）、专线小巴站（230119），不报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60219', 'POI坐标值域检查', '1.POI的引导坐标X、Y不能为空，且不能为0，否则Log
2.POI的显示坐标X、Y不能为空，且不能为0，否则Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60220', '引导Link值域检查', 'POI的引导Link不能为空，且不能为0，否则Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60221', '引导坐标不位于道路上', 'POI的引导坐标与其引导Link的最近距离，应小于等于2米，否则报出LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60225', '桥显示坐标与道路的关系检查', '检查对象：种别为“230201”或“230202”的POI
检查原则：
  1.如果引导Link的上下线分离属性为“否”，那么显示坐标到引导link的最短距离在1.5米到5米之间，报错
  2.如果引导link的上下线分离属性为“是”，那么显示坐标应位于该对上下线分离道路的中间', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60234', '标注“路”的POI不位于道路上检查', '检查对象：标注字段中含有“路”的POI(排除标记“跨路”的数据)
检查原则：
1.如果引导Link的上下线分离属性为“否”，那么显示坐标到引导Link的最短距离应小于1米；
2.如果引导Link的上下线分离属性为“是”，那么显示坐标应位于该对上下线分离道路的中间；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60297', 'POI引导坐标显示坐标距离检查（5000-10000）', '检查条件：
(1) IX_POI表中“STATE(状态)”为非1（删除）；
检查原则：
POI的引导坐标与显示坐标距离大于等于5000米并且小于等于10000米，报出Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60298', 'POI引导坐标显示坐标距离检查（10001-15000）', '检查条件：
(1) IX_POI表中“STATE(状态)”为非1（删除）；
检查原则：
POI的引导坐标与显示坐标距离大于等于10001米并且小于等于15000米，报出Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60299', 'POI引导坐标显示坐标距离检查（15001以上）', '检查条件：
(1) IX_POI表中“STATE(状态)”为非1（删除）；
检查原则：
POI的引导坐标与显示坐标距离大于等于15001米，报出Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60337', 'POIICON距离检查', '检查对象：制作有POI3DICON属性的POI
检查原则：对所有制作有POIICON属性的POI，判断他们相互之间的距离是否小于20米，若小于20米则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60347', '普通POI引导LINK左右行政区划号检查', '检查对象：IX_POI表中的全部POI
检查原则：如果POI在路上（位置关系为“Link上”），且引导LINK的左右行政区划号不相同，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60348', '停车区、服务区的POI或其子关联LINK属性检查', '检查对象：IX_POI表中的全部POI
检查原则：
种别为服务区（230206），停车区（230207）的POI或其子的引导LINK没有服务区属性（0B），也没有停车区属性（0C），报Log
注：以上涉及到取父POI编号和子POI编号分别从IX_POI_PARENT和IX_POI_CHILDREN表中获得', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60363', '服务区POI或其子关联LINK属性检查', '检查对象：IX_POI表中种别为服务区（230206）的POI
涉及到取父POI编号和子POI编号分别从IX_POI_PARENT和IX_POI_CHILDREN表中获得
检查原则：
若种别为服务区（230206）的POI或其子的引导Link同时具有停车区（0C）和服务区（0B）属性，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60364', '服务区POI或其子关联LINK属性检查', '检查对象：IX_POI表中种别为停车区（230207）的POI
涉及到取父POI编号和子POI编号分别从IX_POI_PARENT和IX_POI_CHILDREN表中获得
检查原则：
若种别为停车区（230207）的POI或其子的引导Link同时具有停车区（0C）和服务区（0B）属性，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM60994', 'POI不应落在图廓上', 'POI的坐标不应位于图廓上，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01456', '交叉点内道路浮岛检查', '路口中任意一条交叉口内LINK与该路口挂接的道路（非交叉口内LINK）开发状态不一致时，程序报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01457', '交叉点内道路浮岛检查', '路口中任意一条交叉口内LINK与该路口挂接的道路（非交叉口内LINK）道路等级不一致时，程序报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01458', '交叉点内道路浮岛检查', '同一路口挂接的所有道路（交叉口内LINK除外）有施工、穿行限制、车辆限制中的一种或多种信息时，假设这个信息的集合为U，那么任意一条交叉口内LINK的限制类型应是这个集合的子集，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01459', '交叉点内道路浮岛检查', '路口中交叉口内LINK(9级路不检查)都有施工、穿行限制、车辆限制中的一种或多种信息时，假设这个信息的集合为A，那么该路口挂接的其它道路都应包含施工、穿行限制、车辆限制中的一种或多种信息，且该信息的集合B应包含A', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01470', '10级路错误', '10级路的行人步行属性应为“允许”，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01471', '9级路挂接8级路或7级路检查', '检查原则：9级辅路的端点除9级路外只挂有一条7级路或8级路，并且没有挂接交叉点内道路，则报log
(检查对象屏蔽9级辅路为交叉口内LINK的情况)', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01472', 'POI与引导link的位置关系检查', 'POI索引中的GUIDE_LINK_SIDE的值与POI在道路中的位置不相符时，报log。
说明：点位于道路的距离判断修改为“1.5”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01474', '非新增POI的引导LINK值域检查', '非新增POI（state<>3）的引导LINK不能为空或0，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01475', '新增SA/PA类型POI的引导LINK值域检查', '新增的SA/PA类（种别为230206、230207）的POI的引导LINK不能为空或0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01505', '交叉口内link速度限制值检查', '检查对象：交叉口内link
检查对象的限速值应与检查对象所在的复合路口挂接其挂接的所有link（道路属性为交叉口link除外）的限速值中任意一个限速值相同，如果交叉口内link含掉头口属性且是点限速关联link的除外，否则报log。
（交叉口内link：考虑顺逆限速，两个方向都查；路口挂接link：不考虑顺逆限速来源）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01512', 'POI与大门逻辑关系检查', '检查对象：
IX_POI表中“STATE(状态)”字段为“3（新增）”或“2（修改）”，且LOG字段包含“改RELATION”
检查原则：
1、以道路大门点做50m半径，搜索后以“门”或“门）”结尾的poi；
1）中POI的父POI（分类为180105和120101的，不报log）；
2）中POI的二级父POI（分类为180105和120101的，不报log）；
2、符合检查规则的poi，如果关联link挂接了比自身等级高的道路，不报LOG。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01521', '速度等级错误', '检查对象：含“IMI”属性的link；
检查原则：限速类型为“普通”时，
(1)该link若为II属性，则其限速等级不能为1，否则报err
(2)该link若为M属性，则其限速等级不能为1、2、8，否则报err
(3)该link若为I属性，则其限速等级不能为1、2、3、8，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01522', '速度等级错误', '检查对象：含“未定义交通区域”形态的link；
检查原则：限速类型为“普通”时，该link上的限速等级不能为1、2、3、4、8，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01525', '速度等级与code_type值不匹配', '1.link的速度等级为7，且该link上道路名的code_type（国家编号）值为1或7时，报log
2.link的速度等级为8，且该link上道路名的code_type（国家编号）值为1,2,3或7时，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01532', '收费有误', '收费道路如果不包含全封闭属性，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01534', '形态', '等级为7级以上，属性不包含sa和pa的道路上存在poi连接路时,报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01543', '道路名空值检查', 'link道路名称中的名称组号码(RD_LINK_NAME.NAME_GROUPID)不能为0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01544', '道路名重复检查', '同一根link上不同的道路名称中不允许有相同的NAME值', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01557', '线限速值检查', '检查对象：RD_LINK_SPEEDLIMIT
检查原则：线限速值顺向限速FROM_LINK_SPEEDLIMIT或逆向限速TO_LINK_SPEEDLIMIT不为5的倍数，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01575', '折角检查', 'Link的某个折角偏小
（将检查GLM01001中log为“Link的某个折角偏小”的检查规则单独拿出来作为一个检查项）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01576', '限速条件检查', '检查对象：制作了限速类型为3（特定条件）的道路
检查原则：限速条件只能是1（雨）、2（雪）、3（雾）、10（时间段）、12（季节时段）、6（学校），否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM01581', '未知', '检查对象：交叉口内link
检查原则：一个路口包含一组或多组CRFI，其中CRFI中的link数量>2,那么该CRFI中的link种别应一致，否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03001', '点挂接多link', '道路Node的接续link数必须小于等于7', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03003', '路上点错误', '种别属性为“路上点（收费站）”时，报err。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03038', '死点', '点的所有接续link，排除掉link上限制信息为施工中不开放及未供用后，剩余的接续link数目＞0，则通行方向不应都是出去或者进来的方向。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03045', '孤立点', '道路点的接续link数必须大于0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03046', '铁路道口点挂接检查', '道路中“铁路道口点”属性的点，与铁路做了同一Node关系的点上挂接link数必须等于2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03047', '特殊种别属性', ' NODE点种别和形态的匹配', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03053', '异常点JCT', '点属性不为“JCT”的点，如果至少挂接2条第一官方名称不相同的高速或城高的本线（NDB即上下线分离，NIDB即CA并且非IC，JCT，SAPA，无属性），那么报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03054', '障碍物检查', '具有障碍物属性的点在路口上，报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03055', '障碍物检查', '具有障碍物属性的点所挂接的link上具有步行街属性，报err；（挂接的多条link具有步行街属性，只报一次）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03056', '障碍物检查', '具有障碍物属性的点只能挂接2条link，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03058', '障碍物检查', '检查对象：障碍物属性Node点
检查原则：该Node上若挂接了10级路，报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03059', '障碍物检查', '检查对象：障碍物属性Node点上，仅挂接有2根link。
检查原则：该Node上挂接的2根link不能同时为高速或城高，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03061', '异常桥属性点', '有平面交叉点“桥”属性的点的挂接桥link的个数必须大于0', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03062', '异常桥属性点', '一个点上挂接的link有且只有一条桥属性的link，那么该点应为平面交叉点（桥）（图廓点除外）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03064', '异常隧道属性点', '除图廓点以外,如果点只挂接了一条“隧道”属性的link，那么该点必须“平面交叉点（隧道）”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03066', '异常隧道属性点', '如果点是隧道属性点，那么挂接的隧道属性link不能等于0；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03067', '异常隧道属性点', '挂接隧道link数等于2时，如果还挂接了非隧道属性的link，报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03069', '异常图廓点', '图廓点只能挂接两条不同图幅的Link，即只能有两条Link以该图廓点为起终点，且Link属于不同的图幅', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03070', '异常图廓点', '同一图幅内图廓点的挂接link数应该等于1', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03077', '异常点JCT', 'Node的“JCT”形态不能与“隧道”、“桥”形态共存', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03078', '异常大门点', '制作有大门信息的Node必须是平面交叉点（无属性），否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03080', 'BUA打断速度限制', '一个点上仅挂接两条道路并且道路等级相同，至少一条Link的速度来源是未调查，若两条道路的城市内道路标识和速度限制值都不相同，报log。
特殊说明：
判断速度限制值（限速类型相同）是否相同时考虑方向，即判断两条Link同侧的速度限制值是否相同', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03082', '特殊种别属性', 'Node具有“门牌号码点”形态时，种别必须为“路上点”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03087', '障碍物检查', '障碍物点的挂接link如果满足以下条件之一，报log
（1）公交车专用道
（2）步行街
（3）时间段步行街（加了时间段禁止穿行，并且车辆类型限制同时且只制作了步行者、配送卡车、急救车的link）不能作为永久交限的进入、退出线
（4）出租车专用道（加了永久禁止穿行，并且车辆类型同时且只制作了允许出租车、急救车和行人的link）
（5）卡车专用道（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许配送卡车、运输卡车、急救车和行人的link）
（6）进不去出不来的link（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许步行者、急救车的link）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03089', '形态共存检查', 'CRFInfo（RD_NODE_FORM.FORM_OF_WAY=3）不能和其它形态共存', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03090', '形态共存检查', '门牌号码点（RD_NODE_FORM.FORM_OF_WAY=16）不能和其它形态共存', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03091', '形态共存检查', '同一个NODE点不能同时存在桥（12）和隧道（13）两种形态', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03092', '形态共存检查', '同一个NODE点不能同时存在桥（12）和图廓点（2）两种形态', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03093', '形态共存检查', '同一个NODE点不能同时存在图廓点（2）和隧道（13）两种形态', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03094', '形态值域检查', 'NODE点形态不能为：未调查（0）、收费站（4）、无人看守铁路道口(32)、有人看守铁路道口(31)、KDZone 与道路交点(41)、幅宽变化点(20)、种别变化点(21)、分隔带变化点(23)、车站(14),否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM03097', '铁路道口形态检查', '道路与铁路制作了同一node，则道路node点必须为铁路道路，否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04002', '大门挂接/方向错误', '大门点的挂接link数必须是2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04005', '大门交限检查', '大门的进入线到退出线不能有禁止交限（货车交限和时间段交限允许）和顺行', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04006', '大门交限检查', '大门的进入线或退出线有一根为10级路或者都为10级路，则大门的“通行对象”只能为“行人”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM04007', '大门交限检查', '大门类型为紧急车辆进入，不允许制作时间段信息', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05019', '分歧点箭头图标识检查', '见附件', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05020', '分歧点箭头图标识检查', '见附件', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05022', '分歧点箭头图标识检查', '检查原则：
1.箭头图标识为“无”，否则报log
区分普通方面、高速方面的方法是：判断进出线、退出线都是高速的为高速方面，其余均为普通方面', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05023', '2D3D虚拟替换删除后图形匹配检查', '检查对象：分歧的箭头图代码。

检查原则：
1.分歧的箭头图代码应在AU_MULTIMEDIA表的NAME字段中存在（若箭头图代码需要替换，则替换后的代码应在多媒体文件表AU_MULTIMEDIA的NAME字段中存在），否则报log

判断箭头图代码是否需要替换：
若箭头图代码的后7位在配置表SC_MODEL_REPDEL_G的“CONV_BEFORE”字段的后7位中存在，则需要进行替换。
1. 若对应的“CONV_OUT”有值，则将箭头图代码的后7位替换为“CONV_OUT”的后7位再在多媒体文件表AU_MULTIMEDIA的NAME字段进行查找。
2. 若对应的“CONV_OUT”无值，不查。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05024', '分歧详细信息检查', '1.分歧类型为“3”或“4”的声音方向只能为“9”，否则报log；
2.分歧类型为“0”、“1”、“2”的声音方向不能为“9”，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05025', '分歧详细信息检查', '1.分歧类型为“3”或“4”的设施类型只能为“9”，否则报log；
2.分歧类型为“0”、“1”、“2”的设施类型不能为“9”，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05026', '分歧详细信息检查', '1.分歧类型为“3”或“4”的名称种别只能为“9”，否则报log；
2.分歧类型为“0”、“1”、“2”的名称种别不能为“9”，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05028', '分歧详细信息检查', '1、分歧类型为“3”的模式图代码只能以8或5开头，否则报log；
2、 分歧类型为“4”的模式图代码只能以7开头，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05029', '分歧详细信息检查', '分歧类型为“3”或“4”的箭头图标志只能为“0”，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05031', '分歧名称检查', '分歧类型为“0”、“3”或“4”时必须无分歧名称，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05033', '分歧详细信息检查', '1.相同进入线、退出线处存在两个单分歧时，以下情况可以共存，其他情况报Log1：
a)分歧类型为1：方面分歧,模式图代码不为空, 分歧退出线含有“HW对象JCT属性”,且与2：IC分歧共存；
b)分歧类型为1:方面分歧，模式图代码为空，与3：3D分歧或4：复杂路口模式图（7开头）共存；
c)分歧类型为 3：3D分歧和4：复杂路口模式图（7开头）共存
2.相同进入线、退出线处存在三个单分歧，以下情况可以共存，其他情况报log2
分歧类别为“1：方面分歧”，模式图代码为空与3：3D分歧和4：复杂路口模式图（7开头）共存
3.相同进入线、退出线处存在三个以上单分歧，报log3', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05037', '分歧挂接Link数检查', '检查对象：分歧、方向看板、实景看板、实景图、连续分歧
检查原则：
1.检查对象进入线的终点必须至少挂接3条Link，否则报log；
2.检查对象退出线的起点必须至少挂接3条Link，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05042', '语言代码检查', '分歧名称的语言代码中:
1.大陆 语言代码必须有CHI，且只能为CHI和ENG，否则报错
3.语言代码为"ENG"时,名称发音 字段应为空', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05048', '分歧类型与模式图匹配检查', '检查对象：模式图代码非空的分歧信息
检查原则：
若模式图代码在SC_MODEL_MATCH_G表的FILE_NAME字段存在，则多媒体文件中对应的TYPE应与分歧类别一致，其中：
1、BRANCH_TYPE为0、1、2，对应SC_MODEL_MATCH_G表的B_TYPE字段值为2D,M_TYPE字段值为pattern
2、BRANCH_TYPE为3，对应SC_MODEL_MATCH_G表的B_TYPE字段值为3D,M_TYPE字段值为pattern
3、BRANCH_TYPE为4，对应SC_MODEL_MATCH_G表的B_TYPE字段值为CRPG,M_TYPE字段值为pattern
否则，报log.', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05049', '分歧模式图位数检查', '1.当分歧类型选择为“0”“2”“3”“4”时，模式图号码一定为8位，否则报错；
2.当分歧类型选择为“1”时，如果进入link、退出link和经过link的道路种别同时为“1：高速公路”或“2：城市高速”时，模式图号码一定为8位，否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05050', '关系类型检查', '分歧（所有分歧类型）的进入和退出link如果都挂接在同一路口或同一Node上，那么关系类型为“路口关系”，反之应为“线线关系”，否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05056', '名称语音的值域检查', '检查对象：RD_BRANCH_NAME表中LANG_CODE为CHI或CHT的记录
检查原则：
1.VOICE_FILE字段中不能为空值，否则报LOG
2.VOICE_FILE字段中只能存在半角英文字母、半角数字“1”“2”“3”“4”和半角字符“''”，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05070', '名称语音一致性检查', '同一省份，分歧中文名称、名称发音、编号类型均相同，但名称语音不同。将名称语音不在语音列表(SC_VOICE_file)中的，报出log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05071', '名称语音一致性检查', '检查对象：RD_BRANCH_NAME表中VOICE_FILE  
检查原则：
分歧名称以G、S、Y、X、C、Z开头，后面+“全部数字”或“字母+数字组合”中任意一种形式的分歧名称，名称语音遵守以下规则
1） 分歧名称中的CODE_TYPE(编号类型)为 4国家高速编号或 10省级高速编号时，字母不变，数字转小写拼音
例： Ｇ１０１ 名称语音为： Gyilingyi
2）分歧名称中的CODE_TYPE(编号类型)=“5（国道编号）、6（省道编号）、7（县道编号）、8（乡道编号）或9（专道编号）”时，去掉首字母，其余的字母不变，数字转拼音，末尾为编号类型的拼音。转化后，名称语音的首字母应为大写
例如：Ｇ１０１    名称语音为：Yilingyiguodao
      Ｓ１１７    名称语音为: Yiyiqishengdao
      Ｙ００１    名称语音为: Linglingyixiangdao
      Ｘ００７    名称语音为: Linglingqixiandao
      Ｃ１１６    名称语音为: Yiyiliucundao
      Ｚ５８６    名称语音为: Wubaliuzhuanyongdao
      ＳＬ６６    名称语音为：Lliuliushengdao
否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05072', '名称语音一致性检查', '检查对象：RD_BRANCH_NAME表中VOICE_FILE  
检查原则：
VOICE_FILE 字段的位数必须大于3位，否则报LOG
例如分歧名称为G2，名称语音为Ger，则报出', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05075', '3D专用模式图检查', '制作了调头专用模式图（80000001）的点，沿此端点的非交叉口LINK退出线（退出线不包含提右或提左属性）方向查找60米以内（包括60米）的所有点（点集合I），同时沿另一个端点B的进入线方向查找120米之内的所有点（点集合II），检查集合I中是否有点和集合II中的点构成复合路口（单独调头口的复合路口除外）。
1.若是，且该复合路口挂接有大于等于3条非交叉口内Link，不报LOG；
2.除1以外的其他情况，直接报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05077', '分歧名称检查', 'RD_BRANCH_DETAIL表中BRANCH_TYPE为1：方面分歧，RD_BRANCH_NAME中“NAME”不能重复，否则报og
RD_BRANCH_DETAIL表中BRANCH_TYPE为2：IC分歧，RD_BRANCH_NAME中“NAME”不能重复，否则报og', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05082', '复杂路口模式图检查', '1.分歧类型为“4：复杂路口模式图”时，进入link必须为7级及以上的普通道路，不可为高速、城市高速，否则报log，退出道路不可为9级和10级，否则报log2.制作了3分叉的复杂路口模式图（模式图号为70100040,70100400,70000040,79100400,71000040）应满足以下任意条件，否则报log2-1.只能挂接3条退出线（不考虑交叉口内link），且挂接在同一个点上，否则报log，并且制作了3条相同进入线，不同退出线的3d记录;2-2.箭头图编号第一位只能是“c”,"e","d";且同一进入link的关系中第一位不能出现重复。2-3.箭头图号为“c0100040”的退出link必须在中间，其左侧箭头图号首位为“d”，右侧箭头图首位为：“e”，否则报log2-4.箭头图号为“c0100400”、“c9100400”的退出link必须在最左侧，其右侧相邻的箭头图首位为“e”，最右侧箭头图首位为“d”，否则报log2-5.箭头图号为“c0000040”、“c1000040”的退出link必须在最右侧，其左侧相邻的箭头图首位为“e”，最左侧箭头图首位为“d”，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05094', '出口编号退出link属性检查', '分歧中若存在出口编号且其退出link包含有上下线分离属性时，则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05097', '分歧进入link', '分歧类型是IC的分歧，其进入link一定是高速本线（NBT本线），否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05104', '分歧名称中0和〇检查', '检查对象：分歧名称
检查原则：
1.汉字数字“〇”名称中包含阿拉伯数字“0”，如二0三北路应为二〇三北路
2.阿拉伯数字“0”名称中包含汉字数字“〇”，如G1〇9应为G109', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM05105', '名称语音一致性检查', '查找分歧的中文名称、名称发音、名称语音，与元数据语音列表(SC_VOICE_file)进行对比，如名称相同，发音相同，但名称语音不同，则报出log
说明：如字母+数字组合的名称，名称相同，但发音能找到多个，则只要SC_VOICE_file中有任意一个发音与数据名称发音相同即可。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07001', '红绿灯控制道路检查', '1.有红绿灯的路口，所有的进入link（双向通行link视为进入link）必须制作有红绿灯控制信息（即受红绿灯控制或者不受红绿灯控制），否则报log1；同时，所有退出link必须未制作有红绿灯控制信息，否则报log2；
2，如果一个路口中的进入link制作有红绿灯控制信息，那么该路口一定有红绿灯，否则报log3；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07004', '信号灯受控检查', '如果一个link的受控标志为“受控制”，那么该link必须为一个路口的接续link且方向为进入该路口，否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM07007', '信号灯位置检查', '“控制标志”为“不受控制”，则“信号灯位置”应该为未调查', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08004', '错误的交限进入线或退出线', '原则：
满足以下条件之一的link不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线（也查线线经过线，但不查线线经过线为交叉口内link的情况）
（１）公交车专用道
（２）步行街
（３）时间段步行街（加了时间段禁止穿行，并且车辆类型限制同时且只制作了步行者、配送卡车、急救车的link）不能作为永久交限的进入、退出线
（４）时间段交限与时间段步行街的限制时间段有重合的link不能作为路口（包括线线）时间段交限的进入、退出线；
（５）出租车专用道（加了永久禁止穿行，并且车辆类型同时且只制作了允许出租车、急救车和行人的link）
（６）卡车专用道（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许配送卡车、运输卡车、急救车和行人的link）
（７）进不去出不来的link（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许步行者、急救车的link）
（８）车辆类型限制的景区内部道路的LINK（加了永久禁止穿行，并且车辆类型限制同时且只制作了允许配送卡车、步行者、急救车的LINK）', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08005', '交限车辆类型检查', '路口所有的交限都不允许有车辆类型，否则报log1。
车辆类型为“运输卡车”时，屏蔽该log；
当车辆类型为“运输卡车”时，交限标志必须为“实地交限”，否则报log2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08009', '交限与交限重叠', '一组线线交限（普通非时间段交限）的所有link完全包含在另一组线线交限（普通非时间段交限）的所有link中，报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08010', '交限与交限重叠', '一组线线交限（普通时间段交限）的所有link完全包含在另一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08011', '交限与交限重叠', '一组线线交限（普通时间段交限）的所有link完全包含在另一组线线交限（普通非时间段交限）的所有link中，报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08012', '交限与交限重叠', '一组线线交限（普通非时间段交限）的所有link完全包含在另一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08013', '交限与交限重叠', '路口交限（普通非时间段交限）的所有link完全包含在一组线线交限（普通非时间段交限）的所有link中，报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08016', '交限与交限重叠', '路口交限（普通非时间段交限）的所有link完全包含在一组线线交限（普通时间段交限）的所有link中，报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08025', '连接路属性车信检查', 'form含有“提前右转”的道路，若道路的端点属于一个复合路口，则不能做为直行或调头或左转的线点线车道信息的进入线，否则报log；
form含有“提前右转”的道路连接路，若道路的端点是单路口，则不能做为左转的线点线车道信息的进入线，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08031', '路口交限有经过线检查', '路口交限里不允许有经过线信息', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08036', '交限详细信息', 'RD_RESTRICTION_DETAIL表中交限号码相同的多个RESTRIC_INFO字段的合，应与RD_RESTRICTION表中对应交限号码的RESTRIC_INFO字段值相同', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08038', '交限挂接Link数检查', '检查对象：普通交限信息
检查原则：
1.检查对象进入线的终点必须至少挂接3条Link，否则报log；
2.检查对象退出线的起点必须至少挂接3条Link，否则报log；
特殊说明：
1.若检查对象的进入线的终点与退出线的起点挂接在同一路口，则不报错；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08039', '交限类型检查', '交限的限制类型不能为“未调查”否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08040', '交限类型检查', '交限的限制类型为“时间段禁止”时，交通限制信息中的时间段不能为空。否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08042', '关系类型检查', '交限的进入和退出link如果都挂接在同一路口，那么关系类型为“路口关系”，反之应为“线线关系”，否则报错
说明：交限LINK跨过两个及以上图幅，应制作“线线关系”', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08043', '经过线距离检查', '交限经过线（过滤掉经过线中含环岛属性的link）应小于等于10条，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08044', '同一位置多组交限检查', '1.相同进入线，相同退出线，不同经过线的多组交限，交限号码不应相同，否则报log；
2.相同进入线相同进入点，不同退出线的多组普通交限，交限号码应相同，否则报log；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08045', '交限与顺行矛盾检查', '普通交限与顺行有相同的进入link和退出link，程序报log。说明：时间段交限与顺行可以共存', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08046', '交限车辆类型检查', '交限中的车辆类型为0时，车辆限重必须也为0，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM08049', '交限与交叉口方向矛盾检查', '路口交限的进入线到退出线无通路可行，即报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09003', '警示信息错误检查', '警示信息进入LINK在驶入警示信息所在Node的方向上可通行，否则报错', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09004', '警示信息错误检查', '以下互相矛盾的警示信息不能添加在同一进入link和Node上：
1.“向左急弯路”与“向右急弯路”；
2.“上陡坡”与“下陡坡”；
3.“两侧变窄”与“左侧变窄”；
4.“两侧变窄”与“右侧变窄”；
5.“左侧变窄”与“右侧变窄”；
5.“有人看守铁路道口”与“无人看守铁路道口”；
6.“禁止超车”与“解除禁止超车”；
7.“左右绕行”与“左侧绕行”；
8.“左右绕行”与“右侧绕行”；
9.“左侧绕行”与“右侧绕行”；
10.“注意落石（左）”与“注意落石（右）”；
11.“反向弯路（左）”与“反向弯路（右）”；
12.“傍山险路（左）”与“傍山险路（右）”；
13.“堤坝路（左）”与“堤坝路（右）” ；', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09005', '警示信息错误检查', '信息类型为“通用警示”的危险信息，文字描述为空报Log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09007', '警示信息错误检查', '警示信息Node点若挂接的进入线和退出线全部为高速或者城市高速，则该点上仅能挂接2条link，否则报log1；若Node点仅制作有“左侧汇入右侧合流（14401）”、“右侧汇入左侧合流（14402）”、“减速让行（20201）”、“停车让行(20101)”，不报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09009', '空的警示信息检查', '警示信息标牌类型不能为空，否则报log。log包括警示信息ID。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09010', '警示信息错误检查', '警示信息类型“停车让行”不允许制作在高速本线和直连IC和直连JCT上，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09011', '警示信息错误检查', '警示信息类型为“停车让行”时，其“长度”或“预告距离”应为0，否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09012', '警示信息错误检查', '在一组警示信息的线点关系中，如果线具有交叉口link属性，则其警示信息类型只能是“停车让行”或者“减速让行”,否则报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09014', '值域检查', '只能存在以下警示信息，否则报log
1，向左急弯路 10201
2，向右急弯路 10202
3，反向弯路 10301
4，连续弯路 10302
5，上陡坡 10501
6，下陡坡 10502
7，两侧变窄 10701
8，右侧变窄 10702
9，左侧变窄 10703
10，窄桥 10801
11，双向交通 10901
12，注意儿童 11101
13，注意牲畜 11201
14，注意横风 11601
15，易滑 11701
16，村庄 12001
17，驼峰桥 12301
18，路面不平 12401
19，过水路面 12701
20，有人看守铁路道口 12801
21，无人看守铁路道口 12901
22，注意危险 13701
23，事故易发路段 13401
24，鸣喇叭 31501
25，禁止超车 22901
26，解除禁止超车 23001
27，堤坝路（左、右） "11901
"
， 11902
28，傍山险路（左、右） 11801
， 11802
29，注意落石（左、右） 11501
， 11502
30，左右绕行 13601
31，左侧绕行 13602
32，右侧绕行 13603
33，连续下坡 10601
34，通用警示 13702
35，注意左侧合流 14401
36，注意右侧合流 14402
37，会车让行 20301
38，路面低洼 12601
39，减速让行 20201
40，隧道开灯 14001
41，潮汐车道 14101
42，停车让行 20101
43，路面高突 12501
44，交通意外黑点 13703', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09015', '异常图廓点检查', '警示信息中的点形态不能是图廓点，否则报错。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09016', '警示信息错误检查', '停车让行警示信息与减速让行警示信息不允许制作在同一线点关系上，否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09017', '警示信息错误检查', '警示信息标牌类型“会车让行”不允许制作在单方向的link上，否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09018', '警示信息错误检查', '同一警示信息中有效距离和预告距离不能同时非0，否则报log。', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09019', '警示信息制作范围检查', '检查原则：警示信息不能够制作在9级辅路上。否则报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM09021', '警示信息错误检查', '检查对象：警示信息表（线点结构）RD_WARNINGINFO
检查原则：相同进入线和进入点的警示信息，若警示信息的“标牌类型（TYPE_CODE）"同时存在“下陡坡（10502）”和“连续下坡标志（10601）”，报LOG', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11006', '进入退出线重复记录', '分歧信息中（包含分歧、连续分歧），不能存在相同进入线，相同退出线，相同经过线的记录', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11007', '逻辑关系错误', '分歧信息中退出线不应为交叉点内link属性', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11015', '出口编号错误', '检查对象：普通道路方面分歧（进入线或退出线任意一根为普通路即为普通道路方面分歧）
检查原则：普通道路方面分歧中不能存在出口编号，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11016', '出口编号错误', '检查对象：IC分歧，其“设施属性”和“名称种别”选项分别为“SA”、“SA”或“PA”、“PA”。
检查原则：检查对象中不能存在出口编号，否则报err', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11024', '进入线或退出线种别错误', '该分歧有分歧模式图号，那么进入线和退出线都必须是高速或城高，否则报Log1；该分歧没有分歧模式图号，那么该分歧应该是方面分歧且进入线是普通道路，否则报log2', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11025', '模式图编号错误', '相同进入线，相同进入点,存在多个分歧类型相同的分歧，模式图编号必须相同
屏蔽对象：
1.如果一组3D分歧中存在两个分歧的箭头图号都以“e”开头，不报log', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11026', '模式图编号错误', '检查对象：BRANCH_TYPE为0,1,2,4的分歧
检查原则：进入线、进入点相同箭头图除第一位外的编号必须相同；相同进入线不同退出线，箭头图编号的第一位不能相同', null, null, 1, 'suite3');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('GLM11028', '模式图编号错误', '对象：高速、城高（同时包括进入线和退出线）的ＩＣ、方面、普通分歧的分歧模式图ＩＤ的第一位
原则：箭头图ＩＤ第一位不是“０”、“１”、“２”，报ｌｏｇ', null, null, 1, 'suite3');

commit;
-- suite4
insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR61107', '拼音格式检查', 'SC_TOLLGATE_NAMECK(PY)', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR61109', '拼音格式检查', 'SC_BUA_NAMECK(PY)', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR63710', '中文名称', 'SC_TOLLGATE_NAMECK（NAME_CHI）', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR63713', '中文名称', 'SC_BUA_NAMECK（NAME_CHI）', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71030', '英文格式检查', 'RD_BRANCH_NAME ( NAME )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71032', '英文格式检查', 'RD_CROSS_NAME ( NAME )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71034', '英文格式检查', 'RD_NODE_NAME ( NAME )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71035', '英文格式检查', 'RD_SIGNBOARD_NAME ( NAME )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71037', '英文格式检查', 'RD_TOLLGATE_NAME ( NAME )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71044', '英文格式检查', 'SC_INTERSECTION_NAMECK( NAME_ENG)', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71091', '英文格式检查', 'SC_TOLLGATE_NAMECK(NAME_ENG)', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CHR71092', '英文格式检查', 'SC_BUA_NAMECK(NAME_ENG)', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM01001', '主键约束', '主键有效性检查', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM01002', '外键约束', '外键关联检查', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM01003', '值域约束', '值域约束检查', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10003', 'RD_GATE_CONDITION', 'RD_GATE_CONDITION ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10005', 'RD_LINK_LIMIT', 'RD_LINK_LIMIT ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10006', 'RD_LINK_SPEEDLIMIT', 'RD_LINK_SPEEDLIMIT ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10007', 'RD_RESTRICTION_COND', 'RD_RESTRICTION_CONDITION ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10011', 'RD_WARNINGINFO', 'RD_WARNINGINFO ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10012', 'RD_SPEEDLIMIT', 'RD_SPEEDLIMIT ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM10013', 'RD_LINK_LIMIT_TRUCK', 'RD_LINK_LIMIT_TRUCK ( TIME_DOMAIN )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11003', 'RD_GATE_CONDITION', 'RD_GATE_CONDITION( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11005', 'RD_LINK_LIMIT', 'RD_LINK_LIMIT( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11006', 'RD_LINK_SPEEDLIMIT', 'RD_LINK_SPEEDLIMIT ( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11007', 'RD_RESTRICTION_CONDITION', 'RD_RESTRICTION_CONDITION( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11011', 'RD_WARNINGINFO', 'RD_WARNINGINFO( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11012', 'RD_SPEEDLIMIT', 'RD_SPEEDLIMIT ( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM11013', 'RD_LINK_LIMIT_TRUCK', 'RD_LINK_LIMIT_TRUCK ( TIME_DOMAIN )只允许制作精确时间，不允许制作模糊时间', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20002', '未知', 'RD_GSC_LINK中，字段LINK_PID的值必须存在于RD_LINK
RW_LINK
LC_LINK
CMG_BUILDLINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20008', '未知', 'key in RD_SERIESBRANCH(ARROW_CODE) must be found in SC_MODEL_MATCH_G(FILE_NAME) table', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20009', '未知', 'key in RD_SIGNBOARD(ARROW_CODE) must be found in SC_MODEL_MATCH_G(FILE_NAME) table', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20030', 'RDBRANCH_DETAIL', 'RD_BRANCH_DETAIL中，字段BRANCH_PID的值必须存在于RD_BRANCH(BRANCH_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20031', 'RDBRANCH_REALIMAGE', 'RD_BRANCH_REALIMAGE中，字段BRANCH_PID的值必须存在于RD_BRANCH(BRANCH_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20032', 'RDBRANCH_VIALINKS', 'RD_BRANCH_VIA中，字段BRANCH_PID的值必须存在于RD_BRANCH(BRANCH_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20033', 'RDSERIESBRANCH', 'RD_SERIESBRANCH中，字段BRANCH_PID的值必须存在于RD_BRANCH(BRANCH_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20034', 'RDBRANCH_SIGNASREAL', 'RD_SIGNASREAL中，字段BRANCH_PID的值必须存在于RD_BRANCH(BRANCH_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20035', 'RDBRANCH_SIGNBOARD', 'RD_SIGNBOARD中，字段BRANCH_PID的值必须存在于RD_BRANCH(BRANCH_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20048', 'RDLANE_TOPOLOGY', 'RD_LANE_TOPOLOGY中，字段CONNEXITY_PID的值必须存在于RD_LANE_CONNEXITY(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20051', 'RDBRANCH_NAME', 'RD_BRANCH_NAME中，字段DETAIL_ID的值必须存在于RD_BRANCH_DETAIL(DETAIL_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20052', 'RDRESTRICCONDITION_D', 'RD_RESTRICTION_CONDITION中，字段DETAIL_ID的值必须存在于RD_RESTRICTION_DETAIL(DETAIL_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20053', 'RDRESTRICVIALINK_DET', 'RD_RESTRICTION_VIA中，字段DETAIL_ID的值必须存在于RD_RESTRICTION_DETAIL(DETAIL_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20067', 'RDLINK_ENODE', 'RD_LINK中，字段E_NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20105', 'RDMAINSIDE', 'RD_MAINSIDE_LINK中，字段GROUP_ID的值必须存在于RD_MAINSIDE(GROUP_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20106', 'RDMULTIDIGITIZED', 'RD_MULTIDIGITIZED_LINK中，字段GROUP_ID的值必须存在于RD_MULTIDIGITIZED(GROUP_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20109', 'RDTMCLOCATION_LINKS', 'RD_TMCLOCATION_LINK中，字段GROUP_ID的值必须存在于RD_TMCLOCATION(GROUP_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20393', 'RDNODE_FORM', 'RD_NODE_FORM中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20394', 'RDNODE_MESH', 'RD_NODE_MESH中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20395', 'RDNODE_NAME', 'RD_NODE_NAME中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20397', 'RDRESTRICTION_NODE', 'RD_RESTRICTION中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20401', 'RDTOLLGATE_NODE', 'RD_TOLLGATE中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20402', 'RDSIGNAL_NODE', 'RD_TRAFFICSIGNAL中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20405', 'RDWARNING_NODE', 'RD_WARNINGINFO中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20411', 'RDBRANCH_OUTLINK', 'RD_BRANCH中，字段OUT_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20413', 'RDGATE_OUTLINK', 'RD_GATE中，字段OUT_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20415', 'RDLANEVTOPOLOGY_OUTL', 'RD_LANE_TOPOLOGY中，字段OUT_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20416', 'RDRESTRICTDETAIL_OUT', 'RD_RESTRICTION_DETAIL中，字段OUT_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20418', 'RDTOLLGATE_OUTLINK', 'RD_TOLLGATE中，字段OUT_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20421', 'RDTOLLGATE_COST_OUT', 'RD_TOLLGATE_COST中，字段OUT_TOLLGATE的值必须存在于RD_TOLLGATE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20426', '未知', 'key in RD_SERIESBRANCH(PATTERN_CODE) must be found in SC_MODEL_MATCH_G(FILE_NAME) table', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20481', '未知', 'RD_GATE中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20482', '未知', 'RD_BRANCH_DETAIL中，字段PATTERN_CODE的值必须存在于SC_MODEL_MATCH_G(FILE_NAME)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20483', '未知', 'RD_BRANCH_REALIMAGE中，字段REAL_CODE的值必须存在于SC_MODEL_MATCH_G(FILE_NAME)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20485', '未知', 'RD_TOLLGATE中，字段ETC_FIGURE_CODE的值必须存在于SC_MODEL_MATCH_G(FILE_NAME)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20543', '未知', 'key in RD_SIGNBOARD(BACKIMAGE_CODE) must be found in SC_MODEL_MATCH_G(FILE_NAME) table', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20544', '未知', 'key in RD_BRANCH_REALIMAGE(ARROW_CODE) must be found in SC_MODEL_MATCH_G(FILE_NAME) table', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20551', 'LINK信息', 'RD_LINK_ADDRESS中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20568', '收费站', 'RD_TOLLGATE_MAPPING中，字段GDB_TOLL_PID的值必须存在于RD_TOLLGATE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20569', '收费站', 'RD_TOLLGATE_MAPPING中，字段GDB_TOLL_NODEID的值必须存在于RD_TOLLGATE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20570', '收费站', 'RD_TOLLGATE_FEE中，字段S_MAPPINGID的值必须存在于RD_TOLLGATE_MAPPING(MAPPING_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20571', '收费站', 'RD_TOLLGATE_FEE中，字段E_MAPPINGID的值必须存在于RD_TOLLGATE_MAPPING(MAPPING_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20576', '收费站', 'RD_TOLLGATE_MAPPING中，字段GDB_TOLL_NAME的值必须存在于RD_TOLLGATE_NAME(NAME_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20618', 'RD_HGWG_LIMIT', 'RD_HGWG_LIMIT中，字段LINK_PID值必须存在与RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20619', 'RD_LINK_LIMIT_TRUCK', 'RD_LINK_LIMIT_TRUCK中，字段LINK_PID值必须存在与RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20703', 'RD_LINK_INT_RTIC', 'RD_LINK_INT_RTIC中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20707', 'RD_ELECEYE_PART', 'RD_ELECEYE_PART中，字段GROUP_ID的值必须存在于RD_ELECEYE_PAIR(GROUP_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20708', 'RD_ELECEYE_PART', 'RD_ELECEYE_PART中，字段ELECEYE_PID的值必须存在于RD_ELECTRONICEYE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20714', '未知', 'RD_WARNINGINFO中，字段TYPE_CODE的值必须存在于RD_SIGNPOST_CODE(TYPE_CODE)中,当大陆数据RD_WARNINGINFO表中TYPE_CODE=13703时，报log', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM300044', '几何验证', 'RD_LINK ( GEOMETRY )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM300046', '几何验证', 'RD_NODE ( GEOMETRY )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM300059', '几何验证', 'RD_GSC ( GEOMETRY )', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400001', '进入线进入点方向矛盾', '车信信息点必须为进入线通行方向的终点
', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400002', '经过线不连通', '车信相邻的两条经过线必须是可通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400003', '进入线与第一条经过线方向矛盾', '车信进入线和seq_num为1的经过线必须是可以通行的，如果没有经过线则进入线到退出线必须是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400004', '退出线和最后一条经过线方向矛盾', '车信退出线和最后一条经过线必须是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400006', '经过线的SEQ_NUM错误', '车信经过线的SEQ_NUM必须是从1开始的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400007', '经过线的SEQ_NUM错误', '车信经过线的SEQ_NUM必须是连续的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400008', '进入线进入点方向矛盾', '分歧信息点必须为进入线通行方向的终点
', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400009', '经过线不连通', '分歧相邻的两条经过线，从seq_num由小到大的方向是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400010', '进入线与第一条经过线方向矛盾', '分歧进入线和seq_num为1的经过线必须是可以通行的；如果没有经过线则进入线到退出线必须是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400011', '退出线和最后一条经过线方向矛盾', '分歧退出线和最后一条经过线必须是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400013', '经过线的SEQ_NUM错误', '分歧经过线的SEQ_NUM必须是从1开始的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400014', '经过线的SEQ_NUM错误', '分歧经过线的SEQ_NUM必须是连续的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400015', '进入线进入点方向矛盾', '交限信息点必须为进入线通行方向的终点
', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400016', '经过线不连通', '交限相邻的两条经过线，从seq_num由小到大的方向是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400017', '进入线与第一条经过线方向矛盾', '交限进入线和seq_num为1的经过线必须是可以通行的；如果没有经过线则进入线到退出线必须是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400018', '退出线和最后一条经过线方向矛盾', '交限退出线和最后一条经过线必须是可以通行的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400020', '经过线的SEQ_NUM错误', '交限经过线的SEQ_NUM必须是从1开始的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400021', '经过线的SEQ_NUM错误', '交限经过线的SEQ_NUM必须是连续的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400037', '未知', '警示信息中的点和线必须是挂接的', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400040', '未知', '关系类型为路口关系的交限，信息点必须与退出线通行方向的起点挂接在同一个路口上', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400041', '未知', '关系类型为路口关系的车信，信息点必须与退出线通行方向的起点挂接在同一个路口上', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400044', '未知', '如果交限的关系来源为路口，那么不存在经过线信息', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400046', '未知', '如果分歧的关系来源为路口，那么不存在经过线信息', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400047', '未知', '交限的进入线不能与经过线重复', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400048', '未知', '车信的进入线不能与经过线重复', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400051', '未知', '分歧的进入线不能与经过线重复', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400052', '未知', '交限的进入线和退出线不能相同', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400053', '未知', '车信的进入线和退出线不能相同', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400056', '未知', '分歧的进入线和退出线不能相同', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400057', '未知', '收费站的进入线和退出线不能相同', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400058', '未知', '大门的进入线和退出线不能相同', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400060', '未知', '交限的退出线不能与经过线重复', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400061', '未知', '车信的退出线不能与经过线重复', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400064', '未知', '分歧的退出线不能与经过线重复', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400065', '未知', '收费站的退出线必须与信息主点挂接，且信息点必须为退出线通行方向的起点', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400066', '未知', '大门的退出线必须与信息主点挂接，且信息点必须为退出线通行方向的起点', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400068', '未知', '收费站进入线必须与信息主点挂接，且信息点必须为进入线通行方向的终点', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400069', '未知', '大门进入线必须与信息主点挂接，且信息点必须为进入线通行方向的终点', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400071', '未知', '关系类型为路口关系的分歧，信息点必须与退出线通行方向的起点挂接在同一个路口上(路口关系的分歧，若未做在路口则进入线终点与退出线起点应为同一个点)', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400077', '未知', '关系类型为线线关系的分歧，必须要有经过线', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400078', '未知', '关系类型为线线关系的交限，必须要有经过线', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400081', '未知', '车信退出线的两个端点不应都挂接有此车信经过线', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400082', '未知', '交限退出线的两个端点不应都挂接有此交限经过线', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM400083', '未知', '分歧退出线的两个端点不应都挂接有此分歧经过线', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM50001', '坐标一致性', '根据道路点的坐标检验是否与其所在图幅一致', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM50016', '坐标一致性', '根据道路LINK的坐标检验是否与其所在图幅一致', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM50023', 'RD_SPEEDLIMIT', '限速关系表中的mesh_id应当与关联link号码（Link_pid）对应的mesh_id相同
', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM50024', 'RD_ELECTRONICEYE', '电子眼表中的mesh_id应当与关联link号码（Link_pid）对应的mesh_id相同
', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60002', '未知', 'RD_LANE_VIA(TOPOLOGY_ID,GROUP_ID,SEQ_NUM)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60003', '未知', 'RD_LANE_VIA(TOPOLOGY_ID,GROUP_ID,LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60025', '未知', 'RD_RESTRICTION_DETAIL(RESTRIC_PID,OUT_LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60026', '未知', 'RD_LINK_ADDRESS(LINK_PID,NAME_GROUPID,ADDRESS_TYPE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60049', '未知', 'RD_GATE_CONDITION(PID,VALID_OBJ)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60051', '未知', 'RD_TOLLGATE_PASSAGE(PID,SEQ_NUM)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60054', '未知', 'RD_CROSS_NODE(NODE_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60063', '未知', 'RD_TOLLGATE_NAME(PID,LANG_CODE,NAME)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60064', '未知', 'RD_GSC_LINK(PID,LINK_PID,TABLE_NAME,ZLEVEL)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60065', '未知', 'RD_GSC_LINK(PID,LINK_PID,TABLE_NAME,SHP_SEQ_NUM)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60066', '未知', 'RD_CROSS_LINK(LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60071', '未知', 'RD_CROSS_NAME(PID,LANG_CODE,NAME)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60083', '未知', 'RD_NODE_NAME(NODE_PID,LANG_CODE,NAME)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60093', '未知', 'RD_NODE_MESH(NODE_PID,MESH_ID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60097', '未知', 'RD_SPEEDLIMIT(GEOMETRY,LINK_PID,DIRECT,SPEED_FLAG,SPEED_TYPE,SPEED_DEPENDENT)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60099', '未知', 'RD_TRAFFICSIGNAL(NODE_PID,LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60100', '未知', 'RD_NODE_FORM(NODE_PID,FORM_OF_WAY)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60101', '未知', 'RD_SIGNBOARD_NAME(SIGNBOARD_ID,LANG_CODE,NAME)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60113', '未知', 'RD_LINK_NAME(LINK_PID,NAME_CLASS,SEQ_NUM
)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60114', '未知', 'RD_LINK_RTIC(LINK_PID,RTIC_DIR)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60115', '未知', 'RD_LINK_ZONE(LINK_PID,REGION_ID,TYPE,SIDE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60116', '未知', 'RD_WARNINGINFO(LINK_PID,NODE_PID,TYPE_CODE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60117', '未知', 'RD_LINK_NAME(LINK_PID,NAME_GROUPID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60135', '未知', 'RD_LINK_FORM(LINK_PID,FORM_OF_WAY)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60136', '未知', 'RD_LINK_RTIC(LINK_PID,CODE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60137', '未知', 'RD_LINK(S_NODE_PID,E_NODE_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60138', '未知', 'RD_LINK_SIDEWALK(LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60139', '未知', 'RD_LINK_SPEEDLIMIT(LINK_PID,SPEED_TYPE,SPEED_DEPENDENT)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60141', '未知', 'RD_LINK_WALKSTAIR(LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60143', '未知', 'RD_TOLLGATE_COST(IN_TOLLGATE,OUT_TOLLGATE,VEHICLE_CLASS)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60144', '未知', 'RD_GATE(IN_LINK_PID,NODE_PID,OUT_LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60146', '未知', 'RD_TOLLGATE(IN_LINK_PID,NODE_PID,OUT_LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60148', '未知', 'RD_LANE_CONNEXITY(IN_LINK_PID,NODE_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60149', '未知', 'RD_RESTRICTION(IN_LINK_PID,NODE_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60154', '未知', 'RD_TMCLOCATION_LINK(GROUP_ID,LINK_PID, LOC_DIRECT)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60155', '未知', 'RD_TMCLOCATION_LINK(GROUP_ID,LINK_PID, DIRECT)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60169', '未知', 'RD_GSC(GEOMETRY.SDO_POINT.X, GEOMETRY.SDO_POINT.Y)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60193', '未知', 'RD_RESTRICTION_CONDITION(DETAIL_ID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60195', '未知', 'RD_BRANCH_NAME(DETAIL_ID,LANG_CODE,NAME)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60198', '未知', 'RD_RESTRICTION_VIA(DETAIL_ID,GROUP_ID,SEQ_NUM)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60199', '未知', 'RD_RESTRICTION_VIA(DETAIL_ID,GROUP_ID,LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60201', '未知', 'RD_LANE_TOPOLOGY(CONNEXITY_PID,OUT_LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60209', '未知', 'RD_BRANCH_VIA(BRANCH_PID,GROUP_ID,SEQ_NUM)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60210', '未知', 'RD_BRANCH_VIA(BRANCH_PID,GROUP_ID,LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60212', '未知', 'RD_BRANCH_DETAIL(BRANCH_PID,BRANCH_TYPE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60213', '未知', 'RD_SERIESBRANCH(BRANCH_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60214', '未知', 'RD_SIGNASREAL(BRANCH_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60215', '未知', 'RD_SIGNBOARD(BRANCH_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60216', '未知', 'RD_BRANCH_REALIMAGE(ARROW_CODE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60238', '未知', 'RD_BRANCH_NAME(DETAIL_ID,SEQ_NUM，LANG_CODE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60239', '未知', 'RD_BRANCH_NAME(DETAIL_ID,NAME_GROUPID，LANG_CODE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60241', '未知', 'RD_BRANCH(IN_LINK_PID,NODE_PID,OUT_LINK_PID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60243', '未知', 'RD_LINK_RTIC(LINK_PID,UPDOWN_FLAG)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60254', '未知', 'RD_LINK_LIMIT_TRUCK(LINK_PID, LIMIT_DIR)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60285', '未知', 'RD_BRANCH_REALIMAGE(BRANCH_PID，IMAGE_TYPE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60312', '未知', 'RD_ELECTRONICEYE(GEOMETRY,LINK_PID,DIRECT,LOCATION,KIND)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60318', '未知', 'RD_LINK_INT_RTIC(LINK_PID,RTIC_DIR)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60319', '未知', 'RD_LINK_INT_RTIC(LINK_PID,CODE)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60320', '未知', 'RD_LINK_INT_RTIC(LINK_PID,UPDOWN_FLAG)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM60321', '未知', 'RD_ELECEYE_PART(ELECEYE_PID,GROUP_ID)应唯一', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70054', 'RD_NODE', 'RD_NODE的主键NODE_PID必须在RD_NODE_MESH的NODE_PID中存在，且图廓点有两个图幅号，角点有4个图幅号', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70055', 'RD_NODE', 'RD_NODE的主键NODE_PID必须在RD_NODE_FORM的NODE_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70056', 'RD_NODE', 'RD_NODE的主键NODE_PID必须在RD_LINK的S_NODE_PID
E_NODE_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70057', 'RD_LINK', 'RD_LINK的主键LINK_PID必须在RD_LINK_FORM的LINK_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70058', 'RD_LINK', 'RD_LINK的主键LINK_PID必须在RD_LINK_SPEEDLIMIT的LINK_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70059', 'RD_RESTRICTION', 'RD_RESTRICTION的主键PID必须在RD_RESTRICTION_DETAIL的RESTRIC_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70061', 'RD_LANE_CONNEXITY', 'RD_LANE_CONNEXITY的主键PID必须在RD_LANE_TOPOLOGY的CONNEXITY_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70064', 'RD_BRANCH', 'RD_BRANCH的主键BRANCH_PID必须在RD_BRANCH_DETAIL
RD_BRANCH_REALIMAGE
RD_SIGNBOARD
RD_SERIESBRANCH
RD_SIGNASREAL
RD_BRANCH_SCHEMATIC
的BRANCH_PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70072', 'RD_GSC', 'RD_GSC的主键PID必须在RD_GSC_LINK的PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70074', 'RD_CROSS', 'RD_CROSS的主键PID必须在RD_CROSS_NODE的PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70112', 'RD_GATE', 'RD_GATE的主键 PID 必须在 RD_GATE_CONDITION的 PID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM70113', 'RD_ELECEYE_PAIR', 'RD_ELECEYE_PAIR的主键GROUP_ID必须在RD_ELECEYE_PART的GROUP_ID中存在', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM80013', 'RD_LINK ', 'RD_LINK中表中，WIDTH字段中不允许存在非法值', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20117', 'RDCROSS_LINKS', 'RD_CROSS_LINK中，字段PID的值必须存在于RD_CROSS(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20118', 'RDCROSS_NAME', 'RD_CROSS_NAME中，字段PID的值必须存在于RD_CROSS(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20119', 'RDCROSS_NODES', 'RD_CROSS_NODE中，字段PID的值必须存在于RD_CROSS(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20123', 'RDGATE_CONDITION', 'RD_GATE_CONDITION中，字段PID的值必须存在于RD_GATE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20124', 'RDGSC_LINK', 'RD_GSC_LINK中，字段PID的值必须存在于RD_GSC(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20133', 'RDTOLLGTE_NAME', 'RD_TOLLGATE_NAME中，字段PID的值必须存在于RD_TOLLGATE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20134', 'RDTOLLGATE_PASSAGE', 'RD_TOLLGATE_PASSAGE中，字段PID的值必须存在于RD_TOLLGATE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20167', '未知', 'RD_LINK_ZONE中，字段REGION_ID的值必须存在于AD_ADMIN(REGION_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20172', 'RDRESTRIC_DETAIL', 'RD_RESTRICTION_DETAIL中，字段RESTRIC_PID的值必须存在于RD_RESTRICTION(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20173', '未知', 'RD_LINK中，字段RIGHT_REGION_ID的值必须存在于AD_ADMIN(REGION_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20187', 'RDLINK_SNODE', 'RD_LINK中，字段S_NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20193', 'RDSIGNBOARD_NAME', 'RD_SIGNBOARD_NAME中，字段SIGNBOARD_ID的值必须存在于RD_SIGNBOARD(SIGNBOARD_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20202', '未知', 'RD_TMCLOCATION中，字段TMC_ID的值必须存在于TMC_POINT(TMC_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20207', 'RDLANETOPOLOGY_VIALI', 'RD_LANE_VIA中，字段TOPOLOGY_ID的值必须存在于RD_LANE_TOPOLOGY(TOPOLOGY_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20208', '未知', 'RD_WARNINGINFO中，字段TYPE_CODE的值必须存在于RD_SIGNPOST_CODE(TYPE_CODE)中，当港澳数据RD_WARNINGINFO表中TYPE_CODE=13703时，不报log', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20220', 'RDBRANCH_INLINK', 'RD_BRANCH中，字段IN_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20222', 'RDGATE_INLINK', 'RD_GATE中，字段IN_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20223', 'RDLANE_INLINK', 'RD_LANE_CONNEXITY中，字段IN_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20225', 'RDRESTRICT_INLINK', 'RD_RESTRICTION中，字段IN_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20227', 'RDTOLLGATE_INLINK', 'RD_TOLLGATE中，字段IN_LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20230', 'RDTOLLGATE_COST_IN', 'RD_TOLLGATE_COST中，字段IN_TOLLGATE的值必须存在于RD_TOLLGATE(PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20233', '未知', 'RD_LINK中，字段LEFT_REGION_ID的值必须存在于AD_ADMIN(REGION_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20261', 'RDBRANCH_VIALINK', 'RD_BRANCH_VIA中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20263', 'RDCROSS_LINK', 'RD_CROSS_LINK中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20266', 'RDELECTRONIC_LINK', 'RD_ELECTRONICEYE中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20269', 'RDLANE_VIALINK', 'RD_LANE_VIA中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20270', 'RDLINK_FORM', 'RD_LINK_FORM中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20271', 'RDLINK_LIMIT', 'RD_LINK_LIMIT中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20272', 'RDLINK_NAMES', 'RD_LINK_NAME中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20273', 'RDLINK_RTICS', 'RD_LINK_RTIC中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20274', 'RDLINK_SIDEWALK', 'RD_LINK_SIDEWALK中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20275', 'RDLINK_SPEEDLIMIT', 'RD_LINK_SPEEDLIMIT中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20277', 'RDLINK_STAIR', 'RD_LINK_WALKSTAIR中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20278', 'RDLINK_ZONES', 'RD_LINK_ZONE中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20279', 'RDMAINSIDE_LINK', 'RD_MAINSIDE_LINK中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20280', 'RDMULTIDIGITIZED_LIN', 'RD_MULTIDIGITIZED_LINK中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20282', 'RDRESTRICTION_VIALIN', 'RD_RESTRICTION_VIA中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20286', 'RDSPEEDLIMIT_LINK', 'RD_SPEEDLIMIT中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20287', 'RDTMCLOCATION_LINK', 'RD_TMCLOCATION_LINK中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20288', 'RDSIGNAL_LINK', 'RD_TRAFFICSIGNAL中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20290', 'RDWARNING_LINK', 'RD_WARNINGINFO中，字段LINK_PID的值必须存在于RD_LINK(LINK_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20323', '未知', 'RD_NODE_MESH中，字段MESH_ID的值必须存在于NI_ADMIN_MESH(MESHNUM)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20355', '未知', 'RD_LINK中，字段MESH_ID的值必须存在于NI_ADMIN_MESH(MESHNUM)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20363', '未知', 'RD_LINK_NAME中，字段NAME_GROUPID 的值必须存在于RD_NAME(NAME_GROUPID )中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20369', 'RDBRANCHNAME_TONE', 'RD_BRANCH_NAME_TONE中，字段NAME_ID的值必须存在于RD_BRANCH_NAME(NAME_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20372', 'RDSIGNBOARDNAME_TONE', 'RD_SIGNBOARD_NAME_TONE中，字段NAME_ID的值必须存在于RD_SIGNBOARD_NAME(NAME_ID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20386', 'RDBRANCH_NODE', 'RD_BRANCH中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20387', 'RDCROSS_NODE', 'RD_CROSS_NODE中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('COM20392', 'RDLANE_NODE', 'RD_LANE_CONNEXITY中，字段NODE_PID的值必须存在于RD_NODE(NODE_PID)中', null, null, 1, 'suite4');

commit;
-- suite5
insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CONN51001', '物理连通性检查', '物理连通性检查', null, null, 1, 'suite5');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CONN51006', '道路等级物理连通性检查', '道路等级物理连通性检查', null, null, 1, 'suite5');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CONN51008', 'FC逻辑连通性检查', 'FC逻辑连通性检查', null, null, 1, 'suite5');

insert into CK_RULE_COP (RULE_CODE, RULE_NAME, RULE_DESC, RULE_LEVEL, DEPENDS, RULE_STATUS, SUITE_ID)
values ('CONN51011', '逻辑连通性检查', '逻辑连通性检查', null, null, 1, 'suite5');

commit;

exit;