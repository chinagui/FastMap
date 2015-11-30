create or replace package REGION_GDB_VIRTUAL_MERGE is
  -- Author  : LIYA
  -- Created : 2014/4/24 14:23:09
  -- Purpose : ȥ��ĸ�������ں�

  /**
  **ĸ�������ں�
  **����ϲ�����ĸ��汾��Ϣ(ip,port,sid,schemaname,schemaPassword)��
  **�����ʽ��ip1,port1,sid1,schemaName1,schemaPassword1|ip2,port2,sid2,schemaName2,schemaPassword2|ip3,port3,sid3,schemaName3,schemaPassword3|...
  **/
  PROCEDURE virtual_merge(region_info_list REGION_INFO_LIST);
  PROCEDURE enable_all_constraint;

end REGION_GDB_VIRTUAL_MERGE;
/
create or replace package body REGION_GDB_VIRTUAL_MERGE is

  PROCEDURE create_region_gdb_dblink(v_ip          varchar2,
                                     v_port        number,
                                     v_sid         varchar2,
                                     v_schema_user varchar2,
                                     v_schema_pwd  varchar2,
                                     v_dblink_name varchar2) is
    v_db_link_count NUMBER;
  
  begin
    --����dblink
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_db_links where db_link = ''' ||
                      v_dblink_name || ''''
      INTO v_db_link_count;
    IF (v_db_link_count > 0) THEN
      EXECUTE IMMEDIATE 'DROP DATABASE LINK ' || v_dblink_name;
    END IF;
    EXECUTE IMMEDIATE 'create database link ' || v_dblink_name ||
                      ' connect to ' || v_schema_user || ' identified by "' ||
                      v_schema_pwd || '"
  using ''(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = ' || v_ip ||
                      ' )(PORT = ' || v_port ||
                      ' )))(CONNECT_DATA = (SERVICE_NAME = ' || v_sid ||
                      ' )))''';
  end;

  /**�ں�����ĸ���������?*/
  PROCEDURE do_index_merge(v_dblink_name varchar2, v_version_id number) is
  BEGIN
    ----11.1poi����Ϣ
    --1.
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI SELECT * FROM IX_POI@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --2.
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_NAME SELECT * FROM IX_POI_NAME@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_POI_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --3.
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_NAME_FLAG SELECT * FROM IX_POI_NAME_FLAG@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_POI_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --4
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_NAME_TONE SELECT * FROM IX_POI_NAME_TONE@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_POI_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --5
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_ADDRESS SELECT * FROM IX_POI_ADDRESS@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --6
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CONTACT SELECT * FROM IX_POI_CONTACT@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --7.
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_FLAG SELECT * FROM IX_POI_FLAG@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --8
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_ENTRYIMAGE SELECT * FROM IX_POI_ENTRYIMAGE@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --9
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_ICON SELECT * FROM IX_POI_ICON@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --10 
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_PHOTO SELECT * FROM IX_POI_PHOTO@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --11
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_AUDIO SELECT * FROM IX_POI_AUDIO@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --12
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_VIDEO  SELECT * FROM IX_POI_VIDEO@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --13
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_PARENT SELECT * FROM IX_POI_PARENT@' ||
                      v_dblink_name ||
                      ' where parent_poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --14
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CHILDREN SELECT * FROM IX_POI_CHILDREN@' ||
                      v_dblink_name ||
                      ' where child_poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --15
    EXECUTE IMMEDIATE 'INSERT INTO IX_SAMEPOI SELECT * FROM IX_SAMEPOI@' ||
                      v_dblink_name ||
                      ' where group_id in( select group_id from TEMP_IX_POI_SAME_PART t where t.version_id=' ||
                      v_version_id || ' )';
    --16
    EXECUTE IMMEDIATE 'INSERT INTO IX_SAMEPOI_PART SELECT * FROM IX_SAMEPOI_PART@' ||
                      v_dblink_name ||
                      ' where group_id in( select group_id from TEMP_IX_POI_SAME_PART t where t.version_id=' ||
                      v_version_id || ' )';
    ----11.2poi������?
    --1
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_BUILDING SELECT * FROM IX_POI_BUILDING@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --2
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_DETAIL SELECT * FROM IX_POI_DETAIL@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --3
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_BUSINESSTIME SELECT * FROM IX_POI_BUSINESSTIME@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --4.
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_INTRODUCTION SELECT * FROM IX_POI_INTRODUCTION@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --5
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_ADVERTISEMENT SELECT * FROM IX_POI_ADVERTISEMENT@' ||
                      v_dblink_name;
    --6
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_GASSTATION SELECT * FROM IX_POI_GASSTATION@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --7
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CHARGINGSTATION SELECT * FROM IX_POI_CHARGINGSTATION@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --8
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CHARGINGPLOT SELECT * FROM IX_POI_CHARGINGPLOT@' ||
                      v_dblink_name ||
                      ' where poi_pid in ( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --9 240ģ������
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CHARGINGPLOT_PH SELECT * FROM IX_POI_CHARGINGPLOT_PH@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
  
    --10
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_PARKING SELECT * FROM IX_POI_PARKING@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --11
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_ATTRACTION SELECT * FROM IX_POI_ATTRACTION@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --12
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_HOTEL SELECT * FROM IX_POI_HOTEL@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --13
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_RESTAURANT SELECT * FROM IX_POI_RESTAURANT@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    --14
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_TOURROUTE SELECT * FROM IX_POI_TOURROUTE@' ||
                      v_dblink_name;
    --15
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_EVENT SELECT * FROM IX_POI_EVENT@' ||
                      v_dblink_name;
    --16
    EXECUTE IMMEDIATE 'INSERT INTO IX_POI_CARRENTAL SELECT * FROM IX_POI_CARRENTAL@' ||
                      v_dblink_name ||
                      ' where poi_pid in( select pid from TEMP_IX_POI t where t.version_id=' ||
                      v_version_id || ' )';
    ----11.3������
    --1
    EXECUTE IMMEDIATE 'INSERT INTO IX_POINTADDRESS select * from IX_POINTADDRESS@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_ADDRESS t where t.version_id=' ||
                      v_version_id || ' )';
    --2
    EXECUTE IMMEDIATE 'INSERT INTO IX_POINTADDRESS_NAME SELECT * FROM IX_POINTADDRESS_NAME@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_ADDRESS_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --3
    EXECUTE IMMEDIATE 'INSERT INTO IX_POINTADDRESS_NAME_TONE SELECT * FROM IX_POINTADDRESS_NAME_TONE@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_ADDRESS_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --4
    EXECUTE IMMEDIATE 'INSERT INTO IX_POINTADDRESS_FLAG SELECT * FROM IX_POINTADDRESS_FLAG@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_ADDRESS t where t.version_id=' ||
                      v_version_id || ' )';
    --5
    EXECUTE IMMEDIATE 'INSERT INTO IX_POINTADDRESS_PARENT SELECT * FROM IX_POINTADDRESS_PARENT@' ||
                      v_dblink_name ||
                      ' where parent_pa_pid in( select pid from TEMP_IX_ADDRESS t where t.version_id=' ||
                      v_version_id || ' )';
    --6
    EXECUTE IMMEDIATE 'INSERT INTO IX_POINTADDRESS_CHILDREN SELECT * FROM IX_POINTADDRESS_CHILDREN@' ||
                      v_dblink_name ||
                      ' where child_pa_pid in( select pid from TEMP_IX_ADDRESS t where t.version_id=' ||
                      v_version_id || ' )';
    ----11.4 ����
    --1
    EXECUTE IMMEDIATE 'INSERT INTO IX_ANNOTATION SELECT * FROM IX_ANNOTATION@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_ANNO t where t.version_id=' ||
                      v_version_id || ' )';
    --2
    EXECUTE IMMEDIATE 'INSERT INTO IX_ANNOTATION_NAME SELECT * FROM IX_ANNOTATION_NAME@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_ANNO t where t.version_id=' ||
                      v_version_id || ' )';
    --3
    EXECUTE IMMEDIATE 'INSERT INTO IX_ANNOTATION_FLAG SELECT * FROM IX_ANNOTATION_FLAG@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_ANNO t where t.version_id=' ||
                      v_version_id || ' )';
    --------11.5 HAMLET
    --1
    EXECUTE IMMEDIATE 'INSERT INTO IX_HAMLET SELECT * FROM IX_HAMLET@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_HAMLET t where t.version_id=' ||
                      v_version_id || ' )';
    --2
    EXECUTE IMMEDIATE 'INSERT INTO IX_HAMLET_NAME SELECT * FROM IX_HAMLET_NAME@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_HAMLET_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --3
    EXECUTE IMMEDIATE 'INSERT INTO IX_HAMLET_NAME_TONE SELECT * FROM IX_HAMLET_NAME_TONE@' ||
                      v_dblink_name ||
                      ' where name_id in( select name_id from TEMP_IX_HAMLET_NAME t where t.version_id=' ||
                      v_version_id || ' )';
    --4
    EXECUTE IMMEDIATE 'INSERT INTO IX_HAMLET_FLAG SELECT * FROM IX_HAMLET_FLAG@' ||
                      v_dblink_name ||
                      ' where pid in( select pid from TEMP_IX_HAMLET t where t.version_id=' ||
                      v_version_id || ' )';
    ----11.6���������?
    EXECUTE IMMEDIATE 'INSERT INTO IX_CROSSPOINT SELECT * FROM IX_CROSSPOINT@' ||
                      v_dblink_name;
    ----11.7 �շ�վ����
    EXECUTE IMMEDIATE 'INSERT INTO IX_TOLLGATE SELECT * FROM IX_TOLLGATE@' ||
                      v_dblink_name ||
                      ' where mesh_id in (select mesh_id from TEMP_REGION_MESH_MAP t where t.version_id=' ||
                      v_version_id || ')';
    ----11.8��·������
    EXECUTE IMMEDIATE 'INSERT INTO IX_ROADNAME SELECT * FROM IX_ROADNAME@' ||
                      v_dblink_name ||
                      ' where mesh_id in (select mesh_id from TEMP_REGION_MESH_MAP t where t.version_id=' ||
                      v_version_id || ')';
    ----11.9IC���������?
    EXECUTE IMMEDIATE 'INSERT INTO IX_IC SELECT * FROM IX_IC@' ||
                      v_dblink_name ||
                      ' where mesh_id in (select mesh_id from TEMP_REGION_MESH_MAP t where t.version_id=' ||
                      v_version_id || ')';
    ----11.10 �ʱ�����
    EXECUTE IMMEDIATE 'INSERT INTO IX_POSTCODE SELECT * FROM IX_POSTCODE@' ||
                      v_dblink_name ||
                      ' where mesh_id in (select mesh_id from TEMP_REGION_MESH_MAP t where t.version_id=' ||
                      v_version_id || ')';
    --�����쳣������ 
  
  END;

  /**ȡ�����е��������Լ��?*/
  PROCEDURE disable_all_constraint is
  BEGIN
    --11.1poi����Ϣ   
    --1.IX_POI  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI   DISABLE CONSTRAINT   PK_IX_POI   ';
    --2.IX_POI_NAME 
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME  DISABLE CONSTRAINT  PK_IX_POI_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME  DISABLE CONSTRAINT  IXPOI_NAME   ';
    --3.IX_POI_NAME_FLAG  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME_FLAG  DISABLE CONSTRAINT  IXPOI_NAMEFLAG   ';
    --4.IX_POI_NAME_TONE
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME_TONE  DISABLE CONSTRAINT  IXPOINAME_TONE   ';
    --5.IX_POI_ADDRESS 
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ADDRESS   DISABLE CONSTRAINT  PK_IX_POI_ADDRESS   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ADDRESS   DISABLE CONSTRAINT  IXPOI_ADDRESS   ';
    --6.IX_POI_CONTACT
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CONTACT   DISABLE CONSTRAINT  IXPOI_TELEPHONE   ';
    --7.IX_POI_FLAG  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_FLAG   DISABLE CONSTRAINT  IXPOI_FLAG   ';
    --8.IX_POI_ENTRYIMAGE  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ENTRYIMAGE   DISABLE CONSTRAINT  IXPOI_ENTRYIMAGE   ';
    --9.IX_POI_ICON  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ICON   DISABLE CONSTRAINT  PK_IX_POI_ICON   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ICON   DISABLE CONSTRAINT  IXPOI_ICON   ';
    --10 .IX_POI_PHOTO   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PHOTO   DISABLE CONSTRAINT  IXPOI_PHOTO   ';
    --11.IX_POI_AUDIO
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_AUDIO   DISABLE CONSTRAINT  IXPOI_AUDIO   ';
    --12.IX_POI_VIDEO  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_VIDEO   DISABLE CONSTRAINT  IXPOI_VIDEO   ';
    --13.IX_POI_PARENT  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PARENT   DISABLE CONSTRAINT  PK_IX_POI_PARENT   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PARENT   DISABLE CONSTRAINT  IXPOI_PARENT   ';
    --14.IX_POI_CHILDREN  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHILDREN   DISABLE CONSTRAINT  IXPOI_CHILD   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHILDREN   DISABLE CONSTRAINT  IXPOI_PARENT_CHILD   ';
    --15.IX_SAMEPOI   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_SAMEPOI   DISABLE CONSTRAINT  PK_IX_SAMEPOI   ';
    --16.IX_SAMEPOI_PART   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_SAMEPOI_PART   DISABLE CONSTRAINT  IXPOI_GROUP   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_SAMEPOI_PART   DISABLE CONSTRAINT  IXPOI_SAMEPOIPART   ';
    ----11.2poi������? 
    --1.IX_POI_BUILDING  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_BUILDING   DISABLE CONSTRAINT  IXPOI_BUILDING   ';
    --2.IX_POI_DETAIL  
    --3.IX_POI_BUSINESSTIME   
    --4.IX_POI_INTRODUCTION   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_INTRODUCTION   DISABLE CONSTRAINT  PK_IX_POI_INTRODUCTION   ';
    --5.IX_POI_ADVERTISEMENT   ';
    EXECUTE IMMEDIATE '  ALTER TABLE   IX_POI_ADVERTISEMENT  DISABLE CONSTRAINT  PK_IX_POI_ADVERTISEMENT   ';
    --6.IX_POI_GASSTATION   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_GASSTATION   DISABLE CONSTRAINT  PK_IX_POI_GASSTATION   ';
    --7.IX_POI_CHARGINGSTATION  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHARGINGSTATION   DISABLE CONSTRAINT  PK_IX_POI_CHARGINGSTATION   ';
    --8.IX_POI_CHARGINGPLOT  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHARGINGPLOT   DISABLE CONSTRAINT  IXPOICHARGINGSTATION_PLOT   ';
    --9.IX_POI_CHARGINGPLOT  (240ģ������)
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHARGINGPLOT_PH   DISABLE CONSTRAINT  FK_IX_POI_C_REFERENCE_IX_POI_C';
    --10.IX_POI_PARKING  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PARKING   DISABLE CONSTRAINT  PK_IX_POI_PARKING   ';
    --11.IX_POI_ATTRACTION   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ATTRACTION   DISABLE CONSTRAINT  PK_IX_POI_ATTRACTION   ';
    --12.IX_POI_HOTEL  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_HOTEL   DISABLE CONSTRAINT  PK_IX_POI_HOTEL   ';
    --13.IX_POI_RESTAURANT  
    EXECUTE IMMEDIATE '  ALTER TABLE IX_POI_RESTAURANT    DISABLE CONSTRAINT  PK_IX_POI_RESTAURANT   ';
    --14.IX_POI_TOURROUTE   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_POI_TOURROUTE    DISABLE CONSTRAINT  PK_IX_POI_TOURROUTE   ';
    --15.IX_POI_TOURROUTE 
    EXECUTE IMMEDIATE '  ALTER TABLE IX_POI_TOURROUTE    DISABLE CONSTRAINT  PK_IX_POI_TOURROUTE   ';
    --16.IX_POI_CARRENTAL   
    ----11.3������  
    --1.IX_POINTADDRESS  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS   DISABLE CONSTRAINT  PK_IX_POINTADDRESS   ';
    --2.IX_POINTADDRESS_NAME   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_NAME   DISABLE CONSTRAINT  PK_IX_POINTADDRESS_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_NAME   DISABLE CONSTRAINT  IXPOINTADDRESS_NAME   ';
    --3.IX_POINTADDRESS_NAME_TONE   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_NAME_TONE   DISABLE CONSTRAINT  IXPOINTADDRESSNAME_TONE   ';
    --4.IX_POINTADDRESS_FLAG   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_FLAG   DISABLE CONSTRAINT  IXPOINTADDRESS_FLAG   ';
    --5.IX_POINTADDRESS_PARENT   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_PARENT   DISABLE CONSTRAINT  PK_IX_POINTADDRESS_PARENT   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_PARENT   DISABLE CONSTRAINT  IXPOINTADDRESS_PARENT   ';
    --6.IX_POINTADDRESS_CHILDREN  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_CHILDREN   DISABLE CONSTRAINT  IXPOINTADDRESS_PARENT_CHILD   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_CHILDREN   DISABLE CONSTRAINT  IXPOINTADDRESS_CHILDREN   ';
    ----11.4 ����  
    --1.IX_ANNOTATION 
    EXECUTE IMMEDIATE '  ALTER TABLE IX_ANNOTATION    DISABLE CONSTRAINT  PK_IX_ANNOTATION   ';
    --2.IX_ANNOTATION_NAME   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_ANNOTATION_NAME    DISABLE CONSTRAINT  PK_IX_ANNOTATION_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE IX_ANNOTATION_NAME    DISABLE CONSTRAINT  IXANNOTATION_NAME   ';
    --3.IX_ANNOTATION_FLAG   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_ANNOTATION_FLAG   DISABLE CONSTRAINT  IXANNOTATION_FLAG   ';
    --------11.5 HAMLET  
    --1.IX_HAMLET   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_HAMLET    DISABLE CONSTRAINT  PK_IX_HAMLET   ';
    --2.IX_HAMLET_NAME   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_HAMLET_NAME    DISABLE CONSTRAINT  PK_IX_HAMLET_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE IX_HAMLET_NAME    DISABLE CONSTRAINT  IXHAMLE_NAME   ';
    --3.IX_HAMLET_NAME_TONE  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_HAMLET_NAME_TONE   DISABLE CONSTRAINT  IXHAMLETNAME_TONE   ';
    --4.IX_HAMLET_FLAG   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_HAMLET_FLAG   DISABLE CONSTRAINT  IXHAMLET_FLAG   ';
    ----11.6���������?IX_CROSSPOINT   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_CROSSPOINT   DISABLE CONSTRAINT  PK_IX_CROSSPOINT   ';
    ----11.7 �շ�վ���� IX_TOLLGATE   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_TOLLGATE   DISABLE CONSTRAINT  PK_IX_TOLLGATE   ';
    ----11.8��·������ IX_ROADNAME  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_ROADNAME   DISABLE CONSTRAINT  PK_IX_ROADNAME   ';
    ----11.9IC���������?IX_IC  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_IC   DISABLE CONSTRAINT  PK_IX_IC   ';
    ----11.10 �ʱ����� IX_POSTCODE   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POSTCODE   DISABLE CONSTRAINT  PK_IX_POSTCODE   ';
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE(' DISABLE CONSTRAINT ERROR');
    
  END;

  /**�ָ����е��������Լ��?*/
  PROCEDURE enable_all_constraint is
  BEGIN
    --11.1poi����Ϣ   
    --1.IX_POI  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI   ENABLE CONSTRAINT   PK_IX_POI   ';
    --2.IX_POI_NAME 
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME  ENABLE CONSTRAINT  PK_IX_POI_NAME   ';
    --:3:4 EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME  ENABLE CONSTRAINT  IXPOI_NAME   ';
    --3.IX_POI_NAME_FLAG  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME_FLAG  ENABLE CONSTRAINT  IXPOI_NAMEFLAG   ';
    --4.IX_POI_NAME_TONE
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_NAME_TONE  ENABLE CONSTRAINT  IXPOINAME_TONE   ';
    --5.IX_POI_ADDRESS 
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ADDRESS   ENABLE CONSTRAINT  PK_IX_POI_ADDRESS   ';
    --:5:6  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ADDRESS   ENABLE CONSTRAINT  IXPOI_ADDRESS   ';
    --6.IX_POI_CONTACT
    --:7:8  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CONTACT   ENABLE CONSTRAINT  IXPOI_TELEPHONE   ';
    --7.IX_POI_FLAG  
    --:9:10  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_FLAG   ENABLE CONSTRAINT  IXPOI_FLAG   ';
    --8.IX_POI_ENTRYIMAGE  
    --:11:12  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ENTRYIMAGE   ENABLE CONSTRAINT  IXPOI_ENTRYIMAGE   ';
    --9.IX_POI_ICON  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ICON   ENABLE CONSTRAINT  PK_IX_POI_ICON   ';
    --:13:14  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ICON   ENABLE CONSTRAINT  IXPOI_ICON   ';
    --10 .IX_POI_PHOTO   
    --:15:16  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PHOTO   ENABLE CONSTRAINT  IXPOI_PHOTO   ';
    --11.IX_POI_AUDIO
    --:17:18  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_AUDIO   ENABLE CONSTRAINT  IXPOI_AUDIO   ';
    --12.IX_POI_VIDEO  
    --:19:20  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_VIDEO   ENABLE CONSTRAINT  IXPOI_VIDEO   ';
    --13.IX_POI_PARENT  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PARENT   ENABLE CONSTRAINT  PK_IX_POI_PARENT   ';
    --:21:22  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PARENT   ENABLE CONSTRAINT  IXPOI_PARENT   ';
    --14.IX_POI_CHILDREN  
    --:23:24  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHILDREN   ENABLE CONSTRAINT  IXPOI_CHILD   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHILDREN   ENABLE CONSTRAINT  IXPOI_PARENT_CHILD   ';
    --15.IX_SAMEPOI   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_SAMEPOI   ENABLE CONSTRAINT  PK_IX_SAMEPOI   ';
    --16.IX_SAMEPOI_PART   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_SAMEPOI_PART   ENABLE CONSTRAINT  IXPOI_GROUP   ';
    --??  EXECUTE IMMEDIATE '  ALTER TABLE  IX_SAMEPOI_PART   ENABLE CONSTRAINT  IXPOI_SAMEPOIPART   ';
    ----11.2poi������? 
    --1.IX_POI_BUILDING  
    --??  EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_BUILDING   ENABLE CONSTRAINT  IXPOI_BUILDING   ';
    --2.IX_POI_DETAIL  
    --3.IX_POI_BUSINESSTIME   
    --4.IX_POI_INTRODUCTION   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_INTRODUCTION   ENABLE CONSTRAINT  PK_IX_POI_INTRODUCTION   ';
    --5.IX_POI_ADVERTISEMENT   ';
    EXECUTE IMMEDIATE '  ALTER TABLE   IX_POI_ADVERTISEMENT  ENABLE CONSTRAINT  PK_IX_POI_ADVERTISEMENT   ';
    --6.IX_POI_GASSTATION   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_GASSTATION   ENABLE CONSTRAINT  PK_IX_POI_GASSTATION   ';
    --7.IX_POI_CHARGINGSTATION  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHARGINGSTATION   ENABLE CONSTRAINT  PK_IX_POI_CHARGINGSTATION   ';
    --8.IX_POI_CHARGINGPLOT  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHARGINGPLOT   ENABLE CONSTRAINT  IXPOICHARGINGSTATION_PLOT   ';
    --9.IX_POI_CHARGINGPLOT (240ģ������)
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_CHARGINGPLOT   ENABLE CONSTRAINT  FK_IX_POI_C_REFERENCE_IX_POI_C   ';
    --10.IX_POI_PARKING  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_PARKING   ENABLE CONSTRAINT  PK_IX_POI_PARKING   ';
    --11.IX_POI_ATTRACTION   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_ATTRACTION   ENABLE CONSTRAINT  PK_IX_POI_ATTRACTION   ';
    --12.IX_POI_HOTEL  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POI_HOTEL   ENABLE CONSTRAINT  PK_IX_POI_HOTEL   ';
    --13.IX_POI_RESTAURANT  
    EXECUTE IMMEDIATE '  ALTER TABLE IX_POI_RESTAURANT    ENABLE CONSTRAINT  PK_IX_POI_RESTAURANT   ';
    --14.IX_POI_TOURROUTE   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_POI_TOURROUTE    ENABLE CONSTRAINT  PK_IX_POI_TOURROUTE   ';
    --15.IX_POI_TOURROUTE 
    EXECUTE IMMEDIATE '  ALTER TABLE IX_POI_TOURROUTE    ENABLE CONSTRAINT  PK_IX_POI_TOURROUTE   ';
    --16.IX_POI_CARRENTAL   
    ----11.3������  
    --1.IX_POINTADDRESS  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS   ENABLE CONSTRAINT  PK_IX_POINTADDRESS   ';
    --2.IX_POINTADDRESS_NAME   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_NAME   ENABLE CONSTRAINT  PK_IX_POINTADDRESS_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_NAME   ENABLE CONSTRAINT  IXPOINTADDRESS_NAME   ';
    --3.IX_POINTADDRESS_NAME_TONE   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_NAME_TONE   ENABLE CONSTRAINT  IXPOINTADDRESSNAME_TONE   ';
    --4.IX_POINTADDRESS_FLAG   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_FLAG   ENABLE CONSTRAINT  IXPOINTADDRESS_FLAG   ';
    --5.IX_POINTADDRESS_PARENT   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_PARENT   ENABLE CONSTRAINT  PK_IX_POINTADDRESS_PARENT   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_PARENT   ENABLE CONSTRAINT  IXPOINTADDRESS_PARENT   ';
    --6.IX_POINTADDRESS_CHILDREN  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_CHILDREN   ENABLE CONSTRAINT  IXPOINTADDRESS_PARENT_CHILD ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POINTADDRESS_CHILDREN   ENABLE CONSTRAINT  IXPOINTADDRESS_CHILDREN   ';
    ----11.4 ����  
    --1.IX_ANNOTATION 
    EXECUTE IMMEDIATE '  ALTER TABLE IX_ANNOTATION    ENABLE CONSTRAINT  PK_IX_ANNOTATION   ';
    --2.IX_ANNOTATION_NAME   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_ANNOTATION_NAME    ENABLE CONSTRAINT  PK_IX_ANNOTATION_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE IX_ANNOTATION_NAME    ENABLE CONSTRAINT  IXANNOTATION_NAME   ';
    --3.IX_ANNOTATION_FLAG   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_ANNOTATION_FLAG   ENABLE CONSTRAINT  IXANNOTATION_FLAG   ';
    --------11.5 HAMLET  
    --1.IX_HAMLET   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_HAMLET    ENABLE CONSTRAINT  PK_IX_HAMLET   ';
    --2.IX_HAMLET_NAME   
    EXECUTE IMMEDIATE '  ALTER TABLE IX_HAMLET_NAME    ENABLE CONSTRAINT  PK_IX_HAMLET_NAME   ';
    EXECUTE IMMEDIATE '  ALTER TABLE IX_HAMLET_NAME    ENABLE CONSTRAINT  IXHAMLE_NAME   ';
    --3.IX_HAMLET_NAME_TONE  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_HAMLET_NAME_TONE   ENABLE CONSTRAINT  IXHAMLETNAME_TONE   ';
    --4.IX_HAMLET_FLAG   ';
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_HAMLET_FLAG   ENABLE CONSTRAINT  IXHAMLET_FLAG   ';
    ----11.6���������?IX_CROSSPOINT   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_CROSSPOINT   ENABLE CONSTRAINT  PK_IX_CROSSPOINT   ';
    ----11.7 �շ�վ���� IX_TOLLGATE   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_TOLLGATE   ENABLE CONSTRAINT  PK_IX_TOLLGATE   ';
    ----11.8��·������ IX_ROADNAME  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_ROADNAME   ENABLE CONSTRAINT  PK_IX_ROADNAME   ';
    ----11.9IC���������?IX_IC  
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_IC   ENABLE CONSTRAINT  PK_IX_IC   ';
    ----11.10 �ʱ����� IX_POSTCODE   
    EXECUTE IMMEDIATE '  ALTER TABLE  IX_POSTCODE   ENABLE CONSTRAINT  PK_IX_POSTCODE   ';
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('ENABLE CONSTRAINT ERROE');
    
  END;

  /**
  **�������?�߼�����ȥ��
  **/
  PROCEDURE delete_reduplicated_record is
  BEGIN
  
    --1.IX_POI PID
    EXECUTE IMMEDIATE 'delete from IX_POI a
  where a.rowid != (select max(b.rowid)
                      from IX_POI b
                     where 1 = 1
                       and a.PID = b.pid)';
    --2.IX_POI_NAME  NAME_ID
    EXECUTE IMMEDIATE 'delete from IX_POI_NAME a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_NAME b
                     where 1 = 1
                       and a.name_id = b.name_id)';
    --3.IX_POI_NAME_FLAG  "NAME_ID FLAG_CODE"  180A
    EXECUTE IMMEDIATE 'delete from IX_POI_NAME_FLAG a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_NAME_FLAG b
                     where 1 = 1
                       and a.name_id = b.name_id
                       and a.flag_code = b.flag_code)';
  
    --4. IX_POI_NAME_TONE*  "NAME_ID TONE_A  TONE_B"  �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_NAME_TONE a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_NAME_TONE b
                     where 1 = 1
                       and a.name_id = b.name_id
                       and a.TONE_A = b.TONE_A
                       and a.TONE_B = b.TONE_B)';
    --5. IX_POI_ADDRESS  NAME_ID  
    EXECUTE IMMEDIATE 'delete from IX_POI_ADDRESS a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_ADDRESS b
                     where 1 = 1
                       and a.name_id = b.name_id)';
  
    --6.IX_POI_CONTACT  "POI_PID CONTACT_TYPE CONTACT"  
    EXECUTE IMMEDIATE 'delete from IX_POI_CONTACT a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_CONTACT b
                     where 1 = 1
                       and a.POI_PID = a.POI_PID
                       and a.CONTACT_TYPE = b.CONTACT_TYPE
                       and a.CONTACT = b.CONTACT)';
    --7. IX_POI_FLAG  "POI_PID FLAG_CODE"  
    EXECUTE IMMEDIATE 'delete from IX_POI_FLAG a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_FLAG b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID
                       and a.FLAG_CODE = b.FLAG_CODE)';
    --8.IX_POI_ENTRYIMAGE  "POI_PID IMAGE_CODE"  
    EXECUTE IMMEDIATE 'delete from IX_POI_ENTRYIMAGE a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_ENTRYIMAGE b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID
                       and a.IMAGE_CODE = b.IMAGE_CODE)';
    -- 9. IX_POI_ICON  REL_ID  
    EXECUTE IMMEDIATE 'delete from IX_POI_ICON a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_ICON b
                     where 1 = 1
                       and a.REL_ID = b.REL_ID)';
    --10.IX_POI_PHOTO  "POI_PID PHOTO_ID"  
    EXECUTE IMMEDIATE 'delete from IX_POI_PHOTO a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_PHOTO b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID
                       and a.PHOTO_ID = b.PHOTO_ID)';
    --11.IX_POI_AUDIO  "POI_PID AUDIO_ID"  
    EXECUTE IMMEDIATE 'delete from IX_POI_AUDIO a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_AUDIO b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID
                       and a.AUDIO_ID = b.AUDIO_ID)';
    --12.IX_POI_VIDEO  "POI_PID VIDEO_ID"  
    EXECUTE IMMEDIATE 'delete from IX_POI_VIDEO a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_VIDEO b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID
                       and a.VIDEO_ID = b.VIDEO_ID)';
    --13. IX_POI_RESTAURANT  RESTAURANT_ID  
    EXECUTE IMMEDIATE 'delete from IX_POI_RESTAURANT a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_RESTAURANT b
                     where 1 = 1
                       and a.RESTAURANT_ID = b.RESTAURANT_ID)';
    --14.IX_POI_HOTEL  HOTEL_ID  
    EXECUTE IMMEDIATE 'delete from IX_POI_HOTEL a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_HOTEL b
                     where 1 = 1
                       and a.HOTEL_ID = b.HOTEL_ID)';
    --15.IX_POI_INTRODUCTION  INTRODUCTION_ID  181A,2012/10/22,���������?
    EXECUTE IMMEDIATE 'delete from IX_POI_INTRODUCTION a
  where a.rowid !=
        (select max(b.rowid)
           from IX_POI_INTRODUCTION b
          where 1 = 1
            and a.INTRODUCTION_ID = b.INTRODUCTION_ID)';
    --16.IX_POI_ATTRACTION  ATTRACTION_ID  181A,2012/10/22,���������?
    EXECUTE IMMEDIATE 'delete from IX_POI_ATTRACTION a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_ATTRACTION b
                     where 1 = 1
                       and a.ATTRACTION_ID = b.ATTRACTION_ID)';
    --17.IX_POI_BUSINESSTIME  BUSINESSTIME_ID  1901D,2013/3/20,
    ----   ɾ������Լ��
    EXECUTE IMMEDIATE 'DELETE FROM IX_POI_BUSINESSTIME A
  WHERE A.ROWID !=
    (SELECT MAX(B.ROWID)
       FROM IX_POI_BUSINESSTIME B
      WHERE 1 = 1
        AND A.BUSINESSTIME_ID = B.BUSINESSTIME_ID
        AND A.POI_PID=B.POI_PID
        AND A.MON_SRT=B.MON_SRT
        AND A.MON_END=B.MON_END
        AND A.WEEK_IN_YEAR_SRT=B.WEEK_IN_YEAR_SRT
        AND A.WEEK_IN_YEAR_END=B.WEEK_IN_YEAR_END
        AND A.WEEK_IN_MONTH_SRT=B.WEEK_IN_MONTH_SRT
        AND A.WEEK_IN_MONTH_END=B.WEEK_IN_MONTH_END
        AND A.VALID_WEEK=B.VALID_WEEK
        AND A.DAY_SRT=B.DAY_SRT
        AND A.DAY_END=B.DAY_END
        AND A.TIME_SRT=B.TIME_SRT
        AND A.TIME_DUR=B.TIME_DUR
        AND A.RESERVED=B.RESERVED
        AND A.MEMO=B.MEMO';
    --18.IX_POI_ADVERTISEMENT  ADVERTISE_ID  �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_ADVERTISEMENT a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_ADVERTISEMENT b
                     where 1 = 1
                       and a.ADVERTISE_ID = b.ADVERTISE_ID)';
    --19.IX_POI_GASSTATION  GASSTATION_ID  �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_GASSTATION a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_GASSTATION b
                     where 1 = 1
                       and a.GASSTATION_ID = b.GASSTATION_ID)';
    --20.IX_POI_CHARGINGSTATION  CHARGING_ID  �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_CHARGINGSTATION a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_CHARGINGSTATION b
                     where 1 = 1
                       and a.CHARGING_ID = b.CHARGING_ID)';
    --21.IX_POI_CHARGINGPLOT_PH  CHARGING_ID  �ݲ���Ҫ(240������)
    EXECUTE IMMEDIATE 'delete from IX_POI_CHARGINGPLOT_PH a
 where a.rowid != (select max(b.rowid)
                     from IX_POI_CHARGINGPLOT_PH b
                    where 1 = 1
                      and a.POI_PID = b.POI_PID
                      and b.photo_name = a.photo_name)';
  
    --22.IX_POI_CHARGINGPLOT  "POI_PID GROUP_ID"  �ݲ���Ҫ
    EXECUTE IMMEDIATE '
delete from IX_POI_CHARGINGPLOT a
 where a.rowid != (select max(b.rowid)
                     from IX_POI_CHARGINGPLOT b
                    where 1 = 1
                      and a.POI_PID = b.POI_PID
                      and a.GROUP_ID = b.GROUP_ID
                      and a.group_id = b.group_id
                      and a.count = b.count
                      and a.acdc = b.acdc
                      and a.plug_type = b.plug_type
                      and a.power = b.power
                      and a.voltage = b.voltage
                      and a."CURRENT" = B."CURRENT"
                      and a."MODE" = b."MODE"
                      and a.memo = b.memo
                      and a.plug_num = b.plug_num
                      and a.prices = b.prices
                      and a.open_type = b.open_type
                      and a.available_state = b.available_state
                      and a.plot_num = b.plot_num
                      and a.product_num = b.product_num
                      and a.parking_num = b.parking_num
                      and a.floor = b.floor
                      and a.location_type = b.location_type
                      and a.manufacturer = b.manufacturer
                      and a.factory_num = b.factory_num)';
    --23.IX_POI_TOURROUTE   TOUR_ID  190A,�ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_TOURROUTE a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_TOURROUTE b
                     where 1 = 1
                       and a.TOUR_ID = b.TOUR_ID)';
  
    --24.IX_POI_EVENT  EVENT_ID  190A,�ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_EVENT a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_EVENT b
                     where 1 = 1
                       and a.EVENT_ID = b.EVENT_ID)';
    --25.IX_POI_PARKING  PARKING_ID  190A,�ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_PARKING a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_PARKING b
                     where 1 = 1
                       and a.PARKING_ID = b.PARKING_ID)';
    --26.IX_POI_BUILDING  "POI_PID FLOOR_USED FLOOR_EMPTY"  �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_BUILDING a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_BUILDING b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID)';
    --27.IX_POI_CARRENTAL  POI_PID  210,�ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_CARRENTAL a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_CARRENTAL b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID)';
    --28.IX_POI_DETAIL  "POI_PID WEB_SITE"  190A,�ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POI_DETAIL a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_DETAIL b
                     where 1 = 1
                       and a.POI_PID = b.POI_PID)';
    --29. IX_SAMEPOI  GROUP_ID  
    EXECUTE IMMEDIATE 'delete from IX_SAMEPOI a
  where a.rowid != (select max(b.rowid)
                      from IX_SAMEPOI b
                     where 1 = 1
                       and a.GROUP_ID = b.GROUP_ID)';
    --30.IX_SAMEPOI_PART  "GROUP_ID POI_PID"  
    EXECUTE IMMEDIATE 'delete from IX_SAMEPOI_PART a
  where a.rowid != (select max(b.rowid)
                      from IX_SAMEPOI_PART b
                     where 1 = 1
                       and a.GROUP_ID = b.GROUP_ID
                       and a.POI_PID = b.POI_PID)';
    --31.IX_POI_PARENT  GROUP_ID  
    EXECUTE IMMEDIATE 'delete from IX_POI_PARENT a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_PARENT b
                     where 1 = 1
                       and a.GROUP_ID = b.GROUP_ID)';
    --32.IX_POI_CHILDREN  "GROUP_ID CHILD_POI_PID" 
    EXECUTE IMMEDIATE 'delete from IX_POI_CHILDREN a
  where a.rowid != (select max(b.rowid)
                      from IX_POI_CHILDREN b
                     where 1 = 1
                       and a.GROUP_ID = b.GROUP_ID
                       and a.CHILD_POI_PID = b.CHILD_POI_PID)';
    --33.IX_POINTADDRESS  PID  
    EXECUTE IMMEDIATE 'delete from IX_POINTADDRESS a
  where a.rowid != (select max(b.rowid)
                      from IX_POINTADDRESS b
                     where 1 = 1
                       and a.PID = b.PID)';
    --34.IX_POINTADDRESS_NAME  NAME_ID 
    EXECUTE IMMEDIATE 'delete from IX_POINTADDRESS_NAME a
  where a.rowid != (select max(b.rowid)
                      from IX_POINTADDRESS_NAME b
                     where 1 = 1
                       and a.NAME_ID = b.NAME_ID)';
    --35.IX_POINTADDRESS_NAME_TONE*  "NAME_ID TONE_A TONE_B"  �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_POINTADDRESS_NAME_TONE a
  where a.rowid != (select max(b.rowid)
                      from IX_POINTADDRESS_NAME_TONE b
                     where 1 = 1
                       and a.NAME_ID = b.NAME_ID
                       and a.TONE_A = b.TONE_A
                       and a.TONE_B = b.TONE_B)';
    --36.IX_POINTADDRESS_FLAG  "PID FLAG_CODE"
    EXECUTE IMMEDIATE 'delete from IX_POINTADDRESS_FLAG a
  where a.rowid != (select max(b.rowid)
                      from IX_POINTADDRESS_FLAG b
                     where 1 = 1
                       and a.PID = b.PID
                       and a.FLAG_CODE = b.FLAG_CODE)';
    --37.IX_POINTADDRESS_PARENT  GROUP_ID  
    EXECUTE IMMEDIATE 'delete from IX_POINTADDRESS_PARENT a
  where a.rowid != (select max(b.rowid)
                      from IX_POINTADDRESS_PARENT b
                     where 1 = 1
                       and a.GROUP_ID = b.GROUP_ID)';
    --38.IX_POINTADDRESS_CHILDREN  "GROUP_ID CHILD_PA_PID"  
    EXECUTE IMMEDIATE 'delete from IX_POINTADDRESS_CHILDREN a
  where a.rowid != (select max(b.rowid)
                      from IX_POINTADDRESS_CHILDREN b
                     where 1 = 1
                       and a.GROUP_ID = b.GROUP_ID
                       and a.CHILD_PA_PID = b.CHILD_PA_PID)';
    --39.IX_ANNOTATION  PID  
    EXECUTE IMMEDIATE 'delete from IX_ANNOTATION a
  where a.rowid != (select max(b.rowid)
                      from IX_ANNOTATION b
                     where 1 = 1
                       and a.PID = b.PID)';
    --40. IX_ANNOTATION_NAME  NAME_ID 
    EXECUTE IMMEDIATE 'delete from IX_ANNOTATION_NAME a
  where a.rowid != (select max(b.rowid)
                      from IX_ANNOTATION_NAME b
                     where 1 = 1
                       and a.NAME_ID = b.NAME_ID)';
    --41.IX_ANNOTATION_FLAG  "PID FLAG_CODE"  
    EXECUTE IMMEDIATE 'delete from IX_ANNOTATION_FLAG a
  where a.rowid != (select max(b.rowid)
                      from IX_ANNOTATION_FLAG b
                     where 1 = 1
                       and a.PID = b.PID
                       and a.FLAG_CODE = b.FLAG_CODE)';
    --42.IX_HAMLET  PID 
    EXECUTE IMMEDIATE 'delete from IX_HAMLET a
  where a.rowid != (select max(b.rowid)
                      from IX_HAMLET b
                     where 1 = 1
                       and a.PID = b.PID)';
    --43.IX_HAMLET_NAME NAME_ID 
    EXECUTE IMMEDIATE 'delete from IX_HAMLET_NAME a
  where a.rowid != (select max(b.rowid)
                      from IX_HAMLET_NAME b
                     where 1 = 1
                       and a.NAME_ID = b.NAME_ID)';
    --44.IX_HAMLET_NAME_TONE* "NAME_ID TONE_A TONE_B" �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_HAMLET_NAME_TONE a
  where a.rowid != (select max(b.rowid)
                      from IX_HAMLET_NAME_TONE b
                     where 1 = 1
                       and a.NAME_ID = b.NAME_ID
                       and a.TONE_A = b.TONE_A
                       and a.TONE_B = b.TONE_B)';
    --45. IX_HAMLET_FLAG  "PID FLAG_CODE" 
    EXECUTE IMMEDIATE 'delete from IX_HAMLET_FLAG a
  where a.rowid != (select max(b.rowid)
                      from IX_HAMLET_FLAG b
                     where 1 = 1
                       and a.PID = b.PID
                       and a.FLAG_CODE = b.FLAG_CODE)';
    --46. IX_POSTCODE POST_ID 
    EXECUTE IMMEDIATE 'delete from IX_POSTCODE a
  where a.rowid != (select max(b.rowid)
                      from IX_POSTCODE b
                     where 1 = 1
                       and a.POST_ID = b.POST_ID)';
    --47.IX_CROSSPOINT  PID �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_CROSSPOINT a
  where a.rowid != (select max(b.rowid)
                      from IX_CROSSPOINT b
                     where 1 = 1
                       and a.PID = b.PID)';
    --48.IX_TOLLGATE  PID �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_TOLLGATE a
  where a.rowid != (select max(b.rowid)
                      from IX_TOLLGATE b
                     where 1 = 1
                       and a.PID = b.PID)';
    --49.IX_ROADNAME  PID �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_ROADNAME a
  where a.rowid != (select max(b.rowid)
                      from IX_ROADNAME b
                     where 1 = 1
                       and a.PID = b.PID)';
    --50.IX_IC  PID �ݲ���Ҫ
    EXECUTE IMMEDIATE 'delete from IX_IC a
  where a.rowid != (select max(b.rowid)
                      from IX_IC b
                     where 1 = 1
                       and a.PID = b.PID)';
  
  END;

  /**ִ��ĳ���������ں�**/
  PROCEDURE do_region_merge(v_rec_version_info region_info_type) IS
    v_dbLink_name varchar2(40);
  BEGIN
    v_dbLink_name := 'GDB_DBLINK_' || v_rec_version_info.version_Id;
    --1.����ָ������ĸ���dblink
    create_region_gdb_dblink(v_rec_version_info.ip,
                             v_rec_version_info.port,
                             v_rec_version_info.sid,
                             v_rec_version_info.user_Name,
                             v_rec_version_info.user_Password,
                             v_dbLink_name);
    --2.ִ��������ݵ��ں�?  
    do_index_merge(v_dbLink_name, v_rec_version_info.version_Id);
  END;

  PROCEDURE virtual_merge(region_info_list REGION_INFO_LIST) IS
    v_rec_version_info region_info_type;
    v_count            number;
  BEGIN
    --1.ȡ��ǰ����������Լ��
    disable_all_constraint();
    --2.ѭ�������������ȡ��ÿ����Ĳ���ִ������ں�?
    v_count := region_info_list.count;
    for i in 1 .. v_count loop
      v_rec_version_info := region_info_list(i);
      do_region_merge(v_rec_version_info);
    end loop;
    --3.���ȥ��?
    -- delete_reduplicated_record(); 20140511���ù����ӿ�RemoveDupPrimaryKeyData
    --4.�ָ������Լ��?
    --  enable_all_constraint(); ��Ϊjava�����е���
    null;
  END;

end REGION_GDB_VIRTUAL_MERGE;
/
