CREATE TABLE RD_TOLLGATE_tmp
AS
   SELECT * FROM RD_TOLLGATE;

DROP TABLE RD_TOLLGATE CASCADE CONSTRAINTS;

CREATE TABLE RD_TOLLGATE
(
   PID               NUMBER (10) NOT NULL,
   IN_LINK_PID       NUMBER (10) NOT NULL,
   NODE_PID          NUMBER (10) NOT NULL,
   OUT_LINK_PID      NUMBER (10) NOT NULL,
   TYPE              NUMBER (2)
                        DEFAULT 0
                        NOT NULL
                        CHECK
                           (TYPE IN (0,
                                     1,
                                     2,
                                     3,
                                     4,
                                     5,
                                     6,
                                     7,
                                     8,
                                     9,
                                     10)),
   PASSAGE_NUM       NUMBER (2) DEFAULT 0 NOT NULL,
   ETC_FIGURE_CODE   VARCHAR2 (8),
   HW_NAME           VARCHAR2 (1000),
   FEE_TYPE          NUMBER (1)
                        DEFAULT 0
                        NOT NULL
                        CHECK (FEE_TYPE IN (0, 1, 2)),
   FEE_STD           NUMBER (5, 2) DEFAULT 0 NOT NULL,
   SYSTEM_ID         NUMBER (6) DEFAULT 0 NOT NULL,
   LOCATION_FLAG     NUMBER (1)
                        DEFAULT 0
                        NOT NULL
                        CHECK (LOCATION_FLAG IN (0, 1, 2)),
   TRUCK_FLAG        NUMBER (1)
                        DEFAULT 1
                        NOT NULL
                        CHECK (TRUCK_FLAG IN (0, 1)),
   U_RECORD          NUMBER (2)
                        DEFAULT 0
                        NOT NULL
                        CHECK
                           (U_RECORD IN (0,
                                         1,
                                         2,
                                         3)),
   U_FIELDS          VARCHAR2 (1000),
   U_DATE            VARCHAR2 (14),
   ROW_ID            RAW (16)
);

COMMENT ON TABLE RD_TOLLGATE IS
   '(1)表达收费站的收费类型和ETC车道数
(2)注意:进入LINK和退出LINK必须接续';

COMMENT ON COLUMN RD_TOLLGATE.PID IS '主键';

COMMENT ON COLUMN RD_TOLLGATE.IN_LINK_PID IS '外键,引用"RD_LINK"';

COMMENT ON COLUMN RD_TOLLGATE.NODE_PID IS '外键,引用"RD_NODE"';

COMMENT ON COLUMN RD_TOLLGATE.OUT_LINK_PID IS '外键,引用"RD_LINK"';

COMMENT ON COLUMN RD_TOLLGATE.TYPE IS '收费,领卡等类型';

COMMENT ON COLUMN RD_TOLLGATE.PASSAGE_NUM IS
   '包括ETC通道数在内的通道总数';

COMMENT ON COLUMN RD_TOLLGATE.ETC_FIGURE_CODE IS
   '参考"AU_MULTIMEDIA"中"NAME"';

COMMENT ON COLUMN RD_TOLLGATE.HW_NAME IS
   '记录收费站所在的高速名称';

COMMENT ON COLUMN RD_TOLLGATE.FEE_TYPE IS '0 按里程收费
1 固定收费
2 未调查';

COMMENT ON COLUMN RD_TOLLGATE.FEE_STD IS '单位:元/次或元/公里';

COMMENT ON COLUMN RD_TOLLGATE.U_RECORD IS '增量更新标识';

COMMENT ON COLUMN RD_TOLLGATE.U_FIELDS IS
   '记录更新的英文字段名,多个之间采用半角''|''分隔';
   

INSERT INTO RD_TOLLGATE (pid,
                         in_link_pid,
                         node_pid,
                         out_link_pid,
                         TYPE,
                         passage_num,
                         etc_figure_code,
                         hw_name,
                         fee_type,
                         system_id,
                         fee_std,
                         location_flag,
                         u_record,
                         u_fields,
                         u_date,
                         row_id)
   SELECT * FROM RD_TOLLGATE_tmp;


CREATE TABLE RD_BRANCH_DETAIL_tmp
AS
   SELECT * FROM RD_BRANCH_DETAIL;

DROP TABLE rd_branch_detail CASCADE CONSTRAINTS;

CREATE TABLE RD_BRANCH_DETAIL
(
   DETAIL_ID      NUMBER (10) NOT NULL,
   BRANCH_PID     NUMBER (10) NOT NULL,
   BRANCH_TYPE    NUMBER (1)
                     DEFAULT 0
                     NOT NULL
                     CHECK
                        (BRANCH_TYPE IN (0,
                                         1,
                                         2,
                                         3,
                                         4)),
   VOICE_DIR      NUMBER (1)
                     DEFAULT 0
                     NOT NULL
                     CHECK
                        (VOICE_DIR IN (0,
                                       2,
                                       5,
                                       9)),
   ESTAB_TYPE     NUMBER (1)
                     DEFAULT 0
                     NOT NULL
                     CHECK
                        (ESTAB_TYPE IN (0,
                                        1,
                                        2,
                                        3,
                                        4,
                                        5,
                                        9)),
   NAME_KIND      NUMBER (1)
                     DEFAULT 0
                     NOT NULL
                     CHECK
                        (NAME_KIND IN (0,
                                       1,
                                       2,
                                       3,
                                       4,
                                       5,
                                       6,
                                       7,
                                       8,
                                       9)),
   EXIT_NUM       VARCHAR2 (32),
   ARROW_CODE     VARCHAR2 (10),
   PATTERN_CODE   VARCHAR2 (10),
   ARROW_FLAG     NUMBER (2) DEFAULT 0 NOT NULL CHECK (ARROW_FLAG IN (0, 1)),
   GUIDE_CODE     NUMBER (1)
                     DEFAULT 0
                     NOT NULL
                     CHECK
                        (GUIDE_CODE IN (0,
                                        1,
                                        2,
                                        3,
                                        9)),
   GEOMETRY       SDO_GEOMETRY,
   U_RECORD       NUMBER (2)
                     DEFAULT 0
                     NOT NULL
                     CHECK
                        (U_RECORD IN (0,
                                      1,
                                      2,
                                      3)),
   U_FIELDS       VARCHAR2 (1000),
   U_DATE         VARCHAR2 (14),
   ROW_ID         RAW (16),
   CONSTRAINT PK_RD_BRANCH_DETAIL PRIMARY KEY (DETAIL_ID),
   CONSTRAINT RDBRANCH_DETAIL FOREIGN KEY
      (BRANCH_PID)
       REFERENCES RD_BRANCH (BRANCH_PID)
);

COMMENT ON COLUMN RD_BRANCH_DETAIL.DETAIL_ID IS '主键';

COMMENT ON COLUMN RD_BRANCH_DETAIL.BRANCH_PID IS '外键,引用"RD_BRANCH"';

COMMENT ON COLUMN RD_BRANCH_DETAIL.BRANCH_TYPE IS '[180U]';

COMMENT ON COLUMN RD_BRANCH_DETAIL.VOICE_DIR IS '无,右,左';

COMMENT ON COLUMN RD_BRANCH_DETAIL.ESTAB_TYPE IS '出口,入口,SA,PA,JCT等';

COMMENT ON COLUMN RD_BRANCH_DETAIL.NAME_KIND IS
   'IC,SA,PA,JCT,出口,入口等';

COMMENT ON COLUMN RD_BRANCH_DETAIL.ARROW_CODE IS
   '参考"AU_MULTIMEDIA"中"NAME",如:0a24030a';

COMMENT ON COLUMN RD_BRANCH_DETAIL.PATTERN_CODE IS
   '参考"AU_MULTIMEDIA"中"NAME",如:8a430211';

COMMENT ON COLUMN RD_BRANCH_DETAIL.ARROW_FLAG IS '[171A]';

COMMENT ON COLUMN RD_BRANCH_DETAIL.GUIDE_CODE IS
   '高架向导,Underpath向导等';

COMMENT ON COLUMN RD_BRANCH_DETAIL.U_RECORD IS '增量更新标识';

COMMENT ON COLUMN RD_BRANCH_DETAIL.U_FIELDS IS
   '记录更新的英文字段名,多个之间采用半角''|''分隔';


INSERT INTO rd_branch_detail (detail_id,
                              branch_pid,
                              branch_type,
                              voice_dir,
                              estab_type,
                              name_kind,
                              exit_num,
                              arrow_code,
                              pattern_code,
                              arrow_flag,
                              guide_code,
                              u_record,
                              u_fields,
                              u_date,
                              row_id)
   SELECT * FROM RD_BRANCH_DETAIL_tmp;


CREATE TABLE RD_SIGNBOARD_tmp
AS
   SELECT * FROM RD_SIGNBOARD;

DROP TABLE RD_SIGNBOARD CASCADE CONSTRAINTS;

CREATE TABLE RD_SIGNBOARD
(
   SIGNBOARD_ID     NUMBER (10) NOT NULL,
   BRANCH_PID       NUMBER (10) NOT NULL,
   ARROW_CODE       VARCHAR2 (16),
   BACKIMAGE_CODE   VARCHAR2 (16),
   GEOMETRY         SDO_GEOMETRY,
   U_RECORD         NUMBER (2)
                       DEFAULT 0
                       NOT NULL
                       CHECK
                          (U_RECORD IN (0,
                                        1,
                                        2,
                                        3)),
   U_FIELDS         VARCHAR2 (1000),
   U_DATE           VARCHAR2 (14),
   ROW_ID           RAW (16),
   CONSTRAINT PK_RD_SIGNBOARD PRIMARY KEY (SIGNBOARD_ID),
   CONSTRAINT RDBRANCH_SIGNBOARD FOREIGN KEY
      (BRANCH_PID)
       REFERENCES RD_BRANCH (BRANCH_PID)
);

COMMENT ON COLUMN RD_SIGNBOARD.SIGNBOARD_ID IS '主键';

COMMENT ON COLUMN RD_SIGNBOARD.BRANCH_PID IS '外键,引用"RD_BRANCH"';

COMMENT ON COLUMN RD_SIGNBOARD.ARROW_CODE IS '参考"AU_MULTIMEDIA"中"NAME"';

COMMENT ON COLUMN RD_SIGNBOARD.BACKIMAGE_CODE IS
   '同箭头图代码,均为11 位编码';

COMMENT ON COLUMN RD_SIGNBOARD.U_RECORD IS '增量更新标识';

COMMENT ON COLUMN RD_SIGNBOARD.U_FIELDS IS
   '记录更新的英文字段名,多个之间采用半角''|''分隔';


INSERT INTO RD_SIGNBOARD (signboard_id,
                          branch_pid,
                          arrow_code,
                          backimage_code,
                          u_record,
                          u_fields,
                          u_date,
                          row_id)
   SELECT * FROM RD_SIGNBOARD_tmp;


COMMIT;

exit ;