CREATE OR REPLACE PACKAGE merge_au_pt_company IS

  PROCEDURE pre_process;
  PROCEDURE att_add_pt_company;
  PROCEDURE process_att_modify_log;
  PROCEDURE process_att_pt_company_mod;
  PROCEDURE process_att_pt_system_mod;

END merge_au_pt_company;
/
CREATE OR REPLACE PACKAGE BODY merge_au_pt_company IS
  PROCEDURE save_pt_modify_log(p_rec TEMP_AU_PT_COMPANY_MODIFY_LOG%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into TEMP_AU_PT_COMPANY_MODIFY_LOG(
        COMPANY_ID ,           
        NAME_FLAG ,        
        NAME_ENG_SHORT_FLAG,         
        NAME_ENG_FULL_FLAG ,
        AUDATA_ID 
       ) values (:1,:2,:3,:4,:5)'
      USING p_rec.Company_Id, p_Rec.Name_Flag, p_rec.name_eng_short_flag, p_rec.name_eng_full_flag, p_rec.audata_id;
  END;
  PROCEDURE pre_process IS
  BEGIN
    DELETE FROM temp_au_ptcom_mul_task;
    INSERT INTO temp_au_ptcom_mul_task
      SELECT company_id
        FROM (SELECT t.company_id, COUNT(1)
                FROM au_pt_company t
               GROUP BY t.company_id
              HAVING COUNT(1) > 1) rs;
    DELETE FROM temp_his_pt_company;
    INSERT INTO temp_his_pt_company
      SELECT * FROM pt_company;
  END;
  PROCEDURE att_add_pt_company IS
  BEGIN
    --pt_company
    INSERT INTO pt_company pc
      SELECT COMPANY_ID,
             NAME,
             PHONETIC,
             NAME_ENG_SHORT,
             NAME_ENG_FULL,
             SRC_FLAG,
             CITY_CODE,
             LOG,
             EDITION_FLAG,
             STATE,
             DATA_SOURCE,
             UPDATE_BATCH,
             NIDB_COMPANYID ,
             0              AS U_RECORD,
             NULL           AS U_FIELDS
        FROM au_pt_company aupc
       WHERE aupc.state = 1
         AND aupc.att_oprstatus = 0
         AND NOT EXISTS
       (SELECT 1 FROM pt_company WHERE company_id = aupc.company_id)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ptcom_mul_task tmp
               WHERE tmp.pid = aupc.company_id);
    --pt_system           
    INSERT INTO pt_system ps
      SELECT SYSTEM_ID,
             COMPANY_ID,
             NAME,
             PHONETIC,
             NAME_ENG_SHORT,
             NAME_ENG_FULL,
             SRC_FLAG,
             CITY_CODE,
             LOG,
             EDITION_FLAG,
             STATE,
             DATA_SOURCE,
             UPDATE_BATCH,
             0              AS U_RECORD,
             NULL           AS U_FIELDS
        FROM au_pt_system aups
       WHERE EXISTS (SELECT 1
                FROM au_pt_company aupc
               WHERE aupc.state = 1
                 AND aupc.att_oprstatus = 0
                 AND NOT EXISTS
               (SELECT 1
                        FROM temp_au_ptcom_mul_task tmp
                       WHERE tmp.pid = aupc.company_id)
                 AND aupc.company_id = aups.company_id);
  
  END;
  PROCEDURE process_att_modify_log IS
    v_rec         TEMP_AU_PT_COMPANY_MODIFY_LOG%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    --au_pt_company
    DELETE FROM TEMP_AU_PT_COMPANY_MODIFY_LOG;
    FOR rec IN (SELECT a.audata_id, a.company_id, a.log
                  FROM AU_PT_COMPANY a
                 WHERE a.log IS NOT NULL
                   AND a.state = 2
                   AND a.att_oprstatus = 0
                   AND EXISTS (SELECT 1
                          FROM pt_company p
                         WHERE a.company_id = p.company_id)
                   AND NOT EXISTS
                 (SELECT 1
                          FROM temp_au_ptcom_mul_task tmp
                         WHERE a.company_id = tmp.pid)) LOOP
      v_change_flag    := FALSE;
      v_rec.audata_id  := rec.audata_id;
      v_rec.company_id := rec.company_id;
      v_log            := upper(rec.log);
      IF instr(v_log, '改公司名称') > 0 THEN
        v_rec.NAME_FLAG := 1;
        v_change_flag   := TRUE;
      ELSE
        v_rec.NAME_FLAG := 0;
      END IF;
      IF instr(v_log, '改公司外文名简称') > 0 THEN
        v_rec.NAME_ENG_SHORT_FLAG := 1;
        v_change_flag             := TRUE;
      ELSE
        v_rec.NAME_ENG_SHORT_FLAG := 0;
      END IF;
      IF instr(v_log, '改公司外文名全称') > 0 THEN
        v_rec.NAME_ENG_FULL_FLAG := 1;
        v_change_flag            := TRUE;
      ELSE
        v_rec.NAME_ENG_FULL_FLAG := 0;
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
  PROCEDURE process_att_pt_company_mod IS
  BEGIN
    MERGE INTO pt_company pc
    USING (SELECT aupc.company_id,
                  aupc.name,
                  aupc.phonetic,
                  aupc.name_eng_short,
                  aupc.name_eng_full,
                  aupc.state,
                  aupc.log,
                  '外业修改' AS edition_flag,
                  l.name_flag,
                  l.name_eng_short_flag,
                  l.name_eng_full_flag
             FROM au_pt_company aupc, TEMP_AU_PT_COMPANY_MODIFY_LOG l
            WHERE aupc.audata_id = l.audata_id) rs
    ON (pc.company_id = rs.company_id)
    WHEN MATCHED THEN
      UPDATE
         SET pc.name           = decode(rs.name_flag, 1, rs.name, pc.name),
             pc.phonetic       = decode(rs.name_flag,
                                        1,
                                        rs.phonetic,
                                        pc.phonetic),
             pc.name_eng_short = decode(rs.name_eng_short_flag,
                                        1,
                                        rs.name_eng_short,
                                        pc.name_eng_short),
             pc.name_eng_full  = decode(rs.name_eng_full_flag,
                                        1,
                                        rs.name_eng_full,
                                        pc.name_eng_full),
             pc.state          = rs.state,
             pc.log            = rs.log,
             pc.edition_flag   = rs.edition_flag;
  END;
    PROCEDURE process_att_pt_system_mod IS
      BEGIN
        NULL;
      END;
END merge_au_pt_company;
/
