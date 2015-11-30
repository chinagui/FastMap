CREATE TABLE TEMP_PROCEDURE_LOG
            (
              id number (10) ,
              OCCUR_TIME  TIMESTAMP(8)                      DEFAULT current_timestamp(6),
              LVL         VARCHAR2(100 char),
              MESSAGE     VARCHAR2(4000 char),
              caller     VARCHAR2(500 char),
              session_info      VARCHAR2(400 CHAR)
            );