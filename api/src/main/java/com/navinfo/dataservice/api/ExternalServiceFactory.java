package com.navinfo.dataservice.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/** 
* @ClassName: ServiceFactory 
* @author Xiao Xiaowen 
* @date 2016年5月27日 上午10:32:25 
* @Description: TODO
*  
*/
public class ExternalServiceFactory {
	private Map<String,ExternalService> services;//key:iface class name,value:implement object instance
	public  Map<String,Class<?>> serviceClassMap;//key:iface class name,value:implement class name
	private static class SingletonHolder{
		private static final ExternalServiceFactory INSTANCE = new ExternalServiceFactory();
	}
	public static ExternalServiceFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public ExternalService getExternalService(String ifaceName)throws ServiceException{
		ExternalService service =services.get(ifaceName);
		if(service==null){
			synchronized(this){
				service = services.get(ifaceName);
				if(service==null){
					service = create(ifaceName);
				}
			}
		}
		return service;
	}
	private ExternalService create(String ifaceName)throws ServiceException{
		if(serviceClassMap==null){
			loadMapping();
		}
		Class<?> clazz = serviceClassMap.get(ifaceName);
		if(clazz==null){
			throw new ServiceException("未找到对应的iface类型");
		}
		ExternalService service = null;
		try{
			service = (ExternalService)clazz.getConstructor().newInstance();
		}catch(Exception e){
			throw new ServiceException(e.getMessage(),e);
		}
		return service;
	}

	private void loadMapping()throws ServiceException{
		String mappingFile = "/com/navinfo/dataservice/api/job-class.xml";
		serviceClassMap = new HashMap<String,Class<?>>();
		//加载配置信息
		InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(mappingFile);
            if (is == null) {
                is = ExternalServiceFactory.class.getResourceAsStream(mappingFile);
            }

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String name = element.attributeValue("name");
                String className = element.attributeValue("impl");
                serviceClassMap.put(name, Class.forName(className));
            }
        } catch (Exception e) {
            throw new ServiceException("读取server和类映射文件" + mappingFile + "错误", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
	}
}
