CREATE OR REPLACE PACKAGE merge_au_ix_annotation_no_his IS

  -- Author  : MAYF , CM
  -- Created : 2012/12/07 14:47:43
  -- Purpose : IX_ANNOTATION的内外业融合
  PROCEDURE reset_tmp_xi_anot_name;
  PROCEDURE reset_temp_mg_table;
  PROCEDURE reset_tmp_ix_ann;
  PROCEDURE pre_process_poi(v_merge_type VARCHAR2);
  PROCEDURE process_att_add_annotation;
  PROCEDURE process_att_modify_log;
  PROCEDURE process_att_mod_ix_anot;
  PROCEDURE process_att_mod_ix_anot_name; --ix_annotation_name
  PROCEDURE process_att_mod_anot_name_add;
  PROCEDURE commit_ix_anot_insert;
  PROCEDURE process_att_delete_poi;
  PROCEDURE process_geo_delete_poi;
  PROCEDURE process_geo_add_annotation;
  PROCEDURE process_geo_modify_log;
  PROCEDURE process_attgeo_modify_log;
  PROCEDURE process_geo_mod_ix_annotation;
  PROCEDURE process_att_geo_delete_poi;
  PROCEDURE process_att_geo_add_poi;

  PROCEDURE geo_add_anot_ext;
  PROCEDURE att_add_anot_ext;
  PROCEDURE att_add_anot_name_ext;
  PROCEDURE att_add_anot_name_ext_add;
  PROCEDURE process_name_groupid;

  PROCEDURE mul_att_add_anot_ext(v_audata_id NUMBER);
  PROCEDURE mul_geo_add_anot_ext(v_audata_id NUMBER);

  PROCEDURE reset_tmp_ix_ann(v_pid NUMBER);
  PROCEDURE mul_att_add_anot_name_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_poiname_ext_add(v_audata_id NUMBER);
  PROCEDURE mul_add_annotation(v_audata_id NUMBER);
  PROCEDURE mul_del_poi(v_pid NUMBER);
  PROCEDURE mul_mod_poi_name(v_data_id NUMBER);
  PROCEDURE mul_geo_mod_ix_poi(v_data_id NUMBER);
  PROCEDURE mul_att_mod_ix_poi(v_data_id NUMBER);

  PROCEDURE mul_mod_poi_state_ext(v_pid   NUMBER,
                                  v_state NUMBER,
                                  v_log   ix_annotation.field_modify_flag%TYPE);
  PROCEDURE reset_temp_ixanot_name(v_audata_id NUMBER);
  PROCEDURE process_mod_poi_state(v_merge_type VARCHAR2);

END merge_au_ix_annotation_no_his;
/
CREATE OR REPLACE PACKAGE BODY merge_au_ix_annotation_no_his IS
  PROCEDURE reset_tmp_xi_anot_name IS
  BEGIN
    DELETE FROM temp_his_ix_annot_name;
    INSERT INTO temp_his_ix_annot_name
      SELECT * FROM ix_annotation_name;
  END;
  PROCEDURE reset_temp_mg_table IS
  BEGIN
    DELETE FROM temp_ix_anot_name_mg;
  END;
  PROCEDURE reset_tmp_ix_ann IS
  BEGIN
    --为了记录变化前履历，临时保存POI
    DELETE FROM temp_his_ix_annotation;
    INSERT INTO temp_his_ix_annotation
      SELECT * FROM ix_annotation;
  END;
  --如果一个文字被多个外业任务修改（包括新增、修改、删除）不进行融合
  PROCEDURE pre_process_poi(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           't');
    --单任务作业
    DELETE FROM temp_au_ix_anno_uniq_task;
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_ix_anno_uniq_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_ix_annotation t
                WHERE ' || v_oprstatus_clause || ' 
               GROUP BY t.pid
              HAVING COUNT(1) =1) rs';
    --多任务作业数据
    DELETE FROM temp_au_ix_anno_mul_task;
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_ix_anno_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_ix_annotation t
                WHERE ' || v_oprstatus_clause || ' 
               GROUP BY t.pid
              HAVING COUNT(1) > 1) rs';
    --多任务作业
    --如果一个poi被多个外业任务操作
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'au');
    EXECUTE IMMEDIATE'                                                           
    INSERT INTO temp_au_ix_anot_grp
      SELECT audata_id, pid, state
        FROM (
             --分组后，包含delete操作，并且在delete之后的数据
             WITH st AS (SELECT au.audata_id,
                                au.pid,
                                au.imp_date,
                                decode(instr(au.field_modify_flag, ''删除''),
                                       0,
                                       (decode(instr(au.field_modify_flag,
                                                     ''新增''),
                                               0,
                                               (decode(instr(au.field_modify_flag,
                                                             ''改''),
                                                       0,
                                                       0,
                                                       2)),
                                               3)),
                                       1) AS state,
                                row_number() over(PARTITION BY pid ORDER BY imp_date ASC, audata_id ASC) rn
                           FROM au_ix_annotation au
                          WHERE ' || v_oprstatus_clause || ' and au.pid IN
                                (SELECT pid FROM temp_au_ix_anno_mul_task))
               SELECT st.audata_id, st.pid, st.state
                 FROM st,
                      (SELECT pid, MAX(rn) maxrn
                         FROM (SELECT * FROM st WHERE st.state = 1)
                        GROUP BY pid) rs
                WHERE st.rn >= rs.maxrn
                  AND st.pid = rs.pid
               UNION
               --最后一条是delete的数据               
               SELECT audata_id, pid, state
                 FROM st t1
                WHERE NOT EXISTS (SELECT 1
                         FROM st
                        WHERE st.state = 1
                          AND st.pid = t1.pid))
  
   ';
    --记录母库中已经存在，外业中有要新增的数据
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           't');
    EXECUTE IMMEDIATE 'INSERT INTO TEMP_IX_ANNOTATION_EXT
      SELECT *
        FROM IX_ANNOTATION IA
       WHERE EXISTS
       (SELECT 1
                FROM AU_IX_ANNOTATION t
               WHERE t.PID = ia.pid
                 AND ' || v_oprstatus_clause || '
                 AND t.field_modify_flag LIKE ''%新增%''                 
                 AND EXISTS (SELECT 1
                        FROM temp_au_ix_anno_uniq_task tmp
                       WHERE tmp.pid = t.pid))';
    DELETE FROM temp_his_ix_annotation;
    INSERT INTO temp_his_ix_annotation
      SELECT * FROM ix_annotation;
    reset_tmp_xi_anot_name();
  END;

  PROCEDURE do_copy_c1 IS
    v_pid_count NUMBER := 0;
  BEGIN
    DELETE FROM temp_ix_anot_name_mg_1;
    SELECT COUNT(1)
      INTO v_pid_count
      FROM temp_ix_anot_name_mg ipn
     WHERE ipn.lang_code IN ('CHI', 'CHT')
       AND ipn.name_class = 2
       AND NOT EXISTS (SELECT 1
              FROM ix_annotation_name t
             WHERE t.pid = ipn.pid
               AND t.lang_code IN ('CHI', 'CHT')
               AND t.name_class = 1);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_pid_count);
      INSERT INTO temp_ix_anot_name_mg_1 WITH rs AS
      (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM temp_ix_anot_name_mg
        GROUP BY pid)
        SELECT pid_man.pid_nextval('IX_ANNOTATION_NAME') AS name_id,
               nvl(rs.name_groupid, 1) as name_groupid,
               ipn.pid,
               lang_code,
               NAME,
               phonetic,
               1 AS name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM temp_ix_anot_name_mg ipn,rs
         WHERE ipn.lang_code IN ('CHI', 'CHT')
           AND ipn.name_class = 2
           AND ipn.pid = rs.pid(+)     
           AND NOT EXISTS (SELECT 1
                  FROM ix_annotation_name t
                 WHERE t.pid = ipn.pid
                   AND t.lang_code IN ('CHI', 'CHT')
                   AND t.name_class = 1);
      INSERT INTO temp_ix_anot_name_mg
        SELECT * FROM temp_ix_anot_name_mg_1;
      DELETE FROM temp_ix_anot_name_mg_1;
    END IF;
  
  END;
  PROCEDURE copy_name_class1_data IS
  
  BEGIN
    MERGE INTO ix_annotation_name ipn
    USING (SELECT *
             FROM temp_ix_anot_name_mg mg
            WHERE mg.lang_code IN ('CHI', 'CHT')
              AND mg.name_class = 2
              AND EXISTS (SELECT 1
                     FROM ix_annotation_name t
                    WHERE t.pid = mg.pid
                      AND t.lang_code IN ('CHI', 'CHT')
                      AND t.name_class = 1)) aurs
    ON (ipn.pid = aurs.pid AND ipn.lang_code IN('CHI', 'CHT') AND ipn.name_class = 1)
    WHEN MATCHED THEN
      UPDATE SET ipn.name = aurs.name, ipn.phonetic = aurs.phonetic;
    do_copy_c1();
  EXCEPTION
    WHEN OTHERS THEN
      IF (SQLCODE = -20999) THEN
        do_copy_c1();
      ELSE
        RAISE;
      END IF;
  END;
  PROCEDURE exe_insert_anot_name(v_pid_count       IN OUT NUMBER,
                                 v_oprstatus_claus VARCHAR2,
                                 v_not             VARCHAR2) IS
  BEGIN
    DELETE FROM temp_ix_anot_name_mg; --为生成履历，采用零食表保存新增的数据    
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_pid_count);
      --ix_annotation_name
      EXECUTE IMMEDIATE '
      INSERT INTO temp_ix_anot_name_mg WITH rs AS
      (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name
        GROUP BY pid)
      SELECT PID_MAN.PID_NEXTVAL(''IX_ANNOTATION_NAME'') as NAME_ID,
             nvl(rs.name_groupid, 1) as name_groupid,
             n.pid,
             n.lang_code,
             n.name,
             n.phonetic,
             n.name_class,
             n.old_name,
             0 AS u_record,
             NULL AS u_fields
        FROM au_ix_annotation anno, au_ix_annotation_name n, rs
       WHERE anno.pid = n.pid
         AND anno.pid = rs.pid(+)     
         AND ' || v_oprstatus_claus || '
         AND anno.field_modify_flag LIKE ''%新增%''
         AND EXISTS (SELECT 1
                FROM temp_au_ix_anno_uniq_task tmp
               WHERE tmp.pid = anno.pid)
         AND ' || v_not ||
                        ' EXISTS
       (SELECT 1 FROM TEMP_IX_ANNOTATION_EXT ia WHERE ia.pid = anno.pid)';
    END IF;
    copy_name_class1_data();
    INSERT INTO ix_annotation_name
      SELECT * FROM temp_ix_anot_name_mg;
  END;
  PROCEDURE do_add_annot_name(v_merge_type VARCHAR2, v_param VARCHAR2) IS
    v_oprstatus_claus VARCHAR2(100);
    v_pid_count       NUMBER;
    v_not             VARCHAR2(3);
  BEGIN
    DELETE FROM temp_ix_anot_name_mg; --为生成履历，采用零食表保存新增的数据    
    v_oprstatus_claus := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'anno');
    --需要申请pid
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not 要不为NOT ，要不为''
    --申请PID
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
         FROM au_ix_annotation anno, au_ix_annotation_name n
       WHERE anno.pid = n.pid
         AND anno.field_modify_flag LIKE ''%新增%''
         AND ' || v_oprstatus_claus || '        
         AND EXISTS (SELECT 1
                FROM temp_au_ix_anno_uniq_task tmp
               WHERE tmp.pid = anno.pid)
         AND ' || v_not ||
                      ' EXISTS
       (SELECT 1 FROM TEMP_IX_ANNOTATION_EXT ia WHERE ia.pid = anno.pid)'
      INTO v_pid_count;
    exe_insert_anot_name(v_pid_count, v_oprstatus_claus, v_not);
  
  EXCEPTION
    WHEN OTHERS THEN
      IF (SQLCODE = -20999) THEN
        exe_insert_anot_name(v_pid_count, v_oprstatus_claus, v_not);
      ELSE
        RAISE;
      END IF;
  END;

  PROCEDURE do_add_annotation(v_merge_type VARCHAR2) IS
    v_oprstatus_claus VARCHAR2(100);
  BEGIN
    v_oprstatus_claus := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'anno');
    --ix_annotation
    EXECUTE IMMEDIATE 'INSERT INTO ix_annotation
       SELECT pid,
         kind_code,
         geometry,
         rank,
         src_flag,
         src_pid,
         client_flag,
         spectial_flag,
         region_id,
         mesh_id,
         edit_flag,
         dif_groupid,
         reserved,
         modify_flag,
         field_modify_flag,
         extract_info,
         extract_priority,
         remark,
         detail_flag,
         0                 AS task_id,
         data_version,
         field_task_id,
         0                 AS u_record,
         NULL              AS u_fields
       from au_ix_annotation anno
       WHERE anno.field_modify_flag LIKE ''%新增%''
         AND ' || v_oprstatus_claus || '
         AND EXISTS (SELECT 1
                FROM temp_au_ix_anno_uniq_task tmp
               WHERE tmp.pid = anno.pid)
         AND NOT EXISTS
       (SELECT 1 FROM TEMP_IX_ANNOTATION_EXT ia WHERE ia.pid = anno.pid)';
    do_add_annot_name(v_merge_type, 'NOT');
  
  END;
  PROCEDURE process_att_add_annotation IS
  BEGIN
    do_add_annotation(merge_utils.merge_type_att);
  END;
  PROCEDURE save_poi_modify_log(p_rec temp_au_ann_modify_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into temp_au_ann_modify_log(
       audata_id
      ,pid
      ,name_flag
      ,address_flag
      ,tel_flag
      ,kind_flag
      ,post_code_flag
      ,food_type_flag
      ,parent_flag
      ,lable_flag
      ,display_point_flag
      ,guide_point_flag
       ) values (:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12)'
      USING p_rec.audata_id, p_rec.pid, p_rec.name_flag, p_rec.address_flag, p_rec.tel_flag, p_rec.kind_flag, p_rec.post_code_flag, p_rec.food_type_flag, p_rec.parent_flag, p_rec.lable_flag, p_rec.display_point_flag, p_rec.guide_point_flag;
  END;
  PROCEDURE process_att_modify_log IS
    v_rec         temp_au_ann_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_ann_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.field_modify_flag AS log
                  FROM au_ix_annotation a
                 WHERE a.field_modify_flag LIKE '%改%'
                   AND a.att_oprstatus in( 0,1)
                   AND EXISTS
                 (SELECT 1 FROM ix_annotation p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ix_anno_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      IF instr(v_log, '改名称') > 0 THEN
        v_rec.name_flag := 1;
        v_change_flag   := TRUE;
      ELSE
        v_rec.name_flag := 0;
      END IF;
      IF instr(v_log, '改分类') > 0 THEN
        v_rec.kind_flag := 1;
        v_change_flag   := TRUE;
      ELSE
        v_rec.kind_flag := 0;
      END IF;
      IF v_change_flag = TRUE THEN
        save_poi_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_att_mod_ix_anot IS
  BEGIN
    MERGE INTO ix_annotation ia
    USING (SELECT auia.pid, auia.kind_code
             FROM au_ix_annotation auia
            WHERE EXISTS (SELECT 1
                     FROM temp_au_ann_modify_log tmp
                    WHERE tmp.kind_flag = 1
                      AND tmp.audata_id = auia.audata_id)) rs
    ON (ia.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE SET ia.kind_code = rs.kind_code;
  END;
  PROCEDURE mod_anot_name_add_c1(is_ext BOOLEAN) IS
    v_count NUMBER;
    v_view  VARCHAR2(100);
  BEGIN
    /*DELETE FROM temp_ix_anot_name_mg; --为生成履历，采用零食表保存新增的数据  */
    v_view := CASE is_ext
                WHEN FALSE THEN
                 'view_mg_au_ix_anot_name'
                WHEN TRUE THEN
                 'view_mg_au_ix_anot_name_ext'
              END;
    EXECUTE IMMEDIATE '
      SELECT COUNT(1)  FROM ix_annotation_name ipn
       WHERE ipn.lang_code IN (''CHI'', ''CHT'')
         AND ipn.name_class = 2
         AND EXISTS (SELECT 1
                FROM ' || v_view ||
                      ' aurs
               WHERE aurs.pid = ipn.pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_annotation_name t
               WHERE t.pid = ipn.pid
                 AND t.lang_code IN (''CHI'', ''CHT'')
                 AND t.name_class = 1)'
      INTO v_count;
    IF (v_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_anot_name_mg
         WITH rs AS
      (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name
        GROUP BY pid)
        SELECT pid_man.pid_nextval(''IX_ANNOTATION_NAME''),
               nvl(rs.name_groupid, 1) as name_groupid,
               ipn.pid,
               lang_code,
               NAME,
               phonetic,
               1 as name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM ix_annotation_name ipn,rs
         WHERE ipn.lang_code IN (''CHI'', ''CHT'')
           AND ipn.name_class = 2
           AND ipn.pid = rs.pid(+)   
           AND EXISTS (SELECT 1
                  FROM ' || v_view ||
                        ' aurs
                 WHERE aurs.pid = ipn.pid)
           AND NOT EXISTS (SELECT 1
                  FROM ix_annotation_name t
                 WHERE t.pid = ipn.pid
                   AND t.lang_code IN (''CHI'', ''CHT'')
                   AND t.name_class = 1)';
      /*INSERT INTO ix_annotation_name
      SELECT * FROM temp_ix_anot_name_mg;   */
    END IF;
  END;
  PROCEDURE process_att_mod_ix_anot_name IS
  BEGIN
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在，但是AU_IX_ANNOTATION_NAME中没有数据的，将IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的数据删除
    DELETE FROM ix_annotation_name ian
     WHERE ian.lang_code IN ('CHI', 'CHT')
       AND ian.name_class IN (1, 2)
       AND EXISTS (SELECT 1
              FROM au_ix_annotation auia
             WHERE auia.pid = ian.pid               
               AND auia.field_modify_flag LIKE '%改名称%'
               AND auia.att_oprstatus in( 0,1))
       AND NOT EXISTS (SELECT 1
              FROM au_ix_annotation_name au
             WHERE au.lang_code = 'CHI'
               AND au.name_class = 2
               AND au.pid = ian.pid)  ;
    --view_mg_au_ix_anot_name
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用AU_IX_ANNOTATION_NAME中名称分类为原始（2）的修改IX_ANNOTATION_NAME 中NAME、phonetic 的值
    ----同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值
    MERGE INTO ix_annotation_name ian
    USING view_mg_au_ix_anot_name aurs
    ON (ian.pid = aurs.pid AND ian.lang_code = aurs.lang_code AND ian.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE SET ian.name = aurs.name, ian.phonetic = aurs.phonetic;
    MERGE INTO ix_annotation_name ian
    USING view_mg_au_ix_anot_name aurs
    ON (ian.pid = aurs.pid AND ian.lang_code = aurs.lang_code AND ian.name_class = 1)
    WHEN MATCHED THEN
      UPDATE SET ian.name = aurs.name, ian.phonetic = aurs.phonetic;
    mod_anot_name_add_c1(FALSE);
  END;
  --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的不存在,则用AU_IX_ANNOTATION_NAME中名称分类为原始（2）的增加一条记录
  PROCEDURE process_att_mod_anot_name_add IS
    v_pid_count NUMBER;
  BEGIN
    /*DELETE FROM temp_ix_anot_name_mg; */ --为生成履历，采用零食表保存新增的数据
    --申请PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_annotation_name auipn
     WHERE auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND EXISTS (SELECT 1
              FROM temp_au_ann_modify_log tmp
             WHERE tmp.name_flag = 1
               AND tmp.pid = auipn.pid)
       AND NOT EXISTS (SELECT *
              FROM ix_annotation_name ipn
             WHERE ipn.lang_code IN ('CHI', 'CHT')
               AND ipn.name_class = 2
               AND ipn.pid = auipn.pid);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_pid_count);
      INSERT INTO temp_ix_anot_name_mg WITH rs AS
      (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name
        GROUP BY pid)
        SELECT pid_man.pid_nextval('IX_ANNOTATION_NAME'),
               nvl(rs.name_groupid,1) as name_groupid,
               auipn.pid,
               lang_code,
               NAME,
               phonetic,
               name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_annotation_name auipn,rs
         WHERE auipn.pid=rs.pid(+)
           and auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND EXISTS (SELECT 1
                  FROM temp_au_ann_modify_log tmp
                 WHERE tmp.name_flag = 1
                   AND tmp.pid = auipn.pid)
           AND NOT EXISTS (SELECT *
                  FROM ix_annotation_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 2
                   AND ipn.pid = auipn.pid);
    
    END IF;
    copy_name_class1_data();
  END;
  PROCEDURE commit_ix_anot_insert IS
  BEGIN
    INSERT INTO ix_annotation_name
      SELECT * FROM temp_ix_anot_name_mg;
  END;
  PROCEDURE do_del_annotation(v_merge_type VARCHAR2) IS
    v_oprstatus_claus VARCHAR2(100);
  BEGIN
    v_oprstatus_claus := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'anno');
    --删除子表 IX_ANNOTATION_FLAG
    EXECUTE IMMEDIATE 'DELETE FROM IX_ANNOTATION_FLAG ian
     WHERE EXISTS
     (SELECT 1
              FROM au_ix_annotation anno
             WHERE ian.pid = anno.pid
               AND anno.field_modify_flag LIKE ''%删除%''
               AND ' || v_oprstatus_claus || '  
               AND EXISTS
             (SELECT 1 FROM ix_annotation ia WHERE ia.pid = anno.pid)
               AND not EXISTS (SELECT 1
                      FROM temp_au_ix_anno_mul_task tmp
                     WHERE tmp.pid = anno.pid))';
    --删除子表 ix_annotation_name
    EXECUTE IMMEDIATE 'DELETE FROM ix_annotation_name ian
     WHERE EXISTS
     (SELECT 1
              FROM au_ix_annotation anno
             WHERE ian.pid = anno.pid
               AND anno.field_modify_flag LIKE ''%删除%''
               AND ' || v_oprstatus_claus || '  
               AND EXISTS
             (SELECT 1 FROM ix_annotation ia WHERE ia.pid = anno.pid)
               AND not EXISTS (SELECT 1
                      FROM temp_au_ix_anno_mul_task tmp
                     WHERE tmp.pid = anno.pid))';
    --delete from ix_annotation
    EXECUTE IMMEDIATE ' DELETE FROM ix_annotation ian
     WHERE EXISTS
     (SELECT 1
              FROM au_ix_annotation anno
             WHERE ian.pid = anno.pid
               AND anno.field_modify_flag LIKE ''%删除%''
                AND ' || v_oprstatus_claus || '  
               AND EXISTS
             (SELECT 1 FROM ix_annotation ia WHERE ia.pid = anno.pid)
               AND not EXISTS (SELECT 1
                      FROM temp_au_ix_anno_mul_task tmp
                     WHERE tmp.pid = anno.pid))';
  END;
  PROCEDURE process_att_delete_poi IS
  BEGIN
    do_del_annotation(merge_utils.merge_type_att);
  END;
  PROCEDURE process_geo_delete_poi IS
  BEGIN
    do_del_annotation(merge_utils.merge_type_geo);
  
  END;
  PROCEDURE process_geo_add_annotation IS
  BEGIN
    do_add_annotation(merge_utils.merge_type_geo);
  
  END;
  PROCEDURE process_geo_modify_log IS
    v_rec         temp_au_ann_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_ann_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.field_modify_flag AS log
                  FROM au_ix_annotation a
                 WHERE a.field_modify_flag LIKE '%改%'
                   AND a.geo_oprstatus in( 0,1)
                   AND EXISTS
                 (SELECT 1 FROM ix_annotation p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ix_anno_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      IF instr(v_log, '改位移') > 0 THEN
        v_rec.guide_point_flag := 1;
        v_change_flag          := TRUE;
      ELSE
        v_rec.guide_point_flag := 0;
      END IF;
      IF v_change_flag = TRUE THEN
        save_poi_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_attgeo_modify_log IS
    v_rec         temp_au_ann_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_ann_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.field_modify_flag AS log,a.geo_oprstatus,a.att_oprstatus
                  FROM au_ix_annotation a
                 WHERE a.field_modify_flag LIKE '%改%'
                   AND (a.geo_oprstatus in( 0,1) or a.att_oprstatus in(0,1))
                   AND EXISTS
                 (SELECT 1 FROM ix_annotation p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ix_anno_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      
      IF (rec.att_oprstatus in(0,1)  and instr(v_log, '改名称') > 0 )THEN
        v_rec.name_flag := 1;
        v_change_flag   := TRUE;
      ELSE
        v_rec.name_flag := 0;
      END IF;
      IF( rec.att_oprstatus in(0,1)  and instr(v_log, '改分类') > 0) THEN
        v_rec.kind_flag := 1;
        v_change_flag   := TRUE;
      ELSE
        v_rec.kind_flag := 0;
      END IF;
      IF (rec.geo_oprstatus in(0,1) and instr(v_log, '改位移') > 0) THEN
        v_rec.guide_point_flag := 1;
        v_change_flag          := TRUE;
      ELSE
        v_rec.guide_point_flag := 0;
      END IF;
      IF v_change_flag = TRUE THEN
        save_poi_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_geo_mod_ix_annotation IS
  BEGIN
    --备份ix_annotation
    DELETE FROM temp_his_ix_annotation;
    INSERT INTO temp_his_ix_annotation
      SELECT * FROM ix_annotation;
    --
    MERGE INTO ix_annotation ixan
    USING (SELECT anno.pid, anno.geometry,anno.region_id,anno.mesh_id 
             FROM au_ix_annotation anno, temp_au_ann_modify_log l
            WHERE anno.field_modify_flag LIKE '%改%'
              AND anno.geo_oprstatus in( 0,1)
              AND anno.pid = l.pid
              AND (l.display_point_flag = 1 OR l.guide_point_flag = 1)
              AND EXISTS
            (SELECT 1 FROM ix_annotation ia WHERE ia.pid = anno.pid)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_anno_mul_task tmp
                    WHERE tmp.pid = anno.pid)) rs
    ON (ixan.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ixan.geometry  = rs.geometry,
             ixan.region_id = rs.region_id,
             ixan.mesh_id   = rs.mesh_id;
  END;
  PROCEDURE process_att_geo_delete_poi IS
  BEGIN
    do_del_annotation(merge_utils.merge_type_geoatt);
  END;
  PROCEDURE process_att_geo_add_poi IS
  BEGIN
    do_add_annotation(merge_utils.merge_type_geoatt);
  END;
  --已存在融合 geo
  PROCEDURE geo_add_anot_ext IS
  BEGIN
    --融合主表属性           
    MERGE INTO ix_annotation ip
    USING (SELECT *
             FROM au_ix_annotation auip
            WHERE auip.field_modify_flag LIKE '%新增%'
              AND auip.geo_oprstatus in( 0,1)
              AND auip.pid IN (SELECT pid FROM temp_ix_annotation_ext)) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.geometry  = rs.geometry,
             ip.region_id = rs.region_id,
             ip.mesh_id   = rs.mesh_id;
  END;
  PROCEDURE att_add_anot_ext IS
  BEGIN
    MERGE INTO ix_annotation ip
    USING (SELECT *
             FROM au_ix_annotation auip
            WHERE auip.field_modify_flag LIKE '%新增%'
              AND auip.att_oprstatus in( 0,1)
              AND auip.pid IN (SELECT pid FROM temp_ix_annotation_ext)) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.kind_code         = rs.kind_code,
             ip.rank              = rs.rank,
             ip.src_flag          = rs.src_flag,
             ip.src_pid           = rs.src_pid,
             ip.client_flag       = rs.client_flag,
             ip.spectial_flag     = rs.spectial_flag,
             --ip.region_id         = rs.region_id,
             --ip.mesh_id           = rs.mesh_id,
             ip.edit_flag         = rs.edit_flag,
             ip.modify_flag       = rs.modify_flag,
             ip.field_modify_flag = rs.field_modify_flag,
             ip.extract_info      = rs.extract_info,
             ip.extract_priority  = rs.extract_priority,
             ip.remark            = rs.remark,
             ip.detail_flag       = rs.detail_flag,
             ip.data_version      = rs.data_version,
             ip.field_task_id     = rs.field_task_id;
  END;
  PROCEDURE att_add_anot_name_ext IS
  BEGIN
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在，但是AU_IX_ANNOTATION_NAME中没有数据的，将IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的数据删除
    DELETE FROM ix_annotation_name ian
     WHERE ian.lang_code IN ('CHI', 'CHT')
       AND ian.name_class IN (1, 2)
       AND EXISTS
     (SELECT 1 FROM temp_ix_annotation_ext ext WHERE ext.pid = ian.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_annotation_name au
             WHERE au.lang_code = 'CHI'
               AND au.name_class = 2
               AND au.pid = ian.pid);
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用AU_IX_ANNOTATION_NAME中名称分类为原始（2）的修改IX_ANNOTATION_NAME 中NAME、phonetic 的值
    --同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值
    MERGE INTO ix_annotation_name ian
    USING view_mg_au_ix_anot_name_ext aurs
    ON (ian.pid = aurs.pid AND ian.lang_code = aurs.lang_code AND ian.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE SET ian.name = aurs.name, ian.phonetic = aurs.phonetic;
    MERGE INTO ix_annotation_name ian
    USING view_mg_au_ix_anot_name_ext aurs
    ON (ian.pid = aurs.pid AND ian.lang_code = aurs.lang_code AND ian.name_class = 1)
    WHEN MATCHED THEN
      UPDATE SET ian.name = aurs.name, ian.phonetic = aurs.phonetic;
    mod_anot_name_add_c1(TRUE);
  END;
  --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的不存在,则用AU_IX_ANNOTATION_NAME中名称分类为原始（2）的增加一条记录
  PROCEDURE att_add_anot_name_ext_add IS
    v_pid_count NUMBER;
  BEGIN
    DELETE FROM temp_ix_anot_name_mg; --为生成履历，采用零食表保存新增的数据    
    --申请PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_annotation_name auipn
     WHERE auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND EXISTS (SELECT 1
              FROM temp_ix_annotation_ext tmp
             WHERE tmp.pid = auipn.pid)
       AND NOT EXISTS (SELECT *
              FROM ix_annotation_name ipn
             WHERE ipn.lang_code IN ('CHI', 'CHT')
               AND ipn.name_class = 2
               AND ipn.pid = auipn.pid);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_pid_count);
      INSERT INTO temp_ix_anot_name_mg WITH rs AS
      (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name
        GROUP BY pid)
        SELECT pid_man.pid_nextval('IX_ANNOTATION_NAME'),
               nvl(rs.name_groupid,1) as name_groupid,
               auipn.pid,
               lang_code,
               NAME,
               phonetic,
               name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_annotation_name auipn,rs
         WHERE auipn.pid=rs.pid(+) 
           and auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND EXISTS (SELECT 1
                  FROM temp_ix_annotation_ext tmp
                 WHERE tmp.pid = auipn.pid)
           AND NOT EXISTS (SELECT *
                  FROM ix_annotation_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 2
                   AND ipn.pid = auipn.pid);
    
    END IF;
    copy_name_class1_data();
    /*INSERT INTO ix_annotation_name
    SELECT * FROM temp_ix_anot_name_mg;*/
  END;
  PROCEDURE process_name_groupid IS
  BEGIN
    --如果有新增name的，需要重新处理name_groupid
    MERGE INTO ix_annotation_name ipn
    USING (SELECT name_id,
                  pid,
                  row_number() over(PARTITION BY pid ORDER BY 1) rn
             FROM ix_annotation_name ppn
            WHERE EXISTS (SELECT 1
                     FROM temp_ix_anot_name_mg mg
                    WHERE ppn.pid = mg.pid)) rs
    ON (ipn.name_id = rs.name_id)
    WHEN MATCHED THEN
      UPDATE SET ipn.name_groupid = rs.rn;
  END;

  PROCEDURE mul_att_add_anot_ext(v_audata_id NUMBER) IS
  BEGIN
    EXECUTE IMMEDIATE '
   MERGE INTO ix_annotation ip
    USING (SELECT *
             FROM au_ix_annotation auip
            WHERE  auip.att_oprstatus in( 0,1)
              AND auip.audata_id=:v_audata_id) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.kind_code         = rs.kind_code,
             ip.rank              = rs.rank,
             ip.src_flag          = rs.src_flag,
             ip.src_pid           = rs.src_pid,
             ip.client_flag       = rs.client_flag,
             ip.spectial_flag     = rs.spectial_flag,
             --ip.region_id         = rs.region_id,
             --ip.mesh_id           = rs.mesh_id,
             ip.edit_flag         = rs.edit_flag,
             ip.modify_flag       = rs.modify_flag,
             ip.field_modify_flag = rs.field_modify_flag,
             ip.extract_info      = rs.extract_info,
             ip.extract_priority  = rs.extract_priority,
             ip.remark            = rs.remark,
             ip.detail_flag       = rs.detail_flag,
             ip.data_version      = rs.data_version,
             ip.field_task_id     = rs.field_task_id'
      USING v_audata_id;
  END;
  PROCEDURE mul_geo_add_anot_ext(v_audata_id NUMBER) IS
  BEGIN
    --融合主表属性 
    EXECUTE IMMEDIATE '          
    MERGE INTO ix_annotation ip
    USING (SELECT *
             FROM au_ix_annotation auip
            WHERE auip.geo_oprstatus in( 0,1)
              AND auip.audata_id = :v_audata_id) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.geometry  = rs.geometry,
             ip.region_id = rs.region_id,
             ip.mesh_id   = rs.mesh_id '
      USING v_audata_id;
  END;
  PROCEDURE reset_tmp_ix_ann(v_pid NUMBER) IS
  BEGIN
    EXECUTE IMMEDIATE '
    DELETE FROM temp_his_ix_annotation tmp WHERE tmp.pid = :v_pid'
      USING v_pid;
    EXECUTE IMMEDIATE '    INSERT INTO temp_his_ix_annotation
      SELECT * FROM ix_annotation WHERE pid = :v_pid'
      USING v_pid;
  END;
  PROCEDURE mul_mod_annoname_add_c1(v_data_id NUMBER) IS
    v_count NUMBER;
  BEGIN
    EXECUTE IMMEDIATE '
      SELECT COUNT(1)  FROM ix_annotation_name ipn
       WHERE ipn.lang_code IN (''CHI'', ''CHT'')
         AND ipn.name_class = 2
         AND EXISTS (SELECT 1
                  FROM au_ix_annotation_name au
                 WHERE au.audata_id = :1
                   AND au.lang_code IN (''CHI'', ''CHT'')
                   AND au.name_class = 2 AND ipn.pid = au.pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_annotation_name t
               WHERE t.pid = ipn.pid
                 AND t.lang_code IN (''CHI'', ''CHT'')
                 AND t.name_class = 1)'
      INTO v_count
      USING v_data_id;
    IF (v_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_anot_name_mg t
       WITH rs AS
      (SELECT ian.pid, nvl(MAX(ian.name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name  ian ,au_ix_annotation au   
         where ian.pid=au.pid
         and au.audata_id=:v_data_id   
        GROUP BY ian.pid)
        SELECT pid_man.pid_nextval(''IX_ANNOTATION_NAME''),
               nvl(rs.name_groupid,1) as name_groupid,
               ipn.pid,
               lang_code,
               NAME,
               phonetic,
               1 as name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM ix_annotation_name ipn,rs
         WHERE ipn.lang_code IN (''CHI'', ''CHT'')
           AND ipn.name_class = 2
           and ipn.pid=rs.pid(+) 
           AND EXISTS (SELECT 1
                  FROM au_ix_annotation_name au
                 WHERE au.audata_id = :1
                   AND au.lang_code IN (''CHI'', ''CHT'')
                   AND au.name_class = 2 AND ipn.pid = au.pid)
           AND NOT EXISTS (SELECT 1
                  FROM ix_annotation_name t
                 WHERE t.pid = ipn.pid
                   AND t.lang_code IN (''CHI'', ''CHT'')
                   AND t.name_class = 1)'
        USING v_data_id,v_data_id;
    END IF;
  
  END;
  PROCEDURE mul_att_add_anot_name_ext(v_audata_id NUMBER) IS
  BEGIN
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在，但是AU_IX_ANNOTATION_NAME中没有数据的，将IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的数据删除
    DELETE FROM ix_annotation_name ian
     WHERE ian.lang_code IN ('CHI', 'CHT')
       AND ian.name_class IN (1, 2)
       AND EXISTS (SELECT 1
              FROM au_ix_annotation ext
             WHERE ext.pid = ian.pid
               AND ext.audata_id = v_audata_id)
       AND NOT EXISTS
     (SELECT 1 FROM au_ix_annotation_name au WHERE au.pid = ian.pid);
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用AU_IX_ANNOTATION_NAME中名称分类为原始（2）的修改IX_ANNOTATION_NAME 中NAME、phonetic 的值
    --同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值
    --ix_annotation_name表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用au_ix_annotation_name中名称分类为原始（2）的修改ix_annotation_name 中NAME、NAME_PHONETIC 的值  
    MERGE INTO ix_annotation_name ipn
    USING (SELECT pid, lang_code, name_class, NAME, phonetic
             FROM view_mg_mul_ix_anot_name
            WHERE audata_id = v_audata_id) aurs
    ON (ipn.pid = aurs.pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE SET ipn.name = aurs.name, ipn.phonetic = aurs.phonetic;
    --同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值
    MERGE INTO ix_annotation_name ipn
    USING (SELECT pid, lang_code, name_class, NAME, phonetic
             FROM view_mg_mul_ix_anot_name
            WHERE audata_id = v_audata_id) aurs
    ON (ipn.pid = aurs.pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = 1)
    WHEN MATCHED THEN
      UPDATE SET ipn.name = aurs.name, ipn.phonetic = aurs.phonetic;
    mul_mod_annoname_add_c1(v_audata_id);
  END;
  PROCEDURE mul_att_add_poiname_ext_add(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    /*DELETE FROM temp_ix_anot_name_mg; --为生成履历，采用零食表保存新增的数据    */
    --申请PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_annotation_name auipn
     WHERE auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND auipn.audata_id = v_audata_id
       AND NOT EXISTS (SELECT *
              FROM ix_annotation_name ipn
             WHERE ipn.lang_code IN ('CHI', 'CHT')
               AND ipn.name_class = 2
               AND ipn.pid = auipn.pid);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_pid_count);
      INSERT INTO temp_ix_anot_name_mg WITH rs AS
      (SELECT pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name
        GROUP BY pid)
        SELECT pid_man.pid_nextval('IX_ANNOTATION_NAME'),
               nvl(rs.name_groupid,1) as name_groupid,
               auipn.pid,
               lang_code,
               NAME,
               phonetic,
               name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_annotation_name  auipn,rs
         WHERE auipn.pid=rs.pid(+) 
           and  auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND auipn.audata_id = v_audata_id
           AND NOT EXISTS (SELECT *
                  FROM ix_annotation_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 2
                   AND ipn.pid = auipn.pid);
    
    END IF;
    copy_name_class1_data();
  END;
  PROCEDURE mul_add_anot_name(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    DELETE FROM temp_ix_anot_name_mg;
    --申请PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_annotation_name au
     WHERE au.audata_id = v_audata_id;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_ANNOTATION_NAME', v_pid_count);
      --IX_ANNOTATION_NAME
      INSERT INTO temp_ix_anot_name_mg
      WITH rs AS
      (SELECT ian.pid, nvl(MAX(ian.name_groupid), 0) + 1 AS name_groupid
         FROM ix_annotation_name  ian ,au_ix_annotation au   
         where ian.pid=au.pid
         and au.audata_id=v_audata_id   
        GROUP BY ian.pid)
        SELECT pid_man.pid_nextval('IX_ANNOTATION_NAME'),
               nvl(rs.name_groupid,1) as name_groupid,
               c.pid,
               lang_code,
               NAME,
               phonetic,
               name_class,
               old_name,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_annotation_name c,rs
         WHERE c.audata_id = v_audata_id and c.pid=rs.pid(+);
    END IF;
    copy_name_class1_data();
    INSERT INTO ix_annotation_name
      SELECT * FROM temp_ix_anot_name_mg;
  END;
  PROCEDURE mul_add_annotation(v_audata_id NUMBER) IS
  BEGIN
    INSERT INTO ix_annotation
     SELECT pid,
         kind_code,
         geometry,
         rank,
         src_flag,
         src_pid,
         client_flag,
         spectial_flag,
         region_id,
         mesh_id,
         edit_flag,
         dif_groupid,
         reserved,
         modify_flag,
         field_modify_flag,
         extract_info,
         extract_priority,
         remark,
         detail_flag,
         0                 AS task_id,
         data_version,
         field_task_id,
         0                 AS u_record,
         NULL              AS u_fields
       from au_ix_annotation p
       WHERE p.audata_id = v_audata_id;
    mul_add_anot_name(v_audata_id);
  
  END;
  PROCEDURE mul_del_poi(v_pid NUMBER) IS
  BEGIN
    --删除子表 IX_ANNOTATION_FLAG
    EXECUTE IMMEDIATE 'DELETE FROM IX_ANNOTATION_FLAG ian
     WHERE ian.pid=:v_pid'
      USING v_pid;
    --删除子表 ix_annotation_name
    EXECUTE IMMEDIATE 'DELETE FROM ix_annotation_name ian
      WHERE ian.pid=:v_pid'
      USING v_pid;
    --delete from ix_annotation
    EXECUTE IMMEDIATE ' DELETE FROM ix_annotation ian
     WHERE ian.pid=:v_pid'
      USING v_pid;
  END;
  
  PROCEDURE mul_mod_poi_name(v_data_id NUMBER) IS
  BEGIN
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在，但是AU_IX_ANNOTATION_NAME中没有数据的，将IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的数据删除
    DELETE FROM ix_annotation_name ian
     WHERE ian.lang_code IN ('CHI', 'CHT')
       AND ian.name_class IN (1, 2)
       AND EXISTS (SELECT 1
              FROM au_ix_annotation auia
             WHERE auia.pid = ian.pid
               AND auia.audata_id = v_data_id
               AND auia.att_oprstatus in( 0,1))
       AND NOT EXISTS (SELECT 1
              FROM au_ix_annotation_name au
             WHERE au.lang_code = 'CHI'
               AND au.name_class = 2
               AND au.pid = ian.pid);
    --view_mg_au_ix_anot_name
    --IX_ANNOTATION_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用AU_IX_ANNOTATION_NAME中名称分类为原始（2）的修改IX_ANNOTATION_NAME 中NAME、phonetic 的值
    ----同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值
    MERGE INTO ix_annotation_name ian
    USING (SELECT *
             FROM view_mg_mul_ix_anot_name v
            WHERE v.audata_id = v_data_id) aurs
    ON (ian.pid = aurs.pid AND ian.lang_code = aurs.lang_code AND ian.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE SET ian.name = aurs.name, ian.phonetic = aurs.phonetic;
    MERGE INTO ix_annotation_name ian
    USING (SELECT *
             FROM view_mg_mul_ix_anot_name v
            WHERE v.audata_id = v_data_id) aurs
    ON (ian.pid = aurs.pid AND ian.lang_code = aurs.lang_code AND ian.name_class = 1)
    WHEN MATCHED THEN
      UPDATE SET ian.name = aurs.name, ian.phonetic = aurs.phonetic;
    mul_mod_annoname_add_c1(v_data_id);
  END;
  PROCEDURE mul_att_mod_ix_poi(v_data_id NUMBER) IS
  BEGIN
    EXECUTE IMMEDIATE 'MERGE INTO ix_annotation ia
    USING (SELECT auia.pid, auia.kind_code
             FROM au_ix_annotation auia
            WHERE auia.audata_id=:v_data_id) rs
    ON (ia.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE SET ia.kind_code = rs.kind_code'
      USING v_data_id;
  END;
  PROCEDURE mul_geo_mod_ix_poi(v_data_id NUMBER) IS
  BEGIN
    EXECUTE IMMEDIATE '
    MERGE INTO ix_annotation ixan
    USING (SELECT anno.pid, anno.geometry,anno.region_id,anno.mesh_id
             FROM au_ix_annotation anno
             where anno.audata_id=:v_data_id
             ) rs
    ON (ixan.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ixan.geometry  = rs.geometry,
             ixan.region_id = rs.region_id,
             ixan.mesh_id   = rs.mesh_id '
      USING v_data_id;
  END;
  PROCEDURE mul_mod_poi_state_ext(v_pid   NUMBER,
                                  v_state NUMBER,
                                  v_log   ix_annotation.field_modify_flag%TYPE) IS
  BEGIN
    UPDATE ix_annotation p1
       SET p1.field_modify_flag = v_log
     WHERE p1.pid = v_pid;
  END;
  PROCEDURE reset_temp_ixanot_name(v_audata_id NUMBER) IS
  BEGIN
    EXECUTE IMMEDIATE 'DELETE FROM temp_his_ix_annot_name t where exists (select 1 from au_ix_annotation au where audata_id=:v_audata_id and au.pid=t.pid)'
      USING v_audata_id;
    EXECUTE IMMEDIATE 'INSERT INTO temp_his_ix_annot_name
        SELECT * FROM ix_annotation_name  t where exists (select 1 from au_ix_annotation au where audata_id=:v_audata_id and au.pid=t.pid)'
      USING v_audata_id;
  END;
 PROCEDURE process_mod_poi_state(v_merge_type VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE '
    MERGE INTO ix_annotation p1
    USING (SELECT p2.pid, p2.field_modify_flag
         FROM au_ix_annotation p2, temp_au_ann_modify_log tmp
        WHERE p2.audata_id = tmp.audata_id and (' ||
                      merge_utils.get_proxypoi_clause(v_merge_type, 'p2') || ')) v
     ON (p1.pid = v.pid)
     WHEN MATCHED THEN
     UPDATE SET p1.field_modify_flag = v.field_modify_flag  ';  
  END;
END merge_au_ix_annotation_no_his;
/
