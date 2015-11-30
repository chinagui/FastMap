create or replace function to_wktString
(p_geo mdsys.sdo_geometry)
  return clob is
  v_result     clob ;
  v_el_array_1 SDO_ORDINATE_ARRAY;
  v_length pls_integer := 0;
begin
  dbms_lob.createtemporary(v_result,true);
  if p_geo is null 
  then
      return null;
  end if;

  if p_geo.SDO_GTYPE = 3002 
  then  
      v_el_array_1 := p_geo.SDO_ORDINATES;   
      dbms_lob.write (v_result,13,1,'LINESTRING Z(');
      v_length := 14;
      for i in 1 .. v_el_array_1.count 
      loop
          dbms_lob.write(v_result,length(v_el_array_1(i)),v_length,v_el_array_1(i)); 
          v_length := v_length + length(v_el_array_1(i)) + 1;              
          if mod(i, 3) = 0 and i <> v_el_array_1.count 
          then
              dbms_lob.write (v_result,1,v_length,','); 
              v_length := v_length + 1;
          end if;    
      end loop;  
      dbms_lob.write (v_result,1,v_length,')');
      return v_result;  
  elsif p_geo.SDO_GTYPE = 3001
  then
        v_result := 'POINT Z('|| p_geo.SDO_POINT.x ||' '||p_geo.SDO_POINT.y ||' '||p_geo.SDO_POINT.z||')';
        return v_result;
  end if;
  return SDO_UTIL.TO_WKTGEOMETRY(p_geo);
end;
/
