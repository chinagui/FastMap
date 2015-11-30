CREATE OR REPLACE PACKAGE merge_au_pt_platform IS

  -- Author  : MAYF
  -- Created : 2011/12/30 16:44:48
  -- Purpose : 站台融合

  PROCEDURE pre_process;
  PROCEDURE att_add_pt_platform;
  PROCEDURE process_att_modify_log;
  PROCEDURE process_att_pt_platform_mod;
  PROCEDURE process_att_pt_plat_name_mod;
  PROCEDURE process_att_pt_transfer_mod;
  PROCEDURE att_del_pt_platform;
  PROCEDURE geo_add_pt_platform;

END merge_au_pt_platform;
/
CREATE OR REPLACE PACKAGE BODY merge_au_pt_platform IS
  PROCEDURE pre_process IS
  BEGIN
    DELETE FROM temp_au_pt_plt_mul_task;
    INSERT INTO temp_au_pt_plt_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_pt_platform t
               GROUP BY t.pid
              HAVING COUNT(1) > 1) rs;
    DELETE FROM temp_his_pt_platform;
    INSERT INTO temp_his_pt_platform
      SELECT * FROM pt_platform;
    DELETE FROM temp_his_pt_transfer;
    INSERT INTO temp_his_pt_transfer
      SELECT * FROM pt_transfer;
  END;
  PROCEDURE save_pt_modify_log(p_rec TEMP_AU_PT_PLATFORM_MODIFY_LOG%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into TEMP_AU_PT_PLATFORM_MODIFY_LOG(
        PID,
        PLATNAME_FLAG,
        COLLECT_FLAG,
        PLATLEVEL_FLAG,
        TRANSFLAG_FLAG,
        STOPID_FLAG,
        AUDATA_ID,
        LOCATION_FIR_FLAG,
        LOCATION_SEC_FLAG,
        TRANSFER_TYPE_FLAG,
        TRANSFER_TIME_FLAG,
        EXTERNAL_FLAG_FLAG,
       ) values (:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12)'
      USING p_rec.pid, p_rec.platname_flag, p_rec.collect_flag, p_rec.platlevel_flag, p_rec.transflag_flag, p_rec.stopid_flag, p_rec.audata_id, p_rec.location_fir_flag, p_rec.location_sec_flag, p_rec.transfer_type_flag, p_rec.transfer_time_flag, p_rec.external_flag_flag;
  
  END;

  PROCEDURE process_att_modify_log IS
    v_rec         TEMP_AU_PT_PLATFORM_MODIFY_LOG%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM TEMP_AU_PT_PLATFORM_MODIFY_LOG;
    FOR rec IN (SELECT a.audata_id, a.pid, a.log
                  FROM AU_PT_PLATFORM a
                 WHERE a.log IS NOT NULL
                   AND a.state = 2
                   AND a.att_oprstatus = 0
                   AND EXISTS
                 (SELECT 1 FROM pt_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_pt_plt_mul_task tmp
                         WHERE a.pid = tmp.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      IF instr(v_log, '改站台名') > 0 THEN
        v_rec.platname_flag := 1;
        v_change_flag       := TRUE;
      ELSE
        v_rec.platname_flag := 0;
      END IF;
      IF instr(v_log, '改站台标识') > 0 THEN
        v_rec.collect_flag := 1;
        v_change_flag      := TRUE;
      ELSE
        v_rec.collect_flag := 0;
      END IF;
      IF instr(v_log, '改站台层级') > 0 THEN
        v_rec.platlevel_flag := 1;
        v_change_flag        := TRUE;
      ELSE
        v_rec.platlevel_flag := 0;
      END IF;
      IF instr(v_log, '改站台换乘标识') > 0 THEN
        v_rec.transflag_flag := 1;
        v_change_flag        := TRUE;
      ELSE
        v_rec.transflag_flag := 0;
      END IF;
      IF instr(v_log, '改主点编号') > 0 THEN
        v_rec.stopid_flag := 1;
        v_change_flag     := TRUE;
      ELSE
        v_rec.stopid_flag := 0;
      END IF;
      IF instr(v_log, '改换乘点ID1') > 0 THEN
        v_rec.location_fir_flag := 1;
        v_change_flag           := TRUE;
      ELSE
        v_rec.location_fir_flag := 0;
      END IF;
      IF instr(v_log, '改换乘点ID2') > 0 THEN
        v_rec.location_sec_flag := 1;
        v_change_flag           := TRUE;
      ELSE
        v_rec.location_sec_flag := 0;
      END IF;
      IF instr(v_log, '改换乘类型') > 0 THEN
        v_rec.transfer_type_flag := 1;
        v_change_flag            := TRUE;
      ELSE
        v_rec.transfer_type_flag := 0;
      END IF;
      IF instr(v_log, '改换乘时间') > 0 THEN
        v_rec.transfer_time_flag := 1;
        v_change_flag            := TRUE;
      ELSE
        v_rec.transfer_time_flag := 0;
      END IF;
      IF instr(v_log, '改外部标识') > 0 THEN
        v_rec.external_flag_flag := 1;
        v_change_flag            := TRUE;
      ELSE
        v_rec.external_flag_flag := 0;
      END IF;
    
      IF v_change_flag = TRUE THEN
        save_pt_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_att_pt_platform_mod IS
  BEGIN
    MERGE INTO pt_platform pp
    USING (SELECT aupp.pid,
                  aupp.collect,
                  aupp.p_level,
                  aupp.transit_flag,
                  aupp.poi_pid,
                  aupp.state,
                  aupp.log,
                  '外业修改' AS edition_flag,
                  l.collect_flag,
                  l.platlevel_flag,
                  l.transflag_flag,
                  l.stopid_flag
             FROM au_pt_platform aupp, TEMP_AU_PT_PLATFORM_MODIFY_LOG l
            WHERE aupp.audata_id = l.audata_id
              AND (l.collect_flag = 1 OR l.platlevel_flag = 1 OR
                  l.transflag_flag = 1 OR l.stopid_flag = 1)) rs
    ON (pp.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET pp.collect      = decode(rs.collect_flag,
                                      1,
                                      rs.collect,
                                      pp.collect),
             pp.p_level      = decode(rs.platlevel_flag,
                                      1,
                                      rs.p_level,
                                      pp.p_level),
             pp.transit_flag = decode(rs.transflag_flag,
                                      1,
                                      rs.transit_flag,
                                      pp.transit_flag),
             pp.poi_pid      = decode(rs.stopid_flag,
                                      1,
                                      rs.poi_pid,
                                      pp.poi_pid),
             pp.log          = rs.log,
             pp.state        = rs.state,
             pp.edition_flag = rs.edition_flag;
  END;
  PROCEDURE process_att_pt_plat_name_mod IS
  BEGIN
    DELETE FROM pt_platform_name ppn
     WHERE EXISTS (SELECT 1
              FROM TEMP_AU_PT_PLATFORM_MODIFY_LOG l
             WHERE l.platname_flag = 1
               AND l.pid = ppn.pid);
    INSERT INTO pt_platform_name ppn
      SELECT NAME_ID,
             NAME_GROUPID,
             PID,
             LANG_CODE,
             NAME,
             PHONETIC,
             SRC_FLAG,
             0            AS U_RECORD,
             NULL         AS U_FIELDS
        FROM au_pt_platform_name auppn
       WHERE EXISTS (SELECT 1
                FROM TEMP_AU_PT_PLATFORM_MODIFY_LOG l
               WHERE l.platname_flag = 1
                 AND l.pid = auppn.pid);
  END;
  PROCEDURE process_att_pt_transfer_mod IS
  BEGIN
    MERGE INTO pt_transfer pt
    USING (SELECT TRANSFER_ID,
                  TRANSFER_TYPE,
                  TRANSFER_TIME,
                  EXTERNAL_FLAG,
                  OPERATOR,
                  UPDATE_TIME,
                  LOG,
                  '外业修改' AS edition_flag,
                  STATE,
                  POI_FIR,
                  POI_SEC,
                  PLATFORM_FIR,
                  PLATFORM_SEC,
                  l.location_fir_flag,
                  l.location_sec_flag,
                  l.transfer_type_flag,
                  l.transfer_time_flag,
                  l.external_flag_flag
             FROM au_pt_transfer aupt, TEMP_AU_PT_PLATFORM_MODIFY_LOG l
            WHERE aupt.transfer_type = 1
              AND (l.location_fir_flag = 1 OR l.location_sec_flag = 1 OR
                  l.transfer_type_flag = 1 OR l.transfer_time_flag = 1 OR
                  l.external_flag_flag = 1)
              AND (aupt.platform_fir = l.pid OR aupt.poi_sec = l.pid)) rs
    ON (rs.TRANSFER_ID = pt.transfer_id)
    WHEN MATCHED THEN
      UPDATE
         SET pt.transfer_type = decode(rs.transfer_type_flag,
                                       1,
                                       rs.transfer_type,
                                       pt.transfer_type),
             pt.transfer_time = decode(rs.transfer_time_flag,
                                       1,
                                       rs.transfer_time,
                                       pt.transfer_time),
             pt.external_flag = decode(rs.external_flag_flag,
                                       1,
                                       rs.external_flag,
                                       pt.external_flag),
             pt.PLATFORM_FIR  = decode(rs.location_fir_flag,
                                       1,
                                       rs.PLATFORM_FIR,
                                       pt.PLATFORM_FIR),
             pt.PLATFORM_SEC  = decode(rs.location_sec_flag,
                                       1,
                                       rs.PLATFORM_SEC,
                                       pt.PLATFORM_SEC),
             pt.log           = rs.log,
             pt.state         = rs.state,
             pt.edition_flag  = rs.edition_flag;
  END;
  PROCEDURE att_add_pt_platform IS
  BEGIN
    --add AU_PT_PLATFORM
    INSERT INTO pt_platform pp
      SELECT PID,
             POI_PID,
             CITY_CODE,
             COLLECT,
             P_LEVEL,
             TRANSIT_FLAG,
             OPERATOR,
             UPDATE_TIME,
             LOG,
             EDITION_FLAG,
             STATE,
             DATA_SOURCE,
             UPDATE_BATCH,
             NIDB_PLATFORMID   ,
             0             AS TASK_ID,
             DATA_VERSION,
             FIELD_TASK_ID,
             0             AS U_RECORD,
             NULL          AS U_FIELDS
        FROM au_pt_platform aupp
       WHERE aupp.state = 1
         AND aupp.att_oprstatus = 0
         AND NOT EXISTS
       (SELECT 1 FROM pt_platform pp WHERE pp.pid = aupp.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_pt_plt_mul_task tmp
               WHERE tmp.pid = aupp.pid);
    --add AU_PT_PLATFORM_NAME
    INSERT INTO pt_platform_name ppn
      SELECT NAME_ID,
             NAME_GROUPID,
             PID,
             LANG_CODE,
             NAME,
             PHONETIC,
             SRC_FLAG,
             0            AS U_RECORD,
             NULL         AS U_FIELDS
        FROM au_pt_platform_name auppn
       WHERE EXISTS (SELECT 1
                FROM au_pt_platform aupp
               WHERE aupp.state = 1
                 AND aupp.att_oprstatus = 0
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_pt_plt_mul_task tmp
                       WHERE tmp.pid = aupp.pid)
                 AND aupp.pid = auppn.pid);
    --AU_PT_TRANSFER 应该作为主表
    INSERT INTO pt_transfer pt
      (TRANSFER_ID,
       TRANSFER_TYPE,
       TRANSFER_TIME,
       EXTERNAL_FLAG,
       OPERATOR,
       UPDATE_TIME,
       LOG,
       EDITION_FLAG,
       STATE,
       U_RECORD,
       U_FIELDS,
       POI_FIR,
       POI_SEC,
       PLATFORM_FIR,
       PLATFORM_SEC)
      SELECT TRANSFER_ID,
             TRANSFER_TYPE,
             TRANSFER_TIME,
             EXTERNAL_FLAG,
             OPERATOR,
             UPDATE_TIME,
             LOG,
             EDITION_FLAG,
             STATE,
             0             AS U_RECORD,
             NULL          AS U_FIELDS,
             POI_FIR,
             POI_SEC,
             PLATFORM_FIR,
             PLATFORM_SEC
        FROM AU_PT_TRANSFER aupt
       WHERE EXISTS
       (SELECT 1
                FROM au_pt_platform aupp
               WHERE aupp.state = 1
                 AND aupp.att_oprstatus = 0
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_pt_plt_mul_task tmp
                       WHERE tmp.pid = aupp.pid)
                 AND (aupp.pid = aupt.platform_fir OR
                     aupp.pid = aupt.platform_sec));
    --AU_PT_PLATFORM_ACCESS
    INSERT INTO pt_platform_access ppa
      SELECT RELATE_ID,
             PLATFORM_ID,
             ACCESS_ID,
             AVAILABLE,
             STATE,
             0           AS U_RECORD,
             NULL        AS U_FIELDS
        FROM au_pt_platform_access auppa
       WHERE EXISTS
       (SELECT 1
                FROM au_pt_platform aupp
               WHERE aupp.state = 1
                 AND aupp.att_oprstatus = 0
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_pt_plt_mul_task tmp
                       WHERE tmp.pid = aupp.pid)
                 AND aupp.pid = auppa.platform_id);
  END;
  PROCEDURE att_del_pt_platform IS
  BEGIN
    --pt_platform_name  
    DELETE FROM pt_platform_name ppn
     WHERE EXISTS
     (SELECT 1
              FROM au_pt_platform aupp
             WHERE aupp.state = 3
               AND aupp.att_oprstatus = 0
               AND EXISTS
             (SELECT 1 FROM pt_platform pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_pt_plt_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND aupp.pid = ppn.pid);
    --pt_platform_access   
    DELETE FROM pt_platform_access ppa
     WHERE EXISTS
     (SELECT 1
              FROM au_pt_platform aupp
             WHERE aupp.state = 3
               AND aupp.att_oprstatus = 0
               AND EXISTS
             (SELECT 1 FROM pt_platform pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_pt_plt_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND aupp.pid = ppa.platform_id);
    --pt_transfer             
    DELETE FROM pt_transfer pt
     WHERE EXISTS
     (SELECT 1
              FROM au_pt_platform aupp
             WHERE aupp.state = 3
               AND aupp.att_oprstatus = 0
               AND EXISTS
             (SELECT 1 FROM pt_platform pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS
             (SELECT 1
                      FROM temp_au_pt_plt_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND (aupp.pid = pt.platform_fir OR aupp.pid = pt.platform_sec));
    --pt_platform           
    DELETE FROM pt_platform pp
     WHERE EXISTS (SELECT 1
              FROM au_pt_platform aupp
             WHERE aupp.state = 3
               AND aupp.att_oprstatus = 0
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_pt_plt_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND (aupp.pid = pp.pid));
  
  END;
  PROCEDURE geo_add_pt_platform IS
  BEGIN
    --add AU_PT_PLATFORM
    INSERT INTO pt_platform pp
      SELECT PID,
             POI_PID,
             CITY_CODE,
             COLLECT,
             P_LEVEL,
             TRANSIT_FLAG,
             OPERATOR,
             UPDATE_TIME,
             LOG,
             EDITION_FLAG,
             STATE,
             DATA_SOURCE,
             UPDATE_BATCH,
             NIDB_PLATFORMID   ,
             0             AS TASK_ID,
             DATA_VERSION,
             FIELD_TASK_ID,
             0             AS U_RECORD,
             NULL          AS U_FIELDS
        FROM au_pt_platform aupp
       WHERE aupp.state = 1
         AND aupp.geo_oprstatus = 0
         AND NOT EXISTS
       (SELECT 1 FROM pt_platform pp WHERE pp.pid = aupp.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_pt_plt_mul_task tmp
               WHERE tmp.pid = aupp.pid);
    --add AU_PT_PLATFORM_NAME
    INSERT INTO pt_platform_name ppn
      SELECT NAME_ID,
             NAME_GROUPID,
             PID,
             LANG_CODE,
             NAME,
             PHONETIC,
             SRC_FLAG,
             0            AS U_RECORD,
             NULL         AS U_FIELDS
        FROM au_pt_platform_name auppn
       WHERE EXISTS (SELECT 1
                FROM au_pt_platform aupp
               WHERE aupp.state = 1
                 AND aupp.geo_oprstatus = 0
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_pt_plt_mul_task tmp
                       WHERE tmp.pid = aupp.pid)
                 AND aupp.pid = auppn.pid);
    --AU_PT_TRANSFER
    INSERT INTO pt_transfer pt
      (TRANSFER_ID,
       TRANSFER_TYPE,
       TRANSFER_TIME,
       EXTERNAL_FLAG,
       OPERATOR,
       UPDATE_TIME,
       LOG,
       EDITION_FLAG,
       STATE,
       U_RECORD,
       U_FIELDS,
       POI_FIR,
       POI_SEC,
       PLATFORM_FIR,
       PLATFORM_SEC)
      SELECT TRANSFER_ID,
             TRANSFER_TYPE,
             TRANSFER_TIME,
             EXTERNAL_FLAG,
             OPERATOR,
             UPDATE_TIME,
             LOG,
             EDITION_FLAG,
             STATE,
             0             AS U_RECORD,
             NULL          AS U_FIELDS,
             POI_FIR,
             POI_SEC,
             PLATFORM_FIR,
             PLATFORM_SEC
        FROM AU_PT_TRANSFER aupt
       WHERE EXISTS
       (SELECT 1
                FROM au_pt_platform aupp
               WHERE aupp.state = 1
                 AND aupp.geo_oprstatus = 0
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_pt_plt_mul_task tmp
                       WHERE tmp.pid = aupp.pid)
                 AND (aupp.pid = aupt.platform_fir OR
                     aupp.pid = aupt.platform_sec));
    --AU_PT_PLATFORM_ACCESS
    INSERT INTO pt_platform_access ppa
      SELECT RELATE_ID,
             PLATFORM_ID,
             ACCESS_ID,
             AVAILABLE,
             STATE,
             0           AS U_RECORD,
             NULL        AS U_FIELDS
        FROM au_pt_platform_access auppa
       WHERE EXISTS
       (SELECT 1
                FROM au_pt_platform aupp
               WHERE aupp.state = 1
                 AND aupp.geo_oprstatus = 0
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_pt_plt_mul_task tmp
                       WHERE tmp.pid = aupp.pid)
                 AND aupp.pid = auppa.platform_id);
  END;
END merge_au_pt_platform;
/
