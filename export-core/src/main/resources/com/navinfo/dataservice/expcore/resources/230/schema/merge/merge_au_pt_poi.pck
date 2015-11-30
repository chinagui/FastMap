CREATE OR REPLACE PACKAGE merge_au_pt_poi IS

  -- Author  : MAYF
  -- Created : 2011/12/28 15:58:24
  -- Purpose : 融合pt_poi
  PROCEDURE reset_temp_mg_table;
  PROCEDURE reset_tmp_pt_poi;
  PROCEDURE reset_tmp_pt_poi_name;
  PROCEDURE pre_process(v_merge_type VARCHAR2);
  PROCEDURE att_add_pt_poi;
  PROCEDURE process_att_modify_log;
  PROCEDURE process_attgeo_modify_log;
  PROCEDURE process_att_pt_poi_mod; --基本属性修改
  PROCEDURE process_att_pt_poi_name_mod_s;
  PROCEDURE process_att_pt_poi_name_mod; --名称修改
  PROCEDURE process_att_pt_poi_rel_mod; --父子关系修改
  PROCEDURE att_del_pt_poi;

  PROCEDURE geo_add_pt_poi;
  PROCEDURE process_geo_modify_log;
  PROCEDURE geo_del_pt_poi;
  PROCEDURE delete_isolated_pt_poi_parent;
  PROCEDURE process_geo_pt_poi_mod;

  PROCEDURE process_att_geo_delete_poi;
  PROCEDURE att_geo_add_pt_poi;

  PROCEDURE att_add_pt_poi_ext;
  PROCEDURE att_add_pt_poiname_ext;
  PROCEDURE att_add_pt_poiname_ext_add;
  PROCEDURE process_name_groupid;
  PROCEDURE att_add_relation_ext;
  PROCEDURE att_add_etaaccess_ext;
  PROCEDURE att_add_eta_stop_ext;
  PROCEDURE geo_add_pt_poi_ext;

  PROCEDURE mod_main_poi_state_ext;

  PROCEDURE process_mod_poi_state;

  PROCEDURE commit_poi_name_insert;
  PROCEDURE process_att_pt_eta_stop_mod;
  PROCEDURE process_att_pt_eta_access_mod;

END merge_au_pt_poi;
/
CREATE OR REPLACE PACKAGE BODY merge_au_pt_poi IS
  PROCEDURE reset_temp_mg_table IS
  BEGIN
    DELETE FROM temp_pt_poi_parent_mg;
    DELETE FROM temp_pt_poi_name_mg;
  END;
  PROCEDURE reset_tmp_pt_poi IS
  BEGIN
    --复制pt_poi的备份，用来生成融合履历
    DELETE FROM temp_his_pt_poi;
    INSERT INTO temp_his_pt_poi
      SELECT * FROM pt_poi;
  END;
  PROCEDURE reset_tmp_pt_poi_name IS
  BEGIN
    DELETE FROM temp_his_pt_poi_name;
    INSERT INTO temp_his_pt_poi_name
      SELECT * FROM pt_poi_name;
  END;
  PROCEDURE reset_tmp_pt_parent IS
  BEGIN
    DELETE FROM temp_his_pt_poi_parent;
    INSERT INTO temp_his_pt_poi_parent
      SELECT * FROM pt_poi_parent;
  END;
  PROCEDURE pre_process(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           't');
    DELETE FROM temp_au_ptpoi_mul_task;
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_ptpoi_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_pt_poi t
                WHERE ' || v_oprstatus_clause || ' 
               GROUP BY t.pid
              HAVING COUNT(1) > 1) rs';
    DELETE FROM temp_ptpoi_ext;
    EXECUTE IMMEDIATE 'INSERT INTO temp_ptpoi_ext
            SELECT * FROM pt_poi pt 
                   WHERE EXISTS(SELECT 1 FROM au_pt_poi t WHERE t.state=1 AND ' ||
                      v_oprstatus_clause || ' AND t.pid=pt.pid ) 
                      and not exists(select 1 from temp_au_ptpoi_mul_task mul where mul.pid=pt.pid)';
    reset_tmp_pt_poi();
    reset_tmp_pt_poi_name();
    reset_tmp_pt_parent();
  END;
  PROCEDURE copy_name_class1_data IS
    v_pid_count NUMBER := 0;
  BEGIN
    MERGE INTO pt_poi_name ipn
    USING (SELECT *
             FROM temp_pt_poi_name_mg mg
            WHERE mg.lang_code IN ('CHI', 'CHT')
              AND mg.name_class = 2
              AND EXISTS (SELECT 1
                     FROM pt_poi_name t
                    WHERE t.poi_pid = mg.poi_pid
                      AND t.lang_code IN ('CHI', 'CHT')
                      AND t.name_class = 1)) aurs
    ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code IN('CHI', 'CHT') AND ipn.name_class = 1)
    WHEN MATCHED THEN
      UPDATE SET ipn.name = aurs.name, ipn.phonetic = aurs.phonetic;
    EXECUTE IMMEDIATE 'SELECT COUNT(1)
  FROM temp_pt_poi_name_mg ipn
 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
   AND ipn.name_class = 2
   AND NOT EXISTS (SELECT 1
          FROM pt_poi_name t
         WHERE t.poi_pid = ipn.poi_pid
           AND t.lang_code IN (''CHI'', ''CHT'')
           AND t.name_class = 1)
         '
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('PT_POI_NAME', v_pid_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_pt_poi_name_mg1
       WITH rs AS
   (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
      FROM temp_pt_poi_name_mg
     GROUP BY poi_pid)
      SELECT  PID_MAN.PID_NEXTVAL(''PT_POI_NAME'') as NAME_ID,
            nvl(rs.name_groupid, 1) AS name_groupid,
            ipn.poi_pid,
            ipn.lang_code,
            1 AS name_class,
            ipn.NAME,
            ipn.phonetic,          
            ipn.nidb_pid,
            0 AS u_record,
            NULL AS u_fields
         FROM temp_pt_poi_name_mg ipn,rs
 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
   AND ipn.name_class = 2
   AND ipn.poi_pid = rs.poi_pid(+)
   AND NOT EXISTS (SELECT 1
          FROM pt_poi_name t
         WHERE t.poi_pid = ipn.poi_pid
           AND t.lang_code IN (''CHI'', ''CHT'')
           AND t.name_class = 1)
         ';
    END IF;
    INSERT INTO temp_pt_poi_name_mg
      SELECT * FROM temp_pt_poi_name_mg1;
    DELETE FROM temp_pt_poi_name_mg1;
  
  END;

  PROCEDURE do_add_name(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
  BEGIN
    DELETE FROM temp_pt_poi_name_mg;
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'auptp');
    EXECUTE IMMEDIATE 'SELECT COUNT(1) 
    FROM au_pt_poi_name auptpn, au_pt_poi auptp       
               WHERE auptp.audata_id = auptpn.audata_id
                 AND auptp.state = 1
                 AND ' || v_oprstatus_clause || '
                 and not exists(select 1 from temp_ptpoi_ext ext where ext.pid=auptp.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ptpoi_mul_task tmp
                       WHERE tmp.pid = auptp.pid)'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('PT_POI_NAME', v_pid_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_pt_poi_name_mg WITH rs AS
  (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_poi_name
    GROUP BY poi_pid)
      SELECT PID_MAN.PID_NEXTVAL(''PT_POI_NAME'') as NAME_ID,
             nvl(rs.name_groupid, 1) AS name_groupid,
             auptpn.POI_PID,
             auptpn.LANG_CODE,
             auptpn.NAME_CLASS,
             auptpn.NAME,
             auptpn.PHONETIC,
             auptpn.NIDB_PID,
             0                   AS U_RECORD,
             NULL                AS U_FIELDS
        FROM au_pt_poi_name auptpn, au_pt_poi auptp, rs     
   WHERE auptp.audata_id = auptpn.audata_id
     AND auptp.state = 1
     AND ' || v_oprstatus_clause || '
     AND auptp.pid = rs.poi_pid(+)
     AND NOT EXISTS
   (SELECT 1 FROM temp_ptpoi_ext ext WHERE ext.pid = auptp.pid)
     AND NOT EXISTS
   (SELECT 1 FROM temp_au_ptpoi_mul_task tmp WHERE tmp.pid = auptp.pid)';
      --copy_name_class1_data();
      INSERT INTO pt_poi_name
        SELECT * FROM temp_pt_poi_name_mg;
    END IF;
  END;
  PROCEDURE do_add_relation(v_merge_type VARCHAR2) IS
  BEGIN
   DELETE FROM temp_pt_poi_parent_mg;
    IF (merge_utils.merge_type_geo != v_merge_type) THEN
        EXECUTE IMMEDIATE 'INSERT INTO temp_pt_poi_parent_mg
        SELECT GROUP_ID,PARENT_POI_PID,U_RECORD,U_FIELDS FROM (
      SELECT 
             GROUP_ID,
             PARENT_POI_PID, 
             0 AS U_RECORD, 
             1 AS U_FIELDS,
             row_number() over(PARTITION BY parent_poi_pid ORDER BY 1) AS rn   
        FROM au_pt_poi_parent auppp
       WHERE EXISTS (SELECT 1
                FROM au_pt_poi_children auppc
               WHERE EXISTS (SELECT 1
                        FROM au_pt_poi auptp
                       WHERE auptp.audata_id = auppc.audata_id
                         AND auptp.state = 1
                         AND auptp.att_oprstatus=0
                         and not exists(select 1 from temp_ptpoi_ext ext where ext.pid=auptp.pid)
                         AND NOT EXISTS
                       (SELECT 1
                                FROM temp_au_ptpoi_mul_task tmp
                               WHERE tmp.pid = auptp.pid))
                 AND auppc. group_id = auppp.group_id and auppc.FIELD_TASK_ID=auppp.FIELD_TASK_ID)
         AND NOT EXISTS (SELECT 1

                FROM temp_his_pt_poi_parent ppp
               WHERE ppp.PARENT_POI_PID = auppp.PARENT_POI_PID)
               ) RS WHERE RS.RN=1
               ';
      INSERT INTO pt_poi_parent
        SELECT * FROM temp_pt_poi_parent_mg;
    END IF;
    --添加子表
    EXECUTE IMMEDIATE 'INSERT INTO pt_poi_children
      SELECT group_id,child_poi_pid,relation_type, u_record,U_FIELDS
        FROM (SELECT IPP.GROUP_ID,
                     C.CHILD_POI_PID,
                     C.RELATION_TYPE,
                     0 as U_RECORD,
                     NULL as U_FIELDS,
                     ROW_NUMBER() OVER(PARTITION BY IPP.GROUP_ID, C.CHILD_POI_PID, C.RELATION_TYPE ORDER BY 1) AS RN
                FROM AU_PT_POI          L,
                     AU_PT_POI_CHILDREN C,
                     pt_poi_parent      IPP,
                     AU_PT_POI_PARENT   AUIPP
               WHERE L.STATE = 1
                 AND L.ATT_OPRSTATUS = 0
                 AND C.audata_id = L.audata_id
                 AND AUIPP.GROUP_ID = C.GROUP_ID
                 AND AUIPP.PARENT_POI_PID = IPP.PARENT_POI_PID
                 AND  NOT  EXISTS
               (SELECT 1 FROM temp_ptpoi_ext EXT WHERE EXT.PID = L.PID)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ptpoi_mul_task TMP
                       WHERE TMP.PID = L.PID)) RS
       WHERE RS.RN = 1 ' --有多个外业任务的不融合
    ;
  END;
  PROCEDURE do_add_pt_poi(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'auptp');
    --add pt_poi
    EXECUTE IMMEDIATE 'INSERT INTO pt_poi
      SELECT auptp.PID,
             auptp.POI_KIND,
             auptp.GEOMETRY,
             auptp.X_GUIDE,
             auptp.Y_GUIDE,
             auptp.LINK_PID,
             auptp.SIDE,
             auptp.NAME_GROUPID,
             auptp.ROAD_FLAG,
             auptp.PMESH_ID,
             auptp.CITY_CODE,
             auptp.ACCESS_CODE,
             auptp.ACCESS_TYPE,
             auptp.ACCESS_METH,
             auptp.MESH_ID_5K,
             auptp.MESH_ID,
             auptp.REGION_ID,
             auptp.EDIT_FLAG,
             auptp.POI_MEMO,
             auptp.OPERATOR,
             auptp.UPDATE_TIME,
             auptp.LOG,
             auptp.EDITION_FLAG,
             auptp.STATE,
             auptp.POI_NUM,
             0                   AS TASK_ID,
             auptp.DATA_VERSION,
             auptp.FIELD_TASK_ID,
             0                   AS U_RECORD,
             NULL                AS U_FIELDS
        FROM au_pt_poi auptp
       WHERE auptp.state = 1
         AND ' || v_oprstatus_clause ||
                      '         
         and not exists(select 1 from temp_ptpoi_ext ext where ext.pid=auptp.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ptpoi_mul_task tmp
               WHERE tmp.pid = auptp.pid)';
    --add PT_POI_NAME
    do_add_name(v_merge_type);
  
    --处理父子关系
  
    do_add_relation(v_merge_type);  
    
  END;
  PROCEDURE save_pt_modify_log(p_rec temp_au_pt_poi_modify_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into TEMP_AU_PT_POI_MODIFY_LOG(
        PID,
        NAME_FLAG,
        KIND_FLAG,
        GUIDE_POINT_FLAG,
        DISPLAY_POINT_FLAG,
        POI_MEMO_FLAG,
        PARENT_FLAG,
        ACCESS_TYPE_FLAG,
        ACCESS_METH_FLAG,
        AUDATA_ID
       ) values (:V_PID,
                :V_NAME_FLAG,
                :V_KIND_FLAG,
                :V_GUIDE_POINT_FLAG,
                :V_DISPLAY_POINT_FLAG,
                :V_POI_MEMO_FLAG,
                :V_PARENT_FLAG,
                :V_ACCESS_TYPE_FLAG,
                :V_ACCESS_METH_FLAG,
                :V_AUDATA_ID
                )'
      USING p_rec.pid, p_rec.name_flag, p_rec.kind_flag, p_rec.guide_point_flag, p_rec.display_point_flag, p_rec.poi_memo_flag, p_rec.parent_flag, p_rec.access_type_flag, p_rec.access_meth_flag,
    p_rec.audata_id;
  END;

  PROCEDURE att_add_pt_poi IS
  BEGIN
    do_add_pt_poi(merge_utils.merge_type_att);
  END;
  PROCEDURE do_att_parse_log(v_log         VARCHAR2,
                             v_rec         IN OUT temp_au_pt_poi_modify_log%ROWTYPE,
                             v_change_flag IN OUT BOOLEAN) IS
  BEGIN
    IF instr(v_log, '改名称') > 0 THEN
      v_rec.name_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.name_flag := 0;
    END IF;
    IF instr(v_log, '改种别') > 0 THEN
      v_rec.kind_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.kind_flag := 0;
    END IF;
    IF instr(v_log, '改备注') > 0 THEN
      v_rec.poi_memo_flag := 1;
      v_change_flag       := TRUE;
    ELSE
      v_rec.poi_memo_flag := 0;
    END IF;
    IF instr(v_log, '改父子关系') > 0 THEN
      v_rec.parent_flag := 1;
      v_change_flag     := TRUE;
    ELSE
      v_rec.parent_flag := 0;
    END IF;
    IF instr(v_log, '改出入口类型') > 0 THEN
      v_rec.access_type_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.access_type_flag := 0;
    END IF;
    IF instr(v_log, '改到达方式') > 0 THEN
      v_rec.access_meth_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.access_meth_flag := 0;
    END IF;   
  
  END;
  PROCEDURE do_geo_parse_log(v_log         VARCHAR2,
                             v_rec         IN OUT temp_au_pt_poi_modify_log%ROWTYPE,
                             v_change_flag IN OUT BOOLEAN) IS
  BEGIN
    IF instr(v_log, '改位移') > 0 THEN
      v_rec.guide_point_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.guide_point_flag := 0;
    END IF;
    IF instr(v_log, '改显示坐标') > 0 THEN
      v_rec.display_point_flag := 1;
      v_change_flag            := TRUE;
    ELSE
      v_rec.display_point_flag := 0;
    END IF;
  END;
  PROCEDURE process_att_modify_log IS
    v_rec         temp_au_pt_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_poi_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.log
                  FROM au_pt_poi a
                 WHERE a.log IS NOT NULL
                   AND a.state = 3
                   AND a.att_oprstatus = 0
                   AND EXISTS
                 (SELECT 1 FROM pt_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ptpoi_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
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
  PROCEDURE process_attgeo_modify_log IS
    v_rec         temp_au_pt_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_poi_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.log,a.att_oprstatus,a.geo_oprstatus
                  FROM au_pt_poi a
                 WHERE a.log IS NOT NULL
                   AND a.state = 3
                   AND (a.att_oprstatus = 0 OR a.geo_oprstatus = 0)
                   AND EXISTS
                 (SELECT 1 FROM pt_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ptpoi_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      if rec.att_oprstatus=0 then 
         do_att_parse_log(v_log, v_rec, v_change_flag);
      end if;
      if rec.geo_oprstatus=0 then
         do_geo_parse_log(v_log, v_rec, v_change_flag);
      end if ;
      IF v_change_flag = TRUE THEN
        save_pt_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  --TODO:父子关系这块可以考虑和ix_poi的父子关系、pointaddress的父子关系处理重构
  PROCEDURE process_att_pt_poi_rel_mod IS
  BEGIN
    DELETE FROM temp_pt_poi_parent_mg;
    --把原来的子表删除
    DELETE FROM pt_poi_children ppc
     WHERE EXISTS (SELECT 1
              FROM temp_au_pt_poi_modify_log tmp
             WHERE ppc.child_poi_pid = tmp.pid
               AND tmp.parent_flag = 1);
               
      --1.修改的是子，将父和子都copy到内业中      
      INSERT INTO temp_pt_poi_parent_mg
      SELECT group_id,PARENT_POI_PID,U_RECORD,U_FIELDS FROM (
        SELECT 
               group_id,
               parent_poi_pid,
               0 AS U_RECORD,
               NULL AS U_FIELDS,
               row_number() over(PARTITION BY parent_poi_pid ORDER BY 1) AS rn 
          FROM au_pt_poi_parent p
         WHERE p.group_id IN
               (SELECT c.group_id
                  FROM temp_au_pt_poi_modify_log tmp, au_pt_poi_children c
                 WHERE tmp.parent_flag = 1
                   AND c.audata_id = tmp.audata_id)
           AND NOT EXISTS
         (SELECT 1
                  FROM pt_poi_parent ip
                 WHERE ip.parent_poi_pid = p.parent_poi_pid)) RS WHERE RS.RN=1;
      INSERT INTO pt_poi_parent
        SELECT * FROM temp_pt_poi_parent_mg;
  
    --添加子表    
    INSERT INTO pt_poi_children
      SELECT group_id, child_poi_pid, relation_type, u_record, u_fields
        FROM (SELECT ipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields,
                     row_number() over(PARTITION BY ipp.group_id, c.child_poi_pid, c.relation_type ORDER BY 1) AS rn
                FROM temp_au_pt_poi_modify_log l,
                     au_pt_poi_children        c,
                     pt_poi_parent             ipp,
                     au_pt_poi_parent          auipp
               WHERE l.parent_flag = 1
                 AND c.audata_id = l.audata_id
                 AND auipp.group_id = c.group_id
                 AND auipp.parent_poi_pid = ipp.parent_poi_pid) rs
       WHERE rs.rn = 1;
  END;
  --TODO:考虑和isExt=true的情况进行重构；考虑和ix_poi的名称修改的代码进行重构
  PROCEDURE process_att_pt_poi_name_mod_s IS
    v_pid_count NUMBER;
  BEGIN
    EXECUTE IMMEDIATE 'SELECT COUNT(1) 
    FROM au_pt_poi_name au, temp_au_pt_poi_modify_log l
      WHERE au.audata_id = l.audata_id
        AND l.name_flag = 1
        AND au.lang_code IN (''CHI'', ''CHT'')
        AND au.name_class = 1
        AND NOT EXISTS (SELECT 1
               FROM temp_his_pt_poi_name p
              WHERE p.lang_code IN (''CHI'', ''CHT'')
                AND p.name_class = 1
                AND p.poi_pid = au.poi_pid)
        AND NOT EXISTS (SELECT 1
               FROM temp_au_ptpoi_mul_task tmp
              WHERE tmp.pid = au.poi_pid)'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('PT_POI_NAME', v_pid_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_pt_poi_name_mg WITH rs AS
  (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_poi_name
    GROUP BY poi_pid)
      SELECT PID_MAN.PID_NEXTVAL(''PT_POI_NAME'') as NAME_ID,
             nvl(rs.name_groupid, 1) AS name_groupid,
             auptpn.POI_PID,
             auptpn.LANG_CODE,
             auptpn.NAME_CLASS,
             auptpn.NAME,
             auptpn.PHONETIC,
             auptpn.NIDB_PID,
             0                   AS U_RECORD,
             NULL                AS U_FIELDS
        FROM  au_pt_poi_name auptpn, temp_au_pt_poi_modify_log auptp, rs     
   WHERE auptp.audata_id = auptpn.audata_id
     AND auptp.name_flag = 1    
     AND auptp.pid = rs.poi_pid(+)
     AND au.lang_code IN (''CHI'', ''CHT'')
     AND au.name_class = 1
      AND NOT EXISTS (SELECT 1
               FROM temp_his_pt_poi_name p
              WHERE p.lang_code IN (''CHI'', ''CHT'')
                AND p.name_class = 1
                AND p.poi_pid = auptpn.poi_pid)
     AND NOT EXISTS
   (SELECT 1 FROM temp_au_ptpoi_mul_task tmp WHERE tmp.pid = auptp.pid)';     
      INSERT INTO pt_poi_name
        SELECT * FROM temp_pt_poi_name_mg;
    END IF;
  END;
  /*修改时，如果name_class=1的不存在，但是name_class=2的存在，需要用name_class=2的复制一条，增加到ix_poi-name中*/
  PROCEDURE mod_poi_name_add_c1(is_ext BOOLEAN) IS
    v_pid_count NUMBER;
    v_view  VARCHAR2(100);
    v_not varchar2(20);   
  BEGIN
    if is_ext then
      v_not:='not';
      else
        v_not:='';
      end if ;
   DELETE FROM temp_pt_poi_name_mg;    
    EXECUTE IMMEDIATE 'SELECT COUNT(1) 
    FROM au_pt_poi_name auptpn, au_pt_poi auptp       
               WHERE auptp.audata_id = auptpn.audata_id
                 AND auptp.state = 3
                 AND auptp.att_oprstatus=0
                 and auptpn.lang_code in (''CHI'',''CHT'')
                 AND auiptpn.name_class=1
                 and not exists(select 1 from pt_poi_name pt where pt.name_id=auptpn.name_id)
                 and '||v_not||' exists(select 1 from temp_ptpoi_ext ext where ext.pid=auptp.pid)                 
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ptpoi_mul_task tmp
                       WHERE tmp.pid = auptp.pid)'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('PT_POI_NAME', v_pid_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_pt_poi_name_mg WITH rs AS
  (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_poi_name
    GROUP BY poi_pid)
      SELECT PID_MAN.PID_NEXTVAL(''PT_POI_NAME'') as NAME_ID,
             nvl(rs.name_groupid, 1) AS name_groupid,
             auptpn.POI_PID,
             auptpn.LANG_CODE,
             auptpn.NAME_CLASS,
             auptpn.NAME,
             auptpn.PHONETIC,
             auptpn.NIDB_PID,
             0                   AS U_RECORD,
             NULL                AS U_FIELDS
        FROM au_pt_poi_name auptpn, au_pt_poi auptp, rs     
   WHERE auptp.audata_id = auptpn.audata_id
     AND auptp.state = 1
     AND auptp.att_oprstatus=0
     AND auptp.pid = rs.poi_pid(+)
     and auptpn.lang_code in (''CHI'',''CHT'')
     AND auiptpn.name_class=1
     and not exists(select 1 from pt_poi_name pt where pt.name_id=auptpn.name_id)
     AND '||v_not||' EXISTS
   (SELECT 1 FROM temp_ptpoi_ext ext WHERE ext.pid = auptp.pid)
     AND NOT EXISTS
   (SELECT 1 FROM temp_au_ptpoi_mul_task tmp WHERE tmp.pid = auptp.pid)';
   end if;
      --copy_name_class1_data();
      INSERT INTO pt_poi_name
        SELECT * FROM temp_pt_poi_name_mg; 
                                                            
  END;
  PROCEDURE process_att_pt_poi_name_mod IS
    v_pid_count NUMBER:=0;
  BEGIN
   DELETE FROM temp_pt_poi_name_mg;
   --Lang_Code为”CHI”、名称分类为标准化（1）的记录是否存在，如果不存在则增加一条记录，;否则修改名称分类为标准化（1）的记录的名称内容、名称发音的值  
   MERGE INTO pt_poi_name ipn
   USING VIEW_MG_AU_PT_POI_NAME  aurs
   ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = aurs.name_class)
   WHEN MATCHED THEN
     UPDATE SET ipn.name = aurs.name, ipn.phonetic = aurs.phonetic;
     
   
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理POI名称时出错' || SQLERRM);
      --rollback;
      RAISE;
  END;
  --FIXME:STATE,LOG,EDITION_FLAG需要单独处理
  PROCEDURE process_att_pt_poi_mod IS
  BEGIN
  
    --融合 pt_poi  
    MERGE INTO pt_poi pt
    USING (SELECT aupt.pid,
                  aupt.poi_kind,
                  aupt.poi_memo,
                  aupt.access_type,
                  aupt.access_meth,
                  aupt.log,
                  aupt.state,
                  '外业修改' AS edition_flag,
                  l.kind_flag,
                  l.poi_memo_flag,
                  l.access_type_flag,
                  l.access_meth_flag
             FROM au_pt_poi aupt, temp_au_pt_poi_modify_log l
            WHERE aupt.pid = l.pid
              AND (l.kind_flag = 1 OR l.poi_memo_flag = 1 OR
                  l.access_type_flag = 1 OR l.access_meth_flag = 1)) rs
    ON (pt.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET pt.poi_kind    = decode(rs.kind_flag,
                                     1,
                                     rs.poi_kind,
                                     pt.poi_kind),
             pt.poi_memo    = decode(rs.poi_memo_flag,
                                     1,
                                     rs.poi_memo,
                                     pt.poi_memo),
             pt.access_type = decode(rs.access_type_flag,
                                     1,
                                     rs.access_type,
                                     pt.access_type),
             pt.access_meth = decode(rs.access_meth_flag,
                                     1,
                                     rs.access_meth,
                                     pt.access_meth)
      --pt.log          = rs.log,
      --pt.state        = rs.state,
      --pt.edition_flag = rs.edition_flag
      ;
  
  END;
  PROCEDURE process_att_pt_eta_access_mod IS
  BEGIN
    DELETE FROM pt_eta_access eta
     WHERE EXISTS (SELECT 1
              FROM temp_au_pt_poi_modify_log log
             WHERE log.pid = eta.poi_pid
               AND (log.eta_stop_fare_area = 1 OR
                   log.eta_access_alias_name = 1 OR
                   log.eta_access_open_period = 1 OR
                   log.eta_access_manual_ticket = 1 OR
                   log.eta_access_m_ticket_period = 1 OR
                   log.eta_access_auto_ticket = 1));
    INSERT INTO pt_eta_access eta
      SELECT au.poi_pid,
             au.alias_name,
             au.alias_pinyin,
             au.open_period,
             au.manual_ticket,
             au.manual_ticket_period,
             au.auto_ticket,
             au.log,
             au.edition_flag,
             au.state,
             0                       AS u_record,
             NULL                    AS u_fields
        FROM au_pt_eta_access au, temp_au_pt_poi_modify_log log
       WHERE au.audata_id = log.audata_id
         AND (log.eta_stop_fare_area = 1 OR log.eta_access_alias_name = 1 OR
             log.eta_access_open_period = 1 OR
             log.eta_access_manual_ticket = 1 OR
             log.eta_access_m_ticket_period = 1 OR
             log.eta_access_auto_ticket = 1);
  END;
  PROCEDURE process_att_pt_eta_stop_mod IS
  BEGIN
    DELETE FROM pt_eta_stop eta
     WHERE EXISTS
     (SELECT 1
              FROM temp_au_pt_poi_modify_log log
             WHERE log.pid = eta.poi_pid
               AND (log.eta_stop_alias_name = 1 OR
                   log.eta_stop_private_park = 1 OR
                   log.eta_stop_p_park_period = 1 OR
                   log.eta_stop_carport_exact = 1 OR
                   log.eta_stop_carport_estimate = 1 OR
                   log.eta_stop_bike_park = 1 OR
                   log.eta_stop_b_park_period = 1 OR
                   log.eta_stop_manual_ticket = 1 OR
                   log.eta_stop_m_ticket_period = 1 OR
                   log.eta_stop_mobile = 1 OR
                   log.eta_stop_baggage_security = 1 OR
                   log.eta_stop_left_baggage = 1 OR
                   log.eta_stop_consignation_exact = 1 OR
                   log.eta_stop_consignation_estimate = 1 OR
                   log.eta_stop_convenient = 1 OR log.eta_stop_smoke = 1 OR
                   log.eta_stop_build_type = 1 OR
                   log.eta_stop_auto_ticket = 1 OR log.eta_stop_toilet = 1 OR
                   log.eta_stop_wifi = 1 OR log.eta_stop_open_period = 1));
    INSERT INTO pt_eta_stop eta
      SELECT au.poi_pid,
             au.alias_name,
             au.alias_pinyin,
             au.private_park,
             au.private_park_period,
             au.carport_exact,
             au.carport_estimate,
             au.bike_park,
             au.bike_park_period,
             au.manual_ticket,
             au.manual_ticket_period,
             au.mobile,
             au.baggage_security,
             au.left_baggage,
             au.consignation_exact,
             au.consignation_estimate,
             au.convenient,
             au.smoke,
             au.build_type,
             au.auto_ticket,
             au.toilet,
             au.wifi,
             au.open_period,
             au.fare_area,
             au.log,
             au.edition_flag,
             au.state,
             0                        AS u_record,
             NULL                     AS u_fields      
        FROM au_pt_eta_stop au, temp_au_pt_poi_modify_log log
       WHERE au.audata_id = log.audata_id
         AND (log.eta_stop_alias_name = 1 OR log.eta_stop_private_park = 1 OR
             log.eta_stop_p_park_period = 1 OR
             log.eta_stop_carport_exact = 1 OR
             log.eta_stop_carport_estimate = 1 OR
             log.eta_stop_bike_park = 1 OR log.eta_stop_b_park_period = 1 OR
             log.eta_stop_manual_ticket = 1 OR
             log.eta_stop_m_ticket_period = 1 OR log.eta_stop_mobile = 1 OR
             log.eta_stop_baggage_security = 1 OR
             log.eta_stop_left_baggage = 1 OR
             log.eta_stop_consignation_exact = 1 OR
             log.eta_stop_consignation_estimate = 1 OR
             log.eta_stop_convenient = 1 OR log.eta_stop_smoke = 1 OR
             log.eta_stop_build_type = 1 OR log.eta_stop_auto_ticket = 1 OR
             log.eta_stop_toilet = 1 OR log.eta_stop_wifi = 1 OR
             log.eta_stop_open_period = 1);
  
  END;
  PROCEDURE do_del(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'aupp');
    --删除 父表;
    --父表删除时，需要将子表也删除
    EXECUTE IMMEDIATE 'DELETE FROM pt_poi_children c
     WHERE EXISTS
     (SELECT 1
              FROM (SELECT c.group_id
                      FROM au_pt_poi aupp, pt_poi_parent c
                     WHERE aupp.state = 2
                       AND ' || v_oprstatus_clause || '
                       AND NOT EXISTS (SELECT 1
                              FROM temp_au_ptpoi_mul_task tmp
                             WHERE tmp.pid = aupp.pid) --有多个外业任务的不融合 
                       AND c.PARENT_POI_PID = aupp.pid) v
             WHERE c.group_id = v.group_id)';
    ----删除的是父表;
    EXECUTE IMMEDIATE 'DELETE FROM pt_poi_parent p
     WHERE EXISTS
     (SELECT 1
              FROM (SELECT c.group_id
                      FROM au_pt_poi aupp, pt_poi_parent c
                     WHERE aupp.state = 2
                       AND ' || v_oprstatus_clause || '
                       AND NOT EXISTS (SELECT 1
                              FROM temp_au_ptpoi_mul_task tmp
                             WHERE tmp.pid = aupp.pid) --有多个外业任务的不融合
                       AND c.PARENT_POI_PID = aupp.pid) v
             WHERE p.group_id = v.group_id)';
  
    --删除子表
    ----删除的是儿子
    EXECUTE IMMEDIATE 'DELETE FROM pt_poi_children c
     WHERE EXISTS (SELECT 1
              FROM au_pt_poi aupp
             WHERE aupp.state = 2
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptpoi_mul_task tmp
                     WHERE tmp.pid = aupp.pid) --有多个外业任务的不融合
               AND c.child_poi_pid = aupp.pid)';
    --del pt_poi_name_tone
    EXECUTE IMMEDIATE ' DELETE FROM pt_poi_name_tone ppnt
     WHERE EXISTS (SELECT 1
              FROM au_pt_poi aupp, pt_poi_name ppn
             WHERE aupp.state = 2
               AND ' || v_oprstatus_clause || '
               AND EXISTS
             (SELECT 1 FROM pt_poi pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptpoi_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND aupp.pid = ppn.poi_pid
               AND ppn.name_id = ppnt.name_id)';
    --del pt_poi_name   
    EXECUTE IMMEDIATE 'DELETE FROM pt_poi_name ppn
     WHERE EXISTS (SELECT 1
              FROM au_pt_poi aupp
             WHERE aupp.state = 2
              AND ' || v_oprstatus_clause || '
               AND EXISTS
             (SELECT 1 FROM pt_poi pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptpoi_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND aupp.pid = ppn.poi_pid)';
    --del pt_poi_flag
    EXECUTE IMMEDIATE 'DELETE FROM pt_poi_flag ppf
     WHERE EXISTS (SELECT 1
              FROM au_pt_poi aupp
             WHERE aupp.state = 2
               AND ' || v_oprstatus_clause || '
               AND EXISTS
             (SELECT 1 FROM pt_poi pp WHERE pp.pid = aupp.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptpoi_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND ppf.poi_pid = aupp.pid)';  
    
    --del pt_poi
    EXECUTE IMMEDIATE 'DELETE FROM pt_poi pp
     WHERE EXISTS (SELECT 1
              FROM au_pt_poi aupp
             WHERE aupp.state = 2
              AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ptpoi_mul_task tmp
                     WHERE tmp.pid = aupp.pid)
               AND pp.pid = aupp.pid)';
  END;
  PROCEDURE att_del_pt_poi IS
  BEGIN
    do_del(merge_utils.merge_type_att);
  END;
  PROCEDURE geo_add_pt_poi IS
  BEGIN
    do_add_pt_poi(merge_utils.merge_type_geo);
  END;

  PROCEDURE process_geo_modify_log IS
    v_rec         temp_au_pt_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN
    DELETE FROM temp_au_pt_poi_modify_log;
    FOR rec IN (SELECT a.audata_id, a.pid, a.log
                  FROM au_pt_poi a
                 WHERE a.log IS NOT NULL
                   AND a.state = 3
                   AND a.geo_oprstatus = 0
                   AND EXISTS
                 (SELECT 1 FROM pt_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ptpoi_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      do_geo_parse_log(v_log, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_pt_modify_log(v_rec);
      END IF;
    END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('分析日志时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_geo_pt_poi_mod IS
  BEGIN
    --融合 pt_poi  
    MERGE INTO pt_poi pt
    USING (SELECT aupt.pid,
                  aupt.geometry,
                  aupt.x_guide,
                  aupt.y_guide,
                  aupt.log,
                  aupt.state,
                  aupt.link_pid,
                  aupt.side,
                  aupt.mesh_id,
                  aupt.pmesh_id,
                  '外业修改' AS edition_flag,
                  l.guide_point_flag,
                  l.display_point_flag
             FROM au_pt_poi aupt, temp_au_pt_poi_modify_log l
            WHERE aupt.pid = l.pid
              AND (l.guide_point_flag = 1 OR l.display_point_flag = 1)) rs
    ON (pt.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET pt.geometry     = decode(rs.display_point_flag,
                                      0,
                                      pt.geometry,
                                      rs.geometry),
             pt.mesh_id     = decode(rs.display_point_flag,
                                      0,
                                      pt.mesh_id,
                                      rs.mesh_id),
             pt.pmesh_id     = decode(rs.display_point_flag,
                                      0,
                                      pt.pmesh_id,
                                      rs.pmesh_id),                                                    
             pt.x_guide      = decode(rs.guide_point_flag,
                                      0,
                                      pt.x_guide,
                                      rs.x_guide),
             pt.y_guide      = decode(rs.guide_point_flag,
                                      0,
                                      pt.y_guide,
                                      rs.y_guide),
             pt.link_pid      = decode(rs.guide_point_flag,
                                      0,
                                      pt.link_pid,
                                      rs.link_pid),
             pt.side      = decode(rs.guide_point_flag,
                                        0,
                                        pt.side,
                                        rs.side)                                      
                                      ;
                                      
            -- pt.log          = rs.log,
            -- pt.state        = rs.state,
            -- pt.edition_flag = rs.edition_flag;
  END;
  PROCEDURE geo_del_pt_poi IS
  BEGIN
    do_del(merge_utils.merge_type_geo);
  END;
  --删除POI 孤父
  PROCEDURE delete_isolated_pt_poi_parent IS
  BEGIN
    DELETE FROM pt_poi_parent p
     WHERE NOT EXISTS
     (SELECT * FROM pt_poi_children c WHERE c.group_id = p.group_id);
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('删除孤父出错' || SQLERRM);
      --rollback;
      RAISE;
    
  END;
  PROCEDURE process_att_geo_delete_poi IS
  BEGIN
    do_del(merge_utils.merge_type_geoatt);
  END;
  PROCEDURE att_geo_add_pt_poi IS
  BEGIN
    do_add_pt_poi(merge_utils.merge_type_geoatt);
  END;
  PROCEDURE att_add_pt_poi_ext IS
  BEGIN
    MERGE INTO pt_poi pp
    USING (SELECT *
             FROM au_pt_poi au
            WHERE au.state = 1
              AND au.att_oprstatus = 0
              AND EXISTS
            (SELECT 1 FROM temp_ptpoi_ext tmp WHERE tmp.pid = au.pid)) aupp
    ON (pp.pid = aupp.pid)
    WHEN MATCHED THEN
      UPDATE
         SET pp.poi_kind = aupp.poi_kind,
             --pp.geometry      = aupp.geometry,
             --pp.x_guide       = aupp.x_guide,
             --pp.y_guide       = aupp.y_guide,
             --pp.link_pid      = aupp.link_pid,
             --pp.side          = aupp.side,
             pp.name_groupid = aupp.name_groupid,
             pp.road_flag    = aupp.road_flag,
             pp.pmesh_id     = aupp.pmesh_id,
             pp.city_code    = aupp.city_code,
             pp.access_code  = aupp.access_code,
             pp.access_type  = aupp.access_type,
             pp.access_meth  = aupp.access_meth,
             pp.mesh_id_5k   = aupp.mesh_id_5k,
             pp.mesh_id      = aupp.mesh_id,
             pp.region_id    = aupp.region_id,
             pp.edit_flag    = aupp.edit_flag,
             pp.poi_memo     = aupp.poi_memo,
             pp.operator     = aupp.operator,
             pp.update_time  = aupp.update_time,
             --pp.log           = aupp.log,
             pp.edition_flag = aupp.edition_flag,
             --pp.state         = aupp.state,
             pp.poi_num       = aupp.poi_num,
             pp.data_version  = aupp.data_version,
             pp.field_task_id = aupp.field_task_id;
  END;

  PROCEDURE commit_poi_name_insert IS
  BEGIN
    INSERT INTO pt_poi_name
      SELECT * FROM temp_pt_poi_name_mg;
  END;
  PROCEDURE att_add_pt_poiname_ext IS
  BEGIN   
    MERGE INTO pt_poi_name ppn
    USING view_mg_pt_poiname_ext aurs
    ON (ppn.poi_pid = aurs.poi_pid AND ppn.lang_code = aurs.lang_code AND ppn.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE SET ppn.name = aurs.name, ppn.phonetic = aurs.phonetic;    
  END;
  PROCEDURE att_add_pt_poiname_ext_add IS
    v_count NUMBER;
  BEGIN
    --pt_poi_name表中Lang_Code为”CHI”、名称类型为原始（2）的不存在,则用au_pt_poi_name中名称分类为原始（2）的增加一条记录
    --DELETE FROM temp_pt_poi_name_mg;
    --申请pid
    EXECUTE IMMEDIATE 'SELECT COUNT(1)
      FROM au_pt_poi_name auppn
     WHERE auppn.lang_code IN (''CHI'', ''CHT'')
       AND auppn.name_class = 1
       AND EXISTS
     (SELECT 1 FROM temp_ptpoi_ext ext,au_pt_poi au WHERE ext.pid = auppn.poi_pid and ext.pid=au.pid and au.att_oprstatus=0)     
       AND NOT EXISTS (SELECT 1
              FROM pt_poi_name ppn
             WHERE ppn.lang_code IN (''CHI'', ''CHT'')
               AND ppn.name_class = 1
               AND auppn.poi_pid = ppn.poi_pid)'
      INTO v_count;
    IF (v_count > 0) THEN
      pid_man.apply_pid('PT_POI_NAME', v_count);
      INSERT INTO temp_pt_poi_name_mg t WITH rs AS
  (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
     FROM pt_poi_name
    GROUP BY poi_pid)
        SELECT pid_man.pid_nextval('PT_POI_NAME'),
               nvl(rs.name_groupid, 1) AS name_groupid,
               auipn.poi_pid,
               lang_code,
               name_class,
               NAME,
               phonetic,
               nidb_pid,
               0 AS u_record,
               NULL AS u_fields
          FROM au_pt_poi_name auipn,rs
         WHERE auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 1
           AND auipn.poi_pid = rs.poi_pid(+)
           AND EXISTS (SELECT 1
                  FROM temp_ptpoi_ext tmp,au_pt_poi au
                 WHERE tmp.pid = auipn.poi_pid   and tmp.pid=au.pid and au.att_oprstatus=0)
           AND NOT EXISTS (SELECT 1
                  FROM pt_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 1
                   AND ipn.poi_pid = auipn.poi_pid);
    END IF;
  
  END;
  PROCEDURE process_name_groupid IS
  BEGIN
    --如果有新增name的，需要重新处理name_groupid
    MERGE INTO pt_poi_name ipn
    USING (SELECT name_id,
                  poi_pid,
                  row_number() over(PARTITION BY poi_pid ORDER BY 1) rn
             FROM pt_poi_name ppn
            WHERE EXISTS (SELECT 1
                     FROM temp_pt_poi_name_mg mg
                    WHERE ppn.poi_pid = mg.poi_pid)) rs
    ON (ipn.name_id = rs.name_id)
    WHEN MATCHED THEN
      UPDATE SET ipn.name_groupid = rs.rn;
  END;
  PROCEDURE att_add_relation_ext IS
  BEGIN
    DELETE FROM temp_pt_poi_parent_mg;
    DELETE FROM pt_poi_children ipc
     WHERE ipc.child_poi_pid IN (SELECT pid FROM temp_ptpoi_ext ext);

      --1.修改的是子，将父和子都copy到内业中      
      INSERT INTO temp_pt_poi_parent_mg
        SELECT group_id,
               parent_poi_pid,
               0, --U_RECORD,
               NULL --U_FIELDS
          FROM au_pt_poi_parent p
         WHERE p.group_id IN (SELECT c.group_id
                                FROM temp_ptpoi_ext l, au_pt_poi_children c
                               WHERE c.child_poi_pid = l.pid)
           AND NOT EXISTS
         (SELECT 1
                  FROM temp_his_pt_poi_parent ip
                 WHERE ip.parent_poi_pid = p.parent_poi_pid);
      INSERT INTO pt_poi_parent
        SELECT * FROM temp_pt_poi_parent_mg;
  
    --添加子表    
    INSERT INTO pt_poi_children
      SELECT group_id, child_poi_pid, relation_type, u_record, u_fields
        FROM (SELECT ipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields,
                     row_number() over(PARTITION BY ipp.group_id, c.child_poi_pid, c.relation_type ORDER BY 1) AS rn
                FROM temp_ptpoi_ext     l,
                     au_pt_poi_children c,
                     pt_poi_parent      ipp,
                     au_pt_poi_parent   auipp
               WHERE c.child_poi_pid = l.pid
                 AND auipp.group_id = c.group_id
                 AND auipp.parent_poi_pid = ipp.parent_poi_pid) rs
       WHERE rs.rn = 1;
  END;
  PROCEDURE att_add_eta_stop_ext IS
  BEGIN
    null;
  END;
  PROCEDURE att_add_etaaccess_ext IS
  BEGIN
    null;
  END;
  PROCEDURE geo_add_pt_poi_ext IS
  BEGIN
    MERGE INTO pt_poi pp
    USING (SELECT *
             FROM au_pt_poi au
            WHERE au.state = 1
              AND au.geo_oprstatus = 0
              AND EXISTS
            (SELECT 1 FROM temp_ptpoi_ext tmp WHERE tmp.pid = au.pid)) aupp
    ON (pp.pid = aupp.pid)
    WHEN MATCHED THEN
      UPDATE
         SET pp.geometry = aupp.geometry,
             pp.x_guide  = aupp.x_guide,
             pp.y_guide  = aupp.y_guide,
             pp.link_pid = aupp.link_pid,
             pp.side     = aupp.side;
  END;
  PROCEDURE mod_main_poi_state_ext IS
  BEGIN
    MERGE INTO pt_poi p1
    USING temp_ptpoi_ext v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE SET p1.state = 1;
  END;
  PROCEDURE process_mod_poi_state IS
  BEGIN
    MERGE INTO pt_poi p1
    USING (SELECT p2.pid, p2.log, p2.state, p2.edition_flag
             FROM au_pt_poi p2
            WHERE p2.state = 3
              AND (p2.att_oprstatus = 0 OR p2.geo_oprstatus = 0)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ptpoi_mul_task mul
                    WHERE mul.pid = p2.pid)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.log = v.log, p1.state = 3, p1.edition_flag = '外业修改';
  END;
END merge_au_pt_poi;
/