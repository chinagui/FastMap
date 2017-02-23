CLASSPATH=./config
for file in ./lib/*.jar;
do CLASSPATH=${CLASSPATH}:$file;
done
echo $CLASSPATH

export LANG=en_US.UTF-8
echo collect poi road stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface cp
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface cr

echo daily poi road stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface dp
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface dr

echo subtask stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface subtask

echo blockman stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface blockman

echo task stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface task

echo overview stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface overview

echo overview_group stat
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface group

echo import2Oracle
java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.StaticsInterface imp_oracle