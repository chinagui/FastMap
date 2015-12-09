package com.navinfo.navicommons.workflow.flow;

import com.navinfo.navicommons.workflow.def.DefineException;
import com.navinfo.navicommons.workflow.persistor.DBPersistor;
import com.navinfo.navicommons.workflow.spi.ProcessException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class WorkFlowEngine extends AbstractEngine
{

    public WorkFlowEngine(WorkFlowContext workFlowContext)
    {
        super(workFlowContext);
    }

    public String start(String defPk)  throws ProcessException, DefineException
    {
        //获取流程定义
        def = loadDef(defPk);
        //创建初始流程实例
        flowInstance = createDefaultFlowInstance();
        //执行流程
        start();
        return flowInstance.getId();
    }

    public void recover(String flowInstancePk)  throws ProcessException,IlleagStateException,DefineException
    {
        //恢复流程实例
        flowInstance = loadFlowInstance(flowInstancePk);
        //获取流程定义
        def = loadDef(flowInstance.getDefPk());
        //执行流程
        recover();
    }

    public static void main(String[] args) throws Exception
    {
        WorkFlowContext ctx = new WorkFlowContext();
        ctx.setPersistor(new DBPersistor());
        WorkFlowEngine workFlowEngine = new WorkFlowEngine(ctx);
        workFlowEngine.start("");
    }

}
