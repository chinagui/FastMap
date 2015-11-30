CREATE OR REPLACE PACKAGE merge_au_ix_no_his IS
  -- Author  : LIUQING
  -- Created : 2011/5/20 11:29:54
  -- Purpose : �ϲ���ҵPOI����ҵPOI����,��ҵ�����ƺϲ�����ҵ��������  
  ----�����˻���152 VM_nijGhOFVXZ ibYErADzVl
  ----fixme:��Ҫ����AU_IX_POI_NAME_FLAG ��AU_IX_POI_FLAG�����\�޸�

  TYPE type_ix_poi IS TABLE OF ix_poi%ROWTYPE;
  PROCEDURE reset_tmp_ix_parent;
  PROCEDURE reset_tmp_ix_poi_name;
  PROCEDURE reset_tmp_ix_poi;
  PROCEDURE reset_tmp_ix_poi_ext(v_merge_type VARCHAR2);
  PROCEDURE reset_temp_mg_table;
  PROCEDURE process_modify_poi_contact;
  PROCEDURE process_modify_poi_name_add;
  PROCEDURE process_modify_poi_name;
  --PROCEDURE process_modify_poi_name_en;
  PROCEDURE process_modify_poi_address;
  PROCEDURE delete_mul_relation;  --ɾ�����ӹ�ϵ���ж��������   
  PROCEDURE process_modify_poi_label;
  PROCEDURE process_modify_poi_relation;
  PROCEDURE process_modify_poi_restaurant;
  PROCEDURE delete_isolated_poi_parent;
  PROCEDURE gather_table_stats(v_schemaname VARCHAR2);
  PROCEDURE pre_process_poi(v_merge_type VARCHAR2);
  PROCEDURE process_attgeo_modify_log;
  PROCEDURE process_att_modify_log;
  PROCEDURE process_geo_modify_log;
  PROCEDURE process_att_add_poi;
  PROCEDURE process_geo_add_poi;
  PROCEDURE process_att_geo_delete_poi;
  PROCEDURE process_att_modify_main_poi;
  PROCEDURE process_geo_modify_main_poi;
  PROCEDURE process_geo_delete_poi;
  PROCEDURE process_att_delete_poi;
  PROCEDURE process_att_geo_add_poi;
  --��ҵ�����Ѿ����ڵ����ݣ���ҵ��add �ں�
  PROCEDURE att_add_poi_ext;
  PROCEDURE process_mod_poi_name_add_ext;
  PROCEDURE att_add_poi_name_ext;
  PROCEDURE att_add_poi_address_ext;
  PROCEDURE att_add_poi_contact_ext;
  PROCEDURE att_add_relation_ext;
  PROCEDURE att_add_restaurant_ext;
  PROCEDURE geo_add_poi_ext;
  PROCEDURE process_att_geoatt_samepoi(v_merge_type VARCHAR2);
  --PROCEDURE att_geo_add_poi_ext;
  PROCEDURE mod_main_poi_state_ext(v_merge_type varchar2); 

  PROCEDURE mul_mod_poi_address(v_audata_id NUMBER);
  PROCEDURE mul_mod_poi_restaurant(v_audata_id NUMBER);
  PROCEDURE commit_poi_name_insert;
  PROCEDURE mul_mod_poi_name_add(v_data_id NUMBER);
  PROCEDURE mul_mod_poi_name(v_data_id NUMBER);
  --PROCEDURE mul_mod_poi_relation(v_data_id VARCHAR2);
  PROCEDURE mul_mod_poi_contact(v_data_id NUMBER);
  PROCEDURE mul_att_mod_ix_poi(v_data_id    NUMBER,
                               kindflag     NUMBER,
                               labelflag    NUMBER,
                               postcodeflag NUMBER,
                               addresflag   NUMBER,
                               open24hflag  NUMBER,
                               nameflag     NUMBER,
                               isVerifiedFlag number);
  PROCEDURE mul_geo_mod_ix_poi(v_data_id        NUMBER,
                               displaypointflag NUMBER,
                               guidepointflag   NUMBER,
                               guidexflag       NUMBER,
                               guideyflag       NUMBER);

  PROCEDURE mul_att_add_poi_ext(v_audata_id NUMBER);
  PROCEDURE mul_geo_add_poi_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_poiname_ext_add(v_audata_id NUMBER);
  PROCEDURE mul_att_add_poiname_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_delete_poiname_flag(v_audata_id NUMBER);
  --PROCEDURE mul_att_delete_poiname_en_flag(v_audata_id NUMBER);
  
  --PROCEDURE mul_att_modify_poiname_en(v_audata_id NUMBER);
  PROCEDURE mul_att_add_address_ext(v_audata_id NUMBER);
  --�����ע�ֶΰ������ο���ַ�����ںϺ���ɾ�����ĵ�ַ�ı�ʶ��Ϣ������IX_POI_FLAG���в��롰�̶���־����ʩ�����ַ���ı�ʶ��Ϣ
  PROCEDURE mul_att_add_poi_label_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_contact_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_relation_ext(v_audata_id NUMBER);
  PROCEDURE mul_att_add_restaurant_ext(v_audata_id NUMBER);
  PROCEDURE mul_add_poi(v_audata_id NUMBER);
  PROCEDURE mul_add_poi_rel(v_audata_id NUMBER);
  PROCEDURE mul_del_poi(v_audata_id NUMBER);

  PROCEDURE process_mod_poi_state(v_merge_type VARCHAR2);
  PROCEDURE mul_mod_ix_poi_state(v_data_id NUMBER);
  PROCEDURE mul_mod_poi_state_ext(v_pid   NUMBER,
                                  v_state NUMBER,
                                  v_log   ix_poi.log%TYPE);
  PROCEDURE process_name_groupid;

  PROCEDURE reset_temp_ixpoi_name(v_audata_id NUMBER);
  --PROCEDURE mul_reset_temp_ix_poi(v_pid NUMBER);
  PROCEDURE process_mod_poi_flag;
  PROCEDURE process_del_poi_flag_poi_level;
  PROCEDURE process_add_poi_flag_poi_level;
  PROCEDURE process_mod_verified_mode_flag;
  PROCEDURE att_add_poi_kind_code_ext;
  PROCEDURE att_add_poi_level_flag_ext;
  PROCEDURE att_verified_mode_flag_ext;
  PROCEDURE att_add_poi_flag_ext;
  PROCEDURE mul_add_poi_flag(v_audata_id NUMBER);
  PROCEDURE del_ix_same_poi;
  PROCEDURE process_att_poi_editon_flag;
  PROCEDURE mul_att_poi_editon_flag(v_audata_id NUMBER);
  procedure unique_add_tenant ;
  procedure unique_mod_tenant ;
  procedure unique_geo_add_tenant;
  PROCEDURE mul_add_tenant(v_audata_id NUMBER);
  PROCEDURE mul_mod_tenant(v_audata_id NUMBER);
  procedure mul_reset_poi_building(v_audata_id number);
  PROCEDURE mul_att_add_poiname_ext2(v_audata_id NUMBER);
  procedure process_modify_poi_name2 ;
  procedure att_add_poi_name_ext2 ;
  PROCEDURE do_add_name2(v_merge_type VARCHAR2);
  PROCEDURE do_add_name(v_merge_type VARCHAR2);
  PROCEDURE mul_add_poiname2(v_audata_id NUMBER);
  PROCEDURE mul_add_poiname(v_audata_id NUMBER);
  procedure do_add_single_parent;
  procedure do_add_single_parent_ext;
  procedure mod_single_parent;
  procedure mul_add_single_parent(v_audata_id NUMBER);
  procedure mul_add_single_parent_ext(v_audata_id NUMBER);
  procedure mul_reset_poi_parent(v_audata_id number);
  procedure modifyKind;

  procedure process_mod_poi_name_delclass5;
  procedure process_ext_poi_name_delclass5;
  procedure mul_mod_poi_name_delclass5(v_audata_id number);
  procedure mul_att_mod_poi_label(v_audata_id number);
  procedure mul_att_mod_poi_inner(v_audata_id number);
  procedure process_mod_yucaiji;
  procedure mul_mod_yucaiji(v_audata_id number);
  procedure mul_mod_poi_flag(v_audata_id number);
  procedure mul_mod_verified_mode_flag(v_audata_id number);
  procedure mul_del_poi_flag_poi_level(v_audata_id number);
  procedure mul_add_poi_flag_poi_level(v_audata_id number);
  procedure mul_verified_mode_flag_ext(v_audata_id number);
  procedure mul_att_add_poi_kind_ext(v_audata_id number);
  procedure mul_modifyKind(v_audata_id number);
  procedure att_add_poi_label_ext ;
  procedure att_yucaiji_ext;
  procedure process_mod_verified_flag;
  procedure att_verified_flag_ext;
  procedure mul_mod_verified_flag(v_audata_id number);
  PROCEDURE mul_process_att_geoatt_samepoi(v_audata_id NUMBER,v_merge_type VARCHAR2);
END merge_au_ix_no_his;
/
CREATE OR REPLACE PACKAGE BODY merge_au_ix_no_his IS
--deprecated
  PROCEDURE reset_tmp_ix_poi_name IS
  BEGIN
    execute immediate 'truncate table temp_his_ix_poi_name';
    INSERT INTO temp_his_ix_poi_name
      SELECT * FROM ix_poi_name;
    commit;  
  END;
  --deprecated
  PROCEDURE reset_tmp_ix_poi IS
  BEGIN
    --Ϊ�˼�¼�仯ǰ��������ʱ����POI   
    execute immediate 'truncate table temp_his_ix_poi';
    INSERT INTO temp_his_ix_poi
      SELECT * FROM ix_poi;
     commit;  
  END;

  PROCEDURE reset_tmp_ix_poi_ext(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    --Ϊ�˼�¼�仯ǰ��������ʱ����POI
    execute immediate 'truncate table temp_his_ix_poi_ext';
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           't');
                                                           navi_log.LOG_INFO('reset_tmp_ix_poi_ext', 'INSERT INTO TEMP_HIS_IX_POI_EXT
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
                       WHERE t.pid = tmp.pid))');
    --����ix_poi�к�au_ix_poi ����״̬pid��ͬ������ :û�б������ҵ�������    
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
   commit;                    
  END;
  PROCEDURE reset_temp_mg_table IS
  BEGIN  
    execute immediate 'truncate table temp_ix_poi_parent_mg';
    execute immediate 'truncate table temp_ix_poi_name_mg';   
  END;
  --deprecated
  PROCEDURE reset_tmp_ix_parent IS
  BEGIN 
    execute immediate 'truncate table temp_his_ix_poi_parent';  
    INSERT INTO temp_his_ix_poi_parent
      SELECT * FROM ix_poi_parent;
    commit;  
  END;
  --deprecated
  procedure reset_tmp_ix_buiding is
    begin
      execute immediate 'truncate table temp_his_ix_poi_building';        
    INSERT INTO temp_his_ix_poi_building
      SELECT * FROM ix_poi_building;
    commit;  
    end;
  PROCEDURE gather_table_stats(v_schemaname VARCHAR2) IS
  BEGIN
    FOR rec IN (SELECT table_name
                  FROM user_tables
                 WHERE table_name LIKE 'IX_POI%' OR table_name LIKE 'AU_IX_POI%') LOOP
      dbms_stats.gather_table_stats(v_schemaname, rec.table_name);
    END LOOP;
  
    dbms_stats.gather_table_stats(v_schemaname,'TEMP_HIS_IX_POI_EXT');
    dbms_stats.gather_table_stats(v_schemaname,'TEMP_AU_IX_POI_MUL_TASK');
    --dbms_stats.gather_table_stats(v_schemaname,'TEMP_HIS_IX_POI_BUILDING');   
    --dbms_stats.gather_table_stats(v_schemaname,'TEMP_HIS_IX_POI');  
    
    --dbms_stats.gather_table_stats(v_schemaname,'TEMP_HIS_IX_POI_PARENT'); 
    dbms_stats.gather_table_stats(v_schemaname,'TEMP_AU_IX_POI_GRP'); 
    --dbms_stats.gather_table_stats(v_schemaname,'TEMP_HIS_IX_POI_NAME');     
    
   
  END;
  PROCEDURE pre_process_poi(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           't');
    --�ں�ʱ�����һ��POI�������ҵ�����޸ģ������������޸Ļ��޸ġ�ɾ��������ô�����Mark���������û�ͨ��NAVIMAP�����������ں�
    --temp_au_ix_poi_mul_task ���ڴ�ű������ҵ�����޸ĵ�POI��PID   
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_ix_poi_mul_task
      SELECT pid
        FROM (SELECT t.pid, COUNT(1)
                FROM au_ix_poi t
               WHERE ' || v_oprstatus_clause ||
                      '               
               GROUP BY t.pid
               HAVING COUNT(1) > 1) rs';
    reset_tmp_ix_poi_ext(v_merge_type);
    --���һ��poi�������ҵ�������
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'au');
    EXECUTE IMMEDIATE '
    INSERT INTO temp_au_ix_poi_grp
      SELECT audata_id, pid, state
        FROM (
             --����󣬵�һ������a���Ұ���delete�������������һ��delete����֮�������(Ҳ���������һ��delete)���뵽grp��
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
               --������delete�ķ�������
               SELECT audata_id, pid, state
                 FROM au_ix_poi au
                WHERE '||v_oprstatus_clause||' and au.pid IN (SELECT pid FROM temp_au_ix_poi_mul_task)
                  AND NOT EXISTS (SELECT 1
                         FROM au_ix_poi tmp
                        WHERE tmp.pid = au.pid
                          AND tmp.state = 1))
  ';
  --temp_au_mul_del_ix_poi�洢������ʱ���һ��������ɾ����poi
  IF (v_merge_type='att' OR v_merge_type='geoatt') THEN
  EXECUTE IMMEDIATE '
  insert into temp_au_mul_del_ix_poi
      select pid
         from (with rs as (SELECT au.audata_id,
                             au.pid,
                             au.field_task_id,
                             au.state,
                             row_number() over(PARTITION BY au.pid ORDER BY au.imp_date desc, au.audata_id desc) AS rn
                        FROM au_ix_poi au
                       where '||v_oprstatus_clause||' and au.pid in
                             (SELECT pid FROM temp_au_ix_poi_mul_task))
           select pid
             from rs
            WHERE rs.rn = 1
              and rs.state = 1)';
    END IF;
    --Ϊ�˼�¼�仯ǰ��������ʱ����POI
   --reset_tmp_ix_poi();
    --Ϊ�����ɱ仯ǰ��������ʱ����ix_poi_name
    --reset_tmp_ix_poi_name();
    reset_tmp_ix_parent();
    --reset_tmp_ix_buiding(); 
  
   
  commit;
 
  END;
  /*�¸�Ҳ��Ҫ���ӽ�ȥ*/
  procedure do_add_single_parent is
    v_pid_count NUMBER;
    begin
      execute immediate 'truncate table temp_ix_poi_parent_mg';
      insert into temp_ix_poi_parent_mg
      select p.group_id,
             p.PARENT_POI_PID,
             p.TENANT_FLAG,
             null as MEMO,
             0 AS U_RECORD,
             NULL AS U_FIELDS
        FROM au_ix_poi_parent p, au_ix_poi au
       WHERE au.state = 3
         AND au.att_oprstatus in(0,1)
         AND p.audata_id = au.audata_id
         AND NOT EXISTS (SELECT 1
                FROM  temp_his_ix_poi_ext ext
               WHERE au.pid = ext.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE au.pid = mul.pid)
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id);
        INSERT INTO ix_poi_parent
           SELECT * FROM temp_ix_poi_parent_mg;         
     commit; 
  end;
  procedure mul_add_single_parent(v_audata_id number) is
    begin      
      --execute immediate 'truncate table temp_ix_poi_parent_mg'; 
      mul_reset_poi_parent(v_audata_id);   
      insert into ix_poi_parent
      select p.group_id,
             p.PARENT_POI_PID,
             p.TENANT_FLAG,
             null as MEMO,
             0 AS U_RECORD,
             NULL AS U_FIELDS
        FROM au_ix_poi_parent p, au_ix_poi au
       WHERE au.audata_id=v_audata_id
         AND au.att_oprstatus in( 0,1)
         AND p.audata_id = au.audata_id
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id);
       /* INSERT INTO ix_poi_parent
           SELECT * FROM temp_ix_poi_parent_mg; */
     commit;       
    end;
  procedure do_add_single_parent_ext is
    v_pid_count NUMBER;
    begin
      --execute immediate 'truncate table temp_ix_poi_parent_mg';
      merge into ix_poi_parent ipp
      using (select p.group_id,
             p.TENANT_FLAG
        FROM au_ix_poi_parent p, au_ix_poi au
       WHERE au.state = 3
         AND au.att_oprstatus in( 0,1)
         AND p.audata_id = au.audata_id
         AND EXISTS (SELECT 1
                FROM  temp_his_ix_poi_ext ext
               WHERE au.pid = ext.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE au.pid = mul.pid)
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id))  aurs
      on  (ipp.group_id=aurs.group_id)
      when matched then update  set ipp.TENANT_FLAG=aurs.TENANT_FLAG;
      insert into ix_poi_parent
      select p.group_id,
             p.PARENT_POI_PID,
             p.TENANT_FLAG,
             p.MEMO,
             0 as U_RECORD,
             null as U_FIELDS
        FROM au_ix_poi_parent p, au_ix_poi au
       WHERE au.state = 3
         AND au.att_oprstatus in( 0,1)
         AND p.audata_id = au.audata_id
         AND EXISTS (SELECT 1
                FROM  temp_his_ix_poi_ext ext
               WHERE au.pid = ext.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE au.pid = mul.pid)
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id) 
         and not exists(select 1 from temp_his_ix_poi_parent tmp where tmp.parent_poi_pid=p.parent_poi_pid)        
                 ;
        /*INSERT INTO ix_poi_parent
           SELECT * FROM temp_ix_poi_parent_mg;  */       
      commit;
  end;
  procedure mul_add_single_parent_ext(v_audata_id number) is
    begin      
     -- execute immediate 'truncate table temp_ix_poi_parent_mg';
      mul_reset_poi_parent(v_audata_id);  
      merge into ix_poi_parent ipp
      using (select p.group_id,
             p.TENANT_FLAG
        FROM au_ix_poi_parent p
       WHERE p.audata_id=v_audata_id
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id))  aurs
      on  (ipp.group_id=aurs.group_id)
      when matched then update  set ipp.TENANT_FLAG=aurs.TENANT_FLAG;
      insert into ix_poi_parent
      select p.group_id,
             p.PARENT_POI_PID,
             p.TENANT_FLAG,
             p.MEMO,
             0 as U_RECORD,
             null as U_FIELDS
        FROM au_ix_poi_parent p
       WHERE  p.audata_id=v_audata_id          
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id) 
         and not exists(select 1 from ix_poi_parent tmp where tmp.parent_poi_pid=p.parent_poi_pid)        
                 ;
       /* INSERT INTO ix_poi_parent
           SELECT * FROM temp_ix_poi_parent_mg; */
       commit;    
    end;
  procedure mod_single_parent is
    begin
     
      execute immediate 'truncate table temp_ix_poi_parent_mg';
      merge into ix_poi_parent ipp
      using (select p.group_id,
             p.TENANT_FLAG
        FROM au_ix_poi_parent p, au_ix_poi au
       WHERE au.state = 2
         AND au.att_oprstatus in( 0,1)
         AND p.audata_id = au.audata_id        
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE au.pid = mul.pid)
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id))  aurs
      on  (ipp.group_id=aurs.group_id)
      when matched then update  set ipp.TENANT_FLAG=aurs.TENANT_FLAG;
      insert into temp_ix_poi_parent_mg
      select p.group_id,
             p.PARENT_POI_PID,
             p.TENANT_FLAG,
             p.MEMO,
             0 as U_RECORD,
             null as U_FIELDS
        FROM au_ix_poi_parent p, au_ix_poi au
       WHERE au.state =2
         AND au.att_oprstatus in( 0,1)
         AND p.audata_id = au.audata_id         
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE au.pid = mul.pid)
         AND NOT EXISTS
       (SELECT 1
                FROM au_ix_poi_children c
               WHERE p.group_id = c.group_id
                 AND p.field_task_id = c.field_task_id) 
         and not exists(select 1 from temp_his_ix_poi_parent tmp where tmp.parent_poi_pid=p.parent_poi_pid)        
                 ;
        INSERT INTO ix_poi_parent
           SELECT * FROM temp_ix_poi_parent_mg; 
        commit;   
    end;
 
  /*�����ںϣ����ӹ�ϵ
  v_not: NOT����ĸ����*/
  PROCEDURE do_add_relation(v_param VARCHAR2) IS
    v_not       VARCHAR2(3);
  BEGIN   
     execute immediate 'truncate table temp_ix_poi_parent_mg';
     reset_tmp_ix_parent;
    --���β��ݸ��ӹ�ϵ                  
    --1.���������ӣ��������ڵĸ����Ӷ�copy����ҵ��      
    --�����Բ���ʱ�����ںϸ��ӹ�ϵ��
    --��Ҫ����pid
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not Ҫ��ΪNOT ��Ҫ��Ϊ''
      --��Ҫ���ӵ�ix_poi_parent�ŵ���ʱ���Ա���������
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_parent_mg
      SELECT  GROUP_ID,
             PARENT_POI_PID,
             TENANT_FLAG,
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
                               WHERE tmp.pid = L.pid) --�ж����ҵ����Ĳ��ں�
                      ) v
               WHERE p.group_id = v.group_id  and p.FIELD_TASK_ID=v.FIELD_TASK_ID) AND NOT EXISTS (SELECT 1
                                FROM temp_his_ix_poi_parent ip
                               WHERE ip.parent_poi_pid = p.parent_poi_pid) ';
      INSERT INTO ix_poi_parent
        SELECT * FROM temp_ix_poi_parent_mg;
  
    --����ӱ�
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
       WHERE RS.RN = 1 ' --�ж����ҵ����Ĳ��ں�
    ;
    commit;
  
  END;
  PROCEDURE exe_insert_address(v_oprstatus_clause VARCHAR2,
                               v_not              VARCHAR2) IS
  BEGIN
    execute immediate 'truncate table temp_ix_poi_address_mg';
      --���IX_POI_ADDRESS;
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_address_mg
      SELECT c.NAME_ID,
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
         AND p.audata_id = c.audata_id         
         AND ' || v_not || ' EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid) --�ж����ҵ����Ĳ��ں�
      ';
      INSERT INTO ix_poi_address
        SELECT * FROM temp_ix_poi_address_mg;
    commit;
  END;
  PROCEDURE do_add_address(v_merge_type VARCHAR2, v_param VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_not              VARCHAR2(3);
  BEGIN
    --��Ҫ����pid
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not Ҫ��ΪNOT ��Ҫ��Ϊ''
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'p');
    exe_insert_address(v_oprstatus_clause, v_not);
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF (SQLCODE = -20999) THEN
        exe_insert_address(v_oprstatus_clause, v_not);
      ELSE
        RAISE;
      END IF;
    
  END;
  PROCEDURE copy_name_class1_data IS
    v_pid_count NUMBER := 0;
  BEGIN
    MERGE INTO ix_poi_name ipn
    USING (SELECT *
             FROM temp_ix_poi_name_mg mg
            WHERE mg.lang_code IN ('CHI', 'CHT')             
              AND mg.NAME_TYPE = 2
              AND EXISTS (SELECT 1
                     FROM ix_poi_name t
                    WHERE t.poi_pid = mg.poi_pid
                      AND t.lang_code IN ('CHI', 'CHT')
                      AND t.NAME_TYPE = 1)) aurs
    ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code IN('CHI', 'CHT') AND ipn.NAME_TYPE = 1)
    WHEN MATCHED THEN
      UPDATE
         SET ipn.name = aurs.name, ipn.name_phonetic = aurs.name_phonetic;
    EXECUTE IMMEDIATE 'SELECT COUNT(1)
  FROM temp_ix_poi_name_mg ipn
 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
   AND ipn.NAME_TYPE = 2
   AND NOT EXISTS (SELECT 1
          FROM ix_poi_name t
         WHERE t.poi_pid = ipn.poi_pid
           AND t.lang_code IN (''CHI'', ''CHT'')
           AND t.NAME_TYPE = 1)
         '
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_pid_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_name_mg1 WITH rs AS
      (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM temp_ix_poi_name_mg
        GROUP BY poi_pid)
      SELECT pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
             ipn.poi_pid,
             nvl(rs.name_groupid, 1) AS name_groupid, 
             ipn.name_class,
             1 as NAME_TYPE ,
             ipn.lang_code,
             ipn.name,
             ipn.name_phonetic,
             ipn.keywords,
             ipn.nidb_pid,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
         FROM temp_ix_poi_name_mg ipn,rs
 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
   AND ipn.name_class = 2
   AND ipn.POI_PID = rs.poi_pid(+)
   AND NOT EXISTS (SELECT 1
          FROM ix_poi_name t
         WHERE t.poi_pid = ipn.poi_pid
           AND t.lang_code IN (''CHI'', ''CHT'')
           AND t.name_class = 1)
         ';
    END IF;
    INSERT INTO temp_ix_poi_name_mg
      SELECT * FROM temp_ix_poi_name_mg1;
    DELETE FROM temp_ix_poi_name_mg1;
  commit;
  END;
   PROCEDURE do_add_name(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
    v_not              VARCHAR2(3):='NOT';
  BEGIN
     execute immediate 'truncate table temp_ix_poi_name_mg';   
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
     commit; 
    end ;
  PROCEDURE do_add_name2(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_pid_count        NUMBER;
    v_not              VARCHAR2(3):='NOT';
  BEGIN
    execute immediate 'truncate table temp_ix_poi_name_mg';      
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                          'p');
   --����һ��name_type=1��
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
      commit;
  END;
  
  PROCEDURE do_add_restaurant(v_merge_type VARCHAR2, v_param VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
    v_not              VARCHAR2(3);
  BEGIN   
    execute immediate 'truncate table temp_ix_poi_restaurant_mg';  
    --��Ҫ����pid
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not Ҫ��ΪNOT ��Ҫ��Ϊ''
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'auip');

      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_restaurant_mg(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
      SELECT RESTAURANT_ID,
             POI_PID,
             FOOD_TYPE,
             CREDIT_CARD,
             AVG_COST,
             PARKING,
             0             AS TRAVELGUIDE_FLAG,
             0             AS U_RECORD,
             NULL          AS U_FIELDS
        FROM au_ix_poi_restaurant auipr
       WHERE EXISTS
       (SELECT 1
                FROM au_ix_poi auip
               WHERE auip.audata_id = auipr.audata_id
                 AND auip.state = 3
                 AND ' || v_oprstatus_clause || '
                 AND ' || v_not ||
                        ' EXISTS (SELECT 1
                            FROM temp_his_ix_poi_ext ext
                           WHERE ext.pid = auip.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_poi_mul_task tmp
                       WHERE tmp.pid = auip.pid))';
      INSERT INTO ix_poi_restaurant
        SELECT * FROM temp_ix_poi_restaurant_mg;
      DELETE FROM temp_ix_poi_restaurant_mg;
    commit;
  END;
    PROCEDURE do_add_ix_poi_flag(v_merge_type VARCHAR2,
                               v_param      VARCHAR2 := '',
                               v_state number default 3) IS
    v_oprstatus_clause VARCHAR2(100);
    v_not              VARCHAR2(3);
  BEGIN
    v_not := v_param;
    IF ('NOT' != v_not) THEN
      v_not := '';
    END IF; --v_not Ҫ��ΪNOT ��Ҫ��Ϊ''
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'au');
                                                           
   -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000200000,110000210000,110000220000 �ļ�¼����������ڣ��򲻴���
   -- ����ҵ����AU_IX_POI_FLAG���е�POI_LEVEL��Ϣ�����뵽IX_POI_FLAG����
   -- ����AU_IX_POI_FLAG��FLAG_CODE (110000110002)����NOKIA��֤��Ϣ��������ڣ��򲻲��룩
 /* execute immediate '
  DELETE FROM ix_poi_flag t
   WHERE t.flag_code IN (''110000200000'', ''110000210000'', ''110000220000'')
    AND EXISTS (SELECT 1
          FROM au_ix_poi_flag t2
         WHERE t2.poi_pid = t.poi_pid
           AND t2.flag_code IN
               (''110000200000'', ''110000210000'', ''110000220000''))
    AND EXISTS
   (SELECT 1
          FROM au_ix_poi au, au_ix_poi_flag au_f
         WHERE au.state = '||v_state||'
           AND ' || v_oprstatus_clause || '
           AND ' || v_not ||' EXISTS
         (SELECT 1 FROM temp_his_ix_poi_ext ext WHERE ext.pid = au.pid)
           AND NOT EXISTS (SELECT 1
                  FROM temp_au_ix_poi_mul_task tmp
                 WHERE tmp.pid = au.pid)
           AND au.pid = t.poi_pid and au.audata_id = au_f.audata_id )' ;
  
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_FLAG WITH AURS AS
  (SELECT AU.PID, AUF.FLAG_CODE
     FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
    WHERE AU.STATE = '||v_state||'
      AND ' || v_oprstatus_clause || '
      AND AU.AUDATA_ID = AUF.AUDATA_ID
      AND ' || v_not ||
                      ' EXISTS
    (SELECT 1 FROM TEMP_HIS_IX_POI_EXT EXT WHERE EXT.PID = AU.PID)
      AND NOT EXISTS
    (SELECT 1 FROM TEMP_AU_IX_POI_MUL_TASK TMP WHERE TMP.PID = AU.PID))
   SELECT AURS.PID,
         ''110000200000'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000200000''    
  UNION ALL
  SELECT AURS.PID,
         ''110000210000'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000210000''    
  UNION ALL
  SELECT AURS.PID,
         ''110000220000'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000220000''    
  UNION ALL
  SELECT AURS.PID,
         ''110000110002'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000110002''
     AND NOT EXISTS (SELECT 1
            FROM IX_POI_FLAG RS
           WHERE RS.POI_PID = AURS.PID
             AND RS.FLAG_CODE = ''110000110002'') '; */
                                                   
                                                           
                                                           
                                                     
   --:5.���AU_IX_POI_FLAG���д��ڼ�¼��������������Ӱ汾IX_POI_FLAG����:��ɾ����ҵ���Ѿ����ڵ�flag_code��Ȼ����ҵ�����е�flag_code���뵽�Ӱ汾��
  execute immediate '
 DELETE FROM ix_poi_flag t
 WHERE  
   EXISTS
 (SELECT 1
          FROM au_ix_poi au
         WHERE au.state = '||v_state||'
           AND ' || v_oprstatus_clause || '
           AND EXISTS (SELECT 1
          FROM au_ix_poi_flag t2
         WHERE t2.poi_pid = t.poi_pid
           AND t2.flag_code =t.flag_code AND AU.AUDATA_ID=T2.AUDATA_ID)
           AND ' || v_not ||' EXISTS
         (SELECT 1 FROM temp_his_ix_poi_ext ext WHERE ext.pid = au.pid)
           AND NOT EXISTS (SELECT 1
                  FROM temp_au_ix_poi_mul_task tmp
                 WHERE tmp.pid = au.pid)
           AND au.pid = t.poi_pid)
  ' ;

    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_FLAG WITH AURS AS
  (SELECT AU.PID, AUF.FLAG_CODE
     FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
    WHERE AU.STATE = '||v_state||'
      AND ' || v_oprstatus_clause || '
      AND AU.AUDATA_ID = AUF.AUDATA_ID
      AND ' || v_not ||
                      ' EXISTS
    (SELECT 1 FROM TEMP_HIS_IX_POI_EXT EXT WHERE EXT.PID = AU.PID)
      AND NOT EXISTS
    (SELECT 1 FROM TEMP_AU_IX_POI_MUL_TASK TMP WHERE TMP.PID = AU.PID))
   SELECT AURS.PID,
          AURS.FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
  ';
 
  
   -- �ں�ʱ�����AU_IX_POI���У�log�ֶΰ��������ڲ�POI������ô�ںϺ���IX_POI_FLAG�в���һ����¼110000030000��������ڣ��򲻲��룩
  /*EXECUTE IMMEDIATE 'INSERT INTO ix_poi_flag ipf
  SELECT au.pid, ''110000030000'', 0 AS u_record, NULL AS u_fields
    FROM au_ix_poi au
   WHERE au.state = '||v_state||'
     AND ' || v_oprstatus_clause || '
     AND instr(au.log, ''���ڲ�POI'') > 0
     AND ' || v_not ||
                      ' EXISTS (SELECT 1
                            FROM temp_his_ix_poi_ext ext
                           WHERE ext.pid = au.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_poi_mul_task tmp
                       WHERE tmp.pid = au.pid)
     AND NOT EXISTS (SELECT 1
            FROM ix_poi_flag ipf
           WHERE ipf.poi_pid = au.pid
             AND ipf.flag_code = ''110000030000'')';*/


    -- ���LABEL�ֶΰ��������յ�ַ���������ںϺ���IX_POI_FLAG���У�����һ����¼��FLAG_CODE =110030060000���̶���־����ʩ�����ַ��
  EXECUTE IMMEDIATE 'INSERT INTO ix_poi_flag ipf
  SELECT au.pid, ''110030060000'', 0 AS u_record, NULL AS u_fields
    FROM au_ix_poi au
   WHERE au.state = '||v_state||'
     AND ' || v_oprstatus_clause || '
     AND instr(au.label, ''���յ�ַ'') > 0
     AND ' || v_not ||
                      ' EXISTS (SELECT 1
                            FROM temp_his_ix_poi_ext ext
                           WHERE ext.pid = au.pid)
                 AND NOT EXISTS (SELECT 1
                        FROM temp_au_ix_poi_mul_task tmp
                       WHERE tmp.pid = au.pid)
     AND NOT EXISTS (SELECT 1
            FROM ix_poi_flag ipf
           WHERE ipf.poi_pid = au.pid
             AND ipf.flag_code = ''110030060000'')';
commit;
  END;


  PROCEDURE  process_del_poi_flag_poi_level IS
  BEGIN     
   -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000200000,110000210000,110000220000 �ļ�¼����������ڣ��򲻴���
    DELETE FROM ix_poi_flag t
     WHERE t.flag_code IN ('110000200000', '110000210000','110000210001','110000210002','110000210003','110000210004', '110000220000')
       AND EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.poilevel_flag = 1
               and t.poi_pid = l.pid);
	       commit;
  END;


  PROCEDURE  process_add_poi_flag_poi_level IS
  BEGIN     
     -- ����ҵ����AU_IX_POI_FLAG���е�POI_LEVEL��Ϣ�����뵽IX_POI_FLAG����
     INSERT INTO IX_POI_FLAG
       SELECT AU.PID        as poi_pid,
              AUF.FLAG_CODE as FLAG_CODE,
              0             AS U_RECORD,
              NULL          AS U_FIELDS
         FROM temp_au_poi_modify_log AU, AU_IX_POI_FLAG AUF
        WHERE AU.poilevel_flag = 1
          and au.pid = auf.poi_pid
          and AU.AUDATA_ID = AUF.AUDATA_ID
          and not exists
        (select 1
                 from ix_poi_flag ipf
                where ipf.poi_pid = au.pid
                  and ipf.flag_code = auf.flag_code)
          and auf.flag_code in ('110000200000', '110000210000', '110000220000');
	  commit;
	  
  END;

  PROCEDURE  process_mod_verified_mode_flag IS
  BEGIN     
    --AU_IX_POI���У�STATE= 2��,��LOG����������֤ģʽ������AU_IX_POI_FLAG����FLAG_CODE��ֵ��110000300000���ֳ���֤����
    --����POI_PID��IX_POI_FLAG����FLAG_CODE��ֵ�ǡ�110000330000���Ľ���ɾ����
    DELETE FROM ix_poi_flag f
     WHERE f.flag_code = '110000330000'
       AND EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l, au_ix_poi_flag af
             WHERE l.audata_id = af.audata_id
               AND l.pid = af.poi_pid
               AND f.poi_pid = l.pid
               AND l.verified_mode_flag = 1
               AND af.flag_code = '110000300000');
    --��AU_IX_POI_FLAG����FLAG_CODE��ֵ��110000300000���ֳ���֤�����뵽IX_POI_FLAG���С�
    INSERT INTO ix_poi_flag
      SELECT l.pid        AS poi_pid,
             af.flag_code AS FLAG_CODE,
             0            AS U_RECORD,
             NULL         AS U_FIELDS
        FROM temp_au_poi_modify_log l, au_ix_poi_flag af
       WHERE l.audata_id = af.audata_id
         AND l.pid = af.poi_pid
         AND l.verified_mode_flag = 1
         AND af.flag_code = '110000300000'
         AND NOT EXISTS (SELECT 1 FROM ix_poi_flag ipf where l.pid = ipf.poi_pid and ipf.flag_code = '110000300000');
         
     commit;
  END;

  PROCEDURE process_mod_poi_flag IS
  BEGIN
   /*   delete FROM ix_poi_flag t
    WHERE EXISTS
    (SELECT 1
             FROM au_ix_poi au, ix_poi ipi
            WHERE au.state = 2
              AND au.att_oprstatus in( 0,1)
              and au.pid = ipi.pid
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_poi_mul_task tmp
                    WHERE tmp.pid = au.pid)
              AND au.pid = t.poi_pid
              and exists (SELECT 1
                     FROM au_ix_poi_flag t2
                    WHERE t2.audata_id = au.audata_id
                      and au.pid = t2.poi_pid
                      AND t2.flag_code = t.flag_code
                      and t2.flag_code = 110000110002));
   
   
   INSERT INTO IX_POI_FLAG
      WITH aurs AS  
     (SELECT au.pid, auf.flag_code 
      FROM au_ix_poi au, au_ix_poi_flag auf, ix_poi ipi 
      WHERE au.state = 2 
      AND au.att_oprstatus in( 0,1) 
       AND au.audata_id = auf.audata_id 
       and auf.flag_code = 110000110002 
      and au.pid = auf.poi_pid 
       and ipi.pid = auf.poi_pid 
       AND NOT EXISTS (SELECT 1 
            FROM temp_au_ix_poi_mul_task tmp 
           WHERE tmp.pid = au.pid)) 
   SELECT aurs.pid as poi_pid, aurs.flag_code, 0 AS u_record, NULL AS u_fields  FROM aurs; */
         
   -- ����AU_IX_POI_FLAG��FLAG_CODE (110000110002)����NOKIA��֤��Ϣ��������ڣ��򲻲��룩
     INSERT INTO IX_POI_FLAG
     WITH aurs AS
      (SELECT au.pid, auf.flag_code
         FROM au_ix_poi au, au_ix_poi_flag auf, ix_poi ipi
        WHERE au.state = 2
          AND au.att_oprstatus in( 0,1)
          AND au.audata_id = auf.audata_id
          and auf.flag_code = '110000110002'
          and au.pid = auf.poi_pid
          and ipi.pid = auf.poi_pid
          AND NOT EXISTS (SELECT 1
                 FROM temp_au_ix_poi_mul_task tmp
                WHERE tmp.pid = au.pid)
          and not exists (select *
                 from IX_POI_FLAG a
                where a.poi_pid = au.pid
                  and a.flag_code = AUF.Flag_Code)
       
       )
     SELECT aurs.pid       as poi_pid,
            aurs.flag_code,
            0              AS u_record,
            NULL           AS u_fields
       FROM aurs;
       commit;

  END;

   
  PROCEDURE modifyKind IS
  BEGIN
   
   -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000100000,110000120000,110000140000,110000150000,110000170000,110000220000 �ļ�¼��
   --ɾ��POI LEVEl ��Ϣ '110000200000', '110000210000','110000210001','110000210002','110000210003','110000210004','110000220000'
    DELETE FROM ix_poi_flag t
     WHERE t.flag_code IN ('110000100000',
                           '110000120000',
                           '110000140000',
                           '110000150000',
                           '110000170000',
                           '110000250000',
                           '110000200000', '110000210000','110000210001','110000210002','110000210003','110000210004','110000220000')
       AND EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.kind_flag = 1
               and t.poi_pid = l.pid);
    -- ����ҵ����AU_IX_POI_FLAG���е���Ϣ�����뵽IX_POI_FLAG����
    INSERT INTO IX_POI_FLAG
      SELECT AU.PID        as poi_pid,
             AUF.FLAG_CODE as FLAG_CODE,
             0             AS U_RECORD,
             NULL          AS U_FIELDS
        FROM temp_au_poi_modify_log AU, AU_IX_POI_FLAG AUF
       WHERE AU.kind_flag = 1
         and au.pid = auf.poi_pid
         and AU.AUDATA_ID = AUF.AUDATA_ID
         and auf.flag_code in ('110000100000',
                               '110000120000',
                               '110000140000',
                               '110000150000',
                               '110000170000',
                               '110000250000');
  
    -- ͨ��POI_PID��ɾ��IX_POI_HOTEL���еļ�¼��
    DELETE FROM ix_poi_hotel t
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.kind_flag = 1
               and t.poi_pid = l.pid);
    -- ����ҵ����AU_IX_POI_HOTEL���е���Ϣ�����뵽IX_POI_HOTEL����
    INSERT INTO IX_POI_HOTEL
      (HOTEL_ID,
       POI_PID,
       CREDIT_CARD,
       RATING,
       CHECKIN_TIME,
       CHECKOUT_TIME,
       ROOM_COUNT,
       ROOM_TYPE,
       ROOM_PRICE,
       BREAKFAST,
       SERVICE,
       PARKING,
       LONG_DESCRIPTION,
       LONG_DESCRIP_ENG,
       OPEN_HOUR,
       OPEN_HOUR_ENG,
       TELEPHONE,
       ADDRESS,
       CITY,
       PHOTO_NAME,
       U_RECORD,
       U_FIELDS)
      SELECT hotel_id,
             poi_pid,
             credit_card,
             rating,
             checkin_time,
             checkout_time,
             room_count,
             room_type,
             room_price,
             breakfast,
             service,
             parking,
             long_description,
             long_descrip_eng,
             open_hour,
             open_hour_eng,
             telephone,
             address,
             city,
             photo_name,
             0                AS u_record,
             NULL             AS u_fields
        FROM temp_au_poi_modify_log l, au_ix_poi_hotel h
       WHERE l.kind_flag = 1
         and l.pid = h.poi_pid
         and l.audata_id = h.audata_id;
commit;
  END;



  --�⻧��Ϣ�ں�
  PROCEDURE unique_add_tenant IS
  BEGIN   
    --ix_poi_building
    DELETE FROM ix_poi_building p
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi_building au, au_ix_poi auip
             WHERE au.audata_id = auip.audata_id
               AND auip.att_oprstatus in( 0,1)
               AND auip.state = 3
               AND p.poi_pid = au.poi_pid
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task mul
                     WHERE mul.pid = auip.pid));
    INSERT INTO ix_poi_building
      SELECT poi_pid,
             floor_used,
             floor_empty,
             memo,
             0           AS u_record,
             NULL        AS u_fields
        FROM au_ix_poi_building au, au_ix_poi auip
       WHERE au.audata_id = auip.audata_id
         AND auip.att_oprstatus in( 0,1)
         AND auip.state = 3
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE mul.pid = auip.pid);   
               commit;           
  END;
  procedure unique_geo_add_tenant is
    begin
      --ix_poi_building
    DELETE FROM ix_poi_building p
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi_building au, au_ix_poi auip
             WHERE au.audata_id = auip.audata_id
               AND auip.geo_oprstatus in( 0,1)
               AND auip.state = 3
               AND p.poi_pid = au.poi_pid
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task mul
                     WHERE mul.pid = auip.pid)
               and not exists (select 1 from temp_his_ix_poi_ext ext where ext.pid=p.poi_pid)
                     );
    INSERT INTO ix_poi_building
      SELECT poi_pid,
             floor_used,
             floor_empty,
             memo,
             0           AS u_record,
             NULL        AS u_fields
        FROM au_ix_poi_building au, au_ix_poi auip
       WHERE au.audata_id = auip.audata_id
         AND auip.geo_oprstatus IN( 0,1)
         AND auip.state = 3
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE mul.pid = auip.pid)
         and not exists (select 1 from temp_his_ix_poi_ext ext where ext.pid=auip.pid)      
               ;
               commit;
    end;
  PROCEDURE unique_mod_tenant IS
  BEGIN
    --�޸ģ���������£������������
    --���������log�����޸�
    merge into ix_poi_parent p
    using(select group_id,parent_poi_pid,au.tenant_flag as tenant_flag from au_ix_poi_parent au, temp_au_poi_modify_log tmp
       WHERE au.audata_id=tmp.audata_id
         and tmp.tenant_flag=1
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE mul.pid =  tmp.pid)
         )rs 
    on(p.parent_poi_pid=rs.parent_poi_pid)
    when matched then update set p.tenant_flag=rs.tenant_flag ;
    merge into ix_poi_building p
    using (select au.poi_pid , au.floor_used,
             au.floor_empty ,tmp.floor_used as floor_used_flag,tmp.floor_empty as floor_empty_flag from   au_ix_poi_building au, temp_au_poi_modify_log tmp
       WHERE au.audata_id=tmp.audata_id
         and(tmp.floor_used=1 or tmp.floor_empty = 1)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE mul.pid =  tmp.pid)
         )rs 
     on (p.poi_pid=rs.poi_pid)
     when matched then update set 
       p.floor_used=decode(rs.floor_used_flag,1,rs.floor_used,p.floor_used),
       p.floor_empty=decode(rs.floor_empty_flag,1,rs.floor_empty,p.floor_empty)
     ; 
         
    --�����ڵĲ���
     INSERT INTO ix_poi_parent
      SELECT group_id,
             parent_poi_pid,
             au.tenant_flag,
             memo,
             0              AS u_record,
             NULL           AS u_fields
        FROM au_ix_poi_parent au, temp_au_poi_modify_log tmp
       WHERE au.audata_id=tmp.audata_id
         and tmp.tenant_flag=1
         and not exists(select 1 from ix_poi_parent p where p.parent_poi_pid=au.parent_poi_pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE mul.pid = tmp.pid);
     INSERT INTO ix_poi_building
      SELECT poi_pid,
             au.floor_used,
             au.floor_empty,
             memo,
             0           AS u_record,
             NULL        AS u_fields
        FROM au_ix_poi_building au,temp_au_poi_modify_log tmp
       WHERE au.audata_id=tmp.audata_Id
         and (tmp.floor_used=1 or tmp.floor_empty = 1)
         and not exists(select 1 from ix_poi_building p where p.poi_pid=au.poi_pid)         
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task mul
               WHERE mul.pid = tmp.pid);  
      
       commit;  
  END;
 
  procedure mul_add_tenant(v_audata_id NUMBER) is
    begin   
    execute immediate'
    DELETE FROM ix_poi_building p
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi_building au, au_ix_poi auip
             WHERE au.audata_id = auip.audata_id
               AND auip.att_oprstatus in( 0,1)
               AND auip.state = 3
               AND p.poi_pid = au.poi_pid
               and auip.audata_id=:v_audata_Id
               )' using v_audata_id;
    execute immediate '               
    INSERT INTO ix_poi_building
      SELECT poi_pid,
             floor_used,
             floor_empty,
             memo,
             0           AS u_record,
             NULL        AS u_fields
        FROM au_ix_poi_building au, au_ix_poi auip
       WHERE au.audata_id = auip.audata_id
         AND auip.att_oprstatus in( 0,1)
         AND auip.state = 3
         and auip.audata_id=:v_audata_Id' using v_audata_id;
         commit;
    end;
  PROCEDURE mul_mod_tenant(v_audata_id NUMBER) IS
  BEGIN
    --�޸ģ���������£������������
    --���������log�����޸�
    execute immediate '
    merge into ix_poi_parent p
    using(select group_id,parent_poi_pid,au.tenant_flag as tenant_flag from au_ix_poi_parent au, au_ix_poi tmp
       WHERE au.audata_id=tmp.audata_id
         and instr(tmp.log, ''��TENANT_FLAG'') > 0
         and au.audata_id=:v_audata_id
         )rs 
    on(p.group_id=rs.group_id)
    when matched then update set p.tenant_flag=rs.tenant_flag'  using v_audata_id;
     execute immediate '
    merge into ix_poi_building p
    using (select au.poi_pid , au.floor_used,
             au.floor_empty ,nvl(instr(tmp.log, ''��FLOOR_USED''),0) as floor_used_flag ,nvl(instr(tmp.log, ''��FLOOR_EMPTY''),0) as floor_empty_flag from   au_ix_poi_building au, au_ix_poi tmp
       WHERE au.audata_id=tmp.audata_id
         and(instr(tmp.log, ''��FLOOR_USED'') > 0 or instr(tmp.log, ''��FLOOR_EMPTY'') > 0)
         and au.audata_id=:v_audata_id
         )rs 
     on (p.poi_pid=rs.poi_pid)
     when matched then update set 
      p.floor_used=decode(rs.floor_used_flag,0,p.floor_used,rs.floor_used),
       p.floor_empty=decode(rs.floor_empty_flag,0,p.floor_empty,rs.floor_empty)
     'using v_audata_id; 
         
    --�����ڵĲ���
     execute immediate '
     INSERT INTO ix_poi_parent
      SELECT group_id,
             parent_poi_pid,
             au.tenant_flag,
             memo,
             0              AS u_record,
             NULL           AS u_fields
        FROM au_ix_poi_parent au, au_ix_poi tmp
       WHERE au.audata_id=tmp.audata_id
         and instr(tmp.log, ''��TENANT_FLAG'') > 0
         and not exists(select 1 from ix_poi_parent p where p.parent_poi_pid=au.parent_poi_pid)
         and au.audata_id=:v_audata_id'using  v_audata_id;
      execute immediate '    
     INSERT INTO ix_poi_building
      SELECT poi_pid,
             au.floor_used,
             au.floor_empty,
             memo,
             0           AS u_record,
             NULL        AS u_fields
        FROM au_ix_poi_building au,au_ix_poi tmp
       WHERE au.audata_id=tmp.audata_Id
         and(instr(tmp.log, ''��FLOOR_USED'') > 0 or instr(tmp.log, ''��FLOOR_EMPTY'') > 0)
         and not exists(select 1 from ix_poi_building p where p.poi_pid=au.poi_pid)         
         and au.audata_id=:v_audata_id' using v_audata_id ;
         commit;
        
  END; 
   

  --fixme:��Ҫ����AU_IX_POI_NAME_FLAG �����
  PROCEDURE do_add(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'p');
    --�������;    
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
          open_24h     ,
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
          VERIFIED_FLAG ,
          NULL,--COLLECT_TIME
          9,--GEO_ADJUST_FLAG
          9,--FULL_ATTR_FLAG
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
               WHERE tmp.pid = p.pid) --�ж����ҵ����Ĳ��ں� 
      ';
    do_add_address(v_merge_type, 'NOT');
  
    --dbms_output.put_line('BEGIN IX_POI_CONTACT ');
    --���IX_POI_CONTACT;
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
         AND p.audata_id = c.audata_id
         AND NOT EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext ext
                 WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid) --�ж����ҵ����Ĳ��ں�
      ';
    --do_add_name(v_merge_type, 'NOT');
  
    --dbms_output.put_line('BEGIN ix_poi_parent ');
    do_add_restaurant(v_merge_type, 'NOT');
    --ix_poi_restaurant
  
    IF (merge_utils.merge_type_geo != v_merge_type) THEN
      do_add_relation('NOT');
    END IF;
    do_add_ix_poi_flag(v_merge_type, 'NOT');
    -- �������IX_POI��VERIFIED_FLAG�ֶ�ֵΪ��2�����ߡ�3����POI����Flag_code��IX_POI_FLAG������������������ʾ����110000340000����������Ҫ�Ѹñ�ʾɾ���� 
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_FLAG c1
     WHERE NOT EXISTS
     (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
       AND NOT EXISTS
     (SELECT 1 FROM temp_au_ix_poi_mul_task tmp WHERE tmp.pid = c1.poi_pid)
       AND EXISTS (SELECT 1
              FROM au_ix_poi au, ix_poi i
             WHERE au.pid = c1.poi_pid
               and au.pid = i.pid
               and au.state = 3
               and au.att_oprstatus in (0,1)
               and i.verified_flag in (2, 3))
       AND c1.FLAG_CODE = ''110000340000''';
    --��Ҫ����IX_POIO_NAME_FLAG,IX_POI_HOTEL,IX_POI_BUILDING,IX_POI_PHOTO
    execute immediate '
    INSERT INTO ix_poi_name_flag
      SELECT auf.name_id, auf.flag_code, 0 AS u_record, NULL AS u_fields
        FROM au_ix_poi p, au_ix_poi_name aun, au_ix_poi_name_flag auf
       WHERE p.state = 3
         AND ' || v_oprstatus_clause || '
         AND p.audata_id = aun.audata_id
         AND aun.auname_id = auf.auname_id
         AND NOT EXISTS
       (SELECT 1 FROM temp_his_ix_poi_ext ext WHERE ext.pid = p.pid)
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = p.pid)';
    --��Ҫ����AU_IX_POI_HOTEL
    execute immediate '
     INSERT INTO ix_poi_hotel(HOTEL_ID,POI_PID,CREDIT_CARD,RATING,CHECKIN_TIME,CHECKOUT_TIME,ROOM_COUNT,ROOM_TYPE,ROOM_PRICE,BREAKFAST,SERVICE,PARKING,LONG_DESCRIPTION,LONG_DESCRIP_ENG,OPEN_HOUR,OPEN_HOUR_ENG,TELEPHONE,ADDRESS,CITY,PHOTO_NAME,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
      SELECT hotel_id,
             poi_pid,
             credit_card,
             rating,
             checkin_time,
             checkout_time,
             room_count,
             room_type,
             room_price,
             breakfast,
             service,
             parking,
			 long_description,
			 long_descrip_eng,
			 open_hour,
			 open_hour_eng,
			 telephone,
			 address,
			 city,
			 photo_name,
             0             AS TRAVELGUIDE_FLAG,
             0             AS u_record,
             NULL          AS u_fields
        FROM au_ix_poi p, au_ix_poi_hotel auh
       WHERE p.state = 3
         AND ' || v_oprstatus_clause || '
         AND p.audata_id = auh.audata_id
         AND NOT EXISTS
       (SELECT 1 FROM temp_his_ix_poi_ext ext WHERE ext.pid = p.pid)
         AND NOT EXISTS
       (SELECT 1 FROM temp_au_ix_poi_mul_task tmp WHERE tmp.pid = p.pid)
       ';

    --��Ҫ����AU_IX_POI_PHOTO
    execute immediate '
      INSERT INTO ix_poi_photo
  SELECT poi_pid, photo_id, status, memo, 0 as u_record, null as u_fields
    FROM au_ix_poi p, au_ix_poi_photo aup
   WHERE p.state = 3    
    AND ' || v_oprstatus_clause || '    
     AND p.audata_id = aup.audata_id
     AND NOT EXISTS
   (SELECT 1 FROM temp_his_ix_poi_ext ext WHERE ext.pid = p.pid)
     AND NOT EXISTS
   (SELECT 1 FROM temp_au_ix_poi_mul_task tmp WHERE tmp.pid = p.pid)
     ';
    
    --TODO:��Ҫ����AU_IX_POI_NOKIA*
 
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('���POIʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;
  

  --���ӹ�ϵTAB����ԭ��
  --������1.��ӵ�����
  --���parent��ix_poi_parent ���ڣ�����au_ix_poi_parent����ӣ�������ӵ�au_ix_poi_parent��Ȼ�������au_ix_poi_children
  --
  --�޸ģ�1.�޸ĵ����ӣ� ���parent��ix_poi_parent ����  ������au_ix_poi_parent����� ���������һ����Ȼ��ɾ��ԭ�ȵ�children�������µ�children

  -------------------------------------------------------------------------------------------------------------------
  --POI�ں����ڸ��ӹ�ϵ��Ե�ʣ����������������޸ģ���ɾ����˳������ں�
  --����POIʱ���ӹ�ϵ�Ĵ������£�1.���������ӣ��������Ӷ�copy����ҵ��
  --�޸�POI���ӹ�ϵʱ��1.�޸ĵ����ӣ��������Ӷ�copy����ҵ�У�ɾ��ԭ������
  --ɾ�����ӹ�ϵ�Ĵ������£����ɾ�����Ǹ��ף�����Ҫ��parent��children��ɾ�����������ɾ��children

  --state:0 �� 1 ɾ�� 2 �޸� 3 ����
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
      ,YUCAIJI_FLAG
      ,poilevel_flag
      ,verified_flag
	  ,INNERPOI_FLAG
	  ,OPEN24H_FLAG
	  ,SAMEPOI_FLAG
    ,verified_mode_flag
       ) values (:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12,:13,:14,:15,:16,:17,:18,:19,:20,:21,:22,:23,:24,:25)'
      USING p_rec.audata_id, p_rec.pid, p_rec.name_flag, p_rec.address_flag, p_rec.tel_flag, p_rec.kind_flag, p_rec.post_code_flag, p_rec.food_type_flag, p_rec.parent_flag, p_rec.lable_flag, p_rec.display_point_flag, p_rec.guide_point_flag, p_rec.guide_x_flag, p_rec.guide_y_flag, p_rec.chain_flag,p_rec.tenant_flag,p_rec.FLOOR_USED,p_rec.FLOOR_EMPTY,p_rec.yucaiji_flag,p_rec.poilevel_flag,p_rec.verified_flag,p_rec.INNERPOI_FLAG,p_rec.OPEN24H_FLAG,p_rec.SAMEPOI_FLAG,p_rec.verified_mode_flag;
      commit;
     
  END;

  --�ĵ绰���绰(TELE_NUMBER��SAITEM�ڶ�λ����Ϊ��F��)
  --������ϵ��ʽ
  --��ɾ����ϵ��ʽ�������������ϵ��ʽ
  PROCEDURE process_modify_poi_contact IS
  BEGIN

    --��ֻ����ҵ�ɹ���Ĳ�����ҵ�⣨CONTACT��
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
                 AND c1.poi_pid = l.pid and l.audata_id = c1.audata_id)
         AND c1.contact_type in (1,2)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact
                 AND ipc.contact_type in (1,2));
                 
                 
      --ͨ��POI_PIDɾ��IX_POI_FLAG���еļ�¼��ֻ����ҵ�����еĲ�����ҵ����û�е�, IX_POI_FLAG����FLAG_CODE Ϊ 110040010000, 110040020000 ,110040030000 �ļ�¼�����û�У��򲻴���
     delete from IX_POI_FLAG ipf -- ɾ�� IX_POI_FLAG �м�¼
      where ipf.poi_pid in
            (select c1.poi_pid
               FROM ix_poi_contact c1
              WHERE c1.contact_type in (1,2) and EXISTS (SELECT 1
                       FROM temp_au_poi_modify_log l
                      WHERE l.tel_flag = 1
                        AND c1.poi_pid = l.pid)
                AND NOT EXISTS (SELECT 1
                       FROM au_ix_poi_contact auipc
                      WHERE auipc.poi_pid = c1.poi_pid
                        AND auipc.contact = c1.contact
                        AND auipc.contact_type in (1,2))
                and ipf.poi_pid = c1.poi_pid)
        and ipf.flag_code in ('110040010000', '110040020000', '110040030000');
                            
                 
    --ͨ��POI_PIDɾ��ix_poi_contact���еļ�¼��ֻ����ҵ�����еĲ�����ҵ����û�е�
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.tel_flag = 1
               AND c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.poi_pid = c1.poi_pid
               AND auipc.contact = c1.contact
               AND auipc.contact_type in (1,2))
       and c1.contact_type in (1,2);/*ֻ�ں���ͨ�̻����ƶ��绰 14��sprint1*/
               commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('�޸���ϵ��ʽʱ����' || SQLERRM);
      --rollback;
      RAISE;
    
  END;
  --FIXME:����name_groupid
  PROCEDURE process_modify_poi_name_add IS
    v_pid_count NUMBER;
  BEGIN
    /*DELETE FROM temp_ix_poi_name_mg; --Ϊ����������������ʳ��������������*/
    --IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĳ�����,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2��������һ����¼
    --����PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_poi_name auipn, temp_au_poi_modify_log aulog
     WHERE auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND auipn.audata_id = aulog.audata_id
       AND aulog.name_flag = 1
       AND NOT EXISTS (SELECT *
              FROM ix_poi_name ipn
             WHERE ipn.lang_code IN ('CHI', 'CHT')
               AND ipn.name_class = 2
               AND ipn.poi_pid = auipn.poi_pid);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_pid_count);
      INSERT INTO temp_ix_poi_name_mg WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT pid_man.pid_nextval('IX_POI_NAME') AS name_id,
             auipn.poi_pid,
             nvl(rs.name_groupid, 1) AS name_groupid, 
             auipn.name_class,
             auipn.NAME_TYPE ,
             auipn.lang_code,
             auipn.name,
             auipn.name_phonetic,
             auipn.keywords,
             auipn.nidb_pid,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS           
          FROM au_ix_poi_name auipn, rs, temp_au_poi_modify_log aulog
         WHERE auipn.poi_pid = rs.poi_pid(+)
           AND auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND EXISTS (SELECT 1
                  FROM temp_au_poi_modify_log tmp
                 WHERE tmp.name_flag = 1
                   AND tmp.pid = auipn.poi_pid)
           AND NOT EXISTS (SELECT *
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 2
                   AND ipn.poi_pid = auipn.poi_pid)
           AND auipn.audata_id = aulog.audata_id
           AND aulog.name_flag = 1;
    
    END IF;
    --����������ģ��Ұ�����Lang_CodeΪ��CHI������������Ϊԭʼ��2���ļ�¼����Ҫ����һ������Ϊ��������Ϊ��1��������
    copy_name_class1_data();
    /* INSERT INTO ix_poi_name
    SELECT * FROM temp_ix_poi_name_mg;*/
  commit;
  END;
  /*�޸�ʱ�����name_class=1�Ĳ����ڣ�����name_class=2�Ĵ��ڣ���Ҫ��name_class=2�ĸ���һ�������ӵ�ix_poi-name��*/
  PROCEDURE mod_poi_name_add_c1(is_ext BOOLEAN) IS
    v_count NUMBER;
    v_view  VARCHAR2(100);
  BEGIN
    /*  DELETE  FROM temp_ix_poi_name_mg;*/
    v_view := CASE is_ext
                WHEN FALSE THEN
                 'view_mg_au_ix_poi_name'
                WHEN TRUE THEN
                 'view_mg_au_ix_poi_name_ext'
              END;
    EXECUTE IMMEDIATE '
      SELECT COUNT(1)  FROM ix_poi_name ipn
       WHERE ipn.lang_code IN (''CHI'', ''CHT'')
         AND ipn.name_class = 2
         AND EXISTS (SELECT 1
                FROM ' || v_view ||
                      ' aurs
               WHERE aurs.poi_pid = ipn.poi_pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_name t
               WHERE t.poi_pid = ipn.poi_pid
                 AND t.lang_code IN (''CHI'', ''CHT'')
                 AND t.name_class = 1)'
      INTO v_count;
    IF (v_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_count);
      EXECUTE IMMEDIATE 'INSERT INTO temp_ix_poi_name_mg WITH rs AS
      (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_poi_name
        GROUP BY poi_pid)
        SELECT  pid_man.pid_nextval(''IX_POI_NAME'') AS name_id,
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
           AND ipn.POI_PID = rs.poi_pid(+)
           AND EXISTS (SELECT 1
                  FROM ' || v_view ||
                        ' aurs
                 WHERE aurs.poi_pid = ipn.poi_pid)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name t
                 WHERE t.poi_pid = ipn.poi_pid
                   AND t.lang_code IN (''CHI'', ''CHT'')
                   AND t.name_class = 1)';
    
    END IF;
    commit;
  END;
  PROCEDURE commit_poi_name_insert IS
  BEGIN
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
    commit;  
  END;
  --�޸����Ʊ�
  --��ɾ����������
  procedure process_modify_poi_name2 is
     v_pid_count NUMBER;
    begin      
     execute immediate 'truncate table temp_ix_poi_name_mg';  
       --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI���� NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊ��׼��1���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼�������޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�
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
               and au.lang_code in('CHI', 'CHT') 
               and au.name_type=2
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
               and au.lang_code in(''CHI'', ''CHT'') 
               and au.name_type=2
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
               and au.lang_code in('CHI', 'CHT') 
               and au.name_type=2
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
      commit;
      end;
  PROCEDURE process_modify_poi_name IS
    v_pid_count NUMBER;
  BEGIN 
    execute immediate 'truncate table temp_ix_poi_name_mg';  
    --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI����NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊԭʼ��2���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼�������޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�
     delete from IX_POI_NAME_FLAG a
      where exists
      (select 1
               from ix_poi_name t
              where exists (SELECT 1
                       FROM au_ix_poi_name au
                      WHERE au.audata_id IN
                            (SELECT audata_id
                               FROM temp_au_poi_modify_log aulog
                              WHERE aulog.name_flag = 1
                                and au.audata_id = aulog.audata_id)
                        and au.lang_code in ('CHI', 'CHT')
                        and au.name_type = 2
                        and t.poi_pid = au.poi_pid
                        )
                and t.lang_code IN ('CHI', 'CHT')
                AND t.name_class = 1 
                AND t.name_type = 1
                and t.name_id = a.name_id)
        and a.flag_code in
            ('110010010000', '110010020000', '110010030000', '110010040000');
    MERGE INTO ix_poi_name t
    USING (SELECT *
             FROM au_ix_poi_name au
            WHERE au.audata_id IN
                  (SELECT audata_id
                     FROM temp_au_poi_modify_log aulog
                    WHERE aulog.name_flag = 1  and au.audata_id = aulog.audata_id)
                  and au.lang_code in('CHI', 'CHT') and  au.name_type=2
           ) aurs
    ON (t.lang_code IN('CHI', 'CHT') AND t.name_class = 1 AND t.name_type = 1 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic;
       EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi_name au
         WHERE  au.audata_id IN (SELECT audata_id
                                  FROM temp_au_poi_modify_log aulog
                                 WHERE aulog.name_flag = 1 and au.audata_id = aulog.audata_id)
           and au.lang_code in(''CHI'', ''CHT'') and  au.name_type=2
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
               and au.lang_code in('CHI', 'CHT')
               and au.name_type=2
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
      commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('����POI����ʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;
 
  --�޸ĵ�ַ��
  --��ɾ����������
  --FIXME:����name_groupid
  PROCEDURE process_modify_poi_address IS
  BEGIN
    --������ POI���ơ�POIƴ����OLD���� (POINAME��POIPY�� OLD_NAME�� SA_ITEM��һλ����Ϊ��F��)
    --�ĵ�ַ��ADDRESS_NAME�� POINT_ADDRESS
    --ɾ��;
    /*ͨ��POI_PID�ж�AU_IX_POI_ADDRESS�����Ƿ������ݣ�
    1.���û�����ݣ���ͨ��POI_PIDɾ����POI��ȫ����ַ���������ġ�Ӣ�ġ����ĵ�ַ���Լ���ַ��Ӧ��FLAG_CODE��ʶ�������±���*/

    DELETE FROM IX_POI_ADDRESS C1
    WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.address_flag = 1
               AND c1.poi_pid = l.pid 
               AND NOT EXISTS(SELECT 1 FROM AU_IX_POI_ADDRESS T WHERE T.Audata_Id=l.audata_id))    
    AND c1.lang_code IN ('CHI', 'CHT','ENG', 'POR')   ;
    DELETE FROM IX_POI_FLAG c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.address_flag = 1
               AND c1.poi_pid = l.pid
               and not exists(select 1 from AU_IX_POI_ADDRESS T WHERE T.audata_id=l.audata_id) 
               )      
       AND c1.FLAG_CODE IN   ( '110030010000',
                               '110030020000',
                               '110030030000',
                               '110030040000',
                               '110030050000',
                               '110030060000',
                               '110030070000',
                               '110030080000',
                               '110030090000',
                               '110030100000',
                               '110030110000',
                               '110030120000');
    /*
    ͨ��POI_PID�ж�AU_IX_POI_ADDRESS�����Ƿ������ݣ�
    2.��������ݣ���ͨ��POI_PID�ж�IX_POI_ADDRESS�����Ƿ�������Դ���ΪCHI��CHT�ĵ�ַ���ݣ�
    a����������ڵ�ַ������һ����ַ��¼��
    b�����ĸ����ڵ�ַ
    ?	�޸��������ĵ�ַ��¼�����ݣ�
    ?	ɾ����ӦӢ�ĺ����ĵ�ַ��¼��
    ?	ͬʱͨ��POI_PIDɾ��IX_POI_FLAG����FLAG_CODE Ϊ����ֵ�ļ�¼�����û�У��򲻴���

    */
    --��ҵ����ڲ���ĸ��Ҳ���ڵ�����£�ɾ����ӦӢ�ĺ����ĵ�ַ��¼��
    DELETE FROM IX_POI_ADDRESS c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.address_flag = 1
               AND c1.poi_pid = l.pid
               and exists(select 1 from au_ix_poi_address where audata_Id=l.audata_id)
               )      
      and exists(select 1 from ix_poi_address t where c1.Poi_Pid=T.POI_PID and t.lang_code in('CHI','CHT'))
      AND C1.LANG_CODE IN('ENG', 'POR');
      --��ҵ����ڲ���ĸ��Ҳ���ڵ�����£�ɾ����Ӧpoi_flag��
    DELETE FROM IX_POI_FLAG c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.address_flag = 1
               AND c1.poi_pid = l.pid
               and exists(select 1 from au_IX_POI_ADDRESS T WHERE T.audata_id=l.audata_id)
               )      
       and exists(select 1 from IX_POI_ADDRESS T WHERE T.POI_PID=C1.POI_PID AND T.LANG_CODE IN('CHI','CHT'))
       AND c1.FLAG_CODE IN   ( '110030010000',
                               '110030020000',
                               '110030030000',
                               '110030040000',
                               '110030050000',
                               '110030060000',
                               '110030070000',
                               '110030080000',
                               '110030090000',
                               '110030100000',
                               '110030110000',
                               '110030120000');
    --��������ڵ�ַ������һ����ַ��¼��                                                              
    DELETE FROM ix_poi_address c1
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.address_flag = 1
               AND c1.poi_pid = l.pid
                and exists(select 1 from au_ix_poi_address t where t.audata_id=l.audata_id)
               )     
      AND c1.lang_code IN ('CHI', 'CHT');

      --������ POI���ơ�POIƴ����OLD���� (POINAME��POIPY)
      INSERT INTO ix_poi_address
        SELECT name_id,
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
                   AND c1.audata_id = l.audata_id)
           AND c1.lang_code IN ('CHI', 'CHT');
  commit;
    --commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('����POI����ʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;


 --��ı�ע
 PROCEDURE process_modify_poi_label IS
 BEGIN
   -- ���log�ֶ��а��������ڲ�POI������AU_IX_POI_FLAG�д���FLAG_CODE=110000030000�ļ�¼,��IX_POI_FLAG���в���һ����¼��FLAG_CODE =110000030000������Ѵ��ڣ��򲻲��롣	   
    INSERT INTO ix_poi_flag
      SELECT af.poi_pid, '110000030000', 0 AS u_record, NULL AS u_fields
        FROM au_ix_poi_flag af
       WHERE af.FLAG_CODE = '110000030000'
         AND EXISTS (SELECT 1
                FROM temp_au_poi_modify_log l
               WHERE l.innerpoi_flag = 1
                 AND af.poi_pid = l.pid and l.audata_id = af.audata_id)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_flag ipf
               WHERE ipf.poi_pid = af.poi_pid
                 AND ipf.FLAG_CODE = '110000030000');
   -- ���log�ֶ��а��������ڲ�POI������AU_IX_POI_FLAG�в�����FLAG_CODE=110000030000�ļ�¼,ɾ��IX_POI_FLAG����FLAG_CODE =110000030000�ļ�¼����������ڣ��򲻴���
   DELETE FROM ix_poi_flag ipf
   	   WHERE ipf.flag_code = '110000030000'
       AND EXISTS (SELECT 1 
       		  FROM au_ix_poi au
       		 WHERE ipf.poi_pid = au.pid
       		 AND EXISTS (SELECT 1
                FROM temp_au_poi_modify_log l
               WHERE l.innerpoi_flag = 1
                 AND au.pid = l.pid and l.audata_id = au.audata_id)
        	AND NOT EXISTS (SELECT 1
                FROM au_ix_poi_flag af
               WHERE au.audata_id = af.audata_id
                 AND af.FLAG_CODE = '110000030000'));

  -- �����ע�ֶΰ������ο���ַ�����ںϺ���ɾ�����ĵ�ַ�ı�ʶ��Ϣ������IX_POI_FLAG���в��롰�̶���־����ʩ�����ַ��(FLAG_CODE =110030060000)�ı�ʶ��Ϣ                         
       DELETE FROM IX_POI_FLAG c1
       WHERE  EXISTS (SELECT 1
                FROM au_ix_poi au
               WHERE au.pid = c1.poi_pid
                and instr(au.label, '���յ�ַ') > 0
                )
                and EXISTS (SELECT 1
                 FROM temp_au_poi_modify_log l
                WHERE l.lable_flag = 1
                AND c1.poi_pid = l.pid
                )
         AND c1.FLAG_CODE IN ('110030010000',
                              '110030020000',
                              '110030030000',
                              '110030040000',
                              '110030050000',
                              '110030070000',
                              '110030080000',
                              '110030090000',
                              '110030100000',
                              '110030110000',
                              '110030120000');
                              
   INSERT INTO ix_poi_flag
     SELECT a.pid as poi_pid, '110030060000' as flag_code, 0 AS u_record, NULL AS u_fields
       FROM au_ix_poi a
      where instr(a.label, '���յ�ַ') > 0
        and EXISTS (SELECT 1
               FROM temp_au_poi_modify_log l
              WHERE l.lable_flag = 1
                AND a.pid = l.pid
                and a.audata_id=l.audata_id
                )
        and NOT EXISTS
      (SELECT 1 FROM ix_poi_flag b WHERE a.pid = b.poi_pid and b.flag_code='110030060000');

   -- �����ע�ֶ��в����������յ�ַ���������ںϺ�ɾ��IX_POI_FLAG����FLAG_CODE =110030060000���̶���־����ʩ�����ַ���ļ�¼����������ڣ��򲻴���
   delete from ix_poi_flag ipf
    where ipf.flag_code = '110030060000'
      and EXISTS
    (select 1
             from au_ix_poi a
            where (a.label is null or instr(a.label, '���յ�ַ') = 0)
              and a.pid = ipf.poi_pid
              and EXISTS (SELECT 1
                     FROM temp_au_poi_modify_log l
                    WHERE l.lable_flag = 1
                      AND a.pid = l.pid and a.audata_id=l.audata_id));
 commit;
 EXCEPTION
   WHEN OTHERS THEN
     dbms_output.put_line('�޸ı�עʱ����' || SQLERRM);
     --rollback;
     RAISE;
 END;


  --����POI���ӹ�ϵ
  --�޸�POI���ӹ�ϵʱ��һ����ָ��POI�ĸ��׷����˱仯�����parent���ڣ���ֻ���children������parent��children����Ҫ���
  PROCEDURE process_modify_poi_relation IS
  BEGIN   
    execute immediate 'truncate table temp_ix_poi_parent_mg'; 
    --��ԭ�����ӱ�ɾ��
    DELETE FROM ix_poi_children ipc
     WHERE ipc.child_poi_pid IN
           (SELECT pid FROM temp_au_poi_modify_log l WHERE l.parent_flag = 1);
      --1.�޸ĵ����ӣ��������Ӷ�copy����ҵ��      
      INSERT INTO temp_ix_poi_parent_mg
        SELECT group_id,
               parent_poi_pid,
               TENANT_FLAG,
               null as MEMO,
               0, --U_RECORD,
               NULL --U_FIELDS
          FROM au_ix_poi_parent p
         WHERE exists
           (SELECT c.group_id
              FROM temp_au_poi_modify_log l, au_ix_poi_children c
             WHERE parent_flag = 1
               AND c.audata_id = l.audata_id 
               and c.group_id=p.group_id
               and c.field_task_id=p.field_task_id
               )
           AND NOT EXISTS
         (SELECT 1
                  FROM ix_poi_parent ip
                 WHERE ip.parent_poi_pid = p.parent_poi_pid);
      INSERT INTO ix_poi_parent
        SELECT * FROM temp_ix_poi_parent_mg;
  
    --����ӱ�    
    /*INSERT INTO ix_poi_children
      SELECT group_id, child_poi_pid, relation_type, u_record, u_fields
        FROM (SELECT ipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields,
                     row_number() over(PARTITION BY ipp.group_id, c.child_poi_pid, c.relation_type ORDER BY 1) AS rn
                FROM temp_au_poi_modify_log l,
                     au_ix_poi_children     c,
                     ix_poi_parent          ipp,
                     au_ix_poi_parent       auipp
               WHERE l.parent_flag = 1
                 AND c.audata_id = l.audata_id
                 AND auipp.group_id = c.group_id
                 AND auipp.parent_poi_pid = ipp.parent_poi_pid) rs
       WHERE rs.rn = 1;*/
       INSERT INTO ix_poi_children
         SELECT /*+ordered use_hash(t1,t2)*/
          t2.group_id,
          t1.child_poi_pid,
          t1.relation_type,
          0                AS u_record,
          NULL             AS u_fields
           FROM (SELECT /*+ordered*/
                 DISTINCT c.*, auipp.parent_poi_pid
                   FROM temp_au_poi_modify_log l,
                        au_ix_poi_children     c,
                        au_ix_poi_parent       auipp
                  WHERE l.parent_flag = 1
                    AND c.audata_id = l.audata_id
                    AND c.group_id = auipp.group_id 
                     AND auipp.Field_Task_Id=C.FIELD_TASK_ID
                    ) t1,
                
                (SELECT /*+use_hash(ipp,t2)*/
                  ipp.parent_poi_pid,
                  ipp.group_id,
                  row_number() over(PARTITION BY ipp.parent_poi_pid ORDER BY 1) AS rn
                   FROM ix_poi_parent ipp
                  WHERE EXISTS
                  (SELECT 1
                           FROM (SELECT /*+use_hash(auipp,c)*/
                                  auipp.*
                                   FROM au_ix_poi_parent auipp, au_ix_poi_children c
                                  WHERE auipp.group_id = c.group_id  AND auipp.Field_Task_Id=C.FIELD_TASK_ID) t3
                          WHERE t3.parent_poi_pid = ipp.parent_poi_pid)) t2
          WHERE t1.parent_poi_pid = t2.parent_poi_pid
            AND t2.rn = 1;

  
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('����POI���ӹ�ϵʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;
  --IX_POI_RESTAURANT �޸Ķ�����
  PROCEDURE process_modify_poi_restaurant IS
  BEGIN
   /* DELETE FROM ix_poi_restaurant ipr
     WHERE EXISTS (SELECT 1
              FROM temp_au_poi_modify_log l
             WHERE l.pid = ipr.poi_pid
               AND l.food_type_flag = 1);
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
        FROM au_ix_poi_restaurant auipr,temp_au_poi_modify_log l
       WHERE auipr.audata_id=l.audata_id
                 AND l.food_type_flag = 1'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_RESTAURANT', v_pid_count);
      INSERT INTO ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,U_RECORD,U_FIELDS)
        SELECT pid_man.pid_nextval('IX_POI_RESTAURANT') AS restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
             
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_restaurant auipr, temp_au_poi_modify_log l
         WHERE auipr.audata_id = l.audata_id
           AND l.food_type_flag = 1;
    END IF; */
     
    merge into ix_poi_restaurant t
    using (select l.pid, au.food_type
             from au_ix_poi_restaurant au, temp_au_poi_modify_log l
            where l.pid = au.poi_Pid and l.audata_id=au.audata_id
             AND l.food_type_flag = 1) rs
    on (t.poi_Pid = rs.pid) when matched then
      update set t.food_type = rs.food_type;
    
    -- ��ҵ�����ڲ�������ҵ���ڲ�����
    update ix_poi_restaurant ipr set ipr.food_type = null where not exists
     (
           select 1 from au_ix_poi_restaurant aipr where aipr.poi_pid = ipr.poi_pid
       ) and exists(
           select 1 from temp_au_poi_modify_log t where t.pid = ipr.poi_Pid AND t.food_type_flag = 1 
       );

     -- ��ҵ���ڲ�������ҵ�����ڲ�����
         insert into ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
         select 
               restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
               0 AS TRAVELGUIDE_FLAG,
               0 AS u_record,
               NULL AS u_fields
          from au_ix_poi_restaurant au, temp_au_poi_modify_log l
         where au.poi_pid = l.pid
           and au.audata_id = l.audata_id
           and l.food_type_flag = 1
           and not exists
         (select 1 from ix_poi_restaurant t where t.poi_pid = au.poi_pid);
  
  END;

  --ɾ��POI �¸�
  PROCEDURE delete_isolated_poi_parent IS
  BEGIN
    DELETE FROM ix_poi_parent p
     WHERE NOT EXISTS
     (SELECT * FROM ix_poi_children c WHERE c.group_id = p.group_id);
  commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('ɾ���¸�����' || SQLERRM);
      --rollback;
      RAISE;
    
  END;

  PROCEDURE process_att_modify_main_poi IS
  BEGIN
  
    --���
    --�ķ���:POI�ֱ�(king_code)
    --���ʱ�:�ʱ�(POST_CODE)
    --�Ķ�����:��ζ����(FOOD_TYPE)
    --�ı�ע:��ע(LABEL)
    --��24Сʱ:log�ֶ��а�������24Сʱ���������ִ�Сд��ȫ���OPEN_24H��ֵ��Y"
    --��RELATION: GEOMETRY
    --��λ��:X_GUIDE��Y_GUIDE
    --�ĵ�ַ:OLD_ADDRESS
    --�ĵ绰���绰(SAITEM�ڶ�λ����Ϊ��F��)
    --������ POI���ơ�POIƴ����OLD���� (OLD_NAME�� SA_ITEM��һλ����Ϊ��F��)
    --����ҵ��֤��ʶ
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
                  l.open24h_flag,
                  l.kind_flag,
                  l.post_code_flag,
                  l.food_type_flag,
                  l.parent_flag,
                  l.lable_flag,
                  l.display_point_flag,
                  l.guide_point_flag,
                  l.verified_flag,
                  auipn.name           AS auoldname,
                  auipa.fullname       AS auoldaddress,
                  p2.verified_flag     as au_verified_flag
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
                  l.name_flag = 1 OR l.chain_flag = 1  OR L.VERIFIED_FLAG = 1 OR L.open24h_flag = 1)
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
             p1.open_24h    = decode(v.open24h_flag, 0, p1.open_24h, v.open_24h), 
            p1.old_name = decode(v.name_flag, 0, p1.old_name, v.auoldname),
            p1.verified_flag = decode(v.VERIFIED_FLAG, 0, p1.verified_flag, v.au_verified_flag);
commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('�޸�POI�����ֶ�ʱ����' || SQLERRM);
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
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '������') > 0 THEN
      v_rec.name_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.name_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '�ĵ�ַ') > 0 THEN
      v_rec.address_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.address_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '�ĵ绰') > 0 THEN
      v_rec.tel_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.tel_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '�ķ���') > 0 THEN
      v_rec.kind_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.kind_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '���ʱ�') > 0 THEN
      v_rec.post_code_flag := 1;
      v_change_flag        := TRUE;
    ELSE
      v_rec.post_code_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '�Ķ�����') > 0 THEN
      v_rec.food_type_flag := 1;
      v_change_flag        := TRUE;
    ELSE
      v_rec.food_type_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '��FATHERSON') > 0 THEN
      v_rec.parent_flag := 1;
      v_change_flag     := TRUE;
    ELSE
      v_rec.parent_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '�ı�ע') > 0 THEN
      v_rec.lable_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.lable_flag := 0;
    END IF;
    IF rec.geo_oprstatus in( 0,1) AND instr(v_log, '��RELATION') > 0 THEN
      v_rec.display_point_flag := 1;
      v_change_flag            := TRUE;
    ELSE
      v_rec.display_point_flag := 0;
    END IF;
    IF rec.geo_oprstatus in( 0,1) AND instr(v_log, '��λ��') > 0 THEN
      v_rec.guide_point_flag := 1;
      v_change_flag          := TRUE;
    ELSE
      v_rec.guide_point_flag := 0;
    END IF;
    IF rec.geo_oprstatus in( 0,1) AND instr(v_log, '��GUIDEX') > 0 THEN
      v_rec.guide_x_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.guide_x_flag := 0;
    END IF;
    IF rec.geo_oprstatus in( 0,1) AND instr(v_log, '��GUIDEY') > 0 THEN
      v_rec.guide_y_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.guide_y_flag := 0;
    END IF;
    IF (rec.att_oprstatus in( 0,1) AND v_rec.kind_flag = 1) THEN
      v_rec.chain_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.chain_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) and instr(v_log, '��TENANT_FLAG') > 0 THEN
      v_rec.tenant_flag := 1;

      v_change_flag    := TRUE;
    ELSE
      v_rec.tenant_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) and instr(v_log, '��FLOOR_USED') > 0 THEN
      v_rec.FLOOR_USED := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.FLOOR_USED := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) and instr(v_log, '��FLOOR_EMPTY') > 0 THEN
      v_rec.FLOOR_EMPTY := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.FLOOR_EMPTY := 0;
    END IF;
    IF rec.att_oprstatus in(0,1) and instr(v_log, '��POI_LEVEL') > 0 THEN
      v_rec.poilevel_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.poilevel_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) and instr(v_log, '����ҵ��֤��ʶ') > 0 THEN
      v_rec.verified_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.verified_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '��24Сʱ') > 0 THEN
      v_rec.open24h_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.open24h_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '���ڲ�POI') > 0 THEN
      v_rec.innerpoi_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.innerpoi_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '��ͬһ��ϵ') > 0 THEN
      v_rec.samepoi_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.samepoi_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '����֤ģʽ') > 0 THEN
      v_rec.verified_mode_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.verified_mode_flag := 0;
    END IF;
  END;
  PROCEDURE parse_att_modify_log(rec           au_ix_poi%ROWTYPE,
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
    IF instr(v_log, '������') > 0 THEN
      v_rec.name_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.name_flag := 0;
    END IF;
    IF instr(v_log, '�ĵ�ַ') > 0 THEN
      v_rec.address_flag := 1;
      v_change_flag      := TRUE;
    ELSE
      v_rec.address_flag := 0;
    END IF;
    IF instr(v_log, '�ĵ绰') > 0 THEN
      v_rec.tel_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.tel_flag := 0;
    END IF;
    IF instr(v_log, '�ķ���') > 0 THEN
      v_rec.kind_flag := 1;
      v_change_flag   := TRUE;
    ELSE
      v_rec.kind_flag := 0;
    END IF;
    IF instr(v_log, '���ʱ�') > 0 THEN
      v_rec.post_code_flag := 1;
      v_change_flag        := TRUE;
    ELSE
      v_rec.post_code_flag := 0;
    END IF;
    IF instr(v_log, '�Ķ�����') > 0 THEN
      v_rec.food_type_flag := 1;
      v_change_flag        := TRUE;
    ELSE
      v_rec.food_type_flag := 0;
    END IF;
    IF instr(v_log, '��FATHERSON') > 0 THEN
      v_rec.parent_flag := 1;
      v_change_flag     := TRUE;
    ELSE
      v_rec.parent_flag := 0;
    END IF;
    IF instr(v_log, '�ı�ע') > 0 THEN
      v_rec.lable_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.lable_flag := 0;
    END IF;
    IF instr(v_log, '��TENANT_FLAG') > 0 THEN
      v_rec.tenant_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.tenant_flag := 0;
    END IF;
    IF instr(v_log, '��FLOOR_USED') > 0 THEN
      v_rec.FLOOR_USED := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.FLOOR_USED := 0;
    END IF;
    IF instr(v_log, '��FLOOR_EMPTY') > 0 THEN
      v_rec.FLOOR_EMPTY := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.FLOOR_EMPTY := 0;
    END IF;
    IF instr(v_log, '��Ԥ�ɼ���ʶ') > 0 THEN
      v_rec.yucaiji_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.yucaiji_flag := 0;
    END IF;
    IF instr(v_log, '��POI_LEVEL') > 0 THEN
      v_rec.poilevel_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.poilevel_flag := 0;
    END IF;
    IF (rec.att_oprstatus in( 0,1) AND v_rec.kind_flag = 1) THEN
      v_rec.chain_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.chain_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) and instr(v_log, '����ҵ��֤��ʶ') > 0 THEN
      v_rec.verified_flag := 1;
      v_change_flag    := TRUE;
    ELSE
      v_rec.verified_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '��24Сʱ') > 0 THEN
      v_rec.open24h_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.open24h_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '���ڲ�POI') > 0 THEN
      v_rec.innerpoi_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.innerpoi_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '��ͬһ��ϵ') > 0 THEN
      v_rec.samepoi_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.samepoi_flag := 0;
    END IF;
    IF rec.att_oprstatus in( 0,1) AND instr(v_log, '����֤ģʽ') > 0 THEN
      v_rec.verified_mode_flag := 1;
      v_change_flag  := TRUE;
    ELSE
      v_rec.verified_mode_flag := 0;
    END IF;
  
  END;
  PROCEDURE process_attgeo_modify_log IS
    v_rec         temp_au_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
  BEGIN  
    execute immediate 'truncate table temp_au_poi_modify_log'; 
    FOR rec IN (SELECT a.*
                  FROM au_ix_poi a
                 WHERE a.state = 2
                   AND (a.att_oprstatus in( 0,1) OR a.geo_oprstatus in( 0,1))
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
    commit;
     dbms_stats.gather_table_stats(user,'TEMP_AU_POI_MODIFY_LOG');
  END;
  PROCEDURE process_att_modify_log IS
    v_rec         temp_au_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
  BEGIN   
    execute immediate 'truncate table temp_au_poi_modify_log'; 
    FOR rec IN (SELECT a.*
                  FROM au_ix_poi a
                 WHERE a.state = 2
                   AND a.att_oprstatus in( 0,1)
                   AND EXISTS
                 (SELECT 1 FROM ix_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ix_poi_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag := FALSE;
      parse_att_modify_log(rec, v_rec, v_change_flag);
      IF v_change_flag = TRUE THEN
        save_poi_modify_log(v_rec);
      END IF;
    END LOOP;
  commit;
   dbms_stats.gather_table_stats(user,'TEMP_AU_POI_MODIFY_LOG');
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('������־ʱ����' || SQLERRM);
      RAISE;
  END;
  PROCEDURE delete_same_poi IS
  BEGIN
    DELETE FROM ix_samepoi p
     WHERE NOT EXISTS (SELECT 1
              FROM ix_samepoi_part isp
             WHERE isp.group_id = p.group_id);
    commit;         
  END;

  PROCEDURE process_att_add_poi IS
  BEGIN
    dbms_output.put_line('BEGIN process_att_add_poi ');
    do_add(merge_utils.merge_type_att);
    commit;
  END;
  PROCEDURE do_del(v_merge_type VARCHAR2) IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'poi2');
    --ɾ������;
    EXECUTE IMMEDIATE 'DELETE ix_poi poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.pid
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --�ж����ҵ����Ĳ��ں� 
            )';
    --ɾ��IX_POI_ADDRESS;
    EXECUTE IMMEDIATE 'DELETE IX_POI_ADDRESS poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.POI_PID
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --�ж����ҵ����Ĳ��ں� 
            )';
    --ɾ��IX_POI_CONTACT;
    EXECUTE IMMEDIATE 'DELETE IX_POI_CONTACT poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.POI_PID
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --�ж����ҵ����Ĳ��ں� 
            )';
    --ix_poi_name_tone        
   /* EXECUTE IMMEDIATE 'DELETE ix_poi_name_tone ipnt
     WHERE EXISTS (SELECT 1
              FROM IX_POI_NAME poi1
             WHERE EXISTS (SELECT 1
                      FROM au_ix_poi poi2
                     WHERE poi2.pid = poi1.POI_PID
                       AND poi2.state = 1
                       AND ' || v_oprstatus_clause || '
                       AND NOT EXISTS
                     (SELECT 1
                              FROM temp_au_ix_poi_mul_task tmp
                             WHERE tmp.pid = poi2.pid) --�ж����ҵ����Ĳ��ں� 
                    )
               AND poi1.name_id = ipnt.name_id)'; */
    --ɾ��IX_POI_NAME_FLAG
    EXECUTE IMMEDIATE 'DELETE IX_POI_NAME_FLAG ipnt
     WHERE EXISTS (SELECT 1
              FROM IX_POI_NAME poi1
             WHERE EXISTS (SELECT 1
                      FROM au_ix_poi poi2
                     WHERE poi2.pid = poi1.POI_PID
                       AND poi2.state = 1
                       AND ' || v_oprstatus_clause || '
                       AND NOT EXISTS
                     (SELECT 1
                              FROM temp_au_ix_poi_mul_task tmp
                             WHERE tmp.pid = poi2.pid) --�ж����ҵ����Ĳ��ں� 
                    )
               AND poi1.name_id = ipnt.name_id)';
    --ɾ��IX_POI_NAME;
    EXECUTE IMMEDIATE 'DELETE IX_POI_NAME poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.POI_PID
               AND poi2.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = poi2.pid) --�ж����ҵ����Ĳ��ں� 
            )';
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,
                                                           'L');
    --ɾ�� ����;
    --����ɾ��ʱ����Ҫ���ӱ�Ҳɾ��
    EXECUTE IMMEDIATE 'DELETE FROM ix_poi_children c
     WHERE EXISTS
     (SELECT 1
              FROM (SELECT c.group_id
                      FROM au_ix_poi L, ix_poi_parent c
                     WHERE L.state = 1
                       AND ' || v_oprstatus_clause || '
                       AND NOT EXISTS (SELECT 1
                              FROM temp_au_ix_poi_mul_task tmp
                             WHERE tmp.pid = L.pid) --�ж����ҵ����Ĳ��ں� 
                       AND c.PARENT_POI_PID = L.pid) v
             WHERE c.group_id = v.group_id)';
    ----ɾ�����Ǹ���;
    EXECUTE IMMEDIATE 'DELETE FROM ix_poi_parent p
     WHERE EXISTS
     (SELECT 1
              FROM (SELECT c.group_id
                      FROM au_ix_poi L, ix_poi_parent c
                     WHERE L.state = 1
                       AND ' || v_oprstatus_clause || '
                       AND NOT EXISTS (SELECT 1
                              FROM temp_au_ix_poi_mul_task tmp
                             WHERE tmp.pid = L.pid) --�ж����ҵ����Ĳ��ں�
                       AND c.PARENT_POI_PID = L.pid) v
             WHERE p.group_id = v.group_id)';
  
    --ɾ���ӱ�
    ----ɾ�����Ƕ���
    EXECUTE IMMEDIATE 'DELETE FROM ix_poi_children c
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid) --�ж����ҵ����Ĳ��ں�
               AND c.child_poi_pid = L.pid)';
    --commit;
    --ix_poi_flag
    EXECUTE IMMEDIATE 'DELETE FROM ix_poi_flag ipf
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipf.poi_pid = L.pid)';
    --delete X_POI_ENTRYIMAGE
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_ENTRYIMAGE ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipe.poi_pid = L.pid)';
    --IX_POI_ICON
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_ICON ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipe.poi_pid = L.pid)';
    --IX_POI_PHOTO
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_PHOTO ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipe.poi_pid = L.pid)';
    --IX_POI_AUDIO
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_AUDIO ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipe.poi_pid = L.pid)';
    --IX_POI_VIDEO
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_VIDEO ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipe.poi_pid = L.pid)';
    --IX_SAMEPOI   
    /*EXECUTE IMMEDIATE 'DELETE FROM IX_SAMEPOI ipe
    WHERE EXISTS (SELECT 1
             FROM au_ix_poi L, IX_SAMEPOI_PART isp
            WHERE L.state = 1
              AND ' || v_oprstatus_clause || '
              AND NOT EXISTS (SELECT 1
                     FROM temp_au_ix_poi_mul_task tmp
                    WHERE tmp.pid = L.pid)
              AND isp.poi_pid = L.pid
              AND isp.group_id = ipe.group_id)';*/
    --IX_SAMEPOI_PART
    EXECUTE IMMEDIATE 'DELETE FROM IX_SAMEPOI_PART ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipe.poi_pid = L.pid)';
  
    /*�����Ϣ*/
    --IX_POI_ADVERTISEMENT
  /*  EXECUTE IMMEDIATE 'DELETE FROM IX_POI_ADVERTISEMENT ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)'; */
    --IX_POI_GASSTATION
   /* EXECUTE IMMEDIATE 'DELETE FROM IX_POI_GASSTATION ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)'; */
    --IX_POI_INTRODUCTION
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_INTRODUCTION ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';
    --IX_POI_ATTRACTION  
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_ATTRACTION ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';
    --IX_POI_HOTEL
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_HOTEL ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';
    --IX_POI_RESTAURANT
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_RESTAURANT ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';
  
    --IX_POI_CHARGINGPLOT
  /*  EXECUTE IMMEDIATE 'DELETE FROM IX_POI_CHARGINGPLOT ipa
     WHERE EXISTS
     (SELECT 1
              FROM au_ix_poi L, IX_POI_CHARGINGSTATION ipc
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipc.poi_pid = L.pid
               AND ipc.charging_id = ipa.charging_id)'; */
    --IX_POI_CHARGINGSTATION
   /* EXECUTE IMMEDIATE 'DELETE FROM IX_POI_CHARGINGSTATION ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)'; */
    --IX_POI_BUSINESSTIME           
    /*EXECUTE IMMEDIATE 'DELETE FROM IX_POI_BUSINESSTIME ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)'; */
    --IX_POI_BUILDING
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_BUILDING ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';
    --IX_POI_DETAIL
 /*   EXECUTE IMMEDIATE 'DELETE FROM IX_POI_DETAIL ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';  */             
    --IX_POI_PARKING
   /* EXECUTE IMMEDIATE 'DELETE FROM IX_POI_PARKING ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)'; */
   --IX_POI_TOURROUTE
  /*  EXECUTE IMMEDIATE 'DELETE FROM IX_POI_TOURROUTE ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';  */                      
    --IX_POI_EVENT
  /*  EXECUTE IMMEDIATE 'DELETE FROM IX_POI_EVENT ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi L
             WHERE L.state = 1
               AND ' || v_oprstatus_clause || '
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = L.pid)
               AND ipa.poi_pid = L.pid)';    */        
      commit;         
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('ɾ��POIʱ����' || SQLERRM);
      --rollback;
      RAISE;
    
  END;

  PROCEDURE process_att_delete_poi IS
  BEGIN
  
    do_del(merge_utils.merge_type_att);
    commit;
  END;
  PROCEDURE mod_main_poi_state_ext(v_merge_type VARCHAR2) IS
  BEGIN
     execute immediate '
      MERGE INTO ix_poi p1
      USING (select au.* from au_ix_poi au ,temp_his_ix_poi_ext ext where au.pid=ext.pid  and ('||merge_utils.get_proxypoi_clause(v_merge_type,'au')||')) v
      ON (p1.pid = v.pid)
      WHEN MATCHED THEN
        UPDATE SET p1.state = 3,p1.log=v.log';
    commit;  
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
  PROCEDURE process_geo_modify_main_poi IS
  BEGIN
    --��RELATION: GEOMETRY
    --��λ��:X_GUIDE��Y_GUIDE    
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
                  p2.region_id,
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
             p1.region_id = decode(v.display_point_flag,
                                  0,
                                  p1.region_id,
                                  v.region_id),                                          
             p1.x_guide  = decode(guide_x_flag, 0, decode(guide_y_flag,0,p1.x_guide,v.x_guide),v.x_guide),             
             p1.y_guide = decode(guide_y_flag, 0, decode(guide_x_flag,0,p1.y_guide,v.y_guide),v.y_guide),
             p1.link_pid = decode(v.guide_x_flag,
                                  0,
                                  decode(v.guide_y_flag,
                                         0,
                                         p1.link_pid,
                                         v.link_pid),
                                  v.link_pid),
             p1.side     = decode(v.guide_point_flag,
                                  0,
                                  decode(v.guide_y_flag, 0,decode(v.guide_x_flag,0,p1.side,v.side), v.side),
                                  v.side);
                                  commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('�޸�POI�����ֶ�ʱ����' || SQLERRM);
      RAISE;
    
  END;
  PROCEDURE process_geo_modify_log IS
    v_rec         temp_au_poi_modify_log%ROWTYPE;
    v_change_flag BOOLEAN;
    v_log         VARCHAR2(4000);
  BEGIN  
    execute immediate 'truncate table temp_au_poi_modify_log'; 
    FOR rec IN (SELECT a.audata_id, a.pid, a.log
                  FROM au_ix_poi a
                 WHERE a.log IS NOT NULL
                   AND a.state = 2
                   AND a.geo_oprstatus in( 0,1)
                   AND EXISTS
                 (SELECT 1 FROM ix_poi p WHERE a.pid = p.pid)
                   AND NOT EXISTS (SELECT 1
                          FROM temp_au_ix_poi_mul_task tmp
                         WHERE tmp.pid = a.pid)) LOOP
      v_change_flag   := FALSE;
      v_rec.audata_id := rec.audata_id;
      v_rec.pid       := rec.pid;
      v_log           := upper(rec.log);
      IF instr(v_log, '��RELATION') > 0 THEN
        v_rec.display_point_flag := 1;
        v_change_flag            := TRUE;
      ELSE
        v_rec.display_point_flag := 0;
      END IF;
      IF instr(v_log, '��λ��') > 0 THEN
        v_rec.guide_point_flag := 1;
        v_change_flag          := TRUE;
      ELSE
        v_rec.guide_point_flag := 0;
      END IF;
      IF instr(v_log, '��GUIDEX') > 0 THEN
        v_rec.guide_x_flag := 1;
        v_change_flag      := TRUE;
      ELSE
        v_rec.guide_x_flag := 0;
      END IF;
      IF instr(v_log, '��GUIDEY') > 0 THEN
        v_rec.guide_y_flag := 1;
        v_change_flag      := TRUE;
      ELSE
        v_rec.guide_y_flag := 0;
      END IF;
      IF v_change_flag = TRUE THEN
        save_poi_modify_log(v_rec);
      END IF;
    END LOOP;
  commit;
   dbms_stats.gather_table_stats(user,'TEMP_AU_POI_MODIFY_LOG');
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('������־ʱ����' || SQLERRM);
      RAISE;
  END;
  PROCEDURE process_geo_add_poi IS
  BEGIN
    dbms_output.put_line('BEGIN process_geo_add_poi ');
    do_add(merge_utils.merge_type_geo);
    commit;
  
  END;
  PROCEDURE process_att_geo_add_poi IS
  BEGIN
    dbms_output.put_line('BEGIN process_att_geo_add_poi ');
    do_add(merge_utils.merge_type_geoatt);
    commit;
  END;
  PROCEDURE process_geo_delete_poi IS
  BEGIN
    do_del(merge_utils.merge_type_geo);
    commit;
  
  END;
  PROCEDURE process_att_geo_delete_poi IS
  BEGIN
    do_del(merge_utils.merge_type_geoatt);
    commit;
  END;
  /*ix_poi�����Ѿ����ڵ�pid�� �����ں�*
  * 1. �޸���ҵ����ix_poi�ķǼ����ֶΣ������޸��ں�����
    2 �����ӱ���޸ģ�a.�����ҵ�ӱ�û�����ݣ����ںϣ�
     b.����ɾ����ҵ�ӱ����ݣ�����ҵ���ݽ����ںϣ�����ɾ���ӱ���ں����������������ӱ���ں�����
  */
  PROCEDURE att_add_poi_ext IS
  BEGIN
    --�ں���������           
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
             --ip.region_id    = rs.region_id,
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
             ip.field_task_id = rs.field_task_id,
             ip.verified_flag = rs.verified_flag
             ;
  commit;
  END;
  
  PROCEDURE att_add_poi_kind_code_ext IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(merge_utils.merge_type_att,
                                                           'au');
     -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000100000,110000120000,110000140000,110000150000,110000170000,110000220000 �ļ�¼��
    execute immediate '
    DELETE FROM ix_poi_flag t
     WHERE t.flag_code IN (''110000100000'',
                           ''110000120000'',
                           ''110000140000'',
                           ''110000150000'',
                           ''110000170000'',
                           ''110000250000'')
       AND EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.state = 3
               AND ' || v_oprstatus_clause || '
               AND au.pid = t.poi_pid
               AND EXISTS (SELECT 1
                      FROM temp_his_ix_poi_ext ext
                     WHERE ext.pid = au.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = au.pid))';
  
    -- ͨ��POI_PID��ɾ��IX_POI_HOTEL���еļ�¼
    execute immediate '
    DELETE FROM ix_poi_hotel t
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.state = 3
               AND ' || v_oprstatus_clause || '
               AND au.pid = t.poi_pid
               AND EXISTS (SELECT 1
                      FROM temp_his_ix_poi_ext ext
                     WHERE ext.pid = au.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = au.pid))';
    -- ����ҵ����AU_IX_POI_HOTEL���е���Ϣ�����뵽IX_POI_HOTEL����
    execute immediate '
    INSERT INTO IX_POI_HOTEL
      (HOTEL_ID,
       POI_PID,
       CREDIT_CARD,
       RATING,
       CHECKIN_TIME,
       CHECKOUT_TIME,
       ROOM_COUNT,
       ROOM_TYPE,
       ROOM_PRICE,
       BREAKFAST,
       SERVICE,
       PARKING,
       LONG_DESCRIPTION,
       LONG_DESCRIP_ENG,
       OPEN_HOUR,
       OPEN_HOUR_ENG,
       TELEPHONE,
       ADDRESS,
       CITY,
       PHOTO_NAME,
       TRAVELGUIDE_FLAG,
       U_RECORD,
       U_FIELDS)
      SELECT hotel_id,
             poi_pid,
             credit_card,
             rating,
             checkin_time,
             checkout_time,
             room_count,
             room_type,
             room_price,
             breakfast,
             service,
             parking,
             long_description,
             long_descrip_eng,
             open_hour,
             open_hour_eng,
             telephone,
             address,
             city,
             photo_name,
             0                AS TRAVELGUIDE_FLAG,     
             0                AS u_record,
             NULL             AS u_fields
        FROM au_ix_poi au, au_ix_poi_hotel h
       WHERE au.state = 3
         and au.pid = h.poi_pid
         and au.audata_id = h.audata_id
         AND ' || v_oprstatus_clause || '
               AND EXISTS (SELECT 1
                      FROM temp_his_ix_poi_ext ext
                     WHERE ext.pid = au.pid)
               AND NOT EXISTS (SELECT 1
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = au.pid)';
     commit;
  END;
  
  PROCEDURE att_add_poi_level_flag_ext IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(merge_utils.merge_type_att,
                                                           'au');
    execute immediate '
    DELETE FROM ix_poi_flag t
    WHERE  
        EXISTS(SELECT 1
          FROM au_ix_poi au
         WHERE au.state = 3
           AND ' || v_oprstatus_clause || '
           AND t.flag_code IN (''110000200000'', ''110000210000'',''110000210001'',''110000210002'',''110000210003'',''110000210004'',''110000220000'')
           AND EXISTS
         (SELECT 1 FROM temp_his_ix_poi_ext ext WHERE ext.pid = au.pid)
           AND NOT EXISTS (SELECT 1
                  FROM temp_au_ix_poi_mul_task tmp
                 WHERE tmp.pid = au.pid)
           AND au.pid = t.poi_pid)' ;
    commit;
  END;
  
  PROCEDURE att_verified_mode_flag_ext IS
    v_oprstatus_clause VARCHAR2(100);
  BEGIN
    v_oprstatus_clause := merge_utils.get_proxypoi_clause(merge_utils.merge_type_att,
                                                           'au');
    --AU_IX_POI���У�STATE= 2��,��LOG����������֤ģʽ������AU_IX_POI_FLAG����FLAG_CODE��ֵ��110000300000���ֳ���֤����
    --����POI_PID��IX_POI_FLAG����FLAG_CODE��ֵ�ǡ�110000330000���Ľ���ɾ����
    EXECUTE IMMEDIATE 'DELETE FROM ix_poi_flag f
     WHERE f.flag_code = ''110000330000''
       AND EXISTS
     (SELECT 1
              FROM au_ix_poi au, au_ix_poi_flag af
             WHERE au.audata_id = af.audata_id
               AND au.pid = af.poi_pid
               AND f.poi_pid = au.pid
               AND au.state = 3
               AND ' || v_oprstatus_clause || '
               AND af.flag_code = ''110000300000''
               AND EXISTS
             (SELECT 1 FROM temp_his_ix_poi_ext e WHERE au.pid = e.pid))';
     commit;
  END;
  
  PROCEDURE att_add_poi_flag_ext IS
  BEGIN
    do_add_ix_poi_flag(merge_utils.merge_type_att, '');
    commit;
  END;
  --//TODO:�ع�����  
  PROCEDURE process_mod_poi_name_add_ext IS
    v_pid_count NUMBER;
  BEGIN
    /*DELETE FROM temp_ix_poi_name_mg; --Ϊ����������������ʳ��������������*/
    --IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĳ�����,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2��������һ����¼
    --����PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_poi_name auipn
     WHERE auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND EXISTS (SELECT 1
              FROM temp_his_ix_poi_ext tmp
             WHERE tmp.pid = auipn.poi_pid)
       AND NOT EXISTS (SELECT *
              FROM ix_poi_name ipn
             WHERE ipn.lang_code IN ('CHI', 'CHT')
               AND ipn.name_class = 2
               AND ipn.poi_pid = auipn.poi_pid)
       AND EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.audata_id = auipn.audata_id
               AND au.att_oprstatus in( 0,1));
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_pid_count);
      INSERT INTO temp_ix_poi_name_mg WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT pid_man.pid_nextval('IX_POI_NAME') AS name_id,
               auipn.poi_pid,
               nvl(rs.name_groupid, 1) AS name_groupid,               
               auipn.name_class,
               auipn.name_type,
               auipn.lang_code,
               auipn.name,
               auipn.name_phonetic,
               auipn.keywords,
               auipn.nidb_pid,
               0, --c.U_RECORD,
               NULL --c.U_FIELDS
          FROM au_ix_poi_name auipn, rs
         WHERE auipn.poi_pid = rs.poi_pid(+)
           AND auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext tmp
                 WHERE tmp.pid = auipn.poi_pid)
           AND NOT EXISTS (SELECT *
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 2
                   AND ipn.poi_pid = auipn.poi_pid)
           AND EXISTS (SELECT 1
                  FROM au_ix_poi au
                 WHERE au.audata_id = auipn.audata_id
                   AND au.att_oprstatus in( 0,1));
    END IF;
    --����������ģ��Ұ�����Lang_CodeΪ��CHI������������Ϊԭʼ��2���ļ�¼����Ҫ����һ������Ϊ��������Ϊ��1��������
    copy_name_class1_data();
    /*INSERT INTO ix_poi_name
    SELECT * FROM temp_ix_poi_name_mg;*/
  commit;
  END;
  PROCEDURE att_add_poi_name_ext IS
    v_pid_count number;
  BEGIN
    delete from temp_ix_poi_name_mg;
     --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI���� NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊ��׼��1���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼��
     -- �����޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�,��ɾ������NAME_ID��Ӧ��IX_POI_NAME_FLAG����FLAG_CODE Ϊ
     -- 110010010000 ,110010020000 ,110010030000 ,110010040000 �ļ�¼�����û�У��򲻴���
     
 
     
    MERGE INTO ix_poi_name t
    USING (SELECT au.*
             FROM au_ix_poi_name au,TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
            WHERE au.poi_pid = ext.pid and aui.audata_id = au.audata_id 
            and au.lang_code in('CHI', 'CHT') and  au.name_type=2
            and aui.state=3
            and aui.att_oprstatus in(0,1)
           ) aurs
    ON (t.lang_code IN('CHI', 'CHT') AND t.name_class = 1 AND t.name_type = 1 AND t.poi_pid = aurs.poi_pid)
    WHEN MATCHED THEN
      UPDATE SET NAME = aurs.name, name_phonetic = aurs. name_phonetic;
  
     EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_ix_poi_name au, TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
         WHERE  au.poi_pid = ext.pid and aui.audata_id = au.audata_id and au.lang_code in(''CHI'', ''CHT'') and  au.name_type=2
           and aui.state=3
           and aui.att_oprstatus in(0,1)
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
          FROM au_ix_poi_name au, rs,TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
         WHERE au.poi_pid = rs.poi_pid(+)  
           AND au.poi_pid = ext.pid
           and au.lang_code in('CHI', 'CHT')
           and au.name_type=2
           and au.audata_id = aui.audata_id
           and aui.state=3
           and aui.att_oprstatus in(0,1)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid);
  end if;
    INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;
      commit;
  end;
  /*
  * �޸��ں�poi_name<br/>
     * IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĵ���,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2�����޸�ix_poi_name ��NAME��NAME_PHONETIC ��ֵ<br/>
     * --ͬʱ����Lang_CodeΪ��CHI������������Ϊ��׼��1����NAME��NAME_PHONETIC��ֵ<br/>
     * -IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĳ�����,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2��������һ����¼     * 
  */
  PROCEDURE att_add_poi_name_ext2 IS
    v_pid_count number;
  BEGIN  
    execute immediate 'truncate table temp_ix_poi_name_mg'; 
     --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI����NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊԭʼ��2���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼�������޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�
    MERGE INTO ix_poi_name t
    USING (SELECT au.*
             FROM au_ix_poi_name au,TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
            WHERE au.poi_pid = ext.pid  and au.audata_id=aui.audata_id
            and au.lang_code in('CHI', 'CHT') and  au.name_type=2 
            and aui.state=3
            and aui.att_oprstatus in(0,1)        
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
          FROM au_ix_poi_name au, rs,TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.poi_pid = ext.pid  
           and au.lang_code in('CHI', 'CHT')
           and au.name_type=2
           and au.audata_id = aui.audata_id
           and aui.state=3
           and aui.att_oprstatus in(0,1)    
           and not exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id)
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid);
         execute immediate '                   
    select count(1) from   au_ix_poi_name au,TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
    where  au.poi_pid = ext.pid and au.audata_Id=aui.audata_id
           and au.lang_code in(''CHI'', ''CHT'') and  au.name_type=2
           and aui.state=3
           and aui.att_oprstatus in(0,1)    
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
              FROM au_ix_poi_name au, rs,TEMP_HIS_IX_POI_EXT ext,au_ix_poi aui
         WHERE au.poi_pid = rs.poi_pid(+)
           AND au.poi_pid = ext.pid  
           and au.audata_Id=aui.audata_id
           and au.lang_code in('CHI', 'CHT')
           and au.name_type=2
           and aui.state=3
           and aui.att_oprstatus in(0,1)    
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
      commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('����POI����ʱ����' || SQLERRM);
      --rollback;
      RAISE;               
  END;
  --FIXME:����name_groupid
  PROCEDURE att_add_poi_address_ext IS
  BEGIN
    
    DELETE FROM ix_poi_address c1
     WHERE EXISTS
     (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
      AND EXISTS (SELECT 1
                  FROM au_ix_poi au
                 WHERE au.pid = c1.poi_pid
                   AND au.att_oprstatus in(0,1) and exists(select 1 from au_ix_poi_address t where t.audata_id=au.audata_id))
       AND c1.lang_code IN ('CHI', 'CHT');
      --������ POI���ơ�POIƴ����OLD���� (POINAME��POIPY)
      INSERT INTO ix_poi_address
        SELECT name_id,
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
                 WHERE au.audata_id = c1.audata_id
                   AND au.att_oprstatus in( 0,1));
    commit;
  END;
  PROCEDURE att_add_poi_contact_ext IS
  BEGIN
    --��ֻ����ҵ�ɹ������еĲ�����ҵ��
    INSERT INTO ix_poi_contact
      SELECT poi_pid,
             contact_type,
             contact,
             contact_depart,
             priority,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_contact c1, au_ix_poi aui 
       WHERE EXISTS
       (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact
                 AND ipc.contact_type in (1,2))
           and c1.poi_pid = aui.pid and aui.att_oprstatus in( 0,1) and c1.audata_id = aui.audata_id AND c1.contact_type in (1,2);       
                 
                 
    --��ֻ����ҵ�����е�ɾ��
    DELETE FROM ix_poi_contact c1   -- ɾ�� ix_poi_contact�м�¼
       WHERE EXISTS
     (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
       
       AND EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.pid = c1.poi_pid
               AND au.att_oprstatus = 0
               AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.audata_id = au.audata_id
               AND auipc.contact = c1.contact
               AND auipc.contact_type in (1,2))
               ) 
       AND c1.contact_type in (1,2)/*ֻ�ں���ͨ�̻����ƶ��绰*/;
      commit;
  END;

  PROCEDURE att_add_relation_ext IS
  BEGIN
  
     execute immediate 'truncate table temp_ix_poi_parent_mg'; 
    DELETE FROM ix_poi_children ipc
     WHERE ipc.child_poi_pid IN (SELECT pid FROM temp_his_ix_poi_ext ext)
       AND EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.pid = ipc.child_poi_pid
               AND au.att_oprstatus in( 0,1));

      --1.�޸ĵ����ӣ��������Ӷ�copy����ҵ��      
      INSERT INTO temp_ix_poi_parent_mg
        SELECT group_id,
               parent_poi_pid,
               TENANT_FLAG,
               null as  MEMO,
               0, --U_RECORD,
               NULL --U_FIELDS
          FROM au_ix_poi_parent p
         WHERE EXISTS
               (SELECT c.group_id
                  FROM temp_his_ix_poi_ext l, au_ix_poi_children c
                 WHERE c.child_poi_pid = l.pid
                   AND C.GROUP_ID=P.GROUP_ID AND C.FIELD_TASK_ID=P.FIELD_TASK_ID
                   AND EXISTS (SELECT 1
                          FROM au_ix_poi au
                         WHERE au.audata_id = c.audata_id
                           AND au.att_oprstatus in( 0,1)))
           AND NOT EXISTS
         (SELECT 1
                  FROM temp_his_ix_poi_parent ip
                 WHERE ip.parent_poi_pid = p.parent_poi_pid);
      INSERT INTO ix_poi_parent
        SELECT * FROM temp_ix_poi_parent_mg;
  
    --����ӱ�    
    INSERT INTO ix_poi_children
      SELECT group_id, child_poi_pid, relation_type, u_record, u_fields
        FROM (SELECT ipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields,
                     row_number() over(PARTITION BY ipp.group_id, c.child_poi_pid, c.relation_type ORDER BY 1) AS rn
                FROM temp_his_ix_poi_ext l,
                     au_ix_poi_children  c,
                     ix_poi_parent       ipp,
                     au_ix_poi_parent    auipp
               WHERE c.child_poi_pid = l.pid
                 AND auipp.group_id = c.group_id
                 AND auipp.parent_poi_pid = ipp.parent_poi_pid
                 AND auipp.Field_Task_Id=C.FIELD_TASK_ID
                 AND EXISTS (SELECT 1
                        FROM au_ix_poi au
                       WHERE au.audata_id = c.audata_id
                         AND au.att_oprstatus in( 0,1))) rs
       WHERE rs.rn = 1;
       commit;
  END;
  --
  PROCEDURE att_add_restaurant_ext IS
  BEGIN
   /* DELETE FROM ix_poi_restaurant ipr
     WHERE EXISTS
     (SELECT 1 FROM temp_his_ix_poi_ext l WHERE l.pid = ipr.poi_pid);
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
        FROM au_ix_poi_restaurant auipr
       WHERE EXISTS (SELECT 1
                FROM temp_his_ix_poi_ext l
               WHERE l.pid = auipr.poi_pid) 
             AND EXISTS (SELECT 1
                  FROM au_ix_poi au
                 WHERE au.pid = auipr.poi_pid
                   AND au.att_oprstatus in( 0,1))'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_RESTAURANT', v_pid_count);
      INSERT INTO ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,U_RECORD,U_FIELDS)
        SELECT pid_man.pid_nextval('IX_POI_RESTAURANT') AS restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
              
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_restaurant auipr
         WHERE EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext l
                 WHERE l.pid = auipr.poi_pid)
           AND EXISTS (SELECT 1
                  FROM au_ix_poi au
                 WHERE au.pid = auipr.poi_pid
                   AND au.att_oprstatus in( 0,1));
    END IF; */

     merge into ix_poi_restaurant t
     using (select l.pid, au.food_type
             from au_ix_poi_restaurant au, temp_his_ix_poi_ext  l
            where l.pid = au.poi_Pid ) rs
     on (t.poi_Pid = rs.pid) when matched then
     update set t.food_type = rs.food_type;

     -- ��ҵ�����ڲ�������ҵ���ڲ�����
      update ix_poi_restaurant ipr set ipr.food_type = null where not exists
     (
           select 1 from au_ix_poi_restaurant aipr where aipr.poi_pid = ipr.poi_pid
       ) and exists(
           select 1 from temp_his_ix_poi_ext t where t.pid = ipr.poi_Pid 
       );

        INSERT INTO ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
        SELECT restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
               0 AS TRAVELGUIDE_FLAG,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_restaurant auipr
           WHERE EXISTS (SELECT 1
                  FROM temp_his_ix_poi_ext l
                 WHERE l.pid = auipr.poi_pid)
           AND EXISTS (SELECT 1
                  FROM au_ix_poi au
                WHERE au.pid = auipr.poi_pid and auipr.audata_id = au.audata_id) 
           and not exists
          (select 1 from ix_poi_restaurant t where t.poi_pid = auipr.poi_pid);
    
    commit;
  
  END;
 
  PROCEDURE geo_add_poi_ext IS
  BEGIN
    --�ں���������           
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
             ip.pmesh_id= rs.pmesh_id,
             ip.region_id= rs.region_id
             ;
             commit;
  END;

  PROCEDURE mul_mod_poi_address(v_audata_id NUMBER) IS
  BEGIN
     /*ͨ��POI_PID�ж�AU_IX_POI_ADDRESS�����Ƿ������ݣ�
    1.���û�����ݣ���ͨ��POI_PIDɾ����POI��ȫ����ַ���������ġ�Ӣ�ġ����ĵ�ַ���Լ���ַ��Ӧ��FLAG_CODE��ʶ�������±���*/
    DELETE FROM ix_poi_address c1
     WHERE NOT EXISTS (SELECT 1
              FROM au_ix_poi_address t
             WHERE t.audata_id = v_audata_id
               AND t.poi_pid = c1.poi_pid)
       AND EXISTS (SELECT 1
              FROM au_ix_poi t
             WHERE t.audata_id = v_audata_id
               AND t.pid = c1.poi_pid)
       AND c1.lang_code IN ('CHI', 'CHT', 'ENG', 'POR');
    DELETE FROM IX_POI_FLAG c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid)
       and not exists(select 1 from AU_IX_POI_ADDRESS T WHERE T.audata_id=v_audata_id and t.poi_pid=c1.poi_pid)
       AND c1.FLAG_CODE IN   ( '110030010000',
                               '110030020000',
                               '110030030000',
                               '110030040000',
                               '110030050000',
                               '110030060000',
                               '110030070000',
                               '110030080000',
                               '110030090000',
                               '110030100000',
                               '110030110000',
                               '110030120000');
     /*
    ͨ��POI_PID�ж�AU_IX_POI_ADDRESS�����Ƿ������ݣ�
    2.��������ݣ���ͨ��POI_PID�ж�IX_POI_ADDRESS�����Ƿ�������Դ���ΪCHI��CHT�ĵ�ַ���ݣ�
    a����������ڵ�ַ������һ����ַ��¼��
    b�����ĸ����ڵ�ַ
    ?  �޸��������ĵ�ַ��¼�����ݣ�
    ?  ɾ����ӦӢ�ĺ����ĵ�ַ��¼��
    ?  ͬʱͨ��POI_PIDɾ��IX_POI_FLAG����FLAG_CODE Ϊ����ֵ�ļ�¼�����û�У��򲻴���

    */
    --��ҵ����ڲ���ĸ��Ҳ���ڵ�����£�ɾ����ӦӢ�ĺ����ĵ�ַ��¼��
    DELETE FROM IX_POI_ADDRESS c1
     WHERE exists(select 1 from au_ix_poi_address a where audata_id=v_audata_id and c1.poi_pid=a.poi_pid)
      AND EXISTS(SELECT 1 FROM IX_POI_ADDRESS T WHERE C1.POI_PID=T.POI_PID AND T.LANG_CODE in('CHI','CHT'))
      AND C1.LANG_CODE IN('ENG', 'POR');
      --��ҵ����ڲ���ĸ��Ҳ���ڵ�����£�ɾ����Ӧpoi_flag��
    DELETE FROM IX_POI_FLAG c1
     WHERE  exists(select 1 from au_IX_POI_ADDRESS T WHERE audata_id=v_audata_id and c1.poi_pid=t.poi_pid)
       and exists(select 1 from IX_POI_ADDRESS T WHERE T.POI_PID=C1.POI_PID and t.lang_code in('CHI','CHT'))
       AND c1.FLAG_CODE IN   ( '110030010000',
                               '110030020000',
                               '110030030000',
                               '110030040000',
                               '110030050000',
                               '110030060000',
                               '110030070000',
                               '110030080000',
                               '110030090000',
                               '110030100000',
                               '110030110000',
                               '110030120000');
    --��������ڵ�ַ������һ����ַ��¼��                               
    DELETE FROM ix_poi_address c1
     WHERE  exists(select 1 from au_ix_poi_address t where t.audata_id=v_audata_id and c1.poi_pid=t.poi_pid)
      AND c1.lang_code IN ('CHI', 'CHT');

      --������ POI���ơ�POIƴ����OLD���� (POINAME��POIPY)
      INSERT INTO ix_poi_address
        SELECT name_id,
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
           
           
           -- ɾ����Ӧ��Ӣ�ĺ����ĵ�ַ��¼     
       delete from ix_poi_address
      where poi_pid in
            (SELECT distinct poi_pid
               from au_ix_poi_address c1
              WHERE c1.audata_id = v_audata_id
                AND c1.lang_code IN ('CHI', 'CHT'))
        and lang_code IN ('ENG', 'POR');

    commit;
  END;
  PROCEDURE mul_mod_poi_restaurant(v_audata_id NUMBER) IS
  BEGIN
    /*EXECUTE IMMEDIATE 'DELETE FROM ix_poi_restaurant ipr
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.pid = ipr.poi_pid
               AND l.audata_id=:v_audata_id)'
      USING v_audata_id;
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
        FROM au_ix_poi_restaurant auipr
       WHERE auipr.audata_id=:v_audata_id'
      INTO v_pid_count
      USING v_audata_id;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_RESTAURANT', v_pid_count);
      EXECUTE IMMEDIATE 'INSERT INTO ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,U_RECORD,U_FIELDS)
        SELECT pid_man.pid_nextval(''IX_POI_RESTAURANT'') AS restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
            
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_restaurant auipr
         WHERE auipr.audata_id=:v_audata_id'
        USING v_audata_id;
    END IF; */
    
    
       EXECUTE IMMEDIATE 'merge into ix_poi_restaurant t
        using (select l.pid, au.food_type
                 from au_ix_poi_restaurant au, au_ix_poi l
                where l.pid = au.poi_Pid
                and l.audata_id = au.audata_id
                and l.att_oprstatus in(0,1)
                and l.audata_id = :v_audata_id) rs
        on (t.poi_Pid = rs.pid) when matched then
        update set t.food_type = rs.food_type' USING v_audata_id;
 
 
       -- ��ҵ�����ڲ�������ҵ���ڲ�����
      EXECUTE IMMEDIATE '  update ix_poi_restaurant ipr set ipr.food_type = null where not exists
       (
           select 1 from au_ix_poi_restaurant aipr where aipr.poi_pid = ipr.poi_pid
       ) and exists(
           SELECT 1 FROM  au_ix_poi l WHERE
               l.att_oprstatus in(0,1) and
               l.pid = ipr.poi_pid and l.audata_id = :v_audata_id
       )' USING v_audata_id;


       -- ��ҵ���ڲ�������ҵ�����ڲ�����
         EXECUTE IMMEDIATE ' insert into ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
          select restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
               0 AS TRAVELGUIDE_FLAG,
               0 AS u_record,
               NULL AS u_fields from au_ix_poi_restaurant au
           where au.audata_id = :v_audata_id 
            and exists(
           SELECT 1 FROM  au_ix_poi l WHERE
               l.att_oprstatus in(0,1) and
               l.pid = au.poi_pid and l.audata_id = au.audata_id)
             and not exists
           (select 1 from ix_poi_restaurant t where t.poi_pid = au.poi_pid)' USING v_audata_id;

    commit;
  
  END;
  PROCEDURE mul_mod_poi_name_add(v_data_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    /* DELETE FROM temp_ix_poi_name_mg;*/ --Ϊ����������������ʳ��������������
    --IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĳ�����,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2��������һ����¼
    --����PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_poi_name auipn
     WHERE auipn.audata_id = v_data_id
       AND auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND NOT EXISTS (SELECT t.poi_pid
              FROM ix_poi_name t
             WHERE t.poi_pid = auipn.poi_pid
               AND t.lang_code IN ('CHI', 'CHT')
               AND t.name_class = 2);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_pid_count);
      INSERT INTO temp_ix_poi_name_mg WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT pid_man.pid_nextval('IX_POI_NAME') AS name_id,
             auipn.poi_pid,
             nvl(rs.name_groupid, 1) AS name_groupid, 
             auipn.name_class,
             auipn.NAME_TYPE ,
             auipn.lang_code,
             auipn.name,
             auipn.name_phonetic,
             auipn.keywords,
             auipn.nidb_pid,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
          FROM au_ix_poi_name auipn, rs
         WHERE auipn.audata_id = v_data_id
           AND auipn.poi_pid = rs.poi_pid(+)
           AND auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND NOT EXISTS (SELECT t.poi_pid
                  FROM ix_poi_name t
                 WHERE t.poi_pid = auipn.poi_pid
                   AND t.lang_code IN ('CHI', 'CHT')
                   AND t.name_class = 2);
    
    END IF;
    copy_name_class1_data();
    /*INSERT INTO ix_poi_name
    SELECT * FROM temp_ix_poi_name_mg;*/
  commit;
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
  commit;
  END;
  /*
  //IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĵ���,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2�����޸�ix_poi_name ��NAME��NAME_PHONETIC ��ֵ<br/>
  //--ͬʱ����Lang_CodeΪ��CHI������������Ϊ��׼��1����NAME��NAME_PHONETIC��ֵ<br/>
  // -IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĳ�����,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2��������һ����¼     *
  */
  PROCEDURE mul_mod_poi_name(v_data_id NUMBER) IS
  BEGIN
    --IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĵ��ڣ�����au_ix_poi_name��û�����ݵģ���IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2��������ɾ��
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
    --IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĵ���,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2�����޸�ix_poi_name ��NAME��NAME_PHONETIC ��ֵ
  
    MERGE INTO ix_poi_name ipn
    USING (SELECT * FROM view_mg_mul_ix_poi_name WHERE audata_id = v_data_id) aurs
    ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = aurs.name_class)
    WHEN MATCHED THEN
      UPDATE
         SET ipn.name = aurs.name, ipn.name_phonetic = aurs.name_phonetic;
    --ͬʱ����Lang_CodeΪ��CHI������������Ϊ��׼��1����NAME��NAME_PHONETIC��ֵ
    MERGE INTO ix_poi_name ipn
    USING (SELECT * FROM view_mg_mul_ix_poi_name WHERE audata_id = v_data_id) aurs
    ON (ipn.poi_pid = aurs.poi_pid AND ipn.lang_code = aurs.lang_code AND ipn.name_class = 1)
    WHEN MATCHED THEN
      UPDATE
         SET ipn.name = aurs.name, ipn.name_phonetic = aurs.name_phonetic;
    --���name_class=1�����ݲ����ڣ���Ҫ��name_class=2��copyһ������
    mul_mod_poiname_add_c1(v_data_id);
    commit;
  END;
  
  PROCEDURE mul_mod_poi_contact(v_data_id NUMBER) IS
  BEGIN
    --��ֻ����ҵ�ɹ���Ĳ�����ҵ�⣨CONTACT����CONTACT_TYPE��ֵΪ1
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
         AND c1.contact_type in (1,2)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact
                 AND ipc.contact_type in (1,2));
    --��ֻ����ҵ�����е�ɾ��, ͬʱͨ��POI_PIDɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110040010000, 110040020000 ,110040030000 �ļ�¼�����û�У��򲻴���          
     delete from IX_POI_FLAG ipf   -- ɾ�� IX_POI_FLAG �м�¼
      where ipf.poi_pid in 
      (select c1.poi_pid
            FROM ix_poi_contact c1
     WHERE c1.contact_type in (1,2) and EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_data_id
               AND c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.audata_id = v_data_id
               AND auipc.contact = c1.contact AND auipc.contact_type in (1,2)))
                    and ipf.flag_code in ('110040010000', '110040020000' ,'110040030000');     
    --��ֻ����ҵ�����е�ɾ��
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_data_id
               AND c1.poi_pid = l.pid)
       AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.audata_id = v_data_id
               AND auipc.contact = c1.contact
               AND auipc.contact_type in (1,2)) 
       AND c1.contact_type in (1,2)/*ֻ�ں���ͨ�̻����ƶ��绰*/;
  commit;
  END;
  PROCEDURE mul_mod_ix_poi_state(v_data_id NUMBER) IS
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
                  p2.old_address
             FROM au_ix_poi p2
            WHERE p2.audata_id = v_data_id) v
    ON (p1.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE SET p1.log = v.log, p1.state = 2;
    commit;  
  END;

  PROCEDURE mul_att_mod_ix_poi(v_data_id    NUMBER,
                               kindflag     NUMBER,
                               labelflag    NUMBER,
                               postcodeflag NUMBER,
                               addresflag   NUMBER,
                               open24hflag  NUMBER,
                               nameflag     NUMBER,
                               isVerifiedFlag number) IS
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
                  p2.open_24h,
                  p2.verified_flag
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
             p1.open_24h    = decode(open24hflag, 1, v.open_24h,p1.open_24h),
             p1.old_name = decode(nameflag, 1, v.auoldname, p1.old_name),
             p1.verified_flag = decode(isVerifiedFlag, 1, v.verified_flag, p1.verified_flag);
    commit;
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
                  p2.pmesh_id,
                  p2.region_id
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
              p1.region_id = decode(displaypointflag,
                                  0,
                                  p1.region_id,
                                  v.region_id),                                                                                        
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
                                  commit;
  END;

  PROCEDURE mul_att_add_poi_ext(v_audata_id NUMBER) IS
  BEGIN
    --�ں���������           
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
             --ip.region_id    = rs.region_id,
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
             ip.field_task_id = rs.field_task_id,
             ip.verified_flag = rs.verified_flag;
             commit;
  END;
  PROCEDURE mul_geo_add_poi_ext(v_audata_id NUMBER) IS
  BEGIN
    --�ں���������           
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
             ip.pmesh_id = rs.pmesh_id,
             ip.region_id = rs.region_id;
             commit;
  END;
  PROCEDURE mul_att_add_poiname_ext_add(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
    /*DELETE FROM temp_ix_poi_name_mg; --Ϊ����������������ʳ��������������*/
    --IX_POI_NAME����Lang_CodeΪ��CHI������������Ϊԭʼ��2���Ĳ�����,����au_ix_poi_name�����Ʒ���Ϊԭʼ��2��������һ����¼
    --����PID
    SELECT COUNT(1)
      INTO v_pid_count
      FROM au_ix_poi_name auipn
     WHERE auipn.lang_code IN ('CHI', 'CHT')
       AND auipn.name_class = 2
       AND EXISTS (SELECT 1
              FROM au_ix_poi tmp
             WHERE tmp.audata_id = v_audata_id
               AND tmp.pid = auipn.poi_pid)
       AND NOT EXISTS (SELECT *
              FROM ix_poi_name ipn
             WHERE ipn.lang_code IN ('CHI', 'CHT')
               AND ipn.name_class = 2
               AND ipn.poi_pid = auipn.poi_pid);
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_NAME', v_pid_count);
      INSERT INTO temp_ix_poi_name_mg WITH rs AS
        (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
           FROM ix_poi_name
          GROUP BY poi_pid)
        SELECT  pid_man.pid_nextval('IX_POI_NAME') AS name_id,
             auipn.poi_pid,
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
          FROM au_ix_poi_name auipn, rs
         WHERE auipn.audata_id = v_audata_id
           AND auipn.poi_pid = rs.poi_pid(+)
           AND auipn.lang_code IN ('CHI', 'CHT')
           AND auipn.name_class = 2
           AND EXISTS (SELECT 1
                  FROM au_ix_poi tmp
                 WHERE tmp.audata_id = v_audata_id
                   AND tmp.pid = auipn.poi_pid)
           AND NOT EXISTS (SELECT *
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN ('CHI', 'CHT')
                   AND ipn.name_class = 2
                   AND ipn.poi_pid = auipn.poi_pid);
    END IF;
    copy_name_class1_data();
    /*INSERT INTO ix_poi_name
    SELECT * FROM temp_ix_poi_name_mg;*/
     commit;
  END;
  PROCEDURE mul_att_add_poiname_ext2(v_audata_id NUMBER) IS
    v_pid_count number;
    begin
    
    -- execute immediate 'truncate table temp_ix_poi_name_mg'; 
 --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI����NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊԭʼ��2���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼�������޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�
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
           and au.lang_code in(''CHI'', ''CHT'') 
	   and  au.name_type=2
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
            and au.lang_code in(''CHI'', ''CHT'') 
	   and  au.name_type=2
           and exists(select 1 from ix_poi_name ipn where ipn.name_id=au.name_id) 
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 2
                   AND ipn.poi_pid = au.poi_pid)'using v_audata_id,v_audata_id;
  end if;               
  /*INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;*/
       commit;
  commit;    
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('����POI����ʱ����' || SQLERRM);
      --rollback;
      RAISE;               
  END;
  PROCEDURE mul_att_add_poiname_ext(v_audata_id NUMBER) IS
    v_pid_count number;
    begin   
      --execute immediate 'truncate table temp_ix_poi_name_mg'; 
    --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI���� NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊ��׼��1���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼�������޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�
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
	        and au.lang_code in(''CHI'', ''CHT'') 
		and  au.name_type=2
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
		and au.lang_code in(''CHI'', ''CHT'')
		and au.name_type=2
           AND NOT EXISTS (SELECT 1
                  FROM ix_poi_name ipn
                 WHERE ipn.lang_code IN (''CHI'', ''CHT'')
                   AND name_class = 1
                   AND name_type = 1
                   AND ipn.poi_pid = au.poi_pid)' using v_audata_id,v_audata_id;
  end if;
    /*INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;*/
  commit;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('����POI����ʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;
  
  
  PROCEDURE mul_att_delete_poiname_flag(v_audata_id NUMBER) IS
    v_pid_count number;
    begin
    delete from temp_ix_poi_name_mg;
     --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��CHI���� NAME_CLASSΪ�ٷ����ƣ�1�����������ͣ�NAME_TYPE��Ϊ��׼��1���ļ�¼�Ƿ���ڣ���������ڣ�������һ����¼��
     -- �����޸ĸü�¼��NAME��NAME_PHONETIC��ֵ��ͬʱ����IX_POI��old_name�ֶ�,��ɾ������NAME_ID��Ӧ��IX_POI_NAME_FLAG����FLAG_CODE Ϊ
     -- 110010010000 ,110010020000 ,110010030000 ,110010040000 �ļ�¼�����û�У��򲻴���
     
     delete from IX_POI_NAME_FLAG a
         where exists
         (select 1
                  from ix_poi_name t
                 where exists (SELECT 1
                          FROM au_ix_poi_name au
                          WHERE au.audata_id = v_audata_id
                           and au.lang_code in ('CHI', 'CHT')
                           and au.name_type = 2
                           AND t.poi_pid = au.poi_pid)
                   and t.lang_code IN ('CHI', 'CHT')
                   AND t.name_class = 1
                   AND t.name_type = 1
                   and t.name_id = a.name_id)
           and a.flag_code in
               ('110010010000', '110010020000', '110010030000', '110010040000');  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('���� POI_NAME_FLAG ʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;
  
  
  
   /*PROCEDURE mul_att_delete_poiname_en_flag(v_audata_id NUMBER) IS
        v_pid_count number;
    BEGIN
     --ͨ��POI_PID�ж�IX_POI_NAME����Lang_CodeΪ��ENG/POR����NAME_CLASS = 1���ٷ����ƣ��ļ�¼�Ƿ���ڣ�
     --������ڣ�����ն�Ӧ��name���ݣ���ɾ������NAME_ID��Ӧ��IX_POI_NAME_FLAG����FLAG_CODE Ϊ
     -- 110020010000 
     -- 110020020000 
     -- 110020030000 
     -- 110020040000 
     -- 110020050000 
     -- 110020060000 
     -- 110020070000 
     -- 110020080000 
     -- 110020090000 
     --�ļ�¼����������ڣ��򲻴���
      delete from IX_POI_NAME_FLAG a
      where exists
     (select 1
              from ix_poi_name t
             where exists (SELECT 1
                      FROM  au_ix_poi aip
                     WHERE aip.pid = t.poi_pid and aip.audata_id = v_audata_id)
               and t.lang_code IN ('ENG', 'POR')
               AND t.name_class = 1
               and t.name_id = a.name_id)
       and a.flag_code in
       (110020010000,110020020000,110020030000,110020040000,110020050000,110020060000,110020070000,110020080000,110020090000 );    

    EXCEPTION
        WHEN OTHERS THEN
          dbms_output.put_line('���� POI_NAME_FLAG ʱ����' || SQLERRM);
          --rollback;
          RAISE;
    END; */
  
   
  PROCEDURE mul_att_add_address_ext(v_audata_id NUMBER) IS
  BEGIN
     /*ͨ��POI_PID�ж�AU_IX_POI_ADDRESS�����Ƿ������ݣ�
    1.���û�����ݣ���ͨ��POI_PIDɾ����POI��ȫ����ַ���������ġ�Ӣ�ġ����ĵ�ַ���Լ���ַ��Ӧ��FLAG_CODE��ʶ�������±���*/
    /*DELETE FROM IX_POI_ADDRESS C1
    WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid)
    AND NOT EXISTS(SELECT 1 FROM AU_IX_POI_ADDRESS T WHERE T.POI_PID=C1.POI_PID)
    AND c1.lang_code IN ('CHI', 'CHT','ENG', 'POR')   ;
    DELETE FROM IX_POI_FLAG c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid)
       and not exists(select 1 from AU_IX_POI_ADDRESS T WHERE T.POI_PID=C1.POI_PID)
       AND c1.FLAG_CODE IN   ( '110030010000',
                               '110030020000',
                               '110030030000',
                               '110030040000',
                               '110030050000',
                               '110030060000',
                               '110030070000',
                               '110030080000',
                               '110030090000',
                               '110030100000',
                               '110030110000',
                               '110030120000');*/
     /*
    ͨ��POI_PID�ж�AU_IX_POI_ADDRESS�����Ƿ������ݣ�
    2.��������ݣ���ͨ��POI_PID�ж�IX_POI_ADDRESS�����Ƿ�������Դ���ΪCHI��CHT�ĵ�ַ���ݣ�
    a����������ڵ�ַ������һ����ַ��¼��
    b�����ĸ����ڵ�ַ
    ?  �޸��������ĵ�ַ��¼�����ݣ�
    ?  ɾ����ӦӢ�ĺ����ĵ�ַ��¼��
    ?  ͬʱͨ��POI_PIDɾ��IX_POI_FLAG����FLAG_CODE Ϊ����ֵ�ļ�¼�����û�У��򲻴���
    */
    DELETE FROM ix_poi_address c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi_address l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.poi_pid)
       AND c1.lang_code IN ('CHI', 'CHT');
    
      --������ POI���ơ�POIƴ����OLD���� (POINAME��POIPY)
      EXECUTE IMMEDIATE 'INSERT INTO ix_poi_address
        SELECT name_id,
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
     commit;
  END;
  
  
  
  PROCEDURE mul_att_add_poi_label_ext(v_audata_id NUMBER) IS
  BEGIN
	-- �ں�ʱ�����AU_IX_POI���У�log�ֶΰ��������ڲ�POI������ô�ںϺ���IX_POI_FLAG�в���һ����¼110000030000��������ڣ��򲻲��룩
    INSERT INTO ix_poi_flag ipf
      SELECT au.pid, '110000030000', 0 AS u_record, NULL AS u_fields
        FROM au_ix_poi au
        WHERE au.audata_id=v_audata_id
        and au.state=3 and au.att_oprstatus=0
       AND EXISTS (SELECT 1
            FROM au_ix_poi_flag b
           WHERE b.poi_pid = au.pid
             AND b.flag_code = '110000030000')
        AND NOT EXISTS (SELECT 1
            FROM ix_poi_flag c
           WHERE c.poi_pid = au.pid
             AND c.flag_code = '110000030000');
    -- �����ע�ֶΰ��������յ�ַ�����ںϺ���ɾ�����ĵ�ַ�ı�ʶ��Ϣ������IX_POI_FLAG���в��롰�̶���־����ʩ�����ַ��(FLAG_CODE =110030060000)�ı�ʶ��Ϣ  
    DELETE FROM IX_POI_FLAG c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.pid = c1.poi_pid
               AND instr(au.label, '���յ�ַ') > 0
               AND au.audata_id = v_audata_id)
       AND c1.FLAG_CODE IN ('110030010000',
                            '110030020000',
                            '110030030000',
                            '110030040000',
                            '110030050000',
                            '110030070000',
                            '110030080000',
                            '110030090000',
                            '110030100000',
                            '110030110000',
                            '110030120000');
      
        INSERT INTO ix_poi_flag
         SELECT a.pid, '110030060000', 0 AS u_record, NULL AS u_fields
           FROM au_ix_poi a
          WHERE instr(a.label, '���յ�ַ') > 0
           AND a.audata_id = v_audata_id
            AND NOT EXISTS (SELECT 1
                   FROM ix_poi_flag b
                  WHERE a.pid = b.poi_pid
                    AND b.flag_code = '110030060000');
      commit;
  END;
  
  
  
  PROCEDURE mul_att_add_contact_ext(v_audata_id NUMBER) IS
  BEGIN
    --��ֻ����ҵ�ɹ���Ĳ�����ҵ�⣨CONTACT����CONTACT_TYPE��ֵΪ1
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
       	 AND c1.contact_type in (1,2)
         AND NOT EXISTS (SELECT 1
                FROM ix_poi_contact ipc
               WHERE ipc.poi_pid = c1.poi_pid
                 AND ipc.contact = c1.contact
                 AND ipc.contact_type in (1,2));
    --��ֻ����ҵ�����е�ɾ��
    DELETE FROM ix_poi_contact c1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c1.poi_pid = l.pid
               AND NOT EXISTS (SELECT 1
              FROM au_ix_poi_contact auipc
             WHERE auipc.audata_id = l.audata_id
               AND auipc.contact = c1.contact
               AND auipc.contact_type in (1,2)))       
       AND c1.contact_type in (1,2)/*ֻ�ں���ͨ�̻����ƶ��绰*/;
      commit;
  END;

  PROCEDURE mul_att_add_relation_ext(v_audata_id NUMBER) IS
  BEGIN

    --execute immediate 'truncate table temp_ix_poi_parent_mg'; 
    DELETE FROM ix_poi_children ipc
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi au
             WHERE au.audata_id = v_audata_id
               AND au.pid = ipc.child_poi_pid);

      --1.�޸ĵ����ӣ��������Ӷ�copy����ҵ��  
      merge into ix_poi_parent ipp
      using (select p.group_id, p.TENANT_FLAG
               FROM au_ix_poi_parent p, au_ix_poi_children c
              WHERE c.audata_id = v_audata_id
                AND p.group_id = c.group_id
                AND p.field_task_id = c.field_task_id) aurs
      on (ipp.group_id = aurs.group_id)
      when matched then
        update set ipp.TENANT_FLAG = aurs.TENANT_FLAG;
       
      INSERT INTO ix_poi_parent--temp_ix_poi_parent_mg
        SELECT p.group_id,
               p.parent_poi_pid,
               tenant_flag,
               null as memo,
               0, --U_RECORD,
               NULL --U_FIELDS
          FROM au_ix_poi_parent p, au_ix_poi_children c
         WHERE c.audata_id = v_audata_id
           AND p.group_id = c.group_id
           AND p.field_task_id = c.field_task_id
           AND NOT EXISTS
         (SELECT 1
                  FROM ix_poi_parent ip
                 WHERE ip.parent_poi_pid = p.parent_poi_pid);
     /* INSERT INTO ix_poi_parent
        SELECT * FROM temp_ix_poi_parent_mg;*/
  
    --����ӱ�    
    INSERT INTO ix_poi_children
      SELECT group_id, child_poi_pid, relation_type, u_record, u_fields
        FROM (SELECT auipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields
              FROM au_ix_poi_children c, au_ix_poi_parent auipp, ix_poi_parent ipp
              WHERE c.audata_id = v_audata_id 
					and auipp.field_task_id = c.field_task_id 
					and auipp.group_id = c.group_id 
					and auipp.parent_poi_pid = ipp.parent_poi_pid 
			  union
			  select ipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields
              FROM au_ix_poi_children c, ix_poi_parent ipp 
              where c.audata_id = v_audata_id 
              		and c.group_id = ipp.group_id
                    and not exists (
                    	select 1 from au_ix_poi_parent auipp where auipp.group_id=c.group_id
                    	and auipp.field_task_id = c.field_task_id));
        commit;
  END;
  PROCEDURE mul_att_add_restaurant_ext(v_audata_id NUMBER) IS
  BEGIN
   /* DELETE FROM ix_poi_restaurant ipr
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND l.pid = ipr.poi_pid);
    EXECUTE IMMEDIATE 'SELECT COUNT(1)  
        FROM au_ix_poi_restaurant auipr
       WHERE auipr.audata_id=:v_audata_id'
      INTO v_pid_count
      USING v_audata_id;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('IX_POI_RESTAURANT', v_pid_count);
      INSERT INTO ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,U_RECORD,U_FIELDS)
        SELECT pid_man.pid_nextval('IX_POI_RESTAURANT') AS restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
   
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_restaurant auipr
         WHERE auipr.audata_id = v_audata_id;
    END IF; */
    
  
       EXECUTE IMMEDIATE 'merge into ix_poi_restaurant t
        using (select l.pid, au.food_type
                 from au_ix_poi_restaurant au, au_ix_poi l
                where l.pid = au.poi_Pid
                and l.audata_id = au.audata_id
                and l.att_oprstatus in(0,1)
                and l.audata_id = :v_audata_id) rs
        on (t.poi_Pid = rs.pid) when matched then
        update set t.food_type = rs.food_type' USING v_audata_id;
 
 
       -- ��ҵ�����ڲ�������ҵ���ڲ�����
      EXECUTE IMMEDIATE ' update ix_poi_restaurant ipr set ipr.food_type = null where not exists
       (
           select 1 from au_ix_poi_restaurant aipr where aipr.poi_pid = ipr.poi_pid
       ) and exists(
           SELECT 1 FROM  au_ix_poi l WHERE
               l.att_oprstatus in(0,1) and
               l.pid = ipr.poi_pid and l.audata_id = :v_audata_id
       )' USING v_audata_id;


       -- ��ҵ���ڲ�������ҵ�����ڲ�����
         EXECUTE IMMEDIATE ' insert into ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
          select restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
               0 AS TRAVELGUIDE_FLAG,
               0 AS u_record,
               NULL AS u_fields from au_ix_poi_restaurant au
           where au.audata_id = :v_audata_id 
           and exists(
           SELECT 1 FROM  au_ix_poi l WHERE
               l.att_oprstatus in(0,1) and
               l.pid = au.poi_pid and l.audata_id = au.audata_id
       )
             and not exists
           (select 1 from ix_poi_restaurant t where t.poi_pid = au.poi_pid)' USING v_audata_id;
    
     commit;
  END;
  PROCEDURE mul_add_address(v_audata_id NUMBER) IS
  BEGIN   
    --execute immediate 'truncate table temp_ix_poi_address_mg';
      INSERT INTO ix_poi_address--temp_ix_poi_address_mg
        SELECT c.name_id,
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
     /* INSERT INTO ix_poi_address
        SELECT * FROM temp_ix_poi_address_mg;*/
     commit;
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
        commit;
  END;
  PROCEDURE mul_add_poiname(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
    BEGIN      
      -- execute immediate 'truncate table temp_ix_poi_name_mg';
      --����һ��name_type=2�ģ��������ݵ�name_id��������
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
               u_fields)
         WITH rs AS
            (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
               FROM ix_poi_name t1               
               where t1.poi_pid in(select poi_pid from au_ix_poi_name au where au.audata_id=:v_audata_id)
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
           'using v_audata_id,v_audata_id;
     end if; 
  /* INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;*/
       commit;
    END;
  PROCEDURE mul_add_poiname2(v_audata_id NUMBER) IS
    v_pid_count NUMBER;
  BEGIN
 
   --execute immediate 'truncate table temp_ix_poi_name_mg'; 
   --����һ��name_type=1��
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
         u_fields)
  WITH rs AS
      (SELECT poi_pid, nvl(MAX(name_groupid), 0) + 1 AS name_groupid
         FROM ix_poi_name t1
         where t1.poi_pid in(select poi_pid from au_ix_poi_name au where au.audata_id=:v_audata_id)
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
         and p.audata_id=:v_audata_id' using v_audata_id,v_audata_id;
    
   --����һ��name_type=2�ģ��������ݵ�name_id��������   
   /*INSERT INTO ix_poi_name
      SELECT * FROM temp_ix_poi_name_mg;*/
       commit;
  END;
  PROCEDURE mul_add_restaurant(v_audata_id NUMBER) IS
  BEGIN   
    --execute immediate 'truncate table temp_ix_poi_restaurant_mg';
      INSERT INTO ix_poi_restaurant(RESTAURANT_ID,POI_PID,FOOD_TYPE,CREDIT_CARD,AVG_COST,PARKING,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)  --temp_ix_poi_restaurant_mg
        SELECT restaurant_id,
               poi_pid,
               food_type,
               credit_card,
               avg_cost,
               parking,
               0 AS TRAVELGUIDE_FLAG,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_restaurant auipr
         WHERE auipr.audata_id = v_audata_id;
     /* INSERT INTO ix_poi_restaurant
        SELECT * FROM temp_ix_poi_restaurant_mg;*/
     commit;
  END;
  --��Ҫ�Ż�
  PROCEDURE mul_add_relation(v_audata_id NUMBER) IS
  BEGIN
    execute immediate 'select 1 from ix_poi_parent for update';
      --��Ҫ���ӵ�ix_poi_parent�ŵ���ʱ���Ա���������
      merge into ix_poi_parent ipp
      using (select p.group_id, p.TENANT_FLAG
               FROM au_ix_poi_parent p, au_ix_poi_children c
              WHERE c.audata_id = v_audata_id
                AND p.group_id = c.group_id
                AND p.field_task_id = c.field_task_id) aurs
      on (ipp.group_id = aurs.group_id)
      when matched then
        update set ipp.TENANT_FLAG = aurs.TENANT_FLAG;
        
      INSERT INTO ix_poi_parent--temp_ix_poi_parent_mg
        SELECT group_id,
               parent_poi_pid,
               TENANT_FLAG,
               null as  MEMO,
               0 AS u_record,
               NULL AS u_fields
          FROM au_ix_poi_parent p
         WHERE EXISTS (SELECT 1
                  FROM (SELECT c.group_id, l.field_task_id
                          FROM au_ix_poi l, au_ix_poi_children c
                         WHERE l.state = 3
                           AND l.att_oprstatus in( 0,1)
                           AND c.audata_id = l.audata_id
                           AND l.audata_id = v_audata_id
                           AND c.audata_id = v_audata_id) v
                 WHERE p.group_id = v.group_id
                   AND p.field_task_id = v.field_task_id)
           AND NOT EXISTS
         (SELECT 1
                  FROM ix_poi_parent ip
                 WHERE ip.parent_poi_pid = p.parent_poi_pid);
     /* INSERT INTO ix_poi_parent
        SELECT * FROM temp_ix_poi_parent_mg;*/
  
    --����ӱ�
    INSERT INTO ix_poi_children
      SELECT group_id, child_poi_pid, relation_type, u_record, u_fields
        FROM (SELECT ipp.group_id,
                     c.child_poi_pid,
                     c.relation_type,
                     0 AS u_record,
                     NULL AS u_fields,
                     row_number() over(PARTITION BY ipp.group_id, c.child_poi_pid, c.relation_type ORDER BY 1) AS rn
                FROM au_ix_poi          l,
                     au_ix_poi_children c,
                     ix_poi_parent      ipp,
                     au_ix_poi_parent   auipp
               WHERE l.state = 3
                 AND l.att_oprstatus in( 0,1)
                 AND c.child_poi_pid = l.pid
                 AND auipp.group_id = c.group_id
                 AND auipp.parent_poi_pid = ipp.parent_poi_pid
                 AND auipp.field_task_id = c.field_task_id
                 AND l.audata_id = v_audata_id
                 AND c.audata_id = v_audata_id) rs
       WHERE rs.rn = 1;
        commit;
  END;
  PROCEDURE mul_add_poi_flag(v_audata_id NUMBER) IS
  BEGIN
   --���AU_IX_POI_FLAG���д��ڼ�¼��������������Ӱ汾IX_POI_FLAG����:��ɾ����ҵ���Ѿ����ڵ�flag_code��Ȼ����ҵ�����е�flag_code���뵽�Ӱ汾��
  execute immediate '
  DELETE FROM ix_poi_flag t
  WHERE  EXISTS (SELECT 1
          FROM au_ix_poi_flag t2,au_ix_poi au
         WHERE t2.poi_pid = t.poi_pid and t2.audata_id=au.audata_id and au.audata_id=:v_audata_id
           AND t2.flag_code =t.flag_code)
  ' using v_audata_id ;

    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_FLAG WITH AURS AS
  (SELECT AU.PID, AUF.FLAG_CODE
     FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
    WHERE au.audata_id=:v_audata_id
      AND AU.AUDATA_ID = AUF.AUDATA_ID
   )
   SELECT AURS.PID,
         aurs.FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
  'using v_audata_id; 
 /*
   -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000200000,110000210000,110000220000 �ļ�¼����������ڣ��򲻴���
   -- ����ҵ����AU_IX_POI_FLAG���е�POI_LEVEL��Ϣ�����뵽IX_POI_FLAG����
   -- ����AU_IX_POI_FLAG��FLAG_CODE (110000110002)����NOKIA��֤��Ϣ��������ڣ��򲻲��룩
  execute immediate '
  DELETE FROM ix_poi_flag t
  WHERE t.flag_code IN (''110000200000'', ''110000210000'', ''110000220000'')
   AND EXISTS (SELECT 1
          FROM au_ix_poi_flag t2,au_ix_poi au
         WHERE t2.poi_pid = t.poi_pid and t2.audata_id=au.audata_id and au.audata_id=:v_audata_id
           AND t2.flag_code IN
               (''110000200000'', ''110000210000'', ''110000220000''))   
  ' using v_audata_id ;
  
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_FLAG WITH AURS AS
  (SELECT AU.PID, AUF.FLAG_CODE
     FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
    WHERE au.audata_id=:v_audata_id
      AND AU.AUDATA_ID = AUF.AUDATA_ID
   )
   SELECT AURS.PID,
         ''110000200000'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000200000''    
  UNION ALL
  SELECT AURS.PID,
         ''110000210000'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000210000''    
  UNION ALL
  SELECT AURS.PID,
         ''110000220000'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000220000''    
  UNION ALL
  SELECT AURS.PID,
         ''110000110002'' AS FLAG_CODE,
         0 AS U_RECORD,
         NULL AS U_FIELDS
    FROM AURS
   WHERE FLAG_CODE = ''110000110002''
     AND NOT EXISTS (SELECT 1
            FROM IX_POI_FLAG RS
           WHERE RS.POI_PID = AURS.PID
             AND RS.FLAG_CODE = ''110000110002'')  
  'using v_audata_id;
 */
  
  
   -- �ں�ʱ�����AU_IX_POI���У�log�ֶΰ��������ڲ�POI������ô�ںϺ���IX_POI_FLAG�в���һ����¼110000030000��������ڣ��򲻲��룩
  EXECUTE IMMEDIATE 'INSERT INTO ix_poi_flag ipf
  SELECT au.pid, ''110000030000'', 0 AS u_record, NULL AS u_fields
    FROM au_ix_poi au
   WHERE au.audata_id=:v_audata_id
     AND instr(au.log, ''���ڲ�POI'') > 0
     AND NOT EXISTS (SELECT 1
            FROM ix_poi_flag ipf
           WHERE ipf.poi_pid = au.pid
             AND ipf.flag_code = ''110000030000'')' using v_audata_id;

    --���LABEL�ֶΰ��������յ�ַ���������ںϺ���IX_POI_FLAG���У�����һ����¼��FLAG_CODE =110030060000���̶���־����ʩ�����ַ��
  EXECUTE IMMEDIATE 'INSERT INTO ix_poi_flag ipf
  SELECT au.pid, ''110030060000'', 0 AS u_record, NULL AS u_fields
    FROM au_ix_poi au
   WHERE au.audata_id=:v_audata_id
     AND instr(au.label, ''���յ�ַ'') > 0
     AND NOT EXISTS (SELECT 1
            FROM ix_poi_flag ipf
           WHERE ipf.poi_pid = au.pid
             AND ipf.flag_code = ''110030060000'')' using v_audata_id;

  
  
   commit;
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
       0, --TASK_ID,task_id��Ҫ���⴦��
       data_version,
       field_task_id,
       VERIFIED_FLAG,
       NULL,--COLLECT_TIME
       9,--GEO_ADJUST_FLAG
       9,--FULL_ATTR_FLAG
       0, --U_RECORD,
       NULL --U_FIELDS
        FROM au_ix_poi p
       WHERE p.audata_id = v_audata_id;
  
    mul_add_address(v_audata_id);
    mul_add_contact(v_audata_id);
    --mul_add_poiname(v_audata_id);
    mul_add_restaurant(v_audata_id);
    --������ҵ�Ŵ����ӹ�ϵ
    mul_add_relation(v_audata_id);
    --mul_add_poi_flag(v_audata_id);   
   --��Ҫ����IX_POIO_NAME_FLAG,IX_POI_HOTEL,IX_POI_BUILDING,IX_POI_PHOTO
    execute immediate '
    INSERT INTO ix_poi_name_flag
      SELECT auf.name_id, auf.flag_code, 0 AS u_record, NULL AS u_fields
        FROM au_ix_poi_name aun, au_ix_poi_name_flag auf
       WHERE aun.audata_id = :v_audata_id
         AND aun.auname_id = auf.auname_id
         ' using v_audata_id;
    --��Ҫ����AU_IX_POI_HOTEL
    execute immediate '
     INSERT INTO ix_poi_hotel(HOTEL_ID,POI_PID,CREDIT_CARD,RATING,CHECKIN_TIME,CHECKOUT_TIME,ROOM_COUNT,ROOM_TYPE,ROOM_PRICE,BREAKFAST,SERVICE,PARKING,LONG_DESCRIPTION,LONG_DESCRIP_ENG,OPEN_HOUR,OPEN_HOUR_ENG,TELEPHONE,ADDRESS,CITY,PHOTO_NAME,TRAVELGUIDE_FLAG,U_RECORD,U_FIELDS)
      SELECT hotel_id,
             poi_pid,
             credit_card,
             rating,
             checkin_time,
             checkout_time,
             room_count,
             room_type,
             room_price,
             breakfast,
             service,
             parking,
			 long_description,
			 long_descrip_eng,
			 open_hour,
			 open_hour_eng,
			 telephone,
			 address,
			 city,
			 photo_name,
             0             AS TRAVELGUIDE_FLAG,
             0             AS u_record,
             NULL          AS u_fields
        FROM au_ix_poi_hotel auh
       WHERE  auh.audata_id = :v_audata_id     
       ' using v_audata_id;

    --��Ҫ����AU_IX_POI_PHOTO
    execute immediate '
      INSERT INTO ix_poi_photo
  SELECT poi_pid, photo_id, status, memo, 0 as u_record, null as u_fields
    FROM au_ix_poi_photo aup
   WHERE aup.audata_id = :v_audata_id     
       ' using v_audata_id;
       
       -- �������IX_POI��VERIFIED_FLAG�ֶ�ֵΪ��2�����ߡ�3����POI����Flag_code��IX_POI_FLAG������������������ʾ����110000340000����������Ҫ�Ѹñ�ʾɾ��
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_FLAG c1
          WHERE EXISTS (SELECT 1
                   FROM au_ix_poi au, ix_poi i
                  WHERE au.pid = c1.poi_pid
                    and au.pid = i.pid
                    and au.att_oprstatus in (0,1)
                    and i.verified_flag in (2, 3)
                    and au.audata_id =:v_audata_id)
            AND c1.FLAG_CODE = ''110000340000''' using v_audata_id; 
            
        commit;
  
  END;
  PROCEDURE mul_add_poi_rel(v_audata_id NUMBER) IS
  BEGIN
    --���ӹ�ϵ
    INSERT INTO ix_poi_parent
      SELECT group_id,
             parent_poi_pid,
             TENANT_FLAG,
             null as  MEMO,
             0, --U_RECORD,
             NULL --U_FIELDS
        FROM au_ix_poi_parent p
       WHERE EXISTS
       (SELECT 1
                FROM (SELECT c.group_id
                        FROM au_ix_poi l, au_ix_poi_children c
                       WHERE l.audata_id = v_audata_id
                         AND c.child_poi_pid = l.pid) v
               WHERE p.group_id = v.group_id)
         AND p.group_id NOT IN (SELECT group_id FROM ix_poi_parent);
    INSERT INTO ix_poi_children
      SELECT c.group_id,
             c.child_poi_pid,
             c.relation_type,
             0, --c.U_RECORD,
             NULL --c.U_FIELDS
        FROM au_ix_poi l, au_ix_poi_children c
       WHERE l.audata_id = v_audata_id
         AND c.child_poi_pid = l.pid
         AND NOT EXISTS (SELECT 1
                FROM temp_au_ix_poi_mul_task tmp
               WHERE tmp.pid = l.pid);
                commit;
  END;
  PROCEDURE mul_del_poi(v_audata_id NUMBER) IS
  BEGIN
    --ɾ������;
    DELETE ix_poi poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.pid
               AND poi2.audata_id = v_audata_id);
    --ɾ��IX_POI_ADDRESS;
    DELETE ix_poi_address poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.poi_pid
               AND poi2.audata_id = v_audata_id);
    --ɾ��IX_POI_CONTACT;
    DELETE ix_poi_contact poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.poi_pid
               AND poi2.audata_id = v_audata_id);
    --ix_poi_name_tone        
   /* DELETE ix_poi_name_tone ipnt
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
    --ɾ��IX_POI_NAME;
    DELETE ix_poi_name poi1
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi poi2
             WHERE poi2.pid = poi1.poi_pid
               AND poi2.audata_id = v_audata_id);
    --ɾ�� ����;
    --����ɾ��ʱ����Ҫ���ӱ�Ҳɾ��
    DELETE FROM ix_poi_children c
     WHERE EXISTS (SELECT 1
              FROM (SELECT c.group_id
                      FROM au_ix_poi l, ix_poi_parent c
                     WHERE l.audata_id = v_audata_id
                       AND c.parent_poi_pid = l.pid) v
             WHERE c.group_id = v.group_id);
    ----ɾ�����Ǹ���;
    DELETE FROM ix_poi_parent p
     WHERE EXISTS (SELECT 1
              FROM (SELECT c.group_id
                      FROM au_ix_poi l, ix_poi_parent c
                     WHERE l.audata_id = v_audata_id
                       AND c.parent_poi_pid = l.pid) v
             WHERE p.group_id = v.group_id);
  
    --ɾ���ӱ�
    ----ɾ�����Ƕ���
    DELETE FROM ix_poi_children c
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND c.child_poi_pid = l.pid);
    --commit;
    --ix_poi_flag
    DELETE FROM ix_poi_flag ipf
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipf.poi_pid = l.pid);
    --delete X_POI_ENTRYIMAGE
    DELETE FROM ix_poi_entryimage ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipe.poi_pid = l.pid);
    --IX_POI_ICON
    DELETE FROM ix_poi_icon ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipe.poi_pid = l.pid);
    --IX_POI_PHOTO
    DELETE FROM ix_poi_photo ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipe.poi_pid = l.pid);
    --IX_POI_AUDIO
    DELETE FROM ix_poi_audio ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipe.poi_pid = l.pid);
    --IX_POI_VIDEO
    DELETE FROM ix_poi_video ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipe.poi_pid = l.pid);
    /*ͬһ���ϵ*/
    --IX_SAMEPOI_PART
    DELETE FROM ix_samepoi_part ipe
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipe.poi_pid = l.pid);
  
    /* --IX_SAMEPOI             
    DELETE FROM ix_samepoi p
     WHERE NOT EXISTS (SELECT 1
              FROM ix_samepoi_part isp
             WHERE isp.group_id = p.group_id);*/
    /*�����Ϣ*/
    --IX_POI_ADVERTISEMENT
  /*  DELETE FROM ix_poi_advertisement ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid); */
    --IX_POI_GASSTATION
  /*  DELETE FROM ix_poi_gasstation ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid); */
    --IX_POI_INTRODUCTION
    DELETE FROM ix_poi_introduction ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);
    --IX_POI_ATTRACTION  
    DELETE FROM ix_poi_attraction ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);
    --IX_POI_HOTEL
    DELETE FROM ix_poi_hotel ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);
    --IX_POI_RESTAURANT
    DELETE FROM ix_poi_restaurant ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);
  
    --IX_POI_CHARGINGPLOT
  /*  DELETE FROM ix_poi_chargingplot ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l, ix_poi_chargingstation ipc
             WHERE l.audata_id = v_audata_id
               AND ipc.poi_pid = l.pid
               AND ipc.charging_id = ipa.charging_id); */
    --IX_POI_CHARGINGSTATION
   /* DELETE FROM ix_poi_chargingstation ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid); */
    --IX_POI_CHARGINGSTATION
   /* DELETE FROM ix_poi_businesstime ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid); */
    --IX_POI_BUILDING
    DELETE FROM IX_POI_BUILDING ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);
    --IX_POI_DETAIL
  /*  DELETE FROM IX_POI_DETAIL ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);   */       
     --IX_POI_PARKING
   /* DELETE FROM IX_POI_PARKING ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid); */
     --IX_POI_TOURROUTE
   /* DELETE FROM IX_POI_TOURROUTE ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid); */
     --IX_POI_EVENT
   /* DELETE FROM IX_POI_EVENT ipa
     WHERE EXISTS (SELECT 1
              FROM au_ix_poi l
             WHERE l.audata_id = v_audata_id
               AND ipa.poi_pid = l.pid);   */
	       commit;                                      
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('ɾ��POIʱ����' || SQLERRM);
      --rollback;
      RAISE;
  END;
  PROCEDURE process_name_groupid IS
  BEGIN
    --���������name�ģ���Ҫ���´���name_groupid
    MERGE INTO ix_poi_name ipn
    USING (SELECT name_id,
                  poi_pid,
                  row_number() over(PARTITION BY poi_pid ORDER BY 1) rn
             FROM ix_poi_name ppn
            WHERE EXISTS (SELECT 1
                     FROM temp_ix_poi_name_mg mg
                    WHERE ppn.poi_pid = mg.poi_pid)) rs
    ON (ipn.name_id = rs.name_id)
    WHEN MATCHED THEN
      UPDATE SET ipn.name_groupid = rs.rn;
       commit;
  END;
  PROCEDURE mul_mod_poi_state_ext(v_pid   NUMBER,
                                  v_state NUMBER,
                                  v_log   ix_poi.log%TYPE) IS
  BEGIN
    UPDATE ix_poi p1
       SET p1.state = v_state, p1.log = v_log
     WHERE p1.pid = v_pid;
      commit;
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
                  commit;
  END;
  /*PROCEDURE mul_reset_temp_ix_poi(v_pid NUMBER) IS
  BEGIN
    DELETE FROM temp_his_ix_poi WHERE pid = v_pid;
    INSERT INTO temp_his_ix_poi
      SELECT * FROM ix_poi ip WHERE ip.pid = v_pid;
       commit;
  END;*/
  PROCEDURE del_ix_same_poi IS
  BEGIN
    --IX_SAMEPOI             
    DELETE FROM ix_samepoi p
     WHERE NOT EXISTS (SELECT 1
              FROM ix_samepoi_part isp
             WHERE isp.group_id = p.group_id);
              commit;
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
       commit;
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
       commit;
  END;
  procedure mul_reset_poi_building(v_audata_id number)is
    begin
       --reset temp_his_ix_poi_building;
        execute immediate '
        delete from temp_his_ix_poi_building t where t.poi_pid in(select pid from au_ix_poi au where au.audata_id=:v_audata_id)              
        ' using v_audata_id;
        execute immediate 'insert into temp_his_ix_poi_building select * from ix_poi_building where poi_pid in(select pid from au_ix_poi au where au.audata_id=:v_audata_id)' using v_audata_id;
         commit;
   end;
  procedure mul_reset_poi_parent(v_audata_id number)is
    begin
       --reset temp_his_ix_poi_building;
        execute immediate '
        delete from temp_his_ix_poi_parent t where t.parent_poi_pid in(select parent_poi_pid from au_ix_poi_parent au where au.audata_id=:v_audata_id)              
        ' using v_audata_id;
        execute immediate 'insert into temp_his_ix_poi_parent select * from ix_poi_parent where parent_poi_pid in(select parent_poi_pid from au_ix_poi_parent au where au.audata_id=:v_audata_id)' using v_audata_id;
         commit;
   end; 
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
                          AND au.att_oprstatus in(0,1)
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
                          AND au.att_oprstatus in(0,1)
                          AND l.name_flag = 1));
       
       DELETE FROM ix_poi_name t1
        WHERE t1.name_class = 5
          AND t1.poi_pid IN (SELECT au.pid
                               FROM au_ix_poi au, temp_au_poi_modify_log l
                              WHERE au.audata_id = l.audata_id
                                AND au.att_oprstatus in(0,1)
                                AND l.name_flag = 1);
                                 commit;
     end ;
      procedure process_ext_poi_name_delclass5 is
     begin
      /* delete from ix_poi_name_tone t2
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
      /*  execute immediate' delete from ix_poi_name_tone t2
       WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1,au_ix_poi au 
                WHERE t1.name_class = 5
                  AND t1.poi_pid =au.pid
                  AND au.att_oprstatus in(0,1)
                  and au.audata_id=:v_audata_id)' using v_audata_id ; */
       execute immediate ' DELETE FROM ix_poi_name_flag t2
        WHERE t2.name_id IN
              (SELECT name_id
                 FROM ix_poi_name t1,au_ix_poi au 
                WHERE t1.name_class = 5
                  AND t1.poi_pid =au.pid
                  AND au.att_oprstatus in(0,1)
                  and au.audata_id=:v_audata_id)' using v_audata_id;
       
       execute immediate 'DELETE FROM ix_poi_name t1
        WHERE t1.name_class = 5
          AND t1.poi_pid IN (SELECT au.pid
                               FROM au_ix_poi au
                              WHERE  au.att_oprstatus in(0,1)
                                AND au.audata_id=:v_audata_id)' using v_audata_id;
				commit;
       end;
       PROCEDURE mul_att_mod_poi_label(v_audata_id NUMBER) IS
       BEGIN
      -- �����ע�ֶΰ��������յ�ַ�����ںϺ���ɾ�����ĵ�ַ�ı�ʶ��Ϣ������IX_POI_FLAG���в��롰�̶���־����ʩ�����ַ��(FLAG_CODE =110030060000)�ı�ʶ��Ϣ 
        DELETE FROM IX_POI_FLAG c1
        WHERE EXISTS (SELECT 1
                 FROM au_ix_poi au
                WHERE au.pid = c1.poi_pid
                  AND au.audata_id = v_audata_id
                  and instr(au.label, '���յ�ַ') > 0)
          AND c1.FLAG_CODE IN ('110030010000',
                               '110030020000',
                               '110030030000',
                               '110030040000',
                               '110030050000',
                               '110030070000',
                               '110030080000',
                               '110030090000',
                               '110030100000',
                               '110030110000',
                               '110030120000');
 
         
         INSERT INTO ix_poi_flag
           SELECT a.pid, '110030060000', 0 AS u_record, NULL AS u_fields
             FROM au_ix_poi a
            WHERE instr(a.label, '���յ�ַ') > 0
             AND a.audata_id=v_audata_id
              AND NOT EXISTS (SELECT 1
                     FROM ix_poi_flag b
                    WHERE a.pid = b.poi_pid
                      AND b.flag_code = '110030060000');

         -- �����ע�ֶ��в����������յ�ַ���������ںϺ�ɾ��IX_POI_FLAG����FLAG_CODE =110030060000���̶���־����ʩ�����ַ���ļ�¼����������ڣ��򲻴���
         DELETE FROM ix_poi_flag ipf
          WHERE ipf.flag_code = '110030060000'
            AND EXISTS
          (SELECT 1
                   FROM au_ix_poi a
                  WHERE (a.label IS NULL OR instr(a.label, '���յ�ַ') = 0)
                    AND a.pid = ipf.poi_pid
                    AND a.audata_id=v_audata_id);
	commit;
       EXCEPTION
         WHEN OTHERS THEN
           dbms_output.put_line('�޸ı�עʱ����' || SQLERRM);
           --rollback;
           RAISE;
       END;
       procedure mul_att_mod_poi_inner(v_audata_id NUMBER) IS
        begin
	     -- ���log�ֶ��а������ڲ�POI������IX_POI_FLAG���в���һ����¼��FLAG_CODE =110000030000������Ѵ��ڣ��򲻲��롣
         INSERT INTO ix_poi_flag
           SELECT a.pid, '110000030000', 0 AS u_record, NULL AS u_fields
             FROM au_ix_poi a
            WHERE a.audata_id=v_audata_id
              AND EXISTS (SELECT 1
                     FROM au_ix_poi_flag b
                    WHERE a.audata_id = b.audata_id
                      AND b.flag_code = '110000030000')
              AND NOT EXISTS (SELECT 1
                     FROM ix_poi_flag c
                    WHERE a.pid = c.poi_pid
                      AND c.flag_code = '110000030000');

         -- ���log�ֶ��в����������ڲ�POI����ɾ��IX_POI_FLAG����FLAG_CODE =110000030000�ļ�¼����������ڣ��򲻴���
         DELETE FROM ix_poi_flag ipf
          WHERE ipf.flag_code = '110000030000'
            AND EXISTS (SELECT 1
                   FROM au_ix_poi a
                  WHERE a.pid = ipf.poi_pid
                    AND a.audata_id=v_audata_id
                    AND NOT EXISTS (SELECT 1
                  FROM au_ix_poi_flag af
                 WHERE a.audata_id = af.audata_id
                   AND af.FLAG_CODE = '110000030000'));
	   commit;
	   EXCEPTION
         WHEN OTHERS THEN
           dbms_output.put_line('�޸��ڲ�POIʱ����' || SQLERRM);
           --rollback;
           RAISE;
       END;  
      procedure process_mod_yucaiji is
        begin
          --���ĸ��IX_POI_FLAG�д���FLAG_CODEΪ��110000240000���ļ�¼����ɾ��IX_POI_FLAG�иü�¼�����û�У��򲻴���������
           DELETE FROM ix_poi_flag t
           WHERE t.flag_code = '110000240000'
             AND EXISTS (SELECT 1
                    FROM temp_au_poi_modify_log tmp
                   WHERE tmp.pid = t.poi_pid
                     AND tmp.yucaiji_flag = 1);
		     commit;
         end;
      procedure mul_mod_yucaiji(v_audata_id number) is
        begin
          --���ĸ��IX_POI_FLAG�д���FLAG_CODEΪ��110000240000���ļ�¼����ɾ��IX_POI_FLAG�иü�¼�����û�У��򲻴���������
           DELETE FROM ix_poi_flag t
           WHERE t.flag_code = '110000240000'
             AND EXISTS (SELECT 1
                    FROM au_ix_poi tmp
                   WHERE tmp.pid = t.poi_pid and tmp.att_oprstatus in( 0,1));
		   commit;
          end;
      PROCEDURE mul_mod_poi_flag(v_audata_id NUMBER) IS
      BEGIN
        /*
        --:5.���AU_IX_POI_FLAG���д��ڼ�¼��������������Ӱ汾IX_POI_FLAG����:��ɾ����ҵ���Ѿ����ڵ�flag_code��Ȼ����ҵ�����е�flag_code���뵽�Ӱ汾��
         execute immediate 'DELETE FROM ix_poi_flag t
                     WHERE EXISTS (SELECT 1
                              FROM au_ix_poi au
                             WHERE au.state = 2
                               AND au.att_oprstatus = 0
                               AND au.audata_id = :v_audata_Id
                               AND au.pid = t.poi_pid
                               AND EXISTS (SELECT 1
                                      FROM au_ix_poi_flag t2
                                     WHERE t2.audata_id = au.audata_id
                                       AND t2.flag_code = t.flag_code))
                    ' using v_audata_id;

         execute immediate 'INSERT INTO IX_POI_FLAG WITH AURS AS
          (SELECT AU.PID, AUF.FLAG_CODE
             FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
            WHERE AU.STATE = 2
              AND au.att_oprstatus=0
              AND AU.AUDATA_ID = AUF.AUDATA_ID
              AND au.audata_Id=:v_audata_id)
           SELECT AURS.PID,
                  AURS.FLAG_CODE,
                 0 AS U_RECORD,
                 NULL AS U_FIELDS
            FROM AURS' using v_audata_id; */
            
         -- ����AU_IX_POI_FLAG��FLAG_CODE (110000110002)����NOKIA��֤��Ϣ��������ڣ��򲻲��룩
            EXECUTE IMMEDIATE 'INSERT INTO IX_POI_FLAG WITH AURS AS
             (SELECT AU.PID, AUF.FLAG_CODE
                FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF, ix_poi ipi
               WHERE AU.STATE = 2
                 AND au.att_oprstatus in( 0,1)
                 AND AU.AUDATA_ID = AUF.AUDATA_ID
                 and au.audata_id = :v_audata_id
                 and AUF.FLAG_CODE = ''110000110002''
                 and ipi.pid = auf.poi_pid
                 and au.pid = auf.poi_pid
                 and not exists (
                     select * from IX_POI_FLAG a where a.poi_pid = au.pid and a.flag_code = AUF.Flag_Code
                 ))
            SELECT AURS.PID       as poi_pid,
                   AURS.FLAG_CODE AS FLAG_CODE,
                   0              AS U_RECORD,
                   NULL           AS U_FIELDS
               FROM AURS' using v_audata_id; 
commit;

      END;
      
      PROCEDURE mul_mod_verified_mode_flag(v_audata_id NUMBER) IS
      BEGIN 
        --AU_IX_POI���У�STATE= 2��,��LOG����������֤ģʽ������AU_IX_POI_FLAG����FLAG_CODE��ֵ��110000300000���ֳ���֤����
        --����POI_PID��IX_POI_FLAG����FLAG_CODE��ֵ�ǡ�110000330000���Ľ���ɾ����
        EXECUTE IMMEDIATE 'DELETE FROM ix_poi_flag f
         WHERE f.flag_code = ''110000330000''
           AND EXISTS (SELECT 1
                  FROM au_ix_poi au, au_ix_poi_flag af
                 WHERE au.audata_id = af.audata_id
                   AND au.pid = af.poi_pid
                   AND f.poi_pid = au.pid
                   AND au.audata_id = :v_audata_id
                   AND af.flag_code = ''110000300000'')' using v_audata_id; 
        --��AU_IX_POI_FLAG����FLAG_CODE��ֵ��110000300000���ֳ���֤�����뵽IX_POI_FLAG���С�
        EXECUTE IMMEDIATE 'INSERT INTO ix_poi_flag
          SELECT au.pid       AS poi_pid,
                 af.flag_code AS FLAG_CODE,
                 0            AS U_RECORD,
                 NULL         AS U_FIELDS
            FROM au_ix_poi au, au_ix_poi_flag af
           WHERE au.audata_id = af.audata_id
             AND au.pid = af.poi_pid
             AND au.audata_id = :v_audata_id
             AND af.flag_code = ''110000300000''
             AND NOT EXISTS (SELECT 1 FROM ix_poi_flag ipf where au.pid = ipf.poi_pid and ipf.flag_code = ''110000300000'')' using v_audata_id; 
         commit;
      END;  
      
      PROCEDURE mul_del_poi_flag_poi_level(v_audata_id NUMBER) IS
      BEGIN 
        -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000200000,110000210000,110000220000 �ļ�¼����������ڣ��򲻴���
        execute immediate ' 
          DELETE FROM ix_poi_flag t
           WHERE t.flag_code IN (''110000200000'', ''110000210000'',''110000210001'',''110000210002'',''110000210003'',''110000210004'', ''110000220000'')
             AND EXISTS (SELECT 1
                    FROM au_ix_poi au 
                   WHERE au.pid = t.poi_pid
                   and au.audata_id =:v_audata_id)' using v_audata_id ;
commit;
      END;
      
      
      PROCEDURE mul_add_poi_flag_poi_level(v_audata_id NUMBER) IS
      BEGIN 
       -- ����ҵ����AU_IX_POI_FLAG���е�POI_LEVEL��Ϣ�����뵽IX_POI_FLAG����       
       EXECUTE IMMEDIATE '      
       INSERT INTO IX_POI_FLAG
         SELECT AU.PID        as poi_pid,
                AUF.FLAG_CODE as FLAG_CODE,
                0             AS U_RECORD,
                NULL          AS U_FIELDS
           FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
          WHERE au.pid = auf.poi_pid
            and AU.AUDATA_ID = AUF.AUDATA_ID
            and AU.AUDATA_ID = :v_audata_id
            and not exists
          (select 1
                   from ix_poi_flag ipf
                  where ipf.poi_pid = au.pid
                    and ipf.flag_code = auf.flag_code)
            and auf.flag_code in (''110000200000'', ''110000210000'', ''110000220000'')'using v_audata_id; 
commit;
      END;
      
      PROCEDURE mul_verified_mode_flag_ext(v_audata_id NUMBER) IS
      BEGIN 
        --AU_IX_POI_FLAG����FLAG_CODE��ֵ��110000300000���ֳ���֤����
        --����POI_PID��IX_POI_FLAG����FLAG_CODE��ֵ�ǡ�110000330000���Ľ���ɾ����
        EXECUTE IMMEDIATE 'DELETE FROM ix_poi_flag f
         WHERE f.flag_code = ''110000330000''
           AND EXISTS (SELECT 1
                  FROM au_ix_poi au, au_ix_poi_flag af
                 WHERE au.audata_id = af.audata_id
                   AND au.pid = af.poi_pid
                   AND f.poi_pid = au.pid
                   AND au.audata_id = :v_audata_id
                   AND af.flag_code = ''110000300000'')' using v_audata_id; 
        commit;
      END;  
      
      PROCEDURE mul_att_add_poi_kind_ext(v_audata_id NUMBER) IS
    BEGIN
      -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000100000,110000120000,110000140000,110000150000,110000170000,110000220000 �ļ�¼��
      execute immediate 'DELETE FROM ix_poi_flag t
       WHERE t.flag_code IN (''110000100000'',
                             ''110000120000'',
                             ''110000140000'',
                             ''110000150000'',
                             ''110000170000'',
                             ''110000250000'')
         AND EXISTS (SELECT 1
                    FROM au_ix_poi au 
                   WHERE au.pid = t.poi_pid
                   and au.audata_id =:v_audata_id)'
        using v_audata_id;
    
      -- ͨ��POI_PID��ɾ��IX_POI_HOTEL���еļ�¼
      execute immediate 'DELETE FROM ix_poi_hotel t
       WHERE EXISTS (SELECT 1
                    FROM au_ix_poi au 
                   WHERE au.pid = t.poi_pid
                   and au.audata_id =:v_audata_id)'
        using v_audata_id;
        
      -- ����ҵ����AU_IX_POI_HOTEL���е���Ϣ�����뵽IX_POI_HOTEL����
      execute immediate 'INSERT INTO IX_POI_HOTEL
        (HOTEL_ID,
         POI_PID,
         CREDIT_CARD,
         RATING,
         CHECKIN_TIME,
         CHECKOUT_TIME,
         ROOM_COUNT,
         ROOM_TYPE,
         ROOM_PRICE,
         BREAKFAST,
         SERVICE,
         PARKING,
         LONG_DESCRIPTION,
         LONG_DESCRIP_ENG,
         OPEN_HOUR,
         OPEN_HOUR_ENG,
         TELEPHONE,
         ADDRESS,
         CITY,
         PHOTO_NAME,
         TRAVELGUIDE_FLAG,
         U_RECORD,
         U_FIELDS)
        SELECT hotel_id,
               poi_pid,
               credit_card,
               rating,
               checkin_time,
               checkout_time,
               room_count,
               room_type,
               room_price,
               breakfast,
               service,
               parking,
               long_description,
               long_descrip_eng,
               open_hour,
               open_hour_eng,
               telephone,
               address,
               city,
               photo_name,
               0                AS TRAVELGUIDE_FLAG,
               0                AS u_record,
               NULL             AS u_fields
          FROM au_ix_poi au, au_ix_poi_hotel h
         WHERE au.pid = h.poi_pid
           and au.audata_id = h.audata_id
           and au.audata_id =:v_audata_id'
        using v_audata_id;
    commit;
    END;
      
      PROCEDURE mul_modifyKind(v_audata_id NUMBER) IS
      BEGIN 
        -- ͨ��POI_PID��ɾ��IX_POI_FLAG����FLAG_CODE Ϊ 110000100000,110000120000,110000140000,110000150000,110000170000,110000220000 �ļ�¼��
      execute immediate 'DELETE FROM ix_poi_flag t
       WHERE t.flag_code IN (''110000100000'',
                             ''110000120000'',
                             ''110000140000'',
                             ''110000150000'',
                             ''110000170000'',
                             ''110000250000'')
         AND EXISTS (SELECT 1
                    FROM au_ix_poi au 
                   WHERE au.pid = t.poi_pid
                   and au.audata_id =:v_audata_id)'
        using v_audata_id;
        
        -- ����ҵ����AU_IX_POI_FLAG���е���Ϣ�����뵽IX_POI_FLAG����
        EXECUTE IMMEDIATE '      
       INSERT INTO IX_POI_FLAG
         SELECT AU.PID        as poi_pid,
                AUF.FLAG_CODE as FLAG_CODE,
                0             AS U_RECORD,
                NULL          AS U_FIELDS
           FROM AU_IX_POI AU, AU_IX_POI_FLAG AUF
          WHERE au.pid = auf.poi_pid
            and AU.AUDATA_ID = AUF.AUDATA_ID
            and AU.AUDATA_ID = :v_audata_id
            and auf.flag_code in (''110000100000'',
                                 ''110000120000'',
                                 ''110000140000'',
                                 ''110000150000'',
                                 ''110000170000'',
                                 ''110000250000'')'using v_audata_id; 
    
      -- ͨ��POI_PID��ɾ��IX_POI_HOTEL���еļ�¼
      execute immediate 'DELETE FROM ix_poi_hotel t
       WHERE EXISTS (SELECT 1
                    FROM au_ix_poi au 
                   WHERE au.pid = t.poi_pid
                   and au.audata_id =:v_audata_id)'
        using v_audata_id;
        
      -- ����ҵ����AU_IX_POI_HOTEL���е���Ϣ�����뵽IX_POI_HOTEL����
      execute immediate 'INSERT INTO IX_POI_HOTEL
        (HOTEL_ID,
         POI_PID,
         CREDIT_CARD,
         RATING,
         CHECKIN_TIME,
         CHECKOUT_TIME,
         ROOM_COUNT,
         ROOM_TYPE,
         ROOM_PRICE,
         BREAKFAST,
         SERVICE,
         PARKING,
         LONG_DESCRIPTION,
         LONG_DESCRIP_ENG,
         OPEN_HOUR,
         OPEN_HOUR_ENG,
         TELEPHONE,
         ADDRESS,
         CITY,
         PHOTO_NAME,
         TRAVELGUIDE_FLAG,
         U_RECORD,
         U_FIELDS)
        SELECT hotel_id,
               poi_pid,
               credit_card,
               rating,
               checkin_time,
               checkout_time,
               room_count,
               room_type,
               room_price,
               breakfast,
               service,
               parking,
               long_description,
               long_descrip_eng,
               open_hour,
               open_hour_eng,
               telephone,
               address,
               city,
               photo_name,
               0                AS TRAVELGUIDE_FLAG,
               0                AS u_record,
               NULL             AS u_fields
          FROM au_ix_poi au, au_ix_poi_hotel h
         WHERE au.pid = h.poi_pid
           and au.audata_id = h.audata_id
           and au.audata_id =:v_audata_id'
        using v_audata_id;
commit;
      END;
      
      
     PROCEDURE att_add_poi_label_ext IS
     BEGIN
	   -- ���log�ֶ��а��������ڲ�POI������IX_POI_FLAG���в���һ����¼��FLAG_CODE =110000030000������Ѵ��ڣ��򲻲��롣
       INSERT INTO ix_poi_flag
         SELECT a.pid, '110000030000', 0 AS u_record, NULL AS u_fields
           FROM au_ix_poi a
            WHERE EXISTS (SELECT 1
                   FROM TEMP_HIS_IX_POI_EXT l
                  WHERE l.pid=a.pid)
            and a.state=3 and a.att_oprstatus=0      
            AND NOT EXISTS (SELECT 1
                   FROM ix_poi_flag b
                  WHERE a.pid = b.poi_pid
                    AND b.flag_code = '110000030000')
            AND EXISTS (SELECT 1
                   FROM au_ix_poi_flag c
                  WHERE a.pid = c.poi_pid
                    AND c.flag_code = '110000030000');
    /*-- ���log�ֶ��в����������ڲ�POI����ɾ��IX_POI_FLAG����FLAG_CODE =110000030000�ļ�¼����������ڣ��򲻴���
       DELETE FROM ix_poi_flag ipf
        WHERE ipf.flag_code = '110000030000'
          AND EXISTS
        (SELECT 1
                 FROM au_ix_poi a
                WHERE (a.log IS NULL OR instr(a.log, '���ڲ�POI') = 0)
                  AND a.pid = ipf.poi_pid
                  and a.att_oprstatus in( 0,1)
                  AND EXISTS (SELECT 1
                         FROM TEMP_HIS_IX_POI_EXT l
                        WHERE l.pid=a.pid));

       -- �����ע�ֶ��а��������յ�ַ���������ںϺ���IX_POI_FLAG���в���һ����¼��FLAG_CODE =110030060000���̶���־����ʩ�����ַ��������Ѵ��ڣ��򲻲��룻
       INSERT INTO ix_poi_flag
         SELECT a.pid, '110030060000', 0 AS u_record, NULL AS u_fields
           FROM au_ix_poi a
          WHERE instr(a.label, '���յ�ַ') > 0
            AND EXISTS (SELECT 1
                   FROM TEMP_HIS_IX_POI_EXT l
                  WHERE l.pid=a.pid)
            and a.att_oprstatus in( 0,1)
            AND NOT EXISTS (SELECT 1
                   FROM ix_poi_flag b
                  WHERE a.pid = b.poi_pid
                    AND b.flag_code = '110030060000');

       -- �����ע�ֶ��в����������յ�ַ���������ںϺ�ɾ��IX_POI_FLAG����FLAG_CODE =110030060000���̶���־����ʩ�����ַ���ļ�¼����������ڣ��򲻴���
       DELETE FROM ix_poi_flag ipf
        WHERE ipf.flag_code = '110030060000'
          AND EXISTS
        (SELECT 1
                 FROM au_ix_poi a
                WHERE (a.label IS NULL OR instr(a.label, '���յ�ַ') = 0)
                  AND a.pid = ipf.poi_pid
                  and a.att_oprstatus in( 0,1)
                  AND EXISTS (SELECT 1
                         FROM TEMP_HIS_IX_POI_EXT l
                        WHERE l.pid=a.pid)); */
                        
     -- �����ע�ֶΰ������ο���ַ�����ںϺ���ɾ�����ĵ�ַ�ı�ʶ��Ϣ������IX_POI_FLAG���в��롰�̶���־����ʩ�����ַ��(FLAG_CODE =110030060000)�ı�ʶ��Ϣ  
     DELETE FROM IX_POI_FLAG c1
      WHERE EXISTS
      (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
        AND EXISTS (SELECT 1
               FROM au_ix_poi au
              WHERE au.pid = c1.poi_pid
                and au.state=3
                AND au.att_oprstatus in (0,1)
                AND instr(au.label, '���յ�ַ') > 0)
        AND c1.FLAG_CODE IN ('110030010000',
                             '110030020000',
                             '110030030000',
                             '110030040000',
                             '110030050000',
                             '110030070000',
                             '110030080000',
                             '110030090000',
                             '110030100000',
                             '110030110000',
                             '110030120000');
      
        INSERT INTO ix_poi_flag
         SELECT a.pid, '110030060000', 0 AS u_record, NULL AS u_fields
           FROM au_ix_poi a
          WHERE instr(a.label, '���յ�ַ') > 0
            AND EXISTS (SELECT 1
                   FROM TEMP_HIS_IX_POI_EXT l
                  WHERE l.pid=a.pid)
            and a.state=3      
            and a.att_oprstatus in( 0,1)     
            AND NOT EXISTS (SELECT 1
                   FROM ix_poi_flag b
                  WHERE a.pid = b.poi_pid
                    AND b.flag_code = '110030060000');
                        
commit;
     EXCEPTION
       WHEN OTHERS THEN
         dbms_output.put_line('�޸ı�עʱ����' || SQLERRM);
         --rollback;
         RAISE;
     END;
     PROCEDURE att_yucaiji_ext IS
     BEGIN
       --���ĸ��IX_POI_FLAG�д���FLAG_CODEΪ��110000240000���ļ�¼����ɾ��IX_POI_FLAG�иü�¼�����û�У��򲻴���������
       DELETE FROM ix_poi_flag t
        WHERE t.flag_code = '110000240000'
          AND EXISTS (SELECT 1
                 FROM TEMP_HIS_IX_POI_EXT tmp
                WHERE tmp.pid = t.poi_pid);
		commit;
     END;
     
  --ɾ�����ӹ�ϵ���ж��������   
  PROCEDURE delete_mul_relation IS
     low_group_id NUMBER;
  BEGIN 
   
    --������ʱ���һ��������ɾ��ʱ��ɾ��ʣ����ӱ�
    execute immediate 'delete
     from ix_poi_children c
    where exists (select 1
             from temp_au_mul_del_ix_poi t
            where t.pid = c.child_poi_pid)
       or exists (select 1
             from temp_au_mul_del_ix_poi t, ix_poi_parent p
            where t.pid = p.parent_poi_pid
              and p.group_id = c.group_id)';
   
  --������ʱ���һ��������ɾ��ʱ��ɾ��ʣ��ĸ���
   execute immediate 'delete
     from ix_poi_parent p
    where exists (select 1
             from temp_au_mul_del_ix_poi t
            where t.pid = p.parent_poi_pid)';  
  
      execute immediate 'create table tmp_batch as (
      select mi.parent_poi_pid,mi.group_id,o.group_id other_group_id 
      from ( select parent_poi_pid,min(group_id) group_id,count(1)
          from ix_poi_parent p 
         group by parent_poi_pid
        having count(1) > 1 ) mi,ix_poi_parent o 
        where mi.parent_poi_pid = o.parent_poi_pid and mi.group_id <> o.group_id 
       )';
        
        
      --����ix_poi_children��ͬһ����group_idСֵȡ����ֵ
        execute immediate 'update ix_poi_children c set c.group_id 
        =(select  a.group_id from tmp_batch a where a.other_group_id=c.group_id) 
        where exists(select 1 from tmp_batch a where a.other_group_id=c.group_id)';
        
       --ix_poi_children�����ӱ�ȥ��
       execute immediate 'delete
          from ix_poi_children
         where child_poi_pid in (select child_poi_pid
                                   from ix_poi_children a
                                  group by a.child_poi_pid
                                 having count(1) > 1)
           and rowid not in (select min(rowid)
                               from ix_poi_children
                              group by child_poi_pid
                             having count(1) > 1)';
                             
         ---ix_poi_parent��ɾ�������д�������             
       execute immediate '  delete from ix_poi_parent p
         where  exists ( select 1 from tmp_batch  b where  b.other_group_id=p.group_id)';
         commit;  
  END;

   procedure process_mod_verified_flag is
   begin
     -- �������IX_POI��VERIFIED_FLAG�ֶ�ֵΪ��2�����ߡ�3����POI����Flag_code��IX_POI_FLAG������������������ʾ����110000340000����������Ҫ�Ѹñ�ʾɾ���� 
     DELETE FROM IX_POI_FLAG c1
      WHERE EXISTS (SELECT 1
               FROM temp_au_poi_modify_log l, ix_poi i
              WHERE l.pid = c1.poi_pid
                and l.pid = i.pid
                and l.verified_flag = 1
                and i.verified_flag in (2, 3))
        AND c1.FLAG_CODE = '110000340000';
     commit;
   end;
   procedure att_verified_flag_ext is
   begin
     -- �������IX_POI��VERIFIED_FLAG�ֶ�ֵΪ��2�����ߡ�3����POI����Flag_code��IX_POI_FLAG������������������ʾ����110000340000����������Ҫ�Ѹñ�ʾɾ���� 
     DELETE FROM IX_POI_FLAG c1
      WHERE EXISTS
      (SELECT 1 FROM temp_his_ix_poi_ext l WHERE c1.poi_pid = l.pid)
        AND EXISTS (SELECT 1
               FROM au_ix_poi au, ix_poi i
              WHERE au.pid = c1.poi_pid
                and au.pid = i.pid
                and au.state = 3
                and au.att_oprstatus in (0,1)
                and i.verified_flag in (2, 3))
        AND c1.FLAG_CODE = '110000340000';
     commit;
   end;
   procedure mul_mod_verified_flag(v_audata_id number) is
   begin
     -- �������IX_POI��VERIFIED_FLAG�ֶ�ֵΪ��2�����ߡ�3����POI����Flag_code��IX_POI_FLAG������������������ʾ����110000340000����������Ҫ�Ѹñ�ʾɾ���� 
     EXECUTE IMMEDIATE 'DELETE FROM IX_POI_FLAG c1
          WHERE EXISTS (SELECT 1
                   FROM au_ix_poi au, ix_poi i
                  WHERE au.pid = c1.poi_pid
                    and au.pid = i.pid
                    and au.att_oprstatus in (0,1)
                    and i.verified_flag in (2, 3)
                    and au.audata_id =:v_audata_id)
            AND c1.FLAG_CODE = ''110000340000'''
       using v_audata_id;
     commit;
   end;

     PROCEDURE process_att_geoatt_samepoi(v_merge_type VARCHAR2) IS
     	v_oprstatus_clause VARCHAR2(100);
     BEGIN
	    v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,'a');
     -- ��ҵPOI��stateΪ���޸ġ���logΪ����ͬһ��ϵ����ɾ����ҵIX_SAMEPOI�ļ�¼
     EXECUTE IMMEDIATE 'DELETE FROM IX_SAMEPOI isp
        where isp.group_id in
          (select p.group_id from IX_SAMEPOI_PART p
          where EXISTS
                 (select 1 from au_ix_poi a
                 where a.pid = p.poi_pid 
                  and EXISTS (SELECT 1 from temp_au_poi_modify_log l
                    WHERE l.samepoi_flag = 1
                    AND a.pid = l.pid and a.audata_id=l.audata_id)))';                  
                    
       -- ��ҵPOI��stateΪ���޸ġ���logΪ����ͬһ��ϵ����ɾ����ҵIX_SAMEPOI_PART�ļ�¼
       EXECUTE IMMEDIATE 'DELETE FROM IX_SAMEPOI_PART isp
         where isp.group_id in
          (select p.group_id from IX_SAMEPOI_PART p
          where EXISTS
                 (select 1 from au_ix_poi a
                 where a.pid = p.poi_pid 
                  and EXISTS (SELECT 1 from temp_au_poi_modify_log l
                    WHERE l.samepoi_flag = 1
                    AND a.pid = l.pid and a.audata_id=l.audata_id)))';
                    
       -- ��ҵPOI��stateΪ��ɾ��������������ɾ����ҵIX_SAMEPOI�ļ�¼
     EXECUTE IMMEDIATE ' DELETE FROM IX_SAMEPOI isp
        where isp.group_id in
          (select p.group_id from IX_SAMEPOI_PART p
          where EXISTS
                 (select 1 from au_ix_poi a
                 where (a.state = 1 or a.state=3)
                  and a.pid = p.poi_pid and ' || v_oprstatus_clause || ' 
                  AND NOT EXISTS (SELECT 1 
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = a.pid)))';
                    
       -- ��ҵPOI��stateΪ��ɾ��������������ɾ����ҵIX_SAMEPOI_PART�ļ�¼
       EXECUTE IMMEDIATE ' DELETE FROM IX_SAMEPOI_PART isp
         where isp.group_id in
          (select p.group_id from IX_SAMEPOI_PART p
          where EXISTS
                 (select 1 from au_ix_poi a
                 where (a.state = 1 or a.state=3)
                  and a.pid = p.poi_pid and ' || v_oprstatus_clause || ' 
                  AND NOT EXISTS (SELECT 1 
                      FROM temp_au_ix_poi_mul_task tmp
                     WHERE tmp.pid = a.pid)))';
                                               
       --��ҵPOI��stateΪ���޸ġ�����logΪ����ͬһ��ϵ������Ϊ����������������ҵIX_SAMEPOI�ļ�¼             
       EXECUTE IMMEDIATE ' INSERT INTO IX_SAMEPOI
         SELECT distinct isp.GROUP_ID,isp.RELATION_TYPE,0 as U_RECORD,NULL as U_FIELDS 
           from AU_IX_SAMEPOI isp
            where isp.group_id in
            (select p.group_id from AU_IX_SAMEPOI_PART p
               where EXISTS
                  (select 1 from au_ix_poi au
                where au.audata_id = p.audata_id 
                  and EXISTS (SELECT 1 from temp_au_poi_modify_log l
                     WHERE l.samepoi_flag = 1
                     AND au.pid = l.pid and au.audata_id=l.audata_id))
               or EXISTS 
                    (select 1 from au_ix_poi a
                    where a.state = 3 and  ' || v_oprstatus_clause || ' 
                    and a.audata_id = p.audata_id 
                    AND NOT EXISTS (SELECT 1 FROM temp_au_ix_poi_mul_task mul WHERE a.pid = mul.pid)))';
                    
       --��ҵPOI��stateΪ���޸ġ�����logΪ����ͬһ��ϵ������Ϊ����������������ҵIX_SAMEPOI_PART�ļ�¼             
       EXECUTE IMMEDIATE ' INSERT INTO IX_SAMEPOI_PART
         SELECT distinct isp.GROUP_ID,isp.POI_PID,0 as U_RECORD,NULL as U_FIELDS 
           from AU_IX_SAMEPOI_PART isp
            where isp.group_id in
            (select p.group_id from AU_IX_SAMEPOI_PART p
               where EXISTS
                  (select 1 from au_ix_poi au
                where au.audata_id = p.audata_id 
                  and EXISTS (SELECT 1 from temp_au_poi_modify_log l
                     WHERE l.samepoi_flag = 1 
                     AND au.pid = l.pid and au.audata_id=l.audata_id))
               or EXISTS 
                    (select 1 from au_ix_poi a
                    where a.state = 3 and ' || v_oprstatus_clause || ' 
                    and a.audata_id = p.audata_id
                    AND NOT EXISTS (SELECT 1 FROM temp_au_ix_poi_mul_task mul WHERE a.pid = mul.pid)))';           

      EXCEPTION
       WHEN OTHERS THEN
         dbms_output.put_line('�ںϸ�ͬһ��ϵʱ����' || SQLERRM);
         --rollback;
         RAISE;
        commit;
     END;
     
     PROCEDURE mul_process_att_geoatt_samepoi(v_audata_id NUMBER,v_merge_type VARCHAR2) IS
       v_oprstatus_clause VARCHAR2(100);
     BEGIN
       v_oprstatus_clause := merge_utils.get_proxypoi_clause(v_merge_type,'a');
     -- ��ҵPOI��stateΪ���޸ġ�����logΪ����ͬһ��ϵ����stateΪ��ɾ��������������ɾ����ҵIX_SAMEPOI�ļ�¼
     EXECUTE IMMEDIATE 'DELETE FROM IX_SAMEPOI isp
        where isp.group_id in
            (select p.group_id from IX_SAMEPOI_PART p
               where EXISTS
                  (select 1 from au_ix_poi a
                where a.pid = p.poi_pid
                  and a.audata_id = :v_audata_id
                  and ' || v_oprstatus_clause || '
                  and ((instr(a.log, ''��ͬһ��ϵ'') > 0 and a.state=2) 
                  or (a.state = 1 or a.state = 3))))'using v_audata_id ;
                    
       -- ��ҵPOI��stateΪ���޸ġ�����logΪ����ͬһ��ϵ����stateΪ��ɾ��������������ɾ����ҵIX_SAMEPOI_PART�ļ�¼
       EXECUTE IMMEDIATE 'DELETE FROM IX_SAMEPOI_PART isp
        where isp.group_id in
            (select p.group_id from IX_SAMEPOI_PART p
               where EXISTS
                  (select 1 from au_ix_poi a
                where a.pid = p.poi_pid
                  and a.audata_id = :v_audata_id
                  and ' || v_oprstatus_clause || '
                  and ((instr(a.log, ''��ͬһ��ϵ'') > 0 and a.state=2) 
                  or (a.state = 1 or a.state = 3))))'using v_audata_id ;
                                               
       --��ҵPOI��stateΪ���޸ġ�����logΪ����ͬһ��ϵ������Ϊ����������������ҵIX_SAMEPOI�ļ�¼             
       EXECUTE IMMEDIATE 'INSERT INTO IX_SAMEPOI
         SELECT distinct isp.GROUP_ID,isp.RELATION_TYPE,0 as U_RECORD,NULL as U_FIELDS 
           from AU_IX_SAMEPOI isp
            where isp.group_id in
            (select p.group_id from AU_IX_SAMEPOI_PART p
               where EXISTS
                  (select 1 from au_ix_poi a
                where a.audata_id = p.audata_id
                  and a.audata_id = :v_audata_id
                  and ' || v_oprstatus_clause || '
                  and ((instr(a.log, ''��ͬһ��ϵ'') > 0 and a.state=2) 
                  or a.state = 3)))'using v_audata_id ;
                    
       --��ҵPOI��stateΪ���޸ġ�����logΪ����ͬһ��ϵ������Ϊ����������������ҵIX_SAMEPOI_PART�ļ�¼             
       EXECUTE IMMEDIATE 'INSERT INTO IX_SAMEPOI_PART
         SELECT distinct isp.GROUP_ID,isp.POI_PID,0 as U_RECORD,NULL as U_FIELDS 
           from AU_IX_SAMEPOI_PART isp
            where isp.group_id in
            (select p.group_id from AU_IX_SAMEPOI_PART p
               where EXISTS
                  (select 1 from au_ix_poi a
                where a.audata_id = p.audata_id
                  and a.audata_id = :v_audata_id
                  and ' || v_oprstatus_clause || '
                  and ((instr(a.log, ''��ͬһ��ϵ'') > 0 and a.state=2) 
                  or a.state = 3)))'using v_audata_id ;
              
      EXCEPTION
       WHEN OTHERS THEN
         dbms_output.put_line('�ںϸ�ͬһ��ϵʱ����' || SQLERRM);
         --rollback;
         RAISE;
         commit;
     END;
END merge_au_ix_no_his;
/
