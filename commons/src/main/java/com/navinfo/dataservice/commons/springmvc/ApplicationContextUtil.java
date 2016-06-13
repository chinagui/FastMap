package com.navinfo.dataservice.commons.springmvc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/** 
* @ClassName: FakeRemoteObjectGetter 
* @author Xiao Xiaowen 
* @date 2016年3月23日 上午11:22:53 
* @Description: TODO
*/
public class ApplicationContextUtil implements  ApplicationContextAware {

	private static ApplicationContext applicationContext = null;
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	public static  ApplicationContext getApplicationContext() {  
        return applicationContext;  
    }
	 
    public static Object getBean(String name) throws BeansException {  
        return applicationContext.getBean(name);  
    }  

    public static Object getBean(String name, Class requiredType) throws BeansException {  
        return applicationContext.getBean(name, requiredType);  
    }  
            
    public static boolean containsBean(String name) {  
         return applicationContext.containsBean(name);  
    }  
           
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {  
          return applicationContext.isSingleton(name);  
    }  
           
           
    public static Class getType(String name) throws NoSuchBeanDefinitionException {  
         return applicationContext.getType(name);  
    }  
           
           
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {  
         return applicationContext.getAliases(name);  
    }  

}
