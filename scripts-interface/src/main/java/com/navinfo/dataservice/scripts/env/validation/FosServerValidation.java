package com.navinfo.dataservice.scripts.env.validation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.util.XMLUtils;
import com.navinfo.dataservice.scripts.env.validation.model.FosServer;
import org.dom4j.Element;

/** 
 * @ClassName: FosServerValidation
 * @author xiaoxiaowen4127
 * @date 2017年8月7日
 * @Description: FosServerValidation.java
 */
public class FosServerValidation implements FosEnvValidation{
	
	@Override
	public ValidationResult validation() throws Exception {
		ValidationResult result = new ValidationResult();
		List<FosServer> servers = loadConfigs();
		for(FosServer server:servers){
			//检查server
			File rootFile = new File(server.getServerDir());
			if (!rootFile.exists()) {
				result.addErrs("服务器未安装"+server.getName()+"服务");
			}
			//
			validateSysConfigXml(server,result);
			//
			validateDubboConfigXml(server,result);
		}
		return result;
	}
	
	private List<FosServer> loadConfigs()throws Exception{
		//...
		Element valXmlRoot = XMLUtils.parseRoot("tomcat_validation_config.xml");
		String serverDir = valXmlRoot.elementText("server-dir");
		String tomcatDir = serverDir+"tomcat/";
		String jobserverDir = serverDir+"job-server/";
		List<Element> eleobjs = valXmlRoot.elements("server");
		for (int i = 0; i < eleobjs.size(); i++){
			Element serobj = eleobjs.get(i);//serobj
			FosServer server = new FosServer(serobj.attributeValue("name"),serobj.attributeValue("type"));
			//添加公共的配置
			server.addSysConfig("SYS.dataSource.maxActive", "300");
			//
			if("tomcat".equals(server.getType())){
				server.setServerDir(tomcatDir);
				server.addSysConfig("datasource.sql.type", "druid");
			}else if("jobserver".equals(server.getType())){
				server.setServerDir(jobserverDir);
				server.addSysConfig("datasource.sql.type", "dbcp");
			}
			List<Element> confObjs = serobj.elements("config");
			for(int j = 0;j<confObjs.size();j++){
				Element confobj = confObjs.get(j);//confobj
				String confFile = confobj.attributeValue("file");
				Map<String,String> confs = new HashMap<String,String>();
				List<Element> keyobjs = confobj.elements("key");
				for(int m =0;m<keyobjs.size();m++){
					Element keyobj = keyobjs.get(m);
					confs.put(keyobj.attributeValue("name"), keyobj.getText());
				}
				String confName = confobj.attributeValue("name");
				if("sys".equals(confName)){
					server.setSysConfigFile(confFile);
					server.addSysConfigs(confs);
				}else if("dubbo".equals(confName)){
					server.setDubboConfigFile(confFile);
					server.addDubboConfigs(confs);
				}
			}
		}
		return null;
	}
	
	private void validateSysConfigXml(FosServer server,ValidationResult result)throws Exception{
		//SysConfig file
		String sysConfigFilePath = server.getServerDir()+server.getSysConfigFile();
		File sysConfigfile = new File(sysConfigFilePath);
		if (!sysConfigfile.exists()) {
			result.addErrs(server.getName()+"服务未配置SystemConfig.xml文件");
		}
		Element sysConfigRoot = XMLUtils.parseRoot(sysConfigFilePath);
		for(Entry<String,String> entry:server.getSysConfig().entrySet()){
			Element e = sysConfigRoot.element(entry.getKey());
			if(e==null){
				result.addErrs(server.getName()+"服务的配置文件"+server.getSysConfigFile()+"未配置"+entry.getKey()+"参数");
			}
			if(e.getText()==null||(!e.getText().equals(entry.getValue()))){
				result.addErrs(server.getName()+"服务的配置文件"+server.getSysConfigFile()+"中"+entry.getKey()+"参数配置不正确");
			}
		}
	}
	
	private void validateDubboConfigXml(FosServer server,ValidationResult result)throws Exception{
		//file
		String filePath = server.getServerDir()+server.getDubboConfigFile();
		File file = new File(filePath);
		if (!file.exists()) {
			result.addErrs(server.getName()+"服务未配置"+server.getDubboConfigFile()+"文件");
		}
		Element configRoot = XMLUtils.parseRoot(filePath);
		for(Entry<String,String> entry:server.getDubboConfig().entrySet()){
			Element e = configRoot.element(entry.getKey());
			if(e==null){
				result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置"+entry.getKey()+"参数");
			}
			if(e.getText()==null||(!e.getText().equals(entry.getValue()))){
				result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"中"+entry.getKey()+"参数配置不正确");
			}
		}
		//application
		Element appEle = configRoot.element("dubbo:application");
		if(appEle==null){
			result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置application参数");
		}

		Element monEle = configRoot.element("dubbo:monitor");
		if(monEle==null){
			result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置monitor参数");
		}
	}
	

}
