create or replace package PKG_AU_GENERATION is

  -- Author  : LIYA
  -- Created : 2014/4/14 11:28:38
  -- Purpose : 外业库一二代数据判断接口
  

  --接口1.：是否是空数据库。
  --说明：1：数据库为空；0：数据库不为空
  PROCEDURE is_db_empty ( emptyFlag  OUT int ); 
  --接口2.：是否是2g数据库。
  --说明：1：是2g数据；0：不是2g数据
  PROCEDURE is_2g  ( flag  OUT int ); 
  --接口2.：是否是1g数据库。
  --说明：1：1g数据；0：不是1g数据
  PROCEDURE is_1g  ( flag  OUT int ); 
  --接口3：写入1g或2g参数配置信息 调用方负责事务的commit和rollback
  PROCEDURE insert_Generation  ( generation  in varchar2 );
    

end PKG_AU_GENERATION;
/
create or replace package body PKG_AU_GENERATION is
  /*判断数据空:AU_IX_POI，AU_IX_POINTADDRESS，AU_IX_ANNOTATION三张表数据为空
  */
  function is_data_empty return boolean is
    poiCount number ;
    addressCount number;
    annotationCount number;
    begin
      select count(1) into poiCount  from au_ix_poi where rownum=1 ; 
      select count(1) into addressCount from au_ix_pointaddress where rownum=1;
      select count(1) into annotationCount from au_ix_annotation where rownum=1;
      return poiCount=0 and addressCount=0 and annotationCount=0;
    end;
   /*判断标示空:m_parameter表的AU_DATA_GENERATION为null 或者 m_parameter 不存在name=AU_DATA_GENERATION的数据
   */
  function is_flag_empty return boolean is
    hasGenerationPar number;
    begin
     SELECT COUNT(1)
       INTO hasgenerationpar
       FROM m_parameter p
      WHERE
      /* p.name = 'AU_DATA_GENERATION' OR*/ 
         p.name = 'AU_DATA_GENERATION' AND p.parameter is not  NULL;
     return hasGenerationPar=0;
    end; 
    
  -- 接口1.：是否是空数据库。
  --返回值：1：空数据库；0：不是空数据库
  PROCEDURE is_db_empty ( emptyFlag  OUT int )
    IS
  BEGIN   
    --数据空 and 标识空，则判定数据库为空
    if(is_data_empty and is_flag_empty) then emptyFlag:=1;
    else
       emptyFlag:=0;
    END IF;
    
  END;


 PROCEDURE is_2g  ( flag  OUT int )
   IS 
   generation varchar(36);
   hasGenerationPar int;
   BEGIN 
     select count(1) INTO hasGenerationPar from M_PARAMETER P WHERE P.NAME='AU_DATA_GENERATION';
     IF hasGenerationPar=1 THEN
       select P.PARAMETER INTO generation  from m_parameter p where p.name='AU_DATA_GENERATION';
       IF (generation='2g') then flag:=1;
       ELSE flag:=0;
       END  IF;
     ELSE flag:=0;
     END IF;
   END;
   
/*（数据非空 and 标识空）or (AU_DATA_GENERATION=1g)，则判定数据库为1g
*/
 PROCEDURE is_1g  ( flag  OUT int )
   IS 
   generation varchar(36);
   hasGenerationPar int;
   BEGIN 
     --数据非空 and 标识空 为1g
     if(not is_data_empty and is_flag_empty ) then  flag:=1 ;return ; end if;    
     --AU_DATA_GENERATION=1g 为 1g 
     select count(1) INTO hasGenerationPar from M_PARAMETER P WHERE P.NAME='AU_DATA_GENERATION';
     IF hasGenerationPar=1 THEN 
          select P.PARAMETER INTO generation  from m_parameter p where p.name='AU_DATA_GENERATION';
          IF (generation='1g') then flag:=1;
          ELSE flag:=0;
       END IF;
     ELSE  flag:=0;
     END IF;     
   END;
   
 
   
   
   PROCEDURE insert_Generation  ( generation  in varchar2 )
   IS
   BEGIN
     --1.参数合法判断
     IF (generation is null or  generation not in ('1g','2g'))
        THEN RAISE_APPLICATION_ERROR(-20000, '参数不合法：参数不能为空，且值域为[1g,2g] !');
     ELSE 
         --2.更新或新增
         MERGE INTO M_PARAMETER T1
         USING (SELECT 'AU_DATA_GENERATION' AS NAME FROM DUAL) T2
         ON (T1.NAME=T2.NAME)
         WHEN MATCHED THEN 
            UPDATE  SET T1.PARAMETER=generation WHERE   T2.NAME='AU_DATA_GENERATION'
         WHEN NOT MATCHED THEN
           INSERT  (NAME,PARAMETER,DESCRIPTION) VALUES ('AU_DATA_GENERATION',generation,'设置1g/2g');
          --COMMIT; 由调用方确认是否提交
     END IF;
     EXCEPTION
     WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('SET_PARAMETER ERROR:' || SQLERRM);
      -- ROLLBACK;
      RAISE;
     
   END;
  


end PKG_AU_GENERATION;
/
