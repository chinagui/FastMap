export NLS_LANG=.AL32UTF8
source ./update_glm_tables.conf
sqlplus $fmsys_url @./update_glm_tables.sql