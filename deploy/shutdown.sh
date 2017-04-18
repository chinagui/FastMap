export NLS_LANG=.AL32UTF8
source ./*.conf
#tomcat part
ps ux|grep tomcat-datahub |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-dropbox |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-edit |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-fcc |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-job |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-man |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-metadata |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-render |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-statics |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-column |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-row |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-sys |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
ps ux|grep tomcat-mapspotter |grep $svr_dir |grep -v grep | cut -c 9-15 | xargs kill -s 9
#job server part
ps ux|grep JobServer |grep $jobserver_proc_identifer |grep -v grep | cut -c 9-15 | xargs kill -s 9
