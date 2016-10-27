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
        String parameter = "{\"command\":\"BATCH\",\"type\":\"RDCROSS\",\"dbId\":17,\"data\":{\"pid\":203000025,\"nodePids\":[301001543]}}";
        TestUtil.run(parameter);
    }
}
