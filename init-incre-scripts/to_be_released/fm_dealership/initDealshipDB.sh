export NLS_LANG=.AL32UTF8
source ./initDealershipDB.conf

sqlplus $dealership_user/$dealership_user$dealership_ip @./create_table.sql
sqlplus $dealership_user/$dealership_user$dealership_ip @./create_seq.sql
sqlplus $dealership_user/$dealership_user$dealership_ip @./create_fun.sql





