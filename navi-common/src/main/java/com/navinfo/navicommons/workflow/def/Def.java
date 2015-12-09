package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class Def implements DefineAble
{
    private String id;
    private ClassDef classDef;
    private ParaDef paraDef;
    private FlowDef flowDef;

    public void build(Element e)
    {
        Element classDefEl = e.element("classDef");
        Element paraDefEl = e.element("paraDef");
        Element flowDefEl = e.element("flowDef");
        classDef = new ClassDef();
        classDef.build(classDefEl);
        paraDef = new ParaDef();
        paraDef.build(paraDefEl);
        flowDef = new FlowDef();
        flowDef.build(flowDefEl);
        id = e.attributeValue("id");
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("def");
        e.attributeValue("id",id);
        e.add(classDef.serialaze());
        e.add(paraDef.serialaze());
        e.add(flowDef.serialaze());
        return e;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ClassDef getClassDef() {
        return classDef;
    }

    public void setClassDef(ClassDef classDef) {
        this.classDef = classDef;
    }

    public ParaDef getParaDef() {
        return paraDef;
    }

    public void setParaDef(ParaDef paraDef) {
        this.paraDef = paraDef;
    }

    public FlowDef getFlowDef() {
        return flowDef;
    }

    public void setFlowDef(FlowDef flowDef) {
        this.flowDef = flowDef;
    }

    public Step getStep(String name)
    {
        return flowDef.getStepDef().getStep(name);
    }

    public Clazz getClazz(String id)
    {
        return classDef.getClazz(id);
    }

    public StartPoint getStartPoint()
    {
        return flowDef.getStartPoint();    
    }

    public Step getStartStep()
    {
        return getStep(getStartPoint().getStep());
    }
}
