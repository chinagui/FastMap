create table AD_ADMIN  (
   REGION_ID            NUMBER(10)                      not null,
   ADMIN_ID             NUMBER(6)                      default 0 not null,
   EXTEND_ID            NUMBER(4)                      default 0 not null,
   ADMIN_TYPE           NUMBER(3,1)                    default 0 not null
       check (ADMIN_TYPE in (0,1,2,2.5,3,3.5,4,4.5,4.8,5,6,7,8,9)),
   CAPITAL              NUMBER(1)                      default 0 not null
       check (CAPITAL in (0,1,2,3)),
   POPULATION           VARCHAR2(8),
   GEOMETRY             SDO_GEOMETRY,
   LINK_PID             NUMBER(10)                     default 0 not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   SIDE                 NUMBER(1)                      default 0 not null
       check (SIDE in (0,1,2,3)),
   ROAD_FLAG            NUMBER(1)                      default 0 not null
       check (ROAD_FLAG in (0,1,2,3)),
   PMESH_ID             NUMBER(8)                      default 0 not null,
   JIS_CODE             NUMBER(5)                      default 0 not null,
   MESH_ID              NUMBER(8)                      default 0 not null,
   EDIT_FLAG            NUMBER(1)                      default 1 not null
       check (EDIT_FLAG in (0,1)),
   MEMO                 VARCHAR2(200),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_ADMIN primary key (REGION_ID)
);
INSERT INTO AD_ADMIN SELECT * FROM AD_ADMIN@FMGDB_LINK;
ALTER TABLE AD_ADMIN ADD NAME VARCHAR2(32);
UPDATE AD_ADMIN A SET A.NAME=(SELECT N.NAME FROM AD_ADMIN_NAME@FMGDB_LINK N WHERE A.REGION_ID=N.REGION_ID AND N.LANG_CODE='CHI' AND NAME_CLASS=1);
COMMIT;
UPDATE CITY C SET C.ADMIN_GEO=(SELECT T.GEOMETRY FROM (
SELECT T1.ADMIN_ID PROV_ADMIN_ID,
       T1.NAME     PROV_NAME,
       T2.GEOMETRY,
       T2.NAME     CITY_NAME
  FROM (SELECT ADMIN_ID,NAME FROM AD_ADMIN WHERE ADMIN_TYPE=1) T1,
       (SELECT TO_NUMBER(SUBSTR(ADMIN_ID, 0, 2)) * 10000 PROV_ADMIN_ID,GEOMETRY,NAME FROM AD_ADMIN WHERE ADMIN_TYPE>1 AND ADMIN_TYPE<4)T2
 WHERE T1.ADMIN_ID = T2.PROV_ADMIN_ID
 ) T WHERE C.CITY_NAME=T.CITY_NAME AND C.PROVINCE_NAME=T.PROV_NAME);
COMMIT;
DROP TABLE AD_ADMIN;
DROP DATABASE LINK FMGDB_LINK;