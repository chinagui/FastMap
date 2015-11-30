create or replace package pk_batch_face authid current_user is

  /*
  �������ж�face link�Ĳ����ҳ����˵��棬
  1���ҵ�������ߣ����¼��������������һ����Ȼ������ʱ��˳����б��
  2�����������¹��桢�����ܳ������
  --2.5W
  AD_FACE
  AD_FACE_TOPO
  CMG_BUILDFACE
  CMG_BUILDFACE_TOPO
  CM_BUILDFACE
  CM_BUILDFACE_TOPO
  LU_FACE
  LU_FACE_TOPO
  ZONE_FACE
  ZONE_FACE_TOPO
  LC_FACE
  LC_FACE_TOPO
  
  --20W
  LC_FACE_20W
  LC_FACE_TOPO_20W
  
  --100W
  LC_FACE_100W
  LC_FACE_TOPO_100W
  
  --TOP
  LC_FACE_TOP
  LC_FACE_TOPO_TOP
  */
  v_elem_infos constant sdo_elem_info_array := sdo_elem_info_array(1,
                                                                   1003,
                                                                   1);
  type topo is record(
    face_pid number(10),
    link_pid number(10),
    seq      number(3));
  type face is record(
    face_pid  number(10),
    geometry  mdsys.sdo_geometry,
    area      number(30, 6),
    perimeter number(15, 3));
  type raw_topo is record(
    face_pid   number(10),
    face_geo   mdsys.sdo_geometry,
    area       number(30, 6),
    perimeter  number(15, 3),
    link_pid   number(10),
    s_node_pid number(10),
    e_node_pid number(10),
    geometry   mdsys.sdo_geometry);
  type raw_face is record(
    face_pid  number(10),
    face_geo  mdsys.sdo_geometry,
    area      number(30, 6),
    perimeter number(15, 3),
    link_pid  number(10),
    seq       number(3),
    geometry  mdsys.sdo_geometry);
  type cursor_raw_topo is ref cursor return raw_topo;
  type cursor_raw_face is ref cursor return raw_face;
  type raw_topos is table of raw_topo;
  type raw_faces is table of raw_face;
  type topos is table of topo;
  type faces is table of face;
  type raw_topo_map is table of raw_topo index by binary_integer;
  TYPE repeat_link_cursor_type IS REF CURSOR;

  function sort_face_topo(p_cursor_raw_topo cursor_raw_topo,
                          topo_tab_name     varchar2,
                          link_tab_name     varchar2,
                          face_tab_name     varchar2) return topos
    pipelined order p_cursor_raw_topo by(face_pid)
    parallel_enable(partition p_cursor_raw_topo by range(face_pid));

  function construct_face(p_cursor_raw_face cursor_raw_face,
                          topo_tab_name     varchar2,
                          link_tab_name     varchar2,
                          face_tab_name     varchar2,
                          p_calc_area       boolean default true,
                          p_calc_perimeter  boolean default true
                          
                          ) return faces
    pipelined order p_cursor_raw_face by(face_pid, seq)
    parallel_enable(partition p_cursor_raw_face by range(face_pid));

  procedure re_calc_top_face;
  procedure re_calc_100w_face;
  procedure re_calc_20w_face;
  procedure re_calc_2_5w_face;
  procedure re_calc_face(p_version_id pls_integer, p_scale pls_integer);

  procedure create_temp_table;

  function is_polygon_counterclockwise(p_geo in mdsys.sdo_geometry)
    return varchar2;

  procedure judge_face_repate_link(v_topo_table_name varchar2,
                                   v_face_table_name varchar2,
                                   err_count         OUt NUMBER);

end pk_batch_face;
/
create or replace package body pk_batch_face is
  procedure log(table_name varchar2, face_pid number, msg varchar2) as
    vsql varchar2(1000);
  begin
    vsql := 'insert into tmp_batch_face_msg (table_name,face_pid,mesh_id,msg)values(''' ||
            table_name || ''',' || face_pid || ',(select mesh_id from ' ||
            table_name || ' where face_pid=' || face_pid || '),''' || msg ||
            ''')';
    execute immediate vsql;
  end;
  procedure array_copy(v_ordinates      in out nocopy sdo_ordinate_array,
                       v_link_ordinates in out nocopy sdo_ordinate_array,
                       v_index          in out nocopy pls_integer,
                       v_reverse_flag   boolean,
                       v_first          boolean default false) is
    v_count pls_integer;
    v_begin pls_integer;
  begin
    if not v_first then
      --������ǵ�һ������
      v_begin := 3;
      v_count := v_link_ordinates.count / 2 - 1;
      v_ordinates.extend(v_link_ordinates.count - 2);
    else
      v_begin := 1;
      v_count := v_link_ordinates.count / 2;
      v_ordinates.extend(v_link_ordinates.count);
    end if;
    if not v_reverse_flag then
      --�����˳����
      for j in v_begin .. v_link_ordinates.count loop
        v_ordinates(v_index) := v_link_ordinates(j);
        v_index := v_index + 1;
      end loop;
    else
      --����ǵ�����
      while v_count > 0 loop
        v_ordinates(v_index) := v_link_ordinates(2 * v_count - 1);
        v_index := v_index + 1;
        v_ordinates(v_index) := v_link_ordinates(2 * v_count);
        v_index := v_index + 1;
        v_count := v_count - 1;
      end loop;
    end if;
  end array_copy;

  function join_face_line(v_ordinates      in out nocopy sdo_ordinate_array,
                          v_link_ordinates in out nocopy sdo_ordinate_array,
                          v_index          in out nocopy pls_integer)
    return boolean is
    v_join_flag boolean := false;
  begin
    if v_ordinates(v_index - 2) = v_link_ordinates(1) and
       v_ordinates(v_index - 1) = v_link_ordinates(2) then
      --����������
      array_copy(v_ordinates, v_link_ordinates, v_index, false);
      v_join_flag := true;
    elsif v_ordinates(v_index - 2) =
          v_link_ordinates(v_link_ordinates.count - 1) and
          v_ordinates(v_index - 1) =
          v_link_ordinates(v_link_ordinates.count) then
      --����β����
      array_copy(v_ordinates, v_link_ordinates, v_index, true);
      v_join_flag := true;
    end if;
    return v_join_flag;
  end join_face_line;

  --private
  function construct_one_face(p_raw_faces      in out nocopy raw_faces,
                              p_face_pid       in out nocopy pls_integer,
                              p_calc_area      boolean default true,
                              p_calc_perimeter boolean default true,
                              p_face           face, --���ĳ��������link����ʧ�ܣ��򷵻�ԭ�����棬�ñ�����������ԭ�����棻
                              topo_tab_name    varchar2,
                              link_tab_name    varchar2,
                              face_tab_name    varchar2) return face is
    v_ordinates          sdo_ordinate_array := sdo_ordinate_array();
    v_geo                mdsys.sdo_geometry;
    v_link_ordinates     sdo_ordinate_array;
    v_index              pls_integer := 1;
    v_topo_index         pls_integer := 1;
    v_join_flag          boolean;
    v_face               face;
    v_first_reverse_flag boolean;
  begin
    v_face := p_face;
    for i in 1 .. p_raw_faces.count loop
      v_link_ordinates := p_raw_faces(i).geometry.sdo_ordinates;
      if v_topo_index = 1 then
        --����ǵ�һ����,ֱ�Ӹ�������
        array_copy(v_ordinates, v_link_ordinates, v_index, false, true);
      else
        --����ǵ�n����,ʹ���ߵ�ĩ�����n���߽�������
        v_join_flag := join_face_line(v_ordinates,
                                      v_link_ordinates,
                                      v_index);
        if not v_join_flag and v_topo_index = 2 then
          --���ߵ�ĩ������δ�ɹ������Ϊ�ڶ����ߣ�����ʹ�����ȥ���ӵ�n����
          --��ת v_ordinates
          v_first_reverse_flag := true;
          sdo_util.internal_reverse_line_points(v_ordinates);
          v_join_flag := join_face_line(v_ordinates,
                                        v_link_ordinates,
                                        v_index);
        elsif not v_join_flag then
          --˵���߲��պϣ�������
          log(face_tab_name,
              p_face_pid,
              '����ʱ�����治�պϣ����Ϊ��' || face_tab_name || ':' || p_face_pid ||
              '���ߺ�Ϊ' || link_tab_name || ':' || p_raw_faces(i).link_pid);
          --   raise_application_error(-20999,
          --                           '����ʱ�����治�պϣ����Ϊ��' || p_face_pid || '���ߺ�Ϊ' || p_raw_faces(i)
          --                           .link_pid,
          --                           true);
          return v_face;
        end if;
      end if;
      v_topo_index := v_topo_index + 1;
    end loop;
  
    if v_ordinates.count < 3 then
      log(face_tab_name,
          p_face_pid,
          '��״��������3�����޷����棬���Ϊ��' || face_tab_name || ':' || p_face_pid);
      --  raise_application_error(-20999,
      --                         '��״��������3�����޷����棬���Ϊ��' || p_face_pid,
      --                         true);
      return v_face;
    end if;
  
    if v_ordinates(1) <> v_ordinates(v_ordinates.count - 1) or
       v_ordinates(2) <> v_ordinates(v_ordinates.count) then
      log(face_tab_name,
          p_face_pid,
          '����ʱ�����治�պϣ����Ϊ��' || face_tab_name || ':' || p_face_pid);
      -- raise_application_error(-20999,
      --                        '����ʱ�����治�պϣ����Ϊ��' || p_face_pid,
      --                         true);
      return v_face;
    end if;
    v_geo := sdo_geometry(2003, 8307, null, v_elem_infos, v_ordinates);
    if p_raw_faces.count = 1 then
      --һ���ߵ���,���⴦���ߵķ���
      if is_polygon_counterclockwise(v_geo) = 'FALSE' then
        sdo_util.internal_reverse_line_points(v_ordinates);
        v_geo := sdo_geometry(2003, 8307, null, v_elem_infos, v_ordinates);
      end if;
    elsif p_raw_faces.count = 2 then
      --�����ߵ���,���⴦���ߵķ���
      if is_polygon_counterclockwise(v_geo) = 'FALSE' then
        v_index := 1;
        v_ordinates.delete;
        v_ordinates      := sdo_ordinate_array();
        v_link_ordinates := p_raw_faces(1).geometry.sdo_ordinates;
        array_copy(v_ordinates, v_link_ordinates, v_index, false, true);
        sdo_util.internal_reverse_line_points(v_ordinates);
        v_link_ordinates := p_raw_faces(2).geometry.sdo_ordinates;
        v_join_flag      := join_face_line(v_ordinates,
                                           v_link_ordinates,
                                           v_index);
        v_geo            := sdo_geometry(2003,
                                         8307,
                                         null,
                                         v_elem_infos,
                                         v_ordinates);
      end if;
    end if;
    v_face.face_pid  := p_face_pid;
    v_face.geometry  := v_geo;
    v_face.area      := sdo_geom.sdo_area(v_geo, 0.05);
    v_face.perimeter := sdo_geom.sdo_length(v_geo, 0.05);
    return v_face;
  end;

  --private
  function sort_one_face_topo(p_raw_topo_map1  in out nocopy raw_topo_map,
                              p_raw_topo_map2  in out nocopy raw_topo_map,
                              p_face_pid       in out nocopy pls_integer,
                              p_start_node_pid in out nocopy pls_integer,
                              p_direct_flag    boolean default true,
                              p_face           face,
                              topo_tab_name    varchar2,
                              link_tab_name    varchar2,
                              face_tab_name    varchar2
                              
                              ) return topos is
    v_last_face_pid  pls_integer;
    v_last_node_pid  pls_integer;
    v_first_link_pid pls_integer;
    v_last_link_pid  pls_integer;
    v_seq            pls_integer := 2;
    v_raw_topo       raw_topo;
    v_topo           topo;
    v_topos          topos := topos();
    v_raw_faces      raw_faces := raw_faces();
    v_raw_face       raw_face;
    v_face           face;
  begin
    --�õ���һ����
    v_last_node_pid  := p_start_node_pid;
    v_raw_topo       := p_raw_topo_map1(v_last_node_pid);
    v_first_link_pid := v_raw_topo.link_pid;
    v_topo.face_pid  := v_raw_topo.face_pid;
    v_topo.link_pid  := v_raw_topo.link_pid;
    v_topo.seq       := 1;
    v_last_link_pid  := v_raw_topo.link_pid;
    v_topos.extend(1);
    v_topos(v_topos.count()) := v_topo;
  
    v_raw_face.face_pid := v_topo.face_pid;
    v_raw_face.link_pid := v_topo.link_pid;
    v_raw_face.seq      := v_topo.seq;
    v_raw_face.geometry := v_raw_topo.geometry;
    v_raw_faces.extend(1);
    v_raw_faces(v_raw_faces.count) := v_raw_face;
    while true loop
      v_raw_topo := p_raw_topo_map1(v_last_node_pid);
      if v_raw_topo.link_pid = v_last_link_pid then
        --�һ����Լ�����Ҫ����һ��map���ҵ���һ��link
        if not p_raw_topo_map2.exists(v_last_node_pid) then
          exit;
        end if;
        v_raw_topo := p_raw_topo_map2(v_last_node_pid);
        if v_raw_topo.link_pid = v_last_link_pid then
          --һ���ߵ���
          exit;
        end if;
      end if;
      exit when v_raw_topo.link_pid = v_first_link_pid;
      v_topo.face_pid := v_raw_topo.face_pid;
      v_topo.link_pid := v_raw_topo.link_pid;
      v_topo.seq      := v_seq;
      v_seq           := v_seq + 1;
      v_topos.extend(1);
      v_topos(v_topos.count()) := v_topo;
    
      v_raw_face.face_pid := v_topo.face_pid;
      v_raw_face.link_pid := v_topo.link_pid;
      v_raw_face.seq      := v_topo.seq;
      v_raw_face.geometry := v_raw_topo.geometry;
      v_raw_faces.extend(1);
      v_raw_faces(v_raw_faces.count) := v_raw_face;
    
      --�ı������ʵĽ��
      if v_last_node_pid = v_raw_topo.s_node_pid then
        v_last_node_pid := v_raw_topo.e_node_pid;
      else
        v_last_node_pid := v_raw_topo.s_node_pid;
      end if;
      --�ı������ʵ���
      v_last_link_pid := v_raw_topo.link_pid;
    end loop;
    --����˳������
    v_face := construct_one_face(v_raw_faces,
                                 p_face_pid,
                                 true,
                                 true,
                                 p_face,
                                 topo_tab_name,
                                 link_tab_name,
                                 face_tab_name);
    --�ж����˳�淽�����������Ȧ��ʱ�ӷ�����˳���������
    if v_topos.count > 2 and
       is_polygon_counterclockwise(v_face.geometry) = 'FALSE' then
      for i in 1 .. v_topos.count loop
        v_topos(i).seq := v_topos.count - v_topos(i).seq + 1;
      end loop;
    end if;
    return v_topos;
  end;

  --�Դ����topo��ϵ�Ŷ�˳��
  function sort_face_topo(p_cursor_raw_topo cursor_raw_topo,
                          topo_tab_name     varchar2,
                          link_tab_name     varchar2,
                          face_tab_name     varchar2) return topos
    pipelined order p_cursor_raw_topo by(face_pid)
    parallel_enable(partition p_cursor_raw_topo by range(face_pid)) is
  
    v_raw_topos      raw_topos;
    v_raw_topo       raw_topo;
    v_raw_topo_map1  raw_topo_map;
    v_raw_topo_map2  raw_topo_map;
    v_last_face_pid  pls_integer;
    v_start_node_pid pls_integer;
    v_finish_flag    boolean := false;
    v_topos          topos;
    v_face           face;
  begin
    loop
      if p_cursor_raw_topo%notfound then
        --������Ѿ�������
        if v_last_face_pid is null then
          --������ǿյ�,���ô���
          exit;
        end if;
        --�ǿ������һ���ս��־
        v_finish_flag := true;
        v_raw_topos   := raw_topos();
        v_raw_topos.extend(1);
        v_raw_topo.face_pid := -9999;
        v_raw_topos(1) := v_raw_topo;
      else
        fetch p_cursor_raw_topo bulk collect
          into v_raw_topos limit 1000;
      end if;
      for i in 1 .. v_raw_topos.count loop
      
        if v_last_face_pid is null or
           v_last_face_pid <> v_raw_topos(i).face_pid then
          if v_last_face_pid <> v_raw_topos(i).face_pid then
            --��Ҫ����һ��˳��
            v_topos := sort_one_face_topo(v_raw_topo_map1,
                                          v_raw_topo_map2,
                                          v_last_face_pid,
                                          v_start_node_pid,
                                          true,
                                          v_face,
                                          topo_tab_name,
                                          link_tab_name,
                                          face_tab_name);
            for j in 1 .. v_topos.count loop
              pipe row(v_topos(j));
            end loop;
          end if;
          --�����һ�鴦�����ʱ���˳���һ��Ĵ���
          exit when v_finish_flag;
          v_last_face_pid  := v_raw_topos(i).face_pid;
          v_start_node_pid := v_raw_topos(i).s_node_pid;
          v_raw_topo_map1.delete;
          v_raw_topo_map2.delete;
          v_face.face_pid  := v_raw_topos(i).face_pid;
          v_face.geometry  := v_raw_topos(i).face_geo;
          v_face.area      := v_raw_topos(i).area;
          v_face.perimeter := v_raw_topos(i).perimeter;
        end if;
        --�����˵����ߵĶ�Ӧ��ϵ
        --����������ߵĶ�Ӧ��ϵ
        if not v_raw_topo_map1.exists(v_raw_topos(i).s_node_pid) then
          v_raw_topo_map1(v_raw_topos(i).s_node_pid) := v_raw_topos(i);
        elsif not v_raw_topo_map2.exists(v_raw_topos(i).s_node_pid) then
          v_raw_topo_map2(v_raw_topos(i).s_node_pid) := v_raw_topos(i);
        else
          log(face_tab_name,
              v_last_face_pid,
              '��һ������һ�����������߲�Ϊ2,���Ϊ:' || face_tab_name || ':' ||
               v_last_face_pid || '���ߺ�Ϊ' || link_tab_name || ':' || v_raw_topos(i)
              .link_pid || '������Ϊ�� ' || v_raw_topos(i).s_node_pid);
          --  raise_application_error(-20999,
          --                          '��һ������һ�����������߲�Ϊ2,���Ϊ:' || v_last_face_pid ||
          --                                '���ߺ�Ϊ' || v_raw_topos(i).link_pid ||
          --                                '������Ϊ�� ' || v_raw_topos(i).s_node_pid,
          --                                true);
        
        end if;
        --�����յ����ߵĶ�Ӧ��ϵ
        if not v_raw_topo_map1.exists(v_raw_topos(i).e_node_pid) then
          v_raw_topo_map1(v_raw_topos(i).e_node_pid) := v_raw_topos(i);
        elsif not v_raw_topo_map2.exists(v_raw_topos(i).e_node_pid) then
          v_raw_topo_map2(v_raw_topos(i).e_node_pid) := v_raw_topos(i);
        else
          log(face_tab_name,
              v_last_face_pid,
              '��һ������һ�����������߲�Ϊ2 ,���Ϊ:' || face_tab_name || ':' ||
               v_last_face_pid || '���ߺ�Ϊ' || link_tab_name || ':' || v_raw_topos(i)
              .link_pid || '���������Ϊ�� ' || v_raw_topos(i).e_node_pid);
          --  raise_application_error(-20999,
          --                          '��һ������һ�����������߲�Ϊ2 ,���Ϊ:' ||
          --                           v_last_face_pid || '���ߺ�Ϊ' || v_raw_topos(i)
          --                         .link_pid || '���������Ϊ�� ' || v_raw_topos(i)
          --                         .e_node_pid,
          --                          true);
        
        end if;
      end loop;
      --���������������Ҫ�˳�
      exit when v_finish_flag;
    end loop;
    close p_cursor_raw_topo;
  end;

  --�����Ѿ��ź����face top��ϵ���棬������������ܳ�
  function construct_face(p_cursor_raw_face cursor_raw_face,
                          topo_tab_name     varchar2,
                          link_tab_name     varchar2,
                          face_tab_name     varchar2,
                          p_calc_area       boolean default true,
                          p_calc_perimeter  boolean default true)
    return faces
    pipelined order p_cursor_raw_face by(face_pid, seq)
    parallel_enable(partition p_cursor_raw_face by range(face_pid)) is
    v_face             face;
    v_raw_faces        raw_faces;
    v_raw_face         raw_face;
    v_last_face_pid    pls_integer;
    v_finish_flag      boolean := false;
    v_a_face_raw_faces raw_faces := raw_faces();
    p_face             face;
  begin
    loop
      if p_cursor_raw_face%notfound then
        --������Ѿ�������
        if v_last_face_pid is null then
          --������ǿյ�,���ô���
          exit;
        end if;
        --�ǿ������һ���ս��־
        v_finish_flag := true;
        v_raw_faces   := raw_faces();
        v_raw_faces.extend(1);
        v_raw_face.face_pid := -9999;
        v_raw_faces(1) := v_raw_face;
      else
        fetch p_cursor_raw_face bulk collect
          into v_raw_faces limit 1000;
      end if;
      for i in 1 .. v_raw_faces.count loop
      
        if v_last_face_pid is null or
           v_last_face_pid <> v_raw_faces(i).face_pid then
          if v_last_face_pid <> v_raw_faces(i).face_pid then
            v_face := construct_one_face(v_a_face_raw_faces,
                                         v_last_face_pid,
                                         p_calc_area,
                                         p_calc_perimeter,
                                         p_face,
                                         topo_tab_name,
                                         link_tab_name,
                                         face_tab_name);
            pipe row(v_face);
          end if;
          --�����һ�鴦�����ʱ���˳���һ��Ĵ���
          exit when v_finish_flag;
          v_last_face_pid := v_raw_faces(i).face_pid;
          v_a_face_raw_faces.delete;
          v_a_face_raw_faces := raw_faces();
        
          p_face.face_pid  := v_raw_faces(i).face_pid;
          p_face.geometry  := v_raw_faces(i).face_geo;
          p_face.area      := v_raw_faces(i).area;
          p_face.perimeter := v_raw_faces(i).perimeter;
        end if;
        v_a_face_raw_faces.extend(1);
        v_a_face_raw_faces(v_a_face_raw_faces.count) := v_raw_faces(i);
      end loop;
      --���������������Ҫ�˳�
      exit when v_finish_flag;
    end loop;
    close p_cursor_raw_face;
  end;

  procedure do_calc_face(p_face_name   varchar2,
                         p_topo_name   varchar2,
                         p_link_name   varchar2,
                         p_scale_sufix varchar2 default ' ') is
    v_repeat_link_count int;
  begin
    v_repeat_link_count := 0;
    execute immediate 'delete from tmp_imp_face_seq';
    execute immediate 'delete from tmp_imp_face_pid';
    execute immediate 'insert into tmp_imp_face_pid
      select /*+ use_hash(c) use_hash(n)*/ n.face_pid from tmp_imp_change_face c,' ||
                      p_topo_name || p_scale_sufix || ' n
      where n.face_pid = c.object_id and c.table_name in(:1,:2)
      union
      select /*+ use_hash(c) use_hash(n)*/ n.face_pid from tmp_imp_change_face c,' ||
                      p_topo_name || p_scale_sufix || ' n
      where n.link_pid = c.object_id and c.table_name = :3'
      using p_face_name, p_topo_name, p_link_name;
  
    execute immediate 'insert into tmp_imp_face_seq select *
        from table(pk_batch_face.sort_face_topo(cursor(
        select
        t.face_pid,f.geometry as face_geo,f.area,f.perimeter,l.link_pid,l.s_node_pid,l.e_node_pid,l.geometry
        from ' || p_topo_name || p_scale_sufix ||
                      ' t,' || p_link_name || p_scale_sufix || ' l,' ||
                      p_face_name || p_scale_sufix || ' f,tmp_imp_face_pid p
        where t.face_pid = p.face_pid
        and t.link_pid = l.link_pid
        and f.face_pid=p.face_pid

        ),''' || p_topo_name || p_scale_sufix ||
                      ''',''' || p_link_name || p_scale_sufix || ''',''' ||
                      p_face_name || p_scale_sufix || '''))';
  
    --�ж��Ƿ�����ظ����ݣ����������¼�治�պ�log
    judge_face_repate_link(p_topo_name || p_scale_sufix,
                           p_face_name || p_scale_sufix,
                           v_repeat_link_count);
    --��������ִ��merge
    if v_repeat_link_count = 0 then
      execute immediate 'merge into ' || p_topo_name || p_scale_sufix || ' t
     using tmp_imp_face_seq v
     on(t.face_pid = v.face_pid and t.link_pid = v.link_pid)
     when matched then
     update set t.seq_num = v.seq';
    end if;
  
    execute immediate 'delete from tmp_imp_face';
    execute immediate 'insert into tmp_imp_face select * from table(pk_batch_face.construct_face(cursor(
        select t.face_pid,f.geometry as face_geo,f.area,f.perimeter,t.link_pid,t.seq_num,l.geometry
        from ' || p_topo_name || p_scale_sufix ||
                      ' t,' || p_link_name || p_scale_sufix || ' l,' ||
                      p_face_name || p_scale_sufix || ' f,tmp_imp_face_pid p
        where t.face_pid = p.face_pid
        and t.link_pid = l.link_pid
        and f.face_pid=p.face_pid
        ),''' || p_topo_name || p_scale_sufix ||
                      ''',''' || p_link_name || p_scale_sufix || ''',''' ||
                      p_face_name || p_scale_sufix || '''))';
  
    execute immediate 'merge into ' || p_face_name || p_scale_sufix || ' f
     using tmp_imp_face v
     on(f.face_pid = v.face_pid)
     when matched then
     update set f.geometry = v.geometry,
     f.area = v.area,
     f.perimeter = v.perimeter';
  
  end;

  procedure re_calc_top_face is
  begin
    /*LC_FACE_TOP
    LC_FACE_TOPO_TOP  */
    do_calc_face('LC_FACE', 'LC_FACE_TOPO', 'LC_LINK', '_TOP');
  end;

  procedure re_calc_100w_face is
  begin
    /*LC_FACE_100W
    LC_FACE_TOPO_100W  */
    do_calc_face('LC_FACE', 'LC_FACE_TOPO', 'LC_LINK', '_100W');
  end;

  procedure re_calc_20w_face is
  begin
    /*LC_FACE_20W
    LC_FACE_TOPO_20W */
    do_calc_face('LC_FACE', 'LC_FACE_TOPO', 'LC_LINK', '_20W');
  end;

  procedure re_calc_2_5w_face is
  begin
    /*AD_FACE
    AD_FACE_TOPO
    CMG_BUILDFACE
    CMG_BUILDFACE_TOPO
    CM_BUILDFACE
    CM_BUILDFACE_TOPO
    LU_FACE
    LU_FACE_TOPO
    ZONE_FACE
    ZONE_FACE_TOPO
    LC_FACE
    LC_FACE_TOPO*/
    do_calc_face('AD_FACE', 'AD_FACE_TOPO', 'AD_LINK');
    do_calc_face('CMG_BUILDFACE', 'CMG_BUILDFACE_TOPO', 'CMG_BUILDLINK');
    do_calc_face('CM_BUILDFACE', 'CM_BUILDFACE_TOPO', 'CM_BUILDLINK');
    do_calc_face('LU_FACE', 'LU_FACE_TOPO', 'LU_LINK');
    do_calc_face('ZONE_FACE', 'ZONE_FACE_TOPO', 'ZONE_LINK');
    do_calc_face('LC_FACE', 'LC_FACE_TOPO', 'LC_LINK');
  
  end;

  --�������ڴ�ű仯��������ʱ��
  procedure create_temp_table is
    pragma autonomous_transaction;
    tmp_table_count       pls_integer;
    v_create_sql          varchar2(1000) := 'create global temporary table tmp_imp_change_face
        (
          TABLE_NAME varchar2(50) not null,
          OBJECT_ID  number(10) not null,
          constraint PK_TMP_IMP_C_FACE primary key (OBJECT_ID, TABLE_NAME)
        )
        on commit delete rows';
    v_create_seq_sql      varchar2(1000) := 'create global temporary table tmp_imp_face_seq
      (
        face_pid NUMBER(10) not null,
        link_pid NUMBER(10) not null,
        seq      NUMBER(3),
        constraint PK_TMP_IMP_FACE_seq primary key (LINK_PID, FACE_PID, SEQ)
      )
      on commit delete rows';
    v_create_face_sql     varchar2(1000) := 'create global temporary table tmp_imp_face
    (
      face_pid  number(10) not null,
      geometry  mdsys.sdo_geometry,
      area      number(30,6),
      perimeter number(15,3)��
      constraint PK_TMP_IMP_FACE primary key (FACE_PID)
    ) 
    on commit delete rows';
    v_create_face_pid_sql varchar2(1000) := 'create global temporary table tmp_imp_face_pid
      (
        face_pid NUMBER(10) not null
      )
      on commit delete rows';
  
    -- 20150513 modified by liya
    v_create_face_msg_sql varchar2(1000) := 'create global temporary table tmp_batch_face_msg
      (
      table_name varchar(40),
      face_pid number(10),
      mesh_id number(6),
      msg varchar2(4000)
      )
      on commit delete rows';
  begin
    select count(1)
      into tmp_table_count
      from user_tables
     where table_name = 'TMP_IMP_CHANGE_FACE';
    if tmp_table_count <> 1 then
      execute immediate v_create_sql;
    end if;
    select count(1)
      into tmp_table_count
      from user_tables
     where table_name = 'TMP_IMP_FACE_SEQ';
    if tmp_table_count <> 1 then
      execute immediate v_create_seq_sql;
    end if;
    select count(1)
      into tmp_table_count
      from user_tables
     where table_name = 'TMP_IMP_FACE';
    if tmp_table_count <> 1 then
      execute immediate v_create_face_sql;
    end if;
    select count(1)
      into tmp_table_count
      from user_tables
     where table_name = 'TMP_IMP_FACE_PID';
    if tmp_table_count <> 1 then
      execute immediate v_create_face_pid_sql;
    end if;
    --20150513 modified by liya 
    --��������log��¼�ı�ṹ�仯�������Ҫ���´���
    select count(1)
      into tmp_table_count
      from user_tables
     where table_name = 'TMP_BATCH_FACE_MSG';
    if tmp_table_count = 1 then
      execute immediate 'drop table TMP_BATCH_FACE_MSG';
    end if;
    execute immediate v_create_face_msg_sql;
  
  exception
    when others then
      select count(1)
        into tmp_table_count
        from user_tables
       where table_name in ('TMP_IMP_CHANGE_FACE',
                            'TMP_IMP_FACE_SEQ',
                            'TMP_IMP_FACE',
                            'TMP_IMP_FACE_PID',
                            'TMP_BATCH_FACE_MSG');
      if tmp_table_count <> 5 then
        raise_application_error(-20999,
                                '����������ʱ�����ڣ����޷��Զ��������ֶ�����ʹ�����½ű���' ||
                                v_create_sql || ';' || v_create_seq_sql || ';' ||
                                v_create_face_sql || ';' ||
                                v_create_face_pid_sql || ';' ||
                                v_create_face_msg_sql,
                                true);
      end if;
  end;

  procedure prepare_change_face(p_version_id pls_integer) is
  begin
    execute immediate 'insert into tmp_imp_change_face
    (table_name,object_id)
    select distinct d.table_name,d.object_id
    from data_log@vm_db_link  d
    where d.version_id = :p_version_Id
    and d.table_name in
    (
    ''AD_FACE'',
    ''AD_FACE_TOPO'',
    ''AD_LINK'',
    ''CMG_BUILDFACE'',
    ''CMG_BUILDFACE_TOPO'',
    ''CMG_BUILDLINK'',
    ''CM_BUILDFACE'',
    ''CM_BUILDFACE_TOPO'',
    ''CM_BUILDLINK'',
    ''LU_FACE'',
    ''LU_FACE_TOPO'',
    ''LU_LINK'',
    ''ZONE_FACE'',
    ''ZONE_FACE_TOPO'',
    ''ZONE_LINK'',
    ''LC_FACE'',
    ''LC_FACE_TOPO'',
    ''LC_LINK''
    )'
      using p_version_id;
  end;

  procedure re_calc_face(p_version_id pls_integer, p_scale pls_integer) is
  begin
    --create_temp_table;
    --���������иð汾�±仯���������뵽������ʱ����
    --prepare_change_face(p_version_id);
    case p_scale
      when 25000 then
        re_calc_2_5w_face;
      when 200000 then
        re_calc_20w_face;
      when 1000000 then
        re_calc_100w_face;
      when 0 then
        re_calc_top_face;
    end case;
  exception
    when others then
      dbms_output.put_line(sqlcode || '--' || sqlerrm);
      raise;
  end;

  /*
    ���㷨����������
    http://database.itags.org/oracle/34531/
    �����жϼ����Ƿ���ʱ�ӷ�������
    ��ʱ���򷵻�true
    ���򷵻�false
  
  */
  function is_polygon_counterclockwise(p_geo in mdsys.sdo_geometry)
    return varchar2 is
    ln_sum   number := 0;
    pi_tocke mdsys.sdo_ordinate_array;
  begin
    pi_tocke := p_geo.sdo_ordinates;
    if pi_tocke.count < 3 then
      return 'ERROR';
    end if;
    for i in 1 .. pi_tocke.last - 3 loop
      if mod(i, 2) = 1 then
        ln_sum := ln_sum + (pi_tocke(i) * pi_tocke(i + 3) -
                  pi_tocke(i + 2) * pi_tocke(i + 1));
      end if;
    end loop;
    if ln_sum > 0 then
      return 'TRUE';
    else
      return 'FALSE';
    end if;
  end;

  procedure judge_face_repate_link(v_topo_table_name varchar2,
                                   v_face_table_name varchar2,
                                   err_count         OUt NUMBER) as
    my_cursor repeat_link_cursor_type;
  
    dyn_select varchar2(4000);
    face_pid   number(10);
    link_pid   number(10);
  begin
    err_count  := 0;
    dyn_select := 'select t.face_pid, t.link_pid
  from ' || v_topo_table_name || ' t, tmp_imp_face_seq v
 where t.face_pid = v.face_pid
   and t.link_pid = v.link_pid
 group by t.face_pid, t.link_pid
having count(1) > 1';
    OPEN my_cursor FOR dyn_select;
    LOOP
      FETCH my_cursor
        INTO face_pid, link_pid;
      EXIT WHEN my_cursor%NOTFOUND;
      --˵�����ظ����պϣ�������
      log(v_face_table_name,
          face_pid,
          '����ʱ�����治�պϣ����Ϊ��' || v_face_table_name || ':' || face_pid ||
          '���ߺ�Ϊ' || link_pid);
      err_count := err_count + 1;
    END LOOP;
    CLOSE my_cursor;
  end;
end pk_batch_face;
/
