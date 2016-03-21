package com.navinfo.navicommons.workflow.spi;

import com.navinfo.navicommons.workflow.flow.WorkFlowContext;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-13
 */
public class ExampleProcess implements Processor
{
    public boolean doPreProcess(WorkFlowContext ctx) throws ProcessException
    {
        System.out.println("==doPreProcess==");
        return true;
    }

    public boolean doProcess(WorkFlowContext ctx) throws ProcessException
    {
        System.out.println("==doProcess==");
        return true;
    }

    public void doAfterProcess(WorkFlowContext ctx) throws ProcessException
    {
        System.out.println("==doAfterProcess==");
    }

    public void doRecover(WorkFlowContext ctx) throws ProcessException
    {
        System.out.println("==doRecover==");
    }
}
