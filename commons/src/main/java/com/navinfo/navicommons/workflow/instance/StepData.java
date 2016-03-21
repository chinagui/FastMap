package com.navinfo.navicommons.workflow.instance;

import java.sql.Timestamp;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.navinfo.navicommons.utils.DateUtilsEx;
import com.navinfo.navicommons.workflow.def.DefineAble;
import com.navinfo.navicommons.workflow.flow.WorkFlowContext;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class StepData implements DefineAble
{
    private int seq;
    private String preStep;
    private String nextStep;
    private String name;
    private boolean executed;
    private Timestamp beginTime;
    private Timestamp endTime;
    private boolean success;
    private WorkFlowContext workFlowContext;//记录该步执行前的contex


    public void build(Element e)
    {
        seq = Integer.parseInt(e.attributeValue("seq"));
        preStep = e.attributeValue("preStep");
        nextStep = e.attributeValue("nextStep");
        name = e.attributeValue("name");
        executed = Boolean.valueOf(e.attributeValue("executed"));
        beginTime = DateUtilsEx.getTimeOfTimeStr(e.attributeValue("beginTime"));
        endTime = DateUtilsEx.getTimeOfTimeStr(e.attributeValue("endTime"));
        success = Boolean.valueOf(e.attributeValue("success"));
        Element contextEl = e.element("context");
        workFlowContext = new WorkFlowContext();
        workFlowContext.build(contextEl);
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("step");
        e
                .addAttribute("seq",Integer.toString(seq))
                .addAttribute("preStep",preStep)
                .addAttribute("nextStep",nextStep)
                .addAttribute("name",name)
                .addAttribute("executed",Boolean.toString(executed))
                .addAttribute("beginTime",DateUtilsEx.getTimeStr(beginTime,"yyyy-MM-dd HH:mm:ss"))
                .addAttribute("endTime",DateUtilsEx.getTimeStr(endTime,"yyyy-MM-dd HH:mm:ss"))
                .addAttribute("success",Boolean.toString(success));
        e.add(workFlowContext.serialaze());
        return e;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getPreStep() {
        return preStep;
    }

    public void setPreStep(String preStep) {
        this.preStep = preStep;
    }

    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public Timestamp getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public WorkFlowContext getWorkFlowContext() {
        return workFlowContext;
    }

    public void setWorkFlowContext(WorkFlowContext workFlowContext) {
        this.workFlowContext = workFlowContext;
    }
}
