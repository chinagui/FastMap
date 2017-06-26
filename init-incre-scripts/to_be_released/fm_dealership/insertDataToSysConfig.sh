export NLS_LANG=.AL32UTF8
source ./db.conf

sqlplus $fmsys_url @./insertDataToSysConfig.sql $info_pass $info_feedback






