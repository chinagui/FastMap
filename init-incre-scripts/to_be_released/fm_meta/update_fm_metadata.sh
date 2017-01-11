export NLS_LANG=.AL32UTF8
source ./update_fm_metadata.conf


sqlplus $fmmeta_url @./update_metadata_translate_table.sql


