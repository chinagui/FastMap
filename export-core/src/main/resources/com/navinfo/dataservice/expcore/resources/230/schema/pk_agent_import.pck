create or replace package pk_agent_import
is
  /*
       poi的状态：0无，1删除，2修改，3新增
       task_id：
  */
  v_task_id_col constant varchar2(50) := 'task_id';

  procedure del_poi_main
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure add_poi_main
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure del_poi_name
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure add_poi_name
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure del_poi_address
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure add_poi_address
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure del_poi_contact
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure add_poi_contact
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
  procedure reset_status
  (
     p_task_id varchar2,
     p_dblink varchar2
  );
end pk_agent_import;
/
create or replace package body pk_agent_import
is
  /*
       poi的状态：0无，1删除，2修改，3新增
       p_task_id:代理店作业中分的小任务号
       p_dblink:为母库指向代理店作业子版本的dblink
       代理店的入库不产生履历，采用以代理店作业为准的方式，将母库中的数据删掉，再用代码店的作业数据覆盖
  */
  procedure del_poi_main
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'delete from ix_poi p
        where exists
        (
          select 1 from ix_poi@'||p_dblink||' s
          where s.pid = p.pid
          and s.task_id = :1
          and (s.state = 1 or s.state = 2 or s.state = 3)
        )';
       execute immediate v_sql using p_task_id;
       logger.trace(p_task_id||'任务，删除POI主表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure add_poi_main
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
    tempTable varchar2(35);
    tempCreateSql varchar2(32767);
  begin
	 /*
       v_sql := 'insert into ix_poi
       (pid, kind_code, geometry, x_guide, y_guide, link_pid, side, name_groupid, road_flag, pmesh_id, 
       admin_real, importance, chain, airport_code, access_flag, open_24h, mesh_id_5k, mesh_id, region_id, 
       post_code, edit_flag, state, field_state, label, type, address_flag, ex_priority, edition_flag, 
       poi_memo, old_blockcode, old_name, old_address, old_kind, poi_num, log, task_id, data_version, 
       field_task_id, u_record, u_fields, dif_groupid, reserved)
        select pid, kind_code, geometry, x_guide, y_guide, link_pid, side, name_groupid, road_flag, pmesh_id, 
       admin_real, importance, chain, airport_code, access_flag, open_24h, mesh_id_5k, mesh_id, region_id, 
       post_code, edit_flag, state, field_state, label, type, address_flag, ex_priority, edition_flag, 
       poi_memo, old_blockcode, old_name, old_address, old_kind, poi_num, log, task_id, data_version, 
       field_task_id, u_record, u_fields, dif_groupid, reserved 
       from ix_poi@'||p_dblink||' p
        where p.task_id = :1
        and (p.state = 1 or p.state = 2 or p.state = 3)';
	*/
	
  
   --生成临时表
    tempTable := substr(sys_guid(),1,30);
    tempCreateSql := 'create table '||tempTable||' as select * from ix_poi@'||p_dblink;
    execute immediate tempCreateSql ;
    
  
       v_sql := 'MERGE INTO ix_poi p1
		USING (select * from '||tempTable||' where task_id = :1)  p2
		ON (

		p1.POI_NUM = p2.POI_NUM

		)
		WHEN MATCHED THEN
		  UPDATE
		     SET p1.KIND_CODE = p2.KIND_CODE , p1.CHAIN = p2.CHAIN ,
					p1.POST_CODE = p2.POST_CODE ,
					p1.STATE = p2.STATE where p2.PID = p1.PID
		WHEN NOT MATCHED THEN
		  INSERT
		    (pid,
		     kind_code,
		     geometry,
		     x_guide,
		     y_guide,
		     link_pid,
		     side,
		     name_groupid,
		     road_flag,
		     pmesh_id,
		     admin_real,
		     importance,
		     chain,
		     airport_code,
		     access_flag,
		     open_24h,
		     mesh_id_5k,
		     mesh_id,
		     region_id,
		     post_code,
		     edit_flag,
		     state,
		     field_state,
		     label,
		     type,
		     address_flag,
		     ex_priority,
		     edition_flag,
		     poi_memo,
		     old_blockcode,
		     old_name,
		     old_address,
		     old_kind,
		     poi_num,
		     log,
		     task_id,
		     data_version,
		     field_task_id,
		     u_record,
		     u_fields,
		     dif_groupid,
		     reserved)
		  VALUES
		    (p2.pid,
		     p2.kind_code,
		     p2.geometry,
		     p2.x_guide,
		     p2.y_guide,
		     p2.link_pid,
		     p2.side,
		     p2.name_groupid,
		     p2.road_flag,
		     p2.pmesh_id,
		     p2.admin_real,
		     p2.importance,
		     p2.chain,
		     p2.airport_code,
		     p2.access_flag,
		     p2.open_24h,
		     p2.mesh_id_5k,
		     p2.mesh_id,
		     p2.region_id,
		     p2.post_code,
		     p2.edit_flag,
		     p2.state,
		     p2.field_state,
		     p2.label,
		     p2.type,
		     p2.address_flag,
		     p2.ex_priority,
		     p2.edition_flag,
		     p2.poi_memo,
		     p2.old_blockcode,
		     p2.old_name,
		     p2.old_address,
		     p2.old_kind,
		     p2.poi_num,
		     p2.log,
		     p2.task_id,
		     p2.data_version,
		     p2.field_task_id,
		     p2.u_record,
		     p2.u_fields,
		     p2.dif_groupid,
		     p2.reserved) where p2.PID not in (select PID from ix_poi)';



       execute immediate v_sql using p_task_id;
       logger.trace(p_task_id||'任务，插入POI主表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
       
       --删除临时表
       tempCreateSql := 'drop table '||tempTable;
       execute immediate tempCreateSql ;
  end;

  procedure del_poi_name
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'delete from ix_poi_name n
        where n.poi_pid in
        (
        
       select pid
         from ix_poi@'||p_dblink||' p
        where task_id = :1
          and (exists (select 1
                         from ix_poi p1
                        where p1.poi_num = p.poi_num
                          and p1.PID = p.PID) or
               (p.poi_num not in (select poi_num from ix_poi) and
               p.pid not in (select pid from ix_poi)))  
     
        )';
        execute immediate v_sql using p_task_id;
        logger.trace(p_task_id||'任务，删除POI名称表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure add_poi_name
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
     v_sql varchar2(4000);
  begin
       v_sql := 'insert into ix_poi_name
       (name_id, name_groupid, poi_pid, lang_code, name_class, name, name_phonetic, keywords, nidb_pid, u_record, u_fields, name_type)
        select name_id, name_groupid, poi_pid, lang_code, name_class, name, name_phonetic, 
        keywords, nidb_pid, u_record, u_fields, name_type 
        from ix_poi_name@'||p_dblink||' n
        where n.poi_pid in
        (
         select pid
         from ix_poi@'||p_dblink||' p
        where task_id = :1
          and (exists (select 1
                         from ix_poi p1
                        where p1.poi_num = p.poi_num
                          and p1.PID = p.PID) or
               (p.poi_num not in (select poi_num from ix_poi) and
               p.pid not in (select pid from ix_poi)))  
        )';
        execute immediate v_sql using p_task_id;
        logger.trace(p_task_id||'任务，插入POI名称表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure del_poi_address
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'delete from ix_poi_address n
         where n.poi_pid in
        (
         select pid
         from ix_poi@'||p_dblink||' p
        where task_id = :1
          and (exists (select 1
                         from ix_poi p1
                        where p1.poi_num = p.poi_num
                          and p1.PID = p.PID) or
               (p.poi_num not in (select poi_num from ix_poi) and
               p.pid not in (select pid from ix_poi)))  
        )';
        execute immediate v_sql using p_task_id;
        logger.trace(p_task_id||'任务，删除POI地址表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure add_poi_address
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'insert into ix_poi_address
       (name_id, name_groupid, poi_pid, lang_code, src_flag, fullname, fullname_phonetic, roadname, roadname_phonetic, addrname, 
       addrname_phonetic, province, city, county, town, street, place, landmark, prefix, housenum, type, subnum, surfix, estab, building, 
       floor, unit, room, addons, prov_phonetic, city_phonetic, county_phonetic, town_phonetic, street_phonetic, place_phonetic, 
       landmark_phonetic, prefix_phonetic, housenum_phonetic, type_phonetic, subnum_phonetic, surfix_phonetic, estab_phonetic, 
       building_phonetic, floor_phonetic, unit_phonetic, room_phonetic, addons_phonetic, u_record, u_fields)
        select
        name_id, name_groupid, poi_pid, lang_code, src_flag, fullname, fullname_phonetic, roadname, roadname_phonetic, addrname, 
       addrname_phonetic, province, city, county, town, street, place, landmark, prefix, housenum, type, subnum, surfix, estab, building, 
       floor, unit, room, addons, prov_phonetic, city_phonetic, county_phonetic, town_phonetic, street_phonetic, place_phonetic, 
       landmark_phonetic, prefix_phonetic, housenum_phonetic, type_phonetic, subnum_phonetic, surfix_phonetic, estab_phonetic, 
       building_phonetic, floor_phonetic, unit_phonetic, room_phonetic, addons_phonetic, u_record, u_fields  
        from ix_poi_address@'||p_dblink||' n
        where n.poi_pid in
        (
         select pid
         from ix_poi@'||p_dblink||' p
        where task_id = :1
          and (exists (select 1
                         from ix_poi p1
                        where p1.poi_num = p.poi_num
                          and p1.PID = p.PID) or
               (p.poi_num not in (select poi_num from ix_poi) and
               p.pid not in (select pid from ix_poi)))  
        )';
        execute immediate v_sql using p_task_id;
        logger.trace(p_task_id||'任务，插入POI地址表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure del_poi_contact
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'delete from ix_poi_contact n
         where n.poi_pid in
        (
         select pid
         from ix_poi@'||p_dblink||' p
        where task_id = :1
          and (exists (select 1
                         from ix_poi p1
                        where p1.poi_num = p.poi_num
                          and p1.PID = p.PID) or
               (p.poi_num not in (select poi_num from ix_poi) and
               p.pid not in (select pid from ix_poi)))  
        )';
        execute immediate v_sql using p_task_id;
        logger.trace(p_task_id||'任务，删除POI地址表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure add_poi_contact
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
    v_sql varchar2(4000);
  begin
       v_sql := 'insert into ix_poi_contact
       (poi_pid, contact_type, contact, contact_depart, priority, u_record, u_fields)
        select
        poi_pid, contact_type, contact, contact_depart, priority, u_record, u_fields  
        from ix_poi_contact@'||p_dblink||' n
         where n.poi_pid in
        (
         select pid
         from ix_poi@'||p_dblink||' p
        where task_id = :1
          and (exists (select 1
                         from ix_poi p1
                        where p1.poi_num = p.poi_num
                          and p1.PID = p.PID) or
               (p.poi_num not in (select poi_num from ix_poi) and
               p.pid not in (select pid from ix_poi)))  
        )';
        execute immediate v_sql using p_task_id;
        logger.trace(p_task_id||'任务，插入POI联系方式表数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

  procedure reset_status
  (
     p_task_id varchar2,
     p_dblink varchar2
  )
  is
  begin
       execute immediate 'update ix_poi p set p.state = 0 where p.task_id = :1 and (p.state = 1 or p.state = 2 or p.state = 3)'
       using p_task_id;
       logger.trace(p_task_id||'任务，重置状态数量为：'||sql%rowcount,'代理店入库：'||p_task_id);
  end;

end pk_agent_import;
/
