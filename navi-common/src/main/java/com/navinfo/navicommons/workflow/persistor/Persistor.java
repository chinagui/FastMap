package com.navinfo.navicommons.workflow.persistor;

import com.navinfo.navicommons.workflow.def.Def;
import com.navinfo.navicommons.workflow.instance.FlowInstance;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-20
 */
public interface Persistor
{
    public FlowInstance loadFlowInstance(String instancePk) throws PersistorException;

    public void saveFlowInstance(FlowInstance instance);

    public Def loadDefine(String defPk) throws PersistorException;

    public void saveDefine(Def def);
}
