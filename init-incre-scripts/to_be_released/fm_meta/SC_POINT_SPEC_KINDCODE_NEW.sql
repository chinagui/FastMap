--drop tanle
drop table SC_POINT_SPEC_KINDCODE_NEW;
-- Create table
create table SC_POINT_SPEC_KINDCODE_NEW
(
  POI_KIND      VARCHAR2(6) not null,
  POI_KIND_NAME VARCHAR2(64) not null,
  CHAIN         VARCHAR2(4),
  RATING        NUMBER(2),
  FLAGCODE      VARCHAR2(12),
  CATEGORY      NUMBER(2),
  KG_FLAG       VARCHAR2(2),
  HM_FLAG       VARCHAR2(3),
  MEMO          VARCHAR2(256) not null,
  ID            NUMBER(4) not null,
  TYPE          NUMBER(2) not null,
  TOPCITY       NUMBER(1)
);
insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4147', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 152, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4147', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 153, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4146', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 154, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4045', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 183, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '3483', null, '', 3, 'KG', 'DHM', '表内代理店分类', 146, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4028', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 195, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '34D3', null, '', 3, 'KG', 'HM', '表内代理店分类（FM未作业）', 196, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '413A', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 197, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '413A', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 198, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220100', '公司', '4012', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 199, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4038', null, '', 3, 'KG', 'DHM', '表内代理店分类', 200, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4038', null, '', 3, 'KG', 'DHM', '表内代理店分类', 201, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4094', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 212, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '415E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 203, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4091', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 194, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190107', '省、直辖市政府', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 685, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180400', '风景名胜', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 517, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403C', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 109, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180100', '运动场馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 232, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180101', '羽毛球场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 223, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180102', '网球场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 214, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3055', null, '', 3, '', 'DHM', 'POI英文名重要分类', 225, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3062', null, '', 3, '', 'DHM', 'POI英文名重要分类', 226, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3036', null, '', 3, '', 'DHM', 'POI英文名重要分类', 227, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3063', null, '', 3, '', 'DHM', 'POI英文名重要分类', 228, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '404C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 229, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('210103', '家电维修', '403A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 230, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '404F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 231, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4134', null, '', 7, '', 'DHM', 'POI英文名重要分类', 253, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4096', null, '', 7, '', 'DHM', 'POI英文名重要分类', 282, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '9011', null, '', 3, '', 'DHM', 'POI英文名重要分类', 250, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4040', null, '', 3, '', 'DHM', 'POI英文名重要分类', 274, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4041', null, '', 3, '', 'DHM', 'POI英文名重要分类', 276, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4037', null, '', 3, '', 'DHM', 'POI英文名重要分类', 277, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4046', null, '', 3, '', 'DHM', 'POI英文名重要分类', 278, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4047', null, '', 3, '', 'DHM', 'POI英文名重要分类', 279, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4035', null, '', 7, '', 'DHM', 'POI英文名重要分类', 291, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4041', null, '', 3, '', 'DHM', 'POI英文名重要分类', 281, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4047', null, '', 3, '', 'DHM', 'POI英文名重要分类', 272, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4024', null, '', 3, '', 'DHM', 'POI英文名重要分类', 283, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4005', null, '', 3, '', 'DHM', 'POI英文名重要分类', 284, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4130', null, '', 3, '', 'DHM', 'POI英文名重要分类', 285, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4044', null, '', 3, '', 'DHM', 'POI英文名重要分类', 286, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4046', null, '', 3, '', 'DHM', 'POI英文名重要分类', 287, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4047', null, '', 3, '', 'DHM', 'POI英文名重要分类', 288, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4028', null, '', 7, '', 'DHM', 'POI英文名重要分类', 289, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4132', null, '', 3, '', 'DHM', 'POI英文名重要分类', 290, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4043', null, '', 3, '', 'DHM', 'POI英文名重要分类', 262, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '403D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 260, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '400F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 280, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4030', null, '', 3, '', 'DHM', 'POI英文名重要分类', 254, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '900A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 255, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4032', null, '', 3, '', 'DHM', 'POI英文名重要分类', 256, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4023', null, '', 3, '', 'DHM', 'POI英文名重要分类', 257, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4015', null, '', 3, '', 'DHM', 'POI英文名重要分类', 258, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '401A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 259, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4022', null, '', 3, '', 'DHM', 'POI英文名重要分类', 271, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4015', null, '', 3, '', 'DHM', 'POI英文名重要分类', 261, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4007', null, '', 7, '', 'DHM', 'POI英文名重要分类', 252, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4139', null, '', 3, '', 'DHM', 'POI英文名重要分类', 263, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('240206', 'WI-FI', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 264, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '401A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 265, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '401A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 266, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4091', null, '', 3, '', 'DHM', 'POI英文名重要分类', 267, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4091', null, '', 3, '', 'DHM', 'POI英文名重要分类', 268, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4094', null, '', 7, '', 'DHM', 'POI英文名重要分类', 269, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '400D', null, '', 7, '', 'DHM', 'POI英文名重要分类', 270, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4038', null, '', 3, '', 'DHM', 'POI英文名重要分类', 332, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4038', null, '', 3, '', 'DHM', 'POI英文名重要分类', 425, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '413A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 467, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '413A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 468, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4049', null, '', 3, '', 'DHM', 'POI英文名重要分类', 469, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4049', null, '', 3, '', 'DHM', 'POI英文名重要分类', 470, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '400C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 471, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110301', '酒吧', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 472, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110303', '咖啡馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 473, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4024', null, '', 3, '', 'DHM', 'POI英文名重要分类', 484, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '412F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 475, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4037', null, '', 3, '', 'DHM', 'POI英文名重要分类', 466, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4137', null, '', 3, '', 'DHM', 'POI英文名重要分类', 477, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 478, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '9009', null, '', 3, '', 'DHM', 'POI英文名重要分类', 479, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4042', null, '', 3, '', 'DHM', 'POI英文名重要分类', 426, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4044', null, '', 7, '', 'DHM', 'POI英文名重要分类', 481, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 329, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4039', null, '', 3, '', 'DHM', 'POI英文名重要分类', 482, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4039', null, '', 3, '', 'DHM', 'POI英文名重要分类', 483, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4005', null, '', 3, '', 'DHM', 'POI英文名重要分类', 457, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 455, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4090', null, '', 7, '', 'DHM', 'POI英文名重要分类', 474, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4139', null, '', 3, '', 'DHM', 'POI英文名重要分类', 449, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '404C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 450, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '404F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 451, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180310', '水族馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 452, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180105', '高尔夫球场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 495, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180106', '高尔夫练习场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 454, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180103', '保龄球馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 476, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180104', '滑雪场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 456, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190501', '外国大使馆/领事馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 447, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4034', null, '', 3, '', 'DHM', 'POI英文名重要分类', 240, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4090', null, '', 7, 'KG', 'DHM', '表内代理店分类', 192, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130603', '通信设备零售', '403A', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 191, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4048', null, '', 3, 'KG', 'DHM', '表内代理店分类', 190, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4015', null, '', 3, 'KG', 'DHM', '表内代理店分类', 189, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4047', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 188, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4033', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 187, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4045', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 186, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4042', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 176, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '404B', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 184, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4035', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 193, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4007', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 182, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '9012', null, '', 7, '', 'DHM', 'POI英文名重要分类', 504, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4143', null, '', 7, '', 'DHM', 'POI英文名重要分类', 496, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 497, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140303', '厂家一览表内二手车交易', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 498, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 499, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4003', null, '', 3, '', 'DHM', 'POI英文名重要分类', 500, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4003', null, '', 3, '', 'DHM', 'POI英文名重要分类', 501, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '413E', null, '', 7, '', 'DHM', 'POI英文名重要分类', 465, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '400C', null, '', 7, '', 'DHM', 'POI英文名重要分类', 388, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4030', null, '', 3, '', 'DHM', 'POI英文名重要分类', 391, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 392, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4140', null, '', 3, '', 'DHM', 'POI英文名重要分类', 393, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4140', null, '', 3, '', 'DHM', 'POI英文名重要分类', 394, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '404D', null, '', 7, '', 'DHM', 'POI英文名重要分类', 395, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4141', null, '', 7, '', 'DHM', 'POI英文名重要分类', 396, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4142', null, '', 3, '', 'DHM', 'POI英文名重要分类', 397, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4142', null, '', 3, '', 'DHM', 'POI英文名重要分类', 408, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4144', null, '', 3, '', 'DHM', 'POI英文名重要分类', 399, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4144', null, '', 3, '', 'DHM', 'POI英文名重要分类', 390, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '402F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 438, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4030', null, '', 3, '', 'DHM', 'POI英文名重要分类', 401, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4095', null, '', 7, '', 'DHM', 'POI英文名重要分类', 453, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4025', null, '', 3, '', 'DHM', 'POI英文名重要分类', 402, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4041', null, '', 3, '', 'DHM', 'POI英文名重要分类', 403, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4036', null, '', 3, '', 'DHM', 'POI英文名重要分类', 404, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4133', null, '', 3, '', 'DHM', 'POI英文名重要分类', 405, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4135', null, '', 3, '', 'DHM', 'POI英文名重要分类', 406, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4136', null, '', 3, '', 'DHM', 'POI英文名重要分类', 407, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '403E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 381, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4007', null, '', 3, '', 'DHM', 'POI英文名重要分类', 379, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '402D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 398, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '402E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 373, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '403B', null, '', 3, '', 'DHM', 'POI英文名重要分类', 374, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4036', null, '', 3, '', 'DHM', 'POI英文名重要分类', 375, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4025', null, '', 3, '', 'DHM', 'POI英文名重要分类', 376, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4043', null, '', 3, '', 'DHM', 'POI英文名重要分类', 377, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 378, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4131', null, '', 3, '', 'DHM', 'POI英文名重要分类', 389, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4045', null, '', 3, '', 'DHM', 'POI英文名重要分类', 380, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4033', null, '', 3, '', 'DHM', 'POI英文名重要分类', 371, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '403C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 382, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403B', null, '', 3, '', 'DHM', 'POI英文名重要分类', 383, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 384, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4005', null, '', 3, '', 'DHM', 'POI英文名重要分类', 385, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 386, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4092', null, '', 3, '', 'DHM', 'POI英文名重要分类', 387, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4093', null, '', 7, '', 'DHM', 'POI英文名重要分类', 410, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4133', null, '', 7, '', 'DHM', 'POI英文名重要分类', 372, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 400, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 429, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4036', null, '', 3, '', 'DHM', 'POI英文名重要分类', 430, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4092', null, '', 3, '', 'DHM', 'POI英文名重要分类', 431, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '9006', null, '', 3, '', 'DHM', 'POI英文名重要分类', 432, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '9005', null, '', 3, '', 'DHM', 'POI英文名重要分类', 433, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '602D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 434, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6021', null, '', 3, '', 'DHM', 'POI英文名重要分类', 435, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '601C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 446, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6015', null, '', 3, '', 'DHM', 'POI英文名重要分类', 437, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6016', null, '', 3, '', 'DHM', 'POI英文名重要分类', 428, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6014', null, '', 3, '', 'DHM', 'POI英文名重要分类', 439, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '600D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 440, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '600E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 441, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '600F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 442, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160105', '高等教育', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 443, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180206', '赛马会', '', null, '', 1, '', 'HM', 'POI英文名重要分类', 444, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180211', '赛狗场', '', null, '', 1, '', 'HM', 'POI英文名重要分类', 445, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180207', '博彩', '', null, '', 1, '', 'HM', 'POI英文名重要分类', 419, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180308', '动物园', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 417, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180309', '植物园', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 436, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190114', '边检口岸', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 411, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230128', '机场出发/到达门', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 412, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4146', null, '', 3, '', 'DHM', 'POI英文名重要分类', 413, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180210', '艺术表演场馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 297, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180110', '溜冰场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 298, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180203', '健身房、俱乐部、康乐宫、电玩城', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 299, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180109', '垂钓', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 310, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230105', '火车站出发到达', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 301, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6003', null, '', 3, '', 'DHM', 'POI英文名重要分类', 292, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6045', null, '', 3, '', 'DHM', 'POI英文名重要分类', 303, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6002', null, '', 3, '', 'DHM', 'POI英文名重要分类', 304, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6001', null, '', 3, '', 'DHM', 'POI英文名重要分类', 305, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6000', null, '', 3, '', 'DHM', 'POI英文名重要分类', 306, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6028', null, '', 3, '', 'DHM', 'POI英文名重要分类', 307, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6046', null, '', 3, '', 'DHM', 'POI英文名重要分类', 308, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4024', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 115, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '404C', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 116, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4134', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 117, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4096', null, '', 7, 'KG', 'DHM', '表内代理店分类', 118, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '400C', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 119, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '402D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 120, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '402E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 121, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '403B', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 122, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '404F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 138, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4025', null, '', 3, 'KG', 'DHM', '表内代理店分类', 96, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4031', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 111, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4041', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 83, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4037', null, '', 3, 'KG', 'DHM', '表内代理店分类', 127, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '9009', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 128, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '900D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 129, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4043', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 130, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '900E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 131, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4131', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 132, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4039', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 133, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4046', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 134, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4036', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 135, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4040', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 136, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4034', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 98, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4139', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 125, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190105', '县级政府机关', '', null, '', 1, 'K', 'DHM', '菜单名分类', 1, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190106', '区级政府机关(广州市）', '', null, '', 1, 'K', 'D', '菜单名分类', 2, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190201', '公安局', '', null, '', 6, 'K', 'DHM', '菜单名分类', 3, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190103', '地级市级政府机关', '', null, '', 1, 'K', 'DHM', '菜单名分类', 4, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190108', '市政府', '', null, '', 1, 'K', 'DHM', '菜单名分类', 5, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190109', '区、县政府', '', null, '', 1, 'K', 'DHM', '菜单名分类', 6, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190202', '派出所', '', null, '', 6, 'K', 'DHM', '菜单名分类', 7, 1, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4006', null, '', 3, '', 'DHM', 'POI英文名重要分类', 418, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '9007', null, '', 7, '', 'DHM', 'POI英文名重要分类', 409, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170107', '牙科诊所', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 420, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4160', null, '', 7, '', 'DHM', 'POI英文名重要分类', 421, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4161', null, '', 7, '', 'DHM', 'POI英文名重要分类', 422, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220100', '公司', '412F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 423, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220100', '公司', '4012', null, '', 3, '', 'DHM', 'POI英文名重要分类', 424, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200101', '会议中心、展览中心', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 530, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180400', '风景名胜', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 529, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160207', '科技馆', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 525, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160205', '博物馆、纪念馆、展览馆、陈列馆', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 528, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160206', '美术馆', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 532, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190401', '寺庙、道观、庵', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 531, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180304', '公园', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 522, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180310', '水族馆', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 526, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180309', '植物园', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 524, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180308', '动物园', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 523, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180302', '度假村、疗养院', '', null, '', 1, 'KG', 'DHM', '景点等级分类', 527, 9, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4044', null, '', 3, 'KG', 'DHM', '表外代理店分类', 551, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4036', null, '', 3, 'KG', 'DHM', '表外代理店分类', 552, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '3483', null, '', 3, 'KG', 'DHM', '表外代理店分类', 562, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '400C', null, '', 3, 'KG', 'DHM', '表外代理店分类', 553, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4007', null, '', 3, 'KG', 'DHM', '表外代理店分类', 548, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4137', null, '', 3, 'KG', 'DHM', '表外代理店分类', 554, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '403E', null, '', 3, 'KG', 'DHM', '表外代理店分类', 546, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4136', null, '', 3, 'KG', 'DHM', '表外代理店分类', 555, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4005', null, '', 3, 'KG', 'DHM', '表外代理店分类', 550, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4133', null, '', 3, 'KG', 'DHM', '表外代理店分类', 556, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4037', null, '', 3, 'KG', 'DHM', '表外代理店分类', 557, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '222A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 347, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '222B', null, '', 3, '', 'DHM', 'POI英文名重要分类', 346, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '222C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 345, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '2200', null, '', 3, '', 'DHM', 'POI英文名重要分类', 344, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '412E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 343, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '222D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 342, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '222E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 331, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '220A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 340, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2218', null, '', 3, '', 'DHM', 'POI英文名重要分类', 350, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2209', null, '', 3, '', 'DHM', 'POI英文名重要分类', 338, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2F11', null, '', 3, '', 'DHM', 'POI英文名重要分类', 337, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '222F', null, '', 3, '', 'DHM', 'POI英文名重要分类', 336, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2215', null, '', 3, '', 'DHM', 'POI英文名重要分类', 335, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2F12', null, '', 3, '', 'DHM', 'POI英文名重要分类', 334, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2230', null, '', 3, '', 'DHM', 'POI英文名重要分类', 333, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2231', null, '', 3, '', 'DHM', 'POI英文名重要分类', 359, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2232', null, '', 3, '', 'DHM', 'POI英文名重要分类', 339, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2233', null, '', 3, '', 'DHM', 'POI英文名重要分类', 341, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2234', null, '', 3, '', 'DHM', 'POI英文名重要分类', 369, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '9001', null, '', 3, '', 'DHM', 'POI英文名重要分类', 368, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2235', null, '', 3, '', 'DHM', 'POI英文名重要分类', 367, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2222', null, '', 3, '', 'DHM', 'POI英文名重要分类', 366, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('260000', '乡\镇', '', null, '', 1, 'KG', 'DHM', '地名索引', 25, 3, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('260200', '水系', '', null, '', 1, 'KG', 'DHM', '地名索引', 24, 3, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('260100', '村\屯', '', null, '', 1, 'KG', 'DHM', '地名索引', 26, 3, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190110', '乡镇政府', '', null, '', 1, 'KG', 'DHM', 'hamlet作业需要参考的POI分类', 29, 4, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190100', '政府及管理机构', '', null, '', 1, 'KG', 'DHM', 'hamlet作业需要参考的POI分类', 28, 4, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190113', '基层群众自治组织', '', null, '', 1, 'KG', 'DHM', 'hamlet作业需要参考的POI分类', 27, 4, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4003', null, '', 3, 'KG', 'DHM', '表内代理店分类', 123, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '402F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 86, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4144', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 87, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4144', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 88, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4142', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 89, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4142', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 90, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4140', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 91, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4140', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 92, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4030', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 93, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4145', null, '', 7, 'KG', 'DHM', '表内代理店分类', 94, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4003', null, '', 3, 'KG', 'DHM', '表内代理店分类', 95, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4146', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 110, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402A', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 97, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140303', '厂家一览表内二手车交易', '', null, '', 1, 'KG', 'DHM', '表内代理店分类（FM未作业）', 84, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 99, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4143', null, '', 7, 'KG', 'DHM', '表内代理店分类', 100, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4039', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 105, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '413E', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 106, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4042', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 107, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4005', null, '', 3, 'KG', 'DHM', '表内代理店分类', 108, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190401', '寺庙、道观、庵', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 519, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190400', '宗教', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 518, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190402', '天主教', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 516, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190403', '基督教', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 521, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190404', '伊斯兰教', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 514, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160206', '美术馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 513, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160205', '博物馆、纪念馆、展览馆、陈列馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 505, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4146', null, '', 3, '', 'DHM', 'POI英文名重要分类', 414, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4147', null, '', 3, '', 'DHM', 'POI英文名重要分类', 415, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4147', null, '', 3, '', 'DHM', 'POI英文名重要分类', 416, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4006', null, '', 3, '', 'DHM', 'POI英文名重要分类', 427, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '2219', null, '', 3, '', 'DHM', 'POI英文名重要分类', 365, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '221A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 364, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '223A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 363, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130102', '百货商场零售', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 362, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230215', '加油站', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 351, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230213', '换乘停车场（P+R停车场）', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 360, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '400D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 370, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '404A', null, '', 7, '', 'DHM', 'POI英文名重要分类', 358, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '415F', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 124, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '3483', null, '', 3, 'KG', 'DHM', '表内代理店分类', 71, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '404F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 82, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '404C', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 69, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4012', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 126, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4012', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 81, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4090', null, '', 3, 'KG', 'DHM', '表内代理店分类', 149, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '401D', null, '', 3, 'KG', 'DHM', '表内代理店分类', 46, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '401D', null, '', 3, 'KG', 'DHM', '表内代理店分类', 79, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4097', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 78, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4091', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 77, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4049', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 76, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '9012', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 67, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '400F', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 66, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4026', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 65, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220100', '公司', '403A', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 64, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4049', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 63, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 62, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '2FF1', null, '', 7, 'KG', 'DHM', '表内代理店分类', 80, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '361F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 711, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '3620', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 712, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '3621', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 713, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '3621', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 714, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '348D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 715, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '348D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 716, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '3621', null, '', 3, '', 'DHM', 'POI英文名重要分类', 717, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '3621', null, '', 3, '', 'DHM', 'POI英文名重要分类', 718, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '348D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 719, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '348D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 720, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180308', '动物园', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 662, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180307', '游乐园', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 663, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180304', '公园', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 664, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130703', '家装建材零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 654, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190102', '省级政府机关', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 653, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130102', '百货商场零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 652, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160105', '高等教育', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 651, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130103', '百货商店零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 650, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130101', '商品交易/批发市场零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 649, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120202', '住宅楼', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 648, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120201', '小区', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 647, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120104', '普通出租公寓', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 642, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120103', '酒店公寓', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 645, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120102', '一般旅馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 643, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 638, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 608, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403E', null, '', 3, 'KG', 'DHM', '表内代理店分类', 185, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 210, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220100', '公司', '412F', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 209, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4005', null, '', 3, 'KG', 'DHM', '表内代理店分类', 208, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4092', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 207, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4160', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 206, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4032', null, '', 3, 'KG', 'DHM', '表内代理店分类', 205, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '414A', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 173, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4149', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 172, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4095', null, '', 7, 'KG', 'DHM', '表内代理店分类', 171, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4148', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 170, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4148', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 169, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4162', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 168, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4161', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 157, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4032', null, '', 3, 'KG', 'DHM', '表内代理店分类', 166, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '415D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 175, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4024', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 164, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4139', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 163, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4130', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 162, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '9007', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 161, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '401A', null, '', 3, 'KG', 'DHM', '表内代理店分类', 160, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '412F', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 159, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '415D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 158, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '401A', null, '', 3, 'KG', 'DHM', '表内代理店分类', 155, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4162', null, '', 7, '', 'DHM', 'POI英文名重要分类', 323, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '400D', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 38, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4026', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 36, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4037', null, '', 3, 'KG', 'DHM', '表内代理店分类', 37, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4098', null, '', 3, 'KG', 'DHM', '表内代理店分类', 42, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4006', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 35, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4006', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 34, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '9011', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 33, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '900C', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 32, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4046', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 31, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4131', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 30, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4041', null, '', 3, 'KG', 'DHM', '表外代理店分类', 558, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4022', null, '', 3, 'KG', 'DHM', '表外代理店分类', 559, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '401A', null, '', 3, 'KG', 'DHM', '表外代理店分类', 560, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4015', null, '', 3, 'KG', 'DHM', '表外代理店分类', 561, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4024', null, '', 3, 'KG', 'DHM', '表外代理店分类', 549, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4135', null, '', 3, 'KG', 'DHM', '表外代理店分类', 547, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '400D', null, '', 3, 'KG', 'DHM', '表外代理店分类', 534, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4023', null, '', 3, 'KG', 'DHM', '表外代理店分类', 535, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '412F', null, '', 3, 'KG', 'DHM', '表外代理店分类', 536, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4130', null, '', 3, 'KG', 'DHM', '表外代理店分类', 537, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4047', null, '', 3, 'KG', 'DHM', '表外代理店分类', 538, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4132', null, '', 3, 'KG', 'DHM', '表外代理店分类', 540, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '403D', null, '', 3, 'KG', 'DHM', '表外代理店分类', 541, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '400F', null, '', 3, 'KG', 'DHM', '表外代理店分类', 533, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4030', null, '', 3, 'KG', 'DHM', '表外代理店分类', 542, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '900A', null, '', 3, 'KG', 'DHM', '表外代理店分类', 543, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4032', null, '', 3, 'KG', 'DHM', '表外代理店分类', 544, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4043', null, '', 3, 'KG', 'DHM', '表外代理店分类', 545, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '4046', null, '', 3, 'KG', 'DHM', '表外代理店分类', 539, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230118', '出租车客运出入口（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 575, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230112', '轻轨/地铁站主点（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 573, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230115', '磁悬浮主点（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 572, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230120', '小巴出入口（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 571, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230123', '水上公交（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 569, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230124', '水上公交出入口（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 574, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230121', '缆车站（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 564, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230122', '缆车站出入口（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 568, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230110', '长途客运站出入口（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 563, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230101', '公共电汽车客运（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 567, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230117', '出租车客运（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 570, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '361E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 709, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '361E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 710, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230119', '专线小巴站主点', '', null, '', 2, 'KG', 'HM', '核心公交分类', 566, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230102', '公共电汽车客运出入口（行人导航）', '', null, '', 2, 'KG', 'DHM', '核心公交分类', 565, 11, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170101', '综合医院', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 640, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180203', '健身房、俱乐部、康乐宫、电玩城', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 637, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180106', '高尔夫练习场', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 636, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180105', '高尔夫球场', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 635, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180104', '滑雪场', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 634, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180100', '运动场馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 633, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170108', '疾病预防控制及防疫', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 632, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190109', '区、县政府', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 620, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170102', '专科医院', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 630, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180302', '度假村、疗养院', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 644, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170100', '医疗机构', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 641, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160208', '文化活动中心', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 631, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160207', '科技馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 628, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160206', '美术馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 627, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160205', '博物馆、纪念馆、展览馆、陈列馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 626, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160202', '广播、电视、电影', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 625, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170106', '社区医疗', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 624, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190100', '政府及管理机构', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 623, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190108', '市政府', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 688, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190109', '区、县政府', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 696, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190110', '乡镇政府', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 689, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190111', '行政办公大厅', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 690, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190112', '驻京、驻地方办事处', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 691, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190113', '基层群众自治组织', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 692, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190401', '寺庙、道观、庵', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 693, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190500', '国际组织', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 694, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200101', '会议中心、展览中心', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 695, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200102', '培训中心　', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 708, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('210207', '洗浴服务', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 697, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230100', '客货运输', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 687, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('240100', '科研机构及附属机构', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 699, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230117', '出租车客运（行人导航）', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 599, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230107', '货运火车站', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 598, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120102', '一般旅馆', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 701, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130101', '商品交易/批发市场零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 702, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130106', '超级市场零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 703, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130301', '服装、箱包零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 704, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130303', '母婴用品、儿童用品零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 705, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130602', '电脑及数码产品', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 706, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130700', '金、家具及室内装修材料零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 707, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130702', '家具零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 698, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130703', '家装建材零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 680, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130705', '家居用品零售', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 666, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160202', '广播、电视、电影', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 674, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160205', '博物馆、纪念馆、展览馆、陈列馆', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 668, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160206', '美术馆', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 669, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160207', '科技馆', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 670, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160208', '文化活动中心', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 671, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170106', '社区医疗', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 672, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170108', '疾病预防控制及防疫', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 673, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180104', '滑雪场', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 667, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180203', '健身房、俱乐部、康乐宫、电玩城', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 675, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180210', '艺术表演场馆', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 676, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180301', '农家乐、民俗游', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 677, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180302', '度假村、疗养院', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 686, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180310', '水族馆', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 679, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190100', '政府及管理机构', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 665, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4026', null, '', 3, '', 'DHM', 'POI英文名重要分类', 494, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '400F', null, '', 7, '', 'DHM', 'POI英文名重要分类', 485, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4025', null, '', 3, 'KG', 'DHM', '表内代理店分类', 181, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4036', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 180, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 179, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '402D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 178, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4133', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 177, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4093', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 202, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4092', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 204, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900E', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 43, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4043', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 56, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '9009', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 40, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('240206', 'WI-FI', '', null, '', 1, 'KG', 'DHM', '表内代理店分类（FM未作业）', 47, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4041', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 48, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900C', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 50, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4138', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 51, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4141', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 52, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4015', null, '', 3, '', 'DHM', 'POI英文名重要分类', 489, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4048', null, '', 3, '', 'DHM', 'POI英文名重要分类', 490, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '2223', null, '', 3, '', 'DHM', 'POI英文名重要分类', 491, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4026', null, '', 3, '', 'DHM', 'POI英文名重要分类', 502, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190101', '国家级政府机关', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 681, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190102', '省级政府机关', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 682, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190103', '地级市级政府机关', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 683, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190104', '区级政府机关', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 684, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '3627', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 723, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140302', '厂家一览表外汽车零售及修理', '3627', null, '', 3, 'KG', 'DHM', '表外代理店分类', 724, 10, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190107', '省、直辖市政府', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 622, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190106', '区级政府机关(广州市）', '', null, '', 1, 'KG', 'D', '父POI的分类', 621, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190105', '县级政府机关', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 629, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190104', '区级政府机关', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 646, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190103', '地级市级政府机关', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 656, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180210', '艺术表演场馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 657, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190101', '国家级政府机关', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 655, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180301', '农家乐、民俗游', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 658, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180400', '风景名胜', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 659, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180310', '水族馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 660, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180309', '植物园', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 661, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4037', null, '', 3, '', 'DHM', 'POI英文名重要分类', 322, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '9009', null, '', 3, '', 'DHM', 'POI英文名重要分类', 311, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4042', null, '', 3, '', 'DHM', 'POI英文名重要分类', 320, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '900D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 330, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4043', null, '', 3, '', 'DHM', 'POI英文名重要分类', 318, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '900E', null, '', 3, '', 'DHM', 'POI英文名重要分类', 317, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4131', null, '', 3, '', 'DHM', 'POI英文名重要分类', 316, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4045', null, '', 3, '', 'DHM', 'POI英文名重要分类', 315, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130603', '通信设备零售', '403A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 314, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '404B', null, '', 7, '', 'DHM', 'POI英文名重要分类', 313, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4024', null, '', 3, '', 'DHM', 'POI英文名重要分类', 312, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '412F', null, '', 7, '', 'DHM', 'POI英文名重要分类', 352, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4097', null, '', 7, '', 'DHM', 'POI英文名重要分类', 324, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '401D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 325, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '401D', null, '', 3, '', 'DHM', 'POI英文名重要分类', 326, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4090', null, '', 3, '', 'DHM', 'POI英文名重要分类', 327, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4012', null, '', 3, '', 'DHM', 'POI英文名重要分类', 328, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4012', null, '', 3, '', 'DHM', 'POI英文名重要分类', 302, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 321, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180302', '度假村、疗养院', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 319, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180307', '游乐园', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 293, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180303', '天然浴场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 294, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180107', '赛马场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 295, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180209', '电影院', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 296, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230210', '停车场', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 721, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190105', '县级政府机关', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 678, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190106', '区级政府机关(广州市）', '', null, '', 1, 'KG', 'DHM', '高端POI的父分类', 700, 13, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '900C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 480, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '9011', null, '', 3, '', 'DHM', 'POI英文名重要分类', 221, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130601', '电器零售', '2303', null, '', 3, '', 'DHM', 'POI英文名重要分类', 220, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130601', '电器零售', '2304', null, '', 3, '', 'DHM', 'POI英文名重要分类', 219, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130601', '电器零售', '2305', null, '', 3, '', 'DHM', 'POI英文名重要分类', 218, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4040', null, '', 3, '', 'DHM', 'POI英文名重要分类', 217, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4033', null, '', 3, '', 'DHM', 'POI英文名重要分类', 216, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4046', null, '', 3, '', 'DHM', 'POI英文名重要分类', 215, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4048', null, '', 3, '', 'DHM', 'POI英文名重要分类', 241, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4130', null, '', 7, '', 'DHM', 'POI英文名重要分类', 243, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4148', null, '', 3, '', 'DHM', 'POI英文名重要分类', 224, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4148', null, '', 3, '', 'DHM', 'POI英文名重要分类', 249, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4149', null, '', 7, '', 'DHM', 'POI英文名重要分类', 248, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '414A', null, '', 7, '', 'DHM', 'POI英文名重要分类', 247, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4032', null, '', 3, '', 'DHM', 'POI英文名重要分类', 246, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4032', null, '', 3, '', 'DHM', 'POI英文名重要分类', 245, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4098', null, '', 3, '', 'DHM', 'POI英文名重要分类', 244, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4031', null, '', 3, '', 'DHM', 'POI英文名重要分类', 233, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4031', null, '', 3, '', 'DHM', 'POI英文名重要分类', 242, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4034', null, '', 3, '', 'DHM', 'POI英文名重要分类', 251, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190500', '国际组织', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 458, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190107', '省、直辖市政府', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 459, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190502', '签证处', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 460, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160203', '图书馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 461, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170101', '综合医院', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 462, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('170103', '私人诊所', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 463, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180304', '公园', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 464, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180306', '广场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 448, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200101', '会议中心、展览中心', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 493, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230103', '客运火车站', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 512, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230111', '轻轨/地铁站出入口', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 520, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230108', '客运汽车站', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 506, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230114', '磁悬浮出入口', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 507, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230126', '机场', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 508, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230127', '机场出发/到达', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 509, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230129', '城市候机楼', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 510, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230125', '港口、码头', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 515, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230206', '高速服务区', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 511, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230207', '高速停车区', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 503, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160207', '科技馆', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 492, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3056', null, '', 3, '', 'DHM', 'POI英文名重要分类', 486, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '8008', null, '', 3, '', 'DHM', 'POI英文名重要分类', 487, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3051', null, '', 3, '', 'DHM', 'POI英文名重要分类', 488, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230115', '磁悬浮主点（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 580, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230119', '专线小巴站主点', '', null, '', 2, 'KG', 'HM', '父POI的分类', 577, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230103', '客运火车站', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 578, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230123', '水上公交（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 579, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230127', '机场出发/到达', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 585, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230206', '高速服务区', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 581, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230207', '高速停车区', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 576, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230215', '加油站', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 583, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6025', null, '', 3, '', 'DHM', 'POI英文名重要分类', 309, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '602C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 361, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '602B', null, '', 3, '', 'DHM', 'POI英文名重要分类', 354, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6047', null, '', 3, '', 'DHM', 'POI英文名重要分类', 355, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '602A', null, '', 3, '', 'DHM', 'POI英文名重要分类', 356, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6020', null, '', 3, '', 'DHM', 'POI英文名重要分类', 357, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6026', null, '', 3, '', 'DHM', 'POI英文名重要分类', 353, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('150101', '银行', '6027', null, '', 3, '', 'DHM', 'POI英文名重要分类', 239, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '304C', null, '', 3, '', 'DHM', 'POI英文名重要分类', 238, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3340', null, '', 3, '', 'DHM', 'POI英文名重要分类', 237, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3053', null, '', 3, '', 'DHM', 'POI英文名重要分类', 236, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110200', '快餐', '3342', null, '', 3, '', 'DHM', 'POI英文名重要分类', 235, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('110102', '异国风味', '', null, '', 1, '', 'DHM', 'POI英文名重要分类', 234, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '2225', null, '', 3, '', 'DHM', 'POI英文名重要分类', 300, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '2226', null, '', 3, '', 'DHM', 'POI英文名重要分类', 222, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '2227', null, '', 3, '', 'DHM', 'POI英文名重要分类', 273, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '2228', null, '', 3, '', 'DHM', 'POI英文名重要分类', 349, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130105', '便利零售', '2229', null, '', 3, '', 'DHM', 'POI英文名重要分类', 348, 8, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190110', '乡镇政府', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 588, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130702', '家具零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 593, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130700', '五金、家具及室内装修材料零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 592, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130602', '电脑及数码产品', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 591, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130303', '母婴用品、儿童用品零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 590, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130301', '服装、箱包零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 589, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('130705', '家居用品零售', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 606, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230125', '港口、码头', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 582, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230104', '火车站（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 639, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230108', '客运汽车站', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 596, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230109', '长途客运站（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 587, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230112', '轻轨/地铁站主点（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 586, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '404D', null, '', 7, 'KG', 'DHM', '表内代理店分类', 53, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4034', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 55, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '9011', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 57, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '4031', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 137, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '900D', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 112, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4040', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 113, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '404A', null, '', 7, 'KG', 'DHM', '表内代理店分类（FM未作业）', 114, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230216', '加气站', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 584, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('240100', '科研机构及附属机构', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 594, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190108', '市政府', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 609, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230121', '缆车站（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 618, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190501', '外国大使馆/领事馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 617, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230126', '机场', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 616, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230101', '公共电汽车客运（行人导航）', '', null, '', 2, 'KG', 'DHM', '父POI的分类', 615, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190112', '驻京、驻地方办事处', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 614, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190113', '基层群众自治组织', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 613, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190500', '国际组织', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 612, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230218', '电动汽车充电站', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 722, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190111', '行政办公大厅', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 611, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200101', '会议中心、展览中心　', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 610, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200102', '培训中心', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 597, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220200', '厂矿', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 607, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230100', '客货运输', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 619, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('190401', '寺庙、道观、庵', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 595, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220300', '工业园', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 605, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200103', '大厦/写字楼', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 604, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('220100', '公司', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 603, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('210211', '陵园、公墓', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 602, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('210207', '洗浴服务', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 601, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200104', '商务中心/会馆', '', null, '', 1, 'KG', 'DHM', '父POI的分类', 600, 12, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140101', '厂家一览表内汽车零售', '403B', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 148, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '403C', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 167, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('210103', '家电维修', '403A', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 165, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4048', null, '', 3, 'KG', 'DHM', '表内代理店分类', 140, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4015', null, '', 3, 'KG', 'DHM', '表内代理店分类', 141, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4047', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 142, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4033', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 143, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140201', '厂家一览表内汽车修理', '4030', null, '', 3, 'KG', 'DHM', '表内代理店分类（FM未作业）', 144, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('140301', '厂家一览表内汽车零售及修理', '4044', null, '', 7, 'KG', 'DHM', '表内代理店分类', 145, 7, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 14, '', 5, 'KG', 'DHM', '必须制作别名的分类', 726, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 5, '', 5, 'KG', 'DHM', '必须制作别名的分类', 727, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 4, '', 5, 'KG', 'DHM', '必须制作别名的分类', 728, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 15, '', 5, 'KG', 'DHM', '必须制作别名的分类', 729, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 4, '', 5, 'KG', 'DHM', '网络搜集英文名对象', 730, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 5, '', 5, 'KG', 'DHM', '网络搜集英文名对象', 731, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 14, '', 5, 'KG', 'DHM', '网络搜集英文名对象', 732, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('120101', '星级酒店', '', 15, '', 5, 'KG', 'DHM', '网络搜集英文名对象', 733, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160105', '高等教育', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 734, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160205', '博物馆、纪念馆、展览馆、陈列馆', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 735, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('160206', '美术馆', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 736, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180100', '体育场馆', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 737, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180104', '滑雪场', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 738, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180105', '高尔夫球场', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 739, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180106', '高尔夫练习场', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 740, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180210', '剧场、戏院、音乐厅', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 741, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180306', '广场', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 742, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('180307', '游乐园', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 743, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200101', '会议中心、展览中心', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 744, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('200104', '商务中心\会馆', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 745, 2, 1);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230103', '火车站', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 746, 2, 0);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230105', '火车站出发到达', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 747, 2, 0);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230126', '机场', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 748, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230126', '机场', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 749, 2, 0);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230127', '机场出发/到达', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 750, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230127', '机场出发/到达', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 751, 2, 0);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230128', '机场出发/到达门', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 752, 14, null);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230128', '机场出发/到达门', '', null, '', 1, 'KG', 'DHM', '必须制作别名的分类', 753, 2, 0);

insert into SC_POINT_SPEC_KINDCODE_NEW (POI_KIND, POI_KIND_NAME, CHAIN, RATING, FLAGCODE, CATEGORY, KG_FLAG, HM_FLAG, MEMO, ID, TYPE, TOPCITY)
values ('230129', '城市候机楼', '', null, '', 1, 'KG', 'DHM', '网络搜集英文名对象', 754, 14, null);
commit;
EXIT;