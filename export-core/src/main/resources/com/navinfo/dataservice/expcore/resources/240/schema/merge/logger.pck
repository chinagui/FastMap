create or replace package logger authid current_user
/**
* 日志输出程序：
*   1.自治事务控制
*   2.所有日志都记录到表 temp_procedure_log 中
*   3.日志输出包括以下部分：日志序号(用于日志排序)，记录时间(毫秒级)，级别，日志内容，会话信息(IP和SessionId)
*/
 is
  /**
  * 信息输出：
  * i_message:日志内容
  * i_caller:调用者
  */
  procedure info (i_message varchar2,i_caller varchar2:=null);
  /**
  * 跟踪信息输出
  */
  procedure trace (i_message varchar2,i_caller varchar2:=null);
  /**
  * 调试信息输出
  */
  procedure debug (i_message varchar2,i_caller varchar2:=null);
  /**
  * 错误信息输出
  */
  procedure error (i_message varchar2,i_caller varchar2:=null);

  /**
  * 按级别输出信息：
  * i_lvl:级别字符串，info,trace,debug 或任意字符串
  */
  procedure output (i_lvl varchar2,i_message varchar2,i_caller varchar2:=null);
end logger;
/
create or replace package body logger is
  procedure info (i_message varchar2,i_caller varchar2:=null)is
  begin
    output('info',i_message,i_caller);
  end;

  procedure trace (i_message varchar2,i_caller varchar2:=null)is
  begin
    output('trace',i_message,i_caller);
  end;

  procedure debug (i_message varchar2,i_caller varchar2:=null)is
  begin
    output('debug',i_message,i_caller);
  end;

  procedure error (i_message varchar2,i_caller varchar2:=null)is
  begin
    output('error',i_message,i_caller);
  end;

  PROCEDURE output (i_lvl VARCHAR2,i_message VARCHAR2,i_caller varchar2)IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    v_call_stack varchar2(255);
    v_error_backtrace varchar2(255);
    v_msg varchar2(4000 char);
    v_cnt number:=0;
    v_cnt_seq number:=0;
    v_session_info varchar2(200);
    v_time timestamp(8);
    v_id number;
	BEGIN   
   
		case 
			when i_lvl='prompt' or i_lvl ='info' then 
				v_msg:=substr(i_message,1,4000);
			when i_lvl='debug' then 
				v_msg:=substr(i_message,1,4000);
				v_call_stack:=substr(dbms_utility.FORMAT_CALL_STACK(),1,255);
				v_msg:=substr(v_msg||',callstack:'||v_call_stack,1,4000);
			when i_lvl='error' then
				v_msg:=substr(i_message,1,4000);
				v_error_backtrace:=substr(dbms_utility.format_error_backtrace(),1,255);
				v_msg:=substr(v_msg||',backtrace:'||v_error_backtrace,1,4000);
			else
				v_msg:=substr(i_message,1,4000);
		end case;
    --select current_timestamp(8) into v_time from dual;
    --v_session_info:=sys_context('USERENV','IP_ADDRESS')||'/'||USERENV('sessionid');
    execute immediate 'INSERT /*+APPEND*/ INTO TEMP_PROCEDURE_LOG
			 (id,occur_time, LVL, message,caller,session_info) 
			VALUES (seq_temp_procedure_log.nextval,:occur_time, :i_lvl, :v_msg,:i_caller,:v_session_info)' using current_timestamp,i_lvl,v_msg,i_caller,sys_context('USERENV','IP_ADDRESS')||'/'||USERENV('sessionid');
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('日志记录失败:'|| SQLERRM);
      --if instr(SQLERRM,'TEMP_PROCEDURE_LOG')!=0 then
      
        ---查询日志表序列是否存在
        select count(*) into v_cnt_seq from user_sequences where sequence_name = 'SEQ_TEMP_PROCEDURE_LOG';
        ---如果不存在则创建seq
        if v_cnt_seq=0 then
           execute immediate 'CREATE SEQUENCE  seq_temp_procedure_log
           MINVALUE 1 MAXVALUE 3199999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE
           ';
        end if;
        ---查询程序日志表是否存在
        select count(*)into v_cnt from user_tables where table_name='TEMP_PROCEDURE_LOG';

        ---如果不存在则创建表
        if v_cnt=0 then
          execute immediate 'CREATE TABLE TEMP_PROCEDURE_LOG
            (
              id number (10) ,
              OCCUR_TIME  TIMESTAMP(8)                      DEFAULT current_timestamp(6),
              LVL         VARCHAR2(100 char),
              MESSAGE     VARCHAR2(4000 char),
              caller     VARCHAR2(500 char),
              session_info      VARCHAR2(400 CHAR)
            )
            LOGGING
            NOCOMPRESS
            NOCACHE
            NOPARALLEL
            MONITORING' ;
            output(i_lvl,i_message,i_caller);
        end if;
      --end if;
      rollback;
    END;
end logger;
/
