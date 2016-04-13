package com.navinfo.navicommons.database.sql;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-04-20
 */
public class ResultSetReader
{

    private static final transient Logger log = Logger.getLogger(ResultSetReader.class);

    public static<E> E read( Class<E> clazz, ResultSet rs) throws SQLException
    {
        Map<String, String> cloumnMap = getColumnMetaData(rs);
        return read(clazz,rs,cloumnMap);
    }

    public static<E> E read( Class<E> clazz, ResultSet rs,Map<String, String> cloumnMap) throws SQLException
	{
        if (!rs.next())
            return null;
		E e = getClazzInstance(clazz);
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(e);
        for ( PropertyDescriptor desc : propertyDescriptors )
        {
            String name = desc.getName();
            String col = StringUtil.propertyToDB(name);
            if (propertyMatchDBColumn(cloumnMap, name, col))
            {
                fillValue(rs,e,cloumnMap,name,col);
            }
        }
		return e;
	}

    public static String[] read(ResultSet rs,int count) throws SQLException
    {
        if(!rs.next())
            return null;
        String[] rsArray = new String[count];
        for (int i = 0; i < count; i++)
        {
            rsArray[i] = rs.getString(i + 1);
        }
        return rsArray;
    }

    public static List<String[]> readList(ResultSet rs) throws SQLException
    {
        List<String[]> rsList = new ArrayList<String[]>();
        int count = rs.getMetaData().getColumnCount();
        String[] s = read(rs,count);
        while (s != null)
        {
            rsList.add(s);
            s = read(rs,count);
        }
        return rsList;
    }

    private static boolean propertyMatchDBColumn(Map<String, String> cloumnMap, String name, String col)
    {
        return !"class".equals(name) && cloumnMap.containsKey(col);
    }

    private static <E> E getClazzInstance(Class<E> clazz)
    {
        E e = null;
        try
        {
            e = clazz.newInstance();
        } catch (Exception e1)
        {
            log.error(e1);
        }
        return e;
    }

    private static <E> void fillValue(ResultSet rs, E e, Map<String, String> cloumnMap, String name, String col) throws SQLException
    {
        try
        {
            Object colValue = getValueFromResult(rs, cloumnMap, col);
            if(colValue != null)
            {
                BeanUtils.setProperty( e, name, colValue );
            }
        }
        catch ( InvocationTargetException e1)
        {
            log.error("desc.getName() = " + name ,e1);
        } catch (IllegalAccessException e1)
        {
            log.error("desc.getName() = " + name ,e1);
        }
    }

    private static  Object getValueFromResult(ResultSet rs, Map<String, String> cloumnMap, String col) throws SQLException
    {
        Object colValue = null;
        if("TIMESTAMP".equals(cloumnMap.get(col)))
            colValue = rs.getTimestamp(col);
        else if("CLOB".equals(cloumnMap.get(col)))
            colValue = clob2Str(rs.getClob(col));
        else
            colValue = rs.getObject(col);
        return colValue;
    }

    private static Map<String, String> getColumnMetaData(ResultSet rs) throws SQLException
    {
        ResultSetMetaData metaData = rs.getMetaData();
        Map<String,String> cloumnMap = new HashMap<String, String>(metaData.getColumnCount());
        for(int i = 1;i <= metaData.getColumnCount();i++)
        {
            cloumnMap.put(metaData.getColumnName(i),metaData.getColumnTypeName(i));
        }
        return cloumnMap;
    }

    public static<E> List<E> readList( Class<E> clazz, ResultSet rs )
		throws SQLException
	{
		Map<String, String> cloumnMap = getColumnMetaData(rs);
        ArrayList<E> result = new ArrayList<E>();
		E e = read(clazz, rs,cloumnMap);
		while ( e != null )
		{
			result.add( e );
			e = read( clazz,rs,cloumnMap);
		}
		return result;
	}

    public static String clob2Str(Clob clob) throws SQLException
    {
        try
        {
            StringBuilder content = new StringBuilder();
            if (clob != null)
            {
                Reader is = clob.getCharacterStream();
                BufferedReader br = new BufferedReader(is);
                String s = null;
                s = br.readLine();
                while (s != null)
                {
                    content.append(s).append("\n");
                    s = br.readLine();
                }
                return content.toString();
            }

        } catch (IOException e)
        {
            log.error(e);
        }
        return null;

    }

}
