package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public enum ModuleType
{

    gdb("GDB"),
    roadName("ROAD_NAME"),
    other("OTHER");

    private String type;

    private ModuleType(String code)
    {
        this.type = code;
    }

    public String getType() {
        return type;
    }


}
