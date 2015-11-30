create or replace package PKG_AU_GENERATION is

  -- Author  : LIYA
  -- Created : 2014/4/14 11:28:38
  -- Purpose : ��ҵ��һ���������жϽӿ�
  

  --�ӿ�1.���Ƿ��ǿ����ݿ⡣
  --˵����1�����ݿ�Ϊ�գ�0�����ݿⲻΪ��
  PROCEDURE is_db_empty ( emptyFlag  OUT int ); 
  --�ӿ�2.���Ƿ���2g���ݿ⡣
  --˵����1����2g���ݣ�0������2g����
  PROCEDURE is_2g  ( flag  OUT int ); 
  --�ӿ�2.���Ƿ���1g���ݿ⡣
  --˵����1��1g���ݣ�0������1g����
  PROCEDURE is_1g  ( flag  OUT int ); 
  --�ӿ�3��д��1g��2g����������Ϣ ���÷����������commit��rollback
  PROCEDURE insert_Generation  ( generation  in varchar2 );
    

end PKG_AU_GENERATION;
/
create or replace package body PKG_AU_GENERATION is
  /*�ж����ݿ�:AU_IX_POI��AU_IX_POINTADDRESS��AU_IX_ANNOTATION���ű�����Ϊ��
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
   /*�жϱ�ʾ��:m_parameter���AU_DATA_GENERATIONΪnull ���� m_parameter ������name=AU_DATA_GENERATION������
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
    
  -- �ӿ�1.���Ƿ��ǿ����ݿ⡣
  --����ֵ��1�������ݿ⣻0�����ǿ����ݿ�
  PROCEDURE is_db_empty ( emptyFlag  OUT int )
    IS
  BEGIN   
    --���ݿ� and ��ʶ�գ����ж����ݿ�Ϊ��
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
   
/*�����ݷǿ� and ��ʶ�գ�or (AU_DATA_GENERATION=1g)�����ж����ݿ�Ϊ1g
*/
 PROCEDURE is_1g  ( flag  OUT int )
   IS 
   generation varchar(36);
   hasGenerationPar int;
   BEGIN 
     --���ݷǿ� and ��ʶ�� Ϊ1g
     if(not is_data_empty and is_flag_empty ) then  flag:=1 ;return ; end if;    
     --AU_DATA_GENERATION=1g Ϊ 1g 
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
     --1.�����Ϸ��ж�
     IF (generation is null or  generation not in ('1g','2g'))
        THEN RAISE_APPLICATION_ERROR(-20000, '�������Ϸ�����������Ϊ�գ���ֵ��Ϊ[1g,2g] !');
     ELSE 
         --2.���»�����
         MERGE INTO M_PARAMETER T1
         USING (SELECT 'AU_DATA_GENERATION' AS NAME FROM DUAL) T2
         ON (T1.NAME=T2.NAME)
         WHEN MATCHED THEN 
            UPDATE  SET T1.PARAMETER=generation WHERE   T2.NAME='AU_DATA_GENERATION'
         WHEN NOT MATCHED THEN
           INSERT  (NAME,PARAMETER,DESCRIPTION) VALUES ('AU_DATA_GENERATION',generation,'����1g/2g');
          --COMMIT; �ɵ��÷�ȷ���Ƿ��ύ
     END IF;
     EXCEPTION
     WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('SET_PARAMETER ERROR:' || SQLERRM);
      -- ROLLBACK;
      RAISE;
     
   END;
  


end PKG_AU_GENERATION;
/
