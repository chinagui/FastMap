INSERT INTO POINTADDRESS_EDIT_STATUS(PID,COMMIT_HIS_STATUS) SELECT PID,0 FROM IX_POINTADDRESS;
COMMIT;
EXIT;