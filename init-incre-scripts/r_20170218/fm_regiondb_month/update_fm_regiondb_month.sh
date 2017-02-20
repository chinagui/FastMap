export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb_month.conf

sqlplus $fmregiondb_url @./drop_poi_columnTables.sql
imp $fmregiondb_url file=poi_column_op_conf.dmp tables=poi_column_op_conf
imp $fmregiondb_url file=poi_column_workitem_conf.dmp tables=poi_column_workitem_conf
