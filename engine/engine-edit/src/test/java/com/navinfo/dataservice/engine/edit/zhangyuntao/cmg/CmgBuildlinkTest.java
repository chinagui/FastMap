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
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDLINK\",\"dbId\":13,\"subtaskId\":65," +
                "\"data\":{\"sNodePid\":402000042,\"eNodePid\":403000032,\"geometry\":{\"type\":\"LineString\"," +
                "\"coordinates\":[[116.99396878480911,39.99996079681034],[116.99401035904886,40.00013339076144],[116.9936241209507," +
                "40.0001354454487],[116.99362948536873,39.9999505233471]]},\"catchLinks\":[{\"nodePid\":402000042,\"seqNum\":0}," +
                "{\"nodePid\":403000032,\"seqNum\":3}]}}";
        TestUtil.run(requester);
    }

    @Test
    public void testDeleteCmgBuildlink() {
        String requester = "{\"command\":\"DELETE\",\"dbId\":13,\"type\":\"CMGBUILDLINK\",\"objId\":505000007}";
        TestUtil.run(requester);
    }

    @Test
    public void testRepairCmgBuildlink() {
        String requester = "{\"command\":\"REPAIR\",\"type\":\"CMGBUILDLINK\",\"objId\":520000022,\"dbId\":13,\"subtaskId\":65," +
                "\"data\":{\"type\":\"CMGBUILDLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.89272,39.9997]," +
                "[116.89391434192656,40.000325503753466]]},\"catchInfos\":[{\"nodePid\":504000029,\"longitude\":116.89272," +
                "\"latitude\":39.9997},{\"nodePid\":501000038,\"longitude\":116.89391434192656,\"latitude\":40.000325503753466}]}}";
        TestUtil.run(requester);
    }
}