CREATE OR REPLACE PACKAGE Pk_History_Util IS
  v_seq_id number(10):=0;
  v_default_operate_name varchar2(30):='外业数据融合';
  v_default_version_id number(10);
  v_default_task_id varchar2(30);
  v_default_Operate_Id number(10):=0;
  TYPE t_raw_operate_log IS TABLE OF temp_merge_raw_operate_log%ROWTYPE;
  function to_cdata(v_name  varchar2,v_value number,col_precision number,col_scale number)return varchar2;
  function to_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_dml_type number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) RETURN t_raw_operate_log 
    PIPELINED parallel_enable;
    
  function to_update_au_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_att_flag VARCHAR2,
    v_geo_flag VARCHAR2,
    v_pre_value number,
    v_cur_value number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) RETURN t_raw_operate_log 
    PIPELINED parallel_enable;
    
  procedure generate_del_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  );
  
  
  procedure generate_add_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) ;
  
  procedure generate_update_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) ;
  procedure generate_au_work_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_att_flag VARCHAR2,
    v_geo_flag VARCHAR2,
    v_pre_value number,
    v_cur_value number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  );
END Pk_History_Util;
/
CREATE OR REPLACE PACKAGE BODY Pk_History_Util IS
	PROCEDURE Trace(Msg VARCHAR2) IS
	BEGIN
	  logger.trace(Msg);
		--Dbms_Output.Put_Line(To_Char(SYSDATE, 'yyyy-mm-dd hh24:mi:ss') || ': ' || Substr(Msg, 1, 500));
	
	END;
  
	FUNCTION q(s VARCHAR) /*将传入的字符串首尾加上引号*/
	 RETURN VARCHAR2 IS
	BEGIN
		RETURN '''' || s || '''';
	END;
  
  FUNCTION To_Object_Name(i_Table_Name VARCHAR2) RETURN VARCHAR2 IS
    v_Name VARCHAR2(100);
  BEGIN
    CASE
      WHEN i_Table_Name = 'IX_POINTADDRESS' THEN
        v_Name := 'IXPointAddress';
      WHEN i_Table_Name = 'IX_POINTADDRESS_NAME' THEN
        v_Name := 'IXPointAddress';
      WHEN i_Table_Name = 'IX_POINTADDRESS_CHILDREN' THEN
        v_Name := 'IXPointAddressParent';
      WHEN i_Table_Name = 'IX_POINTADDRESS_PARENT' THEN
				v_Name := 'IXPointAddressParent';
        
			WHEN i_Table_Name = 'AU_IX_POINTADDRESS' THEN
				v_Name := 'AuIXPointAddress';
        
			WHEN i_Table_Name = 'AU_PT_PLATFORM' THEN
				v_Name := 'AuPTPoi';
			WHEN i_Table_Name = 'AU_PT_TRANSFER' THEN
				v_Name := 'AuPTTransfer';
			WHEN i_Table_Name = 'AU_PT_PLATFORM_ACCESS' THEN
				v_Name := 'AUPTPlatform';
			WHEN i_Table_Name = 'AU_PT_COMPANY' THEN
				v_Name := 'AuPTCompany';
			WHEN i_Table_Name = 'AU_PT_SYSTEM' THEN
				v_Name := 'AuPTCompany';
        
			WHEN i_Table_Name = 'PT_PLATFORM' THEN
				v_Name := 'PTPoi';
			WHEN i_Table_Name = 'PT_PLATFORM_NAME' THEN
				v_Name := 'PTPoi';
			WHEN i_Table_Name = 'PT_TRANSFER' THEN
				v_Name := 'PTTransfer';
			WHEN i_Table_Name = 'PT_PLATFORM_ACCESS' THEN
				v_Name := 'PTPoi';
			WHEN i_Table_Name = 'PT_COMPANY' THEN
				v_Name := 'PTCompany';
      WHEN i_Table_Name = 'PT_SYSTEM' THEN
				v_Name := 'PTCompany'; 
			WHEN i_Table_Name = 'PT_ETA_SYSTEM' THEN
				v_Name := 'PTCompany';
      WHEN i_Table_Name = 'PT_STRAND' THEN
				v_Name := 'PTStrand';  
      WHEN i_Table_Name = 'PT_STRAND_NAME' THEN
				v_Name := 'PTStrand';  
      WHEN i_Table_Name = 'PT_STRAND_SCHEDULE' THEN
				v_Name := 'PTStrand';    
      WHEN i_Table_Name = 'PT_STRAND_PLATFORM' THEN
				v_Name := 'PTStrand';  
      WHEN i_Table_Name = 'PT_RUNTIME' THEN
				v_Name := 'PTLine';  
      WHEN i_Table_Name = 'AU_PT_STRAND' THEN
				v_Name := 'AuPTStrand'; 
      WHEN i_Table_Name = 'AU_PT_STRAND_PLATFORM' THEN
				v_Name := 'AuPTStrand';   
      WHEN i_Table_Name = 'IX_POINTADDRESS_FLAG' THEN
				v_Name := 'IXPointAddress';     
			ELSE
				v_Name := i_Table_Name;
		END CASE;
		RETURN v_Name;
	END;
  
  function get_tab_pidname(i_Table_Name VARCHAR2) RETURN VARCHAR2 IS
    v_Name VARCHAR2(100);
  BEGIN
    CASE
      WHEN i_Table_Name = 'IX_POINTADDRESS' THEN
        v_Name := 'PID';
      WHEN i_Table_Name = 'IX_POINTADDRESS_NAME' THEN
        v_Name := 'PID';
      WHEN i_Table_Name = 'IX_POINTADDRESS_CHILDREN' THEN
        v_Name := 'GROUP_ID';
      WHEN i_Table_Name = 'IX_POINTADDRESS_PARENT' THEN
				v_Name := 'GROUP_ID';
			WHEN i_Table_Name = 'AU_IX_POINTADDRESS' THEN
				v_Name := 'AUDATA_ID';
      
			WHEN i_Table_Name = 'AU_PT_PLATFORM' THEN
				v_Name := 'AUDATA_ID';
			WHEN i_Table_Name = 'AU_PT_TRANSFER' THEN
				v_Name := 'AUDATA_ID';
			WHEN i_Table_Name = 'AU_PT_PLATFORM_ACCESS' THEN
				v_Name := 'AUDATA_ID';
			WHEN i_Table_Name = 'AU_PT_COMPANY' THEN
				v_Name := 'AUDATA_ID';
			WHEN i_Table_Name = 'AU_PT_SYSTEM' THEN
				v_Name := 'AUDATA_ID';
			WHEN i_Table_Name = 'AU_PT_STRAND_PLATFORM' THEN
				v_Name := 'AUDATA_ID';--OPERATOR
			WHEN i_Table_Name = 'AU_PT_STRAND' THEN
				v_Name := 'AUDATA_ID';
        
			WHEN i_Table_Name = 'PT_PLATFORM' THEN
				v_Name := 'POI_PID';
			WHEN i_Table_Name = 'PT_PLATFORM_NAME' THEN
				v_Name := 'PID';
			WHEN i_Table_Name = 'PT_TRANSFER' THEN
				v_Name := 'TRANSFER_ID';
			WHEN i_Table_Name = 'PT_PLATFORM_ACCESS' THEN
				v_Name := 'ACCESS_ID';
			WHEN i_Table_Name = 'PT_COMPANY' THEN
				v_Name := 'COMPANY_ID';
			WHEN i_Table_Name = 'PT_SYSTEM' THEN
				v_Name := 'COMPANY_ID';
      WHEN i_Table_Name = 'PT_STRAND' THEN
        v_Name :=  'PID' ;
      WHEN i_Table_Name = 'PT_STRAND_NAME' THEN
        v_Name :=  'PID' ;
      WHEN i_Table_Name = 'PT_STRAND_PLATFORM' THEN
        v_Name :=  'STRAND_PID' ; 
      WHEN i_Table_Name = 'PT_STRAND_SCHEDULE' THEN
        v_Name :=  'STRAND_PID' ;
			ELSE
				v_Name := 'PID';
		END CASE;
		RETURN v_Name;
	END;
  
  function to_cdata(v_name  varchar2,v_value varchar2)return varchar2 is
    begin
      return  '<'|| v_name||'><![CDATA[' ||v_value||']]></'||v_name||'>';          
    end;
  
  function to_cdata(v_name  varchar2,v_value number,col_precision number,col_scale number)return varchar2 is
    begin
      if col_scale=0 then
        return '<'|| v_name ||'><![CDATA[' 
        ||to_char(v_value)
        ||']]></'||v_name||'>';        
      elsif v_value < 1 then
        return '<'|| v_name ||'><![CDATA[0' 
        ||to_char(v_value,'FM999999999999999.'||substr('0000000000',1,col_scale))
        ||']]></'||v_name||'>';
      else
        return '<'|| v_name ||'><![CDATA[' 
        ||to_char(v_value,'FM999999999999999.'||substr('0000000000',1,col_scale))
        ||']]></'||v_name||'>';
      end if;
          
    end;
  
  function to_cdata(v_name  varchar2,v_value date)return varchar2 is
    begin
      return '<'|| v_name||'><![CDATA[' ||to_char(v_value,'yyyy-mm-dd hh24:mi:ss')||']]></'||v_name||'>';
          
    end;
        
 /* function to_cdata_geo_point(v_name  varchar2,v_value sdo_geometry)return varchar2 is
    begin
      return '<'|| v_name||'><![CDATA[POINT (' ||v_value.sdo_point.x||' '||v_value.sdo_point.y||')]]></'||v_name||'>';
          
    end;*/
    
  function to_cdata_geo(v_name  varchar2,v_value sdo_geometry)return varchar2 is
    begin
      if v_value is null then
        return  '<'|| v_name||'><![CDATA[]]></'||v_name||'>';
      elsif v_value.SDO_GTYPE=2001 then      
         return '<'|| v_name||'><![CDATA[POINT (' ||v_value.sdo_point.x||' '||v_value.sdo_point.y||')]]></'||v_name||'>';
      elsif v_value.SDO_GTYPE=2002 then
         return '<'|| v_name||'><![CDATA[' ||v_value.get_wkt()||']]></'||v_name||'>';
      end if;
    end;
  
  function compare_geo_point(a sdo_geometry,b sdo_geometry)return boolean is
    begin
      if a.sdo_point.x=b.sdo_point.x and a.sdo_point.y=b.sdo_point.y then
        return true;
      else
        return false;
      end if;
    end;
    
  function to_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_dml_type number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) RETURN t_raw_operate_log 
    PIPELINED parallel_enable IS
  
  /*procedure to_history(sql_text VARCHAR2) IS */
  
    v_cur    SYS_REFCURSOR;
    v_cursor NUMBER;
  
    v_col_count NUMBER;
    v_desc      dbms_sql.desc_tab;
    v_value     NUMBER;
    v_varchar2  VARCHAR2(4000);
    v_number    NUMBER;
    v_date      DATE;
    v_geo       mdsys.sdo_geometry := NULL;
  
    v_pre_content VARCHAR2(32767);
    v_cur_content VARCHAR2(32767);
    v_ret_row temp_merge_raw_operate_log%ROWTYPE;
    
    v_object_id number;
    v_object_name varchar2(50);
    
    v_tab_middle_index integer;
    v_loop_count pls_integer;
    v_tmp_str_pre varchar2(5000);
    v_tmp_str_cur varchar2(5000);
    
    v_chang_col varchar2(500);
    v_pidname varchar2(30):=get_tab_pidname(v_table);
  BEGIN
    v_object_name:=To_Object_Name(v_table);
    
    OPEN v_cur FOR v_sql;
    v_cursor := dbms_sql.to_cursor_number(v_cur);
    dbms_sql.describe_columns(v_cursor, v_col_count, v_desc);
    FOR i IN 1 .. v_col_count LOOP
      IF v_desc(i).col_type = 2 THEN
        dbms_sql.define_column(v_cursor, i, v_number);
      ELSIF v_desc(i).col_type = 12 THEN
        dbms_sql.define_column(v_cursor, i, v_date);
      ELSIF v_desc(i).col_type = 109 THEN
        dbms_sql.define_column(v_cursor, i, v_geo);
      ELSE
        dbms_sql.define_column(v_cursor, i, v_varchar2, 4000);
      END IF;
      --dbms_output.put_line(v_desc(i).col_name||':'||v_desc(i).col_type||' ');
    END LOOP;
  
    WHILE dbms_sql.fetch_rows(v_cursor) > 0 LOOP
      --dbms_sql.column_value(v_cursor, 1, v_value);
      
      if v_dml_type=1 then --修改履历
         v_loop_count:=(v_col_count-1)/2;         
         dbms_sql.column_value(v_cursor, v_col_count, v_chang_col);
      else
         v_loop_count:=v_col_count;
      end if;
      FOR i IN 1 .. v_loop_count LOOP
        --trace(i||'/'||v_col_count||','||v_loop_count||','||v_desc(i).col_name||'-'||v_desc(v_loop_count+i).col_name);
        if v_desc(i).col_name=v_pidname then
           dbms_sql.column_value(v_cursor, i, v_object_id);
        end if;
        IF v_desc(i).col_type = 2 THEN
          dbms_sql.column_value(v_cursor, i, v_number);
          --v_tmp_str_pre:= '<'|| v_desc(i).col_name||'><![CDATA[' ||to_char(v_number)||']]></'||v_desc(i).col_name||'>';
          v_tmp_str_pre:= to_cdata(v_desc(i).col_name,v_number,v_desc(i).col_precision,v_desc(i).col_scale); 
          if v_dml_type=1 and instr(v_chang_col,'<'||v_desc(i).col_name||'/>') is not null then
             
            dbms_sql.column_value(v_cursor, v_loop_count+i, v_number);
             
            --v_tmp_str_cur:='<'|| v_desc(i).col_name||'><![CDATA[' ||to_char(v_number)||']]></'||v_desc(i).col_name||'>';
            v_tmp_str_cur:=to_cdata(v_desc(i).col_name,v_number,v_desc(i).col_precision,v_desc(i).col_scale);
          else 
            v_tmp_str_cur:=v_tmp_str_pre;
          end if;
        ELSIF v_desc(i).col_type = 12 THEN
          dbms_sql.column_value(v_cursor, i, v_date);
          v_tmp_str_pre:= '<'|| v_desc(i).col_name||'><![CDATA[' ||to_char(v_date,'yyyy-mm-dd hh24:mi:ss')||']]></'||v_desc(i).col_name||'>';
          
          if v_dml_type=1 and instr(v_chang_col,'<'||v_desc(i).col_name||'/>') is not null then
            dbms_sql.column_value(v_cursor, v_loop_count+i, v_date);
            v_tmp_str_cur:='<'|| v_desc(i).col_name||'><![CDATA[' ||to_char(v_date,'yyyy-mm-dd hh24:mi:ss')||']]></'||v_desc(i).col_name||'>';
          else 
            v_tmp_str_cur:=v_tmp_str_pre;
          end if;
        ELSIF v_desc(i).col_type = 109 THEN
          dbms_sql.column_value(v_cursor, i, v_geo);--仅支持点geometry，其它类型待处理，不能用get_wkt函数,它的返回类型是clob，速度太慢
          v_tmp_str_pre:= to_cdata_geo(v_desc(i).col_name,v_geo);--'<'|| v_desc(i).col_name||'><![CDATA[POINT (' ||v_geo.sdo_point.x||' '||v_geo.sdo_point.y||')]]></'||v_desc(i).col_name||'>';
          
          if v_dml_type=1 and instr(v_chang_col,'<'||v_desc(i).col_name||'/>') is not null then
            dbms_sql.column_value(v_cursor, v_loop_count+i, v_geo);
            v_tmp_str_cur:=to_cdata_geo(v_desc(i).col_name,v_geo);--'<'|| v_desc(i).col_name||'><![CDATA[POINT (' ||v_geo.sdo_point.x||' '||v_geo.sdo_point.y||')]]></'||v_desc(i).col_name||'>';
          else 
            v_tmp_str_cur:=v_tmp_str_pre;
          end if;
          
        ELSE
          dbms_sql.column_value(v_cursor, i, v_varchar2);
          v_tmp_str_pre:= '<'|| v_desc(i).col_name||'><![CDATA[' ||v_varchar2||']]></'||v_desc(i).col_name||'>';
          
          if v_dml_type=1 and instr(v_chang_col,'<'||v_desc(i).col_name||'/>') is not null then
            dbms_sql.column_value(v_cursor, v_loop_count+i, v_varchar2);
            v_tmp_str_cur:= '<'|| v_desc(i).col_name||'><![CDATA[' ||v_varchar2||']]></'||v_desc(i).col_name||'>';
          else 
            v_tmp_str_cur:=v_tmp_str_pre;
          end if;
          
        END IF;
        v_pre_content:=v_pre_content||v_tmp_str_pre;
        v_cur_content:=v_cur_content||v_tmp_str_cur;
      --dbms_output.put_line(v_desc(i).col_name||':'||v_desc(i).col_type||' ');
      END LOOP;         
      
      v_ret_row.previous_content:=v_pre_content;
      
      v_ret_row.current_content:=v_cur_content;      
      
      v_ret_row.chang_col:=v_chang_col;
      
      v_ret_row.Dml_Type  := v_dml_type;
      v_ret_row.his_type  :=1;
      v_ret_row.Operate_Log_Id := Sys_Guid();
      v_ret_row.Operate_Id     := v_Operate_Id;
      v_ret_row.Id             := v_seq_id;
      v_ret_row.Inner_Id       := v_seq_id;
      v_ret_row.Table_Name     := v_Table;
      v_ret_row.Object_Name    := v_object_name;	
      v_ret_row.task_id:=v_task_id;
      v_ret_row.version_id:=v_version_id;
      v_ret_row.Object_Id := v_object_id;				
      v_ret_row.operate_name:=v_operate_name;	
      --insert into temp_merge_raw_operate_log values v_ret_row;
      PIPE ROW(v_ret_row);
      v_seq_id:=v_seq_id+1;
      v_tmp_str_pre:=null;
      v_tmp_str_cur:=null;
      v_pre_content:=null;
      v_cur_content:=null;
      v_chang_col:=null;
    END LOOP;
    
    dbms_sql.close_cursor(v_cursor);
    
  END;
  
  FUNCTION GET_node_string(v_cursor in out nocopy number,v_desc in out nocopy dbms_sql.desc_tab,i number
    )RETURN VARCHAR2 IS
    v_varchar2  VARCHAR2(4000);
    v_number    NUMBER;
    v_date      DATE;
    v_geo       mdsys.sdo_geometry := NULL;
    
    begin 
        IF v_desc(i).col_type = 2 THEN
          dbms_sql.column_value(v_cursor, i, v_number);
          return to_cdata(v_desc(i).col_name,v_number,v_desc(i).col_precision,v_desc(i).col_scale);
        ELSIF v_desc(i).col_type = 12 THEN
          dbms_sql.column_value(v_cursor, i, v_date);
          return to_cdata(v_desc(i).col_name,v_date);
        ELSIF v_desc(i).col_type = 109 THEN
        
          dbms_sql.column_value(v_cursor, i, v_geo);
          return to_cdata_geo(v_desc(i).col_name,v_geo);
        ELSE
          dbms_sql.column_value(v_cursor, i, v_varchar2);
          return to_cdata(v_desc(i).col_name,v_varchar2);
          
        END IF;
    
    end;
    
  function diff_to_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_dml_type number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) RETURN t_raw_operate_log 
    PIPELINED parallel_enable IS
   
    v_cur    SYS_REFCURSOR;
    v_cursor NUMBER;
  
    v_col_count NUMBER;
    v_desc      dbms_sql.desc_tab;
    v_value     NUMBER;
    v_varchar2  VARCHAR2(4000);
    v_number    NUMBER;
    v_date      DATE;
    v_geo       mdsys.sdo_geometry := NULL;
  
    v_varchar2_2  VARCHAR2(4000);
    v_number_2    NUMBER;
    v_date_2      DATE;
    v_geo_2     mdsys.sdo_geometry := NULL;
    v_pre_content VARCHAR2(32767);
    v_cur_content VARCHAR2(32767);
    v_ret_row temp_merge_raw_operate_log%ROWTYPE;
    
    v_object_id number;
    v_object_name varchar2(50);
    
    v_tab_middle_index integer;
    v_loop_count pls_integer;
    v_tmp_str_pre varchar2(5000);
    v_tmp_str_cur varchar2(5000);
    
    v_chang_col varchar2(500);
    v_pidname varchar2(30):=get_tab_pidname(v_table);
  BEGIN
    v_object_name:=To_Object_Name(v_table);
    
    OPEN v_cur FOR v_sql;
    v_cursor := dbms_sql.to_cursor_number(v_cur);
    dbms_sql.describe_columns(v_cursor, v_col_count, v_desc);
    FOR i IN 1 .. v_col_count LOOP
      IF v_desc(i).col_type = 2 THEN
        dbms_sql.define_column(v_cursor, i, v_number);
      ELSIF v_desc(i).col_type = 12 THEN
        dbms_sql.define_column(v_cursor, i, v_date);
      ELSIF v_desc(i).col_type = 109 THEN
        dbms_sql.define_column(v_cursor, i, v_geo);
      ELSE
        dbms_sql.define_column(v_cursor, i, v_varchar2, 4000);
      END IF;
      --dbms_output.put_line(v_desc(i).col_name||':'||v_desc(i).col_type||' ');
    END LOOP;
  
    WHILE dbms_sql.fetch_rows(v_cursor) > 0 LOOP
      --dbms_sql.column_value(v_cursor, 1, v_value);
      
      if v_dml_type=1 then --修改履历
         v_loop_count:=(v_col_count-1)/2;         
         dbms_sql.column_value(v_cursor, v_col_count, v_chang_col);
      else
         v_loop_count:=v_col_count;
      end if;
      FOR i IN 1 .. v_loop_count LOOP
        --trace(i||'/'||v_col_count||','||v_loop_count||','||v_desc(i).col_name||'-'||v_desc(v_loop_count+i).col_name);
        if v_desc(i).col_name=v_pidname then
           dbms_sql.column_value(v_cursor, i, v_object_id);
        end if;
        IF v_desc(i).col_type = 2 THEN
          dbms_sql.column_value(v_cursor, i, v_number);
          v_tmp_str_pre:= to_cdata(v_desc(i).col_name,v_number);
          dbms_sql.column_value(v_cursor, v_loop_count+i, v_number_2); 
          v_tmp_str_cur:=to_cdata(v_desc(i).col_name,v_number); 
          if  v_number!=v_number_2 then
             v_chang_col:=v_chang_col||v_desc(i).col_name;
          end if;
        ELSIF v_desc(i).col_type = 12 THEN
          dbms_sql.column_value(v_cursor, i, v_date);
          v_tmp_str_pre:= to_cdata(v_desc(i).col_name,v_date);
          dbms_sql.column_value(v_cursor, v_loop_count+i, v_date_2); 
          v_tmp_str_cur:=to_cdata(v_desc(i).col_name,v_date); 
          if  v_date!=v_date_2 then
             v_chang_col:=v_chang_col||v_desc(i).col_name;
          end if;
        ELSIF v_desc(i).col_type = 109 THEN
        
          dbms_sql.column_value(v_cursor, i, v_geo);
          v_tmp_str_pre:= to_cdata_geo(v_desc(i).col_name,v_geo);
          dbms_sql.column_value(v_cursor, v_loop_count+i, v_geo_2); 
          v_tmp_str_cur:=to_cdata_geo(v_desc(i).col_name,v_geo); 
          if  compare_geo_point(v_geo,v_geo_2) then
             v_chang_col:=v_chang_col||v_desc(i).col_name;
          end if;
          
          
        ELSE
          dbms_sql.column_value(v_cursor, i, v_varchar2);
          v_tmp_str_pre:= to_cdata(v_desc(i).col_name,v_varchar2);
          dbms_sql.column_value(v_cursor, v_loop_count+i, v_varchar2_2); 
          v_tmp_str_cur:=to_cdata(v_desc(i).col_name,v_varchar2); 
          if  v_varchar2!=v_varchar2_2 then
             v_chang_col:=v_chang_col||v_desc(i).col_name;
          end if;
          
        END IF;
        v_pre_content:=v_pre_content||v_tmp_str_pre;
        v_cur_content:=v_cur_content||v_tmp_str_cur;
      --dbms_output.put_line(v_desc(i).col_name||':'||v_desc(i).col_type||' ');
      END LOOP;         
      
      v_ret_row.previous_content:=v_pre_content;
      
      v_ret_row.current_content:=v_cur_content;      
      
      v_ret_row.chang_col:=v_chang_col;
      
      v_ret_row.Dml_Type  := v_dml_type;
      v_ret_row.his_type  :=1;
      v_ret_row.Operate_Log_Id := Sys_Guid();
      v_ret_row.Operate_Id     := v_Operate_Id;
      v_ret_row.Id             := v_seq_id;
      v_ret_row.Inner_Id       := v_seq_id;
      v_ret_row.Table_Name     := v_Table;
      v_ret_row.Object_Name    := v_object_name;	
      v_ret_row.task_id:=v_task_id;
      v_ret_row.version_id:=v_version_id;
      v_ret_row.Object_Id := v_object_id;				
      v_ret_row.operate_name:=v_operate_name;	
      --insert into temp_merge_raw_operate_log values v_ret_row;
      PIPE ROW(v_ret_row);
      v_seq_id:=v_seq_id+1;
      v_tmp_str_pre:=null;
      v_tmp_str_cur:=null;
      v_pre_content:=null;
      v_cur_content:=null;
      v_chang_col:=null;
    END LOOP;
    dbms_sql.close_cursor(v_cursor);
  END; 
    
  
  function to_update_au_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_att_flag VARCHAR2,
    v_geo_flag VARCHAR2,
    v_pre_value number,
    v_cur_value number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) RETURN t_raw_operate_log 
    PIPELINED parallel_enable IS
   
    v_cur    SYS_REFCURSOR;
    v_cursor NUMBER;
  
    v_col_count NUMBER;
    v_desc      dbms_sql.desc_tab;
    v_value     NUMBER;
    v_varchar2  VARCHAR2(4000); 
    
    v_number    NUMBER;
    v_date      DATE;
    v_geo       mdsys.sdo_geometry := NULL;
    
    v_pre_content VARCHAR2(32767);
    v_cur_content VARCHAR2(32767);
    v_ret_row temp_merge_raw_operate_log%ROWTYPE;
    
    v_object_id number;
    v_object_name varchar2(50);
    
    v_tab_middle_index integer;
    v_loop_count pls_integer;
    v_tmp_str_pre varchar2(5000);
    v_tmp_str_cur varchar2(5000);
    
    v_chang_col varchar2(500);
    v_pidname varchar2(30):=get_tab_pidname(v_table);
    
    v_table_cols varchar2(1000);
  BEGIN
    v_object_name:=To_Object_Name(v_table);
    
    select ','||wmsys.wm_concat(t.COLUMN_NAME)||','  into v_table_cols  
    from user_tab_columns t where t.TABLE_NAME =upper(v_table);
    
    OPEN v_cur FOR v_sql;
    v_cursor := dbms_sql.to_cursor_number(v_cur);
    dbms_sql.describe_columns(v_cursor, v_col_count, v_desc);
    FOR i IN 1 .. v_col_count LOOP
      IF v_desc(i).col_type = 2 THEN
        dbms_sql.define_column(v_cursor, i, v_number);
      ELSIF v_desc(i).col_type = 12 THEN
        dbms_sql.define_column(v_cursor, i, v_date);
      ELSIF v_desc(i).col_type = 109 THEN
        dbms_sql.define_column(v_cursor, i, v_geo);
      ELSE
        dbms_sql.define_column(v_cursor, i, v_varchar2, 4000);
      END IF;
      --dbms_output.put_line(v_desc(i).col_name||':'||v_desc(i).col_type||' ');
    END LOOP;
  
    WHILE dbms_sql.fetch_rows(v_cursor) > 0 LOOP
      
      v_loop_count:=v_col_count;
      FOR i IN 1 .. v_loop_count LOOP
        if v_desc(i).col_name=v_pidname then
           dbms_sql.column_value(v_cursor, i, v_object_id);
        end if;
        
        if instr (v_table_cols,','||v_desc(i).col_name||',') >0 then
           
        
          if v_desc(i).col_name='ATT_OPRSTATUS' THEN
              dbms_sql.column_value(v_cursor, i, V_NUMBER);
            
              if v_att_flag ='T' and v_geo_flag='F' then
                IF V_NUMBER=0 THEN                  
                  v_chang_col:=v_chang_col||'<'||v_desc(i).col_name||'/>';
                  v_tmp_str_pre:=to_cdata(v_desc(i).col_name,V_PRE_value);          
                  v_tmp_str_cur:=to_cdata(v_desc(i).col_name,V_CUR_value); 
                END IF ;
               
              elsif v_att_flag ='F' and v_geo_flag='T' then
                  v_varchar2:=get_node_string(v_cursor,v_desc,i);
                  v_tmp_str_pre:=v_varchar2;
                  v_tmp_str_CUR:=v_varchar2;
              elsif v_att_flag ='T' and v_geo_flag='T' then
                IF V_NUMBER=0 THEN                  
                  v_chang_col:=v_chang_col||'<'||v_desc(i).col_name||'/>';
                  v_tmp_str_pre:=to_cdata(v_desc(i).col_name,V_PRE_value);          
                  v_tmp_str_cur:=to_cdata(v_desc(i).col_name,V_CUR_value); 
                END IF ;
                
              end if;
          
          ELSIF  v_desc(i).col_name='GEO_OPRSTATUS' THEN
              dbms_sql.column_value(v_cursor, i, V_NUMBER);
            
              if v_att_flag ='T' and v_geo_flag='F' then
                  v_varchar2:=get_node_string(v_cursor,v_desc,i);
                  v_tmp_str_pre:=v_varchar2;
                  v_tmp_str_CUR:=v_varchar2;
              elsif v_att_flag ='F' and v_geo_flag='T' then
                IF V_NUMBER=0 THEN                  
                  v_chang_col:=v_chang_col||'<'||v_desc(i).col_name||'/>';
                  v_tmp_str_pre:=to_cdata(v_desc(i).col_name,V_PRE_value);          
                  v_tmp_str_cur:=to_cdata(v_desc(i).col_name,V_CUR_value); 
                END IF ;
                
              elsif v_att_flag ='T' and v_geo_flag='T' then
                IF V_NUMBER=0 THEN                  
                  v_chang_col:=v_chang_col||'<'||v_desc(i).col_name||'/>';
                  v_tmp_str_pre:=to_cdata(v_desc(i).col_name,V_PRE_value);          
                  v_tmp_str_cur:=to_cdata(v_desc(i).col_name,V_CUR_value); 
                END IF ;
                
              end if;
                                                    
          
          ELSE
            v_varchar2:=get_node_string(v_cursor,v_desc,i);
            v_tmp_str_pre:=v_varchar2;
            v_tmp_str_CUR:=v_varchar2;
            
          END IF;
          
        end if;
        
        
        v_pre_content:=v_pre_content||v_tmp_str_pre;
        v_cur_content:=v_cur_content||v_tmp_str_cur;
      --dbms_output.put_line(v_desc(i).col_name||':'||v_desc(i).col_type||' ');
      END LOOP;         
      if v_chang_col is not null then
        v_ret_row.previous_content:=v_pre_content;
        
        v_ret_row.current_content:=v_cur_content;   
        
        v_ret_row.chang_col:=v_chang_col;
        
        v_ret_row.Dml_Type  := 1;
        v_ret_row.his_type  :=1;
        v_ret_row.Operate_Log_Id := Sys_Guid();
        v_ret_row.Operate_Id     := v_Operate_Id;
        v_ret_row.Id             := v_seq_id;
        v_ret_row.Inner_Id       := v_seq_id;
        v_ret_row.Table_Name     := v_Table;
        v_ret_row.Object_Name    := v_object_name;	
        v_ret_row.task_id:=v_task_id;
        v_ret_row.version_id:=v_version_id;
        v_ret_row.Object_Id := v_object_id;				
        v_ret_row.operate_name:=v_operate_name;	
        --insert into temp_merge_raw_operate_log values v_ret_row;      
        PIPE ROW(v_ret_row);
      end if;
      v_seq_id:=v_seq_id+1;
      v_tmp_str_pre:=null;
      v_tmp_str_cur:=null;
      v_pre_content:=null;
      v_cur_content:=null;
      v_chang_col:=null;
    END LOOP;
    dbms_sql.close_cursor(v_cursor);
  END; 
    
  
  procedure generate_del_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) is
    begin
      insert into temp_merge_raw_operate_log
      select * from table(to_history(upper(v_table),v_sql,2,v_Operate_Id,v_task_id,v_version_id,v_operate_name));    
    end;
  
  
  procedure generate_add_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) is
    begin
      insert into temp_merge_raw_operate_log
      select * from table(to_history(upper(v_table),v_sql,0,v_Operate_Id,v_task_id,v_version_id,v_operate_name));    
    end;
  
  
  
  procedure generate_update_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) is
    begin
      insert into temp_merge_raw_operate_log
      select * from table(to_history(upper(v_table),v_sql,1,v_Operate_Id,v_task_id,v_version_id,v_operate_name));    
    end;
    
  procedure generate_au_work_history(
    v_table varchar2,
    v_sql VARCHAR2,
    v_att_flag VARCHAR2,
    v_geo_flag VARCHAR2,
    v_pre_value number,
    v_cur_value number,
    v_Operate_Id number:=v_default_Operate_Id,
    v_task_id varchar2:=v_default_task_id,
    v_version_id number:=v_default_version_id,
    v_operate_name varchar2:=v_default_operate_name
  ) is 
    begin
		INSERT INTO Temp_Merge_Raw_Operate_Log
			SELECT *
				FROM TABLE(To_Update_Au_History(upper(V_Table), V_Sql, V_Att_Flag, V_Geo_Flag, V_Pre_Value, V_Cur_Value, V_Operate_Id, V_Task_Id, V_Version_Id, V_Operate_Name
																				 
																				 ));    end;
 

END Pk_History_Util;
/
