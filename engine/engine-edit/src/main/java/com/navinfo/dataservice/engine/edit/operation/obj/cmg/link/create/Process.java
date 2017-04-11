package com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/10
 * @Version: V1.0
 */
public class Process extends AbstractProcess<Command>{

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
