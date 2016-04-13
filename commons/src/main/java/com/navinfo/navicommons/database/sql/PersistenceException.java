package com.navinfo.navicommons.database.sql;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;


public class PersistenceException extends RuntimeException
{
    //~ Instance fields ////////////////////////////////////////////////////////
    /**
     * 属性描述 异常根节点
     */
    protected Throwable rootCause = null;

    /**
     * 属性描述 异常链
     */
    private Collection exceptions = new ArrayList();

    /**
     * 属性描述 异常消息
     */
    private String message = null;

    /**
     * 属性描述 消息Key
     */
    private String messageKey = null;

    /**
     * 属性描述 消息命令
     */
    private Object[] messageArgs = null;

    //~ Constructors ///////////////////////////////////////////////////////////
    /** PersistenceException对象的构造器 */
    public PersistenceException()
    {
    }

    /**
     * PersistenceException对象的构造器
     *
     *@param rootCause  参数描述
     */
    public PersistenceException(Throwable rootCause)
    {
        this.rootCause = rootCause;
    }

    /**
     * PersistenceException对象的构造器
     *
     *@param message  参数描述
     */
    public PersistenceException(String message)
    {
        super(message);
        this.message = message;
    }

    /**
     * PersistenceException对象的构造器
     *
     * @param message    参数描述
     * @param rootCause  参数描述
     */
    public PersistenceException(String message, Throwable rootCause)
    {
        super(message);
        this.rootCause = rootCause;
        this.message = message;
    }

    /**
     * 设置PersistenceException object的 MessageArgs属性
     *
     * @param args  新MessageArgs属性值
     */
    public void setMessageArgs(Object[] args)
    {
        this.messageArgs = args;
    }

    /**
     * 设置PersistenceException object的 MessageKey属性
     *
     * @param key  新MessageKey属性值
     */
    public void setMessageKey(String key)
    {
        this.messageKey = key;
    }

    /**
     * 设置PersistenceException object的 RootCause属性
     *
     * @param anException  新RootCause属性值
     */
    public void setRootCause(Throwable anException)
    {
        rootCause = anException;
    }

    //~ Methods ////////////////////////////////////////////////////////////////
    /**
     * 获得PersistenceException object的Collection属性
     *
     * @return   The Collection value
     */
    public Collection getCollection()
    {
        return exceptions;
    }

    /**
     * 获得PersistenceException object的MessageArgs属性
     *
     * @return   The MessageArgs value
     */
    public Object[] getMessageArgs()
    {
        return messageArgs;
    }

    /**
     * 获得PersistenceException object的MessageKey属性
     *
     * @return   The MessageKey value
     */
    public String getMessageKey()
    {
        return messageKey;
    }

    /**
     * 获得PersistenceException object的RootCause属性
     *
     * @return   The RootCause value
     */
    public Throwable getRootCause()
    {
        return rootCause;
    }

    /**
     * 新增 Exception属性到对象 PersistenceException object
     *
     * @param ex  新增属性 Exception的值
     */
    public void addException(PersistenceException ex)
    {
        exceptions.add(ex);
    }

    /** 方法描述 */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    /**
     * 方法描述
     *
     *@param outStream  参数描述
     */
    public void printStackTrace(PrintStream outStream)
    {
        printStackTrace(new PrintWriter(outStream));
    }

    /**
     * 方法描述
     *
     *@param writer  参数描述
     */
    public void printStackTrace(PrintWriter writer)
    {
        super.printStackTrace(writer);
        if (getRootCause() != null)
        {
            getRootCause().printStackTrace(writer);
        }
        writer.flush();
    }
}
