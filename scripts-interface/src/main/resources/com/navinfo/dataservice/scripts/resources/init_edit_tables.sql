TRUNCATE TABLE POI_EDIT_STATUS;
TRUNCATE TABLE POI_COLUMN_WORKITEM_CONF;
TRUNCATE TABLE POI_COLUMN_OP_CONF;
TRUNCATE TABLE POI_column_STATUS;
INSERT INTO POI_EDIT_STATUS(PID,COMMIT_HIS_STATUS) SELECT PID,1 FROM IX_POI;
COMMIT;

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('1', 'poi_deep', 'deepCarrental', 'FM-M-DP-012', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('2', 'poi_deep', 'deepCarrental', 'FM-M-DP-010', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('3', 'poi_deep', 'deepCarrental', 'FM-M-DP-014', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('4', 'poi_deep', 'deepDetail', 'FM-M-DP-002', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('5', 'poi_deep', 'deepParking', 'FM-M-DP-007', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('6', 'poi_deep', 'deepParking', 'FM-M-DP-004', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('7', 'poi_deep', 'deepParking', 'FM-YW-20-225', 0, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('8', 'poi_deep', 'deepParking', 'FM-M-DP-006', 0, 2);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('9', 'poi_address', 'addrPinyin', 'FM-YW-20-026', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('10', 'poi_address', 'addrSplit', 'FM-A09-01', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('11', 'poi_deep', 'deepCarrental', 'FM-CARRENTAL', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('12', 'poi_deep', 'deepDetail', 'FM-DETAIL', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('13', 'poi_deep', 'deepParking', 'FM-PARKING', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('14', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-018', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('15', 'poi_englishname', 'aliasOriEngName', 'FM-M01-03', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('16', 'poi_englishname', 'aliasStdEngName', 'FM-M01-04', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('17', 'poi_englishname', 'chiEngName', 'FM-YW-20-013', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('18', 'poi_englishname', 'confirmEngName', 'FM-YW-20-014', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('19', 'poi_englishname', 'netEngName', 'FM-M01-02', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('20', 'poi_englishname', 'nonImportantEngAddress', 'FM-YW-20-017', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('21', 'poi_englishname', 'nonImportantLongEngName', 'FM-YW-20-052', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('22', 'poi_englishname', 'officalStandardEngName', 'FM-YW-20-053', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('23', 'poi_englishname', 'photoEngName', 'FM-YW-20-012', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('24', 'poi_name', 'aliasName', 'FM-M01-01', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('25', 'poi_name', 'namePinyin', 'FM-A04-18', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('26', 'poi_name', 'nameUnify', 'FM-A04-04', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('27', 'poi_name', 'nameUnify', 'FM-A04-05', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('28', 'poi_name', 'nameUnify', 'FM-A04-10', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('29', 'poi_name', 'nameUnify', 'FM-A04-08', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('30', 'poi_name', 'nameUnify', 'FM-A04-09', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('31', 'poi_name', 'nameUnify', 'FM-A04-21', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('32', 'poi_name', 'shortName', 'FM-A07-02', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('33', 'poi_name', 'shortName', 'FM-A07-12', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('34', 'poi_name', 'shortName', 'FM-A07-11', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('35', 'poi_name', 'shortName', 'FM-A07-01', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('36', 'poi_name', 'shortName', 'FM-A07-03', 1, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('37', 'poi_address', 'addrPinyin', 'FM-YW-20-008', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('38', 'poi_address', 'addrPinyin', 'FM-A09-16', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('39', 'poi_address', 'addrPinyin', 'FM-A09-17', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('40', 'poi_address', 'addrSplit', 'FM-A09-13', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('41', 'poi_address', 'addrSplit', 'GLM60481', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('42', 'poi_address', 'addrSplit', 'FM-A09-05', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('43', 'poi_address', 'addrSplit', 'GLM60442', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('44', 'poi_address', 'addrSplit', 'GLM60377', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('45', 'poi_address', 'addrSplit', 'GLM60188', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('46', 'poi_address', 'addrSplit', 'GLM60480', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('47', 'poi_address', 'addrSplit', 'FM-A09-07', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('48', 'poi_address', 'addrSplit', 'GLM60186', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('49', 'poi_address', 'addrSplit', 'GLM60119', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('50', 'poi_address', 'addrSplit', 'FM-YW-20-065', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('51', 'poi_address', 'addrSplit', 'FM-14Sum-06-03', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('52', 'poi_address', 'addrSplit', 'FM-D01-108', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('53', 'poi_address', 'addrSplit', 'FM-YW-20-081', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('54', 'poi_address', 'addrSplit', 'FM-A09-14', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('55', 'poi_address', 'addrSplit', 'GLM60115', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('56', 'poi_address', 'addrSplit', 'FM-A09-04', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('57', 'poi_address', 'addrSplit', 'FM-YW-20-080', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('58', 'poi_address', 'addrSplit', 'FM-YW-20-061', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('59', 'poi_address', 'addrSplit', 'FM-YW-20-079', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('60', 'poi_address', 'addrSplit', 'GLM60117', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('61', 'poi_address', 'addrSplit', 'FM-YW-20-038', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('62', 'poi_address', 'addrSplit', 'FM-D01-111', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('63', 'poi_address', 'addrSplit', 'FM-CHR73002', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('64', 'poi_address', 'addrSplit', 'GLM60181', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('65', 'poi_address', 'addrSplit', 'FM-D01-109', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('66', 'poi_address', 'addrSplit', 'FM-D01-110', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('67', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-101', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('68', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-117', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('69', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-110', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('70', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-115', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('71', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-029', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('72', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-118', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('73', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-064', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('74', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-099', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('75', 'poi_englishaddress', 'engMapAddress', 'GLM60335', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('76', 'poi_englishaddress', 'engMapAddress', 'FM-CHR71040', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('77', 'poi_englishaddress', 'engMapAddress', 'GLM60190', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('78', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-100', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('79', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-034', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('80', 'poi_englishaddress', 'engMapAddress', 'FM-YW-20-212', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('81', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-064', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('82', 'poi_englishaddress', 'nonImportantLongEngAddress', 'GLM60335', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('83', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-118', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('84', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-110', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('85', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-029', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('86', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-099', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('87', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-117', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('88', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-034', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('89', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-115', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('90', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-101', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('91', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-YW-20-100', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('92', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-CHR71040', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('93', 'poi_englishname', 'chiEngName', 'FM-D01-90', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('94', 'poi_englishname', 'chiEngName', 'FM-D01-56', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('95', 'poi_englishname', 'chiEngName', 'FM-D01-76', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('96', 'poi_englishname', 'chiEngName', 'FM-D01-44', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('97', 'poi_englishname', 'chiEngName', 'FM-D01-72', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('98', 'poi_englishname', 'chiEngName', 'FM-D01-94', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('99', 'poi_englishname', 'chiEngName', 'FM-D01-64', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('100', 'poi_englishname', 'chiEngName', 'FM-D01-68', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('101', 'poi_englishname', 'chiEngName', 'FM-D01-104', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('102', 'poi_englishname', 'chiEngName', 'FM-D01-48', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('103', 'poi_englishname', 'chiEngName', 'FM-D01-84', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('104', 'poi_englishname', 'chiEngName', 'FM-D01-98', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('105', 'poi_englishname', 'chiEngName', 'FM-D01-80', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('106', 'poi_englishname', 'chiEngName', 'FM-D01-52', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('107', 'poi_englishname', 'chiEngName', 'FM-D01-60', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('108', 'poi_englishname', 'chiEngName', 'FM-D01-102', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('109', 'poi_englishname', 'chiEngName', 'FM-D01-86', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('110', 'poi_englishname', 'confirmEngName', 'FM-D01-86', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('111', 'poi_englishname', 'confirmEngName', 'FM-D01-52', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('112', 'poi_englishname', 'confirmEngName', 'FM-D01-48', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('113', 'poi_englishname', 'confirmEngName', 'FM-D01-76', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('114', 'poi_englishname', 'confirmEngName', 'FM-D01-90', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('115', 'poi_englishname', 'confirmEngName', 'FM-D01-64', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('116', 'poi_englishname', 'confirmEngName', 'FM-D01-68', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('117', 'poi_englishname', 'confirmEngName', 'FM-D01-102', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('118', 'poi_englishname', 'confirmEngName', 'FM-D01-72', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('119', 'poi_englishname', 'confirmEngName', 'FM-D01-80', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('120', 'poi_englishname', 'confirmEngName', 'FM-D01-98', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('121', 'poi_englishname', 'confirmEngName', 'FM-D01-104', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('122', 'poi_englishname', 'confirmEngName', 'FM-D01-60', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('123', 'poi_englishname', 'confirmEngName', 'FM-D01-94', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('124', 'poi_englishname', 'confirmEngName', 'FM-D01-56', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('125', 'poi_englishname', 'confirmEngName', 'FM-D01-44', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('126', 'poi_englishname', 'confirmEngName', 'FM-D01-84', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('127', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-56', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('128', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-86', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('129', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-76', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('130', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-94', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('131', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-98', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('132', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-90', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('133', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-64', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('134', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-68', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('135', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-60', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('136', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-48', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('137', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-102', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('138', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-44', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('139', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-80', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('140', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-72', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('141', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-84', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('142', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-52', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('143', 'poi_englishname', 'nonImportantLongEngName', 'FM-D01-104', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('144', 'poi_englishname', 'officalStandardEngName', 'FM-D01-69', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('145', 'poi_englishname', 'officalStandardEngName', 'FM-D01-73', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('146', 'poi_englishname', 'officalStandardEngName', 'FM-D01-57', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('147', 'poi_englishname', 'officalStandardEngName', 'FM-D01-99', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('148', 'poi_englishname', 'officalStandardEngName', 'FM-D01-53', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('149', 'poi_englishname', 'officalStandardEngName', 'FM-D01-65', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('150', 'poi_englishname', 'officalStandardEngName', 'FM-D01-91', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('151', 'poi_englishname', 'officalStandardEngName', 'FM-D01-81', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('152', 'poi_englishname', 'officalStandardEngName', 'FM-D01-85', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('153', 'poi_englishname', 'officalStandardEngName', 'FM-D01-105', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('154', 'poi_englishname', 'officalStandardEngName', 'FM-D01-103', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('155', 'poi_englishname', 'officalStandardEngName', 'FM-D01-62', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('156', 'poi_englishname', 'officalStandardEngName', 'FM-D01-95', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('157', 'poi_englishname', 'officalStandardEngName', 'FM-D01-87', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('158', 'poi_englishname', 'officalStandardEngName', 'FM-D01-77', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('159', 'poi_englishname', 'officalStandardEngName', 'FM-D01-49', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('160', 'poi_englishname', 'officalStandardEngName', 'FM-D01-45', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('172', 'poi_englishname', 'photoEngName', 'FM-D01-104', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('161', 'poi_englishname', 'photoEngName', 'FM-D01-52', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('163', 'poi_englishname', 'photoEngName', 'FM-D01-80', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('164', 'poi_englishname', 'photoEngName', 'FM-D01-48', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('165', 'poi_englishname', 'photoEngName', 'FM-D01-76', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('166', 'poi_englishname', 'photoEngName', 'FM-D01-90', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('167', 'poi_englishname', 'photoEngName', 'FM-D01-64', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('168', 'poi_englishname', 'photoEngName', 'FM-D01-102', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('169', 'poi_englishname', 'photoEngName', 'FM-D01-68', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('170', 'poi_englishname', 'photoEngName', 'FM-D01-72', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('171', 'poi_englishname', 'photoEngName', 'FM-D01-98', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('162', 'poi_englishname', 'photoEngName', 'FM-D01-86', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('173', 'poi_englishname', 'photoEngName', 'FM-D01-60', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('174', 'poi_englishname', 'photoEngName', 'FM-D01-94', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('175', 'poi_englishname', 'photoEngName', 'FM-D01-56', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('176', 'poi_englishname', 'photoEngName', 'FM-D01-44', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('177', 'poi_englishname', 'photoEngName', 'FM-D01-84', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('190', 'poi_name', 'aliasName', 'FM-D01-19', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('185', 'poi_name', 'aliasName', 'FM-D01-13', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('186', 'poi_name', 'aliasName', 'FM-D01-28', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('187', 'poi_name', 'aliasName', 'FM-D01-38', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('188', 'poi_name', 'aliasName', 'FM-D01-25', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('180', 'poi_name', 'aliasName', 'GLM60339', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('181', 'poi_name', 'aliasName', 'FM-D01-42', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('182', 'poi_name', 'aliasName', 'FM-D01-10', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('183', 'poi_name', 'aliasName', 'FM-D01-22', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('179', 'poi_name', 'aliasName', 'GLM60437', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('184', 'poi_name', 'aliasName', 'GLM60439', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('178', 'poi_name', 'aliasName', 'FM-D01-35', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('189', 'poi_name', 'aliasName', 'FM-D01-16', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('195', 'poi_name', 'namePinyin', 'GLM60415', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('191', 'poi_name', 'namePinyin', 'FM-D01-31', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('193', 'poi_name', 'namePinyin', 'FM-D01-33', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('194', 'poi_name', 'namePinyin', 'FM-D01-30', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('192', 'poi_name', 'namePinyin', 'FM-D01-32', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('200', 'poi_name', 'nameUnify', 'FM-D01-34', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('209', 'poi_name', 'nameUnify', 'GLM60003', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('210', 'poi_name', 'nameUnify', 'FM-D01-21', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('201', 'poi_name', 'nameUnify', 'FM-D01-37', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('202', 'poi_name', 'nameUnify', 'FM-D01-27', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('206', 'poi_name', 'nameUnify', 'FM-D01-09', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('207', 'poi_name', 'nameUnify', 'GLM60048', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('208', 'poi_name', 'nameUnify', 'GLM60069', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('205', 'poi_name', 'nameUnify', 'GLM60158', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('197', 'poi_name', 'nameUnify', 'FM-D01-24', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('203', 'poi_name', 'nameUnify', 'FM-D01-15', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('204', 'poi_name', 'nameUnify', 'FM-D01-18', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('199', 'poi_name', 'nameUnify', 'GLM60414', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('196', 'poi_name', 'nameUnify', 'FM-D01-41', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('198', 'poi_name', 'nameUnify', 'FM-D01-12', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('229', 'poi_name', 'shortName', 'GLM60331', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('214', 'poi_name', 'shortName', 'FM-D01-40', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('223', 'poi_name', 'shortName', 'FM-D01-17', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('213', 'poi_name', 'shortName', 'FM-D01-43', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('215', 'poi_name', 'shortName', 'FM-D01-39', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('216', 'poi_name', 'shortName', 'FM-D01-36', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('221', 'poi_name', 'shortName', 'FM-D01-23', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('218', 'poi_name', 'shortName', 'GLM60304', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('227', 'poi_name', 'shortName', 'FM-D01-14', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('226', 'poi_name', 'shortName', 'FM-D01-11', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('219', 'poi_name', 'shortName', 'FM-D01-29', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('220', 'poi_name', 'shortName', 'FM-D01-26', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('224', 'poi_name', 'shortName', 'FM-D01-20', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('217', 'poi_name', 'shortName', 'GLM60407', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('230', 'poi_englishaddress', 'nonImportantLongEngAddress', 'FM-GLM60189', 3, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('241', 'poi_deep', 'deepDetail', 'FM-M-DP-001', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('242', 'poi_deep', 'deepParking', 'FM-ZY-20-153', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('243', 'poi_deep', 'deepDetail', 'FM-YW-20-218', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('244', 'poi_deep', 'deepDetail', 'FM-ZY-20-237', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('245', 'poi_deep', 'deepDetail', 'FM-YW-20-219', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('246', 'poi_deep', 'deepDetail', 'FM-YW-20-220', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('247', 'poi_deep', 'deepDetail', 'FM-YW-20-221', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('248', 'poi_deep', 'deepParking', 'FM-ZY-20-155', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('249', 'poi_deep', 'deepCarrental', 'FM-ZY-20-198', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('250', 'poi_deep', 'deepCarrental', 'FM-ZY-20-199', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('251', 'poi_deep', 'deepCarrental', 'FM-ZY-20-238', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('252', 'poi_deep', 'deepParking', 'FM-YW-20-227', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('253', 'poi_deep', 'deepParking', 'FM-YW-20-235', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('254', 'poi_deep', 'deepDetail', 'FM-YW-20-222', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('255', 'poi_deep', 'deepParking', 'FM-YW-20-224', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('231', 'poi_deep', 'deepParking', 'FM-ZY-20-149', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('232', 'poi_deep', 'deepParking', 'FM-ZY-20-151', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('233', 'poi_deep', 'deepParking', 'FM-ZY-20-152', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('234', 'poi_deep', 'deepParking', 'FM-ZY-20-154', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('235', 'poi_deep', 'deepDetail', 'FM-M-DP-003', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('236', 'poi_deep', 'deepParking', 'FM-M-DP-005', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('237', 'poi_deep', 'deepParking', 'FM-M-DP-008', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('238', 'poi_deep', 'deepCarrental', 'FM-M-DP-009', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('239', 'poi_deep', 'deepCarrental', 'FM-M-DP-011', 2, 1);

insert into poi_column_workitem_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
values ('240', 'poi_deep', 'deepCarrental', 'FM-M-DP-013', 2, 1);

COMMIT;

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('1', 'poi_name', 'namePinyin', 0, '', 0, '', 0, '', 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,FM-D01-30,FM-D01-31,FM-D01-32,FM-D01-33,GLM60415', 1, 'FM-YW-20-052', 1, 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,FM-A07-02-01,FM-14Sum-12-03,FM-14Sum-12-08,FM-A04-12,FM-A04-13,FM-CHR73001,FM-GLM60154,FM-GLM60211,FM-A04-17,FM-A04-19,FM-A04-20,FM-A07-14,FM-GLM60254,FM-GLM60304,FM-GLM60407,FM-YW-20-036,FM-YW-20-058,FM-YW-20-060,FM-YW-20-146,FM-YW-20-147,FM-YW-20-148,FM-YW-20-149', 1, 'FM-YW-20-052');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('2', 'poi_name', 'nameUnify', 1, 'FM-BAT-20-141', 1, 'FM-A07-02,FM-M01-01,FM-A04-18', 1, 'FM-A07-02,FM-M01-01,FM-A04-18', 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,GLM60069,FM-D01-09,FM-D01-12,FM-D01-15,FM-D01-18,FM-D01-21,FM-D01-24,FM-D01-27,FM-D01-34,FM-D01-37,GLM60158,GLM60003,FM-D01-41,GLM60414,GLM60048', 1, 'FM-YW-20-052', 1, 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,FM-A07-02-01,FM-14Sum-12-03,FM-14Sum-12-08,FM-A04-12,FM-A04-13,FM-CHR73001,FM-GLM60154,FM-GLM60211,FM-A04-17,FM-A04-19,FM-A04-20,FM-A07-14,FM-GLM60254,FM-GLM60304,FM-GLM60407,FM-YW-20-036,FM-YW-20-058,FM-YW-20-060,FM-YW-20-146,FM-YW-20-147,FM-YW-20-148,FM-YW-20-149', 1, 'FM-YW-20-052');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('3', 'poi_name', 'aliasName', 1, 'FM-BAT-20-141', 1, 'FM-A04-18', 1, 'FM-A04-18', 0, '', 1, 'FM-D01-10,FM-D01-13,FM-D01-16,FM-D01-19,FM-D01-22,FM-D01-25,FM-D01-28,FM-D01-35,FM-D01-38,GLM60339,GLM60439,GLM60437,FM-D01-42', 0, '', 1, 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,FM-A07-02-01,FM-14Sum-12-03,FM-14Sum-12-08,FM-A04-12,FM-A04-13,FM-CHR73001,FM-GLM60154,FM-GLM60211,FM-A04-17,FM-A04-19,FM-A04-20,FM-A07-14,FM-GLM60254,FM-GLM60304,FM-GLM60407,FM-YW-20-036,FM-YW-20-058,FM-YW-20-060,FM-YW-20-146,FM-YW-20-147,FM-YW-20-148,FM-YW-20-149', 1, 'FM-YW-20-052');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('4', 'poi_name', 'shortName', 1, 'FM-BAT-20-141', 1, 'FM-M01-01,FM-A04-18', 1, 'FM-A04-18', 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,FM-D01-11,FM-D01-14,FM-D01-17,GLM60331,FM-D01-20,FM-D01-23,FM-D01-26,FM-D01-29,GLM60304,GLM60407,FM-D01-36,FM-D01-39,FM-D01-40,FM-D01-43', 1, 'FM-YW-20-052', 1, 1, 'FM-BAT-20-145,FM-BAT-20-115', 1, 'FM-YW-20-052,FM-A07-02-01,FM-14Sum-12-03,FM-14Sum-12-08,FM-A04-12,FM-A04-13,FM-CHR73001,FM-GLM60154,FM-GLM60211,FM-A04-17,FM-A04-19,FM-A04-20,FM-A07-14,FM-GLM60254,FM-GLM60304,FM-GLM60407,FM-YW-20-036,FM-YW-20-058,FM-YW-20-060,FM-YW-20-146,FM-YW-20-147,FM-YW-20-148,FM-YW-20-149', 1, 'FM-YW-20-052');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('5', 'poi_address', 'addrPinyin', 0, '', 0, '', 0, '', 0, '', 1, 'FM-A09-16,FM-A09-17,FM-YW-20-008', 0, '', 1, 1, 'FM-BAT-20-125,FM-BAT-20-164,FM-BAT-M01-05', 1, 'FM-ZY-20-136,FM-ZY-20-137,FM-ZY-20-138,FM-14Sum-06-03,FM-A09-13,FM-A09-14,FM-A09-03,FM-A09-04,FM-A09-05,FM-A09-06,FM-A09-07,FM-A09-08,FM-A09-09,FM-A09-10,FM-A09-11,FM-A09-12,FM-A09-16,FM-A09-17,FM-CHR73003,FM-CHR73002,FM-YW-20-008,FM-YW-20-009,FM-GLM60302,FM-YW-20-059,FM-YW-20-061,FM-YW-20-065,FM-GLM60377,FM-YW-20-038,FM-YW-20-039,FM-ZY-20-135,FM-YW-20-078,FM-YW-20-079,FM-YW-20-080,FM-YW-20-081,FM-YW-20-082,FM-YW-20-083', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('6', 'poi_address', 'addrSplit', 1, 'FM-BAT-20-142', 1, 'FM-YW-20-026', 1, 'FM-YW-20-026', 1, 'FM-BAT-20-125,FM-BAT-20-164,FM-BAT-M01-05', 1, 'FM-GLM60189,FM-14Sum-06-03,FM-A09-13,FM-A09-14,GLM60181,GLM60188,GLM60377,FM-YW-20-061,FM-YW-20-065,GLM60442,FM-A09-04,FM-A09-05,GLM60115,FM-A09-07,GLM60117,GLM60119,GLM60186,FM-YW-20-079,FM-YW-20-080,FM-YW-20-081,FM-CHR73002,FM-YW-20-038,GLM60480,GLM60481,FM-D01-108,FM-D01-109,FM-D01-110,FM-D01-111', 1, 'FM-GLM60189', 1, 1, 'FM-BAT-20-125,FM-BAT-20-164,FM-BAT-M01-05', 1, 'FM-ZY-20-136,FM-ZY-20-137,FM-ZY-20-138,FM-14Sum-06-03,FM-A09-13,FM-A09-14,FM-A09-03,FM-A09-04,FM-A09-05,FM-A09-06,FM-A09-07,FM-A09-08,FM-A09-09,FM-A09-10,FM-A09-11,FM-A09-12,FM-A09-16,FM-A09-17,FM-CHR73003,FM-CHR73002,FM-YW-20-008,FM-YW-20-009,FM-GLM60302,FM-YW-20-059,FM-YW-20-061,FM-YW-20-065,FM-GLM60377,FM-YW-20-038,FM-YW-20-039,FM-ZY-20-135,FM-YW-20-078,FM-YW-20-079,FM-YW-20-080,FM-YW-20-081,FM-YW-20-082,FM-YW-20-083', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('7', 'poi_englishname', 'netEngName', 1, 'FM-BAT-20-147', 1, 'FM-YW-20-053', 1, 'FM-YW-20-053', 1, 'FM-BAT-20-163', 1, 'FM-GLM60335,FM-CHR71011,FM-GLM60174,FM-GLM60173,FM-GLM60178,FM-GLM60172,FM-GLM60314,FM-GLM60996,FM-YW-20-028,FM-YW-20-033,FM-GLM60406,FM-YW-20-050,FM-YW-20-084,FM-YW-20-085,FM-YW-20-086,FM-YW-20-087,FM-YW-20-088,FM-YW-20-090,FM-YW-20-091,FM-YW-20-092', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('8', 'poi_englishname', 'photoEngName', 1, 'FM-BAT-20-147', 1, 'FM-YW-20-053', 1, 'FM-YW-20-053', 1, 'FM-BAT-20-163', 1, 'FM-D01-44,FM-D01-48,FM-D01-52,FM-D01-56,FM-D01-60,FM-D01-64,FM-D01-68,FM-D01-72,FM-D01-76,FM-D01-80,FM-D01-84,FM-D01-86,FM-D01-90,FM-D01-94,FM-D01-98,FM-D01-102,FM-D01-104', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('9', 'poi_englishname', 'officalStandardEngName', 0, '', 0, '', 0, '', 1, 'FM-BAT-20-163,FM-BAT-M01-09', 1, 'FM-D01-45,FM-D01-49,FM-D01-53,FM-D01-57,FM-D01-62,FM-D01-65,FM-D01-69,FM-D01-73,FM-D01-77,FM-D01-81,FM-D01-85,FM-D01-87,FM-D01-91,FM-D01-95,FM-D01-99,FM-D01-103,FM-D01-105', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('10', 'poi_englishname', 'nonImportantLongEngName', 0, '', 0, '', 0, '', 1, 'FM-BAT-20-163', 1, 'FM-D01-44,FM-D01-48,FM-D01-52,FM-D01-56,FM-D01-60,FM-D01-64,FM-D01-68,FM-D01-72,FM-D01-76,FM-D01-80,FM-D01-84,FM-D01-86,FM-D01-90,FM-D01-94,FM-D01-98,FM-D01-102,FM-D01-104', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('11', 'poi_englishname', 'chiEngName', 1, 'FM-BAT-20-147', 1, 'FM-YW-20-053', 1, 'FM-YW-20-053', 1, 'FM-BAT-20-163', 1, 'FM-D01-44,FM-D01-48,FM-D01-52,FM-D01-56,FM-D01-60,FM-D01-64,FM-D01-68,FM-D01-72,FM-D01-76,FM-D01-80,FM-D01-84,FM-D01-86,FM-D01-90,FM-D01-94,FM-D01-98,FM-D01-102,FM-D01-104', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('12', 'poi_englishname', 'confirmEngName', 1, 'FM-BAT-20-147', 1, 'FM-YW-20-053', 1, 'FM-YW-20-053', 1, 'FM-BAT-20-163', 1, 'FM-D01-44,FM-D01-48,FM-D01-52,FM-D01-56,FM-D01-60,FM-D01-64,FM-D01-68,FM-D01-72,FM-D01-76,FM-D01-80,FM-D01-84,FM-D01-86,FM-D01-90,FM-D01-94,FM-D01-98,FM-D01-102,FM-D01-104', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('13', 'poi_englishname', 'aliasOriEngName', 1, 'FM-BAT-M01-06', 1, 'FM-M01-04', 1, 'FM-M01-04', 1, 'FM-BAT-M01-02', 1, 'FM-GLM60335,FM-CHR71011,FM-GLM60174,FM-GLM60173,FM-GLM60178,FM-GLM60172,FM-GLM60314,FM-GLM60996,FM-YW-20-028,FM-YW-20-033,FM-GLM60406,FM-YW-20-050,FM-YW-20-084,FM-YW-20-085,FM-YW-20-086,FM-YW-20-087,FM-YW-20-088,FM-YW-20-090,FM-YW-20-091,FM-YW-20-092', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('14', 'poi_englishname', 'aliasStdEngName', 0, '', 0, '', 0, '', 0, '', 1, 'FM-GLM60335,FM-CHR71011,FM-GLM60174,FM-GLM60173,FM-GLM60178,FM-GLM60172,FM-GLM60314,FM-GLM60996,FM-YW-20-028,FM-YW-20-033,FM-GLM60406,FM-YW-20-050,FM-YW-20-084,FM-YW-20-085,FM-YW-20-086,FM-YW-20-087,FM-YW-20-088,FM-YW-20-090,FM-YW-20-091,FM-YW-20-092', 0, '', 1, 0, '', 0, '', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('15', 'poi_englishname', 'nonImportantEngAddress', 0, '', 0, '', 0, '', 1, 'FM-BAT-20-163', 1, 'FM-GLM60335,FM-CHR71011,FM-GLM60174,FM-GLM60173,FM-GLM60178,FM-GLM60172,FM-GLM60314,FM-GLM60996,FM-YW-20-028,FM-YW-20-033,FM-GLM60406,FM-YW-20-050,FM-YW-20-084,FM-YW-20-085,FM-YW-20-086,FM-YW-20-087,FM-YW-20-088,FM-YW-20-090,FM-YW-20-091,FM-YW-20-092', 0, '', 1, 1, 'FM-BAT-20-163', 1, 'FM-GLM60335,FM-CHR71011,FM-GLM60174,FM-GLM60173,FM-GLM60178,FM-GLM60172,FM-GLM60314,FM-GLM60996,FM-YW-20-028,FM-YW-20-033,FM-GLM60406,FM-YW-20-050,FM-YW-20-084,FM-YW-20-085,FM-YW-20-086,FM-YW-20-087,FM-YW-20-088,FM-YW-20-090,FM-YW-20-091,FM-YW-20-092', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('16', 'poi_englishaddress', 'engMapAddress', 0, '', 0, '', 0, '', 0, '', 1, 'GLM60190,FM-YW-20-029,FM-YW-20-034,FM-YW-20-064,FM-YW-20-099,FM-YW-20-101,FM-YW-20-212,GLM60335,FM-YW-20-100,FM-YW-20-110,FM-YW-20-115,FM-YW-20-117,FM-YW-20-118,FM-CHR71040', 0, '', 1, 0, '', 1, 'FM-CHR71040,FM-GLM60190,FM-YW-20-034,FM-YW-20-029,FM-YW-20-064,FM-GLM60189,FM-YW-20-099,FM-YW-20-100,FM-YW-20-101,FM-YW-20-110,FM-YW-20-115,FM-YW-20-117,FM-YW-20-118,FM-YW-20-212', 0, '');

insert into poi_column_op_conf (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, SAVE_EXEBATCH, SAVE_BATCHRULES, SAVE_EXECHECK, SAVE_CKRULES, SAVE_EXECLASSIFY, SAVE_CLASSIFYRULES, SUBMIT_EXEBATCH, SUBMIT_BATCHRULES, SUBMIT_EXECHECK, SUBMIT_CKRULES, SUBMIT_EXECLASSIFY, SUBMIT_CLASSIFYRULES, TYPE, FIRST_SUBMIT_EXEBATCH, FIRST_SUBMIT_BATCHRULES, FIRST_SUBMIT_EXECHECK, FIRST_SUBMIT_CKRULES, FIRST_SUBMIT_EXECLASSIFY, FIRST_SUBMIT_CLASSIFYRULES)
values ('17', 'poi_englishaddress', 'nonImportantLongEngAddress', 0, '', 0, '', 0, '', 0, '', 1, 'FM-GLM60189,FM-YW-20-029,FM-YW-20-034,FM-YW-20-064,FM-YW-20-099,FM-YW-20-101,GLM60335,FM-YW-20-100,FM-YW-20-110,FM-YW-20-115,FM-YW-20-117,FM-YW-20-118,FM-CHR71040', 0, '', 1, 0, '', 1, 'FM-CHR71040,FM-GLM60190,FM-YW-20-034,FM-YW-20-029,FM-YW-20-064,FM-GLM60189,FM-YW-20-099,FM-YW-20-100,FM-YW-20-101,FM-YW-20-110,FM-YW-20-115,FM-YW-20-117,FM-YW-20-118,FM-YW-20-212', 0, '');

COMMIT;