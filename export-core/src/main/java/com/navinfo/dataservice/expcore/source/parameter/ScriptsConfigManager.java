package com.navinfo.dataservice.expcore.source.parameter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.navicommons.utils.StringUtils;
import com.navinfo.dataservice.expcore.exception.ExportConfigException;
import com.navinfo.dataservice.commons.log.JobLogger;

/** 
 * @ClassName: ScriptsConfigManager 
 * @author Xiao Xiaowen 
 * @date 2015-11-4 上午10:19:48 
 * @Description: TODO
 *  
 */
public class ScriptsConfigManager {
	protected Logger log = Logger.getLogger(ScriptsConfigManager.class);
	protected List<ScriptsConfig> scriptsConfList;
	private ScriptsConfigManager(){
		log = JobLogger.getLogger(log);
		parseXmlScriptsConfig();
	}
	private static class SingletonHolder{
		private static final ScriptsConfigManager INSTANCE = new ScriptsConfigManager();
	}
	public static final ScriptsConfigManager getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private void parseXmlScriptsConfig(){
		try{
			scriptsConfList = new ArrayList<ScriptsConfig>();
			Document rootDoc=getXMLDocument();
			List<Element> elements = rootDoc.getRootElement().elements();
			for(Element element: elements){
				String expMode=element.attributeValue("exp-mode");
				String feature = element.attributeValue("feature");
				String condition = element.attributeValue("condition");
				String mainScript = element.attributeValue("main-script");
				String replacer = element.attributeValue("replacer");
				if(StringUtils.isEmpty(expMode)
						||StringUtils.isEmpty(feature)
						||StringUtils.isEmpty(condition)
						||StringUtils.isEmpty(mainScript)){
					log.warn("EXPORT-WARN:导出脚本配置文件ExpScriptsConfig.xm中存在exp-mode、feature、condition或者mainScript属性为空，该配置无效，已忽略。");
					continue;
				}
				ScriptsConfig conf = new ScriptsConfig(expMode,feature,condition,mainScript);
				conf.setReplacerClassName(StringUtils.isEmpty(replacer)?"com.navinfo.dataservice.expcore.sql.replacer.DefauleSqlReplacer":replacer);
				scriptsConfList.add(conf);
			}
		}catch(Exception e){
			log.error("解析导出脚本配置文件内容出错。");
			log.error(e.getMessage(),e);
		}
	}
    private Document getXMLDocument() throws ExportConfigException{
        InputStream is = null;
        String file = "/com/navinfo/dataservice/expcore/resources/ExpScriptsConfig.xml";
        try {
           
			is = this.getClass().getResourceAsStream(file);
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            return document;
        } catch (Exception e) {
        	log.error("从classpath中读取"+file+" error!");
            throw new ExportConfigException("读取文件ExpParamConfig.xml错误,请检查ExpParamConfig.xml配置文件是否存在。", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public ScriptsConfig getScriptsConfig(String expMode,String feature,String condition){
    	for(ScriptsConfig conf:scriptsConfList){
    		if(conf.getExpMode().equals(expMode)
    				&&conf.getFeature().equals(feature)
    				&&conf.getCondition().equals(condition)){
    			return conf;
    		}
    	}
    	return null;
    }
}
