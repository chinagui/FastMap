package com.navinfo.navicommons.config;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-4-24
 * Time: 下午4:58
 * To change this template use File | Settings | File Templates.
 */
public interface Config {

    public String getValue(String key) ;

    public String getValue(String key, String defaultValue) ;

    public Integer getIntValue(String key) ;

    public Integer getIntValue(String key,int defaultValue);
    
    public Boolean getBooleanValue(String key);

    public Object get(Object key);
    
    public void put(String key, String value);
}
