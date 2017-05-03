CREATE OR REPLACE PACKAGE POINT_FEATURE_BATCH AUTHID CURRENT_USER AS
  --modify by ZhangRunze 20160820,��POI�����ĵ���������SIDE
  --����ת���ķ�������Ԫ������
  MOVE_DIS CONSTANT NUMBER := 5.1; --Ų����·���붨��

  TYPE GDB_GEOM IS RECORD(
    ID   NUMBER(20),
    GEOM SDO_GEOMETRY);

  TYPE GEOMCURSOR IS REF CURSOR RETURN GDB_GEOM;

  --����ת���ķ�����������
  TYPE GEO_MESH IS RECORD(
    ID        NUMBER(20),
    MESHID    NUMBER,
    MESHID_5K VARCHAR2(10));
  TYPE MESH_TAB IS TABLE OF GEO_MESH;

  FUNCTION BATCHMESHPARAFUN(DATACURSOR IN GEOMCURSOR) RETURN MESH_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION DATACURSOR BY ANY);

  TYPE RELATED_RES IS RECORD(
    PID      NUMBER(10),
    LINK_PID NUMBER,
    X        NUMBER(10, 5),
    Y        NUMBER(10, 5),
    SIDE     NUMBER(1));
  TYPE RELATED_LINK_TAB IS TABLE OF RELATED_RES;

  TYPE REFTYPECURSOR IS REF CURSOR;

  FUNCTION POIRELATEDLINK(DATACURSOR IN REFTYPECURSOR)
    RETURN RELATED_LINK_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION DATACURSOR BY ANY);

  /*������ͼ����---խ�ӿ�*/
  PROCEDURE BATCHMESH_IX_POI;
  PROCEDURE BATCHMESH_PT_POI;
  PROCEDURE BATCHMESH_IX_ANNOTATION_100W;
  PROCEDURE BATCHMESH_IX_ANNOTATION;
  PROCEDURE BATCHMESH_IX_POINTADDRESS;
  PROCEDURE BATCHMESH_IX_HAMLET;
  PROCEDURE BATCHMESH_IX_POSTCODE;

  --��ͼ���ſ�ӿ�
  PROCEDURE POP_BATCHMESH;

  --��������������--խ�ӿ�

  PROCEDURE PREP_AD_FACE_FOR_BATCH;

  PROCEDURE BATCH_BUSSTOP_ADMIN;
  PROCEDURE BATCH_DOORNUM_ADMIN;
  PROCEDURE BATCH_ANNOTATION_ADMIN;
  PROCEDURE BATCH_POSTCODE_ADMIN;
  PROCEDURE BATCH_POI_ADMIN;
  PROCEDURE BATCH_HAMLET_ADMIN;

  --�������·������
  PROCEDURE SHD_IX_POI_TEMP(P_TASK_NAME IN VARCHAR2 DEFAULT 'POINT_FEATURE_BATCH'); --��½ IX_POI������

  PROCEDURE BATCH_IXPOI_LINK(P_TASK_NAME VARCHAR2 DEFAULT 'POINT_FEATURE_BATCH'); --��½ IX_POI������

  PROCEDURE BATCH_PTPOI_LINK(P_TASK_NAME VARCHAR2 DEFAULT 'BATCH_PTPOI_LINK'); --���˵���վ�㽨����

  PROCEDURE BATCH_HAMLET_LINK(P_TASK_NAME VARCHAR2); --IX_HAMLET������

  PROCEDURE BATCH_ADMIN_LINK; --��������������

  PROCEDURE BATCH_DOORNUM_GUIDE_LINK(P_TASK_NAME        VARCHAR2 DEFAULT 'BATCH_DOORNUM_GUIDE_LINK',
                                     GUIDE_DISTANCE     IN NUMBER DEFAULT 50,
                                     AUC_GUIDE_DISTANCE IN NUMBER DEFAULT 3); --����������link

  PROCEDURE BATCH_ERASE_RELATE_LINK;

  PROCEDURE BATCH_POST_CODE_LINK(P_TASK_NAME VARCHAR2);
  PROCEDURE BATCH_HAMLET_INTO(P_TASK_NAME VARCHAR2); --IX_HAMLET��ĸ��
  PROCEDURE RUN_POICROSSLINK(P_TASK_NAME VARCHAR2);

  --����Ų��5mBuffer��POI��������
  FUNCTION CALC_MOVE_GEOMETRY(ORI_PGEOM IN MDSYS.SDO_GEOMETRY,
                              LGEOM     IN MDSYS.SDO_GEOMETRY,
                              SIDE      IN NUMBER) RETURN SDO_GEOMETRY;

  --��POI��ʾ�������·buffer��������� 
  PROCEDURE RUN_POIOUTBUFFER(P_TASK_NAME VARCHAR2);
  
  --��������ʾ�������·buffer������
  PROCEDURE RUN_POINTOUTBUFFER(P_TASK_NAME VARCHAR2);

END;
/
CREATE OR REPLACE PACKAGE BODY POINT_FEATURE_BATCH AS

  FUNCTION BATCHMESHPARAFUN(DATACURSOR IN GEOMCURSOR) RETURN MESH_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION DATACURSOR BY ANY) IS
    AGEOM     GDB_GEOM;
    MESHID    NUMBER;
    ARECMESH  GEO_MESH;
    MESHID_5K VARCHAR2(10);
  BEGIN
    LOOP
      FETCH DATACURSOR
        INTO AGEOM;
      EXIT WHEN DATACURSOR%NOTFOUND;
      IF AGEOM.GEOM IS NOT NULL THEN
        --GEOM����Ϊ��
        MESHID_5K := NAVI_GEOM.GET5KMAPNUMBER1(AGEOM.GEOM);
        IF MESHID_5K IS NULL THEN
          MESHID := NULL;
        ELSE
          MESHID := TO_NUMBER(SUBSTR(MESHID_5K, 1, 6));
        END IF;
      ELSE
        MESHID    := NULL;
        MESHID_5K := NULL;
      END IF;
      ARECMESH.ID        := AGEOM.ID;
      ARECMESH.MESHID    := MESHID;
      ARECMESH.MESHID_5K := MESHID_5K;
      PIPE ROW(ARECMESH);
    END LOOP;
    CLOSE DATACURSOR;
    RETURN;
  END;

  PROCEDURE BATCHMESH_IX_HAMLET IS
  BEGIN
    --IX_HAMLET
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+full(H) PARALLEL(H)*/
                                      H.PID, H.GEOMETRY
                                       FROM IX_HAMLET H)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_HAMLET H
    USING SHD_MESH M
    ON (H.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET H.MESH_ID = M.MESHID, H.MESH_ID_5K = M.MESHID_5K;
    COMMIT;
  END;

  PROCEDURE BATCHMESH_IX_ROAD IS
  BEGIN
    --IX_ROAD
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(R) PARALLEL(R)*/
                                      R.PID, R.GEOMETRY
                                       FROM IX_ROADNAME R)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_ROADNAME R
    USING SHD_MESH M
    ON (R.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET R.MESH_ID = M.MESHID, R.MESH_ID_5K = M.MESHID_5K;
    COMMIT;
  END;

  PROCEDURE BATCHMESH_IX_POINTADDRESS IS
  BEGIN
    --IX_POINTADDRESS
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(A) PARALLEL(A)*/
                                      A.PID, A.GEOMETRY
                                       FROM IX_POINTADDRESS A)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_POINTADDRESS A
    USING SHD_MESH M
    ON (A.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET A.MESH_ID = M.MESHID;
    COMMIT;
  END;

  PROCEDURE BATCHMESH_IX_ANNOTATION IS
  BEGIN
    --IX_ANNOTATION
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(A) PARALLEL(A)*/
                                      A.PID, A.GEOMETRY
                                       FROM IX_ANNOTATION A)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_ANNOTATION A
    USING SHD_MESH M
    ON (A.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET A.MESH_ID = M.MESHID;
    COMMIT;
  
  END;

  PROCEDURE BATCHMESH_IX_ANNOTATION_100W IS
  BEGIN
    --IX_ANNOTATION_100W
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(A) PARALLEL(A)*/
                                      A.PID, A.GEOMETRY
                                       FROM IX_ANNOTATION_100W A)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_ANNOTATION_100W A
    USING (SELECT PID,
                  SUBSTR('000000' || MESHID,
                         LENGTH('000000' || MESHID) - 6 + 1) MESHID
             FROM SHD_MESH) M
    ON (A.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE
         SET A.MESH_ID = SUBSTR(M.MESHID, 1, 1) || SUBSTR(M.MESHID, 3, 1); --��׼100���ͼ����
  
    COMMIT;
  END;

  PROCEDURE BATCHMESH_PT_POI IS
  BEGIN
    --PT_POI
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(P) PARALLEL(P)*/
                                      P.PID, P.GEOMETRY
                                       FROM PT_POI P)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO PT_POI P
    USING SHD_MESH M
    ON (P.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET P.MESH_ID = M.MESHID, P.MESH_ID_5K = M.MESHID_5K;
    COMMIT;
  END;

  PROCEDURE BATCHMESH_IX_POI IS
  BEGIN
    --IX_POI
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(P) PARALLEL(P)*/
                                      P.PID, P.GEOMETRY
                                       FROM IX_POI P)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_POI P
    USING SHD_MESH M
    ON (P.PID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET P.MESH_ID = M.MESHID, P.MESH_ID_5K = M.MESHID_5K;
    COMMIT;
  END;

  PROCEDURE BATCHMESH_IX_POSTCODE IS
  BEGIN
    --IX_POSTCODE
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_MESH');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MESH');
    INSERT /*+APPEND*/
    INTO SHD_MESH NOLOGGING
      SELECT ID, MESHID, MESHID_5K
        FROM TABLE(BATCHMESHPARAFUN(CURSOR (SELECT /*+FULL(P) PARALLEL(P)*/
                                      P.POST_ID, P.GEOMETRY
                                       FROM IX_POSTCODE P)));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MESH', FALSE);
    MERGE INTO IX_POSTCODE P
    USING SHD_MESH M
    ON (P.POST_ID = M.PID)
    WHEN MATCHED THEN
      UPDATE SET P.MESH_ID = M.MESHID, P.MESH_ID_5K = M.MESHID_5K;
    COMMIT;
  END;

  PROCEDURE POP_BATCHMESH IS
  BEGIN
    --ͼ����������
    BATCHMESH_IX_POI;
    BATCHMESH_PT_POI;
    BATCHMESH_IX_ANNOTATION_100W;
    BATCHMESH_IX_ANNOTATION;
    BATCHMESH_IX_POINTADDRESS;
    BATCHMESH_IX_HAMLET;
    BATCHMESH_IX_POSTCODE;
  END;

  PROCEDURE PREP_AD_FACE_FOR_BATCH IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'SHD_AD_FACE_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_AD_FACE_32774');
    INSERT /*+append*/
    INTO SHD_AD_FACE_32774
      (FACE_PID, REGION_ID, GEOMETRY)
      SELECT T.FACE_PID,
             T.REGION_ID,
             SDO_UTIL.RECTIFY_GEOMETRY(SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                                                    32774,
                                                    T.GEOMETRY.SDO_POINT,
                                                    T.GEOMETRY.SDO_ELEM_INFO,
                                                    T.GEOMETRY.SDO_ORDINATES),
                                       0.000005)
        FROM AD_FACE T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_AD_FACE_32774', FALSE, TRUE);
  END;

  /*   PROCEDURE BATCH_IC_ADMIN IS
     BEGIN
       MERGE INTO IX_IC P
       USING (SELECT *
                FROM (SELECT \*+full(P1) parallel(P1)*\ P1.PID,
                             F1.REGION_ID,
                             ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                        FROM IX_IC P1, AD_FACE F1
                       WHERE SDO_RELATE(F1.GEOMETRY,
                                        P1.GEOMETRY,
                                        'MASK=ANYINTERACT') = 'TRUE' ) F
               WHERE F.R = 1) U
       ON (P.PID = U.PID)
       WHEN MATCHED THEN
         UPDATE SET P.REGION_ID=U.REGION_ID;
       COMMIT;
  
     END;
  
  
     PROCEDURE BATCH_TOLLGATE_ADMIN IS
     BEGIN
       MERGE INTO IX_TOLLGATE P
       USING (SELECT *
                FROM (SELECT \*+full(P1) parallel(P1)*\ P1.PID,
                              F1.REGION_ID,
                             ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                        FROM IX_TOLLGATE P1, AD_FACE F1
                         WHERE SDO_RELATE(F1.GEOMETRY,
                                        P1.GEOMETRY,
                                        'MASK=ANYINTERACT') = 'TRUE' ) F
               WHERE F.R = 1) U
       ON (P.PID = U.PID)
       WHEN MATCHED THEN
         UPDATE SET P.REGION_ID = U.REGION_ID;
       COMMIT;
  
     END;
  
  
  */

  PROCEDURE BATCH_HAMLET_ADMIN IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'SHD_IX_HAMLET_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_HAMLET_32774');
    INSERT /*+append*/
    INTO SHD_IX_HAMLET_32774
      (PID, GEOMETRY)
      SELECT T.PID,
             SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                          32774,
                          T.GEOMETRY.SDO_POINT,
                          T.GEOMETRY.SDO_ELEM_INFO,
                          T.GEOMETRY.SDO_ORDINATES)
        FROM IX_HAMLET T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_IX_HAMLET_32774', FALSE);
  
    MERGE INTO IX_HAMLET P
    USING (SELECT *
             FROM (SELECT /*+full(P1) parallel(P1)*/
                    P1.PID,
                    F1.REGION_ID,
                    ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                     FROM SHD_IX_HAMLET_32774 P1, SHD_AD_FACE_32774 F1
                    WHERE SDO_RELATE(F1.GEOMETRY,
                                     P1.GEOMETRY,
                                     'MASK=ANYINTERACT') = 'TRUE') F
            WHERE F.R = 1) U
    ON (P.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET P.REGION_ID = U.REGION_ID;
    COMMIT;
  
  END;

  PROCEDURE BATCH_POI_ADMIN IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_IX_POI_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_POI_32774');
    INSERT /*+append*/
    INTO SHD_IX_POI_32774
      (PID, GEOMETRY)
      SELECT T.PID,
             SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                          32774,
                          T.GEOMETRY.SDO_POINT,
                          T.GEOMETRY.SDO_ELEM_INFO,
                          T.GEOMETRY.SDO_ORDINATES)
        FROM IX_POI T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_IX_POI_32774', FALSE);
  
    MERGE INTO IX_POI P
    USING (SELECT *
             FROM (SELECT /*+full(P1) parallel(P1)*/
                    P1.PID,
                    F1.REGION_ID,
                    ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                     FROM SHD_IX_POI_32774 P1, SHD_AD_FACE_32774 F1
                    WHERE SDO_RELATE(F1.GEOMETRY,
                                     P1.GEOMETRY,
                                     'MASK=ANYINTERACT') = 'TRUE') F
            WHERE F.R = 1) U
    ON (P.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET P.REGION_ID = U.REGION_ID;
    COMMIT;
  
  END;

  /*   PROCEDURE BATCH_IX_ROAD_ADMIN IS
     BEGIN
        MERGE INTO IX_ROADNAME P
        USING (SELECT *
                 FROM (SELECT \*+full(P1) parallel(P1)*\ P1.PID,
                              F1.REGION_ID,
                              ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                         FROM IX_ROADNAME P1, AD_FACE F1
                        WHERE SDO_RELATE(F1.GEOMETRY,
                                        P1.GEOMETRY,
                                        'MASK=ANYINTERACT') = 'TRUE') F
                WHERE F.R = 1) U
        ON (P.PID = U.PID)
        WHEN MATCHED THEN
          UPDATE SET P.REGION_ID = U.REGION_ID;
        COMMIT;
  
  
  
     END;
  */

  /*   PROCEDURE BATCH_CROSSNODE_ADMIN IS
     BEGIN
        MERGE INTO IX_CROSSPOINT P
        USING (SELECT *
                 FROM (SELECT \*+full(P1) parallel(P1)*\ P1.PID,
                              F1.REGION_ID,
                              ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                         FROM IX_CROSSPOINT P1, AD_FACE F1
                         WHERE SDO_RELATE(F1.GEOMETRY,
                                        P1.GEOMETRY,
                                        'MASK=ANYINTERACT') = 'TRUE' ) F
                WHERE F.R = 1) U
        ON (P.PID = U.PID)
        WHEN MATCHED THEN
          UPDATE SET P.REGION_ID = U.REGION_ID;
        COMMIT;
  
  
  
     END;
  */

  PROCEDURE BATCH_POSTCODE_ADMIN IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'SHD_IX_POSTCODE_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_POSTCODE_32774');
    INSERT /*+append*/
    INTO SHD_IX_POSTCODE_32774
      (PID, GEOMETRY)
      SELECT T.POST_ID,
             SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                          32774,
                          T.GEOMETRY.SDO_POINT,
                          T.GEOMETRY.SDO_ELEM_INFO,
                          T.GEOMETRY.SDO_ORDINATES)
        FROM IX_POSTCODE T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_IX_POSTCODE_32774', FALSE);
  
    MERGE INTO IX_POSTCODE P
    USING (SELECT *
             FROM (SELECT /*+full(P1) parallel(P1)*/
                    P1.PID,
                    F1.REGION_ID,
                    ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                     FROM SHD_IX_POSTCODE_32774 P1, SHD_AD_FACE_32774 F1
                    WHERE SDO_RELATE(F1.GEOMETRY,
                                     P1.GEOMETRY,
                                     'MASK=ANYINTERACT') = 'TRUE') F
            WHERE F.R = 1) U
    ON (P.POST_ID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET P.REGION_ID = U.REGION_ID;
    COMMIT;
  
  END;

  PROCEDURE BATCH_ANNOTATION_ADMIN IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_IX_ANN_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_ANN_32774');
    INSERT /*+append*/
    INTO SHD_IX_ANN_32774
      (PID, GEOMETRY)
      SELECT T.PID,
             SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                          32774,
                          T.GEOMETRY.SDO_POINT,
                          T.GEOMETRY.SDO_ELEM_INFO,
                          T.GEOMETRY.SDO_ORDINATES)
        FROM IX_ANNOTATION T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_IX_ANN_32774', FALSE);
  
    MERGE INTO IX_ANNOTATION P
    USING (SELECT *
             FROM (SELECT /*+full(P1) parallel(P1)*/
                    P1.PID,
                    F1.REGION_ID,
                    ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                     FROM SHD_IX_ANN_32774 P1, SHD_AD_FACE_32774 F1
                    WHERE SDO_RELATE(F1.GEOMETRY,
                                     P1.GEOMETRY,
                                     'MASK=ANYINTERACT') = 'TRUE') F
            WHERE F.R = 1) U
    ON (P.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET P.REGION_ID = U.REGION_ID;
    COMMIT;
  
  END;

  PROCEDURE BATCH_DOORNUM_ADMIN IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_IX_PA_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_PA_32774');
    INSERT /*+append*/
    INTO SHD_IX_PA_32774
      (PID, GEOMETRY)
      SELECT T.PID,
             SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                          32774,
                          T.GEOMETRY.SDO_POINT,
                          T.GEOMETRY.SDO_ELEM_INFO,
                          T.GEOMETRY.SDO_ORDINATES)
        FROM IX_POINTADDRESS T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_IX_PA_32774', FALSE);
  
    MERGE INTO IX_POINTADDRESS P
    USING (SELECT *
             FROM (SELECT /*+full(P1) parallel(P1)*/
                    P1.PID,
                    F1.REGION_ID,
                    ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                     FROM SHD_IX_PA_32774 P1, SHD_AD_FACE_32774 F1
                    WHERE SDO_RELATE(F1.GEOMETRY,
                                     P1.GEOMETRY,
                                     'MASK=ANYINTERACT') = 'TRUE') F
            WHERE F.R = 1) U
    ON (P.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET P.REGION_ID = U.REGION_ID;
    COMMIT;
  
  END;

  PROCEDURE BATCH_BUSSTOP_ADMIN IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'SHD_PT_POI_32774');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_PT_POI_32774');
    INSERT /*+append*/
    INTO SHD_PT_POI_32774
      (PID, GEOMETRY)
      SELECT T.PID,
             SDO_GEOMETRY(T.GEOMETRY.SDO_GTYPE,
                          32774,
                          T.GEOMETRY.SDO_POINT,
                          T.GEOMETRY.SDO_ELEM_INFO,
                          T.GEOMETRY.SDO_ORDINATES)
        FROM PT_POI T;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_PT_POI_32774', FALSE);
  
    MERGE INTO PT_POI P
    USING (SELECT *
             FROM (SELECT /*+full(P1) parallel(P1)*/
                    P1.PID,
                    F1.REGION_ID,
                    ROW_NUMBER() OVER(PARTITION BY P1.PID ORDER BY 1) R
                     FROM SHD_PT_POI_32774 P1, SHD_AD_FACE_32774 F1
                    WHERE SDO_RELATE(F1.GEOMETRY,
                                     P1.GEOMETRY,
                                     'MASK=ANYINTERACT') = 'TRUE') F
            WHERE F.R = 1) U
    ON (P.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET P.REGION_ID = U.REGION_ID;
    COMMIT;
  
  END;

  FUNCTION POIRELATEDLINK(DATACURSOR IN REFTYPECURSOR)
    RETURN RELATED_LINK_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION DATACURSOR BY ANY) IS
    ARELATE  RELATED_RES;
    PID      BINARY_INTEGER;
    LINK_PID BINARY_INTEGER;
    PGEOM    MDSYS.SDO_GEOMETRY;
    LGEOM    MDSYS.SDO_GEOMETRY;
    DGEOM    MDSYS.SDO_GEOMETRY;
    PPT      MDSYS.SDO_GEOMETRY;
    LPT      MDSYS.SDO_GEOMETRY;
    DIST     NUMBER;
  BEGIN
    IF DATACURSOR IS NULL THEN
      RETURN;
    END IF;
    LOOP
      FETCH DATACURSOR
        INTO PID, LINK_PID, PGEOM, LGEOM, DGEOM;
      EXIT WHEN DATACURSOR%NOTFOUND;
      SDO_GEOM.SDO_CLOSEST_POINTS(PGEOM,
                                  LGEOM,
                                  NAVI_GEOM.TOLERANCE,
                                  'UNIT=METER',
                                  DIST,
                                  PPT,
                                  LPT);
      ARELATE.PID      := PID;
      ARELATE.LINK_PID := LINK_PID;
      IF DIST < 0.00001 THEN
        --����Ϊ0,˵������link��
        ARELATE.X := NAVI_GEOM.XLONG(PGEOM);
        ARELATE.Y := NAVI_GEOM.YLAT(PGEOM);
      ELSE
        ARELATE.X := NAVI_GEOM.XLONG(LPT);
        ARELATE.Y := NAVI_GEOM.YLAT(LPT);
      END IF;
    
      --������ʾ������link�ľ���, ���1.5�׷�Χ����Ϊ��link��
      IF NAVI_GEOM.GEOM_2_GEOM_DISTANCE(LGEOM, DGEOM) <= 1.5 THEN
        ARELATE.SIDE := 3;
      ELSE
      
        --ʹ����ʾ�������ж�����λ�ù�ϵ
        ARELATE.SIDE := COMMON_UTIL.GET_PT_LINK_SIDE(LGEOM, DGEOM);
      END IF;
    
      PIPE ROW(ARELATE);
    END LOOP;
    CLOSE DATACURSOR;
    RETURN;
  END;

  PROCEDURE SHD_TABLE_PEX(P_TASK_NAME  IN VARCHAR2,
                          P_TABLE_LIST IN VARCHAR2,
                          P_PEX        IN VARCHAR2 DEFAULT 'LINK') IS
    V_TABLE_ARRAY COMMON_UTIL.STRINGARRAY;
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, P_TABLE_LIST);
    V_TABLE_ARRAY := COMMON_UTIL.SPLITSTRING(P_TABLE_LIST);
    FOR I IN 1 .. V_TABLE_ARRAY.COUNT LOOP
      EXECUTE IMMEDIATE 'CREATE TABLE ' || V_TABLE_ARRAY(I) || P_PEX ||
                        ' AS SELECT * FROM ' || V_TABLE_ARRAY(I);
    END LOOP;
  END;

  PROCEDURE SHD_IX_POI_TEMP(P_TASK_NAME IN VARCHAR2 DEFAULT 'POINT_FEATURE_BATCH') IS
    V_REGION_INFO VARCHAR2(30);
    V_LANG_CODE   VARCHAR2(10);
  BEGIN
  
    IF P_TASK_NAME <> 'POINT_FEATURE_BATCH' THEN
      NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'NI_RD_NAME');
      KBDB_DATA_TOOL.PREPARE_DATA('NI_RD_NAME');
    END IF;
  
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_POI_MULTI_RDLINK,SHD_ADDRROAD_LINK');
  
    V_REGION_INFO := NVL(GLM_TOOL.GET_PARAMETER('REGION_INFO'), 'ML');
    IF V_REGION_INFO = 'ML' THEN
      V_LANG_CODE := 'CHI';
    ELSE
      V_LANG_CODE := 'CHT';
    END IF;
  
    --ԭPOI����������                   
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ʼPOI������');
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY FROM RD_LINK L WHERE L.DEVELOP_STATE <> 2;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    --��poi�Ĺ���linkΪ0���߹���link�����ڵ�,����Ϊ0
    MERGE /*+NO_MERGE(U)*/
    INTO SHD_IX_POI_TEMP P
    USING (SELECT ROWID RD
             FROM SHD_IX_POI_TEMP IP
            WHERE NOT EXISTS
            (SELECT 1 FROM RD_LINK RL WHERE IP.LINK_PID = RL.LINK_PID)) U
    ON (P.ROWID = U.RD)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = 0, P.SIDE = 0, P.NAME_GROUPID = 0;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
    --�ҵ�poi�����4��link
    INSERT /*+APPEND*/
    INTO SHD_POI_RELATELINK
      SELECT /*+FULL(P) PARALLEL(P)*/
       P.PID, L.LINK_PID, P.X_GUIDE, P.Y_GUIDE, 0, 0
        FROM SHD_IX_POI_TEMP P, SHD_RD_FILTER_LINK L
       WHERE P.LINK_PID = 0 --���������link�����ڵ�poi
         AND SDO_NN(L.GEOMETRY,
                    NAVI_GEOM.CREATEPOINT(P.X_GUIDE, P.Y_GUIDE),
                    'SDO_NUM_RES=4 DISTANCE=80000 UNIT=METER') = 'TRUE';
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
  
    --�����link��poi�ľ���
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RELATELINK K
    USING RD_LINK L
    ON (K.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET DIST = NAVI_GEOM.GEOM_2_GEOM_DISTANCE(L.GEOMETRY,
                                                   NAVI_GEOM.CREATEPOINT(K.X_GUIDE,
                                                                         K.Y_GUIDE));
    COMMIT;
  
    --��0.5��Ϊ�ݲ�,����֮�������0.5��֮�ڵĶ�����Ҫ��,���ֶ�DISTCOUNT��Ǹ�poi�������������link����
    MERGE /*+PARALLEL(R)*/
    INTO SHD_POI_RELATELINK R
    USING (SELECT POI_PID,
                  LINK_PID,
                  COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) N
             FROM (SELECT T.POI_PID,
                          T.LINK_PID,
                          DIST,
                          DIST - MIN(DIST) OVER(PARTITION BY POI_PID ORDER BY 1) MD
                     FROM SHD_POI_RELATELINK T) K
            WHERE K.MD <= 0.5) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.N;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_MULTI_RDLINK');
  
    --������ҵ��֤���˵�ֱ��ȡ���һ��link
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT POI_PID, LINK_PID, X_GUIDE, Y_GUIDE
        FROM (SELECT PR.POI_PID,
                     PR.LINK_PID,
                     PR.X_GUIDE,
                     PR.Y_GUIDE,
                     PR.DIST,
                     ROW_NUMBER() OVER(PARTITION BY PR.POI_PID ORDER BY PR.DIST ASC) RN
                FROM SHD_POI_RELATELINK PR, IX_POI_FLAG PF
               WHERE PR.POI_PID = PF.POI_PID
                 AND PF.FLAG_CODE = '110000190000') U
       WHERE U.RN = 1;
  
    COMMIT;
  
    --��������һ��link����д��SHD_POI_RD_LINK,�ȴ��󽻵�
    --�����ڶ����д��SHD_POI_MULTI_RDLINK,�������ɸѡ
    INSERT /*+APPEND*/
    ALL WHEN DISTCOUNT = 1 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN DISTCOUNT > 1 THEN INTO SHD_POI_MULTI_RDLINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, PROPFILTER)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0)
      SELECT R.POI_PID,
             R.LINK_PID,
             R.X_GUIDE,
             R.Y_GUIDE,
             R.DIST,
             R.DISTCOUNT
        FROM SHD_POI_RELATELINK R
       WHERE R.DISTCOUNT > 0
         AND NOT EXISTS (SELECT 1
                FROM IX_POI_FLAG PF
               WHERE R.POI_PID = PF.POI_PID
                 AND PF.FLAG_CODE = '110000190000');
  
    COMMIT;
  
    --��������Ϊ�ѵ��ȵ�link,PROPFILTER�ֶα��Ϊ1,����0
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_MULTI_RDLINK K
    USING (SELECT L.LINK_PID
             FROM RD_LINK L
            WHERE L.KIND IN (11, 13)
               OR L.SPECIAL_TRAFFIC = 1
           UNION
           SELECT F.LINK_PID
             FROM RD_LINK_FORM F
            WHERE F.FORM_OF_WAY IN (10, 11, 14, 15, 30, 31, 33, 50)) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.PROPFILTER = 1; --S˵���õ�·���ѵ�,IC������
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_MULTI_RDLINK', FALSE);
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_ADDRROAD_LINK');
  
    --��������ɸѡ,���������һ��link��,ֱ��д��SHD_POI_RD_LINK�ȴ��󽻵�
    --ȫΪ�ѵ�������,д��SHD_ADDRROAD_LINK,�����Ƚϵ�·��
    --�������ѵ�������link,д��SHD_ADDRROAD_LINK,�����Ƚϵ�·��
    INSERT ALL WHEN R = 1 AND PROPFILTER = 0 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) --��ʣ�µ�һ������Ŀ�ı�
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN R = 0 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       PROPMARK,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM) --ȫΪ�ѵ�������
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 1, 0, 0, 0, 0, 0) WHEN R > 1 AND PROPFILTER = 0 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       PROPMARK,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM) --�������ѵ�������,�ų��ѵ����Ժ��link
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0, 0, 0, 0, 0, 0)
      SELECT POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, PROPFILTER, DIST, C - P R
        FROM (SELECT K.POI_PID,
                     K.LINK_PID,
                     K.X_GUIDE,
                     K.Y_GUIDE,
                     K.DIST,
                     K.PROPFILTER,
                     COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) C,
                     SUM(PROPFILTER) OVER(PARTITION BY POI_PID ORDER BY 1) P
                FROM SHD_POI_MULTI_RDLINK K);
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_ADDRROAD_LINK', FALSE);
  
    --����poi�ĵ�·����link�ĵ�·��,link������/Ӣ����
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT P.PID, PA.STREET
             FROM SHD_IX_POI_TEMP P, IX_POI_ADDRESS PA
            WHERE P.PID = PA.POI_PID
              AND PA.LANG_CODE = V_LANG_CODE
              AND PA.STREET IS NOT NULL) U
    ON (K.POI_PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET K.ADDRROAD = U.STREET;
    COMMIT;
  
    --����/Ӣ��
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT DISTINCT L.LINK_PID
             FROM RD_LINK_NAME L, NI_RD_NAME RN
            WHERE L.NAME_GROUPID = RN.NAME_GROUPID
              AND RN.LANG_CODE IN ('ENG', 'POR')) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.FNAMEMARK = 1; --��������/Ӣ�ĵĵ�·
    COMMIT;
  
    --���·������й���,����POI��addrroad���·���ٷ�����ͬ��GFNAMEΪ1,����0
    --���·������й���,����POI��addrroad���·��������ͬ��BNAMEΪ1,����0
    --����������ɺ�,ֻҪ��·����ͬ��,NAMEMARK���Ϊ1,������0
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT NVL(T1.LINK_PID, T2.LINK_PID) LINK_PID,
                  NVL(GFNAMEMARK, 0) GFNAMEMARK,
                  NVL(BNAMEMARK, 0) BNAMEMARK
             FROM (SELECT DISTINCT SA.LINK_PID, 1 AS GFNAMEMARK
                     FROM SHD_ADDRROAD_LINK SA,
                          RD_LINK_NAME      RN,
                          NI_RD_NAME        NA
                    WHERE SA.LINK_PID = RN.LINK_PID
                      AND RN.NAME_GROUPID = NA.NAME_GROUPID
                      AND RN.NAME_CLASS = 1
                      AND SA.ADDRROAD = NA.NAME) T1
             FULL JOIN (SELECT DISTINCT SA.LINK_PID, 1 AS BNAMEMARK
                         FROM SHD_ADDRROAD_LINK SA,
                              RD_LINK_NAME      RN,
                              NI_RD_NAME        NA
                        WHERE SA.LINK_PID = RN.LINK_PID
                          AND RN.NAME_GROUPID = NA.NAME_GROUPID
                          AND RN.NAME_CLASS = 2
                          AND SA.ADDRROAD = NA.NAME) T2
               ON T1.LINK_PID = T2.LINK_PID) U --�ٷ���,����
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET K.GFNAME   = U.GFNAMEMARK,
             K.BNAME    = U.BNAMEMARK,
             K.NAMEMARK =
             (CASE
    WHEN U.GFNAMEMARK + U.BNAMEMARK > 0 THEN 1 ELSE 0 END);
  
    COMMIT;
  
    --��������ɸѡ����ڶ��������(���������ѵ�������, ����ȫ�����ѵ������Ե�)
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT Q.POI_PID, Q.LINK_PID, Q.X_GUIDE, Q.Y_GUIDE
        FROM (SELECT T.POI_PID,
                     T.LINK_PID,
                     T.X_GUIDE,
                     T.Y_GUIDE,
                     T.NAMEMARK,
                     SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                FROM SHD_ADDRROAD_LINK T) Q
       WHERE S = 1
         AND NAMEMARK = 1 --����һ����������ͬ,�����Ƿ�����Ϊ�ѵ�,ֱ��д��SHD_POI_RD_LINK
      
      UNION ALL
      
      SELECT E.POI_PID, E.LINK_PID, E.X_GUIDE, E.Y_GUIDE
        FROM (SELECT Q.POI_PID,
                     Q.LINK_PID,
                     Q.X_GUIDE,
                     Q.Y_GUIDE,
                     DIST,
                     FNAMEMARK,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY FNAMEMARK DESC, DIST ASC) R
                FROM (SELECT T.POI_PID,
                             T.LINK_PID,
                             T.X_GUIDE,
                             T.Y_GUIDE,
                             T.DIST,
                             T.FNAMEMARK,
                             SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                        FROM SHD_ADDRROAD_LINK T) Q
               WHERE Q.S = 0) E
       WHERE E.R = 1 --û��һ������������ͬ,�����Ƿ�����Ϊ�ѵ�,���ȿ�������/Ӣ��,Ȼ���Ǿ���,��ѡһ��
      
      UNION ALL
      
      SELECT V.POI_PID, V.LINK_PID, V.X_GUIDE, V.Y_GUIDE
        FROM (SELECT POI_PID,
                     LINK_PID,
                     X_GUIDE,
                     Y_GUIDE,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY GFNAME DESC, BNAME DESC) R
                FROM (SELECT T.POI_PID,
                             T.LINK_PID,
                             T.X_GUIDE,
                             T.Y_GUIDE,
                             T.DIST,
                             T.FNAMEMARK,
                             T.GFNAME,
                             T.BNAME,
                             SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                        FROM SHD_ADDRROAD_LINK T) W
               WHERE W.S > 1) V
       WHERE V.R = 1; --�����Ƿ��ѵ�������,���ڶ�����ĵ�·����ͬ��linkʱ,���Ȱ��ٷ���,�������ȼ�ȡһ��
  
    COMMIT;
  
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RD_LINK K
    USING SHD_IX_POI_TEMP P
    ON (K.POI_PID = P.PID)
    WHEN MATCHED THEN
      UPDATE SET K.GEOMETRY = P.GEOMETRY;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    /*    IF V_LANG_CODE = 'CHI' THEN
      DELETE FROM IX_POI_FLAG PF
       WHERE PF.FLAG_CODE = '110000070000';
      COMMIT;
    END IF;*/
  
    MERGE INTO SHD_IX_POI_TEMP P
    USING (SELECT *
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+PARALLEL(P)*/
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY LGEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK P,
                                                                   RD_LINK         L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID)))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.LINK_PID = T.LINK_PID,
             P.X_GUIDE  = T.X,
             P.Y_GUIDE  = T.Y,
             P.SIDE     = T.SIDE;
    COMMIT;
  
    --����SA/PA(230206,230207),һ��ȡside=0,����Ӧ�ô���
    UPDATE SHD_IX_POI_TEMP P
       SET P.SIDE = 0
     WHERE P.KIND_CODE IN ('230206', '230207');
    COMMIT;
  
    --����Դ�½����
    --ɸѡ����ʩ�������Եĵ�·�ϵ�IC���շ�վ��������/ͣ����POI����IX_POI_FLAGд��flag=��110000070000��
    /*    IF V_LANG_CODE = 'CHI' THEN
    
      INSERT INTO IX_POI_FLAG
        (POI_PID, FLAG_CODE)
        SELECT P.PID, '110000070000' FLAG_CODE
          FROM RD_LINK L, SHD_IX_POI_TEMP P
         WHERE P.LINK_PID = L.LINK_PID
           AND EXISTS
         (SELECT 1
                  FROM RD_LINK_LIMIT RL
                 WHERE L.LINK_PID = RL.LINK_PID
                   AND RL.TYPE = 4)
           AND P.KIND_CODE IN ('230206', '230207', '230208');
    
      COMMIT;
    END IF;*/
  
    --������pmeshid
    MERGE INTO SHD_IX_POI_TEMP PO
    USING (SELECT IP.PID, L.MESH_ID
             FROM RD_LINK L, SHD_IX_POI_TEMP IP
            WHERE L.LINK_PID = IP.LINK_PID) U
    ON (PO.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET PO.PMESH_ID = U.MESH_ID
       WHERE PO.PMESH_ID = 0
          OR PO.PMESH_ID IS NULL;
  
    COMMIT;
  
  END;

  PROCEDURE BATCH_IXPOI_LINK(P_TASK_NAME VARCHAR2 DEFAULT 'POINT_FEATURE_BATCH') IS
    V_LANG_CODE   VARCHAR2(10);
    V_REGION_INFO VARCHAR2(30);
  BEGIN
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '������ʱ��SHD_IX_POI_TEMP');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_POI_TEMP');
    INSERT INTO SHD_IX_POI_TEMP
      (PID,
       KIND_CODE,
       GEOMETRY,
       NAME_GROUPID,
       LINK_PID,
       SIDE,
       X_GUIDE,
       Y_GUIDE,
       PMESH_ID)
      SELECT PID,
             KIND_CODE,
             GEOMETRY,
             NAME_GROUPID,
             LINK_PID,
             SIDE,
             X_GUIDE,
             Y_GUIDE,
             PMESH_ID
        FROM IX_POI;
    COMMIT;
  
    SHD_IX_POI_TEMP(P_TASK_NAME);
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_POI');
    MERGE /*+NO_MERGE(U)*/
    INTO IX_POI T
    USING SHD_IX_POI_TEMP U
    ON (T.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET T.NAME_GROUPID = U.NAME_GROUPID,
             T.LINK_PID     = U.LINK_PID,
             T.SIDE         = U.SIDE,
             T.X_GUIDE      = U.X_GUIDE,
             T.Y_GUIDE      = U.Y_GUIDE,
             T.PMESH_ID     = U.PMESH_ID;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_POI_FALG');
    V_REGION_INFO := NVL(GLM_TOOL.GET_PARAMETER('REGION_INFO'), 'ML');
    IF V_REGION_INFO = 'ML' THEN
      V_LANG_CODE := 'CHI';
    ELSE
      V_LANG_CODE := 'CHT';
    END IF;
    IF V_LANG_CODE = 'CHI' THEN
      DELETE FROM IX_POI_FLAG PF WHERE PF.FLAG_CODE = '110000070000';
      COMMIT;
    
      INSERT INTO IX_POI_FLAG
        (POI_PID, FLAG_CODE)
        SELECT P.PID, '110000070000' FLAG_CODE
          FROM RD_LINK L, IX_POI P
         WHERE P.LINK_PID = L.LINK_PID
           AND EXISTS
         (SELECT 1
                  FROM RD_LINK_LIMIT RL
                 WHERE L.LINK_PID = RL.LINK_PID
                   AND RL.TYPE = 4)
           AND P.KIND_CODE IN ('230206', '230207', '230208');
    
      COMMIT;
    END IF;
  
    RETURN;
  
    --==================================================================================
    --һ�´��뱻�ϱߴ���������
    --==================================================================================
  
    /*    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'NI_RD_NAME');
    KBDB_DATA_TOOL.PREPARE_DATA('NI_RD_NAME');
    
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_POI_MULTI_RDLINK,SHD_ADDRROAD_LINK');
    
    V_REGION_INFO := NVL(GLM_TOOL.GET_PARAMETER('REGION_INFO'), 'ML');
    IF V_REGION_INFO = 'ML' THEN
      V_LANG_CODE := 'CHI';
    ELSE
      V_LANG_CODE := 'CHT';
    END IF;
    
    --��Ԫ���ݲ�������FM������¼��������ҵ����������������ʾ���겻��ͬ��POI��add by:ZRZ
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '��Ԫ���ݿ�SC_POINT_GUIDE���в������£�������ҵ����������������ʾ���겻��ͬ��POI');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('IX_POI_DEL_PID');
    
    INSERT INTO IX_POI_DEL_PID
      SELECT IP.PID
        FROM IX_POI IP
       WHERE IP.LINK_PID = 0
      
      UNION
      
      SELECT IP.PID
        FROM IX_POI IP
       WHERE NOT EXISTS
       (SELECT 1 FROM RD_LINK RL WHERE IP.LINK_PID = RL.LINK_PID);
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('IX_POI_DEL_PID', FALSE);
    
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('TMP_POI_GUIDECOORD');
    INSERT INTO TMP_POI_GUIDECOORD
      SELECT IP.PID, IP.FIELD_TASK_ID, IP.X_GUIDE, IP.Y_GUIDE
        FROM IX_POI IP, IX_POI_DEL_PID IPD
       WHERE IP.PID = IPD.PID
         AND NAVI_GEOM.GET25KMAPNUMBER1(IP.X_GUIDE, IP.Y_GUIDE) <>
             NAVI_GEOM.GET25KMAPNUMBER1(IP.GEOMETRY)
      
      UNION
      
      SELECT IP.PID, IP.FIELD_TASK_ID, IP.X_GUIDE, IP.Y_GUIDE
        FROM IX_POI IP, IX_POI_DEL_PID IPD
       WHERE IP.PID = IPD.PID
         AND NAVI_GEOM.PTINMESH(NAVI_GEOM.CREATEPOINT(IP.X_GUIDE,
                                                      IP.Y_GUIDE),
                                NAVI_GEOM.GET25KMAPNUMBER1(IP.X_GUIDE,
                                                           IP.Y_GUIDE)) IN
             (1, 2);
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('TMP_POI_GUIDECOORD', FALSE);
    
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '����Ԫ���ݿ�SC_POINT_GUIDE���°���ҵ��ŵ�POI��Ϣ');
    MERGE \*+NO_MERGE(U)*\
    INTO SC_POINT_GUIDE@KBDB SPG
    USING (SELECT TPG.PID, TPG.FIELD_TASK_ID, TPG.X_GUIDE, TPG.Y_GUIDE
             FROM TMP_POI_GUIDECOORD TPG, SC_POINT_GUIDE@KBDB SPG1
            WHERE TPG.PID = SPG1.PID
              AND TPG.FIELD_TASK_ID <> SPG1.FIELD_TASK_ID) U
    ON (SPG.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET SPG.FIELD_TASK_ID = U.FIELD_TASK_ID,
             SPG.X_GUIDE       = U.X_GUIDE,
             SPG.Y_GUIDE       = U.Y_GUIDE;
    COMMIT;
    
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '��Ԫ���ݿ�SC_POINT_GUIDE������°���ҵ��ŵ�POI��Ϣ');
    INSERT INTO SC_POINT_GUIDE@KBDB
      (PID, FIELD_TASK_ID, X_GUIDE, Y_GUIDE)
      SELECT TPG.PID, TPG.FIELD_TASK_ID, TPG.X_GUIDE, TPG.Y_GUIDE
        FROM TMP_POI_GUIDECOORD TPG
       WHERE NOT EXISTS
       (SELECT 1 FROM SC_POINT_GUIDE@KBDB SPG WHERE TPG.PID = SPG.PID);
    COMMIT;
    
    \* NAVI_LOG.LOG_INFO(P_TASK_NAME, '��Ԫ���ݿ�SC_POINT_GUIDE�����һ��PIDΪ0����ҵ�汾��Ϣ��¼');                    
    
    INSERT INTO SC_POINT_GUIDE@KBDB
    (PID, FIELD_TASK_ID)
    SELECT DISTINCT 0 AS PID,
           IP.FIELD_TASK_ID
      FROM IX_POI           IP,
           IX_POI_DEL_PID   IPD
     WHERE IP.PID = IPD.PID;                      
    COMMIT; *\
    
    --ԭPOI����������                   
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ʼPOI������');
    
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT \*+APPEND*\
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY FROM RD_LINK L WHERE L.DEVELOP_STATE <> 2;
    
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
    
    --��poi�Ĺ���linkΪ0���߹���link�����ڵ�,����Ϊ0
    MERGE \*+NO_MERGE(U)*\
    INTO IX_POI P
    USING (SELECT ROWID RD
             FROM IX_POI IP
            WHERE NOT EXISTS
            (SELECT 1 FROM RD_LINK RL WHERE IP.LINK_PID = RL.LINK_PID)) U
    ON (P.ROWID = U.RD)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = 0, P.SIDE = 0, P.NAME_GROUPID = 0;
    
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
    --�ҵ�poi�����4��link
    INSERT \*+APPEND*\
    INTO SHD_POI_RELATELINK
      SELECT \*+FULL(P) PARALLEL(P)*\
       P.PID, L.LINK_PID, P.X_GUIDE, P.Y_GUIDE, 0, 0
        FROM IX_POI P, SHD_RD_FILTER_LINK L
       WHERE P.LINK_PID = 0 --���������link�����ڵ�poi
         AND SDO_NN(L.GEOMETRY,
                    NAVI_GEOM.CREATEPOINT(P.X_GUIDE, P.Y_GUIDE),
                    'SDO_NUM_RES=4 DISTANCE=80000 UNIT=METER') = 'TRUE';
    
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
    
    --�����link��poi�ľ���
    MERGE \*+PARALLEL(K)*\
    INTO SHD_POI_RELATELINK K
    USING RD_LINK L
    ON (K.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET DIST = NAVI_GEOM.GEOM_2_GEOM_DISTANCE(L.GEOMETRY,
                                                   NAVI_GEOM.CREATEPOINT(K.X_GUIDE,
                                                                         K.Y_GUIDE));
    COMMIT;
    
    --��0.5��Ϊ�ݲ�,����֮�������0.5��֮�ڵĶ�����Ҫ��,���ֶ�DISTCOUNT��Ǹ�poi�������������link����
    MERGE \*+PARALLEL(R)*\
    INTO SHD_POI_RELATELINK R
    USING (SELECT POI_PID,
                  LINK_PID,
                  COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) N
             FROM (SELECT T.POI_PID,
                          T.LINK_PID,
                          DIST,
                          DIST - MIN(DIST) OVER(PARTITION BY POI_PID ORDER BY 1) MD
                     FROM SHD_POI_RELATELINK T) K
            WHERE K.MD <= 0.5) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.N;
    
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_MULTI_RDLINK');
    
    --������ҵ��֤���˵�ֱ��ȡ���һ��link
    INSERT \*+APPEND*\
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT POI_PID, LINK_PID, X_GUIDE, Y_GUIDE
        FROM (SELECT PR.POI_PID,
                     PR.LINK_PID,
                     PR.X_GUIDE,
                     PR.Y_GUIDE,
                     PR.DIST,
                     ROW_NUMBER() OVER(PARTITION BY PR.POI_PID ORDER BY PR.DIST ASC) RN
                FROM SHD_POI_RELATELINK PR, IX_POI_FLAG PF
               WHERE PR.POI_PID = PF.POI_PID
                 AND PF.FLAG_CODE = '110000190000') U
       WHERE U.RN = 1;
    
    COMMIT;
    
    --��������һ��link����д��SHD_POI_RD_LINK,�ȴ��󽻵�
    --�����ڶ����д��SHD_POI_MULTI_RDLINK,�������ɸѡ
    INSERT \*+APPEND*\
    ALL WHEN DISTCOUNT = 1 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN DISTCOUNT > 1 THEN INTO SHD_POI_MULTI_RDLINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, PROPFILTER)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0)
      SELECT R.POI_PID,
             R.LINK_PID,
             R.X_GUIDE,
             R.Y_GUIDE,
             R.DIST,
             R.DISTCOUNT
        FROM SHD_POI_RELATELINK R
       WHERE R.DISTCOUNT > 0
         AND NOT EXISTS (SELECT 1
                FROM IX_POI_FLAG PF
               WHERE R.POI_PID = PF.POI_PID
                 AND PF.FLAG_CODE = '110000190000');
    
    COMMIT;
    
    --��������Ϊ�ѵ��ȵ�link,PROPFILTER�ֶα��Ϊ1,����0
    MERGE \*+PARALLEL(K)*\
    INTO SHD_POI_MULTI_RDLINK K
    USING (SELECT L.LINK_PID
             FROM RD_LINK L
            WHERE L.KIND IN (11, 13)
               OR L.SPECIAL_TRAFFIC = 1
           UNION
           SELECT F.LINK_PID
             FROM RD_LINK_FORM F
            WHERE F.FORM_OF_WAY IN (10, 11, 14, 15, 30, 31, 33, 50)) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.PROPFILTER = 1; --S˵���õ�·���ѵ�,IC������
    
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_MULTI_RDLINK', FALSE);
    
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_ADDRROAD_LINK');
    
    --��������ɸѡ,���������һ��link��,ֱ��д��SHD_POI_RD_LINK�ȴ��󽻵�
    --ȫΪ�ѵ�������,д��SHD_ADDRROAD_LINK,�����Ƚϵ�·��
    --�������ѵ�������link,д��SHD_ADDRROAD_LINK,�����Ƚϵ�·��
    INSERT ALL WHEN R = 1 AND PROPFILTER = 0 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) --��ʣ�µ�һ������Ŀ�ı�
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN R = 0 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       PROPMARK,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM) --ȫΪ�ѵ�������
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 1, 0, 0, 0, 0, 0) WHEN R > 1 AND PROPFILTER = 0 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       PROPMARK,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM) --�������ѵ�������,�ų��ѵ����Ժ��link
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0, 0, 0, 0, 0, 0)
      SELECT POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, PROPFILTER, DIST, C - P R
        FROM (SELECT K.POI_PID,
                     K.LINK_PID,
                     K.X_GUIDE,
                     K.Y_GUIDE,
                     K.DIST,
                     K.PROPFILTER,
                     COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) C,
                     SUM(PROPFILTER) OVER(PARTITION BY POI_PID ORDER BY 1) P
                FROM SHD_POI_MULTI_RDLINK K);
    
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_ADDRROAD_LINK', FALSE);
    
    --����poi�ĵ�·����link�ĵ�·��,link������/Ӣ����
    MERGE \*+PARALLEL(K)*\
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT P.PID, PA.STREET
             FROM IX_POI P, IX_POI_ADDRESS PA
            WHERE P.PID = PA.POI_PID
              AND PA.LANG_CODE = V_LANG_CODE
              AND PA.STREET IS NOT NULL) U
    ON (K.POI_PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET K.ADDRROAD = U.STREET;
    COMMIT;
    
    --����/Ӣ��
    MERGE \*+PARALLEL(K)*\
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT DISTINCT L.LINK_PID
             FROM RD_LINK_NAME L, NI_RD_NAME RN
            WHERE L.NAME_GROUPID = RN.NAME_GROUPID
              AND RN.LANG_CODE IN ('ENG', 'POR')) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.FNAMEMARK = 1; --��������/Ӣ�ĵĵ�·
    COMMIT;
    
    --���·������й���,����POI��addrroad���·���ٷ�����ͬ��GFNAMEΪ1,����0
    --���·������й���,����POI��addrroad���·��������ͬ��BNAMEΪ1,����0
    --����������ɺ�,ֻҪ��·����ͬ��,NAMEMARK���Ϊ1,������0
    MERGE \*+PARALLEL(K)*\
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT NVL(T1.LINK_PID, T2.LINK_PID) LINK_PID,
                  NVL(GFNAMEMARK, 0) GFNAMEMARK,
                  NVL(BNAMEMARK, 0) BNAMEMARK
             FROM (SELECT DISTINCT SA.LINK_PID, 1 AS GFNAMEMARK
                     FROM SHD_ADDRROAD_LINK SA,
                          RD_LINK_NAME      RN,
                          NI_RD_NAME        NA
                    WHERE SA.LINK_PID = RN.LINK_PID
                      AND RN.NAME_GROUPID = NA.NAME_GROUPID
                      AND RN.NAME_CLASS = 1
                      AND SA.ADDRROAD = NA.NAME) T1
             FULL JOIN (SELECT DISTINCT SA.LINK_PID, 1 AS BNAMEMARK
                         FROM SHD_ADDRROAD_LINK SA,
                              RD_LINK_NAME      RN,
                              NI_RD_NAME        NA
                        WHERE SA.LINK_PID = RN.LINK_PID
                          AND RN.NAME_GROUPID = NA.NAME_GROUPID
                          AND RN.NAME_CLASS = 2
                          AND SA.ADDRROAD = NA.NAME) T2
               ON T1.LINK_PID = T2.LINK_PID) U --�ٷ���,����
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET K.GFNAME   = U.GFNAMEMARK,
             K.BNAME    = U.BNAMEMARK,
             K.NAMEMARK =
             (CASE
    WHEN U.GFNAMEMARK + U.BNAMEMARK > 0 THEN 1 ELSE 0 END);
    
    COMMIT;
    
    --��������ɸѡ����ڶ��������(���������ѵ�������, ����ȫ�����ѵ������Ե�)
    
    INSERT \*+APPEND*\
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT Q.POI_PID, Q.LINK_PID, Q.X_GUIDE, Q.Y_GUIDE
        FROM (SELECT T.POI_PID,
                     T.LINK_PID,
                     T.X_GUIDE,
                     T.Y_GUIDE,
                     T.NAMEMARK,
                     SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                FROM SHD_ADDRROAD_LINK T) Q
       WHERE S = 1
         AND NAMEMARK = 1 --����һ����������ͬ,�����Ƿ�����Ϊ�ѵ�,ֱ��д��SHD_POI_RD_LINK
      
      UNION ALL
      
      SELECT E.POI_PID, E.LINK_PID, E.X_GUIDE, E.Y_GUIDE
        FROM (SELECT Q.POI_PID,
                     Q.LINK_PID,
                     Q.X_GUIDE,
                     Q.Y_GUIDE,
                     DIST,
                     FNAMEMARK,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY FNAMEMARK DESC, DIST ASC) R
                FROM (SELECT T.POI_PID,
                             T.LINK_PID,
                             T.X_GUIDE,
                             T.Y_GUIDE,
                             T.DIST,
                             T.FNAMEMARK,
                             SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                        FROM SHD_ADDRROAD_LINK T) Q
               WHERE Q.S = 0) E
       WHERE E.R = 1 --û��һ������������ͬ,�����Ƿ�����Ϊ�ѵ�,���ȿ�������/Ӣ��,Ȼ���Ǿ���,��ѡһ��
      
      UNION ALL
      
      SELECT V.POI_PID, V.LINK_PID, V.X_GUIDE, V.Y_GUIDE
        FROM (SELECT POI_PID,
                     LINK_PID,
                     X_GUIDE,
                     Y_GUIDE,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY GFNAME DESC, BNAME DESC) R
                FROM (SELECT T.POI_PID,
                             T.LINK_PID,
                             T.X_GUIDE,
                             T.Y_GUIDE,
                             T.DIST,
                             T.FNAMEMARK,
                             T.GFNAME,
                             T.BNAME,
                             SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                        FROM SHD_ADDRROAD_LINK T) W
               WHERE W.S > 1) V
       WHERE V.R = 1; --�����Ƿ��ѵ�������,���ڶ�����ĵ�·����ͬ��linkʱ,���Ȱ��ٷ���,�������ȼ�ȡһ��
    
    COMMIT;
    
    MERGE \*+PARALLEL(K)*\
    INTO SHD_POI_RD_LINK K
    USING IX_POI P
    ON (K.POI_PID = P.PID)
    WHEN MATCHED THEN
      UPDATE SET K.GEOMETRY = P.GEOMETRY;
    COMMIT;
    
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
    
    IF V_LANG_CODE = 'CHI' THEN
      DELETE FROM IX_POI_FLAG PF WHERE PF.FLAG_CODE = '110000070000';
      COMMIT;
    END IF;
    
    MERGE INTO IX_POI P
    USING (SELECT *
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT \*+PARALLEL(P)*\
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY LGEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK P,
                                                                   RD_LINK         L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID)))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.LINK_PID = T.LINK_PID,
             P.X_GUIDE  = T.X,
             P.Y_GUIDE  = T.Y,
             P.SIDE     = T.SIDE;
    COMMIT;
    
    --����SA/PA(230206,230207),һ��ȡside=0,����Ӧ�ô���
    UPDATE IX_POI P
       SET P.SIDE = 0
     WHERE P.KIND_CODE IN ('230206', '230207');
    COMMIT;
    
    --����Դ�½����
    --ɸѡ����ʩ�������Եĵ�·�ϵ�IC���շ�վ��������/ͣ����POI����IX_POI_FLAGд��flag=��110000070000��
    IF V_LANG_CODE = 'CHI' THEN
    
      INSERT INTO IX_POI_FLAG
        (POI_PID, FLAG_CODE)
        SELECT P.PID, '110000070000' FLAG_CODE
          FROM RD_LINK L, IX_POI P
         WHERE P.LINK_PID = L.LINK_PID
           AND EXISTS
         (SELECT 1
                  FROM RD_LINK_LIMIT RL
                 WHERE L.LINK_PID = RL.LINK_PID
                   AND RL.TYPE = 4)
           AND P.KIND_CODE IN ('230206', '230207', '230208');
    
      COMMIT;
    END IF;
    
    --������pmeshid
    MERGE INTO IX_POI PO
    USING (SELECT IP.PID, L.MESH_ID
             FROM RD_LINK L, IX_POI IP
            WHERE L.LINK_PID = IP.LINK_PID) U
    ON (PO.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET PO.PMESH_ID = U.MESH_ID
       WHERE PO.PMESH_ID = 0
          OR PO.PMESH_ID IS NULL;
    
    COMMIT;*/
  
  END;

  PROCEDURE BATCH_PTPOI_LINK_EXT IS
  BEGIN
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY
        FROM RD_LINK L
       WHERE L.DEVELOP_STATE <> 2
         AND NOT EXISTS (SELECT 1
                FROM RD_LINK_FORM F
               WHERE F.LINK_PID = L.LINK_PID
                 AND F.FORM_OF_WAY = 31);
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
    --�ҵ�poi�����4��link
    INSERT /*+APPEND*/
    INTO SHD_POI_RELATELINK
      SELECT /*+FULL(P) PARALLEL(P)*/
       P.PID, L.LINK_PID, P.X_GUIDE, P.Y_GUIDE, 0, 0
        FROM PT_POI P, SHD_RD_FILTER_LINK L
       WHERE SDO_NN(L.GEOMETRY,
                    NAVI_GEOM.CREATEPOINT(P.X_GUIDE, P.Y_GUIDE),
                    'SDO_NUM_RES=4 DISTANCE=80000 UNIT=METER') = 'TRUE'
         AND P.LINK_PID = 0;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
  
    --�����link��poi�ľ���
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RELATELINK K
    USING RD_LINK L
    ON (K.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET DIST = NAVI_GEOM.GEOM_2_GEOM_DISTANCE(L.GEOMETRY,
                                                   NAVI_GEOM.CREATEPOINT(K.X_GUIDE,
                                                                         K.Y_GUIDE));
    COMMIT;
  
    --��0.5��Ϊ�ݲ�,����֮�������0.5��֮�ڵĶ�����Ҫ��,���ֶ�DISTCOUNT��Ǹ�poi�������������link����
    MERGE /*+PARALLEL(R)*/
    INTO SHD_POI_RELATELINK R
    USING (SELECT POI_PID,
                  LINK_PID,
                  COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) N
             FROM (SELECT T.POI_PID,
                          T.LINK_PID,
                          DIST,
                          DIST - MIN(DIST) OVER(PARTITION BY POI_PID ORDER BY 1) MD
                     FROM SHD_POI_RELATELINK T) K
            WHERE K.MD <= 0.5) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.N;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_MULTI_RDLINK');
  
    --��������һ��link����д��SHD_POI_RD_LINK,�ȴ��󽻵�
    --�����ڶ����д��SHD_POI_MULTI_RDLINK,�������ɸѡ
    INSERT /*+APPEND*/
    ALL WHEN DISTCOUNT = 1 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN DISTCOUNT > 1 THEN INTO SHD_POI_MULTI_RDLINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, PROPFILTER)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0)
      SELECT R.POI_PID,
             R.LINK_PID,
             R.X_GUIDE,
             R.Y_GUIDE,
             R.DIST,
             R.DISTCOUNT
        FROM SHD_POI_RELATELINK R
       WHERE R.DISTCOUNT > 0;
  
    COMMIT;
  
    --��������Ϊ�ѵ��ȵ�link,PROPFILTER�ֶα��Ϊ1,����0
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_MULTI_RDLINK K
    USING (SELECT L.LINK_PID
             FROM RD_LINK L
            WHERE L.KIND IN (11, 13)
               OR L.SPECIAL_TRAFFIC = 1
           UNION
           SELECT F.LINK_PID
             FROM RD_LINK_FORM F
            WHERE F.FORM_OF_WAY IN (10, 11, 14, 15, 30, 31, 33, 50)) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.PROPFILTER = 1; --S˵���õ�·���ѵ�,IC������
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_MULTI_RDLINK', FALSE);
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_ADDRROAD_LINK');
  
    --��������ɸѡ,���������һ��link��,ֱ��д��SHD_POI_RD_LINK�ȴ��󽻵�
    --ȫΪ�ѵ�������,д��SHD_ADDRROAD_LINK,�����Ƚϵ�·��
    --�������ѵ�������link,д��SHD_ADDRROAD_LINK,�����Ƚϵ�·��
    INSERT ALL WHEN R = 1 AND PROPFILTER = 0 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) --��ʣ�µ�һ������Ŀ�ı�
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN R = 0 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       PROPMARK,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM) --ȫΪ�ѵ�������
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 1, 0, 0, 0, 0, 0) WHEN R > 1 AND PROPFILTER = 0 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       PROPMARK,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM) --�������ѵ�������,�ų��ѵ����Ժ��link
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0, 0, 0, 0, 0, 0)
      SELECT POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, PROPFILTER, DIST, C - P R
        FROM (SELECT K.POI_PID,
                     K.LINK_PID,
                     K.X_GUIDE,
                     K.Y_GUIDE,
                     K.DIST,
                     K.PROPFILTER,
                     COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) C,
                     SUM(PROPFILTER) OVER(PARTITION BY POI_PID ORDER BY 1) P
                FROM SHD_POI_MULTI_RDLINK K);
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_ADDRROAD_LINK', FALSE);
  
    --����/Ӣ��
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT DISTINCT L.LINK_PID
             FROM RD_LINK_NAME L, NI_RD_NAME RN
            WHERE L.NAME_GROUPID = RN.NAME_GROUPID
              AND RN.LANG_CODE IN ('ENG', 'POR')) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.FNAMEMARK = 1; --��������/Ӣ�ĵĵ�·
    COMMIT;
  
    --���·������й���,����POI��addrroad���·���ٷ�����ͬ��GFNAMEΪ1,����0
    --���·������й���,����POI��addrroad���·��������ͬ��BNAMEΪ1,����0
    --����������ɺ�,ֻҪ��·����ͬ��,NAMEMARK���Ϊ1,������0
    UPDATE SHD_ADDRROAD_LINK K
       SET K.GFNAME = 0, K.BNAME = 0, K.NAMEMARK = 0;
    COMMIT;
  
    --��������ɸѡ����ڶ��������(���������ѵ�������, ����ȫ�����ѵ������Ե�)
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT Q.POI_PID, Q.LINK_PID, Q.X_GUIDE, Q.Y_GUIDE
        FROM (SELECT T.POI_PID,
                     T.LINK_PID,
                     T.X_GUIDE,
                     T.Y_GUIDE,
                     T.NAMEMARK,
                     SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                FROM SHD_ADDRROAD_LINK T) Q
       WHERE S = 1
         AND NAMEMARK = 1 --����һ����������ͬ,�����Ƿ�����Ϊ�ѵ�,ֱ��д��SHD_POI_RD_LINK
      
      UNION ALL
      
      SELECT E.POI_PID, E.LINK_PID, E.X_GUIDE, E.Y_GUIDE
        FROM (SELECT Q.POI_PID,
                     Q.LINK_PID,
                     Q.X_GUIDE,
                     Q.Y_GUIDE,
                     DIST,
                     FNAMEMARK,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY FNAMEMARK DESC, DIST ASC) R
                FROM (SELECT T.POI_PID,
                             T.LINK_PID,
                             T.X_GUIDE,
                             T.Y_GUIDE,
                             T.DIST,
                             T.FNAMEMARK,
                             SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                        FROM SHD_ADDRROAD_LINK T) Q
               WHERE Q.S = 0) E
       WHERE E.R = 1 --û��һ������������ͬ,�����Ƿ�����Ϊ�ѵ�,���ȿ�������/Ӣ��,Ȼ���Ǿ���,��ѡһ��
      
      UNION ALL
      
      SELECT V.POI_PID, V.LINK_PID, V.X_GUIDE, V.Y_GUIDE
        FROM (SELECT POI_PID,
                     LINK_PID,
                     X_GUIDE,
                     Y_GUIDE,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY GFNAME DESC, BNAME DESC) R
                FROM (SELECT T.POI_PID,
                             T.LINK_PID,
                             T.X_GUIDE,
                             T.Y_GUIDE,
                             T.DIST,
                             T.FNAMEMARK,
                             T.GFNAME,
                             T.BNAME,
                             SUM(NAMEMARK) OVER(PARTITION BY POI_PID ORDER BY 1) S
                        FROM SHD_ADDRROAD_LINK T) W
               WHERE W.S > 1) V
       WHERE V.R = 1; --�����Ƿ��ѵ�������,���ڶ�����ĵ�·����ͬ��linkʱ,���Ȱ��ٷ���,�������ȼ�ȡһ��
  
    COMMIT;
  
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RD_LINK K
    USING PT_POI P
    ON (K.POI_PID = P.PID)
    WHEN MATCHED THEN
      UPDATE SET K.GEOMETRY = P.GEOMETRY;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    MERGE INTO PT_POI P
    USING (SELECT *
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+FULL(P) parallel(P)*/
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK P,
                                                                   RD_LINK         L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID)))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.LINK_PID = T.LINK_PID,
             P.X_GUIDE  = T.X,
             P.Y_GUIDE  = T.Y,
             P.SIDE     = T.SIDE;
    COMMIT;
  
  END;

  PROCEDURE BATCH_PTPOI_LINK(P_TASK_NAME VARCHAR2 DEFAULT 'BATCH_PTPOI_LINK') IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, 'NI_RD_NAME');
    KBDB_DATA_TOOL.PREPARE_DATA('NI_RD_NAME');
  
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME,
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_POI_MULTI_RDLINK,SHD_ADDRROAD_LINK');
  
    MERGE /*+NO_MERGE(U)*/
    INTO PT_POI P
    USING (SELECT ROWID RD
             FROM PT_POI PP
            WHERE NOT EXISTS
            (SELECT 1 FROM RD_LINK RL WHERE PP.LINK_PID = RL.LINK_PID)) U
    ON (P.ROWID = U.RD)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = 0, P.SIDE = 0, P.NAME_GROUPID = 0;
  
    COMMIT;
  
    BATCH_PTPOI_LINK_EXT;
  
    --POI���ֱ�Ϊ�������㣨8085�������������㣨8086����������link��������������һ�£��򸳡�N��
    MERGE INTO PT_POI PI
    USING (SELECT T.LINK_PID
             FROM RD_LINK T
            WHERE T.LEFT_REGION_ID = T.RIGHT_REGION_ID
              AND T.LEFT_REGION_ID > 0) L
    ON (PI.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET PI.SIDE = 0 WHERE PI.POI_KIND IN ('8085', '8086');
  
    COMMIT;
  
    --������pmeshid
    MERGE INTO PT_POI PO
    USING (SELECT IP.PID, L.MESH_ID
             FROM RD_LINK L, PT_POI IP
            WHERE L.LINK_PID = IP.LINK_PID) U
    ON (PO.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET PO.PMESH_ID = U.MESH_ID
       WHERE PO.PMESH_ID = 0
          OR PO.PMESH_ID IS NULL;
  
    COMMIT;
  
  END;

  PROCEDURE BATCH_ADMIN_LINK IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH', 'NI_RD_NAME');
    KBDB_DATA_TOOL.PREPARE_DATA('NI_RD_NAME');
  
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_POI_MULTI_RDLINK,SHD_ADDRROAD_LINK');
  
    --��AD_ADMIN������
    --12�����󣬲���Ϊ�����������״̬����Ϊδ��֤
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY FROM RD_LINK L WHERE L.DEVELOP_STATE <> 2;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    MERGE /*+NO_MERGE(U)*/
    INTO AD_ADMIN P
    USING (SELECT ROWID RD
             FROM AD_ADMIN A
            WHERE NOT EXISTS
            (SELECT 1 FROM RD_LINK RL WHERE A.LINK_PID = RL.LINK_PID)) U
    ON (P.ROWID = U.RD)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = 0, P.SIDE = 0, P.NAME_GROUPID = 0;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RELATELINK
      SELECT /*+FULL(A) PARALLEL(A)*/
       A.REGION_ID,
       L.LINK_PID,
       NAVI_GEOM.XLONG(A.GEOMETRY),
       NAVI_GEOM.YLAT(A.GEOMETRY),
       0,
       0
        FROM AD_ADMIN A, SHD_RD_FILTER_LINK L
       WHERE A.LINK_PID = 0
         AND SDO_NN(L.GEOMETRY,
                    A.GEOMETRY,
                    'SDO_NUM_RES=4 DISTANCE=80000 UNIT=METER') = 'TRUE';
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
  
    --�����link��poi�ľ���
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RELATELINK K
    USING RD_LINK L
    ON (K.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET DIST = NAVI_GEOM.GEOM_2_GEOM_DISTANCE(L.GEOMETRY,
                                                   NAVI_GEOM.CREATEPOINT(K.X_GUIDE,
                                                                         K.Y_GUIDE));
    COMMIT;
  
    --��0.5��Ϊ�ݲ�,����֮�������0.5��֮�ڵĶ�����Ҫ��,���ֶ�DISTCOUNT��Ǹ�poi�������������link����
    MERGE /*+PARALLEL(R)*/
    INTO SHD_POI_RELATELINK R
    USING (SELECT POI_PID,
                  LINK_PID,
                  COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) N
             FROM (SELECT T.POI_PID,
                          T.LINK_PID,
                          DIST,
                          DIST - MIN(DIST) OVER(PARTITION BY POI_PID ORDER BY 1) MD
                     FROM SHD_POI_RELATELINK T) K
            WHERE K.MD <= 0.5) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.N;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_ADDRROAD_LINK');
  
    --��������һ��link����д��SHD_POI_RD_LINK,�ȴ��󽻵�
    --�����ڶ����д��SHD_ADDRROAD_LINK,�������ɸѡ
    INSERT /*+APPEND*/
    ALL WHEN DISTCOUNT = 1 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN DISTCOUNT > 1 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0, 0, 0, 0, 0)
      SELECT R.POI_PID,
             R.LINK_PID,
             R.X_GUIDE,
             R.Y_GUIDE,
             R.DIST,
             R.DISTCOUNT
        FROM SHD_POI_RELATELINK R
       WHERE R.DISTCOUNT > 0;
  
    COMMIT;
  
    --����/Ӣ�����Ƿ�Ϊ��
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT DISTINCT L.LINK_PID
             FROM RD_LINK_NAME L, NI_RD_NAME RN
            WHERE L.NAME_GROUPID = RN.NAME_GROUPID
              AND RN.LANG_CODE IN ('ENG', 'POR')) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.FNAMEMARK = 1;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_ADDRROAD_LINK', FALSE);
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT Q.POI_PID, Q.LINK_PID, Q.X_GUIDE, Q.Y_GUIDE
        FROM (SELECT T.POI_PID,
                     T.LINK_PID,
                     T.X_GUIDE,
                     T.Y_GUIDE,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY T.FNAMEMARK DESC, DIST ASC) R
                FROM SHD_ADDRROAD_LINK T) Q
       WHERE Q.R = 1;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    MERGE INTO AD_ADMIN A
    USING SHD_POI_RD_LINK T
    ON (A.REGION_ID = T.POI_PID)
    WHEN MATCHED THEN
      UPDATE SET A.LINK_PID = T.LINK_PID, A.SIDE = 0; --����һ������,�˴�sideͳһ��ֵN,��Ϊ0
  
    COMMIT;
  
    --������pmeshid
  
    MERGE INTO AD_ADMIN A
    USING (SELECT AN.REGION_ID, L.MESH_ID
             FROM RD_LINK L, AD_ADMIN AN
            WHERE L.LINK_PID = AN.LINK_PID) U
    ON (A.REGION_ID = U.REGION_ID)
    WHEN MATCHED THEN
      UPDATE
         SET A.PMESH_ID = U.MESH_ID
       WHERE A.PMESH_ID = 0
          OR A.PMESH_ID IS NULL;
  
    COMMIT;
  
  END;

  PROCEDURE BATCH_HAMLET_LINK(P_TASK_NAME VARCHAR2) IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, 'NI_RD_NAME');
    KBDB_DATA_TOOL.PREPARE_DATA('NI_RD_NAME');
  
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME,
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_POI_MULTI_RDLINK,SHD_ADDRROAD_LINK');
  
    --12�����󣬲�����������ţ����ߣ�����״̬����Ϊδ��֤�������������
  
    ----14��������  �޸�
    ----16�����������Ӳ��ɹ�����·ͣ��λ������·��RD_LINK_FORM= 80��
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ȡ����������RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY
        FROM RD_LINK L
       WHERE L.KIND NOT IN (1, 2, 10, 11, 13) --- ����  �Ǹ�  ʮ��·  �˶�  �ֶ�
            /*AND L.DEVELOP_STATE <> 2*/
         AND L.IS_VIADUCT <> 1 -- �Ǹ߼�
         AND L.APP_INFO <> 3 -- ��δ����
         AND NOT EXISTS
       (SELECT 1
                FROM RD_LINK_FORM F
               WHERE F.LINK_PID = L.LINK_PID
                 AND F.FORM_OF_WAY IN
                     (14, 15, 16, 17, 20, 22, 30, 31, 33, 50,80)); ----ȫ��� �ѵ�  ��������  ���ߵص� ���н� ����ר�õ�  ��  ���  ���� �������LINK ͣ��λ������·
  
    COMMIT;
  
    DELETE FROM SHD_RD_FILTER_LINK FL
     WHERE EXISTS (SELECT 1
              FROM RD_LINK_LIMIT RL
             WHERE RL.LINK_PID = FL.LINK_PID
               AND RL.TYPE = 2
               AND MOD(RL.VEHICLE, 2) = 0);
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ʼ����Ҫ������');
    /*    MERGE \*+NO_MERGE(U)*\
    INTO IX_HAMLET P
    USING (SELECT ROWID RD FROM IX_HAMLET H WHERE H.POI_PID = 0) U
    ON (P.ROWID = U.RD)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = 0, P.SIDE = 0;*/
  
    UPDATE IX_HAMLET P SET P.LINK_PID = 0, P.SIDE = 0;
  
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '�ռ�������ȡhamlet���������4��link');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RELATELINK
      SELECT /*+FULL(P) PARALLEL(P)*/
       P.PID,
       L.LINK_PID,
       P.GEOMETRY.SDO_POINT.X X_GUIDE,
       P.GEOMETRY.SDO_POINT.Y Y_GUIDE,
       0,
       0
        FROM IX_HAMLET P, SHD_RD_FILTER_LINK L
       WHERE P.LINK_PID = 0
         AND SDO_NN(L.GEOMETRY, P.GEOMETRY, 'SDO_NUM_RES=4') = 'TRUE';
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����hamlet�������Link�ľ���');
    --�����link��poi�ľ���
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RELATELINK K
    USING RD_LINK L
    ON (K.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET K.DIST = NAVI_GEOM.GEOM_2_GEOM_DISTANCE(L.GEOMETRY,
                                                     NAVI_GEOM.CREATEPOINT(K.X_GUIDE,
                                                                           K.Y_GUIDE));
    COMMIT;
  
    --��0.5��Ϊ�ݲ�,����֮�������0.5��֮�ڵĶ�����Ҫ��,���ֶ�DISTCOUNT��Ǹ�poi�������������link����
    MERGE /*+PARALLEL(R)*/
    INTO SHD_POI_RELATELINK R
    USING (SELECT POI_PID,
                  LINK_PID,
                  COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) N
             FROM (SELECT T.POI_PID,
                          T.LINK_PID,
                          DIST,
                          DIST - MIN(DIST) OVER(PARTITION BY POI_PID ORDER BY 1) MD
                     FROM SHD_POI_RELATELINK T) K
            WHERE K.MD <= 0.5) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.N;
  
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      'ͨ�����롢���ƺ�LINK_PID��ȡһ������LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_ADDRROAD_LINK');
  
    --��������һ��link����д��SHD_POI_RD_LINK,�ȴ��󽻵�
    --�����ڶ����д��SHD_ADDRROAD_LINK,�������ɸѡ
    INSERT /*+APPEND*/
    ALL WHEN DISTCOUNT = 1 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN DISTCOUNT > 1 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0, 0, 0, 0, 0)
      SELECT R.POI_PID,
             R.LINK_PID,
             R.X_GUIDE,
             R.Y_GUIDE,
             R.DIST,
             R.DISTCOUNT
        FROM SHD_POI_RELATELINK R
       WHERE R.DISTCOUNT > 0;
  
    COMMIT;
  
    --����/Ӣ�����Ƿ�Ϊ��
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT DISTINCT L.LINK_PID
             FROM RD_LINK_NAME L, NI_RD_NAME RN
            WHERE L.NAME_GROUPID = RN.NAME_GROUPID
              AND RN.LANG_CODE IN ('ENG', 'POR')) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.FNAMEMARK = 1;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_ADDRROAD_LINK', FALSE);
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT Q.POI_PID, Q.LINK_PID, Q.X_GUIDE, Q.Y_GUIDE
        FROM (SELECT T.POI_PID,
                     T.LINK_PID,
                     T.X_GUIDE,
                     T.Y_GUIDE,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY T.FNAMEMARK DESC, DIST ASC, T.LINK_PID ASC) R
                FROM SHD_ADDRROAD_LINK T) Q
       WHERE Q.R = 1;
  
    COMMIT;
  
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RD_LINK K
    USING IX_HAMLET I
    ON (K.POI_PID = I.PID)
    WHEN MATCHED THEN
      UPDATE SET K.GEOMETRY = I.GEOMETRY;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����SIDE����������');
    MERGE INTO IX_HAMLET H
    USING (SELECT *
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+FULL(P) parallel(P)*/
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK P,
                                                                   RD_LINK         L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID)))) T
    ON (H.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET H.LINK_PID = T.LINK_PID,
             H.SIDE     = T.SIDE,
             H.X_GUIDE  = T.X,
             H.Y_GUIDE  = T.Y;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '������PMESH_ID');
    --������pmeshid
    MERGE INTO IX_HAMLET H
    USING (SELECT XH.PID, L.MESH_ID
             FROM RD_LINK L, IX_HAMLET XH
            WHERE L.LINK_PID = XH.LINK_PID) U
    ON (H.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET H.PMESH_ID = U.MESH_ID /* WHERE H.POI_PID = 0*/
      ;
  
    COMMIT;
  END;

  PROCEDURE BATCH_DOORNUM_GUIDE_LINK_8(GUIDE_DISTANCE IN NUMBER DEFAULT 50) IS
    MASK   VARCHAR2(50);
    STRSQL VARCHAR2(2000);
    NCOUNT BINARY_INTEGER;
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('POINT_FEATURE_BATCH',
                                 'TMP_PN_MAINDB,TMP_PA_MAINDB,TMP_PA_AUDB,TMP_DIS');
    SELECT COUNT(1)
      INTO NCOUNT
      FROM IX_POINTADDRESS PA
     WHERE PA.GUIDE_LINK_PID = 0;
    IF NCOUNT = 0 THEN
      RETURN;
    END IF;
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK', '����POI����');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('TMP_PN_MAINDB');
    INSERT /*+APPEND*/
    INTO TMP_PN_MAINDB
      (GEOMETRY, NAME, PID, REGION_ID)
      SELECT /*+PARALLEL(T)*/
       T.GEOMETRY, T2.NAME, T.PID, T.REGION_ID
        FROM IX_POI T, IX_POI_NAME T2
       WHERE T.PID = T2.POI_PID
         AND T2.NAME_CLASS = 1
         AND T2.NAME_TYPE = 1
         AND T2.LANG_CODE IN ('CHI', 'CHT')
         AND T.KIND_CODE IN ('120201', '120202');
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('TMP_PA_MAINDB');
    INSERT /*+APPEND*/
    INTO TMP_PA_MAINDB
      (GEOMETRY, PLACE, ESTAB, PID, REGION_ID)
      SELECT /*+PARALLEL(T)*/
       T.GEOMETRY, T2.PLACE, T2.ESTAB, T.PID, T.REGION_ID
        FROM IX_POI T, IX_POI_ADDRESS T2
       WHERE T.PID = T2.POI_PID 
         AND T2.LANG_CODE IN ('CHI', 'CHT')
         AND T.KIND_CODE IN ('120201', '120202');
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('TMP_PA_AUDB');
    INSERT /*+APPEND*/
    INTO TMP_PA_AUDB
      (RID, PID, GEOMETRY, PLACE, REGION_ID)
      SELECT /*+PARALLEL(T)*/
       T.ROWID RID, T.PID, T.GEOMETRY, T2.PLACE, T.REGION_ID
        FROM IX_POINTADDRESS      T,
             IX_POINTADDRESS_NAME T2,
             IX_POINTADDRESS_FLAG T3
       WHERE T.PID = T2.PID
         AND T.PID = T3.PID
         AND T2.LANG_CODE IN ('CHI', 'CHT')
         AND T3.FLAG_CODE = '150000080000';
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('TMP_DIS');
    INSERT /*+APPEND*/
    INTO TMP_DIS
      (RID, PID, DIS)
      SELECT T.RID,
             T2.PID,
             NAVI_GEOM.CALC_DISTANCE(NAVI_GEOM.XLONG(T.GEOMETRY),
                                     NAVI_GEOM.YLAT(T.GEOMETRY),
                                     NAVI_GEOM.XLONG(T2.GEOMETRY),
                                     NAVI_GEOM.YLAT(T2.GEOMETRY)) DIS
        FROM TMP_PA_AUDB T, TMP_PN_MAINDB T2
       WHERE T2.NAME LIKE '%' || T.PLACE || '%'
         AND ABS(NAVI_GEOM.XLONG(T2.GEOMETRY) - NAVI_GEOM.XLONG(T.GEOMETRY)) < 0.02
         AND ABS(NAVI_GEOM.YLAT(T2.GEOMETRY) - NAVI_GEOM.YLAT(T.GEOMETRY)) < 0.02
         AND T.REGION_ID = T2.REGION_ID
         AND T.PLACE IS NOT NULL
      UNION ALL
      SELECT T.RID,
             T2.PID,
             NAVI_GEOM.CALC_DISTANCE(NAVI_GEOM.XLONG(T.GEOMETRY),
                                     NAVI_GEOM.YLAT(T.GEOMETRY),
                                     NAVI_GEOM.XLONG(T2.GEOMETRY),
                                     NAVI_GEOM.YLAT(T2.GEOMETRY)) DIS
        FROM TMP_PA_AUDB T, TMP_PA_MAINDB T2
       WHERE (T2.PLACE = T.PLACE OR T2.ESTAB = T.PLACE)
         AND T.PLACE IS NOT NULL
         AND ABS(NAVI_GEOM.XLONG(T.GEOMETRY) - NAVI_GEOM.XLONG(T2.GEOMETRY)) < 0.02
         AND ABS(NAVI_GEOM.YLAT(T.GEOMETRY) - NAVI_GEOM.YLAT(T2.GEOMETRY)) < 0.02
         AND T.REGION_ID = T2.REGION_ID;
    COMMIT;
  
    MERGE /*+NO_MERGE(T)*/
    INTO IX_POINTADDRESS P
    USING (SELECT RID,
                  PID,
                  DIS,
                  ROW_NUMBER() OVER(PARTITION BY RID ORDER BY DIS, PID) SEQ
             FROM TMP_DIS) T
    ON (P.ROWID = T.RID AND T.SEQ = 1 AND T.DIS < 1000)
    WHEN MATCHED THEN
      UPDATE SET P.SRC_PID = T.PID, P.SRC_TYPE = '�����������POI';
  
    COMMIT;
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK',
                      '����SHD_RD_FILTER_LINK׼��');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK',
                      '����SHD_RD_FILTER_LINK��ʼ');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY
        FROM RD_LINK L
       WHERE L.DEVELOP_STATE <> 2
         AND L.APP_INFO <> 3;
  
    COMMIT;
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK',
                      '��SHD_RD_FILTER_LINK�����ռ�����');
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE, TRUE);
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK',
                      '����SHD_RD_FILTER_LINK����');
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
  
    MASK := '''SDO_NUM_RES=1 DISTANCE=' || GUIDE_DISTANCE ||
            ' UNIT=METER''';
    --����״̬����Ϊ δ��֤
  
    STRSQL := 'INSERT /*+APPEND*/ INTO SHD_POI_RD_LINK(POI_PID,LINK_PID,X_GUIDE,Y_GUIDE,GEOMETRY)
                   SELECT /*+FULL(P) PARALLEL(P)*/ P.PID, L.LINK_PID, P.X_GUIDE, P.Y_GUIDE, P.GEOMETRY
                     FROM IX_POINTADDRESS P, SHD_RD_FILTER_LINK L WHERE 
                     SDO_NN(L.GEOMETRY, NAVI_GEOM.CREATEPOINT(P.X_GUIDE, P.Y_GUIDE),' || MASK ||
              ') = ''TRUE'' AND  P.GUIDE_LINK_PID=0 AND P.PID IN (SELECT PID FROM 
              IX_POINTADDRESS_FLAG WHERE FLAG_CODE = ''150000080000'')';
  
    EXECUTE IMMEDIATE STRSQL;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK', '����8��·');
    MERGE INTO IX_POINTADDRESS P
    USING (SELECT *
             FROM TABLE(POIRELATEDLINK(CURSOR
                                       (SELECT /*+PARALLEL(P)*/
                                         P.POI_PID,
                                         P.LINK_PID,
                                         NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                               P.Y_GUIDE) PGEOMETRY,
                                         L.GEOMETRY LGEOMETRY,
                                         P.GEOMETRY DGEOMETRY
                                          FROM SHD_POI_RD_LINK P, RD_LINK L
                                         WHERE P.LINK_PID = L.LINK_PID
                                           AND L.KIND = 8
                                           AND P.POI_PID IN
                                               (SELECT PID
                                                  FROM IX_POINTADDRESS_FLAG
                                                 WHERE FLAG_CODE =
                                                       '150000080000'))))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.GUIDE_LINK_PID  = T.LINK_PID,
             P.X_GUIDE         = T.X,
             P.Y_GUIDE         = T.Y,
             P.GUIDE_LINK_SIDE = T.SIDE;
    COMMIT;
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK', '����POI��·');
    
    MERGE INTO IX_POINTADDRESS A
    USING (SELECT IA.PID, COMMON_UTIL.GET_PT_LINK_SIDE(RL.GEOMETRY, IA.GEOMETRY) AS SIDE
             FROM IX_POI          P,
                  IX_POINTADDRESS IA,
                  RD_LINK         RL
            WHERE P.PID = IA.SRC_PID
              AND P.KIND_CODE IN('120201', '120202')
              AND P.LINK_PID = RL.LINK_PID) T
    ON (A.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET A.GUIDE_LINK_SIDE = T.SIDE
       WHERE A.GUIDE_LINK_PID = 0;
    COMMIT;  
    
    MERGE INTO IX_POINTADDRESS A
    USING IX_POI P
    ON (A.SRC_PID = P.PID AND P.KIND_CODE IN('120201', '120202'))
    WHEN MATCHED THEN
      UPDATE
         SET A.GUIDE_LINK_PID  = P.LINK_PID,
             A.X_GUIDE         = P.X_GUIDE,
             A.Y_GUIDE         = P.Y_GUIDE
       WHERE A.GUIDE_LINK_PID = 0;
    COMMIT;   
    
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK',
                      '����Ѿ�������·�ĵ�����');
    DELETE FROM SHD_POI_RD_LINK L
     WHERE EXISTS (SELECT 1
              FROM IX_POINTADDRESS A
             WHERE A.PID = L.POI_PID
               AND A.GUIDE_LINK_PID <> 0);
    COMMIT;
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK',
                      '����Ѿ�����POI�ĵ�����');
    DELETE FROM SHD_POI_RD_LINK L
     WHERE EXISTS (SELECT 1
              FROM IX_POINTADDRESS A, IX_POI P
             WHERE A.PID = L.POI_PID
               AND P.KIND_CODE IN ('120201', '120202')
               AND A.SRC_PID = P.PID);
    COMMIT;
  
    NAVI_LOG.LOG_INFO('BATCH_DOORNUM_GUIDE_LINK', '����');
    MERGE INTO IX_POINTADDRESS P
    USING (SELECT *
             FROM TABLE(POIRELATEDLINK(CURSOR
                                       (SELECT /*+PARALLEL(P)*/
                                         P.POI_PID,
                                         P.LINK_PID,
                                         NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                               P.Y_GUIDE) PGEOMETRY,
                                         L.GEOMETRY LGEOMETRY,
                                         P.GEOMETRY DGEOMETRY
                                          FROM SHD_POI_RD_LINK P, RD_LINK L
                                         WHERE P.LINK_PID = L.LINK_PID
                                           AND P.POI_PID IN
                                               (SELECT PID
                                                  FROM IX_POINTADDRESS_FLAG
                                                 WHERE FLAG_CODE =
                                                       '150000080000'))))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.GUIDE_LINK_PID  = T.LINK_PID,
             P.X_GUIDE         = T.X,
             P.Y_GUIDE         = T.Y,
             P.GUIDE_LINK_SIDE = T.SIDE;
    COMMIT;
  END;

  --�����Ƶ�����link
  PROCEDURE BATCH_DOORNUM_GUIDE_LINK(P_TASK_NAME        VARCHAR2 DEFAULT 'BATCH_DOORNUM_GUIDE_LINK',
                                     GUIDE_DISTANCE     IN NUMBER DEFAULT 50,
                                     AUC_GUIDE_DISTANCE IN NUMBER DEFAULT 3) IS
    MASK   VARCHAR2(50);
    NCOUNT BINARY_INTEGER;
  
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME,
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_IX_POI_TEMP,SHD_CROSS_TEMP,SHD_LINK_NODE_GD');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '������ʱ��SHD_IX_POI_TEMP');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_IX_POI_TEMP');
    INSERT INTO SHD_IX_POI_TEMP
      (PID, LINK_PID, SIDE, X_GUIDE, Y_GUIDE)
      SELECT PID, GUIDE_LINK_PID, GUIDE_LINK_SIDE, X_GUIDE, Y_GUIDE
        FROM IX_POINTADDRESS;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ʼ���������---800');
    BATCH_DOORNUM_GUIDE_LINK_8;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ʼ���������---');
    --��FLAG_CODE='150000060000'������ ����link��Ч�ı���,�����ĵ���������Ϊ0
    /*by kwz del->
    MERGE \*+NO_MERGE(U)*\
     INTO IX_POINTADDRESS A
     USING (SELECT PID
              FROM IX_POINTADDRESS
            MINUS
            SELECT PA.PID
              FROM IX_POINTADDRESS_FLAG PF, IX_POINTADDRESS PA, RD_LINK RL
             WHERE PF.FLAG_CODE = '150000060000'
               AND PA.PID = PF.PID
               AND PA.GUIDE_LINK_PID = RL.LINK_PID) U
     ON (A.PID = U.PID)
     WHEN MATCHED THEN
       UPDATE SET A.GUIDE_LINK_PID = 0, A.GUIDE_LINK_SIDE = 0;
     <-by kwz del*/
    --by kwz add->
    MERGE /*+NO_MERGE(U)*/
    INTO IX_POINTADDRESS A
    USING (SELECT PID
             FROM IX_POINTADDRESS
            WHERE GUIDE_LINK_PID = 0
           MINUS
           SELECT PF.PID
             FROM IX_POINTADDRESS_FLAG PF
            WHERE PF.FLAG_CODE = '150000060000') U
    ON (A.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE SET A.GUIDE_LINK_SIDE = 0;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����SHD_LINK_NODE_GD');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_LINK_NODE_GD');
    INSERT INTO SHD_LINK_NODE_GD
      (NODE_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY FROM RD_LINK L WHERE L.APP_INFO <> 2;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ʾ3�׵�����');
    UPDATE IX_POINTADDRESS
       SET GUIDE_LINK_PID = -1, GUIDE_LINK_SIDE = 0
     WHERE GUIDE_LINK_PID = 0
       AND PID IN (SELECT PF.PID
                     FROM IX_POINTADDRESS_FLAG PF
                    WHERE PF.FLAG_CODE = '150000060000');
    --<-by kwz add
  
    COMMIT;
  
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, 'SHD_RD_FILTER_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    --by kwz add->
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_LINK_NODE_GD', 'GEOMETRY');
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '50������');
    MASK := 'DISTANCE=' || GUIDE_DISTANCE || ' UNIT=METER';
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, POI_PID, X_GUIDE, Y_GUIDE, FLAG)
      SELECT /*+FULL(P) PARALLEL(P)*/
       L.NODE_PID LINK_PID,
       P.PID,
       NAVI_GEOM.XLONG(P.GEOMETRY),
       NAVI_GEOM.YLAT(P.GEOMETRY),
       2
        FROM IX_POINTADDRESS P, SHD_LINK_NODE_GD L
       WHERE P.GUIDE_LINK_PID = 0
         AND SDO_WITHIN_DISTANCE(L.GEOMETRY, P.GEOMETRY, MASK) = 'TRUE';
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '3������');
    MASK := 'DISTANCE=' || AUC_GUIDE_DISTANCE || ' UNIT=METER';
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, POI_PID, X_GUIDE, Y_GUIDE, FLAG)
      SELECT /*+FULL(P) PARALLEL(P)*/
       L.NODE_PID LINK_PID, P.PID, P.X_GUIDE, P.Y_GUIDE, 2
        FROM IX_POINTADDRESS P, SHD_LINK_NODE_GD L
       WHERE P.GUIDE_LINK_PID = -1
         AND SDO_WITHIN_DISTANCE(L.GEOMETRY,
                                 NAVI_GEOM.CREATEPOINT(P.X_GUIDE, P.Y_GUIDE),
                                 MASK) = 'TRUE';
    COMMIT;
  
    UPDATE IX_POINTADDRESS
       SET GUIDE_LINK_PID = 0
     WHERE GUIDE_LINK_PID = -1;
  
    /*MASK := 'DISTANCE=' || AUC_GUIDE_DISTANCE || ' UNIT=METER';
    INSERT \*+APPEND*\
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, POI_PID, X_GUIDE, Y_GUIDE, FLAG)
      SELECT \*+FULL(PA) PARALLEL(PA)*\
       L.LINK_PID, PA.PID, PA.X_GUIDE, PA.Y_GUIDE, 2
        FROM IX_POINTADDRESS_FLAG PF, IX_POINTADDRESS PA, RD_LINK L
       WHERE PF.FLAG_CODE = '150000060000'
         AND PF.PID = PA.PID
         AND NOT EXISTS
       (SELECT 1 FROM RD_LINK R WHERE R.LINK_PID = PA.GUIDE_LINK_PID)
         AND SDO_WITHIN_DISTANCE(L.GEOMETRY,
                                 NAVI_GEOM.CREATEPOINT(PA.X_GUIDE,
                                                       PA.Y_GUIDE),
                                 MASK) = 'TRUE';
    COMMIT;
    
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '50������');
    MASK := 'DISTANCE=' || GUIDE_DISTANCE || ' UNIT=METER';
    INSERT \*+APPEND*\
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, POI_PID, X_GUIDE, Y_GUIDE, FLAG)
      SELECT \*+FULL(P) PARALLEL(P)*\
       L.LINK_PID,
       P.PID,
       NAVI_GEOM.XLONG(P.GEOMETRY),
       NAVI_GEOM.YLAT(P.GEOMETRY),
       2
        FROM IX_POINTADDRESS P, RD_LINK L
       WHERE NOT EXISTS
       (SELECT 1
                FROM IX_POINTADDRESS_FLAG PF
               WHERE P.PID = PF.PID
                 AND PF.FLAG_CODE = '150000060000')
         AND SDO_WITHIN_DISTANCE(L.GEOMETRY, P.GEOMETRY, MASK) = 'TRUE';
    COMMIT;*/
  
    /*    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����GEOMETRY');
    MERGE \*+NO_MERGE(U)*\
    INTO SHD_RD_FILTER_LINK R
    USING RD_LINK U
    ON (R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.GEOMETRY = U.GEOMETRY;*/
    --<-by kwz add
  
    /*by kwz add->
    INSERT \*+APPEND*\
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY FROM RD_LINK L WHERE L.DEVELOP_STATE <> 2;
    <-by kwz add*/
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    --by kwz PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    --by kwz add->
    /* --1��
    UPDATE SHD_RD_FILTER_LINK SET FLAG = 1 WHERE 1 = 2;
    COMMIT;
    
    --2��
    UPDATE SHD_RD_FILTER_LINK
       SET FLAG = 2
     WHERE LINK_PID IN (SELECT LINK_PID
                          FROM RD_LINK
                         WHERE KIND IN (8) --������·
                            OR IMI_CODE <> 0 --IMI
                        UNION ALL
                        SELECT LINK_PID
                          FROM RD_LINK_FORM
                         WHERE FORM_OF_WAY IN (52) --�����ڵ�·
                        UNION ALL
                        SELECT LINK_PID
                          FROM RD_LINK_SPEEDLIMIT
                         WHERE SPEED_CLASS IN (1, 2, 3) --�ٶȵȼ�Ϊ1��2��3�ȼ�
                        UNION ALL
                        SELECT LINK_PID
                          FROM RD_LINK_LIMIT
                         WHERE TYPE IN (4) --ʩ��������
                        );
    COMMIT;*/
  
    --3��
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����3��');
    UPDATE SHD_RD_FILTER_LINK
       SET FLAG = 3
     WHERE LINK_PID IN
           (SELECT LINK_PID
              FROM RD_LINK_FORM
             WHERE FORM_OF_WAY IN (15, 33, 11, 10, 30, 12, 13, 14) --�ѵ�,����,JCT,IC,��,��������ͣ����,ȫ���
            UNION ALL
            SELECT LINK_PID
              FROM RD_LINK
             WHERE IS_VIADUCT = 1 --�߼�
                OR KIND IN (1, 2) --���١����и��ٵȼ���·
            UNION ALL
            SELECT IN_LINK_PID FROM RD_GATE WHERE 1 = 1 --���ŵĽ�����
            )
       AND FLAG = 2;
    COMMIT;
  
    --4��
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����4��');
    UPDATE SHD_RD_FILTER_LINK
       SET FLAG = 4
     WHERE LINK_PID IN
           (SELECT LINK_PID
              FROM RD_LINK_FORM
             WHERE FORM_OF_WAY IN (31, 17, 16, 50, 80) --���,���ߵص�,��������,�������link,ͣ��λ������·
            UNION ALL
            SELECT LINK_PID
              FROM RD_LINK
             WHERE DEVELOP_STATE = 2 --δ��֤��·
                OR KIND IN (11, 13) --�˶ɡ��ֶ�
            UNION ALL
            SELECT LINK_PID
              FROM RD_LINK_LIMIT C
             WHERE BITAND(C.VEHICLE, 2147483649) = 1)
       AND FLAG IN (2, 3);
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '������߼�');
    UPDATE SHD_RD_FILTER_LINK L
       SET FLAG = 1
     WHERE EXISTS (SELECT 1
              FROM (SELECT POI_PID, MIN(FLAG) FLAG
                      FROM SHD_RD_FILTER_LINK
                     WHERE FLAG < 4
                     GROUP BY POI_PID) P
             WHERE P.POI_PID = L.POI_PID
               AND P.FLAG = L.FLAG
               AND P.FLAG > 1);
  
    /* I := 1;
    LOOP
      EXIT WHEN I > 2;
      UPDATE SHD_RD_FILTER_LINK L
         SET FLAG = FLAG - 1
       WHERE EXISTS (SELECT 1
                FROM (SELECT POI_PID, MIN(FLAG) FLAG
                        FROM SHD_RD_FILTER_LINK
                       GROUP BY POI_PID) P
               WHERE P.POI_PID = L.POI_PID
                 AND P.FLAG > 1);
      COMMIT;
      I := I + 1;
    END LOOP;*/
  
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, 'SHD_POI_RELATELINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ȡ����߼�');
    INSERT /*+APPEND*/
    INTO SHD_POI_RELATELINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT /*+FULL(P) PARALLEL(P)*/
       POI_PID, LINK_PID, X_GUIDE, Y_GUIDE
        FROM SHD_RD_FILTER_LINK P
       WHERE FLAG = 1;
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '������߼�');
    --<-by kwz add
  
    /*by kwz add->
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
    
    INSERT \*+APPEND*\
    INTO SHD_POI_RELATELINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT \*+FULL(P) PARALLEL(P)*\
       PA.PID, L.LINK_PID, PA.X_GUIDE, PA.Y_GUIDE
        FROM IX_POINTADDRESS PA, SHD_RD_FILTER_LINK L
       WHERE L.POI_PID = PA.PID
         AND L.FLAG = 1;
    
    --flag_code=150000060000�ҵ������������4��link
    MASK := '''SDO_NUM_RES=4 DISTANCE=' || AUC_GUIDE_DISTANCE ||
            ' UNIT=METER''';
    
    STRSQL := 'INSERT \*+APPEND*\
    INTO SHD_POI_RELATELINK(POI_PID,LINK_PID,X_GUIDE,Y_GUIDE)
      SELECT \*+FULL(P) PARALLEL(P)*\
       PA.PID, L.LINK_PID, PA.X_GUIDE, PA.Y_GUIDE
        FROM IX_POINTADDRESS_FLAG PF, IX_POINTADDRESS PA, SHD_RD_FILTER_LINK L
       WHERE PF.FLAG_CODE=''150000060000'' AND PF.PID=PA.PID
         AND PA.GUIDE_LINK_PID=0
         AND SDO_NN(L.GEOMETRY,
                    NAVI_GEOM.CREATEPOINT(PA.X_GUIDE, PA.Y_GUIDE),
                    ' || MASK || ') = ''TRUE''';
    
    EXECUTE IMMEDIATE STRSQL;
    
    COMMIT;
    
    --flag_code����150000060000�ҵ������������4��link
    MASK := '''SDO_NUM_RES=4 DISTANCE=' || GUIDE_DISTANCE ||
            ' UNIT=METER''';
    
    STRSQL := 'INSERT \*+APPEND*\ INTO SHD_POI_RELATELINK(POI_PID,LINK_PID,X_GUIDE,Y_GUIDE)
                   SELECT \*+FULL(P) PARALLEL(P)*\
                    P.PID, L.LINK_PID, NAVI_GEOM.XLONG(P.GEOMETRY),NAVI_GEOM.YLAT(P.GEOMETRY)
                     FROM IX_POINTADDRESS P, SHD_RD_FILTER_LINK L
                    WHERE NOT EXISTS (SELECT 1
                     FROM IX_POINTADDRESS_FLAG PF
                    WHERE P.PID = PF.PID
                      AND PF.FLAG_CODE = ''150000060000'') AND SDO_NN(L.GEOMETRY, P.GEOMETRY,' || MASK ||
              ') = ''TRUE''';
    
    EXECUTE IMMEDIATE STRSQL;
    <-by kwz add*/
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
  
    --�����link������Ƶľ���,���ڼ������ȽϷ�ʱ��,���Զ��ڽ���һ��link�����ĵ����ƵľͲ�������,ֱ��ʹ��
    MERGE /*+NO_MERGE(U)*/
    INTO SHD_POI_RELATELINK R
    USING (SELECT P.*, COUNT(1) OVER(PARTITION BY P.POI_PID ORDER BY 1) C
             FROM SHD_POI_RELATELINK P) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.C, R.DIST = 0;
    COMMIT;
  
    MERGE /*+NO_MERGE(U)*/
    INTO SHD_POI_RELATELINK K
    USING (SELECT /*+PARALLEL(S)*/
            S.POI_PID,
            S.LINK_PID,
            NAVI_GEOM.GEOM_2_GEOM_DISTANCE(RL.GEOMETRY,
                                           NAVI_GEOM.CREATEPOINT(S.X_GUIDE,
                                                                 S.Y_GUIDE)) DIS
             FROM SHD_POI_RELATELINK S, RD_LINK RL
            WHERE S.DISTCOUNT > 1
              AND S.LINK_PID = RL.LINK_PID) U
    ON (K.POI_PID = U.POI_PID AND K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.DIST = U.DIS;
  
    COMMIT;
  
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, 'SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, GEOMETRY)
      SELECT C.POI_PID, C.LINK_PID, C.X_GUIDE, C.Y_GUIDE, PA.GEOMETRY
        FROM (SELECT P.*,
                     ROW_NUMBER() OVER(PARTITION BY P.POI_PID ORDER BY P.DIST ASC, P.LINK_PID ASC) R
                FROM SHD_POI_RELATELINK P) C,
             IX_POINTADDRESS PA
       WHERE C.R = 1
         AND C.POI_PID = PA.PID;
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    MERGE INTO IX_POINTADDRESS P
    USING (SELECT *
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+PARALLEL(P)*/
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY LGEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK P,
                                                                   RD_LINK         L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID)))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.GUIDE_LINK_PID  = T.LINK_PID,
             P.X_GUIDE         = T.X,
             P.Y_GUIDE         = T.Y,
             P.GUIDE_LINK_SIDE = T.SIDE;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����ë��---');
    SELECT COUNT(1) INTO NCOUNT FROM M_MESH_TYPE;
    IF NVL(NCOUNT, 0) > 0 THEN
      NAVI_LOG.LOG_INFO(P_TASK_NAME, '������ʱ��SHD_CROSS_TEMP');
      PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_CROSS_TEMP');
      INSERT INTO SHD_CROSS_TEMP
        (PID) WITH M0 AS
        (SELECT LINK_PID
           FROM RD_LINK
          WHERE MESH_ID IN
                (SELECT A.MESH_ID
                   FROM RD_LINK A
                 MINUS
                 SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1)),
      
      M1 AS
        (SELECT A.MESH_ID
           FROM IX_POINTADDRESS A
         MINUS
         SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1)
      
        SELECT S.PID
          FROM IX_POINTADDRESS A, SHD_IX_POI_TEMP S
         WHERE A.PID = S.PID
           AND A.GUIDE_LINK_PID IN (SELECT LINK_PID FROM M0)
        
        UNION
        
        SELECT S.PID
          FROM IX_POINTADDRESS A, SHD_IX_POI_TEMP S
         WHERE A.PID = S.PID
           AND A.MESH_ID IN (SELECT MESH_ID FROM M1);
      COMMIT;
    
      NAVI_LOG.LOG_INFO(P_TASK_NAME, '�������������0---');

    /*      FOR RC IN (SELECT PID, GUIDE_LINK_PID LINK_PID
                   FROM IX_POINTADDRESS
                  WHERE PID IN (SELECT PID FROM SHD_CROSS_TEMP)) LOOP
      
        PIPELINE_VALIDATION_UTIL.ADD_EXCEPTION_AND_OBJECT2(P_TASK_NAME,
                                                           '����link = ' ||
                                                           RC.LINK_PID ||
                                                           '������Ʋ����ں���ͼ��',
                                                           'BATCH_POINTADDRESS_GUIDELINK',
                                                           'IX_POINTADDRESS',
                                                           USER,
                                                           RC.PID);
      END LOOP;*/
    
      MERGE INTO IX_POINTADDRESS P
      USING (SELECT PID, LINK_PID, SIDE, X_GUIDE, Y_GUIDE
               FROM SHD_IX_POI_TEMP S
              WHERE S.PID IN (SELECT PID FROM SHD_CROSS_TEMP)) T
      ON (P.PID = T.PID)
      WHEN MATCHED THEN
        UPDATE
           SET P.GUIDE_LINK_PID  = T.LINK_PID,
               P.X_GUIDE         = T.X_GUIDE,
               P.Y_GUIDE         = T.Y_GUIDE,
               P.GUIDE_LINK_SIDE = T.SIDE;
      COMMIT;
    END IF;
  
  /*  NAVI_LOG.LOG_INFO(P_TASK_NAME, '�������������1---');
  
    FOR RC IN (SELECT A.PID, L.LINK_PID
                 FROM IX_POINTADDRESS A, RD_LINK L
                WHERE A.GUIDE_LINK_PID = L.LINK_PID
                  AND A.GUIDE_LINK_SIDE = 3
                  AND L.LEFT_REGION_ID <> L.RIGHT_REGION_ID) LOOP
    
      PIPELINE_VALIDATION_UTIL.ADD_EXCEPTION_AND_OBJECT2(P_TASK_NAME,
                                                         '����link = ' ||
                                                         RC.LINK_PID ||
                                                         '��������������ͬ',
                                                         'BATCH_POINTADDRESS_GUIDELINK',
                                                         'IX_POINTADDRESS',
                                                         USER,
                                                         RC.PID);
    END LOOP;
    COMMIT;*/
  END;

  /*
  PROCEDURE BATCH_IXIC_LINK IS
  BEGIN
     MERGE INTO IX_IC C
     USING (SELECT CM.GLM_ID IC_PID, LM.GLM_ID LINK_PID, NM.GLM_ID NODE_PID
              FROM I_POI_IC_G      I,
                   SHD_RD_LINK_MAP LM,
                   SHD_RD_NODE_MAP NM,
                   SHD_IX_IC_MAP   CM
             WHERE LENGTH(I.LINKID) = 11
               AND I.POINUM = CM.SOURCE_ID
               AND TO_NUMBER(SUBSTR(I.LINKID, 1, 6)) = LM.SOURCE_MESH_ID
               AND TO_NUMBER(SUBSTR(I.LINKID, 7, 5)) = LM.SOURCE_ID
               AND TO_NUMBER(SUBSTR(I.LINKID, 1, 6)) = NM.SOURCE_MESH_ID
               AND TO_NUMBER(I.NODEID) = NM.SOURCE_ID) U
     ON (C.PID = U.IC_PID)
     WHEN MATCHED THEN
       UPDATE SET C.LINK_PID = U.LINK_PID, C.NODE_PID = U.NODE_PID;
     COMMIT;
  END;
  
  
  PROCEDURE BATCH_TOLLGATE_LINK IS
  BEGIN
  
     MERGE INTO IX_TOLLGATE T
     USING (SELECT TM.GLM_ID TOLL_PID, LM.GLM_ID LINK_PID, NM.GLM_ID NODE_PID
              FROM I_POI_TOLL_G        T,
                   SHD_RD_LINK_MAP     LM,
                   SHD_RD_NODE_MAP     NM,
                   SHD_IX_TOLLGATE_MAP TM
             WHERE LENGTH(T.LINKID) = 11
               AND T.POINUM = TM.SOURCE_ID
               AND TO_NUMBER(SUBSTR(T.LINKID, 1, 6)) = LM.SOURCE_MESH_ID
               AND TO_NUMBER(SUBSTR(T.LINKID, 7, 5)) = LM.SOURCE_ID
               AND TO_NUMBER(SUBSTR(T.LINKID, 1, 6)) = NM.SOURCE_MESH_ID
               AND TO_NUMBER(T.NODEID) = NM.SOURCE_ID) U
     ON (T.PID = U.TOLL_PID)
     WHEN MATCHED THEN
       UPDATE SET T.LINK_PID = U.LINK_PID, T.NODE_PID = U.NODE_PID;
     COMMIT;
  END;
  
  */

  PROCEDURE BATCH_ERASE_RELATE_LINK IS
    NCOUNT BINARY_INTEGER := 0;
  BEGIN
    --ɾ������NI_DATALIST��Χ�ĵ�Ҫ�صĹ���link
    SELECT COUNT(1) INTO NCOUNT FROM M_MESH_TYPE;
    IF NCOUNT = 0 THEN
      RETURN;
    END IF;
  
    --IX_POI
    MERGE INTO IX_POI PO
    USING (SELECT IP.PID
             FROM IX_POI IP
            WHERE NOT EXISTS (SELECT 1
                     FROM M_MESH_TYPE M
                    WHERE IP.MESH_ID = M.MESH_ID
                      AND M.TYPE IN (1))) U
    ON (PO.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET PO.LINK_PID     = 0,
             PO.SIDE         = 0,
             PO.NAME_GROUPID = 0,
             PO.PMESH_ID     = 0;
  
    COMMIT;
  
    --PT_POI
    MERGE INTO PT_POI PT
    USING (SELECT PP.PID
             FROM PT_POI PP
            WHERE NOT EXISTS (SELECT 1
                     FROM M_MESH_TYPE M
                    WHERE PP.MESH_ID = M.MESH_ID
                      AND M.TYPE IN (1))) U
    ON (PT.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET PT.LINK_PID     = 0,
             PT.SIDE         = 0,
             PT.NAME_GROUPID = 0,
             PT.PMESH_ID     = 0;
    COMMIT;
  
    --AD_ADMIN
    MERGE INTO AD_ADMIN AA
    USING (SELECT AD.REGION_ID
             FROM AD_ADMIN AD
            WHERE NOT EXISTS (SELECT 1
                     FROM M_MESH_TYPE M
                    WHERE AD.MESH_ID = M.MESH_ID
                      AND M.TYPE IN (1))) U
    ON (AA.REGION_ID = U.REGION_ID)
    WHEN MATCHED THEN
      UPDATE
         SET AA.LINK_PID     = 0,
             AA.SIDE         = 0,
             AA.NAME_GROUPID = 0,
             AA.PMESH_ID     = 0;
    COMMIT;
  
    --IX_HAMLET
    MERGE INTO IX_HAMLET H
    USING (SELECT I.PID
             FROM IX_HAMLET I
            WHERE NOT EXISTS (SELECT 1
                     FROM M_MESH_TYPE M
                    WHERE I.MESH_ID = M.MESH_ID
                      AND M.TYPE IN (1))) U
    ON (H.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET H.LINK_PID = 0, H.SIDE = 0, H.NAME_GROUPID = 0, H.PMESH_ID = 0;
    COMMIT;
  
    --IX_POINTADDRESS
    MERGE INTO IX_POINTADDRESS PA
    USING (SELECT P.PID
             FROM IX_POINTADDRESS P
            WHERE NOT EXISTS (SELECT 1
                     FROM M_MESH_TYPE M
                    WHERE P.MESH_ID = M.MESH_ID
                      AND M.TYPE IN (1))) U
    ON (PA.PID = U.PID)
    WHEN MATCHED THEN
      UPDATE
         SET PA.GUIDE_LINK_PID      = 0,
             PA.GUIDE_LINK_SIDE     = 0,
             PA.LOCATE_LINK_PID     = 0,
             PA.LOCATE_NAME_GROUPID = 0,
             PA.LOCATE_LINK_SIDE    = 0;
    COMMIT;
  
  END;

  PROCEDURE BATCH_POST_CODE_LINK(P_TASK_NAME VARCHAR2) IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME, 'NI_RD_NAME');
    KBDB_DATA_TOOL.PREPARE_DATA('NI_RD_NAME');
  
    NAVI_TABLE_SYNC.SYNC_TABLES2(P_TASK_NAME,
                                 'SHD_RD_FILTER_LINK,SHD_POI_RELATELINK,SHD_POI_RD_LINK,SHD_POI_MULTI_RDLINK,SHD_ADDRROAD_LINK');
  
    --��AD_ADMIN������
    --12�����󣬲���Ϊ�����������״̬����Ϊδ��֤����·����Ϊδ����
    --16�����󣬲���Ϊͣ��λ������·
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY
        FROM RD_LINK L /*WHERE L.DEVELOP_STATE <> 2*/
       WHERE L.APP_INFO <> 3
         AND L.KIND NOT IN (1, 2, 10, 11, 13)
         AND L.IS_VIADUCT <> 1
         AND NOT EXISTS
       (SELECT 1
                FROM RD_LINK_FORM RLF
               WHERE L.LINK_PID = RLF.LINK_PID
                 AND RLF.FORM_OF_WAY IN
                     (30, 31, 20, 22, 14, 15, 16, 17, 33, 50,80));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    /* MERGE \*+NO_MERGE(U)*\
    INTO IX_POSTCODE P
    USING (SELECT ROWID RD
             FROM IX_POSTCODE A
            WHERE NOT EXISTS
            (SELECT 1 FROM RD_LINK RL WHERE A.LINK_PID = RL.LINK_PID AND RL.APP_INFO <> 3)) U
    ON (P.ROWID = U.RD)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = 0, P.SIDE = 0, P.NAME_GROUPID = 0;*/
    UPDATE IX_POSTCODE P
       SET P.LINK_PID = 0, P.SIDE = 0, P.NAME_GROUPID = 0;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RELATELINK');
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RELATELINK
      SELECT /*+FULL(A) PARALLEL(A)*/
       A.POST_ID,
       L.LINK_PID,
       NAVI_GEOM.XLONG(A.GEOMETRY),
       NAVI_GEOM.YLAT(A.GEOMETRY),
       0,
       0
        FROM IX_POSTCODE A, SHD_RD_FILTER_LINK L
       WHERE A.LINK_PID = 0
         AND SDO_NN(L.GEOMETRY,
                    A.GEOMETRY,
                    'SDO_NUM_RES=4 DISTANCE=80000 UNIT=METER') = 'TRUE';
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RELATELINK', FALSE);
  
    --�����link��poi�ľ���
    MERGE /*+PARALLEL(K)*/
    INTO SHD_POI_RELATELINK K
    USING RD_LINK L
    ON (K.LINK_PID = L.LINK_PID)
    WHEN MATCHED THEN
      UPDATE
         SET DIST = NAVI_GEOM.GEOM_2_GEOM_DISTANCE(L.GEOMETRY,
                                                   NAVI_GEOM.CREATEPOINT(K.X_GUIDE,
                                                                         K.Y_GUIDE));
    COMMIT;
  
    --��0.5��Ϊ�ݲ�,����֮�������0.5��֮�ڵĶ�����Ҫ��,���ֶ�DISTCOUNT��Ǹ�poi�������������link����
    MERGE /*+PARALLEL(R)*/
    INTO SHD_POI_RELATELINK R
    USING (SELECT POI_PID,
                  LINK_PID,
                  COUNT(1) OVER(PARTITION BY POI_PID ORDER BY 1) N
             FROM (SELECT T.POI_PID,
                          T.LINK_PID,
                          DIST,
                          DIST - MIN(DIST) OVER(PARTITION BY POI_PID ORDER BY 1) MD
                     FROM SHD_POI_RELATELINK T) K
            WHERE K.MD <= 0.5) U
    ON (R.POI_PID = U.POI_PID AND R.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET R.DISTCOUNT = U.N;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_ADDRROAD_LINK');
  
    --��������һ��link����д��SHD_POI_RD_LINK,�ȴ��󽻵�
    --�����ڶ����д��SHD_ADDRROAD_LINK,�������ɸѡ
    INSERT /*+APPEND*/
    ALL WHEN DISTCOUNT = 1 THEN INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE) WHEN DISTCOUNT > 1 THEN INTO SHD_ADDRROAD_LINK
      (POI_PID,
       LINK_PID,
       X_GUIDE,
       Y_GUIDE,
       DIST,
       FNAMEMARK,
       GFNAME,
       BNAME,
       NAMEMARK,
       LDNUM)
    VALUES
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE, DIST, 0, 0, 0, 0, 0)
      SELECT R.POI_PID,
             R.LINK_PID,
             R.X_GUIDE,
             R.Y_GUIDE,
             R.DIST,
             R.DISTCOUNT
        FROM SHD_POI_RELATELINK R
       WHERE R.DISTCOUNT > 0;
  
    COMMIT;
  
    --����/Ӣ�����Ƿ�Ϊ��
    MERGE /*+PARALLEL(K)*/
    INTO SHD_ADDRROAD_LINK K
    USING (SELECT DISTINCT L.LINK_PID
             FROM RD_LINK_NAME L, NI_RD_NAME RN
            WHERE L.NAME_GROUPID = RN.NAME_GROUPID
              AND RN.LANG_CODE IN ('ENG', 'POR')) U
    ON (K.LINK_PID = U.LINK_PID)
    WHEN MATCHED THEN
      UPDATE SET K.FNAMEMARK = 1;
  
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_ADDRROAD_LINK', FALSE);
  
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, X_GUIDE, Y_GUIDE)
      SELECT Q.POI_PID, Q.LINK_PID, Q.X_GUIDE, Q.Y_GUIDE
        FROM (SELECT T.POI_PID,
                     T.LINK_PID,
                     T.X_GUIDE,
                     T.Y_GUIDE,
                     ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY T.FNAMEMARK DESC, DIST ASC) R
                FROM SHD_ADDRROAD_LINK T) Q
       WHERE Q.R = 1;
  
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_POI_RD_LINK', FALSE);
  
    /*    MERGE INTO IX_POSTCODE A
    USING (SELECT SL.POI_PID, SL.LINK_PID, RL.GEOMETRY
             FROM SHD_POI_RD_LINK SL, SHD_RD_FILTER_LINK RL
            WHERE SL.LINK_PID = RL.LINK_PID) T
    ON (A.POST_ID = T.POI_PID)
    WHEN MATCHED THEN
      UPDATE
         SET A.LINK_PID = T.LINK_PID,
             A.SIDE     = COMMON_UTIL.GET_PT_LINK_SIDE(T.GEOMETRY,
                                                       A.GEOMETRY); --����һ������,�˴�sideͳһ��ֵN,��Ϊ0
    
    COMMIT;*/
  
    MERGE INTO IX_POSTCODE P
    USING (SELECT *
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+PARALLEL(P)*/
                                                             SL.POI_PID,
                                                             SL.LINK_PID,
                                                             A.GEOMETRY  PGEOMETRY,
                                                             RL.GEOMETRY LGEOMETRY,
                                                             A.GEOMETRY  DGEOMETRY
                                                              FROM SHD_POI_RD_LINK SL,
                                                                   SHD_RD_FILTER_LINK RL,
                                                                   (SELECT T.POI_PID POST_ID,
                                                                           NAVI_GEOM.CREATEPOINT(MIN(T.X_GUIDE),
                                                                                                 MIN(T.Y_GUIDE)) GEOMETRY
                                                                      FROM SHD_POI_RELATELINK T
                                                                     GROUP BY T.POI_PID) A
                                                             WHERE SL.LINK_PID =
                                                                   RL.LINK_PID
                                                               AND A.POST_ID =
                                                                   SL.POI_PID)))) T
    ON (P.POST_ID = T.PID)
    WHEN MATCHED THEN
      UPDATE SET P.LINK_PID = T.LINK_PID, P.SIDE = T.SIDE;
    COMMIT;
  
    --������pmeshid
    /*
    MERGE INTO IX_POSTCODE A
    USING (SELECT AN.POST_ID, L.MESH_ID
             FROM RD_LINK L, IX_POSTCODE AN
            WHERE L.LINK_PID = AN.LINK_PID) U
    ON (A.POST_ID = U.POST_ID)
    WHEN MATCHED THEN
      UPDATE
         SET A.PMESH_ID = U.MESH_ID
       WHERE A.PMESH_ID = 0
          OR A.PMESH_ID IS NULL;
    
    COMMIT;*/
  
    NAVI_POI_BATCH_PROCEDURE.INSERT_LOG(0, 0);
  
  END;

  --IX_HAMLET��ĸ��
  PROCEDURE BATCH_HAMLET_INTO(P_TASK_NAME VARCHAR2) IS
    V_PID   NUMBER(10);
    V_COUNT NUMBER(10);
  BEGIN
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ȡTAB����');
    SELECT COUNT(1) INTO V_COUNT FROM M_MESH_TYPE;
    IF V_COUNT > 0 THEN
      SELECT COUNT(1)
        INTO V_COUNT
        FROM SHD_HAMLET_TAB_FILE@DMS
       WHERE NAVI_GEOM.GET25KMAPNUMBER1(GEOMETRY) IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
      V_PID := COMMON_UTIL.APPLY_PID('DMS',
                                     'IX_HAMLET',
                                     V_COUNT,
                                     P_TASK_NAME);
    
      INSERT INTO SHD_HAMLET_TAB
        (PID,
         KIND_CODE,
         GEOMETRY,
         NAME,
         ENGNAME_QC,
         ENGNAME_JC,
         PY,
         FLAG_CODE)
        SELECT ROWNUM + V_PID - 1,
               KIND,
               GEOMETRY,
               NAME,
               ENGNAME_QC,
               ENGNAME_JC,
               PY,
               FLAG_CODE
          FROM SHD_HAMLET_TAB_FILE@DMS
         WHERE NAVI_GEOM.GET25KMAPNUMBER1(GEOMETRY) IN
               (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    ELSE
      SELECT COUNT(1) INTO V_COUNT FROM SHD_HAMLET_TAB_FILE@DMS;
      V_PID := COMMON_UTIL.APPLY_PID('DMS',
                                     'IX_HAMLET',
                                     V_COUNT,
                                     P_TASK_NAME);
    
      INSERT INTO SHD_HAMLET_TAB
        (PID,
         KIND_CODE,
         GEOMETRY,
         NAME,
         ENGNAME_QC,
         ENGNAME_JC,
         PY,
         FLAG_CODE)
        SELECT ROWNUM + V_PID - 1,
               KIND,
               GEOMETRY,
               NAME,
               ENGNAME_QC,
               ENGNAME_JC,
               PY,
               FLAG_CODE
          FROM SHD_HAMLET_TAB_FILE@DMS;
    END IF;
    COMMIT;
  
    DELETE FROM SHD_HAMLET_TAB_FILE@DMS;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����AD_FACE�ռ�����');
    PIPELINE_SDO.SDO_INDEX_ASSERT('AD_FACE', 'GEOMETRY');
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE_KEEP_DATA('IX_HAMLET');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE_KEEP_DATA('IX_HAMLET_FLAG');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE_KEEP_DATA('IX_HAMLET_NAME');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_HAMLET');
    INSERT INTO IX_HAMLET
      (PID,
       KIND_CODE,
       GEOMETRY,
       X_GUIDE,
       Y_GUIDE,
       LINK_PID,
       SIDE,
       NAME_GROUPID,
       ROAD_FLAG,
       PMESH_ID,
       MESH_ID_5K,
       MESH_ID,
       REGION_ID,
       POI_PID,
       POI_NUM,
       EDIT_FLAG,
       U_RECORD,
       U_FIELDS)
      SELECT T.PID,
             T.KIND_CODE,
             SDO_UTIL.RECTIFY_GEOMETRY(NAVI_GEOM.ROUNDGEOMETRY(T.GEOMETRY),
                                       NAVI_GEOM.TOLERANCE) GEO,
             0,
             0,
             0,
             0,
             0,
             0,
             0,
             NAVI_GEOM.GET5KMAPNUMBER1(T.GEOMETRY,
                                       NAVI_GEOM.GET25KMAPNUMBER1(T.GEOMETRY)),
             NAVI_GEOM.GET25KMAPNUMBER1(T.GEOMETRY),
             T.REGION_ID,
             0,
             NULL,
             1,
             0,
             NULL
        FROM (SELECT T.PID,
                     T.KIND_CODE,
                     T.GEOMETRY,
                     AF.REGION_ID,
                     ROW_NUMBER() OVER(PARTITION BY T.PID ORDER BY AF.REGION_ID) RN
                FROM SHD_HAMLET_TAB T, AD_FACE AF
               WHERE SDO_RELATE(AF.GEOMETRY,
                                T.GEOMETRY,
                                'mask = ANYINTERACT') = 'TRUE') T
       WHERE T.RN = 1;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_HAMLET_FLAG');
    INSERT INTO IX_HAMLET_FLAG
      (PID, FLAG_CODE, U_RECORD, U_FIELDS)
      SELECT T.PID, T.FLAG_CODE, 0, NULL FROM SHD_HAMLET_TAB T;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_HAMLET_NAME-����');
    SELECT COUNT(1) INTO V_COUNT FROM SHD_HAMLET_TAB T;
    V_PID := COMMON_UTIL.APPLY_PID('DMS',
                                   'IX_HAMLET_NAME',
                                   V_COUNT,
                                   P_TASK_NAME);
    INSERT INTO IX_HAMLET_NAME
      (NAME_ID,
       NAME_GROUPID,
       PID,
       LANG_CODE,
       NAME_CLASS,
       NAME,
       PHONETIC,
       NIDB_PID,
       U_RECORD,
       U_FIELDS)
      SELECT ROWNUM + V_PID - 1,
             1,
             T.PID,
             DECODE(M.PARAMETER, 'ML', 'CHI', 'CHT'),
             1,
             T.NAME,
             T.PY,
             NULL,
             0,
             NULL
        FROM SHD_HAMLET_TAB T, M_PARAMETER M
       WHERE M.NAME = 'REGION_INFO';
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_HAMLET_NAME-Ӣ��ȫ��');
    SELECT COUNT(1)
      INTO V_COUNT
      FROM SHD_HAMLET_TAB T
     WHERE T.ENGNAME_QC IS NOT NULL;
    V_PID := COMMON_UTIL.APPLY_PID('DMS',
                                   'IX_HAMLET_NAME',
                                   V_COUNT,
                                   P_TASK_NAME);
    INSERT INTO IX_HAMLET_NAME
      (NAME_ID,
       NAME_GROUPID,
       PID,
       LANG_CODE,
       NAME_CLASS,
       NAME,
       PHONETIC,
       NIDB_PID,
       U_RECORD,
       U_FIELDS)
      SELECT ROWNUM + V_PID - 1,
             1,
             T.PID,
             'ENG',
             2,
             T.ENGNAME_QC,
             NULL,
             NULL,
             0,
             NULL
        FROM SHD_HAMLET_TAB T
       WHERE T.ENGNAME_QC IS NOT NULL;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����IX_HAMLET_NAME-Ӣ�ļ��');
    SELECT COUNT(1)
      INTO V_COUNT
      FROM SHD_HAMLET_TAB T
     WHERE T.ENGNAME_QC IS NOT NULL
       AND T.ENGNAME_JC IS NOT NULL;
    V_PID := COMMON_UTIL.APPLY_PID('DMS',
                                   'IX_HAMLET_NAME',
                                   V_COUNT,
                                   P_TASK_NAME);
    INSERT INTO IX_HAMLET_NAME
      (NAME_ID,
       NAME_GROUPID,
       PID,
       LANG_CODE,
       NAME_CLASS,
       NAME,
       PHONETIC,
       NIDB_PID,
       U_RECORD,
       U_FIELDS)
      SELECT ROWNUM + V_PID - 1,
             1,
             T.PID,
             'ENG',
             1,
             T.ENGNAME_JC,
             NULL,
             NULL,
             0,
             NULL
        FROM SHD_HAMLET_TAB T
       WHERE T.ENGNAME_QC IS NOT NULL
         AND T.ENGNAME_JC IS NOT NULL;
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('IX_HAMLET', FALSE);
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('IX_HAMLET_FLAG', FALSE);
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('IX_HAMLET_NAME', FALSE);
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '�������');
  END;

  PROCEDURE RUN_POICROSSLINK(P_TASK_NAME VARCHAR2) IS
  BEGIN
    NAVI_TABLE_SYNC.SYNC_TABLES2('RUN_POICROSSLINK', 'SHD_RD_FILTER_LINK');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ɸѡ��·');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_RD_FILTER_LINK');
    INSERT /*+APPEND*/
    INTO SHD_RD_FILTER_LINK
      (LINK_PID, GEOMETRY)
      SELECT LINK_PID, GEOMETRY
        FROM RD_LINK L
       WHERE L.LINK_PID IN
             (SELECT LINK_PID
                FROM RD_LINK_FORM
               WHERE FORM_OF_WAY = 34
              
              MINUS
              
              SELECT LINK_PID
                FROM RD_LINK_FORM
               WHERE FORM_OF_WAY = 22
              
              MINUS
              
              SELECT LINK_PID
                FROM RD_LINK_LIMIT L
               WHERE BITAND(L.VEHICLE, 2147483648 + 9) IN (2147483648, 9));
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_RD_FILTER_LINK', FALSE);
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_RD_FILTER_LINK', 'GEOMETRY', 'LINE');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ɸѡ��Ҫ�ƶ���poi');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_POI_RD_LINK');
    INSERT /*+APPEND*/
    INTO SHD_POI_RD_LINK
      (POI_PID, LINK_PID, OLD_LINK, X_GUIDE, Y_GUIDE, GEOMETRY, FLAG)
      SELECT /*+parallel(MR)*/
       MR.PID,
       L.LINK_PID,
       MR.LINK_PID GUIDE_LINK_PID,
       MR.X_GUIDE,
       MR.Y_GUIDE,
       MR.GEOMETRY,
       'POI'
        FROM IX_POI MR, SHD_RD_FILTER_LINK L
       WHERE SDO_ANYINTERACT(L.GEOMETRY,
                             NAVI_GEOM.CREATELINK(MR.X_GUIDE,
                                                  MR.Y_GUIDE,
                                                  NAVI_GEOM.XLONG(MR.GEOMETRY),
                                                  NAVI_GEOM.YLAT(MR.GEOMETRY))) =
             'TRUE'
         AND MR.LINK_PID <> L.LINK_PID
         AND MR.LINK_PID IN (SELECT LINK_PID FROM RD_LINK)
      
      UNION ALL
      
      SELECT /*+parallel(M)*/
       R.PID,
       L.LINK_PID,
       R.GUIDE_LINK_PID,
       R.X_GUIDE,
       R.Y_GUIDE,
       R.GEOMETRY,
       '������'
        FROM IX_POINTADDRESS R, SHD_RD_FILTER_LINK L
       WHERE SDO_ANYINTERACT(L.GEOMETRY,
                             NAVI_GEOM.CREATELINK(R.X_GUIDE,
                                                  R.Y_GUIDE,
                                                  NAVI_GEOM.XLONG(R.GEOMETRY),
                                                  NAVI_GEOM.YLAT(R.GEOMETRY))) =
             'TRUE'
         AND R.GUIDE_LINK_PID <> L.LINK_PID
         AND R.GUIDE_LINK_PID IN (SELECT LINK_PID FROM RD_LINK);
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ɾ���ҽӸ�·');
    DELETE FROM SHD_POI_RD_LINK T
     WHERE EXISTS (SELECT 1
              FROM RD_LINK L, RD_LINK P
             WHERE L.LINK_PID = T.LINK_PID
               AND P.LINK_PID = T.OLD_LINK
               AND (L.S_NODE_PID IN (P.S_NODE_PID, P.E_NODE_PID) OR
                   L.E_NODE_PID IN (P.S_NODE_PID, P.E_NODE_PID))
               AND SDO_GEOM.RELATE(L.GEOMETRY,
                                   'DETERMINE',
                                   NAVI_GEOM.CREATELINK(T.X_GUIDE,
                                                        T.Y_GUIDE,
                                                        NAVI_GEOM.XLONG(T.GEOMETRY),
                                                        NAVI_GEOM.YLAT(T.GEOMETRY)),
                                   0.005) = 'TOUCH');
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ɾ��poi�����ศ·');
    --ɾ����POI���벻������ĵ�·
    DELETE FROM SHD_POI_RD_LINK P
     WHERE EXISTS
     (SELECT 1
            --��POI�͵�·�ľ�������
              FROM (SELECT POI_PID,
                           LINK_PID,
                           ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY DIS, LINK_PID) RN
                    --����POI�͵�·�ľ���
                      FROM (SELECT P.POI_PID,
                                   P.LINK_PID,
                                   NAVI_GEOM.PT_LINE_DISTANCE(P.GEOMETRY,
                                                              L.GEOMETRY) DIS
                              FROM SHD_POI_RD_LINK P, SHD_RD_FILTER_LINK L
                             WHERE P.LINK_PID = L.LINK_PID)) M
             WHERE P.POI_PID = M.POI_PID
               AND P.LINK_PID = M.LINK_PID
               AND M.RN > 1);
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '�ƶ���poi');
    MERGE INTO IX_POI P
    USING (SELECT PID, LINK_PID, X, Y, SIDE
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+PARALLEL(P)*/
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY LGEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK    P,
                                                                   SHD_RD_FILTER_LINK L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID
                                                               AND P.FLAG =
                                                                   'POI')))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.LINK_PID = T.LINK_PID,
             P.X_GUIDE  = T.X,
             P.Y_GUIDE  = T.Y,
             P.SIDE     = T.SIDE;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '�ƶ��ĵ�����');
    MERGE INTO IX_POINTADDRESS P
    USING (SELECT PID, LINK_PID, X, Y, SIDE
             FROM TABLE(POINT_FEATURE_BATCH.POIRELATEDLINK(CURSOR
                                                           (SELECT /*+PARALLEL(P)*/
                                                             P.POI_PID,
                                                             P.LINK_PID,
                                                             NAVI_GEOM.CREATEPOINT(P.X_GUIDE,
                                                                                   P.Y_GUIDE) PGEOMETRY,
                                                             L.GEOMETRY LGEOMETRY,
                                                             P.GEOMETRY DGEOMETRY
                                                              FROM SHD_POI_RD_LINK    P,
                                                                   SHD_RD_FILTER_LINK L
                                                             WHERE P.LINK_PID =
                                                                   L.LINK_PID
                                                               AND P.FLAG =
                                                                   '������')))) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE
         SET P.GUIDE_LINK_PID  = T.LINK_PID,
             P.X_GUIDE         = T.X,
             P.Y_GUIDE         = T.Y,
             P.GUIDE_LINK_SIDE = T.SIDE;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��log');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('GDU_SUCCESS_LIST');
    INSERT INTO GDU_SUCCESS_LIST
      (PID, FLAG)
      SELECT /*+PARALLEL(P)*/
       POI_PID, FLAG
        FROM SHD_POI_RD_LINK;
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('GDU_ERROR_LIST');
    INSERT INTO GDU_ERROR_LIST
      (PID, MEMO, FLAG)
      SELECT /*+PARALLEL(P)*/
       PID, DECODE(LINK_PID, 0, '����linkΪ0', '����link������'), 'POI'
        FROM IX_POI P
       WHERE LINK_PID IN (SELECT LINK_PID
                            FROM IX_POI
                          MINUS
                          SELECT LINK_PID FROM RD_LINK)
      
      UNION ALL
      
      SELECT /*+PARALLEL(P)*/
       PID,
       DECODE(GUIDE_LINK_PID, 0, '����linkΪ0', '����link������'),
       '������'
        FROM IX_POINTADDRESS P
       WHERE GUIDE_LINK_PID IN
             (SELECT GUIDE_LINK_PID
                FROM IX_POINTADDRESS
              MINUS
              SELECT LINK_PID FROM RD_LINK);
    COMMIT;
  
  END;

  FUNCTION CALC_MOVE_GEOMETRY(ORI_PGEOM IN MDSYS.SDO_GEOMETRY,
                              LGEOM     IN MDSYS.SDO_GEOMETRY,
                              SIDE      IN NUMBER) RETURN SDO_GEOMETRY IS
  
    PPT       MDSYS.SDO_GEOMETRY;
    LPT       MDSYS.SDO_GEOMETRY;
    DIST      NUMBER;
    ORI_X     NUMBER(12, 7);
    ORI_Y     NUMBER(12, 7);
    ARELATE_X NUMBER(12, 7);
    ARELATE_Y NUMBER(12, 7);
    MOVED_X   NUMBER(12, 7);
    MOVED_Y   NUMBER(12, 7);
    --SIDE              NUMBER(1);
  
  BEGIN
    --��POI���������LINK�ϵ������
    SDO_GEOM.SDO_CLOSEST_POINTS(ORI_PGEOM,
                                LGEOM,
                                NAVI_GEOM.TOLERANCE,
                                'UNIT=METER',
                                DIST,
                                PPT,
                                LPT);
  
    --ԭʼ������
    ORI_X := NAVI_GEOM.XLONG(ORI_PGEOM);
    ORI_Y := NAVI_GEOM.YLAT(ORI_PGEOM);
  
    --����LINK�ϵ����������
    ARELATE_X := NAVI_GEOM.XLONG(LPT);
    ARELATE_Y := NAVI_GEOM.YLAT(LPT);
  
    --MOVE��ĵ����꣺AX + ��OX - AX�� * 5.1 / DIST
    MOVED_X := ARELATE_X + (ORI_X - ARELATE_X) * MOVE_DIS / DIST;
    MOVED_Y := ARELATE_Y + (ORI_Y - ARELATE_Y) * MOVE_DIS / DIST;
    /*
    --�ȸ�ֵΪ���
    SIDE :=  2;
    
    --�����߷��룬���¼������Ҳ�
    IF LINK_DIR > 0 THEN
       --ʹ����ʾ�������ж�����λ�ù�ϵ
       SIDE := COMMON_UTIL.GET_PT_LINK_SIDE(LGEOM, ORI_PGEOM);
    
       --���������3�������ȡ��
       IF LINK_DIR = 3 THEN
         SIDE := 3 - SIDE;
       END IF;
    
    END IF;*/
  
    --�ڲ��������Ų
    IF SIDE = 1 THEN
      --MOVE��ĵ����꣺OX + ��AX - OX�� * (DIST + 5.1) / DIST
      MOVED_X := ORI_X + (ARELATE_X - ORI_X) * (DIST + MOVE_DIS) / DIST;
      MOVED_Y := ORI_Y + (ARELATE_Y - ORI_Y) * (DIST + MOVE_DIS) / DIST;
    
      --����������Ų
    ELSE
      --MOVE��ĵ����꣺AX + ��OX - AX�� * 5.1 / DIST
      MOVED_X := ARELATE_X + (ORI_X - ARELATE_X) * MOVE_DIS / DIST;
      MOVED_Y := ARELATE_Y + (ORI_Y - ARELATE_Y) * MOVE_DIS / DIST;
    
    END IF;
  
    --����Ų�������꣬��Ų����������
    IF MOVED_X > ORI_X THEN
      MOVED_X := MOVED_X + 0.000005;
    ELSE
      MOVED_X := MOVED_X - 0.000005;
    END IF;
  
    IF MOVED_Y > ORI_Y THEN
      MOVED_Y := MOVED_Y + 0.000005;
    ELSE
      MOVED_Y := MOVED_Y - 0.000005;
    END IF;
  
    RETURN NAVI_GEOM.CREATEPOINT(MOVED_X, MOVED_Y);
  END;

  PROCEDURE RUN_POIOUTBUFFER(P_TASK_NAME VARCHAR2) IS
  BEGIN
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ȷ��IX_POI��RD_LINK�Ŀռ�����');
    PIPELINE_SDO.SDO_INDEX_ASSERT('IX_POI', 'GEOMETRY');
    PIPELINE_SDO.SDO_INDEX_ASSERT('RD_LINK', 'GEOMETRY');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ȡ��LINK����Ϊ1.5��5�׵�POI');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_FOCUS_POI_LINK');
    INSERT INTO SHD_FOCUS_POI_LINK
      (PID,
       LINK_PID,
       KIND_CODE,
       SIDE,
       P_GEOM,
       L_GEOM,
       LINK_DIR,
       IS_RELLINK)
      SELECT /*+PARALLEL(P)*/
       P.PID,
       P.LINK_PID,
       P.KIND_CODE,
       P.SIDE,
       P.GEOMETRY AS P_GEOM,
       L.GEOMETRY AS L_GEOM,
       DECODE(L.MULTI_DIGITIZED, 1, L.DIRECT, 0) AS LINK_DIR,
       1 AS IS_RELLINK
        FROM IX_POI P, RD_LINK L
       WHERE P.LINK_PID = L.LINK_PID
         AND NOT SDO_WITHIN_DISTANCE(L.GEOMETRY, P.GEOMETRY, 'distance=1.5') =
              'TRUE'
         AND SDO_WITHIN_DISTANCE(L.GEOMETRY, P.GEOMETRY, 'distance=5') =
             'TRUE';
    COMMIT;
  
    INSERT INTO SHD_FOCUS_POI_LINK
      (PID,
       LINK_PID,
       KIND_CODE,
       SIDE,
       P_GEOM,
       L_GEOM,
       LINK_DIR,
       IS_RELLINK) WITH FOCUS_POI_LINK AS
      (SELECT TMP.BASE_OTHER_INFO AS PID, TMP.REF_OTHER_INFO AS LINK_PID
         FROM TABLE(PIPELINE_SDO.TAB_SPATIAL(CURSOR (SELECT /*+parallel(IP)*/
                                               ROWIDTOCHAR(IP.ROWID),
                                               IP.GEOMETRY,
                                               IP.PID
                                                FROM IX_POI IP),
                                             'SDO_WITHIN_DISTANCE(GEOMETRY, :G, ''distance=5 unit=m'') = ''TRUE''',
                                             'RD_LINK',
                                             'LINK_PID',
                                             '')) TMP
       MINUS
       
       SELECT TMP.BASE_OTHER_INFO AS PID, TMP.REF_OTHER_INFO AS LINK_PID
         FROM TABLE(PIPELINE_SDO.TAB_SPATIAL(CURSOR (SELECT /*+parallel(IP)*/
                                               ROWIDTOCHAR(IP.ROWID),
                                               IP.GEOMETRY,
                                               IP.PID
                                                FROM IX_POI IP),
                                             'SDO_WITHIN_DISTANCE(GEOMETRY, :G, ''distance=1.5 unit=m'') = ''TRUE''',
                                             'RD_LINK',
                                             'LINK_PID',
                                             '')) TMP)
      SELECT /*+ordered*/
       P.PID,
       L.LINK_PID,
       P.KIND_CODE,
       COMMON_UTIL.GET_PT_LINK_SIDE(L.GEOMETRY, P.GEOMETRY) AS SIDE,
       P.GEOMETRY AS P_GEOM,
       L.GEOMETRY AS L_GEOM,
       DECODE(L.MULTI_DIGITIZED, 1, L.DIRECT, 0) AS LINK_DIR,
       0 AS IS_RELLINK
        FROM IX_POI P, FOCUS_POI_LINK FPL, RD_LINK L
       WHERE P.PID = FPL.PID
         AND FPL.LINK_PID = L.LINK_PID
         AND P.LINK_PID <> L.LINK_PID;
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_FOCUS_POI_LINK', FALSE);
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '����λ��ʾ����ͬʱλ��������·5��buffer�ڵ�POI��������б�');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('GDU_ERROR_LIST');
    INSERT INTO GDU_ERROR_LIST
      (PID, MEMO)
    --ɸ��POI�Ĺ���LINK�Ĺҽ�LINK�����������������LINK���Ͳ�Ų
    WITH FILTER_BUFFER_LINK AS
      (SELECT SFP.PID, SFP.LINK_PID
         FROM SHD_FOCUS_POI_LINK SFP
       
       MINUS
       
       SELECT SFP1.PID, RL2.LINK_PID
         FROM SHD_FOCUS_POI_LINK SFP1,
              SHD_FOCUS_POI_LINK SFP2,
              RD_LINK            RL1,
              RD_LINK            RL2
        WHERE SFP1.LINK_PID = RL1.LINK_PID
          AND SFP2.LINK_PID = RL2.LINK_PID
          AND SFP1.IS_RELLINK = 1
          AND SFP2.IS_RELLINK = 0
          AND SFP1.PID = SFP2.PID
          AND RL1.S_NODE_PID IN (RL2.S_NODE_PID, RL2.E_NODE_PID)
       
       MINUS
       
       SELECT SFP1.PID, RL2.LINK_PID
         FROM SHD_FOCUS_POI_LINK SFP1,
              SHD_FOCUS_POI_LINK SFP2,
              RD_LINK            RL1,
              RD_LINK            RL2
        WHERE SFP1.LINK_PID = RL1.LINK_PID
          AND SFP2.LINK_PID = RL2.LINK_PID
          AND SFP1.IS_RELLINK = 1
          AND SFP2.IS_RELLINK = 0
          AND SFP1.PID = SFP2.PID
          AND RL1.E_NODE_PID IN (RL2.S_NODE_PID, RL2.E_NODE_PID))
    
      SELECT T.PID, '��λ��ʾ����ͬʱλ��������·5��buffer��' AS MEMO
        FROM (SELECT FBL.PID
                FROM FILTER_BUFFER_LINK FBL
               GROUP BY FBL.PID
              HAVING COUNT(1) >= 2) T;
    COMMIT;
  
    --ɾ����ŲPOI��������������LINK���������������LINK����ҽ�LINK�ģ���Buffer��
    DELETE FROM SHD_FOCUS_POI_LINK SFP
     WHERE SFP.PID IN (SELECT GEL.PID FROM GDU_ERROR_LIST GEL);
    COMMIT;
  
    --ɾ���������LINK��Buffer����������ҽ�Link��Buffer�еĹҽ�LINK��ֻ������LINK��ΨһBuffer��LINK
    DELETE FROM SHD_FOCUS_POI_LINK SFP
     WHERE SFP.IS_RELLINK = 0
       AND SFP.PID IN (SELECT SFP1.PID
                         FROM SHD_FOCUS_POI_LINK SFP1
                        WHERE SFP1.IS_RELLINK = 1);
    COMMIT;
  
    INSERT INTO GDU_ERROR_LIST
      (PID, MEMO)
      SELECT SFP.PID,
             '��λ��ʾ�������������߷����·�ڲ�buffer�����ֱ�Ϊ' || SFP.KIND_CODE AS MEMO
        FROM SHD_FOCUS_POI_LINK SFP
       WHERE DECODE(SFP.LINK_DIR, 3, 3 - SFP.SIDE, 2, SFP.SIDE, 2) = 1
         AND SFP.KIND_CODE IN ('230101',
                               '230102',
                               '230201',
                               '230202',
                               '230208',
                               '230210',
                               '230112',
                               '230115',
                               '230117',
                               '230118',
                               '230120',
                               '230121',
                               '230122',
                               '230123',
                               '230124');
    COMMIT;
  
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MOVE_POI');
    INSERT INTO SHD_MOVE_POI
      (PID, LINK_PID, P_GEOM, L_GEOM, SIDE)
      SELECT SFP.PID,
             SFP.LINK_PID,
             SFP.P_GEOM,
             SFP.L_GEOM,
             DECODE(SFP.LINK_DIR, 3, 3 - SFP.SIDE, 2, SFP.SIDE, 0, 2) AS SIDE
        FROM SHD_FOCUS_POI_LINK SFP
       WHERE SFP.PID NOT IN (SELECT GEL.PID FROM GDU_ERROR_LIST GEL);
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MOVE_POI', FALSE);
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '����POIŲ����·5����������');
    MERGE INTO SHD_MOVE_POI P
    USING (SELECT SMP.PID,
                  CALC_MOVE_GEOMETRY(SMP.P_GEOM, SMP.L_GEOM, SMP.SIDE) AS MOVED_GEOMETRY
             FROM SHD_MOVE_POI SMP) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE SET P.MOVED_GEOM = T.MOVED_GEOMETRY;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      'Ϊ��ʱ����Ų������꽨�ռ�������������Buffer����');
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_MOVE_POI', 'MOVED_GEOM');
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '����λ��ʾ�����Ƴ�һ����·5��buffer������������·5��buffer�ڵ�POI��������б�');
    INSERT INTO GDU_ERROR_LIST
      (PID, MEMO)
      SELECT P.PID,
             '��λ��ʾ�����Ƴ�һ����·5��buffer������������·5��buffer' AS MEMO
        FROM SHD_MOVE_POI P, RD_LINK L
       WHERE SDO_WITHIN_DISTANCE(L.GEOMETRY, P.MOVED_GEOM, 'distance=5') =
             'TRUE';
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '���ɹ��Ƶ���·5�����POI����ɹ��б�');
    INSERT INTO GDU_SUCCESS_LIST
      (PID)
      SELECT SMP.PID
        FROM SHD_MOVE_POI SMP
      MINUS
      SELECT GEL.PID FROM GDU_ERROR_LIST GEL;
    COMMIT;
  
    NAVI_LOG.LOG_INFO(P_TASK_NAME, '�޸�IX_POI����POI����ʾ����');
    MERGE INTO IX_POI P
    USING (SELECT SMP.PID, SMP.MOVED_GEOM
             FROM SHD_MOVE_POI SMP
            WHERE SMP.PID IN (SELECT GSL.PID FROM GDU_SUCCESS_LIST GSL)) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE SET P.GEOMETRY = T.MOVED_GEOM;
    COMMIT;
  
    --�޸�SHD_MOVE_POI��Ų����SIDE���ڲ��Ϊ���,ֻ�޸����ڹ���LINK��Buffer�ڵ����
    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '�޸�IX_POI���й�����·Ϊ�����߷����POI��Ų����SIDE���ڲ��Ϊ��࣬Ϊ3��ȥԭ��SIDE');
    MERGE INTO IX_POI P
    USING (SELECT SMP.PID
             FROM SHD_MOVE_POI SMP
            WHERE SMP.PID IN (SELECT GSL.PID FROM GDU_SUCCESS_LIST GSL)
              AND SMP.PID IN (SELECT SFP.PID
                                FROM SHD_FOCUS_POI_LINK SFP
                               WHERE SFP.IS_RELLINK = 1)
              AND SMP.SIDE = 1) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE SET P.SIDE = 3 - P.SIDE;
    COMMIT;
  
  END;
  
  PROCEDURE RUN_POINTOUTBUFFER(P_TASK_NAME VARCHAR2) IS
  BEGIN
    NAVI_LOG.LOG_INFO(P_TASK_NAME, 'ȷ��IX_POINTADDRESS��RD_LINK�Ŀռ�����');
    PIPELINE_SDO.SDO_INDEX_ASSERT('IX_POINTADDRESS', 'GEOMETRY');
    PIPELINE_SDO.SDO_INDEX_ASSERT('RD_LINK', 'GEOMETRY');

    NAVI_LOG.LOG_INFO(P_TASK_NAME, '��ȡ��LINK����Ϊ1.5��5�׵�IX_POINTADDRESS');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_FOCUS_POI_LINK');
    INSERT INTO SHD_FOCUS_POI_LINK
      (PID,
       LINK_PID,
       SIDE,
       P_GEOM,
       L_GEOM,
       LINK_DIR,
       IS_RELLINK)
      SELECT /*+PARALLEL(P)*/
       P.PID,
       P.GUIDE_LINK_PID,
       P.GUIDE_LINK_SIDE,
       P.GEOMETRY AS P_GEOM,
       L.GEOMETRY AS L_GEOM,
       0 AS LINK_DIR,
       1 AS IS_RELLINK
        FROM IX_POINTADDRESS P, RD_LINK L
       WHERE P.GUIDE_LINK_PID = L.LINK_PID
         AND NOT SDO_WITHIN_DISTANCE(L.GEOMETRY, P.GEOMETRY, 'distance=1.5') =
              'TRUE'
         AND SDO_WITHIN_DISTANCE(L.GEOMETRY, P.GEOMETRY, 'distance=5') =
             'TRUE';
    COMMIT;

    INSERT INTO SHD_FOCUS_POI_LINK
      (PID,
       LINK_PID,
       SIDE,
       P_GEOM,
       L_GEOM,
       LINK_DIR,
       IS_RELLINK) 
       WITH FOCUS_POINT_LINK AS
      (SELECT TMP.BASE_OTHER_INFO AS PID, TMP.REF_OTHER_INFO AS LINK_PID
         FROM TABLE(PIPELINE_SDO.TAB_SPATIAL(CURSOR (SELECT /*+parallel(IP)*/
                                               ROWIDTOCHAR(IP.ROWID),
                                               IP.GEOMETRY,
                                               IP.PID
                                                FROM IX_POINTADDRESS IP),
                                             'SDO_WITHIN_DISTANCE(GEOMETRY, :G, ''distance=5 unit=m'') = ''TRUE''',
                                             'RD_LINK',
                                             'LINK_PID',
                                             '')) TMP
       MINUS

       SELECT TMP.BASE_OTHER_INFO AS PID, TMP.REF_OTHER_INFO AS LINK_PID
         FROM TABLE(PIPELINE_SDO.TAB_SPATIAL(CURSOR (SELECT /*+parallel(IP)*/
                                               ROWIDTOCHAR(IP.ROWID),
                                               IP.GEOMETRY,
                                               IP.PID
                                                FROM IX_POINTADDRESS IP),
                                             'SDO_WITHIN_DISTANCE(GEOMETRY, :G, ''distance=1.5 unit=m'') = ''TRUE''',
                                             'RD_LINK',
                                             'LINK_PID',
                                             '')) TMP)
      SELECT /*+ordered*/
       P.PID,
       L.LINK_PID,
       COMMON_UTIL.GET_PT_LINK_SIDE(L.GEOMETRY, P.GEOMETRY) AS SIDE,
       P.GEOMETRY AS P_GEOM,
       L.GEOMETRY AS L_GEOM,
       0 AS LINK_DIR,
       0 AS IS_RELLINK
        FROM IX_POINTADDRESS P, FOCUS_POINT_LINK FPL, RD_LINK L
       WHERE P.PID = FPL.PID
         AND FPL.LINK_PID = L.LINK_PID
         AND P.GUIDE_LINK_PID <> L.LINK_PID;
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_FOCUS_POI_LINK', FALSE);

    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '����λ��ʾ����ͬʱλ��������·5��buffer�ڵĵ����Ʋ�������б�');
    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('GDU_ERROR_LIST');
    INSERT INTO GDU_ERROR_LIST
      (PID, MEMO)
    --ɸ��POI�Ĺ���LINK�Ĺҽ�LINK�����������������LINK���Ͳ�Ų
    WITH FILTER_BUFFER_LINK AS
      (SELECT SFP.PID, SFP.LINK_PID
         FROM SHD_FOCUS_POI_LINK SFP

       MINUS

       SELECT SFP1.PID, RL2.LINK_PID
         FROM SHD_FOCUS_POI_LINK SFP1,
              SHD_FOCUS_POI_LINK SFP2,
              RD_LINK            RL1,
              RD_LINK            RL2
        WHERE SFP1.LINK_PID = RL1.LINK_PID
          AND SFP2.LINK_PID = RL2.LINK_PID
          AND SFP1.IS_RELLINK = 1
          AND SFP2.IS_RELLINK = 0
          AND SFP1.PID = SFP2.PID
          AND RL1.S_NODE_PID IN (RL2.S_NODE_PID, RL2.E_NODE_PID)

       MINUS

       SELECT SFP1.PID, RL2.LINK_PID
         FROM SHD_FOCUS_POI_LINK SFP1,
              SHD_FOCUS_POI_LINK SFP2,
              RD_LINK            RL1,
              RD_LINK            RL2
        WHERE SFP1.LINK_PID = RL1.LINK_PID
          AND SFP2.LINK_PID = RL2.LINK_PID
          AND SFP1.IS_RELLINK = 1
          AND SFP2.IS_RELLINK = 0
          AND SFP1.PID = SFP2.PID
          AND RL1.E_NODE_PID IN (RL2.S_NODE_PID, RL2.E_NODE_PID))

      SELECT T.PID, '��λ��ʾ����ͬʱλ��������·5��buffer��' AS MEMO
        FROM (SELECT FBL.PID
                FROM FILTER_BUFFER_LINK FBL
               GROUP BY FBL.PID
              HAVING COUNT(1) >= 2) T;
    COMMIT;

    --ɾ����ŲPOI��������������LINK���������������LINK����ҽ�LINK�ģ���Buffer��
    DELETE FROM SHD_FOCUS_POI_LINK SFP
     WHERE SFP.PID IN (SELECT GEL.PID FROM GDU_ERROR_LIST GEL);
    COMMIT;

    --ɾ���������LINK��Buffer����������ҽ�Link��Buffer�еĹҽ�LINK��ֻ������LINK��ΨһBuffer��LINK
    DELETE FROM SHD_FOCUS_POI_LINK SFP
     WHERE SFP.IS_RELLINK = 0
       AND SFP.PID IN (SELECT SFP1.PID
                         FROM SHD_FOCUS_POI_LINK SFP1
                        WHERE SFP1.IS_RELLINK = 1);
    COMMIT;

    PIPELINE_SCHEMA_UTILS.PREP_TARGET_TABLE('SHD_MOVE_POI');
    INSERT INTO SHD_MOVE_POI
      (PID, LINK_PID, P_GEOM, L_GEOM, SIDE)
      SELECT SFP.PID,
             SFP.LINK_PID,
             SFP.P_GEOM,
             SFP.L_GEOM,
             2 AS SIDE
        FROM SHD_FOCUS_POI_LINK SFP
       WHERE SFP.PID NOT IN (SELECT GEL.PID FROM GDU_ERROR_LIST GEL);
    COMMIT;
    PIPELINE_SCHEMA_UTILS.FINALIZE_TABLE('SHD_MOVE_POI', FALSE);

    NAVI_LOG.LOG_INFO(P_TASK_NAME, '���������Ų����·5����������');
    MERGE INTO SHD_MOVE_POI P
    USING (SELECT SMP.PID,
                  CALC_MOVE_GEOMETRY(SMP.P_GEOM, SMP.L_GEOM, SMP.SIDE) AS MOVED_GEOMETRY
             FROM SHD_MOVE_POI SMP) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE SET P.MOVED_GEOM = T.MOVED_GEOMETRY;
    COMMIT;

    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      'Ϊ��ʱ����Ų������꽨�ռ�������������Buffer����');
    PIPELINE_SDO.SDO_INDEX_ASSERT('SHD_MOVE_POI', 'MOVED_GEOM');

    NAVI_LOG.LOG_INFO(P_TASK_NAME,
                      '����λ��ʾ�����Ƴ�һ����·5��buffer������������·5��buffer�ڵĵ����Ʋ�������б�');
    INSERT INTO GDU_ERROR_LIST
      (PID, MEMO)
      SELECT P.PID,
             '��λ��ʾ�����Ƴ�һ����·5��buffer������������·5��buffer' AS MEMO
        FROM SHD_MOVE_POI P, RD_LINK L
       WHERE SDO_WITHIN_DISTANCE(L.GEOMETRY, P.MOVED_GEOM, 'distance=5') =
             'TRUE';
    COMMIT;

    NAVI_LOG.LOG_INFO(P_TASK_NAME, '���ɹ��Ƶ���·5����ĵ����Ʋ���ɹ��б�');
    INSERT INTO GDU_SUCCESS_LIST
      (PID)
      SELECT SMP.PID
        FROM SHD_MOVE_POI SMP
      MINUS
      SELECT GEL.PID FROM GDU_ERROR_LIST GEL;
    COMMIT;

    NAVI_LOG.LOG_INFO(P_TASK_NAME, '�޸�IX_POINTADDRESS����POI����ʾ����');
    MERGE INTO IX_POINTADDRESS P
    USING (SELECT SMP.PID, SMP.MOVED_GEOM
             FROM SHD_MOVE_POI SMP
            WHERE SMP.PID IN (SELECT GSL.PID FROM GDU_SUCCESS_LIST GSL)) T
    ON (P.PID = T.PID)
    WHEN MATCHED THEN
      UPDATE SET P.GEOMETRY = T.MOVED_GEOM;
    COMMIT;

  END; 
  
END;
/
