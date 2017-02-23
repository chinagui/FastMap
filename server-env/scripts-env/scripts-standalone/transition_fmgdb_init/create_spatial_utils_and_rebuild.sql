create or replace package SPATIAL_UTILS is

  -- Author  : LIUQING
  -- Created : 2011/2/9 16:32:10
  -- Purpose :

  PROCEDURE CREATE_DB_SPATIAL_INDEX(DROP_EXITENT IN BOOLEAN DEFAULT FALSE);
  PROCEDURE CREATE_SPATIAL_INDEX(P_TABLE VARCHAR2, P_CLOUMN VARCHAR2);
  PROCEDURE DROP_SPATIAL_INDEX(P_TABLE VARCHAR2);
  PROCEDURE TRANSFER_TREE2FLAT(SUFFIX IN VARCHAR2);
  PROCEDURE TRANSFER_COMMON(INSERT_SQL IN VARCHAR2,PC_TABLE IN VARCHAR2  );
  /*索引补全*/
  PROCEDURE COMPLETE_DB_SPATIAL_INDEX;
  /*索引全部重建*/
  PROCEDURE REBUILD_ALL_DB_SPATIAL_INDEX;
  FUNCTION from_wktgeometry(wkt CLOB) RETURN sdo_geometry;
  FUNCTION from_wktgeometry(wkt VARCHAR2) RETURN sdo_geometry;
  FUNCTION from_wktgeometry32774(wkt VARCHAR2) RETURN sdo_geometry;
  FUNCTION from_wktgeometry32774(wkt clob) RETURN sdo_geometry;
end SPATIAL_UTILS;
/


CREATE OR REPLACE PACKAGE BODY SPATIAL_UTILS IS

  C_TOLERANCE  CONSTANT NUMBER := 0.5;
  C_SRID       CONSTANT INT := 8307;
  G_ITYPE_NAME CONSTANT VARCHAR2(20) := 'SPATIAL_INDEX'; 

  PROCEDURE DROP_SPATIAL_INDEX(P_TABLE VARCHAR2) IS
    V_INDEX_NAME VARCHAR2(100);
  BEGIN
    SELECT P.INDEX_NAME
      INTO V_INDEX_NAME
      FROM USER_INDEXES P
     WHERE P.TABLE_NAME = UPPER(P_TABLE)
       AND P.ITYP_NAME = G_ITYPE_NAME;
    IF (V_INDEX_NAME IS NOT NULL) THEN
      DBMS_OUTPUT.PUT_LINE('DROP INDEX ' || V_INDEX_NAME); 
      EXECUTE IMMEDIATE 'DROP INDEX ' || V_INDEX_NAME;
    END IF;
 
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('DROP_SPATIAL_INDEX ' || SQLERRM);
  END;

  PROCEDURE CREATE_TABLE_METADATA(P_TABLE VARCHAR2, P_CLOUMN VARCHAR2,P_DIMENSION IN NUMBER DEFAULT 2) IS
    DIMINFO MDSYS.SDO_DIM_ARRAY;
  BEGIN
    IF P_DIMENSION = 2 THEN
      DIMINFO := MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG',
                                                         -180,
                                                         180,
                                                         C_TOLERANCE),
                                   MDSYS.SDO_DIM_ELEMENT('YLAT',
                                                         -90,
                                                         90,
                                                         C_TOLERANCE));
    ELSE
      DIMINFO := MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('XLONG',
                                                           -180,
                                                           180,
                                                           C_TOLERANCE),
                                     MDSYS.SDO_DIM_ELEMENT('YLAT',
                                                           -90,
                                                           90,
                                                           C_TOLERANCE),
                                     MDSYS.SDO_DIM_ELEMENT('Z',
                                                           -1000,
                                                           1000,
                                                           C_TOLERANCE));
    END IF;
   
 
    DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = UPPER(P_TABLE);
 
    INSERT INTO USER_SDO_GEOM_METADATA
    VALUES
      (P_TABLE, P_CLOUMN, DIMINFO, C_SRID);
 
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      DBMS_OUTPUT.PUT_LINE('CREATE_TABLE_META ' || SQLERRM);
  END;

  PROCEDURE CREATE_SPATIAL_INDEX(P_TABLE VARCHAR2, P_CLOUMN VARCHAR2) IS
    V_COUNT NUMBER;
    P_DIMENSION NUMBER;
  BEGIN
    SELECT COUNT(1)
      INTO V_COUNT
      FROM USER_INDEXES P
     WHERE P.TABLE_NAME = UPPER(P_TABLE)
       AND P.ITYP_NAME = G_ITYPE_NAME;
    IF (V_COUNT = 0) THEN
      IF((UPPER(P_TABLE)='ADAS_LINK' OR UPPER(P_TABLE)='ADAS_NODE') AND UPPER(P_CLOUMN)='GEOMETRY') THEN
            P_DIMENSION:=3;
            ELSE
               P_DIMENSION:=2;
      END IF;
   
      CREATE_TABLE_METADATA(P_TABLE, P_CLOUMN,P_DIMENSION);
      EXECUTE IMMEDIATE 'CREATE INDEX ' || P_TABLE || '_' || P_CLOUMN ||
                        ' ON ' || P_TABLE || '(' || P_CLOUMN ||
                        ')  INDEXTYPE IS MDSYS.SPATIAL_INDEX';
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('CREATE_SPATIAL_INDEX ' || SQLERRM);
  END;
 
  PROCEDURE CREATE_PARALLEL_SPATIAL_INDEX(P_TABLE VARCHAR2, P_CLOUMN VARCHAR2) IS
    V_COUNT NUMBER;
    P_DIMENSION NUMBER;
  BEGIN
    SELECT COUNT(1)
      INTO V_COUNT
      FROM USER_INDEXES P
     WHERE P.TABLE_NAME = UPPER(P_TABLE)
       AND P.ITYP_NAME = G_ITYPE_NAME;
    IF (V_COUNT = 0) THEN
      IF((UPPER(P_TABLE)='ADAS_LINK' OR UPPER(P_TABLE)='ADAS_NODE') AND UPPER(P_CLOUMN)='GEOMETRY') THEN
            P_DIMENSION:=3;
            ELSE
               P_DIMENSION:=2;
      END IF;
   
      CREATE_TABLE_METADATA(P_TABLE, P_CLOUMN,P_DIMENSION);
      EXECUTE IMMEDIATE 'CREATE INDEX ' || P_TABLE || '_' || P_CLOUMN ||
                        ' ON ' || P_TABLE || '(' || P_CLOUMN ||
                        ')  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARALLEL';
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('CREATE_SPATIAL_INDEX ' || SQLERRM);
  END;

  PROCEDURE CREATE_DB_SPATIAL_INDEX(DROP_EXITENT IN BOOLEAN DEFAULT FALSE) IS
  BEGIN
 
    FOR REC IN (SELECT TABLE_NAME, COLUMN_NAME
                  FROM USER_TAB_COLS
                 WHERE DATA_TYPE = 'SDO_GEOMETRY'
                   AND table_name not like 'NI_%'
                   AND table_name not like 'TMP_%'
                   AND table_name not like 'TEMP_%'
                   AND table_name not like 'PIPE_%'
                   AND table_name not like 'SHD_%') LOOP
      IF (DROP_EXITENT) THEN
        DROP_SPATIAL_INDEX(REC.TABLE_NAME);
     
      END IF;
      CREATE_SPATIAL_INDEX(REC.TABLE_NAME, REC.COLUMN_NAME);
   
    END LOOP;
  END;
 
  PROCEDURE COMPLETE_DB_SPATIAL_INDEX IS
    V_COUNT NUMBER;
  BEGIN
 
    FOR REC IN (SELECT TABLE_NAME, COLUMN_NAME
                  FROM USER_TAB_COLS
                 WHERE DATA_TYPE = 'SDO_GEOMETRY'
                   AND table_name not like 'NI_%'
                   AND table_name not like 'TMP_%'
                   AND table_name not like 'TEMP_%'
                   AND table_name not like 'PIPE_%'
                   AND table_name not like 'SHD_%') LOOP
      EXECUTE IMMEDIATE 'SELECT count(1) from user_indexes t where t.ityp_name=''SPATIAL_INDEX'' and status = ''INVALID'' and table_name = :table_name'
        INTO V_COUNT
        USING REC.TABLE_NAME;            
      IF (V_COUNT  > 0) THEN
        DROP_SPATIAL_INDEX(REC.TABLE_NAME);  
      END IF;
     
      CREATE_PARALLEL_SPATIAL_INDEX(REC.TABLE_NAME, REC.COLUMN_NAME);
   
    END LOOP;
  END;
 
  PROCEDURE REBUILD_ALL_DB_SPATIAL_INDEX IS
  BEGIN
 
    FOR REC IN (SELECT TABLE_NAME, COLUMN_NAME
                  FROM USER_TAB_COLS
                 WHERE DATA_TYPE = 'SDO_GEOMETRY'
                   AND table_name not like 'NI_%'
                   AND table_name not like 'TMP_%'
                   AND table_name not like 'TEMP_%'
                   AND table_name not like 'PIPE_%'
                   AND table_name not like 'SHD_%') LOOP
      DROP_SPATIAL_INDEX(REC.TABLE_NAME);  
      CREATE_PARALLEL_SPATIAL_INDEX(REC.TABLE_NAME, REC.COLUMN_NAME);
   
    END LOOP;
  END;

  FUNCTION from_wktgeometry(wkt CLOB) RETURN sdo_geometry IS
    geometry sdo_geometry;
  BEGIN
    geometry          := sdo_util.from_wktgeometry(wkt);
    geometry.sdo_srid := 8307;
    RETURN geometry;
  END;

  FUNCTION from_wktgeometry(wkt VARCHAR2) RETURN sdo_geometry IS
    geometry sdo_geometry;
  BEGIN
    geometry          := sdo_util.from_wktgeometry(wkt);
    geometry.sdo_srid := 8307;
    RETURN geometry;
  END;
 
  FUNCTION from_wktgeometry32774(wkt VARCHAR2) RETURN sdo_geometry IS
    geometry sdo_geometry;
  BEGIN
    geometry          := sdo_util.from_wktgeometry(wkt);
    geometry.sdo_srid := 32774;
    RETURN geometry;
  END;

  FUNCTION from_wktgeometry32774(wkt clob) RETURN sdo_geometry IS
    geometry sdo_geometry;
  BEGIN
    geometry          := sdo_util.from_wktgeometry(wkt);
    geometry.sdo_srid := 32774;
    RETURN geometry;
  END;


   PROCEDURE TRANSFER_TREE2FLAT(SUFFIX IN VARCHAR2) IS
    V_TABLE VARCHAR2(100);
    V_SQL VARCHAR2(255);
  BEGIN
    V_TABLE := 'TEMP_PARENT_CHILD_'||SUFFIX;
    V_SQL := 'INSERT INTO '||V_TABLE||' (CHILD_PID,PARENT_PID) SELECT  C.CHILD_POI_PID  CHILD_PID, P.PARENT_POI_PID PARENT_PID  FROM IX_POI_CHILDREN C,IX_POI_PARENT P WHERE C.GROUP_ID= P.GROUP_ID';
    TRANSFER_COMMON(V_SQL,V_TABLE);
    V_TABLE := 'TEMP_PARENT_CHILD_PC_'||SUFFIX;
    V_SQL := 'INSERT INTO '||V_TABLE||' (CHILD_PID,PARENT_PID) SELECT  C.CHILD_PA_PID  CHILD_PID, P.PARENT_PA_PID PARENT_PID  FROM IX_POINTADDRESS_CHILDREN C,IX_POINTADDRESS_PARENT P WHERE C.GROUP_ID= P.GROUP_ID';
    TRANSFER_COMMON(V_SQL,V_TABLE);
  END ;

  PROCEDURE TRANSFER_COMMON(INSERT_SQL IN VARCHAR2,PC_TABLE IN VARCHAR2) IS
    V_COUNT NUMBER;
    V_SQL VARCHAR2(255);
    V_UPDATE VARCHAR2(655);
  BEGIN
    EXECUTE IMMEDIATE 'TRUNCATE TABLE '||PC_TABLE;
    COMMIT;
    EXECUTE IMMEDIATE  INSERT_SQL;

    V_UPDATE := 'delete from '||PC_TABLE||' where  CHILD_PID in (select   CHILD_PID from '||PC_TABLE||' group by   CHILD_PID   having count(CHILD_PID) > 1) and rowid not in (select min(rowid) from   '||PC_TABLE||' group by child_pid having count(child_pid )>1)';
    EXECUTE IMMEDIATE  V_UPDATE;
    COMMIT;
    V_SQL :='SELECT COUNT(1)  FROM  '||PC_TABLE||' T ,'||PC_TABLE||' T1 WHERE T.PARENT_PID= T1.CHILD_PID AND T.PARENT_PID != T.CHILD_PID AND T1.PARENT_PID != T1.CHILD_PID';

    EXECUTE IMMEDIATE V_SQL INTO V_COUNT;


    V_UPDATE := 'merge /*+NO_MERGE(T5)*/  into '||PC_TABLE||' t using (  select t1.rowid tid,t2.parent_pid from '||PC_TABLE||' t1,'||PC_TABLE||' t2 where t1.parent_pid=t2.child_pid  ) t5 on (t.rowid = t5.tid) when matched then update set t.parent_pid=t5.parent_pid';
    WHILE V_COUNT > 0 LOOP
      EXECUTE IMMEDIATE V_UPDATE;
      COMMIT;
      EXECUTE IMMEDIATE V_SQL INTO V_COUNT;
      COMMIT;
    END LOOP;
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TRANSFER_TREE2FLAT ' || SQLERRM);
  END ;
END SPATIAL_UTILS;
/


begin
  SPATIAL_UTILS.COMPLETE_DB_SPATIAL_INDEX();
end;
/

COMMIT;

EXIT;