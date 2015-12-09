package com.navinfo.navicommons.database.sql;

import java.sql.SQLException;
import java.util.List;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-4-26
 */
public interface StoredProcedure
{
    //~ Methods ////////////////////////////////////////////////////////////////
    /**
     * 设置StoredProcedure object的 M_pageLines属性
     *
     *@param pageLines    新PageLines属性值
     */
    void setPageLines(int pageLines);

    /**
     * 获得StoredProcedure object的M_pageLines属性
     *
     *@return   The M_pageLines value
     */
    int getPageLines();

    /**
     * 获得StoredProcedure object的Object属性
     *
     *@param i  参数描述
     *@return   The Object value
     */
    Object getObject(int i);

    /**
     * 获得StoredProcedure object的Procedure属性
     *
     *@return   The Procedure value
     */
    String getProcedure();

    //得到StoreProcedure的调用命令
    /**
     * 获得StoredProcedure object的ProcedureStatement属性
     *
     *@return   The ProcedureStatement value
     */
    String getProcedureStatement();

    /**
     * 新增 InParameter属性到对象 StoredProcedure object
     *
     *@param parameterIndex  新增属性 InParameter的值
     *@param sqlType         新增属性 InParameter的值
     *@param obj             新增属性 InParameter的值
     */
    void addInParameter(int parameterIndex, int sqlType, Object obj);

    /**
     * 新增 InParameter属性到对象 StoredProcedure object
     *
     *@param parameterIndex  新增属性 InParameter的值
     *@param sqlType         新增属性 InParameter的值
     *@param scale           新增属性 InParameter的值
     *@param obj             新增属性 InParameter的值
     */
    void addInParameter(int parameterIndex, int sqlType, int scale, Object obj);

    /**
     * 不带分页的执行存储过程
     *
     *@return                        结果集
     *@exception java.sql.SQLException        异常处理
     *@throws java.sql.SQLException
     */
    List callProcedure()
        throws SQLException;

    /**
     * 带分页的执行存储过程
     *
     *@param pageNum                 页数
     *@return                        记录集
     *@exception SQLException        异常处理
     *@throws java.sql.SQLException
     */
    List callProcedure(int pageNum)
        throws SQLException;

    /** 方法描述 */
    void clear();

    /**
     * 方法描述 注册外部输入存储过程的参数
     *
     *@param parameterIndex  参数描述
     *@param sqlType         参数描述
     */
    void registerOutParameter(int parameterIndex, int sqlType);

    /**
     * 方法描述 注册外部输入存储过程的参数及范围
     *
     *@param parameterIndex  参数描述
     *@param sqlType         参数描述
     *@param scale           参数描述
     */
    void registerOutParameter(int parameterIndex, int sqlType, int scale);
}
