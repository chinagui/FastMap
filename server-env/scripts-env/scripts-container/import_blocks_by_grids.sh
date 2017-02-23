CLASSPATH=./config
for file in ./lib/*.jar;
do CLASSPATH=${CLASSPATH}:$file;
done
echo $CLASSPATH

export LANG=en_US.UTF-8

java -Xms512m -Xmx2000m -cp $CLASSPATH com.navinfo.dataservice.scripts.ToolScriptsInterface -iclass ImportBlockByGrid -irequest import_blocks_by_grids.json &
