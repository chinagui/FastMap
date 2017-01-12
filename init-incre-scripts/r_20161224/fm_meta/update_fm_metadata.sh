export NLS_LANG=.AL32UTF8
source ./update_fm_metadata.conf


sqlplus $fmmeta_url @./SC_POINT_SPEC_KINDCODE_NEW.sql

sqlplus $fmmeta_url @./word_kind.sql

