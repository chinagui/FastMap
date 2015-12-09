package com.navinfo.navicommons.workflow.instance;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.navinfo.navicommons.workflow.def.DefineAble;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class FlowInstance implements DefineAble
{
    private String id;
    private String defPk;
    private boolean closed;
    private FlowData flowData;

    public void build(Element e)
    {
        id = e.attributeValue("id");
        defPk = e.attributeValue("defPk");
        closed = Boolean.valueOf(e.attributeValue("closed"));
        Element flowDataEl = e.element("flow");
        flowData = new FlowData();
        flowData.build(flowDataEl);
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("instance");
        e.addAttribute("id",id);
        e.addAttribute("closed",Boolean.toString(closed));
        e.addAttribute("defPk",defPk);
        e.add(flowData.serialaze());
        return e;
    }

    public void addStepData(StepData stepData)
    {
        if(flowData == null)
        {
            flowData = new FlowData();
            flowData.setActive(true);
        }
        flowData.addStepData(stepData);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefPk() {
        return defPk;
    }

    public void setDefPk(String defPk) {
        this.defPk = defPk;
    }

    public FlowData getFlowData() {
        return flowData;
    }

    public void setFlowData(FlowData flowData) {
        this.flowData = flowData;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public StepData getLastStepData()
    {
        return flowData.getLastStepData();
    }

    public StepData getPreStepData()
    {
        return flowData.getPreStepData();
    }

}
