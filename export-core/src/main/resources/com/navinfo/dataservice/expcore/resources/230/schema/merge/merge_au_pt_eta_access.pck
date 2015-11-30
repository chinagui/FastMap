CREATE OR REPLACE PACKAGE merge_au_pt_eta_access IS

  -- Author  : MAYF
  -- Created : 2011/12/31 15:15:43
  -- Purpose : pt_line相关的融合

  PROCEDURE pre_process;
  PROCEDURE add_pt_eta_access;
  PROCEDURE process_mod_log;
  PROCEDURE mod_pt_eta_access;
  PROCEDURE del_pt_eta_access;
  PROCEDURE reset_tmp_pt_eta_access;

END merge_au_pt_eta_access;
/
CREATE OR REPLACE PACKAGE BODY merge_au_pt_eta_access IS
  PROCEDURE reset_tmp_pt_eta_access IS
  BEGIN
    DELETE FROM temp_pt_eta_access;
    INSERT INTO temp_pt_eta_access
      SELECT * FROM pt_eta_access;
  END;
  PROCEDURE pre_process IS
  BEGIN
    reset_tmp_pt_eta_access();
  END;
  PROCEDURE add_pt_eta_access IS
  BEGIN
    delete from pt_eta_access t where t.poi_pid in(select poi_pid FROM au_pt_eta_access au
       WHERE au.state = 1
         AND au.att_oprstatus = 0);
    INSERT INTO pt_eta_access
      SELECT poi_pid,
             alias_name,
             alias_pinyin,
             open_period,
             manual_ticket,
             manual_ticket_period,
             auto_ticket,
             log,
             edition_flag,
             state,
             0                    AS u_record,
             NULL                 AS u_fields
        FROM au_pt_eta_access au
       WHERE au.state = 1
         AND au.att_oprstatus = 0;
  END;
  PROCEDURE do_parse_log(v_log         VARCHAR2,
                         v_rec         IN OUT temp_au_pt_eta_access_mod_log%ROWTYPE,
                         v_change_flag IN OUT BOOLEAN) IS
  BEGIN
    ---pt_eta_access
    IF instr(v_log, '改出入口别名') > 0 THEN
      v_rec.alias_name := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.alias_name := 0;
    END IF;
    IF instr(v_log, '') > 0 THEN
      v_rec.alias_pinyin := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.alias_pinyin := 0;
    END IF;
    IF instr(v_log, '改开放时间') > 0 THEN
      v_rec.open_period := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.open_period := 0;
    END IF;
    IF instr(v_log, '改人工售票') > 0 THEN
      v_rec.manual_ticket := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.manual_ticket := 0;
    END IF;
    IF instr(v_log, '改人工售票时段') > 0 THEN
      v_rec.manual_ticket_period := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.manual_ticket_period := 0;
    END IF;
    IF instr(v_log, '改自动售票机') > 0 THEN
      v_rec.auto_ticket := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.auto_ticket := 0;
    END IF;
  
  END;
  PROCEDURE save_pt_eta_access_mo_log(p_rec temp_au_pt_eta_access_mod_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into temp_au_pt_eta_access_mod_log(
        PID,
        AUDATA_ID,
        ALIAS_NAME,
        ALIAS_PINYIN,
        OPEN_PERIOD,
        MANUAL_TICKET,
        MANUAL_TICKET_PERIOD,
        AUTO_TICKET
       ) values (:PID,
                  :AUDATA_ID,
                  :ALIAS_NAME,
                  :ALIAS_PINYIN,
                  :OPEN_PERIOD,
                  :MANUAL_TICKET,
                  :MANUAL_TICKET_PERIOD,
                  :AUTO_TICKET
                )'
      USING p_rec.pid, p_rec.audata_id, p_rec.alias_name, p_rec.alias_pinyin, p_rec.open_period, p_rec.manual_ticket, p_rec.manual_ticket_period, p_rec.auto_ticket;
  END;
  PROCEDURE process_mod_log IS
    v_rec         temp_au_pt_eta_access_mod_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_eta_access_mod_log;
    FOR rec IN (SELECT a.audata_id, a.poi_pid, a.log
                  FROM au_pt_eta_access a
                 WHERE a.log IS NOT NULL
                   AND a.state = 3
                   AND a.att_oprstatus = 0) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.poi_pid;
      v_log           := rec.log;
      do_parse_log(v_log, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_pt_eta_access_mo_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错'||SQLERRM);
      RAISE;
  END;
  PROCEDURE mod_pt_eta_access IS
  BEGIN
    DELETE FROM pt_eta_access p1
     WHERE p1.poi_pid IN
           (SELECT pid
              FROM temp_au_pt_eta_access_mod_log l
             WHERE (l.alias_name = 1 OR l.alias_pinyin = 1 OR
                   l.open_period = 1 OR l.manual_ticket = 1 OR
                   l.manual_ticket_period = 1 OR l.auto_ticket = 1));
    INSERT INTO pt_eta_access
      SELECT au.poi_pid,
             au.alias_name,
             au.alias_pinyin,
             au.open_period,
             au.manual_ticket,
             au.manual_ticket_period,
             au.auto_ticket,
             log,             
             '外业修改' AS edition_flag,
             state,
             0 AS u_record,
             NULL AS u_fields
        FROM au_pt_eta_access au, temp_au_pt_eta_access_mod_log l
       WHERE au.audata_id = l.audata_id
         and au.poi_pid in(select poi_pid from temp_pt_eta_access)
         AND (l.alias_name = 1 OR l.alias_pinyin = 1 OR
                   l.open_period = 1 OR l.manual_ticket = 1 OR
                   l.manual_ticket_period = 1 OR l.auto_ticket = 1);
  
  END;
  PROCEDURE del_pt_eta_access IS
  BEGIN
    DELETE FROM pt_eta_access
     WHERE poi_pid IN (SELECT poi_pid
                         FROM au_pt_eta_access au
                        WHERE au.state = 2
                          AND au.att_oprstatus = 0);
  END;

END merge_au_pt_eta_access;
/
