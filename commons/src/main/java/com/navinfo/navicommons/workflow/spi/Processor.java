package com.navinfo.navicommons.workflow.spi;

import com.navinfo.navicommons.workflow.flow.WorkFlowContext;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-6
 */
public interface Processor
{
    public boolean doPreProcess(WorkFlowContext ctx) throws ProcessException;

    public boolean doProcess(WorkFlowContext ctx) throws ProcessException;
    
    public void doAfterProcess(WorkFlowContext ctx) throws ProcessException;

    public void doRecover(WorkFlowContext ctx) throws ProcessException;
}
