export NLS_LANG=.AL32UTF8

source ./*.conf

sqlplus $fmmeta_username/$fmmeta_password@$fmmeta_ip:$fmmeta_port/$fmmeta_servicename @./table_create_meta.sql

sqlplus $fmmeta_username/$fmmeta_password@$fmmeta_ip:$fmmeta_port/$fmmeta_servicename @./MetaTrigger.sql

sqlplus $fmsys_url @./insert_datahub.sql $fmmeta_username $fmmeta_password

java -Djava.awt.headless=true -cp .:ojdbc6.jar:engine-meta-0.0.1-SNAPSHOT.jar com.navinfo.dataservice.engine.meta.patternimage.PatternImageImporter $fmmeta_username $fmmeta_password $fmmeta_ip $fmmeta_port $fmmeta_servicename $image_path

sqlplus $fmmeta_username/$fmmeta_password@$fmmeta_ip:$fmmeta_port/$fmmeta_servicename @./set_pattern_updatetime.sql
