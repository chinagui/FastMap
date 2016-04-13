package com.navinfo.navicommons.database.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-4-26
 */
public class StoredProcedureImpl implements StoredProcedure
{
    //~ Instance fields ////////////////////////////////////////////////////////
    /**
     * m_Parameters属性描述
     *
     *@since   empty
     */
    private Map parameters;

    //从1开始记数，存储过程中的参数
    /**
     * m_outObjects属性描述
     *
     *@since   empty
     */
    private Map outObjects;

    /**
     * m_procName属性描述
     *
     *@since   empty
     */
    private String procName;


    /**
     * m_withReturn属性描述
     *
     *@since   empty
     */
    private boolean withReturn;

    /**
     * m_pageLines属性描述初始值
     *
     *@since   empty
     */
    private int pageLines = 100;

    //~ Static fields/initializers /////////////////////////////////////////////
    /**
     * Log实例
     *
     *@since   empty
     */
    private static Logger logger = Logger.getLogger(StoredProcedure.class);

    private Connection con;

    //~ Constructors ///////////////////////////////////////////////////////////
    /**
     * StoredProcedure对象的构造器
     *
     *@param name        参数描述
     *@param withReturn  参数描述
     */
    public StoredProcedureImpl(Connection con,String name, boolean withReturn)
    {
        parameters = null;
        outObjects = null;
        procName = name;
        this.withReturn = withReturn;
        this.con = con;
    }


    //~ Methods ////////////////////////////////////////////////////////////////
    /**
     * 设置StoredProcedure object的 M_pageLines属性
     *
     *@param pageLines    新PageLines属性值
     */
    public void setPageLines(int pageLines)
    {
        this.pageLines = pageLines;
    }

    /**
     * 获得StoredProcedure object的M_pageLines属性
     *
     *@return   The M_pageLines value
     */
    public int getPageLines()
    {
        return pageLines;
    }

    /**
     * 获得StoredProcedure object的Object属性
     *
     *@param i  参数描述
     *@return   The Object value
     */
    public Object getObject(int i)
    {
        if (outObjects == null)
        {
            return null;
        }
        else
        {
            return outObjects.get(new Integer(i));
        }
    }

    /**
     * 获得StoredProcedure object的Procedure属性
     *
     *@return   The Procedure value
     */
    public String getProcedure()
    {
        return procName;
    }

    //得到StoreProcedure的调用命令
    /**
     * 获得StoredProcedure object的ProcedureStatement属性
     *
     *@return   The ProcedureStatement value
     */
    public String getProcedureStatement()
    {
        String strCall = "{";

        if (withReturn)
        {
            strCall += "? = ";
        }
        strCall += "call ";
        strCall += procName;

        String strParam = "";

        for (int i = 0; (parameters != null) && (i < parameters.size());
                     i++)
        {
            if (strParam.length() > 0)
            {
                strParam += ',';
            }
            strParam += '?';
        }
        if (strParam.length() > 0)
        {
            strCall = strCall + '(' + strParam + ')';
        }
        strCall += "}";
        logger.info("storeprocedure statement:  " + strCall);
        return strCall;
    }

    /**
     * 新增 InParameter属性到对象 StoredProcedure object
     *
     *@param parameterIndex  新增属性 InParameter的值
     *@param sqlType         新增属性 InParameter的值
     *@param obj             新增属性 InParameter的值
     */
    public void addInParameter(int parameterIndex, int sqlType, Object obj)
    {
        addInParameter(parameterIndex, sqlType, 0, obj);
    }

    /**
     * 新增 InParameter属性到对象 StoredProcedure object
     *
     *@param parameterIndex  新增属性 InParameter的值
     *@param sqlType         新增属性 InParameter的值
     *@param scale           新增属性 InParameter的值
     *@param obj             新增属性 InParameter的值
     */
    public void addInParameter(int parameterIndex, int sqlType, int scale, Object obj)
    {
        if (parameters == null)
        {
            parameters = new HashMap();
        }

        Parameter p = new Parameter(parameterIndex, sqlType, scale, 0);

        p.obj = obj;
        parameters.put(new Integer(parameterIndex), p);
    }

    /**
     * 不带分页的执行存储过程
     *
     *@return                        结果集
     *@exception SQLException        异常处理
     *@throws java.sql.SQLException
     */
    public List callProcedure()
        throws SQLException
    {
        return execute(getProcedureStatement(), parameters, 0);
    }

    /**
     * 带分页的执行存储过程
     *
     *@param pageNum                 页数
     *@return                        记录集
     *@exception SQLException        异常处理
     *@throws java.sql.SQLException
     */
    public List callProcedure(int pageNum)
        throws SQLException
    {
        return execute(getProcedureStatement(), parameters, pageNum);
    }

    /** 方法描述 */
    public void clear()
    {
        parameters = null;
        outObjects = null;
        procName = null;
        withReturn = false;
    }

    /**
     * 方法描述 注册外部输入存储过程的参数
     *
     *@param parameterIndex  参数描述
     *@param sqlType         参数描述
     */
    public void registerOutParameter(int parameterIndex, int sqlType)
    {
        registerOutParameter(parameterIndex, sqlType, 0);
    }

    /**
     * 方法描述 注册外部输入存储过程的参数及范围
     *
     *@param parameterIndex  参数描述
     *@param sqlType         参数描述
     *@param scale           参数描述
     */
    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
    {
        if (parameters == null)
        {
            parameters = new HashMap();
        }
        parameters.put(new Integer(parameterIndex), new Parameter(parameterIndex, sqlType, scale, 1));
    }

    /**
     * 设置StoredProcedure object的 Params属性
     *
     *@param params             新Params属性值
     *@param callablestatement  新Params属性值
     *@exception SQLException   异常处理
     */
    private static void setParams(Map params, CallableStatement callablestatement)
        throws SQLException
    {
        if (params != null)
        {
            Set set = params.keySet();
            Integer integer;

            for (Iterator iterator = set.iterator(); iterator.hasNext(); )
            {
                integer = (Integer) iterator.next();

                Parameter p = (Parameter) params.get(integer);

                if (p.parameterType == 0)
                {
                    if (p != null)
                    {
                        callablestatement.setObject(integer.intValue(), p.obj, p.sqlType);
                    }
                    else
                    {
                        callablestatement.setNull(integer.intValue(), p.sqlType);
                    }
                }
                if (p.parameterType == 1)
                {
                    if ((p.sqlType == Types.NUMERIC) || (p.sqlType == Types.DECIMAL) || (p.scale > 0))
                    {
                        callablestatement.registerOutParameter(p.parameterIndex, p.sqlType, p.scale);
                    }
                    else
                    {
                        callablestatement.registerOutParameter(p.parameterIndex, p.sqlType);
                    }
                }
            }
        }
    }

    /**
     * 获得StoredProcedure object的Objects属性
     *
     *@param callablestatement  参数描述
     *@return                   The Objects value
     *@exception SQLException   异常处理
     */
    private Map getObjects(CallableStatement callablestatement)
        throws SQLException
    {
        if (parameters == null)
        {
            return null;
        }

        Map map = parameters;

        outObjects = new HashMap();

        Set set = map.keySet();
        Integer integer;
        Parameter parameter;

        for (Iterator iterator = set.iterator(); iterator.hasNext(); )
        {
            integer = (Integer) iterator.next();
            parameter = (Parameter) map.get(integer);
            if ((parameter != null) && (parameter.parameterType == 1))
            {
                outObjects.put(integer, callablestatement.getObject(parameter.parameterIndex));
            }
        }
        return outObjects;
    }

    /**
     * 核心代码
     *
     *@param procedureStatement      存储过程的名程
     *@param params                  参数
     *@param maxRows                 表示最大记录数
     *@return                        结果集
     *@exception SQLException        异常处理
     *@throws java.sql.SQLException
     */
    private List execute(String procedureStatement, Map params, int maxRows)
        throws SQLException
    {
        if ((procedureStatement == null) || (procedureStatement.length() <= 0))
        {
            return null;
        }

        CallableStatement callablestatement = null;
        List list = new ArrayList();

        try
        {
            callablestatement = con.prepareCall(procedureStatement);
            callablestatement.setMaxRows(maxRows);
            // set storeprocedure's inparam and outparam
            setParams(params, callablestatement);
            for (boolean flag = callablestatement.execute(); flag;
                                flag = callablestatement.getMoreResults())
            {
                ResultSet resultset = callablestatement.getResultSet();

                list = resultSetExtractor(resultset, 0);
                resultset.close();
            }
            getObjects(callablestatement);
            return list;
        }
        catch (Exception e)
        {
            throw new SQLException(e.getMessage());
        }
        finally
        {
            if (callablestatement != null)
            {
                callablestatement.close();
            }
        }
    }

    public  List resultSetExtractor(ResultSet resultset, int from)
    {
        List result = new ArrayList();
        try
        {
            int n = resultset.getMetaData().getColumnCount();
            //忽略from以前的数据
            for (int k = 0; (from > 0) && (k < from) && resultset.next(); k++)
            {
                ;
            }
            while (resultset.next())
            {
                Object[] arow = new Object[n];
                for (int j = 0; j < n; j++)
                {
                    arow[j] = resultset.getObject(j + 1);
                }
                result.add(arow);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    private class Parameter
    {
        /**
         * obj属性描述
         *
         *@since   empty
         */
        public Object obj;

        /**
         * parameterIndex属性描述
         *
         *@since   empty
         */
        public int parameterIndex;

        /**
         * parameterType属性描述
         *
         *@since   empty
         */
        public int parameterType;

        //0为传入数，1为传出参数，2为both
        /**
         * scale属性描述
         *
         *@since   empty
         */
        public int scale;

        /**
         * sqlType属性描述
         *
         *@since   empty
         */
        public int sqlType;

        /**
         * Parameter对象的构造器
         *
         *@param i  参数描述
         *@param j  参数描述
         *@param k  参数描述
         *@param l  参数描述
         */
        public Parameter(int i, int j, int k, int l)
        {
            parameterIndex = i;
            sqlType = j;
            scale = k;
            parameterType = l;
        }
    }
}
