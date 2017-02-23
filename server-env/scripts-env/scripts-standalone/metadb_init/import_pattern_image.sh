export NLS_LANG=.AL32UTF8
source ./import_pattern_image.conf
java -Djava.awt.headless=true -cp .:ojdbc6.jar:engine-meta-0.0.1-SNAPSHOT.jar com.navinfo.dataservice.engine.meta.patternimage.PatternImageImporter $ora_username $ora_password $ora_ip $ora_port $ora_servicename $image_path

