CREATE OR REPLACE PACKAGE SAMPLE_PCK IS

  -- Author  : XUXIAOBO
  -- Created : 2012/12/1 17:32:10

  PROCEDURE SAMPLE_PROCEDURE;

END SAMPLE_PCK;
/
CREATE OR REPLACE PACKAGE BODY SAMPLE_PCK IS
  PROCEDURE SAMPLE_PROCEDURE IS
    V_SEQ_NUM NUMBER(10) := 0;
  BEGIN
  
    -- 一个站点对应多个站台    PT_PLATFORM
    -- 一个站台对应多个出入口  PT_PLATFORM_ACCESS
    -- 一条线路对应多个STRAND
    -- 多个站台对应多个STRAND
  
    -- 删除因引导坐标而删除PT_POI记录的关联表中的记录
    DELETE FROM PT_POI_NAME WHERE POI_PID NOT IN (SELECT PID FROM PT_POI);
    DELETE FROM PT_POI_FLAG WHERE POI_PID NOT IN (SELECT PID FROM PT_POI);
    delete from pt_poi_children c
     where c.child_poi_pid not in (select pid from pt_poi);
    delete from pt_poi_parent c
     where c.PARENT_POI_PID not in (select pid from pt_poi);
    delete from PT_ETA_STOP where POI_PID not in (select pid from pt_poi);
    DELETE FROM pt_poi_children C
     WHERE C.GROUP_ID NOT IN (SELECT GROUP_ID FROM pt_poi_parent);
  
    DELETE FROM IX_HAMLET_NAME
     WHERE PID NOT IN (SELECT PID FROM IX_HAMLET);
    DELETE FROM IX_HAMLET_FLAG
     WHERE PID NOT IN (SELECT PID FROM IX_HAMLET);
  
    DELETE FROM IX_POI_NAME_FLAG
     WHERE NAME_ID IN
           (SELECT NAME_ID
              FROM IX_POI_NAME
             WHERE POI_PID NOT IN (SELECT PID FROM IX_POI));
    DELETE FROM IX_POI_NAME WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_ADDRESS
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CONTACT
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_FLAG WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_ENTRYIMAGE
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_ICON WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_PHOTO WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_AUDIO WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_VIDEO WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_BUILDING
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_DETAIL
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CARRENTAL
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_PARKING
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_BUSINESSTIME
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_INTRODUCTION
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_ADVERTISEMENT
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_ATTRACTION
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_HOTEL WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_RESTAURANT
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_GASSTATION
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CHARGINGPLOT
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CHARGINGPLOT_PH
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CHARGINGSTATION
     WHERE POI_PID NOT IN (SELECT PID FROM IX_POI);
    -- ① 如果POI有父子关系，则：
    --如果它的父不在采样框中，将该POI的父子关系清除；
    --如果它的父和部分子在采样框中，则将框中的父子关系保留，框外的子清除
    DELETE FROM IX_POI_CHILDREN C
     WHERE C.CHILD_POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_PARENT P
     WHERE P.PARENT_POI_PID NOT IN (SELECT PID FROM IX_POI);
    DELETE FROM IX_POI_CHILDREN C
     WHERE C.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POI_PARENT);
    --删除孤父
    DELETE FROM IX_POI_PARENT P
     WHERE P.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POI_CHILDREN);
  
    --②  如果POI有同一关系，则只要其组成结构中有部分在采样框外，则将其同一关系清除
    DELETE FROM IX_SAMEPOI_PART
     WHERE GROUP_ID IN
           (SELECT GROUP_ID
              FROM IX_SAMEPOI_PART P
             WHERE P.POI_PID NOT IN (SELECT PID FROM IX_POI));
  
    DELETE FROM IX_SAMEPOI P
     WHERE P.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_SAMEPOI_PART);
  
    DELETE FROM IX_POINTADDRESS_NAME
     WHERE PID NOT IN (SELECT PID FROM IX_POINTADDRESS);
    DELETE FROM IX_POINTADDRESS_FLAG
     WHERE PID NOT IN (SELECT PID FROM IX_POINTADDRESS);
  
    /*--过滤点门牌的所属link
    for rec in 
    (
       SELECT P.PID FROM  IX_POINTADDRESS P  WHERE P.LOCATE_LINK_PID NOT IN (SELECT PID FROM RD_LINK)
    )
    loop 
       DELETE FROM IX_POINTADDRESS_PARENT   WHERE  PARENT_PA_PID = rec.pid;
       DELETE FROM IX_POINTADDRESS_CHILDREN   WHERE  CHILD_PA_PID = rec.pid;
       DELETE FROM IX_POINTADDRESS WHERE LOCATE_LINK_PID = rec.pid;
    end loop; */
  
    DELETE FROM IX_POINTADDRESS_CHILDREN C
     WHERE C.CHILD_PA_PID NOT IN (SELECT PID FROM IX_POINTADDRESS);
  
    DELETE FROM IX_POINTADDRESS_PARENT P
     WHERE P.PARENT_PA_PID NOT IN (SELECT PID FROM IX_POINTADDRESS);
  
    DELETE FROM IX_POINTADDRESS_CHILDREN C
     WHERE C.GROUP_ID NOT IN (SELECT GROUP_ID FROM IX_POINTADDRESS_PARENT);
    --删除孤父
    DELETE FROM IX_POINTADDRESS_PARENT P
     WHERE P.GROUP_ID NOT IN
           (SELECT GROUP_ID FROM IX_POINTADDRESS_CHILDREN);
  
    -- 索引收费站和索引IC出入口不进行样本数据制作处理
    EXECUTE IMMEDIATE 'TRUNCATE TABLE IX_TOLLGATE';
    EXECUTE IMMEDIATE 'TRUNCATE TABLE IX_IC';
  
    -- 删除没有站点的站台
    DELETE FROM PT_PLATFORM WHERE POI_PID NOT IN (SELECT PID FROM PT_POI);
  
    -- 删除没有站台的站台名
    DELETE FROM PT_PLATFORM_NAME
     WHERE PID NOT IN (SELECT PID FROM PT_PLATFORM);
  
    -- 删除孤立站台。孤立站台指没有线路经过的站台
    for rec in (select pid
                  from PT_PLATFORM
                 where pid not in -- PT_PLATFORM:  站台表
                       (select distinct PLATFORM_PID
                          from PT_STRAND_PLATFORM --  PT_STRAND_PLATFORM: STRAND与站台关系表 
                        )) loop
      delete from PT_PLATFORM_NAME where pid = rec.pid; -- PT_PLATFORM_NAME:公交站台名称表  
      delete from PT_PLATFORM_ACCESS where PLATFORM_ID = rec.pid; -- PT_PLATFORM_ACCESS: 站台与出入口关系表
      delete from PT_PLATFORM where pid = rec.pid;
    end loop;
  
    -- 删除在 PT_PLATFORM 中没有记录的 PT_STRAND_PLATFORM 中的记录
    for rec in (select platform_pid
                  from PT_STRAND_PLATFORM
                 where platform_pid not in (select pid from PT_PLATFORM)) loop
      delete from PT_STRAND_PLATFORM where platform_pid = rec.platform_pid;
    end loop;
  
    --上面删除了PT_STRAND_PLATFORM 。同步PT_STRAND 保证数据一致性
    for rec in (select pid
                  from PT_STRAND
                 where pid not in
                       (select distinct strand_pid from PT_STRAND_PLATFORM)) loop
      delete from PT_STRAND where pid = rec.pid;
      delete from PT_STRAND_NAME where PID = rec.pid;
      delete from PT_STRAND_SCHEDULE where STRAND_PID = rec.pid;
      delete from PT_STRAND_PLATFORM where STRAND_PID = rec.pid;
    end loop;
  
    -- 删除孤立线路。孤立线路指只经过一个站台的线路，
    for rec in (select STRAND_PID
                  from PT_STRAND_PLATFORM psp
                 right join PT_PLATFORM pp
                    on psp.platform_pid = pp.pid having(count(1)) <= 1
                 group by STRAND_PID) loop
      delete from PT_STRAND where PID = rec.STRAND_PID;
      delete from PT_STRAND_NAME where PID = rec.STRAND_PID;
      delete from PT_STRAND_SCHEDULE where STRAND_PID = rec.STRAND_PID;
      delete from PT_STRAND_PLATFORM where STRAND_PID = rec.STRAND_PID;
    end loop;
  
    -- 再删除孤立站台。孤立站台指没有线路经过的站台
    for rec in (select pid
                  from PT_PLATFORM
                 where pid not in -- PT_PLATFORM:  站台表
                       (select distinct PLATFORM_PID
                          from PT_STRAND_PLATFORM --  PT_STRAND_PLATFORM: STRAND与站台关系表 
                        )) loop
      delete from PT_PLATFORM_NAME where pid = rec.pid; -- PT_PLATFORM_NAME:公交站台名称表  
      delete from PT_PLATFORM_ACCESS where PLATFORM_ID = rec.pid; -- PT_PLATFORM_ACCESS: 站台与出入口关系表
      delete from PT_PLATFORM where pid = rec.pid;
    end loop;
    --删除pt_line
    delete from pt_line
     where PID not in (select distinct LINE_ID from PT_STRAND);
    delete from PT_LINE_NAME
     where PID not in (select distinct LINE_ID from PT_STRAND);
    delete from PT_ETA_LINE
     where PID not in (select distinct LINE_ID from PT_STRAND);
    --删除pt_system
    delete from pt_system
     where SYSTEM_ID not in (select distinct SYSTEM_ID from PT_LINE);
    delete from PT_ETA_SYSTEM
     where SYSTEM_ID not in (select distinct SYSTEM_ID from PT_LINE);
    --删除PT_COMPANY
    delete from PT_COMPANY
     where COMPANY_ID not in (select distinct COMPANY_ID from PT_SYSTEM);
    delete from PT_ETA_COMPANY
     where COMPANY_ID not in (select distinct COMPANY_ID from PT_SYSTEM);
  
    -- 当(公交POI(站点和出入口)表)中(种别代码)POI_KIND = 8085 ,8086 (地铁/轻轨站点 或 磁悬浮)时  站间换乘的的站点，只有两个站点都提取时才提取换乘关系
    delete from PT_TRANSFER
     where transfer_id in (select transfer_id
                             from (select transfer_id, pp.pid, TRANSFER_TYPE
                                     from PT_TRANSFER pt
                                     left outer join PT_POI pp
                                       on pt.POI_FIR = pp.pid
                                       or pt.POI_SEC = pp.pid
                                      and pp.POI_KIND in ('8085', '8086'))
                            where TRANSFER_TYPE = 0 having(count(1)) < 2
                            group by transfer_id);
  
    --- 站内换乘的的站台，只有两个站台都提取时才提取站内换乘关系
    delete from PT_TRANSFER
     where transfer_id in (select transfer_id
                             from (select transfer_id, pp.pid, TRANSFER_TYPE
                                     from PT_TRANSFER pt
                                     left outer join PT_PLATFORM pp
                                       on pt.PLATFORM_FIR = pp.pid
                                       or pt.PLATFORM_SEC = pp.pid)
                            where TRANSFER_TYPE = 1 having(count(1)) < 2
                            group by transfer_id);
  
    -- PT_PLATFORM_ACCESS（站台与出入口关系表）
    --- 站台信息表的PID字段与站台与出入口关系表的PLATFORM_ID字段匹配、并且公交POI表中的PID字段与站台与出入口关系表的ACCESS_ID字段匹配的记录原样转出
    delete from PT_PLATFORM_ACCESS
     where platform_id not in (select distinct pid from PT_PLATFORM)
        or access_id not in (select distinct pid from PT_POI);
  
    --  出入口深度信息表的 POI_PID 字段存在于满足切分原则的站台与出入口关系表的ACCESS_ID字段的记录，原样转出,否则不转出
    delete from PT_ETA_ACCESS
     where poi_pid not in
           (select distinct access_id from PT_PLATFORM_ACCESS);
  
    execute immediate 'TRUNCATE TABLE TEMP_PT_STRAND_PLATFORM1';
    execute immediate 'INSERT INTO TEMP_PT_STRAND_PLATFORM1  SELECT STRAND_PID, INTERVAL,SEQ_NUM FROM PT_STRAND_PLATFORM';
    -- 更新 pt_strand_platform 中 INTERVAL
    for rec in (select t.*,
                       rank() over(partition by t.strand_pid order by t.seq_num) as num
                  from TEMP_PT_STRAND_PLATFORM1 t) loop
      if rec.num <> 1 then
        update PT_STRAND_PLATFORM
           set interval =
               (rec.SEQ_NUM - V_SEQ_NUM) / 10000 * interval
         where strand_pid = rec.strand_pid
           and seq_num = rec.seq_num;
      end if;
      V_SEQ_NUM := rec.SEQ_NUM; --前一个SEQ_NUM
    end loop;
  
    -- execute immediate 'drop table TEMP_PT_STRAND_PLATFORM1';
    -- 更新 PT_STRAND_SCHEDULE 中 START_TIME, END_TIME
    MERGE INTO PT_STRAND_SCHEDULE a
    USING (select c.strand_pid,
                  (c.start_num - d.start_num) / 10000 start_count,
                  (d.end_num - c.end_num) / 10000 end_count
             from (select STRAND_PID,
                          max(SEQ_NUM) end_num,
                          min(SEQ_NUM) start_num
                     from PT_STRAND_PLATFORM
                    group by STRAND_PID) c,
                  TEMP_PT_STRAND_PLATFORM2 d
            where c.strand_pid = d.strand_pid) b
    ON (a.strand_pid = b.strand_pid)
    WHEN MATCHED THEN
      UPDATE
         set a.start_time = to_char(to_date('2012-08-13' || a.start_time,
                                            'yyyy-mm-dd hh24:mi:ss') +
                                    (b.start_count * a.interval) / (24 * 60),
                                    'hh24:mi'),
             a.end_time   = to_char(to_date('2012-08-13' || a.end_time,
                                            'yyyy-mm-dd hh24:mi:ss') -
                                    (b.end_count * a.interval) / (24 * 60),
                                    'hh24:mi');
    --  execute immediate 'drop table TEMP_PT_STRAND_PLATFORM2';
  
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('ERROR:' || SQLERRM);
      ROLLBACK;
      RAISE;
  END;
END SAMPLE_PCK;
/
