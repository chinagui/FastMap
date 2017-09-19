package com.navinfo.dataservice.engine.limit.commons.config;

import java.util.Observer;

public interface SystemConfig {

    public String getValue(String key) ;

    public String getValue(String key, String defaultValue) ;

    public Integer getIntValue(String key) ;

    public Integer getIntValue(String key, int defaultValue);
    
    public Boolean getBooleanValue(String key);
    
    public String put(String key, String value);
    public void addObserver(Observer o);
    public void deleteObserver(Observer o);
}
