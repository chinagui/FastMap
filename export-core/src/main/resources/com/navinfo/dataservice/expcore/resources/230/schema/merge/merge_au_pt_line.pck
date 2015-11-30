CREATE OR REPLACE PACKAGE merge_au_pt_line IS

  -- Author  : MAYF
  -- Created : 2011/12/31 15:15:43
  -- Purpose : pt_line相关的融合
  PROCEDURE reset_temp_mg_table;
  PROCEDURE pre_process(v_merge_type VARCHAR2);
  PROCEDURE mod_state_ext;
  PROCEDURE att_add_pt_line_ext;
  PROCEDURE att_add_pt_linename_ext;
  PROCEDURE att_add_pt_linename_ext_add;
  PROCEDURE commit_poi_name_insert;
  PROCEDURE att_add_pt_line;
  PROCEDURE process_att_modify_log;
  PROCEDURE process_mod_poi_state;
  PROCEDURE att_del_pt_line;
  PROCEDURE reset_tmp_pt_line_name;
  PROCEDURE reset_tmp_pt_line;
  PROCEDURE process_att_pt_line_mod;
  PROCEDURE add_pt_eta_line;
  PROCEDURE process_eta_line_mod_log;
  PROCEDURE mod_pt_eta_line;
  PROCEDURE del_pt_eta_line;
  PROCEDURE mod_eta_line_state;
  PROCEDURE reset_tmp_pt_eta_line;
  PROCEDURE att_mod_pt_linename;
  PROCEDURE att_mod_pt_linename_add;

END merge_au_pt_line;
/
CREATE OR REPLACE PACKAGE BODY merge_au_pt_line IS
  PROCEDURE reset_temp_mg_table IS
  BEGIN
    DELETE FROM temp_pt_line_name_mg;
  END;
  PROCEDURE reset_tmp_pt_line IS
  BEGIN
    DELETE FROM temp_his_pt_line;
    INSERT INTO temp_his_pt_line
      SELECT * FROM pt_line;
  END;
  PROCEDURE reset_tmp_pt_line_name IS
  BEGIN
    DELETE FROM temp_pt_line_name;
    INSERT INTO temp_pt_line_name
      SELECT * FROM pt_line_name;
  END;
  PROCEDURE reset_tmp_pt_eta_line IS
  BEGIN
    DELETE FROM temp_pt_eta_line;
    INSERT INTO temp_pt_eta_line
      SELECT * FROM pt_eta_line;
  END;
  PROCEDURE pre_process(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           't');
    DELETE FROM temp_au_ptline_mul_task;
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_ptline_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_pt_line t
                WHERE ' || v_oprstatus_clause || ' 
               GROUP BY t.pid
              HAVING COUNT(1) > 1) rs';
    DELETE FROM temp_ptline_ext;
    EXECUTE IMMEDIATE 'INSERT INTO temp_ptline_ext
    SELECT * FROM pt_line pt WHERE EXISTS(SELECT 1 FROM au_pt_line t WHERE t.state=1 AND ' ||
                      v_oprstatus_clause ||
                      ' AND t.pid=pt.pid ) 
                      and not exists (select 1 from temp_au_ptline_mul_task mul where mul.pid=pt.pid) ';
    reset_tmp_pt_line();
    reset_tmp_pt_line_name();
    reset_tmp_pt_eta_line();
  
  END;
  PROCEDURE mod_state_ext IS
  BEGIN
    MERGE INTO pt_line p1
    USING temp_ptline_ext v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE SET p1.state = 1;
  END;
  PROCEDURE att_add_pt_line_ext IS
  BEGIN
    MERGE INTO pt_line pp
    USING (SELECT *
             FROM au_pt_line au
            WHERE au.state = 1
              AND au.att_oprstatus = 0
              AND EXISTS
            (SELECT 1 FROM temp_ptline_ext tmp WHERE tmp.pid = au.pid)) aupp
    ON (pp.pid = aupp.pid)
    WHEN MATCHED THEN
      UPDATE
         SET pp.system_id    = aupp.system_id,
             pp.city_code    = aupp.city_code,
             pp.type         = aupp.type,
             pp.color        = aupp.color,
             pp.nidb_lineid  = aupp.nidb_lineid,
             pp.log          = aupp.log,
             pp.edition_flag = aupp.edition_flag,
             --pp.state        = aupp.state,
             pp.data_source  = aupp.data_source,
             pp.update_batch = aupp.update_batch
      --pp.u_record     = aupp.u_record,
      --pp.u_fields     = aupp.u_fields
      ;
  END;
  PROCEDURE att_add_pt_linename_ext IS
  BEGIN
    --pt_line_name 表中Lang_Code为”CHI”、名称类型为原始（2）的存在，但是 au_pt_line_name 中没有数据的，将pt_line_name表中Lang_Code为”CHI”、名称类型为原始（2）的数据删除
    DELETE FROM pt_line_name ppn
     WHERE ppn.lang_code IN ('CHI', 'CHT')
       AND EXISTS
     (SELECT 1 FROM temp_ptline_ext ext WHERE ext.pid = ppn.pid)
       AND NOT EXISTS
     (SELECT 1 FROM au_pt_line_name auppn WHERE auppn.pid = ppn.pid);
    --pt_line_name表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用 au_pt_line_name 中名称修改  pt_line_name 中NAME、NAME_PHONETIC 的值
    MERGE INTO pt_line_name ppn
    USING view_mg_pt_linename_ext aurs
    ON (ppn.pid = aurs.pid AND ppn.lang_code = aurs.lang_code)
    WHEN MATCHED THEN
      UPDATE SET ppn.name = aurs.name, ppn.phonetic = aurs.phonetic;
  
  END;
  PROCEDURE att_add_pt_linename_ext_add IS
  BEGIN
    INSERT INTO temp_pt_line_name_mg t WITH rs AS
  (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_line_name
    GROUP BY pid)
  SELECT name_id,
         nvl(rs.name_groupid, 1) AS name_groupid,
         auipn.pid,
         lang_code,
         NAME,
         phonetic,
         src_flag,
         0 AS u_record,
         NULL AS u_fields
    FROM au_pt_line_name auipn, rs
   WHERE auipn.pid = rs.pid(+)
     AND auipn.lang_code IN ('CHI', 'CHT')
     AND EXISTS
   (SELECT 1 FROM temp_ptline_ext tmp WHERE tmp.pid = auipn.pid)
     AND NOT EXISTS (SELECT 1
            FROM pt_line_name ipn
           WHERE ipn.lang_code IN ('CHI', 'CHT')
             AND ipn.pid = auipn.pid);
  END;
  PROCEDURE commit_poi_name_insert IS
  BEGIN
    INSERT INTO pt_line_name
      SELECT * FROM temp_pt_line_name_mg;
  END;
  PROCEDURE do_add_name(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    DELETE FROM temp_pt_line_name_mg;
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'auptp');
    EXECUTE IMMEDIATE 'INSERT INTO temp_pt_line_name_mg WITH rs AS
  (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_line_name
    GROUP BY pid)
  SELECT name_id,
         nvl(rs.name_groupid, 1) AS name_groupid,
         auptpn.pid,
         lang_code,
         NAME,
         phonetic,
         src_flag,
         0 AS u_record,
         NULL AS u_fields
    FROM au_pt_line_name auptpn, rs
   WHERE  auptpn.pid = rs.pid(+) AND EXISTS
    (SELECT 1
             FROM au_pt_line auptp
            WHERE auptp.audata_id = auptpn.audata_id
              AND auptp.state = 1
              AND ' || v_oprstatus_clause || '
              AND NOT EXISTS
            (SELECT 1 FROM temp_ptline_ext ext WHERE ext.pid = auptp.pid)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ptline_mul_task tmp
                    WHERE tmp.pid = auptp.pid))
';
    INSERT INTO pt_line_name
      SELECT * FROM temp_pt_line_name_mg;
  
  END;
  PROCEDURE do_add_pt_line(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'auptp');
    --add pt_poi
    EXECUTE IMMEDIATE 'INSERT INTO pt_line
      SELECT auptp.PID,
            auptp.SYSTEM_ID,
            auptp.CITY_CODE,
            auptp.TYPE,
            auptp.COLOR,
            auptp.NIDB_LINEID,
            auptp.LOG,
            auptp.EDITION_FLAG,
            auptp.STATE,
            auptp.DATA_SOURCE,
            auptp.UPDATE_BATCH,
            0 as U_RECORD,
            null as U_FIELDS
        FROM au_pt_line auptp
       WHERE auptp.state = 1
         AND ' || v_oprstatus_clause ||
                      '         
         and not exists(select 1 from temp_ptline_ext ext where ext.pid=auptp.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ptline_mul_task tmp
               WHERE tmp.pid = auptp.pid)';
    --add PT_POI_NAME
    do_add_name(v_merge_type);
    --PT_ETA_LINE
    /*EXECUTE IMMEDIATE 'INSERT INTO PT_ETA_LINE
    SELECT 
    aupea.PID,
      aupea.ALIAS_NAME,
      aupea.ALIAS_PINYIN,
      aupea.BIKE,
      aupea.BIKE_PERIOD,
      aupea.IMAGE,
      aupea.RACK,
      aupea.DINNER,
      aupea.TOILET,
      aupea.SLEEPER,
      aupea.WHEEL_CHAIR,
      aupea.SMOKE,
      aupea.LOG,
      aupea.EDITION_FLAG,
      aupea.STATE,
      0 as U_RECORD,
      null as U_FIELDS
      FROM au_PT_ETA_LINE aupea
     WHERE EXISTS
     (SELECT 1
              FROM au_pt_line auptp
             WHERE auptp.pid = aupea.pid
               AND auptp.state = 1
               AND ' || v_oprstatus_clause || '
               and not exists(select 1 from temp_ptline_ext ext where ext.pid=auptp.pid)
               AND NOT EXISTS (SELECT 1
              FROM temp_au_ptline_mul_task tmp
             WHERE tmp.pid = auptp.pid))';*/
  
  END;
  PROCEDURE att_add_pt_line IS
  BEGIN
    do_add_pt_line(merge_utils.merge_type_att);
  
  END;

  PROCEDURE do_att_parse_log(v_log         VARCHAR2,
                             v_rec         IN OUT temp_au_pt_line_modify_log%ROWTYPE,
                             v_change_flag IN OUT BOOLEAN) IS
  BEGIN
  
    IF instr(v_log, '改系统编号') > 0 THEN
      v_rec.system_id := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.system_id := 0;
    END IF;
    IF instr(v_log, '改线路类型') > 0 THEN
      v_rec.type    := 1;
      v_change_flag := TRUE;
    ELSE
      v_rec.type := 0;
    END IF;
    IF instr(v_log, '改线路名') > 0 THEN
      v_rec.name    := 1;
      v_change_flag := TRUE;
    ELSE
      v_rec.name := 0;
    END IF;
  END;

  PROCEDURE save_pt_modify_log(p_rec temp_au_pt_line_modify_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into TEMP_AU_PT_LINE_MODIFY_LOG(
        PID,
        SYSTEM_ID,
        TYPE,
        NAME,
        AUDATA_ID
       ) values (:V_PID,
                :V_SYSTEM_ID,
                :V_TYPE,
                :V_NAME,               
                :V_AUDATA_ID
                )'
      USING p_rec.pid, p_rec.system_id, p_rec.type, p_rec.name, p_rec.audata_id;
  END;
  PROCEDURE process_att_modify_log IS
    v_rec         temp_au_pt_line_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_line_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.log
                  FROM au_pt_line a, pt_line p
                 WHERE a.pid = p.pid
                   AND a.log IS NOT NULL
                   AND a.state = 3
                   AND a.att_oprstatus = 0
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ptline_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := rec.log;
      do_att_parse_log(v_log, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_pt_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_mod_poi_state IS
  BEGIN
    MERGE INTO pt_line p1
    USING (SELECT p2.pid, p2.log, p2.state
             FROM au_pt_line p2
            WHERE p2.state = 3
              AND (p2.att_oprstatus = 0)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ptline_mul_task mul
                    WHERE mul.pid = p2.pid)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.log = v.log, p1.state = 3, p1.edition_flag = '外业修改';
  END;
  PROCEDURE do_del(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'aupp');
  
    --del pt_line_name   
    EXECUTE IMMEDIATE 'DELETE FROM pt_line_name ppn
     WHERE EXISTS (SELECT 1
              FROM au_pt_line aupp
             WHERE aupp.state = 2
              AND ' || v_oprstatus_clause || '
               AND EXISTS
             (SELECT 1 FROM pt_line pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptline_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND aupp.pid = ppn.pid)';
    --del PT_ETA_LINE
    /*EXECUTE IMMEDIATE 'DELETE FROM PT_ETA_LINE ppf
    WHERE EXISTS (SELECT 1
             FROM au_pt_line aupp
            WHERE aupp.state = 2
              AND ' || v_oprstatus_clause || '
              AND EXISTS
            (SELECT 1 FROM pt_line pp WHERE pp.pid = aupp.pid)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ptline_mul_task tmp
                    WHERE tmp.pid = aupp.pid)
              AND ppf.pid = aupp.pid)';*/
  
    --del pt_line
    EXECUTE IMMEDIATE 'DELETE FROM pt_line pp
     WHERE EXISTS (SELECT 1
              FROM au_pt_line aupp
             WHERE aupp.state = 2
              AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptline_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND pp.pid = aupp.pid)';
  END;
  PROCEDURE att_del_pt_line IS
  BEGIN
    do_del(merge_utils.merge_type_att);
  END;

  PROCEDURE process_att_pt_line_mod IS
  BEGIN
    /*
    改系统编号
    改线路类型   
    */
    MERGE INTO pt_line p1
    USING (SELECT p2.pid,
                  p2.system_id,
                  p2.type,
                  l.system_id  AS system_id_flag,
                  l.type       AS type_flag
             FROM au_pt_line p2, temp_au_pt_line_modify_log l
            WHERE p2.audata_id = l.audata_id
              AND (l.system_id = 1 OR l.type = 1 OR l.name = 1)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.system_id = decode(v.system_id_flag,
                                   1,
                                   v.system_id,
                                   p1.system_id),
             p1.type      = decode(v.type_flag, 1, v.type, p1.type);
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('修改POI主表字段时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE att_mod_pt_linename IS
  
  BEGIN
    --pt_line_name表中Lang_Code为”CHI”的存在，但是au_pt_line_name中没有数据的，将pt_line_name表中Lang_Code为”CHI”的数据删除
    DELETE FROM pt_line_name ipn
     WHERE ipn.lang_code IN ('CHI', 'CHT')
       AND EXISTS (SELECT 1
              FROM temp_au_pt_line_modify_log tmp
             WHERE tmp.name = 1
               AND ipn.pid = tmp.pid)
       AND NOT EXISTS
     (SELECT 1 FROM au_pt_line_name auipn WHERE auipn.pid = ipn.pid);
    --pt_line_name表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用au_pt_line_name中名称分类为原始（2）的修改pt_line_name 中NAME、NAME_PHONETIC 的值  
    MERGE INTO pt_line_name ipn
    USING view_mg_pt_linename aurs
    ON (ipn.pid = aurs.pid AND ipn.lang_code = aurs.lang_code)
    WHEN MATCHED THEN
      UPDATE SET ipn.name = aurs.name, ipn.phonetic = aurs.phonetic;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理ptline名称时出错' || SQLERRM);
      --rollback;
      RAISE;
  END;
  PROCEDURE att_mod_pt_linename_add IS
  BEGIN
    INSERT INTO temp_pt_line_name_mg t WITH rs AS
  (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_line_name
    GROUP BY pid)
  SELECT name_id,
         nvl(rs.name_groupid, 1) AS name_groupid,
         auipn.pid,
         lang_code,
         auipn.name,
         phonetic,
         src_flag,
         0 AS u_record,
         NULL AS u_fields
    FROM au_pt_line_name auipn, temp_au_pt_line_modify_log tmp, rs
   WHERE auipn.lang_code IN ('CHI', 'CHT')
     AND auipn.pid = tmp.pid
     AND tmp.name = 1
     AND auipn.pid = rs.pid(+)
     AND NOT EXISTS (SELECT 1
            FROM pt_line_name ipn
           WHERE ipn.lang_code IN ('CHI', 'CHT')
             AND ipn.pid = auipn.pid);

  END;
  PROCEDURE add_pt_eta_line IS
  BEGIN
    delete from pt_eta_line 
    where pid in (select pid from au_pt_eta_line au  where au.state = 1
         AND au.att_oprstatus = 0);
    INSERT INTO pt_eta_line
      SELECT pid,
             alias_name,
             alias_pinyin,
             bike,
             bike_period,
             image,
             rack,
             dinner,
             toilet,
             sleeper,
             wheel_chair,
             smoke,
             log,
             edition_flag,
             state,
             0            AS u_record,
             NULL         AS u_fields
        FROM au_pt_eta_line au
       WHERE au.state = 1
         AND au.att_oprstatus = 0;
  END;
  PROCEDURE do_etaline_parse_log(v_log         VARCHAR2,
                                 v_rec         IN OUT temp_au_pt_eta_line_mod_log%ROWTYPE,
                                 v_change_flag IN OUT BOOLEAN) IS
  BEGIN
    IF instr(v_log, '改线路别名') > 0 THEN
      v_rec.alias_name_flag := 1;
      v_change_flag         := TRUE;
    ELSE
      v_rec.alias_name_flag := 0;
    END IF;
    IF instr(v_log, '改线路别名') > 0 THEN
      v_rec.alias_pinyin_flag := 1;
      v_change_flag           := TRUE;
    ELSE
      v_rec.alias_pinyin_flag := 0;
    END IF;
    IF instr(v_log, '改允许自行车') > 0 THEN
      v_rec.bike_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.bike_flag := 0;
    END IF;
    IF instr(v_log, '改自行车允许时段') > 0 THEN
      v_rec.bike_period_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.bike_period_flag := 0;
    END IF;
    IF instr(v_log, '改图像') > 0 THEN
      v_rec.image_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.image_flag := 0;
    END IF;
    IF instr(v_log, '改行李架') > 0 THEN
      v_rec.rack_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.rack_flag := 0;
    END IF;
    IF instr(v_log, '改用餐服务') > 0 THEN
      v_rec.dinner_flag := 1;
      v_change_flag     := TRUE;
    ELSE
      v_rec.dinner_flag := 0;
    END IF;
    IF instr(v_log, '改洗手间') > 0 THEN
      v_rec.toilet_flag := 1;
      v_change_flag     := TRUE;
    ELSE
      v_rec.toilet_flag := 0;
    END IF;
    IF instr(v_log, '改卧铺车厢') > 0 THEN
      v_rec.sleeper_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.sleeper_flag := 0;
    END IF;
    IF instr(v_log, '改轮椅出入') > 0 THEN
      v_rec.wheel_chair_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.wheel_chair_flag := 0;
    END IF;
    IF instr(v_log, '改允许吸烟') > 0 THEN
      v_rec.smoke_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.smoke_flag := 0;
    END IF;
  
  END;
  PROCEDURE save_pt_eta_line_mo_log(p_rec temp_au_pt_eta_line_mod_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into temp_au_pt_eta_line_mod_log(
        PID,
        AUDATA_ID,
        ALIAS_NAME_FLAG,
        ALIAS_PINYIN_FLAG,
        BIKE_FLAG,
        BIKE_PERIOD_FLAG,
        IMAGE_FLAG,
        RACK_FLAG,
        DINNER_FLAG,
        TOILET_FLAG,
        SLEEPER_FLAG,
        WHEEL_CHAIR_FLAG,
        SMOKE_FLAG

       ) values (:v_PID,
                  :v_AUDATA_ID,
                  :v_ALIAS_NAME_FLAG,
                  :v_ALIAS_PINYIN_FLAG,
                  :v_BIKE_FLAG,
                  :v_BIKE_PERIOD_FLAG,
                  :v_IMAGE_FLAG,
                  :v_RACK_FLAG,
                  :v_DINNER_FLAG,
                  :v_TOILET_FLAG,
                  :v_SLEEPER_FLAG,
                  :v_WHEEL_CHAIR_FLAG,
                  :v_SMOKE_FLAG
                )'
      USING p_rec.pid, p_rec.audata_id, p_rec.alias_name_flag, p_rec.alias_pinyin_flag, p_rec.bike_flag, p_rec.bike_period_flag, p_rec.image_flag, p_rec.rack_flag, p_rec.dinner_flag, p_rec.toilet_flag, p_rec.sleeper_flag, p_rec.wheel_chair_flag, p_rec.smoke_flag;
  END;
  PROCEDURE process_eta_line_mod_log IS
    v_rec         temp_au_pt_eta_line_mod_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_eta_line_mod_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.log
                  FROM au_pt_eta_line a
                 WHERE a.log IS NOT NULL
                   AND a.state = 3
                   AND a.att_oprstatus = 0) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := rec.log;
      do_etaline_parse_log(v_log, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_pt_eta_line_mo_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE mod_pt_eta_line IS
  BEGIN
    DELETE FROM pt_eta_line p1
     WHERE p1.pid IN
           (SELECT pid
              FROM temp_au_pt_eta_line_mod_log l
             WHERE (l.alias_name_flag = 1 OR l.alias_pinyin_flag = 1 OR
                   l.bike_flag = 1 OR l.bike_period_flag = 1 OR
                   l.image_flag = 1 OR l.rack_flag = 1 OR l.dinner_flag = 1 OR
                   l.toilet_flag = 1 OR l.sleeper_flag = 1 OR
                   l.wheel_chair_flag = 1 OR l.smoke_flag = 1));
    INSERT INTO pt_eta_line
      SELECT au.pid,
             alias_name,
             alias_pinyin,
             bike,
             bike_period,
             image,
             rack,
             dinner,
             toilet,
             sleeper,
             wheel_chair,
             smoke,
             log,
             '外业修改' AS edition_flag,
             state,
             0 AS u_record,
             NULL AS u_fields
        FROM au_pt_eta_line au, temp_au_pt_eta_line_mod_log l
       WHERE au.state = 3
         AND au.att_oprstatus = 0
         AND au.audata_id = l.audata_id
         AND au.pid IN (SELECT pid FROM temp_pt_eta_line)
         AND (l.alias_name_flag = 1 OR l.alias_pinyin_flag = 1 OR
             l.bike_flag = 1 OR l.bike_period_flag = 1 OR l.image_flag = 1 OR
             l.rack_flag = 1 OR l.dinner_flag = 1 OR l.toilet_flag = 1 OR
             l.sleeper_flag = 1 OR l.wheel_chair_flag = 1 OR
             l.smoke_flag = 1);
  
  END;
  PROCEDURE del_pt_eta_line IS
  BEGIN
    DELETE FROM pt_eta_line
     WHERE pid IN (SELECT pid
                     FROM au_pt_eta_line au
                    WHERE au.state = 2
                      AND au.att_oprstatus = 0);
  END;
  PROCEDURE mod_eta_line_state IS
  BEGIN
    MERGE INTO pt_eta_line p1
    USING (SELECT p2.pid, p2.log, p2.state
             FROM au_pt_eta_line p2
            WHERE p2.state = 3
              AND (p2.att_oprstatus = 0)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.log          = v.log,
             p1.state        = v.state,
             p1.edition_flag = '外业修改';
  END;

END merge_au_pt_line;
/
