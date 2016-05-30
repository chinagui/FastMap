package com.navinfo.dataservice.jobframework.runjob;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.exception.ConfigParseException;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobTypeNotFoundException;

/** 
* @ClassName: JobCreateStrategy 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午3:25:39 
* @Description: TODO
*/
public class JobCreateStrategy {
	private static Logger log = LoggerRepos.getLogger(JobCreateStrategy.class);
	public static Map<String,Class<?>> jobClassMap;
	public static AbstractJob create(JobInfo jobInfo)throws JobTypeNotFoundException,JobCreateException{
		if(jobClassMap==null){
			loadMapping();
		}
		Class<?> clazz = jobClassMap.get(jobInfo.getType().toString());
		if(clazz==null){
			throw new JobTypeNotFoundException("未找到对应的任务类型");
		}
		AbstractJob job = null;
		try{
			job = (AbstractJob)clazz.getConstructor(JobInfo.class).newInstance(jobInfo);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobCreateException(e.getMessage(),e);
		}
		return job;
	}
	public static AbstractJob createAsSubJob(JobInfo jobInfo,AbstractJob parent)throws JobTypeNotFoundException,JobCreateException{
		AbstractJob job = create(jobInfo);
		job.setParent(parent);
		return job;
	}
	private static void loadMapping(){
		String mappingFile = "/com/navinfo/dataservice/jobframework/job-class.xml";
		jobClassMap = new HashMap<String,Class<?>>();
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
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String name = element.attributeValue("name");
                String className = element.attributeValue("class");
                jobClassMap.put(name, Class.forName(className));
            }
        } catch (Exception e) {
        	log.error(e.getMessage());
            throw new ConfigParseException("读取job和类映射文件" + mappingFile + "错误", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
        }
	}
}
