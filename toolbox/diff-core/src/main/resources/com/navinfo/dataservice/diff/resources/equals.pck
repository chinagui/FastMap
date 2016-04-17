CREATE OR REPLACE PACKAGE EQUALS IS
  C_GDB_COORDINATE_TOLERANCE  NUMBER := 0.000005;

  function geo_equals(p_geo1 mdsys.sdo_geometry,p_geo2 mdsys.sdo_geometry)
  return boolean;

  function geo_equals_ext(p_geo1 mdsys.sdo_geometry,p_geo2 mdsys.sdo_geometry)
  return varchar2;

    function equal(a number,b number) return number parallel_enable;
    function equal(a varchar2,b varchar2) return number parallel_enable;
    function equal(a date,b date) return number parallel_enable;
    function equal(a mdsys.sdo_geometry,b mdsys.sdo_geometry) return number parallel_enable;
    function equal(a blob,b blob) return number parallel_enable;
    function equal(a clob,b clob) return number parallel_enable;
    FUNCTION EQUAL(A TIMESTAMP,B TIMESTAMP) RETURN NUMBER PARALLEL_ENABLE;

END EQUALS;
/
CREATE OR REPLACE PACKAGE BODY EQUALS IS



  function geo_equals(p_geo1 mdsys.sdo_geometry,p_geo2 mdsys.sdo_geometry)
  return boolean
  is
  v_result boolean := false;
  v_el_array_1 sdo_elem_info_array;
  v_el_array_2 sdo_elem_info_array;
  v_or_array_1 sdo_ordinate_array;
  v_or_array_2 sdo_ordinate_array;
  begin
       if p_geo1 is null or p_geo2 is null
       then
           return p_geo1 is null and p_geo2 is null;
       end if;

       if p_geo1.SDO_GTYPE <> p_geo2.SDO_GTYPE or p_geo1.SDO_SRID <> p_geo2.SDO_SRID
       then
           return false;
       end if;

       case when p_geo1.SDO_GTYPE = 2001
       then
       --2d point
           return p_geo1.SDO_POINT.X = p_geo2.SDO_POINT.X and p_geo1.SDO_POINT.Y = p_geo2.SDO_POINT.Y;
       when p_geo1.SDO_GTYPE = 3001
       then
         --3d point
           return p_geo1.SDO_POINT.X = p_geo2.SDO_POINT.X and p_geo1.SDO_POINT.Y = p_geo2.SDO_POINT.Y
           and  p_geo1.SDO_POINT.Z = p_geo2.SDO_POINT.Z;
       when p_geo1.SDO_GTYPE = 2002 or p_geo1.SDO_GTYPE = 2003 or p_geo1.SDO_GTYPE = 3002 or  p_geo1.SDO_GTYPE = 3003
       then
       --Ïß¡¢Ãæ
           v_el_array_1 := p_geo1.SDO_ELEM_INFO;
           v_el_array_2 := p_geo2.SDO_ELEM_INFO;
           if v_el_array_1.count <> v_el_array_2.count
           then
               return false;
           end if;

           for i in 1..v_el_array_1.count
           loop
               if v_el_array_1(i) <> v_el_array_2(i)
               then
                   return false;
               end if;
           end loop;

           v_or_array_1 := p_geo1.SDO_ORDINATES;
           v_or_array_2 := p_geo2.SDO_ORDINATES;

           if v_or_array_1.count <> v_or_array_2.count
           then
               return false;
           end if;

           for i in 1..v_or_array_1.count
           loop
               if v_or_array_1(i) <> v_or_array_2(i)
               then
                   return false;
               end if;
           end loop;
       end case;
       return true;
  end;

  function geo_equals_ext(p_geo1 mdsys.sdo_geometry,p_geo2 mdsys.sdo_geometry)
  return varchar2
  is
    v_res varchar2(10);
  begin
       if geo_equals(p_geo1,p_geo2) = true
       then
           v_res := 'true';
       else
           v_res := 'false';
       end if;
       return v_res;
  end;

  function xlong(ptgeo in mdsys.sdo_geometry) return number parallel_enable is
  begin
    if (ptgeo.sdo_point is not null) then
      return ptgeo.sdo_point.x;
    elsif (ptgeo.sdo_ordinates is not null) then
      return ptgeo.sdo_ordinates(1);
    else
      return null;
    end if;
  end;

  function ylat(ptgeo in mdsys.sdo_geometry) return number parallel_enable is

  begin
    if (ptgeo.sdo_point is not null) then
      return ptgeo.sdo_point.y;
    elsif (ptgeo.sdo_ordinates is not null) then
      return ptgeo.sdo_ordinates(2);
    else
      return null;
    end if;
  end;

  function numequal(n1 in number, n2 in number) return boolean parallel_enable is

  begin
    return abs(n1 - n2) < c_gdb_coordinate_tolerance;
  end;


   function issamegeom(geo1 in sdo_geometry, geo2 in sdo_geometry)
    return number  parallel_enable is
    idx      number;
    restrue  binary_integer := 1;
    resfalse binary_integer := 0;

  begin
    if geo1 is null and geo2 is null then
      return restrue;
    end if;

    if (geo1 is null and geo2 is not null) or (geo1 is not null and geo2 is  null) then
      return resfalse;
    end if;

    if (geo1.get_gtype <> geo2.get_gtype) then
      return resfalse;
    end if;

    if geo1.get_gtype = 1 then
      if (geo1.sdo_srid = geo2.sdo_srid) and
         numequal(xlong(geo1), xlong(geo2)) and
         numequal(ylat(geo1), ylat(geo2)) then

        return restrue;
      else
        return resfalse;
      end if;
    else
      if (geo1.sdo_ordinates.count <> geo2.sdo_ordinates.count) then
        return resfalse;
      end if;

      for idx in geo1.sdo_ordinates.first .. geo1.sdo_ordinates.last loop
        if not numequal(geo1.sdo_ordinates(idx), geo2.sdo_ordinates(idx)) then
          return resfalse;
        end if;
      end loop;
      return restrue;
    end if;
  end;

   function equal(a number,b number) return number parallel_enable is
    begin
      if (a is null and b is not null) or (a is not null and b is null) then
        return 0;
      end if;

       if a is null and b is null then
        return 1;
       end if;
       if a=b then
          return 1;
       else
          return 0;
       end if;
    end;


    function equal(a varchar2,b varchar2) return number parallel_enable is
    begin

      if (a is null and b is not null) or (a is not null and b is null) then
        return 0;
      end if;

      if a is null and b is null then
        return 1;
      end if;
      if a=b then
         return 1;
      else
         return 0;
      end if;
    end;


    function equal(a date,b date) return number parallel_enable is
    begin
      if (a is null and b is not null) or (a is not null and b is null) then
         return 0;
      end if;

      if a is null and b is null then
         return 1;
      end if;
      if a=b then
          return 1;
      else
          return 0;
      end if;
    end;

    function equal(a mdsys.sdo_geometry,b mdsys.sdo_geometry) return number parallel_enable is
    begin
      if (a is null and b is not null) or (a is not null and b is null) then
         return 0;
      end if;

      if a is null and b is null then
         return 1;
      end if;
      if issamegeom(a,b)=1 then
         return 1;
      else
         return 0;
      end if;
    end;


    function equal(a blob,b blob) return number  parallel_enable is
    begin
      if (a is null and b is not null) or (a is not null and b is null) then
         return 0;
      end if;

      if a is null and b is null then
         return 1;
      end if;

      if dbms_lob.compare(a,b)=0 then
         return 1;
      else
         return 0;
      end if;
    end;



    function equal(a clob,b clob) return number parallel_enable is
    begin
      if (a is null and b is not null) or (a is not null and b is null) then
         return 0;
      end if;

      if a is null and b is null then
         return 1;
      end if;

      if dbms_lob.compare(a,b)=0 then
         return 1;
      else
         return 0;
      end if;
    end;
 FUNCTION EQUAL(A TIMESTAMP,B TIMESTAMP) RETURN NUMBER PARALLEL_ENABLE IS
    BEGIN
      IF (A IS NULL AND B IS NOT NULL) OR (A IS NOT NULL AND B IS NULL) THEN
         RETURN 0;
      END IF;

      IF A IS NULL AND B IS NULL THEN
         RETURN 1;
      END IF;

      IF A = B THEN
         RETURN 1;
      ELSE
         RETURN 0;
      END IF;

END;

END EQUALS;
/
