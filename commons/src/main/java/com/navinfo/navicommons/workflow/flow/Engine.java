package com.navinfo.navicommons.workflow.flow;

import com.navinfo.navicommons.workflow.def.DefineException;
import com.navinfo.navicommons.workflow.spi.ProcessException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-10
 */
public interface Engine 
{
    String start(String defPk)  throws ProcessException, DefineException;

    void recover(String flowInstancePk)  throws ProcessException,IlleagStateException,DefineException;

    String getInstanceId();

    WorkFlowContext getWorkFlowContext();
}
