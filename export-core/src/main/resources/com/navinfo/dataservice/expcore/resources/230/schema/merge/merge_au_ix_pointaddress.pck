CREATE OR REPLACE PACKAGE merge_au_ix_pointaddress IS

  -- Author  : MAYF
  -- Created : 2011/12/27 9:24:24
  -- Purpose : 点门牌融合
  /*融合预处理：将如果一个文字被多个外业任务修改（包括新增、修改、删除）<br/>
  * 将这个部分数据的数据放到临时表中
  */
  PROCEDURE pre_process;
  PROCEDURE process_att_add_poi;
  PROCEDURE process_modify_pa_relation;
  PROCEDURE process_att_modify_pa_main;
  PROCEDURE process_att_modify_pa_name;
  PROCEDURE process_geo_add_poi;
  PROCEDURE process_geo_modify_pa_main;
  PROCEDURE delete_isolated_poi_parent;
  PROCEDURE process_Att_geo_add_poi;
END merge_au_ix_pointaddress;
/
CREATE OR REPLACE PACKAGE BODY merge_au_ix_pointaddress IS
  PROCEDURE pre_process IS
  BEGIN
    DELETE FROM temp_au_ix_point_mul_task;
    INSERT INTO temp_au_ix_point_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_ix_pointaddress t
               GROUP BY t.pid
              HAVING COUNT(1) > 1) rs;
  END;
  PROCEDURE do_add_pointaddress(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_oprstatus_clause(v_merge_type,
                                                           'aupa');
    --add ix_pointaddress
    EXECUTE IMMEDIATE 'INSERT INTO ix_pointaddress
      SELECT aupa.PID,
             aupa.GEOMETRY,
             aupa.X_GUIDE,
             aupa.Y_GUIDE,
             aupa.GUIDE_LINK_PID,
             --aupa.GUIDE_NAME_GROUPID,
             aupa.LOCATE_LINK_PID,
             aupa.LOCATE_NAME_GROUPID,
             aupa.GUIDE_LINK_SIDE,
             aupa.LOCATE_LINK_SIDE,
             aupa.SRC_PID
             aupa.REGION_ID,
             aupa.MESH_ID,
             aupa.EDIT_FLAG,
             aupa.IDCODE,
             aupa.DPR_NAME,
             aupa.DP_NAME,
             aupa.OPERATOR,
             aupa.MEMOIRE,
             aupa.DPF_NAME,
             aupa.POSTER_ID,
             aupa.ADDRESS_FLAG,
             aupa.MOVE_FLAG,
             aupa.ADD_FLAG,
             aupa.VERIFED,
             aupa.MODIFY_FLAG,
             aupa.LOG,
             aupa.MEMO,
             aupa.RESERVED,
             0                        AS TASK_ID,
             aupa.DATA_VERSION,
             aupa.FIELD_TASK_ID,
             0                        AS U_RECORD,
             NULL                     AS U_FIELDS
        FROM au_ix_pointaddress aupa
       WHERE aupa.add_flag = ''T''
          AND ' || v_oprstatus_clause || '
         AND NOT EXISTS
       (SELECT 1 FROM ix_pointaddress ip WHERE ip.pid = aupa.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_point_mul_task tmp
               WHERE tmp.pid = aupa.pid)';
    --add ix_pointaddress_name
    EXECUTE IMMEDIATE 'INSERT INTO ix_pointaddress_name
      SELECT aupan.NAME_ID,
             aupan.NAME_GROUPID,
             aupan.PID,
             aupan.LANG_CODE,
             aupan.SUM_CHAR,
             aupan.SPLIT_FLAG,
             aupan.FULLNAME,
             aupan.FULLNAME_PHONETIC,
             aupan.ROADNAME  ,
             aupan.ROADNAME_PHONETIC ,
             aupan.ADDRNAME ,
             aupan.ADDRNAME_PHONETIC  ,
             aupan.PROVINCE,
             aupan.CITY,
             aupan.COUNTY,
             aupan.TOWN,
            
             aupan.PLACE,
             aupan.STREET,
             aupan.LANDMARK,
             aupan.PREFIX,
             aupan.HOUSENUM,
             aupan.TYPE,
             aupan.SUBNUM,
             aupan.SURFIX,
             aupan.ESTAB,
             aupan.BUILDING,
             aupan.UNIT,
             aupan.FLOOR,
             aupan.ROOM,
             aupan.ADDONS,
             aupan.PROV_PHONETIC,
             aupan.CITY_PHONETIC,
             aupan.COUNTY_PHONETIC,
             aupan.TOWN_PHONETIC,
             aupan.STREET_PHONETIC,
             aupan.PLACE_PHONETIC,
             aupan.LANDMARK_PHONETIC,
             aupan.PREFIX_PHONETIC,
             aupan.HOUSENUM_PHONETIC,
             aupan.TYPE_PHONETIC,
             aupan.SUBNUM_PHONETIC,
             aupan.SURFIX_PHONETIC,
             aupan.ESTAB_PHONETIC,
             aupan.BUILDING_PHONETIC,
             aupan.FLOOR_PHONETIC,
             aupan.UNIT_PHONETIC,
             aupan.ROOM_PHONETIC,
             aupan.ADDONS_PHONETIC,
             0                       AS U_RECORD,
             NULL                    AS U_FIELDS
        FROM au_ix_pointaddress_name aupan
       WHERE EXISTS (SELECT 1
                FROM au_ix_pointaddress aupa
                WHERE aupa.add_flag = ''T''
                 AND ' || v_oprstatus_clause || '
                 AND NOT EXISTS (SELECT 1
                        FROM ix_pointaddress ip
                       WHERE ip.pid = aupa.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_point_mul_task tmp
                       WHERE tmp.pid = aupa.pid)
                 AND aupa.pid = aupan.pid)';
    --add ix_pointaddress_parent
    EXECUTE IMMEDIATE 'INSERT INTO ix_pointaddress_parent
      SELECT aupap.GROUP_ID,
             aupap.PARENT_PA_PID,
             0                   AS U_RECORD,
             NULL                AS U_FIELDS
        FROM au_ix_pointaddress_parent aupap
       WHERE EXISTS (SELECT *
                FROM au_ix_pointaddress_children aupc
               WHERE aupc.group_id = aupap.group_id
                 AND EXISTS
               (SELECT 1
                        FROM au_ix_pointaddress aupa
                       WHERE aupc.child_pa_pid = aupa.pid
                         AND aupa.add_flag = ''T''
                         AND ' || v_oprstatus_clause || '
                         AND NOT EXISTS (SELECT 1
                                FROM ix_pointaddress ip
                               WHERE ip.pid = aupa.pid)
                         AND NOT EXISTS
                       (SELECT 1
                                FROM temp_au_ix_point_mul_task tmp
                               WHERE tmp.pid = aupa.pid)))
         AND aupap.group_id NOT IN
             (SELECT group_id FROM ix_pointaddress_parent)';
    --add ix_pointaddress_child
    EXECUTE IMMEDIATE 'INSERT INTO ix_pointaddress_children
      SELECT aupc.GROUP_ID,
             aupc.child_pa_pid,
             0                 AS U_RECORD,
             NULL              AS U_FIELDS
        FROM au_ix_pointaddress_children aupc
       WHERE EXISTS
       (SELECT 1
                FROM au_ix_pointaddress aupa
               WHERE aupc.child_pa_pid = aupa.pid
                 AND aupa.add_flag = ''T''
                 AND ' || v_oprstatus_clause || '
                 AND NOT EXISTS (SELECT 1
                        FROM ix_pointaddress ip
                       WHERE ip.pid = aupa.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_point_mul_task tmp
                       WHERE tmp.pid = aupa.pid))';
  
  END;
  PROCEDURE process_att_add_poi IS
  BEGIN
    do_add_pointaddress(merge_utils.MERGE_TYPE_ATT);
  END;
  PROCEDURE process_modify_pa_relation IS
  BEGIN
    --将子删掉
    DELETE FROM ix_pointaddress_children pac
     WHERE EXISTS
     (SELECT 1
              FROM au_ix_pointaddress aupa
             WHERE aupa.pid = pac.child_pa_pid
               AND aupa.modify_flag = 'T'
               AND Aupa.Att_Oprstatus = 0
               AND EXISTS
             (SELECT 1 FROM ix_pointaddress ip WHERE ip.pid = aupa.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_point_mul_task tmp
                     WHERE tmp.pid = aupa.pid));
    INSERT INTO ix_pointaddress_parent
      SELECT aupap.GROUP_ID,
             aupap.PARENT_PA_PID,
             0                   AS U_RECORD,
             NULL                AS U_FIELDS
        FROM au_ix_pointaddress_parent aupap
       WHERE EXISTS (SELECT *
                FROM au_ix_pointaddress_children aupc
               WHERE aupc.group_id = aupap.group_id
                 AND EXISTS
               (SELECT 1
                        FROM au_ix_pointaddress aupa
                       WHERE aupc.child_pa_pid = aupa.pid
                         AND aupa.modify_flag = 'T'
                         AND Aupa.Att_Oprstatus = 0
                         AND EXISTS (SELECT 1
                                FROM ix_pointaddress ip
                               WHERE ip.pid = aupa.pid)
                         AND NOT EXISTS
                       (SELECT 1
                                FROM temp_au_ix_point_mul_task tmp
                               WHERE tmp.pid = aupa.pid)))
         AND aupap.group_id NOT IN
             (SELECT group_id FROM ix_pointaddress_parent);
    --add ix_pointaddress_child
    INSERT INTO ix_pointaddress_children
      SELECT aupc.GROUP_ID,
             aupc.child_pa_pid,
             0                 AS U_RECORD,
             NULL              AS U_FIELDS
        FROM au_ix_pointaddress_children aupc
       WHERE EXISTS
       (SELECT 1
                FROM au_ix_pointaddress aupa
               WHERE aupc.child_pa_pid = aupa.pid
                 AND aupa.modify_flag = 'T'
                 AND Aupa.Att_Oprstatus = 0
                 AND EXISTS (SELECT 1
                        FROM ix_pointaddress ip
                       WHERE ip.pid = aupa.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_point_mul_task tmp
                       WHERE tmp.pid = aupa.pid));
  END;
  PROCEDURE process_att_modify_pa_main IS
  BEGIN
    --保存历史数据，便于生成修改融合履历
    DELETE FROM TEMP_HIS_IX_POINTADDRESS;
    INSERT INTO TEMP_HIS_IX_POINTADDRESS
      SELECT * FROM ix_pointaddress;
    --融合修改的au_ix_pointaddress
    MERGE INTO ix_pointaddress ixpa
    USING (SELECT aupa.pid,
                  aupa.dpr_name,
                  aupa.DP_NAME,
                  aupa.OPERATOR,
                  aupa.MEMOIRE,
                  aupa.DPF_NAME,
                  aupa.POSTER_ID,
                  aupa.ADDRESS_FLAG
             FROM au_ix_pointaddress aupa
            WHERE aupa.modify_flag = 'T'
              AND Aupa.Att_Oprstatus = 0
              AND EXISTS
            (SELECT 1 FROM ix_pointaddress ip WHERE ip.pid = aupa.pid)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_point_mul_task tmp
                    WHERE tmp.pid = aupa.pid)) rs
    ON (ixpa.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ixpa.DPR_NAME     = rs.DPR_NAME,
             ixpa.DP_NAME      = rs.DP_NAME,
             ixpa.OPERATOR     = rs.operator,
             ixpa.MEMOIRE      = rs.memoire,
             ixpa.DPF_NAME     = rs.dpf_name,
             ixpa.POSTER_ID    = rs.poster_id,
             ixpa.ADDRESS_FLAG = rs.address_flag;
  
  END;
  PROCEDURE process_att_modify_pa_name IS
  BEGIN
    DELETE FROM ix_pointaddress_name ixpan
     WHERE EXISTS
     (SELECT 1
              FROM au_ix_pointaddress aupa
             WHERE ixpan.pid = aupa.pid
               AND aupa.modify_flag = 'T'
               AND Aupa.Att_Oprstatus = 0
               AND EXISTS
             (SELECT 1 FROM ix_pointaddress ip WHERE ip.pid = aupa.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_point_mul_task tmp
                     WHERE tmp.pid = aupa.pid));
    INSERT INTO ix_pointaddress_name ixpan
      SELECT aupan.NAME_ID,
             aupan.NAME_GROUPID,
             aupan.PID,
             aupan.LANG_CODE,
             aupan.SUM_CHAR,
             aupan.SPLIT_FLAG,
             aupan.FULLNAME,
             aupan.FULLNAME_PHONETIC,
             ROADNAME  ,
             ROADNAME_PHONETIC ,
             ADDRNAME ,
             ADDRNAME_PHONETIC  ,
             aupan.PROVINCE,
             aupan.CITY,
             aupan.COUNTY,
             aupan.TOWN,
             aupan.PLACE,
             aupan.STREET,
             
             aupan.LANDMARK,
             aupan.PREFIX,
             aupan.HOUSENUM,
             aupan.TYPE,
             aupan.SUBNUM,
             aupan.SURFIX,
             aupan.ESTAB,
             aupan.BUILDING,
             aupan.UNIT,
             aupan.FLOOR,
             aupan.ROOM,
             aupan.ADDONS,
             aupan.PROV_PHONETIC,
             aupan.CITY_PHONETIC,
             aupan.COUNTY_PHONETIC,
             aupan.TOWN_PHONETIC,
             aupan.STREET_PHONETIC,
             aupan.PLACE_PHONETIC,
             aupan.LANDMARK_PHONETIC,
             aupan.PREFIX_PHONETIC,
             aupan.HOUSENUM_PHONETIC,
             aupan.TYPE_PHONETIC,
             aupan.SUBNUM_PHONETIC,
             aupan.SURFIX_PHONETIC,
             aupan.ESTAB_PHONETIC,
             aupan.BUILDING_PHONETIC,
             aupan.FLOOR_PHONETIC,
             aupan.UNIT_PHONETIC,
             aupan.ROOM_PHONETIC,
             aupan.ADDONS_PHONETIC,
             0                       AS U_RECORD,
             NULL                    AS U_FIELDS
        FROM au_ix_pointaddress_name aupan
       WHERE EXISTS
       (SELECT 1
                FROM au_ix_pointaddress aupa
               WHERE aupan.pid = aupa.pid
                 AND aupa.modify_flag = 'T'
                 AND Aupa.Att_Oprstatus = 0
                 AND EXISTS (SELECT 1
                        FROM ix_pointaddress ip
                       WHERE ip.pid = aupa.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_point_mul_task tmp
                       WHERE tmp.pid = aupa.pid));
  
  END;
  PROCEDURE process_geo_add_poi IS
  BEGIN
    do_add_pointaddress(merge_utils.MERGE_TYPE_GEO);
  END;
  PROCEDURE process_geo_modify_pa_main IS
  BEGIN
    --保存历史数据，便于生成修改融合履历
    DELETE FROM TEMP_HIS_IX_POINTADDRESS;
    INSERT INTO TEMP_HIS_IX_POINTADDRESS
      SELECT * FROM ix_pointaddress;
    --融合修改的au_ix_pointaddress
    MERGE INTO ix_pointaddress ixpa
    USING (SELECT aupa.pid,
                  aupa.geometry,
                  aupa.region_id,
                  aupa.mesh_id,
                  aupa.x_guide,
                  aupa.y_guide,
                  aupa.guide_link_pid,
                  aupa.guide_name_groupid,
                  aupa.locate_link_pid,
                  aupa.locate_name_groupid,
                  aupa.guide_link_side,
                  aupa.locate_link_side
             FROM au_ix_pointaddress aupa
            WHERE aupa.move_flag = 'T'
              AND Aupa.geo_Oprstatus = 0
              AND EXISTS
            (SELECT 1 FROM ix_pointaddress ip WHERE ip.pid = aupa.pid)
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_point_mul_task tmp
                    WHERE tmp.pid = aupa.pid)) rs
    ON (ixpa.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ixpa.geometry            = rs.geometry,
             ixpa.region_id           = rs.region_id,
             ixpa.mesh_id             = rs.mesh_id,
             ixpa.x_guide             = rs.x_guide,
             ixpa.y_guide             = rs.y_guide,
             ixpa.guide_link_pid      = rs.guide_link_pid,
             ixpa.guide_name_groupid  = rs.guide_name_groupid,
             ixpa.locate_link_pid     = rs.locate_link_pid,
             ixpa.locate_name_groupid = rs.locate_name_groupid,
             ixpa.guide_link_side     = rs.guide_link_side,
             ixpa.locate_link_side    = rs.locate_link_side;
  END;
  PROCEDURE delete_isolated_poi_parent IS
  BEGIN
    DELETE FROM ix_pointaddress_parent p
     WHERE NOT EXISTS (SELECT *
              FROM ix_pointaddress_children c
             WHERE c.group_id = p.group_id);
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('删除孤父出错' || SQLERRM);
      --rollback;
      RAISE;
  END;
  PROCEDURE process_Att_geo_add_poi IS
  BEGIN
    do_add_pointaddress(merge_utils.MERGE_TYPE_GEOATT);
  END;
END merge_au_ix_pointaddress;
/
