package com.navinfo.dataservice.engine.limit.glm.iface;

import com.navinfo.dataservice.dao.glm.iface.OperType;

/**
 * Created by ly on 2017/9/20.
 */
public interface ICommand {

    /**
     * @return 操作类型
     */
    public OperType getOperType();

    /**
     * @return 数据库类型
     */
    public DbType getDbType();

    /**
     * @return 请求参数
     */
    public String getRequester();


    /**
     * @return 操作对象类型
     */
    public LimitObjType getObjType();
}
