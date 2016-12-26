export NLS_LANG=.AL32UTF8
source ./update_fm_man.conf


sqlplus $fmman_url @./update_fm_man.sql

