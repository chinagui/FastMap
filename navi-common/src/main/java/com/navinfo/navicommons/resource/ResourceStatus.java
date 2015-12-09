package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public enum ResourceStatus
{
    mainLocked(1,"主处理程序锁定"),
    mainLockFree(2,"主处理程序完成"),
    webgisDataProcessLocked(3,"webgis数据处理程序锁定"),
    webgisDataProcessLockFree(4,"webgis数据处理程序完成"),
    webgisViewLocked(5,"webgis显示占用"),
    free(9,"空闲");
    private int code;
    private String desc;

    private ResourceStatus(int code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
