 create or replace trigger MetaTrigger 
   AFTER   INSERT OR UPDATE OR DELETE  
   ON RD_NAME 
   for each row 
   
 DECLARE   
	v_exception_info varchar2(200) := '';
  --创建cursor 
  cursor cur is select distinct t.DB_LINK from user_db_links t where  t.DB_LINK like 'RG_DBLINK_%';
 
 begin 
   
   IF INSERTING THEN  --同步新增
     --将操作信息添加到日志表
      INSERT INTO META_DML_LOGS(DML_object_id,DML_TYPE,DML_TABLE ) values  
      (:NEW.name_id,0,'RD_NAME' ) ; 
    --for 循环
  for dblinks in cur   loop  
  --RD_NAME@dblinks.DB_LINK 数据库表的同步新增
     execute immediate   'INSERT INTO RD_NAME@'||dblinks.DB_LINK||' VALUES(
         :1,:2,:3,:4,:5,:6,:7,:8,:9,:10,
		 :11,:12,:13,:14,:15,:16,:17,:18,:19,:20,
		 :21,:22,:23,:24,:25,:26,:27,:28,:29
    )'    
	using :NEW.name_id,:NEW.name_groupid,:NEW.lang_code,:NEW.name,:NEW.type,
	:NEW.base,:NEW.prefix,:NEW.infix,:NEW.suffix,:NEW.name_phonetic,
    :NEW.type_phonetic,:NEW.base_phonetic,:NEW.prefix_phonetic,:NEW.infix_phonetic,:NEW.suffix_phonetic,
    :NEW.src_flag,:NEW.road_type,:NEW.admin_id,:NEW.code_type,:NEW.voice_file,
    :NEW.src_resume,:NEW.pa_region_id,:NEW.memo,:NEW.route_id,:NEW.u_record,
    :NEW.u_fields,:NEW.split_flag,:NEW.city,:NEW.process_flag;
  end loop;  --for 循环结束
  
  --同步删除
   ELSIF DELETING THEN
     --将操作信息添加到日志表
      INSERT INTO META_DML_LOGS(DML_object_id,DML_TYPE,DML_TABLE ) values 
      (:OLD.name_id,1,'RD_NAME' ) ;
     --for 循环
  for dblinks in cur loop  
  
   --RD_NAME@dblinks.DB_LINK数据库表的同步删除
  execute immediate  'DELETE  RD_NAME@'||dblinks.DB_LINK||' WHERE name_id=:1 '
  using :OLD.name_id;
   
   end loop; --for 循环结束
   
   --同步更新 
   ELSE
     
   --将操作信息添加到日志表
      INSERT INTO META_DML_LOGS(DML_object_id,DML_TYPE,DML_TABLE ) values 
      (:NEW.name_id,2,'RD_NAME' ) ;
 
  --for 循环
  for dblinks in cur loop  
  --RD_NAME@dblinks.DB_LINK数据库表的同步更新
   execute immediate  'UPDATE RD_NAME@'||dblinks.DB_LINK||' SET 
    
    name_groupid=:1,lang_code=:2,name=:3,type=:4,base=:5,
    prefix=:6,infix=:7,suffix=:8,name_phonetic=:9,type_phonetic=:10,
    base_phonetic=:11,prefix_phonetic=:12,infix_phonetic=:13,suffix_phonetic=:14,src_flag=:15,
    road_type=:16,admin_id=:17,code_type=:18,voice_file=:19,src_resume=:20,
    pa_region_id=:21,memo=:22,route_id=:23,u_record=:24,u_fields=:25,
    split_flag=:26,city=:27,process_flag=:28
    
    WHERE name_id=:29'
    
    using :NEW.name_groupid,:NEW.lang_code,:NEW.name,:NEW.type,
	  :NEW.base,:NEW.prefix,:NEW.infix,:NEW.suffix,:NEW.name_phonetic,
    :NEW.type_phonetic,:NEW.base_phonetic,:NEW.prefix_phonetic,:NEW.infix_phonetic,:NEW.suffix_phonetic,
    :NEW.src_flag,:NEW.road_type,:NEW.admin_id,:NEW.code_type,:NEW.voice_file,
    :NEW.src_resume,:NEW.pa_region_id,:NEW.memo,:NEW.route_id,:NEW.u_record,
    :NEW.u_fields,:NEW.split_flag,:NEW.city,:NEW.process_flag,:NEW.name_id;
   end loop; --for 循环结束
   
 
   END IF;
   EXCEPTION     --异常捕获
      WHEN OTHERS THEN
     v_exception_info := '失败ErrorCode' || sqlcode || 'ErrorText:' ||SUBSTR(SQLERRM, 1, 160)||' '||DBMS_UTILITY.format_error_backtrace;
    INSERT INTO META_DML_SF_LOGS(dml_success,DML_TABLE ,exception_info) values (0,'RD_NAME', v_exception_info) ;
END MetaTrigger;
/
exit;