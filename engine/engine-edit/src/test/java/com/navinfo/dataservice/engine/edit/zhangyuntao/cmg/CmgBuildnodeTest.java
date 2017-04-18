package com.navinfo.dataservice.engine.edit.zhangyuntao.cmg;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * @Title: CmgBuildnodeTest
 * @Package: com.navinfo.dataservice.engine.edit.zhangyuntao.cmg
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class CmgBuildnodeTest extends InitApplication {

    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void testCreateCmgBuildnode() {
        String requester = "{\"command\":\"CREATE\",\"dbId\":84,\"objId\":408000006,\"data\":{\"longitude\":127.0671184778542," +
                "\"latitude\":33.96166322264574},\"type\":\"CMGBUILDNODE\"}";
        TestUtil.run(requester);
    }

    @Test
    public void testMoveCmgBuildnode() {
        String requester = "{\"command\":\"MOVE\",\"objId\":404000004,\"type\":\"CMGBUILDNODE\",\"data\":{\"longitude\":127.0668625831604,"
                + "\"latitude\":33.96168639931121},\"dbId\":84}";
        TestUtil.run(requester);
    }
}
