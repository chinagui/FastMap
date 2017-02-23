--�ڴ��������洴��ָ��rtic����ص�dblink
--ORACLE,192.168.4.131,1521,orcl,fm_pid_1,fm_pid_1
create database link RTIC_IMP_DBLINK
  connect to fm_pid_1 identified by fm_pid_1
  using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = 192.168.4.131 )(PORT = 1521 )))(CONNECT_DATA = (SERVICE_NAME = orcl )))';
;
--ɾ�������õ��Ĳ�����rtic���볤�ȵĴ������ݣ�
DELETE FROM rd_link_int_rtic T WHERE LENGTH T.CODE>4095 ;
--ɾ���Ѿ����ڵģ�
DELETE FROM rtic_code@RTIC_IMP_DBLINK  C WHERE EXISTS(SELECT 1 FROM  rd_link_int_rtic t1,rd_link t2 where t2.link_pid=t1.link_pid AND RANK IN(1,2,3,4)
AND t2.mesh_id=C.MESH_ID AND T1.rank=C.RTIC_CLASS AND T1.code=C.RTIC_ID);
--�Ѵ������е�ritic����д�뵽�����
insert into rtic_code@RTIC_IMP_DBLINK (mesh_id,rtic_class,rtic_id,rtic_state,season)
select DISTINCT  t2.mesh_id,rank,code,'ʹ��','16WIN' from rd_link_int_rtic t1,rd_link t2 where t2.link_pid=t1.link_pid AND RANK IN(1,2,3,4)
;
commit;
