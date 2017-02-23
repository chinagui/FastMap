-- Create database link 
create database link &1
  connect to &2 identified by &3
  using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = &4 )(PORT = &5 )))(CONNECT_DATA = (SERVICE_NAME = &6 )))';
exit;