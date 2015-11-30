CREATE OR REPLACE PACKAGE Pk_Merge_Pt IS

	FUNCTION Parselog_Ptp(Log VARCHAR2) RETURN VARCHAR2;

	PROCEDURE Do_Merge(V_Task_Id   VARCHAR2,
										 V_Merge_Att VARCHAR2 := 'F',
										 V_Merge_Geo VARCHAR2 := 'F');
END Pk_Merge_Pt;
/
CREATE OR REPLACE PACKAGE BODY Pk_Merge_Pt IS

  state_default number(1):=0;--状态：默认值
  state_add number(1):=1;--状态：新增
  state_del number(1):=2;--状态：删除
  state_update number(1):=3;--状态：修改

	PROCEDURE Trace(Msg VARCHAR2) IS
	BEGIN
	
		Dbms_Output.Put_Line(To_Char(SYSDATE, 'yyyy-mm-dd hh24:mi:ss') || ': ' || Substr(Msg, 1, 500));
		Logger.Trace(Msg, 'PT Platform融合程序');
	END;

	FUNCTION Parselog_Ptp(Log VARCHAR2) RETURN VARCHAR2 IS
		Vs VARCHAR2(200);
	BEGIN
	
		Vs := '<LOG/><STATE/>';
		IF Log IS NULL OR Instr(Log, '改主点编号') > 0
		THEN
			Vs := Vs || '<POI_PID/>';
		END IF;
		IF Log IS NULL OR Instr(Log, '改验证标识') > 0
		THEN
			Vs := Vs || '<COLLECT/>';
		END IF;
		IF Log IS NULL OR Instr(Log, '改站台层级') > 0
		THEN
			Vs := Vs || '<P_LEVEL/>';
		END IF;
		IF Log IS NULL OR Instr(Log, '改站台换乘标识') > 0
		THEN
			Vs := Vs || '<TRANSIT_FLAG/>';
		END IF;
	
		RETURN Vs;
	END;
	
	
	function Find_Matched_PT/**递增下一个节点，继续匹配，直到最后一条记录为止**/(v_idx        in integer,
                                        v_strand_pid in varchar2,
                                       v_seq         in number,
                                       v_next_seq       out number,
                                        v_next_opr   out varchar2)
  return number is
  v_total_num number;
  v_platform varchar2(100);
    v_found_times number;
begin
  SELECT count(1) into v_total_num FROM pt_strand_platform t WHERE t.strand_pid = v_strand_pid and t.seq_num>v_seq;

  v_found_times := v_idx;
  IF v_idx > v_total_num then
     dbms_output.put_line('Found nothing'||v_idx);
     return -1;
  else
     begin
             WITH rs AS
             (
             select t.operator,t.platform_pid,t.seq_num,rownum num from (SELECT t.operator,t.seq_num,t.platform_pid
                FROM pt_strand_platform t
               WHERE  t.strand_pid=v_strand_pid and t.seq_num>v_seq
             ORDER BY t.seq_num ASC   ) t 
               )
            SELECT t.operator, idx INTO v_next_opr, v_next_seq
              FROM temp_pt_strand_pl_sort t,( select * from rs where   num=v_idx)t2
                WHERE t.operator = t2.operator;
       exception
         when no_data_found then
             dbms_output.put_line('Found nothing,goon...Ex');
            return Find_Matched_PT(v_idx => v_idx+1,v_strand_pid => v_strand_pid,v_seq => v_seq,v_next_seq => v_next_seq,v_next_opr => v_next_opr);
        END;
     
     if v_next_opr is null or v_next_opr = '' then
        dbms_output.put_line('Found nothing,goon...');
        return Find_Matched_PT(v_idx => v_idx+1,v_strand_pid => v_strand_pid,v_seq => v_seq,v_next_seq => v_next_seq,v_next_opr => v_next_opr);
     else
        dbms_output.put_line(',vidx:'||v_idx||'Found it out data is .');
        return v_found_times;
     end if;
  END IF;
end Find_Matched_PT;


PROCEDURE sort_pt_strand_platform(V_Task_Id   VARCHAR2) AS
   v_pre_opr  VARCHAR2(100) := NULL;
  v_pre_seq  NUMBER:=0;
  v_next_opr VARCHAR2(100) := NULL;
  v_next_seq NUMBER:=-1;
  v_mx       NUMBER:=0;
  v_exists  number:=0;
BEGIN
  --将外业中的<>del的数据插入到sort表中
  INSERT INTO temp_pt_strand_pl_sort
    SELECT strand_pid,
           platform_pid,
           seq_num,
           INTERVAL,
           operator,
           update_time,
           log,
           editionflag,
           state,
           data_source,
           update_batch,
           0 AS u_record,
           NULL AS u_fields,
           row_number() over(PARTITION BY strand_pid ORDER BY seq_num ASC) * 10000 AS idx
      FROM (SELECT t.strand_pid,
                   t.platform_pid,
                   t.seq_num,
                   t.INTERVAL,
                   t.operator,
                   t.update_time,
                   t.log,
                   t.editionflag,
                   t.state,
                   t.data_source,
                   t.update_batch,
                   0            AS u_record,
                   NULL         AS u_fields
              FROM au_pt_strand_platform t
              LEFT JOIN temp_pt_strand_pl_add t2
                ON t.strand_pid = t2.strand_pid
               AND t.operator = t2.operator
              LEFT JOIN temp_aupt_strand_pl_err t3
                ON t.operator = t3.operator
             WHERE t.state <> 2
               AND t.strand_pid IS NOT NULL
               AND t2.operator IS NULL
               AND t3.operator IS NULL
            
            UNION
            SELECT strand_pid,
                   platform_pid,
                   seq_num,
                   INTERVAL,
                   operator,
                   update_time,
                   log,
                   editionflag,
                   state,
                   data_source,
                   update_batch,
                   u_record,
                   u_fields
              FROM temp_pt_strand_pl_add);

  --select t1.operator,t2.operator,t2.seq_num from au_pt_strand_platform t1 left join pt_strand_platform t2  on t1.operator=t2.operator where t1.state<>2 ;
  --查询外业中操作的strand_id
  FOR rec IN (SELECT DISTINCT t.strand_pid FROM temp_pt_strand_pl_sort t) LOOP
    SELECT MAX(idx)
      INTO v_mx
      FROM temp_pt_strand_pl_sort
     WHERE strand_pid = rec.strand_pid; 
    --查询内业存在，外业不存在的      
    FOR rec_none IN (SELECT * 　from (SELECT t.*,
                                            au.operator AS au_operator,
                                            row_number() over(PARTITION BY 1 ORDER BY t.seq_num) AS idx,
                                            COUNT(1) over(PARTITION BY 1) total
                                       FROM pt_strand_platform t
                                       LEFT JOIN temp_pt_strand_pl_sort au
                                         ON t.operator = au.operator
                                      WHERE t.strand_pid = rec.strand_pid) WHERE au_operator IS NULL ORDER BY seq_num) LOOP
      v_pre_opr:=null;
      v_pre_seq:=0;  
      v_next_opr:=null;
      v_next_seq:=-1;
      v_exists:=0;   
                                      
      IF (rec_none.idx = 1) THEN
        --第一条
        v_pre_opr := NULL;
        v_pre_seq := 0;
      ELSE
        --查找前面是否在某个共有的数据的后面
        WITH rs AS
         (SELECT t.operator
            FROM pt_strand_platform t
           WHERE t.seq_num < rec_none.seq_num
             and t.strand_pid=rec.strand_pid                    
           ORDER BY t.seq_num DESC)
        SELECT count(1) into v_exists
          FROM temp_pt_strand_pl_sort t, (select * from rs where rownum=1)t2
         WHERE t.operator = t2.operator;
         if(v_exists>0) then
            WITH rs AS
             (SELECT t.operator
                FROM pt_strand_platform t
               WHERE t.seq_num < rec_none.seq_num
                 and t.strand_pid=rec.strand_pid
               ORDER BY t.seq_num DESC)
            SELECT t.operator, t.idx
              INTO v_pre_opr, v_pre_seq
              FROM temp_pt_strand_pl_sort t, (select * from rs where rownum=1)t2
              WHERE t.operator = t2.operator;
         else
            v_pre_opr := NULL;
            v_pre_seq := 0;            
         end if;
      END IF;
    
      IF v_pre_opr IS NOT NULL THEN
        --前面有的话，直接插入到pre的后面
        INSERT INTO temp_pt_strand_pl_sort
        VALUES
          (rec_none.strand_pid,
           rec_none.platform_pid,
           rec_none.seq_num,
           rec_none.interval,
           rec_none.operator,
           rec_none.update_time,
           rec_none.log,
           rec_none.editionflag,
           rec_none.state,
           rec_none.data_source,
           rec_none.update_batch,
           rec_none.u_record,
           rec_none.u_fields,
           v_pre_seq + 1);
         if v_pre_seq=v_mx then v_mx:=v_mx+1 ;end if;  
      ELSE
        IF (rec_none.idx = rec_none.total) THEN
          v_next_opr := NULL;
          v_next_seq := -1;
        ELSE
          --判断后面是否存在
         v_exists:=Find_Matched_PT(v_idx => 1,v_strand_pid => rec.strand_pid,v_seq => rec_none.seq_num,v_next_seq => v_next_seq,v_next_opr => v_next_opr);
        END IF;
        
        IF (v_next_opr IS NOT NULL) THEN
          --后面有，则插入到next的前面
          INSERT INTO temp_pt_strand_pl_sort
          VALUES
            (rec_none.strand_pid,
             rec_none.platform_pid,
             rec_none.seq_num,
             rec_none.interval,
             rec_none.operator,
             rec_none.update_time,
             rec_none.log,
             rec_none.editionflag,
             rec_none.state,
             rec_none.data_source,
             rec_none.update_batch,
             rec_none.u_record,
             rec_none.u_fields,
             v_next_seq - v_exists - 1);
        ELSE
          --增加到最后
          v_mx := v_mx + 1;
          INSERT INTO temp_pt_strand_pl_sort
          VALUES
            (rec_none.strand_pid,
             rec_none.platform_pid,
             rec_none.seq_num,
             rec_none.interval,
             rec_none.operator,
             rec_none.update_time,
             rec_none.log,
             rec_none.editionflag,
             rec_none.state,
             rec_none.data_source,
             rec_none.update_batch,
             rec_none.u_record,
             rec_none.u_fields,
             v_mx);
        
        END IF;
      END IF;      
    END LOOP;
    MERGE INTO temp_pt_strand_pl_sort t
    USING (SELECT operator,
                  row_number() over(PARTITION BY 1 ORDER BY idx) rn
             FROM temp_pt_strand_pl_sort
            WHERE strand_pid = rec.strand_pid) t2
    ON (t.operator = t2.operator)
    WHEN MATCHED THEN
      UPDATE SET t.seq_num = t2.rn * 10000;
  END LOOP;
END;

	FUNCTION Parselog_tf(Log VARCHAR2) RETURN VARCHAR2 IS
		Vs VARCHAR2(200);
	BEGIN
	
		Vs := '<LOG/><STATE/>';
		IF Log IS NULL OR Instr(Log, '改换乘点ID1') > 0
		THEN
			Vs := Vs || '<POI_FIR/>';
		END IF;
		IF Log IS NULL OR Instr(Log, '改换乘点ID2') > 0
		THEN
			Vs := Vs || '<POI_SEC/>';
		END IF;
		IF Log IS NULL OR Instr(Log, '改换乘类型') > 0
		THEN
			Vs := Vs || '<TRANSFER_TYPE/>';
		END IF;
		IF Log IS NULL OR Instr(Log, '改换乘时间') > 0
		THEN
			Vs := Vs || '<TRANSFER_TIME/>';
		END IF;	
		IF Log IS NULL OR Instr(Log, '改外部标识') > 0
		THEN
			Vs := Vs || '<EXTERNAL_FLAG/>';
		END IF;
		RETURN Vs;
	END;
  
  --属性融合或属性加几何
	PROCEDURE Merge_Att(V_Task_Id   VARCHAR2,
											V_Merge_Geo VARCHAR2 := 'F' /*是否同时融几何*/) IS
	BEGIN
		Trace('过滤已作业的数据');
		INSERT INTO Temp_Aupt_Unchg
			SELECT *
				FROM Temp_Aupt_pl_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('Temp_Aupt_Unchg 无变化的数据：'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_Access_Unchg
			SELECT *
				FROM Temp_Aupt_Access_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('Temp_Aupt_Access_Unchg 无变化的数据：'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_strand_Un
			SELECT *
				FROM Temp_Aupt_strand_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('Temp_Aupt_strand_Un 无变化的数据：'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_strand_pl_Un
			SELECT *
				FROM Temp_Aupt_strand_pl_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('Temp_Aupt_strand_pl_Un 无变化的数据：'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_Tf_Unchg
			SELECT *
				FROM Temp_Aupt_Tf_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('Temp_Aupt_Tf_Unchg 无变化的数据：'||SQL%ROWCOUNT);
    
    
    
    
		INSERT INTO temp_aupt_company_un
			SELECT *
				FROM temp_aupt_company_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('temp_pt_company_un 无变化的数据：'||SQL%ROWCOUNT);
    
    
		INSERT INTO temp_aupt_system_un
			SELECT *
				FROM temp_aupt_system_tk
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace('temp_pt_system_un 无变化的数据：'||SQL%ROWCOUNT);
	 
  
		Trace('过滤删除、修改但在母库中不存在的数据');
		INSERT INTO Temp_Aupt_Error
			SELECT T.*
				FROM Temp_Aupt_pl_tk t
				LEFT JOIN Pt_Platform b
					ON T.Pid = B.Pid
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.Pid IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Aupt_Unchg);
		Trace('Temp_Aupt_Error 错误数据：'||SQL%ROWCOUNT);
    

		INSERT INTO Temp_Aupt_strand_Err
			SELECT T.*
				FROM Temp_Aupt_strand_tk t
				LEFT JOIN Pt_strand b
					ON T.pid = b.pid
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.pid IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
				FROM Temp_Aupt_strand_Un);
		Trace('Temp_Aupt_strand_Error 错误数据：'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_Tf_Error
			SELECT T.*
				FROM Temp_Aupt_Tf_tk t
				LEFT JOIN Pt_Transfer b
					ON T.Transfer_Id = B.Transfer_Id
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.Transfer_Id IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Aupt_Tf_Unchg);
		Trace('Temp_Aupt_Tf_Error 错误数据：'||SQL%ROWCOUNT);
	
	
		INSERT INTO Temp_Aupt_company_err
			SELECT T.*
				FROM Temp_Aupt_company_tk t
				LEFT JOIN pt_company b
					ON t.company_id=b.company_id
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.company_id IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Aupt_company_un);
		Trace('Temp_Aupt_company_err 错误数据：'||SQL%ROWCOUNT);
    
    
		INSERT INTO Temp_Aupt_system_err
			SELECT T.*
				FROM Temp_Aupt_system_tk t
				LEFT JOIN pt_system b
					ON t.system_id=b.system_id
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.system_id IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Aupt_system_un);
		Trace('Temp_Aupt_system_err 错误数据：'||SQL%ROWCOUNT);
    
    
		INSERT INTO Temp_Aupt_strand_err
			SELECT T.*
				FROM Temp_Aupt_strand_tk t
				LEFT JOIN pt_strand b
					ON t.pid=b.pid
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.pid IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Aupt_strand_un);
		Trace('Temp_Aupt_strand_err 错误数据：'||SQL%ROWCOUNT);
    
    INSERT INTO Temp_Aupt_strand_pl_err
			SELECT T.*
				FROM Temp_Aupt_strand_pl_tk t
				LEFT JOIN pt_strand_platform b
					ON t.operator=b.operator--pt_strand_platform使用operator作为唯一标示
			 WHERE T.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND b.operator is null
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Aupt_strand_pl_un);
		Trace('Temp_Aupt_strand_pl_err 错误数据：'||SQL%ROWCOUNT);
    
    INSERT INTO Temp_Aupt_Access_error
			SELECT t.*
				FROM Temp_Aupt_Access_tk t
        left join pt_platform_access b
        on t.relate_id=b.relate_id
			 WHERE t.State IN (state_del, state_update)
						 AND T.Att_Oprstatus = 0
						 AND B.relate_id IS NULL
						 AND T.relate_id NOT IN (SELECT relate_id
																			 FROM Temp_Aupt_Access_Unchg);
		Trace('Temp_Aupt_Access_error 错误数据：'||SQL%ROWCOUNT);
    
    -----------------------------------
		INSERT INTO Temp_Aupt_Filter 
			SELECT T.*, B.Pid AS Main_Pid,
						 (CASE
								WHEN Instr(T.Log, '改站台名') > 0 THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Name, T.State AS New_State
				FROM Temp_Aupt_pl_tk t
				LEFT JOIN Pt_Platform b
					ON T.Pid = B.Pid
				LEFT JOIN Temp_Aupt_Unchg c
					ON T.Audata_Id = C.Audata_Id
				LEFT JOIN Temp_Aupt_Error d
					ON T.Audata_Id = D.Audata_Id
			 WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL;
		Trace('需要融合的数据 Temp_Aupt_Filter:'||SQL%ROWCOUNT);
    

		INSERT INTO Temp_Aupt_strand_fl
			SELECT T.*, B.Pid AS Main_Pid,
						 (CASE
								WHEN Instr(T.Log, '改头标') > 0 or Instr(T.Log, '改后缀名') > 0 THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Name, T.State AS New_State
				FROM Temp_Aupt_strand_tk t
				LEFT JOIN Pt_strand b
					ON T.Pid = B.Pid
				LEFT JOIN Temp_Aupt_strand_Un c
					ON T.Audata_Id = C.Audata_Id
				LEFT JOIN Temp_Aupt_strand_Err d
					ON T.Audata_Id = D.Audata_Id
			 WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL;
		Trace('需要融合的数据 Temp_Aupt_strand_fl:'||SQL%ROWCOUNT);
    --Temp_Aupt_strand_fl
    insert into Temp_Aupt_strand_pl_Fl
    select t.*, d.operator as Main_Pid,
            (CASE
								WHEN instr( t.log, '改STRAND编号') > 0 OR
                      instr(t.log, '改站台编号') > 0 OR
                      instr(t.log, '改站台顺序') > 0 OR
                      instr(t.log, '改时间间隔') > 0
                THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Name,T.State AS New_State
    from Temp_Aupt_Strand_pl_tk t 
    left join pt_strand_platform d
    on t.operator=d.operator
    left join temp_aupt_strand_pl_un b
    on t.operator=b.operator
    left join temp_aupt_strand_pl_err c
    on t.operator = c.operator
    where b.operator IS NULL and c.operator is null; 
		Trace('需要融合的数据 Temp_Aupt_strand_pl_Fl:'||SQL%ROWCOUNT);
    
    insert into Temp_Aupt_Access_Filter
    select t.*
    from au_pt_platform_access t -- left join temp_aupt_access_unchg b
    --on t.relate_id=b.relate_id
    left join Temp_Aupt_Access_error d
    on t.relate_id=d.relate_id
    where 
    -- b.relate_id IS NULL and 
    d.relate_id is null; 
		Trace('需要融合的数据 Temp_Aupt_Access_Filter:'||SQL%ROWCOUNT);
    
    insert into Temp_Aupt_tf_filter
    select t.*,b.transfer_id AS Main_Pid,T.State AS New_State
    from Temp_Aupt_tf_tk t
    left join pt_transfer b
    on t.transfer_id=b.transfer_id
    left join Temp_Aupt_Tf_Unchg c
    on t.audata_id=c.audata_id
    left join Temp_Aupt_Tf_Error d
    on t.audata_id=d.Audata_Id
     WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL; 
		Trace('需要融合的数据 Temp_Aupt_tf_filter:'||SQL%ROWCOUNT);
    
    insert into Temp_Aupt_company_fl
    select t.*,b.company_id AS Main_Pid, T.State AS New_State
    from Temp_Aupt_company_tk t
    left join pt_company b
    on t.company_id=b.company_id
    left join Temp_Aupt_company_un c
    on t.audata_id=c.audata_id
    left join Temp_Aupt_company_err d
    on t.audata_id=d.Audata_Id
     WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL; 
		Trace('需要融合的数据 Temp_Aupt_company_fl:'||SQL%ROWCOUNT);
    
    insert into Temp_aupt_system_fl
    select t.*,b.system_id AS Main_Pid, T.State AS New_State
    from Temp_Aupt_system_tk t
    left join pt_system b
    on t.system_id=b.system_id
    left join Temp_Aupt_system_un c
    on t.audata_id=c.audata_id
    left join Temp_Aupt_system_err d
    on t.audata_id=d.Audata_Id
     WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL; 
		Trace('需要融合的数据 Temp_Aupt_system_fl:'||SQL%ROWCOUNT);
	
		Trace('修正数据：新增存在时变成修改');
		UPDATE Temp_Aupt_Filter
			 SET New_State = state_update, Has_Update_Name = 1
		 WHERE State = state_add
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		UPDATE Temp_Aupt_strand_fl
			 SET New_State = state_update, Has_Update_Name = 1
		 WHERE State = state_add
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
    
    UPDATE Temp_Aupt_strand_pl_Fl
			 SET New_State = state_update
		 WHERE State = state_add
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		UPDATE Temp_Aupt_tf_filter
			 SET New_State = state_update
		 WHERE State = state_add
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		UPDATE temp_aupt_system_fl
			 SET New_State = state_update
		 WHERE State = state_add
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
    
		UPDATE temp_aupt_company_fl
			 SET New_State = state_update
		 WHERE State = state_add
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
    
		---────────────────────────────────
		----        主表
		---────────────────────────────────
	
		Trace('生成主表del');
		INSERT INTO Temp_Ptp_Del
			SELECT B.*
				FROM Temp_Aupt_Filter a, Pt_Platform b
			 WHERE A.New_State = state_del
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表add');
		INSERT INTO Temp_Ptp_Add
			(Pid, Poi_Pid, City_Code, COLLECT, P_Level, Transit_Flag, Operator, Update_Time, Log,
			 Edition_Flag, State, Data_Source, Update_Batch, Nidb_Platformid, Task_Id, Data_Version,
			 Field_Task_Id, U_Record, U_Fields)
			SELECT Pid, Poi_Pid, City_Code, COLLECT, P_Level, Transit_Flag, Operator, Update_Time, Log,
						 Edition_Flag, State, Data_Source, Update_Batch, Nidb_Platformid, 0 AS Task_Id,
						 Data_Version, Field_Task_Id, 0 AS U_Record, NULL AS U_Fields
				FROM Temp_Aupt_Filter
			 WHERE New_State = state_add;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表update:before');
		INSERT INTO Temp_Ptp_Del
			SELECT B.*
				FROM Temp_Aupt_Filter a, Pt_Platform b
			 WHERE A.New_State = state_update
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表update:after');
	
		INSERT INTO Temp_Ptp_Add
			(Pid, Poi_Pid, City_Code, COLLECT, P_Level, Transit_Flag, Operator, Update_Time, Log,
			 Edition_Flag, State, Data_Source, Update_Batch, Nidb_Platformid, Task_Id, Data_Version,
			 Field_Task_Id, U_Record, U_Fields)
			SELECT B.Pid,
						 (CASE
								WHEN Instr(A.Log, '改主点编号') > 0 or a.state=state_add THEN
								 A.Poi_Pid
								ELSE
								 B.Poi_Pid
							END) AS Poi_Pid, B.City_Code,
						 (CASE
								WHEN Instr(A.Log, '改验证标识') > 0 or a.state=state_add THEN
								 A.Collect
								ELSE
								 B.Collect
							END) AS COLLECT,
						 (CASE
								WHEN Instr(A.Log, '改站台层级') > 0 or a.state=state_add  THEN
								 A.P_Level
								ELSE
								 B.P_Level
							END) AS P_Level,
						 (CASE
								WHEN Instr(A.Log, '改站台换乘标识') > 0 or a.state=state_add THEN
								 A.Transit_Flag
								ELSE
								 B.Transit_Flag
							END) AS Transit_Flag, B.Operator, B.Update_Time, A.Log, '外业修改', A.State,
						 B.Data_Source, B.Update_Batch, B.Nidb_Platformid, B.Task_Id, B.Data_Version,
						 B.Field_Task_Id, B.U_Record AS U_Record, NULL AS U_Fields
				FROM Temp_Aupt_Filter a, Pt_Platform b
			 WHERE A.New_State = state_update
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		--合并主表
		Trace('合并主表del');
		DELETE FROM Pt_Platform t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Aupt_Filter a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表add');
		INSERT INTO Pt_Platform t
			SELECT *
				FROM Temp_Ptp_Add;
		Trace(SQL%ROWCOUNT);
	/*
		Trace('合并主表update:del');
		DELETE FROM Pt_Platform t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Ptp_Update_Before a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表update:after');
		INSERT INTO Pt_Platform t
			SELECT *
				FROM Temp_Ptp_Update_After;
		Trace(SQL%ROWCOUNT);*/
	
		---────────────────────────────────
		----        名称表
		---────────────────────────────────
	
		Trace('名称表del');
		INSERT INTO Temp_Ptp_Name_Del
			SELECT B.*
				FROM Temp_Aupt_Filter a, Pt_Platform_Name b
			 WHERE A.New_State = state_del
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表add');
		INSERT INTO Temp_Ptp_Name_Add
			(Name_Id, Name_Groupid, Pid, Lang_Code, NAME, Phonetic, Src_Flag, U_Record, U_Fields)
			SELECT B.Name_Id, B.Name_Groupid, B.Pid, B.Lang_Code, B.Name, B.Phonetic, B.Src_Flag,
						 0 AS U_Record, 0 AS U_Fields
				FROM Temp_Aupt_Filter a, Au_Pt_Platform_Name b
			 WHERE A.New_State = state_add
						 AND A.Audata_Id = B.Audata_Id;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表update:del');
		INSERT INTO Temp_Ptp_Name_Del
			SELECT B.*
				FROM Temp_Aupt_Filter a, Pt_Platform_Name b
			 WHERE A.Pid = B.Pid
						 AND B.Lang_Code = 'CHI'
						 AND A.New_State = state_update
						 AND A.Has_Update_Name = 1;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表update:add');
		INSERT INTO Temp_Ptp_Name_Add
			(Name_Id, Name_Groupid, Pid, Lang_Code, NAME, Phonetic, Src_Flag, U_Record, U_Fields)
			SELECT Name_Id, Name_Groupid, Pid, Lang_Code, NAME, Phonetic, Src_Flag, 0 AS U_Record,
						 0 AS U_Fields
				FROM (SELECT B.*
								 FROM Temp_Aupt_Filter a, Au_Pt_Platform_Name b
								WHERE A.Audata_Id = B.Audata_Id
                      AND B.Lang_Code = 'CHI'
											AND A.New_State = state_update
											AND A.Has_Update_Name = 1);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表del');
		DELETE FROM Pt_Platform_Name t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Ptp_Name_Del a
						 WHERE T.Name_Id = A.Name_Id);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表add');
		INSERT INTO Pt_Platform_Name t
			SELECT *
				FROM Temp_Ptp_Name_Add;
		Trace(SQL%ROWCOUNT);
	
		/*Trace('合并名称表update:del');
		DELETE FROM Pt_Platform_Name t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Ptp_Name_Update_Del a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表update:add');
		INSERT INTO Pt_Platform_Name t
			SELECT *
				FROM Temp_Ptp_Name_Update_Add;
		Trace(SQL%ROWCOUNT);*/
	  
    
		---────────────────────────────────
		----        STRAND 表
		---────────────────────────────────
    
		Trace('生成STRAND del');
		INSERT INTO Temp_Pt_strand_Del
			SELECT B.*
				FROM Temp_Aupt_strand_Fl a, Pt_strand b
			 WHERE A.New_State = state_del
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成STRAND add');
		INSERT INTO Temp_Pt_strand_Add
			(CITY_CODE,DATA_SOURCE,DATA_VERSION,DISTANCE,EDITION_FLAG,
			FIELD_TASK_ID,GEOMETRY,INCREASED_PRICE,INCREASED_STEP,
			LINE_ID,LOG,MEMO,NIDB_STRANDID,PAIR_STRAND_PID,
			PID,STATE,TASK_ID,TICKET_START,TICKET_SYS,
			TOTAL_PRICE,UPDATE_BATCH,UP_DOWN,
			U_FIELDS,U_RECORD)
			SELECT CITY_CODE,DATA_SOURCE,DATA_VERSION,DISTANCE,'外业修改',
			FIELD_TASK_ID,GEOMETRY,INCREASED_PRICE,INCREASED_STEP,
			LINE_ID,LOG,MEMO,NIDB_STRANDID,PAIR_STRAND_PID,
			PID,STATE,0 as TASK_ID,TICKET_START,TICKET_SYS,
			TOTAL_PRICE,UPDATE_BATCH,UP_DOWN,
			0,0
				FROM Temp_Aupt_strand_fl
			 WHERE New_State = state_add;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成STRAND update:before');
		INSERT INTO Temp_Pt_strand_Del
			SELECT B.*
				FROM Temp_Aupt_strand_fl a, Pt_strand b
			 WHERE A.New_State = state_update
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成STRAND update:after');
	
		INSERT INTO Temp_Pt_strand_Add
			(CITY_CODE,DATA_SOURCE,DATA_VERSION,DISTANCE,EDITION_FLAG,
			FIELD_TASK_ID,GEOMETRY,INCREASED_PRICE,INCREASED_STEP,
			LINE_ID,LOG,MEMO,NIDB_STRANDID,PAIR_STRAND_PID,
			PID,STATE,TASK_ID,TICKET_START,TICKET_SYS,
			TOTAL_PRICE,UPDATE_BATCH,UP_DOWN,
			U_FIELDS,U_RECORD)
			SELECT B.CITY_CODE,b.DATA_SOURCE,b.DATA_VERSION,b.DISTANCE,'外业修改',
			b.FIELD_TASK_ID,b.GEOMETRY,b.INCREASED_PRICE,b.INCREASED_STEP,
			(CASE
				WHEN Instr(A.Log, '改线路编号') > 0 or a.state=state_add THEN
				 A.LINE_ID
				ELSE
				 B.LINE_ID
				END) AS LINE_ID,
			a.LOG,b.MEMO,b.NIDB_STRANDID,
			b.PAIR_STRAND_PID,
			b.PID,
			a.STATE,b.TASK_ID,b.TICKET_START,b.TICKET_SYS,
			b.TOTAL_PRICE,b.UPDATE_BATCH,
			(CASE
				WHEN Instr(A.Log, '改上下行') > 0 or a.state=state_add THEN
				 A.UP_DOWN
				ELSE
				 B.UP_DOWN
				END) AS UP_DOWN,
			b.U_Fields,b.U_Record AS U_Record
				FROM Temp_Aupt_strand_fl a, Pt_strand b
			 WHERE A.New_State = state_update
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		--合并STRAND 
		Trace('合并STRAND del');
		DELETE FROM Pt_strand t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Aupt_strand_fl a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并STRAND add');
		INSERT INTO Pt_strand t
			SELECT *
				FROM Temp_pt_strand_add;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----        名称表
		---────────────────────────────────
	
		Trace('名称表del');
		INSERT INTO Temp_pt_strand_Name_Del
			SELECT B.*
				FROM Temp_Aupt_strand_fl a, Pt_strand_Name b
			 WHERE A.New_State = state_del
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表add');
		INSERT INTO Temp_pt_strand_name_Add
			(NAME_ID,NAME_GROUPID,PID,LANG_CODE,NAME_CLASS,NAME,PHONETIC,SRC_FLAG,
						U_Record,U_Fields)
			select b.NAME_ID,b.NAME_GROUPID,b.PID,b.LANG_CODE,b.NAME_CLASS,b.NAME,b.PHONETIC,b.SRC_FLAG,
						 0 AS U_Record, 0 AS U_Fields
				FROM Temp_Aupt_strand_fl a, Au_Pt_strand_Name b
			 WHERE A.New_State = state_add
						 AND A.Audata_Id = B.Audata_Id;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表update:del');
		INSERT INTO Temp_pt_strand_Name_Del
			SELECT B.*
				FROM Temp_Aupt_strand_fl a, Pt_strand_Name b
			 WHERE A.Pid = B.Pid
						 AND B.Lang_Code = 'CHI' and (A.state=state_add or (b.name_class=1 and Instr(a.Log, '改后缀名') > 0) or (b.name_class=2 and Instr(a.Log, '改头标') > 0))
						 AND A.New_State = state_update
						 AND A.Has_Update_Name = 1            
             ;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表update:add');
		INSERT INTO Temp_pt_strand_name_Add
			(Name_Id, Name_Groupid, Pid, Lang_Code,name_class, NAME, Phonetic, Src_Flag, U_Record, U_Fields)
			SELECT Name_Id, Name_Groupid, Pid, Lang_Code,name_class, NAME, Phonetic, Src_Flag, 0 AS U_Record,
						 0 AS U_Fields
				FROM (SELECT B.*
								 FROM Temp_Aupt_strand_fl a inner join Au_Pt_strand_Name b on A.Audata_Id = B.Audata_Id
                 left join Temp_pt_strand_Name_Del c on a.pid=c.pid
								WHERE A.New_State = state_update
                      AND B.Lang_Code = 'CHI' 
											AND A.Has_Update_Name = 1
                      and c.pid is null
                      AND B.NAME_CLASS IN(1,2)
               );
  INSERT INTO temp_pt_strand_name_add
      (name_id,
       name_groupid,
       pid,
       lang_code,
       name_class,
       NAME,
       phonetic,
       src_flag,
       u_record,
       u_fields) WITH rs AS
      (SELECT b.*,
              a.log        AS au_log,
              a.state      as au_state
         FROM temp_aupt_strand_fl a
        INNER JOIN au_pt_strand_name b
           ON a.audata_id = b.audata_id
         LEFT JOIN temp_pt_strand_name_del c
           ON a.pid = c.pid
        WHERE a.new_state = 3
          AND B.Lang_Code = 'CHI' 
          AND a.has_update_name = 1
          AND c.pid IS NOT NULL)
      SELECT  name_id,
             name_groupid,
             pid,
             lang_code,
              name_class,
              NAME,
              phonetic,
             src_flag,
             0            AS u_record,
             0            AS u_fields
        FROM rs
       WHERE au_state = state_add or (name_class = 1 AND instr(au_log, '改后缀名') > 0)
      UNION 
      SELECT name_id,
             name_groupid,
             pid,
             lang_code,
              name_class,
              NAME,
              phonetic,
             src_flag,
             0            AS u_record,
             0            AS u_fields
        FROM rs
       WHERE au_state = state_add or ( name_class = 2 AND instr(au_log, '改头标') > 0);

                    
                          
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表del');
		DELETE FROM Pt_strand_Name t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_pt_strand_Name_Del a
						 WHERE T.Name_Id = A.Name_Id);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表add');
		INSERT INTO Pt_strand_Name t
			SELECT *
				FROM Temp_pt_strand_Name_Add;
		Trace(SQL%ROWCOUNT);
    ---------
    --PT_STRAND_SCHEDULE
    ---------
   	Trace('PT_STRAND_SCHEDULE表del');
		INSERT INTO Temp_PT_STRAND_SCHEDULE_Del
			SELECT B.*
				FROM Temp_Aupt_strand_fl a, PT_STRAND_SCHEDULE b
			 WHERE A.New_State = state_del
						 AND A.Pid = B.STRAND_PID;
		Trace(SQL%ROWCOUNT);
    delete from PT_STRAND_SCHEDULE t 
    where exists(select 1 from Temp_PT_STRAND_SCHEDULE_Del a where t.strand_pid=a.strand_pid);
    ---────────────────────────────────
		----   pt_strand_platform表
		---────────────────────────────────
    Trace('pt_strand_platform表del');
    INSERT INTO temp_pt_strand_pl_del
      SELECT b.*
        FROM temp_aupt_strand_pl_fl a, pt_strand_platform b
       WHERE a.operator=b.operator
         AND a.state = state_del;
    Trace('pt_strand_platform表add');
    INSERT INTO temp_pt_strand_pl_add
      SELECT b.strand_pid,
             b.platform_pid,
             b.seq_num,
             b.INTERVAL,
             b.operator,
             b.update_time,
             b.log,
             '外业修改' as editionflag,
             b.state,
             b.data_source,
             b.update_batch,
             0            AS u_record,
             NULL         AS u_fields
        FROM temp_aupt_strand_pl_fl a, au_pt_strand_platform b
       WHERE a.operator = b.operator
         AND a.state = state_add;
         
    
     Trace('pt_strand_platform表update:add');
    
    INSERT INTO temp_pt_strand_pl_add
      (strand_pid,
       platform_pid,
       seq_num,
       INTERVAL,
       operator,
       update_time,
       log,
       editionflag,
       state,
       data_source,
       update_batch,
       u_record,
       u_fields) WITH rs AS
      (SELECT a.*,
              b.strand_pid   AS au_strand_pid,
              b.platform_pid AS au_platform_pid,
              b.seq_num      AS au_seq_num,
              b.interval     AS au_interval,
              a.log          AS au_log
         FROM temp_aupt_strand_pl_fl a
        INNER JOIN au_pt_strand_platform b
           ON a.operator = b.operator         
        WHERE a.new_state = state_update)
      SELECT case when Instr(rs.Log, '改STRAND编号') > 0 then au_strand_pid else strand_pid end  as strand_pid,
             case when Instr(rs.Log, '改站台编号') > 0 then au_platform_pid else platform_pid end as platform_pid,
             case when Instr(rs.Log, '改站台顺序') > 0 then au_seq_num  else seq_num end as seq_num,
             case when Instr(rs.Log, '改时间间隔') > 0 then au_INTERVAL else "INTERVAL" end as "INTERVAL",
             operator,
             update_time,
             log,
             '外业修改' AS editionflag ,
             state,
             data_source,
             update_batch,
             0 as u_record,
             null as u_fields
        FROM rs;     
     Trace('pt_strand_platform表SEQ_NUM排序');
     --先将内业中要删除的删掉
     delete from Pt_strand_platform t where 
     t.operator in(select operator from temp_pt_strand_pl_del);
     --排序   
     Sort_Pt_Strand_Platform(V_Task_Id);
     delete from temp_pt_strand_pl_add;
     insert into temp_pt_strand_pl_add
      select  b.strand_pid,
             b.platform_pid,
             b.seq_num,
             b.INTERVAL,
             b.operator,
             b.update_time,
             b.log,
             b.editionflag,
             b.state,
             b.data_source,
             b.update_batch,
             u_record,
             u_fields from  temp_pt_strand_pl_sort b ;
     Trace('pt_strand_platform表update:del');
     INSERT INTO temp_pt_strand_pl_del
       SELECT b.*
         FROM pt_strand_platform b
        WHERE b.strand_pid IN
              (SELECT strand_pid
                 FROM temp_pt_strand_pl_sort)
             ;
		Trace(SQL%ROWCOUNT);
        
    --合并Pt_strand_platform
    delete from Pt_strand_platform t where 
    t.operator in(select operator from temp_pt_strand_pl_del);
    

    insert into Pt_strand_platform select * from temp_pt_strand_pl_add;
   

		---────────────────────────────────
		----        TRANSFER 表
		---────────────────────────────────
	
		Trace('生成TRANSFER表del');
		INSERT INTO Temp_Ptp_Tf_Del
			SELECT B.*
				FROM Temp_Aupt_Tf_Filter a, Pt_Transfer b
			 WHERE A.New_State = state_del
						 AND A.Transfer_Id = B.Transfer_Id;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成TRANSFER表add');
		INSERT INTO Temp_Ptp_Tf_Add
			(Transfer_Id, Transfer_Type, Poi_Fir, Poi_Sec, Platform_Fir, Platform_Sec, City_Code,
			 Transfer_Time, External_Flag, Operator, Update_Time, Log, Edition_Flag, State, U_Record,
			 U_Fields)
			SELECT Transfer_Id, Transfer_Type, Poi_Fir, Poi_Sec, Platform_Fir, Platform_Sec, City_Code,
						 Transfer_Time, External_Flag, Operator, Update_Time, Log, Edition_Flag, State,
						 0 AS U_Record, NULL AS U_Fields
				FROM Temp_Aupt_Tf_Filter
			 WHERE New_State = state_add;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成TRANSFER表update:before');
		INSERT INTO Temp_Ptp_Tf_Del
			SELECT B.*
				FROM Temp_Aupt_Tf_Filter a, Pt_Transfer b
			 WHERE A.New_State = state_update
						 AND A.Transfer_Id = B.Transfer_Id;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成TRANSFER表update:after');
	
		INSERT INTO Temp_Ptp_Tf_Add
			(Transfer_Id, Transfer_Type, Poi_Fir, Poi_Sec, Platform_Fir, Platform_Sec, City_Code,
			 Transfer_Time, External_Flag, Operator, Update_Time, Log, Edition_Flag, State, U_Record,
			 U_Fields)
			SELECT B.Transfer_Id,
						 (CASE
								WHEN Instr(A.Log, '改换乘类型') > 0 or a.state=state_add THEN
								 A.Transfer_Type
								ELSE
								 B.Transfer_Type
							END) AS Transfer_Type,
						 (CASE
								WHEN Instr(A.Log, '改换乘点ID1') > 0 or a.state=state_add THEN
								 (CASE
                            WHEN Instr(A.Log, '改换乘类型') > 0 or a.state=state_add THEN
                             decode(A.Transfer_Type,0,A.Poi_Fir,0)
                            ELSE
                             decode(b.Transfer_Type,0,A.Poi_Fir,0)
                          END)
								ELSE
								 B.Poi_Fir
							END) AS Poi_Fir,
						 (CASE
								WHEN Instr(A.Log, '改换乘点ID2') > 0 or a.state=state_add THEN
								 (CASE
                            WHEN Instr(A.Log, '改换乘类型') > 0 or a.state=state_add THEN
                             decode(A.Transfer_Type,0,A.Poi_Sec,0)
                            ELSE
                             decode(b.Transfer_Type,0,A.Poi_Sec,0)
                          END)
								ELSE
								 B.Poi_Sec
							END) AS Poi_Sec,
              --
              
						 (CASE
								WHEN Instr(A.Log, '改换乘点ID1') > 0 or a.state=state_add THEN
								 (CASE
                            WHEN Instr(A.Log, '改换乘类型') > 0 or a.state=state_add THEN
                             decode(A.Transfer_Type,1,A.Platform_Fir,0)
                            ELSE
                             decode(b.Transfer_Type,1,A.Platform_Fir,0)
                          END)
								ELSE
								 B.Platform_Fir
							END) AS Platform_Fir,
						 (CASE
								WHEN Instr(A.Log, '改换乘点ID2') > 0 or a.state=state_add THEN
								 (CASE
                            WHEN Instr(A.Log, '改换乘类型') > 0 or a.state=state_add THEN
                             decode(A.Transfer_Type,1,A.Platform_Sec,0)
                            ELSE
                             decode(b.Transfer_Type,1,A.Platform_Sec,0)
                          END)
								ELSE
								 B.Platform_Sec
							END) AS Platform_Sec,
                B.City_Code,
						 (CASE
								WHEN Instr(A.Log, '改换乘时间') > 0 or a.state=state_add THEN
								 A.Transfer_Time
								ELSE
								 B.Transfer_Time
							END) AS Transfer_Time,
						 (CASE
								WHEN Instr(A.Log, '改外部标识') > 0 or a.state=state_add THEN
								 A.External_Flag
								ELSE
								 B.External_Flag
							END) AS External_Flag, B.Operator, B.Update_Time, A.Log, '外业修改', A.State,
						 B.U_Record, B.U_Fields
				FROM Temp_Aupt_Tf_Filter a, Pt_Transfer b
			 WHERE A.New_State = state_update
						 AND A.Transfer_Id = B.Transfer_Id;
	
		Trace(SQL%ROWCOUNT);
	
		--合并TRANSFER表
		Trace('合并TRANSFER表del');
		DELETE FROM Pt_Transfer t
		 WHERE EXISTS (SELECT Transfer_Id
							FROM Temp_Ptp_Tf_Del a
						 WHERE T.Transfer_Id = A.Transfer_Id);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并TRANSFER表add');
		INSERT INTO Pt_Transfer t
			SELECT *
				FROM Temp_Ptp_Tf_Add;
		Trace(SQL%ROWCOUNT);
	
		/*Trace('合并TRANSFER表update:del');
		DELETE FROM Pt_Transfer t
		 WHERE EXISTS (SELECT Transfer_Id
							FROM Temp_Ptp_Tf_Update_Del a
						 WHERE T.Transfer_Id = A.Transfer_Id);
		Trace(SQL%ROWCOUNT);
	
		Trace('Pt_Transfer:after');
		INSERT INTO Pt_Transfer t
			SELECT *
				FROM Temp_Ptp_Tf_Update_Add;
		Trace(SQL%ROWCOUNT);*/
	
		---────────────────────────────────
		----        access 表
		---- 对于PT_PLATFORM_ACCESS表，将子版本中此表中相同PLATFORM_ID的记录删除，同时将子版本中此表中相同ACCESS_ID的记录删除，然后将PT_PLATFORM_ACCESS表所有记录全部插入。（这张表没有主键，故参照其外键入库，考虑到这个表中记录不全，故使用外键所在的主表来删除记录，比较稳妥）
		---────────────────────────────────
		INSERT INTO Temp_Ptp_Access_Del
			SELECT B.*
				FROM Temp_Aupt_Access_Filter a, Pt_Platform_Access b
			 WHERE A.Platform_Id = B.Platform_Id
			UNION
			SELECT B.*
				FROM Temp_Aupt_Access_Filter a, Pt_Platform_Access b
			 WHERE A.Access_Id = B.Access_Id;
	
		INSERT INTO Temp_Ptp_Access_Add
			SELECT Relate_Id, Platform_Id, Access_Id, Available, State, 0 AS U_Record, NULL AS U_Fields
				FROM Temp_Aupt_Access_Filter;
        
    delete from PT_PLATFORM_ACCESS t
    where t.relate_id in (select relate_id from Temp_Ptp_Access_Del);
    
    insert into PT_PLATFORM_ACCESS
    select * from Temp_Ptp_Access_Add;
    -- 公交公司表
        
    Trace('生成pt_company del');
		INSERT INTO temp_pt_company_Del
			SELECT B.*
				FROM Temp_Aupt_company_fl a, pt_company b
			 WHERE A.New_State = state_del
						 AND A.COMPANY_ID = B.COMPANY_ID;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成pt_company add');
		INSERT INTO temp_pt_company_Add
      (COMPANY_ID,NAME,PHONETIC,NAME_ENG_SHORT,NAME_ENG_FULL,SRC_FLAG,CITY_CODE,LOG,EDITION_FLAG,
      STATE,  DATA_SOURCE,  UPDATE_BATCH,  NIDB_COMPANYID,  U_RECORD,  U_FIELDS)
			SELECT 
      COMPANY_ID,NAME,PHONETIC,NAME_ENG_SHORT,NAME_ENG_FULL,SRC_FLAG,CITY_CODE,LOG,EDITION_FLAG,
      STATE,  DATA_SOURCE,  UPDATE_BATCH,  NIDB_COMPANYID,  0,  0
				FROM Temp_Aupt_company_fl
			 WHERE New_State = state_add;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成pt_company update:before');
		INSERT INTO temp_pt_company_Del
			SELECT B.*
				FROM Temp_Aupt_company_fl a, pt_company b
			 WHERE A.New_State = state_update
						 AND A.COMPANY_ID = B.COMPANY_ID;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成pt_company update:after');
	
		INSERT INTO temp_pt_company_Add
			(COMPANY_ID,NAME,PHONETIC,NAME_ENG_SHORT,NAME_ENG_FULL,SRC_FLAG,CITY_CODE,LOG,EDITION_FLAG,
      STATE,  DATA_SOURCE,  UPDATE_BATCH,  NIDB_COMPANYID,  U_RECORD,  U_FIELDS)
			SELECT B.COMPANY_ID,
						 (CASE
								WHEN Instr(A.Log, '改公司名称') > 0 or a.state=state_add  THEN
								 A.NAME
								ELSE
								 B.NAME
							END) AS NAME,
						(CASE
								WHEN Instr(A.Log, '改公司名称') > 0 or a.state=state_add  THEN
								 A.PHONETIC
								ELSE
								 B.PHONETIC
							END) AS PHONETIC,
						 (CASE
								WHEN Instr(A.Log, '改公司外文名简称') > 0 or a.state=state_add  THEN
								 A.NAME_ENG_SHORT
								ELSE
								 B.NAME_ENG_SHORT
							END) AS NAME_ENG_SHORT,
						 (CASE
								WHEN Instr(A.Log, '改公司外文名全称') > 0 or a.state=state_add  THEN
								 A.NAME_ENG_FULL
								ELSE
								 B.NAME_ENG_FULL
							END) AS NAME_ENG_FULL,
        b.SRC_FLAG,b.CITY_CODE,a.LOG,'外业修改',
        a.STATE,  b.DATA_SOURCE,  b.UPDATE_BATCH,  b.NIDB_COMPANYID,  b.U_RECORD,  b.U_FIELDS
            
				FROM Temp_Aupt_company_fl a, pt_company b
			 WHERE A.New_State = state_update
						 AND A.COMPANY_ID = B.COMPANY_ID;
		Trace(SQL%ROWCOUNT);    
        
    delete pt_company t where t.company_id in (select company_id from temp_pt_company_del);
    
    insert into  pt_company select * from temp_pt_company_Add;   
            
    
    -- 公交系统表
        
    Trace('生成pt_system del');
		INSERT INTO temp_pt_system_Del
			SELECT B.*
				FROM Temp_Aupt_system_fl a, pt_system b
			 WHERE A.New_State = state_del
						 AND A.SYSTEM_ID = B.SYSTEM_ID;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成pt_system add');
		INSERT INTO temp_pt_system_Add
      (SYSTEM_ID,COMPANY_ID,NAME,PHONETIC,NAME_ENG_SHORT,NAME_ENG_FULL,SRC_FLAG,CITY_CODE,LOG,
      EDITION_FLAG,STATE,DATA_SOURCE,UPDATE_BATCH,NIDB_SYSTEMID,U_RECORD,U_FIELDS)
			SELECT 
      SYSTEM_ID,COMPANY_ID,NAME,PHONETIC,NAME_ENG_SHORT,NAME_ENG_FULL,SRC_FLAG,CITY_CODE,LOG,
      EDITION_FLAG,STATE,DATA_SOURCE,UPDATE_BATCH,NIDB_SYSTEMID,0,0
				FROM Temp_Aupt_system_fl
			 WHERE New_State = state_add;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成pt_system update:before');
		INSERT INTO temp_pt_system_Del
			SELECT B.*
				FROM Temp_Aupt_system_fl a, pt_system b
			 WHERE A.New_State = state_update
						 AND A.SYSTEM_ID = B.SYSTEM_ID;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成pt_system update:after');
	
		INSERT INTO temp_pt_system_Add
			(SYSTEM_ID,COMPANY_ID,NAME,PHONETIC,NAME_ENG_SHORT,NAME_ENG_FULL,SRC_FLAG,CITY_CODE,LOG,
      EDITION_FLAG,STATE,DATA_SOURCE,UPDATE_BATCH,NIDB_SYSTEMID,U_RECORD,U_FIELDS)
			SELECT B.SYSTEM_ID,
						 (CASE
								WHEN Instr(A.Log, '改所属公司编号') > 0 or a.state=state_add  THEN
								 A.COMPANY_ID
								ELSE
								 B.COMPANY_ID
							END) AS COMPANY_ID,
						(CASE
								WHEN Instr(A.Log, '改系统名称') > 0 or a.state=state_add  THEN
								 A.NAME
								ELSE
								 B.NAME
							END) AS NAME,
						 (CASE
								WHEN Instr(A.Log, '改系统名称') > 0 or a.state=state_add  THEN
								 A.PHONETIC
								ELSE
								 B.PHONETIC
							END) AS PHONETIC,
						 (CASE
								WHEN Instr(A.Log, '改系统外文名简称') > 0 or a.state=state_add  THEN
								 A.NAME_ENG_SHORT
								ELSE
								 B.NAME_ENG_SHORT
							END) AS NAME_ENG_SHORT,
						 (CASE
								WHEN Instr(A.Log, '改系统外文名全称') > 0 or a.state=state_add  THEN
								 A.NAME_ENG_FULL
								ELSE
								 B.NAME_ENG_FULL
							END) AS NAME_ENG_FULL,
        b.SRC_FLAG,b.CITY_CODE,a.LOG,'外业修改',
        a.STATE,  b.DATA_SOURCE,  b.UPDATE_BATCH,  b.NIDB_SYSTEMID,  b.U_RECORD,  b.U_FIELDS            
				FROM Temp_Aupt_system_fl a, pt_system b
			 WHERE A.New_State = state_update
						 AND A.SYSTEM_ID = B.SYSTEM_ID;
		Trace(SQL%ROWCOUNT); 
     
    delete pt_system t where t.company_id in (select company_id from temp_pt_system_del);
    
    insert into  pt_system select * from temp_pt_system_Add;   
       
		Trace('生成履历');
	
		--生成主表履历
		Pk_History_Util.Generate_Del_History('pt_platform', 'select a.* from Temp_ptp_del a');
		Trace(SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_platform', 'select * from Temp_ptp_add');
		Trace(SQL%ROWCOUNT);
    
    
		--生成名称表履历
		Pk_History_Util.Generate_Del_History('pt_platform_name', 'select a.* from Temp_ptp_name_del a');
		Trace('[生成履历] DEL NAME：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_platform_name', 'select * from Temp_ptp_name_add');
		Trace('[生成履历] ADD NAME：' || SQL%ROWCOUNT);
     
		
	  -- strand --
		--生成主表履历
		Pk_History_Util.Generate_Del_History('pt_strand', 'select a.* from Temp_pt_strand_del a');
		Trace(SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_strand', 'select * from Temp_pt_strand_add');
		Trace(SQL%ROWCOUNT);
    
    
		--生成名称表履历
		Pk_History_Util.Generate_Del_History('pt_strand_name', 'select a.* from Temp_pt_strand_name_del a');
		Trace('[生成履历] DEL NAME：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_strand_name', 'select * from Temp_pt_strand_name_add');
		Trace('[生成履历] ADD NAME：' || SQL%ROWCOUNT);
		
    --Temp_PT_STRAND_SCHEDULE_Del
    Pk_History_Util.Generate_Del_History('PT_STRAND_SCHEDULE', 'select a.* from Temp_PT_STRAND_SCHEDULE_Del a');
		Trace('[生成履历] DEL PT_STRAND_SCHEDULE：' || SQL%ROWCOUNT);
    
		--TRANSFER表
		Pk_History_Util.Generate_Del_History('Pt_Transfer', 'select a.* from Temp_ptp_tf_del a');
		Trace(SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('Pt_Transfer', 'select * from Temp_ptp_tf_add');
		Trace(SQL%ROWCOUNT);
	
		--access 表
	
		Pk_History_Util.Generate_Del_History('pt_platform_access', 'select a.* from temp_ptp_access_del a');
		Trace(SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_platform_access', 'select * from temp_ptp_access_add');
		Trace(SQL%ROWCOUNT);
    
    /*update Temp_Merge_Raw_Operate_Log t 
    set t.object_id=(select a.poi_pid from pt_platform a where a.pid=t.object_id)
    where t.table_name='PT_PLATFORM_ACCESS';
    */
    --   pt_strand_platform
	
		Pk_History_Util.Generate_Del_History('pt_strand_platform', 'select a.* from temp_pt_strand_pl_del a');
		Trace(SQL%ROWCOUNT);

	 
		Pk_History_Util.Generate_Add_History('pt_strand_platform', 'select * from temp_pt_strand_pl_add');

		Trace(SQL%ROWCOUNT);
    
    --公交公司表
    
		Pk_History_Util.Generate_Del_History('pt_company', 'select a.* from temp_pt_company_Del a');
		Trace(SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_company', 'select * from temp_pt_company_add');
		Trace(SQL%ROWCOUNT);
    
    --公交系统表   
    
		Pk_History_Util.Generate_Del_History('pt_system', 'select a.* from temp_pt_system_Del a');
		Trace(SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('pt_system', 'select * from temp_pt_system_add');
		Trace(SQL%ROWCOUNT);
    
    --公交线路表
	
		---────────────────────────────────
		----    外业表
		---────────────────────────────────
	
		Trace('生成外业表0->1作业履历');
	
		Pk_History_Util.Generate_Au_Work_History('au_pt_platform', 'select a.* from au_pt_platform a, Temp_Aupt_filter b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_Transfer', 'select a.* from au_Pt_Transfer a, Temp_Aupt_tf_filter b where a.transfer_id=b.transfer_id', 'T', V_Merge_Geo, 0, 1);
		Trace('au_pt_Transfer:'||SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_platform_access', 'select distinct a.* from au_pt_platform_access a, Temp_Aupt_access_filter b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_company', 'select a.* from au_pt_company a, Temp_Aupt_company_fl b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_system', 'select a.* from au_pt_system a, Temp_Aupt_system_fl b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_strand', 'select a.* from au_pt_strand a, Temp_Aupt_strand_fl b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);    
    Pk_History_Util.Generate_Au_Work_History('au_pt_strand_platform', 'select a.* from au_pt_strand_platform a, Temp_Aupt_strand_pl_fl b where a.operator=b.operator', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成外业表1->2作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_pt_platform', 'select a.* from au_pt_platform a, Temp_Aupt_filter b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_Transfer', 'select a.* from au_Pt_Transfer a, Temp_Aupt_tf_filter b where a.transfer_id=b.transfer_id', 'T', V_Merge_Geo, 1, 2);
		Trace('au_pt_Transfer:'||SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_platform_access', 'select distinct a.* from au_pt_platform_access a, Temp_Aupt_access_filter b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_company', 'select a.* from au_pt_company a, Temp_Aupt_company_fl b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_system', 'select a.* from au_pt_system a, Temp_Aupt_system_fl b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_strand', 'select a.* from au_pt_strand a, Temp_Aupt_strand_fl b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
    Pk_History_Util.Generate_Au_Work_History('au_pt_strand_platform', 'select a.* from au_pt_strand_platform a, Temp_Aupt_strand_pl_fl b where a.operator=b.operator', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成外业表1->0作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_pt_platform', 'select a.* from au_pt_platform a, Temp_Aupt_error b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_Transfer', 'select a.* from au_Pt_Transfer a, Temp_Aupt_tf_error b where a.transfer_id=b.transfer_id', 'T', V_Merge_Geo, 1, 0);
		Trace('au_pt_Transfer:'||SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_platform_access', 'select a.* from au_pt_platform_access a, Temp_Aupt_access_error b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_company', 'select a.* from au_pt_company a, Temp_Aupt_company_err b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_system', 'select a.* from au_pt_system a, Temp_Aupt_system_err b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
		Pk_History_Util.Generate_Au_Work_History('au_pt_strand', 'select a.* from au_pt_strand a, Temp_Aupt_strand_err b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
    	Pk_History_Util.Generate_Au_Work_History('au_pt_strand_platform', 'select a.* from au_pt_strand_platform a, Temp_Aupt_strand_pl_err b where a.operator=b.operator', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
	
		Trace('更新外业表0->1');
		UPDATE Au_Pt_Platform t
			 SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Aupt_Filter a
						 WHERE A.Pid = T.Pid);
		Trace(SQL%ROWCOUNT);
    
		UPDATE Au_Pt_Transfer t
			 SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Aupt_Tf_Filter a
						 WHERE A.Transfer_Id = T.Transfer_Id);
		Trace(SQL%ROWCOUNT);
    
	/*	UPDATE Au_Pt_Platform_Access t
			 SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Aupt_Access_Filter a
						 WHERE T.Audata_Id = A.Audata_Id);
		Trace(SQL%ROWCOUNT);*/
    
		UPDATE Au_Pt_Company t SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1 FROM Temp_Aupt_company_fl a WHERE T.Audata_Id = A.Audata_Id);
		Trace(SQL%ROWCOUNT);
    
		UPDATE Au_Pt_System t SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1 FROM Temp_Aupt_system_fl a WHERE T.Audata_Id = A.Audata_Id);
		Trace(SQL%ROWCOUNT);
	  
    
		UPDATE Au_Pt_strand t
			 SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Aupt_strand_fl a
						 WHERE A.Pid = T.Pid);
		Trace(SQL%ROWCOUNT);
    
		UPDATE Au_Pt_strand_Platform t
			 SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Aupt_strand_pl_fl a
						 WHERE T.Operator = A.Operator);
		Trace(SQL%ROWCOUNT);
    
		Trace('将履历写入履历库');
		INSERT INTO Operate_Log
			SELECT *
				FROM Temp_Merge_Raw_Operate_Log;
		Trace(SQL%ROWCOUNT);
	END;

	PROCEDURE Delete_Temp_Data IS
	BEGIN
	
		EXECUTE IMMEDIATE 'delete from  Temp_Merge_Raw_Operate_Log';
    
		DELETE FROM Temp_Aupt_Pl_Tk;
    DELETE FROM Temp_Aupt_Tf_Tk;
    DELETE FROM Temp_Aupt_Access_Tk;
    DELETE FROM Temp_Aupt_Filter;
    DELETE FROM Temp_Aupt_Tf_Filter;
    DELETE FROM Temp_Aupt_Access_Filter;
    DELETE FROM Temp_Aupt_Unchg;
    DELETE FROM Temp_Aupt_Error;
    DELETE FROM Temp_Ptp_Del;
    DELETE FROM Temp_Ptp_Add;
    DELETE FROM Temp_Ptp_Name_Del;
    DELETE FROM Temp_Ptp_Name_Add;
    DELETE FROM Temp_Aupt_Tf_Unchg;
    DELETE FROM Temp_Aupt_Tf_Error;
    DELETE FROM Temp_Ptp_Tf_Del;
    DELETE FROM Temp_Ptp_Tf_Add;
    DELETE FROM Temp_Aupt_Access_Unchg;
    DELETE FROM Temp_Aupt_Access_Error;
    DELETE FROM Temp_Ptp_Access_Del;
    DELETE FROM Temp_Ptp_Access_Add;
    DELETE FROM Temp_Aupt_Company_Tk;
    DELETE FROM Temp_Aupt_Company_Err;
    DELETE FROM Temp_Aupt_Company_Fl;
    DELETE FROM Temp_Aupt_Company_Un;
    DELETE FROM Temp_Pt_Company_Del;
    DELETE FROM Temp_Pt_Company_Add;
    DELETE FROM Temp_Aupt_System_Tk;
    DELETE FROM Temp_Aupt_System_Un;
    DELETE FROM Temp_Aupt_System_Err;
    DELETE FROM Temp_Aupt_System_Fl;
    DELETE FROM Temp_Pt_System_Del;
    DELETE FROM Temp_Pt_System_Add;
    DELETE FROM Temp_Aupt_Strand_Tk;
    DELETE FROM Temp_Aupt_Strand_Un;
    DELETE FROM Temp_Aupt_Strand_Err;
    DELETE FROM Temp_Aupt_Strand_Fl;
    DELETE FROM Temp_Pt_Strand_Del;
    DELETE FROM Temp_Pt_Strand_Add;
    DELETE FROM Temp_Aupt_Strand_pl_Tk;
    DELETE FROM Temp_Aupt_Strand_pl_Un;
    DELETE FROM Temp_Aupt_Strand_pl_Fl;
    DELETE FROM Temp_Pt_Strand_pl_Del;
    DELETE FROM Temp_Pt_Strand_pl_add;
    DELETE FROM Temp_Pt_Strand_Name_Del;
    DELETE FROM Temp_Pt_Strand_Name_Add;
    
    DELETE FROM Temp_Aupt_Strand_pl_tk;
    DELETE FROM Temp_Aupt_Strand_pl_un;
    DELETE FROM Temp_Aupt_Strand_pl_err;
    DELETE FROM Temp_Aupt_Strand_pl_fl;    
    DELETE FROM Temp_Pt_Strand_pl_del;        
    DELETE FROM Temp_Pt_Strand_pl_add;  
    
    delete from temp_pt_strand_pl_sort;          
    

	END;
	PROCEDURE Loop_Do_Merge(V_Field_Task_Id VARCHAR2,
													V_Task_Id       VARCHAR2,
													V_Merge_Att     VARCHAR2 := 'F',
													V_Merge_Geo     VARCHAR2 := 'F') IS
	BEGIN
		Trace('处理外业任务号:' || V_Field_Task_Id);
	
		Delete_Temp_Data;
	
		
		INSERT INTO Temp_Aupt_pl_tk
			SELECT *
				FROM Au_Pt_Platform t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace('Au_Pt_Platform 任务数据量:'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_Tf_tk
			SELECT *
				FROM Au_Pt_Transfer t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;             
		Trace('Au_Pt_Transfer 任务数据量:'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_Access_tk
			SELECT *
				FROM Au_Pt_Platform_Access t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace('Au_Pt_Platform_Access 任务数据量:'||SQL%ROWCOUNT);
	
    
		INSERT INTO Temp_Aupt_company_tk
			SELECT *
				FROM au_pt_company t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace('au_pt_company 任务数据量:'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_system_tk
			SELECT *
				FROM au_pt_system t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace('au_pt_system 任务数据量:'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_strand_tk
			SELECT *
				FROM Au_pt_strand t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace('Au_pt_strand 任务数据量:'||SQL%ROWCOUNT);
    
		INSERT INTO Temp_Aupt_Strand_pl_tk
			SELECT *
				FROM Au_Pt_Strand_Platform t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace('Au_Pt_Strand_Platform 任务数据量:'||SQL%ROWCOUNT);
    
    
		IF V_Merge_Att = 'F' and V_Merge_Geo = 'F'
		THEN
			Raise_Application_Error(-20999, '融合选项无效');
		ELSE
			Merge_Att(V_Task_Id);
		END IF;
	
	END;

	PROCEDURE Do_Merge(V_Task_Id   VARCHAR2,
										 V_Merge_Att VARCHAR2 := 'F',
										 V_Merge_Geo VARCHAR2 := 'F') IS
		V_Field_Task_Id_Cnt INTEGER := 0;
		V_Pid_In_Task_Cnt   INTEGER := 0;
		V_Pid_Same          INTEGER := 0;
	BEGIN
    if V_Merge_Att='F' then    
       trace('作业类型为几何，不处理，直接返回。');  
       return;
    end if;
		Pk_History_Util.V_Seq_Id             := Pk_History_Util.V_Seq_Id + 1;
		Pk_History_Util.V_Default_Task_Id    := V_Task_Id;
		Pk_History_Util.V_Default_Operate_Id := Pk_History_Util.V_Default_Operate_Id + 1;
	
		/*Trace('检查外业数据');
    SELECT COUNT(1)
      INTO V_Pid_In_Task_Cnt
      FROM (SELECT COUNT(Pid)
               FROM pt_platform t
              GROUP BY Field_Task_Id
             HAVING COUNT(Pid) > 1);
    
    IF V_Pid_In_Task_Cnt >= 1
    THEN
      Raise_Application_Error(-20999, '数据错误：在同一个外业任务中有相同pid');
    END IF;
    
    SELECT COUNT(1)
      INTO V_Pid_Same
      FROM (SELECT COUNT(Pid)
               FROM Au_Ix_Pointaddress t
              GROUP BY Pid
             HAVING COUNT(Pid) > 1);*/
	
		IF V_Pid_Same = 0
		THEN
			Loop_Do_Merge(NULL, V_Task_Id, V_Merge_Att, V_Merge_Geo);
		ELSE
			FOR Rec IN (SELECT Field_Task_Id
										FROM (SELECT Field_Task_Id, MIN(Imp_Date) Min_Imp_Date
														 FROM Au_Pt_Platform t
														GROUP BY Field_Task_Id)
									 ORDER BY Min_Imp_Date)
			LOOP
				Loop_Do_Merge(Rec.Field_Task_Id, V_Task_Id, V_Merge_Att, V_Merge_Geo);
			
			END LOOP;
		END IF;
	END;
END Pk_Merge_Pt;
/
