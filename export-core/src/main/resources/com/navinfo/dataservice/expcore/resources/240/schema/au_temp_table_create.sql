--如果修改此问题，添加或删除临时表，需要修改 ExportSource
--ExportSource :if (tables.size() != 16)
--AuDataExpFilter deleteOldBatchData也要修改

/*==============================================================*/
/* TABLE: AU_MARK任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_MARK  (
   MARK_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID             NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_MARK_BATCHID ON TMAU_EXP_AU_MARK(BATCH_ID,MARK_ID);



/*==============================================================*/
/* TABLE: AU_TOPOIMAGE任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_TOPOIMAGE  (
   IMAGE_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID             NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_TOPOIMAGE_BATCHID ON  TMAU_EXP_AU_TOPOIMAGE(BATCH_ID,IMAGE_ID);

/*==============================================================*/
/* TABLE: AU_SERIESPHOTO任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_SERIESPHOTO  (
   PHOTO_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID             NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_SERIESPHOTO_BATCHID ON  TMAU_EXP_AU_SERIESPHOTO(BATCH_ID,PHOTO_ID);


/*==============================================================*/
/* TABLE: AU_GPSTRACK_GROUP任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_GPSTRACK_GROUP  (
   GROUP_ID          NUMBER(10)                      NOT NULL,
   BATCH_ID             NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_GPSTRACK_BATCHID ON  TMAU_EXP_AU_GPSTRACK_GROUP(BATCH_ID,GROUP_ID);



/*==============================================================*/
/* TABLE: AU_ADAS_MARK任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_ADAS_MARK (
   MARK_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID             NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_ADASMARK_BATCHID ON  TMAU_EXP_AU_ADAS_MARK(BATCH_ID,MARK_ID);

/*==============================================================*/
/* TABLE: AU_ADAS_GPSTRACK任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_ADAS_GPSTRACK (
   GPSTRACK_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID                 NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_ADASGPSTRACK_BATCHID ON  TMAU_EXP_AU_ADAS_GPSTRACK(BATCH_ID,GPSTRACK_ID);


/*==============================================================*/
/* TABLE: AU_IX_POI任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_IX_POI (
   AUDATA_ID              NUMBER(10)                    NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_POI_BATCHID ON  TMAU_EXP_AU_IX_POI(BATCH_ID,AUDATA_ID);



/*==============================================================*/
/* TABLE: AU_IX_POINTADDRESS任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_IX_POINTADDRESS (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_POINTADDRESS_BATCHID ON  TMAU_EXP_AU_IX_POINTADDRESS(BATCH_ID,AUDATA_ID);

/*==============================================================*/
/* TABLE: AU_IX_ANNOTATION任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_IX_ANNOTATION (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_ANNOTATION_BATCHID  ON TMAU_EXP_AU_IX_ANNOTATION(BATCH_ID,AUDATA_ID);


/*==============================================================*/
/* TABLE: AU_PT_POI任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_POI (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_PT_POI_BATCHID ON  TMAU_EXP_AU_PT_POI(BATCH_ID,AUDATA_ID);



/*==============================================================*/
/* TABLE: AU_PT_PLATFORM任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_PLATFORM (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_PT_PLATFORM_BATCHID ON  TMAU_EXP_AU_PT_PLATFORM(BATCH_ID,AUDATA_ID);

/*==============================================================*/
/* TABLE: AU_PT_LINE任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_LINE (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_PT_LINE_BATCHID ON  TMAU_EXP_AU_PT_LINE(BATCH_ID,AUDATA_ID);


/*==============================================================*/
/* TABLE: AU_PT_STRAND任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_STRAND (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_PT_STRAND_BATCHID ON  TMAU_EXP_AU_PT_STRAND(BATCH_ID,AUDATA_ID);



/*==============================================================*/
/* TABLE: AU_PT_COMPANY任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_COMPANY (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_PT_COMPANY_BATCHID  ON TMAU_EXP_AU_PT_COMPANY(BATCH_ID,AUDATA_ID);



/*==============================================================*/
/* TABLE: AU_PT_SYSTEM任务分配表                                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_SYSTEM (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX IDX_EXP_PT_SYSTEM_BATCHID  ON TMAU_EXP_AU_PT_SYSTEM(BATCH_ID,AUDATA_ID);


/*==============================================================*/
/* TABLE: TMAU_EXP_AU_PT_TRANSFER                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_TRANSFER (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX TMAUEXPPTTRANSFER_BATCHID  ON TMAU_EXP_AU_PT_TRANSFER(BATCH_ID,AUDATA_ID);



/*==============================================================*/
/* TABLE: TMAU_EXP_AU_PT_ETA_ACCESS                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_ETA_ACCESS (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX TMAUEXPPTETAACCESS_BATCHID  ON TMAU_EXP_AU_PT_ETA_ACCESS(BATCH_ID,AUDATA_ID);




/*==============================================================*/
/* TABLE: TMAU_EXP_AU_PT_ETA_STOP                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_ETA_STOP (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX TMAUEXPPTETASTOP_BATCHID  ON TMAU_EXP_AU_PT_ETA_STOP(BATCH_ID,AUDATA_ID);



/*==============================================================*/
/* TABLE: TMAU_EXP_AU_PT_ETA_LINE                               */
/*==============================================================*/
CREATE TABLE TMAU_EXP_AU_PT_ETA_LINE (
   AUDATA_ID              NUMBER(10)                      NOT NULL,
   BATCH_ID               NUMBER(10)                      NOT NULL
);
CREATE INDEX TMAUEXPPTETALINE_BATCHID  ON TMAU_EXP_AU_PT_ETA_LINE(BATCH_ID,AUDATA_ID);


/*==============================================================*/
/* TABLE: 外业导出时，field_task任务号表                                               */
/*==============================================================*/
create table TMAU_EXP_AU_FIELDTASK
(
  ID NUMBER not null,
  BATCH_ID NUMBER(10) not null
)
;
create index IDX_EXP_AU_FIELDTASK_BATCHID on TMAU_EXP_AU_FIELDTASK (BATCH_ID, ID);


create table TMAU_EXP_AU_IX_POI_RP
(
  AUDATA_ID NUMBER(10) not null,
  BATCH_ID  NUMBER(10) not null
)
;
create index IDX_EXP_AU_IX_POI_RP on TMAU_EXP_AU_IX_POI_RP (BATCH_ID, AUDATA_ID);

create table TMAU_EXP_AU_IX_POI_NOKIA
(
  AUDATA_ID NUMBER(10) not null,
  BATCH_ID  NUMBER(10) not null
)
;
create index IDX_EXP_AU_IX_POI_NOKIA on TMAU_EXP_AU_IX_POI_NOKIA (BATCH_ID, AUDATA_ID);















