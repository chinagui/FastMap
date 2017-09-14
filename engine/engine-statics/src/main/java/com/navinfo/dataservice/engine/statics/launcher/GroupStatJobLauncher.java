package com.navinfo.dataservice.engine.statics.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.commons.exception.ConfigParseException;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/** 
 * @ClassName: GroupStatJobLauncher
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: GroupStatJobLauncher.java
 */
public class GroupStatJobLauncher {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	protected GroupStatJob[] jobs;
	
	private GroupStatJobLauncher() {
	}

	private static class SingletonHolder {
		private static final GroupStatJobLauncher INSTANCE = new GroupStatJobLauncher();
	}

	public static GroupStatJobLauncher getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void init()throws Exception{
		String mappingFile = "/stat-job-group.xml";
		//加载管理库的信息
		InputStream is = null;
        log.debug("parse file " + mappingFile);
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(mappingFile);
            if (is == null) {
                is = JobCreateStrategy.class.getResourceAsStream(mappingFile);
            }

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            jobs = new GroupStatJob[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String name = element.attributeValue("name");
                String starter = element.attributeValue("starter");
                String requestClassName = element.attributeValue("request");

                GroupStatJob job = new GroupStatJob();
                job.setGroupJobType(name);
                job.setGroupJobStarter(starter);
                List<Element> subElements =element.elements();
                Set<String> subJobs = new HashSet<String>();
                for(int j = 0;j<subElements.size();j++){
                	Element subElement = subElements.get(j);
                	subJobs.add(subElement.attributeValue("name"));
                }
                if(subJobs.size()==0){
                	throw new Exception("job name:"+name+"的配置信息错误，未包含任何子job信息");
                }
            	job.setSubJobs(subJobs);
            	jobs[i]=job;
            }
            
        } catch (Exception e) {
        	log.error(e.getMessage(),e);
            throw new ConfigParseException("读取job和类映射文件" + mappingFile + "错误:"+e.getMessage(), e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
        }
	}
	
	public void exchange(String identify,JSONObject identifyJson,String jobType)throws Exception{
		if(jobs!=null){
			//log.info("1");
			for(GroupStatJob job:jobs){
				//log.info("1.1");
				job.trigger(identify,identifyJson, jobType);
			}
		}
	}
}
