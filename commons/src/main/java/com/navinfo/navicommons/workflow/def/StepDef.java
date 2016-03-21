package com.navinfo.navicommons.workflow.def;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class StepDef implements DefineAble
{
    private List<Step> steps;


    public void build(Element e)
    {
        List<Element>stepsEl = e.elements("step");
        steps = new ArrayList<Step>(stepsEl.size());
        for(Element element : stepsEl)
        {
            Step step = new Step();
            step.build(element);
            steps.add(step);
        }
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("stepDef");
        for(Step step : steps)
        {
            e.add(step.serialaze());
        }
        return e;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Step getStep(String stepName)
    {
        Step step = new Step();
        step.setName(stepName);
        if(steps != null)
        {
            int i = steps.indexOf(step);
            if(i != -1)
                return steps.get(i);
        }
        return null;
    }
}
