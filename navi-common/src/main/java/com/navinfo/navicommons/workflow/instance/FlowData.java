package com.navinfo.navicommons.workflow.instance;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.navinfo.navicommons.workflow.def.DefineAble;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class FlowData implements DefineAble
{
    private List<StepData> stepDatas;

    private boolean active;


    public void build(Element e)
    {
        active = Boolean.valueOf(e.attributeValue("active"));
        List<Element> stepDatasEl = e.elements("step");
        stepDatas = new ArrayList<StepData>(stepDatasEl.size());
        for(Element element : stepDatasEl)
        {
            StepData stepData = new StepData();
            stepData.build(element);
            stepDatas.add(stepData);
        }
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("flow");
        e.addAttribute("active",Boolean.toString(active));
        for(StepData stepData : stepDatas)
        {
            e.add(stepData.serialaze());
        }
        return e;
    }

    public void addStepData(StepData stepData)
    {
        if(stepDatas == null)
        {
            stepDatas = new ArrayList<StepData>();
        }
        stepDatas.add(stepData);
    }


    public List<StepData> getStepDatas() {
        return stepDatas;
    }

    public void setStepDatas(List<StepData> stepDatas) {
        this.stepDatas = stepDatas;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public StepData getLastStepData()
    {
        if(stepDatas != null)
        {
            return stepDatas.get(stepDatas.size() - 1);
        }
        return null;
    }

    public StepData getPreStepData()
    {
        if(stepDatas != null && stepDatas.size() >= 1)
        {
            return stepDatas.get(stepDatas.size() - 1);
        }
        return null;
    }


}
