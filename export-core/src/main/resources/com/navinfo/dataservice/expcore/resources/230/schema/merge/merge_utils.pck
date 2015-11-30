CREATE OR REPLACE PACKAGE merge_utils IS
  MERGE_TYPE_ATT CONSTANT VARCHAR2(3) := 'att';
  MERGE_TYPE_GEO CONSTANT VARCHAR2(3) := 'geo';
  MERGE_TYPE_GEOATT CONSTANT VARCHAR2(6) := 'geoatt';
  FUNCTION geo_equals(p_geo1 mdsys.sdo_geometry,
                      p_geo2 mdsys.sdo_geometry) RETURN BOOLEAN;

  FUNCTION geo_equals_ext(p_geo1 mdsys.sdo_geometry,
                          p_geo2 mdsys.sdo_geometry) RETURN VARCHAR2;
  FUNCTION get_oprstatus_clause(v_merge_type VARCHAR2,
                                v_alias      VARCHAR2) RETURN VARCHAR2;
  FUNCTION get_proxypoi_clause(v_merge_type VARCHAR2,
                                v_alias      VARCHAR2) RETURN VARCHAR2;                                

END merge_utils;
/
CREATE OR REPLACE PACKAGE BODY merge_utils IS
  FUNCTION geo_equals(p_geo1 mdsys.sdo_geometry,
                      p_geo2 mdsys.sdo_geometry) RETURN BOOLEAN IS
    v_result     BOOLEAN := FALSE;
    v_el_array_1 sdo_elem_info_array;
    v_el_array_2 sdo_elem_info_array;
    v_or_array_1 sdo_ordinate_array;
    v_or_array_2 sdo_ordinate_array;
  BEGIN
    IF p_geo1 IS NULL OR p_geo2 IS NULL THEN
      RETURN p_geo1 IS NULL AND p_geo2 IS NULL;
    END IF;
  
    IF p_geo1.SDO_GTYPE <> p_geo2.SDO_GTYPE OR
       p_geo1.SDO_SRID <> p_geo2.SDO_SRID THEN
      RETURN FALSE;
    END IF;
  
    CASE
      WHEN p_geo1.SDO_GTYPE = 2001 THEN
        --µã
        RETURN p_geo1.SDO_POINT.X = p_geo2.SDO_POINT.X AND p_geo1.SDO_POINT.Y = p_geo2.SDO_POINT.Y;
      WHEN p_geo1.SDO_GTYPE = 2002 OR p_geo1.SDO_GTYPE = 2003 THEN
        --Ïß¡¢Ãæ
        v_el_array_1 := p_geo1.SDO_ELEM_INFO;
        v_el_array_2 := p_geo2.SDO_ELEM_INFO;
        IF v_el_array_1.count <> v_el_array_2.count THEN
          RETURN FALSE;
        END IF;
      
        FOR i IN 1 .. v_el_array_1.count LOOP
          IF v_el_array_1(i) <> v_el_array_2(i) THEN
            RETURN FALSE;
          END IF;
        END LOOP;
      
        v_or_array_1 := p_geo1.SDO_ORDINATES;
        v_or_array_2 := p_geo2.SDO_ORDINATES;
      
        IF v_or_array_1.count <> v_or_array_2.count THEN
          RETURN FALSE;
        END IF;
      
        FOR i IN 1 .. v_or_array_1.count LOOP
          IF v_or_array_1(i) <> v_or_array_2(i) THEN
            RETURN FALSE;
          END IF;
        END LOOP;
    END CASE;
    RETURN TRUE;
  END;

  FUNCTION geo_equals_ext(p_geo1 mdsys.sdo_geometry,
                          p_geo2 mdsys.sdo_geometry) RETURN VARCHAR2 IS
    v_res VARCHAR2(10);
  BEGIN
    IF geo_equals(p_geo1, p_geo2) = TRUE THEN
      v_res := 'true';
    ELSE
      v_res := 'false';
    END IF;
    RETURN v_res;
  END;
  FUNCTION get_oprstatus_clause(v_merge_type VARCHAR2,
                                v_alias      VARCHAR2) RETURN VARCHAR2 IS
    v_oprstatus_claus VARCHAR2(100);
  BEGIN
    IF (MERGE_TYPE_ATT = v_merge_type) THEN
      IF(v_alias IS NULL) THEN RETURN 'att_oprstatus=0 '; END IF;
      RETURN v_alias || '.att_oprstatus=0 ';      
    ELSE
      IF (MERGE_TYPE_GEO = v_merge_type) THEN
        IF(v_alias IS NULL) THEN RETURN 'geo_oprstatus=0 '; END IF;
        RETURN v_alias || '.geo_oprstatus=0 '; 
      ELSE
         IF(v_alias IS NULL) THEN RETURN ' geo_oprstatus=0 or att_oprstatus=0 '; END IF;
        RETURN  ' ('||v_alias ||'.geo_oprstatus=0 or ' || v_alias ||
                             '.att_oprstatus=0) ';
      END IF;
    END IF;
    RAISE_APPLICATION_ERROR(-20998,'unkown merge type:'||v_merge_type);
  END;
  FUNCTION get_proxypoi_clause(v_merge_type VARCHAR2,
                                v_alias      VARCHAR2) RETURN VARCHAR2 IS
    v_oprstatus_claus VARCHAR2(100);
  BEGIN
    IF (MERGE_TYPE_ATT = v_merge_type) THEN
      IF(v_alias IS NULL) THEN RETURN 'att_oprstatus in(0,1) '; END IF;
      RETURN v_alias || '.att_oprstatus in(0,1) ';      
    ELSE
      IF (MERGE_TYPE_GEO = v_merge_type) THEN
        IF(v_alias IS NULL) THEN RETURN 'geo_oprstatus in(0,1) '; END IF;
        RETURN v_alias || '.geo_oprstatus in(0,1)'; 
      ELSE
         IF(v_alias IS NULL) THEN RETURN ' (geo_oprstatus in(0,1) or att_oprstatus in(0,1) )'; END IF;
        RETURN  ' ('||v_alias ||'.geo_oprstatus in(0,1) or ' || v_alias ||
                             '.att_oprstatus in(0,1) ) ';
      END IF;
    END IF;
    RAISE_APPLICATION_ERROR(-20998,'unkown merge type:'||v_merge_type);
  END;
END merge_utils;
/
