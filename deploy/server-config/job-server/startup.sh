CLASSPATH=./config:./lib/*

echo $CLASSPATH

export LANG=en_US.UTF-8

java -Xms4096m -Xmx8192m -cp $CLASSPATH com.navinfo.dataservice.jobframework.JobServer -denv=dev &
