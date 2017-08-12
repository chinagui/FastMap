package com.navinfo.dataservice.scripts.env.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.util.XMLUtils;
import com.navinfo.dataservice.scripts.env.validation.model.FosServer;

import org.apache.log4j.Logger;
import org.dom4j.Element;

/** 
 * @ClassName: FosServerValidation
 * @author xiaoxiaowen4127
 * @date 2017年8月7日
 * @Description: FosServerValidation.java
 */
public class FosServerValidation implements FosEnvValidation{
	protected Logger log = Logger.getLogger(XMLUtils.class);
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
		List<FosServer> servers = new ArrayList<FosServer>();
		//...
		Element valXmlRoot = XMLUtils.parseXmlFile("server_validation_config.xml");
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
				server.setServerDir(tomcatDir+server.getName()+"/");
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
			log.info("parsed server:"+server.getName());
			servers.add(server);
		}
		return servers;
	}
	
	private void validateSysConfigXml(FosServer server,ValidationResult result)throws Exception{
		//SysConfig file
		String sysConfigFilePath = server.getServerDir()+server.getSysConfigFile();
		log.info("sys xml file:"+sysConfigFilePath);
		File sysConfigfile = new File(sysConfigFilePath);
		if (!sysConfigfile.exists()) {
			result.addErrs(server.getName()+"服务未配置SystemConfig.xml文件");
			return;
		}
		InputStream is = null;
		try{
			is = new FileInputStream(sysConfigfile);
			Element sysConfigRoot = XMLUtils.parseXmlFile(is);
			for(Entry<String,String> entry:server.getSysConfig().entrySet()){
				Element e = sysConfigRoot.element(entry.getKey());
				if(e==null){
					result.addErrs(server.getName()+"服务的配置文件"+server.getSysConfigFile()+"未配置"+entry.getKey()+"参数");
					continue;
				}
				if(e.getText()==null||(!e.getText().equals(entry.getValue()))){
					result.addErrs(server.getName()+"服务的配置文件"+server.getSysConfigFile()+"中"+entry.getKey()+"参数配置不正确");
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
		}
	}
	
	private void validateDubboConfigXml(FosServer server,ValidationResult result)throws Exception{
		//file
		if("null".equals(server.getDubboConfigFile())){
			return;
		}
		String filePath = server.getServerDir()+server.getDubboConfigFile();
		log.info("dubbo xml file:"+filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			result.addErrs(server.getName()+"服务未配置"+server.getDubboConfigFile()+"文件");
			return;
		}

		InputStream is = null;
		try{
			is = new FileInputStream(file);

			Element configRoot = XMLUtils.parseXmlFile(is);
			for(Entry<String,String> entry:server.getDubboConfig().entrySet()){
				Element e = configRoot.element(entry.getKey());
				if(e==null){
					result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置"+entry.getKey()+"参数");
					continue;
				}

				if("consumer".equals(entry.getKey())){
					String[] vs = entry.getValue().split(",");
					if(e.attributeValue("timeout")==null||(!e.attributeValue("timeout").equals(vs[0]))){
						result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"中"+entry.getKey()+"参数配置不正确");
					}
					if(e.attributeValue("retries")==null||(!e.attributeValue("retries").equals(vs[1]))){
						result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"中"+entry.getKey()+"参数配置不正确");
					}
				}else if("registry".equals(entry.getKey())){
					if(e.attributeValue("address")==null||(!e.attributeValue("address").equals(entry.getValue()))){
						result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"中"+entry.getKey()+"参数配置不正确");
					}
				}
			}
			//application
			Element appEle = configRoot.element("application");
			if(appEle==null){
				result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置application参数");
			}else{
				if(appEle.attributeValue("name")==null||(!hasDigit(appEle.attributeValue("name")))){
					result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置application参数");
				}
			}

			Element monEle = configRoot.element("monitor");
			if(monEle==null){
				result.addErrs(server.getName()+"服务的配置文件"+server.getDubboConfigFile()+"未配置monitor参数");
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
		}
	}
	public static boolean hasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }
	public static void main(String[] args) {
		String file = "F:\\dubbo-app-scripts.xml";
		InputStream is = null;
		try{
			is = new FileInputStream(file);

			Element configRoot = XMLUtils.parseXmlFile(is);
			
			//application
			Element appEle = configRoot.element("application");

			System.out.println(appEle.attributeValue("name"));

			Element monEle = configRoot.element("monitor");

			System.out.println(monEle.attributeValue("protocol"));
			

			Element myele = configRoot.element("my");
			if(myele.getText()==null){
				System.out.println("null obj");
			}else{
				System.out.println(myele.getText());
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
		}
	}

}
