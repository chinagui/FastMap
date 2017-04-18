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
    public void testCreateCmgBuildlink() {
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0," +
                "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[127.0669363439083,33.9616218843058],[127.06732124090196," +
                "33.96170864585347]]},\"catchLinks\":[]},\"dbId\":84}";
        TestUtil.run(requester);
    }

    @Test
    public void testDeleteCmgBuildlink() {
        String requester = "{\"command\":\"DELETE\",\"dbId\":13,\"type\":\"CMGBUILDLINK\",\"objId\":505000007}";
        TestUtil.run(requester);
    }

    @Test
    public void testRepairCmgBuildlink() {
        String requester = "{\"command\":\"REPAIR\",\"type\":\"CMGBUILDLINK\",\"objId\":400000005,\"dbId\":13," +
                "\"data\":{\"type\":\"CMGBUILDLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.5321,39.74285]," +
                "[116.53262615203856,39.743609760516875],[116.53354883193968,39.74352726503679],[116.53340935707092,39.74224857246603]," +
                "[116.53236865997314,39.74179483714053],[116.5316,39.74253],[116.53074860572815,39.741852585438785],[116.53011560440063," +
                "39.742413566389125],[116.53009414672852,39.74353551458925],[116.53132,39.74315],[116.53151035308838,39.74344476945796]," +
                "[116.53210043907164,39.74395624045471],[116.5321,39.74285]]},\"catchInfos\":[{\"nodePid\":508000009," +
                "\"longitude\":116.5321,\"latitude\":39.74285},{\"nodePid\":508000009,\"longitude\":116.5321,\"latitude\":39.74285}]}}";
        TestUtil.run(requester);
    }
}
