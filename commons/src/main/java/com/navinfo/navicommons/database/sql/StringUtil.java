package com.navinfo.navicommons.database.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import oracle.sql.CLOB;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-04-20
 */
public class StringUtil
{

    public static String propertyToDB(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                result.append('_');
            }
            result.append(Character.toUpperCase(c));
        }
        return result.toString();
    }

    public static int getCount(String query, char c) {
        int cnt = 0;
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == c) {
                cnt++;
            }
        }
        return cnt;
    }

    public static String getTableName(String name) {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(i + 1, name.length());
            //name = name.substring(i + 1, name.length() - 6);
        }
        return propertyToDB(name);
    }
    
  //首字母转大写
    public static String toUpperCaseFirstOne(String s)
    {
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }
    
    /**
     * 
     * @param tableName =task_id
     * @return taskId
     */
    public static String getObjectName(String tableName) {
    	StringBuilder result = new StringBuilder();
    	String[] tableList=tableName.split("_");
    	result.append(tableList[0].toLowerCase());
        for (int i=1;i<tableList.length;i++) {
        	result.append(toUpperCaseFirstOne(tableList[i].toLowerCase()));
        }
        return result.toString();
    }

    public static String null2blank(Object o)
    {
        if(o == null)
        {
            return "";
        }
        else
        {
            return o.toString();
        }
    }
    
    
    // 将字CLOB转成STRING类型
    public static String ClobToString(CLOB clob) throws SQLException {

        String reString = "";
        if(clob == null)
        {
            return reString;
        }
        try {
            Reader is = clob.getCharacterStream();// 得到流
            BufferedReader br = new BufferedReader(is);
            String s = br.readLine();
            StringBuffer sb = new StringBuffer();
            while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
                sb.append(s);
                s = br.readLine();
            }
            reString = sb.toString();
        }
        catch (Exception e) {
           throw new SQLException(e);
        }
        return reString;
    }

}
