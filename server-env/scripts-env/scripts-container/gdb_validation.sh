CLASSPATH=./config
for file in ./lib/*.jar;
do CLASSPATH=${CLASSPATH}:$file;
done
echo $CLASSPATH

export LANG=en_US.UTF-8

java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.JobScriptsInterface -itype gdbValidation -irequest gdb_validation.json -iresponse gdb_validation.json
