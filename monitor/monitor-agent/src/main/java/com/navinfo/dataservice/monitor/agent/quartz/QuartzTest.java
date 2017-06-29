package com.navinfo.dataservice.monitor.agent.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QuartzTest {

	public static void main(String[] args) {  
        try {  
            String job_name = "动态任务调度service"; 
            String tomcat_name = "动态任务调度tomcat";
            QuartzManager.addJob(job_name, ServiceJob.class, "0/5 * * * * ?");
            QuartzManager.addJob(tomcat_name, TomcatJob.class, "0/10 * * * * ?");
            QuartzManager.startJobs();
            Thread.sleep(50000);  
            System.out.println("【移除定时】开始...");    
            QuartzManager.removeJob(tomcat_name);    
            System.out.println("【移除定时】成功");    
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
      
    public static String formatDateByPattern(Date date,String dateFormat){    
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);    
        String formatTimeStr = null;    
        if (date != null) {    
            formatTimeStr = sdf.format(date);    
        }    
        return formatTimeStr;    
    }
    
    public static String getCron(java.util.Date  date){    
        String dateFormat="ss mm HH dd MM ? yyyy";    
        return formatDateByPattern(date, dateFormat);    
     } 
}
