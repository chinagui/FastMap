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
public class ClassDef implements DefineAble
{
    private List<Clazz> clazzes;


    public void build(Element e) 
    {
        List<Element> clazzEls =  e.elements("class");
        clazzes = new ArrayList<Clazz>(clazzEls.size());
        for (Element element : clazzEls) 
        {
            Clazz clazz = new Clazz();
            clazz.build(element);
            clazzes.add(clazz);
        }

    }

    public Element serialaze()
    {
        Element e = new DefaultElement("classDef");
        for(Clazz c : clazzes)
        {
            e.add(c.serialaze());
        }
        return e;
    }

    public List<Clazz> getClazzes() {
        return clazzes;
    }

    public void setClazzes(List<Clazz> clazzes) {
        this.clazzes = clazzes;
    }

   public Clazz getClazz(String id)
   {
        if(clazzes != null)
        {
            Clazz clazz = new Clazz();
            clazz.setId(id);
            int i = clazzes.indexOf(clazz);
            if(i != -1)
            {
                return clazzes.get(i);
            }

        }
       return null;
   }
}
