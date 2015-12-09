package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class Para implements DefineAble
{
    private String name;
    private String value;
    private String type;


    public void build(Element e)
    {
        name = e.attributeValue("name");
        value = e.attributeValue("value");
        type = e.attributeValue("type");
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("para");
        e.addAttribute("name",name).addAttribute("value",value).addAttribute("type",type);
        return e;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
