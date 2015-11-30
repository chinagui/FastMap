create or replace package PK_FILTER_SENSITIVE_ESTAB authid current_user
is
  /*
  用于常规作业成果库创建时过虑掉敏感POI及其关联的文字
  1)	创建成果库时，标识为敏感设施的POI数据不提取，同时维护POI的父子关系、同一关系
  2)	文字中来源为POI的，如果POI删除了文字也要删除。
  */
  procedure filter;
  
  --110000010000  敏感设施 
  g_sensitive_poi_flag constant varchar2(20) := '110000010000';
  
  v_log varchar2(4000);
  v_index pls_integer;
  
  function get_log
  return varchar2;
    
end PK_FILTER_SENSITIVE_ESTAB;
/
create or replace package body PK_FILTER_SENSITIVE_ESTAB
is

   procedure write_log(p_msg varchar2)
   is
   begin
        v_log := v_log||v_index||':'||p_msg||';';        
        v_index := v_index + 1;
    exception when others then                  
    null;
   end;
   
  function get_log
  return varchar2
  is
  begin
       return v_log;
  end;

   
   
   procedure create_temp_table
   is
     v_sql varchar2(1000);
     pragma autonomous_transaction;
     v_count pls_integer;
   begin
        select count(1) into v_count from user_all_tables 
        where table_name = 'TEMP_SENSITIVE_ESTAB';
        if v_count = 0
        then
            write_log('没有临时表，创建临时表');
            v_sql := 'create global temporary table temp_sensitive_estab
            (
              PID number(10) not null,
              constraint PK_TEMP_SEN_ESTAB primary key (PID)
            )
            on commit delete rows';
            execute immediate  v_sql; 
        else
            write_log('临时表已经存在');
        end if;
   exception when others then
       write_log('创建临时表失败');
      raise_application_error(-20999, '过虑敏感POI中，创建临时表时发生异常:'||sqlcode||'，'||sqlerrm||'，'||dbms_utility.format_error_backtrace(), true);                   
   end;
   
   procedure prepare_sensitive_poi
   is
   begin
        execute immediate 'insert into temp_sensitive_estab
        select distinct poi_pid 
        from ix_poi_flag g
        where g.flag_code =:1' using g_sensitive_poi_flag;
        write_log('发现了'||sql%rowcount||'条敏感POI');
   end;
   
   procedure delete_poi_normal_table
   (
     p_table_name varchar2
   )
   is
   begin
       execute immediate 'delete from '||p_table_name||' f 
       where exists
       (
             select 1 from temp_sensitive_estab t where t.pid = f.poi_pid 
       )';  
       write_log('在'||p_table_name||'中删除了'||sql%rowcount||'记录');     
   end;  
   

   
   
  /* 
    过虑敏感POI
    IX_POI
    IX_POI_ADDRESS
    IX_POI_ADVERTISEMENT
    IX_POI_ATTRACTION
    IX_POI_AUDIO
    IX_POI_BUSINESSTIME
    IX_POI_CHARGINGPLOT
    IX_POI_CHARGINGSTATION
    IX_POI_CHILDREN
    IX_POI_CONTACT
    IX_POI_ENTRYIMAGE
    IX_POI_FLAG
    IX_POI_GASSTATION
    IX_POI_HOTEL
    IX_POI_ICON
    IX_POI_INTRODUCTION
    IX_POI_NAME
    IX_POI_NAME_TONE
    IX_POI_PARENT
    IX_POI_PHOTO
    IX_POI_RESTAURANT
    IX_POI_VIDEO
    
    IX_SAMEPOI
    IX_SAMEPOI_PART
    
    IX_POI_FLAG 索引:POI标识信息表,记录 POIFLAG,名称来源,地址确认标识等信息
    标识代码  FLAG_CODE  VARCHAR2(12)  参考"M_FLAG_CODE" 
             POI  记录级  110000010000  敏感设施 
             
    注意IX_POI与IX_POI_FLAG为一对多的关系
  */
  procedure filter_poi
  is
  begin
      --删除计算父子关系
      execute immediate 'delete from ix_poi_children c
      where exists
      (
        select 1 from temp_sensitive_estab t
        where t.pid = c.child_poi_pid
      )';
      write_log('在ix_poi_children中删除了'||sql%rowcount||'记录');
      execute immediate 'delete from ix_poi_children c
      where exists
      (
      select 1 from temp_sensitive_estab t,ix_poi_parent p
      where t.pid = p.parent_poi_pid
      and p.group_id = c.group_id
      )';
      write_log('在ix_poi_children中删除了'||sql%rowcount||'记录');
      execute immediate 'delete from ix_poi_parent p
      where exists
      (
        select 1 from temp_sensitive_estab t
        where t.pid = p.parent_poi_pid
      )';
      write_log('在ix_poi_parent中删除了'||sql%rowcount||'记录');
      --有父没有子的全删掉
      delete from ix_poi_parent p
      where not exists
      (
            select 1 from ix_poi_children c
            where c.group_id = p.group_id
      );
      write_log('在ix_poi_parent中删除了'||sql%rowcount||'记录');
      --删除同一关系
      --如果同一poi关系小于3个则将主表删掉
      execute immediate 'delete from ix_samepoi p
      where exists
      (
        select 1 
        from ix_samepoi_part c   
        where c.group_id = p.group_id 
        and exists
        (
           select 1 from ix_samepoi_part ci, temp_sensitive_estab t  
           where ci.group_id = c.group_id 
           and ci.poi_pid = t.pid
        )
        group by c.group_id
        having count(1) < 3
      )';
      write_log('在ix_samepoi中删除了'||sql%rowcount||'记录');
      --同一poi关系小于3个的子全部删掉
      execute immediate 'delete from ix_samepoi_part co
      where exists
      (
        select 1 
        from ix_samepoi_part c   
        where c.group_id = co.group_id 
        and exists
        (
           select 1 from ix_samepoi_part ci, temp_sensitive_estab t  
           where ci.group_id = c.group_id 
           and ci.poi_pid = t.pid
        )
        group by c.group_id
        having count(1) < 3
      ) ';
      write_log('在ix_samepoi_part中删除了'||sql%rowcount||'记录');
      --删除能关联上的子
      execute immediate 'delete from ix_samepoi_part c
      where exists
      (
        select 1 from temp_sensitive_estab t
        where t.pid = c.poi_pid
      ) ';
      write_log('在ix_samepoi_part中删除了'||sql%rowcount||'记录');
      --有父没有子的全删掉
      delete from ix_samepoi p
      where not exists
      (
            select 1 from ix_samepoi_part c
            where c.group_id = p.group_id
      );
      write_log('在ix_samepoi中删除了'||sql%rowcount||'记录');
      --删除poi
      --IX_POI_ADDRESS
      delete_poi_normal_table('IX_POI_ADDRESS');
      --IX_POI_ADVERTISEMENT
      delete_poi_normal_table('IX_POI_ADVERTISEMENT');
      --IX_POI_ATTRACTION
      delete_poi_normal_table('IX_POI_ATTRACTION');
      --IX_POI_AUDIO
      delete_poi_normal_table('IX_POI_AUDIO');
      --IX_POI_BUSINESSTIME
      delete_poi_normal_table('IX_POI_BUSINESSTIME');
      
      --IX_POI_CHARGINGPLOT
      --delete_poi_normal_table('IX_POI_CHARGINGPLOT');
      execute immediate 'delete from IX_POI_CHARGINGPLOT p
      where 
      exists
      (
      select 1 from temp_sensitive_estab t,IX_POI_CHARGINGSTATION c
      where t.pid = c.poi_pid
      and c.charging_id = p.charging_id
      )';
      write_log('在IX_POI_CHARGINGPLOT中删除了'||sql%rowcount||'记录');
      --IX_POI_CHARGINGSTATION
      execute immediate 'delete from IX_POI_CHARGINGSTATION c
      where exists
      (
      select 1 from temp_sensitive_estab t
      where t.pid = c.poi_pid
      )';
      write_log('在IX_POI_CHARGINGSTATION中删除了'||sql%rowcount||'记录');
      --delete_poi_normal_table('IX_POI_CHARGINGSTATION');
      
      --IX_POI_CONTACT
      delete_poi_normal_table('IX_POI_CONTACT');
      --IX_POI_ENTRYIMAGE
      delete_poi_normal_table('IX_POI_ENTRYIMAGE');
      --IX_POI_FLAG
      delete_poi_normal_table('IX_POI_FLAG');
      --IX_POI_GASSTATION
      delete_poi_normal_table('IX_POI_GASSTATION');
      --IX_POI_HOTEL
      delete_poi_normal_table('IX_POI_HOTEL');
      --IX_POI_ICON
      delete_poi_normal_table('IX_POI_ICON');
      --IX_POI_INTRODUCTION
      delete_poi_normal_table('IX_POI_INTRODUCTION');
      --IX_POI_NAME
      delete_poi_normal_table('IX_POI_NAME');
      --IX_POI_NAME_TONE
      execute immediate 'delete from ix_poi_name_tone nt
      where exists
      (
      select 1 from temp_sensitive_estab t,ix_poi_name n
      where t.pid = n.poi_pid
      and n.name_id = nt.name_id
      )';
      write_log('在ix_poi_name_tone中删除了'||sql%rowcount||'记录');
      --delete_poi_normal_table('IX_POI_NAME_TONE');
      --IX_POI_PHOTO
      delete_poi_normal_table('IX_POI_PHOTO');
      --IX_POI_RESTAURANT
      delete_poi_normal_table('IX_POI_RESTAURANT');
      --IX_POI_VIDEO
      delete_poi_normal_table('IX_POI_VIDEO');      
      execute immediate 'delete from IX_POI f 
      where exists
      (
           select 1 from temp_sensitive_estab t where t.pid = f.pid 
      )';
      write_log('在IX_POI中删除了'||sql%rowcount||'记录');
  end;

  procedure delete_annotation_table(p_table_name varchar2)
  is
  begin
       execute immediate 'delete from '||p_table_name||' f 
       where exists
       (
             select 1 from temp_sensitive_estab t where t.pid = f.pid 
       )';
       write_log('在'||p_table_name||'中删除了'||sql%rowcount||'记录');
  end;  
  
  /*
    过虑敏感POI关联的文字
    IX_ANNOTATION
    IX_ANNOTATION_FLAG
    IX_ANNOTATION_NAME

    IX_ANNOTATION表中关于文字来源的说明
    来源标记  SRC_FLAG  NUMBER(1)  0  未调查 
    1  道路名 
    2  Hamlet 
    3  旅游图 
    4  文字
    5  POI 
    注:该字段仅用于 2.5 和 20 万数据,百万和 TOP 级
    数据不需要   
    
    来源 PID  SRC_PID  NUMBER(10)  文字来源的数据 ID,如来自 POI则为 POI的 PID 
    来自道路名则为道路名 ID 
    注:该字段仅用于 2.5 和 20 万数据,百万和 TOP 级
    数据不需要
  */
  procedure filter_annotation
  is
  begin
       /*delete_annotation_table('IX_ANNOTATION_FLAG');
       delete_annotation_table('IX_ANNOTATION_NAME');
       delete_annotation_table('IX_ANNOTATION');*/
       execute immediate 'delete from IX_ANNOTATION_NAME a
        where exists
        (
        select  1 from temp_sensitive_estab t,IX_ANNOTATION ai
        where t.pid = ai.src_pid
        and ai.pid = a.pid
        and ai.src_flag = 5
        )';
       execute immediate 'delete from IX_ANNOTATION_FLAG a
        where exists
        (
        select  1 from temp_sensitive_estab t,IX_ANNOTATION ai
        where t.pid = ai.src_pid
        and ai.pid = a.pid
        and ai.src_flag = 5
        )';
       execute immediate 'delete from IX_ANNOTATION a
        where exists
        (
        select 1 from temp_sensitive_estab t
        where t.pid = a.src_pid
        )
        and a.src_flag = 5';               
  end;
  
  /*
  用于常规作业成果库创建时过虑掉敏感POI及其关联的文字
  1)	创建成果库时，标识为敏感设施的POI数据不提取，同时维护POI的父子关系、同一关系
  2)	文字中来源为POI的，如果POI删除了文字也要删除。
  */
  procedure filter
  is
    v_count pls_integer;
  begin
        v_log := '';
        v_index := 1;
        --注意执行顺序,先过虑文字，再过虑POI
        create_temp_table;
        prepare_sensitive_poi;
        execute immediate 'select count(1) from TEMP_SENSITIVE_ESTAB' into v_count;
        if v_count > 0
        then
            filter_annotation;
            filter_poi;  
        end if;
  end;  
  
  
     
end PK_FILTER_SENSITIVE_ESTAB;
/
