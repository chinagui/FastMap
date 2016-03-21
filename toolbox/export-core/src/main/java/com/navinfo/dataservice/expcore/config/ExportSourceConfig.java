package com.navinfo.dataservice.expcore.config;

import com.navinfo.dataservice.datahub.model.OracleSchema;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.JobLogger;

/**
 * 
* @ClassName: ExportSourceConfig 
* @author Xiao Xiaowen 
* @date 2015-11-3 下午3:38:29 
* @Description: 关于导出源的配置参数
*
 */
public class ExportSourceConfig {
	 
	protected OracleSchema sourceSchema;
	protected int tempResourceIndex;//暂不支持内部自己分配临时表资源,支持后放到OracleSource中生成,接口调用无须知道临时表资源

    public ExportSourceConfig(String xmlConfig){
    	parseByXmlConfig(xmlConfig);
    }
    public ExportSourceConfig(){
    	
    }


	@Override
	public String toString() {
		return "";
	}
	public void parseByXmlConfig(String xmlConfig){
		
	}
	private void parseByJsonConfig(String jsonConfig){
		
	}
}
