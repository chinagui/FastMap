package com.navinfo.navicommons.database.sql;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-5-23
 * Time: 下午5:28
 * 存储过程自动创建
 */
public class PackageExec {
    private Logger log = Logger.getLogger(PackageExec.class);
    private Connection conn;
    private static final String encoding = "GBK";

    public PackageExec(Connection conn) {
        this.conn = conn;
    }

    /**
     * 执行某个classpath下的存储过程文件,文件内容必须以/结束
     *
     * @param classpath
     * @throws Exception
     */
    public void execute(String classpath) throws Exception {
        InputStream is = null;
        try {
            is = PackageExec.class.getResourceAsStream(classpath);
            if (is == null) {
                Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
            }
            if (is == null)
                throw new IOException("无法找到配置文件:" + classpath);
            execute(is);
        } catch (Exception e) {
            log.error("执行文件出错,文件名"+classpath+e.getMessage(), e);
            throw e;
        }
    }

    public void execute(InputStream is) throws Exception {
        long t1 = System.currentTimeMillis();
        Statement statement = null;

        try {
            Reader reader = new InputStreamReader(is, encoding);
            String line;
            StringBuilder sql = new StringBuilder();
            BufferedReader in = new BufferedReader(reader);
            statement = conn.createStatement();
            boolean exec = false;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("/") && line.length() == 1) {
                    exec = true;

                } else {
                    sql.append(line + "\n");
                }
                if (exec) {
                    String pck = sql.toString();
//                    log.debug("执行的语句为:"+pck);
                    statement.execute(pck);
                    sql.delete(0, sql.length());
                    exec = false;
                }


            }
//            log.debug("sql="+sql);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;

        } finally {
            DbUtils.closeQuietly(statement);
            if (is != null)
                is.close();
        }

        long t2 = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("sql exec time " + (t2 - t1) + "ms");
        }

    }
    

    /**
     * 执行某个classpath下的存储过程文件,文件内容必须以/结束
     * 支持传入字符集编码名称
     * @param classpath
     * @throws Exception
     */
    public void execute(String classpath,String charsetName) throws Exception {
        InputStream is = null;
        try {
            is = PackageExec.class.getResourceAsStream(classpath);
            if (is == null) {
                Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
            }
            if (is == null)
                throw new IOException("无法找到配置文件:" + classpath);
            execute(is);
        } catch (Exception e) {
            log.error("执行文件出错,文件名"+classpath+e.getMessage(), e);
            throw e;
        }
    }

    public void execute(InputStream is,String charsetName) throws Exception {
        long t1 = System.currentTimeMillis();
        Statement statement = null;

        try {
            Reader reader = new InputStreamReader(is, charsetName);
            String line;
            StringBuilder sql = new StringBuilder();
            BufferedReader in = new BufferedReader(reader);
            statement = conn.createStatement();
            boolean exec = false;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("/") && line.length() == 1) {
                    exec = true;

                } else {
                    sql.append(line + "\n");
                }
                if (exec) {
                    String pck = sql.toString();
//                    log.debug("执行的语句为:"+pck);
                    statement.execute(pck);
                    sql.delete(0, sql.length());
                    exec = false;
                }


            }
//            log.debug("sql="+sql);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;

        } finally {
            DbUtils.closeQuietly(statement);
            if (is != null)
                is.close();
        }

        long t2 = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("sql exec time " + (t2 - t1) + "ms");
        }

    }
}
