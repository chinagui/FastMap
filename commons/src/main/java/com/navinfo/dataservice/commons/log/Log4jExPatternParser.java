package com.navinfo.dataservice.commons.log;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/** 
* @ClassName: Log4jExPatternParser 
* @author Xiao Xiaowen 
* @date 2017年6月8日 下午9:01:48 
* @Description: TODO
*/
public class Log4jExPatternParser extends PatternParser {
    public Log4jExPatternParser(String pattern) {
        super(pattern);
    }

    @Override
    protected void finalizeConverter(char c) {
        if (c == 'T') {
            this.addConverter(new ExPatternConverter(this.formattingInfo));
        } else {
            super.finalizeConverter(c);
        }
    }

    private static class ExPatternConverter extends PatternConverter {

        public ExPatternConverter(FormattingInfo fi) {
            super(fi);
        }

        @Override
        protected String convert(LoggingEvent event) {
            return ThreadLogToken.getInstance().get();
        }

    }
}