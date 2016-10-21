CREATE OR REPLACE TYPE MW_MAP IS OBJECT
(
  WORD    VARCHAR2(255),
  WORDPY  VARCHAR2(255),
  WORDPOS  NUMBER(10),
  WORDLEN  NUMBER(10),
  WORDPY2  VARCHAR2(255)
);
/

CREATE OR REPLACE TYPE MW_ARRAY IS TABLE OF MW_MAP;
/

CREATE OR REPLACE PACKAGE COMMON_UTIL AUTHID CURRENT_USER IS

  --一些ADAS测线形状匹配的常量
  C_LIMITPAGE NUMBER := 1000;
  C_TOLERANCE NUMBER := 0.00005;

  --201502110942_修改RTIC接口
  TYPE STRINGARRAY IS TABLE OF VARCHAR2(32767);
  TYPE CLOBARRAY IS TABLE OF CLOB;

  FUNCTION CLOB_SUBSTR(LOB_LOC IN CLOB,
                       AMOUNT  IN INTEGER,
                       OFFSET  IN INTEGER) RETURN CLOB;

  FUNCTION SPLITSTRING(P_STR IN VARCHAR2, P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN STRINGARRAY;

  FUNCTION SPLITCLOB(P_STR IN CLOB, P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN CLOBARRAY;

  FUNCTION SPLITSTRING(P_STR IN CLOB, P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN STRINGARRAY;

  /*FUNCTION GET_PT_LINK_SIDE(LINKGEOM MDSYS.SDO_GEOMETRY,
                            PT       MDSYS.SDO_GEOMETRY) RETURN NUMBER
    PARALLEL_ENABLE;*/

  FUNCTION APPLY_PID(V_DBLINKNAME VARCHAR2,
                     V_TABLE_NAME IN VARCHAR2,
                     V_LIMIT      IN INTEGER,
                     P_TASK_NAME  VARCHAR2) RETURN NUMBER;

  FUNCTION APPLY_RTIC(V_DBLINKNAME IN VARCHAR2,
                      P_MESH       IN VARCHAR2,
                      P_CLASS      IN NUMBER,
                      P_TASK_NAME  IN VARCHAR2) RETURN NUMBER;

  FUNCTION GET_RTIC_INFO(V_DBLINKNAME IN VARCHAR2,
                         P_MESH       IN VARCHAR2,
                         P_CLASS      IN NUMBER,
                         P_RTICID     IN NUMBER,
                         P_STATE      OUT VARCHAR2,
                         P_SEASON     OUT VARCHAR2) RETURN NUMBER; --成功返回1，失败为0

  FUNCTION SET_RTIC_INFO(V_DBLINKNAME IN VARCHAR2,
                         P_MESH       IN VARCHAR2,
                         P_CLASS      IN NUMBER,
                         P_RTICID     IN NUMBER,
                         P_STATE      IN VARCHAR2,
                         P_SEASON     IN VARCHAR2) RETURN NUMBER; --成功返回1，失败为0

  --检查括号匹配方法,P_LEFT和P_RIGHT可以传多个
  --例如 check_parenthesis('asf(44<a>b)','(<',')>')
  FUNCTION CHECK_PARENTHESIS(P_STR   IN VARCHAR2,
                             P_LEFT  IN VARCHAR2,
                             P_RIGHT IN VARCHAR2) RETURN NUMBER
    PARALLEL_ENABLE;

  /**
  传入全角的字符串，返回对应的半角字符串，如果没有对应的半角字符，则返回原字符
  */
  FUNCTION GET_HALF_FORMAT_STRING(P_STR IN VARCHAR2) RETURN VARCHAR2;

  FUNCTION MD5_CHECKSUM(IS_STR IN VARCHAR2) RETURN VARCHAR2;

  FUNCTION MD5_CHECKSUM(IS_STR IN CLOB) RETURN VARCHAR2;

  FUNCTION MD5_CHECKSUM(IG_GEOM IN SDO_GEOMETRY) RETURN VARCHAR2;

  --by kwz->
  --字符串替换（NEW_STR为空表示删除，PEX_STR为分隔符，为空表示没有分隔符）
  FUNCTION STR_REPLACE(BASE_STR IN VARCHAR2,
                       OLD_STR  IN VARCHAR2,
                       NEW_STR  IN VARCHAR2,
                       PEX_STR  IN VARCHAR2) RETURN VARCHAR2;

  TYPE S_REPLACE_IN IS RECORD(
    PID      VARCHAR2(200), --唯一标示，原样输出
    BASE_STR VARCHAR2(4000), --源字符串
    OLD_STR  VARCHAR2(200), --被替换字符
    NEW_STR  VARCHAR2(200), --替换后字符--为空表示删除
    PEX_STR  VARCHAR2(20) --分隔字符--为空表示没有分隔符
    );
  TYPE REPLACE_CURSOR IS REF CURSOR RETURN S_REPLACE_IN;

  TYPE S_REPLACE_OUT IS RECORD(
    PID       VARCHAR2(200), --唯一标示，原样输出
    BASE_STR  VARCHAR2(4000), --源字符串
    AFTER_STR VARCHAR2(4000) --替换之后的字符串
    );
  TYPE REPLACE_TAB IS TABLE OF S_REPLACE_OUT;

  --字符串替换（实现一个字符串被替换多次）
  FUNCTION TAB_REPLACE(CUR IN REPLACE_CURSOR) RETURN REPLACE_TAB
    PIPELINED ORDER CUR BY(BASE_STR)
    PARALLEL_ENABLE(PARTITION CUR BY HASH(BASE_STR));

  TYPE IN_STRING IS RECORD(
    IN_ROWID VARCHAR2(50), --主标识（原值输出）
    IN_STR   CLOB);
  TYPE IN_STRING_CURSOR IS REF CURSOR RETURN IN_STRING;

  TYPE OUT_STRING IS RECORD(
    OUT_ROWID VARCHAR2(50), --主标识（原值输出）
    OUT_SEQ   NUMBER(10),
    OUT_STR   VARCHAR2(4000));
  TYPE OUT_STRING_TAB IS TABLE OF OUT_STRING;

  FUNCTION SPLITSTRING_TAB(CUR   IN IN_STRING_CURSOR,
                           P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN OUT_STRING_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION CUR BY ANY);

  --<-by kwz
  FUNCTION GET_BRACKETS_CONTENT(IN_STR        IN VARCHAR2,
                                I_POS         IN NUMBER,
                                LEFT_BRACKET  IN VARCHAR2 DEFAULT '(',
                                RIGHT_BRACKET IN VARCHAR2 DEFAULT ')')
    RETURN VARCHAR2;

END COMMON_UTIL;
/

CREATE OR REPLACE PACKAGE BODY COMMON_UTIL IS

  FUNCTION CLOB_SUBSTR(LOB_LOC IN CLOB,
                       AMOUNT  IN INTEGER,
                       OFFSET  IN INTEGER) RETURN CLOB IS
    LIMIT_NUM   BINARY_INTEGER := 8000;
    V_LIMIT     BINARY_INTEGER := 0;
    V_AMOUNT    INTEGER;
    RESULT_CLOB CLOB;
    MIN_CLOB    CLOB;
  BEGIN
    V_AMOUNT := AMOUNT;
    FOR I IN 0 .. FLOOR(AMOUNT / LIMIT_NUM) LOOP
      IF LIMIT_NUM < V_AMOUNT THEN
        V_LIMIT := LIMIT_NUM;
      ELSE
        V_LIMIT := V_AMOUNT;
      END IF;
      MIN_CLOB    := DBMS_LOB.SUBSTR(LOB_LOC,
                                     V_LIMIT,
                                     OFFSET + I * LIMIT_NUM);
      RESULT_CLOB := RESULT_CLOB || MIN_CLOB;
      V_AMOUNT    := V_AMOUNT - LIMIT_NUM;
      --DBMS_LOB.APPEND(RESULT_CLOB,MIN_CLOB);
    END LOOP;
    RETURN RESULT_CLOB;
  END;

  FUNCTION SPLITSTRING(P_STR IN VARCHAR2, P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN STRINGARRAY IS
    SARRAY STRINGARRAY;
    POS    BINARY_INTEGER;
    SIDX   BINARY_INTEGER := 1;
    AIMSTR VARCHAR2(32767);
  BEGIN
    AIMSTR := P_STR;

    SARRAY := STRINGARRAY();

    --    AIMSTR := LTRIM(RTRIM(AIMSTR, P_DLT), P_DLT);

    IF AIMSTR IS NULL THEN
      RETURN SARRAY;
    END IF;

    WHILE (SIDX > 0) LOOP
      POS := INSTR(AIMSTR, P_DLT, SIDX);
      SARRAY.EXTEND(1);
      IF POS > 0 THEN
        SARRAY(SARRAY.COUNT) := SUBSTR(AIMSTR, SIDX, POS - SIDX);
        SIDX := POS + LENGTH(P_DLT);
      ELSE
        SARRAY(SARRAY.COUNT) := SUBSTR(AIMSTR, SIDX);
        SIDX := 0;
      END IF;
    END LOOP;
    RETURN SARRAY;
  END;

  FUNCTION SPLITSTRING(P_STR IN CLOB, P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN STRINGARRAY IS
    SARRAY STRINGARRAY;
    POS    BINARY_INTEGER;
    SIDX   BINARY_INTEGER := 1;
    AIMSTR CLOB;
  BEGIN
    AIMSTR := P_STR;

    SARRAY := STRINGARRAY();

    --    AIMSTR := LTRIM(RTRIM(AIMSTR, P_DLT), P_DLT);

    IF AIMSTR IS NULL THEN
      RETURN SARRAY;
    END IF;

    WHILE (SIDX > 0) LOOP
      POS := DBMS_LOB.INSTR(AIMSTR, P_DLT, SIDX);
      SARRAY.EXTEND(1);
      IF POS > 0 THEN
        SARRAY(SARRAY.COUNT) := DBMS_LOB.SUBSTR(AIMSTR, POS - SIDX, SIDX);
        SIDX := POS + LENGTH(P_DLT);
      ELSE
        SARRAY(SARRAY.COUNT) := DBMS_LOB.SUBSTR(AIMSTR,
                                                LENGTH(AIMSTR) - SIDX + 1,
                                                SIDX);
        SIDX := 0;
      END IF;
    END LOOP;
    RETURN SARRAY;
  END;

  FUNCTION SPLITCLOB(P_STR IN CLOB, P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN CLOBARRAY IS
    SARRAY CLOBARRAY;
    POS    BINARY_INTEGER;
    SIDX   BINARY_INTEGER := 1;
    AIMSTR CLOB;
  BEGIN
    AIMSTR := P_STR;

    SARRAY := CLOBARRAY();

    --    AIMSTR := LTRIM(RTRIM(AIMSTR, P_DLT), P_DLT);

    IF AIMSTR IS NULL THEN
      RETURN SARRAY;
    END IF;

    WHILE (SIDX > 0) LOOP
      POS := DBMS_LOB.INSTR(AIMSTR, P_DLT, SIDX);
      SARRAY.EXTEND(1);
      IF POS > 0 THEN
        SARRAY(SARRAY.COUNT) := CLOB_SUBSTR(AIMSTR, POS - SIDX, SIDX);
        SIDX := POS + LENGTH(P_DLT);
      ELSE
        SARRAY(SARRAY.COUNT) := CLOB_SUBSTR(AIMSTR,
                                            LENGTH(AIMSTR) - SIDX + 1,
                                            SIDX);
        SIDX := 0;
      END IF;
    END LOOP;
    RETURN SARRAY;
  END;

  /*FUNCTION GET_PT_LINK_SIDE(LINKGEOM MDSYS.SDO_GEOMETRY,
                            PT       MDSYS.SDO_GEOMETRY) RETURN NUMBER
    PARALLEL_ENABLE IS
    SIDE     VARCHAR(1);
    DISTANCE NUMBER;
    NSIDE    NUMBER;
    RES_PT   MDSYS.VERTEX_TYPE;
  BEGIN
    RES_PT := PIPELINE_SDO.EUCLIDEAN_PROJECT_PT(SDO_UTIL.GETVERTICES(LINKGEOM),
                                                SDO_UTIL.GETVERTICES(PT) (1),
                                                DISTANCE,
                                                SIDE,
                                                TRUE);
    IF SIDE = 'L' THEN
      NSIDE := 1;
    ELSIF SIDE = 'R' THEN
      NSIDE := 2;
    ELSE
      NSIDE := 3;
    END IF;
    RETURN NSIDE;
  END;*/

  --向DMS的PID管理服务器申请ID
  FUNCTION APPLY_PID(V_DBLINKNAME VARCHAR2,
                     V_TABLE_NAME IN VARCHAR2,
                     V_LIMIT      IN INTEGER,
                     P_TASK_NAME  VARCHAR2) RETURN NUMBER IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    SEG_PID  VARCHAR2(2000);
    CLIENT   VARCHAR2(20) := 'PID_MAN';
    V_CLIENT VARCHAR2(50) := USER;
    SQLBLOCK VARCHAR2(3000);
    POS      BINARY_INTEGER;
    START_ID BINARY_INTEGER;
  BEGIN
    IF V_LIMIT <= 0 THEN
      RETURN - 1;
    END IF;
    SQLBLOCK := 'BEGIN
                 :SEG_PID:=DMS_PID_MAN.APPLY_PID@' ||
                V_DBLINKNAME || '(''' || V_TABLE_NAME || ''', ' || V_LIMIT ||
                ', ''' || V_CLIENT || ''',''yes'','''',''' || P_TASK_NAME ||
                ''');
               END;';

    EXECUTE IMMEDIATE SQLBLOCK
      USING OUT SEG_PID;

    COMMIT;

    POS := INSTR(SEG_PID, ',');

    START_ID := SUBSTR(SEG_PID, 1, POS - 1);

    RETURN START_ID;

  END;

  FUNCTION APPLY_RTIC(V_DBLINKNAME IN VARCHAR2,
                      P_MESH       IN VARCHAR2,
                      P_CLASS      IN NUMBER,
                      P_TASK_NAME  IN VARCHAR2) RETURN NUMBER IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    SEG_PID      VARCHAR2(2000);
    V_CLIENT     VARCHAR2(50) := USER;
    SQLBLOCK     VARCHAR2(3000);
    POS          BINARY_INTEGER;
    START_ID     BINARY_INTEGER;
    P_VERSION_ID NUMBER;
  BEGIN
    EXECUTE IMMEDIATE 'SELECT NVL(MAX(RTIC_ID),10000) + 1 FROM COPSYS_RTIC_CODE_APPLY WHERE TASK_NAME = :T AND MESH_ID = :A AND RTIC_CLASS = :B '
      INTO START_ID
      USING P_TASK_NAME, P_MESH, P_CLASS;

    EXECUTE IMMEDIATE 'INSERT INTO COPSYS_RTIC_CODE_APPLY(TASK_NAME, MESH_ID, RTIC_CLASS, RTIC_ID,DEL_FALG) VALUES(:T,:A,:B,:C,0)'
      USING P_TASK_NAME, P_MESH, P_CLASS, START_ID;
    COMMIT;

    RETURN START_ID;

  END;

  FUNCTION GET_RTIC_INFO(V_DBLINKNAME IN VARCHAR2,
                         P_MESH       IN VARCHAR2,
                         P_CLASS      IN NUMBER,
                         P_RTICID     IN NUMBER,
                         P_STATE      OUT VARCHAR2,
                         P_SEASON     OUT VARCHAR2) RETURN NUMBER --成功返回1，失败为0
   IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    SEG_PID  VARCHAR2(2000);
    SQLBLOCK VARCHAR2(3000);
    POS      BINARY_INTEGER;
  BEGIN

    SQLBLOCK := 'BEGIN
                 :SEG_PID:=DMS_RTICID_MAN.GET_RTICID_STATE_SEASON@' ||
                V_DBLINKNAME || '(''' || P_MESH || ''', ' || P_CLASS || ',' ||
                P_RTICID || ');
               END;';

    EXECUTE IMMEDIATE SQLBLOCK
      USING OUT SEG_PID;
    COMMIT;

    POS := INSTR(SEG_PID, ',');
    IF POS < 1 THEN
      RETURN 0;
    END IF;

    P_STATE  := SUBSTR(SEG_PID, 1, POS - 1);
    P_SEASON := SUBSTR(SEG_PID, POS + 1);
    RETURN 1;
  END;

  FUNCTION SET_RTIC_INFO(V_DBLINKNAME IN VARCHAR2,
                         P_MESH       IN VARCHAR2,
                         P_CLASS      IN NUMBER,
                         P_RTICID     IN NUMBER,
                         P_STATE      IN VARCHAR2,
                         P_SEASON     IN VARCHAR2) RETURN NUMBER --成功返回1，失败为0
   IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    SEG_PID  VARCHAR2(2000);
    SQLBLOCK VARCHAR2(3000);
  BEGIN
    SQLBLOCK := 'BEGIN
                 :SEG_PID:=DMS_RTICID_MAN.UPDATE_RTICID_STANDALONE@' ||
                V_DBLINKNAME || '(''' || P_MESH || ''', ' || P_CLASS || ',' ||
                P_RTICID || ',''' || P_STATE || ''',''' || P_SEASON ||
                ''');
               END;';

    EXECUTE IMMEDIATE SQLBLOCK
      USING OUT SEG_PID;
    COMMIT;

    IF LOWER(SEG_PID) = 'true' THEN
      RETURN 1;
    ELSE
      RETURN 0;
    END IF;
  END;

  FUNCTION CHECK_PARENTHESIS(P_STR   IN VARCHAR2,
                             P_LEFT  IN VARCHAR2,
                             P_RIGHT IN VARCHAR2) RETURN NUMBER
    PARALLEL_ENABLE IS
    V_STR   VARCHAR2(30000);
    V_LEFT  VARCHAR2(10);
    V_RIGHT VARCHAR2(10);
    V_NUM   PLS_INTEGER;
  BEGIN
    IF P_STR IS NULL THEN
      RETURN 0;
    END IF;
    IF LENGTH(P_LEFT) <> LENGTH(P_RIGHT) THEN
      RETURN - 1;
    END IF;
    FOR N IN 1 .. LENGTH(P_LEFT) LOOP
      V_LEFT  := SUBSTR(P_LEFT, N, 1);
      V_RIGHT := SUBSTR(P_RIGHT, N, 1);
      V_NUM   := 0;
      FOR I IN 1 .. LENGTH(P_STR) LOOP
        V_STR := SUBSTR(P_STR, I, 1);
        IF V_STR = V_LEFT THEN
          V_NUM := V_NUM + 1;
        ELSIF V_STR = V_RIGHT THEN
          V_NUM := V_NUM - 1;
        END IF;
        IF V_NUM < 0 THEN
          RETURN - 1;
        END IF;
      END LOOP;
      IF V_NUM <> 0 THEN
        RETURN - 1;
      END IF;
    END LOOP;
    RETURN 0;
  END;

  FUNCTION GET_HALF_FORMAT_STRING(P_STR IN VARCHAR2) RETURN VARCHAR2 IS
    VS_TEMP_CHAR VARCHAR2(10);
    VS_STR_CHAR  VARCHAR2(10);
    VS_RET_STR   VARCHAR2(4000);

  BEGIN
    FOR I IN 1 .. LENGTH(P_STR) LOOP
      VS_STR_CHAR := SUBSTR(P_STR, I, 1);
      SELECT MAX(HALF_WIDTH)
        INTO VS_TEMP_CHAR
        FROM TY_CHARACTER_FULL2HALF
       WHERE FULL_WIDTH = VS_STR_CHAR;

      IF VS_TEMP_CHAR IS NULL THEN
        VS_TEMP_CHAR := VS_STR_CHAR;
      END IF;

      VS_RET_STR := VS_RET_STR || VS_TEMP_CHAR;
    END LOOP;

    RETURN VS_RET_STR;
  END;

  FUNCTION MD5_CHECKSUM(IS_STR IN VARCHAR2) RETURN VARCHAR2 IS
    VS_RET VARCHAR2(32);
  BEGIN
    IF IS_STR IS NULL THEN
      RETURN NULL;
    END IF;

    VS_RET := UTL_RAW.CAST_TO_RAW(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING => IS_STR));

    RETURN VS_RET;
  END;

  FUNCTION MD5_CHECKSUM(IS_STR IN CLOB) RETURN VARCHAR2 IS
    VS_RET     VARCHAR2(32);
    VS_MD5_STR VARCHAR2(30000);
    VS_TEMP    VARCHAR2(30000);
  BEGIN
    FOR I IN 0 .. FLOOR(DBMS_LOB.GETLENGTH(IS_STR) / 8000) LOOP
      VS_TEMP := DBMS_LOB.SUBSTR(IS_STR, 8000, I * 8000 + 1);

      VS_MD5_STR := VS_MD5_STR || MD5_CHECKSUM(VS_TEMP);
    END LOOP;

    VS_RET := MD5_CHECKSUM(VS_MD5_STR);

    RETURN VS_RET;
  END;

  FUNCTION MD5_CHECKSUM(IG_GEOM IN SDO_GEOMETRY) RETURN VARCHAR2 IS
    VS_RET VARCHAR2(32);
  BEGIN
    VS_RET := MD5_CHECKSUM(SDO_UTIL.TO_WKTGEOMETRY(IG_GEOM));

    RETURN VS_RET;
  END;

  --by kwz->
  FUNCTION STR_REPLACE(BASE_STR IN VARCHAR2,
                       OLD_STR  IN VARCHAR2,
                       NEW_STR  IN VARCHAR2,
                       PEX_STR  IN VARCHAR2) RETURN VARCHAR2 IS
    V_RET VARCHAR2(4000);
  BEGIN
    IF BASE_STR IS NULL OR OLD_STR IS NULL THEN
      --无法替换，直接返回原串
      RETURN BASE_STR;
    END IF;

    IF BASE_STR = OLD_STR THEN
      --完全匹配替换串，直接返回新串
      RETURN NEW_STR;
    END IF;

    IF NEW_STR IS NULL AND PEX_STR IS NULL THEN
      --无分隔符，无新串，删除替换串
      RETURN REPLACE(BASE_STR, OLD_STR);
    END IF;

    IF PEX_STR IS NULL THEN
      --无分隔符，直接替换新串
      RETURN REPLACE(BASE_STR, OLD_STR, NEW_STR);
    END IF;

    IF NEW_STR IS NULL THEN
      --有分隔符，无新串，替换串和原串补齐分隔符，新串为分隔符，替换
      V_RET := REPLACE(PEX_STR || BASE_STR || PEX_STR,
                       PEX_STR || OLD_STR || PEX_STR,
                       PEX_STR);
    ELSE
      --有分隔符，有新串，替换串、原串和新串补齐分隔符，替换
      V_RET := REPLACE(PEX_STR || BASE_STR || PEX_STR,
                       PEX_STR || OLD_STR || PEX_STR,
                       PEX_STR || NEW_STR || PEX_STR);
    END IF;

    --替换完后去除首尾分隔符
    RETURN SUBSTR(V_RET,
                  LENGTH(PEX_STR) + 1,
                  LENGTH(V_RET) - 2 * LENGTH(PEX_STR));
  END;

  FUNCTION TAB_REPLACE(CUR IN REPLACE_CURSOR) RETURN REPLACE_TAB
    PIPELINED ORDER CUR BY(BASE_STR)
    PARALLEL_ENABLE(PARTITION CUR BY HASH(BASE_STR)) IS
    VR_IN  S_REPLACE_IN;
    VR_OUT S_REPLACE_OUT;
  BEGIN
    IF CUR IS NULL THEN
      RETURN;
    END IF;

    VR_OUT.BASE_STR := NULL;
    LOOP
      FETCH CUR
        INTO VR_IN;
      EXIT WHEN CUR%NOTFOUND;
      IF VR_OUT.BASE_STR IS NOT NULL AND VR_OUT.BASE_STR <> VR_IN.BASE_STR THEN
        PIPE ROW(VR_OUT);
      END IF;
      IF VR_OUT.BASE_STR IS NULL OR VR_OUT.BASE_STR <> VR_IN.BASE_STR THEN
        VR_OUT.PID       := VR_IN.PID;
        VR_OUT.BASE_STR  := VR_IN.BASE_STR;
        VR_OUT.AFTER_STR := VR_IN.BASE_STR;
      END IF;
      VR_OUT.AFTER_STR := STR_REPLACE(VR_OUT.AFTER_STR,
                                      VR_IN.OLD_STR,
                                      VR_IN.NEW_STR,
                                      VR_IN.PEX_STR);
    END LOOP;
    CLOSE CUR;

    PIPE ROW(VR_OUT);

    RETURN;
  END;
  --<-by kwz

  FUNCTION GET_BRACKETS_CONTENT(IN_STR        IN VARCHAR2,
                                I_POS         IN NUMBER,
                                LEFT_BRACKET  IN VARCHAR2 DEFAULT '(',
                                RIGHT_BRACKET IN VARCHAR2 DEFAULT ')')
    RETURN VARCHAR2 IS

    VN_SUBNAME VARCHAR2(1);
    START_POS  NUMBER;
    END_POS    NUMBER;
    START_FLAG NUMBER;
    VN_FLAG    NUMBER;
    OUT_STR    VARCHAR2(4000);

  BEGIN
    VN_SUBNAME := NULL;
    START_POS  := 0;
    END_POS    := 0;
    START_FLAG := 0;
    VN_FLAG    := 0;
    OUT_STR    := NULL;
    IF (I_POS > 0) THEN
      FOR T IN 1 .. LENGTH(IN_STR) LOOP
        VN_SUBNAME := SUBSTR(IN_STR, T, 1);
        IF VN_SUBNAME = LEFT_BRACKET THEN
          VN_FLAG := VN_FLAG + 1;
          IF (VN_FLAG = I_POS) THEN
            START_FLAG := START_FLAG + 1;
            START_POS  := T;
          ELSIF (VN_FLAG > I_POS) THEN
            START_FLAG := START_FLAG + 1;
          END IF;
        ELSIF VN_SUBNAME = RIGHT_BRACKET THEN
          IF START_FLAG > 0 THEN
            START_FLAG := START_FLAG - 1;
            IF (START_FLAG = 0) THEN
              END_POS := T;
              EXIT;
            END IF;
          END IF;
        END IF;
      END LOOP;
    ELSIF (I_POS < 0) THEN
      FOR T IN 1 .. LENGTH(IN_STR) LOOP
        VN_SUBNAME := SUBSTR(IN_STR, -T, 1);
        IF VN_SUBNAME = RIGHT_BRACKET THEN
          VN_FLAG := VN_FLAG + 1;
          IF (VN_FLAG = ABS(I_POS)) THEN
            START_FLAG := START_FLAG + 1;
            END_POS    := LENGTH(IN_STR) - T + 1;
          ELSIF (VN_FLAG > ABS(I_POS)) THEN
            START_FLAG := START_FLAG + 1;
          END IF;
        ELSIF VN_SUBNAME = LEFT_BRACKET THEN
          IF START_FLAG > 0 THEN
            START_FLAG := START_FLAG - 1;
            IF (START_FLAG = 0) THEN
              START_POS := LENGTH(IN_STR) - T + 1;
              EXIT;
            END IF;
          END IF;
        END IF;
      END LOOP;

    END IF;
    IF (START_POS <> 0 AND END_POS <> 0 AND START_POS < END_POS) THEN
      OUT_STR := SUBSTR(IN_STR, START_POS + 1, END_POS - START_POS - 1);
    END IF;
    RETURN OUT_STR;

  END GET_BRACKETS_CONTENT;

  FUNCTION SPLITSTRING_TAB(CUR   IN IN_STRING_CURSOR,
                           P_DLT IN VARCHAR2 DEFAULT ',')
    RETURN OUT_STRING_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION CUR BY ANY) IS
    V_IN_STRING  IN_STRING;
    V_OUT_STRING OUT_STRING;
    VARR         STRINGARRAY;
  BEGIN
    IF CUR IS NULL THEN
      RETURN;
    END IF;
    LOOP
      FETCH CUR
        INTO V_IN_STRING;
      EXIT WHEN CUR%NOTFOUND;

      V_OUT_STRING.OUT_ROWID := V_IN_STRING.IN_ROWID;

      VARR                 := SPLITSTRING(V_IN_STRING.IN_STR, P_DLT);
      V_OUT_STRING.OUT_SEQ := 1;

      FOR IDX IN VARR.FIRST .. VARR.LAST LOOP
        V_OUT_STRING.OUT_STR := VARR(IDX);
        PIPE ROW(V_OUT_STRING);
        V_OUT_STRING.OUT_SEQ := V_OUT_STRING.OUT_SEQ + 1;
      END LOOP;

    END LOOP;
    CLOSE CUR;
  END;

END COMMON_UTIL;
/

CREATE OR REPLACE PACKAGE PY_UTILS AUTHID CURRENT_USER IS

  -- Author  : TONGLEI
  -- Created : 2011-5-6 17:36:29
  -- Purpose :

  V_FWIDTH VARCHAR2(1000);
  V_HWIDTH VARCHAR2(1000);

  TYPE STRINGARRAY IS TABLE OF VARCHAR2(100);

  -- 将字符串中的全角字符转换为半角字符
  FUNCTION CONVERT_FULL2HALF_WIDTH(P_STR VARCHAR2) RETURN VARCHAR2;

  -- 将字符串中的半角字符转换为全角字符
  FUNCTION CONVERT_HALF2FULL_WIDTH(P_STR VARCHAR2) RETURN VARCHAR2;

  --将B_KEYWORD表的KIND进行拆分 插入到B_KEYWORD_KIND表
  PROCEDURE SPLIT_KIND;

  --从B_FULL_HALF_WIDTH表中取得所有的全角和半角字符
  PROCEDURE GET_FULL_HALF_STR(P_FWIDTH IN OUT VARCHAR2,
                              P_HWIDTH IN OUT VARCHAR2);

  /*
  * 全角字符转半角
  * 多线程，将全半角的对照字符串存在全局变量中，可以提高效率
  * 如果是单个转换，建议使用【convert_full2half_width】
  */
  FUNCTION CONVERT_FULL2HALF_MULTITHREAD(P_STR VARCHAR2) RETURN VARCHAR2;

  /*
  * 半角字符转全角
  * 多线程，将全半角的对照字符串存在全局变量中，可以提高效率
  * 如果是单个转换，建议使用【convert_half2full_width】
  */
  FUNCTION CONVERT_HALF2FULL_MULTITHREAD(P_STR VARCHAR2) RETURN VARCHAR2;

END PY_UTILS;
/

CREATE OR REPLACE PACKAGE BODY PY_UTILS IS

  FUNCTION CONVERT_FULL2HALF_WIDTH(P_STR VARCHAR2) RETURN VARCHAR2 IS
    V_RESULT VARCHAR2(255) := '';
    CURSOR HALF_C(P_CHAR VARCHAR2) IS
      SELECT TRANSLATE(P_CHAR, FWIDTH, HWIDTH)
        FROM (SELECT REPLACE(MAX(SUBSTR(SYS_CONNECT_BY_PATH(HALF_WIDTH, '＠'),
                                        2)),
                             '＠') HWIDTH,
                     REPLACE(MAX(SUBSTR(SYS_CONNECT_BY_PATH(FULL_WIDTH, '@'),
                                        2)),
                             '@') FWIDTH
                FROM (SELECT T.HALF_WIDTH, T.FULL_WIDTH, ROWNUM RN
                        FROM TY_CHARACTER_FULL2HALF T
                       WHERE T.FULL_WIDTH IS NOT NULL
                         AND T.HALF_WIDTH IS NOT NULL)
               START WITH RN = 1
              CONNECT BY RN = ROWNUM) T;
  BEGIN
    OPEN HALF_C(P_STR);
    FETCH HALF_C
      INTO V_RESULT;
    CLOSE HALF_C;

    RETURN V_RESULT;
  END CONVERT_FULL2HALF_WIDTH;

  FUNCTION CONVERT_HALF2FULL_WIDTH(P_STR VARCHAR2) RETURN VARCHAR2 IS
    V_RESULT VARCHAR2(255) := '';
    CURSOR HALF_C(P_CHAR VARCHAR2) IS
      SELECT TRANSLATE(P_CHAR, HWIDTH, FWIDTH)
        FROM (SELECT REPLACE(MAX(SUBSTR(SYS_CONNECT_BY_PATH(HALF_WIDTH, '＠'),
                                        2)),
                             '＠') HWIDTH,
                     REPLACE(MAX(SUBSTR(SYS_CONNECT_BY_PATH(FULL_WIDTH, '@'),
                                        2)),
                             '@') FWIDTH
                FROM (SELECT T.HALF_WIDTH, T.FULL_WIDTH, ROWNUM RN
                        FROM TY_CHARACTER_FULL2HALF T
                       WHERE T.FULL_WIDTH IS NOT NULL
                         AND T.HALF_WIDTH IS NOT NULL)
               START WITH RN = 1
              CONNECT BY RN = ROWNUM) T;
  BEGIN
    OPEN HALF_C(P_STR);
    FETCH HALF_C
      INTO V_RESULT;
    CLOSE HALF_C;

    RETURN V_RESULT;
  END CONVERT_HALF2FULL_WIDTH;

  PROCEDURE GET_FULL_HALF_STR(P_FWIDTH IN OUT VARCHAR2,
                              P_HWIDTH IN OUT VARCHAR2) IS
    CURSOR C_FULL_HALF IS
      SELECT REPLACE(MAX(SUBSTR(SYS_CONNECT_BY_PATH(HALF_WIDTH, '＠'), 2)),
                     '＠') HWIDTH,
             REPLACE(MAX(SUBSTR(SYS_CONNECT_BY_PATH(FULL_WIDTH, '@'), 2)),
                     '@') FWIDTH
        FROM (SELECT T.HALF_WIDTH, T.FULL_WIDTH, ROWNUM RN
                FROM TY_CHARACTER_FULL2HALF T
               WHERE T.FULL_WIDTH IS NOT NULL
                 AND T.HALF_WIDTH IS NOT NULL)
       START WITH RN = 1
      CONNECT BY RN = ROWNUM;
  BEGIN
    OPEN C_FULL_HALF;
    FETCH C_FULL_HALF
      INTO P_HWIDTH, P_FWIDTH;
    CLOSE C_FULL_HALF;
  END;

  /*
  * 全角字符转半角
  * 多线程，将全半角的对照字符串存在全局变量中，可以提高效率
  * 如果是单个转换，建议使用【convert_full2half_width】
  */
  FUNCTION CONVERT_FULL2HALF_MULTITHREAD(P_STR VARCHAR2) RETURN VARCHAR2 IS
    V_RESULT VARCHAR2(255) := '';
  BEGIN
    IF V_FWIDTH IS NULL OR V_HWIDTH IS NULL THEN
      PY_UTILS.GET_FULL_HALF_STR(V_FWIDTH, V_HWIDTH);
    END IF;

    V_RESULT := TRANSLATE(P_STR, V_FWIDTH, V_HWIDTH);
    RETURN V_RESULT;
  END;

  /*
  * 半角字符转全角
  * 多线程，将全半角的对照字符串存在全局变量中，可以提高效率
  * 如果是单个转换，建议使用【convert_half2full_width】
  */
  FUNCTION CONVERT_HALF2FULL_MULTITHREAD(P_STR VARCHAR2) RETURN VARCHAR2 IS
    V_RESULT VARCHAR2(255) := '';
  BEGIN
    IF V_FWIDTH IS NULL OR V_HWIDTH IS NULL THEN
      PY_UTILS.GET_FULL_HALF_STR(V_FWIDTH, V_HWIDTH);
    END IF;

    V_RESULT := TRANSLATE(P_STR, V_HWIDTH, V_FWIDTH);
    RETURN V_RESULT;
  END;

  PROCEDURE SPLIT_KIND IS
    CURSOR C_CUR IS
      SELECT T.ID, T.KIND FROM TY_NAVICOVPY_KEYWORD T;

    V_KIND VARCHAR2(2000);
    V_ID   VARCHAR2(20);
    V_ARR  COMMON_UTIL.STRINGARRAY;
  BEGIN
    DELETE FROM TY_NAVICOVPY_KEYWORD_KIND;
    COMMIT;

    OPEN C_CUR;
    FETCH C_CUR
      INTO V_ID, V_KIND;
    LOOP
      EXIT WHEN C_CUR%NOTFOUND;
      IF V_KIND IS NOT NULL THEN
        V_ARR := COMMON_UTIL.SPLITSTRING(V_KIND, '|');

        FOR I IN 1 .. V_ARR.COUNT LOOP
          INSERT INTO TY_NAVICOVPY_KEYWORD_KIND
            (KEYWORD_ID, KIND)
          VALUES
            (V_ID, V_ARR(I));
        END LOOP;
      END IF;

      FETCH C_CUR
        INTO V_ID, V_KIND;
    END LOOP;

    COMMIT;
    CLOSE C_CUR;
  END;

END PY_UTILS;
/


CREATE OR REPLACE PACKAGE PY_UTILS_WORD AUTHID CURRENT_USER IS
  -- 在转换出现错误时是否返回详细错误信息,
  --0: 只返回错误编码，1：返回完整的错误信息
  C_LOG_DETAIL_YN NUMBER := 1;

  -- 是否对特殊字符进行过滤
  --0：不过滤；1，过滤。
  C_FILTER_SPECIAL_CHARACTERS NUMBER := 0;

  -- 设置是否转换数字。example：1 --> yi, 0 --> ling.
  -- 0：不转换；1：转换； default：0
  C_CONVERT_NUMBER NUMBER := 0;

  -- 设置是否转换罗马数字
  -- 0：不转换；1：转换； default：0
  C_CONVERT_ROM_NUMBER NUMBER := 0;

  --设置汉字的拼音是否首字母大写
  -- 0：不大写；1：大写
  C_PY_FIRST_CHAR_UPPER NUMBER := 1;

  -- 设置如果数字转拼音，首字母是否大写
  -- 0：不大写；1：大写
  C_NUMBER_FIRST_CHAR_UPPER NUMBER := 1;

  --转换后的拼音的音调格式
  --0：无音调；1：有音调，音调直接标在拼音上；2：有音调，音调为数字，紧跟拼音后，无空格
  C_FORMAT_TONE NUMBER := 0;

  ---参考关键词标识
  ---0.不参考；1.参考
  C_REF_KEYWORD_FLAG NUMBER := 1;

  C_SPECIAL_CHARACTERS       VARCHAR2(255) := '[·￣．！＠＃￥％＾＆＊＿－＋｜、：“＜＞，。？｛｝《［］—／（）＝＄‘’＇～”；＂!"#$%&()*+,-./:;<=>?@\^_`{|}~'']';
  PRIVATE_ROM_CHARACTERS     VARCHAR2(255) := '[ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹ＇－＾～＂“”‘’-]';
  PRIVATE_SPECIAL_CHARACTERS VARCHAR2(255) := '[·￣．！＠＃￥％＾＆＊＿－＋｜、：“＜＞，。？｛｝《［］—／（）＝＄‘’＇～”；＂!"#$%&()*+,-./:;<=>?@\^_`{|}~''ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹ]';

  /**
    名称： convert_kw_by_hz
    功能： 根据汉字和拼音，将拼音转换为有音调的格式
    返回值：转换后的拼音
  */
  FUNCTION CONVERT_PY_TONE(P_HZ        IN VARCHAR2,
                           P_PY        IN VARCHAR2 DEFAULT '',
                           P_ADMINCODE IN VARCHAR2 DEFAULT '',
                           P_CONVROAD  IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2 /* DETERMINISTIC RESULT_CACHE*/
    PARALLEL_ENABLE;

  /**
    名称： convert_hz_tone
    功能： 根据汉字，转换出对应的有音调的拼音
    返回值：转换后的拼音
  */
  FUNCTION CONVERT_HZ_TONE(P_HZ        IN VARCHAR2,
                           P_ADMINCODE IN VARCHAR2 DEFAULT '',
                           P_CONVROAD  IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE;

  /**
    名称： convert_road_name
    功能： 根据道路名汉字和拼音，对道路名按照相应的规则进行转换
    返回值：划分后的拼音
  */
  PROCEDURE CONVERT_ROAD_NAME(P_HZ IN OUT NOCOPY VARCHAR2,
                              P_PY IN OUT NOCOPY VARCHAR2);

  /**
    名称： convert_none_tone_py
    功能： 将有音调的拼音转换为无音调的拼音
    返回值：转换后的无音调拼音
  */
  FUNCTION CONVERT_NONE_TONE_PY(P_PY IN VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE;

  TYPE M_RESULT IS RECORD(
    PID       VARCHAR2(4000),
    PY_RESULT VARCHAR2(500));
  TYPE TYPEREFCURSOR IS REF CURSOR;
  TYPE T_RESULT_TAB IS TABLE OF M_RESULT;

  FUNCTION CONVERT_PY_TONE_PIPELINED(IN_CURSOR IN TYPEREFCURSOR)
    RETURN T_RESULT_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION IN_CURSOR BY ANY);

  FUNCTION CONVERT_HZ_TONE_PIPELINED(IN_CURSOR IN TYPEREFCURSOR)
    RETURN T_RESULT_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION IN_CURSOR BY ANY);

  TYPE SHORT_CHAR_TAB_TYPE IS TABLE OF VARCHAR2(16) INDEX BY BINARY_INTEGER;
  TYPE LONG_CHAR_TAB_TYPE IS TABLE OF VARCHAR2(4000) INDEX BY BINARY_INTEGER;
  TYPE CHAR_TAB_TYPE IS TABLE OF VARCHAR2(16);

  TYPE T_COMMON_PARAM IS RECORD(
    V_MW_MAP        MW_ARRAY, ---分词拼音信息表
    V_HZ_T          SHORT_CHAR_TAB_TYPE, --存放汉字的数组
    V_PY_STR_T      LONG_CHAR_TAB_TYPE, --存放所有拼音组合的数组，每个元素是一个完整拼音串
    V_DEF_PY_T      SHORT_CHAR_TAB_TYPE, --存放默认拼音的数组，每个元素是一个字符的拼音
    V_SPECIAL_POS_T SHORT_CHAR_TAB_TYPE, --存放特殊字符的位置
    V_SPECIAL_T     SHORT_CHAR_TAB_TYPE, --存放特殊字符

    -- 对应V_HZ_T中的每个字，存放对应的字符类型
    -- A-汉字 | B：英文单词或字母 | C：数字 | CC：数字拼音 | D：特殊字符 | E：空格
    V_TYPE_T SHORT_CHAR_TAB_TYPE,

    -- 由于转简拼的时候，传入的拼音是进行关键词转换后的拼音，因此将其中的分隔符位置记录下来
    V_SPLIT_CHAR_T SHORT_CHAR_TAB_TYPE, --存放分隔字符的位置

    V_NUM_FORMAT NUMBER(1) --当数字过多会内存溢出，需减少多音字数量
    );

  --为空格分隔的拼音增加分隔符，返回一个带分隔符的连续无空格的拼音
  FUNCTION ADD_CONFUSE_MARK(P_SPACE_SPLITED_PY VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE;

  FUNCTION CONVERT_TO_ENGLISH_MODE(P_HZ        IN VARCHAR2,
                                   P_ADMINCODE IN VARCHAR2 DEFAULT '',
                                   P_CONVROAD  IN VARCHAR2 DEFAULT '',
                                   P_PY        IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE;

  FUNCTION CONV_TO_ENGLISH_MODE_VOICEFILE(P_HZ        IN VARCHAR2,
                                          P_ADMINCODE IN VARCHAR2 DEFAULT '',
                                          P_CONVROAD  IN VARCHAR2 DEFAULT '',
                                          P_PY        IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE;

  /*
  * 生成道路名语音
  * P_CONVTYPE 预留， 暂未使用          1：道路名；2：分歧
  */
  FUNCTION CONVERT_RD_NAME_VOICE(P_HZ       IN VARCHAR2,
                                 P_PY       IN VARCHAR2,
                                 P_ADMIN    IN VARCHAR2,
                                 P_CONVTYPE IN VARCHAR2 DEFAULT '1')
    RETURN VARCHAR2
    PARALLEL_ENABLE;

  /*
  * 生成分歧语音
  * P_CONVTYPE 预留， 暂未使用          1：道路名；2：分歧
  */
  FUNCTION CONVERT_RRANCH_NAME_VOICE(P_HZ       IN VARCHAR2,
                                     P_PY       IN VARCHAR2,
                                     P_ADMIN    IN VARCHAR2,
                                     P_CONVTYPE IN VARCHAR2 DEFAULT '1')
    RETURN VARCHAR2
    PARALLEL_ENABLE;

  TYPE PY_CONV_REC IS RECORD(
    PID         VARCHAR2(4000),
    HZ          VARCHAR2(4000), --汉字串
    PY_SRC      VARCHAR2(4000),
    PY          VARCHAR2(4000), --无音调拼音串
    PY_TONE_STR VARCHAR2(4000), --带音调拼音串（包含多音字的所有拼音和读音）
    PY_TONE_REF VARCHAR2(4000), --建议的拼音串（带音调）
    MULTIPY_NUM NUMBER(3), --多音字个数
    --多音类型：
    --1.即不含多音字也不含多调字；2.包含多音字不含多调字；3.不含多音字包含多调字；4.即包含多音字也包含多调字
    PY_TONE_KIND NUMBER(1),

    ERR_FLAG NUMBER(1), --错误标识：0.无错；1.出错
    ERR_MSG  VARCHAR2(255) --错误提示信息
    );

  TYPE PY_CONV_REST_TAB IS TABLE OF PY_CONV_REC;

  FUNCTION CONVERT_HZ_PY_TONE(IS_HZ_STR    IN VARCHAR2,
                              IS_PY_STR    IN VARCHAR2,
                              IS_ADMINCODE VARCHAR2 DEFAULT NULL)
    RETURN PY_CONV_REC;

  FUNCTION CONVERT_HZ_PY_TONE_PIPELINED(IC_CURSOR IN TYPEREFCURSOR)
    RETURN PY_CONV_REST_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION IC_CURSOR BY ANY);

  PROCEDURE SET_CONTEXT_PARAM(IS_PARAM_NAME  IN VARCHAR2,
                              IS_PARAM_VALUE IN VARCHAR2);

  PROCEDURE CLEAR_CONTEXT_PARAM;

  PROCEDURE INIT_CONTEXT_PARAM;

  FUNCTION CONVERT2NUM(P_SOURCE VARCHAR2) RETURN NUMBER
    PARALLEL_ENABLE;

  FUNCTION CHAR_IS_NUMBER(P_SOURCE VARCHAR2, FLAG NUMBER DEFAULT 0)
    RETURN BOOLEAN
    PARALLEL_ENABLE;

  FUNCTION STR_IS_NUMBER(P_SOURCE VARCHAR2) RETURN BOOLEAN
    PARALLEL_ENABLE;
  FUNCTION SPLIT_NAME(P_SOURCE IN VARCHAR2,
                      P_FIRST  OUT VARCHAR2,
                      P_MID    OUT VARCHAR2,
                      P_LAST   OUT VARCHAR2) RETURN BOOLEAN
    PARALLEL_ENABLE;

  FUNCTION CONVERTNUMBSTR2NUM(P_SOURCE VARCHAR2) RETURN NUMBER
    PARALLEL_ENABLE;

  FUNCTION CONVERT_BASE_ENG(P_BASENAME VARCHAR2,
                            P_BASEPY   VARCHAR2,
                            P_TYPE     VARCHAR2,
                            P_LANGCODE VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE;

  FUNCTION CONVERT_ENG(P_BASENAME VARCHAR2, P_BASEPY VARCHAR2)
    RETURN VARCHAR2
    PARALLEL_ENABLE;

  FUNCTION GETENG_NAME_PY(P_NAME VARCHAR2, P_PY VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE;

  FUNCTION GETENG_NAME_PY_REV(P_NAME VARCHAR2, P_PY VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE;

END PY_UTILS_WORD;
/

CREATE OR REPLACE PACKAGE BODY PY_UTILS_WORD AS

  -- 当给定的汉子与拼音不匹配时触发
  E_HZ_PY_NOT_MATCHING EXCEPTION;

  -- 如果给定的不是汉字或者没有在数据库中检索到，
  E_NO_HZ_FOUND EXCEPTION;

  --判断给定字符是否是半角数字/半角字母/特殊字符
  FUNCTION IS_SPECIAL_CHARACTER(P_CHAR      CHAR,
                                P_TYPE      NUMBER,
                                P_CHECK_STR VARCHAR2) RETURN BOOLEAN IS
    V_RESULT         BOOLEAN := FALSE;
    V_COMPARE_REGEXP VARCHAR2(20) := '';
  BEGIN
    IF P_CHAR IS NULL THEN
      RETURN FALSE;
    END IF;

    IF P_TYPE = 0 THEN
      V_COMPARE_REGEXP := '^[0-9]*$';
    ELSIF P_TYPE = 1 THEN
      V_COMPARE_REGEXP := '^[A-Za-z]*$';
    ELSE
      V_COMPARE_REGEXP := '';
    END IF;

    IF REGEXP_LIKE(P_CHAR, P_CHECK_STR) OR
       REGEXP_LIKE(P_CHAR, V_COMPARE_REGEXP) THEN
      V_RESULT := TRUE;
    END IF;

    RETURN V_RESULT;
  END IS_SPECIAL_CHARACTER;

  ---将连续的空格替换为单空格,并去除首尾空格
  FUNCTION CLEAR_MULTISPACE(P_HZ IN VARCHAR2) RETURN VARCHAR2 IS
    V_TEMP VARCHAR(4000);
  BEGIN
    V_TEMP := P_HZ;
    WHILE INSTR(V_TEMP, '  ') > 0 LOOP
      V_TEMP := REPLACE(V_TEMP, '  ', ' ');
    END LOOP;

    RETURN TRIM(V_TEMP);
  END CLEAR_MULTISPACE;

  --硬编码
  --由于目前的DB数据中，数字的拼音不统一，
  --如关键词Ｘ７１１，对应的拼音为X QI YI YI，而７１１对应的拼音为711
  --导致有的关键词无法正确被检索到，因此将数字也作为多音字处理
  FUNCTION GET_PINYIN_BY_DIGIT(P_HZ     IN VARCHAR2,
                               P_FORMAT IN NUMBER DEFAULT 0)
    RETURN CHAR_TAB_TYPE IS
    V_TABLE_T CHAR_TAB_TYPE;
  BEGIN
    CASE P_HZ
      WHEN '0' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('0');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('ling');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('ling', '0');
        END IF;
      WHEN '1' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('1');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('yi');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('yi', '1');
        END IF;
      WHEN '2' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('2');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('er');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('er', '2');
        END IF;
      WHEN '3' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('3');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('san');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('san', '3');
        END IF;
      WHEN '4' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('4');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('si');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('si', '4');
        END IF;
      WHEN '5' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('5');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('wu');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('wu', '5');
        END IF;
      WHEN '6' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('6');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('liu');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('liu', '6');
        END IF;
      WHEN '7' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('7');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('qi');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('qi', '7');
        END IF;
      WHEN '8' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('8');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('ba');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('ba', '8');
        END IF;
      WHEN '9' THEN
        IF P_FORMAT = 1 THEN
          V_TABLE_T := CHAR_TAB_TYPE('9');
        ELSIF P_FORMAT = 2 THEN
          V_TABLE_T := CHAR_TAB_TYPE('jiu');
        ELSE
          V_TABLE_T := CHAR_TAB_TYPE('jiu', '9');
        END IF;
      ELSE
        V_TABLE_T := CHAR_TAB_TYPE('');
    END CASE;

    RETURN V_TABLE_T;
  END GET_PINYIN_BY_DIGIT;

  --单个数字转拼音
  FUNCTION CONVERTNUM(P_HZ IN VARCHAR2) RETURN VARCHAR2 IS
    V_PY VARCHAR2(4000) := '';
  BEGIN
    IF C_FORMAT_TONE = 0 THEN
      SELECT CASE P_HZ
               WHEN '0' THEN
                ' ling '
               WHEN '1' THEN
                ' yi '
               WHEN '2' THEN
                ' er '
               WHEN '3' THEN
                ' san '
               WHEN '4' THEN
                ' si '
               WHEN '5' THEN
                ' wu '
               WHEN '6' THEN
                ' liu '
               WHEN '7' THEN
                ' qi '
               WHEN '8' THEN
                ' ba '
               WHEN '9' THEN
                ' jiu '
               ELSE
                P_HZ
             END
        INTO V_PY
        FROM DUAL;
    ELSIF C_FORMAT_TONE = 1 THEN
      SELECT CASE P_HZ
               WHEN '0' THEN
                ' líng '
               WHEN '1' THEN
                ' yī '
               WHEN '2' THEN
                ' èr '
               WHEN '3' THEN
                ' sān '
               WHEN '4' THEN
                ' sì '
               WHEN '5' THEN
                ' wǔ '
               WHEN '6' THEN
                ' liù '
               WHEN '7' THEN
                ' qī '
               WHEN '8' THEN
                ' bā '
               WHEN '9' THEN
                ' jiǔ '
               ELSE
                P_HZ
             END
        INTO V_PY
        FROM DUAL;
    ELSE
      SELECT CASE P_HZ
               WHEN '0' THEN
                ' ling2 '
               WHEN '1' THEN
                ' yi1 '
               WHEN '2' THEN
                ' er4 '
               WHEN '3' THEN
                ' san1 '
               WHEN '4' THEN
                ' si4 '
               WHEN '5' THEN
                ' wu3 '
               WHEN '6' THEN
                ' liu4 '
               WHEN '7' THEN
                ' qi1 '
               WHEN '8' THEN
                ' ba1 '
               WHEN '9' THEN
                ' jiu3 '
               ELSE
                P_HZ
             END
        INTO V_PY
        FROM DUAL;
    END IF;

    RETURN V_PY;
  END CONVERTNUM;

  --批量数字转拼音
  FUNCTION CONVERTMULTINUM(P_WORD VARCHAR2) RETURN VARCHAR2 IS
    V_TEMP VARCHAR2(4000);
    V_WORD VARCHAR2(4000);
  BEGIN
    V_TEMP := P_WORD;
    V_WORD := '';

    FOR I IN 1 .. LENGTH(V_TEMP) LOOP
      V_WORD := V_WORD || CONVERTNUM(SUBSTR(V_TEMP, I, 1));
    END LOOP;

    RETURN V_WORD;
  END CONVERTMULTINUM;

  ---转换罗马字符
  FUNCTION CONVERTROMNUM(P_HZ IN VARCHAR2, P_WRAPSPACE IN NUMBER DEFAULT 0)
    RETURN VARCHAR2 IS
    V_PY VARCHAR2(4000) := '';
  BEGIN
    IF C_CONVERT_ROM_NUMBER = 1 THEN
      SELECT CASE P_HZ
               WHEN 'Ⅰ' THEN
                'I'
               WHEN 'Ⅱ' THEN
                'II'
               WHEN 'Ⅲ' THEN
                'III'
               WHEN 'Ⅳ' THEN
                'IV'
               WHEN 'Ⅴ' THEN
                'V'
               WHEN 'Ⅵ' THEN
                'VI'
               WHEN 'Ⅶ' THEN
                'VII'
               WHEN 'Ⅷ' THEN
                'VIII'
               WHEN 'Ⅸ' THEN
                'IX'
               WHEN 'Ⅹ' THEN
                'X'
               WHEN 'Ⅺ' THEN
                'XI'
               WHEN 'Ⅻ' THEN
                'XII'
               WHEN 'ⅰ' THEN
                'I'
               WHEN 'ⅱ' THEN
                'II'
               WHEN 'ⅲ' THEN
                'III'
               WHEN 'ⅳ' THEN
                'IV'
               WHEN 'ⅴ' THEN
                'V'
               WHEN 'ⅵ' THEN
                'VI'
               WHEN 'ⅶ' THEN
                'VII'
               WHEN 'ⅷ' THEN
                'VIII'
               WHEN 'ⅸ' THEN
                'IX'
               WHEN 'ⅹ' THEN
                'X'
               WHEN '＇' THEN
                ''''
               WHEN '－' THEN
                '-'
               WHEN '＾' THEN
                '^'
               WHEN '～' THEN
                '~'
               WHEN '＂' THEN
                '"'

               WHEN '“' THEN
                '"'
               WHEN '”' THEN
                '"'
               WHEN '‘' THEN
                ''''
               WHEN '’' THEN
                ''''
               ELSE
                P_HZ
             END
        INTO V_PY
        FROM DUAL;
    ELSE
      V_PY := P_HZ;
    END IF;

    IF P_WRAPSPACE = 1 AND V_PY <> P_HZ THEN
      V_PY := '　' || V_PY || '　';
    END IF;
    RETURN V_PY;
  END CONVERTROMNUM;

  --将带音标的拼音转换为不带音标的拼音
  FUNCTION CONVERT_NONE_TONE_PY(P_PY IN VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_PY VARCHAR2(4000) := P_PY;
  BEGIN
    V_PY := TRANSLATE(V_PY,
                      'āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜü',
                      'aaaaeeeeiiiioooouuuuvvvvv');

    RETURN V_PY;
  END CONVERT_NONE_TONE_PY;

  ---获取纯净的拼音串：不包含特殊字符、全/半角空格以及音调字符的拼音串
  FUNCTION GET_PURE_PY_STR(IS_PY_STR VARCHAR2) RETURN VARCHAR2 IS
    VS_PY_STR VARCHAR2(4000) := IS_PY_STR;
  BEGIN
    IF VS_PY_STR IS NULL THEN
      RETURN NULL;
    END IF;

    --去除特殊字符
    VS_PY_STR := REGEXP_REPLACE(VS_PY_STR, PRIVATE_SPECIAL_CHARACTERS, '');

    --去除全角空格
    VS_PY_STR := REPLACE(VS_PY_STR, '　', '');

    --去除半角空格
    VS_PY_STR := REPLACE(VS_PY_STR, ' ', '');

    --替换音调字母
    VS_PY_STR := CONVERT_NONE_TONE_PY(VS_PY_STR);

    RETURN VS_PY_STR;
  END;

  /*
  初始化汉字转拼音对象，主要有以下功能：
  1.生成字符串中每个字符的类型：A.汉字；B.英文字母；C.数字；D.特殊字符；E.空格
  2.生成字符串的所有无音调拼音组合
  3.生成字符串中特殊字符信息
  */
  PROCEDURE INIT_PY_LIST(P_PARAM   IN OUT NOCOPY T_COMMON_PARAM,
                         IS_HZ_STR IN VARCHAR2,
                         IN_TYPE   IN NUMBER DEFAULT 0) IS
    VT_PY_TABLE    CHAR_TAB_TYPE; -- 临时存放汉字拼音的列表
    VS_TEMP        VARCHAR2(3);
    VS_HZ_CPY_STR  VARCHAR2(4000); --存放全角转半角之后的汉字串
    VS_TEMP_PY_STR VARCHAR2(255);
    VN_ASC         NUMBER;
  BEGIN
    IF IS_HZ_STR IS NULL THEN
      RETURN;
    END IF;

    ---全角转半角
    VS_HZ_CPY_STR := PY_UTILS.CONVERT_FULL2HALF_MULTITHREAD(IS_HZ_STR);
    FOR I IN 1 .. LENGTH(IS_HZ_STR) LOOP
      P_PARAM.V_HZ_T(I) := SUBSTR(IS_HZ_STR, I, 1);

      VS_TEMP := SUBSTR(VS_HZ_CPY_STR, I, 1);
      VN_ASC  := ASCII(VS_TEMP);

      IF VS_TEMP = ' ' THEN
        --空格
        VT_PY_TABLE := CHAR_TAB_TYPE(' ');
        P_PARAM.V_TYPE_T(I) := 'E';
      ELSIF REGEXP_LIKE(VS_TEMP, '[0-9]') THEN
        --数字
        VT_PY_TABLE := CHAR_TAB_TYPE(VS_TEMP);

        IF C_CONVERT_NUMBER = 0 THEN
          P_PARAM.V_TYPE_T(I) := 'C';
        ELSE
          P_PARAM.V_TYPE_T(I) := 'CC';
        END IF;
      ELSIF REGEXP_LIKE(VS_TEMP, '[a-zA-Z]') THEN
        --字母
        VT_PY_TABLE := CHAR_TAB_TYPE(VS_TEMP);
        P_PARAM.V_TYPE_T(I) := 'B';
      ELSIF VN_ASC > 32 AND VN_ASC < 48 OR VN_ASC > 57 AND VN_ASC < 65 OR
            VN_ASC > 90 AND VN_ASC < 97 OR VN_ASC > 122 AND VN_ASC <= 126 OR
            REGEXP_LIKE(VS_TEMP, PRIVATE_SPECIAL_CHARACTERS) THEN
        --特殊字符
        VT_PY_TABLE := CHAR_TAB_TYPE(VS_TEMP);
        P_PARAM.V_TYPE_T(I) := 'D';

        ---记录特殊字符信息：位置、字符
        P_PARAM.V_SPECIAL_POS_T(P_PARAM.V_SPECIAL_POS_T.COUNT + 1) := I;
        P_PARAM.V_SPECIAL_T(P_PARAM.V_SPECIAL_T.COUNT + 1) := VS_TEMP;
      ELSE
        --汉字
        SELECT DISTINCT PY BULK COLLECT
          INTO VT_PY_TABLE
          FROM TY_NAVICOVPY_PY
         WHERE JT = VS_TEMP;

        IF NVL(VT_PY_TABLE.COUNT, 0) = 0 THEN  --- modify by zhangjin 20140904  修改对任何不认识的字符转出，避免错误
          VT_PY_TABLE := CHAR_TAB_TYPE(VS_TEMP);
          P_PARAM.V_TYPE_T(I) := 'D';
        ELSE
          P_PARAM.V_TYPE_T(I) := 'A';
        END IF;

      END IF;

      -- 将所有拼音的组合存放在P_PARAM的V_PY_T中
      IF VT_PY_TABLE IS NOT NULL THEN
        IF P_PARAM.V_PY_STR_T.COUNT = 0 THEN
          FOR J IN 1 .. VT_PY_TABLE.COUNT LOOP
            IF IN_TYPE = 0 THEN
              P_PARAM.V_PY_STR_T(P_PARAM.V_PY_STR_T.COUNT + 1) := VT_PY_TABLE(J);
            ELSE
              P_PARAM.V_PY_STR_T(P_PARAM.V_PY_STR_T.COUNT + 1) := SUBSTR(VT_PY_TABLE(J),
                                                                         1,
                                                                         1);
            END IF;
          END LOOP;
        ELSE
          FOR K IN 1 .. P_PARAM.V_PY_STR_T.COUNT LOOP
            VS_TEMP_PY_STR := P_PARAM.V_PY_STR_T(K);

            FOR J IN 1 .. VT_PY_TABLE.COUNT LOOP
              IF J = 1 THEN
                IF IN_TYPE = 0 THEN
                  P_PARAM.V_PY_STR_T(K) := VS_TEMP_PY_STR || ' ' ||
                                           VT_PY_TABLE(J);
                ELSE
                  P_PARAM.V_PY_STR_T(K) := VS_TEMP_PY_STR || ' ' ||
                                           SUBSTR(VT_PY_TABLE(J), 1, 1);
                END IF;
              ELSE
                IF IN_TYPE = 0 THEN
                  P_PARAM.V_PY_STR_T(P_PARAM.V_PY_STR_T.COUNT + 1) := VS_TEMP_PY_STR || ' ' ||
                                                                      VT_PY_TABLE(J);
                ELSE
                  P_PARAM.V_PY_STR_T(P_PARAM.V_PY_STR_T.COUNT + 1) := VS_TEMP_PY_STR || ' ' ||
                                                                      SUBSTR(VT_PY_TABLE(J),
                                                                             1,
                                                                             1);
                END IF;
              END IF;
            END LOOP;
          END LOOP;
        END IF;
      ELSE
        -- 如果找不到对应的拼音，抛出异常
        RAISE E_NO_HZ_FOUND;
      END IF;
    END LOOP;
  END INIT_PY_LIST;

  --拼音组合列表与传入的正确发音进行匹配，查找出正确的拼音组合
  FUNCTION GET_CORRECT_PY(P_PARAM   IN OUT NOCOPY T_COMMON_PARAM,
                          P_RIGHTPY IN VARCHAR2) RETURN VARCHAR2 IS
    V_RESULT      VARCHAR2(4000);
    V_TEMP        VARCHAR2(4000);
    V_TMP_RIGHTPY VARCHAR2(4000) := P_RIGHTPY;
  BEGIN
    V_TMP_RIGHTPY := GET_PURE_PY_STR(V_TMP_RIGHTPY);

    FOR I IN 1 .. P_PARAM.V_PY_STR_T.COUNT LOOP
      V_RESULT := P_PARAM.V_PY_STR_T(I);
      V_TEMP   := GET_PURE_PY_STR(V_RESULT);

      IF LOWER(V_TEMP) = LOWER(V_TMP_RIGHTPY) THEN
        EXIT;
      END IF;
    END LOOP;

    V_RESULT := CLEAR_MULTISPACE(V_RESULT);

    RETURN V_RESULT;
  END GET_CORRECT_PY;

  --根据传入的正确拼音串，初始化拼音转换对象中的默认拼音数组
  PROCEDURE INIT_DEF_PY_T(P_PARAM       IN OUT NOCOPY T_COMMON_PARAM,
                          IS_DEF_PY_STR IN VARCHAR2) IS
    VS_DEF_PY_STR VARCHAR2(4000) := IS_DEF_PY_STR;
  BEGIN
    IF VS_DEF_PY_STR IS NULL THEN
      RETURN;
    END IF;

    -- 根据空格进行拆分，把拼音串拆成数组
    -- BEI JING GONG YUAN -->  BEI, JING, GONG, YUAN 
    FOR I IN 1 .. P_PARAM.V_TYPE_T.COUNT LOOP
      IF P_PARAM.V_TYPE_T(I) = 'A' THEN
        P_PARAM.V_DEF_PY_T(I) := REGEXP_SUBSTR(VS_DEF_PY_STR,
                                               '[a-zA-Zāáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜü]+[0-4]?',
                                               1,
                                               1);
        VS_DEF_PY_STR := REGEXP_REPLACE(VS_DEF_PY_STR,
                                        '[a-zA-Zāáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜü]+[0-4]?',
                                        '',
                                        1,
                                        1);
      ELSIF P_PARAM.V_TYPE_T(I) = 'B' THEN
        P_PARAM.V_DEF_PY_T(I) := REGEXP_SUBSTR(VS_DEF_PY_STR,
                                               '[a-zA-Z]',
                                               1,
                                               1);
        VS_DEF_PY_STR := REGEXP_REPLACE(VS_DEF_PY_STR, '[a-zA-Z]', '', 1, 1);
      ELSIF P_PARAM.V_TYPE_T(I) = 'C' OR P_PARAM.V_TYPE_T(I) = 'CC' THEN
        P_PARAM.V_DEF_PY_T(I) := REGEXP_SUBSTR(VS_DEF_PY_STR, '[0-9]', 1, 1);
        VS_DEF_PY_STR := REGEXP_REPLACE(VS_DEF_PY_STR, '[0-9]', '', 1, 1);
      ELSIF P_PARAM.V_TYPE_T(I) = 'D' THEN
        P_PARAM.V_DEF_PY_T(I) := P_PARAM.V_HZ_T(I);
      ELSIF P_PARAM.V_TYPE_T(I) = 'E' THEN
        P_PARAM.V_DEF_PY_T(I) := ' ';
      END IF;
    END LOOP;
  END INIT_DEF_PY_T;

  ---获取指定位置、长度的字符的拼音
  FUNCTION GET_PY_BY_POS(P_PARAM      IN OUT NOCOPY T_COMMON_PARAM,
                         P_KEYWORDPOS VARCHAR2,
                         P_KEYWORDLEN VARCHAR2) RETURN VARCHAR2 IS
    V_RESULT VARCHAR2(4000) := '';
  BEGIN
    FOR I IN 1 .. P_KEYWORDLEN LOOP
      -- 判断如果是数字或字母，并且前一个字符也是数字或字母，中间不加空格
      IF (P_PARAM.V_TYPE_T(I + P_KEYWORDPOS - 1) = 'B' OR
         P_PARAM.V_TYPE_T(I + P_KEYWORDPOS - 1) LIKE 'C%') AND
         I + P_KEYWORDPOS - 2 > 0 AND
         (P_PARAM.V_TYPE_T(I + P_KEYWORDPOS - 2) = 'B' OR
         P_PARAM.V_TYPE_T(I + P_KEYWORDPOS - 2) LIKE 'C%') THEN
        V_RESULT := V_RESULT || P_PARAM.V_DEF_PY_T(I + P_KEYWORDPOS - 1);
      ELSE
        V_RESULT := V_RESULT || ' ' ||
                    P_PARAM.V_DEF_PY_T(I + P_KEYWORDPOS - 1);
      END IF;
    END LOOP;

    RETURN V_RESULT;
  END GET_PY_BY_POS;

  -- 判断查询得到的拼音是否与给定的拼音匹配
  -- 现有的实现方式不是很理想，更好的方法是定义一些纠错的规则
  FUNCTION IS_KEYWORD_PY_CORRECT(P_PARAM      IN OUT T_COMMON_PARAM,
                                 P_KEYWORDPY  VARCHAR2,
                                 P_KEYWORDPOS INT,
                                 P_KEYWORDLEN INT) RETURN BOOLEAN IS
    V_RESULT     BOOLEAN := FALSE;
    V_KEYWORD_PY VARCHAR2(4000);
    V_TEMP_CHAR  VARCHAR2(10);
    V_KEYWORDPOS INT := P_KEYWORDPOS;
  BEGIN
    IF P_KEYWORDPY IS NULL THEN
      RETURN FALSE;
    END IF;
    /*    FOR I IN 1 .. P_PARAM.V_SPECIAL_POS_T.COUNT LOOP
      IF P_PARAM.V_SPECIAL_POS_T(I) <= P_KEYWORDPOS THEN
        V_KEYWORDPOS := V_KEYWORDPOS + 1;
      END IF;
    END LOOP;*/
    FOR I IN 1 .. P_KEYWORDLEN LOOP
      V_TEMP_CHAR := P_PARAM.V_DEF_PY_T(V_KEYWORDPOS + I - 1);

      V_KEYWORD_PY := V_KEYWORD_PY || ' ' || V_TEMP_CHAR;
    END LOOP;

    V_KEYWORD_PY := REPLACE(V_KEYWORD_PY, '　', '');

    IF UPPER(REPLACE(V_KEYWORD_PY, ' ', '')) =
       UPPER(REPLACE(P_KEYWORDPY, ' ', '')) THEN
      V_RESULT := TRUE;
    END IF;

    RETURN V_RESULT;
  END;

  ---初始化关键词拼音表格
  PROCEDURE INIT_MW_LIST(P_PARAM       IN OUT NOCOPY T_COMMON_PARAM,
                         P_HZ          IN VARCHAR2,
                         P_ADMINCODE_2 IN VARCHAR2,
                         P_ADMINCODE_4 IN VARCHAR2) IS
  BEGIN
    SELECT MW_MAP(T.WORD, T.PY, S.WORDPOS, S.WORDLEN, T.PY3) BULK COLLECT
      INTO P_PARAM.V_MW_MAP
      FROM (SELECT SUBSTR(P_HZ, RN.N, RN2.N) STR,
                   RN.N WORDPOS,
                   RN2.N WORDLEN
              FROM DUAL C,
                   (SELECT ROWNUM N
                      FROM DUAL
                    CONNECT BY ROWNUM <
                               (SELECT MAX(LENGTH(P_HZ)) + 1 FROM DUAL T)) RN,
                   (SELECT ROWNUM N
                      FROM DUAL
                    CONNECT BY ROWNUM <
                               (SELECT MAX(LENGTH(P_HZ)) + 1 FROM DUAL T)) RN2
             WHERE LENGTH(P_HZ) >= RN.N
               AND LENGTH(P_HZ) >= RN2.N
               AND RN2.N + RN.N <= LENGTH(P_HZ) + 1) S,
           (SELECT WORD,
                   PY,
                   PY3,
                   ROW_NUMBER() OVER(PARTITION BY WORD ORDER BY RPAD(NVL(ADMINAREA, 'A'), 4, 'A')) RN
              FROM TY_NAVICOVPY_WORD
             WHERE ADMINAREA IS NULL
                OR ADMINAREA = P_ADMINCODE_2
                OR ADMINAREA = P_ADMINCODE_4) T
     WHERE S.STR = T.WORD
       AND T.RN = 1;
  END INIT_MW_LIST;

  --根据参考拼音，获取汉字的拼音
  FUNCTION GET_HZ_PY(P_HZ IN VARCHAR2, P_PY IN VARCHAR2) RETURN VARCHAR2 IS
    CURSOR C_HZ IS
      SELECT PY, PY2, TONE
        FROM TY_NAVICOVPY_PY
       WHERE JT = P_HZ
         AND UPPER(PY) = UPPER(P_PY)
       ORDER BY PYORDER;
    V_PY      VARCHAR2(16);
    V_PY_TONE VARCHAR2(16);
    /*    V_TONE_NUM NUMBER(1);*/
    V_TONE_NUM VARCHAR2(16);
    V_PY_OUT   VARCHAR2(50);
  BEGIN
    OPEN C_HZ;
    FETCH C_HZ
      INTO V_PY, V_PY_TONE, V_TONE_NUM;
    IF C_HZ%NOTFOUND THEN
      V_PY_OUT := PY_UTILS.CONVERT_FULL2HALF_MULTITHREAD(P_HZ);
    ELSE
      IF C_FORMAT_TONE = 0 THEN
        V_PY_OUT := V_PY;
      ELSIF C_FORMAT_TONE = 1 THEN
        --带音调拼音
        V_PY_OUT := V_PY_TONE;
      ELSE
        V_PY_OUT := V_PY || V_TONE_NUM;
      END IF;
    END IF;
    CLOSE C_HZ;

    RETURN V_PY_OUT;
  END GET_HZ_PY;

  ---获取一个汉字的默认拼音
  FUNCTION GET_HZ_DEF_PY(P_HZ IN VARCHAR2) RETURN VARCHAR2 IS
    V_PY      VARCHAR2(16);
    V_PY_TONE VARCHAR2(16);
    /*V_TONE_NUM NUMBER(1);*/
    V_TONE_NUM VARCHAR2(16);
    V_PY_OUT   VARCHAR2(50);
  BEGIN
    SELECT PY, PY2, TONE
      INTO V_PY, V_PY_TONE, V_TONE_NUM
      FROM TY_NAVICOVPY_PY
     WHERE JT = P_HZ
       AND PYORDER = 0;

    IF C_FORMAT_TONE = 0 THEN
      V_PY_OUT := V_PY;
    ELSIF C_FORMAT_TONE = 1 THEN
      --带音调拼音
      V_PY_OUT := V_PY_TONE;
    ELSE
      V_PY_OUT := V_PY || V_TONE_NUM;
    END IF;

    RETURN V_PY_OUT;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      V_PY_OUT := PY_UTILS.CONVERT_FULL2HALF_MULTITHREAD(P_HZ);
      RETURN V_PY_OUT;
  END;

  ---判断一个字符是否是一个英文字母数字串的一部分
  FUNCTION IS_PART_OF_STRING(P_PARAM IN T_COMMON_PARAM, IN_POS NUMBER)
    RETURN BOOLEAN IS
    V_FLAG BOOLEAN := FALSE;
  BEGIN
    IF IN_POS > 1 THEN
      ---不包含数字转拼音的
      IF P_PARAM.V_TYPE_T(IN_POS) IN ('B', 'C') AND
         P_PARAM.V_TYPE_T(IN_POS - 1) IN ('B', 'C') THEN
        V_FLAG := TRUE;
      END IF;
    END IF;

    RETURN V_FLAG;
  END IS_PART_OF_STRING;

  ---根据字符在字符串中的上下文，获取字符的拼音信息
  --主要用于判断是否是独立的拼音，独立拼音前后需要加空格
  FUNCTION GET_PY_BY_CONTEXT(P_PARAM IN T_COMMON_PARAM, IN_POS NUMBER)
    RETURN VARCHAR2 IS
    V_PY VARCHAR2(16);
  BEGIN
    IF IN_POS < 1 OR NOT P_PARAM.V_HZ_T.EXISTS(IN_POS) THEN
      RETURN NULL;
    END IF;

    IF P_PARAM.V_TYPE_T(IN_POS) = 'CC' THEN
      V_PY := CONVERTNUM(P_PARAM.V_HZ_T(IN_POS));
    ELSE
      IF P_PARAM.V_DEF_PY_T.EXISTS(IN_POS) THEN
        V_PY := GET_HZ_PY(P_PARAM.V_HZ_T(IN_POS),
                          P_PARAM.V_DEF_PY_T(IN_POS));
      ELSE
        V_PY := GET_HZ_DEF_PY(P_PARAM.V_HZ_T(IN_POS));
      END IF;
    END IF;

    IF V_PY IS NOT NULL THEN
      IF IS_PART_OF_STRING(P_PARAM, IN_POS) THEN
        V_PY := V_PY;
      ELSIF P_PARAM.V_TYPE_T(IN_POS) = 'A' AND C_PY_FIRST_CHAR_UPPER = 1 THEN
        V_PY := ' ' || INITCAP(V_PY);
      ELSIF P_PARAM.V_TYPE_T(IN_POS) = 'CC' AND
            C_NUMBER_FIRST_CHAR_UPPER = 1 THEN
        V_PY := ' ' || INITCAP(V_PY);
      ELSE
        V_PY := ' ' || V_PY;
      END IF;
    END IF;

    RETURN V_PY;
  END;

  FUNCTION CONVERTMULTIWORD2(P_PARAM       IN OUT NOCOPY T_COMMON_PARAM,
                             P_HZ          IN VARCHAR2,
                             P_ADMINCODE_2 IN VARCHAR2,
                             P_ADMINCODE_4 IN VARCHAR2,
                             P_SPOS        IN NUMBER,
                             P_LENG        IN NUMBER) RETURN VARCHAR2 IS
    CURSOR C_MULTIWORD(V_SPOS NUMBER, V_LENG NUMBER, V_MAP MW_ARRAY) IS
      SELECT WORD, WORDPOS, WORDLEN, WORDPY, WORDPY2
        FROM TABLE(V_MAP) G1
       WHERE G1.WORDPOS >= V_SPOS
         AND G1.WORDPOS + G1.WORDLEN <= V_SPOS + V_LENG
       ORDER BY WORDLEN DESC, WORDPOS;
    V_WORD    VARCHAR2(4000);
    V_WORDPOS NUMBER;
    V_WORDLEN NUMBER;
    V_PY      VARCHAR2(4000);
    V_PY2     VARCHAR2(4000);
    V_PY_OUT  VARCHAR2(4000);
    V_ISFOUND BOOLEAN := FALSE;
  BEGIN
    OPEN C_MULTIWORD(P_SPOS, P_LENG, P_PARAM.V_MW_MAP);
    FETCH C_MULTIWORD
      INTO V_WORD, V_WORDPOS, V_WORDLEN, V_PY, V_PY2;
    IF C_MULTIWORD%FOUND THEN
      LOOP
        EXIT WHEN C_MULTIWORD%NOTFOUND;

        IF IS_KEYWORD_PY_CORRECT(P_PARAM, V_PY, V_WORDPOS, V_WORDLEN) THEN
          V_ISFOUND := TRUE;
          EXIT;
        END IF;

        FETCH C_MULTIWORD
          INTO V_WORD, V_WORDPOS, V_WORDLEN, V_PY, V_PY2;
      END LOOP;
    END IF;

    IF V_ISFOUND THEN
      IF V_WORDPOS > P_SPOS THEN
        V_PY_OUT := CONVERTMULTIWORD2(P_PARAM,
                                      P_HZ,
                                      P_ADMINCODE_2,
                                      P_ADMINCODE_4,
                                      P_SPOS,
                                      V_WORDPOS - P_SPOS);
      END IF;

      IF C_PY_FIRST_CHAR_UPPER = 1 THEN
        V_PY  := INITCAP(V_PY);
        V_PY2 := INITCAP(V_PY2);
      END IF;

      IF C_FORMAT_TONE = 0 THEN
        V_PY_OUT := V_PY_OUT || ' ' || V_PY || ' ';
      ELSE
        V_PY_OUT := V_PY_OUT || ' ' || V_PY2 || ' ';
      END IF;

      IF V_WORDPOS + V_WORDLEN < P_SPOS + P_LENG THEN
        V_PY_OUT := V_PY_OUT ||
                    CONVERTMULTIWORD2(P_PARAM,
                                      P_HZ,
                                      P_ADMINCODE_2,
                                      P_ADMINCODE_4,
                                      V_WORDPOS + V_WORDLEN,
                                      P_SPOS + P_LENG - V_WORDPOS -
                                      V_WORDLEN);
      END IF;
    ELSE
      FOR I IN P_SPOS .. P_SPOS + P_LENG - 1 LOOP
        V_PY_OUT := V_PY_OUT || GET_PY_BY_CONTEXT(P_PARAM, I);
      END LOOP;
    END IF;
    CLOSE C_MULTIWORD;

    RETURN V_PY_OUT;
  END CONVERTMULTIWORD2;

  FUNCTION CONVERTMULTIWORD(P_PARAM       IN OUT NOCOPY T_COMMON_PARAM,
                            P_HZ          IN VARCHAR2,
                            P_ADMINCODE_2 IN VARCHAR2,
                            P_ADMINCODE_4 IN VARCHAR2) RETURN VARCHAR2 IS
    V_PY VARCHAR2(4000) := '';
  BEGIN
    IF C_REF_KEYWORD_FLAG > 0 THEN
      INIT_MW_LIST(P_PARAM, P_HZ, P_ADMINCODE_2, P_ADMINCODE_4);
    END IF;

    V_PY := CONVERTMULTIWORD2(P_PARAM,
                              P_HZ,
                              P_ADMINCODE_2,
                              P_ADMINCODE_4,
                              1,
                              LENGTH(P_HZ));
    RETURN V_PY;
  END CONVERTMULTIWORD;

  /*
  根据字符串及其参考拼音，转换对应的拼音
  参数：初始化好的拼音转换对象、字符串、参考拼音串、省级行政区划代码、市级行政区划代码
  返回值：拼音串
  */
  FUNCTION CONVERTMULTIWORD_BY_PY(P_PARAM        IN OUT NOCOPY T_COMMON_PARAM, ---初始化后的拼音转换对象
                                  IS_HZ_STR      IN VARCHAR2, ---汉字字符串
                                  IS_PY_STR      IN VARCHAR2, ---参考拼音
                                  IS_ADMINCODE_2 IN VARCHAR2, ---省级行政区划代码
                                  IS_ADMINCODE_4 IN VARCHAR2 ---市级行政区划代码
                                  ) RETURN VARCHAR2 IS
    VS_RIGHT_PY VARCHAR2(4000);

    VS_RET_PY VARCHAR2(4000);
  BEGIN
    VS_RIGHT_PY := GET_CORRECT_PY(P_PARAM, IS_PY_STR);
    IF VS_RIGHT_PY IS NULL THEN
      RAISE E_HZ_PY_NOT_MATCHING;
    END IF;

    INIT_DEF_PY_T(P_PARAM, VS_RIGHT_PY);

    VS_RET_PY := CONVERTMULTIWORD(P_PARAM,
                                  IS_HZ_STR,
                                  IS_ADMINCODE_2,
                                  IS_ADMINCODE_4);

    RETURN VS_RET_PY;
  END;

  FUNCTION CONVERTMULTIWORD2_BY_HZ(P_PARAM       IN OUT NOCOPY T_COMMON_PARAM,
                                   P_HZ          IN VARCHAR2,
                                   P_ADMINCODE_2 IN VARCHAR2,
                                   P_ADMINCODE_4 IN VARCHAR2,
                                   P_SPOS        IN NUMBER,
                                   P_LENG        IN NUMBER) RETURN VARCHAR2 IS
    CURSOR C_MULTIWORD(V_SPOS NUMBER, V_LENG NUMBER, V_MAP MW_ARRAY) IS
      SELECT WORD, WORDPOS, WORDLEN, WORDPY, WORDPY2
        FROM TABLE(V_MAP) G1
       WHERE G1.WORDPOS >= V_SPOS
         AND G1.WORDPOS + G1.WORDLEN <= V_SPOS + V_LENG
       ORDER BY WORDLEN DESC, WORDPOS;

    V_WORD    VARCHAR2(4000);
    V_WORDPOS NUMBER;
    V_WORDLEN NUMBER;
    V_PY      VARCHAR2(4000);
    V_PY2     VARCHAR2(4000);
    V_PY_OUT  VARCHAR2(4000);
  BEGIN
    OPEN C_MULTIWORD(P_SPOS, P_LENG, P_PARAM.V_MW_MAP);
    FETCH C_MULTIWORD
      INTO V_WORD, V_WORDPOS, V_WORDLEN, V_PY, V_PY2;
    IF C_MULTIWORD%FOUND THEN
      IF V_WORDPOS > P_SPOS THEN
        V_PY_OUT := CONVERTMULTIWORD2_BY_HZ(P_PARAM,
                                            P_HZ,
                                            P_ADMINCODE_2,
                                            P_ADMINCODE_4,
                                            P_SPOS,
                                            V_WORDPOS - P_SPOS);
      END IF;

      IF C_PY_FIRST_CHAR_UPPER = 1 THEN
        V_PY  := INITCAP(V_PY);
        V_PY2 := INITCAP(V_PY2);
      END IF;

      IF C_FORMAT_TONE = 0 THEN
        V_PY_OUT := V_PY_OUT || ' ' || V_PY || ' ';
      ELSE
        V_PY_OUT := V_PY_OUT || ' ' || V_PY2 || ' ';
      END IF;

      IF V_WORDPOS + V_WORDLEN < P_SPOS + P_LENG THEN
        V_PY_OUT := V_PY_OUT ||
                    CONVERTMULTIWORD2_BY_HZ(P_PARAM,
                                            P_HZ,
                                            P_ADMINCODE_2,
                                            P_ADMINCODE_4,
                                            V_WORDPOS + V_WORDLEN,
                                            P_SPOS + P_LENG - V_WORDPOS -
                                            V_WORDLEN);
      END IF;
    ELSE
      FOR I IN P_SPOS .. P_SPOS + P_LENG - 1 LOOP
        V_PY_OUT := V_PY_OUT || GET_PY_BY_CONTEXT(P_PARAM, I);
      END LOOP;
    END IF;
    CLOSE C_MULTIWORD;

    RETURN V_PY_OUT;
  END CONVERTMULTIWORD2_BY_HZ;

  FUNCTION CONVERTMULTIWORD_BY_HZ(P_PARAM       IN OUT NOCOPY T_COMMON_PARAM,
                                  P_HZ          IN VARCHAR2,
                                  P_ADMINCODE_2 IN VARCHAR2,
                                  P_ADMINCODE_4 IN VARCHAR2) RETURN VARCHAR2 IS
    V_PY VARCHAR2(4000);
  BEGIN
    IF C_REF_KEYWORD_FLAG > 0 THEN
      INIT_MW_LIST(P_PARAM, P_HZ, P_ADMINCODE_2, P_ADMINCODE_4);
    END IF;

    V_PY := CONVERTMULTIWORD2_BY_HZ(P_PARAM,
                                    P_HZ,
                                    P_ADMINCODE_2,
                                    P_ADMINCODE_4,
                                    1,
                                    LENGTH(P_HZ));
    RETURN V_PY;
  END CONVERTMULTIWORD_BY_HZ;

  --预处理，进行以下操作：
  ---1.将汉字串中的全角字符转换成半角字符，能转的转，不能转的保持原值
  ---2.根据过滤特殊字符的开关参数，对特殊字符进行处理。如果过滤，则将特殊字符替换为半角空格
  ---3.将不可见字符全部转换为半角空格，并去掉字符串两端的半角空格，以及字符串中的连续空格
  FUNCTION BEFORE_CHECK(P_HZ VARCHAR2) RETURN VARCHAR2 IS
    V_ASC      NUMBER(10);
    V_REST_STR VARCHAR2(4000);
    V_HZ_CPY   VARCHAR2(4000);
    V_TEMP     VARCHAR2(3);
  BEGIN
    IF P_HZ IS NULL THEN
      RETURN NULL;
    END IF;

    V_HZ_CPY := PY_UTILS.CONVERT_FULL2HALF_WIDTH(P_HZ);
    FOR I IN 1 .. LENGTH(V_HZ_CPY) LOOP
      V_TEMP := SUBSTR(V_HZ_CPY, I, 1);
      V_ASC  := ASCII(V_TEMP);

      IF (V_ASC >= 0 AND V_ASC < 32) THEN
        V_REST_STR := V_REST_STR || ' ';
      ELSE
        IF C_FILTER_SPECIAL_CHARACTERS = 1 AND
           IS_SPECIAL_CHARACTER(V_TEMP, 2, C_SPECIAL_CHARACTERS) THEN
          V_REST_STR := V_REST_STR || ' ';
        ELSE
          V_REST_STR := V_REST_STR || V_TEMP;
        END IF;
      END IF;
    END LOOP;

    --处理连续空格
    V_REST_STR := CLEAR_MULTISPACE(V_REST_STR);

    RETURN V_REST_STR;
  END;

  FUNCTION CONVERT_HZ_TONE(P_HZ        IN VARCHAR2,
                           P_ADMINCODE IN VARCHAR2 DEFAULT '',
                           P_CONVROAD  IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_TEMP   VARCHAR2(4000);
    V_HZ     VARCHAR2(4000) := P_HZ;
    V_PY     VARCHAR2(4000) := '';
    V_HZ_CPY VARCHAR2(4000);
    V_PARAM  T_COMMON_PARAM;
  BEGIN
    IF V_HZ IS NULL THEN
      RETURN '';
    END IF;

    V_HZ_CPY := BEFORE_CHECK(V_HZ);

    IF P_CONVROAD = '1' THEN
      CONVERT_ROAD_NAME(V_HZ_CPY, V_PY);
    END IF;

    INIT_PY_LIST(V_PARAM, V_HZ_CPY);

    V_PY := CONVERTMULTIWORD_BY_HZ(V_PARAM,
                                   V_HZ_CPY,
                                   SUBSTR(P_ADMINCODE, 1, 2),
                                   SUBSTR(P_ADMINCODE, 1, 4));

    V_PY := REPLACE(V_PY, '　', ' ');

    FOR I IN 1 .. LENGTH(PRIVATE_SPECIAL_CHARACTERS) LOOP
      V_TEMP := SUBSTR(PRIVATE_SPECIAL_CHARACTERS, I, 1);
      V_PY   := REPLACE(V_PY, V_TEMP, ' ' || V_TEMP || ' ');
    END LOOP;

    --处理连续空格
    V_PY := CLEAR_MULTISPACE(V_PY);

    --    IF C_FORMAT_TONE = 0 THEN
    --      V_PY := CONVERT_NONE_TONE_PY(V_PY);
    --    END IF;

    V_PY := REPLACE(V_PY, '～', '~');
    ----No .   特殊处理  ----- modify by zhangjin
    V_PY := REPLACE(V_PY, 'No . ', 'No.');
    V_PY := REPLACE(V_PY, 'no . ', 'no.');
    V_PY := REPLACE(V_PY, 'NO . ', 'NO.');
    V_PY := REPLACE(V_PY, 'nO . ', 'nO.');

    RETURN V_PY;
  EXCEPTION
    WHEN E_HZ_PY_NOT_MATCHING THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        RETURN 'ERROR!汉字和拼音不匹配，请检查。';
      ELSE
        RETURN 'ERROR!';
      END IF;
    WHEN E_NO_HZ_FOUND THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        RETURN 'ERROR!没有找到汉字或没有对应的拼音，请检查。';
      ELSE
        RETURN 'ERROR!';
      END IF;
    WHEN OTHERS THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        RETURN 'ERROR!' || SQLERRM || ' HZ:' || P_HZ;
      ELSE
        RETURN 'ERROR!';
      END IF;

  END CONVERT_HZ_TONE;

  FUNCTION CONVERT_PY_TONE(P_HZ        IN VARCHAR2,
                           P_PY        IN VARCHAR2,
                           P_ADMINCODE IN VARCHAR2 DEFAULT '',
                           P_CONVROAD  IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_HZ     VARCHAR2(4000) := P_HZ;
    V_PY     VARCHAR2(4000) := P_PY;
    V_HZ_CPY VARCHAR2(4000);
    V_PY_CPY VARCHAR2(4000);
    V_PARAM  T_COMMON_PARAM;
    V_FORMAT NUMBER(1) := 0;
    V_POS15  NUMBER(10) := 0;
  BEGIN
    IF V_HZ IS NULL AND V_PY IS NULL THEN
      RETURN '';
    END IF;

    IF V_PY IS NULL THEN
      RETURN CONVERT_HZ_TONE(P_HZ, P_ADMINCODE, P_CONVROAD);
    END IF;

    V_HZ_CPY := BEFORE_CHECK(V_HZ);
    V_PY_CPY := BEFORE_CHECK(V_PY);

    IF P_CONVROAD = '1' THEN
      CONVERT_ROAD_NAME(V_HZ, V_PY);
    END IF;

    INIT_PY_LIST(V_PARAM, V_HZ_CPY);

    --判断数字是否多于15个，如果多于15则不作为多音字处理，否则内存溢出
    SELECT REGEXP_INSTR(P_HZ, '[０-９]', 1, 16) INTO V_POS15 FROM DUAL;

    IF V_POS15 > 0 THEN
      IF REGEXP_LIKE(V_PY_CPY, '[0-9]') THEN
        V_FORMAT := 1;
      ELSE
        V_FORMAT := 2;
      END IF;
    END IF;

    V_PARAM.V_NUM_FORMAT := V_FORMAT;

    V_PY := CONVERTMULTIWORD_BY_PY(V_PARAM,
                                   V_HZ_CPY,
                                   V_PY_CPY,
                                   SUBSTR(P_ADMINCODE, 1, 2),
                                   SUBSTR(P_ADMINCODE, 1, 4));

    ---处理连续空格
    V_PY := CLEAR_MULTISPACE(V_PY);

    --    IF C_FORMAT_TONE = 0 THEN
    --      V_PY := CONVERT_NONE_TONE_PY(V_PY);
    --    END IF;

    V_PY := REPLACE(V_PY, '～', '~');

    ----No .   特殊处理   ----- modify by zhangjin
    V_PY := REPLACE(V_PY, 'No . ', 'No.');
    V_PY := REPLACE(V_PY, 'no . ', 'no.');
    V_PY := REPLACE(V_PY, 'NO . ', 'NO.');
    V_PY := REPLACE(V_PY, 'nO . ', 'nO.');

    RETURN V_PY;
  EXCEPTION
    WHEN E_HZ_PY_NOT_MATCHING THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        RETURN 'ERROR!汉字和拼音不匹配，请检查。';
      ELSE
        RETURN 'ERROR!';
      END IF;
    WHEN E_NO_HZ_FOUND THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        RETURN 'ERROR!没有找到汉字或没有对应的拼音，请检查。';
      ELSE
        RETURN 'ERROR!';
      END IF;
    WHEN OTHERS THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        RETURN 'ERROR!' || SQLERRM || ' HZ:' || P_HZ;
      ELSE
        RETURN 'ERROR!';
      END IF;

  END CONVERT_PY_TONE;

  PROCEDURE CONVERT_ROAD_NAME(P_HZ IN OUT NOCOPY VARCHAR2,
                              P_PY IN OUT NOCOPY VARCHAR2) IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    V_HZ  VARCHAR2(4000);
    V_PY  VARCHAR2(4000);
    V_TMP VARCHAR2(4000);
  BEGIN
    IF P_HZ IS NULL OR LENGTH(P_HZ) <= 1 THEN
      RETURN;
    END IF;

    V_PY := REPLACE(P_PY, ' ', '');
    V_PY := UPPER(V_PY);

    IF REGEXP_LIKE(P_HZ, '^Ａ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+') THEN
      V_TMP := REGEXP_SUBSTR(P_HZ, '^Ａ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+', 1, 1);
      V_HZ  := SUBSTR(V_TMP, 2) || '高速公路' ||
               SUBSTR(P_HZ, LENGTH(V_TMP) + 1);

      IF SUBSTR(V_PY, 1, 1) = 'A' THEN
        V_PY := SUBSTR(V_PY, 2);
      END IF;
    ELSIF REGEXP_LIKE(P_HZ, '^Ｇ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+') THEN
      V_TMP := REGEXP_SUBSTR(P_HZ, '^Ｇ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+', 1, 1);

      IF INSTR(V_PY, 'GAOSUGONGLU') > 0 THEN
        V_HZ := SUBSTR(V_TMP, 2) || '高速公路' ||
                SUBSTR(P_HZ, LENGTH(V_TMP) + 1);
      ELSE
        V_HZ := SUBSTR(V_TMP, 2) || '国道' || SUBSTR(P_HZ, LENGTH(V_TMP) + 1);
      END IF;

      IF SUBSTR(V_PY, 1, 1) = 'G' THEN
        V_PY := SUBSTR(V_PY, 2);
      END IF;
    ELSIF REGEXP_LIKE(P_HZ, '^Ｓ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+') THEN
      V_TMP := REGEXP_SUBSTR(P_HZ, '^Ｓ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+', 1, 1);

      IF INSTR(V_PY, 'GAOSUGONGLU') > 0 THEN
        V_HZ := SUBSTR(V_TMP, 2) || '高速公路' ||
                SUBSTR(P_HZ, LENGTH(V_TMP) + 1);
      ELSE
        V_HZ := SUBSTR(V_TMP, 2) || '省道' || SUBSTR(P_HZ, LENGTH(V_TMP) + 1);
      END IF;

      IF ((SUBSTR(V_HZ, 1, 1) = '３' OR SUBSTR(V_HZ, 1, 1) = '４') AND
         SUBSTR(V_PY, 1, 2) = 'SS') OR
         ((SUBSTR(V_HZ, 1, 1) != '３' AND SUBSTR(V_HZ, 1, 1) != '４') AND
         SUBSTR(V_PY, 1, 1) = 'S') THEN
        V_PY := SUBSTR(V_PY, 2);
      END IF;
    ELSIF REGEXP_LIKE(P_HZ, '^Ｘ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+') THEN
      V_TMP := REGEXP_SUBSTR(P_HZ, '^Ｘ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+', 1, 1);
      V_HZ  := SUBSTR(V_TMP, 2) || '县道' || SUBSTR(P_HZ, LENGTH(V_TMP) + 1);

      IF SUBSTR(V_PY, 1, 1) = 'X' THEN
        V_PY := SUBSTR(V_PY, 2);
      END IF;
    ELSIF REGEXP_LIKE(P_HZ, '^Ｙ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+') THEN
      V_TMP := REGEXP_SUBSTR(P_HZ, '^Ｙ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+', 1, 1);
      V_HZ  := SUBSTR(V_TMP, 2) || '乡道' || SUBSTR(P_HZ, LENGTH(V_TMP) + 1);

      IF (SUBSTR(V_HZ, 1, 1) = '１' AND SUBSTR(V_PY, 1, 2) = 'YY') OR
         (SUBSTR(V_HZ, 1, 1) != '１' AND SUBSTR(V_PY, 1, 1) = 'Y') THEN
        V_PY := SUBSTR(V_PY, 2);
      END IF;
    ELSIF REGEXP_LIKE(P_HZ, '^Ｚ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+') THEN
      V_TMP := REGEXP_SUBSTR(P_HZ, '^Ｚ[a-zA-Zａ-ｚＡ-Ｚ]*+\d+', 1, 1);
      V_HZ  := SUBSTR(V_TMP, 2) || '专用道' || SUBSTR(P_HZ, LENGTH(V_TMP) + 1);

      IF SUBSTR(V_PY, 1, 1) = 'Z' THEN
        V_PY := SUBSTR(V_PY, 2);
      END IF;
    ELSE
      V_HZ := P_HZ;
      V_PY := P_PY;
    END IF;

    P_HZ := V_HZ;
    P_PY := V_PY;

  END CONVERT_ROAD_NAME;

  FUNCTION CONVERT_PY_TONE_PIPELINED(IN_CURSOR IN TYPEREFCURSOR)
    RETURN T_RESULT_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION IN_CURSOR BY ANY) IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    V_ID         VARCHAR2(4000);
    V_HZ         VARCHAR2(4000);
    V_PY         VARCHAR2(4000);
    V_ADMIN      VARCHAR2(4000);
    V_RESULT_MAP M_RESULT;
  BEGIN
    LOOP
      FETCH IN_CURSOR
        INTO V_ID, V_HZ, V_PY, V_ADMIN;
      EXIT WHEN IN_CURSOR%NOTFOUND;

      V_RESULT_MAP.PY_RESULT := CONVERT_PY_TONE(V_HZ, V_PY, V_ADMIN);
      V_RESULT_MAP.PID       := V_ID;

      PIPE ROW(V_RESULT_MAP);
    END LOOP;

    COMMIT;
    RETURN;
  END CONVERT_PY_TONE_PIPELINED;

  FUNCTION CONVERT_HZ_TONE_PIPELINED(IN_CURSOR IN TYPEREFCURSOR)
    RETURN T_RESULT_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION IN_CURSOR BY ANY) IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    V_ID         VARCHAR2(4000);
    V_HZ         VARCHAR2(4000);
    V_ADMIN      VARCHAR2(4000);
    V_RESULT_MAP M_RESULT;
  BEGIN
    LOOP
      FETCH IN_CURSOR
        INTO V_ID, V_HZ, V_ADMIN;
      EXIT WHEN IN_CURSOR%NOTFOUND;

      V_RESULT_MAP.PY_RESULT := CONVERT_HZ_TONE(V_HZ, V_ADMIN);
      V_RESULT_MAP.PID       := V_ID;

      PIPE ROW(V_RESULT_MAP);
    END LOOP;

    COMMIT;
    RETURN;
  END CONVERT_HZ_TONE_PIPELINED;

  FUNCTION ADD_CONFUSE_MARK(P_SPACE_SPLITED_PY VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_RESULT  VARCHAR2(1000) := '';
    V_PYARRAY COMMON_UTIL.STRINGARRAY;
    V_COUNT1  NUMBER(10);
    V_COUNT2  NUMBER(10);
  BEGIN
    V_PYARRAY := COMMON_UTIL.SPLITSTRING(P_SPACE_SPLITED_PY, ' ');
    V_RESULT  := V_PYARRAY(1);

    IF V_PYARRAY.COUNT > 1 THEN
      FOR I IN 1 .. V_PYARRAY.COUNT - 1 LOOP
        SELECT COUNT(1)
          INTO V_COUNT1
          FROM B_PYLET P1, B_PYLET P2
         WHERE P1.PY || P2.PY = LOWER(V_PYARRAY(I) || V_PYARRAY(I + 1))
           AND P1.PY <> LOWER(V_PYARRAY(I));
        SELECT COUNT(1)
          INTO V_COUNT2
          FROM B_PYLET P1
         WHERE P1.PY = LOWER(V_PYARRAY(I) || V_PYARRAY(I + 1));
        IF V_COUNT1 + V_COUNT2 > 0 THEN
          V_RESULT := V_RESULT || '''';
        END IF;
        V_RESULT := V_RESULT || V_PYARRAY(I + 1);
      END LOOP;
    END IF;

    V_RESULT := UPPER(SUBSTR(V_RESULT, 1, 1)) || SUBSTR(V_RESULT, 2);
    RETURN V_RESULT;
  END;

  FUNCTION CONVERT_TO_ENGLISH_MODE(P_HZ        IN VARCHAR2,
                                   P_ADMINCODE IN VARCHAR2 DEFAULT '',
                                   P_CONVROAD  IN VARCHAR2 DEFAULT '',
                                   P_PY        IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_PY VARCHAR2(4000);
  BEGIN
    C_PY_FIRST_CHAR_UPPER := 0;
    IF REGEXP_REPLACE(P_HZ, '[A-Za-zＡ-Ｚａ-ｚ０-９0-9]+', '') IS NOT NULL THEN
      C_CONVERT_NUMBER := 1;
    ELSE
      C_CONVERT_NUMBER := 0;
    END IF;
    V_PY := CONVERT_PY_TONE(P_HZ, P_PY, P_ADMINCODE, P_CONVROAD);
    IF V_PY IS NULL THEN
      RETURN NULL;
    END IF;
    C_PY_FIRST_CHAR_UPPER := 1;

    RETURN ADD_CONFUSE_MARK(V_PY);
  END;

  FUNCTION CONV_TO_ENGLISH_MODE_VOICEFILE(P_HZ        IN VARCHAR2,
                                          P_ADMINCODE IN VARCHAR2 DEFAULT '',
                                          P_CONVROAD  IN VARCHAR2 DEFAULT '',
                                          P_PY        IN VARCHAR2 DEFAULT '')
    RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_PY VARCHAR2(4000);
  BEGIN
    C_PY_FIRST_CHAR_UPPER := 0;
    --modify by wangxiwei 130514
    C_CONVERT_NUMBER := 1;
    /*IF REGEXP_REPLACE(P_HZ, '[A-Za-zＡ-Ｚａ-ｚ０-９0-9]+', '') IS NOT NULL THEN
      C_CONVERT_NUMBER := 1;
    ELSE
      C_CONVERT_NUMBER := 0;
    END IF;*/
    V_PY := CONVERT_PY_TONE(P_HZ, P_PY, P_ADMINCODE, P_CONVROAD);
    IF V_PY IS NULL THEN
      RETURN NULL;
    END IF;
    C_PY_FIRST_CHAR_UPPER := 1;
    RETURN ADD_CONFUSE_MARK(V_PY);
  END;

  /*
  * 生成道路名语音
  * P_CONVTYPE 预留， 暂未使用          1：道路名；2：分歧
  */
  FUNCTION CONVERT_RD_NAME_VOICE(P_HZ       IN VARCHAR2,
                                 P_PY       IN VARCHAR2,
                                 P_ADMIN    IN VARCHAR2,
                                 P_CONVTYPE IN VARCHAR2 DEFAULT '1')
    RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_RESULT VARCHAR2(4000) := '';
    V_HZ     VARCHAR2(4000) := '';
    V_PY     VARCHAR2(4000) := '';
    V_REG_HZ VARCHAR2(4000) := '';
    V_REG_PY VARCHAR2(4000) := '';
  BEGIN
    IF P_HZ IS NULL THEN
      RETURN '';
    END IF;

    V_HZ := P_HZ;
    V_PY := P_PY;

    --设定格式，只保留-，其它特殊字符全部过滤
    C_CONVERT_NUMBER      := 1;
    C_PY_FIRST_CHAR_UPPER := 0;
    --C_SPECIAL_CHARACTERS        := '[·￣·．！＠＃￥％＾＆＊＿＋｜、：“＜＞，。？｛｝《［］／（）＝＄‘’＇～”；＂?!"#$%&()*+,./:;<=>?@\^_`{|}~'']';
    C_FILTER_SPECIAL_CHARACTERS := 1;

    --正则表达式匹配 一个字母开头后面若干数字，需要转换为【高速公路】
    IF REGEXP_LIKE(P_HZ, '^[a-zA-Zａ-ｚＡ-Ｚ]++\d+') THEN
      V_REG_HZ := REGEXP_SUBSTR(P_HZ, '^[a-zA-Zａ-ｚＡ-Ｚ]{1}+\d+', 1, 1);
      V_HZ     := V_REG_HZ || '高速公路' || SUBSTR(P_HZ, LENGTH(V_REG_HZ) + 1);

      IF REGEXP_LIKE(P_PY, '^[a-zA-Z]{1}++\d+') THEN
        V_REG_PY := REGEXP_SUBSTR(P_PY, '^[a-zA-Z]{1}+\d+', 1, 1);
        V_PY     := V_REG_PY || 'gaosugonglu' ||
                    SUBSTR(P_PY, LENGTH(V_REG_PY) + 1);
      END IF;
    END IF;

    --转换拼音并增加混淆音分隔符
    V_RESULT := CONV_TO_ENGLISH_MODE_VOICEFILE(V_HZ, P_ADMIN, '', V_PY);

    --如果转换失败，直接返回错误信息
    IF V_RESULT LIKE 'ERROR%' THEN
      RETURN V_RESULT;
    END IF;

    V_RESULT := REPLACE(V_RESULT, '''', '#');

    /*    --如果分隔符两边是大写英文字母，则删除隔音符
        LOOP
          V_REG_IDX := REGEXP_INSTR(V_RESULT, '#+[A-Z]+');
          EXIT WHEN V_REG_IDX <= 0;
          V_RESULT := SUBSTR(V_RESULT, 1, V_REG_IDX - 1) ||
                      SUBSTR(V_RESULT, V_REG_IDX + 1);
        END LOOP;
        LOOP
          V_REG_IDX := REGEXP_INSTR(V_RESULT, '[A-Z]+#+');
          EXIT WHEN V_REG_IDX <= 0;
          V_RESULT := SUBSTR(V_RESULT, 1, V_REG_IDX) ||
                      SUBSTR(V_RESULT, V_REG_IDX + 2);
        END LOOP;
    */
    --第一个字符大写，其它字符全部小写
    V_RESULT := REPLACE(V_RESULT, '#', '''');
    V_RESULT := UPPER(SUBSTR(V_RESULT, 1, 1)) || LOWER(SUBSTR(V_RESULT, 2));
    RETURN V_RESULT;
  END;

  /*
  * 生成道路名语音
  * P_CONVTYPE 预留， 暂未使用          1：道路名；2：分歧
  */
  FUNCTION CONVERT_RRANCH_NAME_VOICE(P_HZ       IN VARCHAR2,
                                     P_PY       IN VARCHAR2,
                                     P_ADMIN    IN VARCHAR2,
                                     P_CONVTYPE IN VARCHAR2 DEFAULT '1')
    RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_RESULT VARCHAR2(4000) := '';
    V_HZ     VARCHAR2(4000) := '';
    V_PY     VARCHAR2(4000) := '';
    V_REG_PY VARCHAR2(4000) := '';
  BEGIN
    IF P_HZ IS NULL THEN
      RETURN '';
    END IF;

    V_HZ := P_HZ;
    V_PY := REPLACE(P_PY, ' ');

    --设定格式，只保留-，其它特殊字符全部过滤
    C_CONVERT_NUMBER      := 1;
    C_PY_FIRST_CHAR_UPPER := 0;
    --C_SPECIAL_CHARACTERS        := '[·￣·．！＠＃￥％＾＆＊＿＋｜、：“＜＞，。？｛｝《［］／（）＝＄‘’＇～”；＂?!"#$%&()*+,./:;<=>?@\^_`{|}~'']';
    C_FILTER_SPECIAL_CHARACTERS := 1;
    C_FORMAT_TONE               := 0;

    IF P_CONVTYPE = '1' THEN
      IF REGEXP_LIKE(P_HZ,
                     '^[GＧSＳXＸYＹCＣZＺ][a-zａ-ｚA-ZＡ-Ｚ]*\d+　[ＥＷＳＮ]$') AND
         REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+[EWSN]$') THEN
        V_HZ := SUBSTR(P_HZ, 1, LENGTH(P_HZ) - 2) ||
                CASE SUBSTR(P_HZ, -1, 1)
                  WHEN 'Ｅ' THEN
                   '东'
                  WHEN 'Ｗ' THEN
                   '西'
                  WHEN 'Ｓ' THEN
                   '南'
                  WHEN 'Ｎ' THEN
                   '北'
                END;
        V_PY := SUBSTR(V_PY, 1, LENGTH(V_PY) - 1) ||
                CASE SUBSTR(V_PY, -1, 1)
                  WHEN 'E' THEN
                   ' dong '
                  WHEN 'W' THEN
                   ' xi'
                  WHEN 'S' THEN
                   ' nan'
                  WHEN 'N' THEN
                   ' bei'
                END;
      END IF;
      IF REGEXP_LIKE(P_HZ,
                     '^[GＧSＳXＸYＹCＣZＺ][a-zａ-ｚA-ZＡ-Ｚ]*\d+（[ＥＷＳＮ]）$') THEN
        V_HZ := SUBSTR(P_HZ, 1, LENGTH(P_HZ) - 3) ||
                CASE SUBSTR(P_HZ, -2, 1)
                  WHEN 'Ｅ' THEN
                   '东'
                  WHEN 'Ｗ' THEN
                   '西'
                  WHEN 'Ｓ' THEN
                   '南'
                  WHEN 'Ｎ' THEN
                   '北'
                END;
        IF REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+[EWSN]$') THEN
          V_PY := SUBSTR(V_PY, 1, LENGTH(V_PY) - 1) ||
                  CASE SUBSTR(V_PY, -1, 1)
                    WHEN 'E' THEN
                     ' dong '
                    WHEN 'W' THEN
                     ' xi'
                    WHEN 'S' THEN
                     ' nan'
                    WHEN 'N' THEN
                     ' bei'
                  END;
        ELSIF REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+\([EWSN]\)$') THEN
          V_PY := SUBSTR(V_PY, 1, LENGTH(V_PY) - 3) ||
                  CASE SUBSTR(V_PY, -2, 1)
                    WHEN 'E' THEN
                     ' dong '
                    WHEN 'W' THEN
                     ' xi'
                    WHEN 'S' THEN
                     ' nan'
                    WHEN 'N' THEN
                     ' bei'
                  END;
        END IF;
      END IF;
    END IF;

    IF P_CONVTYPE = '2' THEN
      IF REGEXP_LIKE(P_HZ, '^[GＧSＳXＸYＹCＣZＺ][a-zａ-ｚA-ZＡ-Ｚ]*\d+$') AND
         REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+$') THEN
        V_HZ := SUBSTR(P_HZ, 2, LENGTH(P_HZ) - 1) || CASE SUBSTR(P_HZ, 1, 1)
                  WHEN 'G' THEN
                   '国道'
                  WHEN 'Ｇ' THEN
                   '国道'
                  WHEN 'S' THEN
                   '省道'
                  WHEN 'Ｓ' THEN
                   '省道'
                  WHEN 'X' THEN
                   '县道'
                  WHEN 'Ｘ' THEN
                   '县道'
                  WHEN 'Y' THEN
                   '乡道'
                  WHEN 'Ｙ' THEN
                   '乡道'
                  WHEN 'C' THEN
                   '村道'
                  WHEN 'Ｃ' THEN
                   '村道'
                  WHEN 'Z' THEN
                   '专用道'
                  WHEN 'Ｚ' THEN
                   '专用道'
                END;
        V_PY := SUBSTR(P_HZ, 2) || CASE SUBSTR(V_PY, 1, 1)
                  WHEN 'G' THEN
                   ' guodao'
                  WHEN 'S' THEN
                   ' shengdao'
                  WHEN 'X' THEN
                   'xiandao'
                  WHEN 'Y' THEN
                   'xiangdao'
                  WHEN 'C' THEN
                   'cundao'
                  WHEN 'Z' THEN
                   'zhuanyongdao'
                END;
      END IF;

      IF REGEXP_LIKE(P_HZ,
                     '^[GＧSＳXＸYＹCＣZＺ][a-zａ-ｚA-ZＡ-Ｚ]*\d+　[ＥＷＳＮ]$') AND
         REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+[EWSN]$') THEN
        V_HZ := SUBSTR(P_HZ, 2, LENGTH(P_HZ) - 3) || CASE SUBSTR(P_HZ, 1, 1)
                  WHEN 'G' THEN
                   '国道'
                  WHEN 'Ｇ' THEN
                   '国道'
                  WHEN 'S' THEN
                   '省道'
                  WHEN 'Ｓ' THEN
                   '省道'
                  WHEN 'X' THEN
                   '县道'
                  WHEN 'Ｘ' THEN
                   '县道'
                  WHEN 'Y' THEN
                   '乡道'
                  WHEN 'Ｙ' THEN
                   '乡道'
                  WHEN 'C' THEN
                   '村道'
                  WHEN 'Ｃ' THEN
                   '村道'
                  WHEN 'Z' THEN
                   '专用道'
                  WHEN 'Ｚ' THEN
                   '专用道'
                END || CASE SUBSTR(P_HZ, -1, 1)
                  WHEN 'Ｅ' THEN
                   '东'
                  WHEN 'Ｗ' THEN
                   '西'
                  WHEN 'Ｓ' THEN
                   '南'
                  WHEN 'Ｎ' THEN
                   '北'
                END;
        V_PY := SUBSTR(P_HZ, 2, LENGTH(V_PY) - 2) || CASE SUBSTR(V_PY, 1, 1)
                  WHEN 'G' THEN
                   ' guodao'
                  WHEN 'S' THEN
                   ' shengdao'
                  WHEN 'X' THEN
                   'xiandao'
                  WHEN 'Y' THEN
                   'xiangdao'
                  WHEN 'C' THEN
                   'cundao'
                  WHEN 'Z' THEN
                   'zhuanyongdao'
                END || CASE SUBSTR(V_PY, -1, 1)
                  WHEN 'E' THEN
                   ' dong '
                  WHEN 'W' THEN
                   ' xi'
                  WHEN 'S' THEN
                   ' nan'
                  WHEN 'N' THEN
                   ' bei'
                END;
      END IF;

      IF REGEXP_LIKE(P_HZ,
                     '^[GＧSＳXＸYＹCＣZＺ][a-zａ-ｚA-ZＡ-Ｚ]*\d+（[ＥＷＳＮ]）$') THEN

        V_HZ := SUBSTR(P_HZ, 2, LENGTH(P_HZ) - 3) || CASE SUBSTR(P_HZ, 1, 1)
                  WHEN 'G' THEN
                   '国道'
                  WHEN 'Ｇ' THEN
                   '国道'
                  WHEN 'S' THEN
                   '省道'
                  WHEN 'Ｓ' THEN
                   '省道'
                  WHEN 'X' THEN
                   '县道'
                  WHEN 'Ｘ' THEN
                   '县道'
                  WHEN 'Y' THEN
                   '乡道'
                  WHEN 'Ｙ' THEN
                   '乡道'
                  WHEN 'C' THEN
                   '村道'
                  WHEN 'Ｃ' THEN
                   '村道'
                  WHEN 'Z' THEN
                   '专用道'
                  WHEN 'Ｚ' THEN
                   '专用道'
                END || CASE SUBSTR(P_HZ, -2, 1)
                  WHEN 'Ｅ' THEN
                   '东'
                  WHEN 'Ｗ' THEN
                   '西'
                  WHEN 'Ｓ' THEN
                   '南'
                  WHEN 'Ｎ' THEN
                   '北'
                END;
        IF REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+[EWSN]$') THEN

          V_PY := SUBSTR(V_PY, 2, LENGTH(V_PY) - 2) ||
                  CASE SUBSTR(V_PY, 1, 1)
                    WHEN 'G' THEN
                     ' guodao'
                    WHEN 'S' THEN
                     ' shengdao'
                    WHEN 'X' THEN
                     'xiandao'
                    WHEN 'Y' THEN
                     'xiangdao'
                    WHEN 'C' THEN
                     'cundao'
                    WHEN 'Z' THEN
                     'zhuanyongdao'
                  END || CASE SUBSTR(V_PY, -1, 1)
                    WHEN 'E' THEN
                     ' dong '
                    WHEN 'W' THEN
                     ' xi'
                    WHEN 'S' THEN
                     ' nan'
                    WHEN 'N' THEN
                     ' bei'
                  END;
        ELSIF REGEXP_LIKE(V_PY, '^[GSXYCZ][a-zA-Z]*\d+\([EWSN]\)$') THEN

          V_PY := SUBSTR(V_PY, 2, LENGTH(V_PY) - 4) ||
                  CASE SUBSTR(V_PY, 1, 1)
                    WHEN 'G' THEN
                     ' guodao'
                    WHEN 'S' THEN
                     ' shengdao'
                    WHEN 'X' THEN
                     'xiandao'
                    WHEN 'Y' THEN
                     'xiangdao'
                    WHEN 'C' THEN
                     'cundao'
                    WHEN 'Z' THEN
                     'zhuanyongdao'
                  END || CASE SUBSTR(V_PY, -2, 1)
                    WHEN 'E' THEN
                     ' dong '
                    WHEN 'W' THEN
                     ' xi'
                    WHEN 'S' THEN
                     ' nan'
                    WHEN 'N' THEN
                     ' bei'
                  END;
        END IF;
      END IF;
    END IF;

    --转换拼音并增加混淆音分隔符
    IF P_CONVTYPE = '1' OR P_CONVTYPE = '2' THEN
      V_RESULT := CONVERT_PY_TONE(V_HZ, V_PY, P_ADMIN);
      V_RESULT := ADD_CONFUSE_MARK(V_RESULT);

    ELSIF P_CONVTYPE = '3' THEN
      V_RESULT := CONV_TO_ENGLISH_MODE_VOICEFILE(V_HZ, P_ADMIN, '', V_PY);
    END IF;

    --如果转换失败，直接返回错误信息
    IF V_RESULT LIKE 'ERROR%' THEN
      RETURN V_RESULT;
    END IF;

    IF P_CONVTYPE = '1' THEN
      V_RESULT := V_REG_PY || V_RESULT;
    END IF;

    V_RESULT := REPLACE(V_RESULT, '''', '#');
    V_RESULT := REPLACE(V_RESULT, ' ', '');

    /*    --如果分隔符两边是大写英文字母，则删除隔音符
        LOOP
          V_REG_IDX := REGEXP_INSTR(V_RESULT, '#+[A-Z]+');
          EXIT WHEN V_REG_IDX <= 0;
          V_RESULT := SUBSTR(V_RESULT, 1, V_REG_IDX - 1) ||
                      SUBSTR(V_RESULT, V_REG_IDX + 1);
        END LOOP;
        LOOP
          V_REG_IDX := REGEXP_INSTR(V_RESULT, '[A-Z]+#+');
          EXIT WHEN V_REG_IDX <= 0;
          V_RESULT := SUBSTR(V_RESULT, 1, V_REG_IDX) ||
                      SUBSTR(V_RESULT, V_REG_IDX + 2);
        END LOOP;
    */
    --第一个字符大写，其它字符全部小写
    V_RESULT := REPLACE(V_RESULT, '#', '''');
    V_RESULT := UPPER(SUBSTR(V_RESULT, 1, 1)) || LOWER(SUBSTR(V_RESULT, 2));
    RETURN V_RESULT;
  END;

  FUNCTION GET_HZ_MULTI_PY(IS_HZ             IN VARCHAR2,
                           ON_MULTIPY_FLAG   OUT NUMBER,
                           ON_MULTITONE_FLAG OUT NUMBER,
                           IS_REF_PY         IN VARCHAR2 DEFAULT NULL)
    RETURN VARCHAR2 IS

    CURSOR CUR_PY IS
      SELECT PY, PY2, TONE
        FROM TY_NAVICOVPY_PY
       WHERE JT = IS_HZ
       ORDER BY PYORDER;

    VN_CNT         NUMBER;
    VS_PRE_PY      VARCHAR2(16);
    VS_CUR_PY_TONE VARCHAR2(50);
    VS_DEF_PY_TONE VARCHAR2(50);
    VS_RET_PY_TONE VARCHAR2(255);
  BEGIN
    IF IS_HZ IS NULL THEN
      RETURN NULL;
    END IF;

    VN_CNT            := 0;
    ON_MULTIPY_FLAG   := 0;
    ON_MULTITONE_FLAG := 0;
    FOR REC IN CUR_PY LOOP
      IF VN_CNT = 0 THEN
        VS_PRE_PY := REC.PY;
      ELSE
        IF REC.PY = VS_PRE_PY THEN
          ON_MULTITONE_FLAG := 1;
        ELSE
          ON_MULTIPY_FLAG := 1;
        END IF;
      END IF;

      IF C_FORMAT_TONE = 0 THEN
        IF C_PY_FIRST_CHAR_UPPER = 0 THEN
          VS_CUR_PY_TONE := REC.PY;
        ELSE
          VS_CUR_PY_TONE := INITCAP(REC.PY);
        END IF;
      ELSIF C_FORMAT_TONE = 1 THEN
        IF C_PY_FIRST_CHAR_UPPER = 0 THEN
          VS_CUR_PY_TONE := REC.PY2;
        ELSE
          VS_CUR_PY_TONE := INITCAP(REC.PY2);
        END IF;
      ELSE
        IF C_PY_FIRST_CHAR_UPPER = 0 THEN
          VS_CUR_PY_TONE := REC.PY || REC.TONE;
        ELSE
          VS_CUR_PY_TONE := INITCAP(REC.PY) || REC.TONE;
        END IF;
      END IF;

      IF IS_REF_PY IS NOT NULL AND LOWER(IS_REF_PY) = LOWER(VS_CUR_PY_TONE) THEN
        VS_DEF_PY_TONE := VS_CUR_PY_TONE;
      ELSE
        IF VN_CNT = 0 OR C_FORMAT_TONE > 0 OR REC.PY <> VS_PRE_PY THEN
          VS_RET_PY_TONE := VS_RET_PY_TONE || '|' || VS_CUR_PY_TONE;
        END IF;
      END IF;

      VS_PRE_PY := REC.PY;
      VN_CNT    := VN_CNT + 1;
    END LOOP;

    IF VS_DEF_PY_TONE IS NOT NULL AND VS_RET_PY_TONE IS NOT NULL THEN
      VS_RET_PY_TONE := VS_DEF_PY_TONE || VS_RET_PY_TONE;
    ELSIF VS_DEF_PY_TONE IS NOT NULL THEN
      VS_RET_PY_TONE := VS_DEF_PY_TONE;
    ELSIF VS_RET_PY_TONE IS NOT NULL THEN
      VS_RET_PY_TONE := SUBSTR(VS_RET_PY_TONE, 2);
    ELSE
      RAISE E_NO_HZ_FOUND;
    END IF;

    IF VN_CNT > 1 THEN
      VS_RET_PY_TONE := '<' || VS_RET_PY_TONE || '>';
    END IF;

    RETURN VS_RET_PY_TONE;
  END;

  FUNCTION GET_PY_BY_TONE_FLAG(P_PARAM      IN OUT NOCOPY T_COMMON_PARAM,
                               IS_HZ_STR    IN VARCHAR2,
                               IS_PY_STR    IN VARCHAR2 DEFAULT NULL,
                               IS_ADMINCODE IN VARCHAR2 DEFAULT NULL,
                               IN_TONE_FLAG IN NUMBER DEFAULT C_FORMAT_TONE)
    RETURN VARCHAR2 IS
    VN_CURR_TONE_FLAG NUMBER(1) := C_FORMAT_TONE;
    VS_PY_TONE_OUT    VARCHAR2(4000);
  BEGIN
    C_FORMAT_TONE := IN_TONE_FLAG;

    IF IS_PY_STR IS NULL THEN
      VS_PY_TONE_OUT := CONVERTMULTIWORD_BY_HZ(P_PARAM,
                                               IS_HZ_STR,
                                               SUBSTR(IS_ADMINCODE, 1, 2),
                                               SUBSTR(IS_ADMINCODE, 1, 4));
    ELSE
      VS_PY_TONE_OUT := CONVERTMULTIWORD_BY_PY(P_PARAM,
                                               IS_HZ_STR,
                                               IS_PY_STR,
                                               SUBSTR(IS_ADMINCODE, 1, 2),
                                               SUBSTR(IS_ADMINCODE, 1, 4));
    END IF;

    --处理连续空格
    VS_PY_TONE_OUT := CLEAR_MULTISPACE(VS_PY_TONE_OUT);

    C_FORMAT_TONE := VN_CURR_TONE_FLAG;

    RETURN VS_PY_TONE_OUT;
  END;

  FUNCTION CONVERT_HZ_PY_TONE(IS_HZ_STR    IN VARCHAR2,
                              IS_PY_STR    IN VARCHAR2,
                              IS_ADMINCODE VARCHAR2 DEFAULT NULL)
    RETURN PY_CONV_REC IS
    VS_HZ_STR     VARCHAR2(4000) := IS_HZ_STR;
    VS_PY_STR     VARCHAR2(4000) := IS_PY_STR;
    VS_DEF_PY_STR VARCHAR2(4000);
    VS_PY_TONE    VARCHAR2(4000);

    VR_PARAM T_COMMON_PARAM;

    VR_RET_REC PY_CONV_REC;

    VN_MULTIPY_NUM    NUMBER(3);
    VN_MULTIPY_FLAG   NUMBER(1);
    VN_MULTITONE_FLAG NUMBER(1);
    VN_PY_TONE_KIND   NUMBER(1);

    VN_MULTIPY_FLAG_TMP   NUMBER(1);
    VN_MULTITONE_FLAG_TMP NUMBER(1);
  BEGIN
    VR_RET_REC.HZ     := VS_HZ_STR;
    VR_RET_REC.PY_SRC := VS_PY_STR;

    IF VS_HZ_STR IS NULL THEN
      VR_RET_REC.ERR_FLAG := 0;
      VR_RET_REC.ERR_MSG  := 'WARNING!汉字串为NULL';

      RETURN VR_RET_REC;
    END IF;

    VS_HZ_STR := BEFORE_CHECK(VS_HZ_STR);

    INIT_PY_LIST(VR_PARAM, VS_HZ_STR);

    VS_DEF_PY_STR := GET_PY_BY_TONE_FLAG(VR_PARAM,
                                         VS_HZ_STR,
                                         VS_PY_STR,
                                         IS_ADMINCODE);
    IF C_FORMAT_TONE = 0 THEN
      VS_PY_STR := VS_DEF_PY_STR;
    ELSE
      VS_PY_STR := GET_PY_BY_TONE_FLAG(VR_PARAM,
                                       VS_HZ_STR,
                                       VS_PY_STR,
                                       IS_ADMINCODE,
                                       0);
    END IF;

    INIT_DEF_PY_T(VR_PARAM, VS_DEF_PY_STR);

    VN_MULTIPY_NUM    := 0;
    VN_MULTIPY_FLAG   := 0;
    VN_MULTITONE_FLAG := 0;
    FOR I IN 1 .. VR_PARAM.V_TYPE_T.COUNT LOOP
      IF VR_PARAM.V_TYPE_T(I) = 'A' THEN
        VS_PY_TONE := VS_PY_TONE || ' ' ||
                      GET_HZ_MULTI_PY(VR_PARAM.V_HZ_T(I),
                                      VN_MULTIPY_FLAG_TMP,
                                      VN_MULTITONE_FLAG_TMP,
                                      VR_PARAM.V_DEF_PY_T(I));

        IF VN_MULTIPY_FLAG_TMP = 1 THEN
          VN_MULTIPY_NUM  := VN_MULTIPY_NUM + 1;
          VN_MULTIPY_FLAG := 1;
        END IF;

        IF VN_MULTITONE_FLAG_TMP = 1 THEN
          VN_MULTIPY_NUM    := VN_MULTIPY_NUM + 1;
          VN_MULTITONE_FLAG := 1;
        END IF;
      ELSIF IS_PART_OF_STRING(VR_PARAM, I) THEN
        VS_PY_TONE := VS_PY_TONE || VR_PARAM.V_DEF_PY_T(I);
      ELSE
        VS_PY_TONE := VS_PY_TONE || ' ' || VR_PARAM.V_DEF_PY_T(I);
      END IF;
    END LOOP;
    VS_PY_TONE := CLEAR_MULTISPACE(VS_PY_TONE);

    IF VN_MULTIPY_FLAG = 0 AND VN_MULTITONE_FLAG = 0 THEN
      VN_PY_TONE_KIND := 1;
    ELSIF VN_MULTIPY_FLAG = 1 AND VN_MULTITONE_FLAG = 0 THEN
      VN_PY_TONE_KIND := 2;
    ELSIF VN_MULTIPY_FLAG = 0 AND VN_MULTITONE_FLAG = 1 THEN
      VN_PY_TONE_KIND := 3;
    ELSE
      VN_PY_TONE_KIND := 4;
    END IF;

    VR_RET_REC.PY           := VS_PY_STR;
    VR_RET_REC.PY_TONE_STR  := VS_PY_TONE;
    VR_RET_REC.PY_TONE_REF  := VS_DEF_PY_STR;
    VR_RET_REC.MULTIPY_NUM  := VN_MULTIPY_NUM;
    VR_RET_REC.PY_TONE_KIND := VN_PY_TONE_KIND;
    VR_RET_REC.ERR_FLAG     := 0;
    VR_RET_REC.ERR_MSG      := 'SUCCESS!';

    RETURN VR_RET_REC;
  EXCEPTION
    WHEN E_HZ_PY_NOT_MATCHING THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        VR_RET_REC.HZ       := VS_HZ_STR;
        VR_RET_REC.ERR_FLAG := 1;
        VR_RET_REC.ERR_MSG  := 'ERROR!汉字和拼音不匹配，请检查。';
      ELSE
        VR_RET_REC.HZ       := VS_HZ_STR;
        VR_RET_REC.ERR_FLAG := 1;
        VR_RET_REC.ERR_MSG  := 'ERROR!';
      END IF;

      RETURN VR_RET_REC;
    WHEN E_NO_HZ_FOUND THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        VR_RET_REC.HZ       := VS_HZ_STR;
        VR_RET_REC.ERR_FLAG := 1;
        VR_RET_REC.ERR_MSG  := 'ERROR!没有找到汉字或没有对应的拼音，请检查。';
      ELSE
        VR_RET_REC.HZ       := VS_HZ_STR;
        VR_RET_REC.ERR_FLAG := 1;
        VR_RET_REC.ERR_MSG  := 'ERROR!';
      END IF;

      RETURN VR_RET_REC;
    WHEN OTHERS THEN
      IF C_LOG_DETAIL_YN = 1 THEN
        VR_RET_REC.HZ       := VS_HZ_STR;
        VR_RET_REC.ERR_FLAG := 1;
        VR_RET_REC.ERR_MSG  := 'ERROR!' || SQLERRM;
      ELSE
        VR_RET_REC.HZ       := VS_HZ_STR;
        VR_RET_REC.ERR_FLAG := 1;
        VR_RET_REC.ERR_MSG  := 'ERROR!';
      END IF;

      RETURN VR_RET_REC;
  END CONVERT_HZ_PY_TONE;

  FUNCTION CONVERT_HZ_PY_TONE_PIPELINED(IC_CURSOR IN TYPEREFCURSOR)
    RETURN PY_CONV_REST_TAB
    PIPELINED
    PARALLEL_ENABLE(PARTITION IC_CURSOR BY ANY) IS
    VN_PID       VARCHAR2(4000);
    VS_HZ_STR    VARCHAR2(4000);
    VS_PY_STR    VARCHAR2(4000);
    VS_ADMINCODE VARCHAR2(6);

    VR_RET_REC PY_CONV_REC;
  BEGIN
    LOOP
      FETCH IC_CURSOR
        INTO VN_PID, VS_HZ_STR, VS_PY_STR, VS_ADMINCODE;
      EXIT WHEN IC_CURSOR%NOTFOUND;

      VR_RET_REC     := CONVERT_HZ_PY_TONE(VS_HZ_STR,
                                           VS_PY_STR,
                                           VS_ADMINCODE);
      VR_RET_REC.PID := VN_PID;

      PIPE ROW(VR_RET_REC);
    END LOOP;
  END;

  ---- 道路名拆分调用    基本名转英文； add by zhangjin

  FUNCTION CONVERT2NUM(P_SOURCE VARCHAR2) RETURN NUMBER
    PARALLEL_ENABLE IS

  BEGIN
    IF P_SOURCE IS NULL THEN
      RETURN 0;
    END IF;

    IF REGEXP_LIKE(P_SOURCE, '[[:digit:]]') THEN
      RETURN TO_SINGLE_BYTE(P_SOURCE);
    END IF;

    IF P_SOURCE = '一' THEN
      RETURN 1;
    ELSIF P_SOURCE = '二' THEN
      RETURN 2;
    ELSIF P_SOURCE = '三' THEN
      RETURN 3;
    ELSIF P_SOURCE = '四' THEN
      RETURN 4;
    ELSIF P_SOURCE = '五' THEN
      RETURN 5;
    ELSIF P_SOURCE = '六' THEN
      RETURN 6;
    ELSIF P_SOURCE = '七' THEN
      RETURN 7;
    ELSIF P_SOURCE = '八' THEN
      RETURN 8;
    ELSIF P_SOURCE = '九' THEN
      RETURN 9;
    ELSIF P_SOURCE = '零' THEN
      RETURN 0;
    ELSIF P_SOURCE = '十' THEN
      RETURN 10;
    ELSIF P_SOURCE = '百' THEN
      RETURN 100;
    ELSIF P_SOURCE = '千' THEN
      RETURN 1000;
    ELSIF P_SOURCE = '万' THEN
      RETURN 10000;
    ELSIF P_SOURCE = '亿' THEN
      RETURN 100000000;
    END IF;

    RETURN 0;
  END CONVERT2NUM;

  FUNCTION CHAR_IS_NUMBER(P_SOURCE VARCHAR2, FLAG NUMBER DEFAULT 0)
    RETURN BOOLEAN
    PARALLEL_ENABLE IS
  BEGIN

    IF P_SOURCE IS NULL THEN
      RETURN FALSE;
    END IF;

    IF REGEXP_LIKE(P_SOURCE, '^[[:digit:]]$') THEN
      RETURN TRUE;
    END IF;

    IF REGEXP_LIKE(P_SOURCE, '^[一二三四五六七八九零]$') THEN
      RETURN TRUE;
    END IF;

    IF FLAG = 0 AND REGEXP_LIKE(P_SOURCE, '^[十百千万亿]$') THEN
      RETURN TRUE;
    END IF;

    RETURN FALSE;
  END CHAR_IS_NUMBER;

  FUNCTION STR_IS_NUMBER(P_SOURCE VARCHAR2) RETURN BOOLEAN
    PARALLEL_ENABLE IS
    V_FLAG NUMBER := 0;
  BEGIN
    IF P_SOURCE IS NULL THEN
      RETURN FALSE;
    END IF;

    FOR I IN 1 .. LENGTH(P_SOURCE) LOOP
      IF CHAR_IS_NUMBER(SUBSTR(P_SOURCE, I, 1), 1) THEN
        V_FLAG := 1;
      END IF;

      IF NOT CHAR_IS_NUMBER(SUBSTR(P_SOURCE, I, 1), 0) THEN
        RETURN FALSE;
      END IF;
    END LOOP;

    IF V_FLAG = 1 OR P_SOURCE = '十' THEN
      RETURN TRUE;
    ELSE
      RETURN FALSE;
    END IF;
  END STR_IS_NUMBER;

  FUNCTION SPLIT_NAME(P_SOURCE IN VARCHAR2,
                      P_FIRST  OUT VARCHAR2,
                      P_MID    OUT VARCHAR2,
                      P_LAST   OUT VARCHAR2) RETURN BOOLEAN
    PARALLEL_ENABLE IS
    V_STR VARCHAR2(10);
  BEGIN

    P_FIRST := '';
    P_MID   := '';
    P_LAST  := '';
    IF P_SOURCE IS NULL THEN
      RETURN FALSE;
    END IF;

    FOR I IN 1 .. LENGTH(P_SOURCE) LOOP
      V_STR := SUBSTR(P_SOURCE, I, 1);
      IF V_STR = '第' THEN
        IF P_MID IS NULL AND
          /*REGEXP_LIKE(SUBSTR(P_SOURCE, I + 1, 1), '[[:digit:]]')*/
           CHAR_IS_NUMBER(SUBSTR(P_SOURCE, I + 1, 1)) THEN
          P_MID := V_STR;
        ELSE
          IF P_MID IS NULL THEN
            P_FIRST := P_FIRST || V_STR;
          ELSE
            IF REGEXP_LIKE(P_MID, '[第号號]') THEN
              P_LAST := P_LAST || V_STR;
            ELSE
              IF /*REGEXP_LIKE(SUBSTR(P_SOURCE, I + 1, 1), '[[:digit:]]')*/
               CHAR_IS_NUMBER(SUBSTR(P_SOURCE, I + 1, 1)) THEN
                P_FIRST := P_FIRST || P_MID || P_LAST;
                P_MID   := V_STR;
                P_LAST  := '';
              ELSE
                P_LAST := P_LAST || V_STR;
              END IF;
            END IF;
          END IF;
        END IF;
      ELSIF REGEXP_LIKE(V_STR, '[号號]') THEN
        IF P_MID IS NOT NULL AND
          /*REGEXP_LIKE(SUBSTR(P_SOURCE, I - 1, 1), '[[:digit:]]')*/
           CHAR_IS_NUMBER(SUBSTR(P_SOURCE, I - 1, 1)) AND P_LAST IS NULL THEN
          P_MID := P_MID || V_STR;
        ELSE
          IF P_MID IS NULL THEN
            P_FIRST := P_FIRST || V_STR;
          ELSE
            P_LAST := P_LAST || V_STR;
          END IF;
        END IF;
      ELSIF /*REGEXP_LIKE(V_STR, '[[:digit:]]')*/
       CHAR_IS_NUMBER(V_STR) THEN
        IF P_LAST IS NULL AND
           (P_MID IS NULL OR NOT REGEXP_LIKE(SUBSTR(P_MID, -1, 1), '[号號]')) THEN
          --/*(SUBSTR(P_MID, -1, 1) <> '号' AND SUBSTR(P_MID, -1, 1) <> '号')*/
          P_MID := P_MID || V_STR;
        ELSE
          IF P_MID IS NULL THEN
            P_FIRST := P_FIRST || V_STR;
          ELSE
            IF REGEXP_LIKE(P_MID, '[第号號]') THEN
              P_LAST := P_LAST || V_STR;
            ELSE
              P_FIRST := P_FIRST || P_MID || P_LAST;
              P_MID   := V_STR;
              P_LAST  := '';
            END IF;
          END IF;
        END IF;
      ELSE
        IF P_MID IS NULL THEN
          P_FIRST := P_FIRST || V_STR;
        ELSE
          P_LAST := P_LAST || V_STR;
        END IF;
      END IF;
    END LOOP;

    IF P_MID IS NOT NULL AND REGEXP_LIKE(P_MID, '[第号號]') THEN
      RETURN TRUE;
    ELSE
      RETURN FALSE;
    END IF;
  END SPLIT_NAME;

  ------ 最大支持万亿
  FUNCTION CONVERTNUMBSTR2NUM(P_SOURCE VARCHAR2) RETURN NUMBER
    PARALLEL_ENABLE IS
    V_STR VARCHAR2(10);
    V_RET NUMBER := 0;
    V_NUM NUMBER := 0;
    V_N   NUMBER := 0;
  BEGIN
    IF P_SOURCE IS NULL THEN
      RETURN 0;
    END IF;

    IF P_SOURCE = '十' THEN
      RETURN 10;
    END IF;

    FOR I IN 1 .. LENGTH(P_SOURCE) LOOP
      V_STR := SUBSTR(P_SOURCE, I, 1);
      IF V_STR = '亿' THEN
        V_NUM := V_NUM + V_N;
        V_RET := V_RET + V_NUM;
        V_RET := V_RET * CONVERT2NUM(V_STR);
        V_N   := 0;
        V_NUM := 0;
        CONTINUE;
      END IF;

      IF V_STR = '万' THEN
        V_NUM := V_NUM + V_N;
        V_RET := V_NUM * CONVERT2NUM(V_STR) + V_RET;
        V_NUM := 0;
        V_N   := 0;
        CONTINUE;
      END IF;

      IF CHAR_IS_NUMBER(V_STR, 1) THEN
        V_N := V_N || CONVERT2NUM(V_STR);
      ELSIF CHAR_IS_NUMBER(V_STR) THEN
        IF V_STR = '十' AND V_N = 0 THEN
          V_N := 1;
        END IF;
        V_NUM := V_NUM + V_N * CONVERT2NUM(V_STR);
        V_N   := 0;
      END IF;

    END LOOP;

    V_RET := V_RET + V_NUM + V_N;

    RETURN V_RET;

  END CONVERTNUMBSTR2NUM;

  FUNCTION CONVERT_BASE_ENG(P_BASENAME VARCHAR2,
                            P_BASEPY   VARCHAR2,
                            P_TYPE     VARCHAR2,
                            P_LANGCODE VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE IS

    V_RET VARCHAR2(4000);
    V_TMP VARCHAR2(4000);

    V_BASE      VARCHAR2(4000);
    V_PY        VARCHAR2(4000);
    V_FIRST     VARCHAR2(4000);
    V_FIRST_PY  VARCHAR2(4000);
    V_FIRST_ENG VARCHAR2(4000);
    V_LAST      VARCHAR2(4000);
    V_LAST_PY   VARCHAR2(4000);
    V_LAST_ENG  VARCHAR2(4000);
    V_TYPE      VARCHAR2(4000);
    V_TYPE_ENG  VARCHAR2(4000);
    V_TYPE_PY   VARCHAR2(4000);
  BEGIN

    IF LENGTH(P_BASENAME) = 2 THEN

      IF P_TYPE = '*' THEN
        BEGIN
          SELECT T.ENGLISHNAME
            INTO V_TMP
            FROM SC_ROADNAME_TYPENAME T
           WHERE T.NAME = SUBSTR(P_BASENAME, 2, 1)
             AND T.LANG_CODE = P_LANGCODE;
        EXCEPTION
          WHEN OTHERS THEN
            V_TMP := '';
        END;

        IF V_TMP IS NULL THEN
          RETURN CONVERT_ENG(P_BASENAME, P_BASEPY);
        ELSE
          RETURN CONVERT_ENG(P_BASENAME, P_BASEPY) || ' ' || V_TMP;
        END IF;
      ELSE
        RETURN CONVERT_ENG(P_BASENAME, P_BASEPY);
      END IF;
    END IF;

    IF LENGTH(P_BASENAME) = 3 THEN
      IF P_TYPE = '*' THEN
        BEGIN
          SELECT T.ENGLISHNAME
            INTO V_TMP
            FROM SC_ROADNAME_TYPENAME T
           WHERE T.NAME = SUBSTR(P_BASENAME, 3, 1)
             AND T.LANG_CODE = P_LANGCODE;
        EXCEPTION
          WHEN OTHERS THEN
            V_TMP := '';
        END;
        IF V_TMP IS NULL THEN
          IF SUBSTR(P_BASENAME, 3, 1) = '段' THEN
            RETURN CONVERT_ENG(SUBSTR(P_BASENAME, 1, 2),
                               SUBSTR(P_BASEPY,
                                      1,
                                      INSTR(P_BASEPY, ' ', -1) - 1)) || ' Section';
          ELSE
            RETURN TRIM(CONVERT_ENG(P_BASENAME, P_BASEPY));
          END IF;
        ELSE
          RETURN TRIM(CONVERT_ENG(SUBSTR(P_BASENAME, 1, 2),
                                  SUBSTR(P_BASEPY,
                                         1,
                                         INSTR(P_BASEPY, ' ', -1) - 1)) || ' ' ||
                      V_TMP);
        END IF;
      ELSE
        RETURN TRIM(CONVERT_ENG(P_BASENAME, P_BASEPY));
      END IF;
    END IF;

    IF SUBSTR(P_BASENAME, LENGTH(P_BASENAME) - 1) = '东南' THEN
      V_BASE := SUBSTR(P_BASENAME, 1, LENGTH(P_BASENAME) - 2);
      V_PY   := SUBSTR(P_BASEPY, 1, INSTR(P_BASEPY, ' ', -1, 2) - 1);
      V_RET  := ' Southeast' || V_RET;
    ELSIF SUBSTR(P_BASENAME, LENGTH(P_BASENAME) - 1) = '东北' THEN
      V_BASE := SUBSTR(P_BASENAME, 1, LENGTH(P_BASENAME) - 2);
      V_PY   := SUBSTR(P_BASEPY, 1, INSTR(P_BASEPY, ' ', -1, 2) - 1);
      V_RET  := ' Northeast' || V_RET;
    ELSIF SUBSTR(P_BASENAME, LENGTH(P_BASENAME) - 1) = '西南' THEN
      V_BASE := SUBSTR(P_BASENAME, 1, LENGTH(P_BASENAME) - 2);
      V_PY   := SUBSTR(P_BASEPY, 1, INSTR(P_BASEPY, ' ', -1, 2) - 1);
      V_RET  := ' Southwest' || V_RET;
    ELSIF SUBSTR(P_BASENAME, LENGTH(P_BASENAME) - 1) = '西北' THEN
      V_BASE := SUBSTR(P_BASENAME, 1, LENGTH(P_BASENAME) - 2);
      V_PY   := SUBSTR(P_BASEPY, 1, INSTR(P_BASEPY, ' ', -1, 2) - 1);
      V_RET  := ' Northwest' || V_RET;
    ELSE
      IF SUBSTR(P_BASENAME, LENGTH(P_BASENAME), 1) = '段' THEN
        V_BASE := SUBSTR(P_BASENAME, 1, LENGTH(P_BASENAME) - 1);
        V_PY   := SUBSTR(P_BASEPY, 1, INSTR(P_BASEPY, ' ', -1) - 1);
        V_RET  := ' Section';
        V_TYPE := '段';
      ELSIF SUBSTR(P_BASENAME, LENGTH(P_BASENAME) - 1, 2) = '环岛' THEN
        V_BASE := SUBSTR(P_BASENAME, 1, LENGTH(P_BASENAME) - 2);
        V_PY   := SUBSTR(P_BASEPY, 1, INSTR(P_BASEPY, ' ', -1, 2) - 1);
        V_RET  := ' Roundabout';
        V_TYPE := '环岛';
      ELSE
        V_BASE := P_BASENAME;
        V_PY   := P_BASEPY;
        V_TYPE := '';
      END IF;
    END IF;

    IF LENGTH(V_BASE) < 3 THEN
      RETURN CONVERT_ENG(V_BASE, V_PY) || ' ' || V_RET;
    END IF;

    IF V_TYPE IS NOT NULL THEN
      IF SUBSTR(V_BASE, LENGTH(V_BASE) - 1) = '东南' THEN
        V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 2);
        V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1, 2) - 1);
        V_RET  := ' Southeast' || V_RET;
      ELSIF SUBSTR(V_BASE, LENGTH(V_BASE) - 1) = '东北' THEN
        V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 2);
        V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1, 2) - 1);
        V_RET  := ' Northeast' || V_RET;
      ELSIF SUBSTR(V_BASE, LENGTH(V_BASE) - 1) = '西南' THEN
        V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 2);
        V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1, 2) - 1);
        V_RET  := ' Southwest' || V_RET;
      ELSIF SUBSTR(V_BASE, LENGTH(V_BASE) - 1) = '西北' THEN
        V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 2);
        V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1, 2) - 1);
        V_RET  := ' Northwest' || V_RET;
      ELSE
        IF SUBSTR(V_BASE, LENGTH(V_BASE)) = '东' THEN
          V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 1);
          V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1) - 1);
          V_RET  := ' East' || V_RET;
        ELSIF SUBSTR(V_BASE, LENGTH(V_BASE)) = '北' THEN
          V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 1);
          V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1) - 1);
          V_RET  := ' North' || V_RET;
        ELSIF SUBSTR(V_BASE, LENGTH(V_BASE)) = '南' THEN
          V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 1);
          V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1) - 1);
          V_RET  := ' South' || V_RET;
        ELSIF SUBSTR(V_BASE, LENGTH(V_BASE)) = '西' THEN
          V_BASE := SUBSTR(V_BASE, 1, LENGTH(V_BASE) - 1);
          V_PY   := SUBSTR(V_PY, 1, INSTR(V_PY, ' ', -1) - 1);
          V_RET  := ' West' || V_RET;
        END IF;
      END IF;
    END IF;

    IF LENGTH(V_BASE) < 3 THEN
      RETURN CONVERT_ENG(V_BASE, V_PY) || ' ' || V_RET;
    END IF;

    ---- 通过类型名 把基本名拆分成三部分   FIRST   TYPE   LAST
    BEGIN
      WITH T AS
       (SELECT S.NAME, S.PY, S.ENGLISHNAME, S.LANG_CODE
          FROM SC_ROADNAME_TYPENAME S
        UNION ALL
        SELECT '段', 'Duan', 'Section', 'CHI'
          FROM DUAL
        UNION ALL
        SELECT '环岛', 'Huan Dao', 'Roundabout', 'CHI' FROM DUAL)
      SELECT P.NAME, P.ENGLISHNAME, P.PY
        INTO V_TYPE, V_TYPE_ENG, V_TYPE_PY
        FROM (SELECT T.NAME, T.ENGLISHNAME, T.PY
                FROM T
               WHERE INSTR(V_BASE, T.NAME, -1) > 0
                 AND T.LANG_CODE = P_LANGCODE
                 AND T.NAME <> '环'
               ORDER BY LENGTH(V_BASE) - INSTR(V_BASE, T.NAME, -1) -
                        LENGTH(T.NAME) + 2,
                        LENGTH(T.NAME) DESC) P
       WHERE ROWNUM = 1;

    EXCEPTION
      WHEN OTHERS THEN
        V_TYPE     := '';
        V_TYPE_ENG := '';
        V_TYPE_PY  := '';
    END;

    /*    IF V_TYPE IS NULL THEN
      IF INSTR(V_BASE, '段', -1) > 0 THEN
        V_TYPE     := '段';
        V_TYPE_ENG := ' Section';
        V_TYPE_PY  := 'Duan';
      ELSIF INSTR(V_BASE, '环岛', -1) > 0 THEN
        V_TYPE     := '环岛';
        V_TYPE_ENG := 'Roundabout';
        V_TYPE_PY  := 'Huan Dao';
      END IF;
    END IF;*/

    IF V_TYPE IS NULL THEN
      RETURN TRIM(CONVERT_ENG(V_BASE, V_PY) || V_RET);
    ELSE
      V_FIRST    := SUBSTR(V_BASE, 1, INSTR(V_BASE, V_TYPE, -1) - 1);
      V_FIRST_PY := SUBSTR(V_PY, 1, INSTR(V_PY, V_TYPE_PY, -1) - 1);
      V_LAST     := SUBSTR(V_BASE,
                           INSTR(V_BASE, V_TYPE, -1) + LENGTH(V_TYPE));
      V_LAST_PY  := SUBSTR(V_PY,
                           INSTR(V_PY, V_TYPE_PY, -1) + LENGTH(V_TYPE_PY) + 1);

      V_FIRST_PY := TRIM(V_FIRST_PY);
      V_LAST_PY  := TRIM(V_LAST_PY);

      IF LENGTH(V_FIRST) = 1 THEN
        RETURN TRIM(CONVERT_ENG(V_BASE, V_PY) || V_RET);
      END IF;

      ----拆分 类型名前部分的后缀
      IF V_FIRST IS NOT NULL AND LENGTH(V_FIRST) > 2 THEN
        BEGIN
          SELECT T.ENGLISHNAME
            INTO V_TMP
            FROM SC_ROADNAME_INFIX T
           WHERE V_FIRST LIKE '%' || T.NAME
             AND T.LANG_CODE = P_LANGCODE;
        EXCEPTION
          WHEN OTHERS THEN
            V_TMP := '';
        END;

        IF V_TMP IS NOT NULL THEN
          V_FIRST := SUBSTR(V_FIRST, 1, LENGTH(V_FIRST) - 1);
          IF INSTR(V_FIRST_PY, ' ', -1) > 0 THEN
            V_FIRST_PY := SUBSTR(V_FIRST_PY,
                                 1,
                                 INSTR(V_FIRST_PY, ' ', -1) - 1);
          END IF;
          V_FIRST_ENG := ' ' || V_TMP;
        END IF;
      END IF;
      ----拆分 类型名后部分的前缀
      IF V_LAST IS NOT NULL THEN
        BEGIN
          SELECT T.ENGLISHNAME
            INTO V_TMP
            FROM SC_ROADNAME_INFIX T
           WHERE V_LAST LIKE T.NAME || '%'
             AND T.LANG_CODE = P_LANGCODE;
        EXCEPTION
          WHEN OTHERS THEN
            V_TMP := '';
        END;

        IF V_TMP IS NOT NULL THEN
          V_LAST     := SUBSTR(V_LAST, 2);
          V_LAST_PY  := SUBSTR(V_LAST_PY, INSTR(V_LAST_PY, ' ', 1) + 1);
          V_LAST_ENG := ' ' || V_TMP;
        END IF;
      END IF;

      IF V_LAST IS NOT NULL AND STR_IS_NUMBER(V_LAST) THEN
        V_RET := TRIM(CONVERT_ENG(V_FIRST, V_FIRST_PY) || V_FIRST_ENG || ' ' ||
                      V_TYPE_ENG || V_LAST_ENG || ' ' ||
                      CONVERTNUMBSTR2NUM(V_LAST) || V_RET);
      ELSE

        V_RET := TRIM(CONVERT_ENG(V_FIRST, V_FIRST_PY) || V_FIRST_ENG || ' ' ||
                      V_TYPE_ENG || V_LAST_ENG || CASE
                        WHEN CONVERT_ENG(V_LAST, V_LAST_PY) IS NOT NULL THEN
                         ' ' || CONVERT_ENG(V_LAST, V_LAST_PY)
                      END || V_RET);
      END IF;
      RETURN REPLACE(REPLACE(V_RET, '( ', '('), ' )', ')');
    END IF;

  END CONVERT_BASE_ENG;

  FUNCTION CONVERT_ENG(P_BASENAME VARCHAR2, P_BASEPY VARCHAR2)
    RETURN VARCHAR2
    PARALLEL_ENABLE IS

    V_BASE VARCHAR2(4000);
    V_PY   VARCHAR2(4000);

    V_FIRST VARCHAR2(4000);
    V_MID   VARCHAR2(4000);
    V_LAST  VARCHAR2(4000);

    V_RET VARCHAR2(4000);

  BEGIN
    V_BASE := TRIM(P_BASENAME);
    V_PY   := TRIM(P_BASEPY);

    IF SPLIT_NAME(V_BASE, V_FIRST, V_MID, V_LAST) THEN
      IF SUBSTR(V_MID, 1, 1) = '第' THEN
        V_MID := SUBSTR(V_MID, 2);
      END IF;

      IF REGEXP_LIKE(V_MID, '[号號]$') THEN
        V_MID := SUBSTR(V_MID, 1, LENGTH(V_MID) - 1);
      END IF;

      V_RET := GETENG_NAME_PY(V_FIRST, V_PY) || ' No.' ||
               CONVERTNUMBSTR2NUM(V_MID) || ' ' ||
               GETENG_NAME_PY_REV(V_LAST, V_PY);
    ELSE
      V_RET := GETENG_NAME_PY(P_BASENAME, P_BASEPY);
    END IF;

    RETURN TRIM(V_RET);
  END CONVERT_ENG;

  FUNCTION GETENG_NAME_PY(P_NAME VARCHAR2, P_PY VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_RET    VARCHAR2(4000);
    V_HZ     VARCHAR2(4000);
    V_PY     VARCHAR2(4000);
    V_STR    VARCHAR2(4000);
    V_NUM    VARCHAR2(4000);
    V_NAMEPY VARCHAR2(4000);
  BEGIN
    IF P_NAME IS NULL THEN
      RETURN '';
    END IF;

    V_NAMEPY := TRIM(P_PY);
    V_NAMEPY := REPLACE(V_NAMEPY, '.', ' . ');
    V_NAMEPY := REPLACE(V_NAMEPY, '  ', ' ');
    V_NAMEPY := TRIM(V_NAMEPY);
    FOR I IN 1 .. LENGTH(P_NAME) LOOP
      V_STR := SUBSTR(P_NAME, I, 1);
      IF REGEXP_LIKE(V_STR, '[0-9a-zA-Z０-９ａ-ｚＡ-Ｚ]') THEN
        IF V_HZ IS NOT NULL THEN
          V_PY  := TRIM(V_PY);
          V_RET := V_RET ||
                   PY_UTILS_WORD.CONVERT_TO_ENGLISH_MODE(V_HZ, '', '', V_PY) || ' ';
          V_HZ  := '';
          V_PY  := '';
        END IF;

        V_NUM := V_NUM || V_STR;
      ELSE
        IF V_NUM IS NOT NULL THEN

          V_RET    := V_RET ||
                      SUBSTR(V_NAMEPY, 1, INSTR(V_NAMEPY, ' ', 1) - 1) || ' ';
          V_NAMEPY := SUBSTR(V_NAMEPY, INSTR(V_NAMEPY, ' ', 1) + 1);
          V_NAMEPY := TRIM(V_NAMEPY);
          V_NUM    := '';
        END IF;
        V_HZ := V_HZ || V_STR;
        IF INSTR(V_NAMEPY, ' ', 1) = 0 THEN
          V_PY := V_PY || V_NAMEPY || ' ';
        ELSE
          V_PY := V_PY || SUBSTR(V_NAMEPY, 1, INSTR(V_NAMEPY, ' ', 1) - 1) || ' ';
        END IF;
        V_NAMEPY := SUBSTR(V_NAMEPY, INSTR(V_NAMEPY, ' ', 1) + 1);
        V_NAMEPY := TRIM(V_NAMEPY);

      END IF;
    END LOOP;

    IF V_HZ IS NOT NULL THEN
      V_PY  := TRIM(V_PY);
      V_RET := V_RET ||
               PY_UTILS_WORD.CONVERT_TO_ENGLISH_MODE(V_HZ, '', '', V_PY) || ' ';
    END IF;

    IF V_NUM IS NOT NULL THEN
      IF INSTR(V_NAMEPY, ' ', 1) = 0 THEN
        V_RET := V_RET || V_NAMEPY || ' ';
      ELSE
        V_RET := V_RET || SUBSTR(V_NAMEPY, 1, INSTR(V_NAMEPY, ' ', 1) - 1) || ' ';
      END IF;
    END IF;

    V_RET := TRIM(V_RET);

    V_RET := REPLACE(V_RET, 'No . ', 'No.');
    V_RET := REPLACE(V_RET, 'no . ', 'no.');
    V_RET := REPLACE(V_RET, 'NO . ', 'NO.');
    V_RET := REPLACE(V_RET, 'nO . ', 'nO.');

    RETURN V_RET;
  END GETENG_NAME_PY;

  FUNCTION GETENG_NAME_PY_REV(P_NAME VARCHAR2, P_PY VARCHAR2) RETURN VARCHAR2
    PARALLEL_ENABLE IS
    V_RET    VARCHAR2(4000);
    V_HZ     VARCHAR2(4000);
    V_PY     VARCHAR2(4000);
    V_STR    VARCHAR2(4000);
    V_NUM    VARCHAR2(4000);
    V_NAMEPY VARCHAR2(4000);
  BEGIN
    IF P_NAME IS NULL THEN
      RETURN '';
    END IF;

    V_NAMEPY := TRIM(P_PY);
    V_NAMEPY := REPLACE(V_NAMEPY, '.', ' . ');
    V_NAMEPY := REPLACE(V_NAMEPY, '  ', ' ');
    V_NAMEPY := TRIM(V_NAMEPY);

    FOR I IN REVERSE 1 .. LENGTH(P_NAME) LOOP
      V_STR := SUBSTR(P_NAME, I, 1);
      IF REGEXP_LIKE(V_STR, '[0-9a-zA-Z０-９ａ-ｚＡ-Ｚ]') THEN
        IF V_HZ IS NOT NULL THEN
          V_PY  := TRIM(V_PY);
          V_RET := ' ' ||
                   PY_UTILS_WORD.CONVERT_TO_ENGLISH_MODE(V_HZ, '', '', V_PY) ||
                   V_RET;
          V_HZ  := '';
          V_PY  := '';
        END IF;

        V_NUM := V_STR || V_NUM;
      ELSE
        IF V_NUM IS NOT NULL THEN

          V_RET    := ' ' || SUBSTR(V_NAMEPY, INSTR(V_NAMEPY, ' ', -1) + 1) ||
                      V_RET;
          V_NAMEPY := SUBSTR(V_NAMEPY, 1, INSTR(V_NAMEPY, ' ', -1) - 1);
          V_NAMEPY := TRIM(V_NAMEPY);
          V_NUM    := '';
        END IF;
        V_HZ := V_STR || V_HZ;
        IF INSTR(V_NAMEPY, ' ', -1) = 0 THEN
          V_PY := ' ' || V_NAMEPY || V_PY;
        ELSE
          V_PY := ' ' || SUBSTR(V_NAMEPY, INSTR(V_NAMEPY, ' ', -1) + 1) || V_PY;
        END IF;
        V_NAMEPY := SUBSTR(V_NAMEPY, 1, INSTR(V_NAMEPY, ' ', -1) - 1);
        V_NAMEPY := TRIM(V_NAMEPY);
      END IF;
    END LOOP;

    IF V_HZ IS NOT NULL THEN
      V_PY  := TRIM(V_PY);
      V_RET := ' ' ||
               PY_UTILS_WORD.CONVERT_TO_ENGLISH_MODE(V_HZ, '', '', V_PY) ||
               V_RET;
    END IF;

    IF V_NUM IS NOT NULL THEN
      IF INSTR(V_NAMEPY, ' ', -1) = 0 THEN
        V_RET := ' ' || V_NAMEPY || V_RET;
      ELSE
        V_RET := ' ' || SUBSTR(V_NAMEPY, INSTR(V_NAMEPY, ' ', -1) + 1) ||
                 V_RET;
      END IF;
    END IF;

    V_RET := TRIM(V_RET);

    V_RET := REPLACE(V_RET, 'No . ', 'No.');
    V_RET := REPLACE(V_RET, 'no . ', 'no.');
    V_RET := REPLACE(V_RET, 'NO . ', 'NO.');
    V_RET := REPLACE(V_RET, 'nO . ', 'nO.');

    RETURN V_RET;
  END GETENG_NAME_PY_REV;

  -----  add by zhangjin

  PROCEDURE SET_CONTEXT_PARAM(IS_PARAM_NAME  IN VARCHAR2,
                              IS_PARAM_VALUE IN VARCHAR2) AS
  BEGIN
    DBMS_SESSION.SET_CONTEXT(NAMESPACE => 'PY_UTILS_WORD_CTX',
                             ATTRIBUTE => IS_PARAM_NAME,
                             VALUE     => IS_PARAM_VALUE);
  END SET_CONTEXT_PARAM;

  PROCEDURE CLEAR_CONTEXT_PARAM AS
  BEGIN
    DBMS_SESSION.CLEAR_CONTEXT('PY_UTILS_WORD_CTX');
  END CLEAR_CONTEXT_PARAM;

  PROCEDURE INIT_CONTEXT_PARAM IS
  BEGIN
    C_LOG_DETAIL_YN := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                       'C_LOG_DETAIL_YN'),
                           1);

    C_FILTER_SPECIAL_CHARACTERS := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                                   'C_FILTER_SPECIAL_CHARACTERS'),
                                       0);

    C_CONVERT_NUMBER := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                        'C_CONVERT_NUMBER'),
                            0);

    C_CONVERT_ROM_NUMBER := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                            'C_CONVERT_ROM_NUMBER'),
                                0);

    C_PY_FIRST_CHAR_UPPER := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                             'C_PY_FIRST_CHAR_UPPER'),
                                 1);

    C_NUMBER_FIRST_CHAR_UPPER := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                                 'C_NUMBER_FIRST_CHAR_UPPER'),
                                     1);

    C_FORMAT_TONE := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX', 'C_FORMAT_TONE'),
                         0);

    C_REF_KEYWORD_FLAG := NVL(SYS_CONTEXT('PY_UTILS_WORD_CTX',
                                          'C_REF_KEYWORD_FLAG'),
                              1);
  END;

BEGIN
  INIT_CONTEXT_PARAM;

END PY_UTILS_WORD;
/