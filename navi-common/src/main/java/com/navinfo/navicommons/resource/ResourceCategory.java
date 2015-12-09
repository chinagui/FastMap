package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public enum ResourceCategory
{
    diff("差分工作库"),
    expView("数据导出GIS显示工作库"),
    changeView("变化数据GIS显示工作库"),
    checkView("数据检查工作库"),
    exp("数据导出母库/工作库"),
    expTemp("数据导出需要使用的临时表后缀"),
    imp("数据入库工作库"),
    impTemp("数据入库需要使用的临时表"),
    expDataStore("从母库或工作库导出数据到Oracle需要使用的工作库"),
    roadName("道路名工作库"),
    gdbAssistant("外业成果数据库"),
    assistedImp("外业成果入库"),
    dbCopy("数据库复制"),
    dcsDiff("外业数据差分"),
    naviStat("统计测试库");
    
    
    private String desc;

    private ResourceCategory(String desc)
    {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
