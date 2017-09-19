package com.navinfo.dataservice.engine.limit.commons.log;

import com.navinfo.dataservice.engine.limit.commons.config.SystemConfigFactory;
import org.apache.log4j.*;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.RootLogger;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-7-29
 * Time: 上午9:37
 * 能动态设置输出文件路径的日志工具
 * 此工具受log4j的配置文件控制，在log4j的配置文件中设置的日志级别依然对此工具类有效
 */
public class DynamicOutputLogger {
    private static final String DEFAULT_PATTERN = "[%d] %5p%6.6r[%t]%x - %C{1}.%M(%L) - %m%n";
    private static DefaultRepositorySelector repositorySelector;
    private static CustomHierarchy h;
//    private  Layout defaultLayout;
    private static boolean enableTaskStdoutLog = SystemConfigFactory.getSystemConfig().getBooleanValue("enableStdouTasktLog");

    static {
        h = new CustomHierarchy(new RootLogger((Level) Level.DEBUG));
        repositorySelector = new DefaultRepositorySelector(h);
       
    }


    /**
     * 动态数据日志到某个文件
     *
     * @param name
     * @param output
     * @return
     * @throws IOException
     */
    public static Logger getFileLogger(String name, String output) throws IOException {
        Logger log = getLogger(name);
        Layout defaultLayout = new PatternLayout(DEFAULT_PATTERN);
        Appender appender = new FileAppender(defaultLayout, output);

        log.addAppender(appender);
        if (enableTaskStdoutLog) {
            log.addAppender(new ConsoleAppender(defaultLayout));
        }
        Appender appender3 = new DailyRollingFileAppender(defaultLayout, "logs/fos.log", "'.'yyyyMMdd");
        log.addAppender(appender3);
        return log;
    }

    /**
     * 动态数据日志到某个文件。每天一个文件
     *
     * @param name
     * @param output
     * @return
     * @throws IOException
     */
    public static Logger getDailyRollingFileLogger(String name, String output) throws IOException {
        Logger log = getLogger(name);
        Layout defaultLayout = new PatternLayout(DEFAULT_PATTERN);
        Appender appender = new DailyRollingFileAppender(defaultLayout, output, "'.'yyyyMMdd");
        log.addAppender(appender);
        if (enableTaskStdoutLog) {
            log.addAppender(new ConsoleAppender(defaultLayout));
        }
        return log;
    }


    public static Logger getLogger(String name) {
        return repositorySelector.getLoggerRepository().getLogger(name);
    }

    public static void removeLogger(String name) {
        Logger log = getLogger(name);
        Enumeration enume = log.getAllAppenders();
        while (enume.hasMoreElements()) {
            Appender appender = (Appender) enume.nextElement();
            appender.close();
        }

        h.remove(name);
    }


    public static void main(String[] args) {
        try {
            Logger log = DynamicOutputLogger.getFileLogger("lq", "logs\\lq.test");
            log.debug("aa1111111111111111111111111111");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
