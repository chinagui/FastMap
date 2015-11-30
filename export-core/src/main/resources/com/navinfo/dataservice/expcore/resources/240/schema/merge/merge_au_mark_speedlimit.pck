CREATE OR REPLACE PACKAGE merge_au_mark_speedlimit IS

  -- Author  : magw
  -- Created : 2014/05/22
  -- Purpose : 点限速融合

  /**初始化临时表*/
  PROCEDURE pre_merge;
  /**点限速单任务新增融合*/
  PROCEDURE do_add;
  /**点限速单任务修改融合*/
  PROCEDURE do_modify;
  /**点限速单任务删除融合*/
  PROCEDURE do_delete;
  /**点限速多任务修改融合*/
  PROCEDURE do_mul_modify(l_mark_id NUMBER);
  /**点限速多任务删除融合*/
  PROCEDURE do_mul_delete(l_mark_id NUMBER);
  /**根据转入的坐标值生成图幅id，如果生成的图幅id为空或conut>1，则使用au_mark中的图幅id*/
  FUNCTION getMeshidByPoint(geox    in number,
                            geoy    in number,
                            mesh_id in number) return number
    PARALLEL_ENABLE;
  /**根据转入的点和与正北方向的夹角,返回该向量方向的另外的一个点*/
  FUNCTION getAnotherPontByAngel(geometry in SDO_GEOMETRY, angel in NUMBER)
    return SDO_GEOMETRY
    PARALLEL_ENABLE;
  /**计算向量m到向量n顺时针方向的夹角(向量m为m1到m2，向量n为n1到n2)*/
  FUNCTION calLineSectionAngle(m1 in SDO_GEOMETRY,
                               m2 in SDO_GEOMETRY,
                               n1 in SDO_GEOMETRY,
                               n2 in SDO_GEOMETRY) return number
    PARALLEL_ENABLE;
  /**根据转入的向量夹角计算限速标牌作用方向*/
  FUNCTION getDirectByAngel(angel in NUMBER) return NUMBER
    PARALLEL_ENABLE;

  /**解析au_mark表中param_s字段，取第一个值*/
  FUNCTION getParam_s(vParam_s in varchar2) return varchar2
    PARALLEL_ENABLE;
END merge_au_mark_speedlimit;
/
CREATE OR REPLACE PACKAGE BODY merge_au_mark_speedlimit IS

  PROCEDURE reset_tmp_rd_speedlimit IS
  BEGIN
    --备份rd_speedlimit表的数据到临时表
    EXECUTE IMMEDIATE 'truncate table temp_his_rd_speedlimit';
    INSERT INTO temp_his_rd_speedlimit
      SELECT * FROM rd_speedlimit;
  END;

  --temp_au_mark_speedlmt_multask用于存放被多个外业au_mark修改的RD_SPEEDLIMIT的PID
  PROCEDURE reset_tmp_speedlmt_multask IS
  BEGIN
  
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_mark_speedlmt_multask
      select rs.pid
    from (select m.gdb_fea_pid as pid, count(1)
          from au_mark m
         where m.type in (0,6)
           and m.merge_flag = 0
           and (m.mark_item in (17, 24, 77, 78) 
            or (m.mark_item = 61 and m.param_ex in (''限速'', ''解除限速'', ''条件限速开始'', ''条件限速结束'')))
           and m.gdb_fea_pid <> 0
           and exists (select 1 from rd_speedlimit r where m.gdb_fea_pid = r.pid)
         group by m.gdb_fea_pid
        having count(1) > 1) rs';
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速融合向临时表插入数据时出错' || SQLERRM);
      RAISE;
  END;

  --temp_au_mark_speedlimit_grp用于存放点限速多任务融合的记录
  PROCEDURE reset_tmp_speedlmt_grp IS
  BEGIN
  
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_mark_speedlimit_grp
      SELECT m.mark_id,
       m.gdb_fea_pid as pid,
       (case m.mark_item when 61 then 1 else 2 end) as state
    FROM au_mark m
   WHERE exists (select 1 from temp_au_mark_speedlmt_multask tmp where tmp.pid = m.gdb_fea_pid)
   order by m.gdb_fea_pid ASC, m.imp_date ASC, m.mark_id ASC';
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速融合向临时表插入数据时出错' || SQLERRM);
      RAISE;
  END;

  --temp_au_mark_speedlimit_log用于存放点限速单任务融合的记录
  PROCEDURE reset_tmp_speedlmt_log IS
  BEGIN
  
    EXECUTE IMMEDIATE 'INSERT INTO temp_au_mark_speedlimit_log
    /*能成功关联到rd_speedlimit*/
   select m.mark_id,
         m.gdb_fea_pid as pid,
         (case m.mark_item when 61 then 1 else 2 end) as state
    from au_mark m
   where m.type in (0,6)
     and m.merge_flag = 0
     and (m.mark_item in (17, 24, 77, 78) 
      or (m.mark_item = 61 and m.param_ex in (''限速'', ''解除限速'', ''条件限速开始'', ''条件限速结束'')))
     and not exists (select 1 from temp_au_mark_speedlmt_multask tmp where tmp.pid = m.gdb_fea_pid)
     and exists (select 1 from rd_speedlimit r where m.gdb_fea_pid = r.pid)
  union all
    /*不能成功关联到rd_speedlimit*/
  select m.mark_id, m.gdb_fea_pid as pid, 3 as state
    from au_mark m
   where m.type in (0,6)
     and m.merge_flag = 0
     and m.mark_item in (17, 24, 77, 78)
     and not exists (select 1 from temp_au_mark_speedlmt_multask tmp where tmp.pid = m.gdb_fea_pid)
     and not exists (select 1 from rd_speedlimit r where m.gdb_fea_pid = r.pid)';
  
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速融合向临时表插入数据时出错' || SQLERRM);
      RAISE;
  END;

  PROCEDURE pre_merge IS
  BEGIN
  
    reset_tmp_rd_speedlimit();
  
    --点限速融合时，如果一个RD_SPEEDLIMIT的PID被多个外业au_mark修改（包括修改、删除），视为多任务融合
    --state=1用于删除点限速，state=2用于更新点限速,state=3用于新增点限速
    reset_tmp_speedlmt_multask();
    reset_tmp_speedlmt_grp();
    reset_tmp_speedlmt_log();
  
  END;

  --根据转入的au_mark值计算限速标牌作用方向
  PROCEDURE loop_do_add_direct(mark_id     in number,
                               link_pid    in number,
                               geox        in number,
                               geoy        in number,
                               angle       in number,
                               m1_geometry in SDO_GEOMETRY) IS
    v_link_pid   NUMBER := 0;
    v_pid_count  NUMBER := 0;
    is_pid_exist NUMBER := 0;
    v_angle      NUMBER := 0;
    v_direct     NUMBER := 0;
    m2_geometry  SDO_GEOMETRY;
    n1_geometry  SDO_GEOMETRY;
    n2_geometry  SDO_GEOMETRY;
  BEGIN
    --根据转入的link_pid,判断在rd_link中是否存在，如果存在返回1、否则返回0
    IF link_pid IS NULL OR link_pid = 0 THEN
      is_pid_exist := 0;
    ELSE
      EXECUTE IMMEDIATE 'SELECT count(1)
              FROM rd_link l
       WHERE l.link_pid = :link_pid'
        INTO v_pid_count
        USING link_pid;
      IF v_pid_count > 0 THEN
        is_pid_exist := 1;
      END IF;
    END IF;
  
    --根据转入的坐标值查找2米范围内的link，如果查找的link为唯一一条,则返回该link_pid，否则返回NULL
    IF is_pid_exist = 1 THEN
      v_link_pid := link_pid;
    ELSE
      EXECUTE IMMEDIATE 'SELECT count(1)
     FROM rd_link l
       WHERE l.kind in (1,2,3,4,5,6,7) and SDO_NN(l.geometry,
                    navisys.navi_geom.createpoint(:geox,:geoy),
                    ''DISTANCE=2 UNIT=METER'') = ''TRUE'''
        INTO v_pid_count
        USING geox, geoy;
      IF v_pid_count <> 1 THEN
        v_link_pid := 0;
      ELSE
        EXECUTE IMMEDIATE 'SELECT l.link_pid
     FROM rd_link l
       WHERE l.kind in (1,2,3,4,5,6,7) and SDO_NN(l.geometry,
                    navisys.navi_geom.createpoint(:geox,:geoy),
                    ''DISTANCE=2 UNIT=METER'') = ''TRUE'''
          INTO v_link_pid
          USING geox, geoy;
      END IF;
    END IF;
  
    --计算限速标牌作用方向
    IF v_link_pid IS NULL OR v_link_pid = 0 THEN
      v_direct := 0;
    ELSE
      m2_geometry := merge_au_mark_speedlimit.getAnotherPontByAngel(m1_geometry,
                                                                    angle);
      EXECUTE IMMEDIATE 'select n.geometry from rd_node n,rd_link l where n.node_pid=l.s_node_pid and l.link_pid=:v_link_pid'
        INTO n1_geometry
        USING v_link_pid;
      EXECUTE IMMEDIATE 'select n.geometry from rd_node n,rd_link l where n.node_pid=l.e_node_pid and l.link_pid=:v_link_pid'
        INTO n2_geometry
        USING v_link_pid;
      v_angle  := merge_au_mark_speedlimit.calLineSectionAngle(m1_geometry,
                                                               m2_geometry,
                                                               n1_geometry,
                                                               n2_geometry);
      v_direct := merge_au_mark_speedlimit.getDirectByAngel(v_angle);
    END IF;
  
    --将限速标牌作用方向插入临时表中
    EXECUTE IMMEDIATE 'INSERT INTO temp_new_mark_direct(mark_id,link_pid,angle,direct) values (:mark_id,:v_link_pid,:v_angle,:v_direct)'
      USING mark_id, v_link_pid, v_angle, v_direct;
  END;

  --计算限速标牌作用方向
  PROCEDURE do_add_direct IS
  BEGIN
    EXECUTE IMMEDIATE 'truncate table temp_new_mark_direct';
    FOR rec IN (SELECT m.mark_id,
                       m.link_pid,
                       m.x_lead,
                       m.y_lead,
                       m.angle,
                       m.geometry
                  FROM au_mark m
                 WHERE EXISTS (SELECT 1
                          FROM temp_au_mark_speedlimit_log l
                         WHERE l.state = 3
                           AND l.mark_id = m.mark_id)
                 ORDER BY m.mark_id) LOOP
      loop_do_add_direct(rec.mark_id,
                         rec.link_pid,
                         rec.x_lead,
                         rec.y_lead,
                         rec.angle,
                         rec.geometry);
    END LOOP;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('计算限速标牌作用方向时出错' || SQLERRM);
      RAISE;
  END;

  --点限速单任务新增融合
  PROCEDURE do_add IS
    v_pid_count NUMBER;
  BEGIN
  
    EXECUTE IMMEDIATE 'truncate table temp_new_rd_speedlimit';
  
    EXECUTE IMMEDIATE 'SELECT COUNT(1)
     FROM au_mark m
       WHERE EXISTS (SELECT 1
                FROM temp_au_mark_speedlimit_log l
               WHERE l.state = 3
                 AND l.mark_id = m.mark_id)'
      INTO v_pid_count;
    IF (v_pid_count > 0) THEN
      pid_man.apply_pid('RD_SPEEDLIMIT', v_pid_count);
    
      do_add_direct();
    
      EXECUTE IMMEDIATE 'INSERT INTO temp_new_rd_speedlimit
      (pid,
       link_pid,
       direct,
       speed_value,
       speed_type,
       speed_dependent,
       speed_flag,
       limit_src,
       time_domain,
       capture_flag,
       descript,
       mesh_id,
       status,
       ck_status,
       adja_flag,
       rec_status_in,
       rec_status_out,
       time_descript,
       geometry,
       u_record,
       u_fields,
       mark_id,
       tollgate_flag)
      SELECT pid_man.pid_nextval(''RD_SPEEDLIMIT'') AS pid,--申请pid
             t.link_pid as link_pid,
             t.direct as direct, --作用标牌方向
             m.param_l*10,--speed_value单位换算
             (case m.mark_item when 17 then 0 when 24 then 0 when 77 then 3 when 78 then 3 end) as speed_type,
             0,
             (case m.mark_item when 17 then 0 when 77 then 0 when 24 then 1 when 78 then 1 end) as speed_flag,
             1,
             NULL,
             (case m.mark_item when 17 then merge_au_mark_speedlimit.getParam_s(m.param_s) when 24 then merge_au_mark_speedlimit.getParam_s(m.param_s) when 77 then ''0'' when 78 then ''0'' end) as capture_flag,
             REPLACE(m.memo,''已抽取|'','''') as descript,
             merge_au_mark_speedlimit.getMeshidByPoint(m.x_lead,m.y_lead,m.mesh_id) as mesh_id, --mesh_id根据X_LEAD、Y_LESD生成
             0,--status
             0,--ck_status
             0,
             3,--rec_status_in
             1,--rec_status_out
             NULL,
             dms_utils.from_wktgeometry(''POINT(''||m.x_lead||'' ''||m.y_lead||'')'') as geometry, --geometry 根据X_LEAD、Y_LESD生成
             0,
             NULL,
             m.mark_id,
             (case m.mark_item when 17 then m.param_r when 24 then m.param_r when 77 then 0 when 78 then 0 end) as tollgate_flag
        FROM au_mark m,temp_new_mark_direct t
       WHERE m.mark_id = t.mark_id
             and EXISTS (SELECT 1
                FROM temp_au_mark_speedlimit_log l
               WHERE l.state = 3
                 AND l.mark_id = m.mark_id)';
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO rd_speedlimit
      (pid,
       link_pid,
       direct,
       speed_value,
       speed_type,
       speed_dependent,
       speed_flag,
       limit_src,
       time_domain,
       capture_flag,
       descript,
       mesh_id,
       status,
       ck_status,
       adja_flag,
       rec_status_in,
       rec_status_out,
       time_descript,
       geometry,
       u_record,
       u_fields,
       tollgate_flag)
      SELECT pid,
             link_pid,
             direct,
             speed_value,
             speed_type,
             speed_dependent,
             speed_flag,
             limit_src,
             time_domain,
             capture_flag,
             descript,
             mesh_id,
             status,
             ck_status,
             adja_flag,
             rec_status_in,
             rec_status_out,
             time_descript,
             geometry,
             u_record,
             u_fields,
             tollgate_flag
        FROM temp_new_rd_speedlimit';
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速单任务新增融合时出错' || SQLERRM);
      RAISE;
  END;

  --点限速单任务修改融合
  PROCEDURE do_modify IS
  BEGIN
  
    MERGE INTO rd_speedlimit r
    USING (SELECT m.gdb_fea_pid as pid,
                  m.param_l * 10 as speed_value,
                  REPLACE(m.memo,'已抽取|','') as descript,
                  3 as rec_status_in,
                  3 as rec_status_out,
                  (case m.mark_item when 17 then merge_au_mark_speedlimit.getParam_s(m.param_s) when 24 then merge_au_mark_speedlimit.getParam_s(m.param_s) end) as capture_flag,
                  (case m.mark_item when 17 then m.param_r when 24 then m.param_r end) as tollgate_flag,
                  (case m.mark_item when 17 then 1 when 24 then 1 when 77 then 0 when 78 then 0 end) as flag_item
             FROM au_mark m
            WHERE EXISTS (SELECT 1
                     FROM temp_au_mark_speedlimit_log l
                    WHERE l.state = 2
                      AND l.mark_id = m.mark_id)) v
    ON (r.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET r.speed_value    = v.speed_value,
             r.descript       = v.descript,
             r.rec_status_in  = v.rec_status_in,
             r.rec_status_out = v.rec_status_out,
             r.capture_flag   = decode(flag_item,1,v.capture_flag,r.capture_flag),
             r.tollgate_flag  = decode(flag_item,1,v.tollgate_flag,r.tollgate_flag);
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速单任务修改融合时出错' || SQLERRM);
      RAISE;
  END;

  --点限速单任务删除融合
  PROCEDURE do_delete IS
  BEGIN
  
    MERGE INTO rd_speedlimit r
    USING (SELECT m.gdb_fea_pid as pid,
                  2             as rec_status_in,
                  2             as rec_status_out
             FROM au_mark m
            WHERE EXISTS (SELECT 1
                     FROM temp_au_mark_speedlimit_log l
                    WHERE l.state = 1
                      AND l.mark_id = m.mark_id)) v
    ON (r.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET r.rec_status_in  = v.rec_status_in,
             r.rec_status_out = v.rec_status_out;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速单任务删除融合时出错' || SQLERRM);
      RAISE;
  END;

  --点限速多任务修改融合
  PROCEDURE do_mul_modify(l_mark_id number) IS
  BEGIN
    --temp_mulhis_rd_speedlimit用于生成改前履历
    EXECUTE IMMEDIATE 'truncate table temp_mulhis_rd_speedlimit';
    EXECUTE IMMEDIATE 'INSERT INTO temp_mulhis_rd_speedlimit SELECT * FROM rd_speedlimit ';
    EXECUTE IMMEDIATE 'MERGE INTO rd_speedlimit r
    USING (SELECT m.gdb_fea_pid as pid,
                  m.param_l * 10 as speed_value,
                  REPLACE(m.memo,''已抽取|'','''') as descript,
                  3 as rec_status_in,
                  3 as rec_status_out,
                  (case m.mark_item when 17 then merge_au_mark_speedlimit.getParam_s(m.param_s) when 24 then merge_au_mark_speedlimit.getParam_s(m.param_s) end) as capture_flag,
                  (case m.mark_item when 17 then m.param_r when 24 then m.param_r end) as tollgate_flag,
                  (case m.mark_item when 17 then 1 when 24 then 1 when 77 then 0 when 78 then 0 end) as flag_item
             FROM au_mark m
            WHERE m.mark_id = :l_mark_id) v
    ON (r.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET r.speed_value    = v.speed_value,
             r.descript       = v.descript,
             r.rec_status_in  = v.rec_status_in,
             r.rec_status_out = v.rec_status_out,
             r.capture_flag   = decode(flag_item,1,v.capture_flag,r.capture_flag),
             r.tollgate_flag  = decode(flag_item,1,v.tollgate_flag,r.tollgate_flag)'
      using l_mark_id;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速多任务修改融合时出错' || SQLERRM);
      RAISE;
  END;

  --点限速多任务删除融合
  PROCEDURE do_mul_delete(l_mark_id number) IS
  BEGIN
  
    --temp_mulhis_rd_speedlimit用于生成改前履历
    EXECUTE IMMEDIATE 'truncate table temp_mulhis_rd_speedlimit';
    EXECUTE IMMEDIATE 'INSERT INTO temp_mulhis_rd_speedlimit SELECT * FROM rd_speedlimit ';
    EXECUTE IMMEDIATE 'MERGE INTO rd_speedlimit r
    USING (SELECT m.gdb_fea_pid as pid,
                  2 as rec_status_in,
                  2 as rec_status_out
             FROM au_mark m
            WHERE m.mark_id = :l_mark_id) v
    ON (r.pid = v.pid)
    WHEN MATCHED THEN
      UPDATE
         SET r.rec_status_in  = v.rec_status_in,
             r.rec_status_out = v.rec_status_out'
      using l_mark_id;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('点限速多任务删除融合时出错' || SQLERRM);
      RAISE;
  END;

  --根据转入的坐标值生成图幅id，如果生成的图幅id为空或conut>1，则使用au_mark中的图幅id
  FUNCTION getMeshidByPoint(geox    in number,
                            geoy    in number,
                            mesh_id in number) return number
    PARALLEL_ENABLE IS
    meshary navisys.navi_geom.meshidary;
  BEGIN
    meshary := navisys.navi_geom.get25kmapnumber(geox, geoy);
    IF meshary IS NULL OR meshary.count() = 0 THEN
      RETURN mesh_id;
    END IF;
    IF meshary.count() > 1 THEN
      RETURN mesh_id;
    END IF;
    RETURN meshary(1);
  EXCEPTION
    WHEN OTHERS THEN
      RETURN NULL;
  END;

  --根据转入的点和与正北方向的夹角,返回该向量方向的另外的一个点
  FUNCTION getAnotherPontByAngel(geometry in SDO_GEOMETRY, angel in NUMBER)
    return SDO_GEOMETRY
    PARALLEL_ENABLE IS
    point_x NUMBER := 0;
    point_y NUMBER := 0;
    v_angel NUMBER := 0;
  BEGIN
    IF geometry IS NULL OR angel IS NULL THEN
      RETURN NULL;
    END IF;
    IF angel < 0 THEN
      v_angel := angel + 360;
    ELSE
      v_angel := angel;
    END IF;
    IF v_angel = 0 OR v_angel = 360 THEN
      point_x := geometry.SDO_POINT.X;
      point_y := geometry.SDO_POINT.Y + 0.0001;
    END IF;
    IF v_angel > 0 AND v_angel < 90 THEN
      point_x := geometry.SDO_POINT.X + 0.0001;
      point_y := geometry.SDO_POINT.Y + 0.0001;
    END IF;
    IF v_angel = 90 THEN
      point_x := geometry.SDO_POINT.X + 0.0001;
      point_y := geometry.SDO_POINT.Y;
    END IF;
    IF v_angel > 90 AND v_angel < 180 THEN
      point_x := geometry.SDO_POINT.X + 0.0001;
      point_y := geometry.SDO_POINT.Y - 0.0001;
    END IF;
    IF v_angel = 180 THEN
      point_x := geometry.SDO_POINT.X;
      point_y := geometry.SDO_POINT.Y - 0.0001;
    END IF;
    IF v_angel > 180 AND v_angel < 270 THEN
      point_x := geometry.SDO_POINT.X - 0.0001;
      point_y := geometry.SDO_POINT.Y - 0.0001;
    END IF;
    IF v_angel = 270 THEN
      point_x := geometry.SDO_POINT.X - 0.0001;
      point_y := geometry.SDO_POINT.Y;
    END IF;
    IF v_angel > 270 AND v_angel < 360 THEN
      point_x := geometry.SDO_POINT.X - 0.0001;
      point_y := geometry.SDO_POINT.Y + 0.0001;
    END IF;
    return navisys.navi_geom.createpoint(point_x, point_y);
  EXCEPTION
    WHEN OTHERS THEN
      RETURN NULL;
  END;

  --计算向量m到向量n顺时针方向的夹角(向量m为m1到m2，向量n为n1到n2)
  FUNCTION calLineSectionAngle(m1 in SDO_GEOMETRY,
                               m2 in SDO_GEOMETRY,
                               n1 in SDO_GEOMETRY,
                               n2 in SDO_GEOMETRY) return number
    PARALLEL_ENABLE IS
    PI       NUMBER := 3.1415926;
    angle_m  NUMBER := 0;
    angle_n  NUMBER := 0;
    angle_mn NUMBER := 0;
  BEGIN
    IF m1 IS NULL OR m1 IS NULL OR n1 IS NULL OR n2 IS NULL THEN
      RETURN 0;
    END IF;
  
    angle_m  := atan2((m2.SDO_POINT.Y - m1.SDO_POINT.Y),
                      (m2.SDO_POINT.X - m1.SDO_POINT.X));
    angle_n  := atan2((n2.SDO_POINT.Y - n1.SDO_POINT.Y),
                      (n2.SDO_POINT.X - n1.SDO_POINT.X));
    angle_mn := angle_m - angle_n;
  
    IF angle_mn < 0 THEN
      angle_mn := (2 * PI + angle_mn);
    END IF;
    
    return ROUND(angle_mn * 180 / PI, 5);
  EXCEPTION
    WHEN OTHERS THEN
      RETURN NULL;
  END;

  --根据转入的向量夹角计算限速标牌作用方向
  FUNCTION getDirectByAngel(angel in NUMBER) return NUMBER
    PARALLEL_ENABLE IS
  BEGIN
    IF angel >= 0 AND angel < 90 THEN
      return 2;
    END IF;
    IF angel > 90 AND angel < 270 THEN
      return 3;
    END IF;
    IF angel > 270 AND angel <= 360 THEN
      return 2;
    END IF;
    return 0;
  END;
  
  --解析au_mark表中param_s字段，取第一个值
  FUNCTION getParam_s(vParam_s in varchar2) return varchar2
    PARALLEL_ENABLE IS
    Param_s varchar2(2000) := '0';
    l_index INTEGER;
  BEGIN
    if vParam_s is null then
      return Param_s;
    else
      l_index := instr(vParam_s,'|',1,1);
        if (l_index = 0) then
          return vParam_s;
        elsif (l_index = 1) then
          return Param_s;
        else
          Param_s := substr(vParam_s,0,l_index-1);
          return Param_s;
        end if;
    end if;
   END;

END merge_au_mark_speedlimit;
/
