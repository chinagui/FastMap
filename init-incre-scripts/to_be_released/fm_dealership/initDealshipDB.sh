#export NLS_LANG=.AL32UTF8
export LANG=en_US.UTF-8
source ./initDealershipDB.conf

sqlplus $dealership_user/$dealership_user$dealership_ip @./create_table_sequence.sql






