package com.navinfo.navicommons.database.sql;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;


/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-04-20
 */
public class SQLQuery
{
    private static final transient Logger log = Logger.getLogger(SQLQuery.class);

    private Connection con;

    public SQLQuery(Connection con)
    {
        this.con = con;
    }

    public  <E> List<E> query(Class<E> clazz, String query, Object... params) throws PersistenceException
    {
        List<E> result = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try
        {
            pst = con.prepareStatement(query);
            setParameters(query, pst, params);
            rs = pst.executeQuery();
            result = ResultSetReader.readList(clazz, rs);
        } catch (SQLException e)
        {
            processException(e);
        }
        finally
        {
            close(rs);
            close(pst);
        }
        return result;
    }

    private void processException(Throwable e)
    {
        log.error(e.getMessage(),e);
        throw new PersistenceException(e);
    }

    private  void setParameters(String query, PreparedStatement pst, Object... params) throws SQLException
    {
        int cnt = StringUtil.getCount(query, '?');
        for (int i = 0; i < cnt; i++)
        {
            pst.setObject(i + 1, params[i]);
        }
    }

    public <E> E get(Class<E> clazz, String query, Object... params) throws PersistenceException
    {
        E result = null;
        List<E> es = query(clazz,query,params);
        if(es != null && es.size() == 1) //todo 不为1时报错
            result = es.get(0);
        return result;
    }

    public  void execute(String query, Object... params) throws PersistenceException
    {
        PreparedStatement pst = null;
        try
        {
            pst = con.prepareStatement(query);
            setParameters(query, pst, params);
            pst.executeUpdate();
        } catch (SQLException e)
        {
            processException(e);
        }
        finally
        {
            close(pst);
        }
    }

    public  void saveList(List items) throws PersistenceException
    {
        if (items.size() == 0)
        {
            return;
        }
        PreparedStatement pst = null;
        try
        {
            Object e = items.get(0);
            String sql = createInsertSql(e);
            pst = con.prepareStatement(sql);
            for (Object item : items)
            {
                addBatch(pst, item);
            }
            pst.executeBatch();
            pst.clearBatch();
        } catch (Exception e1)
        {
            processException(e1);
        }
        finally
        {
            close(pst);
        }
    }

    private void addBatch(PreparedStatement pst, Object item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException
    {
        Map map = PropertyUtils.describe(item);
        setValueFromMap(pst, map);
        pst.addBatch();
    }

    private int setValueFromMap(PreparedStatement pst, Map map) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException
    {
        int i = 1;
        for (Object name : map.keySet())
        {
            if (!"class".equals(name))
                setValue(pst, map, i++, (String)name);
        }
        return i;
    }

    private void setValue(PreparedStatement pst, Map tmap, int i, String name) throws SQLException
    {
        if (!"class".equals(name))
        {
            if (tmap.get(name) == null)
            {
                pst.setString(i, null);
            }
            else
            {
                pst.setObject(i, tmap.get(name));
            }
        }
    }

    private  String createInsertSql(Object e) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Map map = PropertyUtils.describe(e);
        StringBuilder builder = new StringBuilder();
        String tname = StringUtil.getTableName(e.getClass().getName());
        builder.append("INSERT INTO ").append(tname).append(" (");
        int cnt = 0;
        for (Object n : map.keySet())
        {
            String name = (String) n;
            if (!"class".equals(name)) {
                if (cnt != 0) {
                    builder.append(',');
                }
                builder.append(StringUtil.propertyToDB(name));
                cnt++;
            }
        }
        builder.append(") VALUES (");
        for (int i = 0; i < cnt; i++) {
            if (i != 0) {
                builder.append(',');
            }
            builder.append('?');
        }
        builder.append(")");
        return builder.toString();
    }

    private String createUpdateSql(Object o, String key) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Map map = PropertyUtils.describe(o);
        StringBuilder builder = new StringBuilder();
        String tname = StringUtil.getTableName(o.getClass().getName());
        builder.append("UPDATE ").append(tname).append(" SET ");
        int cnt = 0;
        for (Object n : map.keySet())
        {
            String name = (String) n;
            if (!"class".equals(name))
            {
                if (cnt != 0) {
                    builder.append(',');
                }
                builder.append(StringUtil.propertyToDB(name)).append(" = ?");
                cnt++;
            }
        }
        builder.append(" WHERE ").append(StringUtil.propertyToDB(key)).append("=?");
        return builder.toString();
    }

    private  String createDeleteSql(Class clazz,String key) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        StringBuilder builder = new StringBuilder();
        String tname = StringUtil.getTableName(clazz.getName());
        builder.append("DELETE FROM ").append(tname).
        append(" WHERE ").append(StringUtil.propertyToDB(key)).append("=?");
        return builder.toString();
    }

    public  boolean update(Object o, String key) throws PersistenceException
    {
        boolean result = false;
        PreparedStatement pst = null;
        try
        {
            Map map = PropertyUtils.describe(o);
            String updateSql = createUpdateSql(o, key);
            pst = con.prepareStatement(updateSql);
            int i = setValueFromMap(pst,map);
            pst.setObject(i, map.get(key));
            result = pst.executeUpdate() > 0;
        } catch (Exception e)
        {
            processException(e);
        }
        finally
        {
            close(pst);
        }
        return result;
    }

    public  int save(Object e) throws PersistenceException
    {
        int ret = 0;
        PreparedStatement pst = null;
        try
        {
            Map map = PropertyUtils.describe(e);
            pst = con.prepareStatement(createInsertSql(e));
            setValueFromMap(pst,map);
            ret = pst.executeUpdate();
        } catch (Exception e1)
        {
            processException(e1);
        }
        finally
        {
            close(pst);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public  List<String[]> query(String query, Object... params) throws PersistenceException
    {
        List<String[]> result = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try
        {
            pst = con.prepareStatement(query);
            setParameters(query, pst, params);
            rs = pst.executeQuery();
            result = ResultSetReader.readList(rs);
        } catch (SQLException e)
        {
            processException(e);
        }
        finally
        {
            close(rs);
            close(pst);
        }
        return result;
    }

    public  <E> List<E> getAll(Class<E> c)
    {
        String query = "select * from " + StringUtil.getTableName(c.getName());
        return query(c, query);
    }

    public  void saveOrUpdate(Object e, String key) throws PersistenceException
    {
        if (!update(e, key))
        {
            save(e);
        }
    }

    public  void updateList(List updates, String key) throws PersistenceException
    {
        if (updates == null || updates.size() == 0)
            return;
        PreparedStatement pst = null;
        try
        {
            Object o = updates.get(0);
            String sql = createUpdateSql(o, key);
            pst = con.prepareStatement(sql);
            for (Object obj : updates)
            {
                Map map = PropertyUtils.describe(obj);
                int i = setValueFromMap(pst,map);
                pst.setObject(i, map.get(key));
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
        } catch (Exception e)
        {
            processException(e);
        }
        finally
        {
            close(pst);
        }
    }

    public  List<Map<String, String>> queryMap(String query, Object... params) throws PersistenceException
    {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try
        {
            pst = con.prepareStatement(query);
            setParameters(query, pst, params);
            rs = pst.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int c = meta.getColumnCount();
            while (rs.next())
            {
                Map<String, String> row = new HashMap<String, String>();
                for (int i = 0; i < c; i++)
                {
                    row.put(meta.getColumnName(i + 1).toUpperCase(), rs.getString(i + 1));
                }
                result.add(row);
            }
        } catch (SQLException e)
        {
            processException(e);
        }
        finally
        {
            close(rs);
            close(pst);
        }
        return result;
    }


    public  void deletePKList(Class clazz,List Pks, String key) throws PersistenceException
    {
        if (Pks.size() == 0)
        {
            return;
        }
        try
        {
            PreparedStatement pst = con.prepareStatement(createDeleteSql(clazz, key));
            for (Object obj : Pks)
            {
                pst.setObject(1, obj);
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
        } catch (Exception e)
        {
            processException(e);
        }

    }

    public  void executeBatch(String sql,List<List> pars) throws PersistenceException
    {
        if (pars.size() == 0) {
            return;
        }
        PreparedStatement pst = null;
        try
        {
            pst = con.prepareStatement(sql);
            int i;
            for (List parGroup : pars)
            {
                i = 1;
                for(Object par : parGroup)
                {
                    pst.setObject(i++, par);
                }
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
        } catch (SQLException e)
        {
            processException(e);
        }
        finally
        {
            close(pst);
        }
    }

    private static void close(ResultSet rs)
    {
        if(rs != null)
            try
            {
                rs.close();
            } catch (SQLException e)
            {
                log.error(e);
            }
    }
    private static void close(Connection con)
    {
        if(con != null)
            try
            {
                con.close();
            } catch (SQLException e)
            {
                log.error(e);
            }
    }
    private static void close(Statement st)
    {
        if(st != null)
            try
            {
                st.close();
            } catch (SQLException e)
            {
                log.error(e);
            }
    }
}
