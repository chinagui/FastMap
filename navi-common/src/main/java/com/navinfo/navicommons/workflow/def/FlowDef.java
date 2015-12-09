package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class FlowDef implements DefineAble
{
    private StartPoint startPoint;
    private StepDef stepDef;

    public void build(Element e)
    {
        Element startPointEl = e.element("startPoint");
        Element stepDefEl = e.element("stepDef");
        startPoint = new StartPoint();
        startPoint.build(startPointEl);
        stepDef = new StepDef();
        stepDef.build(stepDefEl);
        
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("flowDef");
        e.add(startPoint.serialaze());
        e.add(stepDef.serialaze());
        return e;
    }

    public StartPoint getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(StartPoint startPoint) {
        this.startPoint = startPoint;
    }

    public StepDef getStepDef() {
        return stepDef;
    }

    public void setStepDef(StepDef stepDef) {
        this.stepDef = stepDef;
    }
}
