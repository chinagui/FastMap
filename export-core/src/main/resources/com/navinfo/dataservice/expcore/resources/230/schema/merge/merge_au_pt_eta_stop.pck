CREATE OR REPLACE PACKAGE merge_au_pt_eta_stop IS

  -- Author  : MAYF
  -- Created : 2011/12/31 15:15:43
  -- Purpose : pt_line相关的融合

  PROCEDURE pre_process;
  PROCEDURE add_pt_eta_stop;
  PROCEDURE process_mod_log;
  PROCEDURE mod_pt_eta_stop;
  PROCEDURE del_pt_eta_stop; 
  PROCEDURE reset_tmp_pt_eta_stop;

END merge_au_pt_eta_stop;
/
CREATE OR REPLACE PACKAGE BODY merge_au_pt_eta_stop IS
  PROCEDURE reset_tmp_pt_eta_stop IS
  BEGIN
    DELETE FROM temp_pt_eta_stop;
    INSERT INTO temp_pt_eta_stop
      SELECT * FROM pt_eta_stop;
  END;
  PROCEDURE pre_process IS   
  BEGIN
    reset_tmp_pt_eta_stop();
  END;
  PROCEDURE add_pt_eta_stop IS
  BEGIN
    delete from pt_eta_stop where poi_pid 
    in(select poi_pid FROM au_pt_eta_stop au
       WHERE au.state = 1
         AND au.att_oprstatus = 0);
    INSERT INTO pt_eta_stop
      SELECT poi_pid,
             alias_name,
             alias_pinyin,
             private_park,
             private_park_period,
             carport_exact,
             carport_estimate,
             bike_park,
             bike_park_period,
             manual_ticket,
             manual_ticket_period,
             mobile,
             baggage_security,
             left_baggage,
             consignation_exact,
             consignation_estimate,
             convenient,
             smoke,
             build_type,
             auto_ticket,
             toilet,
             wifi,
             open_period,
             fare_area,
             log,
             edition_flag,
             state,
             0                     AS u_record,
             NULL                  AS u_fields
        FROM au_pt_eta_stop au
       WHERE au.state = 1
         AND au.att_oprstatus = 0;
  END;
  PROCEDURE do_parse_log(v_log         VARCHAR2,
                         v_rec         IN OUT temp_au_pt_eta_stop_mod_log%ROWTYPE,
                         v_change_flag IN OUT BOOLEAN) IS
  BEGIN
    ---pt_eta_stop
    IF instr(v_log, '改主点别名') > 0 THEN
      v_rec.eta_stop_alias_name := 1;
      v_change_flag             := TRUE;
    ELSE
      v_rec.eta_stop_alias_name := 0;
    END IF;
    IF instr(v_log, '改专用停车场') > 0 THEN
      v_rec.eta_stop_private_park := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_private_park := 0;
    END IF;
    IF instr(v_log, '改停车时段') > 0 THEN
      v_rec.eta_stop_p_park_period := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_p_park_period := 0;
    END IF;
    IF instr(v_log, '改车位数量（精确值）') > 0 THEN
      v_rec.eta_stop_carport_exact := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_carport_exact := 0;
    END IF;
    IF instr(v_log, '改车位数量（估值）') > 0 THEN
      v_rec.eta_stop_carport_estimate := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_carport_estimate := 0;
    END IF;
    IF instr(v_log, '改自行车停车场') > 0 THEN
      v_rec.eta_stop_bike_park := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_bike_park := 0;
    END IF;
    IF instr(v_log, '改自行车有人看守时段') > 0 THEN
      v_rec.eta_stop_b_park_period := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_b_park_period := 0;
    END IF;
    IF instr(v_log, '改人工售票') > 0 THEN
      v_rec.eta_stop_manual_ticket := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_manual_ticket := 0;
    END IF;
    IF instr(v_log, '改人工售票时段') > 0 THEN
      v_rec.eta_stop_m_ticket_period := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_m_ticket_period := 0;
    END IF;
    IF instr(v_log, '改手机信号') > 0 THEN
      v_rec.eta_stop_mobile := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_mobile := 0;
    END IF;
    IF instr(v_log, '改行李安检') > 0 THEN
      v_rec.eta_stop_baggage_security := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_baggage_security := 0;
    END IF;
    IF instr(v_log, '改自助行李寄存柜') > 0 THEN
      v_rec.eta_stop_left_baggage := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_left_baggage := 0;
    END IF;
    IF instr(v_log, '改寄存柜数量（精确值）') > 0 THEN
      v_rec.eta_stop_consignation_exact := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_consignation_exact := 0;
    END IF;
    IF instr(v_log, '改寄存柜数量（估值）') > 0 THEN
      v_rec.eta_stop_consignation_estimate := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_consignation_estimate := 0;
    END IF;
    IF instr(v_log, '改零售便利店') > 0 THEN
      v_rec.eta_stop_convenient := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_convenient := 0;
    END IF;
    IF instr(v_log, '改允许吸烟') > 0 THEN
      v_rec.eta_stop_smoke := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_smoke := 0;
    END IF;
    IF instr(v_log, '改建筑类型') > 0 THEN
      v_rec.eta_stop_build_type := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_build_type := 0;
    END IF;
    IF instr(v_log, '改自动售票机') > 0 THEN
      v_rec.eta_stop_auto_ticket := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_auto_ticket := 0;
    END IF;
    IF instr(v_log, '改洗手间') > 0 THEN
      v_rec.eta_stop_toilet := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_toilet := 0;
    END IF;
    IF instr(v_log, '改无线网') > 0 THEN
      v_rec.eta_stop_wifi := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_wifi := 0;
    END IF;
    IF instr(v_log, '改开放时间') > 0 THEN
      v_rec.eta_stop_open_period := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_open_period := 0;
    END IF;
    IF instr(v_log, '改票价区域') > 0 THEN
      v_rec.eta_stop_fare_area := 1;
    
      v_change_flag := TRUE;
    ELSE
      v_rec.eta_stop_fare_area := 0;
    END IF;
  
  END;
  PROCEDURE save_pt_eta_stop_mo_log(p_rec temp_au_pt_eta_stop_mod_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into temp_au_PT_ETA_STOP_mod_log(
        PID,
        AUDATA_ID,
        ETA_STOP_ALIAS_NAME,
        ETA_STOP_PRIVATE_PARK,
        ETA_STOP_P_PARK_PERIOD,
        ETA_STOP_CARPORT_EXACT,
        ETA_STOP_CARPORT_ESTIMATE,
        ETA_STOP_BIKE_PARK,
        ETA_STOP_B_PARK_PERIOD,
        ETA_STOP_MANUAL_TICKET,
        ETA_STOP_M_TICKET_PERIOD,
        ETA_STOP_MOBILE,
        ETA_STOP_BAGGAGE_SECURITY,
        ETA_STOP_LEFT_BAGGAGE,
        ETA_STOP_CONSIGNATION_EXACT,
        ETA_STOP_CONSIGNATION_ESTIMATE,
        ETA_STOP_CONVENIENT,
        ETA_STOP_SMOKE,
        ETA_STOP_BUILD_TYPE,
        ETA_STOP_AUTO_TICKET,
        ETA_STOP_TOILET,
        ETA_STOP_WIFI,
        ETA_STOP_OPEN_PERIOD,
        ETA_STOP_FARE_AREA
       ) values (:PID,
                  :AUDATA_ID,
                  :ETA_STOP_ALIAS_NAME,
                  :ETA_STOP_PRIVATE_PARK,
                  :ETA_STOP_P_PARK_PERIOD,
                  :ETA_STOP_CARPORT_EXACT,
                  :ETA_STOP_CARPORT_ESTIMATE,
                  :ETA_STOP_BIKE_PARK,
                  :ETA_STOP_B_PARK_PERIOD,
                  :ETA_STOP_MANUAL_TICKET,
                  :ETA_STOP_M_TICKET_PERIOD,
                  :ETA_STOP_MOBILE,
                  :ETA_STOP_BAGGAGE_SECURITY,
                  :ETA_STOP_LEFT_BAGGAGE,
                  :ETA_STOP_CONSIGNATION_EXACT,
                  :ETA_STOP_CONSIGNATION_ESTIMATE,
                  :ETA_STOP_CONVENIENT,
                  :ETA_STOP_SMOKE,
                  :ETA_STOP_BUILD_TYPE,
                  :ETA_STOP_AUTO_TICKET,
                  :ETA_STOP_TOILET,
                  :ETA_STOP_WIFI,
                  :ETA_STOP_OPEN_PERIOD,
                  :ETA_STOP_FARE_AREA
                )'
      USING p_rec.pid, p_rec.audata_id, p_rec.eta_stop_alias_name, p_rec.eta_stop_private_park, p_rec.eta_stop_p_park_period, p_rec.eta_stop_carport_exact, p_rec.eta_stop_carport_estimate, p_rec.eta_stop_bike_park, p_rec.eta_stop_b_park_period, p_rec.eta_stop_manual_ticket, p_rec.eta_stop_m_ticket_period, p_rec.eta_stop_mobile, p_rec.eta_stop_baggage_security, p_rec.eta_stop_left_baggage, p_rec.eta_stop_consignation_exact, p_rec.eta_stop_consignation_estimate, p_rec.eta_stop_convenient, p_rec.eta_stop_smoke, p_rec.eta_stop_build_type, p_rec.eta_stop_auto_ticket, p_rec.eta_stop_toilet, p_rec.eta_stop_wifi, p_rec.eta_stop_open_period, p_rec.eta_stop_fare_area;
  END;
  PROCEDURE process_mod_log IS
    v_rec         temp_au_pt_eta_stop_mod_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_eta_stop_mod_log;
    FOR rec IN (SELECT a.audata_id, a.poi_pid, a.log
                  FROM au_pt_eta_stop a
                 WHERE a.log IS NOT NULL
                   AND a.state = 3
                   AND a.att_oprstatus = 0) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.poi_pid;
      v_log           := rec.log;
      do_parse_log(v_log, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_pt_eta_stop_mo_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE mod_pt_eta_stop IS
  BEGIN
    DELETE FROM pt_eta_stop p1
     WHERE p1.poi_pid IN
           (SELECT pid
              FROM temp_au_pt_eta_stop_mod_log l
             WHERE (
                   l.eta_stop_alias_name = 1 OR l.eta_stop_private_park = 1 OR
                   l.eta_stop_p_park_period = 1 OR
                   l.eta_stop_carport_exact = 1 OR
                   l.eta_stop_carport_estimate = 1 OR
                   l.eta_stop_bike_park = 1 OR l.eta_stop_b_park_period = 1 OR
                   l.eta_stop_manual_ticket = 1 OR
                   l.eta_stop_m_ticket_period = 1 OR l.eta_stop_mobile = 1 OR
                   l.eta_stop_baggage_security = 1 OR
                   l.eta_stop_left_baggage = 1 OR
                   l.eta_stop_consignation_exact = 1 OR
                   l.eta_stop_consignation_estimate = 1 OR
                   l.eta_stop_convenient = 1 OR l.eta_stop_smoke = 1 OR
                   l.eta_stop_build_type = 1 OR l.eta_stop_auto_ticket = 1 OR
                   l.eta_stop_toilet = 1 OR l.eta_stop_wifi = 1 OR
                   l.eta_stop_open_period = 1 OR l.eta_stop_fare_area = 1));
    INSERT INTO pt_eta_stop
      SELECT poi_pid,
             alias_name,
             alias_pinyin,
             private_park,
             private_park_period,
             carport_exact,
             carport_estimate,
             bike_park,
             bike_park_period,
             manual_ticket,
             manual_ticket_period,
             mobile,
             baggage_security,
             left_baggage,
             consignation_exact,
             consignation_estimate,
             convenient,
             smoke,
             build_type,
             auto_ticket,
             toilet,
             wifi,
             open_period,
             fare_area,
             log,
             '外业修改' as edition_flag,
             state,
             0                     AS u_record,
             NULL                  AS u_fields
        FROM au_pt_eta_stop au, temp_au_pt_eta_stop_mod_log l
       WHERE au.audata_id = l.audata_id 
         and au.poi_pid in(select poi_pid from temp_pt_eta_stop tmp)        
         AND (l.eta_stop_alias_name = 1 OR
             l.eta_stop_private_park = 1 OR l.eta_stop_p_park_period = 1 OR
             l.eta_stop_carport_exact = 1 OR
             l.eta_stop_carport_estimate = 1 OR l.eta_stop_bike_park = 1 OR
             l.eta_stop_b_park_period = 1 OR l.eta_stop_manual_ticket = 1 OR
             l.eta_stop_m_ticket_period = 1 OR l.eta_stop_mobile = 1 OR
             l.eta_stop_baggage_security = 1 OR
             l.eta_stop_left_baggage = 1 OR
             l.eta_stop_consignation_exact = 1 OR
             l.eta_stop_consignation_estimate = 1 OR
             l.eta_stop_convenient = 1 OR l.eta_stop_smoke = 1 OR
             l.eta_stop_build_type = 1 OR l.eta_stop_auto_ticket = 1 OR
             l.eta_stop_toilet = 1 OR l.eta_stop_wifi = 1 OR
             l.eta_stop_open_period = 1 OR l.eta_stop_fare_area = 1);
  
  END;
  PROCEDURE del_pt_eta_stop IS
  BEGIN
    DELETE FROM pt_eta_stop
     WHERE poi_pid IN (SELECT poi_pid
                         FROM au_pt_eta_stop au
                        WHERE au.state = 2
                          AND au.att_oprstatus = 0);
  END;

END merge_au_pt_eta_stop;
/
