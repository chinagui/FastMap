package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/11 0011.
 */
public class RdHgwgLimit extends InitApplication{
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void create(){
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDHGWGLIMIT\",\"dbId\":17,\"data\":{\"direct\":2,\"linkPid\":200002589,\"latitude\":40.01223045143216,\"longitude\":116.47890601535886}}";
        TestUtil.run(parameter);
    }
}
