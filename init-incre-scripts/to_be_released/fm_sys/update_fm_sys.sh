export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf


sqlplus $fmsys_url @./sys_update_job.sql

sqlplus $fmsys_url @./sys_add_config_rtic.sql

sqlplus $fmsys_url @./sys_add_config_multisrc.sql

sqlplus $fmsys_url @./add_fm_multisrc_sync.sql

sqlplus $fmsys_url @./add_multisrc_fm_sync.sql