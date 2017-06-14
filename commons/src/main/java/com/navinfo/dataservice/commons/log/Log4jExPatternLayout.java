package com.navinfo.dataservice.commons.log;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

/** 
* @ClassName: Log4jExPatternLayout 
* @author Xiao Xiaowen 
* @date 2017年6月8日 下午9:04:40 
* @Description: TODO
*/
public class Log4jExPatternLayout extends PatternLayout {
    public Log4jExPatternLayout(String pattern){
        super(pattern);
    }

    public Log4jExPatternLayout(){
        super();
    }

    @Override
    protected PatternParser createPatternParser(String pattern) {
        return new Log4jExPatternParser(pattern);
    }
}