package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/10/13 0013.
 */
public class RdCrossTest extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void edit() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDCROSS\",\"dbId\":13,\"subtaskId\":357," +
                "\"data\":{\"nodePids\":[408000280,410000326],\"linkPids\":[405000418]}}";
        TestUtil.run(parameter);
    }
}
