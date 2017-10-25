export NLS_LANG=.AL32UTF8
source ./update_fm_regiondb_month.conf

sqlplus $fmregiondb_url @./update_fm_charge.sql TEMP_PLOT_20171013 TEMP_CHARGE_20171013
