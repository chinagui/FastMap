create or replace package PK_FILTER_SENSITIVE_ESTAB authid current_user
is
  /*
  ���ڳ�����ҵ�ɹ��ⴴ��ʱ���ǵ�����POI�������������
  1)	�����ɹ���ʱ����ʶΪ������ʩ��POI���ݲ���ȡ��ͬʱά��POI�ĸ��ӹ�ϵ��ͬһ��ϵ
  2)	��������ԴΪPOI�ģ����POIɾ��������ҲҪɾ����
  */
  procedure filter;
  
  --110000010000  ������ʩ 
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
            write_log('û����ʱ��������ʱ��');
            v_sql := 'create global temporary table temp_sensitive_estab
            (
              PID number(10) not null,
              constraint PK_TEMP_SEN_ESTAB primary key (PID)
            )
            on commit delete rows';
            execute immediate  v_sql; 
        else
            write_log('��ʱ���Ѿ�����');
        end if;
   exception when others then
       write_log('������ʱ��ʧ��');
      raise_application_error(-20999, '��������POI�У�������ʱ��ʱ�����쳣:'||sqlcode||'��'||sqlerrm||'��'||dbms_utility.format_error_backtrace(), true);                   
   end;
   
   procedure prepare_sensitive_poi
   is
   begin
        execute immediate 'insert into temp_sensitive_estab
        select distinct poi_pid 
        from ix_poi_flag g
        where g.flag_code =:1' using g_sensitive_poi_flag;
        write_log('������'||sql%rowcount||'������POI');
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
       write_log('��'||p_table_name||'��ɾ����'||sql%rowcount||'��¼');     
   end;  
   

   
   
  /* 
    ��������POI
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
    
    IX_POI_FLAG ����:POI��ʶ��Ϣ��,��¼ POIFLAG,������Դ,��ַȷ�ϱ�ʶ����Ϣ
    ��ʶ����  FLAG_CODE  VARCHAR2(12)  �ο�"M_FLAG_CODE" 
             POI  ��¼��  110000010000  ������ʩ 
             
    ע��IX_POI��IX_POI_FLAGΪһ�Զ�Ĺ�ϵ
  */
  procedure filter_poi
  is
  begin
      --ɾ�����㸸�ӹ�ϵ
      execute immediate 'delete from ix_poi_children c
      where exists
      (
        select 1 from temp_sensitive_estab t
        where t.pid = c.child_poi_pid
      )';
      write_log('��ix_poi_children��ɾ����'||sql%rowcount||'��¼');
      execute immediate 'delete from ix_poi_children c
      where exists
      (
      select 1 from temp_sensitive_estab t,ix_poi_parent p
      where t.pid = p.parent_poi_pid
      and p.group_id = c.group_id
      )';
      write_log('��ix_poi_children��ɾ����'||sql%rowcount||'��¼');
      execute immediate 'delete from ix_poi_parent p
      where exists
      (
        select 1 from temp_sensitive_estab t
        where t.pid = p.parent_poi_pid
      )';
      write_log('��ix_poi_parent��ɾ����'||sql%rowcount||'��¼');
      --�и�û���ӵ�ȫɾ��
      delete from ix_poi_parent p
      where not exists
      (
            select 1 from ix_poi_children c
            where c.group_id = p.group_id
      );
      write_log('��ix_poi_parent��ɾ����'||sql%rowcount||'��¼');
      --ɾ��ͬһ��ϵ
      --���ͬһpoi��ϵС��3��������ɾ��
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
      write_log('��ix_samepoi��ɾ����'||sql%rowcount||'��¼');
      --ͬһpoi��ϵС��3������ȫ��ɾ��
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
      write_log('��ix_samepoi_part��ɾ����'||sql%rowcount||'��¼');
      --ɾ���ܹ����ϵ���
      execute immediate 'delete from ix_samepoi_part c
      where exists
      (
        select 1 from temp_sensitive_estab t
        where t.pid = c.poi_pid
      ) ';
      write_log('��ix_samepoi_part��ɾ����'||sql%rowcount||'��¼');
      --�и�û���ӵ�ȫɾ��
      delete from ix_samepoi p
      where not exists
      (
            select 1 from ix_samepoi_part c
            where c.group_id = p.group_id
      );
      write_log('��ix_samepoi��ɾ����'||sql%rowcount||'��¼');
      --ɾ��poi
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
      write_log('��IX_POI_CHARGINGPLOT��ɾ����'||sql%rowcount||'��¼');
      --IX_POI_CHARGINGSTATION
      execute immediate 'delete from IX_POI_CHARGINGSTATION c
      where exists
      (
      select 1 from temp_sensitive_estab t
      where t.pid = c.poi_pid
      )';
      write_log('��IX_POI_CHARGINGSTATION��ɾ����'||sql%rowcount||'��¼');
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
      write_log('��ix_poi_name_tone��ɾ����'||sql%rowcount||'��¼');
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
      write_log('��IX_POI��ɾ����'||sql%rowcount||'��¼');
  end;

  procedure delete_annotation_table(p_table_name varchar2)
  is
  begin
       execute immediate 'delete from '||p_table_name||' f 
       where exists
       (
             select 1 from temp_sensitive_estab t where t.pid = f.pid 
       )';
       write_log('��'||p_table_name||'��ɾ����'||sql%rowcount||'��¼');
  end;  
  
  /*
    ��������POI����������
    IX_ANNOTATION
    IX_ANNOTATION_FLAG
    IX_ANNOTATION_NAME

    IX_ANNOTATION���й���������Դ��˵��
    ��Դ���  SRC_FLAG  NUMBER(1)  0  δ���� 
    1  ��·�� 
    2  Hamlet 
    3  ����ͼ 
    4  ����
    5  POI 
    ע:���ֶν����� 2.5 �� 20 ������,����� TOP ��
    ���ݲ���Ҫ   
    
    ��Դ PID  SRC_PID  NUMBER(10)  ������Դ������ ID,������ POI��Ϊ POI�� PID 
    ���Ե�·����Ϊ��·�� ID 
    ע:���ֶν����� 2.5 �� 20 ������,����� TOP ��
    ���ݲ���Ҫ
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
  ���ڳ�����ҵ�ɹ��ⴴ��ʱ���ǵ�����POI�������������
  1)	�����ɹ���ʱ����ʶΪ������ʩ��POI���ݲ���ȡ��ͬʱά��POI�ĸ��ӹ�ϵ��ͬһ��ϵ
  2)	��������ԴΪPOI�ģ����POIɾ��������ҲҪɾ����
  */
  procedure filter
  is
    v_count pls_integer;
  begin
        v_log := '';
        v_index := 1;
        --ע��ִ��˳��,�ȹ������֣��ٹ���POI
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
