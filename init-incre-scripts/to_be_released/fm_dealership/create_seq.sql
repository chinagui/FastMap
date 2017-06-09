DROP SEQUENCE HISTORY_SEQ;
DROP SEQUENCE RESULT_SEQ;
DROP SEQUENCE SOURCE_SEQ;


-- Create sequence 
create sequence HISTORY_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

-- Create sequence 
create sequence RESULT_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;


-- Create sequence 
create sequence SOURCE_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;



commit;
exit;