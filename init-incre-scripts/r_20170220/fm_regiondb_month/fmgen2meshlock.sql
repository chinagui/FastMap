--fm_gen2_meshlock
CREATE TABLE FM_GEN2_MESHLOCK(
MESH_ID NUMBER(8),
LOCK_STATUS NUMBER(2),
LOCK_OWNER VARCHAR2(16),
JOB_ID VARCHAR2(32)
);
CREATE INDEX IX_MESHLOCK_MESHID ON FM_GEN2_MESHLOCK(MESH_ID);
CREATE INDEX IX_MESHLOCK_OWN ON FM_GEN2_MESHLOCK(LOCK_OWNER);
CREATE INDEX IX_MESHLOCK_STAOWN ON FM_GEN2_MESHLOCK(LOCK_STATUS,LOCK_OWNER);
INSERT INTO  FM_GEN2_MESHLOCK VALUES(0，0，'GLOBAL','0');
COMMIT;
exit;




