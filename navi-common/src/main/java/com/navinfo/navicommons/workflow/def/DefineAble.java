package com.navinfo.navicommons.workflow.def;

import org.dom4j.Element;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public interface DefineAble
{
    public void build(Element e);

    public Element serialaze();
}
