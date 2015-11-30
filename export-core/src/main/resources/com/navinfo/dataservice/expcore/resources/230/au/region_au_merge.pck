CREATE OR REPLACE PACKAGE region_au_merge IS
/*
将给定的数据库的相关数据合并到当前的数据库中
合并方式：all 全要素；
*/
 PROCEDURE merge_All(v_ip varchar2,v_port number,v_sid varchar2, v_schema_user varchar2,v_schema_pwd varchar2);
 
 /*
将给定的数据库的相关数据合并到当前的数据库中
合并方式：road 道路；
*/
 PROCEDURE merge_Road(v_ip varchar2,v_port number,v_sid varchar2, v_schema_user varchar2,v_schema_pwd varchar2);
  /*
将给定的数据库的相关数据合并到当前的数据库中
合并方式：index 索引；
*/
 PROCEDURE merge_Index(v_ip varchar2,v_port number,v_sid varchar2, v_schema_user varchar2,v_schema_pwd varchar2);

 
END region_au_merge;
/
CREATE OR REPLACE PACKAGE BODY region_au_merge IS
  /*创建dblink*/
  PROCEDURE create_region_au_db_link(v_ip          varchar2,
                                     v_port        number,
                                     v_sid         varchar2,
                                     v_schema_user varchar2,
                                     v_schema_pwd  varchar2) is
    v_db_link_count NUMBER;
  begin
    --创建dblink
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_db_links where db_link = ''REGION_AU_DB_LINK'''
      INTO v_db_link_count;
    IF (v_db_link_count > 0) THEN
      EXECUTE IMMEDIATE 'DROP DATABASE LINK region_au_db_link';
    END IF;
    EXECUTE IMMEDIATE 'create database link region_au_db_link
  connect to ' || v_schema_user || ' identified by "' ||
                      v_schema_pwd || '"
  using ''(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = ' || v_ip ||
                      ' )(PORT = ' || v_port ||
                      ' )))(CONNECT_DATA = (SERVICE_NAME = ' || v_sid ||
                      ' )))''';
  end;

  /*创建道路临时表*/
  PROCEDURE create_road_temptable is
    v_table_count NUMBER;
  begin
    --删除原始备份表
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_MARK_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_MARK_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_GPSRECORD_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_GPSRECORD_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_DRAFT_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_DRAFT_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_MARK_PHOTO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_MARK_PHOTO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_MARK_AUDIO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_MARK_AUDIO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_MARK_VIDEO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_MARK_VIDEO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_TOPOIMAGE_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_TOPOIMAGE_XA';
    END IF;
  
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_GPSTRACK_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_GPSTRACK_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_GPSTRACK_GROUP_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_GPSTRACK_GROUP_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_GPSTRACK_GROUP_VIDEO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_GPSTRACK_GROUP_VIDEO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_GPSTRACK_PHOTO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_GPSTRACK_PHOTO_XA';
    END IF;
  
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_AUDIO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_AUDIO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_VIDEO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_VIDEO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_PHOTO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_PHOTO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_SERIESPHOTO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_SERIESPHOTO_XA';
    END IF;
  
    --创建临时表 
    EXECUTE IMMEDIATE 'create table AU_MARK_XA as
      select * from AU_MARK@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_GPSRECORD_XA as
      select * from AU_GPSRECORD@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_MARK_PHOTO_XA as
      select * from AU_MARK_PHOTO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_MARK_AUDIO_XA as
      select * from AU_MARK_AUDIO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_MARK_VIDEO_XA as
      select * from AU_MARK_VIDEO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_TOPOIMAGE_XA as
      select * from AU_TOPOIMAGE@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_DRAFT_XA as
      select * from AU_DRAFT@region_au_db_link';
  
    EXECUTE IMMEDIATE 'create table AU_GPSTRACK_XA as
      select * from AU_GPSTRACK@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_GPSTRACK_GROUP_XA as
      select * from AU_GPSTRACK_GROUP@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_GPSTRACK_GROUP_VIDEO_XA as
      select * from AU_GPSTRACK_GROUP_VIDEO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_GPSTRACK_PHOTO_XA as
      select * from AU_GPSTRACK_PHOTO@region_au_db_link';
  
    EXECUTE IMMEDIATE 'create table AU_AUDIO_XA as
      select * from AU_AUDIO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_VIDEO_XA as
      select * from AU_VIDEO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_PHOTO_XA as
      select * from AU_PHOTO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_SERIESPHOTO_XA as
      select * from AU_SERIESPHOTO@region_au_db_link';
  end;

  /*创建索引临时表*/
  PROCEDURE create_index_temptable is
    v_table_count NUMBER;
  begin
    --删除原始备份表
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_ADDRESS_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_ADDRESS_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_BUILDING_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_BUILDING_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_CHARGINGPLOT_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_CHARGINGPLOT_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_CHARGINGSTATION_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_CHARGINGSTATION_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_CHILDREN_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_CHILDREN_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_CONTACT_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_CONTACT_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_FLAG_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_FLAG_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_GASSTATION_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_GASSTATION_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_HOTEL_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_HOTEL_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_NAME_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_NAME_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_NAME_FLAG_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_NAME_FLAG_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_NAME_RP00_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_NAME_RP00_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_NOKIA_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_NOKIA_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_PARENT_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_PARENT_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_PARKING_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_PARKING_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_PHOTO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_PHOTO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_RESTAURANT_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_RESTAURANT_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POI_RP00_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POI_RP00_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_SAMEPOI_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_SAMEPOI_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_SAMEPOI_PART_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_SAMEPOI_PART_XA';
    END IF;
  
    --点门牌
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POINTADDRESS_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POINTADDRESS_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POINTADDRESS_CHILDREN_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POINTADDRESS_CHILDREN_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POINTADDRESS_FLAG_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POINTADDRESS_FLAG_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POINTADDRESS_NAME_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POINTADDRESS_NAME_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_POINTADDRESS_PARENT_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_POINTADDRESS_PARENT_XA';
    END IF;
  
    --文字
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_ANNOTATION_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_ANNOTATION_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_IX_ANNOTATION_NAME_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_IX_ANNOTATION_NAME_XA';
    END IF;
  
    --创建临时表 
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_XA AS
      SELECT * FROM AU_IX_POI@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_ADDRESS_XA AS
      SELECT * FROM AU_IX_POI_ADDRESS@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_BUILDING_XA AS
      SELECT * FROM AU_IX_POI_BUILDING@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_CHARGINGPLOT_XA AS
      SELECT * FROM AU_IX_POI_CHARGINGPLOT@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_CHARGINGSTATION_XA AS
      SELECT * FROM AU_IX_POI_CHARGINGSTATION@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_CHILDREN_XA AS
      SELECT * FROM AU_IX_POI_CHILDREN@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_CONTACT_XA AS
      SELECT * FROM AU_IX_POI_CONTACT@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_FLAG_XA AS
      SELECT * FROM AU_IX_POI_FLAG@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_GASSTATION_XA AS
      SELECT * FROM AU_IX_POI_GASSTATION@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_HOTEL_XA AS
      SELECT * FROM AU_IX_POI_HOTEL@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_NAME_XA AS
      SELECT * FROM AU_IX_POI_NAME@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_NAME_FLAG_XA AS
      SELECT * FROM AU_IX_POI_NAME_FLAG@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_NAME_RP00_XA AS
      SELECT * FROM AU_IX_POI_NAME_RP00@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_NOKIA_XA AS
      SELECT * FROM AU_IX_POI_NOKIA@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_PARENT_XA AS
      SELECT * FROM AU_IX_POI_PARENT@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_PARKING_XA AS
      SELECT * FROM AU_IX_POI_PARKING@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_PHOTO_XA AS
      SELECT * FROM AU_IX_POI_PHOTO@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_RESTAURANT_XA AS
      SELECT * FROM AU_IX_POI_RESTAURANT@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POI_RP00_XA AS
      SELECT * FROM AU_IX_POI_RP00@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_SAMEPOI_XA AS
      SELECT * FROM AU_IX_SAMEPOI@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_SAMEPOI_PART_XA AS
      SELECT * FROM AU_IX_SAMEPOI_PART@region_au_db_link';
  
    --点门牌
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POINTADDRESS_XA AS
      SELECT * FROM AU_IX_POINTADDRESS@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POINTADDRESS_CHILDREN_XA AS
      SELECT * FROM AU_IX_POINTADDRESS_CHILDREN@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POINTADDRESS_FLAG_XA AS
      SELECT * FROM AU_IX_POINTADDRESS_FLAG@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POINTADDRESS_NAME_XA AS
      SELECT * FROM AU_IX_POINTADDRESS_NAME@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_POINTADDRESS_PARENT_XA AS
      SELECT * FROM AU_IX_POINTADDRESS_PARENT@region_au_db_link';
    --文字
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_ANNOTATION_XA AS
      SELECT * FROM AU_IX_ANNOTATION@region_au_db_link';
    EXECUTE IMMEDIATE 'CREATE TABLE AU_IX_ANNOTATION_NAME_XA AS
      SELECT * FROM AU_IX_ANNOTATION_NAME@region_au_db_link';
  
  end;

  /*创建其它（任务信息等）临时表*/
  PROCEDURE create_other_temptable is
    v_table_count NUMBER;
  begin
    --删除原始备份表
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_TASK_INFO_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_TASK_INFO_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_DATA_STATISTICS_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_DATA_STATISTICS_XA';
    END IF;
    EXECUTE IMMEDIATE 'select nvl(count(1),0) from user_tables where TABLE_NAME = ''AU_LOG_STATISTICS_XA'''
      INTO v_table_count;
    IF (v_table_count > 0) THEN
      EXECUTE IMMEDIATE 'drop table AU_LOG_STATISTICS_XA';
    END IF;
  
    --创建临时表 
    EXECUTE IMMEDIATE 'create table AU_TASK_INFO_XA as
      select * from AU_TASK_INFO@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_DATA_STATISTICS_XA as
      select * from AU_DATA_STATISTICS@region_au_db_link';
    EXECUTE IMMEDIATE 'create table AU_LOG_STATISTICS_XA as
      select * from AU_LOG_STATISTICS@region_au_db_link';
  
  end;

  /*合并其它（任务信息等），并且去重*/
  PROCEDURE do_merge_other is
  begin
    --AU_TASK_INFO
    EXECUTE IMMEDIATE 'insert into AU_TASK_INFO
      select * from AU_TASK_INFO_XA where state <> 2';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_TASK_INFO a
     where a.state <> 2 and a.rowid != (select max(b.rowid)
                         from AU_TASK_INFO b
                        where b.state <> 2
                          and a.imp_task_id = b.imp_task_id
                          and EQUAL_UTILS.equal(a.category,b.category)=1)';
    --AU_DATA_STATISTICS
    EXECUTE IMMEDIATE 'insert into AU_DATA_STATISTICS
      select * from AU_DATA_STATISTICS_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from au_data_statistics
 where rowid not in (select max(b.rowid)
                       from AU_DATA_STATISTICS b
                      group by b.task_id,
                               b.data_category,
                               b.statis_type,
                               b.mark_type,
                               b.mesh_id,
                               b.state)';
    --AU_LOG_STATISTICS
    EXECUTE IMMEDIATE 'insert into AU_LOG_STATISTICS
      select * from AU_LOG_STATISTICS_XA';
    --去重  
    EXECUTE IMMEDIATE 'delete from AU_LOG_STATISTICS a
     where a.rowid != (select max(b.rowid)
                         from AU_LOG_STATISTICS b
                        where 1 = 1
                          and a.task_id = b.task_id
                          and EQUAL_UTILS.equal(a.data_category,b.data_category)=1
                          and a.log_category = b.log_category)';
  
  end;

  /*合并道路，并且去重*/
  PROCEDURE do_merge_road is
  begin
    --AU_MARK
    EXECUTE IMMEDIATE 'insert into AU_MARK
      select *
        from AU_MARK_XA
       where mark_id in
             (select mark_id
                from AU_MARK_XA
              minus
              select mark_id
                from AU_MARK_XA
               where mark_id in (select mark_id from AU_MARK))';
    --AU_GPSRECORD
    EXECUTE IMMEDIATE 'insert into AU_GPSRECORD
      select *
        from AU_GPSRECORD_XA
       where GPSRECORD_ID in
             (select GPSRECORD_ID
                from AU_GPSRECORD_XA
              minus
              select GPSRECORD_ID
                from AU_GPSRECORD_XA
               where GPSRECORD_ID in (select GPSRECORD_ID from AU_GPSRECORD))';
  
    --AU_DRAFT
    EXECUTE IMMEDIATE 'insert into AU_DRAFT
      select * from AU_DRAFT_XA';
    EXECUTE IMMEDIATE 'delete from AU_DRAFT a
     where a.rowid != (select max(b.rowid)
                         from AU_DRAFT b
                        where 1 = 1
                          and a.mark_id = b.mark_id
                          and EQUAL_UTILS.equal(a.GEOMETRY,b.GEOMETRY)=1
                          and EQUAL_UTILS.equal(a.STYLE,b.STYLE)=1
                          and EQUAL_UTILS.equal(a.COLOR,b.COLOR)=1
                          and EQUAL_UTILS.equal(a.WIDTH,b.WIDTH)=1
                          and EQUAL_UTILS.equal(a.GEO_SEG,b.GEO_SEG)=1
                          and EQUAL_UTILS.equal(a.TYPE,b.TYPE)=1
                          and EQUAL_UTILS.equal(a.FIELD_SOURCE,b.FIELD_SOURCE)=1)';
    --AU_MARK_PHOTO
    EXECUTE IMMEDIATE 'insert into AU_MARK_PHOTO
      select * from AU_MARK_PHOTO_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_MARK_PHOTO a
     where a.rowid != (select max(b.rowid)
                         from AU_MARK_PHOTO b
                        where 1 = 1
                          and a.mark_id = b.mark_id
                          and a.photo_Id = b.photo_Id)';
    --AU_MARK_AUDIO
    EXECUTE IMMEDIATE 'insert into AU_MARK_AUDIO
      select * from AU_MARK_AUDIO_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_MARK_AUDIO a
     where a.rowid != (select max(b.rowid)
                         from AU_MARK_AUDIO b
                        where 1 = 1
                          and a.mark_id = b.mark_id
                          and a.AUDIO_ID = b.AUDIO_ID)';
    --AU_MARK_VIDEO
    EXECUTE IMMEDIATE 'insert into AU_MARK_VIDEO
      select * from AU_MARK_VIDEO_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_MARK_VIDEO a
     where a.rowid != (select max(b.rowid)
                         from AU_MARK_VIDEO b
                        where 1 = 1
                          and a.mark_id = b.mark_id
                          and a.video_Id = b.video_Id)';
    --AU_TOPOIMAGE
    EXECUTE IMMEDIATE 'insert into AU_TOPOIMAGE
      select *
        from AU_TOPOIMAGE_XA
       where IMAGE_ID in
             (select IMAGE_ID
                from AU_TOPOIMAGE_XA
              minus
              select IMAGE_ID
                from AU_TOPOIMAGE_XA
               where IMAGE_ID in (select IMAGE_ID from AU_TOPOIMAGE))';
  
    --AU_GPSTRACK
    EXECUTE IMMEDIATE 'insert into AU_GPSTRACK
      select *
        from AU_GPSTRACK_XA
       where GPSTRACK_ID in
             (select GPSTRACK_ID
                from AU_GPSTRACK_XA
              minus
              select GPSTRACK_ID
                from AU_GPSTRACK_XA
               where GPSTRACK_ID in (select GPSTRACK_ID from AU_GPSTRACK))';
  
    --AU_GPSTRACK_GROUP
    EXECUTE IMMEDIATE 'insert into AU_GPSTRACK_GROUP
      select *
        from AU_GPSTRACK_GROUP_XA
       where GROUP_ID in
             (select GROUP_ID
                from AU_GPSTRACK_GROUP_XA
              minus
              select GROUP_ID
                from AU_GPSTRACK_GROUP_XA
               where GROUP_ID in (select GROUP_ID from AU_GPSTRACK_GROUP))';
    --AU_GPSTRACK_GROUP_VIDEO
    EXECUTE IMMEDIATE 'insert into AU_GPSTRACK_GROUP_VIDEO
      select * from AU_GPSTRACK_GROUP_VIDEO_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_GPSTRACK_GROUP_VIDEO a
     where a.rowid != (select max(b.rowid)
                         from AU_GPSTRACK_GROUP_VIDEO b
                        where 1 = 1
                          and a.GROUP_ID = b.GROUP_ID
                          and a.VIDEO_ID = b.VIDEO_ID)';  
    --AU_GPSTRACK_PHOTO
    EXECUTE IMMEDIATE 'insert into AU_GPSTRACK_PHOTO
      select * from AU_GPSTRACK_PHOTO_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_GPSTRACK_PHOTO a
     where a.rowid != (select max(b.rowid)
                         from AU_GPSTRACK_PHOTO b
                        where 1 = 1
                          and a.GPSTRACK_ID = b.GPSTRACK_ID
                          and a.PHOTO_GUID = b.PHOTO_GUID)';  
    --AU_AUDIO
    EXECUTE IMMEDIATE 'insert into AU_AUDIO
      select *
        from AU_AUDIO_XA
       where AUDIO_ID in
             (select AUDIO_ID
                from AU_AUDIO_XA
              minus
              select AUDIO_ID
                from AU_AUDIO_XA
               where AUDIO_ID in (select AUDIO_ID from AU_AUDIO))';
    --AU_VIDEO
    EXECUTE IMMEDIATE 'insert into AU_VIDEO
      select *
        from AU_VIDEO_XA
       where VIDEO_ID in
             (select VIDEO_ID
                from AU_VIDEO_XA
              minus
              select VIDEO_ID
                from AU_VIDEO_XA
               where VIDEO_ID in (select VIDEO_ID from AU_VIDEO))';
    --AU_PHOTO
    EXECUTE IMMEDIATE 'insert into AU_PHOTO
      select *
        from AU_PHOTO_XA
       where PHOTO_ID in
             (select PHOTO_ID
                from AU_PHOTO_XA
              minus
              select PHOTO_ID
                from AU_PHOTO_XA
               where PHOTO_ID in (select PHOTO_ID from AU_PHOTO))';
  
    --AU_SERIESPHOTO
    EXECUTE IMMEDIATE 'insert into AU_SERIESPHOTO
      select *
        from AU_SERIESPHOTO_XA
       where PHOTO_ID in
             (select PHOTO_ID
                from AU_SERIESPHOTO_XA
              minus
              select PHOTO_ID
                from AU_SERIESPHOTO_XA
               where PHOTO_ID in (select PHOTO_ID from AU_SERIESPHOTO))';
  
  end;

  /*合并索引，并且去重*/
  PROCEDURE do_merge_index is
  begin
    --合并poi的数据，
    --1.au_ix_poi 
    EXECUTE IMMEDIATE 'insert into au_ix_poi
      select *
        from au_ix_poi_xa
       where audata_id in
             (select audata_id
                from au_ix_poi_xa
              minus
              select audata_id
                from au_ix_poi_xa
               where audata_id in (select audata_id from au_ix_poi))';
    --2.AU_IX_POI_ADDRESS 
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_ADDRESS
      select * from AU_IX_POI_ADDRESS_xa';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_ADDRESS a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_ADDRESS b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.NAME_ID = b.NAME_ID)';
    --3.AU_IX_POI_BUILDING_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_BUILDING
      select * from AU_IX_POI_BUILDING_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_BUILDING a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_BUILDING b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.POI_PID = b.POI_PID)';
    --4.AU_IX_POI_CHARGINGPLOT_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_CHARGINGPLOT
      select * 　from AU_IX_POI_CHARGINGPLOT_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_CHARGINGPLOT a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_CHARGINGPLOT b
                        where 1 = 1
                          and a.AUCHARG_Id = b.AUCHARG_Id
                          and a.CHARGING_ID = b.CHARGING_ID
                          and a.GROUP_ID = b.GROUP_ID
                          and a.COUNT = b.COUNT
                          and a.ACDC = b.ACDC
                          and EQUAL_UTILS.equal(a.PLUG_TYPE,b.PLUG_TYPE)=1
                          and EQUAL_UTILS.equal(a.POWER,b.POWER)=1
                          and EQUAL_UTILS.equal(a.VOLTAGE,b.VOLTAGE)=1
                          and EQUAL_UTILS.equal(a."CURRENT",b."CURRENT")=1
                          and a."MODE" = b."MODE")';
    --5.AU_IX_POI_CHARGINGSTATION_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_CHARGINGSTATION
      select *
        from AU_IX_POI_CHARGINGSTATION_XA
       where AUCHARG_Id in
             (select AUCHARG_Id
                from AU_IX_POI_CHARGINGSTATION_XA
              minus
              select AUCHARG_Id
                from AU_IX_POI_CHARGINGSTATION_XA
               where AUCHARG_Id in
                     (select AUCHARG_Id from AU_IX_POI_CHARGINGSTATION))';
    --6.AU_IX_POI_CHILDREN_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_CHILDREN
      select * from AU_IX_POI_CHILDREN_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_CHILDREN a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_CHILDREN b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.CHILD_POI_PID = b.CHILD_POI_PID
                          and a.GROUP_ID = b.GROUP_ID)';
    --7.AU_IX_POI_CONTACT_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_CONTACT
      select * from AU_IX_POI_CONTACT_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_CONTACT a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_CONTACT b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and EQUAL_UTILS.equal(a.CONTACT,b.CONTACT)=1
                          and a.CONTACT_TYPE = b.CONTACT_TYPE
                          and a.POI_PID = b.POI_PID)';
    --8.AU_IX_POI_FLAG_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_FLAG
      select * from AU_IX_POI_FLAG_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_FLAG a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_FLAG b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and EQUAL_UTILS.equal(a.FLAG_CODE,b.FLAG_CODE)=1
                          and a.POI_PID = b.POI_PID)';
    --9.AU_IX_POI_GASSTATION_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_GASSTATION
      select * from AU_IX_POI_GASSTATION_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_GASSTATION a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_GASSTATION b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.GASSTATION_ID = b.GASSTATION_ID
                          and a.POI_PID = b.POI_PID)';
  
    --10.AU_IX_POI_HOTEL_XA --NO DATASET
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POI_HOTEL
      SELECT * FROM AU_IX_POI_HOTEL_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_HOTEL a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_HOTEL b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.HOTEL_ID = b.HOTEL_ID
                          and a.POI_PID = b.POI_PID)';
    --11.AU_IX_POI_NAME_XA
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POI_NAME
      SELECT *
        FROM AU_IX_POI_NAME_XA
       WHERE auname_id IN
             (SELECT auname_id
                FROM AU_IX_POI_NAME_XA
              MINUS
              SELECT auname_id
                FROM AU_IX_POI_NAME_XA
               WHERE auname_id IN (SELECT auname_id FROM AU_IX_POI_NAME))';
    --12.AU_IX_POI_NAME_FLAG_XA
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POI_NAME_FLAG
      SELECT * FROM AU_IX_POI_NAME_FLAG_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_NAME_FLAG a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_NAME_FLAG b
                        where 1 = 1
                          and a.AUNAME_ID = b.AUNAME_ID
                          and a.NAME_ID = b.NAME_ID
                          and EQUAL_UTILS.equal(a.FLAG_CODE,b.FLAG_CODE)=1)';
    --13.AU_IX_POI_NAME_RP00_XA  
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POI_NAME_RP00
      SELECT *
        FROM AU_IX_POI_NAME_RP00_XA
       WHERE auname_id IN
             (SELECT auname_id
                FROM AU_IX_POI_NAME_RP00_XA
              MINUS
              SELECT auname_id
                FROM AU_IX_POI_NAME_RP00_XA
               WHERE auname_id IN
                     (SELECT auname_id FROM AU_IX_POI_NAME_RP00))';
  
    --14.AU_IX_POI_NOKIA_XA
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POI_NOKIA
      SELECT *
        FROM AU_IX_POI_NOKIA_XA
       WHERE AUDATA_ID IN
             (SELECT AUDATA_ID
                FROM AU_IX_POI_NOKIA_XA
              MINUS
              SELECT AUDATA_ID
                FROM AU_IX_POI_NOKIA_XA
               WHERE AUDATA_ID IN (SELECT AUDATA_ID FROM AU_IX_POI_NOKIA))';
  
    --15.AU_IX_POI_PARENT_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_PARENT
      select * 　from AU_IX_POI_PARENT_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_PARENT a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_PARENT b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.GROUP_ID = b.GROUP_ID)';
  
    --16.AU_IX_POI_PARKING_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_PARKING
      select * from AU_IX_POI_PARKING_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_PARKING a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_PARKING b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.PARKING_ID = b.PARKING_ID
                          and a.POI_PID = b.POI_PID)';
    --17.AU_IX_POI_PHOTO_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_PHOTO
      select * from AU_IX_POI_PHOTO_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_PHOTO a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_PHOTO b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.PHOTO_ID = b.PHOTO_ID
                          and a.POI_PID = b.POI_PID)';
    --18.AU_IX_POI_RESTAURANT_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_POI_RESTAURANT
      select * from AU_IX_POI_RESTAURANT_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POI_RESTAURANT a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POI_RESTAURANT b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.RESTAURANT_ID = b.RESTAURANT_ID)';
    --19.AU_IX_POI_RP00_XA
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POI_RP00
      SELECT *
        FROM AU_IX_POI_RP00_XA
       WHERE AUDATA_ID IN
             (SELECT AUDATA_ID
                FROM AU_IX_POI_RP00_XA
              MINUS
              SELECT AUDATA_ID
                FROM AU_IX_POI_RP00_XA
               WHERE AUDATA_ID IN (SELECT AUDATA_ID FROM AU_IX_POI_RP00))';
  
    --20.AU_IX_SAMEPOI_XA
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_SAMEPOI
      SELECT *
        FROM AU_IX_SAMEPOI_XA
       WHERE SAMEPOI_AUDATA_ID IN
             (SELECT SAMEPOI_AUDATA_ID
                FROM AU_IX_SAMEPOI_XA
              MINUS
              SELECT SAMEPOI_AUDATA_ID
                FROM AU_IX_SAMEPOI_XA
               WHERE SAMEPOI_AUDATA_ID IN
                     (SELECT SAMEPOI_AUDATA_ID FROM AU_IX_SAMEPOI))';
  
    --21.AU_IX_SAMEPOI_PART_XA
    EXECUTE IMMEDIATE 'insert into AU_IX_SAMEPOI_PART
      select * from AU_IX_SAMEPOI_PART_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_SAMEPOI_PART a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_SAMEPOI_PART b
                        where 1 = 1
                          and a.Audata_Id = b.Audata_Id
                          and a.SAMEPOI_AUDATA_ID = b.SAMEPOI_AUDATA_ID
                          and a.GROUP_ID = b.GROUP_ID
                          and a.POI_PID = b.POI_PID)';
  
    --点门牌
    --AU_IX_POINTADDRESS
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POINTADDRESS
      SELECT *
        FROM AU_IX_POINTADDRESS_XA
       WHERE AUDATA_ID IN
             (SELECT AUDATA_ID
                FROM AU_IX_POINTADDRESS_XA
              MINUS
              SELECT AUDATA_ID
                FROM AU_IX_POINTADDRESS_XA
               WHERE AUDATA_ID IN (SELECT AUDATA_ID FROM AU_IX_POINTADDRESS))';
  
    --AU_IX_POINTADDRESS_CHILDREN
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POINTADDRESS_CHILDREN
      SELECT * FROM AU_IX_POINTADDRESS_CHILDREN_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POINTADDRESS_CHILDREN a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POINTADDRESS_CHILDREN b
                        where 1 = 1
                          and a.audata_id = b.audata_id
                          and a.CHILD_PA_PID = b.CHILD_PA_PID
                          and a.GROUP_ID = b.GROUP_ID)';
    --AU_IX_POINTADDRESS_FLAG
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POINTADDRESS_FLAG
      SELECT * FROM AU_IX_POINTADDRESS_FLAG_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POINTADDRESS_FLAG a
     where a.rowid !=
           (select max(b.rowid)
              from AU_IX_POINTADDRESS_FLAG b
             where 1 = 1
               and a.audata_id = b.audata_id
               and a.POINTADDRESS_PID = b.POINTADDRESS_PID
               and EQUAL_UTILS.equal(a.FLAG_CODE,b.FLAG_CODE)=1)';
  
    --AU_IX_POINTADDRESS_NAME
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POINTADDRESS_NAME
      SELECT * FROM AU_IX_POINTADDRESS_NAME_XA';
    --去重
    EXECUTE IMMEDIATE 'delete from AU_IX_POINTADDRESS_NAME a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POINTADDRESS_NAME b
                        where 1 = 1
                          and a.audata_id = b.audata_id
                          and a.NAME_ID = b.NAME_ID)';
    --AU_IX_POINTADDRESS_PARENT
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_POINTADDRESS_PARENT
      SELECT * FROM AU_IX_POINTADDRESS_PARENT_XA';
    --dup delete
    EXECUTE IMMEDIATE 'delete from AU_IX_POINTADDRESS_PARENT a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_POINTADDRESS_PARENT b
                        where 1 = 1
                          and a.audata_id = b.audata_id
                          and a.GROUP_ID = b.GROUP_ID)';
    --文字
    --AU_IX_ANNOTATION
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_ANNOTATION
      SELECT *
        FROM AU_IX_ANNOTATION_XA
       WHERE AUDATA_ID IN
             (SELECT AUDATA_ID
                FROM AU_IX_ANNOTATION_XA
              MINUS
              SELECT AUDATA_ID
                FROM AU_IX_ANNOTATION_XA
               WHERE AUDATA_ID IN (SELECT AUDATA_ID FROM AU_IX_ANNOTATION))';
  
    --AU_IX_ANNOTATION_NAME
    EXECUTE IMMEDIATE 'INSERT INTO AU_IX_ANNOTATION_NAME
      SELECT * FROM AU_IX_ANNOTATION_NAME_XA';
    --dup delete
    EXECUTE IMMEDIATE 'Delete from AU_IX_ANNOTATION_NAME a
     where a.rowid != (select max(b.rowid)
                         from AU_IX_ANNOTATION_NAME b
                        where 1 = 1
                          and a.audata_id = b.audata_id
                          and a.NAME_ID = b.NAME_ID)';
  end;

  /*
  将给定的数据库的相关数据合并到当前的数据库中
  合并方式：all 全要素；
  */
  PROCEDURE merge_all(v_ip          varchar2,
                      v_port        number,
                      v_sid         varchar2,
                      v_schema_user varchar2,
                      v_schema_pwd  varchar2) is
  begin
    --创建dblink
    create_region_au_db_link(v_ip,
                             
                             v_port,
                             v_sid,
                             v_schema_user,
                             v_schema_pwd);
    --创建临时表                         
    create_road_temptable();
    create_index_temptable();
    create_other_temptable();
    --合并道路
    do_merge_road();
    --合并索引
    do_merge_index();
    --合并其它
    do_merge_other();
  
  end;

  /*
  将给定的数据库的相关数据合并到当前的数据库中
  合并方式：road 道路；
  */
  PROCEDURE merge_road(v_ip          varchar2,
                       v_port        number,
                       v_sid         varchar2,
                       v_schema_user varchar2,
                       v_schema_pwd  varchar2) is
  begin
    --创建dblink
    create_region_au_db_link(v_ip,
                             v_port,
                             v_sid,
                             v_schema_user,
                             v_schema_pwd);
    --创建临时表                         
    create_road_temptable();
    create_other_temptable();
    --合并道路
    do_merge_road();
    --合并其它
    do_merge_other();
  
  end;

  /*
  将给定的数据库的相关数据合并到当前的数据库中
  合并方式：index 索引；
  */
  PROCEDURE merge_index(v_ip          varchar2,
                        v_port        number,
                        v_sid         varchar2,
                        v_schema_user varchar2,
                        v_schema_pwd  varchar2) is
  begin
    --创建dblink
    create_region_au_db_link(v_ip,
                             v_port,
                             v_sid,
                             v_schema_user,
                             v_schema_pwd);
    --创建临时表                         
    create_index_temptable();
    create_other_temptable();
    --合并索引
    do_merge_index();
    --合并其它
    do_merge_other();
  
  end;
END region_au_merge;
/
