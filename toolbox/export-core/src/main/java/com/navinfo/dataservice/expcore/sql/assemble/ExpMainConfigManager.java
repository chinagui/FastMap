package com.navinfo.dataservice.expcore.sql.assemble;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.expcore.exception.ExportConfigException;

/** 
 * @ClassName: ScriptsConfigManager 
 * @author Xiao Xiaowen 
 * @date 2015-11-4 上午10:19:48 
 * @Description: TODO
 *  
 */
public class ExpMainConfigManager {
	protected Logger log = LoggerRepos.getLogger(ExpMainConfigManager.class);
	protected List<ExpMainConfig> confList;
	private ExpMainConfigManager(){
		parseExpMainConfig();
	}
	private static class SingletonHolder{
		private static final ExpMainConfigManager INSTANCE = new ExpMainConfigManager();
	}
	public static final ExpMainConfigManager getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private void parseExpMainConfig(){
		try{
			confList = new ArrayList<ExpMainConfig>();
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
				Set<String> expModeSet = new HashSet<String>();
				CollectionUtils.addAll(expModeSet,expMode.split(","));
				Set<String> expFeatureSet = new HashSet<String>();
				CollectionUtils.addAll(expFeatureSet, feature.split(","));
				Set<String> expConditionSet = new HashSet<String>();
				CollectionUtils.addAll(expConditionSet, condition.split(","));
						
				ExpMainConfig conf = new ExpMainConfig(expModeSet,expFeatureSet,expConditionSet,mainScript);
				conf.setReplacerClassName(StringUtils.isEmpty(replacer)?"com.navinfo.dataservice.expcore.sql.replacer.DefauleSqlReplacer":replacer);
				confList.add(conf);
			}
		}catch(Exception e){
			log.error("解析导出脚本配置文件内容出错。");
			log.error(e.getMessage(),e);
		}
	}
    private Document getXMLDocument() throws ExportConfigException{
        InputStream is = null;
        String file = "/com/navinfo/dataservice/expcore/resources/ExpMainConfig.xml";
        try {
           
			is = this.getClass().getResourceAsStream(file);
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            return document;
        } catch (Exception e) {
        	log.error("从classpath中读取"+file+" error!");
            throw new ExportConfigException("读取文件ExpMainConfig.xml错误,请检查ExpMainConfig.xml配置文件是否存在。", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public ExpMainConfig getExpMainConfig(String expMode,String feature,String condition){
    	for(ExpMainConfig conf:confList){
    		if(conf.getExpMode().contains(expMode)
    				&&conf.getFeature().contains(feature)
    				&&conf.getCondition().contains(condition)){
    			return conf;
    		}
    	}
    	return null;
    }
}
