package com.navinfo.dataservice.engine.edit.zhangyuntao.cmg;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * @Title: CmgBuildlinkTest
 * @Package: com.navinfo.dataservice.engine.edit.zhangyuntao.cmg
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class CmgBuildlinkTest extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void testCreaCMGBUILDLINKteLink(){
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0," +
                "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[127.0669363439083,33.9616218843058],[127.06732124090196," +
                "33.96170864585347]]},\"catchLinks\":[]},\"dbId\":84}";
        TestUtil.run(requester);
    }
}
