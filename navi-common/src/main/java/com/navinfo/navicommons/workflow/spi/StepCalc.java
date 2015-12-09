package com.navinfo.navicommons.workflow.spi;

import com.navinfo.navicommons.workflow.flow.WorkFlowContext;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-13
 */
public interface StepCalc
{
    public String getNextStep(WorkFlowContext ctx);
}
