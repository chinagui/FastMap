-- CREATE TABLES
--ci_para_rd_name
CREATE TABLE CI_PARA_RD_NAME(
  NAME VARCHAR2(50) DEFAULT NULL,
  ADMIN_ID NUMBER(10) DEFAULT NULL,
  NAME_LEN NUMBER(10) DEFAULT NULL
);
CREATE INDEX IX_CI_PARA_AD_ADMIN  ON  CI_PARA_RD_NAME(ADMIN_ID);
CREATE INDEX IX_CI_PARA_RD_NAME_LEN  ON  CI_PARA_RD_NAME(NAME_LEN);

-- CI_PARA_IX_POI_ADDRESS_STREET
CREATE TABLE CI_PARA_IX_POI_ADDRESS_STREET(
  STREET VARCHAR2(50) DEFAULT NULL,
  REGION_ID NUMBER(10) DEFAULT NULL,
  ADMIN_ID NUMBER(10) DEFAULT NULL,
  SRC VARCHAR2(50) DEFAULT NULL,
  STREET_LEN NUMBER(10) DEFAULT NULL
);
CREATE INDEX IX_CI_PARA_POIADDR_STR  ON  CI_PARA_IX_POI_ADDRESS_STREET(ADMIN_ID);
CREATE INDEX IX_CI_PARA_POIADDR_STRLEN  ON  CI_PARA_IX_POI_ADDRESS_STREET(STREET_LEN);

-- CI_PARA_IX_POI_ADDRESS_PLACE
CREATE TABLE CI_PARA_IX_POI_ADDRESS_PLACE(
  PLACE VARCHAR2(50) DEFAULT NULL,
  REGION_ID NUMBER(10) DEFAULT NULL,
  ADMIN_ID NUMBER(10) DEFAULT NULL,
  SRC VARCHAR2(50) DEFAULT NULL,
  PLACE_LEN NUMBER(10) DEFAULT NULL
);
CREATE INDEX IX_CI_PARA_POIADDR_PLA  ON  CI_PARA_IX_POI_ADDRESS_PLACE(ADMIN_ID);
CREATE INDEX IX_CI_PARA_POIADDR_PLALEN  ON  CI_PARA_IX_POI_ADDRESS_PLACE(PLACE_LEN);

-- CI_PARA_AD_ADMIN
CREATE TABLE CI_PARA_AD_ADMIN (
  REGIONID NUMBER(10) DEFAULT NULL,
  PROVNM VARCHAR2(50) DEFAULT NULL,
  CITYNM VARCHAR2(50) DEFAULT NULL,
  XIANNM varchar2(50) DEFAULT NULL,
  WHOLENM varchar2(50) DEFAULT NULL,
  ADMIN_ID NUMBER(10) DEFAULT NULL,
  WHOLENM_LEN NUMBER(10) DEFAULT NULL,
  ADMIN_TYPE NUMBER(10) DEFAULT NULL
);
CREATE INDEX IX_CI_PARA_ADADMIN_ID  ON  CI_PARA_AD_ADMIN(ADMIN_ID);
CREATE INDEX IX_CI_PARA_ADADMIN_NMLEN  ON  CI_PARA_AD_ADMIN(WHOLENM_LEN);
CREATE INDEX IX_CI_PARA_ADADMIN_IDTYPE  ON  CI_PARA_AD_ADMIN(ADMIN_ID,ADMIN_TYPE);

-- init rows
--CI_PARA_RD_NAME
INSERT INTO CI_PARA_RD_NAME (
SELECT NAME, ADMIN_ID,LENGTHB(NAME)
          FROM rd_name
         WHERE (src_resume IS NULL OR src_resume <> '点门牌') AND (lang_code = 'CHI' OR lang_code = 'CHT')
               AND TYPE IN
                      ('步行街',
                       '村道',
                       '大道',
                       '大街',
                       '大路',
                       '大馬路',
                       '大巷',
                       '道',
                       '复线',
                       '干道',
                       '干线',
                       '高架路',
                       '高速',
                       '高速公路',
                       '公路',
                       '横街',
                       '横路',
                       '橫街',
                       '胡同',
                       '环',
                       '环路',
                       '环线',
                       '夹道',
                       '街',
                       '街子',
                       '快速',
                       '快速干线',
                       '快速公路',
                       '快速路',
                       '联络道',
                       '路',
                       '马路',
                       '馬路',
                       '弄',
                       '繞道',
                       '商贸街',
                       '商业街',
                       '食街',
                       '条',
                       '线',
                       '巷',
                       '巷子',
                       '小道',
                       '小街',
                       '小路',
                       '小巷',
                       '斜街',
                       '斜路',
                       '斜巷',
                       '新街',
                       '新路',
                       '新巷',
                       '匝道',
                       '正街',
                       '支路',
                       '支线',
                       '直街')
        UNION
        SELECT NAME, admin_id,LENGTHB(NAME)
          FROM rd_name
         WHERE (src_resume IS NULL OR src_resume <> '点门牌') AND (lang_code = 'CHI' OR lang_code = 'CHT') AND TYPE = '*' AND REGEXP_LIKE (NAME, '(段|道中|道东|道南|道西|道北)$'));
-- CI_PARA_IX_POI_ADDRESS_STREET
INSERT INTO CI_PARA_IX_POI_ADDRESS_STREET (
SELECT DISTINCT A.STREET,B.REGION_ID,C.ADMIN_ID,'ix_poi_address',LENGTHB(A.STREET)
  FROM IX_POI_ADDRESS A, IX_POI B, AD_ADMIN C
 WHERE A.POI_PID = B.PID AND A.LANG_CODE IN ('CHI', 'CHT') AND STREET IS NOT NULL AND B.REGION_ID = C.REGION_ID
 union all
       SELECT DISTINCT A.STREET,B.REGION_ID,C.ADMIN_ID,'ix_poi_address',LENGTHB(A.STREET)
  FROM IX_POI_ADDRESS A, IX_POI B, AD_ADMIN C
 WHERE A.POI_PID = B.PID AND A.LANG_CODE IN ('CHI', 'CHT') AND STREET IS NOT NULL AND B.REGION_ID = C.REGION_ID);
-- CI_PARA_IX_POI_ADDRESS_PLACE
INSERT INTO CI_PARA_IX_POI_ADDRESS_PLACE (
SELECT PLACE,REGION_ID,ADMIN_ID,SRC,LENGTHB(PLACE)
  FROM (SELECT DISTINCT A.PLACE, B.REGION_ID, C.ADMIN_ID, 'ix_poi_address' AS SRC
          FROM IX_POI_ADDRESS A, IX_POI B, AD_ADMIN C
         WHERE A.POI_PID = B.PID AND A.LANG_CODE IN ('CHI', 'CHT') AND (PLACE IS NOT NULL) AND B.REGION_ID = C.REGION_ID
        UNION
        SELECT B.NAME AS PLACE, A.REGION_ID, C.ADMIN_ID, 'ix_poi_name' AS SRC
          FROM IX_POI A, IX_POI_NAME B, AD_ADMIN C
         WHERE A.PID = B.POI_PID AND A.KIND_CODE = '120201' AND B.LANG_CODE IN ('CHI', 'CHT') AND A.REGION_ID = C.REGION_ID)
union all
SELECT PLACE,REGION_ID,ADMIN_ID,SRC,LENGTHB(PLACE)
  FROM (SELECT DISTINCT A.PLACE, B.REGION_ID, C.ADMIN_ID, 'ix_poi_address' AS SRC
          FROM IX_POI_ADDRESS A, IX_POI B, AD_ADMIN C
         WHERE A.POI_PID = B.PID AND A.LANG_CODE IN ('CHI', 'CHT') AND (PLACE IS NOT NULL) AND B.REGION_ID = C.REGION_ID
        UNION
        SELECT B.NAME AS PLACE, A.REGION_ID, C.ADMIN_ID, 'ix_poi_name' AS SRC
          FROM IX_POI A, IX_POI_NAME B, AD_ADMIN C
         WHERE A.PID = B.POI_PID AND A.KIND_CODE = '120201' AND B.LANG_CODE IN ('CHI', 'CHT') AND A.REGION_ID = C.REGION_ID));
-- CI_PARA_AD_ADMIN
INSERT INTO CI_PARA_AD_ADMIN SELECT * FROM (   
WITH RS_hm
     AS (SELECT /*+ no_merge */ A1.REGION_ID AS REGIONID, N1.NAME AS PROVNM, NULL AS CITYNM, NULL AS XIANNM, N1.NAME AS WHOLENM, A1.ADMIN_ID, LENGTH (N1.NAME) AS WHOLENM_LEN, A1.ADMIN_TYPE
           FROM AD_ADMIN A1, AD_ADMIN_NAME N1
          WHERE A1.ADMIN_TYPE >= 1 AND A1.ADMIN_TYPE < 2 AND A1.REGION_ID = N1.REGION_ID AND N1.LANG_CODE = 'CHT'
         UNION ALL
         SELECT A2.REGION_ID, N1.NAME, N2.NAME, NULL, N1.NAME || N2.NAME, A2.ADMIN_ID, LENGTH (N1.NAME || N2.NAME) AS WHOLENM_LEN, A2.ADMIN_TYPE
           FROM AD_ADMIN A1, AD_ADMIN_NAME N1, AD_ADMIN_GROUP G1, AD_ADMIN_PART P1, AD_ADMIN A2, AD_ADMIN_NAME N2
          WHERE     A1.ADMIN_TYPE >= 1
                AND A1.ADMIN_TYPE < 2
                AND A1.REGION_ID = N1.REGION_ID
                AND N1.LANG_CODE = 'CHT'
                AND A1.REGION_ID = G1.REGION_ID_UP
                AND G1.GROUP_ID = P1.GROUP_ID
                AND P1.REGION_ID_DOWN = A2.REGION_ID
                AND A2.REGION_ID = N2.REGION_ID
                AND N2.LANG_CODE = 'CHT'
         UNION ALL
         SELECT A3.REGION_ID, N1.NAME, N2.NAME, N3.NAME, N1.NAME || N2.NAME || N3.NAME, A3.ADMIN_ID, LENGTH (N1.NAME || N2.NAME || N3.NAME) AS WHOLENM_LEN, A3.ADMIN_TYPE
           FROM AD_ADMIN A1, AD_ADMIN_NAME N1, AD_ADMIN_GROUP G1, AD_ADMIN_PART P1, AD_ADMIN A2, AD_ADMIN_NAME N2, AD_ADMIN_GROUP G2, AD_ADMIN_PART P2, AD_ADMIN A3, AD_ADMIN_NAME N3
          WHERE     A1.ADMIN_TYPE >= 1
                AND A1.ADMIN_TYPE < 2
                AND A1.REGION_ID = N1.REGION_ID
                AND N1.LANG_CODE = 'CHT'
                AND A1.REGION_ID = G1.REGION_ID_UP
                AND G1.GROUP_ID = P1.GROUP_ID
                AND P1.REGION_ID_DOWN = A2.REGION_ID
                AND A2.REGION_ID = N2.REGION_ID
                AND N2.LANG_CODE = 'CHT'
                AND A2.REGION_ID = G2.REGION_ID_UP
                AND G2.GROUP_ID = P2.GROUP_ID
                AND P2.REGION_ID_DOWN = A3.REGION_ID
                AND A3.REGION_ID = N3.REGION_ID
                AND N3.LANG_CODE = 'CHT'),
     RS AS (SELECT /*+ no_merge */ A1.REGION_ID AS REGIONID, N1.NAME AS PROVNM, NULL AS CITYNM, NULL AS XIANNM, N1.NAME AS WHOLENM, A1.ADMIN_ID, LENGTH (N1.NAME) AS WHOLENM_LEN, A1.ADMIN_TYPE
           FROM AD_ADMIN A1, AD_ADMIN_NAME N1
          WHERE A1.ADMIN_TYPE >= 1 AND A1.ADMIN_TYPE < 2 AND A1.REGION_ID = N1.REGION_ID AND N1.LANG_CODE = 'CHI'
         UNION ALL
         SELECT A2.REGION_ID, N1.NAME, N2.NAME, NULL, N1.NAME || N2.NAME, A2.ADMIN_ID, LENGTH (N1.NAME || N2.NAME) AS WHOLENM_LEN, A2.ADMIN_TYPE
           FROM AD_ADMIN A1, AD_ADMIN_NAME N1, AD_ADMIN_GROUP G1, AD_ADMIN_PART P1, AD_ADMIN A2, AD_ADMIN_NAME N2
          WHERE     A1.ADMIN_TYPE >= 1
                AND A1.ADMIN_TYPE < 2
                AND A1.REGION_ID = N1.REGION_ID
                AND N1.LANG_CODE = 'CHI'
                AND A1.REGION_ID = G1.REGION_ID_UP
                AND G1.GROUP_ID = P1.GROUP_ID
                AND P1.REGION_ID_DOWN = A2.REGION_ID
                AND A2.REGION_ID = N2.REGION_ID
                AND N2.LANG_CODE = 'CHI'
         UNION ALL
         SELECT A3.REGION_ID, N1.NAME, N2.NAME, N3.NAME, N1.NAME || N2.NAME || N3.NAME, A3.ADMIN_ID, LENGTH (N1.NAME || N2.NAME || N3.NAME) AS WHOLENM_LEN, A3.ADMIN_TYPE
           FROM AD_ADMIN A1, AD_ADMIN_NAME N1, AD_ADMIN_GROUP G1, AD_ADMIN_PART P1, AD_ADMIN A2, AD_ADMIN_NAME N2, AD_ADMIN_GROUP G2, AD_ADMIN_PART P2, AD_ADMIN A3, AD_ADMIN_NAME N3
          WHERE     A1.ADMIN_TYPE >= 1
                AND A1.ADMIN_TYPE < 2
                AND A1.REGION_ID = N1.REGION_ID
                AND N1.LANG_CODE = 'CHI'
                AND A1.REGION_ID = G1.REGION_ID_UP
                AND G1.GROUP_ID = P1.GROUP_ID
                AND P1.REGION_ID_DOWN = A2.REGION_ID
                AND A2.REGION_ID = N2.REGION_ID
                AND N2.LANG_CODE = 'CHI'
                AND A2.REGION_ID = G2.REGION_ID_UP
                AND G2.GROUP_ID = P2.GROUP_ID
                AND P2.REGION_ID_DOWN = A3.REGION_ID
                AND A3.REGION_ID = N3.REGION_ID
                AND N3.LANG_CODE = 'CHI')             
SELECT DISTINCT REGIONID,PROVNM,CITYNM,XIANNM,WHOLENM,ADMIN_ID,LENGTHB(WHOLENM) , ADMIN_TYPE
  FROM RS
  union all
SELECT DISTINCT REGIONID, PROVNM,CITYNM,XIANNM,WHOLENM,ADMIN_ID,LENGTHB(WHOLENM) ,ADMIN_TYPE
  FROM RS_hm);
  
COMMIT;
EXIT;