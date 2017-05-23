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
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDNODE\",\"dbId\":13,\"subtaskId\":1,\"objId\":510000137," +
                "\"data\":{\"longitude\":116.53250521156434,\"latitude\":39.73986262543881}}";
        TestUtil.run(requester);
    }

    @Test
    public void testMoveCmgBuildnode() {
        String requester = "{\"command\":\"MOVE\",\"type\":\"CMGBUILDNODE\",\"dbId\":13,\"subtaskId\":1,\"objId\":405000029," +
                "\"data\":{\"longitude\":116.52350664138794,\"latitude\":39.74985231943386}}";
        TestUtil.run(requester);
    }
}
