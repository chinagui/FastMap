package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class Step implements DefineAble
{
    private String name;
    private String processor;
    private String nextStep;
    private String nextStepCalc;
    private String desc;


    public void build(Element e)
    {
        name = e.attributeValue("name");
        processor = e.attributeValue("processor");
        nextStep = e.attributeValue("nextStep");
        nextStepCalc = e.attributeValue("nextStepCalc");
        desc = e.attributeValue("desc");
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("step");
        e
                .addAttribute("name",name)
                .addAttribute("processor",processor)
                .addAttribute("nextStep",nextStep)
                .addAttribute("desc",desc)
                .addAttribute("nextStepCalc",nextStepCalc);
        return e;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    public String getNextStepCalc() {
        return nextStepCalc;
    }

    public void setNextStepCalc(String nextStepCalc) {
        this.nextStepCalc = nextStepCalc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Step step = (Step) o;

        return !(name != null ? !name.equals(step.name) : step.name != null);
    }
}
