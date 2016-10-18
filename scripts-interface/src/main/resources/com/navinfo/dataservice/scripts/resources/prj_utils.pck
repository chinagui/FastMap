begin
  execute immediate 'create global temporary table TMP_RESTRICT
  (link_pid   INTEGER,s_node_pid INTEGER,e_node_pid INTEGER,direct     INTEGER)
  on commit delete rows';

  execute immediate 'create global temporary table TMP_RESTRICT2(link_pid   INTEGER,s_node_pid INTEGER,e_node_pid INTEGER,direct     INTEGER,via_path   VARCHAR2(250))on commit delete rows';
end;
/
  

create or replace package package_utils is

  type record_restrict is record(
    link_pid      int,
    in_node1      varchar2(50),
    in_node2      varchar2(50),
    out_node1     varchar2(50),
    out_node2     varchar2(50),
    relation_type int,
    via_path      varchar2(250));

  type rows_record_restrict is table of record_restrict;

  procedure get_restrict_in_shape(p_in_link_pid in int,
                                  p_in_node_pid in int,
                                  v_in_lng1     out number,
                                  v_in_lat1     out number,
                                  v_in_lng2     out number,
                                  v_in_lat2     out number);

  procedure get_restrict_out_shape(p_out_link_pid in int,
                                   p_out_node_pid in int,
                                   v_out_lng1     out number,
                                   v_out_lat1     out number,
                                   v_out_lng2     out number,
                                   v_out_lat2     out number);

  procedure get_restrict_via(p_in_link_pid  in int,
                             p_in_node_pid  in int,
                             p_out_link_pid in int,
                             v_out_lng1     out number,
                             v_out_lat1     out number,
                             v_out_lng2     out number,
                             v_out_lat2     out number,
                             v_via_path     out varchar2);
  function get_restrict_points(p_in_link_pid int,
                               p_in_node_pid int,
                               out_link_pids varchar2)
    return rows_record_restrict
    pipelined;
    
   FUNCTION num_to_bin (p_num NUMBER) RETURN VARCHAR2;
   
   function parse_vehicle(vehicle number) return number;
   
   function track_links(p_link_pid number,p_link_dir number) return varchar2;
end;
/

create or replace package body package_utils is

  procedure get_restrict_in_shape(p_in_link_pid in int,
                                  p_in_node_pid in int,
                                  v_in_lng1     out number,
                                  v_in_lat1     out number,
                                  v_in_lng2     out number,
                                  v_in_lat2     out number) is
    v_link_row rd_link%rowtype;
  
  begin
    --计算进入线的距离进入点最近的形状点
    select * into v_link_row from rd_link where link_pid = p_in_link_pid;
  
    if v_link_row.s_node_pid = p_in_node_pid then
      v_in_lng1 := v_link_row.geometry.sdo_ordinates(3);
      v_in_lat1 := v_link_row.geometry.sdo_ordinates(4);
      v_in_lng2 := v_link_row.geometry.sdo_ordinates(1);
      v_in_lat2 := v_link_row.geometry.sdo_ordinates(2);
    else
      declare
        v_cnt_cs int := v_link_row.geometry.sdo_ordinates.count;
      begin
        v_in_lng1 := v_link_row.geometry.sdo_ordinates(v_cnt_cs - 3);
        v_in_lat1 := v_link_row.geometry.sdo_ordinates(v_cnt_cs-2);
        v_in_lng2 := v_link_row.geometry.sdo_ordinates(v_cnt_cs - 1);
        v_in_lat2 := v_link_row.geometry.sdo_ordinates(v_cnt_cs);
      end;
    end if;
  end;

  procedure get_restrict_out_shape(p_out_link_pid in int,
                                   p_out_node_pid in int,
                                   v_out_lng1     out number,
                                   v_out_lat1     out number,
                                   v_out_lng2     out number,
                                   v_out_lat2     out number)
  
   is
    v_link_row rd_link%rowtype;
  begin
    select * into v_link_row from rd_link where link_pid = p_out_link_pid;
  
    if v_link_row.s_node_pid = p_out_node_pid then
      v_out_lng1 := v_link_row.geometry.sdo_ordinates(1);
      v_out_lat1 := v_link_row.geometry.sdo_ordinates(2);
      v_out_lng2 := v_link_row.geometry.sdo_ordinates(3);
      v_out_lat2 := v_link_row.geometry.sdo_ordinates(4);
    else
      declare
        v_cnt_cs int := v_link_row.geometry.sdo_ordinates.count;
      begin
        v_out_lng1 := v_link_row.geometry.sdo_ordinates(v_cnt_cs - 1);
        v_out_lat1 := v_link_row.geometry.sdo_ordinates(v_cnt_cs);
        v_out_lng2 := v_link_row.geometry.sdo_ordinates(v_cnt_cs - 3);
        v_out_lat2 := v_link_row.geometry.sdo_ordinates(v_cnt_cs - 2);
      end;
    end if;
  end;

  procedure get_restrict_via(p_in_link_pid  in int,
                             p_in_node_pid  in int,
                             p_out_link_pid in int,
                             v_out_lng1     out number,
                             v_out_lat1     out number,
                             v_out_lng2     out number,
                             v_out_lat2     out number,
                             v_via_path     out varchar2) is
  
    geo1           sdo_geometry;
    box1           sdo_geometry;
    geo2           sdo_geometry;
    box2           sdo_geometry;
    union_box      sdo_geometry;
    buffer_box     sdo_geometry;
    v_area         number;
    v_via_cnt      int;
    v_link_row     rd_link%rowtype;
    v_out_node_pid int;
  begin
    select geometry into geo1 from rd_link where link_pid = p_in_link_pid;
    box1 := sdo_geom.sdo_mbr(geo1);
    select geometry into geo2 from rd_link where link_pid = p_out_link_pid;
    box2       := sdo_geom.sdo_mbr(geo2);
    union_box  := sdo_geom.sdo_mbr(sdo_geom.sdo_union(box1, box2, 0.1));
    buffer_box := sdo_geom.sdo_buffer(union_box, 0.1, 0.1, 'UNIT=KILOMETER');
    v_area     := sdo_geom.sdo_area(buffer_box, 0.1, 'unit=SQ_KM');
  
    if v_area <= 4 then
      insert into tmp_restrict
        select link_pid, s_node_pid, e_node_pid, direct
          from rd_link a
         where sdo_within_distance(geometry, buffer_box, 'distance=0') =
               'TRUE' and u_record !=2;
               
               
      insert into tmp_restrict a
        select link_pid, e_node_pid, s_node_pid, 0
          from tmp_restrict
         where direct = 1;
         
          
    
      update tmp_restrict
         set s_node_pid = e_node_pid, e_node_pid = s_node_pid
       where direct = 3;
       
        
    
      insert into tmp_restrict2
        select *
          from (select a.*, sys_connect_by_path(link_pid, ',') via_path
                  from tmp_restrict a
                connect by nocycle s_node_pid = prior e_node_pid
                 start with link_pid = p_in_link_pid
                        and e_node_pid = p_in_node_pid)
         where link_pid = p_out_link_pid;
    
      v_via_cnt := sql%rowcount;
    
      if v_via_cnt > 0 then
        if v_via_cnt = 1 then
          select s_node_pid, via_path
            into v_out_node_pid, v_via_path
            from tmp_restrict2;
        else
          --计算距离最短的经过线
          declare
            v_min_length number := 100;
            v_tmp_length number;
          begin
            for i in (select * from tmp_restrict2) loop
              select sum(sdo_geom.sdo_length(geometry,
                                             0.1,
                                             'unit=kilometer'))
                into v_tmp_length
                from rd_link a,
                     (select regexp_substr(i.via_path, '[0-9]+', 1, level) link_pid
                        from dual
                      connect by prior dbms_random.value is not null
                             and level <= regexp_count(i.via_path, '[0-9]+')) b
               where a.link_pid = to_number(b.link_pid);
            
              if v_tmp_length < v_min_length then
              
                v_min_length := v_tmp_length;
              
          
                  v_out_node_pid := i.s_node_pid;
                  v_via_path := i.via_path;
              end if;
            end loop;
          end;
        end if;
      
        --计算退出点
        get_restrict_out_shape(p_out_link_pid,
                               v_out_node_pid,
                               v_out_lng1,
                               v_out_lat1,
                               v_out_lng2,
                               v_out_lat2);
      
      else
      
        --没有经过线，则计算距离，找一个距离进入点最近的那个点作为退出点
      
        select *
          into v_link_row
          from rd_link
         where link_pid = p_out_link_pid;
      
        for i in (select a.node_pid
                    from rd_node a, rd_node b
                   where a.node_pid in
                         (v_link_row.s_node_pid, v_link_row.e_node_pid)
                     and b.node_pid = p_in_node_pid
                   order by sdo_geom.sdo_distance(a.geometry,
                                                  b.geometry,
                                                  0.1)) loop
          if v_out_node_pid is null then
            v_out_node_pid := i.node_pid;
          
            get_restrict_out_shape(p_out_link_pid,
                                   v_out_node_pid,
                                   v_out_lng1,
                                   v_out_lat1,
                                   v_out_lng2,
                                   v_out_lat2);
          end if;
        end loop;
      end if;
    
      commit;
    else
    
      --没有经过线，则计算距离，找一个距离进入点最近的那个点作为退出点
    
      select *
        into v_link_row
        from rd_link
       where link_pid = p_out_link_pid;
    
      for i in (select a.node_pid
                  from rd_node a, rd_node b
                 where a.node_pid in
                       (v_link_row.s_node_pid, v_link_row.e_node_pid)
                   and b.node_pid = p_in_node_pid
                 order by sdo_geom.sdo_distance(a.geometry, b.geometry, 0.1)) loop
        if v_out_node_pid is null then
          v_out_node_pid := i.node_pid;
        
          get_restrict_out_shape(p_out_link_pid,
                                 v_out_node_pid,
                                 v_out_lng1,
                                 v_out_lat1,
                                 v_out_lng2,
                                 v_out_lat2);
        end if;
      end loop;
    end if;
  
  end;

  function get_restrict_points(p_in_link_pid int,
                               p_in_node_pid int,
                               out_link_pids varchar2)
    return rows_record_restrict
    pipelined is
    v_in_lng1 number;
    v_in_lat1 number;
    v_in_lng2 number;
    v_in_lat2 number;
  
    v_link_row rd_link%rowtype;
  
    v_record_row record_restrict;
    pragma autonomous_transaction;
  begin
  
    get_restrict_in_shape(p_in_link_pid,p_in_node_pid,v_in_lng1,v_in_lat1,v_in_lng2,v_in_lat2);
  
    for out_link in (select regexp_substr(out_link_pids, '[0-9]+', 1, level) pid
                       from dual
                     connect by prior dbms_random.value is not null
                            and level <=
                                regexp_count(out_link_pids, '[0-9]+')) loop
    
      --判断是路口交限还是线线交限
      declare
      
        v_cnt_cross int;
      
        v_out_node_pid int;
        v_out_lng1     number;
        v_out_lat1     number;
        v_out_lng2     number;
        v_out_lat2     number;
		v_via_path varchar2(512);
      begin
        select count(*)
          into v_cnt_cross
          from rd_cross_node a, rd_link b
         where a.node_pid = p_in_node_pid
           and b.link_pid = p_in_link_pid
           and exists
         (select null
                  from rd_cross_node c, rd_link d
                 where a.pid = c.pid
                   and d.link_pid = out_link.pid
                   and c.node_pid in (d.s_node_pid, d.e_node_pid));
            get_restrict_via(p_in_link_pid,
                             p_in_node_pid,
                             out_link.pid,
                             v_out_lng1,
                             v_out_lat1,
                             v_out_lng2,
                             v_out_lat2,
                             v_via_path);
        if v_cnt_cross > 0 then
          --路口交限
          select *
            into v_link_row
            from rd_link
           where link_pid = out_link.pid;
          for i in (select a.node_pid
                      from rd_node a, rd_node b
                     where a.node_pid in
                           (v_link_row.s_node_pid, v_link_row.e_node_pid)
                       and b.node_pid = p_in_node_pid
                     order by sdo_geom.sdo_distance(a.geometry,
                                                    b.geometry,
                                                    0.1)) loop
            if v_out_node_pid is null then
              v_out_node_pid := i.node_pid;
            
              get_restrict_out_shape(out_link.pid,
                                     v_out_node_pid,
                                     v_out_lng1,
                                     v_out_lat1,
                                     v_out_lng2,
                                     v_out_lat2);
            
              v_record_row.link_pid := out_link.pid;
            
              v_record_row.in_node1      := v_in_lng1 || ',' || v_in_lat1;
              v_record_row.in_node2      := v_in_lng2 || ',' || v_in_lat2;
              v_record_row.out_node1     := v_out_lng1 || ',' || v_out_lat1;
              v_record_row.out_node2     := v_out_lng2 || ',' || v_out_lat2;
              v_record_row.relation_type := 1;
			  v_record_row.via_path      := v_via_path;
            
              pipe row(v_record_row);
            end if;
          
          end loop;
        
        else
            v_record_row.link_pid := out_link.pid;
          
            v_record_row.in_node1      := v_in_lng1 || ',' || v_in_lat1;
            v_record_row.in_node2      := v_in_lng2 || ',' || v_in_lat2;
            v_record_row.out_node1     := v_out_lng1 || ',' || v_out_lat1;
            v_record_row.out_node2     := v_out_lng2 || ',' || v_out_lat2;
            v_record_row.relation_type := 2;
            v_record_row.via_path      := v_via_path;
            pipe row(v_record_row);
        end if;
      
      end;
    
    end loop;
  
  end;
  
FUNCTION num_to_bin (p_num NUMBER) RETURN VARCHAR2
IS
   r_binstr   VARCHAR2 (32767);
   l_num      NUMBER           := p_num;
BEGIN
   WHILE l_num != 0 LOOP
      r_binstr := TO_CHAR (MOD (l_num, 2)) || r_binstr;
      l_num := TRUNC (l_num / 2);
   END LOOP;
   if r_binstr is null then
     return '0';
   end if;
   RETURN r_binstr;
END num_to_bin;

function parse_vehicle(vehicle number) return number
is
v_vehicle varchar2(32);
begin
  v_vehicle := lpad(num_to_bin(vehicle),32,0);

  if substr(v_vehicle,1,1) ='0' then
    if substr(v_vehicle,30,1)='1' or substr(v_vehicle,31,1)='1' then
      return 1;
    else
      return 0;
    end if;
  else
    if substr(v_vehicle,30,1)='0' or substr(v_vehicle,31,1)='0' then
      return 1;
    else
      return 0;
    end if;
  end if;

end;

function track_links(p_link_pid number,
                                       p_link_dir number) return varchar2 is
  v_pre_s_node_pid int;
  v_pre_e_node_pid int;
  v_pre_direct     int;
  v_pre_link_pid   int := p_link_pid;
  v_path           varchar2(4000) := p_link_pid;
  type myrecord1 is record(
    link_pid     int,
    v_s_node_pid int,
    v_e_node_pid int,
    v_direct     int,
    pid          int);
  type records is table of myrecord1;
  v_re records;
begin
  select decode(p_link_dir, 2, s_node_pid, e_node_pid),
         decode(p_link_dir, 2, e_node_pid, s_node_pid)
    into v_pre_s_node_pid, v_pre_e_node_pid
    from rd_link
   where link_pid = p_link_pid;

  loop
    select a.link_pid, a.s_node_pid, a.e_node_pid, a.direct
    ,b.pid
     bulk collect
      into v_re
      from rd_link a
      ,rd_speedlimit b
     where a.link_pid != v_pre_link_pid
     and a.link_pid = b.link_pid(+)
       and v_pre_e_node_pid in (s_node_pid, e_node_pid);
    exit when v_re.count > 1 or v_re.count = 0;
   
    if v_re(1).v_s_node_pid = v_pre_e_node_pid then
      v_pre_e_node_pid := v_re(1).v_e_node_pid;
    else
      v_pre_e_node_pid := v_re(1).v_s_node_pid;
    end if;
    exit when v_re(1).pid is not null;
    v_pre_link_pid := v_re(1).link_pid;
    v_path         := v_path || ',' || v_pre_link_pid;
  end loop;
  return v_path;

end;

end;
/

create or replace package package_check is
  type rows_ruleid is table of varchar2(50);
  function fun_check(restric_pid int) return rows_ruleid
    pipelined;
end;
/

create or replace package body package_check is
  function fun_GLM08049(p_restric_pid int) return int is
    --获取路口pid
    cross_pid int;
    --退出点
    out_node_pid int;
    --结果数
    v_cnt int;
    --进入点
    v_in_node_pid int;
  begin
    select node_pid
      into v_in_node_pid
      from rd_restriction
     where pid = p_restric_pid;
    select pid
      into cross_pid
      from rd_cross_node
     where node_pid = v_in_node_pid;
  
    with tmp1 as
     (select a.pid, b.s_node_pid, b.e_node_pid, b.direct, b.link_pid
        from rd_cross_link a, rd_link b
       where a.pid = cross_pid
         and a.link_pid = b.link_pid),
    tmp2 as
     (select pid, s_node_pid, e_node_pid, link_pid
        from tmp1
       where direct = 2),
    tmp3 as
     (select pid, e_node_pid e, s_node_pid, link_pid
        from tmp1
       where direct = 3),
    tmp4 as
     (select pid, s_node_pid, e_node_pid, link_pid
        from tmp1
       where direct = 1
      union all
      select pid, e_node_pid, s_node_pid, link_pid
        from tmp1
       where direct = 1),
    tmp5 as
     (select *
        from tmp2
      union all
      select *
        from tmp3
      union all
      select *
        from tmp4),
    tmp6 as
     (select e_node_pid
        from tmp5
      connect by nocycle pid = prior pid
             and prior e_node_pid = s_node_pid
       start with s_node_pid = v_in_node_pid)
    select count(*)
      into v_cnt
      from tmp6
     where e_node_pid in
           (select node_pid
              from rd_cross_node a
             where a.pid = cross_pid
               and exists
             (select null
                      from rd_link b
                     where b.link_pid in
                           (select out_link_pid
                              from rd_restriction_detail
                             where restric_pid = p_restric_pid)
                       and a.node_pid in (b.s_node_pid, b.e_node_pid)));
  
    if v_cnt > 0 then
      return 0;
    else
      return 1;
    end if;
  
  exception
    when no_data_found then
      return 0;
    
  end;

  function fun_GLM08044_2(p_restric_pid int) return int is
    v_cnt int;
  begin
    select count(*)
      into v_cnt
      from rd_restriction a
     where exists (select null
              from rd_restriction b
             where pid = p_restric_pid
               and a.in_link_pid = b.in_link_pid
               and a.node_pid = b.node_pid)
     group by in_link_pid, node_pid;
    if v_cnt > 1 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_GLM08044_1(p_restric_pid int) return int is
    v_cnt int;
  begin
    select count(*)
      into v_cnt
      from rd_restriction_detail a
     where a.relationship_type = 2
       and a.restric_pid = p_restric_pid
     ;
    if v_cnt > 1 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_GLM08040(p_restric_pid int) return int is
    v_cnt int;
  begin
    select count(*)
      into v_cnt
      from rd_restriction_detail a
     where restric_pid = p_restric_pid
       and type = 2
       and exists (select null
              from rd_restriction_condition b
             where a.detail_id = b.detail_id
               and b.time_domain is null);
    if v_cnt > 0 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_GLM08039(p_restric_pid int) return int is
    v_cnt int;
  begin
    select count(*)
      into v_cnt
      from rd_restriction_detail a
     where restric_pid = p_restric_pid
       and type = 0;
    if v_cnt > 0 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_GLM08033(p_restric_pid int) return int is
    v_cnt int;
  begin
    select count(*)
      into v_cnt
      from rd_cross_link a
     where link_pid in (select in_link_pid
                          from rd_restriction
                         where pid = p_restric_pid
                        union all
                        select out_link_pid
                          from rd_restriction_detail
                         where restric_pid = p_restric_pid);
    if v_cnt > 0 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_GLM08006_1_GLM08005_1(p_restric_pid int) return int is
    v_cnt int;
  begin
    select count(*)
      into v_cnt
      from rd_restriction_condition a
     where exists (select null
              from rd_restriction_detail b
             where a.detail_id = b.detail_id
               and b.restric_pid = p_restric_pid);
    if v_cnt > 0 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_GLM08004_1_GLM08004_2(p_restric_pid int) return int is
    v_cnt int;
  begin
    with tmp1 as
     (select in_link_pid link_pid, pid
        from rd_restriction
       where pid = p_restric_pid),
    tmp2 as
     (select out_link_pid link_pid, detail_id
        from rd_restriction_detail
       where restric_pid = p_restric_pid),
    tmp3 as
     (select link_pid, 1
        from rd_restriction_via
       where detail_id in (select detail_id from tmp2))
    select count(*)
      into v_cnt
      from rd_link
     where link_pid in (select link_pid
                          from tmp1
                        union all
                        select link_pid
                          from tmp2
                        union all
                        select link_pid
                          from tmp3)
       and link_pid in
           (select link_pid from rd_link_form where form_of_way in (20, 22));
    if v_cnt > 0 then
      return 1;
    else
      return 0;
    end if;
  end;

  function fun_check(restric_pid int) return rows_ruleid
    pipelined is
    v_num number(1);
  begin
    v_num := fun_GLM08049(restric_pid);
    if v_num = 1 then
      pipe row('GLM08049');
    end if;
  
    /*v_num := fun_GLM08044_2(restric_pid);
    if v_num = 1 then
      pipe row('GLM08044_2');
    end if;*/
  
    v_num := fun_GLM08044_1(restric_pid);
    if v_num = 1 then
      pipe row('GLM08044_1');
    end if;
  
    /*v_num := fun_GLM08040(restric_pid);
    if v_num = 1 then
      pipe row('GLM08040');
    end if;*/
  
    v_num := fun_GLM08039(restric_pid);
    if v_num = 1 then
      pipe row('GLM08039');
    end if;
  
    v_num := fun_GLM08033(restric_pid);
    if v_num = 1 then
      pipe row('GLM08033');
    end if;
  
    /*v_num := fun_GLM08006_1_GLM08005_1(restric_pid);
    if v_num = 1 then
      pipe row('GLM08006_1_GLM08005_1');
    end if;
  
    v_num := fun_GLM08004_1_GLM08004_2(restric_pid);
    if v_num = 1 then
      pipe row('GLM08004_1_GLM08004_2');
    end if;
    
    
    -----------------------------------
    v_num := fun_GLM08049(restric_pid);
    if v_num = 1 then
      pipe row('GLM08049');
    end if;
  
    v_num := fun_GLM08044_2(restric_pid);
    if v_num = 1 then
      pipe row('GLM08044_2');
    end if;
  
    v_num := fun_GLM08044_1(restric_pid);
    if v_num = 1 then
      pipe row('GLM08044_1');
    end if;
  
    v_num := fun_GLM08040(restric_pid);
    if v_num = 1 then
      pipe row('GLM08040');
    end if;
  
    v_num := fun_GLM08039(restric_pid);
    if v_num = 1 then
      pipe row('GLM08039');
    end if;
  
    v_num := fun_GLM08033(restric_pid);
    if v_num = 1 then
      pipe row('GLM08033');
    end if;
  
    v_num := fun_GLM08006_1_GLM08005_1(restric_pid);
    if v_num = 1 then
      pipe row('GLM08006_1_GLM08005_1');
    end if;
  
    v_num := fun_GLM08004_1_GLM08004_2(restric_pid);
    if v_num = 1 then
      pipe row('GLM08004_1_GLM08004_2');
    end if;*/
  end fun_check;
end;
/

begin
  for a in (select distinct table_name from user_indexes where table_name like 'RD%') loop
    execute immediate 'analyze table ' || a.table_name ||
                      ' compute statistics';
  end loop;
end;
/

