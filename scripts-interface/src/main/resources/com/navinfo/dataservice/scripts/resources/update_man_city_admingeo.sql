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
create table AD_ADMIN_NAME  (
   NAME_ID              NUMBER(10)                      not null,
   NAME_GROUPID         NUMBER(10)                     default 0 not null,
   REGION_ID            NUMBER(10)                      not null,
   LANG_CODE            VARCHAR2(3)                    default 'CHI' not null
       check (LANG_CODE in ('CHI','CHT','ENG','POR','ARA','BUL','CZE','DAN','DUT','EST','FIN','FRE','GER','HIN','HUN','ICE','IND','ITA','JPN','KOR','LIT','NOR','POL','RUM','RUS','SLO','SPA','SWE','THA','TUR','UKR','SCR')),
   NAME_CLASS           NUMBER(1)                      default 1 not null
       check (NAME_CLASS in (1,2,3,4)),
   NAME                 VARCHAR2(120),
   PHONETIC             VARCHAR2(1000),
   SRC_FLAG             NUMBER(2)                      default 0 not null
       check (SRC_FLAG in (0,1,2,3,4,5,6)),
   U_RECORD             NUMBER(2)                      default 0 not null
       check (U_RECORD in (0,1,2,3)),
   U_FIELDS             VARCHAR2(1000),
   U_DATE               VARCHAR2(14),
   ROW_ID               RAW(16),
   constraint PK_AD_ADMIN_NAME primary key (NAME_ID),
   constraint ADADMIN_NAMES foreign key (REGION_ID)
         references AD_ADMIN (REGION_ID)
);
INSERT INTO AD_ADMIN SELECT * FROM AD_ADMIN@FMGDB_LINK;
INSERT INTO AD_ADMIN_NAME SELECT * FROM AD_ADMIN_NAME@FMGDB_LINK;
COMMIT;
UPDATE CITY C SET C.ADMIN_ID=(SELECT A.ADMIN_ID FROM AD_ADMIN A,AD_ADMIN_NAME N WHERE A.REGION_ID=N.REGION_ID AND A.ADMIN_TYPE>1 AND N.LANG_CODE='CHI' AND N.NAME=C.CITY_NAME);
UPDATE CITY C SET C.ADMIN_GEO=(SELECT A.GEOMETRY FROM AD_ADMIN A,AD_ADMIN_NAME N WHERE A.REGION_ID=N.REGION_ID AND A.ADMIN_TYPE>1 AND N.LANG_CODE='CHI' AND N.NAME=C.CITY_NAME);
COMMIT;
DROP TABLE AD_ADMIN_NAME;
DROP TABLE AD_ADMIN;
DROP DATABASE LINK FMGDB_LINK;