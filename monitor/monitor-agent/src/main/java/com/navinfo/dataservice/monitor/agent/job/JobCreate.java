package com.navinfo.dataservice.monitor.agent.job;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.commons.exception.ConfigParseException;
import com.navinfo.dataservice.commons.log.LoggerRepos;


/**
 * 任务配置xml解析类
 * @ClassName JobCreate
 * @author Han Shaoming
 * @date 2017年6月26日 上午11:48:00
 * @Description TODO
 */
public class JobCreate {
	
	protected static Logger log = LoggerRepos.getLogger(JobCreate.class);
	
	@SuppressWarnings("unchecked")
	public static List<JobConfig> getJobs() throws Exception{
		List<JobConfig> jobs = new ArrayList<JobConfig>();
		String configFile = "/jobConfig.xml";
		//加载管理库的信息
		InputStream in = null;
        log.info("parse file " + configFile);
        try {
        	in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            if (in == null) {
            	in = JobCreate.class.getResourceAsStream(configFile);
            }

            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String name = element.elementText("name");
                String activity = element.elementText("activity");
                String scanPeriod = element.elementText("scanPeriod");
                String className = element.elementText("className");
                log.info("name:"+name+",activity:"+activity+",scanPeriod"+scanPeriod+",className:"+className);
                
                JobConfig jobConfig = new JobConfig();
                jobConfig.setName(name);
				
				if ("true".equals(activity))
				{
					jobConfig.setActivity(true);
				}
				else
				{
					jobConfig.setActivity(false);
				}
				jobConfig.setScanPeriod(scanPeriod);
				jobConfig.setClassName(className);
				jobs.add(jobConfig);
            }
        } catch (Exception e) {
        	log.error(e.getMessage(),e);
            throw new ConfigParseException("读取job和类映射文件" + configFile + "错误", e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            	e.printStackTrace();
            	log.error(e.getMessage(),e);
            }
        }
		return jobs;
	}

}
