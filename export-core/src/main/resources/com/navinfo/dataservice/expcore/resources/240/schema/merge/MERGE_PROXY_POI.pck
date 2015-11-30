CREATE OR REPLACE PACKAGE merge_proxy_poi IS
  -- Author  : LIUQING
  -- Created : 2011/5/20 11:29:54
  -- Purpose : 合并外业POI到内业POI表中,外业点门牌合并到内业点门牌中  
  ----测试账户：152 VM_nijGhOFVXZ ibYErADzVl
  ----fixme:需要处理AU_IX_POI_NAME_FLAG 和AU_IX_POI_FLAG的添加\修改

  TYPE type_ix_poi IS TABLE OF ix_poi%ROWTYPE;
 
  PROCEDURE reset_temp_mg_table;
  PROCEDURE process_modify_poi_contact;
 
  PROCEDURE process_modify_poi_name;
  PROCEDURE process_modify_poi_address;
  

  PROCEDURE pre_process_poi(v_merge_type VARCHAR2);
  PROCEDURE process_attgeo_modify_log;

 
  PROCEDURE process_att_geo_delete_poi;
  PROCEDURE process_att_modify_main_poi;
  PROCEDURE process_geo_modify_main_poi;

  
  PROCEDURE process_att_geo_add_poi;
  --内业库中已经存在的数据，外业的add 融合
  PROCEDURE att_add_poi_ext;
  
  PROCEDURE att_add_poi_name_ext;
  PROCEDURE att_add_poi_address_ext;
  PROCEDURE att_add_poi_contact_ext;
 
  PROCEDURE geo_add_poi_ext;
  
  

  PROCEDURE mul_mod_poi_address(v_audata_id NUMBER);
  
  PROCEDURE commit_poi_name_insert;
  
  PROCEDURE mul_mod_poi_name(v_data_id NUMBER);
 
  PROCEDURE mul_mod_poi_contact(v_data_id NUMBER);
  PROCEDURE mul_att_mod_ix_poi(v_data_id    NUMBER,
                               kindflag     NUMBER,
                               labelflag    NUMBER,
                               postcodeflag NUMBER,
                               addresflag   NUMBER,
                               nameflag     NUMBER);
  PROCEDURE mul_geo_mod_ix_poi(v_data_id        NUMBER,
                               displaypointflag NUMBER,
                               guidepointflag   NUMBER,
                               guidexflag       NUMBER,
                               guideyflag       NUMBER);

  PROCEDURE mul_att_add_poi_ext(v_audata_id NUMBER);
  PROCEDURE mul_geo_add_poi_ext(v_audata_id NUMBER);
  
  PROCEDURE mul_att_add_poiname_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_address_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_contact_ext(v_audata_id NUMBER);
  
  
  PROCEDURE mul_add_poi(v_audata_id NUMBER);
 
  PROCEDURE mul_del_poi(v_audata_id NUMBER);

 
 
  PROCEDURE mul_mod_poi_state_ext(v_pid   NUMBER,
                                  v_state NUMBER,
                                  v_log   ix_poi.log%TYPE);
 

  PROCEDURE reset_temp_ixpoi_name(v_audata_id NUMBER);
  PROCEDURE mul_reset_temp_ix_poi(v_pid NUMBER);
 
  
  
 
  PROCEDURE process_att_poi_editon_flag;
  PROCEDURE mul_att_poi_editon_flag(v_audata_id NUMBER);
  
  
  
 
  PROCEDURE mul_att_add_poiname_ext2(v_audata_id NUMBER);
  PROCEDURE process_mod_poi_state(v_merge_type VARCHAR2);
  
  procedure process_mod_poi_name_delclass5;
  procedure process_ext_poi_name_delclass5;
  procedure mul_mod_poi_name_delclass5(v_audata_id number);
  procedure mod_main_poi_state_ext(v_merge_type varchar2);
END merge_proxy_poi;
/
CREATE OR REPLACE PACKAGE BODY merge_proxy_poi IS
 
  PROCEDURE reset_temp_mg_table IS
  BEGIN
    DELETE FROM temp_ix_poi_parent_mg;
    DELETE FROM temp_ix_poi_name_mg;
  END;
  PROCEDURE reset_tmp_ix_poi_ext(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    --为了记录变化前履历，临时保存POI
    DELETE FROM temp_his_ix_poi_ext;
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           't');
    --备份ix_poi中和au_ix_poi 新增状态pid相同的数据 :没有被多个外业任务操作    
    EXECUTE IMMEDIATE 'INSERT INTO TEMP_HIS_IX_POI_EXT
      SELECT ip.*
        FROM ix_poi ip
       WHERE EXISTS
       (SELECT 1
                FROM au_ix_poi t
               WHERE ip.pid = t.pid
                 AND t.state = 3
                 AND ' || v_oprstatus_clause || ' 
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_poi_mul_task tmp
                       WHERE t.pid = tmp.pid))';
  
  END;
 
  PROCEDURE pre_process_poi(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           't');
    --融合时，如果一个POI被多个外业任务修改（包括新增、修改或修改、删除），那么将多个Mark都导出由用户通过NAVIMAP处理，不进行融合
    --temp_au_ix_poi_mul_task 用于存放被多个外业任务修改的POI的PID   
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_ix_poi_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_ix_poi t
               WHERE ' || v_oprstatus_clause ||
                      '               
               GROUP BY t.pid
               HAVING COUNT(1) > 1) rs';
    reset_tmp_ix_poi_ext(v_merge_type);
    --如果一个poi被多个外业任务操作
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'au');
    EXECUTE IMMEDIATE '
    INSERT INTO temp_au_ix_poi_grp
      SELECT audata_id, pid, state
        FROM (
             --分组后，第一条不是a并且包含delete操作，将在最后一个delete及其之后的数据(也即包含最后一条delete)加入到grp中
             WITH st AS (SELECT au.audata_id,
                                au.pid,
                                au.imp_date,
                                au.state,
                                row_number() over(PARTITION BY pid ORDER BY imp_date ASC, audata_id ASC) rn
                           FROM au_ix_poi au
                          WHERE '||v_oprstatus_clause||' and au.pid IN
                                (SELECT pid FROM temp_au_ix_poi_mul_task))
               SELECT st.audata_id, st.pid, st.state
                 FROM st,
                      (SELECT pid, MAX(rn) maxrn
                         FROM (SELECT * FROM st WHERE st.state = 1)
                        GROUP BY pid) rs
                WHERE st.rn >= rs.maxrn
                  AND st.pid = rs.pid
               UNION ALL
               --不包含delete的分组数据
               SELECT audata_id, pid, state
                 FROM au_ix_poi au
                WHERE '||v_oprstatus_clause||' and au.pid IN (SELECT pid FROM temp_au_ix_poi_mul_task)
                  AND NOT EXISTS (SELECT 1
                         FROM au_ix_poi tmp
                        WHERE tmp.pid = au.pid
                          AND tmp.state = 1))
  ';
   
  END;
  /*新增融合：父子关系
  v_not: NOT不在母库中*/
  PROCEDURE do_add_relation(v_param VARCHAR2) IS
    v_pid_count NUMBER;
    v_not       VARCHAR2(3);
  BEGIN
    DELETE FROM temp_ix_poi_parent_mg;
    --几何不容父子关系                  
    --1.新增的是子，将不存在的父和子都copy到内业中      
    --仅属性操作时，才融合父子关系，
    --需要申请pid
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not 要不为NOT ，要不为''
    EXECUTE IMMEDIATE 'SELECT COUNT(1)
                        FROM au_ix_poi_parent p
                       WHERE EXISTS (SELECT 1
                                FROM (SELECT c.group_id,c.FIELD_TASK_ID
                                        FROM au_ix_poi L, au_ix_poi_children c
                                       WHERE L.state = 3
                                         AND L.ATT_OPRSTATUS in( 0,1)
                                         AND c.audata_id = L.audata_id
                                         AND ' ||
                      v_not || '  EXISTS (SELECT 1
                                                FROM temp_his_ix_poi_ext ext
                                               WHERE ext.pid = L.pid)
                                         AND NOT EXISTS (SELECT 1
                                                FROM temp_au_ix_poi_mul_task tmp
                                               WHERE tmp.pid = L.pid)) v
                               WHERE p.group_id = v.group_id and p.FIELD_TASK_ID=v.FIELD_TASK_ID)
                               AND NOT EXISTS (SELECT 1
                                FROM temp_his_ix_poi_parent ip
                               WHERE ip.parent_poi_pid = p.parent_poi_pid)       
                               '
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_PARENT', v_pid_count);
      --将要增加的ix_poi_parent放到临时表，以便生成履历
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_parent_mg
      SELECT PID_MAN.PID_NEXTVAL(''IX_POI_PARENT'') as GROUP_ID,
             PARENT_POI_PID,

             0 as TENANT_FLAG,
             null as MEMO,

             0 AS U_RECORD,
             NULL AS U_FIELDS
        FROM au_ix_poi_parent p
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT c.group_id,c.FIELD_TASK_ID
                        FROM au_ix_poi L, au_ix_poi_children c
                       WHERE L.state = 3
                         AND L.ATT_OPRSTATUS in( 0,1)
                         AND c.audata_id = L.audata_id
                         AND  ' || v_not ||
                        '  EXISTS (SELECT 1
                            FROM temp_his_ix_poi_ext ext
                           WHERE ext.pid = L.pid)
                         AND NOT EXISTS (SELECT 1
                                FROM temp_au_ix_poi_mul_task tmp
                               WHERE tmp.pid = L.pid) --有多个外业任务的不融合
                      ) v
               WHERE p.group_id = v.group_id  and p.FIELD_TASK_ID=v.FIELD_TASK_ID) AND NOT EXISTS (SELECT 1
                                FROM temp_his_ix_poi_parent ip
                               WHERE ip.parent_poi_pid = p.parent_poi_pid) ';
      INSERT INTO ix_poi_parent
        SELECT * FROM temp_ix_poi_parent_mg;
    END IF;
  
    --添加子表
    EXECUTE IMMEDIATE 'INSERT INTO ix_poi_children
      SELECT group_id,child_poi_pid,relation_type, u_record,U_FIELDS
        FROM (SELECT IPP.GROUP_ID,
                     C.CHILD_POI_PID,
                     C.RELATION_TYPE,
                     0 as U_RECORD,
                     NULL as U_FIELDS,
                     ROW_NUMBER() OVER(PARTITION BY IPP.GROUP_ID, C.CHILD_POI_PID, C.RELATION_TYPE ORDER BY 1) AS RN
                FROM AU_IX_POI          L,
                     AU_IX_POI_CHILDREN C,
                     ix_poi_parent      IPP,
                     AU_IX_POI_PARENT   AUIPP
               WHERE L.STATE = 3
                 AND L.ATT_OPRSTATUS in( 0,1)
                 AND C.audata_id = L.audata_id
                 AND AUIPP.GROUP_ID = C.GROUP_ID
                 AND AUIPP.PARENT_POI_PID = IPP.PARENT_POI_PID
                 AND  ' || v_not ||
                      '  EXISTS
               (SELECT 1 FROM TEMP_HIS_IX_POI_EXT EXT WHERE EXT.PID = L.PID)
                 AND NOT EXISTS (SELECT 1
                        FROM TEMP_AU_IX_POI_MUL_TASK TMP
                       WHERE TMP.PID = L.PID)) RS
       WHERE RS.RN = 1 ' --有多个外业任务的不融合
    ;
  
  END;
  PROCEDURE exe_insert_address(v_pid_count        IN OUT NUMBER,
                               v_oprstatus_clause VARCHAR2,
                               v_not              VARCHAR2) IS
  BEGIN
    DELETE FROM temp_ix_poi_address_mg;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_ADDRESS', v_pid_count);
      --添加IX_POI_ADDRESS;
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_address_mg
      SELECT PID_MAN.PID_NEXTVAL(''IX_POI_ADDRESS'') as NAME_ID,
         c.NAME_GROUPID,
         c.POI_PID,
         c.LANG_CODE,
         c.SRC_FLAG,
         c.FULLNAME,
         c.FULLNAME_PHONETIC,
         c.ROADNAME,
         c.ROADNAME_PHONETIC,
         c.ADDRNAME,
         c.ADDRNAME_PHONETIC,
         c.PROVINCE,
         c.CITY,
         c.COUNTY,
         c.TOWN,
         c.PLACE,
         c.STREET,
        
         c.LANDMARK,
         c.PREFIX,
         c.HOUSENUM,
         c.TYPE,
         c.SUBNUM,
         c.SURFIX,
         c.ESTAB,
         c.BUILDING,
         c.FLOOR,
         c.UNIT,
         c.ROOM,
         c.ADDONS,
         c.PROV_PHONETIC,
         c.CITY_PHONETIC,
         c.COUNTY_PHONETIC,
         c.TOWN_PHONETIC,
         c.STREET_PHONETIC,
         c.PLACE_PHONETIC,
         c.LANDMARK_PHONETIC,
         c.PREFIX_PHONETIC,
         c.HOUSENUM_PHONETIC,
         c.TYPE_PHONETIC,
         c.SUBNUM_PHONETIC,
         c.SURFIX_PHONETIC,
         c.ESTAB_PHONETIC,
         c.BUILDING_PHONETIC,
         c.FLOOR_PHONETIC,
         c.UNIT_PHONETIC,
         c.ROOM_PHONETIC,
         c.ADDONS_PHONETIC,
         0, --c.U_RECORD,
         NULL --c.U_FIELDS
        FROM au_ix_poi p, au_IX_POI_ADDRESS c
       WHERE p.state = 3
         AND ' || v_oprstatus_clause || '
         AND p.pid = c.poi_pid         
         AND ' || v_not || ' EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid) --有多个外业任务的不融合
      ';
      INSERT INTO ix_poi_address
        SELECT * FROM temp_ix_poi_address_mg;
    END IF;
  END;
  PROCEDURE do_add_address(v_merge_type VARCHAR2, v_param VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
    v_not              VARCHAR2(3);
  BEGIN
    --需要申请pid
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not 要不为NOT ，要不为''
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'p');
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
         FROM au_ix_poi p, au_IX_POI_ADDRESS c
       WHERE p.state = 3
         AND ' || v_oprstatus_clause || '
         AND p.pid = c.poi_pid         
         AND ' || v_not ||
                      ' EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid)'
      INTO v_pid_count;
    exe_insert_address(v_pid_count, v_oprstatus_clause, v_not);
  EXCEPTION
    WHEN OTHERS THEN
      IF (SQLCODE = -20999) THEN
        exe_insert_address(v_pid_count, v_oprstatus_clause, v_not);
      ELSE
        RAISE;
      END IF;
    
  END;
   PROCEDURE do_add_name1(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
    v_not              VARCHAR2(3):='NOT';
  BEGIN
    DELETE FROM temp_ix_poi_name_mg; --为生成履历，采用零食表保存新增的数据
     v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'p');
     EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi p, au_ix_poi_name c
         WHERE p.state = 3
           AND p.audata_id = c.audata_id
           AND p.pid = c.poi_pid        
           AND ' || v_oprstatus_clause ||
                          '        
           AND ' || v_not ||
                          ' EXISTS (SELECT 1
                    FROM temp_his_ix_poi_ext ext
                   WHERE ext.pid = p.pid)       
           AND NOT EXISTS (SELECT 1
                  FROM temp_au_ix_poi_mul_task tmp
                 WHERE tmp.pid = p.pid)
           '
      INTO v_pid_count;
      IF (v_pid_count > 0) THEN
          pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
          EXECUTE IMMEDIATE '
         INSERT INTO temp_ix_poi_name_mg 
          (name_id,
               poi_pid,
               name_groupid,
               name_class,
               name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               u_record,
               u_fields)
         WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name
              GROUP BY poi_pid)
            SELECT pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
                   c.poi_pid,
                   nvl(rs.name_groupid, 1) AS name_groupid, 
                   1 AS name_class,
                   1 AS NAME_TYPE ,
                   c.lang_code,
                   c.name,
                   c.name_phonetic,
                   c.keywords,
                   c.nidb_pid,
                   0, --c.U_RECORD,s
                   NULL --c.U_FIELDS
              FROM au_ix_poi p, au_ix_poi_name c, rs
             WHERE p.state = 3
               AND p.audata_id = c.audata_id
               AND p.pid = c.poi_pid
               AND p.pid = rs.poi_pid(+)    
               AND ' || v_oprstatus_clause ||
                              '        
               AND ' || v_not ||
                              ' EXISTS (SELECT 1
                        FROM temp_his_ix_poi_ext ext
                       WHERE ext.pid = p.pid)       
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = p.pid)';
     end if; 
   INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
    end ;
  PROCEDURE do_add_name2(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
    v_not              VARCHAR2(3):='NOT';
  BEGIN
    DELETE FROM temp_ix_poi_name_mg; --为生成履历，采用零食表保存新增的数据
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'p');
   --增加一条name_type=1的
  EXECUTE IMMEDIATE '
  INSERT INTO temp_ix_poi_name_mg 
         (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields)
  WITH rs AS
      (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_poi_name
        GROUP BY poi_pid)
      SELECT c.name_id,
             c.poi_pid,
             nvl(rs.name_groupid, 1) AS name_groupid, 
             1 AS name_class,
             2 AS NAME_TYPE ,
             c.lang_code,
             c.name,
             c.name_phonetic,
             c.keywords,
             c.nidb_pid,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
        FROM au_ix_poi p, au_ix_poi_name c, rs
       WHERE p.state = 3
         AND p.audata_id = c.audata_id
         AND p.pid = c.poi_pid
         AND p.pid = rs.poi_pid(+)    
         AND ' || v_oprstatus_clause ||
                        '        
         AND ' || v_not ||
                        ' EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)       
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid)';
   INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
  END;
  PROCEDURE do_add_name(v_merge_type VARCHAR2, v_param VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
    v_not              VARCHAR2(3);
  BEGIN
   do_add_name1(v_merge_type);
   do_add_name2(v_merge_type);
  END;
  
  
   

  
  PROCEDURE do_add(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'p');
    --添加主表;    
    EXECUTE IMMEDIATE 'INSERT INTO ix_poi
      SELECT /*+index(p,IX_AIP_PS)*/

           PID          ,
          KIND_CODE    ,
          GEOMETRY     ,
          X_GUIDE      ,
          Y_GUIDE      ,
          LINK_PID     ,
          SIDE         ,
          NAME_GROUPID ,
          ROAD_FLAG    ,
          PMESH_ID     ,
          ADMIN_REAL   ,
          IMPORTANCE   ,
          CHAIN        ,
          AIRPORT_CODE ,
          ACCESS_FLAG  ,
          OPEN_24H     ,
          MESH_ID_5K   ,
          MESH_ID      ,
          REGION_ID    ,
          POST_CODE    ,
          EDIT_FLAG    ,
          DIF_GROUPID  ,
          RESERVED     ,
          STATE        ,
          FIELD_STATE  ,
          LABEL        ,
          TYPE         ,
          ADDRESS_FLAG ,
          EX_PRIORITY  ,
          EDITION_FLAG ,
          POI_MEMO     ,
          OLD_BLOCKCODE,
          OLD_NAME     ,
          OLD_ADDRESS  ,
          OLD_KIND     ,
          POI_NUM      ,
          LOG          ,
          0 as TASK_ID      ,
          DATA_VERSION ,
          FIELD_TASK_ID,
          0 as U_RECORD     ,
          null as U_FIELDS

        FROM au_ix_poi p
       WHERE state = 3
         AND ' || v_oprstatus_clause || '
         AND NOT EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid) --有多个外业任务的不融合 
      ';
    do_add_address(v_merge_type, 'NOT');
  
    --dbms_output.put_line('BEGIN IX_POI_CONTACT ');
    --添加IX_POI_CONTACT;
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CONTACT
      SELECT c.POI_PID,
             c.CONTACT_TYPE,
             c.CONTACT,
             c.CONTACT_DEPART,
             c.PRIORITY,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
        FROM au_ix_poi p, au_IX_POI_CONTACT c
       WHERE p.state = 3
         AND ' || v_oprstatus_clause || '
         AND p.pid = c.poi_pid
         AND NOT EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid) --有多个外业任务的不融合
      ';
    do_add_name(v_merge_type, 'NOT');
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('添加POI时出错' || SQLERRM);
      --rollback;
      RAISE;
  END;

  --父子关系TAB导入原则
  --新增：1.添加的是子
  --如果parent在ix_poi_parent 存在，则在au_ix_poi_parent不添加，否则添加到au_ix_poi_parent。然后再添加au_ix_poi_children
  --
  --修改：1.修改的是子： 如果parent在ix_poi_parent 存在  ，则在au_ix_poi_parent不添加 ，否则添加一条，然后删除原先的children，新增新的children

  -------------------------------------------------------------------------------------------------------------------
  --POI融合由于父子关系的缘故，必须先新增，再修改，后删除的顺序进行融合
  --新增POI时父子关系的处理如下：1.新增的是子，将父和子都copy到内业中
  --修改POI父子关系时，1.修改的是子，将父和子都copy到内业中，删除原来的子
  --删除父子关系的处理如下：如果删除的是父亲，则需要把parent和children都删除，否则仅仅删除children

  --state:0 无 1 删除 2 修改 3 新增
  PROCEDURE save_poi_modify_log(p_rec temp_au_poi_modify_log%ROWTYPE) IS
  BEGIN
    EXECUTE IMMEDIATE 'insert into temp_au_poi_modify_log(
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
      ,GUIDE_X_FLAG
      ,GUIDE_Y_FLAG
      ,CHAIN_FLAG
      ,tenant_flag
      ,FLOOR_USED
      ,FLOOR_EMPTY
       ) values (:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12,:13,:14,:15,:16,:17,:18)'
      USING p_rec.audata_id, p_rec.pid, p_rec.name_flag, p_rec.address_flag, p_rec.tel_flag, p_rec.kind_flag, p_rec.post_code_flag, p_rec.food_type_flag, p_rec.parent_flag, p_rec.lable_flag, p_rec.display_point_flag, p_rec.guide_point_flag, p_rec.guide_x_flag, p_rec.guide_y_flag, p_rec.chain_flag,p_rec.tenant_flag,p_rec.FLOOR_USED,p_rec.FLOOR_EMPTY;
  END;

  --改电话：电话(TELE_NUMBER、SAITEM第二位更新为“F”)
  --处理联系方式
  --先删除联系方式，再重新添加联系方式
  PROCEDURE process_modify_poi_contact IS
  BEGIN
  
    --将只在外业成果库的插入作业库（CONTACT），CONTACT_TYPE赋值为1
    INSERT INTO ix_poi_contact
      SELECT poi_pid,
             contact_type,
             contact,
             contact_depart,
             priority,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_contact c1
       WHERE EXISTS (SELECT 1
                FROM temp_au_poi_modify_log l
               WHERE l.tel_flag = 1
                 AND c1.poi_pid = l.pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact);
    --将只在作业库中有的删除
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.tel_flag = 1
               AND c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.poi_pid = c1.poi_pid
               AND auipc.contact = c1.contact);
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('修改联系方式时出错' || SQLERRM);
      --rollback;
      RAISE;
    
  END;
 
  
  PROCEDURE commit_poi_name_insert IS
  BEGIN
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
  END;
  procedure process_modify_poi_name2 is
     v_pid_count NUMBER;
    begin
       delete from temp_ix_poi_name_mg;
       --通过POI_PID判断IX_POI_NAME表中Lang_Code为”CHI”、 NAME_CLASS为官方名称（1），名称类型（NAME_TYPE）为标准（1）的记录是否存在，如果不存在，则增加一条记录；否则修改该记录的NAME和NAME_PHONETIC的值。同时更新IX_POI的old_name字段
    MERGE INTO ix_poi_name t
    USING (SELECT *
             FROM au_ix_poi_name au
            WHERE au.audata_id IN
                  (SELECT audata_id
                     FROM temp_au_poi_modify_log aulog
                    WHERE aulog.name_flag = 1)
                    and au.lang_code in('CHI', 'CHT') and  au.name_type=2
           
           ) aurs
    ON (t.lang_code IN('CHI', 'CHT') AND t.name_class = 1 AND t.name_type = 2 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic;
  
    
      
          INSERT INTO temp_ix_poi_name_mg
            (name_id,
             poi_pid,
             name_groupid,
             name_class,
             name_type,
             lang_code,
             NAME,
             name_phonetic,
             keywords,
             nidb_pid,
             u_record,
             u_fields) WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name
              GROUP BY poi_pid)
            SELECT au.name_id,
                   au.poi_pid,
                   nvl(rs.name_groupid, 1) AS name_groupid,
                   1 AS name_class,
                   2 AS name_type,
                   lang_code,
                   NAME,
                   name_phonetic,
                   keywords,
                   nidb_pid,
                   0 AS u_record,
                   NULL AS u_fields
              FROM au_ix_poi_name au, rs
             WHERE au.poi_pid = rs.poi_pid(+)
               AND au.audata_id IN (SELECT audata_id
                                      FROM temp_au_poi_modify_log aulog
                                     WHERE aulog.name_flag = 1)
               and not exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)                      
               AND NOT EXISTS (SELECT 1
                      FROM ix_poi_name ipn
                     WHERE ipn.lang_code IN ('CHI', 'CHT')
                       AND name_class = 1
                       AND name_type = 2
                       AND ipn.poi_pid = au.poi_pid);
    execute immediate '                   
    select count(1) from   au_ix_poi_name au
    where au.audata_id  IN (SELECT audata_id
                                      FROM temp_au_poi_modify_log aulog
                                     WHERE aulog.name_flag = 1)
               and  exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)                      
               AND NOT EXISTS (SELECT 1
                      FROM ix_poi_name ipn
                     WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                       AND name_class = 1
                       AND name_type = 2
                       AND ipn.poi_pid = au.poi_pid)' into v_pid_count; 
     if(v_pid_count>0) then
      pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
          INSERT INTO temp_ix_poi_name_mg
            (name_id,
             poi_pid,
             name_groupid,
             name_class,
             name_type,
             lang_code,
             NAME,
             name_phonetic,
             keywords,
             nidb_pid,
             u_record,
             u_fields) WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name
              GROUP BY poi_pid)
            SELECT pid_man.pid_nextval('IX_POI_NAME') AS name_id,
                   au.poi_pid,
                   nvl(rs.name_groupid, 1) AS name_groupid,
                   1 AS name_class,
                   2 AS name_type,
                   lang_code,
                   NAME,
                   name_phonetic,
                   keywords,
                   nidb_pid,
                   0 AS u_record,
                   NULL AS u_fields
              FROM au_ix_poi_name au, rs
             WHERE au.poi_pid = rs.poi_pid(+)
               AND au.audata_id IN (SELECT audata_id
                                      FROM temp_au_poi_modify_log aulog
                                     WHERE aulog.name_flag = 1)
               and  exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)                         
               AND NOT EXISTS (SELECT 1
                      FROM ix_poi_name ipn
                     WHERE ipn.lang_code IN ('CHI', 'CHT')
                       AND name_class = 1
                       AND name_type = 2
                       AND ipn.poi_pid = au.poi_pid);
     end if;                                
    
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
      end;
  PROCEDURE process_modify_poi_name1 IS
    v_pid_count NUMBER;
  BEGIN    
    delete from temp_ix_poi_name_mg;
    --通过POI_PID判断IX_POI_NAME表中Lang_Code为”CHI”、NAME_CLASS为官方名称（1），名称类型（NAME_TYPE）为原始（2）的记录是否存在，如果不存在，则增加一条记录；否则修改该记录的NAME和NAME_PHONETIC的值。同时更新IX_POI的old_name字段
    MERGE INTO ix_poi_name t
    USING (SELECT *
             FROM au_ix_poi_name au
            WHERE au.audata_id IN
                  (SELECT audata_id
                     FROM temp_au_poi_modify_log aulog
                    WHERE aulog.name_flag = 1)
                  and au.lang_code in('CHI', 'CHT') and  au.name_type=2  
           
           ) aurs
    ON (t.lang_code IN('CHI', 'CHT') AND t.name_class = 1 AND t.name_type = 1 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic;
       EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi_name au
         WHERE  au.audata_id IN (SELECT audata_id
                                  FROM temp_au_poi_modify_log aulog
                                 WHERE aulog.name_flag = 1)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid)
           '
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
          pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
          INSERT INTO temp_ix_poi_name_mg
            (name_id,
             poi_pid,
             name_groupid,
             name_class,
             name_type,
             lang_code,
             NAME,
             name_phonetic,
             keywords,
             nidb_pid,
             u_record,
             u_fields) WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name
              GROUP BY poi_pid)
            SELECT pid_man.pid_nextval('IX_POI_NAME') AS name_id,
                   au.poi_pid,
                   nvl(rs.name_groupid, 1) AS name_groupid,
                   1 AS name_class,
                   1 AS name_type,
                   lang_code,
                   NAME,
                   name_phonetic,
                   keywords,
                   nidb_pid,
                   0 AS u_record,
                   NULL AS u_fields
              FROM au_ix_poi_name au, rs
             WHERE au.poi_pid = rs.poi_pid(+)
               AND au.audata_id IN (SELECT audata_id
                                      FROM temp_au_poi_modify_log aulog
                                     WHERE aulog.name_flag = 1)
               AND NOT EXISTS (SELECT 1
                      FROM ix_poi_name ipn
                     WHERE ipn.lang_code IN ('CHI', 'CHT')
                       AND name_class = 1
                       AND name_type = 1
                       AND ipn.poi_pid = au.poi_pid);
    
    end if;
   
   
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理POI名称时出错' || SQLERRM);
      --rollback;
      RAISE;
  END;
  --修改名称表
  --先删除，再新增
  PROCEDURE process_modify_poi_name IS
    v_pid_count NUMBER;
  BEGIN    
    process_modify_poi_name1;
    process_modify_poi_name2;
  END;

  --修改地址表
  --先删除，再新增
  --FIXME:处理name_groupid
  PROCEDURE process_modify_poi_address IS
    v_pid_count NUMBER;
  BEGIN
    --改名称 POI名称、POI拼音、OLD名称 (POINAME、POIPY、 OLD_NAME、 SA_ITEM第一位更新为“F”)
    --改地址：ADDRESS_NAME、 POINT_ADDRESS
    --删除;
    DELETE FROM ix_poi_address c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.address_flag = 1
               AND c1.poi_pid = l.pid)
       AND c1.lang_code IN ('CHI', 'CHT');
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
       from  au_ix_poi_address c1
       WHERE EXISTS (SELECT 1
                FROM temp_au_poi_modify_log l
               WHERE l.address_flag = 1
                 AND c1.poi_pid = l.pid)
         AND c1.lang_code IN (''CHI'', ''CHT'')'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_ADDRESS', v_pid_count);
      --改名称 POI名称、POI拼音、OLD名称 (POINAME、POIPY)
      INSERT INTO ix_poi_address
        SELECT pid_man.pid_nextval('IX_POI_ADDRESS') AS name_id,
               name_groupid,
               poi_pid,
               lang_code,
               src_flag,
               fullname,
               fullname_phonetic,
               roadname,
               roadname_phonetic,
               addrname,
               addrname_phonetic,
               province,
               city,
               county,
               town,
               place,
               street,
              
               landmark,
               prefix,
               housenum,
               TYPE,
               subnum,
               surfix,
               estab,
               building,
               floor,
               unit,
               room,
               addons,
               prov_phonetic,
               city_phonetic,
               county_phonetic,
               town_phonetic,
               street_phonetic,
               place_phonetic,
               landmark_phonetic,
               prefix_phonetic,
               housenum_phonetic,
               type_phonetic,
               subnum_phonetic,
               surfix_phonetic,
               estab_phonetic,
               building_phonetic,
               floor_phonetic,
               unit_phonetic,
               room_phonetic,
               addons_phonetic,
               0, --U_RECORD,
               NULL --U_FIELDS      
          FROM au_ix_poi_address c1
         WHERE EXISTS (SELECT 1
                  FROM temp_au_poi_modify_log l
                 WHERE l.address_flag = 1
                   AND c1.poi_pid = l.pid)
           AND c1.lang_code IN ('CHI', 'CHT');
    END IF;
  
  
   -- 删除对应英文和葡文地址记录   
       delete from ix_poi_address
        where poi_pid in (select distinct poi_pid
                      FROM au_ix_poi_address c1
                     WHERE EXISTS (SELECT 1
                              FROM temp_au_poi_modify_log l
                             WHERE l.address_flag = 1
                               AND c1.poi_pid = l.pid)
                       AND c1.lang_code IN ('CHI', 'CHT'))
        and lang_code IN ('ENG', 'POR'); 
    --commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理POI名称时出错' || SQLERRM);
      --rollback;
      RAISE;
  END;

 


  PROCEDURE process_att_modify_main_poi IS
  BEGIN
  
    --变更
    --改分类:POI种别(king_code)
    --改邮编:邮编(POST_CODE)
    --改多义性:风味类型(FOOD_TYPE)
    --改标注:标注(LABEL)
    --改标注:标注字段中包含“24小时”（不区分大小写、全半角OPEN_24H赋值”Y"
    --改RELATION: GEOMETRY
    --改位移:X_GUIDE、Y_GUIDE
    --改地址:OLD_ADDRESS
    --改电话：电话(SAITEM第二位更新为“F”)
    --改名称 POI名称、POI拼音、OLD名称 (OLD_NAME、 SA_ITEM第一位更新为“F”)
    MERGE INTO ix_poi p1
    USING (SELECT p2.pid,
                  p2.log,
                  p2.state,
                  p2.old_name,
                  p2.y_guide,
                  p2.x_guide,
                  p2.geometry,
                  p2.label,
                  p2.post_code,
                  p2.kind_code,
                  p2.old_address,
                  p2.old_kind,
                  p2.chain,
                  p2.open_24h,
                  l.chain_flag,
                  l.name_flag,
                  l.address_flag,
                  l.tel_flag,
                  l.kind_flag,
                  l.post_code_flag,
                  l.food_type_flag,
                  l.parent_flag,
                  l.lable_flag,
                  l.display_point_flag,
                  l.guide_point_flag,
                  auipn.name           AS auoldname,
                  auipa.fullname       AS auoldaddress
             FROM au_ix_poi p2,
                  temp_au_poi_modify_log l,
                  (SELECT *
                     FROM au_ix_poi_name
                    WHERE lang_code IN ('CHI', 'CHT')
                      AND name_type = 2) auipn,
                  (SELECT *
                     FROM au_ix_poi_address
                    WHERE lang_code IN ('CHI', 'CHT')) auipa
            WHERE p2.audata_id = l.audata_id
              AND (l.address_flag = 1 OR l.kind_flag = 1 OR
                  l.post_code_flag = 1 OR l.lable_flag = 1 OR
                  l.name_flag = 1 OR l.chain_flag = 1)
              AND p2.audata_id = auipn.audata_id(+)
              AND p2.audata_id = auipa.audata_id(+)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.chain       = decode(v.chain_flag,
                                     0,
                                     p1.chain,
                                     nvl(v.chain, p1.chain)),
             p1.old_address = decode(v.address_flag,
                                     0,
                                     p1.old_address,
                                     v.auoldaddress),
             p1.kind_code   = decode(v.kind_flag,
                                     0,
                                     p1.kind_code,
                                     v.kind_code),
             p1.old_kind    = decode(v.kind_flag,
                                     0,
                                     p1.old_kind,
                                     v.kind_code),
             p1.post_code   = decode(v.post_code_flag,
                                     0,
                                     p1.post_code,
                                     v.post_code),
             p1.label       = decode(v.lable_flag, 0, p1.label, v.label),
             p1.open_24h    = decode(v.lable_flag, 1,(CASE
    WHEN instr(to_single_byte(v.label), '24小时') > 0 THEN 1 ELSE p1.open_24h END), p1.open_24h), p1.old_name = decode(v.name_flag, 0, p1.old_name, v.auoldname);
  
    
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('修改POI主表字段时出错' || SQLERRM);
      RAISE;
  END;
  PROCEDURE parse_attgeo_modify_log(rec           au_ix_poi%ROWTYPE,
                                    v_rec         IN OUT temp_au_poi_modify_log%ROWTYPE,
                                    v_change_flag IN OUT BOOLEAN) IS
    v_log VARCHAR2(4000);
  BEGIN
    v_change_flag   := FALSE;
    v_rec.audata_id := rec.audata_id;
    v_rec.pid       := rec.pid;
  
    IF (rec.log IS NULL) THEN
      RETURN;
    END IF;
    v_log := upper(rec.log);
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改名称') > 0 THEN
      v_rec.name_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.name_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改地址') > 0 THEN
      v_rec.address_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.address_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改电话') > 0 THEN
      v_rec.tel_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.tel_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改分类') > 0 THEN
      v_rec.kind_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.kind_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改邮编') > 0 THEN
      v_rec.post_code_flag := 1;
      v_change_flag        := TRUE;
    ELSE
      v_rec.post_code_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改多义性') > 0 THEN
      v_rec.food_type_flag := 1;
      v_change_flag        := TRUE;
    ELSE
      v_rec.food_type_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改FATHERSON') > 0 THEN
      v_rec.parent_flag := 1;
      v_change_flag     := TRUE;
    ELSE
      v_rec.parent_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) AND instr(v_log, '改标注') > 0 THEN
      v_rec.lable_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.lable_flag := 0;
    END IF;
    IF rec.geo_oprstatus in(0,1) AND instr(v_log, '改RELATION') > 0 THEN
      v_rec.display_point_flag := 1;
      v_change_flag            := TRUE;
    ELSE
      v_rec.display_point_flag := 0;
    END IF;
    IF rec.geo_oprstatus in(0,1) AND instr(v_log, '改位移') > 0 THEN
      v_rec.guide_point_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.guide_point_flag := 0;
    END IF;
    IF rec.geo_oprstatus in(0,1) AND instr(v_log, '改GUIDEX') > 0 THEN
      v_rec.guide_x_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.guide_x_flag := 0;
    END IF;
    IF rec.geo_oprstatus in(0,1) AND instr(v_log, '改GUIDEY') > 0 THEN
      v_rec.guide_y_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.guide_y_flag := 0;
    END IF;
    IF (rec.att_oprstatus in(0,1) AND v_rec.kind_flag = 1) THEN
      v_rec.chain_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.chain_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) and instr(v_log, '改TENANT_FLAG') > 0 THEN
      v_rec.tenant_flag := 1;

      v_change_flag    := TRUE;
    ELSE
      v_rec.tenant_flag := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) and instr(v_log, '改FLOOR_USED') > 0 THEN
      v_rec.FLOOR_USED := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.FLOOR_USED := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) and instr(v_log, '改FLOOR_EMPTY') > 0 THEN
      v_rec.FLOOR_EMPTY := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.FLOOR_EMPTY := 0;
    END IF;
  
  END;
  
  PROCEDURE process_attgeo_modify_log IS
    v_rec         temp_au_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
  BEGIN
    
    DELETE FROM temp_au_poi_modify_log;
    FOR rec IN (SELECT a.*
                  FROM au_ix_poi a
                 WHERE a.state = 2
                   AND (a.att_oprstatus in(0,1) OR a.geo_oprstatus in(0,1))
                   AND EXISTS
                 (SELECT 1 FROM ix_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ix_poi_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag := FALSE;
      parse_attgeo_modify_log(rec, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_poi_modify_log(v_rec);
      END IF;
    END LOOP;
  END;
  
  

  
  PROCEDURE do_del(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'poi2');
    --删除主表;
    EXECUTE IMMEDIATE 'DELETE ix_poi poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.pid
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --有多个外业任务的不融合 
            )';
    --删除IX_POI_ADDRESS;
    EXECUTE IMMEDIATE 'DELETE IX_POI_ADDRESS poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.POI_PID
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --有多个外业任务的不融合 
            )';
    --删除IX_POI_CONTACT;
    EXECUTE IMMEDIATE 'DELETE IX_POI_CONTACT poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.POI_PID
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --有多个外业任务的不融合 
            )';   
    --删除IX_POI_NAME;
    EXECUTE IMMEDIATE 'DELETE IX_POI_NAME poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.POI_PID
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --有多个外业任务的不融合 
            )';
               
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('删除POI时出错' || SQLERRM);
      --rollback;
      RAISE;
    
  END;

  PROCEDURE process_geo_modify_main_poi IS
  BEGIN
    --改RELATION: GEOMETRY
    --改位移:X_GUIDE、Y_GUIDE    
    MERGE INTO ix_poi p1
    USING (SELECT p2.pid,
                  p2.log,
                  p2.state,
                  p2.old_name,
                  p2.y_guide,
                  p2.x_guide,
                  p2.geometry,
                  p2.label,
                  p2.post_code,
                  p2.kind_code,
                  p2.old_address,
                  p2.link_pid,
                  p2.side,
                  p2.mesh_id,
                  p2.pmesh_id,
                  l.name_flag,
                  l.address_flag,
                  l.tel_flag,
                  l.kind_flag,
                  l.post_code_flag,
                  l.food_type_flag,
                  l.parent_flag,
                  l.lable_flag,
                  l.display_point_flag,
                  l.guide_point_flag,
                  l.guide_x_flag,
                  l.guide_y_flag
             FROM au_ix_poi p2, temp_au_poi_modify_log l
            WHERE p2.audata_id = l.audata_id
              AND (l.display_point_flag = 1 OR l.guide_point_flag = 1 OR
                  guide_x_flag = 1 OR guide_y_flag = 1)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.geometry = decode(v.display_point_flag,
                                  0,
                                  p1.geometry,
                                  v.geometry),
             p1.mesh_id = decode(v.display_point_flag,
                                  0,
                                  p1.mesh_id,
                                  v.mesh_id), 
             p1.pmesh_id = decode(v.display_point_flag,
                                  0,
                                  decode(v.guide_x_flag,0,decode(v.guide_y_flag,0,p1.pmesh_id,v.pmesh_id),v.pmesh_id),
                                  v.pmesh_id),                                          
             p1.x_guide  = decode(guide_x_flag, 0, decode(guide_y_flag,0,p1.x_guide,v.x_guide),v.x_guide),             
             p1.y_guide = decode(guide_y_flag, 0, decode(guide_x_flag,0,p1.y_guide,v.y_guide),v.y_guide),
             p1.link_pid = decode(v.guide_x_flag,
                                  0,
                                  decode(v.guide_y_flag,
                                         0,
                                         p1.link_pid,
                                         v.link_pid),
                                  v.link_pid),
             p1.side     = decode(v.guide_x_flag,
                                  0,
                                  decode(v.guide_y_flag, 0, p1.side, v.side),
                                  v.side);
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('修改POI主表字段时出错' || SQLERRM);
      RAISE;
    
  END;
  
 /* PROCEDURE process_geo_add_poi IS
  BEGIN
    dbms_output.put_line('BEGIN process_geo_add_poi ');
    do_add(merge_utils.merge_type_geo);
  
  END;*/
  PROCEDURE process_att_geo_add_poi IS
  BEGIN
    dbms_output.put_line('BEGIN process_att_geo_add_poi ');
    do_add(merge_utils.merge_type_geoatt);
  END;

  PROCEDURE process_att_geo_delete_poi IS
  BEGIN
    do_del(merge_utils.merge_type_geoatt);
  END;
  /*ix_poi库中已经存在的pid的 新增融合*
  * 1. 修改内业主表ix_poi的非几何字段；生成修改融合履历
    2 对于子表的修改：a.如果外业子表没有数据，不融合；
     b.否则，删掉内业子表数据，用外业数据进行融合；生成删除子表的融合履历，生成新增子表的融合履历
  */
  PROCEDURE att_add_poi_ext IS
  BEGIN
    --融合主表属性           
    MERGE INTO ix_poi ip
    USING (SELECT *
             FROM au_ix_poi auip
            WHERE auip.state = 3
              AND auip.att_oprstatus in( 0,1)
              AND auip.pid IN (SELECT pid FROM temp_his_ix_poi_ext tmp)) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.kind_code    = rs.kind_code,
             ip.name_groupid = rs.name_groupid,
             ip.road_flag    = rs.road_flag,
             --ip.pmesh_id     = rs.pmesh_id,
             ip.admin_real   = rs.admin_real,
             ip.importance   = rs.importance,
             ip.chain        = rs.chain,
             ip.airport_code = rs.airport_code,
             ip.access_flag  = rs.access_flag,
             ip.open_24h     = rs.open_24h,
             ip.mesh_id_5k   = rs.mesh_id_5k,
             --ip.mesh_id      = rs.mesh_id,
             ip.region_id    = rs.region_id,
             ip.post_code    = rs.post_code,
             ip.edit_flag    = rs.edit_flag,
             ip.state         = rs.state,
             ip.field_state   = rs.field_state,
             ip.label         = rs.label,
             ip.type          = rs.type,
             ip.address_flag  = rs.address_flag,
             ip.ex_priority   = rs.ex_priority,
             ip.edition_flag  = rs.edition_flag,
             ip.poi_memo      = rs.poi_memo,
             ip.old_blockcode = rs.old_blockcode,
             ip.old_name      = rs.old_name,
             ip.old_address   = rs.old_address,
             ip.old_kind      = rs.old_kind,
             ip.poi_num       = rs.poi_num,
             ip.log           = rs.log,
             ip.data_version  = rs.data_version,
             ip.field_task_id = rs.field_task_id;
  
  END;
  PROCEDURE att_add_poi_name_ext1 IS
    v_pid_count number;
  BEGIN
    delete from temp_ix_poi_name_mg;
     --通过POI_PID判断IX_POI_NAME表中Lang_Code为”CHI”、 NAME_CLASS为官方名称（1），名称类型（NAME_TYPE）为标准（1）的记录是否存在，如果不存在，则增加一条记录；否则修改该记录的NAME和NAME_PHONETIC的值。同时更新IX_POI的old_name字段
    MERGE INTO ix_poi_name t
    USING (SELECT au.*
             FROM au_ix_poi_name au,TEMP_HIS_IX_POI_EXT ext
            WHERE au.poi_pid = ext.pid
            and au.lang_code in('CHI', 'CHT') and  au.name_type=2
           ) aurs
    ON (t.lang_code IN('CHI', 'CHT') AND t.name_class = 1 AND t.name_type = 1 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic;
  
     EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi_name au, TEMP_HIS_IX_POI_EXT ext
         WHERE  au.poi_pid = ext.pid  
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid)
           '
      INTO v_pid_count;
      IF (v_pid_count > 0) THEN
          pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
      INSERT INTO temp_ix_poi_name_mg
        (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields) WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT  pid_man.pid_nextval('IX_POI_NAME') AS name_id,
               au.poi_pid,
               nvl(rs.name_groupid, 1) AS name_groupid,
               1 AS name_class,
               1 AS name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_name au, rs,TEMP_HIS_IX_POI_EXT ext
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.poi_pid = ext.pid  
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid);
  end if;
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
  end;
  /*
  * 修改融合poi_name<br/>
     * IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用au_ix_poi_name中名称分类为原始（2）的修改ix_poi_name 中NAME、NAME_PHONETIC 的值<br/>
     * --同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值<br/>
     * -IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的不存在,则用au_ix_poi_name中名称分类为原始（2）的增加一条记录     * 
  */
  PROCEDURE att_add_poi_name_ext2 IS
    v_pid_count number;
  BEGIN
    delete from temp_ix_poi_name_mg;
     --通过POI_PID判断IX_POI_NAME表中Lang_Code为”CHI”、NAME_CLASS为官方名称（1），名称类型（NAME_TYPE）为原始（2）的记录是否存在，如果不存在，则增加一条记录；否则修改该记录的NAME和NAME_PHONETIC的值。同时更新IX_POI的old_name字段
    MERGE INTO ix_poi_name t
    USING (SELECT au.*
             FROM au_ix_poi_name au,TEMP_HIS_IX_POI_EXT ext
            WHERE au.poi_pid = ext.pid  
            and au.lang_code in('CHI', 'CHT') and  au.name_type=2         
           ) aurs
    ON (t.lang_code IN('CHI', 'CHT') AND t.name_class = 1 AND t.name_type =2 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic;
  

      INSERT INTO temp_ix_poi_name_mg
        (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields) WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT au.name_id,
               au.poi_pid,
               nvl(rs.name_groupid, 1) AS name_groupid,
               1 AS name_class,
               2 AS name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_name au, rs,TEMP_HIS_IX_POI_EXT ext
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.poi_pid = ext.pid  
           and not exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid);
         execute immediate '                   
    select count(1) from   au_ix_poi_name au,TEMP_HIS_IX_POI_EXT ext
    where  au.poi_pid = ext.pid  
               and  exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)                      
               AND NOT EXISTS (SELECT 1
                      FROM ix_poi_name ipn
                     WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                       AND name_class = 1
                       AND name_type = 2
                       AND ipn.poi_pid = au.poi_pid)' into v_pid_count; 
     if(v_pid_count>0) then
      pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
          INSERT INTO temp_ix_poi_name_mg
            (name_id,
             poi_pid,
             name_groupid,
             name_class,
             name_type,
             lang_code,
             NAME,
             name_phonetic,
             keywords,
             nidb_pid,
             u_record,
             u_fields) WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name
              GROUP BY poi_pid)
            SELECT pid_man.pid_nextval('IX_POI_NAME') AS name_id,
                   au.poi_pid,
                   nvl(rs.name_groupid, 1) AS name_groupid,
                   1 AS name_class,
                   2 AS name_type,
                   lang_code,
                   NAME,
                   name_phonetic,
                   keywords,
                   nidb_pid,
                   0 AS u_record,
                   NULL AS u_fields
              FROM au_ix_poi_name au, rs,TEMP_HIS_IX_POI_EXT ext
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.poi_pid = ext.pid  
           and exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid);
     end if;             
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理POI名称时出错' || SQLERRM);
      --rollback;
      RAISE;               
  END;
 
  /*
  * 修改融合poi_name<br/>
     * IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用au_ix_poi_name中名称分类为原始（2）的修改ix_poi_name 中NAME、NAME_PHONETIC 的值<br/>
     * --同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值<br/>
     * -IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的不存在,则用au_ix_poi_name中名称分类为原始（2）的增加一条记录     * 
  */
  PROCEDURE att_add_poi_name_ext IS
    v_pid_count number;
  BEGIN
     att_add_poi_name_ext1;
     att_add_poi_name_ext2;           
  END;
  --FIXME:处理name_groupid
  PROCEDURE att_add_poi_address_ext IS
    v_pid_count NUMBER;
  BEGIN
  
    DELETE FROM ix_poi_address c1
     WHERE EXISTS
     (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
       AND c1.lang_code IN ('CHI', 'CHT');
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
       from  au_ix_poi_address c1
       WHERE EXISTS (SELECT 1
                FROM temp_his_ix_poi_ext l
               WHERE  c1.poi_pid = l.pid)
         AND c1.lang_code IN (''CHI'', ''CHT'')
          and exists(select 1 from au_ix_poi au where au.pid=c1.poi_pid and au.att_oprstatus in(0,1))
         '
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_ADDRESS', v_pid_count);
      --改名称 POI名称、POI拼音、OLD名称 (POINAME、POIPY)
      INSERT INTO ix_poi_address
        SELECT pid_man.pid_nextval('IX_POI_ADDRESS') AS name_id,
               name_groupid,
               poi_pid,
               lang_code,
               src_flag,
               fullname,
               fullname_phonetic,
               roadname,
               roadname_phonetic,
               addrname,
               addrname_phonetic,
               province,
               city,
               county,
               town,
               place,
               street,
               
               landmark,
               prefix,
               housenum,
               TYPE,
               subnum,
               surfix,
               estab,
               building,
               floor,
               unit,
               room,
               addons,
               prov_phonetic,
               city_phonetic,
               county_phonetic,
               town_phonetic,
               street_phonetic,
               place_phonetic,
               landmark_phonetic,
               prefix_phonetic,
               housenum_phonetic,
               type_phonetic,
               subnum_phonetic,
               surfix_phonetic,
               estab_phonetic,
               building_phonetic,
               floor_phonetic,
               unit_phonetic,
               room_phonetic,
               addons_phonetic,
               0, --U_RECORD,
               NULL --U_FIELDS      
          FROM au_ix_poi_address c1
         WHERE EXISTS
         (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
           AND c1.lang_code IN ('CHI', 'CHT')
           AND EXISTS (SELECT 1
                  FROM au_ix_poi au
                 WHERE au.pid = c1.poi_pid
                   AND au.att_oprstatus in( 0,1));
    END IF;
  END;
  PROCEDURE att_add_poi_contact_ext IS
  BEGIN
    --将只在外业成果库的插入作业库（CONTACT），CONTACT_TYPE赋值为1
    INSERT INTO ix_poi_contact
      SELECT poi_pid,
             contact_type,
             contact,
             contact_depart,
             priority,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_contact c1
       WHERE EXISTS
       (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact)
         AND EXISTS (SELECT 1
                FROM au_ix_poi au
               WHERE au.pid = c1.poi_pid
                 AND au.att_oprstatus in( 0,1));
    --将只在作业库中有的删除
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS
     (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.poi_pid = c1.poi_pid
               AND auipc.contact = c1.contact)
       AND EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.pid = c1.poi_pid
               AND au.att_oprstatus in( 0,1));
  END;

  
 
  PROCEDURE geo_add_poi_ext IS
  BEGIN
    --融合主表属性           
    MERGE INTO ix_poi ip
    USING (SELECT *
             FROM au_ix_poi auip
            WHERE auip.state = 3
              AND auip.geo_oprstatus in( 0,1)
              AND auip.pid IN (SELECT pid FROM temp_his_ix_poi_ext)) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.geometry = rs.geometry,
             ip.x_guide  = rs.x_guide,
             ip.y_guide  = rs.y_guide,
             ip.link_pid = rs.link_pid,
             ip.side     = rs.side,
             ip.mesh_id = rs.mesh_id,
             ip.pmesh_id= rs.pmesh_id;
  END;

  PROCEDURE mul_mod_poi_address(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    --改名称 POI名称、POI拼音、OLD名称 (POINAME、POIPY、 OLD_NAME、 SA_ITEM第一位更新为“F”)
    --改地址：ADDRESS_NAME、 POINT_ADDRESS
    --删除;
    DELETE FROM ix_poi_address c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid)
       AND c1.lang_code IN ('CHI', 'CHT');
    EXECUTE IMMEDIATE ' SELECT COUNT(1)  
       from  au_ix_poi_address c1
       WHERE c1.audata_id=:v_audata_id
         AND c1.lang_code IN (''CHI'', ''CHT'')'
      INTO v_pid_count
      USING v_audata_id;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_ADDRESS', v_pid_count);
      --改名称 POI名称、POI拼音、OLD名称 (POINAME、POIPY)
      INSERT INTO ix_poi_address
        SELECT pid_man.pid_nextval('IX_POI_ADDRESS') AS name_id,
               name_groupid,
               poi_pid,
               lang_code,
               src_flag,
               fullname,
               fullname_phonetic,
               roadname,
               roadname_phonetic,
               addrname,
               addrname_phonetic,
               province,
               city,
               county,
               town,
               
               place,
               street,
               landmark,
               prefix,
               housenum,
               TYPE,
               subnum,
               surfix,
               estab,
               building,
               floor,
               unit,
               room,
               addons,
               prov_phonetic,
               city_phonetic,
               county_phonetic,
               town_phonetic,
               street_phonetic,
               place_phonetic,
               landmark_phonetic,
               prefix_phonetic,
               housenum_phonetic,
               type_phonetic,
               subnum_phonetic,
               surfix_phonetic,
               estab_phonetic,
               building_phonetic,
               floor_phonetic,
               unit_phonetic,
               room_phonetic,
               addons_phonetic,
               0, --U_RECORD,
               NULL --U_FIELDS      
          FROM au_ix_poi_address c1
         WHERE c1.audata_id = v_audata_id
           AND c1.lang_code IN ('CHI', 'CHT');
           
           
      -- 删除对应的英文和葡文地址记录     
     delete from ix_poi_address
      where poi_pid in
            (SELECT distinct poi_pid
               from au_ix_poi_address c1
              WHERE c1.audata_id = v_audata_id
                AND c1.lang_code IN ('CHI', 'CHT'))
        and lang_code IN ('ENG', 'POR');
           
           
    END IF;
  END;
  
  PROCEDURE mul_mod_poiname_add_c1(v_data_id NUMBER) IS
    v_count NUMBER;
  BEGIN
    EXECUTE IMMEDIATE '
      SELECT COUNT(1)  FROM ix_poi_name ipn
       WHERE ipn.lang_code IN (''CHI'', ''CHT'')
         AND ipn.name_class = 2
         AND EXISTS (SELECT 1
                  FROM au_ix_poi_name au
                 WHERE au.audata_id = :1
                   AND au.lang_code IN (''CHI'', ''CHT'')
                   AND au.name_class = 2 AND ipn.poi_pid = au.poi_pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_name t
               WHERE t.poi_pid = ipn.poi_pid
                 AND t.lang_code IN (''CHI'', ''CHT'')
                 AND t.name_class = 1)'
      INTO v_count
      USING v_data_id;
    IF (v_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_name_mg t  WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
             poi_pid,
             nvl(rs.name_groupid, 1) AS name_groupid, 
             name_class,
             NAME_TYPE ,
             lang_code,
             name,
             name_phonetic,
             keywords,
             nidb_pid,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
          FROM ix_poi_name ipn,rs
         WHERE ipn.lang_code IN (''CHI'', ''CHT'')
           AND ipn.name_class = 2
           AND ipn.poi_pid = rs.poi_pid(+)
           AND EXISTS (SELECT 1
                  FROM au_ix_poi_name au
                 WHERE au.audata_id = :1
                   AND au.lang_code IN (''CHI'', ''CHT'')
                   AND au.name_class = 2 AND ipn.poi_pid = au.poi_pid)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name t
                 WHERE t.poi_pid = ipn.poi_pid
                   AND t.lang_code IN (''CHI'', ''CHT'')
                   AND t.name_class = 1)'
        USING v_data_id;
    END IF;
  
  END;
  /*
  //IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用au_ix_poi_name中名称分类为原始（2）的修改ix_poi_name 中NAME、NAME_PHONETIC 的值<br/>
  //--同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值<br/>
  // -IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的不存在,则用au_ix_poi_name中名称分类为原始（2）的增加一条记录     *
  */
  PROCEDURE mul_mod_poi_name(v_data_id NUMBER) IS
  BEGIN
    --IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在，但是au_ix_poi_name中没有数据的，将IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的数据删除
    DELETE FROM ix_poi_name ipn
     WHERE ipn.lang_code = 'CHI'
       AND ipn.name_class IN (1, 2)
       AND EXISTS (SELECT 1
              FROM au_ix_poi auip
             WHERE auip.pid = ipn.poi_pid
               AND auip.audata_id = v_data_id)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_name auipn
             WHERE auipn.audata_id = v_data_id);
    --IX_POI_NAME表中Lang_Code为”CHI”、名称类型为原始（2）的存在,则用au_ix_poi_name中名称分类为原始（2）的修改ix_poi_name 中NAME、NAME_PHONETIC 的值
  
    MERGE INTO ix_poi_name ipn
    USING (SELECT * FROM view_mg_mul_ix_poi_name WHERE audata_id = v_data_id) aurs
    ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE
         SET ipn.name = aurs.name, ipn.name_phonetic = aurs.name_phonetic;
    --同时更新Lang_Code为“CHI”、名称类型为标准（1）的NAME和NAME_PHONETIC的值
    MERGE INTO ix_poi_name ipn
    USING (SELECT * FROM view_mg_mul_ix_poi_name WHERE audata_id = v_data_id) aurs
    ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = 1)
    WHEN MATCHED THEN
      UPDATE
         SET ipn.name = aurs.name, ipn.name_phonetic = aurs.name_phonetic;
    --如果name_class=1的数据不存在，需要将name_class=2的copy一条插入
    mul_mod_poiname_add_c1(v_data_id);
  END;
  /*--FIXME:参考process_modify_poi_relation
  PROCEDURE mul_mod_poi_relation(v_data_id VARCHAR2) IS
  BEGIN
    DELETE FROM ix_poi_children ipc
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi auip
             WHERE auip.audata_id = v_data_id
               AND auip.pid = ipc.child_poi_pid);
    --1.修改的是子，将父和子都copy到内业中
    INSERT INTO ix_poi_parent
      SELECT group_id,
             parent_poi_pid,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_parent p
       WHERE p.group_id IN
             (SELECT c.group_id
                FROM au_ix_poi l, au_ix_poi_children c
               WHERE l.audata_id = v_data_id
                 AND c.child_poi_pid = l.pid
                 AND c.group_id NOT IN
                     (SELECT group_id FROM temp_his_ix_poi_parent));
  
    --添加子表
    INSERT INTO ix_poi_children
      SELECT c.group_id,
             c.child_poi_pid,
             c.relation_type,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS,
        FROM au_ix_poi l, au_ix_poi_children c
       WHERE l.audata_id = v_data_id
         AND c.child_poi_pid = l.pid;
  END;*/
  PROCEDURE mul_mod_poi_contact(v_data_id NUMBER) IS
  BEGIN
    --将只在外业成果库的插入作业库（CONTACT），CONTACT_TYPE赋值为1
    INSERT INTO ix_poi_contact
      SELECT poi_pid,
             contact_type,
             contact,
             contact_depart,
             priority,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_contact c1
       WHERE c1.audata_id = v_data_id
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact);
    --将只在作业库中有的删除
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_data_id
               AND c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.audata_id = v_data_id
               AND auipc.contact = c1.contact);
  
  END;
  

  PROCEDURE mul_att_mod_ix_poi(v_data_id    NUMBER,
                               kindflag     NUMBER,
                               labelflag    NUMBER,
                               postcodeflag NUMBER,
                               addresflag   NUMBER,
                               nameflag     NUMBER) IS
  BEGIN
    MERGE INTO ix_poi p1
    USING (SELECT p2.pid,
                  p2.log,
                  p2.state,
                  p2.old_name,
                  p2.y_guide,
                  p2.x_guide,
                  p2.geometry,
                  p2.label,
                  p2.post_code,
                  p2.kind_code,
                  p2.old_address,
                  p2.old_kind,
                  auipn.name     AS auoldname,
                  auipa.fullname AS auoldaddress,
                  p2.chain,
                  p2.open_24h
             FROM au_ix_poi p2,
                  (SELECT *
                     FROM au_ix_poi_name
                    WHERE audata_id = v_data_id
                      AND lang_code IN ('CHI', 'CHT')
                      AND name_type = 2) auipn,
                  (SELECT *
                     FROM au_ix_poi_address
                    WHERE audata_id = v_data_id
                      AND lang_code IN ('CHI', 'CHT')) auipa
            WHERE p2.audata_id = v_data_id
              AND p2.audata_id = auipn.audata_id(+)
              AND p2.audata_id = auipa.audata_id(+)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.chain       = decode(kindflag,
                                     1,
                                     nvl(v.chain, p1.chain),
                                     p1.chain),
             p1.old_address = decode(addresflag,
                                     1,
                                     v.auoldaddress,
                                     p1.old_address),
             p1.kind_code   = decode(kindflag, 1, v.kind_code, p1.kind_code),
             p1.old_kind    = decode(kindflag, 1, v.kind_code, p1.old_kind),
             p1.post_code   = decode(postcodeflag,
                                     1,
                                     v.post_code,
                                     p1.post_code),
             p1.label       = decode(labelflag, 1, v.label, p1.label),
             p1.open_24h    = decode(labelflag, 1,(CASE
    WHEN instr(to_single_byte(v.label), '24小时') > 0 THEN 1 ELSE p1.open_24h END), p1.open_24h), p1.old_name = decode(nameflag, 1, v.auoldname, p1.old_name);
  
  END;
  PROCEDURE mul_geo_mod_ix_poi(v_data_id        NUMBER,
                               displaypointflag NUMBER,
                               guidepointflag   NUMBER,
                               guidexflag       NUMBER,
                               guideyflag       NUMBER) IS
  BEGIN
    MERGE INTO ix_poi p1
    USING (SELECT p2.pid,
                  p2.log,
                  p2.state,
                  p2.y_guide,
                  p2.x_guide,
                  p2.geometry,
                  p2.link_pid,
                  p2.side,
                  p2.mesh_id,
                  p2.pmesh_id
             FROM au_ix_poi p2
            WHERE p2.audata_id = v_data_id) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET p1.geometry = decode(displaypointflag,
                                  0,
                                  p1.geometry,
                                  v.geometry),
             p1.mesh_id = decode(displaypointflag,
                                  0,
                                  p1.mesh_id,
                                  v.mesh_id), 
              p1.pmesh_id = decode(displaypointflag,
                                  0,
                                  decode(guidexflag,0,decode(guideyflag,0,p1.pmesh_id,v.pmesh_id),v.pmesh_id),
                                  v.pmesh_id),                                                                                         
              p1.x_guide  = decode(guidexflag, 0, decode(guideyflag,0,p1.x_guide,v.x_guide),v.x_guide),             
              p1.y_guide = decode(guideyflag, 0, decode(guidexflag,0,p1.y_guide,v.y_guide),v.y_guide),
             
             p1.link_pid = decode(guidexflag,
                                  0,
                                  decode(guideyflag,
                                         0,
                                         p1.link_pid,
                                         v.link_pid),
                                  v.link_pid),
             p1.side     = decode(guideyflag,
                                  0,
                                  decode(guidexflag, 0, p1.side, v.side),
                                  v.side);
  END;

  PROCEDURE mul_att_add_poi_ext(v_audata_id NUMBER) IS
  BEGIN
    --融合主表属性           
    MERGE INTO ix_poi ip
    USING (SELECT *
             FROM au_ix_poi auip
            WHERE auip.audata_id = v_audata_id
              AND auip.att_oprstatus in( 0,1)) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
         SET ip.kind_code    = rs.kind_code,
             ip.name_groupid = rs.name_groupid,
             ip.road_flag    = rs.road_flag,
             --ip.pmesh_id     = rs.pmesh_id,
             ip.admin_real   = rs.admin_real,
             ip.importance   = rs.importance,
             ip.chain        = rs.chain,
             ip.airport_code = rs.airport_code,
             ip.access_flag  = rs.access_flag,
             ip.open_24h     = rs.open_24h,
             ip.mesh_id_5k   = rs.mesh_id_5k,
             --ip.mesh_id      = rs.mesh_id,
             ip.region_id    = rs.region_id,
             ip.post_code    = rs.post_code,
             ip.edit_flag    = rs.edit_flag,
             --ip.state         = rs.state,
             ip.field_state   = rs.field_state,
             ip.label         = rs.label,
             ip.type          = rs.type,
             ip.address_flag  = rs.address_flag,
             ip.ex_priority   = rs.ex_priority,
             ip.edition_flag  = rs.edition_flag,
             ip.poi_memo      = rs.poi_memo,
             ip.old_blockcode = rs.old_blockcode,
             ip.old_name      = rs.old_name,
             ip.old_address   = rs.old_address,
             ip.old_kind      = rs.old_kind,
             ip.poi_num       = rs.poi_num,
             ip.log           = rs.log,
             ip.data_version  = rs.data_version,
             ip.field_task_id = rs.field_task_id;
  END;
  PROCEDURE mul_geo_add_poi_ext(v_audata_id NUMBER) IS
  BEGIN
    --融合主表属性           
    MERGE INTO ix_poi ip
    USING (SELECT *
             FROM au_ix_poi auip
            WHERE auip.audata_id = v_audata_id
              AND auip.geo_oprstatus in( 0,1)) rs
    ON (ip.pid = rs.pid)
    WHEN MATCHED THEN
      UPDATE
          SET ip.geometry = rs.geometry,
             ip.x_guide  = rs.x_guide,
             ip.y_guide  = rs.y_guide,
             ip.link_pid = rs.link_pid,
             ip.side     = rs.side,
             ip.mesh_id = rs.mesh_id,
             ip.pmesh_id = rs.pmesh_id;
  END;
  
 PROCEDURE mul_att_add_poiname_ext2(v_audata_id NUMBER) IS
    v_pid_count number;
    begin
      delete from temp_ix_poi_name_mg;
 --通过POI_PID判断IX_POI_NAME表中Lang_Code为”CHI”、NAME_CLASS为官方名称（1），名称类型（NAME_TYPE）为原始（2）的记录是否存在，如果不存在，则增加一条记录；否则修改该记录的NAME和NAME_PHONETIC的值。同时更新IX_POI的old_name字段
     EXECUTE IMMEDIATE '   
    MERGE INTO ix_poi_name t
    USING (SELECT au.*
             FROM au_ix_poi_name au
            WHERE au.audata_id= :v_audata_id   
            and au.lang_code in(''CHI'', ''CHT'') and  au.name_type=2       
           ) aurs
    ON (t.lang_code IN(''CHI'', ''CHT'') AND t.name_class = 1 AND t.name_type = 2 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic' using v_audata_id; 
       EXECUTE IMMEDIATE '   
      INSERT INTO ix_poi_name--temp_ix_poi_name_mg
        (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields) WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid          
           FROM ix_poi_name t1
            where t1.poi_pid in(select poi_pid from au_ix_poi_name au where au.audata_id=:v_audata_id)
          GROUP BY poi_pid)
        SELECT au.name_id,
               au.poi_pid,
               nvl(rs.name_groupid, 1) AS name_groupid,
               1 AS name_class,
               2 AS name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_name au, rs
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.audata_id=:v_audata_id 
           and not exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id) 
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid)' using v_audata_id,v_audata_id;
     EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi_name au
         WHERE  au.audata_id=:v_audata_id 
           and exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id) 
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid)
           '
      INTO v_pid_count using v_audata_id;
      IF (v_pid_count > 0) THEN
          pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
          EXECUTE IMMEDIATE '
      INSERT INTO ix_poi_name--temp_ix_poi_name_mg
        (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields) WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name t1
           where t1.poi_pid in(select poi_pid from au_ix_poi_name au where au.audata_id=:v_audata_id)
          GROUP BY poi_pid)
        SELECT  pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
               au.poi_pid,
               nvl(rs.name_groupid, 1) AS name_groupid,
               1 AS name_class,
               2 AS name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_name au, rs
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.audata_id=:v_audata_id 
           and exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id) 
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid)'using v_audata_id,v_audata_id;
  end if;               
  INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
      commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理POI名称时出错' || SQLERRM);
      --rollback;
      RAISE;               
  END;
  PROCEDURE mul_att_add_poiname_ext(v_audata_id NUMBER) IS
    v_pid_count number;
    begin
      delete from temp_ix_poi_name_mg;
    --通过POI_PID判断IX_POI_NAME表中Lang_Code为”CHI”、 NAME_CLASS为官方名称（1），名称类型（NAME_TYPE）为标准（1）的记录是否存在，如果不存在，则增加一条记录；否则修改该记录的NAME和NAME_PHONETIC的值。同时更新IX_POI的old_name字段
     EXECUTE IMMEDIATE '
    MERGE INTO ix_poi_name t
    USING (SELECT au.*
             FROM au_ix_poi_name au
            WHERE au.audata_id=:v_audata_id
            and au.lang_code in(''CHI'', ''CHT'') and  au.name_type=2
           ) aurs
    ON (t.lang_code IN(''CHI'', ''CHT'') AND t.name_class = 1 AND t.name_type = 1 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic' using v_audata_id;
  
     EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi_name au
         WHERE  au.audata_id=:v_audata_id 
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid)
           '
      INTO v_pid_count using v_audata_id;
      IF (v_pid_count > 0) THEN
          pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
       EXECUTE IMMEDIATE '   
      INSERT INTO ix_poi_name--temp_ix_poi_name_mg
        (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields) WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid           
           FROM ix_poi_name t1
           where t1.poi_pid in(select poi_pid from au_ix_poi_name au where au.audata_id=:v_audata_id)
          GROUP BY poi_pid)
        SELECT  pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
               au.poi_pid,
               nvl(rs.name_groupid, 1) AS name_groupid,
               1 AS name_class,
               1 AS name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_name au, rs
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.audata_id=:v_audata_id 
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid)' using v_audata_id,v_audata_id;
  end if;
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
      commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('处理POI名称时出错' || SQLERRM);
      --rollback;
      RAISE;               
  END;
  PROCEDURE mul_att_add_address_ext(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    DELETE FROM ix_poi_address c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid)
       AND c1.lang_code IN ('CHI', 'CHT');
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
       from  au_ix_poi_address c1
       WHERE c1.audata_id=:v_audata_Id
         AND c1.lang_code IN (''CHI'', ''CHT'')'
      INTO v_pid_count
      USING v_audata_id;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_ADDRESS', v_pid_count);
      --改名称 POI名称、POI拼音、OLD名称 (POINAME、POIPY)
      EXECUTE IMMEDIATE 'INSERT INTO ix_poi_address
        SELECT pid_man.pid_nextval(''IX_POI_ADDRESS'') AS name_id,
               name_groupid,
               poi_pid,
               lang_code,
               src_flag,
               fullname,
               fullname_phonetic,
               roadname,
               roadname_phonetic,
               addrname,
               addrname_phonetic,
               province,
               city,
               county,
               town,
               
               place,
               street,
               landmark,
               prefix,
               housenum,
               TYPE,
               subnum,
               surfix,
               estab,
               building,
               floor,
               unit,
               room,
               addons,
               prov_phonetic,
               city_phonetic,
               county_phonetic,
               town_phonetic,
               street_phonetic,
               place_phonetic,
               landmark_phonetic,
               prefix_phonetic,
               housenum_phonetic,
               type_phonetic,
               subnum_phonetic,
               surfix_phonetic,
               estab_phonetic,
               building_phonetic,
               floor_phonetic,
               unit_phonetic,
               room_phonetic,
               addons_phonetic,
               0, --U_RECORD,
               NULL --U_FIELDS      
          FROM au_ix_poi_address c1
         WHERE c1.audata_id=:v_audata_id
           AND c1.lang_code IN (''CHI'', ''CHT'')'
        USING v_audata_id;
    END IF;
  END;
  PROCEDURE mul_att_add_contact_ext(v_audata_id NUMBER) IS
  BEGIN
    --将只在外业成果库的插入作业库（CONTACT），CONTACT_TYPE赋值为1
    INSERT INTO ix_poi_contact
      SELECT poi_pid,
             contact_type,
             contact,
             contact_depart,
             priority,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_contact c1
       WHERE c1.audata_id = v_audata_id
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact);
    --将只在作业库中有的删除
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.poi_pid = c1.poi_pid
               AND auipc.contact = c1.contact);
  END;

  
  PROCEDURE mul_add_address(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    DELETE FROM temp_ix_poi_address_mg;
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_poi_address au
     WHERE au.audata_id = v_audata_id;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_ADDRESS', v_pid_count);
      INSERT INTO temp_ix_poi_address_mg
        SELECT pid_man.pid_nextval('IX_POI_ADDRESS') AS name_id,
               c.name_groupid,
               c.poi_pid,
               c.lang_code,
               c.src_flag,
               c.fullname,
               c.fullname_phonetic,
               c.roadname,
               c.roadname_phonetic,
               c.addrname,
               c.addrname_phonetic,
               c.province,
               c.city,
               c.county,
               c.town,
              
               c.place,
               c.street,
               c.landmark,
               c.prefix,
               c.housenum,
               c.type,
               c.subnum,
               c.surfix,
               c.estab,
               c.building,
               c.floor,
               c.unit,
               c.room,
               c.addons,
               c.prov_phonetic,
               c.city_phonetic,
               c.county_phonetic,
               c.town_phonetic,
               c.street_phonetic,
               c.place_phonetic,
               c.landmark_phonetic,
               c.prefix_phonetic,
               c.housenum_phonetic,
               c.type_phonetic,
               c.subnum_phonetic,
               c.surfix_phonetic,
               c.estab_phonetic,
               c.building_phonetic,
               c.floor_phonetic,
               c.unit_phonetic,
               c.room_phonetic,
               c.addons_phonetic,
               0, --c.U_RECORD,
               NULL --c.U_FIELDS
          FROM au_ix_poi_address c
         WHERE c.audata_id = v_audata_id;
      INSERT INTO ix_poi_address
        SELECT * FROM temp_ix_poi_address_mg;
    END IF;
  END;
  PROCEDURE mul_add_contact(v_audata_id NUMBER) IS
  BEGIN
    INSERT INTO ix_poi_contact
      SELECT c.poi_pid,
             c.contact_type,
             c.contact,
             c.contact_depart,
             c.priority,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
        FROM au_ix_poi_contact c
       WHERE c.audata_id = v_audata_id;
  END;
   PROCEDURE mul_add_poiname1(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
    BEGIN
       DELETE FROM temp_ix_poi_name_mg;
      --增加一条name_type=2的，该条数据的name_id重新生成
     EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi p, au_ix_poi_name c
         WHERE p.state = 3
           AND p.audata_id = c.audata_id
           AND p.pid = c.poi_pid        
           and p.audata_id=:v_audata_id
           '
      INTO v_pid_count using v_audata_id;
      IF (v_pid_count > 0) THEN
          pid_man.apply_pid('IX_POI_NAME', v_pid_count); 
          EXECUTE IMMEDIATE '
         INSERT INTO temp_ix_poi_name_mg 
          (name_id,
               poi_pid,
               name_groupid,
               name_class,
               name_type,
               lang_code,
               NAME,
               name_phonetic,
               keywords,
               nidb_pid,
               u_record,
               u_fields)
         WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name
              GROUP BY poi_pid)
            SELECT pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
                   c.poi_pid,
                   nvl(rs.name_groupid, 1) AS name_groupid, 
                   1 AS name_class,
                   1 AS NAME_TYPE ,
                   c.lang_code,
                   c.name,
                   c.name_phonetic,
                   c.keywords,
                   c.nidb_pid,
                   0, --c.U_RECORD,s
                   NULL --c.U_FIELDS
              FROM au_ix_poi p, au_ix_poi_name c, rs
             WHERE p.state = 3
               AND p.audata_id = c.audata_id
               AND p.pid = c.poi_pid
               AND p.pid = rs.poi_pid(+)    
               and p.audata_id=:v_audata_id
           'using v_audata_id;
     end if; 
   INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
    END;
  PROCEDURE mul_add_poiname2(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
     DELETE FROM temp_ix_poi_name_mg; --为生成履历，采用零食表保存新增的数据   
   --增加一条name_type=1的
  EXECUTE IMMEDIATE '
  INSERT INTO temp_ix_poi_name_mg 
         (name_id,
         poi_pid,
         name_groupid,
         name_class,
         name_type,
         lang_code,
         NAME,
         name_phonetic,
         keywords,
         nidb_pid,
         u_record,
         u_fields)
  WITH rs AS
      (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_poi_name
        GROUP BY poi_pid)
      SELECT c.name_id,
             c.poi_pid,
             nvl(rs.name_groupid, 1) AS name_groupid, 
             1 AS name_class,
             2 AS NAME_TYPE ,
             c.lang_code,
             c.name,
             c.name_phonetic,
             c.keywords,
             c.nidb_pid,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
        FROM au_ix_poi p, au_ix_poi_name c, rs
       WHERE p.state = 3
         AND p.audata_id = c.audata_id
         AND p.pid = c.poi_pid
         AND p.pid = rs.poi_pid(+)    
         and p.audata_id=:v_audata_id' using v_audata_id;
    
   --增加一条name_type=2的，该条数据的name_id重新生成   
   INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
  END;
  PROCEDURE mul_add_poiname(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    mul_add_poiname1(v_audata_Id);
    mul_add_poiname2(v_audata_Id);
  END;
 
 
  PROCEDURE mul_add_poi(v_audata_id NUMBER) IS
  BEGIN
    INSERT INTO ix_poi
      SELECT /*+index(p,IX_AIP_PS)*/
       pid,
       kind_code,
       geometry,
       x_guide,
       y_guide,
       link_pid,
       side,
       name_groupid,
       road_flag,
       pmesh_id,
       admin_real,
       importance,
       chain,
       airport_code,
       access_flag,
       open_24h,
       mesh_id_5k,
       mesh_id,
       region_id,
       post_code,       
       edit_flag,
       DIF_GROUPID,
       RESERVED,
       state,
       field_state,
       label,
       TYPE,
       address_flag,
       ex_priority,
       edition_flag,
       poi_memo,
       old_blockcode,
       old_name,
       old_address,
       old_kind,
       poi_num,
       log,
       0, --TASK_ID,task_id需要特殊处理
       data_version,
       field_task_id,
       0, --U_RECORD,
       NULL --U_FIELDS
        FROM au_ix_poi p
       WHERE p.audata_id = v_audata_id;
  
    mul_add_address(v_audata_id);
    mul_add_contact(v_audata_id);
    mul_add_poiname(v_audata_id);
  
  END;
 
  PROCEDURE mul_del_poi(v_audata_id NUMBER) IS
  BEGIN
    --删除主表;
    DELETE ix_poi poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.pid
               AND poi2.audata_id = v_audata_id);
    --删除IX_POI_ADDRESS;
    DELETE ix_poi_address poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.poi_pid
               AND poi2.audata_id = v_audata_id);
    --删除IX_POI_CONTACT;
    DELETE ix_poi_contact poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.poi_pid
               AND poi2.audata_id = v_audata_id);
    --ix_poi_name_tone        
  /*  DELETE ix_poi_name_tone ipnt
     WHERE EXISTS (SELECT 1
              FROM ix_poi_name poi1
             WHERE EXISTS (SELECT 1
                      FROM au_ix_poi poi2
                     WHERE poi2.pid = poi1.poi_pid
                       AND poi2.audata_id = v_audata_id)
               AND poi1.name_id = ipnt.name_id); */
    DELETE ix_poi_name_flag ipnt
     WHERE EXISTS (SELECT 1
              FROM ix_poi_name poi1
             WHERE EXISTS (SELECT 1
                      FROM au_ix_poi poi2
                     WHERE poi2.pid = poi1.poi_pid
                       AND poi2.audata_id = v_audata_id)
               AND poi1.name_id = ipnt.name_id);
    --删除IX_POI_NAME;
    DELETE ix_poi_name poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.poi_pid
               AND poi2.audata_id = v_audata_id);
               
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('删除POI时出错' || SQLERRM);
      --rollback;
      RAISE;
  END;
  
  PROCEDURE mul_mod_poi_state_ext(v_pid   NUMBER,
                                  v_state NUMBER,
                                  v_log   ix_poi.log%TYPE) IS
  BEGIN
    UPDATE ix_poi p1
       SET p1.state = v_state, p1.log = v_log
     WHERE p1.pid = v_pid;
  END;
  PROCEDURE reset_temp_ixpoi_name(v_audata_id NUMBER) IS
  BEGIN
    DELETE FROM temp_his_ix_poi_name n
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.audata_id = v_audata_id
               AND au.pid = n.poi_pid);
    INSERT INTO temp_his_ix_poi_name
      SELECT *
        FROM ix_poi_name n
       WHERE EXISTS (SELECT 1
                FROM au_ix_poi au
               WHERE au.audata_id = v_audata_id
                 AND au.pid = n.poi_pid);
  END;
  PROCEDURE mul_reset_temp_ix_poi(v_pid NUMBER) IS
  BEGIN
    DELETE FROM temp_his_ix_poi WHERE pid = v_pid;
    INSERT INTO temp_his_ix_poi
      SELECT * FROM ix_poi ip WHERE ip.pid = v_pid;
  END;
  
  PROCEDURE process_att_poi_editon_flag IS
  BEGIN
    MERGE INTO ix_poi p1
    USING (SELECT pid, edition_flag
             FROM au_ix_poi au
            WHERE au.state = 2
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_poi_mul_task tmp
                    WHERE tmp.pid = au.pid)) aurs
    ON (p1.pid = aurs.pid AND ((p1.edition_flag IS NULL AND aurs.edition_flag IS NOT NULL) OR (p1.edition_flag IS NOT NULL AND aurs.edition_flag IS NULL) OR (p1.edition_flag <> aurs.edition_flag)))
    WHEN MATCHED THEN
      UPDATE SET p1.edition_flag = aurs.edition_flag;
  END;
  PROCEDURE mul_att_poi_editon_flag(v_audata_id NUMBER) IS
  BEGIN
    MERGE INTO ix_poi p1
    USING (SELECT pid, edition_flag
             FROM au_ix_poi au
            WHERE au.audata_id = v_audata_id) aurs
    ON (p1.pid = aurs.pid AND ((p1.edition_flag IS NULL AND aurs.edition_flag IS NOT NULL) OR (p1.edition_flag IS NOT NULL AND aurs.edition_flag IS NULL) OR (p1.edition_flag <> aurs.edition_flag)))
    WHEN MATCHED THEN
      UPDATE SET p1.edition_flag = aurs.edition_flag;
  END;
  PROCEDURE process_mod_poi_state(v_merge_type VARCHAR2) IS
  BEGIN
  
    EXECUTE IMMEDIATE 'MERGE INTO ix_poi p1
    USING (SELECT p2.pid, p2.log, p2.state
             FROM au_ix_poi p2
            WHERE p2.state = 2
              AND (' ||
                      merge_utils.get_proxypoi_clause(v_merge_type, 'p2') || ')
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_poi_mul_task mul
                    WHERE mul.pid = p2.pid)) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE SET p1.log = v.log, p1.state = 2';
  END;
   procedure process_mod_poi_name_delclass5 is
     begin
     /*  delete from ix_poi_name_tone t2
       WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1
                WHERE t1.name_class = 5
                  AND t1.poi_pid IN
                      (SELECT au.pid
                         FROM au_ix_poi au, temp_au_poi_modify_log l
                        WHERE au.audata_id = l.audata_id
                          AND au.att_oprstatus in( 0,1)
                          AND l.name_flag = 1)); */
       DELETE FROM ix_poi_name_flag t2
        WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1
                WHERE t1.name_class = 5
                  AND t1.poi_pid IN
                      (SELECT au.pid
                         FROM au_ix_poi au, temp_au_poi_modify_log l
                        WHERE au.audata_id = l.audata_id
                          AND au.att_oprstatus in( 0,1)
                          AND l.name_flag = 1));
       
       DELETE FROM ix_poi_name t1
        WHERE t1.name_class = 5
          AND t1.poi_pid IN (SELECT au.pid
                               FROM au_ix_poi au, temp_au_poi_modify_log l
                              WHERE au.audata_id = l.audata_id
                                AND au.att_oprstatus in( 0,1)
                                AND l.name_flag = 1);
     end ;
     procedure process_ext_poi_name_delclass5 is
     begin
     /*  delete from ix_poi_name_tone t2
       WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1
                WHERE t1.name_class = 5
                  AND t1.poi_pid IN
                      (SELECT au.pid
                               FROM au_ix_poi au, temp_his_ix_poi_ext l,au_ix_poi_name t2
                              WHERE au.pid = l.pid
                                AND au.att_oprstatus in(0,1)
                                AND au.audata_id=t2.audata_id)); */
       DELETE FROM ix_poi_name_flag t2
        WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1
                WHERE t1.name_class = 5
                  AND t1.poi_pid IN
                      (SELECT au.pid
                               FROM au_ix_poi au, temp_his_ix_poi_ext l,au_ix_poi_name t2
                              WHERE au.pid = l.pid
                                AND au.att_oprstatus in(0,1)
                                AND au.audata_id=t2.audata_id));
       
       DELETE FROM ix_poi_name t1
        WHERE t1.name_class = 5
          AND t1.poi_pid IN (SELECT au.pid
                               FROM au_ix_poi au, temp_his_ix_poi_ext l,au_ix_poi_name t2
                              WHERE au.pid = l.pid
                                AND au.att_oprstatus in(0,1)
                                AND au.audata_id=t2.audata_id);
                                 commit;
     end ;
     procedure mul_mod_poi_name_delclass5(v_audata_id number) is
       begin
     /*   execute immediate' delete from ix_poi_name_tone t2
       WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1,au_ix_poi au 
                WHERE t1.name_class = 5
                  AND t1.poi_pid =au.pid
                  and au.att_oprstatus in(0,1)
                  and au.audata_id=:v_audata_id)' using v_audata_id ; */
       execute immediate ' DELETE FROM ix_poi_name_flag t2
        WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1,au_ix_poi au 
                WHERE t1.name_class = 5
                  AND t1.poi_pid =au.pid
                  and au.att_oprstatus in(0,1)
                  and au.audata_id=:v_audata_id)' using v_audata_id;
       
       execute immediate 'DELETE FROM ix_poi_name t1
        WHERE t1.name_class = 5
          AND t1.poi_pid IN (SELECT au.pid
                               FROM au_ix_poi au
                              WHERE au.att_oprstatus in( 0,1)
                                AND au.audata_id=:v_audata_id)' using v_audata_id;
       end;
    PROCEDURE mod_main_poi_state_ext(v_merge_type VARCHAR2) IS
    BEGIN
      EXECUTE IMMEDIATE '
    MERGE INTO ix_poi p1
    USING (select au.* from au_ix_poi au ,temp_his_ix_poi_ext ext where au.pid=ext.pid  and (' ||
                        merge_utils.get_proxypoi_clause(v_merge_type, 'au') ||
                        ')) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE SET p1.state = 3,p1.log=v.log';
    END;
  

END merge_proxy_poi;
/
