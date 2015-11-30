CREATE OR REPLACE PACKAGE EXECUTE_AFTER_CREATE IS

  -- AUTHOR  : LIUQING
  -- CREATED : 2011/11/29 9:12:11
  -- PURPOSE : 子版本创建后执行的脚本

  --PURPOSE : 子版本创建后执行的同步脚本
  PROCEDURE SYNCHRONOUS_EXECUTE;

  --PURPOSE : 子版本创建后执行的异步脚本
  PROCEDURE ASYNCHRONOUS_EXECUTE;

  --计算哪些数据在导出范围外
  --1.按图幅导出，则计算link、face 、node的图幅号为非导出图幅
  --2.按范围导出，则计算link、face、node在范围内
  PROCEDURE SET_EDIT_FLAG_BY_MESH;
  PROCEDURE SET_EDIT_FLAG_BY_TASK_EXTENT;
  --将BI_task表中的多面，拆成多个2003面
  PROCEDURE SPLITE_BI_TASK_GEOMETRY;

  PROCEDURE DELETE_NOT_INTEGRATED_PT;

  PROCEDURE CREATE_32774_INDEX(P_TABLE VARCHAR2, P_CLOUMN VARCHAR2);

  --如果关系都是touch 和disjo，那么去除这种关系
  PROCEDURE REMOVE_TOUCH_GEOMETRY(P_TMP_TABLE VARCHAR2,
                                  PID_CLOUMN  VARCHAR2);

  PROCEDURE MERGE_EDIT_FLAT_TO_TABLE(P_TABLE     VARCHAR2,
                                     P_TMP_TABLE VARCHAR2,
                                     PID_CLOUMN  VARCHAR2);

END EXECUTE_AFTER_CREATE;
/
CREATE OR REPLACE PACKAGE BODY EXECUTE_AFTER_CREATE IS

  --删除数据导出后不完整的数据，如同一关系，立交关系，父子关系

  PROCEDURE DELETE_NOT_INTEGRATED_DATA IS
  BEGIN
  
    --delete RD_SAMENODE_PART
    DELETE FROM RD_SAMENODE_PART
     WHERE GROUP_ID IN
           (
            
            SELECT P.GROUP_ID
              FROM RD_SAMENODE_PART P
             WHERE P.TABLE_NAME = 'RD_NODE'
               AND P.NODE_PID NOT IN (SELECT NODE_PID FROM RD_NODE)
            UNION ALL
            SELECT P.GROUP_ID
              FROM RD_SAMENODE_PART P
             WHERE P.TABLE_NAME = 'RW_NODE'
               AND P.NODE_PID NOT IN (SELECT NODE_PID FROM RW_NODE)
            UNION ALL
            SELECT P.GROUP_ID
              FROM RD_SAMENODE_PART P
             WHERE P.TABLE_NAME = 'AD_NODE'
               AND P.NODE_PID NOT IN (SELECT NODE_PID FROM AD_NODE)
            UNION ALL
            SELECT P.GROUP_ID
              FROM RD_SAMENODE_PART P
             WHERE P.TABLE_NAME = 'ZONE_NODE'
               AND P.NODE_PID NOT IN (SELECT NODE_PID FROM ZONE_NODE)
            UNION ALL
            SELECT P.GROUP_ID
              FROM RD_SAMENODE_PART P
             WHERE P.TABLE_NAME = 'LU_NODE'
               AND P.NODE_PID NOT IN (SELECT NODE_PID FROM LU_NODE)
            
            );
    --delete RD_SAMENODE
    DELETE FROM RD_SAMENODE
     WHERE GROUP_ID NOT IN (SELECT GROUP_ID FROM RD_SAMENODE_PART);
    --delete RD_SAMELINK_PART
    DELETE FROM RD_SAMELINK_PART
     WHERE GROUP_ID IN
           (
            
            SELECT P.GROUP_ID
              FROM RD_SAMELINK_PART P
             WHERE P.TABLE_NAME = 'RD_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM RD_LINK)
            UNION ALL
            
            SELECT P.GROUP_ID
              FROM RD_SAMELINK_PART P
             WHERE P.TABLE_NAME = 'RW_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM RW_LINK)
            
            UNION ALL
            
            SELECT P.GROUP_ID
              FROM RD_SAMELINK_PART P
             WHERE P.TABLE_NAME = 'AD_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM AD_LINK)
            
            UNION ALL
            
            SELECT P.GROUP_ID
              FROM RD_SAMELINK_PART P
             WHERE P.TABLE_NAME = 'ZONE_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM ZONE_LINK)
            
            UNION ALL
            
            SELECT P.GROUP_ID
              FROM RD_SAMELINK_PART P
             WHERE P.TABLE_NAME = 'LU_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM LU_LINK));
    --delete RD_SAMELINK
    DELETE FROM RD_SAMELINK
     WHERE GROUP_ID NOT IN (SELECT GROUP_ID FROM RD_SAMELINK_PART);
    --delete RD_GSC_LINK
    DELETE FROM RD_GSC_LINK P
     WHERE P.PID IN
           (
            
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'RD_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM RD_LINK)
            UNION ALL
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'LC_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM LC_LINK)
            UNION ALL
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'LU_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM LU_LINK)
            UNION ALL
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'RW_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM RW_LINK)
            UNION ALL
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'AD_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM AD_LINK)
            UNION ALL
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'ZONE_LINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM ZONE_LINK)
            UNION ALL
            SELECT P.PID
              FROM RD_GSC_LINK P
             WHERE P.TABLE_NAME = 'CMG_BUILDLINK'
               AND P.LINK_PID NOT IN (SELECT LINK_PID FROM CMG_BUILDLINK));
    --delete RD_GSC
    DELETE FROM RD_GSC P WHERE P.PID NOT IN (SELECT PID FROM RD_GSC_LINK);
    --delete IX_POI_PARENT
    /*DELETE FROM IX_POI_PARENT P
    WHERE P.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POI_CHILDREN P);*/
  
    --delete IX_POI_CHILDREN
    DELETE FROM IX_POI_CHILDREN
     WHERE CHILD_POI_PID NOT IN (SELECT PID FROM IX_POI);
  
    DELETE FROM AU_IX_POI_CHILDREN
     WHERE AUDATA_ID NOT IN (SELECT AUDATA_ID FROM AU_IX_POI);
  
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('CAL_EXTERNAL_PART_FEATURE ERROR:' || SQLERRM);
    
      ROLLBACK;
      RAISE;
  END;

  --检查预处理
  PROCEDURE CHECK_PRE_INIT IS
  BEGIN
    DBMS_SCHEDULER.create_job(job_name   => 'CHECK_PRE_INIT_JOB',
                              job_type   => 'STORED_PROCEDURE',
                              job_action => 'GLM_VALIDATION.CHECK_INIT_STEP1',
                              enabled    => true);
  
  END;

  --计算哪些数据在导出范围外
  --1.按图幅导出，则计算link、face 、node的图幅号为非导出图幅
  --2.按范围导出，则计算link、face、node在范围内

  PROCEDURE SET_EDIT_FLAG_BY_MESH IS
    --按图幅导出时，核心图幅数量
    V_MESH_COUNT NUMBER(10);
  
  BEGIN
    --按图幅导出时，计算核心图幅数量
    SELECT COUNT(1) INTO V_MESH_COUNT FROM M_MESH_TYPE WHERE TYPE = 1;
    IF V_MESH_COUNT > 0 THEN
      --按图幅导出
      --设置导出核心图幅外的link为不可编辑
      UPDATE RD_LINK
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      --设置导出核心图幅外的NODE为不可编辑  ,图廓点可编辑   
      UPDATE RD_NODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM RD_NODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      --设置导出核心图幅外的link为不可编辑
      UPDATE RW_LINK
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      --设置导出核心图幅外的NODE为不可编辑  ,图廓点可编辑   
      UPDATE RW_NODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM RW_NODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      --设置导出核心图幅外的POI为不可编辑
      UPDATE IX_POI
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_POINTADDRESS
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_ANNOTATION
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_HAMLET
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_CROSSPOINT
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_TOLLGATE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_ROADNAME
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_IC
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE IX_POSTCODE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE AD_ADMIN
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE AD_FACE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE AD_LINK
         SET EDIT_FLAG = 0
       WHERE LINK_PID NOT IN
             (SELECT NM.LINK_PID
                FROM AD_LINK_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      UPDATE AD_NODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM AD_NODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
      --20150511补充（图幅外的面的所有要素不可编辑）
      --1.AD_FACE
      --图幅外的面的线不可编辑
      UPDATE AD_LINK K
         SET K.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM AD_FACE_TOPO T, AD_FACE F
               WHERE T.FACE_PID = F.FACE_PID
                 AND F.MESH_ID NOT IN
                     (SELECT MESH_ID FROM M_MESH_TYPE T WHERE T.TYPE = 1)
                 AND K.LINK_PID = T.LINK_PID);
    
      --图幅外的面的点不可编辑
      UPDATE AD_NODE N
         SET N.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT K.S_NODE_PID NODE_PID
                        FROM AD_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM AD_FACE_TOPO T, AD_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      UNION
                      SELECT K.E_NODE_PID NODE_PID
                        FROM AD_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM AD_FACE_TOPO T, AD_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      
                      ) X
               WHERE X.NODE_PID = N.NODE_PID);
    
      UPDATE ZONE_FACE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE ZONE_LINK
         SET EDIT_FLAG = 0
       WHERE LINK_PID NOT IN
             (SELECT NM.LINK_PID
                FROM ZONE_LINK_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      UPDATE ZONE_NODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM ZONE_NODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
      --2.ZONE_FACE
      --20150511补充（图幅外的面的所有要素不可编辑）
      --图幅外的面的线不可编辑
      UPDATE ZONE_LINK K
         SET K.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM ZONE_FACE_TOPO T, ZONE_FACE F
               WHERE T.FACE_PID = F.FACE_PID
                 AND F.MESH_ID NOT IN
                     (SELECT MESH_ID FROM M_MESH_TYPE T WHERE T.TYPE = 1)
                 AND K.LINK_PID = T.LINK_PID);
    
      --图幅外的面的点不可编辑
      UPDATE ZONE_NODE N
         SET N.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT K.S_NODE_PID NODE_PID
                        FROM ZONE_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM ZONE_FACE_TOPO T, ZONE_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      UNION
                      SELECT K.E_NODE_PID NODE_PID
                        FROM ZONE_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM ZONE_FACE_TOPO T, ZONE_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      
                      ) X
               WHERE X.NODE_PID = N.NODE_PID);
    
      UPDATE LC_FACE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE LC_LINK
         SET EDIT_FLAG = 0
       WHERE LINK_PID NOT IN
             (SELECT NM.LINK_PID
                FROM LC_LINK_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      UPDATE LC_NODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM LC_NODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      --3.LC_FACE
      --20150511补充（图幅外的面的所有要素不可编辑）
      --图幅外的面的线不可编辑
      UPDATE LC_LINK K
         SET K.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM LC_FACE_TOPO T, LC_FACE F
               WHERE T.FACE_PID = F.FACE_PID
                 AND F.MESH_ID NOT IN
                     (SELECT MESH_ID FROM M_MESH_TYPE T WHERE T.TYPE = 1)
                 AND K.LINK_PID = T.LINK_PID);
    
      --图幅外的面的点不可编辑
      UPDATE LC_NODE N
         SET N.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT K.S_NODE_PID NODE_PID
                        FROM LC_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM LC_FACE_TOPO T, LC_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      UNION
                      SELECT K.E_NODE_PID NODE_PID
                        FROM LC_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM LC_FACE_TOPO T, LC_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      
                      ) X
               WHERE X.NODE_PID = N.NODE_PID);
    
      UPDATE LU_FACE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE LU_LINK
         SET EDIT_FLAG = 0
       WHERE LINK_PID NOT IN
             (SELECT NM.LINK_PID
                FROM LU_LINK_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      UPDATE LU_NODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM LU_NODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      --4.LU_FACE
      --20150511补充（图幅外的面的所有要素不可编辑）
      --图幅外的面的线不可编辑
      UPDATE LU_LINK K
         SET K.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM LU_FACE_TOPO T, LU_FACE F
               WHERE T.FACE_PID = F.FACE_PID
                 AND F.MESH_ID NOT IN
                     (SELECT MESH_ID FROM M_MESH_TYPE T WHERE T.TYPE = 1)
                 AND K.LINK_PID = T.LINK_PID);
    
      --图幅外的面的点不可编辑
      UPDATE LU_NODE N
         SET N.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT K.S_NODE_PID NODE_PID
                        FROM LU_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM LU_FACE_TOPO T, LU_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      UNION
                      SELECT K.E_NODE_PID NODE_PID
                        FROM LU_LINK K
                       WHERE EXISTS (SELECT 1
                                FROM LU_FACE_TOPO T, LU_FACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      
                      ) X
               WHERE X.NODE_PID = N.NODE_PID);
    
      UPDATE CMG_BUILDFACE
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
    
      UPDATE CMG_BUILDLINK
         SET EDIT_FLAG = 0
       WHERE LINK_PID NOT IN
             (SELECT NM.LINK_PID
                FROM CMG_BUILDLINK_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      UPDATE CMG_BUILDNODE
         SET EDIT_FLAG = 0
       WHERE NODE_PID NOT IN
             (SELECT NM.NODE_PID
                FROM CMG_BUILDNODE_MESH NM, M_MESH_TYPE MT
               WHERE MT.TYPE = 1
                 AND NM.MESH_ID = MT.MESH_ID);
    
      --5.CMG_BUILDFACE
      --20150511补充（图幅外的面的所有要素不可编辑）
      --图幅外的面的线不可编辑
      UPDATE CMG_BUILDLINK K
         SET K.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM CMG_BUILDFACE_TOPO T, CMG_BUILDFACE F
               WHERE T.FACE_PID = F.FACE_PID
                 AND F.MESH_ID NOT IN
                     (SELECT MESH_ID FROM M_MESH_TYPE T WHERE T.TYPE = 1)
                 AND K.LINK_PID = T.LINK_PID);
    
      --图幅外的面的点不可编辑
      UPDATE CMG_BUILDNODE N
         SET N.EDIT_FLAG = 0
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT K.S_NODE_PID NODE_PID
                        FROM CMG_BUILDLINK K
                       WHERE EXISTS
                       (SELECT 1
                                FROM CMG_BUILDFACE_TOPO T, CMG_BUILDFACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      UNION
                      SELECT K.E_NODE_PID NODE_PID
                        FROM CMG_BUILDLINK K
                       WHERE EXISTS
                       (SELECT 1
                                FROM CMG_BUILDFACE_TOPO T, CMG_BUILDFACE F
                               WHERE T.FACE_PID = F.FACE_PID
                                 AND F.MESH_ID NOT IN
                                     (SELECT MESH_ID
                                        FROM M_MESH_TYPE T
                                       WHERE T.TYPE = 1)
                                 AND K.LINK_PID = T.LINK_PID)
                      
                      ) X
               WHERE X.NODE_PID = N.NODE_PID);
    
      /* UPDATE CM_BUILDFACE
        SET EDIT_FLAG = 0
      WHERE MESH_ID NOT IN
            (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);*/
    
      /*UPDATE CM_BUILDLINK
        SET EDIT_FLAG = 0
      WHERE LINK_PID NOT IN
            (SELECT NM.LINK_PID
               FROM CM_BUILDLINK_MESH NM, M_MESH_TYPE MT
              WHERE MT.TYPE = 1
                AND NM.MESH_ID = MT.MESH_ID);*/
    
      /*      UPDATE CM_BUILDNODE
               SET EDIT_FLAG = 0
             WHERE NODE_PID NOT IN
                   (SELECT NM.NODE_PID
                      FROM CM_BUILDNODE_MESH NM, M_MESH_TYPE MT
                     WHERE MT.TYPE = 1
                       AND NM.MESH_ID = MT.MESH_ID);
      */
      UPDATE PT_POI
         SET EDIT_FLAG = 0
       WHERE MESH_ID NOT IN
             (SELECT MESH_ID FROM M_MESH_TYPE WHERE TYPE = 1);
      --ADAS_LINK编辑标示edit_flag继承RD_LINK
      MERGE INTO ADAS_LINK R
      USING (SELECT L.LINK_PID, L.EDIT_FLAG FROM RD_LINK L) V
      ON (R.RDLINK_PID = V.LINK_PID)
      WHEN MATCHED THEN
        UPDATE SET R.EDIT_FLAG = V.EDIT_FLAG;
    END IF;
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('SET_EDIT_FLAG_BY_MESH ERROR:' || SQLERRM);
    
      ROLLBACK;
    
  END;

  PROCEDURE CREATE_32774_METADATA(P_TABLE   VARCHAR2,
                                  P_CLOUMN  VARCHAR2,
                                  P_DIAMEND NUMBER DEFAULT 2) IS
    DIMINFO MDSYS.SDO_DIM_ARRAY;
  BEGIN
    DIMINFO := MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG',
                                                         -180,
                                                         180,
                                                         0.000005),
                                   MDSYS.SDO_DIM_ELEMENT('YLAT',
                                                         -90,
                                                         90,
                                                         0.000005));
    IF (3 = P_DIAMEND) THEN
      DIMINFO := MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG',
                                                           -180,
                                                           180,
                                                           0.000005),
                                     MDSYS.SDO_DIM_ELEMENT('YLAT',
                                                           -90,
                                                           90,
                                                           0.000005),
                                     MDSYS.SDO_DIM_ELEMENT('Z',
                                                           -9999,
                                                           9999,
                                                           0.000005));
    END IF;
  
    DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = UPPER(P_TABLE);
  
    INSERT INTO USER_SDO_GEOM_METADATA
    VALUES
      (P_TABLE, P_CLOUMN, DIMINFO, 32774);
  
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      Navi_Log.LOG_ERROR('SET_EDIT_FLAG_BY_TASK_EXTENT',
                         'CREATE_TABLE_META ' || SQLERRM);
      RAISE;
    
  END;

  PROCEDURE CREATE_32774_INDEX(P_TABLE VARCHAR2, P_CLOUMN VARCHAR2) IS
    V_COUNT NUMBER;
    V_GTYPE NUMBER;
  BEGIN
    SELECT nvl(COUNT(1), 0)
      INTO V_COUNT
      FROM USER_INDEXES P
     WHERE P.TABLE_NAME = UPPER(P_TABLE)
       AND P.ITYP_NAME = 'SPATIAL_INDEX';
    IF (V_COUNT = 0) THEN
      EXECUTE IMMEDIATE 'SELECT NVL(MAX(T.' || P_CLOUMN ||
                        '.SDO_GTYPE),2) FROM  ' || P_TABLE ||
                        ' T WHERE ROWNUM=1'
        INTO V_GTYPE;
      IF V_GTYPE IS NOT NULL AND
         3 = TO_NUMBER(SUBSTR(TO_CHAR(V_GTYPE), 0, 1)) THEN
        CREATE_32774_METADATA(P_TABLE, P_CLOUMN, 3);
        EXECUTE IMMEDIATE 'CREATE INDEX ' || P_TABLE || '_G' || ' ON ' ||
                          P_TABLE || '(' || P_CLOUMN ||
                          ')  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS(''SDO_INDX_DIMS=3'')  PARALLEL';
      ELSE
        CREATE_32774_METADATA(P_TABLE, P_CLOUMN);
        EXECUTE IMMEDIATE 'CREATE INDEX ' || P_TABLE || '_G' || ' ON ' ||
                          P_TABLE || '(' || P_CLOUMN ||
                          ')  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARALLEL';
      END IF;
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
    
      Navi_Log.LOG_ERROR('SET_EDIT_FLAG_BY_TASK_EXTENT',
                         'CREATE_SPATIAL_INDEX ' || SQLERRM);
      RAISE;
  END;

  PROCEDURE UPDATE_EDIT_FLAG(P_TABLE VARCHAR2, PID_CLOUMN VARCHAR2) IS
    V_UPDATE_SQL VARCHAR2(4000);
    PRAGMA AUTONOMOUS_TRANSACTION;
  BEGIN
  
    V_UPDATE_SQL := 'UPDATE ' || P_TABLE || ' L
                 SET L.EDIT_FLAG = 0
               WHERE L.' || PID_CLOUMN ||
                    ' NOT IN
                     (SELECT ' || PID_CLOUMN || '
                        FROM TMP_' || P_TABLE ||
                    '_32774 L
                       WHERE SDO_RELATE(L.GEOMETRY,
                                        (SELECT GEOMETRY FROM BI_TASK WHERE ROWNUM = 1),
                                        ''MASK=ANYINTERACT'') =''TRUE'')';
    DBMS_OUTPUT.put_line(V_UPDATE_SQL);
    Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT', V_UPDATE_SQL);
    EXECUTE IMMEDIATE (V_UPDATE_SQL);
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
    
      Navi_Log.LOG_ERROR('SET_EDIT_FLAG_BY_TASK_EXTENT',
                         'SET_EDIT_FLAG ' || P_TABLE || SQLERRM);
      ROLLBACK;
  END;

  --将BI_task表中的多面，拆成多个2003面
  PROCEDURE SPLITE_BI_TASK_GEOMETRY IS
    V_TEMP_TABLE_SQL VARCHAR2(4000);
    V_GEO_ARRAY      SDO_GEOMETRY_ARRAY;
    V_INSERT_SQL     VARCHAR2(4000);
  BEGIN
    V_TEMP_TABLE_SQL := 'CREATE TABLE TMP_BI_TASK(GEOMETRY SDO_GEOMETRY)';
    EXECUTE IMMEDIATE (V_TEMP_TABLE_SQL);
    SELECT SDO_UTIL.EXTRACT_ALL(GEOMETRY) INTO V_GEO_ARRAY FROM BI_TASK;
    FOR I IN 1 .. V_GEO_ARRAY.COUNT LOOP
      V_INSERT_SQL := 'INSERT INTO TMP_BI_TASK(GEOMETRY) VALUES(:1)';
      EXECUTE IMMEDIATE V_INSERT_SQL
        USING V_GEO_ARRAY(I);
    
    END LOOP;
    COMMIT;
  
  END;

  --如果关系都是touch 和disjo，那么去除这种关系
  PROCEDURE REMOVE_TOUCH_GEOMETRY(P_TMP_TABLE VARCHAR2,
                                  PID_CLOUMN  VARCHAR2) IS
    V_SEARCH_SQL VARCHAR2(400);
    V_UPDATE_SQL VARCHAR2(400);
    TYPE GEO_RELATION_CUR IS REF CURSOR;
    C_GEO_RELATION GEO_RELATION_CUR;
    V_PID          NUMBER(10);
    V_RELATION     VARCHAR2(20);
    V_REMOVE_FLAG  BOOLEAN := TRUE;
    V_PRE_PID      NUMBER(10) := -1;
  BEGIN
    --EDIT_FLAG = 1可编辑
    V_SEARCH_SQL := 'SELECT L.' || PID_CLOUMN || ',
          SDO_RELATE(L.GEOMETRY, T.GEOMETRY, ''MASK=DETERMINE'') DET
       FROM ' || P_TMP_TABLE || ' L, TMP_BI_TASK T
      WHERE L.EDIT_FLAG = 1
      ORDER BY ' || PID_CLOUMN;
    OPEN C_GEO_RELATION FOR V_SEARCH_SQL;
    LOOP
      FETCH C_GEO_RELATION
        INTO V_PID, V_RELATION;
      EXIT WHEN C_GEO_RELATION %NOTFOUND;
      /*写要取游标中的一条一条数据的代码*/
      IF V_PRE_PID != V_PID THEN
        /*切换到新的PID*/
        IF V_PRE_PID <> -1 AND V_REMOVE_FLAG THEN
          /*删除当前对象*/
          V_UPDATE_SQL := 'UPDATE ' || P_TMP_TABLE ||
                          ' SET EDIT_FLAG=0 WHERE ' || PID_CLOUMN || '=' ||
                          V_PRE_PID;
        
          Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT', V_UPDATE_SQL);
          EXECUTE IMMEDIATE (V_UPDATE_SQL);
        END IF;
        V_PRE_PID     := V_PID;
        V_REMOVE_FLAG := TRUE;
      END IF;
    
      IF V_RELATION <> 'TOUCH' AND V_RELATION <> 'DISJOINT' THEN
        V_REMOVE_FLAG := FALSE;
      END IF;
    
    END LOOP;
  
    IF V_PRE_PID <> -1 AND V_REMOVE_FLAG THEN
      /*每次删除是在遍历到下一组时，再删除上一组需要删除的数据，所以如果最后一组需要删除，需要单独处理。如果最后一组不需要处理，这最后删除的数据会处理2遍*/
      V_UPDATE_SQL := 'UPDATE ' || P_TMP_TABLE || ' SET EDIT_FLAG=0 WHERE ' ||
                      PID_CLOUMN || '=' || V_PRE_PID;
    
      Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT', V_UPDATE_SQL);
      EXECUTE IMMEDIATE (V_UPDATE_SQL);
    END IF;
    COMMIT;
  
  EXCEPTION
    WHEN OTHERS THEN
    
      NAVI_LOG.LOG_ERROR('SET_EDIT_FLAG_BY_TASK_EXTENT',
                         'REMOVE_TOUCH_GEOMETRY ' || V_SEARCH_SQL ||
                         SQLERRM);
      CLOSE C_GEO_RELATION;
      ROLLBACK;
    
  END;

  --将临时表中的edit_flag信息合并到原表中
  PROCEDURE MERGE_EDIT_FLAT_TO_TABLE(P_TABLE     VARCHAR2,
                                     P_TMP_TABLE VARCHAR2,
                                     PID_CLOUMN  VARCHAR2) IS
    V_MERGE_SQL VARCHAR2(4000);
  BEGIN
    V_MERGE_SQL := 'MERGE INTO ' || P_TABLE || ' A USING ' || P_TMP_TABLE ||
                   ' B ON (A.' || PID_CLOUMN || '=B.' || PID_CLOUMN ||
                   ') WHEN MATCHED THEN UPDATE SET A.EDIT_FLAG=B.EDIT_FLAG';
  
    Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT',
                      'MERGE_EDIT_FLAT_TO_TABLE' || V_MERGE_SQL);
  
    EXECUTE IMMEDIATE (V_MERGE_SQL);
    COMMIT;
  
  EXCEPTION
    WHEN OTHERS THEN
    
      NAVI_LOG.LOG_ERROR('SET_EDIT_FLAG_BY_TASK_EXTENT',
                         'MERGE_EDIT_FLAT_TO_TABLE ' || V_MERGE_SQL ||
                         SQLERRM);
    
      ROLLBACK;
  END;

  PROCEDURE CREATE_32774_TABLE IS
    V_TEMP_TABLE_SQL VARCHAR2(4000);
    V_TEMP_TABLE     VARCHAR2(32);
    V_UPDATE_SQL     VARCHAR2(4000);
  
  BEGIN
    SPLITE_BI_TASK_GEOMETRY;
    --找到所有的表
    FOR REC IN (SELECT A.TABLE_NAME, C.COLUMN_NAME
                  FROM (select *
                          from user_tab_columns c
                         where c.COLUMN_NAME = 'EDIT_FLAG'
                           AND TABLE_NAME NOT LIKE 'PIPELINE_%'
                           AND TABLE_NAME NOT LIKE 'SHD_%'
                           AND TABLE_NAME NOT LIKE 'NI_%'
                           AND TABLE_NAME NOT LIKE 'VIEW_%'
                           AND TABLE_NAME NOT LIKE '%_100W'
                           AND TABLE_NAME NOT LIKE '%_20W'
                           AND TABLE_NAME NOT LIKE 'AU_%') A,
                       /* (select *
                        from user_tab_columns c
                       where c.COLUMN_NAME = 'GEOMETRY'
                         AND TABLE_NAME NOT LIKE 'PIPELINE_%'
                         AND TABLE_NAME NOT LIKE 'SHD_%'
                         AND TABLE_NAME NOT LIKE 'NI_%'
                         AND TABLE_NAME NOT LIKE 'VIEW_%'
                          AND TABLE_NAME NOT LIKE '%_100W'
                           AND TABLE_NAME NOT LIKE '%_20W'
                         AND TABLE_NAME NOT LIKE 'AU_%') B,*/
                       ( --找到主键
                        SELECT CC.TABLE_NAME, CC.COLUMN_NAME
                          FROM USER_CONSTRAINTS CON, USER_CONS_COLUMNS CC
                         WHERE CON.CONSTRAINT_TYPE = 'P'
                           AND CC.CONSTRAINT_NAME = CON.CONSTRAINT_NAME
                           AND CC.TABLE_NAME NOT LIKE 'PIPELINE_%'
                           AND CC.TABLE_NAME NOT LIKE 'SHD_%'
                           AND CC.TABLE_NAME NOT LIKE 'VIEW_%'
                           AND CC.TABLE_NAME NOT LIKE 'AU_%'
                           AND CC.TABLE_NAME NOT LIKE '%_100W'
                           AND CC.TABLE_NAME NOT LIKE '%_20W'
                           AND CC.TABLE_NAME NOT LIKE 'NI_%') C
                 WHERE A.TABLE_NAME = C.TABLE_NAME
                /* AND A.TABLE_NAME = B.TABLE_NAME*/
                ) LOOP
      V_TEMP_TABLE := 'TMP_' || REC.TABLE_NAME || '_32774';
      /*ADAS_NODE,ADAS_LINK are 3D */
      V_TEMP_TABLE_SQL := 'CREATE TABLE ' || V_TEMP_TABLE ||
                          '  PARALLEL NOLOGGING AS SELECT ' ||
                          REC.COLUMN_NAME ||
                          ',SDO_UTIL.RECTIFY_GEOMETRY(SDO_GEOMETRY(TT.GEOMETRY.SDO_GTYPE,32774,TT.GEOMETRY.SDO_POINT,TT.GEOMETRY.SDO_ELEM_INFO,TT.GEOMETRY.SDO_ORDINATES),0.000005) AS GEOMETRY,EDIT_FLAG FROM (SELECT ' ||
                          REC.COLUMN_NAME ||
                          ',SDO_CS.MAKE_2D(T.GEOMETRY,32774) AS GEOMETRY,0 AS EDIT_FLAG FROM ' ||
                          REC.TABLE_NAME || ' T) TT';
      Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT', V_TEMP_TABLE_SQL);
    
      EXECUTE IMMEDIATE (V_TEMP_TABLE_SQL);
    
      Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT',
                        '创建空间索引' || V_TEMP_TABLE);
    
      CREATE_32774_INDEX(V_TEMP_TABLE, 'GEOMETRY');
    
      V_UPDATE_SQL := 'UPDATE ' || V_TEMP_TABLE || ' L
                 SET L.EDIT_FLAG = 1
                   WHERE SDO_RELATE(L.GEOMETRY,
                                        (SELECT GEOMETRY FROM BI_TASK WHERE ROWNUM = 1),
                                        ''MASK=ANYINTERACT'') =''TRUE''';
    
      Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT', V_UPDATE_SQL);
    
      EXECUTE IMMEDIATE (V_UPDATE_SQL);
    
      COMMIT;
    
      REMOVE_TOUCH_GEOMETRY(V_TEMP_TABLE, REC.COLUMN_NAME);
      MERGE_EDIT_FLAT_TO_TABLE(REC.TABLE_NAME,
                               V_TEMP_TABLE,
                               REC.COLUMN_NAME);
      --UPDATE_EDIT_FLAG(REC.TABLE_NAME,REC.COLUMN_NAME);
    
    END LOOP;
  
  END;

  --LIUQING  :2012/2/6
  --按任务圈进行是否可编辑赋值

  PROCEDURE SET_EDIT_FLAG_BY_TASK_EXTENT IS
    --按图幅导出时，核心图幅数量
    V_CONTAIN_TASK_EXTENT NUMBER(1) := 0;
  
  BEGIN
  
    Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT',
                      '按任务圈进行是否可编辑赋值');
  
    --按图幅导出时，计算核心图幅数量
    SELECT COUNT(1)
      INTO V_CONTAIN_TASK_EXTENT
      FROM BI_TASK
     WHERE GEOMETRY IS NOT NULL;
  
    Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT',
                      '任务圈数：' || V_CONTAIN_TASK_EXTENT);
  
    IF V_CONTAIN_TASK_EXTENT > 0 THEN
    
      CREATE_32774_TABLE;
    
    END IF;
    Navi_Log.LOG_INFO('SET_EDIT_FLAG_BY_TASK_EXTENT', 'COMMIT');
  
    --写入记录到
    INSERT INTO M_PARAMETER
      (NAME, PARAMETER, DESCRIPTION)
    VALUES
      ('SET_EDIT_FLAG', '1', '设置edit_flag结束');
  
    COMMIT;
    --检查初始化
    CHECK_PRE_INIT;
  EXCEPTION
    WHEN OTHERS THEN
      Navi_Log.LOG_ERROR('SET_EDIT_FLAG_BY_TASK_EXTENT',
                         'SET_EDIT_FLAG_BY_TASK_EXTENT ERROR:' || SQLERRM);
      DBMS_OUTPUT.PUT_LINE('SET_EDIT_FLAG_BY_TASK_EXTENT ERROR:' ||
                           SQLERRM);
    
      ROLLBACK;
    
  END;

  --公交按区域导出后，需要删除不完整的数据
  --索引按区域导出后，需要删除不完整的数据
  PROCEDURE DELETE_NOT_INTEGRATED_PT IS
    v_sql     varchar2(2000);
    V_STEP    NUMBER(10) := 10;
    V_SEQ_NUM NUMBER(10) := 0;
    v_count   NUMBER(10) := 0;
  BEGIN
    --模型数据SEQ_NUM不正确，为了兼容不正确的数据，做这个特殊处理
    SELECT NVL(COUNT(1), 0) INTO v_count FROM PT_STRAND_PLATFORM;
    IF (v_count > 0) THEN
      SELECT SEQ_NUM
        INTO V_SEQ_NUM
        FROM PT_STRAND_PLATFORM
       WHERE ROWNUM = 1;
      IF (V_SEQ_NUM >= 10000) THEN
        V_STEP := 10000;
      END IF;
    END IF;
  
    delete from pt_poi_children c
     where c.child_poi_pid not in (select pid from pt_poi);
    delete from pt_poi_parent c
     where c.PARENT_POI_PID not in (select pid from pt_poi);
  
    DELETE FROM pt_poi_children C
     WHERE C.GROUP_ID NOT IN (SELECT GROUP_ID FROM pt_poi_parent);
    --delete PT_POI_PARENT
    DELETE FROM PT_POI_PARENT P
     WHERE P.GROUP_ID NOT IN (SELECT GROUP_ID FROM PT_POI_CHILDREN P);
  
    --删除孤立线路。孤立线路指只经过一个站台的线路，即删除公交线路 Strand 与站台关系表中STRAND_PID字段值唯一的记录。
    DELETE FROM PT_STRAND_PLATFORM
     WHERE STRAND_PID IN (SELECT STRAND_PID
                            FROM (SELECT COUNT(1), STRAND_PID
                                    FROM PT_STRAND_PLATFORM
                                   GROUP BY STRAND_PID
                                  HAVING COUNT(1) = 1));
    --删除孤立站台。孤立站台指没有线路经过的站台，即删除站台信息表中站台号码在公交线路 Strand 与站台关系表中不存在的记录
    DELETE FROM PT_PLATFORM P
     WHERE P.PID NOT IN (SELECT PLATFORM_PID FROM PT_STRAND_PLATFORM);
  
    --将母库的所有PT_STRAND_SCHEDULE搬到子版本中,DDL触发事务
    v_sql := 'CREATE TABLE TEMP_PT_STRAND_PLATFORM AS
      SELECT STRAND_PID, INTERVAL FROM PT_STRAND_PLATFORM@GDB_DB_LINK';
    dbms_output.put_line(v_sql);
    execute immediate v_sql;
    --重新计算PT_STRAND_PLATFORM 的INTERVAL
    v_sql := 'CREATE TABLE TEMP_PT_STRANDPLT_INTERVAL AS 
      SELECT A.STRAND_PID, ROUND(A.TOTAL_INTERVAL / B.AMOUNT) AS INTERVAL
        FROM (SELECT STRAND_PID, SUM(INTERVAL) TOTAL_INTERVAL
                FROM TEMP_PT_STRAND_PLATFORM
               GROUP BY STRAND_PID) A,
             (SELECT STRAND_PID, COUNT(1) AMOUNT
                FROM PT_STRAND_PLATFORM
               GROUP BY STRAND_PID) B
       WHERE A.STRAND_PID = B.STRAND_PID';
    dbms_output.put_line(v_sql);
    execute immediate v_sql;
    --合并到  PT_STRAND_PLATFORM
    v_sql := 'MERGE INTO PT_STRAND_PLATFORM T1
    USING (SELECT STRAND_PID, INTERVAL FROM TEMP_PT_STRANDPLT_INTERVAL) T2
    ON (T1.STRAND_PID = T2.STRAND_PID)
    WHEN MATCHED THEN
      UPDATE SET T1.INTERVAL = T2.INTERVAL';
    dbms_output.put_line(v_sql);
    execute immediate v_sql;
  
    --计算:1 PT_STRAND_SCHEDULE
    --START_TIME（新）= START_TIME（原始）+ INTERVAL（原始）*N
    --END_TIME（新）= END_TIME（原始）+ INTERVAL（原始）*N
    v_sql := 'CREATE TABLE TEMP_PT_STRAND_SCH_ADDTIME AS 
 select A.strand_pid,A.N * B.interval ADD_TIME
      from (select strand_pid, MIN(seq_num) / ' || V_STEP ||
             ' - 1 N
          from PT_STRAND_PLATFORM
         GROUP BY strand_pid) a,
       (select strand_pid, MIN(interval) interval
          from TEMP_PT_STRAND_PLATFORM
         GROUP BY strand_pid) b
 where a.strand_pid = b.strand_pid';
    dbms_output.put_line(v_sql);
    execute immediate v_sql;
  
    --合并到  PT_STRAND_SCHEDULE
    v_sql := 'UPDATE PT_STRAND_SCHEDULE S
    SET (S.START_TIME,S.END_TIME )=
        (SELECT to_char(to_date(''2012-08-13 '' || S.start_time,
                                ''yyyy-mm-dd hh24:mi:ss'') + A.ADD_TIME / 1440,
                        ''hh24:mi''),
                        to_char(to_date(''2012-08-13 '' || S.END_TIME,
                                ''yyyy-mm-dd hh24:mi:ss'') + A.ADD_TIME / 1440,
                        ''hh24:mi'')
           FROM TEMP_PT_STRAND_SCH_ADDTIME A
          WHERE A.strand_pid = S.strand_pid)
  WHERE EXISTS (SELECT 1
           FROM TEMP_PT_STRAND_SCH_ADDTIME A
          WHERE A.strand_pid = S.strand_pid)';
    dbms_output.put_line(v_sql);
    execute immediate v_sql;
  
    -- ① 如果POI有父子关系，则：
    --如果它的父不在采样框中，将该POI的父子关系清除；
    --如果它的父和部分子在采样框中，则将框中的父子关系保留，框外的子清除
    DELETE FROM IX_POI_CHILDREN C
     WHERE C.CHILD_POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_PARENT P
     WHERE P.PARENT_POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CHILDREN C
     WHERE C.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POI_PARENT);
    --删除孤父
    DELETE FROM IX_POI_PARENT P
     WHERE P.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POI_CHILDREN);
  
    --②  如果POI有同一关系，则只要其组成结构中有部分在采样框外，则将其同一关系清除
    DELETE FROM IX_SAMEPOI_PART
     WHERE GROUP_ID IN
           (SELECT GROUP_ID
              FROM IX_SAMEPOI_PART P
             WHERE P.POI_PID NOT IN (SELECT PID FROM IX_POI));
  
    DELETE FROM IX_SAMEPOI P
     WHERE P.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_SAMEPOI_PART);
  
    --点门牌 
  
    DELETE FROM IX_POINTADDRESS_CHILDREN C
     WHERE C.CHILD_PA_PID NOT IN (SELECT PID FROM IX_POINTADDRESS);
    DELETE FROM IX_POINTADDRESS_PARENT P
     WHERE P.PARENT_PA_PID NOT IN (SELECT PID FROM IX_POINTADDRESS);
    DELETE FROM IX_POINTADDRESS_CHILDREN C
     WHERE C.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POINTADDRESS_PARENT);
    --删除孤父
    DELETE FROM IX_POINTADDRESS_PARENT P
     WHERE P.GROUP_ID NOT IN
           (SELECT GROUP_ID FROM IX_POINTADDRESS_CHILDREN);
  
    COMMIT;
  
  END;

  --异步处理数据导出后
  PROCEDURE ASN_CAL_SET_EDIT_FLAG IS
  BEGIN
    DBMS_SCHEDULER.create_job(job_name   => 'ASN_CAL_SET_EDIT_FLAG_JOB',
                              job_type   => 'STORED_PROCEDURE',
                              job_action => 'EXECUTE_AFTER_CREATE.SET_EDIT_FLAG_BY_TASK_EXTENT',
                              enabled    => true);
  
  END;

  --PURPOSE : 子版本创建后执行的同步脚本
  PROCEDURE SYNCHRONOUS_EXECUTE IS
  BEGIN
    DELETE_NOT_INTEGRATED_DATA;
  END;

  --PURPOSE : 子版本创建后执行的异步脚本
  PROCEDURE ASYNCHRONOUS_EXECUTE IS
  BEGIN
    --异步处理数据导出后,进行EDIT_FLAG赋值
    ASN_CAL_SET_EDIT_FLAG;
    --检查预处理
    --CHECK_PRE_INIT;
    --此功能移到ASN_CAL_SET_EDIT_FLAG中执行，因为check_pre_init创建空间索引，对edit_flag赋值有影响
  END;

END EXECUTE_AFTER_CREATE;
/
