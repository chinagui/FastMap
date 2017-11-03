WHENEVER SQLERROR CONTINUE;

UPDATE POI_COLUMN_OP_CONF
   SET SUBMIT_CKRULES =
       (SELECT T.SUBMIT_CKRULES
          FROM POI_COLUMN_OP_CONF T
         WHERE T.FIRST_WORK_ITEM = 'poi_address'
           AND T.SECOND_WORK_ITEM = 'addrSplit') || ',FM-A09-11'
 WHERE FIRST_WORK_ITEM = 'poi_address'
   AND SECOND_WORK_ITEM = 'addrSplit';
   
INSERT INTO POI_COLUMN_WORKITEM_CONF
  (ID, FIRST_WORK_ITEM, SECOND_WORK_ITEM, WORK_ITEM_ID, CHECK_FLAG, TYPE)
VALUES
  ((SELECT MAX(TO_NUMBER(ID)) + 1 FROM POI_COLUMN_WORKITEM_CONF T),
   'poi_address',
   'addrSplit',
   'FM-A09-11',
   2,
   1);

COMMIT;
EXIT;