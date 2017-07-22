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
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDLINK\",\"dbId\":13,\"subtaskId\":65,\"data\":{\"sNodePid\":0," +
                "\"eNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.87499806284903,40.08321439763695]," +
                "[116.87499940395355,40.08349041597817]]},\"catchLinks\":[]}}";
        TestUtil.run(requester);
    }

    @Test
    public void testDeleteCmgBuildlink() {
        String requester = "{\"command\":\"DELETE\",\"dbId\":13,\"type\":\"CMGBUILDLINK\",\"objId\":505000007}";
        TestUtil.run(requester);
    }

    @Test
    public void testRepairCmgBuildlink() {
        String requester = "{\"command\":\"REPAIR\",\"type\":\"CMGBUILDLINK\",\"objId\":502000032,\"dbId\":13,\"subtaskId\":58," +
                "\"data\":{\"type\":\"CMGBUILDLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[123.67841,32.41627]" +
                "[123.67865,32.41596]]},\"catchInfos\":[]}}";
        TestUtil.run(requester);
    }
}