export NLS_LANG=.AL32UTF8
source ./update_fm_metadata.conf


sqlplus $fmmeta_url @./SC_POINT_CODE2LEVEL.sql


