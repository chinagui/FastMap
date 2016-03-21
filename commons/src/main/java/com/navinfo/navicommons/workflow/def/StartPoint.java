package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class StartPoint implements DefineAble
{
    private String step;

    public void build(Element e)
    {
        step = e.attributeValue("step");
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("startPoint");
        e.addAttribute("step",step);
        return e;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
