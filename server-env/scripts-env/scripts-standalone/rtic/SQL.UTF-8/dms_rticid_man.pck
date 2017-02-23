CREATE OR REPLACE PACKAGE dms_rticid_man IS

  -- Author  : xxw
  -- Created : 2014/2/2
  -- Purpose : rticid manager
  
  /**
  *申请rticid
  *此方法提供给远程数据库对象调用
  *申请数量最大支持500个，超过500数量请分多次申请
  *此方法会提交事务
  *返回的RTICID不保证连续且不保证顺序
  *@return :返回多个rticid以逗号分隔的序列字符串，格式为rticid1[,rticidn]
  */
  FUNCTION apply_rticid_standalone(
    p_mesh in varchar2,
    p_class in number,
    p_limit in integer,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return varchar2;
  
  /**
  *申请rticid
  *此方法提供给java程序调用
  *此方法不会提交事务
  *返回的RTICID不保证连续且不保证顺序
  *@return :返回多个rticid的数组
  */
  procedure apply_rticid(
    p_mesh in varchar2,
    p_class in number,
    p_limit in integer,
    o_set OUT t_varchar_array,
    p_version_id in number,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '');
    
  
  
  /**
  *归还rticid
  *此方法会提交事务
  *归还的数量不超过500个
  *返回是否归还成功(true/false)（传入id数量和存在且为预使用的id记录数一致才能归还成功）
  */
  function give_back_rticid_standalone(
    p_mesh in varchar2,
    p_class in number,
    p_rticid_seq in varchar2,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return varchar2;
  /**
  *归还rticid
  *此方法不会提交事务
  *返回是否归还成功(true/false)（传入id数量和存在且为预使用的id记录数一致才能归还成功）
  */
  function give_back_rticid(
    p_mesh in varchar2,
    p_class in number,
    p_rticid_seq in clob,
    p_version_id in number,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return varchar2;
    
  /**
  *
  */  
  function give_back_rticid_by_task(
    p_task_id in varchar2,
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return number;
/**
  *
  */  
  function give_back_rticid_by_version(
    p_version_id in number,
    p_task_id in varchar2,
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return number;
    
  /**
  *获取rticid的状态和作业季
  *此方法不会提交事务
  *返回“状态值,作业季”
  */
  function get_rticid_state_season(
    p_mesh in varchar2,
    p_class in number,
    p_rticid in number)return varchar2;

  /**
  *修改rticid的状态和作业季
  *自治事务，内部自动提交
  *此方法未做锁控制，修改时不能做归还
  *返回true/false,影响记录>0未true，其余为false
  */
  function update_rticid_standalone(
    p_mesh in varchar2,
    p_class in number,
    p_rticid in number,
    p_state in varchar2,
    p_season in varchar2)return varchar2;
    
END dms_rticid_man;
/
CREATE OR REPLACE PACKAGE BODY dms_rticid_man IS

  PROCEDURE apply_rticid(
    p_mesh in varchar2,
    p_class in number,
    p_limit in integer,
    o_set OUT t_varchar_array,
    p_version_id in number,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '') is
    v_init_rticid number(6) :=1;
    exist_set t_varchar_array := t_varchar_array();
    new_set t_varchar_array := t_varchar_array();
    TYPE usage_table_type IS TABLE OF rtic_segment_usage%ROWTYPE INDEX BY BINARY_INTEGER;
    usage_row usage_table_type;--加锁记录
  begin
    --验证参数
    if p_mesh is null or p_class is null or p_limit<=0 then
      raise_application_error(-20009,'参数不正确：图幅号:'||p_mesh||'等级：'||p_class||'申请数量：'||p_limit);
    end if;
    --加锁
    select * BULK COLLECT into usage_row from rtic_segment_usage where useage_id = '0' for update;
    --优先查找记录中存在未使用的rticid
    declare cursor e_cursor is select * from rtic_code where mesh_id = p_mesh and rtic_class = p_class and rtic_state = '未使用' order by rtic_id;
    begin
      for e_rec in e_cursor loop
        exist_set.extend(1);
        exist_set(exist_set.count):=to_char(e_rec.rtic_id);
        if exist_set.count=p_limit then
          o_set:=exist_set;
          update rtic_code set rtic_state = '预使用' where mesh_id = p_mesh and rtic_class = p_class and rtic_state = '未使用' and rtic_id in (select to_number(column_value) from table(exist_set));
          insert into rtic_segment_usage (useage_id,mesh_id,rtic_class,rtic_ids,version_id,task_id,client_id,client_ip,use_for,usage_type,usage_time)values(sys_guid(),p_mesh,p_class,exist_set,p_version_id,p_task_id,p_client,p_client_ip,p_use_for,1,sysdate);
          return;
        end if;
      end loop;
    end;
    --未使用的rticid不够，才会执行下面部分
    --把全部未使用的记录都更新为预使用
    update rtic_code set rtic_state = '预使用' where mesh_id = p_mesh and rtic_class = p_class and rtic_state = '未使用';
    --在已有的记录间隔中查找可用rticid
    declare cursor n_cursor is select * from rtic_code where mesh_id = p_mesh and rtic_class = p_class order by rtic_id;
    begin
      <<cursor_loop>>
      for n_rec in n_cursor loop
        if n_rec.rtic_id > v_init_rticid then
          <<interval_loop>>
          for i in v_init_rticid..(n_rec.rtic_id-1) loop
            exist_set.extend(1);
            exist_set(exist_set.count):=to_char(i);
            new_set.extend(1);
            new_set(new_set.count):=to_char(i);
            if exist_set.count=p_limit then
              o_set:=exist_set;
              insert into rtic_code select p_mesh,p_class,to_number(column_value),'预使用','' from table(new_set);
              insert into rtic_segment_usage (useage_id,mesh_id,rtic_class,rtic_ids,version_id,task_id,client_id,client_ip,use_for,usage_type,usage_time)values(sys_guid(),p_mesh,p_class,exist_set,p_version_id,p_task_id,p_client,p_client_ip,p_use_for,1,sysdate);
              return;
            end if;
          end loop interval_loop;
        end if;
        v_init_rticid:=n_rec.rtic_id+1;
      end loop cursor_loop;
    end;
    --在记录间隔查找还未找齐，才会执行下面部分
    --如果记录间rtic不够，则从结尾处直接补齐
    for j in v_init_rticid..4095 loop
      if exist_set.count<p_limit then 
        exist_set.extend(1);
        exist_set(exist_set.count):=to_char(j);
        new_set.extend(1);
        new_set(new_set.count):=to_char(j);
        if exist_set.count=p_limit then
          o_set:=exist_set;
          --这部分会把上一步记录间隔中查找的rticid也一起写入
          insert into rtic_code select p_mesh,p_class,to_number(column_value),'预使用','' from table(new_set);
          insert into rtic_segment_usage (useage_id,mesh_id,rtic_class,rtic_ids,version_id,task_id,client_id,client_ip,use_for,usage_type,usage_time)values(sys_guid(),p_mesh,p_class,exist_set,p_version_id,p_task_id,p_client,p_client_ip,p_use_for,1,sysdate);
          return;
        end if;
      end if;
    end loop;
    
    --到此则表明rticid已经不够了，抛出错误
    if exist_set.count<p_limit then 
      raise_application_error(-20009,'图幅号:'||p_mesh||'等级:'||p_class||'的rticid已不足以分配给此次请求，请联系研发同事。');
    end if;
  end;
  
  FUNCTION apply_rticid_standalone(
    p_mesh in varchar2,
    p_class in number,
    p_limit in integer,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return varchar2 is
  PRAGMA AUTONOMOUS_TRANSACTION;
  o_pid_set t_varchar_array;
  v_rticid varchar2(4000);
  begin
    if p_limit is null or p_limit>500 then
      raise_application_error(-20009,'申请失败：申请数量超过500。');
    end if;
    apply_rticid(p_mesh,p_class,p_limit,o_pid_set,0,p_task_id,p_client,p_client_ip,p_use_for);
    COMMIT;
    for i in 1..o_pid_set.count loop
      if i>1 then
        v_rticid:=v_rticid||','||to_char(o_pid_set(i));
      else
        v_rticid:=to_char(o_pid_set(i));
      end if;
    end loop; 
    RETURN v_rticid; 
  end;
  
  function give_back_rticid_standalone(
    p_mesh in varchar2,
    p_class in number,
    p_rticid_seq in varchar2,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return varchar2 is
  PRAGMA AUTONOMOUS_TRANSACTION;
  v_result varchar2(10);
  v_seq_count integer:=-1;
  begin
    SELECT LENGTH(REGEXP_REPLACE(REPLACE(p_rticid_seq, ',', '@'),  '[^@]+',  '')) into v_seq_count FROM DUAL;
    if v_seq_count<0 or v_seq_count>499 then
      raise_application_error(-20009,'归还失败：归还的rticid序列数量超过500。');
    end if;
    v_result := give_back_rticid(p_mesh,p_class,p_rticid_seq,0,p_task_id,p_client,p_client_ip,p_use_for);
    commit;
    return v_result;
  end;
  
  function give_back_rticid(
    p_mesh in varchar2,
    p_class in number,
    p_rticid_seq in clob,
    p_version_id in number,
    p_task_id in varchar2 default '',
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return varchar2 is
  seq_arr t_varchar_array;
  exist_count integer:=0;
  TYPE usage_table_type IS TABLE OF rtic_segment_usage%ROWTYPE INDEX BY BINARY_INTEGER;
  usage_row usage_table_type;--加锁记录
  begin
    if p_mesh is null or p_class is null or p_rticid_seq is null then
      raise_application_error(-20009,'参数不正确：图幅号:'||p_mesh||'等级：'||p_class||'p_rticid_seq：'||p_rticid_seq);
    end if;
    --加锁
    select * BULK COLLECT into usage_row from rtic_segment_usage where useage_id = '0' for update;
    --归还
    select * bulk collect into seq_arr
           from table(comma_to_table2(p_rticid_seq));
    select count(1) into exist_count from rtic_code where mesh_id = p_mesh and rtic_class = p_class and rtic_state = '预使用' and rtic_id in (select to_number(column_value) from table(seq_arr));
    if seq_arr is not null and exist_count>0 and seq_arr.count=exist_count then
      update rtic_code set rtic_state = '未使用' where mesh_id = p_mesh and rtic_class = p_class and rtic_state = '预使用' and rtic_id in (select to_number(column_value) from table(seq_arr));
      insert into rtic_segment_usage (useage_id,mesh_id,rtic_class,rtic_ids,version_id,task_id,client_id,client_ip,use_for,usage_type,usage_time)values(sys_guid(),p_mesh,p_class,seq_arr,p_version_id,p_task_id,p_client,p_client_ip,p_use_for,2,sysdate);
      return 'true';
    end if;
    raise_application_error(-20009,'归还出错：传入的id数量和库中已有的记录且状态为预使用的数量不一致。');
    return 'false';
  end;
  
  function give_back_rticid_by_task(
    p_task_id in varchar2,
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return number is
  row_arr t_varchar_array;
  t_count integer:=0;
  TYPE usage_table_type IS TABLE OF rtic_segment_usage%ROWTYPE INDEX BY BINARY_INTEGER;
  usage_row usage_table_type;--加锁记录
  begin
    if p_task_id is null then
      raise_application_error(-20009,'参数不正确：任务号:'||p_task_id);
    end if;
    --加锁
    select * BULK COLLECT into usage_row from rtic_segment_usage where useage_id = '0' for update;
    --归还
    declare cursor n_cursor is select * from rtic_segment_usage where task_id = p_task_id and usage_type = 1;
    begin
      for rec in n_cursor loop
        row_arr:=rec.rtic_ids;
        if row_arr is not null then
          update rtic_code set rtic_state = '未使用' where mesh_id = rec.mesh_id and rtic_class = rec.rtic_class and rtic_state = '预使用' and rtic_id in (select to_number(column_value) from table(rec.rtic_ids));
          insert into rtic_segment_usage (useage_id,mesh_id,rtic_class,rtic_ids,task_id,client_id,client_ip,use_for,usage_type,usage_time)values(sys_guid(),rec.mesh_id,rec.rtic_class,rec.rtic_ids,rec.task_id,rec.client_id,rec.client_ip,rec.use_for,2,sysdate);
          t_count:=t_count+SQL%ROWCOUNT;
        end if;
      end loop;
    end;
    return t_count;
  end;
  
  function give_back_rticid_by_version(
    p_version_id in number,
    p_task_id in varchar2,
    p_client in varchar2 default '',
    p_client_ip in varchar2 default '',
    p_use_for in varchar2 default '')return number is
  row_arr t_varchar_array;
  t_count integer:=0;
  TYPE usage_table_type IS TABLE OF rtic_segment_usage%ROWTYPE INDEX BY BINARY_INTEGER;
  usage_row usage_table_type;--加锁记录
  begin
    if p_version_id is null or p_task_id is null then
      raise_application_error(-20009,'参数不正确：版本号:'||p_version_id||',任务号:'||p_task_id);
    end if;
    --加锁
    select * BULK COLLECT into usage_row from rtic_segment_usage where useage_id = '0' for update;
    --归还
    declare cursor n_cursor is select * from rtic_segment_usage where task_id = p_task_id and version_id=p_version_id and usage_type = 1;
    begin
      for rec in n_cursor loop
        row_arr:=rec.rtic_ids;
        if row_arr is not null then
          update rtic_code set rtic_state = '未使用' where mesh_id = rec.mesh_id and rtic_class = rec.rtic_class and rtic_state = '预使用' and rtic_id in (select to_number(column_value) from table(rec.rtic_ids));
          insert into rtic_segment_usage (useage_id,mesh_id,rtic_class,rtic_ids,version_id,task_id,client_id,client_ip,use_for,usage_type,usage_time)values(sys_guid(),rec.mesh_id,rec.rtic_class,rec.rtic_ids,rec.version_id,rec.task_id,rec.client_id,rec.client_ip,rec.use_for,2,sysdate);
          t_count:=t_count+SQL%ROWCOUNT;
        end if;
      end loop;
    end;
    return t_count;
  end;
  
  function get_rticid_state_season(
    p_mesh in varchar2,
    p_class in number,
    p_rticid in number)return varchar2 is
  v_result varchar2(20);
  begin
    if p_mesh is null or p_class is null or p_rticid is null then
      raise_application_error(-20009,'参数不正确：图幅号:'||p_mesh||'等级：'||p_class||'p_rticid：'||p_rticid);
    end if;
    select rtic_state||','||season into v_result from rtic_code where mesh_id = p_mesh and rtic_class = p_class and rtic_id=p_rticid and rownum=1;
    return v_result;
  end;
  
  function update_rticid_standalone(
    p_mesh in varchar2,
    p_class in number,
    p_rticid in number,
    p_state in varchar2,
    p_season in varchar2)return varchar2 is
  PRAGMA AUTONOMOUS_TRANSACTION;
  v_result varchar2(10):='false';
  begin
    if p_mesh is null or p_class is null or p_rticid is null then
      raise_application_error(-20009,'参数不正确：图幅号:'||p_mesh||'等级：'||p_class||'p_rticid：'||p_rticid);
    end if;
    update rtic_code set rtic_state=p_state,season=p_season where mesh_id=p_mesh and rtic_class=p_class and rtic_id=p_rticid;
    if SQL%ROWCOUNT > 0 then
      v_result:='true';
    end if;
    commit;
    return v_result;
  end;

END;
/
