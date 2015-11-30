CREATE OR REPLACE PACKAGE Pk_Merge_Pa IS
  function merge_log(v_log varchar2) return varchar2;
  function merge_state(v_state varchar2) return varchar2;
	FUNCTION Parselog(Log   VARCHAR2,
										V_Att NUMBER,
										V_Geo NUMBER) RETURN VARCHAR2;

	PROCEDURE Do_Merge(V_Task_Id   VARCHAR2,
										 V_Merge_Att VARCHAR2 := 'F',
										 V_Merge_Geo VARCHAR2 := 'F');
END Pk_Merge_Pa;
/
CREATE OR REPLACE PACKAGE BODY Pk_Merge_Pa IS

	PROCEDURE Trace(Msg VARCHAR2) IS
	BEGIN
	
		--Dbms_Output.Put_Line(To_Char(SYSDATE, 'yyyy-mm-dd hh24:mi:ss') || ': ' || Substr(Msg, 1, 500));
		Logger.Trace(Msg, '点门牌融合程序');
	END;

	FUNCTION Parselog(Log   VARCHAR2,
										V_Att NUMBER,
										V_Geo NUMBER) RETURN VARCHAR2 IS
		Vs VARCHAR2(200);
	BEGIN
	
		Vs := '<LOG/><STATE/>';
		IF V_Att = 1 AND Nvl(Instr(Log, '改DPRN'), 1) > 0 --log 为空时表示修改全部字段
		THEN
			Vs := Vs || '<DPR_NAME/>';
		END IF;
		IF V_Att = 1 AND Nvl(Instr(Log, '改DPN'), 1) > 0
		THEN
			Vs := Vs || '<DP_NAME/>';
		END IF;
		IF V_Att = 1 AND Nvl(Instr(Log, '改标注'), 1) > 0
		THEN
			Vs := Vs || '<MEMOIRE/>';
		END IF;
	
		IF V_Geo = 1 AND Nvl(Instr(Log, '改REL'), 1) > 0
		THEN
			Vs := Vs || '<GEOMETRY/><MESH_ID/><REGION_ID/>';
		END IF;
		IF V_Geo = 1 AND ( Nvl(Instr(Log, '改GUIDEX'), 1) > 0 OR Nvl(Instr(Log, '改GUIDEY'), 1) > 0 )
		THEN
			Vs := Vs || '<GUIDE_LINK_PID/><GUIDE_LINK_SIDE/><X_GUIDE/><Y_GUIDE/>';
		END IF;
		RETURN Vs;
	END;

	--属性融合或属性加几何
	PROCEDURE Merge_Att(V_Task_Id   VARCHAR2,
											V_Merge_Geo VARCHAR2 := 'F' /*是否同时融几何*/) IS
	BEGIN
		Trace('过滤已作业的数据');
		INSERT INTO Temp_Mpa_Unchanged --过滤已作业的数据
			SELECT *
				FROM Temp_Pa
			 WHERE Att_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace(SQL%ROWCOUNT);
	
    
		Trace('过滤删除、修改但在母库中不存在的数据');
		INSERT INTO Temp_Mpa_Error
			SELECT T.*
				FROM Temp_Pa t
				LEFT JOIN Ix_Pointaddress b
					ON T.Pid = B.Pid
			 WHERE T.State IN (1, 2)
						 AND T.Att_Oprstatus = 0
						 AND B.Pid IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id          
																			 FROM Temp_Mpa_Unchanged);
		Trace(SQL%ROWCOUNT);
	
    
		Trace('确定需要融合的数据');
		INSERT INTO Temp_Mpa_Au --其它数据需要融合
			SELECT T.AUDATA_ID,
            T.PID,
            T.GEOMETRY,
            T.X_GUIDE,
            T.Y_GUIDE,
            T.GUIDE_LINK_PID,
            T.LOCATE_LINK_PID,
            T.LOCATE_NAME_GROUPID,
            T.GUIDE_LINK_SIDE,
            T.LOCATE_LINK_SIDE,
            T.SRC_PID,
            T.REGION_ID,
            T.MESH_ID,
            T.MESH_ID_2K,
            T.EDIT_FLAG,
            T.IDCODE,
            T.DPR_NAME,
            T.DP_NAME,
            T.OPERATOR,
            T.MEMOIRE,
            T.DPF_NAME,
            T.POSTER_ID,
            T.ADDRESS_FLAG,
            T.VERIFED,
            T.STATE,
            T.LOG,
            T.MEMO,
            T.RESERVED,
            T.DATA_VERSION,
            T.GEO_TASK_ID,
            T.ATT_TASK_ID,
            T.FIELD_TASK_ID,
            T.GEO_OPRSTATUS,
            T.GEO_CHECKSTATUS,
            T.ATT_OPRSTATUS,
            T.ATT_CHECKSTATUS,
            T.IMP_DATE, A.Child_Pa_Pid, A.Parent_Pa_Pid, A.Group_Id, B.Pid AS Main_Pid,
						 (CASE
								WHEN t.address_flag=0 
                  and ( Instr(T.Log, 'DPRNAME') > 0
                  or Instr(T.Log, '改DPNAME') > 0 ) THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Name,
						 (CASE
								WHEN Instr(T.Log, '改FATHERSON') > 0 THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Fatherson, T.State AS New_State
				FROM Temp_Pa t
			
				LEFT JOIN (SELECT DISTINCT B.Child_Pa_Pid, A.Parent_Pa_Pid, A.Group_Id
										 FROM Au_Ix_Pointaddress_Parent a, Au_Ix_Pointaddress_Children b
										WHERE A.Group_Id = B.Group_Id and (V_Task_Id is null or b.field_task_id=V_Task_Id) ) a
					ON T.Pid = A.Child_Pa_Pid
				LEFT JOIN Ix_Pointaddress b
					ON T.Pid = B.Pid
				LEFT JOIN Temp_Mpa_Unchanged c
					ON T.Audata_Id = C.Audata_Id
				LEFT JOIN Temp_Mpa_Error d
					ON T.Audata_Id = D.Audata_Id
			 WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL;
		Trace(SQL%ROWCOUNT);
	
    
		Trace('修正数据：新增存在时变成修改');
		UPDATE Temp_Mpa_Au
			 SET New_State = 2, Has_Update_Name = 1, Has_Update_Fatherson = 1
		 WHERE State = 3
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----        主表
		---────────────────────────────────
	
		--生成主表del、add、update的数据
		Trace('生成主表del');
		INSERT INTO Temp_Mpa_Del /*ix_pointaddress表字段*/
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表add');
		INSERT INTO Temp_Mpa_Add
			( /*ix_pointaddress表字段*/ Pid, Geometry, X_Guide, Y_Guide, Guide_Link_Pid, 
			--Guide_Name_Groupid,
			 Locate_Link_Pid, Locate_Name_Groupid, Guide_Link_Side, Locate_Link_Side,SRC_PID, Region_Id, Mesh_Id,
			 Edit_Flag, Idcode, Dpr_Name, Dp_Name, Operator, Memoire, Dpf_Name, Poster_Id, Address_Flag,
			 Verifed, Log, Memo, RESERVED, Task_Id, Src_Type, Data_Version, Field_Task_Id, U_Record, U_Fields, State)
			SELECT Pid, Geometry, X_Guide, Y_Guide, Guide_Link_Pid, 
			--Guide_Name_Groupid, 
			Locate_Link_Pid,
						 Locate_Name_Groupid, Guide_Link_Side, Locate_Link_Side,SRC_PID,  Region_Id, Mesh_Id, Edit_Flag,
						 Idcode, Dpr_Name, Dp_Name, Operator, Memoire, Dpf_Name, Poster_Id, Address_Flag,
						 Verifed, Log, Memo,RESERVED, 0 AS Task_Id, NULL AS Src_Type, Data_Version, Field_Task_Id, 0 AS U_Record,
						 NULL AS U_Fields, State
				FROM Temp_Mpa_Au a
			 WHERE New_State = 3;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表update:before');
		INSERT INTO Temp_Mpa_Update_Before
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress b
			 WHERE A.New_State = 2
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表update:after');
		INSERT INTO Temp_Mpa_Update_After
			(Pid, Geometry, X_Guide, Y_Guide, Guide_Link_Pid, 
			--Guide_Name_Groupid, 
			Locate_Link_Pid,
			 Locate_Name_Groupid, Guide_Link_Side, Locate_Link_Side,SRC_PID, Region_Id, Mesh_Id, Edit_Flag, Idcode,
			 Dpr_Name, Dp_Name, Operator, Memoire, Dpf_Name, Poster_Id, Address_Flag, Verifed, Log, Memo,RESERVED,
			 Task_Id,SRC_TYPE, Data_Version, Field_Task_Id, U_Record, U_Fields, State)
			SELECT A.Pid,
						 (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND Instr(A.Log, '改REL') > 0 THEN
								 A.Geometry
								ELSE
								 B.Geometry
							END) AS Geometry,
						 (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND (Instr(A.Log, '改GUIDEX') > 0 OR Instr(A.Log, '改GUIDEY') > 0) THEN
								 A.X_Guide
								ELSE
								 B.X_Guide
							END) AS X_Guide,
						 (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND (Instr(A.Log, '改GUIDEX') > 0 OR Instr(A.Log, '改GUIDEY') > 0) THEN
								 A.Y_Guide
								ELSE
								 B.Y_Guide
							END) AS Y_Guide,
						 (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND (Instr(A.Log, '改GUIDEX') > 0 OR Instr(A.Log, '改GUIDEY') > 0) THEN
								 A.Guide_Link_Pid
								ELSE
								 B.Guide_Link_Pid
							END) AS Guide_Link_Pid,
					--	 (CASE
						--		WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND Instr(A.Log, '改位移') > 0 THEN
							--	 A.Guide_Name_Groupid
							--	ELSE
							--	 B.Guide_Name_Groupid
                                                        --	END) AS Guide_Name_Groupid,
						 B.Locate_Link_Pid, B.Locate_Name_Groupid,
						
						 (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND (Instr(A.Log, '改GUIDEX') > 0 OR Instr(A.Log, '改GUIDEY') > 0 )THEN
								 A.Guide_Link_Side
								ELSE
								 B.Guide_Link_Side
							END) AS Guide_Link_Side, B.Locate_Link_Side,B.SRC_PID, 
              (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND Instr(A.Log, '改REL') > 0 THEN
								 A.Region_Id
								ELSE
								 B.Region_Id
							END) AS Region_Id ,
              (CASE
								WHEN V_Merge_Geo = 'T' AND A.Geo_Oprstatus = 0 AND Instr(A.Log, '改REL') > 0 THEN
								 A.Mesh_Id
								ELSE
								 B.Mesh_Id
							END) AS Mesh_Id , B.Edit_Flag , B.Idcode,
						 (CASE
								WHEN Instr(A.Log, '改DPRN') > 0 THEN
								 A.Dpr_Name
								ELSE
								 B.Dpr_Name
							END) AS Dpr_Name,
						 (CASE
								WHEN Instr(A.Log, '改DPN') > 0 THEN
								 A.Dp_Name
								ELSE
								 B.Dp_Name
							END) AS Dp_Name, B.Operator,
						 (CASE
								WHEN Instr(A.Log, '改标注') > 0 THEN
								 A.Memoire
								ELSE
								 B.Memoire
							END) AS Memoire, B.Dpf_Name, B.Poster_Id, B.Address_Flag, B.Verifed, A.Log,
						 -- 
						 B.Memo, B.RESERVED, 0 AS Task_Id,B.Src_Type, B.Data_Version, B.Field_Task_Id, 0 AS U_Record, NULL AS U_Fields,
						 A.State --
				FROM Temp_Mpa_Au a, Ix_Pointaddress b
			 WHERE A.New_State = 2
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		--合并主表
		Trace('合并主表del');
		DELETE FROM Ix_Pointaddress t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Del a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表add');
		INSERT INTO Ix_Pointaddress t
			SELECT *
				FROM Temp_Mpa_Add;
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表update:del');
		DELETE FROM Ix_Pointaddress t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Update_Before a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表update:after');
		INSERT INTO Ix_Pointaddress t
			SELECT *
				FROM Temp_Mpa_Update_After;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----        名称表
		---────────────────────────────────
	
		Trace('名称表del');
		INSERT INTO Temp_Mpa_Name_Del /*ix_pointaddress_name表字段*/
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Name b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表add');
		INSERT INTO Temp_Mpa_Name_Add
			( /*ix_pointaddress_name表字段*/ Name_Id, Name_Groupid, Pid, Lang_Code, Sum_Char, Split_Flag,
			 Fullname, Fullname_Phonetic, Roadname, Roadname_Phonetic, Addrname, Addrname_Phonetic,
			 Province, City, County, Town, Street, Place, Landmark, Prefix, Housenum, TYPE, Subnum, Surfix,
			 Estab, Building, Unit, Floor, Room, Addons, Prov_Phonetic, City_Phonetic, County_Phonetic,
			 Town_Phonetic, Street_Phonetic, Place_Phonetic, Landmark_Phonetic, Prefix_Phonetic,
			 Housenum_Phonetic, Type_Phonetic, Subnum_Phonetic, Surfix_Phonetic, Estab_Phonetic,
			 Building_Phonetic, Floor_Phonetic, Unit_Phonetic, Room_Phonetic, Addons_Phonetic, U_Record,
			 U_Fields)
			SELECT Name_Id, Name_Groupid, A.Pid, Lang_Code, Sum_Char, Split_Flag,
      (case when a.address_flag=0 then a.DPR_NAME || a.DP_NAME 
      else Fullname end)as Fullname,
						 Fullname_Phonetic, Roadname, Roadname_Phonetic, Addrname, Addrname_Phonetic, Province,
						 City, County, Town, Street, Place, Landmark, Prefix, Housenum, TYPE, Subnum, Surfix,
						 Estab, Building, Unit, Floor, Room, Addons, Prov_Phonetic, City_Phonetic,
						 County_Phonetic, Town_Phonetic, Street_Phonetic, Place_Phonetic, Landmark_Phonetic,
						 Prefix_Phonetic, Housenum_Phonetic, Type_Phonetic, Subnum_Phonetic, Surfix_Phonetic,
						 Estab_Phonetic, Building_Phonetic, Floor_Phonetic, Unit_Phonetic, Room_Phonetic,
						 Addons_Phonetic, 0 AS U_Record, NULL AS U_Fields
				FROM Temp_Mpa_Au a, Au_Ix_Pointaddress_Name b
			 WHERE A.New_State = 3
						 AND A.Audata_Id = B.Audata_Id;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表update:del');
		INSERT INTO Temp_Mpa_Name_Update_Del /*ix_pointaddress_name表字段*/
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Name b
			 WHERE A.Pid = B.Pid
						 AND B.Lang_Code = 'CHI'
						 AND A.New_State = 2
						 AND A.Has_Update_Name = 1;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表update:add');
    --如果地址标识为0，则将DPR_NAME + DPNAME合并后赋值给FULLNAME。同时更新IX_POINTADDRESS_NAME表的其它附加信息、楼栋号、前缀、后缀、门牌、类型、子号内容。
		INSERT INTO Temp_Mpa_Name_Update_Add
			( /*ix_pointaddress_name表字段*/ Name_Id, Name_Groupid, Pid, Lang_Code, Sum_Char, Split_Flag,
			 Fullname, Fullname_Phonetic, Roadname, Roadname_Phonetic, Addrname, Addrname_Phonetic,
			 Province, City, County, Town, Street, Place, Landmark, Prefix, Housenum, TYPE, Subnum, Surfix,
			 Estab, Building, Unit, Floor, Room, Addons, Prov_Phonetic, City_Phonetic, County_Phonetic,
			 Town_Phonetic, Street_Phonetic, Place_Phonetic, Landmark_Phonetic, Prefix_Phonetic,
			 Housenum_Phonetic, Type_Phonetic, Subnum_Phonetic, Surfix_Phonetic, Estab_Phonetic,
			 Building_Phonetic, Floor_Phonetic, Unit_Phonetic, Room_Phonetic, Addons_Phonetic, U_Record,
			 U_Fields)     
			SELECT Name_Id, Name_Groupid, Pid, Lang_Code, Sum_Char, Split_Flag,
             DPR_NAME || DP_NAME as  Fullname,
						 Fullname_Phonetic, Roadname, Roadname_Phonetic, Addrname, Addrname_Phonetic, Province,
						 City, County, 
             case when  au_dprn>0 then au_Town else Town end as Town, 
             case when  au_dprn>0 then au_Street else Street end as Street, 
             case when  au_dprn>0 then au_Place else place end as place, 
             Landmark, 
             case when  au_dpn>0 then au_prefix else prefix end as Prefix, 
             case when au_dpn>0 then au_housenum else housenum end as Housenum, 
             case when au_dpn>0 then au_type else type end as TYPE, 
             case when au_dpn>0 then au_subnum else subnum end as Subnum, 
             case when au_dpn>0 then au_surfix else surfix end as Surfix,
						 case when  au_dprn>0 then au_Estab else Estab end as Estab, 
             case when au_dpn>0 then au_building else Building end as Building, 
             Unit, Floor, Room, 
             case when au_dpn>0 then  au_addons else Addons end as Addons, 
             Prov_Phonetic, City_Phonetic,
						 County_Phonetic, 
             case when  au_dprn>0 then au_Town_Phonetic else Town_Phonetic end as Town_Phonetic, 
             case when  au_dprn>0 then au_Street_Phonetic else Street_Phonetic end as Street_Phonetic, 
             case when  au_dprn>0 then au_Place_Phonetic else Place_Phonetic end as Place_Phonetic, 
             Landmark_Phonetic,
						 case when au_dpn>0 then au_Prefix_Phonetic else Prefix_Phonetic end as Prefix_Phonetic, 
             case when au_dpn>0 then au_Housenum_Phonetic else Housenum_Phonetic end as Housenum_Phonetic, 
             case when au_dpn>0 then au_Type_Phonetic else Type_Phonetic end as Type_Phonetic, 
             case when au_dpn>0 then au_Subnum_Phonetic else Subnum_Phonetic end as Subnum_Phonetic, 
             case when au_dpn>0 then au_Surfix_Phonetic else Surfix_Phonetic end as Surfix_Phonetic,
						 case when  au_dprn>0 then au_Estab_Phonetic else Estab_Phonetic end as Estab_Phonetic, 
             case when au_dpn>0 then au_Building_Phonetic else Building_Phonetic end as Building_Phonetic, 
             Floor_Phonetic, Unit_Phonetic, Room_Phonetic,
						 case when au_dpn>0 then au_Addons_Phonetic else Addons_Phonetic end as Addons_Phonetic,
             0 AS U_Record, NULL AS U_Fields
				FROM (SELECT c.*,a.DPR_NAME, a.DP_NAME,b.ADDONS as au_ADDONS,b.BUILDING as au_BUILDING,b.PREFIX as au_PREFIX,
                     b.SURFIX as au_SURFIX,b.HOUSENUM as au_HOUSENUM,b.TYPE as au_type,b.SUBNUM as au_SUBNUM,
                     b.addons_phonetic as au_addons_phonetic,b.building_phonetic as au_building_phonetic,
                     b.prefix_phonetic as au_prefix_phonetic,b.surfix_phonetic as au_surfix_phonetic,
                     b.housenum_phonetic as au_housenum_phonetic,b.type_phonetic as au_type_phonetic,
                     b.subnum_phonetic as au_subnum_phonetic,
                     b.town as au_town,b.town_phonetic as au_town_phonetic,b.street as au_street,
                     b.street_phonetic as au_street_phonetic,
                     b.ESTAB as au_ESTAB,b.estab_phonetic as  au_estab_phonetic,
                     b.PLACE as au_place,b.place_phonetic as au_place_phonetic,Instr(A.Log, '改DPN') as au_dpn,Instr(A.Log, '改DPRN') as au_dprn
								 FROM Temp_Mpa_Au a, Au_Ix_Pointaddress_Name b,Temp_Mpa_Name_Update_Del c
								WHERE A.Audata_Id = B.Audata_Id
                      and b.pid=c.pid                     
											AND A.New_State = 2
											AND A.Has_Update_Name = 1
                      );
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表del');
		DELETE FROM Ix_Pointaddress_Name t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Name_Del a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表add');
		INSERT INTO Ix_Pointaddress_Name t
			SELECT *
				FROM Temp_Mpa_Name_Add;
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表update:del');
		DELETE FROM Ix_Pointaddress_Name t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Name_Update_Del a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表update:add');
		INSERT INTO Ix_Pointaddress_Name t
			SELECT *
				FROM Temp_Mpa_Name_Update_Add;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----    父子关系父表
		---────────────────────────────────
	 /* for rec in (select * from Temp_Mpa_Au) loop
      trace(V_Task_Id||',Temp_Mpa_Au:'||rec.pid||','||rec.state);
    
    end loop;*/
    
		Trace('生成父子关系父表add');
		INSERT INTO Temp_Mpa_Parent_Add
			(Group_Id, Parent_Pa_Pid, U_Record)
			SELECT DISTINCT C.Group_Id, C.Parent_Pa_Pid, 0
				FROM Temp_Mpa_Au a
				LEFT JOIN Au_Ix_Pointaddress_Children b
					ON A.Audata_Id = B.Audata_Id and (V_Task_Id is null or b.field_task_id=V_Task_Id)
				LEFT JOIN Au_Ix_Pointaddress_Parent c
					ON B.Group_Id = C.Group_Id
				LEFT JOIN Ix_Pointaddress_Parent d
					ON C.Parent_Pa_Pid = D.Parent_Pa_Pid
			 WHERE C.Group_Id IS NOT NULL
						 AND D.Parent_Pa_Pid IS NULL
             ;
		Trace(SQL%ROWCOUNT);
    
	/*  for rec in (select * from Temp_Mpa_Parent_Add) loop
      trace(V_Task_Id||',Temp_Mpa_Parent_Add:'||rec.group_id||','||rec.parent_pa_pid);
    
    end loop;*/
    
		Trace('合并父子关系父表add');
		INSERT INTO Ix_Pointaddress_Parent t
			SELECT *
				FROM Temp_Mpa_Parent_Add;
		Trace(SQL%ROWCOUNT);
	
		--────────────────────────────────
		----    父子关系子表
		--────────────────────────────────
	
		Trace('生成父子关系子表del');
		INSERT INTO Temp_Mpa_Children_Del /*ix_pointaddress_children.**/
			SELECT DISTINCT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Children b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Child_Pa_Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成父子关系子表del(删除父时，所有子关系都要删除)');
		INSERT INTO Temp_Mpa_Children_Cascade_Del
			SELECT DISTINCT C.*
				FROM Temp_Mpa_Au a
				LEFT JOIN Ix_Pointaddress_Parent b
					ON A.Pid = B.Parent_Pa_Pid
				LEFT JOIN Ix_Pointaddress_Children c
					ON B.Group_Id = C.Group_Id
			 WHERE A.New_State = 1
						 AND C.Group_Id IS NOT NULL
						 AND NOT EXISTS (SELECT 1
								FROM Temp_Mpa_Children_Del e
							 WHERE E.Group_Id = C.Group_Id
										 AND E.Child_Pa_Pid = C.Child_Pa_Pid);
    Trace(SQL%ROWCOUNT);
    
    Trace('生成父子关系子表del(删除父时)');                
    INSERT INTO Temp_mpa_parent_del
			SELECT DISTINCT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Parent b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Parent_Pa_Pid;
    Trace(SQL%ROWCOUNT);
    
    Trace('合并父子关系子表del');
		DELETE FROM Ix_Pointaddress_Children t
		 WHERE EXISTS (SELECT 1
							FROM Temp_Mpa_Children_Del a
						 WHERE T.Child_Pa_Pid = A.Child_Pa_Pid)
					 OR EXISTS (SELECT 1
							FROM Temp_Mpa_Children_Cascade_Del b
						 WHERE T.Child_Pa_Pid = B.Child_Pa_Pid);
		Trace(SQL%ROWCOUNT);
    
    Trace('合并父子关系父表del');
		DELETE FROM Ix_Pointaddress_Parent t
		 WHERE EXISTS (SELECT 1
							FROM Temp_mpa_parent_del a
						 WHERE T.parent_pa_pid = A.parent_pa_pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成父子关系子表add');
		INSERT INTO Temp_Mpa_Children_Add
			( /*ix_pointaddress_children表字段*/ Group_Id, Child_Pa_Pid, U_Record, U_Fields)
			SELECT b.Group_Id, A.Child_Pa_Pid, 0, NULL
				FROM Temp_Mpa_Au a,Ix_Pointaddress_Parent b
        WHERE a.parent_pa_pid=b.parent_pa_pid
			       AND New_State = 3
						 AND A.Group_Id IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成父子关系子表update:del');
		INSERT INTO Temp_Mpa_Children_Update_Del /*ix_pointaddress_children表字段*/
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Children b
			 WHERE A.Pid = B.Child_Pa_Pid
						 AND A.New_State = 2
						 AND A.Has_Update_Fatherson = 1;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成父子关系子表update:add');
		INSERT INTO Temp_Mpa_Children_Update_Add
			( /*ix_pointaddress_children表字段*/ Group_Id, Child_Pa_Pid, U_Record, U_Fields)
			SELECT b.Group_Id, A.Child_Pa_Pid, 0, NULL
				FROM Temp_Mpa_Au a,Ix_Pointaddress_Parent b
           WHERE a.parent_pa_pid=b.parent_pa_pid
			       AND A.New_State = 2
						 AND A.Has_Update_Fatherson = 1
						 AND A.Group_Id IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		Trace('合并父子关系子表add');
		INSERT INTO Ix_Pointaddress_Children t
			SELECT *
				FROM Temp_Mpa_Children_Add;
		Trace(SQL%ROWCOUNT);
	
		Trace('合并父子关系子表update:del');
		DELETE FROM Ix_Pointaddress_Children t
		 WHERE EXISTS (SELECT 1
							FROM Temp_Mpa_Children_Update_Del a
						 WHERE T.Child_Pa_Pid = A.Child_Pa_Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并父子关系子表update:add');
		INSERT INTO Ix_Pointaddress_Children t
			SELECT *
				FROM Temp_Mpa_Children_Update_Add;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----    处理孤立父
		---────────────────────────────────
	
		Trace('生成处理孤立父del');
		INSERT INTO Temp_Mpa_Parent_Del
			SELECT DISTINCT *
				FROM Ix_Pointaddress_Parent a
			 WHERE A.Group_Id NOT IN (SELECT Group_Id
																	FROM Ix_Pointaddress_Children);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并：删除孤立父');
		DELETE FROM Ix_Pointaddress_Parent t
		 WHERE EXISTS (SELECT Group_Id
							FROM Temp_Mpa_Parent_Del a
						 WHERE T.Group_Id = A.Group_Id);
		Trace(SQL%ROWCOUNT);
	
  
     ---────────────────────────────────
		----    处理 IX_POINTADDRESS_FLAG (点门牌标识表)
		---────────────────────────────────
    
    
      
   ---如果AU_IX_POINTADDRESS_FLAG表中存在记录，则新增后插入子版本IX_POINTADDRESS_FLAG表中
    EXECUTE IMMEDIATE  'truncate table temp_ix_pointaddress_flag_add';
    EXECUTE IMMEDIATE  'truncate table temp_ix_pointaddress_flag_del';
    
    
     -- 点门牌标识表删除 
    Trace('点门牌标识表del');
		INSERT INTO temp_ix_pointaddress_flag_del /*AU_IX_POINTADDRESS_FLAG表字段*/
			SELECT b.pid, b.flag_code, 0 AS U_RECORD, NULL AS U_FIELDS
				FROM Temp_Mpa_Au a, IX_POINTADDRESS_FLAG b
			 WHERE a.New_State = 1
						 AND  a.pid = b.pid;
    
     
    Trace('点门牌标识表(IX_POINTADDRESS_FLAG)add');
    INSERT INTO temp_ix_pointaddress_flag_add /* IX_POINTADDRESS_FLAG 表字段*/         
      SELECT b.POINTADDRESS_PID, b.flag_code, 0 AS U_RECORD, NULL AS U_FIELDS
      FROM Temp_Mpa_Au a, AU_IX_POINTADDRESS_FLAG b
      WHERE a.New_State = 3
    --   AND b.flag_code <> '150000060000'
       AND a.audata_id = b.audata_id
       AND not exists (select 1 
              from IX_POINTADDRESS_FLAG c
             where b.pointaddress_pid = c.pid and b.flag_code = c.flag_code);
		Trace(SQL%ROWCOUNT);
    
    Trace('点门牌标识表add');
		INSERT INTO IX_POINTADDRESS_FLAG t
			SELECT *
				FROM temp_ix_pointaddress_flag_add;
		Trace(SQL%ROWCOUNT);
    
      --生成点门牌标识表履历
		Pk_History_Util.Generate_Add_History('IX_POINTADDRESS_FLAG', 'select * from temp_ix_pointaddress_flag_add');
    EXECUTE IMMEDIATE  'truncate table temp_ix_pointaddress_flag_add'; 
    
    IF V_Merge_Geo = 'T'
		THEN
      -- 如果AU_IX_POINTADDRESS_FLAG中存在FLAG_CODE为“150000060000”的记录，母库IX_POINTADDRESS_FLAG表中不存在该记录，则新增一条记录，FLAG_CODE赋值为“150000060000”，存在则不处理。
      Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:add');
     INSERT INTO temp_ix_pointaddress_flag_add /* IX_POINTADDRESS_FLAG 表字段*/      
         SELECT b.POINTADDRESS_PID,
           b.flag_code,
           0 AS U_RECORD,
           NULL AS U_FIELDS
          FROM Temp_Mpa_Au             a,
               AU_IX_POINTADDRESS_FLAG b
         WHERE a.New_State = 2
           and b.flag_code = '150000060000'
           and ( Instr(a.Log, '改GUIDEX') > 0 or Instr(a.Log, '改GUIDEY') > 0)
           AND a.audata_id = b.audata_id and a.geo_oprstatus = 0
           and not exists(select 1 from IX_POINTADDRESS_FLAG c where a.pid = c.pid and c.flag_code = '150000060000'); 
      Trace(SQL%ROWCOUNT);

      ---- 如果AU_IX_POINTADDRESS_FLAG中不存在FLAG_CODE为“150000060000”的记录，母库IX_POINTADDRESS_FLAG表中存在该记录，则删除该记录，否则不处理。
      Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:del');
     INSERT INTO temp_ix_pointaddress_flag_del /* IX_POINTADDRESS_FLAG 表字段*/
         SELECT c.*
          FROM Temp_Mpa_Au    a,
               IX_POINTADDRESS_FLAG    c
         WHERE a.New_State = 2
           AND c.flag_code = '150000060000'
           and ( Instr(a.Log, '改GUIDEX') > 0 or Instr(a.Log, '改GUIDEY') > 0) 
           and a.geo_oprstatus = 0
           and a.pid = c.pid 
           and not exists (select 1 from AU_IX_POINTADDRESS_FLAG b where b.pointaddress_pid = a.pid and b.flag_code = '150000060000'); 
      Trace(SQL%ROWCOUNT);
  			
		END IF;
    
    
       Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:del');
		DELETE FROM IX_POINTADDRESS_FLAG t
		 WHERE EXISTS (SELECT Pid
							FROM temp_ix_pointaddress_flag_del a
						 WHERE t.Pid = a.Pid and t.flag_code = a.flag_code);
             -- and t.flag_code = '150000060000';      
             
		Trace(SQL%ROWCOUNT);
	
		Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:add');
		INSERT INTO IX_POINTADDRESS_FLAG t
			SELECT *
				FROM temp_ix_pointaddress_flag_add;
		Trace(SQL%ROWCOUNT);

  
  
		Trace('生成履历');
	
		--生成主表履历
		--if(v_merge_geo='T') then --仅同时融合几何时才生成删除履历 
		Pk_History_Util.Generate_Del_History('ix_pointaddress', 'select a.* from Temp_mpa_del a,Temp_mpa_au b where a.pid=b.pid /*and b.geo_oprstatus=0*/');
		Trace(SQL%ROWCOUNT);
		--end if;
		Pk_History_Util.Generate_Add_History('ix_pointaddress', 'select * from Temp_mpa_add');
		Trace(SQL%ROWCOUNT);
		IF (V_Merge_Geo = 'T')
		THEN
			Pk_History_Util.Generate_Update_History('ix_pointaddress', 'select a.*,b.*,pk_merge_pa.parselog(b.log,1,1) from temp_mpa_update_before a,temp_mpa_update_after b where a.pid=b.pid
      and exists(
select 1 from Temp_Mpa_Au x where x.pid=b.pid and x.geo_oprstatus=0
)
');
			Trace(SQL%ROWCOUNT);
      
			Pk_History_Util.Generate_Update_History('ix_pointaddress', 'select a.*,b.*,pk_merge_pa.parselog(b.log,1,0) from temp_mpa_update_before a,temp_mpa_update_after b where a.pid=b.pid
      and exists(
select 1 from Temp_Mpa_Au x where x.pid=b.pid and x.geo_oprstatus>0
)');
			Trace(SQL%ROWCOUNT);
		ELSE
			Pk_History_Util.Generate_Update_History('ix_pointaddress', 'select a.*,b.*,pk_merge_pa.parselog(b.log,1,0) from temp_mpa_update_before a,temp_mpa_update_after b where a.pid=b.pid');
			Trace(SQL%ROWCOUNT);
		END IF;
	
		--生成名称表履历
		--if(v_merge_geo='T') then --仅同时融合几何时才生成删除履历
		Pk_History_Util.Generate_Del_History('ix_pointaddress_name', 'select a.* from Temp_mpa_name_del a,Temp_mpa_au b where a.pid=b.pid /*and b.geo_oprstatus=0*/');
		Trace('[生成履历] DEL NAME：' || SQL%ROWCOUNT);
		--end if;
	
		Pk_History_Util.Generate_Add_History('ix_pointaddress_name', 'select * from Temp_mpa_name_add');
		Trace('[生成履历] ADD NAME：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Del_History('ix_pointaddress_name', 'select * from Temp_mpa_name_update_del');
		Trace('[生成履历] UPDATE-DEL NAME：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('ix_pointaddress_name', 'select * from Temp_mpa_name_update_add');
		Trace('[生成履历] UPDATE-ADD NAME：' || SQL%ROWCOUNT);
	
		--生成父子关系子表履历  
		--if(v_merge_geo='T') then --仅同时融合几何时才生成删除履历
		Pk_History_Util.Generate_Del_History('ix_pointaddress_children', 'select a.* from Temp_mpa_children_del a,Temp_mpa_au b where a.Child_Pa_Pid=b.pid /*and b.geo_oprstatus=0*/');
		Trace(SQL%ROWCOUNT);
		--end if;
	
		Pk_History_Util.Generate_Del_History('ix_pointaddress_children', 'select a.* from Temp_mpa_children_cascade_del a');
		Trace('[生成履历] DEl 孤立子关系：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('ix_pointaddress_children', 'select * from Temp_mpa_children_add');
		Trace('[生成履历] ADD 子关系：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Del_History('ix_pointaddress_children', 'select * from Temp_mpa_children_update_del');
		Trace('[生成履历] UPADTE-DEL 子关系：' || SQL%ROWCOUNT);
	
		Pk_History_Util.Generate_Add_History('ix_pointaddress_children', 'select * from Temp_mpa_children_update_add');
		Trace('[生成履历] UPDATE-ADD 子关系：' || SQL%ROWCOUNT);
	
		--生成父子关系父表履历  
		--if(v_merge_geo='T') then --仅同时融合几何时才生成删除履历
		Pk_History_Util.Generate_Del_History('ix_pointaddress_parent', 'select * from temp_mpa_parent_del');
		Trace(SQL%ROWCOUNT);
		--end if;
	
		Pk_History_Util.Generate_Add_History('ix_pointaddress_parent', 'select * from temp_mpa_parent_add');
		Trace(SQL%ROWCOUNT);
	


     --生成点门牌标识表履历
		Pk_History_Util.Generate_Del_History('IX_POINTADDRESS_FLAG', 'select * from temp_ix_pointaddress_flag_del');
		Pk_History_Util.Generate_Add_History('IX_POINTADDRESS_FLAG', 'select * from temp_ix_pointaddress_flag_add');
     
		---────────────────────────────────
		----    外业表
		---────────────────────────────────
	
		Trace('生成外业表0->1作业履历');
	
		Pk_History_Util.Generate_Au_Work_History('au_ix_pointaddress', 'select a.* from au_ix_pointaddress a, temp_mpa_au b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 0, 1);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成外业表1->2作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_ix_pointaddress', 'select a.* from au_ix_pointaddress a, temp_mpa_au b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 2);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成外业表1->0作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_ix_pointaddress', 'select a.* from au_ix_pointaddress a, temp_mpa_error b where a.audata_id=b.audata_id', 'T', V_Merge_Geo, 1, 0);
		Trace(SQL%ROWCOUNT);
	
		Trace('更新外业表0->1');
		UPDATE Au_Ix_Pointaddress t
			 SET T.Att_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Mpa_Au a
						 WHERE A.Audata_Id = T.audata_id);
		Trace(SQL%ROWCOUNT);
	
		IF V_Merge_Geo = 'T'
		THEN
			Trace('更新外业表0->1');
			UPDATE Au_Ix_Pointaddress t
				 SET T.Geo_Oprstatus = 1
			 WHERE T.Geo_Oprstatus = 0
						 AND EXISTS (SELECT 1
								FROM Temp_Mpa_Au a
							 WHERE A.Audata_Id = T.audata_id);
			Trace(SQL%ROWCOUNT);
		
		END IF;
	
		Trace('将履历写入履历库');
		INSERT INTO Operate_Log
			SELECT *
				FROM Temp_Merge_Raw_Operate_Log;
		Trace(SQL%ROWCOUNT);
	END;

	--仅融合几何
	PROCEDURE Merge_Geo(V_Task_Id    VARCHAR2,
											V_Ignore_Att VARCHAR2 := 'F' /*忽略同时为属性作业的数据*/) IS
	BEGIN
		Trace('过滤已作业的数据');
		INSERT INTO Temp_Mpa_Unchanged --
			SELECT *
				FROM Temp_Pa
			 WHERE Geo_Oprstatus IN (1, 2)
						 OR State = 0;
		Trace(SQL%ROWCOUNT);
	
		IF V_Ignore_Att = 'T'
		THEN
			--忽略同时为属性作业的数据
		
			Trace('忽略属性作业数据');
			INSERT INTO Temp_Mpa_Unchanged --过滤已作业的数据
				SELECT *
					FROM Temp_Pa
				 WHERE Geo_Oprstatus = 0
							 AND Att_Oprstatus = 0;
			Trace(SQL%ROWCOUNT);
		
		END IF;
	
		Trace('过滤删除或修改但在母库中不存在的数据');
		INSERT INTO Temp_Mpa_Error
			SELECT T.*
				FROM Temp_Pa t
				LEFT JOIN Ix_Pointaddress b
					ON T.Pid = B.Pid
			 WHERE T.State IN (1, 2)
						 AND B.Pid IS NULL
						 AND T.Audata_Id NOT IN (SELECT Audata_Id
																			 FROM Temp_Mpa_Unchanged);
		Trace(SQL%ROWCOUNT);
	
		Trace('确定需要融合的数据');
		INSERT INTO Temp_Mpa_Au --其它数据需要融合
			SELECT T.AUDATA_ID,
            T.PID,
            T.GEOMETRY,
            T.X_GUIDE,
            T.Y_GUIDE,
            T.GUIDE_LINK_PID,
            T.LOCATE_LINK_PID,
            T.LOCATE_NAME_GROUPID,
            T.GUIDE_LINK_SIDE,
            T.LOCATE_LINK_SIDE,
            T.SRC_PID,
            T.REGION_ID,
            T.MESH_ID,
            T.MESH_ID_2K,
            T.EDIT_FLAG,
            T.IDCODE,
            T.DPR_NAME,
            T.DP_NAME,
            T.OPERATOR,
            T.MEMOIRE,
            T.DPF_NAME,
            T.POSTER_ID,
            T.ADDRESS_FLAG,
            T.VERIFED,
            T.STATE,
            T.LOG,
            T.MEMO,
            T.RESERVED,
            T.DATA_VERSION,
            T.GEO_TASK_ID,
            T.ATT_TASK_ID,
            T.FIELD_TASK_ID,
            T.GEO_OPRSTATUS,
            T.GEO_CHECKSTATUS,
            T.ATT_OPRSTATUS,
            T.ATT_CHECKSTATUS,
            T.IMP_DATE, A.Child_Pa_Pid, A.Parent_Pa_Pid, A.Group_Id, B.Pid AS Main_Pid,
						 (CASE
								WHEN t.address_flag=0 
                  and ( Instr(T.Log, 'DPRNAME') > 0
                  or Instr(T.Log, '改DPNAME') > 0 ) THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Name,
						 (CASE
								WHEN Instr(T.Log, '改FATHERSON') > 0 THEN
								 1
								ELSE
								 0
							END) AS Has_Update_Fatherson, T.State AS New_State
				FROM Temp_Pa t
			
				LEFT JOIN (SELECT DISTINCT B.Child_Pa_Pid, A.Parent_Pa_Pid, A.Group_Id
										 FROM Au_Ix_Pointaddress_Parent a, Au_Ix_Pointaddress_Children b
										WHERE A.Group_Id = B.Group_Id) a
					ON T.Pid = A.Child_Pa_Pid
				LEFT JOIN Ix_Pointaddress b
					ON T.Pid = B.Pid
				LEFT JOIN Temp_Mpa_Unchanged c
					ON T.Audata_Id = C.Audata_Id
				LEFT JOIN Temp_Mpa_Error d
					ON T.Audata_Id = D.Audata_Id
			 WHERE C.Audata_Id IS NULL
						 AND D.Audata_Id IS NULL;
		Trace(SQL%ROWCOUNT);
	
		Trace('修正数据：新增存在时变成修改');
		UPDATE Temp_Mpa_Au
			 SET New_State = 2, Has_Update_Name = 1, Has_Update_Fatherson = 1
		 WHERE State = 3
					 AND Main_Pid IS NOT NULL;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----        主表
		---────────────────────────────────
	
		--生成主表del、add、update的数据
		Trace('生成主表del');
		INSERT INTO Temp_Mpa_Del /*ix_pointaddress表字段*/
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表add');
		INSERT INTO Temp_Mpa_Add
			( /*ix_pointaddress表字段*/ Pid, Geometry, X_Guide, Y_Guide, Guide_Link_Pid, 
			--Guide_Name_Groupid,
			 Locate_Link_Pid, Locate_Name_Groupid, Guide_Link_Side, Locate_Link_Side,SRC_PID, Region_Id, Mesh_Id,
			 Edit_Flag, Idcode, Dpr_Name, Dp_Name, Operator, Memoire, Dpf_Name, Poster_Id, Address_Flag,
			 Verifed, Log, Memo, RESERVED,Task_Id, Src_Type, Data_Version, Field_Task_Id, U_Record, U_Fields, State)
			SELECT Pid, Geometry, X_Guide, Y_Guide, Guide_Link_Pid, 
			--Guide_Name_Groupid, 
			Locate_Link_Pid,
						 Locate_Name_Groupid, Guide_Link_Side, Locate_Link_Side,SRC_PID, Region_Id, Mesh_Id, Edit_Flag,
						 Idcode, Dpr_Name, Dp_Name, Operator, Memoire, Dpf_Name, Poster_Id, Address_Flag,
						 Verifed, Log, Memo,RESERVED, 0 AS Task_Id, NULL AS Src_Type, Data_Version, Field_Task_Id, 0 AS U_Record,
						 NULL AS U_Fields, State
				FROM Temp_Mpa_Au a
			 WHERE New_State = 3;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表update:before');
		INSERT INTO Temp_Mpa_Update_Before
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress b
			 WHERE A.New_State = 2
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成主表update:after');
		INSERT INTO Temp_Mpa_Update_After
			(Pid, Geometry, X_Guide, Y_Guide, Guide_Link_Pid, 
			--Guide_Name_Groupid, 
			Locate_Link_Pid,
			 Locate_Name_Groupid, Guide_Link_Side, Locate_Link_Side,SRC_PID, Region_id,Mesh_Id,Edit_Flag, Idcode,
			 Dpr_Name, Dp_Name, Operator, Memoire, Dpf_Name, Poster_Id, Address_Flag, Verifed, Log, Memo,RESERVED,
			 Task_Id, Src_Type, Data_Version, Field_Task_Id, U_Record, U_Fields, State)
			SELECT A.Pid,
						 (CASE
								WHEN Instr(A.Log, '改REL') > 0 THEN
								 A.Geometry
								ELSE
								 B.Geometry
							END) AS Geometry,
						 (CASE
								WHEN (Instr(A.Log, '改GUIDEX') > 0 OR  Instr(A.Log, '改GUIDEY') > 0) THEN
								 A.X_Guide
								ELSE
								 B.X_Guide
							END) AS X_Guide,
						 (CASE
								WHEN (Instr(A.Log, '改GUIDEX') > 0 OR  Instr(A.Log, '改GUIDEY') > 0) THEN
								 A.Y_Guide
								ELSE
								 B.Y_Guide
							END) AS Y_Guide,
						 (CASE
								WHEN (Instr(A.Log, '改GUIDEX') > 0 OR  Instr(A.Log, '改GUIDEY') > 0) THEN
								 A.Guide_Link_Pid
								ELSE
								 B.Guide_Link_Pid
							END) AS Guide_Link_Pid,
					--	 (CASE
							--	WHEN Instr(A.Log, '改位移') > 0 THEN
							--	 A.Guide_Name_Groupid
							--	ELSE
							--	 B.Guide_Name_Groupid
                                                        --	END) AS Guide_Name_Groupid, 
							B.Locate_Link_Pid, B.Locate_Name_Groupid,
						
						 (CASE
								WHEN (Instr(A.Log, '改GUIDEX') > 0 OR  Instr(A.Log, '改GUIDEY') > 0 ) THEN
								 A.Guide_Link_Side
								ELSE
								 B.Guide_Link_Side
							END) AS Guide_Link_Side, B.Locate_Link_Side, B.SRC_PID,
              (CASE
								WHEN Instr(A.Log, '改REL') > 0 THEN
								 A.Region_id
								ELSE
								 B.Region_id
							END) AS Region_id , 
             (CASE
								WHEN Instr(A.Log, '改REL') > 0 THEN
								 A.Mesh_Id
								ELSE
								 B.Mesh_Id
							END) AS Mesh_Id ,
             B.Edit_Flag,
						 B.Idcode, B.Dpr_Name AS Dpr_Name, B.Dp_Name AS Dp_Name, B.Operator,
						 B.Memoire AS Memoire, B.Dpf_Name, B.Poster_Id, B.Address_Flag, B.Verifed, A.Log,
						 -- 
						 B.Memo,B.RESERVED, 0 AS Task_Id,B.Src_Type, B.Data_Version, B.Field_Task_Id, 0 AS U_Record, NULL AS U_Fields,
						 A.State --
				FROM Temp_Mpa_Au a, Ix_Pointaddress b
			 WHERE A.New_State = 2
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		--合并主表
		Trace('合并主表del');
		DELETE FROM Ix_Pointaddress t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Del a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表add');
		INSERT INTO Ix_Pointaddress t
			SELECT *
				FROM Temp_Mpa_Add;
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表update:del');
		DELETE FROM Ix_Pointaddress t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Update_Before a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并主表update:after');
		INSERT INTO Ix_Pointaddress t
			SELECT *
				FROM Temp_Mpa_Update_After;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----        名称表
		---────────────────────────────────
	
		Trace('名称表del');
		INSERT INTO Temp_Mpa_Name_Del /*ix_pointaddress_name表字段*/
			SELECT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Name b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('名称表add');
		INSERT INTO Temp_Mpa_Name_Add
			( /*ix_pointaddress_name表字段*/ Name_Id, Name_Groupid, Pid, Lang_Code, Sum_Char, Split_Flag,
			 Fullname, Fullname_Phonetic, Roadname, Roadname_Phonetic, Addrname, Addrname_Phonetic,
			 Province, City, County, Town, Street, Place, Landmark, Prefix, Housenum, TYPE, Subnum, Surfix,
			 Estab, Building, Unit, Floor, Room, Addons, Prov_Phonetic, City_Phonetic, County_Phonetic,
			 Town_Phonetic, Street_Phonetic, Place_Phonetic, Landmark_Phonetic, Prefix_Phonetic,
			 Housenum_Phonetic, Type_Phonetic, Subnum_Phonetic, Surfix_Phonetic, Estab_Phonetic,
			 Building_Phonetic, Floor_Phonetic, Unit_Phonetic, Room_Phonetic, Addons_Phonetic, U_Record,
			 U_Fields)
			SELECT Name_Id, Name_Groupid, A.Pid, Lang_Code, Sum_Char, Split_Flag, 
      (case when a.address_flag=0 then a.DPR_NAME || a.DP_NAME 
      else Fullname end)as Fullname,
						 Fullname_Phonetic, Roadname, Roadname_Phonetic, Addrname, Addrname_Phonetic, Province,
						 City, County, Town, Street, Place, Landmark, Prefix, Housenum, TYPE, Subnum, Surfix,
						 Estab, Building, Unit, Floor, Room, Addons, Prov_Phonetic, City_Phonetic,
						 County_Phonetic, Town_Phonetic, Street_Phonetic, Place_Phonetic, Landmark_Phonetic,
						 Prefix_Phonetic, Housenum_Phonetic, Type_Phonetic, Subnum_Phonetic, Surfix_Phonetic,
						 Estab_Phonetic, Building_Phonetic, Floor_Phonetic, Unit_Phonetic, Room_Phonetic,
						 Addons_Phonetic, 0 AS U_Record, NULL AS U_Fields
				FROM Temp_Mpa_Au a, Au_Ix_Pointaddress_Name b
			 WHERE A.New_State = 3
						 AND A.Audata_Id = B.Audata_Id;
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表del');
		DELETE FROM Ix_Pointaddress_Name t
		 WHERE EXISTS (SELECT Pid
							FROM Temp_Mpa_Name_Del a
						 WHERE T.Pid = A.Pid);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并名称表add');
		INSERT INTO Ix_Pointaddress_Name t
			SELECT *
				FROM Temp_Mpa_Name_Add;
		Trace(SQL%ROWCOUNT);
	
		---────────────────────────────────
		----    父子关系子表
		---────────────────────────────────
	
		Trace('生成父子关系子表del');
		INSERT INTO Temp_Mpa_Children_Del /*ix_pointaddress_children.**/
			SELECT DISTINCT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Children b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Child_Pa_Pid;
		Trace(SQL%ROWCOUNT);
	
		Trace('生成父子关系子表del(删除父时，所有子关系都要删除)');
		INSERT INTO Temp_Mpa_Children_Cascade_Del2
			SELECT DISTINCT C.*
				FROM Temp_Mpa_Au a
				LEFT JOIN Ix_Pointaddress_Parent b
					ON A.Pid = B.Parent_Pa_Pid
				LEFT JOIN Ix_Pointaddress_Children c
					ON B.Group_Id = C.Group_Id
			 WHERE A.New_State = 1
						 AND C.Group_Id IS NOT NULL
						 AND NOT EXISTS (SELECT 1
								FROM Temp_Mpa_Children_Del e
							 WHERE E.Group_Id = C.Group_Id
										 AND E.Child_Pa_Pid = C.Child_Pa_Pid)
			/* and not  exists 
      (select 1 from Temp_Mpa_Children_Cascade_Del e 
      where e.group_id=c.group_id and e.child_pa_pid=c.child_pa_pid)*/
			;
      
    Trace('生成父子关系子表del(删除父时)');                
    INSERT INTO Temp_mpa_parent_del
			SELECT DISTINCT B.*
				FROM Temp_Mpa_Au a, Ix_Pointaddress_Parent b
			 WHERE A.New_State = 1
						 AND A.Pid = B.Parent_Pa_Pid;
    Trace(SQL%ROWCOUNT);
	
		Trace('合并父子关系子表del');
		DELETE FROM Ix_Pointaddress_Children t
		 WHERE EXISTS (SELECT 1
							FROM Temp_Mpa_Children_Del a
						 WHERE T.Child_Pa_Pid = A.Child_Pa_Pid)
					 OR EXISTS (SELECT 1
							FROM Temp_Mpa_Children_Cascade_Del2 b
						 WHERE T.Child_Pa_Pid = B.Child_Pa_Pid);
	
    Trace('合并父子关系父表del');
		DELETE FROM Ix_Pointaddress_Parent t
		 WHERE EXISTS (SELECT 1
							FROM Temp_mpa_parent_del a
						 WHERE T.parent_pa_pid = A.parent_pa_pid);
		Trace(SQL%ROWCOUNT);
		---────────────────────────────────
		----    处理孤立父
		---────────────────────────────────
	
		Trace('生成处理孤立父del');
		INSERT INTO Temp_Mpa_Parent_Del
			SELECT *
				FROM Ix_Pointaddress_Parent a
			 WHERE A.Group_Id NOT IN (SELECT Group_Id
																	FROM Ix_Pointaddress_Children);
		Trace(SQL%ROWCOUNT);
	
		Trace('合并：删除孤立父');
		DELETE FROM Ix_Pointaddress_Parent t
		 WHERE EXISTS (SELECT Group_Id
							FROM Temp_Mpa_Parent_Del a
						 WHERE T.Group_Id = A.Group_Id);
		Trace(SQL%ROWCOUNT);
	
  
     ---────────────────────────────────
		----    处理 IX_POINTADDRESS_FLAG (点门牌标识表)
		---────────────────────────────────
    
      ---如果AU_IX_POINTADDRESS_FLAG表中存在记录，则新增后插入子版本IX_POINTADDRESS_FLAG表中
    EXECUTE IMMEDIATE  'truncate table temp_ix_pointaddress_flag_add';
    EXECUTE IMMEDIATE  'truncate table temp_ix_pointaddress_flag_del'; 
    
     -- 点门牌标识表删除 
    Trace('点门牌标识表del');
		INSERT INTO temp_ix_pointaddress_flag_del /*AU_IX_POINTADDRESS_FLAG表字段*/
			SELECT b.pid, b.flag_code, 0 AS U_RECORD, NULL AS U_FIELDS
				FROM Temp_Mpa_Au a, IX_POINTADDRESS_FLAG b
			 WHERE a.New_State = 1
						 AND  a.pid = b.pid;
    
    
    Trace('点门牌标识表(IX_POINTADDRESS_FLAG)add');
    INSERT INTO temp_ix_pointaddress_flag_add /* IX_POINTADDRESS_FLAG 表字段*/         
      SELECT b.POINTADDRESS_PID, b.flag_code, 0 AS U_RECORD, NULL AS U_FIELDS
      FROM Temp_Mpa_Au a, AU_IX_POINTADDRESS_FLAG b
      WHERE a.New_State = 3
    --   AND b.flag_code <> '150000060000'
       AND a.audata_id = b.audata_id
       AND not exists (select 1 
              from IX_POINTADDRESS_FLAG c
             where b.pointaddress_pid = c.pid and b.flag_code = c.flag_code);
		Trace(SQL%ROWCOUNT);
    
    Trace('点门牌标识表add');
		INSERT INTO IX_POINTADDRESS_FLAG t
			SELECT *
				FROM temp_ix_pointaddress_flag_add;
		Trace(SQL%ROWCOUNT);
    
      --生成点门牌标识表履历
		Pk_History_Util.Generate_Add_History('IX_POINTADDRESS_FLAG', 'select * from temp_ix_pointaddress_flag_add');
     EXECUTE IMMEDIATE  'truncate table temp_ix_pointaddress_flag_add';

     -- 如果AU_IX_POINTADDRESS_FLAG中存在FLAG_CODE为“150000060000”的记录，母库IX_POINTADDRESS_FLAG表中不存在该记录，则新增一条记录，FLAG_CODE赋值为“150000060000”，存在则不处理。
		Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:add');
   INSERT INTO temp_ix_pointaddress_flag_add /* IX_POINTADDRESS_FLAG 表字段*/      
       SELECT b.POINTADDRESS_PID,
         b.flag_code,
         0 AS U_RECORD,
         NULL AS U_FIELDS
        FROM Temp_Mpa_Au             a,
             AU_IX_POINTADDRESS_FLAG b
       WHERE a.New_State = 2
         and b.flag_code = '150000060000'
         and ( Instr(a.Log, '改GUIDEX') > 0 or Instr(a.Log, '改GUIDEY') > 0)
         AND a.audata_id = b.audata_id and a.geo_oprstatus = 0
         and not exists(select 1 from IX_POINTADDRESS_FLAG c where a.pid = c.pid and c.flag_code = '150000060000'); 
		Trace(SQL%ROWCOUNT);

    ---- 如果AU_IX_POINTADDRESS_FLAG中不存在FLAG_CODE为“150000060000”的记录，母库IX_POINTADDRESS_FLAG表中存在该记录，则删除该记录，否则不处理。
    Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:del');
   INSERT INTO temp_ix_pointaddress_flag_del /* IX_POINTADDRESS_FLAG 表字段*/
       SELECT c.*
        FROM Temp_Mpa_Au    a,
             IX_POINTADDRESS_FLAG    c
       WHERE a.New_State = 2
         AND c.flag_code = '150000060000'
         and ( Instr(a.Log, '改GUIDEX') > 0 or Instr(a.Log, '改GUIDEY') > 0) 
         and a.geo_oprstatus = 0
         and a.pid = c.pid 
         and not exists (select 1 from AU_IX_POINTADDRESS_FLAG b where b.pointaddress_pid = a.pid and b.flag_code = '150000060000'); 
		Trace(SQL%ROWCOUNT);
    
    
      Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:del');
		DELETE FROM IX_POINTADDRESS_FLAG t
		 WHERE EXISTS (SELECT Pid
							FROM temp_ix_pointaddress_flag_del a
						 WHERE t.Pid = a.Pid and t.flag_code = a.flag_code);
           --  and t.flag_code = '150000060000';
		Trace(SQL%ROWCOUNT);
	
		Trace('点门牌标识表(IX_POINTADDRESS_FLAG)update:add');
		INSERT INTO IX_POINTADDRESS_FLAG t
			SELECT *
				FROM temp_ix_pointaddress_flag_add;
		Trace(SQL%ROWCOUNT);
    
    
    
		--生成主表履历
		Pk_History_Util.Generate_Del_History('ix_pointaddress', 'select a.* from Temp_mpa_del a');
		Pk_History_Util.Generate_Add_History('ix_pointaddress', 'select * from Temp_mpa_add');
		Pk_History_Util.Generate_Update_History('ix_pointaddress', 'select a.*,b.*,pk_merge_pa.parselog(b.log,0,1) from temp_mpa_update_before a,temp_mpa_update_after b where a.pid=b.pid');
	
		--生成名称表履历
		Pk_History_Util.Generate_Del_History('ix_pointaddress_name', 'select a.* from Temp_mpa_name_del a');
		Pk_History_Util.Generate_Add_History('ix_pointaddress_name', 'select * from Temp_mpa_name_add');
	
		--生成父子关系子表履历  
		Pk_History_Util.Generate_Del_History('ix_pointaddress_children', 'select * from Temp_mpa_children_del');
	
		Pk_History_Util.Generate_Del_History('ix_pointaddress_children', 'select a.* from Temp_mpa_children_cascade_del2 a');
		Trace(SQL%ROWCOUNT);
		--生成父子关系父表履历  
		Pk_History_Util.Generate_Del_History('ix_pointaddress_parent', 'select a.* from temp_mpa_parent_del a');
	
  
    --生成点门牌标识表履历
		Pk_History_Util.Generate_Del_History('IX_POINTADDRESS_FLAG', 'select * from temp_ix_pointaddress_flag_del');
		Pk_History_Util.Generate_Add_History('IX_POINTADDRESS_FLAG', 'select * from temp_ix_pointaddress_flag_add');
  
		---────────────────────────────────
		----    外业表
		---────────────────────────────────
	
		Trace('生成外业表0->1作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_ix_pointaddress', 'select a.* from au_ix_pointaddress a, temp_mpa_au b where a.audata_id=b.audata_id', 'F', 'T', 0, 1);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成外业表1->2作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_ix_pointaddress', 'select a.* from au_ix_pointaddress a, temp_mpa_au b where a.audata_id=b.audata_id', 'F', 'T', 1, 2);
		Trace(SQL%ROWCOUNT);
	
		Trace('生成外业表1->0作业履历');
		Pk_History_Util.Generate_Au_Work_History('au_ix_pointaddress', 'select a.* from au_ix_pointaddress a, temp_mpa_error b where a.audata_id=b.audata_id', 'F', 'T', 1, 0);
		Trace(SQL%ROWCOUNT);
	
		Trace('更新外业表0->1');
		UPDATE Au_Ix_Pointaddress t
			 SET T.Geo_Oprstatus = 1
		 WHERE EXISTS (SELECT 1
							FROM Temp_Mpa_Au a
						 WHERE A.Audata_Id = T.audata_id);
		Trace(SQL%ROWCOUNT);
	
		Trace('将履历写入履历库');
		INSERT INTO Operate_Log
			SELECT *
				FROM Temp_Merge_Raw_Operate_Log;
		Trace(SQL%ROWCOUNT);
	END;

	PROCEDURE Delete_Temp_Data IS
	BEGIN
	
		/*     execute immediate 'truncate table  Temp_Merge_Raw_Operate_Log';
    --execute immediate 'truncate table  temp_pa';
    execute immediate 'truncate table  temp_mpa_au';
    execute immediate 'truncate table  Temp_mpa_unchanged';
    execute immediate 'truncate table  Temp_mpa_error';
    execute immediate 'truncate table  Temp_mpa_del';
    execute immediate 'truncate table  Temp_mpa_add';
    execute immediate 'truncate table  Temp_mpa_update_before';
    execute immediate 'truncate table  Temp_mpa_update_after';
    execute immediate 'truncate table  Temp_mpa_name_del';
    execute immediate 'truncate table  Temp_mpa_name_add';
    execute immediate 'truncate table  Temp_mpa_name_update_del';
    execute immediate 'truncate table  Temp_mpa_name_update_add';
    execute immediate 'truncate table  Temp_mpa_parent_del';
    execute immediate 'truncate table  Temp_mpa_parent_add';
    execute immediate 'truncate table  Temp_mpa_children_del';
    execute immediate 'truncate table  Temp_mpa_children_add';
    execute immediate 'truncate table  Temp_mpa_children_update_del';
    execute immediate 'truncate table  Temp_mpa_children_update_add';*/
		EXECUTE IMMEDIATE 'delete from  Temp_Merge_Raw_Operate_Log';
		--execute immediate 'delete from  temp_pa';
		EXECUTE IMMEDIATE 'delete from  temp_mpa_au';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_unchanged';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_error';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_del';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_add';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_update_before';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_update_after';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_name_del';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_name_add';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_name_update_del';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_name_update_add';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_parent_del';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_parent_add';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_children_del';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_children_add';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_children_update_del';
		EXECUTE IMMEDIATE 'delete from  Temp_mpa_children_update_add';
   
	
	END;

	PROCEDURE Loop_Do_Merge(V_Field_Task_Id VARCHAR2,
													V_Merge_Att     VARCHAR2 := 'F',
													V_Merge_Geo     VARCHAR2 := 'F') IS
	BEGIN
		Trace('处理外业任务号:' || V_Field_Task_Id);
		DELETE FROM Temp_Pa;
		DELETE FROM Temp_Mpa_Children_Cascade_Del;
		DELETE FROM Temp_Mpa_Children_Cascade_Del2;
		Delete_Temp_Data;
	
		Trace('确定外业表数据量');
		INSERT INTO Temp_Pa
			SELECT *
				FROM Au_Ix_Pointaddress t
			 WHERE V_Field_Task_Id IS NULL
						 OR T.Field_Task_Id = V_Field_Task_Id;
		Trace(SQL%ROWCOUNT);
	
		IF V_Merge_Att = 'F' AND V_Merge_Geo = 'F'
		THEN
			Raise_Application_Error(-20999, '融合选项无效');
		ELSIF V_Merge_Att = 'T' AND V_Merge_Geo = 'F'
		THEN
		
			Trace('仅融合属性');
			Merge_Att(V_Field_Task_Id);
			Trace('仅融合属性完成');
		
		ELSIF V_Merge_Att = 'F' AND V_Merge_Geo = 'T'
		THEN
			Trace('仅融合几何');
			Merge_Geo(V_Field_Task_Id);
			Trace('仅融合几何完成');
		
		ELSIF V_Merge_Att = 'T' AND V_Merge_Geo = 'T'
		THEN
		
			Trace('融合属性+几何');
			Trace('先融合属性');
		
			Merge_Att(V_Field_Task_Id, V_Merge_Geo => 'T');
		
			Delete_Temp_Data;
		
			Trace('再融合几何');
		
			Merge_Geo(V_Field_Task_Id, V_Ignore_Att => 'T');
		END IF;
	
	END;
  
  function merge_log(v_log varchar2) return varchar2
    is
    v_result varchar2(200);
    begin
      
      if instr(v_log,'改名称')>0 then
         v_result:='改名称'||'|';
      end if;
      if instr(v_log,'改DPRNAME')>0 then
         v_result:=v_result||'改DPRNAME'||'|';
      end if;
      if instr(v_log,'改DPNAME')>0 then
         v_result:=v_result||'改DPNAME'||'|';
      end if;
      if instr(v_log,'改RELATION')>0 then
         v_result:=v_result||'改RELATION'||'|';
      end if;
      if instr(v_log,'改标注')>0 then
         v_result:=v_result||'改标注'||'|';
      end if;
      if instr(v_log,'改GUIDEX')>0 then
         v_result:=v_result||'改GUIDEX'||'|';
      end if;
      if instr(v_log,'改GUIDEY')>0 then
         v_result:=v_result||'改GUIDEY'||'|';
      end if;
      if instr(v_log,'改FATHERSON')>0 then
         v_result:=v_result||'改FATHERSON'||'|';
      end if;
      
      if substr(v_result,length(v_result),1)='|' then
        v_result:=substr(v_result,1,length(v_result)-1);
      end if;
      return substr(v_result,1,32);
    end;

  function merge_state(v_state varchar2) return varchar2
    is
    v_start varchar2(1);
    v_end varchar2(1);
    v_result varchar2(200);
    begin
        --删除：1
        --修改：2
        --新增：3
        if length(v_state)=1 then
          return v_state;
        end if;
        
        v_start:=substr(v_state,1,1);
        v_end:=substr(v_state,length(v_state),1);
        
/*
  多条外业记录如果STATE为新增、修改、删除的融合后的数据在子版本不存在
:1	多条外业记录如果STATE为新增、修改的融合后的数据的STATE为新增
:2	多条外业记录如果STATE为修改、修改的融合后的数据的STATE为修改
:3	多条外业记录如果STATE为修改、删除的融合后的数据的STATE为删除
:4	多条外业记录如果STATE为新增、删除的融合后的数据在子版本不存在
*/
        if v_start='3' and v_end='2' then
          return '3';
        elsif v_start='2' and v_end='2' then
          return '2';
        elsif v_start='2' and v_end='1' then
          return '1';
        else
          return v_end;
        end if;
    end;
    
	PROCEDURE Do_Merge(V_Task_Id   VARCHAR2,
										 V_Merge_Att VARCHAR2 := 'F',
										 V_Merge_Geo VARCHAR2 := 'F') IS
		V_Field_Task_Id_Cnt INTEGER := 0;
		V_Pid_In_Task_Cnt   INTEGER := 0;
		V_Pid_Same          INTEGER := 0;
	BEGIN
		Pk_History_Util.V_Seq_Id             := Pk_History_Util.V_Seq_Id + 1;
		Pk_History_Util.V_Default_Task_Id    := V_Task_Id;
		Pk_History_Util.V_Default_Operate_Id := Pk_History_Util.V_Default_Operate_Id + 1;
	
	
		Trace('检查外业数据');
		SELECT COUNT(1)
			INTO V_Pid_In_Task_Cnt
			FROM (SELECT COUNT(1)
							 FROM Au_Ix_Pointaddress t
							GROUP BY Field_Task_Id,pid
						 HAVING COUNT(1) > 1);
	
		IF V_Pid_In_Task_Cnt >= 1
		THEN
			Raise_Application_Error(-20999, '数据错误：在同一个外业任务中有相同pid的点门牌');
		END IF;
	
		SELECT COUNT(1)
			INTO V_Pid_Same
			FROM (SELECT COUNT(Pid)
							 FROM Au_Ix_Pointaddress t
							GROUP BY Pid
						 HAVING COUNT(Pid) > 1);
	
		IF V_Pid_Same = 0
		THEN
			Loop_Do_Merge(NULL, V_Merge_Att, V_Merge_Geo);
		ELSE
			FOR Rec IN (SELECT Field_Task_Id
										FROM (SELECT Field_Task_Id, MIN(Imp_Date) Min_Imp_Date
														 FROM Au_Ix_Pointaddress t
														GROUP BY Field_Task_Id)
									 ORDER BY Min_Imp_Date)
			LOOP
				Loop_Do_Merge(Rec.Field_Task_Id, V_Merge_Att, V_Merge_Geo);
			
			END LOOP;
		END IF;
    Trace('合并log,state');
    delete temp_mpa_log_state;
    delete temp_mpa_update_before;
    delete temp_mpa_update_after;
    
    insert into temp_mpa_log_state(pid,log,state)
    select pid,pk_merge_pa.merge_log(Listagg(t.log,',') Within GROUP(ORDER BY imp_date)),
           pk_merge_pa.merge_state(Listagg(t.state,',') Within GROUP(ORDER BY imp_date))
    from  (
       select *
        from AU_IX_POINTADDRESS t 
        where t.audata_id in (
        select object_id from operate_log a 
        where table_name = 'AU_IX_POINTADDRESS' group by object_id
        having count(*)>=2
        )
        order by pid,imp_date
      ) t
    group by t.pid;
    
    insert into temp_mpa_update_before
    select * from IX_POINTADDRESS a
    where a.pid in (
       select pid from temp_mpa_log_state
    );
    
    update IX_POINTADDRESS t set (log,state)=
      (select log,state from temp_mpa_log_state a where t.pid=a.pid)
     where t.pid in (select pid from temp_mpa_log_state);
    

    insert into temp_mpa_update_after
    select * from IX_POINTADDRESS a
    where a.pid in (
       select pid from temp_mpa_log_state
    );
    
    delete temp_merge_raw_operate_log;
    Pk_History_Util.Generate_Update_History('IX_POINTADDRESS', 'select a.*,b.*,''<LOG/><STATE/>'' from temp_mpa_update_before a,temp_mpa_update_after b where a.pid=b.pid');
    Trace('合并log,state:'||SQL%ROWCOUNT);
    insert into operate_log 
    select * from temp_merge_raw_operate_log;
	END;
END Pk_Merge_Pa;
/
