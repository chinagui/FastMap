package com.navinfo.navicommons.workflow.flow;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.navinfo.navicommons.utils.DateUtilsEx;
import com.navinfo.navicommons.workflow.def.Clazz;
import com.navinfo.navicommons.workflow.def.Def;
import com.navinfo.navicommons.workflow.def.DefineException;
import com.navinfo.navicommons.workflow.def.Step;
import com.navinfo.navicommons.workflow.instance.FlowData;
import com.navinfo.navicommons.workflow.instance.FlowInstance;
import com.navinfo.navicommons.workflow.instance.StepData;
import com.navinfo.navicommons.workflow.spi.ProcessException;
import com.navinfo.navicommons.workflow.spi.Processor;
import com.navinfo.navicommons.workflow.spi.StepCalc;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-10
 */
public abstract class AbstractEngine implements Engine 
{
    private static final transient Logger log = Logger.getLogger(AbstractEngine.class);
    protected Def def;
    protected FlowInstance flowInstance;
    protected WorkFlowContext workFlowContext;

    public AbstractEngine(WorkFlowContext workFlowContext)
    {
        this.workFlowContext = workFlowContext;
    }

    protected Def loadDef(String defPk)
    {
        return workFlowContext.getPersistor().loadDefine(defPk);
    }

    protected FlowInstance loadFlowInstance(String flowInstancePk)
    {
        return workFlowContext.getPersistor().loadFlowInstance(flowInstancePk);
    }


    protected void start() throws ProcessException,DefineException
    {
        Step step = def.getStartStep();
        run(step,false);
    }

    protected void recover() throws ProcessException,IlleagStateException,DefineException
    {
        //检查流程状态,有问题抛出异常
        checkState();
        //得到最后执行的一步
        StepData stepData = flowInstance.getLastStepData();
        //恢复context
        recoverContext(stepData);
        //已经执行过了,找下一步
        if(stepData.isExecuted())
        {
            Step nextStep = def.getStep(stepData.getNextStep());
            run(nextStep,false);
        }
        //未执行完成，preRun已经完成，则恢复后再执行
        else
        {
            reExecuteUnfinishStep(stepData);
        }

    }

    protected void reExecuteUnfinishStep(StepData currentStepData) throws ProcessException, DefineException
    {
        Step currentStep = def.getStep(currentStepData.getName());
        boolean success = executeProcessor(currentStep.getProcessor(),true);
        Step nextStep = afterRun(currentStep,currentStepData,success);
        runNextStep(nextStep);
    }

    protected void checkState() throws IlleagStateException
    {
        if(flowInstance.isClosed())
        {
            log.error("流程已经关闭，无法执行恢复");
            throw new IlleagStateException("流程已经关闭，无法执行恢复");
        }
    }

    protected void recoverContext(StepData stepData)
    {
        WorkFlowContext oldCtx = stepData.getWorkFlowContext();
        oldCtx.setPersistor(workFlowContext.getPersistor());
        workFlowContext = oldCtx;
    }

    protected void run(Step step,boolean needRecover) throws ProcessException,DefineException
    {
        //processor执行前写步骤开始
        StepData currentStepData = preRun(step);
        //执行processor
        boolean success = executeProcessor(step.getProcessor(),needRecover);
        //执行完后写步骤结束,并返回下一步
        Step nextStep = afterRun(step,currentStepData,success);
        //执行下一步
        runNextStep(nextStep);
    }

    protected void runNextStep(Step nextStep) throws ProcessException, DefineException
    {
        if(nextStep != null)
        {
            run(nextStep,false);
        }
        //没有下一步了，流程结束
        else
        {
            finishFlow();
        }
    }

    protected void finishFlow()
    {
        flowInstance.setClosed(true);
        workFlowContext.getPersistor().saveFlowInstance(flowInstance);
    }

    protected boolean executeProcessor(String processId,boolean needRecover) throws ProcessException,DefineException
    {
        boolean success = false;
        Clazz clazz = def.getClazz(processId);
        Processor processor = (Processor)createComponentByName(clazz);
        if(needRecover)
        {
            processor.doRecover(workFlowContext);
        }
        if(processor.doPreProcess(workFlowContext))
        {
            success = processor.doProcess(workFlowContext);
            processor.doAfterProcess(workFlowContext);
        }
        return success;
    }

    protected StepData preRun(Step step)
    {
        StepData preStepData = flowInstance.getPreStepData();
        StepData stepData = new StepData();
        stepData.setName(step.getName());
        stepData.setBeginTime(DateUtilsEx.getCurTime());
        if(preStepData != null)
        {
            stepData.setPreStep(preStepData.getName());
            stepData.setSeq(preStepData.getSeq() + 1);
        }
        else
        {
            stepData.setSeq(1);
        }
        stepData.setWorkFlowContext(workFlowContext.clone());
        flowInstance.addStepData(stepData);
        workFlowContext.getPersistor().saveFlowInstance(flowInstance);
        return stepData;
    }
    
    protected Step afterRun(Step step,StepData currentStepData,boolean success) throws DefineException
    {
        Step nextStep = getNextStep(step);
        currentStepData.setNextStep(nextStep == null ? null : nextStep.getName());
        currentStepData.setEndTime(DateUtilsEx.getCurTime());
        currentStepData.setExecuted(true);
        currentStepData.setSuccess(success);
        workFlowContext.getPersistor().saveFlowInstance(flowInstance);
        return nextStep;
    }

    private Step getNextStep(String currentStep) throws DefineException
    {
        Step step =  def.getStep(currentStep);
        return getNextStep(step);
    }

    private Step getNextStep(Step currentStep) throws DefineException
    {
        if(currentStep == null)
            return null;
        if(currentStep.getNextStep() != null)
            return def.getStep(currentStep.getNextStep());
        if(currentStep.getNextStepCalc() != null)
        {
            Clazz clazz = def.getClazz(currentStep.getNextStepCalc());
            StepCalc stepCalc = (StepCalc)createComponentByName(clazz);
            return def.getStep(stepCalc.getNextStep(workFlowContext));
        }
        return null;
    }


    protected FlowInstance createDefaultFlowInstance()
    {
        FlowInstance flowInstance = new FlowInstance();
        flowInstance.setDefPk(def.getId());
        flowInstance.setId(UUID.randomUUID().toString().replace("-",""));
        flowInstance.setFlowData(new FlowData());
        return flowInstance;
    }

    public Object createComponentByName(Clazz clazz)throws DefineException
    {
        Object o = null;
        try
        {
            o = Class.forName(clazz.getName()).newInstance();
        } catch (Exception e)
        {
            log.error("初始化处理器时失败：" + clazz.getName(),e);
            throw new DefineException("初始化处理器时失败：" + clazz.getName(),e);
        }
        return o;                
    }

    public String getInstanceId() 
    {
        return flowInstance == null ? null : flowInstance.getId();
    }

    public WorkFlowContext getWorkFlowContext()
    {
        return workFlowContext;
    }
}
