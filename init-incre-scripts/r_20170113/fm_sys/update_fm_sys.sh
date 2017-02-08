export NLS_LANG=.AL32UTF8
source ./update_fm_sys.conf

imp $fmsys_url file=batch_rule.dmp
sqlplus $fmsys_url @./update_glm_grid_map.sql

sh ./init_check_rules.sh