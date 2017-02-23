-- cp_region_province
insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (1, 'UR1', 120000, '天津市');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (1, 'UR1', 130000, '河北省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (1, 'UR1', 110000, '北京市');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (2, 'UR2', 210000, '辽宁省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (2, 'UR2', 230000, '黑龙江省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (2, 'UR2', 220000, '吉林省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (3, 'UR3', 310000, '上海市');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (3, 'UR3', 320000, '江苏省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (4, 'UR4', 340000, '安徽省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (4, 'UR4', 330000, '浙江省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (5, 'UR5', 410000, '河南省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (5, 'UR5', 370000, '山东省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (6, 'UR6', 420000, '湖北省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (6, 'UR6', 350000, '福建省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (6, 'UR6', 360000, '江西省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (7, 'UR7', 520000, '贵州省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (7, 'UR7', 450000, '广西壮族自治区');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (7, 'UR7', 430000, '湖南省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (8, 'UR8', 440000, '广东省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (9, 'UR9', 510000, '四川省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (9, 'UR9', 530000, '云南省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (9, 'UR9', 500000, '重庆市');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (10, 'UR10', 620000, '甘肃省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (10, 'UR10', 640000, '宁夏回族自治区');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (10, 'UR10', 630000, '青海省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (10, 'UR10', 650000, '新疆维吾尔自治区');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (10, 'UR10', 540000, '西藏自治区');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (10, 'UR10', 150000, '内蒙古自治区');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (12, 'UR12', 460000, '海南省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (13, 'UR13', 140000, '山西省');

insert into cp_region_province (REGION_ID, NDS_REGIONCODE, ADMINCODE, PROVINCE)
values (13, 'UR13', 610000, '陕西省');

-- info,multi-src,dealership,block相关会在初始化大区时同步维护
INSERT INTO CITY (CITY_ID,CITY_NAME,REGION_ID,PLAN_STATUS) VALUES (100000,'全国多源POI',0,0);
INSERT INTO CITY (CITY_ID,CITY_NAME,REGION_ID,PLAN_STATUS) VALUES (100001,'全国代理店',0,0);
INSERT INTO CITY (CITY_ID,CITY_NAME,REGION_ID,PLAN_STATUS) VALUES (100002,'全国一级情报',0,0);

DELETE FROM ROLE;
INSERT INTO ROLE(ROLE_ID,ROLE_NAME) VALUES(2,'普通用户');
INSERT INTO ROLE(ROLE_ID,ROLE_NAME) VALUES(3,'生管管理员');
INSERT INTO ROLE(ROLE_ID,ROLE_NAME) VALUES(4,'采集管理员');
INSERT INTO ROLE(ROLE_ID,ROLE_NAME) VALUES(5,'日编管理员');
INSERT INTO ROLE(ROLE_ID,ROLE_NAME) VALUES(6,'月编管理员');

Insert into man_config(conf_key,conf_value,conf_desc)
Values('POI_DAY2MONTH','1','日落月-POI定时融合月库开关，0可进行日落月，1不可进行日落月');


COMMIT;
EXIT;