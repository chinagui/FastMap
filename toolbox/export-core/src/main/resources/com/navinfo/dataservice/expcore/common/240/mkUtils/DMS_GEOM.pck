CREATE OR REPLACE PACKAGE  DMS_GEOM IS
  type t_mesh_map is table of boolean index by varchar2(50);
  type T_VARCHAR_ARRAY is varray(400000) of varchar2(4000);
  function get_neighbor_mesh(p_mesh varchar2,p_count    pls_integer default 2)return t_varchar_array;
  function get_2d5w_mesh_id(p_x number, p_y number) return varchar2;
  function get_8Neighbor_Mesh(p_mesh_Id varchar2) return t_varchar_array;
  function isadjoinmeshrelationforcount(meshid1    varchar2,
                                        meshid2    varchar2,
                                        p_count   pls_integer default 2,
                                        mapscale   NUMBER )RETURN NUMBER;

END DMS_GEOM;
/
CREATE OR REPLACE PACKAGE BODY  DMS_GEOM IS

function  get_neighbor_mesh  (p_mesh  varchar2,
                             p_count    pls_integer default 2)
    return t_varchar_array is
    v_mesh_map        t_mesh_map;
    v_mesh            varchar2(50);
    v_entend          boolean;
    v_not_extend_mesh t_varchar_array := t_varchar_array();
    v_extend_mesh     t_varchar_array;
    v_extend_mesh_map t_mesh_map;
    v_meshs           t_varchar_array := t_varchar_array();
    v_int_mesh_array  t_varchar_array := t_varchar_array();
  begin
      v_int_mesh_array.extend(1);
      v_int_mesh_array(1) := p_mesh;
      if p_count = 0
      then
       return v_int_mesh_array;
      end if;
      v_mesh_map(v_int_mesh_array(1)) := false;
    --未扩展前的图幅列表
    v_mesh := v_mesh_map.first;
    while v_mesh is not null loop
      v_entend := v_mesh_map(v_mesh);
      v_not_extend_mesh.extend(1);
      v_not_extend_mesh(v_not_extend_mesh.count) := v_mesh;
      v_mesh := v_mesh_map.next(v_mesh);
    end loop;
  
    --扩展图幅9宫图
    for ext in 1 .. p_count loop
      v_mesh := v_mesh_map.first;
      while v_mesh is not null loop
        v_entend := v_mesh_map(v_mesh);
        v_extend_mesh_map(v_mesh) := false;
        if not v_entend then
          --扩展
          v_extend_mesh := dms_geom.get_8Neighbor_Mesh(v_mesh);
          for i in 1 .. v_extend_mesh.count loop
            v_extend_mesh_map(v_extend_mesh(i)) := false;
          end loop;
        end if;
        v_mesh := v_mesh_map.next(v_mesh);
      end loop;
      v_mesh_map := v_extend_mesh_map;
    end loop;
  
    v_mesh := v_extend_mesh_map.first;
    while v_mesh is not null loop
      v_meshs.extend();
      v_meshs(v_meshs.count) := v_mesh;
      v_mesh := v_extend_mesh_map.next(v_mesh);
    end loop;
    return v_meshs;
  end;



function isadjoinmeshrelationforcount  (meshid1    varchar2,
                                        meshid2    varchar2,
                                        p_count   pls_integer default 2,
                                        mapscale   NUMBER )RETURN NUMBER
is 
    meshs1     t_varchar_array ;
    meshs2     t_varchar_array ;
    v_flag     number;
  

begin

 if p_count = 0 
    then
    if meshid1 = meshid2
    then
     return 1;
     else
     return 0;
    end if;
 
 end if;
    meshs1  :=  get_neighbor_mesh(meshid1,p_count);
    meshs2  :=  get_neighbor_mesh(meshid1,p_count-1);
   IF meshs1 IS NULL THEN
      RETURN NULL;
    END IF;
  IF meshs2 IS NULL THEN
      RETURN NULL;
    END IF;

  for i in  meshs2.FIRST .. meshs2.LAST
  loop
     if  meshs2(I) = meshid2 
        then
        return 0;
      end if;
  
  end loop;
  
  for i in meshs1.FIRST .. meshs1.LAST
  loop
     if  meshs1(I) = meshid2 
        then
        return 1;
      end if;
    
  end loop;
    return 0;
  

end;
                                        


 function get_8Neighbor_Mesh(p_mesh_Id varchar2) return t_varchar_array is
    m1        pls_integer;
    m2        pls_integer;
    m3        pls_integer;
    m4        pls_integer;
    m5        pls_integer;
    m6        pls_integer;
    x         pls_integer;
    y         pls_integer;
    meshs     t_varchar_array := t_varchar_array();
    v_mesh_id varchar2(50);
  begin
    v_mesh_id := p_mesh_Id;
    if length(p_mesh_Id) = 5 then
      v_mesh_id := '0' || p_mesh_Id;
    end if;
    if length(v_mesh_id) <> 6 then
      meshs.extend(1);
      meshs(1) := v_mesh_id;
      return meshs;
    end if;
    meshs.extend(8);
    m1 := substr(v_mesh_id, 1, 1);
    m2 := substr(v_mesh_id, 2, 1);
    m3 := substr(v_mesh_id, 3, 1);
    m4 := substr(v_mesh_id, 4, 1);
    m5 := substr(v_mesh_id, 5, 1);
    m6 := substr(v_mesh_id, 6, 1);
    x := (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
    y := (m1 * 10 + m2) * 2400 + m5 * 300;
    x := x + 450 / 2;
    y := y + 300 / 2;
    meshs(1) := get_2d5w_mesh_id(x - 450, y + 300);
    meshs(2) := get_2d5w_mesh_id(x, y + 300);
    meshs(3) := get_2d5w_mesh_id(x + 450, y + 300);
    meshs(4) := get_2d5w_mesh_id(x - 450, y);
    meshs(5) := get_2d5w_mesh_id(x + 450, y);
    meshs(6) := get_2d5w_mesh_id(x - 450, y - 300);
    meshs(7) := get_2d5w_mesh_id(x, y - 300);
    meshs(8) := get_2d5w_mesh_id(x + 450, y - 300);
    return meshs;
  exception
    when others then
      raise_application_error(-20888,
                              '输入的图幅号无法正常扩圈，请检查输入的图幅号是否在中国版图范围内:' ||
                              sqlcode || '，' || sqlerrm || '，' ||
                              dbms_utility.format_error_backtrace(),
                              true);
  end;
  
   function get_2d5w_mesh_id(p_x number, p_y number) return varchar2 is
    x       number;
    y       number;
    W       number;
    M1M2    pls_integer;
    J1      pls_integer;
    J2      number;
    M3M4    pls_integer;
    M5      pls_integer;
    M6      pls_integer;
    mesh_id varchar2(6);
  begin
    x := p_x;
    y := p_y;
    x := x / 3600;
    y := y / 3600;
  
    W    := y;
    M1M2 := floor(W * 1.5);
  
    J1 := floor(x);
    J2 := x - J1;
  
    M3M4 := J1 - 60;
    M5   := floor((W - M1M2 / 1.5) * 12);
    M6   := floor(J2 * 8);
    if M1M2 < 0 or M3M4 < 0 or M5 < 0 or M6 < 0 then
      raise_application_error(-20888,
                              '图幅号已经超出了中国的范围:' || sqlcode || '，' || sqlerrm || '，' ||
                              dbms_utility.format_error_backtrace(),
                              true);
    end if;
    mesh_id := M1M2 || M3M4 || M5 || M6;
    if length(mesh_id) = 5 then
      mesh_id := '0' || mesh_id;
    end if;
    return mesh_id;
  end;
END DMS_GEOM;
/