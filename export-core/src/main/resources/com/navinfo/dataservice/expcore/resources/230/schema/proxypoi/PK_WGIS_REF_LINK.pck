CREATE OR REPLACE PACKAGE PK_WGIS_REF_LINK IS

  -- Author  : MAYF
  -- Created : 2011/7/8 17:09:22
  -- Purpose : 引导link相关的计算

  PROCEDURE GETREFPOINT(V_DISPLAY_X NUMBER,
                        V_DISPLAY_Y NUMBER,
                        V_LINK_PID  NUMBER,
                        V_REF_X     OUT NUMBER,
                        V_REF_Y     OUT NUMBER);

END PK_WGIS_REF_LINK;
/
CREATE OR REPLACE PACKAGE BODY PK_WGIS_REF_LINK IS

  PROCEDURE GETREFPOINT(V_DISPLAY_X NUMBER,
                        V_DISPLAY_Y NUMBER,
                        V_LINK_PID  NUMBER,
                        V_REF_X     OUT NUMBER,
                        V_REF_Y     OUT NUMBER) AS
    COLA_C_GEOM SDO_GEOMETRY;
    COLA_D_GEOM SDO_GEOMETRY;
    DIST        NUMBER;
    GEOMA       SDO_GEOMETRY;
    GEOMB       SDO_GEOMETRY;
  BEGIN
    COLA_C_GEOM := MDSYS.SDO_GEOMETRY(2001,
                                      8307,
                                      MDSYS.SDO_POINT_TYPE(V_DISPLAY_X,
                                                           V_DISPLAY_Y,
                                                           NULL),
                                      NULL,
                                      NULL);
    SELECT C.GEOMETRY
      INTO COLA_D_GEOM
      FROM RD_LINK C
     WHERE C.LINK_PID = V_LINK_PID;
  
    SDO_GEOM.SDO_CLOSEST_POINTS(COLA_C_GEOM,
                                COLA_D_GEOM,
                                0.5,
                                'unit=M',
                                DIST,
                                GEOMA,
                                GEOMB);
    V_REF_X := GEOMB.SDO_POINT.X;
    V_REF_Y := GEOMB.SDO_POINT.Y;
  
  END;
END PK_WGIS_REF_LINK;
/
