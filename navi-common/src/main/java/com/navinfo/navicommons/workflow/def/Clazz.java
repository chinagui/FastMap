package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class Clazz implements DefineAble
{
    private String id;
    private String type;
    private String name;


    public void build(Element e)
    {
        id = e.attributeValue("id");
        type = e.attributeValue("type");
        name = e.attributeValue("name");
    }

    public Element serialaze()
    {
        Element e = new DefaultElement("class");
        e.addAttribute("id",id).addAttribute("type",type).addAttribute("name",name);
        return e;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Clazz clazz = (Clazz) o;

        return !(id != null ? !id.equals(clazz.id) : clazz.id != null);

    }
    
}
