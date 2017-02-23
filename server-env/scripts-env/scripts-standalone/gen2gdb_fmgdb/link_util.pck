create or replace package link_util authid current_user is

  type record_link is record(
    link_pid number,
    node_pid number,
    geometry sdo_geometry);
  type list_record_link is table of record_link;
  function get_display_point_list(c1 sys_refcursor) return list_record_link
    pipelined
    parallel_enable(partition c1 by any);
  function get_display_point(s_x        number,
                             s_y        number,
                             e_x        number,
                             e_y        number,
                             p_distance number,
                             p_len      out number,
                             p_geom     out sdo_geometry) return number;
end;
/
create or replace package body link_util is
  function get_display_point_list(c1 sys_refcursor) return list_record_link
    pipelined
    parallel_enable(partition c1 by any) is
    v_len_link  number;
    v_type1     type1;
    v_cnt_link  number;
    v_p_number  number(5);
    v_out_len   number;
    v_out_point sdo_geometry;
    v_s_x       number;
    v_s_y       number;
    v_e_x       number;
    v_e_y       number;
    v_direct    number(1) := -1;
  
    geom_link   sdo_geometry;
    geom_point  sdo_geometry;
    len         number;
    scale       number;
    v_len       number;
    in_link_pid number;
    node_pid    number;
  
    v_record_link record_link;
  begin
    loop
      fetch c1
        into geom_link, geom_point, len, scale, in_link_pid, node_pid;
      exit when c1%notfound;
      v_len := len;
    
      select * bulk collect
        into v_type1
        from table(geom_link.SDO_ORDINATES);
    
      v_cnt_link := v_type1.count;
      if (v_type1(1) = geom_point.SDO_POINT.X and
         v_type1(2) = geom_point.SDO_POINT.Y) then
        v_direct := 1;
      end if;
    
      v_len_link := sdo_geom.sdo_length(geom_link, 0.5);
      if (v_len_link <> v_len) then
        if (v_len_link < v_len) then
          v_len := v_len_link * scale;
        end if;
        if v_direct = 1 then
          v_p_number := 2;
          v_s_x      := v_type1(1);
          v_s_y      := v_type1(2);
          v_p_number := v_p_number + 1;
          v_e_x      := v_type1(v_p_number);
          v_p_number := v_p_number + 1;
          v_e_y      := v_type1(v_p_number);
        
          while (fun2(v_s_x,
                      v_s_y,
                      v_e_x,
                      v_e_y,
                      v_len,
                      v_out_len,
                      v_out_point) = -1) loop
            v_s_x      := v_e_x;
            v_s_y      := v_e_y;
            v_p_number := v_p_number + 1;
            v_e_x      := v_type1(v_p_number);
            v_p_number := v_p_number + 1;
            v_e_y      := v_type1(v_p_number);
            v_len      := v_len - v_out_len;
          end loop;
          --return v_out_point;
          v_record_link.link_pid := in_link_pid;
          v_record_link.node_pid := node_pid;
          v_record_link.geometry := v_out_point;
          pipe row(v_record_link);
        else
          v_p_number := v_cnt_link - 1;
          v_s_x      := v_type1(v_cnt_link - 1);
          v_s_y      := v_type1(v_cnt_link);
          v_p_number := v_p_number - 1;
          v_e_y      := v_type1(v_p_number);
          v_p_number := v_p_number - 1;
          v_e_x      := v_type1(v_p_number);
        
          while (fun2(v_s_x,
                      v_s_y,
                      v_e_x,
                      v_e_y,
                      v_len,
                      v_out_len,
                      v_out_point) = -1) loop
            v_s_x      := v_e_x;
            v_s_y      := v_e_y;
            v_p_number := v_p_number - 1;
            v_e_y      := v_type1(v_p_number);
            v_p_number := v_p_number - 1;
            v_e_x      := v_type1(v_p_number);
            v_len      := v_len - v_out_len;
          end loop;
          --return v_out_point;
          v_record_link.link_pid := in_link_pid;
          v_record_link.node_pid := node_pid;
          v_record_link.geometry := v_out_point;
          pipe row(v_record_link);
        end if;
      
      else
        if (v_direct = 1) then
          v_out_point := sdo_geometry(2001,
                                      8307,
                                      sdo_point_type(v_type1(v_cnt_link - 1),
                                                     v_type1(v_cnt_link),
                                                     null),
                                      null,
                                      null);
          --return v_out_point;
          v_record_link.link_pid := in_link_pid;
          v_record_link.node_pid := node_pid;
          v_record_link.geometry := v_out_point;
          pipe row(v_record_link);
        else
          v_out_point := sdo_geometry(2001,
                                      8307,
                                      sdo_point_type(v_type1(1),
                                                     v_type1(2),
                                                     null),
                                      null,
                                      null);
          --return v_out_point;
          v_record_link.link_pid := in_link_pid;
          v_record_link.node_pid := node_pid;
          v_record_link.geometry := v_out_point;
          pipe row(v_record_link);
        end if;
      
      end if;
    
    end loop;
  
  end;
  function get_display_point(s_x        number,
                             s_y        number,
                             e_x        number,
                             e_y        number,
                             p_distance number,
                             p_len      out number,
                             p_geom     out sdo_geometry) return number is
  
    v_s_geom sdo_geometry := sdo_geometry(2001,
                                          8307,
                                          sdo_point_type(s_x, s_y, null),
                                          null,
                                          null);
  
    v_e_geom sdo_geometry := sdo_geometry(2001,
                                          8307,
                                          sdo_point_type(e_x, e_y, null),
                                          null,
                                          
                                          null);
  
    v_distance number := sdo_geom.sdo_distance(v_s_geom, v_e_geom, 0.5);
  begin
    if (v_distance > p_distance) then
      declare
        v_buffer sdo_geometry;
        v_ring   sdo_geometry;
        v_link   sdo_geometry;
        v_array  SDO_ORDINATE_ARRAY := SDO_ORDINATE_ARRAY();
      
      begin
        v_array.extend;
        v_array(1) := s_x;
        v_array.extend;
        v_array(2) := s_y;
        v_array.extend;
        v_array(3) := e_x;
        v_array.extend;
        v_array(4) := e_y;
        v_link := sdo_geometry(2002,
                               8307,
                               null,
                               SDO_ELEM_INFO_ARRAY(1, 2, 1),
                               sdo_ordinate_array(s_x, s_y, e_x, e_y));
      
        v_buffer := sdo_geom.sdo_buffer(v_s_geom, p_distance + 0.05, 0.5);
      
        v_ring := sdo_util.polygontoline(v_buffer);
        /* 
         dbms_output.put_line(sdo_Geom.Relate(v_buffer, 'DETERMINED', v_link, 0.5 ));
        */
        /*for a in 1..v_buffer.SDO_ELEM_INFO.count loop
          dbms_output.put_line(v_buffer.SDO_ELEM_INFO(a));
        end loop;
        */
      
        /* for a in 1..v_ring.sdo_ordinates.count loop
          dbms_output.put_line(v_ring.sdo_ordinates(a));
        end loop;*/
      
        p_geom := sdo_geom.sdo_intersection(v_ring, v_link, 0.5);
      
        /*if (p_geom is null) then
          dbms_output.put_line(sdo_geom.sdo_distance(v_link,v_buffer,0.5));
          dbms_output.put_line(sdo_geom.sdo_distance(v_link,v_ring,0.5));
        end if;*/
      
        return 0;
      end;
    elsif (v_distance < p_distance) then
      p_len := v_distance;
      return - 1;
    else
      p_geom := v_e_geom;
      return 0;
    end if;
  
  end;
end;
/

create or replace FUNCTION num_to_bin (p_num NUMBER) RETURN VARCHAR2
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
/

create or replace function format_vehicle(vehicle number) return number
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
/

exit;