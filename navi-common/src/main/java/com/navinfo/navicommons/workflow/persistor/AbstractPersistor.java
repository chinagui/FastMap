package com.navinfo.navicommons.workflow.persistor;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.navinfo.navicommons.workflow.def.Def;
import com.navinfo.navicommons.workflow.def.DefineAble;
import com.navinfo.navicommons.workflow.instance.FlowInstance;
import com.navinfo.navicommons.workflow.utils.XMLUtils;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-24
 */
public abstract class AbstractPersistor implements Persistor
{
    private static final transient Logger log = Logger.getLogger(AbstractPersistor.class);

    protected FlowInstance buildFlowInstance(String xmlStr)
    {
        FlowInstance flowInstance = new FlowInstance();
        return buildDefineAble(xmlStr, flowInstance);
    }

    protected Def buildDef(String xmlStr)
    {
        Def def = new Def();
        return buildDefineAble(xmlStr, def);
    }

    private <T extends DefineAble> T  buildDefineAble(String xmlStr, T defineAble)
    {
        try
        {
            Element root = XMLUtils.parseRoot(xmlStr);
            defineAble.build(root);
        } catch (DocumentException e)
        {
            log.error("解析xml时出错",e);
            throw new PersistorException("解析xml时出错",e);
        }
        return defineAble;
    }

    protected String serialazeDefineAble(DefineAble defineAble)
    {
        Element root = defineAble.serialaze();
        return XMLUtils.element2str(root);
    }

    public FlowInstance loadFlowInstance(String instancePk) throws PersistorException
    {
        String xmlStr = doLoadFlowInstance(instancePk);
        return buildFlowInstance(xmlStr);
    }

    public void saveFlowInstance(FlowInstance instance)
    {
        String xmlStr = serialazeDefineAble(instance);
        doSaveFlowInstance(instance.getId(),xmlStr);
    }

    public Def loadDefine(String defPk) throws PersistorException
    {
        String xmlStr = doLoadDefine(defPk);
        return buildDef(xmlStr);
    }

    public void saveDefine(Def def)
    {
        String xmlStr = serialazeDefineAble(def);
        doSaveDefine(def.getId(),xmlStr);
    }

    protected abstract String doLoadFlowInstance(String instancePk) throws PersistorException;

    protected abstract void doSaveFlowInstance(String instancePk,String xml) throws PersistorException;

    protected abstract String doLoadDefine(String defPk) throws PersistorException;

    protected abstract void doSaveDefine(String defPk,String xml) throws PersistorException;
}
