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
public class ParaDef implements DefineAble
{
    private List<Para> paras;

    public void build(Element e)
    {
        List<Element> parasEl = e.elements("para");
        paras = new ArrayList<Para>(parasEl.size());
        for(Element element : parasEl)
        {
            Para para = new Para();
            para.build(element);
            paras.add(para);
        }
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("paraDef");
        for(Para para : paras)
        {
            e.add(para.serialaze());
        }
        return e;
    }

    public List<Para> getParas() {
        return paras;
    }

    public void setParas(List<Para> paras) {
        this.paras = paras;
    }
}
