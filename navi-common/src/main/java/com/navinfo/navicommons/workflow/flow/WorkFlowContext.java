package com.navinfo.navicommons.workflow.flow;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.navinfo.navicommons.workflow.def.DefineAble;
import com.navinfo.navicommons.workflow.persistor.FilePersistor;
import com.navinfo.navicommons.workflow.persistor.Persistor;
import com.navinfo.navicommons.workflow.utils.SerializUtils;
import com.navinfo.navicommons.workflow.utils.StringConverter;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-6
 */
public class WorkFlowContext implements Serializable, DefineAble
{
    private static final transient Logger log = Logger.getLogger(WorkFlowContext.class);
    //运行中的参数,会持久化
    private Map<String,Serializable> valueStack = new HashMap<String, Serializable>();

    //初始化的资源，不会持久化
    private transient Map<String,Object> resourcePool = new HashMap<String, Object>();

    //负责元数据及实例数据的装载、持久化
    private transient Persistor persistor;


    public Map<String, Serializable> getValueStack() {
        return valueStack;
    }

    public void setValueStack(Map<String, Serializable> valueStack) {
        this.valueStack = valueStack;
    }

    public Serializable getValueFromStack(String key)
    {
        return  valueStack.get(key);
    }

    public void putValueToStack(String key, Serializable value)
    {
        valueStack.put(key,value);
    }

    public void build(Element e)
    {
        WorkFlowContext context = bulidObject(e.getText());
        this.setValueStack(context.getValueStack());
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("context");
        e.addText(serialazeObject());
        return e;
    }

    private WorkFlowContext bulidObject(String hex)
    {
        WorkFlowContext o = new WorkFlowContext();
        try
        {
            o = (WorkFlowContext)SerializUtils.deserialize(StringConverter.hexToByte(hex));
        } catch (Exception e)
        {
            log.error(e);
        }
        return o;
    }

    private String serialazeObject()
    {
        String s = "";
        try
        {
            long t = System.currentTimeMillis();
            s = StringConverter.byteToHex(SerializUtils.serialize(this));
            //log.debug("系列化用时 " + (System.currentTimeMillis() - t));
        } catch (IOException e)
        {
            log.error(e);
        }
        return s;
    }

    public Persistor getPersistor() {
        return persistor;
    }

    public void setPersistor(Persistor persistor) {
        this.persistor = persistor;
    }

    public Map<String, Object> getResourcePool() {
        return resourcePool;
    }

    public void setResourcePool(Map<String, Object> resourcePool) {
        this.resourcePool = resourcePool;
    }

    public Object getResourceFromPool(String key)
    {
        return resourcePool.get(key);
    }

    public void putResourceToPool(String key,Object resource)
    {
        resourcePool.put(key,resource);
    }

    @Override
    public WorkFlowContext clone()
    {
        long time = System.currentTimeMillis();
        WorkFlowContext context = null;
        try {
            context = (WorkFlowContext)SerializUtils.deepClone(this);
        } catch (Exception e)
        {
            log.warn("contex中有无法系列化的元素",e);
            return this;
        }
        //log.debug("clone 用时" + (System.currentTimeMillis() - time));
        return context;
    }

    public static WorkFlowContext getDefaultContext()
    {
        WorkFlowContext workFlowContext = new WorkFlowContext();
        workFlowContext.setPersistor(new FilePersistor());
        return workFlowContext;
    }
}
