package com.navinfo.dataservice.engine.limit.commons.config;

import com.navinfo.dataservice.engine.limit.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.engine.limit.commons.database.oracle.MyDriverManagerDataSource;
import com.navinfo.dataservice.engine.limit.commons.database.navi.QueryRunner;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 只有包内部使用
 * 支持动态修改的配置文件类
 * 加入observer列表中的对象需要谨慎，一般随系统启动到关闭都存在的对象/线程才能加入，任务类内部/短线程一般不能加入
 * @author xiaoxiaowen4127
 *
 */
class DynamicSystemConfig extends Observable implements SystemConfig {
	private static Logger log = Logger.getLogger(DynamicSystemConfig.class);
	private static class SingletonHolder{
		private static final DynamicSystemConfig INSTANCE = new DynamicSystemConfig();
	}
	public static final DynamicSystemConfig getInstance(){
		return SingletonHolder.INSTANCE;
	}

	private static final String defaultConfigFile = "SystemConfig.xml";
	private Map<String,String> dynamicConfigMap = new ConcurrentHashMap<String,String>();
	private DynamicSystemConfig(){
		loadRawConfig(defaultConfigFile);
		loadRawConfig("/SystemConfig.xml");
		loadDynamicConfig();
	}
	private void loadRawConfig(String configFile){
		//加载管理库的信息
		InputStream is = null;
        log.debug("parse file " + configFile);
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            if (is == null) {
                is = DynamicSystemConfig.class.getResourceAsStream(configFile);
            }

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String key = element.getName();
                String value = element.getTextTrim();
                dynamicConfigMap.put(key, value);
            }
        } catch (Exception e) {
        	log.error(e.getMessage());
        	log.warn("加载配置文件"+configFile+"错误，已忽略。。。");
            //throw new ConfigParseException("读取文件" + configFile + "错误", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
        }
	}
	public void loadDynamicConfig(){
		Connection conn = null;
		try{
			QueryRunner runner = new QueryRunner();
			DriverManagerDataSource dataSource = new MyDriverManagerDataSource();
			String driveClassName = dynamicConfigMap.get("SYS.jdbc.driverClassName");
			String url = dynamicConfigMap.get("SYS.jdbc.url");
			String username = dynamicConfigMap.get("SYS.jdbc.username");
			String pwd = dynamicConfigMap.get("SYS.jdbc.password");
			dataSource.setDriverClassName(driveClassName);
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(pwd);
			conn = dataSource.getConnection();
			String sql = "SELECT CONF_KEY,CONF_VALUE FROM SYS_CONFIG WHERE APP_TYPE=?";
			//load default config
			Map<String,String> defaultConfig = runner.query(conn, sql, new ParseDynamicConfigHandler(), "default");
			dynamicConfigMap.putAll(defaultConfig);
			//load current app.type config
			String appType = getValue("app.type");
			if(StringUtils.isNotEmpty(appType)){
				Map<String,String> appConfig = runner.query(conn, sql, new ParseDynamicConfigHandler(), appType);
				dynamicConfigMap.putAll(appConfig);
			}else{
				log.warn("系统未配置app.type类型，已忽略。。。");
			}
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			log.warn("加载动态配置信息表SYS_CONFIG错误，已忽略。。。");
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 仅用于指定加载某一些key的属性值
	 * keySet.size() 不能超过1000
	 * @param keySet
	 */
	public void loadDynamicConfig(Set<String> keySet){
		if(keySet==null||keySet.size()==0){
			return ;
		}
		Connection conn = null;
		try{
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT CONF_KEY,CONF_VALUE FROM SYS_CONFIG WHERE CONF_KEY IN (?) APP_TYPE=?";
			//...
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			log.warn("加载动态配置信息表SYS_CONFIG错误，已忽略。。。");
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public void reloadParaConfig(Set<String> keySet ){
		log.info("加载系统参数");
		for (String key : keySet) {
			log.info("key====="+key);
		}
		loadDynamicConfig(keySet);
		setChanged();
		notifyObservers(keySet);
	}
	public String put(String key,String value){
		return dynamicConfigMap.put(key, value);
	}
	public void putAll(Map<String,String> map){
		dynamicConfigMap.putAll(map);
	}
	
	public String getValue(String key){
		return dynamicConfigMap.get(key);
	}
	public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return defaultValue;
        else
            return value;
    }
    public Integer getIntValue(String key) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return null;
        else
            return Integer.valueOf(value);
    }

    public Integer getIntValue(String key, int defaultValue) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return defaultValue;
        else
            return Integer.valueOf(value);
    }

    public Boolean getBooleanValue(String key) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return false;
        else
            return value.toUpperCase().equals("TRUE");
    }
    class ParseDynamicConfigHandler implements ResultSetHandler<Map<String,String>>{

		/* (non-Javadoc)
		 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
		 */
		@Override
		public Map<String, String> handle(ResultSet rs) throws SQLException {
			Map<String,String> map = new HashMap<String,String>();
			while(rs.next()){
				map.put(rs.getString("CONF_KEY"), rs.getString("CONF_VALUE"));
			}
			return map;
		}
    	
    }
    
    
    public static void main(String[] args) {
    	Map<String,String> dynamicConfigMap = new ConcurrentHashMap<String,String>();
    	dynamicConfigMap.put("kk", "");
	}
}
