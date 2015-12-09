package com.navinfo.navicommons.database.sql;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-4-26
 */

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.navicommons.database.Page;

public class PageQuery
{
    protected final static int DEFAULT_PAGE_SIZE = 20;

    private int start = 0;
    private int limit = 20;

    private Connection con;

    public PageQuery(Connection con , int start, int limit)
    {
        this.con = con;
        this.start = start;
        this.limit = limit > 0 ? limit : DEFAULT_PAGE_SIZE;
    }


    /**
     * 方法描述 执行指定页数的Page对象(带参数)
     *
     *@param sqlStatement              参数描述
     *@param params                    参数描述
     *@return                          返回值
     */
    public Page queryMap(String sqlStatement, Object... params)
    {
        Page page = new Page(start,limit);
        //得出总数
        page.setTotalCount(cacRsCount(sqlStatement,params));
        //执行分页sql
        String pageSqlstatement = pageSql(sqlStatement);
        SQLQuery sqlQuery = new SQLQuery(con);
        List result = sqlQuery.queryMap(pageSqlstatement, params);
        page.setResult(result);
        return page;
    }

    public Page query(Class clazz, String sqlStatement, Object... params)
    {
        Page page = new Page(start,limit);
        //得出总数
        page.setTotalCount(cacRsCount(sqlStatement,params));
        //执行分页sql
        String pageSqlstatement = pageSql(sqlStatement);
        SQLQuery sqlQuery = new SQLQuery(con);
        List result = sqlQuery.query(clazz,pageSqlstatement, params);
        page.setResult(result);
        return page;

    }

    public Page getAll(Class clazz)
    {
        String sqlStatement = "select * from " + StringUtil.getTableName(clazz.getName());

        Page page = new Page(start,limit);
        //得出总数
        page.setTotalCount(cacRsCount(sqlStatement));
        //执行分页sql
        String pageSqlstatement = pageSql(sqlStatement);
        SQLQuery sqlQuery = new SQLQuery(con);
        List result = sqlQuery.query(clazz,pageSqlstatement);
        page.setResult(result);
        return page;

    }

    private int cacRsCount(String sqlStatement, Object ...params)
    {
        int count = 0;
        SQLQuery sqlQuery = new SQLQuery(con);
        List list = sqlQuery.queryMap(countSql(sqlStatement), params);
        if (list.size() > 0)
        {
            Map row = (HashMap) list.get(0);
            Object o = row.get("COUNT");
            count = (o != null ? Integer.parseInt((String)o) : 0);
        }
        return count;
    }

    /**
     * 方法描述 计算指定Sql的查询记录个数
     *
     *@param s  参数描述
     *@return   返回值
     */
    private  String countSql(String s)
    {
        String temp = s.toLowerCase();
        int i = temp.indexOf("from");
        String sql = "select count(*) as COUNT " + s.substring(i, temp.length()); //TODO complex sql
        return sql;
    }

    /**
     * 方法描述 查询分页指定每页记录数记录
     *
     *@param s        参数描述
     *@return         返回值
     * just for oracle ,different database has different limit dialract
     */
    private String pageSql(String s)
    {
        if (s == null)
        {
            return null;
        }

        int nBeginRecNum = start + 1;
        int nEndBeginRecNum = start + limit;
        String sql = "select * from (select rownum as my_rownum,table_a.* from(" + s + ") table_a where rownum<=" + nEndBeginRecNum + ") where my_rownum>=" +
                     nBeginRecNum;

        return sql;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
