export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf

sqlplus $fmsys_url @./sys_add_config.sql