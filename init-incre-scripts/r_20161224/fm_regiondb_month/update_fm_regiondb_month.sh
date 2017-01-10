export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb_month.conf

sqlplus $fmregiondb_url @./POI_COLUMN_OP_CONF.sql
sqlplus $fmregiondb_url @./POI_COLUMN_STATUS.sql
sqlplus $fmregiondb_url @./POI_COLUMN_WORKITEM_CONF.sql