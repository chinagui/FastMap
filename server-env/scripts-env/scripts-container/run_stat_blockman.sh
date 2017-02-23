CLASSPATH=./config
for file in ./lib/*.jar;
do CLASSPATH=${CLASSPATH}:$file;
done
echo $CLASSPATH

export LANG=en_US.UTF-8

echo blockman stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface blockman